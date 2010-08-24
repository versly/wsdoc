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

import javax.servlet.http.HttpServletRequest;

@RestApiMountPoint("/mount")
public class RestDocEndpoint {
    /**
     * This is an exciting JavaDoc comment
     */
    @RequestMapping(value = "foo/{dateParam}", method = RequestMethod.GET)
    public Map<String,List<Date>> methodWithJavadocAndInterestingArgs(HttpServletRequest req, 
        @PathVariable java.util.Date dateParam) {
        return null;
    }
}
