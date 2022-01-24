package alp.co.jp.util;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;

import alp.co.jp.BaseFonts;
import alp.co.jp.Fonts;
import alp.co.jp.text.AFont;
import alp.co.jp.text.Basefont;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

public class FontUtil {

	static ArrayList<BaseFontInf> basefontinf_ = new ArrayList<BaseFontInf>();
	static ArrayList<FontInf> fontinf_ = new ArrayList<FontInf>();

	public enum FONTKIND {
		EUDC("外字"), MINCHYO("ＰＣの明朝"), GOTHIC("ＰＣのゴシック")
		, SIMPCH("簡体中国語"), MINTCH("繁体中国語明朝"), GOHTCH("繁体中国語ゴシック")
		, MINKS("韓国語明朝"), GOHKS("韓国語ゴシック"), ETCKANJI("その他"), ANSI("半角文字フォント");
		private String name;

		FONTKIND(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		public boolean comp(String cmpstr) {
			int len = name.length();
			if (cmpstr.length()<len)
				return false;
			return cmpstr.substring(0, len).equals(name);
		}
	}


	public class BaseFontInf {
		private int id;
		private String name;
		private String encode;
		private String encode_v;
		private FONTKIND fontkind;
		private BaseFont baseFont_H;
		private BaseFont baseFont_V;
		private boolean embedded;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public FONTKIND getFontkind() {
			return fontkind;
		}

		public void setFontkind(FONTKIND fontkind) {
			this.fontkind = fontkind;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEncode() {
			return encode;
		}

		public void setEncode(String encode) {
			this.encode = encode;
		}

		public String getEncode_v() {
			return encode_v;
		}

		public void setEncode_v(String encode_v) {
			this.encode_v = encode_v;
		}

		public BaseFont getBaseFont_H() {
			return baseFont_H;
		}

		public void setBaseFont_H(BaseFont baseFontH) {
			baseFont_H = baseFontH;
		}

		public BaseFont getBaseFont_V() {
			return baseFont_V;
		}

		public void setBaseFont_V(BaseFont baseFontV) {
			baseFont_V = baseFontV;
		}

		public boolean isEmbedded() {
			return embedded;
		}

		public void setEmbedded(boolean embedded) {
			this.embedded = embedded;
		}

	}

	public class FontInf {
		int base_id, point, style, linespace;
		Float width;
		boolean vertical;

		public FontInf(int base_id, int point, int style, int linespace,
				Float width, boolean vertical) {
			this.base_id = base_id;
			this.point = point;
			this.style = style;
			this.linespace = linespace;
			this.width = width;
			this.vertical = vertical;
		}

		public float getPoint() {
			return point / 10f;
		}

		public float getLineheight() {
			return (point * 10 + linespace) / 100f;
		}

		public float getLinespace() {
			return linespace / 100f;
		}

		public int getStyle() {
			return style;
		}

		public FONTKIND getFontKind() {
			return getBaseFontInf(base_id).getFontkind();
		}

		public String getEncode() {
			if (this.vertical == false)
				return getBaseFontInf(base_id).getEncode();
			else
				return getBaseFontInf(base_id).getEncode_v();
		}

		public boolean isVertical() {
			return vertical;
		}

		public String getName() {
			return getBaseFontInf(base_id).getName();
		}

		public Float getWidth() {
			return width;
		}

		// 外字
		public BaseFont getEudcBaseFont() {
			BaseFontInf bf = basefontinf_.get(0);
			if (this.vertical == false)
				return bf.getBaseFont_H();
			else
				return bf.getBaseFont_V();
		}
		// BaseFont
		public BaseFont getBaseFont() {
			BaseFontInf bf = basefontinf_.get(base_id);
			if (this.vertical == false)
				return bf.getBaseFont_H();
			else
				return bf.getBaseFont_V();
		}
		public void setBaseFont(int base_id) {
			this.base_id = base_id;
		}
	}

