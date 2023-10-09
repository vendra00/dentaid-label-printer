package com.rockwell.custmes.services.order.ifc;

import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder;
import com.rockwell.mes.services.order.ifc.IOrderExplosionService;

/**
 * Enhanced order explosion service
 * @author JParalkar
 *
 */
public interface IEnhancedOrderExplosionService extends IOrderExplosionService {

	/**
     * Method to set the list of ErpOrderBuilder
     * 
     * @param orderBuilder - ErpOrderBuilder
     */
    public void setOrderBuilder(ErpOrderBuilder orderBuilder);
}
