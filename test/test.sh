

c1curl() {
  curl -k https://localhost:8443/api/$1
  curlCode=$?  
  echo 
}

c1psqlc() {
  psql -h localhost postgra postgra -c "$1" 

}

c1createUser() {
  c1curl createUser/$1 &&
    psql -h localhost postgra postgra -c "\du" && 
    psql -h localhost postgra postgra -c "drop role $1" 
}
c1createDatabase() {
  c1curl createDatabase/$1 &&
    psql -h localhost postgra postgra -c "\l" && 
    psql -h localhost postgra postgra -c "drop database $1" 
}

c1dropDatabase() {
  c1psqlc "\l" | grep $1 &&
    c1psqlc "drop database $1" &&
    c1psqlc "\l" 
}  

c1dropRole() {
  c1psqlc "\du" | grep $1 &&
    c1psqlc "drop role $1" &&
    c1psqlc "\du" 
}  

c0default() {
  c1dropRole test1u
  c1createUser test1u
  c1dropDatabase test1
  c1createDatabase test1
  c1dropDatabase test1
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

