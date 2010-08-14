/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.io.PrintStream;

public class JsonDict extends AbstractJsonType {

    private JsonType keyType;
    private JsonType valType;

    public JsonDict(JsonType keyType, JsonType valType) {
        this.keyType = keyType;
        this.valType = valType;
    }

    @Override
    public void writePlainText(PrintStream stream, int indent) {
        if (keyType instanceof JsonObject || valType instanceof JsonObject) {
            stream.print("[ ");
            keyType.writePlainText(stream, indent + 2);
            stream.println("->");
            valType.writePlainText(stream, indent + 2);
            stream.println();
            indent(stream, indent);
            stream.print("]");
        } else {
            stream.print("[ ");
            keyType.writePlainText(stream, indent + 2);
            stream.print(" -> ");
            valType.writePlainText(stream, indent + 2);
            stream.print(" ]");
        }
    }
}
