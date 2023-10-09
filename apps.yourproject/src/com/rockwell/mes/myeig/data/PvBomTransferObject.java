package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;
import com.rockwell.mes.myeig.data.OrderTransferObject.Order;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderAdditionalData;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderCpsComponent;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderCpsPack;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderItem;
import com.rockwell.mes.myeig.data.OrderTransferObject.OrderStatus;
import com.rockwell.mes.myeig.data.OrderTransferObject.ProductionOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Order transfer bean. Used to create a process orders in PharmaSuite
 * 
 * @author Adasoft
 *
 */
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = false)
public class PvBomTransferObject extends BasePayload implements Serializable {

    private static final long serialVersionUID = 245678367863L; //ESTE SERIAL HAY QUE CAMBIARLO POR UNO QUE NO EXISTA

    /** the IDoc document number or message id */
    // IDOC.EDI_DC40.DOCNUM
    private String idoc;
    
    // IDOC.ZE1PVBOM_HEADER
    private PVBOMHeader pvbom;
    
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PVBOMHeader implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        // ZE1PVBOM_HEADER.MATNR
        private String material;
        
        // ZE1PVBOM_HEADER.VERID
        private String ProdVersion;
        
        // ZE1PVBOM_HEADER.WERKS
        private String plantCode;
        
        // ZE1PVBOM_HEADER.STLAL
        private String alternativeBOM;
        
        // ZE1PVBOM_HEADER.BMENG
        private String baseQty;
        
        // ZE1PVBOM_HEADER.BMEIN
        private String uom;
        
        // ZE1PVBOM_HEADER.ZZPACK_LINE
        private String packLine;
        
        // ZE1PVBOM_HEADER.BSTMI
        private String lotSizeFrom;
        
        // ZE1PVBOM_HEADER.BSTMA
        private String lotSizeTo;
        
        // ZE1PVBOM_HEADER.ZE1PVBOM_ITEMS

        private List<PVBOMItem> PVBOMItems = new ArrayList<>();
    }
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class PVBOMItem implements Serializable {
        	private static final long serialVersionUID = 1L;	
        
        // ZE1PVBOM_ITEMS.POSNR
        private String position;
        
        // ZE1PVBOM_ITEMS.IDNRK
        private String bomMaterial;
        
        // ZE1PVBOM_ITEMS.POSTP
        private String itemCategory;
        
        // ZE1PVBOM_ITEMS.MENGE
        private String componentQty;
        
        // ZE1PVBOM_ITEMS.MEINS
        private String componentUom;
        
        // ZE1PVBOM_ITEMS.FMENG
        private String fixQty;
        
        // ZE1PVBOM_ITEMS.ALPGR
        private String alternativeItem;
        }  
}
