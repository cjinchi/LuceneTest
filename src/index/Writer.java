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
	 * 构造函数
	 * 
	 * @param path
	 *            索引文件路径
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
	 * 创建索引
	 * 
	 * @param pathname
	 *            需要索引的文件夹路径
	 */
	public void index(String pathname) {
		try {
			// 创建Document对象
			org.apache.lucene.document.Document document = null;

			// 为Document添加Field
			File file = new File(pathname);

			for (File f : file.listFiles()) {
				document = new org.apache.lucene.document.Document();
				document.add(new StringField("filename", f.getName(), Store.YES));
				document.add(new StringField("path", f.getAbsolutePath(), Store.YES));
				document.add(new org.apache.lucene.document.TextField("content", new FileReader(f)));
				// document.add(new TextField("content", f.toString(), Store.NO));
				// 通过IndexWriter添加文档到索引中
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
	 * 删除指定索引
	 * 
	 * @param fld
	 *            域
	 * @param text
	 *            参数
	 */
	public void delete(String fld, String text) {
		try {
			// 删除索引
			indexWriter.deleteDocuments(new Term(fld, text));
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 彻底删除delete的文件
	 */
	public void forcedelete() {
		try {
			// 彻底删除
			indexWriter.forceMergeDeletes();
			indexWriter.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 更新索引，根据fld和text找到文件，更新为document
	 * 
	 * @param fld
	 *            域
	 * @param text
	 *            参数
	 * @param document
	 *            新文件
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
