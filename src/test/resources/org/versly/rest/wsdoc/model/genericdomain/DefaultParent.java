package org.versly.rest.wsdoc.model.genericdomain;


public class DefaultParent<T> extends Grandparent<Long, Integer> implements Parent<T> {

    private T parentField;

    private Grandparent<String, Character> grandparent;

    @Override
    public T getParentField() {
        return parentField;
    }

    public void setParentField(T parentField) {
        this.parentField = parentField;
    }

    public Grandparent<String, Character> getGrandparent() {
        return grandparent;
    }

    public void setGrandparent(Grandparent<String, Character> grandparent) {
        this.grandparent = grandparent;
    }
}
