package track;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingUtilities;

import page.Page;
import page.PageView;
import page.Constants;
import note.Note;
import instruments.Instrument;
import instruments.Instruments;
import themes.ThemeReader;
import utils.console;
import utils.MouseMods;

interface MouseStrategy {
    public void doIt(MouseEvent evt);
}

class SortbyStart implements Comparator<Note> {
    // sort in ascending order
    public int compare(Note a, Note b) {
        return (int) (a.start - b.start);
    }
}

class Notes extends ArrayList<Note> {

    public boolean sorted = false;

    public Notes() {
        this.sorted = sorted;
    }

    public Notes sortByStart() {
        if (!this.sorted) {
            Collections.sort(this, new SortbyStart());
            this.sorted = true;
        }
        return this;
    }

    public Notes sortByStart(boolean force) {
        if (force) {
            Collections.sort(this, new SortbyStart());
            this.sorted = true;
        } else {
            this.sortByStart();
        }
        return this;
    }
}

public class TrackController {

    public Page pageController;
    private TrackView view;
    private Point dragStart = new Point();
    private Point dragStartGrid = new Point();
    private Rectangle selectorRect;
    private TrackType trackType;

    private MouseStrategy moveSelectorRect = new DragSelectorRect();
    private MouseStrategy mouseMoveNote = new MoveNote();
    private MouseStrategy lengthenNote = new LengthenNote();
    private MouseStrategy lengthenNoteTentative = new LengthenNoteTentative();
    private MouseStrategy setNoteVelocity = new MouseMoveVelocity();
    private MouseStrategy mouseStrategy;

    private boolean isMuted = false;
    private double gridFraction = 0.125;
    private int lastY = 0; // using for velocitySlider
    private VelocitySlider vSlider;

    private Note selectedNote;
    protected Notes notes = new Notes();
    private Notes tabbedNotes = new Notes();
    private Notes selection = new Notes();
    private String name = "untitled track";
    private int channel;
    private int volume;
    private Instrument instrument;

    public TrackController(Page page) {
        pageController = page;
        view = new TrackView(this, "untitled track");
        selectorRect = new Rectangle(0, 0);
    }

    private int calcGridSize() {
        return (int) (gridFraction * PageView.measureSize);
    }

    class DragSelectorRect implements MouseStrategy {
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

    class MoveNote implements MouseStrategy {
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

            moveSelectedNotes(diffX / calcGridSize(), diffY);
        }
    }

    class LengthenNoteTentative implements MouseStrategy {
        @Override
        public void doIt(MouseEvent evt) {

            int y = evt.getY();

            if (selectedNote.duration < 0 ||
                    Math.abs(y - dragStart.y) > ThemeReader.getMeasure("track.strings.spacing")) {
                mouseStrategy = moveSelectorRect;
                notes.remove(selectedNote);
                view.drawNote(selectedNote);
            } else {
                int x = evt.getX();
                int x2 = findNearestGrid(x);
                int diffX = x2 - dragStartGrid.x;

                dragStartGrid.x = x2;
                lengthenSelectedNotes(diffX);
            }
        }
    }

    class LengthenNote implements MouseStrategy {
        @Override
        public void doIt(MouseEvent evt) {
            int x = evt.getX();
            int x2 = findNearestGrid(x);
            int diffX = x2 - dragStartGrid.x;

            dragStartGrid.x = x2;
            lengthenSelectedNotes(diffX);
        }
    }

    class MouseMoveVelocity implements MouseStrategy {
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

    protected void handleMouseDownDrawArea(MouseEvent evt) {

        int x = evt.getX();
        int y = evt.getY();

        //double dist = x / (gridFraction * PageView.measureSize);

        dragStart.setLocation(x, y);
        dragStartGrid.setLocation(findNearestGrid(x), findNearestStringNum(y));
        selectorRect.setLocation(x, y);

        MouseMods.setFromEvent(evt);
        view.hideFretField();
        tabbedNotes.clear();

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
            //console.log(note);
            if (!selection.contains(note) && !MouseMods.shift) {
                clearSelection();
            }

            selectNote(note);
            if (MouseMods.ctrl || MouseMods.rClick) {
                vSlider = pageController.showVelocitySlider(evt, selectedNote);
                mouseStrategy = setNoteVelocity;
                lastY = y;

            } else if (x > selectedNote.rectangle.x + selectedNote.rectangle.width - 5) {
                //view.changeCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                mouseStrategy = lengthenNote;

            } else if (MouseMods.alt) {
                pageController.copySelection();
                pageController.pasteSelection();
                mouseStrategy = mouseMoveNote;

            } else {
                //view.changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                mouseStrategy = mouseMoveNote;
            }

        } else if (MouseMods.lClick) {
            clearSelection();
            note = addNote(x, y);
            selectNote(note);
            mouseStrategy = lengthenNoteTentative;
        }

    }

    protected void handleMouseUpDrawArea(MouseEvent evt) {

        if (selectedNote != null && selectedNote.duration <= 0) {
            notes.remove(selectedNote);
            clearSelection();
        }

        view.changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        pageController.hideVelocitySlider();
        if (mouseStrategy == moveSelectorRect) {
            hideSelectorRect();
        } else if (selection.size() == 1) {
            pageController.playSelection(this);
        }
    }

