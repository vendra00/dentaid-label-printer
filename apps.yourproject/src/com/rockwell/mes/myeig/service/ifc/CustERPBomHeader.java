package com.rockwell.mes.myeig.service.ifc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.Part;
import com.rockwell.mes.myeig.data.OrderTransferObject;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderAdditionalData;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderCpsComponent;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderCpsPack;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderItem;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderStatus;
import com.rockwell.mes.myeig.data.OrderTransferObject.ProductionOrder;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder.ErpOrderItemBuilder;
import com.rockwell.mes.services.s88.impl.recipe.MESERPBomHeader;
import com.rockwell.mes.services.s88.impl.recipe.MESERPBomItem;

import groovy.transform.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class CustERPBomHeader extends MESERPBomHeader{

//--------------------------------- Inicio Declaracion de Getters y Setters que usará la clase -------------------------------------------------------
	
	private Part part;
	private String productVersion;
	private String plant;
	
	
	
//--------------------------------- Fin Declaracion de Getters y Setters que usará la clase ----------------------------------------------------------
	
    private Map<String, Object> udaMap = Collections.EMPTY_MAP;
    
    private List<CustERPBomItem> components = new ArrayList<CustERPBomItem>();

    public CustERPBomHeader udaMap(Map<String, Object> value) {
        if (value == null) {
            udaMap = Collections.EMPTY_MAP;
        } else {
            udaMap = new HashMap(value);
        }
        return this;
    }

    public Map<String, Object> getUdaMap() {
        return udaMap;
    }

    public MESERPBomHeader components(List<CustERPBomItem> value) {
        if (value == null) {
            components = new ArrayList<CustERPBomItem>();
        } else {
            components = value;
        }
        return this;
    }

    public List<CustERPBomItem> getComponents() {
        return components;
    }
    
    @Data 
    @NoArgsConstructor 
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper=false)
    public static class CustERPBomItem extends MESERPBomItem{

        
        private Map<String, Object> udaMap = Collections.EMPTY_MAP;

        public CustERPBomItem udaMap(Map<String, Object> value) {
            if (value == null) {
                udaMap = Collections.EMPTY_MAP;
            } else {
                udaMap = new HashMap(value);
            }
            return this;
        }

        public Map<String, Object> getUdaMap() {
            return udaMap;
        }

    }
}
