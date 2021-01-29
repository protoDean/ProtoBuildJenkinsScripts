
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

//Using Slack
//https://github.com/jenkinsci/slack-plugin/blob/master/README.md
node {
    print currentBuild.getStartTimeInMillis() 
    print currentBuild.number

	def targetFile = env.targetFile
    
   	dir(path: "${env.PROJECT_PATH}/TestFileOps")
	{
		def projectFolder = env.projectFolder
		def sourceBranch = env.sourceBranch

			archivePath = "${buildPath}/${buildId}"
			echo runShell("mkdir -p ${OUTPUT_PATH_DAILY_BUILDS}/TestFileOps")
			echo runShell("cp -r ${targetFile} ${OUTPUT_PATH_DAILY_BUILDS}/TestFileOps/")
	
	}

}

// def DoGame(String gameName) {
        
// 		final PROFILE_IOS_RELEASE = "Outside Define"

// 		stage("Stage 1")
// 		{
//         	echo "It WOrks " + gameName 
// 		}

// 		stage("Stage 2")
// 		{
//         	echo "Yeay" 
// 		}

// 		stage("Stage 2")
// 		{
//         	echo PROFILE_IOS_RELEASE 
// 		}

// }


def runShell(String command){

    def responseCode = sh returnStatus: true, script: "${command} &> tmp.txt" 

    def output =  readFile(file: "tmp.txt")
	
    if (responseCode != 0){
      println "[ERROR] ${output}"
      throw new Exception("${output}")
    }else{
      return "${output}"
    }
}

