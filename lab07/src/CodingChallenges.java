import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class CodingChallenges {

    /**
     * Return the missing number from an array of length N containing all the
     * values from 0 to N except for one missing number.
     */
    public static int missingNumber(int[] values) {
        // the array may not be sorted and may contain duplicates.
        Set<Integer> seen = new HashSet<>();
        int max = 0;
        for (int value : values) {
            if (value < 0) {
                continue; // ignore negative numbers
            }
            seen.add(value);
            if (value > max) {
                max = value;
            }
        }
        // Check for the missing number in the range 0 to max
        for (int i = 0; i <= max; i++) {
            if (!seen.contains(i)) {
                return i;
            }
        }
        // If all numbers from 0 to max are present, the missing number is max + 1
        return max + 1;
    }

    /**
     * Returns true if and only if s1 is a permutation of s2. s1 is a
     * permutation of s2 if it has the same number of each character as s2.
     */
    public static boolean isPermutation(String s1, String s2) {
        if (s1.length() != s2.length()) {
            return false;
        }

        Map<Character, Integer> charCount = new HashMap<>();

        for (char c : s1.toCharArray()) {
            charCount.put(c, charCount.getOrDefault(c, 0) + 1);
        }

        for (char c : s2.toCharArray()) {
            if (!charCount.containsKey(c) || charCount.get(c) == 0) {
                return false;
            }
            charCount.put(c, charCount.get(c) - 1);
        }

        return true;
    }
}
