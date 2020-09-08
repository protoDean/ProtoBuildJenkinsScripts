import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.util.Date
import java.text.SimpleDateFormat 
node{

	def sourceProject = env.sourceProject
	def projectFolder = env.projectFolder

		//Update Source
	// https://github.com/protoDean/${projectFolder}.git
	//sh "/usr/bin/git clone https://github.com/protoDean/${sourceProject}.git ${env.PROJECT_PATH}/${projectFolder}"
	
	
	dir(path: "${env.PROJECT_PATH}")
	{

		//git(url:"https://github.com/protoDean/${projectFolder}", branch: "${sourceBranch}" , credentialsId:"JenkinsGithubLogin")
		
		//withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsGithubLogin',
		//usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
		//{
			//sh "/usr/bin/git lfs install"
		
			//sh "/usr/bin/git checkout -f ${sourceBranch}"
			//sh "/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"

			//sh "/usr/bin/git remote --set-url origin https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
			//	"/usr/bin/git fetch --tags --force origin &&" +
			//	"/usr/bin/git checkout -f -B ${sourceBranch} origin/${sourceBranch} &&" +
			//	"/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder} &&" +
			//	"/usr/bin/git clean -d -f"		//Cleans any unknown files (not ignored ones. use -x to clean ignored files too.)


			//Set it back to the non passwork version.
			//sh "/usr/bin/git remote --set-url origin https://github.com/protoDean/${projectFolder}" 

			sh "git clone --recurse-submodules --remote-submodules https://protostarBuildMachine@github.com/protoDean/${sourceProject} ${sourceProject}"
				

			echo "Most recent commit \n"
			echo runShell("/usr/bin/git log -1 --oneline")
			
		//}
	}

	dir(path: "${env.PROJECT_PATH}/${sourceProject}")
	{
		sh "/usr/bin/git lfs install"
	}
		// //git(url:"https://github.com/protoDean/${projectFolder}", branch: "${sourceBranch}" , credentialsId:"JenkinsGithubLogin")
		
		// withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsGithubLogin',
		// usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
		// {
		// 	//sh "/usr/bin/git lfs install"
		
		// 	//sh "/usr/bin/git checkout -f ${sourceBranch}"
		// 	//sh "/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"
		// 	sh "/usr/bin/git init ${env.PROJECT_PATH}/${projectFolder}"

		// 	dir(path: "${env.PROJECT_PATH}/${projectFolder}")
		// 	{
		// 		sh "/usr/bin/git lfs install"
		// 		sh "/usr/bin/git fetch --tags --force https://${credUser}:${credPassword}@github.com/protoDean/${sourceProject} +refs/heads/*:refs/remotes/origin/* &&" +
		// 			"/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${sourceProject}"
		// 	}
		// }
		
		currentBuild.description = "Cloned https://github.com/protoDean/${sourceProject}.git to ${sourceProject}"
	

}

def runShell(String command){

    def responseCode = sh returnStatus: true, script: "${command} &> tmp.txt" 

    def output =  readFile(file: "tmp.txt")
	
   // if (responseCode != 0){
   //   println "[ERROR] ${output}"
   //   throw new Exception("${output}")
   // }else{
      return "${output}"
    //}
}