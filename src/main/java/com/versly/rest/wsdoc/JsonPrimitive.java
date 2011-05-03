/*
 * Copyright (c) Taskdock, Inc. 2009-2010. All Rights Reserved.
 */

package com.versly.rest.wsdoc;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

public class JsonPrimitive implements JsonType, Serializable {

    private static final Map<String, String> _primitiveTypeNamesByJavaTypeName = new HashMap();

    static {
        _primitiveTypeNamesByJavaTypeName.put(Object.class.getName(), "object");
        _primitiveTypeNamesByJavaTypeName.put(String.class.getName(), "string");

        _primitiveTypeNamesByJavaTypeName.put(boolean.class.getName(), "boolean");
        _primitiveTypeNamesByJavaTypeName.put(Boolean.class.getName(), "boolean");
        _primitiveTypeNamesByJavaTypeName.put(byte.class.getName(), "byte");
        _primitiveTypeNamesByJavaTypeName.put(Byte.class.getName(), "byte");
        _primitiveTypeNamesByJavaTypeName.put(char.class.getName(), "char");
        _primitiveTypeNamesByJavaTypeName.put(Character.class.getName(), "char");
        _primitiveTypeNamesByJavaTypeName.put(double.class.getName(), "double");
        _primitiveTypeNamesByJavaTypeName.put(Double.class.getName(), "double");
        _primitiveTypeNamesByJavaTypeName.put(float.class.getName(), "float");
        _primitiveTypeNamesByJavaTypeName.put(Float.class.getName(), "float");
        _primitiveTypeNamesByJavaTypeName.put(int.class.getName(), "int");
        _primitiveTypeNamesByJavaTypeName.put(Integer.class.getName(), "int");
        _primitiveTypeNamesByJavaTypeName.put(long.class.getName(), "long");
        _primitiveTypeNamesByJavaTypeName.put(Long.class.getName(), "long");
        _primitiveTypeNamesByJavaTypeName.put(short.class.getName(), "short");
        _primitiveTypeNamesByJavaTypeName.put(Short.class.getName(), "short");
        _primitiveTypeNamesByJavaTypeName.put(void.class.getName(), "void");
        _primitiveTypeNamesByJavaTypeName.put(Void.class.getName(), "void");

        _primitiveTypeNamesByJavaTypeName.put(URL.class.getName(), "url");
        _primitiveTypeNamesByJavaTypeName.put(UUID.class.getName(), "uuid");

        _primitiveTypeNamesByJavaTypeName.put(Date.class.getName(), "timestamp");
        _primitiveTypeNamesByJavaTypeName.put(java.sql.Date.class.getName(), "date");
        _primitiveTypeNamesByJavaTypeName.put(java.sql.Time.class.getName(), "time");
        _primitiveTypeNamesByJavaTypeName.put(Timestamp.class.getName(), "timestamp");
        _primitiveTypeNamesByJavaTypeName.put(DateTime.class.getName(), "iso-8610 timestamp string");
    }

    private String typeName;
    private List<String> restrictions;

    public JsonPrimitive(String typeName) {
        if (isPrimitive(typeName))
            this.typeName = _primitiveTypeNamesByJavaTypeName.get(typeName);
        else
            this.typeName = typeName;
    }

    /**
     * Whether or not the passed-in type name should be considered a primitive for the purposes
     * of the generated documentation (i.e., a terminal node in the rendered output).
     */
    public static boolean isPrimitive(String typeName) {
        return _primitiveTypeNamesByJavaTypeName.containsKey(typeName);
    }

    public String getTypeName() {
        return typeName;
    }

    public void setRestrictions(List<String> restrictions) {
        this.restrictions = restrictions;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }
}
