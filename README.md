# Address Service

This is an example Web Service making use of a pure functional stack.
Namely:
* Cats
* Http4s
* Circe
* Doobie

# Database

This service uses a postgres database.
- To fire up a dockerized postgres database run
```
start-deps
```
- To shutdown the dockerized postgres database run
```
stop-deps
```

- To create the needed database and user run
```
psql -h localhost -p 5432 -U postgres < src/main/resources/db/create_db.sql
```