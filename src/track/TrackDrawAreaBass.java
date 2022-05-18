package track;

import java.awt.*;

import note.Note;
import themes.ThemeReader;
import utils.console;


class TrackDrawAreaBass extends TrackDrawArea {

    public TrackDrawAreaBass(TrackController controller, int numOfStrings) {
        super(controller, numOfStrings);
        this.type = "bass";
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        Color selectedColor = ThemeReader.getColor("note.selected.background");
        Color unselectedColor = ThemeReader.getColor("note.unselected.background");

        drawStrings(g2);
        drawGridLines(g2);

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

    }
}
