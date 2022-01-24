package alp.co.jp.util;

import com.lowagie.text.Document;

public class MetaDefine {

	private String title;
	private String subject;
	private String keywords;
	private String creator;
	private String author;

	public MetaDefine(String title, String subject, String keywords,
			String creator, String author) {
		super();
		this.title = title;
		this.subject = subject;
		this.keywords = keywords;
		this.creator = creator;
		this.author = author;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}

	public void MataData(Document document) {
		if (this.title.length()>0) {
			document.addTitle(this.title);
		}
		if (this.subject.length()>0) {
			document.addSubject(this.subject);
		}
		if (this.keywords.length()>0) {
			document.addKeywords(this.keywords);
		}
		if (this.creator.length()>0) {
			document.addCreator(this.creator);
		}
		if (this.author.length()>0) {
			document.addAuthor(this.author);
		}
	}
}
