/**
 * WeatherAnomalyDetection
 * ------------------------
 * This class finds and counts all "anomaly periods" in a sequence of temperature changes.
 *
 * Definition of an anomaly period:
 *   It must be a continuous subarray of at least 3 days.
 *   The total sum of temperature changes in that subarray must lie within the given inclusive range [low_threshold, high_threshold].
 */
public class WeatherAnomalyDetection {

    /**
     * Counts the number of anomaly periods that satisfy the constraints.
     *
     * @param temperatures   an array of daily temperature changes
     * @param low_threshold  minimum acceptable sum for an anomaly period
     * @param high_threshold maximum acceptable sum for an anomaly period
     * @return the total count of valid anomaly periods
     */
    public static int countAnomalyPeriods(int[] temperatures, int low_threshold, int high_threshold) {
        int n = temperatures.length;   // Total number of temperature readings
        int count = 0;                 // Tracks how many valid anomaly periods are found

        // Outer loop: choose starting day of subarray
        for (int i = 0; i < n; i++) {
            int currentSum = 0; // Running sum for subarray starting at index i

            // Inner loop: extend subarray to include day j
            for (int j = i; j < n; j++) {
                currentSum += temperatures[j];     // Add current day's temperature change
                int length = j - i + 1;            // Calculate the length of subarray

                // Condition for anomaly period:
                //  - Must have at least 3 elements
                //  - Sum must be between low_threshold and high_threshold (inclusive)
                if (length >= 3 && currentSum >= low_threshold && currentSum <= high_threshold) {
                    count++;  // Found a valid anomaly period
                }
            }
        }
        return count; // Return total valid anomaly periods found
    }

    public static void main(String[] args) {
        // ===============================
        // Example 1
        // Array: [3, -1, -4, 6, 2]
        // Valid range: [2, 5]
        // Expected result: 3
        // ===============================
        int[] changes1 = {3, -1, -4, 6, 2};
        int low1 = 2, high1 = 5;
        System.out.println("Example 1 (Expected: 3) => Actual: " +
                countAnomalyPeriods(changes1, low1, high1));

        // ===============================
        // Example 2
        // Array: [-2, 3, 1, -5, 4]
        // Valid range: [-1, 2]
        // Expected result: 4
        // ===============================
        int[] changes2 = {-2, 3, 1, -5, 4};
        int low2 = -1, high2 = 2;
        System.out.println("Example 2 (Expected: 4) => Actual: " +
                countAnomalyPeriods(changes2, low2, high2));

        // ===============================
        // Case 3
        // Array: [1, 2, 3]
        // Valid range: [3, 6]
        // Only one subarray of length >=3 → sum = 6 (valid)
        // Expected: 1
        // ===============================
        System.out.println("Case 3 (Expected: 1) => Actual: " +
                countAnomalyPeriods(new int[]{1, 2, 3}, 3, 6));

        // ===============================
        // Case 4
        // Array: [10, 20, 30]
        // Valid range: [10, 20]
        // All sums exceed the range → no valid anomaly period
        // Expected: 0
        // ===============================
        System.out.println("Case 4 (Expected: 0) => Actual: " +
                countAnomalyPeriods(new int[]{10, 20, 30}, 10, 20));

        // ===============================
        // Case 5
        // Array: [1, -1, 1, -1, 1]
        // Valid range: [-1, 1]
        // Alternating small sums create multiple valid subarrays
        // Expected: 6
        // ===============================
        System.out.println("Case 5 (Expected: 6) => Actual: " +
                countAnomalyPeriods(new int[]{1, -1, 1, -1, 1}, -1, 1));

        // ===============================
        // Case 6
        // Array: [0, 0, 0, 0, 0]
        // Valid range: [0, 0]
        // Many subarrays are zero sum, only those with length >= 3 count
        // Expected: 6
        // ===============================
        System.out.println("Case 6 (Expected: 6) => Actual: " +
                countAnomalyPeriods(new int[]{0, 0, 0, 0, 0}, 0, 0));

        // ===============================
        // Case 7 (NEW)
        // Array: [-1, -2, -3, -4]
        // Valid range: [-10, -5]
        // Subarrays:
        //  [-1,-2,-3] sum=-6
        //  [-2,-3,-4] sum=-9
        //  [-1,-2,-3,-4] sum=-10
        // Expected: 3
        // ===============================
        System.out.println("Case 7 (Expected: 3) => Actual: " +
                countAnomalyPeriods(new int[]{-1, -2, -3, -4}, -10, -5));

        // ===============================
        // Case 8 (NEW)
        // Array: [1, 2, 3, 4]
        // Valid range: [0, 100] → everything qualifies if length >=3
        // Valid subarrays:
        //   [1,2,3] 
        //   [2,3,4] 
        //   [1,2,3,4] 
        // Expected: 3
        // ===============================
        System.out.println("Case 8 (Expected: 3) => Actual: " +
                countAnomalyPeriods(new int[]{1, 2, 3, 4}, 0, 100));
    }
}
