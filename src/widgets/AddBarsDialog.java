package widgets;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLabel;
import javax.swing.Timer;

import utils.*;


class BoldLabel extends JLabel {

    public BoldLabel(String text) {
        super(text);
        setFont(new Font("Courier New", Font.BOLD, 12));
    }

}


public class AddBarsDialog extends JDialog {

    NumberInputField xField;
    NumberInputField yField;
    int numberToAdd;
    int addBefore;

    public AddBarsDialog(JFrame owner) {
        super(owner, true);
        xField = new NumberInputField(1, 3);


        //xField.addKeyListener(new KeyAdapter() {
            //public void keyReleased(KeyEvent evt) {
            //}

            //public void keyTyped(KeyEvent evt) {}

            //public void keyPressed(KeyEvent evt) {
                //if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                    //showIt();
                //}
            //}
        //});

        yField = new NumberInputField(3, 3);
        JPanel contents = new JPanel();
        contents.add(new BoldLabel("Insert"));
        contents.add(xField);
        contents.add(Box.createHorizontalStrut(5)); // a spacer
        contents.add(new BoldLabel("bars before measure:"));
        contents.add(yField);

        JPanel buttons = new JPanel();

        JButton enterButton = new JButton("Enter");
        enterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                numberToAdd = xField.getValue();
                addBefore = yField.getValue();
                dispose();
            }
        });
        buttons.add(enterButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                numberToAdd = 0;
                addBefore = 0;
                dispose();
            }
        });
        buttons.add(cancelButton);
        contents.add(buttons, BorderLayout.PAGE_END);
        JPanel main = new JPanel(new BorderLayout());
        main.add(contents, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.PAGE_END);
        add(main);

        // @HoverCraft Full Of Eels
        // https://stackoverflow.com/a/22325697
        int condition = JPanel.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = ((JPanel) getContentPane()).getInputMap(condition);
        ActionMap actionMap = ((JPanel) getContentPane()).getActionMap();
        String enter = "enter";
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enter);
        actionMap.put(enter, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                numberToAdd = xField.getValue();
                addBefore = yField.getValue();
                dispose();
            }
        });

        pack();
        setSize(400, 150);
        setLocationRelativeTo(owner);
        setVisible(true);
        xField.requestFocusInWindow();
    }

    public int[] getValue() {
        int[] value = {numberToAdd, addBefore};
        return value;
    }

}