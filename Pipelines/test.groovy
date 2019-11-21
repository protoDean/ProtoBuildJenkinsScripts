

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

	print pwd()
	def imported = load("testImport.groovy")
	imported.example1()

	imported.example2()
    
}

def DoGame(String gameName) {
        
		final PROFILE_IOS_RELEASE = "Outside Define"

		stage("Stage 1")
		{
        	echo "It WOrks " + gameName 
		}

		stage("Stage 2")
		{
        	echo "Yeay" 
		}

		stage("Stage 2")
		{
        	echo PROFILE_IOS_RELEASE 
		}

}