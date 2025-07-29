package PlusWorld;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import static org.junit.Assert.*;

import byowTools.TileEngine.TERenderer;
import byowTools.TileEngine.TETile;
import byowTools.TileEngine.Tileset;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Draws a world consisting of plus shaped regions.
 */
public class PlusWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static final List<TETile> tileList = new ArrayList<>();

    static boolean addPlus(TETile[][] tiles, int s, TETile tile, Pair<Integer, Integer> offset){
        if (s < 1) {
            return false;
        }
        // offset points to the leftmost, bottommost tile of the bounding box of the Plus shape
        // the bounding box is a square of size 3s x 3s, centered at (3s/2, 3s/2)
        // the strokes of the plus shape is of width s and height 3s
        Function<Pair<Integer, Integer>, Boolean> predicate = (p) -> {
            int x = p.getLeft(), y = p.getRight();
            return  (x >= 0 && x < 3 * s && y >= 0 && y < 3 * s) &&
                    (x >= s && x < 2 * s || y >= s && y < 2 * s);
        };
        return addShape(tiles, predicate, tile, offset);
    }

    static boolean addShape(TETile[][] tiles, Function<Pair<Integer, Integer>, Boolean> predicate, TETile tile, Pair<Integer, Integer> offset) {
        int height = tiles[0].length;
        int width = tiles.length;
        int x = offset.getLeft();
        int y = offset.getRight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (predicate.apply(Pair.of(i - x, j - y))) {
                    tiles[i][j] = tile;
                }
            }
        }
        return true;
    }

    static void tessellateWorld(TETile[][] world, int s, Pair<Integer, Integer> startingPoint, boolean leftShoulder) {
        int height = world[0].length;
        int width = world.length;
        int x0 = startingPoint.getLeft();
        int y0 = startingPoint.getRight();
        int ax = 2 * s;
        int ay = leftShoulder ? s : -s;
        int bx = leftShoulder ? - s : s;
        int by = 2 * s;
        for (int i = -Math.floorDiv(x0, s) - 1; i <= Math.floorDiv(width - x0, s) + 1 ; i++)
            for (int j = -Math.floorDiv(y0, s) - 1; j <= Math.floorDiv(height - y0, s) + 1; j++){
                int x = x0 + i * ax + j * bx;
                int y = y0 + i * ay + j * by;
                // check that the bounding box will intersect the world
                if (!(x >= 0 || y >= 0 || x + 3 * s <= width || y + 3 * s <= height)) {
                    return; // the plus shape is out of bounds
                }
                if (!tileList.isEmpty()) {
                    TETile randomTile = tileList.get(RANDOM.nextInt(tileList.size()));
                    addPlus(world, s, randomTile, Pair.of(x, y));
                }

            }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        // populate tileList if it is empty
        if (tileList.isEmpty()) {
            for (Field field : Tileset.class.getFields()) {
                if (field.getType() == TETile.class) {
                    try {
                        tileList.add((TETile) field.get(null));
                    } catch (IllegalAccessException e) {
                        // simply ignore
                    }
                }
            }
        }
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // randomly tessellate the world with plus shapes
        int s = RANDOM.nextInt(5) + 1; // plus size between 1 and 3
        int x = RANDOM.nextInt(WIDTH);
        int y = RANDOM.nextInt(HEIGHT);
        boolean leftShoulder = RANDOM.nextBoolean();
        tessellateWorld(world, s, Pair.of(x, y), leftShoulder);

        // addPlus(world, 10, Tileset.WALL, Pair.of((WIDTH) / 2 - 15, (HEIGHT) / 2 - 15));

        // draws the world to the screen
        ter.renderFrame(world);
    }
}
