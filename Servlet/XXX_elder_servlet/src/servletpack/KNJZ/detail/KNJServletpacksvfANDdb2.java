// kanji=漢字
/*
 * $Id: b9c6194718e0cd3218021bb9ff0181b0c98ec666 $
 *
 * 作成日: 2005/06/22 21:10:29 - JST
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
*
*  帳票におけるＳＶＦおよびＤＢ２の設定
*  2005/06/22 yamashiro
*
*/

public class KNJServletpacksvfANDdb2 {

    private static final Log log = LogFactory.getLog(KNJServletpacksvfANDdb2.class);
	public String printname;
    public PrintWriter outstrm;


	/** print設定 */
    public void setSvfInit( HttpServletRequest request, HttpServletResponse response ,Vrw32alp svf )
	{
		int ret = 0;
		try {
		    printname = request.getParameter("PRINTNAME");   			    //プリンタ名
            outstrm = new PrintWriter (response.getOutputStream());
            if( printname!=null )	response.setContentType("text/html");
            else					response.setContentType("application/pdf");

            ret = svf.VrInit();						   		//クラスの初期化

            if( printname!=null ){
                ret = svf.VrSetPrinter("", printname);			//プリンタ名の設定
                if( ret < 0 ) log.info("printname ret = " + ret);
            } else
                ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.error("db new error:" + ex);
		}
  }


	/** svf close */
    public void closeSvf( Vrw32alp svf, boolean nonedata )
	{
		int ret = 0;
		if( printname!=null ){
			outstrm.println("<HTML>");
			outstrm.println("<HEAD>");
			outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
			outstrm.println("</HEAD>");
			outstrm.println("<BODY>");
			if( !nonedata )	outstrm.println("<H1>対象データはありません。</h1>");
			else			outstrm.println("<H1>印刷しました。</h1>");
			outstrm.println("</BODY>");
			outstrm.println("</HTML>");
		} else if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}
		ret = svf.VrQuit();
		if( ret == 0 )log.info("===> VrQuit():" + ret);
		outstrm.close();			//ストリームを閉じる 
    }


	/** DB set */
	public DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			log.error("db new error:" + ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/** DB open */
	public boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!"+ex );
			return true;
		}//try-cathの括り
		return false;
	}


	/** DB close */
	public void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}//try-cathの括り
	}


}
