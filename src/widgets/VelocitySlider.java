package widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

import utils.console;
import utils.BoundedRange;

public class VelocitySlider extends JSlider {

    int fontHeight = 14;
    int presentValue = 0;
    private Font fretFont = new Font("Sans", Font.PLAIN, fontHeight);
    FontMetrics fontMetrics;
    int displayNumberHeight;
    int displayNumberWidth;
    public BoundedRange range = new BoundedRange(0, 127, 0, 127);

    public VelocitySlider() {
        super(SwingConstants.VERTICAL);
        setBorder(BorderFactory.createLineBorder(Color.gray));
        fontMetrics = this.getFontMetrics(fretFont);
        displayNumberHeight = fontMetrics.getAscent();
        displayNumberWidth = fontMetrics.stringWidth("127");
        /* set size a little larger to ease reading and get thumb out from under pointer */
        setBounds(0, 0, 42, 130);
        setMaximum(127);
    }

    public void setVelocityRange(int lowest, int highest) {
        range.setValues(lowest, highest);
        if (lowest == highest) {
            setUI(new VelocitySliderUI(this, Color.red));
        } else {
            setUI(new VelocitySliderUIRange(this, Color.red, lowest, highest));
        }
    }

    @Override
    public void setValue(int value) {
        presentValue = value;
        super.setValue(value);
    }

    public void adjustValue(int value) {
        int delta = value - presentValue;
        int test = range.applyDelta(delta);
        if (test != 0) {
            presentValue = value;
            super.setValue(value);
        }
    };

    public int getMedianValue() {
        return range.getMedian();
    }

    class VelocitySliderUI extends BasicSliderUI {

        Color thumbColor;
        int thumbHeight = 40;

        Dimension thumbSize;

        public VelocitySliderUI(JSlider s, Color tColor) {
            super(s);
            thumbColor = tColor;
        }

        @Override
        protected Dimension getThumbSize() {
            Dimension d = super.getThumbSize();
            d.width = Math.max(d.width, displayNumberWidth + 3);
            d.height = Math.max(d.height, displayNumberHeight + 3);
            return d;
        }

        // override paint to inject our paint-thumb-with-number method:
        // https://coderanch.com/t/338457/java/JSlider-knob-color
        @Override
        public void paint( Graphics g, JComponent c )   {

            Rectangle clip = g.getClipBounds();
            getThumbSize();

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
        public void paintThumb(Graphics g, int value)  {
            int w = thumbRect.width;
            int h = thumbRect.height;

            g.translate(thumbRect.x, thumbRect.y);
            g.setFont(fretFont);

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

                // fill thumb with color
                g.fillRect(0, 0, w, h);

                // draw line bottom and right
                g.setColor(Color.black);
                g.drawLine(0, h-1, w-1, h-1);
                g.drawLine(w-1, 0, w-1, h-1);

                // draw line top and left
                g.setColor(getHighlightColor());
                g.drawLine(0, 0, 0, h-2);
                g.drawLine(1, 0, w-2, 0);

                g.setColor(getShadowColor());
                g.drawLine(1, h-2, w-2, h-2);
                g.drawLine(w-2, 1, w-2, h-3);
                g.drawRect(0, 0, w, h);

                g.setColor(Color.black);
                String velocityAmount = String.valueOf(range.getMinimum());
                int x = (thumbRect.width - fontMetrics.stringWidth(velocityAmount)) / 2;
                int y = (thumbRect.height - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();

                g.drawString(velocityAmount, x, y);
            }

            g.translate(-thumbRect.x, -thumbRect.y);
        }

        @Override
        public String toString() {
            return "velocitySlider";
        }
    }

    class VelocitySliderUIRange extends VelocitySliderUI {

        int min;
        int max;
        int linesApart;

        public VelocitySliderUIRange(JSlider s, Color tColor, int lowest, int highest) {
            super(s, tColor);
            this.min = lowest;
            this.max = highest;
        }

        @Override
        protected Dimension getThumbSize() {
            Dimension d = super.getThumbSize();
            d.width = Math.max(d.width, displayNumberWidth + 3);
            d.height = Math.max(d.height, displayNumberHeight * 2 + 6);
            d.height = Math.max(d.height, max - min + 2);
            return d;
        }

        /**
        * Paints the thumb with number
        * @param g the graphics
        */
        @Override
        public void paintThumb(Graphics g, int value)  {
            int w = thumbRect.width;
            int h = thumbRect.height;
            if (h > fontHeight * 2 + 4) {
                thumbRect.y = 127 - range.getMaximum();
            }
            g.translate(thumbRect.x, thumbRect.y);
            g.setFont(fretFont);

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

                // fill thumb with color
                g.fillRect(0, 0, w, h);

                // draw line bottom and right
                g.setColor(Color.black);
                g.drawLine(0, h-1, w-1, h-1);
                g.drawLine(w-1, 0, w-1, h-1);

                // draw line top and left
                g.setColor(getHighlightColor());
                g.drawLine(0, 0, 0, h-2);
                g.drawLine(1, 0, w-2, 0);

                g.setColor(getShadowColor());
                g.drawLine(1, h-2, w-2, h-2);
                g.drawLine(w-2, 1, w-2, h-3);
                g.drawRect(0, 0, w, h);

                g.setColor(Color.black);

                String upperAmount = String.valueOf(range.getMaximum());
                int x1 = (thumbRect.width - fontMetrics.stringWidth(upperAmount)) / 2;
                int y1 = fontHeight;

                g.drawString(upperAmount, x1, y1);

                String lowerAmount = String.valueOf(range.getMinimum());
                int x2 = (thumbRect.width - fontMetrics.stringWidth(lowerAmount)) / 2;
                int y2 = h - 4;

                g.drawString(lowerAmount, x2, y2);
            }

            g.translate(-thumbRect.x, -thumbRect.y);
        }
    }

}
