repositories.remote << 'http://repo1.maven.org/maven2'

define 'ironmq' do
  project.version = "0.0.3"
  test.with 'junit:junit:jar:4.10'
  compile.with transitive('net.sf.json-lib:json-lib:jar:jdk15:2.4')
  package :jar
end
