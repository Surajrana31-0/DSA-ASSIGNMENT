import java.util.*;

/**
 * CryptarithmeticSolver
 * ---------------------
 * Solves a cryptarithmetic puzzle like:
 *
 *   SEND
 * + MORE
 * ------
 *  MONEY
 *
 * by assigning unique digits (0-9) to each letter so that the sum is valid.
 *
 * Constraints:
 *  ✅ Each letter must have a unique digit.
 *  ✅ Leading letters cannot be 0.
 *  ✅ At most 10 unique letters (since only 10 digits available).
 */
public class CryptarithmeticSolver {

    /**
     * Checks if there exists ANY valid digit assignment such that:
     *   word1 + word2 = word3
     *
     * @param word1 First addend
     * @param word2 Second addend
     * @param word3 Result word (sum)
     * @return true if a valid mapping exists, otherwise false
     */
    public static boolean hasSolution(String word1, String word2, String word3) {
        // Collect all unique letters from the three words
        Set<Character> distinctLetters = new HashSet<>();
        addLetters(word1, distinctLetters);
        addLetters(word2, distinctLetters);
        addLetters(word3, distinctLetters);

        // If there are more than 10 unique letters, impossible to assign digits 0-9
        if (distinctLetters.size() > 10) return false;

        // Convert the set of distinct letters to an array for easier indexing
        char[] letters = new char[distinctLetters.size()];
        int idx = 0;
        for (char c : distinctLetters) letters[idx++] = c;

        // Mapping from character to digit (A-Z → 0-9). Initially -1 (unassigned)
        int[] charToDigit = new int[26];
        Arrays.fill(charToDigit, -1);

        // Tracks which digits are already used
        boolean[] usedDigits = new boolean[10];

        // Start recursive backtracking to try all digit assignments
        return backtrack(0, letters, charToDigit, usedDigits, word1, word2, word3);
    }

    /**
     * Helper method to add all letters from a word into a set.
     */
    private static void addLetters(String word, Set<Character> set) {
        for (char c : word.toCharArray()) set.add(c);
    }

    /**
     * Backtracking method:
     * Assign digits to letters one by one and check if a valid solution exists.
     *
     * @param idx         Current letter index we are trying to assign
     * @param letters     Array of unique letters
     * @param charToDigit Mapping from char → digit
     * @param usedDigits  Tracks which digits are already used
     * @param word1       First addend
     * @param word2       Second addend
     * @param word3       Result word
     * @return true if a valid mapping exists
     */
    private static boolean backtrack(int idx, char[] letters, int[] charToDigit,
                                     boolean[] usedDigits, String word1, String word2, String word3) {
        // Base case: all letters assigned → validate the equation
        if (idx == letters.length) {
            return validate(charToDigit, word1, word2, word3);
        }

        // Current letter we are assigning a digit to
        char c = letters[idx];

        // Try all digits 0-9 for this letter
        for (int digit = 0; digit <= 9; digit++) {
            // Skip if this digit is already used
            if (usedDigits[digit]) continue;

            // Leading letters cannot be assigned digit 0
            if (digit == 0 && isLeadingLetter(c, word1, word2, word3)) continue;

            // Assign this digit
            usedDigits[digit] = true;
            charToDigit[c - 'A'] = digit;

            // Recursively assign next letter
            if (backtrack(idx + 1, letters, charToDigit, usedDigits, word1, word2, word3)) {
                return true; // found a valid solution
            }

            // Backtrack → undo assignment
            usedDigits[digit] = false;
            charToDigit[c - 'A'] = -1;
        }
        return false; // no valid digit for this letter
    }

    /**
     * Checks if a letter is a leading letter in any word.
     * Leading letters cannot be 0.
     */
    private static boolean isLeadingLetter(char c, String word1, String word2, String word3) {
        return word1.charAt(0) == c || word2.charAt(0) == c || word3.charAt(0) == c;
    }

    /**
     * Validates if the current char→digit mapping satisfies:
     *    num(word1) + num(word2) == num(word3)
     */
    private static boolean validate(int[] charToDigit, String word1, String word2, String word3) {
        // Leading zeros check
        if (charToDigit[word1.charAt(0) - 'A'] == 0 ||
            charToDigit[word2.charAt(0) - 'A'] == 0 ||
            charToDigit[word3.charAt(0) - 'A'] == 0) {
            return false;
        }

        // Convert words into numbers using the current mapping
        long num1 = convertToNumber(word1, charToDigit);
        long num2 = convertToNumber(word2, charToDigit);
        long num3 = convertToNumber(word3, charToDigit);

        // Ensure valid conversion and check equation
        return num1 != -1 && num2 != -1 && num3 != -1 && (num1 + num2 == num3);
    }

    /**
     * Converts a word into its numeric form based on current mapping.
     */
    private static long convertToNumber(String word, int[] charToDigit) {
        long num = 0;
        for (char c : word.toCharArray()) {
            int digit = charToDigit[c - 'A'];
            if (digit == -1) return -1; // unassigned letter → invalid
            num = num * 10 + digit;
        }
        return num;
    }

    /**
     * Test driver for common cryptarithmetic puzzles.
     */
    public static void main(String[] args) {
        // Test case 1: CODE + BUG = DEBUG (should return false, no valid solution)
        System.out.println("CODE + BUG = DEBUG: " +
            hasSolution("CODE", "BUG", "DEBUG"));

        // Test case 2: SEND + MORE = MONEY (classic puzzle, should return true)
        System.out.println("SEND + MORE = MONEY: " +
            hasSolution("SEND", "MORE", "MONEY"));

        // Test case 3: A + A = B (simple puzzle, has a solution: A=5, B=0 for example)
        System.out.println("A + A = B: " +
            hasSolution("A", "A", "B"));
    }
}
