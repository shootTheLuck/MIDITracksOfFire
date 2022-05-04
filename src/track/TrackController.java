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

import actions.Actions;
import instruments.Instrument;
import note.Note;
import page.Page;
import page.PageView;
import themes.ThemeReader;
import utils.console;
import utils.BoundedRange;
import widgets.VelocitySlider;

interface MouseStrategy {
    public void doIt(MouseEvent evt);
}

public class TrackController {

    public Page pageController;
    private TrackView view;
    private Point dragStart = new Point();
    private Point dragStartGrid = new Point();
    private int startAverageVelocity;
    private boolean collapsingVelocityRange;
    private TrackType trackType;

    private MouseStrategy dragSelectorRect = new SelectorRectStrategy();
    private MouseStrategy moveNote = new NoteMoveStrategy();
    private MouseStrategy lengthenNote = new NoteLengthStrategy();
    private MouseStrategy lengthenNoteTentative = new NoteLengthTentativeStrategy();
    private MouseStrategy setNoteVelocity = new NoteVelocityStrategy();
    private MouseStrategy nullStrategy = new NullStrategy();
    private MouseStrategy mouseStrategy;

    protected boolean isMuted = false;
    protected boolean isSelected = false;
    private double gridFraction = 0.125;
    private VelocitySlider vSlider;

    private Note selectedNote;
    protected Note.List notes = new Note.List();
    private Note.List tabbedNotes = new Note.List();
    private Note.List selection = new Note.List();
    private String name = "untitled track";
    private int channel;
    private int volume;
    private Instrument instrument;
    protected Rectangle selectorRect = new Rectangle(0, 0);

    public TrackController(Page page) {
        pageController = page;
        trackType = new TrackType();
        view = new TrackView(this, name);
    }

    private int calcGridSize() {
        return (int)(gridFraction * PageView.measureSize);
    }

    //////////////////   MouseStrategy Classes   //////////////////

    class NullStrategy implements MouseStrategy {
        @Override
        public void doIt(MouseEvent evt) {}
    }

    class SelectorRectStrategy implements MouseStrategy {
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

