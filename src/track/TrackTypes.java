package track;

import java.util.ArrayList;
import java.util.List;

public class TrackTypes {

    static List<TrackType> list = new ArrayList<>();
    //public static int getMaxHeight() {
        //return 150;
    //}

    public static TrackType Guitar = new GuitarTrackType();
    public static TrackType Bass = new BassTrackType();
    public static TrackType Drums = new DrumsTrackType();

    static {
        list.add(Guitar);
        list.add(Bass);
        list.add(Drums);
    }

    static TrackType[] getArray() {
        return list.toArray(new TrackType[list.size()]);
    }
}