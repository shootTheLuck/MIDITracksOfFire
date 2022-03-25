
import javax.swing.SwingUtilities;

import page.Page;
import utils.*;

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                console.log("starting up...");
                Page page = Page.getInstance();
                page.loadFile("midi/RepoMan3Test.mid");
                //page.loadFile("midi/2Instruments.mid");
            }
        });
    }
}

