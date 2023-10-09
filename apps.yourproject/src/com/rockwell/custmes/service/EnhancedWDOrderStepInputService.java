package com.rockwell.custmes.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepInput;
import com.datasweep.compatibility.client.Sublot;
import com.rockwell.custmes.commons.base.ifc.nameduda.CustMESNamedUDAOrderStepInput;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESRuntimeException;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStepInput;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.commons.base.ifc.services.Transactional;
import com.rockwell.mes.commons.base.ifc.utility.Pair;
import com.rockwell.mes.myeig.service.ifc.IOutboundMessageService;
import com.rockwell.mes.myeig.service.impl.OutboundMessageService;
import com.rockwell.mes.services.s88.ifc.execution.IRuntimeEntity;
import com.rockwell.mes.services.warehouseintegration.ifc.WarehouseRuntimeException;
import com.rockwell.mes.services.wd.ifc.InvalidDataException;
import com.rockwell.mes.services.wd.ifc.ProduceSublotInfoForFinish;
import com.rockwell.mes.services.wd.ifc.WeighingSituation;
import com.rockwell.mes.services.wd.impl.WDOrderStepInputService;

public class EnhancedWDOrderStepInputService extends WDOrderStepInputService {
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(EnhancedWDOrderStepInputService.class);

    /** The service for OutboundMessage */
    IOutboundMessageService outboundMsgService = ServiceFactory.getService(IOutboundMessageService.class);

    @Override @Transactional
    public Sublot finish(final OrderStepInput orderStepInput, final WeighingSituation weighingSituation,
            final ProduceSublotInfoForFinish produceSublotInfo, final IRuntimeEntity rtEntity) throws InvalidDataException {
        Sublot sublot = super.finish(orderStepInput, weighingSituation, produceSublotInfo, rtEntity);

        try {
            outboundMsgService.createConsumptionMessage(orderStepInput);
        } catch (DatasweepException | MESException e) {
            LOGGER.error(e);
            throw new MESRuntimeException(e);
        }

        return sublot;
    }
}
