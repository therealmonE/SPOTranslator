package io.github.therealmone.spoStackMachine.collections.arraylist;

import io.github.therealmone.spoStackMachine.collections.Collection;

public interface ArrayList extends Collection {
    void add(double value);

    double get(int index);

    void remove(int index);

    void rewrite(int index, double value);

    static ArrayList getInstance() {
        return new ArrayListImpl();
    }
}
