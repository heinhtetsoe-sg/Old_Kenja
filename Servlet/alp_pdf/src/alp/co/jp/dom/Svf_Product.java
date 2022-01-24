package alp.co.jp.dom;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Document; //import org.w3c.dom.DocumentType;
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
import alp.co.jp.line.Direction;
import alp.co.jp.line.Direction.direct;
import alp.co.jp.line.Line;
import alp.co.jp.line.PenStyle;
import alp.co.jp.line.Rect;
import alp.co.jp.text.AFont;
import alp.co.jp.text.Basefont;
import alp.co.jp.text.FieldImageURL;
import alp.co.jp.text.FieldText;
import alp.co.jp.text.StaticText;
import alp.co.jp.util.ColorUtil;
import alp.co.jp.util.Decimal;
import alp.co.jp.util.Serialize;
import alp.co.jp.util.TextPosition;
import alp.co.jp.util.XmlPdfPath;

public class Svf_Product {
    private final XmlPdfPath xmlPdfpath;
    private Product Pro;
    private Fonts fonts;
    private final ArrayList<RepeatTag> repeat_ = new ArrayList<RepeatTag>();

    // 生成
    public Svf_Product(final String pdfPath) {
	// 基となるフォルダを定義
	xmlPdfpath = new XmlPdfPath(pdfPath);
    }

    // SVFのXML読込み
    public Product Read(final String pdfName) {
	xmlPdfpath.setName(pdfName);
	Pro = new Product();
	// 初期状態をセット
	Pro.setTitle(pdfName);
	// 用紙サイズ
	ProductForm form = new ProductForm();
	form.setPageWidth(595);
	form.setPageHeight(842);
	form.setLeftMargine(5);
	form.setTopMargine(5);
	form.setSizeName("A4縦");
	Pro.setForm(form);
	// 基本フォント
	BaseFonts bfonts = new BaseFonts();
	for (int i = 0; i < 4; i++) {
	    Basefont bfont = new Basefont();
	    bfont.setNo(i);
	    if (i == 0) {
		bfont.setPdfFontName("外字");
		bfont.setPdfEncoding("Identity-H");
		bfont.setEmbeddedPath("/usr/share/fonts/EUDC/EUDC.TTF");
	    } else if (i == 1) {
		bfont.setPdfFontName("ＰＣの明朝");
		bfont.setPdfEncoding("UniJIS-UCS2-HW-H");
		bfont.setEmbeddedPath("");
	    } else if (i == 2) {
		bfont.setPdfFontName("ＰＣのゴシック");
		bfont.setPdfEncoding("UniJIS-UCS2-HW-H");
		bfont.setEmbeddedPath("");
	    } else {
		bfont.setPdfFontName("Courier");
		bfont.setPdfEncoding("");
		bfont.setEmbeddedPath("");
	    }
	    bfonts.addBasefont(bfont);
	}
	Pro.setBasefonts(bfonts);
	// フォント
	fonts = new Fonts();
	Pro.setFonts(fonts);
	// 領域
	ARegion reg = new ARegion();
	reg.setName("All");
	reg.setWidth(585);
	reg.setHeight(832);
	Pro.getRegion().add(reg);
	Repeat_List rlist = new Repeat_List();
	reg.setRepeat_list(rlist);
	Line_List llist = new Line_List();
	reg.setLine_list(llist);
	Static_List slist = new Static_List();
	reg.setStatic_list(slist);
	Field_List flist = new Field_List();
	reg.setField_list(flist);
	// DocumentBuilderFactory の新しいインスタンスを取得
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// 構文解析時にドキュメントの妥当性を検証
	factory.setValidating(true);
	// 属性http://java.sun.com/xml/jaxp/properties/schemaLanguageはhttp://www.w3.org/2001/XMLSchema
	factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
		"http://www.w3.org/2001/XMLSchema");
	// DOM Document インスタンスを取得する API を定義
	DocumentBuilder builder = null;
	try {
	    builder = factory.newDocumentBuilder();
	    builder.setErrorHandler(new DefaultHandler());
	    // XML 文書全体
	    Document document = builder.parse(xmlPdfpath.getSvfPath());
	    // DocumentType doctype = document.getDoctype();
	    NodeList roots = document.getChildNodes();
	    for (int i = 0; i < roots.getLength(); i++) {
		Node order = roots.item(i);
		if (order.getNodeType() == Node.ELEMENT_NODE) {
		    String nodename = order.getNodeName();
		    if (nodename.equals("FormData")) {
			NodeList prodlist = order.getChildNodes();
			NodeRead(rlist, llist, slist, flist, prodlist, 0);
		    }
		}
	    }

	    log("node read result");
	    for (int i = 0; i < flist.getText_().size(); i++) {
		log(" flist text " + i + " = " + flist.getText_().get(i));
	    }

	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return Pro;
    }

