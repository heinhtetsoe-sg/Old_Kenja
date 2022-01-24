/**
 * [成績管理] ＜ＫＮＪＤ３０１Ｋ＞  クラブ別報告書（中学・高校）
 *
 * 2005/06/07 nakamoto 新規作成
 * 2005/06/13 nakamoto ３学期平均は印字しない。
 * 2005/07/01 nakamoto 中学・高校共通に修正。
 * 2005/07/04 nakamoto 国語総合について、学年成績・評定のみ出力。また、読替元は、評定は出力しない。
 * 2005/07/06 nakamoto 中間試験を欠課しているが期末試験は受験している生徒についても、学期成績を計算してしまっている不具合を修正。
 * 2005/07/12 nakamoto テーブル名変更による修正(COURSE_GROUP⇒COURSE_GROUP_DAT)---NO022
 *
 *************************
 *
 * 2005/10/13 nakamoto （中学）席次は、テーブル(RECORD_RANK_DAT)から参照するように修正---NO024
 *            nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD301K {

	private static final Log log = LogFactory.getLog(KNJD301K.class);
	private String printname;   			//プリンタ名
	private String schno[];	                //学籍番号
    private PrintWriter outstrm;
	private DecimalFormat dmf1 = new DecimalFormat("#");
	private DecimalFormat dmf2 = new DecimalFormat("0.0");
    private String _sql = null;
	private String _useCurriculumcd = "1";

	/**
	  *
	  *  KNJD.classから最初に起動されるクラス
	  *
	  **/
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		String param[] = new String[8];
		boolean nonedata = false;

	// パラメータの取得
	    getParam( request, param );

	// print svf設定
		setSvfInit( response, svf );

	// ＤＢ接続
		db2 = setDb(request);
		if( openDb(db2) ){
			log.error("db open error");
			return;
		}

	// 印刷処理
		nonedata = printSvf( request, db2, svf, param );

	// 終了処理
		closeSvf( svf, nonedata );
		closeDb( db2 );

	}	//doGetの括り


    /**
     *  印刷処理
     */
    private boolean printSvf( HttpServletRequest request, DB2UDB db2, Vrw32alp svf, String param[] )
	{
		boolean nonedata = false;
		try {
			setHead( db2, svf, param );			//見出し項目
for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("param[" + i + "]=" + param[i]);
			if( printSvfMain( db2, svf, param ) )nonedata = true;		//SVF-FORM出力処理
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
		return nonedata;
    }


	/** 
     *  SVF-FORMセット＆見出し項目（全体）
     */
	private void setHead(DB2UDB db2,Vrw32alp svf,String param[])
	{
		svf.VrSetForm("KNJD301.frm", 4);			    //SVF-FORM
		svf.VrAttribute("CLUBNAME","FF=1");			//ＳＶＦ属性変更--->改ページ（クラブ）

		param[4] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度"; //年度

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		try {
    		returnval = getinfo.Control(db2);
	    	param[5] = KNJ_EditDate.h_format_JP(returnval.val3);    //作成日
		} catch( Exception ex ){
			log.error("setHead get control_date error! ", ex);
		}

		try {
    		returnval = getinfo.Semester(db2,param[0],param[1]);
    		param[6] = returnval.val1;                              //学期名
		} catch( Exception ex ){
			log.error("setHead get semestername error! ", ex);
		}
		getinfo = null;
		returnval = null;
	}


	/** 
     *
     * SVF-OUT 印刷処理
     */
	private boolean printSvfMain( DB2UDB db2, Vrw32alp svf, String param[] )
	{
		boolean nonedata = false;
		PreparedStatement arrps[] = new PreparedStatement[1];

		try {
			if (param[7].equals("H")) _sql = prestatementSubclass(param);   //高校:成績明細データ
			if (param[7].equals("J")) _sql = prestatementSubclassJ(param);  //中学:成績明細データ
//			log.debug(_sql);
			arrps[0] = db2.prepareStatement(_sql);
		} catch( Exception ex ) {
			log.error("boolean printSvfMain prepareStatement error! " + ex);
			return nonedata;
		}

		try {
			if( printSvfMainHrclass( db2, svf, param, arrps ) )nonedata = true;
		} catch( Exception ex ) {
			log.error("boolean printSvfMain printSvfMainHrclass() error! " + ex);
		}

		prestatementClose( arrps );	//preparestatementを閉じる

		return nonedata;

	}//boolean printSvfMain()の括り


	/** 
     *
     * SVF-OUT 学級単位の印刷処理
     *    Stirng param[]について ==> param[2]:学年・組 param[5]:該当生徒の学籍番号
     */
	private boolean printSvfMainHrclass( DB2UDB db2, Vrw32alp svf, String param[], PreparedStatement arrps[] )
	{
		boolean nonedata = false;
		ResultSet arrrs[] = new ResultSet[1];

		try {
log.debug("executeQuery start!");
			arrrs[0] = arrps[0].executeQuery();                  		//成績明細データのResultSet
log.debug("executeQuery end!");
		} catch( SQLException ex ) {
			log.error("boolean printSvfMainHrclass executeQuery error! "+ex);
			return nonedata;
		}

		try {
			String arrschno[] = {"0","0"};  //成績明細データの保管 ＝＞ {arrschno[0]:学籍番号,arrschno[1]:科目コード}
            int subno = 1;                  //科目列No

			while( arrrs[0].next() )
			{
                //初回を除いて学籍番号のブレイク時印刷！
				if( !arrschno[0].equals("0") && 
                    !arrschno[0].equals(arrrs[0].getString("SCHREGNO")) )
                {
                    svf.VrEndRecord();
                    arrschno[1] = "0";          //初期化(科目コード)
                    subno = 1;                  //初期化(科目列No)
                }

                //各生徒の初回を除いた科目コードのブレイク時(印字列指定)
				if( !arrschno[1].equals("0") && 
                    !arrschno[1].equals(arrrs[0].getString("SUBCLASSCD")) ) subno++;

				arrschno[0] = arrrs[0].getString("SCHREGNO");       //成績明細の学籍番号を保存
				arrschno[1] = arrrs[0].getString("SUBCLASSCD");     //成績明細の科目コードを保存
				printSvfRegdOut( svf, arrrs[0], param, subno );	    //学籍データ印刷
				printSvfRecDetailOut( svf, arrrs[0], param, subno );//成績明細データ印刷

				nonedata = true;
			}

			if( !arrschno[0].equals("0") ) svf.VrEndRecord();   //最終生徒印刷！
		} catch( Exception ex ) { log.error("printSvfMainHrclass read error! "+ex);	}

		try {
			for( int i = 0 ; i < arrrs.length ; i++ ) if( arrrs[i] != null ) arrrs[i].close();
		} catch( SQLException ex ) { log.error("printSvfMainHrclass rs.close() error!  "+ex); }

		return nonedata;
	}


	/** 
     * SVF-OUT 学籍
     */
	private void printSvfRegdOut( Vrw32alp svf, ResultSet rs, String param[], int subno ){

		try {
			svf.VrsOut("NENDO",     param[4]);								                    //年度
			svf.VrsOut("SEMESTER",  param[6]);								                    //学期
            svf.VrsOut("DATE",      param[5]);							                        //処理日
			svf.VrsOut("CLUBNAME",  rs.getString("CLUBNAME"));					                //クラブ名
			svf.VrsOut("NAME",      rs.getString("NAME"));					                    //生徒名
			svf.VrsOut("HR_NAME",   rs.getString("HR_NAMEABBV") + "-" + 
                                          String.valueOf(rs.getInt("ATTENDNO")) );					    //年組番

			svf.VrsOut("SUBCLASS" + String.valueOf( subno ),  rs.getString("SUBABBV"));			//科目名

            //テスト種別---2005.06.13Add
			svf.VrsOutn("ITEMNAME",   1,  "１学期中間");
			svf.VrsOutn("ITEMNAME",   2,  "１学期期末");
			svf.VrsOutn("ITEMNAME",   3,  "１学期成績");
			svf.VrsOutn("ITEMNAME",   4,  "２学期中間");
			svf.VrsOutn("ITEMNAME",   5,  "２学期期末");
			svf.VrsOutn("ITEMNAME",   6,  "２学期成績");
			svf.VrsOutn("ITEMNAME",   7,  "３学期期末");
			svf.VrsOutn("ITEMNAME",   8,  (param[7].equals("H")) ? "" : "３学期成績");
			svf.VrsOutn("ITEMNAME",   9,  "学年成績");

            //項目名(合計・平均・席次)---2005.07.01Add
    		svf.VrAttribute("ITEM_TOTAL"  ,"ZenFont=1");			//ＳＶＦ属性変更--->ゴシック
    		svf.VrAttribute("ITEM_AVERAGE","ZenFont=1");			//ＳＶＦ属性変更--->ゴシック
    		svf.VrAttribute("ITEM_ORDER"  ,"ZenFont=1");			//ＳＶＦ属性変更--->ゴシック
			svf.VrsOut("ITEM_TOTAL"   ,  (param[7].equals("H")) ? "合計" : "９合計");
			svf.VrsOut("ITEM_AVERAGE" ,  (param[7].equals("H")) ? "平均" : "９平均");
			svf.VrsOut("ITEM_ORDER"   ,  (param[7].equals("H")) ? "席次" : "９席次");
            if (param[7].equals("J")) {
        		svf.VrAttribute("SUBCLASS16"  ,"ZenFont=1");			//ＳＶＦ属性変更--->ゴシック
        		svf.VrAttribute("SUBCLASS17"  ,"ZenFont=1");			//ＳＶＦ属性変更--->ゴシック
        		svf.VrAttribute("SUBCLASS18"  ,"ZenFont=1");			//ＳＶＦ属性変更--->ゴシック
    			svf.VrsOut("SUBCLASS16"   ,  "５合計");
    			svf.VrsOut("SUBCLASS17"   ,  "５平均");
    			svf.VrsOut("SUBCLASS18"   ,  "５席次");
            }

		} catch( SQLException ex ){
			log.error("[KNJD171K]printSvfRegdOut error!",ex);
		}

	}//printSvfRegdOut()の括り


	/** 
     * SVF-OUT 成績明細印刷
     */
	private void printSvfRecDetailOut( Vrw32alp svf, ResultSet rs, String param[], int subno ){

		try {
            svf.VrsOutn( getSvfOutField( rs.getString("SUBCLASSCD"), subno ),
                               getSvfOutPoint( Integer.parseInt( rs.getString("SEMESTER") ), Integer.parseInt( rs.getString("KIND") ) ),
                               getOutputDat( rs , param ) );			//得点
			if( rs.getString("SEMESTER").equals("3")  &&  Integer.parseInt( rs.getString("KIND") ) == 3  &&  rs.getString("ASSESS") != null )
				svf.VrsOut( "RATING" + String.valueOf( subno ),  rs.getString("ASSESS") );//学年評定

		} catch( SQLException ex ){	log.error("error! "+ex ); }

	}//printSvfRecDetailOut()の括り


	/**
     *  学期・成績種別から出力行を設定する
     */
	private int getSvfOutPoint( int semes, int kind ){

        int point = 0;

		if( semes == 1 )
			point = ( kind == 1 )? 1 : ( kind == 2 )? 2 : 3;
		else if( semes == 2 )
			point = ( kind == 1 )? 4 : ( kind == 2 )? 5 : 6;
		else
			point = ( kind == 1 )? 7 : ( kind == 2 )? 8 : 9;

		return point;
	}


	/**
     *  科目コード等から出力フィールドを設定する
     */
	private String getSvfOutField( String subclasscd, int subno ){

        String field = "";

		field = ( subclasscd.equals("X9G") )? "TOTAL" :
				( subclasscd.equals("X9H") )? "AVERAGE" :
				( subclasscd.equals("X9R") )? "ORDER" :
				( subclasscd.equals("X5G") )? "POINT16" :
				( subclasscd.equals("X5H") )? "POINT17" :
				( subclasscd.equals("X5R") )? "POINT18" :
				"POINT" + String.valueOf( subno );

		return field;
	}


	/** 
     *
     * SVF-FORM 生徒成績データをセット
     */
	private String getOutputDat( ResultSet rs, String param[] )
	{
		String retval = null;
		try {
			if( rs.getString("SCORE") != null ){
    			if( param[7].equals("J") && 
                    ( (rs.getString("SUBCLASSCD")).equals("X5H") || 
                      (rs.getString("SUBCLASSCD")).equals("X9H") ) )
    			    retval = String.valueOf( dmf2.format( Float.parseFloat(rs.getString("SCORE")) ) );
                else
    			    retval = String.valueOf( dmf1.format( Float.parseFloat(rs.getString("SCORE")) ) );
            }

		} catch( Exception ex ){
			log.error("getOutputDate error! ",ex);
		}
		if( retval == null ) retval = "";
		return retval;
	}


	/**PrepareStatement作成**/
	private String prestatementSubclass(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//学籍クラブの表
			stb.append("WITH CLUB_HIST AS ( ");
			stb.append("    SELECT SCHREGNO, CLUBCD ");
			stb.append("    FROM   SCHREG_CLUB_HIST_DAT ");
			stb.append("    WHERE  CLUBCD IN "+param[2]+" AND ");
			stb.append("         ((EDATE IS NULL AND SDATE <= '"+param[3]+"') OR ");
			stb.append("          (EDATE IS NOT NULL AND SDATE <= '"+param[3]+"' AND '"+param[3]+"' <= EDATE)) ");
			stb.append("    ) ");
			//学籍の表（年度・学期）
			stb.append(",SCHNO_C AS ( ");
			stb.append("    SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
			stb.append("           T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD,T1.COURSECODE ");
			stb.append("    FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ");
			stb.append("    WHERE  T1.YEAR = '"+param[0]+"' AND ");
			stb.append("           T1.SEMESTER <= '"+param[1]+"' AND ");
			stb.append("           T1.YEAR = T2.YEAR AND ");
			stb.append("           T1.SEMESTER = T2.SEMESTER AND ");

			stb.append("           NOT EXISTS(SELECT  'X' ");
			stb.append("                      FROM    SCHREG_BASE_MST S1 ");
			stb.append("                      WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
			stb.append("                            ((S1.GRD_DIV IN ('2','3') AND S1.GRD_DATE < ");//転学(2)・退学(3)
			stb.append("                              CASE WHEN T2.EDATE < '"+param[3]+"' THEN T2.EDATE ");
			stb.append("                                   ELSE '"+param[3]+"' END) OR ");
			stb.append("                             (S1.ENT_DIV IN ('4','5') AND S1.ENT_DATE > ");//転入(4)・編入(5)
			stb.append("                              CASE WHEN T2.EDATE < '"+param[3]+"' THEN T2.EDATE ");
			stb.append("                                   ELSE '"+param[3]+"' END)) ) AND ");
			stb.append("           NOT EXISTS(SELECT  'X' ");
			stb.append("                      FROM    SCHREG_TRANSFER_DAT S1 ");
			stb.append("                      WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
			stb.append("                              S1.TRANSFERCD IN ('1','2') AND ");//留学(1)・休学(2)
			stb.append("                              CASE WHEN T2.EDATE < '"+param[3]+"' THEN T2.EDATE ");
			stb.append("                                   ELSE '"+param[3]+"' END ");
			stb.append("                                   BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ) ");
			stb.append("    ) ");
            //NO025----------↑----------
			//学籍の表（クラブ）
			stb.append(",SCHNO_B AS ( ");
			stb.append("    SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, T2.CLUBCD, ");
			stb.append("           T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD,T1.COURSECODE ");
			stb.append("    FROM   SCHNO_C T1, CLUB_HIST T2 ");
			stb.append("    WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("    ) ");
			//学籍の表（クラス）
			stb.append(",SCHNO_A AS ( ");
			stb.append("    SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
			stb.append("           T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD,T1.COURSECODE ");
			stb.append("    FROM   SCHNO_C T1 ");
			stb.append("    WHERE  (T1.GRADE,T1.HR_CLASS) IN(SELECT DISTINCT T2.GRADE,T2.HR_CLASS FROM SCHNO_B T2) ");
			stb.append("    ) ");
			//成績データの表 対象生徒はクラス
            //    成績がNULLで出欠が'KK','KS'の場合出欠データ在り
            //    成績がNOT NULLの場合成績データ在り
			stb.append(",RECORD_DAT_A AS( ");
			stb.append("    SELECT  '1' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
			if ("1".equals(_useCurriculumcd)) {
	            stb.append("            W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
			}
			stb.append("            W1.SUBCLASSCD, W2.GRADE, W2.HR_CLASS, ");
			stb.append("			CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE1, ");
			stb.append("			CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE2, ");
			stb.append("			CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
			stb.append("			           (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
			stb.append("			           VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");//2005.07.06
			stb.append("                 ELSE NULL END AS KK_SCORE3, ");

			stb.append("			CASE WHEN SEM1_INTER_REC IS NOT NULL THEN SEM1_INTER_REC ");
			stb.append("                 ELSE NULL END AS SCORE1, ");
			stb.append("			CASE WHEN SEM1_TERM_REC IS NOT NULL THEN SEM1_TERM_REC ");
			stb.append("                 ELSE NULL END AS SCORE2, ");
			stb.append("			CASE WHEN SEM1_REC IS NOT NULL THEN SEM1_REC ");
			stb.append("                 ELSE NULL END AS SCORE3, ");
			stb.append("			CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS ");
			stb.append("    FROM    KIN_RECORD_DAT W1 ");
			stb.append("			INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '1' AND W1.SCHREGNO = W2.SCHREGNO ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' ");
			stb.append("    UNION ");
			stb.append("    SELECT  '2' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
			stb.append("            W1.SUBCLASSCD, W2.GRADE, W2.HR_CLASS, ");
			stb.append("			CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE1, ");
			stb.append("			CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE2, ");
			stb.append("			CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
			stb.append("			           (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
			stb.append("			           VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");//2005.07.06
			stb.append("                 ELSE NULL END AS KK_SCORE3, ");

			stb.append("			CASE WHEN SEM2_INTER_REC IS NOT NULL THEN SEM2_INTER_REC ");
			stb.append("                 ELSE NULL END AS SCORE1, ");
			stb.append("			CASE WHEN SEM2_TERM_REC IS NOT NULL THEN SEM2_TERM_REC ");
			stb.append("                 ELSE NULL END AS SCORE2, ");
			stb.append("			CASE WHEN SEM2_REC IS NOT NULL THEN SEM2_REC ");
			stb.append("                 ELSE NULL END AS SCORE3, ");
			stb.append("			CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS ");
			stb.append("    FROM    KIN_RECORD_DAT W1 ");
			stb.append("			INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '2' AND W1.SCHREGNO = W2.SCHREGNO ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' ");
			stb.append("    UNION ");
			stb.append("    SELECT  '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
			stb.append("            W1.SUBCLASSCD, W2.GRADE, W2.HR_CLASS, ");
			stb.append("			CASE WHEN SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE1, ");
			stb.append("			NULLIF(W1.SCHREGNO,W2.SCHREGNO) AS KK_SCORE2, ");
			stb.append("			CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR ");
			stb.append("			           (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND ");
			stb.append("			           VALUE(SEM1_REC_FLG,'0') = '0' THEN '( )' ");
			stb.append("                 WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR ");
			stb.append("			           (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND ");
			stb.append("			           VALUE(SEM2_REC_FLG,'0') = '0' THEN '( )' ");
			stb.append("                 WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND ");
			stb.append("                       VALUE(SEM3_TERM_REC_FLG,'0') = '0' THEN '( )' ");//2005.07.06
			stb.append("                 ELSE NULL END AS KK_SCORE3, ");

			stb.append("			CASE WHEN SEM3_TERM_REC IS NOT NULL THEN SEM3_TERM_REC ");
			stb.append("                 ELSE NULL END AS SCORE1, ");
			stb.append("			CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE 0 END AS SCORE2, ");
			stb.append("			CASE WHEN GRADE_RECORD IS NOT NULL THEN GRADE_RECORD ");
			stb.append("                 ELSE NULL END AS SCORE3, ");
            stb.append("            CASE WHEN SUBSTR(W1.SUBCLASSCD,1,2) <> '90' AND VALUE(N1.NAME1,'0') = '0' THEN ");
			stb.append("                 CASE WHEN GRADE_RECORD IS NOT NULL THEN ");
			stb.append("                           CASE JUDGE_PATTERN WHEN 'A' THEN A_PATTERN_ASSESS ");
			stb.append("     				                          WHEN 'B' THEN B_PATTERN_ASSESS ");
			stb.append("     				                          WHEN 'C' THEN C_PATTERN_ASSESS ELSE NULL END ");
			stb.append("                      ELSE NULL END ");
			stb.append("                 ELSE RTRIM(CHAR(GRADE_RECORD)) END AS ASSESS ");
			stb.append("    FROM    KIN_RECORD_DAT W1 ");
			stb.append("			INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '3' AND W1.SCHREGNO = W2.SCHREGNO ");
            stb.append("            LEFT JOIN V_NAME_MST N1 ON N1.YEAR = '"+param[0]+"' AND N1.NAMECD1 = 'D065' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                                   N1.NAME1 = W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD ");
            } else {
                stb.append("                                   N1.NAME1 = W1.SUBCLASSCD ");
            }
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' ");
			stb.append("    ) ");
			//成績データの表 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
			stb.append(",RECORD_DAT_B AS( ");
			stb.append("    SELECT  SEMESTER, 1 AS KIND, ATTENDNO, SCHREGNO, SUBCLASSCD, GRADE, HR_CLASS, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("            KK_SCORE1 AS KK_SCORE, SCORE1 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_A ");
			stb.append("    UNION ");
			stb.append("    SELECT  SEMESTER, 2 AS KIND, ATTENDNO, SCHREGNO, SUBCLASSCD, GRADE, HR_CLASS, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("            KK_SCORE2 AS KK_SCORE, SCORE2 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_A ");
			stb.append("    UNION ");
			stb.append("    SELECT  SEMESTER, 3 AS KIND, ATTENDNO, SCHREGNO, SUBCLASSCD, GRADE, HR_CLASS, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("            KK_SCORE3 AS KK_SCORE, SCORE3 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_A ");
			stb.append("    ) ");
            //評価読替科目
			stb.append(",SUB_REP AS ( ");
			stb.append("    SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.ATTEND_CLASSCD, W1.ATTEND_SCHOOL_KIND, W1.ATTEND_CURRICULUM_CD, ");
            }
			stb.append("            ATTEND_SUBCLASSCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.COMBINED_CLASSCD, W1.COMBINED_SCHOOL_KIND, W1.COMBINED_CURRICULUM_CD, ");
            }
			stb.append("            COMBINED_SUBCLASSCD ");
			stb.append("    FROM    SUBCLASS_REPLACE_COMBINED_DAT W1 ");
			stb.append("    WHERE   YEAR='"+param[0]+"' AND REPLACECD='1' ");
			stb.append("    ) ");
            //合計
			stb.append(",SCH_SUM AS ( ");
			stb.append("    SELECT  T1.SCHREGNO, T1.SEMESTER, T1.KIND, T1.GRADE, T1.HR_CLASS, ");
			stb.append("            SUM(T1.SCORE) AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("            INNER JOIN SCHNO_B T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("    WHERE   T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) < '90' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            (T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD) NOT IN(SELECT DISTINCT COMBINED_CLASSCD,COMBINED_SCHOOL_KIND,COMBINED_CURRICULUM_CD,COMBINED_SUBCLASSCD FROM SUB_REP W2 ) ");
            } else {
                stb.append("            (T1.SUBCLASSCD) NOT IN(SELECT DISTINCT COMBINED_SUBCLASSCD FROM SUB_REP W2 ) ");
            }
			stb.append("            AND T1.KK_SCORE IS NULL ");//2005.07.06
            if ("1".equals(_useCurriculumcd)) {
                stb.append("        AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            } else {
                stb.append("        AND T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            }
			stb.append("    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.KIND, T1.GRADE, T1.HR_CLASS ");
			stb.append("    ) ");
            //平均
			stb.append(",SCH_AVG AS ( ");
			stb.append("    SELECT  T1.SCHREGNO, T1.SEMESTER, T1.KIND, T1.GRADE, T1.HR_CLASS, ");
			stb.append("            ROUND(AVG(FLOAT(SCORE)) ,0) AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("            INNER JOIN SCHNO_B T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("    WHERE   T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) < '90' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            (T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD) NOT IN(SELECT DISTINCT COMBINED_CLASSCD,COMBINED_SCHOOL_KIND,COMBINED_CURRICULUM_CD,COMBINED_SUBCLASSCD FROM SUB_REP W2 ) AND ");
            } else {
                stb.append("            (T1.SUBCLASSCD) NOT IN(SELECT DISTINCT COMBINED_SUBCLASSCD FROM SUB_REP W2 ) AND ");
            }
			stb.append("			(T1.SCHREGNO,T1.SEMESTER,T1.KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
			stb.append("									                 FROM    RECORD_DAT_B W2 ");
			stb.append("									                 WHERE   KK_SCORE IS NOT NULL AND ");
			stb.append("                                                             SUBSTR(SUBCLASSCD,1,2) < '90' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                                                         AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            } else {
                stb.append("                                                         AND SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            }
			stb.append("								                     GROUP BY SCHREGNO,SEMESTER,KIND ");
			stb.append("								                     HAVING 0 < COUNT(*) ) ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("        AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            } else {
                stb.append("        AND T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            }
			stb.append("    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.KIND, T1.GRADE, T1.HR_CLASS ");
			stb.append("    ) ");
            //席次
			stb.append(",SCH_RANK AS ( ");
			stb.append("    SELECT  SCHREGNO, SEMESTER, KIND, GRADE, HR_CLASS, ");
			stb.append("            RANK() OVER(PARTITION BY SEMESTER,KIND,GRADE,HR_CLASS ");
			stb.append("                        ORDER BY AVG(FLOAT(SCORE)) DESC) AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("    WHERE   SCORE IS NOT NULL AND SUBSTR(SUBCLASSCD,1,2) < '90' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            (T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD) NOT IN(SELECT DISTINCT COMBINED_CLASSCD,COMBINED_SCHOOL_KIND,COMBINED_CURRICULUM_CD,COMBINED_SUBCLASSCD FROM SUB_REP W2 ) AND ");
            } else {
                stb.append("            (T1.SUBCLASSCD) NOT IN(SELECT DISTINCT COMBINED_SUBCLASSCD FROM SUB_REP W2 ) AND ");
            }
			stb.append("			(SCHREGNO,SEMESTER,KIND) NOT IN(SELECT  SCHREGNO,SEMESTER,KIND ");
			stb.append("									                 FROM    RECORD_DAT_B W2 ");
			stb.append("									                 WHERE   KK_SCORE IS NOT NULL AND ");
			stb.append("                                                             SUBSTR(SUBCLASSCD,1,2) < '90' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                                                         AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            } else {
                stb.append("                                                         AND SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            }
			stb.append("								                     GROUP BY SCHREGNO,SEMESTER,KIND ");
			stb.append("								                     HAVING 0 < COUNT(*) ) ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("        AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            } else {
                stb.append("        AND T1.SUBCLASSCD NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '"+param[0]+"' AND NAMECD1 = 'D065') ");
            }
			stb.append("    GROUP BY SCHREGNO,SEMESTER,KIND, GRADE, HR_CLASS ");
			stb.append("    ) ");
            //メイン
			stb.append("SELECT T3.SCHREGNO,T1.SEMESTER,T1.KIND, ");
			stb.append("        CASE WHEN SUBSTR(T1.SUBCLASSCD,1,2) = '90' OR N1.NAME1 IS NOT NULL THEN NULL ");//2005.07.04
			stb.append("             WHEN SS1.COMBINED_SUBCLASSCD IS NOT NULL AND (T1.SEMESTER <> '3' OR T1.KIND <> 3) THEN NULL ");//2005.07.04
			stb.append("             ELSE T1.SCORE END AS SCORE, ");//2005.07.04
//			stb.append("        CASE WHEN SUBSTR(T1.SUBCLASSCD,1,2) <> '90' THEN T1.SCORE ELSE NULL END AS SCORE, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("       (SELECT S1.SUBCLASSABBV FROM SUBCLASS_MST S1 WHERE S1.CLASSCD=T1.CLASSCD AND S1.SCHOOL_KIND=T1.SCHOOL_KIND AND S1.CURRICULUM_CD=T1.CURRICULUM_CD AND S1.SUBCLASSCD=T1.SUBCLASSCD) AS SUBABBV, ");
            } else {
                stb.append("       T1.SUBCLASSCD, ");
                stb.append("       (SELECT S1.SUBCLASSABBV FROM SUBCLASS_MST S1 WHERE S1.SUBCLASSCD=T1.SUBCLASSCD) AS SUBABBV, ");
            }
            stb.append("        CASE WHEN SS2.ATTEND_SUBCLASSCD IS NOT NULL AND T1.SEMESTER = '3' AND T1.KIND = 3 THEN NULL ");//2005.07.04
			stb.append("             WHEN SUBSTR(T1.SUBCLASSCD,1,2) <> '90' AND VALUE(N1.NAME1,'0') = '0' THEN T2.ASSESS ");
			stb.append("             ELSE (SELECT ASSESSMARK FROM RELATIVEASSESS_MST R1 ");
			stb.append("                   WHERE  R1.GRADE = T3.GRADE AND ");
			stb.append("                          R1.ASSESSCD = '3' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                      R1.CLASSCD =T1.CLASSCD AND R1.SCHOOL_KIND = T1.SCHOOL_KIND AND R1.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
			stb.append("                          R1.SUBCLASSCD = T1.SUBCLASSCD AND ");
			stb.append("                          ASSESSLOW <= INT(T2.ASSESS) AND INT(T2.ASSESS) <= ASSESSHIGH )END ");
			stb.append("             AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T3.GRADE,T3.HR_CLASS,T3.ATTENDNO,T3.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T3.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   RECORD_DAT_B T1 ");
			stb.append("       INNER JOIN SCHNO_B T3 ON T3.SCHREGNO=T1.SCHREGNO AND T3.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN RECORD_DAT_A T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER AND ");
			if ("1".equals(_useCurriculumcd)) {
	            stb.append("                                T2.CLASSCD =T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
			}
			stb.append("                                    T2.SUBCLASSCD=T1.SUBCLASSCD AND T2.SEMESTER='3' ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T3.YEAR AND S4.SEMESTER=T3.SEMESTER AND S4.GRADE=T3.GRADE AND S4.HR_CLASS=T3.HR_CLASS ");
			stb.append("       LEFT JOIN SUB_REP SS1 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                            SS1.COMBINED_CLASSCD =T1.CLASSCD AND SS1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND SS1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
			stb.append("                                SS1.COMBINED_SUBCLASSCD=T1.SUBCLASSCD ");//2005.07.04
			stb.append("       LEFT JOIN SUB_REP SS2 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                            SS2.ATTEND_CLASSCD =T1.CLASSCD AND SS2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND SS2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
			stb.append("                                SS2.ATTEND_SUBCLASSCD=T1.SUBCLASSCD ");//2005.07.04
            stb.append("       LEFT JOIN V_NAME_MST N1 ON N1.YEAR = '"+param[0]+"' AND N1.NAMECD1 = 'D065' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("                              N1.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            } else {
                stb.append("                              N1.NAME1 = T1.SUBCLASSCD ");
            }
			stb.append("WHERE  T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) <= '90' ");
			stb.append("       AND T1.KK_SCORE IS NULL ");//2005.07.06
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,'X9G' AS SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_SUM T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,'X9H' AS SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_AVG T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,'X9R' AS SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_RANK T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("ORDER BY CLUBCD,GRADE,HR_CLASS,ATTENDNO,SUBCLASSCD,SEMESTER,KIND ");
		} catch( Exception ex ){
			log.warn("prestatementSubclass error!",ex);
		}
		return stb.toString();

	}//prestatementSubclass()の括り


	/**成績表：中学**/
	private String prestatementSubclassJ(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//学籍クラブの表
			stb.append("WITH CLUB_HIST AS ( ");
			stb.append("    SELECT SCHREGNO, CLUBCD ");
			stb.append("    FROM   SCHREG_CLUB_HIST_DAT ");
			stb.append("    WHERE  CLUBCD IN "+param[2]+" AND ");
			stb.append("         ((EDATE IS NULL AND SDATE <= '"+param[3]+"') OR ");
			stb.append("          (EDATE IS NOT NULL AND SDATE <= '"+param[3]+"' AND '"+param[3]+"' <= EDATE)) ");
			stb.append("    ) ");
			//学籍の表（年度・学期）
			stb.append(",SCHNO_C AS ( ");
			stb.append("    SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
			stb.append("           T1.GRADE, T1.HR_CLASS, T3.COURSE_SEQ ");
			stb.append("    FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2, COURSE_GROUP_DAT T3 ");//---NO022
			stb.append("    WHERE  T1.YEAR = '"+param[0]+"' AND ");
			stb.append("           T1.SEMESTER <= '"+param[1]+"' AND ");
			stb.append("           T1.YEAR = T2.YEAR AND ");
			stb.append("           T1.SEMESTER = T2.SEMESTER AND ");
			stb.append("           T1.YEAR = T3.YEAR AND ");
			stb.append("           T1.GRADE = T3.GRADE AND ");
			stb.append("           T1.COURSECODE = T3.COURSECODE AND ");

			stb.append("           NOT EXISTS(SELECT  'X' ");
			stb.append("                      FROM    SCHREG_BASE_MST S1 ");
			stb.append("                      WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
			stb.append("                            ((S1.GRD_DIV IN ('2','3') AND S1.GRD_DATE < ");//転学(2)・退学(3)
			stb.append("                              CASE WHEN T2.EDATE < '"+param[3]+"' THEN T2.EDATE ");
			stb.append("                                   ELSE '"+param[3]+"' END) OR ");
			stb.append("                             (S1.ENT_DIV IN ('4','5') AND S1.ENT_DATE > ");//転入(4)・編入(5)
			stb.append("                              CASE WHEN T2.EDATE < '"+param[3]+"' THEN T2.EDATE ");
			stb.append("                                   ELSE '"+param[3]+"' END)) ) AND ");
			stb.append("           NOT EXISTS(SELECT  'X' ");
			stb.append("                      FROM    SCHREG_TRANSFER_DAT S1 ");
			stb.append("                      WHERE   S1.SCHREGNO = T1.SCHREGNO AND ");
			stb.append("                              S1.TRANSFERCD IN ('1','2') AND ");//留学(1)・休学(2)
			stb.append("                              CASE WHEN T2.EDATE < '"+param[3]+"' THEN T2.EDATE ");
			stb.append("                                   ELSE '"+param[3]+"' END ");
			stb.append("                                   BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ) ");
			stb.append("    ) ");
            //NO025----------↑----------
			//学籍の表（クラブ）
			stb.append(",SCHNO_B AS ( ");
			stb.append("    SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, T2.CLUBCD, ");
			stb.append("           T1.GRADE, T1.HR_CLASS, T1.COURSE_SEQ ");
			stb.append("    FROM   SCHNO_C T1, CLUB_HIST T2 ");
			stb.append("    WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("    ) ");
			//学籍の表（コースグループ）
			stb.append(",SCHNO_A AS ( ");
			stb.append("    SELECT T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T1.ATTENDNO, ");
			stb.append("           T1.GRADE, T1.HR_CLASS, T1.COURSE_SEQ ");
			stb.append("    FROM   SCHNO_C T1 ");
			stb.append("    WHERE  (T1.GRADE,T1.COURSE_SEQ) IN(SELECT DISTINCT T2.GRADE,T2.COURSE_SEQ FROM SCHNO_B T2) ");
			stb.append("    ) ");
			//成績データの表 対象生徒はコースグループ
            //    成績がNULLで出欠が'KK','KS'の場合出欠データ在り
            //    成績がNOT NULLの場合成績データ在り
			stb.append(",RECORD_DAT_A AS( ");
			stb.append("    SELECT  '1' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
			stb.append("            W1.SUBCLASSCD, W2.GRADE, W2.HR_CLASS, W2.COURSE_SEQ, ");
			stb.append("			CASE WHEN SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE1, ");
			stb.append("			CASE WHEN SEM1_TERM_REC IS NULL AND SEM1_TERM_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE2, ");
			stb.append("			CASE WHEN SEM1_REC IS NOT NULL THEN NULL ELSE '( )' END AS KK_SCORE3, ");

			stb.append("			CASE WHEN SEM1_INTER_REC IS NOT NULL THEN SEM1_INTER_REC ");
			stb.append("                 ELSE NULL END AS SCORE1, ");
			stb.append("			CASE WHEN SEM1_TERM_REC IS NOT NULL THEN SEM1_TERM_REC ");
			stb.append("                 ELSE NULL END AS SCORE2, ");
			stb.append("			CASE WHEN SEM1_REC IS NOT NULL THEN SEM1_REC ");
			stb.append("                 ELSE NULL END AS SCORE3, ");
			stb.append("			CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS ");
			stb.append("    FROM    KIN_RECORD_DAT W1 ");
			stb.append("			INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '1' AND W1.SCHREGNO = W2.SCHREGNO ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' ");

			stb.append("    UNION ");
			stb.append("    SELECT  '2' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
			stb.append("            W1.SUBCLASSCD, W2.GRADE, W2.HR_CLASS, W2.COURSE_SEQ, ");
			stb.append("			CASE WHEN SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE1, ");
			stb.append("			CASE WHEN SEM2_TERM_REC IS NULL AND SEM2_TERM_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE2, ");
			stb.append("			CASE WHEN SEM2_REC IS NOT NULL THEN NULL ELSE '( )' END AS KK_SCORE3, ");

			stb.append("			CASE WHEN SEM2_INTER_REC IS NOT NULL THEN SEM2_INTER_REC ");
			stb.append("                 ELSE NULL END AS SCORE1, ");
			stb.append("			CASE WHEN SEM2_TERM_REC IS NOT NULL THEN SEM2_TERM_REC ");
			stb.append("                 ELSE NULL END AS SCORE2, ");
			stb.append("			CASE WHEN SEM2_REC IS NOT NULL THEN SEM2_REC ");
			stb.append("                 ELSE NULL END AS SCORE3, ");
			stb.append("			CASE WHEN W1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS ");
			stb.append("    FROM    KIN_RECORD_DAT W1 ");
			stb.append("			INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '2' AND W1.SCHREGNO = W2.SCHREGNO ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' ");

			stb.append("    UNION ");
			stb.append("    SELECT  '3' AS SEMESTER, W2.ATTENDNO, W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
			stb.append("            W1.SUBCLASSCD, W2.GRADE, W2.HR_CLASS, W2.COURSE_SEQ, ");
			stb.append("			CASE WHEN SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI IN('KK','KS') THEN '( )' ");
			stb.append("                 ELSE NULL END AS KK_SCORE1, ");
			stb.append("			CASE WHEN SEM3_REC IS NOT NULL THEN NULL ELSE '( )' END AS KK_SCORE2, ");
			stb.append("			CASE WHEN GRADE_RECORD IS NOT NULL THEN NULL ELSE '-' END AS KK_SCORE3, ");

			stb.append("			CASE WHEN SEM3_TERM_REC IS NOT NULL THEN SEM3_TERM_REC ");
			stb.append("                 ELSE NULL END AS SCORE1, ");
			stb.append("			CASE WHEN SEM3_REC IS NOT NULL THEN SEM3_REC ");
			stb.append("                 ELSE NULL END AS SCORE2, ");
			stb.append("			CASE WHEN GRADE_RECORD IS NOT NULL THEN GRADE_RECORD ");
			stb.append("                 ELSE NULL END AS SCORE3, ");
			stb.append("            GRADE_ASSESS AS ASSESS ");
			stb.append("    FROM    KIN_RECORD_DAT W1 ");
			stb.append("			INNER JOIN SCHNO_A W2 ON W2.SEMESTER = '3' AND W1.SCHREGNO = W2.SCHREGNO ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' ");
			stb.append("    ) ");
			//成績データの表 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
			stb.append(",RECORD_DAT_B AS( ");
			stb.append("    SELECT  SEMESTER, 1 AS KIND, ATTENDNO, SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("            SUBCLASSCD, GRADE, HR_CLASS, COURSE_SEQ, ");
			stb.append("            KK_SCORE1 AS KK_SCORE, SCORE1 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_A ");
			stb.append("    UNION ");
			stb.append("    SELECT  SEMESTER, 2 AS KIND, ATTENDNO, SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("            SUBCLASSCD, GRADE, HR_CLASS, COURSE_SEQ, ");
			stb.append("            KK_SCORE2 AS KK_SCORE, SCORE2 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_A ");
			stb.append("    UNION ");
			stb.append("    SELECT  SEMESTER, 3 AS KIND, ATTENDNO, SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("            CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("            SUBCLASSCD, GRADE, HR_CLASS, COURSE_SEQ, ");
			stb.append("            KK_SCORE3 AS KK_SCORE, SCORE3 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_A ");
			stb.append("    ) ");
            //合計
			stb.append(",SCH_SUM AS ( ");
			stb.append("    SELECT  T1.SCHREGNO, T1.SEMESTER, T1.KIND, T1.GRADE, T1.COURSE_SEQ, 'X5G' AS SUBCLASSCD, ");
			stb.append("            SUM(T1.SCORE) AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("            INNER JOIN SCHNO_B T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("    WHERE   T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) <= '05' ");
			stb.append("    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.KIND, T1.GRADE, T1.COURSE_SEQ ");
			stb.append("    UNION ");
			stb.append("    SELECT  T1.SCHREGNO, T1.SEMESTER, T1.KIND, T1.GRADE, T1.COURSE_SEQ, 'X9G' AS SUBCLASSCD, ");
			stb.append("            SUM(T1.SCORE) AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("            INNER JOIN SCHNO_B T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("    WHERE   T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) <= '09' ");
			stb.append("    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.KIND, T1.GRADE, T1.COURSE_SEQ ");
			stb.append("    ) ");
            //平均
			stb.append(",SCH_AVG AS ( ");
			stb.append("    SELECT  T1.SCHREGNO, T1.SEMESTER, T1.KIND, T1.GRADE, T1.COURSE_SEQ, 'X5H' AS SUBCLASSCD, ");
			stb.append("            ROUND(AVG(FLOAT(SCORE))*10 ,0)/10 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("            INNER JOIN SCHNO_B T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("    WHERE   T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) <= '05' ");
			stb.append("    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.KIND, T1.GRADE, T1.COURSE_SEQ ");
			stb.append("    UNION ");
			stb.append("    SELECT  T1.SCHREGNO, T1.SEMESTER, T1.KIND, T1.GRADE, T1.COURSE_SEQ, 'X9H' AS SUBCLASSCD, ");
			stb.append("            ROUND(AVG(FLOAT(SCORE))*10 ,0)/10 AS SCORE ");
			stb.append("    FROM    RECORD_DAT_B T1 ");
			stb.append("            INNER JOIN SCHNO_B T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("    WHERE   T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) <= '09' ");
			stb.append("    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.KIND, T1.GRADE, T1.COURSE_SEQ ");
			stb.append("    ) ");

			//席次データの表A 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
			stb.append(",RECORD_RANK_A AS( ");
			stb.append("    SELECT  '1' AS SEMESTER, W1.SCHREGNO, W1.RANK_DIV, ");
			stb.append("			SEM1_INTER_REC_RANK AS SCORE1, ");
			stb.append("			SEM1_TERM_REC_RANK AS SCORE2, ");
			stb.append("			SEM1_REC_RANK AS SCORE3 ");
			stb.append("    FROM    RECORD_RANK_DAT W1 ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' AND W1.RANK_DIV IN('2','3') ");

			stb.append("    UNION ");
			stb.append("    SELECT  '2' AS SEMESTER, W1.SCHREGNO, W1.RANK_DIV, ");
			stb.append("			SEM2_INTER_REC_RANK AS SCORE1, ");
			stb.append("			SEM2_TERM_REC_RANK AS SCORE2, ");
			stb.append("			SEM2_REC_RANK AS SCORE3 ");
			stb.append("    FROM    RECORD_RANK_DAT W1 ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' AND W1.RANK_DIV IN('2','3') ");

			stb.append("    UNION ");
			stb.append("    SELECT  '3' AS SEMESTER, W1.SCHREGNO, W1.RANK_DIV, ");
			stb.append("			SEM3_TERM_REC_RANK AS SCORE1, ");
			stb.append("			SEM3_REC_RANK AS SCORE2, ");
			stb.append("			GRADE_RECORD_RANK AS SCORE3 ");
			stb.append("    FROM    RECORD_RANK_DAT W1 ");
			stb.append("	WHERE   W1.YEAR = '"+param[0]+"' AND W1.RANK_DIV IN('2','3') ");
			stb.append("    ) ");

			//席次データの表B 学期・成績種別で分解 => 学期・成績種別ごとで１レコードとする
			stb.append(",RECORD_RANK_B AS( ");
			stb.append("    SELECT  SEMESTER, 1 AS KIND, SCHREGNO, RANK_DIV, SCORE1 AS SCORE ");
			stb.append("    FROM    RECORD_RANK_A ");
			stb.append("    UNION ");
			stb.append("    SELECT  SEMESTER, 2 AS KIND, SCHREGNO, RANK_DIV, SCORE2 AS SCORE ");
			stb.append("    FROM    RECORD_RANK_A ");
			stb.append("    UNION ");
			stb.append("    SELECT  SEMESTER, 3 AS KIND, SCHREGNO, RANK_DIV, SCORE3 AS SCORE ");
			stb.append("    FROM    RECORD_RANK_A ");
			stb.append("    ) ");

            //席次
			stb.append(",SCH_RANK AS ( ");
			stb.append("    SELECT  SCHREGNO, SEMESTER, KIND, 'X5R' AS SUBCLASSCD, SCORE ");
			stb.append("    FROM    RECORD_RANK_B ");
			stb.append("    WHERE   SCORE IS NOT NULL AND RANK_DIV = '2' ");
			stb.append("    UNION ");
			stb.append("    SELECT  SCHREGNO, SEMESTER, KIND, 'X9R' AS SUBCLASSCD, SCORE ");
			stb.append("    FROM    RECORD_RANK_B ");
			stb.append("    WHERE   SCORE IS NOT NULL AND RANK_DIV = '3' ");
			stb.append("    ) ");
            //NO024----------↑----------

            //メイン
            //成績個人
			stb.append("SELECT T3.SCHREGNO,T1.SEMESTER,T1.KIND,T1.SCORE, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("       (SELECT S1.SUBCLASSABBV FROM SUBCLASS_MST S1 WHERE S1.CLASSCD=T1.CLASSCD AND S1.SCHOOL_KIND=T1.SCHOOL_KIND AND S1.CURRICULUM_CD=T1.CURRICULUM_CD AND S1.SUBCLASSCD=T1.SUBCLASSCD) AS SUBABBV, ");
            } else {
                stb.append("       T1.SUBCLASSCD, ");
                stb.append("       (SELECT S1.SUBCLASSABBV FROM SUBCLASS_MST S1 WHERE S1.SUBCLASSCD=T1.SUBCLASSCD) AS SUBABBV, ");
            }
			stb.append("       T2.ASSESS,S3.NAME,S4.HR_NAMEABBV,T3.GRADE,T3.HR_CLASS,T3.ATTENDNO,T3.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T3.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   RECORD_DAT_B T1 ");
			stb.append("       INNER JOIN SCHNO_B T3 ON T3.SCHREGNO=T1.SCHREGNO AND T3.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN RECORD_DAT_A T2 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER AND T2.SUBCLASSCD=T1.SUBCLASSCD AND T2.SEMESTER='3' ");
			if ("1".equals(_useCurriculumcd)) {
	            stb.append("       AND T2.CLASSCD=T1.CLASSCD AND T2.SCHOOL_KIND=T1.SCHOOL_KIND AND T2.CURRICULUM_CD=T1.CURRICULUM_CD ");
			}
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T3.YEAR AND S4.SEMESTER=T3.SEMESTER AND S4.GRADE=T3.GRADE AND S4.HR_CLASS=T3.HR_CLASS ");
			stb.append("WHERE  T1.SCORE IS NOT NULL AND SUBSTR(T1.SUBCLASSCD,1,2) <= '09' ");
            //成績５合計
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,T2.SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_SUM T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("WHERE  T2.SUBCLASSCD = 'X5G' ");
            //成績５平均
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,T2.SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_AVG T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("WHERE  T2.SUBCLASSCD = 'X5H' ");
            //成績５席次
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,T2.SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_RANK T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("WHERE  T2.SUBCLASSCD = 'X5R' ");
            //成績９合計
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,T2.SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_SUM T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("WHERE  T2.SUBCLASSCD = 'X9G' ");
			stb.append("       AND (1 < T2.KIND OR T2.SEMESTER = '3') ");
            //成績９平均
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,T2.SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_AVG T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("WHERE  T2.SUBCLASSCD = 'X9H' ");
			stb.append("       AND (1 < T2.KIND OR T2.SEMESTER = '3') ");
            //成績９席次
			stb.append("UNION  ");
			stb.append("SELECT T1.SCHREGNO,T2.SEMESTER,T2.KIND,T2.SCORE,T2.SUBCLASSCD,'' AS SUBABBV, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NOT NULL THEN NULL ELSE '' END AS ASSESS, ");
			stb.append("       S3.NAME,S4.HR_NAMEABBV,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO,T1.CLUBCD ");
			stb.append("       ,(SELECT S2.CLUBNAME FROM CLUB_MST S2 WHERE S2.CLUBCD=T1.CLUBCD) AS CLUBNAME ");
			stb.append("FROM   SCH_RANK T2 ");
			stb.append("       INNER JOIN SCHNO_B T1 ON T2.SCHREGNO=T1.SCHREGNO AND T2.SEMESTER=T1.SEMESTER ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST S3 ON S3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT S4 ON S4.YEAR=T1.YEAR AND S4.SEMESTER=T1.SEMESTER AND S4.GRADE=T1.GRADE AND S4.HR_CLASS=T1.HR_CLASS ");
			stb.append("WHERE  T2.SUBCLASSCD = 'X9R' ");
			stb.append("       AND (1 < T2.KIND OR T2.SEMESTER = '3') ");

			stb.append("ORDER BY CLUBCD,GRADE,HR_CLASS,ATTENDNO,SUBCLASSCD,SEMESTER,KIND ");

		} catch( Exception ex ){
			log.warn("prestatementSubclassJ error!",ex);
		}
		return stb.toString();

	}//prestatementSubclassJ()の括り


	/**PrepareStatement close**/
	private void prestatementClose( PreparedStatement arrps[] )
	{
		try {
			if( arrps[0] != null )arrps[0].close();
		} catch( Exception ex ){
			log.error("[KNJD171K]prestatementClose error!",ex);
		}
	}//prestatementClose()の括り


	/** 
     *  get parameter doGet()パラメータ受け取り 
	 */
    private void getParam( HttpServletRequest request, String param[] ){

		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("GAKKI");   					//1-3:学期
			param[2] = setGetSchregno( request.getParameterValues("CLASS_SELECTED") );	//クラブコードの編集
			param[3] = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );    //異動基準日
	        param[7] = request.getParameter("SCHOOL_JUDGE");         		//中学(J)か高校(H)かを判断---2005.06.13Add
		} catch( Exception ex ) {
			log.error("request.getParameter error!",ex);
		}
//for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("param[" + i + "]=" + param[i]);
    }


	/**
     *  対象クラブコード編集(SQL用) 
     */
	private String setGetSchregno( String schno[] ){

        final StringBuffer stb = new StringBuffer();

		for( int ia=0 ; ia<schno.length ; ia++ ){
			if( ia==0 )	stb.append("('");
			else		stb.append("','");
			stb.append(schno[ia]);
		}
		stb.append("')");

		return stb.toString();
	}


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){

		try {
            outstrm = new PrintWriter (response.getOutputStream());
            if( printname!=null )	response.setContentType("text/html");
            else					response.setContentType("application/pdf");

            int ret = svf.VrInit();						   		//クラスの初期化

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
    private void closeSvf( Vrw32alp svf, boolean nonedata ){
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
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}
		int ret = svf.VrQuit();
		if( ret == 0 )log.info("===> VrQuit():" + ret);
		outstrm.close();			//ストリームを閉じる 
    }


	/** DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
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
	private boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!"+ex );
			return true;
		}
		return false;
	}//private boolean Open_db()


	/** DB close */
	private void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}
	}//private Close_Db()



}//クラスの括り
