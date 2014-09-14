

set -u

cd ~/nb/lib/maven

gsonPath='com/google/code/gson/gson'
gson_2_3="$gsonPath/2.3/gson-2.3"
gson_2_2_2="gsonPath/2.2_2/gson-2.2.2.jar"
tomcat_jdbc='org/apache/tomcat/tomcat-jdbc/8.0.9/tomcat-jdbc-8.0.9'

wget_gson() {
  wget http://central.maven.org/maven2/com/google/code/gson/gson/2.3/gson-2.3.jar
  wget http://central.maven.org/maven2/com/google/code/gson/gson/2.3/gson-2.3-sources.jar
  wget http://central.maven.org/maven2/com/google/code/gson/gson/2.3/gson-2.3-javadoc.jar
}

c1wget() {
  path=$1
  name=`basename $1 .jar`
  echo
  echo "$path $name"
  curl -s -I http://central.maven.org/maven2/$path | grep '^HTTP'
  if ls -l $name.jar
  then
    echo "exists: $name"
  else
    if curl -s -I http://central.maven.org/maven2/$path
    then
      curl -s -O http://central.maven.org/maven2/$path
    fi
  fi
}


c1fetch() {
  c1wget $1.jar
  c1wget $1-sources.jar
  c1wget $1-javadoc.jar
}

c1fetch $gson_2_2_2



