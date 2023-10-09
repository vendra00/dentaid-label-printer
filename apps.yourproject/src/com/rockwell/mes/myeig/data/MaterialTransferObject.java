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
 * Material Transfer. Used to create materials in PharmaSuite (via MATMAS04)
 * 
 * @author rroney
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MaterialTransferObject extends BasePayload implements Serializable {
	private static final long serialVersionUID = -8408828939024824549L;

	// IDOC document number or message id
	private String idoc;

	// List of materials
	private List<Material> materials;

	/**
	 * Material bean
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class Material implements Serializable {

		private static final long serialVersionUID = 1L;

		// E1MARAM.MATNR
		private String partNumber;
		// E1MARAM.MTART
		private String materialType;
		// E1MARAM.MEINS
		private String unitOfMeasure;

		// E1MARAM.MATKL
		private String materialGroup;
		// E1MARAM.TEMPB
		private String temperatureConditionsIndicator;
		// E1MARAM.MHDHB
		private String totalShelfLife;
		// E1MARAM.BEHVO
		private String containerRequirement;
		// E1MARAM.MSTAE
		private String materialStatus;

		// List of material texts: E1MAKTM
		private List<MaterialText> materialTexts = new ArrayList<MaterialText>();

		// List of material plants: E1MARCM
		private List<MaterialPlant> materialPlants = new ArrayList<MaterialPlant>();

		// List of material units: E1MARMM
		private List<MaterialUnitOfMeasure> materialUnits = new ArrayList<MaterialUnitOfMeasure>();

		// List of material additional data: ZE1_ADD_DATA
		private List<MaterialAdditionalData> materialAdditionalData = new ArrayList<MaterialAdditionalData>();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class MaterialText implements Serializable {
		private static final long serialVersionUID = 1L;

		private String description;
		private String languageCode;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class MaterialPlant implements Serializable {
		private static final long serialVersionUID = 1L;

		// E1MARCM.WERKS
		private String plant;
		// E1MARCM.MMSTA
		private String specificMaterialStatus;
		// E1MARCM.MATGR
		private String specificMaterialGrouping;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class MaterialUnitOfMeasure implements Serializable {
		private static final long serialVersionUID = 1L;

		// E1MARMM.MEINH
		private String alternativeUnitMeasure;
		// E1MARMM.UMREZ
		private String numeratorBaseUnits;
		// E1MARMM.UMREN
		private String denominatorBaseUnits;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class MaterialAdditionalData implements Serializable {
		private static final long serialVersionUID = 1L;

		// ZE1_ADD_DATA.T143-TBTXT
		private String temperatureConditionDescription;
		// ZE1_ADD_DATA.T144-BVTXT
		private String containerRequirementDescription;
		// ZE1_ADD_DATA.GHS_SYMBOL
		private String ghsSymbols;
		// ZE1_ADD_DATA.H_CODE
		private String hazardCodes;
		// ZE1_ADD_DATA.P_CODE
		private String precautionaryCodes;
		// ZE1_ADD_DATA.SIGNAL_WORD
		private String signalWord;
		// ZE1_ADD_DATA.OEB
		private String oeb;
		// ZE1_ADD_DATA.MA_COUNTRY
		private String maCountry;
		// ZE1_ADD_DATA.MA_STRENGTH
		private String maStrength;
		// ZE1_ADD_DATA.MA_PACKAGING_TYPE
		private String maPackagingType;
	}
}
