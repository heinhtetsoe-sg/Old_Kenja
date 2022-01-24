package alp.co.jp.line;

import java.io.Serializable;

import alp.co.jp.AreaElement;
import alp.co.jp.RepeatElement;
import alp.co.jp.util.RepeatUtil.REP_PLACE;

public class RundRect implements Serializable, Cloneable, RepeatElement, AreaElement {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private int x, y, width, height, pen_width, radius;
	private RundCorner.CORNER psition;
	private PenStyle.style penstyle;
	private String repeatID = null;
	private REP_PLACE repPlace;

	public RundRect clone() {
		try {
			return (RundRect) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getPen_width() {
		return pen_width;
	}

	public void setPen_width(int penWidth) {
		pen_width = penWidth;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public PenStyle.style getPen_style() {
		return penstyle;
	}

	public void setPen_style(String penStyle) {
		this.penstyle = PenStyle.getValue(penStyle);
	}

	public RundCorner.CORNER getPsition() {
		return psition;
	}

	public void setPsition(String psition) {
		this.psition = RundCorner.getValue(psition);
	}

	public String getRepeatID() {
		return repeatID;
	}

	public void setRepeatID(String repeatID) {
		this.repeatID = repeatID;
	}

	public REP_PLACE getRepPlace() {
		return repPlace;
	}

	public void setRepPlace(REP_PLACE repPlace) {
		this.repPlace = repPlace;
	}
}
