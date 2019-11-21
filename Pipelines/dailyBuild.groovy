node{
	def sourceBranch = "default"
	DoGame(env.projectFolder , sourceBranch);
}

def DoGame(String projectFolder , String sourceBranch) {
        
   	final PROFILE_IOS_RELEASE = "iosRelease"
	final PROFILE_IOS_DEBUG = "iosDebug"
	final PROFILE_ANDROID_DEBUG = "googleDebugApk"
	final PROFILE_ANDROID_RELEASE = "googleRelease"

       
        //Grab the build num from the release build, and make the debug build the same. So we can swap between them.
        def finalBuildNumber
       
		def buildPath = "../DailyBuilds/DailyBuild" + currentBuild.number
		def outputFolder =  "Daily/${projectFolder}Daily" +  currentBuild.number
        
        def commonParams = [
            [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder],
            [$class: 'StringParameterValue', name: 'outputFolder', value: outputFolder] ,
            [$class: 'StringParameterValue', name: 'sourceBranch', value: sourceBranch],
            [$class: 'StringParameterValue', name: 'unityVersion', value: unityVersion],
            [$class: 'StringParameterValue', name: 'buildPath', value: buildPath]
            ]
        
        stage("ReleaseBuildIos" + projectFolder) {
    
            def finalBuildResult = build job: 'IosReleaseBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: PROFILE_IOS_RELEASE] 
                ], propagate: false, wait: true
                
                
            finalBuildNumber = "" + finalBuildResult.number
        }
        
  
        stage("DebugBuildIos" + projectFolder) {
      
            build job: 'IosDevBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: PROFILE_IOS_DEBUG],
                [$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]
                ], propagate: false, wait: true
        }
    
         stage("DevBuildAndroid" + projectFolder) {
    
            def finalBuildResult = build job: 'AndroidDevBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: PROFILE_ANDROID_DEBUG] 
                ], propagate: false, wait: true
                
                
            finalBuildNumber = "" + finalBuildResult.number
        }
        
        stage("ReleaseBuildAndroid" + projectFolder) {
      
            build job: 'AndroidDevBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: PROFILE_ANDROID_RELEASE],
                [$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]
                ], propagate: false, wait: true
        }
        
        //rename folder
        //sh "mv -v " + DAILY_BUILD_TEMP + " " + dailyBuildFolder
        //Upload Release build if neccessary.
}