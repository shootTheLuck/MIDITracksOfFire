package widgets;

import java.awt.event.*;

import javax.swing.JTextField;
import javax.swing.Timer;


public class NumberInputField extends InputField {

    private int value = 0;
    int minimum = Integer.MIN_VALUE;
    int maximum = Integer.MAX_VALUE;
    String regex = "[0-9- ]+";

    public NumberInputField() {
        super();
        setHorizontalAlignment(JTextField.RIGHT);
        addListeners();
    }

    public NumberInputField(int value) {
        super(String.valueOf(value));
        this.value = value;
        setHorizontalAlignment(JTextField.RIGHT);
        addListeners();
    }

    public NumberInputField(int value, int size) {
        super(String.valueOf(value), size);
        this.value = value;
        setHorizontalAlignment(JTextField.RIGHT);
        addListeners();
    }

    public NumberInputField(int value, int size, int minimum, int maximum) {
        super(String.valueOf(value), size);
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        setHorizontalAlignment(JTextField.RIGHT);
        addListeners();
    }

    private void addListeners() {

        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                //checkKeyStroke();
            }

            public void keyTyped(KeyEvent evt) {}

            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                    check();
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent evt) {
            }
            public void focusLost(FocusEvent evt) {
                check();
            }
        });

    }

    public void setValue(int value) {
        this.value = value;
        setText(String.valueOf(value));
    }

    public int getValue() {
        return this.value;
    }

    public void setMinimum(int min) {
        this.minimum = min;
    }

    public int getMinimum() {
        return this.minimum;
    }

    public void setMaximum(int max) {
        this.maximum = max;
    }

    public int getMaximum() {
        return this.maximum;
    }

    // https://stackoverflow.com/questions/11129951/flashing-color-of-a-jtextfield/11130842#11130842
    private void flash() {

        int timerDelay = 100;
        int totalTime = 400;
        final int totalCount = totalTime / timerDelay;
        Timer timer = new javax.swing.Timer(timerDelay, new ActionListener() {
            int count = 0;

            public void actionPerformed(ActionEvent evt) {
                if (count % 2 == 0) {
                    selectAll();
                    //setBackground(flashColor);
                } else {
                    //setBackground(null);
                    select(0, 0);
                    if (count >= totalCount) {
                        ((Timer)evt.getSource()).stop();
                    }
                }
                count++;
            }
        });
        timer.start();
    }

    //https://stackoverflow.com/questions/1313390/is-there-any-way-to-accept-only-numeric-values-in-a-jtextfield
    //@Override
    //public void processKeyEvent(KeyEvent ev) {
        //char c = ev.getKeyChar();
        //console.log("trying", ev);
        //super.processKeyEvent(ev);
    //}

    private void checkKeyStroke() {
        String s = getText().replaceAll("[^0-9]", "");
        if (!s.equals(getText())) {
            setText(s);
            flash();
        } else {
            //console.log("check ok");
        }
    }

    private void check() {

        try {
            setValue(Integer.parseInt(getText()));
        } catch(NumberFormatException err) {
            setValue(getValue());
        }

        if (Integer.valueOf(getText()) > maximum) {
            setValue(maximum);
        } else if (Integer.valueOf(getText()) < minimum) {
            setValue(minimum);
        }

    }

    @Override
    public void paste() {
        super.paste();
        check();
    }

    @Override
    public String toString() {
        return "NumberInputField " + getValue();
    }

}
