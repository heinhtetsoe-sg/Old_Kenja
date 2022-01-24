/**
 *
 *	学校教育システム 賢者 [出欠管理] 欠席・欠課の要注意者・超過者リスト
 *
 *	2004/12/16 yamashiro
 *	2005/03/07 yamashiro タイムアウトで出力されない不具合を修正 => 成績一覧表（レーザー）の出欠記録のＳＱＬと同一仕様
 *	2005/03/09 yamashiro タイトルが出力されない不具合を修正
 *                       転学、退学において異動日は在籍とみなす
 *  2005/03/17 yamashiro 欠席日数超過者をテーブルに書き込む処理を追加
 *  2005/10/08 yamashiro 編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                       留学・休学・不在日数の算出において、ATTEND_SEMES_DATもみる。
 *  2006/03/08 yamashiro ○残作業?117の未対応について対応 --NO101
 *  2006/03/13 yamashiro ○「出席すべき日数＊1/3(3以外もあり)」において小数点は切り上げ。--NO102
 *
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Iterator;

import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC140K_GRADE extends KNJC140K_BASE {

    private static final Log log = LogFactory.getLog(KNJC140K_GRADE.class);

    private String sql1;
	ResultSet rs;
    private boolean _hasSchChrDatExecutediv;

    /**
     *  コンストラクター
     */
    KNJC140K_GRADE(final DB2UDB db2, final Vrw32alp svf, final String[] param, final String[] pselect) {
        super(db2, svf, param, pselect);
        _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
    }

    private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT 1 FROM ");
        if (StringUtils.isBlank(colname)) {
            stb.append("SYSCAT.TABLES");
        } else {
            stb.append("SYSCAT.COLUMNS");
        }
        stb.append(" WHERE TABNAME = '" + tabname + "' ");
        if (!StringUtils.isBlank(colname)) {
            stb.append(" AND COLNAME = '" + colname + "' ");
        }
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasTableColumn = false;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                hasTableColumn = true;
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
        return hasTableColumn;
    }

	/** 
     *  印刷処理 
     *  2005/03/07Modify
     */
	void printSvfMain()	{

        if (log.isTraceEnabled()) {
            for (int i = 0; i < param.length; i++) {
                log.trace("param[" + i + "]=" + param[i]);
            }
        }

		//SQL作成
		try {
		    sql1 = prestatementAbsentData();
			ps1 = db2.prepareStatement(sql1);		//学級別月別生徒欠課
			//ps2 = db2.prepareStatement(prestatementJnisu());		//学級別授業日数
			ps3 = db2.prepareStatement(prestatementHrclass());	    //学年別学級
			ps2 = db2.prepareStatement(prestatementupdate());	        //超過者を登録 05/03/17
		} catch (Exception ex) {
            log.error("error! ", ex);
		}

		//学年ごとに全学級を読込む --> データは学級ごとに処理  pselect[]は対象学年
		for (int i = 0; i < pselect.length; i++) {
			deleteOver(pselect[i]);					//05/03/17
			printSvfGrade(pselect[i]);
		}
		rs = null;

		prestatementClose();	//preparestatementを閉じる

	}//boolean Set_Detail_1()の括り


	/** 
     *  学年別印刷処理 
     *  2005/03/07
     */
	void printSvfGrade(final String grade)
	{
	    final List data_lst = new LinkedList(); 	//生徒明細
		//学年ごとの処理
		try {
		    svf.VrsOut("TITLE",	getPaperTitle());   //05/03/09Modify
			ps3.setString(1, grade);	//学年
			rs = ps3.executeQuery();
			while (rs.next()) {
            	printSvfHrclass(grade, rs.getString("HR_CLASS"), data_lst);	//学級別出力処理
			}

		} catch (Exception ex) {
		    log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(rs);
        }

        //長欠者がいれば明細印刷
        if (0 < data_lst.size()) {
            svfprintDetailOut(data_lst);        //長欠者明細出力
            data_lst.clear();
        }
	}

    /** 
     *  タイトル出力
     *  2005/03/09
     */
    String getPaperTitle()
    {
        return "欠席日数超過者リスト［学年別］";
    }

	/** 
     *  学級ごとの処理 
     */
	private void printSvfHrclass(final String grade, final String hrclass, final List data_lst) {

		ResultSet rs = null;

	//	学級別長期欠課者の取得-->生徒明細データをリストへ保存
		try {
			svf.VrsOut("GRADE",	String.valueOf(Integer.parseInt(grade)) + "年生");	//学年（改ページ）
			int pp = 0;
			ps1.setString(++pp, grade);		//学年
			ps1.setString(++pp, hrclass);	    //組
			rs = ps1.executeQuery();
            //log.debug("ps1 = " + ps1.toString());
			while (rs.next()) {
				printSvfSaveListDetail(rs, data_lst);
				updateOver(rs, grade);           //05/03/17
			}
		} catch (Exception ex) {
            log.error("error! ", ex);
		} finally {
		    DbUtils.closeQuietly(rs);
		}

	}//void printSvfHrclass()の括り

	/** 
     *  学級別処理
     *      生徒明細をListへ保存 => 取り出す際はStringTokenizerで
     */
	private void printSvfSaveListDetail(final ResultSet rs, final List data_lst) {

		try {
			String strx = null;
            final StringBuffer stb = new StringBuffer();

			stb.append(rs.getString("HR_NAME") + "-" + dmf1.format(rs.getInt("ATTENDNO")));	//組・出席番号
			stb.append(",");

			stb.append(rs.getString("NAME"));													//生徒名
			stb.append(",");

			strx = "   " + String.valueOf(rs.getInt("ABSENT")) + "日";
			stb.append(strx.substring(strx.length() - 4, strx.length()));					//欠課日数

			strx = "   " + String.valueOf(rs.getInt("MLESSON"));
			stb.append("(" + strx.substring(strx.length() - 3, strx.length()) + ")");		//授業日数

			data_lst.add( stb.toString());

		} catch (SQLException ex) {
            log.error("error! ", ex);
		}
	}


	/** 
     *  学年別出力処理-->学級ごとの処理-->生徒明細をSVF-FORMへ出力 
     */
	private void svfprintDetailOut(final List data_lst) {

		try {
			int ret = 0;
			svf.VrsOut("subject1",  "欠席");
			String strx = "   " + String.valueOf(data_lst.size()) + "人";
			svf.VrsOut("late",      "(" + strx.substring(strx.length() - 4, strx.length()) + ")");	//人数
			ret = svf.VrEndRecord();
			if (ret == 0) { nonedata = true; }

			for (final Iterator itr = data_lst.iterator(); itr.hasNext();) {
				final StringTokenizer stk = new StringTokenizer((String) itr.next(), ",");
				if (stk.hasMoreTokens()) {
					svf.VrsOut("HR_CLASS", stk.nextToken());				//組・出席番号
				}
				if (stk.hasMoreTokens()) {
					svf.VrsOut("name1",    stk.nextToken());				//生徒名
				}
				if (stk.hasMoreTokens()) {
					svf.VrsOut("times1",   stk.nextToken());				//欠課時数
				}
				ret = svf.VrEndRecord();
				if (ret == 0) { nonedata = true; }
			}
		} catch (Exception ex) {
            log.error("error! ", ex);
		}
	}


	/** 
     *  学年別出力処理-->欠席日数超過者を削除
     *  2005/03/17
     */
	private void deleteOver(final String grade)
	{
        final String sql = "delete from attend_semes_over_dat WHERE  YEAR = '" + param[0] + "' AND grade = '" + grade + "' ";
        log.debug("sql = " + sql);
        try{
            db2.executeUpdate(sql);   // UPDATE文実行
        } catch (Exception ex) {
            ex.printStackTrace();
            db2.commit();
            //throw excp;
        }
	}


	/** 
     *  学年別出力処理-->欠席日数超過者を登録
     *  2005/03/17
     */
	private void updateOver(final ResultSet rs, final String grade)
	{

        try{
			ps2.setString(1, param[0]);
			ps2.setString(2, grade );
			ps2.setString(3, rs.getString("SCHREGNO"));
            ps2.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            db2.commit();
            //throw excp;
        }
	}


	/** 
     *  PrepareStatement作成-->超過者を登録
     *	2005/03/17
     */
	private String prestatementupdate() {
		return "INSERT INTO attend_semes_over_dat (year,grade,schregno) VALUES (?,?,?) ";
	}


	/** 
     *  PrepareStatement作成-->学年別(学年＆学級ごと)長欠生徒取得 
     *	2005/03/07Modify 成績一覧表（レーザー）を参考にして作成
     */
	private String prestatementAbsentData() {

		final StringBuffer stb = new StringBuffer();
		try {
			//対象学籍の表
			stb.append("WITH SCHNO AS(");
			stb.append(		"SELECT  W1.SCHREGNO, ATTENDNO, W4.NAME, HR_NAME, ");
            //stb.append(             "CASE WHEN W2.SCHREGNO IS NOT NULL THEN 1 WHEN W3.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");
            stb.append(             "CASE WHEN W2.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");  //<change specification of n-times>
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
			stb.append(			    "INNER JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
			stb.append(			    "INNER JOIN SCHREG_REGD_HDAT W5 ON W5.YEAR = W1.YEAR AND ");
			stb.append(										          "W5.SEMESTER = W1.SEMESTER AND ");
			stb.append(										          "W5.GRADE = W1.GRADE AND ");
			stb.append(										          "W5.HR_CLASS = W1.HR_CLASS ");
                            //05/10/08 Modify 転学を追加<change specification of n-times>
            stb.append(     "LEFT    JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                   "AND ((W2.GRD_DIV IN('2','3') AND W2.GRD_DATE < '" + param[8] + "') ");   //<change specification of n-times>
            stb.append(                                     "OR (W2.ENT_DIV IN('4','5') AND W2.ENT_DATE > '" + param[8] + "')) ");  //<change specification of n-times>
                            //05/10/08 Modify 留学・休学を追加
            //stb.append("LEFT    JOIN SCHREG_TRANSFER_DAT W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            //stb.append(                                  "AND (TRANSFERCD IN('1','2') AND '" + param[8] + "' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");  //<change specification of n-times>
            stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "W1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(             "W1.GRADE = ? AND ");
            stb.append(             "W1.HR_CLASS = ? ");
            stb.append(     "), ");


            //対象生徒の時間割データ NO101 元のSCHEDULE_SCHREG
            //05/10/08Modify <change specification of n-times>
            stb.append("SCHEDULE_SCHREG_R AS(");
            stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN '" + param[6] + "' AND '" + param[8] + "' ");
            stb.append(         "AND T1.YEAR = T2.YEAR ");
            stb.append(         "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(         "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");    //05/10/07Build NOT EXISTS
            stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(                            "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                              "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE ))) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (_hasSchChrDatExecutediv) {
                stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
            }
            stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "), ");

            //対象生徒の時間割データ  NO101
            //05/10/08Modify <change specification of n-times>
            stb.append("SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T1.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");    //--NO101 Build 'NOT EXISTS〜'
            stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                            "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
            stb.append(     "), ");

            //対象生徒の出欠データ
            stb.append("T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, L1.REP_DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0 ");
            stb.append("             LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T0.DI_CD, ");
            stb.append(             "SCHEDULE_SCHREG T1 ");
            stb.append(     "WHERE   T0.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T0.ATTENDDATE BETWEEN '" + param[6] + "' AND '" + param[8] + "' AND ");
            stb.append(             "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(             "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T0.PERIODCD = T1.PERIODCD ");
            stb.append(     "), ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append("T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     "), ");

            //留学日数を算出 05/02/02
            //05/10/08Modify 休学も追加 <change specification of n-times>
            stb.append("OFFDAYS_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE "); //05/03/09
            stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCHEDULE_SCHREG_R T1 ");   //05/10/07Modify  NO101
            //NO101 stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCHEDULE_SCHREG T1 ");   //05/10/07Modify
            stb.append(     "WHERE   T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(         "AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(         "AND T3.TRANSFERCD IN('1') ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ");  //05/03/09
            stb.append(     "), ");
          /* ***  05/10/08 Delete
            //異動者（退学・転学）不在日数を算出 05/02/02
            stb.append("LEAVE_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO,COUNT(DISTINCT T1.EXECUTEDATE)AS LEAVE_DATE ");
            stb.append(     "FROM    SCHREG_BASE_MST T3, SCH_CHR_DAT T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN '" + param[0] + "-04-01" + "' AND '" + param[8] + "' AND ");
            stb.append(             "T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "T3.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append(             "T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(             "T3.GRD_DIV IN('2','3') AND ");
            //stb.append(             "T1.EXECUTEDATE BETWEEN T3.GRD_DATE AND '" + param[8] + "' ");
			stb.append(             "T3.GRD_DATE < T1.EXECUTEDATE AND ");   				//05/03/09Modify異動日は在籍とする
			stb.append(             "T1.EXECUTEDATE <= '" + param[8] + "' ");				//05/03/09Modify異動日は在籍とする
            stb.append(     "GROUP BY T3.SCHREGNO ");
            stb.append(     "), ");
          *** */

			//出欠の表
			stb.append("ATTEND AS(");
            stb.append(   "SELECT  TT0.SCHREGNO, ");
									//出席すべき日数 05/10/08Modify 
            stb.append(           "( VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0)) ");  //NO101
            if ("true".equals(param[11])) {
                stb.append(           " - VALUE(TT3_1.VIRUS,0) - VALUE(TT7.VIRUS,0) ");
            }
            if ("true".equals(param[12])) {
                stb.append(           " - VALUE(TT3_2.KOUDOME,0) - VALUE(TT7.KOUDOME,0) ");
            }
            stb.append(            " + ( VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) - VALUE(TT7.OFFDAYS,0) ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._semOffDays)) {
                stb.append(        " + VALUE(TT8.TRANSFER_DATE,0) + VALUE(TT7.OFFDAYS, 0)");
            }
            stb.append(               " ) AS MLESSON, ");
            //NO101 stb.append(           "( VALUE(TT1.LESSON,0) - VALUE(TT8.TRANSFER_DATE,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0)) ");
            //NO101 stb.append(            " + ( VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0)) AS MLESSON, ");
                                    /* ***
            stb.append(           "VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ");
            stb.append(           " + VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
            stb.append(           " - VALUE(TT9.LEAVE_DATE,0) ");
            stb.append(           " - VALUE(TT8.TRANSFER_DATE,0) AS MLESSON, ");
                                    *** */
									//欠席日数
            stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
            stb.append(           "VALUE(TT3_1.VIRUS,0) + VALUE(TT7.VIRUS,0) AS VIRUS, ");
            stb.append(           "VALUE(TT3_2.KOUDOME,0) + VALUE(TT7.KOUDOME,0) AS KOUDOME, ");
            stb.append(           "VALUE(TT4.MOURNING,0) + VALUE(TT7.MOURNING,0) AS MOURNING, ");
            stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._semOffDays)) {
                stb.append(       " + VALUE(TT8.TRANSFER_DATE, 0) ");
            }
            stb.append(           " + VALUE(TT7.ABSENT,0) AS ABSENT ");
            stb.append(   "FROM    SCHNO TT0 ");
            //個人別授業日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(      "FROM    T_PERIOD_CNT ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO ");
            //個人別出停日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('2','9') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            //個人別出停伝染病日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS VIRUS ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('25','26') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3_1 ON TT0.SCHREGNO = TT3_1.SCHREGNO ");
            //個人別出停交止日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('19','20') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
            //個人別忌引日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  REP_DI_CD IN ('3','10') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");
            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W0.SCHREGNO, ");
            stb.append(              "SUM(CASE L1.REP_DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(              "SUM(CASE L1.REP_DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(              "SUM(CASE L1.REP_DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(      "FROM    ATTEND_DAT W0 ");
            stb.append(         "INNER JOIN (");
            stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(         "FROM    T_PERIOD_CNT T0, ");
            stb.append(            "(");
            stb.append(            "SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    T_ATTEND_DAT W1 ");
            stb.append(            "WHERE   W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
            stb.append(            "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(            ") T1 ");
            stb.append(         "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(         ") W1 ON W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            stb.append("      LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = W0.DI_CD ");
            stb.append(      "GROUP BY W0.SCHREGNO ");
            stb.append(      ") TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");
            //月別集計データから集計した表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT SCHREGNO, ");
            stb.append(                   "VALUE(SUM(LESSON),0) AS LESSON, ");  //05/10/08 Modify  NO101
            //NO101 stb.append(                   "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) AS LESSON, ");  //05/10/08 Modify
            //stb.append(                   "SUM(LESSON) AS LESSON, ");
            stb.append(                   "SUM(OFFDAYS) AS OFFDAYS, ");
            stb.append(                   "SUM(MOURNING) AS MOURNING, ");
            stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
            if ("true".equals(param[11])) {
                stb.append(                   "SUM(VIRUS) AS VIRUS, ");
            } else {
                stb.append(                   "0 AS VIRUS, ");
            }
            if ("true".equals(param[12])) {
                stb.append(                   "SUM(KOUDOME) AS KOUDOME, ");
            } else {
                stb.append(                   "0 AS KOUDOME, ");
            }
            stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._semOffDays)) {
                stb.append(               "+ VALUE(OFFDAYS,0)");
            }
            stb.append(                       ") AS ABSENT, ");
            stb.append(                   "SUM(LATE) AS LATE, ");
            stb.append(                   "SUM(EARLY) AS EARLY ");
            stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
            stb.append(            "WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(                   "SEMESTER <= '" + param[1] + "' AND ");
            stb.append(                   "SEMESTER||MONTH <= '" + param[7] + "' AND ");
            stb.append(                   "EXISTS(");
            stb.append(                       "SELECT  'X' ");
            stb.append(                       "FROM    SCHNO W2 ");
            stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(            "GROUP BY SCHREGNO ");
            stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");
            //休学日数の表
            stb.append(   "LEFT JOIN OFFDAYS_SCHREG TT8 ON TT8.SCHREGNO=TT0.SCHREGNO ");
            //異動者不在日数の表
            //stb.append(   "LEFT JOIN LEAVE_SCHREG TT9 ON TT9.SCHREGNO=TT0.SCHREGNO ");
            stb.append(     ") ");

			/* メイン表
             *   ATTEND表より出席すべき日数の１／３(分母は選択)以上の者を抽出
             *   SCHNO1表のLEAVEにより対象学期終了日における在籍者(=0)と異動者(=1)を判別
             */
            stb.append("SELECT  ATTENDNO, NAME, HR_NAME, ");
			stb.append(		   "MLESSON, ");
			stb.append(		   "ABSENT ");
			stb.append(        ",w1.schregno ");    //05/03/17
            stb.append("FROM    ATTEND W1, SCHNO W2 ");
			stb.append("WHERE   CASE WHEN 0 < MOD(VALUE(MLESSON,0)," + param[5] + ") THEN 1 + VALUE(MLESSON,0) / " + param[5] + " ELSE VALUE(MLESSON,0) / " + param[5] + " END <= ABSENT AND ");  //NO102
            stb.append(        "LEAVE = 0 AND ");
			//NO102 stb.append("WHERE   VALUE(MLESSON,0) / " + param[5] + " <= ABSENT AND LEAVE = 0 AND ");
			stb.append(		   "W1.SCHREGNO = W2.SCHREGNO AND ");
			stb.append(		   "W2.LEAVE = 0 ");
            stb.append(    "AND 0 < VALUE(MLESSON,0) ");  //NO101

			stb.append("ORDER BY ATTENDNO");

		} catch (Exception ex) {
            log.error("error! ", ex);
		}
		return stb.toString();

	}//prestatementHrclass()の括り

	/** 
     *  PrepareStatement作成-->該当学年の学級取得 
	 *    学年別学級の表  ホスト変数-->学年、組
     *	2005/03/07Modify
     */
	String prestatementHrclass() {

		final StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT W1.GRADE, W1.HR_CLASS ");
			stb.append("FROM   SCHREG_REGD_HDAT W1 ");
			stb.append("WHERE  W1.YEAR = '" + param[0] + "' AND ");
            stb.append(       "W1.SEMESTER = '" + param[1] + "' AND ");
			stb.append(		  "W1.GRADE = ? ");
			stb.append("ORDER BY W1.HR_CLASS");
		} catch (Exception ex) {
            log.error("error! ", ex);
		}
		return stb.toString();
	}


}//クラスの括り
