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

package org.versly.rest.wsdoc.jaxrs;

import org.versly.rest.wsdoc.RestApiMountPoint;
import org.versly.rest.wsdoc.model.ParameterizedTypeReferrer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestApiMountPoint("/mount")
public class RestDocEndpoint {

    /**
     * This is an exciting JavaDoc comment
     */
    @GET
    @Path("foo/{dateParam}/{intParam}")
    public Map<String,List<ExcitingReturnValue>> methodWithJavadocAndInterestingArgs(
        @Context HttpServletRequest req,
        @PathParam("dateParam") Date dp,
        @PathParam("intParam") int intParam) {
        return null;
    }

    @GET
    @Path("voidreturn")
    public void methodWithVoidReturn(@Context HttpServletRequest req) {
    }

    @GET
    @Path("voidreturn")
    public void methodWithVoidReturnAndParams(@Context HttpServletRequest req,
                                              @QueryParam("param0") int p0,
                                              @QueryParam("param1") int param1) {
    }

    @GET
    @Path("uuidReturn")
    public UUID uuidReturn() {
        return null;
    }

    @POST
    @Path("recursiveParam")
    public void recursiveParam(@Context HttpServletRequest req,
                               @QueryParam("recursive") ValueWithRecursion recursive)
    {
    }

    @GET
    @Path("recursiveReturn")
    public ValueWithRecursion recursiveReturn(@Context HttpServletRequest req)
    {
        return null;
    }

    @GET
    @Path("recursiveListReturn")
    public ValueWithListRecursion recursiveListReturn(HttpServletRequest req)
    {
        return null;
    }

    @GET
    @Path("endpointWithParameterizedType")
    public ParameterizedTypeReferrer endpointWithParameterizedType() {
        return null;
    }

    @POST
    @Path("recursiveListRequestBody")
    public void recursiveListReturn(ValueWithListRecursion body)
    {
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
