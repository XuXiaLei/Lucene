package cn.xxl.LuceneTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.wltea.analyzer.lucene.IKAnalyzer;



public class Test {

	public void TextIndex() throws Exception{
		//第一步：导入jar包
		//第二步：创建indexWriter对象
			//指定directory对象
			//指定分析器analyzer
		//第三步：创建document对象
		//第四步：创建field对象，并将其添加到document
		//第五步：使用indexWriter写入document到索引库
		//第六步：关闭indexWriter
		Directory directory = FSDirectory.open(new File("D:\\tmp\\法律法规\\indexDirectory"));
		
		Analyzer analyzer = new StandardAnalyzer();
		//Analyzer analyzer2 = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);	
		IndexWriter indexWriter = new IndexWriter(directory,config);
		Document document = new Document();
		File f = new File("D:\\tmp\\法律法规\\法律法规");
		File[] files = f.listFiles();
		for(File file : files) {
			//TextField：分词
			//LongField：long型
			//StoredField:不分词，只存储
			//文件名字
			String file_name = file.getName();
			Field fileNameField = new TextField("file_name", file_name, Store.YES);
			//文件大小
			long file_size = FileUtils.sizeOf(file);
			Field fileSizeField = new LongField("file_size", file_size, Store.YES);
			//文件路径
			String file_path = file.getPath();
			Field filePathField = new StoredField("file_path", file_path);
			//文件内容
			String file_content = FileUtils.readFileToString(file, "UTF-8");
			Field fileContentField = new TextField("file_content", file_content, Store.YES);
			document.add(fileNameField);
			document.add(fileSizeField);
			document.add(filePathField);
			document.add(fileContentField);
			indexWriter.addDocument(document);
		}
		indexWriter.close();
	}
	public static void main(String[] args) throws IOException {
        // 保存word文件的路径
        String dataDirectory = "D:\\tmp\\法律法规\\法律法规";
        // 保存Lucene索引文件的路径
        String indexDirectory = "D:\\tmp\\法律法规\\indexDirectory";
        // 创建Directory对象 ，也就是分词器对象
        Directory directory = new SimpleFSDirectory(new File(indexDirectory));
        // 创建一个简单的分词器,可以对数据进行分词
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            // 文件是第几个
            System.out.println("这是第" + i + "个文件----------------");
            // 文件的完整路径
            System.out.println("完整路径：" + files[i].toString());
            // 获取文件名称
            String fileName = files[i].getName();
            // 获取文件后缀名，将其作为文件类型
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1,
                    fileName.length()).toLowerCase();
            // 文件名称
            System.out.println("文件名称：" + fileName);
            // 文件类型
            System.out.println("文件类型：" + fileType);

            Document doc = new Document();

            // String fileCode = FileType.getFileType(files[i].toString());
            // 查看各个文件的文件头标记的类型
            // System.out.println("fileCode=" + fileCode);

            InputStream in = new FileInputStream(files[i]);
            InputStreamReader reader = null;

            if (fileType != null && !fileType.equals("")) {

                if (fileType.equals("doc")) {
                    // 获取doc的word文档
                    WordExtractor wordExtractor = new WordExtractor(in);
                    // 创建Field对象，并放入doc对象中
                    // Field的各个字段含义如下：
                    // 第1个参数是设置field的name，
                    // 第2个参数是value，value值可以是文本（String类型，Reader类型或者是预分享的TokenStream）,
                    // 二进制（byet[]）, 或者是数字（一个 Number类型）
                    // 第3个参数是Field.Store，选择是否存储，如果存储的话在检索的时候可以返回值
                    // 第4个参数是Field.Index，用来设置索引方式
                    doc.add(new Field("contents", wordExtractor.getText(),
                            Field.Store.YES, Field.Index.ANALYZED));
                    // 关闭文档
                    
                    System.out.println("注意：已为文件“" + fileName + "”创建了索引");

                } else if (fileType.equals("docx")) {
                    // 获取docx的word文档
                    XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                            new XWPFDocument(in));
                    // 创建Field对象，并放入doc对象中
                    doc.add(new Field("contents", xwpfWordExtractor.getText(),
                            Field.Store.YES, Field.Index.ANALYZED));
                    // 关闭文档
                    xwpfWordExtractor.close();
                    System.out.println("注意：已为文件“" + fileName + "”创建了索引");

                } else if (fileType.equals("pdf")) {
                    // 获取pdf文档
                    PDFParser parser = new PDFParser(in);
                    parser.parse();
                    PDDocument pdDocument = parser.getPDDocument();
                    PDFTextStripper stripper = new PDFTextStripper();
                    // 创建Field对象，并放入doc对象中
                    doc.add(new Field("contents", stripper.getText(pdDocument),
                            Field.Store.NO, Field.Index.ANALYZED));
                    // 关闭文档
                    pdDocument.close();
                    System.out.println("注意：已为文件“" + fileName + "”创建了索引");

                } else if (fileType.equals("txt")) {
                    // 建立一个输入流对象reader  
                    reader = new InputStreamReader(in); 
                    // 建立一个对象，它把文件内容转成计算机能读懂的语言
                    BufferedReader br = new BufferedReader(reader);   
                    String txtFile = "";
                    String line = null;

                    while ((line = br.readLine()) != null) {  
                        // 一次读入一行数据
                        txtFile += line;   
                    }  
                    // 创建Field对象，并放入doc对象中
                    doc.add(new Field("contents", txtFile, Field.Store.NO,
                            Field.Index.ANALYZED));
                    System.out.println("注意：已为文件“" + fileName + "”创建了索引");

                } else {

                    System.out.println();
                    continue;

                }

            }
            // 创建文件名的域，并放入doc对象中
            doc.add(new Field("filename", files[i].getName(), Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            // 创建时间的域，并放入doc对象中
            doc.add(new Field("indexDate", DateTools.dateToString(new Date(),
                    DateTools.Resolution.DAY), Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            // 写入IndexWriter
            indexWriter.addDocument(doc);
            // 换行
            System.out.println();
        }
        // 查看IndexWriter里面有多少个索引
        System.out.println("numDocs=" + indexWriter.numDocs());
        // 关闭索引
        indexWriter.close();

    }

}