    protected void handleMouseMoveDrawArea(MouseEvent evt) {
        mouseStrategy.doIt(evt);
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

    // not using
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
        notes.sorted = false;
    }

    private Note addNote(int x, int y) {
        Note note = new Note();
        note.stringNum = findNearestStringNum(y);
        note.pitch = trackType.findNotePitch(note.stringNum, note.fret);
        x = findNearestGrid(x);
        note.start = setNoteStart(x);
        notes.add(note);
        notes.sorted = false;
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
        return selection.sortByStart(true);
    }

    public List<Note> getNotes() {
        return notes.sortByStart(true);
    }

    private void selectNote(Note note) {
        selectedNote = note;
        selectedNote.isSelected = true;
        //ensure note is drawn last (on top of others)
        if (notes.contains(note)) {
            notes.remove(note);
        }
        notes.add(note);
        notes.sorted = false;
        if (!selection.contains(selectedNote)) {
            selection.add(selectedNote);
            selection.sorted = false;
        }
        view.drawNote(selectedNote);
    }

    private void hideSelectorRect() {
        view.drawRectangle(selectorRect);
        clearSelection();

        /* loop backward through notes */
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            if (selectorRect.contains(note.rectangle)) {
                selectNote(note);
            }
        }
        selectorRect.setSize(0, 0);
    }

    private void clearSelection() {

        for (int i = selection.size() - 1; i >= 0; i--) {
            Note note = selection.get(i);
            if (note.duration <= 0) {
                notes.remove(note);
            }
        }
        selection.forEach(note -> {
            note.isSelected = false;
            view.drawNote(note);
        });
        selectedNote = null;
        selection.clear();
    }

    private void deleteNotes(Notes notes) {


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
            note.duration += Integer.signum(deltaX) * pageController.getTicksPerMeasure() * gridFraction;
        }
    }

    private void moveSelectedNotes(int deltaX, int deltaY) {

        // do some checks on block as a whole before moving anything
        for (Note note : selection) {
            if (note.rectangle.x < calcGridSize()) {
                deltaX = Math.max(deltaX, 0);
            }
            if (note.stringNum + deltaY < 0 ||
                note.stringNum + deltaY > trackType.numOfStrings - 1) {
                deltaY = 0;
            }
        }

        for (Note note : selection) {
            view.drawNote(note);
            note.start += deltaX * pageController.getTicksPerMeasure() * gridFraction;
            note.stringNum += deltaY;
            note.pitch = trackType.findNotePitch(note.stringNum, note.fret);
        }
    }

    public void moveSelectionArrowKeys(int dirX, int dirY) {
        moveSelectedNotes(dirX, dirY);
    }

    public void tabThroughNotes() {
        if (selectedNote != null) {
            notes.sortByStart();
            for (Note note : selection) {
                tabbedNotes.add(note);
            }
            long currentSelectionStart = selectedNote.start;
            clearSelection();
            for (Note note : notes) {
                if (!tabbedNotes.contains(note) && note.start >= currentSelectionStart) {
                    selectNote(note);
                    pageController.playSelection(this);
                    break;
                }
            }
        }
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

    public void removeBars(int measureStart, int measureEnd) {

        int afterRange = measureEnd + 1;
        int ticksPerMeasure = pageController.getTicksPerMeasure();
        long deltaStart = -1 * (afterRange - measureStart) * ticksPerMeasure;
console.log("d", deltaStart);
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            long measure = 1 + (note.start / ticksPerMeasure);
            if (measure >= measureStart && measure < afterRange) {
                notes.remove(note);
                view.drawNote(note);
            } else if (measure >= afterRange) {
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
            //changeNoteFret(number);
            view.showFretField(selectedNote, number);
        }
    }

    protected void handleVolumeField(int value) {
        volume = value * 127/100;
        pageController.handleTrackVolumeField(this, volume);
    }

    protected void handleTrackNameField() {
        name = view.getTrackNameField();
    }

    protected void handleFretFieldEnter() {
        if (selectedNote != null) {
            view.hideFretField();
            handleTrackInput(false);
        }
    }

    protected void handleFretFieldTab() {
        if (selectedNote != null) {
            handleFretFieldEnter();
            tabThroughNotes();
        }
    }

    protected void handleFretFieldChange() {
        if (selectedNote != null) {
            String fretAsString = view.getFretField();
            if ("".equals(fretAsString) || fretAsString == null) {
                changeNoteFret(0);
            } else {
                changeNoteFret(Integer.valueOf(fretAsString));
            }
        }
    }

    protected void handleTrackInput(boolean elementNeedsFocus) {
        pageController.handleTrackInput(this, elementNeedsFocus);
    }

    protected void handleCollapseButton(Constants c) {
        switch(c) {
            case BUTTON_TRACKCOLLAPSE:
                view.toggleCollapseButton(c);
                break;
            case BUTTON_TRACKEXPAND:
                view.toggleCollapseButton(c);
                break;
            default:
        }
    }

    protected void handleMuteButton(Constants c) {
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

    protected void handleInstrumentPicker(Instrument instrument) {
        if (Instruments.isDrumSet(instrument)) {
            setChannel(9);
        } else {
            //don't do this --but need to get from drum to other inst
            //setChannel(1);
        }
        setInstrument(instrument.number);
    }

    protected void handleGridSizePicker(double value) {
        gridFraction = value;
    }


    protected void handleTrackTypePicker(Object i) {
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