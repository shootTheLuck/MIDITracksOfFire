package widgets;

import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;



public class UnsavedDialog extends JDialog {

    JPanel contents;
    JButton saveButton;
    JButton closeWithoutButton;
    JButton cancelButton;
    public String value;

    public UnsavedDialog(JFrame owner) {
        super(owner, "", true);

        setLayout(new BorderLayout());
        contents = new JPanel();

        JPanel buttons = new JPanel();

        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                value = "save";
                actionOnEnter();
                dispose();
            }
        });
        buttons.add(saveButton);

        closeWithoutButton = new JButton("Close Without Saving");
        closeWithoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                value = "close";
                actionOnCancel();
                dispose();
            }
        });
        buttons.add(closeWithoutButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                value = "cancel";
                actionOnCancel();
                dispose();
            }
        });
        buttons.add(cancelButton);

        add(Box.createRigidArea(new Dimension(1, 15)), BorderLayout.PAGE_START);
        add(contents, BorderLayout.CENTER);
        add(buttons, BorderLayout.PAGE_END);

        pack();
        setSize(480, 150);
        setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(saveButton);
    }

    //@Override
    protected void actionOnCancel() {
        //numberToAdd = 0;
        //addBefore = 0;
    }

    //@Override
    protected void actionOnEnter() {
        //numberToAdd = xField.getValue();
        //addBefore = yField.getValue();
        //if (trackRadioButtons.allTracksButton.isSelected()) {
            //allTracks = true;
        //} else {
            //allTracks = false;
        //}
    }

    public String getValue() {
        return value;
    }

}