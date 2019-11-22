def DoGamePlatform(String projectFolder , String sourceBranch ,  String paramUnityVersion , String target , Int buildLevel ) {
        
   	final PROFILE_IOS_RELEASE = "iosRelease"
	final PROFILE_IOS_DEBUG = "iosDebug"
	final PROFILE_ANDROID_DEBUG = "googleDebugApk"
	final PROFILE_ANDROID_RELEASE = "googleRelease"

	final TARGET_ANDROID = "Android"
	final TARGET_IOS = "iOS"

	enum BuildLevel{
 		NONE, DEBUG , RELEASE , RELEASE_UPLOAD
	}

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
        
		if(buildLevel >= BuildLevel.RELEASE)
		{
			try
			{
				buildProfile = PROFILE_IOS_RELEASE
				stage(buildProfile + projectFolder) {
					
					def finalBuildResult = build job: 'UnityBuild', parameters: commonParams + [
						[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile ] 
						
						], propagate: true, wait: true
						
						
					finalBuildNumber = "" + finalBuildResult.number
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  
			}
		}

		if(buildLevel >= BuildLevel.DEBUG)
		{	
			try
			{
				buildProfile = PROFILE_IOS_DEBUG
				stage(buildProfile + projectFolder) {
			
					var Params = commonParams + [
						[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile]]
						

					//override build num if we did a release
					if(finalBuildNumber)
					{
						params = params + 
							[[$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]]
					}

					build job: 'UnityBuild',  parameters: params, propagate: true, wait: true
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  
			}
		}

		if(buildLevel >= BuildLevel.RELEASE_UPLOAD)
		{
			print("Uploading Build!");
		}

        //rename folder
        //sh "mv -v " + DAILY_BUILD_TEMP + " " + dailyBuildFolder
        //Upload Release build if neccessary.
}

return this