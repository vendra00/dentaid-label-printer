package com.adasoft.phase.rest.model;

public enum UIMsgDialect {
	
	STATUS_SUCCESS("Success"),
	STATUS_FAIL("Fail"),
	
	SERVER_UP("Server is UP!"), 
	SERVER_DOWN("Server is Down!"), 
		
	LABEL_CREATED_SUCCESS("Label created successfully!"),
	LABEL_CREATED_FAIL("Label was not created!"),
	LABEL_EDITED_SUCCESS("Label edited successfully!"),
	LABEL_EDITED_FAIL("Label was not edited!"),
	LABEL_DELETED_SUCCESS("Label deleted successfully!"),
	LABEL_DELETED_FAIL("Label was not deleted!"),
	
	LABEL_NAME_FRAME_TABLE("Labels Table"),
	
	// BUTTONS 
	CREATE_BTN_NAME("Create Label"),
	FETCH_BTN_NAME("Fetch Label"),
	EDIT_BTN_NAME("Edit Label"),
	DELETE_BTN_NAME("Delete Label"),
	PING_BTN_NAME("Ping");

	private final String msg;

	UIMsgDialect(String msg) {
		this.msg = msg;
	}

	// Helper method to convert a string to the corresponding UI
	public static UIMsgDialect fromString(String msg) {
		for (UIMsgDialect msgDialect : UIMsgDialect.values()) {
			if (msgDialect.msg.equalsIgnoreCase(msg)) {
				return msgDialect;
			}
		}
		throw new IllegalArgumentException("Invalid Message: " + msg);
	}

	public String getMsgDialect() {
		return msg;
	}
}
