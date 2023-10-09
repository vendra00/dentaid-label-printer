package com.adasoft.phase.equipment.ftps.setmultrtprops0100;

import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.ui.Time;
import com.rockwell.mes.apps.ebr.ifc.phase.ui.UIConstants;
import com.rockwell.mes.apps.ebr.ifc.swing.PhaseSwingHelper;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.functional.UnitOfMeasureHelper;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.ssb.ftpc.commons.i18n.I18nMesFieldFormats;
import com.rockwell.ssb.ftpc.commons.i18n.I18nRdlPhaseSetMultRtProps0100;
import com.rockwell.ssb.ftpc.commons.list.choice.SetMultRtEqPropsChoiceListChoiceList;
import com.rockwell.ssb.ftps.parameter.booleanoptions0100.MESParamBooleanOptions;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.RtPhaseModelSetMultRtProps.ParameterBundleType;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.BooleanPropertyBundlePanel;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.DateTimePropertyBundlePanel;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.DecimalPropertyBundlePanel;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.DurationPropertyBundlePanel;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.LongPropertyBundlePanel;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.MeasuredValuePropertyBundlePanel;
import com.rockwell.ssb.ftps.phase.setmultrtprops0100.views.StringPropertyBundlePanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.apache.commons.lang3.StringUtils;

public class RtPhaseViewProperties {
    private static final double WIDTH_COLUMN_LABELID = 0.5;
    private static final double WIDTH_COLUMN_FIELD = 0.08;
    private static final double WIDTH_COLUMN_LABEL = 0.02;
    private static final int COLUMNS_NUMBER = 11;
    private static final int HEIGHTBOOLEAN = 25;
    private static final int TOP = 5;
    private static final int LEFT = 0;
    private static final int BOTTOM = 5;
    private static final int RIGHT = 0;
    private static SimpleDateFormat simpleDateFormat;
    private RtPhaseModelSetMultRtProps model;
    private GridBagLayout gridBagLayout = new GridBagLayout();
    private GridBagConstraints c = new GridBagConstraints();
    private List<RtPhaseRuntimeProperty> rtPhaseRuntimeProperties;

    protected RtPhaseViewProperties(RtPhaseModelSetMultRtProps theModel) {
        this.model = theModel;
    }

    public RtPhaseModelSetMultRtProps getModel() {
        return this.model;
    }

