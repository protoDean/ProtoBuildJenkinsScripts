import groovy.json.JsonSlurperClassic

node{
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	def dailyBuildSettings = new JsonSlurperClassic().parseText(file.text);

	for (game in dailyBuildSettings.games) 
	{
		for (target in game.targets) 
		{	
		   dailyBuild.DoGamePlatform(game.projectName , game.sourceBranch , game.unityVersion , game.target.id , game.target.buildLevel);
		}
	}

	//dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	//dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}
