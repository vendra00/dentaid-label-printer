package com.rockwell.mes.myeig.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MasterRecipeFilter;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.UnitOfMeasure;

import com.datasweep.plantops.common.constants.IObjectTypes;

import com.rockwell.integration.messaging.MessageEnvelope;

import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.myeig.data.PvBomTransferObject;
import com.rockwell.mes.myeig.data.PvBomTransferObject.PVBOMItem;
import com.rockwell.mes.myeig.model.CustERPBOMItem;
import com.rockwell.mes.myeig.service.ifc.CustERPBomHeader;
import com.rockwell.mes.myeig.service.ifc.CustERPBomHeader.CustERPBomItem;
import com.rockwell.mes.myeig.utility.IntegrationGatewayHelper;
import com.rockwell.mes.services.s88.ifc.recipe.RecipeConfiguration;
/**
 * This class implements functionality to process incoming order messages
 * <p>
 * 
 * @author Adasoft
 */
public class PvBomTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(PvBomTransferObjectInboundAction.class);
    
    private static final String SynthonPlantCode = "2100";
    private static final String ValorUdaSi = "1";
    private static final String ValorUdaNo = "0"; 
    private static final String BulkMaterial = "30";
    private static final String RecipeState = "Verification";

    @Override
    public synchronized void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {
    	PvBomTransferObject pvBomTransferObject = (PvBomTransferObject) data.getPayload();
    	setDocNum(pvBomTransferObject.getIdoc());
    	Map<String, Object> ordUdaMap = new HashMap<String, Object>();
    	Map<String, Object> componentUdaMap = new HashMap<String, Object>();    	
    	List<CustERPBomItem> components = new ArrayList<CustERPBomItem>();
    	
    	
    	
//------------------------------- 1. Material to produce from PVBOM --------------------------------------------------------------
    	Part part = validate(true,pvBomTransferObject.getPvbom().getMaterial(),Part.class,"Material OSO PVBOM");
//-------------------------------------------------------------------------------------------------------------------------------- 
    	
//------------------------------- 2. Product version -----------------------------------------------------------------------------
    	String productVersion = validate(true,pvBomTransferObject.getPvbom().getProdVersion(),String.class,"Product Version");
//--------------------------------------------------------------------------------------------------------------------------------       

//------------------------------- 3. OrderPlant ----------------------------------------------------------------------------------
        String Plant = validate(true,pvBomTransferObject.getPvbom().getPlantCode(), String.class, "Plant");
        if (Plant != null && !StringUtils.equals(Plant, SynthonPlantCode)) {
            addError(this.getClass().getName(), "Plant code is incorrect.\n");            
        }
//--------------------------------------------------------------------------------------------------------------------------------
        
//------------------------------- 4. Base Quantity -------------------------------------------------------------------------------
    	String baseQty = validate(true,pvBomTransferObject.getPvbom().getBaseQty(),String.class,"Base Quantity");
//--------------------------------------------------------------------------------------------------------------------------------
    	
//------------------------------- 5. Unit of Measure -----------------------------------------------------------------------------
    	UnitOfMeasure UoM = validate(true,pvBomTransferObject.getPvbom().getUom(),UnitOfMeasure.class,"Unit of Measure");
    	//AFH: Check UoM is the related with the material (OSO) -- Optional
//--------------------------------------------------------------------------------------------------------------------------------
 
//------------------------------- 6. Create ERP Bom Header -----------------------------------------------------------------------

    	CustERPBomHeader custErpBomHeader = new CustERPBomHeader();
    	custErpBomHeader.setPart(part);
    	custErpBomHeader.setProductVersion(productVersion);
    	custErpBomHeader.setPlant(Plant);
        custErpBomHeader.setBaseQty(IntegrationGatewayHelper.getMeasuredValue(baseQty,UoM));
        //custErpBomHeader.Save(PCContext.getCurrentClientTime(), "Save BOM Header",PCContext.getDefaultAccessPrivilege());
//--------------------------------------------------------------------------------------------------------------------------------  
      
//------------------------------- 7. Loop to create every BOM Item ---------------------------------------------------------------  
    	for( PVBOMItem pvbomItem : pvBomTransferObject.getPvbom().getPVBOMItems()) {
    		
//------------------------------- 7.1 Material Position -------------------------------------------------------------------------
	    	String componentPos = validate(true,pvbomItem.getPosition(),String.class,"Part Position");
//--------------------------------------------------------------------------------------------------------------------------------

//------------------------------- 7.2 BOM Component (OSI) -----------------------------------------------------------------------
	    	Part componentPart = validate(true,pvbomItem.getBomMaterial(),Part.class,"Component Part");
//--------------------------------------------------------------------------------------------------------------------------------

//------------------------------- 7.3 Component Quantity -------------------------------------------------------------------------
	    	String componentQty = validate(true,pvbomItem.getComponentQty(),String.class,"Component Quantity");
//--------------------------------------------------------------------------------------------------------------------------------
	    	
//------------------------------- 7.4 Component Unit of Measure ------------------------------------------------------------------
	    	String componentUoM = validate(true,pvbomItem.getComponentUom(),String.class,"Component Unit of Measure");
//--------------------------------------------------------------------------------------------------------------------------------
	    	
//------------------------------- 7.5 Fixed Quantity -----------------------------------------------------------------------------
	    	String componentFixedQty = validate(true,pvbomItem.getFixQty(),String.class,"Component fixed Quantity");
//--------------------------------------------------------------------------------------------------------------------------------  
	    	
//------------------------------- 7.6 Alternative Item ---------------------------------------------------------------------------
	    	String alternativeItem = validate(true,pvbomItem.getAlternativeItem(),String.class,"Alternative Item");
//--------------------------------------------------------------------------------------------------------------------------------
	    	
//------------------------------- 7.7 Create ERP BOM Items with ERP BOM Header ---------------------------------------------------
	        CustERPBOMItem custErpBomItem = new CustERPBOMItem();	        
	        custErpBomItem.setErpBomHeader(custErpBomHeader);        
	        custErpBomItem.setPosition(componentPos);
	        custErpBomItem.setComponentPart(componentPart.toString());
	        custErpBomItem.setComponentQty(componentQty);
	        custErpBomItem.setComponentUoM(componentUoM);
	        custErpBomItem.setComponentFixedQty(componentFixedQty);
	        custErpBomItem.setAlternativeItem(alternativeItem);
//--------------------------------------------------------------------------------------------------------------------------------
	        
//------------------------------- 7.8. Reorder the componentPart positions -------------------------------------------------------     
    	    String MaterialType = "";
            String MaterialGroup = "";
            
            MaterialType = validate(false, componentPart.getUDA("X_materialType").toString(), String.class, "Material Type");
            if (componentPart.getUDA("X_erp01") == null) {
            	addError(this.getClass().getName(), "This material:" + componentPart + " don't have material group (UDA_X_erp01). \n");
            } else {
            MaterialGroup = validate(false, componentPart.getUDA("X_erp01").toString(), String.class, "Material Group");
            }
            
            if (!StringUtils.isEmpty(MaterialType)) {
            	if (MaterialType.equals(BulkMaterial)) {
            		logInfo(LOGGER, "Asignar la posicion 10 al material Bulk");
            		componentPos = "0010";
            		//AFH If two BulkMaterial arrive the first is position 0010  and the second 0011
            	}
            	else {
            		switch (MaterialGroup) {
                    // ----------- PVC -----------
                    case ("1013"): {
                        logInfo(LOGGER, "Asignar la posicion 20 al material PVC");                                   
                        componentPos = "0020";
                        break;
                    }
                    // ----------- Aluminium_Preprinted -----------
                    case "1011": {
                        ordUdaMap.put("ct_Alu_Preprinted", ValorUdaSi);
                        logInfo(LOGGER, "Asignar la posicion 30 al material Aluminium Preprinted");
                        componentPos = ("0030");
                        break;
                    }
                 // ----------- Aluminium -----------
                    case "1018":
                    case "1019": {
                        logInfo(LOGGER, "Asignar la posicion 30 al material Aluminium");
                        componentPos = ("0030");
                        break;
                    }
                    // ----------- Leaflet -----------
                    case ("1020"): {
                    	logInfo(LOGGER, "Asignar la posicion 40 al material Leaflet");
                        componentPos = "0040";
                        break;
                    }
                    // ----------- Box -----------
                    case ("1021"): {
                    	logInfo(LOGGER, "Asignar la posicion 50 al material Box");
                        componentPos = "0050";
                        break;
                    }
                    // ----------- Shipping Box -----------
                    case ("1030"): {
                    	logInfo(LOGGER, "Asignar la posicion 60 al material Shipping Box");
                        componentPos = ("0060");
                        break;
                    }
                    // ----------- Tamper Label -----------
                    case ("1025"): {
                    	logInfo(LOGGER, "Asignar la posicion 70 al material Tamper Label");
                        componentPos = ("0070");
                        break;
                    }
                    // ----------- Customer Label -----------

                    case ("1022"): {
                    	logInfo(LOGGER, "Asignar la posicion 80 al material Customer Label");
                        componentPos = ("0080");
                        break;
                    }
                    // ----------- Primary Level -----------
                    case ("1010"): {
                    	logInfo(LOGGER, "Asignar la posicion 90 al material Primary Level");
                        componentPos = ("0090");
                        break;
                    }
                    // ----------- Bottle -----------
                    case ("1028"): {
                    	logInfo(LOGGER, "Asignar la posicion 100 al material Bottle");
                        componentPos = "0100";
                        break;
                    }
                    // ----------- Caps -----------
                    case ("1029"): {
                    	logInfo(LOGGER, "Asignar la posicion 110 al material Caps");
                        componentPos = "0110";
                        break;
                    }
                 // ----------- ALU SAC -----------
                    case "1017":{
                    	logInfo(LOGGER, "Asignar la posicion 120 al material ALU SAC");
                    	componentPos = "0120";
                    	break;
                    }
                    default:
                        if (MaterialGroup != "1025") {
                            ordUdaMap.put("ct_TamperExist", ValorUdaSi);
                        } else {
                            ordUdaMap.put("ct_TamperExist", ValorUdaNo);
                        }
                        if (MaterialGroup != "1022") {
                            ordUdaMap.put("ct_Bolino_Rel", ValorUdaSi);
                        } else {
                            ordUdaMap.put("ct_Bolino_Rel", ValorUdaNo);
                        }
            		}
            	}
            }
    	}
    		CustERPBomItem itemBuilder = new CustERPBomItem();
    		itemBuilder.udaMap(componentUdaMap);
    		components.add(itemBuilder);
    		CustERPBomHeader builder = new CustERPBomHeader().udaMap(ordUdaMap);
//------------------------------- End of for loop (point 7) --------------------------------------------------------------- 
    	

	    
//------------------------------- 8. Building Block used ------------------------------------------------------------------
	    String BBName = null;
        switch (pvBomTransferObject.getPvbom().getPackLine()) {
        case "1":
            BBName = "";
            break;
        case "2":    
        	BBName = "";
             break;
        case "3":
        	BBName = "";
            break;
        case "4":
        	BBName = "";
            break;
        case "5":
        	BBName = "";
            break;
        case "6":
        	BBName = "";
            break;
        case "7":
        	BBName = "BB_Pack_UHL";
            break;
        case "8":
        	BBName = "BB_Pack_UHL";
            break;
        }
//------------------------------- 9. Lookup if already exist master recipe ------------------------------------------------
		
      		MasterRecipe recipe = null;
      	    recipe = getValidMasterRecipe(part, productVersion);
      	    if (recipe != null) {
      	            addError(this.getClass().getName(), "That MasterRecipe already exists: " + recipe);
      	        }
      	    else {
        //API Call Rockwell
      	    		recipe = CreateMasterRecipe(builder, RecipeState, BBName);
      	    }
    }
