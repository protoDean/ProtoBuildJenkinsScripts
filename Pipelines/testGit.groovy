
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

//Using Slack
//https://github.com/jenkinsci/slack-plugin/blob/master/README.md
node {
    print currentBuild.getStartTimeInMillis() 
    print currentBuild.number
    
   	dir(path: "${env.PROJECT_PATH}/${projectFolder}")
	{

		//git(url:"https://github.com/protoDean/ShoutyHeads", branch: "master" , credentialsId:"JenkinsGithubLogin")
		
		withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsGithubLogin',
			usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
		{

		 	sh 'echo uname=$credUser pwd=$credPassword'

		 	sh "/usr/bin/git fetch --all https://${credUser}:${credPassword}@github.com/protoDean/ShoutyHeads"
		 	sh "/usr/bin/git checkout -f master"
		 	
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

