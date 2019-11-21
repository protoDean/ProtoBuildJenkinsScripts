


node {
    print currentBuild.getStartTimeInMillis() 
    print currentBuild.number
    
    buildProfile = "iosDebug"
    ouputFolder = "JenkinsTest"
    projectFolder = "JenkinsTest"
    //def finalBuildResult = build job: 'TestItems', parameters: [
    //    [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder]
    //    ]
        
    //THis lets us get the variables used in this build. Will be handy.
    //def j1EnvVariables = finalBuildResult.getBuildVariables();
    //print "${j1EnvVariables}" 
    DoGame("Foobie")
    
}

def DoGame(String gameName) {
        
        echo "It WOrks " + gameName 
}