            view.drawRectangle(selectorRect);
        }
    }

    class NoteMoveStrategy implements MouseStrategy {
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

            int xAmount = diffX / calcGridSize();
            int seriesId = dragStart.x + dragStart.y;
            pageController.addAction(new NoteMoveAction(TrackController.this, selection, xAmount, diffY, seriesId));
        }
    }

    class NoteLengthTentativeStrategy implements MouseStrategy {
        @Override
        public void doIt(MouseEvent evt) {

            int x = evt.getX();
            int y = evt.getY();

            //if (selectedNote.duration < 0 ||
            if (x <= dragStart.x ||
                    Math.abs(y - dragStart.y) > ThemeReader.getMeasure("track.strings.spacing")) {
                mouseStrategy = dragSelectorRect;
                notes.remove(selectedNote);
                view.drawNote(selectedNote);

                /* set duration to 0 to remove from gui */
                selectedNote.duration = 0;
            } else if (!trackType.isDrums()) {
                int x2 = findNearestGrid(x);
                int diffX = x2 - dragStartGrid.x;
                dragStartGrid.x = x2;
                lengthenSelectedNote(selectedNote, diffX);
            }
        }
    }

    class NoteLengthStrategy implements MouseStrategy {
        @Override
        public void doIt(MouseEvent evt) {
            int x = evt.getX();
            int x2 = findNearestGrid(x);
            int diffX = x2 - dragStartGrid.x;

            dragStartGrid.x = x2;
            if (diffX != 0) {
                int seriesId = dragStart.x + dragStart.y;
                pageController.addAction(new NoteLengthAction(TrackController.this, selection, diffX, seriesId));
            }
        }
    }

    class NoteVelocityStrategy implements MouseStrategy {
        @Override
        public void doIt(MouseEvent evt) {
            if (vSlider.isVisible()) {
                Rectangle bounds = vSlider.getBounds();
                Component drawArea = (Component)evt.getSource();
                Rectangle adjusted =
                        SwingUtilities.convertRectangle(vSlider.getParent(), bounds, drawArea);
                int y = evt.getY();

                vSlider.adjustValue(adjusted.y + adjusted.height - y);
            }
        }
    }

    //////////////////   end MouseStrategy classes  //////////////////

    //////////////////   Action Classes  //////////////////

    class NoteMoveAction extends Actions.Item {

        TrackController track;
        ArrayList<Note> notes;
        int x;
        int y;

        public NoteMoveAction(TrackController track, ArrayList<Note> notes, int x, int y, int series) {
            this.name = "moveNote(s)";
            this.track = track;
            this.notes = new ArrayList<Note>(notes);
            this.x = x;
            this.y = y;
            this.series = series;
        }

        public void execute() {
            moveSelectedNotes(notes, x, y);
        }

        public void redo() {
            clearSelection();
            for (Note note : this.notes) {
                selectNote(note);
            }
            moveSelectedNotes(notes, x, y);
        }

        public void undo() {
            clearSelection();
            for (Note note : this.notes) {
                selectNote(note);
            }
            moveSelectedNotes(notes, -x, -y);
        }
    }

    class NoteAddAction extends Actions.Item {

        TrackController track;
        int x;
        int y;
        boolean drum;
        Note note;

        public NoteAddAction(TrackController track, Note note) {
            this.name = "addNote";
            this.track = track;
            this.note = note;
        }

        public void execute() {
            //already done
        }

        public void redo() {
            if (this.note.duration > 0) {
                clearSelection();
                loadNote(this.note);
                selectNote(note);
                view.drawNote(this.note);
            }
        }

        public void undo() {
            deleteNote(this.note);
        }
    }

    class NoteLoadAction extends Actions.Item {
        TrackController track;
        Note.List notesToLoad;

        public NoteLoadAction(TrackController track, Note.List notes) {
            this.name = "loadNote(s)";
            this.track = track;
            notesToLoad = new Note.List();
                for (Note note : notes) {
                Note clone = note.clone();
                notesToLoad.add(clone);
            }
            //this.notesToLoad = notes;
        }

        public void execute() {
            pageController.selectTrack(track);
            clearSelection();
            for (Note note : this.notesToLoad) {
                loadNote(note);
                selectNote(note);
            }
        }

        public void redo() {
            execute();
        }

        public void undo() {
            pageController.selectTrack(track);
            for (Note note : this.notesToLoad) {
                selectNote(note);
            }
            pageController.cutSelection();
        }
    }

    class NoteLengthAction extends Actions.Item {

        int diffX;
        ArrayList<Note> notes;

        public NoteLengthAction(TrackController track, ArrayList<Note> notes, int diffX, int series) {
            this.name = "lengthenNote(s)";
            this.notes = new ArrayList<Note>(notes);
            this.diffX = diffX;
            this.series = series;
        }

        public void execute() {
            lengthenSelectedNotes(this.notes, diffX);
        }

        public void redo() {
            clearSelection();
            for (Note note : this.notes) {
                selectNote(note);
            }
            lengthenSelectedNotes(this.notes, diffX);
        }

        public void undo() {
            clearSelection();
            for (Note note : this.notes) {
                selectNote(note);
            }
            lengthenSelectedNotes(this.notes, -diffX);
        }

    }

    class NoteVelocityAction extends Actions.Item {

        int diffV;
        ArrayList<Note> notes;

        public NoteVelocityAction(ArrayList<Note> notes, int diffV) {
            this.name = "setNoteVelocities";
            this.notes = new ArrayList<Note>(notes);
            this.diffV = diffV;
        }

        public void execute() {
            adjustVelocities(this.notes, diffV);
        }

        public void redo() {
            clearSelection();
            for (Note note : this.notes) {
                selectNote(note);
            }
            adjustVelocities(this.notes, diffV);
        }

        public void undo() {
            clearSelection();
            for (Note note : this.notes) {
                selectNote(note);
            }
            adjustVelocities(this.notes, -diffV);
        }

    }

    //////////////////   end Action Classes  //////////////////

    protected void handleMouseDownDrawArea(MouseEvent evt) {

        int x = evt.getX();
        int y = evt.getY();

        dragStart.setLocation(x, y);
        dragStartGrid.setLocation(findNearestGrid(x), findNearestStringNum(y));
        selectorRect.setLocation(x, y);

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

            if (!selection.contains(note) && !evt.isShiftDown()) {
                clearSelection();
            }

            selectNote(note);
            double lastPartOfNote = selectedNote.rectangle.x + selectedNote.rectangle.width * 0.8;

            if (evt.isControlDown() || SwingUtilities.isRightMouseButton(evt)) {
                int medianVelocity = 0;
                int maxVelocity = 0;
                int minVelocity = 127;
                for (Note aNote : selection) {
                    maxVelocity = Math.max(maxVelocity, aNote.velocity);
                    minVelocity = Math.min(minVelocity, aNote.velocity);
                }
                medianVelocity = (maxVelocity + minVelocity) / 2;

                startAverageVelocity = medianVelocity;
                vSlider = pageController.showVelocitySlider(evt, medianVelocity);
                vSlider.setValue(medianVelocity);

                if (evt.isShiftDown()) {
                    collapsingVelocityRange = true;
                    vSlider.setVelocityRange(medianVelocity, medianVelocity);
                } else {
                    collapsingVelocityRange = false;
                    vSlider.setVelocityRange(minVelocity, maxVelocity);
                }

                mouseStrategy = setNoteVelocity;

            } else if (x > lastPartOfNote && channel != 9) {
                //view.changeCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                mouseStrategy = lengthenNote;

            } else if (evt.isAltDown()) {
                pageController.copySelection();
                pageController.pasteSelection();
                mouseStrategy = moveNote;

            } else {
                //view.changeCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                mouseStrategy = moveNote;
                //pageController.playNote(selectedNote, this);
            }

        } else if (SwingUtilities.isLeftMouseButton(evt)) {
            if (!evt.isShiftDown()) {
                clearSelection();
            }
            int stringNum = findNearestStringNum(y);
            if (stringNum > -1 && stringNum <= trackType.numOfStrings - 1) {
                boolean drum = trackType.isDrums();
                addNote(x, y, drum);
                mouseStrategy = lengthenNoteTentative;
            } else {
                mouseStrategy = dragSelectorRect;
            }
        } else {
            clearSelection();
            mouseStrategy = nullStrategy;
        }
    }

    protected void handleMouseUpDrawArea(MouseEvent evt) {

        //view.changeCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if (selectedNote != null && selectedNote.duration <= 0) {
            deleteNote(selectedNote);
            selectedNote = null;
        }

        if (mouseStrategy instanceof NoteVelocityStrategy) {
            pageController.hideVelocitySlider();
            int medianVelocity = vSlider.getMedianValue();
            if (collapsingVelocityRange) {
                setVelocities(selection, medianVelocity);
            } else {
                int delta = medianVelocity - startAverageVelocity;
                adjustVelocities(selection, delta);
            }
            pageController.playNote(selectedNote, this);

        } else if (mouseStrategy instanceof SelectorRectStrategy) {
            boolean shift = evt.isShiftDown();
            hideSelectorRect(shift);

        } else if (mouseStrategy instanceof NoteLengthStrategy) {
            pageController.playNote(selectedNote, this);

        } else if (mouseStrategy instanceof NoteLengthTentativeStrategy) {
            if (selectedNote != null) {
                pageController.addAction(new NoteAddAction(this, selectedNote));
                pageController.playNote(selectedNote, this);
            }

        } else if (mouseStrategy instanceof NoteMoveStrategy) {
            pageController.playNote(selectedNote, this);
        }

    }

    private void handleVelocitySlider() {

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

    public void setName(String n) {
        name = n;
        view.setTrackNameField(name);
    }

    public String getName() {
        return name;
    }

    private long setNoteStart(int x) {
        // zero-based double of measure position ie 1.25
        double measure = (double)x/PageView.measureSize;
        double corrected = Math.ceil(measure / gridFraction) * gridFraction;
        return (long)(corrected * pageController.getTicksPerMeasure());
    }

    public void setProgress(double progress, long tick) {
        int x = (int)(progress * PageView.measureSize);
        int soundAmount = 0;
        for (Note note: notes) {
            if (tick > note.start && tick < note.start + note.duration) {
                soundAmount += note.velocity;
                if (soundAmount > 126) break;
            }
        }
        view.showProgress(x, soundAmount);
    }

    public void cancelProgress() {
        view.cancelProgress();
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void loadNote(Note note) {
        trackType.assignStringAndFret(note);
        notes.add(note);
        if (channel == 9) {
            // 1/32 note
            note.duration = pageController.getTicksPerMeasure() / 32;
        }
        notes.sorted = false;
    }

    private Note addNote(int x, int y, boolean drum) {
        Note note = new Note();
        note.stringNum = findNearestStringNum(y);
        note.pitch = trackType.findNotePitch(note.stringNum, note.fret);
        x = findNearestGrid(x);
        note.start = setNoteStart(x);
        notes.add(note);
        if (drum) {
            // 1/32 note
            note.duration = pageController.getTicksPerMeasure() / 32;
        }
        notes.sorted = false;
        selectNote(note);
        return note;
    }

    public void setAsSelectedTrack() {
        isSelected = true;
        view.highliteBorder();
    }

    public void setAsNotSelectedTrack() {
        isSelected = false;
        view.deHighliteBorder();
        clearSelection();
    }

    public List<Note> getSelection() {
        return selection.sortByStart(true);
    }

    public List<Note> getNotes() {
        return notes.sortByStart(true);
    }

    public void selectAllNotes() {
        selection.clear();

        /* loop backward through notes */
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            selectNote(note);
        }
    }

    private void selectNote(Note note) {
        selectedNote = note;
        selectedNote.isSelected = true;
        /* ensure note is drawn last (on top of others) */
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

    private void hideSelectorRect(boolean shift) {
        view.drawRectangle(selectorRect);
        if (shift != true) {
            clearSelection();
        }

        /* loop backward through notes */
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            if (selectorRect.contains(note.rectangle)) {
                selectNote(note);
                selectedNote = note;
            }
        }
        selectorRect.setSize(0, 0);
    }

    private void clearSelection() {
        selection.forEach(note -> {
            note.isSelected = false;
            if (note.duration <= 0) {
                notes.remove(note);
            }
            view.drawNote(note);
        });
        selectedNote = null;
        selection.clear();
    }

    private void deleteNote(Note note) {
        if (notes.contains(note)) {
            notes.remove(note);
        }
        if (selection.contains(note)) {
            selection.remove(note);
        }
        view.drawNote(note);
    }

    public void deleteSelectedNotes() {
        for (Note note : selection) {
            notes.remove(note);
            view.drawNote(note);
        }
        selection.clear();
    }

    public Note.List cutSelectedNotes() {
        Note.List temp = new Note.List();
        for (Note note : selection) {
            notes.remove(note);
            view.drawNote(note);
            temp.add(note);
        }
        selection.clear();
        return temp;
    }

    public Note.List copySelectedNotes() {
        Note.List temp = new Note.List();
        for (Note note : selection) {
            Note clone = note.clone();
            temp.add(clone);
        }
        return temp;
    }

    public void pasteSelectedNotes(Note.List clipboard) {
        pageController.addAction(new NoteLoadAction(this, clipboard));
    }

    private void setVelocities(ArrayList<Note> notes, int velocity) {
        for (Note note : notes) {
            note.velocity = velocity;
            note.velocity = Math.min(Math.max(0, note.velocity), 127);
        }
    }

    private void adjustVelocities(List<Note> notes, int delta) {
        for (Note note : notes) {
            note.velocity += delta;
            note.velocity = Math.min(Math.max(0, note.velocity), 127);
        }
    }

    private void lengthenSelectedNote(Note note, int deltaX) {
        if (deltaX == 0) return;
        view.drawNote(note);
        note.duration += Integer.signum(deltaX) * pageController.getTicksPerMeasure() * gridFraction;
    }

    private void lengthenSelectedNotes(ArrayList<Note> notes, int deltaX) {
        if (deltaX == 0) return;
        for (Note note : notes) {
            lengthenSelectedNote(note, deltaX);
        }
    }

    private void moveSelectedNotes(ArrayList<Note> notes, int deltaX, int deltaY) {

        // do some checks on block as a whole before moving anything
        for (Note note : notes) {
            if (note.rectangle.x < calcGridSize()) {
                deltaX = Math.max(deltaX, 0);
            }
            if (note.stringNum + deltaY < 0 ||
                note.stringNum + deltaY > trackType.numOfStrings - 1) {
                deltaY = 0;
            }
        }

        if (deltaX == 0 && deltaY == 0) {
            return;
        }

        for (Note note : notes) {
            view.drawNote(note);
            note.start += deltaX * pageController.getTicksPerMeasure() * gridFraction;
            note.stringNum += deltaY;
            note.pitch = trackType.findNotePitch(note.stringNum, note.fret);
        }
    }

    public void moveSelectionArrowKeys(int dirX, int dirY) {
        pageController.addAction(new NoteMoveAction(this, selection, dirX, dirY, -1));
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
        return (int)(dist * (gridFraction * PageView.measureSize));
    }

    private int findNearestStringNum(int y) {
        int topMargin = ThemeReader.getMeasure("track.strings.margin.top");
        int stringSpacing = ThemeReader.getMeasure("track.strings.spacing");
        int stringNum = (int)Math.round((double)(y - topMargin) / stringSpacing);
        return stringNum;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    //TODO template pattern?
    public void setInstrument(int number) {
        if (channel == 9) {
            for (Instrument drum : Instrument.drumList) {
                if (drum.number == number) {
                    instrument = drum;
                    view.setInstrumentName(drum.name);
                    break;
                }
            }
        } else {
            for (Instrument inst : Instrument.list) {
                if (inst.number == number) {
                    instrument = inst;
                    view.setInstrumentName(inst.name);
                    break;
                }
            }
        }
    }

    public void setTrackType(TrackType type) {
        if (!trackType.toString().equals(type.toString())) {
            trackType = type;
            for (Note note : notes) {
                trackType.assignStringAndFret(note);
            }
            view.setTrackType(type);
        }
    }

    public void insertBars(int numberToAdd, int addBefore) {
        int ticksPerMeasure = pageController.getTicksPerMeasure();
        long noteStartDelta = numberToAdd * ticksPerMeasure;
        for (Note note : notes) {
            long noteMeasure = 1 + (note.start / ticksPerMeasure);
            if (noteMeasure >= addBefore) {
                note.start += noteStartDelta;
            }
        }
    }

    public void removeBars(int measureStart, int measureEnd) {

        int afterRange = measureEnd + 1;
        int ticksPerMeasure = pageController.getTicksPerMeasure();
        long noteStartDelta = (afterRange - measureStart) * ticksPerMeasure;

        /* loop backward through notes */
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            long noteMeasure = 1 + (note.start / ticksPerMeasure);
            if (noteMeasure >= measureStart && noteMeasure < afterRange) {
                notes.remove(note);
                //view.drawNote(note);
            } else if (noteMeasure >= afterRange) {
                note.start -= noteStartDelta;
            }
        }
        view.drawNew();
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

    protected void handleMuteButton() {
        if (isMuted == true) {
            isMuted = false;
            view.showUnMuted();
        } else {
            isMuted = true;
            view.showMuted();
        }
        pageController.handleMuteButton(this, isMuted);
    }

    protected void handleInstrumentPicker(Instrument instrument) {
        if (Instrument.isDrumSet(instrument)) {
            setChannel(9);
        } else {
            //TODO: multiple tracks with same channel. is this ok?
            setChannel(1);
        }
        setInstrument(instrument.number);
    }

    protected void handleGridSizePicker(double value) {
        gridFraction = value;
    }

    protected void handleTrackTypePicker(String name) {
        if (name.equals("guitar")) {
            setTrackType(new TrackTypeGuitar());
        } else if (name.equals("bass")) {
            setTrackType(new TrackTypeBass());
        } else if (name.equals("drums")) {
            setTrackType(new TrackTypeDrums());
        }
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