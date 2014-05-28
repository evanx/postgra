

c1curl() {
  curl -k https://localhost:8443/api/$1
  curlCode=$?  
  echo 
}

c0default() {
  c1curl createDatabase
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

