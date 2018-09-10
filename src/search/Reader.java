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
	 * ���캯��
	 * 
	 * @param path
	 *            �����ļ���ŵ�ַ
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
	 * ����һ��IndexSearcher��������
	 * 
	 * @return ��indexReader������IndexSearcher
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
	 * ����
	 * 
	 * @param fld
	 *            ��
	 * @param text
	 *            ����
	 * @param numResult
	 *            �������
	 * @return ���н����·��
	 */
	public String[] search(String fld, String text, int numResult, boolean reverse) {
		try {
			// ����IndexReader����IndexSearcher
			IndexSearcher searcher = createSearcher();

			// �������ص�Query
			Term term = new Term(fld, text.toLowerCase());
			Query query = new TermQuery(term);

			// ����������TopDocs
			// TopDocs topDocs = searcher.search(query, numResult);
			Sort sort = new Sort(new SortField("score", SortField.Type.LONG, reverse));
			TopDocs topDocs = searcher.search(query, numResult, sort);

			// ��ȡScoreDoc
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
	 * ��ʾ��������ľ�����Ϣ
	 * 
	 * @param result
	 *            �����������ص�String[]
	 */
	public void printSearchResult(String[] result) {
		for (int i = 0; i < result.length; i++) {
			System.out.println("��N0." + (i + 1) + "��" + result[i]);
			// Show more info here
		}
	}
}
