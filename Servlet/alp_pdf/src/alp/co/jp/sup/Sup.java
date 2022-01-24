package alp.co.jp.sup;

import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;

public abstract class Sup {
	//出力位置指定は共通
	protected float x, y;
	protected float leftmargine, topmargine, pagelength;

	public Sup() {
		// TODO 自動生成されたコンストラクター・スタブ
		x = y = leftmargine = topmargine = 0f;
		pagelength = 842f;	//A4縦を初期値とする。
	}
	public void SetInit(float pagelength, float leftmargine, float topmargine) {
		this.pagelength = pagelength;
		this.leftmargine = leftmargine;
		this.topmargine = topmargine;
	}
	/*public void SetPos(float x, float y) {
		this.x = x - leftmargine;
		this.y = pagelength - (y + topmargine);
	}*/
	public abstract void SetPos(float x, float y);
	//出力処理を実装のこと
	public abstract void Out(PdfContentByte cb) throws DocumentException, IOException;
}
