package com.rockwell.mes.myeig.activities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MasterRecipeFilter;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.compatibility.client.Part;
import com.datasweep.plantops.common.constants.IObjectTypes;
import com.rockwell.integration.messaging.MessageEnvelope;
import com.rockwell.library.commons.parser.gs1.GS1AIPair;
import com.rockwell.library.commons.parser.gs1.GS1ApplicationIdentifier;
import com.rockwell.library.commons.parser.gs1.GS1Parser;
import com.rockwell.library.commons.parser.gs1.GS1ParserResult;
import com.rockwell.library.commons.parser.gs1.exception.GS1ParserException;
import com.rockwell.mes.commons.base.ifc.choicelist.IMESChoiceElement;
import com.rockwell.mes.commons.base.ifc.choicelist.MESChoiceListHelper;
import com.rockwell.mes.commons.base.ifc.configuration.MESConfiguration;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.myeig.data.MaterialTransferObject;
import com.rockwell.mes.myeig.data.MaterialTransferObject.Material;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialAdditionalData;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialPlant;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialText;
import com.rockwell.mes.myeig.data.MaterialTransferObject.MaterialUnitOfMeasure;
import com.rockwell.mes.myeig.data.OrderTransferObject.Order;
import com.rockwell.mes.myeig.service.ifc.IInboundMessageService;
import com.rockwell.mes.myeig.utility.IntegrationGatewayHelper;
import com.rockwell.mes.services.inventory.ifc.AbstractBatchQualityTransitionEventListener;
import com.rockwell.mes.services.s88.ifc.recipe.RecipeConfiguration;

/**
 * This class implements functionality to process incoming material messages
 * <p>
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All
 *         Rights Reserved.
 */
public class MaterialTransferObjectInboundAction extends AbstractInboundActivity {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(MaterialTransferObjectInboundAction.class);

    private static final Locale LOCALE_ESP = new Locale("es", "ES");
    private static final Locale LOCALE_ENG = new Locale("en", "EN");

    private static final String CONVERSION_FACTOR_PALLET = "Pallet";
    private static final String CONVERSION_FACTOR_CAJA = "Caja";

    private static final String SynthonPlantCode = "2100"; // Codigo de planta ESP

    private static final String MaterialTypeChoice = "50";

