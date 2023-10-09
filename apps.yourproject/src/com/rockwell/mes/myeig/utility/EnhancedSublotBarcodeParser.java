// -------------------------------------------------------------
//   $RCSfile: EnhancedSublotBarcodeParser.java,v $
//    $Source: /var/cvs/ftps/projects/core/src/java/com/rockwell/ssb/ftps/services/inventory/impl/Attic/EnhancedSublotBarcodeParser.java,v $
//      $Date: 2018/03/23 14:49:50 $
//    $Author: ylaubert $
//  $Revision: 1.1.2.1 $
//
//  (c) Copyright 2016 Rockwell Automation Technologies, Inc. All Rights Reserved.
// -------------------------------------------------------------
package com.rockwell.mes.myeig.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.datasweep.compatibility.client.Batch;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.Sublot;
import com.datasweep.compatibility.client.SublotFilter;
import com.rockwell.library.commons.parser.gs1.GS1AIPair;
import com.rockwell.library.commons.parser.gs1.GS1ApplicationIdentifier;
import com.rockwell.library.commons.parser.gs1.GS1Parser;
import com.rockwell.library.commons.parser.gs1.GS1ParserResult;
import com.rockwell.library.commons.parser.gs1.exception.GS1ParserException;
import com.rockwell.mes.commons.base.ifc.exceptions.MESInvalidBarcodeException;
import com.rockwell.mes.commons.base.ifc.services.PCContext;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.services.inventory.ifc.IBatchService;
import com.rockwell.mes.services.inventory.ifc.ISublotBarcodeParser;
import com.rockwell.mes.services.inventory.ifc.ISublotService;
import com.rockwell.mes.services.inventory.impl.SublotBarcodeParser;
import com.rockwell.mes.services.recipe.ifc.IMESRecipeService;
import com.rockwell.mes.services.wip.ifc.IOrderStepExecutionService;
import com.rockwell.mes.services.wip.ifc.OrderStepInputSublot;

/**
 * Support GS1 DataMatrix sublot barcodes
 *
 * @author ccassan, (c) Copyright 2017 Rockwell Automation Technologies, Inc. All Rights Reserved.
 */
public class EnhancedSublotBarcodeParser extends SublotBarcodeParser implements ISublotBarcodeParser {

    /** logger */
    private static final Log LOGGER = LogFactory.getLog(EnhancedSublotBarcodeParser.class);

    // GS symbol (group separator) ASCII=29
    private static final char GS = '\u001D';

    // $ (Dollar sign) ASCII=36
    private static final char DOLLAR_SIGN = '\u0024';

    // EXPIRICY_DATE_PREFIX -> 17
    private static final String EXPIRICY_DATE_PREFIX = "17";

    private static final IBatchService BATCH_SERVICE = ServiceFactory.getService(IBatchService.class);

    private static final IMESRecipeService MES_RECIPE_SERVICE = ServiceFactory.getService(IMESRecipeService.class);

    private static final ISublotService SUBLOT_SERVICE = ServiceFactory.getService(ISublotService.class);

    /** redefinition of private {@link SublotBarcodeParser#SUBLOT_IDENTIFIER_CHAR} */
    private static final char SUBLOT_IDENTIFIER_CHAR = 's';

    /** redefinition of private {@link SublotBarcodeParser#BATCH_IDENTIFIER_CHAR} */
    private static final char BATCH_IDENTIFIER_CHAR = 'b';

    /** redefinition of private {@link SublotBarcodeParser#PART_IDENTIFIER_CHAR} */
    private static final char PART_IDENTIFIER_CHAR = 'p';

    /** CONSTANT to define length for weighing barcodes */
    private static final int WEIGH_BARCODE_LENGHT = 17;

