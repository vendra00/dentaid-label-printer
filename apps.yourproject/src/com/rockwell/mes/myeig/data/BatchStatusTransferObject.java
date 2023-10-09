package com.rockwell.mes.myeig.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.rockwell.integration.messaging.BasePayload;

/**
 * Batch transfer bean. Used to create a batch in PharmaSuite
 * <p>
 * 
 */
public class BatchStatusTransferObject extends BasePayload implements Serializable {

    /** The <code>serialVersionUID</code> */    
    private static final long serialVersionUID = -2915992933949394302L;

    /** the IDoc document number or message id */
    private String idoc;
    
	/** List of materials */
	private List<BatchMove> batchmoves = new ArrayList<BatchMove>();
    
	/**
     * @return Returns the IDoc document number
     */
    public String getIdoc() {
        return idoc;
    }

    /**
     * @param idoc Sets the IDoc document number
     */
    public void setIdoc(final String idoc) {
        this.idoc = idoc;
    }

	/**
	 * @return Returns the list of batch moves
	 */
	public List<BatchMove> getBatchMoves() {
		return batchmoves;
	}

	/**
	 * @param batchmoves
	 *            Sets the list of batch moves
	 */
	public void setBatchMoves(final List<BatchMove> batchmoves) {
		this.batchmoves = batchmoves;
	}
    
	/**
	 * BatchMove bean
	 * <p>
	 * 
	 */	
	public static class BatchMove implements Serializable {
    
		/** The <code>serialVersionUID</code> */
		private static final long serialVersionUID = 1L;
		
	    /** the batch number */
	    private String batch;

	    /** the material number */
	    private String material;

	    /** the move type */
	    private String moveType;
		
	    /**
	     * @return Returns the batch.
	     */
	    public String getBatch() {
	        return batch;
	    }
	
	    /**
	     * @param batch The batch to set.
	     */
	    public void setBatch(String batch) {
	        this.batch = batch;
	    }
	
	    /**
	     * @return Returns the material.
	     */
	    public String getMaterial() {
	        return material;
	    }
	
	    /**
	     * @param material The material to set.
	     */
	    public void setMaterial(String material) {
	        this.material = material;
	    }
	
	    /**
	     * @return Returns the expiryDate.
	     */
	    public String getMoveType() {
	        return moveType;
	    }
	
	    /**
	     * @param expiryDate The expiryDate to set.
	     */
	    public void setMoveType(String moveType) {
	        this.moveType = moveType;
	    }

	}
}
