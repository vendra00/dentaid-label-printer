package com.rockwell.custmes.services.order.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datasweep.compatibility.client.ControlRecipe;
import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.INamedUDA;
import com.datasweep.compatibility.client.MFC;
import com.datasweep.compatibility.client.MasterRecipe;
import com.datasweep.compatibility.client.MeasuredValue;
import com.datasweep.compatibility.client.OrderStep;
import com.datasweep.compatibility.client.OrderStepInput;
import com.datasweep.compatibility.client.OrderStepOutput;
import com.datasweep.compatibility.client.Part;
import com.datasweep.compatibility.client.ProcessOrderItem;
import com.datasweep.plantops.common.constants.IOrderStepInputTypes;
import com.datasweep.plantops.common.constants.IOrderStepOutputTypes;
import com.rockwell.custmes.services.order.ifc.IEnhancedOrderExplosionService;
import com.rockwell.mes.commons.base.ifc.choicelist.IMESChoiceElement;
import com.rockwell.mes.commons.base.ifc.exceptions.MESException;
import com.rockwell.mes.commons.base.ifc.functional.MeasuredValueUtilities;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAMFC;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStepInput;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAOrderStepOutput;
import com.rockwell.mes.commons.base.ifc.nameduda.MESNamedUDAPart;
import com.rockwell.mes.commons.base.ifc.services.ServiceFactory;
import com.rockwell.mes.commons.base.ifc.utility.MesClassUtility;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder;
import com.rockwell.mes.myeig.service.ifc.ErpOrderBuilder.ErpOrderItemBuilder;
import com.rockwell.mes.myeig.service.impl.OutboundMessageService;
import com.rockwell.mes.services.commons.ifc.order.OSOPositionStatus;
import com.rockwell.mes.services.order.ifc.EnumOrderStepInputStatus;
import com.rockwell.mes.services.order.ifc.OrderUtils;
import com.rockwell.mes.services.order.impl.OrderExplosionService;
import com.rockwell.mes.services.recipe.ifc.IMESRecipeService;
import com.rockwell.mes.services.recipe.ifc.weighing.EnumWeighingMethods;
import com.rockwell.mes.services.s88.ifc.BomUtility;
import com.rockwell.mes.services.s88.ifc.MESMaterialUtility;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMasterRecipe;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMaterialParameter;
import com.rockwell.mes.services.s88.ifc.recipe.IMESMaterialParameter.TYPE;
import com.rockwell.mes.services.s88.ifc.recipe.IMESOperation;
import com.rockwell.mes.services.s88.ifc.recipe.IMESPhase;
import com.rockwell.mes.services.s88.ifc.recipe.IMESProcedure;
import com.rockwell.mes.services.s88.ifc.recipe.IMESUnitProcedure;
import com.rockwell.mes.services.s88.ifc.recipe.PlannedQuantityMode;
import com.rockwell.mes.services.s88.impl.recipe.MESMasterRecipe;

/**
 * Order explosion service extended for replacing default material with the line items received from SAP
 * 
 * @author Administrator
 *
 */
public class EnhancedOrderExplosionService extends OrderExplosionService implements IEnhancedOrderExplosionService {

	/** Logger */
	private final Logger log = LoggerFactory.getLogger(EnhancedOrderExplosionService.class);

    /** Variable to hold the order builder received from the SAP */
	private ErpOrderBuilder orderBuilder;

	/** List to hold the order item components received from the SAP */
	private List<ErpOrderItemBuilder> orderItemBuilder;

	/** Map to hold the mapping of the unit procedure and unit procedure material params */
	private Map<IMESUnitProcedure, List<IMESMaterialParameter>> positionUnitProcedureMatParameter = new LinkedHashMap<>();

	/** In order to associate OSI to OSO we need to store the created OSI */
	private Map<Long, OrderStepInput> param2OsiMap = new HashMap<>();

	/** This map is used to associated an OSO to its successor OSI */
	private Map<Long, List<OrderStepOutput>> transfersMap = new HashMap<>();

	/**Lis to hold the MFC of the recipe */
	private List<MFC> mfcs = new ArrayList<>();

	/** The final OSO for non dispensing unit procedures */
	private OrderStepOutput finalOso = null;

	/** This list contains the params that have been already created by custom OSI */
	private List<Long> paramsToNotCreate = new ArrayList<>();

	/** This map is used to associate the OSO to its material */
	private Map<OrderStepOutput, Part> transferOsoMaterialMapping = new HashMap<>();

