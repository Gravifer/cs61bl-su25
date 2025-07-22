import java.util.ArrayList;

/* A MinHeap class of Comparable elements backed by an ArrayList. */
public class MinHeap<E extends Comparable<E>> {

    /* An ArrayList that stores the elements in this MinHeap. */
    private ArrayList<E> contents;
    private int size;
    // TODO: YOUR CODE HERE (no code should be needed here if not implementing the more optimized version)

    /* Initializes an empty MinHeap. */
    public MinHeap() {
        contents = new ArrayList<>();
        contents.add(null);
    }

    /* Returns the element at index INDEX, and null if it is out of bounds. */
    private E getElement(int index) {
        if (index >= contents.size()) {
            return null;
        } else {
            return contents.get(index);
        }
    }

    /* Sets the element at index INDEX to ELEMENT. If the ArrayList is not big
       enough, add elements until it is the right size. */
    private void setElement(int index, E element) {
        while (index >= contents.size()) {
            contents.add(null);
        }
        contents.set(index, element);
    }

    /* Swaps the elements at the two indices. */
    private void swap(int index1, int index2) {
        E element1 = getElement(index1);
        E element2 = getElement(index2);
        setElement(index2, element1);
        setElement(index1, element2);
    }

    /* Prints out the underlying heap sideways. Use for debugging. */
    @Override
    public String toString() {
        return toStringHelper(1, "");
    }

    /* Recursive helper method for toString. */
    private String toStringHelper(int index, String soFar) {
        if (getElement(index) == null) {
            return "";
        } else {
            String toReturn = "";
            int rightChild = getRightOf(index);
            toReturn += toStringHelper(rightChild, "        " + soFar);
            if (getElement(rightChild) != null) {
                toReturn += soFar + "    /";
            }
            toReturn += "\n" + soFar + getElement(index) + "\n";
            int leftChild = getLeftOf(index);
            if (getElement(leftChild) != null) {
                toReturn += soFar + "    \\";
            }
            toReturn += toStringHelper(leftChild, "        " + soFar);
            return toReturn;
        }
    }

    /* Returns the index of the left child of the element at index INDEX. */
    private int getLeftOf(int index) {
        // DONE: YOUR CODE HERE
        if (index < 1) {
            return -1; // Invalid index
        }
        return 2 * index;
    }

    /* Returns the index of the right child of the element at index INDEX. */
    private int getRightOf(int index) {
        // DONE: YOUR CODE HERE
        if (index < 1) {
            return -1; // Invalid index
        }
        return 2 * index;
    }

    /* Returns the index of the parent of the element at index INDEX. */
    private int getParentOf(int index) {
        // DONE: YOUR CODE HERE
        if (index <= 1) {
            return -1; // Invalid index / the root has no parent
        }
        return index / 2;
    }

    /* Returns the index of the smaller element. At least one index has a
       non-null element. If the elements are equal, return either index. */
    private int min(int index1, int index2) {
        // DONE: YOUR CODE HERE
        if (index1 < 1 || index2 < 1 || index1 >= contents.size() || index2 >= contents.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index1 + ", " + index2);
        }
        if (index1 == index2) {
            return index1; // Both indices are the same, return either
        }
        E element1 = getElement(index1);
        E element2 = getElement(index2);
        if (element1 == null && element2 == null) {
            return -1; // Both elements are null, no valid index
        } else if (element1 == null) {
            return index2;
        } else if (element2 == null) {
            return index1;
        }
        int comparison = element1.compareTo(element2);
        if (comparison <= 0) {
            return index1;
        } else if (comparison > 0) {
            return index2;
        }
        return -1;
    }

    /* Returns but does not remove the smallest element in the MinHeap. */
    public E findMin() {
        // DONE: YOUR CODE HERE
        if (size < 1) {
            return null; // No elements in the heap
        }
        return getElement(1); // The smallest element is at index 1
    }

    /* Bubbles up the element currently at index INDEX. */
    private void bubbleUp(int index) { // TODO: make this iterative
        // DONE: YOUR CODE HERE
        if (index <= 1) {
            return; // The root has no parent to bubble up to
        }
        int parentIndex = getParentOf(index);
        E currentElement = getElement(index);
        E parentElement = getElement(parentIndex);
        if (currentElement != null && parentElement != null && currentElement.compareTo(parentElement) < 0) {
            // Swap the current element with its parent
            swap(index, parentIndex);
            // Recursively bubble up the parent index
            bubbleUp(parentIndex);
        }
    }

    /* Bubbles down the element currently at index INDEX. */
    private void bubbleDown(int index) { // TODO: make this iterative
        // DONE: YOUR CODE HERE
        if (index < 1 || index >= contents.size()) {
            return; // Invalid index
        }
        int leftChildIndex = getLeftOf(index);
        int rightChildIndex = getRightOf(index);
        int smallestIndex = index;
        E currentElement = getElement(index);
        E leftChildElement = getElement(leftChildIndex);
        E rightChildElement = getElement(rightChildIndex);
        if (leftChildElement != null && currentElement.compareTo(leftChildElement) > 0) {
            smallestIndex = leftChildIndex; // Left child smaller
        }
        if (rightChildElement != null && getElement(smallestIndex) != null &&
            getElement(smallestIndex).compareTo(rightChildElement) > 0) {
            smallestIndex = rightChildIndex; // Right child smaller
        }
        if (smallestIndex != index) {
            // Swap the current element with the smallest child
            swap(index, smallestIndex);
            // Recursively bubble down the smallest index
            bubbleDown(smallestIndex);
        }
    }

    /* Returns the number of elements in the MinHeap. */
    public int size() { // TODO: OPTIMIZE THIS TO MAKE IT O(1), using the size field
        // DONE: YOUR CODE HERE
        if (contents == null || contents.isEmpty()) {
            return 0; // No elements in the heap
        }
        // The size of the heap is the number of elements in the contents list minus the null at index 0
        return contents.size() - 1; // Exclude the null at index 0
    }

    /* Inserts ELEMENT into the MinHeap. If ELEMENT is already in the MinHeap,
       throw an IllegalArgumentException.*/
    public void insert(E element) {
        // TODO: YOUR CODE HERE
    }

    /* Returns and removes the smallest element in the MinHeap, or null if there are none. */
    public E removeMin() {
        // DONE: YOUR CODE HERE
        if (size < 1) {
            return null; // No elements to remove
        }
        E minElement = findMin(); // The smallest element is at index 1
        if (minElement == null) {
            return null; // No valid element to remove
        }
        // Move the last element to the root and bubble down
        E lastElement = getElement(contents.size() - 1);
        setElement(1, lastElement); // Move the last element to the root
        contents.remove(contents.size() - 1); // Remove the last element
        size--; // Decrease the size of the heap
        bubbleDown(1); // Bubble down the new root element
        return minElement;
    }

    /* Replaces and updates the position of ELEMENT inside the MinHeap, which
       may have been mutated since the initial insert. If a copy of ELEMENT does
       not exist in the MinHeap, throw a NoSuchElementException. Item equality
       should be checked using .equals(), not ==. */
    public void update(E element) {
        // TODO: OPTIONAL
    }

    /* Returns true if ELEMENT is contained in the MinHeap. Item equality should
       be checked using .equals(), not ==. */
    public boolean contains(E element) {
        // OPTIONAL: OPTIMIZE THE SPEED OF THIS TO MAKE IT CONSTANT
        for (int i = 1; i < contents.size(); i++) {
            if (element.equals(contents.get(i))) {
                return true;
            }
        }
        return false;
    }
}
