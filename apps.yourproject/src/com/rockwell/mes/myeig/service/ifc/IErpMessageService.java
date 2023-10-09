package com.rockwell.mes.myeig.service.ifc;

import com.datasweep.compatibility.client.DatasweepException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.IMESService;
import com.rockwell.mes.myeig.model.MESMyEigPersistentObjects;

/**
 * Interface of service API for ERP interface messaging
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 * 
 */
public interface IErpMessageService extends IMESService {

    /** prefix used for default (i.e. non customer specific) AT columns */
    public static final String DEFAULT_AT_COLUMN_PREFIX = "X_";

    /**
     * Gets the persistent object (AT table)
     * 
     * 
     * @param objectId ID of referenced table
     * @param tableName Table name of referenced object
     * @return The persistent object
     * @throws DatasweepException On Error.
     */
    public MESMyEigPersistentObjects fetchPersistentObject(Long objectId, String tableName) throws DatasweepException;

    /**
     * Persists a transfer object
     * 
     * @param objectId ID of referenced table
     * @param tableName Table name of referenced object
     * @param object The transfer object to persist
     * @return The persistent object
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public MESMyEigPersistentObjects savePersistentObject(Long objectId, String tableName, Object object)
            throws MESException, DatasweepException;

    /**
     * Gets the message payload object.
     * 
     * @param objectId The object id
     * @param tableName The table name
     * @return The persisted payload
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    public Object getPayload(Long objectId, String tableName) throws MESException, DatasweepException;

}
