/*
 * Copyright 2011 TaskDock, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.versly.rest.wsdoc.impl;

import java.io.Serializable;
import java.util.*;

public class JsonObject implements JsonType, Serializable {

    private transient Set<String> ignoreProperties = new HashSet<>();

    public void ignoreProperty(String name) {
        ignoreProperties.add(name);
    }

    public boolean isIgnoringProperty(String name) {
        return ignoreProperties.contains(name);
    }

    private List<JsonField> _fields = new ArrayList<>();

    public <T extends JsonType> JsonField addField(String fieldName, T value) {
        JsonField<T> field = new JsonField<>(fieldName, value);
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
