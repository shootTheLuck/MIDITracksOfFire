package page;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.*;
import javax.swing.*;

import themes.Themes;
import utils.*;
import page.Page;

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

    public PageNumberBar(int height, int lMargin) {

        this.height = height;
        this.lMargin = lMargin;
        //this.page = page;
        //this.pageView = pageView;
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
                    int x = evt.getX();
                    Page.getInstance().handleMeasureSizeSlider(x - lMargin);
                }
            }
        });
    }

    private void drawNumbersAndLines(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        int tickMarkHeight = height/5;

        //TODO j < Page.numOfMeasures?
        for (int j = 0; j < PageView.width/PageView.measureSize; j++) {

            // draw measure numbers (starting with 1) centered by they're width
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
            lMargin + Page.numOfMeasures * PageView.measureSize,
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

    private void drawRectangle(Rectangle rect) {
        repaint(lMargin + rect.x - 1, rect.y - 3, rect.width + 2, rect.height + 6);
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
        measureSizeDragger.x = value + PageView.measureSize - measureSizeDragger.width/2 + lMargin;
        drawRectangle(measureSizeDragger);
    }

    public void adjustMeasureSize(int measureSize) {
        measureSizeDragger.x = measureSize - measureSizeDragger.width/2 + lMargin;
        playingMeasure.setSize(measureSize, 3);
    }

    public void setProgress(long tick, int ticksPerMeasure) {

        drawRectangle(playingMeasure);
        playingMeasure.x = (int) (tick / ticksPerMeasure * PageView.measureSize);
        drawRectangle(playingMeasure);

        drawRectangle(playingPosition);
        playingPosition.x = (int) ((double) tick / ticksPerMeasure * PageView.measureSize);
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


}