//------------------------------- End of ProcessActivityData---------------------------------------------------------


    
    private MasterRecipe getValidMasterRecipe(final Part part, final String productVersion) throws DatasweepException {
        logInfo(LOGGER, "Determining master recipe");
        MasterRecipe masterRecipe = null;
        if (part == null) {
            return masterRecipe;
        }
        String separator = MESConfiguration.getMESConfiguration().getString("eig_MasterRecipeDelimitString", "_", "Master recipe delimiter");
        // String recipeName = product.getPartNumber() + separator + routeMap;
        // Personalización de Synthon: no concatenaremos ProductMaterial_RouteMap, sino que solo hacemos "_routeMap;"
        String recipeName = separator + productVersion;
        MasterRecipeFilter masterRecipeFilter = PCContext.getFunctions().createMasterRecipeFilter();
        // masterRecipeFilter.forNameEqualTo(recipeName);
        masterRecipeFilter.forNameContaining(recipeName);

        // RaulDG: en pruebas para poder calcular el código de la receta cuando es receta de Pack
        // if (StringUtils.equals(OutboundMessageService.PACKAGING_ORDER_TYPE_VALUE, OrderType)) {
        // product.setPartNumber("DummyBOM_Material");
        // }
        // else {
        masterRecipeFilter.forProducedPartEqualTo(part);
        // }

        if (!RecipeConfiguration.allowNonValidMasterRecipeForOrder()) {
            masterRecipeFilter.forCurrentStateEqualTo("Valid");
        }
        final List recipeList = masterRecipeFilter.exec();

        if (recipeList.size() == 1) { // only return the master recipe if exactly 1 is found
            masterRecipe = (MasterRecipe) recipeList.get(0);
            logInfo(LOGGER, "Master recipe found: " + masterRecipe.getName());
        }

        return masterRecipe;
    }
    private MasterRecipe CreateMasterRecipe(final CustERPBomHeader builder, final String recipeState, String BBName ) throws DatasweepException {
    	MasterRecipe masterRecipe = null;
    	return masterRecipe;
    }
    
    @Override
	public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_BOM);
	}
}
//------------------------------- End of PvBomTransferObjectInboundAction --------------------------------------------------------------
