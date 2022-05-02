package page;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import utils.console;

class PageKeyListener {

    JPanel panel;
    Page controller;
    ActionMap actionMap;
    InputMap inputMap;
    InputMap globalInputMap;

    public PageKeyListener(JPanel panel, Page controller) {
        this.controller = controller;
        this.panel = panel;
        actionMap = panel.getActionMap();
        inputMap  = panel.getInputMap(JComponent.WHEN_FOCUSED);
        globalInputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        // panel requests focus when enter key is pressed in (focused) subcomponents
        new KeyActionGlobal("regainFocus", KeyEvent.VK_ENTER);

        new KeyAction("left", KeyEvent.VK_LEFT);
        new KeyAction("right", KeyEvent.VK_RIGHT);
        new KeyAction("up", KeyEvent.VK_UP);
        new KeyAction("down", KeyEvent.VK_DOWN);
        new KeyAction("delete", KeyEvent.VK_DELETE);
        new KeyAction("delete2", KeyEvent.VK_BACK_SPACE);
        new KeyAction("enter", KeyEvent.VK_ENTER);
        new KeyAction("tab", KeyEvent.VK_TAB);
        new KeyAction("undo", KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        new KeyAction("redo", KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);

        // listen for number keys
        for (int i = 0; i < 10; i++) {
            String name = String.valueOf(i);
            new KeyAction(name, i + 48);
            new KeyAction(name, i + 96);
        }

    }

    public void setFocus() {
        this.panel.requestFocusInWindow();
    }

    class KeyActionGlobal extends AbstractAction {

        public KeyActionGlobal(String name, int keyCode) {
            globalInputMap.put(KeyStroke.getKeyStroke(keyCode, 0), name);
            actionMap.put(name, this);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            setFocus();
        }
    }

    class KeyAction extends AbstractAction {
        private int keyCode;

        public KeyAction(String name, int keyCode, int modifier) {
            this.keyCode = keyCode;
            inputMap.put(KeyStroke.getKeyStroke(keyCode, modifier), name);
            actionMap.put(name, this);
        }

        public KeyAction(String name, int keyCode) {
            this(name, keyCode, 0);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            controller.handleKeys(keyCode);
        }
    }
}