package page;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import themes.ThemeReader;
import track.TrackView;
import widgets.FileChooser;
import widgets.InsertBarsDialog;
import widgets.RemoveBarsDialog;
import widgets.VelocitySlider;

import utils.console;

public class PageView {

    public static int measureSize = 150;
    public static int width = 3003;
    protected PagePlayControls playControls;
    protected PageMenu menuBar;
    private PageKeyListener keyListener;

    private Page page;
    private JFrame frame;
    private JPanel mainPanel;
    private JScrollBar hScrollBar;
    private JScrollBar vScrollBar;
    private JPanel topBar;
    private JPanel numberBarContainer;
    private PageNumberBar numberBar;
    private VelocitySlider velocitySlider;
    private JDialog openDialog;

    private int scrollPosition = 0;
    private int scrollIncrement = 160;
    private int numberBarHeight = 30;
    private Dimension numberBarSize;
    private FileChooser fileChooser;
    private int leftMargin = ThemeReader.getMeasure("track.strings.margin.left");

    protected PageView(Page pageController) {

        this.page = pageController;

        frame = new JFrame("untitled");
        String widthString = pageController.getPreference("window.width");
        String heightString = pageController.getPreference("window.height");

        int width;
        int height;
        try {
            width = Integer.parseInt(widthString);
            height = Integer.parseInt(heightString);
        } catch (NumberFormatException e) {
            width = 1000;
            height = 900;
        }

        frame.setSize(new Dimension(width, height));
        frame.setPreferredSize(new Dimension(width, height));
        frame.setLocationRelativeTo(null);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int height = frame.getHeight();
                int width = frame.getWidth();
                pageController.setPreference("window.width", width);
                pageController.setPreference("window.height", height);
                frame.setPreferredSize(new Dimension(width, height));
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pageController.shutDown();
            }
        });

        ImageIcon img = new ImageIcon("./assets/icon.png");
        frame.setIconImage(img.getImage());

        menuBar = new PageMenu(pageController);
        frame.setJMenuBar(menuBar);

        JPanel base = new JPanel();
        base.setBackground(Color.black);
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
        frame.add(base);

        topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.LINE_AXIS));
        base.add(topBar);

        playControls = new PagePlayControls(pageController);
        playControls.setLayout(new BoxLayout(playControls, BoxLayout.LINE_AXIS));
        topBar.add(playControls);

        topBar.setSize(new Dimension(PageView.width, 40));
        topBar.setPreferredSize(new Dimension(PageView.width, 40));
        topBar.setMaximumSize(new Dimension(PageView.width, 40));

        numberBar = new PageNumberBar(page, numberBarHeight, leftMargin);
        numberBar.setLayout(null);
        numberBarContainer = new JPanel(null);

        numberBarSize = new Dimension(PageView.width, numberBarHeight);
        numberBarContainer.setSize(numberBarSize);
        numberBarContainer.setMinimumSize(numberBarSize);
        numberBarContainer.setMaximumSize(numberBarSize);
        numberBarContainer.setPreferredSize(numberBarSize);
        base.add(numberBarContainer);
        numberBarContainer.add(numberBar);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setSize(new Dimension(PageView.width, 850));
        mainPanel.setMinimumSize(new Dimension(PageView.width, 850));
        mainPanel.setPreferredSize(new Dimension(PageView.width, 100));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        hScrollBar = scrollPane.getHorizontalScrollBar();
        vScrollBar = scrollPane.getVerticalScrollBar();

        hScrollBar.setUnitIncrement(scrollIncrement);
        vScrollBar.setUnitIncrement(scrollIncrement);

        Component lScrollButton = hScrollBar.getComponent(1);
        Component rScrollButton = hScrollBar.getComponent(0);

        lScrollButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setHorizontalScroll(Math.max(0, scrollPosition - scrollIncrement));
            }
        });
        rScrollButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setHorizontalScroll(scrollPosition + scrollIncrement);
            }
        });

        hScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                if (hScrollBar.getValueIsAdjusting()) {
                    /* manually adjusting scroll */
                    handleHorizontalScrollBar(evt.getValue());
                } else  {
                    /* keep scoll position on window resize */
                    setHorizontalScroll(scrollPosition);
                }
            }
        });

        vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                setVerticalScroll(evt.getValue());
            }
        });

        class MyView extends JViewport {
            //@Override
            public void setViewPosition(Point p) {

            }
        }

        JViewport viewport = new MyView();
        viewport.setView(mainPanel);
        scrollPane.setViewport(viewport);
        base.add(scrollPane);

        velocitySlider = new VelocitySlider();
        //velocitySlider.setUI(new VelocitySliderUI(velocitySlider, Color.red));
        velocitySlider.setMaximum(127);
        velocitySlider.setBounds(0, 0, 31, 127);

        velocitySlider.setVisible(false);
        velocitySlider.setBorder(BorderFactory.createLineBorder(Color.gray));
        JLayeredPane layers = frame.getLayeredPane();
        layers.add(velocitySlider, 10);

        fileChooser = new FileChooser();

        keyListener = new PageKeyListener(base, pageController);
        base.setFocusTraversalKeysEnabled(false);
        topBar.setFocusTraversalKeysEnabled(false);
    }

    /* handle focus so that global keybindings will work */
    protected void setFocus() {
        keyListener.setFocus();
    }

    protected void setTitle(String title) {
        frame.setTitle(title);
    }

    protected void setVisible(boolean tf) {
        frame.setVisible(tf);
    }

    //@Override
    //protected Dimension getPreferredSize() {
        //return new Dimension(1200, 900);
    //}

    protected void setTheme() {
        topBar.setBackground(ThemeReader.getColor("page.topPanel.background"));
        numberBar.setBackground(ThemeReader.getColor("page.numberBar.background"));
        mainPanel.setBackground(ThemeReader.getColor("page.mainPanel.background"));
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                ((TrackView)components[i]).setTheme();
            }
        }
        frame.repaint();
    }

    protected void showInfo(Object o) {
        playControls.infoField.setText(o.toString());
    }

    protected void setProgress(double progress) {
        numberBar.setProgress(progress);

        int currentMeasure = (int)progress;
        int currentPosition = currentMeasure * PageView.measureSize - scrollPosition;
        int currentWidth = getCurrentWidth();
        if (currentPosition > currentWidth) {
            scrollPosition += currentPosition;
            setHorizontalScroll(scrollPosition);
        }
    }

    protected void cancelProgress() {
        numberBar.cancelProgress();
    }

    private void handleHorizontalScrollBar(int value) {
        scrollPosition = PageView.measureSize * (value/PageView.measureSize);
        numberBar.setScrollPosition(scrollPosition);
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                ((TrackView)components[i]).setScrollPosition(scrollPosition);
            }
        }
    }

    protected void setScrollPositionToMeasure(int number) {
        int scrollValue = PageView.measureSize * number;
        setHorizontalScroll(scrollValue);
    }

    private void setHorizontalScroll(int value) {
        handleHorizontalScrollBar(value);
        hScrollBar.setValue(scrollPosition);
    }

    private void setVerticalScroll(int value) {
        mainPanel.setLocation(0, -value);
    }

    private int getCurrentWidth() {
        Dimension d = frame.getSize();
        return d.width - leftMargin * 2;
    }

    protected void addMeasures(int howMany, int minNumOfMeasures) {
        PageView.width += PageView.measureSize * howMany;
        PageView.width = Math.max(PageView.width, minNumOfMeasures * PageView.measureSize);

        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                ((TrackView)components[i]).adjustMeasureSize(PageView.measureSize);
            }
        }
        //handleScrollChange();
        numberBar.adjustMeasureSize(measureSize);
        frame.revalidate();
        frame.repaint();
        reset();
    }

    protected void adjustMeasureSize(int sliderValue, int numOfMeasures) {
        int minimumMeasureSize = 50;
        double maximumMeasureSize = getCurrentWidth() * 0.8;
        PageView.measureSize = (int)Math.min(Math.max(minimumMeasureSize, sliderValue), maximumMeasureSize);
        PageView.width = numOfMeasures * PageView.measureSize + PageView.measureSize;

        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                ((TrackView)components[i]).adjustMeasureSize(PageView.measureSize);
            }
        }
        //handleScrollChange();
        numberBar.adjustMeasureSize(measureSize);
        reset();
    }

    protected void reset() {

        int height = mainPanel.getSize().height;
        mainPanel.setSize(new Dimension(PageView.width, height));
        mainPanel.setMinimumSize(new Dimension(PageView.width, height));
        mainPanel.setPreferredSize(new Dimension(PageView.width, height));

        numberBarSize.width = PageView.width + leftMargin;

        numberBar.setSize(numberBarSize);
        numberBar.setMinimumSize(numberBarSize);
        numberBar.setPreferredSize(numberBarSize);

        numberBarContainer.setSize(numberBarSize);
        numberBarContainer.setMaximumSize(numberBarSize);
        numberBarContainer.setPreferredSize(numberBarSize);
        frame.revalidate();
        frame.pack();
        frame.repaint();
    }

    protected void addTrackView(TrackView trackView, int totalNumOfTracks) {
        trackView.setPageView(this);
        trackView.setAlignmentX(0.0f);
        mainPanel.add(trackView);

        int newHeight = 0;
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                newHeight += components[i].getSize().height;
            }
        }
        Dimension size = mainPanel.getSize();
        Dimension newSize = new Dimension((int) size.getWidth(), Math.max((int) size.getHeight(), newHeight));

        mainPanel.setSize(newSize);
        mainPanel.setMinimumSize(newSize);
        mainPanel.setPreferredSize(newSize);
        trackView.setScrollPosition(scrollPosition);
        frame.revalidate();
        frame.pack();
    }

    protected void removeTrackView(TrackView trackView) {
        if (mainPanel.isAncestorOf(trackView)) {
            mainPanel.remove(trackView);
            frame.revalidate();
            frame.repaint();
        }
    }

    protected void removeAllTrackViews() {
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                mainPanel.remove(components[i]);
            }
        }
        frame.revalidate();
        frame.repaint();
    }

    protected void setBPMField(int value) {
        playControls.BPMField.setValue(value);
    }

    protected int getBPMField() {
        return playControls.BPMField.getValue();
    }

    protected void setPlayStartField(int value) {
        playControls.playStartField.setValue(value);
    }

    protected int getPlayStartField() {
        return playControls.playStartField.getValue();
    }

    protected void showPlaying() {
        playControls.togglePlayButton(Constants.BUTTON_STOP);
        menuBar.toggleMusicPlay(Constants.BUTTON_STOP);
    }

    protected void showStopped() {
        playControls.togglePlayButton(Constants.BUTTON_PLAY);
        menuBar.toggleMusicPlay(Constants.BUTTON_PLAY);
    }

    protected VelocitySlider showVelocitySlider(MouseEvent evt, int velocity) {

        Component c = (Component)evt.getSource();
        Point point = SwingUtilities.convertPoint(c, evt.getX(), evt.getY(), frame);

        int x = point.x;
        int y = point.y;

        //sub slider width/2
        x -= velocitySlider.getWidth() / 2;
        //sub track topbar height
        y -= ThemeReader.getMeasure("track.topPanel.height");
        //center slider on velocity
        y -= 127 - velocity;

        velocitySlider.setLocation(x, y);
        velocitySlider.setDisplay(velocity);
        velocitySlider.setValue(velocity);
        velocitySlider.setVisible(true);
        return velocitySlider;
    }

    protected void hideVelocitySlider() {
        velocitySlider.setVisible(false);
    }

    protected String showUnsavedDialog() {
        int a = JOptionPane.showConfirmDialog(frame,"Unsaved Changes. Would you like to save?");
        if (a == JOptionPane.YES_OPTION) {
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            return "save";
        } else if (a == JOptionPane.CANCEL_OPTION) {
            //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            return "cancel";
        } else {
            //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            return "exit";
        }
    }

    protected void  showInsertBarsDialog(int x, int y) {
        InsertBarsDialog insertBarsDialog = new InsertBarsDialog((JFrame)frame, x, y);
        insertBarsDialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                //int[] value = insertBarsDialog.getValue();
                //page.handleInsertBarsDialog(value[0], value[1]);
                page.handleInsertBarsDialog(
                    insertBarsDialog.numberToAdd,
                    insertBarsDialog.addBefore,
                    insertBarsDialog.allTracks);
            }
        });
        insertBarsDialog.setVisible(true);
    }

    protected void showRemoveBarsDialog(int x, int y) {
        RemoveBarsDialog removeBarsDialog = new RemoveBarsDialog((JFrame) frame, x, y);
        removeBarsDialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                page.handleRemoveBarsDialog(
                    removeBarsDialog.from,
                    removeBarsDialog.to,
                    removeBarsDialog.allTracks);
            }
        });
        removeBarsDialog.setVisible(true);
    }

    protected String showFileChooser(String filter, String path) {
        return fileChooser.showOpenChooser(filter, path);
    }

    protected String showFileSaver(String filter, String path, String name) {
        return fileChooser.showSaveChooser(filter, path, name);
    }

    protected void disableMenuItem(Constants c) {
        menuBar.disableMenuItem(c);
    }

    protected void close() {
        if (openDialog != null) {
            openDialog.dispose();
        }
    }

    @Override
    public String toString() {
        return "PageView";
    }

}