
import javax.swing.SwingUtilities;

import page.Page;
import utils.console;

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.log("starting up...");
                Page page = new Page();
                page.loadFile("midi/RepoMan2Test.mid");
                //page.loadFile("midi/Friday.mid");
                //page.loadFile("midi/DrumHits.mid");
                //page.loadFile("midi/test2.mid");
            }
        });
    }
}

