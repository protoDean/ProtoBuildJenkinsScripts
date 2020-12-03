import java.text.SimpleDateFormat 
import java.util.Date

def GetUniqueTargetId( targetSetting)
{
	return targetSetting.target + "_" + targetSetting.buildProfile
}

def DoGamePlatform(game , boolean alwaysBuild , gameResult , dailyBuildFolder ) {
    
	String projectFolder = game.projectName 
	String sourceBranch = game.sourceBranch  
	String paramUnityVersion = game.unityVersion

	final SCM_PLASTIC = "plastic"
	final SCM_GIT = "git"

	def sourceControl =  game.sourceControl

	if(sourceControl == null)
	{
		//fallback to git
		sourceControl = SCM_GIT
	}

	String projectPath
	if(sourceControl == SCM_PLASTIC) 
	{
		projectPath = env.PROJECT_PATH_PLASTIC
	}
	else if (sourceControl == SCM_GIT)
	{
		projectPath = env.PROJECT_PATH
	}
	else
	{
		throw "Unknown Source Control " + sourceControl
	}
	

	final KEYCHAIN_ID = "login.keychain"  // "/Users/Shared/Jenkins/Home/Provisioning/jenkinsKeychain.keychain"
	//How long before timeout.
	int timeoutMins = 35

	//jenkins user for github. must have credentials in the keychain
	final JENKINS_GITHUB_USER = "protostarbuildmachine"	

   	final PROFILE_IOS_RELEASE = "iosRelease"
	final PROFILE_IOS_DEBUG = "iosDebug"
	final PROFILE_ANDROID_DEBUG = "googleDebugApk"
	final PROFILE_ANDROID_RELEASE = "googleRelease"

	final TARGET_ANDROID = "Android"
	final TARGET_IOS = "iOS"

	final String POST_BUILD_COPY_TO_NETWORK = "copyToNetwork"
	final String POST_BUILD_UPLOAD = "upload"

	final ARCHIVE_POST_FIX = "_Archive"

	def wereFailures = false;
	
	def buildDescription = "Building ${projectFolder} at branch ${sourceBranch} with ${paramUnityVersion}"

	final NO_CHANGES_FOUND = "no changes found"


	String infoLastCommit = "Unknown"
	String currentRevision = "Unknown" 

	dir(path: "${projectPath}/${projectFolder}")
	{
		if (sourceControl == SCM_GIT)
		{
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsLoginPassword',
				usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
			{

				echo runShell("git version");
				echo runShell("which git");
				
				echo runShell("security -v unlock-keychain -p ${credPassword} ${KEYCHAIN_ID}")
				
				String gitResult = ""

				runShell("git lfs install")
				runShell("git fetch --tags --force https://${JENKINS_GITHUB_USER}@github.com/protoDean/${projectFolder} +refs/heads/*:refs/remotes/origin/*")
				runShell("git checkout -f -B ${sourceBranch} origin/${sourceBranch}")
				runShell("git lfs checkout")
				runShell("git submodule update --init --recursive")
				runShell("git submodule foreach git lfs pull")

				infoLastCommit =  runShell("git log -1 --oneline")

				echo "Most recent commit: \n" + infoLastCommit

				buildDescription += "\nLast Commit: " + infoLastCommit + "\n\n"
				
				currentRevision = runShell("git -C ${projectPath}/${projectFolder} rev-parse HEAD").trim()
			}
		}
		else if (sourceControl == SCM_PLASTIC)
		{
			echo runShell("cm undo -r")
			echo runShell("cm switch ${sourceBranch}");
			echo runShell("cm update");

			currentRevision = runShell("cm status --cset");
			infoLastCommit = "Dont know how got get the comment in plastic. For now \n" + currentRevision;
		}
	}

	def lastBuildNumber = null;
	buildDescription += "Targets: \n"

	for (targetSetting in game.targets) 
	{	
		final String TARGET_ID = GetUniqueTargetId(targetSetting)

		if(targetSetting.disable || game.disable )
		{
			print "Skipping " + game.projectName + " " + TARGET_ID + " - Disabled"
			continue;
		}

		print "Doing " + game.projectName + " " + TARGET_ID
		String target = targetSetting.target
		def gameTargetResult = GetTargetResults(targetSetting.target , gameResult)

		
		

		//Clean out the folder
		dir(path: "${projectPath}/${projectFolder}")
		{
			if (sourceControl == SCM_GIT)
			{
				//Cleans any unknown files (not ignored ones. use -x to clean ignored files too.)
				runShell("git clean -d -f")
				runShell("git submodule foreach --recursive git clean -xfd")
			}
			else if (sourceControl == SCM_PLASTIC)
			{
				echo runShell("cm undo -r")
			}
		}


		if( alwaysBuild == false &&
			gameTargetResult.changeSet == currentRevision && 
			gameTargetResult.targetId == TARGET_ID)
		{
			print "Build Skipping " + projectFolder + " " + target + " - No Changes required."
			return
		}

		//print("Skipping the rest for now");
		//return;

		buildDescription += TARGET_ID + ":"

		def buildPath = "../DailyBuilds/${dailyBuildFolder}"


        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectPath', value: "${projectPath}/${projectFolder}"],
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: paramUnityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath],
			[$class: 'StringParameterValue', name: 'unityBuildTarget', value: target],
			[$class: 'StringParameterValue', name: 'buildProfile', value: targetSetting.buildProfile] 
            ]
		
		
		def buildId = null
		def archivePath = null
		def xCodePath = null
        

		def buildResultString = ""

		try
		{
			
			stage( projectFolder + "-" +  targetSetting.buildProfile) {
				timeout(timeoutMins) 
				{
					def buildParams = commonParams 

//Can override build num with this:
//						buildParams = buildParams + 
//							[[$class: 'StringParameterValue', name: 'buildNumOverride', value: lastBuildNumber]]

					def finalBuildResult = build job: 'UnityBuild', parameters: buildParams, propagate: false, wait: true
						
						
					lastBuildNumber = "" + finalBuildResult.number

					def envVariables = finalBuildResult.getBuildVariables();
					//print "${j1EnvVariables}" 

					buildId = envVariables.unityBuildId;

					if(target == TARGET_ANDROID)
					{
						//Add a little bat to deploy it.
						writeFile(file:"${buildPath}/${buildId}/deployWindows.bat" , text : "D:\\android\\sdk\\platform-tools\\adb.exe install -r build.apk\npause")
					}

					buildResultString = finalBuildResult.getCurrentResult() 

				}
				
			}
		}
		catch(e) {
			//This try catch doesnt seem to work.
			wereFailures = true
			
			echo e.toString()  

			buildResultString = "Failed in catch"
		}



		gameTargetResult.targetId = TARGET_ID
		gameTargetResult.changeSet = currentRevision

		echo "Build Result string:"
		echo buildResultString

	
		gameTargetResult.lastBuildResult = buildResultString
		

		buildDescription += gameTargetResult.lastBuildResult + "\n"
		
		//Post Build Tasks.
		if(targetSetting.postBuild.indexOf(POST_BUILD_COPY_TO_NETWORK) != -1)
		{
			//If successful we want to stash the build artifact somewhere
			if(target == TARGET_ANDROID)
			{
				archivePath = "${buildPath}/${buildId}"
				sh "mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}"
				sh "cp -r ${archivePath} ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/"
			}
			else if(target == TARGET_IOS)
			{
				//Removed archiving for now.
				//xCodePath = "${buildPath}/${buildId}"
				//archivePath = "${xCodePath}${ARCHIVE_POST_FIX}"
				//iOS - archive it.
				//sh "xcodebuild -project ${xCodePath}/Unity-iPhone.xcodeproj archive -archivePath ${archivePath}/${buildId}.xcarchive -configuration Release -scheme Unity-iPhone"
			}
		}


		if(targetSetting.postBuild.indexOf(POST_BUILD_UPLOAD) != -1)
		{
			//If successful we want to stash the build artifact somewhere
			if(target == TARGET_ANDROID)
			{
				//How to upload!
			}
			else if(target == TARGET_IOS)
			{
				stage(projectFolder + "-Upload") 
				{
					try
					{
						//How to upload?
						print("xCode Exporting ipa")
						sh "xcodebuild -exportArchive -allowProvisioningUpdates -archivePath ${archivePath}/${releaseBuildId}.xcarchive -exportOptionsPlist ${xCodePath}/exportOptions.plist -exportPath ${archivePath}/Ipa"


						print("xCode Uploading ipa")
					}
					catch(e) {
						wereFailures = true
						echo e.toString()  
					}
				}
			}
		}
	}

	currentBuild.description = buildDescription
}

