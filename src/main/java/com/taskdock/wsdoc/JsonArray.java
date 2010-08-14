/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

public class JsonArray implements JsonType {
    private JsonType elementType;

    public JsonArray(JsonType elementType) {
        this.elementType = elementType;
    }

    public JsonType getElementType() {
        return elementType;
    }
}
