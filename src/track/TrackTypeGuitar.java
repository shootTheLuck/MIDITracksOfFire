package track;

import note.Note;
import utils.console;

public class TrackTypeGuitar extends TrackType {

    public TrackTypeGuitar() {
        name = "guitar";
        numOfStrings = 6;
    }

    public int findNotePitch(int stringNum, int fret) {
        if (stringNum == 0) return 64 + fret;
        if (stringNum == 1) return 59 + fret;
        if (stringNum == 2) return 55 + fret;
        if (stringNum == 3) return 50 + fret;
        if (stringNum == 4) return 45 + fret;
        if (stringNum == 5) return 40 + fret;
        return 0;
    }

    public void assignStringAndFret(Note note) {
        int stringNum = 0;
        int fret = 0;
        int pitch = note.pitch;

        if (pitch >= 64) {
            stringNum = 0;
            fret = pitch - 64;

        } else if (pitch >= 59) {
            stringNum = 1;
            fret = pitch - 59;

        } else if (pitch >= 55) {
            stringNum = 2;
            fret = pitch - 55;

        } else if (pitch >= 50) {
            stringNum = 3;
            fret = pitch - 50;

        } else if (pitch >= 45) {
            stringNum = 4;
            fret = pitch - 45;

        } else if (pitch >= 40) {
            stringNum = 5;
            fret = pitch - 40;

        } else {
            stringNum = 6;
            fret = pitch - 40;
        }

        note.stringNum = stringNum;
        note.fret = fret;
    }

    @Override
    public String toString() {
        return "guitar";
    }

}
