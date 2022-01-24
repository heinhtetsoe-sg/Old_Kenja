package alp.co.jp.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;

public class DigitalSignDefine {

	private String certpath;
	private String password;
	private String inpdfpath;
	private String outpdfpath;
	private Rectangle rect;
	private int page;
	private String reason = "認証";
	private String location = "Tokyo";
	private String text = "";
	private int fontsize = 9;
	private String imagePath = "";

	public String getCertpath() {
		return certpath;
	}
	public void setCertpath(String certpath) {
		this.certpath = certpath;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getInpdfpath() {
		return inpdfpath;
	}
	public void setInpdfpath(String inpdfpath) {
		this.inpdfpath = inpdfpath;
	}
	public String getOutpdfpath() {
		return outpdfpath;
	}
	public void setOutpdfpath(String outpdfpath) {
		this.outpdfpath = outpdfpath;
	}
	public Rectangle getRectangle() {
		return rect;
	}
	public void setRectangle(float llx, float lly, float urx, float ury) {
		this.rect = new Rectangle(llx, lly, urx, ury);
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public boolean DigitalSign() {
		boolean ret = false;
		try {
			KeyStore ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(this.certpath), this.password.toCharArray());
			String alias = (String)ks.aliases().nextElement();
			PrivateKey key = (PrivateKey)ks.getKey(alias, this.password.toCharArray());
			Certificate[] chain = ks.getCertificateChain(alias);
			PdfReader reader = new PdfReader(this.inpdfpath);
			FileOutputStream fout = new FileOutputStream(this.outpdfpath);
			PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
			PdfSignatureAppearance sap = stp.getSignatureAppearance();
			sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
			sap.setReason(reason);
			sap.setLocation(location);
			if (text.length()>0) {
				BaseFont bf = BaseFont.createFont("HeiseiMin-W3","UniJIS-UCS2-HW-H",true);
				Font font = new Font(bf, fontsize, Font.BOLD);
				sap.setLayer2Font(font);
				sap.setLayer2Text(text);
			}
			if (imagePath.length()>0) {
				Image img = Image.getInstance(imagePath);
				sap.setImage(img);
			}
			Calendar dt = Calendar.getInstance();
			sap.setSignDate(dt);
			sap.setVisibleSignature(this.rect, this.page, null);
			stp.close();
			ret = true;

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return ret;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getFontsize() {
		return fontsize;
	}
	public void setFontsize(int fontsize) {
		this.fontsize = fontsize;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
