import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;

import netscape.javascript.JSObject;

public class QueStamp extends Applet {

	/**
	 * 印影読込み
	 */
	private static final long serialVersionUID = 1L;
	public void init() {
		//印鑑番号取出し
		String para=getParameter("inkan_no");
		if (para.length()==0)
			return;	//印鑑番号指定無しは処理せず。
		try {
			Runtime rt = Runtime.getRuntime();
			byte[] buff= new byte[1024];
			String[] cmd={"",""};
			cmd[0]="C:/ALP/Inkan/Inkan.exe";
			cmd[1]=para;
			Process prs=rt.exec(cmd);
			InputStream in=prs.getInputStream();
			//結果取込み
			buff[0]='!';
			in.read(buff, 0, buff.length);
			prs.waitFor();
			Object arg[]=new Object[3];
			arg[2] = para;
			if (buff[0]=='!') {
				arg[0] = new String("non");
				arg[1] = new String("");
			}
			else {
				String str=new String(buff);
				System.out.println("印鑑{"+str+"}");
				//印影ビットマップを読込む。
				File inf = new File(str);
				FileInputStream fi = new FileInputStream(inf);
				int binarylength = (int)inf.length();
				byte[] indata = new byte[(int) inf.length()];
				fi.read(indata);
				fi.close();
				//読込んだ印影ビットマップをBase64変換する。
				int len = Base64enc.getencodelength(binarylength);
				byte[] bmpbyte = new byte[len];
				Base64enc.encode(indata, bmpbyte, binarylength);
				//Base64変換した印影ビットマップをhidden経由でサーバーへ送る。
				String basestr = new String(bmpbyte);
				arg[0] = str;
				arg[1] = basestr;
				System.out.println("ビットマップサイズ:"+len);
			}
			//結果を戻す。
			JSObject win = JSObject.getWindow(this);
			win.call("recvValue",arg);
			//String urlstr=getParameter("returnurl");
			//URL url=new URL(urlstr);
			//AppletContext cnt = getAppletContext();
			//cnt.showDocument(url, "_self");
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	public void paint(Graphics g) {
		Color c = new Color(128,128,255);
		setBackground(c);
	}
}
