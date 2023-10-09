package com.rockwell.custmes.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.AccessPrivilege;
import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.Carrier;
import com.datasweep.compatibility.client.CreationSublotInfo;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.IStatefulObject;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.compatibility.client.TransitionReturnData;
import com.datasweep.compatibility.client.UnitOfMeasure;
import com.datasweep.plantops.common.measuredvalue.IMeasuredValue;
import com.datasweep.plantops.common.measuredvalue.IUnitOfMeasure;
import com.rockwell.mes.commons.base.ifc.exceptions.MESIncompatibleUoMException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESRuntimeException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESTransitionFailedException;
import com.rockwell.mes.commons.base.ifc.fsm.FSMBroker;
import com.rockwell.mes.commons.base.ifc.fsm.IFSMStatusTransitionRequestForStatefulObject;
import com.rockwell.mes.commons.base.ifc.functional.IMeasuredValueConverter;
import com.rockwell.mes.commons.base.ifc.functional.SimpleMeasuredValueConverter;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDABatch;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDASublot;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.services.commons.ifc.functional.PartRelatedMeasuredValueUtilities;
import com.rockwell.mes.services.inventory.ifc.AbstractBatchQualityTransitionEventListener;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.inventory.ifc.IManualModificationListener;
import com.rockwell.mes.services.inventory.ifc.TransactionHistoryContext;
import com.rockwell.mes.services.inventory.ifc.TransactionHistoryHandler;
import com.rockwell.mes.services.inventory.ifc.TransactionSubtype;
import com.rockwell.mes.services.inventory.ifc.TransactionType;
import com.rockwell.mes.services.inventory.impl.StorageLocationHelper;


public class SublotHelper {

    private static final Log LOGGER = LogFactory.getLog(SublotHelper.class);

    /** The SublotHelper is transActional */
    public static final String TRANSACTIONAL = "required";

    private static SublotHelper instance = new SublotHelper();

    /** constants copied from ISublotService */

    /** FSM relationship name for sublot quality FSM */
    public static final String FSM_RELATIONSHIP_SUBLOT_QUALITY = "SublotQuality";

    /** sublot itself has been identified */
    public static final long IDENTIFICATIONMODE_SUBLOT = 0;

    /** sublot has been identified via its load carrier */
    public static final long IDENTIFICATIONMODE_LOADCARRIER = 1;

    private SublotHelper() {
        // singleton, see the instance method
    }

    public static SublotHelper getInstance() {
        return instance;
    }

    public Response createSublot(final Batch batch, final String identifier, final MeasuredValue quantity, final Location sloc,
            final TransactionHistoryContext thContext) throws MESIncompatibleUoMException {
        Response response = createSublots(batch, new String[] { identifier }, quantity, sloc, thContext);
        if (response.isOk()) {
            Sublot[] sublots = (Sublot[]) response.getResult();
            if (sublots != null && sublots.length != 0) {
                response.setResult(sublots[0]);
            }
        }
        return response;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.rockwell.mes.services.inventory.ifc.ISublotService#createMESSublots(com.rockwell.mes.services.inventory.ifc.IMESBatch,
     *      int, com.datasweep.compatibility.client.MeasuredValue,
     *      com.rockwell.mes.services.inventory.ifc.IMESStorageLocation,
     *      com.rockwell.mes.services.inventory.ifc.IMESLoadCarrierType)
     */
    public Response createSublots(final Batch batch, final String[] identifiers, final MeasuredValue quantity, final Location sloc,
            final TransactionHistoryContext thContext) throws MESIncompatibleUoMException {
        Response response = new Response();
        try {

            Sublot[] sublots = null;

            checkSublotCreationPrerequisites(batch, identifiers.length, sloc);

            IBatchService batchService = ServiceFactory.getService(IBatchService.class);
            String releaseStatus = batchService.getBatchStatus(batch);

            // "true" means: create transaction history entries
            sublots = createSublotsInternally(batch, identifiers, quantity, sloc, releaseStatus, thContext, true);
            response.setResult(sublots);
        } catch (MESIncompatibleUoMException exc) {
            response = UIHelper.createResponseObject(exc);
        } catch (MESRuntimeException exc) {
            response = UIHelper.createResponseObject(exc);
        }
        return response;
    }

