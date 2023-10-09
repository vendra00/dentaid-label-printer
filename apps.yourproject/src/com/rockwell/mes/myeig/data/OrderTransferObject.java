package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Order transfer bean. Used to create a process orders in PharmaSuite
 * 
 * @author rroney
 *
 */
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = false)
public class OrderTransferObject extends BasePayload implements Serializable {

    private static final long serialVersionUID = 8845248952380834188L;

    /** the IDoc document number or message id */
    // IDOC.EDI_DC40
    private String idoc;

    // IDOC.E1AFKOL
    private Order order;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Order implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.AUFNR
        private String orderNumber;

        // E1AFKOL.AUART
        private String orderType;

        // E1AFKOL.MATNR
        private String materialIdentifier;

        // E1AFKOL.WERKS
        private String plantCode;

        // E1AFKOL.GAMNG
        private String targetQuantity;

        // E1AFKOL.GMEIN
        private String unitOfMeasurement;

        // E1AFKOL.GSTRP
        private String basicStartDate;

        // E1AFKOL.GLTRP
        private String basicEndDate;

        // E1AFKOL.PSPEL
        private String wbsElement;

        // E1AFKOL.E1AFPOL
        private List<OrderItem> orderItems = new ArrayList<>();

        // E1AFKOL.E1JSTKL
        private List<OrderStatus> orderStatus = new ArrayList<>();

        // E1AFKOL.E1AFFLL
        private List<ProductionOrder> productionOrder = new ArrayList<>();

        // E1AFKOL.ZE1PRO_ADD_DATA
        private OrderAdditionalData orderAdditionalData;

        // E1AFKOL.ZE1PRO_CPS_PACK
        private OrderCpsPack orderCpsPack;

        // E1AFKOL.ZE1PRO_CPS_COMP
        private OrderCpsComponent orderCpsComponent;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderItem implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.E1AFPOL.CHARG
        private String batchName;

        // E1AFKOL.E1AFPOL.VERID
        private String productionVersion;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderStatus implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.E1JSTKL.STAT
        private String objectStatus;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ProductionOrder implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.E1AFFLL.E1AFVOL
        private List<Operation> operations = new ArrayList<>();
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Operation implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL
        private List<OrderReservation> reservations;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderReservation implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.MATNR
        private String materialNumber;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.POSNR
        private String bomPosition;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.RSNUM
        private String reservationNumber;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.RSPOS
        private String reservationPosition;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.BDMNG
        private String quantity;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.MEINS
        private String unitOfMeasure;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.CHARG
        private String bomBatch;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.SOBKZ
        private String specialStockIndicator;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.LGORT
        private String storageLocation;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.LGNUM
        private String warehouseNumber;

        // E1AFKOL.E1AFFLL.E1AFVOL.E1RESBL.PRVBE
        private String productSupplyArea;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderAdditionalData implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZSTABSAMPAPPL
        private String stabilitySample;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZSTABSAMPLEQTY
        private String stabilitySampleQuantity;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZREFSAMPLAPPL
        private String referenceSample;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZREFSAMPLEQTY
        private String referenceSampleQuantity;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZOTHSAMPAPPL
        private String otherSample;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZOTHSAMPLEQTY
        private String otherSampleQuantity;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZNRSR
        private String nrsrNumber;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZLICHA
        private String customerBatch;

        // E1AFKOL.ZE1PRO_ADD_DATA.PRUEFLOS
        private String inspectionLot;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZZPACKLINE
        private String packagingLine;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZCUS_VFDAT
        private String customerExpiryDate;

        // E1AFKOL.ZE1PRO_ADD_DATA.ZCUS_MANUFDAT
        private String customerManufacturingDate;

        // E1AFKOL.ZE1PRO_ADD_DATA.GS1_ELEMENT_STRING
        private String gs1ElementString;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderCpsPack implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1AFKOL.ZE1PRO_CPS_PACK.Z_CPS_VERSION
        private String cpsVersion;

        // E1AFKOL.ZE1PRO_CPS_PACK.ZZSERIALIZATION_REL
        private String serializationRelevancy;

        // E1AFKOL.ZE1PRO_CPS_PACK.ZZAGGR_BOX
        private String aggregationBox;

        // E1AFKOL.ZE1PRO_CPS_PACK.ZZAGGR_PALLET
        private String aggregationPallet;

        // E1AFKOL.ZE1PRO_CPS_PACK.BOXES_P_OUTERBOX
        private String numberOfBoxesPerOuterbox;

        // E1AFKOL.ZE1PRO_CPS_PACK.OUTBOX_P_PALLET
        private String numberOfBoxesPerPallet;

        // E1AFKOL.ZE1PRO_CPS_PACK.UNITS_P_PALLET
        private String numberOfUnitsPerPallet;

        // E1AFKOL.ZE1PRO_CPS_PACK.PRODUCT_COMMENTS
        private String productComments;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderCpsComponent implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private String crossPerforation;

        private String laetusCodeLeaflet;

        private String laetusCodeBox;

        private String markBoxBatchNumber;

        private String markBoxExpiryDate;

        private String markBoxPrice;

        private String markBoxManufacturingDate;

        private String markBoxOthers;

        private String markBoxSerialNumber;

        private String markBoxProductCode;

        private String valueBoxProductCode;

        private String sequenceBoxBatchNumber;

        private String sequenceBoxExpiryDate;

        private String sequenceBoxPrice;

        private String sequenceBoxManufacturingDate;

        private String sequenceBoxOthers;

        private String sequenceBoxSerialNumber;

        private String sequenceBoxProductCode;

        private String markLabelBatchNumber;

        private String markLabelExpiryDate;

        private String markLabelPrice;

        private String markLabelManufacturingDate;

        private String markLabelOthers;

        private String markLabelSerialNumber;

        private String markLabelProductCode;

        private String valueLabelProductCode;

        private String sequenceLabelBatchNumber;

        private String sequenceLabelExpiryDate;

        private String sequenceLabelPrice;

        private String sequenceLabelManufacturingDate;

        private String sequenceLabelOthers;

        private String sequenceLabelSerialNumber;

        private String sequenceLabelProductCode;

        private String formatCPS;

        private String customerItemNo;
    }
}
