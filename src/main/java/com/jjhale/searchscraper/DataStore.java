package com.jjhale.searchscraper;


import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;

public class DataStore {

    private RestHighLevelClient client;

    private final static String SEARCH_TASK = "searchtask";
    private final static String SEARCH_RESULT = "searchresult";




    public DataStore() {
        // default connection
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")));
    }

    public boolean addTask(String name, List<String> keywords) {
        // Use a map to define the json ala
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-document-index.html
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("taskName", name);
        jsonMap.put("keywords", keywords);
        jsonMap.put("createdAt", System.currentTimeMillis());
        jsonMap.put("active", false);

        // add with the name as the Document id
        IndexRequest request = new IndexRequest("searchtask", "doc")
                .id(name)
                .source(jsonMap).opType(DocWriteRequest.OpType.CREATE);


        // Perform sync request
        try {
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            return true;
        } catch(ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                // TODO log failure
                System.err.println("Failed to add task named `" + name + "` as one already exists with that name.");
            } else {
                System.err.println("Exception:\n" + e.getMessage());
            }
            return false;
        } catch (IOException e) {
            // TODO log exception
            System.err.println("Failed to add task named `" + name + "`. Got exception:\n" + e.getMessage());
            return false;
        }
    }

    public boolean addSearchResult(String taskName, String keyword,
                                   String content, String title,
                                   int statusCode, long creationTimeMillis) {

        System.out.println("Adding Search results");
        // Use a map to define the json ala
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-document-index.html
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("taskName", taskName);
        jsonMap.put("keyword", keyword);
        jsonMap.put("content", content);
        jsonMap.put("title", title);
        jsonMap.put("httpStatusCode", statusCode);
        jsonMap.put("createdAt", creationTimeMillis);


        // add with the name as the Document id
        IndexRequest request = new IndexRequest(SEARCH_RESULT, "doc")
                .id(UUID.randomUUID().toString())
                .source(jsonMap).opType(DocWriteRequest.OpType.CREATE);


        // Perform sync request
        try {
            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            return true;
        } catch(ElasticsearchException e) {
            if (e.status() == RestStatus.CONFLICT) {
                // TODO log failure
                System.err.println("Failed to add search result `" + taskName +" : " +
                        keyword + "` as one already exists with that name.");
            } else {
                System.err.println("Exception:\n" + e.getMessage());
            }
            return false;
        } catch (IOException e) {
            // TODO log exception
            System.err.println("Failed to add search result named `" + taskName + "`. Got exception:\n" + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> fetchNextTask() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("active", false));
        sourceBuilder.from(0);
        // Grab only one task
        sourceBuilder.size(1);
        // oldest first
        sourceBuilder.sort(new FieldSortBuilder("createdAt").order(SortOrder.ASC));

        SearchRequest searchRequest = new SearchRequest(SEARCH_TASK);
        searchRequest.source(sourceBuilder);

        // Sync execute the request:
        Map<String, Object> nextTask = null;
        long version =0;
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            if(searchHits.length == 0) {
                return Collections.emptyMap();
            } else if(searchHits.length == 1) {
                nextTask = searchHits[0].getSourceAsMap();
                version = searchHits[0].getVersion();

            } else {
                System.err.println("Unexpected number of hits returned: " + searchHits.length);
                return null;
            }

        } catch(IOException e) {
            // TODO log exception
            System.err.println("Got exception:\n" + e.getMessage());
            return null;
        }

        // Update
        if((boolean)nextTask.get("active") ) {
            throw new IllegalStateException("Expected to only get inactive tasks back");
        }
        nextTask.put("active", true);

        String taskName = (String)nextTask.getOrDefault("taskName", "");
        UpdateRequest request = new UpdateRequest(SEARCH_TASK, "doc", taskName)
                .doc("active", true);

        try {
            UpdateResponse updateResponse = client.update(
                    request, RequestOptions.DEFAULT);
            if (updateResponse.getResult() != DocWriteResponse.Result.UPDATED) {
                return null;
            } else {
                return nextTask;
            }
         } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search for all collected keywords
     * @return
     */
    public List<String> listKeywords() {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest(SEARCH_RESULT);
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        String[] includeFields = new String[] {"keyword", "taskName"};
        searchSourceBuilder.fetchSource(includeFields, null);
        searchRequest.source(searchSourceBuilder);


        Set<String> keywords = new TreeSet<>();

        // Sync execute the request:
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            while (searchHits != null && searchHits.length > 0) {
                // Save the hits
                for(SearchHit hit  : searchHits) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    String keyword  = (String) source.getOrDefault("keyword", "");
                    if(!keyword.equals("")) {
                        keywords.add(keyword);
                    }
                    //String keyword  = hit.field("keyword").getValue();

                }

                // Look at the next batch
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }

            // Clean up the scroll
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean succeeded = clearScrollResponse.isSucceeded();



        } catch(IOException e) {
            // TODO log exception
            System.err.println("Got exception:\n" + e.getMessage());
            return null;
        } catch (ElasticsearchStatusException e) {
            System.err.println("Got exception." + e.getMessage());
            // drop thru and return empyty list
        }

        return new ArrayList<>(keywords);
    }

    /**
     * List docs for a keyword:
     */
    public List<SearchResult> listDocsForKeyword(String keyword) {
        SearchRequest searchRequest = new SearchRequest(SEARCH_RESULT);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("keyword", keyword));
        String[] excludeFields = new String[] {"content"};
        searchSourceBuilder.fetchSource(null, excludeFields);
        searchRequest.source(searchSourceBuilder);


        Set<SearchResult> keywords = new TreeSet<>();

        // Sync execute the request:
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();

            for(SearchHit hit  : searchHits) {
                Map<String, Object> source = hit.getSourceAsMap();
                String id = hit.getId();
                String title  = (String) source.getOrDefault("title", "(missing)");
                String taskName  = (String) source.getOrDefault("taskName", "(missing)");
                int httpStatusCode = (int) source.getOrDefault("httpStatusCode", -1);
                long createdAt = (long) source.getOrDefault("createdAt", -1);
                SearchResult searchResult = new SearchResult(id, taskName, title, httpStatusCode, createdAt);
                keywords.add(searchResult);
            }

        } catch(IOException e) {
            // TODO log exception
            System.err.println("Got exception:\n" + e.getMessage());
            return null;
        } catch (ElasticsearchStatusException e) {
            System.err.println("Got exception." + e.getMessage());
            // drop thru and return empyty list
        }

        return new ArrayList<>(keywords);
    }

    public String read(String docId) {
        GetRequest getRequest = new GetRequest(
                SEARCH_RESULT,
                "doc",
                docId);

        String[] includes = new String[]{"content"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
        getRequest.fetchSourceContext(fetchSourceContext);
        GetResponse getResponse;
        try {
            getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        }
        catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                System.err.println("not found.");
                return null;
            }
            e.printStackTrace();
            return null;
        }
         catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if(getResponse.isExists()) {
            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
            return (String) sourceAsMap.getOrDefault("content", null);
        } else {
            return null;
        }


    }


    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            // TODO LOG EXCEPTION
        }
    }


}
