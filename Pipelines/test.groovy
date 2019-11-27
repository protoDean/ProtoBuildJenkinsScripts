
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

//Using Slack
//https://github.com/jenkinsci/slack-plugin/blob/master/README.md
node {
    print currentBuild.getStartTimeInMillis() 
    print currentBuild.number
    
    buildProfile = "iosDebug"
    ouputFolder = "JenkinsTest"
    projectFolder = "JenkinsTest"
    //def finalBuildResult = build job: 'TestItems', parameters: [
    //    [$class: 'StringParameterValue', name: 'projectFolder', value: projectFolder]
    //    ]
        
    //THis lets us get the variables used in this build. Will be handy.
    //def j1EnvVariables = finalBuildResult.getBuildVariables();
    //print "${j1EnvVariables}" 
    //DoGame("Foobie")

	print pwd()
	def imported = load(pwd() + "@script/Pipelines/testImport.groovy")
	imported.example1()

	imported.example2()


	//Slurper
    

	//print json.test
	// env.someJson
	//File file = new File("/Volumes/StoreSafe/Jenkins/BuildSettings/dailyBuilds.json")
	//def dailyBuildSettings = new JsonSlurper().parseText(file.text);

	//print "Test scm"

	//def projectFolder = "JenkinsTest"

	//script{
		//echo "/usr/local/bin/hg pull -R " + env.PROJECT_PATH + "/JenkinsTest"
		//sh "/usr/local/bin/hg pull -R ${env.PROJECT_PATH}/${projectFolder}"
	//}
	
	

	//slackSend(channel: "#builds" , color : "good" , message : "Build Started - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}/console|Open>)")

	//Clear the file
	writeFile(file:"testFile.txt" , text : "")

	sh "echo 'First LIne: ${env.JOB_NAME} (<${env.BUILD_URL}/console|Open>)' >> testFile.txt"

	sh "echo 'Result Line' >> testFile.txt"

	

	def output = readFile(file: "testFile.txt");

	print output

	def attachments = [
		[
			text: output , //'I find your lack of faith disturbing!',
			fallback: 'Hey, Vader seems to be mad at you.',
			color: '#ff0000'
		]
	]
	//slackSend( attachments: attachments)


	blocks = [
	[
		"type": "section",
		"text": [
			"type": "mrkdwn",
			"text": "Hello, Assistant to the Regional Manager Dwight! *Michael Scott* wants to know where you'd like to take the Paper Company investors to dinner tonight.\n\n *Please select a restaurant:*"
		]
	],
    [
		"type": "divider"
	],
	[
		"type": "section",
		"text": [
			"type": "mrkdwn",
			"text": "*Farmhouse Thai Cuisine*\n:star::star::star::star: 1528 reviews\n They do have some vegan options, like the roti and curry, plus they have a ton of salad stuff and noodles can be ordered without meat!! They have something for everyone here"
		],
		"accessory": [
			"type": "image",
			"image_url": "https://s3-media3.fl.yelpcdn.com/bphoto/c7ed05m9lC2EmA3Aruue7A/o.jpg",
			"alt_text": "alt text for image"
			]
		]
	]
	//THis requires a custom bot.
	//slackSend( blocks: blocks)


	//didnt work
	//slackUploadFile(filePath : "testFile.txt" , channel : "#builds")
	//print hgOutput

	//def hgOutput = runShell("/usr/local/bin/hg pull -R ${env.PROJECT_PATH}/JenkinsTest")
	//print hgOutput

	//imported.Student study = new imported.Student();
	//print study.name

	final String BUILD_RESULTS = "dailyBuildResults.json";
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


}

// def DoGame(String gameName) {
        
// 		final PROFILE_IOS_RELEASE = "Outside Define"

// 		stage("Stage 1")
// 		{
//         	echo "It WOrks " + gameName 
// 		}

// 		stage("Stage 2")
// 		{
//         	echo "Yeay" 
// 		}

// 		stage("Stage 2")
// 		{
//         	echo PROFILE_IOS_RELEASE 
// 		}

// }


def runShell(String command){

    def responseCode = sh returnStatus: true, script: "${command} &> tmp.txt" 

    def output =  readFile(file: "tmp.txt")
	
    if (responseCode != 0){
      println "[ERROR] ${output}"
      throw new Exception("${output}")
    }else{
      return "${output}"
    }
}

