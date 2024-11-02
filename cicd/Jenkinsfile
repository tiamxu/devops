String deploy
String project
String dockerf
imageMap = [:]
// main call
// do prefix is public exec stage
def call(String team, String deployname, String scheme, String dockertype) {
    stage("Initialization")
    if (!team || !deployname || !scheme) {
      echo "[ERROR] argument cannot be null"
      currentBuild.result = 'FAILURE'
      return
    }

      deploy = deployname
      project = team
      sh "echo 1.currect dir: `pwd`"
      echo "team: ${team}, deploy: ${deploy} , deployment: ${deployname}, scheme: ${scheme}, dockertype: ${dockertype}, project: ${project}"
      sh "rm -rf `pwd`/*"
      sh "rm -rf `pwd`/.git"
      sh "rm -rf `pwd`/.gitignore"
      sh "ls -al `pwd`/"
      
      //deleteDir()

      if (params.XBUILD_REPO.split(":")[0] == 'http') {
            git credentialsId: params.XBUILD_OWNER , branch: params.XBUILD_BRANCHE , url: params.XBUILD_REPO
      }
     // git credentialsId: params.XBUILD_OWNER , branch: params.XBUILD_BRANCHE , url: params.XBUILD_REPO
     doScheme = "[${scheme}]"
      echo "${doScheme}"
     pjpath="$deploy"
     this.changeDir()
     if (fileExists(pjpath)) {
      sh "echo 2.currect dir: `pwd`"
         dir (pjpath) {
             sh "echo 3.currect dir: `pwd`"
             doScheme[1..doScheme.length()-2].tokenize(',').each{this."$it"()}
         }
      } else {
             sh "echo 4.currect dir: `pwd`"
         doScheme[1..doScheme.length()-2].tokenize(',').each{this."$it"()}    
      } 

}

def schemeDpBuildTagImage(){
    println('TEST BUILD')
    this.printMes("TEST BUILD",'green1')
    imageMap."${params.XBUILD_BRANCHE}" = ['fullpath': "https://" + params.XBUILD_REPO ]
    println imageMap.dev.fullpath
    this.buildTag(params.XBUILD_BRANCHE,params.XBUILD_DOCKERTYPE,'',params.XBUILD_DOCKERTYPE)

}

def scheme(){
    echo "Code type determination"
    this.codeType('dev')
}
def schemeBuildToTest(){
    this.codeType('test')
}

def schememtestcurl() {

  dockerf = 'ha'
  sh "curl -o Dockerfile ${HTTP_FILE_URL}/Dockerfile-${dockerf}"

  if ( sh (returnStdout: true, script: 'grep "404 page not found" Dockerfile  ||echo ""').trim()) {

    sh "curl -o Dockerfile ${HTTP_FILE_URL}/Dockerfile-java.v1"

  }
  sh "cat Dockerfile"    

}


def schemeweb() {
    //this.doSQA()
    //this.doBuild()
    sh 'ls -l'
    dockerf = 'web.v1-' + deploy
    this.buildImageWeb('dev','','dev')
    //this.doAnsible()
   
}

def codeType(env){

  stage("Code type detected")
  echo "language type detected: java(pom.xml) web(dist) node(index.js)"
  checkok=false
  if ( sh (returnStdout: true, script: 'ls pom.xml||echo ""').trim()){
      checkok=true
      echo "detected type: java"

      this.doBuild()
      sh 'ls -l'
      dockerf = 'java.v1'
      this.buildImage(env,'',env)
      
  }

  if ( sh (returnStdout: true, script: 'ls build.gradle||echo ""').trim()){
      checkok=true
      echo "detected type: java"
      this.doBuildGradle()
      SONAR_LANGUAGE = 'java'
      this.doSQA()
      sh 'ls -l'
      dockerf = 'java.v1'
      sh "mkdir target; mv build/libs/*.jar target/"
      this.buildImage(env,'',env)     
  }

  if ( sh (returnStdout: true, script: 'ls  *.ts|| echo ""').trim()){
      checkok=true
      echo "detected type: web"
      SONAR_LANGUAGE = 'ts'
      this.doSQA()
      this.doBuildWeb()
      sh 'ls -l'
      dockerf = 'web.v1'
      sh "mkdir target; mv dist/* target/"
      this.buildImageWeb(env,'',env)
      
  }
  if ( sh (returnStdout: true, script: 'ls -d client|| echo ""').trim()){
      checkok=true
      echo "detected type: node"
      SONAR_LANGUAGE = 'js'
      //this.doSQA()
      this.doBuildNode()
      sh 'ls -l'
      dockerf = 'node.v1'
      sh "mkdir target; mv dist/ target/ ; mv node_modules/ target/"
      this.buildImageWeb(env,'',env)
      
  }

  if (!checkok) {
      currentBuild.result = 'ABORTED';
      error("No language type detected")
  }   

}



