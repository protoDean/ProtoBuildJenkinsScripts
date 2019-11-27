import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
node{
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	def dailyBuildSettings

	JsonSlurperClassic slurper = new JsonSlurperClassic() 
	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	dailyBuildSettings = slurper.parseText(file.text);
	
	final String BUILD_RESULTS = "dailyBuildResults.json";
	def buildResults = fileExists(BUILD_RESULTS) ? readFile(BUILD_RESULTS) : slurper.parseText("{}") 
	
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
			def gameResult = GetGameResults(game)
			if(gameResult == null)
			{
				gameResult = game
			}
			
				for (target in game.targets) 
				{	
					if(target.buildLevel > 0)
					{
						def gameTargetResult = GetTarget(target.id , gameResult)

						print "Doing Target " + target.id
						print 

						//dailyBuild.DoGamePlatform(target , game , false , gameTargetResult);
					}
				}
		}

	print JsonOutput.toJson(buildResults)
		//Now write the result.
	writeFile(file:BUILD_RESULTS , JsonOutput.toJson(buildResults) )

	//dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	//dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}

def GetGameResults( gameToGet ,  results)
{
	for (game in results.games) 
	{
		String projectFolder = game.projectName 
		String sourceBranch = game.sourceBranch  
		String paramUnityVersion = game.unityVersion

		if(game.projectName == gameToGet.projectName &&
			game.paramUnityVersion == gameToGet.paramUnityVersion)
			{
				return game;
			}

	}

	//add new
	results.add([
		projectName : gameToGet.projectName ,
		unityVersion :  gameToGet.unityVersion
		targets : []
	])

	return null
}

def GetTarget( String targetId ,  gameResults)
{
	for (target in gameResults.targets) 
	{
	
		if(target.id == targetId)
			{
				return target;
			}

	}

	//add new
	gameResults.targets.add([
		id : targetId,
		buildLevel : 0,
		changeSet : null
	])

	return null
}