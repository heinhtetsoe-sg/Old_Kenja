package servletpack.KNJA;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import nao_package.svf.*;
import nao_package.db.*;
import java.sql.*;
import java.util.*;
import servletpack.KNJZ.detail.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ２３０Ｋ＞  講座別名列
 *
 *	2004/07/26 nakamoto 作成日
 *	2004/08/07 nakamoto パラメータ（適用開始日付）を追加
 *						担任名表示の正担任のみ表示の条件をカット（副担任も表示）
 *	2004/08/10 nakamoto 出力件数を追加。列は全て使用。自動改ページ。
 *	2004/09/06 nakamoto フォームＩＤを変更
 *	2006/05/25 o-naka NO001 氏名の前に女性は'*'を表示する(男:空白、女:'*')
 */

public class KNJA230K {

    private static final Log log = LogFactory.getLog(KNJA230K.class);
	int len = 0;			//列数カウント用

	/**
	 * HTTP Get リクエストの処理
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[11];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("SEMESTER");   							//学期
			param[2] = request.getParameter("ATTENDCLASSCD");   					//講座コード
			param[3] = request.getParameter("NAME_SHOW");   						//科目担任名（職員コード）
			param[4] = request.getParameter("CHARGEDIV");   						//担任区分1:正担任,0:副担任
			param[9] = request.getParameter("APPDATE");   							//適用開始日付 2004/08/07
			param[10] = request.getParameter("KENSUU");   							//出力件数
		} catch( Exception ex ) {
			log.error("parameter error!", ex);
		}

	//	print設定
//		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		svf.VrInit();						   	//クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!", ex);
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2,svf,param);								//見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//生徒preparestatement
			ps2 = db2.prepareStatement(Pre_Stat2(param));		//講座preparestatement
			ps3 = db2.prepareStatement(Pre_Stat3(param));		//担任preparestatement
		} catch( Exception ex ) {
			log.error("DB2 open error!", ex);
		}
		//SVF出力
		StringTokenizer stz1 = new StringTokenizer(param[2],",",false);		//講座コード
		StringTokenizer stz2 = new StringTokenizer(param[3],",",false);		//職員コード
		StringTokenizer stz3 = new StringTokenizer(param[4],",",false);		//担任区分
		StringTokenizer stz4 = new StringTokenizer(param[9],",",false);		//適用開始日付
		while (stz1.hasMoreTokens()){
			param[2] = stz1.nextToken();	//講座コード
			param[3] = stz2.nextToken();	//職員コード
			param[4] = stz3.nextToken();	//担任区分
			param[9] = stz4.nextToken();	//適用開始日付
			Set_Detail_2(db2,svf,param,ps2);							//講座出力のメソッド
			Set_Detail_3(db2,svf,param,ps3);							//担任出力のメソッド
			for( int ib=0 ; ib<Integer.parseInt(param[10]) ; ib++ )
				if( Set_Detail_1(db2,svf,param,ps1) )nonedata = true;		//生徒出力のメソッド
		}
		if( nonedata )	svf.VrEndPage();					//SVFフィールド出力

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		Pre_Stat_f(ps1,ps2,ps3);	//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrSetForm("KNJA230K.frm", 1);		//04/09/06	KIN25_3

	//	ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
		//svf.VrAttribute("SUBCLASS1","FF=1");

	//	学期開始日・学期終了日の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[1]);
			param[5] = returnval.val2;						//学期開始日
			param[6] = returnval.val3;						//学期終了日
		} catch( Exception e ){
			log.error("Semester date get error!", e);
		}
		getinfo = null;
		returnval = null;

	}//Set_Head()の括り



	/**SVF-FORM**/
	private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps1)
	{
		boolean nonedata = false;
		try {
			int pp = 0;
			ps1.setString(++pp,param[2]);	//講座コード
			ps1.setString(++pp,param[9]);	//適用開始日付 2004/08/07
			ResultSet rs = ps1.executeQuery();
			//log.debug("Set_Detail_1 sql ok!");

			String strx;
			String stry;
			String strz;
			int x;					//姓の文字数
			int y;					//名の文字数
			int z;					//空白文字の位置
			int gyo = 1;			//行数カウント用
			//int len = 1;			//列数カウント用
			int ban = 1;			//連番
			len++;

			while( rs.next() ){
				if ( gyo > 50 ){
					gyo = 1;
					len++;
				}
				if ( len > 2 ){
					len = 1;
					svf.VrEndPage();					//SVFフィールド出力
				}
			//	講座名・担任名出力
				svf.VrsOut("SUBCLASS" + String.valueOf(len) 	, param[7] );
				svf.VrsOut("STAFFNAME" + String.valueOf(len) 	, param[8] );

			//	連番・組略称・出席番号・かな出力
				svf.VrsOutn("NUMBER" + String.valueOf(len) 	,gyo, String.valueOf(ban) );
				svf.VrsOutn("HR_CLASS" + String.valueOf(len) 	,gyo, rs.getString("HR_NAMEABBV") );
				svf.VrsOutn("ATTENDNO" + String.valueOf(len) 	,gyo, rs.getString("ATTENDNO") );
				svf.VrsOutn("KANA" + String.valueOf(len) 		,gyo, rs.getString("NAME_KANA") );
				svf.VrsOutn("MARK" + String.valueOf(len) 		,gyo, rs.getString("SEX") );//NO001 男:空白、女:'*'

			//	生徒漢字・規則に従って出力
				strz = rs.getString("NAME");
				z = strz.indexOf("　");
				if ( z < 0 ){
					svf.VrsOutn("NAME" + String.valueOf(len) 	,gyo, strz );					//空白がない
				} else {
					strx = strz.substring(0,z);
					stry = strz.substring(z+1);
					x = strx.length();
					y = stry.length();
					if ( x == 1 ){
						svf.VrsOutn("LNAME" + String.valueOf(len) + "_2" 	,gyo, strx );		//姓１文字
					} else {
						svf.VrsOutn("LNAME" + String.valueOf(len) + "_1" 	,gyo, strx );		//姓２文字以上
					}
					if ( y == 1 ){
						svf.VrsOutn("FNAME" + String.valueOf(len) + "_2" 	,gyo, stry );		//名１文字
					} else {
						svf.VrsOutn("FNAME" + String.valueOf(len) + "_1" 	,gyo, stry );		//名２文字以上
					}
				}

				//svf.VrEndRecord();
				nonedata = true;
				gyo++;			//行数カウント用
				ban++;			//連番
			}
			rs.close();
			//if( nonedata )	svf.VrEndPage();					//SVFフィールド出力
    	    //log.debug("Set_Detail_1 read ok!");
		} catch( Exception ex ) {
			log.error("Set_Detail_1 read error!", ex);
		}
		return nonedata;

	}//Set_Detail_1()の括り



	/**SVF-FORM**/
	private void Set_Detail_2(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps2)
	{
		try {
			int pp = 0;
			ps2.setString(++pp,param[2]);	//講座コード
			ResultSet rs = ps2.executeQuery();
			//log.debug("Set_Detail_2 sql ok!");

			if( rs.next() ){
				param[7] = rs.getString("CHAIRNAME");		//講座名称
			} else {
				param[7] = "";
			}
			rs.close();
    	    //log.debug("Set_Detail_2 read ok!");
		} catch( Exception ex ) {
			log.error("Set_Detail_2 read error!", ex);
		}

	}//Set_Detail_2()の括り



	/**SVF-FORM**/
	private void Set_Detail_3(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps3)
	{
		try {
			int pp = 0;
			ps3.setString(++pp,param[3]);	//職員コード
			ResultSet rs = ps3.executeQuery();
			//log.debug("Set_Detail_2 sql ok!");

			if( rs.next() ){
				param[8] = rs.getString("STAFFNAME");		//職員名称
			} else {
				param[8] = "";
			}
			//if( !param[4].equals("1") ) param[8] = "";		//担任区分 2004/08/07
			rs.close();
    	    //log.debug("Set_Detail_3 read ok!");
		} catch( Exception ex ) {
			log.error("Set_Detail_3 read error!", ex);
		}

	}//Set_Detail_3()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT ");
			stb.append("    T1.SCHREGNO, ");
			stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");//NO001 男:空白、女:'*'
			stb.append("    value(T1.NAME,'') NAME, ");
			stb.append("    value(T1.NAME_KANA,'') NAME_KANA, ");
			stb.append("    value(T6.HR_NAMEABBV,'') HR_NAMEABBV, ");
			stb.append("    value(T2.GRADE,'') GRADE, ");
			stb.append("    value(T2.HR_CLASS,'') HR_CLASS, ");
			stb.append("    value(T2.ATTENDNO,'') ATTENDNO ");
			stb.append("FROM ");
			stb.append("    CHAIR_STD_DAT T7, ");
			stb.append("    SCHREG_BASE_MST T1, ");
			stb.append("    SCHREG_REGD_DAT T2, ");
			stb.append("    SCHREG_REGD_HDAT T6 ");
			stb.append("WHERE ");
			stb.append("    T7.YEAR = '"+param[0]+"' AND ");
			stb.append("    T7.SEMESTER = '"+param[1]+"' AND ");
			stb.append("    T7.CHAIRCD =? AND ");
			//2004/08/07
			stb.append("    T7.APPDATE =? AND ");
			//stb.append("    T7.APPDATE BETWEEN '"+param[5]+"' AND '"+param[6]+"' AND ");
			//stb.append("    (T7.APPDATE,T7.SCHREGNO) IN  ");
			//stb.append("    (SELECT MAX(APPDATE) APPDATE,SCHREGNO  ");
			//stb.append("     FROM CHAIR_STD_DAT ");
			//stb.append("     WHERE YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND CHAIRCD=?  ");
			//stb.append("     GROUP BY SCHREGNO) AND ");
			stb.append("    T1.SCHREGNO = T7.SCHREGNO AND ");
			stb.append("    T2.SCHREGNO = T7.SCHREGNO AND ");
			stb.append("    T2.YEAR = T7.YEAR AND ");
			stb.append("    T2.SEMESTER = T7.SEMESTER AND ");
			stb.append("    T6.YEAR = T2.YEAR AND ");
			stb.append("    T6.SEMESTER = T2.SEMESTER AND ");
			stb.append("    T6.GRADE = T2.GRADE AND ");
			stb.append("    T6.HR_CLASS = T2.HR_CLASS ");
			stb.append("ORDER BY ");
			stb.append("    T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ");
		} catch( Exception e ){
			log.error("Pre_Stat1 error!", e);
		}
		return stb.toString();

	}//Pre_Stat1()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT value(CHAIRNAME,'') CHAIRNAME FROM CHAIR_DAT ");
			stb.append("WHERE YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND CHAIRCD=? ");
		} catch( Exception e ){
			log.error("Pre_Stat2 error!", e);
		}
		return stb.toString();

	}//Pre_Stat2()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT value(STAFFNAME,'') STAFFNAME FROM STAFF_MST WHERE STAFFCD=? ");
		} catch( Exception e ){
			log.error("Pre_Stat3 error!", e);
		}
		return stb.toString();

	}//Pre_Stat3()の括り



	/**PrepareStatement close**/
	private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3)
	{
		try {
			ps1.close();
			ps2.close();
			ps3.close();
		} catch( Exception e ){
			log.error("Pre_Stat_f error!", e);
		}
	}//Pre_Stat_f()の括り



}//クラスの括り
