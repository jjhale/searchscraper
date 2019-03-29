Hi,

I have some points that I'd like to clarify.

1) Elasticsearch
Can I assume that there is already an Elasticsearch instance running?

I'd like to have the URL to HTTP rest endpoint stored in an environmental
variable.

2) Which URLs to save?
In the spec it says:
> Each URL returned for a search must be parsed, downloaded, and stored in a document 

Can I assume that we should only parse the search result urls, rather than 
every URL on the search page? Eg excluding ads, knowledge graph hits etc

I was planning on using the following selector:
```
div.srg > div.g > div > div.rc > div.r > a[href]
```

This selector gets the main search results, but also any results in 
the "People also search for... " results - is that ok?

3) Search tasks

Should I assume that a keyword can include a space? Eg "apache spark"?

4) SearchTarget === SearchTask
You say:
> The search results should be saved in a SearchResult type, taskName should be the same name of the SearchTarget type that did the search.

Can I assume that the "name of the SearchTarget" is the "searchName", aka the "taskName" in SearchTask?


5) Adding keyword via CLI

For adding a keyword via the CLI is it ok to assume that we are actually
adding an entire SearchTask - ie a searchName and list of keywords? 

eg:

```
scrape --add --name="centralized logging" --keywords="datadog", "metrics", "logging"
```

6) Search Task name collisions?

How should we handle multiple search tasks with the same name? Should they be allowed or rejected?

Thanks,

Joe Hale
