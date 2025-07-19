package main;

import static utils.Utils.*;

import ngrams.NGramMap;
import org.slf4j.LoggerFactory;

import browser.NgordnetServer;

public class Main {
    static {
        LoggerFactory.getLogger(Main.class).info("\033[1;38mChanging text color to white");
    }
    /* Do not delete or modify the code above! */

    public static void main(String[] args) {
        NgordnetServer hns = new NgordnetServer();

        long startTime = System.currentTimeMillis();

        /* The following code might be useful to you.
         */
        NGramMap ngm = new NGramMap(TOP_14337_WORDS_FILE, TOTAL_COUNTS_FILE);

        hns.startUp();
        System.out.println("Registering handlers...");
        hns.register("history", new HistoryHandler(ngm));
        hns.register("historytext", new HistoryTextHandler(ngm));

        System.out.println("Finished server startup! Visit http://localhost:4567/ngordnet_2a.html");
        long endTime = System.currentTimeMillis();
        System.out.println("Server startup took " + (endTime - startTime) + " milliseconds.");
    }
}
