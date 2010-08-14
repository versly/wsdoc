/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.taskdock.wsdoc;

import java.io.PrintStream;

public interface JsonType {

    void writePlainText(PrintStream stream, int indent);
}

