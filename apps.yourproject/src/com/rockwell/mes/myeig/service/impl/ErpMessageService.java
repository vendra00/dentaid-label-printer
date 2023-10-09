package com.rockwell.mes.myeig.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.DatasweepException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.myeig.model.MESMyEigPersistentObjects;
import com.rockwell.mes.myeig.model.MESMyEigPersistentObjectsFilter;
import com.rockwell.mes.myeig.service.ifc.IErpMessageService;

/**
 * Service API for ERP interface messaging
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 * 
 */
public class ErpMessageService implements IErpMessageService {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(ErpMessageService.class);

    @Override
    public MESMyEigPersistentObjects fetchPersistentObject(Long objectId, String tableName) throws DatasweepException {

        MESMyEigPersistentObjects persistentObject = null;
        MESMyEigPersistentObjectsFilter epof = new MESMyEigPersistentObjectsFilter();
        epof.forObjectIDEqualTo(objectId);
        epof.forTableNameEqualTo(tableName);
        List<MESMyEigPersistentObjects> objects = epof.getFilteredObjects();
        if (CollectionUtils.isNotEmpty(objects)) {
            persistentObject = objects.get(0);
        }
        return persistentObject;
    }

    @Override
    public MESMyEigPersistentObjects savePersistentObject(Long objectId, String tableName, Object object)
            throws MESException, DatasweepException {
        MESMyEigPersistentObjects persistentObject = fetchPersistentObject(objectId, tableName);
        if (persistentObject == null) {
            persistentObject = new MESMyEigPersistentObjects();
            persistentObject.setObjectID(objectId);
            persistentObject.setTableName(tableName);

        }
        savePersistentObject(persistentObject, object);
        return persistentObject;
    }

    /**
     * Persists a transfer object blob
     * 
     * @param persistentObject The persistent object
     * @param object The transfer object to persist
     * @throws MESException On Error.
     * @throws DatasweepException On Error.
     */
    protected void savePersistentObject(MESMyEigPersistentObjects persistentObject, Object object) throws MESException,
            DatasweepException {
        ObjectOutputStream oos = null;
        try {
            // serialize, zip and store
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            persistentObject.setObjectBlob(baos.toByteArray());
            // save AT Row
            persistentObject.Save(PCContext.getCurrentServerTime(), "Creating transfer object entry for EIG message",
                    PCContext.getDefaultAccessPrivilege());
        } catch (IOException e) {
            throw new MESException(e);
        } finally {
            closeStream(oos);
        }
    }

    @Override
    public Object getPayload(Long objectId, String tableName) throws MESException, DatasweepException {
        MESMyEigPersistentObjects persistentObject = fetchPersistentObject(objectId, tableName);
        Object object = null;
        ObjectInputStream objectInputStream = null;
        try {
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(persistentObject.getObjectBlob());
            objectInputStream = new ObjectInputStream(byteInputStream);
            object = objectInputStream.readObject();
        } catch (IOException e) {
            throw new MESException(e);
        } catch (ClassNotFoundException e) {
            throw new MESException(e);
        } finally {
            closeStream(objectInputStream);
        }
        return object;
    }

    /**
     * Closes the given stream.
     * 
     * @param stream stream to close
     */
    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.warn("error closing stream" + e.getMessage());
            }
        }
    }
}
