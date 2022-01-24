// kanji=漢字
/*
 * $Id$
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
import servletpack.KNJA.detail.KNJ_TransferRecSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA130B {
    private static final Log log = LogFactory.getLog(KNJA130B.class);

    private static final String CERTIF_KINDCD = "107";

    private static final String _90 = "90";
    private static final String _ABROAD = "abroad";

    public static boolean DEBUG = false;

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        sd.setSvfInit(request, response, svf);

        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        final Param param = getParam(request, db2);

        // 印刷処理
        boolean nonedata = printSvf(db2, svf, param);

        // 終了処理
        param.close();
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 74072 $ $Date: 2020-05-01 11:04:53 +0900 (金, 01 5 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final List<KNJA130_0> formObj = param.getFormObj(db2, svf);
        final SchoolInfo schoolInfo = new SchoolInfo(db2, param);
        final Map<String, StaffMst> staffMstMap = StaffMst.load(db2, param);

        boolean nonedata = false;
        for (int i = 0; i < param._selectedHR.length; i++) { // HR組の繰り返し
            final List<String> schregnos = param.createSchregnos(db2, param._selectedHR[i]); // 出力対象学籍番号を格納
            for (final String schregno : schregnos) {
                final Student student = new Student(schregno, db2, schoolInfo, staffMstMap, param);
                log.debug(" student = " + student);
                param.loadStudentData(db2, student);

                for (final KNJA130_0 knjobj : formObj) {
                    knjobj.setDetail(student);
                    if (knjobj.nonedata) {
                        nonedata = true;
                    }
                }
            }
        }

        return nonedata;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B, C> Map<B, C> getMappedHashMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static <T> List<List<T>> getGroupedList(final List<T> list, final int max) {
        List<List<T>> groupedList = new ArrayList<List<T>>();
        List<T> current = null;
        for (final T t : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList<T>();
                groupedList.add(current);
            }
            current.add(t);
        }
        return groupedList;
    }

    private static String addNumber(final String i1, final String i2) {
        if (!NumberUtils.isDigits(i1)) return i2;
        if (!NumberUtils.isDigits(i2)) return i1;
        return String.valueOf((NumberUtils.isDigits(i1) ? Integer.parseInt(i1) : 0) + (NumberUtils.isDigits(i2) ? Integer.parseInt(i2) : 0));
    }

    private static String concat(final List list) {
        final StringBuffer stb = new StringBuffer();
        for (final Object o : list) {
            if (null == o) {
                continue;
            }
            stb.append(o.toString());
        }
        return stb.toString();
    }

    private static List<String> getTokenList(final String strx, final int len, final int cnt) {
        return KNJ_EditKinsoku.getTokenList(strx, len, cnt);
    }

    private static Calendar getCalendarOfDate(final String date) {
        final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(sqlDate);
        return cal;
    }

    private static String month(final String date) {
        try {
            return String.valueOf(getCalendarOfDate(date).get(Calendar.MONTH) + 1);
        } catch (Exception e) {
        }
        return null;
    }

    private static String dayOfMonth(final String date) {
        try {
            return String.valueOf(getCalendarOfDate(date).get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
        }
        return null;
    }

    private static String year(final String date) {
        try {
            return String.valueOf(getCalendarOfDate(date).get(Calendar.YEAR));
        } catch (Exception e) {
        }
        return null;
    }

    private static String getKeySubclasscd(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final Param param) {
        return classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
    }

    private static String getSubclasscd(final StudyRec studyrec, final Param param) {
        return getKeySubclasscd(studyrec._classcd, studyrec._schoolKind, studyrec._curriculumCd, studyrec._subclasscd, param);
    }

    private static String getSubclasscd(final Map<String, String> row , final Param param) {
        return KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
    }

    private static Integer sum(final List<Integer> intlist) {
        if (null == intlist || intlist.isEmpty()) {
            return null;
        }
        int sum = 0;
        for (final Integer e : intlist) {
            sum += e.intValue();
        }
        return new Integer(sum);
    }

    private static String append(final Object a, final String b) {
        if (null == a || a.toString().length() == 0) {
            return "";
        }
        return a.toString() + b;
    }

    private static <T> String debugCollectionToStr(final String debugText, final Collection<T> col, final String comma) {
        final StringBuffer stb = new StringBuffer();
        final List<T> list = new ArrayList<T>(col);
        stb.append(StringUtils.defaultString(debugText) + " [\n");
        for (int i = 0; i < list.size(); i++) {
            stb.append(i == 0 ? StringUtils.repeat(" ", StringUtils.defaultString(comma).length()) : comma).append(i).append(": ").append(list.get(i)).append("\n");
        }
        stb.append("]");
        return stb.toString();
    }

    // --- 内部クラス -------------------------------------------------------
    private static class Student {
        final String _schregno;
        final String _title;
        final List<Gakuseki> _gakusekiList;
        final List<Gakuseki> _gakusekiAttendRecList;
        final int _gradeRecNumber;
        private SchoolInfo _schoolInfo;
        final PersonalInfo _personalInfo;
        /** 住所履歴 */
        final List<AddressRec> _addressRecList;
        /** 保護者/保証人住所履歴 */
        final List<AddressRec> _guardianAddressRecList;
        /** 異動履歴 */
        final List<TransferRec> _transferRecList;
        /** 学習記録データ */
        final List<StudyRec> _studyRecList;
        /** 留年した年度 */
        final Collection<String> _dropYears;
        final Map<String, AttendRec> _attendRecMap;
        final Map<String, HtrainRemark> _htrainRemarkMap;
        /** 代替科目の備考マップのキー(年度指定無し) */
        final String _keyAll = "9999";
        /** 教科コード90の代替科目備考を表示するときtrue */
        private boolean _isShowStudyRecBikoSubstitution90;
        final List<List<Gakuseki>> _pageGakusekiListList;
        /** 総合学習活動(HTRAINREMARK_HDAT.TOTALSTUDYACT) */
        private Map<String, String> _htrainRemarkHdatAct;
        /** 総合学習評価(HTRAINREMARK_HDAT.TOTALSTUDYVAL) */
        private Map<String, String> _htrainRemarkHdatVal;
        /** 進路/就職情報 */
        final List<String> _afterGraduatedCourseTextList;
        /** [学習の記録] 備考を保持する */
        final GakushuBiko _gakushuBiko = new GakushuBiko();
        /** 保護者のかわりに保証人を表示するか */
        final boolean _isPrintGuarantor;
        final String _addressGrdHeader;
        final String _schregEntGrdHistComebackDatComebackYear;

        private Student(
                final String schregno,
                final DB2UDB db2,
                final SchoolInfo schoolInfo,
                final Map<String, StaffMst> staffMstMap,
                final Param param
        ) {
            _schregno = schregno;
            _schregEntGrdHistComebackDatComebackYear = getSchregEntGrdHistComebackDatComebackYear(db2, _schregno, param);
            loadStudent(db2, _schregno, param);
            _gakusekiList = loadGakuseki(db2, _schregno, _schregEntGrdHistComebackDatComebackYear, staffMstMap, param);
            _title = param.isKoumokuGakunen() ? "学年" : "年度";
            _gradeRecNumber = setGradeRecNumber(param);
            _personalInfo = loadPersonal(db2, _schregno, param);
            _transferRecList = loadTransferRec(db2, _schregno, param);
            _isPrintGuarantor = getPrintGuarantor(_transferRecList);
            _addressGrdHeader = _isPrintGuarantor ? "保証人" : "保護者";
            _addressRecList = AddressRec.loadAddressRec(db2, _schregno, param, AddressRec.SQL_SCHREG);
            _guardianAddressRecList = AddressRec.loadAddressRec(db2, _schregno, param, _isPrintGuarantor ? AddressRec.SQL_GUARANTOR : AddressRec.SQL_GUARDIAN);
            _dropYears = getDropYears(_gakusekiList, param);
            _studyRecList = loadStudyRec(db2, _schregno, _schregEntGrdHistComebackDatComebackYear, _gakusekiList, _dropYears, param, _gakushuBiko._ssc);
            _attendRecMap = loadAttendRec(db2, _schregno, _schregEntGrdHistComebackDatComebackYear, param);
            _htrainRemarkMap = loadHtrainRemark(db2, _schregno, param);
            if (!param._isNendogoto) {
                loadHtrainRemarkdat(db2, _schregno, param);
            }
            _afterGraduatedCourseTextList = loadAfterGraduatedCourse(db2, param, _schregno);
            _gakusekiAttendRecList = createGakusekiAttendRec(db2, _gakusekiList, _attendRecMap, param);
            createStudyRecBiko(db2, _schregno, param, _gakushuBiko);
            createStudyRecQualifiedBiko(db2, _schregno, param, _gakushuBiko);
            _gakushuBiko.createStudyRecBikoSubstitution(param, getCreditMstMap(db2, _schregno, _gakusekiList, param), getStudyRecSubclassMap(param, null));
            _pageGakusekiListList = createPageGakusekiListList(param, _gakusekiList);
            _schoolInfo = schoolInfo;
        }

        private static String getSchregEntGrdHistComebackDatComebackYear(final DB2UDB db2, final String schregno, final Param param) {
            String rtn = "0";
            if (!param._hasSchregEntGrdHistComebackDat) {
                return rtn;
            }
            final String sql = " SELECT FISCALYEAR(MAX(COMEBACK_DATE)) AS COMEBACK_YEAR FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT WHERE SCHREGNO = '" + schregno + "' AND SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ";
            // log.debug(" sql comeback = " + sql);
            for (final String comebackYear : KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "COMEBACK_YEAR")) {
                if (null != comebackYear) {
                    rtn = comebackYear;
                }
            }
            return rtn;
        }

        private Map getCreditMstMap(final DB2UDB db2, final String schregno, final List<Gakuseki> gakusekiList, final Param param) {

            final Map creditMstMap = new HashMap();

            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH REGD_YEAR(YEAR) AS (");
            String union = "";
            for (final Gakuseki gakuseki : gakusekiList) {
                sql.append(union);
                sql.append(" VALUES('" + gakuseki._year + "') ");
                union = " UNION ";
            }
            sql.append(" ) ");
            sql.append(" , MAX_SEMESTER AS ( ");
            sql.append("   SELECT T1.YEAR, T1.SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            sql.append("   FROM SCHREG_REGD_DAT T1 ");
            sql.append("   INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            sql.append("   WHERE SCHREGNO = '" + schregno + "' ");
            sql.append("   GROUP BY T1.YEAR, T1.SCHREGNO ");
            sql.append(" ) ");
            sql.append("   SELECT T1.YEAR, T1.SCHREGNO, ");
            sql.append("   T3.CLASSCD, ");
            sql.append("   T3.SCHOOL_KIND, ");
            sql.append("   T3.CURRICULUM_CD, ");
            sql.append("          T3.SUBCLASSCD, T3.CREDITS ");
            sql.append("   FROM SCHREG_REGD_DAT T1 ");
            sql.append("   INNER JOIN MAX_SEMESTER T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("       AND T2.YEAR = T1.YEAR ");
            sql.append("       AND T2.SEMESTER = T1.SEMESTER ");
            sql.append("   INNER JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
            sql.append("       AND T3.COURSECD = T1.COURSECD ");
            sql.append("       AND T3.MAJORCD = T1.MAJORCD ");
            sql.append("       AND T3.GRADE = T1.GRADE ");
            sql.append("       AND T3.COURSECODE = T1.COURSECODE ");
            sql.append("       AND T3.CREDITS IS NOT NULL ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql.toString())) {
                final String subclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
                final String key = _90.equals(KnjDbUtils.getString(row, "CLASSCD")) ? _90 : subclasscd;
                final String year = KnjDbUtils.getString(row, "YEAR");
                final Integer credits = Integer.valueOf(KnjDbUtils.getString(row, "CREDITS"));
                getMappedHashMap(creditMstMap, key).put(year + ":" + subclasscd, credits);
                getMappedHashMap(getMappedHashMap(creditMstMap, key + "-YEAR"), year).put(subclasscd, credits);
            }
            return creditMstMap;
        }

        public Map<String, StudyRecYearTotal> getStudyRecYear(final Param param) {
            return createStudyRecYear(_studyRecList, _dropYears);
        }

        private boolean getPrintGuarantor(final List<TransferRec> transferRecList) {
            boolean isPrintGuarantor = false;
            String entdate = null; // 入学日付
            for (final TransferRec tr : transferRecList) {
                if ("A002".equals(tr._nameCode1)) {
                    entdate = tr._sDate;
                    break;
                }
            }
            String birthday = null;
            if (null != _personalInfo) {
                birthday = _personalInfo._birthday;
            }
            try {
                final BigDecimal diff = diffYear(birthday, entdate);
                final int age = diff.setScale(0, BigDecimal.ROUND_DOWN).intValue();
                // 入学時の年齢が20歳以上なら保護者ではなく保証人を表示
                if (age >= 20) {
                    isPrintGuarantor = true;
                }
                log.debug(" student age = " + diff + " [year]  isPrintGuarantor? " + isPrintGuarantor);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return isPrintGuarantor;
        }

        private BigDecimal diffYear(final String date1, final String date2) {
//            log.debug(" diffYear date1 ='" + date1 + "', date2 = '" + date2 + "' ");
            if (null == date1 || null == date2) {
                return BigDecimal.valueOf(0);
            }
            final BigDecimal ALL_DAY_OF_YEAR = new BigDecimal(365);
            final Calendar cal1 = Calendar.getInstance();
            cal1.setTime(java.sql.Date.valueOf(date1));
            final int y1 = cal1.get(Calendar.YEAR);
            final int doy1 = cal1.get(Calendar.DAY_OF_YEAR);

            final Calendar cal2 = Calendar.getInstance();
            cal2.setTime(java.sql.Date.valueOf(date2));
            final int y2 = cal2.get(Calendar.YEAR);
            final int doy2 = cal2.get(Calendar.DAY_OF_YEAR);

            final BigDecimal diff = new BigDecimal(y2 - y1).add(new BigDecimal(doy2 - doy1).divide(ALL_DAY_OF_YEAR, 10, BigDecimal.ROUND_DOWN));
            return diff;
        }

        public TreeSet<String> gakusekiYearSet() {
            final TreeSet<String> set = new TreeSet<String>();
            for (final Gakuseki g : _gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        public int gakusekiMinYear() {
            int min = Integer.MAX_VALUE;
            for (final String year : gakusekiYearSet()) {
                if (null == year) {
                    continue;
                }
                final int iyear = Integer.parseInt(year);
                if (0 != iyear) {
                    min = Math.min(min, iyear);
                }
            }
            return min == Integer.MAX_VALUE ? -1 : min;
        }

        private static Collection<String> getDropYears(final List<Gakuseki> gakusekiList, final Param param) {
            final Collection<String> dropYears = new HashSet();
            for (final Gakuseki gaku : gakusekiList) {
                if (param.isGakunenSei()) {
                    if (gaku._isDrop) {
                        dropYears.add(gaku._year);
                    }
                }
            }
            return dropYears;
        }

        /**
         * 生徒名をセットします。
         * @param db2
         */
        private void loadStudent(final DB2UDB db2, final String schregno, final Param param) {
            _isShowStudyRecBikoSubstitution90 = true;
        }

        private String sql_info_reg(String t_switch, final Param param, final String schregno) {

            if (t_switch.length() < 8) {
                final StringBuffer stbx = new StringBuffer(t_switch);
                stbx.append("000000");
                t_switch = stbx.toString();
            }
            final String ts0 = t_switch.substring(0, 1);
            final String ts1 = t_switch.substring(1, 2);
            final String ts2 = t_switch.substring(2, 3);
            final String ts3 = t_switch.substring(3, 4);
            final String ts4 = t_switch.substring(4, 5);
            final String ts5 = t_switch.substring(5, 6);
            final String ts6 = t_switch.substring(6, 7);
            final String ts7 = t_switch.substring(7, 8);

            final StringBuffer sql = new StringBuffer();

            sql.append("SELECT ");
            sql.append("T2.NAME,");
            sql.append("T2.REAL_NAME,");
            sql.append("T2.GRD_DATE, ");
            sql.append("T14.NAME AS NAME_HIST_FIRST, ");
            sql.append("T18.REAL_NAME AS REAL_NAME_HIST_FIRST, ");
            sql.append("T18.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            if (ts7.equals("1")) {
                sql.append("T2.NAME_ENG,");
            }
            sql.append("T2.NAME_KANA,T2.REAL_NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,");
            sql.append("T21.BIRTHDAY_FLG,");
            if (ts7.equals("1")) {
                sql.append("T7.ABBV2 AS SEX_ENG,");
            }
            sql.append("T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,");
            // 課程・学科・コース
            if (!ts2.equals("0")) {
                sql.append("T3.COURSENAME,");
                if (param._hasMajorMstMajorname2) {
                    sql.append("VALUE(T4.MAJORNAME2, T4.MAJORNAME) AS MAJORNAME,");
                } else {
                    sql.append("T4.MAJORNAME,");
                }
                sql.append("T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,");
                if (ts7.equals("1"))
                    sql.append("T3.COURSEENG,T4.MAJORENG,");
            }
            // 卒業
            if (ts0.equals("1")) {
                sql.append("CASE WHEN T2.GRD_DATE IS NULL THEN ");
                sql.append("    RTRIM(CHAR(INT(T1.YEAR) + case t1.annual when '01' then 3 when '02' then 2 else 1 end)) || '-' ");
                sql.append("    || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' ELSE VARCHAR(T2.GRD_DATE) END AS GRADU_DATE,");
                sql.append("CASE WHEN T2.GRD_DATE IS NULL THEN '卒業見込み' ELSE ");
                sql.append("(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' ");
                sql.append("AND T2.GRD_DIV = ST2.NAMECD2) END AS GRADU_NAME,");
            }
            // 入学
            if (ts1.equals("1")) {
                sql.append("T2.ENT_DATE,T2.ENT_DIV,");
                sql.append("(SELECT DISTINCT ANNUAL FROM SCHREG_REGD_DAT ST1,SCHREG_BASE_MST ST2 ");
                sql.append("WHERE ST1.SCHREGNO=ST2.SCHREGNO AND ST1.YEAR=FISCALYEAR(ST2.ENT_DATE) AND ");
                sql.append("ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,");

                sql.append("(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND T2.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,");
                sql.append( "(SELECT MIN(TBL1.ANNUAL) FROM SCHREG_REGD_DAT TBL1 WHERE TBL1.SCHREGNO=T1.SCHREGNO AND TBL1.ANNUAL='04') AS ENTER_GRADE2,");
                sql.append( "(SELECT MIN(TBL2.YEAR)   FROM SCHREG_REGD_DAT TBL2 WHERE TBL2.SCHREGNO=T1.SCHREGNO AND TBL2.ANNUAL='04') || '-04-01' AS ENT_DATE2,");
            }
            // 住所
            if (!ts3.equals("0")) {
                sql.append("VALUE(T8.ADDR1,'') || VALUE(T8.ADDR2,'') AS ADDR,");
                sql.append("T8.ADDR1,T8.ADDR2,T8.TELNO,T8.ZIPCD,");
                if (ts7.equals("1")) {
                    sql.append("VALUE(T8.ADDR1_ENG,'') || VALUE(T8.ADDR2_ENG,'') AS ADDR_ENG,");
                    sql.append("T8.ADDR1_ENG,T8.ADDR2_ENG,");
                }
            }
            // 卒業中学情報
            if (ts4.equals("1")) {
                sql.append("T2.FINISH_DATE,");
                sql.append("FIN_S.FINSCHOOL_NAME AS J_NAME,");
                if (!"HIRO".equals(param._definecode.schoolmark)) {
                    sql.append("NM_MST.NAME1 AS INSTALLATION_DIV,");
                }
                sql.append("VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            }
            // 保護者情報
            if (ts5.equals("1")) {
                sql.append("T12.GUARD_NAME, ");
                sql.append("T12.GUARD_REAL_NAME, ");
                sql.append("T16.GUARD_NAME AS GUARD_NAME_HIST_FIRST, ");
                sql.append("T12.GUARD_REAL_KANA, ");
                sql.append("T12.GUARD_KANA,");
                sql.append("T20.GUARD_REAL_NAME AS G_R_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T20.GUARD_NAME      AS G_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,");
                sql.append("T12.GUARD_ADDR1,T12.GUARD_ADDR2,T12.GUARD_ZIPCD,");

                sql.append("T12.GUARANTOR_NAME, ");
                sql.append("T12.GUARANTOR_REAL_NAME, ");
                sql.append("T23.GUARANTOR_NAME AS GUARANTOR_NAME_HIST_FIRST, ");
                sql.append("T12.GUARANTOR_REAL_KANA, ");
                sql.append("T12.GUARANTOR_KANA,");
                sql.append("T23.GUARANTOR_KANA AS GUARANTOR_KANA_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_REAL_NAME AS GRT_R_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_NAME      AS GRT_NAME_WITH_RN_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_REAL_KANA AS GRT_R_KANA_WITH_RN_HIST_FIRST, ");
                sql.append("T25.GUARANTOR_KANA      AS GRT_KANA_WITH_RN_HIST_FIRST, ");
                sql.append("VALUE(T12.GUARANTOR_ADDR1,'') || VALUE(T12.GUARANTOR_ADDR2,'') AS GUARANTOR_ADDR,");
                sql.append("T12.GUARANTOR_ADDR1,T12.GUARANTOR_ADDR2,T12.GUARANTOR_ZIPCD,");
            }
            sql.append("(CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("T11.NAME_OUTPUT_FLG, ");
            sql.append("(CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("T26.GUARD_NAME_OUTPUT_FLG, ");
            sql.append("T1.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("(   SELECT     * ");
            sql.append("FROM       SCHREG_REGD_DAT T1 ");
            sql.append("WHERE      T1.SCHREGNO= '" + schregno + "' AND T1.YEAR= '" + param._year + "' ");
            if (ts6.equals("1")) { // 学期を特定
                sql.append("AND T1.SEMESTER= '" + param._gakki + "' ");
            } else {
                // 最終学期
                sql.append("AND T1.SEMESTER=(SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO= '" + schregno + "' AND YEAR= '" + param._year + "')");
            }

            sql.append(") T1 ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            sql.append("AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            // 卒業情報有りの場合
            if (ts0.equals("1")) {
                sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            }
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=T2.SEX ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = T2.FINSCHOOLCD ");
            if (!"HIRO".equals(param._definecode.schoolmark)) {
                sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            }
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            if (!ts2.equals("0")) {
                sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
                sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
                sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
                sql.append("AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
            }
            // 生徒住所
            if (ts3.equals("1")) {
                sql.append("LEFT JOIN SCHREG_ADDRESS_DAT AS T8 ");
                sql.append("INNER JOIN(");
                sql.append("SELECT     MAX(ISSUEDATE) AS ISSUEDATE ");
                sql.append("FROM       SCHREG_ADDRESS_DAT ");
                sql.append("WHERE      SCHREGNO= '" + schregno + "' AND FISCALYEAR(ISSUEDATE) <= '" + param._year + "' ");
                sql.append(")T9 ON T9.ISSUEDATE = T8.ISSUEDATE ON T8.SCHREGNO = T1.SCHREGNO ");
            }
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = T2.SCHREGNO AND T11.DIV = '02' ");
            // 保護者情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ");
            }

            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T13 ON T13.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T14 ON T14.SCHREGNO = T13.SCHREGNO AND T14.ISSUEDATE = T13.ISSUEDATE ");

            // 保護者履歴情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T15 ON T15.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARDIAN_HIST_DAT T16 ON T16.SCHREGNO = T15.SCHREGNO AND T16.ISSUEDATE = T15.ISSUEDATE ");
            }
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T17 ON T17.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T18 ON T18.SCHREGNO = T17.SCHREGNO AND T18.ISSUEDATE = T17.ISSUEDATE ");

            // 保護者履歴情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_REAL_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T19 ON T19.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARDIAN_HIST_DAT T20 ON T20.SCHREGNO = T19.SCHREGNO AND T20.ISSUEDATE = T19.ISSUEDATE ");
            }
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");

            // 保護者履歴情報
            if (ts5.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARANTOR_HIST_DAT WHERE GUARANTOR_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T22 ON T22.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARANTOR_HIST_DAT T23 ON T23.SCHREGNO = T15.SCHREGNO AND T23.ISSUEDATE = T22.ISSUEDATE ");

                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARANTOR_HIST_DAT WHERE GUARANTOR_REAL_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) T24 ON T24.SCHREGNO = T2.SCHREGNO ");
                sql.append("LEFT JOIN GUARANTOR_HIST_DAT T25 ON T25.SCHREGNO = T24.SCHREGNO AND T25.ISSUEDATE = T24.ISSUEDATE ");
            }
            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = T2.SCHREGNO AND T26.DIV = '02' ");

            return sql.toString();
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final String schregno, final String sYear, final Param param) {
            final String certifKind = param._isHigh ? CERTIF_KINDCD : "108";
            StringBuffer stb = new StringBuffer();
            // 印鑑関連 1
            stb.append(" WITH T_INKAN AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
            stb.append("         STAFFCD ");
            stb.append("     FROM ");
            stb.append("         ATTEST_INKAN_DAT ");
            stb.append("     GROUP BY ");
            stb.append("         STAFFCD ");
            stb.append(" ), REGD AS ( ");
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO ");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("      WHERE   T1.SCHREGNO = '" + schregno + "' ");
            stb.append("          AND '" + sYear + "' <= T1.YEAR ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER IN (SELECT  MAX(T2.SEMESTER)AS SEMESTER");
            stb.append("                             FROM    SCHREG_REGD_DAT T2");
            stb.append("                             WHERE   T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR");
            stb.append("                             GROUP BY T2.YEAR)");
            stb.append(" ), GRD_DATE_YEAR_SEMESTER AS ( ");
            stb.append("      SELECT  T1.SCHREGNO, T3.GRD_DATE, T4.YEAR, T4.SEMESTER ");
            stb.append("      FROM    (SELECT DISTINCT SCHREGNO FROM REGD) T1");
            stb.append("      LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND T3.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("          AND T3.GRD_DIV IN ('1', '2', '3') ");
            stb.append("      LEFT JOIN SEMESTER_MST T4 ON T4.SEMESTER <> '9' ");
            stb.append("          AND T3.GRD_DATE BETWEEN T4.SDATE AND T4.EDATE ");
            stb.append(" ), T_TEACHER AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.STAFFCD, ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.FROM_DATE, ");
            stb.append("         MIN(CASE WHEN VALUE(T2.GRD_DATE, '9999-12-31') < T1.TO_DATE THEN VALUE(T2.GRD_DATE, '9999-12-31') ELSE T1.TO_DATE END) AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT T1 ");
            stb.append("         LEFT JOIN GRD_DATE_YEAR_SEMESTER T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     WHERE ");
            stb.append("         T1.TR_DIV = '1' ");
            stb.append("         AND T1.FROM_DATE <= VALUE(T2.GRD_DATE, '9999-12-31') ");
            stb.append("     GROUP BY ");
            stb.append("         T1.STAFFCD, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), T_MINMAX_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         MAX(FROM_DATE) AS MAX_FROM_DATE, ");
            stb.append("         MIN(FROM_DATE) AS MIN_FROM_DATE ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER ");
            stb.append("     GROUP BY ");
            stb.append("         YEAR, GRADE, HR_CLASS ");
            stb.append(" ), T_TEACHER_MIN_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MIN_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MIN_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), T_TEACHER_MAX_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MAX_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MAX_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), PRINCIPAL_HIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR) AS ORDER ");
            stb.append("     FROM ");
            stb.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,REGD T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
            stb.append("     ORDER BY ");
            stb.append("         T2.YEAR, T1.FROM_DATE ");
            stb.append(" ), YEAR_PRINCIPAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR ");
            stb.append("         ,T2.STAFFCD AS PRINCIPALSTAFFCD1, T2.FROM_DATE AS PRINCIPAL1_FROM_DATE, T2.TO_DATE AS PRINCIPAL1_TO_DATE ");
            stb.append("         ,T3.STAFFCD AS PRINCIPALSTAFFCD2, T3.FROM_DATE AS PRINCIPAL2_FROM_DATE, T3.TO_DATE AS PRINCIPAL2_TO_DATE ");
            stb.append("     FROM ( ");
            stb.append("       SELECT YEAR, MIN(ORDER) AS FIRST, MAX(ORDER) AS LAST FROM PRINCIPAL_HIST GROUP BY YEAR ");
            stb.append("      ) T1 ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T2 ON T2.YEAR = T1.YEAR AND T2.ORDER = T1.LAST ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T3 ON T3.YEAR = T1.YEAR AND T3.ORDER = T1.FIRST ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T8.GRADE_CD ");
            stb.append("   ,T8.GRADE_NAME2 ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,T3.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("         ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,T4.STAFFCD AS STAFFCD1 ");
            stb.append("   ,T9.STAFFCD AS STAFFCD2 ");
            stb.append("   ,T10.FROM_DATE AS STAFF1_FROM_DATE, T10.TO_DATE AS STAFF1_TO_DATE ");
            stb.append("   ,T11.FROM_DATE AS STAFF2_FROM_DATE, T11.TO_DATE AS STAFF2_TO_DATE ");
            stb.append("   ,T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME ");
            stb.append("   ,T13.STAFFCD AS PRINCIPALSTAFFCD1 ");
            stb.append("   ,T14.STAFFCD AS PRINCIPALSTAFFCD2 ");
            stb.append("   ,T12.PRINCIPAL1_FROM_DATE, T12.PRINCIPAL1_TO_DATE ");
            stb.append("   ,T12.PRINCIPAL2_FROM_DATE, T12.PRINCIPAL2_TO_DATE ");

            // 印鑑関連 2
            stb.append("   ,ATTEST.CHAGE_OPI_SEQ ");
            stb.append("   ,ATTEST.LAST_OPI_SEQ ");
            stb.append("   ,ATTEST.FLG ");
            stb.append("   ,IN1.STAMP_NO AS CHAGE_STAMP_NO ");
            stb.append("   ,IN2.STAMP_NO AS LAST_STAMP_NO ");
            stb.append("   ,IN21.STAMP_NO AS LAST_STAMP_NO1 ");
            stb.append("   ,IN22.STAMP_NO AS LAST_STAMP_NO2 ");

            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("                              AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MAX_FROM_DATE T10A ON T10A.YEAR = T1.YEAR ");
            stb.append("          AND T10A.GRADE = T1.GRADE AND T10A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T10 ON T10.STAFFCD = T10A.STAFFCD ");
            stb.append("          AND T10.FROM_DATE = T10A.MAX_FROM_DATE AND T10.YEAR = T1.YEAR AND T10.GRADE = T1.GRADE AND T10.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MIN_FROM_DATE T11A ON T11A.YEAR = T1.YEAR ");
            stb.append("          AND T11A.GRADE = T1.GRADE AND T11A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T11 ON T11.STAFFCD = T11A.STAFFCD ");
            stb.append("          AND T11.FROM_DATE = T11A.MIN_FROM_DATE AND T11.YEAR = T1.YEAR AND T11.GRADE = T1.GRADE AND T11.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T10.STAFFCD ");
            stb.append(" LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T11.STAFFCD ");

            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '" + certifKind + "'");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT T8 ON T8.YEAR = T1.YEAR ");
            stb.append("          AND T8.GRADE = T1.GRADE ");

            // 印鑑関連 3
            stb.append(" LEFT JOIN T_INKAN IN1 ON IN1.STAFFCD = T10.STAFFCD ");
            stb.append(" LEFT JOIN T_INKAN IN2 ON IN2.STAFFCD = T6.REMARK7 ");
            stb.append(" LEFT JOIN ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.CHAGE_OPI_SEQ, ");
            stb.append("         T1.LAST_OPI_SEQ, ");
            stb.append("         L1.FLG ");
            stb.append("     FROM ");
            stb.append("         ATTEST_OPINIONS_WK T1 ");
            stb.append("         LEFT JOIN ATTEST_OPINIONS_UNMATCH L1 ");
            stb.append("                ON L1.YEAR = T1.YEAR ");
            stb.append("               AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("      ) ATTEST ON ATTEST.YEAR = T1.YEAR AND ATTEST.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN YEAR_PRINCIPAL T12 ON T12.YEAR = T1.YEAR ");
            stb.append(" LEFT JOIN STAFF_MST T13 ON T13.STAFFCD = T12.PRINCIPALSTAFFCD1 ");
            stb.append(" LEFT JOIN STAFF_MST T14 ON T14.STAFFCD = T12.PRINCIPALSTAFFCD2 ");
            stb.append(" LEFT JOIN T_INKAN IN21 ON IN21.STAFFCD = T13.STAFFCD ");
            stb.append(" LEFT JOIN T_INKAN IN22 ON IN22.STAFFCD = T14.STAFFCD ");
            stb.append(" order by t1.hr_class");
            return stb.toString();
        }

        /**
         * 学籍履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<Gakuseki> loadGakuseki(final DB2UDB db2, final String schregno, final String sYear, final Map<String, StaffMst> staffMstMap, final Param param) {
            final List<Gakuseki> gakusekiList = new LinkedList<Gakuseki>();
            final Map hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
            try {
                final String sql = sqlSchGradeRec(schregno, sYear, param);
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
                    final String grade = KnjDbUtils.getString(row, "GRADE");
                    final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                    final String gradeName2 = KnjDbUtils.getString(row, "GRADE_NAME2");
                    final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                    String hrname = null;
                    if ("1".equals(param._useSchregRegdHdat)) {
                        hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    } else if ("0".equals(param._useSchregRegdHdat)) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrclass, hmap);
                    }

                    if (null == hrname) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrclass);
                    }

                    final String attendno = String.valueOf(KnjDbUtils.getInt(row, "ATTENDNO", new Integer(0)).intValue());
                    final String annual = String.valueOf(KnjDbUtils.getInt(row, "ANNUAL", new Integer(0)).intValue());
                    final String principalName = KnjDbUtils.getString(row, "PRINCIPALNAME");
//                  final String principalStaffcd = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD");
                    final String principalStaffcd1 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD1");
                    final String principalStaffcd2 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD2");
                    final String staffcd1 = KnjDbUtils.getString(row, "STAFFCD1");
                    final String staffcd2 = KnjDbUtils.getString(row, "STAFFCD2");
                    final String staff1FromDate = KnjDbUtils.getString(row, "STAFF1_FROM_DATE");
                    final String staff1ToDate = KnjDbUtils.getString(row, "STAFF1_TO_DATE");
                    final String staff2FromDate = KnjDbUtils.getString(row, "STAFF2_FROM_DATE");
                    final String staff2ToDate = KnjDbUtils.getString(row, "STAFF2_TO_DATE");
                    final String principal1FromDate = KnjDbUtils.getString(row, "PRINCIPAL1_FROM_DATE");
                    final String principal1ToDate = KnjDbUtils.getString(row, "PRINCIPAL1_TO_DATE");
                    final String principal2FromDate = KnjDbUtils.getString(row, "PRINCIPAL2_FROM_DATE");
                    final String principal2ToDate = KnjDbUtils.getString(row, "PRINCIPAL2_TO_DATE");

                    final String staffSeq = KnjDbUtils.getString(row, "CHAGE_OPI_SEQ");
                    final String principalSeq = KnjDbUtils.getString(row, "LAST_OPI_SEQ");
                    final String kaizanFlg = KnjDbUtils.getString(row, "FLG");
                    final String staffStampNo = KnjDbUtils.getString(row, "CHAGE_STAMP_NO");
                    final String principalStampNo = KnjDbUtils.getString(row, "LAST_STAMP_NO");
                    final String principalStampNo1 = KnjDbUtils.getString(row, "LAST_STAMP_NO1");

                    final Staff principal = new Staff(year, new StaffMst(null, principalName, null, null, null), null, null, principalStampNo);
                    final Staff principal1 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, principalStampNo1);
                    final Staff principal2 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);
                    final Staff staff1 = new Staff(year, StaffMst.get(staffMstMap, staffcd1), staff1FromDate, staff1ToDate, staffStampNo);
                    final Staff staff2 = new Staff(year, StaffMst.get(staffMstMap, staffcd2), staff2FromDate, staff2ToDate, null);

                    final Gakuseki gakuseki = new Gakuseki(year, nendo, grade, gradeCd, gradeName2, hrclass, hrname, attendno, annual,
                            principal, principal1, principal2, staff1, staff2,
                            staffSeq, principalSeq, kaizanFlg);
                    if (!param.isGdatH(gakuseki._year, gakuseki._grade)) {
                        continue;
                    }
                    gakusekiList.add(gakuseki);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }

            // リストをソートします。
            Collections.sort(gakusekiList);

            // 学年制の場合、留年対応します。
            if (param.isGakunenSei()) {
                String grade = null;
                for (final ListIterator<Gakuseki> i = gakusekiList.listIterator(gakusekiList.size()); i.hasPrevious();) {
                    final Gakuseki gaku = i.previous();
                    if (null != grade && gaku._grade.equals(grade)) {
                        gaku._isDrop = true;
                    }
                    grade = gaku._grade;
                }
            }
            return gakusekiList;
        }

        /**
         * 個人情報クラスを作成ます。
         */
        private PersonalInfo loadPersonal(final DB2UDB db2, final String schregno, final Param param) {

            String studentRealName = null;
            String studentName = null;
            boolean isPrintRealName = false;
            boolean isPrintNameAndRealName = false;
            /** 最も古い履歴の生徒名 */
            String studentNameHistFirst = null;

            String studentKana = null;
            String courseName = null;
            String majorName = null;
            String guardKana = null;
            String guardName = null;
            String guardNameHistFirst = null;
            String guarantorKana = null;
            String guarantorName = null;
            String guarantorNameHistFirst = null;
            String birthday = null;
            String birthdayStr = null;
            String sex = null;
            String finishDate = null;
            String installationDiv = null;
            String juniorSchoolName = null;
            String finschoolTypeName = null;
            String schAddress1 = null;
            String schAddress2 = null;

            try {
                final String sql = sql_info_reg("1111111000", param, schregno);

                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));

                isPrintRealName = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME"));
                isPrintNameAndRealName = "1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG"));

                final String name = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME"));
                final String nameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_HIST_FIRST"));
                if (isPrintRealName) {
                    final String realName = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME"));
                    final String realNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME_HIST_FIRST"));
                    final String nameWithRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_WITH_RN_HIST_FIRST"));

                    if (isPrintNameAndRealName) {
                        if (StringUtils.isBlank(realName + name)) {
                            studentRealName = "";
                            studentName     = "";
                        } else {
                            studentRealName = realName;
                            studentName     = StringUtils.isBlank(name) ? "" : realName.equals(name) ? name : "（" + name + "）";
                        }
                        studentNameHistFirst = StringUtils.isBlank(realNameHistFirst + nameWithRealNameHistFirst) ? "" : realNameHistFirst.equals(nameWithRealNameHistFirst) ? realNameHistFirst : realNameHistFirst + "（" + nameWithRealNameHistFirst + "）";
                    } else {
                        studentRealName      = StringUtils.isBlank(realName) ? name : realName;
                        studentName          = name;
                        studentNameHistFirst = realNameHistFirst;
                    }
                } else {
                    studentRealName      = "";
                    studentName          = name;
                    studentNameHistFirst = nameHistFirst;
                }

                final String nameKana0 = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_KANA"));
                if (isPrintRealName) {
                    final String realNameKana = StringUtils.defaultString(KnjDbUtils.getString(row, "REAL_NAME_KANA"));

                    studentKana = getNameForm(nameKana0, realNameKana, isPrintRealName, isPrintNameAndRealName);

                } else {
                    studentKana = nameKana0;
                }
                courseName = KnjDbUtils.getString(row, "COURSENAME");
                majorName = KnjDbUtils.getString(row, "MAJORNAME");

                final String guardKana0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_KANA"));
                final String guardName0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_NAME"));
                final String guardNameHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_NAME_HIST_FIRST"));
                final String guarantorKana0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_KANA"));
                final String guarantorName0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_NAME"));
                final String guarantorNameHistFirst0 = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_NAME_HIST_FIRST"));

                final boolean isGuardPrintRealName = "1".equals(KnjDbUtils.getString(row, "USE_GUARD_REAL_NAME"));
                final boolean isGuardPrintNameAndRealName = "1".equals(KnjDbUtils.getString(row, "GUARD_NAME_OUTPUT_FLG"));
                if (isGuardPrintRealName) {
                    final String guardRealKana = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_REAL_KANA"));
                    final String guardRealName = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARD_REAL_NAME"));
                    final String guardRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "G_R_NAME_WITH_RN_HIST_FIRST"));
                    final String guardNameWithGuardRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "G_NAME_WITH_RN_HIST_FIRST"));

                    final String guarantorRealKana = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_REAL_KANA"));
                    final String guarantorRealName = StringUtils.defaultString(KnjDbUtils.getString(row, "GUARANTOR_REAL_NAME"));
                    final String guarantorRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "GRT_R_NAME_WITH_RN_HIST_FIRST"));
                    final String guarantorNameWithGuarantorRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "GRT_NAME_WITH_RN_HIST_FIRST"));

                    guardKana = getNameForm(guardKana0, guardRealKana, isGuardPrintRealName, isGuardPrintNameAndRealName);
                    guardName = getNameForm(guardName0, guardRealName, isGuardPrintRealName, isGuardPrintNameAndRealName);
                    guardNameHistFirst = getNameForm(guardNameWithGuardRealNameHistFirst, guardRealNameHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName);
                    guarantorKana = getNameForm(guarantorKana0, guarantorRealKana, isGuardPrintRealName, isGuardPrintNameAndRealName);
                    guarantorName = getNameForm(guarantorName0, guarantorRealName, isGuardPrintRealName, isGuardPrintNameAndRealName);
                    guarantorNameHistFirst = getNameForm(guarantorNameWithGuarantorRealNameHistFirst, guarantorRealNameHistFirst, isGuardPrintRealName, isGuardPrintNameAndRealName);

                } else {
                    guardKana = guardKana0;
                    guardName = guardName0;
                    guardNameHistFirst = guardNameHistFirst0;
                    guarantorKana = guarantorKana0;
                    guarantorName = guarantorName0;
                    guarantorNameHistFirst = guarantorNameHistFirst0;
                }
                birthday = KnjDbUtils.getString(row, "BIRTHDAY");
                birthdayStr = getBirthday(db2, birthday, KnjDbUtils.getString(row, "BIRTHDAY_FLG"), param);
                sex = KnjDbUtils.getString(row, "SEX");
                finishDate = getFinishDate(db2, KnjDbUtils.getString(row, "FINISH_DATE"), param);
                log.debug("schoolmark = "+param._definecode.schoolmark);
                if (!"HIRO".equals(param._definecode.schoolmark)) {
                    installationDiv = KnjDbUtils.getString(row, "INSTALLATION_DIV");
                }
                juniorSchoolName = KnjDbUtils.getString(row, "J_NAME");
                finschoolTypeName = KnjDbUtils.getString(row, "FINSCHOOL_TYPE_NAME");
                schAddress1 = KnjDbUtils.getString(row, "ADDR1");
                schAddress2 = KnjDbUtils.getString(row, "ADDR2");
            } catch (final Exception e) {
                log.error("個人情報クラス作成にてエラー", e);
            }
            final PersonalInfo personalInfo = new PersonalInfo(
                    studentRealName,
                    studentName,
                    isPrintRealName,
                    isPrintNameAndRealName,
                    studentNameHistFirst,
                    courseName,
                    majorName,
                    studentKana,
                    guardKana,
                    guardName,
                    guardNameHistFirst,
                    guarantorKana,
                    guarantorName,
                    guarantorNameHistFirst,
                    birthday,
                    birthdayStr,
                    sex,
                    finishDate,
                    installationDiv,
                    juniorSchoolName,
                    finschoolTypeName,
                    schAddress1,
                    schAddress2
            );
            return personalInfo;
        }

        private String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param) {
            final String birthday;
            if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
                birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
            } else {
                birthday = KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP(db2, date));
            }
            return birthday;
        }

        private String getFinishDate(final DB2UDB db2, final String finishDate, final Param param) {
            return KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP_M(db2, finishDate), param._year);
        }

        private String getNameForm(final String name, final String realname, final boolean showreal, final boolean showboth) {
            final String rtn;
            if (showboth && !StringUtils.isBlank(realname) && !StringUtils.isBlank(name)) {
                if (realname.equals(name)) {
                    rtn = realname;
                } else {
                    rtn = realname + "（" + name + "）";
                }
            } else if (showreal && !StringUtils.isBlank(realname)) {
                rtn = realname;
            } else if (!StringUtils.isBlank(name)) {
                rtn = name;
            } else {
                rtn = "";
            }
            return rtn;
        }

        /**
         * 異動履歴クラスを作成し、リストに加えます。
         */
        private List<TransferRec> loadTransferRec(final DB2UDB db2, final String schregno, final Param param) {
            final List<TransferRec> transferRecList = new LinkedList<TransferRec>();
            final String psKey = "PS_TRANSFER";
            if (null == param._psMap.get(psKey)) {
                final Map paramMap = new HashMap();
                paramMap.put("useAddrField2", param._useAddrField2);

                final KNJ_TransferRecSql obj = new KNJ_TransferRecSql();
                final String sql = obj.sql_state(paramMap);
                param.setPs(db2, psKey, sql);
            }

            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, schregno, schregno, schregno, schregno, param._year})) {
                final String nameCode1 = KnjDbUtils.getString(row, "NAMECD1");
                final String nameCode2 = KnjDbUtils.getString(row, "NAMECD2");
                final String name = null != KnjDbUtils.getString(row, "NAME1") ? KnjDbUtils.getString(row, "NAME1") : "";
                final String sYear = KnjDbUtils.getString(row, "YEAR");
                final String sDate = KnjDbUtils.getString(row, "SDATE");
                final String eDate = KnjDbUtils.getString(row, "EDATE");
                final String sDateStr = KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP(db2, sDate), param._year);
                final String eDateStr = KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP(db2, eDate), param._year);
                final String grade = StringUtils.isNotBlank(KnjDbUtils.getString(row, "GRADE")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "GRADE"))) : null;
                final String gradeCd = StringUtils.isNotBlank(KnjDbUtils.getString(row, "GRADE_CD")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "GRADE_CD"))) : null;
                final String reason = "".equals(KnjDbUtils.getString(row, "REASON")) ? null : KnjDbUtils.getString(row, "REASON");
                final String place = "".equals(KnjDbUtils.getString(row, "PLACE")) ? null : KnjDbUtils.getString(row, "PLACE");
                final String address = "".equals(KnjDbUtils.getString(row, "ADDR")) ? null : KnjDbUtils.getString(row, "ADDR");
                final String address2 = !"1".equals(param._useAddrField2) || "".equals(KnjDbUtils.getString(row, "ADDR2")) ? null : KnjDbUtils.getString(row, "ADDR2");
                final String certifno = "".equals(KnjDbUtils.getString(row, "CERTIFNO")) ? null : KnjDbUtils.getString(row, "CERTIFNO");

                final TransferRec transferRec = new TransferRec(
                        nameCode1,
                        nameCode2,
                        name,
                        sYear,
                        sDate,
                        sDateStr,
                        eDate,
                        eDateStr,
                        grade,
                        gradeCd,
                        reason,
                        place,
                        address,
                        address2,
                        certifno);
                transferRecList.add(transferRec);
            }
            return transferRecList;
        }

        /**
         * 指定年度年次の学籍を得る
         * @param year 指定年度
         * @param annual1 指定年次
         * @return 学籍
         */
        private Gakuseki getGakuseki(final String year, final String annual1) {
            if (year == null || annual1 == null) {
                return null;
            }
            final String annual = Integer.valueOf(annual1).toString();
            for (final Gakuseki gaku : _gakusekiList) {
                if (year.equals(gaku._year) && annual.equals(gaku._annual)) {
                    return gaku;
                }
            }
            return null;
        }

        public <T extends Comparable<T>> String getSogoSubclassname(final Param param, final TreeMap<T, Gakuseki> yearGakusekiMap, final T removeKey) {
            T minKey = null;
            Gakuseki minKeyGakuseki = null;
            yearGakusekiMap.remove(removeKey);
            if (!yearGakusekiMap.isEmpty()) {
                minKey = yearGakusekiMap.firstKey();
                minKeyGakuseki = yearGakusekiMap.get(minKey);
            }
            final int tankyuStartYear = 2019;
            boolean isTankyu = false;
            if (null != minKeyGakuseki) {
                final int year = NumberUtils.isDigits(minKeyGakuseki._year) ? Integer.parseInt(minKeyGakuseki._year) : 9999;
                final int gradeCdInt = NumberUtils.isDigits(minKeyGakuseki._grade) ? Integer.parseInt(minKeyGakuseki._grade) : 99;
                if (year == tankyuStartYear     && gradeCdInt <= 1
                 || year == tankyuStartYear + 1 && gradeCdInt <= 2
                 || year == tankyuStartYear + 2 && gradeCdInt <= 3
                 || year >= tankyuStartYear + 3
                 ) {
                    isTankyu = true;
                }
            }
            if (param._isOutputDebug) {
                log.info(" 探究? " + isTankyu + ", startKey = " + minKey + ", minKeyGakuseki = " + minKeyGakuseki);
            }
            return isTankyu ? "総合的な探究の時間" : "総合的な学習の時間";
        }

        /**
         * 学習記録データクラスを作成し、リストに加えます。
         * @param db2
         */
        private List<StudyRec> loadStudyRec(final DB2UDB db2, final String schregno, final String sYear, final List<Gakuseki> gakusekiList, final Collection dropYears, final Param param,
                final StudyrecSubstitutionContainer ssc) {
            // 学年制の場合、留年対応します。
            final List<StudyRec> studyRecList = new LinkedList<StudyRec>();
            final TreeMap<String, String> yearAnnualMap = new TreeMap<String, String>(); // 在籍データの年度と年次のマップ
            for (final Gakuseki gaku : gakusekiList) {
                if (null != gaku._annual) {
                    yearAnnualMap.put(gaku._year, gaku._annual);
                }
            }

            studyRecList.addAll(createAbroadStudyrec(db2, schregno, sYear, param, yearAnnualMap));

            studyRecList.addAll(createStudyrec(db2, schregno, sYear, param));

            // 全部/一部代替科目取得
            studyRecList.addAll(createStudyrecSubstitution(db2, schregno, sYear, dropYears, param, ssc));

            // リストをソートします。
            Collections.sort(studyRecList);
            if (DEBUG) {
                for (final StudyRec sr : studyRecList) {
                    log.debug(" studyrec subclasscd = " + getSubclasscd(sr, param) + " " + sr._subclassname);
                }
            }
            return studyRecList;
        }

        private List<StudyRec> createStudyrecSubstitution(final DB2UDB db2, final String schregno, final String sYear, final Collection<String> dropYears, final Param param,
                final StudyrecSubstitutionContainer ssc) {
            final List<StudyRec> studyRecList = new ArrayList<StudyRec>();

            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final String sql = sqlReplaceSubClassSubstitution(param, substitutionTypeFlg, schregno, sYear);
                if (param._isOutputDebug) {
                    log.info(" subst " + substitutionTypeFlg + " sql = " + sql);
                }
                final List<StudyRec> l = new ArrayList<StudyRec>();
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                    if (dropYears.contains(KnjDbUtils.getString(row, "YEAR"))) {
                        continue;
                    }
                    final Map<String, StudyRecSubstitution> studyrecSubstitution = getMappedMap(ssc.getStudyrecSubstitution(substitutionTypeFlg), KnjDbUtils.getString(row, "YEAR"));
                    l.add(createReplacedStudyRec(row, studyrecSubstitution, param));
                }
                if (param._isOutputDebug) {
                    log.info(" subst " + substitutionTypeFlg + " list = " + l);
                }
                studyRecList.addAll(l);
            }
            return studyRecList;
        }

        private List<StudyRec> createStudyrec(final DB2UDB db2, final String schregno, final String sYear, final Param param) {
            final List<StudyRec> studyRecList = new ArrayList<StudyRec>();
            final String sql = sqlStudyrec(param, schregno, sYear);
            if (param._isOutputDebug) {
                log.info(" studyrec sql = " + sql);
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                final String curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String grades = KnjDbUtils.getString(row, "GRADES");
                final StudyRec studyRec = new StudyRec(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "ANNUAL"),
                        KnjDbUtils.getString(row, "CLASSCD"), schoolKind, curriculumCd, KnjDbUtils.getString(row, "SUBCLASSCD"),
                        KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSNAME"), KnjDbUtils.getString(row, "SPECIALDIV"),
                        KnjDbUtils.getInt(row, "CREDIT", null), KnjDbUtils.getInt(row, "ADD_CREDIT", null),
                        KnjDbUtils.getInt(row, "COMP_CREDIT", null),
                        NumberUtils.isNumber(grades) ? Double.valueOf(grades) : null,
                        KnjDbUtils.getInt(row, "SHOWORDERCLASS", null), KnjDbUtils.getInt(row, "SHOWORDERSUBCLASS", null));
                if ("0".equals(studyRec._year) && !param._seitoSidoYorokuZaisekiMae) {
                    continue;
                }
                studyRecList.add(studyRec);
            }
            return studyRecList;
        }

        private List<StudyRec> createAbroadStudyrec(final DB2UDB db2, final String schregno, final String sYear, final Param param, final TreeMap<String, String> yearAnnualMap) {
            final List<StudyRec> studyRecList = new ArrayList<StudyRec>();
            final String sql = sqlAbroadCredit(param, schregno, sYear);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String annual;
                if (null != yearAnnualMap.get(year)) {
                    annual = (String) yearAnnualMap.get(year);
                } else { // 在籍データの範囲外の留学
                    if (param._seitoSidoYorokuZaisekiMae) {
                        final String minYear = yearAnnualMap.isEmpty() || null == yearAnnualMap.firstKey() ? "0000" : yearAnnualMap.firstKey();
                        if (year.compareTo(minYear) < 0) {
                            final int diffyear = Integer.parseInt(minYear) - Integer.parseInt(year);
                            final int minannual = Integer.parseInt(yearAnnualMap.get(minYear));
                            annual = new DecimalFormat("00").format(minannual - diffyear); // 年度の差分から在籍前留学時の年次を計算した値
                        } else {
                            annual = null;
                        }
                    } else {
                        annual = null;
                    }
                }
                final StudyRec studyRec = new StudyRec(year, annual, KnjDbUtils.getInt(row, "CREDIT", null), true);
                studyRecList.add(studyRec);
//                log.debug(" abroad record = " + studyRec);
            }
            return studyRecList;
        }

        /** 代替科目作成 */
        private StudyRec createReplacedStudyRec(final Map<String, String> row, final Map<String, StudyRecSubstitution> studyrecSubstitutionMap, final Param param) {

            final String year = KnjDbUtils.getString(row, "YEAR");
            final String annual = KnjDbUtils.getString(row, "ANNUAL");
            final String substitutionClasscd = KnjDbUtils.getString(row, "SUBSTITUTION_CLASSCD");           // 代替先科目教科コード
            final String substitutionClassName = KnjDbUtils.getString(row, "SUBSTITUTION_CLASSNAME");       // 代替先科目教科名
            final String substitutionSubClasscd = KnjDbUtils.getString(row, "SUBSTITUTION_SUBCLASSCD");     // 代替先科目コード
            final String substitutionSubClassName = KnjDbUtils.getString(row, "SUBSTITUTION_SUBCLASSNAME"); // 代替先科目名
            final Integer credit = null;
            final Double grades = null;
            final Integer showorderClass = Integer.valueOf(KnjDbUtils.getString(row, "SHOWORDERCLASS"));
            final Integer showorderSubClass = Integer.valueOf(KnjDbUtils.getString(row, "SHOWORDERSUBCLASS"));
            final String specialDiv = KnjDbUtils.getString(row, "SPECIALDIV");
            final String substitutionSchoolKind = KnjDbUtils.getString(row, "SUBSTITUTION_SCHOOL_KIND");
            final String substitutionCurriculumCd = KnjDbUtils.getString(row, "SUBSTITUTION_CURRICULUM_CD");
            final String mapKey = substitutionClasscd + "-" + substitutionSchoolKind + "-" + substitutionCurriculumCd + "-" + substitutionSubClasscd;

            final StudyRec replacedStudyRec = new StudyRec(
                    year, annual, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubClasscd,
                    substitutionClassName, substitutionSubClassName, specialDiv,
                    credit, null, null, grades, showorderClass, showorderSubClass);

            final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"); // 代替元科目コード
            final String attendClasscd = KnjDbUtils.getString(row, "ATTEND_CLASSCD");
            final String attendSchoolKind = KnjDbUtils.getString(row, "ATTEND_SCHOOL_KIND");
            final String attendCurriculumCd = KnjDbUtils.getString(row, "ATTEND_CURRICULUM_CD");
            final String attendClassname = KnjDbUtils.getString(row, "ATTEND_CLASSNAME"); // 代替元教科名称
            final String attendSubclassname = KnjDbUtils.getString(row, "ATTEND_SUBCLASSNAME"); // 代替元科目名称
            final String substitutionCredit = KnjDbUtils.getString(row, "SUBSTITUTION_CREDIT") == null ? " " : KnjDbUtils.getString(row, "SUBSTITUTION_CREDIT"); // 代替先単位

            if (null == studyrecSubstitutionMap.get(mapKey)) { // すでに同一の代替元科目がある場合
                final StudyRecSubstitution studyRecSubstitution = new StudyRecSubstitution(
                        null, annual, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubClasscd,
                        substitutionClassName, substitutionSubClassName, specialDiv,
                        credit, grades, showorderClass, showorderSubClass);
                studyrecSubstitutionMap.put(mapKey, studyRecSubstitution);
            }
            final StudyRecSubstitution studyRecSubstitution = studyrecSubstitutionMap.get(mapKey);
            studyRecSubstitution.attendSubclasses.add(new StudyRecSubstitution.AttendSubclass(year, attendClasscd, attendSchoolKind, attendCurriculumCd, attendSubclasscd, attendClassname, attendSubclassname, substitutionCredit));
            return replacedStudyRec;
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private String pre_sql_Common(final Param param) {

            // 教科コードが90より大きい対象
            String classcd90Over = "";
            if (!param._e065Name1JiritsuKatsudouSubclasscdList.isEmpty()) {
                classcd90Over = " OR T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ('" + GakushuBiko.mkStringUnique(param._e065Name1JiritsuKatsudouSubclasscdList, "', '") + "') ";
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            stb.append("        ,T1.SCHOOL_KIND ");
            stb.append("        ,T1.CURRICULUM_CD ");
            stb.append("        ,T1.SUBCLASSCD ");
            stb.append("        ,VALUATION AS GRADES ");
            stb.append("        ,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
            stb.append("        ,VALUE(T1.COMP_CREDIT, 0) AS COMP_CREDIT ");
            stb.append("        ,T1.ADD_CREDIT ");
            stb.append("        ,CLASSNAME ");
            stb.append("        ,SUBCLASSNAME ");
            stb.append("        ,T1.SCHOOLCD ");
            stb.append(" FROM    SCHREG_STUDYREC_DAT T1 ");
            if (param._hasStudyrefProvFlgDat) {
                stb.append("         LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND L3.YEAR = T1.YEAR ");
                stb.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                stb.append("            AND L3.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND L3.PROV_FLG = '1' ");
            }
            stb.append(" WHERE   EXISTS(SELECT 'X' FROM SCHBASE T2 WHERE T1.SCHREGNO = T2.SCHREGNO AND T2.SYEAR <= T1.YEAR AND T1.YEAR <= T2.YEAR) ");
            stb.append("     AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' " + classcd90Over +") ");
            if ("1".equals(param._seitoSidoYorokuNotPrintAnotherStudyrec)) {
                stb.append("      AND T1.SCHOOLCD <> '1' ");
            }
            if (param.isGakunenSei() && param._isHeisetuKou && param._isHigh) {
                stb.append("      AND T1.ANNUAL not in " + param._gradeInChugaku + " ");
            }
            if (!param._isPrintMirisyu) {
                stb.append("      AND NOT (T1.COMP_CREDIT IS NOT NULL ");
                stb.append("               AND T1.COMP_CREDIT = 0 ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NOT NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END = 0) ");
            }
            if (!param._isPrintRisyuNomi) {
                stb.append("      AND (");
                stb.append("         NOT (T1.COMP_CREDIT IS NOT NULL ");
                stb.append("               AND T1.COMP_CREDIT <> 0 ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NOT NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END = 0) ");
                stb.append(classcd90Over);
                stb.append("    ) ");
            } else {
                stb.append("      AND (");
                if (param._isPrintRisyuTourokuNomi) {
                    // 履修登録のみ
                    stb.append("         (T1.COMP_CREDIT IS NULL ");
                    stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NULL) ");
                    stb.append("         OR ");
                }
                if (param._isPrintMirisyu) {
                    stb.append("         (T1.COMP_CREDIT IS NOT NULL OR T1.COMP_CREDIT IS NULL) ");
                } else {
                    stb.append("         T1.COMP_CREDIT <> 0 ");
                }
                stb.append(classcd90Over);
                stb.append("    ) ");
            }
            if (!param._isPrintRisyuTourokuNomi) {
                stb.append("      AND (");
                stb.append("         NOT (T1.COMP_CREDIT IS NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NULL) ");
                stb.append(classcd90Over);
                stb.append("    ) ");
            }
            if (param._hasStudyrefProvFlgDat) {
                stb.append("     AND L3.SUBCLASSCD IS NULL ");
            }
            return stb.toString();
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private String sqlReplaceSubClassSubstitution(final Param param, final String substitutionTypeFlg, final String schregno, final String sYear) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR, SYEAR) AS (VALUES(CAST('" + schregno + "' AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4)), CAST('" + sYear + "' AS VARCHAR(4))))");
            stb.append(" , DATA AS(");
            stb.append(pre_sql_Common(param));
            stb.append(" )");
            stb.append(" ,MAX_SEMESTER AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            stb.append("     ,T1.SCHOOL_KIND");
            stb.append("     ,T1.CURRICULUM_CD");
            stb.append("       ,VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD");
            stb.append("       ,T1.GRADES,T1.CREDIT,T1.COMP_CREDIT,T1.CLASSNAME,T1.SUBCLASSNAME ");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" WHERE  T1.CREDIT > 0 ");
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD, T1.STUDYREC_SUBCLASSCD");
            stb.append("     ,T1.SCHOOL_KIND ");
            stb.append("     ,T1.CURRICULUM_CD ");
            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append(" FROM DATA2 T1");
            stb.append(" INNER JOIN MAX_SEMESTER T2 ON T1.SCHREGNO = T2.SCHREGNO");
            stb.append("       AND T1.YEAR = T2.YEAR");
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD, T1.STUDYREC_SUBCLASSCD ");
            stb.append("     ,T1.SCHOOL_KIND ");
            stb.append("     ,T1.CURRICULUM_CD ");
            stb.append(" )");
            stb.append(" SELECT  T1.YEAR, T1.ANNUAL ");
            stb.append("       , T4.SUBSTITUTION_CLASSCD");
            stb.append("       , T4.SUBSTITUTION_SCHOOL_KIND");
            stb.append("       , T4.SUBSTITUTION_CURRICULUM_CD");
            stb.append("       , T4.SUBSTITUTION_SUBCLASSCD ");
            stb.append("       , SBSTCLM.CLASSNAME AS SUBSTITUTION_CLASSNAME");
            stb.append("       , VALUE(SBSTSUBM.SUBCLASSORDERNAME1, SBSTSUBM.SUBCLASSNAME) AS SUBSTITUTION_SUBCLASSNAME");
            stb.append("       , ATCREM.CREDITS AS SUBSTITUTION_CREDIT");
            stb.append("       , VALUE(SBSTCLM.SHOWORDER, -1) AS SHOWORDERCLASS"); // 表示順教科
            stb.append("       , VALUE(SBSTSUBM.SHOWORDER, -1) AS SHOWORDERSUBCLASS"); // 表示順科目
            stb.append("       , T1.SUBCLASSCD AS ATTEND_SUBCLASSCD"); // 代替元科目(表示する行のグループコード科目)
//          stb.append("       , T4.ATTEND_SUBCLASSCD"); // 代替元科目
            stb.append("       , T4.ATTEND_CLASSCD "); // 代替元科目
            stb.append("       , T4.ATTEND_SCHOOL_KIND "); // 代替元科目
            stb.append("       , T4.ATTEND_CURRICULUM_CD "); // 代替元科目
            stb.append("       , T4.ATTEND_SUBCLASSCD AS SRC_ATTEND_SUBCLASSCD"); // 代替元科目
            stb.append("       , ATCLM.CLASSNAME AS ATTEND_CLASSNAME");
            stb.append("       , VALUE(ATSUBM.SUBCLASSORDERNAME1, ATSUBM.SUBCLASSNAME) AS ATTEND_SUBCLASSNAME"); // 代替元科目
            stb.append("       , value(SBSTCLM.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            stb.append(" FROM   STUDYREC T1 ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_DAT T4 ON T1.YEAR = T4.YEAR ");
            stb.append("        AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("        AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T4.ATTEND_SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ");
            stb.append("        AND T4.SUBSTITUTION_TYPE_FLG = '" + substitutionTypeFlg + "' ");
            stb.append(" INNER JOIN MAX_SEMESTER SEM ON SEM.YEAR = T1.YEAR AND SEM.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = SEM.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T5 ON ");
            stb.append("            T5.YEAR = T4.YEAR AND T5.SUBSTITUTION_SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            stb.append("        AND T5.SUBSTITUTION_CLASSCD = T4.SUBSTITUTION_CLASSCD ");
            stb.append("        AND T5.SUBSTITUTION_SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
            stb.append("        AND T5.SUBSTITUTION_CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            stb.append("        AND T5.ATTEND_SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            stb.append("        AND T5.ATTEND_CLASSCD = T4.ATTEND_CLASSCD ");
            stb.append("        AND T5.ATTEND_SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
            stb.append("        AND T5.ATTEND_CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            stb.append("        AND T5.MAJORCD = REGD.MAJORCD AND T5.COURSECD = REGD.COURSECD ");
            stb.append("        AND T5.GRADE = REGD.GRADE AND T5.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN CREDIT_MST ATCREM ON ");
            stb.append("           ATCREM.CLASSCD = T4.ATTEND_CLASSCD ");
            stb.append("       AND ATCREM.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
            stb.append("       AND ATCREM.CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            stb.append("       AND ATCREM.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            stb.append("       AND ATCREM.YEAR = REGD.YEAR ");
            stb.append("       AND ATCREM.GRADE = REGD.GRADE ");
            stb.append("       AND ATCREM.COURSECD = REGD.COURSECD ");
            stb.append("       AND ATCREM.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND ATCREM.COURSECODE = REGD.COURSECODE ");
            if ("1".equals(param._seitoSidoYorokuDaitaiCheckMasterYear)) {
                stb.append(" INNER JOIN V_CLASS_MST SBSTCLM ON SBSTCLM.YEAR = T4.YEAR AND ");
            } else {
                stb.append(" LEFT JOIN CLASS_MST SBSTCLM ON ");
            }
            stb.append("           SBSTCLM.CLASSCD = T4.SUBSTITUTION_CLASSCD ");
            stb.append("       AND SBSTCLM.SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
            stb.append(" LEFT JOIN CLASS_MST ATCLM ON ");
            stb.append("       ATCLM.CLASSCD = T4.ATTEND_CLASSCD ");
            stb.append("       AND ATCLM.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
            if ("1".equals(param._seitoSidoYorokuDaitaiCheckMasterYear)) {
                stb.append(" INNER JOIN V_SUBCLASS_MST SBSTSUBM ON SBSTSUBM.YEAR = T4.YEAR AND ");
            } else {
                stb.append(" LEFT JOIN SUBCLASS_MST SBSTSUBM ON ");
            }
            stb.append("           SBSTSUBM.SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            stb.append("       AND SBSTSUBM.CLASSCD = T4.SUBSTITUTION_CLASSCD ");
            stb.append("       AND SBSTSUBM.SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
            stb.append("       AND SBSTSUBM.CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            if ("1".equals(param._seitoSidoYorokuDaitaiCheckMasterYear)) {
                stb.append(" INNER JOIN V_SUBCLASS_MST ATSUBM ON ATSUBM.YEAR = T4.YEAR AND ");
            } else {
                stb.append(" LEFT JOIN SUBCLASS_MST ATSUBM ON ");
            }
            stb.append("           ATSUBM.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            stb.append("       AND ATSUBM.CLASSCD = T4.ATTEND_CLASSCD ");
            stb.append("       AND ATSUBM.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
            stb.append("       AND ATSUBM.CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            stb.append(" WHERE  T4.SUBSTITUTION_CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("     OR T4.SUBSTITUTION_CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            stb.append(" ORDER BY T1.YEAR, T1.ANNUAL ");
            stb.append("       , T4.SUBSTITUTION_CLASSCD");
            stb.append("       , T4.SUBSTITUTION_SCHOOL_KIND");
            stb.append("       , T4.SUBSTITUTION_CURRICULUM_CD");
            stb.append("       , T4.SUBSTITUTION_SUBCLASSCD ");
            return stb.toString();
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private String sqlStudyrec(final Param param, final String schregno, final String sYear) {
            // 同一年度同一科目の場合単位は合計とします。
            //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
            String gradesCase = "case when 0 < T1.GRADES then GRADES end";
            String creditCase = "case when 0 < T1.GRADES then CREDIT end";

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR, SYEAR) AS (VALUES(CAST('" + schregno + "' AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4)), CAST('" + sYear + "' AS VARCHAR(4))))");
            stb.append(" , DATA AS(");
            stb.append(pre_sql_Common(param));
            stb.append(" )");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.SCHOOLCD,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            stb.append("       ,T1.SCHOOL_KIND");
            stb.append("       ,T1.CURRICULUM_CD");
            stb.append("       ,VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.GRADES, T1.CREDIT, T1.ADD_CREDIT, T1.COMP_CREDIT, T1.CLASSNAME, T1.SUBCLASSNAME ");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            stb.append("       ,T1.SCHOOL_KIND");
            stb.append("       ,T1.CURRICULUM_CD");
            stb.append("       ,T1.SUBCLASSCD");
            stb.append("       ,case when COUNT(*) = 1 then MAX(T1.GRADES) ");//１レコードの場合、評定はそのままの値。
            stb.append("            when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
            stb.append("            when SC.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+") * T1.CREDIT)) / SUM("+creditCase+"),0)");
            stb.append("            else MAX(T1.GRADES) end AS GRADES");
            stb.append("       ,SUM(T1.CREDIT) AS CREDIT");
            stb.append("       ,SUM(T1.ADD_CREDIT) AS ADD_CREDIT");
            stb.append("       ,SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       ,MIN(T1.SCHOOLCD) AS SCHOOLCD");
            stb.append(" FROM DATA2 T1 ");
            stb.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR ");
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            stb.append("       ,T1.SCHOOL_KIND");
            stb.append("       ,T1.CURRICULUM_CD");
            stb.append("          ,T1.SUBCLASSCD,SC.GVAL_CALC ");
            stb.append(" )");

            stb.append(" SELECT  T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            stb.append("       ,T1.SCHOOL_KIND");
            stb.append("       ,T1.CURRICULUM_CD");
            stb.append("       , T1.SUBCLASSCD");
            stb.append("       , VALUE(");
            if (param._hasAnotherClassMst) {
                stb.append("               CASE WHEN SCHOOLCD = '1' THEN ANT2.CLASSNAME END, ");
            }
            stb.append("               T1.CLASSNAME, T2.CLASSNAME) AS CLASSNAME");

            stb.append("       , VALUE(");
            if (param._hasAnotherSubclassMst) {
                stb.append("               CASE WHEN SCHOOLCD = '1' THEN ANT3.SUBCLASSORDERNAME1 END, ");
                stb.append("               CASE WHEN SCHOOLCD = '1' THEN ANT3.SUBCLASSNAME END, ");
            }
            stb.append("               T1.SUBCLASSNAME ");
            stb.append("              ,T3.SUBCLASSORDERNAME1 ");
            stb.append("              ,T3.SUBCLASSNAME ");
            stb.append("         ) AS SUBCLASSNAME");
            stb.append("       , T1.CREDIT");
            stb.append("       , T1.ADD_CREDIT");
            stb.append("       , T1.COMP_CREDIT");
            stb.append("       , T1.GRADES");
            stb.append("       , T1.SCHOOLCD");
            stb.append("       , CASE WHEN T2.SHOWORDER IS NOT NULL THEN T2.SHOWORDER ELSE -1 END AS SHOWORDERCLASS"); // 表示順教科
            stb.append("       , CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERSUBCLASS"); // 表示順科目
            stb.append("       , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            stb.append(" FROM   STUDYREC T1 ");
            stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("       AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            if (param._hasAnotherClassMst) {
                stb.append(" LEFT JOIN ANOTHER_CLASS_MST ANT2 ON ANT2.CLASSCD = T1.CLASSCD ");
                stb.append("       AND ANT2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            if (param._hasAnotherSubclassMst) {
                stb.append(" LEFT JOIN ANOTHER_SUBCLASS_MST ANT3 ON ANT3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("       AND ANT3.CLASSCD = T1.CLASSCD ");
                stb.append("       AND ANT3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND ANT3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }

            return stb.toString();
        }

        /**
         * @return 留学単位のＳＱＬ文を戻します。
         * @see 年度別の単位。(留年の仕様に対応)
         */
        private String sqlAbroadCredit(final Param param, final String schregno, final String sYear) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR, SYEAR) AS (VALUES(CAST('" + schregno + "' AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4)), CAST('" + sYear + "' AS VARCHAR(4))))");
            stb.append(" SELECT TRANSFER_YEAR AS YEAR, SUM(ABROAD_CREDITS) AS CREDIT ");
            stb.append(" FROM(");
            stb.append("      SELECT  ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
            stb.append("      FROM    SCHREG_TRANSFER_DAT W1 ");
            stb.append("      WHERE   EXISTS(SELECT 'X' FROM SCHBASE W2 WHERE W1.SCHREGNO = W2.SCHREGNO) ");
            stb.append("          AND TRANSFERCD = '1' ");
            stb.append("     )ST1 ");
            if (!param._seitoSidoYorokuZaisekiMae) {
                stb.append("     , (");
                stb.append("      SELECT  YEAR ");
                stb.append("      FROM    SCHREG_REGD_DAT W1 ");
                stb.append("      WHERE   EXISTS(SELECT 'X' FROM SCHBASE W2 WHERE W1.SCHREGNO = W2.SCHREGNO AND W2.SYEAR <= W1.YEAR AND W1.YEAR <= W2.YEAR) ");
                if (param.isGakunenSei() && param._isHeisetuKou && param._isHigh) {
                    stb.append("      AND W1.GRADE not in " + param._gradeInChugaku + " ");
                }
                stb.append("      GROUP BY YEAR ");
                stb.append("     )ST2 ");
                stb.append(" WHERE  INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
            }
            stb.append("GROUP BY TRANSFER_YEAR ");
            return stb.toString();
        }

        /**
         * @return 出欠の記録のＳＱＬ文を戻します。
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private String sqlAttendRec(final Param param, final String schregno, final String sYear) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  T1.YEAR, ANNUAL");
            stb.append("       , VALUE(CLASSDAYS,0) AS CLASSDAYS"); // 授業日数
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._definecode.schoolmark.substring(0, 1).equals("K")) {
                stb.append("              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            } else {
                stb.append("              THEN VALUE(CLASSDAYS,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            }
            stb.append("              END AS ATTEND_1"); // 授業日数 - (休学日数) [- 留学日数]
            stb.append("       , VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR"); // 出停・忌引
            stb.append("       , VALUE(SUSPEND,0) AS SUSPEND"); // 出停:2
            stb.append("       , VALUE(MOURNING,0) AS MOURNING"); // 忌引:3
            stb.append("       , VALUE(ABROAD,0) AS ABROAD"); // 留学:4
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("              THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append("              ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append("              END AS REQUIREPRESENT"); // 要出席日数:5
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("              THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append("              ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append("              END AS ATTEND_6"); // 病欠＋事故欠（届・無）:6
            stb.append("       , VALUE(PRESENT,0) AS PRESENT"); // 出席日数:7
            stb.append("       , VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8"); // 忌引＋出停:8
            stb.append(" FROM(");
            stb.append("      SELECT  YEAR, ANNUAL");
            stb.append("            , SUM(CLASSDAYS) AS CLASSDAYS");
            stb.append("            , SUM(OFFDAYS) AS OFFDAYS");
            stb.append("            , SUM(ABSENT) AS ABSENT");
            stb.append("            , SUM(SUSPEND) AS SUSPEND");
            stb.append("            , SUM(MOURNING) AS MOURNING");
            stb.append("            , SUM(ABROAD) AS ABROAD");
            stb.append("            , SUM(REQUIREPRESENT) AS REQUIREPRESENT");
            stb.append("            , SUM(SICK) AS SICK");
            stb.append("            , SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE");
            stb.append("            , SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE");
            stb.append("            , SUM(PRESENT) AS PRESENT");
            stb.append("       FROM   SCHREG_ATTENDREC_DAT");
            stb.append("       WHERE  SCHREGNO = '" + schregno + "' AND '" + sYear + "' <= YEAR AND YEAR <= '" + param._year + "' ");
            if (param.isGakunenSei() && param._isHeisetuKou && param._isHigh) {
                stb.append("      AND ANNUAL not in " + param._gradeInChugaku + " ");
            }
            if ("1".equals(param._seitoSidoYorokuNotPrintAnotherAttendrec)) {
                stb.append("      AND SCHOOLCD <> '1' ");
            }
            stb.append("       GROUP BY YEAR, ANNUAL");
            stb.append("     )T1 ");
            stb.append("     LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            return stb.toString();
        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         *  ・科目別修得単位数計 Student._studyRecSubclass。
         * </pre>
         */
        private Map<String, StudyRecSubclassTotal> createStudyRecTotal(final List<StudyRec> studyRecList, final Collection<String> dropYears, final Param param, final Set<String> yearSet) {
            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = new TreeMap();
            final Map<String, List<StudyRec>> subclassStudyrecListMap = new HashMap();
            if (param._isOutputDebug) {
                log.info(" yearSet = " + yearSet);
            }
            for (final StudyRec studyrec : studyRecList) {
                if (null != yearSet && !yearSet.contains(studyrec._year)) {
                    if (param._isOutputDebug) {
                        log.info(" skip year " + studyrec._year);
                    }
                    continue;
                }
                final String keycd = _90.equals(studyrec._classcd) ? _90 : getSubclasscd(studyrec, param);
                getMappedList(subclassStudyrecListMap, keycd).add(studyrec);
            }
            for (final String keycd : subclassStudyrecListMap.keySet()) {
                studyRecSubclassMap.put(keycd, new StudyRecSubclassTotal(getMappedList(subclassStudyrecListMap, keycd), dropYears));
            }
            return studyRecSubclassMap;
        }

        public Map<String, StudyRecSubclassTotal> getStudyRecSubclassMap(final Param param, final Set<String> yearSet) {
            return createStudyRecTotal(_studyRecList, _dropYears, param, yearSet);
        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         *  ・年度別修得単位数計 Student._studyRecYear。
         * </pre>
         */
        private Map<String, StudyRecYearTotal> createStudyRecYear(final List<StudyRec> studyRecList, final Collection<String> dropYears) {
            final Map<String, StudyRecYearTotal> studyRecYear = new HashMap<String, StudyRecYearTotal>();
            for (final StudyRec studyrec : studyRecList) {
                if (null != studyrec._credit) {
                    if (!studyRecYear.containsKey(studyrec._year)) {
                        studyRecYear.put(studyrec._year, new StudyRecYearTotal(studyrec._year, dropYears.contains(studyrec._year)));
                    }
                    final StudyRecYearTotal yeartotal = studyRecYear.get(studyrec._year);
                    yeartotal._studyRecList.add(studyrec);
                }
            }
            return studyRecYear;
        }

        /**
         * 出欠記録クラスを作成し、マップに加えます。
         * @param db2
         */
        private Map<String, AttendRec> loadAttendRec(final DB2UDB db2, final String schregno, final String sYear, final Param param) {
            final Map<String, AttendRec> attendRecMap = new HashMap<String, AttendRec>();
            final String sql = sqlAttendRec(param, schregno, sYear);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                attendRecMap.put(year, new AttendRec(year, KnjDbUtils.getString(row, "ANNUAL"), row));
            }
            return attendRecMap;
        }

        /**
         * 所見クラスを作成し、マップに加えます。
         */
        private Map<String, HtrainRemark> loadHtrainRemark(final DB2UDB db2, final String schregno, final Param param) {
            final Map<String, HtrainRemark> htrainRemarkMap = new HashMap<String, HtrainRemark>();
            final String sql = sqlHtrainRemark(schregno, param._year);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final String specialActRemark = KnjDbUtils.getString(row, "SPECIALACTREMARK");
                final String totalRemark = KnjDbUtils.getString(row, "TOTALREMARK");
                final String attendRecRemark = KnjDbUtils.getString(row, "ATTENDREC_REMARK");
                final String totalStudyAct = KnjDbUtils.getString(row, "TOTALSTUDYACT");
                final String totalStudyVal = KnjDbUtils.getString(row, "TOTALSTUDYVAL");

                final HtrainRemark htrainRemark = new HtrainRemark(
                        year,
                        annual,
                        specialActRemark,
                        totalRemark,
                        attendRecRemark,
                        totalStudyAct,
                        totalStudyVal
                );
                htrainRemarkMap.put(year, htrainRemark);
            }
            return htrainRemarkMap;
        }

        private String sqlHtrainRemark(final String schregno, final String year) {
            final String sql;
            sql = "SELECT  YEAR, ANNUAL, TOTALSTUDYACT, TOTALSTUDYVAL, SPECIALACTREMARK, TOTALREMARK, ATTENDREC_REMARK"
                + " FROM HTRAINREMARK_DAT"
                + " WHERE SCHREGNO = '" + schregno + "'"
                + " AND YEAR <= '" + year + "'"
                ;
            return sql;
        }

        private void loadHtrainRemarkdat(final DB2UDB db2, final String schregno, final Param param) {
            final String psKey = "PS_HTRAINREMARK_HDAT";
            if (!param._psMap.containsKey(psKey)) {
                final String sql = "select YEAR, TOTALSTUDYACT, TOTALSTUDYVAL"
                + " from HTRAINREMARK_DAT"
                + " where SCHREGNO = ? "
                + " AND YEAR <= '" + param._year + "'"
                + " ORDER BY YEAR"
                ;
                param.setPs(db2, psKey, sql);
            }

            _htrainRemarkHdatAct = new HashMap();
            _htrainRemarkHdatVal = new HashMap();

            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno})) {
                if (KnjDbUtils.getString(row, "TOTALSTUDYACT") != null) {
                    _htrainRemarkHdatAct.put(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "TOTALSTUDYACT"));
                }
                if (KnjDbUtils.getString(row, "TOTALSTUDYVAL") != null) {
                    _htrainRemarkHdatVal.put(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "TOTALSTUDYVAL"));
                }
            }
        }

        private List<String> loadAfterGraduatedCourse(final DB2UDB db2, final Param param, final String schregno) {

            final TreeSet<String> yearSet = gakusekiYearSet();
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            final String sql =
                // 進路用・就職用両方の最新の年度を取得
                "with TA as( select "
                + "         SCHREGNO, "
                + "         '0' as SCH_SENKOU_KIND, "
                + "         MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, "
                + "         '1' as COMP_SENKOU_KIND, "
                + "         MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR "
                + " from "
                + "         AFT_GRAD_COURSE_DAT "
                + " where "
                + "         SCHREGNO = ?  and PLANSTAT = '1'"
                + "         AND YEAR BETWEEN '" + minYear + "' AND '" + maxYear + "' "
                + " group by "
                + "         SCHREGNO "
                // 進路用・就職用どちらか(進路が優先)の最新の受験先種別コードを取得
                + "), TA2 as( select "
                + "     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, "
                + "     T1.SCHREGNO, "
                + "     T1.SENKOU_KIND, "
                + "     MAX(T1.SEQ) AS SEQ "
                + " from "
                + "     AFT_GRAD_COURSE_DAT T1 "
                + " inner join TA on "
                + "     T1.SCHREGNO = TA.SCHREGNO "
                + "     and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) "
                + "     and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) "
                + " where "
                + "     T1.PLANSTAT = '1'"
                + " group by "
                + "     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), "
                + "     T1.SCHREGNO, "
                + "     T1.SENKOU_KIND "
                + ") "
                // 最新の年度と受験先種別コードの感想を取得
                + "select  "
                + "      T1.SENKOU_KIND "
                + "     ,T1.STAT_CD "
                + "     ,T1.THINKEXAM "
                + "     ,T1.JOB_THINK "
                + "     ,L1.NAME1 as E017NAME1 "
                + "     ,L2.NAME1 as E018NAME1 "
                + "     ,L3.SCHOOL_NAME "
                + "     ,T1.FACULTYCD "
                + "     ,L5.FACULTYNAME "
                + "     ,T1.DEPARTMENTCD "
                + "     ,L6.DEPARTMENTNAME "
                + "     ,L7.ADDR1 AS CAMPUSADDR1 "
                + "     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 "
                + "     ,L4.COMPANY_NAME "
                + "     ,L4.ADDR1 AS COMPANYADDR1 "
                + "     ,L4.ADDR2 AS COMPANYADDR2 "
                + "     ,L9.ABBV3 AS E012ABBV3 "
                + "from "
                + "     AFT_GRAD_COURSE_DAT T1 "
                + "inner join TA2 on "
                + "     T1.YEAR = TA2.YEAR "
                + "     and T1.SCHREGNO = TA2.SCHREGNO "
                + "     and T1.SENKOU_KIND = TA2.SENKOU_KIND "
                + "     and T1.SEQ = TA2.SEQ "
                + "left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD "
                + "left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD "
                + "left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD "
                + "left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD "
                + "     and L5.FACULTYCD = T1.FACULTYCD "
                + "left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD "
                + "     and L6.FACULTYCD = T1.FACULTYCD "
                + "     and L6.DEPARTMENTCD = T1.DEPARTMENTCD "
                + "left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD "
                + "     and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD "
                + "left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD "
                + "     and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD "
                + "left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD "
                + "left join NAME_MST L9 on L9.NAMECD1 = 'E012' and L9.NAMECD2 = L3.SCHOOL_GROUP "
                + "where "
                + "     T1.PLANSTAT = '1' "
                + "order by "
                + "     T1.YEAR, T1.SCHREGNO "
                ;

            final List<String> afterGraduatedCourseTextList = new ArrayList<String>();
            // テーブルがあるか確認する。テーブルが無いなら後の処理を行わない。
            if (!param._hasAftGradCourseDat) {
                return afterGraduatedCourseTextList;
            }
//          log.debug(" schregno = " + schregno + " sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql, new Object[] { schregno })) {
                if ("0".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 進学
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E017NAME1")) {
                        afterGraduatedCourseTextList.addAll(getTokenList(KnjDbUtils.getString(row, "THINKEXAM"), 50, 10));
                    } else if ("1".equals(KnjDbUtils.getString(row, "E012ABBV3"))) {
                    } else {
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME")));
                        final String faculutyname = "000".equals(KnjDbUtils.getString(row, "FACULTYCD")) || null == KnjDbUtils.getString(row, "FACULTYNAME") ?  "" : KnjDbUtils.getString(row, "FACULTYNAME");
                        final String departmentname = "000".equals(KnjDbUtils.getString(row, "DEPARTMENTCD")) || null == KnjDbUtils.getString(row, "DEPARTMENTNAME") ? "" : KnjDbUtils.getString(row, "DEPARTMENTNAME");
                        afterGraduatedCourseTextList.add(faculutyname + departmentname);
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "CAMPUSFACULTYADDR1"), KnjDbUtils.getString(row, "CAMPUSADDR1")));
                    }
                } else if ("1".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 就職
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E018NAME1")) {
                        afterGraduatedCourseTextList.addAll(getTokenList(KnjDbUtils.getString(row, "JOB_THINK"), 50, 10));
                    } else {
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANY_NAME")));
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANYADDR1")));
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANYADDR2")));
                    }
                }
            }
            return afterGraduatedCourseTextList;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private List<Gakuseki> createGakusekiAttendRec(final DB2UDB db2, final List<Gakuseki> gakusekiList, final Map<String, AttendRec> attendRecMap, final Param param) {
            final Map<String, Gakuseki> map = new HashMap<String, Gakuseki>();
            for (final Gakuseki gakuseki : gakusekiList) {
                map.put(gakuseki._year, gakuseki);
            }

            for (final String year : attendRecMap.keySet()) {
                if (map.containsKey(year)) {
                    continue;
                }
                map.put(year, new Gakuseki(db2, attendRecMap.get(year), param));
            }

            final List<Gakuseki> list = new LinkedList<Gakuseki>(map.values());
            Collections.sort(list, new GakusekiComparator());
            return list;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiStudyRec(final DB2UDB db2, final List<Gakuseki> gakusekiList, final List<StudyRec> studyRecList, final Param param) {
            final Map<String, Gakuseki> map = new HashMap<String, Gakuseki>();
            for (final Gakuseki gakuseki : gakusekiList) {
                map.put(gakuseki._year, gakuseki);
            }

            for (final StudyRec studyrec : studyRecList) {
                if (map.containsKey(studyrec._year)) {
                    continue;
                }
                final Gakuseki gakuseki = new Gakuseki(db2, studyrec, param);
                map.put(studyrec._year, gakuseki);
            }

            final List<Gakuseki> list = new LinkedList<Gakuseki>(map.values());
            Collections.sort(list, new GakusekiComparator());
            return list;
        }

        /**
         * 学習記録備考クラスを作成し、マップに加えます。
         */
        private void createStudyRecBiko(final DB2UDB db2, final String schregno, final Param param, final GakushuBiko gakushuBiko) {
            final String psKey = "PS_STUDYREC_BIKO";
            if (!param._psMap.containsKey(psKey)) {
                final String sql = sqlStudyrecBiko(param);
                param.setPs(db2, psKey, sql);
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, param._year})) {
                if (KnjDbUtils.getString(row, "REMARK") == null) {
                    continue;
                }
                final String key = _90.equals(KnjDbUtils.getString(row, "CLASSCD")) ? _90 : getSubclasscd(row, param);
                gakushuBiko.putStudyrecBiko(key, KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "REMARK"));
            }
        }

        /**
         * 学習記録資格備考を作成し、備考データに加えます。
         */
        private void createStudyRecQualifiedBiko(final DB2UDB db2, final String schregno, final Param param, final GakushuBiko gakushuBiko) {
            final String psKey = "PS_QUALIFIED_BIKO";
            if (!param._psMap.containsKey(psKey)) {
                final String sql = sqlStudyrecQualifiedBiko(param);
                param.setPs(db2, psKey, sql);
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, schregno, param._year})) {
                if (KnjDbUtils.getString(row, "NAME1") == null) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD").startsWith(_90) ? _90 : getSubclasscd(row, param);
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final String sikakuName = KnjDbUtils.getString(row, "NAME1");
                final Integer credits = KnjDbUtils.getInt(row, "CREDITS", null);
                final Boolean isOldFormat = new Boolean(null == year || (Integer.parseInt(year) < 2013)); // 2013年度未満は古いフォーマット
                final Object[] d = new Object[]{year, sikakuName, credits, annual, isOldFormat};
                log.debug(" d = " + ArrayUtils.toString(d));
                gakushuBiko.addSikakuBiko(subclasscd, d);
            }
        }

        /**
         * 指定年度の教科コード90の代替科目備考を得る。
         * @param key 年度のキー
         * @return 備考の配列。なければnullを返す
         */
        private String[] getArraySubstitutionBiko90(final String key, final Param param) {
            final List list = new ArrayList();
            final Map yearSubstZenbu = (Map) _gakushuBiko.getStudyRecBikoSubstitution90(StudyrecSubstitutionContainer.ZENBU, _gakusekiList, _keyAll, param);
            final Map yearSubstIchibu = (Map) _gakushuBiko.getStudyRecBikoSubstitution90(StudyrecSubstitutionContainer.ICHIBU, _gakusekiList, _keyAll, param);
            final Set<String> yearSet = new TreeSet<String>();
            yearSet.addAll(yearSubstZenbu.keySet());
            yearSet.addAll(yearSubstIchibu.keySet());
            for (final String year : yearSet) {
                final Set<String> subclasscdSet = new TreeSet<String>();
                subclasscdSet.addAll(getMappedMap(yearSubstZenbu, year).keySet());
                subclasscdSet.addAll(getMappedMap(yearSubstIchibu, year).keySet());
                for (final String subclasscd : subclasscdSet) {
                    list.addAll(getMappedList(getMappedMap(yearSubstZenbu, year), subclasscd));
                    list.addAll(getMappedList(getMappedMap(yearSubstIchibu, year), subclasscd));
                }
            }
            final List rtn = new ArrayList();
            for (final Object o : list) {
                if (!rtn.contains(o)) {
                    rtn.add(o);
                }
            }
            return (String[]) rtn.toArray(new String[rtn.size()]);
        }


        /**
         * <pre>
         *  学年制で留年した生徒で改ページする場合ページごとの学籍のリストを得る
         * </pre>
         *
         * @param gakusekiList
         * @return ページごとの学籍のリスト
         */
        protected List<List<Gakuseki>> createPageGakusekiListList(final Param param, final List<Gakuseki> gakusekiList) {

            final List<List<Gakuseki>> pageGakusekiListList = new ArrayList<List<Gakuseki>>();
            List<Gakuseki> pageGakusekiList = null;
            if (param.isGakunenSei()) {
                for (final Gakuseki gakuseki : gakusekiList) {
                    if (null == pageGakusekiList) {
                        pageGakusekiList = new ArrayList<Gakuseki>();
                        pageGakusekiListList.add(pageGakusekiList);
                    }
                    pageGakusekiList.add(gakuseki);
                    if (gakuseki._isDrop) {
                        pageGakusekiList = null;
                    }
                }
            } else {
                pageGakusekiList = new ArrayList<Gakuseki>();
                pageGakusekiList.addAll(gakusekiList);
                pageGakusekiListList.add(pageGakusekiList);
            }
            return pageGakusekiListList;
        }


        /**
         * @return 学習の記録備考のＳＱＬ文を戻します。
         */
        private String sqlStudyrecBiko(final Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, T1.REMARK");
            stb.append(" FROM ");
            stb.append("     STUDYRECREMARK_DAT T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? AND T1.YEAR <= ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, T1.YEAR");
            return stb.toString();
        }

        /**
         * @return 学習の記録備考のＳＱＬ文を戻します。
         */
        private String sqlStudyrecQualifiedBiko(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH YEAR_ANNUAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         MAX(ANNUAL) AS ANNUAL ");
            stb.append("     FROM ");
            stb.append("         SCHREG_STUDYREC_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         SCHREGNO = ? ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T4.ANNUAL, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     VALUE(T3.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, T1.CONTENTS, T2.NAME1, T1.CREDITS ");
            stb.append(" FROM ");
            stb.append("     STUDYRECREMARK_QUALIFIED_DAT T1 ");
            stb.append("     INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'H305' ");
            stb.append("         AND T2.NAMECD2 = T1.CONTENTS ");
            stb.append("     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     LEFT JOIN YEAR_ANNUAL T4 ON T4.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? AND T1.YEAR <= ? ");
            stb.append("     AND T1.CONDITION_DIV = '1' ");
            stb.append("     AND T1.CREDITS IS NOT NULL ");
            stb.append("     AND T2.NAME1 IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     T1.REGDDATE, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, T1.SEQ ");
            return stb.toString();
        }

        /**
         * gradeRecNumberを設定します。
         */
        private int setGradeRecNumber(final Param param) {
            if (!param.isGakunenSei()) {
                return _gakusekiList.size();
            }

            int n = 0;
            for (final Iterator<Gakuseki> i = _gakusekiList.iterator(); i.hasNext();) {
                final Gakuseki gaku = i.next();
                if (!gaku._isDrop) {
                    n++;
                }
            }
            return n;
        }

        /**
         * @param student
         * @return 最終年度を戻します。
         */
        private String getLastYear() {
            for (final ListIterator<Gakuseki> it = _gakusekiList.listIterator(_gakusekiList.size()); it.hasPrevious();) {
                final Gakuseki gakuseki = it.previous();
                if (null != gakuseki) {
                    return gakuseki._year;
                }
                break;
            }
            return null;
        }

        /**
         * 生徒が転学・退学・卒業したか
         * @return 生徒が転学・退学・卒業したならtrue、そうでなければfalse
         */
        public boolean isGrd() {
            boolean rtn = false;
            for (final TransferRec tr : _transferRecList) {
                if ("A003".equals(tr._nameCode1)) {
                    final int namecd2 = Integer.parseInt(tr._nameCode2);
                    if (namecd2 == 3) { // 転学
                        rtn = true;
                    } else if (namecd2 == 2) { // 退学
                        rtn = true;
                    } else if (namecd2 == 1) { // 卒業
                        rtn = true;
                    }
                }
            }
            return rtn;
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒の学籍履歴クラス>>。
     */
    private static class Gakuseki implements Comparable<Gakuseki> {
        private final String _grade;
        private final String _gradeCd;
        private final String _gakunenSimple;
        private final String _gradeName2;
        private final String _nendo;
        private final String _hrname;
        private final String _attendno;
        private final String _year;
        private final String _hr_class;
        private final String _annual;
        private final Staff _principal;
        private final Staff _principal1;
        private final Staff _principal2;
        private final Staff _staff1;
        private final Staff _staff2;
        private final String _dataflg; // 在籍データから作成したか
        private boolean _isDrop;
        private String _staffSeq;
        private String _principalSeq;
        private String _kaizanFlg;

        /**
         * コンストラクタ。
         * @param rs
         * @param hmap
         * @param param
         */
        private Gakuseki(
                final String year,
                final String nendo,
                final String grade,
                final String gradeCd,
                final String gradeName2,
                final String hrclass,
                final String hrname,
                final String attendno,
                final String annual,
                final Staff principal,
                final Staff principal1,
                final Staff principal2,
                final Staff staff1,
                final Staff staff2,
                final String staffSeq,
                final String principalSeq,
                final String kaizanFlg
        ) {
            _year = year;
            _nendo = nendo;

            _grade = grade;
            _gradeCd = gradeCd;
            _gradeName2 = gradeName2;

            final String gakunen = !NumberUtils.isDigits(gradeCd) ? " " : String.valueOf(Integer.parseInt(gradeCd));
            _gakunenSimple = gakunen;
            _hr_class = hrclass;
            _hrname = hrname;
            _attendno = attendno;
            _annual = annual;
            _dataflg = "0".equals(_year) ? "2" : "1";
            _principal = principal;
            _principal1 = principal1;
            _principal2 = principal2;
            _staff1 = staff1;
            _staff2 = staff2;
            _staffSeq = staffSeq;
            _principalSeq = principalSeq;
            _kaizanFlg = kaizanFlg;
        }

        /**
         * コンストラクタ。
         */
        private Gakuseki(final DB2UDB db2, final AbstractAttendAndStudy studyrec, final Param param) {
            _year = studyrec._year;
            if (isNyugakumae()) {
                _nendo = "入学前";
            } else {
                _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(studyrec._year)) + "年度";
            }
            _grade = studyrec._annual;
            if (null != _grade) {
                if (0 >= Integer.parseInt(_grade)) {
                    _annual = null;
                    _gradeCd = null;
                    _gakunenSimple = null;
                    _gradeName2 = "入学前";
                    _dataflg = "2";
                } else {
                    _annual = studyrec._annual;
                    final String gradeCd = param.getGdatGradeCd(_year, _grade);
                    _gradeCd = null == gradeCd ? _grade : gradeCd;
                    _gakunenSimple = !NumberUtils.isDigits(gradeCd) ? " " : String.valueOf(Integer.parseInt(gradeCd));
                    if (null == param.getGdatGradeName2(_year, _annual)) {
                        _gradeName2 = "第" + Integer.parseInt(_annual) + "学年";
                    } else {
                        _gradeName2 = param.getGdatGradeName2(_year, _annual);
                    }
                    _dataflg = "1";
                }
            } else {
                _annual = null;
                _gradeCd = null;
                _gakunenSimple = null;
                _gradeName2 = null;
                _dataflg = "2";
            }
            _hr_class = null;
            _hrname = null;
            _attendno = null;

            _principal = Staff.Null;
            _principal1 = Staff.Null;
            _principal2 = Staff.Null;
            _staff1 = Staff.Null;
            _staff2 = Staff.Null;
        }

        private boolean isNyugakumae() {
            return "0".equals(_year);
        }

        private String[] nendoArray() {
            if (isNyugakumae() || _nendo == null || _nendo.length() < 4) {
                return new String[]{"", "", ""};
            }
            final String[] arNendo = new String[3];
            arNendo[0] = _nendo.substring(0, 2);
            arNendo[1] = _nendo.substring(2, _nendo.length() - 2);
            arNendo[2] = "年度";
            return arNendo;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Gakuseki that) {
            int rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return _year.hashCode() + _grade.hashCode() + _hr_class.hashCode() + _attendno.hashCode();
        }

        /**
         * @return 元号を除いた年度を戻します。
         */
        private String getNendo2() {
            if (isNyugakumae()) {
                return _nendo;
            } else {
                final String[] arNendo = nendoArray();
                return arNendo[1] + arNendo[2];
            }
        }

        public String toString() {
            return "[year = " +_year + " : grade = " + _grade + " : gradeCd = " + _gradeCd + " : nendo = "+ ArrayUtils.toString(nendoArray()) + " : hr_class = " + _hr_class + " : attendno = " + _attendno + "]";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフマスタ>>。
     */
    private static class StaffMst {
        /**pkg*/ static StaffMst Null = new StaffMst(null, null, null, null, null);
        final String _staffcd;
        final String _name;
        final String _kana;
        final String _nameReal;
        final String _kanaReal;
        private final Map _yearStaffNameSetUp;
        private final Map _yearStaffNameHist;
        public StaffMst(final String staffcd, final String name, final String kana, final String nameReal, final String kanaReal) {
            _staffcd = staffcd;
            _name = name;
            _kana = kana;
            _nameReal = nameReal;
            _kanaReal = kanaReal;
            _yearStaffNameSetUp = new HashMap();
            _yearStaffNameHist = new HashMap();
        }
        public boolean isPrintNameBoth(final String year) {
            final Map nameSetup = (Map) _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year) {
            final Map nameSetup = (Map) _yearStaffNameSetUp.get(year);
            return null != nameSetup;
        }

        public List<String> getNameLine(final String year) {
            final String[] nameLine;
            final String name = getYearHistDatValue(year, "STAFFNAME", _name);
            final String nameReal = getYearHistDatValue(year, "STAFFNAME_REAL", _nameReal);
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(nameReal)) {
                    nameLine = new String[]{name};
                } else {
                    if (StringUtils.isBlank(name)) {
                        nameLine = new String[]{nameReal};
                    } else {
                        final String n = "（" + name + "）";
                        if ((null == nameReal ? "" : nameReal).equals(name)) {
                            nameLine =  new String[]{nameReal};
                        } else if (getMS932ByteLength(nameReal + n) > 26) {
                            nameLine =  new String[]{nameReal, n};
                        } else {
                            nameLine =  new String[]{nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year)) {
                if (StringUtils.isBlank(nameReal)) {
                    nameLine = new String[]{name};
                } else {
                    nameLine = new String[]{nameReal};
                }
            } else {
                nameLine = new String[]{name};
            }
            return Arrays.asList(nameLine);
        }

        /**
         * 指定年度を含む履歴の名前フィールドの値を得る
         * @param yearSearch 指定年度
         * @param nameField 名前フィールド
         * @param defVal デフォルトの値
         * @return 指定年度を含む履歴の名前フィールドの値。値がnullならデフォルトの値
         */
        private String getYearHistDatValue(final String yearSearch, final String nameField, final String defVal) {
            final TreeMap sortedMap = new TreeMap(_yearStaffNameHist);
            Map histDat = null;
            for (final Iterator it = sortedMap.values().iterator(); it.hasNext();) {
                final Map histDat0 = (Map) it.next();
                final String syear = (String) histDat0.get("SYEAR");
                final String eyear = (String) histDat0.get("EYEAR");
                if (syear.compareTo(yearSearch) <= 0 && yearSearch.compareTo(eyear) <= 0) {
                    histDat = histDat0;
                }
            }
            String rtn;
            if (histDat == null) {
                // 履歴データがなければデフォルトの値
                rtn = defVal;
            } else {
                // 履歴データの指定フィールドの値がなければデフォルトの値
                rtn = StringUtils.isEmpty((String) histDat.get(nameField)) ? defVal : (String) histDat.get(nameField);
            }
            log.debug(" year search = " + yearSearch + ",  histDat = " + histDat + ", rtn = " + rtn);
            return rtn;
        }

        public static StaffMst get(final Map<String, StaffMst> staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return staffMstMap.get(staffcd);
        }

        public static Map<String, StaffMst> load(final DB2UDB db2, final Param param) {
            final Map<String, StaffMst> rtn = new HashMap<String, StaffMst>();

            final String sql1 = "SELECT * FROM STAFF_MST ";
            for (final Iterator it = KnjDbUtils.query(db2, sql1).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String staffcd = (String) m.get("STAFFCD");
                final String name = (String) m.get("STAFFNAME");
                final String kana = (String) m.get("STAFFNAME_KANA");
                final String nameReal = (String) m.get("STAFFNAME_REAL");
                final String kanaReal = (String) m.get("STAFFNAME_KANA_REAL");

                final StaffMst s = new StaffMst(staffcd, name, kana, nameReal, kanaReal);

                rtn.put(s._staffcd, s);
            }

            final String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT WHERE YEAR <= '" + param._year + "' AND DIV = '02' ";
            for (final Iterator it = KnjDbUtils.query(db2, sql2).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                if (null == rtn.get(m.get("STAFFCD"))) {
                    continue;
                }
                final StaffMst s = (StaffMst) rtn.get(m.get("STAFFCD"));

                final Map nameSetupDat = new HashMap();
                nameSetupDat.put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
                s._yearStaffNameSetUp.put(m.get("YEAR"), nameSetupDat);
            }

            if (!param._isKumamoto) {
                final StringBuffer sqlHist = new StringBuffer();
                sqlHist.append(" WITH MAX_SDATE AS ( ");
                sqlHist.append("SELECT ");
                sqlHist.append("   STAFFCD, MAX(SDATE) AS SDATE ");
                sqlHist.append(" FROM STAFF_NAME_HIST_DAT ");
                sqlHist.append(" WHERE ");
                sqlHist.append("   FISCALYEAR(SDATE) <= '" + param._year + "' ");
                sqlHist.append(" GROUP BY ");
                sqlHist.append("   STAFFCD, FISCALYEAR(SDATE) ");
                sqlHist.append(" )");
                sqlHist.append("SELECT ");
                sqlHist.append("  T1.STAFFCD, ");
                sqlHist.append("   FISCALYEAR(T1.SDATE) AS SYEAR, ");
                sqlHist.append("   FISCALYEAR(VALUE(T1.EDATE, '9999-12-31')) AS EYEAR, ");
                sqlHist.append("  T1.STAFFNAME, ");
                sqlHist.append("  T1.STAFFNAME_REAL ");
                sqlHist.append(" FROM STAFF_NAME_HIST_DAT T1 ");
                sqlHist.append(" INNER JOIN MAX_SDATE T2 ON T2.STAFFCD = T1.STAFFCD AND T2.SDATE = T1.SDATE ");
                for (final Map<String, String> m : KnjDbUtils.query(db2, sqlHist.toString())) {
                    if (null == rtn.get(m.get("STAFFCD"))) {
                        continue;
                    }
                    final StaffMst s = rtn.get(m.get("STAFFCD"));

                    final Map<String, String> nameHistDat = new HashMap<String, String>();
                    nameHistDat.put("STAFFCD", m.get("STAFFCD"));
                    nameHistDat.put("STAFFNAME", m.get("STAFFNAME"));
                    nameHistDat.put("STAFFNAME_REAL", m.get("STAFFNAME_REAL"));
                    nameHistDat.put("SYEAR", m.get("SYEAR"));
                    nameHistDat.put("EYEAR", m.get("EYEAR"));
                    s._yearStaffNameHist.put(m.get("SYEAR"), nameHistDat);
                }
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + ", nameSetupDat=" + _yearStaffNameSetUp + ", yearStaffNameHist = " + _yearStaffNameHist + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフクラス>>。
     */
    private static class Staff {
        /**pkg*/ static Staff Null = new Staff(null, StaffMst.Null, null, null, null);
        final String _year;
        final StaffMst _staffMst;
        final String _dateFrom;
        final String _dateTo;
        final String _stampNo;
        public Staff(final String year, final StaffMst staffMst, final String dateFrom, final String dateTo, final String stampNo) {
            _year = year;
            _staffMst = staffMst;
            _dateFrom = dateFrom;
            _dateTo = dateTo;
            _stampNo = stampNo;
        }

        public String getNameString() {
            final StringBuffer stb = new StringBuffer();
            final List name = _staffMst.getNameLine(_year);
            for (int i = 0; i < name.size(); i++) {
                if (null == name.get(i)) continue;
                stb.append(name.get(i));
            }
            return stb.toString();
        }

        public List getNameBetweenLine() {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between;
            if (StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate)) {
                between = "";
            } else if (!StringUtils.isBlank(jpMonthName(fromDate)) && jpMonthName(fromDate).equals(jpMonthName(toDate))) {
                between = "(" + jpMonthName(fromDate) + ")";
            } else {
                between = "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";
            }

            final List rtn;
            if (getMS932ByteLength(getNameString() + between) > 26) {
                rtn = Arrays.asList(new String[]{getNameString(), between});
            } else {
                rtn = Arrays.asList(new String[]{getNameString() + between});
            }
            return rtn;
        }

        private String toYearDate(final String date, final String year) {
            if (null == date) {
                return null;
            }
            final String sdate = year + "-04-01";
            final String edate = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
            if (date.compareTo(sdate) <= 0) {
                return sdate;
            } else if (date.compareTo(edate) >= 0) {
                return edate;
            }
            return date;
        }

        private String jpMonthName(final String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            return new java.text.SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
        }

        public String toString() {
            return "Staff(year=" + _year + ", staffMst=" + _staffMst + ", dateFrom=" + _dateFrom + ", dateTo=" + _dateTo + ", stampNo="+ _stampNo + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学校クラス>>。
     */
    private static class SchoolInfo {
        private String _schoolAddress1;
        private String _schoolAddress2;
        private String _schoolZipcode;
        private String _schoolName1;
        private String _bunkouSchoolAddress1;
        private String _bunkouSchoolAddress2;
        private String _bunkouSchoolName;
        /** 写真データ格納フォルダ */
        private String _imageDir;
        /** 写真データの拡張子 */
        private String _imageExt;

        /**
         * コンストラクタ。
         */
        SchoolInfo(final DB2UDB db2, final Param param) {
            loadSchool(db2, param);

//            final KNJ_Control.ReturnVal value = new KNJ_Control().Control(db2);
//            _imageDir = value.val4;
//            _imageExt = value.val5;
            _imageDir = "image/stamp";
            _imageExt = "bmp";
        }

        /**
         * 学校クラスを作成ます。
         */
        private void loadSchool(final DB2UDB db2, final Param param) {
            final Map paramMap = new HashMap();
            KNJ_SchoolinfoSql obj = new KNJ_SchoolinfoSql("10000");
            final Map<String, String> schoolinfoRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, obj.pre_sql(paramMap), new Object[] {param._year, param._year}));
            if (!schoolinfoRow.isEmpty()) {
                _schoolAddress1 = StringUtils.defaultString(KnjDbUtils.getString(schoolinfoRow, "SCHOOLADDR1"));
                _schoolAddress2 = StringUtils.defaultString(KnjDbUtils.getString(schoolinfoRow, "SCHOOLADDR2"));
                _schoolZipcode = KnjDbUtils.getString(schoolinfoRow, "SCHOOLZIPCD");
                _schoolName1 = KnjDbUtils.getString(schoolinfoRow, "SCHOOLNAME1");
            }

            if (true) { // param.isGakunenSei() && param._isChuKouIkkan) {

                final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR='" + param._year + "' AND CERTIF_KINDCD='" + CERTIF_KINDCD + "'";
                final String certifschoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));

                log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + certifschoolName + "]");

                if (null != certifschoolName && !StringUtils.isEmpty(certifschoolName)) {
                    _schoolName1 = certifschoolName;
                }
            }

            _bunkouSchoolAddress1 = "";
            _bunkouSchoolAddress2 = "";
            _bunkouSchoolName = "";
            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + param._year + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "' ";
            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if (!row.isEmpty()) {
                _bunkouSchoolAddress1 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK8"));
                _bunkouSchoolAddress2 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK9"));
                _bunkouSchoolName = KnjDbUtils.getString(row, "REMARK4");
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒情報クラス>>。
     */
    private static class PersonalInfo {
        final String _studentRealName;
        final String _studentName;
        final boolean _isPrintRealName;
        final boolean _isPrintNameAndRealName;
        /** 最も古い履歴の生徒名 */
        final String _studentNameHistFirst;
        final String _courseName;
        final String _majorName;
        final String _studentKana;
        final String _guardKana;
        final String _guardName;
        final String _guardNameHistFirst;
        final String _guarantorKana;
        final String _guarantorName;
        final String _guarantorNameHistFirst;
        final String _birthday;
        final String _birthdayStr;
        final String _sex;
        final String _finishDate;
        final String _installationDiv;
        final String _juniorSchoolName;
        final String _finschoolTypeName;
        final String _schAddress1;
        final String _schAddress2;

        /**
         * コンストラクタ。
         */
        private PersonalInfo(
                final String studentRealName,
                final String studentName,
                final boolean isPrintRealName,
                final boolean isPrintNameAndRealName,
                final String studentNameHistFirst,
                final String courseName,
                final String majorName,
                final String studentKana,
                final String guardKana,
                final String guardName,
                final String guardNameHistFirst,
                final String guarantorKana,
                final String guarantorName,
                final String guarantorNameHistFirst,
                final String birthday,
                final String birthdayStr,
                final String sex,
                final String finishDate,
                final String installationDiv,
                final String juniorSchoolName,
                final String finschoolTypeName,
                final String schAddress1,
                final String schAddress2
        ) {
            _studentRealName = studentRealName;
            _studentName = studentName;
            _isPrintRealName = isPrintRealName;
            _isPrintNameAndRealName = isPrintNameAndRealName;
            _studentNameHistFirst = studentNameHistFirst;

            _courseName = courseName;
            _majorName = majorName;
            _studentKana = studentKana;
            _guardKana = guardKana;
            _guardName = guardName;
            _guardNameHistFirst = guardNameHistFirst;
            _guarantorKana = guarantorKana;
            _guarantorName = guarantorName;
            _guarantorNameHistFirst = guarantorNameHistFirst;
            _birthday = birthday;
            _birthdayStr = birthdayStr;
            _sex = sex;
            _finishDate = finishDate;
            _installationDiv = installationDiv;
            _juniorSchoolName = juniorSchoolName;
            _finschoolTypeName = finschoolTypeName;
            _schAddress1 = schAddress1;
            _schAddress2 = schAddress2;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒住所履歴クラス>>。
     */
    private static class AddressRec {
        static final int SQL_SCHREG = 0;
        static final int SQL_GUARDIAN = 1;
        static final int SQL_GUARANTOR = 2;

        final int _idx;
        final String _issuedate;
        final String _address1;
        final String _address2;
        final String _zipCode;
        final boolean _isPrintAddr2;

        private AddressRec(final int idx, final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
            _idx = idx;
            _issuedate = issuedate;
            _address1 = addr1;
            _address2 = addr2;
            _zipCode = zip;
            _isPrintAddr2 = isPrintAddr2;
        }
        public String toString() {
            return "AddressRec(" + _idx + ":" + _issuedate + "," + _address1 + " " + _address2 + ")";
        }

        private static List<AddressRec> getPrintAddressRecList(final List<AddressRec> addressRecList, final int max) {
            final LinkedList<AddressRec> rtn = new LinkedList<AddressRec>();
            if (addressRecList.isEmpty()) {
                return rtn;
            }
            rtn.add(addressRecList.get(0));
            rtn.addAll(reverse(take(max - rtn.size(), reverse(drop(1, addressRecList)))));
            return rtn;
        }
        private static <T> List<T> take(final int count, final List<T> list) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (int i = 0; i < count && i < list.size(); i++) {
                rtn.add(list.get(i));
            }
            return rtn;
        }
        private static <T> List<T> drop(final int count, final List<T> list) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (int i = count; i < list.size(); i++) {
                rtn.add(list.get(i));
            }
            return rtn;
        }
        private static <T> List<T> reverse(final List<T> list) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (final ListIterator<T> it = list.listIterator(list.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }

        static boolean isSameAddressList(final List<AddressRec> addrListA, final List<AddressRec> addrListB) {
            boolean rtn = true;
            if (addrListA == null || addrListA.isEmpty() || addrListB == null || addrListB.isEmpty() || addrListA.size() != addrListB.size()) {
                rtn = false;
            } else {
                final int max = addrListA.size(); // == addrList2.size();
                for (int i = 0; i < max; i++) {
                    final AddressRec addressAi = addrListA.get(i);
                    final AddressRec addressBi = addrListB.get(i);
                    if (!isSameAddress(addressAi, addressBi)) {
                        rtn = false;
                        break;
                    }
                }
            }
            return rtn;
        }

        static boolean isSameAddress(final AddressRec addressAi, final AddressRec addressBi) {
            boolean rtn = true;
            if (null == addressAi || null == addressBi) {
                rtn = false;
            } else {
                if (null == addressAi._address1 && null == addressBi._address1) {
                } else if (null == addressAi._address1 || null == addressBi._address1 || !addressAi._address1.equals(addressBi._address1)) {
                    rtn = false;
                }
                if (null == addressAi._address2 && null == addressBi._address2) {
                } else if (!addressAi._isPrintAddr2 && !addressBi._isPrintAddr2) {
                } else if (null == addressAi._address2 || null == addressBi._address2 || !addressAi._address2.equals(addressBi._address2)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 住所履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<AddressRec> loadAddressRec(final DB2UDB db2, final String schregno, final Param param, final int sqlflg) {
            final String psKey = "PS_ADDRESS" + sqlflg;
            if (!param._psMap.containsKey(psKey)) {
                final String sql = sqlAddressDat(sqlflg);
                param.setPs(db2, psKey, sql);
            }
            final List<AddressRec> addressRecList = new LinkedList<AddressRec>();
            int idx = 0;
            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, param._year})) {
                final String issuedate = KnjDbUtils.getString(row, "ISSUEDATE");
                final String address1 = KnjDbUtils.getString(row, "ADDR1");
                final String address2 = KnjDbUtils.getString(row, "ADDR2");
                final String zipCode = KnjDbUtils.getString(row, "ZIPCD");
                final boolean isPrintAddr2 = "1".equals(KnjDbUtils.getString(row, "ADDR_FLG"));
                final AddressRec addressRec = new AddressRec(idx, issuedate, address1, address2, zipCode, isPrintAddr2);
                addressRecList.add(addressRec);
                idx += 1;
            }
            return addressRecList;
        }

        /**
         * 住所のSQLを得る
         * @param sqlflg 0:生徒住所, 1:保護者住所, 2:保証人住所
         * @return
         */
        private static String sqlAddressDat(final int sqlflg) {

            StringBuffer stb = new StringBuffer();
            if (AddressRec.SQL_SCHREG == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            } else if (AddressRec.SQL_GUARDIAN == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else if (AddressRec.SQL_GUARANTOR == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARANTOR_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARANTOR_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARANTOR_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARANTOR_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARANTOR_ADDRESS_DAT T1  ");
            }
            stb.append("INNER JOIN (SELECT SCHREGNO, ENT_DATE, VALUE(GRD_DATE, '9999-12-31') AS GRD_DATE  ");
            stb.append("            FROM SCHREG_ENT_GRD_HIST_DAT ");
            stb.append("            WHERE SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("           ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("       T1.SCHREGNO = ?  ");
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= ?  ");
            stb.append("       AND (ENT_DATE BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ENT_DATE <= ISSUEDATE)  ");
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒異動情報クラス>>。
     */
    private static class TransferRec {
        private final String _nameCode1;
        private final String _nameCode2;
        private final String _name;
        private final String _sYear;
        private final String _sDate;
        private final String _sDateStr;
        private final String _eDate;
        private final String _eDateStr;
        private final String _grade;
        private final String _gradeCd;
        private final String _reason;
        private final String _place;
        private final String _address;
        private final String _address2;
        private final String _certifno;

        public TransferRec(
                final String nameCode1,
                final String nameCode2,
                final String name,
                final String sYear,
                final String sDate,
                final String sDateStr,
                final String eDate,
                final String eDateStr,
                final String grade,
                final String gradeCd,
                final String reason,
                final String place,
                final String address,
                final String address2,
                final String certifno) {
            _nameCode1 = nameCode1;
            _nameCode2 = nameCode2;
            _name = name;
            _sYear = sYear;
            _sDate = sDate;
            _sDateStr = sDateStr;
            _eDate = eDate;
            _eDateStr = eDateStr;
            _grade = grade;
            _gradeCd = gradeCd;
            _reason = reason;
            _place = place;
            _address = address;
            _address2 = address2;
            _certifno = certifno;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static abstract class AbstractAttendAndStudy {
        protected final String _year;
        protected final String _annual;
        AbstractAttendAndStudy(final String year, final String annual) {
            _year = year;
            _annual = annual;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class AttendRec extends AbstractAttendAndStudy implements Comparable<AttendRec> {
        private Integer _attend_1; // 授業日数
        private Integer _suspendMourning; // 出停 + 忌引
        private Integer _abroad; // 留学
        private Integer _requirepresent; // 要出席
        private Integer _attend_6; // 欠席
        private Integer _present; // 出席

        /**
         * コンストラクタ。
         */
        private AttendRec(final String year, final String annual, final Map<String, String> row) {
            super(year, annual);
            _attend_1 = KnjDbUtils.getInt(row, "ATTEND_1", null);
            _suspendMourning = KnjDbUtils.getInt(row, "SUSP_MOUR", null);
            _abroad = KnjDbUtils.getInt(row, "ABROAD", null);
            _requirepresent = KnjDbUtils.getInt(row, "REQUIREPRESENT", null);
            _attend_6 = KnjDbUtils.getInt(row, "ATTEND_6", null);
            _present = KnjDbUtils.getInt(row, "PRESENT", null);
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final AttendRec that) {
            int rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRec extends AbstractAttendAndStudy implements Comparable<StudyRec> {
        private final String _classcd;
        private final String _schoolKind;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final String _classname;
        private final String _subclassname;
        private Integer _grades;
        private final Integer _credit;
        private final Integer _compCredit;
        private Integer _addCredit;
        private final Integer _showorderClass;
        private final Integer _showorderSubClass;
        private final String _specialDiv;

        private static enum KIND {
            SOGO90, SOGO90COMP, SYOKEI, ABROAD, TOTAL,
            JIRITSU; // 自立活動
        }

        /**
         * コンストラクタ。
         */
        private StudyRec(final String year, final String annual, final String classcd,
                final String schoolKind, final String curriculumCd, final String subclasscd,
                final String classname, final String subclassname, final String specialDiv, final Integer credit,
                final Integer addCredit, final Integer compCredit,
                final Double grades, final Integer showorderClass, final Integer showorderSubClass) {
            super(year, annual);
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _credit = credit;
            _compCredit = compCredit;
            _addCredit = addCredit;
            setGrades(grades);
            _showorderClass = showorderClass;
            _showorderSubClass = showorderSubClass;
            _specialDiv = specialDiv;
        }

        /**
         * コンストラクタ。留学。
         */
        private StudyRec(final String year, final String annual, final Integer credit, final boolean abroad) {
            super(year, annual);
            _classcd = "AA";
            _schoolKind = "AA";
            _curriculumCd = "AA";
            _subclasscd = "AAAAAA";
            _classname = _ABROAD;
            _subclassname = _ABROAD;
            _showorderClass = new Integer(0);
            _showorderSubClass = new Integer(0);
            _specialDiv = "0";
            _credit = credit;
            _compCredit = null;
        }

        public String getSubClassName() {
            return _subclassname;
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        public boolean isRisyuNomi(final Param param) {
            // 修得単位数が0 かつ 履修単位数が1以上 かつ 評定が1
            return 0 == intVal(_credit, -1) && 1 <= intVal(_compCredit, -1) && 1 == intVal(_grades, -1);
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        public boolean isMirisyu(final Param param) {
            // 修得単位数が0 かつ 履修単位数が0
            return 0 == intVal(_credit, -1) && 0 == intVal(_compCredit, -1);
        }

        /**
         * 履修登録のみか
         * @return 履修登録のみならtrue
         */
        public boolean isRisyuTourokunomi(final Param param) {
            // 修得単位数がnull かつ 履修単位数がnull
            return -1 == intVal(_credit, -1) && -1 == intVal(_compCredit, -1);
        }

        private static int intVal(final Number n, final int def) {
            return null == n ? def : n.intValue();
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final StudyRec that) {
            int rtn;
            rtn = _specialDiv.compareTo(that._specialDiv);
            if (0 != rtn)
                return rtn;
            rtn = _showorderClass.compareTo(that._showorderClass);
            if (0 != rtn)
                return rtn;
            rtn = _classcd.compareTo(that._classcd);
            if (0 != rtn)
                return rtn;
            if (null != _schoolKind && null != that._schoolKind) {
                rtn = _schoolKind.compareTo(that._schoolKind);
                if (0 != rtn)
                    return rtn;
            }
            if (null != _curriculumCd && null != that._curriculumCd) {
                rtn = _curriculumCd.compareTo(that._curriculumCd);
                if (0 != rtn)
                    return rtn;
            }
            rtn = _showorderSubClass.compareTo(that._showorderSubClass);
            if (0 != rtn)
                return rtn;
            rtn = _subclasscd.compareTo(that._subclasscd);
            if (0 != rtn)
                return rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }

        /**
         * @param grades 設定する grades。
         */
        private void setGrades(final Double grades) {
            if (null == grades) {
                return;
            }
            _grades = new Integer((int) (float) Math.round(grades.floatValue()));
        }


        public String toString() {
            return "Studyrec(classcd=" + _classcd + ":subClasscd=" + _subclasscd + ":subClassName=" + _subclassname + ":year=" + _year + ":annual=" + _annual + ":credit=" + _credit + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データ科目別単位数のクラス>>。 学習記録データクラスを科目別に集計しました。
     */
    private static class StudyRecSubclassTotal implements Comparable<StudyRecSubclassTotal> {

        private final List<StudyRec> _subclassStudyrecList;
        private final Collection<String> _dropYears;
        private Integer _compCredit;

        /**
         * コンストラクタ。
         *
         * @param rs
         */
        private StudyRecSubclassTotal(final List<StudyRec> subclassStudyrecList, final Collection<String> dropYears) {
            _subclassStudyrecList = subclassStudyrecList;
            _dropYears = dropYears;
        }

        public Integer credit() {
            List<Integer> creditList = new ArrayList<Integer>();
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!_dropYears.contains(sr._year) && null != sr._credit) {
                    creditList.add(sr._credit);
                }
            }
            return sum(creditList);
        }


        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        private boolean isRisyuNomi(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!sr.isRisyuNomi(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        private boolean isMirisyu(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!sr.isMirisyu(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }


        /**
         * 履修登録のみか
         * @return 履修登録のみならtrue
         */
        private boolean isRisyuTourokunomi(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _subclassStudyrecList) {
                if (!sr.isRisyuTourokunomi(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        private StudyRec studyrec() {
            return _subclassStudyrecList.get(0);
        }

        public String specialDiv() {
            return studyrec()._specialDiv;
        }

        public String classcd() {
            return studyrec()._classcd;
        }

        public String schoolKind() {
            return studyrec()._schoolKind;
        }

        public String curriculumCd() {
            return studyrec()._curriculumCd;
        }

        public String className() {
            return studyrec()._classname;
        }

        public String subClassName() {
            return studyrec()._subclassname;
        }

        public String subClasscd() {
            return studyrec()._subclasscd;
        }

        public boolean isAllDropped() {
            boolean isAllDropped = true;
            for (final StudyRec studyRec : _subclassStudyrecList) {
                if (!_dropYears.contains(studyRec._year)) {
                    isAllDropped = false;
                }
            }
            return isAllDropped;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final StudyRecSubclassTotal that) {
            int rtn;
            rtn = specialDiv().compareTo(that.specialDiv());
            if (0 != rtn)
                return rtn;
            rtn = studyrec()._showorderClass.compareTo(that.studyrec()._showorderClass);
            if (0 != rtn)
                return rtn;
            rtn = classcd().compareTo(that.classcd());
            if (0 != rtn)
                return rtn;
            if (null != schoolKind() && null != that.schoolKind()) {
                rtn = schoolKind().compareTo(that.schoolKind());
                if (0 != rtn)
                    return rtn;
            }
            if (null != curriculumCd() && null != that.curriculumCd()) {
                rtn = curriculumCd().compareTo(that.curriculumCd());
                if (0 != rtn)
                    return rtn;
            }
            rtn = studyrec()._showorderSubClass.compareTo(that.studyrec()._showorderSubClass);
            if (0 != rtn)
                return rtn;
            rtn = subClasscd().compareTo(that.subClasscd());
            return rtn;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "[" + className() + ":" + subClassName() + "]";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <pre>
     * 学習記録データ年度別単位数のクラス
     *  学習記録データクラスを年度別に集計します。
     *  メンバ変数について
     *    _year 年度
     *    _subject90：総合的な学習の時間
     *    _abroad：留学
     *    _subject：教科(総合的な学習の時間を含めた)
     *    _total：総合計（教科＋留学）
     * </pre>
     */
    private static class StudyRecYearTotal {
        private String _year;
        private boolean _isDrop;

        final List<StudyRec> _studyRecList = new ArrayList<StudyRec>();

        /**
         * コンストラクタ。
         */
        private StudyRecYearTotal(final String year, final boolean isDrop) {
            _year = year;
            _isDrop = isDrop;
        }

        public static Map<StudyRec.KIND, List<Integer>> getKindCreditListMap(final Param param, final Map<String, StudyRecYearTotal> studyrecyear, final Set<String> allYearSet, final StudyRec.KIND[] kindList, final StudyRec.KIND[] optKindList) {

            final Map<StudyRec.KIND, List<Integer>> kindCreditMap = new TreeMap<StudyRec.KIND, List<Integer>>();
            for (final StudyRec.KIND kind : kindList) {
                kindCreditMap.put(kind, new ArrayList<Integer>());
            }
            for (final StudyRecYearTotal yearTotal : studyrecyear.values()) {
                if (yearTotal._isDrop || !allYearSet.contains(yearTotal._year)) {
                    continue;
                }
                for (final StudyRec.KIND kind : kindList) {
                    kindCreditMap.get(kind).addAll(yearTotal.getKindCreditList(param, kind));
                }
                if (null != optKindList) {
                    for (final StudyRec.KIND kind : optKindList) {
                        final List<Integer> optsCredit = yearTotal.getKindCreditList(param, kind);
                        if (!optsCredit.isEmpty()) {
                            getMappedList(kindCreditMap, kind).addAll(optsCredit);
                        }
                    }
                }
            }
            return kindCreditMap;
        }

        public List<Integer> getKindCreditList(final Param param, final StudyRec.KIND kind) {
            switch (kind) {
                case SOGO90:
                {
                    final List<Integer> list = new ArrayList<Integer>();
                    for (final StudyRec sr : _studyRecList) {
                        final Integer credit = sr._credit;
                        if (null == credit) {
                            continue;
                        }
                        if (_ABROAD.equals(sr._classname)) {
                            continue;
                        }
                        if (_90.equals(sr._classcd)) {
                            list.add(credit);
                        }
                    }
                    return list;
                }
                case SOGO90COMP:
                {
                    final List<Integer> list = new ArrayList<Integer>();
                    for (final StudyRec sr : _studyRecList) {
                        final Integer credit = sr._compCredit;
                        if (null == credit) {
                            continue;
                        }
                        if (_ABROAD.equals(sr._classname)) {
                            continue;
                        }
                        if (_90.equals(sr._classcd)) {
                            list.add(credit);
                        }
                    }
                    return list;
                }
                case SYOKEI:
                {
                    final List<Integer> list = new ArrayList<Integer>();
                    for (final StudyRec sr : _studyRecList) {
                        if (null == sr._credit) {
                            continue;
                        }
                        if (_ABROAD.equals(sr._classname)) {
                            continue;
                        }
                        list.add(sr._credit);
                    }
                    return list;
                }
                case ABROAD:
                {
                    final List<Integer> list = new ArrayList<Integer>();
                    for (final StudyRec sr : _studyRecList) {
                        if (null == sr._credit) {
                            continue;
                        }
                        if (_ABROAD.equals(sr._classname)) {
                            list.add(sr._credit);
                        }
                    }
                    return list;
                }
                case TOTAL:
                {
                    final List<Integer> list = new ArrayList<Integer>();
                    for (final StudyRec sr : _studyRecList) {
                        if (null == sr._credit) {
                            continue;
                        }
                        list.add(sr._credit);
                    }
                    return list;
                }
                case JIRITSU:
                {
                    final List<Integer> list = new ArrayList<Integer>();
                    for (final StudyRec sr : _studyRecList) {
                        if (null == sr._credit) {
                            continue;
                        }
                        final String subclasscd = getSubclasscd(sr, param);
                        if (param._e065Name1JiritsuKatsudouSubclasscdList.contains(subclasscd)) {
                            list.add(sr._credit);
                        }
                    }
                    return list;
                }
                default:
                    log.warn(" no process : " + kind);
            }
            return null;
        }
    }


    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRecSubstitution extends StudyRec {

        private List<AttendSubclass> attendSubclasses = new ArrayList<AttendSubclass>();

        /**
         * コンストラクタ。
         */
        private StudyRecSubstitution(final String year, final String annual, final String classcd,
                final String schoolKind, final String curriculumCd, final String subclasscd,
                final String classname, final String subclassname, final String specialDiv, final Integer credit,
                final Double grades, final Integer showorderClass, final Integer showorderSubClass) {
            super(year, annual, classcd, schoolKind, curriculumCd, subclasscd, classname, subclassname, specialDiv, credit, null,
                    null,
                    grades, showorderClass, showorderSubClass);
        }

        public Set<String> getSubstitutionYears(final Param param) {
            final Set<String> years = new TreeSet<String>();
            for (final AttendSubclass attendSubclass : attendSubclasses) {
                years.add(attendSubclass._attendyear);
            }
            return years;
        }

        /**
         * 「活動の記録用」の代替科目備考文字列を得る。
         * @return
         */
        public String getBikoSubstitution90(final String year, final String substitutionTypeFlg, final Param param) {
            final Set<String> addedNames = new HashSet<String>();

            final StringBuffer attendSubClassNames = new StringBuffer();
            String sp = "";

            for (final AttendSubclass attendSubclasss : attendSubclasses) {

                final String attendSubClassName = StringUtils.defaultString(attendSubclasss._attendClassName) + "・" + StringUtils.defaultString(attendSubclasss._attendSubclassName);

                if (!addedNames.contains(attendSubClassName) && (year == null || year.equals(attendSubclasss._attendyear))) {
                    attendSubClassNames.append(sp + attendSubClassName);
                    sp = "、";
                    addedNames.add(attendSubClassName);
                }
            }
            if (attendSubClassNames.length() != 0) {
                return StringUtils.defaultString(getSubClassName()) + "は" + attendSubClassNames.toString() + "で" + "代替";
            }
            return "";
        }

        /**
         * 履修科目の最大年度を得る
         * @return 履修科目の最大年度
         */
        public String getMaxAttendSubclassYear() {
            String maxyear = null;
            for (final AttendSubclass array : attendSubclasses) {
                if (maxyear == null || array._attendyear != null && maxyear.compareTo(array._attendyear) < 0) {
                    maxyear = array._attendyear;
                }
            }
            return maxyear;
        }

        private static class AttendSubclass {
            final String _attendyear;
            final String _attendClassCd;
            final String _attendSchoolKind;
            final String _attendCurriculumCd;
            final String _attendSubClassCd;
            final String _attendClassName;
            final String _attendSubclassName;
            final String _substitutionCredit;
            public AttendSubclass(final String attendyear, final String attendClassCd, final String attendSchoolKind, final String attendCurriculumCd, final String attendSubclassCd, final String attendClassName, final String attendSubclassName, final String substitutionCredit) {
                _attendyear = attendyear;
                _attendClassCd = attendClassCd;
                _attendSchoolKind = attendSchoolKind;
                _attendCurriculumCd = attendCurriculumCd;
                _attendSubClassCd = attendSubclassCd;
                _attendClassName = attendClassName;
                _attendSubclassName = attendSubclassName;
                _substitutionCredit = substitutionCredit;
            }
            public String toString() {
                return "Attend(" + _attendyear + ", " + _attendSubClassCd + ", " + _attendSubclassName + ", " + _substitutionCredit + ")";
            }
        }

        public String toString() {
            return "Subst(" + super.toString() + ", attends = " + attendSubclasses + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static class StudyrecSubstitutionContainer {

        static final String ZENBU = "1";  // 代替フラグ:全部
        static final String ICHIBU = "2";  // 代替フラグ:一部

        static final List<String> TYPE_FLG_LIST = Arrays.asList(StudyrecSubstitutionContainer.ZENBU, StudyrecSubstitutionContainer.ICHIBU);

        private final Map studyRecSubstitutions = new HashMap();

        public Map<String, Map<String, StudyRecSubstitution>> getStudyrecSubstitution(final String substitutionTypeFlg) {
            return getMappedMap(studyRecSubstitutions, substitutionTypeFlg);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class HtrainRemark implements Comparable<HtrainRemark> {
        static HtrainRemark Null = new HtrainRemark(null, null, null, null, null, null, null);

        private final String _year;
        private final String _annual;
        /** 特別活動 */
        private final String _specialActRemark;
        /** 所見 */
        private final String _totalRemark;
        /** 出欠備考 */
        private final String _attendRecRemark;
        /** 総合的な学習の時間学習活動 */
        private final String _totalStudyAct;
        /** 総合的な学習の時間評価 */
        private final String _totalStudyVal;

        private HtrainRemark(
                final String year,
                final String annual,
                final String specialActRemark,
                final String totalRemark,
                final String attendRecRemark,
                final String totalStudyAct,
                final String totalStudyVal
        ) {
            _year = year;
            _annual = annual;
            _specialActRemark = specialActRemark;
            _totalRemark = totalRemark;
            _attendRecRemark = attendRecRemark;
            _totalStudyAct = totalStudyAct;
            _totalStudyVal = totalStudyVal;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final HtrainRemark that) {
            return _year.compareTo(that._year);
        }

        public String toString() {
            return "year=" + _year + ", totalStudyAct=[" + _totalStudyAct + "]";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<Gakusekiクラスのソート>>。
     */
    private static class GakusekiComparator implements Comparator<Gakuseki> {
        public int compare(Gakuseki g1, Gakuseki g2) {
            return g1._year.compareTo(g2._year);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習備考>>。
     */
    private static class GakushuBiko {

        private Map _risyuTannibiko = new HashMap();
        private Map _studyrecbiko = new HashMap();
        private Map<String, List<Object[]>> _sikakubiko = new HashMap<String, List<Object[]>>();
        private Map<String, Map<String, Map<String, String>>> _studyrecbikoSubstitution = new HashMap<String, Map<String, Map<String, String>>>();

        private final StudyrecSubstitutionContainer _ssc = new StudyrecSubstitutionContainer();

        private Map getSubclassRisyuTanniBikoMap(final String subclasscd) {
            return getMappedMap(_risyuTannibiko, subclasscd);
        }

        /**
         * 科目の年度の学習記録履修単位備考をセットする。
         * @param subclasscd 科目コード
         * @param year 年度
         * @param rishuTanniBiko 履修単位備考
         */
        public void putRisyuTanniBiko(final String subclasscd, final String year, final String rishuTanniBiko) {
            getSubclassRisyuTanniBikoMap(subclasscd).put(year, rishuTanniBiko);
        }

        private Map getStudyrecBikoMap(final String subclasscd) {
            return getMappedMap(_studyrecbiko, subclasscd);
        }

        private List<Object[]> getSikakuBikoList(final String subclasscd) {
            return getMappedList(_sikakubiko, subclasscd);
        }

        /**
         * 科目の年度の学習記録備考をセットする。
         * @param subclasscd 科目コード
         * @param year 年度
         * @param studyrecBiko 学習記録備考
         */
        public void putStudyrecBiko(final String subclasscd, final String year, final String studyrecBiko) {
            getStudyrecBikoMap(subclasscd).put(year, studyrecBiko);
        }

        public void addSikakuBiko(final String subclassCd, final Object[] d) {
            getSikakuBikoList(subclassCd).add(d);
        }

        private Map<String, String> getStudyrecSubstitutionBikoMap(final String subclasscd, final String substitutionTypeFlg) {
            return getMappedMap(getMappedMap(_studyrecbikoSubstitution, subclasscd), substitutionTypeFlg);
        }

        /**
         * 科目の年度の学習記録代替科目備考をセットする。
         * @param subclasscd 科目コード
         * @param year 年度
         * @param studyrecSubstitutionBiko 学習記録代替科目備考
         */
        public void putStudyrecSubstitutionBiko(final String subclasscd, final String substitutionTypeFlg, final String year, final String studyrecSubstitutionBiko) {
            getStudyrecSubstitutionBikoMap(subclasscd, substitutionTypeFlg).put(year, studyrecSubstitutionBiko);
        }

        /**
         * 最小年度から最大年度までの備考の連結文字列を得る。
         * @param map 年度をキーとする備考のマップ
         * @param spStr 区切り文字
         * @return 最小年度から最大年度までの備考の連結文字列
         */
        private StringBuffer getBiko(final Map<String, String> map, final PrintGakuseki printGakuseki, final String spStr) {
            String minYear = null;
            String maxYear = null;
            if (null != printGakuseki) {
                final TreeSet<String> yearSet = new TreeSet<String>(printGakuseki._yearPositionMap.keySet());
                minYear = yearSet.first();
                maxYear = yearSet.last();
            }

            final List<String> bikoList = new ArrayList<String>();
            for (final String year : map.keySet()) {
                final String biko = map.get(year);
                if (inArea(year, minYear, maxYear) && biko.length() != 0) {
                    bikoList.add(biko);
                }
            }
            return mkStringUnique(bikoList, spStr);
        }

        /**
         * 最小年度から最大年度までの備考のリストを得る。
         * @param map 年度をもつ備考のリスト
         * @param yearMin 最小年度
         * @param yearMax 最大年度
         * @return 最小年度から最大年度までの備考のリスト
         */
        private List<Object[]> getBikoList(final List<Object[]> list, final PrintGakuseki printGakuseki) {
            final TreeSet<String> yearSet = new TreeSet<String>(printGakuseki._yearPositionMap.keySet());
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            final List<Object[]> rtn = new ArrayList<Object[]>();
            for (final Object[] d : list) {
                final String year = (String) d[0];
                if (inArea(year, minYear, maxYear)) {
                    rtn.add(d);
                }
            }
            return rtn;
        }

        private boolean inArea(final Comparable o, final Comparable a, final Comparable b) {
            return (a == null || a.compareTo(o) <= 0) && (b == null || o.compareTo(b) <= 0);
        }

        /**
         * 科目コードのyearMinからyearMaxまでの履修単位備考をコンマ連結で得る。
         * @param subclasscd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getRisyuTanniBiko(final String subclasscd, final PrintGakuseki printGakuseki) {
            return getBiko(getSubclassRisyuTanniBikoMap(subclasscd), printGakuseki, "、");
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録備考を得る。
         * @param subclassCd 科目コード
         * @param subclassName 科目名
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public List<String> getStudyrecBikoList(final String subclasscd, final String subclassname, final PrintGakuseki printGakuseki) {
            final List<String> rtn = new ArrayList<String>();
            final String studyrecBiko = getBiko(getStudyrecBikoMap(subclasscd), printGakuseki, "").toString();
            if (!StringUtils.isBlank(studyrecBiko)) {
                rtn.add(studyrecBiko);
            }
            final List<Object[]> sikakuBikoList = getBikoList(getSikakuBikoList(subclasscd), printGakuseki);
            if (sikakuBikoList.size() == 0) {
            } else if (sikakuBikoList.size() >= 2) {
                rtn.add("※下記参照のこと");
            } else {
                rtn.add(createSikakuBiko(subclassname, sikakuBikoList.get(0)));
            }
            return rtn;
        }

        public String createSikakuBiko(final String subclassName, final Object[] d) {
            final String sikakuName = (String) d[1];
            final Integer credits = (Integer) d[2];
            final String annual = (String) d[3];
            final Boolean isOldFormat = (Boolean) d[4];
            final String sikakuBiko;
            if (isOldFormat.booleanValue()) {
                sikakuBiko = append(subclassName, "の") + credits + "単位は" + sikakuName + "による";
            } else {
                String annualStr = "";
                if (NumberUtils.isDigits(annual)) {
                    annualStr = String.valueOf(Integer.parseInt(annual)) + "年次の";
                }
                sikakuBiko = annualStr + append(subclassName, "の") + credits + "単位分は" + sikakuName + "取得による";
            }
            return sikakuBiko;
        }

        /**
         * yearMinからyearMaxまでの資格備考を得る。
         * @return
         */
        public List<String> getSikakuBikoList(final PrintGakuseki printGakuseki, final Map<String, StudyRecSubclassTotal> studyRecSubclassMap) {

            final List<String> rtn = new ArrayList<String>();
            for (final String subclassCd : _sikakubiko.keySet()) {
                final List<Object[]> subclassSikakuBikoList = getBikoList(getSikakuBikoList(subclassCd), printGakuseki);
                final StudyRecSubclassTotal total = studyRecSubclassMap.get(subclassCd);
                if (subclassSikakuBikoList.size() < 2 || null == total) {
                } else {
                    final String subClassName = total.subClassName();
                    for (final Object[] d : subclassSikakuBikoList) {
                        rtn.add(createSikakuBiko(subClassName, d));
                    }
                }
            }
            return rtn;
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録代替科目備考を得る。
         * @param subclasscd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecSubstitutionBiko(final String subclasscd, final String substitutionTypeFlg, final PrintGakuseki printGakuseki) {
            return getBiko(getStudyrecSubstitutionBikoMap(subclasscd, substitutionTypeFlg), printGakuseki, "");
        }

//        /**
//         * 科目コードの学習記録代替科目の単位を得る。
//         * @param subclassCd 科目コード
//         * @return
//         */
//        public Integer getStudyrecSubstitutionCredit(final String subclasscd, final String substitutionTypeFlg, final Param param) {
//            if (null == subclasscd) {
//                return null;
//            }
//            final Map studyRecSubstitutionYearMap = (Map) _ssc.getStudyrecSubstitution(substitutionTypeFlg);
//            final List list = new ArrayList();
//            for (final Iterator yit = studyRecSubstitutionYearMap.keySet().iterator(); yit.hasNext();) {
//                final String year = (String) yit.next();
//                final Map studyRecSubstitutionMap = (Map) studyRecSubstitutionYearMap.get(year);
//                for (final Iterator it = studyRecSubstitutionMap.keySet().iterator(); it.hasNext();) {
//                    final String substitutionSubClassCd = (String) it.next();
//                    if (!subclasscd.equals(substitutionSubClassCd)) {
//                        continue;
//                    }
//                    final StudyRecSubstitution studyRecSubstitution = (StudyRecSubstitution) studyRecSubstitutionMap.get(substitutionSubClassCd);
//                    final Object[] info = studyRecSubstitution.getBikoSubstitutionInfo(param);
//                    list.add(info);
//                }
//            }
//            if (list.isEmpty()) {
//                return null;
//            }
//            int total = 0;
//            for (final Iterator it = list.iterator(); it.hasNext();) {
//                final Object[] info = (Object[]) it.next();
//                total += ((Integer) info[1]).intValue();
//            }
//            return new Integer(total);
//        }

        /**
         * 代替科目の学習記録備考を作成し、マップに加えます。
         */
        private void createStudyRecBikoSubstitution(final Param param, final Map creditMstMap, final Map<String, StudyRecSubclassTotal> studyrecSubclassMap) {

            final Map<String, List<String>> substSubclassCountMap = new HashMap<String, List<String>>();
            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map<String, Map<String, StudyRecSubstitution>> studyRecSubstitutionYearMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);

                // 代替科目備考追加処理
                for (final Map.Entry<String, Map<String, StudyRecSubstitution>> e : studyRecSubstitutionYearMap.entrySet()) {
                    final String year = e.getKey();
                    final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = e.getValue();
                    for (final Map.Entry<String, ?> e2 : studyRecSubstitutionMap.entrySet()) {
                        final String substitutionSubClassCd = e2.getKey();

                        final String keyCd;
                        final String classcd = StringUtils.split(substitutionSubClassCd, "-")[0];
                        if (_90.equals(classcd)) {
                            keyCd = _90;
                        } else {
                            keyCd = substitutionSubClassCd;
                        }

                        getMappedList(substSubclassCountMap, keyCd).add(year);
                    }
                }
            }

            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map<String, Map<String, StudyRecSubstitution>> studyRecSubstitutionYearMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);

                final Map<String, List<Map<String, String>>> setMap = new HashMap<String, List<Map<String, String>>>();
//                final Map totalSubCreditMap = new HashMap();
                final Map<String, Integer> creditNokoriMap = new HashMap<String, Integer>();

                // 代替科目備考追加処理
                for (final Map.Entry<String, Map<String, StudyRecSubstitution>> e : studyRecSubstitutionYearMap.entrySet()) {
                    final String year = e.getKey();
                    final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = e.getValue();
                    for (final Map.Entry<String, StudyRecSubstitution> e2 : studyRecSubstitutionMap.entrySet()) {
                        final String substitutionSubClassCd = e2.getKey();

                        final StudyRecSubstitution studyRecSubstitution = e2.getValue();
                        final String keyCd;
                        final String classcd = StringUtils.split(substitutionSubClassCd, "-")[0];
                        if (_90.equals(classcd)) {
                            keyCd = _90;
                        } else {
                            keyCd = substitutionSubClassCd;
                        }

                        if (StudyrecSubstitutionContainer.ICHIBU.equals(substitutionTypeFlg)) {
                            final Set<String> substitutionYears = studyRecSubstitution.getSubstitutionYears(param);

                            final int creditMstCredits = getCreditMstCreditsTotal(keyCd, creditMstMap, substitutionYears);
                            final StudyRecSubclassTotal subclassTotal = studyrecSubclassMap.get(keyCd);
                            int studyrecCredit = 0;
                            if (null != subclassTotal && null != subclassTotal.credit()) {
                                studyrecCredit = subclassTotal.credit().intValue();
                            }
                            final Integer creditNokori = new Integer(creditMstCredits - studyrecCredit); // 単位マスタの単位 - 生徒の取得単位
                            creditNokoriMap.put(keyCd, creditNokori);

                            final Map<String, Integer> yearCreditMstCredits = getCreditMstYearCreditsMap(param, keyCd, creditMstMap);
                            for (final Map.Entry<String, Integer> ye : yearCreditMstCredits.entrySet()) {
                                final String creYear = ye.getKey();
                                final Integer yearCredit = ye.getValue();
                                int yearStudyrecCredit = 0;
                                if (null != subclassTotal) {
                                    for (int i = 0; i < subclassTotal._subclassStudyrecList.size(); i++) {
                                        final StudyRec sr = subclassTotal._subclassStudyrecList.get(i);
                                        if (creYear.equals(sr._year) && null != sr._credit) {
                                            yearStudyrecCredit += sr._credit.intValue();
                                        }
                                    }
                                }
                                final Integer creditNokori2 = new Integer(yearCredit.intValue() - yearStudyrecCredit); // 単位マスタの単位 - 生徒の取得単位
                                creditNokoriMap.put(keyCd + "-" + creYear, creditNokori2);
                            }
                        }

                        final List<String> addedName = new ArrayList<String>();
                        final Map<String, String> yearSubstCreditMap = new HashMap<String, String>();
                        String totalSubstCredit = "";

                        for (final StudyRecSubstitution.AttendSubclass attendSubclass : studyRecSubstitution.attendSubclasses) {
                            addedName.add(attendSubclass._attendSubclassName);
                            totalSubstCredit = addNumber(attendSubclass._substitutionCredit, totalSubstCredit);
                            yearSubstCreditMap.put(attendSubclass._attendyear, attendSubclass._substitutionCredit);
                        }

                        final Map info2 = new HashMap();
                        info2.put("year", year);
                        info2.put("attendSubclassNameList", addedName);
                        info2.put("totalSubstCredit", totalSubstCredit);
                        info2.put("yearSubstCreditMap", yearSubstCreditMap);
                        getMappedList(setMap, keyCd).add(info2);
                    }
                }

                if (StudyrecSubstitutionContainer.ICHIBU.equals(substitutionTypeFlg)) {
                    if (param._isOutputDebug) {
                        log.info(" substitutionTypeFlg = " + substitutionTypeFlg + ", creditNokoriMap = " + creditNokoriMap);
                    }
                }

                for (final Iterator<Map.Entry<String, List<Map<String, String>>>> it = setMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry<String, List<Map<String, String>>> e = it.next();
                    final String keyCd = e.getKey();

                    final Collection<String> attendSubclassNameList = new ArrayList<String>();
                    final Collection<String> substYearList = new ArrayList<String>();
                    final Collection<String> totalSubstCreditList = new ArrayList<String>();
                    final Map<String, String> yearCredtMap = new HashMap<String, String>();

                    for (final Iterator<Map<String, String>> iit = e.getValue().iterator(); iit.hasNext();) {
                        final Map info2 = iit.next();

                        attendSubclassNameList.addAll((Collection) info2.get("attendSubclassNameList"));
                        substYearList.add((String) info2.get("year"));
                        totalSubstCreditList.add((String) info2.get("totalSubstCredit"));
                        yearCredtMap.putAll((Map<String, String>) (Map) info2.get("yearSubstCreditMap"));
                    }

                    final String totalSubCredit = sum(totalSubstCreditList);

                    String ichibu = "";
                    final String maxSubstYear = maximum(substYearList);
                    if (StudyrecSubstitutionContainer.ICHIBU.equals(substitutionTypeFlg)) {
                        final List<String> substYears = getMappedList(substSubclassCountMap, keyCd);
                        final boolean hasOtherSubst = substYears.size() > 1;
                        final String creditNokoriKey;
                        if ("1".equals(param._seitoSidoYorokuDaitaiCreditEach1) && new HashSet<String>(yearCredtMap.values()).size() == 1 && "2".equals(new HashSet<String>(yearCredtMap.values()).iterator().next())) {
                            final String sCredit = String.valueOf(Integer.parseInt(sum(yearCredtMap.values())) / 2);
                            ichibu = append(sCredit, "単位");
                        } else {
                            if (hasOtherSubst) { // 2回以上代替
                                // 総学の当年度から取得
                                creditNokoriKey = keyCd + "-" + maxSubstYear;
                            } else {
                                // 総学の総単位から取得
                                creditNokoriKey = keyCd;
                            }
                            final Integer creditNokori = creditNokoriMap.get(creditNokoriKey);
                            // 一部代替単位数
                            String sCredit = null;
                            if (null != creditNokori && NumberUtils.isDigits(totalSubCredit)) {
                                sCredit = String.valueOf(Math.min(creditNokori.intValue(), Integer.parseInt(totalSubCredit)));
                            } else {
                                sCredit = null == creditNokori ? totalSubCredit : creditNokori.toString();
                            }
                            if (param._isOutputDebug) {
                                log.info(" " + keyCd + " totalSubCredit = " + totalSubCredit + " / creditNokori = " + creditNokori + " (creditNokoriKey = " + creditNokoriKey + " / " + creditNokoriMap + ") / sCredit = " + sCredit + " / substYears = " + substYears);
                            }
                            ichibu = append(sCredit, "単位");
                        }
                    }

                    putStudyrecSubstitutionBiko(keyCd, substitutionTypeFlg, maxSubstYear, "「" + ichibu + "代替・" + mkStringUnique(attendSubclassNameList, "、").toString() + totalSubCredit + "単位」");
                }
            }
        }

        private static String sum(final Collection<String> col) {
            String sum = null;
            for (final String num : col) {
                sum = addNumber(sum, num);
            }
            return sum;
        }

        private static String maximum(final Collection<String> col) {
            String max = null;
            for (final String o : col) {
                if (null == max) {
                    max = o;
                } else if (null != max && null != o) {
                    max = o.compareTo(max) < 0 ? max : o;
                }
            }
            return max;
        }

        private static StringBuffer mkStringUnique(final Collection<String> col, final String comma) {
            Collection<String> nameYet = new HashSet<String>();
            final StringBuffer stb = new StringBuffer();
            String sp = "";
            for (final String name : col) {
                if (null == name) {
                    continue;
                }
                if (!nameYet.contains(name)) {
                    stb.append(sp + name);
                    sp = comma;
                    nameYet.add(name);
                }
            }
            return stb;
        }

        private int getCreditMstCreditsTotal(final String key, final Map creditMstMap, final Collection<String> substitutionYears) {
            final Map<String, Map<String, Integer>> map = (Map) creditMstMap.get(key + "-YEAR");
            int creditsTotal = 0;
            if (null != map) {
                for (final String year : map.keySet()) {
                    if (substitutionYears.contains(year)) {
                        final Map<String, Integer> subclasscdCredits = map.get(year);
                        for (final Integer credits : subclasscdCredits.values()) {
                            creditsTotal += credits.intValue();
                        }
                    }
                }
            }
            log.fatal(" key = " + key + ", creditMstMap = " + map + " / substitutionYears = " + substitutionYears + ", total = " + creditsTotal);
            return creditsTotal;
        }

        private Map<String, Integer> getCreditMstYearCreditsMap(final Param param, final String key, final Map creditMstMap) {
            final Map<String, Integer> rtn = new HashMap<String, Integer>();
            final Map map = getMappedHashMap(creditMstMap, key + "-YEAR");
            for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String year = (String) e.getKey();
                final Map subMap = (Map) e.getValue();
                for (final Iterator subit = subMap.entrySet().iterator(); subit.hasNext();) {
                    final Map.Entry sube = (Map.Entry) subit.next();
                    //final String subclasscd = (String) sube.getKey();
                    final Integer cred = (Integer) sube.getValue();

                    if (null == rtn.get(year)) {
                        rtn.put(year, cred);
                    } else {
                        rtn.put(year, new Integer(cred.intValue() + ((Integer) rtn.get(year)).intValue()));
                    }
                }
            }
            log.fatal(" key = " + key + ", creditMstMap = " + rtn);
            return rtn;
        }

        /**
         * 総合的な学習の時間の代替科目の学習記録備考を作成し、マップに加えます。
         */
        private Map<String, Map<String, List<String>>> getStudyRecBikoSubstitution90(final String substitutionTypeFlg, final List gakusekiList, final String keyAll, final Param param) {

            final Map<String, Map<String, List<String>>> map = new HashMap<String, Map<String, List<String>>>();

            final Map<String, Map<String, StudyRecSubstitution>> studyRecSubstitutionYearMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);
            for (final String year : studyRecSubstitutionYearMap.keySet()) {
                final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = studyRecSubstitutionYearMap.get(year);
                for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {

                    if (!_90.equals(substitutionSubClassCd.substring(0,2))) {
                        continue;
                    }
                    final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);

//                    for (final Iterator itg = gakusekiList.iterator(); itg.hasNext();) {
//                        final Gakuseki gakuseki = (Gakuseki) itg.next();
//                        if (null == gakuseki._year) {
//                            continue;
//                        }
//                        getMappedList(map, gakuseki._year).add(studyRecSubstitution.getBikoSubstitution90(gakuseki._year, substitutionTypeFlg, param));
//                    }

                    final String biko = studyRecSubstitution.getBikoSubstitution90(null, substitutionTypeFlg, param);
                    log.info(" substitutionTypeFlg " + substitutionTypeFlg + " / " + substitutionSubClassCd + " add biko " + year + " " + biko);
                    getMappedList(getMappedMap(map, year), substitutionSubClassCd).add(biko);
                }
            }
            return map;
        }
    }

    private static class PrintGakuseki {
        final List<String> _yearList = new ArrayList<String>();
        final TreeMap<String, Gakuseki> _gakusekiMap = new TreeMap<String, Gakuseki>();
        final Map<String, Integer> _yearPositionMap = new HashMap<String, Integer>();
        public Gakuseki _dropGakuseki = null;

        public Set<String> getYearSet() {
            return new TreeSet<String>(_yearList);
        }
        public String toString() {
            return "PrintGakuseki(" + _yearList + ", " + _gakusekiMap + ", " + _yearPositionMap + ", " + _dropGakuseki + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static abstract class KNJA130_0 {

        protected Vrw32alp svf;
        protected DB2UDB db2;
        private Param _param;
        private String _currentForm;
        private final Map<String, Map<String, SvfField>> _fieldMap = new HashMap();

        protected boolean nonedata; // データ有りフラグ

        KNJA130_0(final DB2UDB db2, final Vrw32alp svf, final Param param) {
            this.db2 = db2;
            this.svf = svf;
            _param = param;
        }

        protected Param param() {
            return _param;
        }

        public abstract void setDetail(final Student student);

        public int VrSetForm(final String form, final int n) {
            final int rtn = svf.VrSetForm(form, n);
            if (_param._isOutputDebug) {
                log.info(" setForm " + form);
            }
            try {
                _currentForm = form;
                if (!_fieldMap.containsKey(form)) {
                    _fieldMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
                }
            } catch (final Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        public SvfField getField(final String field) {
            return getMappedMap(_fieldMap, _currentForm).get(field);
        }

        protected String getFieldForData(final String[] fields, final String data) {
            final int datasize = getMS932ByteLength(data);
            String fieldFound = null;
            searchField:
            for (int i = 0; i < fields.length; i++) {
                final String fieldname = fields[i];
                final SvfField svfField = getField(fieldname);
                if (null == svfField) {
                    continue searchField;
                }
                fieldFound = fieldname;
                if (datasize <= svfField._fieldLength) {
                    return fieldname;
                }
            }
            return fieldFound;
        }

        public int svfVrsOutForData(final String[] fields, final String data) {
            return VrsOut(getFieldForData(fields, data), data);
        }

        public int VrsOut(final String field, final String data) {
            SvfField svfField = getField(field);
            if (null == svfField && (_param._isOutputDebug || _param._isOutputDebugField)) {
                log.error("no field " + field + " (" + data + ")");
            } else if (_param._isOutputDebugField) {
                log.info("(" + (null == field ? field : "\"" + field + "\"") + ", " + (null == data ? data : "\"" + data + "\"") + ")");
            }
            return svf.VrsOut(field, data);
        }

        public int VrEndRecord() {
            if (_param._isOutputDebugField) {
                log.info("");
            }
            return svf.VrEndRecord();
        }

        /**
         * <pre>
         *  学年・年度表示欄の印字位置（番号）を戻します。
         *  ・学年制の場合は学年による固定位置
         *  ・単位制の場合は連番
         * </pre>
         *
         * @param i 連番
         * @param gakuseki
         * @return
         */
        protected int getGradeColumnNum(final int i, final Gakuseki gakuseki) {
            return getGradeColumnNum(i, gakuseki, false);
        }

        protected int getGradeColumnNum(final int i, final Gakuseki gakuseki, final boolean includeZaisekiMae) {
            final int column = _param._is3YearsSystem ? 3 : 4;
            if (_param.isGakunenSei() && !includeZaisekiMae) {
                if (null == gakuseki._gradeCd) {
                    return -1;
                }
                int j = Integer.parseInt(gakuseki._gradeCd);
                return (0 == j % column) ? column : j % column;
            } else {
                return i;
            }
        }

        /**
         * <pre>
         *  学年制で留年した生徒で改ページする場合ページごとの学籍のリストを得る
         * </pre>
         *
         * @param gakusekiList
         * @return ページごとの学籍のリスト
         */
        protected  List<Gakuseki> getPageGakusekiList(final Student student, final Gakuseki gakuseki) {
            for (final List<Gakuseki> gakusekiList : student._pageGakusekiListList) {
                if (gakusekiList.contains(gakuseki)) {
                    return gakusekiList;
                }
            }
            return Collections.EMPTY_LIST;
        }

        protected void printName(final Vrw32alp svf,
                final Student student,
                final String field,
                final String field1,
                final String field2,
                final KNJSvfFieldInfo name) {

            final int width = name._x2 - name._x1;
            final PersonalInfo personalInfo = student._personalInfo;
            if (personalInfo._isPrintRealName &&
                    personalInfo._isPrintNameAndRealName &&
                    !StringUtils.isBlank(personalInfo._studentRealName + personalInfo._studentName) &&
                    !personalInfo._studentRealName.equals(personalInfo._studentName)
            ) {
                final String printName1 = personalInfo._studentRealName;
                final String printName2 = personalInfo._studentName;
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(field1, width, name._height, name._ystart1, name._minnum, name._maxnum);
                final float charSize1 = modify1.getCharSize(printName1);
                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(field2, width, name._height, name._ystart2, name._minnum, name._maxnum);
                final float charSize2 = modify2.getCharSize(printName2);
                final float charSize = Math.min(charSize1, charSize2);
                svf.VrAttribute(field1, "Size=" + charSize);
                svf.VrAttribute(field2, "Size=" + charSize);
                VrsOut(field1, printName1);
                VrsOut(field2, printName2);
            } else {
                final String printName = personalInfo._isPrintRealName ? personalInfo._studentRealName : personalInfo._studentName;
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(field, width, name._height, name._ystart, name._minnum, name._maxnum);
                final float charSize = modify.getCharSize(printName);
                svf.VrAttribute(field, "Size=" + charSize);
                VrsOut(field, printName);
            }
        }

        protected static class KNJSvfFieldInfo {
            int _x1;   //開始位置X(ドット)
            int _x2;   //終了位置X(ドット)
            int _height;  //フィールドの高さ(ドット)
            int _ystart;  //開始位置Y(ドット)
            int _ystart1;  //開始位置Y(ドット)フィールド1
            int _ystart2;  //開始位置Y(ドット)フィールド2
            int _minnum;  //最小設定文字数
            int _maxnum;  //最大設定文字数
            public KNJSvfFieldInfo(final int x1, final int x2, final int height, final int ystart, final int ystart1, final int ystart2, final int minnum, final int maxnum) {
                _x1 = x1;
                _x2 = x2;
                _height = height;
                _ystart = ystart;
                _ystart1 = ystart1;
                _ystart2 = ystart2;
                _minnum = minnum;
                _maxnum = maxnum;
            }
            public KNJSvfFieldInfo() {
                this(-1, -1, -1, -1, -1, -1, -1, -1);
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /*
     * 学校教育システム 賢者 [学籍管理] 生徒指導要録 学籍の記録 2004/08/18
     * yamashiro・組のデータ型が数値でも文字でも対応できるようにする 2006/04/13
     * SCHREG_BASE_MST ) --NO001 ・編入において学年が出力されない不具合を修正 --NO001 ・印鑑の出力処理を追加
     * --NO002 ・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加 --NO003 =>
     * 無い場合は従来通りHR_CLASSを出力 ・日付および年度の出力仕様を変更 --NO004 ・学年をすべて出力 --NO005
     * ・生徒名および保護者名の出力仕様変更 --NO006 ・学籍異動履歴出力仕様変更 --NO007 2006/05/02
     * yamashiro・入学前学歴の学校名出力仕様変更 --NO008
     */

    /**
     * 生徒指導要録(学籍の記録)。
     */
    private static class KNJA130_1 extends KNJA130_0 {

        private String _form;
        private KNJSvfFieldInfo _kana;
        private KNJSvfFieldInfo _gKana;
        private KNJSvfFieldInfo _name;
        private KNJSvfFieldInfo _gName;
        int _addressMax;

        KNJA130_1(final DB2UDB db2, final Vrw32alp svf, final Param param) {
            super(db2, svf, param);
        }

        public void setForm(final Student student) {
            if (param()._isNewForm) {
                _kana = new KNJSvfFieldInfo(749, 1545, KNJSvfFieldModify.charSizeToPixel(8.0), 929, -1, -1, 12, 100);
                _gKana = new KNJSvfFieldInfo(749, 1712, KNJSvfFieldModify.charSizeToPixel(8.0), 1893, -1, -1, 12, 100);
                _name = new KNJSvfFieldInfo(749, 1545, KNJSvfFieldModify.charSizeToPixel(13.0), 1080, 1040, 1118, 24, 48);
                _gName = new KNJSvfFieldInfo(749, 1712, KNJSvfFieldModify.charSizeToPixel(13.0), 2044, 2004, 2084, 24, 48);
                if (param()._is3YearsSystem) {
                    _form = "KNJA130_11B_2.frm";
                } else {
                    _form = "KNJA130_1B_2.frm";
                }
                _addressMax = 2;
            } else {
                _kana = new KNJSvfFieldInfo(689, 1550, KNJSvfFieldModify.charSizeToPixel(8.0), 1287, -1, -1, 12, 100);
                _gKana = new KNJSvfFieldInfo(689, 1689, KNJSvfFieldModify.charSizeToPixel(8.0), 2369, -1, -1, 12, 100);
                _name = new KNJSvfFieldInfo(689, 1556, KNJSvfFieldModify.charSizeToPixel(13.0), 1441, 1401, 1481, 24, 48);
                _gName = new KNJSvfFieldInfo(689, 1689, KNJSvfFieldModify.charSizeToPixel(13.0), 2523, 2483, 2563, 24, 48);
                if (param()._is3YearsSystem) {
                    _form = "KNJA130_11B.frm";
                } else {
                    _form = "KNJA130_1B.frm";
                }
                _addressMax = 3;
            }
            VrSetForm(_form, 1);
        }

        public void setDetail(final Student student) {
            final List<List<Gakuseki>> gakusekiListList = new ArrayList();
            List<Gakuseki> current = null;
            final int max = param()._is3YearsSystem ? 3 : 4;
            int i = -1;
            for (final Gakuseki gakuseki : student._gakusekiList) {
                i = getGradeColumnNum(i, gakuseki);
                if (i > max) {
                    current = null;
                }
                if (null == current || current.size() >= max) {
                    current = new ArrayList<Gakuseki>();
                    gakusekiListList.add(current);
                    i = 0;
                }
                current.add(gakuseki);
                if (gakuseki._isDrop) {
                    // 留年以降を改ページします。
                    log.debug("drop = " + gakuseki);
                    current = null;
                    i = 0;
                }
            }

            for (int pi = 0; pi < gakusekiListList.size(); pi++) {
                final List<Gakuseki> gakusekiList = gakusekiListList.get(pi);
                setForm(student);
                // 印刷処理
                printDetail(student);
                i = 0;
                for (final Gakuseki gakuseki : gakusekiList) {
                    i = getGradeColumnNum(i, gakuseki);
                    printGradeRecDetail(i, gakuseki, student._schoolInfo);
                }
                svf.VrEndPage();
                nonedata = true;
            }
        }

        /**
         * {@inheritDoc}
         */
        protected int getGradeColumnNum(int i, final Gakuseki gakuseki) {
            if (param().isGakunenSei()) {
                if (null == gakuseki._gradeCd) {
                    i = -1;
                } else {
                    i = Integer.parseInt(gakuseki._gradeCd);
                }
            } else {
                i++;
            }
            return i;
        }

        private String getTransGrade(final String transGrade) {
            if (null != transGrade) {
                return transGrade;
            } else {
                return "1";
            }
        }

        /**
         * 変動しない(ページで)項目を印刷します。
         * @param student
         */
        private void printDetail(final Student student) {
            // デフォルト印刷
            printSvfDefault(student);

            // 学校情報印刷
            printSchoolInfo(student);

            // 個人情報印刷
            printPersonalInfo(student);

            // 住所履歴印刷
            printAddressRec(student);

            // 保護者住所履歴印刷
            printGuardianAddressRec(student);

            // 異動履歴印刷
            printTransferRec(student);

            // 学籍等履歴項目名印刷
            printGradeRecTitle(student);

            // 進学先・就職先等印刷
            printAfterGraduatedCourse(student);
        }

        /**
         * 異動情報を印刷します。
         * @param param
         * @param student
         */
        private void printTransferRec(final Student student) {
            final String torikeshisen = (param().isGakunenSei()) ? "＝＝＝＝＝＝＝" : "＝＝＝";
            final int x;
            if (param()._isNewForm) {
                final int charWidth = 49;
                x = 2737 + (param().isGakunenSei() ? 0 : charWidth * 4);
            } else {
                final int charWidth = 55;
                x = 2775 + (param().isGakunenSei() ? 0 : charWidth * 4);
            }
            final int keta = getMS932ByteLength(torikeshisen);
            svf.VrAttribute("LINE1", "X=" + x + ", UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            svf.VrAttribute("LINE2", "X=" + x + ", UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            if (param().isGakunenSei()) {
                VrsOut("ENTERDIV1", "第  学年　入学");
                VrsOut("ENTERDIV2", "第  学年編入学");
                VrsOut("TENNYU", "第  学年転入学");
            } else {
                VrsOut("ENTERDIV1", "入　学");
                VrsOut("ENTERDIV2", "編入学");
            }

            int ia = 1; // 休学・留学回数
            String namecd1 = null;
            for (final TransferRec tr : student._transferRecList) {
                namecd1 = tr._nameCode1;
                int namecd2 = Integer.parseInt(tr._nameCode2);
                if (namecd1.equals("A002")) {
                    // 入学・編入学・転入学
                    if (4 == namecd2) {
                        printTennyugaku(tr);
                    } else if (5 == namecd2) {
                        printHennyugaku(tr);
                    } else {
                        printNyugaku(tr);
                    }
                }
                if (namecd1.equals("A004")) {
                    if (namecd2 == 1) { // 留学
                        if (printRyugakuKyugaku(student, tr, ia)) {
                            ia++;
                        }
                    }
                    if (namecd2 == 2) { // 休学
                        if (printRyugakuKyugaku(student, tr, ia)) {
                            ia++;
                        }
                    }
                }
                if (namecd1.equals("A003")) {
                    if (namecd2 == 3 || namecd2 == 2 || namecd2 == 6 || namecd2 == 7) {
                        VrsOut("TRANSFER_DATE_2", tr._sDateStr);
//                        if (namecd2 == 3) {
//                            VrsOut("tengaku_GRADE", gradecdPlus1When0331(tr._gradeCd, tr._sDate));
//                        } else {
//                            VrsOut("tengaku_GRADE", tr._gradeCd);
//                        }
                        VrsOut("tengaku_GRADE", tr._gradeCd);
//                        if (tr._reason != null)
//                            VrsOut("TRANSFERREASON2_1", tr._reason);
                        if (tr._place != null)
                            VrsOut("TRANSFERREASON2_1", tr._place); // NO007
                        if ("1".equals(param()._useAddrField2)) {
                            final boolean useField2 = getMS932ByteLength(tr._address) > 50 || getMS932ByteLength(tr._address2) > 50;
                            if (null != tr._address2) {
                                VrsOut("TRANSFERREASON2_2" + (useField2 ? "_2" : ""), tr._address);
                                VrsOut("TRANSFERREASON2_3" + (useField2 ? "_2" : ""), tr._address2);
                            } else {
                                VrsOut("TRANSFERREASON2_2" + (useField2 ? "_2" : ""), tr._address);
                            }
                        } else {
                            if (tr._address != null)
                                VrsOut("TRANSFERREASON2_2", tr._address); // NO007
                        }

                        if (namecd2 == 3) { // 転学
                            VrsOut("KUBUN", "転学");
                        } else if (namecd2 == 2) { // 退学
                            VrsOut("KUBUN", "退学");
                        } else if (namecd2 == 7) { // 転籍
                            VrsOut("KUBUN", "転籍");
                        } else if (namecd2 == 6) { // 除籍
                            VrsOut("KUBUN", "除籍");
                        }
                    }
                    if (namecd2 == 1) { // 卒業
                        VrsOut("TRANSFER_DATE_4", tr._sDateStr);
                        if (!param()._isYuushinkan && tr._certifno != null)
                            VrsOut("FIELD1", tr._certifno); // 卒業台帳番号
                                                                                // NO007
                                                                                // Modify
                    }
                }
            }
        }

        /**
         * 3/31転学の場合学年コード+1、それ以外は学年コードを得る。
         * @param gradecd 学年コード
         * @param date 転学日付
         * @return 学年コード
         */
        private String gradecdPlus1When0331(final String gradecd, final String date) {
            if (!StringUtils.isNumeric(gradecd) || !"3".equals(month(date)) || !"31".equals(dayOfMonth(date))) {
                return gradecd;
            }
            return String.valueOf(1 + Integer.parseInt(gradecd));
        }

        /**
         * 入学を印字します。
         * @param brank:ブランク文字列
         * @param transferrec
         */
        private void printNyugaku(final TransferRec transferRec) {
            VrsOut("ENTERDATE1", transferRec._sDateStr);
            svf.VrAttribute("LINE1", "X=10000"); // 打ち消し線消去
//            if (null != transferRec._reason) {
//                VrsOut("ENTERRESONS3", "(" + transferRec._reason + ")");
//            }

            final StringBuffer stb = new StringBuffer();
            if (param().isGakunenSei()) {
                stb.append("第 ");
                String str = getTransGrade(transferRec._gradeCd);
                stb.append(str);
                stb.append("学年");
                stb.append("入　学");
                VrsOut("ENTERDIV1", stb.toString());
            }
        }

        /**
         * 編入学を印字します。
         * @param brank:ブランク文字列
         * @param transferrec
         */
        private void printHennyugaku(final TransferRec transferRec) {
            VrsOut("ENTERDATE2", transferRec._sDateStr);
            svf.VrAttribute("LINE2", "X=10000"); // 打ち消し線消去
//            if (null != transferRec._reason) {
//                VrsOut("ENTERRESONS3", "(" + transferRec._reason + ")");
//            }

            final StringBuffer stb = new StringBuffer();
            if (param().isGakunenSei()) {
                stb.append("第 ");
                String str = (null != transferRec._gradeCd) ? transferRec._gradeCd : " ";
                stb.append(str);
                stb.append("学年");
                stb.append("編入学");
                VrsOut("ENTERDIV2", stb.toString());
            }
        }

        /**
         * 転入学を印字します。
         * @param transferrec
         */
        private void printTennyugaku(final TransferRec transferrec) {
            VrsOut("TRANSFER_DATE_1", transferrec._sDateStr);

            if (null != transferrec._place) {
                VrsOut("TRANSFERREASON1_1", transferrec._place);
            }
            if ("1".equals(param()._useAddrField2)) {
                final boolean useField2 = getMS932ByteLength(transferrec._address) > 50 || getMS932ByteLength(transferrec._address2) > 50;
                if (null != transferrec._address2) {
                    VrsOut("TRANSFERREASON1_2" + (useField2 ? "_2" : ""), transferrec._address);
                    VrsOut("TRANSFERREASON1_3" + (useField2 ? "_2" : ""), transferrec._address2);
                } else {
                    VrsOut("TRANSFERREASON1_2" + (useField2 ? "_2" : ""), transferrec._address);
                }
            } else {
                if (null != transferrec._address) {
                    VrsOut("TRANSFERREASON1_2", transferrec._address);
                }
//                if (null != transferrec._reason) {
//                    VrsOut("TRANSFERREASON1_3", "(" + transferrec._reason + ")");
//                }
            }

            if (param().isGakunenSei()) {
                final StringBuffer stb = new StringBuffer();
                stb.append("第 ");
                String str = (null != transferrec._gradeCd) ? transferrec._gradeCd : " ";
                stb.append(str);
                stb.append("学年");
                stb.append("転入学");
                VrsOut("TENNYU", stb.toString());
            }
        }

        /**
         * 留学・休学を印字します。
         * @param tr
         */
        private boolean printRyugakuKyugaku(final Student student, final TransferRec tr, final int ia) {
            if (!(param()._seitoSidoYorokuZaisekiMae && Integer.parseInt(tr._sYear) < student.gakusekiMinYear() || student.gakusekiYearSet().contains(tr._sYear))) {
                return false;
            } else if (ia > 3) {
                return false;
            }
            VrsOut("TRANSFER_DATE3_" + ia, tr._sDateStr + "\uFF5E" + tr._eDateStr);
            final String reason = tr._reason == null ? "" : "／" + tr._reason;
            VrsOut("TRANSFERREASON3_" + ia + "_1", tr._name + reason);
            if (tr._place != null) {
                VrsOut("TRANSFERREASON3_" + ia + "_2", tr._place);
            }
            return true;
        }

        /**
         * 生徒住所履歴を印刷します。
         * @param student
         */
        private void printAddressRec(final Student student) {
            final List<AddressRec> printAddressRecList = AddressRec.getPrintAddressRecList(student._addressRecList, _addressMax);
            for (int i = 1, num = printAddressRecList.size(); i <= num; i++) {
                final AddressRec addressRec = printAddressRecList.get(i - 1);
                final boolean islast = i == num;
                printZipCode("ZIPCODE", i, addressRec._zipCode, islast, "ZIPCODELINE");

                final int n1 = getMS932ByteLength(addressRec._address1);
                printAddr1("ADDRESS", i, addressRec._address1, n1, islast, "ADDRESSLINE");

                if (addressRec._isPrintAddr2) {
                    printAddr2("ADDRESS", i, addressRec._address2, n1, islast, "ADDRESSLINE");
                }
            }
        }

        private void printAddr1(final String field, int i, final String addr1, final int keta1, final boolean islast, final String linefield) {
            String p = null;
            if ("1".equals(param()._useAddrField2) && 50 < keta1) {
                p = "_1_3";
            } else if (40 < keta1) {
                p = "_1_2";
            } else if (0 < keta1) {
                p = "_1_1";
            }
            if (p != null) {
                VrsOut(field + i + p, addr1);
                printAddressLine(svf, addr1, islast, linefield + i + p);
            }
        }

        private void printAddr2(final String field, int i, final String addr2, final int keta1, final boolean islast, final String linefield) {
            if (param()._isYuushinkan) {
            } else {
                final int keta2 = getMS932ByteLength(addr2);
                String p = null;
                if ("1".equals(param()._useAddrField2) && (50 < keta2 || 50 < keta1)) {
                    p = "_2_3";
                } else if (40 < keta2 || 40 < keta1) {
                    p = "_2_2";
                } else if (0 < keta2) {
                    p = "_2_1";
                }
                if (p != null) {
                    VrsOut(field + i + p, addr2);
                    printAddressLine(svf, addr2, islast, linefield + i + p);
                }
            }
        }

        private boolean isSameAddressList(final List<AddressRec> addrListA, final List<AddressRec> addrListB) {
            boolean rtn = true;
            if (addrListA == null || addrListA.isEmpty() || addrListB == null || addrListB.isEmpty() || addrListA.size() != addrListB.size()) {
                rtn = false;
            } else {
                final int max = addrListA.size(); // == addrList2.size();
                for (int i = 0; i < max; i++) {
                    final AddressRec addressAi = addrListA.get(i);
                    final AddressRec addressBi = addrListB.get(i);
                    if (!isSameAddress(addressAi, addressBi)) {
                        rtn = false;
                        break;
                    }
                }
            }
            return rtn;
        }

        private boolean isSameAddress(final AddressRec addressAi, final AddressRec addressBi) {
            boolean rtn = true;
            if (null == addressAi || null == addressBi) {
                rtn = false;
            } else {
                if (null == addressAi._address1 && null == addressBi._address1) {
                } else if (null == addressAi._address1 || null == addressBi._address1 || !addressAi._address1.equals(addressBi._address1)) {
                    rtn = false;
                }
                if (null == addressAi._address2 && null == addressBi._address2) {
                } else if (!addressAi._isPrintAddr2 && !addressBi._isPrintAddr2) {
                } else if (null == addressAi._address2 || null == addressBi._address2 || !addressAi._address2.equals(addressBi._address2)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        private AddressRec getSameLineSchregAddress(final List<AddressRec> printAddressRecList, final int i) {
            AddressRec rtn = null;
            if (printAddressRecList.size() > i) {
                rtn = printAddressRecList.get(i);
            }
            return rtn;
        }

        /**
         * 保護者住所履歴を印刷します。
         * @param student
         */
        private void printGuardianAddressRec(final Student student) {
            VrsOut("GRD_HEADER", student._addressGrdHeader);
            final String SAME_TEXT = "生徒の欄に同じ";
            final List printAddressRecList = AddressRec.getPrintAddressRecList(student._addressRecList, _addressMax);
            final List<AddressRec> guardPrintAddressRecList = AddressRec.getPrintAddressRecList(student._guardianAddressRecList, _addressMax);
            if (AddressRec.isSameAddressList(printAddressRecList, guardPrintAddressRecList)) {
                // 住所が生徒と同一
                VrsOut("GUARDIANADD1_1_1", SAME_TEXT);
                return;
            }
            for (int i = 1, num = guardPrintAddressRecList.size(); i <= num; i++) {
                final AddressRec guardianAddressRec = guardPrintAddressRecList.get(i - 1);
                final boolean islast = i == num;
                final String guardianAddress1 = StringUtils.defaultString(guardianAddressRec._address1);
                final String guardianAddress2 = StringUtils.defaultString(guardianAddressRec._address2);

                //final AddressRec schregAddressRec = getSameLineSchregAddress(printAddressRecList, i - 1);
                boolean isSameAddress = false; // AddressRec.isSameAddress(schregAddressRec, guardianAddressRec);
                if (islast && !isSameAddress) {
                    // 最新の生徒住所とチェック
                    final String addr1 = StringUtils.defaultString(student._personalInfo._schAddress1);
                    final String addr2 = StringUtils.defaultString(student._personalInfo._schAddress2);
                    isSameAddress = addr1.equals(guardianAddress1) && addr2.equals(guardianAddress2);
                }

                if (islast && isSameAddress) {
                    // 内容が生徒と同一
                    VrsOut("GUARDIANADD" + i + "_1_1", SAME_TEXT);
                    printAddressLine(svf, SAME_TEXT, islast, "GUARDIANADDLINE" + i + "_1_1");
                } else {
                    printZipCode("GUARDZIP", i, guardianAddressRec._zipCode, islast, "GUARDZIPLINE");

                    final int keta1 = getMS932ByteLength(guardianAddress1);
                    printAddr1("GUARDIANADD", i, guardianAddress1, keta1, islast, "GUARDIANADDLINE");

                    if (guardianAddressRec._isPrintAddr2) {
                        printAddr2("GUARDIANADD", i, guardianAddress2, keta1, islast, "GUARDIANADDLINE");
                    }
                }
            }
        }

        private void printZipCode(final String field, int i, final String zipCode, final boolean islast, final String linefield) {
            if (param()._printZipcd) {
                VrsOut(field + i, zipCode);
                printAddressLine(svf, zipCode, islast, linefield + i);
            }
        }

        /**
         * かなを表示する
         */
        private void printKana(final String fieldKana, final String schKana, final int posx1, final int posx2, final int height, final int ystart, final int minnum, final int maxnum) {
            final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldKana, posx2 - posx1, height, ystart, minnum, maxnum);
            final float charSize = modify.getCharSize(schKana);
            svf.VrAttribute(fieldKana, "Size=" + charSize);
            svf.VrAttribute(fieldKana, "Y=" + (int) modify.getYjiku(0, charSize));
            VrsOut(fieldKana, schKana);
        }

        /**
         * 名前を表示する。
         */
        private void printName(final String nameHistFirst, String name, final String field, final int posx1, final int posx2, final int height, final int minnum, final int maxnum, final int ystart, final int ystart1, final int ystart2) {
            if (StringUtils.isBlank(nameHistFirst) || nameHistFirst.equals(name)) {
                // 履歴なしもしくは最も古い履歴の名前が現データの名称と同一
                final String fieldname = field + "1";
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldname, posx2 - posx1, height, ystart, minnum, maxnum);
                final float charSize = modify.getCharSize(name);
                svf.VrAttribute(fieldname, "Size=" + charSize);
                svf.VrAttribute(fieldname, "Y=" + (int) modify.getYjiku(0, charSize));
                VrsOut(fieldname, name);
            } else {
                final int keta = Math.min(getMS932ByteLength(nameHistFirst), maxnum);
                final String fieldname1 = field + "1_1";
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(fieldname1, posx2 - posx1, height, ystart1, minnum, maxnum);
                final float charSize1 = modify1.getCharSize(nameHistFirst);
                svf.VrAttribute(fieldname1, "Size=" + charSize1);
                svf.VrAttribute(fieldname1, "Y=" + (int) modify1.getYjiku(0, charSize1));
                VrsOut(fieldname1, nameHistFirst);
                svf.VrAttribute(fieldname1, "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線

                final String fieldname2 = field + "2_1";
                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(fieldname2, posx2 - posx1, height, ystart2, minnum, maxnum);
                final float charSize2 = modify2.getCharSize(name);
                svf.VrAttribute(fieldname2, "Size=" + charSize2);
                svf.VrAttribute(fieldname2, "Y=" + (int) modify2.getYjiku(0, charSize2));
                VrsOut(fieldname2, name);
            }
        }

        /**
         * 生徒情報を印刷します。
         * @param student
         * @param personalinfo
         */
        private void printPersonalInfo(final Student student) {
            final PersonalInfo personalInfo = student._personalInfo;
            if (null == personalInfo) {
                return;
            }
            final int keta = getMS932ByteLength("＝＝＝＝＝");
            if (!"全日制".equals(personalInfo._courseName)) {
                svf.VrAttribute("LINE4", "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            }
            if (!"定時制".equals(personalInfo._courseName)) {
                svf.VrAttribute("LINE5", "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            }
            VrsOut("COURSENAME", personalInfo._courseName);
            VrsOut("MAJORNAME", personalInfo._majorName);

            printKana("KANA", personalInfo._studentKana, _kana._x1, _kana._x2, _kana._height, _kana._ystart, _kana._minnum, _kana._maxnum);
            final String guarKana = student._isPrintGuarantor ? personalInfo._guarantorKana : personalInfo._guardKana;
            printKana("GUARD_KANA", guarKana, _gKana._x1, _gKana._x2, _gKana._height, _gKana._ystart, _gKana._minnum, _gKana._maxnum);
            if (param()._simei != null) { // 漢字名指定あり？
                final String printName;
                if (personalInfo._isPrintRealName && personalInfo._isPrintNameAndRealName && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                    printName = personalInfo._studentRealName + personalInfo._studentName;
                } else if (personalInfo._isPrintRealName) {
                    printName = personalInfo._studentRealName;
                } else {
                    printName = personalInfo._studentName;
                }
                printName(personalInfo._studentNameHistFirst, printName, "NAME", _name._x1, _name._x2, _name._height, _name._minnum, _name._maxnum, _name._ystart, _name._ystart1, _name._ystart2);

                final String guarName = student._isPrintGuarantor ? personalInfo._guarantorName : personalInfo._guardName;
                final String guarNamehistFirst = student._isPrintGuarantor ? personalInfo._guarantorNameHistFirst : personalInfo._guardNameHistFirst;
                printName(guarNamehistFirst, guarName, "GUARD_NAME", _gName._x1, _gName._x2, _gName._height, _gName._minnum, _gName._maxnum, _gName._ystart, _gName._ystart1, _gName._ystart2);
            }
            VrsOut("BIRTHDAY", personalInfo._birthdayStr + "生");
            VrsOut("SEX", personalInfo._sex);
            VrsOut("J_GRADUATEDDATE_Y", personalInfo._finishDate);
            if (param().isInstallationPrint(db2) && !"HIRO".equals(param()._definecode.schoolmark)) {
                if (param()._isHousei) {
                    final String ritu = personalInfo._installationDiv;
                    if (null != ritu) {
                        VrsOut("INSTALLATION_DIV",  ritu + "立");
                    }
                } else {
                    VrsOut("INSTALLATION_DIV", personalInfo._installationDiv);
                }
            }
            // 入学前学歴の学校名編集
            if (param()._isKumamoto) {
                printSvfFinSchoolKumamoto(personalInfo._installationDiv, personalInfo._juniorSchoolName, personalInfo._finschoolTypeName + "卒業");
            } else {
                printSvfFinSchool(personalInfo._juniorSchoolName, personalInfo._finschoolTypeName + "卒業");
            }                                                                   // --NO008
        }

        /**
         *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
         *  高校指導要録・中学指導要録・中等学校指導要録の様式１で使用しています。
         *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
         *  全角スペースより前半の文字を○○○○○立と見なします。
         *  @param str1 例えば"千代田区　アルプ"
         *  @param str2 例えば"小学校卒業"
         */
        private void printSvfFinSchool(
                final String str1,
                final String str2
        ) {
            final String schoolName;
            if (null == str1) {
                schoolName = "";
            } else {
                final int i = str1.indexOf('　');  // 全角スペース
                if (-1 < i && 5 >= i) {
                    final String ritu = str1.substring(0, i);
                    if (null != ritu) {
                        VrsOut("INSTALLATION_DIV",  ritu + "立");
                    }
                    schoolName = str1.substring(i + 1);
                } else {
                    schoolName = str1;
                }
            }
            final int schoolNameLen = getMS932ByteLength(schoolName);

            final String kotei = (null == str2) ? "" : str2;
            final int koteiLen = getMS932ByteLength(kotei);

            final String finschool1 = (!"HIRO".equals(param()._definecode.schoolmark)) ? "FINSCHOOL1" : "FINSCHOOL1_HIRO";
            final String finschool2 = (!"HIRO".equals(param()._definecode.schoolmark)) ? "FINSCHOOL2" : "FINSCHOOL2_HIRO";
            final String finschool3 = (!"HIRO".equals(param()._definecode.schoolmark)) ? "FINSCHOOL3" : "FINSCHOOL3_HIRO";

            if (schoolNameLen == 0) {
                VrsOut(finschool1, kotei);
            } else if (schoolNameLen + koteiLen <= 40) {
                VrsOut(finschool1, schoolName + kotei);
            } else if(schoolNameLen + koteiLen <= 50) {
                VrsOut(finschool2, schoolName + kotei);
            } else {
                VrsOut(finschool2, schoolName);
                VrsOut(finschool3, kotei);
            }
        }

        /**
         *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
         *  高校指導要録・中学指導要録・中等学校指導要録の様式１で使用しています。
         *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
         *  全角スペースより前半の文字を○○○○○立と見なします。
         *  熊本はINSTLLATION_DIVを、学校名の前に出力します。
         *  @param nameMstL001
         *  @param str1 例えば"千代田区　アルプ"
         *  @param str2 例えば"小学校卒業"
         */
        private void printSvfFinSchoolKumamoto(
                final String nameMstL001,
                final String str1,
                final String str2
        ) {
            final String schoolName;
            if (null == str1) {
                schoolName = "";
            } else {
                schoolName = StringUtils.defaultString(nameMstL001) + StringUtils.defaultString(str1);
            }
            final int schoolNameLen = getMS932ByteLength(schoolName);

            final String kotei = (null == str2) ? "" : str2;
            final int koteiLen = getMS932ByteLength(kotei);

            final String finschool1 = "FINSCHOOL1";
            final String finschool2 = "FINSCHOOL2";
            final String finschool3 = "FINSCHOOL3";

            if (schoolNameLen == 0) {
                VrsOut(finschool1, kotei);
            } else if (schoolNameLen + koteiLen <= 40) {
                VrsOut(finschool1, schoolName + kotei);
            } else if(schoolNameLen + koteiLen <= 50) {
                VrsOut(finschool2, schoolName + kotei);
            } else {
                VrsOut(finschool2, schoolName);
                VrsOut(finschool3, kotei);
            }
        }

        /**
         * 学校情報を印刷します。
         */
        private void printSchoolInfo(final Student student) {
            final SchoolInfo schoolInfo = student._schoolInfo;
            if (null == schoolInfo) {
                return;
            }
            VrsOut("NAME_gakko1", schoolInfo._schoolName1);
            if (!StringUtils.isBlank(schoolInfo._bunkouSchoolName)) {
                VrsOut("NAME_gakko2", "（" + schoolInfo._bunkouSchoolName + "）");
            }
            if (param()._isNewForm || "KNJA130_1B_2.frm".equals(_form)) {
                VrsOut("ADDRESS_gakko1", schoolInfo._schoolAddress1 + schoolInfo._schoolAddress2);
                if (!StringUtils.isBlank(schoolInfo._bunkouSchoolAddress1 + schoolInfo._bunkouSchoolAddress2)) {
                    VrsOut("ADDRESS_gakko3", "（" + schoolInfo._bunkouSchoolAddress1 + schoolInfo._bunkouSchoolAddress2 + "）");
                }
            } else {
                final int addr1len = getMS932ByteLength(schoolInfo._schoolAddress1);
                final int addr2len = getMS932ByteLength(schoolInfo._schoolAddress2);
                final int addr3len = getMS932ByteLength(schoolInfo._bunkouSchoolAddress1 + schoolInfo._bunkouSchoolAddress2);
                final int addr3plen = getMS932ByteLength("（" + schoolInfo._bunkouSchoolAddress1 + schoolInfo._bunkouSchoolAddress2 + "）");
                final boolean use40 = addr1len <= 40 && addr2len <= 40 && (addr3len == 0 ? 0 : addr3plen) <= 40;
                final boolean use50 = !"1".equals(param()._useAddrField2) || addr1len <= 50 && addr2len <= 50 && (addr3len == 0 ? 0 : addr3plen) <= 50;
                VrsOut("ADDRESS_gakko1" + (use40 ? "" : use50 ? "_2" : "_3"), schoolInfo._schoolAddress1);
                VrsOut("ADDRESS_gakko2" + (use40 ? "" : use50 ? "_2" : "_3"), schoolInfo._schoolAddress2);
                if (addr3len > 0) {
                    VrsOut("ADDRESS_gakko3" + (use40 ? "" : use50 ? "_2" : "_3"), "（" + schoolInfo._bunkouSchoolAddress1 + schoolInfo._bunkouSchoolAddress2 + "）");
                }
            }
            if (param()._printSchoolZipcd) {
                VrsOut("ZIPCODE", "〒" + schoolInfo._schoolZipcode);
            }
        }

        /**
         * 住所の取り消し線印刷
         * @param svf
         * @param i
         */
        private void printAddressLine(final Vrw32alp svf, final String val, final boolean islast, final String field) {
            if (null == val || islast) {
                return;
            }
            svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + getMS932ByteLength(val));
        }

        /**
         * 学籍履歴項目のタイトルを印刷します。
         * @param student
         */
        private void printGradeRecTitle(final Student student) {
            VrsOut("GRADENAME1", student._title);

            if (param().isGakunenSei()) {
                VrsOut("GRADENAME2", student._title);
            }
        }

        /**
         * 学籍履歴を印刷します。
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(
                final int i,
                final Gakuseki gakuseki,
                final SchoolInfo school
        ) {
            // 学年
            if (param().isKoumokuGakunen()) {
                VrsOut("GRADE2_" + i, gakuseki._gakunenSimple);
            } else {
                if (param()._isNewForm) {
                    VrsOut("GRADE2_" + i, gakuseki._nendo);
                } else {
                    final String[] nendoArray = gakuseki.nendoArray();
                    VrsOut("GRADE1_" + i, nendoArray[0]);
                    VrsOut("GRADE2_" + i, nendoArray[1]);
                    VrsOut("GRADE3_" + i, nendoArray[2]);
                }
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            VrsOut(hrClassField + i, gakuseki._hrname);

            // 整理番号
            VrsOut("ATTENDNO_" + i, gakuseki._attendno);

            //
            VrsOut("YEAR_" + i, KNJ_EditDate.setNendoFormat(db2, gakuseki._nendo, null));
            if (param().isGakunenSei()) {
                VrsOut("GRADE_" + i, gakuseki._gakunenSimple);
            }

            // 校長氏名
            printStaffName("1", i, true, gakuseki._principal, gakuseki._principal1, gakuseki._principal2);

            // 担任氏名
            printStaffName("2", i, false, Staff.Null, gakuseki._staff1, gakuseki._staff2);

            //印影
            log.debug("印影："+param()._inei);
            if (null != param()._inei) {
                log.debug("改竄："+gakuseki._kaizanFlg);
                log.debug("署名（校長）："+gakuseki._principalSeq);
                log.debug("署名（担任）："+gakuseki._staffSeq);
                //改竄されていないか？
                if (null == gakuseki._kaizanFlg) {
                    //署名（校長）しているか？
                    if (null != gakuseki._principalSeq) {
                        final String str;
                        if (null == gakuseki._principal1._staffMst._staffcd) {
                            str = getImageFile(gakuseki._principal._stampNo, school);
                        } else {
                            str = getImageFile(gakuseki._principal1._stampNo, school);
                        }
                        if (str != null) {
                            if (param()._isColorPrint != null) {
                                VrsOut("STAFFBTM_1_" + i + "C", str); // 校長印
                            } else {
                                VrsOut("STAFFBTM_1_" + i, str); // 校長印
                            }
                        }
                    }
                    //署名（担任）しているか？
                    if (null != gakuseki._staffSeq) {
                        String str = getImageFile(gakuseki._staff1._stampNo, school);
                        if (str != null) {
                            if (param()._isColorPrint != null) {
                                VrsOut("STAFFBTM_2_" + i + "C", str); // 担任印
                            } else {
                                VrsOut("STAFFBTM_2_" + i, str); // 担任印
                            }
                        }
                    }
                }
            }
        }

        private void printStaffName(final String j, final int i, final boolean isCheckStaff0, final Staff staff0, final Staff staff1, final Staff staff2) {
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff0.getNameString();
                VrsOut("STAFFNAME_" + j + "_" + i + (getMS932ByteLength(name) > 20 ? "_1" : ""), name);
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List line = new ArrayList();
                line.addAll(staff1._staffMst.getNameLine(staff1._year));
                if (line.size() == 2) {
                    VrsOut("STAFFNAME_" + j + "_" + i + "_3", (String) line.get(0));
                    VrsOut("STAFFNAME_" + j + "_" + i + "_4", (String) line.get(1));
                } else {
                    final String name = staff1.getNameString();
                    VrsOut("STAFFNAME_" + j + "_" + i + (getMS932ByteLength(name) > 20 ? "_1" : ""), name);
                }
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List line = new ArrayList();
                line.addAll(staff2.getNameBetweenLine());
                line.addAll(staff1.getNameBetweenLine());
                if (line.size() == 2) {
                    VrsOut("STAFFNAME_" + j + "_" + i + "_3", (String) line.get(0));
                    VrsOut("STAFFNAME_" + j + "_" + i + "_4", (String) line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        VrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), (String) line.get(k));
                    }
                }
            }
        }

        /**
         * 進学先・就職先等の情報を印刷します。
         * @param student
         */
        private void printAfterGraduatedCourse(final Student student) {
            final List<String> textList = student._afterGraduatedCourseTextList;
            for (int i = 0; i < textList.size(); i++) {
                final String line = textList.get(i);
                final String field = "AFTER_GRADUATION" + String.valueOf(i + 1) + (getMS932ByteLength(line) > 50 ? "_2" : "") ;
                VrsOut(field, line);
            }
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String filename, final SchoolInfo school) {
            if (null == param()._documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == school._imageDir) {
                return null;
            }
            if (null == school._imageExt) {
                return null;
            }
            if (null == filename) {
                return null;
            }
            StringBuffer stb = new StringBuffer();
            stb.append(param()._documentRoot);
            stb.append("/");
            stb.append(school._imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(school._imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

        /**
         * SVF-FORM 印刷処理 初期印刷
         */
        private void printSvfDefault(final Student student) {
            try {
                final String setDateFormat = KNJ_EditDate.setDateFormat(db2, null, param()._year);
                VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(null) + "生");
                VrsOut("J_GRADUATEDDATE_Y", setDateFormat);
                VrsOut("ENTERDATE1", setDateFormat);
                VrsOut("TRANSFER_DATE_1", setDateFormat);
                VrsOut("TRANSFER_DATE_2", setDateFormat);
                VrsOut("TRANSFER_DATE3_1", setDateFormat + "\uFF5E" + setDateFormat);
                VrsOut("TRANSFER_DATE_4", setDateFormat);
                for (int i = 0; i < 4; i++) {
                    VrsOut("YEAR_" + (i + 1), KNJ_EditDate.setNendoFormat(db2, null, param()._year));
                }
                if (param().isGakunenSei()) {
                    if (student._gakusekiList.size() > 0) {
                        final Gakuseki gakuseki = student._gakusekiList.get(0);
                        if (NumberUtils.isDigits(gakuseki._gradeCd)) {
                            final String setNendoFormat = KNJ_EditDate.setNendoFormat(db2, null, gakuseki._year);
                            for (int g = 1; g <= Integer.parseInt(gakuseki._gradeCd); g++) {
                                VrsOut("YEAR_" + String.valueOf(g), setNendoFormat);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.debug("printSvfDefault error!", ex);
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 修得単位の記録。
     * 2004/11/29 yamashiro 教科名を左右の欄に跨って出力しない
     * 2006/04/13 yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001
     * ・データが無い場合もフォームを出力する --NO001
     */
    private static class KNJA130_2 extends KNJA130_0 {

        private servletpack.KNJZ.detail.KNJSvfFieldModify svfobj = new servletpack.KNJZ.detail.KNJSvfFieldModify(); // フォームのフィールド属性変更
        /** 1列目までの行数 */
        private final int MAX_LINE1;
        /** 2列目までの行数 */
        private final int MAX_LINE2;

        private KNJSvfFieldInfo _name;

        KNJA130_2(final DB2UDB db2, final Vrw32alp svf, final Param param) {
            super(db2, svf, param);
//            MAX_LINE1 = param()._isNewForm ? 35 : 19;
//            MAX_LINE2 = MAX_LINE1 + (param()._isNewForm ? 35 : 19);
//            MAX_LINE_PER_PAGE = MAX_LINE2 + (param()._isNewForm ? 31 : 15);
            MAX_LINE1 = param()._isNewForm ? 35 : 35;
            MAX_LINE2 = MAX_LINE1 + (param()._isNewForm ? 35 : 35);
        }

        public String getForm(final Student student) {
            final String form;
            if (param()._isNewForm) {
                if (param()._is3YearsSystem) {
                    form = "KNJA130_12B_2.frm";
                } else {
                    form = "KNJA130_2B_2.frm";
                }
                _name = new KNJSvfFieldInfo(); // 名前欄なし
            } else {
                if (param()._is3YearsSystem) {
                    form = "KNJA130_12B.frm";
                } else {
                    form = "KNJA130_2B.frm";
                }
                _name = new KNJSvfFieldInfo(501, 1235, KNJSvfFieldModify.charSizeToPixel(11.0), 561, 531, 591, 24, 48);
            }
            return form;
        }

        public void setDetail(final Student student) {
            final List<PrintGakuseki> printGradeRecList = getPrintGradeRecList(student);

            final Set<String> allYearSet = new HashSet<String>();
            for (final PrintGakuseki pg : printGradeRecList) {
                allYearSet.addAll(pg.getYearSet());
                final String sogoSubclassname = student.getSogoSubclassname(param(), pg._gakusekiMap, "0");

                final Map<String, StudyRecYearTotal> studyrecyear = student.getStudyRecYear(param());
                final Map<StudyRec.KIND, List<Integer>> kindCreditMap = StudyRecYearTotal.getKindCreditListMap(param(), studyrecyear, allYearSet, new StudyRec.KIND[] {StudyRec.KIND.SOGO90, StudyRec.KIND.SOGO90COMP, StudyRec.KIND.ABROAD}, new StudyRec.KIND[] {StudyRec.KIND.JIRITSU});

                final boolean hasJiritsu = kindCreditMap.keySet().contains(StudyRec.KIND.JIRITSU);
                /** 1ページの最大行数 (3列目までの行数) */
                final int MAX_LINE_PER_PAGE = MAX_LINE2 + (param()._isNewForm ? 31 : 31) - (hasJiritsu ? 2 : 0);
                final List<PrintLine> printLineList = setStudyDetail(hasJiritsu, MAX_LINE_PER_PAGE, student, allYearSet, param());
                final List<List<PrintLine>> pageList = getPageList(printLineList);
                if (param()._isOutputDebug) {
                    log.info(" printLine size = " + printLineList.size() + ", pageList size = " + pageList.size());
                }

                final String form = getForm(student);

                if (param()._isOutputDebug) {
                    log.info(" form = " + form);
                }

                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List<PrintLine> currentPage = pageList.get(pi);

                    VrSetForm(form, 4);

                    printGradeRecSub(svf, student, pg);
                    for (final Gakuseki gakuseki : pg._gakusekiMap.values()) {
                        final Integer j = pg._yearPositionMap.get(gakuseki._year);
                        printGradeRecDetail(svf, j.intValue(), gakuseki);
                    }

                    if (param()._isOutputDebug) {
                        log.info(" page idx " + pi + ", line size = " + currentPage.size());
                    }
                    for (int i = 0; i < currentPage.size(); i++) {
                        final PrintLine printLine = currentPage.get(i);
//                		if (param()._isOutputDebug) {
//                			log.info(" " + i + " = " + printLine);
//                		}
                        VrsOut("EDU_DIV2", printLine._edudiv2);
                        if (null != printLine._edudiv) {
                            VrsOut("EDU_DIV", printLine._edudiv);
                        }
                        if (null != printLine._classname) {
                            VrsOut("CLASSNAME", printLine._classname);
                        }
                        if (null != printLine._subclassname) {
                            VrsOut("SUBCLASSNAME", printLine._subclassname);
                            if (null != printLine._subclassnameAttribute) {
                                svf.VrAttribute("SUBCLASSNAME", printLine._subclassnameAttribute);
                            }
                        }
                        if (null != printLine._credit) {
                            VrsOut("CREDIT", printLine._credit);
                        }
                        VrsOut("CLASSNAME2", printLine._classname2); // 教科コード
                        VrEndRecord();
                    }

                    printTotalCredits(student, sogoSubclassname, kindCreditMap);
                }
                nonedata = true;
            }
        }

        private List<List<PrintLine>> getPageList(final List<PrintLine> printLineList) {
            final List<List<PrintLine>> pageList = new ArrayList<List<PrintLine>>();
            List<PrintLine> current = null;
            for (int i = 0; i < printLineList.size(); i++) {
                final PrintLine printLine = printLineList.get(i);
                if (null == current || printLine._linex == 0) {
                    current = new ArrayList<PrintLine>();
                    pageList.add(current);
                }
                current.add(printLine);
            }
            return pageList;
        }

        private List<PrintLine> setStudyDetail(final boolean hasJiritsu, final int MAX_LINE_PER_PAGE, final Student student, final Set<String> allYearSet, final Param param) {
            String specialDiv = "00";
            int linex = 0;
            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = student.getStudyRecSubclassMap(param, allYearSet);

            final List<StudyRecSubclassTotal> studyRecSubclassList = new ArrayList(studyRecSubclassMap.values());
            Collections.sort(studyRecSubclassList);
            if (param._isOutputDebug) {
                log.info(" studyrecSubclassList size = " + studyRecSubclassList.size());
            }

            final List<PrintLine> printLineList = new ArrayList<PrintLine>();

            final List<StudyrecTotalSpecialDiv> studyrecTotalSpecialDivList = getStudyrecTotalSpecialDivList(studyRecSubclassList, param);
            for (final StudyrecTotalSpecialDiv studyrectotalSpecialDiv : studyrecTotalSpecialDivList) {
                if (studyrectotalSpecialDiv.isAllDropped()) {
                    if (param._isOutputDebug) {
                        log.info(" skip specialDiv : " + studyrectotalSpecialDiv);
                    }
                    continue;
                }

                specialDiv = studyrectotalSpecialDiv._specialDiv;
                final String s_specialname = param.getSpecialDivName(specialDiv);
                final List<String> list_specialname = toCharStringList(s_specialname); // 普通・専門名のリスト

                for (final StudyrecTotalClass studyrectotalClass : studyrectotalSpecialDiv._classes) {
                    // 総合的な学習の時間・留学は回避します。
                    if (_90.equals(studyrectotalClass._classcd) || _ABROAD.equals(studyrectotalClass._classname) || studyrectotalClass.isJiritsu(param) || studyrectotalClass.isAllDropped()) {
                        if (param._isOutputDebug) {
                            log.info(" skip class : " + studyrectotalClass);
                        }
                        continue;
                    }

                    final String classcd = studyrectotalClass._classcd; // 教科コードの保存
                    final List<String> list_classname = toCharStringList(studyrectotalClass._classname); // 教科名のリスト

                    final List<Object[]> list_subclass = new LinkedList<Object[]>();
                    for (final StudyrecTotalSubclass studyrectotalSubclass : studyrectotalClass._subclasses) {
                        if (studyrectotalSubclass.isAllDropped()) {
                            if (param._isOutputDebug) {
                                log.info(" skip dropped : " + studyrectotalSubclass);
                            }
                            continue;
                        }

                        for (final StudyRecSubclassTotal sst : studyrectotalSubclass._totals) {
                            list_subclass.add(new Object[]{getSubclasscd(sst.studyrec(), param), sst.subClassName(), sst.credit(), sst._compCredit});
                        }
                    }

                    // 教科名文字数と科目数で多い方を教科の行数にする。教科間の科目が続く場合は、空行を出力する [[最終行の扱い次第では代替処理その2を使用]]
                    final int nameline = list_classname.size() <= list_subclass.size() ? (list_subclass.size() + 1) : list_classname.size();

                    // 教科が次列に跨らないために、空行を出力する
                    if ((linex < MAX_LINE1 && MAX_LINE1 < linex + nameline) ||
                        (idxin(MAX_LINE1, linex, MAX_LINE2) && MAX_LINE2 < linex + nameline) ||
                        (idxin(MAX_LINE2, linex, MAX_LINE_PER_PAGE) && MAX_LINE_PER_PAGE < linex + nameline)) {
                        final int max = (linex < MAX_LINE1) ? MAX_LINE1 : (linex < MAX_LINE2) ? MAX_LINE2 : MAX_LINE_PER_PAGE;
                        for (int j = linex; j < max; j++) {
                            final PrintLine printLine = nextLine(printLineList, linex);
                            printLine._edudiv2 = specialDiv;
                            linex++;
                        }
                    }

                    for (int i = 0; i < nameline; i++) {
                        final PrintLine printLine = nextLine(printLineList, linex);
                        if (0 < list_specialname.size()) {
                            printLine._edudiv = str(list_specialname.remove(0)); // 普通・専門名
                        }
                        if (i < list_classname.size()) {
                            printLine._classname = str(list_classname.get(i)); // 教科名
                        }
                        if (i < list_subclass.size()) {
                            final Object[] subclass = list_subclass.get(i);
//                            final String subclasscd = (String) subclass[0];
                            final String subclassname = (String) subclass[1];
                            final Integer credit = (Integer) subclass[2];
                            final Integer compCredit = (Integer) subclass[3];

                            printLine._subclassnameAttribute = svfFieldAttribute(MAX_LINE_PER_PAGE, subclassname, linex); // SVF-FIELD属性変更のメソッド

                            printLine._subclassname = subclassname; // 科目名

                            String creVal = "";
                            if (credit != null) {
                                if (credit.intValue() == 0) {
                                    if (null != compCredit && compCredit.intValue() > 0) {
                                        // 履修単位数
                                        creVal = "(" + compCredit.intValue() + ")";
                                    }
                                } else {
                                    // 修得単位数
                                    creVal = credit.toString();
                                }
                            }

//                            boolean isOutputCredit = false;
//
//                            if (null != subclasscd) {
//                                final String substBikoZenbu = student._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ZENBU, null, null).toString();
//                                final String substBikoIchibu = student._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ICHIBU, null, null).toString();
//                                if (!StringUtils.isBlank(substBikoZenbu)) {
//                                    final List biko = getTokenList(substBikoZenbu, 10, 2); // 全部代替科目備考
//                                    for (int j = 0; j < biko.size(); j++) {
//                                        VrsOut("CREDIT" + (2 + j), str(biko.get(j)));
//                                    }
//                                    isOutputCredit = true;   // 全部代替科目備考を表示する場合、修得単位数は表示しない
//                                } else if (!StringUtils.isBlank(substBikoIchibu)) {
//                                    VrsOut("CREDIT4_1", creVal);
//                                    final List biko = getTokenList(substBikoIchibu, 14, 2); // 一部代替科目備考
//                                    for (int j = 0; j < biko.size(); j++) {
//                                        VrsOut("CREDIT4_" + (2 + j), str(biko.get(j)));
//                                    }
//                                    isOutputCredit = true;
//                                }
//                            }
//                            if (!isOutputCredit) {
                                printLine._credit = creVal;
//                            }
                        }

                        printLine._edudiv2 = specialDiv;
                        printLine._classname2 = classcd; // 教科コード
                        linex++;
                    }

                    if (linex == MAX_LINE_PER_PAGE) {
                        linex = 0;
                    }
                }

                // 普通・専門名文字数
                if (0 != list_specialname.size()) {
                    final int nameline = list_specialname.size();
                    for (int i = 0; i < nameline; i++) {
                        final PrintLine printLine = nextLine(printLineList, linex);
                        printLine._edudiv = str(list_specialname.get(i)); // 普通・専門名
                        printLine._edudiv2 = specialDiv;
                        linex++;
                    }
                    // 普通・専門名のリストを削除する
                    list_specialname.clear();
                    if (linex == MAX_LINE_PER_PAGE) {
                        linex = 0;
                    }
                }
            }
            if (param._isOutputDebug) {
                log.info(" output printLineList size = " + printLineList.size());
            }
            final int max = (linex <= MAX_LINE_PER_PAGE ? 0 : 105) + MAX_LINE_PER_PAGE;
            for (int i = linex; i < max; i++) {
                final PrintLine printLine = nextLine(printLineList, linex);
                printLine._edudiv2 = specialDiv;
                printLine._classcd = ""; // 教科コード
                linex += 1;
            }
            return printLineList;
        }

        private static PrintLine nextLine(final List<PrintLine> printLineList, final int linex) {
            final PrintLine pl = new PrintLine(linex);
            printLineList.add(pl);
            return pl;
        }

        private static class PrintLine {
            final int _linex;
            String _edudiv2;
            String _edudiv;
            String _classcd;
            String _classname;
            String _classname2;
            String _subclassname;
            String _subclassnameAttribute;
            String _credit;
            public PrintLine(final int linex) {
                _linex = linex;
            }
            public String toString() {
                return "PrintLine(" + _linex + ", " + _classcd + ", "  + _classname + ", " + _subclassname + ")";
            }
        }

        private static boolean idxin(final int minidx, final int i, final int length) {
            return minidx <= i && i < length;
        }

        private static List<String> toCharStringList(final String s) {
            final List<String> rtn = new LinkedList<String>();
            if (null != s) {
                for (int i = 0; i < s.length(); i++) {
                    rtn.add(s.substring(i, i + 1));
                }
            }
            return rtn;
        }

        private static String str(final Object o) {
            return (null == o) ? null : o.toString();
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル・学籍履歴）を印刷します。
         * @param svf
         * @param student
         */
        private List<PrintGakuseki> getPrintGradeRecList(final Student student) {
            final List<PrintGakuseki> list = new ArrayList<PrintGakuseki>();
            final String lastyear = student.getLastYear();
            final int max = param()._is3YearsSystem ? 3 : 4;
            boolean createnew = true;
            PrintGakuseki pg = null;
            int i = 1;
            for (final Gakuseki gakuseki : student._gakusekiList) {
                final int j = getGradeColumnNum(i, gakuseki);
                if (createnew) {
                    pg = new PrintGakuseki();
                    list.add(pg);
                    createnew = false;
                }
                pg._yearList.add(gakuseki._year);
                pg._gakusekiMap.put(gakuseki._year, gakuseki);
                pg._yearPositionMap.put(gakuseki._year, j);
                final boolean islastyear = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                if (!islastyear && gakuseki._isDrop) {
                    createnew = true;
                    i = 1;
                } else if (!islastyear && max == i) {
                    createnew = true;
                    i = 1;
                } else {
                    i++;
                }
            }
            if (param()._isOutputDebug) {
                log.info(debugCollectionToStr(" list ", list, ",") + " / " + debugCollectionToStr("gakusekiList", student._gakusekiList, ","));
            }
            return list;
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub(final Vrw32alp svf, final Student student, final PrintGakuseki printGakuseki) {
            VrsOut("GRADENAME", student._title);

            printName(svf, student, "NAME1", "NAME2", "NAME3", _name);
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(final Vrw32alp svf, final int i, final Gakuseki gakuseki) {
            if (param().isKoumokuGakunen()) {
                VrsOut("GRADE1_" + i, gakuseki._gakunenSimple);
            } else {
                VrsOut("GRADE2_" + i, gakuseki._nendo);
            }
            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            VrsOut(hrClassField + i, gakuseki._hrname);
            VrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        /**
         * 修得単位数総合計を集計後印字します。（総合的な学習の時間・小計・留学・合計）
         */
        private void printTotalCredits(final Student student, final String sogoSubclassname, final Map<StudyRec.KIND, List<Integer>> kindCreditMap) {
            final GakushuBiko gakushubiko = student._gakushuBiko;

            for (final StudyRec.KIND knd : new StudyRec.KIND[] {StudyRec.KIND.SOGO90, StudyRec.KIND.ABROAD, StudyRec.KIND.JIRITSU}) {
                if (!kindCreditMap.containsKey(knd)) {
                    continue;
                }
                if (knd == StudyRec.KIND.SOGO90) {
                    final String substitutionZenbuBiko = gakushubiko.getStudyrecSubstitutionBiko(_90, StudyrecSubstitutionContainer.ZENBU, null).toString();
                    final String substitutionIchibuBiko = gakushubiko.getStudyrecSubstitutionBiko(_90, StudyrecSubstitutionContainer.ICHIBU, null).toString();
                    if (!StringUtils.isBlank(substitutionZenbuBiko)) {
//		                final List biko = getTokenList(substitutionZenbuBiko, 10, 2);
//		                for (int j = 0; j < biko.size(); j++) {
//		                    VrsOut("CREDIT" + (4 + j), (String) biko.get(j));
//		                }
                    } else if (!StringUtils.isBlank(substitutionIchibuBiko)) {
//		                final String tanni = arhasCredits[0] ? String.valueOf(arCredits[0]) : "";
//		                VrsOut("CREDIT6_1", String.valueOf(tanni));
//		                final List biko = getTokenList(substitutionIchibuBiko, 14, 2);
//		                for (int j = 0; j < biko.size(); j++) {
//		                    VrsOut("CREDIT6_" + (j + 2), (String) biko.get(j));
//		                }
                    }
//		          } else if (arhasCredits[0]) {
                    VrsOut("FOOTER_SUBCLASSNAME", sogoSubclassname);
                    final List<Integer> credits = kindCreditMap.get(knd);
                    final List<Integer> compCredits = kindCreditMap.get(StudyRec.KIND.SOGO90COMP);
                    if (!credits.isEmpty()) {
                        final Integer sum = sum(credits); // not null
                        if (sum.intValue() == 0 && !compCredits.isEmpty() && sum(compCredits).intValue() > 0) {
                            VrsOut("time", "(" + String.valueOf(sum(compCredits)) + ")");
                        } else {
                            VrsOut("time", String.valueOf(sum));
                        }
                    } else {
                        if (!compCredits.isEmpty() && sum(compCredits).intValue() > 0) {
                            VrsOut("time", "(" + String.valueOf(sum(compCredits)) + ")");
                        } else {
                            VrsOut("time", "0");
                        }
                    }
                    VrEndRecord();
//				} else if (knd == StudyRec.KIND.SYOKEI) {
//					final List<Integer> credits = kindCreditMap.get(knd);
//		            if (!credits.isEmpty()) {
//		                VrsOut("SUBTOTAL", String.valueOf(sum(credits)));
//		            } else {
//		                VrsOut("SUBTOTAL", "0");
//		            }
                } else if (knd == StudyRec.KIND.ABROAD) {
                    VrsOut("FOOTER_SUBCLASSNAME", "留学");
                    final List<Integer> credits = kindCreditMap.get(knd);
                    if (!credits.isEmpty()) {
                        VrsOut("time", String.valueOf(sum(credits)));
                    } else {
                        VrsOut("time", "0");
                    }
                    VrEndRecord();
//				} else if (knd == StudyRec.KIND.TOTAL) {
//					final List<Integer> credits = kindCreditMap.get(knd);
//		            if (!credits.isEmpty()) {
//		                VrsOut("TOTAL", String.valueOf(sum(credits)));
//		            } else {
//		                VrsOut("TOTAL", "0");
//		            }
                } else if (knd == StudyRec.KIND.JIRITSU) {
                    VrsOut("FOOTER_SUBCLASSNAME", "自立活動");
                    final List<Integer> credits = kindCreditMap.get(knd);
                    VrsOut("time", String.valueOf(sum(credits)));
                    VrEndRecord();
                }
            }
        }

        /**
         * 科目名の文字数により文字ピッチ及びＹ軸を変更します。（SVF-FORMのフィールド属性変更）
         *
         * @param subclassname:科目名
         * @param line:出力行(通算)
         */
        private String svfFieldAttribute(final int MAX_LINE_PER_PAGE, final String subclassname, final int line) {
            int ln = line + 1;
            final int pline;
            if (param()._isNewForm) {
                svfobj.width = 580; // フィールドの幅(ドット) 科目名が枠に重なるのを防ぐため、幅=650から40ほど引いた。
                svfobj.height = 98; // フィールドの高さ(ドット)
                svfobj.ystart = 845; // 開始位置(ドット)
                svfobj.minnum = 20; // 最小設定文字数
                svfobj.maxnum = 60; // 最大設定文字数
            } else {
                svfobj.width = 580; // フィールドの幅(ドット) 科目名が枠に重なるのを防ぐため、幅=650から40ほど引いた。
                svfobj.height = 98; // フィールドの高さ(ドット)
                svfobj.ystart = 845; // 開始位置(ドット)
                svfobj.minnum = 20; // 最小設定文字数
                svfobj.maxnum = 60; // 最大設定文字数
//                svfobj.width = 610; // フィールドの幅(ドット) 科目名が枠に重なるのを防ぐため、幅=650から40ほど引いた。
//                svfobj.height = 178; // フィールドの高さ(ドット)
//                svfobj.ystart = 766; // 開始位置(ドット)
//                svfobj.minnum = 20; // 最小設定文字数
//                svfobj.maxnum = 60; // 最大設定文字数
            }
            while (MAX_LINE_PER_PAGE < ln) {
                ln -= MAX_LINE_PER_PAGE;
            }
            if (MAX_LINE2 < ln) {
                pline = ln % MAX_LINE2;
            } else if (ln % MAX_LINE2 == 0) {
                pline = MAX_LINE2;
            } else if (MAX_LINE1 < ln) {
                pline = ln % MAX_LINE1;
            } else if (ln % MAX_LINE1 == 0) {
                pline = MAX_LINE1;
            } else {
                pline = ln;
            }
            svfobj.setRetvalue(subclassname, pline);
            String attribute = "";
            attribute += "Y=" + svfobj.jiku; // 開始Ｙ軸
            attribute += ",";
            attribute += "Size=" + svfobj.size; // 文字サイズ
            return attribute;
        }


        private static List<StudyrecTotalSpecialDiv> getStudyrecTotalSpecialDivList(final List<StudyRecSubclassTotal> studyRecSubclassList, final Param param) {
            final List<StudyrecTotalSpecialDiv> rtn = new ArrayList<StudyrecTotalSpecialDiv>();
            for (final StudyRecSubclassTotal studyrectotal : studyRecSubclassList) {
                if (studyrectotal.isRisyuNomi(param) || studyrectotal.isMirisyu(param) || studyrectotal.isRisyuTourokunomi(param)) {
                    // 単位不認定（履修のみ）もしくは未履修もしくは履修登録のみの場合様式1裏に表示しない
                    continue;
                }
                StudyrecTotalSpecialDiv stsd = getStudyrecTotalSpecialDiv(studyrectotal.specialDiv(), rtn, param);
                if (null == stsd) {
                    stsd = new StudyrecTotalSpecialDiv(studyrectotal.specialDiv());
                    rtn.add(stsd);
                }
                StudyrecTotalClass stc = getStudyrecTotalClass(studyrectotal.classcd(), studyrectotal.schoolKind(), stsd._classes, param);
                if (null == stc) {
                    stc = new StudyrecTotalClass(studyrectotal.classcd(), studyrectotal.schoolKind(), studyrectotal.className());
                    stsd._classes.add(stc);
                }
                StudyrecTotalSubclass sts = getStudyrecTotalSubclass(studyrectotal.curriculumCd(), studyrectotal.subClasscd(), stc._subclasses, param);
                if (null == sts) {
                    sts = new StudyrecTotalSubclass(studyrectotal.curriculumCd(), studyrectotal.subClasscd(), studyrectotal.subClassName());
                    stc._subclasses.add(sts);
                }
                sts._totals.add(studyrectotal);
            }
            return rtn;
        }

        private static StudyrecTotalSubclass getStudyrecTotalSubclass(final String curriculumCd, final String subclasscd, final List<StudyrecTotalSubclass> studyRecTotalSubclassList, final Param param) {
            StudyrecTotalSubclass rtn = null;
            for (final StudyrecTotalSubclass sts : studyRecTotalSubclassList) {
                if (sts._curriculumCd.equals(curriculumCd) && sts._subclasscd.equals(subclasscd)) {
                    rtn = sts;
                    break;
                }
            }
            return rtn;
        }

        private static StudyrecTotalClass getStudyrecTotalClass(final String classcd, final String schoolKind, final List<StudyrecTotalClass> studyRecTotalClassList, final Param param) {
            StudyrecTotalClass rtn = null;
            for (final StudyrecTotalClass stc : studyRecTotalClassList) {
                if (stc._classcd.equals(classcd) && stc._schoolKind.equals(schoolKind)) {
                    rtn = stc;
                    break;
                }
            }
            return rtn;
        }

        private static StudyrecTotalSpecialDiv getStudyrecTotalSpecialDiv(final String specialDiv, final List<StudyrecTotalSpecialDiv> studyRecTotalSpecialDivList, final Param param) {
            StudyrecTotalSpecialDiv rtn = null;
            for (final StudyrecTotalSpecialDiv stc : studyRecTotalSpecialDivList) {
                if (stc._specialDiv.equals(specialDiv)) {
                    rtn = stc;
                    break;
                }
            }
            return rtn;
        }

        private static class StudyrecTotalSubclass {
            final String _curriculumCd;
            final String _subclasscd;
            final String _subclassname;
            final List<StudyRecSubclassTotal> _totals = new ArrayList<StudyRecSubclassTotal>();
            StudyrecTotalSubclass(final String curriculumCd, final String subclasscd, final String subclassname) {
                _curriculumCd = curriculumCd;
                _subclasscd = subclasscd;
                _subclassname = subclassname;
            }
            /** データがすべて留年した年度か */
            public boolean isAllDropped() {
                return false;
//                boolean isAllDropped = true;
//                for (final Iterator it = _totals.iterator(); it.hasNext();) {
//                    final StudyRecSubclassTotal studyrecSubclassTotal = (StudyRecSubclassTotal) it.next();
//                    if (!studyrecSubclassTotal.isAllDropped()) {
//                        isAllDropped = false;
//                    }
//                }
//                return isAllDropped;
            }
        }

        private static class StudyrecTotalClass {
            final String _classcd;
            final String _schoolKind;
            final String _classname;
            final List<StudyrecTotalSubclass> _subclasses = new ArrayList<StudyrecTotalSubclass>();
            StudyrecTotalClass(final String classcd, final String schoolKind, final String classname) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _classname = classname;
            }
            public boolean isJiritsu(final Param param) {
                for (final StudyrecTotalSubclass sub : _subclasses) {
                    for (final StudyRecSubclassTotal t : sub._totals) {
                        for (final StudyRec sr : t._subclassStudyrecList) {
                            if (param._e065Name1JiritsuKatsudouSubclasscdList.contains(getSubclasscd(sr, param))) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            /** データがすべて留年した年度か */
            public boolean isAllDropped() {
                return false;
//                boolean isAllDropped = true;
//                for (final Iterator it = _subclasses.iterator(); it.hasNext();) {
//                    final StudyrecTotalSubclass studyrectotalSubclass = (StudyrecTotalSubclass) it.next();
//                    if (!studyrectotalSubclass.isAllDropped()) {
//                        isAllDropped = false;
//                    }
//                }
//                return isAllDropped;
            }
        }

        private static class StudyrecTotalSpecialDiv {
            final String _specialDiv;
            final List<StudyrecTotalClass> _classes = new ArrayList<StudyrecTotalClass>();
            StudyrecTotalSpecialDiv(final String specialDiv) {
                _specialDiv = specialDiv;
            }
            /** データがすべて留年した年度か */
            public boolean isAllDropped() {
                return false;
//                boolean isAllDropped = true;
//                for (final Iterator it = _classes.iterator(); it.hasNext();) {
//                    final StudyrecTotalClass studyrectotalClass = (StudyrecTotalClass) it.next();
//                    if (!studyrectotalClass.isAllDropped()) {
//                        isAllDropped = false;
//                    }
//                }
//                return isAllDropped;
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 学習の記録。
     * yamashiro・組のデータ型が数値でも文字でも対応できるようにする 2006/04/13
     * yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001 ・データがない場合の不具合修正 --NO001
     * ・名称マスターに表示用組( 'A021'+HR_CLASS で検索 )の処理を追加 --NO003 => 無い場合は従来通りHR_CLASSを出力
     * ・学年をすべて出力 --NO005
     */
    private static class KNJA130_3 extends KNJA130_0 {

        private KNJSvfFieldInfo _name;

        KNJA130_3(final DB2UDB db2, final Vrw32alp svf, final Param param) {
            super(db2, svf, param);
        }

        public void setForm(final Student student) {
            final String form;
            if (param()._isNewForm) {
                if (param()._is3YearsSystem) {
                    form = "KNJA130_13B_2.frm";
                } else {
                    form = "KNJA130_3B_2.frm";
                }
                _name = new KNJSvfFieldInfo(275, 1130, KNJSvfFieldModify.charSizeToPixel(11.0), 366, 338, 393, 24, 48);
            } else {
                if (param()._is3YearsSystem) {
                    form = "KNJA130_13B.frm";
                } else {
                    form = "KNJA130_3B.frm";
                }
                _name = new KNJSvfFieldInfo(421, 1155, KNJSvfFieldModify.charSizeToPixel(11.0), 325, 295, 355, 24, 48);
            }
            VrSetForm(form, 4);
        }

        private String eduDivFieldname() {
            if (param()._isPrintYoshiki2OmoteEduDiv2) {
                return "EDU_DIV_2";
            }
            return "EDU_DIV";
        }

        public void setDetail(final Student student) {
            setForm(student);
            int i = 1;
            boolean hasZeroprintflg = false;
            final List<PrintGakuseki> grademapList = new ArrayList<PrintGakuseki>();
            final String lastyear = StringUtils.defaultString(student.getLastYear());
            {
                PrintGakuseki printGakuseki = new PrintGakuseki();
                grademapList.add(printGakuseki);
                final int max = param()._is3YearsSystem ? 3 : 4;

                final List<Gakuseki> gakusekiStudyRecList = Student.createGakusekiStudyRec(db2, student._gakusekiList, student._studyRecList, param());
                boolean includeZaisekimae = false;
                for (final Gakuseki gakuseki : gakusekiStudyRecList) {
                    boolean zaisekimae = false;
                    if ("1".equals(gakuseki._dataflg) && !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                        continue;
                    } else if ("2".equals(gakuseki._dataflg)) {
                        includeZaisekimae = true;
                        zaisekimae = true;
                    }
                    final int j = zaisekimae ? max : getGradeColumnNum(i, gakuseki, includeZaisekimae);

                    printGakuseki._yearList.add(gakuseki._year);
                    printGakuseki._yearPositionMap.put(gakuseki._year, new Integer(j));
                    printGakuseki._gakusekiMap.put(gakuseki._year, gakuseki);
                    final boolean lastyearflg = lastyear.equals(gakuseki._year);
                    // 留年以降を改ページします。
                    if (zaisekimae) {
                    } else if (!lastyearflg && gakuseki._isDrop) {
                        printGakuseki._dropGakuseki = gakuseki;
                        printGakuseki = new PrintGakuseki();
                        grademapList.add(printGakuseki);
                        includeZaisekimae = false;
                        if (hasZeroprintflg) {
                            i = !NumberUtils.isDigits(gakuseki._gradeCd) ? -1 : Integer.parseInt(gakuseki._gradeCd);
                            hasZeroprintflg = false;
                        }
                    } else if (!lastyearflg && max == i) {
                        printGakuseki = new PrintGakuseki();
                        grademapList.add(printGakuseki);
                        includeZaisekimae = false;
                        i = 1;
                    } else {
                        i++;
                    }
                    if ("0".equals(gakuseki._year)) {
                        hasZeroprintflg = true; // 入学前を出力した。
                    }
                }
            }

            int MAX_LINE_PER_PAGE;

            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = student.getStudyRecSubclassMap(param(), null);
            for (final PrintGakuseki printGakuseki : grademapList) {

                final StudyRec.KIND[] footerKinds = {StudyRec.KIND.SOGO90, StudyRec.KIND.SYOKEI, StudyRec.KIND.ABROAD, StudyRec.KIND.TOTAL};
                final Map<StudyRec.KIND, List<Integer>> kindCreditListMap = StudyRecYearTotal.getKindCreditListMap(param(), student.createStudyRecYear(student._studyRecList, student._dropYears), student.gakusekiYearSet(), footerKinds, new StudyRec.KIND[] {StudyRec.KIND.JIRITSU});

                MAX_LINE_PER_PAGE = kindCreditListMap.containsKey(StudyRec.KIND.JIRITSU) ? 63 : param()._isNewForm ? 65 : 65;
                List<PrintLine> printLineListAll = getPrintLineList(MAX_LINE_PER_PAGE, student, printGakuseki, studyRecSubclassMap, true);
                if (MAX_LINE_PER_PAGE < printLineListAll.size()) {
                    printLineListAll = getPrintLineList(MAX_LINE_PER_PAGE, student, printGakuseki, studyRecSubclassMap, false);
                }
                final List<List<PrintLine>> groupedList = getGroupedList(printLineListAll, MAX_LINE_PER_PAGE);

                final List<String> sikakuBikoList = student._gakushuBiko.getSikakuBikoList(printGakuseki, studyRecSubclassMap);
                if (param()._isOutputDebug) {
                    log.info(" sikakuBikolist (" + sikakuBikoList.size() + ") = " + sikakuBikoList);
                }

                for (int pi = 0; pi < groupedList.size(); pi++) {
                    final List<PrintLine> printLineList = groupedList.get(pi);
                    final boolean isLastPage = pi == groupedList.size() - 1;

                    setForm(student);
                    printGradeRecSub(svf, student);
//                    log.debug(" print gradeMap = " + gradeMap);
                    for (final String year : printGakuseki._yearPositionMap.keySet()) {
                        final Integer ii = printGakuseki._yearPositionMap.get(year);
                        final Gakuseki gakuseki = printGakuseki._gakusekiMap.get(year);
                        printGradeRecDetail(svf, ii.intValue(), gakuseki);
                    }

                    final boolean lastyearflg = printGakuseki._yearPositionMap.containsKey(lastyear);

                    if (isLastPage) {
                        int bikoPrintLineIdx = MAX_LINE_PER_PAGE - 1;
                        for (int bi = sikakuBikoList.size() - 1; bi >= 0; bi--) {
                            final String biko = sikakuBikoList.get(bi);

                            if (bikoPrintLineIdx < 0) {
                                log.warn("not set sikaku biko : " + biko);
                            } else {
                                boolean isSet = false;
                                while (0 <= bikoPrintLineIdx) {
                                    final PrintLine printLine = printLineList.get(bikoPrintLineIdx);
                                    if (null == printLine._biko) {
                                        printLine._biko = biko;
                                        isSet = true;
                                        bikoPrintLineIdx -= 1;
                                        break;
                                    }
                                    bikoPrintLineIdx -= 1;
                                }
                                if (!isSet) {
                                    log.warn("not set sikaku biko : " + biko + ", bikoLine = " + bikoPrintLineIdx + ", lineSize = " + printLineList.size());
                                }
                            }
                        }
                    }

                    for (final PrintLine printLine : printLineList) {

                        if (null != printLine._classname) {
                            VrsOut("CLASSNAME", printLine._classname);
                        }
                        VrsOut("CLASSNAME2", printLine._classname2); // 教科コード
                        VrsOut("SUBCLASSNAME", printLine._subclassname); // 科目名
                        for (final Map.Entry<String, String> e : printLine._gradesMap.entrySet()) {
                            VrsOut(e.getKey(), e.getValue());
                        }
                        for (final Map.Entry<String, String> e : printLine._creditsMap.entrySet()) {
                            VrsOut(e.getKey(), e.getValue());
                        }

                        VrsOut(eduDivFieldname(), printLine._edudiv);
                        VrsOut("EDU_DIV2", printLine._edudiv2);

                        if (null != printLine._credits) {
                            VrsOut("CREDIT", printLine._credits); // 科目別修得単位数
                        }

                        printBiko(printLine._biko);

                        VrEndRecord();
                        nonedata = true;
                    }

                    final boolean isPrintTotalCredits = param()._isPrintYoshiki2OmoteTotalCreditByPage || lastyearflg;

                    final List<String> biko90 = getBiko(param(), student, printGakuseki, null, _90, "");
                    printYearCredits(kindCreditListMap, isPrintTotalCredits, biko90, student, printGakuseki);
                }
            }
            nonedata = true;
        }

        /**
         * 指定年度の学籍を得る
         * @param year 指定年度
         * @return 学籍
         */
        private Gakuseki getGakuseki(final List<Gakuseki> gakusekiList, final String year) {
            if (year == null) {
                return null;
            }
            final List<Gakuseki> gakuList = new ArrayList<Gakuseki>();
            for (final Gakuseki gaku : gakusekiList) {
                if (year.equals(gaku._year)) {
                    gakuList.add(gaku);
                }
            }
            if (gakuList.isEmpty()) {
                return null;
            } else {
                if (gakuList.size() > 1) {
                    log.warn(" " + year + " gakuseki = " + gakuList);
                }
                return gakuList.get(0);
            }
        }

        private List<PrintLine> getPrintLineList(final int MAX_LINE_PER_PAGE, final Student student, final PrintGakuseki printGakuseki, final Map<String, StudyRecSubclassTotal> studyRecSubclassMap, final boolean isAddBlankLine) {
            final List<StudyRec> studyrecList = new ArrayList<StudyRec>();
            for (final StudyRec studyrec : student._studyRecList) {
                if (!printGakuseki._yearPositionMap.keySet().contains(studyrec._year)) {
                    continue;
                }
                studyrecList.add(studyrec);
            }
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = getStudyrecSpecialDivList(studyrecList, param());

//          int lineClasscd = 0; // 教科毎の行数

            final String lastyear = StringUtils.defaultString(student.getLastYear());
            final boolean lastyearflg = printGakuseki._yearPositionMap.containsKey(lastyear);
            final boolean isPrintTotalCredits = param()._isPrintYoshiki2OmoteTotalCreditByPage || lastyearflg;

            removeNotTarget(student, studyrecSpecialDivList);

            final List<PrintLine> printLineList = new ArrayList<PrintLine>();
            for (final StudyrecSpecialDiv studyrecSpecialDiv : studyrecSpecialDivList) {
                final List<PrintLine> printLineSpList = new ArrayList<PrintLine>();

                // 教科毎の表示
                for (final StudyrecClass studyrecClass : studyrecSpecialDiv._studyrecClassList) {
                    final List<PrintLine> printLineClsList = new ArrayList<PrintLine>();
                    // 科目毎の表示
                    for (final StudyrecSubClass studyrecSubClass : studyrecClass._studyrecSubclassList) {
                        final PrintLine printLine = nextLine(printLineClsList);

                        Integer pageSubclassCredit = null;
                        for (final StudyRec studyrec : studyrecSubClass._studyrecList) {

                            final String keysubclasscd = getSubclasscd(studyrec, param());

                            final StudyRecSubclassTotal subclassObj = studyRecSubclassMap.get(keysubclasscd);
                            printLine._subclassname = subclassObj.subClassName(); // 科目名

                            final List<String> bikoList = getBiko(param(), student, printGakuseki, studyrec, keysubclasscd, studyrec._subclassname);
                            if (bikoList.size() > 0) {
                                printLine._biko = concat(bikoList);
                            }

                            final Integer col = printGakuseki._yearPositionMap.get(studyrec._year);
                            // 学年ごとの出力
                            if (null != col && col.intValue() != 0) {
                                if (null != studyrec._grades) {
                                    if ("1".equals(param()._seitoSidoYorokuHyotei0ToBlank) && studyrec._grades.intValue() == 0) {
                                    } else {
                                        printLine._gradesMap.put("GRADES" + col.intValue(), studyrec._grades.toString()); // 評定
                                    }
                                }
                                if (null != studyrec._credit) {
                                    final String creditVal;
                                    if (studyrec._credit.intValue() == 0) {
                                        if (null != studyrec._compCredit && studyrec._compCredit.intValue() > 0) {
                                            creditVal = "(" + studyrec._compCredit.toString() + ")";
                                        } else {
                                            creditVal = studyrec._credit.toString();
                                        }
                                    } else {
                                        creditVal = studyrec._credit.toString();
                                    }
                                    printLine._creditsMap.put("CREDIT" + col.intValue(), creditVal); // 単位
                                    if (!student._dropYears.contains(studyrec._year)) {
                                        pageSubclassCredit = null == pageSubclassCredit ? studyrec._credit : new Integer(pageSubclassCredit.intValue() + studyrec._credit.intValue());
                                    }
                                }
                            }
                        }


                        for (final StudyRec studyrec : studyrecSubClass._studyrecList) {

                            final String keysubclasscd = getSubclasscd(studyrec, param());

                            if (isPrintTotalCredits) {
                                if (param()._isPrintYoshiki2OmoteTotalCreditByPage) {
                                    if (null != pageSubclassCredit) {
                                        printLine._credits = pageSubclassCredit.toString(); // 科目別修得単位数
                                    }
                                } else {
                                    final StudyRecSubclassTotal subclassObj = studyRecSubclassMap.get(keysubclasscd);
                                    if (null != subclassObj && null != subclassObj.credit() && subclassObj.credit().intValue() != 0) {
                                        printLine._credits = subclassObj.credit().toString(); // 科目別修得単位数
                                    }
                                }
                            }
                        }
                    }

                    // 教科名
                    final List<String> split = split(studyrecClass._className, 1);
                    final String keyclasscd = studyrecClass._classcd + "-" + studyrecClass._schoolKind;
                    for (int i = 0; i < printLineClsList.size(); i++) {
                        PrintLine printLine = printLineClsList.get(i);
                        if (i < split.size()) {
                            printLine._classname = split.get(i); // 教科名
                        }
                        printLine._classname2 = keyclasscd;
                    }
                    boolean outputNokori = false;
                    for (int i = printLineClsList.size(); i < split.size(); i++) {
                        final PrintLine printLine = nextLine(printLineClsList);
                        printLine._classname = split.get(i); // 教科名
                        printLine._classname2 = keyclasscd;
                        outputNokori = true;
                    }
                    if (isAddBlankLine) {
                        if (!outputNokori) {
                            if (printLineClsList.size() != MAX_LINE_PER_PAGE) {
                                PrintLine printLine = nextLine(printLineClsList);
                                printLine._classname2 = studyrecClass._classcd; // 教科コード
                            }
                        }
                    }
                    printLineSpList.addAll(printLineClsList);
                }

                // 普通・専門毎の行数
                final List<String> split = split(param().getSpecialDivName(studyrecSpecialDiv._specialDiv), param()._isPrintYoshiki2OmoteEduDiv2 ? 2 : 1);
                for (int i = 0; i < printLineSpList.size(); i++) {
                    PrintLine printLine = printLineSpList.get(i);
                    if (i < split.size()) {
                        printLine._edudiv = split.get(i);
                    }
                    printLine._edudiv2 = studyrecSpecialDiv._specialDiv;
                }
                for (int i = printLineSpList.size(); i < split.size(); i++) {
                    final PrintLine printLine = nextLine(printLineSpList);
                    printLine._edudiv = split.get(i);
                    printLine._edudiv2 = studyrecSpecialDiv._specialDiv;
                }
                printLineList.addAll(printLineSpList);
            }
            if (printLineList.size() == 0 || 0 < printLineList.size() && printLineList.size() % MAX_LINE_PER_PAGE > 0) {
                String specialDiv = "";
                if (0 < studyrecSpecialDivList.size()) {
                    specialDiv = studyrecSpecialDivList.get(studyrecSpecialDivList.size() - 1)._specialDiv;
                } else {
                    specialDiv = "1";
                }
                int linex = printLineList.size() % MAX_LINE_PER_PAGE;
                for (; linex < MAX_LINE_PER_PAGE; linex++) {
                    final PrintLine printLine = nextLine(printLineList);
                    printLine._edudiv2 = specialDiv;
                    printLine._classname2 = ""; // 教科コード
                }
            }
            return printLineList;
        }

        private List<String> split(final String name, final int splitSize) {
            final List<String> split = new ArrayList<String>();
            for (int i = 0; i < name.length(); i += splitSize) {
                final String sub = name.substring(i, Math.min(name.length(), i + splitSize));
                split.add(sub);
            }
            return split;
        }

        private void removeNotTarget(final Student student, final List<StudyrecSpecialDiv> studyrecSpecialDivList) {
            for (final StudyrecSpecialDiv studyrecSpecialDiv : studyrecSpecialDivList) {
                for (final Iterator<StudyrecClass> it = studyrecSpecialDiv._studyrecClassList.iterator(); it.hasNext();) {
                    final StudyrecClass studyrecClass = it.next();
                    // 総合的な学習の時間・留学は回避します。
                    if (_90.equals(studyrecClass._classcd)) {
                        // 科目毎の表示
                        for (final StudyrecSubClass studyrecSubClass : studyrecClass._studyrecSubclassList) {
                            for (final StudyRec studyrec : studyrecSubClass._studyrecList) {
                                final String compVal = getRisyuTanniBiko(param(), student, studyrec);
                                if (!"".equals(compVal)) {
                                    student._gakushuBiko.putRisyuTanniBiko(_90, studyrec._year, compVal);
                                }
                            }
                        }
                        it.remove();
                        continue;
                    } else if (_ABROAD.equals(studyrecClass._className)) {
                        it.remove();
                        continue;
                    } else if (studyrecClass.isJiritsu(param())) {
                        it.remove();
                        continue;
                    }
                }
            }
        }

        private static class PrintLine {
            final int _linex;
            String _edudiv;
            String _edudiv2;
            String _classname;
            String _classname2;
            String _subclassname;
            String _biko;
            Map<String, String> _gradesMap = new HashMap<String, String>();
            Map<String, String> _creditsMap = new HashMap<String, String>();
            String _credits;
            PrintLine(final int linex) {
                _linex = linex;
            }
        }

        private static PrintLine nextLine(final List<PrintLine> printLineList) {
            final PrintLine pl = new PrintLine(printLineList.size());
            printLineList.add(pl);
            return pl;
        }

        private void printBiko(final String biko) {
            for (int j = 1; j <= 5; j++) {
                VrsOut("biko" + j, ""); // クリア処理
            }
            final int len = getMS932ByteLength(biko);

            if (len != 0) {
                final int i;
                if (len <= 40) {
                    i = 1; // 40桁フィールド
                } else if (len <= 60) {
                    i = 2; // 60桁フィールド
                } else if (len <= 80) {
                    i = 3; // 80桁フィールド
                } else if (len <= 100) {
                    i = 4; // 100桁フィールド
                } else {
                    i = 5; // 240桁フィールド
                }
                VrsOut("biko" + i, biko);
            }
        }

        private static List<String> getBiko(final Param param, final Student student, final PrintGakuseki printGakuseki, final StudyRec studyrec, final String keysubclasscd, final String subclassname) {
            if (null != studyrec) {
                final String compVal = getRisyuTanniBiko(param, student, studyrec);
                if (!"".equals(compVal)) {
                    student._gakushuBiko.putRisyuTanniBiko(keysubclasscd, studyrec._year, compVal);
                }
            }
            final List<String> rtn = new ArrayList<String>();
            final List<String> gakushuBikoList = student._gakushuBiko.getStudyrecBikoList(keysubclasscd, subclassname, printGakuseki);
            final String gakushuBiko = gakushuBikoList.size() > 0 ? (String) gakushuBikoList.get(0) : "";
            final String studyrecSubstitutionBikoZenbu = student._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, StudyrecSubstitutionContainer.ZENBU, printGakuseki).toString();
            final String studyrecSubstitutionBikoIchibu = student._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, StudyrecSubstitutionContainer.ICHIBU, printGakuseki).toString();
            final String substBiko = studyrecSubstitutionBikoZenbu.equals(studyrecSubstitutionBikoIchibu) ? studyrecSubstitutionBikoZenbu : studyrecSubstitutionBikoZenbu + studyrecSubstitutionBikoIchibu;
            final String rishuTanniBiko = student._gakushuBiko.getRisyuTanniBiko(keysubclasscd, printGakuseki).toString();
            final String biko = rishuTanniBiko.toString() + gakushuBiko + substBiko;
            if (param._isOutputDebug) {
                if (!StringUtils.isBlank(rishuTanniBiko)) {
                    log.info(" rishuTanniBiko = " + rishuTanniBiko);
                }
                if (!StringUtils.isBlank(gakushuBiko)) {
                    log.info(" gakushuBiko = " + gakushuBiko);
                }
                if (!StringUtils.isBlank(substBiko)) {
                    log.info(" substBiko = " + substBiko);
                }
            }
            if (!StringUtils.isBlank(biko)) {
                rtn.add(biko);
            }
            for (int i = 1; i < gakushuBikoList.size(); i++) {
                rtn.add(gakushuBikoList.get(i));
            }
            return rtn;
        }
        private static String getRisyuTanniBiko(final Param param, final Student student, final StudyRec studyrec) {
            final String rtn;
            final String head = getRisyuTanniBikoHead(param, student, studyrec); // 第○学年 or ○○○年度
            final String compCre = null == studyrec._compCredit ? "" : studyrec._compCredit.toString();
            if (studyrec.isMirisyu(param)) {
                // 未履修の場合の備考処理
                if (null != param._mirisyuRemarkFormat) {
                    rtn = formatRemark(param._mirisyuRemarkFormat, head, compCre);
                } else {
                    rtn = "";
                }
            } else if (studyrec.isRisyuNomi(param)) {
                // 履修のみの場合の備考処理
                if (null != param._risyunomiRemarkFormat) {
                    rtn = formatRemark(param._risyunomiRemarkFormat, head, compCre);
                } else {
                    rtn = "";
                }
            } else {
                rtn = "";
            }
            return rtn;
        }

        private static String formatRemark(final String format, final String gakunenNendo, final String compCre) {
            String tmp = format;
            tmp = StringUtils.replace(tmp, "x", gakunenNendo);
            tmp = StringUtils.replace(tmp, "y", compCre);
            return tmp;
        }

        private static String getRisyuTanniBikoHead(final Param param, final Student student, final StudyRec studyrec) {
            final Gakuseki gakuseki = student.getGakuseki(studyrec._year, studyrec._annual);
            if (gakuseki == null) {
                return null;
            }
            final String head = param.isKoumokuGakunen() ? gakuseki._gradeName2 : gakuseki.getNendo2();
            return null == head ? "" : head;
        }

        /**
         * 年度・学年別修得単位数を印字します。（総合的な学習の時間・小計・留学・合計）
         * @param gakusyuubikomap
         * @param lastyearflg
         * @param studyrecyear
         * @param isGrd
         * @param grademap
         */
        private void printYearCredits(final Map<StudyRec.KIND, List<Integer>> kindCreditListMap, final boolean isPrintTotalCredits, final List<String> bikoList, final Student student, final PrintGakuseki printgakuseki) {

            final StudyRec.KIND[] footerKinds;
            if (kindCreditListMap.containsKey(StudyRec.KIND.JIRITSU)) {
                footerKinds = new StudyRec.KIND[] {StudyRec.KIND.SOGO90, StudyRec.KIND.JIRITSU, StudyRec.KIND.SYOKEI, StudyRec.KIND.ABROAD, StudyRec.KIND.TOTAL};
            } else {
                footerKinds = new StudyRec.KIND[] {StudyRec.KIND.SOGO90, StudyRec.KIND.SYOKEI, StudyRec.KIND.ABROAD, StudyRec.KIND.TOTAL};
            }

            final int totalCol = 5;
            final String sogoSubclassname = student.getSogoSubclassname(param(), printgakuseki._gakusekiMap, "0");
            final Map<String, StudyRecYearTotal> studyrecyear = student.getStudyRecYear(param());
            for (final StudyRec.KIND kind : footerKinds) {
                for (final String year : printgakuseki._yearPositionMap.keySet()) {
                    final StudyRecYearTotal yearTotal = studyrecyear.get(year);
                    final int col = printgakuseki._yearPositionMap.get(year).intValue();
                    final List<Integer> creditList = null == yearTotal ? Collections.EMPTY_LIST : yearTotal.getKindCreditList(param(), kind);
                    final List<Integer> totalCredits = kindCreditListMap.get(kind);
                    final boolean isDrop = student._dropYears.contains(year);
                    if (kind == StudyRec.KIND.SOGO90) {
                        VrsOut("SOGO_SUBCLASSNAME", sogoSubclassname);
                        if (null != yearTotal && !creditList.isEmpty()) {
                            VrsOut("tani_" + col + "_sgj", sum(creditList).toString());
                        }
                        if (bikoList.size() > 0) {
                            final String biko = bikoList.get(0);
                            svfVrsOutForData(new String[] {"biko_sgj", "biko_sgj2", "biko_sgj3", "biko_sgj4", "biko_sgj5"}, biko);
                        }

                        if (isPrintTotalCredits) {
                            if (!totalCredits.isEmpty()) {
                                VrsOut("tani_" + totalCol + "_sgj", String.valueOf(sum(totalCredits)));
                            }
                        }
                    } else if (kind == StudyRec.KIND.JIRITSU) {
                        VrsOut("TOTAL_SUBCLASSNAME", "自立活動");
                        if (!isDrop) {
                            if (null != yearTotal && !creditList.isEmpty()) {
                                VrsOut("tani_" + col, sum(creditList).toString());
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + col, "0");
                            }
                        }
                        if (isPrintTotalCredits) {
                            if (!totalCredits.isEmpty()) {
                                VrsOut("tani_" + totalCol, String.valueOf(sum(totalCredits)));
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + totalCol, "0");
                            }
                        }
                    } else if (kind == StudyRec.KIND.SYOKEI) {
                        VrsOut("TOTAL_SUBCLASSNAME", "小計");
                        if (!isDrop) {
                            if (null != yearTotal && !creditList.isEmpty()) {
                                VrsOut("tani_" + col, sum(creditList).toString());
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + col, "0");
                            }
                        }
                        if (isPrintTotalCredits) {
                            if (!totalCredits.isEmpty()) {
                                VrsOut("tani_" + totalCol, String.valueOf(sum(totalCredits)));
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + totalCol, "0");
                            }
                        }
                    } else if (kind == StudyRec.KIND.ABROAD) {
                        VrsOut("TOTAL_SUBCLASSNAME", "留学");
                        if (!isDrop) {
                            if (null != yearTotal && !creditList.isEmpty()) {
                                VrsOut("tani_" + col, sum(creditList).toString());
                            } else {
                                VrsOut("tani_" + col, "0");
                            }
                        }
                        if (isPrintTotalCredits) {
                            if (!totalCredits.isEmpty()) {
                                VrsOut("tani_" + totalCol, String.valueOf(sum(totalCredits)));
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + totalCol, "0");
                            }
                        }
                    } else if (kind == StudyRec.KIND.TOTAL) {
                        VrsOut("TOTAL_SUBCLASSNAME", "合計");
                        if (!isDrop) {
                            if (null != yearTotal && !creditList.isEmpty()) {
                                VrsOut("tani_" + col, sum(creditList).toString());
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + col, "0");
                            }
                        }
                        if (isPrintTotalCredits) {
                            if (!totalCredits.isEmpty()) {
                                VrsOut("tani_" + totalCol, String.valueOf(sum(totalCredits)));
                            } else if (student.isGrd()) {
                                VrsOut("tani_" + totalCol, "0");
                            }
                        }
                    }
                }

                VrEndRecord();
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub(final Vrw32alp svf, final Student student) {
            VrsOut("GRADENAME1", student._title);
            VrsOut("GRADENAME2", student._title);

            printName(svf, student, "NAME1", "NAME2", "NAME3", _name);

            SchoolInfo schoolinfo = student._schoolInfo;
            if (null != schoolinfo && null != schoolinfo._schoolName1) {
                if (getMS932ByteLength(schoolinfo._schoolName1) > 30) {
                    VrsOut("SCHOOLNAME2", schoolinfo._schoolName1);
                } else {
                    VrsOut("SCHOOLNAME1", schoolinfo._schoolName1);
                }
            }
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(final Vrw32alp svf, final int i, final Gakuseki gakuseki) {
            if (param().isKoumokuGakunen()) {
                VrsOut("GRADE1_" + i, gakuseki._gakunenSimple);
                VrsOut("GRADE2_" + i, gakuseki._gradeName2);
            } else {
                VrsOut("GRADE3_" + i, gakuseki._nendo);
                VrsOut("GRADE2_" + i, gakuseki.getNendo2());
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            VrsOut(hrClassField + i, gakuseki._hrname);
            VrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        private static List<StudyrecSpecialDiv> getStudyrecSpecialDivList(final List<StudyRec> studyrecList, final Param param) {
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = new ArrayList<StudyrecSpecialDiv>();
            for (final StudyRec studyrec : studyrecList) {
                if (null == studyrec._classcd || null == studyrec._subclasscd) {
                    continue;
                }
                final StudyrecSpecialDiv ssd = getStudyrecSpecialDiv(studyrecSpecialDivList, studyrec._specialDiv);
                final StudyrecClass sc = getStudyrecClass(ssd._studyrecClassList, studyrec._showorderClass, studyrec._classcd, studyrec._schoolKind, studyrec._classname, param);
                final StudyrecSubClass ssc = getStudyrecSubClass(sc._studyrecSubclassList, studyrec._showorderSubClass, studyrec._classcd, studyrec._schoolKind, studyrec._curriculumCd, studyrec._subclasscd, param);
                ssc._studyrecList.add(studyrec);
            }
            for (final StudyrecSpecialDiv div : studyrecSpecialDivList) {
                Collections.sort(div._studyrecClassList);
                for (final StudyrecClass studyrecClass : div._studyrecClassList) {
                    Collections.sort(studyrecClass._studyrecSubclassList);
                }
            }
            return studyrecSpecialDivList;
        }

        private static StudyrecSpecialDiv getStudyrecSpecialDiv(final List<StudyrecSpecialDiv> list, final String specialDiv) {
            StudyrecSpecialDiv studyrecSpecialDiv = null;
            for (final StudyrecSpecialDiv studyrecSpecialDiv0 : list) {
                if (specialDiv.equals(studyrecSpecialDiv0._specialDiv)) {
                    studyrecSpecialDiv = studyrecSpecialDiv0;
                    break;
                }
            }
            if (null == studyrecSpecialDiv) {
                studyrecSpecialDiv = new StudyrecSpecialDiv(specialDiv);
                list.add(studyrecSpecialDiv);
            }
            return studyrecSpecialDiv;
        }

        private static StudyrecClass getStudyrecClass(final List<StudyrecClass> list, final Integer showOrderClass, final String classcd, final String schoolKind, final String classname, final Param param) {
            StudyrecClass studyrecClass = null;
            for (final StudyrecClass studyrecClass0 : list) {
                if (classcd.equals(studyrecClass0._classcd) && schoolKind.equals(studyrecClass0._schoolKind)) {
                    studyrecClass = studyrecClass0;
                    break;
                }
            }
            if (null == studyrecClass) {
                studyrecClass = new StudyrecClass(classcd, schoolKind, classname, showOrderClass);
                list.add(studyrecClass);
            }
            return studyrecClass;
        }

        private static StudyrecSubClass getStudyrecSubClass(final List<StudyrecSubClass> list, final Integer showOrderSubclass, final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final Param param) {
            StudyrecSubClass studyrecSubClass = null;
            for (final StudyrecSubClass studyrecSubClass0 : list) {
                if (classcd.equals(studyrecSubClass0._classcd) && schoolKind.equals(studyrecSubClass0._schoolKind) && curriculumCd.equals(studyrecSubClass0._curriculumCd) && subclasscd.equals(studyrecSubClass0._subclasscd)) {
                    studyrecSubClass = studyrecSubClass0;
                    break;
                }
            }
            if (null == studyrecSubClass) {
                studyrecSubClass = new StudyrecSubClass(classcd, schoolKind, curriculumCd, subclasscd, showOrderSubclass);
                list.add(studyrecSubClass);
            }
            return studyrecSubClass;
        }

        private static class StudyrecSpecialDiv {
            final String _specialDiv;
            final List<StudyrecClass> _studyrecClassList;
            public StudyrecSpecialDiv(final String specialDiv) {
                _specialDiv = specialDiv;
                _studyrecClassList = new ArrayList<StudyrecClass>();
            }
        }

        private static class StudyrecClass implements Comparable<StudyrecClass> {
            final String _classcd;
            final String _schoolKind;
            final String _className;
            final Integer _showOrderClass;
            final List<StudyrecSubClass> _studyrecSubclassList;
            public StudyrecClass(final String classcd, final String schoolKind, final String classname, final Integer showOrderClass) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _className = classname;
                _showOrderClass = showOrderClass;
                _studyrecSubclassList = new ArrayList<StudyrecSubClass>();
            }
            public boolean isJiritsu(final Param param) {
                for (final StudyrecSubClass sub : _studyrecSubclassList) {
                    for (final StudyRec sr : sub._studyrecList) {
                        if (param._e065Name1JiritsuKatsudouSubclasscdList.contains(getSubclasscd(sr, param))) {
                            return true;
                        }
                    }
                }
                return false;
            }
            /**
             * {@inheritDoc}
             */
            public int compareTo(final StudyrecClass that) {
                int rtn;
                rtn = _showOrderClass.compareTo(that._showOrderClass);
                if (0 != rtn)
                    return rtn;
                rtn = _classcd.compareTo(that._classcd);
                if (0 != rtn)
                    return rtn;
                if (null != _schoolKind && null != that._schoolKind) {
                    rtn = _schoolKind.compareTo(that._schoolKind);
                    if (0 != rtn)
                        return rtn;
                }
                return rtn;
            }
        }

        private static class StudyrecSubClass implements Comparable<StudyrecSubClass> {
            final String _classcd;
            final String _schoolKind;
            final String _curriculumCd;
            final String _subclasscd;
            final Integer _showOrderSubclass;
            final List<StudyRec> _studyrecList;
            public StudyrecSubClass(final String classcd, final String schoolKind, final String curriculumCd, final String subclasscd, final Integer showOrderSubclass) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _curriculumCd = curriculumCd;
                _subclasscd = subclasscd;
                _showOrderSubclass = showOrderSubclass;
                _studyrecList = new ArrayList<StudyRec>();
            }
            /**
             * {@inheritDoc}
             */
            public int compareTo(final StudyrecSubClass that) {
                int rtn;
                rtn = _classcd.compareTo(that._classcd);
                if (0 != rtn)
                    return rtn;
                if (null != _schoolKind && null != that._schoolKind) {
                    rtn = _schoolKind.compareTo(that._schoolKind);
                    if (0 != rtn)
                        return rtn;
                }
                if (null != _curriculumCd && null != that._curriculumCd) {
                    rtn = _curriculumCd.compareTo(that._curriculumCd);
                    if (0 != rtn)
                        return rtn;
                }
                rtn = _showOrderSubclass.compareTo(that._showOrderSubclass);
                if (0 != rtn)
                    return rtn;
                rtn = _subclasscd.compareTo(that._subclasscd);
                return rtn;
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 活動の記録。
     * 高校生徒指導要録の指導に関する記録を印刷します。
     * ２.総合的な学習の時間の記録
     * ３.特別活動の記録
     * ４.総合所見及び指導上参考となる諸事項
     * ５.出欠の記録
     */
    private static class KNJA130_4 extends KNJA130_0 {

        private KNJSvfFieldInfo _name;

        KNJA130_4(final DB2UDB db2, final Vrw32alp svf, final Param param) {
            super(db2, svf, param);
        }

        public void setForm(final Student student) {
            final String form;
            if (param()._isNewForm) {
                if (param()._is3YearsSystem) {
                    form = "KNJA130_14B_2.frm";
                } else {
                    form = "KNJA130_4B_2.frm";
                }
                _name = new KNJSvfFieldInfo(433, 1288, KNJSvfFieldModify.charSizeToPixel(12.0), 287, 320, 353, 24, 56);
            } else {
                final int x1, x2, ystart1;
                if (param()._is3YearsSystem) {
                    form = "KNJA130_14B.frm";
                    x1 = 797;
                    x2 = 1597;
                    ystart1 = 247;
                } else {
                    form = "KNJA130_4B.frm";
                    x1 = 801;
                    x2 = 1601;
                    ystart1 = 249;
                }
                _name = new KNJSvfFieldInfo(x1, x2, KNJSvfFieldModify.charSizeToPixel(12.0), 282, ystart1, 315, 24, 48);
            }
            VrSetForm(form, 1);
        }

        public void setDetail(final Student student) {
            final int max = param()._is3YearsSystem ? 3 : 4;

            final List<List<Gakuseki>> gakusekiListList = new ArrayList<List<Gakuseki>>();
            List<Gakuseki> current = null;
            final List<TreeMap<Integer, Gakuseki>> yearGakusekiMapList = new ArrayList<TreeMap<Integer, Gakuseki>>();
            TreeMap<Integer, Gakuseki> currentMap = null;
            for (final Gakuseki gakuseki : student._gakusekiAttendRecList) {
                if (param().isTanniSei() && "0".equals(gakuseki._year) || !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                    continue;
                }

                if (null == current || current.size() >= max) {
                    current = new ArrayList<Gakuseki>();
                    gakusekiListList.add(current);
                    currentMap = new TreeMap<Integer, Gakuseki>();
                    yearGakusekiMapList.add(currentMap);
                }
                currentMap.put(current.size(), gakuseki);
                current.add(gakuseki);
                if (gakuseki._isDrop) {
                    current = null;
                }
            }

            for (int k = 0; k < gakusekiListList.size(); k++) {
                final List<Gakuseki> gakusekiList = gakusekiListList.get(k);
                final TreeMap<Integer, Gakuseki> yearGakusekiMap = yearGakusekiMapList.get(k);
                log.info(" yearGakusekiMap = " + yearGakusekiMap);
                setForm(student);

                VrsOut("SOGO_TITLE", append(student.getSogoSubclassname(param(), yearGakusekiMap, -1), "の記録"));    // 特別活動の記録

                printGradeRecSub(svf, student);

                if (!param()._isNendogoto) { // 「通年」か?

                    String htrainRemarkHdatAct = "";
                    String htrainRemarkHdatVal = "";
                    String seqAct = "";
                    String seqVal = "";

                    for (final Gakuseki gakusekip : gakusekiList) {
                        final String totalStudyAct = student._htrainRemarkHdatAct.get(gakusekip._year);
                        if (null != totalStudyAct) {
                            htrainRemarkHdatAct += seqAct + totalStudyAct;
                            seqAct = "\n";
                        }
                        final String totalStudyVal = student._htrainRemarkHdatVal.get(gakusekip._year);
                        if (null != totalStudyVal) {
                            htrainRemarkHdatVal += seqVal + totalStudyVal;
                            seqVal = "\n";
                        }
                    }


                    // 「総合的な学習の時間の記録」を印字
                    final int actChar, actLine;
                    final String actField1;
                    if ("66 * 2".equals(param()._seitoSidoYoroku_dat_TotalstudyactSize)) {
                        actChar = 66;
                        actLine = 9;
                        actField1 = "REC_1_2";
                    } else {
                        actChar = 44;
                        actLine = 6;
                        actField1 = "REC_1";
                    }
                    if (student._isShowStudyRecBikoSubstitution90) {
                        final String[] arrbikoSubstitution90 = student.getArraySubstitutionBiko90(student._keyAll, param());
                        printDetail2WithBiko(svf, htrainRemarkHdatAct, actChar * 2, actLine, actField1, arrbikoSubstitution90);    // 「学習活動」の欄
                    } else {
                        printDetail2(        svf, htrainRemarkHdatAct, actChar * 2, actLine, actField1);    // 「学習活動」の欄
                    }

                    if ("66 * 3".equals(param()._seitoSidoYoroku_dat_TotalstudyvalSize)) {
                        printDetail2(svf, htrainRemarkHdatVal, 66 * 2, 9, "REC_2_2");    // 「評価」の欄
                    } else {
                        printDetail2(svf, htrainRemarkHdatVal, 44 * 2, 6, "REC_2");    // 「評価」の欄
                    }
                }

                int i = 1;
                for (int l = 0; l < gakusekiList.size(); l++) {
                    final Gakuseki gakuseki = gakusekiList.get(l);

                    final int j = getGradeColumnNum(i, gakuseki);

                    printGradeRecDetail(svf, j, gakuseki);

                    // 所見データを印刷
                    printHtrainRemark(student, gakuseki, j);

                    // 出欠データを印刷
                    if (!"1".equals(param()._remarkOnly)) {
                        printAttendRec(student, gakuseki, j);
                    }
                    i++;
                }

                svf.VrEndPage();
            }

            nonedata = true;
        }

        private void printDetail2(
                final Vrw32alp svf,
                final String strData,
                final int lenA,
                final int lenB,
                final String strField
        ) {
            if (null == strData) {
                return;
            }
            final List<String> arrstr = getTokenList(strData, lenA, lenB);
            for (int j = 0; j < arrstr.size(); j++) {
                if (null == arrstr.get(j)) {
                    continue;
                }
                svf.VrsOutn(strField, j + 1, arrstr.get(j));
            }
        }

        private void printDetail2WithBiko(
                final Vrw32alp svf,
                final String strData,
                final int lenA,
                final int lenB,
                final String strField,
                final String[] biko
        ) {
            int last = 1;
            if (null != strData) {
                final List<String> arrstr = getTokenList(strData, lenA, lenB);
                for (int j = 0; j < arrstr.size(); j++) {
                    if (null == arrstr.get(j)) {
                        continue;
                    }
                    svf.VrsOutn(strField, last, arrstr.get(j));
                    last += 1;
                }
            }
            if (null != biko) {
                for (int k = 0; k < biko.length; k++) {
                    if (null == biko[k]) {
                        continue;
                    }
                    final List<String> arrstr = getTokenList(biko[k], lenA, lenB);
                    for (int j = 0; j < arrstr.size(); j++) {
                        if (null == arrstr.get(j)) {
                            continue;
                        }
                        svf.VrsOutn(strField, last, arrstr.get(j));
                        last += 1;
                    }
                }
            }
        }

        private void printHtrainRemark(final Student student, final Gakuseki gakuseki, int j) {
            final HtrainRemark remark = null == student._htrainRemarkMap.get(gakuseki._year) ? HtrainRemark.Null : student._htrainRemarkMap.get(gakuseki._year);
//            final String[] studyRecSubstitution90;
//            if (student._isShowStudyRecBikoSubstitution90) {
//                studyRecSubstitution90 = student.getArraySubstitutionBiko90(gakuseki._year, param());
//            } else {
//                studyRecSubstitution90 = null;
//            }
//          if (param._isNendogoto) {
//              printDetailWithBiko(svf, _totalStudyAct, i, "", 22, 4, "rec_1_", studyRecSubstitution90);   // 総合的な学習の時間学習活動
//              printDetail(        svf, _totalStudyVal, i, "", 22, 4, "rec_2_");   // 総合的な学習の時間評価
//          }

            // 特別活動
            if ("1".equals(param()._seitoSidoYorokuSpecialactremarkFieldSize)) {
                printDetail(svf, remark._specialActRemark, j, "_2", 44, 10, "SPECIALACTREMARK_");
            } else {
                printDetail(svf, remark._specialActRemark, j, "", 22, 6, "SPECIALACTREMARK_");
            }

            // 所見
            if ("1".equals(param()._seitoSidoYorokuSougouFieldSize)) {
                printDetail(svf, remark._totalRemark, j, "_2", 132, 8, "rec_3_");
            } else {
                printDetail(svf, remark._totalRemark, j, "", 88, 6, "rec_3_");
            }

            // 出欠備考
            printDetail(svf, remark._attendRecRemark, j, "", 40, 2, "syuketu_8_");
        }

        /**
         * 所見等データを印刷します。
         * @param svf
         * @param strData 編集元の文字列
         * @param i
         * @param lenA 行当りの文字数（Byte)
         * @param lenB 行数
         * @param strField SVFフィールド名
         */
        private void printDetail(
                final Vrw32alp svf,
                final String strData,
                final int i,
                final String postfix,
                final int lenA,
                final int lenB,
                final String strField
        ) {
            if (null == strData) {
                return;
            }
            final List<String> arrstr = getTokenList(strData, lenA, lenB);
            for (int j = 0; j < arrstr.size(); j++) {
                if (null == arrstr.get(j)) {
                    continue;
                }
                svf.VrsOutn(strField + i + postfix, j + 1, arrstr.get(j));
            }
        }

        // TAKAESU: 上記メソッドと同一視可能
        private void printAttendRec(final Student student, final Gakuseki gakuseki, int j) {
            final AttendRec attendrec = student._attendRecMap.get(gakuseki._year);
            if (null != attendrec) {
                // 授業日数
                VrsOut("syuketu_1_" + j, getStringValue(attendrec._attend_1));
                // 出停 +  忌引日数
                VrsOut("syuketu_2_" + j, getStringValue(attendrec._suspendMourning));
                // 留学日数
                VrsOut("syuketu_4_" + j, getStringValue(attendrec._abroad));
                // 要出席日数
                VrsOut("syuketu_5_" + j, getStringValue(attendrec._requirepresent));
                // 欠席日数
                VrsOut("syuketu_6_" + j, getStringValue(attendrec._attend_6));
                // 出席日数
                VrsOut("syuketu_7_" + j, getStringValue(attendrec._present));
            }
        }

        /**
         * @param Int
         * @return Integer Int がnullでなければ文字列に変換して戻します。
         */
        private String getStringValue(Integer Int) {
            if (null == Int) {
                return null;
            }
            return String.valueOf(Int);
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub(final Vrw32alp svf, final Student student) {
            VrsOut("GRADENAME1", student._title);
            VrsOut("GRADENAME2", student._title);

            printName(svf, student, "NAME1", "NAME2", "NAME3", _name);
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(final Vrw32alp svf, final int i, final Gakuseki gakuseki) {
            final boolean enableYear = (null != gakuseki._year) && (0 != Integer.parseInt(gakuseki._year));
            if (param().isKoumokuGakunen()) {

                if (enableYear) {
                    VrsOut("GRADE4_" + i, gakuseki._gakunenSimple);
                    VrsOut("GRADE3_" + i + "_2", gakuseki._gakunenSimple);
                } else {
                    VrsOut("GRADE3_" + i + "_2", gakuseki._nendo);
                }

                VrsOut("GRADE1_" + i, gakuseki._gradeName2);    // 特別活動の記録
                VrsOut("GRADE2_" + i, gakuseki._gradeName2);    // 総合所見及び...
                VrsOut("GRADE5_" + i, gakuseki._gradeName2);    // 総合的な学習の時間の記録
            } else {
                if (enableYear) {
                    VrsOut("GRADE3_" + i, gakuseki._nendo);
                    final String[] nendoArray = gakuseki.nendoArray();
                    VrsOut("GRADE3_" + i + "_1", nendoArray[0]);
                    VrsOut("GRADE3_" + i + "_2", nendoArray[1]);
                    VrsOut("GRADE3_" + i + "_3", nendoArray[2]);
                } else {
                    VrsOut("GRADE3_" + i + "_2", gakuseki._nendo);
                }

                VrsOut("GRADE1_" + i, gakuseki._nendo); // 特別活動の記録
                VrsOut("GRADE2_" + i, gakuseki._nendo); // 総合所見及び...
                VrsOut("GRADE5_" + i, gakuseki._nendo); // 総合的な学習の時間の記録
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            VrsOut(hrClassField + i, gakuseki._hrname);
            VrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    public static class KNJSvfFieldModify {

        private static final Log log = LogFactory.getLog(KNJSvfFieldModify.class);

        private final String _fieldname; // フィールド名
        private final int _width;   //フィールドの幅(ドット)
        private final int _height;  //フィールドの高さ(ドット)
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        public KNJSvfFieldModify(String fieldname, int width, int height, int ystart, int minnum, int maxnum) {
            _fieldname = fieldname;
            _width = width;
            _height = height;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        /**
         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
         * @param posx1 フィールドの左端X
         * @param posx2 フィールドの右端X
         * @param num フィールド指定の文字数
         * @param charSize 変更後の文字サイズ
         * @return ずれ幅の値
         */
        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, float charSize) {
            final int maxWidth = getStringLengthPixel(charSize, num); // 文字の大きさを考慮したフィールドの最大幅
            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2 + 10;
            return offset;
        }

        private int getStringLengthPixel(final float charSize, final int num) {
            return charSizeToPixel(charSize) * num / 2;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public float getCharSize(String str) {
            return Math.min((float) pixelToCharSize(_height), retFieldPoint(_width, getStringByteSize(str)));                  //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private int getStringByteSize(String str) {
            return Math.min(Math.max(retStringByteValue( str ), _minnum), _maxnum);
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charSize 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static int charSizeToPixel(final double charSize) {
            return (int) Math.round(charSize / 72 * 400);
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharSize(final int pixel) {
            return pixel / 400.0 * 72;
        }

        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public float getYjiku(int hnum, float charSize) {
            float jiku = 0;
            try {
                jiku = retFieldY(_height, charSize) + _ystart + _height * hnum;  //出力位置＋Ｙ軸の移動幅
            } catch (Exception ex) {
                log.error("setRetvalue error!", ex);
                log.debug(" jiku = " + jiku);
            }
            return jiku;
        }

        /**
         *  文字数を取得
         */
        private static int retStringByteValue(final String str) {
            return KNJ_EditEdit.getMS932ByteLength(str);
        }

        /**
         *  文字サイズを設定
         */
        private static float retFieldPoint(int width, int num) {
            return (float) Math.round((float) width / (num / 2) * 72 / 400 * 10) / 10;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static float retFieldY(int height, float charSize) {
            return (float) Math.round(((double) height - (charSize / 72 * 400)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    private static class Param {

        private static final String SCHOOL_KIND = "H";

        public boolean _isHigh;
        final String _year;
        final String _gakki;
        final String _gradeInChugaku = "('01','02','03')";
        final String _output;
        final String[] _categorySelected;
        final String[] _selectedHR;
        final String _grade;
        /** 3年制か */
        final boolean _is3YearsSystem;
        /** 生徒指導要録 */
        final String _seito;
        /** 修得単位の記録 */
        final String _tani;
        /** 学習の記録 */
        final String _gakushu;
        /** 活動の記録 */
        final String _katsudo;
        /** 生徒・保護者氏名出力(生徒指導要録に関係する) */
        final String _simei;
        /** 陰影出力(生徒指導要録に関係する) */
        final String _inei;
        /** 陰影保管場所(陰影出力に関係する) */
        final String _documentRoot;
        /** 陰影をカラープリンターで印刷するか(陰影出力に関係する) */
        final String _isColorPrint;
        /** 現住所の郵便番号を出力 */
        final boolean _printZipcd;
        /** 学校所在地の郵便番号を出力 */
        final boolean _printSchoolZipcd;
        /** 中高一貫ならtrue */
        private boolean _isChuKouIkkan;
        /** 併設校ならtrue */
        private boolean _isHeisetuKou;
        /** 法政ならtrue */
        private boolean _isHousei;
        /** 熊本ならtrue */
        private boolean _isKumamoto;
        /** 湧心館ならtrue */
        private boolean _isYuushinkan;
        /**
         *  名称マスタA021を使用するなら"0"
         *  組名称にSCHREG_REGD_HDATのHR_CLASS_NAME1を使用するなら"1"
         */
        final String _useSchregRegdHdat;
        /** 「活動の記録」にて「総合的な学習の時間の記録」欄は年度毎か? */
        final boolean _isNendogoto;
        /** 生年月日に西暦を使用するか */
        private boolean _isSeireki;

        final boolean _hasStudyrefProvFlgDat;

        /** 履修のみ科目出力 */
        private boolean _isPrintRisyuNomi;
        /** 未履修科目出力 */
        private boolean _isPrintMirisyu;
        /** 履修登録のみ科目出力 */
        private boolean _isPrintRisyuTourokuNomi;
        /** 2013年度からのフォームか */
        private boolean _isNewForm = false;
        /** プロパティーファイルの_seitoSidoYorokuSpecialactremarkFieldSize */
        final String _seitoSidoYorokuSpecialactremarkFieldSize;
        /** プロパティーファイルのseitoSidoYorokuSougouFieldSize */
        final String _seitoSidoYorokuSougouFieldSize;
        /** プロパティーファイルのseitoSidoYoroku_dat_TotalstudyactSize */
        final String _seitoSidoYoroku_dat_TotalstudyactSize;
        /** プロパティーファイルのseitoSidoYoroku_dat_TotalstudyvalSize */
        final String _seitoSidoYoroku_dat_TotalstudyvalSize;
        /** プロパティーファイルのseitoSidoYorokuZaisekiMaeが1なら true */
        final boolean _seitoSidoYorokuZaisekiMae;
        /** プロパティーファイルのseitoSidoYorokuKoumokuMeiが1なら true */
        final String _seitoSidoYorokuKoumokuMei;
        /** プロパティーファイルのseitoSidoYorokuHyotei0ToBlankが1なら指導要録の学習の記録で評定0はブランク表示にする */
        final String _seitoSidoYorokuHyotei0ToBlank;
        /** プロパティーファイルのseitoSidoYorokuNotPrintAnotherStudyrecが1なら指導要録のSCHOOLCD='1'のSCHREG_STUDYREC_DATを読み込みしない */
        final String _seitoSidoYorokuNotPrintAnotherStudyrec;
        /** プロパティーファイルのseitoSidoYorokuNotPrintAnotherAttendrecが1なら指導要録のSCHOOLCD='1'のSCHREG_ATTENDREC_DATを読み込みしない */
        final String _seitoSidoYorokuNotPrintAnotherAttendrec;
        final String _useCurriculumcd;
        final String _useAddrField2;
        final String _useGakkaSchoolDiv;
        final KNJDefineSchool _definecode;
        final boolean _isPrintYoshiki2OmoteTotalCreditByPage;
        final boolean _isPrintYoshiki2OmoteEduDiv2 = true; // 様式2表の教科専門区分文言は1行2文字ずつ表示
        protected Set _gdatHYearGradeSet = Collections.EMPTY_SET;
        protected Map _gdatGradeName2 = Collections.EMPTY_MAP;
        protected Map _gdatGradeCd = Collections.EMPTY_MAP;
        protected Map _specialDivNameMap = Collections.EMPTY_MAP;
        protected Map<String, PreparedStatement> _psMap = new TreeMap<String, PreparedStatement>();
        protected String _mirisyuRemarkFormat; // 未履修（履修不認定）備考フォーマット
        protected String _risyunomiRemarkFormat; // 履修のみ（修得不認定）備考フォーマット
        /** 学科年度データの学校区分 */
        protected String _majorYdatSchooldiv;
        /** 所見プレビュー */
        protected String _remarkOnly;
        final String _seitoSidoYorokuDaitaiCheckMasterYear;
        final String _seitoSidoYorokuDaitaiCreditEach1;
        final List<String> _e065Name1JiritsuKatsudouSubclasscdList;
        final boolean _hasAftGradCourseDat;
        final boolean _hasAnotherClassMst;
        final boolean _hasAnotherSubclassMst;
        final boolean _hasSchregEntGrdHistComebackDat;
        final boolean _hasMajorMstMajorname2;
        final Map<String, String> _dbPrginfoProperties;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugField;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _dbPrginfoProperties = getDbPrginfoProperties(db2);
            if (!_dbPrginfoProperties.isEmpty()) {
                log.info(" dbPrginfoProperties = " + _dbPrginfoProperties);
            }
            _year = getParameter(request, "YEAR"); // 年度
            _gakki = getParameter(request, "SEMESTER"); // 学期
            _inei = "".equals(getParameter(request, "INEI")) ? null : getParameter(request, "INEI"); // 陰影出力
            _documentRoot = getParameter(request, "DOCUMENTROOT"); // 陰影保管場所 NO001
            _isColorPrint = getParameter(request, "COLOR_PRINT"); // 陰影をカラープリンターで印刷するか
            _simei = getParameter(request, "SIMEI"); // 漢字名出力
            _seito = getParameter(request, "SEITO");
            _tani = getParameter(request, "TANI");
            _gakushu = getParameter(request, "GAKUSHU");
            _katsudo = getParameter(request, "KATSUDO");
            _isPrintRisyuNomi = "1".equals(getParameter(request, "RISYU"));
            _isPrintMirisyu = "1".equals(getParameter(request, "MIRISYU"));
            _isPrintRisyuTourokuNomi = "1".equals(getParameter(request, "RISYUTOUROKU"));
            _output = getParameter(request, "OUTPUT");    // 1=個人, 2=クラス
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 複数生徒 or 複数年組
            if ("2".equals(_output)) {
                _selectedHR = _categorySelected;
            } else {
                _selectedHR = request.getParameterValues("GRADE_HR_CLASS");
            }
            final String grade = request.getParameterValues("GRADE_HR_CLASS")[0];
            if (null != grade && grade.length() >= 2) {
                _grade = grade.substring(0, 2);
            } else {
                _grade = null;
            }
//            final String prgId = getParameter(request, "PRGID");
//            _isNendogoto = "KNJA130A".equals(prgId);
//            log.debug("「活動の記録」にて「総合的な学習の時間の記録」欄を年度毎にするか?⇒" + _isNendogoto);
            _isNendogoto = false;
            _useSchregRegdHdat = getParameter(request, "useSchregRegdHdat");
            _is3YearsSystem = "1".equals(getParameter(request, "RADIO"));
            _printZipcd = "1".equals(getParameter(request, "SCHZIP"));
            _printSchoolZipcd = "1".equals(getParameter(request, "SCHOOLZIP"));
            _definecode = new KNJDefineSchool();
            _seitoSidoYorokuSougouFieldSize = getParameter(request, "seitoSidoYorokuSougouFieldSize");
            _seitoSidoYorokuSpecialactremarkFieldSize = getParameter(request, "seitoSidoYorokuSpecialactremarkFieldSize");
            _seitoSidoYoroku_dat_TotalstudyactSize = null != getParameter(request, "seitoSidoYoroku_dat_TotalstudyactSize") ?
                    StringUtils.replace(getParameter(request, "seitoSidoYoroku_dat_TotalstudyactSize"), "+", " ") : "";
            _seitoSidoYoroku_dat_TotalstudyvalSize = null != getParameter(request, "seitoSidoYoroku_dat_TotalstudyvalSize") ?
                    StringUtils.replace(getParameter(request, "seitoSidoYoroku_dat_TotalstudyvalSize"), "+", " ") : "";
            _seitoSidoYorokuZaisekiMae = "1".equals(getParameter(request, "seitoSidoYorokuZaisekiMae"));
            _seitoSidoYorokuKoumokuMei = getParameter(request, "seitoSidoYorokuKoumokuMei");
            _seitoSidoYorokuHyotei0ToBlank = getParameter(request, "seitoSidoYorokuHyotei0ToBlank");
            _seitoSidoYorokuNotPrintAnotherStudyrec = getParameter(request, "seitoSidoYorokuNotPrintAnotherStudyrec");
            _seitoSidoYorokuNotPrintAnotherAttendrec = getParameter(request, "seitoSidoYorokuNotPrintAnotherAttendrec");
            _useCurriculumcd = getParameter(request, "useCurriculumcd");
            _useAddrField2 = getParameter(request, "useAddrField2");
            _useGakkaSchoolDiv = getParameter(request, "useGakkaSchoolDiv");
            _isPrintYoshiki2OmoteTotalCreditByPage = true;
            _remarkOnly = getParameter(request, "remarkOnly");
            _seitoSidoYorokuDaitaiCheckMasterYear = getParameter(request, "seitoSidoYorokuDaitaiCheckMasterYear");
            _seitoSidoYorokuDaitaiCreditEach1 = getParameter(request, "seitoSidoYorokuDaitaiCreditEach1");
            setNewForm();

            _definecode.setSchoolCode(db2, _year);

            isJuniorHiSchool(db2);
            log.debug("中高一貫か? = " + _isChuKouIkkan);
            log.debug("併設校か? = " + _isHeisetuKou);
            setSeireki(db2);
            setSpecialDivNameMap(db2);
            setFuninteiRemarkFormat(db2);
            setIsHigh(db2);
            setGdatHYearGradeSet(db2);
            _e065Name1JiritsuKatsudouSubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'E065' ORDER BY NAMECD2 "), "NAME1");
            _hasAftGradCourseDat = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
            _hasAnotherClassMst = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
            _hasAnotherSubclassMst = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);
            _hasSchregEntGrdHistComebackDat = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _hasMajorMstMajorname2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
            _hasStudyrefProvFlgDat = KnjDbUtils.setTableColumnCheck(db2, "STUDYREC_PROV_FLG_DAT", null);

            final String[] outputDebug = StringUtils.split(_dbPrginfoProperties.get("outputDebug"));
            if (!ArrayUtils.isEmpty(outputDebug)) {
                log.info(" outputDebug = " + ArrayUtils.toString(outputDebug));
            }
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugField = ArrayUtils.contains(outputDebug, "field");
        }

        private String getParameter(final HttpServletRequest request, final String name) {
            if (request.getParameterMap().containsKey(name)) {
                return request.getParameter(name);
            }
            if (_dbPrginfoProperties.containsKey(name)) {
                final String prop = _dbPrginfoProperties.get(name);
                log.info(" #set " + name + " = " + prop);
                return prop;
            }
            log.info(" no prop : " + name);
            return null;
        }

        public PreparedStatement setPs(final DB2UDB db2, final String psKey, final String sql) {
            PreparedStatement ps = null;
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return ps;
        }

        public void close() {
            for (final Iterator<String> it = _psMap.keySet().iterator(); it.hasNext();) {
                final String psKey = it.next();
                DbUtils.closeQuietly(_psMap.get(psKey));
                it.remove();
            }
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA130B' "), "NAME", "VALUE");
        }

        private List<String> createSchregnos(final DB2UDB db2, final String kumi) {
            final List<String> rtn = new ArrayList<String>();

            if ("2".equals(_output)) {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT SCHREGNO FROM SCHREG_REGD_DAT ");
                stb.append("WHERE YEAR = '" + _year + "' ");
                stb.append("AND SEMESTER = '" + _gakki + "' ");
                stb.append("AND GRADE || HR_CLASS = ? ");
                stb.append("ORDER BY ATTENDNO ");

                rtn.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString(), new Object[] {kumi}), "SCHREGNO"));
            } else {
                for (int i = 0; i < _categorySelected.length; i++) {
                    rtn.add(_categorySelected[i]);
                }
            }
            return rtn;
        }

        private void setNewForm() {
            final int startYear = 2013; // 2013年度入学生徒から新フォーム
            if (Integer.parseInt(_year) == startYear + 0 && Integer.parseInt(_grade) <= 1 ||
                Integer.parseInt(_year) == startYear + 1 && Integer.parseInt(_grade) <= 2 ||
                Integer.parseInt(_year) == startYear + 2 && Integer.parseInt(_grade) <= 3 ||
                Integer.parseInt(_year) >= startYear + 3) {
                _isNewForm = true;
            } else {
                _isNewForm = false;
            }
        }

        public void loadStudentData(final DB2UDB db2, final Student student) {
            if ("1".equals(_useGakkaSchoolDiv)) {
                setMajorYdatSchooldiv(db2, student._schregno, _year);
            }
        }

        private List<KNJA130_0> getFormObj(final DB2UDB db2, final Vrw32alp svf) {
            final List<KNJA130_0> rtn = new ArrayList<KNJA130_0>();

            // 様式１（学籍に関する記録）
            if (null != _seito) {
                rtn.add(new KNJA130_1(db2, svf, this));
            }

            // 様式１の裏（修得単位の記録）
            if (null != _tani) {
                rtn.add(new KNJA130_2(db2, svf, this));
            }

            // 様式２（指導に関する記録）
            if (null != _gakushu) {
                rtn.add(new KNJA130_3(db2, svf, this));
            }

            // 様式２の裏（所見等）
            if (null != _katsudo) {
                rtn.add(new KNJA130_4(db2, svf, this));
            }

            return rtn;
        }

        /**
         * 中高一貫か?
         * @param db2 DB2UDB
         * @return 中高一貫ならtrue
         */
        private void isJuniorHiSchool(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));

            final String namespare2 = KnjDbUtils.getString(row, "NAMESPARE2");
            final String name1 = KnjDbUtils.getString(row, "NAME1");
            if ("1".equals(namespare2)) {
                _isHeisetuKou = true;
            }
            if ("1".equals(namespare2) || "2".equals(namespare2)) {
                _isChuKouIkkan = true;
            }
            _isHousei = "HOUSEI".equals(name1);
            _isKumamoto = "kumamoto".equals(name1);
            _isYuushinkan = "Yuushinkan".equals(name1);
        }

        private String getSchooldiv() {
            final String schooldiv;
            if ("1".equals(_useGakkaSchoolDiv)) {
                schooldiv = null != _majorYdatSchooldiv ? _majorYdatSchooldiv : _definecode.schooldiv;
            } else {
                schooldiv = _definecode.schooldiv;
            }
            return schooldiv;
        }

        /**
         * 学年制か?
         * @return 学年制なら<code>true</code>
         */
        private boolean isGakunenSei() {
            return "0".equals(getSchooldiv());
        }

        /**
         * 単位制か?
         * @return 単位制なら<code>true</code>
         */
        private boolean isTanniSei() {
            return "1".equals(getSchooldiv());
        }

        private boolean isInstallationPrint(final DB2UDB db2) {

            String str = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));

            if ("KINDAI".equals(str)) {
                return false;
            }
            if ("KINJUNIOR".equals(str)) {
                return false;
            }
            if ("kumamoto".equals(str)) {
                return false;
            }

            return true;
        }

        private void setSeireki(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2 = '00'";
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
        }

        private void setSpecialDivNameMap(final DB2UDB db2) {
            final String sql = "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A029' ";
            _specialDivNameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "NAMECD2", "NAME1");
        }

        private void setFuninteiRemarkFormat(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'A030' AND NAMECD2 = '00' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));

            _mirisyuRemarkFormat = KnjDbUtils.getString(row, "NAMESPARE1");
            _risyunomiRemarkFormat = KnjDbUtils.getString(row, "NAMESPARE2");
        }

        /**
         * 普通・専門の文言
         * @param div 普通・専門区分　0:普通、1:専門
         * @return 文言
         */
        private String getSpecialDivName(String div) {
            if (null == div) {
                div = "0";
            }
            final String defaultname;
            if ("0".equals(div)) {
                // 普通教育
                defaultname = _isNewForm ? "各学科に共通する各教科・科目" : "普通教育に関する各教科・科目";
            } else if ("1".equals(div)) {
                //　専門教科
                defaultname = _isNewForm ? "主として専門学科において開設される各教科・科目" : "専門教育に関する各教科・科目";
            } else if ("2".equals(div)) {
                // その他
                defaultname = "その他特に必要な教科・科目";
            } else {
                defaultname = "";
            }
            final String key;
            if (NumberUtils.isDigits(div)) {
                key = String.valueOf(Integer.parseInt(div) + 1);
            } else {
                key = "";
            }
            return StringUtils.defaultString((String) _specialDivNameMap.get(key), defaultname);
        }

        private void setIsHigh(final DB2UDB db2) {
            _isHigh = true;
            if (null == _grade) {
                return;
            }
            final String sql = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, sql);
            if (rowList.size() > 0 && !SCHOOL_KIND.equals(KnjDbUtils.getOne(rowList))) {
                _isHigh = false;
            }
        }

        public boolean isGdatH(final String year, final String grade) {
            if (null == year || null == grade) {
                return false;
            }
            return _gdatHYearGradeSet.contains(year + ":" + grade);
        }

        public String getGdatGradeName2(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : (String) _gdatGradeName2.get(year + ":" + grade);
            return rtn;
        }

        public String getGdatGradeCd(final String year, final String grade) {
            final String rtn = (null == year || null == grade) ? null : (String) _gdatGradeCd.get(year + ":" + grade);
            return rtn;
        }

        private void setGdatHYearGradeSet(final DB2UDB db2) {
            _gdatHYearGradeSet = new HashSet();
            _gdatGradeName2 = new HashMap();
            _gdatGradeCd = new HashMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, " SELECT YEAR, GRADE, GRADE_NAME2, GRADE_CD FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND = '" + SCHOOL_KIND + "' ")) {
                final String yg = KnjDbUtils.getString(row, "YEAR") + ":" + KnjDbUtils.getString(row, "GRADE");
                _gdatHYearGradeSet.add(yg);
                _gdatGradeName2.put(yg, KnjDbUtils.getString(row, "GRADE_NAME2"));
                _gdatGradeCd.put(yg, KnjDbUtils.getString(row, "GRADE_CD"));
            }
            if (_seitoSidoYorokuZaisekiMae) {
                _gdatHYearGradeSet.add("0:00");
            }
        }

        /**
         * 指定生徒・年度の学科年度データの学校区分を得る
         */
        private void setMajorYdatSchooldiv(final DB2UDB db2, final String schregno, final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("   SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   WHERE SCHREGNO = '" + schregno + "' AND YEAR = '" + year + "' ");
            stb.append("   GROUP BY SCHREGNO, YEAR ");
            stb.append(" ) ");
            stb.append(" SELECT T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T4.SCHOOLDIV ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" INNER JOIN MAJOR_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
            stb.append(" INNER JOIN MAJOR_YDAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("     AND T4.COURSECD = T1.COURSECD ");
            stb.append("     AND T4.MAJORCD = T1.MAJORCD ");
            log.debug(" majorYdatSchooldiv = " + stb.toString());

            final Map assocMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            _majorYdatSchooldiv = KnjDbUtils.getString(assocMap, "SCHOOLDIV");

            // log.fatal(" schoolmst.schooldiv = " + _definecode.schooldiv + ", majorYdatSchoolDiv = " + _majorYdatSchooldiv + " -> "+  getSchooldiv());
        }

        public boolean isKoumokuGakunen() {
            Boolean rtn = null;
            if (null != _seitoSidoYorokuKoumokuMei) {
                if ("1".equals(_seitoSidoYorokuKoumokuMei)) {
                    rtn = Boolean.TRUE;
                } else if ("2".equals(_seitoSidoYorokuKoumokuMei)) {
                    rtn = Boolean.FALSE;
                }
            }
            if (null == rtn) {
                if (isGakunenSei()) {
                    rtn = Boolean.TRUE;
                } else if (isTanniSei()) {
                    rtn = Boolean.FALSE;
                }
            }
            if (null == rtn) {
                rtn = Boolean.TRUE;
            }
            return rtn.booleanValue();
        }
    }
}
