package page;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JOptionPane;

import track.TrackView;
import track.VelocitySlider;
import track.VelocitySliderUI;
import note.Note;
import widgets.*;
import themes.ThemeReader;
import utils.*;

public class PageView extends JFrame {

    public static int measureSize = 150;
    public static int width = 3003;
    protected PagePlayControls playControls;
    protected PageMenu menuBar;
    private PageKeyListener keyListener;

    private JPanel mainPanel;
    private JScrollBar hScrollBar;
    private JScrollBar vScrollBar;
    private JPanel topBar;
    private JPanel numberBarContainer;
    private PageNumberBar numberBar;
    private VelocitySlider velocitySlider;

    private int scrollPosition = 0;
    private int numberBarHeight = 30;
    private Dimension numberBarSize;
    private FileChooser fileChooser;
    private int leftMargin = ThemeReader.getMeasure("track.strings.margin.left");
    private boolean changingMeasureSize = false;


    protected PageView(Page pageController) {

        setTitle("untitled");

        String widthString = pageController.getPreference("window.width");
        String heightString = pageController.getPreference("window.height");

        int width;
        int height;
        try {
            width = Integer.parseInt(widthString);
            height = Integer.parseInt(heightString);
        }
        catch (NumberFormatException e) {
            width = 1000;
            height = 900;
        }

        setSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int height = getHeight();
                int width = getWidth();
                pageController.setPreference("window.width", width);
                pageController.setPreference("window.height", height);
                setPreferredSize(new Dimension(width, height));
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                pageController.shutDown();
            }
        });

        ImageIcon img = new ImageIcon("./assets/icon.png");
        setIconImage(img.getImage());

        menuBar = new PageMenu(pageController);
        setJMenuBar(menuBar);

        JPanel base = new JPanel();
        base.setBackground(Color.black);
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
        add(base);

        topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.LINE_AXIS));
        base.add(topBar);

        playControls = new PagePlayControls(pageController);
        playControls.setLayout(new BoxLayout(playControls, BoxLayout.LINE_AXIS));
        topBar.add(playControls);

        topBar.setSize(new Dimension(PageView.width, 40));
        topBar.setPreferredSize(new Dimension(PageView.width, 40));
        topBar.setMaximumSize(new Dimension(PageView.width, 40));

        numberBar = new PageNumberBar(numberBarHeight, leftMargin);
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

        hScrollBar.setUnitIncrement(160);
        vScrollBar.setUnitIncrement(160);
        hScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                if (hScrollBar.getValueIsAdjusting()) {
                    /* manually adjusting scroll */
                    handleHorizontalScrollBar(evt.getValue());
                } else {
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
        velocitySlider.setUI(new VelocitySliderUI(velocitySlider, Color.red));
        velocitySlider.setMaximum(127);
        velocitySlider.setBounds(0, 0, 31, 127);

        velocitySlider.setVisible(false);
        //hack to hide velocitySlider because setVisible affects page scroll
        //velocitySlider.setLocation(-100, 0);
        velocitySlider.setBorder(BorderFactory.createLineBorder(Color.gray));
        JLayeredPane layers = getLayeredPane();
        layers.add(velocitySlider, 10);

        fileChooser = new FileChooser();

        keyListener = new PageKeyListener(base, pageController);
    }

    /* handle focus so that global keybindings will work */
    protected void setFocus() {
        keyListener.setFocus();
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
        repaint();
    }

    protected void showInfo(Object o) {
        playControls.infoField.setText(o.toString());
    }

    protected void setProgress(long tick, int ticksPerMeasure) {
        numberBar.setProgress(tick, ticksPerMeasure);
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
        int scrollValue = PageView.measureSize * (number - 1);
        setHorizontalScroll(scrollValue);
    }

    protected void setHorizontalScroll(int value) {
        handleHorizontalScrollBar(value);
        hScrollBar.setValue(scrollPosition);
    }

    protected int getScrollPostion() {
        return scrollPosition;
    }

    protected void setVerticalScroll(int value) {
        mainPanel.setLocation(0, -value);
    }

    protected int getCurrentWidth() {
        Dimension d = this.getSize();
        return d.width - leftMargin * 2;
    }

    protected void addMeasures(int howMany) {
        PageView.width += PageView.measureSize * howMany;

        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                ((TrackView)components[i]).adjustMeasureSize(PageView.measureSize);
            }
        }
        //handleScrollChange();
        numberBar.adjustMeasureSize(measureSize);
        revalidate();
        repaint();
        reset();
    }

    protected void adjustMeasureSize(int sliderValue) {
        double minimumMeasureSize = 50.0;
        double maximumMeasureSize = getCurrentWidth() * 0.8;
        PageView.measureSize = (int) Math.min(Math.max(minimumMeasureSize, sliderValue), maximumMeasureSize);
        PageView.width = Page.numOfMeasures * PageView.measureSize + PageView.measureSize;

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
        numberBar.repaint();

        numberBarContainer.setSize(numberBarSize);
        numberBarContainer.setMaximumSize(numberBarSize);
        numberBarContainer.setPreferredSize(numberBarSize);
        revalidate();
        pack();
        repaint();
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
        revalidate();
        pack();
    }

    protected void removeTrackView(TrackView trackView) {
        if (mainPanel.isAncestorOf(trackView)) {
            mainPanel.remove(trackView);
            revalidate();
            repaint();
        }
    }

    protected void removeAllTrackViews() {
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                mainPanel.remove(components[i]);
            }
        }
        revalidate();
        repaint();
    }

    protected String showFileSaver(String filter) {
        return fileChooser.showSaveChooser(filter);
    }

    protected VelocitySlider showVelocitySlider(MouseEvent evt, Note note) {

        Component c = (Component) evt.getSource();
        Point point = SwingUtilities.convertPoint(c, evt.getX(), evt.getY(), this);

        int x = point.x;
        int y = point.y;
        //sub slider width/2
        x -= 15;
        //sub track topbar height
        y -= 26;
        //center slider on note value
        y -= 127 - note.velocity;

        velocitySlider.setLocation(x, y);
        velocitySlider.setDisplay(note.velocity);
        velocitySlider.setValue(note.velocity);
        velocitySlider.setVisible(true);
        return velocitySlider;
    }

    protected void hideVelocitySlider() {
        velocitySlider.setVisible(false);
        //hack to hide velocitySlider because setVisible affects page scroll
        //velocitySlider.setLocation(-100, 0);
    }

    protected int[] showAddBarsDialog() {

        AddBarsDialog addBarsDialog = new AddBarsDialog((JFrame) this);

        int[] result = addBarsDialog.getValue();
        if (result[0] > 0) {
            return result;
        } else {
            int[] nothingToAdd = {0, 0};
            return nothingToAdd;
        }
    }

    protected String showFileChooser(String filter) {
        return fileChooser.showOpenChooser(filter);
    }

    protected void disableMenuItem(Constants c) {
        menuBar.disableMenuItem(c);
    }

    @Override
    public String toString() {
        return "PageView";
    }

}