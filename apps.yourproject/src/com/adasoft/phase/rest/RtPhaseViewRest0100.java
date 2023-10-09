package com.adasoft.phase.rest;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.adasoft.phase.rest.model.Call;
import com.adasoft.phase.rest.model.Label;
import com.adasoft.phase.rest.model.LabelTableModel;
import com.adasoft.phase.rest.model.LogMsgDialect;
import com.adasoft.phase.rest.model.UIMsgDialect;
import com.adasoft.phase.rest.model.Request;
import com.adasoft.phase.rest.service.LabelService;
import com.adasoft.phase.rest.service.LabelServiceImpl;
import com.adasoft.phase.rest.service.RequestService;
import com.adasoft.phase.rest.service.RequestServiceImpl;
import com.rockwell.mes.apps.ebr.ifc.phase.ui.UIConstants;
import com.rockwell.mes.apps.ebr.ifc.swing.PhaseSwingHelper;
import com.rockwell.mes.commons.shared.phase.mvc.AbstractPhaseMainView0200;
import com.rockwell.mes.commons.shared.phase.viewhelper.swing.LayoutHelper0201;

/**
 * TODO: Runtime phase view.
 * <p>
 * Skeleton generated by the PhaseLibManager.
 * <p>
 * 
 * TODO: @author UserName, (c) Copyright 2013 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class RtPhaseViewRest0100 extends AbstractPhaseMainView0200<RtPhaseModelRest0100> {

	/** Comment for <code>serialVersionUID</code> */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(RtPhaseViewRest0100.class.getName());
	List<Label> labelsList = new ArrayList<>();

	JPanel layoutPanel = new JPanel();


	private JButton fetchButton;
	private JButton pingButton;
	private JButton createButton;
	private JButton editButton;
	private JButton deleteButton;

	/**
	 * @param theModel The model of this view
	 */
	protected RtPhaseViewRest0100(final RtPhaseModelRest0100 theModel) {
		super(theModel);
	}

	@Override
	protected String getNavigatorInfoColumn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void createUI() {
		// CHECKSTYLE:MagicNumber:off (Reason: Columns, rows and width up to 3 ...)
		LOGGER.info(LogMsgDialect.CREATE_UI_CALL.getMsgDialect());
		setOpaque(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbConstraints = new GridBagConstraints();

		gbConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		LayoutHelper0201.setGBConstraints(gbConstraints, 0, 0, 3, 1);
		add(createInstructionPanel(getModel().getInstructionTextColumn1(), LayoutHelper0201.calculatePreferredWidth(gbConstraints)), gbConstraints);

		LayoutHelper0201.setGBConstraints(gbConstraints, 0, 1, 3, 1);
		add(createActionPanel(LayoutHelper0201.calculatePreferredWidth(gbConstraints)),
				gbConstraints);

		gbConstraints.anchor = GridBagConstraints.PAGE_END;
		LayoutHelper0201.setGBConstraints(gbConstraints, 3, 1, 1, 1);
		add(createConfirmPanel(), gbConstraints);

		// CHECKSTYLE:MagicNumber:on
	}

	/**
	 * @param preferredWidth the preferred width.
	 * @return The action panel
	 */
	protected Component createActionPanel(int preferredWidth) {
		LOGGER.info(LogMsgDialect.CREATE_ACTION_PANEL_CALL.getMsgDialect());
		JPanel layoutPanel = PhaseSwingHelper.createPanel();
		((FlowLayout) layoutPanel.getLayout()).setHgap(UIConstants.DEFAULT_GAP);
		((FlowLayout) layoutPanel.getLayout()).setVgap(UIConstants.DEFAULT_GAP);

		fetchButton = new JButton(UIMsgDialect.FETCH_BTN_NAME.getMsgDialect());        
		fetchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fetchAndDisplayLabels();
			}
		});
		layoutPanel.add(fetchButton);

		pingButton = new JButton(UIMsgDialect.PING_BTN_NAME.getMsgDialect());        
		pingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pingServer();
			}
		});
		layoutPanel.add(pingButton);

		createButton = new JButton(UIMsgDialect.CREATE_BTN_NAME.getMsgDialect());      
		createButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createlabelServer();
			}
		});
		layoutPanel.add(createButton);

		editButton = new JButton(UIMsgDialect.EDIT_BTN_NAME.getMsgDialect());      
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editLabelServer();
			}
		});
		layoutPanel.add(editButton);

		deleteButton = new JButton(UIMsgDialect.DELETE_BTN_NAME.getMsgDialect());      
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteLabelServer();
			}
		});
		layoutPanel.add(deleteButton);

		layoutPanel.add(Box.createVerticalGlue());

		return layoutPanel;
	}       

	private Request findRequestByCallType(Call callType) {
		// Create an instance of RequestService
		RequestService requestService = new RequestServiceImpl();
		List<Request> requests = requestService.getRequests();

		if (requests.isEmpty()) {
			LOGGER.warning(LogMsgDialect.NO_REQUEST_FOUND.getMsgDialect());
			return null;
		}

		// Iterate through requests and find the position based on the URL's last part
		for (Request request : requests) {
			String url = request.getUrl();
			String lastPart = url.substring(url.lastIndexOf('/') + 1);
			if (callType.getUrlCallName().equals(lastPart)) {
				return request;
			}
		}

		LOGGER.warning(LogMsgDialect.NO_MATCHING_REQUEST_FOUND.getMsgDialect());
		return null;
	}

	private void pingServer() {
		Request request = findRequestByCallType(Call.PING);
		if (request == null) return;

		// Create an instance of LabelService
		LabelService labelService = new LabelServiceImpl();
		boolean serverStatus = labelService.pingServer(request);
		JOptionPane.showMessageDialog(null,
				serverStatus ? UIMsgDialect.SERVER_UP.getMsgDialect() : UIMsgDialect.SERVER_DOWN.getMsgDialect(),
						serverStatus ? UIMsgDialect.STATUS_SUCCESS.getMsgDialect() : UIMsgDialect.STATUS_FAIL.getMsgDialect(),
								serverStatus ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
	}

	private void editLabelServer() {
		Request request = findRequestByCallType(Call.EDIT_LABEL);
		if (request == null) return;

		// Create an instance of LabelService
		LabelService labelService = new LabelServiceImpl();
		Boolean edited = labelService.editLabelInServer(request);

		JOptionPane.showMessageDialog(null,
				edited ? UIMsgDialect.LABEL_EDITED_SUCCESS.getMsgDialect() : UIMsgDialect.LABEL_EDITED_FAIL.getMsgDialect(),
						edited ? UIMsgDialect.STATUS_SUCCESS.getMsgDialect() : UIMsgDialect.STATUS_FAIL.getMsgDialect(),
								edited ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

	}

	private void deleteLabelServer() {
		Request request = findRequestByCallType(Call.DELETE_LABEL);
		if (request == null) return;

		// Create an instance of LabelService
		LabelService labelService = new LabelServiceImpl();
		Boolean deleted = labelService.deleteLabelInServer(request);

		JOptionPane.showMessageDialog(null,
				deleted ? UIMsgDialect.LABEL_DELETED_SUCCESS.getMsgDialect() : UIMsgDialect.LABEL_DELETED_FAIL.getMsgDialect(),
						deleted ? UIMsgDialect.STATUS_SUCCESS.getMsgDialect() : UIMsgDialect.STATUS_FAIL.getMsgDialect(),
								deleted ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

	}

	private void createlabelServer() {
		Request request = findRequestByCallType(Call.CREATE_LABEL);
		if (request == null) return;

		// Create an instance of LabelService
		LabelService labelService = new LabelServiceImpl();
		Boolean created = labelService.createLabelInServer(request);

		JOptionPane.showMessageDialog(null,
				created ? UIMsgDialect.LABEL_CREATED_SUCCESS.getMsgDialect() : UIMsgDialect.LABEL_CREATED_FAIL.getMsgDialect(),
						created ? UIMsgDialect.STATUS_SUCCESS.getMsgDialect() : UIMsgDialect.STATUS_FAIL.getMsgDialect(),
								created ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
	}

	private void fetchAndDisplayLabels() {
		Request request = findRequestByCallType(Call.FETCH_LABELS);
		if (request == null) return;

		// Create an instance of LabelService
		LabelService labelService = new LabelServiceImpl();
		List<Label> labels = labelService.fetchAllLabelsFromServer(request);

		// Display the fetched labels in a table
		displayLabelsInTable(labels);
	}

	private void displayLabelsInTable(List<Label> labels) {
		JFrame frame = new JFrame(UIMsgDialect.LABEL_NAME_FRAME_TABLE.getMsgDialect());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		// Create the table model using the custom LabelTableModel
		LabelTableModel labelTableModel = new LabelTableModel(labels);

		// Create the JTable with the table model
		JTable labelTable = new JTable(labelTableModel);

		// Add the table to a scroll pane
		JScrollPane scrollPane = new JScrollPane(labelTable);
		frame.add(scrollPane, BorderLayout.CENTER);

		// Set preferred size and show the frame
		frame.setPreferredSize(new Dimension(600, 400));
		frame.pack();
		frame.setVisible(true);

	}
}
