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

import track.TrackView;
import track.VelocitySlider;
import track.VelocitySliderUI;
import note.Note;
import widgets.*;
import themes.*;
import utils.*;

class PageView extends JFrame {

    protected PagePlayControls playControls;
    protected PageKeyListener keyListener;
    protected PageMenu menuBar;

    private JPanel mainPanel;
    private JScrollPane scrollPane;
    private JScrollBar hScrollBar;
    private JScrollBar vScrollBar;
    private JViewport viewport;
    private JPanel base;
    private JPanel numberBarContainer;
    private PageNumberBar numberBar;
    private VelocitySlider velocitySlider;

    private int numberBarHeight = 30;
    private Dimension numberBarSize;
    private FileChooser fileChooser;
    private int leftMargin = Themes.margin.get("left");
    private boolean changingMeasureSize = false;

    public PageView(Page pageController) {

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

        base = new JPanel();
        base.setBackground(Color.black);
        base.setLayout(new BoxLayout(base, BoxLayout.Y_AXIS));
        add(base);

        JPanel topBar = new JPanel();
        topBar.setBackground(Themes.colors.get("unSelectedTrack"));
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.LINE_AXIS));
        base.add(topBar);

        playControls = new PagePlayControls(pageController);
        playControls.setLayout(new BoxLayout(playControls, BoxLayout.LINE_AXIS));
        topBar.add(playControls);

        topBar.setSize(new Dimension(Page.width, 40));
        topBar.setPreferredSize(new Dimension(Page.width, 40));
        topBar.setMaximumSize(new Dimension(Page.width, 40));

        numberBar = new PageNumberBar(numberBarHeight, leftMargin, pageController);
        numberBar.setLayout(null);
        numberBar.setBackground(Color.yellow);
        //numberBar.setBackground(Themes.colors.get("mainPanel"));
        numberBarContainer = new JPanel(null);

        numberBarSize = new Dimension(Page.width, numberBarHeight);
        numberBarContainer.setSize(numberBarSize);
        numberBarContainer.setMinimumSize(numberBarSize);
        numberBarContainer.setMaximumSize(numberBarSize);
        numberBarContainer.setPreferredSize(numberBarSize);
        base.add(numberBarContainer);
        numberBarContainer.add(numberBar);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBackground(Themes.colors.get("mainPanel"));
        mainPanel.setSize(new Dimension(Page.width, 850));
        mainPanel.setMinimumSize(new Dimension(Page.width, 850));
        mainPanel.setPreferredSize(new Dimension(Page.width, 100));

        scrollPane = new JScrollPane(mainPanel);
        hScrollBar = scrollPane.getHorizontalScrollBar();
        vScrollBar = scrollPane.getVerticalScrollBar();

        hScrollBar.setUnitIncrement(160);
        vScrollBar.setUnitIncrement(160);
        hScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                if (hScrollBar.getValueIsAdjusting()) {
                    /* send input to controller if manually adjusting scroll */
                    pageController.handleHorizontalScrollBar(evt.getValue());
                } else {
                    /* get scoll position from controller on window resize */
                    setHorizontalScroll(pageController.scrollValue);
                }
            }
        });

        vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent evt) {
                pageController.handleVerticalScrollBar(evt.getValue());
            }
        });

        class MyView extends JViewport {
            //@Override
            public void setViewPosition(Point p) {
                //if (!changingMeasureSize) {
                    Component view = getView();
                    //if (p.x != 0) {
                        //pageController.handleScrollBar(p.x);
                    //} else {
                        //if (view.getLocation().getX() != 0) {
                            //view.setLocation(200, -p.y);
                        //}
                    //}
                //}
                //pageController.handleScrollBar(200);
                 //@Override
                //public void setViewPosition(Point p) {
                        //fireStateChanged();
                    //Component view = getView();
                    if (view != null && ! p.equals(getViewPosition())) {
                        //scrollUnderway = true;
                        //view.setLocation(-p.x, -p.y);
                        //fireStateChanged();
                        //pageController.handleScrollBar(p.x);
                    }
                //}
            }

            //@Override
            //public void componentResized(ComponentEvent ev) {
            //}


            //@Override
            //public void scrollRectToVisible​(Rectangle contentRect) {
            //}

            //@Override
            //public void setExtentSize​(Dimension newExtent) {
            //}

            //@Override
            //public void setViewSize​(Dimension newSize) {
            //}

            //@Override
            //public void reshape​(int x, int y, int w, int h) {
                //console.log("reshape");
                    //boolean sizeChanged = (getWidth() != w) || (getHeight() != h);
                    //if (sizeChanged) {
                        //backingStoreImage = null;
                    //}
                    //super.reshape(x, y, w, h);
            //}

        }

        viewport = new MyView();

        //viewport.addChangeListener(new ChangeListener() {
            //public void stateChanged(ChangeEvent evt) {
                //evt.consume();
                //Point p = viewport.getViewPosition();
                //if (!changingMeasureSize) {
                    //Component view = getView();
                    //if (p.x != 0) {
                        //pageController.handleScrollBar(p.x);
                    //} else {
                        //if (viewport.getLocation().getX() != 0) {
                            //viewport.setLocation(200, -p.y);
                        //}
                    //}
                //}
            //}
        //});

        viewport.setView(mainPanel);
        scrollPane.setViewport(viewport);
        base.add(scrollPane);

        velocitySlider = new VelocitySlider();
        velocitySlider.setUI(new VelocitySliderUI(velocitySlider, Color.red));
        velocitySlider.setMaximum(127);
        velocitySlider.setBounds(0, 0, 31, 127);

        //hack to hide velocitySlider because setVisible affects page scroll
        velocitySlider.setLocation(-100, 0);
        velocitySlider.setBorder(BorderFactory.createLineBorder(Color.gray));
        JLayeredPane layers = getLayeredPane();
        layers.add(velocitySlider, 10);

        fileChooser = new FileChooser();

        keyListener = new PageKeyListener(base, pageController);
    }

    /* handle focus so that global keybindings will work */
    public void setFocus() {
        keyListener.setFocus();
    }

    //@Override
    //public Dimension getPreferredSize() {
        //return new Dimension(1200, 900);
    //}

    public void showInfo(Object o) {
        //infoField.setText(o.toString());
    }

    public void setProgress(long tick) {
        numberBar.setProgress(tick);
    }

    public void cancelProgress() {
        numberBar.cancelProgress();
    }

    public void setHorizontalScroll(int value) {
        numberBar.setScrollPosition(value);
        hScrollBar.setValue(value);
    }

    public void setVerticalScroll(int value) {
        mainPanel.setLocation(0, -value);
    }

    public int getCurrentWidth() {
        Dimension d = this.getSize();
        return d.width - leftMargin * 2;
    }

    public void adjustMeasureSize() {
        numberBar.adjustMeasureSize();
        reset();
    }

    public void reset() {

        int height = mainPanel.getSize().height;
        mainPanel.setSize(new Dimension(Page.width, height));
        mainPanel.setMinimumSize(new Dimension(Page.width, height));
        mainPanel.setPreferredSize(new Dimension(Page.width, height));

        numberBarSize.width = Page.width + leftMargin;

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

    public void addTrackView(TrackView trackView, int totalNumOfTracks) {
        trackView.setAlignmentX(0.0f);
        int maxTrackHeight = Themes.getMaxTrackHeight();
        int newHeight = totalNumOfTracks * maxTrackHeight;

        Dimension size = mainPanel.getSize();
        Dimension newSize = new Dimension((int) size.getWidth(), Math.max((int) size.getHeight(), newHeight));

        mainPanel.setSize(newSize);
        mainPanel.setMinimumSize(newSize);
        mainPanel.setPreferredSize(newSize);
        mainPanel.add(trackView);
        revalidate();
        pack();
    }

    public void removeTrackView(TrackView trackView) {
        if (mainPanel.isAncestorOf(trackView)) {
            mainPanel.remove(trackView);
            revalidate();
            repaint();
        }
    }

    public void removeAllTrackViews() {
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof TrackView) {
                mainPanel.remove(components[i]);
            }
        }
        revalidate();
        repaint();
    }

    public String showFileSaver(String filter) {
        return fileChooser.showSaveChooser(filter);
    }

    public VelocitySlider showVelocitySlider(int scrollValue, MouseEvent evt, Note note) {

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
        //velocitySlider.setVisible(true);
        return velocitySlider;
    }

    public void hideVelocitySlider() {
        //velocitySlider.setVisible(false);
        //hack to hide velocitySlider because setVisible affects page scroll
        velocitySlider.setLocation(-100, 0);
    }

    public String showFileChooser(String filter) {
        return fileChooser.showOpenChooser(filter);
    }

    public void disableMenuItem(Constants c) {
        menuBar.disableMenuItem(c);
    }

    @Override
    public String toString() {
        return "PageView";
    }

}