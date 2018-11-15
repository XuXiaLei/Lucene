package cn.xxl.LuceneTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wltea.analyzer.lucene.IKAnalyzer;



public class Test {

	public static void TextIndex() throws Exception{
		Directory directory = FSDirectory.open(new File("D:\\tmp\\法律法规\\indexDirectory"));
		//Analyzer analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.index_ansj);
		//Analyzer analyzer = new StandardAnalyzer();
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);	
		IndexWriter indexWriter = new IndexWriter(directory,config);
		
		File f = new File("D:\\tmp\\法律法规\\法律法规");
		File[] files = f.listFiles();
		for(File file : files) {
			
			Document document = new Document();
			// 文件名称
			String file_name = file.getName();
			// 文件类型
			String fileType = file_name.substring(file_name.lastIndexOf(".") + 1,
                    file_name.length()).toLowerCase();
			//文件大小
			long file_size = FileUtils.sizeOf(file);
			//文件路径
			String file_path = file.getPath();
			//Field
			Field fileNameField = new TextField("file_name", file_name, Store.YES);
			
			Field fileSizeField = new LongField("file_size", file_size, Store.YES);
			
			Field filePathField = new StoredField("file_path", file_path);
			
			document.add(fileNameField);
			document.add(fileSizeField);
			document.add(filePathField);
            
			InputStream in = new FileInputStream(file);
            InputStreamReader reader = null;
            
            if (fileType != null && !fileType.equals("")) {

                if (fileType.equals("doc")) {
                    // 获取doc的word文档
                    WordExtractor wordExtractor = new WordExtractor(in);
                    
                    String content = wordExtractor.getText();
                    Field fileContentField = new TextField("file_content", content, Store.YES);
                    
                    document.add(fileContentField);
                    wordExtractor.close();
                    System.out.println("注意：已为文件“" + file_name + "”创建了索引");

                } else if (fileType.equals("docx")) {
                    // 获取docx的word文档
                    XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                            new XWPFDocument(in));
                    // 创建Field对象，并放入doc对象中
                    String content = xwpfWordExtractor.getText();
                    //content = content.substring(0, 100);
                    Field fileContentField = new TextField("file_content", content, Store.YES);
                    document.add(fileContentField);
                    // 关闭文档
                    xwpfWordExtractor.close();
                    System.out.println("注意：已为文件“" + file_name + "”创建了索引");

                } else if (fileType.equals("pdf")) {
                    // 获取pdf文档
                	//方法一：
                	/**
                    InputStream input = null;
                    input = new FileInputStream( pdfFile );
                    //加载 pdf 文档
                    PDFParser parser = new PDFParser(new RandomAccessBuffer(input));
                    parser.parse();
                    document = parser.getPDDocument();
                    **/
                	//方法二：
                    PDDocument pdDocument = PDDocument.load(file);
                    // 获取页码
                    int pages = pdDocument.getNumberOfPages();
                    //读取文本内容
                    PDFTextStripper stripper = new PDFTextStripper();
                    // 设置按顺序输出
                    stripper.setSortByPosition(true);
                    stripper.setStartPage(1);
                    stripper.setEndPage(pages);
                    String content = stripper.getText(pdDocument);
                    //content = content.substring(0, 100);
                    // 创建Field对象，并放入doc对象中
                    Field fileContentField = new TextField("file_content", content, Store.YES);
                    document.add(fileContentField);
                    // 关闭文档
                    pdDocument.close();
                    System.out.println("注意：已为文件“" + file_name + "”创建了索引");

                } else if (fileType.equals("txt")) {
                    // 建立一个输入流对象reader  
                    reader = new InputStreamReader(in); 
                    // 建立一个对象，它把文件内容转成计算机能读懂的语言
                    BufferedReader br = new BufferedReader(reader);   
                    String content = "";
                    String line = null;
                    while ((line = br.readLine()) != null) {  
                        // 一次读入一行数据
                        content += line;   
                    }  
                    // 创建Field对象，并放入doc对象中
                    //content = content.substring(0, 100);
                    Field fileContentField = new TextField("file_content", content, Store.YES);
                    document.add(fileContentField);
                    br.close();
                    System.out.println("注意：已为文件“" + file_name + "”创建了索引");
                } else {

                    System.out.println("非指定文件");
                    continue;

                }

            }
            in.close();
			indexWriter.addDocument(document);
		}
		indexWriter.close();
	}
	public static void testSearch() throws Exception{
		
		Directory directory = FSDirectory.open(new File("D:\\tmp\\法律法规\\indexDirectory"));
		//Directory directory = new RAMDirectory();
		
		IndexReader indexReader = DirectoryReader.open(directory);
		
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		Query query = new TermQuery(new Term("file_name","党政领导干部"));
		
		TopDocs topDocs = indexSearcher.search(query, 10);
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for(ScoreDoc scoreDoc:scoreDocs) {
			int doc = scoreDoc.doc;
			Document document = indexSearcher.doc(doc);
			String name = document.get("file_name");
			System.out.println(name);
			String size = document.get("file_size");
			System.out.println(size);
			String path = document.get("file_path");
			System.out.println(path);
			//String content = document.get("file_content");
			//System.out.println(content);
		}
		//创建indexReader
		indexReader.close();
	}
	public static void main(String[] args) throws IOException {
        try {
			//TextIndex();
        	testSearch();
		} catch (Exception e) {
			e.printStackTrace();
		}

    }

}
