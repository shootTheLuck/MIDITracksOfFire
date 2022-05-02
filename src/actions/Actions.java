package actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.Timer;

import utils.console;

public class Actions {

    static private int index = 0;
    static private ArrayList<Item>list = new ArrayList<Item>();

    /* small amount of debouncing */
    static private int debounceAmount = 40;
    static private int timerCount = debounceAmount;
    static private Timer progressTimer = new Timer(1, (ActionEvent evt) -> {
        timerCount -= 1;
        if (timerCount == 0) {
            Actions.progressTimer.stop();
            timerCount = debounceAmount;
        }
    });

    public static abstract class Item {
        protected String name;
        protected int series = -1;
        protected void execute() {};
        protected void redo() {};
        protected void undo() {};
    }

    public static void add(Item actionItem) {
        actionItem.execute();
        list.add(index, actionItem);
        index += 1;
        while (list.size() > index) {
            list.remove(list.size() - 1);
        }
        //console.log("adding. index is", index, "list size:", list.size());
        //console.log("adding", actionItem.name);
    }

    public static  void redo() {
        if (!progressTimer.isRunning()) {
            progressTimer.start();
        }
        if (index < list.size() && timerCount == debounceAmount) {
            Item actionItem = list.get(index);
            actionItem.redo();
            index += 1;
            int series = actionItem.series;
            if (series > -1) {
                for (int i = index; i < list.size(); i++) {
                    actionItem = list.get(i);
                    if (actionItem.series == series && index < list.size()) {
                        actionItem.redo();
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
        if (!progressTimer.isRunning()) {
            progressTimer.start();
        }
        if (index > 0 && timerCount == debounceAmount) {
            Item actionItem = list.get(index -1);
            int series = actionItem.series;
            if (series > -1) {
                /* loop backward through list */
                for (int i = index - 1; i >= 0; i--) {
                    actionItem = list.get(i);
                    if (actionItem.series == series && index > 0) {
                        actionItem.undo();
                        index = Math.max(0, index - 1);
                    }
                }
            } else {
                actionItem.undo();
                index = Math.max(0, index - 1);
            }
            //console.log("undoing", actionItem.name);
        }
        //console.log("undo . index is", index, "list size:", list.size());
    }

}