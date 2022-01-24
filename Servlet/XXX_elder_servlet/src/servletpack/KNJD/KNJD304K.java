package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３０４＞  欠席者一覧表（公欠・欠席者別）
 *
 *	2004/07/31 nakamoto 作成日
 *	2004/08/05 nakamoto 画面パラメータ(年組→学年)に変更。
 *	2004/08/11 nakamoto フォーム(年組番→年組と番)に変更。
 *	2004/09/06 nakamoto フォームＩＤを変更
 *	2004/09/08 nakamoto 学級が異なり同一出席番号の場合、線が引かれない不具合を修正
 *	2004/10/01 nakamoto・DB2のSQLにおいてINT関数の不具合を修正
 *	2004/10/20 nakamoto 出欠はkin_record_datにもたすような仕様に変更。この対応でattend_dat部分を修正。
 *	2004/11/01 nakamoto testitem_mstを参照しない（タイトルのテスト種別名を固定で入力）
 *	2005/02/05 nakamoto db2.commit追加。処理速度改善
 *  2005/02/18 yamashiro 欠席者を抽出する条件にkin_record_datの対象となる成績が入力されていないことを追加
 *                       異動者除外の処理追加
 ***********************************************************************************************
 *  2005/10/20 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 **/

public class KNJD304K {

    private static final Log log = LogFactory.getLog(KNJD304K.class);
	int len = 0;			//行数

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
			/*
				0	YEAR 			2002 
				1	GAKKI			1 
				2	TESTKINDCD		0101,0201 
					CLASS_SELECTED 	01P03 配列
					DBNAME 			KINDAI 
			*/

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[8];			//05/02/18Modify yamashiro

