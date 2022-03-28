package track;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import page.Page;
import page.PageView;
import note.Note;
import instruments.Instrument;
import instruments.Instruments;
import themes.*;
import utils.*;

interface MouseMoveStategy {
    public void doIt(MouseEvent evt);
}

public class TrackController {

    public Page pageController;
    private TrackView view;
    private Note selectedNote;
    private Point dragStart = new Point();
    private Point dragStartGrid = new Point();
    private List<Note> selection;
    private Rectangle selectorRect;
    private TrackType trackType;
    private MouseMoveStategy moveSelectorRect;
    private MouseMoveStategy mouseMoveNote;
    private MouseMoveStategy lengthenNote;
    private MouseMoveStategy lengthenNoteTentative;
    private MouseMoveStategy setNoteVelocity;
    private MouseMoveStategy mouseMoveStategy;
    private boolean isMuted = false;
    private double gridFraction = 0.125;
    //private int instrumentNum = 0;
    private int lastX = 0;
    private int lastY = 0;
    private VelocitySlider vSlider;
    private ArrayList<Note> notes = new ArrayList<Note>();
    private String name = "untitled track";
    private int channel;
    private int volume;
    private Instrument instrument;

    public TrackController(Page page) {
        pageController = page;
        //trackType = type;
        //view = new TrackView(this, trackType, "untitled track");
        view = new TrackView(this, "untitled track");
        selection = new ArrayList<>();
        selectorRect = new Rectangle(0, 0);

        moveSelectorRect = new MouseMoveSelectorRect();
        mouseMoveNote = new MouseMoveNormal();
        lengthenNoteTentative = new MouseMoveLengthenTentative();
        lengthenNote = new MouseMoveLengthen();
        setNoteVelocity = new MouseMoveVelocity();
    }

    private int calcGridSize() {
        return (int) (gridFraction * PageView.measureSize);
    }

    class MouseMoveSelectorRect implements MouseMoveStategy {
        @Override
        public void doIt(MouseEvent evt) {
            int x = evt.getX();
            int y = evt.getY();

            //view.changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            view.drawRectangle(selectorRect);
            //https://stackoverflow.com/questions/30941766
            selectorRect.x = Math.min(dragStart.x, x);
            selectorRect.y = Math.min(dragStart.y, y);
            selectorRect.width = Math.abs(dragStart.x - x);
            selectorRect.height = Math.abs(dragStart.y - y);

            //TODO next 2 lines were keeping dotted lines within draw area ..top bar ends up being 32px high
            //int trackHeight = Themes.getTrackHeight(trackType.numOfStrings);
            //selectorRect.height = Math.min(selectorRect.height, trackHeight - selectorRect.y - 40);
            view.drawRectangle(selectorRect);
        }
    }

    class MouseMoveNormal implements MouseMoveStategy {
        @Override
        public void doIt(MouseEvent evt) {
            int x = evt.getX();
            int y = evt.getY();
            int x2 = findNearestGrid(x);
            int y2 = findNearestStringNum(y);

            int diffX = x2 - dragStartGrid.x;
            int diffY = y2 - dragStartGrid.y;

            dragStartGrid.x = x2;
            dragStartGrid.y = y2;

            //moveSelectedNotes(diffX, diffY * Themes.getLineSpacing());
            moveSelectedNotes(diffX / calcGridSize(), diffY);
        }
    }

    class MouseMoveLengthenTentative implements MouseMoveStategy {
        @Override
        public void doIt(MouseEvent evt) {

            int x = evt.getX();
            int y = evt.getY();

            if (selectedNote.duration < 0 || Math.abs(y - dragStart.y) > ThemeReader.getMeasure("track.strings.spacing")) {
                mouseMoveStategy = moveSelectorRect;
                notes.remove(selectedNote);
            } else {
                int x2 = findNearestGrid(x);
                int diffX = x2 - dragStartGrid.x;

                dragStartGrid.x = x2;
                lengthenSelectedNotes(diffX);
            }
            view.drawNote(selectedNote);
        }
    }

    class MouseMoveLengthen implements MouseMoveStategy {
        @Override
        public void doIt(MouseEvent evt) {
            int x = evt.getX();
            int x2 = findNearestGrid(x);
            int diffX = x2 - dragStartGrid.x;

            dragStartGrid.x = x2;
            lengthenSelectedNotes(diffX);
        }
    }

