def DoGamePlatform(String projectFolder , String sourceBranch ,  String paramUnityVersion , String target , int buildLevel) {
        
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


	

	def wereFailures = false;
       
        //Grab the build num from the release build, and make the debug build the same. So we can swap between them.
        def finalBuildNumber = null
       
		def buildPath = "../DailyBuilds/DailyBuild" + currentBuild.number
		def outputFolder =  "Daily/${projectFolder}Daily" +  currentBuild.number
        
        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder],
            [$class: 'StringParameterValue', name: 'outputFolder', value: outputFolder] ,
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: paramUnityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath],
			[$class: 'StringParameterValue', name: 'buildTarget', value: target] 
            ]
		
		def buildProfile 
		def releaseBuildId = null
        
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
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  
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

					build job: 'UnityBuild',  parameters: buildParams, propagate: true, wait: true
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  
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

return this