def doBuild(){
    stage("Build")
    withMaven(maven: 'maven') {
      if(project=="mall"){
          sh "mvn clean package -U -Dmaven.test.skip=true"
      }
      sh "mvn clean package -U -Dmaven.test.skip=true"
    }
    
}

def doBuildGradle(){
    stage("Build")

    sh "echo 5.currect dir#####: `pwd`"
    sh "ls -l"
    withGradle(gradle: 'gradle'){
       if (project == "league") {
          sh "gradle clean build -g /root/.gradle/${JOB_NAME} -x test"
        }
       if (project == "mall") {
          sh "gradle clean build -g /root/.gradle/${JOB_NAME} -PuploadGradle=true -x test"
          sh "echo 6..####"
        }

       sh "gradle clean build -g /root/.gradle/${JOB_NAME} -PuploadGradle=true -x test"
    }
    
}
def doBuildWeb(){
    stage("BuildWeb")
 
    isbuild = sh (returnStdout: true, script: 'ls dist/*|| echo ""').trim()
    if (!isbuild) {

        if ( "$project" == "aliance-shop-taro") {
           sh """
              npm config ls -l 
              npm install --loglevel silly
              npm run build:h5
           """
        } else {
           sh '''
                npm install --loglevel silly
                npm run build:h5
          '''
        }
    }

    
}
def doBuildNode(){
    stage("BuildNode")
    echo "${deploy}"
    if ( "$deploy" == "aliance-admin") {
        sh """
          npm install
          npm run download
          npm run build
        """
    } else {
        sh """
          npm config ls -l 
          npm install --loglevel silly
          npm run build:h5
        """
    }

}

def changeDir(){
    if ( deploy == "mall-business") {
     pjpath="mall-application/business-web"
    }
    if (deploy =="mall-buyer") {
     pjpath="mall-domain/${deploy}/buyer-web"
    }
    if (deploy =="mall-data") {
      pjpath="mall-domain/${deploy}/data-web"
    }
    if (deploy =="mall-goods") {
      pjpath="mall-domain/${deploy}/goods-web"
    }
    if (deploy =="mall-helper") {
      pjpath="mall-helper/helper-web"
    }
    if (deploy =="mall-job") {
      pjpath="mall-application/job-web"
    }
    if (deploy =="mall-mini") {
      pjpath="mall-application/mini-web"
    }
    if (deploy =="mall-open") {
      pjpath="mall-application/open-web"
    }
    if (deploy =="mall-operation") {
      pjpath="mall-application/operation-web"
    }
    if (deploy =="mall-order") {
      pjpath="mall-domain/${deploy}/order-web"
    }
    if (deploy =="mall-permission") {
      pjpath="mall-domain/${deploy}/permission-web"
    }



}

