package com.rockwell.mes.myeig.model;



import com.rockwell.mes.services.s88.impl.recipe.MESERPBomItem;


public class CustERPBOMItem extends MESERPBomItem {
	

	String componentPart;
	String componentQty;
	String componentUoM;
	String componentFixedQty;
	String alternativeItem;


	public String getComponentPart() {
		return componentPart;
	}

	public void setComponentPart(String componentPart) {
		this.componentPart = componentPart;
	}

	public String getComponentQty() {
		return componentQty;
	}

	public void setComponentQty(String componentQty) {
		this.componentQty = componentQty;
	}

	public String getComponentUoM() {
		return componentUoM;
	}

	public void setComponentUoM(String componentUoM) {
		this.componentUoM = componentUoM;
	}

	public String getComponentFixedQty() {
		return componentFixedQty;
	}

	public void setComponentFixedQty(String componentFixedQty) {
		this.componentFixedQty = componentFixedQty;
	}

	public String getAlternativeItem() {
		return alternativeItem;
	}

	public void setAlternativeItem(String alternativeItem) {
		this.alternativeItem = alternativeItem;
	}
}
