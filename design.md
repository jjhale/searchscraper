# CLI:

- choose CLI parsing lib
  - https://mvnrepository.com/artifact/commons-cli/commons-cli/1.4
  
- define interface

# Scraper
- use Jsoup

- looks like we can use an xpath:
//*[@id="rso"]/div[3]/div/div[2]/div/div/div[1]/a
#rso > div:nth-child(3) > div > div:nth-child(2) > div > div > div.r > a

THere is a class called 

div.srg > div.g > div > div.rc > div.r > a

# Elasticsearch
- Connection details as env param
- request Job
- submit job
- should use the REST API

https://www.elastic.co/guide/en/x-pack/current/api-java.html
We plan on deprecating the TransportClient in Elasticsearch 7.0 and removing it completely in 8.0. Instead, you should be using the Java High Level REST Client, which executes HTTP requests rather than serialized Java requests. The migration guide describes all the steps needed to migrate.

Note that embedded elastic search is very strongly discouraged, so gonna use docker instead.
https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html


# how to run?

Could have the googleSearch thing poll ES - let it run as a deamon.
 - might be able to subscribe to different docs?
 - how to deal with multiple scrapers?
 

eg calling it with --deamon gets it going.

vs calling it with Add job, list or read just writes to ES

# faults :
- how to handle partial jobs?
