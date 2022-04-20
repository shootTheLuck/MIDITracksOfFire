package page;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.Timer;

import actions.Actions;
import midi.Midi;
import note.Note;
import themes.ThemeReader;
import track.TrackController;
import track.TrackType;
import track.TrackTypeGuitar;
import track.TrackTypeBass;
import track.TrackTypeDrums;
import widgets.VelocitySlider;
import utils.console;


public class Page {

    private int numOfMeasures = 50;
    private int minNumOfMeasures = 50;

    private String prefFile = "config/preferences.txt";
    private Properties preferences;
    private int minWidth = 3003;
    private PageView view;
    private Midi midi;
    private List<Note> clipboard;
    private int BPM = 120;
    private int measureStart = 1;
    private int resolution;
    private Timer progressTimer;

    private boolean isPlaying = false;

    private TrackController selectedTrack;
    private List<TrackController> tracks;
    private File file;

    public Page() {
        preferences = new Properties();
        setDefaultPreferences();
        boolean themed = false;

        try (FileInputStream fis = new FileInputStream(prefFile)) {
            preferences.load(fis);
            String themeFile = preferences.getProperty("theme");
            if (themeFile != null && !themeFile.isEmpty()) {
                ThemeReader.loadTheme("themes/" + themeFile);
                themed = true;
            }
        } catch (FileNotFoundException ex) {
            console.log("an error occured trying to load preferences file", prefFile, ":", ex);
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
            handleProgressTimer(currentTick);
        });

        addNewTrack();
        //view.pack();
        view.reset();
        if (themed) {
            view.setTheme();
        }
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

    public void setDefaultPreferences() {
        preferences.setProperty("theme", "default.theme");
        preferences.setProperty("window.width", "1000");
        preferences.setProperty("window.height", "800");
        preferences.setProperty("midiDirectory", "midi");
        preferences.setProperty("soundFont", "sf2/Windows.sf2");
    }

    public void startNewFile() {
        removeAllTracks();
        view.setTitle("untitled");
        midi.unMuteAllTracks();
        addNewTrack();
    }

