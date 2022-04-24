package midi;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import page.Page;
import track.TrackController;
import note.Note;
import utils.console;

class SortbyStart implements Comparator<Note> {
    /* sort in ascending order */
    public int compare(Note a, Note b) {
        return (int) (a.start - b.start);
    }
}

public class Midi {

    private Page pageController;
    private Synthesizer midiSynth;
    private Sequencer sequencer;
    private Receiver midiReceiver;
    private Soundbank soundBank;
    private Sequence playSequence;

    public Midi(Page page) {

        pageController = page;
        File sf2File = new File(pageController.getPreference("soundFont"));

        try {

            midiSynth = MidiSystem.getSynthesizer();
            midiReceiver = midiSynth.getReceiver();

            Soundbank sbDefault = midiSynth.getDefaultSoundbank();
            midiSynth.unloadAllInstruments(sbDefault);

            try {
                midiSynth.open();
                soundBank = MidiSystem.getSoundbank(sf2File);
                midiSynth.loadAllInstruments(soundBank);

                sequencer = MidiSystem.getSequencer();
                sequencer.open();

                for (Transmitter tm: sequencer.getTransmitters()) {
                    tm.close();
                }

                sequencer.getTransmitter().setReceiver(midiReceiver);
                sequencer.addMetaEventListener(new MetaEventListener() {
                    public void meta(MetaMessage msg) {
                        if (msg.getType() == 47) {
                            /* end of sequence */
                            pageController.handleSoundComplete();
                        }
                    }
                });

                midiSynth.getChannels();

                /* set reverb to 0 on all channels .. not sure about this one */
                //for (MidiChannel channel : mChannels) {
                    //channel.controlChange(91, 0);
                //}

            } catch(Exception e) {
                e.printStackTrace();
            }

        } catch(MidiUnavailableException e) {
            System.out.println("midi unavailable: " + e);
        }
    }

