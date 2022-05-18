package track;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import javax.swing.*;

import note.Note;
import page.PageView;
import themes.ThemeReader;
import utils.console;
import widgets.NumberInputField;


class TrackDrawArea extends JLayeredPane {

    //protected TrackType trackType; // not using yet
    protected TrackController controller;
    protected float[] dash1 = {2F, 2F};
    protected BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 1.0f, dash1, 2f);

    protected Line2D progressLine;
    protected Font fretFont = new Font("Dialog", Font.PLAIN, 11);
    protected FontMetrics fontMetrics = getFontMetrics(fretFont);
    protected int fontHeight = fontMetrics.getAscent();

    protected int topMargin;
    protected int numOfStrings;

    protected NumberInputField fretField;
    protected Runnable sendFretField;
    protected String type;

    public TrackDrawArea(TrackController controller, int numOfStrings) {

        this.controller = controller;
        this.numOfStrings = numOfStrings;
        this.type = "generic";

        setLayout(null);

        //int trackHeight = Themes.getTrackHeight(numOfStrings);
        progressLine = new Line2D.Double(-1000, 0, -1000, 0);

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

        fretField = new NumberInputField(0, 3);
        fretField.setAutoFocus(false);
        sendFretField = new Runnable() {
            @Override
            public void run() {
                controller.handleFretFieldChange();
            }
        };
        fretField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                    // don't start playing music on first enter after fret change
                    evt.consume();
                    controller.handleFretFieldEnter();
                } else
                if (evt.getKeyChar() == KeyEvent.VK_TAB) {
                    controller.handleFretFieldTab();
                } else {
                    SwingUtilities.invokeLater(sendFretField);
                }
            }
        });

        Dimension d = new Dimension(20, 20);
        fretField.setSize(d);
        fretField.setMinimumSize(d);
        fretField.setMaximumSize(d);
        fretField.setPreferredSize(d);
        fretField.setFocusTraversalKeysEnabled(false);
        add(fretField, 2);
        fretField.setVisible(false);

    }

    @Override
    public int getHeight() {
        int height = ThemeReader.getMeasure("track.strings.spacing") * (numOfStrings - 1);
        height += ThemeReader.getMeasure("track.strings.margin.top");
        height += ThemeReader.getMeasure("track.strings.margin.bottom");
        return height;
    }

    protected void drawGridLines(Graphics2D g) {
        int measureSize = PageView.measureSize;
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int lineSpacing = ThemeReader.getMeasure("track.strings.spacing");
        int bottomOfStrings = topMargin + lineSpacing * (numOfStrings - 1);

        g.setColor(ThemeReader.getColor("track.gridLines.color"));
        for (int j = 0; j < 4 * PageView.width/measureSize; j++) {
            g.drawLine(
                (int)(measureSize/4.0 * j),
                topMargin,
                (int)(measureSize/4.0 * j),
                bottomOfStrings);
        }

        g.setColor(ThemeReader.getColor("track.barLines.color"));
        for (int i = 0; i < 1 + PageView.width/measureSize; i++) {
            g.drawLine(
                measureSize * i,
                topMargin,
                measureSize * i,
                bottomOfStrings);
        }
    }

    protected void drawStrings(Graphics2D g) {
        int measureSize = PageView.measureSize;
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int lineSpacing = ThemeReader.getMeasure("track.strings.spacing");
        int bottomOfStrings = topMargin + lineSpacing * (numOfStrings - 1);

        g.setColor(ThemeReader.getColor("track.strings.color"));
        g.setStroke(new BasicStroke(1));

        for (int i = 0; i < numOfStrings; i++) {
            g.drawLine(
                0,
                topMargin + i * lineSpacing,
                PageView.width,
                topMargin + i * lineSpacing);
        }
    }

    protected void setNoteRectangle(Note note) {
        note.rectangle.x = getNoteX(note.start) + 1;
        note.rectangle.y = getNoteY(note.stringNum);
        note.rectangle.width = getNoteWidth(note.duration);
        note.rectangle.height = getNoteHeight();
    }

    protected int getNoteX(long start) {
        return (int)((double)start / controller.pageController.getTicksPerMeasure() * PageView.measureSize);
    }

    protected int getNoteY(int stringNum) {
        int stringSpacing = ThemeReader.getMeasure("track.strings.spacing");
        int stringY = getNoteStringY(stringNum);
        return stringY - stringSpacing / 2;
    }

    protected int getNoteStringY(int stringNum) {
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int stringSpacing = ThemeReader.getMeasure("track.strings.spacing");
        return topMargin + stringNum * stringSpacing;
    }

    protected int getNoteWidth(long duration) {
        return (int)((double)duration / controller.pageController.getTicksPerMeasure() * PageView.measureSize);
    }

    protected int getNoteHeight() {
        //return ThemeReader.getMeasure("note.height");
        int stringSpacing = ThemeReader.getMeasure("track.strings.spacing");
        return stringSpacing * 1;
    }

    protected void drawNote(Graphics2D g2, Note note, Color color) {
        setNoteRectangle(note);

        String fretNum = "" + note.fret;
        int fretNumWidth = fontMetrics.stringWidth(fretNum);
        int drawHeight = ThemeReader.getMeasure("note.height");
        int stringY = getNoteStringY(note.stringNum);

        //draw note color
        g2.setColor(color);
        g2.fillRect(
            note.rectangle.x,
            //note.rectangle.y,
            stringY - drawHeight/2,
            note.rectangle.width,
            drawHeight);

        //draw note outline
        g2.setColor(Color.BLACK);
        g2.drawRect(
            note.rectangle.x,
            //note.rectangle.y,
            stringY - drawHeight/2,
            note.rectangle.width,
            drawHeight);

        //draw box for fret number
        g2.setColor(color);
        g2.fillRect(
            note.rectangle.x,
            //note.rectangle.y + drawHeight/2 - fontHeight/2,
            stringY - fontHeight/2,
            fretNumWidth,
            fontHeight);

        //draw fret number  TODO: "-1" is magic number to place fret number just above centered
        g2.setColor(Color.BLACK);
        g2.drawString(
            fretNum, note.rectangle.x,
            //note.rectangle.y + drawHeight/2 + fontHeight/2 - 1);
            stringY + fontHeight/2 - 1);
    }

    protected void drawSelectorRect(Graphics2D g2, Rectangle rect) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.GRAY);
        g2.setStroke(dashed);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    protected void setProgressLine(int x) {
        int trackHeight = ThemeReader.getMeasure("track.strings.spacing") * numOfStrings;
        trackHeight += ThemeReader.getMeasure("track.strings.margin.top");
        trackHeight += ThemeReader.getMeasure("track.strings.margin.bottom");
        int oldX = (int)progressLine.getX1();
        repaint(oldX - 1, 0, oldX + 1, trackHeight);
        progressLine.setLine(x, 0, x, trackHeight);
        repaint(x, 0, x, trackHeight);
    }

    protected void overwriteNote(Note note) {
        setNoteRectangle(note);
        // additions/subtractions: paint slightly more than needed to erase outdated pixels
        int xBuffer = PageView.measureSize + 20;
        int yBuffer = ThemeReader.getMeasure("track.strings.spacing") * 2;

        repaint(
            note.rectangle.x - xBuffer,
            note.rectangle.y - yBuffer,
            Math.abs(note.rectangle.width) + xBuffer * 2,
            Math.abs(note.rectangle.height) + yBuffer * 2);
    }

    protected void showFretField(Note note, int fretNum) {
        fretField.setVisible(true);
        fretField.setLocation(note.rectangle.x - 5, note.rectangle.y - 5);
        fretField.setText(String.valueOf(fretNum));
        fretField.setCaretPosition(fretField.getDocument().getLength());
        fretField.requestFocusInWindow();
        //fretField.grabFocus();
        SwingUtilities.invokeLater(sendFretField);
    }

    protected void hideFretField() {
        fretField.setText("");
        fretField.setVisible(false);
    }

}
