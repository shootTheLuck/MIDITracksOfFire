
import javax.swing.SwingUtilities;

import page.Page;
import utils.console;

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.log("starting up...");
                //Page page = new Page(null);
                Page page = new Page("midi/RepoMan2Test.mid");
            }
        });
    }
}

