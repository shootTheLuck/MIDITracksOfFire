package track;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import instruments.Instrument;
import note.Note;
import page.PageView;
import page.Constants;
import themes.ThemeReader;
import widgets.InputField;
import widgets.NumberInputField;
import widgets.ObjectMenuItem;


public class TrackView extends JPanel {

    private JPanel topBar;
    private JPanel drawContainer;
    private JPanel side;
    private TrackDrawArea drawArea;
    private TrackDrawArea drawAreaGuitar;
    private TrackDrawArea drawAreaBass;
    private TrackDrawArea drawAreaDrums;
    private TrackType trackType = TrackTypes.Guitar;
    private TrackController controller;
    private PageView pageView;

    // topBar items:
    private JLabel collapseButton;
    private Constants collapseAction;
    private JLabel muteButton;
    private Constants muteAction;
    private InputField trackNameField;
    private Border trackNameBorder;
    private TrackInstrumentPicker instrumentPicker;
    private JComboBox<String>gridSizePicker;
    private JComboBox<TrackType>trackTypePicker;
    private NumberInputField volumeField;
    private int borderWidth = 1;
    private int leftMargin = ThemeReader.getMeasure("track.strings.margin.left");
    private Icon muteOnIcon = new ImageIcon("assets/audio-volume-muted.png");
    private Icon muteOffIcon = new ImageIcon("assets/audio-volume-high.png");
    private Icon collapseIcon = new ImageIcon("assets/pan-down-symbolic.symbolic.png");
    private Icon expandIcon = new ImageIcon("assets/pan-end-symbolic.symbolic.png");

