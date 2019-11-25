
import groovy.json.JsonSlurper

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
	def imported = load(pwd() + "@script/Pipelines/testImport.groovy")
	imported.example1()

	imported.example2()


	//Slurper
    

	//print json.test
	// env.someJson
	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	def dailyBuildSettings = new JsonSlurper().parseText(file.text);

	env.someJson = "foobar"

	print env.testProperty

	env.testProperty = "foobar"

	print env.testProperty


	print "Test scm"

	sh "/usr/local/bin/hg pull -R ${env.PROJECT_PATH}/JenkinsTest"
	//def hgOutput = runShell("/usr/local/bin/hg pull -R ${env.PROJECT_PATH}/JenkinsTest")

	//print hgOutput

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


// def runShell(String command){

//     def responseCode = sh returnStatus: true, script: "${command} &> tmp.txt" 

//     def output =  readFile(file: "tmp.txt")
	
//     if (responseCode != 0){
//       println "[ERROR] ${output}"
//       throw new Exception("${output}")
//     }else{
//       return "${output}"
//     }
// }