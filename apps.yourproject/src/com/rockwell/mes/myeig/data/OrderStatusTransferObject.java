package com.rockwell.mes.myeig.data;

import java.io.Serializable;

import com.rockwell.integration.messaging.BasePayload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = false)
public class OrderStatusTransferObject extends BasePayload implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String idoc;

    private E1ZProstat e1Zprostat;

    @Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = false)
    public static class E1ZProstat implements Serializable {

        private static final long serialVersionUID = -1L;

        private String testRun;

        private String ze1ProStatus;

        private String insplot;

        private String statFinished;

        private String statProdReviewed;

        private String statQaReviewed;
    }
}