    private static void log(final String s) {
	System.out.println(" Svf_Product: " + s);
    }

    private static String attrValue(final Node node, final String name) {
	return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    private static String attrValue(final Node node, final String name, final String def) {
	final String v = attrValue(node, name);
	if (null == v) {
	    return def;
	}
	return v;
    }

    // SVFのXML内定義ノード読込み
    private void NodeRead(final Repeat_List rlist, final Line_List llist, final Static_List slist,
	    final Field_List flist, final NodeList prodlist, final int level) {
	// フォントと繰返しを採取
	for (final Node node : new NodeIterator(prodlist, Node.ELEMENT_NODE)) {
	    String nodename = node.getNodeName();
	    if (nodename.equals("Text")) {
		int point = (int) (Float.parseFloat(attrValue(node, "point")) * 10);
		int kind = Integer.parseInt(attrValue(node, "fontNum", "0"));
		int d = Integer.parseInt(attrValue(node, "direction"));
		int pitch = 0;
		final String id = getFontId(point, kind, d, pitch);
		if (fonts.findFont(id) == 0) {
		    AFont af = createAFont(point, kind, d, pitch, id);
		    fonts.addFont(af);
		}
	    } else if (nodename.equalsIgnoreCase("Field")) {
		int point = (int) (Float.parseFloat(attrValue(node, "point")) * 10);
		int kind = Integer.parseInt(attrValue(node, "fontNum", "0"));
		int d = Integer.parseInt(attrValue(node, "direction"));
		int pitch = Decimal.From400To7200(attrValue(node, "pitch"));
		final String id = getFontId(point, kind, d, pitch);
		if (fonts.findFont(id) == 0) {
		    AFont af = createAFont(point, kind, d, pitch, id);
		    fonts.addFont(af);
		}
	    }
	    // 繰返し
	    else if (nodename.equalsIgnoreCase("Repeat")) {
		ARepeat ap = fromSvfToRepeat(node);
		rlist.addRepeat(ap);
	    }
	}
	// オブジェクト作成
	for (final Node node : new NodeIterator(prodlist, Node.ELEMENT_NODE)) {
	    String nodename = node.getNodeName();
	    if (nodename.equalsIgnoreCase("Bitmap")) {
		FieldImageURL img = fromSvfToFieldImageURL(node);
		flist.addImage(img);
	    }
	    // 直線
	    else if (nodename.equalsIgnoreCase("Line")) {
		Line ln = fromSvfToLine(node);
		llist.addLine(ln);
	    }
	    // 矩形
	    else if (nodename.equalsIgnoreCase("Box")) {
		Rect rc = fromSvfToRect(node);
		llist.addRect(rc);
	    }
	    // テキスト
	    else if (nodename.equals("Text")) {
		StaticText st = fromSvfToStaticText(node);
		slist.addText(st);
	    }
	    // フィールド(可変)テキスト
	    else if (nodename.equalsIgnoreCase("Field")) {
		FieldText ft = fromSvfToFieldText(node);
		flist.addText(ft);
	    } else if (nodename.equalsIgnoreCase("SubForm")) {
		// サブフォーム
		if (level > 0) { // 入れ子は不可
		    continue;
		}
		// サブフォームを領域に定義
		String subname = attrValue(node, "name");
		// 領域
		// ARegion rreg = new ARegion();
		// rreg.setName(value);
		int x = Decimal.From400To7200(attrValue(node, "x1"));
		int y = Decimal.From400To7200(attrValue(node, "y1"));
		int x2 = Decimal.From400To7200(attrValue(node, "x2"));
		int y2 = Decimal.From400To7200(attrValue(node, "y2"));
		int d = Integer.parseInt(attrValue(node, "direction"));
		int width = x2 - x;
		int height = y2 - y;
		NodeList rprodlist = node.getChildNodes();
		for (final Node rorder : new NodeIterator(rprodlist, Node.ELEMENT_NODE)) {
		    String rnodename = rorder.getNodeName();
		    if (rnodename.equals("Record")) {
			NodeList rrprodlist = rorder.getChildNodes();
			Repeat_List rrlist = new Repeat_List();
			Line_List rllist = new Line_List();
			Static_List rslist = new Static_List();
			Field_List rflist = new Field_List();
			NodeRead(rrlist, rllist, rslist, rflist, rrprodlist, 1);
			int rx = Decimal.From400To7200(attrValue(rorder, "x1"));
			int ry = Decimal.From400To7200(attrValue(rorder, "y1"));
			int rxl = Decimal.From400To7200(attrValue(rorder, "x2")) - rx;
			int ryl = Decimal.From400To7200(attrValue(rorder, "y2")) - ry;
			int repeatX = 0, repeatY = 0;
			if (d == 1) {
			    repeatY = (height / ryl) != 0 ? height / ryl : 1;
			    rxl = 1;
			    System.out.println(String.format("DefRepeat Y[%d] Interval[%d]", repeatY, ryl));
			} else {
			    repeatX = (width / rxl) != 0 ? width / rxl : 1;
			    ryl = 1;
			    System.out.println(String.format("DefRepeat X [%d] Interval[%d]", repeatX, rxl));
			}
			DefRepeat(subname, repeatX, repeatY, rxl, ryl, rlist, rllist, rslist, rflist);
			llist.AddAll(rllist);
			slist.AddAll(rslist);
			flist.AddAll(rflist);
		    }
		}
	    } else if (nodename.equalsIgnoreCase("SubProperties.Box")) {
		if (attrValue(node, "fillingOutline").equals("true")) {
		    Rect rc = fromSvfToRect2(node);
		    llist.addRect(rc);
		}
	    } else if (nodename.equalsIgnoreCase("Paper")) {
		int size = Integer.parseInt(attrValue(node, "size"));
		int direction = Integer.parseInt(attrValue(node, "direction"));
		SetPaperSize(size, direction);
	    }
	}
    }

    private String getFontId(final int point, final int kind, final int d, final int pitch) {
	String id;
	if (pitch > 0) {
	    if (kind == 1) { // ゴシック
		if (d == 1) { // 縦書き
		    id = String.format("G%03dV%03d", point, pitch);
		} else {
		    // 横書き
		    id = String.format("G%03dH%03d", point, pitch);
		}
	    } else
	    // 明朝
	    if (d == 1) { // 縦書き
		id = String.format("M%03dV%03d", point, pitch);
	    } else {
		// 横書き
		id = String.format("M%03dH%03d", point, pitch);
	    }
	} else {
	    if (kind == 1) { // ゴシック
		if (d == 1) { // 縦書き
		    id = String.format("GOTV%04d", point);
		} else {
		    // 横書き
		    id = String.format("GOTH%04d", point);
		}
	    } else
	    // 明朝
	    if (d == 1) { // 縦書き
		id = String.format("MINV%04d", point);
	    } else {
		// 横書き
		id = String.format("MINH%04d", point);
	    }
	}
	return id;
    }

    public AFont createAFont(final int point, final int kind, final int d, final int pitch, final String id) {
	AFont af = new AFont();
	if (kind == 1) { // ゴシック
	    af.setBasefon(2);
	} else {
	    // 明朝
	    af.setBasefon(1);
	}
	af.setPoint(point);
	af.setVertical(d == 1);
	af.setLinespace(pitch);
	af.setID(id);
	return af;
    }

    private Rect fromSvfToRect2(final Node node) {
	int x = Decimal.From400To7200(attrValue(node, "x1"));
	int y = Decimal.From400To7200(attrValue(node, "y1"));
	int x2 = Decimal.From400To7200(attrValue(node, "x2"));
	int y2 = Decimal.From400To7200(attrValue(node, "y2"));
	int penwidth = Integer.parseInt(attrValue(node, "lineWidth"));
	if (penwidth == 0) {
	    penwidth = Integer.parseInt(attrValue(node, "lineWidthDot"));
	}
	penwidth = (penwidth * 720 + 200) / 400;
	int lineType = Integer.parseInt(attrValue(node, "lineType"));
	Rect rc = new Rect();
	rc.setPen_width(penwidth);
	setRect(rc, x, y, x2, y2, lineType);
	return rc;
    }

    private FieldText fromSvfToFieldText(final Node node) {
	TextPosition.position tpos = TextPosition.position.LEFT;
	String path = attrValue(node, "name");
	int x = Decimal.From400To7200(attrValue(node, "x"));
	int y = Decimal.From400To7200(attrValue(node, "y"));
	int point = (int) (Float.parseFloat(attrValue(node, "point")) * 10);
	int pitch = Decimal.From400To7200(attrValue(node, "pitch"));
	int kind = Integer.parseInt(attrValue(node, "fontNum", "0"));
	int charcount = Integer.parseInt(attrValue(node, "charCountDisp"));
	int d = Integer.parseInt(attrValue(node, "direction"));
	int length = ((point + pitch) * charcount) / 2 + 1;
	int editStyle = Integer.parseInt(attrValue(node, "editStyle"));
	if (editStyle == 1) {
	    tpos = TextPosition.position.RIGHT;
	} else if (editStyle == 3) {
	    tpos = TextPosition.position.CENTER;
	} else if (editStyle == 4) {
	    tpos = TextPosition.position.AVE;
	}
	FieldText ft = new FieldText();
	ft.setText("");
	ft.setFontID(DefFont(point, kind, d, pitch));
	ft.setX(x);
	ft.setY(y);
	ft.setTextAlignment(tpos.getName());
	point *= 10;
	if (d == 1) { // 縦書き
	    ft.setWidth(point);
	    ft.setHeight((point + pitch) * charcount - pitch);
	    // 縦のSvfスタイル不明。
	    if (editStyle == 3) {
		ft.setVerticalAlignment(TextPosition.position.CENTER.getName());
	    } else { // 全て上詰めにする。
		ft.setVerticalAlignment(TextPosition.position.UPPER.getName());
	    }
	} else { // 横書き
	    length = ((point + pitch * 2) * charcount) / 2 - pitch;
	    ft.setWidth(length);
	    ft.setHeight(point);
	    ft.setVerticalAlignment(TextPosition.position.LEFT.getName());
	}
	// 可変フィールドの繰返し属性をセット
	ft.setTextAttribute(1, ColorUtil.getName(Color.BLACK), ColorUtil.getName(Color.BLACK), false, false);
	x = y = 1;
	RepeatTag tg = FindRepeatTag(path);
	if (tg != null) {
	    ft.setPath(path + "[0]"); // 繰返しはパスを配列にする。
	    ft.setRepeatID(tg.getRepeatID());
	} else {
	    ft.setPath(path);
	}
	return ft;
    }

    private StaticText fromSvfToStaticText(final Node node) {
	String name = attrValue(node, "name");
	int x = Decimal.From400To7200(attrValue(node, "x"));
	int y = Decimal.From400To7200(attrValue(node, "y"));
	int point = (int) (Float.parseFloat(attrValue(node, "point")) * 10);
	int pitch = Decimal.From400To7200(attrValue(node, "pitch"));
	int kind = Integer.parseInt(attrValue(node, "fontNum", "0"));
	int d = Integer.parseInt(attrValue(node, "direction"));
	String text = attrValue(node, "strText");
	StaticText st = new StaticText();
	st.setText(text);
	st.setFontID(DefFont(point, kind, d, 0));
	st.setX(x);
	st.setY(y);
	st.setTextAlignment(TextPosition.position.LEFT.getName());
	point *= 10;
	if (d == 1) { // 縦書き
	    st.setTextAlignment(TextPosition.position.LEFT.getName());
	    st.setWidth(point);
	    if (pitch > 0) {
		st.setHeight((point + pitch) * text.length() - pitch);
		st.setVerticalAlignment(TextPosition.position.AVE.getName());
	    } else {
		st.setHeight(point * text.length());
		st.setVerticalAlignment(TextPosition.position.UPPER.getName());
	    }
	} else { // 横書き
	    st.setHeight(point);
	    st.setVerticalAlignment(TextPosition.position.LEFT.getName());
	    if (pitch > 0) {
		st.setWidth((point + pitch) * text.length() - pitch);
		st.setTextAlignment(TextPosition.position.AVE.getName());
	    } else {
		st.setWidth(point * text.length() + point);
		st.setTextAlignment(TextPosition.position.LEFT.getName());
	    }
	}
	// System.out.println(String.format("%s 幅[%d] 高さ[%d]", text,
	// st.getWidth(), st.getHeight()));
	RepeatTag tg = FindRepeatTag(name);
	if (tg != null) {
	    st.setRepeatID(tg.getRepeatID());
	}
	return st;
    }

    private Rect fromSvfToRect(final Node node) {
	String name = attrValue(node, "name");
	Rect rc = fromSvfToRect2(node);
	RepeatTag tg = FindRepeatTag(name);
	if (tg != null) {
	    rc.setRepeatID(tg.getRepeatID());
	}
	return rc;
    }

    private Line fromSvfToLine(final Node node) {
	String name = attrValue(node, "name");
	int x = Decimal.From400To7200(attrValue(node, "x1"));
	int y = Decimal.From400To7200(attrValue(node, "y1"));
	int x2 = Decimal.From400To7200(attrValue(node, "x2"));
	int y2 = Decimal.From400To7200(attrValue(node, "y2"));
	int penwidth = Integer.parseInt(attrValue(node, "lineWidth"));
	if (penwidth == 0) {
	    penwidth = Integer.parseInt(attrValue(node, "lineWidthDot"));
	}
	penwidth = (penwidth * 720 + 200) / 400;
	int lineType = Integer.parseInt(attrValue(node, "lineType"));
	Line ln = new Line();
	ln.setPen_width(penwidth);
	setLine(ln, x, y, x2, y2, lineType);
	RepeatTag tg = FindRepeatTag(name);
	if (tg != null) {
	    ln.setRepeatID(tg.getRepeatID());
	}
	return ln;
    }

    private FieldImageURL fromSvfToFieldImageURL(final Node node) {
	String name = attrValue(node, "name");
	int x = Decimal.From400To7200(attrValue(node, "x1"));
	int y = Decimal.From400To7200(attrValue(node, "y1"));
	int x2 = Decimal.From400To7200(attrValue(node, "x2"));
	int y2 = Decimal.From400To7200(attrValue(node, "y2"));
	FieldImageURL img = new FieldImageURL();
	img.setUrl("");
	img.setPath(name);
	img.setX(x);
	img.setY(y);
	img.setWidth(x2 - x);
	img.setHeight(y2 - y);
	RepeatTag tg = FindRepeatTag(name);
	if (tg != null) {
	    img.setRepeatID(tg.getRepeatID());
	}
	return img;
    }

    private ARepeat fromSvfToRepeat(final Node node) {
	String rID = attrValue(node, "name");
	int count = Integer.parseInt(attrValue(node, "count"));
	int d = Integer.parseInt(attrValue(node, "direction"));
	int pitch = Decimal.From400To7200(attrValue(node, "pitch"));
	StringTokenizer st1 = new StringTokenizer(attrValue(node, "strIncludeComp"), ",");
	RepeatTag rep = new RepeatTag();
	rep.setRepeatID(rID);
	for (int sz = st1.countTokens(); sz > 0; sz--) {
	    rep.AddName(st1.nextToken());
	}
	repeat_.add(rep);
	ARepeat ap = new ARepeat();
	ap.setID(rID);
	if (d == 1) {
	    ap.setRepeatX(1);
	    ap.setRepeatY(count);
	    ap.setIntervalY(pitch);
	} else {
	    ap.setRepeatX(count);
	    ap.setRepeatY(1);
	    ap.setIntervalX(pitch);
	}
	return ap;
    }

    // 用紙定義
    private void SetPaperSize(final int size, final int direction) {
	int a, b;
	StringBuffer name = new StringBuffer();
	switch (size) {
	case 0: // ハガキ
	    name.append("ハガキ");
	    a = 283;
	    b = 421;
	    break;
	case 1: // A5
	    name.append("A5");
	    a = 421;
	    b = 595;
	    break;
	case 3: // A3
	    name.append("A3");
	    a = 842;
	    b = 1190;
	    break;
	case 4: // B5
	    name.append("B5");
	    a = 501;
	    b = 709;
	    break;
	case 5: // B4
	    name.append("B4");
	    a = 709;
	    b = 1002;
	    break;
	case 6: // レター
	    name.append("レター");
	    a = 612;
	    b = 792;
	    break;
	case 7: // 連帳ないのでリーガル
	    name.append("リーガル");
	    a = 612;
	    b = 1008;
	    break;
	case 2: // A4
	default:
	    name.append("A4");
	    a = 595;
	    b = 842;
	}
	ProductForm Form = Pro.getForm();
	ARegion reg = Pro.getRegion().get(0);
	if (direction == 0) { // 横
	    name.append("横");
	    Form.setPageWidth(b);
	    Form.setPageHeight(a);
	    reg.setWidth(b - Form.getLeftMargine());
	    reg.setHeight(a - Form.getTopMargine());
	} else { // 縦
	    name.append("縦");
	    Form.setPageWidth(a);
	    Form.setPageHeight(b);
	    reg.setWidth(a - Form.getLeftMargine());
	    reg.setHeight(b - Form.getTopMargine());
	}
	Form.setSizeName(name.toString());
    }

    // 各オブジェクトの繰返し定義
    private static void DefRepeat(final String subname, final int repeatX, final int repeatY, final int xl,
	    final int yl, final Repeat_List rlist, final Line_List llist, final Static_List slist,
	    final Field_List flist) {
	ARepeat ar = new ARepeat();
	ar.setID(subname);
	if (repeatX > 0) {
	    ar.setRepeatX(repeatX);
	    ar.setIntervalX(xl);
	}
	if (repeatY > 0) {
	    ar.setRepeatY(repeatY);
	    ar.setIntervalY(yl);
	}
	rlist.addRepeat(ar);
	// 繰返し付加
	// 罫線
	for (Line orgln : llist.getLine_()) {
	    if (repeatX > 1) {
		if ((orgln.getDirection() == direct.U_H) || (orgln.getDirection() == direct.B_H)) {
		    int x = orgln.getWidth();
		    if (x >= xl) {
			orgln.setWidth(x + (xl * (repeatX - 1)));
			continue;
		    }
		}
	    }
	    if (repeatY > 1) {
		if ((orgln.getDirection() == direct.L_V) || (orgln.getDirection() == direct.R_V)) {
		    int y = orgln.getHeight();
		    if (y >= yl) {
			orgln.setHeight(y + (yl * (repeatY - 1)));
			continue;
		    }
		}
	    }
	    orgln.setRepeatID(subname);
	}
	// 矩形
	for (Rect orgrc : llist.getRect_()) {
	    orgrc.setRepeatID(subname);
	}
	// 固定文字
	for (StaticText orgst : slist.getText_()) {
	    orgst.setRepeatID(subname);
	}
	// 可変フィールド
	for (FieldText orgft : flist.getText_()) {
	    String path = orgft.getPath();
	    orgft.setPath(path + "[0]"); // 繰返しはパスを配列にする。
	    Color color = orgft.getColor(0);
	    Color bkcolor = orgft.getBkcolor(0);
	    boolean strike = orgft.isStrike(0);
	    boolean underline = orgft.isUnderline(0);
	    orgft.setRepeatID(subname);
	    orgft.setTextAttribute(repeatX * repeatY, ColorUtil.getName(color), ColorUtil.getName(bkcolor), strike,
		    underline);
	}
    }

    // フォント種別定義
    private int DefFont(final int point, final int kind, final int D, final int pitch) {
	String value;
	if (pitch == 0) {
	    if (kind == 1) { // ゴシック
		if (D == 1) { // 縦書き
		    value = String.format("GOTV%04d", point);
		} else {
		    // 横書き
		    value = String.format("GOTH%04d", point);
		}
	    } else { // 明朝
		if (D == 1) { // 縦書き
		    value = String.format("MINV%04d", point);
		} else {
		    // 横書き
		    value = String.format("MINH%04d", point);
		}
	    }
	} else {
	    if (kind == 1) { // ゴシック
		if (D == 1) { // 縦書き
		    value = String.format("G%03dV%03d", point, pitch);
		} else {
		    // 横書き
		    value = String.format("G%03dH%03d", point, pitch);
		}
	    } else
	    // 明朝
	    if (D == 1) { // 縦書き
		value = String.format("M%03dV%03d", point, pitch);
	    } else {
		// 横書き
		value = String.format("M%03dH%03d", point, pitch);
	    }

	}
	return fonts.findFont(value);
    }

    // シリアルオブジェクト化
    public boolean SerializePDF(final String pdfName) {
	if (Pro == null) {
	    return false;
	}
	if ((pdfName != null) && (pdfName.length() > 0)) {
	    xmlPdfpath.setName(pdfName);
	}
	return Serialize.Output(Pro, xmlPdfpath.getSerialPath());
    }

    // 直線定義
    private void setLine(final Line ln, final int x, final int y, final int x2, final int y2, final int lineType) {
	int x0, y0, width, height;
	Direction.direct d;
	PenStyle.style s;

	if (x <= x2) {
	    x0 = x;
	    width = x2 - x;
	} else {
	    x0 = x2;
	    width = x - x2;
	}
	if (y <= y2) {
	    y0 = y;
	    height = y2 - y;
	} else {
	    y0 = y2;
	    height = y - y2;

	}
	if (width == 0) {
	    d = Direction.direct.L_V; // 左・縦
	    width = 200; // GUIで認識可能なように幅を広げた
	} else if (height == 0) {
	    d = Direction.direct.U_H; // 上・横
	    height = 200; // GUIで認識可能なように高さを広げた
	} else {
	    if (y < y2) {
		d = Direction.direct.LH_SL; // 左上から右下
	    } else {
		d = Direction.direct.RH_SL; // 右上から左下
	    }
	}
	if (lineType == 1) {
	    s = PenStyle.style.DOT;
	} else if (lineType == 2) {
	    s = PenStyle.style.DASH;
	} else if (lineType == 3) {
	    s = PenStyle.style.LDASH;
	} else if (lineType == 4) {
	    s = PenStyle.style.DASHDOT;
	} else if (lineType == 5) {
	    s = PenStyle.style.DASHDOTDOT;
	} else {
	    s = PenStyle.style.STRAIGHT;
	}
	ln.setX(x0);
	ln.setY(y0);
	ln.setWidth(width);
	ln.setHeight(height);
	ln.setDirection(d.getName());
	ln.setPen_style(s.getName());
    }

    // 矩形定義
    private static void setRect(final Rect rc, final int x, final int y, final int x2, final int y2,
	    final int lineType) {
	int x0, y0, width, height;
	PenStyle.style s;

	if (x <= x2) {
	    x0 = x;
	    width = x2 - x;
	} else {
	    x0 = x2;
	    width = x - x2;
	}
	if (y <= y2) {
	    y0 = y;
	    height = y2 - y;
	} else {
	    y0 = y2;
	    height = y - y2;

	}
	if (width == 0) {
	    width = 1;
	} else if (height == 0) {
	    height = 1;
	}
	if (lineType == 1) {
	    s = PenStyle.style.DOT;
	} else if (lineType == 2) {
	    s = PenStyle.style.DASH;
	} else if (lineType == 3) {
	    s = PenStyle.style.LDASH;
	} else if (lineType == 4) {
	    s = PenStyle.style.DASHDOT;
	} else if (lineType == 5) {
	    s = PenStyle.style.DASHDOTDOT;
	} else {
	    s = PenStyle.style.STRAIGHT;
	}
	rc.setX(x0);
	rc.setY(y0);
	rc.setWidth(width);
	rc.setHeight(height);
	rc.setPen_style(s.getName());
    }

    // 繰返しタグの配列から繰返しタグクラス検索
    private RepeatTag FindRepeatTag(final String name) {
	for (int i = 0; i < repeat_.size(); i++) {
	    RepeatTag tg = repeat_.get(i);
	    if (tg.isIncludeName(name)) {
		return tg;
	    }
	}
	return null;
    }

    // 繰返しタグクラス
    private class RepeatTag {
	private String repeatID;
	ArrayList<String> names = new ArrayList<String>();

	public void AddName(final String name) {
	    names.add(name);
	}

	public boolean isIncludeName(final String name) {
	    return names.contains(name);
	}

	public void setRepeatID(final String repeatID) {
	    this.repeatID = repeatID;
	}

	public String getRepeatID() {
	    return repeatID;
	}
    }

    private static class NodeIterator implements Iterable<Node>, Iterator<Node> {
	int idx = 0;
	final List<Node> _nodes = new ArrayList<Node>();

	NodeIterator(final NodeList nodeList, final short nodeType) {
	    for (int i = 0, len = nodeList.getLength(); i < len; i++) {
		Node node = nodeList.item(i);
		if (node.getNodeType() == nodeType) {
		    _nodes.add(node);
		}
	    }
	}

	@Override
	public Iterator<Node> iterator() {
	    return this;
	}

	@Override
	public boolean hasNext() {
	    return idx < _nodes.size();
	}

	@Override
	public Node next() {
	    final Node node = _nodes.get(idx);
	    idx += 1;
	    return node;
	}

	public void remove() {
	    throw new NotImplementedException();
	}
    }
}
