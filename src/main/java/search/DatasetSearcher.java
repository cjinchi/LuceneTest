package search;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DatasetSearcher {
    private IndexReader indexReader = null;
    private Directory directory = null;

    public DatasetSearcher(String indexPath) {
        try {
            directory = FSDirectory.open(Paths.get(indexPath));
            indexReader = DirectoryReader.open(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IndexSearcher createSearcher() {
        try {
            if (indexReader == null) {
                indexReader = DirectoryReader.open(directory);
            } else {
                IndexReader temp = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
                if (temp != null) {
                    indexReader.close();
                    indexReader = temp;
                }
            }
            return new IndexSearcher(indexReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] search(String[] fields, String text, int numResult) {
        try {
            IndexSearcher searcher = createSearcher();

//            Term term = new Term(fld, text.toLowerCase());
            Map<String, Analyzer> analyzerMap = new HashMap<>();
            analyzerMap.put("id", new KeywordAnalyzer());
            analyzerMap.put("name", new KeywordAnalyzer());
            analyzerMap.put("local_id", new KeywordAnalyzer());
            analyzerMap.put("notes", new StandardAnalyzer());
            analyzerMap.put("title", new StandardAnalyzer());
            analyzerMap.put("dump", new StandardAnalyzer());
            analyzerMap.put("tag", new StandardAnalyzer());

            PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);

//            QueryParser parser = new QueryParser("title", analyzerWrapper);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzerWrapper);
            Query query = parser.parse(text.toLowerCase());
            TopDocs topDocs = searcher.search(query, numResult);

//            Sort sort = new Sort(new SortField("score", SortField.Type.LONG, reverse));
//            TopDocs topDocs = searcher.search(query, numResult, sort);

            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            String[] result = new String[scoreDocs.length];
            for (int i = 0; i < scoreDocs.length; i++) {
                result[i] = searcher.doc(scoreDocs[i].doc).get("title");
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void printSearchResult(String[] result) {
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
            // Show more info here
        }
    }
}
