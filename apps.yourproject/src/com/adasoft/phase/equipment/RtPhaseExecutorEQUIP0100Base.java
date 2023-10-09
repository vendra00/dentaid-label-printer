package com.adasoft.phase.equipment;

import com.rockwell.library.ftpc.AppMode;
import com.rockwell.library.ftpc.guice.logger.InjectLogger;
import com.rockwell.library.ftps.ebr.common.PhaseExecutorSwing;
import com.rockwell.library.ftps.ebr.common.PhaseViewSwing;
import com.rockwell.library.ftps.ebr.phase.PhaseControlRegistry;
import com.rockwell.mes.apps.ebr.ifc.phase.IPhaseCompleter;
import com.rockwell.mes.commons.parameter.bool.MESParamBoolean0100;
import com.rockwell.mes.commons.parameter.exceptiondef.MESParamExceptionDef0300;
import com.rockwell.mes.commons.parameter.instruction.MESParamInstruction0300;
import com.rockwell.mes.commons.parameter.uomdefinition.MESParamUoMDefinition0100;
import com.rockwell.mes.parameter.equipmentobject.MESParamEqObject0100;
import com.rockwell.mes.parameter.phasecompletionmode.MESParamCompletionMode0100;
import com.rockwell.mes.parameter.product.excptenabledef.MESParamExcptEnableDef0200;
import com.rockwell.mes.services.s88.ifc.execution.IMESRtPhase;
import com.rockwell.mes.services.s88.ifc.recipe.IMESPhase;
import com.rockwell.mes.services.s88.ifc.recipe.IS88ProcessParameterBundle;
import com.rockwell.ssb.ftps.parameter.mastbundlident0100.MESParamMastBundlIdent;
import com.adasoft.phase.equipment.parameter.MESParamDecimPropDef0100;
import com.adasoft.phase.equipment.parameter.MESParamLongPropDef0100;
import com.adasoft.phase.equipment.parameter.MESParamMValPropDef0100;
import com.adasoft.phase.equipment.parameter.MESParamMulPropsHide;
import com.datasweep.compatibility.client.ActivitySetStep;
import com.datasweep.compatibility.client.Response;

import com.google.common.collect.Lists;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.apache.commons.logging.Log;

public class RtPhaseExecutorEQUIP0100Base extends PhaseExecutorSwing {
	
	public static final String PHASE_NAME = "Set Multiple Equipment Runtime Properties (AD) [1.0]";
    private final Injector injector = this.createInjector();
    @InjectLogger
    private Log log;
    
    @Inject
    protected EventBus eventBus;
    
    @Inject
    protected ControlProvider controlProvider;
    
    @Inject
    protected PhaseControlRegistry phaseControlRegistry;
    
    @Inject
    protected PhaseUIBeanSetMultRtProps phaseBean;
    
