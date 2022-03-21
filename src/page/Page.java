package page;

import java.awt.event.*;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sound.midi.*;
import javax.swing.*;

import track.TrackController;
import track.TrackTypes;
import track.TrackType;
import track.VelocitySlider;
import midi.Midi;
import note.Note;
import utils.*;


public class Page {

    // global!!
    public static int width = 3003;
    public static int measureSize = 150;
    public static int scrollValue = 0;
    public static int numOfMeasures = 20;
    public static Properties preferences;

    private final String prefFile = "config/preferences.txt";
    private final int minWidth = 3003;
    private PageView view;
    private Midi midi;
    private TrackController selectedTrack;
    private List<Note> clipboard;
    private int BPM = 120;
    private Timer progressTimer;

    private boolean isPlaying = false;

    private List<TrackController> tracks;
    private int resolution;
    private String name = "Untitled";
    private File file;

    public Page() {
        preferences = new Properties();

        try (FileInputStream fis = new FileInputStream(prefFile)) {
            preferences.load(fis);
        } catch (FileNotFoundException ex) {
            console.log("an error occured trying to load file", prefFile, ":", ex);
            // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) {
            //
        }

        view = new PageView(this);
        midi = new Midi(this);
        resolution = 960;
        tracks = new ArrayList<>();
        clipboard = new ArrayList<>();
        progressTimer = new Timer(20, (ActionEvent evt) -> {
            long currentTick = midi.getTickPosition();
            handleSoundProgress(currentTick);
        });

        addNewTrack();
        //view.pack();
        view.reset();
        view.setVisible(true);

    }

    public String getPreference(String prefName) {
        return preferences.getProperty(prefName);
    }

    public void setPreference(String prefName, String prefValue) {
        preferences.setProperty(prefName, prefValue);
    }

    public void setPreference(String prefName, int prefValue) {
        preferences.setProperty(prefName, String.valueOf(prefValue));
    }

    public void loadFile(String filename) {
        removeAllTracks();
        midi.unMuteAllTracks();
        try {
            file = new File(filename);
            view.setTitle(file.getName());

            Sequence sequence = MidiSystem.getSequence(file);
            long l = sequence.getTickLength();
            numOfMeasures = (int) l/(sequence.getResolution() * 4);
            width = Math.max(minWidth, numOfMeasures * measureSize + measureSize);
            resolution = sequence.getResolution();
            view.reset();
            for (Track track : sequence.getTracks()) {
                loadTrack(track);
            }
            selectTrack(tracks.get(0));

        } catch (Exception e) {
            if (e instanceof MidiUnavailableException) {
                console.log("midi unavailable on this system: " + e);
            } else if (e instanceof InvalidMidiDataException) {
                console.log("file", filename, "does not appear to be valid midi file: " + e);
            } else {
                console.log("an error occured trying to load file", filename, ":", e);
            }
            addNewTrack();
        }
    }

