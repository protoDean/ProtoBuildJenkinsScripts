import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.util.Date
import java.text.SimpleDateFormat 

node{

	def DailyBuildCode = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	

	def dailyBuildSettings

	def buildSettings = env.buildSettings;
	dailyBuildSettings = new JsonSlurperClassic().parseText(buildSettings)


	final String BUILD_RESULTS = "dailyBuildResults.json"
	def buildResults = null

	if(fileExists(BUILD_RESULTS))
	{
		String txt = readFile(file : BUILD_RESULTS) 
		print "Existing Build Results: \n " + txt

		if(txt != null)
		{
			buildResults = new JsonSlurperClassic().parseText(txt )
		}
		else
		{
			buildResults = [ games: [] ]
		}
	}
	else
	{
		buildResults = [ games: [] ]
	}

	def dateFormat = new SimpleDateFormat("yyyy-MMdd-HHmm")
   	def date = new Date()
	def dailyBuildFolder = "DailyBuild_" + dateFormat.format(date)

	for (game in dailyBuildSettings.games) 
	{
		def gameResult = DailyBuildCode.GetGameResults(game , buildResults)
		DailyBuildCode.DoGamePlatform( game ,  true ,  gameResult , dailyBuildFolder)
	}

	//Format results better.
	//def resultsJson = JsonOutput.toJson(buildResults)

	//print resultsJson
	//Now write the result.
	//writeFile(file:BUILD_RESULTS , text : resultsJson )

	//currentBuild.description = resultsJson

}
