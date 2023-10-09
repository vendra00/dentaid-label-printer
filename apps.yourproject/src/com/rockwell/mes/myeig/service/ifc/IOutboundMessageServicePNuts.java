package com.rockwell.mes.myeig.service.ifc;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.client.Sublot;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.services.IMESServicePNuts;

/**
 * Interface of service API for ERP interface messaging
 * 
 * @author syim, (c) Copyright 2012 Rockwell Automation Solutions, Inc. All
 *         Rights Reserved.
 * 
 */
public interface IOutboundMessageServicePNuts extends IErpMessageService, IMESServicePNuts {

    /**
     * Send relocation material message to ERP
     * 
     * @param orderStpeInputs
     * @return ask for material message
     * @throws MESException
     * @throws DatasweepException
     */
    Response createMaterialRelocationMessage(Sublot[] paramArrayOfSublot);
}
