package utils;

import java.awt.event.MouseEvent;

public class MouseMods {

    public static boolean lClick;
    public static boolean rClick;
    public static boolean shift;
    public static boolean alt;
    public static boolean ctrl;

    public static void setFromEvent(MouseEvent evt) {
        int modifier = evt.getModifiersEx();

        shift = (modifier & MouseEvent.SHIFT_DOWN_MASK) > 0 ?
            true: false;
        alt = (modifier & MouseEvent.ALT_DOWN_MASK) > 0 ?
            true: false;
        ctrl = (modifier & MouseEvent.CTRL_DOWN_MASK) > 0 ?
            true: false;
        lClick = (modifier & MouseEvent.BUTTON1_DOWN_MASK) > 0 ?
            true: false;
        rClick = (modifier & MouseEvent.BUTTON3_DOWN_MASK) > 0 ?
            true: false;
    }
}

// use case:

// MouseMods.setFromEvent(mouseEvent);
// if (MouseMods.ctrl || MouseMods.rClick) {
//      do something...
// }