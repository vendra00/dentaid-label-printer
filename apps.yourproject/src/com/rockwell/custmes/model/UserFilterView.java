package com.rockwell.custmes.model;

import com.datasweep.compatibility.client.NamedFilter;
import com.datasweep.plantops.common.utility.IDBSchema;
import com.rockwell.custmes.annotations.Column;
import com.rockwell.custmes.annotations.Id;
import com.rockwell.custmes.annotations.ObjectType;
import com.rockwell.custmes.annotations.Table;

@Table(name = IDBSchema.DS_OBJECT_TABLE_NAME)
@ObjectType(type = NamedFilter.class)
public class UserFilterView implements KeyedObject {
    private long key;
    private String name;
    private String objectClass;

    /**
     * @return the key
     */
    @Id
    @Column(name = IDBSchema.DS_OBJECT_OBJECT_KEY)
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
     * @return the name
     */
    @Column(name = IDBSchema.DS_OBJECT_OBJECT_NAME)
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    @Column(name = IDBSchema.DS_OBJECT_OBJECT_CLASS)
    public String getObjectClass() {
        return objectClass;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }
}
