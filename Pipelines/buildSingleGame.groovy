import groovy.json.JsonSlurperClassic

node{

	//DEPRECATED
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	def dailyBuildSettings
	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	dailyBuildSettings = new JsonSlurperClassic().parseText(file.text);

	int buildLevel = -1;

	switch(env.buildLevel) {
		case "":
		case null:
			print "No build level supplied, Using build setting"
		break
		case "debug": buildLevel = 1
		break
		case "release": buildLevel = 2
		break
		case "upload": buildLevel = 3
	}

		for (game in dailyBuildSettings.games) 
		{
			if(game.projectName == env.projectFolder)
			{
				for (target in game.targets) 
				{	
					if(target.id == env.target)
					{

						//No new changes
						def attachments = [
										[
											text: "Doing a single build of ${projectFolder} (${game.sourceBranch}} on ${env.target}" ,
											color: '#00aa00'
										]
									]

						slackSend( attachments: attachments )

						
						print "Doing " + game.projectName + " Target " + target.id
						dailyBuild.DoGamePlatform(game.projectName , game.sourceBranch , game.unityVersion , target.id , buildLevel >= 0 ? buildLevel : target.buildLevel , true);
					}
				}
			}
		}

}