    public void loadFile(String filename) {
        removeAllTracks();
        midi.unMuteAllTracks();
        try {
            file = new File(filename);
            Sequence sequence = MidiSystem.getSequence(file);
            view.setTitle(file.getName());

            String pathWithoutFileName = file.getParent();
            setPreference("midiDirectory", pathWithoutFileName);

            long l = sequence.getTickLength();
            numOfMeasures = (int) l/(sequence.getResolution() * 4);
            numOfMeasures = Math.max(numOfMeasures, minNumOfMeasures);
            PageView.width = Math.max(minWidth, numOfMeasures * PageView.measureSize + PageView.measureSize);
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
        ArrayList<Note> notes = new ArrayList<Note>();

        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            long tick = event.getTick();
            MidiMessage message = event.getMessage();

            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage)message;
                int messageType = metaMessage.getType();

                if (messageType == 3) {
                    String trackName = new String(metaMessage.getData());
                    trackController.setName(trackName);
                } else if (messageType == 81) {
                    // BPM
                    /* https://stackoverflow.com/questions/22798345/
                     * how-to-get-integer-value-from-byte-array-returned-by-metamessage-getdata
                     */
                    byte[] data = metaMessage.getData();
                    int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                    BPM = 60000000 / tempo;
                    view.setBPMField(BPM);
                }

            } else if (message instanceof ShortMessage) {

                ShortMessage shortMessage = (ShortMessage)message;

                int command = shortMessage.getCommand();
                int pitch = shortMessage.getData1();
                int velocity = shortMessage.getData2();

                if (command == ShortMessage.PROGRAM_CHANGE) {

                    int channel = shortMessage.getChannel();
                    int instrumentNum = shortMessage.getData1();

                    if (channel == 9) {
                        trackType = new TrackTypeDrums();
                    } else if (instrumentNum >= 32 && instrumentNum <= 39) {
                        trackType = new TrackTypeBass();
                    } else {
                        trackType = new TrackTypeGuitar();
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
                            //trackNote.duration = tick - trackNote.start;
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
        track.setTrackType(new TrackTypeGuitar());
        track.setChannel(tracks.size());

        //TODO template pattern?
        track.setInstrument(0);
        track.setVolume(127);
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

    protected void handleInsertBarsDialog(int numberToAdd, int addBefore, boolean allTracks) {
        if (numberToAdd > 0) {
            numOfMeasures += numberToAdd;
            view.addMeasures(numberToAdd, minNumOfMeasures);
            if (allTracks) {
                for (TrackController track : tracks) {
                    track.insertBars(numberToAdd, addBefore);
                }
            } else {
                selectedTrack.insertBars(numberToAdd, addBefore);
            }
        }
    }

    protected void handleRemoveBarsDialog(int start, int end, boolean allTracks) {
        int numberToRemove = end - start + 1;
        if (start > 0 && end > 0) {
            if (allTracks) {
                numOfMeasures -= numberToRemove;
                numOfMeasures = Math.max(numOfMeasures, minNumOfMeasures);
                view.addMeasures(-numberToRemove, minNumOfMeasures);
                for (TrackController track : tracks) {
                    track.removeBars(start, end);
                }
            } else {
                selectedTrack.removeBars(start, end);
            }
        }
    }

    private void playAll() {
        int measureStart = view.getPlayStartField() - 1;
        long startTime = measureStart * getTicksPerMeasure();

        view.setScrollPositionToMeasure(measureStart);
        midi.play(tracks, BPM, resolution, startTime);
        isPlaying = true;
        progressTimer.start();
    }

    public void playSelection(TrackController track) {
        List<Note> selection = track.getSelection();
        if (selection.size() > 0) {
            midi.playSelection(track, BPM, resolution);
            isPlaying = true;
        }
    }

    public int getTicksPerMeasure() {
        return resolution * 4;
    }

    private void stopAll() {
        midi.stop();
        progressTimer.stop();
        view.cancelProgress();
        for (TrackController track : tracks) {
            track.cancelProgress();
        }
        isPlaying = false;
    }

    public void addAction(Actions.Item action) {
        Actions.add(action);
    }

    public void shutDown() {
        console.log("shutting down...");
        //console.log("items", Actions.list.size(), "unsaved?", Actions.hasUnsavedChanges());
        midi.close();
        view.close();
        String settingsFileName = ThemeReader.getSettingsName();

        if (settingsFileName != null) {
            setPreference("theme", settingsFileName);
        }
        try (FileOutputStream out = new FileOutputStream(prefFile)) {
            preferences.store(out, "---Preferences File---");
        } catch (FileNotFoundException ex) {
            console.log("an error occured trying to save preferences to file", prefFile, ":", ex);
        } catch (IOException ex) {
            //
        }
        System.exit(0);
    }

    private void openFile() {
        String path;
        String currentDirectory = System.getProperty("user.dir");
        String midiDirectory = preferences.getProperty("midiDirectory");
        if (midiDirectory != null && !midiDirectory.isEmpty()) {
            if (midiDirectory == "midi") {
                path = currentDirectory + "/" + midiDirectory;
            } else {
                path = midiDirectory;
            }
        } else {
            path = currentDirectory;
        }

        String fileName = view.showFileChooser("mid", path);
        if (!"".equals(fileName)) {
            loadFile(fileName);
        }
    }

    private void saveFileAs() {
        String path;
        String currentDirectory = System.getProperty("user.dir");
        String midiDirectory = preferences.getProperty("midiDirectory");
        if (midiDirectory != null && !midiDirectory.isEmpty()) {
            if (midiDirectory.equals("midi")) {
                path = currentDirectory + "/" + midiDirectory;
            } else {
                path = midiDirectory;
            }
        } else {
            path = currentDirectory;
        }
        String currentName = file.getName();
        String fileName = view.showFileSaver("mid", path, currentName);
        if (!"".equals(fileName)) {
            file = new File(fileName);
            midi.writeToFile(file, tracks, BPM, resolution);
            view.setTitle(file.getName());
        }
    }

    private void saveFile() {
        if (file != null) {
            midi.writeToFile(file, tracks, BPM, resolution);
        } else {
            saveFileAs();
        }
    }

    private void openSoundFont() {
        String sf2 = view.showFileChooser("sf2", "sf2");
        if (!"".equals(sf2)) {
            midi.setSoundfont(sf2);
        }
    }

    private void chooseTheme() {
        String fileName = view.showFileChooser("theme", null);
        if (!"".equals(fileName)) {
            ThemeReader.loadTheme(fileName);
            view.setTheme();
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
                case KeyEvent.VK_TAB:
                    selectedTrack.tabThroughNotes();
                    break;
                case KeyEvent.VK_SPACE:
                    selectedTrack.tabThroughNotes();
                    break;
            }
        }
    }

    public void handleMeasureSizeSlider(int sliderValue) {
        view.adjustMeasureSize(sliderValue, numOfMeasures);
    }

    public void handleMenuItem(Constants evt) {
        switch(evt) {
            case MENU_FILE_NEW:
                startNewFile();
                break;
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
                shutDown();
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
            case MENU_VIEW_SETTHEME:
                chooseTheme();
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
            case MENU_EDIT_UNDO:
                Actions.undo();
                break;
            case MENU_EDIT_REDO:
                Actions.redo();
                break;
            case MENU_EDIT_INSERTBARS:
                view.showInsertBarsDialog(1, 1);
                break;
            case MENU_EDIT_REMOVEBARS:
                view.showRemoveBarsDialog(0, 0);
                break;
            default:
        }
    }

    public void handlePlayControls(Constants c) {
        switch(c) {

            case BUTTON_PLAY:
                if (isPlaying == false) {
                    playAll();
                    view.showPlaying();
                } else {
                    stopAll();
                    view.showStopped();
                }
                break;

            case BUTTON_PLAYSELECTION:
                if (isPlaying == false) {
                    playSelection(selectedTrack);
                    //check again to see if there was an actual selection
                    if (isPlaying == true) {
                        view.showPlaying();
                    }
                } else {
                    stopAll();
                    view.showStopped();
                }
                break;

            case FIELD_PLAYSTART:
                int measureStart = view.getPlayStartField() - 1;
                view.setScrollPositionToMeasure(measureStart);
                break;
            case FIELD_LOOPSTART:
                //TODO
                break;
            case FIELD_LOOPSTOP:
                //TODO
                break;
            case FIELD_BPM:
                BPM = view.getBPMField();
                break;
            default:
        }
        view.setFocus();
    }

    public void handleMuteButton(TrackController track, boolean muted) {
        int index = tracks.indexOf(track);
        midi.muteTrack(index, muted);
    }

    public void handleTrackVolumeField(TrackController track, int value) {
        midi.setTrackVolume(track.getChannel(), value);
    }

    public void handleSoundComplete() {
        stopAll();
        view.showStopped();
    }

    public void handleProgressTimer(long tick) {
        double progress = (double)tick / getTicksPerMeasure();
        view.setProgress(progress);

        for (TrackController track : tracks) {
            track.setProgress(progress, tick);
        }
        //selectedTrack.setProgress(progress, tick);
    }

    public VelocitySlider showVelocitySlider(MouseEvent evt, int velocity) {
        return view.showVelocitySlider(evt, velocity);
    }

    public void hideVelocitySlider() {
        view.hideVelocitySlider();
    }

    @Override
    public String toString() {
        return "Page";
    }
}