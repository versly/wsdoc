/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.versly.rest.wsdoc;

public class Utils {
    static final String SERIALIZED_RESOURCE_LOCATION = "web-service-api.ser";

    static String joinPaths(String lhs, String rhs) {
        while (lhs.endsWith("/"))
            lhs = lhs.substring(0, lhs.length() - 1);

        while (rhs.startsWith("/"))
            rhs = rhs.substring(1);

        return lhs + "/" + rhs;
    }
}
