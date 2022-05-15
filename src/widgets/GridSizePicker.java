
package widgets;


import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JSeparator;
import java.util.LinkedHashMap;

//import utils.BiMap;
import utils.console;

public class GridSizePicker extends SeparatorComboBox {

    private static LinkedHashMap<String, Double> SIZES_STRAIGHT = new LinkedHashMap<>();
    private static LinkedHashMap<String, Double> SIZES_TRIPLET = new LinkedHashMap<>();
    public static LinkedHashMap<String, Double> SIZES = new LinkedHashMap<>();

    static {

        SIZES_STRAIGHT.put( "1/1", 1.0 );
        SIZES_STRAIGHT.put( "1/2", 0.5 );
        SIZES_STRAIGHT.put( "1/4", 0.25 );
        SIZES_STRAIGHT.put( "1/8", 0.125 );
        SIZES_STRAIGHT.put( "1/16", 0.0625 );
        SIZES_STRAIGHT.put( "1/32", 0.03125 );
        SIZES_STRAIGHT.put( "1/64", 0.015625 );

        SIZES_TRIPLET.put( "1/1T", 0.6666666666667 );
        SIZES_TRIPLET.put( "1/2T", 0.3333333333333 );
        SIZES_TRIPLET.put( "1/4T", 0.1666666666667 );
        SIZES_TRIPLET.put( "1/8T", 0.0833333333333 );
        SIZES_TRIPLET.put( "1/16T", 0.0416666666667 );
        SIZES_TRIPLET.put( "1/32T", 0.0208333333333 );
        SIZES_TRIPLET.put( "1/64T", 0.0104166666667 );

        SIZES_STRAIGHT.forEach((key,value) -> SIZES.put(key, value));
        SIZES_TRIPLET.forEach((key,value) -> SIZES.put(key, value));
    }

    private int previousIndex = 0;
    private boolean isShowingTriplets = false;

    public GridSizePicker() {
        super();

        addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent evt) {

                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    String selectedItem = (String)getSelectedItem();
                    if (selectedItem.equals("Triplet")) {
                        isShowingTriplets = !isShowingTriplets;
                        if (isShowingTriplets) {
                            showTriplet();
                        } else {
                            showStraight();
                        }
                        setSelectedIndex(previousIndex);

                    } else {

                    }
                }
            }
        });

        addActionListener((ActionEvent ae) -> {
            String selectedItem = (String)getSelectedItem();
            if (selectedItem.equals("Triplet")) {

            } else {
                previousIndex = getSelectedIndex();
            }
        });

        showStraight();
    }


    @Override
    protected void fireActionEvent() {
        // if the mouse made the selection -> the comboBox has focus
        //if (this.hasFocus()) {
            super.fireActionEvent();
        //}
    }

    protected double getFraction(String item) {
        return SIZES.get(item);
    }

    protected void showGridSize(double fraction) {
        SIZES.forEach((key,value) -> {
            if (value == fraction) {
                setSelectedItem(key);
                return;
            }
        });
        setSelectedItem("1/8");
    }

    private void showStraight() {
        removeAllItems();
        SIZES_STRAIGHT.forEach((key,value) -> addItem(key));
        addItem( new JSeparator() );
        addItem( "Triplet" );
        //mode = modes.STRAIGHT;
    }

    private void showTriplet() {
        removeAllItems();
        SIZES_TRIPLET.forEach((key,value) -> addItem(key));
        addItem( new JSeparator() );
        addItem( "Triplet" );
        //mode = modes.TRIPLET;
    }

}