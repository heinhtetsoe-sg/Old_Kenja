package alp.co.jp.util;

import com.lowagie.text.Image;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.Barcode39;
import com.lowagie.text.pdf.BarcodeEAN;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.Element;

public class BarCodeUtil {

	public enum BARKIND {
		CODE39("CODE39"), EAN13("EAN"), CODE128("CODE128");
		private String name;

		BARKIND(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static BARKIND getValue(String str) {
		if (str != null) {
			for (BARKIND b : BARKIND.values()) {
				if (str.equals(b.getName()))
					return b;
			}
		}
		return BARKIND.CODE39;
	}

	public static Image getBarCode(BARKIND kind, String code, PdfContentByte cb) {
		Image Bar = null;
		int i, e, dgt, n;
		switch (kind) {
		case EAN13: // EAN13
			byte[] scode = new byte[code.length() + 1];
			e = dgt = 0; // モジュラス10ウェイト3算出
			for (i = code.length(); i > 0; i--, e++) {
				scode[i - 1] = (byte) code.charAt(i - 1);
				n = scode[i - 1] - '0';
				if ((e & 1) == 0)
					dgt += (n * 3);
				else
					dgt += n;
			}
			scode[code.length()] = (byte) (((10 - (dgt % 10)) % 10) + '0');
			// String str = new String(scode);
			// System.out.println("BAR{"+str+"}");
			BarcodeEAN barcode = new BarcodeEAN();
			barcode.setCode(new String(scode));
			Bar = barcode.createImageWithBarcode(cb, null, null);
			break;
		case CODE39: // CODE39
			Barcode39 barcode39 = new Barcode39();
			barcode39.setCode(code);
			Bar = barcode39.createImageWithBarcode(cb, null, null);
			break;
		case CODE128: // CODE128
			Barcode128 barcode128 = new Barcode128();
			barcode128.setCode(code);
			barcode128.setSize(10);
			barcode128.setTextAlignment(Element.ALIGN_JUSTIFIED_ALL);
			barcode128.setBaseline(10.0F);
			char[] text = new char[code.length() * 2];
			for (i = 0; i < code.length(); i++) {
				text[i * 2] = code.charAt(i);
				text[i * 2 + 1] = ' ';
			}
			barcode128.setAltText(new String(text));
			Bar = barcode128.createImageWithBarcode(cb, null, null);
			break;
		}
		return Bar;
	}

}
