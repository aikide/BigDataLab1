import com.spbstu.bigdata.apriori.Apriori;
import com.spbstu.bigdata.apriori.ItemSet;
import com.spbstu.bigdata.data.Data;
import com.spbstu.bigdata.fpgrowth.FPGrowth;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestFPGrowth {
    final private String testProved = "testProved.csv";
    final private String testAll = "testAll.csv";
    final private String testBig = "20000.csv";

    private Data dataStorage;
    private FPGrowth fpGrowth;
    private List<int []> transactions;

    @Before
    public void initAprioriTestData() throws Exception {
        dataStorage = new Data(testProved);

        dataStorage.loadData(false);
        transactions = dataStorage.getDatabase();
    }

    @Test
    public void testMaxSupportResultIsEmpty() {
        fpGrowth = new FPGrowth(2, 0.5, transactions);

        fpGrowth.runFPGrowth();

        Assert.assertNotNull(fpGrowth.getFreqSet());
        Assert.assertEquals(0, fpGrowth.getFreqSet().size());
    }

    @Test
    public void testMaxConfidenceResultIsEmpty() {
        fpGrowth = new FPGrowth(0.2, 2.0, transactions);

        fpGrowth.runFPGrowth();

        Map<ItemSet, ItemSet> rules = fpGrowth.calculateRules(true);

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

        fpGrowth = new FPGrowth(0.3, 0.5, transactions);

        fpGrowth.runFPGrowth();

        Map<ItemSet, ItemSet> rules = fpGrowth.calculateRules(true);

        for (Map.Entry<ItemSet, ItemSet> rule : rules.entrySet()) {
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

        fpGrowth = new FPGrowth(0.2, 0.5, transactions);

        fpGrowth.runFPGrowth();
        Map<ItemSet, ItemSet> rules = fpGrowth.calculateRules(true);

        for (Map.Entry<ItemSet, ItemSet> rule : rules.entrySet()) {
            Assert.assertTrue(rule.getValue().getItemsLen() == 1 && rule.getValue().getItems()[0] == 1);
        }
    }

    @Test
    public void testBigDataAndCompareFreqSetsWithApriori() throws Exception {
        boolean hasMatch;

        /* Reload database with specific file */
        dataStorage = new Data(testBig);

        dataStorage.loadData(true);
        transactions = dataStorage.getDatabase();

        fpGrowth = new FPGrowth(0.01, 0.2, transactions);
        Apriori apriori = new Apriori(0.01, 0.2, transactions);

        fpGrowth.runFPGrowth();
        apriori.runApriori();

        List<ItemSet> fpIS = fpGrowth.getFreqSet(),
                apIS = apriori.getFreqSet();

        Assert.assertTrue(apIS.size() == fpIS.size());

        for (ItemSet fpSet : fpIS) {
            hasMatch = false;
            for (ItemSet apSet : apIS) {
                if (fpSet.equals(apSet)) {
                    hasMatch = true;
                    break;
                }
            }
            Assert.assertTrue(hasMatch);
        }
    }

    @Test
    public void testBigDataAndCompareRulesWithApriori() throws Exception {
        boolean hasMatch;

        /* Reload database with specific file */
        dataStorage = new Data(testBig);

        dataStorage.loadData(true);
        transactions = dataStorage.getDatabase();

        fpGrowth = new FPGrowth(0.01, 0.2, transactions);
        Apriori apriori = new Apriori(0.01, 0.2, transactions);

        fpGrowth.runFPGrowth();
        apriori.runApriori();

        Map<ItemSet, ItemSet> fpgrowthRules = fpGrowth.calculateRules(true),
                aprioriRules = apriori.calculateRules(true);

        Assert.assertTrue(fpgrowthRules.size() == aprioriRules.size());

        for (Map.Entry<ItemSet, ItemSet> fpgrowthRule : fpgrowthRules.entrySet()) {
            hasMatch = false;
            for (Map.Entry<ItemSet, ItemSet> aprioriRule : aprioriRules.entrySet()) {
                if (fpgrowthRule.getKey().equals(aprioriRule.getKey()) && fpgrowthRule.getValue().equals(aprioriRule.getValue())) {
                    hasMatch = true;
                    break;
                }
            }
            Assert.assertTrue(hasMatch);
        }
    }

    @After
    public void closeFPGrowthTestData() {
        dataStorage = null;
        fpGrowth = null;
    }
}
