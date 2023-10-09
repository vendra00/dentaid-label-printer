package com.rockwell.custmes.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.Location;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Sublot;
import com.rockwell.mes.commons.base.ifc.exceptions.MESIncompatibleUoMException;
import com.rockwell.mes.services.inventory.ifc.TransactionHistoryContext;
import com.rockwell.mes.services.inventory.impl.SublotService;

/**
 * Customer specific sublot service that allows to create sublots with some given identifiers. 
 *
 * @author hplang
 */
public class CtSublotService extends SublotService {

    private static final Log LOGGER = LogFactory.getLog(CtSublotService.class);
    
    private String[] sublotIdentifiers = null;

    private int currentSublotIdentifierIndex = 0;

    public Sublot[] ctCreateSublots(final Batch batch, final int numOfSublots, final String[] identifiers,
            final MeasuredValue quantity, final Location sloc, final TransactionHistoryContext thContext)
            throws MESIncompatibleUoMException {
        LOGGER.debug("creating sublots with identifiers provided");
        sublotIdentifiers = identifiers;
        currentSublotIdentifierIndex = 0;
        return super.createSublots(batch, numOfSublots, quantity, sloc, thContext);
    }

    @Override
    public Sublot[] createSublots(Batch batch, int numOfSublots, MeasuredValue quantity, Location sloc,
            TransactionHistoryContext thContext) throws MESIncompatibleUoMException {
        sublotIdentifiers = null;
        currentSublotIdentifierIndex = 0;
        return super.createSublots(batch, numOfSublots, quantity, sloc, thContext);
    }
    
    @Override
    public String createSublotName() {
        if (sublotIdentifiers != null && currentSublotIdentifierIndex < sublotIdentifiers.length
                && sublotIdentifiers[currentSublotIdentifierIndex] != null) {
            String identifier = sublotIdentifiers[currentSublotIdentifierIndex];
            LOGGER.debug("fetch the sublot identifier as provided by the caller: " + identifier);
            return identifier;
        } else {
            return super.createSublotName();
        }
    }
}
