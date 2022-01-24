// kanji=漢字
/*
 * $Id: eb6c8f91b7dab60507af3807d1fb4f25776490bd $
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
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *
 *  学校教育システム 賢者 [出欠管理]  出席簿（公簿）
 *
 */

public class KNJC043S {

    private static final Log log = LogFactory.getLog(KNJC043S.class);

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
                if (new KNJC043_LIST(svf, param).printSvf(db2)) {
                    hasdata = true;
                }
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
        	if (null != param) {
        		param.close();
        	}
            // 終了処理
            if (!hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            db2.commit();
            db2.close();
        }
    }   //doGetの括り
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75509 $"); // CVSキーワードの取り扱いに注意
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
        Term(final String key, final String sdate, final String edate) {
            _key = key;
            _sdate = sdate;
            _edate = edate;
        }
        
        String dateRangeKey() {
            return _sdate + ":" + _edate;
        }

        public String getDateFromTo(final DB2UDB db2) {
            String dateFromTo = null;
            try {
                dateFromTo = KNJ_EditDate.h_format_JP(db2, _sdate) + " " + FROM_TO_MARK + " " + KNJ_EditDate.h_format_JP(db2, _edate);
            } catch (Exception ex) {
                log.warn("now date-get error!", ex);
            }
            return dateFromTo;
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
            final String sqlSsemeSdate;
            if ("2".equals(param._hrClassType)) {
            	sqlSsemeSdate = " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + param._year + "' AND '" + param._date1 + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '9' ";
            } else {
            	sqlSsemeSdate = " SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "' AND '" + param._date1 + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '9' ";
            }
            Date ssemeSdate = toDate(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlSsemeSdate)));

            // edayを含む学期の終了日
            final String sqlEsemeEdate;
            if ("2".equals(param._hrClassType)) {
            	sqlEsemeEdate = " SELECT EDATE FROM SEMESTER_MST WHERE YEAR = '" + param._year + "' AND '" + param._date2 + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '9'";
            } else {
            	sqlEsemeEdate = " SELECT EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "' AND '" + param._date2 + "' BETWEEN SDATE AND EDATE AND SEMESTER <> '9'";
            }
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
//                if (param._onlySunday == null && cweek2 < 8) {
                    cal.add(Calendar.DATE, (8 - cweek2)); // calが日曜日でなければ来る日曜日をセット
//                }
                endD = toDateString(min(cal.getTime(), esemeEdate)); // dateとesemeEdateを比較し、dateが学期終了日より後なら学期終了日を範囲終了日とする
            } catch (Exception ex) {
                log.error("ReturnVal getTermValue 2 error!", ex);
            }

//            // 9学期 SDATE
//            final String sqlSeme9Sdate = " SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "' AND SEMESTER = '9' ";
//            final String nendD = StringUtils.defaultString(DB.getOne(DB.query(db2, sqlSeme9Sdate)), param._year + "-04-01");

            final Term term = new Term("TERM", startD, endD);
//            valu.put("2", nendD);
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
            _semesAttendance = new HashMap();
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
//        int _sickOnly;          //病欠
//        int _noticeOnly;        //欠席（届けあり）
//        int _nonoticeOnly;      //欠席（届けなし）
        int _late;              //遅刻
        int _early;             //早退
        int _lesson;            //授業日数
        int _mlesson;           //出席すべき日数
        int _present;           //出席日数
//        int _jugyoLate;         //授業遅刻
//        int _jugyoEarly;        //授業早退
//        int _jugyoKekka;        //授業欠課

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
//            _sickOnly = sickOnly;
//            _noticeOnly = noticeOnly;
//            _nonoticeOnly = nonoticeOnly;
            _late = late;
            _early = early;
            _lesson = lesson;
            _mlesson = mlesson;
            _present = present;
