package search;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Reader {

	private IndexReader indexReader = null;
	private Directory directory = null;

	/**
	 * 构造函数
	 * 
	 * @param path
	 *            索引文件存放地址
	 */
	public Reader(String path) {
		try {
			directory = FSDirectory.open(Paths.get(path));
			indexReader = DirectoryReader.open(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建一个IndexSearcher用于搜索
	 * 
	 * @return 由indexReader创建的IndexSearcher
	 */
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

	/**
	 * 搜索
	 * 
	 * @param fld
	 *            域
	 * @param text
	 *            参数
	 * @param numResult
	 *            结果个数
	 * @return 所有结果的路径
	 */
	public String[] search(String fld, String text, int numResult, boolean reverse) {
		try {
			// 根据IndexReader创建IndexSearcher
			IndexSearcher searcher = createSearcher();

			// 创建搜素的Query
			Term term = new Term(fld, text.toLowerCase());
			Query query = new TermQuery(term);

			// 搜索并返回TopDocs
			// TopDocs topDocs = searcher.search(query, numResult);
			Sort sort = new Sort(new SortField("score", SortField.Type.LONG, reverse));
			TopDocs topDocs = searcher.search(query, numResult, sort);

			// 获取ScoreDoc
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;

			String[] result = new String[scoreDocs.length];
			for (int i = 0; i < scoreDocs.length; i++) {
				result[i] = searcher.doc(scoreDocs[i].doc).get("path");
			}
			return result;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 显示搜索结果的具体信息
	 * 
	 * @param result
	 *            搜索函数返回的String[]
	 */
	public void printSearchResult(String[] result) {
		for (int i = 0; i < result.length; i++) {
			System.out.println("【N0." + (i + 1) + "】" + result[i]);
			// Show more info here
		}
	}
}
