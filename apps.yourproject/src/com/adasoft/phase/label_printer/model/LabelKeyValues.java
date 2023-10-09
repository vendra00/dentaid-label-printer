package com.adasoft.phase.label_printer.model;

public enum LabelKeyValues {
	
	URL("URL"), 
	BODY("Body"), 
	APYKEY("ApiKey");
	
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
