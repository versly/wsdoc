/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.rest.wsdoc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonObject implements JsonType, Serializable {

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

    public class JsonField<T extends JsonType> implements Serializable {

        private String fieldName;
        private T fieldType;
        private String commentText;

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

        public void setCommentText(String commentText) {
            this.commentText = commentText;
        }

        public String getCommentText() {
            return commentText;
        }
    }
}
