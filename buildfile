repositories.remote << 'http://repo1.maven.org/maven2'
repositories.remote << 'http://iron-io.github.com/maven/repository'

THIS_VERSION = "0.0.12"
GROUP        = 'ironmq'

JUNIT = 'junit:junit:jar:4.10'

DEPS = [
  'com.google.code.gson:gson:jar:2.1',
  'org.apache.commons:commons-lang3:jar:3.1',
]

define 'ironmq' do
  repositories.release_to[:url] = "file://#{base_dir}/../maven/repository"

  project.version = THIS_VERSION
  project.group   = GROUP
  test.with JUNIT
  DEPS.each do |dep|
    compile.with transitive(dep)
  end
  compile.using(:source => '1.6', :target => '1.6')
  package :jar
end
