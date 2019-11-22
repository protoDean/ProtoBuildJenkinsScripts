node{
	


	try
	{
			def buildPath = "../BespokeBuilds/Build" + currentBuild.number
				
				 def commonParams = [
				[$class: 'StringParameterValue', name: 'projectFolder', value: env.projectFolder],
				[$class: 'StringParameterValue', name: 'sourceBranch', value: env.sourceBranch],
				[$class: 'StringParameterValue', name: 'unityVersion', value: env.unityVersion],
				[$class: 'StringParameterValue', name: 'buildPath', value: buildPath],
				[$class: 'StringParameterValue', name: 'buildTarget', value: env.buildTarget] ,
				[$class: 'StringParameterValue', name: 'buildProfile', value: buildProfile ] 
				]

				stage( env.projectFolder + "-" + env.buildProfile) {
					
					def finalBuildResult = build job: 'UnityBuild', parameters: commonParams , propagate: true, wait: true
						
						
					finalBuildNumber = "" + finalBuildResult.number

					def envVariables = finalBuildResult.getBuildVariables();
    				//print "${j1EnvVariables}" 

					releaseBuildId = envVariables.unityBuildId;
				}
			}
			catch(e) {
				wereFailures = true
				echo e.toString()  
			}
	
	//Now Copy to output
	//env.outputFolder
  
}


