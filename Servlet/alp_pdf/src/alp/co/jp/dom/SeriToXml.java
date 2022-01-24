package alp.co.jp.dom;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
import alp.co.jp.element.Elements;
import alp.co.jp.line.Line;
import alp.co.jp.line.Net;
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
import alp.co.jp.util.ColorUtil;
import alp.co.jp.util.Decimal;
import alp.co.jp.util.Serialize;
import alp.co.jp.util.TextPosition;
import alp.co.jp.util.TextWidth;
import alp.co.jp.util.XmlPdfPath;

public class SeriToXml {

    public XmlPdfPath xmlPdfpath;
    Product Pro = null;
    PrintWriter prw = null;
    String element_name;
    boolean ele_seiial_exist;
    Elements Ele = null;

    public SeriToXml(final String xmlPath) {
	// 各種ファイルのパス取出し準備
	xmlPdfpath = new XmlPdfPath(xmlPath);
    }

    // プロダクトオブジェクトを読込む
    public boolean Init(final String xmlName) {
	xmlPdfpath.setName(xmlName);

	// シリアライズ入力
	// プロダクト
	Pro = new Product();
	Pro = (Product) Serialize.Input(xmlPdfpath.getSerialPath());
	if (Pro == null) {
	    System.out.println("Serialize.Inputエラー");
	    return false;
	}
	return true;
    }

