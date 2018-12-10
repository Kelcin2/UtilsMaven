package com.github.flyinghe.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * DOM4J工具类。
 * 
 * @author Flying
 * 
 */
public class DOM4JTools {

	/**
	 * 获取XML文件的Document文档对象
	 * 
	 * @param file
	 *            指定文件
	 * @return 返回一个Document对象
	 */
	public static Document getDocument(File file) {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(file);
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
		return document;
	}

	/**
	 * 将指定内存中的DOCUMENT文档对象存储到硬盘上
	 * 
	 * @param document
	 *            指定一个内存中的Document文档对象
	 * @param file
	 *            指定存储路径
	 */
	public static void saveXML(Document document, File file) {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter xmlWriter = null;
		try {

			xmlWriter = new XMLWriter(new FileOutputStream(file), format);
			xmlWriter.write(document);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (xmlWriter != null) {
				try {
					xmlWriter.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
