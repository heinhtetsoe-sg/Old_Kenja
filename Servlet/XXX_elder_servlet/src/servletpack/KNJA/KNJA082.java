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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＡ０８２＞  クラス編成事前一覧
 *
 *	2006/03/17 m-yama 作成
 *	2006/03/20 m-yama NO001 ふりがな出力指定追加
 *  2006/03/27 yamashiro NO002 １学年における留年生の氏名取得を変更
 *  2006/03/27 yamashiro NO003 「クラス名＋出席番号」の出力を「クラス名」だけとする
 *                             「カナ」が出力されない不具合を修正
 */

public class KNJA082 {


	private static final Log log = LogFactory.getLog(KNJA082.class);
	
	private String _param_year;
    private String _param_grade;
    private String _param_output;
    private String _date;
    private String _param_output3;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
					 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String[] param = new String[11];

		//パラメータの取得
		try {
			_param_year  = request.getParameter("YEAR");			//来年度
			_param_grade  = request.getParameter("GRADE");			//学年
			_param_output  = request.getParameter("OUTPUT");			//帳票種類
			_param_output3  = request.getParameter("OUTPUT3");		//ふりがな出力 NO001
		} catch (Exception ex) {
			log.warn("parameter error!", ex);
		}

		//	print設定
		PrintWriter outstrm = new PrintWriter(response.getOutputStream());
		response.setContentType("application/pdf");

		//	svf設定
		svf.VrInit();						   	//クラスの初期化
		svf.VrSetSpoolFileStream(response.getOutputStream());   		//PDFファイル名の設定

