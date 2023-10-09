package com.rockwell.custmes.services.batchrecord.impl;

import org.apache.commons.lang3.StringUtils;
import org.mesa.xml.b2MMLV0600.IndividualApprovalType;

import com.rockwell.mes.commons.versioning.ifc.IVersioningService;
import com.rockwell.mes.services.batchrecord.impl.converter.IndividualApprovalConverter;

/**
 * Data converter to convert a status transition history entry into {@link IndividualApprovalType}.
 * <p>
 * 
 * @author mkuehne, (c) Copyright 2012 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class MYCIndividualApprovalConverter extends IndividualApprovalConverter {

    /** Name of the state edit - pharma */
    private static final String STATE_NAME_EDITPHARMA = "Edit Pharma";

    /** Name of the state verification - ready for review */
    private static final String STATE_NAME_VERIFREADYFORREVIEW = "Verif. ReadyForReview";

    /** Name of the state verification - reviewed */
    private static final String STATE_NAME_VERIFICATIONREVIEWED = "Verif. Reviewed";

    /** information type to be filled for Approval record: transition information for Action 'Submitted' */
    public static final int APPROVAL_TRANSITION_VERIFREADYFORREVIEW = 50;

    /** information type to be filled for Approval record: transition information for Action 'Reviewed by' */
    public static final int APPROVAL_TRANSITION_VERIFICATIONREVIEWED = 51;

    @Override
    public Integer getTransitionStepValue(IndividualApprovalType appInd, String fromStatus, String toStatus) {
        final Integer returnValue;
        if (isInitialTransition(fromStatus, toStatus)) {
            returnValue = StatusTransitionStep.INITIAL_TRANSITION.getStepValue();
        } else if (isReadyForReviewTransition(fromStatus, toStatus)) {
            returnValue = APPROVAL_TRANSITION_VERIFREADYFORREVIEW;
        } else if (isReviewedTransition(fromStatus, toStatus)) {
            returnValue = APPROVAL_TRANSITION_VERIFICATIONREVIEWED;
        } else if (isValidateTransition(fromStatus, toStatus)) {
            returnValue = StatusTransitionStep.VALIDATE_TRANSITION.getStepValue();
        } else {
            returnValue = null;
        }
        return returnValue;
    }

    // overridden for approval for initial creation (changed FSM)
    @Override
    protected boolean isInitialTransition(final String fromStatus, final String toStatus) {
        return StringUtils.isBlank(fromStatus) && STATE_NAME_EDITPHARMA.equals(toStatus);
    }

    // approval for latest state change to verification - ready for review (changed FSM)
    private boolean isReadyForReviewTransition(String fromStatus, String toStatus) {
        return STATE_NAME_EDITPHARMA.equals(fromStatus) && STATE_NAME_VERIFREADYFORREVIEW.equals(toStatus);
    }

    // approval for latest state change to verification - reviewed (changed FSM)
    private boolean isReviewedTransition(String fromStatus, String toStatus) {
        return STATE_NAME_VERIFREADYFORREVIEW.equals(fromStatus) && STATE_NAME_VERIFICATIONREVIEWED.equals(toStatus);
    }

    // approval for initial state change to valid (changed FSM),
    // ignore transitions from valid to valid
    @Override
    protected boolean isValidateTransition(String fromStatus, String toStatus) {
        if (!toStatus.equals(IVersioningService.STATE_NAME_VALID)) {
            return false;
        }

        return fromStatus.equals(STATE_NAME_VERIFICATIONREVIEWED) //
                || fromStatus.equals(IVersioningService.STATE_NAME_SCHEDULED);
    }

}