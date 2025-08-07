public class TriangleDrawer {
    public static void drawTriangle() {
        drawTriangle(5);
    }
    public static void drawTriangle(int size) {  // while version
        int i = 1;
        while (i <= size) {
            // int j = 1;
            // while (j <= size - i) {
            //     System.out.print(" ");
            //     j++;
            // }
            // int k = 1;
            // while (k <= (2 * i - 1)) {
            for (int k = 1; k <= i; k++) {
                System.out.print("*");
            }
            System.out.println();
            i++;
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
