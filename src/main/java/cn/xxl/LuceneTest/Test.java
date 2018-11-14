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
		//��һ��������jar��
		//�ڶ���������indexWriter����
			//ָ��directory����
			//ָ��������analyzer
		//������������document����
		//���Ĳ�������field���󣬲�������ӵ�document
		//���岽��ʹ��indexWriterд��document��������
		//���������ر�indexWriter
		Directory directory = FSDirectory.open(new File("D:\\tmp\\���ɷ���\\indexDirectory"));
		
		Analyzer analyzer = new StandardAnalyzer();
		//Analyzer analyzer2 = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);	
		IndexWriter indexWriter = new IndexWriter(directory,config);
		Document document = new Document();
		File f = new File("D:\\tmp\\���ɷ���\\���ɷ���");
		File[] files = f.listFiles();
		for(File file : files) {
			//TextField���ִ�
			//LongField��long��
			//StoredField:���ִʣ�ֻ�洢
			//�ļ�����
			String file_name = file.getName();
			Field fileNameField = new TextField("file_name", file_name, Store.YES);
			//�ļ���С
			long file_size = FileUtils.sizeOf(file);
			Field fileSizeField = new LongField("file_size", file_size, Store.YES);
			//�ļ�·��
			String file_path = file.getPath();
			Field filePathField = new StoredField("file_path", file_path);
			//�ļ�����
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
        // ����word�ļ���·��
        String dataDirectory = "D:\\tmp\\���ɷ���\\���ɷ���";
        // ����Lucene�����ļ���·��
        String indexDirectory = "D:\\tmp\\���ɷ���\\indexDirectory";
        // ����Directory���� ��Ҳ���Ƿִ�������
        Directory directory = new SimpleFSDirectory(new File(indexDirectory));
        // ����һ���򵥵ķִ���,���Զ����ݽ��зִ�
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        File[] files = new File(dataDirectory).listFiles();
        for (int i = 0; i < files.length; i++) {
            // �ļ��ǵڼ���
            System.out.println("���ǵ�" + i + "���ļ�----------------");
            // �ļ�������·��
            System.out.println("����·����" + files[i].toString());
            // ��ȡ�ļ�����
            String fileName = files[i].getName();
            // ��ȡ�ļ���׺����������Ϊ�ļ�����
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1,
                    fileName.length()).toLowerCase();
            // �ļ�����
            System.out.println("�ļ����ƣ�" + fileName);
            // �ļ�����
            System.out.println("�ļ����ͣ�" + fileType);

            Document doc = new Document();

            // String fileCode = FileType.getFileType(files[i].toString());
            // �鿴�����ļ����ļ�ͷ��ǵ�����
            // System.out.println("fileCode=" + fileCode);

            InputStream in = new FileInputStream(files[i]);
            InputStreamReader reader = null;

            if (fileType != null && !fileType.equals("")) {

                if (fileType.equals("doc")) {
                    // ��ȡdoc��word�ĵ�
                    WordExtractor wordExtractor = new WordExtractor(in);
                    // ����Field���󣬲�����doc������
                    // Field�ĸ����ֶκ������£�
                    // ��1������������field��name��
                    // ��2��������value��valueֵ�������ı���String���ͣ�Reader���ͻ�����Ԥ�����TokenStream��,
                    // �����ƣ�byet[]��, ���������֣�һ�� Number���ͣ�
                    // ��3��������Field.Store��ѡ���Ƿ�洢������洢�Ļ��ڼ�����ʱ����Է���ֵ
                    // ��4��������Field.Index����������������ʽ
                    doc.add(new Field("contents", wordExtractor.getText(),
                            Field.Store.YES, Field.Index.ANALYZED));
                    // �ر��ĵ�
                    
                    System.out.println("ע�⣺��Ϊ�ļ���" + fileName + "������������");

                } else if (fileType.equals("docx")) {
                    // ��ȡdocx��word�ĵ�
                    XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(
                            new XWPFDocument(in));
                    // ����Field���󣬲�����doc������
                    doc.add(new Field("contents", xwpfWordExtractor.getText(),
                            Field.Store.YES, Field.Index.ANALYZED));
                    // �ر��ĵ�
                    xwpfWordExtractor.close();
                    System.out.println("ע�⣺��Ϊ�ļ���" + fileName + "������������");

                } else if (fileType.equals("pdf")) {
                    // ��ȡpdf�ĵ�
                    PDFParser parser = new PDFParser(in);
                    parser.parse();
                    PDDocument pdDocument = parser.getPDDocument();
                    PDFTextStripper stripper = new PDFTextStripper();
                    // ����Field���󣬲�����doc������
                    doc.add(new Field("contents", stripper.getText(pdDocument),
                            Field.Store.NO, Field.Index.ANALYZED));
                    // �ر��ĵ�
                    pdDocument.close();
                    System.out.println("ע�⣺��Ϊ�ļ���" + fileName + "������������");

                } else if (fileType.equals("txt")) {
                    // ����һ������������reader  
                    reader = new InputStreamReader(in); 
                    // ����һ�����������ļ�����ת�ɼ�����ܶ���������
                    BufferedReader br = new BufferedReader(reader);   
                    String txtFile = "";
                    String line = null;

                    while ((line = br.readLine()) != null) {  
                        // һ�ζ���һ������
                        txtFile += line;   
                    }  
                    // ����Field���󣬲�����doc������
                    doc.add(new Field("contents", txtFile, Field.Store.NO,
                            Field.Index.ANALYZED));
                    System.out.println("ע�⣺��Ϊ�ļ���" + fileName + "������������");

                } else {

                    System.out.println();
                    continue;

                }

            }
            // �����ļ������򣬲�����doc������
            doc.add(new Field("filename", files[i].getName(), Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            // ����ʱ����򣬲�����doc������
            doc.add(new Field("indexDate", DateTools.dateToString(new Date(),
                    DateTools.Resolution.DAY), Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            // д��IndexWriter
            indexWriter.addDocument(doc);
            // ����
            System.out.println();
        }
        // �鿴IndexWriter�����ж��ٸ�����
        System.out.println("numDocs=" + indexWriter.numDocs());
        // �ر�����
        indexWriter.close();

    }

}
