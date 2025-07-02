public class TriangleDrawer2 {
    public static void drawTriangle() {
        drawTriangle(5);
    }
    public static void drawTriangle(int SIZE) { // for version
        for (int i = 1; i <= SIZE; i++) {
            // for (int j = 1; j <= SIZE - i; j++) {
            //     System.out.print(" ");
            // }
            // for (int k = 1; k <= (2 * i - 1); k++) {
            for (int k = 1; k <= i; k++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int size = 5; // default

        if (args.length >= 1) {
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Warning: argument not an integer, using default size 5.");
            }
        }

        drawTriangle(size);
    }
}
