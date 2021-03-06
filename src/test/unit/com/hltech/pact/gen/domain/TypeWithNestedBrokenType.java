package com.hltech.pact.gen.domain;

public class TypeWithNestedBrokenType {

    private final BrokenRequestType foo;
    private final String bar;

    public TypeWithNestedBrokenType(BrokenRequestType foo, String bar) {
        this.foo = foo;
        this.bar = bar;
    }

    public BrokenRequestType getFoo() {
        return foo;
    }

    public String getBar() {
        return bar;
    }
}
