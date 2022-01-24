// kanji=漢字
/*
 * $Id: 514196574e3cf1420c4648e9d4c88605f49dc953 $
 *
 * 作成日: 2004/06/30
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Line;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *
 *  学校教育システム 賢者 [出欠管理]  出席簿（公簿）
 *
 *  2004/06/30 yamashiro・新様式としてKNJC040とは別途作成
 *  2004/08/18 yamashiro・講座クラスデータの同時展開の講座コードゼロに対応
 *  2004/08/31 yamashiro・新仕様に変更
 *  2004/10/06 yamashiro・１日遅刻１日早退の除外条件として公欠は含めない
 *  2004/10/28 yamashiro・'〜'出力の不具合を修正
 *  2004/11/01 yamashiro・忌引のある日でも遅刻、早退をカウントする
 *  2004/11/04 yamashiro・学期集計の遅刻、早退の条件を修正
 *  2004/11/08 yamashiro・学期集計の遅刻、早退の条件を修正
 *  2004/11/26 yamashiro・累計項目名に「までの計」を追加
 *  2005/01/07/yamashiro・講座名称出力において、６文字を超えた分は出力しない
 *
 *  2004/12/31 yamashiro・KNJC041を一部変更して作成
 *                        科目名に替えて講座名称出力対応
 *  2005/01/08 yamashiro・講座名を出力す場合、同時展開クラスは群名称を出力する
 *  2005/02/25 yamashiro・最初の校時の忌引は遅刻にカウントしない、最後の校時の忌引は早退にカウントしない、
 *  2005/03/23 yamashiro・KNJC042の新仕様版として作成
 *  2005/04/16 yamashiro・集計と明細の条件にと対象校時を追加
 *  2005/04/27 yamashiro・プログラムを共通化する => KNJZ/detail/KNJDefineCode.classを参照
 *                        ・時間割講座データ集計フラグを使用する場合と使用しない場合の処理を行う
 *                        ・課程マスタの開始終了校時を使用する場合と使用しない場合の処理を行う
 *  2006/02/07 yamashiro・印刷指示画面に「コアタイムのみ出力」「全て出力」の選択を追加  --NO006
 *
 */

public class KNJC043 {

