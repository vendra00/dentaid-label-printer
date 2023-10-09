package com.adasoft.phase.rest.service;

import java.util.List;

import com.adasoft.phase.rest.model.Label;
import com.adasoft.phase.rest.model.Request;

public interface LabelService {
	List<Label> fetchAllLabelsFromServer(Request request);
	boolean pingServer(Request request);	
	boolean createLabelInServer(Request request);
	boolean editLabelInServer(Request request);
	boolean deleteLabelInServer(Request request);
}
