package cn.xxl.LuceneTest;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class LuceneManager {

	public IndexWriter getIndexWriter() throws Exception {
		Directory directory = FSDirectory.open(new File("D:\\tmp\\法律法规\\indexDirectory"));
		//Analyzer analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.index_ansj);
		//Analyzer analyzer = new StandardAnalyzer();
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);	
		IndexWriter indexWriter = new IndexWriter(directory,config);
		return indexWriter;
	}
	public IndexSearcher getIndexSearcher() throws Exception {
		Directory directory = FSDirectory.open(new File("D:\\tmp\\法律法规\\indexDirectory"));
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		return indexSearcher;
	}
	public void printResult(IndexSearcher indexSearcher,Query query) throws Exception{
		TopDocs topDocs = indexSearcher.search(query, 10);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for(ScoreDoc scoreDoc:scoreDocs) {
			int doc = scoreDoc.doc;
			Document document = indexSearcher.doc(doc);
			String fileName = document.get("file_name");
			System.out.println(fileName);
			String filePath = document.get("file_path");
			System.out.println(filePath);
			String fileContent = document.get("file_content");
			System.out.println(fileContent.substring(0, 10));
			String fileSize = document.get("file_size");
			System.out.println(fileSize);
		}
	}
	//删除全部
	public void testDeleteAll() throws Exception {
		IndexWriter indexWriter = getIndexWriter();
		indexWriter.deleteAll();
		indexWriter.close();
	}
	//条件删除
	public void testDelete() throws Exception {
		IndexWriter indexWriter = getIndexWriter();
		Query query = new TermQuery(new Term("file_name","党政领导干部"));
		indexWriter.deleteDocuments(query);
		indexWriter.close();
	}
	//通过id删除
	public void DeleteById() throws Exception {
		IndexWriter indexWriter = getIndexWriter();
		Query query = new TermQuery(new Term("id","1"));
		indexWriter.deleteDocuments(query);
		indexWriter.close();
	}
	public void testUpdate() throws Exception{
		IndexWriter indexWriter = getIndexWriter();
		Document document = new Document();

		document.add(new TextField("file_name","俱乐部",Store.YES));
		
		indexWriter.updateDocument(new Term("file_name","俱乐部"), document);
		indexWriter.close();
	}
	//查询全部
	public void testSearchAll() throws Exception{
		IndexSearcher indexSearcher = getIndexSearcher();
		Query query = new MatchAllDocsQuery();
		printResult(indexSearcher, query);
		indexSearcher.getIndexReader().close();
	}
	public static void main(String[] args) throws IOException {
        LuceneManager manager = new LuceneManager();
		try {
        	//manager.testDeleteAll();
			//manager.testUpdate();
			manager.testDelete();
        	System.out.println("yes");
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
}
