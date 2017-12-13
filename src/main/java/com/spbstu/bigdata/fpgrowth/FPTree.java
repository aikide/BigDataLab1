package com.spbstu.bigdata.fpgrowth;


import java.util.*;

public class FPTree {
    private FPNode root;
    private double minSupport;
    private double deltaDatabaseSupport;
    private Map<Integer, FPNode> elements; // map of defining (elements/items id -> elements/items node) pairs

    public FPTree(double minSupport, double deltaDatabaseSupport) {
        this.root = new FPNode();
        this.minSupport = minSupport;
        this.deltaDatabaseSupport = deltaDatabaseSupport;
    }

    public FPNode getRoot() {
        return root;
    }

    public double getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(double minSupport) {
        this.minSupport = minSupport;
    }

    public Map<Integer, FPNode> getElements() {
        return elements;
    }

    public void setElements(Map<Integer, FPNode> elements) {
        this.elements = elements;
    }

    public double getDeltaDatabaseSupport() {
        return deltaDatabaseSupport;
    }

    public void setDeltaDatabaseSupport(double deltaDatabaseSupport) {
        this.deltaDatabaseSupport = deltaDatabaseSupport;
    }

    public void setElements(List<FPNode> elementList) {
        if (this.elements == null)
            this.elements = new LinkedHashMap<>();
        else
            this.elements.clear();
        elementList.forEach((item) ->  this.elements.put(item.getItem(), item));
    }

    public void setupChildConnection(FPNode childNode) {
        FPNode rightNode = elements.get(childNode.getItem());
        childNode.setRight(rightNode.getRight());
        rightNode.setRight(childNode);
        elements.put(rightNode.getItem(), rightNode);
    }

    public void setupElements(Map<Integer, Integer> itemCount) {
        List<FPNode> elementValues = new ArrayList<>();
        this.elements = new LinkedHashMap<>();

        itemCount.forEach((key, value) ->
        {
            if (value * deltaDatabaseSupport >= this.minSupport)
                elementValues.add(new FPNode(key, value * deltaDatabaseSupport));
        });

        Collections.sort(elementValues, (o1, o2) -> -o1.compareTo(o2));

        for (FPNode node : elementValues)
            this.elements.put(node.getItem(), node);
    }

    // levelItemId - id in list of items in desc order by supp
    public FPTree buildCFPTree(int levelItemId) {
        FPTree cfpTree = new FPTree(-1, 0.0);
        List<FPNode> levels = new ArrayList<>(elements.values()),
                ghostChildren,
                cfpLevels = new ArrayList<>();
        FPNode elementItem = levels.get(levelItemId), branchItem, ghostCopy, prevGhostNode,
                levelItem, levelHead, levelLast, cfpRoot = cfpTree.getRoot();

        // setup ghosts
        elementItem = elementItem.getRight();
        while (elementItem != null) {
            branchItem = elementItem.getParent();
            prevGhostNode = null;
            while (!branchItem.isRoot()) {
                ghostCopy = branchItem.getGhostCopy();
                if (ghostCopy == null) {
                    ghostCopy = new FPNode(branchItem.getItem(), 0);
                    branchItem.setGhostCopy(ghostCopy);

                    if (prevGhostNode != null) {
                        prevGhostNode.setParent(ghostCopy);

                        ghostChildren = new ArrayList<>();
                        ghostChildren.add(prevGhostNode);
                        ghostCopy.setChildren(ghostChildren);
                    }
                } else if (prevGhostNode != null && prevGhostNode.getParent() == null) {
                    prevGhostNode.setParent(ghostCopy);

                    ghostChildren = ghostCopy.getChildren();
                    if (ghostChildren == null)
                        ghostChildren = new ArrayList<>();
                    ghostChildren.add(prevGhostNode);
                    ghostCopy.setChildren(ghostChildren);
                }
                ghostCopy.updateNodeSupport(elementItem.getSupport());

                prevGhostNode = ghostCopy;
                branchItem = branchItem.getParent();
            }
            if (prevGhostNode != null && prevGhostNode.getParent() == null) {
                prevGhostNode.setParent(cfpRoot);
                ghostChildren = cfpRoot.getChildren();
                if (ghostChildren == null)
                    ghostChildren = new ArrayList<>();
                ghostChildren.add(prevGhostNode);
                cfpRoot.setChildren(ghostChildren);
            }
            elementItem = elementItem.getRight();
        }

        // compile (ghost) cfptree
        for (int i = 0; i <= levelItemId - 1; i++) {
            levelHead = null;
            levelLast = null;
            levelItem = levels.get(i).getRight();
            while (levelItem != null) {
                ghostCopy = levelItem.getGhostCopy();
                if (ghostCopy != null) {
                    if (levelHead == null) {
                        levelHead = new FPNode(levelItem.getItem(), 0);
                        levelLast = levelHead;
                    }
                    levelLast.setRight(ghostCopy);
                    levelLast = ghostCopy;
                    levelHead.updateNodeSupport(ghostCopy.getSupport());
                    levelItem.setGhostCopy(null);
                }
                levelItem = levelItem.getRight();
            }
            if (levelHead != null)
                cfpLevels.add(levelHead);
        }

        cfpTree.setElements(cfpLevels);

        return cfpTree;
    }
}
