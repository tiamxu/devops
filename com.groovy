import groovy.json.JsonSlurper 
@NonCPS
def jsonParse(def json) {
    new groovy.json.JsonSlurper.parseText(json)
}
//mJj4NEqIWkZv7+5Ev29Ra3HuiXGImlyn1AYzqOAXWsk=
def token = 'glpat-57WwyFMeYAsszMzyzjsn'
def item=[]
def call(String ii, g) {
    currentBuild.displayName = "[feature_k8s] $ii"
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
        withCredentials([string(credentialsId: "token1" , variable: 'token')]) { token="$token" }
        def response = httpRequest url: "http://172.168.1.10:8082/api/v4/projects/$item.code_id", customHeaders: [[name: 'Authorization',value: "Bearer $token"]]
        def gitlab = jsonParse(response.content)
        if (item.codeId != gitlab.id) {
	        currentBuild.result = "NOT_BUILT" 
	        return
	    }
	    item['url']=gitlab['http_url_to_repo']
        dir(item.app_name){    
          git branch: 'master' , credentialsId: 'deploykey', url: item.url
          commitMsg=sh(script: 'git log -1 --pretty=format:"%ad %h for %an %s" --date=format:" %H:%M:%S/%m-%d"', returnStdout: true).trim()
          currentBuild.description += "\n[$item.app_name] " + commitMsg
          doBuild()
          currentBuild.description += " $currentBuild.currentResult"
        }
    }
}
//curl --header "PRIVATE-TOKEN:glpat-uwENzHNk3FGZArNyz9hM" "http://172.168.1.10:8082/api/v4/projects/2"
//gitlab token:glpat-57WwyFMeYAsszMzyzjsn
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
def buildMvn() {

}
def buildGradle(){

}
def buildGo() {
    // sh 'go mod download'
    sh 'go test'
    sh 'go build -o bin/uniserver-linux'
    
}
def dockerfile() {

}
def buildImage() {
    imageName="harbor.cy-sys.cn/$item.appGroup-dev/$item.appName"
    docker.withRegistry("https://harbor.cy-sys.cn","Dockerfile-dev") {
      image=docker.build("$imageName")
      image.push()
      //clean local image
      sh "docker rmi $imageName"
    }
    // restartPod()
}
return this;