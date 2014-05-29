

user=test1user
database=test2
password=$user
table=test3table
userJson="user: '$user', password: '$password'"
databaseJson="database: '$database', $userJson"
tableJson="table: '$table', $databaseJson"

c1curl() {
  curl -k https://localhost:8443/api/$1 
  curlCode=$?  
  echo 
}

c2curl() {
  echo "$@"
  curl -k https://localhost:8443/api/$1 -d "$2" 
  curlCode=$?  
  echo 
}

c1psqlc() {
  psql -h localhost postgra postgra -c "$1" 
}

c2drop() {
  c1psqlc "drop $1 $2"
}  

c2createDatabase() {
  c1curl createDatabase/$1 "$2" &&
    psql -h localhost postgra postgra -c "\l" 
}

c1dropDatabase() {
  c1psqlc "\l" | grep $1 &&
    c1psqlc "drop database $1" &&
    c1psqlc "\l" 
}  

c0default() {
  c2curl createDatabase "{ $databaseJson }"
  c2curl createUser "{ $databaseJson }"
  c2curl createTable "{ $tableJson, sql: 'id int, name text' }"
  c2curl dropTable "{ $tableJson }"
  c2curl dropDatabase "{ $databaseJson }"
  c2curl dropUser "{ $userJson }"
  c2drop table $table
  c2drop role $user
  c2drop database $database
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