    class MouseMoveVelocity implements MouseMoveStategy {
        @Override
        public void doIt(MouseEvent evt) {
            Component c = (Component) evt.getSource();
            Rectangle bounds = vSlider.getBounds();
            bounds.grow(1, 4);
            Rectangle adjusted =
                    SwingUtilities.convertRectangle(vSlider.getParent(), bounds, c);
            int y = evt.getY();
            int v = vSlider.getValue();
            int delta = y - lastY;
            if (y > adjusted.y && y < y + adjusted.height) {
                vSlider.setValue(v - delta);
                selectedNote.velocity = vSlider.getValue();
                vSlider.setDisplay(selectedNote.velocity);
                lastY = y;
            }
        }
    }

    public TrackView getView() {
        return view;
    }

    public void setVolume(int n) {
        volume = n;
        view.setVolumeField(100 * n/127);
    }

    public int getVolume() {
        return volume;
    }

    public Rectangle getSelectorRect() {
        return selectorRect;
    }

    public void setName(String n) {
        name = n;
        view.setName(name);
    }

    public String getName() {
        return name;
    }

    private long setNoteStart(int x) {
        double measure = (double)x/PageView.measureSize;
        double corrected = Math.ceil(measure /gridFraction) * gridFraction;
        return (long) (corrected * pageController.getTicksPerMeasure());
    }

    private long setNoteDuration(int width) {
        long gridsIn = width/calcGridSize();
        long gridsPerMeasure = PageView.measureSize/calcGridSize();
        long ticksPerGrid = pageController.getTicksPerMeasure()/ gridsPerMeasure;
        return gridsIn * ticksPerGrid;
        //return (long) ((double) width * pageController.getTicksPerMeasure() / PageView.measureSize);
    }

    public void setProgress(long tick, int ticksPerMeasure) {
        int x = (int) ((double) tick / ticksPerMeasure * PageView.measureSize);
        view.drawProgressLine(x);
    }

    public void cancelProgress() {
        view.hideProgressLine();
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void loadNote(Note note) {
        trackType.assignStringAndFret(note);
        notes.add(note);
    }

    private Note addNote(int x, int y) {
        Note note = new Note();
        note.stringNum = findNearestStringNum(y);
        note.pitch = trackType.findNotePitch(note.stringNum, note.fret);
        x = findNearestGrid(x);
        note.start = setNoteStart(x);
        notes.add(note);
        return note;
    }

    public void setAsSelectedTrack() {
        view.highliteBorder();
    }

    public void setAsNotSelectedTrack() {
        view.deHighliteBorder();
        clearSelection();
    }

    public List<Note> getSelection() {
        return selection;
    }

    public List<Note> getNotes() {
        return notes;
    }

    private void selectNote(Note note) {
        selectedNote = note;
        selectedNote.isSelected = true;
        //ensure note is drawn last (on top of others)
        if (notes.contains(note)) {
            notes.remove(note);
        }
        notes.add(note);
        if (!selection.contains(selectedNote)) {
            selection.add(selectedNote);
        }
        view.drawNote(selectedNote);
    }

    private void hideSelectorRect() {
        view.drawRectangle(selectorRect);
        clearSelection();

        ///* loop backward through notes */
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            if (selectorRect.contains(note.rectangle)) {
                selectNote(note);
                if (note.rectangle.x == findNearestGrid(note.rectangle.x)) {
                    lastX = note.rectangle.x;
                }
            }
        }
        selectorRect.setSize(0, 0);
    }

    private void clearSelection() {
        selection.forEach(note -> {
            note.isSelected = false;
            //view.drawRectangle(note.rectangle);
            view.drawNote(note);
        });
        selectedNote = null;
        selection.clear();
    }

    public void deleteSelectedNotes() {
        for (Note note : selection) {
            notes.remove(note);
            view.drawNote(note);
        }
        selection.clear();
    }

    public List<Note> cutSelectedNotes() {
        List<Note>temp = new ArrayList<>();
        for (Note note : selection) {
            notes.remove(note);
            view.drawNote(note);
            temp.add(note);
        }
        selection.clear();
        return temp;
    }

    public List<Note> copySelectedNotes() {
        List<Note>temp = new ArrayList<>();
        for (Note note : selection) {
            Note clone = note.clone();
            temp.add(clone);
        }
        return temp;
    }

    public void pasteSelectedNotes(List<Note> clipboard) {
        if (clipboard.size() > 0) {
            clearSelection();
            for (Note note : clipboard) {
                Note clone = note.clone();
                clone.fromClipboard = true;
                loadNote(clone);
                selectNote(clone);
            }
        }
    }

