
database=database1
password=password2
table=table3
email=test4@test.org
user=$email
databaseJson="database: '$database', password: '$password', guest: true"
tableJson="table: '$table', user: '$user', $databaseJson"
tmp=~/tmp/postgra/test
mkdir -p $tmp

c2curl() {
  echo "$@"
  curl -s -k https://ngena.com:8843/api/$1 -d "$2" > $tmp/out
  cat $tmp/out | python -mjson.tool || cat $tmp/out
  curlCode=$?  
  echo 
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
}

c0guest() {
  c2curl 'guest/insert' "{ $tableJson, data: { name: 'Evan1', type: 'person'} }"
  c2curl 'guest/insert' "{ $tableJson, data: { name: 'Evan2', type: 'person' } }"
  c2curl 'guest/select' "{ $tableJson, where: { name: 'Evan2' } }"
  c2curl 'guest/update' "{ $tableJson, data: { type: 'individual' }, where: { name: 'Evan2' } }"
  c2curl 'guest/select' "{ $tableJson, where: { name: 'Evan2' } }"
}

c0reg() {
  c2curl 'guest/register' "{ email: '$email', password: '$password' }"
  c2curl 'guest/login' "{ email: '$email', password: '$password' }"
}

c0dereg() {
  c2curl 'guest/deregister' "{ email: '$email', password: '$password' }"
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

c0explicit() {
  curl -s -k https://ngena.com:8843/api/admin/createDatabase -d '{ database: "mydb", password: "mypass" }' | python -mjson.tool
  curl -s -k https://ngena.com:8843/api/admin/createTable -d '{ 
     database: "mydb",
     password: "mypass",
     table: "person", 
     sql: "name varchar(64), age int"
  }' | python -mjson.tool
  curl -s -k https://ngena.com:8843/api/admin/insert -d '{
     database: "mydb",
     password: "mypass",
     table: "person",
     data: {
        name: "Evan",
        age: 45
     }
  }' | python -mjson.tool
  curl -s -k https://ngena.com:8843/api/admin/update -d '{
     database: "mydb",
     password: "mypass",
     table: "person",
     data: {
        age: 46
     },
     where: {
       name: "Evan"
     }
  }' | python -mjson.tool
  curl -s -k https://ngena.com:8843/api/admin/select -d '{
     database: "mydb",
     password: "mypass",
     table: "person",
     where: {
        name: "Evan"
     }
  }' | python -mjson.tool
  curl -s -k https://ngena.com:8843/api/admin/dropTable -d '{
     database: "mydb",
     password: "mypass",
     table: "person"
  }' | python -mjson.tool
  curl -s -k https://ngena.com:8843/api/admin/dropDatabase -d '{ database: "mydb", password: "mypass" }' | python -mjson.tool
}


if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

