import java.util.*;

/**
 * This class solves the problem of maximizing a tech startup's capital by
 * selecting at most 'k' projects optimally based on project investments and revenues.
 */
public class TechStartupCapitalOptimizer {

    /**
     * Calculates the maximum capital achievable after completing up to 'k' projects.
     *
     * @param k The maximum number of projects that can be selected (non-negative)
     * @param c The initial capital (non-negative)
     * @param investments Array of minimum investments required for each project (non-null, length > 0)
     * @param revenues Array of revenue gains from each project (non-null, same length as investments)
     * @return The maximized capital after completing up to k projects.
     * @throws IllegalArgumentException if input constraints are violated
     */
    public static int findMaximizedCapital(int k, int c, int[] investments, int[] revenues) {
        if (k < 0 || c < 0) {
            throw new IllegalArgumentException("Number of projects and capital must be non-negative.");
        }
        if (investments == null || revenues == null || investments.length != revenues.length) {
            throw new IllegalArgumentException("Investments and revenues must be non-null and of the same length.");
        }

        int n = investments.length;
        // Create list of projects as (investment, revenue) pairs
        List<int[]> projects = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            // Defensive check: Ignore negative investments or revenues for safety (optional)
            if (investments[i] < 0 || revenues[i] < 0) {
                throw new IllegalArgumentException("Investments and revenues cannot be negative.");
            }
            projects.add(new int[]{investments[i], revenues[i]});
        }

        // Sort projects by increasing investment for efficient processing
        projects.sort(Comparator.comparingInt(a -> a[0]));

        // Max-heap for revenues of affordable projects
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

        int currentCapital = c;
        int index = 0;

        for (int count = 0; count < k; count++) {
            // Add all newly affordable projects to max-heap
            while (index < n && projects.get(index)[0] <= currentCapital) {
                maxHeap.offer(projects.get(index)[1]);
                index++;
            }

            // If none affordable, we cannot proceed further
            if (maxHeap.isEmpty()) {
                break;
            }

            // Select the project with max revenue to maximize capital
            currentCapital += maxHeap.poll();
        }

        return currentCapital;
    }

    /**
     * Main method to test the implementation with assignment examples
     * and additional edge cases.
     */
    public static void main(String[] args) {
        // Assignment Example 1
        int k1 = 2;
        int c1 = 0;
        int[] investments1 = {0, 2, 3};
        int[] revenues1 = {2, 5, 8};
        int result1 = findMaximizedCapital(k1, c1, investments1, revenues1);
        System.out.println("Example 1 Output: " + result1);  // Expected: 7

        // Assignment Example 2
        int k2 = 3;
        int c2 = 1;
        int[] investments2 = {1, 3, 5};
        int[] revenues2 = {3, 6, 10};
        int result2 = findMaximizedCapital(k2, c2, investments2, revenues2);
        System.out.println("Example 2 Output: " + result2);  // Expected: 20

        // Edge Case 1: k=0 → no projects selected
        int k3 = 0;
        int c3 = 100;
        int[] investments3 = {10, 20};
        int[] revenues3 = {100, 200};
        int result3 = findMaximizedCapital(k3, c3, investments3, revenues3);
        System.out.println("Edge Case k=0 Output: " + result3);  // Expected: 100

        // Edge Case 2: No affordable projects
        int k4 = 3;
        int c4 = 1;
        int[] investments4 = {10, 20};
        int[] revenues4 = {50, 100};
        int result4 = findMaximizedCapital(k4, c4, investments4, revenues4);
        System.out.println("Edge Case No Affordable Projects Output: " + result4);  // Expected: 1

        // Additional Case 1: High initial capital, only top k selected
        int k5 = 2;
        int c5 = 10;
        int[] investments5 = {1, 2, 3};
        int[] revenues5 = {10, 20, 30};
        int result5 = findMaximizedCapital(k5, c5, investments5, revenues5);
        System.out.println("Additional Case 1 Output: " + result5);  // Expected: 60

        // Additional Case 2: Need to reinvest to unlock expensive projects
        int k6 = 2;
        int c6 = 1;
        int[] investments6 = {1, 5};
        int[] revenues6 = {5, 50};
        int result6 = findMaximizedCapital(k6, c6, investments6, revenues6);
        System.out.println("Additional Case 2 Output: " + result6);  // Expected: 56
    }
}
