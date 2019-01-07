package index;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class DatasetIndexer {
    private Directory dir;
    private IndexWriter writer;
    private IndexWriterConfig iwc;

    public DatasetIndexer(String indexPath) {
        if (indexPath == null) {
            System.out.println("indexPath null");
            return;
        }
        try {
            dir = FSDirectory.open(Paths.get(indexPath));
            Map<String, Analyzer> analyzerMap = new HashMap<>();
            analyzerMap.put("id", new KeywordAnalyzer());
            analyzerMap.put("name", new KeywordAnalyzer());
            analyzerMap.put("local_id", new KeywordAnalyzer());
            analyzerMap.put("notes", new StandardAnalyzer());
            analyzerMap.put("title", new StandardAnalyzer());
            analyzerMap.put("dump", new StandardAnalyzer());
            analyzerMap.put("tag", new StandardAnalyzer());

            PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);

            iwc = new IndexWriterConfig(analyzerWrapper);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            dir = null;
            iwc = null;
        }
    }

    public void createIndex() {
        if (dir == null || iwc == null) {
            throw new RuntimeException("dir or iwc is null");
        }
        iwc.setOpenMode(OpenMode.CREATE);
        try {
            writer = new IndexWriter(dir, iwc);
            indexDocs();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public void updateIndex() {
        if (dir == null || iwc == null) {
            throw new RuntimeException("dir or iwc is null");
        }
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        try {
            writer = new IndexWriter(dir, iwc);
            indexDocs();
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void indexDocs() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&serverTimezone=UTC",
                            "localhost", "3306", "datasets", "root", ""));
            System.out.println("connect");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        try {
            statement = connection.prepareStatement("SELECT local_id,id,notes,title,name FROM f_dataset;");
            resultSet = statement.executeQuery();
            System.out.println("get");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        if (resultSet == null) {
            System.out.println("Result Set is null");
            return;
        }

        try {
            while (resultSet.next()) {
                System.out.println("one");
                String id = resultSet.getString("id");
                int local_id = resultSet.getInt("local_id");
                String title = resultSet.getString("title");
                String name = resultSet.getString("name");
                String notes = resultSet.getString("notes");

                statement = connection.prepareStatement("SELECT\n" + "    f1.label AS one,\n" + "    f2.label AS two,\n"
                        + "    f3.label AS three \n" + "FROM\n" + "    f_uri_label_id f1,\n"
                        + "    f_uri_label_id f2,\n" + "    f_uri_label_id f3,\n" + "    f_triple \n" + "WHERE\n"
                        + "    f1.dataset_local_id = ? \n" + "    AND f2.dataset_local_id = ? \n"
                        + "    AND f3.dataset_local_id = ? \n" + "    AND f_triple.dataset_local_id = ? \n"
                        + "    AND f1.id = f_triple.`subject` \n" + "    AND f2.id = f_triple.predicate \n"
                        + "    AND f3.id = f_triple.object;");
                statement.setInt(1, local_id);
                statement.setInt(2, local_id);
                statement.setInt(3, local_id);
                statement.setInt(4, local_id);
                ResultSet dumps = statement.executeQuery();
                StringBuilder dumpBuilder = new StringBuilder();
                while (dumps.next()) {
                    String one = dumps.getString("one");
                    String two = dumps.getString("two");
                    String three = dumps.getString("three");
                    if (one != null) {
                        dumpBuilder.append(one);
                        dumpBuilder.append(" ");
                    }
                    if (two != null) {
                        dumpBuilder.append(two);
                        dumpBuilder.append(" ");
                    }
                    if (three != null) {
                        dumpBuilder.append(three);
                        dumpBuilder.append(" ");
                    }
                }
                String dump = dumpBuilder.toString();

                statement = connection
                        .prepareStatement("SELECT `name`\n" + "FROM f_tag\n" + "WHERE dataset_local_id = ?;");
                statement.setInt(1, local_id);
                ResultSet tags = statement.executeQuery();
                StringBuilder tagBuilder = new StringBuilder();
                while (tags.next()) {
                    String tagName = tags.getString("name");
                    if (tagName != null) {
                        tagBuilder.append(tagName);
                        tagBuilder.append(" ");
                    }
                }
                String tag = tagBuilder.toString();

                Document doc = new Document();
                doc.add(new StringField("id", id, Field.Store.YES));
                doc.add(new TextField("title", title, Field.Store.YES));
                doc.add(new StringField("name", name, Field.Store.YES));
                doc.add(new StringField("local_id", String.valueOf(local_id), Field.Store.YES));
                doc.add(new TextField("notes", notes, Field.Store.NO));
                doc.add(new TextField("dump", dump, Field.Store.NO));
                doc.add(new TextField("tag", tag, Field.Store.NO));

                try {
                    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                        writer.addDocument(doc);
                    } else {
                        writer.updateDocument(new Term("id", id), doc);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