	/** This map is used to hold the OSO to its associated input position */
	private Map<OrderStepOutput, String> osoPositionMapping = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	protected void explCreateOSIAndOSO(ProcessOrderItem poi, List<OrderStep> ordersteps, Map<String, MeasuredValue> arg2,
			Map<String, MeasuredValue> arg3, MeasuredValue arg4) throws DatasweepException, MESException {

        // clear the global variables for the cache purpose
		positionUnitProcedureMatParameter.clear();
		param2OsiMap.clear();
		transfersMap.clear();
		mfcs.clear();
		finalOso = null;
		paramsToNotCreate.clear();
		transferOsoMaterialMapping.clear();
		osoPositionMapping.clear();

        int intPosition = 0;// RM: Inicializar variable local

		// Get the control recipe from the process order item

        ControlRecipe controlRecipe = OrderUtils.getControlRecipe(poi);// RM:11/ENE/2023<-- Devuelve el numero de Orden

		// Get the master recipe from the control recipe
        MasterRecipe masterRecipe = controlRecipe.getMasterRecipe();// RM:11/ENE/2023<-- Devuelve el identificador de la
                                                                    // receta.

		// If dummy bom found for the master recipe immediately return
		if (BomUtility.isDummyBOM(masterRecipe.getProcessBOM())) {
			return;
		}

        // If order type is NOT ZPCK (Without DummyBOM) standard explosion will be called.
		// RM:
        if (orderBuilder != null || orderBuilder == null) {
			super.explCreateOSIAndOSO(poi, ordersteps, arg2, arg3, arg4);
			return;
		}

        // If the order IS DummyBOM, the positions of the input material will be substituted by Loipro file
		// Create order step map associated with the routes
		Map<String, OrderStep> orderStepMap = this.createRouteStepName2OrderStepsMap(ordersteps);

		// Get the MFC of the recipe
		mfcs = masterRecipe.getMFCs();

		// Create a line item mapping
		Map<Integer, ErpOrderItemBuilder> orderLineItems = createMappingOfOrderRelatedLineItems();

		// Create a tree map to sort the line items with the positions
		TreeMap<Integer, ErpOrderItemBuilder> sortedOrderLineItems = new TreeMap<>(orderLineItems);

		// Create a map to hold the additional position for the material replacement
		Map<Integer, List<Integer>> additionalPositions = createAddtionalPositionsForReplacement(sortedOrderLineItems);

		// Initialize the material parameters from the recipe
		initMaterialParameters(masterRecipe);
		log.info("Creating standard OSIs and OSOs");

		// Map to hold the association of the material parameter with its unit procedure
		Map<IMESUnitProcedure, List<IMESMaterialParameter>> allParams = positionUnitProcedureMatParameter;

        // Sort the UP as per the dispense flag set to true to init the transfer
        List<IMESUnitProcedure> sortedUPList = sortValues(allParams);
        for (IMESUnitProcedure unitProc : sortedUPList) {
            // Get the order step from the route step
            OrderStep orderStep = orderStepMap.get(unitProc.getName());
            // Flag to check is unit procedure is for dispense
            Boolean isDispense = unitProc.getIsWeighAndDispense();
            // Get the list of material parameters for the unit procedure
            List<IMESMaterialParameter> params = allParams.get(unitProc);
            // If the unit procedure is not for dispensing operations we immediately create the final oso
            if (!isDispense) {
                // Create a final OSO
                createFinalOso(params, orderStep, poi);
            }

			// Iterate the material parameters
			for (IMESMaterialParameter param : params) {
				// Get the position of the param
				String position = param.getMFCPosition();
                // RM: Si el mterial tiene "_" separo el "_" y meto las dos partes
                // en un array
                // AFH 21/02/2023 revisar este código creo que falla en las ordenes de pack
                if (position.contains("_")) {
                String positionSplit[] = position.split("_", 2);
                    // RM: Como los 4 primeros numeros son iguales, ordeno los mismos materiales
                    // por la segunda parte del array
                    // AFH 28/02/2023 Revisar lógica sin bucle for
                    // for (int i = 1; i < positionSplit.length; i++) {
                    // if ((positionSplit[i].length() < 4)) {
                    intPosition = Integer.valueOf(positionSplit[0]);
                    // }
                    // }
                }
                // RM: Si el material no tiene "_" procedo de manera normal.
                else {
                    intPosition = Integer.valueOf(position);
                }
				// Flag to check is the material parameter is transfer
				boolean isTransfer = isTransfer(param);

				// Check is the position exists in the line items map for the customization
				if (sortedOrderLineItems.containsKey(intPosition)) {
					// Check for the position, replacement additional positions exits.
					// eg. for the bom position at 0010, additional position exists in the line items as 0011, 0012 ---- 0019
					if (additionalPositions.containsKey(intPosition) && additionalPositions.get(intPosition) != null 
							&&  !additionalPositions.get(intPosition).isEmpty()) {
						// Iterate the additional position
						for (Integer additionalPosition : additionalPositions.get(intPosition)) {
							try {
								// Create an osi and oso
								createCustomOSIOSO(poi, orderStep, param, isDispense, sortedOrderLineItems, isTransfer, additionalPosition);
								// Find the output parameter for the material parameter for which osi should not be created 
								findParamToNotCreate(param);
							} catch (DatasweepException | MESException e) {
								log.error("Error creating osi {}", e.getMessage());
								throw new MESException(e);
							}
						}
						// If no additional position exists create an OSI for the material
					} else {
						try {
							// Create an osi and oso
							createCustomOSIOSO(poi, orderStep, param, isDispense, sortedOrderLineItems, isTransfer, intPosition);
							// Find the output parameter for the material parameter for which osi should not be created 
							findParamToNotCreate(param);
						} catch (DatasweepException | MESException e) {
							log.error("error creating osi {}", e.getMessage());
							throw new MESException(e);
						}
					}
					// If material position not exists in the line item, create a standard or transfer OSI
				}/* else {
					
					
					boolean isInput = param.getUsageType() == IOrderStepInputTypes.INPUT_TYPE_INPUT;
					// Check if the material param usage type is input
					if (isInput) {
						try {
							// Create a standard or transfer OSI for the material parameter
							createStandardOSI(poi, orderStep, param, isTransfer, isDispense, Collections.emptyMap());
						} catch (DatasweepException | MESException e) {
							log.error("error creating osi {}", e.getMessage());
							throw new MESException(e);
						}
						// Create standard OSO
					} else if (!paramsToNotCreate.contains(param.getKey())) {
						// The output param is not custom and could be created
						OrderStepInput osi = findOsi(param);
						createStandardOSO(orderStep, osi, param, isTransfer, null);
					}
				}*/
				
			}
		}
	}

