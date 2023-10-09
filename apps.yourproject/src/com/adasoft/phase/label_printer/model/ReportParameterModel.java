package com.adasoft.phase.label_printer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ReportParameterModel {
    @JsonProperty("Parameter")
    private String parameter;
    @JsonProperty("Value")
    private String value;
}
