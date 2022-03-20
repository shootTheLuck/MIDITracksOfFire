package page;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import themes.Themes;
import utils.*;

class PageNumberBar extends JPanel {

    private boolean dragging = false;

    private Font font = new Font("Dialog", Font.PLAIN, 11);
    private FontMetrics fontMetrics = getFontMetrics(font);

    private int indicatorHeight;
    private int lMargin;
    private int height;

    private Page page;

    private Rectangle measureSizeDragger;
    private Rectangle playingMeasure;
    private Rectangle playingPosition;

    public PageNumberBar(int height, int lMargin, Page page) {

        this.height = height;
        this.lMargin = lMargin;
        this.page = page;
        measureSizeDragger = new Rectangle(new Dimension(20, 20));
        measureSizeDragger.x = Page.measureSize - measureSizeDragger.width/2 + lMargin;
        indicatorHeight = height/10;

        playingMeasure = new Rectangle(new Dimension(Page.measureSize, indicatorHeight));
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
                    int x = evt.getX();
                    page.handleMeasureSizeSlider(x - lMargin);
                }
            }
        });
    }

    private void drawNumbersAndLines(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        int tickMarkHeight = height/5;

        //TODO j < Page.numOfMeasures?
        for (int j = 0; j < Page.width/Page.measureSize; j++) {

            // draw measure numbers (starting with 1) centered by they're width
            String string = String.valueOf(j + 1);
            int width = fontMetrics.stringWidth(string);
            g2.drawString(string, lMargin + j * Page.measureSize - width/2, height * 2/3);

            // draw vertical tick marks at each measure
            g2.drawLine(
                lMargin + j * Page.measureSize,
                height - tickMarkHeight,
                lMargin + j * Page.measureSize,
                height);
        }

        // draw horizontal line
        g2.drawLine(
            lMargin,
            height - indicatorHeight,
            lMargin + Page.numOfMeasures * Page.measureSize,
            height - indicatorHeight);

        // draw dragger rectangle
        g2.drawRect(
            measureSizeDragger.x,
            height/2 - measureSizeDragger.height/2,
            measureSizeDragger.width,
            measureSizeDragger.height);
    }

    private void drawPlayingIndicators(Graphics2D g2) {
        g2.setColor(Themes.colors.get("playingMeasure"));
        g2.fillRect(lMargin + playingMeasure.x, playingMeasure.y, playingMeasure.width, playingMeasure.height);
        g2.setColor(Color.white);
        g2.fillRect(lMargin + playingPosition.x, playingPosition.y, playingPosition.width, playingMeasure.height);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawNumbersAndLines(g2);
        drawPlayingIndicators(g2);
        g2.dispose();
        getToolkit().sync();
    }

    public void setScrollPosition(int value) {
        drawRectangle(measureSizeDragger);
        setLocation(-value, 0);
        measureSizeDragger.x = value + Page.measureSize - measureSizeDragger.width/2 + lMargin;
        drawRectangle(measureSizeDragger);
    }

    public void adjustMeasureSize() {
        measureSizeDragger.x = Page.measureSize - measureSizeDragger.width/2 + lMargin;
        playingMeasure.setSize(Page.measureSize, 3);
    }

    public void setProgress(long tick) {
        int ticksPerMeasure = page.getTicksPerMeasure();

        drawRectangle(playingMeasure);
        playingMeasure.x = (int) (tick / ticksPerMeasure * Page.measureSize);
        drawRectangle(playingMeasure);

        drawRectangle(playingPosition);
        playingPosition.x = (int) ((double) tick / ticksPerMeasure * Page.measureSize);
        drawRectangle(playingPosition);
    }

    public void cancelProgress() {
        drawRectangle(playingMeasure);
        playingMeasure.x = -10000;
        //drawRectangle(playingMeasure);

        drawRectangle(playingPosition);
        playingPosition.x = -10000;
        drawRectangle(playingPosition);
    }

    private void drawRectangle(Rectangle rect) {
        repaint(lMargin + rect.x - 1, rect.y - 3, rect.width + 2, rect.height + 6);
    }

}
