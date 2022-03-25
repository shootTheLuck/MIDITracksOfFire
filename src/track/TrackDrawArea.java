package track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.MaskFormatter;

import page.Page;
import page.PageView;
import note.Note;
import themes.*;
import utils.*;


class TrackDrawArea extends JLayeredPane {

    //private String fretNum;

    private TrackType trackType;
    private TrackController controller;
    private float[] dash1 = { 2F, 2F };
    private BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 1.0f, dash1, 2f);

    private double zoomFactor = 1;
    private double prevZoomFactor = 1;
    private boolean zoomer = false;

    private Line2D progressLine;
    private Font fretFont = new Font("Dialog", Font.PLAIN, 11);
    private FontMetrics fontMetrics = getFontMetrics(fretFont);
    private int fontHeight = fontMetrics.getAscent();

    public TrackDrawArea(TrackController controller, TrackType trackType) {

        this.controller = controller;
        this.trackType = trackType;
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.black, 1));

        int trackHeight = Themes.getTrackHeight(trackType.numOfStrings);
        progressLine = new Line2D.Double(-1000, 0, -1000, trackHeight);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                controller.handleMouseDownDrawArea(evt);
            }
            public void mouseReleased(MouseEvent evt) {
                controller.handleMouseUpDrawArea(evt);
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent evt) {
                controller.handleMouseMoveDrawArea(evt);
            }
        });

    }

    private void drawDrumLines(Graphics2D g) {
        int measureSize = PageView.measureSize;
        int topMargin = Themes.margin.get("top");
        int lineSpacing = Themes.getLineSpacing();

        g.setColor(Color.gray);
        g.fillRect(0, 10, PageView.width, trackType.numOfStrings * lineSpacing * 2);

        g.setColor(Color.white);
        //g.setStroke(new BasicStroke(trackType.noteDrawHeight));
        g.setStroke(new BasicStroke(Themes.getDrumNoteHeight()));
        for (int i = 0; i < trackType.numOfStrings; i++) {
            g.drawLine(
                0,
                topMargin + i * lineSpacing,
                PageView.width,
                topMargin + i * lineSpacing);
        }
        g.setStroke(new BasicStroke(1));

    }

    private void drawLines(Graphics2D g) {
        int measureSize = PageView.measureSize;
        int topMargin = Themes.margin.get("top");
        int lineSpacing = Themes.getLineSpacing();

        g.setColor(Themes.colors.get("strings"));
        g.setStroke(new BasicStroke(1));

        for (int i = 0; i < trackType.numOfStrings; i++) {
            g.drawLine(
                0,
                topMargin + i * lineSpacing,
                PageView.width,
                topMargin + i * lineSpacing);
        }

        g.setColor(Themes.colors.get("gridLines"));
        for (int j = 0; j < 4 * PageView.width/measureSize; j++) {
            g.drawLine(
                (int)(measureSize/4.0 * j),
                topMargin,
                (int)(measureSize/4.0 * j),
                topMargin + lineSpacing * (trackType.numOfStrings - 1));
        }

        g.setColor(Themes.colors.get("barLines"));
        for (int i = 0; i < 1 + PageView.width/measureSize; i++) {
            g.drawLine(
                measureSize * i,
                topMargin,
                measureSize * i,
                topMargin + lineSpacing * (trackType.numOfStrings - 1));
        }

    }

    private void drawTriangle(Graphics2D g2, Note note, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D path = new Path2D.Double();

        int x = getNoteX(note.start) + 0;
        int y = getNoteY(note.stringNum);
        //int width = getNoteWidth(note.duration);

        int stringY = y + Themes.getDrumNoteHeight()/2;
        path.moveTo(x, stringY - Themes.getDrumNoteHeight()/2);
        path.lineTo(x + trackType.noteDrawWidth, stringY );
        path.lineTo(x, stringY + Themes.getDrumNoteHeight()/2);
        path.closePath();
        g2.setColor(color);
        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.draw(path);
    }

    private int getNoteX(long start) {
        return (int) ((double) start / controller.pageController.getTicksPerMeasure() * PageView.measureSize);
    }

    private int getNoteY(int stringNum) {
        return Themes.margin.get("top") + stringNum * Themes.getLineSpacing() - Themes.getNoteHeight()/2;
    }

    private int getNoteWidth(long duration) {
        return (int) ((double) duration / controller.pageController.getTicksPerMeasure() * PageView.measureSize);
    }

    private void drawNote(Graphics2D g2, Note note, Color color) {
        int lineSpacing = Themes.getLineSpacing();
        int drawHeight = Themes.getNoteHeight();
        String fretNum = "" + note.fret;
        int fretNumWidth = fontMetrics.stringWidth(fretNum);

        note.rectangle.x = getNoteX(note.start) + 1;
        note.rectangle.y = getNoteY(note.stringNum);
        note.rectangle.width = getNoteWidth(note.duration);
        note.rectangle.height = Themes.getNoteHeight();

        //draw note color
        g2.setColor(color);
        g2.fillRect(note.rectangle.x, note.rectangle.y, note.rectangle.width, note.rectangle.height);

        //draw note outline
        g2.setColor(Color.BLACK);
        g2.drawRect(note.rectangle.x, note.rectangle.y, note.rectangle.width, note.rectangle.height);

        //draw box for fret number
        g2.setColor(color);
        g2.fillRect(note.rectangle.x, note.rectangle.y + drawHeight/2 - fontHeight/2, fretNumWidth, fontHeight);

        //draw fret number  TODO: "-1" is magic number to place fretNum just above centered
        g2.setColor(Color.BLACK);
        g2.drawString(fretNum, note.rectangle.x, note.rectangle.y + drawHeight/2 + fontHeight/2 - 1);
    }

    private void drawSelectorRect(Graphics2D g2, Rectangle rect) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.GRAY);
        g2.setStroke(dashed);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    protected void setProgressLine(int x) {
        int trackHeight = Themes.getTrackHeight(trackType.numOfStrings);
        int oldX = (int) progressLine.getX1();
        repaint(oldX - 1, 0, oldX + 1, trackHeight);
        progressLine.setLine(x, 0, x, trackHeight);
        repaint(x, 0, x, trackHeight);
    }

    protected void overwriteNote(Note note) {
        // additions/subtractions paint slightly more than needed to erase outdated pixels
        repaint(getNoteX(note.start) - 8, getNoteY(note.stringNum) - 4, getNoteWidth(note.duration) + 16, Themes.getNoteHeight() + 8);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(fretFont);

        if (zoomer) {
            AffineTransform at = new AffineTransform();
            at.scale(zoomFactor, zoomFactor);
            prevZoomFactor = zoomFactor;
            g2.transform(at);
            zoomer = false;
        }

        if (trackType.name == "drums") {
            //drawDrumLines(g2);
            drawLines(g2);
            for (Note note : controller.getNotes()) {
                //if (note.x < PageView.width) {
                    if (note.isSelected) {
                        drawTriangle(g2, note, Themes.colors.get("selectedNote"));
                    } else {
                        drawTriangle(g2, note, Themes.colors.get("note"));
                    }
                //}
            }
        } else {
            drawLines(g2);
            for (Note note : controller.getNotes()) {
                //if (note.x < PageView.width) {
                    if (note.isSelected) {
                        drawNote(g2, note, Themes.colors.get("selectedNote"));
                    } else {
                        drawNote(g2, note, Themes.colors.get("note"));
                    }
                //}
            }
        }

        g2.draw(progressLine);

        Rectangle rect = controller.getSelectorRect();

        if (rect.width + rect.height > 0) {
            drawSelectorRect(g2, rect);
        }

        g2.dispose();
        getToolkit().sync();
    }
}
