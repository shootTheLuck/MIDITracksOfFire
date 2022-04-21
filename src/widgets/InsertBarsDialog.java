package widgets;

import javax.swing.Box;
import javax.swing.JFrame;


public class InsertBarsDialog extends Dialog {

    private NumberInputField xField;
    private NumberInputField yField;
    private TrackRadioButtons trackRadioButtons;

    public int numberToAdd;
    public int addBefore;
    public boolean allTracks;

    public InsertBarsDialog(JFrame owner, int x, int y) {
        super(owner, "", false);
        xField = new NumberInputField(x, 3);
        yField = new NumberInputField(y, 3);

        contents.add(new BoldLabel("Insert"));
        contents.add(xField);
        //contents.add(Box.createHorizontalStrut(2)); // spacer
        contents.add(new BoldLabel("bars before measure"));
        //contents.add(Box.createHorizontalStrut(2)); // spacer
        contents.add(yField);

        contents.add(Box.createHorizontalStrut(10)); // spacer
        trackRadioButtons = new TrackRadioButtons();
        contents.add(trackRadioButtons);

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
        if (trackRadioButtons.allTracksButton.isSelected()) {
            allTracks = true;
        } else {
            allTracks = false;
        }
    }

    public int[] getValue() {
        int[] value = {numberToAdd, addBefore};
        return value;
    }

}