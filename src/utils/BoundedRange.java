package utils;

public class BoundedRange extends Range {

    int lowerBound;
    int upperBound;

    public BoundedRange(int min, int max, int lowerBound, int upperBound) {
        super(min, max);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public void setBounds(int lowerBound, int upperBound) {
        if (lowerBound > upperBound) {
            throw new ArithmeticException("range lowerBound cannot be greater than range upperBound");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int getLowerBound() {
        return this.lowerBound;
    }

    public int getUpperBound() {
        return this.upperBound;
    }

    @Override
    public void setValues(int min, int max) {
        if (min < this.lowerBound) {
            throw new ArithmeticException("low value is less than range lowerBound");
        } else if (max > this.upperBound) {
            throw new ArithmeticException("high value is less than range upperBound");
        }
        super.setValues(min, max);
    }

    @Override
    public int applyDelta(int delta) {
        int effectiveDelta = 0;
        if (delta > 0) {
            effectiveDelta = Math.min(this.upperBound - this.maximum, delta);
        } else if (delta < 0) {
            effectiveDelta = Math.max(this.lowerBound - this.minimum, delta);
        }
        super.applyDelta(effectiveDelta);
        return effectiveDelta;
    }

    @Override
    public String toString() {
        return "BoundedRange. min: " + minimum + ", max: " + maximum +  " lowerBound: " + lowerBound + " upperBound: " + upperBound;
    }
}
