package com.rockwell.custmes.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify the foreign key column for a property of a {@link com.rockwell.custmes.model.KeyedObject} that
 * is linked to another database table.
 *
 * @author rweinga
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JoinTable {
    String name();

    String alias() default "";
}