// arg repo is 'REGISTRY_'  suffix for jenkins global variable
// arg ns is kubernetes namespace prefix, default is project name
// arg label is 'imageMap' key
def buildImage(repo,ns=null,label) {
    stage("buildImage")
    echo "repo:${repo}, ns:${ns}, label: ${label}"
    if ( !repo || !label) {
      echo "[ERROR] argument cannot be null"
      currentBuild.result = 'FAILURE'      
      return
    }   

    if (ns) {
      ns = ns + "-" + project
    }else{
      ns = project
    }

    //imageTag = sh (returnStdout: true, script: "git rev-parse --short HEAD ||echo .").trim()
    dockerpath = "${project}/${deploy}:${ns}_b${env.Build_id}"
    if ( "x${params.XBUILD_VERSION}" != 'x' ) {
       dockerpath = "${project}/${deploy}:${ns}_${params.XBUILD_VERSION}"
    }
    echo "############${dockerpath}#################"

    
    REGISTRY = "REGISTRY".toUpperCase()
    REGISTRY_CREID = "REGISTRY_CREID".toUpperCase()
    echo env."${REGISTRY}" + "/${dockerpath}"

    tdeploy= sh (returnStdout: true, script: "echo ${deploy} ||echo .").trim()
    restapi = sh (returnStdout: true, script: "ls -d *${tdeploy} ||echo .").trim()
    echo "3.tdeploy: ${tdeploy}, restapi: ${restapi}"
    if ( sh (returnStdout: true, script: "ls -l ${restapi}/target/ || echo ''").trim() == ''){

        restapi = sh (returnStdout: true, script: " ls -d */target|tr -d '/target' ").trim()

    }
    sh  "ls -l ${restapi}/target/"

    sh "curl -o ${restapi}/target/Dockerfile ${HTTP_FILE_URL}/Dockerfile-${dockerf}-${project}-${deploy}"

    if ( sh (returnStdout: true, script: "grep '404 Not Found' ${restapi}/target/Dockerfile  ||echo ''").trim()) {

         sh "curl -o ${restapi}/target/Dockerfile ${HTTP_FILE_URL}/Dockerfile-${dockerf}-${project}"
  
    }

    sh "echo ENV buildNumber ${env.Build_id} >> ${restapi}/target/Dockerfile"
 

    echo env."${REGISTRY}" + " " + env."${REGISTRY_CREID}"
    docker.withRegistry(env."${REGISTRY}",env."${REGISTRY_CREID}") {
      def buildImage = docker.build(dockerpath,"${restapi}/target")
      buildImage.push()
    }
      def url = "${env.REGISTRY}".split("/")[2]
      println url
      //sh "docker rmi registry.xyb2b.com.cn/${dockerpath}"
      sh "docker rmi ${url}/${dockerpath}"
      sh "docker rmi ${dockerpath}"

}

//
def buildImageWeb(repo,ns=null,label) {
    stage("buildImage")
    if ( !repo || !label) {
      echo "[ERROR] argument cannot be null"
      currentBuild.result = 'FAILURE'      
      return
    }   

    if (ns) {
      ns = ns + "-" + project
    }else{
      ns = project
    }
    
    dockerpath = "${project}/${deploy}:${ns}_${params.XBUILD_VERSION}"
    
    REGISTRY = "REGISTRY".toUpperCase()
    REGISTRY_CREID = "REGISTRY_CREID".toUpperCase()
    echo env."${REGISTRY}" + "/${dockerpath}"

    sh  "ls -l ./"
    tdeploy= sh (returnStdout: true, script: "echo ${deploy} ||echo .").trim()
    restapi = sh (returnStdout: true, script: "ls -d *${tdeploy} ||echo .").trim()
    echo "3.tdeploy: ${tdeploy}, restapi: ${restapi}"
    if ( sh (returnStdout: true, script: "ls -l ${restapi}/target/ || echo ''").trim() == ''){

        restapi = sh (returnStdout: true, script: " ls -d */target|tr -d '/target' ").trim()

    }
    sh  "ls -l ${restapi}/target/"

    sh "curl -o ${restapi}/target/Dockerfile ${HTTP_FILE_URL}/Dockerfile-${dockerf}-${deploy}"

    if ( sh (returnStdout: true, script: "grep '404 Not Found' ${restapi}/target/Dockerfile  ||echo ''").trim()) {

         sh "curl -o ${restapi}/target/Dockerfile ${HTTP_FILE_URL}/Dockerfile-${dockerf}"
  
    }

    sh "echo ENV buildNumber ${env.Build_id} >> Dockerfile"


    docker.withRegistry(env."${REGISTRY}",env."${REGISTRY_CREID}") {
      def buildImage = docker.build(dockerpath,"${restapi}/target")
      parallel A: { 
      buildImage.push()
     },B:{
	//timeout(5) {
	        sleep(time: 60, unit: 'SECONDS')	
	//       }
	}
    }
    imageMap."${label}" = ['project': project] 
    imageMap."${label}" = ['deploy' : deploy]
    imageMap."${label}" = ['ns' : ns]
    imageMap."${label}" = ['bid' : env.Build_id]
    imageMap."${label}" = ['repo': REGISTRY]
    imageMap."${label}" = ['repoid': REGISTRY_CREID]
    imageMap."${label}" = ['fullpath': env."${REGISTRY}" + "/${dockerpath}"]

}
//
def buildImageNode(repo,ns=null,label) {
    stage("buildImageNode")
    if ( !repo || !label) {
      echo "[ERROR] argument cannot be null"
      currentBuild.result = 'FAILURE'      
      return
    }   

    if (ns) {
      ns = ns + "-" + project
    }else{
      ns = project
    }
    
    dockerpath = "${project}/${deploy}:${ns}_b${env.Build_id}"
    
    REGISTRY = "REGISTRY".toUpperCase()
    REGISTRY_CREID = "REGISTRY_CREID".toUpperCase()
    echo env."${REGISTRY}" + "/${dockerpath}"

    sh "echo ENV buildNumber ${env.Build_id} >> Dockerfile"


    docker.withRegistry(env."${REGISTRY}",env."${REGISTRY_CREID}") {
      def buildImage = docker.build(dockerpath,".")
      parallel A: { 
      buildImage.push()
     },B:{
	//timeout(5) {
	        sleep(time: 60, unit: 'SECONDS')	
	//       }
	}
    }

}

