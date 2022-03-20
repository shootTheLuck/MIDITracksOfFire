package track;

import java.util.ArrayList;
import java.util.List;

import note.Note;
import themes.Themes;


public class TrackType {
    public String name = "name";
    public int numOfStrings = 0;
    public int noteDrawWidth = -1;

    public int findNotePitch(int stringNum, int fret) {
        if (stringNum == 0) return 64 + fret;
        if (stringNum == 1) return 59 + fret;
        if (stringNum == 2) return 55 + fret;
        if (stringNum == 3) return 50 + fret;
        if (stringNum == 4) return 45 + fret;
        if (stringNum == 5) return 40 + fret;
        return 0;
    }

    //public int findNoteY(int stringNum) {
        //return Themes.margin.get("top") + Themes.getLineSpacing() * stringNum - noteDrawHeight/2;
    //}

    //public int assignNoteHeight(Note note) {

    //}

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
        return "generic";
    }
}

class GuitarTrackType extends TrackType {
    public GuitarTrackType() {
        name = "guitar";
        numOfStrings = 6;
    }
    @Override
    public String toString() {
        return "guitar";
    }
}

class DrumsTrackType extends TrackType {
    public DrumsTrackType() {
        name = "drums";
        numOfStrings = 8;
        noteDrawWidth = 30;
    }
    @Override
    public String toString() {
        return "drums";
    }
}

class BassTrackType extends TrackType {
    public BassTrackType() {
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


