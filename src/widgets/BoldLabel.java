
package widgets;

import java.awt.Font;

import javax.swing.JLabel;

class BoldLabel extends JLabel {

    public BoldLabel(String text) {
        super(text);
        setFont(new Font("Courier New", Font.BOLD, 12));
    }

}
