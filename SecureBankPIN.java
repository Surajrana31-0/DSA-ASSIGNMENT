import java.util.*;

/**
 * SecureBankPIN - A utility class to check and improve the strength of a PIN code.
 * 
 * This program evaluates how many steps are needed to make a given PIN (or password)
 * strong based on these rules:
 *  - Must have at least one lowercase, one uppercase, and one digit.
 *  - Length must be at least 6 characters.
 *  - No 3+ repeating consecutive characters.
 *  - Must not exceed 20 characters (otherwise need deletions).
 *
 * This algorithm is based on the "Strong Password Checker" problem.
 */
public class SecureBankPIN {

    /**
     * strongPasswordChecker - Determines the minimum number of changes (insertions, deletions, replacements)
     * required to make the given pin_code strong.
     *
     * @param pin_code the input PIN or password string
     * @return minimum number of steps required to make the PIN strong
     */
    public static int strongPasswordChecker(String pin_code) {
        int n = pin_code.length(); // length of the given PIN

        // Flags to check if the PIN contains required character types
        boolean hasLower = false, hasUpper = false, hasDigit = false;

        // Check if PIN has lowercase, uppercase and digit
        for (char c : pin_code.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        // Count how many required types are missing
        int missing_type = 0;
        if (!hasLower) missing_type++;  // missing lowercase
        if (!hasUpper) missing_type++;  // missing uppercase
        if (!hasDigit) missing_type++;  // missing digit

        // CASE 1: PIN is too short (<6 characters)
        // → Need to add missing characters or required types
        if (n < 6) {
            // Either add enough chars to reach length 6, or fix missing types (whichever is larger)
            return Math.max(6 - n, missing_type);
        }

        // List to store lengths of repeating runs (e.g. "aaa" -> length 3)
        List<Integer> runs = new ArrayList<>();
        for (int i = 0; i < n; ) {
            int j = i;
            // Move j until characters are same
            while (j < n && pin_code.charAt(j) == pin_code.charAt(i)) {
                j++;
            }
            int runLength = j - i;
            // Only consider runs >=3 because they violate the rule
            if (runLength >= 3) {
                runs.add(runLength);
            }
            i = j; // move to next different character
        }

        // CASE 2: PIN is within allowed length (6 to 20 chars)
        if (n <= 20) {
            int replace = 0; // total replacements needed for repeating runs
            for (int len : runs) {
                replace += len / 3; // every 3 same chars need at least 1 replacement
            }
            // Must fix both: repeating runs & missing char types
            return Math.max(replace, missing_type);
        }

        // CASE 3: PIN is too long (>20 characters)
        int del_needed = n - 20; // how many deletions required to bring it to max length 20
        int total_replace = 0;   // how many replacements required for repeating runs

        // Calculate total replacements if no deletions are applied
        for (int len : runs) {
            total_replace += len / 3;
        }

        // modCount[0] = count of runs where length % 3 == 0
        // modCount[1] = count of runs where length % 3 == 1
        // modCount[2] = count of runs where length % 3 == 2
        int[] modCount = new int[3];
        for (int len : runs) {
            modCount[len % 3]++;
        }

        // Try to minimize replacements by using deletions smartly
        int saved = 0;

        // First, delete from runs where len % 3 == 0 (1 deletion reduces 1 replacement)
        int use0 = Math.min(modCount[0], del_needed);
        saved += use0;
        del_needed -= use0;

        // Then, delete from runs where len % 3 == 1 (2 deletions reduce 1 replacement)
        int use1 = Math.min(modCount[1] * 2, del_needed);
        saved += use1 / 2; // each 2 deletions save 1 replacement
        del_needed -= use1;

        // Finally, for remaining deletions, every 3 deletions save 1 replacement
        saved += del_needed / 3;

        // Adjust remaining replacements after deletions
        total_replace = Math.max(0, total_replace - saved);

        // Final result = mandatory deletions + max(missing types, remaining replacements)
        return (n - 20) + Math.max(total_replace, missing_type);
    }

    /**
     * Main method for quick testing with example cases.
     */
    public static void main(String[] args) {
        // Example 1: Very short PIN
        System.out.println("Example 1: " + strongPasswordChecker("X1!")); // Expected output: 3 (needs 3 more chars)

        // Example 2: Only digits, but length 6 (missing lowercase+uppercase)
        System.out.println("Example 2: " + strongPasswordChecker("123456")); // Expected output: 2

        // Example 3: Already strong PIN (contains all types & length OK)
        System.out.println("Example 3: " + strongPasswordChecker("Aa1234!")); // Expected output: 0

        // Case 4: Very short & repeating chars
        System.out.println("Case 4: " + strongPasswordChecker("aaa")); // Expected output: 3

        // Case 5: Short but has repeating chars
        System.out.println("Case 5: " + strongPasswordChecker("aaaaa")); // Expected output: 2

        // Case 6: Long repeating digits
        System.out.println("Case 6: " + strongPasswordChecker("1111111111")); // Expected output: 3
    }
}
