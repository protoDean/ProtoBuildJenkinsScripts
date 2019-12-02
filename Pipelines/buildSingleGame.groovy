import groovy.json.JsonSlurperClassic

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
				def gameResult = DailyBuildCode.GetGameResults(game , buildResults)

				for (target in game.targets) 
				{	
					if(target.id == env.target)
					{
						def gameTargetResult = DailyBuildCode.GetTargetResults(target.id , gameResult)
						
						//No new changes
						def attachments = [
										[
											text: "Doing a single build of ${projectFolder} (${game.sourceBranch}} on ${env.target}" ,
											color: '#00aa00'
										]
									]

						slackSend( attachments: attachments )

						if(buildLevel >= 0)
						{
							target.buildLevel = buildLevel
						}
					
						print "Doing " + game.projectName + " Target " + target.id

						DailyBuildCode.DoGamePlatform( game ,  target ,  false ,  gameTargetResult)
					}
				}
			}
		}

	print JsonOutput.toJson(buildResults)
	//Now write the result.
	writeFile(file:BUILD_RESULTS , text : JsonOutput.toJson(buildResults) )

}
