package alp.co.jp.text;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.AreaElement;
import alp.co.jp.RepeatElement;
import alp.co.jp.util.TextPosition;
import alp.co.jp.util.RepeatUtil.REP_PLACE;

public class FieldText implements Serializable, Cloneable, RepeatElement, AreaElement {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private int fontID, x, y, width, height, offsetX, offsetY;
	private String text, path;
	private TextPosition.position textAlignment, verticalAlignment;
	ArrayList<FieldTextAttribute> textattr;	//フィールドの属性配列(繰返し数分確保)
	private String repeatID = null;
	private REP_PLACE repPlace;

	public FieldText clone() {
		try {
			return (FieldText) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
	
	public String toString() {
	    return "FieldText(text = " + text + ", path = " + path + ")";
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	public void setColor(int idx, String color) {
		FieldTextAttribute attr = textattr.get(idx);
		attr.setColor(color);
	}

	public Color getColor(int idx) {
		FieldTextAttribute attr = textattr.get(idx);
		return attr.getColor();
	}

	public void setBkcolor(int idx, String bkcolor) {
		FieldTextAttribute attr = textattr.get(idx);
		attr.setBkcolor(bkcolor);
	}

	public Color getBkcolor(int idx) {
		FieldTextAttribute attr = textattr.get(idx);
		return attr.getBkcolor();
	}

	public boolean isStrike(int idx) {
		FieldTextAttribute attr = textattr.get(idx);
		return attr.isStrike();
	}

	public void setStrike(int idx, boolean strike) {
		FieldTextAttribute attr = textattr.get(idx);
		attr.setStrike(strike);
	}

	public boolean isUnderline(int idx) {
		FieldTextAttribute attr = textattr.get(idx);
		return attr.isUnderline();
	}

	public void setUnderline(int idx, boolean underline) {
		FieldTextAttribute attr = textattr.get(idx);
		attr.setUnderline(underline);
	}

	public boolean isFullpaint(int idx) { // 印字範囲を背景色で塗り潰す。追加;
		FieldTextAttribute attr = textattr.get(idx);
		return attr.isFullpaint();
	}

	public void setFullpaint(int idx, boolean fullpaint) {
		FieldTextAttribute attr = textattr.get(idx);
		attr.setFullpaint(fullpaint);
	}

	public void setTextAttribute(int repeat, String color, String bkcolor,
			boolean strike, boolean underline) {
		int l = repeat > 0 ? repeat : 1;
		if (textattr != null)
			textattr.clear();
		textattr = new ArrayList<FieldTextAttribute>(l);
		for (; l > 0; l--) {
			FieldTextAttribute attr = new FieldTextAttribute();
			attr.setColor(color);
			attr.setBkcolor(bkcolor);
			attr.setStrike(strike);
			attr.setUnderline(underline);
			attr.setFullpaint(false);
			textattr.add(attr);
		}
	}

	public void setTextAttribute(int repeat, String color, String bkcolor,
			boolean strike, boolean underline, boolean fullpaint) {
		int l = repeat > 0 ? repeat : 1;
		if (textattr != null)
			textattr.clear();
		textattr = new ArrayList<FieldTextAttribute>(l);
		for (; l > 0; l--) {
			FieldTextAttribute attr = new FieldTextAttribute();
			attr.setColor(color);
			attr.setBkcolor(bkcolor);
			attr.setStrike(strike);
			attr.setUnderline(underline);
			attr.setFullpaint(fullpaint);
			textattr.add(attr);
		}
	}

	public String getRepeatID() {
		return repeatID;
	}

	public void setRepeatID(String repeatID) {
		this.repeatID = repeatID;
	}

	public REP_PLACE getRepPlace() {
		return null == repPlace ? REP_PLACE.BODY : repPlace;
	}

	public void setRepPlace(REP_PLACE repPlace) {
		this.repPlace = repPlace;
	}
}
