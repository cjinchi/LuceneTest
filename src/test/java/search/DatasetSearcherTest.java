package search;

public class DatasetSearcherTest {
    public static void main(String[] args) {
        DatasetSearcher searcher = new DatasetSearcher("IndexDirectoryName");
        DatasetSearcher.printSearchResult(searcher.search(new String[] { "tag" }, "police", 40));
    }
}
