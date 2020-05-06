import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.util.Date
import java.text.SimpleDateFormat 
node{

	def DailyBuildCode = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	def dailyBuildSettings
	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	dailyBuildSettings = new JsonSlurperClassic().parseText(file.text);

	final String BUILD_RESULTS = "dailyBuildResults.json"
	def buildResults = null

	if(fileExists(BUILD_RESULTS))
	{
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

	def dateFormat = new SimpleDateFormat("yyyy-MMdd-HHmm")
   	def date = new Date()
	def dailyBuildFolder = "DailyBuild_" + dateFormat.format(date)


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

					if(buildLevel >= 0)
					{
						target.buildLevel = buildLevel
					}
				
					print "Doing " + game.projectName + " Target " + target.id

					DailyBuildCode.DoGamePlatform( game ,  target ,  true ,  gameTargetResult , dailyBuildFolder)
				}
			}
		}
	}

	def resultsJson = JsonOutput.toJson(buildResults)

	print resultsJson
	//Now write the result.
	writeFile(file:BUILD_RESULTS , text : resultsJson )

	currentBuild.description = resultsJson

}
