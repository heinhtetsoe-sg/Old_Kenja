package alp.co.jp.dom;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import alp.co.jp.element.AElement;
import alp.co.jp.util.Serialize;
import alp.co.jp.util.XmlPdfPath;

public class Xml_Element {
	XmlPdfPath xmlPdfpath;

	public Xml_Element(String pdfpath) {
		xmlPdfpath = new XmlPdfPath(pdfpath);
	}

	public void setProName(String name) {
		xmlPdfpath.setName(name);
	}

	public AElement Read(String FieldxmlName) {
		if ((FieldxmlName != null) && (FieldxmlName.length() > 0))
			xmlPdfpath.setFieldxmlname(FieldxmlName);
		AElement Ele = new AElement();
		// DocumentBuilderFactory の新しいインスタンスを取得
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// 構文解析時にドキュメントの妥当性を検証
		factory.setValidating(true);
		// 属性http://java.sun.com/xml/jaxp/properties/schemaLanguageはhttp://www.w3.org/2001/XMLSchema
		factory.setAttribute(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
		// DOM Document インスタンスを取得する API を定義
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new DefaultHandler());
			// XML 文書全体
			Document document = builder.parse(xmlPdfpath.getFieldxmlpath());
			NodeList roots = document.getChildNodes();
			for (int i = 0; i < roots.getLength(); i++) {
				Node data_n = roots.item(i);
				int type = data_n.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Ele = GetElement(data_n);
					break;
				}
			}
			// シリアライズ出力
			boolean ret = Serialize.Output(Ele, xmlPdfpath.getSerialfldPath());
			if (ret == false) {
				System.out.println("Serialize.Outputエラー");
			}
		} catch (ParserConfigurationException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return Ele;
	}

	public String getSerialfldPath() {
		return xmlPdfpath.getSerialfldPath();
	}

	public static AElement GetElement(Node data) {
		String nodename = data.getNodeName();
		AElement ele = new AElement(nodename, "");
		ArrayList<AElement> ele_array = ele.getElement_();
		NodeList ele_list = data.getChildNodes();
		boolean judge = false;
		for (int n = 0; n < ele_list.getLength(); n++) {
			Node child_node = ele_list.item(n);
			if (child_node.getNodeType() == Node.ELEMENT_NODE) {
				AElement child_element = GetElement(child_node);
				if (child_element != null) {
					ele_array.add(child_element);
					judge = true;
				}
			}
		}
		// 子があるときは値を空白にする。
		String value = (judge==false) ? data.getTextContent().trim() : "";
		ele.setValue(value.trim());
		return ele;
	}
}