def runShell(String command){

    def responseCode = sh returnStatus: true, script: "${command} &> tmp.txt" 

	def output = ""
	try{
    	output =  readFile(file: "tmp.txt")
	}
	catch(e)
	{
		output = "No output, read tmp.txt failed"
	}

    if (responseCode != 0)
	{
      	echo "[ERROR] ${output}"
     	throw new Exception("${output}")
    }else
	{
      return "${output}"
    }
}


def GetGameResults( gameToGet ,  results)
{
	for (game in results.games) 
	{
		String projectFolder = game.projectName 
		String sourceBranch = game.sourceBranch  
		String paramUnityVersion = game.unityVersion

		if(game.projectName == gameToGet.projectName &&
			game.paramUnityVersion == gameToGet.paramUnityVersion)
			{
				return game;
			}

	}

	//add new
	def newGame = [
		projectName : gameToGet.projectName ,
		unityVersion :  gameToGet.unityVersion,
		targets : []
		]

	results.games += newGame 
	return newGame
}

def GetTargetResults( String targetId ,  gameResults)
{
	for (target in gameResults.targets) 
	{
	
		if(target.id == targetId)
		{
			return target;
		}
	}

	//add new
	def newTarget = [
		id : targetId,
		buildLevel : 0,
		changeSet : null,
		lastBuildResult : "Unknown"
	]

	gameResults.targets += newTarget

	return newTarget
}

return this