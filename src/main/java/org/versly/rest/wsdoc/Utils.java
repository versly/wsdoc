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

public class Utils {
    static final String SERIALIZED_RESOURCE_LOCATION = "org.versly.rest.wsdoc.web-service-api.ser";

    static String joinPaths(String lhs, String rhs) {

        // Mike Rawlins 2013-03-15 Don't append slash if right hand side is empty
        if (rhs == null || rhs.length() == 0) {
            return lhs;
        }

        while (lhs.endsWith("/"))
            lhs = lhs.substring(0, lhs.length() - 1);

        while (rhs.startsWith("/"))
            rhs = rhs.substring(1);

        return lhs + "/" + rhs;
    }
}
