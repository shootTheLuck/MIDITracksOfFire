package widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


class Dialog extends JDialog {

    JPanel contents;
    JButton enterButton;
    JButton cancelButton;

    public Dialog(JFrame owner) {
        super(owner);

        setLayout(new BorderLayout());
        contents = new JPanel();

        JPanel buttons = new JPanel();

        enterButton = new JButton("Enter");
        enterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionOnEnter();
                dispose();
            }
        });
        buttons.add(enterButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
        getRootPane().setDefaultButton(enterButton);
    }

    protected void actionOnCancel() {}

    protected void actionOnEnter() {}

}