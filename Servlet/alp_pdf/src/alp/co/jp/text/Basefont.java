package alp.co.jp.text;

import java.io.Serializable;

import com.lowagie.text.pdf.AsianFontMapper;
import com.lowagie.text.pdf.BaseFont;

public class Basefont implements Serializable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private int No;
	private String pdfFontName;
	//private encode pdfEncoding;
	private String pdfEncoding;
	private String embeddedPath;

	private enum encode {
		UniJIS_HW_H	(AsianFontMapper.JapaneseEncoding_HW_H),
		UniJIS_H	(AsianFontMapper.JapaneseEncoding_H),
		UniGB_H		(AsianFontMapper.ChineseSimplifiedEncoding_H),
		UniCNS_H	(AsianFontMapper.ChineseTraditionalEncoding_H),
		UniKS_H		(AsianFontMapper.KoreanEncoding_H),
		IDENTI_H	(BaseFont.IDENTITY_H),
		UniJIS_HW_V	(AsianFontMapper.JapaneseEncoding_HW_V),
		UniJIS_V	(AsianFontMapper.JapaneseEncoding_V),
		UniGB_V		(AsianFontMapper.ChineseSimplifiedEncoding_V),
		UniCNS_V	(AsianFontMapper.ChineseTraditionalEncoding_V),
		UniKS_V		(AsianFontMapper.KoreanEncoding_V),
		IDENTI_V	(BaseFont.IDENTITY_V);
		private String name;
		encode(String name){ this.name = name; }
		public String getName() { return name; }
	}
	//縦横の指定があってもなくても可とした 2015/11/26
	public static String filterValue(String str) {
		for(encode e: encode.values()) {
			String cmp = e.getName();
			int len = cmp.length();
			if ((str.equals(cmp))||(str.equals(cmp.substring(0, len-2))))
				return cmp.substring(0, len-2);
		}
		return str;
	}

	public String getPdfFontName() {
		return pdfFontName;
	}
	public void setPdfFontName(String pdfFontName) {
		this.pdfFontName = pdfFontName;
	}
	public String getPdfEncoding() {
		//return pdfEncoding.getName();
		return pdfEncoding;
	}
	public void setPdfEncoding(String pdfEncoding) {
		this.pdfEncoding = filterValue(pdfEncoding);
	}
	public String getEmbeddedPath() {
		return embeddedPath;
	}
	public void setEmbeddedPath(String embeddedPath) {
		this.embeddedPath = embeddedPath;
	}
	public int getNo() {
		return No;
	}
	public void setNo(int no) {
		this.No = no;
	}
}
