def DoGamePlatform(String projectFolder , String sourceBranch ,  String paramUnityVersion , String target , int buildLevel , boolean alwaysBuild) {
        
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

	//check for incoming
	def incoming = runShell("hg incoming -R ${env.PROJECT_PATH}/${projectFolder} --branch ${sourceBranch} --template {desc}");

	if(incoming.indexOf(NO_CHANGES_FOUND) < 0)
	{

		def currentBranch = runShell("hg identify -b -R ${env.PROJECT_PATH}/${projectFolder}");

		if(currentBranch.equalsIgnoreCase(sourceBranch))
		{
			//No new changes
			def attachments = [
							[
								text: "New Changes: \n" + incoming ,
								color: '#00aa00'
							]
						]

			slackSend( attachments: attachments )
		}
		else
		{
			slackSend("Source branch has changed from " + currentBranch + " to " + sourceBranch)
		}
	}
	else
	{
		def msgText = alwaysBuild ? "No changes found but building anyway." :
				"No New Changes found in ${projectFolder} branch ${sourceBranch}. Skipping."
		//No new changes
		def attachments = [
						[
							text: msgText ,
							color: '#00aa00'
						]
					]

		slackSend( attachments: attachments )
		
		
		if(alwaysBuild == false)
		{
			print "Skipping. No new changes"
			return;
		}
	}

	//Update Source
	//checkout poll: false, scm: [$class: 'MercurialSCM', credentialsId: '', installation: 'Mercurial Default', revision: sourceBranch, source: "${env.PROJECT_PATH}/${projectFolder}"]
	sh "/usr/local/bin/hg pull -R ${env.PROJECT_PATH}/${projectFolder}"
	sh "/usr/local/bin/hg update " + sourceBranch + " -R ${PROJECT_PATH}/${projectFolder} -C"

	final ARCHIVE_POST_FIX = "_Archive"

	def wereFailures = false;
       
        //Grab the build num from the release build, and make the debug build the same. So we can swap between them.
        def finalBuildNumber = null
       
	    def dateFormat = new SimpleDateFormat("yyyy-MMdd-HHmm")
   		def date = new Date()

	   def dailyBuildFolder = "DailyBuild_" + dateFormat.format(date)
		def buildPath = "../DailyBuilds/${dailyBuildFolder}"


        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder],
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: paramUnityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath],
			[$class: 'StringParameterValue', name: 'buildTarget', value: target] 
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
					}
					else if(target == TARGET_IOS)
					{
						
						xCodePath = "${buildPath}/${releaseBuildId}"
						archivePath = "${xCodePath}${ARCHIVE_POST_FIX}"
						//iOS - archive it.
						sh "xcodebuild -project ${xCodePath}/Unity-iPhone.xcodeproj archive -archivePath ${archivePath}/${releaseBuildId}.xcarchive -configuration Release -scheme Unity-iPhone"
					}

					sh "mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}"
					sh "cp -r ${archivePath} ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/"

					def attachments = [
						[
							text: releaseBuildId + " Success! (${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/${releaseBuildId})" ,
							color: '#00aa00'
						]
					]
					slackSend( attachments: attachments)
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
				stage(projectFolder + "-" + buildProfile) {
			
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

						//Copy the apk too
						archivePath = "${buildPath}/${debugBuildId}"
						sh "mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}"
						sh "cp -r ${archivePath} ${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/"

						slackButton = "(${OUTPUT_PATH_DAILY_BUILDS}/${dailyBuildFolder}/${releaseBuildId})"
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

		if(buildLevel >= BUILD_RELEASE_UPLOAD)
		{
			print("Uploading Build!");
			try
			{
				stage(projectFolder + "-Upload") 
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

return this