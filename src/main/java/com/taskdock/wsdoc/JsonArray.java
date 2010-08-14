/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.io.PrintStream;

public class JsonArray extends AbstractJsonType {
    private JsonType elementType;

    public JsonArray(JsonType elementType) {
        this.elementType = elementType;
    }

    @Override
    public void writePlainText(PrintStream stream, int indent) {
        if (elementType instanceof JsonObject) {
            stream.print("[ ");
            elementType.writePlainText(stream, indent + 2);
            stream.println();
            indent(stream, indent);
            stream.print("]");
        } else {
            stream.print("[ ");
            elementType.writePlainText(stream, indent + 2);
            stream.print(" ]");
        }
    }
}
