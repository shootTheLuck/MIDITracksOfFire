package page;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import widgets.InputField;
import widgets.NumberInputField;


class PagePlayControls extends JPanel {

    protected JButton playButton;
    protected NumberInputField playStartField;
    protected JButton loopButton;
    protected NumberInputField loopStartField;
    protected NumberInputField loopStopField;
    protected NumberInputField BPMField;
    public JTextField infoField;

    private Icon playIcon = new ImageIcon("assets/media-playback-start.png");
    private Icon stopIcon = new ImageIcon("assets/media-playback-stop.png");
    private Icon loopIcon = new ImageIcon("assets/media-playlist-repeat2.png");

    public PagePlayControls(Page pageController) {

        Dimension playControlButtonSize = new Dimension(40, 28);
        Dimension numberFieldSize = new Dimension(50, 30);

        playButton = new JButton(playIcon);
        playButton.setPreferredSize(playControlButtonSize);
        playButton.setMaximumSize(playControlButtonSize);
        playButton.setFocusPainted(false);

        playButton.addActionListener((ActionEvent ae) -> {
            pageController.handlePlayControls(Constants.BUTTON_PLAY);
        });
        add(playButton);

        playStartField = new NumberInputField(1);
        playStartField.setMinimum(1);
        playStartField.setPreferredSize(numberFieldSize);
        playStartField.setMaximumSize(numberFieldSize);
        playStartField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pageController.handlePlayControls(Constants.FIELD_PLAYSTART);
            }
        });
        add(playStartField);

        loopButton = new JButton(loopIcon);
        loopButton.setPreferredSize(playControlButtonSize);
        loopButton.setMaximumSize(playControlButtonSize);
        loopButton.setFocusPainted(false);

        loopButton.addActionListener((ActionEvent ae) -> {
            //pageController.handleLoopButton(playAction);
        });
        add(loopButton);

        loopStartField = new NumberInputField(1);
        loopStartField.setPreferredSize(numberFieldSize);
        loopStartField.setMaximumSize(numberFieldSize);
        loopStartField.setMinimum(1);
        loopStartField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pageController.handlePlayControls(Constants.FIELD_LOOPSTART);
            }
        });
        add(loopStartField);

        JLabel label = new JLabel(" - ");
        add(label);

        loopStopField = new NumberInputField(2);
        loopStopField.setPreferredSize(numberFieldSize);
        loopStopField.setMaximumSize(numberFieldSize);
        loopStopField.setMinimum(2);
        loopStopField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pageController.handlePlayControls(Constants.FIELD_LOOPSTOP);
            }
        });
        add(loopStopField);


        add(Box.createHorizontalGlue());

        JLabel BPMlabel = new JLabel(" BPM ");
        //BPMlabel.setFocusable(true);
        add(BPMlabel);

        BPMField = new NumberInputField(120);
        BPMField.setPreferredSize(new Dimension(150, 30));
        BPMField.setMaximumSize(new Dimension(150, 30));
        BPMField.setMinimum(1);
        BPMField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pageController.handlePlayControls(Constants.FIELD_BPM);
            }
        });
        add(BPMField);

        infoField = new InputField();
        infoField.setPreferredSize(new Dimension(150, 30));
        infoField.setMaximumSize(new Dimension(150, 30));
        infoField.setText("info");

        add(infoField);
    }

    protected void togglePlayButton(Constants c) {
        if (c == Constants.BUTTON_STOP) {
            playButton.setIcon(stopIcon);
        } else {
            playButton.setIcon(playIcon);
        }
    }

    protected int getPlayStartField() {
        return playStartField.getValue();
    }

    protected void setPlayStartField(int value) {
        playStartField.setValue(value);
    }

    protected int getBPMField() {
        return BPMField.getValue();
    }

    protected void setBPMField(int bpm) {
        BPMField.setValue(bpm);
    }

}