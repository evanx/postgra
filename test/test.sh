

database=database1
password=password2
table=table3
user=user4
databaseJson="database: '$database', password: '$password', guest: true"
tableJson="table: '$table', user: '$user', $databaseJson"

c1curl() {
  curl -s -k https://localhost:8443/api/$1 | python -mjson.tool
  curlCode=$?  
  echo 
}

c2curl() {
  echo "$@"
  curl -s -k https://localhost:8443/api/$1 -d "$2" #| python -mjson.tool
  curlCode=$?  
  echo 
}

c1psqlc() {
  psql -h localhost postgra postgra -c "$1" 
}

c1terminate() {
  psql -h localhost postgra postgra -c "select pg_terminate_backend(procpid) from pg_stat_activity where datname = '$1'"
}

c0terminate() {
  psql -h localhost postgra postgra -c "select pg_terminate_backend(procpid) from pg_stat_activity"
}

c2drop() {
  c1psqlc "drop $1 $2"
}  

c1dropDatabase() {
  c1psqlc "\l" | grep $1 &&
    c1psqlc "drop database $1" &&
    c1psqlc "\l" 
}  

c0pdrop() {
  c2drop table $table
  c2drop role $user
  c2drop database $database
}

c0drop() {
  c2curl 'admin/dropTable' "{ $tableJson }"
  c2curl 'admin/dropDatabase' "{ $databaseJson }"
}

c0create() {
  c2curl 'admin/createDatabase' "{ $databaseJson }"
  c2curl 'admin/createTable' "{ $tableJson, sql: 'name text' }"
  c2curl 'guest/insert' "{ $tableJson, data: { name: 'Evan1' } }"
  c2curl 'guest/insert' "{ $tableJson, data: { name: 'Evan2' } }"
  c2curl 'guest/select' "{ $tableJson, where: { name: 'Evan2' } }"
}

c0default() {
  c0create
  c0drop
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

