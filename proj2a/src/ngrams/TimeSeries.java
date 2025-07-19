package ngrams;

import java.util.*;

/**
 * An object for mapping a year number (e.g. 1996) to numerical data. Provides
 * utility methods useful for data analysis.
 *
 * @author Josh Hug
 */
public class TimeSeries extends TreeMap<Integer, Double> {

    /** If it helps speed up your code, you can assume year arguments to your NGramMap
     * are between 1400 and 2100. We've stored these values as the constants
     * MIN_YEAR and MAX_YEAR here. */
    public static final int MIN_YEAR = 1400;
    public static final int MAX_YEAR = 2100;

    /**
     * Constructs a new empty TimeSeries.
     */
    public TimeSeries() {
        super();
    }

    /**
     * Creates a copy of TS, but only between STARTYEAR and ENDYEAR,
     * inclusive of both end points.
     */
    public TimeSeries(TimeSeries ts, int startYear, int endYear) {
        super();
        // DONE: Fill in this constructor.
        if (startYear < MIN_YEAR || endYear > MAX_YEAR || startYear > endYear) { // unclear how to treat; fail fast for now
            throw new IllegalArgumentException("Invalid year range: " + startYear + " to " + endYear);
        }
        for (Integer year : ts.keySet()) {
            if (year >= startYear && year <= endYear) {
                this.put(year, ts.get(year));
            }
        }
    }

    /**
     *  Returns all years for this time series in ascending order.
     */
    public List<Integer> years() {
        // DONE: Fill in this method.
        if (this.isEmpty()) {
            return List.of();
        }
        // TreeMap's keySet() returns keys in ascending order,
        // so we can safely convert it to a List.
        return List.copyOf(this.keySet()); // toStream().sorted();
    }

    /**
     *  Returns all data for this time series. Must correspond to the
     *  order of years().
     */
    public List<Double> data() {
        // DONE: Fill in this method.
        if (this.isEmpty()) {
            return List.of();
        }
        // TreeMap's values() returns values in the order of keys,
        // so we can safely convert it to a List.
        return List.copyOf(this.values());
    }

    /**
     * Returns the year-wise sum of this TimeSeries with the given TS. In other words, for
     * each year, sum the data from this TimeSeries with the data from TS. Should return a
     * new TimeSeries (does not modify this TimeSeries).
     *
     * If both TimeSeries don't contain any years, return an empty TimeSeries.
     * If one TimeSeries contains a year that the other one doesn't, the returned TimeSeries
     * should store the value from the TimeSeries that contains that year.
     */
    public TimeSeries plus(TimeSeries ts) {
        // DONE: Fill in this method.
        TimeSeries result = new TimeSeries();
        if (this.isEmpty() && ts.isEmpty()) {
            return result; // return empty TimeSeries
        } else if (this.isEmpty()) {
            return ts; // return the other TimeSeries
        } else if (ts.isEmpty()) {
            return this; // return this TimeSeries
        }

        // merge the keys of both TimeSeries
        Set <Integer> keys = new TreeSet<>();
        keys.addAll(this.keySet());
        keys.addAll(ts.keySet());

        for (Integer year : keys) {
            double thisValue = this.getOrDefault(year, 0.0);
            double tsValue = ts.getOrDefault(year, 0.0);
            result.put(year, thisValue + tsValue);
        }

        return result;
    }

    /**
     * Returns the quotient of the value for each year this TimeSeries divided by the
     * value for the same year in TS. Should return a new TimeSeries (does not modify this
     * TimeSeries).
     *
     * If TS is missing a year that exists in this TimeSeries, throw an
     * IllegalArgumentException.
     * If TS has a year that is not in this TimeSeries, ignore it.
     */
    public TimeSeries dividedBy(TimeSeries ts) {
        // DONE: Fill in this method.
        if (ts.isEmpty()) {
            throw new IllegalArgumentException("Cannot divide by an empty TimeSeries.");
        }
        TimeSeries result = new TimeSeries();
        for (Integer year : this.keySet()) {
            if (!ts.containsKey(year)) {
                throw new IllegalArgumentException("TS is missing year: " + year);
            }
            double thisValue = this.get(year);
            double tsValue = ts.get(year);
            if (tsValue == 0) {
                throw new IllegalArgumentException("Division by zero for year: " + year);
            }
            result.put(year, thisValue / tsValue);
        }
        return result;
    }

    // DONE: Add any private helper methods.
    // DONE: Remove all T0D0 comments before submitting.
}
