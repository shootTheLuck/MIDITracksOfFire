package widgets;

import javax.swing.Box;
import javax.swing.JFrame;


public class RemoveBarsDialog extends Dialog {

    private NumberInputField xField;
    private NumberInputField yField;
    private TrackRadioButtons trackRadioButtons;

    public int from;
    public int to;
    public boolean allTracks;

    public RemoveBarsDialog(JFrame owner, int x, int y) {
        super(owner);
        xField = new NumberInputField(x, 3);
        yField = new NumberInputField(y, 3);

        contents.add(new BoldLabel("Remove Bars"));
        contents.add(xField);
        contents.add(Box.createHorizontalStrut(2)); // spacer
        contents.add(new BoldLabel("to"));
        contents.add(Box.createHorizontalStrut(2)); // spacer
        contents.add(yField);

        contents.add(Box.createHorizontalStrut(20)); // spacer
        trackRadioButtons = new TrackRadioButtons();
        contents.add(trackRadioButtons);

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
        if (trackRadioButtons.allTracksButton.isSelected()) {
            allTracks = true;
        } else {
            allTracks = false;
        }
    }

    public int[] getValue() {
        int[] value = {from, to};
        return value;
    }


}