import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.util.Date
import java.text.SimpleDateFormat 
node{

	def sourceProject = env.sourceProject
	def projectFolder = env.projectFolder

		//Update Source
	// https://github.com/protoDean/${projectFolder}.git
	sh "/usr/bin/git clone https://github.com/protoDean/${sourceProject}.git ${env.PROJECT_PATH}/${projectFolder}"
	
	


		//git(url:"https://github.com/protoDean/${projectFolder}", branch: "${sourceBranch}" , credentialsId:"JenkinsGithubLogin")
		
		withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsGithubLogin',
		usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
		{
			//sh "/usr/bin/git lfs install"
		
			//sh "/usr/bin/git checkout -f ${sourceBranch}"
			//sh "/usr/bin/git lfs pull https://${credUser}:${credPassword}@github.com/protoDean/${projectFolder}"

			sh "/usr/bin/git clone https://${credUser}:${credPassword}@github.com/protoDean/${sourceProject}.git ${env.PROJECT_PATH}/${projectFolder}"

			//Set it back to the non passwork version.
			sh "/usr/bin/git remote --set-url origin https://github.com/protoDean/${sourceProject}" 
		}
		
		currentBuild.description = "Cloned https://github.com/protoDean/${sourceProject}.git to ${projectFolder}"
	

}
