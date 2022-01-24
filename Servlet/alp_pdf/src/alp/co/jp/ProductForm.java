package alp.co.jp;

import java.io.Serializable;

public class ProductForm implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    private int pageWidth;
    private int pageHeight;
    private int topMargine;
    private int leftMargine;
    private int bottomMargine;
    private int rightMargine;
    private String sizeName;

    public ProductForm() {
	// TODO 自動生成されたコンストラクター・スタブ
    }

    public int getPageWidth() {
	return pageWidth;
    }

    public void setPageWidth(final int pageWidth) {
	this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
	return pageHeight;
    }

    public void setPageHeight(final int pageHeight) {
	this.pageHeight = pageHeight;
    }

    public int getTopMargine() {
	return topMargine;
    }

    public void setTopMargine(final int topMargine) {
	this.topMargine = topMargine;
    }

    public int getLeftMargine() {
	return leftMargine;
    }

    public void setLeftMargine(final int leftMargine) {
	this.leftMargine = leftMargine;
    }

    public int getBottomMargine() {
	return bottomMargine;
    }

    public void setBottomMargine(final int bottomMargine) {
	this.bottomMargine = bottomMargine;
    }

    public int getRightMargine() {
	return rightMargine;
    }

    public void setRightMargine(final int rightMargine) {
	this.rightMargine = rightMargine;
    }

    public String getSizeName() {
	return sizeName;
    }

    public void setSizeName(final String sizeName) {
	this.sizeName = sizeName;
    }
}