    // XMLファイル作成
    public boolean WriteXml(final String xml_name) {

	// XMLファイルパス取出し
	if ((xml_name != null) && (xml_name.length() > 0)) {
	    xmlPdfpath.setName(xml_name);
	}
	{
	    final String XmlPath = xmlPdfpath.getXmlPath();
	    ele_seiial_exist = new File(xmlPdfpath.getSerialfldPath()).exists();
	    System.out.println(" SerToXml :: file " + xmlPdfpath.getSerialfldPath() + " exists? = " + ele_seiial_exist);
	    try {
		prw = new PrintWriter(new FileWriter(XmlPath));
	    } catch (IOException e) {
		e.printStackTrace();
		return false;
	    }
	}
	{
	    // ヘッダー
	    WriteXmlLine(0, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
	    // タイトル
	    String title = Pro.getTitle();
	    WriteXmlLine(0, String.format("<Gui_Product title=\"%s\">", title));
	    // 用紙サイズ
	    ProductForm form = Pro.getForm();
	    int width = form.getPageWidth();
	    int height = form.getPageHeight();
	    int leftmargine = form.getLeftMargine();
	    int rightmargine = form.getRightMargine();
	    int topmargine = form.getTopMargine();
	    int bottommargine = form.getBottomMargine();
	    WriteXmlLine(1, String.format("<ProductForm pageWidth=\"%d\" pageHeight=\"%d\"", width, height));
	    WriteXmlLine(2,
		    String.format("leftMargin=\"%d\" rightMargin=\"%d\" topMargin=\"%d\" bottomMargin=\"%d\">%s",
			    leftmargine, rightmargine, topmargine, bottommargine, form.getSizeName()));
	    WriteXmlLine(1, "</ProductForm>");
	}
	{ // 基本フォント
	    WriteXmlLine(1, "<BaseFonts>");
	    BaseFonts bfs = Pro.getBasefonts();
	    int sz = bfs.getSize();
	    for (int idx = 0; idx < sz; idx++) {
		Basefont bf = bfs.getBasefont(idx);
		final String line;
		if (idx == 0) {
		    line = String.format(
			    "<basefont No.=\"0\" pdfFontName=\"外字\" pdfEncoding=\"Identity-H\" EmbeddedPath=\"%s\"/>",
			    bf.getEmbeddedPath());
		} else {
		    line = String.format(
			    "<basefont No.=\"%d\" pdfFontName=\"%s\" pdfEncoding=\"%s\" EmbeddedPath=\"%s\"/>", idx,
			    bf.getPdfFontName(), bf.getPdfEncoding(), bf.getEmbeddedPath());
		}
		WriteXmlLine(2, line);
	    }
	    WriteXmlLine(1, "</BaseFonts>");
	}
	{ // フォント
	    WriteXmlLine(1, "<Fonts>");
	    Fonts fs = Pro.getFonts();
	    for (int idx = 0, sz = fs.getSize(); idx < sz; idx++) {
		AFont af = fs.getAfont(idx + 1);
		int p = af.getPoint();
		int l = af.getLinespace();
		StringBuffer line = new StringBuffer(
			String.format("<font basefont=\"%d\" point=\"%s\" linespace=\"%s\"", af.getBasefon(),
				Decimal.ToDecimalS1(p), Decimal.ToDecimalS2(l)));
		if (af.getVertical() == true) {
		    line.append(" Vertical=\"true\"");
		}
		if (af.getBold() == true) {
		    line.append(" Bold=\"true\"");
		}
		if (af.getItalic() == true) {
		    line.append(" Italic=\"true\"");
		}
		Float w = af.getWidth();
		if ((w.floatValue() > 0.0f) && (w.equals(TextAttribute.WIDTH_REGULAR) == false)) {
		    line.append(String.format(" Width=\"%s\"", TextWidth.getName(w)));
		}
		line.append(String.format(">%s", af.getID()));
		WriteXmlLine(2, line.toString());
		WriteXmlLine(2, "</font>");
	    }
	    WriteXmlLine(1, "</Fonts>");
	}
	{ // フィールド要素
	    element_name = Pro.getElement();
	    if (element_name == null) {
		element_name = xmlPdfpath.getFieldxmlname();
	    } else {
		xmlPdfpath.setFieldxmlname(element_name);
	    }
	    WriteXmlLine(1, String.format("<Element path=\"%s.xml\"/>", element_name));
	    if (ele_seiial_exist == false) {
		AElement aele = new AElement(Pro.getTitle() + "データ", "");
		Ele = new Elements(aele);
	    }
	}
	{ // 領域
	    for (ARegion ar : Pro.getRegion()) {
		StringBuffer line = new StringBuffer(
			String.format("<Region name=\"%s\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\">", ar.getName(),
				ar.getX(), ar.getY(), ar.getWidth(), ar.getHeight()));
		if (ar.isDisable() == true) {
		    line.append(" disable=\"true\"");
		}
		WriteXmlLine(1, line.toString());
		Region(ar);
		WriteXmlLine(1, "</Region>");
	    }
	    // フィールド要素Xmlを作成
	    File element = new File(xmlPdfpath.getFieldxmlpath());
	    if (element.exists()) {
		System.out.println("SeriToXml: not output file " + element);
	    } else {
		AElement aele;
		if (ele_seiial_exist == false) {
		    aele = Ele.getElement();
		} else {
		    aele = (AElement) Serialize.Input(xmlPdfpath.getSerialfldPath());
		}
		try {
		    System.out.println("SeriToXML output " + element);
		    PrintWriter elementwtr = new PrintWriter(element);
		    elementwtr.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		    element_tag(aele, 0, elementwtr);
		    elementwtr.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
	    }
	}
	{
	    WriteXmlLine(0, "</Gui_Product>");
	}
	//
	try {
	    prw.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    protected void WriteXmlLine(final int tabCount, final String line) {
	StringBuffer buff = new StringBuffer(256);
	for (int i = 0; i < tabCount; i++) {
	    buff.append("\t");
	}
	buff.append(line);
	buff.append("\n");
	prw.write(buff.toString());
    }

    // 領域
    private void Region(final ARegion ar) {
	// 繰返し
	Repeat_List rl = ar.getRepeat_list();
	WriteXmlLine(2, "<Repeat_List>");
	if (ar.getRepeat_list() != null) {
	    for (int i = 1, sz = rl.getSize(); i <= sz; i++) {
		ARepeat rp = rl.getARepeat(i);
		int repeatx = rp.getRepeatX();
		int repeaty = rp.getRepeatY();
		int intervalx = rp.getIntervalX();
		int intervaly = rp.getIntervalY();
		StringBuffer line = new StringBuffer("<Repeat");
		if (repeatx > 1) {
		    line.append(
			    String.format(" intervalX=\"%s\" repeatX=\"%d\"", Decimal.ToDecimalS2(intervalx), repeatx));
		}
		if (repeaty > 1) {
		    line.append(
			    String.format(" intervalY=\"%s\" repeatY=\"%d\"", Decimal.ToDecimalS2(intervaly), repeaty));
		}
		if (rp.isVertical()) {
		    line.append(String.format(" Vertical=\"true\""));
		}
		String Parent = rp.getParent();
		if ((Parent != null) && (Parent.length() > 0)) {
		    line.append(String.format(" Parent=\"%s\"", Parent));
		}
		int pen_width = rp.getPen_width();
		if (pen_width > 0) {
		    line.append(String.format(" pen_style=\"%s\" pen_width=\"%s\"", rp.getPenstyle().getName(),
			    Decimal.ToDecimalS1(pen_width)));
		}
		line.append(String.format(">%s</Repeat>", rp.getID()));
		WriteXmlLine(3, line.toString());
	    }
	}
	WriteXmlLine(2, "</Repeat_List>");
	// 罫線
	WriteXmlLine(2, "<Line_List>");
	if (ar.getLine_list() != null) {
	    Line_List ll = ar.getLine_list();
	    // 罫線定義行
	    for (Net nt : ll.getNet_()) {
		xml_Net(nt);
	    }
	    for (Rect rc : ll.getRect_()) {
		xml_Rect(rc);
	    }
	    for (RundRect rr : ll.getRundrect_()) {
		xml_RundRect(rr);
	    }
	    for (Line ln : ll.getLine_()) {
		xml_Line(ln);
	    }
	}
	WriteXmlLine(2, "</Line_List>");
	// 固定フィールド
	WriteXmlLine(2, "<Static_List>");
	if (ar.getStatic_list() != null) {
	    Static_List sl = ar.getStatic_list();
	    // 固定フィールド定義行
	    for (StaticText st : sl.getText_()) {
		xml_StaticText(st);
	    }
	    for (StaticImageURL im : sl.getImage_()) {
		xml_StaticImage(im);
	    }
	    for (StaticBarcode br : sl.getBarcode_()) {
		xml_StaticBarcode(br);
	    }
	}
	WriteXmlLine(2, "</Static_List>");
	// 可変フィールド
	WriteXmlLine(2, "<Field_List>");
	if (ar.getField_list() != null) {
	    Field_List fl = ar.getField_list();
	    // 可変フィールド定義行
	    for (final FieldText ft : fl.getText_()) {
		xml_FieldText(ft);
		String[] texts = { ft.getText() };
		if (ele_seiial_exist == false) {
		    final ARepeat rp = rl.getARepeat(ft.getRepeatID());
		    final int repeat = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
		    log("SeriToXml: read region : FieldText = " + ft.toString() + ", repeat = " + repeat);
		    Ele.setElementN(ft.getPath(), repeat, texts);
		}
	    }
	    for (final FieldImageURL im : fl.getImage_()) {
		xml_FieldImage(im);
		String[] texts = { im.getUrl() };
		if (ele_seiial_exist == false) {
		    final ARepeat rp = rl.getARepeat(im.getRepeatID());
		    final int repeat = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
		    Ele.setElementN(im.getPath(), repeat, texts);
		}
	    }
	    for (final FieldBarcode br : fl.getBarcode_()) {
		xml_FieldBarcode(br);
		String[] texts = { br.getText() };
		if (ele_seiial_exist == false) {
		    final ARepeat rp = rl.getARepeat(br.getRepeatID());
		    final int repeat = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
		    Ele.setElementN(br.getPath(), repeat, texts);
		}
	    }
	}
	WriteXmlLine(2, "</Field_List>");
    }

    private static void log(final String s) {
	System.out.println(s);
    }

    // フィールド要素
    public static void element_tag(final AElement aele, final int level, final PrintWriter elementwtr) {
	System.out.println("SeriToXml: element_tag " + aele.getName() + " (level " + level + "), value = "
		+ aele.getValue() + ", child size = " + aele.getElement_().size());
	final StringBuffer line = new StringBuffer();
	for (int i = 0; i < level; i++) {
	    line.append("\t");
	}
	if (aele.getElement_().size() == 0) {
	    line.append(String.format("<%1$s>%2$s</%1$s>\n", aele.getName(), aele.getValue()));
	    elementwtr.write(line.toString());
	} else {
	    line.append(String.format("<%s>%s\n", aele.getName(), aele.getValue()));
	    elementwtr.write(line.toString());
	    for (AElement childAele : aele.getElement_()) {
		element_tag(childAele, level + 1, elementwtr);
	    }
	    int idx;
	    idx = line.indexOf("<");
	    line.insert(idx + 1, "/");
	    idx = line.lastIndexOf(">");
	    elementwtr.write(line.substring(0, idx + 1) + "\n");
	}
    }

    // 直線
    private void xml_Line(final Line ln) {
	int x = ln.getX();
	int y = ln.getY();
	int width = ln.getWidth();
	int height = ln.getHeight();
	WriteXmlLine(3, String.format("<Line x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\"", Decimal.ToDecimalS2(x),
		Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width), Decimal.ToDecimalS2(height)));
	int pen_width = ln.getPen_width();
	StringBuffer buff = new StringBuffer(String.format(" direction=\"%s\" pen_style=\"%s\" pen_width=\"%s\"",
		ln.getDirection().getName(), ln.getPen_style().getName(), Decimal.ToDecimalS1(pen_width)));
	String repeatID = ln.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, ln.getRepPlace().getName()));
	}
	buff.append("/>");
	WriteXmlLine(3, buff.toString());
    }