	//	パラメータの取得
		String classcd[] = request.getParameterValues("GRADE");   			//学年
		try {
	        param[0] = request.getParameter("YEAR");         						//年度
			param[1] = request.getParameter("GAKKI");   							//学期
			param[2] = request.getParameter("TESTKINDCD");   						//0101:中間/0201:期末
			param[7] = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );//異動基準日 05/02/18Modify yamashiro
		} catch( Exception ex ) {
			log.warn("parameter error!");
		}

	//	print設定
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
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2,svf,param);								//見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1(param));		//生徒毎科目数preparestatement
			ps2 = db2.prepareStatement(Pre_Stat2(param));		//公欠・欠席者preparestatement
		} catch( Exception ex ) {
			log.warn("DB2 open error!");
		}
		//SVF出力
		for( int ia=0 ; ia<classcd.length ; ia++ ){
			if( Set_Detail_1(db2,svf,param,classcd[ia],ps1,ps2) ){		//帳票出力のメソッド(生徒毎科目数)
				nonedata = true;
			}
		}

	//	該当データ無し
		if (!nonedata) {
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void Set_Head(DB2UDB db2, Vrw32alp svf, String param[]) {

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrSetForm("KNJD304.frm", 4);	//04/09/06	KIN12
		svf.VrsOut("NENDO"	,nao_package.KenjaProperties.gengou
													(Integer.parseInt(param[0])) + "年度");		//年度

	//	ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
		svf.VrAttribute("GRADE","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));		//作成日
		} catch( Exception e ){
			log.warn("ctrl_date get error!");
		}
	//	学期名称の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[1]);
			svf.VrsOut("SEMESTER"	,returnval.val1);	//学期名称
			param[3] = returnval.val2;						//学期開始日
			param[4] = returnval.val3;						//学期終了日
		} catch( Exception e ){
			log.warn("Semester name get error!");
		}
	//	テスト種別名の取得 04/11/01
		StringBuffer stb = new StringBuffer();
		if( param[2].equals("0101") )stb.append("中間テスト");
		else stb.append("期末テスト");
		svf.VrsOut("TESTNAME",stb.toString());		//成績種別
		getinfo = null;
		returnval = null;

		//各学期成績出欠情報 04/10/20Modify
		if(param[2].equals("0101")){					//中間
			param[5] = "SEM"+param[1]+"_INTER_REC_DI";
			param[6] = "SEM"+param[1]+"_INTER_REC";     //05/02/18Modify yamashiro
		} else {										//期末
			param[5] = "SEM"+param[1]+"_TERM_REC_DI";
			param[6] = "SEM"+param[1]+"_TERM_REC";      //05/02/18Modify yamashiro
		}

	}//Set_Head()の括り



	/**SVF-FORM**/
	private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[],String classcd
														,PreparedStatement ps1,PreparedStatement ps2)
	{
	    ResultSet rs = null;
		boolean nonedata = false;
		try {
			int pp = 0;
			ps1.setString(++pp,classcd);	//学年
			//ps1.setString(++pp,classcd);	//学年 04/10/20Modify
			rs = ps1.executeQuery();
			svf.VrsOut("GRADE",	classcd);							//学年（改ページ用）

			String schregno;
			int len2 = 0;
			int cnt_sub = 0;
			while( rs.next() ){
			//	行数をカウント
				len2 = len;								//カウント前の行数
				len++;
				cnt_sub = rs.getInt("CNT_SUB");			//科目数
				//８科目以上は次行に出力（行数カウント）
				while( cnt_sub > 8 ){
					len++;
					cnt_sub = cnt_sub - 8;
				}
				//行数が３０行超えたら改ページ（行数：初期値）
				if ( len > 30 ){
					for (int j=0; j<(30-len2); j++) svf.VrEndRecord();
					len = len - len2;
				}

				schregno = rs.getString("SCHREGNO");
				Set_Detail_2(db2,svf,param,schregno,ps2);					//公欠・欠席者出力のメソッド

				nonedata = true;
			}
		} catch (Exception ex) {
			log.warn("Set_Detail_1 read error!");
		} finally {
		    DbUtils.closeQuietly(rs);
		    db2.commit();//05.02.05
		}
		return nonedata;

	}//Set_Detail_1()の括り



	/**SVF-FORM**/
	private void Set_Detail_2(DB2UDB db2,Vrw32alp svf,String param[],String schregno,PreparedStatement ps2)
	{
	    ResultSet rs = null;
		try {
			int pp = 0;
			ps2.setString(++pp,schregno);	//学籍番号
			ps2.setString(++pp,schregno);	//学籍番号
			rs = ps2.executeQuery();

			int ia = 1;
			int msk_show = 0;		//04/09/08

			while( rs.next() ){

			//	８科目以上は次行に出力
				if ( ia > 8 ){
					if( msk_show > 0 ) svf.VrsOut("ATTENDNO"	,"" );		//04/09/08
					svf.VrEndRecord();
					Svf_Int(svf);							//SVFフィールド初期化
					ia = 1;
					msk_show = 1;
				}
				svf.VrsOut("HR_CLASS"	,rs.getString("HR_NAMEABBV") );	//年組
				svf.VrsOut("ATTENDNO"	,rs.getString("ATTENDNO") );	//番
				//出席番号用マスク	04/09/08
				svf.VrsOut("ATTEND_MSK"	,rs.getString("HR_NAMEABBV") + "-" + rs.getString("ATTENDNO") );
				svf.VrsOut("NAME"		,rs.getString("NAME") );									//氏名
				svf.VrsOut("ABSENT" 	+ String.valueOf(ia)	,rs.getString("DI_NAME") );			//欠席・公欠
				svf.VrsOut("SUBCLASS" + String.valueOf(ia)	,rs.getString("SUBCLASSABBV") );	//科目

				ia++;

			}
			if ( ia > 1 ){
				if( msk_show > 0 ) svf.VrsOut("ATTENDNO"	,"" );		//04/09/08
				svf.VrEndRecord();
				Svf_Int(svf);							//SVFフィールド初期化
			}
		} catch( Exception ex ) {
			log.warn("Set_Detail_2 read error!");
		} finally {
		    DbUtils.closeQuietly(rs);
		    db2.commit();
		}

	}//Set_Detail_2()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat1(String param[]){

	//	生徒毎科目数データ
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,HR_CLASS,ATTENDNO ");
            stb.append( prestateCommonRegd( param ) );                      //05/02/18Modify yamashiro
	        stb.append(     "), ");											//05/02/18Modify yamashiro

			stb.append("KIN_REC AS ( ");
			stb.append("    SELECT SCHREGNO,COUNT(SUBCLASSCD) CNT_SUB ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND "+param[5]+" in ('KS','KK') ");
			stb.append("           AND " + param[6] + " IS NULL ");         //05/02/18Modify yamashiro
			stb.append("    GROUP BY SCHREGNO ) ");

			stb.append("SELECT W1.SCHREGNO, W2.HR_CLASS, W2.ATTENDNO, W1.CNT_SUB ");
			stb.append("FROM   KIN_REC W1, SCHNO W2 ");
			stb.append("WHERE  W2.SCHREGNO=W1.SCHREGNO ");
			stb.append("ORDER BY W2.HR_CLASS,W2.ATTENDNO ");

		} catch( Exception e ){
			log.warn("Pre_Stat1 error!");
		}
		return stb.toString();

	}//Pre_Stat1()の括り



	/**PrepareStatement作成**/
	private String Pre_Stat2(String param[])
	{
	//	公欠・欠席者データ
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT ");
			stb.append(    "tbl2.schregno,tbl2.hr_nameabbv,tbl2.attendno, ");
			stb.append(    "tbl4.curriculum_cd,tbl4.subclasscd,tbl5.name,tbl1.di_name,tbl4.subclassabbv ");
			stb.append("FROM ");
			stb.append("    (SELECT ");
			stb.append("        case when "+param[5]+"='KS' then '欠'  ");
			stb.append("             when "+param[5]+"='KK' then '公' end di_name, ");
			stb.append("        schregno, classcd, school_kind, curriculum_cd, subclasscd ");
			stb.append("    FROM ");
			stb.append("        kin_record_dat ");
			stb.append("    WHERE ");
			stb.append("        year='"+param[0]+"' AND schregno =? AND ");
			stb.append("        "+param[5]+" in ('KS','KK') ");
			stb.append("        AND " + param[6] + " IS NULL ");                        //05/02/18Modify yamashiro
			stb.append("    ) tbl1, ");

			stb.append(    "(SELECT ");
			stb.append(        "w1.schregno,w1.attendno,w2.hr_nameabbv ");
			stb.append(    "FROM ");
			stb.append(        "schreg_regd_dat w1,schreg_regd_hdat w2 ");
			stb.append(    "WHERE ");
			stb.append(        "w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND ");
			stb.append(        "w1.schregno =? AND ");
			stb.append(        "w1.year=w2.year AND w1.semester=w2.semester AND ");
			stb.append(        "w1.grade = w2.grade AND w1.hr_class = w2.hr_class ");
			stb.append(    ") tbl2, ");
			stb.append(    "subclass_mst tbl4,schreg_base_mst tbl5 ");
			stb.append("WHERE ");
			stb.append(    "tbl1.schregno=tbl2.schregno AND ");
			stb.append(    "tbl1.schregno=tbl5.schregno AND ");
            stb.append(    "tbl1.classcd=tbl4.classcd AND ");
            stb.append(    "tbl1.school_kind=tbl4.school_kind AND ");
            stb.append(    "tbl1.curriculum_cd=tbl4.curriculum_cd AND ");
			stb.append(    "tbl1.subclasscd=tbl4.subclasscd ");
			stb.append("ORDER BY ");
			stb.append(    "tbl2.attendno,tbl4.curriculum_cd,tbl4.subclasscd ");
		} catch( Exception e ){
			log.warn("Pre_Stat2 error!");
		}
		return stb.toString();

	}//Pre_Stat2()の括り

	private void Svf_Int(Vrw32alp svf){

		svf.VrsOut("HR_CLASS" 	, "" );
		svf.VrsOut("ATTENDNO" 	, "" );
		svf.VrsOut("NAME" 		, "" );
		for (int j=1; j<9; j++){
			svf.VrsOut("ABSENT" 	+ String.valueOf(j) 	, "" );
			svf.VrsOut("SUBCLASS" + String.valueOf(j) 	, "" );
		}

	}//Svf_Int()の括り


	/** 
     *  SQLStatement作成 学籍の表 共通部品
     *  2005/02/18 yamashiro
     *    GRD_DIV       2:退学  3:転学
     *    TRANSFERCD    1:留学  2:休学  3出停:  4:編入
     *    異動の基準日は印刷指示画面で指定
     *  2005/10/20 nakamoto
	 *　転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
	 *　転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
	 *　留学(1)・休学(2)者
     */
	String prestateCommonRegd( String param[] ) {

        StringBuffer stb = new StringBuffer();
		try {
	        stb.append(	    "FROM   SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");//NO025
	        stb.append(		"WHERE  W1.YEAR = '" + param[0] + "' AND ");
	        stb.append(	  	       "W1.SEMESTER = '" + param[1] + "' AND ");
	        stb.append(            "W1.GRADE = ? AND ");
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");//NO025
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");//NO025
	        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
	        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                              "((S1.GRD_DIV IN ('2','3') AND ");
	        stb.append(                                "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END) OR ");
	        stb.append(                               "(S1.ENT_DIV IN ('4','5') AND ");
	        stb.append(                                "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END)) ) AND ");
	        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
	        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND ");
	        stb.append(                                "CASE WHEN T1.EDATE < '"+param[7]+"' THEN T1.EDATE ELSE '"+param[7]+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
		} catch( Exception e ){
			log.warn("Pre_Stat2 error!");
		}

        return stb.toString();
    }


}//クラスの括り
