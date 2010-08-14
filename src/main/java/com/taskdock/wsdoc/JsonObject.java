/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonObject implements JsonType {

    private List<JsonField> _fields = new ArrayList();

    public <T extends JsonType> JsonField addField(String fieldName, T value) {
        JsonField<T> field = new JsonField(fieldName, value);
        if (_fields.contains(field))
            throw new IllegalStateException("field " + fieldName + " is already defined");
        _fields.add(field);
        return field;
    }

    public Collection<JsonField> getFields() {
        return _fields;
    }

    public class JsonField<T extends JsonType> {

        private String fieldName;
        private T fieldType;

        public JsonField(String fieldName, T value) {
            this.fieldName = fieldName;
            this.fieldType = value;
        }

        public String getFieldName() {
            return fieldName;
        }

        public T getFieldType() {
            return fieldType;
        }
    }
}
