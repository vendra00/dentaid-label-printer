package com.rockwell.custmes.services.batchreport.impl;

import com.rockwell.custmes.services.batchrecord.impl.MYCIndividualApprovalConverter;
import com.rockwell.mes.services.batchreport.impl.MasterRecipeDocumentWrapper;

/**
 * Custom Wrapper for a B2MML MasterRecipeDocument. Provides the getters used by Jasper reports.
 * <p>
 * 
 * @author hhofsaess, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class MYCMasterRecipeDocumentWrapper extends MasterRecipeDocumentWrapper {

    /**
     * information type to be filled for Approval record: transition information
     * for Action 'Submitted'
     */
    public static final int APPROVAL_TRANSITION_VERIFREADYFORREVIEW = MYCIndividualApprovalConverter.APPROVAL_TRANSITION_VERIFREADYFORREVIEW;

    /**
     * information type to be filled for Approval record: transition information
     * for Action 'Reviewed by'
     */
    public static final int APPROVAL_TRANSITION_VERIFICATIONREVIEWED = MYCIndividualApprovalConverter.APPROVAL_TRANSITION_VERIFREADYFORREVIEW;

}