	/**
     * Method to sort the UP's as per the dispense flag
     * 
     * @param map
     * @return
     */
    private List<IMESUnitProcedure> sortValues(Map<IMESUnitProcedure, List<IMESMaterialParameter>> map) {
        List<IMESUnitProcedure> sortedList = new ArrayList<>();
        List<IMESUnitProcedure> list = new ArrayList(map.keySet());
        List<IMESUnitProcedure> dispenseUPList =
                list.stream().filter(item -> Boolean.TRUE.equals(item.getIsWeighAndDispense())).collect(Collectors.toList());
        if (dispenseUPList != null && !dispenseUPList.isEmpty()) {
            sortedList.addAll(dispenseUPList);
        }
        List<IMESUnitProcedure> nonDispenseUPList =
                list.stream().filter(item -> Boolean.FALSE.equals(item.getIsWeighAndDispense())).collect(Collectors.toList());
        if (nonDispenseUPList != null && !nonDispenseUPList.isEmpty()) {
            sortedList.addAll(nonDispenseUPList);
        }
        return sortedList;
    }

    /**
     * Method to create additional positions for the replacements
     * 
     * @param sortedOrderLineItems - Line items from the IDOC
     * @return Map
     */
	private Map<Integer, List<Integer>> createAddtionalPositionsForReplacement(
			TreeMap<Integer, ErpOrderItemBuilder> sortedOrderLineItems) {

		// Get the line item replacement position in multiple of 10
		Map<Object, Object> orderLineItemsWithReplacementPositions = sortedOrderLineItems.entrySet().stream()
				.filter(map -> Integer.valueOf(map.getKey()).intValue() %10 == 0)
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

		// Create a sorted map
		TreeMap<Object, Object> orderLineItemsReplacementPositions = new TreeMap<Object, Object>(orderLineItemsWithReplacementPositions); 

		// Get the additional line items which are not multiple of 10
		Map<Object, Object> orderLineItemsWithAdditionalPositions = sortedOrderLineItems.entrySet().stream()
				.filter(map -> Integer.valueOf(map.getKey()).intValue() %10 != 0)
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

		// Create a sorted map
		TreeMap<Object, Object> orderLineItemsAdditionalPositions = new TreeMap<Object, Object>(orderLineItemsWithAdditionalPositions); 

		// Create a map to put the replacement positions
		Map<Integer, List<Integer>> additionalPositionsMap = new HashMap<>();

		// Create a list to hold the line items for the replacement of the original item
		List<Object> lineItemsWithReplacementPositions = orderLineItemsReplacementPositions.keySet().stream().
				collect(Collectors.toCollection(ArrayList::new));

		// Get the last position of the list
		Integer lastPosition = (Integer) lineItemsWithReplacementPositions.get(lineItemsWithReplacementPositions.size() - 1);

		// Iterate the line items
		for (int i = 0 ; i < lineItemsWithReplacementPositions.size(); i++) {
			// Create a list to hold the additional positions
			List<Integer> additionalPositions = new ArrayList<>();
			// Get the next position
			Integer nextBomPosition =  (Integer) lineItemsWithReplacementPositions.get(i+1 >= lineItemsWithReplacementPositions.size() ? i : i+1);
			// Get the current position
			Integer currentBomPosition = (Integer) lineItemsWithReplacementPositions.get(i);

			// Iterate the additional position map
			for (Map.Entry<Object, Object> entry : orderLineItemsAdditionalPositions.entrySet()) {
				Integer positionKey = (Integer) entry.getKey();
				if (currentBomPosition.equals(lastPosition) && positionKey > currentBomPosition) {
					additionalPositions.add(positionKey);
				} else if (positionKey > currentBomPosition && positionKey < nextBomPosition) {
					additionalPositions.add(positionKey);
				}
			}
			if (!additionalPositions.isEmpty()) {
				additionalPositionsMap.put(currentBomPosition, additionalPositions);
            }
            if (!additionalPositions.contains(currentBomPosition)) {
                additionalPositions.add(currentBomPosition);
                additionalPositionsMap.put(currentBomPosition, additionalPositions);
            }
		}
		return additionalPositionsMap;
	}

	@Override
	public void setOrderBuilder(ErpOrderBuilder orderBuilder) {
		this.orderBuilder = orderBuilder;
		this.orderItemBuilder = orderBuilder.getComponents();
	}

