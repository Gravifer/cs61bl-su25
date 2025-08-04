import edu.princeton.cs.algs4.Stopwatch;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A class to run timing tests on the sorting algorithms, and plot the results.
 * You do not need to modify this class or worry about how it works.
 * Simply run the main method after you have implemented the sorting algorithms.
 *
 * Note this class might take a while to run, depending on the speed of your computer.
 */

public class TimingTests {

    /* The upper and lower bounds of the range of array sizes to test. */
    // * NOTE: You may want to change these values if the tests are taking too long.
    static final int UPPER_BOUND = 200000;
    static final int LOWER_BOUND = 20000;
    static final int STEP_SIZE = 20000;

    /* The seed used to generate random numbers. Ensures that every algorithm sorts the same set of numbers.*/
    static final long SEED = 61;

    public static void main(String[] args) {
        List<XYChart> charts = new ArrayList<>();

        /* Run the timing tests and store the results. */
        // * NOTE: You can comment out the tests you don't want to run.
        charts.add(timeHeapSort());
        charts.add(timeMergeSort());
        charts.add(timeInsertionSort());
        charts.add(timeSelectionSort());
        charts.add(timeQuickSort());

        /* Display the charts in a window. */
        var sw = new SwingWrapper<>(charts);
        sw.displayChartMatrix();
        // 保存所有图表为一个网格到单个PNG文件
        try {
            // 计算网格行列（近似为正方形）
            int chartCount = charts.size();
            int cols = (int) Math.ceil(Math.sqrt(chartCount));
            int rows = (int) Math.ceil((double) chartCount / cols);
            int chartWidth = 600;
            int chartHeight = 400;
            java.awt.image.BufferedImage combined = new java.awt.image.BufferedImage(
                    cols * chartWidth, rows * chartHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = combined.createGraphics();
            for (int i = 0; i < chartCount; i++) {
                int r = i / cols;
                int c = i % cols;
                // 使用BitmapEncoder.getBufferedImage获取图像
                java.awt.image.BufferedImage chartImg = org.knowm.xchart.BitmapEncoder.getBufferedImage(charts.get(i));
                // 缩放到目标尺寸
                java.awt.Image scaled = chartImg.getScaledInstance(chartWidth, chartHeight, java.awt.Image.SCALE_SMOOTH);
                g.drawImage(scaled, c * chartWidth, r * chartHeight, null);
            }
            g.dispose();
            javax.imageio.ImageIO.write(combined, "png", new java.io.File("all_charts_grid.png"));
            System.out.println("All charts saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static XYChart timeHeapSort(){
        List<Integer> Ns = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        Random RANDOM = new Random(SEED);


        for (int N = LOWER_BOUND; N <= UPPER_BOUND; N+=STEP_SIZE) {
            Ns.add(N);
            int [] arr = RANDOM.ints(N).toArray();
            Stopwatch sw = new Stopwatch();
            HeapSort.sort(arr);
            times.add(sw.elapsedTime());
        }

        TimingData td = new TimingData(Ns, times);
        String title = "Heap Sort";
        printTimingTable(td, title);

        XYChart chart = QuickChart.getChart(title, "N", "Time to Sort (s)", "Time", td.getNs(), td.getTimes());
        return chart;
    }

    public static XYChart timeInsertionSort() {
        List<Integer> Ns = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        Random RANDOM = new Random(SEED);

        for (int N = LOWER_BOUND; N <= UPPER_BOUND; N += STEP_SIZE) {
            Ns.add(N);
            int[] arr = RANDOM.ints(N).toArray();
            Stopwatch sw = new Stopwatch();
            InsertionSort.sort(arr);
            times.add(sw.elapsedTime());
        }

        TimingData td = new TimingData(Ns, times);
        String title = "Insertion Sort";
        printTimingTable(td, title);

        XYChart chart = QuickChart.getChart(title, "N", "Time to Sort (s)", "Time", td.getNs(), td.getTimes());
        return chart;
    }

    public static XYChart timeSelectionSort() {
        List<Integer> Ns = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        Random RANDOM = new Random(SEED);

        for (int N = LOWER_BOUND; N <= UPPER_BOUND; N += STEP_SIZE) {
            Ns.add(N);
            int[] arr = RANDOM.ints(N).toArray();
            Stopwatch sw = new Stopwatch();
            SelectionSort.sort(arr);
            times.add(sw.elapsedTime());
        }

        TimingData td = new TimingData(Ns, times);
        String title = "Selection Sort";
        printTimingTable(td, title);

        XYChart chart = QuickChart.getChart(title, "N", "Time to Sort (s)", "Time", td.getNs(), td.getTimes());
        return chart;
    }


    public static XYChart timeMergeSort(){
        List<Integer> Ns = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        Random RANDOM = new Random(SEED);


        for (int N = LOWER_BOUND; N <= UPPER_BOUND; N+=STEP_SIZE) {
            Ns.add(N);
            int [] arr = RANDOM.ints(N).toArray();
            Stopwatch sw = new Stopwatch();
            int [] res = MergeSort.sort(arr);
            times.add(sw.elapsedTime());
        }

        TimingData td = new TimingData(Ns, times);
        String title = "Merge Sort";
        printTimingTable(td, title);

        XYChart chart = QuickChart.getChart(title, "N", "Time to Sort (s)", "Time", td.getNs(), td.getTimes());
        return chart;
    }

    public static XYChart timeQuickSort(){
        List<Integer> Ns = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        Random RANDOM = new Random(SEED);


        for (int N = LOWER_BOUND; N <= UPPER_BOUND; N+=STEP_SIZE) {
            Ns.add(N);
            int [] arr = RANDOM.ints(N).toArray();
            Stopwatch sw = new Stopwatch();
            int [] res = QuickSort.sort(arr);
            times.add(sw.elapsedTime());
        }

        TimingData td = new TimingData(Ns, times);
        String title = "QuickSort";
        printTimingTable(td, title);

        XYChart chart = QuickChart.getChart(title, "N", "Time to Sort (s)", "Time", td.getNs(), td.getTimes());
        return chart;
    }

    private static void printTimingTable(TimingData data, String title) {
        System.out.println(title+":\n");

        System.out.printf("%12s %12s\n", "N", "time (s)");
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < data.getNs().size(); i += 1) {
            int N = data.getNs().get(i);
            double time = data.getTimes().get(i);
            System.out.printf("%12d %12.2f\n", N, time);
        }
        System.out.println();
        System.out.println();
    }


}
