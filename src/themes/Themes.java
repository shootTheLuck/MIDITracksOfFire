package themes;

import java.util.HashMap;
import java.awt.Color;

public class Themes {

    public static HashMap<String, Color> colors = new HashMap<String, Color>();

    static {
        colors.put("mainPanel", Color.decode("0x0000CC"));
        colors.put("selectedTrack", Color.decode("0x666666"));
        colors.put("unSelectedTrack", Color.decode("0xC0C0C0"));
        colors.put("note", Color.decode("0xEEEEEE"));
        colors.put("selectedNote", Color.decode("0xEECC00"));
        //colors.put("barLines", Color.decode("0x888888"));
        colors.put("barLines", Color.decode("0x000000"));
        colors.put("gridLines", Color.decode("0xbababa"));
        colors.put("strings", Color.decode("0x888888"));
        colors.put("playingMeasure", Color.decode("0xC32020"));
    }

    // top, left, bottom, right
    public static Margin margin = new Margin(20, 60, 30, 20);
    static int lineSpacing = 20;


    public static int getTrackTopBarHeight() {
        return 30;
    }

    public static int getLineSpacing() {
        return lineSpacing;
    }

    public static int getNoteHeight() {
        return 6;
    }

    public static int getDrumNoteHeight() {
        return 6;
    }

    public static int getDrumNoteWidth() {
        return 6;
    }

    public static int getTrackHeight(int numOfStrings) {
        return margin.top + margin.bottom + numOfStrings * lineSpacing;
    }

    public static int getMaxTrackHeight() {
        return getTrackTopBarHeight() + getTrackHeight(6);
    }
}