		//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!",ex);
			return;
		}
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

		//	ＳＶＦ作成処理
		boolean nonedata = false; 								//該当データなしフラグ

		getHeaderData(db2,svf,param);							//ヘッダーデータ抽出メソッド

		//SVF出力
        if (printMain(db2, svf, param)) {
            nonedata = true;
        }

        // 該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

	}//doGetの括り


	/**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2, Vrw32alp svf, String[] param) {

	//	作成日
		try {
    		KNJ_Get_Info getinfo = new KNJ_Get_Info();
    		KNJ_Get_Info.ReturnVal returnval = null;
			returnval = getinfo.Control(db2);
            _date = KNJ_EditDate.h_format_JP(returnval.val3);
    		getinfo = null;
    		returnval = null;
        } catch (Exception e) {
            log.warn("ctrl_date get error!", e);
		}
	}//getHeaderData()の括り


	/**印刷処理メイン*/
    private boolean printMain(DB2UDB db2, Vrw32alp svf, String[] param) {
		boolean nonedata = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
		    final String sql = classMeisaiSql(param);
            log.debug(sql);
            ps = db2.prepareStatement(sql);
			rs = ps.executeQuery();

            // 明細データをセット
			nonedata = printMeisai(svf, param, rs);

        } catch (Exception ex) {
            log.warn("printMain read error!", ex);
		} finally {
		    DbUtils.closeQuietly(null, ps, rs);
		    db2.commit();
		}
		return nonedata;

	}//printMain()の括り

    private boolean printMeisai(Vrw32alp svf, String[] param, ResultSet rs) {
		boolean nonedata = false;
		int row  = 1;				//列カウンタ MAX 8
		int line = 1;				//行カウンタ MAX50
		int boycnt  = 0;			//男子カウンタ
		int girlcnt = 0;			//女子カウンタ
		String classchange = "*";	//改列用
		String gradechange = "*";	//改列用
		
        if (_param_output.equals("1")) {
            svf.VrSetForm("KNJA082_1.frm", 1);
        } else {
            svf.VrSetForm("KNJA082_2.frm", 1);
        }
        svf.VrsOut("DATE", _date);

        try {
            while (rs.next()) {
                if (_param_output.equals("1")) {
                    if (!classchange.equals("*") && !classchange.equalsIgnoreCase(rs.getString("CLASSCHANGE"))) {
                        svf.VrsOut("BOY" + row, String.valueOf(boycnt));
                        svf.VrsOut("GIRL" + row, String.valueOf(girlcnt));
                        svf.VrsOut("TOTAL" + row, String.valueOf(boycnt + girlcnt));
                        girlcnt = 0;
                        boycnt = 0;
                        line = 1;
                        row++;
                    }
                    if (!gradechange.equals("*") && !gradechange.equalsIgnoreCase(rs.getString("GRADE"))) {
                        svf.VrEndPage();
                        girlcnt = 0;
                        boycnt  = 0;
                        line = 1;
                        row  = 1;
                    }
                    if (line > 50) {
                        line = 1;
                        row++;
                    }
                    if (row > 8) {
                        svf.VrEndPage();
                        line = 1;
                        row = 1;
                    }
                    svf.VrsOut("HR_NAME" + row, rs.getString("HR_NAME"));
                    svf.VrsOut("STAFF_NAME" + row, rs.getString("STAFFNAME"));

                    //NO001
                    if (null != _param_output3) {
                        svf.VrsOutn("KANA" + row, line, rs.getString("NAME_KANA"));
                    }
                    svf.VrsOutn("NAME" + row, line, rs.getString("NAME"));
                    svf.VrsOutn("ATTENDNO" + row, line, rs.getString("ATTENDNO"));
                    if (null != rs.getString("SEX") && rs.getString("SEX").equals("1")) {
                        boycnt++;
                    } else {
                        girlcnt++;
                    }
                } else {
                    /**五十音別明細データをセット*/
                    if (line > 50) {
                        line = 1;
                        row++;
                    }
                    if (row > 4) {
                        svf.VrEndPage();
                        line = 1;
                        row = 1;
                    }
                    if (!gradechange.equals("*") && !gradechange.equalsIgnoreCase(rs.getString("GRADE"))) {
                        svf.VrEndPage();
                        row = 1;
                        line = 1;
                    }
                    if (classchange.equals("*")) {
                        svf.VrsOutn("SORT" + row, line, rs.getString("CLASSCHANGE"));
                        line++;
                    } else if (!classchange.equals("*") && !classchange.equalsIgnoreCase(rs.getString("CLASSCHANGE"))) {
                        svf.VrsOutn("SORT" + row, line, rs.getString("CLASSCHANGE"));
                        line++;
                    }
                    if (line > 50) {
                        line = 1;
                        row++;
                    }
                    if (row > 4) {
                        svf.VrEndPage();
                        line = 1;
                        row = 1;
                    }
                    //NO001
                    if (null != _param_output3){
                        //NO003
                        if (rs.getString("NAME_KANA") != null) {
                            //log.debug("length="+rs.getString("NAME_KANA")+"  "+rs.getString("NAME_KANA").length());
                            if (rs.getString("NAME_KANA").length() <= 10) {
                                svf.VrsOutn("KANA" + row + "_1", line, rs.getString("NAME_KANA")); // NO003
                            } else {
                                svf.VrsOutn("KANA" + row + "_2", line, rs.getString("NAME_KANA")); // NO003
                            }
                        }
                        //NO003 svf.VrsOutn("KANA"+row      ,line   ,rs.getString("NAME_KANA") );
                    }
                    svf.VrsOutn("NAME" + row, line, rs.getString("NAME"));
                    svf.VrsOutn("HR_NAME" + row, line, rs.getString("HR_NAME")); // NO003
                    //NO003 svf.VrsOutn("HR_NAME"+row       ,line   ,rs.getString("HR_NAME")+"-"+rs.getString("ATTENDNO") + "番" );
                }
                line++;
                classchange = rs.getString("CLASSCHANGE");
                gradechange = rs.getString("GRADE");
                nonedata = true;
            }
            if (line > 1) {
                if (_param_output.equals("1")) {
                    svf.VrsOut("BOY" + row, String.valueOf(boycnt));
                    svf.VrsOut("GIRL" + row, String.valueOf(girlcnt));
                    svf.VrsOut("TOTAL" + row, String.valueOf(boycnt + girlcnt));
                }
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.warn("printMeisai_2 read error!", ex);
        }
		return nonedata;

	}//printMeisai_1()の括り

	/**
	 *	クラス別明細データを抽出
	 */
	private String classMeisaiSql(String[] param)
	{
	    final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append("SELECT ");
        stb.append("    CASE WHEN T5.NAME      IS NULL OR T5.NAME = ''      THEN T2.NAME      ELSE T5.NAME      END AS NAME, ");  //NO002
        stb.append("    CASE WHEN T5.NAME_KANA IS NULL OR T5.NAME_KANA = '' THEN T2.NAME_KANA ELSE T5.NAME_KANA END AS NAME_KANA, ");  //NO002
        if (_param_output.equals("1")) {
            stb.append("    T3.HR_NAME, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    CASE WHEN T5.SEX IS NULL OR T5.SEX = '' THEN T2.SEX ELSE T5.SEX END AS SEX, ");  //NO002
            stb.append("    VALUE(T1.GRADE,'00') || VALUE(T1.HR_CLASS,'000') AS CLASSCHANGE, ");
        } else {
            stb.append("    VALUE(T3.HR_NAMEABBV,'') AS HR_NAME, ");  //NO002
            stb.append("    VALUE(T1.ATTENDNO,'000') AS ATTENDNO, ");
            stb.append("    VALUE(CASE WHEN T5.NAME_KANA IS NULL THEN SUBSTR(T2.NAME_KANA,1,3) ELSE SUBSTR(T5.NAME_KANA,1,3) END, '') AS CLASSCHANGE, ");  //NO002
        }
        stb.append("    T4.STAFFNAME, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS ");
        stb.append("FROM ");
        stb.append("    CLASS_FORMATION_DAT T1 ");
        stb.append("    LEFT JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR = T1.YEAR ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T3.GRADE = T1.GRADE ");
        stb.append("    AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("    LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T3.TR_CD1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR ");
        stb.append("      AND T6.GRADE = T1.GRADE ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + _param_year + "' ");
        stb.append("    AND T1.SEMESTER = '1' ");
        if (!_param_grade.equals("99")) {
            stb.append("    AND T1.GRADE = '" + _param_grade + "' ");
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("* ");
        stb.append("FROM ");
        stb.append("    MAIN_T ");
        if (_param_output.equals("1")) {
            stb.append("ORDER BY ");
            stb.append("    GRADE, ");
            stb.append("    HR_CLASS, ");
            stb.append("    ATTENDNO ");
        } else {
            stb.append("ORDER BY ");
            stb.append("    GRADE, ");
            stb.append("    NAME_KANA ");
        }
        return stb.toString();

	}//classMeisaiSql()の括り

}//クラスの括り
