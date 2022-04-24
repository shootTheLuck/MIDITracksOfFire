package track;

import note.Note;
import utils.console;

public class TrackType {

    public String name = "name";
    public int numOfStrings = 0;

    public int findNotePitch(int stringNum, int fret) {
        return -1;
    }

    public void assignStringAndFret(Note note) {}

    public boolean isDrums() {
        if (this instanceof TrackTypeDrums) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "TrackType " + name;
    }

}


