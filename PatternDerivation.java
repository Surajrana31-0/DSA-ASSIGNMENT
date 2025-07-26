import java.util.*;

/**
 * PatternDerivation
 * -----------------
 * This program calculates how many times a smaller pattern `p2` can be
 * matched inside a repeated sequence of `p1` (repeated `t1` times).
 *
 * Think of it as:
 *   - We have a base pattern p1 (like "abc")
 *   - We repeat it t1 times, forming a big string
 *   - We want to count how many times p2 appears in sequence
 *
 * This is done efficiently without building the entire huge string.
 */
public class PatternDerivation {

    /**
     * Computes the maximum number of times pattern `p2` appears
     * within t1 repetitions of pattern `p1`.
     *
     * @param p1 Base pattern to repeat
     * @param t1 How many times p1 is repeated
     * @param p2 Pattern we want to count
     * @return Number of times p2 can be formed in the repeated sequence
     */
    public static int maxOccurrences(String p1, int t1, String p2) {
        int n1 = p1.length(); // length of base pattern
        int n2 = p2.length(); // length of target pattern

        // If target pattern is empty, it can appear infinite times
        if (n2 == 0) return Integer.MAX_VALUE;

        // Precompute for each possible state of matching p2:
        // nextState[state] -> how far in p2 we will be after scanning p1 once
        // completions[state] -> how many complete matches of p2 we found in one scan of p1
        int[] nextState = new int[n2];
        int[] completions = new int[n2];

        // For each possible "partial progress" state in p2
        for (int s = 0; s < n2; s++) {
            int state = s;   // current position in p2 (how many chars matched)
            int comp = 0;    // number of full matches completed in this iteration

            // Simulate scanning p1 once
            for (int i = 0; i < n1; i++) {
                if (state < n2 && p1.charAt(i) == p2.charAt(state)) {
                    state++; // matched one more char of p2
                }
                if (state == n2) {
                    // Completed one match of p2
                    comp++;
                    state = 0; // reset state for next match
                }
            }

            nextState[s] = state;     // store where we end up after scanning p1
            completions[s] = comp;    // store how many matches found
        }

        // Simulation variables
        int totalCompletions = 0;      // total matches found
        int currentState = 0;          // current matching progress
        Map<Integer, int[]> visited = new HashMap<>();
        // visited[state] = {timeStep when seen, totalCompletions at that time}
        int time = 0;                  // how many times we scanned p1 so far

        // Process up to t1 repetitions of p1
        while (time < t1) {
            // If we have seen this state before, a cycle is detected
            if (visited.containsKey(currentState)) {
                int[] prev = visited.get(currentState);
                int prevTime = prev[0];
                int prevCompletions = prev[1];

                // Calculate cycle info
                int cycleLength = time - prevTime;
                int cycleCompletions = totalCompletions - prevCompletions;

                // How many full cycles can we skip ahead?
                int remaining = t1 - time;
                int cycles = remaining / cycleLength;

                // Add all matches from skipped cycles
                totalCompletions += cycles * cycleCompletions;

                // Advance time by skipping cycles
                time += cycles * cycleLength;

                // If we reached or passed t1 after skipping cycles → stop
                if (time >= t1) break;

                // Clear visited to avoid multiple cycle processing
                visited.clear();
            }

            // Record this state (time step & matches count)
            visited.put(currentState, new int[]{time, totalCompletions});

            // Add matches found in this repetition
            totalCompletions += completions[currentState];

            // Move to next state
            currentState = nextState[currentState];

            // One repetition of p1 processed
            time++;
        }

        return totalCompletions;
    }

    public static void main(String[] args) {
        /**
         * Test cases to demonstrate behavior:
         *
         * Example:
         *   p1 = "bca", repeated 6 times
         *   p2 = "ba"
         *   → should output 6 (ba can appear 6 times)
         */
        System.out.println("Example: " + maxOccurrences("bca", 6, "ba")); // Expected: 6

        // Case 1: p1="abc" repeated 5 times, looking for "ac"
        // "ac" appears 5 times
        System.out.println("Case 1: " + maxOccurrences("abc", 5, "ac"));   // Expected: 5

        // Case 2: p1="aaa" repeated 1000 times, looking for "a"
        // Every char is 'a', so total matches = 3 * 1000 = 3000
        System.out.println("Case 2: " + maxOccurrences("aaa", 1000, "a")); // Expected: 3000

        // Case 3: p1="abcd" repeated 10 times, looking for "bd"
        // Each scan has 1 "bd", so total 10 matches
        System.out.println("Case 3: " + maxOccurrences("abcd", 10, "bd")); // Expected: 10

        // Case 4: p1="xyz" repeated 4 times, looking for "xy"
        // Each scan has 1 "xy", so total 4 matches
        System.out.println("Case 4: " + maxOccurrences("xyz", 4, "xy"));   // Expected: 4
    }
}
