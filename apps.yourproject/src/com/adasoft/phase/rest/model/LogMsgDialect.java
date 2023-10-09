package com.adasoft.phase.rest.model;

public enum LogMsgDialect {
	
	//**********************
	// RtPhaseViewRest0100
	//**********************
	
	// INFO
	CREATE_UI_CALL("Create UI Call"),
	CREATE_ACTION_PANEL_CALL("Create Action Panel Call"),
	
	// WARNING
	NO_REQUEST_FOUND("No request found!"),
	NO_MATCHING_REQUEST_FOUND("No matching request found!"),
	
	
	//**********************
	// RtPhaseViewRest0100
	//**********************
	
	// INFO
	START_CALL("Start Call"),
	PHASE_KEY_VALUE("PHASE KEY: "),
	BODY_NOT_PARSED_VALUES("BODY VALUES NOT PARSED: "),
	CREATED_REQUEST_OBJECT("Created Request: "),
	
	//ERROR
	FORMAT_DATA_ERROR("Unexpected format for data: "),
	
	
	//**********************
	// LabelServiceImpl
	//**********************
	
	// INFO
	FETCH_ALL_LABELS_FROM_SERVER("Fetch All Labels From Server Call"),
	FETCH_ALL_LABELS_FROM_SERVER_OK("The fetch labels from server request was successful: "),	
	PING_SERVER_CALL("Ping Server Call"),
	PING_OK("Server is reachable."),	
	CREATE_LABEL_IN_SERVER_CALL("Create Label In Server Call"),
	CREATE_LABEL_RESPONSE_FROM_SERVER("Response from server: \""),
	CREATE_LABEL_OK("Label created successfully!"),	
	EDIT_LABEL_IN_SERVER_CALL("Edit Label In Server Call"),
	EDIT_LABEL_RESPONSE_FROM_SERVER("Response from server: "),
	EDIT_LABEL_OK("Label edited successfully!"),	
	DELETE_LABEL_IN_SERVER_CALL("Delete Label In Server Call"),
	DELETE_LABEL_OK("Label deleted successfully!"),
	
	// WARNING
	FETCH_ALL_LABELS_FROM_SERVER_KO("Failed to fetch labels from server: "),
	PING_KO("Failed to reach server. Status code: "),
	CREATE_LABEL_KO("Label creation failed: "),
	EDIT_LABEL_KO("Label editing failed: "),
	DELETE_LABEL_KO("Label deletion failed: "),
		
	// ERROR
	CREATE_LABEL_ERROR("An error occurred while creating the label in the server: "),
	FETCH_ALL_LABELS_FROM_SERVER_ERROR("An error occurred while fetching all labels call: "),
	PING_SERVER_ERROR("An error occurred while pinging the server: "),
	EDIT_LABEL_ERROR("An error occurred while editing the label: "),
	PARSING_JSON_ERROR("An error occurred while parsing JSON: "),
	ENCONDING_URL_ERROR("Error encoding the URL: "),
	DELETE_LABEL_ERROR("An error occurred while deleting the label: "),
	
	
	//**********************
	//RequestServiceImpl
	//**********************
	
	// INFO
	CREATE_REQUEST_CALL("Create Request Call"),
	PARSED_BODY_TO_JSON("Parsed BODY to JSON: "),
	
	// ERROR
	CONVERT_BODY_TO_JSON_ERROR("An error occurred when converting the body to json: ");
	
	private final String msg;

	LogMsgDialect(String msg) {
		this.msg = msg;
	}

	// Helper method to convert a string to the corresponding LOG
	public static LogMsgDialect fromString(String msg) {
		for (LogMsgDialect msgDialect : LogMsgDialect.values()) {
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
