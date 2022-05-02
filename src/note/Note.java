package note;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import utils.console;


class SortbyStart implements Comparator<Note> {
    // sort in ascending order
    public int compare(Note a, Note b) {
        return (int) (a.start - b.start);
    }
}

public class Note {

    public static class List extends ArrayList<Note> {
        public boolean sorted = false;

        public List() {
            this.sorted = sorted;
        }
        public List sortByStart() {
            if (!this.sorted) {
                Collections.sort(this, new SortbyStart());
                this.sorted = true;
            }
            return this;
        }

        public List sortByStart(boolean force) {
            if (force) {
                Collections.sort(this, new SortbyStart());
                this.sorted = true;
            } else {
                this.sortByStart();
            }
            return this;
        }
    }

    private static int number = 0;

    public int velocity = 80;
    public long start = 0;
    public long duration = 0;
    public int stringNum = 0;
    public int fret = 0;
    public int pitch = 65;
    public boolean isSelected = false;
    public boolean isNew = true;
    public Rectangle rectangle = new Rectangle();
    public final int serialNumber;

    public Note() {
        serialNumber = number;
        number += 1;
    }

    public Note clone() {
        Note clone = new Note();
        clone.isSelected = false;
        clone.isNew = true;
        clone.rectangle = new Rectangle(this.rectangle);
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