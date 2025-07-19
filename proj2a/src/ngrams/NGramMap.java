package ngrams;

import edu.princeton.cs.algs4.In;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ngrams.TimeSeries.MAX_YEAR;
import static ngrams.TimeSeries.MIN_YEAR;
import static utils.Utils.*;

/**
 * An object that provides utility methods for making queries on the
 * Google NGrams dataset (or a subset thereof).
 *
 * An NGramMap stores pertinent data from a "words file" and a "counts
 * file". It is not a map in the strict sense, but it does provide additional
 * functionality.
 *
 * @author Josh Hug
 */
public class NGramMap {
    // DONE: Add any necessary static/instance variables.

    /** The class wordHistory is a TimeSeries that stores the history of a single word. */
    private class WordHistory extends TimeSeries {
        /** Copy constructor */
        WordHistory() {
            super();
        }

        private WordHistory(WordHistory wh) {
            super();
            this.putAll(wh);
        }
    }
    private final HashMap<String, WordHistory> data;
    private final TimeSeries dataCounts;
    private final TimeSeries totalCounts;

    /**
     * Constructs an NGramMap from WORDSFILENAME and COUNTSFILENAME.
     */
    public NGramMap(String wordsFilename, String countsFilename) {
        // DONE: Fill in this constructor. See the "NGramMap Tips" section of the spec for help.
        if (wordsFilename == null || countsFilename == null) {
            throw new IllegalArgumentException("Filenames cannot be null.");
        }
        In wf = new In(wordsFilename);
        In cf = new In(countsFilename);
        // check that files exist, are non-empty and readable
        if (!wf.exists() || !cf.exists() || wf.isEmpty() || cf.isEmpty()) {
            System.err.println("Error: Files not found.");
            System.err.println("Working dir: " + System.getProperty("user.dir"));
            System.err.println("Words file: " + wordsFilename);
            System.err.println("Counts file: " + countsFilename);
            throw new IllegalArgumentException("Files do not exist or are empty.");
        }
        data = new HashMap<>();
        dataCounts = new TimeSeries();
        totalCounts = new TimeSeries();

        while (wf.hasNextLine()) { // Read the words file
            String line = wf.readLine();
            String[] parts = line.split("\t");
            if (parts.length < 3) {
                continue; // skip malformed lines
            }
            String word = parts[0];
            int year = Integer.parseInt(parts[1]);
            double count = Double.parseDouble(parts[2]);
            if (year < MIN_YEAR || year > MAX_YEAR) {
                continue; // skip out-of-range years
            }

            data.putIfAbsent(word, new WordHistory());
            data.get(word).put(year, count);
            // // dataCounts.put(year, dataCounts.getOrDefault(year, 0.0) + count); // ? not what asked by the course; we read total counts separately
        }

        while (cf.hasNextLine()) { // Read the counts file
            String line = cf.readLine();
            String[] parts = line.split(",");
            if (parts.length < 2) {
                continue; // skip malformed lines
            }
            int year = Integer.parseInt(parts[0]);
            double totalCount = Double.parseDouble(parts[1]);
            if (year < MIN_YEAR || year > MAX_YEAR) {
                continue; // skip out-of-range years
            }
            totalCounts.put(year, totalCount);
        }
    }

    /**
     * Provides the history of WORD between STARTYEAR and ENDYEAR, inclusive of both ends. The
     * returned TimeSeries should be a copy, not a link to this NGramMap's TimeSeries. In other
     * words, changes made to the object returned by this function should not also affect the
     * NGramMap. This is also known as a "defensive copy". If the word is not in the data files,
     * returns an empty TimeSeries.
     */
    public TimeSeries countHistory(String word, int startYear, int endYear) {
        // DONE: Fill in this method.
        if (startYear < MIN_YEAR || endYear > MAX_YEAR || startYear > endYear) {
            throw new IllegalArgumentException("Invalid year range: " + startYear + " to " + endYear);
        }
        if (data.containsKey(word)) {
            WordHistory history = data.get(word);
            return history.filterByYearRange(startYear, endYear); // cast made
        }
        return new WordHistory();
    }

