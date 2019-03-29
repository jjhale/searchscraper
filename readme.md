# Geting started:

Overview: 
0) install Docker and maven
1) clone this repo.
2) start Elasticsearch instance
3) Build the uber jar
4) Start the scraper
5) Add search tasks
6) list keywords and documents
7) view documents.

## Running Elasticsearch in docker:

Need docker installed

cd into repo

Start ES with:


docker-compose up

stop with
docker-compose down 

Stop and clear persistent volumnes with
docker-compose down -v

## Build using Maven:

Open terminal in the folder you cloned repo into and run:

mvn -Dmaven.test.skip=true package

## Start the scraper
This is a process that polls the ES instance looking for Search tasks:

Open terminal in the folder you cloned repo into and run:

java -jar target/searchscraper-1.0-SNAPSHOT.jar --scraper --scraper-threads 5

Note that you can quit it by pressing Ctrl + C

## Add tasks

in a new console add a task using 

java -jar target/searchscraper-1.0-SNAPSHOT.jar --add --search-name <SearchTask> --keywords <keyword1> <keyword2> ...

eg
 1) searchName: "centralized logging", keywords: "datadog", "metrics", "logging"

```
java -jar target/searchscraper-1.0-SNAPSHOT.jar --add --search-name "centralized logging" --keywords datadog metrics logging
```

Note that you need to either escape spaces or put a phrase in quotes eg 
```
java -jar target/searchscraper-1.0-SNAPSHOT.jar --add --search-name "centralized logging" --keywords "data dog" datum\ dogs
```

## List keywords

Get a list of all of the keywords scraped using this:
```
java -jar target/searchscraper-1.0-SNAPSHOT.jar --list

```

Add a specific keyword to get the list of results for that document:
```
java -jar target/searchscraper-1.0-SNAPSHOT.jar --list datadog
```

It should return a list of documents. Each is perfix by their document ID (a random UUID)

## Read document:

Get the content of the doc using:
```
java -jar target/searchscraper-1.0-SNAPSHOT.jar --read <document ID>
```


# Known issues:

1) Putting the mapping

I messed up configuring the Mapping for the indicies - so when you try to search for
keywords which include spaces they don't get hit (cos the whole phrase aint in the inverted index).
Ran out of time to fix that.

2) Logging 
Would have liked to have decent logging 

3) Exception handling for ES operations

It is kinda messy.





