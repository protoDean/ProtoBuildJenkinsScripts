node{

	//DEPRECATED
	def dailyBuild = load(pwd() + "@script/Pipelines/Common/buildGame.groovy")

	dailyBuild.DoGame(env.projectFolder , env.sourceBranch , env.unityVersion);
}