//            _jugyoLate = jugyoLate;
//            _jugyoEarly = jugyoEarly;
//            _jugyoKekka = jugyoKekka;
        }
        
        public void add(final Attendance a) {
        	_absent += a._absent;
            _abroad += a._abroad;
            _mourning += a._mourning;
            _suspend += a._suspend;
            _koudome += a._koudome;
            _virus += a._virus;
            _sick += a._sick;
//            _sickOnly += a._sickOnly;
//            _noticeOnly += a._noticeOnly;
//            _nonoticeOnly += a._nonoticeOnly;
            _late += a._late;
            _early += a._early;
            _lesson += a._lesson;
            _mlesson += a._mlesson;
            _present += a._present;
//            _jugyoLate += a._jugyoLate;
//            _jugyoEarly += a._jugyoEarly;
//            _jugyoKekka += a._jugyoKekka;
        }
        
        
        public static String getSubclassRowSick2Sum(final Collection<Map<String, String>> rowList) {
            BigDecimal sick2Total = new BigDecimal(0);
            for (final Map<String, String> subclassRow : rowList) {
            	final String sick2 = KnjDbUtils.getString(subclassRow, "SICK2");
            	if (NumberUtils.isDigits(sick2)) {
            		sick2Total = sick2Total.add(new BigDecimal(sick2));
            	}
            }
            return sick2Total.setScale(0, BigDecimal.ROUND_HALF_DOWN).toString();
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

        public Map<String, KNJC043_LIST.Attend> _datePeriodKintaiMap = new HashMap();
//        public Map<String, TreeMap<Integer, String>> _datePeriodKintaiMapMap = new HashMap();
//        public Map<String, TreeMap<Integer, String>> _datePeriodRepDiCdMapMap = new HashMap();
//        public Map<String, String> _onedayAttendComment = new HashMap<String, String>();
//        public Map<String, Integer> _onedayAttendAttributeTarget = new HashMap();
        public Map<String, Map<String, String>> _termAttendInfoMap = new HashMap<String, Map<String, String>>();
        public Map<String, TreeMap<String, Map<String, String>>> _termAttendSubclassInfoMap = new HashMap<String, TreeMap<String, Map<String, String>>>();
        final Map<String, TreeMap<String, TreeMap<String, String>>> _attendDiRemarkMap = new TreeMap();
        StudentAttendance _studentAttendance;
        final Map<String, List<Map<String, String>>> _lhrShukeiRowList = new HashMap<String, List<Map<String, String>>>();
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
            if ("2".equals(param._hrClassType)) {
            	stb.append(    "FROM    SCHREG_REGD_GHR_DAT ");
            } else {
            	stb.append(    "FROM    SCHREG_REGD_DAT ");
            }
            stb.append(    "WHERE   YEAR = '" + param._year + "' ");
            stb.append(        "AND SEMESTER = '" + param._semester + "' ");
            if ("2".equals(param._hrClassType)) {
            	stb.append(        "AND GHR_CD = '" + param._ghrCd + "' ");
            } else {
            	stb.append(        "AND GRADE = '" + param._grade + "' ");
            	stb.append(        "AND HR_CLASS = '" + param._hrClass + "' ");
            }
            stb.append(") ");
            
            stb.append(",TRANSFER_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, ");
            stb.append(           " MAX(TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append(    "FROM    SCHREG_TRANSFER_DAT T1 ");
            if ("2".equals(param._hrClassType)) {
            	stb.append(    "INNER JOIN SEMESTER_MST T3 ON T1.TRANSFER_SDATE BETWEEN T3.SDATE AND T3.EDATE ");
            } else {
            	stb.append(    "INNER JOIN V_SEMESTER_GRADE_MST T3 ON T1.TRANSFER_SDATE BETWEEN T3.SDATE AND T3.EDATE ");
            }
            stb.append(    "INNER JOIN SCHNO_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(    "WHERE   T1.TRANSFERCD IN('1','2') ");
            stb.append(        "AND T3.YEAR = '" + param._year + "' ");
            stb.append(        "AND T3.SEMESTER <= '" + param._semester + "' ");
            if (!"2".equals(param._hrClassType)) {
            	stb.append(        "AND T3.GRADE = '" + param._grade + "' ");
            }
            stb.append(    "GROUP BY T1.SCHREGNO ");
            stb.append(") ");
            
            stb.append(",TRANSFER_B AS(");
            stb.append(    "SELECT  S1.SCHREGNO, S1.TRANSFER_SDATE, S1.TRANSFER_EDATE, S1.TRANSFERCD ");
            stb.append(    "FROM    SCHREG_TRANSFER_DAT S1 ");
            stb.append(    "WHERE   EXISTS(SELECT 'X' FROM TRANSFER_A S2 WHERE S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(                                                   "AND S1.TRANSFER_SDATE = S2.TRANSFER_SDATE) ");
            stb.append(") ");
            
            stb.append("SELECT  W1.SCHREGNO, ");
            if ("2".equals(param._hrClassType)) {
            	stb.append("        W1.GHR_ATTENDNO AS ATTENDNO, ");
            } else {
            	stb.append("        W1.ATTENDNO, ");
            }
            stb.append("        W3.NAME,");
            stb.append(        "CASE WHEN W4.GRD_DIV IS NOT NULL THEN '1' ELSE '0' END AS KBN_DIV1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN NMA003.NAME1 ");
            stb.append(                                         " ELSE NMA002.NAME1 END AS KBN_NAME1, ");
            stb.append(        "T_TRANS.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "T_TRANS.TRANSFER_EDATE AS KBN_DATE2E,");
            stb.append(        "NMA004.NAME1 AS KBN_NAME2 ");
            if ("2".equals(param._hrClassType)) {
            	stb.append("FROM    SCHREG_REGD_GHR_DAT W1 ");
            } else {
            	stb.append("FROM    SCHREG_REGD_DAT W1 ");
            }
            stb.append("INNER   JOIN SCHNO_A ON SCHNO_A.SCHREGNO = W1.SCHREGNO ");
            if ("2".equals(param._hrClassType)) {
            	stb.append("INNER   JOIN SEMESTER_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = W1.YEAR ");
            } else {
            	stb.append("INNER   JOIN V_SEMESTER_GRADE_MST    W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = W1.YEAR AND W2.GRADE = W1.GRADE ");
            }
            stb.append("INNER   JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO AND (W4.GRD_DIV IN('2','3') OR W4.ENT_DIV IN ('4','5')) ");
            stb.append("LEFT    JOIN TRANSFER_B T_TRANS ON T_TRANS.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT    JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = W4.ENT_DIV ");
            stb.append("LEFT    JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = W4.GRD_DIV ");
            stb.append("LEFT    JOIN NAME_MST NMA004 ON NMA004.NAMECD1 = 'A004' AND NMA004.NAMECD2 = T_TRANS.TRANSFERCD ");
            stb.append(    "WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append(        "AND W1.SEMESTER = '" + param._semester + "' ");
            if ("2".equals(param._hrClassType)) {
            	stb.append(        "AND W1.GHR_CD = '" + param._ghrCd + "' ");
            } else {
            	stb.append(        "AND W1.GRADE = '" + param._grade + "' ");
                stb.append(        "AND W1.HR_CLASS = '" + param._hrClass + "' ");
            }
            if ("2".equals(param._hrClassType)) {
            	stb.append("ORDER BY W1.GHR_ATTENDNO");
            } else {
                stb.append("ORDER BY W1.ATTENDNO");
            }
            return stb.toString();

        }
        
//        private static void setAttendOnedayInfo(final DB2UDB db2, final String date, final Param param, final StudentMap studentMap) {
//            
//            param._attendParamMap.put("grade", param._grade);
//            param._attendParamMap.put("hrClass", param._hrClass);
//            final String sql = AttendAccumulate.getAttendSemesSql(
//                    param._year,
//                    param._semester,
//                    date,
//                    date,
//                    param._attendParamMap
//            );
//            
//            for (final Map row : KnjDbUtils.query(db2, sql)) {
//
//                if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
//                    continue;
//                }
//                
//                final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
//                if (null == student || null == student._line) {
//                    continue;
//                }
//                
//                String comment = "";
//                if (getInt(row, "SICK", 0) > 0) {
//                    comment = "欠";
//                } else {
//                    if (getInt(row, "SUSPEND", 0) + getInt(row, "VIRUS", 0) + getInt(row, "KOUDOME", 0) > 0) {
//                        comment += "停";
//                    }
//                    if (getInt(row, "MOURNING", 0) > 0) {
//                        comment += "忌";
//                    }
//                    if (getInt(row, "LATE", 0) > 0) {
//                        comment += "遅";
//                    }
//                    if (getInt(row, "EARLY", 0) > 0) {
//                        comment += "早";
//                    }
//                }
//                if (comment.length() > 0) {
//                    student._onedayAttendComment.put(date, comment);
//                    log.info(" check onedayattend : " + KnjDbUtils.getString(row, "SCHREGNO")  + " " + comment);
//                }
//            }
//        }
        
        /** 
         *  学期集計およびＳＶＦ出力 累計へ累積
         */
        private static void setSumAttend(final DB2UDB db2, final Term term, final Param param, final StudentMap studentMap) {
            if ("2".equals(param._hrClassType)) {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        term._sdate,
                        term._edate,
                        param._attendParamMap
                );

                PreparedStatement ps = null;
                try {
                	ps = db2.prepareStatement(sql);
                	
                    //DB読み込みと集計処理
                    for (final String schregno : studentMap._studentMap.keySet()) {
                    	
                        for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {schregno})) {
                            if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                continue;
                            }
                            
                            final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                            if (null == student || null == student._line) {
                                continue;
                            }

                            //学期集計の出力
                            student._termAttendInfoMap.put(term.dateRangeKey(), row);
//                            if (param._isOutputDebug) {
//                            	log.info("   " + student._schregno + " = " + row);
//                            }
                        }
                    }
                } catch (Exception e) {
                	log.error("exception!", e);
                } finally {
                	DbUtils.closeQuietly(ps);
                	db2.commit();
                }
                
                if (!param._c046name1List.isEmpty()) {
                    final String subclassSql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            term._sdate,
                            term._edate,
                            param._attendParamMap
                    );
                    
                    try {
                    	ps = db2.prepareStatement(subclassSql);
                    	
                        //DB読み込みと集計処理
                        for (final String schregno : studentMap._studentMap.keySet()) {
                        	
                            for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {schregno})) {
                                if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                    continue;
                                }
                                
                                final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                                if (null == student || null == student._line) {
                                    continue;
                                }
                                
                                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                                if (param._c046name1List.contains(subclasscd)) {
                                	//学期集計の出力
                                	getMappedMap(student._termAttendSubclassInfoMap, term.dateRangeKey()).put(subclasscd, row);
                                }
                            }
                        }
                    	
                    } catch (Exception e) {
                    	log.error("exception!", e);
                    } finally {
                    	DbUtils.closeQuietly(ps);
                    	db2.commit();
                    }
                }
            	
            } else {
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
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    
                    final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (null == student || null == student._line) {
                        continue;
                    }

                    //学期集計の出力
                    student._termAttendInfoMap.put(term.dateRangeKey(), row);
//                    if (param._isOutputDebug) {
//                    	log.info("   " + student._schregno + " = " + row);
//                    }
                }
                
                if (!param._c046name1List.isEmpty()) {
                    final String subclassSql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            term._sdate,
                            term._edate,
                            param._attendParamMap
                    );
                    

                    //DB読み込みと集計処理
                    for (final Map<String, String> row : KnjDbUtils.query(db2, subclassSql)) {
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        
                        final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student || null == student._line) {
                            continue;
                        }

                        //学期集計の出力
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        if (param._c046name1List.contains(subclasscd)) {
                        	//学期集計の出力
                        	getMappedMap(student._termAttendSubclassInfoMap, term.dateRangeKey()).put(subclasscd, row);
                        }
                    }
                }
            }
        }

        private static void setAttendDiRemark(final DB2UDB db2, final Term term, final Param param, final StudentMap studentMap) {

            final String sql = sqlAttendDiRemark(param, term);
            log.debug("sql = " + sql);
            
            for (final Map<String, String> rs : KnjDbUtils.query(db2, sql)) {
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
            if ("2".equals(param._hrClassType)) {
            	stb.append(    " T2.GHR_ATTENDNO AS ATTENDNO, ");
            } else {
            	stb.append(    " T2.ATTENDNO, ");
            }
            if (param._hasAttendDatDiRemarkCd) {
                stb.append(    " VALUE(C901.NAME1, T1.DI_REMARK) AS DI_REMARK ");
            } else {
                stb.append(    " T1.DI_REMARK ");
            }
            stb.append(" FROM ");
            stb.append(    " ATTEND_DAT T1 ");
            if ("2".equals(param._hrClassType)) {
            	stb.append(    " INNER JOIN SCHREG_REGD_GHR_DAT T2 ");
            } else {
            	stb.append(    " INNER JOIN SCHREG_REGD_DAT T2 ");
            }
            stb.append(        "  ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(        " AND T2.YEAR = '" + param._year + "' ");
            stb.append(        " AND T2.SEMESTER = '" + param._semester + "' ");
            if ("2".equals(param._hrClassType)) {
                stb.append(        " AND T2.GHR_CD = '" + param._ghrCd + "' ");
            } else {
                stb.append(        " AND T2.GRADE = '" + param._grade + "' AND T2.HR_CLASS = '" + param._hrClass + "' ");
            }
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
            stb.append(    " T1.PERIODCD ");

            return stb.toString();
        }
    }
    
    private static class StudentMap {
        final Map<String, Student> _studentMap;
        final TreeMap<Integer, Student> _printLineStudentMap;
        StudentMap(final Map studentMap) {
        	_studentMap = studentMap;
        	
        	final TreeMap printLineStudentMap = new TreeMap();
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
            final List<Student> studentList = new ArrayList();
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
            
            final TreeMap<Integer, Student> printLineStudentMap = new TreeMap();                            //学籍番号と通し行番号の保管
            final Map<String, Student> schregnoStudentMap = new HashMap();                            //通し行番号と生徒名の保管

            //  生徒名等ResultSet作成
            final String sql = Student.sqlStudent(param);
            if (param._isOutputDebug) {
            	log.info(" student sql = " + sql);
            }
            
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
            log.info(" student size = " + schregnoStudentMap.size());
            
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
    	String _currentForm;
    	Map<String, SvfField> _fieldInfoMap;
    	final Set<String> _logOnce = new HashSet<String>();
    	
    	public Form(final Vrw32alp svf, final Param param) {
    		_svf = svf;
    		_param = param;
    	}
    	
    	public int setForm(final String formname, final int k) {
    		_currentForm = formname;
    		try {
    			if ("KNJC043S_1.frm".equals(formname)) {
    				if (!_param._createdFormFiles.containsKey(formname)) {
        				final File formFile = new File(_svf.getPath(formname));
        				final SvfForm svfForm = new SvfForm(formFile);

    					final int days = 6;
    					final int stdCount = 8;

    					final SvfForm.Box box = new SvfForm.Box(0, 3, new SvfForm.Point(224, 314), new SvfForm.Point(6272, 4253));
    					
    					final int periY1 = 688;
    					final int scheX1includesHeader = 834;
    					final int scheX1 = 1055;
    					final int scheX2 = 4873;
    					final int scheY1 = 748;

        				if (svfForm.readFile()) {
        					final SvfForm.Field kouji1 = svfForm.getField("KOUJI1");
        					final SvfForm.Field classname1 = svfForm.getField("classname1");
        					final SvfForm.Field staffname1 = svfForm.getField("staffname1");
        					final SvfForm.Field attend = svfForm.getField("attend1");
        					for (int i = 0, xlines = days * _param._knjc043s_1PeriodCount; i < xlines; i++) {
        						if (i == 0) {
        							continue;
        						}
        						final int x = divnth(scheX1, scheX2, xlines, i);
        						final int y1 = i % _param._knjc043s_1PeriodCount == 0 ? box._upperLeft._y : periY1;
        						final int lineWidth = i % _param._knjc043s_1PeriodCount == 0 ? 3 : 1;
        						svfForm.addLine(new SvfForm.Line(0, lineWidth, new SvfForm.Point(x, y1), new SvfForm.Point(x, box._lowerRight._y), null));
        						
        						svfForm.addField(kouji1.copyTo("KOUJI" + String.valueOf(i + 1)).setX(x + 2));
        						svfForm.addField(classname1.copyTo("classname" + String.valueOf(i + 1)).setX(x + 2));
        						svfForm.addField(staffname1.copyTo("staffname" + String.valueOf(i + 1)).setX(x + 2));
        						svfForm.addField(attend.copyTo("attend" + String.valueOf(i + 1)).setX(x + 2));
        					}
        					
        					for (int i = 0, ylines = stdCount * 3; i < ylines; i++) {
        						if (i == 0) {
        							continue;
        						}
        						final int y = divnth(scheY1, box._lowerRight._y, ylines, i);
        						final int x1 = i % 3 == 0 ? box._upperLeft._x : scheX1includesHeader;
        						final int x2 = i % 3 == 0 ? box._lowerRight._x : scheX2;
        						final int lineWidth = i % 3 == 0 ? 3 : 1;
        						svfForm.addLine(new SvfForm.Line(0, lineWidth, new SvfForm.Point(x1, y), new SvfForm.Point(x2, y), null));
        					}
        					
        					final SvfForm.Field hymd1 = svfForm.getField("hymd1");
        					for (int i = 1; i < days; i++) {
        						final int dayX = divnth(scheX1, scheX2, days, i);
        						final SvfForm.Field hymdN = hymd1.copyTo("hymd" + String.valueOf(i + 1)).setX(dayX + hymd1._position._x - scheX1);
        						svfForm.addField(hymdN);
        					}

        					File tmp = svfForm.writeTempFile();
        					if (tmp.exists()) {
        						_param._createdFormFiles.put(formname, tmp);
        					}
        				}
    				}
    				
    				File created = _param._createdFormFiles.get(formname);
    				if (null != created) {
    					_currentForm = created.getName();
    				}
    			}
    		} catch (Exception e) {
    			log.error("exception!", e);
    		}
    		final int n = _svf.VrSetForm(_currentForm, k);
    		if (_param._isOutputDebug) {
    			log.info(" setForm " + _currentForm);
    		}
    		try {
    			_fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(_svf);
    		} catch (Exception e) {
    			log.error("exception!", e);
    		}
			return n;
    	}

        private static int divnth(final int v1, final int v2, final int div, final int nth) {
        	return v1 + (int) (nth * (v2 - v1) / (double) div);
        }
        
        private boolean hasField(final String fieldname) {
        	return _fieldInfoMap.containsKey(fieldname);
        }
        
        private void logOnce(final String s) {
        	if (_logOnce.contains(s)) {
        		return;
        	}
        	log.info(s);
        	_logOnce.add(s);
        }

    	public int VrsOut(final String field, final String data) {
    		if (_param._isOutputDebug) {
    			if (!hasField(field)) {
    				logOnce(_currentForm +  " no field : " + field);
    			}
    		}
    		final int n = _svf.VrsOut(field, data);
    		if (_param._isOutputDebugVrsOut) {
    			if (hasField(field)) {
    				log.info("svf.VrsOut(\"" + field + "\", " + data + ")");
    			}
    		}
			return n;
    	}
    	
    	public int VrsOutn(final String field, final int gyo, final String data) {
    		if (_param._isOutputDebug) {
    			if (!hasField(field)) {
    				logOnce(_currentForm +  " no field : " + field);
    			}
    		}
    		final int n = _svf.VrsOutn(field, gyo, data);
    		if (_param._isOutputDebugVrsOut) {
    			if (hasField(field)) {
    				log.info("svf.VrsOutn(\"" + field + "\", " + gyo + ", " + data + ")");
    			}
    		}
			return n;
    	}
    	
    	public int VrAttribute(final String field, final String data) {
    		if (_param._isOutputDebug) {
    			if (!hasField(field)) {
    				logOnce(_currentForm +  " no field : " + field);
    			}
    		}
    		final int n = _svf.VrAttribute(field, data);
    		if (_param._isOutputDebugVrsOut) {
    			if (hasField(field)) {
    				log.info("svf.VrAttribute(\"" + field + "\", " + data + ")");
    			}
    		}
			return n;
    	}
    	
    	public int VrAttributen(final String field, final int gyo, final String data) {
    		if (_param._isOutputDebug) {
    			if (!hasField(field)) {
    				logOnce(_currentForm +  " no field : " + field);
    			}
    		}
    		final int n = _svf.VrAttributen(field, gyo, data);
    		if (_param._isOutputDebugVrsOut) {
    			if (hasField(field)) {
    				log.info("svf.VrAttributen(\"" + field + "\", " + gyo + ", " + data + ")");
    			}
    		}
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
    */
    private static class KNJC043_CALC extends Form {
        
        private static final int MAX_LINE = 10;

        private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
        private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";

        private boolean hasdata;
        
        private String _month;
        
        public KNJC043_CALC(final Vrw32alp svf, final Param param) {
        	super(svf, param);
        }
        

        /**
         *   SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2) {

            final StudentMap studentMap = StudentMap.getStudentMap(db2, _param);

            String date1 = null;
            if (null != _param._date2) {
            	Calendar cal = Calendar.getInstance();
            	cal.setTime(java.sql.Date.valueOf(_param._date2));
            	cal.set(Calendar.DAY_OF_MONTH, 1);
            	Date dateDate1 = cal.getTime();
            	_month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            	date1 = new SimpleDateFormat("yyyy-MM-dd").format(dateDate1);
            	//log.info(" date1 = " + date1);
            }

            setAttendance(db2, studentMap, date1);
            
            printAttendance(studentMap);
            
            return hasdata;
        }

        /**
         *
         *  SVF-FORM設定
         */
        private String getSvfForm() {
            String form = "KNJC043S_2.frm";
            return form;
        }

        /** 
         *  出力処理＿見出し出力 
         */
        private void printHeadSvf() {
//            VrsOut("HR_NAME", _param._hrname);        //組名称
//            VrsOut("teacher", _param._staffname); //担任名
//            VrsOut("ymd1", _param._ctrlDateFormatJp); //作成日
//            VrsOut("nendo", _param._nendo); //年度

            VrsOut("TITLE", StringUtils.defaultString(_param._nendo) + "　出席統計　" + StringUtils.defaultString(_param._hrname) +" " + _month + "月");

//            for (int i = 0; i < _param._semesterdiv && i < _param._semesternameList.size(); i++) {
//                VrsOut("SEMESTER" + (i + 1), _param._semesternameList.get(i));
//            }
//            VrsOut("SEMESTER" + (_param._semesterdiv + 1), "1 " + FROM_TO_MARK + " " + _param._semesterdiv + "学期累計");
//            VrsOut("RANGE", _param._calcRange);
            
//            VrsOut("NOTE", "※");
//            VrAttribute("NOTE1",  AMIKAKE_ATTR1);
//            VrsOut("NOTE1", " ");
//            VrsOut("NOTE2", "：欠席超過");
//            VrAttribute("NOTE3",  AMIKAKE_ATTR2);
//            VrsOut("NOTE3", " ");
//            VrsOut("NOTE4", "：欠席注意");
            
//            for (int i = 0; i < 4; i++) {
//                VrsOut("KESSEKI_NAME" + String.valueOf(i + 1) + "_1", _param.getDicdDatNameMap("4"));
//                VrsOut("KESSEKI_NAME" + String.valueOf(i + 1) + "_2", _param.getDicdDatNameMap("5"));
//            }
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
//                if (_param._isOutputDebug) {
//                	log.info(" " + line + " = (" + student._attendno + ") " + student._name );
//                }
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
            if ("2".equals(_param._hrClassType)) {
                stb.append(" FROM SEMESTER_MST");
                stb.append(" WHERE YEAR = '" + _param._year + "' ");
            } else {
                stb.append(" FROM V_SEMESTER_GRADE_MST");
                stb.append(" WHERE YEAR = '" + _param._year + "' AND GRADE = '" + _param._grade + "' ");
            }
            stb.append(" ORDER BY SEMESTER");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String sdate = KnjDbUtils.getString(row, "SDATE");
                if (toCalendar(_param._date2).before(toCalendar(sdate))) { continue; }

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
        private void setAttendance(final DB2UDB db2, final StudentMap studentMap, final String date1) {
        	if (null == _param._date2) {
        		return;
        	}
        	
            final Map semesterDateMap = getSemesterDate(db2);
            if ("2".equals(_param._hrClassType)) {
                _param._attendParamMap.put("schregno", "?");
                final Map attendParamMap = new HashMap(_param._attendParamMap);
                attendParamMap.put("semesFlg", Boolean.FALSE);
                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._year,
                        _param._semester,
                        date1,
                        _param._date2,
                        attendParamMap
                );
                
                if (_param._isOutputDebug) {
                    log.info(" CALC(" + _param._hrClassType + ") sql = " + sql);
                }
                
                PreparedStatement ps = null;
                Set<String> schregnos = studentMap._studentMap.keySet();
				try {
                	ps = db2.prepareStatement(sql);
                    //DB読み込みと集計処理
                	if (_param._isOutputDebug) {
                		log.info(" schregnos = " + schregnos);
                	}
                    for (final String schregno : schregnos) {
                    	
                    	List<Map<String, String>> rowList = KnjDbUtils.query(db2, ps, new Object[] { schregno });
                    	if (_param._isOutputDebug) {
                    		log.info("   row size (" + schregno + ") = " + rowList.size());
                    	}
						for (final Map rs : rowList) {
                    		final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                    		if (!SEMEALL.equals(semester)) {
                    			continue;
                    		}
                    		if (null == getMappedMap(semesterDateMap, semester).get("SDATE") || null == getMappedMap(semesterDateMap, semester).get("EDATE")) {
                    			log.info(" semester date null : " + semester + " / " + getMappedMap(semesterDateMap, semester));
                    			continue;
                    		}
                    		
                    		final Student student = studentMap.getStudent(schregno);
                    		if (null == student) {
                    			log.info(" null student : " + schregno);
                    			continue;
                    		}
                    		
                    		setAttendance(student, rs);
                    	}
                    }
                	
                } catch (Exception e) {
                	log.error("exception!", e);
                } finally {
                	DbUtils.closeQuietly(ps);
                	db2.commit();
                }

                if (!_param._c046name1List.isEmpty()) {
                	final String subclassSql = AttendAccumulate.getAttendSubclassSql(
                			_param._year,
                			_param._semester,
                			date1,
                			_param._date2,
                			attendParamMap
                			);
                	
                	try {
                		ps = db2.prepareStatement(subclassSql);
                		
                		//DB読み込みと集計処理
                		for (final String schregno : schregnos) {
                			
                			for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {schregno})) {
                				final String semester = KnjDbUtils.getString(row, "SEMESTER");
                				if (!SEMEALL.equals(semester)) {
                					continue;
                				}
                				
                        		if (null == getMappedMap(semesterDateMap, semester).get("SDATE") || null == getMappedMap(semesterDateMap, semester).get("EDATE")) {
                        			continue;
                        		}
                        		
                        		final Student student = studentMap.getStudent(schregno);
                        		if (null == student) {
                        			continue;
                        		}
                				
                				final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                				if (_param._c046name1List.contains(subclasscd)) {
                					//学期集計の出力
                					getMappedList(student._lhrShukeiRowList, semester).add(row);
                					if (SEMEALL.equals(semester)) {
                						getMappedList(student._lhrShukeiRowList, "total").add(row);
                					}
                				}
                			}
                		}
                		
                	} catch (Exception e) {
                		log.error("exception!", e);
                	} finally {
                		DbUtils.closeQuietly(ps);
                		db2.commit();
                	}
                }
            } else {
                _param._attendParamMap.put("grade", _param._grade);
                _param._attendParamMap.put("hrClass", _param._hrClass);
                final Map attendParamMap = new HashMap(_param._attendParamMap);
                attendParamMap.put("semesFlg", Boolean.FALSE);
                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._year,
                        _param._semester,
                        date1,
                        _param._date2,
                        attendParamMap
                );
                
                if (_param._isOutputDebug) {
                    log.info(" CALC(" + _param._hrClassType + ") sql = " + sql);
                }
                
                //DB読み込みと集計処理
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    if (!SEMEALL.equals(semester)) {
                        continue;
                    }
                    if (null == getMappedMap(semesterDateMap, semester).get("SDATE") || null == getMappedMap(semesterDateMap, semester).get("EDATE")) {
                        continue;
                    }
                    
                    final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                    final Student student = studentMap.getStudent(schregno);
                    if (null == student) {
                        continue;
                    }
                    
                    setAttendance(student, row);
                }
                
                if (!_param._c046name1List.isEmpty()) {
                	final String subclassSql = AttendAccumulate.getAttendSubclassSql(
                			_param._year,
                			_param._semester,
                			date1,
                			_param._date2,
                			attendParamMap
                			);
                	
                    
                    //DB読み込みと集計処理
                    for (final Map<String, String> row : KnjDbUtils.query(db2, subclassSql)) {
                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
                        if (!SEMEALL.equals(semester)) {
                            continue;
                        }
                        if (null == getMappedMap(semesterDateMap, semester).get("SDATE") || null == getMappedMap(semesterDateMap, semester).get("EDATE")) {
                            continue;
                        }
                        
                        final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                        final Student student = studentMap.getStudent(schregno);
                        if (null == student) {
                            continue;
                        }
                        
        				final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
        				if (_param._c046name1List.contains(subclasscd)) {
        					//学期集計の出力
        					getMappedList(student._lhrShukeiRowList, semester).add(row);
        					if (SEMEALL.equals(semester)) {
        						getMappedList(student._lhrShukeiRowList, "total").add(row);
        					}
        				}
                    }
                }
            }
        }
        
        private void setAttendance(final Student student, final Map rs) {
        	final String semester = KnjDbUtils.getString(rs, "SEMESTER");
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
            	student._studentAttendance = new StudentAttendance(student._schregno);
            }
            
            student._studentAttendance.add(semester, attendance);
            if (attendance._sick != 0) {
                log.debug(" schregno = "+  student._schregno + " (" + student + ") semes = " + semester + " attendance = " + attendance);
            }
            
            //生徒別累計処理
            student._studentAttendance._total.add(attendance);
        }
        
        /** 
         *  学期集計およびＳＶＦ出力 累計へ累積
         *      累計 :     cnt_sum[出欠種別][生徒出力位置]
         */
        private void printAttendance(final StudentMap studentMap) {
        	
        	final Map<String, Attendance> totalAttendanceMap = new HashMap();
        	final Map<String, List<Map<String, String>>> semesterLhrTotalList = new HashMap<String, List<Map<String, String>>>();
            for (final Student student : studentMap._studentMap.values()) {
                if (null == student._studentAttendance) {
                	log.info(" no attendance : " + student._schregno);
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
                    
                    getMappedList(semesterLhrTotalList, key).addAll(getMappedList(student._lhrShukeiRowList, key));
                    
                    //累計処理
                    key = "total";
                    if (!totalAttendanceMap.containsKey(key)) {
                    	totalAttendanceMap.put(key, new Attendance());
                    }
                    final Attendance total = totalAttendanceMap.get(key);
                    total.add(attendance);
                    
                    getMappedList(semesterLhrTotalList, key).addAll(getMappedList(student._lhrShukeiRowList, key));
                }
            }
            if (studentMap._printLineStudentMap.isEmpty()) {
            	return;
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
                    
                    for (final String semester : student._studentAttendance._semesAttendance.keySet()) {
//                        final Attendance a = student._studentAttendance._semesAttendance.get(semester);
//                        //学期集計の出力
//                        printAttendance(pline, a, getMappedList(student._lhrShukeiRowList, semester));
                        
//                        kessekiAmikake(student._schregno, semester, pline, a);
                        
                        //累計の出力
                        printAttendance(pline, student._studentAttendance._total, getMappedList(student._lhrShukeiRowList, semester));
                        
                    } // 生徒の行
                    
                } // ページ
                
                if (maxPage - 1 == pageIdx) {
                    // 累計出力処理
                    final int MAX_LINE = KNJC043_CALC.MAX_LINE + 1;
                    for (final String key : totalAttendanceMap.keySet()) {
                        if (!"total".equals(key)) {
                        	continue;
                        }
                        final Attendance total = totalAttendanceMap.get(key);
                        printAttendance(MAX_LINE, total, getMappedList(semesterLhrTotalList, key));
                        if (total._mlesson > 0) {
                        	final String percentage = new BigDecimal(String.valueOf(total._present)).multiply(new BigDecimal(100)).divide(new BigDecimal(String.valueOf(total._mlesson)), 1, BigDecimal.ROUND_HALF_UP).toString(); // 出席日数合計 / 出席すべき日数計 * 100
                        	VrsOut("PERCENTAGE", percentage);
                        }
                    }
                }
                _svf.VrEndPage();
                hasdata = true;
            }
        }

