package widgets;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


public class TrackRadioButtons extends JPanel {

    ButtonGroup buttonGroup;
    public JRadioButton thisTracksButton;
    public JRadioButton allTracksButton;

    public TrackRadioButtons() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        String thisTrackString = "this track";
        thisTracksButton = new JRadioButton(thisTrackString);
        thisTracksButton.setActionCommand(thisTrackString);

        String allTracksString = "all tracks";
        allTracksButton = new JRadioButton(allTracksString);
        allTracksButton.setActionCommand(allTracksString);
        allTracksButton.setSelected(true);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(thisTracksButton);
        buttonGroup.add(allTracksButton);

        add(thisTracksButton);
        add(allTracksButton);

    }

}