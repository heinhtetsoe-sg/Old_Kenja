package alp.co.jp.text;

import java.awt.Color;
import java.io.Serializable;

import alp.co.jp.AreaElement;
import alp.co.jp.RepeatElement;
import alp.co.jp.util.ColorUtil;
import alp.co.jp.util.TextPosition;
import alp.co.jp.util.RepeatUtil.REP_PLACE;

public class StaticText implements Serializable, Cloneable, RepeatElement, AreaElement {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private int fontID, x, y, width, height, offsetX, offsetY;
	private String text;
	private TextPosition.position textAlignment, verticalAlignment;
	private Color color = Color.BLACK, bkcolor = Color.BLACK;
	private boolean strike, underline, fullpaint; // 印字範囲を背景色で塗り潰す。追加
	private String repeatID = null;
	private REP_PLACE repPlace;

	public StaticText clone() {
		try {
			return (StaticText) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	public TextPosition.position getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(String textAlignment) {
		this.textAlignment = TextPosition.getValue(textAlignment);
	}

	public TextPosition.position getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(String verticalAlignment) {
		this.verticalAlignment = TextPosition.getValue(verticalAlignment);
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

	public int getFontID() {
		return fontID;
	}

	public void setFontID(int fontID) {
		this.fontID = fontID;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setColor(String str) {
		this.color = ColorUtil.getValue(str);
	}

	public Color getColor() {
		return color;
	}

	public void setBkcolor(String str) {
		this.bkcolor = ColorUtil.getValue(str);
	}

	public Color getBkcolor() {
		return bkcolor;
	}

	public boolean isStrike() {
		return strike;
	}

	public void setStrike(boolean strike) {
		this.strike = strike;
	}

	public boolean isUnderline() {
		return underline;
	}

	public void setUnderline(boolean underline) {
		this.underline = underline;
	}

	public String getRepeatID() {
		return repeatID;
	}

	public void setRepeatID(String repeatID) {
		this.repeatID = repeatID;
	}

	public boolean isFullpaint() {
		return fullpaint;
	}

	public void setFullpaint(boolean fullpaint) {
		this.fullpaint = fullpaint;
	}

	public REP_PLACE getRepPlace() {
		return null == repPlace ? REP_PLACE.BODY : repPlace;
	}

	public void setRepPlace(REP_PLACE repPlace) {
		this.repPlace = repPlace;
	}
}
