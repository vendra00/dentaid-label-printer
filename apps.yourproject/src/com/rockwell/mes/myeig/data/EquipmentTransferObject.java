package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;

/**
 * Sublot transfer bean. Used to manage sublots in PharmaSuite (Customer customization)
 * 
 * @author tmedina, (Adasoft).
 */
public class EquipmentTransferObject extends BasePayload implements Serializable {

	/** The <code>serialVersionUID</code> */    
    private static final long serialVersionUID = -2279890335693930697L;

    /** the idoc */
    private List<IDOC> idocs;
    
    public List<IDOC> getIdocs() {
		return idocs;
	}

	public void setIdocs(List<IDOC> idocs) {
		this.idocs = idocs;
	}
	
	
	public static class IDOC implements Serializable {
		/** The <code>serialVersionUID</code> */
		private static final long serialVersionUID = 6395378370809352069L;

		/** the DOC number */
	    private String docNum;
	    
	    /** the batch number */
	    private String code;

		/** the sublot ID */
	    private String description;

		/** the sublot Quantity */
	    private String status;

	    /**
	     * @return Returns the docNum.
	     */
	    public String getDocNum() {
	        return docNum;
	    }

	    /**
	     * @param docNum The docNum to set.
	     */
	    public void setDocNum(String docNum) {
	        this.docNum = docNum;
	    }
	    
	    public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("EquipmentTransferObject::IDOC: ");
			sb.append("docNum=" + docNum + ", ");
			sb.append("code=" + code + ", ");
			sb.append("description=" + description + ", ");
			sb.append("status=" + status + ", ");
			return sb.toString();
		}
	    
	    
	}

}
