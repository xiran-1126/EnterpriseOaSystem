package com.oa.common;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String moduleName() default "";
    BusinessType businessType() default BusinessType.OTHER;
    boolean saveRequestParam() default true;
    boolean saveResponseData() default true;
}
