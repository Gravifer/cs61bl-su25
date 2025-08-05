import org.junit.Test;
import com.google.common.primitives.Ints;

import static com.google.common.truth.Truth.assertThat;

public class DistributionSortsTest {
    @Test
    public void testBasic() {
        // DONE: test it out!
        int[] arr1 = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        int[] expected1 = {1, 1, 2, 3, 3, 4, 5, 5, 5, 6, 9};
        DistributionSorts.countingSort(arr1);
        assertThat(Ints.asList(arr1)).containsExactlyElementsIn(Ints.asList(expected1)).inOrder();

        int[] arr2 = {0, 0, 0, 0};
        int[] expected2 = {0, 0, 0, 0};
        DistributionSorts.countingSort(arr2);
        assertThat(Ints.asList(arr2)).containsExactlyElementsIn(Ints.asList(expected2)).inOrder();

        int[] arr3 = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        int[] expected3 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        DistributionSorts.countingSort(arr3);
        assertThat(Ints.asList(arr3)).containsExactlyElementsIn(Ints.asList(expected3)).inOrder();

        int[] arr4 = {170, 45, 75, 90, 802, 24, 2, 66};
        int[] expected4 = {2, 24, 45, 66, 75, 90, 170, 802};
        DistributionSorts.lsdRadixSort(arr4);
        assertThat(Ints.asList(arr4)).containsExactlyElementsIn(Ints.asList(expected4)).inOrder();

        int[] arr5 = {1, 10, 100, 1000};
        int[] expected5 = {1, 10, 100, 1000};
        DistributionSorts.lsdRadixSort(arr5);
        assertThat(Ints.asList(arr5)).containsExactlyElementsIn(Ints.asList(expected5)).inOrder();

        int[] arr6 = {5, 4, 3, 2, 1};
        int[] expected6 = {1, 2, 3, 4, 5};
        DistributionSorts.lsdRadixSort(arr6);
        assertThat(Ints.asList(arr6)).containsExactlyElementsIn(Ints.asList(expected6)).inOrder();
    }

}
