package track;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import instruments.Instruments;
import widgets.ObjectMenuItem;

class TrackInstrumentPicker extends JButton {

    public TrackInstrumentPicker(ActionListener action) {
        super();
        setFocusPainted(false);
        JPopupMenu popUp = new JPopupMenu();

        for (int i = 0; i < Instruments.categories.length; i ++) {
            JMenu category = new JMenu(Instruments.categories[i]);
            popUp.add(category);
        }

        addActionListener((ActionEvent evt) -> {
            popUp.show(TrackInstrumentPicker.this, 0, 0);
        });

        ActionListener setDisplayName = (ActionEvent evt) -> {
            String instName = evt.getActionCommand();
            TrackInstrumentPicker.this.setText(instName);
        };

        MenuElement[] categories = popUp.getSubElements();

        for (int i = 0; i < categories.length - 1; i++) {
            for (int j = i * 8; j < i * 8 + 8; j++) {
                JMenuItem instNameItem = new ObjectMenuItem(Instruments.list.get(j));
                instNameItem.addActionListener(setDisplayName);
                instNameItem.addActionListener(action);
                JMenuItem m = (JMenuItem) categories[i];
                m.add(instNameItem);
            }
        }

        for (int j = 0; j < Instruments.drumList.size(); j++) {
            JMenuItem drumNameItem = new ObjectMenuItem(Instruments.drumList.get(j));
            drumNameItem.addActionListener(setDisplayName);
            drumNameItem.addActionListener(action);
            JMenuItem m = (JMenuItem) categories[categories.length - 1];
            m.add(drumNameItem);
        }
    }

}