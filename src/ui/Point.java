package ui;

public class Point {
    public int y;
    public int x;

    public Point(int y, int x) {
        this.y = y;
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Point other = (Point) o;
        return (this.x == other.x && this.y == other.y);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 3100 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "y=" + y + ",x=" + x;
    }
}
