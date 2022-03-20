package widgets;

import java.awt.event.*;
import javax.swing.JTextField;


public class InputField extends JTextField {

    public boolean isFocused = false;

    // not using
    KeyAdapter loseFocusOnEnter = new KeyAdapter() {
        public void keyPressed(KeyEvent evt) {
            if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                setFocusable(false);
                setFocusable(true);
            }
        }
    };

    // not using
    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            setFocusable(false);
            setFocusable(true);
        }
    };

    FocusListener focusListener = new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
            isFocused = true;
            selectAll();
        }
        public void focusLost(FocusEvent e) {
            isFocused = false;
        }
    };

    public InputField() {
        addFocusListener(focusListener);
        //addKeyListener(loseFocusOnEnter);
    }

    public InputField(String text) {
        super(text);
        addFocusListener(focusListener);
        //addKeyListener(loseFocusOnEnter);
    }

    public InputField(String text, int columns) {
        super(text, columns);
        addFocusListener(focusListener);
        //addKeyListener(loseFocusOnEnter);
    }


}