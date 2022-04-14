package track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import note.Note;
import themes.ThemeReader;
import utils.console;

class TrackDrawAreaDrums extends TrackDrawArea {

    public TrackDrawAreaDrums(TrackController controller, int numOfStrings) {
        super(controller, numOfStrings);
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
}
