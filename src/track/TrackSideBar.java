package track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import note.Note;
import page.PageView;
import themes.ThemeReader;
import utils.console;
import widgets.NumberInputField;


class TrackSideBar extends JPanel {

    private Font font;
    private FontMetrics fontMetrics;
    private int fontHeight;
    private String[] content = {};

    public TrackSideBar() {
        font = new Font("MonoSpace", Font.PLAIN, 9);
        fontMetrics = getFontMetrics(font);
        fontHeight = fontMetrics.getAscent();
    }

    protected void setContent(String[] array) {
        this.content = array;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(font);
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int stringSpacing = ThemeReader.getMeasure("track.strings.spacing");
        int totalWidth = ThemeReader.getMeasure("track.strings.margin.left");

        for (int i = 0; i < content.length; i++) {
            String string = content[i];
            int stringWidth = fontMetrics.stringWidth(string);
            g.drawString(string, totalWidth - stringWidth - 3, topMargin + fontHeight/2 + i * stringSpacing - 1);
        }
    }

}