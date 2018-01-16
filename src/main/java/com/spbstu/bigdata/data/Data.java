package com.spbstu.bigdata.data;

import com.spbstu.bigdata.apriori.ItemSet;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {
    private String inputData;
    private String itemSeparator;
    private List<int[]> database;
    private int maxNumber;
    private boolean isTransactionType;

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public int getDBSize() {
        return database.size();
    }

    public String getItemSeparator() {
        return itemSeparator;
    }

    public void setItemSeparator(String itemSeparator) {
        this.itemSeparator = itemSeparator;
    }

    public List<int[]> getDatabase() {
        return database;
    }

    // itemSeparator:
    // * ", " - csv - default
    // * " " - .dat
    public Data(String inputData) {
        this.inputData = inputData;
        this.itemSeparator = ",";
        this.database = null;
        this.maxNumber = 0;
    }

    public Data(String inputData, String delim, boolean isTransactionType) {
        this.inputData = inputData;
        this.itemSeparator = delim;
        this.database = null;
        this.maxNumber = 0;
        this.isTransactionType = isTransactionType;
    }

    // Copy-ed from Apache Commons StringUtils
    private static int countMatches(String str, String sub) {
        if(!str.isEmpty() && !sub.isEmpty()) {
            int count = 0;

            for(int idx = 0; (idx = str.indexOf(sub, idx)) != -1; idx += sub.length()) {
                ++count;
            }

            return count;
        } else {
            return 0;
        }
    }

    // Load file
    public void loadData(boolean excludeFirst) throws IOException {
        if (this.isTransactionType) {
            loadDataTransactional(excludeFirst);
        } else {
            loadDataNormal(excludeFirst);
        }
    }


    private void loadDataNormal(boolean excludeFirst) throws IOException {
        int firstItem = 0;
        database = new ArrayList<>(); // the database in memory

        if (excludeFirst)
            firstItem = 1;

        BufferedReader reader = new BufferedReader(new FileReader(inputData));
        String line;

        while (((line = reader.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }

            String[] lineSep = line.split(itemSeparator);
            int transaction[] = new int[lineSep.length - firstItem];

            for (int i = firstItem; i < lineSep.length; i++) {
                Integer item = Integer.parseInt(lineSep[i].trim());
                transaction[i - firstItem] = item;
            }
            this.maxNumber = Math.max(this.maxNumber, transaction[transaction.length - 1]);
            database.add(transaction);
        }
        reader.close();
    }


    private void loadDataTransactional(boolean excludeFirst) throws IOException {
        int firstItem = 0, transLen = 0, transactionStock[];
        database = new ArrayList<>(); // the database in memory

        if (excludeFirst)
            firstItem = 1;

        BufferedReader reader = new BufferedReader(new FileReader(inputData));
        String line = reader.readLine();

        if (line.isEmpty()) {
            throw new IOException("Error: First line in file is empty, but it shouldn't be.");
        }

        transactionStock = new int[countMatches(line, ",") + 1 - firstItem];

        do {
            if (line.isEmpty()) {
                continue;
            }

            String[] lineSep = line.split(itemSeparator);

            for (int i = firstItem; i < lineSep.length; i++) {
                if (!lineSep[i].equals("0")) {
                    transactionStock[transLen] = i - firstItem;
                    transLen += 1;
                }
            }

            int transaction[] = new int[transLen];

            System.arraycopy(transactionStock, 0, transaction, 0, transLen);
            this.maxNumber = Math.max(this.maxNumber, transaction[transLen - 1]);
            database.add(transaction);
            // transactionStock may be not 'zero'-ed, because it is updated from the beginning and we know then to stop
            // getting numbers (transLen helps)
            transLen = 0;
        } while (((line = reader.readLine()) != null));
        reader.close();
    }

    public void saveDatabaseInTransactionStyle(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        BufferedWriter writer = new BufferedWriter(fileWriter);
        int id = 0;

        for (int[] transaction : database) {
            if (transaction[id] == 0) {
                writer.write("1");
                if (transaction.length - 1 > id) {
                    ++id;
                }
            }
            else
                writer.write("0");

            for (int i = 1; i <= maxNumber; i++) {
                if (transaction[id] == i) {
                    writer.write(", 1");
                    if (transaction.length - 1 > id) {
                        ++id;
                    }
                } else
                    writer.write(", 0");
            }
            writer.write("\n");
            id = 0;
        }
        writer.close();
    }

    // Debug only
    private List<String> getTranslatedData(String filename) throws IOException {
        List<String> translatedData = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(inputData));
        String line;

        while (((line = reader.readLine()) != null)) {
            if (line.isEmpty()) {
                continue;
            }

            String[] lineSep = line.split(itemSeparator);

            for (int i = 0; i < lineSep.length; i++) {
                translatedData.add(lineSep[i]);
            }
            break;
        }
        reader.close();

        return translatedData;
    }


    public void saveResultRules(Map<ItemSet, ItemSet> rules, long time, int freqSetSize,
                                String fileNamePrefix, boolean saveToFileFlag,
                                String algorithm, boolean showOnlyStats) throws IOException {
        StringBuilder result = new StringBuilder();
        List<String> translatedData;

        translatedData = new ArrayList<>();
        for (int i = 0; i <= maxNumber; i++) {
            translatedData.add(String.valueOf(i));
        }

        result.append(algorithm).
                append(" run test case.\n").
                append("=======================================================\n").
                append("Size of frequent items' set: ").
                append(freqSetSize).
                append("\n").
                append("Number of rules: ").
                append(rules.size()).
                append("\n").
                append("Search time: ").
                append(time).
                append(" ms\n");
        if (showOnlyStats) {
            System.out.println(result.toString());
            return;
        }
        if (!rules.isEmpty()) {
            rules.forEach((key, value) ->
            {
                int[] fromItems = key.getItems(),
                        toItems = value.getItems();
                String fromRule = "", toRule = "";

                for (int i = 0; i < fromItems.length - 1; i++)
                    fromRule += fromItems[i] + ", ";
                fromRule += translatedData.get(fromItems[fromItems.length - 1]);

                for (int i = 0; i < toItems.length - 1; i++)
                    toRule += translatedData.get(toItems[i]) + ", ";
                toRule += translatedData.get(toItems[toItems.length - 1]);

                result.append("Rule: [").
                        append(fromRule).
                        append("] -> [").
                        append(toRule).
                        append("], conf = ").
                        append(key.getSupport()).
                        append("\n");
            });
        }
        result.append("=======================================================\n");

        int firstIndex = inputData.lastIndexOf('/');
        String fileNameTail = inputData.substring(firstIndex == -1 ? 0 : firstIndex + 1, inputData.lastIndexOf('.'));
        if (saveToFileFlag) {
            FileWriter fileWriter = new FileWriter(fileNamePrefix + fileNameTail + ".txt");
            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.write(result.toString());
            writer.close();
        } else {
            System.out.println(result.toString());
        }
    }

    // Debug only
    public void compareData(Map<ItemSet, ItemSet> rules, String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        boolean first = false, hasMatch = false, delta = false;
        double deltaVal = 0;

        Map<ItemSet, ItemSet> rulesCopy = new HashMap<>(rules.size());

        for (Map.Entry<ItemSet, ItemSet> rule : rules.entrySet()) {
            rulesCopy.put(rule.getKey(), rule.getValue());
        }

        while (((line = reader.readLine()) != null)) {
            if (line.isEmpty() || !first) {
                first = true;
                continue;
            }

            String[] lineSep = line.split(",");
            String from = lineSep[0].substring(0, lineSep[0].indexOf('=')),
                    to = lineSep[0].substring(lineSep[0].indexOf('=') + 4);
            String[] fromList = from.split(" "),
                    toList = to.split(" ");
            int[] fromInt = new int[fromList.length],
                    toInt = new int[toList.length];
            double conf = Double.parseDouble(lineSep[2]);

            for (int i = 0; i < fromList.length; i++) {
                fromInt[i] = Integer.parseInt(fromList[i]);
            }
            for (int i = 0; i < toList.length; i++) {
                toInt[i] = Integer.parseInt(toList[i]);
            }

            hasMatch = false;
            delta = false;
            ItemSet fromItemset = new ItemSet(fromInt),
                    toItemset = new ItemSet(toInt);
            for (Map.Entry<ItemSet, ItemSet> rule : rules.entrySet()) {
                if (rule.getKey().equals(fromItemset) && rule.getValue().equals(toItemset)) {
                    if (Math.abs(conf - rule.getKey().getSupport()) > 0.00001) {
                        deltaVal = rule.getKey().getSupport();
                        delta = true;
                    }
                    rulesCopy.remove(rule.getKey(), rule.getValue());
                    hasMatch = true;
                    break;
                }
            }
            if (!hasMatch) {
                System.out.println("Error!!!");
                System.out.println(line);
            }
            if (delta) {
                System.out.print("Delta!!! ");
                System.out.print(deltaVal);
                System.out.print("            <>            ");
                System.out.println(conf);
            }

        }
        if (!rulesCopy.isEmpty()) {
            System.out.println("Copy-error!!!");
            for  (Map.Entry<ItemSet, ItemSet> rule : rulesCopy.entrySet()) {
                System.out.print(rule.getKey().toString());
                System.out.print(" -> ");
                System.out.println(rule.getValue().toString());
            }
        }

        reader.close();
    }
}
