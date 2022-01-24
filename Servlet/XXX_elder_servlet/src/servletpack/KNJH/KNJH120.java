package servletpack.KNJH;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [指導情報管理]
 *
 *					＜ＫＮＪＨ１２０＞  科目別資格取得一覧
 *
 * 2003/12/11 nakamoto 学年が変わったら、改ページする。
 * 2006/04/12 m-yama   NO001 テーブル変更に伴う修正。
 * 2006/08/14 m-yama   NO002 出力順の修正、設定内容の追加。
 * 2006/08/22 m-yama   NO003 設定区分により、名称マスタを切替える。
 * 2007/09/25 nakamoto NO004 修正前：今年度の科目年度データの科目を表示する。
 *                           修正後：科目マスタから表示する。
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH120 extends HttpServlet {
	Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB    db2;					// Databaseクラスを継承したクラス
	String dbname = new String();
	boolean nonedata; 			// 該当データなしフラグ
	int ret;		// ＳＶＦ応答値
	KNJ_EditDate editdate = new KNJ_EditDate();		//和暦変換取得クラスのインスタンス作成

    private static final Log log = LogFactory.getLog("KNJH120.class");
    
    private String _useCurriculumcd = null;
    private String _useSchool_KindField;
    private String _SCHOOLKIND;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);

	// パラメータの取得
	    String param[] = new String[6];
		try {
			dbname   = request.getParameter("DBNAME");      	// データベース名
	        param[0] = request.getParameter("YEAR");         	// 年度
			param[1] = request.getParameter("GAKKI");   		// 学期

			//学年・組の編集
			String ghclass[] = request.getParameterValues("CLASS_SELECTED");   	// クラス
			int i = 0;
			param[2] = "(";
			while(i < ghclass.length){
				if(ghclass[i] == null ) break;
				if(i > 0) param[2] = param[2] + ",";
				param[2] = param[2] + "'" + ghclass[i] + "'";
				i++;
			}
			param[2] = param[2] + ")";


			//科目
				param[3] = request.getParameter("SUBCLASSCD_FROM");  	// コードfrom
				param[4] = request.getParameter("SUBCLASSCD_TO");  		// コードto
				
				_useCurriculumcd = request.getParameter("useCurriculumcd");
			    _useSchool_KindField = request.getParameter("useSchool_KindField");
			    _SCHOOLKIND = request.getParameter("SCHOOLKIND");

		} catch( Exception ex ) {
			System.out.println("[KNJH120]parameter error!");
			System.out.println(ex);
		}


	// print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();							//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定
		ret = svf.VrSetForm("KNJH120.frm", 4);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			System.out.println("[KNJH120]DB2 open error!");
		}


		/*作成日取得*/
		try {
			KNJ_Control date = new KNJ_Control();								//取得クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = date.Control(db2);
			param[5] = returnval.val3;											//作成日
		} catch( Exception e ){
			System.out.println("[KNJH120]DB2 CONTROL_MST query error!");
			System.out.println( e );
		}

		for(int ia=0 ; ia<param.length ; ia++) System.out.println("[KNJH120]param[" + ia + "]=" + param[ia]);


	/*-----------------------------------------------------------------------------
	    ＳＶＦ作成処理       
	  -----------------------------------------------------------------------------*/
		nonedata = false; 		// 該当データなしフラグ(MES001.frm出力用)

		set_detail(param);

	// ＳＶＦフォーム出力
		/*該当データ無し*/
		if(nonedata == false){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}

	// 終了処理
		db2.close();		// DBを閉じる
		ret = svf.VrQuit();
		outstrm.close();	// ストリームを閉じる 

	}	//doGetの括り


	/*----------------------------*
	 * 賞罰明細出力               *
	 *----------------------------*/
	public void set_detail(String param[])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "WITH MAIN_T AS (SELECT "
					+ "T2.GRADE,"
					+ "T2.HR_CLASS,"
					+ "T2.ATTENDNO,"
					+ "T2.COURSECD,"
					+ "T2.MAJORCD,"
					+ "T2.COURSECODE,";
			if ("1".equals(_useCurriculumcd)) {
                sql+=     "T1.CLASSCD, ";
                sql+=     "T1.SCHOOL_KIND, ";
                sql+=     "T1.CURRICULUM_CD, ";
			}
			sql+=	  "T1.SUBCLASSCD AS SUBCLASSCD,"
					+ "T1.SEQ,"
					+ "CHAR(T1.REGDDATE) AS REGDDATE,"
					+ "T3.NAME_SHOW,"
					+ "T5.SUBCLASSNAME,"
					+ "CASE WHEN T1.CONDITION_DIV = '1' THEN '増加' WHEN T1.CONDITION_DIV = '2' THEN '学外増加' ELSE '学外認定' END AS CONDITION,"
					+ "CASE WHEN T1.CONDITION_DIV = '1' THEN T1.CONTENTS || ':' || N1.NAME1 "
					+ "     WHEN T1.CONDITION_DIV = '2' THEN T1.CONTENTS || ':' || N2.NAME1 "
					+ "     ELSE VALUE(T1.CONTENTS,' ') END AS CONTENTS,"
					+ "VALUE(T1.REMARK,' ') AS REMARK,"
					+ "VALUE(CHAR(T1.CREDITS),' ') AS CREDIT2,"
					+ "T4.HR_NAMEABBV "
				+ "FROM "
					+ "SCHREG_QUALIFIED_DAT T1 "
					+ "LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'H305' AND N1.NAMECD2 = T1.CONTENTS "
					+ "LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'H306' AND N2.NAMECD2 = T1.CONTENTS,"
					+ "SCHREG_REGD_DAT  T2,"
					+ "SCHREG_REGD_HDAT  T4,"
					+ "SUBCLASS_MST  T5,"//NO004 V_SUBCLASS_MST → SUBCLASS_MST
					+ "SCHREG_BASE_MST  T3 "
				+ "WHERE "
						+ "T2.GRADE || T2.HR_CLASS  IN " + param[2] +  " "
					+ "AND T2.YEAR      	= '" + param[0] + "' "
					+ "AND T2.SEMESTER  	= '" + param[1] + "' "
					+ "AND T2.YEAR  		= T1.YEAR "
					+ "AND T2.SCHREGNO  	= T1.SCHREGNO "
					+ "AND T3.SCHREGNO  	= T1.SCHREGNO "
