node{
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	dailyBuild.DoGame("LawnMower" ,"default" , "2019.2.9f1");
	dailyBuild.DoGame("Starfish" , "default" ,"2019.1.14f1");
	//dailyBuild.DoGame("SlingKong" , "default" ,"2019.1.14f1");
	
}
