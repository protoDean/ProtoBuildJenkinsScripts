import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
node{
	def DailyBuildCode = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	def dailyBuildSettings

	
	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	dailyBuildSettings = new JsonSlurperClassic().parseText(file.text);
	
	final String BUILD_RESULTS = "dailyBuildResults.json"
	def buildResults = null

	if(fileExists(BUILD_RESULTS))
	{
		print "exists"

		String txt = readFile(file : BUILD_RESULTS) 
		print "Existing Build Results: \n " + txt

		buildResults = new JsonSlurperClassic().parseText(txt )
	}
	else
	{
		buildResults = [ games: [] ]
	}

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
			],
			[
				text: "View on <${env.BUILD_URL}|Jenkins>" ,
				color: '#00aa00'
			]
		]

		slackSend( attachments: attachments)

		currentBuild.description = output


	print "Using settings: " + file.text

		for (game in dailyBuildSettings.games) 
		{
			print "Doing Game " + game.projectName
			def gameResult = DailyBuildCode.GetGameResults(game , buildResults)

				for (target in game.targets) 
				{	
					if(target.buildLevel > 0)
					{
						def gameTargetResult = DailyBuildCode.GetTargetResults(target.id , gameResult)

						print("Doing Target " + target.id)
						 

						DailyBuildCode.DoGamePlatform(target , game , false , gameTargetResult);
					}
				}
		}

	print JsonOutput.toJson(buildResults)
		//Now write the result.
	writeFile(file:BUILD_RESULTS , text : JsonOutput.toJson(buildResults) )

	//dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	//dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}
