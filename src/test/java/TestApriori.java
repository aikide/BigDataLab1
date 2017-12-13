import com.spbstu.bigdata.apriori.Apriori;
import com.spbstu.bigdata.apriori.ItemSet;
import com.spbstu.bigdata.data.Data;
import com.spbstu.bigdata.fpgrowth.FPGrowth;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TestApriori {
    final private String testProved = "testProved.csv";
    final private String testAll = "testAll.csv";
    final private String testBig = "20000.csv";

    private Data dataStorage;
    private Apriori apriori;
    private List<int []> transactions;

    @Before
    public void initAprioriTestData() throws Exception {
        dataStorage = new Data(testProved);

        dataStorage.loadData(false);
        transactions = dataStorage.getDatabase();
    }

    @Test
    public void testMaxSupportResultIsEmpty() {
        apriori = new Apriori(2.0, 0.5, transactions);

        apriori.runApriori();

        Assert.assertNotNull(apriori.getFreqSet());
        Assert.assertEquals(0, apriori.getFreqSet().size());
    }

    @Test
    public void testMaxConfidenceResultIsEmpty() {
        apriori = new Apriori(0.2, 2.0, transactions);

        apriori.runApriori();
        Map<ItemSet, ItemSet> rules = apriori.calculateRules(true);

        Assert.assertNotNull(rules);
        Assert.assertEquals(0, rules.size());
    }


    @Test
    public void testProvedData() {
        List<int[]> answer = new ArrayList<>(6);
        boolean hasMatch;

        answer.add(new int[] {1, 4});
        answer.add(new int[] {4, 1});
        answer.add(new int[] {3, 2});
        answer.add(new int[] {3, 4});
        answer.add(new int[] {2, 4});
        answer.add(new int[] {4, 2});

        apriori = new Apriori(0.3, 0.5, transactions);

        apriori.runApriori();
        Map<ItemSet, ItemSet> rules = apriori.calculateRules(true);

        for (Entry<ItemSet, ItemSet> rule : rules.entrySet()) {
            hasMatch = false;
            for (int[] answerItem : answer) {
                if (answerItem[0] == rule.getKey().getItems()[0] && answerItem[1] == rule.getValue().getItems()[0]) {
                    hasMatch = true;
                    break;
                }
            }
            Assert.assertTrue(hasMatch);
        }
    }

    @Test
    public void testAllData() throws Exception {
        /* Reload database with specific file */
        dataStorage = new Data(testAll);

        dataStorage.loadData(false);
        transactions = dataStorage.getDatabase();

        apriori = new Apriori(0.2, 0.5, transactions);

        apriori.runApriori();
        Map<ItemSet, ItemSet> rules = apriori.calculateRules(true);

        for (Entry<ItemSet, ItemSet> rule : rules.entrySet()) {
            Assert.assertTrue(rule.getValue().getItemsLen() == 1 && rule.getValue().getItems()[0] == 1);
        }
    }

    @Test
    public void testBigDataAndCompareFreqSetsWithFPGrowth() throws Exception {
        boolean hasMatch;

        /* Reload database with specific file */
        dataStorage = new Data(testBig);

        dataStorage.loadData(true);
        transactions = dataStorage.getDatabase();

        apriori = new Apriori(0.01, 0.2, transactions);
        FPGrowth fpGrowth = new FPGrowth(0.01, 0.2, transactions);

        apriori.runApriori();
        fpGrowth.runFPGrowth();

        List<ItemSet> apIS = apriori.getFreqSet(),
                fpIS = fpGrowth.getFreqSet();

        Assert.assertTrue(apIS.size() == fpIS.size());

        for (ItemSet apSet : apIS) {
            hasMatch = false;
            for (ItemSet fpSet : fpIS) {
                if (apSet.equals(fpSet)) {
                    hasMatch = true;
                    break;
                }
            }
            Assert.assertTrue(hasMatch);
        }
    }

    @Test
    public void testBigDataAndCompareRulesWithFPGrowth() throws Exception {
        boolean hasMatch;

        /* Reload database with specific file */
        dataStorage = new Data(testBig);

        dataStorage.loadData(true);
        transactions = dataStorage.getDatabase();

        apriori = new Apriori(0.06, 0.2, transactions);
        FPGrowth fpGrowth = new FPGrowth(0.06, 0.2, transactions);

        apriori.runApriori();
        fpGrowth.runFPGrowth();
        Map<ItemSet, ItemSet> aprioriRules = apriori.calculateRules(true),
                fpgrowthRules = fpGrowth.calculateRules(true);

        Assert.assertTrue(fpgrowthRules.size() == aprioriRules.size());

        for (Entry<ItemSet, ItemSet> aprioriRule : aprioriRules.entrySet()) {
            hasMatch = false;
            for (Entry<ItemSet, ItemSet> fpgrowthRule : fpgrowthRules.entrySet()) {
                if (aprioriRule.getKey().equals(fpgrowthRule.getKey()) && aprioriRule.getValue().equals(fpgrowthRule.getValue())) {
                    hasMatch = true;
                    break;
                }
            }
            Assert.assertTrue(hasMatch);
        }
    }

    @After
    public void closeAprioriTestData() {
        dataStorage = null;
        apriori = null;
    }
}
