// CHECKSTYLE:FileLength:off (reason: generated)
// CHECKSTYLE:LineLength:off (reason: generated)
// CHECKSTYLE:MethodLength:off (reason: generated)
package com.rockwell.mes.myeig.model;

/**
 * This file was generated by ATDefAccessClassGenerator and FMPP 2.3.15
 *
 * Please do not modify this file manually !!
 */
import java.util.List;

import com.datasweep.compatibility.client.ATRowFilter;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Server;
import com.rockwell.mes.commons.base.ifc.objects.MESATObject;
import com.rockwell.mes.commons.base.ifc.services.PCContext;

/**
 * Generated filter class for application table MY_EigPersistentObjects.
 */
public class MESGeneratedMyEigPersistentObjectsFilter extends ATRowFilter {

    /** Generated attribute definition */
    private static final long serialVersionUID = 1L;

    /** Generated attribute definition */
    protected static final String ATDEFINITION_NAME = "MY_EigPersistentObjects";

    /**
     * Generated constructor
     *
     * @param server The Server object
     */
    public MESGeneratedMyEigPersistentObjectsFilter(Server server) {
        super(server, ATDEFINITION_NAME);
    }

    /**
     * Generated default constructor
     */
    public MESGeneratedMyEigPersistentObjectsFilter() {
        super(PCContext.getServerImpl(), ATDEFINITION_NAME);
    }

    /**
     * Generated method definition
     *
     * @return the list of the objects
     */
    public List<MESMyEigPersistentObjects> getFilteredObjects() {
        return MESATObject.getFilteredMESATObjectList(this, MESMyEigPersistentObjects.class);
    }

    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forObjectBlobEqualTo(byte[] value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_OBJECTBLOB, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forObjectBlobNotEqualTo(byte[] value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameNotEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_OBJECTBLOB, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forObjectIDEqualTo(Long value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_OBJECTID, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forObjectIDGreaterThanOrEqualTo(Long value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameGreaterThanOrEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_OBJECTID, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forObjectIDLessThan(Long value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameLessThan(
                MESGeneratedMyEigPersistentObjects.COL_NAME_OBJECTID, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forObjectIDNotEqualTo(Long value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameNotEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_OBJECTID, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forTableNameContaining(String value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameContaining(
                MESGeneratedMyEigPersistentObjects.COL_NAME_TABLENAME, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forTableNameEqualTo(String value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_TABLENAME, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forTableNameNotEqualTo(String value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameNotEqualTo(
                MESGeneratedMyEigPersistentObjects.COL_NAME_TABLENAME, value);
    }
    /**
     * Generated method definition
     *
     * @param value the value to be filtered for
     * @return the filter object
     * @throws DatasweepException thrown when error occurs
     */
    public MESMyEigPersistentObjectsFilter forTableNameStartingWith(String value) throws DatasweepException {
        return (MESMyEigPersistentObjectsFilter) forColumnNameStartingWith(
                MESGeneratedMyEigPersistentObjects.COL_NAME_TABLENAME, value);
    }
}
