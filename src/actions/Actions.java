package actions;

import java.util.ArrayList;

import utils.console;

public class Actions {

    static private int index = 0;
    static private int savedAt = 0;
    public static ArrayList<Item>list = new ArrayList<Item>();

    public static class Item {
        public int series = -1;
        public void execute() {}
        public void redo() {}
        public void undo() {}
    }

    public static void markSave() {
        savedAt = index;
    }

    public static boolean hasUnsavedChanges() {
        return (savedAt != index || index != 0);
    }

    public static void add(Item c) {
        c.execute();
        list.add(index, c);
        index += 1;
        while (list.size() > index) {
            list.remove(list.size() -1);
        }
        //console.log("adding. index is", index, "list size:", list.size());
    }

    public static  void redo() {
        if (index < list.size()) {
            Item action = list.get(index);
            action.redo();
            index += 1;
            int series = action.series;
            if (series > -1) {
                for (int i = index; i < list.size(); i++) {
                    action = list.get(i);
                    if (action.series == series && index < list.size()) {
                        action.redo();
                        index += 1;
                    } else {
                        break;
                    }
                }
            }
        }
        //console.log("redo . index is", index, "list size:", list.size());
    }

    public static  void undo() {
        if (index > 0) {
            Item action = list.get(index -1);
            int series = action.series;
            if (series > -1) {
                /* loop backward through list */
                for (int i = index - 1; i >= 0; i--) {
                    action = list.get(i);
                    if (action.series == series && index > 0) {
                        action.undo();
                        index = Math.max(0, index - 1);
                    }
                }
            } else {
                action.undo();
                index = Math.max(0, index - 1);
            }
        }
        //console.log("undo . index is", index, "list size:", list.size());
    }

}