/** A class that represents a path via pursuit curves. */
public class Path {
    Point curr, next;
    public Path(double x, double y) {
        this(new Point(x, y));
    }
    public Path(Point next) {
        this(next, new Point());
    }
    // public Path(double x_next, double y_next, double x_curr, double y_curr) {
    //     this(new Point(x_next, y_next), new Point(x_curr, y_curr));
    // }
    public Path(Point next, Point curr) {
        this.next = next;
        this.curr = curr;
    }

    public double getCurrX() {
        return this.curr.getX();
    }
    public double getCurrY() {
        return this.curr.getY();
    }
    public double getNextX() {
        return this.next.getX();
    }
    public double getNextY() {
        return this.next.getY();
    }

    public Point getCurrentPoint() {
        return new Point(this.curr);
    }
    public void setCurrentPoint(Point point) {
        // this.setCurrentPoint(point.getX(), point.getY());
        this.curr = new Point(point);
    }
    private void setCurrentPoint(double x, double y) {
        // this.curr.setX(x);
        // this.curr.setY(y);
        this.curr = new Point(x, y);
    }
    private void setNextPoint(Point point) {
        // this.setNextPoint(point.getX(), point.getY());
        this.next = new Point(point);
    }
    private void setNextPoint(double x, double y) {
        // this.next.setX(x);
        // this.next.setY(y);
        this.next = new Point(x, y);
    }


    public void iterate(double dx, double dy) {
        this.setCurrentPoint(this.next);
        this.setNextPoint(this.getNextX() + dx, this.getNextY() + dy);
    }
}
