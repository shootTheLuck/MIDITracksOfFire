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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import javax.swing.event.*;

import page.PageView;
import widgets.*;
import utils.*;
import themes.*;
import note.Note;
import instruments.Instrument;


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
    private Font topBarFont = new Font("Dialog", Font.PLAIN, 12);
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
    //private int topBarHeight = 30;
    private int leftMargin = Themes.margin.get("left");
    private Icon muteOnIcon = new ImageIcon("assets/audio-volume-muted.png");
    private Icon muteOffIcon = new ImageIcon("assets/audio-volume-high.png");
    private Icon collapseIcon = new ImageIcon("assets/pan-down-symbolic.symbolic.png");
    private Icon expandIcon = new ImageIcon("assets/pan-end-symbolic.symbolic.png");

    private JFormattedTextField fretField;
    private Font fretFont = new Font("Dialog", Font.PLAIN, 11);
    private FontMetrics fMetrics = getFontMetrics(fretFont);
    private int fHeight = fMetrics.getAscent();

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

        int topElementHeight = Themes.getTrackTopBarHeight() - 5;

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

        topBar.add(Box.createRigidArea(new Dimension(100, 1)));

        topBar.add(new JLabel("TrackType "));
        trackTypePicker = new JComboBox<>(TrackTypes.getArray());
        trackTypePicker.setSelectedItem(trackType);
        trackTypePicker.addActionListener((ActionEvent ae) -> {
            controller.handleTrackTypePicker(trackTypePicker.getSelectedItem());
        });
        setComponentSize(trackTypePicker, 70, topElementHeight);
        topBar.add(trackTypePicker);

        topBar.add(new JLabel(" GridSize "));
        String sizes[] = {"1/1", "1/2", "1/4", "1/8", "1/16" , "1/32" , "1/64"};
        gridSizePicker = new JComboBox<>(sizes);
        gridSizePicker.setSelectedIndex(3);
        gridSizePicker.addActionListener((ActionEvent ae) -> {
            String fraction = (String) gridSizePicker.getSelectedItem();

            //https://stackoverflow.com/questions/13249858s
            String[] ratio = fraction.split("/");
            double gridFraction = Double.parseDouble(ratio[0]) / Double.parseDouble(ratio[1]);
            controller.handleGridSizePicker(gridFraction);
        });
        setComponentSize(gridSizePicker, 60, topElementHeight);
        topBar.add(gridSizePicker);

        topBar.add(new JLabel(" Volume "));
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

        topBar.add(new JLabel(" Instrument "));
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

        side = new JPanel();
        setComponentSize(side, leftMargin, 100);
        drawContainer.add(side);

        //setTrackType(trackType);

        deHighliteBorder();

        //fretField = new NumberInputField(2);
            //fretField.setFont(fretFont);
            //hideComponent(fretField);
            //setComponentSize(fretField, 20, 20);
            //drawArea.add(fretField, 2);

        try {
            MaskFormatter maskFormatter = new MaskFormatter("##");
            fretField = new JFormattedTextField(maskFormatter);
            fretField.setColumns(2);
            fretField.setFont(fretFont);
            hideComponent(fretField);

            Runnable sendUpdate = new Runnable() {
                @Override
                public void run() {
                    controller.handleFretField(false);
                }
            };

            fretField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    int keyCode = evt.getKeyCode();

                    if (keyCode >= 96 && keyCode <= 105) {
                        SwingUtilities.invokeLater(sendUpdate);
                    }
                    if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                        SwingUtilities.invokeLater(sendUpdate);
                    }
                    if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
                        controller.handleFretField(true);
                    }
                }
            });

            setComponentSize(fretField, 20, 20);
            drawArea.add(fretField, 2);

        } catch(Exception e) {
            e.printStackTrace();
        }

        listAllComponents(this);

    }

    public void setPageView(PageView pageView) {
        this.pageView = pageView;
    }

    //https://stackoverflow.com/questions/10271116/iterate-through-all-objects-in-jframe
    private void listAllComponents(Container parent) {
        for (Component c : parent.getComponents()) {
            addNotifier(c);

            if (c instanceof Container) {
                listAllComponents((Container) c);
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

    // utility
    private void hideComponent(Component c) {
        c.setLocation(-10000, 0);
    }

    // page scrollbars only move drawArea
    public void setScrollPosition(int value) {
        drawArea.setLocation(-value + leftMargin, 0);
    }

    public void highliteBorder() {
        topBar.setBackground(Themes.colors.get("selectedTrack"));
        trackNameField.setBackground(Color.white);
        if (!trackNameField.isFocused) {
            trackNameField.setBorder(trackNameBorder);
        }
    }

    public void deHighliteBorder() {
        topBar.setBackground(Themes.colors.get("unSelectedTrack"));
        trackNameField.setBackground(Themes.colors.get("unSelectedTrack"));
        trackNameField.setBorder(new LineBorder(Themes.colors.get("unSelectedTrack"), 2));
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

        setComponentSize(side, leftMargin, Themes.getTrackHeight(trackType.numOfStrings));
        adjustMeasureSize(PageView.measureSize);
        //setScrollPosition();
        //revalidate();
        //drawArea.repaint();
    }

    public void adjustMeasureSize(int measureSize) {
        setComponentSize(this, PageView.width, Themes.getTrackHeight(trackType.numOfStrings));
        setComponentSize(drawArea, PageView.width, Themes.getTrackHeight(trackType.numOfStrings));
        revalidate();
        repaint();
    }

    private void collapse() {
        setComponentSize(this, PageView.width, Themes.getTrackTopBarHeight() + 2);
        revalidate();
    }

    private void expand() {
        setComponentSize(this, PageView.width, Themes.getTrackHeight(trackType.numOfStrings));
        setComponentSize(drawArea, PageView.width, Themes.getTrackHeight(trackType.numOfStrings));
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
        fretField.setLocation(note.x - 2, note.y - 5);
        //fretField.requestFocusInWindow();
        fretField.grabFocus();
        fretField.setText(String.valueOf(fretNum));
    }

    public void hideFretField() {
        fretField.setText("");
        hideComponent(fretField);
    }

    public String getFretField() {
        return fretField.getText().trim();
    }

    public void drawRectangle(Rectangle rect) {
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