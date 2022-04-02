package widgets;

import javax.swing.Box;
import javax.swing.JFrame;


public class RemoveBarsDialog extends Dialog {

    NumberInputField xField;
    NumberInputField yField;
    int from;
    int to;

    public RemoveBarsDialog(JFrame owner, int x, int y) {
        super(owner);
        xField = new NumberInputField(x, 3);
        yField = new NumberInputField(y, 3);

        contents.add(new BoldLabel("Remove Bars"));
        contents.add(xField);
        contents.add(Box.createHorizontalStrut(5)); // a spacer
        contents.add(new BoldLabel("to"));
        contents.add(yField);

        xField.requestFocusInWindow();
    }

    @Override
    protected void actionOnCancel() {
        from = 0;
        to = 0;
    }

    @Override
    protected void actionOnEnter() {
        from = xField.getValue();
        to = yField.getValue();
    }

    public int[] getValue() {
        int[] value = {from, to};
        return value;
    }

}