    private void lengthenSelectedNotes(int deltaX) {
        if (deltaX == 0) return;
        for (Note note : selection) {
            view.drawNote(note);
            //note.duration += deltaX * pageController.getTicksPerMeasure() / PageView.measureSize;
            note.duration += Integer.signum(deltaX) * pageController.getTicksPerMeasure() * gridFraction;
            view.drawNote(note);
        }
    }

    private void moveSelectedNotes(int deltaX, int deltaY) {

        // do some checks on block as a whole before moving anything
        for (Note note : selection) {
            // don't move any notes to left if within 1 gridSize of 0
            if (note.rectangle.x < calcGridSize()) {
                deltaX = Math.max(deltaX, 0);
            }
            // don't move any notes to above or below number of strings
            if ((note.stringNum == 0 && deltaY < 0) ||
                (note.stringNum == trackType.numOfStrings - 1 && deltaY > 0)) {
                deltaY = 0;
            }
        }

        for (Note note : selection) {
            view.drawNote(note);
            //note.start += deltaX * pageController.getTicksPerMeasure() / PageView.measureSize;
            note.start += deltaX * pageController.getTicksPerMeasure() * gridFraction;
            note.stringNum += deltaY;
            note.pitch = trackType.findNotePitch(note.stringNum, note.fret);
            view.drawNote(note);
        }
    }

    public void moveSelectionArrowKeys(int dirX, int dirY) {
        for (Note note : selection) {
            //if (note.rectangle.x == findNearestGrid(note.rectangle.x)) {
                //lastX = note.rectangle.x;
                lastX = findNearestGrid(note.rectangle.x);
                break;
            //}
        }
        int x2 = lastX + calcGridSize() * dirX;
        x2 = findNearestGrid(x2);
        int deltaX = x2 - lastX;
        //int deltaY = dirY * Themes.getLineSpacing();
        //moveSelectedNotes(deltaX, deltaY);
        moveSelectedNotes(deltaX /calcGridSize(), dirY);
        lastX += deltaX;
    }

    private int findNearestGrid(int x) {
        double dist = x / (gridFraction * PageView.measureSize);
        double decimalPart = dist - Math.floor(dist);
        // snap to left side of cursor unless very close to next grid
        if (decimalPart > 0.7) {
            dist = Math.round(dist);
        } else {
            dist = Math.floor(dist);
        }
        return (int) (dist * (gridFraction * PageView.measureSize));
    }

    private int findNearestStringNum(int y) {
        int stringNum = (int) Math.round((double)(y - ThemeReader.getMeasure("track.strings.margin.top")) / ThemeReader.getMeasure("track.strings.spacing"));
        stringNum = Math.min(trackType.numOfStrings - 1, Math.max(0, stringNum));
        return stringNum;
    };

    //public void adjustMeasureSize(int measureSize) {
        //view.adjustMeasureSize(measureSize);
        //lastX = findNearestGrid(lastX);
    //}

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(int number) {
        if (channel == 9) {
            for (Instrument drum : Instruments.drumList) {
                if (drum.number == number) {
                    instrument = drum;
                    view.setInstrumentName(drum.name);
                    break;
                }
            }
        } else {
            for (Instrument inst : Instruments.list) {
                if (inst.number == number) {
                    instrument = inst;
                    view.setInstrumentName(inst.name);
                    break;
                }
            }
        }
    }

    public void setTrackType(TrackType type) {
        trackType = type;
        for (Note note : notes) {
            trackType.assignStringAndFret(note);
        }
        view.setTrackType(type);
    }

    public long getNoteMeasure(Note note) {
        int ticksPerMeasure = pageController.getTicksPerMeasure();
        return 1 + note.start / ticksPerMeasure;
    }

