package track;

import note.Note;
import utils.console;

public class TrackTypeDrums extends TrackType {

    public TrackTypeDrums() {
        name = "drums";
        numOfStrings = 8;
        noteDrawWidth = 30;
    }

    @Override
    public int findNotePitch(int stringNum, int fret) {
        if (stringNum == 1) return 49; // crash
        if (stringNum == 2) return 51; // ride
        if (stringNum == 3) return 46; // open hi hat
        if (stringNum == 4) return 42; // closed hi hat
        if (stringNum == 5) return 38; // snare
        if (stringNum == 6) return 31; // stick
        if (stringNum == 7) return 35; // bass
        return 0;
    }

    @Override
    public void assignStringAndFret(Note note) {
        int stringNum = 0;
        int fret = 0;
        int pitch = note.pitch;
        switch (pitch) {
            case 49: // crash
            case 57: // crash 2
                stringNum = 1;
                break;
            case 51: // ride
            case 53: // ride bell
            case 59: // ride 2
                stringNum = 2;
                break;
            case 46: // open hi hat
                stringNum = 3;
                break;
            case 42: // closed hi hat
                stringNum = 4;
                break;
            case 38: // snare
                stringNum = 5;
                break;
            case 31: // stick
                stringNum = 6;
                break;
            case 35: // bass
            case 36: // kick
                stringNum = 7;
                break;
        }

        note.stringNum = stringNum;
        note.fret = fret;
    }

    @Override
    public String toString() {
        return "drums";
    }
}