package com.adasoft.phase.label_printer.service;

import com.adasoft.phase.label_printer.model.Request;

public interface LabelPrinterService {
	boolean sendlabelToPrint(Request request);
}
