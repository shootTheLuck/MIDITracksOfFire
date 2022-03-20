package track;

import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;

public class VelocitySlider extends JSlider {

    public VelocitySlider() {
        super(SwingConstants.VERTICAL);
    }

    public void setDisplay(int v) {
        SliderUI ui = getUI();
        BasicSliderUI bui = (BasicSliderUI) ui;
        VelocitySliderUI vui = (VelocitySliderUI) bui;
        vui.setDisplay(v);
    }

}
