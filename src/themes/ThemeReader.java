
package themes;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import utils.console;

//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.XMLConstants;


public class ThemeReader {

    private static Properties defaults = new Properties();
    public static Properties settings = new Properties();
    public static File themeFile;

    static {
        try (FileInputStream fis = new FileInputStream("src/themes/default.theme")) {
                defaults.load(fis);
        } catch (FileNotFoundException ex) {
            console.error("an error occured trying to load default theme file:", ex);
        } catch (IOException ex) {
            //
        }
    }

    public static void loadTheme(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            /// save as file as well
            themeFile = new File(fileName);
            settings.load(fis);
        } catch (FileNotFoundException ex) {
            console.error("an error occured trying to load theme file", fileName, ":", ex);
        } catch (IOException ex) {
            //
        }
    }

    public static String getThemeFilename() {
        if (themeFile != null) {
            return themeFile.getName();
        }
        return null;
    }

    public static String getDefaultSetting(String name) {
        return defaults.getProperty(name);
    }

    public static Color getDefaultColor(String name) {
        return Color.decode(defaults.getProperty(name));
    }

    public static int getDefaultMeasure(String name) {
        return Integer.valueOf(defaults.getProperty(name));
    }

    public static String getSetting(String name) {
        String s = settings.getProperty(name);
        if (s == null) {
            return getDefaultSetting(name);
        }
        return s;
    }

    public static Color getColor(String name) {
        return Color.decode(getSetting(name));
    }

    public static int getMeasure(String name) {
        return Integer.parseInt(getSetting(name));
    }


}