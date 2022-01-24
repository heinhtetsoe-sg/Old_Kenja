package alp.co.jp.dom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import alp.co.jp.ARegion;
import alp.co.jp.ARepeat;
import alp.co.jp.BaseFonts;
import alp.co.jp.Field_List;
import alp.co.jp.Fonts;
import alp.co.jp.Line_List;
import alp.co.jp.Product;
import alp.co.jp.ProductForm;
import alp.co.jp.Repeat_List;
import alp.co.jp.Static_List;
import alp.co.jp.element.AElement;
import alp.co.jp.line.Line;
import alp.co.jp.line.Net;
import alp.co.jp.line.PenStyle;
import alp.co.jp.line.Rect;
import alp.co.jp.line.RundRect;
import alp.co.jp.text.AFont;
import alp.co.jp.text.Basefont;
import alp.co.jp.text.FieldBarcode;
import alp.co.jp.text.FieldImageURL;
import alp.co.jp.text.FieldText;
import alp.co.jp.text.StaticBarcode;
import alp.co.jp.text.StaticImageURL;
import alp.co.jp.text.StaticText;
import alp.co.jp.util.BarCodeUtil;
import alp.co.jp.util.Decimal;
import alp.co.jp.util.RepeatUtil;
import alp.co.jp.util.RepeatUtil.REP_PLACE;
import alp.co.jp.util.Serialize;
import alp.co.jp.util.StringUtil;
import alp.co.jp.util.TextWidth;
import alp.co.jp.util.XmlPdfPath;

public class Xml_Product {
	XmlPdfPath xmlPdfpath;

	public Xml_Product(String pdfPath) {
		xmlPdfpath = new XmlPdfPath(pdfPath);
	}

