package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

public class Product implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    private String title;
    private ProductForm Form;
    private BaseFonts basefonts;
    private Fonts fonts;
    private String element;
    private ArrayList<ARegion> region = null;

    public Product() {
	super();
	region = new ArrayList<ARegion>();
    }

    public Fonts getFonts() {
	return fonts;
    }

    public void setFonts(final Fonts fonts) {
	this.fonts = fonts;
    }

    public void setElement(final String element) {
	this.element = element;
    }

    public String getElement() {
	return element;
    }

    public ArrayList<ARegion> getRegion() {
	return region;
    }

    public void setRegion(final ArrayList<ARegion> region) {
	this.region = region;
    }

    public BaseFonts getBasefonts() {
	return basefonts;
    }

    public void setBasefonts(final BaseFonts basefonts) {
	this.basefonts = basefonts;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(final String title) {
	this.title = title;
    }

    public ProductForm getForm() {
	return Form;
    }

    public void setForm(final ProductForm form) {
	Form = form;
    }

    public ARegion getARegion(final String ID) {
	int max = region.size();
	for (int idx = 0; idx < max; idx++) {
	    ARegion reg = region.get(idx);
	    if (reg.getName().equals(ID)) {
		return reg;
	    }
	}
	return null;
    }
}
