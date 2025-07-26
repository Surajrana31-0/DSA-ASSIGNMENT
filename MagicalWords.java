import java.util.*;

/**
 * MagicalWords
 * ------------
 * This program finds the **maximum product** of lengths of two palindromic substrings
 * that appear **one after the other** in a given string `M`.
 *
 * In simpler terms:
 *   - First, pick a palindrome that ends at some position.
 *   - Then, pick another palindrome that starts after it.
 *   - Multiply their lengths.
 *   - Find the maximum product over all such pairs.
 */
public class MagicalWords {

    /**
     * Finds the maximum product of two **non-overlapping palindromic substrings** in M.
     *
     * @param M The input string
     * @return Maximum product of two palindromic substrings' lengths
     */
    public static int maxPowerCombination(String M) {
        int n = M.length();

        // Edge case: empty string → no palindrome
        if (n == 0) return 0;

        // maxLenStarting[i] = length of the longest palindrome starting at index i
        int[] maxLenStarting = new int[n];
        Arrays.fill(maxLenStarting, 0);

        // STEP 1: Find all palindromes centered at each position
        for (int center = 0; center < n; center++) {
            int l = center, r = center; // Expand around center
            while (l >= 0 && r < n && M.charAt(l) == M.charAt(r)) {
                int len = r - l + 1; // Current palindrome length

                // If this palindrome is longer than what we had before for start index `l`
                if (len > maxLenStarting[l]) {
                    maxLenStarting[l] = len;
                }
                // Expand outward
                l--;
                r++;
            }
        }

        // STEP 2: Precompute bestAfter[i] = best palindrome length starting at or after index i
        int[] bestAfter = new int[n + 1];
        bestAfter[n] = 0; // after end → no palindrome
        for (int i = n - 1; i >= 0; i--) {
            // Either best starting exactly at i or any better palindrome starting later
            bestAfter[i] = Math.max(maxLenStarting[i], bestAfter[i + 1]);
        }

        // STEP 3: Try all palindromic substrings as the **first palindrome**
        //         and multiply by the best possible second palindrome after it
        int maxProduct = 0;
        for (int center = 0; center < n; center++) {
            int l = center, r = center;
            while (l >= 0 && r < n && M.charAt(l) == M.charAt(r)) {
                int len = r - l + 1;     // length of current palindrome
                int nextStart = r + 1;   // next available position after current palindrome
                int second = (nextStart < n) ? bestAfter[nextStart] : 0; // best palindrome after it

                // Product of current palindrome length * best next palindrome length
                maxProduct = Math.max(maxProduct, len * second);

                // Expand outward for a larger palindrome
                l--;
                r++;
            }
        }

        return maxProduct;
    }

    public static void main(String[] args) {
        /**
         * TEST CASES:
         *
         * Example 1:
         *   M = "xyzyxabc"
         *   "xyzyx" is a palindrome (length 5),
         *   After it "a" (length 1) is trivial,
         *   So max product = 5
         */
        System.out.println("Example 1: " + maxPowerCombination("xyzyxabc")); // Expected: 5

        /**
         * Example 2:
         *   M = "levelwowracecar"
         *   "level" (5) + "racecar" (7) → product = 35
         */
        System.out.println("Example 2: " + maxPowerCombination("levelwowracecar")); // Expected: 35

        /**
         * Case 3:
         *   M = "aaa"
         *   longest palindrome = "aaa" (3),
         *   but no second palindrome after it → product = 0
         *   (but smaller split → "a"(1) * "a"(1) = 1)
         */
        System.out.println("Case 3: " + maxPowerCombination("aaa")); // Expected: 1

        /**
         * Case 4:
         *   M = "a"
         *   Only one letter → cannot form two palindromes → 0
         */
        System.out.println("Case 4: " + maxPowerCombination("a")); // Expected: 0

        /**
         * Case 5:
         *   M = "ababa"
         *   "aba" (3) + "a" (1) → product = 3
         */
        System.out.println("Case 5: " + maxPowerCombination("ababa")); // Expected: 3

        /**
         * Case 6:
         *   M = "abcde"
         *   No repeated letters → no multi-length palindromes → 0
         */
        System.out.println("Case 6: " + maxPowerCombination("abcde")); // Expected: 0
    }
}
