def example1() {
  println 'Hello from example1'
}

def example2() {
  println 'Hello from example2'
  println "Build Num " + currentBuild.number
}

return this