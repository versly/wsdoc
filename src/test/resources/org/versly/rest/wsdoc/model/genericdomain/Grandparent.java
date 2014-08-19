package org.versly.rest.wsdoc.model.genericdomain;

/**
 * User: Ryan Walls
 * Date: 1/11/13
 */
public class Grandparent<S,T> {

    private S firstGrandparentField;
    private T secondGrandparentField;

    public S getFirstGrandparentField() {
        return firstGrandparentField;
    }

    public void setFirstGrandparentField(S firstGrandparentField) {
        this.firstGrandparentField = firstGrandparentField;
    }

    public T getSecondGrandparentField() {
        return secondGrandparentField;
    }

    public void setSecondGrandparentField(T secondGrandparentField) {
        this.secondGrandparentField = secondGrandparentField;
    }
}
