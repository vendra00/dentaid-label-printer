package com.rockwell.custmes.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.datasweep.compatibility.client.Keyed;

/**
 * Used to specify the class against which a {@link com.rockwell.custmes.model.KeyedObject} that
 * is linked to a database table by the {@link com.rockwell.custmes.annotations.Table} annotation is mapped.
 *
 * @author rweinga
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectType {
    Class<? extends Keyed> type();
}
