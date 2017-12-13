package com.spbstu.bigdata.apriori;


import java.util.*;

public class Apriori {
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

    public Apriori(double minSupport, double minConf, List<int[]> database) {
        this.minSupport = minSupport;
        this.minConf = minConf;
        this.database = database;
    }

    public List<ItemSet> setupInitialSupport(Map<Integer, Integer> itemCount) {
        deltaDatabaseSupport = 1.0;
        minSupport *= (double)database.size();

        List<ItemSet> frequentItems = new ArrayList<>();
        itemCount.forEach((key, value) ->
                {
                    if (value * deltaDatabaseSupport >= minSupport) {
                        frequentItems.add(new ItemSet(key, value * deltaDatabaseSupport));
                    }
                }
        );

        Collections.sort(frequentItems, (o1, o2) -> o1.compareTo(o2));

        if (frequentItems.size() == 0){
            return null;
        }

        return frequentItems;
    }

    public boolean checkCandidateFrequency(int[] newItemSet, List<ItemSet> level) {
        ItemSet itemSet = new ItemSet(newItemSet);
        int[] skiweredItemSet = new int[newItemSet.length - 1];

        for (int candidItem = 0; candidItem < newItemSet.length; candidItem++) {
            System.arraycopy(newItemSet, 0, skiweredItemSet, 0, candidItem);
            System.arraycopy(newItemSet, candidItem + 1, skiweredItemSet, candidItem, newItemSet.length - 1 - candidItem);
            itemSet.setItems(skiweredItemSet);

            if (!level.contains(itemSet)) {
                return false;
            }
        }

        return true;
    }


    public List<ItemSet> genNewCandidateList(List<ItemSet> level, boolean pruneCandidates) {
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

                if (!pruneCandidates || checkCandidateFrequency(newItemSet, level)) {
                    candidates.add(new ItemSet(newItemSet));
                }
            }
        }
        return candidates;
    }


    public void runApriori() {
        Map<Integer, Integer> itemCount = new HashMap<>();

        freqSet = new ArrayList<>();

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

        List<ItemSet> level = setupInitialSupport(itemCount);
        if (level == null) {
            timestamp = 0;
            return;
        }
        level.forEach(freqSet::add);

        do {
            List<ItemSet> candidList = genNewCandidateList(level, true);

            for (int[] transaction : database) {
                for (ItemSet candidate : candidList) {
                    int itemNum = 0;
                    for (int item : transaction) {
                        if (item == candidate.getItems()[itemNum]) {
                            itemNum++;
                            if (itemNum == candidate.getItems().length) {
                                candidate.updateSupport(deltaDatabaseSupport);
                                break;
                            }
                        } else if (item > candidate.getItems()[itemNum]) {
                            break;
                        }
                    }
                }
            }

            level.clear();
            candidList.forEach((candidate) ->
                {
                    if (candidate.getSupport() >= minSupport) {
                        level.add(candidate);
                        freqSet.add(candidate);
                    }
                }
            );


        } while(!level.isEmpty());

        timestamp = System.currentTimeMillis() - timestamp;
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
                candidList.add(new ItemSet(i, deltaDatabaseSupport));
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
                candidList = genNewCandidateList(candidList, false);
                ++m;
                id = 0;
            } while (!(candidList.size() == 0 || m >= itemSet.getItemsLen()));
        }
        return rules;
    }

    public void countSupport(ItemSet exceptionSet)
    {
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
