package midi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

import page.Page;
import note.Note;
import track.TrackController;
import utils.console;

//// ****** Not using yet ****** ////
public class MidiReader {

    private Page pageController;
    private Synthesizer midiSynth;
    private Sequencer sequencer;
    private Receiver midiReceiver;
    private Soundbank soundBank;
    private Sequence playSequence;

    public MidiReader(Page pageController) {
        this.pageController = pageController;
    }

    public Sequence loadFile(File file) {
        try {
            Sequence sequence = MidiSystem.getSequence(file);

            long l = sequence.getTickLength();

            //// not implemented in page yet
            //pageController.setResolution(sequence.getResolution());
            //pageController.setNumOfMeasures((int)l/(sequence.getResolution() * 4));

            Track[] tracks = sequence.getTracks();
            for (Track track : tracks) {
                loadTrack(track);
            }
            return sequence;

        } catch (Exception e) {
            if (e instanceof MidiUnavailableException) {
                console.error("midi unavailable on this system: " + e);
            } else if (e instanceof InvalidMidiDataException) {
                console.error("file", file.getName(), "does not appear to be valid midi file: " + e);
            }
        }
        return null;
    }

    private void loadTrack(Track track) {

        ArrayList<Note> notes = new ArrayList<Note>();
        String trackName = "";
        int channel = 0;
        int instrumentNum = 0;
        int volume = 80;

        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            long tick = event.getTick();
            MidiMessage message = event.getMessage();

            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage)message;
                int messageType = metaMessage.getType();

                if (messageType == 3) {
                    trackName = new String(metaMessage.getData());
                } else if (messageType == 81) {
                    // BPM
                    /* https://stackoverflow.com/questions/22798345/
                     * how-to-get-integer-value-from-byte-array-returned-by-metamessage-getdata
                     */
                    byte[] data = metaMessage.getData();
                    int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                    //// not implemented in page yet
                    //pageController.setBPM( 60000000 / tempo);
                }

            } else if (message instanceof ShortMessage) {

                ShortMessage shortMessage = (ShortMessage)message;

                int command = shortMessage.getCommand();
                int pitch = shortMessage.getData1();
                int velocity = shortMessage.getData2();

                if (command == ShortMessage.PROGRAM_CHANGE) {

                    channel = shortMessage.getChannel();
                    instrumentNum = shortMessage.getData1();

                } else if (command == ShortMessage.CONTROL_CHANGE) {
                    //if (trackController != null) {
                        switch(shortMessage.getData1()) {
                            case 7:
                                //trackController.setVolume(shortMessage.getData2());
                                volume = shortMessage.getData2();
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
                    //}
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

        // right now only loading a track if it has notes
        // not loading first track from easybeat
        if (notes.size() > 0) {
            //// not implemented in page yet
            //TrackController tController = pageController.loadTrack(trackName, instrumentNum, channel, volume);
            //for (Note note : notes) {
                //tController.loadNote(note);
            //}
        }
    }
}


//pageController implementations:

//public void loadFile(String filename) {
    //removeAllTracks();
    //midi.unMuteAllTracks();
    //try {
        //file = new File(filename);
        //midiReader.loadFile(file);
        //selectTrack(tracks.get(0));
        //fileChecksum = generateChecksum();
        //view.setTitle(file.getName());

    //} catch (Exception e) {
        //console.error("an error occured trying to load file", filename, ":", e);
        //addNewTrack();
    //}
//}


//public void setResolution(int number) {
    //resolution = number;
//}

//public void setNumOfMeasures(int number) {
    //numOfMeasures = Math.max(number, minNumOfMeasures);
    //PageView.width = Math.max(minWidth, numOfMeasures * PageView.measureSize + PageView.measureSize);
    //view.reset();
//}

//public TrackController loadTrack(String name, int instrumentNum, int channel, int volume) {
    //TrackController track = new TrackController(this);
    //track.setName(name);
    //track.setInstrument(instrumentNum);
    //track.setChannel(channel);
    //track.setVolume(volume);

    //if (channel == 9) {
        //track.setTrackType(new TrackTypeDrums());
    //} else if (instrumentNum >= 32 && instrumentNum <= 39) {
        //track.setTrackType(new TrackTypeBass());
    //} else {
        //track.setTrackType(new TrackTypeGuitar());
    //}

    //track.setTrackType(trackType);
    //for (Note note : notes) {
        //trackController.loadNote(note);
    //}
    //tracks.add(track);
    //view.addTrackView(track.getView(), tracks.size());
    //return track;
//}

