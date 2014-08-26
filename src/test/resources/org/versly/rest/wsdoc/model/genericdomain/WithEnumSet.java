package org.versly.rest.wsdoc.model.genericdomain;

import java.math.BigDecimal;
import java.util.EnumSet;

public class WithEnumSet {
    private EnumSet<Enum1> myEnumSet = EnumSet.noneOf(Enum1.class);
    private Enum1 myEnum;

    public EnumSet<Enum1> getMyEnumSet() {
        return myEnumSet;
    }

    public void setMyEnumSet(EnumSet<Enum1> myEnumSet) {
        this.myEnumSet = myEnumSet;
    }

    public Enum1 getMyEnum() {
        return myEnum;
    }

    public void setMyEnum(Enum1 myEnum) {
        this.myEnum = myEnum;
    }
}