    @Override
    public void processActivityData(final MessageEnvelope data) throws DatasweepException, MESException {

        MaterialTransferObject materialTransferObject = (MaterialTransferObject) data.getPayload();
        setDocNum(materialTransferObject.getIdoc()); // set the document no. for all logging
        logInfo(LOGGER, "Material inbound integration activity start.");
        logInfo(LOGGER, "SCP");

        for (Material materialObject : materialTransferObject.getMaterials()) {
            // 1. material number
            // RM:02/ENE/2023--Accedemos al segmento MATNR y obtenemos el materialNo
            String materialNo = validate(true, materialObject.getPartNumber(), String.class, "Material Number");
            setObjectsProcessed("Material Number: " + materialNo); // for inbound event log details

            // Going to our mapping list to retrieve the correct UoM value to be stored in PS database.
            // String psUoM = IntegrationGatewayHelper.erpUnitOfMeasureToMesUoM(materialObject.getUnitOfMeasure());
            // if (StringUtils.isEmpty(psUoM)) {
            // LOGGER.error("The received value from ERP '" + materialObject.getUnitOfMeasure() + "' is not mapped in
            // our mapping list");
            // addError(this.getClass().getName(),
            // "UoM of the material object ('" + materialObject.getUnitOfMeasure() + "') can not be found in the mapping
            // list\n");
            // psUoM = materialObject.getUnitOfMeasure();
            // }

            // 2. unit of measure
            // RM:02/ENE/2023--Accedemos al segmento MEINS y obtenemos la Unidad de medida
            UnitOfMeasure uom = validate(true, materialObject.getUnitOfMeasure(), UnitOfMeasure.class, "Unit of Measure");

            // 3. material type mapping - from a list - maybe calculated differently on your project
            String materialTyp = IntegrationGatewayHelper
                    .CustomerErpMaterialTypeToMesType(validate(true, materialObject.getMaterialType(), String.class, "Material Type"));
            Long matTypChoice = null;
            if (StringUtils.isBlank(materialTyp)) { // ERP material type should map to an FTPS material type
                logError(LOGGER, "Material Type mapping does not exist in the system");
                addError(this.getClass().getName(), "Material Type mapping does not exist in the system\n");
            } else {
                // RM:02/ENE/2023--Si el material existe en la Choice List managment del FTPC\Process
                // Designer\Forms\mes_ChoiceListManager,
                // lo meto en la variable matTypChoice
                matTypChoice = MESChoiceListHelper.getChoiceElement("MaterialType", materialTyp).getValue();
            }

            // 4. planned potency - 100% as a default from configuration - maybe calculated differently on your project
            MeasuredValue plannedPotency = MESConfiguration.getMESConfiguration().getMeasuredValue("eig_MaterialPlannedPotency",
                    IntegrationGatewayHelper.getMeasuredValue("100 %"), "Default planned potency for EIG created materials");

            // 5. material description
            String description = null;
            if (materialObject.getMaterialTexts() != null && !materialObject.getMaterialTexts().isEmpty()) {
                for (MaterialText materialText : materialObject.getMaterialTexts()) { // 1st in the list
                    validate(true, materialText.getLanguageCode(), String.class, "Language code");
                    // rdominguez: SYNTHON customization --> validating received language ISO code is 'US' (for empty
                    // codes
                    // validation is not enabled).
                    if (LOCALE_ENG.getCountry().equals(materialText.getLanguageCode())) {
                        description = validate(true, materialText.getDescription(), String.class, "Material Description");
                        break;
                    } else {
                        logWarn(LOGGER, "Ignoring language code '" + materialText.getLanguageCode() + "' (SPRA_ISO).\n");
                    }
                    }
                }
                if (description == null) {
                    addError(this.getClass().getName(), "Language code (SPRA_ISO) received does not match with expected\n");
                    LOGGER.error("Language code (SPRA_ISO) received does not match with expected");
                }

            // 6. Weigh type - AuxiliarySubstance as a default from configuration - maybe different on your project
            // RM:02/ENE/2023--Declaro la variable weighType como un elemento de la lista FTPC\Process
                // Designer\Forms\mes_ChoiceListManager
                IMESChoiceElement weighType;

                if (materialTyp.equals("AuxiliarySubstance")) {
                    // RM:02/ENE/2023-- Si materialTyp es AuxiliarySubstance, declaro la variable weighType como el
                    // elemento de la lista
                    // FTPC\Process Designer\Forms\mes_ChoiceListManager\WeighingType
                    weighType = MESChoiceListHelper.getChoiceElement("WeighingType", "ActiveSubstance");
                } else {
                    // RM:02/ENE/2023-- Si materialTyp NO es AuxiliarySubstance, declaro la variable weighType como el
                    // elemento de la lista
                    // FTPC\Process Designer\Forms\mes_ChoiceListManager\WeighingType
                    weighType = MESChoiceListHelper.getChoiceElement("WeighingType", "AuxiliarySubstance");
                }
                logInfo(LOGGER, "weighType: " + weighType);

            // 7. Default Weigh mode - NetWeighing as a default from configuration - maybe different on your project
                IMESChoiceElement weighingMode = MESChoiceListHelper.getChoiceElement("WeighingMethod", MESConfiguration.getMESConfiguration()
                    .getString("eig_MaterialDefaultWeighingMethod", "NetWeighing", "The default weighing method of EIG created materials"));

            // 8. Default Allowed Weigh mode - NetWeighing as a default from configuration - maybe different on your
            // project
            IMESChoiceElement weighingAllowed = MESChoiceListHelper.getChoiceElement("WeighingMethod", MESConfiguration.getMESConfiguration()
                    .getString("eig_MaterialDefaultWeighingMethod", "NetWeighing", "The default weighing method of EIG created materials"));

            // UDAs
            HashMap<String, Object> udas = new HashMap<String, Object>();

            udas.put("X_UnitOfMeasure", uom);
            udas.put("X_materialType", matTypChoice);
            udas.put("X_plannedPotency", plannedPotency);
            udas.put("X_weighingType", weighType.getValue());
            udas.put("X_defaultWeighingMethod", weighingMode.getValue());
            udas.put("X_allowedWeighingMethods", weighingAllowed.getValue());

            // 9. Default material custom UoM --> only first in the list will be processed

            MeasuredValue convFactor = null;

            Double Double_convFactor = null;
            Double Double_convFactorDenominator = null;
            MeasuredValue RealconvFactor = null;

            MeasuredValue convOffset = MeasuredValueUtilities.createMV("0");
            UnitOfMeasure targetUoM = null;

            if (uom != null) {
                for (MaterialUnitOfMeasure materialUnit : materialObject.getMaterialUnits()) {
                    // Busco en los alternative UOM que no estén en la lista BlackList que está en el ProcessDessigner
                    if (!IntegrationGatewayHelper.isUoMMaterialUnitInBlackList(materialUnit.getAlternativeUnitMeasure())
                            && !materialUnit.getAlternativeUnitMeasure().equals(uom.getSymbol())) {
                        if (!StringUtils.isEmpty(materialUnit.getNumeratorBaseUnits())) {
                            convFactor = MeasuredValueUtilities.createMV(materialUnit.getNumeratorBaseUnits());
                            // Double_convFactor = Double.parseDouble(materialUnit.getNumeratorBaseUnits());
                            Double_convFactor = null;
                            if (!StringUtils.isEmpty(materialUnit.getAlternativeUnitMeasure()) && convFactor != null) {
                                targetUoM = validate(true, materialUnit.getAlternativeUnitMeasure(), UnitOfMeasure.class, "Unit of Measure");
                                // Double_convFactorDenominator =
                                // Double.parseDouble(materialUnit.getDenominatorBaseUnits());
                                Double_convFactorDenominator = null;
                                // RD:01/02/2022: el verdadero factor de conversion se obtiene dividiendo UMREZ / UMREN
                                // RealconvFactor = MeasuredValueUtilities.createMV(Double.toString(Double_convFactor /
                                // Double_convFactorDenominator));
                                RealconvFactor = null;
                            }
                        }

                        break;
                    } else {
                        logInfo(LOGGER, "Ignoring value '" + materialUnit.getAlternativeUnitMeasure()
                                + "' as target unit measure because this value is blacklisted or is same as UoM '" + uom.getSymbol() + "'.");
                        }

                    }
                }
            // 9. Conversion Unidades/Caja y Unidades/Palet
//            for (MaterialUnitOfMeasure materialUnit : materialObject.getMaterialUnits()) {
//                if (CONVERSION_FACTOR_CAJA.equals(materialUnit.getTargetUnitMeasure()))
//                    udas.put("ct_conversion_box", Double.parseDouble(materialUnit.getConversionFactor()));
//                else if (CONVERSION_FACTOR_PALLET.equals(materialUnit.getTargetUnitMeasure()))
//                    udas.put("ct_conversion_pallet", Double.parseDouble(materialUnit.getConversionFactor()));
//            }

            udas.put("X_ConvFactor", null);
            udas.put("X_ConvTargetUoM", null);
            udas.put("X_ConvOffset", null);
            udas.put("X_ConvSourceUoM", null);
            udas.put("X_Category_1", "");// RM: Se pone vacia porque tienen que procesar todos los materiales
            udas.put("X_erp01", materialObject.getMaterialGroup());// RM: En esta UDA va el material group para que se
                                                                   // muestre en el PMC
            // udas.put("X_Category_2", materialObject.getGroupOfArticlesDescription());
            udas.put("X_erp10", materialObject.getContainerRequirement());
            udas.put("X_erp02", materialObject.getTemperatureConditionsIndicator());
            udas.put("X_erp03", materialObject.getTotalShelfLife());
            udas.put("X_erp04", materialObject.getMaterialStatus());
            
            // ***********************11. PACKAGING LEVEL*******************************************
            //
            // RM:02/ENE/2023--Como solo se analizaran los finished goods, se comprueba que la variable matTypChoice sea
            // 50, que es su valor predefinido en la lista
            // FTPC\Process Designer\Forms\mes_ChoiceListManager\MaterialType

            if (matTypChoice == Long.parseLong(MaterialTypeChoice)) {
                udas.put("X_packagingLevel0", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Piece").getValue());

                // RM: Inicilizar variables para numerador y denominador del packaging Level
                int caso = 0;
                boolean hayBox = false;
                boolean hayPallet = false;
                boolean hayBottle = false;
                Double pieceNumerator = 0.0;
                Double pieceDenominator = 0.0;
                Double blisterNumerator = 0.0;
                Double blisterDenominator = 0.0;
                Double pacNumerator = 0.0;
                Double pacDenominator = 0.0;
                Double boxNumerator = 0.0;
                Double boxDenominator = 0.0;
                Double palletNumerator = 0.0;
                Double palletDenominator = 0.0;
                Double bottleNumerator = 0.0;
                Double bottleDenominator = 0.0;

                for (MaterialAdditionalData MaterialAddData : materialObject.getMaterialAdditionalData()) {
                    String PackType = MaterialAddData.getMaPackagingType();
                    if (PackType != null) {
                    if (PackType.toLowerCase().contains("blister")) {
                        caso = 1;
                    } else if (PackType.toLowerCase().contains("can")) {
                        caso = 2;
                    }
                    else if (PackType.toLowerCase().contains("sachets")) {
                        caso = 3;
                    }
                    }else
                    	{
                    	addError(this.getClass().getName(), "This material: " + materialObject.getPartNumber() + " don't have PackagingType. "
                    			+ "Please, check MA_PACKAGING_TYPE field in MATMAS idoc." );
                    	}
                    }
                for (MaterialUnitOfMeasure MaterialUnit : materialObject.getMaterialUnits()) {
                    if (IntegrationGatewayHelper.isUoMMaterialUnitInBlackList(MaterialUnit.getAlternativeUnitMeasure())
                            && MaterialUnit.getAlternativeUnitMeasure().equals(uom.getSymbol())) {
                        logInfo(LOGGER, "Ignoring value '" + MaterialUnit.getAlternativeUnitMeasure()
                                + "' as target unit measure because this value is blacklisted or is same as UoM '" + uom.getSymbol() + "'.");
                    } else {
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("AU")) // RM: Piece
                        {
                            pieceNumerator = Double.parseDouble(MaterialUnit.getNumeratorBaseUnits());
                            pieceDenominator = Double.parseDouble(MaterialUnit.getDenominatorBaseUnits());
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("BL")) // RM: Blister
                        {
                            blisterNumerator = Double.parseDouble(MaterialUnit.getNumeratorBaseUnits());
                            blisterDenominator = Double.parseDouble(MaterialUnit.getDenominatorBaseUnits());
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pac")) // RM: Pack
                        {
                            pacNumerator = Double.parseDouble(MaterialUnit.getNumeratorBaseUnits());
                            pacDenominator = Double.parseDouble(MaterialUnit.getDenominatorBaseUnits());
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box"))// RM: Case
                        {
                            hayBox = true;
                            boxNumerator = Double.parseDouble(MaterialUnit.getNumeratorBaseUnits());
                            boxDenominator = Double.parseDouble(MaterialUnit.getDenominatorBaseUnits());

                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) // RM: Pallet
                        {
                            hayPallet = true;
                            palletNumerator = Double.parseDouble(MaterialUnit.getNumeratorBaseUnits());
                            palletDenominator = Double.parseDouble(MaterialUnit.getDenominatorBaseUnits());
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("CAN")) // RM: Bottle
                        {
                            hayBottle = true;
                            bottleNumerator = Double.parseDouble(MaterialUnit.getNumeratorBaseUnits());
                            bottleDenominator = Double.parseDouble(MaterialUnit.getDenominatorBaseUnits());
                        }
                    }
            }
            if (caso == 1) // RM y AR: Hay Blister
            {
                    for (MaterialUnitOfMeasure MaterialUnit : materialObject.getMaterialUnits()) {
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("BL")) {
                            udas.put("X_packagingLevel1", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Blister").getValue());
                            udas.put("X_packagingLevelContent1", pieceDenominator / blisterDenominator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pac")) {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pack").getValue());
                            udas.put("X_packagingLevelContent2", blisterDenominator / pacDenominator);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box")) & (hayBox == true & hayPallet == true)) {
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent3", boxNumerator / pacNumerator);
                            udas.put("X_packagingLevel4", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent4", palletNumerator / boxNumerator);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box")) & (hayBox == true & hayPallet == false)) {
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent3", boxNumerator / pacNumerator);
                        }
                        if (hayPallet == false & hayBox == false) {
                            udas.put("X_packagingLevel3", null);
                            udas.put("X_packagingLevelContent3", null);
                            udas.put("X_packagingLevel4", null);
                            udas.put("X_packagingLevelContent4", null);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) & (hayPallet == true & hayBox == false)) {
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent3", "1");
                            udas.put("X_packagingLevel4", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent4", palletNumerator / boxNumerator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) {
                            udas.put("X_packagingLevel4", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent4", palletNumerator / boxNumerator);
                        }
                    }
                } else if (caso == 2) // RM y AR: Hay Bottle
                {
                    for (MaterialUnitOfMeasure MaterialUnit : materialObject.getMaterialUnits()) {
                        if (hayBottle == true && MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("CAN")) {
                            udas.put("X_packagingLevel1", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Bottle").getValue());
                            udas.put("X_packagingLevelContent1", pieceDenominator / bottleDenominator);
                        }
                        if (hayBottle == false) {
                            udas.put("X_packagingLevel1", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Bottle").getValue());
                            udas.put("X_packagingLevelContent1", pieceDenominator / pacDenominator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pac") && hayBottle == true) {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pack").getValue());
                            udas.put("X_packagingLevelContent2", bottleDenominator / pacDenominator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pac") && hayBottle == false) {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pack").getValue());
                            udas.put("X_packagingLevelContent2", pacNumerator / pacDenominator);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box")) & (hayBox == true & hayPallet == true)) {
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent3", boxNumerator / pacNumerator);
                            udas.put("X_packagingLevel4", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent4", palletNumerator / boxNumerator);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box")) & (hayBox == true & hayPallet == false)) {
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent3", boxNumerator / pacNumerator);
                        }
                        if (hayPallet == false & hayBox == false) {
                            udas.put("X_packagingLevel3", null);
                            udas.put("X_packagingLevelContent3", null);
                            udas.put("X_packagingLevel4", null);
                            udas.put("X_packagingLevelContent4", null);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) & (hayPallet == true & hayBox == false)) {
                            udas.put("X_packagingLevel3", null);
                            udas.put("X_packagingLevelContent3", null);
                            udas.put("X_packagingLevel4", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent4", palletNumerator / boxNumerator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) {
                            udas.put("X_packagingLevel4", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent4", palletNumerator / boxNumerator);
                        }
                    }
                }

                // RM y AR: Falta por definir que pasa si el siguiente nivel de Packaging es Sachet
                else if (caso == 3) // RM y AR: 
                {
                    for (MaterialUnitOfMeasure MaterialUnit : materialObject.getMaterialUnits()) {
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pac"))
                        {
                            udas.put("X_packagingLevel1", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pack").getValue());
                            udas.put("X_packagingLevelContent1", pieceDenominator / pacDenominator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box"))
                        {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent2", boxNumerator / pacNumerator);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box")) & (hayBox == true & hayPallet == true)) {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent2", boxNumerator / pacNumerator);
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent3", palletNumerator / boxNumerator);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("box")) & (hayBox == true & hayPallet == false)) {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent2", boxNumerator / pacNumerator);
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent3", "1");

                        }
                        if (hayPallet == false & hayBox == false) {
                            udas.put("X_packagingLevel2", null);
                            udas.put("X_packagingLevelContent2", null);
                            udas.put("X_packagingLevel3", null);
                            udas.put("X_packagingLevelContent3", null);
                        }
                        if ((MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) & (hayPallet == true & hayBox == false)) {
                            udas.put("X_packagingLevel2", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Case").getValue());
                            udas.put("X_packagingLevelContent2", "1");
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent3", palletNumerator / boxNumerator);
                        }
                        if (MaterialUnit.getAlternativeUnitMeasure().equalsIgnoreCase("pal")) {
                            udas.put("X_packagingLevel3", MESChoiceListHelper.getChoiceElement("PackagingLevel", "Pallet").getValue());
                            udas.put("X_packagingLevelContent3", palletNumerator / boxNumerator);
                        }
                    }
                }

            } else {
                logInfo(LOGGER, "Material type =" + materialTyp + ".Packaging levels is only available for Finished Goods ");
                udas.put("X_packagingLevel0", null);
                udas.put("X_packagingLevel1", null);
                udas.put("X_packagingLevelContent1", null);
                udas.put("X_packagingLevel2", null);
                udas.put("X_packagingLevelContent2", null);
                udas.put("X_packagingLevel3", null);
                udas.put("X_packagingLevelContent3", null);
                udas.put("X_packagingLevel4", null);
                udas.put("X_packagingLevelContent4", null);
            }


            // 10. Plant
            // RM:03/ENE/2023-- Comprueba que el codigo de planta es el de SYNTHON ESP (2100), aunque esto ya se hace el
            // principio, no se porque esta aqui tambien.
                for (MaterialPlant MaterialPlant : materialObject.getMaterialPlants()) {
                    String Plant = validate(true, MaterialPlant.getPlant(), String.class, "Plant");
                    if (Plant != null && !StringUtils.equals(Plant, SynthonPlantCode)) {
                        addError(this.getClass().getName(), "Plant code is incorrect.\n");
                    }
                    udas.put("X_erp05", MaterialPlant.getPlant());
                    udas.put("X_erp06", MaterialPlant.getSpecificMaterialStatus());
                    udas.put("ct_MaterialGrouping",MaterialPlant.getSpecificMaterialGrouping());
                }
            
            // 11. AdditionalData
            String SignalWord = "";
            String GHSSymbols = "";
            String HazardCodes = "";
            String PrecautionaryCodes = "";

            for (MaterialAdditionalData MaterialAdditionalData : materialObject.getMaterialAdditionalData()) {

                udas.put("X_erp07", MaterialAdditionalData.getTemperatureConditionDescription());
                udas.put("X_erp08", MaterialAdditionalData.getContainerRequirementDescription());
                udas.put("X_erp09", MaterialAdditionalData.getOeb());
                udas.put("ct_Country", MaterialAdditionalData.getMaCountry());
                udas.put("ct_Strength", MaterialAdditionalData.getMaStrength());
                udas.put("ct_Packaging_Type", MaterialAdditionalData.getMaPackagingType());
                
                SignalWord = MaterialAdditionalData.getSignalWord();
                GHSSymbols = MaterialAdditionalData.getGhsSymbols();

                HazardCodes = MaterialAdditionalData.getHazardCodes();
                PrecautionaryCodes = MaterialAdditionalData.getPrecautionaryCodes();

                // Por si acaso mandan datos nulos
                if (SignalWord == null)
                    SignalWord = "";
                if (GHSSymbols == null)
                    GHSSymbols = "";
                if (HazardCodes == null)
                    HazardCodes = "";
                if (PrecautionaryCodes == null)
                    PrecautionaryCodes = "";
            }

            // Signal word possibilities: 10 = Danger, 20 = Warning, 30 = None
            switch (StringUtils.upperCase(SignalWord)) {
            case "DANGER":
                udas.put("X_GHSSignalWordType", 10);
                break;
            case "WARNING":
                udas.put("X_GHSSignalWordType", 20);
                break;
            default:
                udas.put("X_GHSSignalWordType", 30);
            }
            
            // HazardGHS possiblities (in order):
            // _HazardGHS_explos,_HazardGHS_flamme,_HazardGHS_rondflam,_HazardGHS_bottle,_HazardGHS_acid_red,_HazardGHS_skull,_HazardGHS_aquatic_pollut_red,_HazardGHS_silhouete,_HazardGHS_exclam
            String HazardGHS_String = "";

            // Como los datos del Hazard vienen separados por punto y coma, tengo que ir anotandolos
            StringBuilder delimiter = new StringBuilder(1);
            delimiter.append(";");

            // Cuento los token que hay separados por punto y coma
            StringTokenizer HazardGHSTokens = new StringTokenizer(GHSSymbols, delimiter.toString());

            // Si solo han pasado 1parámetro (sin ningún punto y coma), lo asigno directamente
            if (HazardGHSTokens.countTokens() <= 1) {
                HazardGHS_String = validateHazardGHS(GHSSymbols);
            }

            // Si han pasado más de 1 parámetro separado por punto y coma, los recorro
            else {
                while (HazardGHSTokens.countTokens() > 0) {
                    String HazardGHS_Loop = HazardGHSTokens.nextToken();
                    HazardGHS_String = HazardGHS_String + validateHazardGHS(HazardGHS_Loop);
                    HazardGHS_String = HazardGHS_String + ",";
                }
                }
            udas.put("X_GHSImageNames", HazardGHS_String);
            
            if (description != null) {
                udas.put("X_shortDescription", description.substring(0, Math.min(description.length(), 50)));
            }

            if (getErrors() == null || getErrors().size() == 0) {
                final IInboundMessageService ims = ServiceFactory.getService(IInboundMessageService.class);
                ims.createERPMaterial(materialNo, description, udas, HazardCodes, PrecautionaryCodes);
            } else
                logError(LOGGER,
                        MessageFormat.format(resources.getString("ct_eihub_error.generic_error"), new Object[] { materialTransferObject.getIdoc() }));
        }
    }

    private String validateHazardGHS(final String ValidationString) throws DatasweepException {

        String Return_String = "";

        switch (ValidationString) {

        case "GHS01":
            Return_String = "_HazardGHS_explos";
            break;
        case "GHS02":
            Return_String = "_HazardGHS_flamme";
            break;
        case "GHS03":
            Return_String = "_HazardGHS_rondflam";
            break;
        case "GHS04":
            Return_String = "_HazardGHS_bottle";
            break;
        case "GHS05":
            Return_String = "_HazardGHS_acid_red";
            break;
        case "GHS06":
            Return_String = "_HazardGHS_skull";
            break;
        case "GHS07":
            Return_String = "_HazardGHS_exclam";
            break;
        case "GHS08":
            Return_String = "_HazardGHS_silhouete";
            break;
        case "GHS09":
            Return_String = "_HazardGHS_aquatic_pollut_red";
            break;
        }

        return Return_String;
    }

    @Override
    public Long getObjectType() {
        return new Long(IObjectTypes.TYPE_PART);
    }

}
