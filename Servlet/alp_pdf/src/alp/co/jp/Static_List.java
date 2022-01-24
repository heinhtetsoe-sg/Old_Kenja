package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.text.*;

public class Static_List implements Serializable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<StaticText> text_ = null;
	private ArrayList<StaticImageURL> image_ = null;
	private ArrayList<StaticBarcode> barcode_ = null;

	public Static_List() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
		text_ = new ArrayList<StaticText>();
		image_ = new ArrayList<StaticImageURL>();
		barcode_ = new ArrayList<StaticBarcode>();
	}

	public ArrayList<StaticText> getText_() {
		return text_;
	}
	public ArrayList<StaticImageURL> getImage_() {
		return image_;
	}
	public ArrayList<StaticBarcode> getBarcode_() {
		return barcode_;
	}

	public int addText(StaticText text) {
		text_.add(text);
		return text_.size();
	}
	public int addImage(StaticImageURL image) {
		image_.add(image);
		return image_.size();
	}
	public int addBarcode(StaticBarcode barcode) {
		barcode_.add(barcode);
		return barcode_.size();
	}
	public void AddAll(Static_List rslist) {
		ArrayList<StaticText> ltext = rslist.getText_();
		ArrayList<StaticImageURL> limage = rslist.getImage_();
		ArrayList<StaticBarcode> lbarcode = rslist.getBarcode_();
		
		if (ltext.size()>0)
			text_.addAll(ltext);
		if (limage.size()>0)
			image_.addAll(limage);
		if (lbarcode.size()>0)
			barcode_.addAll(lbarcode);
	}
}
