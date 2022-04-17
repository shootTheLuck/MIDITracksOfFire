package track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import note.Note;
import page.PageView;
import themes.ThemeReader;
import utils.console;

class TrackDrawAreaDrums extends TrackDrawArea {

    public TrackDrawAreaDrums(TrackController controller, int numOfStrings) {
        super(controller, numOfStrings);
    }

    @Override
    public int getHeight() {
        int height = ThemeReader.getMeasure("track.strings.spacing") * (numOfStrings - 0);
        height += ThemeReader.getMeasure("track.strings.margin.top");
        height += ThemeReader.getMeasure("track.strings.margin.bottom");
        return height;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(fretFont);

        Color selectedColor = ThemeReader.getColor("note.selected.background");
        Color unselectedColor = ThemeReader.getColor("note.unselected.background");

        drawDrumLines(g2);
        drawGridLines(g2);
        for (Note note : controller.notes) {
            if (note.isSelected) {
                drawTriangle(g2, note, selectedColor);
            } else {
                drawTriangle(g2, note, unselectedColor);
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

    protected void drawTriangle(Graphics2D g2, Note note, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D path = new Path2D.Double();

        note.rectangle.x = getNoteX(note.start) + 0;
        note.rectangle.y = getNoteY(note.stringNum);
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

    protected void drawDrumLines(Graphics2D g) {
        int measureSize = PageView.measureSize;
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int lineSpacing = ThemeReader.getMeasure("track.strings.spacing") * 1;

        g.setColor(Color.gray);
        //g.fillRect(0, lineSpacing, PageView.width, numOfStrings * lineSpacing + (int)(lineSpacing * 0.5));

        //g.setColor(Color.white);
        //g.setStroke(new BasicStroke(noteDrawHeight));
        //g.setStroke(new BasicStroke(ThemeReader.getMeasure("note.height")));
        g.setStroke(new BasicStroke(1));
        for (int i = 0; i < numOfStrings + 1; i++) {
            g.drawLine(
                0,
                topMargin - lineSpacing/2 + i * lineSpacing,
                PageView.width,
                topMargin - lineSpacing/2  + i * lineSpacing);
        }
        g.setStroke(new BasicStroke(1));

    }

}