	/**
	 * Method to create route step mapping
	 * @param orderSteps - List of order steps
	 * @return Map containing the association of the route step to order step
	 */
	private Map<String, OrderStep> createRouteStepName2OrderStepsMap(List<OrderStep> orderSteps) {
		Map<String, OrderStep> map = new HashMap<>();
		for (OrderStep os : orderSteps) {
			map.put(os.getRouteStep().getName(), os);
		}
		return map;
	}

	/**
	 * Method to create a mapping of the order related line items
	 * @return Map containing the position and its respective line item
	 */
	private Map<Integer, ErpOrderItemBuilder> createMappingOfOrderRelatedLineItems() {
		Map<Integer, ErpOrderItemBuilder> orderLineItemsByPositionMap = new HashMap<>();
		if (orderItemBuilder != null) {
			orderItemBuilder.stream().forEach(item -> orderLineItemsByPositionMap.put(Integer.valueOf(item.getPosition()), item));
		}
		return orderLineItemsByPositionMap;
	}

	/**
	 * Method to initialize the material parameter
	 * @param mr - Master recipe
	 * @throws DatasweepException - throws if any occurs
	 */
	private void initMaterialParameters(MasterRecipe mr) throws DatasweepException {
		// Get the MES master recipe
		IMESMasterRecipe imesMr = new MESMasterRecipe(mr);
		// Get all the procedures
		List<IMESProcedure> procs = imesMr.getAllProcedures();
		for (IMESProcedure proc : procs) {
			// Get all the unit procedures
			List<IMESUnitProcedure> unitProcs = proc.getAllUnitProcedures();
			for (IMESUnitProcedure unitProc : unitProcs) {
				// Create a list to hold the positions for the up
				List<IMESMaterialParameter> positions = new ArrayList<>();
				positionUnitProcedureMatParameter.put(unitProc, positions);
				// Get all operations of the UP
				List<IMESOperation> ops = unitProc.getAllOperations();
				for (IMESOperation op : ops) {
					// Get all phases of the operations
					List<IMESPhase> phases = op.getAllPhases();
					for (IMESPhase phase : phases) {
						// Iterate the material params and add in the list
						for (IMESMaterialParameter iMESMaterialParameter : phase.getMaterialParameters()) {
							positions.add(iMESMaterialParameter);
						}
					}
				}
			}
		}
	}

	/**
	 * Method to find is material parameter is a transfer or not
	 * @param param - Material parameter
	 * @return true, if param is transfer
	 */
	private boolean isTransfer(IMESMaterialParameter param) {
		// Iterate the mfc items of the recipe
		for (MFC mfc : mfcs) {
			// Get the material in key
			Long inParamKey = MESNamedUDAMFC.getMaterialInParameterKey(mfc);
			// Get the material out key
			Long outParamKey = MESNamedUDAMFC.getMaterialOutParameterKey(mfc);

			// If the param has been found as predecessor/successor but it has no successor/predecessor -> it's not a transfer
			// If the param has been found as predecessor/successor but it has a successor/predecessor -> it is a transfer
			if (param.getType() == TYPE.INPUT && inParamKey != null && param.getKey() == inParamKey) {
				return outParamKey != null;
			}
			if (param.getType() == TYPE.OUTPUT && outParamKey != null && param.getKey() == outParamKey) {
				return inParamKey != null;
			}
		}
		return false;
	}

	/**
	 * Method to create customized OSI and OSI
	 * @param poi - Process order Item
	 * @param orderStep - Order step
	 * @param param - Material parameter
	 * @param isDispense - Flag for dispense UP
	 * @param sortedOrderLineItems - Map containing order line items
	 * @param isTransfer - Boolean value
	 * @param additionalPosition - additional position for which OSI needs to be created
	 * @throws DatasweepException - throws if any occurs
	 * @throws MESException - throws if any occurs
	 */
	private void createCustomOSIOSO(ProcessOrderItem poi, OrderStep orderStep, IMESMaterialParameter param, boolean isDispense, TreeMap<Integer, 
			ErpOrderItemBuilder> sortedOrderLineItems, boolean isTransfer, Integer additionalPosition) throws DatasweepException, MESException {
        // Get the MFC item from the material parameter
        MFC mfc = findMfc(param);
        if (mfc != null) {
            // Get the order line item builder
            ErpOrderItemBuilder orderLineItem = sortedOrderLineItems.get(additionalPosition);
            // Get the char for the OSI name, T : Transfer and I : Input
            String inputOrTrans = isTransfer ? "T" : "I";
            // Assign the input type of the order step
            int inputType = isTransfer ? IOrderStepInputTypes.INPUT_TYPE_INTERMEDIATE : IOrderStepInputTypes.INPUT_TYPE_INPUT;
            // Check if line item is not null
            if (orderLineItem != null) {
                // Get the position of the additional line item
                //String newPosition = orderLineItem.getPosition();
                String newPosition = param.getMFCPosition();
                
                // Create an OSI name
                String osiName = orderLineItem.getPosition() + "-" + inputOrTrans + "-01";
                log.info("Creating order step input {}", osiName);

                // Get the material of the line item
                Part part = orderLineItem.getPart();

                // Create OSI
                OrderStepInput osi = createOSI(part, osiName, inputType, newPosition, orderLineItem.getQuantity(), orderStep, param,
                        orderLineItem.getUdaMap(), isDispense);

                // Get the out mfc for the param

                MFC outMfc = null;
                if (isDispense || mfc.getSuccessor() != null) {
                    outMfc = mfc.getSuccessor();
                } else if (mfc.getSuccessor() == null) {
                    // Iterate the MFC
                    for (MFC mfcOp : mfcs) {
                        if (TYPE.OUTPUT.longValue() == mfcOp.getType()) {
                            outMfc = mfcOp;
                            break;
                        }
                    }
                }

                // The oso could be a transfer material
                isTransfer = outMfc != null && outMfc.getType() == TYPE.TRANSFER.longValue();

                // If the unit procedure is for dispensing we create the oso otherwise we attach the osi to the final
                // oso
                // If the finalOso is still null it means we are in a non dispensing procedure with an output material
                // to transfer
                if (isDispense || finalOso == null) {
                    // Create a custom OSO for the OSI
                    createCustomOSO(orderStep, osi, newPosition, orderLineItem.getQuantity(), part, param.getPlannedQuantityMode(), isTransfer,
                            outMfc);
                } else {
                    // Associate the output with the input
                    osi.associateOrderStepOutput(finalOso);
                }
            }
        }
	}

