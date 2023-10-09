package com.adasoft.phase.rest.model;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class LabelTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private List<Label> labels;
	private final String[] columnNames = {"Name", "Description", "Value"};

	public LabelTableModel(List<Label> labels) {
		this.labels = labels;
	}

	@Override
	public int getRowCount() {
		return labels.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public Label getLabelAt(int row) {
		return labels.get(row);
	}

	// Method to remove a label at a specific row
	public void removeLabelAt(int row) {
		labels.remove(row);
		fireTableRowsDeleted(row, row);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Label label = labels.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return label.getName();
		case 1:
			return label.getDescription();
		case 2:
			return label.getValue();
		default:
			return null;
		}
	}

	// Additional helper methods to update data if needed

	public void setLabels(List<Label> labels) {
		this.labels = labels;
		fireTableDataChanged();
	}

	public Label getLabel(int rowIndex) {
		return labels.get(rowIndex);
	}
}


