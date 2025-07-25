import java.util.*;

/**
 * This class provides a solution to maximize a tech startup's capital by selecting
 * at most 'k' distinct projects based on available capital and project requirements.
 * The algorithm uses a greedy approach with a max-heap to always select the highest-revenue
 * project that is currently affordable.
 */
public class TechStartupCapitalOptimizer {

    /**
     * Calculates the maximum capital achievable by selecting up to 'k' projects.
     * 
     * @param k The maximum number of projects that can be selected
     * @param c The initial capital available to the startup
     * @param investments Array of minimum investment required for each project
     * @param revenues Array of revenue gains from completing each project
     * @return The maximized capital after completing up to 'k' projects
     */
    public static int findMaximizedCapital(int k, int c, int[] investments, int[] revenues) {
        int n = investments.length;
        // Create a list to store projects as (investment, revenue) pairs
        List<int[]> projects = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            projects.add(new int[]{investments[i], revenues[i]});
        }
        
        // Sort projects by their investment requirement (ascending order)
        // This allows us to efficiently find affordable projects as capital increases
        Collections.sort(projects, (a, b) -> Integer.compare(a[0], b[0]));
        
        // Max-heap implementation using PriorityQueue (reverse natural ordering)
        // This will allow efficient retrieval of the highest-revenue project
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((x, y) -> Integer.compare(y, x));
        
        int index = 0;          // Pointer to track position in sorted projects list
        int currentCapital = c;  // Track current available capital
        
        // Attempt to select up to 'k' projects
        for (int projectCount = 0; projectCount < k; projectCount++) {
            // Add all newly affordable projects to the max-heap
            // Since projects are sorted by investment, we can efficiently find
            // all projects that have become affordable with current capital
            while (index < n && projects.get(index)[0] <= currentCapital) {
                // Add project revenue to the max-heap
                maxHeap.offer(projects.get(index)[1]);
                index++;
            }
            
            // If no projects are affordable, exit early
            if (maxHeap.isEmpty()) {
                break;
            }
            
            // Select the project with the highest revenue from affordable projects
            // This greedy choice maximizes immediate capital growth
            currentCapital += maxHeap.poll();
        }
        
        return currentCapital;
    }

    /**
     * Test driver with multiple examples including edge cases and new scenarios
     */
    public static void main(String[] args) {
        // Example 1: From problem statement (expected output: 7)
        int k1 = 2, c1 = 0;
        int[] revenues1 = {2, 5, 8};
        int[] investments1 = {0, 2, 3};
        System.out.println("Example 1: " + findMaximizedCapital(k1, c1, investments1, revenues1));
        
        // Example 2: From problem statement (expected output: 20)
        int k2 = 3, c2 = 1;
        int[] revenues2 = {3, 6, 10};
        int[] investments2 = {1, 3, 5};
        System.out.println("Example 2: " + findMaximizedCapital(k2, c2, investments2, revenues2));
        
        // Example 3: Single high-value project selection
        // k=1, c=10 → can choose Project 1 (investment 10 → revenue 20) → 10 + 20 = 30
        int k3 = 1, c3 = 10;
        int[] revenues3 = {10, 20, 30};
        int[] investments3 = {5, 10, 15};
        System.out.println("Example 3: " + findMaximizedCapital(k3, c3, investments3, revenues3));
        
        // Example 4: Sequential selection with full reinvestment
        // k=2, c=5 → 
        //   First: Project 3 (investment 4 → revenue 5) → 5+5=10
        //   Second: Project 2 (investment 3 → revenue 4) → 10+4=14
        int k4 = 2, c4 = 5;
        int[] revenues4 = {2, 3, 4, 5};
        int[] investments4 = {1, 2, 3, 4};
        System.out.println("Example 4: " + findMaximizedCapital(k4, c4, investments4, revenues4));
        
        // Edge Case 1: Zero projects selected (k=0)
        System.out.println("Edge Case (k=0): " + 
            findMaximizedCapital(0, 100, new int[]{50}, new int[]{200}));
        
        // Edge Case 2: No affordable projects
        System.out.println("No Affordable Projects: " + 
            findMaximizedCapital(3, 1, new int[]{10, 20}, new int[]{50, 100}));
        
        // Edge Case 3: More projects available than needed (k=2 with 3 affordable projects)
        System.out.println("More Projects Than k: " + 
            findMaximizedCapital(2, 5, new int[]{1, 2, 3}, new int[]{10, 20, 30}));
    }
}