import org.junit.Test;

import static com.google.common.truth.Truth.assertWithMessage;

public class CodingChallengesTest {

    @Test
    public void testMissingNumber() {
	    // the missing number from an array of length N containing all the values from 0 to N except for one missing number.
        // the array may not be sorted and may contain duplicates.
        assertWithMessage("Missing number in [0] should be 1")
                .that(CodingChallenges.missingNumber(new int[]{0})).isEqualTo(1);
        assertWithMessage("Missing number in [0, 1] should be 2")
                .that(CodingChallenges.missingNumber(new int[]{0, 1})).isEqualTo(2);
        assertWithMessage("Missing number in [1] should be 0")
                .that(CodingChallenges.missingNumber(new int[]{1})).isEqualTo(0);
        assertWithMessage("Missing number in [0, 2] should be 1")
                .that(CodingChallenges.missingNumber(new int[]{2, 0})).isEqualTo(1);
        assertWithMessage("Missing number in [0, 1, 2, 3, 4] should be 5")
                .that(CodingChallenges.missingNumber(new int[]{0, 1, 2, 3, 4})).isEqualTo(5);
        assertWithMessage("Missing number in [0, 1, 2, 3, 5] should be 4")
                .that(CodingChallenges.missingNumber(new int[]{0, 1, 2, 3, 5})).isEqualTo(4);
        assertWithMessage("Missing number in [0, 1, 2, 3, 4, 5] should be 6")
                .that(CodingChallenges.missingNumber(new int[]{0, 1, 2, 3, 4, 5})).isEqualTo(6);
        assertWithMessage("Missing number in [1, 2, 3, 4, 5] should be 0")
                .that(CodingChallenges.missingNumber(new int[]{1, 2, 3, 4, 5})).isEqualTo(0);
        assertWithMessage("Missing number in [0, 1, 2, 4, 5, 6] should be 3")
                .that(CodingChallenges.missingNumber(new int[]{4, 2, 0, 1, 6, 5})).isEqualTo(3);
    }

    @Test
    public void testIsPermutation() {
	    // true if and only if s1 is a permutation of s2. s1 is a permutation of s2 if it has the same number of each character as s2.
        assertWithMessage("Empty strings should be permutations of each other")
                .that(CodingChallenges.isPermutation("", "")).isTrue();
        assertWithMessage("a should not be a permutation of b")
                .that(CodingChallenges.isPermutation("a", "b")).isFalse();
        assertWithMessage("abc should be a permutation of cba")
                .that(CodingChallenges.isPermutation("abc", "cba")).isTrue();
        assertWithMessage("abc should not be a permutation of abcd")
                .that(CodingChallenges.isPermutation("abc", "abcd")).isFalse();
        assertWithMessage("aabbcc should be a permutation of bbacca")
                .that(CodingChallenges.isPermutation("aabbcc", "bbacca")).isTrue();
        assertWithMessage("aabbcc should not be a permutation of aabbc")
                .that(CodingChallenges.isPermutation("aabbcc", "aabbc")).isFalse();
    }
}
