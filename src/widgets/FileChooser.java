package widgets;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

import utils.console;

public class FileChooser extends JFileChooser {

    private void setup(String filter, String path) {

        //String workingPath = System.getProperty("user.dir");
        if (path != null) {
            setCurrentDirectory(new File(path));
        } else {
            String workingPath = System.getProperty("user.dir");
            setCurrentDirectory(new File(workingPath));
        }

        if (filter != null) {
            setFileFilter(
                    new FileNameExtensionFilter("Only ." + filter + " files", filter));
        }
    }

    private String evaluate(int result) {
        if (result == JFileChooser.APPROVE_OPTION) {
            return getSelectedFile().getAbsolutePath();
        } else {
            return "";
        }
    }

    public String showOpenChooser(String filter) {
        setup(filter, null);
        setDialogTitle("Open File");
        int result = showOpenDialog(null);
        return evaluate(result);
    }

    public String showOpenChooser(String filter, String path) {
        setup(filter, path);
        setDialogTitle("Open File");
        int result = showOpenDialog(null);
        return evaluate(result);
    }

    public String showSaveChooser(String filter) {
        setup(filter, null);
        setDialogTitle("Save File");
        int result = showSaveDialog(null);
        return evaluate(result);
    }

    public String showSaveChooser(String filter, String path) {
        setup(filter, path);
        setDialogTitle("Save File");
        int result = showSaveDialog(null);
        return evaluate(result);
    }

    @Override
    public String toString() {
        return getClass() + ". methods return String filename.";
    }


}