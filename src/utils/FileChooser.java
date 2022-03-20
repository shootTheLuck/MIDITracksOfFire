package utils;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

public class FileChooser extends JFileChooser {

    private void setup(String filter) {
        File workingDirectory = new File(System.getProperty("user.dir"));
        setCurrentDirectory(workingDirectory);
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
        setup(filter);
        setDialogTitle("Open File");
        int result = showOpenDialog(null);
        return evaluate(result);
    }

    public String showSaveChooser(String filter) {
        setup(filter);
        setDialogTitle("Save File");
        int result = showSaveDialog(null);
        return evaluate(result);
    }

    @Override
    public String toString() {
        return getClass() + ". methods return String filename.";
    }


}