	/**
	 * 基本フォントを取込みBaseFontを作成
	 */
	public FontUtil(BaseFonts basefonts) {
		int nmb = basefonts.getSize();
		for (int i = 0; i < nmb; i++) {
			Basefont bfont = basefonts.getBasefont(i);
			String name = bfont.getPdfFontName();
			String enbe_path = bfont.getEmbeddedPath();
			String encode = bfont.getPdfEncoding().trim();
			String encode_h = "UniJIS-UCS2-HW-H";
			String encode_v = "UniJIS-UCS2-HW-V";
			//エンコードの決定
			if (encode.isEmpty()) {
				encode = BaseFont.CP1252;
			}
			if (encode.substring(0, 1).equals("C")) {
				encode_h = encode_v = encode;
			}
			else {	//Identity・"UniJIS-UCS2-HW・"UniGB-UCS2・UniCNS-UCS2・UniKS-UCS2
				encode_h = encode + "-H";
				encode_v = encode + "-V";
			}
			int bno = bfont.getNo();
			BaseFontInf inf = new BaseFontInf();
			inf.setId(bno);
			inf.setEncode(encode_h);
			inf.setEncode_v(encode_v);
			if (FONTKIND.EUDC.comp(name)) {
				inf.setName(enbe_path);
				inf.setFontkind(FONTKIND.EUDC);
				//inf.setEncode("Identity-H");
				//inf.setEncode_v("Identity-V");
				inf.setEmbedded(true);
			} else if (FONTKIND.MINCHYO.comp(name)) {
				inf.setFontkind(FONTKIND.MINCHYO);
				inf.setName("HeiseiMin-W3");
				//inf.setEncode("UniJIS-UCS2-HW-H");
				//inf.setEncode_v("UniJIS-UCS2-HW-V");
				// /inf.setEncode("UniJIS-UCS2-H");
				// /inf.setEncode_v("UniJIS-UCS2-V");
				inf.setEmbedded(false);
			} else if (FONTKIND.GOTHIC.comp(name)) {
				inf.setFontkind(FONTKIND.GOTHIC);
				inf.setName("HeiseiKakuGo-W5");
				//inf.setEncode("UniJIS-UCS2-HW-H");
				//inf.setEncode_v("UniJIS-UCS2-HW-V");
				// /inf.setEncode("UniJIS-UCS2-H");
				// /inf.setEncode_v("UniJIS-UCS2-V");
				inf.setEmbedded(false);
			} else if (FONTKIND.SIMPCH.comp(name)) {
				inf.setFontkind(FONTKIND.SIMPCH);
				inf.setName("STSong-Light");
				//inf.setEncode("UniGB-UCS2-H");
				//inf.setEncode_v("UniGB-UCS2-V");
				inf.setEmbedded(false);
			} else if (FONTKIND.MINTCH.comp(name)) {
				inf.setFontkind(FONTKIND.MINTCH);
				inf.setName("MSung-Light");
				//inf.setEncode("UniCNS-UCS2-H");
				//inf.setEncode_v("UniCNS-UCS2-V");
				inf.setEmbedded(false);
			} else if (FONTKIND.GOHTCH.comp(name)) {
				inf.setFontkind(FONTKIND.GOHTCH);
				inf.setName("MHei-Medium");
				//inf.setEncode("UniCNS-UCS2-H");
				//inf.setEncode_v("UniCNS-UCS2-V");
				inf.setEmbedded(false);
			} else if (FONTKIND.MINKS.comp(name)) {
				inf.setFontkind(FONTKIND.MINKS);
				inf.setName("HYSMyeongJo-Medium");
				//inf.setEncode("UniKS-UCS2-H");
				//inf.setEncode_v("UniKS-UCS2-V");
				inf.setEmbedded(false);
			} else if (FONTKIND.GOHKS.comp(name)) {
				inf.setFontkind(FONTKIND.GOHKS);
				inf.setName("HYGoThic-Medium");
				//inf.setEncode("UniKS-UCS2-H");
				//inf.setEncode_v("UniKS-UCS2-V");
				inf.setEmbedded(false);
			} else {
				String[] ansi0 = { Font.SERIF, Font.SANS_SERIF,
						Font.MONOSPACED, Font.DIALOG, Font.DIALOG_INPUT };
				String[] ansi1 = { BaseFont.TIMES_ROMAN, BaseFont.HELVETICA,
						BaseFont.COURIER, BaseFont.HELVETICA, BaseFont.COURIER};
				String[] ansi = { BaseFont.COURIER, BaseFont.COURIER_OBLIQUE,
						BaseFont.COURIER_BOLD, BaseFont.COURIER_BOLDOBLIQUE,
						BaseFont.HELVETICA, BaseFont.HELVETICA_OBLIQUE,
						BaseFont.HELVETICA_BOLD,
						BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.TIMES_ROMAN,
						BaseFont.TIMES_BOLD, BaseFont.TIMES_ITALIC,
						BaseFont.TIMES_BOLDITALIC, BaseFont.ZAPFDINGBATS,
						BaseFont.SYMBOL };
				int len = ansi.length;
				for (; len > 0; len--) {
					int nlen = ansi[len - 1].length();
					if (name.length()<nlen)
						continue;
					String cmpstr = name.substring(0, nlen);
					if (cmpstr.equals(ansi[len - 1])) {
						inf.setFontkind(FONTKIND.ANSI);
						inf.setName(cmpstr);
						inf.setEmbedded(false);
						break;
					}
				}
				if (len == 0) {
					for (len = ansi0.length; len > 0; len--) {
						int nlen = ansi0[len - 1].length();
						if (name.length()<nlen)
							continue;
						if (name.substring(0, nlen).equals(ansi0[len - 1])) {	//java.awt.Font
							inf.setFontkind(FONTKIND.ANSI);	// →
							inf.setName(ansi1[len - 1]);	//com.lowagie.text.pdf.BaseFont
							inf.setEmbedded(false);
							break;
						}
					}
				}
				if (len == 0) {
					if (enbe_path.length()>0) {
						inf.setFontkind(FONTKIND.ETCKANJI);
						inf.setName(enbe_path);
						inf.setEncode("Identity-H");
						inf.setEncode_v("Identity-V");
						inf.setEmbedded(true);
					}
					else {
						continue;
					}
				}
			}
			try {
				BaseFont H = BaseFont.createFont(inf.getName(),
						inf.getEncode(), inf.isEmbedded());
				BaseFont V = BaseFont.createFont(inf.getName(),
						inf.getEncode_v(), inf.isEmbedded());
				inf.setBaseFont_H(H);
				inf.setBaseFont_V(V);
			} catch (DocumentException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			basefontinf_.add(inf);
		}
	}

	// 基本フォント情報オブジェクト取出し
	public BaseFontInf getBaseFontInf(int id) {
		int max = basefontinf_.size();
		for (int i = 0; i < max; i++) {
			BaseFontInf inf = basefontinf_.get(i);
			if (inf.getId() == id)
				return inf;
		}
		return null;
	}

	/**
	 * フォント定義から基本フォントの位置・ポイント・スタイルの情報を保存 (フォント情報を保存)
	 */
	public void FontInf(Fonts fonts) {
		ArrayList<AFont> fonts_ = fonts.getFont_();
		int sz = fonts_.size();
		if (sz > 0)
			fontinf_.clear();
		for (int i = 0; i < sz; i++) {
			AFont afont = fonts_.get(i);
			FontInf inf = new FontInf(afont.getBasefon(), afont.getPoint(),
					afont.getStyle(), afont.getLinespace(), afont.getWidth(), afont
							.getVertical());
			fontinf_.add(inf);
		}
	}

	// フォント情報を取出す。
	public FontInf getFontInf(int ID) {
		int sz = fontinf_.size();
		if (ID <= sz)
			return fontinf_.get(ID - 1);
		else
			return null;
	}
}
