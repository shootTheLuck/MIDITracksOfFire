package page;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import themes.ThemeReader;
import utils.console;

class PageNumberBar extends JPanel {

    private boolean dragging = false;
    private Page page;

    private Font font = new Font("Dialog", Font.PLAIN, 11);
    private FontMetrics fontMetrics = getFontMetrics(font);

    private int scrollPosition;
    private int indicatorHeight;
    private int lMargin;
    private int height;

    private Rectangle measureSizeDragger;
    private Rectangle playingMeasure;
    private Rectangle playingPosition;

    public PageNumberBar(Page page, int height, int lMargin) {
        this.page = page;
        this.height = height;
        this.lMargin = lMargin;

        measureSizeDragger = new Rectangle(new Dimension(20, 20));
        measureSizeDragger.x = PageView.measureSize - measureSizeDragger.width/2 + lMargin;
        indicatorHeight = height/10;

        playingMeasure = new Rectangle(new Dimension(PageView.measureSize, indicatorHeight));
        playingMeasure.setLocation(-1000, height - indicatorHeight);

        playingPosition = new Rectangle(new Dimension(2, indicatorHeight));
        playingPosition.setLocation(-1000, height - indicatorHeight);

        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                int x = evt.getX();
                int y = evt.getY();
                if (measureSizeDragger.contains(x, y)) {
                    dragging = true;
                }
            }
            public void mouseReleased(MouseEvent evt) {
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                if (dragging) {
                    int newValue = evt.getX() + getX() - lMargin;
                    page.handleMeasureSizeSlider(newValue);
                }
            }
        });
    }

    private void drawNumbersAndLines(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(font);
        int tickMarkHeight = height/5;

        //TODO j < Page.numOfMeasures?
        for (int j = 0; j < PageView.width/PageView.measureSize; j++) {

            // draw measure numbers (starting with 1) centered by their width
            String string = String.valueOf(j + 1);
            int width = fontMetrics.stringWidth(string);
            g2.drawString(string, lMargin + j * PageView.measureSize - width/2, height * 2/3);

            // draw vertical tick marks at each measure
            g2.drawLine(
                lMargin + j * PageView.measureSize,
                height - tickMarkHeight,
                lMargin + j * PageView.measureSize,
                height);
        }

        // draw horizontal line
        g2.drawLine(
            lMargin,
            height - indicatorHeight,
            lMargin + PageView.width,
            height - indicatorHeight);

        // draw dragger rectangle
        g2.drawRect(
            measureSizeDragger.x,
            height/2 - measureSizeDragger.height/2,
            measureSizeDragger.width,
            measureSizeDragger.height);
    }

    private void drawPlayingIndicators(Graphics2D g2) {
        g2.setColor(ThemeReader.getColor("page.numberBar.playingMeasure"));
        g2.fillRect(lMargin + playingMeasure.x, playingMeasure.y, playingMeasure.width, playingMeasure.height);
        g2.setColor(Color.white);
        g2.fillRect(lMargin + playingPosition.x, playingPosition.y, playingPosition.width, playingMeasure.height);
    }

    private void drawRectangle(Rectangle rect) {
        repaint(lMargin + rect.x - 1, rect.y - 3, rect.width + 2, rect.height + 6);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        drawNumbersAndLines(g2);
        drawPlayingIndicators(g2);
        g2.dispose();
        getToolkit().sync();
    }

    public void setScrollPosition(int value) {
        drawRectangle(measureSizeDragger);
        setLocation(-value, 0);
        measureSizeDragger.x = value + PageView.measureSize - measureSizeDragger.width/2 + lMargin;
        drawRectangle(measureSizeDragger);
    }

    public void adjustMeasureSize(int measureSize) {
        measureSizeDragger.x = measureSize - measureSizeDragger.width/2 + lMargin;
        playingMeasure.width = measureSize;
    }

    public void setProgress(double progress) {
        drawRectangle(playingMeasure);
        playingMeasure.x = (int)Math.floor(progress) * PageView.measureSize;
        drawRectangle(playingMeasure);

        drawRectangle(playingPosition);
        playingPosition.x = (int)(progress* PageView.measureSize);
        drawRectangle(playingPosition);
    }

    public void cancelProgress() {
        drawRectangle(playingMeasure);
        playingMeasure.x = -10000;
        //drawRectangle(playingMeasure);

        drawRectangle(playingPosition);
        playingPosition.x = -10000;
        //drawRectangle(playingPosition);
    }


}
