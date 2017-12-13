package com.spbstu.bigdata.apriori;

import java.util.Arrays;

public class ItemSet implements Comparable {
    private int[] items;
    private double support;

    public ItemSet(int itemsLen) {
        items = new int[itemsLen];
    }

    public ItemSet(int item, double support) {
        items = new int[1];
        items[0] = item;
        this.support = support;
    }

    public ItemSet(int[] items) {
        this.items = items.clone();
    }

    public ItemSet(int[] items, double support) {
        this.items = items.clone();
        this.support = support;
    }

    public int[] getItems() {
        return items;
    }

    public void setItems(int[] items) {
        this.items = items;
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public int getItemsLen() {
        return items.length;
    }

    public void updateSupport(double delta) {
        this.support += delta;
    }

    public int replaceItem(int itemNum, int newItemValue)
    {
        int prevValue = -1;
        if (itemNum >= 0 && itemNum <= items.length) {
            prevValue = items[itemNum];
            items[itemNum] = newItemValue;
        }
        return prevValue;
    }

    public ItemSet constructExceptionSet(ItemSet excludedSet)
    {
        int idItems = 0, idExcepts = 0;

        if (items.length == excludedSet.getItemsLen()) {
            return new ItemSet(new int[0], this.getSupport());
        }

        int[] exceptItems = new int[items.length - excludedSet.getItemsLen()],
            excludedItems = excludedSet.items;

        for (int item : items) {
            if (idExcepts < excludedItems.length && item == excludedItems[idExcepts]) {
                ++idExcepts;
                continue;
            }
            exceptItems[idItems] = item;
            ++idItems;
        }
        return new ItemSet(exceptItems);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass() && this.compareTo(obj) == 0;
    }

    @Override
    public int compareTo(Object o) {
        boolean isEqual = true;
        int diff;
        ItemSet other = (ItemSet) o;

        if (items.length == other.getItems().length) {
            for (int i = 0; i < items.length; i++) {
                diff = items[i] - other.getItems()[i];
                if (items[i] == -1 || other.getItems()[i] == -1) // nessesary for skipping elements (usually, items >= 0 always)
                    continue;
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    isEqual = false;
                }
            }
        } else {
            return items.length - other.getItems().length;
        }

        if (isEqual)
            return 0;
        return -1;
    }

    @Override
    public String toString() {
        return "(" +  String.join(", ", Arrays.toString(this.items))
                + ", s: " + String.format("%.4f", this.support) + ")";
    }
}
