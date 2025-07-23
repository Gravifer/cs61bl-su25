import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

public class UnionFindConcurrentTest {
    @Test
    public void testConcurrentUnionAndFind() throws InterruptedException {
        final int N = 1000;
        final UnionFind uf = new UnionFind(N);
        final int threads = 10;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threads);
        final AtomicBoolean failed = new AtomicBoolean(false);

        // Each thread unions a range of elements
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = tid * (N / threads); i < (tid + 1) * (N / threads) - 1; i++) {
                        uf.union(i, i + 1);
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        startLatch.countDown();
        doneLatch.await();
        assertFalse(failed.get(), "Exception occurred in threads");
        // All elements in the same thread's range should be connected
        for (int t = 0; t < threads; t++) {
            int base = t * (N / threads);
            int root = uf.find(base);
            for (int i = base + 1; i < (t + 1) * (N / threads); i++) {
                assertEquals(root, uf.find(i), "Elements in same range should be connected");
            }
        }
    }

    @Test
    public void testConcurrentFind() throws InterruptedException {
        final int N = 100;
        final UnionFind uf = new UnionFind(N);
        for (int i = 0; i < N - 1; i++) {
            uf.union(i, i + 1);
        }
        final int threads = 20;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threads);
        final AtomicBoolean failed = new AtomicBoolean(false);
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < N; i++) {
                        int root = uf.find(i);
                        assertEquals(root, uf.find(0), "All elements should have same root");
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        startLatch.countDown();
        doneLatch.await();
        assertFalse(failed.get(), "Exception occurred in threads");
    }

    @Test
    public void testHighlyConcurrentOverlappingUnionsAndFinds() throws InterruptedException {
        final int N = 1_000_000;
        final int threads = 1000;
        final UnionFind uf = new UnionFind(N);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threads);
        final AtomicBoolean failed = new AtomicBoolean(false);

        // Each thread will union and find in overlapping ranges
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    int start = Math.max(0, tid * (N / threads) - 10_000);
                    int end = Math.min(N, (tid + 1) * (N / threads) + 10_000);
                    // Interleaved unions
                    for (int i = start; i < end - 1; i += 2) {
                        uf.union(i, (i + tid) % N);
                    }
                    // Interleaved finds
                    for (int i = start; i < end; i += 3) {
                        uf.find(i);
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        startLatch.countDown();
        doneLatch.await();
        assertFalse(failed.get(), "Exception occurred in threads");
        // Spot check: endpoints should be connected by overlapping unions
        assertEquals(uf.find(0), uf.find(N - 1), "First and last should be connected");
    }
}
