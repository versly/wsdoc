/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.io.PrintStream;

abstract class AbstractJsonType implements JsonType {

    protected void indent(PrintStream stream, int count) {
        for (int i = 0; i < count; i++) {
            stream.print(" ");
        }
    }
}
