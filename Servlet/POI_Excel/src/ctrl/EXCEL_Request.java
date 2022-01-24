package ctrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import util.Alp_Properties;
import util.en_cipher;

/**
 * Servlet implementation class EXCEL_Request
 */
@WebServlet("/EXCEL_Request")
public class EXCEL_Request extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EXCEL_Request.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EXCEL_Request() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		String URL = "";
		// パラメータ取出し
		// プログラムID取出し
		String programID = request.getParameter("programID");
		// pdf関数呼び出し時のパラメータ取出し
		String Information = request.getParameter("Information");
		// トークン取出し
		String token = request.getParameter("token");
		if (programID == null) {
			return;
		}
		log.info("ID:" + programID + " Info:" + Information + " Token:" + token);
		// サーブレットのコンテキストを取出しておく。
		ServletContext contxt = getServletConfig().getServletContext();
		// 定義ファイル読込みオブジェクトを生成
		Alp_Properties prop = new Alp_Properties(contxt);
		// 定義パスワードを取出す。
		String password = prop.getProperty("excel_password");
		final int sha1length = 40; // 16進表記の長さ: 40文字
		int randomlength = token.length() > sha1length ? token.length()
				- sha1length : 0;
		String random = randomlength > 0 ? token.substring(0, randomlength)
				: "";
		String anser = token.substring(randomlength);
		// ハッシュ値算出
		String enc = en_cipher.encryptStr(random + password);
		// エラー発生時の自動閉じる時間
		String errortime = prop.getProperty("autoClose_error");
		// パスワード判定
		if ((random.length() == 0) || (enc.equals(anser) == false)) {
			String msg = "Excel作成要求パスワードエラー";
			URL = String.format(
					"view/Page_Close.jsp?pdfpath=%s&message=%s&timevalue=%s",
					programID, msg, errortime);
			log.info(msg);
			request.getRequestDispatcher(URL).forward(request, response);
			return;
		}
		//
		String errorMessage = "";
		// プログラム名とフォーム名を取出す
		String ctrlName = ""; // プログラム名
		String formName = ""; // フォーム名
		String programInfo = prop.getProperty(programID);
		if ((programInfo != null) && (programInfo.length() > 0)) {
			String[] propcols = programInfo.split(",", 0);
			if (propcols.length >= 2) {
				// プログラム名
				ctrlName = propcols[0];
				// フォーム名
				formName = propcols[1];
			}
		}
		// 取出しOKか判定
		if ((ctrlName.length() == 0) || (formName.length() == 0)) {
			// エラー時はエラーメッセージを出してブラウザウインドゥを閉じる
			String msg = "Excel作成要求プログラムIDエラー";
			URL = String.format(
					"view/Page_Close.jsp?pdfpath=%s&message=%s&timevalue=%s",
					programID, msg, errortime);
			log.info(msg);
			request.getRequestDispatcher(URL).forward(request, response);
			return;
		}
		// 作業元フォルダを取出す
		String basefolder = prop.getProperty("basefolder");
		if ((basefolder == null) || (basefolder.length() == 0))
			basefolder = contxt.getRealPath("/");
		// テンプレートExcel有無判定
		String inline = formName + ".xlsx";
		String TempletPath = basefolder + "/Templet/" + inline;
		File excelFile = new File(TempletPath);
		if (excelFile.exists() == false) {
			String msg = "テンプレートExcelファイルが有りません";
			URL = String.format(
					"view/Page_Close.jsp?pdfpath=%s&message=%s&timevalue=%s",
					programID, msg, errortime);
			log.info(msg);
			request.getRequestDispatcher(URL).forward(request, response);
			return;
		}
		log.info("Excel作成{作業元フォルダ:" + basefolder + "}{プログラム名:" + ctrlName
				+ "}{フォーム名:" + formName + "}{プログラムID:" + programID + "}");
		// Excel作成クラス名決定
		Boolean ret = false;
		final Class<?> clazz;
		String mkpdfName = "mkexcel." + ctrlName;
		Workbook book = null;
		try {
			book = WorkbookFactory.create(new FileInputStream(excelFile));
			clazz = Class.forName(mkpdfName);
			log.info("Class:" + clazz.getName());
			// Excel作成
			// コンストラクタを指定してインスタンス作成
			Object instance = clazz.getConstructor(Workbook.class,
					String.class, Alp_Properties.class).newInstance(book,
					Information, prop);
			if (instance == null) {
				errorMessage = "インスタンス作成失敗";
				log.error(errorMessage);
			} else {
				log.info("インスタンス作成成功");
				errorMessage = (String) clazz.getMethod("getErrorMessage")
						.invoke(instance);
				if (errorMessage.isEmpty()) {	//コンストラクタ呼出しOK
					// Excel作成
					ret = (Boolean) clazz.getMethod("excel").invoke(instance);
					log.info("Excel作成:" + ret);
					if (ret == false) {
						errorMessage = (String) clazz.getMethod(
								"getErrorMessage").invoke(instance);
						if (errorMessage.isEmpty()) {
							errorMessage = "出力ページなし。";
						}
					}
				}
				else {	//コンストラクタでエラー発生
					log.error(errorMessage);
					ret = false;
				}
			}
			// 送信
			if ((book != null)&&(ret == true)) {
				log.info("名前：" + inline);
				response.setHeader("Content-Disposition",inline);
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				book.write(response.getOutputStream());
			}
		} catch (ClassNotFoundException e) {
			StackTrace(e);
			errorMessage = "ClassNotFoundException " + e.getMessage();
			log.error(errorMessage);
		} catch (InstantiationException e) {
			// TODO 自動生成された catch ブロック
			StackTrace(e);
			errorMessage = "InstantiationException " + e.getMessage();
			log.error(errorMessage);
		} catch (IllegalAccessException e) {
			// TODO 自動生成された catch ブロック
			StackTrace(e);
			errorMessage = "IllegalAccessException " + e.getMessage();
			log.error(errorMessage);
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			StackTrace(e);
			errorMessage = "IllegalArgumentException " + e.getMessage();
			log.error(errorMessage);
		} catch (InvocationTargetException e) {
			// TODO 自動生成された catch ブロック
			StackTrace(e);
			errorMessage = "InvocationTargetException " + e.getMessage();
			log.error(errorMessage);
		} catch (NoSuchMethodException e) {
			// TODO 自動生成された catch ブロック
			StackTrace(e);
			errorMessage = "NoSuchMethodException " + e.getMessage();
			log.error(errorMessage);
		} catch (SecurityException e) {
			// TODO 自動生成された catch ブロック
			StackTrace(e);
			errorMessage = "SecurityException " + e.getMessage();
			log.error(errorMessage);
		} finally {
			if (book != null) {
				book.close();
			}
			// PDF作成エラー処理
			if (ret == false) {
				// PDF作成エラーPDFをブラウザへ出力
				URL = String.format(
						"view/Page_Close.jsp?pdfpath=%s&message=%s&timevalue=%s",
						programID, errorMessage, errortime);
				request.getRequestDispatcher(URL).forward(request, response);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private void StackTrace(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter  pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.flush();
		String trace = sw.toString();
		log.info(trace);
	}
}