	/**
	 * Method to create customized OSO
	 * @param orderStep - Order step
	 * @param osi - Order step input
	 * @param position - Position of the Input
	 * @param qty - Quantity of the line item
	 * @param part - Material of the line item
	 * @param qtyMode - Planned quantity mode
	 * @param isTransfer - Boolean is transfer
	 * @param outMfc - Output MFC
	 */
	public void createCustomOSO(OrderStep orderStep, OrderStepInput osi, String position, MeasuredValue qty, Part part, IMESChoiceElement qtyMode, boolean isTransfer, MFC outMfc) {
		// Get the char for the OSI name, T : Transfer and O : Output
		String outputOrTrans = isTransfer ? "T" : "O";
		// Create an OSO name
		String osoName = position + "-" + outputOrTrans + "-01";
		// Get the OSO type
		int mfcType = isTransfer ? IOrderStepOutputTypes.OUTPUT_TYPE_INTERMEDIATE : IOrderStepOutputTypes.OUTPUT_TYPE_OUTPUT;
		// Assign the OSI material as a output material
		Part outPart = part;

		log.info("Creating order step output {}", osoName);
		// Create and add the order step output to the order step
		OrderStepOutput oso = orderStep.addOrderStepOutput(outPart, mfcType);
		// Set the UDA values
		MESNamedUDAOrderStepOutput.setNumber(oso, osoName);
		MESNamedUDAOrderStepOutput.setPosition(oso, position);
		MESNamedUDAOrderStepOutput.setPlannedQuantityMode(oso, qtyMode);
		MESNamedUDAOrderStepOutput.setStatus(oso, EnumOrderStepInputStatus.OSI_STATUS_CREATED.getValue());
		MESNamedUDAOrderStepOutput.setTotalPositionStatus(oso, OSOPositionStatus.NOT_STARTED.longValue());
		oso.setPlannedQuantity(qty);
		MESNamedUDAOrderStepOutput.setPlannedQuantity(oso, qty);
		MESNamedUDAOrderStepOutput.setOriginalPlannedQuantity(oso, qty);
		MESNamedUDAOrderStepOutput.setQuantityFixed(oso, 0l);

		// Add the OSO position mapping in the map
		osoPositionMapping.put(oso, position);
		// Associate osi to oso
		osi.associateOrderStepOutput(oso);

		// If transfer add the OSO in the oso list and put it in the transfer map
		if (isTransfer && outMfc != null) {
			transferOsoMaterialMapping.put(oso, outPart);
			List<OrderStepOutput> osoList = transfersMap.get(outMfc.getKey());
			if (osoList == null) {
				osoList = new ArrayList<>();
			}
			osoList.add(oso);
			transfersMap.put(outMfc.getKey(), osoList);
		}
	}

