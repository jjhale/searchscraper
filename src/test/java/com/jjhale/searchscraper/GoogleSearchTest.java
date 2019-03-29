package com.jjhale.searchscraper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GoogleSearchTest {

    @Test
    public void testBuildUri()
    {
        GoogleSearch googleSearch = new GoogleSearch("");
        String url = googleSearch.buildSearchUrl("simple");
        assertEquals("https://www.google.com/search?q=simple&num=10&as_qdr=all", url);
    }

    @Test
    public void testBuildUriAmp()
    {
        GoogleSearch googleSearch = new GoogleSearch("");
        String url = googleSearch.buildSearchUrl("include&this");
        assertEquals("https://www.google.com/search?q=include%26this&num=10&as_qdr=all", url);
    }

    @Test
    public void testBuildUriSpace()
    {
        GoogleSearch googleSearch = new GoogleSearch("");
        String url = googleSearch.buildSearchUrl("a space");
        assertEquals("https://www.google.com/search?q=a+space&num=10&as_qdr=all", url);
    }

    @Test
    public void testBuildUriPlus()
    {
        GoogleSearch googleSearch = new GoogleSearch("");
        String url = googleSearch.buildSearchUrl("1+1");
        assertEquals("https://www.google.com/search?q=1%2B1&num=10&as_qdr=all", url);
    }

    @Test
    public void testBuildUriColon()
    {
        GoogleSearch googleSearch = new GoogleSearch("");
        String url = googleSearch.buildSearchUrl("1:1");
        assertEquals("https://www.google.com/search?q=1%3A1&num=10&as_qdr=all", url);
    }
}

