import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * A simple visit counter for a website.
 * This implementation is thread-safe but may have performance issues under high contention.
 * <p>
 * Functional Requirements
 * - count visit to pages
 * - get count of visits to a page
 * - pages should be identified by id
 * <p>
 * Concurrency Requirements
 * - multiple threads may call visit and getCount concurrently
 * <p>
 * Correctness Requirements
 * - visit and getCount should be atomic
 * <p>
 * Performance Requirements
 * - visit and getCount should be fast
 *
 */

class VisitCounterFirstVersion {

    private final int[] counts;

    public VisitCounterFirstVersion(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(
                    "Number of pages must be positive"
            );
        }
        counts = new int[n];
    }

    public synchronized void visit(int pageId) {
        validatePageId(pageId);
        counts[pageId]++;
    }

    public synchronized int getCount(int pageId) {
        validatePageId(pageId);
        return counts[pageId];
    }

    private void validatePageId(int pageId) {
        if (pageId < 0 || pageId >= counts.length) {
            throw new IllegalArgumentException(
                    "Invalid page ID: " + pageId
            );
        }
    }
}

class VisitCounterSecondVersion {

    private final AtomicIntegerArray counts;

    public VisitCounterSecondVersion(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(
                    "Number of pages must be positive"
            );
        }

        counts = new AtomicIntegerArray(n);
    }

    public void visit(int pageId) {
        validatePageId(pageId);
        counts.incrementAndGet(pageId);
    }

    public int getCount(int pageId) {
        validatePageId(pageId);
        return counts.get(pageId);
    }

    private void validatePageId(int pageId) {
        if (pageId < 0 || pageId >= counts.length()) {
            throw new IllegalArgumentException(
                    "Invalid page ID: " + pageId
            );
        }
    }
}

public class HitCounter {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        int threadCount = 100;
        int visitsPerThread = 1000;

        VisitCounterFirstVersion counter1 = new VisitCounterFirstVersion(3);
        VisitCounterSecondVersion counter2 = new VisitCounterSecondVersion(3);

        try (ExecutorService executor =
                     Executors.newFixedThreadPool(threadCount)) {
            Future<?>[] tasks = new Future<?>[threadCount];

            for (int i = 0; i < threadCount; i++) {
                tasks[i] = executor.submit(() -> {
                    for (int j = 0; j < visitsPerThread; j++) {
                        counter2.visit(0);
                    }
                });
            }

            for (Future<?> task : tasks) {
                task.get();
            }

            int actual = counter2.getCount(0);
            int expected = threadCount * visitsPerThread;

            System.out.println("Expected: " + expected);
            System.out.println("Actual:   " + actual);

            if (actual != expected) {
                throw new AssertionError("Count mismatch!");
            }
        }


    }

}
