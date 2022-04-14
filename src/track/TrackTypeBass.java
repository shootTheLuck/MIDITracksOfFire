package track;

import note.Note;
import utils.console;

public class TrackTypeBass extends TrackType {

    public TrackTypeBass() {
        name = "bass";
        //trackHeight = 100;
        numOfStrings = 4;
    }

    @Override
    public void assignStringAndFret(Note note) {
        int stringNum = 0;
        int fret = 0;
        int pitch = note.pitch;
        if (pitch >= 43) {
            stringNum = 0;
            fret = pitch - 43;
        } else if (note.pitch >= 38) {
            stringNum = 1;
            fret = pitch - 38;
        } else if (pitch >= 33) {
            stringNum = 2;
            fret = pitch - 33;
        } else if (pitch >= 28) {
            stringNum = 3;
            fret = pitch - 28;
        }
        note.stringNum = stringNum;
        note.fret = fret;
    }

    @Override
    public int findNotePitch(int stringNum, int fret) {
        if (stringNum == 0) return 55 + fret - 12;
        if (stringNum == 1) return 50 + fret - 12;
        if (stringNum == 2) return 45 + fret - 12;
        if (stringNum == 3) return 40 + fret - 12;
        return 0;
    }

    @Override
    public String toString() {
        return "bass";
    }
}
