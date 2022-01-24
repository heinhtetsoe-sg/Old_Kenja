package alp.co.jp;

import java.io.Serializable;

public class ARegion implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean disable;
    private Line_List line_list = null;
    private Static_List static_list = null;
    private Field_List field_list = null;
    private Repeat_List repeat_list = null;

    public ARegion() {
	// TODO 自動生成されたコンストラクター・スタブ
    }

    public String getName() {
	return name;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public int getX() {
	return x;
    }

    public void setX(final int x) {
	this.x = x;
    }

    public int getY() {
	return y;
    }

    public void setY(final int y) {
	this.y = y;
    }

    public int getWidth() {
	return width;
    }

    public void setWidth(final int width) {
	this.width = width;
    }

    public int getHeight() {
	return height;
    }

    public void setHeight(final int height) {
	this.height = height;
    }

    public boolean isDisable() {
	return disable;
    }

    public void setDisable(final boolean disable) {
	this.disable = disable;
    }

    public Line_List getLine_list() {
	return line_list;
    }

    public void setLine_list(final Line_List lineList) {
	line_list = lineList;
    }

    public Static_List getStatic_list() {
	return static_list;
    }

    public void setStatic_list(final Static_List staticList) {
	static_list = staticList;
    }

    public Field_List getField_list() {
	return field_list;
    }

    public void setField_list(final Field_List fieldList) {
	field_list = fieldList;
    }

    public void setRepeat_list(final Repeat_List repeat_list) {
	this.repeat_list = repeat_list;
    }

    public Repeat_List getRepeat_list() {
	return repeat_list;
    }
}
