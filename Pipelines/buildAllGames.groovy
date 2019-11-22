import groovy.json.JsonSlurperClassic

node{
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	def dailyBuildSettings = new JsonSlurperClassic().parseText(file.text);

	print "Using settings: " + file.text

	
		for (game in dailyBuildSettings.games) 
		{
			print "Doing Game " + game.projectName
			
				for (target in game.targets) 
				{	
					print "Doing Target " + target.id
					dailyBuild.DoGamePlatform(game.projectName , game.sourceBranch , game.unityVersion , target.id , target.buildLevel);

					if(target.id == "Android")
					{
						//Get the build id. Copy it.
						//mkdir -p ${OUTPUT_PATH}/${outputFolder}
						//mv ${WORKSPACE}/${buildPath}/${unityBuildId} ${OUTPUT_PATH}/${outputFolder}/${unityBuildId}
					}
				}
			
		}
	

	//dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	//dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}
