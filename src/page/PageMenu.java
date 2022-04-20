package page;

import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import utils.console;

class PageMenu extends JMenuBar {

    private Page pageController;
    private List<MenuItem> menuItems;

    public PageMenu(Page pageController) {
        this.pageController = pageController;
        menuItems = new ArrayList<>();
        add(new FileMenu());
        add(new EditMenu());
        add(new TrackMenu());
        add(new ViewMenu());
        add(new MusicMenu());
    }

    public void toggleMusicPlay(Constants c) {
        if (c == Constants.BUTTON_STOP) {
            disableMenuItem(Constants.MENU_MUSIC_PLAY);
            disableMenuItem(Constants.MENU_MUSIC_PLAYSELECTION);
            enableMenuItem(Constants.MENU_MUSIC_STOP);
        } else {
            enableMenuItem(Constants.MENU_MUSIC_PLAY);
            enableMenuItem(Constants.MENU_MUSIC_PLAYSELECTION);
            disableMenuItem(Constants.MENU_MUSIC_STOP);
        }
    }

    public void disableMenuItem(Constants c) {
        for (MenuItem m : menuItems) {
            if (m.actionConstant == c) {
                m.setEnabled(false);
                return;
            }
        }
    }

    public void enableMenuItem(Constants c) {
        for (MenuItem m : menuItems) {
            if (m.actionConstant == c) {
                m.setEnabled(true);
                return;
            }
        }
    }

    private void setAccessibleText(JComponent c, String desc) {
        c.getAccessibleContext().setAccessibleDescription(desc);
    }

    class MenuItem extends JMenuItem {

        Constants actionConstant;

        public MenuItem(String name) {
            setText(name);
            menuItems.add(this);
        }

        private void setAccessible(String desc) {
            setAccessibleText(this, desc);
        }

        private void setCommandKey(String key) {
            setAccelerator(KeyStroke.getKeyStroke(
                    key.charAt(0), ActionEvent.CTRL_MASK));
        }

