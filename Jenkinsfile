node{
    stage('Loading') 
    def rootDir = pwd()
    println(rootDir)
    def pipeline = load 'pipeline.groovy'
    pipeline('hello','server')
}