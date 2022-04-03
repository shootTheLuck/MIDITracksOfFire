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
import javax.swing.SwingUtilities;

import note.Note;
import page.PageView;
import themes.ThemeReader;
import utils.console;
import widgets.NumberInputField;


class TrackDrawArea extends JLayeredPane {

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

    protected NumberInputField fretField;
    private Runnable sendFretField;

    public TrackDrawArea(TrackController controller, TrackType trackType) {

        this.controller = controller;
        this.trackType = trackType;
        setLayout(null);
        setBorder(BorderFactory.createLineBorder(Color.black, 1));

        //int trackHeight = Themes.getTrackHeight(trackType.numOfStrings);
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

    private void drawDrumLines(Graphics2D g) {
        int measureSize = PageView.measureSize;

        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int lineSpacing = ThemeReader.getMeasure("track.strings.spacing");

        g.setColor(Color.gray);
        g.fillRect(0, 10, PageView.width, trackType.numOfStrings * lineSpacing * 2);

        g.setColor(Color.white);
        //g.setStroke(new BasicStroke(trackType.noteDrawHeight));
        g.setStroke(new BasicStroke(ThemeReader.getMeasure("note.height")));
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
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int lineSpacing = ThemeReader.getMeasure("track.strings.spacing");

        g.setColor(ThemeReader.getColor("track.strings.color"));
        g.setStroke(new BasicStroke(1));

        for (int i = 0; i < trackType.numOfStrings; i++) {
            g.drawLine(
                0,
                topMargin + i * lineSpacing,
                PageView.width,
                topMargin + i * lineSpacing);
        }

        g.setColor(ThemeReader.getColor("track.gridLines.color"));
        for (int j = 0; j < 4 * PageView.width/measureSize; j++) {
            g.drawLine(
                (int)(measureSize/4.0 * j),
                topMargin,
                (int)(measureSize/4.0 * j),
                topMargin + lineSpacing * (trackType.numOfStrings - 1));
        }

        g.setColor(ThemeReader.getColor("track.barLines.color"));
        for (int i = 0; i < 1 + PageView.width/measureSize; i++) {
            g.drawLine(
                measureSize * i,
                topMargin,
                measureSize * i,
                topMargin + lineSpacing * (trackType.numOfStrings - 1));
        }

    }

    private void setNoteRectangle(Note note) {
        note.rectangle.x = getNoteX(note.start) + 1;
        note.rectangle.y = getNoteY(note.stringNum);
        note.rectangle.width = getNoteWidth(note.duration);
        note.rectangle.height = getNoteHeight();
    }

    private int getNoteX(long start) {
        return (int) ((double) start / controller.pageController.getTicksPerMeasure() * PageView.measureSize);
    }

    private int getNoteY(int stringNum) {
        return ThemeReader.getMeasure("track.strings.margin.top") +
        stringNum * ThemeReader.getMeasure("track.strings.spacing") - ThemeReader.getMeasure("note.height")/2;
    }

    private int getDrumNoteY(int stringNum) {
        return ThemeReader.getMeasure("track.strings.margin.top") +
        stringNum * ThemeReader.getMeasure("track.strings.spacing") - ThemeReader.getMeasure("drumNote.height")/2;
    }

    private int getNoteWidth(long duration) {
        return (int) ((double) duration / controller.pageController.getTicksPerMeasure() * PageView.measureSize);
    }

    private int getNoteHeight() {
        return ThemeReader.getMeasure("note.height");
    }

    private void drawTriangle(Graphics2D g2, Note note, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D path = new Path2D.Double();

        note.rectangle.x = getNoteX(note.start) + 0;
        note.rectangle.y = getDrumNoteY(note.stringNum);
        note.rectangle.width = ThemeReader.getMeasure("drumNote.width");;
        note.rectangle.height = ThemeReader.getMeasure("drumNote.height");

        int stringY = note.rectangle.y + note.rectangle.height/2;

        path.moveTo(note.rectangle.x, note.rectangle.y);
        path.lineTo(note.rectangle.x + note.rectangle.width, stringY );
        path.lineTo(note.rectangle.x, stringY + note.rectangle.height/2);
        path.closePath();
        g2.setColor(color);
        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.draw(path);
    }

    private void drawNote(Graphics2D g2, Note note, Color color) {
        setNoteRectangle(note);

        String fretNum = "" + note.fret;
        int fretNumWidth = fontMetrics.stringWidth(fretNum);
        int drawHeight = note.rectangle.height;

        //draw note color
        g2.setColor(color);
        g2.fillRect(
            note.rectangle.x,
            note.rectangle.y,
            note.rectangle.width,
            drawHeight);

        //draw note outline
        g2.setColor(Color.BLACK);
        g2.drawRect(
            note.rectangle.x,
            note.rectangle.y,
            note.rectangle.width,
            drawHeight);

        //draw box for fret number
        g2.setColor(color);
        g2.fillRect(
            note.rectangle.x,
            note.rectangle.y + drawHeight/2 - fontHeight/2,
            fretNumWidth,
            fontHeight);

        //draw fret number  TODO: "-1" is magic number to place fret number just above centered
        g2.setColor(Color.BLACK);
        g2.drawString(
            fretNum, note.rectangle.x,
            note.rectangle.y + drawHeight/2 + fontHeight/2 - 1);
    }

    private void drawSelectorRect(Graphics2D g2, Rectangle rect) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.GRAY);
        g2.setStroke(dashed);
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    protected void setProgressLine(int x) {
        int trackHeight = ThemeReader.getMeasure("track.strings.spacing") * trackType.numOfStrings;
        trackHeight += ThemeReader.getMeasure("track.strings.margin.top");
        trackHeight += ThemeReader.getMeasure("track.strings.margin.bottom");
        int oldX = (int) progressLine.getX1();
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(fretFont);

        //if (zoomer) {
            //AffineTransform at = new AffineTransform();
            //at.scale(zoomFactor, zoomFactor);
            //prevZoomFactor = zoomFactor;
            //g2.transform(at);
            //zoomer = false;
        //}

        Color selectedColor = ThemeReader.getColor("note.selected.background");
        Color unselectedColor = ThemeReader.getColor("note.unselected.background");

        if (trackType.name == "drums") {
            //drawDrumLines(g2);
            drawLines(g2);
            for (Note note : controller.notes) {
                //if (note.x < PageView.width) {
                    if (note.isSelected) {
                        drawTriangle(g2, note, selectedColor);
                    } else {
                        drawTriangle(g2, note, unselectedColor);
                    }
                //}
            }
        } else {
            drawLines(g2);
            for (Note note : controller.notes) {
                //if (note.x < PageView.width) {
                    if (note.isSelected) {
                        drawNote(g2, note, selectedColor);
                    } else {
                        drawNote(g2, note, unselectedColor);
                    }
                //}
            }
        }

        g2.draw(progressLine);

        Rectangle rect = controller.selectorRect;
        if (rect.width + rect.height > 0) {
            drawSelectorRect(g2, rect);
        }

        g2.dispose();
        getToolkit().sync();
    }
}