    public static final String PARAM_INSTRUCTION = "Instruction";
    public static final String PARAM_IDENTIFIEDEQUIPMENTENTITY = "Identified equipment entity";
    public static final String PARAM_MISMATCHINGPROPERTY = "Mismatching property";
    public static final String PARAM_MODE = "Mode";
    public static final String PARAM_HIDDENPHASECONFIGURATION = "Hidden phase configuration";
    public static final String PARAM_BUNDLE_DECIMAL_DECIMAL = "Decimal";
    public static final String PARAM_BUNDLE_DECIMAL_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_DECIMAL_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_DECIMAL_LIMITCONFIGURATION = "Limit configuration";
    public static final String PARAM_BUNDLE_LONG_LONG = "Long";
    public static final String PARAM_BUNDLE_LONG_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_LONG_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_LONG_LIMITCONFIGURATION = "Limit configuration";
    public static final String PARAM_BUNDLE_MEASUREDVALUE_MEASUREDVALUE = "Measured Value";
    public static final String PARAM_BUNDLE_MEASUREDVALUE_UNITOFMEASURE = "Unit of Measure";
    public static final String PARAM_BUNDLE_MEASUREDVALUE_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_MEASUREDVALUE_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_MEASUREDVALUE_LIMITCONFIGURATION = "Limit configuration";
    public static final String PARAM_BUNDLE_BOOLEAN_BOOLEAN = "Boolean";
    public static final String PARAM_BUNDLE_BOOLEAN_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_BOOLEAN_BOOLEANOPTIONS = "Boolean options";
    public static final String PARAM_BUNDLE_BOOLEAN_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_BOOLEAN_LIMITCONFIGURATION = "Limit configuration";
    public static final String PARAM_BUNDLE_STRING_STRING = "String";
    public static final String PARAM_BUNDLE_STRING_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_STRING_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_STRING_LIMITCONFIGURATION = "Limit configuration";
    public static final String PARAM_BUNDLE_DURATION_DURATION = "Duration";
    public static final String PARAM_BUNDLE_DURATION_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_DURATION_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_DURATION_LIMITCONFIGURATION = "Limit configuration";
    public static final String PARAM_BUNDLE_DATETIME_DATETIME = "Date Time";
    public static final String PARAM_BUNDLE_DATETIME_PROPERTYDEFINITION = "Property definition";
    public static final String PARAM_BUNDLE_DATETIME_ENABLECASCADEPROPERTYPROPAGATIONONASSEMBLEDCHILDENTITIES = "Enable cascade property propagation on assembled child entities";
    public static final String PARAM_BUNDLE_DATETIME_LIMITCONFIGURATION = "Limit configuration";

    private Injector createInjector() {
        List<Module> modules = Lists.newArrayList();
        if (AppMode.getMode().equals(AppMode.JUNIT_PC)) {
            modules.add(RtPhaseExecutorSetMultRtPropsModules.getJUnitModule());
        } else {
            modules.add(RtPhaseExecutorSetMultRtPropsModules.getRuntimeModule());
        }

        return Guice.createInjector((Module[])modules.toArray(new Module[modules.size()]));
    }
    
	public RtPhaseExecutorEQUIP0100Base(IPhaseCompleter inPhaseCompleter, IMESRtPhase inRtPhase) {
		super(inPhaseCompleter, inRtPhase);
        this.injector.injectMembers(this);
	}

	public RtPhaseExecutorEQUIP0100Base(IMESPhase inPhase, ActivitySetStep inStep) {
		super(inPhase, inStep);
        this.injector.injectMembers(this);
	}
	
	public MESParamInstruction0300 getInstruction() {
        return (MESParamInstruction0300)this.getProcessParameterData(MESParamInstruction0300.class, "Instruction");
    }
	
	public MESParamEqObject0100 getIdentifiedEquipmentEntity() {
        return (MESParamEqObject0100)this.getProcessParameterData(MESParamEqObject0100.class, "Identified equipment entity");
    }
	
	public MESParamExceptionDef0300 getMismatchingProperty() {
        return (MESParamExceptionDef0300)this.getProcessParameterData(MESParamExceptionDef0300.class, "Mismatching property");
    }

    public MESParamCompletionMode0100 getMode() {
        return (MESParamCompletionMode0100)this.getProcessParameterData(MESParamCompletionMode0100.class, "Mode");
    }
    
    public MESParamMulPropsHide getHiddenPhaseConfiguration() {
        return (MESParamMulPropsHide)this.getProcessParameterData(MESParamMulPropsHide.class, "Hidden phase configuration");
    }

    protected MESGeneratedRtPhaseOutputEQUIP0100 getRtPhaseOutput() {
        return (MESGeneratedRtPhaseOutputEQUIP0100)this.getRtPhase().getRtPhaseOutput();
    }
    
    public MESGeneratedRtPhaseDataEQUIP0100 getRtPhaseData() {
        return (MESGeneratedRtPhaseDataEQUIP0100)this.getRtPhase().getRtPhaseData();
    }

    public final List<MESGeneratedRtPhaseDataEQUIP0100> getAllRtPhaseData() {
        return this.getRtPhase().getAllRtPhaseData();
    }
    
