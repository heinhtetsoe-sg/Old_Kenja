package servletpack.KNJA;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import java.io.File;
//import java.util.Calendar;
//import java.text.SimpleDateFormat;
//import servletpack.KNJZ.detail.KNJ_Get_Info;
//import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ３１０＞  生徒名札（大宮開成）
 *
 *	2005/04/07 nakamoto 作成日
 **/

public class KNJA310 {

    private static final Log log = LogFactory.getLog(KNJA310.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[5];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("GAKKI");         				//学期
	        param[2] = request.getParameter("GRADE_HR_CLASS");         		//学年＋組
	        param[3] = request.getParameter("BUSUU");         			    //各生徒の出力枚数
			// 学籍番号の指定
			String schno[] = request.getParameterValues("category_selected");//学籍番号
			int i = 0;
			param[4] = "(";
			while(i < schno.length){
				if(schno[i] == null ) break;
				if(i > 0) param[4] = param[4] + ",";
				param[4] = param[4] + "'" + schno[i] + "'";
				i++;
			}
			param[4] = param[4] + ")";
		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		svf.VrInit();						   		//クラスの初期化
		svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//生徒情報
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if( setSvfout(db2,svf,param,ps1) ) nonedata = true;		//帳票出力のメソッド

log.debug("nonedata = "+nonedata);

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		preStatClose(ps1);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り

	/**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
	) {
		svf.VrSetForm("KNJA310.frm", 4);
	}


	/**帳票出力**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1
	) {
		boolean nonedata = false;
		try {
			ResultSet rs = ps1.executeQuery();

            int rec_cnt = 0;    //レコードカウント用
			while( rs.next() ){
                //各生徒の出力枚数分ループ
                for (int i = 0; i < Integer.parseInt(param[3]); i++) {
                    //１行出力(１行：３列)
    				if (rec_cnt == 3) {
                        svf.VrEndRecord();
                        rec_cnt = 0;    //初期化
                    }
                    //年組番・氏名
                    String hr_name = rs.getString("HR_NAME") + String.valueOf(rs.getInt("ATTENDNO")) + "番";
    				svf.VrsOut("HR_CLASS"  + String.valueOf(rec_cnt + 1)  ,hr_name );
    				svf.VrsOut("NAME"      + String.valueOf(rec_cnt + 1) + (getMS932ByteLength(rs.getString("NAME")) > 14 ? "_2" : ""),rs.getString("NAME") );

    				nonedata = true;
                    rec_cnt++;
                }
			}
            //最終行出力
			if (nonedata) svf.VrEndRecord();
			rs.close();
    		db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}



	/**生徒情報**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（学籍番号）
		try {
            //在籍
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
			stb.append("           SEMESTER='"+param[1]+"' AND ");
			stb.append("           SCHREGNO IN "+param[4]+" ) ");

            //メイン
			stb.append("SELECT T1.SCHREGNO ");
			stb.append("       ,T1.ATTENDNO ");
			stb.append("       ,T2.NAME ");
			stb.append("       ,T3.HR_NAME ");
   			stb.append("FROM   SCHNO T1 ");
   			stb.append("       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
   			stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND ");
   			stb.append("                                        T3.SEMESTER=T1.SEMESTER AND ");
   			stb.append("                                        T3.GRADE=T1.GRADE AND ");
   			stb.append("                                        T3.HR_CLASS=T1.HR_CLASS ");
			stb.append("ORDER BY T1.ATTENDNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り


	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1
	) {
		try {
			ps1.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



}//クラスの括り