    /**
     * @param batch The batch to which the sublots should belong
     * @param numOfSublots Number of sublots to be created
     * @param quantity Quantity of new sublots
     * @param sloc Storage location of the sublots
     * @param releaseStatus Initial release status of the sublots
     * @param thContext Transaction history context
     * @param createTxEntries Specifies if transaction history entries should be created ("true": yes, "false": no)
     * @return An array of the created sublots
     * @throws MESIncompatibleUoMException Thrown when the given UoM is incompatible to the batch UoM
     */
    protected Sublot[] createSublotsInternally(final Batch batch, final String[] identifiers, final MeasuredValue quantity, final Location sloc,
            final String releaseStatus, final TransactionHistoryContext thContext, final boolean createTxEntries) throws MESIncompatibleUoMException {
        // consider part-specific conversion factors
        MeasuredValue convQty = convertToTargetUoM(batch, quantity);

        // 1. Create PC sublots
        List<Sublot> sublotList = createPCSublots(batch, identifiers, convQty);

        // 2. Initialize the logically deleted flag and the release status
        for (Sublot sublot : sublotList) {
            // Set some default values explicitly, since this is not done by the
            // middle tier
            // It is absolutely needed for filtering later, because filtering
            // with specific null
            // consideration is very difficult.
            setSublotLogicallyDeleted(sublot, false);
            MESNamedUDASublot.setIdentifiedForOSI(sublot, 0L);
            MESNamedUDASublot.setAllocatedForOSI(sublot, 0L);

            saveSublot(sublot);
        }

        Sublot[] sublots = new Sublot[sublotList.size()];
        sublotList.toArray(sublots);

        // 3. place the sublots on the given storage location (in fact the
        // sublots are placed on the intermediate bin of the storage location)
        relocateSublotsInternally(sublots, sloc);

        // 4. Create transaction history entries, if required
        if (createTxEntries) {
            TransactionHistoryHandler thHandler = new TransactionHistoryHandler(TransactionType.INPUT, TransactionSubtype.GOODS_RECEIPT, thContext);
            for (int i = 0; i < sublots.length; i++) {
                thHandler.historyDataBefore(batch, null);
                thHandler.historyDataAfter(batch, sublots[i]);
            }
        }

        return sublots;
    }

    /**
     * Checks the plausibility of the sublot creation parameters.
     *
     * @param batch Batch for which the sublots should be created
     * @param numOfSublots Number of sublots to be created
     * @param sloc Storage location of sublots
     */
    protected void checkSublotCreationPrerequisites(final Batch batch, final int numOfSublots, final Location sloc) {

        if (batch == null) {
            throw new MESRuntimeException(this.getClass().getName() + ".checkSublotCreationPrerequisites(): Batch is missing");
        }

        if (numOfSublots <= 0) {
            throw new MESRuntimeException(this.getClass().getName() + ".checkSublotCreationPrerequisites(): Number of sublots must be > 0");
        }

        if (sloc == null) {
            throw new MESRuntimeException(this.getClass().getName() + ".checkSublotCreationPrerequisites(): " + "storage location must be specified");
        }
    }

