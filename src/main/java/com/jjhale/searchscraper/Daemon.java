package com.jjhale.searchscraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

/**
 * Polls ES randomly every 1 to 5 seconds to see if there are any new search tasks.
 */
public class Daemon {
    private DataStore dataStore;

    public Daemon(int numScraperThreads) {
        dataStore = new DataStore();
        Random rand = new Random();

        ExecutorService scrapers = Executors.newFixedThreadPool(numScraperThreads);

        while(true) {
            Map<String, Object> nextTask = dataStore.fetchNextTask();
            if(nextTask != null && !nextTask.isEmpty() ) {
                String taskName = (String) nextTask.getOrDefault("taskName", "");
                List<String> keywords = (List<String>) nextTask.getOrDefault("keywords", Collections.EMPTY_LIST);
                for(String keyword : keywords) {
                    // Search google for the keyword:
                    GoogleSearch search = new GoogleSearch(keyword);

                    try {
                        List<String> urls = search.execute();
                        for(String url : urls) {
                            scrapers.submit(
                                    () -> downloadUrlAndStore(taskName, keyword, url)
                            );
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            int waitMs = rand.nextInt(5001);
            System.out.println("Waiting for " + waitMs + "ms until checking for new tasks");
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                break; // exit the while loop
            }
        }
        scrapers.shutdownNow();
    }


    /**
     *
     * @param taskName
     * @param keyword
     * @param url
     */
    protected void downloadUrlAndStore(String taskName, String keyword, String url) {
        try {
            Connection connection = Jsoup.connect(url).ignoreHttpErrors(true);
            Document doc = connection.get();
            String title = doc.title();
            String body = connection.response().body();

            dataStore.addSearchResult(
                    taskName, keyword, body, title,
                    connection.response().statusCode(),
                    System.currentTimeMillis() );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