        private void setActionConstant(Constants c) {
            this.actionConstant = c;
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    pageController.handleMenuItem(c);
                }
            });
        }
    }

    class FileMenu extends JMenu {

        public FileMenu() {
            setText("File");
            setAccessibleText(this, "File Menu");

            MenuItem fileNew = new MenuItem("New");
            fileNew.setAccessible("Create New File");
            fileNew.setCommandKey("N");
            fileNew.setActionConstant(Constants.MENU_FILE_NEW);
            add(fileNew);

            MenuItem fileOpen = new MenuItem("Open...");
            fileOpen.setAccessible("Open File");
            fileOpen.setCommandKey("O");
            fileOpen.setActionConstant(Constants.MENU_FILE_OPEN);
            add(fileOpen);

            MenuItem fileSave = new MenuItem("Save");
            fileSave.setAccessible("Save File");
            fileSave.setCommandKey("S");
            fileSave.setActionConstant(Constants.MENU_FILE_SAVE);
            add(fileSave);

            MenuItem fileSaveAs = new MenuItem("Save As...");
            fileSaveAs.setAccessible("Save File With New Filename");
            fileSaveAs.setActionConstant(Constants.MENU_FILE_SAVEAS);
            add(fileSaveAs);

            MenuItem fileClose = new MenuItem("Close");
            fileClose.setAccessible("Close File");
            fileClose.setCommandKey("W");
            fileClose.setActionConstant(Constants.MENU_FILE_CLOSE);
            add(fileClose);

            MenuItem fileQuit = new MenuItem("Quit");
            fileQuit.setAccessible("Quit Program");
            fileQuit.setCommandKey("Q");
            fileQuit.setActionConstant(Constants.MENU_FILE_QUIT);
            add(fileQuit);
        }
    }

    class EditMenu extends JMenu {

        public EditMenu() {
            setText("Edit");
            setAccessibleText(this, "Edit Menu");

            MenuItem editUndo = new MenuItem("Undo");
            editUndo.setAccessible("Undo Previous Action");
            editUndo.setCommandKey("Z");
            editUndo.setActionConstant(Constants.MENU_EDIT_UNDO);
            add(editUndo);

            MenuItem editRedo = new MenuItem("Redo");
            editRedo.setAccessible("Redo Previous Action");
            editRedo.setCommandKey("Y");
            editRedo.setActionConstant(Constants.MENU_EDIT_REDO);
            add(editRedo);

            addSeparator();

            MenuItem editCut = new MenuItem("Cut");
            editCut.setAccessible("Cut Selected Notes");
            editCut.setCommandKey("X");
            editCut.setActionConstant(Constants.MENU_EDIT_CUT);
            add(editCut);

            MenuItem editCopy = new MenuItem("Copy");
            editCopy.setAccessible("Copy Selected Notes");
            editCopy.setCommandKey("C");
            editCopy.setActionConstant(Constants.MENU_EDIT_COPY);
            add(editCopy);

            MenuItem editPaste = new MenuItem("Paste");
            editPaste.setAccessible("Paste Selected Notes");
            editPaste.setCommandKey("V");
            editPaste.setActionConstant(Constants.MENU_EDIT_PASTE);
            add(editPaste);

            //MenuItem editClear = new MenuItem("Clear");
            //editClear.setAccessible("Clear Selected Notes");
            //editClear.setActionConstant(Constants.MENU_EDIT_CLEAR);
            //add(editClear);

            MenuItem editSelectAll = new MenuItem("Select All");
            editSelectAll.setAccessible("Select All Notes In Track");
            editSelectAll.setCommandKey("A");
            editSelectAll.setActionConstant(Constants.MENU_EDIT_SELECTALL);
            add(editSelectAll);

            addSeparator();

            MenuItem editInsertBars = new MenuItem("Insert Bars...");
            editInsertBars.setAccessible("Add Chosen Number of Measures to All Tracks");
            //editAddBars.setCommandKey("A");
            editInsertBars.setActionConstant(Constants.MENU_EDIT_INSERTBARS);
            add(editInsertBars);

            MenuItem editRemoveBars = new MenuItem("Remove Bars...");
            editRemoveBars.setAccessible("Remove Chosen Number of Measures from All Tracks");
            //editAddBars.setCommandKey("A");
            editRemoveBars.setActionConstant(Constants.MENU_EDIT_REMOVEBARS);
            add(editRemoveBars);
        }
    }

    class TrackMenu extends JMenu {

        public TrackMenu() {
            setText("Track");
            setAccessibleText(this, "Track Menu");

            MenuItem addTrack = new MenuItem("Add Track");
            addTrack.setAccessible("Add a New Track");
            addTrack.setActionConstant(Constants.MENU_TRACK_ADD);
            add(addTrack);

            MenuItem removeTrack = new MenuItem("Remove Track");
            removeTrack.setAccessible("Remove Selected Track");
            removeTrack.setActionConstant(Constants.MENU_TRACK_REMOVE);
            add(removeTrack);
        }
    }

    class ViewMenu extends JMenu {

        public JMenu subMenu;

        public ViewMenu() {
            setText("View");
            setAccessibleText(this, "View Menu");

            MenuItem setTheme = new MenuItem("Set Theme");
            setTheme.setAccessible("Set Application Theme");
            setTheme.setActionConstant(Constants.MENU_VIEW_SETTHEME);

            //subMenu = new JMenu("sub");
            //setTheme.add(subMenu);
            //MenuItem menuItem = new MenuItem("An item in the submenu");
            //setTheme.add(menuItem);
            add(setTheme);

        }
    }

    class MusicMenu extends JMenu {

        public MusicMenu() {
            setText("Music");
            setAccessibleText(this, "Music Menu");
            KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            KeyStroke space = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);

            MenuItem musicPlay = new MenuItem("Play");
            musicPlay.setAccessible("Play");
            musicPlay.setActionConstant(Constants.MENU_MUSIC_PLAY);
            musicPlay.setAccelerator(enter);
            add(musicPlay);

            MenuItem musicPlaySelection = new MenuItem("Play Selection");
            musicPlaySelection.setAccessible("Play Selection");
            musicPlaySelection.setActionConstant(Constants.MENU_MUSIC_PLAYSELECTION);
            musicPlaySelection.setAccelerator(space);
            add(musicPlaySelection);

            MenuItem musicStop = new MenuItem("Stop Playing");
            musicStop.setAccessible("Stop Playing");
            musicStop.setActionConstant(Constants.MENU_MUSIC_STOP);
            musicStop.setAccelerator(enter);
            musicStop.setEnabled(false);
            add(musicStop);

            addSeparator();

            MenuItem musicFind = new MenuItem("Find Play Position");
            musicFind.setAccessible("Find Play Position");
            musicFind.setActionConstant(Constants.MENU_MUSIC_FIND);
            //musicStop.setAccelerator(enter);
            add(musicFind);

            MenuItem musicSoundfont = new MenuItem("Set Soundfont");
            musicSoundfont.setAccessible("Set Soundfont");
            musicSoundfont.setActionConstant(Constants.MENU_MUSIC_SOUNDFONT);
            add(musicSoundfont);
        }
    }

}
