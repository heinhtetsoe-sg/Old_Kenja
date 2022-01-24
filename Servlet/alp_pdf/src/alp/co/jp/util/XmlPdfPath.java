package alp.co.jp.util;

public class XmlPdfPath {

	private String BasePath;
	private String name;
	private String fieldxmlname;
	private String templatename;
	private String staticimagePath;
	private String fieldimagePath;
	private String pdffolder;

	public XmlPdfPath(String basePath) {
		if ((basePath.length() == 0)
				|| (basePath.charAt(basePath.length() - 1) == '/'))
			this.BasePath = basePath;
		else
			this.BasePath = basePath + "/";
		this.staticimagePath = basePath + "img/";
		this.fieldimagePath = this.staticimagePath;
		setPdffolder("");
	}

	public String getBasePath() {
	    return BasePath;
	}

	public void setName(String name) {
		this.name = name;
		this.fieldxmlname = name + ".fld";
		this.templatename = name + "_Template";
	}

	public String getXmlPath() {
		String Path = BasePath + "xml/" + name + ".xml";
		return Path;
	}

	public String getFieldxmlpath() {
		String Path = BasePath + "xml/" + fieldxmlname + ".xml";
		return Path;
	}

	public String getSvfPath() {
		String Path = BasePath + "svf/" + name + ".xml";
		return Path;
	}

	public void setFieldxmlname(String fieldxmlname) {
		if (fieldxmlname!=null)
			this.fieldxmlname = fieldxmlname;
	}

	public String getFieldxmlname() {
		return fieldxmlname;
	}

	public String getSerialPath() {
		String Path = BasePath + "serial/" + name + ".bin";
		return Path;
	}

	public String getSerialfldPath() {
		String Path = BasePath + "serial/" + fieldxmlname + ".bin";
		return Path;
	}

	public String getTemplateName() {
		return this.templatename;
	}

	public String getTemplatePath(String templatename) {
		String Path = BasePath + "template/" + templatename + ".pdf";
		return Path;
	}

	public String getPdfPath(String pdfname) {
		if (pdffolder.length()>0) {
			return this.pdffolder + pdfname + ".pdf";
		}
		else {
			return BasePath + "pdf/" + pdfname + ".pdf";
		}
	}

	public void setStaticimagePath(String imagePath) {
		if ((imagePath.length() == 0)
				|| (imagePath.charAt(imagePath.length() - 1) == '/'))
			this.staticimagePath = imagePath;
		else
			this.staticimagePath = imagePath + '/';
	}

	public String getStaticimagePath() {
		return this.staticimagePath;
	}

	public void setFieldimagePath(String imagePath) {
		if ((imagePath.length() == 0)
				|| (imagePath.charAt(imagePath.length() - 1) == '/'))
			this.fieldimagePath = imagePath;
		else
			this.fieldimagePath = imagePath + '/';
	}

	public String getFieldimagePath() {
		return this.fieldimagePath;
	}

	public void setPdffolder(String pdffolder) {
		if (pdffolder.length()>0) {
			if (pdffolder.charAt(pdffolder.length() - 1) == '/')
				this.pdffolder = pdffolder;
			else
				this.pdffolder = pdffolder + '/';
		}
		else
			this.pdffolder = "";
	}
}
