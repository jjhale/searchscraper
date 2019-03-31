package com.jjhale.searchscraper;

import org.junit.Test;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DataStoreTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testConneciton()
    {
        DataStore ds = new DataStore();
        ds.close();
        assertTrue( true );
    }

    @Test
    public void testAdd()
    {
        DataStore ds = new DataStore();
        boolean result = ds.addTask("test" + System.currentTimeMillis(), Arrays.asList("term1", "keyword2"));

        ds.close();
        assertTrue( result );
    }



    @Test
    public void test()
    {
        DataStore ds = new DataStore();
        Map<String, Object> result = ds.fetchNextTask();

        ds.close();
        assertTrue( result != null);
    }

    @Test
    public void testList()
    {
        DataStore ds = new DataStore();
        List<String> result = ds.listKeywords();

        for(String kw : result) {
            System.out.println(kw);
        }

        ds.close();
        assertTrue( result != null);
    }

    @Test
    public void testListKeyword()
    {
        DataStore ds = new DataStore();
        List<SearchResult> result = ds.listDocsForKeyword("term1");

        for(SearchResult kw : result) {
            System.out.println(kw);
        }

        ds.close();
        assertTrue( result != null);
    }

    @Test
    public void testListSpacyKeyword()
    {
        DataStore ds = new DataStore();
        List<SearchResult> result = ds.listDocsForKeyword("spacey 12");

        for(SearchResult kw : result) {
            System.out.println(kw);
        }

        ds.close();
        assertTrue( result != null);
    }

    @Test
    public void testRead()
    {
        String id = "0715f17d-9860-4df3-876d-c7ea77d91bcf";
        DataStore ds = new DataStore();
        String result = ds.read(id);;

        if(result == null) {
            System.out.println("NOT FOUND");
        } else {
            System.out.println(result);
        }


        ds.close();
        assertTrue( result != null);
    }

    @Test
    public void testMapping()
    {
        DataStore ds = new DataStore();
         ds.initMappings();


        ds.close();

    }

    @Test
    public void createMapping()
    {
        DataStore ds = new DataStore();
        ds.createMapping();


        ds.close();

    }


}