    public void setSoundfont(String url) {
        File sf2File = new File(url);
        try {
            midiSynth.unloadAllInstruments(soundBank);
            soundBank = MidiSystem.getSoundbank(sf2File);
            midiSynth.loadAllInstruments(soundBank);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private long getStartTime(List<Note> notes) {
        long startTime = 99999999999L;
        for (Note note : notes) {
            startTime = Math.min(note.start, startTime);
        }
        return startTime;
    }

    private Track makeMidiTrack(TrackController tController, int BPM, Sequence sequence) {
        Track track = sequence.createTrack();
        int channel = tController.getChannel();
        int instrumentNum = tController.getInstrument().number;
        boolean isMuted = tController.isMuted();
        int volume = isMuted? 0 : tController.getVolume();
        int reverbLevel = 0;
        int panLevel = 64;
        try {
            // 0x51 = tempo message. 3 is number of bytes in databyte array
            MetaMessage tempo = new MetaMessage();
            tempo.setMessage(0x51 ,getTempoData(BPM), 3);
            track.add(new MidiEvent(tempo, 0));

            MetaMessage trackName = new MetaMessage();
            trackName.setMessage(0x03, tController.getName().getBytes(), tController.getName().length());
            track.add(new MidiEvent(trackName, 0));

            // eot already provided by java
            // experiment with adding our own
            //MetaMessage end = new MetaMessage();
            //byte[] bet = {};
            //end.setMessage(47, bet, 0);
            //track.add(new MidiEvent(end, 10000));

            ShortMessage setInstrumentNum = new ShortMessage();
            setInstrumentNum.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrumentNum, 0);
            track.add(new MidiEvent(setInstrumentNum, 0));

            ShortMessage setVolume = new ShortMessage();
            setVolume.setMessage(ShortMessage.CONTROL_CHANGE, channel, 7, volume);
            track.add(new MidiEvent(setVolume, 0));

            ShortMessage setReverb = new ShortMessage();
            setReverb.setMessage(ShortMessage.CONTROL_CHANGE, channel, 91, reverbLevel);
            track.add(new MidiEvent(setReverb, 0));

            ShortMessage setPan = new ShortMessage();
            setPan.setMessage(ShortMessage.CONTROL_CHANGE, channel, 8, panLevel);
            track.add(new MidiEvent(setPan, 0));

            return track;

        } catch (Exception ex) {
            console.log("Midi: an error happened making midi track", ex);
            return null;
        }

    }

    private void loadMidiNote(Note note, int channel, Track track) {
        try {
            int startTime = 0;

            //ShortMessage pitchBend1 =
                    //new ShortMessage(ShortMessage.PITCH_BEND, channel, 0, 120);
            ShortMessage noteOn =
                    new ShortMessage(ShortMessage.NOTE_ON, channel, note.pitch, note.velocity);
            ShortMessage noteOff =
                    new ShortMessage(ShortMessage.NOTE_ON, channel, note.pitch, 0);
                    //new ShortMessage(ShortMessage.NOTE_OFF, channel, note.pitch, 0);

            long noteStart = note.start - startTime; // 0 if just one note
            long noteEnd = noteStart + note.duration;

            //track.add(new MidiEvent(pitchBend1, noteStart));
            track.add(new MidiEvent(noteOn, noteStart));
            track.add(new MidiEvent(noteOff, noteEnd));

        } catch (Exception ex) {
            console.log("Midi: an error happened making midi note", ex);
        }
    }

    public void playSelection(TrackController tController, int BPM, int resolution) {
        unMuteAllTracks();
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, resolution);
            Track track = makeMidiTrack(tController, BPM, sequence);
            List<Note>trackNotes = tController.getSelection();
            //Collections.sort(trackNotes, new SortbyStart());
            int channel = tController.getChannel();
            for (Note note : trackNotes) {
                loadMidiNote(note, channel, track);
            }
            long startTime = getStartTime(trackNotes);
            sequencer.setSequence(sequence);

            /* hack? subtract 1 tick to play selection starting with simultaneus notes */
            sequencer.setTickPosition(startTime - 1);
            sequencer.start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void play(List<TrackController> trackControllers, int BPM, int resolution, long startTime) {
        unMuteAllTracks();
        try {
            playSequence = new Sequence(Sequence.PPQ, resolution);
            for (TrackController tController : trackControllers) {
                Track track = makeMidiTrack(tController, BPM, playSequence);
                List<Note>trackNotes = tController.getNotes();
                //Collections.sort(trackNotes, new SortbyStart());
                int channel = tController.getChannel();
                for (Note note : trackNotes) {
                    loadMidiNote(note, channel, track);
                }
            }

            sequencer.setSequence(playSequence);
            sequencer.setTickPosition(startTime);
            sequencer.start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPlayPosition(long tick) {
        sequencer.setTickPosition(tick);
    }

    public void writeToFile(File file, List<TrackController> trackControllers, int BPM, int resolution) {
        this.stop();
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, resolution);
            for (TrackController tController : trackControllers) {
                Track track = makeMidiTrack(tController, BPM, sequence);
                List<Note>trackNotes = tController.getNotes();
                //Collections.sort(trackNotes, new SortbyStart());
                int channel = tController.getChannel();
                for (Note note : trackNotes) {
                    loadMidiNote(note, channel, track);
                }
            }
            sequencer.setSequence(sequence);

            int[] allowedTypes = MidiSystem.getMidiFileTypes(sequence);

            if (allowedTypes.length == 0) {
                console.log("No supported MIDI file types available on this system.");
            } else {
                MidiSystem.write(sequence, allowedTypes[0], file);
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    private byte[] getTempoData(int BPM) {
        // magic
        // http://www.java2s.com/example/java/javax.sound.midi/create-a-set-tempo-meta-event-for-midi.html
        long microsecondsPerQuarterNote = 60000000 / BPM;
        byte[] array = new byte[] {0, 0, 0};
        for (int i = 0; i < 3; i++) {
            int shift = (3 - 1 - i) * 8;
            array[i] = (byte) (microsecondsPerQuarterNote >> shift);
        }
        return array;
    }

    public void changeTrackInstrument() {
        //TODO
    }

    public void muteTrack(int i, boolean muted) {
        sequencer.setTrackMute(i, muted);
    }

    public void unMuteAllTracks() {
        Sequence sequence = sequencer.getSequence();
        if (sequence != null) {
            Track[] tracks = sequence.getTracks();
            for (int i = 0; i < tracks.length; i++) {
                sequencer.setTrackMute(i, false);
            }
        }
    }

    public void setVolume() {
        // TODO overall volume
    }

    public void setTrackVolume(int channel, int value) {
        Sequence sequence = sequencer.getSequence();
        if (sequence != null) {
            Track[] tracks = sequence.getTracks();
            for (int i = 0; i < tracks.length; i++) {
                Track track = tracks[i];
                try {
                    ShortMessage msg = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 7, value);
                    track.add(new MidiEvent(msg, sequencer.getTickPosition()));
                } catch (Exception ex) {
                    console.log("Midi: an error happened trying to set track volume", ex);
                }
            }
        }
    }

    public long getTickPosition() {
        return sequencer.getTickPosition();
    }

    public void stop() {
        sequencer.stop();
    }

    public void close() {
        sequencer.close();
        midiSynth.close();
    }

}
