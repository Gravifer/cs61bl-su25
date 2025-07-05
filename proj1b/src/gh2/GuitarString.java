package gh2;

import deque.ArrayDeque61B;
import deque.Deque61B;

/*
 * NOTE: Implementation of GuitarString is OPTIONAL practice, and will not be tested in the auto-grader.
 * This class will not compile until the Deque61B implementations are complete.
 */

public class GuitarString {
    /** Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday. */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    // DONE: uncomment the following line once you're ready to start this portion
    private Deque61B<Double> buffer;
    private int bufferSize;
    private void newBuffer(int capacity) {
        // set every element in the buffer to 0.0
        this.buffer = new ArrayDeque61B<>(capacity); // fastest way to do this without breaching the interface contract
        for (int i = 0; i < capacity; i++) {
            buffer.addLast(0.0); // Fill the buffer with zeros
        }
    }
    private void clearBuffer() {
        // Clear the buffer by removing all elements
        int capacity = buffer.size();
        newBuffer(capacity);
    }

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        // TODO: Initialize the buffer with capacity = SR / frequency. You'll need to
        //       cast the result of this division operation into an int. For
        //       better accuracy, use the Math.round() function before casting.
        //       Your should initially fill your buffer with zeros.
        int capacity = (int) Math.round(SR / frequency);
        if (capacity <= 0) {
            throw new IllegalArgumentException("Frequency must be positive.");
        }
        newBuffer(capacity);
        this.bufferSize = capacity;
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        // DONE: Dequeue everything in buffer, and replace with random numbers
        //       between -0.5 and 0.5. You can get such a number by using:
        //       double r = Math.random() - 0.5;
        //
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.
        for (int i = 0; i < bufferSize; i++) {
            double r = Math.random() - 0.5; // Generate a random number between -0.5 and 0.5
            buffer.removeFirst(); // Remove the first element
            buffer.addLast(r); // Add the new random number to the end of the buffer
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        // DONE: Dequeue the front sample and enqueue a new sample that is
        //       the average of the two multiplied by the DECAY factor.
        //       **Do not call StdAudio.play().**
        double sample1 = buffer.removeFirst(); // Remove the first element
        double sample2 = sample(); // Peek at the first element without removing it
        double newSample = DECAY * 0.5 * (sample1 + sample2); // Calculate the new sample
        buffer.addLast(newSample); // Add the new sample to the end of the buffer
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        // DONE: Return the first element in the buffer without removing it.
        if (buffer.isEmpty()) {
            return 0.0; // If the buffer is empty, return 0.0
        }
        return buffer.get(0); // Return the first element in the buffer
    }
}
    // DONE: Remove all comments that say T0D0 when you're done.
