package com.rockwell.custmes.activities;

import com.datasweep.compatibility.ui.ContentAlignment;
import com.datasweep.compatibility.ui.FlatLabel;
import com.datasweep.plantops.swing.ControlDock;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationBaseActivity;

/**
 * Visualizes a text message
 * <p>
 *
 * @author rweingar
 */
public class NavigationMessageActivity extends NavigationBaseActivity {
    /**
     * Comment for <code>CONFIG_MESSAGE</code>
     */
    public static final String CONFIG_MESSAGE = "message";

    /**
     * Comment for <code>label</code>
     */
    private FlatLabel label;

    /**
     * Constructor
     */
    public NavigationMessageActivity() {
        super();
        label = new FlatLabel();
        label.setName("labelMessage");
        label.setActivityName("labelMessage");
        label.setDock(ControlDock.FILL);
        label.setTextAlign(ContentAlignment.MIDDLECENTER);
        label.setImageAlign(ContentAlignment.MIDDLELEFT);

        contentPane.add(label);

    }

    @Override
    public String getActivityDescription() {
        return "Displays a confiured message";
    }


    @Override
    protected void configurationItemSet(String key, Object value) {
        super.configurationItemSet(key, value);

        if (CONFIG_MESSAGE.equals(key)) {
            label.setText((String) value);
        }
    }


    @Override
    public ItemDescriptor[] configurationDescriptors() {
        return new ItemDescriptor[] { new ItemDescriptor(CONFIG_MESSAGE, this.getClass(), String.class) };
    }

}
