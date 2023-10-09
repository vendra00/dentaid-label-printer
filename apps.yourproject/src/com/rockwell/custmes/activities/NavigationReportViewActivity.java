package com.rockwell.custmes.activities;

import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JRException;

import com.datasweep.compatibility.client.Report;
import com.datasweep.compatibility.ui.controls.ReportViewerControl;
import com.datasweep.plantops.swing.ControlDock;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.mes.clientfw.pec.ifc.view.NavigationBaseActivity;

/**
 * Activity to visualize a report
 * <p>
 *
 * @author rweingar
 */
public class NavigationReportViewActivity extends NavigationBaseActivity {
    /**
     * Comment for <code>INPUT_REPORT</code>
     */
    public static final String INPUT_REPORT = "report";

    /**
     * Comment for <code>reportViewer</code>
     */
    private ReportViewerControl reportViewer;

    /**
     * Comment for <code>LOGGER</code>
     */
    private static final org.apache.commons.logging.Log LOGGER = org.apache.commons.logging.LogFactory
            .getLog(NavigationReportViewActivity.class);

    /**
     * Constructor
     */
    public NavigationReportViewActivity() {
        super();

        try {
            reportViewer = new ReportViewerControl();
        } catch (JRException exc) {
            LOGGER.error("Cannot instantiate report viewer control", exc);
        }

        reportViewer.setName("reportViewer");
        reportViewer.setActivityName("reportViewer");
        reportViewer.setDock(ControlDock.FILL);
        reportViewer.setPrintButtonEnabled(false);
        reportViewer.setSaveButtonEnabled(false);

        contentPane.add(reportViewer);
    }


    @Override
    protected void inputItemSet(String key, final Object value) {
        super.inputItemSet(key, value);

        if (INPUT_REPORT.equals(key)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        reportViewer.setReport((Report) value);
                    } catch (Exception exc) {
                        LOGGER.error("Cannot set report object", exc);
                    }
                }
            });
        }
    }

    @Override
    public String getActivityDescription() {
        return "Activity to visualize a sublot report";
    }

    @Override
    public ItemDescriptor[] inputDescriptors() {
        return new ItemDescriptor[] { new ItemDescriptor(INPUT_REPORT, this.getClass(), Report.class) };
    }
}
