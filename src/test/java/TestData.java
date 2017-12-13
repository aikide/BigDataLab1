import com.spbstu.bigdata.data.Data;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestData {
    final private String testDataFile = "test.csv";

    @Test(expected = Exception.class)
    public void testUnknownFileLoading() throws Exception {
        Data data = new Data("");

        data.loadData(false);
    }

    @Test
    public void testCorrectFileLoading() throws Exception {
        Data data = new Data(testDataFile);

        data.loadData(false);

        List<int []> fileContents = data.getDatabase();

        Assert.assertNotNull(fileContents);
        Assert.assertNotEquals(0, fileContents.size());
    }

    @Test
    public void testCorrectFileCorrectContentLoading() throws Exception {
        Data data = new Data(testDataFile);

        data.loadData(false);

        List<int []> fileContents = data.getDatabase();

        Assert.assertEquals(5, fileContents.size());
        Assert.assertArrayEquals(new int[] {1, 2, 3, 4, 5}, fileContents.get(0));
        Assert.assertArrayEquals(new int[] {2, 3}, fileContents.get(1));
        Assert.assertArrayEquals(new int[] {1, 2, 6}, fileContents.get(2));
        Assert.assertArrayEquals(new int[] {1, 2, 7}, fileContents.get(3));
        Assert.assertArrayEquals(new int[] {1, 6, 8}, fileContents.get(4));
    }


    @Test
    public void testCorrectFileCorrectContentWithFlagLoading() throws Exception {
        Data data = new Data(testDataFile);

        data.loadData(true);

        List<int []> fileContents = data.getDatabase();

        Assert.assertEquals(5, fileContents.size());
        Assert.assertArrayEquals(new int[] {2, 3, 4, 5}, fileContents.get(0));
        Assert.assertArrayEquals(new int[] {3}, fileContents.get(1));
        Assert.assertArrayEquals(new int[] {2, 6}, fileContents.get(2));
        Assert.assertArrayEquals(new int[] {2, 7}, fileContents.get(3));
        Assert.assertArrayEquals(new int[] {6, 8}, fileContents.get(4));
    }
}
