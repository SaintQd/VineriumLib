package org.saintqd.vineriumlib.utils;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedCollection<E> {
    private NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    public WeightedCollection() {
        this(new Random());
    }

    public WeightedCollection(Random random) {
        this.random = random;
    }

    public WeightedCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double randomValue = random.nextDouble() * total;
        return map.higherEntry(randomValue).getValue();
    }

    public int size() {
        return map.size();
    }

    public NavigableMap<Double, E> getContents() {
        return map;
    }

    public void setContents(NavigableMap<Double, E> map) {
        this.map = map;
        if (map.isEmpty())
            this.total = 0;
        else
            this.total = map.lastKey();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
        total = 0;
    }

    public double getTotal() {
        return total;
    }
}
