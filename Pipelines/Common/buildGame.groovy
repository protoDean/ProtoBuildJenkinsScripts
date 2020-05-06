import java.text.SimpleDateFormat 
import java.util.Date

def GetUniqueTargetId( targetSetting)
{
	return targetSetting.target + "_" + targetSetting.profile
}

def DoGamePlatform(game , boolean alwaysBuild , gameResult , dailyBuildFolder ) {
    
	String projectFolder = game.projectName 
	String sourceBranch = game.sourceBranch  
	String paramUnityVersion = game.unityVersion
	

	//How long before timeout.
	int timeoutMins = 35
	

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
	
	

	final NO_CHANGES_FOUND = "no changes found"

	
	/* 
	//From Local Mercurial
	//check for incoming
	//def incoming = runShell("hg incoming -R ${env.PROJECT_PATH}/${projectFolder} --branch ${sourceBranch} --template {desc}");

	//Update Source
	sh "/usr/local/bin/hg pull -R ${env.PROJECT_PATH}/${projectFolder}"
	sh "/usr/local/bin/hg update " + sourceBranch + " -R ${PROJECT_PATH}/${projectFolder} -C"

	//Check against existing builds.
	def currentRevision = runShell("hg identify -i -R ${env.PROJECT_PATH}/${projectFolder}").trim()
	*/

	//From GitHub
	//check for incoming
	
	//def incoming = "TODO List incoming Changes" // runShell("hg incoming -R ${env.PROJECT_PATH}/${projectFolder} --branch ${sourceBranch} --template {desc}");


	dir(path: "${env.PROJECT_PATH}/${projectFolder}")
	{

		//git(url:"https://github.com/protoDean/${projectFolder}", branch: "${sourceBranch}" , credentialsId:"JenkinsGithubLogin")
		
		withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsGithubLogin',
		usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
		{
			//sh "/usr/bin/git lfs install"
		
			//sh "/usr/bin/git checkout -f ${sourceBranch}"
			//sh "/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"

			//sh "/usr/bin/git remote --set-url origin https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
			//	"/usr/bin/git fetch --tags --force origin &&" +
			//	"/usr/bin/git checkout -f -B ${sourceBranch} origin/${sourceBranch} &&" +
			//	"/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
			//	"/usr/bin/git clean -d -f"		//Cleans any unknown files (not ignored ones. use -x to clean ignored files too.)


			//Set it back to the non passwork version.
			//sh "/usr/bin/git remote --set-url origin https://github.com/protoDean/${projectFolder}" 

				sh "/usr/bin/git fetch --tags --force https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} +refs/heads/*:refs/remotes/origin/* &&" +
				"/usr/bin/git checkout -f -B ${sourceBranch} origin/${sourceBranch} &&" +
				"/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
				"/usr/bin/git clean -d -f"		//Cleans any unknown files (not ignored ones. use -x to clean ignored files too.)
				

			echo "Most recent commit \n"
			echo runShell("/usr/bin/git log -1 --oneline")
			
		}
		
		
	}

	//Update Source
	// https://github.com/protoDean/${projectFolder}.git
	//sh "cd ${env.PROJECT_PATH}/${projectFolder} && /usr/bin/git fetch"
	//sh "cd ${env.PROJECT_PATH}/${projectFolder} && /usr/bin/git checkout -f ${sourceBranch}"
	//sh "/usr/local/bin/hg update " + sourceBranch + " -R ${PROJECT_PATH}/${projectFolder} -C"
	// Get the changeset git describe --abbrev=12 --always
	// git rev-parse HEAD  :Gets the hash of the HEAD, where we are.
	// git rev-parse master : Gets the hash of the branch?

	//Check against existing builds.
	def currentRevision = runShell("/usr/bin/git -C ${env.PROJECT_PATH}/${projectFolder} rev-parse HEAD").trim()


	def lastBuildNumber = null;

	for (targetSetting in game.targets) 
	{	
		if(targetSetting.disable || game.disable )
		{
			print "Skipping " + game.projectName + " " + DailyBuildCode.GetUniqueTargetId(target) + " - Disabled"
			continue;
		}

		print "Doing " + game.projectName + " " + DailyBuildCode.GetUniqueTargetId(target)
		String target = targetSetting.target
		def gameTargetResult = DailyBuildCode.GetTargetResults(target.id , gameResult)

		final String TARGET_ID = GetUniqueTargetId(targetSetting)

		//Clean out the folder
		dir(path: "${env.PROJECT_PATH}/${projectFolder}")
		{
			sh "/usr/bin/git clean -d -f"
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

		def buildPath = "../DailyBuilds/${dailyBuildFolder}"


        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder],
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: paramUnityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath],
			[$class: 'StringParameterValue', name: 'unityBuildTarget', value: target] 
            ]
		
		def buildProfile 
		def buildId = null
		def archivePath = null
		def xCodePath = null
        
		try
		{
			
			stage( projectFolder + "-" +  targetSetting.buildProfile) {
				timeout(timeoutMins) 
				{
					def buildParams = commonParams + [
						[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile]]

//Can override build num with this:
//						buildParams = buildParams + 
//							[[$class: 'StringParameterValue', name: 'buildNumOverride', value: lastBuildNumber]]

					def finalBuildResult = build job: 'UnityBuild', parameters: buildParams, propagate: true, wait: true
						
						
					lastBuildNumber = "" + finalBuildResult.number

					def envVariables = finalBuildResult.getBuildVariables();
					//print "${j1EnvVariables}" 

					buildId = envVariables.unityBuildId;

					if(target == TARGET_ANDROID)
					{
						//Add a little bat to deploy it.
						writeFile(file:"${buildPath}/${buildId}/deployWindows.bat" , text : "D:\\android\\sdk\\platform-tools\\adb.exe install -r build.apk\npause")
					}

				}
				
			}
		}
		catch(e) {
			wereFailures = true
			echo e.toString()  
		}

		if(wereFailures == false)
		{
			gameTargetResult.targetId = TARGET_ID
			gameTargetResult.changeSet = currentRevision
			gameTargetResult.lastBuildResult = "Success"
		}
		else
		{
			gameTargetResult.lastBuildResult = "Failed"
		}
		
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
}

def runShell(String command){

    def responseCode = sh returnStatus: true, script: "${command} &> tmp.txt" 

    def output =  readFile(file: "tmp.txt")
	
   // if (responseCode != 0){
   //   println "[ERROR] ${output}"
   //   throw new Exception("${output}")
   // }else{
      return "${output}"
    //}
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