	/**
	 * Method to find the OSI from the material parameter
	 * @param param - Material parameter
	 * @return OrderStepInput
	 */
	private OrderStepInput findOsi(IMESMaterialParameter param) {
		// Iterate the MFC
		for (MFC mfcOut : mfcs) {
			if (mfcOut.getType() == TYPE.INPUT.longValue()) {
				continue;
			}

			// Get the out param key
			Long outParamKey = MESNamedUDAMFC.getMaterialOutParameterKey(mfcOut);

			if (param.getKey() == outParamKey) {
				for (MFC mfcIn : mfcs) {
					if (mfcIn.getType() != TYPE.INPUT.longValue()) {
						continue;
					}

					if (mfcIn.getSuccessor().getKey() == mfcOut.getKey()) {
						Long inParamKey = MESNamedUDAMFC.getMaterialInParameterKey(mfcIn);
						return param2OsiMap.get(inParamKey);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method to find the MFC for the material parameter
	 * @param param - Material parameter
	 * @return MFC
	 */
	private MFC findMfc(IMESMaterialParameter param) {
		// Iterate the MFC
		for (MFC mfc : mfcs) {
			// Get the in key of MFC
			Long inParamKey = MESNamedUDAMFC.getMaterialInParameterKey(mfc);
			// Get the out key of MFC
			Long outParamKey = MESNamedUDAMFC.getMaterialOutParameterKey(mfc);

			if ((inParamKey != null && param.getKey() == inParamKey) || (outParamKey != null && param.getKey() == outParamKey)) {
				return mfc;
			}
		}
		return null;
	}

	/**
	 * Method to find the successor for the material parameter
	 * @param param - Material parameter
	 * @return MFC
	 */
	private MFC findSuccessor(IMESMaterialParameter param) {
		// Iterate the MFC
		for (MFC mfc : mfcs) {
			if (mfc.getType() != TYPE.TRANSFER.longValue()) {
				continue;
			}
			// Get the in key of MFC
			Long inParamKey = MESNamedUDAMFC.getMaterialInParameterKey(mfc);
			// Get the out key of MFC
			Long outParamKey = MESNamedUDAMFC.getMaterialOutParameterKey(mfc.getSuccessor());

			if (param.getKey() == inParamKey && outParamKey != null) {
				return mfc;
			}
		}
		return null;
	}

	/**
	 * Method to check the tolerances for the OSI
	 * @param poiName
	 * @param part
	 * @param osi
	 * @param position
	 * @throws DatasweepException
	 * @throws MESException
	 */
    private void checkTolerances(Part part, OrderStepInput osi, String position, IMESMaterialParameter param) {

        MeasuredValue lowerTolAbsolute = param.getLowerToleranceAbsolute();
        MeasuredValue upperTolAbsolute = param.getUpperToleranceAbsolute();
        MeasuredValue lowerTolRelative = param.getLowerToleranceRelative();
        MeasuredValue upperTolRelative = param.getUpperToleranceRelative();

        lowerTolAbsolute = lowerTolAbsolute != null ? lowerTolAbsolute : MESNamedUDAPart.getLowerToleranceAbsolute(part);
        upperTolAbsolute = upperTolAbsolute != null ? upperTolAbsolute : MESNamedUDAPart.getUpperToleranceAbsolute(part);
        lowerTolRelative = lowerTolRelative != null ? lowerTolRelative : MESNamedUDAPart.getLowerToleranceRelative(part);
        upperTolRelative = upperTolRelative != null ? upperTolRelative : MESNamedUDAPart.getLowerToleranceRelative(part);

        MESNamedUDAOrderStepInput.setLowerToleranceAbsolute(osi, lowerTolAbsolute);
        MESNamedUDAOrderStepInput.setUpperToleranceAbsolute(osi, upperTolAbsolute);
        MESNamedUDAOrderStepInput.setLowerToleranceRelative(osi, lowerTolRelative);
        MESNamedUDAOrderStepInput.setUpperToleranceRelative(osi, upperTolRelative);
	}

	/**
	 * Method to create standard or transfer OSI
	 * @param poi - Process order item
	 * @param orderStep - Order step
	 * @param param - Material prameter
	 * @param isTransfer - Boolean is transfer
	 * @param isDispense - Boolean is dispense
	 * @throws DatasweepException - throws if any occurs
	 * @throws MESException - throws if any occurs
	 */
	private void createStandardOSI(ProcessOrderItem poi, OrderStep orderStep, IMESMaterialParameter param, boolean isTransfer, boolean isDispense, Map<String, Object> udaMap) throws DatasweepException, MESException {
		// Get the char for the OSI name, T : Transfer and I : Input
		String inputOrTrans = isTransfer ? "T" : "I";
		// Create the OSI name
		String osiName = param.getMFCPosition() + "-" + inputOrTrans + "-01";
		// Initialize the quantity as null 
		MeasuredValue mv = null;
		// Initialize the material as null
		Part part = null;
		// Initialize the list of trasfer oso as null
		List<OrderStepOutput> transferOsos = null;
		// Check if it's a transfer
		if (isTransfer) {
			// Set the input type as intermediate
			int inputType = IOrderStepInputTypes.INPUT_TYPE_INTERMEDIATE;
			// Get the MFC for the material parameter
			MFC mfc = findMfc(param);
			// Get the list of transfer OSOs for the MFC
			transferOsos = transfersMap.get(mfc.getKey());
			// Check is dummy material assign for the parameter
			// If yes, trasnsfer osi needs to be created with out material of previous UP
			if (MESMaterialUtility.isDummyMaterial(param.getMaterial())) {
				if (transferOsos != null) {
					// Iterate the trsnafer oso
					for (OrderStepOutput transferOso : transferOsos) {
						// Get the position of the OSO transfer mapping
						String position = osoPositionMapping.get(transferOso);
						// Create the osi name
						osiName = position + "-" + inputOrTrans + "-01";
						// Get the material for transfer
						part = transferOsoMaterialMapping.get(transferOso);
						// Assign qty for the OSI
						MeasuredValue osiQuantity = PlannedQuantityMode.AS_PRODUCED.getChoiceElement() == param.getPlannedQuantityMode() ? transferOso.getPlannedQuantity() : param.getPlannedQuantity();
						// Create transfer osi
                        OrderStepInput osi =
                                createOSI(part, osiName, inputType, StringUtils.EMPTY, osiQuantity, orderStep, param, udaMap, isDispense);
						// If the unit procedure is for dispensing we create the oso otherwise we attach the osi to the final oso
                        if (!isDispense) {
							osi.associateOrderStepOutput(finalOso);
						}
						// Put the osi in the map
						param2OsiMap.put(param.getKey(), osi);

						// If transfer oso not null, associate osi with oso
						if (transferOsos != null) {
							transferOso.associateOrderStepInput(osi);
						}
					}
				}
			} else {
				// Get the parameter material 
				part = param.getMaterial();
				// Assign qty for the OSI
				if (param.getPlannedQuantityMode() == PlannedQuantityMode.AS_PRODUCED.getChoiceElement()) {
					if (isTransfer) {
						// If it is an input transfer we need to get the incoming quantity and set it as planned quantity
						for (OrderStepOutput transferOso : transferOsos) {
							if (mv == null) {
								mv = transferOso.getPlannedQuantity();
							} else {
								try {
									mv = (MeasuredValue) mv.add(transferOso.getPlannedQuantity());
								} catch (Exception e) {
									log.error("error calculating osi quantity", e);
								}
							}
						}
					} else {
						mv = MeasuredValueUtilities.createZero(MESNamedUDAPart.getUnitOfMeasure(part));
					}
				} else {
					mv = param.getPlannedQuantity();
				}
				// Create standard OSI
                OrderStepInput osi = createOSI(part, osiName, inputType, StringUtils.EMPTY, mv, orderStep, param, Collections.emptyMap(), isDispense);

				// If the unit procedure is for dispensing we create the oso otherwise we attach the osi to the final oso
                if (!isDispense) {
					osi.associateOrderStepOutput(finalOso);
				}

				// Put osi in the Map 
				param2OsiMap.put(param.getKey(), osi);
				// Associate osi with oso
				if (transferOsos != null) {
					for (OrderStepOutput transferOso : transferOsos) {
						transferOso.associateOrderStepInput(osi);
					}
				}
			}
		} else {
			int inputType = IOrderStepInputTypes.INPUT_TYPE_INPUT;
			log.info("creating order step input {}", osiName);
			MeasuredValue osiQuantity = PlannedQuantityMode.AS_PRODUCED.getChoiceElement() == param.getPlannedQuantityMode() ?  
					MeasuredValueUtilities.createZero(MESNamedUDAPart.getUnitOfMeasure(part)) : param.getPlannedQuantity();
            OrderStepInput osi = createOSI(param.getMaterial(), osiName, inputType, param.getMFCPosition(), osiQuantity, orderStep, param,
                    Collections.emptyMap(), isDispense);
					// If the unit procedure is for dispensing we create the oso otherwise we attach the osi to the
					// final oso
            if (!isDispense) {
						osi.associateOrderStepOutput(finalOso);
					}
					param2OsiMap.put(param.getKey(), osi);
		}
	}

	/**
	 * Method to create OSI and set the OSI values
	 * @param part - OSI material
	 * @param osiName - Name
	 * @param inputType - Input type 
	 * @param position - Position
	 * @param quantity - Quantity
	 * @param orderStep - Order Step
	 * @param param - Material parameter
	 * @return OrderStepInput
	 * @throws DatasweepException - throws if any occurs
	 */
    public OrderStepInput createOSI(Part part, String osiName, int inputType, String position, MeasuredValue quantity, OrderStep orderStep,
            IMESMaterialParameter param, Map<String, Object> udaMap, boolean isDispense) throws DatasweepException {
		OrderStepInput osi = orderStep.addOrderStepInput(part, quantity, inputType);
		MESNamedUDAOrderStepInput.setNumber(osi, osiName);
		MESNamedUDAOrderStepInput.setOriginOSIName(osi, osi.getName());
		MESNamedUDAOrderStepInput.setIsSpitCopy(osi, MesClassUtility.LONG_FALSE);
		MESNamedUDAOrderStepInput.setSplitNumber(osi, 0L);
		MESNamedUDAOrderStepInput.setTotalNumIdentifiedSublots(osi, 0L);
		MESNamedUDAOrderStepInput.setStatus(osi, EnumOrderStepInputStatus.OSI_STATUS_CREATED);
		MESNamedUDAOrderStepInput.setPlannedQuantityMode(osi, param.getPlannedQuantityMode());
		MESNamedUDAOrderStepInput.setAutomatedConsumption(osi, 0l);
		MESNamedUDAOrderStepInput.setIsMergedWithProductOutput(osi, 0l);
		MESNamedUDAOrderStepInput.setOriginalPlannedQuantity(osi, quantity);
		MESNamedUDAOrderStepInput.setPlannedPotency(osi, MeasuredValueUtilities.createMV("100", "%"));
		Long wm = param.getAllowedWeighingMethods();
		IMESChoiceElement defaultWm = MESNamedUDAPart.getDefaultWeighingMethod(part);
		MESNamedUDAOrderStepInput.setDefaultWeighingMethod(osi, defaultWm != null ? defaultWm : EnumWeighingMethods.NET_WEIGHING);
		MESNamedUDAOrderStepInput.setAllowedWeighingMethods(osi, wm != null ? wm : EnumWeighingMethods.NET_WEIGHING.getValue());
		MESNamedUDAOrderStepInput.setPosition(osi, position);
		MESNamedUDAOrderStepInput.setTotalPositionStatus(osi, 10l);
		MESNamedUDAOrderStepInput.setWeighingType(osi, param.getWeighingType());

        // If the unit procedure is dispense assign the tolerances to the OSI
        if (isDispense) {
            checkTolerances(part, osi, position, param);
        }

		if (udaMap != null && !udaMap.isEmpty()) {
			setUdasOnObject(osi, udaMap);
		}

		return osi;
	}

	/**
	 * Create standard OSO
	 * @param orderStep
	 * @param osi
	 * @param param
	 * @param isTransfer
	 * @param idocQty
	 */
	public void createStandardOSO(OrderStep orderStep, OrderStepInput osi, IMESMaterialParameter param, boolean isTransfer, MeasuredValue idocQty) {
		String outputOrTrans = isTransfer ? "T" : "O";

		String osoName = param.getMFCPosition() + "-" + outputOrTrans + "-01";
		MeasuredValue mv = null;

		// If the quantity is not null it means we are creating the final oso so we need to set the quantity
		// coming from the IDOC
		if (idocQty != null) {
			mv = idocQty;
		} else {
			mv = param.getPlannedQuantity();
		}

		int mfcType = isTransfer ? IOrderStepOutputTypes.OUTPUT_TYPE_INTERMEDIATE : IOrderStepOutputTypes.OUTPUT_TYPE_OUTPUT;
		log.info("creating order step output {}", osoName);

		OrderStepOutput oso = orderStep.addOrderStepOutput(param.getMaterial(), mfcType);
		MESNamedUDAOrderStepOutput.setNumber(oso, osoName);
		MESNamedUDAOrderStepOutput.setPosition(oso, param.getMFCPosition());
		MESNamedUDAOrderStepOutput.setPlannedQuantityMode(oso, param.getPlannedQuantityMode());
		MESNamedUDAOrderStepOutput.setStatus(oso, EnumOrderStepInputStatus.OSI_STATUS_CREATED.getValue());
		MESNamedUDAOrderStepOutput.setTotalPositionStatus(oso, OSOPositionStatus.NOT_STARTED.longValue());
		oso.setPlannedQuantity(mv);
		MESNamedUDAOrderStepOutput.setPlannedQuantity(oso, mv);
		MESNamedUDAOrderStepOutput.setOriginalPlannedQuantity(oso, mv);
		MESNamedUDAOrderStepOutput.setQuantityFixed(oso, 0l);
		if (osi != null) {
			osi.associateOrderStepOutput(oso);
		} else {
			finalOso = oso;
		}

		if (isTransfer) {
			MFC successor = findSuccessor(param);
			if (successor != null) {
				List<OrderStepOutput> osoList = transfersMap.get(successor.getKey());
				if (osoList == null) {
					osoList = new ArrayList<>();
				}
				osoList.add(oso);
				transfersMap.put(successor.getKey(), osoList);
			}

		}
	}

	/**
	 * Method to create final OSO
	 * @param params
	 * @param orderStep
	 * @param poi
	 */
	private void createFinalOso(List<IMESMaterialParameter> params, OrderStep orderStep, ProcessOrderItem poi) {
		for (IMESMaterialParameter param : params) {
            if (param.getType() == TYPE.OUTPUT) {
				boolean isTransfer = isTransfer(param);
				if (isTransfer) {
					// if the output is a transfer it will be created later. Only the real output needs to be created
					continue;
				}

                // Part materialPart = ServiceFactory.getService(IMESRecipeService.class).getPart("121304");
                // AÑADIDO PARA PROBAR DUMMY EN OSO
                Part materialPart = orderBuilder.getPart();

                param.setMaterial(materialPart);
                // Antes se le pasaba como parametro poi.getQuantity(), vamos a probar con orderBuider.
                // Original
                // createStandardOSO(orderStep, null, param, false, poi.getQuantity());
                // Modificado
                createStandardOSO(orderStep, null, param, false, orderBuilder.getQuantity());
				paramsToNotCreate.add(param.getKey());
				return;
            }
		}
	}

	/**
	 * Method to find the parameters which are not required to be created.
	 * @param param
	 */
	private void findParamToNotCreate(IMESMaterialParameter param) {
		for (MFC mfc : mfcs) {
			if (mfc.getType() != TYPE.INPUT.longValue()) {
				continue;
			}

			Long inParamKey = MESNamedUDAMFC.getMaterialInParameterKey(mfc);
			Long outParamKey = MESNamedUDAMFC.getMaterialOutParameterKey(mfc.getSuccessor());

			if (param.getKey() == inParamKey) {
				paramsToNotCreate.add(outParamKey);
				break;
			}
		}
	}

	/**
	 * Set the UDA values of the Map to a named UDA Object.
	 * 
	 * @param object - The object to be changed
	 * @param theUdas - Map of values for UDAs Key is name of UDA
	 * @throws DatasweepException - thrown when an error occurs
	 */
	public static void setUdasOnObject(INamedUDA object, Map<String, Object> theUdas) throws DatasweepException {

		for (Map.Entry<String, Object> entry : theUdas.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if ((key.startsWith("X_") || key.startsWith("ct_"))
					&& !Objects.equals(object.getUDA(key), value)) {
				object.setUDA(value, key);
			}
		}
	}
}
