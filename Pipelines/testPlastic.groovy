
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

//Using Slack
//https://github.com/jenkinsci/slack-plugin/blob/master/README.md
node {
    print currentBuild.getStartTimeInMillis() 
    print currentBuild.number
    
	final projectPath = env.PROJECT_PATH_PLASTIC

	def projectFolder = env.projectFolder
	def sourceBranch = env.sourceBranch

   	dir(path: "${projectPath}/${projectFolder}")
	{
	
		//git(url:"https://github.com/protoDean/${projectFolder}", branch: "${sourceBranch}" , credentialsId:"JenkinsGithubLogin")
		
		withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsGithubLogin',
			usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
		{

			//sh "/usr/bin/git lfs install"
			//sh "/usr/bin/git fetch --tags --force https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"
			//sh "/usr/bin/git checkout -f -b ${sourceBranch} origin/${sourceBranch}"
			//sh "/usr/bin/git pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"
			//sh "/usr/bin/git reset --hard origin/${sourceBranch}" 
			//sh "/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"

//Still doesnt update properly
			//sh "/usr/bin/git fetch --tags --force https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
			//	"/usr/bin/git reset --hard origin/${sourceBranch} &&" +
			//	"/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
			//	"/usr/bin/git clean -d -f"		//Cleans any unknown files (not ignored ones. use -x to clean ignored files too.)

			//switch branch
			echo runShell("cm switch ${sourceBranch}");
			echo runShell("cm update");

			//undo local changes
			echo runShell("cm undo -r")

			echo runShell("cm status --cset")
		 	
		 }
		
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