	public Product Read(String pdfName) {
		boolean elementset = false;
		xmlPdfpath.setName(pdfName);
		Product Pro = new Product();
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
			Document document = builder.parse(xmlPdfpath.getXmlPath());
			DocumentType doctype = document.getDoctype();
			if (doctype != null) {
				String intSubset = doctype.getInternalSubset();
				String[] enti = new String[3];
				if (StringUtil.getIntSubset(intSubset, enti) == true) {
					// System.out.println(intSubset);
					// System.out.println(enti[0]); //field
					// System.out.println(enti[1]); //SYSTEM
					// System.out.println(enti[2]); //フィールドパス
					if (enti[1].equals("SYSTEM")) {
						String name = enti[2]
								.substring(0, enti[2].length() - 4);
						Pro.setElement(name);
						xmlPdfpath.setFieldxmlname(name);
					}
				}
			}

			NodeList roots = document.getChildNodes();
			for (int i = 0; i < roots.getLength(); i++) {
				Node order = roots.item(i);
				String nodename = order.getNodeName();
				int type = order.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					if (nodename.equals("Gui_Product")) {
						ArrayList<ARegion> regionlist = Pro.getRegion();
						NamedNodeMap oAttrs = order.getAttributes();
						Pro.setTitle(oAttrs.getNamedItem("title")
								.getNodeValue());
						NodeList prodlist = order.getChildNodes();
						for (int l = 0; l < prodlist.getLength(); l++) {
							Node node = prodlist.item(l);
							nodename = node.getNodeName();
							type = node.getNodeType();
							if (type == Node.ELEMENT_NODE) {
								// 用紙設定
								if (nodename.equals("ProductForm")) {
									NamedNodeMap formattr = node
											.getAttributes();
									ProductForm form = new ProductForm();
									String value = node.getTextContent();
									String text = value.trim();
									form.setSizeName(text);
									form.setPageWidth(Integer.parseInt(formattr
											.getNamedItem("pageWidth")
											.getNodeValue()));
									form.setPageHeight(Integer
											.parseInt(formattr.getNamedItem(
													"pageHeight")
													.getNodeValue()));
									form.setLeftMargine(Integer
											.parseInt(formattr.getNamedItem(
													"leftMargin")
													.getNodeValue()));
									form.setRightMargine(Integer
											.parseInt(formattr.getNamedItem(
													"rightMargin")
													.getNodeValue()));
									form
											.setTopMargine(Integer
													.parseInt(formattr
															.getNamedItem(
																	"topMargin")
															.getNodeValue()));
									form.setBottomMargine(Integer
											.parseInt(formattr.getNamedItem(
													"bottomMargin")
													.getNodeValue()));
									Pro.setForm(form);
								}
								// 基本フォント設定
								else if (nodename.equals("BaseFonts")) {
									BaseFonts bfonts = new BaseFonts();
									boolean notsort = false;
									NodeList bfntlist = node.getChildNodes();
									for (int b = 0; b < bfntlist.getLength(); b++) {
										Node bnode = bfntlist.item(b);
										String name = bnode.getNodeName();
										type = node.getNodeType();
										if ((type == Node.ELEMENT_NODE)
												&& (name.equals("basefont"))) {
											NamedNodeMap bfontattr = bnode
													.getAttributes();
											Basefont bfont = new Basefont();
											int no = Integer.parseInt(bfontattr
													.getNamedItem("No.")
													.getNodeValue());
											bfont.setNo(no);
											bfont
													.setPdfFontName(bfontattr
															.getNamedItem(
																	"pdfFontName")
															.getNodeValue());
											bfont
													.setPdfEncoding(bfontattr
															.getNamedItem(
																	"pdfEncoding")
															.getNodeValue());
											Node nmnode = bfontattr
													.getNamedItem("EmbeddedPath");
											String value = nmnode != null ? nmnode
													.getNodeValue()
													: "";
											bfont.setEmbeddedPath(value);
											int index = bfonts
													.addBasefont(bfont);
											if (index != (no + 1)) {
												// String str =
												// String.format("不一致[%d]!=[%d]",
												// index, no);
												// System.out.println(str);
												notsort = true;
											}
										}
									}
									if (notsort == true) {
										System.out.println("基本フォントはソートされていません");
									}
									Pro.setBasefonts(bfonts);
								}
								// フォント設定
								else if (nodename.equals("Fonts")) {
									int maxidx = Pro.getBasefonts()
											.getBasefonts().size();
									Fonts fonts = new Fonts();
									NodeList fontlist = node.getChildNodes();
									for (int f = 0; f < fontlist.getLength(); f++) {
										Node fnode = fontlist.item(f);
										String name = fnode.getNodeName();
										type = node.getNodeType();
										if ((type == Node.ELEMENT_NODE)
												&& (name.equals("font"))) {
											NamedNodeMap fontattr = fnode
													.getAttributes();
											AFont font = new AFont();
											String value = fnode
													.getTextContent();
											String text = value.trim();
											font.setID(text);
											int idx = Integer.parseInt(fontattr
													.getNamedItem("basefont")
													.getNodeValue());
											if (idx > maxidx) {
												System.out
														.println("指定基本フォントなし");
											}
											font.setBasefon(idx);
											value = fontattr.getNamedItem(
													"point").getNodeValue();
											int p = Decimal.to10decimal(value);
											font.setPoint(p);
											Node nmnode = fontattr
													.getNamedItem("linespace");
											value = nmnode != null ? nmnode
													.getNodeValue() : "";
											p = Decimal.to100decimal(value);
											font.setLinespace(p);
											nmnode = fontattr
													.getNamedItem("Vertical");
											value = nmnode != null ? nmnode
													.getNodeValue() : "false";
											font.setVertical(value
													.equals("true"));
											nmnode = fontattr
													.getNamedItem("Bold");
											value = nmnode != null ? nmnode
													.getNodeValue() : "false";
											font.setBold(value.equals("true"));
											nmnode = fontattr
													.getNamedItem("Italic");
											value = nmnode != null ? nmnode
													.getNodeValue() : "false";
											font
													.setItalic(value
															.equals("true"));
											nmnode = fontattr
													.getNamedItem("Width");
											value = nmnode != null ? nmnode
													.getNodeValue() : "標準";
											font.setWidth(TextWidth
													.getValue(value));
											fonts.addFont(font);
										}
									}
									Pro.setFonts(fonts);
								}
								// フィールド定義
								else if (nodename.equals("Element")) {
									// フィールド定義xmlパスを取出す
									NamedNodeMap regattr = node.getAttributes();
									Node nmnode = regattr.getNamedItem("path");
									if (nmnode != null) {
										String name = nmnode.getNodeValue();
										if (name.endsWith(".xml") == true) {
											xmlPdfpath.setFieldxmlname(name
													.substring(0,
															name.length() - 4));
											Pro.setElement(name.substring(0,
													name.length() - 4));
										} else {
											xmlPdfpath.setFieldxmlname(name);
											Pro.setElement(name);
										}
									}
									// 定義有効ならフィールド要素を読込む
									NodeList fieldnode = node.getChildNodes();
									if (fieldnode != null) {
										AElement Ele;
										for (int f = 0; f < fieldnode
												.getLength(); f++) {
											Node data_n = fieldnode.item(i);
											if (data_n.getNodeType() == Node.ELEMENT_NODE) {
												Ele = Xml_Element
														.GetElement(data_n);
												String path = xmlPdfpath
														.getSerialfldPath();
												boolean ret = Serialize.Output(
														Ele, path);
												if (ret == false)
													System.out
															.println("Serialize.Outputエラー");
												else
													elementset = true;
												break; // フィールド要素のルートなのでこれ以上ないはず
											}
										}
									}
								}
								// 領域定義
								else if (nodename.equals("Region")) {
									ARegion reg = new ARegion();
									NamedNodeMap regattr = node.getAttributes();
									reg.setName(regattr.getNamedItem("name")
											.getNodeValue());
									reg.setX(Integer.parseInt(regattr
											.getNamedItem("x").getNodeValue()));
									reg.setY(Integer.parseInt(regattr
											.getNamedItem("y").getNodeValue()));
									reg.setWidth(Integer.parseInt(regattr
											.getNamedItem("width")
											.getNodeValue()));
									reg.setHeight(Integer.parseInt(regattr
											.getNamedItem("height")
											.getNodeValue()));
									Node nmnode = regattr
											.getNamedItem("disable");
									String value = nmnode != null ? nmnode
											.getNodeValue() : "false";
									reg.setDisable(value.equals("true"));
									NodeList reglist = node.getChildNodes();
									for (int r = 0; r < reglist.getLength(); r++) {
										Node rn = reglist.item(r);
										String rname = rn.getNodeName();
										type = rn.getNodeType();
										if (type == Node.ELEMENT_NODE) {
											// 繰返し設定
											if (rname.equals("Repeat_List")) {
												Repeat_List rlist = new Repeat_List();
												setRepeatList(rlist, rn);
												reg.setRepeat_list(rlist);
											}
											// 罫線設定
											else if (rname.equals("Line_List")) {
												Line_List llist = new Line_List();
												setLineList(llist, rn);
												reg.setLine_list(llist);
											}
											// 固定テキスト設定
											else if (rname
													.equals("Static_List")) {
												Static_List slist = new Static_List();
												setStaticText(slist, rn, Pro
														.getFonts());
												reg.setStatic_list(slist);
											}
											// 可変テキスト設定
											else if (rname.equals("Field_List")) {
												Field_List flist = new Field_List();
												Repeat_List rlst = reg
														.getRepeat_list();
												setFieldText(flist, rn, Pro
														.getFonts(), rlst);
												reg.setField_list(flist);
											}
										}
									}
									regionlist.add(reg);
								}
							}
						}
					}
				}
			}
			// フィールド要素を未だ読み込んでいないならここで行う
			if (elementset == false) {
				String Path = xmlPdfpath.getFieldxmlpath();
				// System.out.println("field path:"+Path);
				File element = new File(Path);
				if (element.exists() == true) {
					document = builder.parse(Path);
					roots = document.getChildNodes();
					for (int i = 0; i < roots.getLength(); i++) {
						Node order = roots.item(i);
						int type = order.getNodeType();
						if (type == Node.ELEMENT_NODE) { // 定義有効ならフィールド要素を読込む
							// String nodename = order.getNodeName();
							// System.out.println("Element:"+nodename);
							AElement Ele = Xml_Element.GetElement(order);
							String path = xmlPdfpath.getSerialfldPath();
							boolean ret = Serialize.Output(Ele, path);
							if (ret == false)
								System.out.println("Serialize.Outputエラー");
							break;
						}
					}
				}
			}
			boolean ret = Serialize.Output(Pro, xmlPdfpath.getSerialPath());
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
		return Pro;
	}

	// 繰返し設定
	private static void setRepeatList(Repeat_List rlist, Node rn) {

		NodeList ndlist = rn.getChildNodes();
		for (int i = 0; i < ndlist.getLength(); i++) {
			Node nd = ndlist.item(i);
			int type = nd.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				String name = nd.getNodeName();
				if (name.equals("Repeat")) {
					int repeatX, repeatY, intervalX, intervalY, pen_width;
					String repeatID = nd.getTextContent().trim();
					NamedNodeMap repeatattr = nd.getAttributes();
					ARepeat repeat = new ARepeat();
					Node nmnode = repeatattr.getNamedItem("repeatX");
					repeatX = nmnode != null ? Integer.parseInt(nmnode
							.getNodeValue()) : 0;
					nmnode = repeatattr.getNamedItem("repeatY");
					repeatY = nmnode != null ? Integer.parseInt(nmnode
							.getNodeValue()) : 0;
					nmnode = repeatattr.getNamedItem("intervalX");
					intervalX = nmnode != null ? Decimal.to100decimal(nmnode
							.getNodeValue()) : 0;
					nmnode = repeatattr.getNamedItem("intervalY");
					intervalY = nmnode != null ? Decimal.to100decimal(nmnode
							.getNodeValue()) : 0;
					repeat.setID(repeatID);
					//繰返し回数を補正 2015/12/07 繰返し0なら親の繰返しを使うようにするため削除
					//if ((repeatX==0)&&(repeatY>0))
					//	repeatX = 1;
					//if ((repeatY==0)&&(repeatX>0))
					//	repeatY = 1;
					repeat.setRepeatX(repeatX);
					repeat.setRepeatY(repeatY);
					repeat.setIntervalX(intervalX);
					repeat.setIntervalY(intervalY);
					nmnode = repeatattr.getNamedItem("Parent");
					String Parent = nmnode != null ? nmnode.getNodeValue() : "";
					nmnode = repeatattr.getNamedItem("pen_style");
					PenStyle.style penstyle = nmnode != null ? PenStyle.getValue(repeatattr.getNamedItem("pen_style")
							.getNodeValue()) : PenStyle.style.STRAIGHT;
					nmnode = repeatattr.getNamedItem("pen_width");
					pen_width = nmnode != null ? Decimal.to10decimal(repeatattr.getNamedItem(
							"pen_width").getNodeValue()) : 0;
					repeat.setParent(Parent);
					repeat.setPenstyle(penstyle);
					repeat.setPen_width(pen_width);
					nmnode = repeatattr.getNamedItem("Vertical");
					if (nmnode == null) {
						if ((repeatX<=1)&&(repeatY>1))
							repeat.setVertical(true);
						else
							repeat.setVertical(false);
					}
					else
						repeat.setVertical(nmnode.getNodeValue().equals("true"));
					rlist.addRepeat(repeat);
				}
			}
		}
	}

	// 罫線設定
	private static void setLineList(Line_List llist, Node rn) {

		NodeList ndlist = rn.getChildNodes();
		for (int i = 0; i < ndlist.getLength(); i++) {
			Node nd = ndlist.item(i);
			int type = nd.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				int x, y, width, height, pen_width;
				String name = nd.getNodeName();
				if (name.equals("Line")) {
					NamedNodeMap lineattr = nd.getAttributes();
					Line line = new Line();
					x = Decimal.to100decimal(lineattr.getNamedItem("x")
							.getNodeValue());
					line.setX(x);
					y = Decimal.to100decimal(lineattr.getNamedItem("y")
							.getNodeValue());
					line.setY(y);
					width = Decimal.to100decimal(lineattr
							.getNamedItem("width").getNodeValue());
					line.setWidth(width);
					height = Decimal.to100decimal(lineattr.getNamedItem(
							"height").getNodeValue());
					line.setHeight(height);
					line.setDirection(lineattr.getNamedItem("direction")
							.getNodeValue());
					line.setPen_style(lineattr.getNamedItem("pen_style")
							.getNodeValue());
					pen_width = Decimal.to10decimal(lineattr.getNamedItem(
							"pen_width").getNodeValue());
					line.setPen_width(pen_width);
					Node nmnode = lineattr.getNamedItem("repeat");
					if (nmnode != null) {
						line.setRepeatID(nmnode.getNodeValue());
						nmnode = lineattr.getNamedItem("Pos");
						if (nmnode != null)
							line.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							line.setRepPlace(REP_PLACE.BODY);
					}
					llist.addLine(line);
				} else if (name.equals("Rect")) {
					NamedNodeMap rectattr = nd.getAttributes();
					Rect rect = new Rect();
					x = Decimal.to100decimal(rectattr.getNamedItem("x")
							.getNodeValue());
					rect.setX(x);
					y = Decimal.to100decimal(rectattr.getNamedItem("y")
							.getNodeValue());
					rect.setY(y);
					width = Decimal.to100decimal(rectattr
							.getNamedItem("width").getNodeValue());
					rect.setWidth(width);
					height = Decimal.to100decimal(rectattr.getNamedItem(
							"height").getNodeValue());
					rect.setHeight(height);
					rect.setPen_style(rectattr.getNamedItem("pen_style")
							.getNodeValue());
					pen_width = Decimal.to10decimal(rectattr.getNamedItem(
							"pen_width").getNodeValue());
					rect.setPen_width(pen_width);
					Node nmnode = rectattr.getNamedItem("repeat");
					if (nmnode != null) {
						rect.setRepeatID(nmnode.getNodeValue());
						nmnode = rectattr.getNamedItem("Pos");
						if (nmnode != null)
							rect.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							rect.setRepPlace(REP_PLACE.BODY);
					}
					llist.addRect(rect);
				} else if (name.equals("RundRect")) {
					NamedNodeMap rundattr = nd.getAttributes();
					RundRect rund = new RundRect();
					x = Decimal.to100decimal(rundattr.getNamedItem("x")
							.getNodeValue());
					rund.setX(x);
					y = Decimal.to100decimal(rundattr.getNamedItem("y")
							.getNodeValue());
					rund.setY(y);
					width = Decimal.to100decimal(rundattr
							.getNamedItem("width").getNodeValue());
					rund.setWidth(width);
					height = Decimal.to100decimal(rundattr.getNamedItem(
							"height").getNodeValue());
					rund.setHeight(height);
					rund.setPen_style(rundattr.getNamedItem("pen_style")
							.getNodeValue());
					pen_width = Decimal.to10decimal(rundattr.getNamedItem(
							"pen_width").getNodeValue());
					rund.setPen_width(pen_width);
					rund.setPsition(rundattr.getNamedItem("corner")
							.getNodeValue());
					int radius = Decimal.to100decimal(rundattr.getNamedItem(
							"radius").getNodeValue());
					rund.setRadius(radius);
					Node nmnode = rundattr.getNamedItem("repeat");
					if (nmnode != null) {
						rund.setRepeatID(nmnode.getNodeValue());
						nmnode = rundattr.getNamedItem("Pos");
						if (nmnode != null)
							rund.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							rund.setRepPlace(REP_PLACE.BODY);
					}
					llist.addRundRect(rund);
				} else if (name.equals("Net")) {
					NamedNodeMap netattr = nd.getAttributes();
					Net net = new Net();
					x = Decimal.to100decimal(netattr.getNamedItem("x")
							.getNodeValue());
					net.setX(x);
					y = Decimal.to100decimal(netattr.getNamedItem("y")
							.getNodeValue());
					net.setY(y);
					width = Decimal.to100decimal(netattr.getNamedItem("width")
							.getNodeValue());
					net.setWidth(width);
					height = Decimal.to100decimal(netattr.getNamedItem(
							"height").getNodeValue());
					net.setHeight(height);
					net.setPattern(netattr.getNamedItem("pattern")
							.getNodeValue());
					Node nmnode = netattr.getNamedItem("repeat");
					if (nmnode != null) {
						net.setRepeatID(nmnode.getNodeValue());
						nmnode = netattr.getNamedItem("Pos");
						if (nmnode != null)
							net.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							net.setRepPlace(REP_PLACE.BODY);
					}
					llist.addNet(net);
				}
			}
		}
	}

	// 固定テキスト設定
	private static void setStaticText(Static_List slist, Node rn, Fonts fonts) {

		NodeList ndlist = rn.getChildNodes();
		for (int i = 0; i < ndlist.getLength(); i++) {
			Node nd = ndlist.item(i);
			int type = nd.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				int x, y, width, height, offsetX, offsetY;
				String name = nd.getNodeName();
				String text = nd.getTextContent().trim();
				text = StringUtil.peel(text);
				if (name.equals("Text")) {
					NamedNodeMap stattr = nd.getAttributes();
					StaticText st = new StaticText();
					st.setText(text);
					x = Decimal.to100decimal(stattr.getNamedItem("x")
							.getNodeValue());
					st.setX(x);
					y = Decimal.to100decimal(stattr.getNamedItem("y")
							.getNodeValue());
					st.setY(y);
					width = Decimal.to100decimal(stattr.getNamedItem("width")
							.getNodeValue());
					st.setWidth(width);
					height = Decimal.to100decimal(stattr
							.getNamedItem("height").getNodeValue());
					st.setHeight(height);
					offsetX = Decimal.to100decimal(stattr.getNamedItem(
							"offsetX").getNodeValue());
					st.setOffsetX(offsetX);
					offsetY = Decimal.to100decimal(stattr.getNamedItem(
							"offsetY").getNodeValue());
					st.setOffsetY(offsetY);
					String value = stattr.getNamedItem("font").getNodeValue();
					int idx = fonts.findFont(value);
					if (idx == 0) {
						System.out.println("指定フォント無し");
					}
					st.setFontID(idx);
					Node nmnode = stattr.getNamedItem("textAlignment");
					value = nmnode != null ? nmnode.getNodeValue() : "";
					st.setTextAlignment(value);
					nmnode = stattr.getNamedItem("verticalAlignment");
					value = nmnode != null ? nmnode.getNodeValue() : "";
					st.setVerticalAlignment(value);
					nmnode = stattr.getNamedItem("Color");
					String color = nmnode != null ? nmnode.getNodeValue() : "";
					st.setColor(color);
					nmnode = stattr.getNamedItem("bkColor");
					color = nmnode != null ? nmnode.getNodeValue() : "";
					st.setBkcolor(color);
					nmnode = stattr.getNamedItem("FullPaint"); // 印字範囲を背景色で塗り潰す。追加;
					value = nmnode != null ? nmnode.getNodeValue() : "false";
					st.setFullpaint(value.equals("true"));
					nmnode = stattr.getNamedItem("Strike");
					value = nmnode != null ? nmnode.getNodeValue() : "false";
					st.setStrike(value.equals("true"));
					nmnode = stattr.getNamedItem("Underline");
					value = nmnode != null ? nmnode.getNodeValue() : "false";
					st.setUnderline(value.equals("true"));
					nmnode = stattr.getNamedItem("repeat");
					if (nmnode != null) {
						st.setRepeatID(nmnode.getNodeValue());
						nmnode = stattr.getNamedItem("Pos");
						if (nmnode != null)
							st.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							st.setRepPlace(REP_PLACE.BODY);
					}
					slist.addText(st);
				} else if (name.equals("Image_URL")) {
					NamedNodeMap stattr = nd.getAttributes();
					StaticImageURL img = new StaticImageURL();
					img.setUrl(text);
					x = Decimal.to100decimal(stattr.getNamedItem("x")
							.getNodeValue());
					img.setX(x);
					y = Decimal.to100decimal(stattr.getNamedItem("y")
							.getNodeValue());
					img.setY(y);
					width = Decimal.to100decimal(stattr.getNamedItem("width")
							.getNodeValue());
					img.setWidth(width);
					height = Decimal.to100decimal(stattr
							.getNamedItem("height").getNodeValue());
					img.setHeight(height);
					offsetX = Decimal.to100decimal(stattr.getNamedItem(
							"offsetX").getNodeValue());
					img.setOffsetX(offsetX);
					offsetY = Decimal.to100decimal(stattr.getNamedItem(
							"offsetY").getNodeValue());
					img.setOffsetY(offsetY);
					Node nmnode = stattr.getNamedItem("repeat");
					if (nmnode != null) {
						img.setRepeatID(nmnode.getNodeValue());
						nmnode = stattr.getNamedItem("Pos");
						if (nmnode != null)
							img.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							img.setRepPlace(REP_PLACE.BODY);
					}
					slist.addImage(img);
				} else if (name.equals("Barcode")) {
					NamedNodeMap stattr = nd.getAttributes();
					StaticBarcode bar = new StaticBarcode();
					bar.setText(text);
					x = Decimal.to100decimal(stattr.getNamedItem("x")
							.getNodeValue());
					bar.setX(x);
					y = Decimal.to100decimal(stattr.getNamedItem("y")
							.getNodeValue());
					bar.setY(y);
					width = Decimal.to100decimal(stattr.getNamedItem("width")
							.getNodeValue());
					bar.setWidth(width);
					height = Decimal.to100decimal(stattr
							.getNamedItem("height").getNodeValue());
					bar.setHeight(height);
					offsetX = Decimal.to100decimal(stattr.getNamedItem(
							"offsetX").getNodeValue());
					bar.setOffsetX(offsetX);
					offsetY = Decimal.to100decimal(stattr.getNamedItem(
							"offsetY").getNodeValue());
					bar.setOffsetY(offsetY);
					bar.setCodeKind(BarCodeUtil.getValue(stattr.getNamedItem(
							"codeKind").getNodeValue()));
					Node nmnode = stattr.getNamedItem("repeat");
					if (nmnode != null) {
						bar.setRepeatID(nmnode.getNodeValue());
						nmnode = stattr.getNamedItem("Pos");
						if (nmnode != null)
							bar.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							bar.setRepPlace(REP_PLACE.BODY);
					}
					slist.addBarcode(bar);
				}
			}
		}
	}

	// フィールドテキスト設定
	private static void setFieldText(Field_List flist, Node rn, Fonts fonts,
			Repeat_List rlst) {

		NodeList ndlist = rn.getChildNodes();
		for (int i = 0; i < ndlist.getLength(); i++) {
			Node nd = ndlist.item(i);
			int type = nd.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				int x, y, width, height, offsetX, offsetY;
				String name = nd.getNodeName();
				String text = nd.getTextContent().trim();
				if (name.equals("Text")) {
					NamedNodeMap ftattr = nd.getAttributes();
					FieldText ft = new FieldText();
					ft.setText(text);
					x = Decimal.to100decimal(ftattr.getNamedItem("x")
							.getNodeValue());
					ft.setX(x);
					y = Decimal.to100decimal(ftattr.getNamedItem("y")
							.getNodeValue());
					ft.setY(y);
					width = Decimal.to100decimal(ftattr.getNamedItem("width")
							.getNodeValue());
					ft.setWidth(width);
					height = Decimal.to100decimal(ftattr
							.getNamedItem("height").getNodeValue());
					ft.setHeight(height);
					offsetX = Decimal.to100decimal(ftattr.getNamedItem(
							"offsetX").getNodeValue());
					ft.setOffsetX(offsetX);
					offsetY = Decimal.to100decimal(ftattr.getNamedItem(
							"offsetY").getNodeValue());
					ft.setOffsetY(offsetY);
					String value = ftattr.getNamedItem("font").getNodeValue();
					int idx = fonts.findFont(value);
					if (idx == 0) {
						System.out.println("指定フォント無し");
					}
					ft.setFontID(idx);
					Node nmnode = ftattr.getNamedItem("textAlignment");
					value = nmnode != null ? nmnode.getNodeValue() : "";
					ft.setTextAlignment(value);
					nmnode = ftattr.getNamedItem("verticalAlignment");
					value = nmnode != null ? nmnode.getNodeValue() : "";
					ft.setVerticalAlignment(value);
					int repeat = 1;
					nmnode = ftattr.getNamedItem("repeat");
					if (nmnode != null) {
						String rID = nmnode.getNodeValue();
						ft.setRepeatID(rID);
						nmnode = ftattr.getNamedItem("Pos");
						REP_PLACE RE = (nmnode != null) ? RepeatUtil.getValue(nmnode.getNodeValue()) : REP_PLACE.BODY;
						ft.setRepPlace(RE);
						Repeat_List.RepeatEx rp = rlst.RepeatEx(rID, RE);
						repeat = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
					}
					String path = ftattr.getNamedItem("path").getNodeValue();
					ft.setPath(path);
					nmnode = ftattr.getNamedItem("Color");
					String color = nmnode != null ? nmnode.getNodeValue() : "";
					nmnode = ftattr.getNamedItem("bkColor");
					String bkcolor = nmnode != null ? nmnode.getNodeValue()
							: "";
					nmnode = ftattr.getNamedItem("FullPaint"); // 印字範囲を背景色で塗り潰す。追加;
					value = nmnode != null ? nmnode.getNodeValue() : "false";
					boolean fullpaint = value.equals("true");
					nmnode = ftattr.getNamedItem("Strike");
					value = nmnode != null ? nmnode.getNodeValue() : "false";
					boolean strike = value.equals("true");
					nmnode = ftattr.getNamedItem("Underline");
					value = nmnode != null ? nmnode.getNodeValue() : "false";
					boolean underline = value.equals("true");
					ft.setTextAttribute(repeat, color, bkcolor, strike,
							underline, fullpaint);
					flist.addText(ft);
				} else if (name.equals("Image_URL")) {
					NamedNodeMap ftattr = nd.getAttributes();
					FieldImageURL img = new FieldImageURL();
					img.setUrl(text);
					x = Decimal.to100decimal(ftattr.getNamedItem("x")
							.getNodeValue());
					img.setX(x);
					y = Decimal.to100decimal(ftattr.getNamedItem("y")
							.getNodeValue());
					img.setY(y);
					width = Decimal.to100decimal(ftattr.getNamedItem("width")
							.getNodeValue());
					img.setWidth(width);
					height = Decimal.to100decimal(ftattr
							.getNamedItem("height").getNodeValue());
					img.setHeight(height);
					offsetX = Decimal.to100decimal(ftattr.getNamedItem(
							"offsetX").getNodeValue());
					img.setOffsetX(offsetX);
					offsetY = Decimal.to100decimal(ftattr.getNamedItem(
							"offsetY").getNodeValue());
					img.setOffsetY(offsetY);
					Node nmnode = ftattr.getNamedItem("repeat");
					if (nmnode != null) {
						img.setRepeatID(nmnode.getNodeValue());
						nmnode = ftattr.getNamedItem("Pos");
						if (nmnode != null)
							img.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							img.setRepPlace(REP_PLACE.BODY);
					}
					String path = ftattr.getNamedItem("path").getNodeValue();
					img.setPath(path);
					flist.addImage(img);
				} else if (name.equals("Barcode")) {
					NamedNodeMap ftattr = nd.getAttributes();
					FieldBarcode bar = new FieldBarcode();
					bar.setText(text);
					x = Decimal.to100decimal(ftattr.getNamedItem("x")
							.getNodeValue());
					bar.setX(x);
					y = Decimal.to100decimal(ftattr.getNamedItem("y")
							.getNodeValue());
					bar.setY(y);
					width = Decimal.to100decimal(ftattr.getNamedItem("width")
							.getNodeValue());
					bar.setWidth(width);
					height = Decimal.to100decimal(ftattr
							.getNamedItem("height").getNodeValue());
					bar.setHeight(height);
					offsetX = Decimal.to100decimal(ftattr.getNamedItem(
							"offsetX").getNodeValue());
					bar.setOffsetX(offsetX);
					offsetY = Decimal.to100decimal(ftattr.getNamedItem(
							"offsetY").getNodeValue());
					bar.setOffsetY(offsetY);
					bar.setCodeKind(BarCodeUtil.getValue(ftattr.getNamedItem(
							"codeKind").getNodeValue()));
					Node nmnode = ftattr.getNamedItem("repeat");
					if (nmnode != null) {
						bar.setRepeatID(nmnode.getNodeValue());
						nmnode = ftattr.getNamedItem("Pos");
						if (nmnode != null)
							bar.setRepPlace(RepeatUtil.getValue(nmnode.getNodeValue()));
						else
							bar.setRepPlace(REP_PLACE.BODY);
					}
					String path = ftattr.getNamedItem("path").getNodeValue();
					bar.setPath(path);
					flist.addBarcode(bar);
				}
			}
		}
	}
}
