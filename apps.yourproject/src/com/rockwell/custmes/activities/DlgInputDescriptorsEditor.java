package com.rockwell.custmes.activities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author RWeingar
 */
public class DlgInputDescriptorsEditor extends JDialog {

    private static final Log LOGGER = LogFactory.getLog(DlgInputDescriptorsEditor.class);

    private static final long serialVersionUID = 1L;

    protected JTable tab = new JTable();
    protected BeanTableModel<PNutsDescriptor> dataModel;
    protected JPanel panel = new JPanel();
    protected JButton buttonAdd = new JButton();
    protected JButton buttonRemove = new JButton();
    protected JComboBox comboReturn = new JComboBox();
    protected ComboBoxModel cbReturnModel = new DefaultComboBoxModel(ClassUtils.getClassesForPNuts());

    private PNutsInputDescriptors object;

    private static final int BUTTON_HEIGTH = 21;

    private static final int BUTTON_WIDTH = 100;

    private static final int COMBO_WIDTH = 100;

    private static final int DIALOG_HEIGHT = 600;

    private static final int DIALOG_WIDTH = 800;

    private static final int COMBO_RETURN_X = 240;

    private static final int BUTTON_REMOVE_X = 120;

    private static final int BUTTON_ADD_X = 0;

    public DlgInputDescriptorsEditor() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setModal(true);
        setTitle("Descriptor editor");

        try {
            dataModel = new BeanTableModel<PNutsDescriptor>(PNutsDescriptor.class);
        } catch (IntrospectionException e) {
            LOGGER.error("problem on introspection, exception says " + StringUtils.defaultString(e.getMessage()));
        }

        tab.setModel(dataModel);
        ComboBoxModel cbModel = new DefaultComboBoxModel(ClassUtils.getClassesForPNuts());
        JComboBox combobox = new JComboBox();
        combobox.setModel(cbModel);
        DefaultCellEditor editor = new DefaultCellEditor(combobox);
        tab.setDefaultEditor(Class.class, editor);
        Container content = getContentPane();

        tab.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        buttonAdd.setLocation(BUTTON_ADD_X, 0);
        buttonAdd.setSize(BUTTON_WIDTH, BUTTON_HEIGTH);
        buttonAdd.setText("Add");
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataModel.addItem(new PNutsDescriptor());
            }
        });

        buttonRemove.setLocation(BUTTON_REMOVE_X, 0);
        buttonRemove.setSize(BUTTON_WIDTH, BUTTON_HEIGTH);
        buttonRemove.setText("Remove");
        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tab.getSelectedRow();
                dataModel.removeItem(row);
            }
        });

        comboReturn.setLocation(COMBO_RETURN_X, 0);
        comboReturn.setSize(COMBO_WIDTH, BUTTON_HEIGTH);
        comboReturn.setModel(cbReturnModel);
        comboReturn.setVisible(false);

        panel.add(buttonAdd);
        panel.add(buttonRemove);
        panel.add(comboReturn);

        content.setLayout(new BorderLayout());
        content.add(new JScrollPane(tab), BorderLayout.CENTER);
        content.add(panel, BorderLayout.SOUTH);
        content.invalidate();

        object = new PNutsInputDescriptors();
    }

    public void setObject(PNutsInputDescriptors obj) {
        if (obj instanceof PNutsOutputDescriptors) {
            comboReturn.setVisible(true);
            cbReturnModel.setSelectedItem(((PNutsOutputDescriptors) obj).getReturnValue());
        }
        object = obj;
        dataModel.setObjects(obj.getParameters());
    }

    public PNutsInputDescriptors getObject() {
        if (object instanceof PNutsOutputDescriptors) {
            ((PNutsOutputDescriptors) object).setReturnValue((Class<?>) cbReturnModel.getSelectedItem());
        }
        return object;
    }
}
