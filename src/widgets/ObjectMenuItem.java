package widgets;

import javax.swing.JMenuItem;

public class ObjectMenuItem extends JMenuItem {

    public Object object;

    public ObjectMenuItem(Object o) {
        super(o.toString());
        object = o;
    }
}
