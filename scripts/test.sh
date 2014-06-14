
database=database1
password=password2
table=table3
email=test4@test.org
user=$email
databaseJson="database: '$database', password: '$password', guest: true"
tableJson="table: '$table', user: '$user', $databaseJson"
tmp=~/tmp/postgra/test
mkdir -p $tmp

c1curl() {
  curl -s -k https://localhost:8443/api/$1 > $tmp/out
  cat $tmp/out | python -mjson.tool || cat $tmp/out
  curlCode=$?  
  echo 
}

c2curl() {
  echo "$@"
  curl -s -k https://localhost:8443/api/$1 -d "$2" > $tmp/out
  cat $tmp/out | python -mjson.tool || cat $tmp/out
  curlCode=$?  
  echo 
}

c2curlp() {
  curl -s -k https://localhost:8443/api/$1 -d @- -H "email: $email" -H "content-type: $2" > $tmp/out
  cat $tmp/out | python -mjson.tool || cat $tmp/out
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
  c2curl 'admin/createTable' "{ $tableJson, sql: 'name text, type text' }"
  c2curl 'admin/execute' "{ $databaseJson, sql: 'create unique index on $table (name)' }"
}

c0guest() {
  c2curl 'guest/insert' "{ $tableJson, data: { name: 'Evan1', type: 'person'} }"
  c2curl 'guest/insert' "{ $tableJson, data: { name: 'Evan2', type: 'person' } }"
  c2curl 'guest/select' "{ $tableJson, where: { name: 'Evan2' } }"
  c2curl 'guest/update' "{ $tableJson, data: { type: 'individual' }, where: { name: 'Evan2' } }"
  c2curl 'guest/select' "{ $tableJson, where: { name: 'Evan2' } }"
  c2curl 'guest/save' "{ $tableJson, data: { name: 'Evan3', type: 'java developer'} }"
  c2curl 'guest/save' "{ $tableJson, data: { id: 10, type: 'javascript developer'} }"
}

c0reg() {
  c2curl 'user/register' "{ email: '$email', password: '$password' }"
  c2curl 'user/login' "{ email: '$email', password: '$password' }"
}

c0dereg() {
  c2curl 'user/deregister' "{ email: '$email', password: '$password' }"
}

c0testreg() {
  c0reg
  c0dereg
}

c0default() {
  c0create
  c0reg
  c0guest
  c0dereg
  c0drop
}

c0testpost() {
  echo "{}" | c2curlp upload/article 'application/json'
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

