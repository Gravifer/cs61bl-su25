package game2048logic;

import game2048rendering.Side;
import org.jetbrains.annotations.NotNull;

import static game2048logic.MatrixUtils.rotateLeft;
import static game2048logic.MatrixUtils.rotateRight;

/**
 * @author  Josh Hug
 */
public class GameLogic {
    /** Moves the given tile up as far as possible, subject to the minR constraint.
     *
     * @param board the current state of the board
     * @param r     the row number of the tile to move up
     * @param c -   the column number of the tile to move up
     * @param minR  the minimum row number that the tile can land in; e.g.,
     *              if minR is 2, the moving tile should move no higher than row 2.
     * @return      if there is a merge, returns the 1 + the row number where the merge occurred.
     *              if no merge occurs, then return minR.
     */
    public static int moveTileUpAsFarAsPossible(int[]@NotNull[] board, int r, int c, int minR) {
        if (board[r][c] == 0) {
            return -1;
        }
        if (r <= minR) {
            return minR;
        }
        if (board[r - 1][c] == 0) {
            board[r - 1][c] = board[r][c];
            board[r][c] = 0;
            return moveTileUpAsFarAsPossible(board, r - 1, c, minR);
        } else if (board[r - 1][c] == board[r][c]) {
            board[r - 1][c] += board[r][c];
            board[r][c] = 0;
            return r;
        } else {
            return minR;
        }
    }

    /**
     * Modifies the board to simulate the process of tilting column c
     * upwards.
     *
     * @param board     the current state of the board
     * @param c         the column to tilt up.
     */
    public static void tiltColumn(int[]@NotNull[] board, int c) {
        // DONE: fill this in (task 5)
        int minR = 0;
        for (int r = 1; r < board.length; r++) {
            if (board[r][c] != 0) {
                minR = moveTileUpAsFarAsPossible(board, r, c, minR);
            }
        }
    }

    /**
     * Modifies the board to simulate tilting all columns upwards.
     *
     * @param board     the current state of the board.
     */
    public static void tiltUp(int[]@NotNull[] board) {
        // DONE: fill this in (task 6)
        for (int c = 0; c < board.length; c++) {
            tiltColumn(board, c);
        }
    }

    /**
     * Modifies the board to simulate tilting the entire board to
     * the given side.
     *
     * @param board the current state of the board
     * @param side  the direction to tilt
     */
    public static void tilt(int[]@NotNull[] board, Side side) {
        // DONE: fill this in (task 7)
        if (side == game2048rendering.Side.NORTH) {
            tiltUp(board);
        } else if (side == game2048rendering.Side.EAST) {
            rotateLeft(board);
            tiltUp(board);
            rotateRight(board);
        } else if (side == game2048rendering.Side.SOUTH) {
            rotateLeft(board);
            rotateLeft(board);
            tiltUp(board);
            rotateLeft(board);
            rotateLeft(board);
        } else if (side == game2048rendering.Side.WEST) {
            rotateRight(board);
            tiltUp(board);
            rotateLeft(board);
        } else {
            System.out.println("Invalid side specified");
        }
    }
}
