package alp.co.jp;

import java.io.Serializable;

import alp.co.jp.line.PenStyle;

public class ARepeat implements Serializable, Cloneable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private String rID, Parent;
	private int repeatX, repeatY, intervalX, intervalY;
	private int pen_width;
	private PenStyle.style penstyle;
	private boolean Vertical;

	public ARepeat clone() {
		try {
			return (ARepeat) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	public String getID() {
		return rID;
	}

	public void setID(String rID) {
		this.rID = rID;
	}

	public int getRepeatX() {
		return repeatX;	//2015/12/07 繰返し0なら親の繰返しを使うようにするため削除 > 0 ? repeatX : (repeatY > 0 ? 1 : 0);
	}

	public void setRepeatX(int repeatX) {
		this.repeatX = repeatX;
	}

	public int getRepeatY() {
		return repeatY;	//2015/12/07 繰返し0なら親の繰返しを使うようにするため削除 > 0 ? repeatY : (repeatX > 0 ? 1 : 0);
	}

	public void setRepeatY(int repeatY) {
		this.repeatY = repeatY;
	}

	public int getIntervalX() {
		return intervalX;
	}

	public void setIntervalX(int intervalX) {
		this.intervalX = intervalX;
	}

	public int getIntervalY() {
		return intervalY;
	}

	public void setIntervalY(int intervalY) {
		this.intervalY = intervalY;
	}

	public String getParent() {
		return Parent;
	}

	public void setParent(String Parent) {
		this.Parent = Parent;
	}

	public int getPen_width() {
		return pen_width;
	}

	public void setPen_width(int penWidth) {
		pen_width = penWidth;
	}

	public PenStyle.style getPenstyle() {
		return penstyle;
	}

	public void setPenstyle(PenStyle.style penstyle) {
		this.penstyle = penstyle;
	}

	public boolean isVertical() {
		return Vertical;
	}

	public void setVertical(boolean vertical) {
		Vertical = vertical;
	}

	// 指定繰返し回数の位置を取出す
	public int calcuAddX(int count) {
		if (Vertical == false)
			return (count % repeatX) * intervalX;
		else
			return (count / repeatY) * intervalX;
	}

	public int calcuAddY(int count) {
		if (Vertical == false)
			return (count / repeatX) * intervalY;
		else
			return (count % repeatY) * intervalY;
	}
}
