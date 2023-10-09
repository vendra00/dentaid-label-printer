package com.rockwell.custmes.model;

import com.datasweep.compatibility.client.Part;
import com.datasweep.plantops.common.utility.IDBSchema;
import com.rockwell.custmes.annotations.Column;
import com.rockwell.custmes.annotations.Id;
import com.rockwell.custmes.annotations.ObjectType;
import com.rockwell.custmes.annotations.Table;

@Table(name = IDBSchema.PART_TABLE_NAME)
@ObjectType(type = Part.class)
public class PartView implements KeyedObject {
    private long key;
    private String identifier;
    private String description;

    /**
     * @return the key
     */
    @Id
    @Column(name = IDBSchema.PART_PART_KEY)
    @Override
    public long getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    @Override
    public void setKey(long key) {
        this.key = key;
    }

    /**
     * @return the identifier
     */
    @Column(name = IDBSchema.PART_PART_NUMBER)
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the description
     */
    @Column(name = IDBSchema.PART_DESCRIPTION)
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