    protected final MESGeneratedRtPhaseDataEQUIP0100 addRtPhaseData() {
        return (MESGeneratedRtPhaseDataEQUIP0100)this.getRtPhase().addRtPhaseData();
    }

    public MESParamMastBundlIdent getDecimalOfBundleDecimal(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamMastBundlIdent)this.getProcessParameterData(MESParamMastBundlIdent.class, parameterBundle.getInternalIdentifierOfProcessParameter("Decimal"));
    }
    
    public MESParamDecimPropDef0100 getPropertyDefinitionOfBundleDecimal(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamDecimPropDef0100)this.getProcessParameterData(MESParamDecimPropDef0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Property definition"));
    }

    public MESParamBoolean0100 getEnableCascadePropertyPropagationOnAssembledChildEntitiesOfBundleDecimal(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamBoolean0100)this.getProcessParameterData(MESParamBoolean0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Enable cascade property propagation on assembled child entities"));
    }
    
    public MESParamExcptEnableDef0200 getLimitConfigurationOfBundleDecimal(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamExcptEnableDef0200)this.getProcessParameterData(MESParamExcptEnableDef0200.class, parameterBundle.getInternalIdentifierOfProcessParameter("Limit configuration"));
    }
    
    public MESParamMastBundlIdent getLongOfBundleLong(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamMastBundlIdent)this.getProcessParameterData(MESParamMastBundlIdent.class, parameterBundle.getInternalIdentifierOfProcessParameter("Long"));
    }
    
    public MESParamLongPropDef0100 getPropertyDefinitionOfBundleLong(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamLongPropDef0100)this.getProcessParameterData(MESParamLongPropDef0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Property definition"));
    }
    
    public MESParamBoolean0100 getEnableCascadePropertyPropagationOnAssembledChildEntitiesOfBundleLong(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamBoolean0100)this.getProcessParameterData(MESParamBoolean0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Enable cascade property propagation on assembled child entities"));
    }

    public MESParamExcptEnableDef0200 getLimitConfigurationOfBundleLong(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamExcptEnableDef0200)this.getProcessParameterData(MESParamExcptEnableDef0200.class, parameterBundle.getInternalIdentifierOfProcessParameter("Limit configuration"));
    }
    
    public MESParamMastBundlIdent getMeasuredValueOfBundleMeasuredValue(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamMastBundlIdent)this.getProcessParameterData(MESParamMastBundlIdent.class, parameterBundle.getInternalIdentifierOfProcessParameter("Measured Value"));
    }
    
    public MESParamUoMDefinition0100 getUnitOfMeasureOfBundleMeasuredValue(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamUoMDefinition0100)this.getProcessParameterData(MESParamUoMDefinition0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Unit of Measure"));
    }

    public MESParamMValPropDef0100 getPropertyDefinitionOfBundleMeasuredValue(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamMValPropDef0100)this.getProcessParameterData(MESParamMValPropDef0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Property definition"));
    }

    public MESParamBoolean0100 getEnableCascadePropertyPropagationOnAssembledChildEntitiesOfBundleMeasuredValue(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamBoolean0100)this.getProcessParameterData(MESParamBoolean0100.class, parameterBundle.getInternalIdentifierOfProcessParameter("Enable cascade property propagation on assembled child entities"));
    }

    public MESParamExcptEnableDef0200 getLimitConfigurationOfBundleMeasuredValue(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamExcptEnableDef0200)this.getProcessParameterData(MESParamExcptEnableDef0200.class, parameterBundle.getInternalIdentifierOfProcessParameter("Limit configuration"));
    }
    
    public MESParamMastBundlIdent getBooleanOfBundleBoolean(IS88ProcessParameterBundle parameterBundle) {
        return (MESParamMastBundlIdent)this.getProcessParameterData(MESParamMastBundlIdent.class, parameterBundle.getInternalIdentifierOfProcessParameter("Boolean"));
    }
	

	@Override
	protected PhaseViewSwing createView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Response doPerformComplete() {
		// TODO Auto-generated method stub
		return null;
	}

}
