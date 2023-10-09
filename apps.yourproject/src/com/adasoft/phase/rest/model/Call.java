package com.adasoft.phase.rest.model;

public enum Call {
	PING("ping"), 
	CREATE_LABEL("create-label"),
	EDIT_LABEL("update-by-name"),
	DELETE_LABEL("delete-label-by-name"),
	FETCH_LABELS("get-all-labels");
	

	private final String urlCallName;

	Call(String urlCallName) {
		this.urlCallName = urlCallName;
	}

	// Helper method to convert a string to the corresponding Call
	public static Call fromString(String urlCallName) {
		for (Call call : Call.values()) {
			if (call.urlCallName.equalsIgnoreCase(urlCallName)) {
				return call;
			}
		}
		throw new IllegalArgumentException("Invalid Call: " + urlCallName);
	}

	public String getUrlCallName() {
		return urlCallName;
	}
}
