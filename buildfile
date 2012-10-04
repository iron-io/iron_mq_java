repositories.remote << 'http://repo1.maven.org/maven2'
repositories.remote << 'http://iron-io.github.com/maven/repository'

define 'ironmq' do
  repositories.release_to[:url] = "file://#{base_dir}/../maven/repository"

  project.version = "0.0.11"
  test.with 'junit:junit:jar:4.10'
  compile.with transitive('com.google.code.gson:gson:jar:2.1')
  compile.using(:source => '1.6', :target => '1.6')
  package :jar
end
