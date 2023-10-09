package com.adasoft.phase.label_printer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ReportModel {
	@JsonProperty("ReportId")
    private long reportId;
    @JsonProperty("ReportName")
    private String reportName;
    @JsonProperty("ReportParameters")
    private List<ReportParameterModel> reportParameters; 
}
