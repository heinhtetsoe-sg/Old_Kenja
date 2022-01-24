package alp.co.jp.util;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

public class CriptDefine {

	private String ownerPassword;
	private String userPassword;
	private boolean copy_able;
	private boolean print_able;

	public CriptDefine() {
		// TODO 自動生成されたコンストラクター・スタブ
		ownerPassword = "_AlpSetukei1472@2741IEKUTEsPLa_";
		userPassword = "";
		copy_able = false;
		print_able = false;
	}

	public String getOwnerPassword() {
		return ownerPassword;
	}

	public void setOwnerPassword(String ownerPassword) {
		this.ownerPassword = ownerPassword;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public boolean isCopy_able() {
		return copy_able;
	}

	public void setCopy_able(boolean copyAble) {
		copy_able = copyAble;
	}

	public boolean isPrint_able() {
		return print_able;
	}

	public void setPrint_able(boolean printAble) {
		print_able = printAble;
	}

	public boolean setEncCription(PdfWriter writer) {
		if (userPassword.length() <= 0)
			return false;
		try {
			if (copy_able) {	//メインメニューから名前をつけて保存が出来てしまうのであまり有効でない。
				if (print_able)
					writer.setEncryption(userPassword.getBytes(), ownerPassword.getBytes(),
							PdfWriter.ALLOW_COPY
									| PdfWriter.ALLOW_PRINTING,
							PdfWriter.STANDARD_ENCRYPTION_128);
				else
					writer.setEncryption(userPassword.getBytes(), ownerPassword.getBytes(),
							PdfWriter.ALLOW_COPY,
							PdfWriter.STANDARD_ENCRYPTION_128);
			} else {
				if (print_able)
					writer.setEncryption(userPassword.getBytes(), ownerPassword.getBytes(),
							PdfWriter.ALLOW_PRINTING,
							PdfWriter.STANDARD_ENCRYPTION_128);
				else
					writer.setEncryption(userPassword.getBytes(), ownerPassword.getBytes(),
							0,
							PdfWriter.STANDARD_ENCRYPTION_128);
			}
			return true;
		} catch (DocumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}
	}
}
