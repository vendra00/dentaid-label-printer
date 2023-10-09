package com.rockwell.custmes.apps.recipeeditor.impl.hierarchy.check;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Part;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.HierarchyLevelAndStructureTypeTraits;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.IMasterRecipeStructureModel;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.IMaterialParameter;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.IParameter;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.IRecipeElement;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.IRecipeStructureModel;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.check.AbstractRecipeStructureSingleParameterCheck;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.check.fw.CheckMessageLevel;
import com.rockwell.mes.apps.recipeeditor.ifc.hierarchy.check.fw.ICheckMessagesModel;
import com.rockwell.mes.commons.base.ifc.i18n.I18nMessageUtility;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAPart;
import com.rockwell.mes.services.s88.ifc.HierarchyLevel;
import com.rockwell.mes.services.s88.ifc.HierarchyLevelHelper.LetterCase;
import com.rockwell.mes.services.s88.ifc.recipe.IMESERPBom;
import com.rockwell.mes.services.s88.ifc.recipe.IMESERPBomItem;
import com.rockwell.mes.services.s88.ifc.recipe.ParameterType;
import com.rockwell.mes.services.s88.ifc.recipe.ParameterType.ParameterSubType;

 /**
 * This check compares the contents of the field "MYC_ plannedPotency2" of the
 * material parameter with the value of the ERP BOM item. If the value in the
 * ERP BOM item is not empty, then both values must be identical. Otherwise it
 * is an error. <br>
 * <br>
 * This check is not enabled in standard FTPS, you must
 * <ul>
 * <li>add it to the list of recipe-structure-checks (i.e.
 * myc_recipeStructureChecks),
 * <li>configure the configuration key
 * 'LibraryHolder/apps-recipeeditor-impl.jar/RecipeStructureChecks' using this
 * list in your application (i.e. 'myc_DefaultApplication'),
 * <li>register this class in an XML configuration file (i.e.
 * 'myc-recipeeditor.xml').
 * <li>register this XML configuration file in the central configuration file
 * (i.e. 'myc-config.xml') and
 * <li>configure the configuration key
 * 'LibraryHolder/commons-base-ifc.jar/ServicesConfigFile' using this
 * configuration file in your application (i.e. 'myc_DefaultApplication').
 * <ul>
 */
public class mycERPBomItemSecondPotencyCheck extends AbstractRecipeStructureSingleParameterCheck {

    /** The Logger */
    private static final Log LOGGER = LogFactory.getLog(mycERPBomItemSecondPotencyCheck.class);

    /** The message pack with localized messages. (Should be moved to central class). */
    private static final String MSG_PACK = "myc_ui_RecipeDesigner_Checks";

    /** The IDs of all localized messages (message pack 'cust_ui_RecipeDesigner_Checks' for this check. */
    private static final String //
    ERP_BOMITEM_CHECK_SECOND_POTENCY_DIFFERENT_MSG_ID = "ERPBOMItemSecondPotencyCheck_ValuesDifferent_ErrorMsg";

    /** The IDs of all customer-specific messages-types for this check. */
    private static final String //
    ERP_BOMITEM_CHECK_SECOND_POTENCY_DIFFERENT = "0001CUST";

    // Example: "The {1} has an unsuitable material input parameter, since its
    // second potency do not correspond to the those of the {2} position of the
    // ERP BOM; required potency: {5}, current potency: {4}. ({0})"

    /** Stores the ERP BOM to improve performance. */
    private IMESERPBom erpBom;

    /** The column- / property-name of the second potency. */
    private static final String COLUMN_NAME_SECOND_POTENCY = "MYC_plannedPotency2";

    /** The names of the properties to observe. */
    private static final List<String> PROPERTY_NAMES_TO_OBSERVE = Arrays.asList(//
            IMaterialParameter.PROPERTY_MFC_POSITION, //
            COLUMN_NAME_SECOND_POTENCY //
            );

    /** Constructor, exists only for debugging. */
    public mycERPBomItemSecondPotencyCheck() {
        super();
    }

    /** Execute check only for recipes with ERP BOM. */
    @Override
    public boolean isApplicableForRecipeStructureModel(IRecipeStructureModel recipeStructureModel) {
        if (!recipeStructureModel.isMasterRecipe()) {
            return false;
        }

        return ((IMasterRecipeStructureModel) getRecipeStructureModel()).getERPBom() != null;
    }

    /** Execute check only for material parameters. */
    @Override
    protected boolean isRelevantParameterType(ParameterType parameterType) {
        return parameterType == ParameterType.MATERIAL;
    }

    /** Execute check only on phase level. */
    @Override
    protected boolean isRelevantRecipeElement(IRecipeElement recipeElement) {
        // check only on phase level
        return recipeElement.getLevel().equals(HierarchyLevel.PHASE);
    }

