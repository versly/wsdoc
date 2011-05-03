/*
 * Copyright (c) Versly, Inc. 2009-2010. All Rights Reserved.
 */

package com.versly.rest.wsdoc;

import java.io.Serializable;

public class JsonDict implements JsonType, Serializable {

    private JsonType keyType;
    private JsonType valType;

    public JsonDict(JsonType keyType, JsonType valType) {
        this.keyType = keyType;
        this.valType = valType;
    }

    public JsonType getKeyType() {
        return keyType;
    }

    public JsonType getValueType() {
        return valType;
    }
}
