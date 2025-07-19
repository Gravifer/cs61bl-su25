import ngrams.TimeSeries;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Unit Tests for the TimeSeries class.
 *  @author Josh Hug
 */
public class TimeSeriesTest {
    @Test
    public void testFromSpec() {
        TimeSeries catPopulation = new TimeSeries();
        catPopulation.put(1991, 0.0);
        catPopulation.put(1992, 100.0);
        catPopulation.put(1994, 200.0);

        TimeSeries dogPopulation = new TimeSeries();
        dogPopulation.put(1994, 400.0);
        dogPopulation.put(1995, 500.0);

        TimeSeries totalPopulation = catPopulation.plus(dogPopulation);
        // expected: 1991: 0,
        //           1992: 100
        //           1994: 600
        //           1995: 500

        List<Integer> expectedYears = new ArrayList<>();
        expectedYears.add(1991);
        expectedYears.add(1992);
        expectedYears.add(1994);
        expectedYears.add(1995);

        assertThat(totalPopulation.years()).isEqualTo(expectedYears);

        List<Double> expectedTotal = new ArrayList<>();
        expectedTotal.add(0.0);
        expectedTotal.add(100.0);
        expectedTotal.add(600.0);
        expectedTotal.add(500.0);

        for (int i = 0; i < expectedTotal.size(); i += 1) {
            assertThat(totalPopulation.data().get(i)).isWithin(1E-10).of(expectedTotal.get(i));
        }
    }

    @Test
    public void testEmptyBasic() {
        TimeSeries catPopulation = new TimeSeries();
        TimeSeries dogPopulation = new TimeSeries();

        assertThat(catPopulation.years()).isEmpty();
        assertThat(catPopulation.data()).isEmpty();

        TimeSeries totalPopulation = catPopulation.plus(dogPopulation);

        assertThat(totalPopulation.years()).isEmpty();
        assertThat(totalPopulation.data()).isEmpty();
    }

    @Test
    public void testConstructorWithRange() {
        TimeSeries ts = new TimeSeries();
        ts.put(1990, 1.0);
        ts.put(1995, 2.0);
        ts.put(2000, 3.0);

        TimeSeries rangeTs = new TimeSeries(ts, 1992, 1998);

        assertThat(rangeTs.years()).containsExactly(1995);
        assertThat(rangeTs.data()).containsExactly(2.0);
    }

    @Test
    public void testConstructorWithRangeEmpty() {
        TimeSeries ts = new TimeSeries();
        ts.put(1990, 1.0);
        ts.put(1995, 2.0);
        ts.put(2000, 3.0);

        TimeSeries rangeTs = new TimeSeries(ts, 2001, 2005);

        assertThat(rangeTs.years()).isEmpty();
        assertThat(rangeTs.data()).isEmpty();
    }

    @Test
    public void testPlusWithEmpty() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 1.0);

        TimeSeries ts2 = new TimeSeries();

        TimeSeries result = ts1.plus(ts2);

        assertThat(result.years()).containsExactly(2000);
        assertThat(result.data()).containsExactly(1.0);
    }

    @Test
    public void testDividedByWithEmpty() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 1.0);

        TimeSeries ts2 = new TimeSeries();

        try {
            ts1.dividedBy(ts2);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("Cannot divide by an empty TimeSeries.");
        }
    }

    @Test
    public void testDividedByWithMissingYear() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 1.0);
        ts1.put(2001, 2.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, 1.0);

        try {
            ts1.dividedBy(ts2);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("TS is missing year: 2001");
        }
    }

    @Test
    public void testDividedByWithZeroValue() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 1.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, 0.0);

        try {
            ts1.dividedBy(ts2);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("Division by zero for year: 2000");
        }
    }

    @Test
    public void testPlusWithYears() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 1.0);
        ts1.put(2001, 2.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2001, 3.0);
        ts2.put(2002, 4.0);

        TimeSeries result = ts1.plus(ts2);

        assertThat(result.years()).containsExactly(2000, 2001, 2002);
        assertThat(result.data()).containsExactly(1.0, 5.0, 4.0);
    }

    @Test
    public void testPlusWithSameYears() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 1.0);
        ts1.put(2001, 2.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, 3.0);
        ts2.put(2001, 4.0);

        TimeSeries result = ts1.plus(ts2);

        assertThat(result.years()).containsExactly(2000, 2001);
        assertThat(result.data()).containsExactly(4.0, 6.0);
    }

    @Test
    public void testPlusWithNoYears() {
        TimeSeries ts1 = new TimeSeries();
        TimeSeries ts2 = new TimeSeries();

        TimeSeries result = ts1.plus(ts2);

        assertThat(result.years()).isEmpty();
        assertThat(result.data()).isEmpty();
    }

    @Test
    public void testPlusWithNegativeValues() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, -1.0);
        ts1.put(2001, 2.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2001, -3.0);
        ts2.put(2002, 4.0);

        TimeSeries result = ts1.plus(ts2);

        assertThat(result.years()).containsExactly(2000, 2001, 2002);
        assertThat(result.data()).containsExactly(-1.0, -1.0, 4.0);
    }

    @Test
    public void testDividedByWithSameYears() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 10.0);
        ts1.put(2001, 20.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, 2.0);
        ts2.put(2001, 4.0);

        TimeSeries result = ts1.dividedBy(ts2);

        assertThat(result.years()).containsExactly(2000, 2001);
        assertThat(result.data()).containsExactly(5.0, 5.0);
    }

    @Test
    public void testDividedByWithNegativeValues() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, -10.0);
        ts1.put(2001, 20.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, -2.0);
        ts2.put(2001, 4.0);

        TimeSeries result = ts1.dividedBy(ts2);

        assertThat(result.years()).containsExactly(2000, 2001);
        assertThat(result.data()).containsExactly(5.0, 5.0);
    }

    @Test
    public void testDividedByWithZeroValues() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, 10.0);
        ts1.put(2001, 20.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, 0.0);
        ts2.put(2001, 4.0);

        try {
            ts1.dividedBy(ts2);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageThat().contains("Division by zero for year: 2000");
        }
    }

    @Test
    public void testDividedByWithNegativeYears() {
        TimeSeries ts1 = new TimeSeries();
        ts1.put(2000, -10.0);
        ts1.put(2001, 20.0);

        TimeSeries ts2 = new TimeSeries();
        ts2.put(2000, -2.0);
        ts2.put(2001, 4.0);

        TimeSeries result = ts1.dividedBy(ts2);

        assertThat(result.years()).containsExactly(2000, 2001);
        assertThat(result.data()).containsExactly(5.0, 5.0);
    }
} 
