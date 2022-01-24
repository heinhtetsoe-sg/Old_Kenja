package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.text.FieldText;
import alp.co.jp.text.FieldBarcode;
import alp.co.jp.text.FieldImageURL;

public class Field_List implements Serializable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<FieldText> text_ = null;
	private ArrayList<FieldImageURL> image_ = null;
	private ArrayList<FieldBarcode> barcode_ = null;

	public Field_List() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
		text_ = new ArrayList<FieldText>();
		image_ = new ArrayList<FieldImageURL>();
		barcode_ = new ArrayList<FieldBarcode>();
	}

	public ArrayList<FieldText> getText_() {
		return text_;
	}
	public ArrayList<FieldImageURL> getImage_() {
		return image_;
	}
	public ArrayList<FieldBarcode> getBarcode_() {
		return barcode_;
	}

	public int addText(FieldText text) {
		text_.add(text);
		return text_.size();
	}
	public int addImage(FieldImageURL image) {
		image_.add(image);
		return image_.size();
	}
	public int addBarcode(FieldBarcode barcode) {
		barcode_.add(barcode);
		return barcode_.size();
	}
	public void AddAll(Field_List rflist) {
		ArrayList<FieldText> ltext = rflist.getText_();
		ArrayList<FieldImageURL> limage = rflist.getImage_();
		ArrayList<FieldBarcode> lbarcode = rflist.getBarcode_();
		
		if (ltext.size()>0)
			text_.addAll(ltext);
		if (limage.size()>0)
			image_.addAll(limage);
		if (lbarcode.size()>0)
			barcode_.addAll(lbarcode);
	}
}