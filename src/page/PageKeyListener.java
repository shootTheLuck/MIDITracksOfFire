package page;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import java.awt.Dimension;

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
        globalInputMap  = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyActionGlobal enterAction = new KeyActionGlobal("enter", KeyEvent.VK_ENTER);

        KeyAction leftAction = new KeyAction("left", KeyEvent.VK_LEFT);
        KeyAction rightAction = new KeyAction("right", KeyEvent.VK_RIGHT);
        KeyAction upAction = new KeyAction("up", KeyEvent.VK_UP);
        KeyAction downAction = new KeyAction("down", KeyEvent.VK_DOWN);
        KeyAction deleteAction = new KeyAction("delete", KeyEvent.VK_DELETE);
        KeyAction deleteAction2 = new KeyAction("delete2", KeyEvent.VK_BACK_SPACE);
        KeyAction enterAction2 = new KeyAction("enter", KeyEvent.VK_ENTER);

        for (int i = 0; i < 10; i++) {
            String text = String.valueOf(i);
            new KeyAction(text, i + 48);
            new KeyAction(text, i + 96);
        }

        //inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left");
        //actionMap.put("left", leftAction);

        //KeyAction rightAction = new KeyAction(KeyStroke.getKeyStroke("RIGHT"));
        //inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        //actionMap.put("right", rightAction);

    }

    public void setFocus() {
        this.panel.requestFocusInWindow();
        //this.panel.grabFocus();
    }

    class KeyActionGlobal extends AbstractAction {
        private int keyCode;

        public KeyActionGlobal(String name, int keyCode) {
            this.keyCode = keyCode;

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

        public KeyAction(String name, int keyCode) {
            this.keyCode = keyCode;

            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), name);
            actionMap.put(name, this);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            controller.handleKeys(keyCode);
        }
    }
}