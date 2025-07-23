package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;

public class HyponymsHandler extends NgordnetQueryHandler {

    /**
     * @param q the query to handle
     * @return a string to display in the browser
     */
    @Override
    public String handle(NgordnetQuery q) {
        return "Hello";
    }
}
