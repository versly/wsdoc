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

package org.versly.rest.wsdoc;

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