    @Override
    public Map<String, String> getMatchingBarcodeTemplateMap(List<String> barcodes, List<String> templates)
            throws MESInvalidBarcodeException {

        // Strategy: To keep impact small, we convert GS1 barcodes into non-GS1 barcodes as supported by product.
        List<String> newBarcodes = new ArrayList<>();
        for (String barcode : barcodes) {

            // barcode could start with GS or FNC1 : remove it
            char prefix = barcode.charAt(0);

            // Hay 2 posibles códigos de barras que pueden identificar:
            // 1: MateriaPrima de Pesaje: 0000123456789011 -> Se identifican porque tienen 16 carácteres
            // 2: Bulto pesado de Pharmasuite: $SL00000198 9999 6015010 -> Se identifican porque empiezan por $

            if (prefix == DOLLAR_SIGN) {
                // 2: Bulto pesado de Pharmasuite
                barcode = generateDollarBarcode(barcode);
                newBarcodes.add(barcode);

            } else if (barcode.length() < WEIGH_BARCODE_LENGHT) {
                // 1: Bulto pesado de Pharmasuite
                List<String> matriculaBarcodes = generateWeighBarcode(barcode);
                if (matriculaBarcodes != null) {
                    newBarcodes.addAll(matriculaBarcodes); // Aunque devuelva una lista de sublotes, sólo el primero se
                                                           // procesará
                }

            }
        }

        return super.getMatchingBarcodeTemplateMap(newBarcodes, templates);
    }

    private List<String> generateWeighBarcode(String barcode) {
        // 1: Matricula de Weighing -> Formato: 0000123456789011
        List<String> outputBarcodes = null;

        if (!StringUtils.isEmpty(barcode)) {
            SublotFilter sublotFilter = PCContext.getFunctions().createSublotFilter();
            sublotFilter = sublotFilter.forNameEqualTo(barcode.trim());
            List<Sublot> sublots = PCContext.getFunctions().getFilteredSublots(sublotFilter);

            if (sublots != null && !sublots.isEmpty()) {
                outputBarcodes = new ArrayList<String>();
                for (Sublot sublot : sublots) {
                    outputBarcodes.add(generateNewSublotBarcodeString(sublot));
                }

            } else {
                LOGGER.error("ERROR!! No se ha podido recuperar el sublote para el ID --> '" + barcode + ".");
            }
        } else {
            LOGGER.error("ERROR!! el código de barras leído está vacío.");
        }

        return outputBarcodes;
    }
    public static String left(String s, int size) {
        return s.substring(0, Math.min(size, s.length()));
    }


    private String generateDollarBarcode(String inputBarcode) {
        // 2: Bulto pesado de Pharmasuite -> Formato: $SL00000104 9999 6015010
        String material = StringUtils.EMPTY;
        String batch = StringUtils.EMPTY;
        String sublotnumber = StringUtils.EMPTY;
        String outputBarcode = null;

        String barcode = inputBarcode.substring(1); //

        String[] parts = barcode.split("\\s{1,}"); // Divido el barcode en 3 partes separandolos por espacios

        // sublotnumber = parts[0].substring(0, 10);
        sublotnumber = parts[0];
        batch = parts[1];
        material = parts[2];

        Sublot sublot = null;
        String batchCompoundIdentifier = BATCH_SERVICE.createBatchCompoundIdentifier(batch, material);
        sublot = SUBLOT_SERVICE.loadSublot(batchCompoundIdentifier, sublotnumber);

        if ((sublot != null) && !StringUtils.isEmpty(batch)) {
            String batchName = BATCH_SERVICE.retrieveBatchIdentifier(sublot.getBatch());
            if (!StringUtils.equals(batchName, batch) || !StringUtils.equals(material, sublot.getPart().getPartNumber())) {
                LOGGER.error("Los datos recuperados desde el BATCH_SERVICE no se corresponden con los datos extraídos del código de barras.");
            }
        }

        if (sublot != null) {
            outputBarcode = generateNewSublotBarcodeString(sublot);
        }
        return outputBarcode;
    }

    public String generateNewSublotBarcodeString(Sublot sublot) {

        final Map<Character, String> hashMap = prepareHashMap(sublot.getName(),
                BATCH_SERVICE.retrieveBatchIdentifier(sublot.getBatch()), sublot.getPartNumber());

        final List<String> defaultTemplate = super.getConfiguredBarcodeTemplates();
        return super.generateBarcodeString(defaultTemplate.get(0), hashMap);
    }

    /** @see SublotBarcodeParser#prepareHashMap(Sublot, Batch, Part) */
    private Map<Character, String> prepareHashMap(String identifierSublot, String identifierBatch,
            String identifierPart) {
        final Map<Character, String> hashMap = new HashMap();

        hashMap.put(SUBLOT_IDENTIFIER_CHAR, identifierSublot);
        hashMap.put(BATCH_IDENTIFIER_CHAR, identifierBatch);
        hashMap.put(PART_IDENTIFIER_CHAR, identifierPart);

        return hashMap;
    }

}
