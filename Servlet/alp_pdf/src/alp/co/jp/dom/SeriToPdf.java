package alp.co.jp.dom;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import alp.co.jp.ARegion;
import alp.co.jp.ARepeat;
import alp.co.jp.Field_List;
import alp.co.jp.Fonts;
import alp.co.jp.Line_List;
import alp.co.jp.Product;
import alp.co.jp.ProductForm;
import alp.co.jp.Repeat_List;
import alp.co.jp.Static_List;
import alp.co.jp.element.AElement;
import alp.co.jp.element.Elements;
import alp.co.jp.line.Direction;
import alp.co.jp.line.Line;
import alp.co.jp.line.Net;
import alp.co.jp.line.NetPattern;
import alp.co.jp.line.PenStyle;
import alp.co.jp.line.Rect;
import alp.co.jp.line.RundCorner.CORNER;
import alp.co.jp.line.RundRect;
import alp.co.jp.sup.Sup;
import alp.co.jp.text.AFont;
import alp.co.jp.text.FieldBarcode;
import alp.co.jp.text.FieldImageURL;
import alp.co.jp.text.FieldText;
import alp.co.jp.text.StaticBarcode;
import alp.co.jp.text.StaticImageURL;
import alp.co.jp.text.StaticText;
import alp.co.jp.util.BarCodeUtil;
import alp.co.jp.util.BarCodeUtil.BARKIND;
import alp.co.jp.util.CriptDefine;
import alp.co.jp.util.FontUtil;
import alp.co.jp.util.FontUtil.FontInf;
import alp.co.jp.util.MetaDefine;
import alp.co.jp.util.RepeatUtil.REP_PLACE;
import alp.co.jp.util.Serialize;
import alp.co.jp.util.StringUtil;
import alp.co.jp.util.TextPosition.position;
import alp.co.jp.util.XmlPdfPath;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BadPdfFormatException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class SeriToPdf {

    private static Log log = LogFactory.getLog(SeriToPdf.class);

    public XmlPdfPath xmlPdfpath;
    private Product _product = null;
    private Rectangle pageSize = null;
    private float _leftmargine, _rightmargine, _topmargine, _bottommargine;
    private String _title;
    private String _pdfFileName;
    private Document _document = null;
    private PdfWriter _writer = null;
    private PdfReader _reader = null;
    private PdfImportedPage page = null;
    private Elements _elements = null;
    private PdfContentByte _cb = null;
    private PdfContentByte _underLayer = null;
    private FontUtil _font_util = null;
    private int _PageCount;
    private CriptDefine _crip = null;
    private MetaDefine _meta = null;
    private final float ITALIC_ANGLE = 0.20f;
    private final ArrayList<Sup> _sup_list = new ArrayList<Sup>();
    private final DubleLinePoint _duble_point = new DubleLinePoint();
    private boolean _doSerializeFileCheck = true;

    enum PDFMAKE_MODE {
	MAKE_PDF, MAKE_TEMPLATE, FROM_TEMPLATE;
    }

    public enum LINE_MODE {
	LINE_ALL, LINE_AFTER, LINE_BEFORE;
    }

    PDFMAKE_MODE make_mode = PDFMAKE_MODE.MAKE_PDF;
    LINE_MODE line_mode = LINE_MODE.LINE_ALL;

    /**
     * @param args
     */
    public SeriToPdf(final String pdfpath) {
	// 各種ファイルのパス取出し準備
	xmlPdfpath = new XmlPdfPath(pdfpath);
    }

    public boolean Init(final String pdfName, final LINE_MODE mode) {
	line_mode = mode;
	return Init(pdfName);
    }

    public boolean Init(final String pdfName, final String pdfFileName, final LINE_MODE mode) {
	line_mode = mode;
	return Init(pdfName, pdfFileName);
    }

    // 領域を初期化してフィールドを戻す
    public AElement InitField() {
	// 領域定義を初期化
	Product pro = (Product) Serialize.Input(xmlPdfpath.getSerialPath());
	if (pro == null) {
	    System.out.println("Serialize.Inputエラー");
	    return null;
	}
	_product.setRegion(pro.getRegion());
	// フィールドの値を初期化
	_elements = null;
	AElement ele = null;
	xmlPdfpath.setFieldxmlname(_product.getElement());
	String serfldpath = xmlPdfpath.getSerialfldPath();
	File element_serial = new File(serfldpath);
	if (element_serial.exists()) {
	    ele = (AElement) Serialize.Input(serfldpath);
	}
	if (ele != null) {
	    _elements = new Elements(ele);
	} else {
	    _elements = new Elements(new AElement(_product.getTitle(), ""));
	    System.out.println("Serialize{" + serfldpath + "}存在せず");
	}
	_sup_list.clear();
	return ele;
    }

    public boolean Init(final String productName) {
	return Init(productName, "");
    }

    public boolean Init(final String productName, final String pdfFileName) {
	xmlPdfpath.setName(productName);

	// シリアライズ入力
	// プロダクト
	_product = new Product();
	_product = (Product) Serialize.Input(xmlPdfpath.getSerialPath());
	if (_product == null) {
	    System.out.println("Serialize.Inputエラー");
	    return false;
	}
	if (_doSerializeFileCheck) {
	    serializedFileCheck(productName);
	}
	// フィールド要素
	AElement ele = null;
	xmlPdfpath.setFieldxmlname(_product.getElement());
	String serfldpath = xmlPdfpath.getSerialfldPath();
	File element_serial = new File(serfldpath);
	if (element_serial.exists()) {
	    ele = (AElement) Serialize.Input(serfldpath);
	}
	if (ele != null) {
	    _elements = new Elements(ele);
	} else {
	    _elements = new Elements(new AElement(_product.getTitle(), ""));
	    System.out.println("Serialize{" + serfldpath + "}存在せず");
	}
	_title = _product.getTitle(); // PDF名称になる
	if (pdfFileName.length() > 0) {
	    _pdfFileName = pdfFileName;
	} else {
	    _pdfFileName = _title; // PDF名称になる
	}
	ProductForm form = _product.getForm();
	int width = form.getPageWidth();
	int height = form.getPageHeight();
	pageSize = new Rectangle(width, height);
	_leftmargine = form.getLeftMargine();
	_rightmargine = form.getRightMargine();
	_topmargine = form.getTopMargine();
	_bottommargine = form.getBottomMargine();
	return true;
    }

    // メタデータを定義
    public void setMetaData(final String title, final String subject, final String keywords, final String creator, final String author) {
	_meta = new MetaDefine(title, subject, keywords, creator, author);
    }

    // 暗号化パスワードを作成
    // ユーザーパスワードその他を格納
    public boolean CripPassword(final String userpassword, final boolean copyable, final boolean printable) {
	if (userpassword.length() <= 0) {
	    return false;
	}
	if (_crip == null) {
	    _crip = new CriptDefine();
	}
	_crip.setUserPassword(userpassword);
	_crip.setCopy_able(copyable);
	_crip.setPrint_able(printable);
	return true;
    }

    // 所有者パスワードを格納
    public boolean CripOwnerPassword(final String ownerpassword) {
	if (ownerpassword.length() <= 0) {
	    return false;
	}
	if (_crip == null) {
	    _crip = new CriptDefine();
	}
	_crip.setOwnerPassword(ownerpassword);
	return true;
    }

    public Product getProduct() {
	return _product;
    }

    public Elements getElements() {
	return _elements;
    }

    public AElement getElement() {
	return _elements.getElement();
    }

    // PDFテンプレート作成
    public void Make_PDF_Template() {
	make_mode = PDFMAKE_MODE.MAKE_TEMPLATE;
	Make_PDF(xmlPdfpath.getTemplateName());
	Make_Page();
    }

    // PDFテンプレート作成
    public void Make_PDF_Template(final String template_name) {
	make_mode = PDFMAKE_MODE.MAKE_TEMPLATE;
	Make_PDF(template_name);
	Make_Page();
    }

    // PDFテンプレートからPDF作成
    public void PDF_From_Template() {
	PDF_From_Template(xmlPdfpath.getTemplateName());
    }

    // PDFテンプレートからPDF作成
    public void PDF_From_Template(final String pdf_Templete) {
	make_mode = PDFMAKE_MODE.FROM_TEMPLATE;
	try {
	    _reader = new PdfReader(xmlPdfpath.getTemplatePath(pdf_Templete));
	    // ドキュメント生成
	    _document = new Document(pageSize, _leftmargine, _rightmargine, _topmargine, _bottommargine);
	    // PdfCopy インスタンスを取得、オープン
	    _writer = PdfWriter.getInstance(_document, new FileOutputStream(xmlPdfpath.getPdfPath(_pdfFileName)));
	    // 暗号化パスワード
	    if (_crip != null) {
		_crip.setEncCription(_writer);
	    }
	    _document.open();
	    // メタデータ格納
	    if (_meta != null) {
		_meta.MataData(_document);
	    }
	    if ((_meta == null) || (_meta.getTitle().length() == 0)) {
		_document.addTitle(_title);
	    }
	    // 1 番目：sample.pdf の1 ページ目をコピーする
	    page = _writer.getImportedPage(_reader, 1);
	    _cb = _writer.getDirectContent();
	    _underLayer = _writer.getDirectContentUnder();
	    _cb.addTemplate(page, 1f, 0f, 0f, 1f, 0, 0);
	    _PageCount = 0;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (BadPdfFormatException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (DocumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    // PDFテンプレートから(ブラウザ出力)PDF作成
    public void PDF_From_Template(final OutputStream pdfresponse) {
	PDF_From_Template(xmlPdfpath.getTemplateName(), pdfresponse);
    }

    // PDFテンプレートから(ブラウザ出力)PDF作成
    public void PDF_From_Template(final String pdf_Templete, final OutputStream pdfresponse) {
	make_mode = PDFMAKE_MODE.FROM_TEMPLATE;
	try {
	    _reader = new PdfReader(xmlPdfpath.getTemplatePath(pdf_Templete));
	    // ドキュメント生成
	    _document = new Document(pageSize, _leftmargine, _rightmargine, _topmargine, _bottommargine);
	    // PdfCopy インスタンスを取得、オープン
	    _writer = PdfWriter.getInstance(_document, pdfresponse);
	    // 暗号化パスワード
	    if (_crip != null) {
		_crip.setEncCription(_writer);
	    }
	    _document.open();
	    // メタデータ格納
	    if (_meta != null) {
		_meta.MataData(_document);
	    }
	    if ((_meta == null) || (_meta.getTitle().length() == 0)) {
		_document.addTitle(_title);
	    }
	    // 1 番目：sample.pdf の1 ページ目をコピーする
	    page = _writer.getImportedPage(_reader, 1);
	    _cb = _writer.getDirectContent();
	    _underLayer = _writer.getDirectContentUnder();
	    _cb.addTemplate(page, 1f, 0f, 0f, 1f, 0, 0);
	    _PageCount = 0;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (BadPdfFormatException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (DocumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    // PDF作成用テンプレートを変更
    public void Change_Template() {
	Change_Template(xmlPdfpath.getTemplateName());
    }

    // PDF作成用テンプレートを変更
    public void Change_Template(final String pdf_Templete) {
	if (make_mode != PDFMAKE_MODE.FROM_TEMPLATE) {
	    return;
	}
	try {
	    _reader = new PdfReader(xmlPdfpath.getTemplatePath(pdf_Templete));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    // PDFファイル作成
    public void Make_PDF() {
	Make_PDF(_pdfFileName);
    }

    // PDFファイル名称を文字列で指定
    public void Make_PDF(final String pdfFileName) {
	// PDF作成
	try {
	    // ドキュメント生成
	    _document = new Document(pageSize, _leftmargine, _rightmargine, _topmargine, _bottommargine);
	    // インスタンスを取得、オープン
	    if (make_mode == PDFMAKE_MODE.MAKE_TEMPLATE) {
		_writer = PdfWriter.getInstance(_document,
			new FileOutputStream(xmlPdfpath.getTemplatePath(pdfFileName)));
	    } else {
		_writer = PdfWriter.getInstance(_document, new FileOutputStream(xmlPdfpath.getPdfPath(pdfFileName)));
	    }
	    // 暗号化パスワード
	    if (_crip != null) {
		_crip.setEncCription(_writer);
	    }
	    _document.open();
	    // メタデータ格納
	    if (_meta != null) {
		_meta.MataData(_document);
	    }
	    if ((_meta == null) || (_meta.getTitle().length() == 0)) {
		_document.addTitle(_title);
	    }
	    _cb = _writer.getDirectContent();
	    _underLayer = _writer.getDirectContentUnder();
	    _PageCount = 0;
	} catch (DocumentException de) {
	    System.err.println(de.getMessage());
	    de.printStackTrace();
	} catch (FileNotFoundException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    // ブラウザ画面へPDF出力時使用
    public void Make_PDF(final OutputStream pdfresponse) {
	// PDF作成
	try {
	    // ドキュメント生成
	    _document = new Document(pageSize, _leftmargine, _rightmargine, _topmargine, _bottommargine);
	    // インスタンスを取得、オープン
	    _writer = PdfWriter.getInstance(_document, pdfresponse);
	    // 暗号化パスワード
	    if (_crip != null) {
		_crip.setEncCription(_writer);
	    }
	    _document.open();
	    // メタデータ格納
	    if (_meta != null) {
		_meta.MataData(_document);
	    }
	    if ((_meta == null) || (_meta.getTitle().length() == 0)) {
		_document.addTitle(_title);
	    }
	    _cb = _writer.getDirectContent();
	    _underLayer = _writer.getDirectContentUnder();
	    _PageCount = 0;
	} catch (DocumentException de) {
	    System.err.println(de.getMessage());
	    de.printStackTrace();
	}
    }

    // PDFページ出力
    public void Make_Page() {
	if (_PageCount > 0) {
	    // 次ページ作成
	    _document.newPage();
	    // テンプレートから作成
	    if (make_mode == PDFMAKE_MODE.FROM_TEMPLATE) {
		_document.newPage();
		page = _writer.getImportedPage(_reader, 1);
		_cb.addTemplate(page, 1f, 0f, 0f, 1f, 0, 0);
	    }
	}
	_PageCount++;
	// 基本フォント定義を取出してBaseFont情報作成
	_font_util = new FontUtil(_product.getBasefonts());
	// フォント定義
	// 通常文字のフォント作成
	_font_util.FontInf(_product.getFonts());
	// 領域定義
	for (final ARegion region : _product.getRegion()) {
	    // String name = region.getName();
	    // System.out.println(name);
	    if (!region.isDisable()) {
		WriteARegion(region);
	    }
	}
	// 付録
	if (make_mode != PDFMAKE_MODE.MAKE_TEMPLATE) {
	    for (final Sup sup : _sup_list) {
		try {
		    sup.Out(_cb);
		} catch (DocumentException e) {
		    // TODO 自動生成された catch ブロック
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO 自動生成された catch ブロック
		    e.printStackTrace();
		}
	    }
	}
    }

    public void Close() {
	if (_document != null) {
	    // ドキュメントクローズ
	    _document.close();
	    _document = null;
	}
    }

    // 領域出力
    private void WriteARegion(final ARegion region) {
	final float reg_X = region.getX() + _leftmargine;
	final float reg_Y = region.getY() + _topmargine;
	final float reg_width = region.getWidth();
	final float reg_height = region.getHeight();
	final DrawingContext dc = new DrawingContext(reg_X, reg_Y, reg_width, reg_height);
	dc.savePenStyle = PenStyle.style.STRAIGHT;
	final Repeat_List repeat_list = region.getRepeat_list();
	final Line_List line_list = region.getLine_list();
	final Static_List static_list = region.getStatic_list();
	final Field_List field_list = region.getField_list();

	// 可変部分のイメージを先に出力
	// テンプレート作成はスルー
	if (make_mode != PDFMAKE_MODE.MAKE_TEMPLATE) {
	    if (field_list != null) {
		// イメージを先に出力し文字と罫線を透過とする
		for (final FieldImageURL image : field_list.getImage_()) {
		    drawFieldImageURL(dc, repeat_list, image);
		}
	    }
	}
	// 網掛けは文字列の出力に先駆けて描画
	if ((make_mode == PDFMAKE_MODE.MAKE_TEMPLATE /* テンプレート作成時 */)
		|| (make_mode == PDFMAKE_MODE.MAKE_PDF /* 直にPDF作成時 */)) {
	    if (line_list != null) {
		// 座標を設定
		_cb.saveState();
		_cb.concatCTM(1f, 0f, 0f, -1f, dc._reg_X, pageSize.getHeight() - dc._reg_Y);
		// 網掛け
		for (final Net net : line_list.getNet_()) {
		    drawNet(dc, repeat_list, net);
		}
		_cb.resetGrayFill();
		// 座標を元に戻す
		_cb.restoreState();
	    }
	}
	// 固定部分の出力
	// テンプレートから作成はスルー
	if (make_mode != PDFMAKE_MODE.FROM_TEMPLATE) {
	    // 固定部分のイメージを先に出力し文字と罫線を透過とする
	    if (static_list != null) {
		for (final StaticImageURL image : static_list.getImage_()) {
		    drawStaticImageURL(dc, repeat_list, image);
		}
	    }
	    // 固定部分の出力
	    if (static_list != null) {
		// 固定文字
		// PdfTemplate取得
		final PdfTemplate tp = _cb.createTemplate(dc._reg_width, dc._reg_height);
		// AsianFontMapperで日本語フォントをマッピングしてGraphic2D取得
		for (final StaticText text : static_list.getText_()) {
		    drawStaticText(dc, repeat_list, tp, text);
		}
		_cb.addTemplate(tp, dc._reg_X, pageSize.getHeight() - (dc._reg_Y + dc._reg_height));
		// バーコード
		for (final StaticBarcode barcode : static_list.getBarcode_()) {
		    drawStaticBarcode(dc, repeat_list, barcode);
		}
	    }
	}
	// 可変部分の出力
	// テンプレート作成はスルー
	if (make_mode != PDFMAKE_MODE.MAKE_TEMPLATE) {
	    if (field_list != null) {
		// PdfTemplate取得
		final PdfTemplate tp = _cb.createTemplate(dc._reg_width, dc._reg_height);
		// AsianFontMapperで日本語フォントをマッピングしてGraphic2D取得
		for (final FieldText text : field_list.getText_()) {
		    drawFieldText(dc, repeat_list, tp, text);
		}
		_cb.addTemplate(tp, dc._reg_X, pageSize.getHeight() - (dc._reg_Y + dc._reg_height));
		// バーコード
		for (final FieldBarcode barcode : field_list.getBarcode_()) {
		    drawBarcode(dc, repeat_list, barcode);
		}
	    }
	}
	// 罫線操作(網掛けを除く)
	if ((((make_mode == PDFMAKE_MODE.MAKE_TEMPLATE) && (line_mode != LINE_MODE.LINE_AFTER)))
		|| (((make_mode == PDFMAKE_MODE.FROM_TEMPLATE) && (line_mode != LINE_MODE.LINE_BEFORE)))
		|| (make_mode == PDFMAKE_MODE.MAKE_PDF)) {
	    if (line_list != null) {
		// 座標を設定
		_cb.saveState();
		_cb.concatCTM(1f, 0f, 0f, -1f, dc._reg_X, pageSize.getHeight() - dc._reg_Y);
		// 直線描画
		for (final Line line : line_list.getLine_()) {
		    drawLine(dc, repeat_list, line);
		}
		// 矩形
		for (final Rect rect : line_list.getRect_()) {
		    drawRect(dc, repeat_list, rect);
		}
		// 角丸め矩形
		for (final RundRect rundrect : line_list.getRundrect_()) {
		    drawRundRect(dc, repeat_list, rundrect);
		}
		// 直線スタイルへ戻す
		dc.savePenStyle = PenStyle.style.STRAIGHT;
		// 座標を元に戻す
		_cb.restoreState();
	    }
	}
    }

    private void drawFieldImageURL(final DrawingContext dc, final Repeat_List repeat_list, final FieldImageURL image) {
	final int X0 = image.getX() + image.getOffsetX();
	final int Y0 = image.getY() + image.getOffsetY();
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(image.getRepeatID(), image.getRepPlace());
	int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	// フィールドデータ取出し
	int itemmax = 0;
	if ((rp != null) && (rp.isParentRepeat())) {
	    if (rp.isVertical()) {
		itemmax = rp.getRepeatY();
	    } else {
		itemmax = rp.getRepeatX();
	    }
	}
	final ArrayList<String> strs = _elements.FindValue(image.getPath(), max, itemmax);
	max = Math.min(max, strs.size());
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    String path = strs.get(idx);
	    if ((path != null) && (path.length() > 0)) {
		final String p = StringUtil.CheckDirectory(path) == true ? path : xmlPdfpath.getFieldimagePath() + path;
		outimage(dc, p, X / 100f, Y / 100f, image.getWidth() / 100f, image.getHeight() / 100f);
	    }
	}
    }

    private void drawNet(final DrawingContext dc, final Repeat_List repeat_list, final Net net) {
	final int X0 = net.getX();
	final int Y0 = net.getY();
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(net.getRepeatID(), net.getRepPlace());
	final int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	_cb.setLineDash(0);
	final float node = NetPattern.getNode(net.getPattern());
	_cb.setGrayFill(node);
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    _cb.rectangle(X / 100f, Y / 100f, net.getWidth() / 100f, net.getHeight() / 100f);
	    _cb.fill();
	}
    }

    private void drawStaticImageURL(final DrawingContext dc, final Repeat_List repeat_list,
	    final StaticImageURL image) {
	final int X0 = image.getX() + image.getOffsetX();
	final int Y0 = image.getY() + image.getOffsetY();
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(image.getRepeatID(), image.getRepPlace());
	int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	final String path = image.getUrl();
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    final String p = StringUtil.CheckDirectory(path) == true ? path : xmlPdfpath.getStaticimagePath() + path;
	    outimage(dc, p, X / 100f, Y / 100f, image.getWidth() / 100f, image.getHeight() / 100f);
	}
    }

    private void drawStaticText(final DrawingContext dc, final Repeat_List repeat_list, final PdfTemplate tp,
	    final StaticText text) {
	final int X0 = text.getX();
	final int Y0 = text.getY();
	final String ch = text.getText();
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(text.getRepeatID(), text.getRepPlace());
	final int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    outtext(dc, tp, X, Y, text.getOffsetX(), text.getOffsetY(), text.getWidth() / 100f, text.getHeight() / 100f,
		    text.getFontID(), ch, text.getTextAlignment(), text.getVerticalAlignment(), text.getColor(),
		    text.getBkcolor(), text.isStrike(), text.isUnderline(), text.isFullpaint());
	}
    }

    private void drawStaticBarcode(final DrawingContext dc, final Repeat_List repeat_list,
	    final StaticBarcode barcode) {
	final int X0 = barcode.getX() + barcode.getOffsetX();
	final int Y0 = barcode.getY() + barcode.getOffsetY();
	final float width = barcode.getWidth() / 100f;
	final float height = barcode.getHeight() / 100f;
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(barcode.getRepeatID(), barcode.getRepPlace());
	final int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    out_barcode(dc, barcode.getCodeKind(), barcode.getText(), X / 100f, Y / 100f, width, height);
	}
    }

    private void drawFieldText(final DrawingContext dc, final Repeat_List repeat_list, final PdfTemplate tp,
	    final FieldText text) {
	boolean conc = false; // 要素繰返しを実行
	float width = text.getWidth() / 100f;
	float height = text.getHeight() / 100f;
	REP_PLACE RE = text.getRepPlace();
	Repeat_List.RepeatEx rp = repeat_list.RepeatEx(text.getRepeatID(), text.getRepPlace());
	int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	// フィールドデータ取出し
	int itemmax = 0;
	if ((rp != null) && (rp.isParentRepeat()) && (RE != REP_PLACE.HEDER) && (RE != REP_PLACE.FOOTER)) {
	    if (rp.isVertical()) {
		itemmax = rp.getRepeatY();
	    } else {
		itemmax = rp.getRepeatX();
	    }
	}
	ArrayList<String> strs = _elements.FindValue(text.getPath(), max, itemmax);
	max = Math.min(max, strs.size());
	if ((RE == REP_PLACE.FACTER) && (rp != null)) {
	    // 要素数による繰返し
	    final int intervalX = rp.getIntervalX();
	    final int intervalY = rp.getIntervalY();
	    float penwidth = rp.getPen_width() / 10f;
	    conc = true;
	    int loopmax = rp.isVertical() ? rp.getRepeatY() : rp.getRepeatX();
	    String t = "";
	    int counter, pp;
	    int X = text.getX();
	    int Y = text.getY();
	    for (int idx = counter = pp = 0; idx <= loopmax; idx++) {
		counter++;
		String txt = "";
		if (idx < max) {
		    txt = strs.get(idx);
		    if (idx == 0) {
			t = txt;
			continue;
		    } else if (txt.equals(t) == true) {
			continue;
		    } else {
			pp = idx;
		    }
		}
		counter--;
		if (!rp.isVertical()) {
		    width = (text.getWidth() * counter) / 100f;
		} else {
		    height = (text.getHeight() * counter) / 100f;
		}
		if (idx <= max) {
		    outtext(dc, tp, X, Y, text.getOffsetX(), text.getOffsetY(), width, height, text.getFontID(), t,
			    text.getTextAlignment(), text.getVerticalAlignment(), text.getColor(pp),
			    text.getBkcolor(pp), text.isStrike(pp), text.isUnderline(pp), text.isFullpaint(pp));
		}
		if (!rp.isVertical()) { // 横並び
		    X += (intervalX * counter);
		    // 中間仕切り縦罫線の印字
		    if ((idx < loopmax) && (penwidth > 0f)) {
			// 座標を設定
			_cb.saveState();
			_cb.concatCTM(1f, 0f, 0f, -1f, dc._reg_X, pageSize.getHeight() - dc._reg_Y);
			SetPenStyle(rp.getPenstyle(), PenStyle.style.STRAIGHT, penwidth);
			_cb.moveTo(X / 100f, Y / 100f);
			_cb.lineTo(X / 100f, Y / 100f + height);
			_cb.stroke();
			if (rp.getPenstyle() == PenStyle.style.DOUBLE) {
			    float x_ = X / 100f + (penwidth * 3) / 4;
			    _cb.moveTo(x_, Y / 100f);
			    _cb.lineTo(x_, Y / 100f + height);
			    _cb.stroke();
			}
			// 直線スタイルへ戻す
			dc.savePenStyle = PenStyle.style.STRAIGHT;
			// 座標を元に戻す
			_cb.restoreState();
		    }
		} else { // 縦並び
		    Y += (intervalY * counter);
		    // 中間仕切り横罫線の印字
		    if ((idx < loopmax) && (penwidth > 0f)) {
			// 座標を設定
			_cb.saveState();
			_cb.concatCTM(1f, 0f, 0f, -1f, dc._reg_X, pageSize.getHeight() - dc._reg_Y);
			SetPenStyle(rp.getPenstyle(), PenStyle.style.STRAIGHT, penwidth);
			_cb.moveTo(X / 100f, Y / 100f);
			_cb.lineTo(X / 100f + width, Y / 100f);
			_cb.stroke();
			if (rp.getPenstyle() == PenStyle.style.DOUBLE) {
			    float y_ = Y / 100f + (penwidth * 3) / 4;
			    _cb.moveTo(X / 100f, y_);
			    _cb.lineTo(X / 100f + width, y_);
			    _cb.stroke();
			}
			// 直線スタイルへ戻す
			dc.savePenStyle = PenStyle.style.STRAIGHT;
			// 座標を元に戻す
			_cb.restoreState();
		    }
		}
		counter = 1;
		t = txt;
	    }
	}
	if (conc == false) {
	    final int X0 = text.getX();
	    final int Y0 = text.getY();
	    for (int idx = 0; idx < max; idx++) {
		int X = X0;
		int Y = Y0;
		if (rp != null) {
		    X += rp.calcuAddX(idx);
		    Y += rp.calcuAddY(idx);
		}
		String chline = strs.get(idx);
		if ((chline != null) && (chline.length() > 0)) {
		    outtext(dc, tp, X, Y, text.getOffsetX(), text.getOffsetY(), width, height, text.getFontID(), chline,
			    text.getTextAlignment(), text.getVerticalAlignment(), text.getColor(idx),
			    text.getBkcolor(idx), text.isStrike(idx), text.isUnderline(idx), text.isFullpaint(idx));
		}
	    }
	}
    }

    private void drawBarcode(final DrawingContext dc, final Repeat_List repeat_list, final FieldBarcode barcode) {
	final int X0 = barcode.getX() + barcode.getOffsetX();
	final int Y0 = barcode.getY() + barcode.getOffsetY();
	final float width = barcode.getWidth() / 100f;
	final float height = barcode.getHeight() / 100f;
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(barcode.getRepeatID(), barcode.getRepPlace());
	int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	// フィールドデータ取出し
	int itemmax = 0;
	if ((rp != null) && (rp.isParentRepeat())) {
	    if (rp.isVertical()) {
		itemmax = rp.getRepeatY();
	    } else {
		itemmax = rp.getRepeatX();
	    }
	}
	ArrayList<String> strs = _elements.FindValue(barcode.getPath(), max, itemmax);
	max = Math.min(max, strs.size());
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    String barline = strs.get(idx++);
	    if ((barline != null) && (barline.length() > 0)) {
		out_barcode(dc, barcode.getCodeKind(), barline, X / 100f, Y / 100f, width, height);
	    }
	}
    }

    private void drawLine(final DrawingContext dc, final Repeat_List repeat_list, final Line line) {
	final int X0 = line.getX();
	final int Y0 = line.getY();
	final float width = line.getWidth() / 100f;
	final float height = line.getHeight() / 100f;
	final float penwidth = line.getPen_width() / 10f;
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(line.getRepeatID(), line.getRepPlace());
	final int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	dc.savePenStyle = SetPenStyle(line.getPen_style(), dc.savePenStyle, penwidth);
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    float y = Y / 100f;
	    float y2 = y;
	    float x = X / 100f;
	    float x2 = x;
	    if (line.getDirection() == Direction.direct.U_H) { // 上・横
		x2 += width;
	    } else if (line.getDirection() == Direction.direct.L_V) { // 左・縦
		y2 += height;
	    } else if (line.getDirection() == Direction.direct.LH_SL) { // 左上から右下
		x2 += width;
		y2 += height;
	    } else if (line.getDirection() == Direction.direct.RH_SL) { // 左下から右上
		x2 += width;
		y += height;
	    } else if (line.getDirection() == Direction.direct.B_H) { // 下・横
		x2 += width;
		y += height;
		y2 = y;
	    } else if (line.getDirection() == Direction.direct.R_V) { // 右・縦
		x += width;
		x2 = x;
		y2 = y + height;
	    }
	    _cb.moveTo(x, y);
	    _cb.lineTo(x2, y2);
	    _cb.stroke();
	    if (line.getPen_style() == PenStyle.style.DOUBLE) {
		if (line.getDirection() == Direction.direct.U_H) { // 上・横
		    y += ((penwidth / 4) * 3);
		    y2 = y;
		} else if (line.getDirection() == Direction.direct.L_V) { // 左・縦
		    x += ((penwidth / 4) * 3);
		    x2 = x;
		} else if (line.getDirection() == Direction.direct.B_H) { // 下・横
		    y += ((penwidth / 4) * 3);
		    y2 = y;
		} else if (line.getDirection() == Direction.direct.R_V) { // 右・縦
		    x += ((penwidth / 4) * 3);
		    x2 = x;
		} else {
		    _duble_point.CalucDoblePosition((penwidth * 3) / 4, width, height);
		    final double x_plus = _duble_point.getX_plus();
		    final double y_plus = _duble_point.getY_plus();
		    if (line.getDirection() == Direction.direct.LH_SL) { // 左上から右下
			x -= x_plus;
			x2 -= x_plus;
			y += y_plus;
			y2 += y_plus;
		    } else if (line.getDirection() == Direction.direct.RH_SL) { // 左下から右上
			x += x_plus;
			x2 += x_plus;
			y += y_plus;
			y2 += y_plus;
		    }
		}
		_cb.moveTo(x, y);
		_cb.lineTo(x2, y2);
		_cb.stroke();
	    }
	}
    }

    private void drawRect(final DrawingContext dc, final Repeat_List repeat_list, final Rect rect) {
	final int X0 = rect.getX();
	final int Y0 = rect.getY();
	final float width = rect.getWidth() / 100f;
	final float height = rect.getHeight() / 100f;
	final float penwidth = rect.getPen_width() / 10f;
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(rect.getRepeatID(), rect.getRepPlace());
	final int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	dc.savePenStyle = SetPenStyle(rect.getPen_style(), dc.savePenStyle, penwidth);
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    float y = Y / 100f;
	    float x = X / 100f;
	    float x2 = x + width;
	    float y2 = y + height;
	    _cb.moveTo(x, y);
	    _cb.lineTo(x2, y);
	    _cb.lineTo(x2, y2);
	    _cb.lineTo(x, y2);
	    _cb.closePathStroke();
	    if (rect.getPen_style() == PenStyle.style.DOUBLE) {
		final float in = (penwidth * 3) / 4;
		x += in;
		x2 -= in;
		y += in;
		y2 -= in;
		_cb.moveTo(x, y);
		_cb.lineTo(x2, y);
		_cb.lineTo(x2, y2);
		_cb.lineTo(x, y2);
		_cb.closePathStroke();
	    }
	}
    }

    private void drawRundRect(final DrawingContext dc, final Repeat_List repeat_list, final RundRect rundrect) {
	final int X0 = rundrect.getX();
	final int Y0 = rundrect.getY();
	final float width = rundrect.getWidth() / 100f;
	final float height = rundrect.getHeight() / 100f;
	final float penwidth = rundrect.getPen_width() / 10f;
	final Repeat_List.RepeatEx rp = repeat_list.RepeatEx(rundrect.getRepeatID(), rundrect.getRepPlace());
	final int max = (rp != null) ? rp.getRepeatX() * rp.getRepeatY() : 1;
	dc.savePenStyle = SetPenStyle(rundrect.getPen_style(), dc.savePenStyle, penwidth);
	for (int idx = 0; idx < max; idx++) {
	    int X = X0;
	    int Y = Y0;
	    if (rp != null) {
		X += rp.calcuAddX(idx);
		Y += rp.calcuAddY(idx);
	    }
	    out_rundrect(rundrect.getPsition(), X / 100f, Y / 100f, width, height, rundrect.getRadius() / 100f);
	    if (rundrect.getPen_style() == PenStyle.style.DOUBLE) {
		float in = (penwidth * 3) / 4;
		out_rundrect(rundrect.getPsition(), X / 100f + in, Y / 100f + in, width - in * 2, height - in * 2,
			rundrect.getRadius() / 100f);
	    }
	}
    }

    // 罫線線種設定
    private PenStyle.style SetPenStyle(final PenStyle.style Type, final PenStyle.style savePenStyle,
	    final float penwidth) {
	float onechain[] = { 3f, 1f, 1f, 1f };
	float twochain[] = { 3f, 1f, 1f, 1f, 1f, 1f };
	PenStyle.style type = PenStyle.style.STRAIGHT;
	if (Type == PenStyle.style.DOUBLE) {
	    _cb.setLineWidth(penwidth / 4);
	} else {
	    type = Type;
	    _cb.setLineWidth(penwidth);
	}
	if (type != savePenStyle) { // 変化なしはなにもせず。
	    if (savePenStyle == PenStyle.style.STRAIGHT) { // 元が直線は保存
		_cb.saveState();
	    }
	    if (type == PenStyle.style.STRAIGHT) { // 直線は保存を元に戻す
		_cb.restoreState();
	    } else {
		// 線幅による倍数を算出
		int multi = (int) ((penwidth + 0.25) * 100) / 50;
		if (multi == 0) {
		    multi = 1;
		}
		if (savePenStyle == PenStyle.style.DOT) {
		    _cb.setLineCap(PdfContentByte.LINE_CAP_BUTT);
		}
		if (type == PenStyle.style.DASH) {
		    _cb.setLineDash(3 * multi, 1.5f * multi, 0);
		} else if (type == PenStyle.style.DOT) {
		    _cb.setLineCap(PdfContentByte.LINE_CAP_ROUND);
		    _cb.setLineDash(0.5f * multi, 1 * multi, 0);
		} else if (type == PenStyle.style.DASHDOT) {
		    int l = onechain.length;
		    float[] pattern = new float[l];
		    for (int i = 0; i < l; i++) {
			pattern[i] = onechain[i] * multi;
		    }
		    _cb.setLineDash(pattern, 0);
		} else if (type == PenStyle.style.DASHDOTDOT) {
		    int l = twochain.length;
		    float[] pattern = new float[l];
		    for (int i = 0; i < l; i++) {
			pattern[i] = twochain[i] * multi;
		    }
		    _cb.setLineDash(pattern, 0);
		} else if (type == PenStyle.style.LDASH) {
		    _cb.setLineDash(5 * multi, 2 * multi, 0);
		}
	    }
	}
	return type;
    }

    // 角丸め矩形描画
    private void out_rundrect(final CORNER kind, final float x, final float y, final float width, final float height,
	    final float radius) {
	boolean u_l, u_r, b_r, b_l;
	u_l = u_r = b_r = b_l = false;
	switch (kind) {
	case ALL:
	    _cb.roundRectangle(x, y, width, height, radius);
	    _cb.closePathStroke();
	    return;
	case UP:
	    u_l = u_r = true;
	    break;
	case BM:
	    b_r = b_l = true;
	    break;
	case LF:
	    u_l = b_l = true;
	    break;
	case RI:
	    u_r = b_r = true;
	    break;
	case LU:
	    u_l = true;
	    break;
	case RU:
	    u_r = true;
	    break;
	case LB:
	    b_l = true;
	    break;
	case RB:
	    b_r = true;
	    break;
	case D_LU:
	    u_r = b_r = b_l = true;
	    break;
	case D_RU:
	    u_l = b_r = b_l = true;
	    break;
	case D_LB:
	    u_l = u_r = b_r = true;
	    break;
	case D_RB:
	    u_l = u_r = b_l = true;
	    break;
	case ULBR:
	    u_l = b_r = true;
	    break;
	case URBL:
	    u_r = b_l = true;
	}
	float X = x;
	float Y = y;
	float Xr = x + width;
	float Yb = y + height;
	if (u_l == true) {
	    _cb.moveTo(X, Y + radius);
	    _cb.curveFromTo(X, Y, X + radius, Y);
	} else {
	    _cb.moveTo(X, Y);
	}
	if (u_r == true) {
	    _cb.lineTo(Xr - radius, Y);
	    _cb.curveFromTo(Xr, Y, Xr, Y + radius);
	} else {
	    _cb.lineTo(Xr, Y);
	}
	if (b_r == true) {
	    _cb.lineTo(Xr, Yb - radius);
	    _cb.curveFromTo(Xr, Yb, Xr - radius, Yb);
	} else {
	    _cb.lineTo(Xr, Yb);
	}
	if (b_l == true) {
	    _cb.lineTo(X + radius, Yb);
	    _cb.curveFromTo(X, Yb, X, Yb - radius);
	} else {
	    _cb.lineTo(X, Yb);
	}
	_cb.closePathStroke();
    }

    // 斜め２重線のずれ算出
    class DubleLinePoint {
	// ２重線の次線描画位置ずれ算出
	private double x_plus;
	private double y_plus;

	public double getX_plus() {
	    return x_plus;
	}

	public double getY_plus() {
	    return y_plus;
	}

	public void CalucDoblePosition(final double plus_Width, final double width, final double height) {
	    double slant = 0.0d;
	    if (width < 0.1) {
		x_plus = 0.0;
		if (height > 0.1) {
		    y_plus = plus_Width;
		}
		return;
	    } else if (height < 0.1) {
		y_plus = 0.0;
		if (width > 0.1) {
		    x_plus = plus_Width;
		}
		return;
	    }
	    slant = Math.sqrt(width * width + height * height);
	    x_plus = (plus_Width * height) / slant;
	    y_plus = (plus_Width * width) / slant;
	}
    }

    // バーコード描画
    private void out_barcode(final DrawingContext dc, final BARKIND kind, final String bartext, final float x,
	    final float y, final float width, final float height) {
	final Image bar = BarCodeUtil.getBarCode(kind, bartext, _cb);
	final float pwidth = bar.getPlainWidth();
	final float pheight = bar.getPlainHeight();
	final float scale = width / pwidth < height / pheight ? width / pwidth : height / pheight;
	bar.scalePercent(scale * 100.0f);
	bar.setAbsolutePosition(dc._reg_X + x, (pageSize.getHeight() - dc._reg_Y) - (y + height));
	try {
	    _cb.addImage(bar);
	} catch (DocumentException e) {
	    // TODO 自動生成された catch ブロック
	    e.printStackTrace();
	}
    }

    // イメージ描画
    private void outimage(final DrawingContext dc, final String path, final float x, final float y, final float width,
	    final float height) {
	try {
	    final Image img = Image.getInstance(path);
	    // float pwidth = imgx.getPlainWidth();
	    // float pheight = imgx.getPlainHeight();
	    // float scale = width / pwidth < height / pheight ? width / pwidth
	    // : height / pheight;
	    // imgx.scalePercent(scale * 100.0f); //幅と高さを保持して縮小拡大
	    img.scaleAbsolute(width, height); // 指定の幅と高さで縮小拡大
	    img.setAbsolutePosition(dc._reg_X + x, (pageSize.getHeight() - dc._reg_Y) - (y + height));
	    _underLayer = _writer.getDirectContentUnder();
	    _underLayer.addImage(img);
	} catch (BadElementException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (DocumentException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    // テキスト出力
    private void outtext(final DrawingContext dc, final PdfTemplate tp, final int x, final int y, final int offsetx,
	    final int offsety, final float width, final float height, final int fid, final String ch,
	    final position textpos, final position vertpos, final Color color, final Color bkcolor,
	    final boolean strike, final boolean underline, final boolean fullpaint) {
	// 文字数取出し
	int strlength = ch.length();
	if (strlength == 0) { // 空文字は戻る
	    if ((fullpaint == true) && (bkcolor.equals(Color.BLACK) != true)) { // 背景色塗潰し。追加
		_cb.saveState();
		_cb.setColorFill(bkcolor);
		_cb.rectangle(dc._reg_X + x / 100f, pageSize.getHeight() - dc._reg_Y - y / 100f - height, width,
			height);
		_cb.fill();
		_cb.setColorFill(Color.BLACK);
		_cb.restoreState();
	    }
	    return;
	}
	// フォント情報取出し
	final FontInf finf = _font_util.getFontInf(fid);
	// フォントのスタイルとサイズ指定
	final float sz = finf.getPoint();
	final float lineheight = finf.getLineheight();
	final float linespace = finf.getLinespace();
	final int style = finf.getStyle();
	final Float fwidth = finf.getWidth();
	final boolean bold = (style & java.awt.Font.BOLD) != 0 ? true : false;
	final boolean italic = (style & java.awt.Font.ITALIC) != 0 ? true : false;
	final boolean vertical = finf.isVertical();
	// 外字フォント情報取出し
	final BaseFont eudcfont = finf.getEudcBaseFont();
	// ベースフォント
	final BaseFont basefont = finf.getBaseFont();
	// 文字配置取出し
	final float[] width_lst = new float[strlength];
	final float[] xpos_lst = new float[strlength + 1];
	final float[] ypos_lst = new float[strlength + 1];
	final float[] mult_lst = new float[strlength];
	final ArrayList<String> str_list = new ArrayList<String>();
	float chwidth = 0;
	float chheight = 0;
	int idx = 0;
	float X;
	float Y;
	float back_width = 0, back_height = sz;
	// 横倍角情報取出し
	float width_M = TextAttribute.WIDTH_REGULAR.floatValue();
	if (fwidth.floatValue() != width_M) {
	    // if (fwidth.equals(TextAttribute.WIDTH_REGULAR) == false) {
	    // 細(0.75)が判定できなかった
	    if (fwidth.floatValue() != 0.0f) {
		width_M = fwidth.floatValue();
	    }
	}
	if (vertical == false) { // 横書き
	    // 開始位置取出し
	    X = (x + offsetx) / 100f; // 指定Ｘ座標は左。
	    Y = (y + offsety) / 100f + (sz / 5f) * 4f; // 指定Ｙ座標は下。補正。
	    xpos_lst[0] = ypos_lst[0] = 0;
	    // 文字配置取出し
	    if (textpos == position.LEFT) { // 左詰
		// HTML風禁則処理、idxに行数が戻る
		idx = StringUtil.StringConfine(ch, basefont, str_list, width, width_M, sz, lineheight, width_lst,
			xpos_lst, ypos_lst);
	    } else {
		// カウント用に文字列作成(外字がnull置換されて飛ばされるのを回避)
		final StringBuffer chbuffer = createChBuffer(ch, strlength);
		int i;
		for (i = 0; i < strlength; i++) {
		    chwidth = basefont.getWidthPoint(chbuffer.substring(0, i + 1), sz) * width_M;
		    basefont.getWidthPoint(ch, sz);
		    if (chwidth > width) { // 文字列オーバー
			if (textpos == position.CENTER) {
			    chwidth = back_width;
			    strlength = i > 0 ? i : 1; // 切捨て
			    break;
			}
		    }
		    back_width = chwidth;
		}
		str_list.add(ch.substring(0, strlength));
		if (textpos == position.RIGHT) { // 右詰
		    X = X + (width - chwidth);
		    width_lst[idx] = chwidth;
		} else if (textpos == position.CENTER) { // 中央
		    X = X + (width - chwidth) / 2f;
		    width_lst[idx] = chwidth;
		} else if ((textpos == position.FIT) && (strlength > 1)) { // 適合(旧版対応)
		    width_lst[idx] = chwidth;
		    mult_lst[idx] = (width - chwidth) / i;
		} else if ((textpos == position.AVE) && (strlength > 1)) { // 均等割り
		    String str = new String(chbuffer);
		    chwidth = basefont.getWidthPoint(str, sz) * width_M + (offsetx * 2) / 100f;
		    mult_lst[idx] = ((width - chwidth) / (strlength - 1)) / width_M;
		}
		idx++;
	    }
	    if (vertpos == position.BOTTOM) { // 下詰
		Y = Y + (height - (chheight + lineheight));
	    } else if (vertpos == position.CENTER) { // 中央
		Y = Y + (height - (chheight + lineheight)) / 2f;
	    }
	} else { // 縦書き
	    Y = (y + offsety) / 100f; // 指定Ｙ座標は上。
	    X = (x + offsetx) / 100f; // 指定Ｘ座標は(半角)右？。
	    final float m_height = sz; // 代替で指定フォントポイントとする。
	    chwidth = width_lst[idx] = basefont.getWidthPoint(ch.substring(0, 1), sz);
	    xpos_lst[0] = (chwidth / 2) * width_M;
	    ypos_lst[0] = 0;
	    int i;
	    for (i = 0; i < strlength; i++) {
		back_height = chheight;
		chheight += m_height;
		if (chheight > height) { // 文字列オーバー
		    if (vertpos != position.FIT) {
			chheight = back_height;
			strlength = i > 0 ? i : 1; // 切捨て
			break;
		    }
		}
	    }
	    if (i > 1) {
		chheight += ((i - 1) * linespace);
	    }
	    str_list.add(ch.substring(0, strlength));
	    if (vertpos == position.BOTTOM) { // 下詰
		ypos_lst[0] = height - chheight;
		mult_lst[idx] = 0;
	    } else if (vertpos == position.CENTER) { // 中央
		ypos_lst[0] = (height - chheight) / 2f;
		mult_lst[idx] = 0;
	    } else if (((vertpos == position.FIT) /* 適合(旧版対応) */ || (textpos == position.AVE)) && (strlength > 1)) { // 均等割り
		width_lst[idx] = chheight;
		// mult_lst[idx] = height / chheight;
		mult_lst[idx] = ((height - m_height) / (strlength - 1) - m_height) * -1; // 2014/9/2
	    }
	    if (textpos == position.RIGHT) { // 右詰
		X = X + (width - chwidth);
	    } else if (textpos == position.CENTER) { // 中央
		X = X + (width - chwidth) / 2f;
	    }
	    idx++;
	}
	// 文字出力
	_cb.saveState();
	if ((fullpaint == true) && (bkcolor.equals(Color.BLACK) != true)) { // 背景色塗潰し。追加
	    _cb.setColorFill(bkcolor);
	    _cb.rectangle(dc._reg_X + x / 100f, pageSize.getHeight() - dc._reg_Y - y / 100f - height, width, height);
	    _cb.fill();
	    _cb.setColorFill(Color.BLACK);
	}
	for (int i = 0; i < idx; i++) {
	    // 背景色
	    if ((fullpaint == false) && (bkcolor.equals(Color.BLACK) != true)) {
		_cb.setColorFill(bkcolor);
		if (vertical == false) { // 横書き
		    _cb.rectangle(dc._reg_X + X + xpos_lst[i],
			    pageSize.getHeight() - dc._reg_Y - Y - (ypos_lst[i] + (sz / 10f)), width_lst[i],
			    back_height);
		} else {
		    // 縦書き
		    _cb.rectangle(dc._reg_X + X, pageSize.getHeight() - dc._reg_Y - Y - (ypos_lst[i] + chheight),
			    width_lst[i], chheight);
		}
		_cb.fill();
		_cb.setColorFill(Color.BLACK);
	    }
	    // 文字色
	    if (color.equals(Color.BLACK) != true) {
		_cb.setColorStroke(color);
		_cb.setColorFill(color);
	    }
	    String line = str_list.get(i);
	    int length = line.length();
	    boolean eudc = false;
	    // 文字出力開始
	    _cb.beginText();
	    // 開始位置
	    float X0 = dc._reg_X + X + xpos_lst[i];
	    float Y0 = pageSize.getHeight() - dc._reg_Y - Y - ypos_lst[i];
	    // 禁則処理・均等割りの文字間隔をセット
	    if (mult_lst[i] != 0f) {
		_cb.setCharacterSpacing(mult_lst[i]);
	    }
	    // 斜体
	    if (italic) {
		_cb.setTextMatrix(1, 0, ITALIC_ANGLE, 1, 0, 0); // 斜体設定
		// 座標の補正
		X0 = X0 - Y0 * ITALIC_ANGLE;
	    }
	    // 太字
	    if (bold) {
		float l_w = 0.2f;
		if ((sz / 20f) >= 0.3f) { // 線の太さを定義
		    l_w = sz / 20f;
		}
		_cb.setLineWidth(l_w);
		if (color.equals(Color.BLACK) != true) {
		    _cb.setColorStroke(color);
		    _cb.setColorFill(color);
		}
		_cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
	    }
	    // 幅倍角
	    if (fwidth.equals(TextAttribute.WIDTH_REGULAR) == false) {
		_cb.setHorizontalScaling(fwidth * 100f);
		width_M = fwidth.byteValue();
	    } else {
		width_M = 1.0f;
	    }
	    // ベースフォント設定
	    _cb.setFontAndSize(basefont, sz);
	    // 開始位置セット
	    _cb.moveText(X0, Y0);
	    // 文字出力
	    if ((vertical == true) && (linespace >= 0.01f)) {
		for (int l = 0; l < length; l++) {
		    // 外字はベースフォントを切り替える
		    char c = line.charAt(l);
		    if (isGaiji(c)) { // E000～F9FF
			if (eudc == false) {
			    _cb.setFontAndSize(eudcfont, sz);
			}
			eudc = true;
		    } else {
			if (eudc == true) {
			    _cb.setFontAndSize(basefont, sz);
			}
			eudc = false;
		    }
		    _cb.showText(line.substring(l, l + 1));
		    _cb.moveText(0, -lineheight);
		}
		if (eudc == true) {
		    _cb.setFontAndSize(basefont, sz);
		}
	    } else {
		int top = 0;
		for (int l = top = 0; l < length; l++) {
		    // 外字はベースフォントを切り替える
		    char c = line.charAt(l);
		    if (isGaiji(c)) { // E000～F9FF
			if ((eudc == false) && (top < l)) {
			    _cb.showText(line.substring(top, l));
			    top = l;
			}
			eudc = true;
		    } else {
			if ((eudc == true) && (top < l)) {
			    _cb.setFontAndSize(eudcfont, sz);
			    _cb.showText(line.substring(top, l));
			    _cb.setFontAndSize(basefont, sz);
			    top = l;
			}
			eudc = false;
		    }
		}
		if (top < length) {
		    if (eudc == false) {
			_cb.showText(line.substring(top));
		    } else {
			_cb.setFontAndSize(eudcfont, sz);
			_cb.showText(line.substring(top));
			_cb.setFontAndSize(basefont, sz);
		    }
		}
	    }
	    if (italic) {
		_cb.setTextMatrix(1, 0, 0, 1, 0, 0); // 斜体を戻す
	    }
	    if (bold) {
		_cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL); // 太字を戻す
	    }
	    if (fwidth.equals(TextAttribute.WIDTH_REGULAR) == false) {
		_cb.setHorizontalScaling(100f); // 幅倍角を戻す
	    }
	    // 文字出力終了
	    _cb.endText();
	    // 抹消線・下線処理
	    if ((strike) || (underline)) {
		float l_w = 0.3f;
		if ((sz / 12f) >= 0.3f) { // 線の太さを定義
		    l_w = sz / 12f;
		}
		_cb.setLineWidth(l_w);
		final StringBuffer chbuffer = createChBuffer(ch, strlength);
		for (int l = 0; l < length; l++) {
		    final char c = chbuffer.charAt(l);
		    final float t_x, t_y;
		    if (vertical == false) { // 横書き
			t_x = dc._reg_X + X + xpos_lst[i]
				+ basefont.getWidthPoint(chbuffer.substring(0, l), sz) * width_M;
			t_y = (pageSize.getHeight() - dc._reg_Y - ypos_lst[i]);
		    } else { // 縦書き
			t_x = dc._reg_X + X;
			t_y = pageSize.getHeight() - dc._reg_Y
				- (ypos_lst[i] + chheight - (sz / 8f) - sz * width_M * l);
		    }
		    final float chw = basefont.getWidthPoint(c, sz) * width_M;
		    if (strike) {
			_cb.moveTo(t_x, t_y - (Y - (sz / 3f) * 1f));
			_cb.lineTo(t_x + chw, t_y - (Y - (sz / 3f) * 1f));
			_cb.stroke();

		    } else if (underline) {
			_cb.moveTo(t_x, t_y - (Y + (sz / 8f) * 1f));
			_cb.lineTo(t_x + chw, t_y - (Y + (sz / 8f) * 1f));
			_cb.stroke();
		    }
		}
	    }
	}
	_cb.restoreState();
    }

    private static StringBuffer createChBuffer(final String ch, final int strlength) {
	final StringBuffer chbuffer = new StringBuffer();
	// カウント用に文字列作成
	for (int l = 0; l < strlength; l++) {
	    char c = ch.charAt(l);
	    if (isGaiji(c)) { // E000～F9FF外字
		chbuffer.append('外');
	    } else {
		chbuffer.append(c);
	    }
	}
	return chbuffer;
    }

    private static boolean isGaiji(final char c) {
	return '\uE000' <= c && c <= '\uF9FF';
    }

    // 可変フィールドを取出す
    public FieldText getFieldText(final String RegionName, final int No) {
	for (final ARegion reg : _product.getRegion()) {
	    if (reg.getName().equals(RegionName)) {
		final ArrayList<FieldText> texts = reg.getField_list().getText_();
		return texts.get(No - 1);
	    }
	}
	return null;
    }

    // 可変フィールドを取出す
    public FieldText getFieldText(final String RegionName, final String fieldpath) {
	String searchRegName = null;
	for (final ARegion reg : _product.getRegion()) {
	    searchRegName = reg.getName();
	    if (reg.getName().equals(RegionName)) {
		for (final FieldText fld : reg.getField_list().getText_()) {
		    if (fld.getPath().equals(fieldpath)) {
			return fld;
		    }
		}
	    }
	}
	if (null == RegionName && _product.getRegion().size() == 1 && null != searchRegName) {
	    return getFieldText(searchRegName, fieldpath);
	}
	return null;
    }

    // 付録登録
    public boolean Add_Sup(final Sup sup) {
	sup.SetInit(pageSize.getHeight(), _leftmargine, _topmargine);
	return _sup_list.add(sup);
    }

    // 繰返しを取出す
    public ARepeat get_ARepeat(final String repeatID, final String regionID) {
	ARegion region = null;
	if (regionID.length() > 0) {
	    region = _product.getARegion(regionID);
	} else {
	    region = _product.getRegion().get(0);
	}
	if (region == null) {
	    return null;
	}
	ARepeat rep = region.getRepeat_list().getARepeat(repeatID);
	return rep;
    }

    // フォントのベースフォント変更
    public boolean setBaseFont(final String fid, final int base_id) {
	Fonts fon = _product.getFonts();
	AFont f = fon.findFontName(fid);
	if (f == null) {
	    return false;
	}
	f.setBasefon(base_id);
	return true;
    }

    private static class DrawingContext {
	PenStyle.style savePenStyle;
	final float _reg_X, _reg_Y, _reg_width, _reg_height;

	DrawingContext(final float reg_X, final float reg_Y, final float reg_width, final float reg_height) {
	    _reg_X = reg_X;
	    _reg_Y = reg_Y;
	    _reg_width = reg_width;
	    _reg_height = reg_height;
	}
    }

    private void serializedFileCheck(final String productName) {
	final File xmlFile = new File(xmlPdfpath.getXmlPath());
	if (xmlFile.exists()) {
	    boolean execSerializeFlg = false;
	    final File serialFile = new File(xmlPdfpath.getSerialPath());
	    if (serialFile.exists()) {
		if (xmlFile.lastModified() > serialFile.lastModified()) {
		    log.info(" シリアライズされたファイルが古いため再シリアライズ : xmlFile (" + xmlFile.getPath() + " : "
			    + millisToDateTime(xmlFile.lastModified()) + ") > serialFile (" + serialFile.getPath()
			    + " : " + millisToDateTime(serialFile.lastModified()) + ")");
		    execSerializeFlg = true;
		} else {
		    final File xmlFldFile = new File(xmlPdfpath.getFieldxmlpath());
		    if (xmlFldFile.exists()) {
			final File serialFldFile = new File(xmlPdfpath.getSerialfldPath());
			if (serialFldFile.exists()) {
			    if (xmlFldFile.lastModified() > serialFldFile.lastModified()) {
				log.info(" シリアライズされたフィールドファイルが古いため再シリアライズ : xmlFile (" + xmlFldFile.getPath() + " : "
					+ millisToDateTime(xmlFldFile.lastModified()) + ") > serialFile ("
					+ serialFldFile.getPath() + " : "
					+ millisToDateTime(serialFldFile.lastModified()) + ")");
				execSerializeFlg = true;
			    }
			} else {
			    log.info(" シリアライズ : xmlFldFile (" + xmlFldFile.getPath() + " : "
				    + millisToDateTime(xmlFldFile.lastModified()) + ") ");
			    execSerializeFlg = true;
			}
		    }
		}
	    } else {
		log.info(" シリアライズ : xmlFile (" + xmlFile.getPath() + " : " + millisToDateTime(xmlFile.lastModified())
			+ ") ");
		execSerializeFlg = true;
	    }
	    if (execSerializeFlg) {
		createSerializedFile(xmlPdfpath.getBasePath(), productName);
	    }
	}
    }

    private static String millisToDateTime(final long millis) {
	final Calendar cal = Calendar.getInstance();
	cal.setTimeInMillis(millis);
	final DecimalFormat df = new DecimalFormat("00");
	return String.valueOf(cal.get(Calendar.YEAR)) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-"
		+ df.format(cal.get(Calendar.DAY_OF_MONTH)) + " " + df.format(cal.get(Calendar.HOUR_OF_DAY)) + ":"
		+ df.format(cal.get(Calendar.MINUTE)) + ":" + df.format(cal.get(Calendar.SECOND));
    }

    /**
     * フォームのxmlファイルからシリアライズされたファイル（{pdfPath}/serial/*.bin,
     * *.fld.bin）とテンプレートのファイル({pdfPath}/template/*pdf)を作成する
     *
     * @param pdfPath
     *            xmlのディレクトリ
     * @param pdfname
     *            xmlの名前（拡張子なし）。場所は{pdfPath}/xml/*.xml
     */
    private static void createSerializedFile(final String pdfPath, final String pdfname) {
	final Xml_Product xPro = new Xml_Product(pdfPath);
	xPro.Read(pdfname);

	final SeriToPdf pdf = new SeriToPdf(pdfPath);
	pdf._doSerializeFileCheck = false; // シリアライズをチェックしない
	pdf.Init(pdfname); // 使用するXML指定
	// テンプレート作成
	pdf.Make_PDF_Template();
	// 終了
	pdf.Close();
    }
}
