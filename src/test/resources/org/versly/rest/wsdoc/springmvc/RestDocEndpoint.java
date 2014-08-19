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

package org.versly.rest.wsdoc.springmvc;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.versly.rest.wsdoc.RestApiMountPoint;
import org.versly.rest.wsdoc.model.ParameterizedTypeReferrer;

@RestApiMountPoint("/mount")
public class RestDocEndpoint {

    /**
     * This is an exciting JavaDoc comment
     */
    @RequestMapping(value = "foo/{dateParam}/{intParam}", method = RequestMethod.GET)
    public Map<String,List<ExcitingReturnValue>> methodWithJavadocAndInterestingArgs(HttpServletRequest req,
        @PathVariable("dateParam") java.util.Date dp,
        @PathVariable int intParam) {
        return null;
    }

    @RequestMapping(value = "voidreturn", method = RequestMethod.GET)
    public void methodWithVoidReturn(HttpServletRequest req) {
    }

    @RequestMapping(value = "voidreturn", method = RequestMethod.GET, params = { "param0", "param1" })
    public void methodWithVoidReturnAndParams(HttpServletRequest req,
                                              @RequestParam("param0") int p0,
                                              @RequestParam int param1) {
    }

    @RequestMapping(value = "multipart", method = RequestMethod.GET)
    public void methodWithVoidReturn(MultipartHttpServletRequest req) {
    }

    @RequestMapping(value = "uuidReturn", method = RequestMethod.GET)
    public UUID uuidReturn(MultipartHttpServletRequest req) {
        return null;
    }

    @RequestMapping(value="recursiveParam", method = RequestMethod.POST)
    public void recursiveParam(HttpServletRequest req,
                               @RequestParam("recursive") ValueWithRecursion recursive)
    {
    }

    @RequestMapping(value="recursiveReturn", method = RequestMethod.GET)
    public ValueWithRecursion recursiveReturn(HttpServletRequest req)
    {
        return null;
    }

    @RequestMapping(value="recursiveListReturn", method = RequestMethod.GET)
    public @ResponseBody ValueWithListRecursion recursiveListReturn(HttpServletRequest req)
    {
        return null;
    }

    @RequestMapping(value="endpointWithParameterizedType", method = RequestMethod.GET)
    public ParameterizedTypeReferrer endpointWithParameterizedType() {
        return null;
    }

    @RequestMapping(value = { "multiple-bindings-a", "multiple-bindings-b" }, method = RequestMethod.GET)
    public void multipleBindings() {
    }

    public class ExcitingReturnValue {
        /**
         * The exciting return value's date!
         */
        public Date getDate() {
            return null;
        }

        public void setDate(Date date) { // here to exercise a setter bug
        }
    }

    public class ValueWithRecursion {
        public ValueWithRecursion getOther() {
            return null;
        }

        public void setOther(ValueWithRecursion other) {
        }
    }

    public class ValueWithListRecursion {

        public String getStringValue() {
            return null;
        }

        public void setStringValue(String val) {
        }

        public List<ValueWithListRecursion> getOthers() {
            return null;
        }

        public void setOthers(List<ValueWithRecursion> others) {
        }
    }
}
