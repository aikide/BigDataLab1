/* Info about input arguments.
 * bigdatalab1.jar [<excludeFirstColumnFlag>] [<delimiterFlag>] [<returnedAnswerIndicator>] <inputFileName> <minSupp> <minConf>
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
 *  *  Details: If file has delimiter, different from usual comma & space (', '), it could be set to:
 *  *           -s - space symbol (' ')
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
 * * bigdatalab1.jar "test3.csv" 0.21 0.3
 * * Result file: rules_test3.txt
 *
 * * bigdatalab1.jar -e -s -c "test4.csv" 0.47 0.05
 * * Result file: <console>
 * */