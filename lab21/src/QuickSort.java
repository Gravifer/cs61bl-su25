public class QuickSort {

    /**
     * @param arr
     *
     * Sort the array arr using quicksort with the 3-scan partition algorithm.
     * The quicksort algorithm is as follows:
     * 1. Select a pivot, partition array in place around the pivot.
     * 2. Recursively call quicksort on each subsection of the modified array.
     */
    public static int[] sort(int[] arr) {
        quickSort(arr, 0, arr.length);
        return arr;
    }

    /**
     * @param arr
     * @param start
     * @param end
     *
     * Helper method for sort: runs quicksort algorithm on array from [start:end)
     */
    private static void quickSort(int[] arr, int start, int end) {
        // DONE: Implement quicksort
        if (start < end - 1) {
            int[] partitionIndices = partition(arr, start, end);
            int lEnd = partitionIndices[0];
            int gStart = partitionIndices[1];

            quickSort(arr, start, lEnd);
            quickSort(arr, gStart, end);
        }
    }

    /**
     * @param arr
     * @param start
     * @param end
     *
     * Partition the array in-place following the 3-scan partitioning scheme.
     * You may assume that first item is always selected as the pivot.
     * 
     * Returns a length-2 int array of indices:
     * [end index of "less than" section, start index of "greater than" section]
     *
     * Most of the code for quicksort is in this function
     */
    private static int[] partition(int[] arr, int start, int end) {
        // DONE: Implement partition
        int pivot = arr[start];
        int lEnd = start; // one over the end index of "less than", therefore always pointing to the pivot
        int gStart = end; // start index of "greater than"

        // 3-way qsort
        for (int i = start; i < gStart; i++) {
            if (arr[i] < pivot) {
                swap(arr, lEnd, i);
                lEnd++;
            } else if (arr[i] > pivot) {
                gStart--;
                swap(arr, i, gStart);
                i--; // stay at the same index after swapping
            }
        }

        return new int[]{lEnd, gStart};
    }


    /**
     * @param arr
     * @param i
     * @param j
     *
     * Swap the elements at indices i and j in the array arr.
     * A helper method you can use in your implementation of sort.
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}   
