package note;

import java.awt.Rectangle;
import utils.console;

public class Note {

    private static int number = 0;

    public int velocity = 80;
    public long start = 0;
    public long duration = 0;
    public int stringNum = 0;
    public int fret = 0;
    public int pitch = 65;
    public boolean isSelected = false;
    public Rectangle rectangle = new Rectangle();
    public final int serialNumber;

    public Note() {
        serialNumber = number;
        number += 1;
    }

    public Note clone() {
        Note clone = new Note();
        clone.isSelected = false;
        clone.rectangle = new Rectangle();
        clone.rectangle.x = this.rectangle.x;
        clone.rectangle.y = this.rectangle.y;
        clone.rectangle.width = this.rectangle.width;
        clone.rectangle.height = this.rectangle.height;
        clone.velocity = this.velocity;
        clone.start = this.start;
        clone.duration = this.duration;
        clone.stringNum = this.stringNum;
        clone.fret = this.fret;
        clone.pitch = this.pitch;
        return clone;
    }

    //https://www.geeksforgeeks.org/overriding-equals-method-in-java/
    // not using this? should also override hashCode method?
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
            "\n  #: " + serialNumber +
            "\n  start: " + start +
            "\n  duration: " + duration +
            "\n  end: " + (start + duration) +
            "\n  pitch " + pitch +
            "\n  velocity " + velocity +
            "\n  rectangle x: " +
                rectangle.x + ", y: " +
                rectangle.y + ", width: " +
                rectangle.width + ", height: " + rectangle.height +
            "\n";
    }

}