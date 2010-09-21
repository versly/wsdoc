/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.util.Map;
import java.util.List;
import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

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
}