    // 矩形
    private void xml_Rect(final Rect rc) {
	int x = rc.getX();
	int y = rc.getY();
	int width = rc.getWidth();
	int height = rc.getHeight();
	WriteXmlLine(3, String.format("<Rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\"", Decimal.ToDecimalS2(x),
		Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width), Decimal.ToDecimalS2(height)));
	int pen_width = rc.getPen_width();
	StringBuffer buff = new StringBuffer(String.format(" pen_style=\"%s\" pen_width=\"%s\"",
		rc.getPen_style().getName(), Decimal.ToDecimalS1(pen_width)));
	String repeatID = rc.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, rc.getRepPlace().getName()));
	}
	buff.append("/>");
	WriteXmlLine(3, buff.toString());
    }

    // 角丸め矩形
    private void xml_RundRect(final RundRect rr) {
	int x = rr.getX();
	int y = rr.getY();
	int width = rr.getWidth();
	int height = rr.getHeight();
	WriteXmlLine(3, String.format("<RundRect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\"", Decimal.ToDecimalS2(x),
		Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width), Decimal.ToDecimalS2(height)));
	int pen_width = rr.getPen_width();
	int radius = rr.getRadius();
	StringBuffer buff = new StringBuffer(String.format(
		" pen_style=\"%s\" pen_width=\"%s\" corner=\"%s\" radius=\"%s\"", rr.getPen_style().getName(),
		Decimal.ToDecimalS1(pen_width), rr.getPsition().getName(), Decimal.ToDecimalS2(radius)));
	String repeatID = rr.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, rr.getRepPlace().getName()));
	}
	buff.append("/>");
	WriteXmlLine(3, buff.toString());
    }

    // 網掛
    private void xml_Net(final Net nt) {
	int x = nt.getX();
	int y = nt.getY();
	int width = nt.getWidth();
	int height = nt.getHeight();
	StringBuffer buff = new StringBuffer(
		String.format("<Net x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" pattern=\"%s\"",
			Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width),
			Decimal.ToDecimalS2(height), nt.getPattern().getName()));
	String repeatID = nt.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, nt.getRepPlace().getName()));
	}
	buff.append("/>");
	WriteXmlLine(3, buff.toString());
    }

    // 固定テキスト
    private void xml_StaticText(final StaticText st) {
	int x = st.getX();
	int y = st.getY();
	int width = st.getWidth();
	int height = st.getHeight();
	int offsetX = st.getOffsetX();
	int offsetY = st.getOffsetY();
	TextPosition.position textAlignment = st.getTextAlignment();
	TextPosition.position verticalAlignment = st.getVerticalAlignment();
	StringBuffer buff = new StringBuffer(
		String.format("<Text x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" offsetX=\"%s\" offsetY=\"%s\"",
			Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width),
			Decimal.ToDecimalS2(height), Decimal.ToDecimalS2(offsetX), Decimal.ToDecimalS2(offsetY)));
	if (textAlignment.equals(TextPosition.position.LEFT) == false) {
	    buff.append(String.format(" textAlignment=\"%s\"", textAlignment.getName()));
	}
	if (verticalAlignment.equals(TextPosition.position.LEFT) == false) {
	    buff.append(String.format(" verticalAlignment=\"%s\"", verticalAlignment.getName()));
	}
	Color color = st.getColor();
	if (color.equals(Color.BLACK) == false) {
	    buff.append(String.format(" Color=\"%s\"", ColorUtil.getName(color)));
	}
	Color bkColor = st.getBkcolor();
	if (bkColor.equals(Color.BLACK) == false) {
	    buff.append(String.format(" bkColor=\"%s\"", ColorUtil.getName(bkColor)));
	}
	if (st.isFullpaint()) {
	    buff.append(" FullPaint=\"true\"");
	}
	buff.append(String.format(" font=\"%s\"", Pro.getFonts().getAfont(st.getFontID()).getID()));
	if (st.isStrike() == true) {
	    buff.append(" Strike=\"true\"");
	}
	if (st.isUnderline() == true) {
	    buff.append(" Underline=\"true\"");
	}
	String repeatID = st.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, st.getRepPlace().getName()));
	}
	buff.append(">");
	WriteXmlLine(3, buff.toString());
	WriteXmlLine(3, String.format(" \"%s\"", st.getText())); // テキスト
	WriteXmlLine(3, "</Text>");
    }

    // 可変テキスト
    private void xml_FieldText(final FieldText ft) {
	int x = ft.getX();
	int y = ft.getY();
	int width = ft.getWidth();
	int height = ft.getHeight();
	int offsetX = ft.getOffsetX();
	int offsetY = ft.getOffsetY();
	TextPosition.position textAlignment = ft.getTextAlignment();
	TextPosition.position verticalAlignment = ft.getVerticalAlignment();
	StringBuffer buff = new StringBuffer(
		String.format("<Text x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" offsetX=\"%s\" offsetY=\"%s\"",
			Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width),
			Decimal.ToDecimalS2(height), Decimal.ToDecimalS2(offsetX), Decimal.ToDecimalS2(offsetY)));
	if (textAlignment.equals(TextPosition.position.LEFT) == false) {
	    buff.append(String.format(" textAlignment=\"%s\"", textAlignment.getName()));
	}
	if (verticalAlignment.equals(TextPosition.position.LEFT) == false) {
	    buff.append(String.format(" verticalAlignment=\"%s\"", verticalAlignment.getName()));
	}
	Color color = ft.getColor(0);
	if (color.equals(Color.BLACK) == false) {
	    buff.append(String.format(" Color=\"%s\"", ColorUtil.getName(color)));
	}
	Color bkColor = ft.getBkcolor(0);
	if (bkColor.equals(Color.BLACK) == false) {
	    buff.append(String.format(" bkColor=\"%s\"", ColorUtil.getName(bkColor)));
	}
	if (ft.isFullpaint(0) == true) {
	    buff.append(" FullPaint=\"true\"");
	}
	buff.append(String.format(" font=\"%s\"", Pro.getFonts().getAfont(ft.getFontID()).getID()));
	if (ft.isStrike(0) == true) {
	    buff.append(" Strike=\"true\"");
	}
	if (ft.isUnderline(0) == true) {
	    buff.append(" Underline=\"true\"");
	}
	String repeatID = ft.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, ft.getRepPlace().getName()));
	}
	WriteXmlLine(3, buff.toString());
	WriteXmlLine(3, String.format(" path=\"%s\">%s", ft.getPath(), ft.getText())); // フィールド
	WriteXmlLine(3, "</Text>");
    }

    // 固定イメージ
    private void xml_StaticImage(final StaticImageURL im) {
	int x = im.getX();
	int y = im.getY();
	int width = im.getWidth();
	int height = im.getHeight();
	int offsetX = im.getOffsetX();
	int offsetY = im.getOffsetY();
	StringBuffer buff = new StringBuffer(
		String.format("<Image_URL x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" offsetX=\"%s\" offsetY=\"%s\"",
			Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width),
			Decimal.ToDecimalS2(height), Decimal.ToDecimalS2(offsetX), Decimal.ToDecimalS2(offsetY)));
	String repeatID = im.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, im.getRepPlace().getName()));
	}
	buff.append(">");
	WriteXmlLine(3, buff.toString());
	WriteXmlLine(3, String.format(" \"%s\"", im.getUrl()));
	WriteXmlLine(3, "</Image_URL>");
    }

    // 可変イメージ
    private void xml_FieldImage(final FieldImageURL im) {
	int x = im.getX();
	int y = im.getY();
	int width = im.getWidth();
	int height = im.getHeight();
	int offsetX = im.getOffsetX();
	int offsetY = im.getOffsetY();
	StringBuffer buff = new StringBuffer(
		String.format("<Image_URL x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" offsetX=\"%s\" offsetY=\"%s\"",
			Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width),
			Decimal.ToDecimalS2(height), Decimal.ToDecimalS2(offsetX), Decimal.ToDecimalS2(offsetY)));
	String repeatID = im.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, im.getRepPlace().getName()));
	}
	WriteXmlLine(3, buff.toString());
	WriteXmlLine(3, String.format(" path=\"%s\">%s", im.getPath(), im.getUrl()));
	WriteXmlLine(3, "</Image_URL>");
    }

    // 固定バーコード
    private void xml_StaticBarcode(final StaticBarcode br) {
	int x = br.getX();
	int y = br.getY();
	int width = br.getWidth();
	int height = br.getHeight();
	int offsetX = br.getOffsetX();
	int offsetY = br.getOffsetY();
	StringBuffer buff = new StringBuffer(String.format(
		"<Barcode x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" offsetX=\"%s\" offsetY=\"%s\" codeKind=\"%s\"",
		Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width), Decimal.ToDecimalS2(height),
		Decimal.ToDecimalS2(offsetX), Decimal.ToDecimalS2(offsetY), br.getCodeKind().getName()));
	String repeatID = br.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, br.getRepPlace().getName()));
	}
	buff.append(">");
	WriteXmlLine(3, buff.toString());
	WriteXmlLine(3, String.format(" \"%s\"", br.getText()));
	WriteXmlLine(3, "</Barcode>");
    }

    // 可変バーコード
    private void xml_FieldBarcode(final FieldBarcode br) {
	int x = br.getX();
	int y = br.getY();
	int width = br.getWidth();
	int height = br.getHeight();
	int offsetX = br.getOffsetX();
	int offsetY = br.getOffsetY();
	StringBuffer buff = new StringBuffer(String.format(
		"<Barcode x=\"%s\" y=\"%s\" width=\"s\" height=\"%s\" offsetX=\"%s\" offsetY=\"%s\" codeKind=\"%s\"",
		Decimal.ToDecimalS2(x), Decimal.ToDecimalS2(y), Decimal.ToDecimalS2(width), Decimal.ToDecimalS2(height),
		Decimal.ToDecimalS2(offsetX), Decimal.ToDecimalS2(offsetY), br.getCodeKind().getName()));
	String repeatID = br.getRepeatID();
	if ((repeatID != null) && (repeatID.length() > 0)) {
	    buff.append(String.format(" repeat=\"%s\" Pos=\"%s\"", repeatID, br.getRepPlace().getName()));
	}
	WriteXmlLine(3, buff.toString());
	WriteXmlLine(3, String.format(" path=\"%s\"\">%s", br.getPath(), br.getText()));
	WriteXmlLine(3, "</Image_URL>");
    }
}
