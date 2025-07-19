package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import ngrams.NGramMap;
import ngrams.TimeSeries;

import java.util.List;

/**
 * A handler for processing Ngordnet queries related to the history text.
 * This class extends NgordnetQueryHandler and provides a method to handle
 * queries by returning a formatted string with the query details.
 *
 * @implSpec The displayed info should be of the following format:
 * <pre>{@code
 *      cat: {2000=1.71568475416827E-5, 2001=1.6120939684412677E-5, 2002=1.61742010630623E-5, ..., 2019=2.4581322086049953E-5}
 *      dog: {2000=3.127517699525712E-5, 2001=2.99511426723737E-5, 2002=3.0283458650225453E-5, ..., 2019=5.5807040409297486E-5}
 * }</pre>
 */
public class HistoryTextHandler extends NgordnetQueryHandler {
    private final NGramMap ngramMap;
    public HistoryTextHandler(NGramMap map) {
        super();
        if (map == null) {
            throw new NullPointerException("Must supply a non-null NGramMap.");
        }
        this.ngramMap = map;
    }
    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        int startYear = q.startYear();
        int endYear = q.endYear();

        // String response = "You entered the following info into the browser:\n";
        // response += "Words: " + q.words() + "\n";
        // response += "Start Year: " + q.startYear() + "\n";
        // response += "End Year: " + q.endYear() + "\n";
        StringBuilder response = new StringBuilder();
        for (String word : words) {
            // Get the count history for the word in the specified year range
            TimeSeries history = ngramMap.weightHistory(word, startYear, endYear);
            if (!history.isEmpty()) {
                response.append(word).append(": ").append(history).append("\n");
            } else {
                response.append(word).append(": No data available for this word in the specified year range.\n");
            }
        }
        return response.toString();
    }
}
