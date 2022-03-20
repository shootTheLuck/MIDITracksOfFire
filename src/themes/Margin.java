package themes;

public class Margin {
    public int top;
    public int left;
    public int bottom;
    public int right;

    public Margin(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public int get(String which) {
        switch (which) {

            case "top" :
                return top;

            case "left" :
                return left;

            case "bottom" :
                return bottom;

            case "right" :
                return right;

            default:
                return -1;
        }
    }
}
