import groovy.json.JsonSlurperClassic
@NonCPS
def jsonParse(def json) {
    new groovy.json.JsonSlurperClassic().parseText(json)
}
def item=[]
def call(String ii, g) {
    //currentBuild.displayName = "[feature_k8s] $ii"
    currentBuild.description = " "
    for ( String i : ii.split(' ')){
        if(!i) {
          currentBuild.result = "NOT_BUILT"
          return
	    }
        getDatabaseConnection(type:"GLOBAL") {
          item = sql(sql:"select * from item where app_name = '$i' and app_group = '$g'")[0]
          println(item)
        }
	    if(!item) {
          currentBuild.result = "NOT_BUILT"
          return
        }
        //withCredentials([string(credentialsId: "token1" , variable: 'token')]) { token="$token" }
        def response = httpRequest url: "http://172.168.1.10/api/v4/projects/$item.code_id", customHeaders: [[name: 'Authorization',value: "Bearer glpat-57WwyFMeYAsszMzyzjsn"]]
        def gitlab = jsonParse(response.content)
        if (item.code_id != gitlab.id) {
	        currentBuild.result = "NOT_BUILT"
	        return
	    }
	    item['url']=gitlab['http_url_to_repo']
        dir(item.app_name){
          git branch: 'main', credentialsId: 'gitlab', url: item.url
          commitMsg=sh(script: 'git log -1 --pretty=format:"%ad %h for %an %s" --date=format:" %H:%M:%S/%m-%d"', returnStdout: true).trim()
          currentBuild.description += "\n[$item.app_name] " + commitMsg
          doBuild()
          currentBuild.description += " $currentBuild.currentResult"
        }
    }
}

def doBuild() {
    switch(item.app_type) {
        case "go":
            buildGo()
        // case "java":
        //     buildMvn()
        // case "gradle"
        // case "nodejs":
        //     buildNodejs()
        default:
            println("ERROR: not found build type")
            currentBuild.result = "NOT_BUILD"
            return
    }
}

def buildGo() {
      //docker.image('golang:1.17').inside {
      withDockerContainer('golang:1.17') {
      sh 'go version'
      sh 'ls /var/jenkins_home/workspace/hello/hello'
      sh 'go mod download'
      sh 'go test'
      sh 'go build -o bin/uniserver-linux'
    }
    buildImage()

}

def buildImage() {
    registry = "https://registry.cn-hangzhou.aliyuncs.com"
    imageName="registry.cn-hangzhou.aliyuncs.com/unipal/test_hello"
    docker.withRegistry(registry,'registry-vpc') {
      image=docker.build("$imageName")
      image.push()
      sh "docker rmi $imageName"
    }
    // restartPod()
}


return this;