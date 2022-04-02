package widgets;

import javax.swing.Box;
import javax.swing.JFrame;


public class InsertBarsDialog extends Dialog {

    NumberInputField xField;
    NumberInputField yField;
    int numberToAdd;
    int addBefore;

    public InsertBarsDialog(JFrame owner, int x, int y) {
        super(owner);
        xField = new NumberInputField(x, 3);
        yField = new NumberInputField(y, 3);

        contents.add(new BoldLabel("Insert"));
        contents.add(xField);
        contents.add(Box.createHorizontalStrut(5)); // a spacer
        contents.add(new BoldLabel("bars before measure"));
        contents.add(yField);

        xField.requestFocusInWindow();

    }

    @Override
    protected void actionOnCancel() {
        numberToAdd = 0;
        addBefore = 0;
    }

    @Override
    protected void actionOnEnter() {
        numberToAdd = xField.getValue();
        addBefore = yField.getValue();
    }

    public int[] getValue() {
        int[] value = {numberToAdd, addBefore};
        return value;
    }

}