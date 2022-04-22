package track;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import instruments.Instrument;
import widgets.ObjectMenuItem;
import utils.console;

class TrackInstrumentPicker extends JButton {

    public TrackInstrumentPicker(ActionListener action) {
        super();
        setFocusPainted(false);
        JPopupMenu popUp = new JPopupMenu();

        for (int i = 0; i < Instrument.categories.length; i ++) {
            JMenu category = new JMenu(Instrument.categories[i]);
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
            //console.log(categories[i]);
            for (int j = i * Instrument.NUM_PER_CATEGORY;
                     j < i * Instrument.NUM_PER_CATEGORY + Instrument.NUM_PER_CATEGORY; j++) {
                JMenuItem instNameItem = new ObjectMenuItem(Instrument.list.get(j));
                instNameItem.addActionListener(setDisplayName);
                instNameItem.addActionListener(action);
                JMenuItem m = (JMenuItem)categories[i];
                m.add(instNameItem);
            }
        }

        for (int j = 0; j < Instrument.drumList.size(); j++) {
            JMenuItem drumNameItem = new ObjectMenuItem(Instrument.drumList.get(j));
            drumNameItem.addActionListener(setDisplayName);
            drumNameItem.addActionListener(action);
            JMenuItem m = (JMenuItem)categories[categories.length - 1];
            m.add(drumNameItem);
        }
    }

}