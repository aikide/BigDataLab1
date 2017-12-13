package com.spbstu.bigdata.fpgrowth;


import com.spbstu.bigdata.apriori.ItemSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FPGrowth {
    private int candidCount;
    private double minSupport;
    private double minConf;
    private double deltaDatabaseSupport;
    private List<int[]> database;

    private List<ItemSet> freqSet;
    private long timestamp;

    public double getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(double minSupport) {
        this.minSupport = minSupport;
    }

    public double getMinConf() {
        return minConf;
    }

    public void setMinConf(double minConf) {
        this.minConf = minConf;
    }

    public List<ItemSet> getFreqSet() {
        return freqSet;
    }

    public void setFreqSet(List<ItemSet> freqSet) {
        this.freqSet = freqSet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public FPGrowth(double minSupport, double minConf, List<int[]> database) {
        this.minSupport = minSupport;
        this.minConf = minConf;
        this.database = database;
        this.candidCount = 0;
    }

    public List<int[]> orderDatabase(List<FPNode> elements) {
        List<int []> orderedDB = new ArrayList<>();
        int[] tempItem = new int[elements.size()], currItem;
        int currItemLen = 0;
        FPNode node;

        for (int[] item : database) {
            currItemLen = 0;
            for (int i = 0; i < elements.size(); i++) {
                node = elements.get(i);
                for (int j = 0; j < item.length; j++) {
                    if (item[j] == node.getItem()) {
                        tempItem[currItemLen] = item[j];
                        ++currItemLen;
                        break;
                    }
                }
            }
            if (currItemLen != 0) {
                currItem = new int[currItemLen];
                System.arraycopy(tempItem, 0, currItem, 0, currItemLen);
                orderedDB.add(currItem);
            }
        }

        return orderedDB;
    }

    public FPTree createInitialFPTree(Map<Integer, Integer> itemCount) {
        FPTree tree = new FPTree(minSupport, deltaDatabaseSupport);
        Map<Integer, FPNode> elements;
        FPNode node, childNode;

        tree.setupElements(itemCount);
        elements = tree.getElements();
        List<int []> orderedDB = orderDatabase(new ArrayList<>(elements.values()));

        for (int[] item : orderedDB) {
            node = tree.getRoot();

            for (int elem : item) {
                if (!elements.containsKey(elem)) {
                    break;
                }

                childNode = node.getChildByItem(elem);
                if (childNode == null) {
                    childNode = node.addChild(elem);
                    tree.setupChildConnection(childNode);
                }
                childNode.updateNodeSupport(deltaDatabaseSupport);
                node = childNode;
            }
        }

        return tree;
    }

    public void runFPGrowth() {
        Map<Integer, Integer> itemCount = new HashMap<>();

        timestamp = System.currentTimeMillis();

        /*** Get frequent item sets ***/
        for (int[] item : database) {
            for (int i : item) {
                Integer count = itemCount.get(i);
                if (count == null) {
                    itemCount.put(i, 1);
                } else {
                    itemCount.put(i, ++count);
                }
            }
        }

        deltaDatabaseSupport = 1.0;
        minSupport *= (double)database.size();

        FPTree tree = createInitialFPTree(itemCount);
        freqSet = new ArrayList<>();

        // recursive search of frequent patterns
        fpFind(tree, new int[0], freqSet);

        timestamp = System.currentTimeMillis() - timestamp;
    }


    private int[] constructNewItem(int[] item, FPNode node) {
        int pos = 0, insertItem = node.getItem();
        int[] newItem = new int[item.length + 1];

        for (int itemElement : item) {
            if (itemElement > insertItem)
                break;
            ++pos;
        }
        System.arraycopy(item, 0, newItem, 0, pos);
        newItem[pos] = insertItem;
        System.arraycopy(item, pos, newItem, pos + 1, item.length - pos);

        return newItem;
    }


    public void fpFind(FPTree tree, int[] item, List<ItemSet> freqSet) {
        List<FPNode> levelList = new ArrayList<>(tree.getElements().values());
        FPTree cfpTree;
        FPNode node;

        for (int i = levelList.size() - 1; i >= 0; i--) {
            node = levelList.get(i);

            if (node.getSupport() >= minSupport) {
                int[] newItem = constructNewItem(item, node);
                freqSet.add(new ItemSet(newItem, node.getSupport()));

                cfpTree = tree.buildCFPTree(i);
                fpFind(cfpTree, newItem, freqSet);
            }
        }
    }

    /*** Get association riles, based on frequent sets ***/
    public Map<ItemSet, ItemSet> calculateRules(boolean excludeSoleItems) {
        int m = 0, id = 0;
        double conf;
        ItemSet exceptionSet, excludedSet;
        Map<ItemSet, ItemSet> rules = new HashMap<>();

        for (ItemSet itemSet : freqSet) {
            if (excludeSoleItems && itemSet.getItems().length <= 1)
                continue;
            m = 1;
            List<ItemSet> candidList = new ArrayList<>();
            for (int i : itemSet.getItems()) {
                candidList.add(new ItemSet(i, 1));
            }

            do {
                while (id < candidList.size()) {
                    excludedSet = candidList.get(id);
                    exceptionSet = itemSet.constructExceptionSet(excludedSet);
                    countSupport(exceptionSet);
                    conf = itemSet.getSupport() / exceptionSet.getSupport();
                    excludedSet.setSupport(itemSet.getSupport());
                    if (conf >= minConf) {
                        exceptionSet.setSupport(conf);
                        rules.put(exceptionSet, excludedSet);
                        ++id;
                    } else {
                        candidList.remove(id);
                    }
                }
                candidList = genNewCandidateList(candidList);
                ++m;
                id = 0;
            } while (!(candidList.size() == 0 || m >= itemSet.getItemsLen()));
        }
        return rules;
    }

    public List<ItemSet> genNewCandidateList(List<ItemSet> level) {
        List<ItemSet> candidates = new ArrayList<>();
        boolean finishSearch = false, finishContinue = false;

        for (int i = 0; i < level.size(); i++) {
            int[] itemSet1 = level.get(i).getItems();

            for (int j = i + 1; j < level.size(); j++) {
                int[] itemSet2 = level.get(j).getItems();

                for (int k = 0; k < itemSet1.length; k++) {
                    if (k == itemSet1.length - 1) {
                        if (itemSet1[k] >= itemSet2[k]) {
                            finishSearch = true;
                            break;
                        }
                    } else if (itemSet1[k] < itemSet2[k]) {
                        finishContinue = true;
                        break;
                    } else if (itemSet1[k] > itemSet2[k]) {
                        finishSearch = true;
                        break;
                    }
                }

                if (finishContinue) {
                    finishContinue = false;
                    continue;
                } else if (finishSearch) {
                    finishSearch = false;
                    break;
                }

                int newItemSet[] = new int[itemSet1.length + 1];
                System.arraycopy(itemSet1, 0, newItemSet, 0, itemSet1.length);
                newItemSet[itemSet1.length] = itemSet2[itemSet2.length - 1];
                candidates.add(new ItemSet(newItemSet));
            }
        }
        return candidates;
    }

    public void countSupport(ItemSet exceptionSet) {
        int[] excItems = exceptionSet.getItems();

        if (excItems.length == 0)
            return;

        for (int[] transaction : database) {
            int itemNum = 0;
            for (int item : transaction) {
                if (item == excItems[itemNum]) {
                    itemNum++;
                    if (itemNum == excItems.length) {
                        exceptionSet.updateSupport(deltaDatabaseSupport);
                        break;
                    }
                } else if (item > excItems[itemNum]) {
                    break;
                }
            }
        }
    }
}
