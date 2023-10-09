package com.rockwell.custmes.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify the database column for a property belonging to a {@link com.rockwell.custmes.model.KeyedObject} that
 * is linked to a database table by the {@link com.rockwell.custmes.annotations.Table} annotation.
 *
 * @author rweinga
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Column {
    String name();
}
