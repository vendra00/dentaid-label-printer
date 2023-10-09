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
 * 
 * Batch Transfer. Used to create materials in PharmaSuite (via BATMAS03)
 * 
 * @author rroney
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BatchTransferObject extends BasePayload implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // IDOC document number or message id
    private String idoc;

    // List of materials E1BATMAS
    private List<Material> materials = new ArrayList<Material>();

    /**
     * Material bean
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Material implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1BATMAS.MATNR
        private String materialNumber;

        // E1BATMAS.CHARGE
        private String batchNumber;

        // DUDA RICARDO
        // private String batch;

        // List of material texts: E1BATCHATT
        private List<BatchAttribute> batchAttributes = new ArrayList<>();

        // List of material texts: E1BATCHSTATUS
        private List<BatchStatus> batchStatus = new ArrayList<>();

        // List of material texts: ZE1BATADDDATA
        private List<BatchAddtionalData> batchAdditionalData = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchAttribute implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1BATCHATT.EXPIRYDATE
        private String expirationDate;

        // E1BATCHATT.NEXTINSPEC
        private String nextInspectionDate;

        // E1BATCHATT.PROD_DATE
        private String productionDate;

        // E1BATCHATT.VENDORBATCH
        private String vendorBatch;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchStatus implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // E1BATCHSTATUS.RESTRICTED
        private String restricted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchAddtionalData implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        // ZE1BATADDDATA.PLANT
        private String plant;

        // ZE1BATADDDATA.BQMPOTENCY
        private String potency;

        // ZE1BATADDDATA.BQMPOTENCYCL
        private String potencyCl;
    }
}
