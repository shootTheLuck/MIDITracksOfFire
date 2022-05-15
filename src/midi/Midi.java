package midi;

import java.io.File;
import java.util.HashMap;
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


public class Midi {

    private Page pageController;
    private Synthesizer synthesizer;
    private Sequencer sequencer;
    private Receiver receiver;
    private Soundbank soundBank;
    private Sequence playSequence;

    public long loopStart;
    public long loopStop;

    static long MAX_LONG = 9007100000000000l;

    static int TEXT = 0x01;
    static int TRACKNAME = 0x03;
    static int END_OF_TRACK = 0x2F;
    static int TEMPO = 0x51;

    static int VOLUME = 7;
    static int BALANCE = 8;
    static int PAN = 10;
    static int REVERB = 91;
    static int CHORUS = 93;

    public Midi(Page page) {

        pageController = page;
        File sf2File = new File(pageController.getPreference("soundFont"));

        try {

            synthesizer = MidiSystem.getSynthesizer();
            receiver = synthesizer.getReceiver();

            Soundbank sbDefault = synthesizer.getDefaultSoundbank();
            synthesizer.unloadAllInstruments(sbDefault);

            try {
                synthesizer.open();
                soundBank = MidiSystem.getSoundbank(sf2File);
                synthesizer.loadAllInstruments(soundBank);

                sequencer = MidiSystem.getSequencer();
                sequencer.open();

                for (Transmitter transmitter: sequencer.getTransmitters()) {
                    transmitter.close();
                }

                sequencer.getTransmitter().setReceiver(receiver);
                sequencer.addMetaEventListener(new MetaEventListener() {
                    public void meta(MetaMessage msg) {
                        if (msg.getType() == Midi.END_OF_TRACK) {
                            /* end of sequence */
                            pageController.handleSoundComplete();
                        }
                    }
                });

                synthesizer.getChannels();

                /* set reverb to 0 on all channels .. not sure about this one */
                //for (MidiChannel channel : mChannels) {
                    //channel.controlChange(91, 0);
                //}

            } catch(Exception e) {
                e.printStackTrace();
            }

        } catch(MidiUnavailableException ex) {
            console.error("midi unavailable:", ex);
        }
    }

    public void setSoundfont(String url) {
        File sf2File = new File(url);
        try {
            synthesizer.unloadAllInstruments(soundBank);
            soundBank = MidiSystem.getSoundbank(sf2File);
            synthesizer.loadAllInstruments(soundBank);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private long getStartTime(List<Note> notes) {
        long startTime = MAX_LONG;
        for (Note note : notes) {
            startTime = Math.min(note.start, startTime);
        }
        return startTime;
    }

    private Track makeMidiTrack(TrackController tController, int BPM, Sequence sequence) {
        Track track = sequence.createTrack();

        int channel = tController.getChannel();
        String trackName = tController.getName();
        int instrumentNum = tController.getInstrument().number;
        boolean isMuted = tController.isMuted();
        int volume = isMuted? 0 : tController.getVolume();
        int reverbLevel = 0;
        int panLevel = 64;

        try {
            // 3 is number of bytes in databyte array
            MetaMessage setTempo = new MetaMessage();
            setTempo.setMessage(Midi.TEMPO ,getTempoData(BPM), 3);
            track.add(new MidiEvent(setTempo, 0));

            MetaMessage setTrackName = new MetaMessage();
            setTrackName.setMessage(Midi.TRACKNAME, trackName.getBytes(), trackName.length());
            track.add(new MidiEvent(setTrackName, 0));

            MetaMessage setText = new MetaMessage();
            String text = "Wowo this is cool";
            setText.setMessage(Midi.TEXT, text.getBytes(), text.length());
            track.add(new MidiEvent(setText, 0));

            ShortMessage setInstrument = new ShortMessage();
            // 0 needed for message that takes up to two data bytes
            setInstrument.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrumentNum, 0);
            track.add(new MidiEvent(setInstrument, 0));

            ShortMessage setVolume = new ShortMessage();
            setVolume.setMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.VOLUME, volume);
            track.add(new MidiEvent(setVolume, 0));

            ShortMessage setReverb = new ShortMessage();
            setReverb.setMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.REVERB, reverbLevel);
            track.add(new MidiEvent(setReverb, 0));

            ShortMessage setPan = new ShortMessage();
            setPan.setMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.BALANCE, panLevel);
            track.add(new MidiEvent(setPan, 0));

