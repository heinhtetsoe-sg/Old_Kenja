package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.text.Basefont;

public class BaseFonts implements Serializable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Basefont> basefonts = null;

	public BaseFonts() {
		super();
		// TODO 自動生成されたコンストラクター・スタブ
		basefonts = new ArrayList<Basefont>();
	}

	public ArrayList<Basefont> getBasefonts() {
		return basefonts;
	}

	public int addBasefont(Basefont bft) {
		basefonts.add(bft);
		return basefonts.size();
	}

	public int getSize() {
		return basefonts.size();
	}

	public Basefont getBasefont(int idx) {
		return basefonts.get(idx);
	}
}
