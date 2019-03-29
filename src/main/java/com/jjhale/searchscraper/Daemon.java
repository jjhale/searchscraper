package com.jjhale.searchscraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
    private ExecutorService scrapers;
    private Random rand;

    public Daemon(int numScraperThreads) {
        dataStore = new DataStore();
        rand = new Random();
        scrapers = Executors.newFixedThreadPool(numScraperThreads);


    }

    public void start() {
        System.out.println("Starting scraper - polling the ES instance for search tasks.");
        System.out.println("Quit by pressing <Ctrl> + c");

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
            int waitMs = 1000+rand.nextInt(4001);
            System.out.println("Waiting for " + waitMs + "ms until checking for new tasks");
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException e) {
                System.out.println("Ending.");
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
        String title = "";
        String body = "";
        int status = 0;
        try {
            System.out.println("Downloading " + url + " for keyword " + keyword) ;
            Connection connection = Jsoup.connect(url).ignoreHttpErrors(true);
            Connection.Response response = connection.execute();
            System.out.println("Executed " + url);
            status = response.statusCode();
            body = response.body();//connection.response().body();
            Document doc = Jsoup.parse(body);
            title = doc.title();



            System.out.println("Downloaded  " + url);
        } catch (SocketTimeoutException e) {
            status = 408; // Time out
            title = url;
            body = "Time out when fetching " + url;
        } catch (UnsupportedMimeTypeException e) {
            // EG if we try and download a pdf or a dmg etc.
            status=200; // must have got a response
            title=url;
            body="Unsupported MIME type: " + e.getMimeType() + " from " + e.getUrl();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        dataStore.addSearchResult(
                taskName, keyword, body, title,
                status,
                System.currentTimeMillis() );

    }


}
