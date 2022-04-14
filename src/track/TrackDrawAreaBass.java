package track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import note.Note;
import themes.ThemeReader;
import utils.console;


class TrackDrawAreaBass extends TrackDrawArea {

    public TrackDrawAreaBass(TrackController controller, int numOfStrings) {
        super(controller, numOfStrings);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setFont(fretFont);

        Color selectedColor = ThemeReader.getColor("note.selected.background");
        Color unselectedColor = ThemeReader.getColor("note.unselected.background");

        drawLines(g2);
        for (Note note : controller.notes) {
            if (note.isSelected) {
                drawNote(g2, note, selectedColor);
            } else {
                drawNote(g2, note, unselectedColor);
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