    public void insertBars(int numberToAdd, int addBefore) {
        int ticksPerMeasure = pageController.getTicksPerMeasure();
        long deltaStart = numberToAdd * ticksPerMeasure;
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            long measure = 1 + (note.start / ticksPerMeasure);
            if (measure >= addBefore) {
                note.start += deltaStart;
            }
        }
    }

    public void setScrollPosition(int value) {
        view.setScrollPosition(value);
    }

    public void setChannel(int n) {
        channel = n;
    }

    public int getChannel() {
        return channel;
    }

    private void changeNoteFret(int number) {
        if (selectedNote != null) {
            selectedNote.fret = number;
            selectedNote.pitch = trackType.findNotePitch(selectedNote.stringNum, selectedNote.fret);
            pageController.playSelection(this);
        }
    }

    public void showFretField(int number) {
        if (selectedNote != null) {
            changeNoteFret(number);
            view.showFretField(selectedNote, number);
        }
    }

    public void handleVolumeField(int value) {
        volume = value * 127/100;
    }

    public void handleTrackNameField() {
        name = view.getTrackNameField();
    }

    public void handleFretField(boolean isEnter) {
        if (selectedNote != null) {
            if (isEnter == true) {
                view.hideFretField();
                handleTrackInput(false);
            } else {
                String fretAsString = view.getFretField();
                if ("".equals(fretAsString) || fretAsString == null) {
                    changeNoteFret(0);
                } else {
                    changeNoteFret(Integer.valueOf(fretAsString));
                }
            }
        }
    }

    public void handleTrackInput(boolean elementNeedsFocus) {
        pageController.handleTrackInput(this, elementNeedsFocus);
    }

    public void handleCollapseButton(Constants c) {
        switch(c) {
            case BUTTON_TRACKCOLLAPSE:
                view.toggleCollapseButton(c);
                break;
            case BUTTON_TRACKEXPAND:
                view.toggleCollapseButton(c);
                break;
            default:
        }
        pageController.handleMuteButton(this, isMuted);
    }

    public void handleMuteButton(Constants c) {
        switch(c) {
            case BUTTON_TRACKMUTE:
                isMuted = true;
                view.toggleMuteButton(Constants.BUTTON_TRACKUNMUTE);
                break;
            case BUTTON_TRACKUNMUTE:
                isMuted = false;
                view.toggleMuteButton(Constants.BUTTON_TRACKMUTE);
                break;
            default:
        }
        pageController.handleMuteButton(this, isMuted);
    }

    public void handleMouseDownDrawArea(MouseEvent evt) {

        int x = evt.getX();
        int y = evt.getY();

        //double dist = x / (gridFraction * PageView.measureSize);

        dragStart.setLocation(x, y);
        dragStartGrid.setLocation(findNearestGrid(x), findNearestStringNum(y));
        selectorRect.setLocation(x, y);

        MouseMods.setFromEvent(evt);
        view.hideFretField();

        Note note = null;
        /* loop backward through notes */
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note n = notes.get(i);
            if (n.rectangle.contains(x, y)) {
                note = n;
                break;
            }
        }

        if (note != null) {
            if (!selection.contains(note) && !MouseMods.shift) {
                clearSelection();
            }

            selectNote(note);
            if (MouseMods.ctrl || MouseMods.rClick) {
                vSlider = pageController.showVelocitySlider(evt, selectedNote);
                mouseMoveStategy = setNoteVelocity;
                lastY = y;

            } else if (x > selectedNote.rectangle.x + selectedNote.rectangle.width - 5) {
                //view.changeCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                mouseMoveStategy = lengthenNote;

            } else if (MouseMods.alt) {
                pageController.copySelection();
                pageController.pasteSelection();
                mouseMoveStategy = mouseMoveNote;

            } else {
                //view.changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                mouseMoveStategy = mouseMoveNote;
            }

        } else if (MouseMods.lClick) {
            clearSelection();
            note = addNote(x, y);
            selectNote(note);
            mouseMoveStategy = lengthenNoteTentative;
        }

    }

    public void handleMouseUpDrawArea(MouseEvent evt) {

        if (selectedNote != null && selectedNote.duration <= 0) {
            notes.remove(selectedNote);
            clearSelection();
        }

        view.changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        pageController.hideVelocitySlider();
        if (mouseMoveStategy == moveSelectorRect) {
            hideSelectorRect();
        } else {
            pageController.playSelection(this);
        }
    }

    public void handleMouseMoveDrawArea(MouseEvent evt) {
        mouseMoveStategy.doIt(evt);
    }

    public void handleInstrumentPicker(Instrument instrument) {
        if (Instruments.isDrumSet(instrument)) {
            setChannel(9);
        } else {
            //don't do this --but need to get from drum to other inst
            //setChannel(1);
        }
        setInstrument(instrument.number);
    }

    public void handleGridSizePicker(double value) {
        gridFraction = value;
    }


    public void handleTrackTypePicker(Object i) {
        TrackType type = (TrackType) i;
        setTrackType(type);
    }

    @Override
    public String toString() {
        return "TrackController" +
                "\n channel " + getChannel() +
                "\n instrumentNum " + getInstrument().number +
                "\n volume " + getVolume() +
                "";
    }

}