    protected JPanel createUI(JPanel propertiesPanel, ControlProvider controlProvider) {
        int booleans = 0;
        propertiesPanel.setLayout(this.gridBagLayout);
        this.c.insets = new Insets(5, 0, 5, 0);
        this.c.gridy = 0;
        int additionalLines = 0;
        this.createDummyLabel(propertiesPanel);
        Iterator var5 = this.model.getMapProperty().values().iterator();

        while(var5.hasNext()) {
            RtPhaseRuntimeProperty property = (RtPhaseRuntimeProperty)var5.next();
            if (!StringUtils.isEmpty(property.getPropertyIdentifier())) {
                switch (ParameterBundleType.fromIdentifier(property.getBundleIdentifier())) {
                    case RT_BIGDECIMAL_TYPE:
                        DecimalPropertyBundlePanel decimalPropBundlePanel = new DecimalPropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), decimalPropBundlePanel);
                        additionalLines += decimalPropBundlePanel.getAdditionalLines();
                        ++this.c.gridy;
                        break;
                    case RT_LONG_TYPE:
                        LongPropertyBundlePanel longPropBundlePanel = new LongPropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), longPropBundlePanel);
                        additionalLines += longPropBundlePanel.getAdditionalLines();
                        ++this.c.gridy;
                        break;
                    case RT_MEASUREDVALUE_TYPE:
                        MeasuredValuePropertyBundlePanel measuredValuePropBundlePanel = new MeasuredValuePropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), measuredValuePropBundlePanel);
                        additionalLines += measuredValuePropBundlePanel.getAdditionalLines();
                        ++this.c.gridy;
                        break;
                    case RT_BOOLEAN_TYPE:
                        Object booleanPropertyValue = property.getPropertyValue();
                        Boolean newValueBoolean = null;
                        if (booleanPropertyValue != null && booleanPropertyValue instanceof Boolean) {
                            newValueBoolean = (Boolean)booleanPropertyValue;
                            if (Boolean.TRUE.equals(newValueBoolean)) {
                                property.setPropertyValue(SetMultRtEqPropsChoiceListChoiceList.Yes.val());
                            } else if (Boolean.FALSE.equals(newValueBoolean)) {
                                property.setPropertyValue(SetMultRtEqPropsChoiceListChoiceList.No.val());
                            }
                        }

                        BooleanPropertyBundlePanel booleanPropBundlePanel = new BooleanPropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), booleanPropBundlePanel);
                        additionalLines += booleanPropBundlePanel.getAdditionalLines();
                        ++booleans;
                        ++this.c.gridy;
                        break;
                    case RT_STRING_TYPE:
                        StringPropertyBundlePanel stringPropBundlePanel = new StringPropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), stringPropBundlePanel);
                        additionalLines += stringPropBundlePanel.getAdditionalLines();
                        ++this.c.gridy;
                        break;
                    case RT_DURATION_TYPE:
                        DurationPropertyBundlePanel durationPropBundlePanel = new DurationPropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), durationPropBundlePanel);
                        additionalLines += durationPropBundlePanel.getAdditionalLines();
                        ++this.c.gridy;
                        break;
                    case RT_DATETIME_TYPE:
                        DateTimePropertyBundlePanel dateTimePropBundlePanel = new DateTimePropertyBundlePanel(propertiesPanel, this.c, this.model, property);
                        controlProvider.register(property.getUserBundleIdentifier(), dateTimePropBundlePanel);
                        additionalLines += dateTimePropBundlePanel.getAdditionalLines();
                        ++this.c.gridy;
                }
            }
        }

        int width = (int)propertiesPanel.getPreferredSize().getWidth();
        propertiesPanel.setPreferredSize(new Dimension(width, UIConstants.PHASE_WORK_AREA_SIZE.height + this.c.gridy * (int)UIConstants.LABEL_PREFERRED_SIZE.getHeight() + additionalLines * (int)UIConstants.LABEL_PREFERRED_SIZE.getHeight() + booleans * 25));
        return propertiesPanel;
    }

    private void createDummyLabel(JPanel propertiesPanel) {
        for(int i = 0; i < 11; ++i) {
            Component emptyLabel = PhaseSwingHelper.createJLabel(" ");
            this.c.fill = 2;
            ++this.c.gridx;
            if (i == 0) {
                this.c.weightx = 0.5;
            } else if (i % 2 != 0) {
                this.c.weightx = 0.08;
            } else {
                this.c.weightx = 0.02;
            }

            propertiesPanel.add(emptyLabel, this.c);
        }

        this.c.gridy = 1;
    }

    protected String setRtPropertyValueGridData(List<MESRtPhaseDataSetMultRtProps> list) {
        StringBuilder gridCompleted = new StringBuilder();
        gridCompleted.append("<html><table width=\"100%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\" style=\"float:left; \"><tr style=\"color: black\" bgcolor=\"#FFFFFF\">");
        gridCompleted.append("<th style=\"border:1px solid white\"><div style=\"width:300px;text-align: left;\">Property</div></th>");
        gridCompleted.append("<th style=\"border:1px solid white\"><div style=\"width:300px;text-align: left;\">Value</div></th></tr>");
        Map<String, RtPhaseRuntimeProperty> mapList = this.getModel().getMapProperty();
        this.rtPhaseRuntimeProperties = new ArrayList();
        Iterator var4 = list.iterator();

        while(var4.hasNext()) {
            MESRtPhaseDataSetMultRtProps data = (MESRtPhaseDataSetMultRtProps)var4.next();
            this.rtPhaseRuntimeProperties.add(mapList.get(data.getBundleId()));
        }

        this.addTable(gridCompleted, this.rtPhaseRuntimeProperties);
        gridCompleted.append("</table></html>");
        return gridCompleted.toString();
    }

    private void addTable(StringBuilder gridCompleted, Collection<RtPhaseRuntimeProperty> properties) {
        Iterator var3 = properties.iterator();

        while(var3.hasNext()) {
            RtPhaseRuntimeProperty property = (RtPhaseRuntimeProperty)var3.next();
            if (property != null) {
                if (ParameterBundleType.fromIdentifier(property.getBundleIdentifier()).equals(ParameterBundleType.RT_MEASUREDVALUE_TYPE)) {
                    MeasuredValue mv = (MeasuredValue)property.getPropertyValue();
                    if (!mv.isConvertable(UnitOfMeasureHelper.getUomBySymbol(this.getModel().getUoMBundle(property)))) {
                        property.setValid(false);
                    }
                }

                if (property.isValid()) {
                    gridCompleted.append("<tr style=\"color: white\" ><td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + (this.getModel().getShortDescriptionBundle(property).isEmpty() ? property.getUserBundleIdentifier() : this.getModel().getShortDescriptionBundle(property)) + "</td>");
                }

                if (ParameterBundleType.fromIdentifier(property.getBundleIdentifier()).equals(ParameterBundleType.RT_BIGDECIMAL_TYPE)) {
                    BigDecimal bd = (BigDecimal)property.getPropertyValue();
                    MeasuredValue mv = MeasuredValueUtilities.createMV(bd, "");
                    gridCompleted.append("<td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + mv.toString() + "</td></tr>");
                } else if (ParameterBundleType.fromIdentifier(property.getBundleIdentifier()).equals(ParameterBundleType.RT_BOOLEAN_TYPE)) {
                    Boolean bool = (Boolean)property.getPropertyValue();
                    MESParamBooleanOptions booleanOptions = this.model.getBooleanOptions(property);
                    if (bool != null) {
                        if (bool) {
                            gridCompleted.append("<td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + (booleanOptions.getDisplayTextForTrue() == null ? "Yes" : booleanOptions.getDisplayTextForTrue()) + "</td></tr>");
                        } else {
                            gridCompleted.append("<td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + (booleanOptions.getDisplayTextForFalse() == null ? "No" : booleanOptions.getDisplayTextForFalse()) + "</td></tr>");
                        }
                    } else {
                        gridCompleted.append("<td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + I18nRdlPhaseSetMultRtProps0100.NAValue_Label.msg() + "</td></tr>");
                    }
                } else if (ParameterBundleType.fromIdentifier(property.getBundleIdentifier()).equals(ParameterBundleType.RT_DATETIME_TYPE)) {
                    Time time = (Time)property.getPropertyValue();
                    Calendar calendar = time.getCalendar();
                    Date date = calendar.getTime();
                    SimpleDateFormat configuredDateFormat = this.model.dateFormat(property);
                    if (configuredDateFormat != null) {
                        simpleDateFormat = configuredDateFormat;
                    }

                    gridCompleted.append("<td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + simpleDateFormat.format(date) + "</td></tr>");
                } else {
                    gridCompleted.append("<td style=\"border:1px solid white\"><div style=\"width:100%;text-align: left;\">" + property.getPropertyValue() + "</td></tr>");
                }
            }
        }

    }

    static {
        simpleDateFormat = new SimpleDateFormat(I18nMesFieldFormats.Date_WithHoursMinutesSeconds.msg(), PCContext.getEnvironment().getCurrentLocale());
    }
}
