package com.jjhale.searchscraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GoogleSearch {

    private String keyword;
    private List<String> results;

    public GoogleSearch(String keyword) {
        this.keyword = keyword;
    }

    public List<String> execute() throws IOException {
        String searchUrl = buildSearchUrl(this.keyword);
        Document doc = Jsoup.connect(searchUrl).get();
        String title = doc.title();
        Elements links = doc.select("div.srg > div.g > div > div.rc > div.r > a[href]");
        System.out.println("title is: " + title);
        results = new ArrayList<>();
        for (Element link : links) {
            results.add(link.attr("href"));
        }
        return results;
    }

    public List<String> getResults() {
        return results;
    }

    public String buildSearchUrl(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            return "https://www.google.com/search?q="
                    + encodedKeyword +
                    "&num=10&as_qdr=all";
        } catch (UnsupportedEncodingException e) {
            // Hmm, UTF-8 not supported :(
            System.err.println("Bad encoding: " + e.getMessage());
            return ""; // TODO: nicer error handling or bomb?
        }

    }
}
