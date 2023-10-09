package com.rockwell.custmes.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify the foreign table column against which a {@link com.rockwell.custmes.annotations.JoinTable} is
 * linked.
 *
 * @author rweinga
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JoinCondition {

    String joinColumn();

    String column() default "";

    String table() default "";
}