    /** Execute check only for the changes of BOM-position or second potency. */
    @Override
    protected boolean isRelevantPropertyChange(PropertyChangeEvent event) {
        String propName = event.getPropertyName();
        boolean check = PROPERTY_NAMES_TO_OBSERVE.contains(propName);
        return check;
    }

    /** Set local attribute 'erpBOM'. */
    @Override
    public void setRecipeStructureModel(IRecipeStructureModel recipeStructureModel) {
        super.setRecipeStructureModel(recipeStructureModel);
        if (recipeStructureModel.isMasterRecipe()) {
            this.erpBom = ((IMasterRecipeStructureModel) getRecipeStructureModel()).getERPBom();
        }
    }

    /**
     * Check the material parameter, if
     * <ul>
     * <li>it has a ERP BOM item on same position
     * <li>the ERP BOM item on same position has same second potency
     * </ul>
     * 
     * @param parameter The parameter to check
     */
    @Override
    protected void checkParameter(IParameter parameter) {
        IMaterialParameter materialParameter = (IMaterialParameter) parameter;
        if (!materialParameter.getSubType().equals(ParameterSubType.IN)) {
            // only check input parameters her.
            return;
        }

        String mfcPosition = materialParameter.getMFCPosition();
        if (StringUtils.isBlank(mfcPosition)) {
            // local material is allowed and will not checked against ERP BOM
            return;
        }

        IMESERPBomItem aERPBomItem = erpBom.getItem(mfcPosition);
        if (aERPBomItem == null) {
            // not necessary to add an error message about missing ERP BOM item,
            // because this is already done by 'ERPBomItemPhaseLevelCheck'.
            return;
        }

        if (isConsistent(aERPBomItem, materialParameter)) {
            Part aERPBomPart = aERPBomItem.getMaterial();
            LOGGER.debug("bom position [OK]:" + mfcPosition + " has one compatible BOM Item assigned. ["
                    + aERPBomPart.getPartNumber() + "]");
        }
    }

    /**
     * Checks, if the attribute "MYC_plannedPotency2" of the material parameter
     * and the ERP BOM item are consistent.
     * 
     * @param aERPBomItem a ERP BOM item
     * @param materialParameter material parameter
     * @return <code>true</code> , if the attributes 'MYC_plannedPotency2' are
     *         consistent, else <code> false</code>
     */
    protected boolean isConsistent(IMESERPBomItem aERPBomItem, IMaterialParameter materialParameter) {
        Object originalValue = aERPBomItem.getATRow().getValue(COLUMN_NAME_SECOND_POTENCY);
        if (originalValue == null) {
            return true; // no check necessary
        }

        // same value expected
        Object currentValue = materialParameter.getInstance().getATRow().getValue(COLUMN_NAME_SECOND_POTENCY);
        boolean valid = ObjectUtils.equals(originalValue, currentValue);
        if (valid) {
            return true; // values identically
        }

        // values different => error message
        IRecipeElement recipeElement = materialParameter.getParent();
        String levelNameLowerCase = HierarchyLevelAndStructureTypeTraits.getLocalizedName(recipeElement.getLevel(), LetterCase.LOWER_CASE, true);
        Part aERPBomPart = aERPBomItem.getMaterial();
        String shortDescription = MESNamedUDAPart.getShortDescription(aERPBomPart);
        String dispShortDescr = (shortDescription == null) ? "" : shortDescription;
        String dispCurrentValue = (currentValue == null) ? "" : currentValue.toString();
        String[] msgArgs = { recipeElement.getName(), levelNameLowerCase, aERPBomItem.getPosition(), //
                dispShortDescr, dispCurrentValue, originalValue.toString() };
        addErrorMessage(materialParameter, ERP_BOMITEM_CHECK_SECOND_POTENCY_DIFFERENT,
                ERP_BOMITEM_CHECK_SECOND_POTENCY_DIFFERENT_MSG_ID, msgArgs);
        return false;
    }

    /**
     * Add an error message to the message model
     * 
     * @param materialParameter material parameter
     * @param messageTypeString the type of the message
     * @param messageIdString the message string
     * @param msgArgs the message arguments
     */
    protected void addErrorMessage(IMaterialParameter materialParameter, String messageTypeString,
            String messageIdString, Object[] msgArgs) {
        String i18nMsg = I18nMessageUtility.getLocalizedMessage(MSG_PACK, messageIdString, msgArgs);
        ICheckMessagesModel messages = getCheckMessagesModel();
        messages.addParameterRelatedMessage(i18nMsg, messageTypeString, CheckMessageLevel.ERROR, //
                materialParameter, null, this);
    }

}