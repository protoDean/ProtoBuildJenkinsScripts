import groovy.json.JsonSlurper

node{
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	def dailyBuildSettings = new JsonSlurper().parseText(file.text);

	for (game in dailyBuildSettings.games) {
   		dailyBuild.DoGame(game.projectName , game.sourceBranch , game.unityVersion , game.targets);
	}

	//dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	//dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}
