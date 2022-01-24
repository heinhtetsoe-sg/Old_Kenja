/**
 *	繰返しオブジェクトのリスト
 */
package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.line.PenStyle;
import alp.co.jp.util.RepeatUtil.REP_PLACE;

public class Repeat_List implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<ARepeat> repeat_ = null;

    public Repeat_List() {
	super();
	repeat_ = new ArrayList<ARepeat>();
    }

    public ArrayList<ARepeat> getRepeat_() {
	return repeat_;
    }

    public int getSize() {
	return repeat_.size();
    }

    public ARepeat getARepeat(final int ID) {
	int sz = repeat_.size();
	if ((ID <= sz) && (ID > 0)) {
	    return repeat_.get(ID - 1);
	}
	return null;
    }

    public ARepeat getARepeat(final String rID) {
	if (rID == null) {
	    return null;
	}
	int max = repeat_.size();
	for (int idx = 0; idx < max; idx++) {
	    ARepeat rp = repeat_.get(idx);
	    if (rID.equals(rp.getID())) {
		return rp;
	    }
	}
	return null;
    }

    public int addRepeat(final ARepeat repeat) {
	repeat_.add(repeat);
	return repeat_.size();
    }

    // 繰返しの情報取出しオブジェクト
    public class RepeatEx {
	private int addX, addY, repeatX, repeatY, intervalX, intervalY;
	private final int pen_width;
	private final PenStyle.style penstyle;
	private boolean Vertical;
	private boolean ParentRepeat;
	private final ArrayList<String> chkParent = new ArrayList<String>();
	private boolean P_Stop;

	public RepeatEx(final ARepeat rp, final REP_PLACE re) {
	    String rID = rp.getID();
	    repeatX = rp.getRepeatX();
	    repeatY = rp.getRepeatY();
	    intervalX = rp.getIntervalX();
	    intervalY = rp.getIntervalY();
	    pen_width = rp.getPen_width();
	    penstyle = rp.getPenstyle();
	    addX = repeatX * intervalX; // Ｘ軸 原点加算
	    addY = repeatY * intervalY; // Ｙ軸 原点加算
	    Vertical = ((rp.isVertical() == true) || (addX == 0)) ? true : false;
	    if (re == REP_PLACE.FOOTER) { // 繰返しフッター
		repeatX = repeatX > 0 ? 1 : 0; // Ｘ軸繰返し無し
		repeatY = repeatY > 0 ? 1 : 0; // Ｙ軸繰返し無し
	    } else if (re == REP_PLACE.HEDER) {
		addX = addY = 0; // 原点加算なし
		repeatX = repeatX > 0 ? 1 : 0; // Ｘ軸繰返し無し
		repeatY = repeatY > 0 ? 1 : 0; // Ｙ軸繰返し無し
	    } else { // ボディ(含む要因)
		addX = addY = 0; // 原点加算なし
	    }
	    // 親繰返しを付加
	    String Parent = rp.getParent();
	    if (!Parent.isEmpty()) {
		P_Stop = false;
		chkParent.add(rID); // 最初の繰返し
		int length = Parent.length();
		if (Parent.lastIndexOf("〆") >= (length - 1)) {
		    P_Stop = true;
		    // 相互親の無効指定マークはずす
		    Parent = Parent.substring(0, length - 1);
		}
		while (!Parent.isEmpty()) {
		    String NextParent = RepeatPosition(rID, Parent);
		    rID = Parent;
		    Parent = NextParent;
		}
		// 縦並びを再度決定
		if ((repeatY <= 1) && (repeatX > 1)) {
		    Vertical = false;
		} else if ((repeatX <= 1) && (repeatY > 1)) {
		    Vertical = true;
		}
	    }
	}

	private String RepeatPosition(final String OrgRepeatID, final String ParentID) {
	    String parent_id = ParentID;
	    // 親の繰返し名称に相互親の無効指定マーク付加されているか検査
	    // 既に処理済の繰返しか検査
	    if (chkParent.indexOf(ParentID) >= 0) {
		return "";
	    }
	    ARepeat rp = getARepeat(ParentID);
	    if (rp == null) {
		return "";
	    }
	    // 次の親の名称取出し
	    String NextParent = rp.getParent();
	    if (!NextParent.isEmpty()) {
		int length = NextParent.length();
		if (NextParent.lastIndexOf("〆") >= (length - 1)) {
		    // 相互親の無効指定マークはずす
		    NextParent = NextParent.substring(0, length - 1);
		}
	    }
	    // 相互親の無効指定なら処理前に中断
	    if ((P_Stop == true) && (NextParent.compareTo(OrgRepeatID) == 0)) {
		return "";
	    }
	    int xn0, yn0, xi0, yi0;
	    xn0 = rp.getRepeatX();
	    yn0 = rp.getRepeatY();
	    xi0 = rp.getIntervalX();
	    yi0 = rp.getIntervalY();
	    if (xn0 > 1) {
		if (repeatX == 0) {
		    repeatX = xn0;
		    intervalX = xi0;
		} else {
		    addX += (xi0 * xn0);
		}
	    }
	    if (yn0 > 1) {
		if (repeatY == 0) {
		    repeatY = yn0;
		    intervalY = yi0;
		} else {
		    addY += (yi0 * yn0);
		}
	    }
	    chkParent.add(parent_id);
	    return NextParent;
	}

	public int getRepeatX() {
	    return (repeatX > 0) ? repeatX : 1;
	}

	public int getRepeatY() {
	    return (repeatY > 0) ? repeatY : 1;
	}

	public int getIntervalX() {
	    return intervalX;
	}

	public int getIntervalY() {
	    return intervalY;
	}

	public int getPen_width() {
	    return pen_width;
	}

	public PenStyle.style getPenstyle() {
	    return penstyle;
	}

	public boolean isVertical() {
	    return Vertical;
	}

	public boolean isParentRepeat() {
	    return ParentRepeat;
	}

	// 指定繰返し回数の位置を取出す
	public int calcuAddX(final int count) {
	    if (Vertical == false) {
		return (repeatX > 0) ? (count % repeatX) * intervalX + addX : addX;
	    } else {
		return (repeatY > 0) ? (count / repeatY) * intervalX + addX : addX;
	    }
	}

	public int calcuAddY(final int count) {
	    if (Vertical == false) {
		return (repeatX > 0) ? (count / repeatX) * intervalY + addY : addY;
	    } else {
		return (repeatY > 0) ? (count % repeatY) * intervalY + addY : addY;
	    }
	}
    }

    // 繰返しの情報取出しオブジェクト作成
    public RepeatEx RepeatEx(final String rID, final REP_PLACE re) {
	// TODO 自動生成されたメソッド・スタブ
	if (rID == null) {
	    return null;
	}
	ARepeat rp = getARepeat(rID);
	if (rp == null) {
	    return null;
	}
	RepeatEx repeatEx = new RepeatEx(rp, re);
	return repeatEx;
    }
}
