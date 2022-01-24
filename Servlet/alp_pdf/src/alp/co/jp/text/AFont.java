package alp.co.jp.text;

import java.io.Serializable;

public class AFont implements Serializable, Cloneable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private String fID;
	private int basefon;
	private int point;
	private int linespace;
	private boolean vertical;
	private boolean bold;
	private boolean italic;
	private int style;
	private Float width=0.0f;

	public AFont clone(){
        try{
            return (AFont)super.clone();
        }catch(CloneNotSupportedException e){
            throw new InternalError(e.toString());
        }
    }
	public String getID() {
		return fID;
	}
	public void setID(String iD) {
		fID = iD;
	}
	public int getBasefon() {
		return basefon;
	}
	public void setBasefon(int basefon) {
		this.basefon = basefon;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	public int getLinespace() {
		return linespace;
	}
	public void setLinespace(int linespace) {
		this.linespace = linespace;
	}
	public boolean getVertical() {
		return vertical;
	}
	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}
	public boolean getBold() {
		return bold;
	}
	public void setBold(boolean bold) {
		this.bold = bold;
	}
	public boolean getItalic() {
		return italic;
	}
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	public int getStyle() {
		//style = java.awt.Font.PLAIN | java.awt.Font.CENTER_BASELINE;
		//java.awt.Font.CENTER_BASELINEはjava.awt.Font.BOLDと同値なのでやめる
		style = java.awt.Font.PLAIN ;
		if (getBold() == true)
			style |= java.awt.Font.BOLD;
		if (getItalic() == true)
			style |=java.awt.Font.ITALIC;
		return style;
	}
	public Float getWidth() {
		return width;
	}
	public void setWidth(Float width) {
		this.width = width;
	}
}
