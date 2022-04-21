package track;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

import themes.ThemeReader;
import utils.console;


class TrackSideBar extends JPanel {

    private Font font;
    private FontMetrics fontMetrics;
    private int fontHeight;
    private String[] content;

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