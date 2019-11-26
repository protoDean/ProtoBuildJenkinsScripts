import groovy.json.JsonSlurperClassic

node{
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	def dailyBuildSettings

	
	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	dailyBuildSettings = new JsonSlurperClassic().parseText(file.text);
	

	
		def output = "It's time to build! Today we are doing... \n\n"

		for (game in dailyBuildSettings.games) 
		{
			output += game.projectName + " on branch " + game.sourceBranch + " ["
				
			for (target in game.targets) 
			{	
				output += target.id + " level " + target.buildLevel + ", "
			}

			output += "]\n"
		}

		def attachments = [
			[
				text: output ,
				color: '#00aa00'
			]
		]
		slackSend( attachments: attachments)
	

	print "Using settings: " + file.text

		for (game in dailyBuildSettings.games) 
		{
			print "Doing Game " + game.projectName
			
				for (target in game.targets) 
				{	
					print "Doing Target " + target.id
					//dailyBuild.DoGamePlatform(game.projectName , game.sourceBranch , game.unityVersion , target.id , target.buildLevel);
				}

		}
	

	//dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	//dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}

