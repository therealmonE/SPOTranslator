package io.github.therealmone.spoStackMachine.collections.hashset;

import io.github.therealmone.translatorAPI.Beans.Node;

class Element extends Node {
    private double value;
    private final int hashCode;

    Element(final String name, final double value) {
        super(name);
        this.value = value;
        this.hashCode = name.hashCode();
    }

    double getValue() {
        return this.value;
    }

    void setValue(double value) {
        this.value = value;
    }

    int getHashCode() {
        return this.hashCode;
    }
}
