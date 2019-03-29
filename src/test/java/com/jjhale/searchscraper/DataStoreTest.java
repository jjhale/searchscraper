package com.jjhale.searchscraper;

import org.junit.Test;


import java.util.Arrays;
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
}
