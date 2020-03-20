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
	
	currentBuild.description = "Cloned https://github.com/protoDean/${sourceProject}.git to ${projectFolder}"

}
