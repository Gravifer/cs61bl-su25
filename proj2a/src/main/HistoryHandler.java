package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import ngrams.NGramMap;
import ngrams.TimeSeries;
import org.knowm.xchart.XYChart;
import plotting.Plotter;

import java.util.ArrayList;
import java.util.List;


public class HistoryHandler extends NgordnetQueryHandler {
    private final NGramMap ngramMap;
    public HistoryHandler(NGramMap map) {
        super();
        if (map == null) {
            throw new NullPointerException("Must supply a non-null NGramMap.");
        }
        this.ngramMap = map;
    }
    @Override
    public String handle(NgordnetQuery q) {
        // System.out.println("Got query that looks like:");
        // System.out.println("Words: " + q.words());
        // System.out.println("Start Year: " + q.startYear());
        // System.out.println("End Year: " + q.endYear());
        List<String> words = q.words();
        int startYear = q.startYear();
        int endYear = q.endYear();

        ArrayList<TimeSeries> lts = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (String word : words) {
            TimeSeries history = ngramMap.weightHistory(word, startYear, endYear);
            lts.add(history);
            labels.add(word);
        }

        XYChart chart = Plotter.generateTimeSeriesChart(labels, lts);
        // Plotter.displayChart(chart); // display locally

        return Plotter.encodeChartAsString(chart);
    }
}
