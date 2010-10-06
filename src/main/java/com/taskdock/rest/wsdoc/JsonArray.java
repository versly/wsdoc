/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.rest.wsdoc;

import java.io.Serializable;

public class JsonArray implements JsonType, Serializable {
    private JsonType elementType;

    public JsonArray(JsonType elementType) {
        this.elementType = elementType;
    }

    public JsonType getElementType() {
        return elementType;
    }
}
