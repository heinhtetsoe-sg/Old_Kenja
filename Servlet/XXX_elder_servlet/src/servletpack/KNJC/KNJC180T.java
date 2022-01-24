/**
 *
 *    学校教育システム 賢者 [出欠管理] クラス別出欠検証リスト（東京都）
 *
 *    2006/02/03 o-naka 初回リリース
 *    2006/05/11 NO001 o-naka 欠席／出席／遅刻・早退にて、男子別・女子別を追加
 *    2006/05/12 NO002 o-naka 明細：男女の数字０は、空白表示とする。
 */

package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJAttendTerm;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;


public class KNJC180T {

    private static final Log log = LogFactory.getLog(KNJC180T.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private DecimalFormat dmf1 = new DecimalFormat("00");

    private boolean nonedata;
    private Calendar cals = Calendar.getInstance();
    private Calendar cale = Calendar.getInstance();

    private String _paramYear;
    private String _param1;
    private String _param2;
    private String _param3;
    private String _param4;
    private String _param5;
    private String _paramGrade;
    // 学校マスタ参照
    private KNJSchoolMst _knjSchoolMst;
    private String _useVirus;
    private String _useKoudome;
    private String _gradeCd;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        log.fatal("$Revision: 69703 $ $Date: 2019-09-13 21:31:40 +0900 (金, 13 9 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        Vrw32alp svf = new Vrw32alp();     // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                // Databaseクラスを継承したクラス

        // パラメータの取得
        try {
            _paramYear = request.getParameter("YEAR");                        //年度
            if (request.getParameter("month") != null) {
                _param1 = request.getParameter("month").substring(0, 1);   //学期
                _param2 = request.getParameter("month").substring(2, 4);   //月
                _param5 = request.getParameter("month").substring(5, 6);   //月の識別フラグ
            }
            _paramGrade = request.getParameter("GRADE");                       //学年
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        _useVirus = request.getParameter("useVirus");
        _useKoudome = request.getParameter("useKoudome");

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                            //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) {
            	db2.close();
            }
            return;
        }
        
        final Map<String, String> gdat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _paramYear + "' AND GRADE = '" + _paramGrade + "' "));
        _gradeCd = KnjDbUtils.getString(gdat, "GRADE_CD");
        final String schoolKind = KnjDbUtils.getString(gdat, "SCHOOL_KIND");

        // 印刷処理
        try {
        	final Map<String, Object> knjParamMap = new HashMap<String, Object>();
        	knjParamMap.put("SCHOOL_KIND", schoolKind);
            _knjSchoolMst = new KNJSchoolMst(db2, _paramYear, knjParamMap);
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }

        try {
            setTargetMonth(db2);               //出欠集計日範囲設定
            setHead(db2, svf);                    //見出し出力のメソッド

            printSvfMain(db2, svf);   //出欠統計出力のメソッド
        } catch (Exception ex) {
            log.error("printSvf error!", ex);
        } finally {
            // 終了処理
            if (!nonedata) {
            	svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }//try-cathの括り
        }
    }

    /**
     *  見出し項目等
     **/
    private void setHead(final DB2UDB db2, final Vrw32alp svf) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJC180T.frm", 4);                //共通フォーム
        svf.VrsOut("NENDO",  KNJ_EditDate.gengou(db2, Integer.parseInt(_paramYear)) + "年度");//年度
        svf.VrsOut("MONTH1",   String.valueOf(Integer.parseInt(_param2)) );//月
        svf.VrsOut("GRADE", NumberUtils.isDigits(_gradeCd) ? String.valueOf(Integer.parseInt(_gradeCd)) : NumberUtils.isDigits(_paramGrade) ? String.valueOf(Integer.parseInt(_paramGrade)) : StringUtils.defaultString(_paramGrade));//学年

        //    作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",  KNJ_EditDate.h_format_JP(db2, returnval.val3));        //作成日
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

        getinfo = null;
        returnval = null;

        try {
            sdf.applyPattern("yyyy-MM-dd");
            cals.setTime(sdf.parse( _param3 ) );    //範囲FROM
            cale.setTime(sdf.parse( _param4 ) );    //範囲TO
            int i = cals.get(Calendar.DATE) - 1;
            String week[] = {"","日","月","火","水","木","金","土"};

            while (!cals.after(cale)) {
                i++;
                svf.VrsOutn( "DAY"  , i,    String.valueOf( cals.get(Calendar.DATE) ) );  //日
                svf.VrsOutn( "WEEK" , i,    week[ cals.get( Calendar.DAY_OF_WEEK ) ] );   //曜日

                cals.add( Calendar.DATE, 1 );                        //翌日をセット
            }

        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }//setHead()の括り


    /**
     *  出欠集計の月別集計範囲日付を保存
     *      印刷指示画面から受取るパラメーターmonth'9-99-9'において、
     *        左端が学期、真中が月、右端が１は学期開始月２は学期終了月０は以外の月となっている
     */
    private void setTargetMonth(DB2UDB db2) {
        PreparedStatement ps = null;
        KNJAttendTerm obj = new KNJAttendTerm();
        try {
            //出欠集計の月別集計範囲日付を取得
            obj.setMonthTermDate(db2, _paramYear, _param1, Integer.parseInt(_param2), true);
            _param3 = obj.sdate;
            _param4 = obj.edate;
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(ps);
        }
    }


    /**
     *  統計出力処理
     */
    private void printSvfMain( DB2UDB db2, Vrw32alp svf) {
        try {
//            ps = db2.prepareStatement( prestatementAttend(param) );      //学級別生徒出欠データ
            printSvfMain2(db2, svf, _paramGrade, 1);  //学級別
            printSvfMain2(db2, svf, _paramGrade, 2);  //学年計
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /**
     *  統計出力処理
     */
    private void printSvfMain2(final DB2UDB db2, final Vrw32alp svf, final String hrclass, int flg) {
        //出力処理
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String classno = null;      //クラス番号の保存
            int attend[] = {0,0,0};     //クラス別出欠統計
            int attend1[] = {0,0,0};     //クラス別出欠統計・男子 NO001 Add
            int attend2[] = {0,0,0};     //クラス別出欠統計・女子 NO001 Add

            ps = db2.prepareStatement(prestatementAttend(flg));      //学級別生徒出欠データ
            ps.setString(1, hrclass);  //学年
            //log.debug("ps="+ps.toString());
            log.debug("sql = " + prestatementAttend(flg) );//NO001 Add
            log.debug("hrclass="+hrclass+"  flg="+flg+"   start");
            rs = ps.executeQuery();
            log.debug("hrclass="+hrclass+"  flg="+flg+"   end");
            while (rs.next()) {
                if (classno == null || ! rs.getString("GR_CL").equals(classno)) {
                    if (classno != null) {
                    	printSvfStdTotal(svf, attend, attend1, attend2);    //クラス別計 NO001 Modify
                    }
                    classno = rs.getString("GR_CL");
                    svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));     //クラス名称
                    for (int i = 0; i < attend.length; i++) {
                        attend[i] = 0;    //クラス別出欠統計の初期化
                        attend1[i] = 0;   //クラス別出欠統計の初期化・男子 NO001 Add
                        attend2[i] = 0;   //クラス別出欠統計の初期化・女子 NO001 Add
                    }
                }
                printSvfStdDetail( svf, rs, attend, attend1, attend2 );      //クラス別明細 NO001 Modify
            }
            if (classno != null) {
            	printSvfStdTotal( svf, attend, attend1, attend2 ); //クラス別計 NO001 Modify
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
            DbUtils.closeQuietly(ps);
        }

    }


    /**
     *  クラス別明細出力
     */
    private void printSvfStdDetail( Vrw32alp svf, ResultSet rs, int[] attend, int[] attend1, int[] attend2)
    {

        try {
            int i = Integer.parseInt( rs.getString("ATTENDDATE").substring(8, 10) );
            if( i == 1  &&  Integer.parseInt( rs.getString("ATTENDDATE").substring(5, 7) ) != Integer.parseInt( _param3.substring(5, 7))) {
            	i = 32;
            }
                
            //明細
            svf.VrsOutn( "ATTEND"     , i,     rs.getString("ATTEND") );  //出席
            svf.VrsOutn( "ABSENCE"    , i,     rs.getString("ABSENCE") ); //欠席
            svf.VrsOutn( "LATE"       , i,     rs.getString("LATE") );    //遅刻・早退

            if( 0 < Integer.parseInt( rs.getString("ATTEND1") ) )   //NO002 Add
                svf.VrsOutn( "ATTEND1"    , i,     rs.getString("ATTEND1") ); //男子 NO001 Add
            if( 0 < Integer.parseInt( rs.getString("ABSENCE1") ) )  //NO002 Add
                svf.VrsOutn( "ABSENCE1"   , i,     rs.getString("ABSENCE1") );//男子 NO001 Add
            if( 0 < Integer.parseInt( rs.getString("LATE1") ) )     //NO002 Add
                svf.VrsOutn( "LATE1"      , i,     rs.getString("LATE1") );   //男子 NO001 Add
            if( 0 < Integer.parseInt( rs.getString("ATTEND2") ) )   //NO002 Add
                svf.VrsOutn( "ATTEND2"    , i,     rs.getString("ATTEND2") ); //女子 NO001 Add
            if( 0 < Integer.parseInt( rs.getString("ABSENCE2") ) )  //NO002 Add
                svf.VrsOutn( "ABSENCE2"   , i,     rs.getString("ABSENCE2") );//女子 NO001 Add
            if( 0 < Integer.parseInt( rs.getString("LATE2") ) )     //NO002 Add
                svf.VrsOutn( "LATE2"      , i,     rs.getString("LATE2") );   //女子 NO001 Add

            //計
            attend[0] += Integer.parseInt( rs.getString("ATTEND") );
            attend[1] += Integer.parseInt( rs.getString("ABSENCE") );
            attend[2] += Integer.parseInt( rs.getString("LATE") );
            attend1[0] += Integer.parseInt( rs.getString("ATTEND1") );  //NO001 Add
            attend1[1] += Integer.parseInt( rs.getString("ABSENCE1") ); //NO001 Add
            attend1[2] += Integer.parseInt( rs.getString("LATE1") );    //NO001 Add
            attend2[0] += Integer.parseInt( rs.getString("ATTEND2") );  //NO001 Add
            attend2[1] += Integer.parseInt( rs.getString("ABSENCE2") ); //NO001 Add
            attend2[2] += Integer.parseInt( rs.getString("LATE2") );    //NO001 Add
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

    }


    /**
     *  クラス別計出力
     */
    private void printSvfStdTotal(final Vrw32alp svf, int[] attend, int[] attend1, int[] attend2) {
        try {
            svf.VrsOut( "TOTAL_ATTEND",       String.valueOf( attend[0] ) );  //出席
            svf.VrsOut( "TOTAL_ABSENCE",      String.valueOf( attend[1] ) );  //欠席
            svf.VrsOut( "TOTAL_LATE",         String.valueOf( attend[2] ) );  //遅刻・早退
            svf.VrsOut( "TOTAL_ATTEND1",      String.valueOf( attend1[0] ) ); //男子 NO001 Add
            svf.VrsOut( "TOTAL_ABSENCE1",     String.valueOf( attend1[1] ) ); //男子 NO001 Add
            svf.VrsOut( "TOTAL_LATE1",        String.valueOf( attend1[2] ) ); //男子 NO001 Add
            svf.VrsOut( "TOTAL_ATTEND2",      String.valueOf( attend2[0] ) ); //女子 NO001 Add
            svf.VrsOut( "TOTAL_ABSENCE2",     String.valueOf( attend2[1] ) ); //女子 NO001 Add
            svf.VrsOut( "TOTAL_LATE2",        String.valueOf( attend2[2] ) ); //女子 NO001 Add
            svf.VrEndRecord();
            nonedata = true;
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }


    /**
     *  PrepareStatement作成  クラス別出欠データ取得
     */
    private String prestatementAttend(int flg) {

        StringBuffer stb = new StringBuffer();
        try {

            //対象生徒
            stb.append("WITH SCHNO AS( ");
            stb.append("     SELECT  W1.SCHREGNO, W1.ATTENDNO, W1.GRADE, W1.HR_CLASS, W1.YEAR, W1.SEMESTER ");
                                    //対象校時(課程マスタの開始校時を参照)---データがない場合、名称マスタの区分'B001'の校時のMIN値を参照
            stb.append("            ,CASE WHEN W2.S_PERIODCD IS NOT NULL THEN W2.S_PERIODCD ");
            stb.append("                  ELSE (SELECT MIN(NAMECD2) FROM NAME_MST WHERE NAMECD1='B001') ");
            stb.append("                  END AS S_PERIODCD ");
                                    //対象校時(課程マスタの終了校時を参照)---データがない場合、名称マスタの区分'B001'の校時のMAX値を参照
            stb.append("            ,CASE WHEN W2.E_PERIODCD IS NOT NULL THEN W2.E_PERIODCD ");
            stb.append("                  ELSE (SELECT MAX(NAMECD2) FROM NAME_MST WHERE NAMECD1='B001') ");
            stb.append("                  END AS E_PERIODCD ");
            stb.append("     FROM    SCHREG_REGD_DAT W1  ");
            stb.append("             LEFT JOIN COURSE_MST W2 ON W2.COURSECD=W1.COURSECD ");
            stb.append("     WHERE   W1.YEAR = '" + _paramYear + "' AND  ");
            stb.append("               W1.SEMESTER = '" + _param1 + "' AND  ");
            stb.append("             W1.GRADE = ?  ");
            stb.append("     ), ");

            //対象生徒の時間割データ
            stb.append("SCHEDULE_DAT AS( ");
            stb.append("     SELECT  EXECUTEDATE,PERIODCD,YEAR,SEMESTER,CHAIRCD  ");
            stb.append("     FROM    SCH_CHR_DAT  ");
            stb.append("     WHERE   YEAR = '" + _paramYear + "' AND  ");
            stb.append("             EXECUTEDATE BETWEEN '" + _param3 + "' AND '" + _param4 + "'  ");
            stb.append("     GROUP BY EXECUTEDATE,PERIODCD,YEAR,SEMESTER,CHAIRCD  ");
            stb.append("     ), ");
            stb.append("SCHEDULE_SCHREG AS( ");
            stb.append("     SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD  ");
            stb.append("     FROM    SCHEDULE_DAT T1, CHAIR_STD_DAT T2  ");
            stb.append("     WHERE   T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND  ");
            stb.append("             T1.YEAR = T2.YEAR AND  ");
            stb.append("             T1.SEMESTER = T2.SEMESTER AND  ");
            stb.append("             T1.CHAIRCD = T2.CHAIRCD AND  ");
                                    //コアタイム内の集計(課程マスタの開始終了校時を参照)
            stb.append("             EXISTS (SELECT 'X' FROM SCHNO T3 WHERE T2.SCHREGNO=T3.SCHREGNO AND T1.PERIODCD BETWEEN T3.S_PERIODCD AND T3.E_PERIODCD) AND  ");
                                    //留学(1)・休学(2)期間は除外
            stb.append("             NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T3  ");
            stb.append("                        WHERE  T3.SCHREGNO = T2.SCHREGNO AND  ");
            stb.append("                               T3.TRANSFERCD IN('1','2') AND  ");
            stb.append("                               T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE) AND  ");
                                    //退学(2)・転学(3)・転入(4)・編入(5)期間は除外
            stb.append("             NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T3  ");
            stb.append("                        WHERE  T3.SCHREGNO = T2.SCHREGNO  ");
            stb.append("                           AND ((T3.GRD_DIV IN('2','3') AND T3.GRD_DATE < T1.EXECUTEDATE)  ");
            stb.append("                             OR (T3.ENT_DIV IN('4','5') AND T3.ENT_DATE > T1.EXECUTEDATE)) )  ");
            stb.append("     GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD  ");
            stb.append("     ),  ");

            //対象生徒の出欠データ
            stb.append("T_ATTEND_DAT AS( ");
            stb.append("     SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, T2.REP_DI_CD  ");
            stb.append("     FROM    ATTEND_DAT T0  ");
            stb.append("             INNER JOIN SCHEDULE_SCHREG T1 ON T0.YEAR = '" + _paramYear + "' AND  ");
            stb.append("                 T0.ATTENDDATE BETWEEN '" + _param3 + "' AND '" + _param4 + "' AND  ");
            stb.append("                 T0.SCHREGNO = T1.SCHREGNO AND  ");
            stb.append("                 T0.ATTENDDATE = T1.EXECUTEDATE AND  ");
            stb.append("                 T0.PERIODCD = T1.PERIODCD  ");
            stb.append("             INNER JOIN ATTEND_DI_CD_DAT T2 ON T2.YEAR = '" + _paramYear + "' AND T2.DI_CD = T0.DI_CD ");
            stb.append("     ),  ");

            //対象生徒の出欠データ（忌引・出停した日）
            stb.append("  T_ATTEND_DAT_B AS( ");
            stb.append(" SELECT ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE, ");
            stb.append("    MIN(T0.PERIODCD) AS FIRST_PERIOD, ");
            stb.append("    COUNT(DISTINCT T0.PERIODCD) AS PERIOD_CNT ");
            stb.append(" FROM ");
            stb.append("    T_ATTEND_DAT T0 ");
            stb.append(" WHERE ");
            stb.append("    REP_DI_CD IN('2','3','9','10'");
            if ("true".equals(_useVirus)) {
                stb.append("    , '19','20' ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append("    , '25','26' ");
            }
            stb.append("    ) ");
            stb.append(" GROUP BY ");
            stb.append("    T0.SCHREGNO, ");
            stb.append("    T0.ATTENDDATE ");
            stb.append(" ), ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append("T_PERIOD_CNT AS( ");
            stb.append("     SELECT  T1.SCHREGNO, T1.EXECUTEDATE,  ");
            stb.append("             MIN(T1.PERIODCD) AS FIRST_PERIOD,  ");
            stb.append("             MAX(T1.PERIODCD) AS LAST_PERIOD,  ");
            stb.append("             COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("     FROM    SCHEDULE_SCHREG T1  ");
            stb.append("     GROUP BY T1.SCHREGNO, T1.EXECUTEDATE  ");
            stb.append("     )  ");

            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                //対象生徒の日単位のデータ（忌引・出停した日）
                stb.append(", T_PERIOD_SUSPEND_MOURNING AS( ");
                stb.append(" SELECT ");
                stb.append("    T0.SCHREGNO, ");
                stb.append("    T0.EXECUTEDATE ");
                stb.append(" FROM ");
                stb.append("    T_PERIOD_CNT T0, ");
                stb.append("    T_ATTEND_DAT_B T1 ");
                stb.append(" WHERE ");
                stb.append("        T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T0.EXECUTEDATE = T1.ATTENDDATE ");
                stb.append("    AND T0.FIRST_PERIOD = T1.FIRST_PERIOD ");
                stb.append("    AND T0.PERIOD_CNT = T1.PERIOD_CNT ");
                stb.append("     )  ");
            }

            //メイン表
            if (flg == 1) {
                stb.append("SELECT ATTENDDATE, U2.GRADE||U2.HR_CLASS AS GR_CL, HR_NAME  ");
            }
            if (flg == 2) {
                stb.append("SELECT ATTENDDATE, U2.GRADE AS GR_CL, '学年計' AS HR_NAME  ");
            }
            stb.append("      ,SUM(CASE WHEN DICD = '0' THEN 1 ELSE 0 END) AS ATTEND ");    //クラス別日付別出席人数
            stb.append("      ,SUM(CASE WHEN DICD = '4' THEN 1 ELSE 0 END) AS ABSENCE ");   //クラス別日付別欠席人数
            stb.append("      ,SUM(CASE WHEN DICD IN ('15','23''24') THEN 1 ELSE 0 END) AS LATE ");     //クラス別日付別遅早人数
            stb.append("      ,SUM(CASE WHEN DICD = '0'  AND SEX = '1' THEN 1 ELSE 0 END) AS ATTEND1 ");    //男子 NO001 Add
            stb.append("      ,SUM(CASE WHEN DICD = '4'  AND SEX = '1' THEN 1 ELSE 0 END) AS ABSENCE1 ");   //男子 NO001 Add
            stb.append("      ,SUM(CASE WHEN DICD IN ('15','23''24') AND SEX = '1' THEN 1 ELSE 0 END) AS LATE1 ");      //男子 NO001 Add
            stb.append("      ,SUM(CASE WHEN DICD = '0'  AND SEX = '2' THEN 1 ELSE 0 END) AS ATTEND2 ");    //女子 NO001 Add
            stb.append("      ,SUM(CASE WHEN DICD = '4'  AND SEX = '2' THEN 1 ELSE 0 END) AS ABSENCE2 ");   //女子 NO001 Add
            stb.append("      ,SUM(CASE WHEN DICD IN ('15','23''24') AND SEX = '2' THEN 1 ELSE 0 END) AS LATE2 ");      //女子 NO001 Add
            stb.append("FROM( ");
                            //個人別日付別出席の表
                            //出席は、時間割で今日履修している生徒から、欠席・忌引・出停している生徒を引く---2006/02/03 宮城さんに確認済み
            stb.append("     SELECT '0' AS DICD, T1.SCHREGNO, T1.ATTENDDATE  ");
            stb.append("     FROM( ");
            stb.append("        SELECT SCHREGNO, EXECUTEDATE AS ATTENDDATE  ");
            stb.append("        FROM   SCHEDULE_SCHREG ");
            stb.append("        GROUP BY SCHREGNO,EXECUTEDATE  ");
            stb.append("        )T1 ");
            stb.append("     WHERE NOT EXISTS( ");
            stb.append("            SELECT 'X' FROM( ");
            stb.append("                 SELECT  '4' AS DICD, W0.SCHREGNO, W0.ATTENDDATE  ");
            stb.append("                 FROM    ATTEND_DAT W0,  ");
            stb.append("                   (SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD  ");
            stb.append("                    FROM    T_PERIOD_CNT T0,  ");
            stb.append("                      (SELECT  W1.SCHREGNO, W1.ATTENDDATE,  ");
            stb.append("                               MIN(W1.PERIODCD) AS FIRST_PERIOD,  ");
            stb.append("                               COUNT(W1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("                       FROM    T_ATTEND_DAT W1  ");
            stb.append("              WHERE ");
            stb.append("                  W1.REP_DI_CD IN ('4','5','6','11','12','13' ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                          ,'2','9','3','10' ");
                if ("true".equals(_useVirus)) {
                    stb.append("    , '19','20' ");
                }
                if ("true".equals(_useKoudome)) {
                    stb.append("    , '25','26' ");
                }
            }
            stb.append("                              ) ");
            stb.append("                       GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ");
            stb.append("                       ) T1  ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append("              SELECT ");
                stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append("              FROM ");
                stb.append("                  T_ATTEND_DAT W1 ");
                stb.append("              WHERE ");
                stb.append("                  W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
                stb.append("              GROUP BY ");
                stb.append("                  W1.SCHREGNO, ");
                stb.append("                  W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
            }
            stb.append("                    WHERE   T0.SCHREGNO = T1.SCHREGNO AND  ");
            stb.append("                            T0.EXECUTEDATE = T1.ATTENDDATE AND  ");
            stb.append("                            T0.FIRST_PERIOD = T1.FIRST_PERIOD AND  ");
            stb.append("                            T0.PERIOD_CNT = T1.PERIOD_CNT  ");
            stb.append("                    ) W1  ");
            stb.append("                 WHERE   W0.SCHREGNO = W1.SCHREGNO AND  ");
            stb.append("                         W0.ATTENDDATE = W1.EXECUTEDATE AND  ");
            stb.append("                         W0.PERIODCD = W1.FIRST_PERIOD  ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("                 GROUP BY W0.SCHREGNO, W0.ATTENDDATE  ");
            stb.append("                 )T2 ");
            stb.append("            WHERE T1.SCHREGNO=T2.SCHREGNO AND ");
            stb.append("                  T1.ATTENDDATE=T2.ATTENDDATE ) ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("       AND NOT EXISTS(SELECT 'X' FROM T_PERIOD_SUSPEND_MOURNING T3  ");
                stb.append("                      WHERE T1.SCHREGNO = T3.SCHREGNO AND T1.ATTENDDATE = T3.EXECUTEDATE)  ");
            } else {
                stb.append("       AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B T3  ");
                stb.append("                      WHERE T1.SCHREGNO = T3.SCHREGNO AND T1.ATTENDDATE = T3.ATTENDDATE)  ");
            }
                            //個人別日付別欠席の表
                            //欠席は、時間割で履修している講座の日単位の校時全てを欠席している生徒---日本語難しいなぁ、ソース解読して理解してね。
            stb.append("     UNION  ");
            stb.append("     SELECT  '4' AS DICD, W0.SCHREGNO, W0.ATTENDDATE  ");
            stb.append("     FROM    ATTEND_DAT W0,  ");
            stb.append("       (SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD  ");
            stb.append("        FROM    T_PERIOD_CNT T0,  ");
            stb.append("          (SELECT  W1.SCHREGNO, W1.ATTENDDATE,  ");
            stb.append("                   MIN(W1.PERIODCD) AS FIRST_PERIOD,  ");
            stb.append("                   COUNT(W1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("           FROM    T_ATTEND_DAT W1  ");
            stb.append("              WHERE ");
            stb.append("                  W1.REP_DI_CD IN ('4','5','6','11','12','13' ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                          ,'2','9','3','10' ");
                if ("true".equals(_useVirus)) {
                    stb.append("    , '19','20' ");
                }
                if ("true".equals(_useKoudome)) {
                    stb.append("    , '25','26' ");
                }
            }
            stb.append("                              ) ");
            stb.append("           GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ");
            stb.append("           ) T1  ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("         INNER JOIN ( ");
                stb.append("              SELECT ");
                stb.append("                  W1.SCHREGNO, W1.ATTENDDATE, ");
                stb.append("                  MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
                stb.append("                  COUNT(W1.PERIODCD) AS PERIOD_CNT ");
                stb.append("              FROM ");
                stb.append("                  T_ATTEND_DAT W1 ");
                stb.append("              WHERE ");
                stb.append("                  W1.REP_DI_CD IN ('4','5','6','11','12','13') ");
                stb.append("              GROUP BY ");
                stb.append("                  W1.SCHREGNO, ");
                stb.append("                  W1.ATTENDDATE ");
                stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.ATTENDDATE ");
            }
            stb.append("        WHERE   T0.SCHREGNO = T1.SCHREGNO AND  ");
            stb.append("                T0.EXECUTEDATE = T1.ATTENDDATE AND  ");
            stb.append("                T0.FIRST_PERIOD = T1.FIRST_PERIOD AND  ");
            stb.append("                T0.PERIOD_CNT = T1.PERIOD_CNT  ");
            stb.append("        ) W1  ");
            stb.append("     WHERE   W0.SCHREGNO = W1.SCHREGNO AND  ");
            stb.append("             W0.ATTENDDATE = W1.EXECUTEDATE AND  ");
            stb.append("             W0.PERIODCD = W1.FIRST_PERIOD  ");
            if ("1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("    AND (W1.SCHREGNO, W1.EXECUTEDATE) NOT IN (SELECT T0.SCHREGNO, T0.EXECUTEDATE FROM T_PERIOD_SUSPEND_MOURNING T0) ");
            }
            stb.append("     GROUP BY W0.SCHREGNO, W0.ATTENDDATE  ");
                            //個人別日付別遅刻の表１
                            //最小校時を欠席して、途中から出席している生徒。但し、忌引・出停した日は除外する。
            stb.append("     UNION  ");
            stb.append("     SELECT  '15' AS DICD, T0.SCHREGNO, T0.EXECUTEDATE AS ATTENDDATE  ");
            stb.append("     FROM    T_PERIOD_CNT T0  ");
            stb.append("       INNER JOIN( ");
            stb.append("           SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("           FROM    T_ATTEND_DAT W1  ");
            stb.append("           WHERE   W1.REP_DI_CD NOT IN ('0','14','15','16','23','24','29','30','31','32')  ");
            if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                AND NOT EXISTS ");
                stb.append("                        (SELECT ");
                stb.append("                             'X' ");
                stb.append("                         FROM ");
                stb.append("                             T_ATTEND_DAT_B W2 ");
                stb.append("                         WHERE ");
                stb.append("                             W2.SCHREGNO = W1.SCHREGNO ");
                stb.append("                             AND W2.ATTENDDATE = W1.ATTENDDATE ");
                stb.append("                        ) ");
            }
            stb.append("           GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ");
            stb.append("           )T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND  ");
            stb.append("                                                T0.PERIOD_CNT != T1.PERIOD_CNT  ");
            stb.append("       INNER JOIN( ");
            stb.append("           SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ");
            stb.append("           FROM    T_ATTEND_DAT  ");
            stb.append("           WHERE   REP_DI_CD IN ('4','5','6','11','12','13')  ");
            stb.append("           )T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND  ");
            stb.append("                                                T0.FIRST_PERIOD = T2.PERIODCD  ");
            stb.append("     GROUP BY T0.SCHREGNO, T0.EXECUTEDATE  ");
                            //個人別日付別遅刻の表２
                            //最小校時を遅刻している生徒。但し、忌引・出停した日は除外する。
            stb.append("     UNION  ");
            stb.append("     SELECT  '15' AS DICD, T0.SCHREGNO, T0.EXECUTEDATE AS ATTENDDATE  ");
            stb.append("     FROM    T_PERIOD_CNT T0  ");
            stb.append("       INNER JOIN( ");
            stb.append("           SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ");
            stb.append("           FROM    T_ATTEND_DAT W1  ");
            stb.append("           WHERE   REP_DI_CD IN ('15','23','24','29','31','32')  ");
            if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                AND NOT EXISTS ");
                stb.append("                        (SELECT ");
                stb.append("                            'X' ");
                stb.append("                         FROM ");
                stb.append("                            T_ATTEND_DAT_B W2 ");
                stb.append("                         WHERE ");
                stb.append("                            W2.SCHREGNO = W1.SCHREGNO ");
                stb.append("                            AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append("           )T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND  ");
            stb.append("                                                T0.FIRST_PERIOD = T2.PERIODCD  ");
                            //個人別日付別早退の表１
                            //初めから出席して、最大校時を欠席している生徒。但し、忌引・出停した日は除外する。
            stb.append("    UNION  ");
            stb.append("    SELECT  '15' AS DICD, T0.SCHREGNO, T0.EXECUTEDATE AS ATTENDDATE  ");
            stb.append("    FROM    T_PERIOD_CNT T0  ");
            stb.append("       INNER JOIN( ");
            stb.append("           SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT  ");
            stb.append("           FROM    T_ATTEND_DAT W1  ");
            stb.append("           WHERE   W1.REP_DI_CD NOT IN ('0','14','15','16','23','24','29','30','31','32')  ");
            if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                AND NOT EXISTS ");
                stb.append("                        (SELECT ");
                stb.append("                             'X' ");
                stb.append("                         FROM ");
                stb.append("                             T_ATTEND_DAT_B W2 ");
                stb.append("                         WHERE ");
                stb.append("                             W2.SCHREGNO = W1.SCHREGNO ");
                stb.append("                             AND W2.ATTENDDATE = W1.ATTENDDATE ");
                stb.append("                        ) ");
            }
            stb.append("           GROUP BY W1.SCHREGNO, W1.ATTENDDATE  ");
            stb.append("           )T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND  ");
            stb.append("                                                T0.PERIOD_CNT != T1.PERIOD_CNT  ");
            stb.append("       INNER JOIN( ");
            stb.append("           SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ");
            stb.append("           FROM    T_ATTEND_DAT  ");
            stb.append("           WHERE   REP_DI_CD IN ('4','5','6')  ");
            stb.append("           )T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND  ");
            stb.append("                                               T0.LAST_PERIOD = T3.PERIODCD  ");
            stb.append("    GROUP BY T0.SCHREGNO, T0.EXECUTEDATE  ");
                            //個人別日付別早退の表２
                            //最大校時を遅刻している生徒。但し、忌引・出停した日は除外する。
            stb.append("    UNION  ");
            stb.append("    SELECT  '15' AS DICD, T0.SCHREGNO, T0.EXECUTEDATE AS ATTENDDATE  ");
            stb.append("    FROM    T_PERIOD_CNT T0  ");
            stb.append("       INNER JOIN( ");
            stb.append("           SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD  ");
            stb.append("           FROM    T_ATTEND_DAT W1  ");
            stb.append("           WHERE   REP_DI_CD IN ('16', '30', '31', '32')  ");
            if (!"1".equals(_knjSchoolMst._syukessekiHanteiHou)) {
                stb.append("                AND NOT EXISTS ");
                stb.append("                        (SELECT ");
                stb.append("                            'X' ");
                stb.append("                         FROM ");
                stb.append("                            T_ATTEND_DAT_B W2 ");
                stb.append("                         WHERE ");
                stb.append("                            W2.SCHREGNO = W1.SCHREGNO ");
                stb.append("                            AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            }
            stb.append("           )T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND  ");
            stb.append("                                               T0.LAST_PERIOD = T3.PERIODCD  ");

            stb.append("    )U1,  ");
            stb.append("    SCHNO U2,  ");
            stb.append("    SCHREG_BASE_MST U3,  ");//NO001 Add
            stb.append("    SCHREG_REGD_HDAT U4  ");

            stb.append("WHERE U1.SCHREGNO = U2.SCHREGNO AND  ");
            stb.append("      U1.SCHREGNO = U3.SCHREGNO AND  ");//NO001 Add
            stb.append("      U4.YEAR = U2.YEAR AND U4.SEMESTER = U2.SEMESTER AND U4.GRADE = U2.GRADE AND U4.HR_CLASS = U2.HR_CLASS  ");
            if (flg == 1) {
                stb.append("GROUP BY U2.GRADE, U2.HR_CLASS, U1.ATTENDDATE, U4.HR_NAME ");//クラス別日付別の人数を集計
                stb.append("ORDER BY U2.GRADE, U2.HR_CLASS, U1.ATTENDDATE ");
            }
            if (flg == 2) {
                stb.append("GROUP BY U2.GRADE, U1.ATTENDDATE ");//学年別日付別の人数を集計
                stb.append("ORDER BY U2.GRADE, U1.ATTENDDATE ");
            }

        } catch( Exception ex ){
            log.error("error! " + ex );
        }
        return stb.toString();

    }//prestatementAttend()の括り

}//クラスの括り
