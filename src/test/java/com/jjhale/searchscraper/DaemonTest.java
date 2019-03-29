package com.jjhale.searchscraper;

import org.junit.Test;

public class DaemonTest {

    @Test
    public void testStartIt() {
        Daemon daemon = new Daemon(1);
        daemon.start();
    }

    @Test
    public void testGetUrl() {
        Daemon daemon = new Daemon(1);
        //daemon.start();
        daemon.downloadUrlAndStore("made up",
                "spacey example",
                "https://www.google.com");

    }
}