    private static final Log log = LogFactory.getLog(KNJC043.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd");
    private static String FROM_TO_MARK = "\uFF5E";
    private static String SEMEALL = "9";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasdata = false;

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                         //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定
        } catch (IOException ex) {
            log.error("db new error:", ex);
        }

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) {
                db2.close();
            }
            return;
        }

        Param param = null;
        try {
            param = createParam(request, db2);

            // 印刷処理 集計表
            if (param._output1 != null) {
                if (new KNJC043_CALC(svf, param).printSvf(db2)) {
                    hasdata = true;
                }
            }

            if (param._output2 != null) {
                KNJC043_LIST p = new KNJC043_LIST(svf, db2, param);
                if (p.printSvf(db2)) {
                    hasdata = true;
                }

                if (param._check2 != null) {
                    if (p.printSvfSubclass(svf, db2)) {
                        hasdata = true;
                    }
                }
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            if (null != param) {
                param.close();
            }
            db2.commit();
            db2.close();
        }
    }   //doGetの括り

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 73721 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static String toDateString(final Date date) {
        return sdf.format(date);
    }

    private static Date toDate(final String date) {
        if (null != date) {
            try {
                return sdf.parse(date);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return null;
    }

    private static <A, B, C> TreeMap<B, C> getMappedMap(final Map<A, TreeMap<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static int getInt(final Map row, final String field, final int defaultValue) {
        final String str = KnjDbUtils.getString(row, field);
        if (!NumberUtils.isDigits(str)) {
            if (null != str) {
                throw new IllegalArgumentException("not integer field :" + field + ", value = " + str);
            }
            return defaultValue;
        }
        return Integer.parseInt(str);
    }

    private static java.sql.Date getDate(final Map row, final String field) {
        if (null == KnjDbUtils.getString(row, field)) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(KnjDbUtils.getString(row, field));
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return null;
    }

    private static Calendar toCalendar(final String date) {
        final Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(toDate(date));
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return cal;
    }

    private static class Term {
        final String _key;
        final String _sdate;
        final String _edate;
        final String _dateFromToString;
        Term(final String key, final String sdate, final String edate, final String dateFromToString) {
            _key = key;
            _sdate = sdate;
            _edate = edate;
            _dateFromToString = dateFromToString;
        }

        public static String dateFromToString(final DB2UDB db2, final String sdate, final String edate) {
            String dateFromTo = null;
            try {
                dateFromTo = KNJ_EditDate.h_format_JP(db2, sdate) + " " + FROM_TO_MARK + " " + KNJ_EditDate.h_format_JP(db2, edate);
            } catch (Exception ex) {
                log.warn("now date-get error!", ex);
            }
            return dateFromTo;
        }

        String dateRangeKey() {
            return _sdate + ":" + _edate;
        }

        private static int getCweek(final Calendar cal) {
            int cweek = cal.get(Calendar.DAY_OF_WEEK); // calの曜日を取得
            if (cweek == 1) {
                cweek = 8; // 日曜日は８と設定
            }
            return cweek;
        }

        /**
        *
        *  印刷範囲期間の再設定->指定日を含む月〜日まで->指定日を含む学期の範囲内
        *
        */
        private static Term getTermValue(final DB2UDB db2, final Param param) {

            // sdayを含む学期の開始日
            final String sqlSsemeSdate = " SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "' AND '" + param._date1 + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '" + SEMEALL + "' ";
            Date ssemeSdate = toDate(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlSsemeSdate)));

            // edayを含む学期の終了日
            final String sqlEsemeEdate = " SELECT EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "' AND '" + param._date2 + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '" + SEMEALL + "'";
            Date esemeEdate = toDate(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlEsemeEdate)));

            String startD = null;
            try {
                final Calendar cal = Calendar.getInstance();
                try {
                    if (ssemeSdate == null) {
                        ssemeSdate = toDate(param._date1); // 範囲開始日
                    }
                    cal.setTime(toDate(param._date1)); // 受け取った開始日付をCalendar calに変換
                } catch (Exception ex) {
                    log.error("exception!", ex);
                }
                int cweek1 = getCweek(cal); // calの曜日を取得
                if (Calendar.MONDAY < cweek1) {
                    cal.add(Calendar.DATE, - (cweek1 - Calendar.MONDAY)); // calが月曜日を越えたら去る月曜日をセット
                }
                startD = toDateString(max(cal.getTime(), ssemeSdate)); // dateとssemeSdateを比較し、dateが学期開始日より前なら学期開始日を範囲開始日とする
            } catch (Exception ex) {
                log.error("ReturnVal getTermValue 2 error!", ex);
            }

            String endD = null;
            try {
                Calendar cal = Calendar.getInstance();
                try {
                    if (esemeEdate == null) {
                        esemeEdate = toDate(param._date2); // 範囲終了日
                    }
                    cal = toCalendar(param._date2); // 受け取った終了日付をCalendar calに変換
                } catch (Exception ex) {
                    log.error("exception!", ex);
                }
                final int cweek2 = getCweek(cal); // calの曜日を取得
                if (param._onlySunday == null && cweek2 < 8) {
                    cal.add(Calendar.DATE, (8 - cweek2)); // calが日曜日でなければ来る日曜日をセット
                }
                endD = toDateString(min(cal.getTime(), esemeEdate)); // dateとesemeEdateを比較し、dateが学期終了日より後なら学期終了日を範囲終了日とする
            } catch (Exception ex) {
                log.error("ReturnVal getTermValue 2 error!", ex);
            }

            final Term term = new Term("TERM", startD, endD, Term.dateFromToString(db2, startD, endD));
            return term;
        }

        private static Date min(final Date date1, final Date date2) {
            return date1.before(date2) ? date1 : date2;
        }

        private static Date max(final Date date1, final Date date2) {
            return date1.after(date2) ? date1 : date2;
        }
    }

    public static class StudentAttendance {
        final String _schregno;
        final Map<String, Attendance> _semesAttendance;
        final Attendance _total;

        StudentAttendance(final String schregno) {
            _schregno = schregno;
            _semesAttendance = new HashMap<String, Attendance>();
            _total = new Attendance();
        }

        public void add(final String semester, final Attendance attendance) {
            if (null == semester) {
                return;
            }
            _semesAttendance.put(semester, attendance);
        }
    }

    public static class Attendance {
        String _semester;
        int _absent;            //公欠
        int _abroad;            //留学
        int _mourning;          //忌引
        int _suspend;           //出席停止
        int _koudome;           //出席停止交止
        int _virus;             //出席停止伝染病
        int _sick;            //欠席
        int _sickOnly;          //病欠
        int _noticeOnly;        //欠席（届けあり）
        int _nonoticeOnly;      //欠席（届けなし）
        int _late;              //遅刻
        int _early;             //早退
        int _lesson;            //授業日数
        int _mlesson;           //出席すべき日数
        int _present;           //出席日数
        int _jugyoLate;         //授業遅刻
        int _jugyoEarly;        //授業早退
        int _jugyoKekka;        //授業欠課

        public Attendance() {
            this(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public Attendance(
                final String semester,
                final int absent,
                final int abroad,
                final int mourning,
                final int suspend,
                final int koudome,
                final int virus,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int late,
                final int early,
                final int lesson,
                final int mlesson,
                final int present,
                final int jugyoLate,
                final int jugyoEarly,
                final int jugyoKekka
        ) {
            _semester = semester;
            _absent = absent;
            _abroad = abroad;
            _mourning = mourning;
            _suspend = suspend;
            _koudome = koudome;
            _virus = virus;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _late = late;
            _early = early;
            _lesson = lesson;
            _mlesson = mlesson;
            _present = present;
            _jugyoLate = jugyoLate;
            _jugyoEarly = jugyoEarly;
            _jugyoKekka = jugyoKekka;
        }

        public void add(final Attendance a) {
            _absent += a._absent;
            _abroad += a._abroad;
            _mourning += a._mourning;
            _suspend += a._suspend;
            _koudome += a._koudome;
            _virus += a._virus;
            _sick += a._sick;
            _sickOnly += a._sickOnly;
            _noticeOnly += a._noticeOnly;
            _nonoticeOnly += a._nonoticeOnly;
            _late += a._late;
            _early += a._early;
            _lesson += a._lesson;
            _mlesson += a._mlesson;
            _present += a._present;
            _jugyoLate += a._jugyoLate;
            _jugyoEarly += a._jugyoEarly;
            _jugyoKekka += a._jugyoKekka;
        }

        public String toString() {
            return "m:" + _mourning + " s:" + _suspend + " sick:" + _sick + " late:" + _late + " early:" + _early + " lesson:" + _lesson + " mlesson:" + _mlesson;
        }
    }

    private static class Student {
        public final Integer _line;
        public int _printline;
        public final String _schregno;
        public String _attendno;
        public String _name;

        public String _kbnDate2;
        public String _kbnDate2e;
        public String _kbnName2;
        public String _kbnDate1;
        public String _kbnName1;
        public String _kbnDiv1;

        public Map<String, Map<String, String>> _datePeriodKintaiMap = new HashMap();
        public Map<String, TreeMap<Integer, String>> _datePeriodKintaiMapMap = new HashMap();
        public Map<String, TreeMap<Integer, String>> _datePeriodRepDiCdMapMap = new HashMap();
        public Map<String, List<Integer>> _datePeriodListMap = new HashMap();
        public Map<String, String> _onedayAttendComment = new HashMap<String, String>();
        public Map<String, Integer> _onedayAttendAttributeTarget = new HashMap();
        public Map<String, Map<String, String>> _termAttendInfoMap = new HashMap<String, Map<String, String>>();
        final Map<String, TreeMap<String, TreeMap<String, String>>> _attendDiRemarkMap = new TreeMap();
        StudentAttendance _studentAttendance;
        public Student(final Integer line, final String schregno) {
            _line = line;
            _schregno = schregno;
        }
        public String toString() {
            return "Student(" + _line + ":" + _schregno + ")";
        }

        private TransferInfo getTransferInfo() {
            // 備考情報
            String info = null;

            // 異動情報の優先順位は 「留学・休学 > 退学・転学 > 転入・編入」 とする
            if (_kbnDate2 != null) {
                final Calendar cals = toCalendar(_kbnDate2);
                final Calendar cale = toCalendar(_kbnDate2e);
                info = sdf2.format(cals.getTime()) + FROM_TO_MARK + sdf2.format(cale.getTime()) + StringUtils.defaultString(_kbnName2);
            } else if (_kbnDate1 != null) {
                final Calendar cals = toCalendar(_kbnDate1);
                info = sdf3.format(cals.getTime()) + StringUtils.defaultString(_kbnName1);
            }
            if (null == info) {
                return null;
            }

            final TransferInfo tInfo = new TransferInfo();
            tInfo._containInfo = true;
            tInfo._info = info;
//            log.info(" transferinfo row = " + row);
            // 異動情報の優先順位は 「留学・休学 > 退学・転学 > 転入・編入」 とする
            if (_kbnDate2 != null) {
                tInfo._containSdate = true;
                tInfo._sdate = _kbnDate2;
                tInfo._containEdate = true;
                tInfo._edate = _kbnDate2e;
            } else if (_kbnDate1 != null) {
                if ("1".equals(_kbnDiv1)) {
                    tInfo._containEdate = true;
                    tInfo._edate = _kbnDate1;
                    tInfo._containLeave = true;
                    tInfo._leave = _kbnDiv1;   // 退学および転学の場合
                    // tInfo._edate = retSchTransferDateNext(kbnDate1);
                } else {
                    tInfo._containSdate = true;
                    tInfo._sdate = _kbnDate1;
                }
            }
            return tInfo;
        }

        /**
         *  PrepareStatement作成
         */
        private static String sqlStudent(final Param param) {

            //  出席番号、氏名
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  SCHREGNO ");
            stb.append(    "FROM    SCHREG_REGD_DAT ");
            stb.append(    "WHERE   YEAR = '" + param._year + "' ");
            stb.append(        "AND SEMESTER = '" + param._semester + "' ");
            stb.append(        "AND GRADE = '" + param._grade + "' ");
            stb.append(        "AND HR_CLASS = '" + param._hrClass + "' ");
            stb.append(") ");

            stb.append(",TRANSFER_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, ");
            stb.append(           " MAX(TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append(    "FROM    SCHREG_TRANSFER_DAT T1 ");
            stb.append(    "INNER JOIN V_SEMESTER_GRADE_MST T3 ON T1.TRANSFER_SDATE BETWEEN T3.SDATE AND T3.EDATE ");
            stb.append(    "INNER JOIN SCHNO_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(    "WHERE   T1.TRANSFERCD IN('1','2') ");
            stb.append(        "AND T3.YEAR = '" + param._year + "' ");
            stb.append(        "AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND T3.GRADE = '" + param._grade + "' ");
            stb.append(    "GROUP BY T1.SCHREGNO ");
            stb.append(") ");

            stb.append(",TRANSFER_B AS(");
            stb.append(    "SELECT  S1.SCHREGNO, S1.TRANSFER_SDATE, S1.TRANSFER_EDATE, S1.TRANSFERCD ");
            stb.append(    "FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append(    "WHERE   EXISTS(SELECT 'X' FROM TRANSFER_A S2 WHERE S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(                                                   "AND S1.TRANSFER_SDATE = S2.TRANSFER_SDATE) ");
            stb.append(") ");

            stb.append("SELECT  W1.SCHREGNO, ");
            stb.append("        W1.ATTENDNO, ");
            stb.append("        W3.NAME,");
            stb.append(        "CASE WHEN W4.GRD_DIV IS NOT NULL THEN '1' ELSE '0' END AS KBN_DIV1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN NMA003.NAME1 ");
            stb.append(                                         " ELSE NMA002.NAME1 END AS KBN_NAME1, ");
            stb.append(        "T_TRANS.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "T_TRANS.TRANSFER_EDATE AS KBN_DATE2E,");
            stb.append(        "NMA004.NAME1 AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER   JOIN SCHNO_A ON SCHNO_A.SCHREGNO = W1.SCHREGNO ");
            stb.append("INNER   JOIN V_SEMESTER_GRADE_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = W1.YEAR AND W2.GRADE = W1.GRADE ");
            stb.append("INNER   JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO AND (W4.GRD_DIV IN('2','3') OR W4.ENT_DIV IN ('4','5')) ");
            stb.append("LEFT    JOIN TRANSFER_B T_TRANS ON T_TRANS.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT    JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = W4.ENT_DIV ");
            stb.append("LEFT    JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = W4.GRD_DIV ");
            stb.append("LEFT    JOIN NAME_MST NMA004 ON NMA004.NAMECD1 = 'A004' AND NMA004.NAMECD2 = T_TRANS.TRANSFERCD ");
            stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append(        "AND W1.SEMESTER = '" + param._semester + "' ");
            stb.append(        "AND W1.GRADE = '" + param._grade + "' ");
            stb.append(        "AND W1.HR_CLASS = '" + param._hrClass + "' ");
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();

        }

        private static void setAttendOnedayInfo(final DB2UDB db2, final String date, final Param param, final StudentMap studentMap) {

            if ("1".equals(param._knjc043tMeisaiPrintAttendDayDat)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                if ("1".equals(param._use_attendDayDiCd_00)) {
                    stb.append("     CASE WHEN T1.DI_CD = '00' THEN '　' ELSE L1.DI_MARK END AS DI_MARK ");
                } else {
                    stb.append("     L1.DI_MARK ");
                }
                stb.append("   , T1.SCHREGNO ");
                stb.append("   , T1.DI_CD ");
                stb.append(" FROM ATTEND_DAY_DAT T1 ");
                if ("1".equals(param._use_attendDayDiCd_00)) {
                    stb.append(" LEFT JOIN ATTEND_DI_CD_DAT L1 ON ");
                } else {
                    stb.append(" INNER JOIN ATTEND_DI_CD_DAT L1 ON ");
                }
                stb.append("      L1.YEAR = T1.YEAR ");
                stb.append("    AND L1.DI_CD = T1.DI_CD ");
                stb.append(" INNER JOIN SEMESTER_MST L2 ON ");
                stb.append("      L2.YEAR = '" + param._year + "' ");
                stb.append("    AND L2.SEMESTER = '" + param._semester + "' ");
                stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON ");
                stb.append("      REGD.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND REGD.YEAR = T1.YEAR ");
                stb.append("    AND REGD.SEMESTER = L2.SEMESTER ");
                stb.append(" WHERE ");
                stb.append("  T1.ATTENDDATE = '" + date + "' ");
                stb.append("  AND REGD.GRADE = '" + param._grade + "' ");
                stb.append("  AND REGD.HR_CLASS = '" + param._hrClass + "' ");

                for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                    final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == student || null == student._line) {
                        continue;
                    }
                    final String diMark = KnjDbUtils.getString(row, "DI_MARK");
                    if (null != diMark) {
                        student._onedayAttendComment.put(date, diMark);
                        log.info(" check attend_day_dat : " + student._schregno  + " " + diMark);
                    }
                }
            } else {
                param._attendParamMap.put("grade", param._grade);
                param._attendParamMap.put("hrClass", param._hrClass);
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        date,
                        date,
                        param._attendParamMap
                        );

                for (final Map row : KnjDbUtils.query(db2, sql)) {

                    if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }

                    final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == student || null == student._line) {
                        continue;
                    }

                    String comment = "";
                    if (getInt(row, "SICK", 0) > 0) {
                        comment = "欠";
                    } else {
                        if (getInt(row, "SUSPEND", 0) + getInt(row, "VIRUS", 0) + getInt(row, "KOUDOME", 0) > 0) {
                            comment += "停";
                        }
                        if (getInt(row, "MOURNING", 0) > 0) {
                            comment += "忌";
                        }
                        if (getInt(row, "LATE", 0) > 0) {
                            comment += "遅";
                        }
                        if (getInt(row, "EARLY", 0) > 0) {
                            comment += "早";
                        }
                    }
                    if (comment.length() > 0) {
                        student._onedayAttendComment.put(date, comment);
                        log.info(" check onedayattend : " + KnjDbUtils.getString(row, "SCHREGNO")  + " " + comment);
                    }
                }
            }
        }

        /**
         *  学期集計およびＳＶＦ出力 累計へ累積
         */
        private static void setSumAttend(final DB2UDB db2, final Term term, final Param param, final StudentMap studentMap) {

            param._attendParamMap.put("grade", param._grade);
            param._attendParamMap.put("hrClass", param._hrClass);
            final String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    param._semester,
                    term._sdate,
                    term._edate,
                    param._attendParamMap
            );

            //DB読み込みと集計処理
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                    continue;
                }

                final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student || null == student._line) {
                    continue;
                }

                //学期集計の出力
                student._termAttendInfoMap.put(term.dateRangeKey(), row);
            }
        }

        private static void setAttendDiRemark(final DB2UDB db2, final Term term, final Param param, final StudentMap studentMap) {
            if (param._isOutputDebug) {
                log.info(" set attendDiRemark ");
            }

            final String sql = sqlAttendDiRemark(param, term);
            log.debug("sql = " + sql);

            for (final Map rs : KnjDbUtils.query(db2, sql)) {
                final String date = KnjDbUtils.getString(rs, "ATTENDDATE");
                final String attendno = KnjDbUtils.getString(rs, "ATTENDNO");
                final String periodcd = KnjDbUtils.getString(rs, "PERIODCD");
                final String remark = KnjDbUtils.getString(rs, "DI_REMARK");

                final Student student = studentMap.getStudent(KnjDbUtils.getString(rs, "SCHREGNO"));
                if (null == student) {
                    continue;
                }

                log.debug("date=" + date + ", attendno=" + attendno + ", periodcd=" + periodcd + ", name=" + student._name + ", remark=" + remark);

                getMappedMap(getMappedMap(student._attendDiRemarkMap, term.dateRangeKey()), date).put(periodcd, remark);
            }
        }

        private static String sqlAttendDiRemark(final Param param, final Term term) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append(    " T1.SCHREGNO, ");
            stb.append(    " T1.ATTENDDATE, ");
            stb.append(    " T1.PERIODCD, ");
            stb.append(    " T2.ATTENDNO, ");
            if (param._hasAttendDatDiRemarkCd) {
                stb.append(    " VALUE(C901.NAME1, T1.DI_REMARK) AS DI_REMARK ");
            } else {
                stb.append(    " T1.DI_REMARK ");
            }
            stb.append(" FROM ");
            stb.append(    " ATTEND_DAT T1 ");
            stb.append(    " INNER JOIN SCHREG_REGD_DAT T2 ");
            stb.append(        "  ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(        " AND T2.YEAR = '" + param._year + "' ");
            stb.append(        " AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append(        " AND T2.GRADE = '" + param._grade + "' AND T2.HR_CLASS = '" + param._hrClass + "' ");
            stb.append(    " INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            if (param._hasAttendDatDiRemarkCd) {
                stb.append(    " LEFT JOIN NAME_MST C901 ON C901.NAMECD1 = 'C901' ");
                stb.append(    "                        AND C901.NAMECD2 = T1.DI_REMARK_CD ");
            }

            stb.append(" WHERE ");
            stb.append(    " T1.ATTENDDATE BETWEEN '" + term._sdate + "' AND '" + term._edate + "' AND ");
            if (param._hasAttendDatDiRemarkCd) {
                stb.append(    " 0 < LENGTH(VALUE(C901.NAME1, T1.DI_REMARK)) AND ");
            } else {
                stb.append(    " 0 < LENGTH(T1.DI_REMARK) AND ");
            }
            stb.append(    " T1.YEAR='" + param._year + "' ");
            stb.append(" ORDER BY ");
            stb.append(    " T1.ATTENDDATE, ");
            stb.append(    " T2.ATTENDNO, ");
            stb.append(    " T1.PERIODCD ");

            return stb.toString();
        }
    }

    private static class StudentMap {
        final Map<String, Student> _studentMap;
        final TreeMap<Integer, Student> _printLineStudentMap;
        StudentMap(final Map studentMap) {
            _studentMap = studentMap;

            final TreeMap<Integer, Student> printLineStudentMap = new TreeMap<Integer, Student>();
            for (final Student st : _studentMap.values()) {
                printLineStudentMap.put(st._line, st);
            }
            _printLineStudentMap = printLineStudentMap;
        }

        public Student getStudentOfPrintLine(final Integer line) {
            return _printLineStudentMap.get(line);
        }

        public Student getStudent(final String schregno) {
            return _studentMap.get(schregno);
        }

        private List<Student> getStudentList(int startLine, final int maxLine) {
            final List<Student> studentList = new ArrayList<Student>();
            for (int line = startLine; line < startLine + maxLine; line++) {

                final Student student = getStudentOfPrintLine(new Integer(line));
                if (null != student && null != student._line) {

                    student._printline = student._line.intValue() - startLine + 1;

                    studentList.add(student);
                }
            }
            return studentList;
        }

        /**
         *  生徒情報の保存処理
         *    String lschname[]の前３桁に出席番号を挿入
         *    備考情報追加 Map hm3
         */
        private static StudentMap getStudentMap(final DB2UDB db2, final Param param) {

            final TreeMap<Integer, Student> printLineStudentMap = new TreeMap<Integer, Student>();  //学籍番号と通し行番号の保管
            final Map<String, Student> schregnoStudentMap = new HashMap<String, Student>();         //通し行番号と生徒名の保管

            //  生徒名等ResultSet作成
            final String sql = Student.sqlStudent(param);
//            log.info(" student sql = " + sql);

            int stdno = 0;
            //生徒情報をセット
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final int linei;
                if (param._output4 == null) {
                    linei = Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"));
                } else {
                    stdno++;
                    linei = stdno;
                }
                final Integer line = new Integer(linei);
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                if (!printLineStudentMap.containsKey(line)) {                 //取り合えず重複する行番号は除外！
                    final Student student = new Student(line, schregno);
                    schregnoStudentMap.put(student._schregno, student);    //学籍番号に行番号をセットする
                    student._attendno = StringUtils.right("000" + KnjDbUtils.getString(row, "ATTENDNO"), 3);
                    student._name = KnjDbUtils.getString(row, "NAME"); //行番号に出席番号と名前をセットする
                    printLineStudentMap.put(line, student);
                }
                final Student student = schregnoStudentMap.get(schregno);

                student._kbnDate2 = KnjDbUtils.getString(row, "KBN_DATE2");
                student._kbnDate2e = KnjDbUtils.getString(row, "KBN_DATE2E");
                student._kbnName2 = KnjDbUtils.getString(row, "KBN_NAME2");
                student._kbnDate1 = KnjDbUtils.getString(row, "KBN_DATE1");
                student._kbnName1 = KnjDbUtils.getString(row, "KBN_NAME1");
                student._kbnDiv1 = KnjDbUtils.getString(row, "KBN_DIV1");
            }

            return new StudentMap(schregnoStudentMap);
        }
    }

    private static class TransferInfo {
        public boolean _containInfo;
        public String _info;
        public boolean _containSdate;
        public String _sdate;
        public boolean _containEdate;
        public String _edate;
        public boolean _containLeave;
        public String _leave;

        private boolean isPrintBiko(final Term term) {
            boolean isPrint = false;
            if (_containEdate && !_containSdate) {
                if (!toCalendar(term._edate).before(toCalendar(_edate))) {
                    isPrint = true;
                }
            } else if (_containSdate && !_containEdate) {
                if (!toCalendar(term._edate).before(toCalendar(_sdate))) {
                    isPrint = true;
                }
            } else if (_containSdate && _containEdate) {

                if (!toCalendar(_sdate).before(toCalendar(term._sdate)) && !toCalendar(_sdate).after(toCalendar(term._edate))) {
                    isPrint = true;
                } else if(!toCalendar(_edate).before(toCalendar(term._sdate)) && !toCalendar(_edate).after(toCalendar(term._edate))) {
                    isPrint = true;
                } else if(toCalendar(_sdate).before(toCalendar(term._sdate)) && toCalendar(_edate).after(toCalendar(term._edate))) {
                    isPrint = true;
                }
            }
            return isPrint;
        }
    }

    private static class Period {
        final String _cd;
        final String _name;
        Period(final String cd, final String name) {
            _cd = cd;
            _name = name;
        }
        public String toString() {
            return "Period(" + _cd + ", " + _name + ")";
        }
    }

    private static class Form {
        final Vrw32alp _svf;
        final Param _param;
        public Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }

        public int setForm(final String form, final int k) {
            final int n = _svf.VrSetForm(form, k);
            if (_param._isOutputDebug) {

            }
            return n;
        }

        public int VrsOut(final String field, final String data) {
            final int n = _svf.VrsOut(field, data);
            return n;
        }

        public int VrsOutn(final String field, final int gyo, final String data) {
            final int n = _svf.VrsOutn(field, gyo, data);
            return n;
        }

        public int VrAttribute(final String field, final String data) {
            final int n = _svf.VrAttribute(field, data);
            return n;
        }

        public int VrAttributen(final String field, final int gyo, final String data) {
            final int n = _svf.VrAttributen(field, gyo, data);
            return n;
        }

        public int VrEndPage() {
            final int n = _svf.VrEndPage();
            return n;
        }
    }

    /**
    *
    *  学校教育システム 賢者 [出欠管理]  出席簿（公簿）
    *   集計表
    *
    *  2005/03/23 yamashiro・集計出力を新様式として作成
    *  2005/04/15 yamashiro・’〜’をUNICODEで出力
    *  2005/04/16 yamashiro・集計と明細の条件にと対象校時を追加
    *  2005/04/27 yamashiro・集計処理において時間割講座データ集計フラグの条件を追加 => KNJC043_BASEで対応
    *  2005/05/13 yamashiro・集計期間範囲において印刷範囲終了日が考慮されない不具合を修正
    *  2005/06/09 yamashiro・出席番号の欠番を詰めて出力する処理を追加
    *                      ・１４校時対応フォームを追加
    *  2005/10/22 yamashiro・印刷範囲日付と現在学期が異なる場合集計が出力されない不具合を修正
    *  2005/12/05 yamashiro・備考項目を追加（異動情報を出力）   --NO003
    *  2006/01/20 yamashiro・備考欄を追加し、異動情報を表示する --NO003 (05/12/05の仕様が再決定された）
    *  2006/01/27 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --NO004
    *  2006/02/07 yamashiro・異動情報は期間がある場合は期間を出力する --NO005
    */
    private static class KNJC043_CALC extends Form {

        private static final int MAX_LINE = 50;

        private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
        private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";

        private boolean hasdata;

        KNJC043_CALC(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        /**
         *   SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2) {

            final StudentMap studentMap = StudentMap.getStudentMap(db2, _param);

            setAttendance(db2, studentMap);

            printAttendance(studentMap);

            return hasdata;
        }

        /**
         *
         *  SVF-FORM設定
         */
        private String getSvfForm() {
            String form = null;
            if (Param.KNJC043B.equals(_param._prgid)) {
                form = "KNJC043B_2.frm";   //３学期制用
            } else if (_param._isTokiwagi) {
                form = "KNJC043_2TOKIWAGI.frm";   //２学期制用
            } else if (_param._isOsakashinnai) {
                form = "KNJC043_3OSAKASHINNAI.frm";   //２学期制用
            } else if (_param._isKumamoto) {
                if (2 < _param._semesterdiv) {
                    form = "KNJC043_9.frm";   //３学期制用
                } else {
                    form = "KNJC043_8.frm";   //２学期制用
                }
            } else {
                if (2 < _param._semesterdiv) {
                    form = "KNJC043_3.frm";   //３学期制用
                } else {
                    form = "KNJC043_2.frm";   //２学期制用
                }
            }
            return form;
        }

        /**
         *  出力処理＿見出し出力
         */
        private void printHeadSvf() {
            VrsOut("HR_NAME", _param._hrname);        //組名称
            if (_param._isSapporo && (null != _param._staffname && null != _param._teacher2)) {
                VrsOut("teacher2", _param._staffname); //担任名
                VrsOut("teacher3", _param._teacher2); //担任名
            } else {
                VrsOut("teacher", _param._staffname); //担任名
            }
            VrsOut("ymd1", _param._ctrlDateFormatJp); //作成日
            VrsOut("nendo", _param._nendo); //年度

            for (int i = 0; i < _param._semesterdiv && i < _param._semesternameList.size(); i++) {
                VrsOut("SEMESTER" + (i + 1), (String) _param._semesternameList.get(i));
            }
            VrsOut("SEMESTER" + (_param._semesterdiv + 1), "1 " + FROM_TO_MARK + " " + _param._semesterdiv + "学期累計");
            VrsOut("RANGE", _param._calcRange);

            VrsOut("NOTE", "※");
            VrAttribute("NOTE1",  AMIKAKE_ATTR1);
            VrsOut("NOTE1", " ");
            VrsOut("NOTE2", "：欠席超過");
            VrAttribute("NOTE3",  AMIKAKE_ATTR2);
            VrsOut("NOTE3", " ");
            VrsOut("NOTE4", "：欠席注意");

            // KNJC043Bのみ
            for (int i = 0; i < 4; i++) {
                VrsOut("KESSEKI_NAME" + String.valueOf(i + 1) + "_1", _param.getDicdDatNameMap("4"));
                VrsOut("KESSEKI_NAME" + String.valueOf(i + 1) + "_2", _param.getDicdDatNameMap("5"));
            }
        }

        /**
         *  出力処理＿生徒名等出力
         **/
        private void printStudentsname(final Student student, final int line) {

            if (student._attendno != null) {
                if ("1".equals(_param._use_SchregNo_hyoji)) {
                    VrsOutn("SCHREGNO", line, student._schregno);
                    VrsOutn("name2", line, student._name);
                } else  {
                    VrsOutn("name", line, student._name);
                }
                if (_param._isOutputDebug) {
                    log.info(" " + line + " = (" + student._attendno + ") " + student._name );
                }
                VrsOutn("NUMBER", line, String.valueOf(Integer.parseInt(student._attendno)));
            }
            //備考出力
            final TransferInfo transferInfo = student.getTransferInfo();
            if (null != transferInfo) {
                if (null != transferInfo._info) {
                    boolean isPrintBiko = false;
                    if (transferInfo._containEdate && !transferInfo._containSdate) {
                        if (!toCalendar(transferInfo._edate).after(toCalendar(_param._date2))) {
                            isPrintBiko = true;
                        }
                    } else if (transferInfo._containSdate) {
                        if (!toCalendar(transferInfo._sdate).after(toCalendar(_param._date2))) {
                            isPrintBiko = true;
                        }
                    }
                    if (isPrintBiko) {
                        VrsOutn("REMARK", line, transferInfo._info);  //備考
                    }
                }
            }
        }

        /**
         *  学期期間の取得
         */
        private Map getSemesterDate(final DB2UDB db2) {
            final Map semesterDateMap = new TreeMap();

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SEMESTER, SDATE, EDATE");
            stb.append(" FROM V_SEMESTER_GRADE_MST");
            stb.append(" WHERE YEAR = '" + _param._year + "' AND SEMESTER < '" + SEMEALL + "' AND GRADE = '" + _param._grade + "' ");
            stb.append(" ORDER BY SEMESTER");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String sdate = KnjDbUtils.getString(row, "SDATE");
                if (toCalendar(_param._date2).before(toCalendar(sdate))) { break; }

                getMappedMap(semesterDateMap, KnjDbUtils.getString(row, "SEMESTER")).put("SDATE", sdate);

                String edate = KnjDbUtils.getString(row, "EDATE");
                if (toCalendar(_param._date2).before(toCalendar(edate))) {
                    edate = _param._date2;
                }
                getMappedMap(semesterDateMap, KnjDbUtils.getString(row, "SEMESTER")).put("EDATE", edate);
            }

            return semesterDateMap;
        }

        /**
         *  学期集計およびＳＶＦ出力 累計へ累積
         *      累計 :     cnt_sum[出欠種別][生徒出力位置]
         */
        private void setAttendance(final DB2UDB db2, final StudentMap studentMap) {

            final Map semesterDateMap = getSemesterDate(db2);

            _param._attendParamMap.put("grade", _param._grade);
            _param._attendParamMap.put("hrClass", _param._hrClass);
            final String sql = AttendAccumulate.getAttendSemesSql(
                    _param._year,
                    _param._semester,
                    null,
                    _param._date2,
                    _param._attendParamMap
            );

            if (_param._isOutputDebug) {
                log.info(" CALC sql = " + sql);
            }

            //DB読み込みと集計処理
            for (final Map rs : KnjDbUtils.query(db2, sql)) {
                final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                if (SEMEALL.equals(semester)) {
                    continue;
                }
                if (null == getMappedMap(semesterDateMap, semester).get("SDATE") || null == getMappedMap(semesterDateMap, semester).get("EDATE")) {
                    continue;
                }

                final String schregno = KnjDbUtils.getString(rs, "SCHREGNO");
                final Student student = studentMap.getStudent(schregno);
                if (null == student) {
                    continue;
                }

                final int absent = Integer.parseInt(KnjDbUtils.getString(rs, "ABSENT"));
                final int abroad = Integer.parseInt(KnjDbUtils.getString(rs, "TRANSFER_DATE"));
                final int mourning = Integer.parseInt(KnjDbUtils.getString(rs, "MOURNING"));
                final int suspend = Integer.parseInt(KnjDbUtils.getString(rs, "SUSPEND"));
                final int koudome = Integer.parseInt(KnjDbUtils.getString(rs, "KOUDOME"));
                final int virus = Integer.parseInt(KnjDbUtils.getString(rs, "VIRUS"));
                final int sick = Integer.parseInt(KnjDbUtils.getString(rs, "SICK"));
                final int sickOnly = Integer.parseInt(KnjDbUtils.getString(rs, "SICK_ONLY"));
                final int noticeOnly = Integer.parseInt(KnjDbUtils.getString(rs, "NOTICE_ONLY"));
                final int nonoticeOnly = Integer.parseInt(KnjDbUtils.getString(rs, "NONOTICE_ONLY"));
                final int late = Integer.parseInt(KnjDbUtils.getString(rs, "LATE"));
                final int early = Integer.parseInt(KnjDbUtils.getString(rs, "EARLY"));
                final int lesson = Integer.parseInt(KnjDbUtils.getString(rs, "LESSON"));
                final int mlesson = Integer.parseInt(KnjDbUtils.getString(rs, "MLESSON"));
                final int present = Integer.parseInt(KnjDbUtils.getString(rs, "PRESENT"));
                final int jugyoLate = Integer.parseInt(KnjDbUtils.getString(rs, "JYUGYOU_TIKOKU"));
                final int jugyoEarly = Integer.parseInt(KnjDbUtils.getString(rs, "JYUGYOU_SOUTAI"));
                final int jugyoKekka = Integer.parseInt(KnjDbUtils.getString(rs, "KEKKA_JISU"));

                final Attendance attendance = new Attendance(semester, absent, abroad, mourning, suspend, koudome, virus, sick, sickOnly, noticeOnly, nonoticeOnly, late, early, lesson, mlesson, present, jugyoLate, jugyoEarly, jugyoKekka);
                if (null == student._studentAttendance) {
                    student._studentAttendance = new StudentAttendance(schregno);
                }

                student._studentAttendance.add(semester, attendance);
                if (attendance._sick != 0) {
                    log.debug(" schregno = "+  schregno + " (" + studentMap.getStudent(schregno) + ") semes = " + semester + " attendance = " + attendance);
                }

                //生徒別累計処理
                student._studentAttendance._total.add(attendance);
            }
        }

        /**
         *  学期集計およびＳＶＦ出力 累計へ累積
         *      累計 :     cnt_sum[出欠種別][生徒出力位置]
         */
        private void printAttendance(final StudentMap studentMap) {

            final Map<String, Attendance> totalAttendanceMap = new HashMap<String, Attendance>();
            for (final Student student : studentMap._studentMap.values()) {
                if (null == student._studentAttendance) {
                    continue;
                }

                for (final String attKey : student._studentAttendance._semesAttendance.keySet()) {
                    final Attendance attendance = student._studentAttendance._semesAttendance.get(attKey);

                    String key;
                    //学期別累計処理
                    key = attendance._semester;
                    if (!totalAttendanceMap.containsKey(key)) {
                        totalAttendanceMap.put(key, new Attendance());
                    }
                    final Attendance semeTotal = totalAttendanceMap.get(key);
                    semeTotal.add(attendance);

                    //累計処理
                    key = "total";
                    if (!totalAttendanceMap.containsKey(key)) {
                        totalAttendanceMap.put(key, new Attendance());
                    }
                    final Attendance total = totalAttendanceMap.get(key);
                    total.add(attendance);
                }
            }

            final Integer lastno = studentMap._printLineStudentMap.lastKey();
            int outputline = 0;

            final String form = getSvfForm();
            log.info("form = " + form);
            setForm(form, 1);

            final int maxPage = lastno.intValue() / MAX_LINE + (lastno.intValue() % MAX_LINE != 0 ? 1 : 0);
            for (int pageIdx = 0; pageIdx < maxPage; pageIdx ++) {
                printHeadSvf();                         //見出し等を出力するメソッド
                for (int line = pageIdx * MAX_LINE + 1; line <= (pageIdx + 1) * MAX_LINE; line++) {
                    final int pline = line % MAX_LINE == 0 ? MAX_LINE : line % MAX_LINE;
                    VrsOutn("NUMBER", pline, String.valueOf(line));
                    final Student student = studentMap.getStudentOfPrintLine(new Integer(line));
                    if (null == student) {
                        continue;
                    }

                    if (_param._output4 == null) {
                        for (int k = outputline + 1; k < pline; k++) {
                            VrsOutn("NUMBER", k, String.valueOf(k));
                        }
                    }
                    printStudentsname(student, pline);  //生徒名を出力するメソッド
                    outputline = pline;
                    if (null == student._studentAttendance) {
                        continue;
                    }

                    final String columnTotal = String.valueOf(_param._semesterdiv + 1);

                    for (final String semester : student._studentAttendance._semesAttendance.keySet()) {
                        final Attendance a = student._studentAttendance._semesAttendance.get(semester);
                        //学期集計の出力
                        printAttendance(semester, pline, a);

                        kessekiAmikake(student._schregno, semester, pline, a);

                        //累計の出力
                        printAttendance(columnTotal, pline, student._studentAttendance._total);

                        hasdata = true;
                    } // 生徒の行

                } // ページ

                if (maxPage - 1 == pageIdx) {
                    // 累計出力処理
                    final int MAX_LINE = 51;
                    for (final String key : totalAttendanceMap.keySet()) {
                        final int column;
                        if ("total".equals(key)) {
                            column = _param._semesterdiv + 1;
                        } else if (NumberUtils.isDigits(key)) {
                            column = Integer.parseInt(key);
                        } else {
                            continue;
                        }
                        final Attendance total = totalAttendanceMap.get(key);
                        printAttendance(String.valueOf(column), MAX_LINE, total);
                    }
                }
                VrEndPage();
            }
        }

        private void kessekiAmikake(final String schregno, final String semester, final int pline, final Attendance a) {
            if (_param._knjSchoolMst._kessekiWarnBunbo != null && _param._knjSchoolMst._kessekiWarnBunsi != null) {
                final int bunbo = Integer.parseInt(_param._knjSchoolMst._kessekiWarnBunbo);
                final int bunsi = Integer.parseInt(_param._knjSchoolMst._kessekiWarnBunsi);
                final BigDecimal limitBd = new BigDecimal(a._mlesson).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
                final BigDecimal sickBd = new BigDecimal(a._sick);
                if (sickBd.compareTo(limitBd) > 0) {
                    log.debug(schregno + " 注意：出席すべき日数 = " + a._mlesson + " 欠席 = " + a._sick + " 上限 = " + limitBd);
                    VrAttributen("TOTAL_ABSENCE" + semester,  pline, AMIKAKE_ATTR2);
                }
            }
            if (_param._knjSchoolMst._kessekiOutBunbo != null && _param._knjSchoolMst._kessekiOutBunsi != null) {
                final int bunbo = Integer.parseInt(_param._knjSchoolMst._kessekiOutBunbo);
                final int bunsi = Integer.parseInt(_param._knjSchoolMst._kessekiOutBunsi);
                final BigDecimal limitBd = new BigDecimal(a._mlesson).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
                final BigDecimal sickBd = new BigDecimal(a._sick);
                if (sickBd.compareTo(limitBd) > 0) {
                    log.debug(schregno + " 超過：出席すべき日数 = " + a._mlesson + " 欠席 = " + a._sick + " 上限 = " + limitBd);
                    VrAttributen("TOTAL_ABSENCE" + semester,  pline, AMIKAKE_ATTR1);
                }
            }
        }

        private void printAttendance(final String column, final int line, final Attendance attendance) {
            if (Param.KNJC043B.equals(_param._prgid)) {
                VrsOutn("TOTAL_SUSPEND" + column,  line, String.valueOf(attendance._suspend + attendance._mourning + attendance._virus + attendance._koudome)); //出停・忌引
                VrsOutn("KESSEKI" + column + "_1", line, String.valueOf(attendance._sickOnly));   //病欠
                VrsOutn("KESSEKI" + column + "_2", line, String.valueOf(attendance._noticeOnly)); //届けあり
                VrsOutn("TOTAL_ABSENCE" + column,  line, String.valueOf(attendance._sickOnly + attendance._noticeOnly));       //欠席

                VrsOutn("TOTAL_PRESENT" + column,  line, String.valueOf(attendance._mlesson));    //出席すべき日数
                VrsOutn("TOTAL_ATTEND"  + column,  line, String.valueOf(attendance._present));    //出席日数
                final String late;
                final String early;
                if ("1".equals(_param._knjc043bPrintLateEarly)) {
                    late = String.valueOf(attendance._late);  //遅刻
                    early = String.valueOf(attendance._early); //早退
                } else {
                    late = String.valueOf(attendance._jugyoLate);  //授業遅刻
                    early = String.valueOf(attendance._jugyoEarly); //授業早退
                }
                VrsOutn("TOTAL_LATE"    + column,  line, late);
                VrsOutn("TOTAL_LEAVE"   + column,  line, early);
                VrsOutn("TOTAL_KEKKA"   + column,  line, String.valueOf(attendance._jugyoKekka)); //授業欠課
            } else {
                if (_param._isTokiwagi) {
                    VrsOutn("TOTAL_SUSPEND_KIBIKI" + column,  line, String.valueOf(attendance._suspend + attendance._virus + attendance._koudome + attendance._mourning));        //出停・忌引
                    VrsOutn("TOTAL_ABROAD" + column,  line, String.valueOf(attendance._abroad));        //留学
                    VrsOutn("TOTAL_ABSENCE" + column,  line, String.valueOf(attendance._sick));        //欠席
                    VrsOutn("TOTAL_LATE"    + column,  line, String.valueOf(attendance._late));        //遅刻
                    VrsOutn("TOTAL_LEAVE"   + column,  line, String.valueOf(attendance._early));        //早退
                    VrsOutn("TOTAL_LESSON"  + column,  line, String.valueOf(attendance._lesson));        //授業日数
                    VrsOutn("TOTAL_ATTEND"  + column,  line, String.valueOf(attendance._mlesson));        //出席すべき日数
                } else if (_param._isOsakashinnai) {
                    VrsOutn("TOTAL_SUSPEND" + column,  line, String.valueOf(attendance._suspend + attendance._virus + attendance._koudome));        //出停
                    VrsOutn("TOTAL_KIBIKI"  + column,  line, String.valueOf(attendance._mourning));        //忌引
                    VrsOutn("TOTAL_KOKETSU" + column,  line, String.valueOf(attendance._absent));        //公欠
                    VrsOutn("TOTAL_ABSENCE" + column,  line, String.valueOf(attendance._sick));        //欠席
                    VrsOutn("TOTAL_LATE"    + column,  line, String.valueOf(attendance._late));        //遅刻
                    VrsOutn("TOTAL_LEAVE"   + column,  line, String.valueOf(attendance._early));        //早退
                } else {
                    VrsOutn("TOTAL_KIBIKI"  + column,  line, String.valueOf(attendance._mourning));        //忌引
                    VrsOutn("TOTAL_SUSPEND" + column,  line, String.valueOf(attendance._suspend + attendance._virus + attendance._koudome));        //出停
                    VrsOutn("TOTAL_ABSENCE" + column,  line, String.valueOf(attendance._sick));        //欠席
                    VrsOutn("TOTAL_LATE"    + column,  line, String.valueOf(attendance._late));        //遅刻
                    VrsOutn("TOTAL_LEAVE"   + column,  line, String.valueOf(attendance._early));        //早退
                    VrsOutn("TOTAL_LESSON"  + column,  line, String.valueOf(attendance._lesson));        //授業日数
                    VrsOutn("TOTAL_ATTEND"  + column,  line, String.valueOf(attendance._mlesson));        //出席すべき日数
                }
            }
        }
    }


    /**
     *  学校教育システム 賢者 [出欠管理]  出席簿（公簿）
     */
    private static class KNJC043_LIST extends Form {

        private boolean hasdata;

        final List<Period> _periodList;
        int _periodnum;                  //出力校時数(１日の)
        int _jdgPeriod;
        int _svfLine = 0;
        final TreeMap<Integer, Integer> columnXMap = new TreeMap<Integer, Integer>();
        final TreeMap<Integer, Integer> lineYMap = new TreeMap();

        KNJC043_LIST(final Vrw32alp svf, final DB2UDB db2, final Param param) {
            super(svf, param);

            //対象校時および名称取得
            _periodList = getPeiodList(_param, db2);
            _jdgPeriod = "1".equals(param._knjc043tMeisaiPrintAttendDayDat) ? 10 : 9;
            _periodnum = (_periodList.size() <= _jdgPeriod) ? _jdgPeriod : 16;
            log.info("_periodnum:" + _periodnum + " _jdgPeriod:" + _jdgPeriod);
        }

        /**
         *   svf print 印刷処理
         */
        public boolean printSvf(final DB2UDB db2) {

            //見出しデータ取得
            //  ＳＶＦフォームの設定->学期制による
            // _1,_6:月〜日（9校時） _4,_7:月〜金（16校時）
            String form = null;
            if (Param.KNJC043B.equals(_param._prgid)) {
                form = "KNJC043B_1.frm";
                _svfLine = 50;
            } else if (_param._isIbaraki) {
                if (_periodnum <= _jdgPeriod) {
                    if (_param._outputDiremark != null) {
                        form = "KNJC043_6A.frm";
                        _svfLine = 40;
                    } else {
                        form = "KNJC043_1A.frm";
                        _svfLine = 50;
                    }
                } else {
                    if (_param._outputDiremark != null) {
                        form = "KNJC043_7A.frm";
                        _svfLine = 40;
                    } else {
                        form = "KNJC043_4A.frm";
                        _svfLine = 50;
                    }
                }
            } else {
                if (_periodnum <= _jdgPeriod) {
                    if (_param._outputDiremark != null) {
                        form = "KNJC043_6.frm";
                        _svfLine = 40;
                    } else {
                        form = "1".equals(_param._knjc043tMeisaiPrintAttendDayDat) ? "KNJC043_1_2.frm" : "KNJC043_1.frm";
                        _svfLine = 50;
                    }
                } else if (_periodList.size() < 12) {
                    final String form12 = getForm12();
                    if (null != form12) {
                        form = form12;
                    }
                }
            }
            if (null == form) {
                if (_param._outputDiremark != null) {
                    form = "KNJC043_7.frm";
                    _svfLine = 40;
                } else {
                    form = "KNJC043_4.frm";
                    _svfLine = 50;
                }
            }
            if (_param._isOutputDebug) {
                log.info(" form = " + form);
            }

            //印刷範囲の再設定
            final Term term = Term.getTermValue(db2, _param);

            final List<Week> weekList = getWeekList(db2, _param, term);

            //  生徒の読み込み
            final StudentMap studentMap = StudentMap.getStudentMap(db2, _param);

            for (int wi = 0; wi < weekList.size(); wi++) {
                final Week week = weekList.get(wi);

                if (_param._isPrintOnedayAttendInfo || "1".equals(_param._knjc043tMeisaiPrintAttendDayDat)) {
                    // 1日出欠の取得と処理
                    for (final String date : week._dateList) {
                        log.info(" check oneday info : " + date);
                        Student.setAttendOnedayInfo(db2, date, _param, studentMap);
                    }
                }
                if (_param._isOutputDebug) {
                    log.info(" set week : " + wi + " / " + String.valueOf(weekList.size()));
                }
                Student.setSumAttend(db2, week._term, _param, studentMap);
                Student.setAttendDiRemark(db2, week._term, _param, studentMap);
            }

            final Map<String, Set<String>> weekDatePeriodSetMap = getDatePeriodKeySetMap(weekList, db2, _param, studentMap);

            final List<Map<String, String>> pageList = getPageList(studentMap, _svfLine);

            final int totalPage = weekList.size() * pageList.size();          //総ページ数

            for (int pageIdx = 0; pageIdx < pageList.size(); pageIdx++) {

                for (int wi = 0; wi < weekList.size(); wi++) {
                    final Week week = weekList.get(wi);

                    log.info(" ( week idx = " + wi + ", page idx = " + pageIdx + ")");

                    setForm(form);

                    final int startLine = pageIdx * _svfLine + 1;

                    printHead(term, week, weekList.size() * pageIdx + wi + 1, totalPage); //見出し等を出力するメソッド

                    final List<Student> studentList = studentMap.getStudentList(startLine, _svfLine);

                    printStudentsname(week._term, studentList); //生徒名を出力するメソッド
                    if (_param._output4 == null) {
                        for (int line = 0; line < _svfLine; line++) {
                            VrsOutn("NUMBER", line + 1, String.valueOf(startLine + line));
                        }
                    }

                    //週計および学期累計を取得し累積処理後出力するメソッド
                    printSumAttend(week._term, studentList);

                    //出欠データを取得し出力するメソッド
                    /**
                     *  出欠データのSVF出力
                     *    '時間割講座データ集計フラグが集計しないとなっている箇所を網掛け表示'に対応
                     *    「日曜日のみ出力」に適応のため、SVF-FORMのFIELDへ出力する際の列番をgetPositionOfLineメソッド(=>フォームにより異なる)で設定する
                     */

                    final Set<String> datePeriodSet = weekDatePeriodSetMap.get(week._term.dateRangeKey());
                    printAttendData(week, studentList, datePeriodSet);

                    if (_param._isPrintOnedayAttendInfo || "1".equals(_param._knjc043tMeisaiPrintAttendDayDat)) {
                        for (final String date : week._dateList) {
                            printAttendOnedayData(week, date, studentList);
                        }
                    }

                    if (_param._outputDiremark != null) {
                        final String totalRemark = getAttendDiRemark(week, studentList);

                        final String[] token = KNJ_EditEdit.get_token(totalRemark, 220, 10);
                        if (token != null) {
                            for (int i = 0; i < token.length; i++) {
                                VrsOutn("REMARK", i + 1, token[i]);
                            }
                        }
                        log.debug("total_remark = " + totalRemark.length());
                    }
                    VrEndPage();
                    hasdata = true;
                }                 //出欠データを出力するメソッド
            }

            return hasdata;
        }

        private void setForm(String form) {
            _svf.VrSetForm(form, 1);
            if (!columnXMap.isEmpty()) {
                for (final Integer col : columnXMap.keySet()) {
                    final Integer x = columnXMap.get(col);
                    final String attrX = "X=" + String.valueOf(x + 7);
                    VrAttributen("KOUJI", col, attrX);
                    VrAttributen("subject", col, attrX);
                    for (int line = 1; line <= _svfLine; line++) {
                        final Integer y = lineYMap.get(line) + 15;
                        final String attrY = "Y=" + String.valueOf(y);
                        VrAttributen("attend" + col.toString(), line, attrX + "," + attrY);
                        VrAttributen("CLEAR_attend" + col.toString(), line, attrX + "," + attrY);
                    }
                }
                for (int col = columnXMap.lastKey() + 1; col <= 99; col++) {
                    final String attrX = "X=10000";
                    VrAttributen("KOUJI", col, attrX);
                    VrAttributen("subject", col, attrX);
                }
            }
            if (!lineYMap.isEmpty()) {
                for (int line = 1; line <= _svfLine; line++) {
                    final Integer y = lineYMap.get(line) + 15;
                    final String attrY = "Y=" + String.valueOf(y);
                    VrAttributen("NUMBER", line, attrY);
                    VrAttributen("name", line, attrY);
                    VrAttributen("name2", line, attrY);
                    VrAttributen("SCHREGNO", line, attrY);
                    VrAttributen("SUSPEND", line, attrY);
                    VrAttributen("KIBIKI", line, attrY);
                    VrAttributen("ABSENCE", line, attrY);
                    VrAttributen("LATE", line, attrY);
                    VrAttributen("LEAVE", line, attrY);
                    VrAttributen("NOTE", line, attrY);
                }
            }
        }

        private String getForm12() {
            int periodmax = 12;
            String form11 = null;
            try {
                final String form = "KNJC043_10.frm";

                final String formFilePath = _svf.getPath(form);
                if (null != formFilePath) {
                    final File formFile = new File(formFilePath);
                    SvfForm svfForm = new SvfForm(formFile);
                    if (svfForm.readFile()) {

                        final SvfForm.Field kouji = svfForm.getField("KOUJI");
                        final int ymin = svfForm.getNearestUpperLine(kouji._position)._start._y;
                        final SvfForm.Field mark1 = svfForm.getField("MARK1");
                        final int ymax = svfForm.getNearestUpperLine(mark1._position)._start._y;

                        for (int i = 1; i <= 6; i++) { // 月曜日～土曜日
                            final SvfForm.Field hymdn = svfForm.getField("hymd" + String.valueOf(i));
                            final SvfForm.Line left = svfForm.getNearestLeftLine(hymdn._position);
                            final SvfForm.Line right = svfForm.getNearestRightLine(hymdn._position);
                            final int width = right._start._x - left._start._x;

                            for (int pi = 0; pi < periodmax; pi++) {
                                final int x = (int) (left._start._x + width / (double) periodmax * pi);
                                if (pi != 0) {
                                    svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.width(1), new SvfForm.Point(x, ymin), new SvfForm.Point(x, ymax)));
                                }
                                if (_param._isOutputDebugAll) {
                                    log.info(" block " + i + ", pi = " + String.valueOf(pi) + " / " + periodmax + ", x = " + x);
                                }
                                columnXMap.put((i - 1) * periodmax + pi + 1, x);
                            }
                        }

                        int studentLine;
                        if (_param._outputDiremark != null) {
                            studentLine = 40;
                        } else {
                            studentLine = 50;
                        }
                        final SvfForm.Field num = svfForm.getField("NUMBER");

                        final Line frameLine = svfForm.getNearestUpperLine(num._position);
                        final int lineXstart = frameLine._start._x;
                        final int lineXend = frameLine._end._x;
                        final int lineYStart = frameLine._start._y;
                        final int lineHeight = svfForm.getNearestLowerLine(num._position)._start._y - lineYStart;
                        for (int i = 1; i <= studentLine; i++) {
                            final int y = lineYStart + lineHeight * (i - 1);
                            final int lineWidth =  i % 5 == 0 ? 2 : 1;
                            svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.width(lineWidth), new SvfForm.Point(lineXstart, y), new SvfForm.Point(lineXend, y)));
                            lineYMap.put(i, y);
                        }

                        final File file = svfForm.writeTempFile();
                        if (null != file && file.exists()) {
                            _param._tmpFileList.add(file);
                            _periodnum = periodmax;
                            _svfLine = studentLine;
                            form11 = file.getAbsolutePath();
                        }
                    }
                }
            } catch (Throwable e) {
                if (_param._isOutputDebug) {
                    log.error("exception!", e);
                }
            }
            return form11;
        }

        private Integer getPeriodPosition(final String periodcd) {
            for (int i = 0; i < _periodList.size(); i++) {
                final Period period = _periodList.get(i);
                if (period._cd.equals(periodcd)) {
                    return new Integer(i + 1);
                }
            }
            return null;
        }

        /**
         * @return 出欠明細データのＳＱＬを戻します。
         */
        private String sqlAttendData(final Param param) {
            final StringBuffer stb = new StringBuffer();

            final String q = "?";

            stb.append("WITH DATE_RANGE (SDATE, EDATE) AS(");
            stb.append(" VALUES(CAST(" + q + " AS DATE), CAST(" + q + " AS DATE)) ");
            stb.append(") ");
            stb.append(", SCHNO AS(");
            stb.append(    "SELECT  SCHREGNO ");
            stb.append(    "FROM    SCHREG_REGD_DAT W2 ");
            stb.append(    "WHERE   W2.YEAR = '" + param._year + "' AND ");
            stb.append(            "W2.GRADE = '" + param._grade + "' AND W2.HR_CLASS = '" + param._hrClass + "' ");
            stb.append(    "GROUP BY SCHREGNO");
            stb.append(    ")");

            if (param._useschchrcountflg) {
                stb.append(",COUNTFLG AS(");
                stb.append(    "SELECT  EXECUTEDATE, PERIODCD, CHAIRCD, COUNTFLG ");
                stb.append(    "FROM    SCH_CHR_COUNTFLG T1 ");
                stb.append(    "INNER JOIN DATE_RANGE DR ON T1.EXECUTEDATE BETWEEN DR.SDATE AND DR.EDATE ");
                stb.append(    "WHERE ");
                stb.append(            "GRADE = '" + param._grade + "' AND HR_CLASS = '" + param._hrClass + "' AND ");
                stb.append(            "COUNTFLG = '0' ");
                stb.append(    ")");
            }

            stb.append(" ,TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV, ");
            stb.append("         T2.COUNTFLG ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            if ("TESTITEM_MST_COUNTFLG".equals(param._useTestCountflg)) {
                stb.append("         TESTITEM_MST_COUNTFLG T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
                stb.append("         AND T2.SCORE_DIV  = '01' ");
            } else {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            }
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append(") ");

            stb.append(",ATTEND AS(");
            stb.append(    "SELECT  W1.SCHREGNO, ");
            stb.append(            "W1.ATTENDDATE, ");
            stb.append(            "W1.PERIODCD, ");
            stb.append(            "W1.DI_CD ");
            stb.append(    "FROM    ATTEND_DAT W1 ");
            stb.append(    "INNER JOIN DATE_RANGE DR ON W1.ATTENDDATE BETWEEN DR.SDATE AND DR.EDATE ");
            stb.append(    "INNER JOIN SCHNO T2 ON T2.SCHREGNO = W1.SCHREGNO ");
            stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append(    ")");

            stb.append(",CHAIRSTD AS(");
            stb.append("    SELECT STD.SCHREGNO, STD.YEAR, STD.SEMESTER, STD.CHAIRCD, STD.APPDATE, STD.APPENDDATE");
            stb.append("    FROM CHAIR_DAT T2 ");
            stb.append("    INNER JOIN CHAIR_STD_DAT STD ON ");
            stb.append("          STD.YEAR = T2.YEAR ");
            stb.append("      AND T2.SEMESTER = STD.SEMESTER");
            stb.append("      AND T2.CHAIRCD = STD.CHAIRCD");
            stb.append("    INNER JOIN SCHNO T1 ON T1.SCHREGNO = STD.SCHREGNO ");
            stb.append("    WHERE T2.YEAR = '" + param._year + "'");
            stb.append(")");

            stb.append(",SCHEDULE AS(");
            stb.append("    SELECT T2.YEAR, T1.SEMESTER, T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, MIN(T1.EXECUTED) AS EXECUTED, MIN(T3.EXECUTED) AS HR_EXECUTED, T1.DATADIV");
            stb.append("    FROM SCH_CHR_DAT T1 ");
            stb.append(    "INNER JOIN DATE_RANGE DR ON T1.EXECUTEDATE BETWEEN DR.SDATE AND DR.EDATE ");
            stb.append("    INNER JOIN CHAIRSTD T2 ON T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE");
            stb.append("        AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER");
            stb.append("    LEFT JOIN SCH_CHR_HRATE_DAT T3 ON T1.EXECUTEDATE = T3.EXECUTEDATE ");
            stb.append("        AND T1.PERIODCD = T3.PERIODCD ");
            stb.append("        AND T1.CHAIRCD = T3.CHAIRCD ");
            stb.append("        AND T3.GRADE = '" + param._grade + "' ");
            stb.append("        AND T3.HR_CLASS = '" + param._hrClass + "' ");
            stb.append("    GROUP BY T2.YEAR, T1.SEMESTER, T2.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.DATADIV");
            stb.append(")");

            stb.append(",SCHLEAVE AS(");
            stb.append("    SELECT T1.SCHREGNO,T1.EXECUTEDATE");
            stb.append("    FROM( ");
            stb.append("         SELECT SCHREGNO,EXECUTEDATE");
            stb.append("         FROM SCHEDULE");
            stb.append("         GROUP BY SCHREGNO,EXECUTEDATE");
            stb.append("        ) T1");
            stb.append("    WHERE EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T2");
            stb.append("                 WHERE T2.SCHREGNO = T1.SCHREGNO");
            stb.append("                   AND T2.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > T2.GRD_DATE)");
            stb.append("       OR EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T3");
            stb.append("                 WHERE T3.SCHREGNO = T1.SCHREGNO");
            stb.append("                   AND T3.ENT_DIV IN('4','5')");
            stb.append("                   AND T1.EXECUTEDATE < T3.ENT_DATE)");
            stb.append("       OR EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T4");
            stb.append("                 WHERE T4.SCHREGNO = T1.SCHREGNO");
            stb.append("                   AND T4.TRANSFERCD IN('1','2')");
            stb.append("                   AND T1.EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE)");
            stb.append("    GROUP BY T1.SCHREGNO,T1.EXECUTEDATE");
            stb.append(")");

            stb.append(" SELECT T1.SCHREGNO, ");
            stb.append("        T1.EXECUTEDATE, ");
            stb.append("        T1.PERIODCD, ");
            stb.append("        T1.CHAIRCD, ");
            stb.append("        VALUE(T_CHAIR.CHAIRABBV, T_CHAIR.CHAIRNAME) AS CHAIRNAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T_CHAIR.CLASSCD, ");
                stb.append("        T_CHAIR.CLASSCD || '-' || T_CHAIR.SCHOOL_KIND || '-' || T_CHAIR.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T_CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("       ,VALUE(SUBM.SUBCLASSABBV, SUBM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("       ,T1.EXECUTED ");
            stb.append("       ,T1.HR_EXECUTED ");
            stb.append("       ,T2.DI_CD");
            stb.append("       ,ATDD.REP_DI_CD");
            stb.append("       ,DAYOFWEEK_ISO(T1.EXECUTEDATE) AS DAYOFWEEK");
            stb.append("       ,ATDD.DI_MARK AS DI_NAME");
            stb.append("       ,CASE WHEN T1.DATADIV = '2' THEN VALUE(T6.COUNTFLG, '1') ");
            stb.append("             WHEN T1.DATADIV IN ('0', '1') THEN VALUE(T3.COUNTFLG,'1') ");
            stb.append("             ELSE '1' END AS COUNTFLG");
            stb.append("       ,CASE WHEN T4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE");
            stb.append(" FROM SCHEDULE T1");
            stb.append(" LEFT JOIN ATTEND T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.EXECUTEDATE AND T2.PERIODCD = T1.PERIODCD");
            stb.append(" LEFT JOIN COUNTFLG T3 ON T3.EXECUTEDATE = T1.EXECUTEDATE AND T3.PERIODCD = T1.PERIODCD AND T3.CHAIRCD = T1.CHAIRCD");
            stb.append(" LEFT JOIN SCHLEAVE T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.EXECUTEDATE = T1.EXECUTEDATE");
            stb.append(" LEFT JOIN CHAIR_DAT T_CHAIR ON T1.YEAR = T_CHAIR.YEAR AND T1.SEMESTER = T_CHAIR.SEMESTER AND T1.CHAIRCD = T_CHAIR.CHAIRCD");
            stb.append(" LEFT JOIN TEST_COUNTFLG T6 ON T6.EXECUTEDATE = T1.EXECUTEDATE AND T6.PERIODCD = T1.PERIODCD AND T6.CHAIRCD = T1.CHAIRCD AND T6.DATADIV = T1.DATADIV ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON T_CHAIR.SUBCLASSCD = SUBM.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T_CHAIR.CLASSCD = SUBM.CLASSCD ");
                stb.append("  AND T_CHAIR.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                stb.append("  AND T_CHAIR.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param._year + "' AND ATDD.DI_CD = T2.DI_CD ");
            stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD");
            return stb.toString();
        }

        /**
         *  学期取得
         */
        private static int getSemesterValue2(final Param param, final String dateStr) {
            int rtn = 0;
            if (null == dateStr) {
                return rtn;
            }
            final Date date = java.sql.Date.valueOf(dateStr);
            for (final Map.Entry<String, Term> e : param._semesterDates.entrySet()) {
                final String semester = e.getKey();
                if (SEMEALL.equals(semester)) {
                    continue;
                }
                final Term semeTerm = param._semesterDates.get(semester);
                if (null == semeTerm) {
                    log.warn(" null semester : " + semester);
                }
                final Date sdate = toDate(semeTerm._sdate);
                final Date edate = toDate(semeTerm._edate);
                if (null == sdate || null == edate) {
                    log.warn(" null semester range: " + semeTerm);
                }
                if (sdate.compareTo(date) <= 0 && date.compareTo(edate) <= 0) {
                    rtn = Integer.parseInt(semester);
                }
            }
            return rtn;
        }

        /**
         *  対象校時および名称取得
         */
        private static List<Period> getPeiodList(final Param param, final DB2UDB db2) {
            //  校時名称
            final List<Period> periodlist = new ArrayList<Period>();

            final StringBuffer stb = new StringBuffer();
            stb.append(    "SELECT  W1.NAMECD2, W1.NAME1 ");
            if (param._usefromtoperiod) {
                stb.append(       ",CASE WHEN NAMECD2 BETWEEN S_PERIODCD AND E_PERIODCD THEN 1 ELSE 0 END AS ONPERIOD ");
            } else {
                stb.append(       ",1 AS ONPERIOD ");
            }
            if (param._usefromtoperiod) {
                stb.append("FROM    NAME_MST W1, COURSE_MST W2 ");
                stb.append("WHERE   NAMECD1 = 'B001' ");
                stb.append(    "AND COURSECD IN (SELECT MIN(COURSECD) FROM SCHREG_REGD_DAT W3 ");
                stb.append(                    "WHERE  W3.YEAR = '" + param._year + "' ");
                stb.append(                       "AND W3.SEMESTER = '" + param._semester + "' ");
                stb.append(                       "AND W3.GRADE = '" + param._grade + "' AND W3.HR_CLASS = '" + param._hrClass + "') ");
            } else {
                stb.append("FROM    NAME_MST W1 ");
                stb.append("WHERE   NAMECD1 = 'B001' ");
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM NAME_YDAT W2 ");
            stb.append(                   "WHERE  W2.YEAR = '" + param._year + "' ");
            stb.append(                      "AND W2.NAMECD1 = 'B001' AND W2.NAMECD2 = W1.NAMECD2) ");
            stb.append("ORDER BY NAMECD2");
            log.debug("period sql = " + stb.toString());

            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, stb.toString());

            for (int i = 0; i < rowList.size() && periodlist.size() < 16; i++) {
                final Map rs = rowList.get(i);
                if ("2".equals(param._radio1) ||  getInt(rs, "ONPERIOD", 0) == 1) {
                    periodlist.add(new Period(KnjDbUtils.getString(rs, "NAMECD2"), KnjDbUtils.getString(rs, "NAME1")));
                }
            }
            log.info(" periodlist size = " + periodlist.size());

            return periodlist;
        }

        /**
         *  出力処理＿見出し出力
         */
        private void printHead(final Term term, final Week week, final int page, final int totalPage) {

            VrsOut("HR_NAME", _param._hrname);        //組名称
            if (_param._isSapporo && (null != _param._staffname && null != _param._teacher2)) {
                VrsOut("teacher2", _param._staffname);        //担任名
                VrsOut("teacher3", _param._teacher2);        //担任名
            } else {
                VrsOut("teacher", _param._staffname);        //担任名
            }
            VrsOut("ymd1", _param._ctrlDateFormatJp);        //作成日
            VrsOut("nendo", _param._nendo);        //年度
            VrsOut("PRINT", term._dateFromToString);        //印刷範囲
            VrAttribute("PAGE", "Edit=,Hensyu=1"); // 右詰め
            VrsOut("PAGE", String.valueOf(page));           //ページ数
            VrsOut("TOTAL_PAGE", String.valueOf(totalPage));           //総ページ数

            if (_param._onlySunday != null) {
                VrsOut("PERIOD", "日の集計");
            } else if (_periodnum == _jdgPeriod) {
                VrsOut("PERIOD", "月" + FROM_TO_MARK + "日の集計");
            } else {
                VrsOut("PERIOD", "月" + FROM_TO_MARK + "土の集計");
            }
            VrAttribute("MARK1",  "Paint=(2,70,1),Bold=1");
            VrsOut("MARK1", "  ");
            VrsOut("MARK2", "＝");

            // KNJC043Bのみ
            VrsOut("KESSEKI_NAME1", _param.getDicdDatNameMap("4"));
            VrsOut("KESSEKI_NAME2", _param.getDicdDatNameMap("5"));

            for (int i = 0; i < week._dateList.size(); i++) {
                final String datestr = week._dateList.get(i);
                try {
                    final int dow = toCalendar(datestr).get(Calendar.DAY_OF_WEEK);
                    final int col = Calendar.SUNDAY == dow ? 7 : dow - 1;
                    VrsOut("hymd" + String.valueOf(col), KNJ_EditDate.h_format_JP_MD(datestr) + "(" + KNJ_EditDate.h_format_W(datestr) + ")"); //ＳＶＦ出力(日付)
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            for (int j = 0; j < week._periodStartList.size(); j++) {
                final int widx = week._periodStartList.get(j);
                for (int i = 0; i < _periodList.size(); i++) {
                    final Period period = _periodList.get(i);
                    VrsOutn("KOUJI", widx * _periodnum + i + 1 , period._name);
                }
                if ("1".equals(_param._knjc043tMeisaiPrintAttendDayDat)) {
                    VrsOutn("KOUJI", (widx + 1) * _periodnum, "日");
                }
            }

            //校時及び科目を出力するメソッド
            for (final Integer col : week._subjectNameMap.keySet()) {
                VrsOutn("subject", col.intValue(), week._subjectNameMap.get(col));
            }
        }

        private List<Map<String, String>> getPageList(final StudentMap studentMap, final int _svfLine) {
            final List list = new ArrayList();

            int oldpage = 0;

            String sattendno = null;
            String eattendno = null;
            List<String> schregnoList = new ArrayList<String>();

            for (final Integer line : studentMap._printLineStudentMap.keySet()) {
                //PAGE印刷処理
                final int page = (line.intValue() - 1) / _svfLine;
                if (page != oldpage) {
                    final Map map = new HashMap();
                    map.put("SATTENDNO", sattendno);
                    map.put("EATTENDNO", eattendno);
                    map.put("SCHREGNO_LIST", schregnoList);
                    list.add(map);
                    oldpage = line.intValue() / _svfLine;      //ページを保管
                    sattendno = null;
                }
                final Student student = studentMap.getStudentOfPrintLine(line);         //行番号で学生番号を取り出す
                if (student._attendno == null) {
                    continue;
                }
                if (sattendno == null) {
                    sattendno = student._attendno;     //先頭出席番号を保管
                    schregnoList = new ArrayList();
                }
                eattendno = student._attendno;         //後尾出席番号を保管
                schregnoList.add(student._schregno);
            }
            if (sattendno != null) {
                final Map map = new HashMap();
                map.put("SATTENDNO", sattendno);
                map.put("EATTENDNO", eattendno);
                map.put("SCHREGNO_LIST", schregnoList);
                list.add(map);
            }
            return list;
        }

        private static String mkString(final List<String> stringList, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (int i = 0; i < stringList.size(); i++) {
                if (stringList.get(i) != null) {
                    stb.append(c).append(stringList.get(i));
                    c = comma;
                }
            }
            return stb.toString();
        }

        private String getAttendDiRemark(final Week week, final List<Student> studentList) {

            final Set<String> dateSet = new TreeSet<String>();

            for (final Student student : studentList) {
                if (null == student) {
                    continue;
                }
                dateSet.addAll(getMappedMap(student._attendDiRemarkMap, week._term.dateRangeKey()).keySet());
            }

            final List<String> remarkList = new ArrayList<String>();
            for (final String date : dateSet) {

                final StringBuffer stb = new StringBuffer();

                for (final Student student : studentList) {
                    if (null == student) {
                        continue;
                    }

                    final Map<String, String> periodAttendRemarkMap = getMappedMap(getMappedMap(student._attendDiRemarkMap, week._term.dateRangeKey()), date);
                    if (periodAttendRemarkMap.isEmpty()) {
                        continue;
                    }

                    if (stb.length() > 0) {
                        stb.append("／");
                    }

                    stb.append(KNJ_EditDate.h_format_JP_MD(date) + "," + student._name);

                    for (final String period : periodAttendRemarkMap.keySet()) {
                        final String remark = periodAttendRemarkMap.get(period);

                        stb.append(",").append(remark);
                    }
                }
                if (stb.length() > 0) {
                    remarkList.add(stb.toString());
                }
            }

            final String totalRemark = mkString(remarkList, "\n");
            return totalRemark;
        }


        private void printAttendOnedayData(final Week week, final String date, final List<Student> studentList) {
            for (final Student student : studentList) {

                String attendField = null;
                final Integer dayofweek = week._dateDayofweekMap.get(date);
                if (null != dayofweek) {
                    int col = _periodnum;
                    attendField = "attend" + String.valueOf(getPositionOfLine(_param, dayofweek.intValue(), col));
                }
                if (null == attendField) {
                    continue;
                }

                VrsOutn(attendField, student._printline, "");

                final String comment = student._onedayAttendComment.get(date);
                if (null != comment) {
                    if ("1".equals(_param._knjc043tMeisaiPrintAttendDayDat)) {
                        final int mpLen = KNJ_EditEdit.getMS932ByteLength(comment);
                        final String aField = mpLen > 2 ? "_2" : "";
                        VrsOutn(attendField + aField, student._printline, comment);
                    } else {
                        VrsOutn(attendField, student._printline, comment);
                    }
                } else {
                    if (getMappedList(student._datePeriodListMap, date).size() > 0) {
                        VrsOutn(attendField, student._printline, "未");
                    }
                }
            }
        }

        private void printAttendData(final Week week, final List<Student> studentList, final Set<String> datePeriodSet) {
            for (final Student student : studentList) {
                final int line = student._printline;

                for (final String date : student._onedayAttendAttributeTarget.keySet()) {
                    if (!week._dateList.contains(date)) {
                        continue;
                    }

                    final Integer dayofweek = getDB2DayOfWeek(date);
                    if (null == dayofweek) {
                        continue;
                    }

                    for (final Period period : _periodList) {
                        final Integer intperiod = getPeriodPosition(period._cd);

                        final String attendField = String.valueOf(getPositionOfLine(_param, dayofweek.intValue(), intperiod.intValue()));
                        //log.info(" attendField = " + attendField + " / " + date + " / " + dayofweek);
                        VrAttributen("attend" + attendField, line, "Paint=(1,80,1),Hensyu=3");
                    }
                }

                for (final String datePeriodKey : datePeriodSet) {

                    final Map dataMap = student._datePeriodKintaiMap.get(datePeriodKey);
                    if (null == dataMap) {
                        continue;
                    }
                    final String attendField = "attend" + (String) dataMap.get("attendField");
                    if ("1".equals(dataMap.get("KARA"))) {
                        VrAttributen(attendField, line, "Size=6.0" );
                        VrsOutn(attendField, line, "空");
                    } else {
                        final Map row = dataMap;
                        final String chairField = "CHAIR" + (String) row.get("attendField");

                        boolean amikake = false;
                        // 時間割講座データ集計フラグを使用する場合の網掛け設定
                        if (_param._useschchrcountflg) {
                            amikake = (Integer.parseInt(KnjDbUtils.getString(row, "COUNTFLG")) == 0) ? true : false;
                            if (amikake) {
                                final String ATTR_AMIKAKE = "Paint=(2,70,1),Bold=1,Hensyu=3";
                                VrAttributen(attendField, line, ATTR_AMIKAKE);
                                if (_param._isIbaraki) {
                                    VrAttributen(chairField, line, ATTR_AMIKAKE);
                                }
                            }
                        }

                        boolean shukketsuZumi = false;
                        // 出欠入力済みで出席は「ブランク」、未入力は「未」を印刷
                        if (KnjDbUtils.getString(row, "EXECUTED") != null || KnjDbUtils.getString(row, "HR_EXECUTED") != null) {
                            //knjc043tMeisaiPrintAttendDayDatプロパティが立っている時だけ、出欠記号が入るか、チェックする。じゃないと、文字が被る。
                            if ((
                                 ("1".equals(_param._knjc043tMeisaiPrintAttendDayDat) && "".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "DI_NAME"))))
                                 ||
                                 (!"1".equals(_param._knjc043tMeisaiPrintAttendDayDat))
                                )
                                &&
                                isPrintMinyuuryoku(_param, KnjDbUtils.getString(row, "EXECUTED"), KnjDbUtils.getString(row, "HR_EXECUTED"), KnjDbUtils.getString(row, "EXECUTEDATE"))
                            ) {
                                VrAttributen(attendField, line, "Size=6.0");
                                VrsOutn(attendField, line, "未");
                            } else {
                                VrAttributen(attendField, line, "Size=9.0");
                                VrsOutn(attendField, line, "  ");

                                if (_param._isIbaraki) {
                                    final String subject;
                                    if (_param._usechairname) {
                                        subject = KnjDbUtils.getString(row, "CHAIRNAME");
                                    } else {
                                        subject = KnjDbUtils.getString(row, "SUBCLASSNAME");
                                    }
                                    VrsOutn(chairField, line, subject);
                                }
                                shukketsuZumi = true;
                            }
                        }

                        // 出欠記号
                        if (KnjDbUtils.getString(row, "DI_NAME") != null) {
                            final String attr;
                            if (_param._isIbaraki) {
                                attr = "Size=7.0";
                            } else {
                                attr = "Size=9.0";
                            }
                            if (!_param._isIbaraki || _param._isIbaraki && shukketsuZumi) {
                                VrAttributen(attendField, line, attr);
                                final String dnStr = KnjDbUtils.getString(row, "DI_NAME");
                                if ("1".equals(_param._knjc043tMeisaiPrintAttendDayDat)) {
                                    final int mpLen = KNJ_EditEdit.getMS932ByteLength(dnStr);
                                    final String aField = mpLen > 2 ? "_2" : "";
                                    VrsOutn(attendField + aField, line, dnStr);
                                } else {
                                    VrsOutn(attendField, line, dnStr);
                                }
                                // 取り消し線
                                if (KnjDbUtils.getString(row, "LEAVE") != null && getInt(row, "LEAVE", 0) == 1) {
                                    final Integer intperiod = getPeriodPosition(KnjDbUtils.getString(row, "PERIODCD"));
                                    final int dayofweek = getInt(row, "DAYOFWEEK", 0);

                                    VrsOutn("CLEAR_attend" + getPositionOfLine(_param, dayofweek, intperiod.intValue()), line, "＝");
                                }
                            }
                        }
                        // 網掛け設定解除
                        if (_param._useschchrcountflg) {
                            if (amikake) {
                                final String ATTR_CLEAR = "Paint=(0,0,0),Bold=0,Hensyu=3";
                                VrAttributen(attendField, line, ATTR_CLEAR);
                                if (_param._isIbaraki) {
                                    VrAttributen(chairField, line, ATTR_CLEAR);
                                }
                            }
                        }
                    }

                }
            }
        }

        private Integer getDB2DayOfWeek(String date) {
            if (null == date) {
                return null;
            }

            final int dayOfWeekJava = toCalendar(date).get(Calendar.DAY_OF_WEEK);
            if (dayOfWeekJava == Calendar.SUNDAY) {
                return new Integer(7);
            }
            return new Integer(dayOfWeekJava - 1);
        }

        private Map<String, Set<String>> getDatePeriodKeySetMap(final List<Week> weekList, final DB2UDB db2, final Param param, final StudentMap studentMap) {

            PreparedStatement ps = null;

            final Map datePeriodKeySetMap = new HashMap();
            try {
                final String sql = sqlAttendData(param);
                if (param._isOutputDebug) {
                    log.info(" sqlAttendData = " + sql);
                }
                ps = db2.prepareStatement(sql);        //出欠データ

                for (final Week week : weekList) {

                    final Set datePeriodKeySet = new HashSet();

                    final Object[] parameter = new Object[] {
                            java.sql.Date.valueOf(week._term._sdate),      //集計開始日（週始め）
                            java.sql.Date.valueOf(week._term._edate),      //集計終了日（週終わり）
                    };

                    for (final Map row : KnjDbUtils.query(db2, ps, parameter)) {

                        final String date = KnjDbUtils.getString(row, "EXECUTEDATE");
                        final String datePeriodKey = date + "" + KnjDbUtils.getString(row, "PERIODCD");
                        final Integer intperiod = getPeriodPosition(KnjDbUtils.getString(row, "PERIODCD"));
                        if (intperiod == null) {
                            continue;
                        }
                        final int dayofweek = getInt(row, "DAYOFWEEK", 0);
                        final String attendField = String.valueOf(getPositionOfLine(param, dayofweek, intperiod.intValue()));

                        if ("1".equals(param._shrSyurei)) {
                            final boolean isShrSyurei;
                            if ("1".equals(param._useCurriculumcd)) {
                                isShrSyurei = "92".equals(KnjDbUtils.getString(row, "CLASSCD"));
                            } else {
                                isShrSyurei = KnjDbUtils.getString(row, "SUBCLASSCD") != null && KnjDbUtils.getString(row, "SUBCLASSCD").startsWith("92");
                            }
                            if (isShrSyurei) {
                                continue;
                            }
                        }

                        if (!datePeriodKeySet.contains(datePeriodKey)) {
                            // 未履修「空」の印刷
                            for (final Iterator<Integer> lit = studentMap._printLineStudentMap.keySet().iterator(); lit.hasNext();) {
                                final Integer printLine = lit.next();
                                final Student student = studentMap.getStudentOfPrintLine(printLine);
                                if (null == student || !NumberUtils.isDigits(student._attendno)) {
                                    continue;
                                }
                                final Map dataMap = new HashMap();
                                dataMap.put("KARA", "1");
                                dataMap.put("attendField", attendField);
                                student._datePeriodKintaiMap.put(datePeriodKey, dataMap);
                            }
                            datePeriodKeySet.add(datePeriodKey);
                        }
                        final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student || null == student._line) {
                            continue;
                        }
                        row.put("attendField", attendField);
                        student._datePeriodKintaiMap.put(datePeriodKey, row);

                        if (param._isPrintOnedayAttendInfo) {
                            getMappedMap(student._datePeriodKintaiMapMap, date).put(intperiod, KnjDbUtils.getString(row, "DI_CD"));
                            getMappedMap(student._datePeriodRepDiCdMapMap, date).put(intperiod, KnjDbUtils.getString(row, "REP_DI_CD"));

                        }

                        if ("1".equals(param._knjc043tMeisaiPrintAttendDayDat)) {
                            getMappedList(student._datePeriodListMap, date).add(intperiod);
                        }
                    }

                    datePeriodKeySetMap.put(week._term.dateRangeKey(), datePeriodKeySet);
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            if (param._isPrintOnedayAttendInfo && "1".equals(param._isOnedayAttendCheck)) {
                for (final Integer printLine : studentMap._printLineStudentMap.keySet()) {
                    final Student student = studentMap.getStudentOfPrintLine(printLine);
                    if (null == student || !NumberUtils.isDigits(student._attendno)) {
                        continue;
                    }
                    for (final Map.Entry<String, TreeMap<Integer, String>> e : student._datePeriodKintaiMapMap.entrySet()) {
                        final String date = e.getKey();
                        final TreeMap<Integer, String> periodKintaiTreeMap = e.getValue();

                        boolean setBefore = false;
                        boolean shussekiBefore = false;
                        int flipped = 0;
                        for (final Map.Entry<Integer, String> periodKintai : periodKintaiTreeMap.entrySet()) {
                            final String diCd = periodKintai.getValue();
                            boolean shusseki = false;
                            if (setBefore) {
                                final String repDiCd = getMappedMap(student._datePeriodKintaiMapMap, date).get(periodKintai.getKey());
                                final String hanteiDiCd = StringUtils.defaultString(repDiCd, diCd);
                                shusseki = isGakkounaiShusseki(hanteiDiCd);
                                if (shussekiBefore != shusseki) {
                                    flipped += 1;
                                }
                            }
                            setBefore = true;
                            shussekiBefore = shusseki;
                        }
                        if (flipped > 1) {
                            student._onedayAttendAttributeTarget.put(date, new Integer(flipped));
                        }
                    }
                }
            }

            return datePeriodKeySetMap;
        }

        // 校内にいる勤怠はtrue
        private boolean isGakkounaiShusseki(final String diCd) {
            if (!NumberUtils.isDigits(diCd)) {
                if (null != diCd) {
                    log.warn("unknown DI_CD:" + diCd);
                }
                return true;
            }
            final int diCdInt = Integer.parseInt(diCd);
            boolean shusseki = true;
            switch (diCdInt) {
            case 0: break; // 出席
            case 1: shusseki = false; break; // 公欠
            case 2: shusseki = false; break; // 出停
            case 3: shusseki = false; break; // 忌引
            case 4: shusseki = false; break; // 病欠
            case 5: shusseki = false; break; // 届出有
            case 6: shusseki = false; break; // 届出無
            case 8: shusseki = false; break; // 公欠
            case 9: shusseki = false; break; // 出停
            case 10: shusseki = false; break; // 忌引
            case 11: shusseki = false; break; // 病欠
            case 12: shusseki = false; break; // 届出有
            case 13: shusseki = false; break; // 届出無
            case 14: break; // 保健室結果
            case 15: break; // 遅刻
            case 16: break; // 早退
            case 19: shusseki = false; break; // 出停伝染病
            case 20: shusseki = false; break; // 出停伝染病
            case 25: shusseki = false; break; // 出停交止
            case 26: shusseki = false; break; // 出停交止
            case 29: break; // 欠課遅刻
            case 30: break; // 欠課早退
            case 31: break; // 欠課遅刻早退
            case 32: break; // 出席扱いの遅刻早退
            default:
                shusseki = true;
            }
            return shusseki;
        }

        /**
         * '未'を印字するか'　'（ブランク）を印字するかを判別して戻します。
         * @param rs
         * @return 「'未'を表記しない」とした場合、出欠を取った場合、出欠日が未来の場合は false を戻します。
         */
        private boolean isPrintMinyuuryoku(final Param param, final String executed, final String hrExecuted, final String executedate) {
            if (null == param._output5) {
                return false;
            }
            if ("1".equals(executed)) {
                return false;
            }
            if ("1".equals(hrExecuted)) {
                return false;
            }
            if (!isPastDay(param, executedate)) {
                return false;
            }
            return true;
        }


        /**
         * @param str "yyyy-MM-dd"のフォーマットの文字列
         * @return String str が _nowDate と比較して同じ日または過去ならtrueを戻します。
         * _nowDateはnew Date()。
         */
        private boolean isPastDay(final Param param, final String str) {
            if (null == str) {
                return false;
            }
            try {
                Date date = sdf.parse(str);
                if (0 >= date.compareTo(param._loginDate)) {
                    return true;
                }
            } catch (ParseException e) {
                 log.error("ParseException", e);
            }
            return false;
        }

        /**
         *  SVF-FORMのFIELDへ出力する際の列番を(=>フォームにより異なる)設定する処理
         */
        private int getPositionOfLine(final Param param, final int dayofweek, final int period) {
            int line = 0;
            if (param._onlySunday != null) {
                line = period;
            } else {
                line = (dayofweek - 1) * _periodnum + period;
            }
            return line;
        }

        /**
         *  学期集計およびＳＶＦ出力 累計へ累積
         */
        private void printSumAttend(final Term term, final List<Student> studentList) {

            //DB読み込みと集計処理
            for (int i = 0; i < studentList.size(); i++) {

                final Student student = studentList.get(i);

                final Map row = student._termAttendInfoMap.get(term.dateRangeKey());

                final int line = student._printline;
                //学期集計の出力
                if (Param.KNJC043B.equals(_param._prgid)) {
                    VrsOutn("SUSPEND",  line, String.valueOf(getInt(row, "SUSPEND", 0) + getInt(row, "MOURNING", 0) + getInt(row, "VIRUS", 0) + getInt(row, "KOUDOME", 0)));      // 出停 + 忌引
                    VrsOutn("PRESENT",  line, KnjDbUtils.getString(row, "MLESSON"));         // 要出席数

                    VrsOutn("KESSEKI1", line, KnjDbUtils.getString(row, "SICK_ONLY"));       // 病欠のみ
                    VrsOutn("KESSEKI2", line, KnjDbUtils.getString(row, "NOTICE_ONLY"));     // 欠席（届けあり）のみ
                    VrsOutn("ABSENCE",  line, String.valueOf(getInt(row, "SICK_ONLY", 0) + getInt(row, "NOTICE_ONLY", 0)));         // 欠席合計
                    VrsOutn("ATTEND",   line, KnjDbUtils.getString(row, "PRESENT"));         // 出席数
                    final String chikoku;
                    final String soutai;
                    if ("1".equals(_param._knjc043bPrintLateEarly)) {
                        chikoku = KnjDbUtils.getString(row, "LATE");  // 遅刻
                        soutai = KnjDbUtils.getString(row, "EARLY");  // 早退
                    } else {
                        chikoku = KnjDbUtils.getString(row, "JYUGYOU_TIKOKU");  // 授業遅刻
                        soutai = KnjDbUtils.getString(row, "JYUGYOU_SOUTAI");   // 授業早退
                    }
                    VrsOutn("LATE",     line, chikoku);
                    VrsOutn("EARLY",    line, soutai);
                    VrsOutn("KEKKA",    line, KnjDbUtils.getString(row, "KEKKA_JISU"));      // 授業欠課

                } else {
                    VrsOutn("KIBIKI",   line, KnjDbUtils.getString(row, "MOURNING"));     // 忌引
                    VrsOutn("SUSPEND",  line, String.valueOf(getInt(row, "SUSPEND", 0) + getInt(row, "VIRUS", 0) + getInt(row, "KOUDOME", 0)));      // 出停
                    VrsOutn("ABSENCE",  line, KnjDbUtils.getString(row, "SICK"));         // 欠席
                    VrsOutn("LATE",     line, KnjDbUtils.getString(row, "LATE"));         // 遅刻
                    VrsOutn("LEAVE",    line, KnjDbUtils.getString(row, "EARLY"));        // 早退
                }
            }

        }

        /**
         *  出力処理＿生徒名等出力
         *   引数に Map hm3 (備考) を追加
         **/
        private void printStudentsname(final Term term, final List<Student> studentList) {
            for (int i = 0; i < studentList.size(); i++) {
                final Student student = studentList.get(i);
                final int line = student._printline;
                if (student._attendno != null) {
                    if ("1".equals(_param._use_SchregNo_hyoji)) {
                        VrsOutn("SCHREGNO", line, student._schregno);
                        VrsOutn("name2", line, student._name);
                    } else  {
                        VrsOutn("name", line, student._name);
                    }
                    if (_param._output4 != null) {
                        VrsOutn("NUMBER", line, String.valueOf(Integer.parseInt(student._attendno)));
                    } else {
                        // 別に出力
                    }
                }
                //備考出力
                final TransferInfo transferInfo = student.getTransferInfo();
                if (null != transferInfo && transferInfo._containInfo && transferInfo.isPrintBiko(term)) {
                    VrsOutn( "NOTE", line, transferInfo._info);  //備考
                }
            }
        }

        /**
         *  PrepareStatement作成
         *     日付、校時、科目
         *     学期を超えて期間指定可なのでSCH_CHR_DATの学期で他の講座関連レコードを取り出す！
         *     単位制の場合は( schooldiv == 0 )科目を出力する個所に校時名称を固定で出力する！
         *     04/08/18 講座クラスデータの同時展開の講座コードゼロに対応
         *     04/12/31 講座名称を科目名の代用に使用を追加
         *     05/01/08 講座名を出力す場合、同時展開クラスは群名称を出力する
         */
        private static String sqlSchduleData(final Param param, final int schooldiv, final Term term) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH T_MAIN AS ( ");
            stb.append(" SELECT  W1.EXECUTEDATE, W1.PERIODCD, W1.CHAIRCD, ");
            stb.append("         W2.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append("         W2.SUBCLASSCD AS SUBCLASSCD, W2.GROUPCD, ");
            stb.append("         DAYOFWEEK_ISO(W1.EXECUTEDATE)AS DAYOFWEEK, ");
            stb.append("         Meisyou_Get(W1.PERIODCD,'B001',1) AS PERIODNAME, ");
            if (schooldiv == 0) {
                stb.append("         W5.SUBCLASSABBV AS SUBCLASSNAME, ");
            } else {
                stb.append("         W5.SUBCLASSNAME, ");
            }
            stb.append("         W6.CLASSNAME, ");
            stb.append("         W7.GROUPNAME  ");
            stb.append("         ,W2.CHAIRNAME,W1.ATTENDNO, W1.TESTITEMNAME ");
            stb.append(" FROM    (SELECT K3.EXECUTEDATE ");
            stb.append("               , K3.PERIODCD ");
            stb.append("               , K3.CHAIRCD ");
            stb.append("               , K3.YEAR ");
            stb.append("               , K3.SEMESTER  ");
            stb.append("               , MIN(K0.ATTENDNO) AS ATTENDNO ");
            stb.append("               , CAST(NULL AS VARCHAR(1)) AS TESTITEMNAME ");
            stb.append("          FROM   SCHREG_REGD_DAT K0 ");
            stb.append("                 INNER JOIN CHAIR_STD_DAT K1 ON ");
            stb.append("                     K0.YEAR = K1.YEAR AND  ");
            stb.append("                     K0.SEMESTER = K1.SEMESTER AND ");
            stb.append("                     K0.SCHREGNO = K1.SCHREGNO ");
            stb.append("                 INNER JOIN SCH_CHR_DAT K3 ON ");
            stb.append("                     K3.EXECUTEDATE BETWEEN K1.APPDATE AND K1.APPENDDATE AND ");
            stb.append("                     K3.CHAIRCD = K1.CHAIRCD AND ");
            stb.append("                     K3.YEAR = K1.YEAR AND  ");
            stb.append("                     K3.SEMESTER = K1.SEMESTER ");
            stb.append("          WHERE  K0.YEAR = '" + param._year + "' ");
            stb.append("             AND K0.GRADE = '" + param._grade + "' ");
            stb.append("             AND K0.HR_CLASS = '" + param._hrClass + "' ");
            stb.append("             AND K3.EXECUTEDATE BETWEEN '" + term._sdate + "' AND '" + term._edate + "' ");
            //校時別科目一覧表, 2:全て出力
            if (schooldiv == 1 && param._radio1.equals("2")) {
                if (param._onlySunday != null) {
                    //日曜日のみ出力
                    stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) = 7 ");
                } else {
                    //日曜日以外出力
                    stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) not in (7) ");
                }
            }
            stb.append("          GROUP BY K3.EXECUTEDATE, K3.PERIODCD, K3.CHAIRCD, K3.YEAR, K3.SEMESTER ");

            if (schooldiv != 1) {
                stb.append("     UNION ALL ");
                stb.append("          SELECT K3.EXECUTEDATE ");
                stb.append("               , K3.PERIODCD ");
                stb.append("               , K3.CHAIRCD ");
                stb.append("               , K3.YEAR ");
                stb.append("               , K3.SEMESTER ");
                stb.append("               , MIN(K0.ATTENDNO) AS ATTENDNO ");
                stb.append("               , K5.TESTITEMNAME ");
                stb.append("          FROM   SCHREG_REGD_DAT K0 ");
                stb.append("                 INNER JOIN CHAIR_STD_DAT K1 ON ");
                stb.append("                     K0.YEAR = K1.YEAR AND  ");
                stb.append("                     K0.SEMESTER = K1.SEMESTER AND  ");
                stb.append("                     K0.SCHREGNO = K1.SCHREGNO ");
                stb.append("                 INNER JOIN SCH_CHR_TEST K3 ON ");
                stb.append("                     K3.EXECUTEDATE BETWEEN K1.APPDATE AND K1.APPENDDATE AND ");
                stb.append("                     K3.YEAR = K1.YEAR AND  ");
                stb.append("                     K3.SEMESTER = K1.SEMESTER AND  ");
                stb.append("                     K3.CHAIRCD = K1.CHAIRCD ");
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                    stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV K5 ON K5.YEAR = K3.YEAR ");
                    stb.append("                         AND K5.SEMESTER = K3.SEMESTER ");
                    stb.append("                         AND K5.TESTKINDCD = K3.TESTKINDCD ");
                    stb.append("                         AND K5.TESTITEMCD = K3.TESTITEMCD ");
                    stb.append("                         AND K5.SCORE_DIV = '01' ");
                } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(param._useTestCountflg)) {
                    stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW K5 ON K5.YEAR = K3.YEAR ");
                    stb.append("                         AND K5.SEMESTER = K3.SEMESTER ");
                    stb.append("                         AND K5.TESTKINDCD = K3.TESTKINDCD ");
                    stb.append("                         AND K5.TESTITEMCD = K3.TESTITEMCD ");
                } else {
                    stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG K5 ON K5.YEAR = K3.YEAR ");
                    stb.append("                         AND K5.TESTKINDCD = K3.TESTKINDCD ");
                    stb.append("                         AND K5.TESTITEMCD = K3.TESTITEMCD ");
                }
                stb.append("          WHERE  K0.YEAR = '" + param._year + "' ");
                stb.append("             AND K0.GRADE = '" + param._grade + "' ");
                stb.append("             AND K0.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("             AND K3.EXECUTEDATE BETWEEN '" + term._sdate + "' AND '" + term._edate + "' ");
                //校時別科目一覧表, 2:全て出力
                if (schooldiv == 1 && param._radio1.equals("2")) {
                    if (param._onlySunday != null) {
                        //日曜日のみ出力
                        stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) = 7 ");
                    } else {
                        //日曜日以外出力
                        stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) not in (7) ");
                    }
                }
                stb.append("          GROUP BY K3.EXECUTEDATE, K3.PERIODCD, K3.CHAIRCD, K3.YEAR, K3.SEMESTER, K5.TESTITEMNAME ");
            }

            stb.append("          ) W1  ");
            stb.append("         INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("         LEFT JOIN SUBCLASS_MST W5 ON W5.SUBCLASSCD = W2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND W5.CLASSCD = W2.CLASSCD ");
                stb.append("         AND W5.SCHOOL_KIND = W2.SCHOOL_KIND ");
                stb.append("         AND W5.CURRICULUM_CD = W2.CURRICULUM_CD ");
            }
            stb.append("         LEFT JOIN CLASS_MST W6 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         W6.CLASSCD = W2.CLASSCD ");
                stb.append("         AND W6.SCHOOL_KIND = W2.SCHOOL_KIND ");
            } else {
                stb.append("         W6.CLASSCD = SUBSTR(W2.SUBCLASSCD,1,2) ");
            }
            stb.append("         LEFT JOIN V_ELECTCLASS_MST W7 ON W7.YEAR = W1.YEAR AND W7.GROUPCD = W2.GROUPCD ");
            stb.append("     ) ");

            stb.append(" , T_SUBCLASS_CNT AS ( ");
            stb.append(" SELECT  EXECUTEDATE, PERIODCD, COUNT(DISTINCT SUBCLASSCD) AS SUBCLASS_CNT, COUNT(DISTINCT CHAIRCD) AS CHAIR_CNT  ");
            stb.append(" FROM    T_MAIN  ");
            stb.append(" GROUP BY EXECUTEDATE, PERIODCD ");
            stb.append("     ) ");

            stb.append(" SELECT  T1.*, T2.SUBCLASS_CNT, T2.CHAIR_CNT ");
            stb.append(" FROM    T_MAIN T1 ");
            stb.append("         LEFT JOIN T_SUBCLASS_CNT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE AND T2.PERIODCD = T1.PERIODCD ");
            if (schooldiv == 0) {
                stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD, T1.ATTENDNO ");
            } else {
                stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD, T1.SUBCLASSCD, T1.CHAIRCD ");
            }

            return stb.toString();
        }

        private static class Week {
            final Term _term;
            List<Integer> _periodStartList;
            List<String> _dateList;
            Map<String, Integer> _dateDayofweekMap;
            int _weekint;
            Map<Integer, String> _subjectNameMap;
            Week(final Term weekTerm) {
                _term = weekTerm;
            }
        }

        private List<Week> getWeekList(final DB2UDB db2, final Param param, final Term term) {
            final Calendar cals = toCalendar(term._sdate);      //開始日付(週)
            final Calendar cale = toCalendar(term._edate);      //終了日付(週)

            final List<Week> weekList = new ArrayList<Week>();
            for (;;) {
                final Week week = setWeekList(db2, param, cals, cale);    //一週間の日付をセット
                if (week._weekint == 9) {
                    break;                //戻り値が9なら終了
                }
                if (week._weekint == 1) {
                    continue;             //戻り値が1なら出力無し
                }

                weekList.add(week);
            }
            for (final Week week : weekList) {
                setWeekHead(week, db2, param); //校時及び科目を取得するメソッド
            }
            return weekList;
        }

        /**
         *  一週間の日付の取得
         *
         */
        private Week setWeekList(final DB2UDB db2, final Param param, final Calendar cals, final Calendar cale) {
            String scheSdate;
            String scheEdate;
            final List dateList = new ArrayList();
            final List<Integer> printPeriodStartList = new ArrayList<Integer>();
            final Map<String, Integer> dateDayofweekMap = new HashMap();
            if (param._onlySunday != null) {
                scheSdate = null;  //ページ週の開始日
                scheEdate = null;  //ページ週の終了日
            } else {
                scheSdate = toDateString(cals.getTime());         //ページ週の月曜日をセット
                scheEdate = null;
            }
            try {
                if (cals.after(cale)) {
                    final Week week = new Week(null);
                    week._weekint = 9;
                    return week;                 //日付が印刷範囲終了日を越えたら終了！
                }
                for (int j = 0; j < 8; cals.add(Calendar.DATE, 1), j++) { //月〜日まで
                    if (cals.after(cale)) {
                        break;                //日付が印刷範囲終了日を越えたら終了！
                    }
                    final String datestr = toDateString(cals.getTime());
                    //getSemesterValueでcalsの属する学期を取得し学期内なら日付を出力する->夏休み等は除外
                    final int semesterValue2 = getSemesterValue2(param, datestr);
                    if (param._onlySunday != null) {
                        if (semesterValue2 > 0) {
                            final int cweek = cals.get(Calendar.DAY_OF_WEEK);
                            if (cweek != Calendar.SUNDAY) {
                                continue;  //日曜日以外は回避
                            }
                            dateList.add(datestr);
                            scheSdate = datestr; //ページ週の開始日をセット
                            scheEdate = datestr; //ページ週の終了日をセット
                            printPeriodStartList.add(new Integer(0));
                            dateDayofweekMap.put(datestr, new Integer(0));
                            break;
                        }
                    } else {
                        if (semesterValue2 > 0) {
                            int cweek = cals.get(Calendar.DAY_OF_WEEK);
                            if (cweek == Calendar.SUNDAY) {
                                cweek = 8;
                            }
                            dateList.add(datestr);
                            scheEdate = datestr; //ページ週の最終日をセット
                            printPeriodStartList.add(new Integer(cweek - 2));
                            dateDayofweekMap.put(datestr, new Integer(cweek - 1));
                        }
                        if (_periodnum == 16 && cals.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                            break;
                        } else if (cals.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                            break;
                        }
                    }
                }
                //次の出力日付をセット
                if (!cals.after(cale)) {
                    final int cweek = cals.get(Calendar.DAY_OF_WEEK);
                    if (cweek == Calendar.SUNDAY) {
                        cals.add(Calendar.DATE,1);                //日曜日は翌月曜日をセット
                    } else if (cweek > Calendar.MONDAY) {
                        cals.add(Calendar.DATE, 9 - cweek);        //月曜日を越えたら来る月曜日をセット
                    }
                }
            } catch (Exception ex) {
                log.warn("svf-out error!",ex);
                final Week week = new Week(null);
                week._weekint = 9;
                return week;
            }
            if (scheEdate == null) {
                final Week week = new Week(null);
                week._weekint = 1;
                return week;                      //出力無し
            }
            final Week week = new Week(new Term("WEEK_TERM", scheSdate, scheEdate, Term.dateFromToString(db2, scheSdate, scheEdate)));
            week._dateList = dateList;
            week._periodStartList = printPeriodStartList;
            week._dateDayofweekMap = dateDayofweekMap;
            week._weekint = 0;
            return week; //継続
        }

        /**
         *  一週間の校時科目の出力
         */
        private void setWeekHead(final Week week, final DB2UDB db2, final Param param) {

            int periodcdBefore = 0;   //校時コードの保存
            int dayofweekBefore = 0;      //曜日コードの保存

            final String sql = sqlSchduleData(param, 1, week._term);
            log.debug(" sql = " + sql);

            week._subjectNameMap = new HashMap();

            for (final Map row : KnjDbUtils.query(db2, sql)) {

                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String periodcd = KnjDbUtils.getString(row, "PERIODCD");
                final int dayofweek = getInt(row, "DAYOFWEEK", 0);
                final String testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                final String chairname = KnjDbUtils.getString(row, "CHAIRNAME");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final int groupcd = getInt(row, "GROUPCD", 0);
                final String groupname = KnjDbUtils.getString(row, "GROUPNAME");
                final int chairCnt = getInt(row, "CHAIR_CNT", 0);
                final int subclassCnt = getInt(row, "SUBCLASS_CNT", 0);

                final boolean isShrSyurei;
                if ("1".equals(param._useCurriculumcd)) {
                    isShrSyurei = "92".equals(classcd);
                } else {
                    isShrSyurei = subclasscd != null && subclasscd.startsWith("92");
                }
                if (isShrSyurei && "1".equals(param._shrSyurei)) {
                    continue;
                }
                final Integer intperiod = getPeriodPosition(periodcd);
                if (intperiod == null) {
                    continue;
                }
                if (periodcdBefore == intperiod.intValue() && dayofweekBefore == dayofweek) {
                    continue;
                }

                final String subjectname;
                final boolean isKome = groupcd == 0;
                if (param._usechairname) {
                    subjectname = isKome ? getChairname(chairname, (1 < chairCnt ? " *" : "")) : getChairname(groupname, "");
                } else {
                    if (testitemname != null) {
                        subjectname = testitemname;
                    } else {
                        subjectname = isKome ? getChairname(subclassname, (1 < subclassCnt ? " *" : "")) : getChairname(groupname, "");
                    }
                }

                final Integer col = new Integer(getPositionOfLine(param, dayofweek, intperiod.intValue()));
                week._subjectNameMap.put(col, subjectname);

                periodcdBefore = intperiod.intValue();
                dayofweekBefore = dayofweek;
            }
        }

        /**
         *   講座名称を出力
         *       ６文字を超えた分は出力しない
         */
        private static String getChairname(final String name, final String kome) {
            final int komelen = StringUtils.defaultString(kome).length();
            final int chairnamelenMax = 6 - komelen;
            return (null == name ? "" : name.length() > chairnamelenMax ? name.substring(0, chairnamelenMax) : name) + kome;
        }

        /**
         *   svf print 校時別科目一覧表 印刷処理
         */
        public boolean printSvfSubclass(final Vrw32alp svf, final DB2UDB db2) {

            // 印刷範囲の再設定
            final Term term = Term.getTermValue(db2, _param);

            //見出しデータ取得
            //  ＳＶＦフォームの設定->学期制による
            svf.VrSetForm("KNJC043_5.frm", 4);

            //  SVF出力編集->年度、印刷範囲
            //出力処理
            svf.VrsOut("NENDO", _param._nendo);        //年度
            svf.VrsOut("HR_CLASS", _param._hrname);        //組名称
            svf.VrsOut("PRINT_DATE", term._dateFromToString);        //印刷範囲

            printWeekHeadSubclass(svf, db2, term);

            return hasdata;
        }

        /**
         *  一週間の校時科目の出力
         */
        private void printWeekHeadSubclass(final Vrw32alp svf, final DB2UDB db2, final Term term) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlSchduleData(_param, 1, term);
                ps = db2.prepareStatement(sql);      //校時科目

                log.debug("ps2 start");
                rs = ps.executeQuery();
                log.debug("ps2 end");

                //DB読み込みとSVF出力
                String peri_msk     = "";
                String subclass_msk = "";
                String chair_msk    = "";
                String chairname    = "";
                String seq    = "";
                String peri_msk_tmp     = "";
                String subclass_msk_tmp = "";
                int record_cnt = 0; //改ページ用
                while (rs.next()) {
                    //改ページ行数
                    if (record_cnt > 49) {
                        record_cnt = 0;
                    }
                    //改行
                    final String executedate = rs.getString("EXECUTEDATE");
                    final String periodcd = rs.getString("PERIODCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String tmp_flg_code = executedate + periodcd + subclasscd;
                    if (!subclass_msk.equals("") && !subclass_msk.equals(tmp_flg_code)) {
                        if (record_cnt != 0) {
                            if (peri_msk.equals(peri_msk_tmp))          svf.VrsOut("PERIOD"   , "" );
                            if (subclass_msk.equals(subclass_msk_tmp))  svf.VrsOut("SUBCLASS"   , "" );
                        }
                        svf.VrEndRecord();
                        record_cnt++;
                        peri_msk_tmp     = peri_msk;
                        subclass_msk_tmp = subclass_msk;
                        chairname = "";
                        seq = "";
                    }
                    //改行
                    if (!chairname.equals("")) {
                        seq = "、";
                    }
                    String chairname_tmp = chairname + seq + rs.getString("CHAIRNAME");
                    if (40 < chairname_tmp.length()) {
                        if (record_cnt != 0) {
                            if (peri_msk.equals(peri_msk_tmp))          svf.VrsOut("PERIOD"   , "" );
                            if (subclass_msk.equals(subclass_msk_tmp))  svf.VrsOut("SUBCLASS"   , "" );
                        }
                        svf.VrEndRecord();
                        record_cnt++;
                        peri_msk_tmp     = peri_msk;
                        subclass_msk_tmp = subclass_msk;
                        chairname = "";
                        seq = "";
                    }
                    //セット
                    svf.VrsOut("DATE"     , KNJ_EditDate.h_format_S(executedate,"M") + "/" + KNJ_EditDate.h_format_S(executedate,"d") + "(" + KNJ_EditDate.h_format_W(executedate) + ")" );
                    svf.VrsOut("PERIOD"   , rs.getString("PERIODNAME") );
                    svf.VrsOut("SUBCLASS" , rs.getString("SUBCLASSNAME") );
                    if (!chairname.equals("")) {
                        seq = "、";
                    }
                    chairname = chairname + seq + rs.getString("CHAIRNAME");
                    svf.VrsOut("CHAIRNAME", chairname);
                    //マスク
                    peri_msk     = executedate + periodcd;
                    subclass_msk = peri_msk + subclasscd;
                    chair_msk    = subclass_msk + rs.getString("CHAIRCD");
                    svf.VrsOut("PERI_MSK"     , peri_msk);
                    svf.VrsOut("SUBCLASS_MSK" , subclass_msk);
                    svf.VrsOut("CHAIR_MSK"    , chair_msk);
                    hasdata = true;
                }
                //改行
                if (!subclass_msk.equals("")) {
                    if (record_cnt != 0) {
                        if (peri_msk.equals(peri_msk_tmp)) {
                            svf.VrsOut("PERIOD"   , "" );
                        }
                        if (subclass_msk.equals(subclass_msk_tmp)) {
                            svf.VrsOut("SUBCLASS"   , "" );
                        }
                    }
                    svf.VrEndRecord();
                }
            } catch (Exception ex) {
                log.warn("ResuleSet read error!", ex);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private static class Param {

        final static String KNJC043B = "KNJC043B";

        final String _year; //年度
        final String _semester; //学期
        final String _gradeHrClass; //学年
        final String _grade;
        final String _hrClass;
        final String _output1;
        final String _output2;
        final String _date1; // 印刷範囲開始日
        final String _date2; // 印刷範囲終了日
        final String _ctrlDateFormatJp;
        final String _nendo;
        final String _hrname;
        final String _staffname;
        final String _teacher2;
        final String _output4; // 1:空行を詰めて印字
        final String _radio1; //1：コアタイム出力　2：全て出力
        final String _onlySunday; // 日曜日のみ出力
        final String _output5; // 「未」を表示する
        final String _date3; // ログイン日付
        final String _check2; // 校時別科目一覧表
        final String _outputDiremark; // null:標準パターンを使用、 1:明細票（４０名＋出欠備考欄）パターンを使用
        final String _isOnedayAttendCheck; // 1日出欠判定チェック
        final String _shrSyurei; // 「SHR」「終礼」の科目を出力しない
        final String _useTestCountflg;
        final String _knjc043bPrintLateEarly;
        final String _knjc043tMeisaiPrintAttendDayDat;
        final String _knjc043bKessekiTitleKotei;
        final boolean _isPrintOnedayAttendInfo;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;
        final List<File> _tmpFileList = new ArrayList();

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final boolean _hasAttendDatDiRemarkCd;
        final String _calcRange;

        final String _z010Name1;
        final boolean _isKindai;
        final boolean _isKumamoto;
        final boolean _isSapporo;
        final boolean _isIbaraki;
        final boolean _isTokiwagi;
        final boolean _isOsakashinnai;

        final boolean _useschchrcountflg;
        boolean _usefromtoperiod = false;
        boolean _useabsencehigh = false;
        boolean _usechairname = true;

        final KNJSchoolMst _knjSchoolMst;
//        final KNJDefineSchool _definecode;       //各学校における定数等設定
        final int _semesterdiv;
        final String _prgid;
        final Map<String, String> _dicdDatName1Map;
        final Map _attendParamMap;

        private Date _loginDate;

        private Map<String, Term> _semesterDates;
        final List _semesternameList;

        final boolean _isOutputDebugAll;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugVrsout;
        final String _use_attendDayDiCd_00;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrClass.substring(0, 2);
            _hrClass = _gradeHrClass.substring(2);
            _output1 = request.getParameter("OUTPUT1");
            _output2 = request.getParameter("OUTPUT2");
            _date1 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE1"));
            _date2 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE2"));
            _output4 = request.getParameter("OUTPUT4");
            _radio1 = StringUtils.defaultString(request.getParameter("RADIO1"), "2");
            if ("2".equals(_radio1) && request.getParameter("CHECK1") != null) {
                _onlySunday = "1";    //日曜日のみ出力
            } else {
                _onlySunday = null;
            }
            _output5 = request.getParameter("OUTPUT5");
            _date3 = request.getParameter("DATE3");
            _check2 = request.getParameter("CHECK2");
            _outputDiremark = request.getParameter("OUTPUT_DIREMARK");
            _isOnedayAttendCheck = request.getParameter("ONEDAY_ATTEND_CHECK");
            _shrSyurei = request.getParameter("SHR_SYUREI");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _knjc043bPrintLateEarly = request.getParameter("knjc043bPrintLateEarly");
            _knjc043tMeisaiPrintAttendDayDat = request.getParameter("knjc043tMeisaiPrintAttendDayDat");
            _knjc043bKessekiTitleKotei = request.getParameter("knjc043bKessekiTitleKotei");
            _use_attendDayDiCd_00 = request.getParameter("use_attendDayDiCd_00");

            _ctrlDateFormatJp = KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT CTRL_DATE FROM CONTROL_MST WHERE CTRL_NO = '01' "))); //現在処理日

            //  SVF出力編集->年度、印刷範囲
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            //  組名称及び担任名の取得
            final Map<String, String> hrRow = Hrclass_Staff(db2, _year, _semester, _grade, _hrClass);
            String hrclass_name = "";     //組名称
            String staff_name = "";       //担任名
            String staff_name2 = "";      //担任名2
            if (!hrRow.isEmpty()) {
                hrclass_name = KnjDbUtils.getString(hrRow, "HR_NAME");
                staff_name = KnjDbUtils.getString(hrRow, "STAFFNAME");
                staff_name2 = KnjDbUtils.getString(hrRow, "STAFFNAME2");
            }
            _hrname = hrclass_name; //組名称
            _staffname = staff_name; //担任名
            _teacher2 = staff_name2; //担任名

            _semesternameList = getSemesterNameList(db2, _year, _grade);

            _z010Name1 = setZ010Name1(db2);
            _isKindai = "KINDAI".equals(_z010Name1) || "KINJUNIOR".equals(_z010Name1);
            _isKumamoto = "kumamoto".equals(_z010Name1);
            _isSapporo = "sapporo".equals(_z010Name1);
            _isIbaraki = false;
            _isTokiwagi = "tokiwagi".equals(_z010Name1);
            _isOsakashinnai = "osakashinnai".equals(_z010Name1);
            _isPrintOnedayAttendInfo = _isIbaraki;
            if ("1".equals(_knjc043bKessekiTitleKotei)) {
                _dicdDatName1Map = new HashMap<String, String>();
                _dicdDatName1Map.put("4", "欠席");
                _dicdDatName1Map.put("5", "届出欠席");
            } else {
                _dicdDatName1Map = setDicdDatNameMap(db2);
            }
            _knjSchoolMst = setKnjSchoolMst(db2);
//            _definecode = setClasscode(db2);

            _useschchrcountflg = true;
            if (_isKindai) {
                _semesterdiv = 3;
            } else {
                _semesterdiv = NumberUtils.isDigits(_knjSchoolMst._semesterDiv) ? Integer.parseInt(_knjSchoolMst._semesterDiv) : 2;
                _usefromtoperiod = true;
                _useabsencehigh = true;
            }
            setUseChairname(db2);

            _prgid = request.getParameter("PRGID");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");

            _loginDate = setNowDate(_date3);
            _calcRange = "年度初め " + FROM_TO_MARK + " " + KNJ_EditDate.h_format_JP(db2, _date2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);

            _hasAttendDatDiRemarkCd = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_DAT", "DI_REMARK_CD");
            _semesterDates = setSemesterDates(db2);

            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugVrsout = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "vrsout");
        }

        /**
         *  科目表示の設定値を取得
         */
        public void setUseChairname(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  NAMECD1, NAMECD2 ");
            stb.append("FROM    NAME_YDAT ");
            stb.append("WHERE   NAMECD1 = 'C000' AND NAMECD2 = '2' ");
            _usechairname = KnjDbUtils.query(db2, stb.toString()).size() > 0; // 「1件でもあれば」?
        }

        public void close() {
            for (final File file : _tmpFileList) {
                if (file.exists()) {
                    final boolean deleted = file.delete();
                    if (_isOutputDebug) {
                        log.info(" delete file " + file.getAbsolutePath() + " : deleted? " + deleted);
                    }
                }
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC043' AND NAME = '" + propName + "' "));
        }

        /**
         * @return 現在日付 "yyyy-MM-dd" を戻します。
         */
        private static Date setNowDate(String str) {
            if (null == str) {
                str = toDateString(new Date());
            }
            return toDate(str);
        }

        /** ＤＢより組名称及び担任名を取得するメソッド **/
        public Map<String, String> Hrclass_Staff(final DB2UDB db2, final String year, final String semester, final String grade, final String hrClass) {
            String sql;
            sql = "SELECT "
                    + "HR_NAME,"
                    + "HR_NAMEABBV,"
                    + "W1.STAFFNAME,"
                    + "W3.STAFFNAME AS STAFFNAME2 "
                + "FROM "
                    + "SCHREG_REGD_HDAT W2 "
                    + "LEFT JOIN STAFF_MST W1 ON W1.STAFFCD=W2.TR_CD1 "
                    + "LEFT JOIN STAFF_MST W3 ON W3.STAFFCD=W2.TR_CD2 "
                + "WHERE "
                        + "YEAR = '" + year + "' "
                    + "AND GRADE = '" + grade + "' AND HR_CLASS = '" + hrClass + "' ";
            if (!SEMEALL.equals(semester))  sql = sql               //学期指定の場合
                    + "AND SEMESTER = '" + semester + "'";
            else                        sql = sql               //学年指定の場合
                    + "AND SEMESTER = (SELECT "
                                        + "MAX(SEMESTER) "
                                    + "FROM "
                                        + "SCHREG_REGD_HDAT W3 "
                                    + "WHERE "
                                            + "W2.YEAR = W3.YEAR "
                                        + "AND W2.GRADE = W3.GRADE "
                                        + "AND W2.HR_CLASS = W3.HR_CLASS)";

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
        }

        private Map setSemesterDates(final DB2UDB db2) {
            final Map rtn = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT SEMESTER, SDATE, EDATE FROM V_SEMESTER_GRADE_MST ");
            stb.append(" WHERE YEAR = '" + _year + "' ");
            stb.append("   AND GRADE = '" + _grade + "' ");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String semester = KnjDbUtils.getString(row, "SEMESTER");
                if (null == semester) {
                    continue;
                }
                final String sdate = KnjDbUtils.getString(row, "SDATE");
                final String edate = KnjDbUtils.getString(row, "EDATE");
                rtn.put(semester, new Term("SEMESTER_" + semester + "_TERM", sdate, edate, Term.dateFromToString(db2, sdate, edate)));
            }
            return rtn;
        }

        public List<String> getSemesterNameList(final DB2UDB db2, final String year, final String grade) {
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ORDER BY SEMESTER");
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SEMESTERNAME");
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private Map setDicdDatNameMap(DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT DI_CD, DI_NAME1 FROM ATTEND_DI_CD_DAT WHERE YEAR = '" + _year + "' "), "DI_CD", "DI_NAME1");
        }

        public String getDicdDatNameMap(final String namecd2) {
            return _dicdDatName1Map.get(namecd2);
        }

        private KNJSchoolMst setKnjSchoolMst(final DB2UDB db2) {
            final Map smParamMap = new HashMap();
            if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
                smParamMap.put("SCHOOL_KIND", schoolKind);
            }
            KNJSchoolMst knjSchoolMst = null;
            try {
                knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            return knjSchoolMst;
        }

        /**
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool setClasscode(final DB2UDB db2) {
            KNJDefineSchool definecode = null;
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
                log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }
    }
}
// eof
