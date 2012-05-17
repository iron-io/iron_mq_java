repositories.remote << 'http://repo1.maven.org/maven2'
repositories.remote << 'http://iron-io.github.com/maven/repository'

define 'ironmq' do
  repositories.release_to[:url] = "file://#{base_dir}/../maven/repository"

  project.version = "0.0.4"
  test.with 'junit:junit:jar:4.10'
  compile.with transitive('net.sf.json-lib:json-lib:jar:jdk15:2.4')
  package :jar
end
