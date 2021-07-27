package it.digifox03.reselect.api;

import net.minecraft.util.Identifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated method MUST return an {@link Identifier}
 * and it CANNOT have generic parameters or return type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReselectMethod {
}
