package index;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Writer {
	private Directory directory = null;
	private IndexWriter indexWriter = null;

	/**
	 * ���캯��
	 * 
	 * @param path
	 *            �����ļ�·��
	 */
	public Writer(String path) {
		try {
			directory = FSDirectory.open(Paths.get(path));
//			indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));
			indexWriter = new IndexWriter(directory, new IndexWriterConfig(new SmartChineseAnalyzer()));
//			indexWriter = new IndexWriter(directory, new IndexWriterConfig(new IKAnalyzer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��������
	 * 
	 * @param pathname
	 *            ��Ҫ�������ļ���·��
	 */
	public void index(String pathname) {
		try {
			// ����Document����
			org.apache.lucene.document.Document document = null;

			// ΪDocument���Field
			File file = new File(pathname);

			for (File f : file.listFiles()) {
				document = new org.apache.lucene.document.Document();
				document.add(new StringField("filename", f.getName(), Store.YES));
				document.add(new StringField("path", f.getAbsolutePath(), Store.YES));
				document.add(new org.apache.lucene.document.TextField("content", new FileReader(f)));
				// document.add(new TextField("content", f.toString(), Store.NO));
				// ͨ��IndexWriter����ĵ���������
				document.add(new NumericDocValuesField("score", f.getName().length()));
				// document.add(new StoredField("score", f.getName().length()));
				indexWriter.addDocument(document);
			}

			indexWriter.commit();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ɾ��ָ������
	 * 
	 * @param fld
	 *            ��
	 * @param text
	 *            ����
	 */
	public void delete(String fld, String text) {
		try {
			// ɾ������
			indexWriter.deleteDocuments(new Term(fld, text));
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ɾ��delete���ļ�
	 */
	public void forcedelete() {
		try {
			// ����ɾ��
			indexWriter.forceMergeDeletes();
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * ��������������fld��text�ҵ��ļ�������Ϊdocument
	 * 
	 * @param fld
	 *            ��
	 * @param text
	 *            ����
	 * @param document
	 *            ���ļ�
	 */
	public void updateindex(String fld, String text, org.apache.lucene.document.Document document) {
		try {
			indexWriter.updateDocument(new Term(fld, text), document);
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
