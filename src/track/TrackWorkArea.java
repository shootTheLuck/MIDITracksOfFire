package track;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.FlowLayout;

public class TrackWorkArea extends JPanel {

    private JPanel side;
    protected TrackDrawArea drawArea;
    private TrackController controller;

    public TrackWorkArea(TrackController controller, int numOfStrings) {

        this.controller = controller;
        //JPanel panel = new JPanel();

        int leftMargin = 40;

        side = new TrackSideBar();
        //setComponentSize(side, leftMargin, 100);
        add(side);

        JButton next = new JButton("wowowo");
        add(next);

        drawArea = new TrackDrawAreaGuitar(controller, 6);
        drawArea.setBackground(Color.red);
        add(drawArea);
        //pack();
        //validate();
        //repaint();
    }

}