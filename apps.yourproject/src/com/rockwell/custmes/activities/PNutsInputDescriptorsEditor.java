package com.rockwell.custmes.activities;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.datasweep.plantops.property.editor.EditorSupport;

public class PNutsInputDescriptorsEditor extends EditorSupport implements ActionListener {
    private JLabel tfLabel;
    private JButton button;
    private static final int PANEL_HEIGHT = 500;
    private static final int PANEL_WIDTH = 1000;

    public PNutsInputDescriptorsEditor() {
        tfLabel = new JLabel();

        button = new JButton("...");
        button.setMargin(EditorSupport.BUTTON_MARGIN);
        button.addActionListener(this);

        panel = new JPanel();
        panel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        panel.setLayout(new BorderLayout());
        panel.add(tfLabel, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);

    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();

        if (src instanceof JButton) {
            PNutsInputDescriptors obj = (PNutsInputDescriptors) getValue();

            DlgInputDescriptorsEditor dlg = new DlgInputDescriptorsEditor();
            if (obj != null) {
                dlg.setObject(obj);
            }
            dlg.setVisible(true);
            obj = dlg.getObject();
            setValue(obj);
            dlg.dispose();
        }
    }


    @Override
    public void setValue(Object value) {
        super.setValue(value);
        String text = "";
        if (value instanceof PNutsInputDescriptors) {
            if (value != null) {
                text = ((PNutsInputDescriptors) value).toString();

            }
            String[] fragments = text.split("\n");
            for (int idx = 0; idx < fragments.length; idx++) {
                String frag = fragments[idx];
                if (frag.trim().length() > 0) {
                    if (idx + 1 == fragments.length) {
                        text = frag;
                    } else {
                        text = frag + " ...";
                    }
                    break;
                }
            }
        }
        tfLabel.setText(text);
        tfLabel.invalidate();
    }
}
