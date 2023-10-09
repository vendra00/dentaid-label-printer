package com.rockwell.custmes.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface State {
    /**
     * @return FSM key
     */
    String fsmName();

    /**
     * @return Column name containing the object key for this state
     */
    String keyName();

    /**
     * @return A value from the IObjectTypes enumeration.
     */
    short type();
}
