/**
 *
 *	学校教育システム 賢者 [出欠管理] 長欠者要注意者リスト
 *
 *	2004/08/29 yamashiro
 *  2004/12/15 yamashiro 月の集計日付範囲の仕様変更
 *  2005/09/06 yamashiro 処理速度改善 => ATTEND_SEMES_DAT + ATTEND_DAT(端数処理) より出欠日数を算出
 *  2006/03/08 yamashiro ○残作業?117の未対応について対応 --NO101
 *  
 * @version $Id: f6e24926863decc90c38778e7e868a14ec5a4329 $
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC150K_CHOKETU {

    private static final Log log = LogFactory.getLog(KNJC150K_CHOKETU.class);

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private DecimalFormat dmf1 = new DecimalFormat("00");
	private int pline;					//出力行数カウント

    private String frommonth[];   //出力対象月 04/12/14
    private String tomonth[];     //出力対象月 04/12/14
	private	Calendar cals = Calendar.getInstance();
	private	Calendar cale = Calendar.getInstance();
	private	PreparedStatement ps1, ps2, ps3;
	public boolean nonedata;
    private KNJSchoolMst knjSchoolMst;
    private boolean _hasSchChrDatExecutediv;

    /**
	 *  HTTP Get リクエストの処理
	 */
	void printSvf(final DB2UDB db2, final Vrw32alp svf, final String[] param, final String[] pselect) {
        _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
		//boolean nonedata = false;			//該当データなしフラグ
        loadSchoolMst(db2, param[0]);
		setHead(db2, svf, param);			//見出し出力のメソッド
		if (printSvfMain(db2, svf, param, pselect)) {
		    nonedata = true;	//学年別出力  pselectは対象学年
		}
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

	/** 見出し項目等 **/
	private void setHead(final DB2UDB db2, final Vrw32alp svf, final String[] param) {

		svf.VrSetForm("KNJC140.frm", 4);				//共通フォーム
		svf.VrsOut("NENDO",   nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");//年度
		svf.VrsOut("PRGID",   "KNJC150");
		svf.VrsOut("TITLE",	"長欠者要注意者リスト");

        //	ＳＶＦ属性変更--->改ページ
		svf.VrAttribute("GRADE",   "FF=1");	//対象
		svf.VrAttribute("SEMESTER","FF=1");	//集計期間

        //	作成日(現在処理日)の取得
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = getinfo.Control(db2);
			svf.VrsOut("ymd",KNJ_EditDate.h_format_JP(returnval.val3));		//作成日
		} catch (Exception ex) {
            log.error("error! ", ex);
		}

        //  対象月を保存 04/12/14
        setTargetMonth(db2, param);

	}//setHead()の括り


	/** 
     *  対象月を保存 04/12/14
     */
	private void setTargetMonth(final DB2UDB db2, final String[] param) {

        final List arr1 = new ArrayList(); 
        final List arr2 = new ArrayList(); 
		try {
			cals.setTime(sdf.parse(param[3]+"-01"));	//開始日付
			cale.setTime(sdf.parse(param[4]+"-01"));	//終了日付
			ps2 = db2.prepareStatement( prestatementSemesterDate());  //学級別生徒欠課
            sdf.applyPattern("yyyy-MM");
            while (!cals.after(cale)) {
                //各月の集計範囲fromをセット
                if (cals.get(Calendar.MONTH) + 1 == 4) {      
                    arr1.add(new String(setSemesterDate(db2, param, 1, "SDATE")));
                } else if (cals.get(Calendar.MONTH) + 1 == 9) { 
                    arr1.add(new String(setSemesterDate(db2, param, 2, "SDATE")));
                } else if (cals.get(Calendar.MONTH) + 1 == 1) { 
                    arr1.add(new String(setSemesterDate(db2, param, 3, "SDATE")));
                } else {
                    arr1.add(new String(sdf.format(cals.getTime()) + "-02"));
                }
                //各月の集計範囲toをセット
                cals.add(Calendar.MONTH,1);
                if (cals.get(Calendar.MONTH) == 8) {
                    arr2.add(new String(setSemesterDate(db2, param, 1, "EDATE")));
                } else if (cals.get(Calendar.MONTH) == 12) {
                    arr2.add(new String(setSemesterDate(db2, param, 2, "EDATE")));
                } else if (cals.get(Calendar.MONTH) == 3) {
                    arr2.add(new String(setSemesterDate(db2, param, 3, "EDATE")));
                } else {
                    arr2.add(new String(sdf.format(cals.getTime()) + "-01"));
                }
            }
            frommonth = new String[arr1.size()];
            tomonth = new String[arr2.size()];
            for (int i = 0; i < arr1.size(); i++) {
                frommonth[i] = (String)arr1.get(i);
            }
            for (int i = 0; i < arr2.size(); i++) {
                tomonth[i] = (String)arr2.get(i);
            }
		} catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(ps2);
        }

        for (int i = 0; i < frommonth.length; i++) {
            log.debug("frommonth[" + i + "]=" + frommonth[i] + "  tomonth[" + i + "]=" + tomonth[i]);
        }
    }


	/** 
     *  学期の開始日・終了日取得
     */
	private String setSemesterDate(final DB2UDB db2, final String[] param, final int semester, final String field) {

	    ResultSet rs = null;
        String strdate = null;
		try {
			ps2.setString( 1, param[0] );	                    //年度
			ps2.setString( 2, String.valueOf(semester));	    //学期
			rs = ps2.executeQuery();
            if (rs.next()) { 
                if (field.equals("SDATE")) {
                    strdate = rs.getString(field);
                } else {
                    strdate = rs.getString(field);
                }
            }
		} catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return strdate;
    }


	/** 
     *  学年別出力処理 
     *    学年別出力は、Listに明細データを保存後、Listの要素を編集して印刷する。
	 *      =>  時間割が多いほどデータ読み込みに時間が掛かるので、RecordSet作成を１回に納める
     *  2005/09/06 Modify
     */
	private boolean printSvfMain(final DB2UDB db2, final Vrw32alp svf, final String[] param, final String[] pselect)
	{

        if (log.isTraceEnabled()) {
            for (int i = 0 ; i < param.length ; i++) {
                log.trace("param[" + i + "]=" + param[i]);
            }
        }
		boolean nonedata = false; 			//該当データなしフラグ
		ResultSet rs1 = null;
        sdf.applyPattern("yyyy-MM-dd");

		//SQL作成
		try {
			ps1 = db2.prepareStatement(prestatementAbsentNum(param));		//学級別生徒欠課
			ps3 = db2.prepareStatement(prestatementHrclass(param));		//学年別学級
		} catch (Exception ex ) {
            log.error("error! " + ex );
		}
        //log.debug("ps1="+ps1.toString());
		//学年ごと印刷範囲年月ごとに処理
		//学年ごと印刷範囲年月ごとに全学級を読込む --> データは学級ごとに処理  pselect[]は対象学年
		for (int i = 0 ; i < pselect.length ; i++) {
			//学年別対象年月別の処理
			for (int j = 0 ; j < frommonth.length ; j++) {
				final List data_lst = new LinkedList( ); 	//生徒明細
				//学年ごとにps3で学級を取得し処理する
				try {
                    log.debug("pselect="+pselect[i]);
					ps3.setString(1,pselect[i]);	//学年
					rs1 = ps3.executeQuery();
					while (rs1.next()) {
						if (setOutList(db2, svf, param, rs1.getString("GRADE"), rs1.getString("HR_CLASS"), data_lst, j)) {
						    nonedata = true;	//学級別出力処理
						}
					}
				} catch (Exception ex) {
                    log.error("error! ", ex);
                } finally {
                    DbUtils.closeQuietly(rs1);
                    db2.commit();
				}
				//長欠者がいれば明細印刷
                log.debug("data_lst.size="+data_lst.size());
				if (data_lst.size() > 0) {
					if (printsvfMeisai(svf, data_lst)) {
					    nonedata = true;		//長欠明細出力
					}
					data_lst.clear();
				}
			}//for()
		}//for()

		prestatementClose();	//preparestatementを閉じる
		return nonedata;

	}//boolean printSvfMain()の括り


	/** 学年別出力処理-->学級ごとの処理 **/
	private boolean setOutList(final DB2UDB db2, final Vrw32alp svf, final String[] param, final String grade, final String hrclass, final List data_lst, final int j)
	{
	//	初期設定
		int pp = 0;
		boolean nonedata = false;

		try {
            cals.setTime(sdf.parse( frommonth[j]));	//月終了日
    		svf.VrsOut("GRADE",    String.valueOf(Integer.parseInt(grade)) + "年生");	            //学年（改ページ）
			svf.VrsOut("SEMESTER", (cals.get(Calendar.MONTH) + 1) + "月(" + param[5] + "日以上)");	//学年（改ページ）
		} catch (Exception ex) {
            log.error("error! ", ex);
		}

		ResultSet rs = null;
        //	学級別長期欠課者の取得-->生徒明細データをリストへ保存
		try {
            log.debug("grade=" + grade + "  hrclass=" + hrclass + "  frommonth[j]=" + frommonth[j] + "-->tomonth[j]=" + tomonth[j]);
            //SQL作成メソッドprestatementAbsentNumの変更に伴いsetStringを変更
			pp = 0;
			ps1.setString(++pp, grade);			//学年
			ps1.setString(++pp, grade);			//学年
			ps1.setString(++pp, hrclass);		//組
            ps1.setDate(++pp, java.sql.Date.valueOf(tomonth[j]));   //印刷範囲to
			ps1.setString(++pp, frommonth[j].substring(5, 7));	    //対象月

            //log.debug("ps1 start");
			rs = ps1.executeQuery();
            //log.debug("ps1 end");

			while (rs.next()) {
			    setOutListDetail(rs,data_lst);
			}
		} catch (Exception ex ) {
            log.error("error! " , ex );
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
		}

		return nonedata;

	}//void setOutList()の括り


	/** 学年別出力処理-->学級ごとの処理-->生徒明細をListへ保存 **/
	private void setOutListDetail(final ResultSet rs, final List data_lst) {
        log.debug("check");
		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(rs.getString("HR_NAME") + "-" + dmf1.format(rs.getInt("ATTENDNO")));	//組・出席番号
			stb.append(",");
			stb.append(rs.getString("NAME"));											//生徒名
			stb.append(",(");
			final String strx = "   " + String.valueOf(rs.getInt("ABSENTCOUNT")) + "日";
			stb.append(strx.substring(strx.length() - 4, strx.length()) + ")");				//欠課日数
			//リストへ生徒明細をセット-->取り出す際はStringTokenizerで
			data_lst.add(stb.toString());
		} catch (SQLException ex) {
            log.error("error! " , ex);
		}

	}//void setOutListDetail()の括り


	/** 学年別出力処理-->学級ごとの処理-->生徒明細をSVF-FORMへ出力 **/
	private boolean printsvfMeisai(final Vrw32alp svf, final List data_lst) {

		boolean nonedata = false;
		try {
			int ret = 0;
			svf.VrsOut("subject1"	,"欠席");
			final String strx = "   " + String.valueOf(data_lst.size()) + "人";
			svf.VrsOut("late" 	,"(" + strx.substring(strx.length() - 4, strx.length()) + ")");	//人数
			ret = svf.VrEndRecord();
			if (ret == 0) {
			    nonedata = true;
			}
            log.debug("ret=" + ret);

			for (final Iterator itr = data_lst.iterator(); itr.hasNext();) {
			    final StringTokenizer stk = new StringTokenizer((String)itr.next(),",");
				if (stk.hasMoreTokens())
					ret = svf.VrsOut("HR_CLASS" ,stk.nextToken());				//組・出席番号
				if (stk.hasMoreTokens())
					ret = svf.VrsOut("name1" 	,stk.nextToken());				//生徒名
				if (stk.hasMoreTokens())
					ret = svf.VrsOut("times1" 	,stk.nextToken());				//欠課時数
				ret = svf.VrEndRecord();
				if (ret == 0) {
				    nonedata = true;
				}
			}

		} catch (Exception ex) {
            log.error("error! " , ex );
		}
		return nonedata;

	}//printsvfMeisai()の括り


	/** 
     *  PrepareStatement作成  学年別(学年＆学級ごと)長欠生徒取得 
     *
     *  2005/09/06 Modify 処理速度改善 => ATTEND_SEMES_DAT + ATTEND_DAT(端数処理) より出欠日数を算出
     */
	private String prestatementAbsentNum(final String[] param) {

	//	該当学級の長欠生徒表  ホスト変数-->学年、組、学年、組、学年、組、学年、授業日数
		final StringBuffer stb = new StringBuffer();
		try {
            //対象生徒
			stb.append("WITH SCHNO AS(");
			stb.append(		"SELECT  T1.SCHREGNO, T1.ATTENDNO, T4.HR_NAME, T5.NAME ");
            stb.append(     "FROM    SCHREG_REGD_DAT T1 ");
			stb.append(     "INNER JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = '" + param[0] + "' ");
            stb.append(                                   "AND T4.SEMESTER = '" + param[1] + "' ");
			stb.append(									  "AND T4.GRADE = ? AND T4.HR_CLASS = T1.HR_CLASS ");
			stb.append(     "INNER JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' ");
            stb.append(         "AND T1.SEMESTER = '" + param[1] + "' ");
            stb.append(         "AND T1.GRADE = ? AND T1.HR_CLASS = ? ");
            stb.append(     "), ");

            //対象生徒の時間割データ
            stb.append("SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T4.DI_CD, (CASE WHEN T3.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ");
            stb.append(     "FROM    SCH_CHR_DAT T1 ");
            stb.append(     "INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR AND ");
            stb.append(             "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(             "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(     "LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(             "T3.TRANSFERCD = '2' AND ");
            stb.append(             "T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "LEFT JOIN ATTEND_DAT T4 ON ");
            stb.append(             "T4.SCHREGNO = T2.SCHREGNO AND ");
            stb.append(             "T4.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T4.PERIODCD = T1.PERIODCD AND ");
            stb.append(             "T4.CHAIRCD = T1.CHAIRCD ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' ");
            stb.append(             "AND T2.SCHREGNO IN(SELECT W1.SCHREGNO FROM SCHNO W1 GROUP BY W1.SCHREGNO) ");
            stb.append(             "AND T1.EXECUTEDATE BETWEEN '" + param[6] + "' AND ? ");
//			if (definecode.usefromtoperiod )
//				stb.append(         "T1.PERIODCD IN " + param[18] + " AND ");            //05/04/16
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");    //--NO101 Build 'NOT EXISTS〜'
            stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(                            "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                              "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");    //--NO101 Build 'NOT EXISTS〜'
            stb.append(                        "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                            "AND T4.TRANSFERCD IN('1') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
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
            stb.append("                   LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            if (_hasSchChrDatExecutediv) {
                stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
            }
            stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T4.DI_CD, T3.SCHREGNO ");
            stb.append(     "), ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append("T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     ") ");

            //メイン表
            stb.append("SELECT  TT0.SCHREGNO, TT0.NAME, TT0.HR_NAME, TT0.ATTENDNO ");
            stb.append(       ",VALUE(TT5.ABSENT,0) + VALUE(TT7.ABSENT,0) AS ABSENTCOUNT ");
            stb.append("FROM    SCHNO TT0 ");
            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W0.SCHREGNO, ");
            stb.append(              "COUNT(*) AS ABSENT ");
            stb.append(      "FROM    ATTEND_DAT W0 ");
            stb.append(         "INNER JOIN (");
            stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(         "FROM    T_PERIOD_CNT T0, ");
            stb.append(            "(");
            stb.append(            "SELECT  W1.SCHREGNO, W1.EXECUTEDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    SCHEDULE_SCHREG W1 ");
            stb.append("                    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = W1.DI_CD ");
            stb.append(            "WHERE   L1.REP_DI_CD IN ('4','5','6','11','12','13') ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._semOffDays)) {
                stb.append(                     "OR W1.IS_OFFDAYS = '1' ");
            }
            stb.append(            "GROUP BY W1.SCHREGNO, W1.EXECUTEDATE ");
            stb.append(            ") T1 ");
            stb.append(         "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.EXECUTEDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(         ") W1 ON W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            stb.append(      "GROUP BY W0.SCHREGNO ");
            stb.append(      "HAVING 0 < COUNT(*) ");
            stb.append(      ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");

            //月別集計データから集計した表
            stb.append("LEFT JOIN(");
            stb.append(   "SELECT  SCHREGNO, ");
            stb.append(           "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._semOffDays)) {
                stb.append(            " + VALUE(OFFDAYS,0) ");
            }
            stb.append(           "   ) AS ABSENT ");
            stb.append(   "FROM    ATTEND_SEMES_DAT W1 ");
            stb.append(   "WHERE   YEAR = '" + param[0] + "' AND ");
            stb.append(           "MONTH = ? AND ");
            stb.append(           "EXISTS(SELECT  'X' ");
            stb.append(                  "FROM    SCHNO W2 ");
            stb.append(                  "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(   "GROUP BY SCHREGNO ");
            stb.append(   "HAVING 0 < SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0)");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._semOffDays)) {
                stb.append(            " + VALUE(OFFDAYS,0) ");
            }
            stb.append(                 ")");
            stb.append(  ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

            stb.append("WHERE " + param[5] + " <= VALUE(TT5.ABSENT,0) + VALUE(TT7.ABSENT,0) ");
			stb.append("ORDER BY ATTENDNO");

		} catch (Exception ex) {
            log.error("error! " , ex );
		}
		return stb.toString();

	}//prestatementAbsentNum()の括り


	/** PrepareStatement作成-->該当学年の学級取得 **/
	private String prestatementHrclass(final String[] param) {

	//	学年別学級の表  ホスト変数-->学年、組
		final StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT GRADE,HR_CLASS,HR_NAME ");
			stb.append("FROM   SCHREG_REGD_HDAT W1 ");
			stb.append("WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND ");
			stb.append(		  "W1.GRADE=? ");
			stb.append("ORDER BY W1.HR_CLASS ");

		} catch (Exception ex) {
            log.error("error! " , ex );
		}
		return stb.toString();

	}//prestatementHrclass()の括り

    /**
     * 学校マスタをロードする
     * @param db2 DB2
     * @param year 年度
     */
    public void loadSchoolMst(final DB2UDB db2, final String year) {
        try {
            knjSchoolMst = new KNJSchoolMst(db2, year);
        } catch (SQLException ex) {
            log.error("loadSchoolMst exception!", ex);
        }
    }
    
	/** 
     *  PrepareStatement作成 学期の開始日・終了日取得
     */
	private String prestatementSemesterDate() {

	    String str = "SELECT SDATE,EDATE FROM SEMESTER_MST WHERE YEAR= ? AND SEMESTER= ? ";
		return str;
	}


	/**PrepareStatement close**/
	void prestatementClose()
	{
	    DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
        DbUtils.closeQuietly(ps3);
	}//prestatementClose()の括り


}//クラスの括り
