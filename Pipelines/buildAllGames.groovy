import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.util.Date
import java.text.SimpleDateFormat 

node{
	def DailyBuildCode = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")
	
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

		slackSend( attachments: [
			[
				text: output ,
				color: '#00aa00'
			],
			[
				text: "View on <${env.BUILD_URL}|Jenkins>" ,
				color: '#00aa00'
			])


	print "Using settings: " + file.text

	def dateFormat = new SimpleDateFormat("yyyy-MMdd-HHmm")
   	def date = new Date()
	def dailyBuildFolder = "DailyBuild_" + dateFormat.format(date)

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
						DailyBuildCode.DoGamePlatform( game ,  target ,  false ,  gameTargetResult , dailyBuildFolder)
					}
				}
		}

	def resultsJson = JsonOutput.toJson(buildResults)

	slackSend( attachments:  [
									[
										text: resultsJson ,
										color: '#00aa00'
									]
								] )

	print resultsJson
	//Now write the result.
	writeFile(file:BUILD_RESULTS , text : resultsJson )

	currentBuild.description = resultsJson
	
}