//        private void kessekiAmikake(final String schregno, final String semester, final int pline, final Attendance a) {
//            if (_param._knjSchoolMst._kessekiWarnBunbo != null && _param._knjSchoolMst._kessekiWarnBunsi != null) {
//                final int bunbo = Integer.parseInt(_param._knjSchoolMst._kessekiWarnBunbo);
//                final int bunsi = Integer.parseInt(_param._knjSchoolMst._kessekiWarnBunsi);
//                final BigDecimal limitBd = new BigDecimal(a._mlesson).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
//                final BigDecimal sickBd = new BigDecimal(a._sick);
//                if (sickBd.compareTo(limitBd) > 0) {
//                    log.debug(schregno + " 注意：出席すべき日数 = " + a._mlesson + " 欠席 = " + a._sick + " 上限 = " + limitBd);
//                    VrAttributen("TOTAL_ABSENCE" + semester,  pline, AMIKAKE_ATTR2);
//                }
//            }
//            if (_param._knjSchoolMst._kessekiOutBunbo != null && _param._knjSchoolMst._kessekiOutBunsi != null) {
//                final int bunbo = Integer.parseInt(_param._knjSchoolMst._kessekiOutBunbo);
//                final int bunsi = Integer.parseInt(_param._knjSchoolMst._kessekiOutBunsi);
//                final BigDecimal limitBd = new BigDecimal(a._mlesson).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
//                final BigDecimal sickBd = new BigDecimal(a._sick);
//                if (sickBd.compareTo(limitBd) > 0) {
//                    log.debug(schregno + " 超過：出席すべき日数 = " + a._mlesson + " 欠席 = " + a._sick + " 上限 = " + limitBd);
//                    VrAttributen("TOTAL_ABSENCE" + semester,  pline, AMIKAKE_ATTR1);
//                }
//            }
//        }
        
        private void printAttendance(final int line, final Attendance attendance, final List<Map<String, String>> subclassRowList) {
        	VrsOutn("LESSON",  line, String.valueOf(attendance._lesson)); // 授業日数
        	VrsOutn("SUSPEND", line, String.valueOf(attendance._suspend + attendance._virus + attendance._koudome)); //出停
            VrsOutn("KIBIKI",  line, String.valueOf(attendance._mourning));        //忌引
            VrsOutn("MLESSON", line, String.valueOf(attendance._mlesson));    //出席すべき日数
            VrsOutn("ABSENCE", line, String.valueOf(attendance._sick));        //欠席
            VrsOutn("ATTEND",  line, String.valueOf(attendance._present));    //出席日数
            VrsOutn("LATE"  ,  line, String.valueOf(attendance._late));        //遅刻
            VrsOutn("EARLY" ,  line, String.valueOf(attendance._early));        //早退
            VrsOutn("LHR_KEKKA", line, Attendance.getSubclassRowSick2Sum(subclassRowList)); //授業欠課
        }
    }


    /**
     *  学校教育システム 賢者 [出欠管理]  出席簿（公簿）
     */
    private static class KNJC043_LIST extends Form {

        private boolean hasdata;

        public KNJC043_LIST(final Vrw32alp svf, final Param param) {
        	super(svf, param);
        }
        
        /**
         *   svf print 印刷処理
         */
        public boolean printSvf(final DB2UDB db2) {
            
            //対象校時および名称取得
            _param._periodList = getPeiodList(db2);

            final String form = "KNJC043S_1.frm";
            final int svfLine = 8;
            setForm(form, 1);
            
            //印刷範囲の再設定
            final Term term = Term.getTermValue(db2, _param);
            
            final List<Week> weekList = Week.getWeekList(db2, _param, term);
            
            //  生徒の読み込み
            final StudentMap studentMap = StudentMap.getStudentMap(db2, _param);

            for (final Week week : weekList) {

//                if (_param._isPrintOnedayAttendInfo) {
//                    // 1日出欠の取得と処理
//                    for (final String date : week._dateList) {
//                        log.info(" check oneday info : " + date);
//                        Student.setAttendOnedayInfo(db2, date, _param, studentMap);
//                    }
//                }
                Student.setSumAttend(db2, week._term, _param, studentMap);
                Student.setAttendDiRemark(db2, week._term, _param, studentMap);
            }
            
            final Map<String, Set<String>> weekDatePeriodSetMap = getDatePeriodKeySetMap(weekList, db2, _param, studentMap);

            final List pageList = getPageList(studentMap, svfLine);
            
            final int t_page = weekList.size() * pageList.size();          //総ページ数

            for (int pageIdx = 0; pageIdx < pageList.size(); pageIdx++) {
//                final Map pageMap = (Map) pageList.get(pageIdx);
//                log.info(" pageMap = " + pageMap);
                
                for (int wi = 0; wi < weekList.size(); wi++) {
                    final Week week = weekList.get(wi);
                    
                    log.info("form = " + form + " ( week idx = " + wi + ", page idx = " + pageIdx + ")");
                    
                    final int startLine = pageIdx * svfLine + 1;

                    printHead(db2, term, week, t_page); //見出し等を出力するメソッド
                    
                    final List<Student> studentList = studentMap.getStudentList(startLine, svfLine);
                    
                    printStudentsname(week._term, studentList); //生徒名を出力するメソッド
                    if (_param._output4 == null) {
                        for (int line = 0; line < svfLine; line++) {
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
                    
//                    if (_param._isPrintOnedayAttendInfo) {
//                        for (final String date : week._dateList) {
//                            printAttendOnedayData(week, date, studentList);
//                        }
//                    }

//                    if (_param._outputDiremark != null) {
//                        final String totalRemark = getAttendDiRemark(week, studentList);
//
//                        final String[] token = KNJ_EditEdit.get_token(totalRemark, 220, 10);
//                        if (token != null) {
//                            for (int i = 0; i < token.length; i++) {
//                                VrsOutn("REMARK", i + 1, token[i]);
//                            }
//                        }
//                        log.debug("total_remark = " + totalRemark.length());
//                    }
                    _svf.VrEndPage();
                    hasdata = true;
                }                 //出欠データを出力するメソッド
            }
            
            return hasdata;
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
            if ("2".equals(param._hrClassType)) {
                stb.append(    "FROM    SCHREG_REGD_GHR_DAT W2 ");
                stb.append(    "WHERE   W2.YEAR = '" + param._year + "' AND ");
                stb.append(            "W2.GHR_CD = '" + param._ghrCd + "' ");
            } else {
                stb.append(    "FROM    SCHREG_REGD_DAT W2 ");
                stb.append(    "WHERE   W2.YEAR = '" + param._year + "' AND ");
                stb.append(            "W2.GRADE = '" + param._grade + "' AND W2.HR_CLASS = '" + param._hrClass + "' ");
            }
            stb.append(    "GROUP BY SCHREGNO");
            stb.append(    ")");
            
            stb.append(",COUNTFLG AS(");
            stb.append(    "SELECT  EXECUTEDATE, PERIODCD, CHAIRCD, COUNTFLG ");
            stb.append(    "FROM    SCH_CHR_COUNTFLG T1 ");
            stb.append(    "INNER JOIN DATE_RANGE DR ON T1.EXECUTEDATE BETWEEN DR.SDATE AND DR.EDATE ");
            stb.append(    "WHERE ");
            if ("2".equals(param._hrClassType)) {
            	stb.append(            "(GRADE, HR_CLASS) IN (SELECT GRADE, HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + param._year + "' AND SCHREGNO IN (SELECT SCHREGNO FROM SCHNO)) ");
            } else {
            	stb.append(            "GRADE = '" + param._grade + "' AND HR_CLASS = '" + param._hrClass + "' ");
            }
            stb.append(            " AND COUNTFLG = '0' ");
            stb.append(    ")");
            
//            stb.append(" ,TEST_COUNTFLG AS ( ");
//            stb.append("     SELECT ");
//            stb.append("         T1.EXECUTEDATE, ");
//            stb.append("         T1.PERIODCD, ");
//            stb.append("         T1.CHAIRCD, ");
//            stb.append("         '2' AS DATADIV, ");
//            stb.append("         T2.COUNTFLG ");
//            stb.append("     FROM ");
//            stb.append("         SCH_CHR_TEST T1, ");
//            if ("TESTITEM_MST_COUNTFLG".equals(param._useTestCountflg)) {
//                stb.append("         TESTITEM_MST_COUNTFLG T2 ");
//                stb.append("     WHERE ");
//                stb.append("         T2.YEAR       = T1.YEAR ");
//            } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
//                stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
//                stb.append("     WHERE ");
//                stb.append("         T2.YEAR       = T1.YEAR ");
//                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
//                stb.append("         AND T2.SCORE_DIV  = '01' ");
//            } else {
//                stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
//                stb.append("     WHERE ");
//                stb.append("         T2.YEAR       = T1.YEAR ");
//                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
//            }
//            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
//            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
//            stb.append(") ");
            
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
            
            stb.append(",CHAIRSTF AS(");
            stb.append("    SELECT STF.SEMESTER, STF.CHAIRCD, STF.STAFFCD, MST.STAFFNAME, ROW_NUMBER() OVER(PARTITION BY T2.SEMESTER, T2.CHAIRCD ORDER BY CASE WHEN CHARGEDIV = '1' THEN 0 ELSE 2 END, STF.STAFFCD) AS ROWNO ");
            stb.append("    FROM CHAIR_DAT T2 ");
            stb.append("    INNER JOIN CHAIR_STF_DAT STF ON ");
            stb.append("          STF.YEAR = T2.YEAR ");
            stb.append("      AND T2.SEMESTER = STF.SEMESTER");
            stb.append("      AND T2.CHAIRCD = STF.CHAIRCD");
            stb.append("    INNER JOIN STAFF_MST MST ON MST.STAFFCD = STF.STAFFCD ");
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
            
            stb.append(" SELECT T1.SCHREGNO ");
            stb.append("       ,T1.EXECUTEDATE ");
            stb.append("       ,T1.PERIODCD ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,STF.STAFFCD ");
            stb.append("       ,STF.STAFFNAME ");
            stb.append("       ,VALUE(T_CHAIR.CHAIRABBV, T_CHAIR.CHAIRNAME) AS CHAIRNAME ");
            stb.append("       ,T_CHAIR.CLASSCD ");
            stb.append("       ,VALUE(CLM.CLASSABBV, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("       ,T_CHAIR.CLASSCD || '-' || T_CHAIR.SCHOOL_KIND || '-' || T_CHAIR.CURRICULUM_CD || '-' || T_CHAIR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("       ,VALUE(SUBM.SUBCLASSABBV, SUBM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("       ,T1.EXECUTED ");
            stb.append("       ,T1.HR_EXECUTED ");
            stb.append("       ,T2.DI_CD");
            stb.append("       ,ATDD.REP_DI_CD");
            stb.append("       ,DAYOFWEEK_ISO(T1.EXECUTEDATE) AS DAYOFWEEK");
            stb.append("       ,ATDD.DI_MARK AS DI_NAME");
            stb.append("       ,CASE "); // WHEN T1.DATADIV = '2' THEN VALUE(T6.COUNTFLG, '1') ");
            stb.append("             WHEN T1.DATADIV IN ('0', '1') THEN VALUE(T3.COUNTFLG,'1') ");
            stb.append("             ELSE '1' END AS COUNTFLG");
            stb.append("       ,CASE WHEN T4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE");
            stb.append(" FROM SCHEDULE T1");
            stb.append(" LEFT JOIN ATTEND T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ATTENDDATE = T1.EXECUTEDATE AND T2.PERIODCD = T1.PERIODCD");
            stb.append(" LEFT JOIN COUNTFLG T3 ON T3.EXECUTEDATE = T1.EXECUTEDATE AND T3.PERIODCD = T1.PERIODCD AND T3.CHAIRCD = T1.CHAIRCD");
            stb.append(" LEFT JOIN SCHLEAVE T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.EXECUTEDATE = T1.EXECUTEDATE");
            stb.append(" LEFT JOIN CHAIR_DAT T_CHAIR ON T1.YEAR = T_CHAIR.YEAR AND T1.SEMESTER = T_CHAIR.SEMESTER AND T1.CHAIRCD = T_CHAIR.CHAIRCD");
//            stb.append(" LEFT JOIN TEST_COUNTFLG T6 ON T6.EXECUTEDATE = T1.EXECUTEDATE AND T6.PERIODCD = T1.PERIODCD AND T6.CHAIRCD = T1.CHAIRCD AND T6.DATADIV = T1.DATADIV ");
            stb.append(" LEFT JOIN CHAIRSTF STF ON STF.SEMESTER = T_CHAIR.SEMESTER AND STF.CHAIRCD = T_CHAIR.CHAIRCD AND STF.ROWNO = 1 ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON T_CHAIR.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("  AND T_CHAIR.CLASSCD = SUBM.CLASSCD ");
            stb.append("  AND T_CHAIR.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("  AND T_CHAIR.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append(" LEFT JOIN CLASS_MST CLM ON T_CHAIR.CLASSCD = CLM.CLASSCD ");
            stb.append("  AND T_CHAIR.SCHOOL_KIND = CLM.SCHOOL_KIND ");
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
                if ("9".equals(semester)) {
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
        private List<Period> getPeiodList(final DB2UDB db2) {
            //  校時名称
            final List<Period> periodlist = new ArrayList();
            
            final StringBuffer stb = new StringBuffer();
            stb.append(    "SELECT  W1.NAMECD2, W1.NAME1 ");
            if (_param._definecode.usefromtoperiod) {
                stb.append(       ",CASE WHEN NAMECD2 BETWEEN S_PERIODCD AND E_PERIODCD THEN 1 ELSE 0 END AS ONPERIOD ");
            } else {
                stb.append(       ",1 AS ONPERIOD ");
            }
            if (_param._definecode.usefromtoperiod) {
                stb.append("FROM    NAME_MST W1, COURSE_MST W2 ");
                stb.append("WHERE   NAMECD1 = 'B001' ");
                stb.append(    "AND COURSECD IN (SELECT MIN(COURSECD) FROM SCHREG_REGD_DAT W3 ");
                stb.append(                    "WHERE  W3.YEAR = '" + _param._year + "' ");
                stb.append(                       "AND W3.SEMESTER = '" + _param._semester + "' ");
                if ("2".equals(_param._hrClassType)) {
                	stb.append(                       "AND W3.SCHREGNO IN  (SELECT SCHREGNO FROM SCHREG_REGD_GHR_DAT WHERE YEAR = '" + _param._year + "' AND SEMESTER = '" + _param._semester + "' AND GHR_CD = '" + _param._ghrCd + "') ");
                } else {
                	stb.append(                       "AND W3.GRADE = '" + _param._grade + "' AND W3.HR_CLASS = '" + _param._hrClass + "' ");
                }
                stb.append(                       ") ");
            } else {
                stb.append("FROM    NAME_MST W1 ");
                stb.append("WHERE   NAMECD1 = 'B001' ");
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM NAME_YDAT W2 ");
            stb.append(                   "WHERE  W2.YEAR = '" + _param._year + "' ");
            stb.append(                      "AND W2.NAMECD1 = 'B001' AND W2.NAMECD2 = W1.NAMECD2) ");
            stb.append("ORDER BY W1.NAMECD2");
            log.debug("period sql = " + stb.toString());

            final List rowList = KnjDbUtils.query(db2, stb.toString());

            for (int i = 0; i < rowList.size() && periodlist.size() < 16; i++) {
                final Map rs = (Map) rowList.get(i);
                if ("2".equals(_param._radio1) ||  getInt(rs, "ONPERIOD", 0) == 1) {
                    periodlist.add(new Period(KnjDbUtils.getString(rs, "NAMECD2"), KnjDbUtils.getString(rs, "NAME1")));
                }
            }
            log.info(" periodlist size = " + periodlist.size());
//            _param._knjc043s_1PeriodCount = (periodlist.size() <= 9) ? 9 : 16;
            _param._knjc043s_1PeriodCount = 10;

            return periodlist;
        }

        /** 
         *  出力処理＿見出し出力 
         */
        private void printHead(final DB2UDB db2, final Term term, final Week week, final int t_page) {
            
            VrsOut("HR_NAME", _param._hrname);        //組名称
            VrsOut("teacher", _param._staffname);        //担任名
            VrsOut("ymd1", _param._ctrlDateFormatJp);        //作成日
            VrsOut("nendo", _param._nendo);        //年度
            VrsOut("PRINT", term.getDateFromTo(db2));        //印刷範囲
            _svf.VrlOut("TOTAL_PAGE", t_page);           //総ページ数
            
            //VrsOut("PERIOD", (_param._onlySunday != null) ? "日の集計": (_param.periodnum == 9) ? "月" + FROM_TO_MARK  + "日の集計": "月" + FROM_TO_MARK + "土の集計");
            VrsOut("PERIOD", "月" + FROM_TO_MARK + "土の集計");
            VrAttribute("MARK1", "Paint=(2,70,1),Bold=1");
            VrsOut("MARK1", "  ");
            VrsOut("MARK2", "＝");
            
//            VrsOut("KESSEKI_NAME1", _param.getDicdDatNameMap("4"));
//            VrsOut("KESSEKI_NAME2", _param.getDicdDatNameMap("5"));
            
            for (final String datestr : week._dateList) {
                try {
                	final int dow = toCalendar(datestr).get(Calendar.DAY_OF_WEEK);
					final int col = Calendar.SUNDAY == dow ? 7 : dow - 1;
                	VrsOut("hymd" + String.valueOf(col), KNJ_EditDate.h_format_JP_MD(datestr) + "(" + KNJ_EditDate.h_format_W(datestr) + ")"); //ＳＶＦ出力(日付)
                } catch (Exception e) {
                	log.error("exception!", e);
                }
            }
            for (final int widx : week._periodStartList) {
                for (int i = 0; i < _param._periodList.size(); i++) {
                    final Period period = _param._periodList.get(i);
                    VrsOut("KOUJI" + String.valueOf(widx * _param._knjc043s_1PeriodCount + i + 1), period._name);
                }
            }
//            
//            //校時及び科目を出力するメソッド
//            for (final Integer col : week._subjectNameMap.keySet()) {
//                VrsOutn("subject", col.intValue(), week._subjectNameMap.get(col));
//            }
        }
        
        private List<Map<String, String>> getPageList(final StudentMap studentMap, final int _svfLine) {
            final List<Map<String, String>> list = new ArrayList();

            int oldpage = 0;

            String sattendno = null;
            String eattendno = null;
            List<String> schregnoList = new ArrayList();

            for (final Iterator mpi = studentMap._printLineStudentMap.keySet().iterator(); mpi.hasNext();) {
                final Integer line = (Integer) mpi.next();               //行番号を取り出す
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
        
//        private String getAttendDiRemark(final Week week, final List<Student> studentList) {
//            
//            final Set<String> dateSet = new TreeSet();
//            
//            for (final Student student : studentList) {
//                
//                if (null == student) {
//                    continue;
//                }
//                dateSet.addAll(getMappedMap(student._attendDiRemarkMap, week._term.dateRangeKey()).keySet());
//            }
//            
//            final List<String> remarkList = new ArrayList();
//            for (final String date : dateSet) {
//                
//                final StringBuffer stb = new StringBuffer();
//
//                for (final Student student : studentList) {
//                    
//                    if (null == student) {
//                        continue;
//                    }
//                    
//                    final TreeMap<String, String> periodAttendRemarkMap = getMappedMap(getMappedMap(student._attendDiRemarkMap, week._term.dateRangeKey()), date);
//                    if (periodAttendRemarkMap.isEmpty()) {
//                        continue;
//                    }
//                    
//                    if (stb.length() > 0) {
//                        stb.append("／");
//                    }
//                    
//                    stb.append(KNJ_EditDate.h_format_JP_MD(date) + "," + student._name);
//                    
//                    for (final String period : periodAttendRemarkMap.keySet()) {
//                        final String remark = periodAttendRemarkMap.get(period);
//                        
//                        stb.append(",").append(remark);
//                    }
//                }
//                if (stb.length() > 0) {
//                    remarkList.add(stb.toString());
//                }
//            }
//            
//            final String totalRemark = mkString(remarkList, "\n");
//            return totalRemark;
//        }
        
        
//        private void printAttendOnedayData(final Week week, final String date, final List<Student> studentList) {
//            for (final Student student : studentList) {
//
//                String attendField = null;
//                final Integer dayofweek = week._dateDayofweekMap.get(date);
//                if (null != dayofweek) {
//                    int col;
//                    if (_param.periodnum <= 9) {
//                        col = 9;
//                    } else {
//                        col = 16;
//                    }
//                    attendField = "attend" + String.valueOf(getPositionOfLine(_param, dayofweek.intValue(), col));
//                }
//                if (null == attendField) {
//                    continue;
//                }
//                    
//                final String comment = student._onedayAttendComment.get(date);
//                if (null != comment) {
//                    VrsOutn(attendField, student._printline, comment);
//                }
//            }
//        }

        private void printAttendData(final Week week, final List<Student> studentList, final Set<String> datePeriodSet) {
            for (final Student student : studentList) {
                final int line = student._printline;
                
//                for (final String date : student._onedayAttendAttributeTarget.keySet()) {
//                    if (!week._dateList.contains(date)) {
//                        continue;
//                    }
//
//                    final Integer dayofweek = getDB2DayOfWeek(date);
//                    if (null == dayofweek) {
//                        continue;
//                    }
//
//                    for (final Period period : _param._periodList) {
//                        final Integer intperiod = _param.getPeriodPosition(period._cd);
//                        
//                        final String attendField = String.valueOf(getPositionOfLine(_param, dayofweek.intValue(), intperiod.intValue()));
//                        //log.info(" attendField = " + attendField + " / " + date + " / " + dayofweek);
//                        VrAttributen("attend" + attendField, line, "Paint=(1,80,1),Hensyu=3");
//                    }
//                }

                for (final String datePeriodKey : datePeriodSet) {

                    final Attend dataMap = student._datePeriodKintaiMap.get(datePeriodKey);
					if (null == dataMap) {
                        continue;
                    }
                    final String attendField = "attend" + dataMap._attendFieldColumn;
                    if ("1".equals(dataMap._kara)) {
                        VrAttributen(attendField, line, "Size=6.0" );
                        VrsOutn(attendField, line, "空");
                    } else {
                    	final Attend row = dataMap;
//                        final String chairField = "CHAIR" + row.get("attendField");
                    	VrsOutn("classname" + dataMap._attendFieldColumn, line, row._classname);
                    	VrsOutn("staffname" + dataMap._attendFieldColumn, line, row._staffname);
                        
                        boolean amikake = false;
                        // 時間割講座データ集計フラグを使用する場合の網掛け設定
                        if (_param._definecode.useschchrcountflg) {
                            amikake = (Integer.parseInt(row._countflg) == 0) ? true : false;
                            if (amikake) {
                                final String ATTR_AMIKAKE = "Paint=(2,70,2),Bold=1,Hensyu=3";
                                VrAttributen(attendField, line, ATTR_AMIKAKE);
//                                if (_param._isIbaraki) {
//                                    VrAttributen(chairField, line, ATTR_AMIKAKE);
//                                }
                            }
                        }

//                        boolean shukketsuZumi = false;
                        // 出欠入力済みで出席は「ブランク」、未入力は「未」を印刷
                        if (row._executed != null || row._hrExecuted != null) {
                            if (isPrintMinyuuryoku(_param, row._executed, row._hrExecuted, row._executedate)) {
                                VrAttributen(attendField, line, "Size=6.0");
                                VrsOutn(attendField, line, "未");
                            } else {
//                                VrAttributen(attendField, line, "Size=9.0");
                                VrsOutn(attendField, line, "  ");

//                                if (_param._isIbaraki) {
//                                    final String subject;
//                                    if (_param._definecode.usechairname) {
//                                        subject = KnjDbUtils.getString(row, "CHAIRNAME");
//                                    } else {
//                                        subject = KnjDbUtils.getString(row, "SUBCLASSNAME");
//                                    }
//                                    VrsOutn(chairField, line, subject);
//                                }
//                                shukketsuZumi = true;
                            }
                        }
                        
                        // 出欠記号
                        if (row._diName != null) {
                            final String attr;
//                            if (_param._isIbaraki) {
//                                attr = "Size=7.0";
//                            } else {
                                attr = "Size=9.0";
//                            }
                            //if (!_param._isIbaraki || _param._isIbaraki && shukketsuZumi) {
                                VrAttributen(attendField, line, attr);
                                VrsOutn(attendField, line, row._diName);
                                // 取り消し線
                                if (NumberUtils.isDigits(row._leave) && Integer.parseInt(row._leave) == 1) {
                                    final Integer intperiod = _param.getPeriodPosition(row._periodcd);
                                    final int dayofweek = NumberUtils.isDigits(row._dayofweek) ? Integer.parseInt(row._dayofweek) : -1;
                                    
                                    VrsOutn("CLEAR_attend" + getPositionOfLine(_param, dayofweek, intperiod.intValue()), line, "＝");
                                }
                            //}
                        }
                        // 網掛け設定解除
                        if (_param._definecode.useschchrcountflg) {
                            if (amikake) {
                                final String ATTR_CLEAR = "Paint=(0,0,0),Bold=0,Hensyu=3";
                                VrAttributen(attendField, line, ATTR_CLEAR);
//                                if (_param._isIbaraki) {
//                                    VrAttributen(chairField, line, ATTR_CLEAR);
//                                }
                            }
                        }
                    }
                }
            }
        }

        private Integer getDB2DayOfWeek(final String date) {
            if (null == date) {
                return null;
            }
            
            final int dayOfWeekJava = toCalendar(date).get(Calendar.DAY_OF_WEEK);
            if (dayOfWeekJava == Calendar.SUNDAY) {
                return new Integer(7);
            }
            return new Integer(dayOfWeekJava - 1);
        }

        private Map getDatePeriodKeySetMap(final List<Week> weekList, final DB2UDB db2, final Param param, final StudentMap studentMap) {
            
            PreparedStatement ps = null;

            final Map<String, Set<String>> datePeriodKeySetMap = new HashMap();
            try {
                final String sql = sqlAttendData(param);
                if (param._isOutputDebug) {
                	log.info(" sqlAttendData = " + sql);
                }
                ps = db2.prepareStatement(sql);        //出欠データ
                
                for (final Week week : weekList) {

                    final Set<String> datePeriodKeySet = new HashSet();

                    final Object[] parameter = new Object[] {
                            java.sql.Date.valueOf(week._term._sdate),      //集計開始日（週始め）
                            java.sql.Date.valueOf(week._term._edate),      //集計終了日（週終わり）
                    };
                    
                    for (final Map row : KnjDbUtils.query(db2, ps, parameter)) {
                        
                        final String date = KnjDbUtils.getString(row, "EXECUTEDATE");
                        final String datePeriodKey = date + "" + KnjDbUtils.getString(row, "PERIODCD");
                        final Integer intperiod = param.getPeriodPosition(KnjDbUtils.getString(row, "PERIODCD"));
                        if (intperiod == null) {
                            continue;
                        }
                        final int dayofweek = getInt(row, "DAYOFWEEK", 0);
                        final String col = String.valueOf(getPositionOfLine(param, dayofweek, intperiod.intValue()));
                        
                        if ("1".equals(param._shrSyurei)) {
                        	final String shureiClasscd = "92";
                            final boolean isShrSyurei;
//                            if ("1".equals(param._useCurriculumcd)) {
                                isShrSyurei = shureiClasscd.equals(KnjDbUtils.getString(row, "CLASSCD"));
//                            } else {
//                                isShrSyurei = KnjDbUtils.getString(row, "SUBCLASSCD") != null && KnjDbUtils.getString(row, "SUBCLASSCD").startsWith(shureiClasscd);
//                            }
                            if (isShrSyurei) {
                                continue;
                            }
                        }
                        
                        if (!datePeriodKeySet.contains(datePeriodKey)) {
                            // 未履修「空」の印刷
                            for (final Integer printLine : studentMap._printLineStudentMap.keySet()) {
                                final Student student = studentMap.getStudentOfPrintLine(printLine);
                                if (null == student || !NumberUtils.isDigits(student._attendno)) {
                                    continue;
                                }
                                final Attend attend = new Attend();
                                attend._kara = "1";
                                attend._attendFieldColumn = col;
                                student._datePeriodKintaiMap.put(datePeriodKey, attend);
                            }
                            datePeriodKeySet.add(datePeriodKey);
                        }
                        final Student student = studentMap.getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student || null == student._line) {
                            continue;
                        }
                        
                        final Attend attend = new Attend();
                        attend._attendFieldColumn = col;
                        attend._executedate = KnjDbUtils.getString(row, "EXECUTEDATE");
                        attend._periodcd = KnjDbUtils.getString(row, "PERIODCD");
                        attend._chairname = KnjDbUtils.getString(row, "CHAIRNAME");
                        attend._classcd = KnjDbUtils.getString(row, "CLASSCD");
                        attend._classname = KnjDbUtils.getString(row, "CLASSNAME");
                        attend._staffname = KnjDbUtils.getString(row, "STAFFNAME");
                        attend._subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        attend._subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                        attend._executed = KnjDbUtils.getString(row, "EXECUTED");
                        attend._hrExecuted = KnjDbUtils.getString(row, "HR_EXECUTED");
                        attend._diCd = KnjDbUtils.getString(row, "DI_CD");
                        attend._repDiCd = KnjDbUtils.getString(row, "REP_DI_CD");
                        attend._dayofweek = KnjDbUtils.getString(row, "DAYOFWEEK");
                        attend._diName = KnjDbUtils.getString(row, "DI_NAME");
                        attend._countflg = KnjDbUtils.getString(row, "COUNTFLG");
                        attend._leave = KnjDbUtils.getString(row, "LEAVE");
                        student._datePeriodKintaiMap.put(datePeriodKey, attend);
                        
//                        if (param._isPrintOnedayAttendInfo) {
//                            getMappedMap(student._datePeriodKintaiMapMap, date).put(intperiod, KnjDbUtils.getString(row, "DI_CD"));
//                            getMappedMap(student._datePeriodRepDiCdMapMap, date).put(intperiod, KnjDbUtils.getString(row, "REP_DI_CD"));
//                        }
                    }
                    
                    datePeriodKeySetMap.put(week._term.dateRangeKey(), datePeriodKeySet);
                }
                
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            
//            if (param._isPrintOnedayAttendInfo && "1".equals(param._isOnedayAttendCheck)) {
//                for (final Integer printLine : studentMap._printLineStudentMap.keySet()) {
//                    final Student student = studentMap.getStudentOfPrintLine(printLine);
//                    if (null == student || !NumberUtils.isDigits(student._attendno)) {
//                        continue;
//                    }
//                    for (final Map.Entry<String, TreeMap<Integer, String>> e : student._datePeriodKintaiMapMap.entrySet()) {
//                        final String date = e.getKey();
//                        final TreeMap<Integer, String> periodKintaiTreeMap = e.getValue();
//
//                        boolean setBefore = false;
//                        boolean shussekiBefore = false;
//                        int flipped = 0;
//                        for (final Map.Entry<Integer, String> periodKintai : periodKintaiTreeMap.entrySet()) {
//                            final String diCd = periodKintai.getValue();
//                            boolean shusseki = false;
//                            if (setBefore) {
//                                final String repDiCd = getMappedMap(student._datePeriodKintaiMapMap, date).get(periodKintai.getKey());
//                                final String hanteiDiCd = StringUtils.defaultString(repDiCd, diCd);
//                                shusseki = isGakkounaiShusseki(hanteiDiCd);
//                                if (shussekiBefore != shusseki) {
//                                    flipped += 1;
//                                }
//                            }
//                            setBefore = true;
//                            shussekiBefore = shusseki;
//                        }
//                        if (flipped > 1) {
//                            student._onedayAttendAttributeTarget.put(date, new Integer(flipped));
//                        }
//                    }
//                }
//            }
            
            return datePeriodKeySetMap;
        }
        
//        // 校内にいる勤怠はtrue
//        private boolean isGakkounaiShusseki(final String diCd) {
//            if (!NumberUtils.isDigits(diCd)) {
//                if (null != diCd) {
//                    log.warn("unknown DI_CD:" + diCd);
//                }
//                return true;
//            }
//            final int diCdInt = Integer.parseInt(diCd);
//            boolean shusseki = true;
//            switch (diCdInt) {
//            case 0: break; // 出席
//            case 1: shusseki = false; break; // 公欠
//            case 2: shusseki = false; break; // 出停
//            case 3: shusseki = false; break; // 忌引
//            case 4: shusseki = false; break; // 病欠
//            case 5: shusseki = false; break; // 届出有
//            case 6: shusseki = false; break; // 届出無
//            case 8: shusseki = false; break; // 公欠
//            case 9: shusseki = false; break; // 出停
//            case 10: shusseki = false; break; // 忌引
//            case 11: shusseki = false; break; // 病欠
//            case 12: shusseki = false; break; // 届出有
//            case 13: shusseki = false; break; // 届出無
//            case 14: break; // 保健室結果
//            case 15: break; // 遅刻
//            case 16: break; // 早退
//            case 19: shusseki = false; break; // 出停伝染病
//            case 20: shusseki = false; break; // 出停伝染病
//            case 25: shusseki = false; break; // 出停交止
//            case 26: shusseki = false; break; // 出停交止
//            case 29: break; // 欠課遅刻
//            case 30: break; // 欠課早退
//            case 31: break; // 欠課遅刻早退
//            case 32: break; // 出席扱いの遅刻早退
//            default:
//                shusseki = true;
//            }
//            return shusseki;
//        }

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
        private static int getPositionOfLine(final Param param, final int dayofweek, final int period) {
            int line = 0;
//            if (param._onlySunday != null) {
//                line = period;
//            } else {
                line = (dayofweek - 1) * param._knjc043s_1PeriodCount + period;
//            }
            return line;
        }

        /** 
         *  学期集計およびＳＶＦ出力 累計へ累積
         */
        private void printSumAttend(final Term term, final List<Student> studentList) {
            
            for (final Student student : studentList) {

                final Map<String, String> row = student._termAttendInfoMap.get(term.dateRangeKey());
                
                final int line = student._printline;
                //学期集計の出力
                VrsOutn("LESSON",  line, String.valueOf(getInt(row, "LESSON", 0)));   // 授業日数
                VrsOutn("SUSPEND",  line, String.valueOf(getInt(row, "SUSPEND", 0) + getInt(row, "VIRUS", 0) + getInt(row, "KOUDOME", 0)));      // 出停
                VrsOutn("MOURNING",   line, KnjDbUtils.getString(row, "MOURNING"));   // 忌引
                VrsOutn("ABSENCE",  line, KnjDbUtils.getString(row, "SICK"));         // 欠席日数
                VrsOutn("ATTEND",   line, KnjDbUtils.getString(row, "PRESENT"));      // 出席日数
                VrsOutn("LATE",     line, KnjDbUtils.getString(row, "LATE"));         // 遅刻
                VrsOutn("EARLY",    line, KnjDbUtils.getString(row, "EARLY"));        // 早退
                VrsOutn("KEKKA",    line, Attendance.getSubclassRowSick2Sum(getMappedMap(student._termAttendSubclassInfoMap, term.dateRangeKey()).values()));   // 授業欠課
            }
        }
        
        /** 
         *  出力処理＿生徒名等出力 
         *   引数に Map hm3 (備考) を追加
         **/
        private void printStudentsname(final Term term, final List<Student> studentList) {
            for (final Student student : studentList) {
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
            stb.append(" SELECT  W1.EXECUTEDATE, W1.PERIODCD, ");
//            stb.append("         W1.CHAIRCD, ");
//            stb.append("         W2.CLASSCD, ");
//            stb.append("         W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD AS SUBCLASSCD, ");
//            stb.append("         W2.GROUPCD, ");
            stb.append("         DAYOFWEEK_ISO(W1.EXECUTEDATE) AS DAYOFWEEK, ");
            stb.append("         NMB001.NAME1 AS PERIODNAME ");
//            stb.append("         Meisyou_Get(W1.PERIODCD,'B001',1) AS PERIODNAME, ");
//            if (schooldiv == 0) {
//                stb.append("         W5.SUBCLASSABBV AS SUBCLASSNAME, ");
//            } else {
//                stb.append("         W5.SUBCLASSNAME, ");
//            }
//            stb.append("         W6.CLASSNAME, ");
//            stb.append("         W7.GROUPNAME  ");
//            stb.append("         ,W2.CHAIRNAME ");
            stb.append("         ,W1.ATTENDNO ");
//            stb.append("         , W1.TESTITEMNAME ");
            stb.append(" FROM    (SELECT K3.EXECUTEDATE ");
            stb.append("               , K3.PERIODCD ");
//            stb.append("               , K3.CHAIRCD ");
            stb.append("               , K3.YEAR ");
            stb.append("               , K3.SEMESTER  ");
            if ("2".equals(param._hrClassType)) {
            	stb.append("               , MIN(K0.GHR_ATTENDNO) AS ATTENDNO ");
            } else {
            	stb.append("               , MIN(K0.ATTENDNO) AS ATTENDNO ");
            }
//            stb.append("               , CAST(NULL AS VARCHAR(1)) AS TESTITEMNAME ");
            if ("2".equals(param._hrClassType)) {
                stb.append("          FROM   SCHREG_REGD_GHR_DAT K0 ");
            } else {
                stb.append("          FROM   SCHREG_REGD_DAT K0 ");
            }
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
            if ("2".equals(param._hrClassType)) {
            	stb.append("             AND K0.GHR_CD = '" + param._ghrCd + "' ");
            } else {
            	stb.append("             AND K0.GRADE = '" + param._grade + "' ");
            	stb.append("             AND K0.HR_CLASS = '" + param._hrClass + "' ");
            }
            stb.append("             AND K3.EXECUTEDATE BETWEEN '" + term._sdate + "' AND '" + term._edate + "' ");
            //校時別科目一覧表, 2:全て出力
            if (schooldiv == 1 && param._radio1.equals("2")) {
//                if (param._onlySunday != null) {
//                    //日曜日のみ出力
//                    stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) = 7 ");
//                } else {
                    //日曜日以外出力
                    stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) not in (7) ");
//                }
            }
            stb.append("          GROUP BY K3.EXECUTEDATE, K3.PERIODCD, K3.YEAR, K3.SEMESTER ");
//            stb.append("                 , K3.CHAIRCD, K3.YEAR, K3.SEMESTER ");
            
//            if (schooldiv != 1) {
//                stb.append("     UNION ALL ");
//                stb.append("          SELECT K3.EXECUTEDATE ");
//                stb.append("               , K3.PERIODCD ");
//                stb.append("               , K3.CHAIRCD ");
//                stb.append("               , K3.YEAR ");
//                stb.append("               , K3.SEMESTER ");
//                stb.append("               , MIN(K0.ATTENDNO) AS ATTENDNO ");
//                stb.append("               , K5.TESTITEMNAME ");
//                stb.append("          FROM   SCHREG_REGD_DAT K0 ");
//                stb.append("                 INNER JOIN CHAIR_STD_DAT K1 ON ");
//                stb.append("                     K0.YEAR = K1.YEAR AND  ");
//                stb.append("                     K0.SEMESTER = K1.SEMESTER AND  ");
//                stb.append("                     K0.SCHREGNO = K1.SCHREGNO ");
//                stb.append("                 INNER JOIN SCH_CHR_TEST K3 ON ");
//                stb.append("                     K3.EXECUTEDATE BETWEEN K1.APPDATE AND K1.APPENDDATE AND ");
//                stb.append("                     K3.YEAR = K1.YEAR AND  ");
//                stb.append("                     K3.SEMESTER = K1.SEMESTER AND  ");
//                stb.append("                     K3.CHAIRCD = K1.CHAIRCD ");
//                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
//                    stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV K5 ON K5.YEAR = K3.YEAR ");
//                    stb.append("                         AND K5.SEMESTER = K3.SEMESTER ");
//                    stb.append("                         AND K5.TESTKINDCD = K3.TESTKINDCD ");
//                    stb.append("                         AND K5.TESTITEMCD = K3.TESTITEMCD ");
//                    stb.append("                         AND K5.SCORE_DIV = '01' ");
//                } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(param._useTestCountflg)) {
//                    stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW K5 ON K5.YEAR = K3.YEAR ");
//                    stb.append("                         AND K5.SEMESTER = K3.SEMESTER ");
//                    stb.append("                         AND K5.TESTKINDCD = K3.TESTKINDCD ");
//                    stb.append("                         AND K5.TESTITEMCD = K3.TESTITEMCD ");
//                } else {
//                    stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG K5 ON K5.YEAR = K3.YEAR ");
//                    stb.append("                         AND K5.TESTKINDCD = K3.TESTKINDCD ");
//                    stb.append("                         AND K5.TESTITEMCD = K3.TESTITEMCD ");
//                }
//                stb.append("          WHERE  K0.YEAR = '" + param._year + "' ");
//                stb.append("             AND K0.GRADE = '" + param._grade + "' ");
//                stb.append("             AND K0.HR_CLASS = '" + param._hrClass + "' ");
//                stb.append("             AND K3.EXECUTEDATE BETWEEN '" + term._sdate + "' AND '" + term._edate + "' ");
//                //校時別科目一覧表, 2:全て出力
//                if (schooldiv == 1 && param._radio1.equals("2")) {
//                    if (param._onlySunday != null) {
//                        //日曜日のみ出力
//                        stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) = 7 ");
//                    } else {
//                        //日曜日以外出力
//                        stb.append("         AND DAYOFWEEK_ISO(K3.EXECUTEDATE) not in (7) ");
//                    }
//                }
//                stb.append("          GROUP BY ");
//                stb.append("                   K3.EXECUTEDATE, K3.PERIODCD, K3.YEAR, K3.SEMESTER, K5.TESTITEMNAME ");
//                stb.append("                 , K3.CHAIRCD ");
//            }
            
            stb.append("          ) W1  ");
//            stb.append("         INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.CHAIRCD = W1.CHAIRCD ");
//            stb.append("         LEFT JOIN SUBCLASS_MST W5 ON W5.SUBCLASSCD = W2.SUBCLASSCD ");
//            stb.append("             AND W5.CLASSCD = W2.CLASSCD ");
//            stb.append("             AND W5.SCHOOL_KIND = W2.SCHOOL_KIND ");
//            stb.append("             AND W5.CURRICULUM_CD = W2.CURRICULUM_CD ");
//            stb.append("         LEFT JOIN CLASS_MST W6 ON ");
//            stb.append("                 W6.CLASSCD = W2.CLASSCD ");
//            stb.append("             AND W6.SCHOOL_KIND = W2.SCHOOL_KIND ");
//            stb.append("         LEFT JOIN V_ELECTCLASS_MST W7 ON W7.YEAR = W1.YEAR AND W7.GROUPCD = W2.GROUPCD ");
            stb.append("         LEFT JOIN V_NAME_MST NMB001 ON NMB001.YEAR = W1.YEAR AND NMB001.NAMECD1 = 'B001' AND NMB001.NAMECD2 = W1.PERIODCD ");
            stb.append("     ) ");
            
//            stb.append(" , T_SUBCLASS_CNT AS ( ");
//            stb.append(" SELECT  EXECUTEDATE, PERIODCD, COUNT(DISTINCT SUBCLASSCD) AS SUBCLASS_CNT, COUNT(DISTINCT CHAIRCD) AS CHAIR_CNT  ");
//            stb.append(" FROM    T_MAIN  ");
//            stb.append(" GROUP BY EXECUTEDATE, PERIODCD ");
//            stb.append("     ) ");
            
            stb.append(" SELECT  T1.* ");
//            stb.append("       , T2.SUBCLASS_CNT, T2.CHAIR_CNT ");
            stb.append(" FROM    T_MAIN T1 ");
//            stb.append("         LEFT JOIN T_SUBCLASS_CNT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE AND T2.PERIODCD = T1.PERIODCD ");
            stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD ");
//            if (schooldiv == 0) {
//                stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD, T1.ATTENDNO ");
//            } else {
//                stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD, T1.SUBCLASSCD, T1.CHAIRCD ");
//            }
            
            return stb.toString();
        }

        private static class Week {
        	static final int WEEKINT0 = 0;
        	static final int WEEKINT1 = 1;
        	static final int WEEKINT9 = 9;
        	
            final Term _term;
            List<Integer> _periodStartList;
            List<String> _dateList;
            Map<String, Integer> _dateDayofweekMap;
            int _weekint;
//            Map<Integer, String> _subjectNameMap;
            Week(final Term weekTerm) {
                _term = weekTerm;
            }
            
            private static List<Week> getWeekList(final DB2UDB db2, final Param param, final Term term) {
                final Calendar cals = toCalendar(term._sdate);      //開始日付(週)
                final Calendar cale = toCalendar(term._edate);      //終了日付(週)
                
                final List<Week> weekList = new ArrayList();
                for (;;) {
                    final Week week = setWeekList(db2, param, cals, cale);    //一週間の日付をセット
                    if (week._weekint == WEEKINT9) {
                        break;                //戻り値が9なら終了
                    }
                    if (week._weekint == WEEKINT1) {
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
            private static Week setWeekList(final DB2UDB db2, final Param param, final Calendar cals, final Calendar cale) {
                String scheSdate;
                String scheEdate;
                final List<String> dateList = new ArrayList<String>();
                final List<Integer> printPeriodStartList = new ArrayList<Integer>();
                final Map<String, Integer> dateDayofweekMap = new HashMap();
//                if (param._onlySunday != null) {
//                    scheSdate = null;  //ページ週の開始日
//                    scheEdate = null;  //ページ週の終了日
//                } else {
                    scheSdate = toDateString(cals.getTime());         //ページ週の月曜日をセット
                    scheEdate = null;
//                }
                try {
                    if (cals.after(cale)) {
                        final Week week = new Week(null);
                        week._weekint = WEEKINT9;
                        return week;                 //日付が印刷範囲終了日を越えたら終了！
                    }
                    for (int j = 0; j < 8; cals.add(Calendar.DATE, 1), j++) { //月〜日まで
                        if (cals.after(cale)) {
                            break;                //日付が印刷範囲終了日を越えたら終了！
                        }
                        final String datestr = toDateString(cals.getTime());
                        //getSemesterValueでcalsの属する学期を取得し学期内なら日付を出力する->夏休み等は除外
                        final int semesterValue2 = getSemesterValue2(param, datestr);
//                        if (param._onlySunday != null) {
//                            if (semesterValue2 > 0) {
//                                final int cweek = cals.get(Calendar.DAY_OF_WEEK);
//                                if (cweek != Calendar.SUNDAY) {
//                                    continue;  //日曜日以外は回避
//                                }
//                                dateList.add(datestr);
//                                scheSdate = datestr; //ページ週の開始日をセット
//                                scheEdate = datestr; //ページ週の終了日をセット
//                                printPeriodStartList.add(new Integer(0));
//                                dateDayofweekMap.put(datestr, new Integer(0));
//                                break;
//                            }
//                        } else {
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
                            if (param._knjc043s_1PeriodCount == 16 && cals.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                                break;
                            } else if (cals.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                break;
                            }
//                        }
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
                    week._weekint = WEEKINT9;
                    return week;
                }
                if (scheEdate == null) {
                    final Week week = new Week(null);
                    week._weekint = WEEKINT1;
                    return week;                      //出力無し
                }
                final Week week = new Week(new Term("WEEK_TERM", scheSdate, scheEdate));
                week._dateList = dateList;
                week._periodStartList = printPeriodStartList;
                week._dateDayofweekMap = dateDayofweekMap;
                week._weekint = WEEKINT0;
                return week; //継続
            }
            
            /** 
             *  一週間の校時科目の出力 
             */
            private static void setWeekHead(final Week week, final DB2UDB db2, final Param param) {

                int periodcdBefore = 0;   //校時コードの保存
                int dayofweekBefore = 0;      //曜日コードの保存

                final String sql = sqlSchduleData(param, 1, week._term);
                log.info(" sql = " + sql);
                
//                week._subjectNameMap = new HashMap();

                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    
//                    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
//                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String periodcd = KnjDbUtils.getString(row, "PERIODCD");
                    final int dayofweek = getInt(row, "DAYOFWEEK", 0);
//                    final String testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
//                    final String chairname = KnjDbUtils.getString(row, "CHAIRNAME");
//                    final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
//                    final int groupcd = getInt(row, "GROUPCD", 0);
//                    final String groupname = KnjDbUtils.getString(row, "GROUPNAME");
//                    final int chairCnt = getInt(row, "CHAIR_CNT", 0);
//                    final int subclassCnt = getInt(row, "SUBCLASS_CNT", 0);

                    // 終礼
                    final boolean isShrSyurei;
//                    if ("1".equals(param._useCurriculumcd)) {
                        isShrSyurei = false; //"92".equals(classcd);
//                    } else {
//                        isShrSyurei = subclasscd != null && subclasscd.startsWith("92");
//                    }
                    if (isShrSyurei && "1".equals(param._shrSyurei)) {
                        continue;
                    }
                    final Integer intperiod = param.getPeriodPosition(periodcd);
                    if (intperiod == null) {
                        continue;
                    }
                    if (periodcdBefore == intperiod.intValue() && dayofweekBefore == dayofweek) {
                        continue;
                    }

//                    final String subjectname;
//                    final boolean isKome = groupcd == 0;
//                    if (param._definecode.usechairname) {
//                        subjectname = isKome ? getChairname(chairname, (1 < chairCnt ? " *" : "")) : getChairname(groupname, "");
//                    } else {
//                        if (testitemname != null) {
//                            subjectname = testitemname;
//                        } else {
//                            subjectname = isKome ? getChairname(subclassname, (1 < subclassCnt ? " *" : "")) : getChairname(groupname, "");
//                        }
//                    }
//                    
//                    final Integer col = new Integer(getPositionOfLine(param, dayofweek, intperiod.intValue()));
//                    week._subjectNameMap.put(col, subjectname);
                    
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
        }

        private static class Attend {
			String _executedate;
        	String _periodcd;
        	String _attendFieldColumn;
        	String _subclassname;
        	String _subclasscd;
        	String _classcd;
        	String _classname;
        	String _chairname;
        	String _staffname;
        	
        	String _kara;
        	String _executed;
        	String _hrExecuted;
        	String _diCd;
        	String _repDiCd;
        	String _dayofweek;
        	String _diName;
        	String _countflg;
        	String _leave;
        }
    }
    
    private static class Param {
        
        final String _year; //年度
        final String _semester; //学期
        final String _hrClassType;
        final String _ghrCd;
        final String _gradeHrClass; //学年
        final String _grade;
        final String _hrClass;
        final String _output1;
        final String _output2;
        final String _date1;
        final String _date2;
        final String _ctrlDateFormatJp;
        final String _nendo;
        String _hrname;
        String _staffname;
        String _teacher2;
        final List<String> _semesternameList;
        final String _output4; // 出力番号選択チェックボックス 1:空行を詰めて印字
        //final String _output3;
        final String _radio1; //1：コアタイム出力　2：全て出力
//        final String _onlySunday;
        final String _output5;
        final String _date3;
//        final String _check2;
//        final String _outputDiremark;
//        final String _isOnedayAttendCheck;
        final String _shrSyurei;
        final String _useTestCountflg;
//        final boolean _isPrintOnedayAttendInfo;
        final List<String> _c046name1List; // LHR 科目コードリスト 

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;
        
//        /** 教育課程コードを使用するか */
//        final String _useCurriculumcd;
        final boolean _hasAttendDatDiRemarkCd;
//        final String _calcRange;
        
        final String _z010Name1;
//        final boolean _isIbaraki;

        final KNJSchoolMst _knjSchoolMst;
        final KNJDefineSchool _definecode;       //各学校における定数等設定
        final int _semesterdiv;
        final String _prgid;
        final Map<String, String> _dicdDatName1Map;
        final Map _attendParamMap;

        private Date _loginDate;
        
        private Map<String, Term> _semesterDates;
        
        protected List<Period> _periodList;
        protected int _knjc043s_1PeriodCount;                  //出力校時数(１日の) 
        
    	final Map<String, File> _createdFormFiles = new HashMap();
        final boolean _isOutputDebugAll;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugVrsOut;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            if ("2".equals(_hrClassType)) {
            	_ghrCd = request.getParameter("GRADE_HR_CLASS");
            	_grade = null;
            	_hrClass = null;
            } else {
            	_ghrCd = null;
                _grade = _gradeHrClass.substring(0, 2);
                _hrClass = _gradeHrClass.substring(2);
            }
            _output1 = request.getParameter("OUTPUT1");
            _output2 = request.getParameter("OUTPUT2");
            _date1 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE1"));    //0印刷範囲開始
            _date2 = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE2"));    //1印刷範囲終了
            _output4 = request.getParameter("OUTPUT4");  //欠番を詰める
            _radio1 = StringUtils.defaultString(request.getParameter("RADIO1"), "2");
//            if ("2".equals(_radio1) && request.getParameter("CHECK1") != null) {
//                _onlySunday = "1";    //日曜日のみ出力
//            } else {
//                _onlySunday = null;
//            }
            _output5 = request.getParameter("OUTPUT5");  //「未」を出力する
            _date3 = request.getParameter("DATE3");  // ログイン日付
//            _check2 = request.getParameter("CHECK2");  // 校時別科目一覧表
//            _outputDiremark = request.getParameter("OUTPUT_DIREMARK");  // null:標準パターンを使用、 1:明細票（４０名＋出欠備考欄）パターンを使用
//            _isOnedayAttendCheck = request.getParameter("ONEDAY_ATTEND_CHECK");
            _shrSyurei = request.getParameter("SHR_SYUREI");
            _useTestCountflg = request.getParameter("useTestCountflg");

            _ctrlDateFormatJp = KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT CTRL_DATE FROM CONTROL_MST WHERE CTRL_NO = '01' "))); //現在処理日
            
            //  SVF出力編集->年度、印刷範囲
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            
            //  組名称及び担任名の取得
            setHrStaff(db2, _year, _semester);
            
            _semesternameList = getSemesterNameList(db2, _year);                                            //学期名称
            
            _z010Name1 = setZ010Name1(db2);
//            _isIbaraki = false;
//            _isPrintOnedayAttendInfo = _isIbaraki;
            _dicdDatName1Map = setDicdDatNameMap(db2);
            _knjSchoolMst = setKnjSchoolMst(db2);
            _definecode = setClasscode(db2);
            _semesterdiv = _definecode.semesdiv;
            _prgid = request.getParameter("PRGID");

//            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            
            _loginDate = setNowDate(_date3);
//            _calcRange = "年度初め " + FROM_TO_MARK + " " + KNJ_EditDate.h_format_JP(db2, _date2);
            _c046name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'C046' AND NAME1 IS NOT NULL ORDER BY NAMECD2 "), "NAME1");
            if (!_c046name1List.isEmpty()) {
            	log.info(" C046 name1List = " + _c046name1List);
            }
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);

            _hasAttendDatDiRemarkCd = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_DAT", "DI_REMARK_CD");
            _semesterDates = setSemesterDates(db2);
            
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugVrsOut = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "vrsOut");
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC043S' AND NAME = '" + propName + "' "));
        }
        
        public void close() {
        	for (final Iterator<Map.Entry<String, File>> it = _createdFormFiles.entrySet().iterator(); it.hasNext();) {
        		final Map.Entry<String, File> e = it.next();
        		final File file = e.getValue();
        		final boolean delete = file.delete();
        		log.info(" file " + file.getName() + " delete? " + delete);
        		it.remove();
        	}
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
        public void setHrStaff(final DB2UDB db2, final String year, final String semester) {
            String hrclass_name = "";     //組名称
            String staff_name = "";       //担任名
            String staff_name2 = "";      //担任名2
            try{
                String sql;
                if ("2".equals(_hrClassType)) {
                    sql = "SELECT "
                            + "GHR_NAME AS HR_NAME,"
                            + "GHR_NAMEABBV AS HR_NAMEABBV,"
                            + "W1.STAFFNAME,"
                            + "W3.STAFFNAME AS STAFFNAME2 "
                        + "FROM "
                            + "SCHREG_REGD_GHR_HDAT W2 "
                            + "LEFT JOIN STAFF_MST W1 ON W1.STAFFCD=W2.TR_CD1 "
                            + "LEFT JOIN STAFF_MST W3 ON W3.STAFFCD=W2.TR_CD2 "
                        + "WHERE "
                                + "YEAR = '" + year + "' "
                            + "AND GHR_CD = '" + _ghrCd + "' ";
                    if (!"9".equals(semester))  sql = sql               //学期指定の場合
                            + "AND SEMESTER = '" + semester + "'";
                    else                        sql = sql               //学年指定の場合
                            + "AND SEMESTER = (SELECT "
                                                + "MAX(SEMESTER) "
                                            + "FROM "
                                                + "SCHREG_REGD_GHR_HDAT W3 "
                                            + "WHERE "
                                                    + "W2.YEAR = W3.YEAR "
                                                + "AND W2.GHR_CD = W3.GHR_CD)";
                } else {
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
                			+ "AND GRADE = '" + _grade + "' AND HR_CLASS = '" + _hrClass + "' ";
                	if (!"9".equals(semester))  sql = sql               //学期指定の場合
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
                }
                
                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
                hrclass_name = KnjDbUtils.getString(row, "HR_NAME");
                staff_name = KnjDbUtils.getString(row, "STAFFNAME");
                staff_name2 = KnjDbUtils.getString(row, "STAFFNAME2");
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            _hrname = hrclass_name; //組名称
            _staffname = staff_name; //担任名
            _teacher2 = staff_name2; //担任名
        }
        
        private Map<String, Term> setSemesterDates(final DB2UDB db2) {
            final Map rtn = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            if ("2".equals(_hrClassType)) {
                stb.append("SELECT SEMESTER, SDATE, EDATE FROM SEMESTER_MST ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
            } else {
                stb.append("SELECT SEMESTER, SDATE, EDATE FROM V_SEMESTER_GRADE_MST ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
                stb.append("   AND GRADE = '" + _grade + "' ");
            }
            
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String semester = KnjDbUtils.getString(row, "SEMESTER");
                if (null == semester) {
                    continue;
                }
                rtn.put(semester, new Term("SEMESTER_" + semester + "_TERM", KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE")));
            }
            return rtn;
        }
        
        public List<String> getSemesterNameList(final DB2UDB db2, final String year) {
            final StringBuffer sql = new StringBuffer();
            if ("2".equals(_hrClassType)) {
                sql.append("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' ORDER BY SEMESTER");
            } else {
                sql.append("SELECT SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND GRADE = '" + _grade + "' ORDER BY SEMESTER");
            }
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
				final String schoolKind;
	            if ("2".equals(_hrClassType)) {
	            	schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE IN (SELECT MAX(GRADE) FROM SCHREG_REGD_DAT WHERE YEAR = '" + _year + "' AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_GHR_DAT WHERE YEAR = '" + _year + "' AND GHR_CD = '" + _ghrCd + "')) "));
	            } else {
	            	schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
	            }
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
        
        private Integer getPeriodPosition(final String periodcd) {
            for (int i = 0; i < _periodList.size(); i++) {
                final Period period = _periodList.get(i);
                if (period._cd.equals(periodcd)) {
                    return new Integer(i + 1);
                }
            }
            return null;
        }
    }
}
// eof
