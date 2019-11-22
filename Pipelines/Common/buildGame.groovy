def DoGame(String projectFolder , String sourceBranch ,  String paramUnityVersion) {
        
   	final PROFILE_IOS_RELEASE = "iosRelease"
	final PROFILE_IOS_DEBUG = "iosDebug"
	final PROFILE_ANDROID_DEBUG = "googleDebugApk"
	final PROFILE_ANDROID_RELEASE = "googleRelease"

	final TARGET_ANDROID = "Android"
	final TARGET_IOS = "iOS"

	def wereFailures = false;
       
        //Grab the build num from the release build, and make the debug build the same. So we can swap between them.
        def finalBuildNumber
       
		def buildPath = "../DailyBuilds/DailyBuild" + currentBuild.number
		def outputFolder =  "Daily/${projectFolder}Daily" +  currentBuild.number
        
        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder],
            [$class: 'StringParameterValue', name: 'outputFolder', value: outputFolder] ,
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: paramUnityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath]
            ]
		
		def buildProfile 
        
		try
		{
			buildProfile = PROFILE_IOS_RELEASE
			stage(buildProfile + projectFolder) {
				
				catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
						sh "exit 1"
					}
				def finalBuildResult = build job: 'UnityBuild', parameters: commonParams + [
					[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile ] ,
					[$class: 'StringParameterValue', name: 'buildTarget', value: TARGET_IOS] 
					], propagate: true, wait: true
					
					
				finalBuildNumber = "" + finalBuildResult.number
			}
		}
		catch(e) {
        	wereFailures = true
        	echo e.toString()  
    	}
        
		try
		{
			buildProfile = PROFILE_IOS_DEBUG
			stage(buildProfile + projectFolder) {
		
				build job: 'IosDevBuild', parameters: commonParams + [
					[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile],
					[$class: 'StringParameterValue', name: 'buildTarget', value: TARGET_IOS] ,
					[$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]
					], propagate: true, wait: true
			}
		}
		catch(e) {
        	wereFailures = true
        	echo e.toString()  
    	}
    
		try{
			buildProfile = PROFILE_ANDROID_DEBUG
			stage(buildProfile + projectFolder) {
		
				def finalBuildResult = build job: 'AndroidDevBuild', parameters: commonParams + [
					[$class: 'StringParameterValue', name: 'buildTarget', value: TARGET_ANDROID] ,
					[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile] 
					], propagate: true, wait: true
					
					
				finalBuildNumber = "" + finalBuildResult.number
			}
		}
		catch(e) {
        	wereFailures = true
        	echo e.toString()  
    	}
        
		try{
			buildProfile = PROFILE_ANDROID_DEBUG
			stage(buildProfile + projectFolder) {
		
				build job: 'AndroidDevBuild', parameters: commonParams + [
					[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile],
					[$class: 'StringParameterValue', name: 'buildTarget', value: TARGET_ANDROID] ,
					[$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]
					], propagate: true, wait: true
			}
		}
		catch(e) {
        	wereFailures = true
        	echo e.toString()  
    	}
        
        //rename folder
        //sh "mv -v " + DAILY_BUILD_TEMP + " " + dailyBuildFolder
        //Upload Release build if neccessary.
}

return this