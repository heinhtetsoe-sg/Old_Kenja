package alp.co.jp.text;

import java.io.Serializable;

import alp.co.jp.AreaElement;
import alp.co.jp.RepeatElement;
import alp.co.jp.util.RepeatUtil.REP_PLACE;

public class StaticImageURL implements Serializable, Cloneable, RepeatElement, AreaElement {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private int x, y, width, height, offsetX, offsetY;
	private String url;
	private String repeatID = null;
	private REP_PLACE repPlace;

	public StaticImageURL clone() {
		try {
			return (StaticImageURL) super.clone();
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

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
