package com.adasoft.phase.rest.model;

public class Label {
	private String name;
	private String description;
	private float value;

	public Label(String name, String description, float value) {
		this.name = name;
		this.description = description;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public float getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setValue(float value) {
		this.value = value;
	}


}

