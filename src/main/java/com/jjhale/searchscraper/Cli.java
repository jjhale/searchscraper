package com.jjhale.searchscraper;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Hello world!
 *
 */
public class Cli
{

    public static void main( String[] args )
    {

        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        OptionGroup optgrp = new OptionGroup();
        optgrp.addOption(Option.builder("l")
                .longOpt("list")
                .hasArg().argName("keyword").optionalArg(true)
                .type(String.class)
                .desc("List documents scraped for keyword")
                .build());
        optgrp.addOption( Option.builder("r")
                .longOpt("read")
                .hasArg().argName("doc_id")
                .type(String.class)
                .desc("Display a specific scraped document.")
                .build());
        optgrp.addOption( Option.builder("a")
                .longOpt("add")
                .type(String.class)
                .desc("Add keywords to scrape")
                .build());
        optgrp.addOption( Option.builder("s")
                .longOpt("scraper")
                .type(String.class)
                .desc("Start the scraper watcher")
                .build());



        Options options = new Options();
        options.addOptionGroup(optgrp);

        options.addOption( Option.builder("n")
                        .longOpt("search-name").hasArg()
                        .type(String.class)
                        .desc("Name of the search task for a set of keywords")
                        .build());

        options.addOption( Option.builder("k")
                .longOpt("keywords")
                .type(String.class).hasArgs()
                .desc("keywords to scrape. ")
                .build());

        options.addOption( Option.builder("t")
                .longOpt("scraper-threads")
                .type(Integer.class).valueSeparator().hasArg()
                .desc("Number of scraper threads to use.")
                .build());

        //String[] args2 = new String[]{ "--add --search-name=\"some thing\" --keywords=kw1, kw2" };
       // String[] args2 = new String[]{ "--add",  "--search-name", "some thing new", "--keywords", "kw3", "kw4"};
       // String[] args2 = new String[]{ "--scraper"};
//        String[] args2 = new String[]{ "--list"};

        int exitCode = 0;
        CommandLine line;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "searchscraper \n" +
                    "  [--add --search-name=<SearchTask> --keywords=<keyword1> <keyword2> ...]\n" +
                    "  [--list [<keyword>] ]\n" +
                    "  [--read <doc_id>]\n", options , true);
            System.exit(2);
            return;
        }

        if( line.hasOption( "add" ) ) {
            // Add Search Task mode
            if(!line.hasOption( "search-name" ) || !line.hasOption("keywords")) {
                System.out.println("must have search name and keywords when adding");
                System.exit(2);
            }
            String name = line.getOptionValue( "search-name" );
            String[] keywords = line.getOptionValues("keywords");
            System.out.println("Got keywords: "  + Arrays.toString(keywords) );

            exitCode = add(name, Arrays.asList(keywords));

        } else if( line.hasOption( "list" ) ) {
            // List Keyword mode
            DataStore ds = new DataStore();
            String keyword = line.getOptionValue( "list" );
            System.out.println("Listing with keyword = `" + keyword + "`");
            if(keyword == null) {
                List<String > keywords  =  ds.listKeywords();
                for(String kw : keywords) {
                    System.out.println(kw);
                }
                exitCode=0;
            } else {
                List<SearchResult > results  =  ds.listDocsForKeyword(keyword);
                for(SearchResult kw : results) {
                    System.out.println(kw);
                }
            }
            ds.close();

        } else if( line.hasOption( "read" ) ) {
            // Show a specific document
            String docId = line.getOptionValue( "read" );
            if(docId == null) {
                System.err.println("read option missing doc_id parameter");
                exitCode = 2;
            } else {

                DataStore ds = new DataStore();
                String result = ds.read(docId);

                if (result == null) {
                    System.err.println("NOT FOUND");
                    exitCode = 1;
                } else {
                    System.out.println(result);
                }
                ds.close();
            }
        }
        else if( line.hasOption( "scraper" ) ) {
            int numThreads = 1;
            if(line.hasOption( "scraper-threads")) {
                String threadString = line.getOptionValue("scraper-threads");
                try {
                    numThreads = Integer.parseInt(threadString);
                } catch (NumberFormatException e) {
                    System.out.println(
                            "unable to parse number of threads from `" +
                                    threadString  + "`");
                }

            }
            // Start scraper mode
            Daemon daemon = new Daemon(numThreads);
            daemon.start();
        } else {
            //  generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "searchscraper \n" +
                    "  [--add --search-name <SearchTask> --keywords <keyword1> <keyword2> ...]\n" +
                    "  [--list [<keyword>] ]\n" +
                    "  [--read <doc_id>]\n", options , true);
            exitCode = 2;
        }


        System.exit(exitCode);
    }

    protected static int add(String name, List<String> keywords) {
        DataStore ds = new DataStore();
        int exitCode =  ds.addTask(name, keywords) ? 0 : 1;
        ds.close();
        return exitCode;
    }

    protected static int list(String keyword) {
        if(keyword.equals("")) {
            // List the keywords

        } else {
            // Look up docs for that keyword and print them out.

        }
        return 0;
    }

    protected static int startDeamon(int threads) {
        new Daemon(threads);
        return 0;

    }
}
