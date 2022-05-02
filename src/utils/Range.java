package utils;

public class Range {

    int minimum;
    int maximum;

    public Range(int min, int max) {
        if (min > max) {
            throw new ArithmeticException("range minimum cannot be greater than range maximum");
        }
        this.minimum = min;
        this.maximum = max;
    }

    public void setValues(int min, int max) {
        if (min > max) {
            throw new ArithmeticException("range minimum cannot be greater than range maximum");
        }
        this.minimum = min;
        this.maximum = max;
    }

    public int getMinimum() {
        return this.minimum;
    }

    public int getMaximum() {
        return this.maximum;
    }

    public int getMedian() {
        return (this.minimum + this.maximum) / 2;
    }

    public int getSize() {
        return (this.maximum - this.minimum);
    }

    public int applyDelta(int delta) {
        this.minimum += delta;
        this.maximum += delta;
        return delta;
    }

    @Override
    public String toString() {
        return "Range min: " + minimum + ",max: " + maximum;
    }
}