    /**
     * Method which converts a sublot-related MeasuredValue into its target UoM, i.e. 1. a part-specific UoM conversion
     * is performed, if available and 2. the conversion into the batch UoM is performed
     *
     * @param batch Related batch
     * @param mvToBeConverted MeasuredValue to be converted
     * @return Converted MeasuredValue
     * @throws MESIncompatibleUoMException Thrown when the given UoM is incompatible to the batch UoM
     */
    private MeasuredValue convertToTargetUoM(final Batch batch, final MeasuredValue mvToBeConverted) throws MESIncompatibleUoMException {

        // now we try to convert the resulting MeasuredValue into the batch UoM
        UnitOfMeasure batchUoM = MESNamedUDABatch.getUnitOfMeasure(batch);

        // Currently we cannot be sure that a batch has a dedicated UoM.
        // Therefore we check (as a fallback level) if it has a quantity and
        // if yes, we use its UoM!
        if (batchUoM == null) {
            MeasuredValue batchQty = batch.getQuantity();

            if (batchQty != null) {
                LOGGER.warn("Batch <" + batch.getName() + "> has no dedicated UoM - we use the UoM of its quantity");
                batchUoM = (UnitOfMeasure) batchQty.getUnitOfMeasure();
            }
        }

        MeasuredValue result = mvToBeConverted;
        if (batchUoM != null) {
            // NOTE: the following check is necessary due to a PC bug!
            if (!batchUoM.equals(result.getUnitOfMeasure())) {
                result = (MeasuredValue) performPartSpecificUoMConversion(batch.getPart(), mvToBeConverted, batchUoM);
            }
        } else {
            LOGGER.error("Batch <" + batch.getName() + "> has no UoM!");
        }

        return result;
    }

    /**
     * Helper method which performs a part-specific UoM conversion of the given MeasuredValue, if one is available
     *
     * @param part Part whose specific conversion shall be used
     * @param mvToBeConverted MeasuredValue to be converted
     * @param targetUoM wished UoM of the result
     * @return Converted MeasuredValue, if conversion available and successful. Otherwise the original MeasuredValue is
     *         returned
     * @throws MESIncompatibleUoMException if conversion failed.
     */
    private IMeasuredValue performPartSpecificUoMConversion(Part part, IMeasuredValue mvToBeConverted, IUnitOfMeasure targetUoM)
            throws MESIncompatibleUoMException {
        if (part == null || mvToBeConverted == null || targetUoM.equals(mvToBeConverted.getUnitOfMeasure())) {
            return mvToBeConverted;
        }

        IMeasuredValueConverter converter = PartRelatedMeasuredValueUtilities.getConfiguredPartSpecificMeasuredValueConverter(part);
        if (converter == null) {
            // if we have no part specific conversion, use PC built-in
            // conversion
            converter = SimpleMeasuredValueConverter.getInstance();
        }
        if (converter.canConvert(mvToBeConverted.getUnitOfMeasure(), targetUoM)) {
            IMeasuredValue newValue = converter.convert(mvToBeConverted, targetUoM);
            if (newValue.getScale() < 0) {
                try {
                    newValue.setScale(0);
                } catch (Exception e) {
                    e.equals(e); // Check style nonsense
                }
            }
            return newValue;
        } else {
            throw new MESIncompatibleUoMException("Incompatible UoMs: Source UoM = <%1$s> " + ", UoM of batch = <%2$s>.",
                    mvToBeConverted.getUnitOfMeasure(), targetUoM);
        }
    }

    /**
     * Creates PC sublots for the given batch
     *
     * @param batch Batch for which the sublots should be created
     * @param numOfSublots Number of sublots to be created
     * @param quantity Quantity of new sublots
     * @return An array of DSublot objects
     */
    @SuppressWarnings("unchecked")
    private List<Sublot> createPCSublots(final Batch batch, final String[] identifiers, final MeasuredValue quantity) {
        Response response;
        Vector creationSublotInfoItems = new Vector();
        String comment = ""; // should we support passing a comment here?

        for (String sublotIdentifier : identifiers) {

            CreationSublotInfo creationSublotInfo = PCContext.getFunctions().createCreationSublotInfo(sublotIdentifier, quantity);
            creationSublotInfoItems.add(creationSublotInfo);
        }

        response = batch.createSublots(null, creationSublotInfoItems, comment, null);

        if (response.isError()) {
            throw new MESRuntimeException(this.getClass().getName() + ".createPCSublots(): PC call Batch.createSublots() failed: "
                    + response.getFirstErrorMessage());
        }
        List<Sublot> sublots = (List<Sublot>) response.getResult();

        return sublots;
    }