    public TrackView(TrackController controller, String name) {
        this.controller = controller;

        drawAreaGuitar = new TrackDrawArea(controller, TrackTypes.Guitar);
        drawAreaBass = new TrackDrawArea(controller, TrackTypes.Bass);
        drawAreaDrums = new TrackDrawArea(controller, TrackTypes.Drums);
        drawArea = drawAreaGuitar;

        setBorder(BorderFactory.createLineBorder(Color.black, borderWidth));
        setLayout(new BorderLayout());

        topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.LINE_AXIS));
        //topBar.setMinimumSize(new Dimension(520, Themes.getTrackTopBarHeight()));
        //topBar.setPreferredSize(new Dimension(520, Themes.getTrackTopBarHeight()));
        add(topBar, BorderLayout.PAGE_START);

        int topElementHeight = ThemeReader.getMeasure("track.topPanel.height") - 5;
        Font topBarFont = new Font("Dialog", Font.PLAIN, 12);

        UIManager.put("Label.font", topBarFont);
        UIManager.put("Button.font", topBarFont);
        UIManager.put("ComboBox.font", topBarFont);
        UIManager.put("PopupMenu.font", topBarFont);
        UIManager.put("MenuItem.font", topBarFont);
        UIManager.put("Menu.font", topBarFont);

        topBar.add(Box.createRigidArea(new Dimension(1, topElementHeight)));

        collapseAction = Constants.BUTTON_TRACKCOLLAPSE;
        collapseButton = new JLabel(collapseIcon);
        collapseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.handleCollapseButton(collapseAction);
            }
        });
        topBar.add(collapseButton);

        muteAction = Constants.BUTTON_TRACKMUTE;
        muteButton = new JLabel(muteOffIcon);
        muteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.handleMuteButton(muteAction);
            }
        });
        topBar.add(muteButton);

        trackNameField = new InputField(name);
        trackNameField.setFont(new Font("Dialog", Font.BOLD, 12));

        trackNameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                trackNameField.setBorder(BorderFactory.createLineBorder(Color.yellow));
            }
            public void focusLost(FocusEvent e) {
                trackNameField.setBorder(trackNameBorder);
            }
        });

        trackNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                    // don't start playing music on first enter after name change
                    evt.consume();
                    controller.handleTrackNameField();
                }
            }
        });

        setComponentSize(trackNameField, 200, topElementHeight);
        topBar.add(trackNameField);

        // save reference to the standard border
        trackNameBorder = trackNameField.getBorder();

        topBar.add(Box.createRigidArea(new Dimension(200, 1)));

        topBar.add(new JLabel("  TrackType "));
        trackTypePicker = new JComboBox<>(TrackTypes.getArray());
        trackTypePicker.setSelectedItem(trackType);
        trackTypePicker.setFocusable(false);
        trackTypePicker.addActionListener((ActionEvent ae) -> {
            controller.handleTrackTypePicker(trackTypePicker.getSelectedItem());
        });
        setComponentSize(trackTypePicker, 70, topElementHeight);
        topBar.add(trackTypePicker);

        topBar.add(new JLabel("  GridSize "));
        String sizes[] = {"1/1", "1/2", "1/4", "1/8", "1/16" , "1/32" , "1/64"};
        gridSizePicker = new JComboBox<>(sizes);
        gridSizePicker.setSelectedIndex(3);
        gridSizePicker.setFocusable(false);
        gridSizePicker.addActionListener((ActionEvent ae) -> {
            String fraction = (String) gridSizePicker.getSelectedItem();

            //https://stackoverflow.com/questions/13249858s
            String[] ratio = fraction.split("/");
            double gridFraction = Double.parseDouble(ratio[0]) / Double.parseDouble(ratio[1]);
            controller.handleGridSizePicker(gridFraction);
        });
        setComponentSize(gridSizePicker, 60, topElementHeight);
        topBar.add(gridSizePicker);

        topBar.add(new JLabel("  Volume "));
        volumeField = new NumberInputField(0, 3);
        volumeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                    // don't start playing music on first enter after volume change
                    evt.consume();
                    int value = getVolumeField();
                    if (value > 100) {
                        value = 100;
                    }
                    controller.handleVolumeField(value);
                }
            }
        });
        setComponentSize(volumeField, 40, topElementHeight);
        topBar.add(volumeField);

        topBar.add(new JLabel("  Instrument "));
        instrumentPicker = new TrackInstrumentPicker(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                ObjectMenuItem item = (ObjectMenuItem) evt.getSource();
                Instrument instrument = (Instrument) item.object;
                controller.handleInstrumentPicker(instrument);
            }
        });
        setComponentSize(instrumentPicker, 180, topElementHeight);
        topBar.add(instrumentPicker);

        drawContainer = new JPanel(null);
        add(drawContainer);

        //TODO eliminate this. use leftmargin in drawarea instead
        side = new JPanel();
        setComponentSize(side, leftMargin, 100);
        drawContainer.add(side);

        gridSizePicker.setFocusTraversalKeysEnabled(false);

        deHighliteBorder();
        addNotifierToAllComponents(this);
    }

    public void setPageView(PageView pageView) {
        this.pageView = pageView;
    }

    public void setTheme() {
        drawArea.setTheme();
    }

    //https://stackoverflow.com/questions/10271116/iterate-through-all-objects-in-jframe
    private void addNotifierToAllComponents(Container parent) {
        for (Component c : parent.getComponents()) {
            addNotifier(c);

            if (c instanceof Container) {
                addNotifierToAllComponents((Container) c);
            }
        }
    }

    /**
     * utility. all components notify page on mouseclick
     * to set this as the selected track.
     */
    private void addNotifier(Component component) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (component instanceof JTextField ||
                    component.getParent() instanceof JComboBox) {
                    controller.handleTrackInput(true);
                } else {
                    controller.handleTrackInput(false);
                }
            }
        });

        KeyAdapter loseFocusOnEnter = new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                    controller.handleTrackInput(false);
                }
            }
        };

        component.addKeyListener(loseFocusOnEnter);
    }

    // utility
    private void setComponentSize(Component component, int width, int height) {
        Dimension d = new Dimension(width, height);
        component.setSize(d);
        component.setMinimumSize(d);
        component.setMaximumSize(d);
        component.setPreferredSize(d);
    }

    // page scrollbars only move drawArea
    public void setScrollPosition(int value) {
        drawArea.setLocation(-value + leftMargin, 0);
    }

    public void highliteBorder() {
        topBar.setBackground(ThemeReader.getColor("track.topPanel.selected.background"));
        trackNameField.setBackground(ThemeReader.getColor("track.nameField.selected.background"));
        if (!trackNameField.isFocused) {
            trackNameField.setBorder(trackNameBorder);
        }
    }

    public void deHighliteBorder() {
        topBar.setBackground(ThemeReader.getColor("track.topPanel.unselected.background"));
        trackNameField.setBackground(ThemeReader.getColor("track.topPanel.unselected.background"));
        trackNameField.setBorder(new LineBorder(ThemeReader.getColor("track.topPanel.unselected.background"), 2));
        hideProgressLine();
    }

    @Override
    public void setName(String name) {
        trackNameField.setText(name);
    }

    public void setInstrumentName(String name) {
        instrumentPicker.setText(name);
    }

    public void setTrackType(TrackType trackType) {
        this.trackType = trackType;
        if (drawArea != null) {
            drawContainer.remove(drawArea);
        }

        String s = trackType.toString();
        if (s.equals("guitar")) {
            drawArea = drawAreaGuitar;
        } else if (s.equals("bass")) {
            drawArea = drawAreaBass;
        } else if (s.equals("drums")) {
            drawArea = drawAreaDrums;
        }

        trackTypePicker.setSelectedItem(trackType);
        addNotifier(drawArea);
        drawContainer.add(drawArea);

        int height = ThemeReader.getMeasure("track.strings.spacing") * trackType.numOfStrings;
        height += ThemeReader.getMeasure("track.strings.margin.top");
        height += ThemeReader.getMeasure("track.strings.margin.bottom");
        setComponentSize(side, leftMargin, height);
        //setComponentSize(side, 0, height);
        adjustMeasureSize(PageView.measureSize);
        //setScrollPosition();
        //revalidate();
        //drawArea.repaint();
    }

    public void adjustMeasureSize(int measureSize) {
        int height = ThemeReader.getMeasure("track.strings.spacing") * trackType.numOfStrings;
        height += ThemeReader.getMeasure("track.strings.margin.top");
        height += ThemeReader.getMeasure("track.strings.margin.bottom");
        setComponentSize(this, PageView.width, height);
        setComponentSize(drawArea, PageView.width, height);
        revalidate();
        repaint();
    }

    private void collapse() {
        setComponentSize(this, PageView.width, ThemeReader.getMeasure("track.topPanel.height") + 2);
        revalidate();
    }

    private void expand() {
        int height = ThemeReader.getMeasure("track.strings.spacing") * trackType.numOfStrings;
        height += ThemeReader.getMeasure("track.strings.margin.top");
        height += ThemeReader.getMeasure("track.strings.margin.bottom");
        setComponentSize(this, PageView.width, height);
        setComponentSize(drawArea, PageView.width, height);
        revalidate();
    }

    public void toggleMuteButton(Constants c) {
        if (c == Constants.BUTTON_TRACKUNMUTE) {
            muteButton.setIcon(muteOnIcon);
            muteAction = Constants.BUTTON_TRACKUNMUTE;
        } else {
            muteButton.setIcon(muteOffIcon);
            muteAction = Constants.BUTTON_TRACKMUTE;
        }
    }

    public void toggleCollapseButton(Constants c) {
        if (c == Constants.BUTTON_TRACKEXPAND) {
            collapseButton.setIcon(collapseIcon);
            expand();
            revalidate();
            collapseAction = Constants.BUTTON_TRACKCOLLAPSE;
        } else {
            collapseButton.setIcon(expandIcon);
            collapse();
            collapseAction = Constants.BUTTON_TRACKEXPAND;
        }
    }

    public void changeCursor(Cursor cursor) {

        //drawArea.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        drawArea.setCursor(cursor);

        //Toolkit toolkit = Toolkit.getDefaultToolkit();
        //Image image = toolkit.getImage("assets/media-playback-start.png");
        //Cursor c = toolkit.createCustomCursor(image , new Point(), "img");
        //drawArea.setCursor(c);
    }

    public void setTrackNameField(String string) {
        trackNameField.setText(string);
    }

    public String getTrackNameField() {
        return trackNameField.getText();
    }

    public void setVolumeField(Object object) {
        volumeField.setValue(Integer.valueOf(object.toString()));
    }

    public int getVolumeField() {
        return volumeField.getValue();
    }

    public void showFretField(Note note, int fretNum) {
        drawArea.showFretField(note, fretNum);
    }

    public void hideFretField() {
        drawArea.hideFretField();
    }

    public String getFretField() {
        return drawArea.fretField.getText().trim();
    }

    public void drawRectangle(Rectangle rect) {
        rect.y = Math.max(0, rect.y);
        rect.x = Math.max(0, rect.x);
         ///additions/subtractions paint slightly more than needed to erase outdated pixels
        drawArea.repaint(rect.x - 8, rect.y - 4, rect.width + 16, rect.height + 8);
    }

    public void drawNote(Note note) {
        drawArea.overwriteNote(note);
    }

    public void drawProgressLine(int x) {
        drawArea.setProgressLine(x);
    }

    public void hideProgressLine() {
        drawArea.setProgressLine(-1000);
    }

    @Override
    public String toString() {
        return "TrackView";
    }

}