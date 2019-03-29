Search Scraper



Challenge:

Write a java CLI that can search google for batches of keywords, and store the search results.



Instructions:

Your solution will need to be able to be downloaded, set up, and run on MacOS or a unix OS (ubuntu preferred)
You need to provide instructions on how to set up, test, and run your application
The solution will use Elasticsearch to store search tasks and search results
Each keyword in a search task must be searched
Each search must be done at a random interval, between 1 and 5 seconds between searches
Each URL returned for a search must be parsed, downloaded, and stored in a document (see below schema)
The solution can parallelize processing to increase performance (this is a nice to have)


CLI Interface

a cli user should be able to "add" a keyword to be searched/scraped
a cli user should be able to "list" of scraped documents for a keyword
a cli user should be able to read a scraped document


The steps the CLI should perform are

Get a SearchTask from the data store,
For each keyword in the SearchTask perform a google search of the form
https://www.google.com/search?q={keyword}&num=10&as_qdr=all,
Parse each URL returned in the google search result, download it, and write the downloaded content to a SearchResult document


Elasticsearch Schema to help you get started:

// Stores the list of keywords for given client name

// create 3 documents based on the below schema with the following

// 1) searchName: "centralized logging", keywords: "datadog", "metrics", "logging"

// 2) searchName: "relational databases", keywords: "postgres", "sql", "rbdms ", "mysql"

// 3) searchName: "programming languages", keywords: "golang", "rust", "ruby", "python", "java", "clojure"

{

    "SearchTask" : {

        "properties" : {

            "taskName" : { "type" : "string", "index" : "not_analyzed" },

            "keywords" : { "type" : "string", "index" : "not_analyzed" }

            "createdAt" : { "type" : "date", "format": "epoch_millis||dateOptionalTime" },

            "active" : { "type" : "boolean" }

    }

}



// The search results should be saved in a SearchResult type, taskName should be the same name of the SearchTarget type that did the search.

// content and title should be HTTP download content of search result obtained using the SearchTarget keyword

{

    "SearchResult" : {

        "properties" : {

            "taskName" : { "type" : "string", "index" : "not_analyzed" }

            "content" : { "type" : "string", "index" : "analyzed" },

            "title" : { "type" : "string", "index" : "analyzed" },

            "httpStatusCode" : { "type" : "integer" },

            "createdAt" : { "type" : "date", "format": "epoch_millis||dateOptionalTime" }

    }

}



Feel free to modify the schema to accommodate your solution.



Warm regards,
Ashley