    /**
     * Helper method to set the "logically deleted" flag of the given sublot
     *
     * @param sublot Sublot
     * @param logicallyDeleted "Logically deleted" flag
     */
    private void setSublotLogicallyDeleted(final Sublot sublot, boolean logicallyDeleted) {
        Long logicallyDeletedAsLong = null;

        if (logicallyDeleted) {
            logicallyDeletedAsLong = new Long(1);
        } else {
            logicallyDeletedAsLong = new Long(0);
        }

        MESNamedUDASublot.setLogicallyDeleted(sublot, logicallyDeletedAsLong);
    }

    /**
     * Saves the given sublot
     *
     * @param sublot Sublot to be saved
     */
    protected void saveSublot(final Sublot sublot) {
        String comment = "";
        AccessPrivilege accessPrivilege = PCContext.getDefaultAccessPrivilege();

        try {
            sublot.Save(null, comment, accessPrivilege);
        } catch (DatasweepException e) {
            throw new MESRuntimeException(this.getClass().getName() + ".saveSublot(): Saving of sublot failed", e);
        }
    }

    /**
     * Execute status transition on a sublot (not possible in PS standard)
     * 
     * @throws MESTransitionFailedException Transition failed
     */
    public TransitionReturnData applyQualityStatusTransition(final String pTransitionName, final Sublot pSublot) //
            throws MESTransitionFailedException {
        return FSMBroker.applyStatusTransition(new IFSMStatusTransitionRequestForStatefulObject() {

            @Override
            public IStatefulObject getStatefulObject() {
                return pSublot;
            }

            @Override
            public String getRelationshipName() {
                return AbstractBatchQualityTransitionEventListener.FSM_REL_SHIP;
            }

            @Override
            public String getTransitionName() {
                return pTransitionName;
            }

            @Override
            public String getCallbackConfigKey() {
                return FSMBroker.FSM_CONFIG_PATH + "SublotQuality.TransitionListener";
            }

            @Override
            public Collection<String> getCallbackDefaultConfig() {
                return Arrays.asList(new String[] { IManualModificationListener.class.getName() });
            }

            @Override
            public String getCallbackDefaultConfigComment() {
                return "This should never end up as default configuration";
            }

            @Override
            public <C> C getSpecificContext(Class<C> contextClass) {
                return null;
            }

        });
    }

    /**
     * Relocates the given sublots to the given storage location. This is done not directly, but by means of the
     * associated intermediate bin. ATTENTION: Be aware that this method assumes that all the given sublot reside on the
     * same bin!
     *
     * @param sublots Sublots to be relocated
     * @param sloc Target storage location (mandatory if no LC is given)
     */
    protected void relocateSublotsInternally(final Sublot[] sublots, final Location sloc) {

        if (sloc == null) {
            // this is a legal case when a temporary sublot is created,
            // therefore we silently return here
            return;
        }

        if (sublots.length == 0) {
            throw new IllegalArgumentException("sublots must not be null");
        }

        // remove sublots from their current bin
        Carrier oldBin = sublots[0].getCarrier();

        // old bin can be "null" when sublots were just created, so this is
        // a valid case
        if (oldBin != null) {
            if (!StorageLocationHelper.isIntermediateBin(oldBin)) {
                throw new MESRuntimeException("Bin of sublot <" + sublots[0].getName() + "> is not the expected one");
            }

            oldBin.removeSublots(sublots);
            // saveBin(oldBin);
        }

        // StorageLocationService.getBinOfStorageLocation() already throws an
        // exception if no intermediate bin could be found
        Carrier newBin = StorageLocationHelper.getBinOfStorageLocation(sloc);

        newBin.addSublots(sublots);
        // saveBin(newBin);

        LOGGER.info("relocateSublotsInternally(): # of sublots stored on bin <" + newBin.getName() + "> = " + newBin.getContainedSublots().size());
    }

}