            return track;

        } catch (Exception ex) {
            console.error("Midi: an error happened making midi track", ex);
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
                    //new ShortMessage(ShortMessage.NOTE_ON, channel, note.pitch, 0);
                    new ShortMessage(ShortMessage.NOTE_OFF, channel, note.pitch, 0);

            long noteStart = note.start - startTime; // 0 if just one note
            long noteEnd = noteStart + note.duration;

            //track.add(new MidiEvent(pitchBend1, noteStart));
            track.add(new MidiEvent(noteOn, noteStart));
            track.add(new MidiEvent(noteOff, noteEnd));

        } catch (Exception ex) {
            console.error("Midi: an error happened making midi note", ex);
        }
    }

    public void playNote(Note note, TrackController tController, int BPM, int resolution) {
        unMuteAllTracks();
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, resolution);
            Track track = makeMidiTrack(tController, BPM, sequence);
            int channel = tController.getChannel();
            loadMidiNote(note, channel, track);
            long startTime = note.start;
            sequencer.setSequence(sequence);

            /* hack? subtract 1 tick to play selection starting with simultaneus notes */
            sequencer.setTickPosition(startTime - 1);
            sequencer.start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    public void playSelection(TrackController tController, int BPM, int resolution) {
        unMuteAllTracks();
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, resolution);
            Track track = makeMidiTrack(tController, BPM, sequence);
            List<Note>trackNotes = tController.getSelection();
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

    /* push end of track off so loopEndPoint falls within sequence limits */
    private void addDummyEndOfTrack(Track track) {
        try {
            MetaMessage endOfSequence = new MetaMessage();
            endOfSequence.setMessage(Midi.END_OF_TRACK, null, 0);
            track.add(new MidiEvent(endOfSequence, MAX_LONG));
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void play(List<TrackController> trackControllers, int BPM, int resolution, long startTime, boolean looping) {
        unMuteAllTracks();
        try {
            playSequence = new Sequence(Sequence.PPQ, resolution);
            for (TrackController tController : trackControllers) {
                Track track = makeMidiTrack(tController, BPM, playSequence);
                List<Note>trackNotes = tController.getNotes();
                int channel = tController.getChannel();
                for (Note note : trackNotes) {
                    loadMidiNote(note, channel, track);
                }
                if (looping) {
                    addDummyEndOfTrack(track);
                }
            }

            sequencer.setSequence(playSequence);
            sequencer.setTickPosition(startTime);

            if (looping) {
                sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
                sequencer.setLoopStartPoint(loopStart);
                sequencer.setLoopEndPoint(loopStop);
            } else {
                sequencer.setLoopCount(0);
            }
            sequencer.start();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    //public void setLoop(long loopStart, long loopStop) {
        //console.log("start", loopStart, loopStop);
        //sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        //sequencer.setLoopStartPoint(loopStart);
        //sequencer.setLoopEndPoint(loopStop);
    //}

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
                int channel = tController.getChannel();
                for (Note note : trackNotes) {
                    loadMidiNote(note, channel, track);
                }
            }
            sequencer.setSequence(sequence);

            int[] allowedTypes = MidiSystem.getMidiFileTypes(sequence);

            if (allowedTypes.length == 0) {
                console.error("No supported MIDI file types available on this system.");
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
                    ShortMessage setVolume = new ShortMessage();
                    setVolume.setMessage(ShortMessage.CONTROL_CHANGE, channel, Midi.VOLUME, value);
                    track.add(new MidiEvent(setVolume, sequencer.getTickPosition()));
                } catch (Exception ex) {
                    console.error("Midi: an error happened trying to set track volume", ex);
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
        synthesizer.close();
    }

}
