package note;

import java.awt.Rectangle;

class Serial {
    static long number = 0;
}

public class Note extends Rectangle {

    public int velocity = 80;
    public long start = 0;
    public long duration = 0;
    public int stringNum = 0;
    public int fret = 0;
    public int pitch = 65;
    public boolean isSelected = false;
    public boolean fromClipboard = false;
    private final long serialNumber;

    public Note() {
        serialNumber = Serial.number;
        Serial.number += 1;
    }

    public Note(int x, int y) {
        this.x = x;
        this.y = y;
        serialNumber = Serial.number;
        Serial.number += 1;
    }

    public Note clone() {
        Note clone = new Note();
        clone.isSelected = false;
        clone.x = this.x;
        clone.y = this.y;
        clone.width = this.width;
        clone.height = this.height;
        clone.velocity = this.velocity;
        clone.start = this.start;
        clone.duration = this.duration;
        clone.stringNum = this.stringNum;
        clone.fret = this.fret;
        clone.pitch = this.pitch;
        clone.fromClipboard = false;
        return clone;
    }

    //https://www.geeksforgeeks.org/overriding-equals-method-in-java/
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Note)) {
            return false;
        }

        // typecast o to Note so that we can compare data members
        Note note = (Note) o;
        return note.serialNumber == this.serialNumber;
    }

    @Override
    public String toString() {
        return "Note" +
                //"\n  #: " + serialNumber +
                "\n  x: " + x +
                "\n  y: " + y +
                //"\n  width: " + width +
                //"\n  start: " + start +
                //"\n  duration: " + duration +
                //"\n  instrument " + instrumentNum +
                //"\n  pitch " + pitch +
                //"\n  velocity " + velocity +
                "\n";
    }

}