/*
 * Copyright (c) Versly, Inc. 2009-2010. All Rights Reserved.
 */

package com.versly.rest.wsdoc;

import java.io.Serializable;

public class JsonRecursiveObject implements JsonType, Serializable {
    private String recursedObjectTypeName;

    public JsonRecursiveObject(String recursedObjectTypeName) {
        this.recursedObjectTypeName = recursedObjectTypeName;
    }

    public String getRecursedObjectTypeName() {
        return recursedObjectTypeName;
    }
}
