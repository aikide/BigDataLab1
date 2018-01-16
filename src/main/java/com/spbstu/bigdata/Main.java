package com.spbstu.bigdata;


import com.spbstu.bigdata.apriori.Apriori;
import com.spbstu.bigdata.apriori.ItemSet;
import com.spbstu.bigdata.data.Data;
import com.spbstu.bigdata.fpgrowth.FPGrowth;

import java.util.Map;

/* Info about input arguments.
 * bigdatalab1.jar [<excludeFirstColumnFlag>] [<delimiterFlag>] [<transactionTypeOfFileFlag>] [<returnedAnswerIndicator>] <inputFileName> <minSupp> <minConf>
 * Descriptions:
 *  <excludeFirstColumnFlag>:
 *  *  Necessary: No
 *  *  Values: '-e', ''
 *  *  Default value: ''.
 *  *  Details: If file has number of row on first position in each row, then it could be excluded by parser.
 *  *           Warning! If first element is not excluded, in row could possibly be two elements with same value.
 *  *           Such situation is undesirable, because Apriori puts emphasis on unique values in row and their position
 *  *           (i.e. order matters).
 *  <delimiterFlag>:
 *  *  Necessary: No
 *  *  Values: '-s', ''
 *  *  Default value: ''.
 *  *  Details: If file has delimiter, different from usual comma (','), it could be set to:
 *  *           -s - space symbol (' ')
 *  <transactionTypeOfFileFlag>:
 *  *  Necessary: No
 *  *  Values: '-t', ''
 *  *  Default value: ''.
 *  *  Details: If file is consists of lines of 0 and 1, which is representation of transactions occurred, this flag
 *  *           could be set to -t. If representation consists of rows of numbers of elements in transaction, no value
 *  *           no value needed (default representation).
 *  <returnedAnswerIndicator>:
 *  *  Necessary: No
 *  *  Values: '-c', '-f'
 *  *  Default value: '-f'.
 *  *  Details: Used to change representation of returned answer's style.
 *  *           -c - Return answer in console
 *  *           -f - Return answer in file (filename: 'rules_' + inputFileName)
 *  <inputFileName>:
 *  *  Necessary: Yes
 *  *  Values: Sequence of characters, which could be recognised as path.
 *             Encased in double apostrophes (") from both sides.
 *  *  Default value: None.
 *  *  Details: File with data, where each row is filled with numbers separated with sign ','.
 *              Each number corresponds to item with name in transaction file (if given).
 *  <minSupp>:
 *  *  Necessary: Yes
 *  *  Values: Floating point type number.
 *  *  Default value: None.
 *  *  Details: Minimum support.
 *  <minConf>:
 *  *  Necessary: Yes
 *  *  Values: Floating point type number.
 *  *  Default value: None.
 *  *  Details: Minimum confidence number.
 * Example of commands:
 * * bigdatalab1.jar "test.csv" 0.1 0.2
 * * Result file: rules_test.txt
 *
 * * bigdatalab1.jar -e "test2.csv" 0.22 0.16
 * * Result file: rules_test2.txt
 *
 * * bigdatalab1.jar -s -t "test3.csv" 0.21 0.3
 * * Result file: rules_test3.txt
 *
 * * bigdatalab1.jar -e -c "test4.csv" 0.47 0.05
 * * Result file: <console>
 * */

public class Main {
    public static void main(String[] argv) {
        String inputFileName, delim = ",";
        int argShift = 0;
        double minSupp = 0, minConf = 0;
        boolean excludeFirstColumn = false,
                saveDataToFile = true,
                isTransactionType = false;

        switch (argv.length)
        {
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                if (argv[argShift].equals("-e")) {
                    excludeFirstColumn = true;
                    argShift += 1;
                }
                if (argv[argShift].equals("-s")) {
                    delim = " ";
                    argShift += 1;
                }
                if (argv[argShift].equals("-t")) {
                    isTransactionType = true;
                    argShift += 1;
                }
                if (argv[argShift].equals("-c")) {
                    saveDataToFile = false;
                    argShift += 1;
                } else if (argv[argShift].equals("-f")) {
                    argShift += 1;
                } else if (argv[argShift].startsWith("-")) {
                    System.out.println("Error: expected arguments not found. Check argument list.");
                    return;
                }

                inputFileName = argv[argShift];
                if (argv[argShift + 1].matches("\\d+\\.\\d+")) {
                    minSupp = Double.parseDouble(argv[argShift + 1]);
                } else {
                    System.out.println("Error: expected 'minSupp' argument is not a float-type number. Check argument list.");
                    return;
                }
                if (argv[argShift + 2].matches("\\d+\\.\\d+")) {
                    minConf = Double.parseDouble(argv[argShift + 2]);
                } else {
                    System.out.println("Error: expected 'minConf' argument is not a float-type number. Check argument list.");
                    return;
                }
                break;
            default:
                System.out.println("Error: Number of arguments doesn't match required pattern.");
                return;
        }


        Data data = new Data(inputFileName, delim, isTransactionType);


        try {
            data.loadData(excludeFirstColumn);
        } catch (Exception ex) {
            System.out.println("Error: exception caught during loading.");
            ex.printStackTrace();
            return;
        }

        Apriori apriori = new Apriori(minSupp, minConf, data.getDatabase());
        apriori.runApriori();
        Map<ItemSet, ItemSet> aprioriResult = apriori.calculateRules(true);

        FPGrowth fpGrowth = new FPGrowth(minSupp, minConf, data.getDatabase());
        fpGrowth.runFPGrowth();
        Map<ItemSet, ItemSet> fpGrowthResult = fpGrowth.calculateRules(true);

        try {
            data.saveResultRules(aprioriResult, apriori.getTimestamp(), apriori.getFreqSet().size(),  "rulesApriori_", saveDataToFile, "Apriori", false);
            data.saveResultRules(fpGrowthResult, fpGrowth.getTimestamp(), fpGrowth.getFreqSet().size(), "rulesFPGrowth_", saveDataToFile, "FPGrowth", false);
        } catch (Exception ex) {
            System.out.println("Error: exception caught during saving results.");
            ex.printStackTrace();
        }
    }
}
