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
 * Stock Report. Used to create stocks in PharmaSuite (via MATMAS04)
 * 
 * @author rroney
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SublotTransferObject extends BasePayload implements Serializable {

    private static final long serialVersionUID = -2279890335693930697L;

    private String idoc;

    private List<Stock> stock = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Stock implements Serializable {

        private static final long serialVersionUID = 1L;
        private String plant;
        private List<Detail> details = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Detail implements Serializable {

        private static final long serialVersionUID = 1L;
        private String sublot;
        private String material;
        private String batch;
        private String quantity;
        private String unitOfMeasure;
        private String isoUnitOfMeasure;
    }
}
