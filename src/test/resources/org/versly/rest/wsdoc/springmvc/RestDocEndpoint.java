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
@RequestMapping("/api/v1")
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

    @RequestMapping(value = "voidreturn1", method = RequestMethod.GET)
    public void methodWithVoidReturn(HttpServletRequest req) {
    }

    /**
     * Returns void
     * @param req Nothing interesting
     * @param p0 The integer request parameter 0
     * @param param1 The integer request parameter 1
     */
    @RequestMapping(value = "voidreturn2", method = RequestMethod.GET, params = { "param0", "param1" })
    public void methodWithVoidReturnAndParams(HttpServletRequest req,
                                              @RequestParam("param0") int p0,
                                              @RequestParam int param1) {
    }

    @RequestMapping(value = "pojoQueryParams", method = RequestMethod.GET)
    public void methodWithPojoArgs(PojoValue pojoQueryParams) {
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

    public class PojoValue {

        public String getQueryParamVal1() {
            return null;
        }

        public int getQueryParamVal2() {
            return 1;
        }

        /**
         * The first query param
         */
        public void setQueryParamVal1(String str) {
        }

        public void setQueryParamVal2(int i) {
        }
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

    // this following widges/{id1}/gizmos endpoints are for verifying issue #29

    /**
     * This endpoint creates things.
     * @param id1 The widget identifier.
     */
    @RequestMapping(value = "widgets/{id1}/gizmos", method = RequestMethod.POST)
    public void createThing(@PathVariable("id1") String id1) {
    }

    /**
     * This endpoint gets things.
     * @param id1 The widget identifier.
     * @param id2 The gizmo identifier.
     */
    @RequestMapping(value = "widgets/{id1}/gizmos/{id2}", method = RequestMethod.GET)
    public void getThing(@PathVariable("id1") String id1, @PathVariable("id2") String id2) {
    }

    /**
     * This endpoint Deletes things.
     * @param id1 The widget identifier.
     * @param id2 The gizmo identifier.
     */
    @RequestMapping(value = "widgets/{id1}/gizmos/{id2}", method = RequestMethod.DELETE)
    public void deleteThing(@PathVariable("id1") String id1, @PathVariable("id2") String id2) {
    }

    enum Colors {
        RED,
        GREEN,
        BLUE
    };

    /**
     * This endpoint tests query parameters that are enums.
     *
     * @param color Whirlygig color of interest.
     */
    @RequestMapping(value = "/whirlygigs", method = RequestMethod.GET)
    public void getWhirlygig(@RequestParam("color") Colors color) {
    }
    
    /**
     * This endpoint tests uri path parameters that are enums.
     *
     * @param color The color of interest.
     */
    @RequestMapping(value = "/colors/{color}", method = RequestMethod.GET)
    public void getColor(@PathVariable("color") Colors color) {
    }
}