    private void loadTrack(Track track) {
        TrackController trackController = new TrackController(this);
        TrackType trackType = null;
        String trackName = null;
        ArrayList<Note> notes = new ArrayList<Note>();

        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            long tick = event.getTick();
            MidiMessage message = event.getMessage();

            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                int messageType = metaMessage.getType();

                if (messageType == 3) {
                    // track name
                    trackController.setName(new String(metaMessage.getData()));
                } else if (messageType == 81) {
                    // BPM
                    /* https://stackoverflow.com/questions/22798345/
                     * how-to-get-integer-value-from-byte-array-returned-by-metamessage-getdata
                     */
                    byte[] data = metaMessage.getData();
                    int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                    BPM = 60000000 / tempo;
                    view.playControls.setBPMField(BPM);
                }

            } else if (message instanceof ShortMessage) {

                ShortMessage shortMessage = (ShortMessage) message;

                int command = shortMessage.getCommand();
                int pitch = shortMessage.getData1();
                int velocity = shortMessage.getData2();

                if (command == ShortMessage.PROGRAM_CHANGE) {

                    int channel = shortMessage.getChannel();
                    int instrumentNum = shortMessage.getData1();

                    if (channel == 9) {
                        trackType =  TrackTypes.Drums;
                    } else if (instrumentNum >= 32 && instrumentNum <= 39) {
                        trackType =  TrackTypes.Bass;
                    } else {
                        trackType =  TrackTypes.Guitar;
                    }

                    trackController.setTrackType(trackType);
                    trackController.setChannel(channel);
                    trackController.setInstrument(instrumentNum);

                } else if (command == ShortMessage.CONTROL_CHANGE) {
                    if (trackController != null) {
                        switch(shortMessage.getData1()) {
                            case 7:
                                trackController.setVolume(shortMessage.getData2());
                                break;
                            case 8:
                                // balance
                                break;
                            case 10:
                                // pan
                                break;
                            case 91:
                                // reverb
                                break;
                            case 92:
                                // tremolo
                                break;
                            case 93:
                                // chorus
                                break;
                            case 94:
                                // formerly Celeste [Detune] Depth
                                break;
                            case 95:
                                // phaser
                                break;
                        }
                    }
                    // console.log("Other shortMessage. Command:",shortMessage.getCommand(), shortMessage.getData1(), shortMessage.getData2());
                } else if (command == ShortMessage.NOTE_ON && velocity > 0) {
                    Note note = new Note();
                    note.start = tick;
                    note.pitch = pitch;
                    note.velocity = velocity;
                    notes.add(note);

                } else if ((command == ShortMessage.NOTE_ON && velocity == 0) ||
                            command == ShortMessage.NOTE_OFF) {
                    /* loop backwards */
                    for (int j = notes.size() - 1; j >= 0; j--) {
                        Note trackNote = notes.get(j);
                        if (trackNote.pitch == pitch) {
                            trackNote.duration = tick - trackNote.start;
                            trackController.loadNote(trackNote);
                            break;
                        }
                    }

                } else {
                    // console.log("Other shortMessage. command:", command, shortMessage.getData1(), shortMessage.getData2());
                    // command
                    // 224 pitch bend
                }

            } else {
                // console.log("Other long message.", message);
            }
        }

        // right now only loading a track if there is an instrument (and trackType)
        // not loading first track from easybeat
        if (trackType != null) {
            addTrack(trackController);
        }
    }

    private void selectTrack(TrackController track) {
        if (selectedTrack != null && selectedTrack != track) {
            selectedTrack.setAsNotSelectedTrack();
        }
        selectedTrack = track;
        track.setAsSelectedTrack();
    }

    private void addTrack(TrackController track) {
        tracks.add(track);
        view.addTrackView(track.getView(), tracks.size());
    }

    private void addNewTrack() {
        TrackController track = new TrackController(this);
        track.setTrackType(TrackTypes.Guitar);
        track.setChannel(tracks.size());
        track.setInstrument(0);
        track.setVolume(127);
        track.setScrollPosition(scrollValue);
        addTrack(track);
        selectTrack(track);
    }

    private void removeSelectedTrack() {
        if (selectedTrack != null) {
            for (TrackController track : tracks) {
                if (track == selectedTrack) {
                    tracks.remove(track);
                    break;
                }
            }
            view.removeTrackView(selectedTrack.getView());
        }
    }

    private void removeAllTracks() {
        tracks.clear();
        view.removeAllTrackViews();
    }

    public void cutSelection() {
        clipboard = selectedTrack.cutSelectedNotes();
    }

    public void copySelection() {
        clipboard = selectedTrack.copySelectedNotes();
    }

    public void pasteSelection() {
        selectedTrack.pasteSelectedNotes(clipboard);
    }

    private void playAll() {
        int measureStart = view.playControls.getPlayStartField();
        long startTime = (measureStart - 1) * getResolution() * 4;

        setScrollPositionToMeasure(measureStart);
        midi.play(tracks, BPM, getResolution(), startTime);
        progressTimer.start();
    }

    public void playSelection(TrackController track) {
        List<Note> selection = track.getSelection();
        if (selection.size() > 0) {
            ////need to provide nested arraylist
            List<List<Note>> allNotes = new ArrayList<>();
            allNotes.add(selection);
            midi.playSelection(allNotes, track, BPM, getResolution());
        }
    }

    public int getResolution() {
        return resolution;
    }

    public int getTicksPerMeasure() {
        return resolution * 4;
    }

    private void stopAll() {
        midi.stop();
        progressTimer.stop();
        view.cancelProgress();
        selectedTrack.cancelProgress();
        isPlaying = false;
    }

    public void shutDown() {
        console.log("shutting down...");

        try (FileOutputStream out = new FileOutputStream(prefFile)) {
            preferences.store(out, "---Preferences File---");
            out.close();
        } catch (FileNotFoundException ex) {
            console.log("an error occured trying to save to file", prefFile, ":", ex);
            // FileNotFoundException catch is optional and can be collapsed
        } catch (IOException ex) {
            //
        }
        System.exit(0);
    }

    private void openFile() {
        String fileName = view.showFileChooser("mid");
        if (!"".equals(fileName)) {
            loadFile(fileName);
        }
    }

    private void saveFile() {
        if (file != null) {
            midi.writeToFile(file, tracks, BPM, getResolution());
        } else {
            saveFileAs();
        }
    }

    private void saveFileAs() {
        String filename = view.showFileSaver("mid");
        if (!"".equals(filename)) {
            file = new File(filename);
            midi.writeToFile(file, tracks, BPM, getResolution());
            view.setTitle(file.getName());
        }
    }

    private void openSoundFont() {
        String sf2 = view.showFileChooser("sf2");
        if (!"".equals(sf2)) {
            midi.setSoundfont(sf2);
        }
    }

    public void handleTrackInput(TrackController track, boolean elementNeedsFocus) {
        selectTrack(track);
        if (!elementNeedsFocus) {
            view.setFocus();
        }
    }

    public void handleKeys(int keyCode) {
        if (keyCode >= 96 && keyCode <= 105) {
            int number = keyCode - 96;
            selectedTrack.showFretField(number);
        } else {
            switch(keyCode) {
                case KeyEvent.VK_UP:
                    selectedTrack.moveSelectionArrowKeys(0, -1);
                    break;
                case KeyEvent.VK_DOWN:
                    selectedTrack.moveSelectionArrowKeys(0, 1);
                    break;
                case KeyEvent.VK_LEFT:
                    selectedTrack.moveSelectionArrowKeys(-1, 0);
                    break;
                case KeyEvent.VK_RIGHT:
                    selectedTrack.moveSelectionArrowKeys(1, 0);
                    break;
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_BACK_SPACE:
                    selectedTrack.deleteSelectedNotes();
                    break;
                case KeyEvent.VK_ENTER:
                    handlePlayControls(Constants.BUTTON_PLAY);
                    break;
            }
        }
    }

    public void handleNumberSliderOLD(int delta) {
        Page.measureSize += delta;

        if (Page.measureSize <= 50) {
            Page.measureSize = 50;
        }

        Page.width = Page.numOfMeasures * Page.measureSize + Page.measureSize;
        //int currentWidth = view.getCurrentWidth();
        //Page.width = Math.max(Page.width, currentWidth);

        view.showInfo(Page.measureSize);
        view.adjustMeasureSize();
        for (TrackController track : tracks) {
            track.adjustMeasureSize();
        }
    }

    public void handleMeasureSizeSlider(int sliderValue) {
        //Math.min(Math.max(min, value), max);
        //Page.measureSize = Math.max(50, Math.min(sliderValue, 620));
        double minimumMeasureSize = 50.0;
        double maximumMeasureSize = view.getCurrentWidth() * 0.8;
        Page.measureSize = (int) Math.min(Math.max(minimumMeasureSize, sliderValue), maximumMeasureSize);

        Page.width = Page.numOfMeasures * Page.measureSize + Page.measureSize;
        //int currentWidth = view.getCurrentWidth();
        //Page.width = Math.max(Page.width, currentWidth);

        for (TrackController track : tracks) {
            track.adjustMeasureSize();
        }
        view.adjustMeasureSize();
        //handleScrollChange();
    }

    public void handleMenuItem(Constants evt) {
        switch(evt) {
            case MENU_FILE_OPEN:
                openFile();
                break;
            case MENU_FILE_SAVE:
                saveFile();
                break;
            case MENU_FILE_SAVEAS:
                saveFileAs();
                break;
            case MENU_FILE_CLOSE:
                view.disableMenuItem(Constants.MENU_FILE_CLOSE);
                break;
            case MENU_FILE_QUIT:
                shutDown();
                break;
            case MENU_TRACK_ADD:
                addNewTrack();
                break;
            case MENU_TRACK_REMOVE:
                removeSelectedTrack();
                break;
            case MENU_MUSIC_PLAY:
                handlePlayControls(Constants.BUTTON_PLAY);
                break;
            case MENU_MUSIC_PLAYSELECTION:
                handlePlayControls(Constants.BUTTON_PLAYSELECTION);
                break;
            case MENU_MUSIC_STOP:
                handlePlayControls(Constants.BUTTON_PLAY);
                break;
            case MENU_MUSIC_FIND:
                //TODO;
                break;
            case MENU_MUSIC_SOUNDFONT:
                openSoundFont();
                break;
            case MENU_EDIT_CUT:
                cutSelection();
                break;
            case MENU_EDIT_COPY:
                copySelection();
                break;
            case MENU_EDIT_PASTE:
                pasteSelection();
                break;
            default:
        }
    }

    public void handlePlayControls(Constants c) {
        switch(c) {

            case BUTTON_PLAY:
                if (isPlaying == false) {
                    playAll();
                    view.playControls.togglePlayButton(Constants.BUTTON_STOP);
                    view.menuBar.toggleMusicPlay(Constants.BUTTON_STOP);
                    isPlaying = true;
                } else {
                    stopAll();
                    view.playControls.togglePlayButton(Constants.BUTTON_PLAY);
                    view.menuBar.toggleMusicPlay(Constants.BUTTON_PLAY);
                    isPlaying = false;
                }
                break;

            case BUTTON_PLAYSELECTION:
                if (isPlaying == false) {
                    playSelection(selectedTrack);
                    view.playControls.togglePlayButton(Constants.BUTTON_STOP);
                    view.menuBar.toggleMusicPlay(Constants.BUTTON_STOP);
                    isPlaying = true;
                } else {
                    stopAll();
                    view.playControls.togglePlayButton(Constants.BUTTON_PLAY);
                    view.menuBar.toggleMusicPlay(Constants.BUTTON_PLAY);
                    isPlaying = false;
                }
                break;

            case FIELD_PLAYSTART:
                int measureStart = view.playControls.getPlayStartField();
                setScrollPositionToMeasure(measureStart);
            case FIELD_LOOPSTART:
                //TODO
            case FIELD_LOOPSTOP:
                //TODO
            case FIELD_BPM:
                BPM = view.playControls.getBPMField();
            default:
        }
        view.setFocus();
    }

    public void handleMuteButton(TrackController t, boolean muted) {
        int index = tracks.indexOf(t);
        midi.muteTrack(index, muted);
    }

    public void handleSoundComplete() {
        stopAll();
        view.playControls.togglePlayButton(Constants.BUTTON_PLAY);
        view.menuBar.toggleMusicPlay(Constants.BUTTON_PLAY);
    }

    public void handleSoundProgress(long tick) {
        view.setProgress(tick);
        selectedTrack.setProgress(tick);
        int currentMeasure = (int) tick/getTicksPerMeasure();
        int currentPosition = currentMeasure * Page.measureSize - scrollValue;
        int currentWidth = view.getCurrentWidth();
        if (currentPosition > currentWidth) {
            scrollValue += currentPosition;
            handleScrollChange();
        }
    }

    public void handleScrollChange() {
        if (view != null) {
            view.setHorizontalScroll(scrollValue);
        }
        for (TrackController track : tracks) {
            track.setScrollPosition(scrollValue);
        }
    }

    public void setScrollPositionToMeasure(int number) {
        scrollValue = Page.measureSize * (number - 1);
        handleScrollChange();
    }

    public void handleHorizontalScrollBar(int value) {
        //new Exception().printStackTrace();
//new Error().printStackTrace();
        scrollValue = Page.measureSize * (value/Page.measureSize);
        handleScrollChange();
    }

    public void handleVerticalScrollBar(int value) {
        //console.log("here");
        if (view != null) {
            view.setVerticalScroll(value);
        }
    }

    public VelocitySlider showVelocitySlider(MouseEvent evt, Note selectedNote) {
        return view.showVelocitySlider(scrollValue, evt, selectedNote);
    }

    public void hideVelocitySlider() {
        view.hideVelocitySlider();
    }

    @Override
    public String toString() {
        return "Page";
    }
}