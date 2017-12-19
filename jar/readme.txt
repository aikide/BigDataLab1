Application information.

 * Language: Java (ver. 1.8)
 * Plugins: maven (for building app; needed to be installed and added to PATH variable), JUnit (for testing)
 * About maven commands: they needed to be executed in directory with 'pom.xml' file (project home directory)
 * Building: mvn install
 * Delete build: mav clean
 * Run tests: msv test
 * Run program: After building is complete, there will be created folder 'target' with '*.jar' file inside. In this
 *              directory (or 'jar' directory, where prepared jar file already exists) execute command:
 *              java -jar <jar name> <program params>.
 *              Example: java -jar bigdatalab1-1.0-SNAPSHOT.jar -e "../1000.csv" 0.05 0.05
 *              After program is finished it's work, results could be found, depending of flags, in command prompt or
 *              files "rulesApriori_*.txt" and "rulesFPGrowth_*.txt".
 * Reminder: Some files contain transaction number as first element of line. To get accurate data exclude such elements with '-e' flag.
 * ****
 * Info about types files app uses as input data.
 *  Generally, input file should be presented like example below:
    "1, 3, 5, 6
     3, 4, 5
     1, 3, 4, 7
     2, 4, 8
     2"
    Here, every transaction consists of numbers of objects (numbers could be started from 0), separated with 'separator'.
    Every transaction starts from new line.
    List of possible separators is: ", " (comma and space after it), " " (space only).
    Type of file isn't relevant.
 * ****
 * Info about input arguments.
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