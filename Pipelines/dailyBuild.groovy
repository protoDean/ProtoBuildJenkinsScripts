node{

        def buildProfile = "iosRelease"
        def projectFolder = env.projectFolder
        def sourceBranch = "default"
        
        //Grab the build num from the release build, and make the debug build the same. So we can swap between them.
        def finalBuildNumber
       
        buildProfile = "iosRelease"

        //final DAILY_BUILD_TEMP = "DailyBuildTemp" + currentBuild.getStartTimeInMillis() 
        //def dailyBuildFolder = DAILY_BUILD_TEMP
        //ef buildPath = "../DailyBuilds/"+ DAILY_BUILD_TEMP
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
                [$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile] 
                ], propagate: false, wait: true
                
                
            finalBuildNumber = "" + finalBuildResult.number
            
            //def finalBuildCompletePath = finalBuildResult.getBuildVariables().WORKSPACE + "/" + dailyBuildFolder
            //dailyBuildFolder =  "DailyBuild" + finalBuildNumber
            
            //sh "echo ${dailyBuildFolder}"

        }
        
        buildProfile = "iosDebug"
            
        stage("DebugBuildIos" + projectFolder) {
      
            build job: 'IosDevBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile],
                [$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]
                ], propagate: false, wait: true
        }
        
        //rename folder
        //sh "mv -v " + DAILY_BUILD_TEMP + " " + dailyBuildFolder
        //Upload Release build if neccessary.
        
         buildProfile = "googleDebugApk"
         stage("DevBuildAndroid" + projectFolder) {
    
            def finalBuildResult = build job: 'AndroidDevBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile] 
                ], propagate: false, wait: true
                
                
            finalBuildNumber = "" + finalBuildResult.number
            
            //def finalBuildCompletePath = finalBuildResult.getBuildVariables().WORKSPACE + "/" + dailyBuildFolder
            //dailyBuildFolder =  "DailyBuild" + finalBuildNumber
            
            //sh "echo ${dailyBuildFolder}"

        }
        
        buildProfile = "googleRelease"
            
        stage("ReleaseBuildAndroid" + projectFolder) {
      
            build job: 'AndroidDevBuild', parameters: commonParams + [
                [$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile],
                [$class: 'StringParameterValue', name: 'buildNumOverride', value: finalBuildNumber]
                ], propagate: false, wait: true
        }
        
        //rename folder
        //sh "mv -v " + DAILY_BUILD_TEMP + " " + dailyBuildFolder
        //Upload Release build if neccessary.
    
}
