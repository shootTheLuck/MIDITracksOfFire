package instruments;

public class Instrument {

    public String name;
    public int number;

    public Instrument(String s, int n) {
        name = s;
        number = n;
    }

    @Override
    public String toString() {
        return name;
    }
}
