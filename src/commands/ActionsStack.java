package commands;

//import java.util.LinkedList;
import java.util.ArrayList;

import utils.console;

public class ActionsStack {

    static private int index = 0;
    static ArrayList<Command>list = new ArrayList<Command>();

    public static class Command {
        public int series = -1;
        public void execute() {}
        public void redo() {}
        public void undo() {}
    }

    public static void add(Command c) {
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
            Command action = list.get(index);
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
    }

    public static  void undo() {
        if (index > 0) {
            Command action = list.get(index -1);
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