package org.versly.rest.wsdoc;

public class Child extends DefaultParent<String> {

    private String childField;

    public String getChildField() {
        return childField;
    }

    public void setChildField(String childField) {
        this.childField = childField;
    }
}
