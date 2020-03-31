import java.text.SimpleDateFormat 
import java.util.Date



def DoGamePlatform(game , targetSetting  , boolean alwaysBuild , gameTargetResult , dailyBuildFolder ) {
    
	String projectFolder = game.projectName 
	String sourceBranch = game.sourceBranch  
	String paramUnityVersion = game.unityVersion
	String target = targetSetting.id
	int buildLevel = targetSetting.buildLevel

	//How long before timeout.
	int timeoutMins = 35
	

   	final PROFILE_IOS_RELEASE = "iosRelease"
	final PROFILE_IOS_DEBUG = "iosDebug"
	final PROFILE_ANDROID_DEBUG = "googleDebugApk"
	final PROFILE_ANDROID_RELEASE = "googleRelease"

	final TARGET_ANDROID = "Android"
	final TARGET_IOS = "iOS"

	final int 	BUILD_NONE = 0 
	final int 	BUILD_DEBUG = 1
	final int 	BUILD_RELEASE = 2
	final int 	BUILD_RELEASE_UPLOAD = 3

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
			sh "/usr/bin/git lfs install"
			sh "/usr/bin/git fetch https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"
			sh "/usr/bin/git checkout -f ${sourceBranch}"
			sh "/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"
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


	if( alwaysBuild == false &&
		gameTargetResult.changeSet == currentRevision && 
		gameTargetResult.buildLevel >= buildLevel)
	{
		print "Build Skipping " + projectFolder + " " + target + " - No Changes required."
		return
	}

	print("Skipping the rest for now");
	return;

	final ARCHIVE_POST_FIX = "_Archive"

	def wereFailures = false;
       
        //Grab the build num from the release build, and make the debug build the same. So we can swap between them.
        def finalBuildNumber = null
       
	    

	  
		def buildPath = "../DailyBuilds/${dailyBuildFolder}"


        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder],
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: paramUnityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath],
			[$class: 'StringParameterValue', name: 'unityBuildTarget', value: target] 
            ]
		
		def buildProfile 
		def releaseBuildId = null
		def archivePath = null
		def xCodePath = null
        
		if(buildLevel >= BUILD_RELEASE)
		{
			try
			{
				buildProfile = (target == TARGET_ANDROID ? PROFILE_ANDROID_RELEASE : PROFILE_IOS_RELEASE)
				stage( projectFolder + "-" + buildProfile) {
					timeout(timeoutMins) 
					{
						def finalBuildResult = build job: 'UnityBuild', parameters: commonParams + [
							[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile ] 
							
							], propagate: true, wait: true
							
							
						finalBuildNumber = "" + finalBuildResult.number

						def envVariables = finalBuildResult.getBuildVariables();
						//print "${j1EnvVariables}" 

						releaseBuildId = envVariables.unityBuildId;

						//If successful we want to stash the build artifact somewhere
						if(target == TARGET_ANDROID)
						{
							archivePath = "${buildPath}/${releaseBuildId}"
							sh "mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}"
							sh "cp -r ${archivePath} ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/"
						}

						else if(target == TARGET_IOS)
						{
							//Removed archiving for now.
							//xCodePath = "${buildPath}/${releaseBuildId}"
							//archivePath = "${xCodePath}${ARCHIVE_POST_FIX}"
							//iOS - archive it.
							//sh "xcodebuild -project ${xCodePath}/Unity-iPhone.xcodeproj archive -archivePath ${archivePath}/${releaseBuildId}.xcarchive -configuration Release -scheme Unity-iPhone"
						}

						//sh "mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}"
						//sh "cp -r ${archivePath} ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/"

						def attachments = [
							[
								text: releaseBuildId + " Success! (${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/${releaseBuildId})" ,
								color: '#00aa00'
							]
						]
						slackSend( attachments: attachments)


					}
					
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  

				def attachments = [
						[
							text: projectFolder + "-" + buildProfile + " Failed! (<${env.BUILD_URL}|Open>)" ,
							color: '#ff0000'
						]
					]

				slackSend( attachments: attachments )
				//slackSend( attachments: attachments , channel : "general" )
			}
		}

		if(buildLevel >= BUILD_DEBUG)
		{	
			try
			{
				buildProfile =  (target == TARGET_ANDROID ? PROFILE_ANDROID_DEBUG : PROFILE_IOS_DEBUG)
				stage(projectFolder + "-" + buildProfile) 
				{
					timeout(timeoutMins) 
					{
						def buildParams = commonParams + [
							[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile]]
							

						//override build num if we did a release
						if(finalBuildNumber)
						{
							buildParams = buildParams + 
								[[$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]]
						}

						def finalBuildResult = build job: 'UnityBuild',  parameters: buildParams, propagate: true, wait: true

						def envVariables = finalBuildResult.getBuildVariables();
						def debugBuildId = envVariables.unityBuildId;

						def slackButton = ""
						if(target == TARGET_ANDROID)
						{
							//Add a little bat to deploy it.
							writeFile(file:"${buildPath}/${debugBuildId}/deployWindows.bat" , text : "D:\\android\\sdk\\platform-tools\\adb.exe install -r build.apk\npause")

							//Copy the apk too
							archivePath = "${buildPath}/${debugBuildId}"
							sh "mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}"
							sh "cp -r ${archivePath} ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/"

							slackButton = "(${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/${debugBuildId})"
						}

						def attachments = [
							[
								text: debugBuildId + " Success! "+ slackButton  ,
								color: '#00aa00'
							]
						]
						slackSend( attachments: attachments)
					}
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  

					def attachments = [
						[
							text: projectFolder + "-" + buildProfile + " Failed! (<${env.BUILD_URL}|Open>)" ,
							color: '#ff0000'
						]
					]
					
				slackSend( attachments: attachments )
			}
		}

		
		if(wereFailures == false)
		{
			gameTargetResult.buildLevel = buildLevel
			gameTargetResult.changeSet = currentRevision
			gameTargetResult.lastBuildResult = "Success"
		}
		else
		{
			gameTargetResult.lastBuildResult = "Failed"
		}
		

		if(buildLevel >= BUILD_RELEASE_UPLOAD)
		{
			print("Uploading Build! Not currently working sorry.");
			try
			{
				stage(projectFolder + "-Upload") 
				{
					timeout(timeoutMins) 
					{
						if(releaseBuildId)
						{
							print("Uploading at " + "DailyBuild" + currentBuild.number + "/" + releaseBuildId)

							//iOS - 
							if(target == TARGET_ANDROID)
							{
								print("Hmmm... not sure how to upload an android build.")
							}
							else if(target == TARGET_IOS)
							{
								//iOS - archive it.
								print("xCode Exporting ipa")
								sh "xcodebuild -exportArchive -allowProvisioningUpdates -archivePath ${archivePath}/${releaseBuildId}.xcarchive -exportOptionsPlist ${xCodePath}/exportOptions.plist -exportPath ${archivePath}/Ipa"


								print("xCode Uploading ipa")
								//altool --upload-app -f "CLI.ipa" -u $USERNAME -p $PASSWORD
							}
						}
						else
						{
							print("Build id not set. Must have failed")
							sh "exit 1"
						}
					}
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  
			}
		}

        //rename folder
        //sh "mv -v " + DAILY_BUILD_TEMP + " " + dailyBuildFolder
        //Upload Release build if neccessary.
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