    /**
     * Provides the history of WORD. The returned TimeSeries should be a copy, not a link to this
     * NGramMap's TimeSeries. In other words, changes made to the object returned by this function
     * should not also affect the NGramMap. This is also known as a "defensive copy". If the word
     * is not in the data files, returns an empty TimeSeries.
     */
    public TimeSeries countHistory(String word) {
        // DONE: Fill in this method.
        if (data.containsKey(word)) {
            return data.get(word).copy();
        }
        return new WordHistory();
    }

    /**
     * Returns a defensive copy of the total number of words recorded per year in all volumes.
     */
    public TimeSeries totalCountHistory() {
        // DONE: Fill in this method.
        TimeSeries targetCounts = totalCounts;
        if (targetCounts != null) {
            return targetCounts.copy(); // defensive copy
        }
        return new TimeSeries();
    }

    /**
     * Provides a TimeSeries containing the relative frequency per year of WORD between STARTYEAR
     * and ENDYEAR, inclusive of both ends. If the word is not in the data files, returns an empty
     * TimeSeries.
     */
    public TimeSeries weightHistory(String word, int startYear, int endYear) {
        // DONE: Fill in this method.
        if (startYear < MIN_YEAR || endYear > MAX_YEAR || startYear > endYear) {
            throw new IllegalArgumentException("Invalid year range: " + startYear + " to " + endYear);
        }
        if (data.containsKey(word)) {
            // TimeSeries countHistory = data.get(word).filterByYearRange(startYear, endYear);
            // TimeSeries totalCounts = this.totalCountHistory().filterByYearRange(startYear, endYear);
            // TimeSeries weightHistory = new TimeSeries();
            // for (Integer year : countHistory.years()) {
            //     double count = countHistory.get(year);
            //     double totalCount = totalCounts.get(year);
            //     if (totalCount > 0) {
            //         weightHistory.put(year, count / totalCount);
            //     } else {
            //         weightHistory.put(year, 0.0);
            //     }
            // }
            // return weightHistory;
            return data.get(word).filterByYearRange(startYear, endYear)
                    .dividedBy(this.totalCountHistory().filterByYearRange(startYear, endYear));
        }
        return new TimeSeries();
    }

    /**
     * Provides a TimeSeries containing the relative frequency per year of WORD compared to all
     * words recorded in that year. If the word is not in the data files, returns an empty
     * TimeSeries.
     */
    public TimeSeries weightHistory(String word) {
        // DONE: Fill in this method.
        if (data.containsKey(word)) {
            return data.get(word).dividedBy(data.get(word));
        }
        return new TimeSeries();
    }

    /**
     * Provides the summed relative frequency per year of all words in WORDS between STARTYEAR and
     * ENDYEAR, inclusive of both ends. If a word does not exist in this time frame, ignore it
     * rather than throwing an exception.
     */
    public TimeSeries summedWeightHistory(Collection<String> words,
                                          int startYear, int endYear) {
        // DONE: Fill in this method.
        if (startYear < MIN_YEAR || endYear > MAX_YEAR || startYear > endYear) {
            throw new IllegalArgumentException("Invalid year range: " + startYear + " to " + endYear);
        }
        TimeSeries summedWeights = new TimeSeries();
        for (String word : words) {
            TimeSeries weightHistory = weightHistory(word, startYear, endYear);
            for (Integer year : weightHistory.years()) {
                double weight = weightHistory.get(year);
                summedWeights.put(year, summedWeights.getOrDefault(year, 0.0) + weight);
            }
        }
        return summedWeights;
    }

    /**
     * Returns the summed relative frequency per year of all words in WORDS. If a word does not
     * exist in this time frame, ignore it rather than throwing an exception.
     */
    public TimeSeries summedWeightHistory(Collection<String> words) {
        // DONE: Fill in this method.
        TimeSeries summedWeights = new TimeSeries();
        for (String word : words) {
            TimeSeries weightHistory = weightHistory(word);
            for (Integer year : weightHistory.years()) {
                double weight = weightHistory.get(year);
                summedWeights.put(year, summedWeights.getOrDefault(year, 0.0) + weight);
            }
        }
        return summedWeights;
    }

    // TODO: Add any private helper methods.
    // TODO: Remove all T0D0 comments before submitting.
}
