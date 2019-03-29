package com.jjhale.searchscraper;


import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    private RestHighLevelClient client;

    private final static String SEARCH_TASK = "searchtask";




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
        IndexRequest request = new IndexRequest("searchresult", "doc")
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
            GetResult result = updateResponse.getGetResult();
            if (result.isExists()) {
                return result.sourceAsMap();
            }else {

                return null;
            }
         } catch (IOException e) {
            e.printStackTrace();
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
