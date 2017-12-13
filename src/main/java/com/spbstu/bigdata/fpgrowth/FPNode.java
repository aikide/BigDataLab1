package com.spbstu.bigdata.fpgrowth;


import java.util.ArrayList;
import java.util.List;

public class FPNode implements Comparable {
    private int item;
    private List<FPNode> children; // self-explaining
    private FPNode parent; // parent
    private FPNode right; // right node from us
    private FPNode ghostCopy; // used only in cfp building
    private double support;

    // for root creation
    public FPNode() {
        this.item = -1;
        this.right = null;
        this.parent = null;
        this.children = null;
        this.ghostCopy = null;
    }

    // for elements
    public FPNode(int item, double support) {
        this.item = item;
        this.support = support;
        this.children = null;
        this.parent = null;
        this.ghostCopy = null;
    }

    public boolean isRoot() {
        return (item == -1);
    }

    public void updateNodeSupport(double delta) {
        this.support += delta;
    }

    public FPNode getChildByItem(int element) {
        if (children == null)
            return null;

        for (FPNode node : children) {
            if (node.getItem() == element) {
                return node;
            }
        }

        return null;
    }

    public FPNode addChild(int childItem) {
        FPNode child = new FPNode(childItem, 0);

        child.setParent(this);
        if (children == null)
            children = new ArrayList<>();
        children.add(child);

        return child;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public List<FPNode> getChildren() {
        return children;
    }

    public void setChildren(List<FPNode> children) {
        this.children = children;
    }

    public FPNode getRight() {
        return right;
    }

    public void setRight(FPNode right) {
        this.right = right;
    }

    public FPNode getParent() {
        return parent;
    }

    public void setParent(FPNode parent) {
        this.parent = parent;
    }

    public double getSupport() {
        return support;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public FPNode getGhostCopy() {
        return ghostCopy;
    }

    public void setGhostCopy(FPNode ghostCopy) {
        this.ghostCopy = ghostCopy;
    }

    @Override
    public int compareTo(Object o) {
        double diff = this.support - ((FPNode) o).support;

        if (diff > 0.0)
            return 1;
        else if (diff < 0.0)
            return -1;

        return 0;
    }

    @Override
    public String toString() {
        return "(i: " + this.item +
                ", s: " + String.format("%.4f", this.support) +
                ", p: " + (this.parent == null ? "null" : this.parent.getItem()) +
                ", c: " + (this.children == null ? "null" : this.children.size()) +
                ", g: " + (this.ghostCopy == null ? "no" : "yes") + ")";
    }
}
