package widgets;

import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JSlider;
//import javax.swing.plaf.basic.BasicSliderUI;

public class VelocitySlider extends JSlider {

    public VelocitySlider() {
        super(SwingConstants.VERTICAL);
        setUI(new VelocitySliderUI(this, Color.red));
    }

    public void setDisplay(int v) {
        SliderUI ui = getUI();
        BasicSliderUI bui = (BasicSliderUI) ui;
        VelocitySliderUI vui = (VelocitySliderUI) bui;
        vui.setDisplay(v);
    }

}

class VelocitySliderUI extends BasicSliderUI {

    Color thumbColor;
    int displayValue = 0;

    private Font fretFont = new Font("Sans", Font.PLAIN, 10);
    public VelocitySliderUI(JSlider s, Color tColor) {
        super(s);
        thumbColor = tColor;
    }

    public void setDisplay(int v) {
        displayValue = v;
    }

    // override paint to inject our paint-thumb-with-number method:
    // https://coderanch.com/t/338457/java/JSlider-knob-color

    @Override
    public void paint( Graphics g, JComponent c )   {

        // recalculateIfInsetsChanged();
        // recalculateIfOrientationChanged();

        Rectangle clip = g.getClipBounds();

        if (!clip.intersects(trackRect) && slider.getPaintTrack()) {
            calculateGeometry();
        }

        if (slider.getPaintTrack() && clip.intersects(trackRect)) {
            paintTrack(g); // this one used
        }

        if (slider.getPaintTicks() && clip.intersects(tickRect)) {
            paintTicks(g);
        }

        if (slider.getPaintLabels() && clip.intersects(labelRect)) {
            paintLabels(g);
        }

        if (slider.hasFocus() && clip.intersects(focusRect)) {
            paintFocus(g);
        }

        if (clip.intersects(thumbRect)) {
            paintThumb(g, slider.getValue()); // this one used
        }
    }

    /**
    * Paints the thumb with number
    * @param g the graphics
    */
    //@Override
    public void paintThumb(Graphics g, int value)  {
        Rectangle knobBounds = thumbRect;
        int w = knobBounds.width;
        int h = knobBounds.height;

        g.translate(knobBounds.x, knobBounds.y);
        g.setFont(fretFont);
        FontMetrics fMetrics = g.getFontMetrics(fretFont);

        String v = String.valueOf(displayValue);

        int fWidth = fMetrics.stringWidth(v);
        int fHeight = fMetrics.getAscent();

        int x = -5 + knobBounds.x + (knobBounds.width - fMetrics.stringWidth(v)) / 2;
        int y = 0 + (knobBounds.height - fMetrics.getHeight()) / 2 + fMetrics.getAscent();

        if (slider.isEnabled() ) {
            g.setColor(slider.getBackground());
        }
        else {
            g.setColor(slider.getBackground().darker());
        }

        Boolean paintThumbArrowShape =
            (Boolean)slider.getClientProperty("Slider.paintThumbArrowShape");

        if ((!slider.getPaintTicks() && paintThumbArrowShape == null) ||
            paintThumbArrowShape == Boolean.FALSE) {
            // "plain" version
            g.fillRect(0, 0, w, h);

            g.setColor(Color.black);
            g.drawLine(0, h-1, w-1, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.setColor(getHighlightColor());
            g.drawLine(0, 0, 0, h-2);
            g.drawLine(1, 0, w-2, 0);

            g.setColor(getShadowColor());
            //g.drawLine(1, h-2, w-2, h-2);
            //g.drawLine(w-2, 1, w-2, h-3);
            //g.drawRect(0, 0, w, h);

            g.setColor(Color.black);
            g.drawString(v, x, y);
        }

        g.translate(-knobBounds.x, -knobBounds.y);
    }

    @Override
    public String toString() {
        return "wowo";
    }
}
