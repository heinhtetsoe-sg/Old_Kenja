package alp.co.jp.text;

import java.awt.Color;
import java.io.Serializable;

import alp.co.jp.util.ColorUtil;

public class FieldTextAttribute implements Serializable, Cloneable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;

	private Color color, bkcolor;
	private boolean strike, underline, fullpaint; // 印字範囲を背景色で塗り潰す。追加;

	public FieldTextAttribute clone(){
        try{
            return (FieldTextAttribute)super.clone();
        }catch(CloneNotSupportedException e){
            throw new InternalError(e.toString());
        }
    }
	public Color getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = ColorUtil.getValue(color);
	}
	public Color getBkcolor() {
		return bkcolor;
	}
	public void setBkcolor(String bkcolor) {
		this.bkcolor = ColorUtil.getValue(bkcolor);
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
	public boolean isFullpaint() {
		return fullpaint;
	}
	public void setFullpaint(boolean fullpaint) {
		this.fullpaint = fullpaint;
	}
}
