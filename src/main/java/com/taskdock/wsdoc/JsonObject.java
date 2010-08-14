/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class JsonObject extends AbstractJsonType {

    private List<JsonField> _fields = new ArrayList();

    public void writePlainText(PrintStream stream, int indent) {
        stream.println();
        indent(stream, indent);
        stream.println("{");
        for (int i = 0; i < _fields.size(); i++) {
            String sep = i == _fields.size() - 1 ? "" : ",";
            _fields.get(i).writePlainText(stream, indent + 2, sep);
            stream.println();
        }
        indent(stream, indent);
        stream.print("}");
    }
    
    public <T extends JsonType> JsonField addField(String fieldName, T value) {
        JsonField<T> field = new JsonField(fieldName, value);
        _fields.add(field);
        return field;
    }

    public class JsonField<T extends JsonType> {

        private String fieldName;
        private T value;

        public JsonField(String fieldName, T value) {
            this.fieldName = fieldName;
            this.value = value;
        }

        public void writePlainText(PrintStream stream, int indent, String sep) {
            indent(stream, indent);
            stream.printf("%s: ", fieldName);
            value.writePlainText(stream, indent + 2);
            stream.print(sep);

            if (value instanceof JsonPrimitive) {
                JsonPrimitive p = (JsonPrimitive) value;
                if (p.getRestrictions() != null)
                    stream.printf(" // one of %s", p.getRestrictions());
            }
        }
    }
}
