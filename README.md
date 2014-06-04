postgra
=======

Microservice for PostgreSQL via HTTP/JSON API

See https://trello.com/b/SuTdcMZd/postgra

<pre>
  curl -s -k https://ngena.com:8843/api/admin/createDatabase -d '{ 
     database: "mydb", password: "mypass" 
  }'
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
  curl -s -k https://ngena.com:8843/api/admin/dropDatabase -d '{ 
     database: "mydb", password: "mypass" 
  }'
</pre>