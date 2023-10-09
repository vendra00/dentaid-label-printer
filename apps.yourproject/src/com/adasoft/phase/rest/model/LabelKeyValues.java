package com.adasoft.phase.rest.model;

public enum LabelKeyValues {
	
	URL("URL"), 
	REST_CALL("RESTful Call");
	
	private final String labelKeyValue;

	LabelKeyValues(String labelKeyValue) {
		this.labelKeyValue = labelKeyValue;
	}

	// Helper method to convert a string to the corresponding label key value
	public static LabelKeyValues fromString(String labelKeyValue) {
		for (LabelKeyValues value : LabelKeyValues.values()) {
			if (value.labelKeyValue.equalsIgnoreCase(labelKeyValue)) {
				return value;
			}
		}
		throw new IllegalArgumentException("Invalid label key: " + labelKeyValue);
	}

	public String getLabelKeyValue() {
		return labelKeyValue;
	}
}