//NO004				+ "AND T5.YEAR  		= T1.YEAR "
					+ "AND ";
	        if ("1".equals(_useCurriculumcd)) {
	            sql+=     "T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ";
	        }
			sql+=     "    T5.SUBCLASSCD 	BETWEEN '" + param[3] + "' AND '" + param[4] + "' ";
            if ("1".equals(_useCurriculumcd)) {
                sql+= "AND T5.CLASSCD       = T1.CLASSCD ";
                sql+= "AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                sql+= "AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql+= "   AND T5.SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                }
            }
            sql+=     "AND T5.SUBCLASSCD  	= T1.SUBCLASSCD "
					+ "AND T2.YEAR  		= T4.YEAR "
					+ "AND T2.SEMESTER  	= T4.SEMESTER "
					+ "AND T2.GRADE  		= T4.GRADE "
					+ "AND T2.HR_CLASS  	= T4.HR_CLASS "
				+ ") "
				+ "SELECT "
					+ "T1.GRADE,"
					+ "T1.HR_CLASS,"
					+ "T1.ATTENDNO,";
            if ("1".equals(_useCurriculumcd)) {
                sql+= "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
            }
            sql+=     "T1.SUBCLASSCD AS SUBCLASSCD,"
					+ "T1.REGDDATE,"
					+ "T1.SEQ,"
					+ "T1.NAME_SHOW,"
					+ "T1.SUBCLASSNAME,"
					+ "T1.CONDITION,"
					+ "T1.CONTENTS,"
					+ "T1.REMARK,"
					+ "T1.CREDIT2,"
					+ "T1.HR_NAMEABBV, "
					+ "T2.CREDITS AS CREDIT1 "
				+ "FROM MAIN_T T1 "
					+ " LEFT JOIN CREDIT_MST T2 ON T2.YEAR = '" + param[0] + "' AND T2.COURSECD = T1.COURSECD "
					+ "                         AND T2.MAJORCD = T1.MAJORCD AND T2.GRADE = T1.GRADE ";
            if ("1".equals(_useCurriculumcd)) {
                sql +="                         AND T2.COURSECODE = T1.COURSECODE AND T2.CLASSCD = T1.CLASSCD ";
                sql +="                         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            } else {
                sql +="                         AND T2.COURSECODE = T1.COURSECODE AND T2.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
            }
            sql +="                         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
			sql +=    "ORDER BY 1,2,3,4,5,6"; //NO002 SEQを追加

            System.out.println("[KNJH120]set_detail sql="+sql);
            db2.query(sql);
			java.sql.ResultSet rs = db2.getResultSet();
			System.out.println("[KNJH120]set_detail sql ok!");

  	  	   /** 照会結果の取得とsvf_formへ出力 **/
			//年度
			ret = svf.VrsOut("nendo"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
			//作成日
			ret = svf.VrsOut("TODAY"	, KNJ_EditDate.h_format_JP(param[5]));

			while( rs.next() ){
// 2003/12/11
				ret = svf.VrsOut("GRADE"    	, rs.getString("GRADE"));					//学年（改ページ用）

				ret = svf.VrsOut("HR_NAME"    	, rs.getString("HR_NAMEABBV"));				        //学年-組
				ret = svf.VrsOut("ATTENDNO"   	, String.valueOf(rs.getInt("ATTENDNO")));	        //出席番号
				ret = svf.VrsOut("NAME"   	  	, rs.getString("NAME_SHOW"));					    //氏名
				ret = svf.VrsOut("DATE"     	, KNJ_EditDate.h_format_JP(rs.getString("REGDDATE")));	//取得日
				ret = svf.VrsOut("SUBJECT"   	, rs.getString("SUBCLASSNAME"));					//科目名
				ret = svf.VrsOut("REMARK1"   	, rs.getString("REMARK"));					        //備考
				ret = svf.VrsOut("CONTENTS1"  	, rs.getString("CONTENTS"));				        //資格内容
				ret = svf.VrsOut("CREDIT1"   	, rs.getString("CREDIT1"));					        //単位数   NO001
				ret = svf.VrsOut("CREDIT2"   	, rs.getString("CREDIT2"));					        //単位数   NO001
				ret = svf.VrsOut("CONDITION_DIV", rs.getString("CONDITION"));				        //設定内容 NO001
				ret = svf.VrEndRecord();
				nonedata = true; //該当データなしフラグ
			}
			db2.commit();
			System.out.println("[KNJH120]set_detail read ok!");
		} catch( Exception ex ){
			System.out.println("[KNJH120]set_detail read error!");
			System.out.println( ex );
		}

	}	//set_detailの括り


}	//クラスの括り

