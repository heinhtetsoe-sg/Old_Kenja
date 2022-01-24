package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.text.AFont;

public class Fonts implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<AFont> font_ = null;

    public Fonts() {
	super();
	// TODO 自動生成されたコンストラクター・スタブ
	font_ = new ArrayList<AFont>();
    }

    public ArrayList<AFont> getFont_() {
	return font_;
    }

    public int addFont(final AFont fn) {
	font_.add(fn);
	return font_.size();
    }

    public int getSize() {
	return font_.size();
    }

    public int findFont(final String fID) {
	int max = font_.size();
	for (int idx = 0; idx < max; idx++) {
	    AFont fn = font_.get(idx);
	    if (fID.equals(fn.getID())) {
		return idx + 1;
	    }
	}
	return 0;
    }

    public AFont findFontName(final String fID) {
	int max = font_.size();
	for (int idx = 0; idx < max; idx++) {
	    AFont fn = font_.get(idx);
	    if (fID.equals(fn.getID())) {
		return fn;
	    }
	}
	return null;
    }

    public AFont getAfont(final int ID) {
	int sz = font_.size();
	if (ID <= sz) {
	    return font_.get(ID - 1);
	}
	return null;
    }
}
