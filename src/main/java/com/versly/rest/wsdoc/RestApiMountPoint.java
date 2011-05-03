/*
 * Copyright (c) Versly, Inc. 2009-2010. All Rights Reserved.
 */

package com.versly.rest.wsdoc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>Documents the expected REST URL path to which the {@link org.springframework.web.bind.annotation.RequestMapping}s
 * in this class will be mounted, presumably by a servlet configuration etc. This should not include the hostname.</p>
 * <br/>
 * <p>For example: <code>@RestApiMountPoint("/api/v1/users/")</code>.</p>
 */
@Target(ElementType.TYPE)
public @interface RestApiMountPoint {
    String value();
}
