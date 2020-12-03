	//Update Source
	// https://github.com/protoDean/${projectFolder}.git
	//sh "cd ${env.PROJECT_PATH}/${projectFolder} && /usr/bin/git fetch"
	//sh "cd ${env.PROJECT_PATH}/${projectFolder} && /usr/bin/git checkout -f ${sourceBranch}"
	//sh "/usr/local/bin/hg update " + sourceBranch + " -R ${PROJECT_PATH}/${projectFolder} -C"
	// Get the changeset git describe --abbrev=12 --always
	// git rev-parse HEAD  :Gets the hash of the HEAD, where we are.
	// git rev-parse master : Gets the hash of the branch?

	//Check against existing builds.

		//Maybe should clone if not existing 
	// dir(path: "${env.PROJECT_PATH}")
	// {

	// 	//git(url:"https://github.com/protoDean/${projectFolder}", branch: "${sourceBranch}" , credentialsId:"JenkinsGithubLogin")
		
	// 	withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JenkinsLoginPassword',
	// 		usernameVariable: 'credUser', passwordVariable: 'credPassword']]) 
	// 	{

	// 		echo runShell("security -v unlock-keychain -p ${credPassword} login.keychain")
	// 		echo runShell("git clone --recurse-submodules --remote-submodules https://${JENKINS_GITHUB_USER}@github.com/protoDean/${projectFolder} ${projectFolder}")
	// 	}
	// }

				//echo "GitResult is " + gitResult
				//echo "GitResult is " + gitResult

			// if(gitResult.indexOf("fatal") >= 0)
			// {
			// 	//Error with git
			// 	buildDescription += "Error with Git \n" + gitResult
			// 	currentBuild.description = buildDescription
				// if(gitResult.indexOf("fatal") >= 0)
				// {
				// 	//Error with git
				// 	buildDescription += "Error with Git \n" + gitResult
				// 	currentBuild.description = buildDescription

			// 	error("Error with Git " + gitResult )
			// }	
				// 	error("Error with Git " + gitResult )
				// }	