def shGit(cmd,id=null) {
    if (!id) { id = orgproject }
    sh 'git config --global credential.helper /opt/bin/gitcredential-helper.sh'
    withCredentials([[
        $class: 'UsernamePasswordMultiBinding',
        credentialsId: id,
        usernameVariable: 'GITUVAR',
        passwordVariable: 'GITPVAR'
   ]]) {
            sh "${cmd}"
        }
}



def printMes(value,color){
    colors = ['red'   : "\033[40;31m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m",
              'blue'  : "\033[47;34m ${value} \033[0m",
              'green' : "[1;32m>>>>>>>>>>${value}>>>>>>>>>>[m",
              'green1' : "\033[40;32m >>>>>>>>>>>${value}<<<<<<<<<<< \033[0m" ]
    ansiColor('xterm') {
        println(colors[color])
    }
}
def doSQA(){
    stage("SonarQube analysis code")
    SONAR_ENCODING = 'UTF-8'
    PROJECT_NAME = deploy
    SONAR_HOST = 'http://sonarqube.xyb2b.com.cn'
    def SONAR_HOME = tool name: 'sonar-scanner'
   // withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_TOKEN')]) {
     withSonarQubeEnv('sonarqube') {
      if(SONAR_LANGUAGE == 'java') {
        sh """ 
            ${SONAR_HOME}/bin/sonar-scanner -Dsonar.host.url=${SONAR_HOST} \
            -Dsonar.projectKey=com.xingyun.${project}:${PROJECT_NAME} \
            -Dsonar.projectName=${PROJECT_NAME} \
            -Dsonar.projectVersion=1.0 \
            -Dsonar.ws.timeout=30 \
            -Dsonar.sources=./src/main/java \
            -Dsonar.java.binaries=./build/classes \
            -Dsonar.java.libraries=/root/.gradle/${JOB_NAME} \
            -Dsonar.branch.name=${params.XBUILD_BRANCHE} \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.language=${SONAR_LANGUAGE}
        """
      }
      if(SONAR_LANGUAGE == 'ts') {
         sh """
            ${SONAR_HOME}/bin/sonar-scanner -Dsonar.host.url=${SONAR_HOST} \
            -Dsonar.projectKey=com.xingyun.${project}:${PROJECT_NAME} \
            -Dsonar.projectName=${PROJECT_NAME} \
            -Dsonar.projectVersion=1.0 \
            -Dsonar.ws.timeout=30 \
            -Dsonar.sources=./src \
            -Dsonar.branch.name=${params.XBUILD_BRANCHE} \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.language=${SONAR_LANGUAGE}
        """
      }

      if(SONAR_LANGUAGE == 'js') { 
         sh """
            ${SONAR_HOME}/bin/sonar-scanner  \
            -Dsonar.projectKey=com.xingyun.${project}:${PROJECT_NAME} \
            -Dsonar.projectName=${PROJECT_NAME} \
            -Dsonar.projectVersion=1.0 \
            -Dsonar.ws.timeout=30 \
            -Dsonar.sources=./client/src \
            -Dsonar.branch.name=${params.XBUILD_BRANCHE} \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.language=${SONAR_LANGUAGE}
        """

       }

    }

}

return this;
