package com.partners.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface UserAction {
	UserPermissionEnum Action() default UserPermissionEnum.ALLOWALL;  
	String Description() default "";
}
