
import javax.swing.SwingUtilities;

import page.Page;
import utils.console;

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.log("starting up...");
                //Page page = new Page(null);
                //Page page = new Page("midi/RepoMan2Test.mid");
                //Page page = new Page("midi/Ace_Of_Spades2.mid");
                Page page = new Page("midi/I Don't Want to Grow Up.mid");
            }
        });
    }
}

