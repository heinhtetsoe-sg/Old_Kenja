// kanji=漢字
/*
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.SvfForm.Line;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 小学校用
 */

public class KNJA133P {
    private static final Log log = LogFactory.getLog(KNJA133P.class);

    private static final String CHITEKI1_知的障害 = "1";
    private static final String CHITEKI2_知的障害以外 = "2";
    private static final String CHITEKI3_知的障害_視覚障害等 = "3";

    private boolean _hasdata = false;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        svf_out_ex(request, response, Collections.EMPTY_MAP);
    }

    public void svf_out_ex(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Map paramMap
    ) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        Param param = null;
        try {
            // print svf設定
            sd.setSvfInit(request, response, svf);

            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            param = getParam(request, db2, paramMap);

            // 印刷処理
            printSvf(request, db2, svf, param);
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                param.closeStatementQuietly();
                param.closeForm();
            }

            // 終了処理
            sd.closeSvf(svf, _hasdata);
            sd.closeDb(db2);
        }
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) throws SQLException {
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2, paramMap);
    }

    /**
     * 印刷処理
     */
    private void printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param) {

        final List<Student> studentList = getStudentList(db2, param);
        final List<KNJA133P_0> knjobj = param.getKnja133List(request, db2, svf);

        for (final Student student : studentList) {

            if (param._isOutputDebug) {
                log.info(" schregno = " + student._schregno);
            }

            student._printEntGrdHistList = student.getPrintSchregEntGrdHistList(param);

            int totalpage = 0;
            if ("1".equals(param._printBlankPage)) {
                for (final KNJA133P_0 knja133p : knjobj) {
                    totalpage += knja133p.getPrintPage(student);
                }
            }

            for (final KNJA133P_0 knja133p : knjobj) {
                for (int egi = 0; egi < student._printEntGrdHistList.size(); egi++) {
                    final PersonalInfo pInfo = student._printEntGrdHistList.get(egi);
                    if (param._isOutputDebug) {
                        log.info(" entgrd idx = " + egi);
                    }
                    // knja133p._notOutput = true; // フォームのみ出力
                    if (knja133p.printSvf(db2, student, pInfo)) {
                        _hasdata = true; // 印刷処理
                    }
                    if (knja133p instanceof KNJA133P_1 && "1".equals(param._printBlankPage) && totalpage % 2 == 1) {
                        // 「奇数ページの時は空白ページを印刷する」総ページ数が奇数で
                        svf.VrSetForm("BLANK_A4_TATE.frm", 1);
                        svf.VrsOut("BLANK", "BLANK");
                        svf.VrEndPage();
                    }
                }
            }
        }
        for (final KNJA133P_0 knja133p : knjobj) {
            knja133p.close();
        }
    }

    private List<Student> getStudentList(final DB2UDB db2, final Param param) {

        final List<String> schregnoList = param.getSchregnoList(db2);

        final List<Student> studentList = new ArrayList<Student>();
        for (final String schregno : schregnoList) {
            final Student student = new Student(schregno, db2, param);
            studentList.add(student);
        }
        Student.setSchregEntGrdHistComebackDat(db2, param, studentList);
        return studentList;
    }

    private static String defstr(final Object s) {
        return null == s ? "" : StringUtils.defaultString(s.toString());
    }

    private static String defstr(final Object s1, final String s2) {
        return null == s1 ? s2 : StringUtils.defaultString(s1.toString(), s2);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static <A, B, C> TreeMap<B, C> getMappedTreeMap(final Map<A, TreeMap<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }


    private static Calendar getCalendarOfDate(final String date) {
        final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(sqlDate);
        return cal;
    }

    private static int getCalendarNendo(final Calendar cal) {
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
            return year - 1;
        }
        return year;
    }

    private static String getString(final String field, final Map map) {
        return KnjDbUtils.getString(map, field);
    }


    private static Integer getInteger(final String field, final Map row) {
        final String s = getString(field, row);
        if (null == s) {
            return null;
        }
        if (NumberUtils.isNumber(s)) {
            return Integer.valueOf(s);
        }
        log.error("not digits : " + s + "/" + field + " / " + row);
        return null;
    }

    private static int getInt(final String field, final Map row) {
        final Integer s = getInteger(field, row);
        if (null == s) {
            return 0;
        }
        return s.intValue();
    }

    /**
     *  文字編集（ブランク挿入）
     */
    private static StringBuffer setFormatInsertBlank(final String hdate) {
        final StringBuffer stb = new StringBuffer();
        final StringBuffer digits = new StringBuffer();
        for (int i = 0; i < hdate.length(); i++) {
            final char ch = hdate.charAt(i);
            if (Character.isDigit(ch) || ch == '元') {
                digits.append(ch);
            } else {
                if (0 < digits.length()) {
                    final int keta = KNJ_EditEdit.getMS932ByteLength(digits.toString());
                    digits.insert(0, StringUtils.repeat(" ", 2 - keta));
                    stb.append(" ").append(digits).append(" ");
                    digits.delete(0, digits.length());
                }
                stb.append(ch);
            }
        }
        return stb;
    }

    /**
     *  年度の編集（ブランク挿入）
     *  @param hdate 編集対象年度「平成3年度」
     *  @param nendo 元号取得用年度
     *  @return 「平成3年度」-> 「平成 3年度」
     */
    private static String setNendoFormat(final DB2UDB db2, final Param param, final String hdate, final String nendo) {
        //日付が無い場合は「平成　年度」の様式とする
        if (hdate == null) {
            if (param._isSeireki) {
                return "　    年度";
            } else {
                return wareki(db2, Integer.parseInt(nendo)) + "    年度";
            }
        }
        //「平成18年度」の様式とする => 数値は２桁
        return setFormatInsertBlank(hdate).toString();
    }

    private static String wareki(final DB2UDB db2, final int nen) {
        final String warekinen = KNJ_EditDate.gengou(db2, nen);
        if (null != warekinen && warekinen.length() > 2) {
            return warekinen.substring(0, 2);
        }
        return "";
    }

    /**
     *  日付の編集（ブランク挿入）
     *  @param hdate 編集対象日付「平成18年1月1日」
     *  @param nendo 元号取得用年度
     *  @return 「平成3年1月1日」-> 「平成 3年 1月 1日」
     */
    private static String setDateFormat(final DB2UDB db2, final Param param, final String hdate, final String nendo) {
        if (hdate == null) {
            if (param._isSeireki) {
                return "　    年    月    日";
            } else {
                //日付が無い場合は「平成　年  月  日」の様式とする
                return wareki(db2, Integer.parseInt(nendo)) + "    年    月    日";
            }
        }
        //「平成18年 1月 1日」の様式とする => 数値は２桁
        return setFormatInsertBlank(hdate).toString();
    }

    /**
     * 日付の編集（XXXX年XX月XX日の様式に編集）
     * @param hdate
     * @return
     */
    private static String setDateFormat2(final Param param, final String hdate) {
        if (StringUtils.isBlank(hdate)) {
            return "　  年  月  日";
        }
        return setFormatInsertBlankDate(new StringBuffer(hdate)).toString();
    }

    /**
     * 文字編集（日付の数字が１桁の場合、ブランクを挿入）
     * @param stb
     * @return
     */
    private static StringBuffer setFormatInsertBlankDate(final StringBuffer stb) {
        int n = 0;
        for (int i = 0; i < stb.length(); i++) {
            final char ch = stb.charAt(i);
            if (Character.isDigit(ch)) {
                n++;
            } else if (0 < n) {
                if (1 == n) {
                    stb.insert(i - n, " ");
                    i++;
                    n = 0;
                }
            }
        }
        return stb;
    }

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP("2002-10-27")     :平成14年10月27日
     *   String dat = h_format_JP("2002/10/27")     :平成14年10月27日
     *----------------------------------------------------------------------------------------------*/
    private static String h_format_JP(final DB2UDB db2, final Param param, final String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            Calendar cal = new GregorianCalendar(new Locale("ja","JP"));
            cal.setTime(dat);
            final int nen = cal.get(Calendar.YEAR);
            final int tsuki = cal.get(Calendar.MONTH) + 1;
            final int hi = cal.get(Calendar.DATE);
            if (param._isSeireki) {
                hdate = nen + "年" + tsuki + "月" + hi + "日";
            } else {
                hdate = KNJ_EditDate.gengou(db2, nen, tsuki, hi);
            }
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JPの括り

    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(日本)
     * ※使い方
     *   String dat = h_format_JP_M("2002-10-27")       :平成14年10月
     *   String dat = h_format_JP_M("2002/10/27")       :平成14年10月
     *----------------------------------------------------------------------------------------------*/
    private static String h_format_JP_M(final DB2UDB db2, final Param param, final String strx) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    try {                                       //applyPatternを追加 2003/12/15
                        sdf.applyPattern("yyyyMMdd");
                        dat = sdf.parse(strx);
                    } catch (Exception e3) {
                        hdate = "";
                        return hdate;
                    }
                }
            }
            Locale local = new Locale("ja","JP");
            Calendar cal = new GregorianCalendar(local);
            cal.setTime(dat);
            final int nen = cal.get(Calendar.YEAR);
            final int tsuki = cal.get(Calendar.MONTH) + 1;
            final int hi = cal.get(Calendar.DATE);
            String stra;
            if (param._isSeireki) {
                stra = nen + "年" + tsuki + "月" + hi + "日";
            } else {
                stra = KNJ_EditDate.gengou(db2, nen, tsuki, hi);
            }
            int ia = stra.indexOf('月');
            if( ia>=0 )     hdate = stra.substring(0,ia+1);
            else            hdate = "";
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_JP_Mの括り

    private static String listString(final Collection coll, final int depth) {
        if (null == coll) {
            return "null";
        } else if (coll.size() == 0) {
            return "[]";
        }
        final String space = StringUtils.repeat("  ", depth);
        final StringBuffer stb = new StringBuffer();
        stb.append(space).append("[").append("\n");
        String comma = "  ";
        final List list = new ArrayList(coll);
        for (int i = 0; i < list.size(); i++) {
            final Object o = list.get(i);
            stb.append(space).append(comma);
            if (o instanceof List) {
                stb.append(listString((List) o, depth + 1));
            } else if (null == o) {
                stb.append("null");
            } else if (o instanceof Map.Entry) {
                final Map.Entry e = (Map.Entry) o;
                final Object v = e.getValue();
                stb.append(space).append(e.getKey()).append("=").append(v instanceof Collection ? listString((Collection) v, depth + 1) : v);
            } else if (list.size() == 0) {
                stb.append("[]");
            } else {
                stb.append(o.toString());
            }
            stb.append("\n");
            comma = ", ";
        }
        stb.append(space).append("]").append("\n");
        return stb.toString();
    }

    private static String notBlank(final String s1, final String s2) {
        return StringUtils.isBlank(s1) ? s2 : s1;
    }

    /**
     * 生徒情報
     */
    private static class Student {
        final String _schregno;
        final String _schoolKind;
        SchregRegdDat _loginRegd = new SchregRegdDat();
        final List<SchregRegdDat> _regdList;
        final PersonalInfo _personalInfo;
        List<PersonalInfo> _schregEntGrdHistComebackDatList;
        final List<Attend> _attendList;
        final List<HTrainremarkPdat> _htrainremarkPdatList;
        final HtrainremarkPHdat _htrainremarkPHdat;
        final List<ActRecord> _actRecordList;
        final List<ClassView> _classViewList;
        final List<ValueRecord> _valueRecordList;
        final List<String> _afterGraduatedCourseTextList;
        final Map<String, List<KNJA134P_3.ClassRemark>> _gradecdClassRemarkListMap;
        protected List<PersonalInfo> _printEntGrdHistList;
        Set<String> _notPrintEntGrdComebackYear = new HashSet<String>();
        public Student(final String schregno, final DB2UDB db2, final Param param) {
            _schregno = schregno;
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param._year + "' AND GRADE IN (SELECT MAX(GRADE) FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + _schregno + "' AND YEAR = '" + param._year + "') "));

            final String psRegdKey = "PS_REGD";
            if (null == param.getPs(psRegdKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ");
                stb.append(" FROM SCHREG_REGD_DAT ");
                stb.append(" WHERE SCHREGNO = ? ");
                if (null != param._startYear) {
                    stb.append(" AND YEAR >= '" + param._startYear + "' ");
                }
                stb.append(" ORDER BY YEAR, SEMESTER ");

                if (param._isOutputDebugQuery) {
                    log.info(" regd sql = " + stb.toString());
                }

                param.setPs(psRegdKey, db2, stb.toString());
            }

            _regdList = new ArrayList<SchregRegdDat>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psRegdKey), new String[] { _schregno })) {
                SchregRegdDat regd = new SchregRegdDat();
                regd._year = KnjDbUtils.getString(row, "YEAR");
                regd._semester = KnjDbUtils.getString(row, "SEMESTER");
                regd._grade = KnjDbUtils.getString(row, "GRADE");
                regd._hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                regd._coursecd = KnjDbUtils.getString(row, "COURSECD");
                regd._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                regd._coursecode = KnjDbUtils.getString(row, "COURSECODE");
                if (param._year.equals(regd._year) && param._gakki.equals(regd._semester)) {
                    _loginRegd = regd;
                }
                _regdList.add(regd);
            }
            _personalInfo = PersonalInfo.load(db2, param, this, null);
            _attendList = Attend.load(db2, param, _schregno);
            _htrainremarkPdatList = HTrainremarkPdat.load(db2, param, _schregno);
            _htrainremarkPHdat = HtrainremarkPHdat.load(db2, param, _schregno);
            _actRecordList = ActRecord.load(db2, param, _schregno);
            _classViewList = ClassView.load(db2, param, _schregno, _personalInfo._entGrdHistDat);
            _valueRecordList = ValueRecord.load(db2, param, _schregno);
            _afterGraduatedCourseTextList = AfterGraduatedCourse.loadTextList(db2, param, _schregno, _personalInfo._gakusekiList);
            _gradecdClassRemarkListMap = KNJA134P_3.ClassRemark.loadGradecdClassRemarkListMap(db2, param, this);
        }

        private String getGrade(final String year) {
            if (null != year) {
                for (final SchregRegdDat regd : _regdList) {
                    if (year.equals(regd._year)) {
                        return regd._grade;
                    }
                }
            }
            return null;
        }

        protected List<PersonalInfo> getPrintSchregEntGrdHistList(final Param param) {
            final List<PersonalInfo> rtn = new ArrayList();
            // 復学が同一年度の場合、復学前、復学後を表示
            // 復学が同一年度ではない場合、復学後のみ表示
            final List<PersonalInfo> pInfoList = new ArrayList();
            pInfoList.addAll(_schregEntGrdHistComebackDatList);
            pInfoList.add(_personalInfo);
            for (final PersonalInfo pInfo : pInfoList) {
                final int begin = pInfo.getYearBegin();
                final int end = pInfo.getYearEnd(param);
                if (param._isOutputDebug && pInfoList.size() > 1) {
                    log.info(" add PersonalInfo " + begin + ", " + end);
                }
                final boolean useStartYear = rtn.size() > 0;
                rtn.addAll(pInfo.fuyasu(this, param, useStartYear));
            }
            if (rtn.isEmpty()) {
                if (pInfoList.size() == 0) {
                    log.fatal("対象データがない!");
                } else {
                    log.fatal("最後のデータを表示:" + rtn);
                    rtn.add(pInfoList.get(pInfoList.size() - 1));
                    rtn.get(0)._isFirst = true;
                }
            } else {
            }
            if (param._isOutputDebug || rtn.size() > 1) {
                log.warn(" schregno = " + _schregno + ", printEntgrdhistList = " + rtn.size() + ", notPrintEntGrdComebackYear = " + _notPrintEntGrdComebackYear);
            }
            for (final PersonalInfo pInfo : rtn) {
                final int begin = pInfo.getYearBegin();
                final int end = pInfo.getYearEnd(param);
                for (int y = begin; y <= end; y++) {
                    final String sy = String.valueOf(y);
                    if (_notPrintEntGrdComebackYear.contains(sy)) {
                        log.info(" set print year : " + sy);
                        _notPrintEntGrdComebackYear.remove(sy);
                    }
                }
            }
            return rtn;
        }

        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getPersonalInfoYearEnd(final PersonalInfo target, final Param param) {
            final TreeSet<Integer> yearSetAll = new TreeSet<Integer>();
            final List<PersonalInfo> personalInfoList = _printEntGrdHistList;
            for (final ListIterator<PersonalInfo> it = personalInfoList.listIterator(personalInfoList.size()); it.hasPrevious();) { // 新しい生徒情報順
                final PersonalInfo personalInfo = it.previous();
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd(param);
                final TreeSet<Integer> yearSet = new TreeSet<Integer>();
                for (int y = begin; y <= end; y++) {
                    final Integer year = new Integer(y);
                    if (yearSetAll.contains(year)) {
                        // 新しい生徒情報で表示されるものは含まない
                    } else {
                        yearSetAll.add(year);
                        yearSet.add(year);
                    }
                }
                if (target == personalInfo) {
                    if (yearSet.isEmpty()) {
                        return -1; // 対象の生徒情報は成績等は表示しない
                    }
                    return yearSet.last().intValue();
                }
            }
            return -1; // 対象の生徒情報は成績等は表示しない
        }

        public static TreeSet<String> gakusekiYearSet(final List<Gakuseki> regdRecordList) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final Gakuseki g : regdRecordList) {
                set.add(g._year);
            }
            return set;
        }

        public int currentGradeCd(final Param param) {
            final int paramYear = Integer.parseInt(param._year);
            int diffyear = 100;
            int currentGrade = -1;
            for (final Gakuseki gakuseki : _personalInfo._gakusekiList) {
                if (!StringUtils.isNumeric(gakuseki._year) || -1 == gakuseki._gradeCd) {
                    continue;
                }
                final int dy = paramYear - Integer.parseInt(gakuseki._year);
                if (dy >= 0 && diffyear > dy) {
                    currentGrade = gakuseki._gradeCd;
                    diffyear = dy;
                }
            }
            return currentGrade;
        }

        private static void setSchregEntGrdHistComebackDat(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            final Map<String, List<String>> schregComebackDateMap = new HashMap<String, List<String>>();
            try {
                for (final Student student : studentList) {
                    student._schregEntGrdHistComebackDatList = Collections.emptyList();
                }
                if (!param._hasSchregEntGrdHistComebackDat) {
                    return;
                }
                final String sql =
                        " SELECT T1.* "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' "
                        + (null != param._startYear ? " AND (T1.GRD_DATE IS NULL OR FISCALYEAR(T1.GRD_DATE) >= '" + param._startYear + "') " : "")
                        + " ORDER BY COMEBACK_DATE ";
                if (param._isOutputDebugQuery) {
                    log.info(" comeback sql = " + sql);
                }
                ps = db2.prepareStatement(sql);
                for (final Student student : studentList) {
                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] { student._schregno } )) {
                        getMappedList(schregComebackDateMap, student._schregno).add(KnjDbUtils.getString(row, "COMEBACK_DATE"));
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
            for (final Student student : studentList) {
                if (null == schregComebackDateMap.get(student._schregno)) {
                    continue;
                }
                student._schregEntGrdHistComebackDatList = new ArrayList<PersonalInfo>();
                final List<String> comebackDateList = schregComebackDateMap.get(student._schregno);
                if (param._isOutputDebug) {
                    log.info(" schregno = " + student._schregno + ",  comebackdate = " + comebackDateList);
                }
                for (final String comebackDate : comebackDateList) {
                    PersonalInfo comebackPersonalInfo = PersonalInfo.load(db2, param, student, comebackDate);
                    student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
                }
            }
        }
    }

    /**
     * 学校データ
     */
    private static class SchoolInfo {
        final String _schoolName1;
        final String _certifSchoolName;
        final String _principalName;
        final String _schoolZipCd;
        final String _schoolAddr1;
        final String _schoolAddr2;
        final String _remark2;
        final String _remark3;
        public SchoolInfo(
                final String schoolName1,
                final String certifSchoolName1,
                final String principalName,
                final String schoolZipCd,
                final String schoolAddr1,
                final String schoolAddr2,
                final String remark2,
                final String remark3) {
            _schoolName1 = schoolName1;
            _certifSchoolName = certifSchoolName1;
            _principalName = principalName;
            _schoolZipCd = schoolZipCd;
            _schoolAddr1 = schoolAddr1;
            _schoolAddr2 = schoolAddr2;
            _remark2 = remark2;
            _remark3 = remark3;
        }

        /**
         * 学校データを得る
         */
        private static SchoolInfo load(final DB2UDB db2, final Param param) {
            final Map certiifSchoolMap = new HashMap();
            final String certifKindCd = "116";
            final String sql1 = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + param._year + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";

            for (final Map row1 : KnjDbUtils.query(db2, sql1)) {
                certiifSchoolMap.put("SCHOOL_NAME", getString("SCHOOL_NAME", row1));
                certiifSchoolMap.put("PRINCIPAL_NAME", getString("PRINCIPAL_NAME", row1));
                certiifSchoolMap.put("REMARK2", getString("REMARK2", row1));
                certiifSchoolMap.put("REMARK3", getString("REMARK3", row1));
            }

            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + certiifSchoolMap.get("SCHOOL_NAME") + "]");
            final String certifSchoolName = (String) certiifSchoolMap.get("SCHOOL_NAME");
            final String principlalName = (String) certiifSchoolMap.get("PRINCIPAL_NAME");
            final String remark2 = (String) certiifSchoolMap.get("REMARK2");
            final String remark3 = (String) certiifSchoolMap.get("REMARK3");

            String schoolName1 = null;
            String schoolZipCd = null;
            String schoolAddr1 = null;
            String schoolAddr2 = null;
            final String psKey = "PS_KNJ_Schoolinfo";
            if (null == param.getPs(psKey)) {
                String sql = null;
                final Map paramMap = new HashMap();
                if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                    paramMap.put("schoolMstSchoolKind", Param.SCHOOL_KIND);
                }
                try {
                    sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
                } catch (Throwable e) {
                    log.warn("old KNJ_SchoolinfoSql.");
                    sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
                }
                param.setPs(psKey, db2, sql);
            }
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {param._year, param._year})) {
                schoolName1 = getString("SCHOOLNAME1", row);
                schoolAddr1 = getString("SCHOOLADDR1", row);
                schoolZipCd = getString("SCHOOLZIPCD", row);
                schoolAddr2 = getString("SCHOOLADDR2", row);
            }
            return new SchoolInfo(schoolName1, certifSchoolName, principlalName, schoolZipCd, schoolAddr1, schoolAddr2, remark2, remark3);
        }
    }

    /**
     * 生徒情報
     */
    private static class PersonalInfo {
        final Student _student;
        final String _studentName1;
        final String _studentName;
        final String _studentRealName;
        final String _studentNameHistFirst;
        final String _studentKana;
        final String _guardName;
        final String _guardNameHistFirst;
        final String _guardKana;
        final boolean _useRealName;
        final boolean _nameOutputFlg;
        final String _courseName;
        final String _majorName;
        final String _birthdayFlg;
        final String _birthday;
        final String _birthdayStr;
        final String _sex;
        final String _finishDate;
        final String _installationDiv;
        final String _schAddress1;
        final String _schAddress2;
        final String _jName;
        final String _finschoolTypeName;
        final String _recordDiv;

        String _entYear;
        Semester _entSemester;
        String _entDate;
        String _entReason;
        String _entSchool;
        String _entAddr;
        Integer _entDiv;
        String _entDivName;
        String _grdYear;
        Semester _grdSemester;
        String _grdDate;
        String _grdReason;
        String _grdSchool;
        String _grdAddr;
        String _grdNo;
        Integer _grdDiv;
        String _grdDivName;
        final String _curriculumYear;
        final String _tengakuSakiGrade;
        final String _tengakuSakiZenjitu;
        final String _nyugakumaeSyussinJouhou;
        String _comebackDate;
        boolean _isFirst;

        List<Map<String, String>> _addressRowList;
        List<Address> _addressList;
        List<Map<String, String>> _guardianAddressRowList;
        List<Address> _guardianAddressList;
        List<Gakuseki> _gakusekiList;
        List<Map<String, String>> _transferInfoList;
        SchregEntGrdHistDat _entGrdHistDat;

        /**
         * コンストラクタ。
         */
        public PersonalInfo(final DB2UDB db2, final Student student, final Map<String, String> row, final Param param, final Map<String, String> entGrdHistRow) {
            _student = student;
            _entYear    = getString("ENT_YEAR", entGrdHistRow);
            _entSemester = Semester.get(param, _entYear, KnjDbUtils.getString(entGrdHistRow, "ENT_SEMESTER"));
            _entDate    = getString("ENT_DATE", entGrdHistRow);
            _entReason  = getString("ENT_REASON", entGrdHistRow);
            _entSchool  = getString("ENT_SCHOOL", entGrdHistRow);
            _entAddr    = getString("ENT_ADDR", entGrdHistRow);
            _entDiv     = StringUtils.isNumeric(getString("ENT_DIV", entGrdHistRow)) ? Integer.valueOf(getString("ENT_DIV", entGrdHistRow)) : null;
            _entDivName = getString("ENT_DIV_NAME", entGrdHistRow);
            _grdYear    = getString("GRD_YEAR", entGrdHistRow);
            _grdSemester = Semester.get(param, _entYear, KnjDbUtils.getString(entGrdHistRow, "GRD_SEMESTER"));
            _grdDate    = getString("GRD_DATE", entGrdHistRow);
            _grdReason  = getString("GRD_REASON", entGrdHistRow);
            _grdSchool  = getString("GRD_SCHOOL", entGrdHistRow);
            _grdAddr    = getString("GRD_ADDR", entGrdHistRow);
            _grdNo      = getString("GRD_NO", entGrdHistRow);
            _grdDiv     = StringUtils.isNumeric(getString("GRD_DIV", entGrdHistRow)) ? Integer.valueOf(getString("GRD_DIV", entGrdHistRow)) : null;
            _grdDivName = getString("GRD_DIV_NAME", entGrdHistRow);

            _curriculumYear = KnjDbUtils.getString(entGrdHistRow, "CURRICULUM_YEAR");
            _tengakuSakiZenjitu = KnjDbUtils.getString(entGrdHistRow, "TENGAKU_SAKI_ZENJITU");
            _tengakuSakiGrade = KnjDbUtils.getString(entGrdHistRow, "TENGAKU_SAKI_GRADE");
            _nyugakumaeSyussinJouhou = KnjDbUtils.getString(entGrdHistRow, "NYUGAKUMAE_SYUSSIN_JOUHOU");

            final String nameHistFirst = StringUtils.defaultString(getString("NAME_HIST_FIRST", row));
            final String realNameHistFirst = StringUtils.defaultString(getString("REAL_NAME_HIST_FIRST", row));
            final String nameWithRealNameHistFirst = StringUtils.defaultString(getString("NAME_WITH_RN_HIST_FIRST", row));

            final String nameKana = StringUtils.defaultString(getString("NAME_KANA", row));
            final String guardKana = StringUtils.defaultString(getString("GUARD_KANA", row));
            final String guardName = StringUtils.defaultString(getString("GUARD_NAME", row));
            final boolean useGuardRealName = "1".equals(getString("USE_GUARD_REAL_NAME", row));
            final boolean guardNameOutputFlg = "1".equals(getString("GUARD_NAME_OUTPUT_FLG", row));
            final String realNameKana = StringUtils.defaultString(getString("REAL_NAME_KANA", row));
            final String guardRealKana = StringUtils.defaultString(getString("GUARD_REAL_KANA", row));
            final String guardRealName = StringUtils.defaultString(getString("GUARD_REAL_NAME", row));
            final String guardRealNameHistFirst = StringUtils.defaultString(getString("G_R_NAME_WITH_RN_HIST_FIRST", row));
            final String guardNameWithGuardRealNameHistFirst = StringUtils.defaultString(getString("G_NAME_WITH_RN_HIST_FIRST", row));
            _useRealName = "1".equals(getString("USE_REAL_NAME", row));
            _nameOutputFlg = "1".equals(getString("NAME_OUTPUT_FLG", row));
            _studentName = StringUtils.defaultString(getString("NAME", row));
            _studentRealName = StringUtils.defaultString(getString("REAL_NAME", row));
            if (_useRealName) {
                if (_nameOutputFlg) {
                    _studentKana = StringUtils.isBlank(realNameKana + nameKana) ? "" : realNameKana + "（" + nameKana + "）";
                    _studentName1         = StringUtils.isBlank(_studentRealName + _studentName) ? "" : _studentRealName + "（" + _studentName + "）";
                    _studentNameHistFirst = StringUtils.isBlank(realNameHistFirst + nameWithRealNameHistFirst) ? "" : realNameHistFirst + "（" + nameWithRealNameHistFirst + "）";
                } else {
                    _studentKana = realNameKana;
                    _studentName1         = _studentRealName;
                    _studentNameHistFirst = realNameHistFirst;
                }
            } else {
                _studentKana = nameKana;
                _studentName1         = _studentName;
                _studentNameHistFirst = nameHistFirst;
            }
            if (useGuardRealName) {
                if (guardNameOutputFlg) {
                    _guardKana = StringUtils.isBlank(guardRealKana + guardKana) ? "" : guardRealKana + "（" + guardKana + "）";
                    _guardName = StringUtils.isBlank(guardRealName + guardName) ? "" : guardRealName + "（" + guardName + "）";
                    _guardNameHistFirst = StringUtils.isBlank(guardRealNameHistFirst + guardNameWithGuardRealNameHistFirst) ? "" : guardRealNameHistFirst + "（" + guardNameWithGuardRealNameHistFirst + "）";
                } else {
                    _guardKana = guardRealKana;
                    _guardName = guardRealName;
                    _guardNameHistFirst = guardRealNameHistFirst;
                }
            } else {
                _guardKana = guardKana;
                _guardName = guardName;
                _guardNameHistFirst = StringUtils.defaultString(getString("GUARD_NAME_HIST_FIRST", row));
            }

            _courseName = getString("COURSENAME", row);
            _majorName = getString("MAJORNAME", row);

            _birthdayFlg = getString("BIRTHDAY_FLG", row);
            _birthday = getString("BIRTHDAY", row);
            final boolean isWareki = (param._isNaraken || param._isKyoto);
            _birthdayStr = getBirthday(db2, _birthday, _birthdayFlg, param, isWareki);
            _sex = getString("SEX", row);
            _finishDate = setDateFormat(db2, param, h_format_JP_M(db2, param, getString("FINISH_DATE", row)), param._year);
            _installationDiv = getString("INSTALLATION_DIV", row);
            _schAddress1 = getString("ADDR1", row);
            _schAddress2 = getString("ADDR2", row);
            _jName = getString("J_NAME", row);
            _finschoolTypeName = getString("FINSCHOOL_TYPE_NAME", row);
            _recordDiv = getString("RECORD_DIV", row);
        }

        public Collection<PersonalInfo> fuyasu(final Student student, final Param param, final boolean useStartYear) {
            return Collections.singleton(this);
        }

        private static String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param, final boolean isWareki) {
            String birthday = "";
            if ((param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) && !isWareki) {
                birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + setDateFormat2(param, KNJ_EditDate.h_format_JP_MD(date));
            } else {
                birthday = setDateFormat2(param, h_format_JP(db2, param, date));
            }
            if (!StringUtils.isBlank(birthday)) {
                birthday += "生";
            }
            return birthday;
        }

        public int getYearBegin() {
            return null == _entDate ? 0 : getCalendarNendo(getCalendarOfDate(_entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _grdDate ? 9999 : getCalendarNendo(getCalendarOfDate(_grdDate)));
        }

        private static boolean isTargetYear(final Student student, final PersonalInfo personalInfo, final String dataYear, final Param param) {
            return null == dataYear || (personalInfo.getYearBegin() <= Integer.parseInt(dataYear) && Integer.parseInt(dataYear) <= student.getPersonalInfoYearEnd(personalInfo, param));
        }

        /**
         * 生徒情報を得る
         */
        public static PersonalInfo load(final DB2UDB db2, final Param param, final Student student, final String comebackDate) {
            final String psKey2 = "PS_PERSONAL2" + StringUtils.defaultString(comebackDate);
            if (null == param.getPs(psKey2)) {
                final String sql = sql_state(param, comebackDate);
                if (param._isOutputDebugQuery) {
                    log.info(" personal2(" + psKey2 + ") sql = " + sql);
                }
                param.setPs(psKey2, db2, sql);
            }
            final Map<String, String> entGrdHistRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {student._schregno}));

            final String psKey = "PS_PERSONAL" + StringUtils.defaultString(comebackDate);
            if (null == param.getPs(psKey)) {
                final String sql = sql_info_reg(param, comebackDate);
                if (param._isOutputDebugQuery) {
                    log.info(" personalinfo(" + psKey + ") sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }
            final Map<String, String> baseRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno, student._schregno}));
            PersonalInfo personalInfo = new PersonalInfo(db2, student, baseRow, param, entGrdHistRow);

            personalInfo._addressRowList = Address.getRowList(db2, param, false, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._addressList = Address.load(personalInfo._addressRowList);
            personalInfo._guardianAddressRowList = Address.getRowList(db2, param, true, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._guardianAddressList = Address.load(personalInfo._guardianAddressRowList);
            personalInfo._comebackDate = comebackDate;
            personalInfo._gakusekiList = Gakuseki.load(db2, param, student._schregno, personalInfo._grdDate);
            personalInfo._transferInfoList = loadTransferList(db2, param, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._entGrdHistDat = SchregEntGrdHistDat.load(db2, param, student._schregno, comebackDate);

            return personalInfo;
        }

        /**
         * 住所履歴を得る
         */
        public static List<Map<String, String>> loadTransferList(final DB2UDB db2, final Param param, final Student student, final String startDate, final String endDate) {
            final List<Map<String, String>> transferList = new ArrayList<Map<String, String>>();
            if (null == param.seito) {
                return transferList;
            }
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * ");
            sql.append(" FROM SCHREG_TRANSFER_DAT ");
            sql.append(" WHERE SCHREGNO = '" + student._schregno + "' ");
            if (null != endDate) {
                sql.append(" AND TRANSFER_SDATE <= '" + endDate + "' ");
            }
            if (null != param._startYear) {
                sql.append(" AND FISCALYEAR(TRANSFER_SDATE) >= '" + param._startYear + "' ");
            }
            sql.append(" ORDER BY TRANSFER_SDATE ");
            transferList.addAll(KnjDbUtils.query(db2, sql.toString()));
            return transferList;
        }

        public static String sql_info_reg(final Param param, final String comebackDate) {

            final String q = "?";
            final StringBuffer sql = new StringBuffer();
            final String switch2 = "1";
            final String switch3 = "1";
            final String switch6 = "1";

            sql.append("SELECT ");
            sql.append("BASE.NAME,");
            sql.append("BASE.NAME_KANA,");
            sql.append("BASE.GRD_DATE, ");
            sql.append("T14.NAME AS NAME_HIST_FIRST, ");
            sql.append("T18.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            sql.append("BASE.BIRTHDAY, BASE.SEX AS SEX_FLG, T7.ABBV1 AS SEX,");
            sql.append("T21.BIRTHDAY_FLG, ");
            sql.append("BASE.REAL_NAME, ");
            sql.append("BASE.REAL_NAME_KANA, ");
            sql.append("T18.REAL_NAME AS REAL_NAME_HIST_FIRST, ");
            sql.append("T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,");
            // 課程・学科・コース
            if (!switch2.equals("0")) {
                sql.append("T3.COURSENAME,T4.MAJORNAME,T5.COURSECODENAME,");
            }
            // 入学
            sql.append("ENTGRD.ENT_DATE, ENTGRD.ENT_DIV,");
            sql.append("(SELECT DISTINCT ANNUAL FROM SCHREG_REGD_DAT ST1,SCHREG_ENT_GRD_HIST_DAT ST2 ");
            sql.append("WHERE ST1.SCHREGNO=ST2.SCHREGNO AND ST2.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' AND ST1.YEAR=FISCALYEAR(ST2.ENT_DATE) AND ");
            sql.append("ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,");

            sql.append("(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND ENTGRD.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,");
            sql.append( "(SELECT MIN(TBL1.ANNUAL) FROM SCHREG_REGD_DAT TBL1 WHERE TBL1.SCHREGNO=T1.SCHREGNO AND TBL1.ANNUAL='04') AS ENTER_GRADE2,");
            sql.append( "(SELECT MIN(TBL2.YEAR)   FROM SCHREG_REGD_DAT TBL2 WHERE TBL2.SCHREGNO=T1.SCHREGNO AND TBL2.ANNUAL='04') || '-04-01' AS ENT_DATE2,");

            // 住所
            if (!switch3.equals("0")) {
                sql.append("VALUE(T8.ADDR1,'') || VALUE(T8.ADDR2,'') AS ADDR,");
                sql.append("T8.ADDR1,T8.ADDR2,T8.TELNO,T8.ZIPCD,");
                sql.append("T8.ADDR_FLG,");
            }
            // 卒業中学情報
            sql.append("ENTGRD.FINISH_DATE,");
            sql.append("FIN_S.FINSCHOOL_NAME AS J_NAME,");
            sql.append("NM_MST.NAME1 AS INSTALLATION_DIV,");
            sql.append("VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            // 保護者情報
            sql.append("T12.GUARD_NAME, ");
            sql.append("T12.GUARD_REAL_NAME, ");
            sql.append("T16.GUARD_NAME AS GUARD_NAME_HIST_FIRST, ");
            sql.append("T12.GUARD_REAL_KANA, ");
            sql.append("T12.GUARD_KANA,");
            sql.append("T20.GUARD_REAL_NAME AS G_R_NAME_WITH_RN_HIST_FIRST, ");
            sql.append("T20.GUARD_NAME      AS G_NAME_WITH_RN_HIST_FIRST, ");
            sql.append("(CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("T11.NAME_OUTPUT_FLG, ");
            sql.append("(CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("T26.GUARD_NAME_OUTPUT_FLG, ");
            if (param._isMusashinohigashi) {
                sql.append("CASE WHEN T1.HR_CLASS IN ('003', '004', '005') THEN 2 ELSE 1 END AS RECORD_DIV, ");
            } else {
                sql.append("CAST(NULL AS VARCHAR(1)) AS RECORD_DIV, ");
            }
            sql.append("T1.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("(   SELECT     * ");
            sql.append("FROM       SCHREG_REGD_DAT T1 ");
            sql.append("WHERE      T1.SCHREGNO = " + q + " AND T1.YEAR= '" + param._year + "' ");
            if (null != param._startYear) {
                sql.append(" AND T1.YEAR >= '" + param._startYear + "' ");
            }
            if (switch6.equals("1")) { // 学期を特定
                sql.append("AND T1.SEMESTER = '" + param._gakki + "' ");
            } else {
                // 最終学期
                sql.append("AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO = " + q + " AND YEAR = '" + param._year + "')");
            }
            sql.append(") T1 ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR ");
            sql.append("  AND T6.SEMESTER = T1.SEMESTER ");
            sql.append("  AND T6.GRADE = T1.GRADE ");
            sql.append("  AND T6.HR_CLASS = T1.HR_CLASS ");
            if (param._isMusashinohigashi) {
                sql.append("LEFT JOIN SCHREG_REGD_FI_DAT REGDFI ON REGDFI.SCHREGNO = T1.SCHREGNO ");
                sql.append("  AND REGDFI.YEAR = T1.YEAR ");
                sql.append("  AND REGDFI.SEMESTER = T1.SEMESTER ");
                sql.append("LEFT JOIN SCHREG_REGD_FI_HDAT REGDFIH ON REGDFIH.YEAR = REGDFI.YEAR ");
                sql.append("  AND REGDFIH.SEMESTER = REGDFI.SEMESTER ");
                sql.append("  AND REGDFIH.GRADE = REGDFI.GRADE ");
                sql.append("  AND REGDFIH.HR_CLASS = REGDFI.HR_CLASS ");
            }
            // 卒業情報有りの場合
            sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(        " AND T10.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = BASE.SCHREGNO AND ENTGRD.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=BASE.SEX ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = ENTGRD.FINSCHOOLCD ");
            sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            if (!switch2.equals("0")) {
                sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
                sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
                sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
                sql.append("AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
            }

            // 生徒住所
            if (switch3.equals("1")) {
                sql.append("LEFT JOIN SCHREG_ADDRESS_DAT AS T8 ");
                sql.append("INNER JOIN(");
                sql.append("SELECT     MAX(ISSUEDATE) AS ISSUEDATE ");
                sql.append("FROM       SCHREG_ADDRESS_DAT ");
                sql.append("WHERE      SCHREGNO = " + q + " AND FISCALYEAR(ISSUEDATE) <= '" + param._year + "' ");
                sql.append(")T9 ON T9.ISSUEDATE = T8.ISSUEDATE ON T8.SCHREGNO = T1.SCHREGNO ");
            }
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '02' ");
            // 保護者情報
            sql.append("LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = BASE.SCHREGNO ");
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T13 ON T13.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T14 ON T14.SCHREGNO = T13.SCHREGNO AND T14.ISSUEDATE = T13.ISSUEDATE ");

            // 保護者履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T15 ON T15.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN GUARDIAN_HIST_DAT T16 ON T16.SCHREGNO = T15.SCHREGNO AND T16.ISSUEDATE = T15.ISSUEDATE ");
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T17 ON T17.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T18 ON T18.SCHREGNO = T17.SCHREGNO AND T18.ISSUEDATE = T17.ISSUEDATE ");

            // 保護者履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T19 ON T19.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN GUARDIAN_HIST_DAT T20 ON T20.SCHREGNO = T19.SCHREGNO AND T20.ISSUEDATE = T19.ISSUEDATE ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = BASE.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");
            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = BASE.SCHREGNO AND T26.DIV = '02' ");
            return sql.toString();
        }

        private static String sql_state(final Param param, final String comebackDate) {
            boolean hasTengakuSakiGrade;
            if (null != comebackDate) {
                hasTengakuSakiGrade = param._hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT_TENGAKU_SAKI_GRADE;
            } else {
                hasTengakuSakiGrade = param._hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE;
            }

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ESEME.SEMESTER AS ENT_SEMESTER, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            sql.append("    ENT_DIV, ");
            sql.append("    A002.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GSEME.SEMESTER AS GRD_SEMESTER, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
            sql.append("    GRD_NO, ");
            sql.append("    GRD_DIV, ");
            sql.append("    A003.NAME1 AS GRD_DIV_NAME, ");
            sql.append("    T1.CURRICULUM_YEAR, ");
            sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
            if (hasTengakuSakiGrade) {
                sql.append("    T1.TENGAKU_SAKI_GRADE, ");
            } else {
                sql.append("    CAST(NULL AS VARCHAR(1)) AS TENGAKU_SAKI_GRADE, ");
            }
            sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
            sql.append(" FROM ");
            if (null != comebackDate) {
                sql.append("    SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            } else {
                sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
            }
            sql.append("    LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' AND A002.NAMECD2 = T1.ENT_DIV ");
            sql.append("    LEFT JOIN NAME_MST A003 ON A003.NAMECD1 = 'A003' AND A003.NAMECD2 = T1.GRD_DIV ");
            sql.append("    LEFT JOIN SEMESTER_MST ESEME ON ESEME.SEMESTER <> '9' AND T1.ENT_DATE BETWEEN ESEME.SDATE AND ESEME.EDATE ");
            sql.append("    LEFT JOIN SEMESTER_MST GSEME ON GSEME.SEMESTER <> '9' AND T1.GRD_DATE BETWEEN GSEME.SDATE AND GSEME.EDATE ");
            sql.append(" WHERE ");
            sql.append("    T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (null != comebackDate) {
                sql.append("    AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
            }
            if (null != comebackDate) {
                if (null != param._startYear) {
                    sql.append(" AND FISCALYEAR(T1.ENT_DATE) >= '" + param._startYear + "' ");
                }
            }
            return sql.toString();
        }
    }

    /**
     * 在籍データ
     */
    private static class Gakuseki {

        final int _pos;
        final String _year;
        final String _grade;
        final int _gradeCd;
        final String _hrname;
        final String _attendno;
        final String _nendo;
        final Staff _principal;
        final String _staffSeq;
        final String _principalSeq;
        final String _kaizanFlg;

        public Gakuseki(
                final int i,
                final String year,
                final String grade,
                final int gradeCd,
                String hrname,
                final String attendno,
                final String nendo,
                final Staff principal,
                final String chageOpiSeq,
                final String lastOpiSeq,
                final String flg
                ) {
            _pos = i;
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrname = hrname;
            _attendno = attendno;
            _nendo = nendo;
            _staffSeq = chageOpiSeq;
            _principalSeq = lastOpiSeq;
            _kaizanFlg = flg;
            _principal = principal;
        }

        public static Gakuseki getGakuseki(final String year, final List<Gakuseki> gakusekiList) {
            if (null != year) {
                for (final Gakuseki gakuseki : gakusekiList) {
                    if (year.equals(gakuseki._year)) {
                        return gakuseki;
                    }
                }
            }
            return null;
        }

        private String jpMonthName(String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            return new SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
        }

        private String getStaffNameString(final String staffName, final String fromDate, final String toDate) {
            final String name = null == staffName ? "" : staffName;
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "～" + jpMonthName(toDate) + ")";
            return name + between;
        }

        /**
         * 在籍データのリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<Gakuseki> load(final DB2UDB db2, final Param param, String schregno, final String grdDate) {

            final List<Gakuseki> gakusekiList = new ArrayList();

            if (param.hmap == null) {
                param.hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
            }

            final String psKey1 = "PS_ATTEST";
            if (null == param.getPs(psKey1)) {
                final StringBuffer stb = new StringBuffer();
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
                stb.append("     WHERE T1.SCHREGNO = ? ");
                if (null != param._startYear) {
                    stb.append(" AND T1.YEAR >= '" + param._startYear + "' ");
                }

                param.setPs(psKey1, db2, stb.toString());
            }
            final Map yearAttestMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, param.getPs(psKey1), new Object[] {schregno}), "YEAR");

            final String sql = sqlSchGradeRec(param, schregno, grdDate);
            if (param._isOutputDebugQuery) {
                log.info(" gakuseki sql = " + sql);
            }
            for (final Map row : KnjDbUtils.query(db2, sql)) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                final String principalStaffcd = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD");
                final String principalname = KnjDbUtils.getString(row, "PRINCIPALNAME");

                final Map attest = getMappedMap(yearAttestMap, year);
                final String chageOpiSeq = KnjDbUtils.getString(attest, "CHAGE_OPI_SEQ");
                final String lastOpiSeq = KnjDbUtils.getString(attest, "LAST_OPI_SEQ");
                final String flg = KnjDbUtils.getString(attest, "FLG");

                final int i = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), grade); // 学年
                final int gradeCd = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), grade); // 学年

                String hrname = null;
                if (param.isTokubetsuShien() && "1".equals(param._seitoSidoYorokuUseHrClassTypeP) && "2".equals(param._hrClassType) && param._isSagaken) {
                    hrname = KnjDbUtils.getString(row, "GHR_NAME");
                } else {
                    if ("1".equals(param._useSchregRegdHdat)) {
                        hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    } else if ("0".equals(param._useSchregRegdHdat)) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrClass, param.hmap);
                    }
                    if (hrname == null) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrClass);
                    }
                }
                final String nendo = setNendoFormat(db2, param, (param._isSeireki ? year : KNJ_EditDate.gengou(db2, Integer.parseInt(year))) + "年度", year);

                final Staff principal = new Staff(year, new StaffMst(null, principalname, null, null, null), null, null, param._staffInfo.getStampNo(param, principalStaffcd, year));

                final Gakuseki gakuseki = new Gakuseki(i, year, grade, gradeCd, hrname, attendno, nendo, principal, chageOpiSeq, lastOpiSeq, flg);
                gakusekiList.add(gakuseki);
            }
            return gakusekiList;
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param, final String schregno, final String grdDate) {
            final String certifKind = "116";
            final boolean useGhrName = param.isTokubetsuShien() && "1".equals(param._seitoSidoYorokuUseHrClassTypeP) && "2".equals(param._hrClassType) && param._isSagaken;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MAX(SEMESTER) AS SEMESTER ");
            stb.append("     FROM SCHREG_REGD_DAT ");
            stb.append("     WHERE SCHREGNO = '" + schregno + "' ");
            stb.append("     GROUP BY YEAR ");
            if (null != grdDate) {
                stb.append("     UNION ALL ");
                stb.append("     SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
                stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("         AND '" + grdDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("     WHERE T1.SCHREGNO = '" + schregno + "' ");
            }
            stb.append(" ) ");
            stb.append(" , MIN_YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MIN(SEMESTER) AS SEMESTER ");
            stb.append("     FROM YEAR_SEMESTER ");
            stb.append("     GROUP BY YEAR ");
            stb.append(" ) ");
            if (useGhrName) {
                stb.append("  , GHR_REGD AS ( ");
                stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.SCHREGNO");
                stb.append("      FROM SCHREG_REGD_GHR_DAT T1 ");
                stb.append("      INNER JOIN (SELECT  T2.SCHREGNO, T2.YEAR, MAX(T2.SEMESTER) AS SEMESTER");
                stb.append("                  FROM    SCHREG_REGD_GHR_DAT T2");
                stb.append("                  GROUP BY T2.SCHREGNO, T2.YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("      WHERE   T1.SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
                stb.append("          AND T1.YEAR <= '" + param._year + "'");
                stb.append(" ) ");
            }

            stb.append(" SELECT ");
            stb.append("    REGD.YEAR ");
            stb.append("   ,REGD.GRADE ");
            stb.append("   ,REGD.HR_CLASS ");
            stb.append("   ,REGD.ATTENDNO ");
            stb.append("   ,REGD.ANNUAL ");
            stb.append("   ,REGDH.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("   ,REGDH.HR_CLASS_NAME1");
            }
            if (useGhrName) {
                stb.append("   ,GHRH.GHR_NAME ");
            }
            stb.append("   ,CER.REMARK7 AS PRINCIPALSTAFFCD ");
            stb.append("   ,CER.PRINCIPAL_NAME AS PRINCIPALNAME ");

            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN MIN_YEAR_SEMESTER T2 ON T2.YEAR = REGD.YEAR AND T2.SEMESTER = REGD.SEMESTER ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                              AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            if (useGhrName) {
                stb.append(" LEFT JOIN GHR_REGD GHRR ON GHRR.SCHREGNO = REGD.SCHREGNO AND GHRR.YEAR = REGD.YEAR ");
                stb.append(" LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO AND GHR.YEAR = REGD.YEAR AND GHR.SEMESTER = GHRR.SEMESTER ");
                stb.append(" LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR AND GHRH.SEMESTER = GHR.SEMESTER ");
                stb.append("                                    AND GHRH.GHR_CD = GHR.GHR_CD ");
            }

            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT CER ON CER.YEAR = REGD.YEAR ");
            stb.append("      AND CER.CERTIF_KINDCD = '" + certifKind + "'");
            stb.append(" WHERE   REGD.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND REGD.YEAR <= '" + param._year + "'");
            if (null != param._startYear) {
                stb.append(" AND REGD.YEAR >= '" + param._startYear + "' ");
            }
            stb.append("     AND REGD.YEAR NOT IN ( ");
            stb.append("        SELECT N1.YEAR ");
            stb.append("        FROM SCHREG_REGD_DAT N1 ");
            stb.append("        INNER JOIN SCHREG_REGD_GDAT N2 ON N2.YEAR = N1.YEAR AND N2.GRADE = N1.GRADE ");
            stb.append("        WHERE N1.SCHREGNO = '" + schregno + "' ");
            stb.append("          AND N2.SCHOOL_KIND <> '" + Param.SCHOOL_KIND + "' ");
            stb.append("        ) ");
            stb.append(" ORDER BY REGD.YEAR, REGD.GRADE, REGD.HR_CLASS ");
            return stb.toString();
        }

        public String toString() {
            return "Gakuseki(" + _year + ", " + _grade + ")";
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
        private final Map<String, Map<String, String>> _yearStaffNameSetUp;
        private final Map<String, Map<String, String>> _yearStaffNameHist;
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
            final Map nameSetup = _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year) {
            final Map nameSetup = _yearStaffNameSetUp.get(year);
            return null != nameSetup;
        }

        public List<String> getNameLine(final String year, final Param param, final int keta) {
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
                        } else if (KNJ_EditEdit.getMS932ByteLength(nameReal + n) > keta) {
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
            final TreeMap<String, Map<String, String>> sortedMap = new TreeMap(_yearStaffNameHist);
            Map histDat = null;
            for (final Map histDat0 : sortedMap.values()) {
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
            // log.debug(" year search = " + yearSearch + ",  histDat = " + histDat + ", rtn = " + rtn);
            return rtn;
        }

        public static StaffMst get(final Map<String, StaffMst> staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return staffMstMap.get(staffcd);
        }

        public static Map load(final DB2UDB db2, final String year, final Param param) {
            final Map<String, StaffMst> rtn = new HashMap<String, StaffMst>();

            final String sql1 = "SELECT * FROM STAFF_MST ";
            for (final Map m : KnjDbUtils.query(db2, sql1)) {
                final String staffcd = (String) m.get("STAFFCD");
                final String name = (String) m.get("STAFFNAME");
                final String kana = (String) m.get("STAFFNAME_KANA");
                final String nameReal = (String) m.get("STAFFNAME_REAL");
                final String kanaReal = (String) m.get("STAFFNAME_KANA_REAL");

                final StaffMst s = new StaffMst(staffcd, name, kana, nameReal, kanaReal);

                rtn.put(s._staffcd, s);
            }

            String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT T1 WHERE YEAR <= '" + year + "' AND DIV = '02' ";
            if (null != param._startYear) {
                sql2 += " AND T1.YEAR >= '" + param._startYear + "' ";
            }
            for (final Map<String, String> m : KnjDbUtils.query(db2, sql2)) {
                if (null == rtn.get(m.get("STAFFCD"))) {
                    continue;
                }
                final StaffMst s = rtn.get(m.get("STAFFCD"));

                final Map<String, String> nameSetupDat = new HashMap();
                nameSetupDat.put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
                s._yearStaffNameSetUp.put(m.get("YEAR"), nameSetupDat);
            }

            final StringBuffer sqlHist = new StringBuffer();
            sqlHist.append(" WITH MAX_SDATE AS ( ");
            sqlHist.append("SELECT ");
            sqlHist.append("   STAFFCD, MAX(SDATE) AS SDATE ");
            sqlHist.append(" FROM STAFF_NAME_HIST_DAT ");
            sqlHist.append(" WHERE ");
            sqlHist.append("   FISCALYEAR(SDATE) <= '" + year + "' ");
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
                final StaffMst s = rtn.get(KnjDbUtils.getString(m, "STAFFCD"));

                final Map<String, String> nameHistDat = new HashMap();
                nameHistDat.put("STAFFCD", m.get("STAFFCD"));
                nameHistDat.put("STAFFNAME", m.get("STAFFNAME"));
                nameHistDat.put("STAFFNAME_REAL", m.get("STAFFNAME_REAL"));
                nameHistDat.put("SYEAR", m.get("SYEAR"));
                nameHistDat.put("EYEAR", m.get("EYEAR"));
                s._yearStaffNameHist.put(m.get("SYEAR"), nameHistDat);
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + (_yearStaffNameSetUp.isEmpty() ? "" : ", nameSetupDat=" + _yearStaffNameSetUp) + (_yearStaffNameHist.isEmpty() ? "" : ", yearStaffNameHist = " + _yearStaffNameHist) + ")";
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

        public String getNameString(final Param param, final int keta) {
            final StringBuffer stb = new StringBuffer();
            final List<String> name = _staffMst.getNameLine(_year, param, keta);
            for (int i = 0; i < name.size(); i++) {
                if (null == name.get(i)) continue;
                stb.append(name.get(i));
            }
            return stb.toString();
        }

        public List<String> getNameBetweenLine(final Param param, final int keta) {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";

            final List<String> rtn;
            if (KNJ_EditEdit.getMS932ByteLength(getNameString(param, keta) + between) > keta) {
                rtn = Arrays.asList(new String[]{getNameString(param, keta), between});
            } else {
                rtn = Arrays.asList(new String[]{getNameString(param, keta) + between});
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

    private static class StaffInfo {

        public static final String TR_DIV1 = "1";
        public static final String TR_DIV2 = "2";
        public static final String TR_DIV3 = "3";

        protected Map<String, Map<Integer, String>> _inkanMap = Collections.EMPTY_MAP;
        protected Map<String, List<Map<String, String>>> _yearPrincipalListMap = Collections.EMPTY_MAP;
        protected Map _staffMstMap = Collections.EMPTY_MAP;
        protected Map<String, TreeMap<String, List<Staff>>> _staffClassHistMap = Collections.EMPTY_MAP;

        private void setYearPrincipalMap(final DB2UDB db2, final Param param) {
            _yearPrincipalListMap = new TreeMap();

            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH PRINCIPAL_HIST AS ( ");
            sql.append("     SELECT ");
            sql.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR ORDER BY T2.YEAR, T1.FROM_DATE) AS ORDER ");
            sql.append("     FROM ");
            sql.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,SCHOOL_MST T2 ");
            sql.append("     WHERE ");
            sql.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
            if (null != param._startYear) {
                sql.append(" AND T2.YEAR >= '" + param._startYear + "' ");
            }
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            sql.append(" ) ");
            sql.append("     SELECT ");
            sql.append("         T1.YEAR, T1.STAFFCD, T1.FROM_DATE, T1.TO_DATE ");
            sql.append("     FROM PRINCIPAL_HIST T1 ");
            sql.append("     ORDER BY T1.YEAR, T1.ORDER ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                getMappedList(_yearPrincipalListMap, KnjDbUtils.getString(row, "YEAR")).add(row);
            }
        }

        public void setInkanMap(final DB2UDB db2, final Param param) {
            _inkanMap = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH TMP AS (");
            sql.append("   SELECT ");
            sql.append("     STAFFCD, ");
            sql.append("     VALUE(START_DATE, DATE, '1901-01-01') AS DATE, ");
            sql.append("     STAMP_NO ");
            sql.append("   FROM ATTEST_INKAN_DAT ");
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   FISCALYEAR(T1.DATE) AS YEAR, ");
            sql.append("   MAX(T1.STAMP_NO) AS STAMP_NO ");
            sql.append(" FROM TMP T1 ");
            sql.append(" INNER JOIN (SELECT ");
            sql.append("               L1.STAFFCD, ");
            sql.append("               FISCALYEAR(L1.DATE) AS YEAR, ");
            sql.append("               MAX(L1.DATE) AS DATE ");
            sql.append("             FROM TMP L1 ");
            sql.append("             GROUP BY ");
            sql.append("               L1.STAFFCD, ");
            sql.append("               FISCALYEAR(L1.DATE) ");
            sql.append("            ) T2 ON T2.STAFFCD = T1.STAFFCD ");
            sql.append("                AND T2.DATE = T1.DATE ");
            if (null != param._startYear) {
                sql.append(" AND FISCALYEAR(T1.DATE) >= '" + param._startYear + "' ");
            }
            sql.append(" GROUP BY ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   FISCALYEAR(T1.DATE) ");
            sql.append(" ORDER BY ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   MAX(T1.STAMP_NO) ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                getMappedMap(_inkanMap, KnjDbUtils.getString(row, "STAFFCD")).put(KnjDbUtils.getInt(row, "YEAR", null), KnjDbUtils.getString(row, "STAMP_NO"));
            }
        }

        private String getStampNo(final Param param, final String staffcd, final String year) {
            if (null == _inkanMap.get(staffcd) || !NumberUtils.isDigits(year)) {
                return null;
            }
            String stampNo = null;
            final Map<Integer, String> yearStampnoMap = getMappedMap(_inkanMap, staffcd);
            if (yearStampnoMap.size() == 1) {
                final Map.Entry<Integer, String> e = yearStampnoMap.entrySet().iterator().next();
                stampNo = e.getValue();
                return stampNo;
            }
            final Integer iYear = Integer.valueOf(year);
            for (final Map.Entry<Integer, String> e : yearStampnoMap.entrySet()) {
                final Integer inkanYear = (Integer) e.getKey();
                if (inkanYear.intValue() > iYear.intValue()) {
                    break;
                }
                stampNo = e.getValue();
            }
            return stampNo;
        }

        public void setStaffClassHistMap(final DB2UDB db2, final Param param, final String maxYear) {

            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.TR_DIV, ");
            stb.append("         T1.STAFFCD, ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.FROM_DATE, ");
            stb.append("         VALUE(MAX(T1.TO_DATE), '9999-12-31') AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR <= '" + maxYear + "' ");
            if (null != param._startYear) {
                stb.append(" AND T1.YEAR >= '" + param._startYear + "' ");
            }
            stb.append("     GROUP BY ");
            stb.append("         T1.TR_DIV, T1.STAFFCD, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append("     ORDER BY T1.TR_DIV, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE, T1.STAFFCD ");
            final String sql = stb.toString();

            final Map<String, TreeMap<String, List<Staff>>> rtn = new HashMap();
            for (final Map row : KnjDbUtils.query(db2, sql, null)) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");

                final String trDiv = KnjDbUtils.getString(row, "TR_DIV");
                final String staffcd = KnjDbUtils.getString(row, "STAFFCD");
                final String staffFromDate = KnjDbUtils.getString(row, "FROM_DATE");
                final String staffToDate = KnjDbUtils.getString(row, "TO_DATE");
                if (null == staffFromDate) {
                    continue;
                }

                final Staff staff = new Staff(year, StaffMst.get(_staffMstMap, staffcd), staffFromDate, staffToDate, getStampNo(param, staffcd, year));

                getMappedList(getMappedTreeMap(rtn, staffClassHistKey(year, grade, hrClass, trDiv)), staffFromDate).add(staff);
            }
            _staffClassHistMap = rtn;
        }

        private static String staffClassHistKey(final String year, final String grade, final String hrClass, final String trDiv) {
            return year + ":" + grade + ":" + hrClass + ":" + trDiv;
        }

        private List<Staff> getHistStaffList(final String year, final String grade, final String hrClass, final String trDiv) {
            final List<Staff> staffList = new ArrayList();
            final TreeMap<String, List<Staff>> grhrStaffMap = getMappedTreeMap(_staffClassHistMap, staffClassHistKey(year, grade, hrClass, trDiv));
            for (final List<Staff> fromDateStaffList : grhrStaffMap.values()) {
                if (fromDateStaffList.size() > 0) {
                    staffList.add(fromDateStaffList.get(0)); // 同一日付なら最小職員コードの職員
                }
            }
            return staffList;
        }

        public List getStudentStaffHistList(final Param param, final Student student, final PersonalInfo pInfo, final String trDiv, final String year) {
            final List<SchregRegdDat> regdInYear = getMappedList(SchregRegdDat.getYearRegdListMap(student._regdList), year);
            final List<Staff> studentStaffHistList = new ArrayList<Staff>();
            SchregRegdDat beforeRegd = null;
            List<String> differentGradeHrclasses = new ArrayList<String>();
            for (final SchregRegdDat regd : regdInYear) {
                if (null != beforeRegd && !(regd._grade + regd._hrClass).equals(beforeRegd._grade + beforeRegd._hrClass)) {
                    differentGradeHrclasses.add("TO:" + beforeRegd._semester + beforeRegd._grade + beforeRegd._hrClass);
                    differentGradeHrclasses.add("FROM:" + regd._semester + regd._grade + regd._hrClass);
                }
                beforeRegd = regd;
            }
            if (param._isOutputDebugStaff) {
                log.info(" " + year + " trDiv" + trDiv + " different grade hrclass = " + differentGradeHrclasses);
            }
            Staff beforeStaff = null;
            for (final SchregRegdDat regd : regdInYear) {
                final Semester semester = Semester.get(param, regd._year, regd._semester);
                String sdate = semester._sdate;
                String edate = semester._edate;
                boolean setFrom = false;
                boolean setTo = false;
                if (differentGradeHrclasses.contains("FROM:" + regd._semester + regd._grade + regd._hrClass)) {
                    setFrom = true;
                }
                if (differentGradeHrclasses.contains("TO:" + regd._semester + regd._grade + regd._hrClass)) {
                    setTo = true;
                }
                final String regdSemesterKey = Semester.get(param, regd._year, regd._semester).key();
                if (pInfo._entSemester.key().equals(regdSemesterKey)) {
                    sdate = pInfo._entDate;
                    setFrom = true;
                }
                boolean noMore = false;
                if (pInfo._grdSemester.key().equals(regdSemesterKey)) {
                    edate = pInfo._grdDate;
                    setTo = true;
                    noMore = true;
                }
                if (param._isOutputDebugStaff) {
                    log.info("  set staff semester " + semester.key() + ", setFrom = " + setFrom + ", setTo = " + setTo);
                }

                for (Staff staff : getHistStaffList(year, regd._grade, regd._hrClass, trDiv)) {
                    if (null != sdate && null != staff._dateTo && staff._dateTo.compareTo(sdate) < 0) {
                        if (param._isOutputDebugStaff) {
                            log.info("  -- skip staff " + staff._staffMst._staffcd + "(" + regd._grade + regd._hrClass + ", dateTo = " + staff._dateTo + ")");
                        }
                        continue;
                    }
                    if (null != edate && null != staff._dateFrom && edate.compareTo(staff._dateFrom) < 0) {
                        if (param._isOutputDebugStaff) {
                            log.info("  -- skip staff " + staff._staffMst._staffcd + "(" + regd._grade + regd._hrClass + ", dateFrom = " + staff._dateFrom + ")");
                        }
                        continue;
                    }
                    String dateFrom = staff._dateFrom;
                    if (null != beforeStaff && beforeStaff._staffMst == staff._staffMst) {
                        dateFrom = beforeStaff._dateFrom;
                    } else if (setFrom) {
                        dateFrom = Util.maxDate(sdate, dateFrom);
                    }
                    String dateTo = staff._dateTo;
                    if (setTo) {
                        dateTo = Util.minDate(edate, dateTo);
                        for (int i = studentStaffHistList.size() - 1; i >= 0; i--) {
                            final Staff befStaff = studentStaffHistList.get(i);
                            if (befStaff._staffMst == staff._staffMst && StringUtils.defaultString(befStaff._dateTo).equals(StringUtils.defaultString(staff._dateTo))) {
                                studentStaffHistList.set(i, new Staff(befStaff._year, befStaff._staffMst, befStaff._dateFrom, dateTo, befStaff._stampNo));
                            }
                        }
                    }
                    staff = new Staff(staff._year, staff._staffMst, dateFrom, dateTo, staff._stampNo);
                    if (param._isOutputDebugStaff) {
                        log.info("     add staff " + studentStaffHistList.size() + " " + semester.key() + "-" + regd._grade + "-" + regd._hrClass + " " + staff);
                    }
                    studentStaffHistList.add(staff);
                    beforeStaff = staff;
                }
                if (noMore) {
                    break;
                }
            }
            return studentStaffHistList;
        }
    }

    /**
     * 出欠データ
     */
    private static class Attend {

        final String _year;
        final int _g;
        final String _lesson;
        final String _suspendMourning;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _requirePresent;
        final String _present;
        final String _absent;
        final String _late;
        final String _early;

        public Attend(
                final String year,
                final int g,
                final String lesson,
                final String suspendMourning,
                final String suspend,
                final String mourning,
                final String abroad,
                final String requirePresent,
                final String present,
                final String absent,
                final String late,
                final String early) {
            _year = year;
            _g = g;
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
            _late = late;
            _early = early;
        }

        /**
         *  年次ごとの出欠データのリストを得る
         */
        public static List<Attend> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<Attend> attendList = new ArrayList<Attend>();
            if (null == param.koudo) {
                return attendList;
            }
            final String sql = getAttendSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int p = 0;
                ps = db2.prepareStatement(sql);
                ps.setString( ++p, schregno);    //学籍番号
                ps.setString( ++p, schregno);    //学籍番号
                ps.setString( ++p, schregno);    //学籍番号
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final int g = param.getGradeCd(year, rs.getString("ANNUAL"));
                    final String lesson = rs.getString("LESSON");
                    final String suspendMourning = rs.getString("SUSPEND_MOURNING");
                    final String suspend = rs.getString("SUSPEND");
                    final String mourning = rs.getString("MOURNING");
                    final String abroad = rs.getString("ABROAD");
                    final String requirePresent = rs.getString("REQUIREPRESENT");
                    final String present = rs.getString("PRESENT");
                    final String absent = rs.getString("ABSENT");
                    final String late = rs.getString("LATE");
                    final String early = rs.getString("EARLY");

                    final Attend attend = new Attend(year, g, lesson, suspendMourning, suspend, mourning, abroad, requirePresent, present, absent, late, early);
                    attendList.add(attend);
                }
            } catch (Exception ex) {
                log.error("printSvfAttendRecord error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return attendList;
        }

        /**
         *  priparedstatement作成  出欠の記録
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String getAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        SCHREG_ATTENDREC_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("   GROUP BY ");
            stb.append("        ANNUAL ");
            stb.append(" ), SEMES AS ( ");
            stb.append("    SELECT ");
            stb.append("        YEAR, SCHREGNO, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
            stb.append("    FROM ");
            stb.append("        ATTEND_SEMES_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("   GROUP BY ");
            stb.append("        YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR, ");
            stb.append("        T1.ANNUAL, ");
            stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append("              THEN VALUE(CLASSDAYS,0) ");
            stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            stb.append(             "END AS LESSON, ");
            stb.append(        "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
            stb.append(        "VALUE(SUSPEND,0) AS SUSPEND, ");
            stb.append(        "VALUE(MOURNING,0) AS MOURNING, ");
            stb.append(        "VALUE(ABROAD,0) AS ABROAD, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append(             "END AS REQUIREPRESENT, ");
            stb.append(        "VALUE(PRESENT,0) AS PRESENT, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append(             "END AS ABSENT, ");
            stb.append(        "VALUE(SEMES.LATE, 0) AS LATE, ");
            stb.append(        "VALUE(SEMES.EARLY, 0) AS EARLY ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append(        "INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.ANNUAL = T1.ANNUAL ");
            stb.append(        "LEFT JOIN SEMES ON SEMES.YEAR = T1.YEAR AND SEMES.SCHREGNO = T1.SCHREGNO ");
            stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append(        " AND S1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            stb.append("WHERE   T1.YEAR <= '" + param._year + "' ");
            stb.append(    "AND T1.SCHREGNO = ? ");
            if (null != param._startYear) {
                stb.append("    AND T1.YEAR >= '" + param._startYear + "' ");
            }
            return stb.toString();
        }
    }

    /**
     * 住所データ
     */
    private static class Address {
        final String _issuedate;
        final String _address1;
        final String _address2;
        final String _zipCd;
        final boolean _isPrintAddr2;

        private Address(final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
            _issuedate = issuedate;
            _address1 = addr1;
            _address2 = addr2;
            _zipCd = zip;
            _isPrintAddr2 = isPrintAddr2;
        }

        /**
         * 住所履歴を得る
         */
        public static List<Address> load(final List<Map<String, String>> rowList) {
            final List<Address> addressRecordList = new ArrayList<Address>();
            for (final Map row : rowList) {
                final String issuedate = KnjDbUtils.getString(row, "ISSUEDATE");
                final String address1 = KnjDbUtils.getString(row, "ADDR1");
                final String address2 = KnjDbUtils.getString(row, "ADDR2");
                final boolean isPrintAddr2 = "1".equals(KnjDbUtils.getString(row, "ADDR_FLG"));
                final String zipCd = KnjDbUtils.getString(row, "ZIPCD");

                final Address addressRecord = new Address(issuedate, address1, address2, zipCd, isPrintAddr2);
                addressRecordList.add(addressRecord);
            }
            return addressRecordList;
        }
        /**
         * 住所履歴を得る
         */
        public static List<Map<String, String>> getRowList(final DB2UDB db2, final Param param, final boolean isGuardian, final Student student, final String startDate, final String endDate) {
            final List<Map<String, String>> addressRecordList = new ArrayList<Map<String, String>>();
            if (null == param.seito) {
                return addressRecordList;
            }
            final String sql = isGuardian ? sqlAddress(param, true, startDate, endDate) : sqlAddress(param, false, startDate, endDate);
            return KnjDbUtils.query(db2, sql, new String[] { student._schregno, param._year, });
        }
        public static String sqlAddress(final Param param, final boolean isGuardianAddress, final String startDate, final String endDate) {

            StringBuffer stb = new StringBuffer();
            if (isGuardianAddress) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            }
            stb.append("WHERE  ");
            stb.append("       T1.SCHREGNO = ?  ");
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= ?  ");
            if (null != startDate && null != endDate) {
                stb.append("       AND (ISSUEDATE BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR VALUE(EXPIREDATE, '9999-12-31') BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR ISSUEDATE <= '" + startDate + "' AND '" + endDate + "' <= VALUE(EXPIREDATE, '9999-12-31') ");
                stb.append("          OR '" + startDate + "' <= ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') <= '" + endDate + "' ) ");
            } else if (null != startDate && null == endDate) {
                stb.append("       AND ('" + startDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR '" + startDate + "' <= ISSUEDATE) ");
            } else if (null == startDate && null != endDate) {
                stb.append("       AND ('" + endDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ISSUEDATE <= '" + endDate + "') ");
            }
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE DESC ");
            return stb.toString();
        }
        public String toString() {
            return "AddressRec(" + _issuedate + "," + _address1 + " " + _address2 + ")";
        }
    }

    private static class HtrainremarkPHdat {
        String _totalstudyact;
        String _totalstudyval;
        String _detail2HdatSeq001Remark1;

        public static HtrainremarkPHdat load(final DB2UDB db2, final Param param, final String schregno) {
            HtrainremarkPHdat remarkHdat = new HtrainremarkPHdat();
            if (null == param.koudo || null == param.gakushu) {
                return remarkHdat;
            }

            final String psKey = "PS_HRP_HD";
            if (null == param.getPs(psKey)) {

                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT ");
                stb.append("    TOTALSTUDYACT ");
                stb.append("   ,TOTALSTUDYVAL ");
                stb.append("FROM    HTRAINREMARK_P_HDAT T1 ");
                stb.append("WHERE   SCHREGNO = ? ");

                param.setPs(psKey, db2, stb.toString());
            }

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {schregno}));

            remarkHdat._totalstudyact = KnjDbUtils.getString(row, "TOTALSTUDYACT");
            remarkHdat._totalstudyval = KnjDbUtils.getString(row, "TOTALSTUDYVAL");

            if (param.isTokubetsuShien()) {
                final String psKey2 = "PS_HRHD_DET2";
                if (null == param.getPs(psKey2)) {

                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT ");
                    stb.append("    REMARK1 ");
                    stb.append("FROM    HTRAINREMARK_DETAIL2_HDAT T1 ");
                    stb.append("WHERE   SCHREGNO = ? ");
                    stb.append("    AND HTRAIN_SEQ = ? ");

                    param.setPs(psKey2, db2, stb.toString());
                }

                remarkHdat._detail2HdatSeq001Remark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {schregno, "001"}));
            }
            return remarkHdat;
        }
    }

    /**
     * 総合的な学習の時間の記録・外国語活動の記録
     */
    private static class HTrainremarkPdat {
        final String _year;
        int _gradeCdInt;
        String _totalstudyact;
        String _totalstudyval;
        String _specialActRemark;
        String _totalRemark;
        String _attendrecRemark;
        String _viewremark;
        String _foreignLangAct1;
        String _foreignLangAct2;
        String _foreignLangAct3;
        String _foreignLangAct4;
        String _detail2DatSeq001Remark1;
        String _detail2DatSeq002Remark1;
        public HTrainremarkPdat(final String year) {
            _year = year;
        }

        public static List<HTrainremarkPdat> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<HTrainremarkPdat> htrainRemarkDatList = new ArrayList<HTrainremarkPdat>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String remarkRecordSql = getRemarkRecordSql(param);
                ps = db2.prepareStatement(remarkRecordSql);
                int p = 0;
                ps.setString(++p, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HTrainremarkPdat remark = new HTrainremarkPdat(rs.getString("YEAR"));

                    remark._gradeCdInt = param.getGradeCd(rs.getString("YEAR"), rs.getString("ANNUAL"));
                    remark._totalstudyact = rs.getString("TOTALSTUDYACT");
                    remark._totalstudyval = rs.getString("TOTALSTUDYVAL");
                    remark._specialActRemark = rs.getString("SPECIALACTREMARK");
                    remark._totalRemark = rs.getString("TOTALREMARK");
                    remark._attendrecRemark = rs.getString("ATTENDREC_REMARK");
                    remark._viewremark = rs.getString("VIEWREMARK");
                    remark._foreignLangAct1 = rs.getString("FOREIGNLANGACT1");
                    remark._foreignLangAct2 = rs.getString("FOREIGNLANGACT2");
                    remark._foreignLangAct3 = rs.getString("FOREIGNLANGACT3");
                    remark._foreignLangAct4 = rs.getString("FOREIGNLANGACT4");

                    htrainRemarkDatList.add(remark);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (param.isTokubetsuShien()) {
                final String sqlDet2Hdat = getHtrainremarkDetail2HDatSql(param);
                try {
                    ps = db2.prepareStatement(sqlDet2Hdat);
                    ps.setString(1, param._year);
                    ps.setString(2, schregno);
                    ps.setString(3, "001");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        if (null == year) {
                            continue;
                        }
                        if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                            final HTrainremarkPdat htrainremarkDat = new HTrainremarkPdat(year);
                            htrainremarkDat._gradeCdInt = param.getGradeCd(year, rs.getString("ANNUAL"));
                            htrainRemarkDatList.add(htrainremarkDat);
                        }
                        final HTrainremarkPdat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                        htrainremarkDat._detail2DatSeq001Remark1 = rs.getString("REMARK1");
                    }
                } catch (final Exception e) {
                    log.error("Exception", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
                try {
                    ps = db2.prepareStatement(sqlDet2Hdat);
                    ps.setString(1, param._year);
                    ps.setString(2, schregno);
                    ps.setString(3, "002");
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        if (null == year) {
                            continue;
                        }
                        if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                            final HTrainremarkPdat htrainremarkDat = new HTrainremarkPdat(year);
                            htrainremarkDat._gradeCdInt = param.getGradeCd(year, rs.getString("ANNUAL"));
                            htrainRemarkDatList.add(htrainremarkDat);
                        }
                        final HTrainremarkPdat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                        htrainremarkDat._detail2DatSeq002Remark1 = rs.getString("REMARK1");
                    }
                } catch (final Exception e) {
                    log.error("Exception", e);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return htrainRemarkDatList;
        }

        private static HTrainremarkPdat getHTrainRemarkDat(final List<HTrainremarkPdat> list, final String year) {
            for (final HTrainremarkPdat d : list) {
                if (d._year.equals(year)) {
                    return d;
                }
            }
            return null;
        }

        /**
         *  priparedstatement作成  総合的な学習の時間の記録
         */
        private static String getRemarkRecordSql(final Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_P_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= '" + param._year + "' ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T1.ANNUAL ");
            stb.append(      " ,TOTALSTUDYACT ");
            stb.append(      " ,TOTALSTUDYVAL ");
            stb.append(      " ,SPECIALACTREMARK ");
            stb.append(      " ,TOTALREMARK ");
            stb.append(      " ,ATTENDREC_REMARK ");
            stb.append(      " ,VIEWREMARK ");
            stb.append(      " ,BEHAVEREC_REMARK ");
            stb.append(      " ,FOREIGNLANGACT1 ");
            stb.append(      " ,FOREIGNLANGACT2 ");
            stb.append(      " ,FOREIGNLANGACT3 ");
            stb.append(      " ,FOREIGNLANGACT4 ");
            stb.append("FROM    HTRAINREMARK_P_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.ANNUAL = T1.ANNUAL ");
            if (null != param._startYear) {
                stb.append(" AND T1.YEAR >= '" + param._startYear + "' ");
            }
            return stb.toString();
        }

        private static String getHtrainremarkDetail2HDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_P_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= ? ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T2.ANNUAL ");
            stb.append("       ,REMARK1 ");
            stb.append("FROM HTRAINREMARK_DETAIL2_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    T1.HTRAIN_SEQ = ? ");
            if (null != param._startYear) {
                stb.append(" AND T1.YEAR >= '" + param._startYear + "' ");
            }
            return stb.toString();
        }
    }

    /**
     * 行動の記録・特別活動の記録
     */
    private static class ActRecord {
        final String _year;
        final int _g;
        final String _record;
        final int _code;
        final String _div;
        public ActRecord(
                final String year,
                final int g,
                final String record,
                final int code,
                final String div) {
            _year = year;
            _g = g;
            _record = record;
            _code = code;
            _div = div;
        }

        /**
         *  SVF-FORM 印刷処理 明細
         *  行動の記録・特別活動の記録
         */
        public static List<ActRecord> load(final DB2UDB db2, final Param param, final String schregno) {
            final String psKey = "PS_ACT";
            if (null == param.getPs(psKey)) {
                final String sql = getActRecordSql(param);
                param.setPs(psKey, db2, sql);
            }
            final List<ActRecord> actList = new ArrayList();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno, schregno})) {

                final String year = getString("YEAR", row);
                final String record = getString("RECORD", row);
                final int g = param.getGradeCd(getString("YEAR", row), getString("ANNUAL", row));
                final int code = Integer.parseInt(getString("CODE", row));
                final String div = getString("DIV", row);

                final ActRecord act = new ActRecord(year, g, record, code, div);
                actList.add(act);
            }
            return actList;
        }

        /**
         *  priparedstatement作成  行動の記録・特別活動の記録
         */
        private static String getActRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        BEHAVIOR_DAT ");
            stb.append("    WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("    ,DIV ");
            stb.append("    ,CODE ");
            stb.append("    ,T1.ANNUAL ");
            stb.append("    ,RECORD ");
            stb.append("FROM    BEHAVIOR_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("WHERE   T1.YEAR <= '" + param._year + "' ");
            stb.append(    "AND T1.SCHREGNO = ? ");
            if (null != param._startYear) {
                stb.append(" AND T1.YEAR >= '" + param._startYear + "' ");
            }
            return stb.toString();
        }
    }

    private static class AfterGraduatedCourse {

        public static List<String> loadTextList(final DB2UDB db2, final Param param, final String schregno, final List<Gakuseki> regdRecordList) {
            final List<String> textList = new ArrayList<String>();
            if (null == param.seito || !param._hasAftGradCourseDat) {
                return textList;
            }
            ResultSet rs = null;
            try {
                final TreeSet<String> yearSet = Student.gakusekiYearSet(regdRecordList);
                if (yearSet.isEmpty()) {
                    return textList;
                }
                final String minYear = yearSet.first();
                final String maxYear = yearSet.last();
                final String psKey = "PS_AFT_GRAD";
                if (null == param.getPs(psKey)) {
                    final String sql = getSql(param);
                    param.setPs(psKey, db2, sql);
                }
                PreparedStatement ps = param.getPs(psKey);
                ps.setString(1, schregno);
                ps.setString(2, minYear);
                ps.setString(3, maxYear);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("0".equals(rs.getString("SENKOU_KIND"))) { // 進学
                        if (null == rs.getString("STAT_CD") || null != rs.getString("E017NAME1")) {
                            final String[] token = KNJ_EditEdit.get_token(rs.getString("THINKEXAM"), 50, 10);
                            if (null != token) {
                                textList.addAll(Arrays.asList(token));
                            }
                         } else {
                             if (null != rs.getString("FINSCHOOL_NAME")) {
                                 textList.add(StringUtils.defaultString(rs.getString("FINSCHOOL_NAME")));
                             } else {
                                 textList.add(StringUtils.defaultString(rs.getString("SCHOOL_NAME")));
                                 textList.add(StringUtils.defaultString(rs.getString("FACULTYNAME")) + StringUtils.defaultString(rs.getString("DEPARTMENTNAME")));
                                 textList.add(StringUtils.defaultString(rs.getString("CAMPUSFACULTYADDR1"), rs.getString("CAMPUSADDR1")));
                             }
                         }
                     } else if ("1".equals(rs.getString("SENKOU_KIND"))) { // 就職
                        if (null == rs.getString("STAT_CD") || null != rs.getString("E018NAME1")) {
                            final String[] token = KNJ_EditEdit.get_token(rs.getString("JOB_THINK"), 50, 10);
                            if (null != token) {
                                textList.addAll(Arrays.asList(token));
                            }
                         } else {
                            textList.add(StringUtils.defaultString(rs.getString("COMPANY_NAME")));
                            textList.add(StringUtils.defaultString(rs.getString("COMPANYADDR1")));
                            textList.add(StringUtils.defaultString(rs.getString("COMPANYADDR2")));
                         }
                     }

                }
            } catch (final Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return textList;
        }

        protected static String getSql(final Param param) {
            // 進路用・就職用両方の最新の年度を取得
            final StringBuffer stb = new StringBuffer();
            stb.append("with TA as( select ");
            stb.append("         SCHREGNO, ");
            stb.append("         '0' as SCH_SENKOU_KIND, ");
            stb.append("         MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, ");
            stb.append("         '1' as COMP_SENKOU_KIND, ");
            stb.append("         MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR ");
            stb.append(" from ");
            stb.append("         AFT_GRAD_COURSE_DAT ");
            stb.append(" where ");
            stb.append("         SCHREGNO = ?  and PLANSTAT = '1' and YEAR <= '" + param._year + "' ");
            stb.append("         AND YEAR BETWEEN ? AND ? ");
            stb.append(" group by ");
            stb.append("         SCHREGNO ");
            // 進路用・就職用どちらか(進路が優先)の最新の受験先種別コードを取得);
            stb.append("), TA2 as( select ");
            stb.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND, ");
            stb.append("     MAX(T1.SEQ) AS SEQ ");
            stb.append(" from ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append(" inner join TA on ");
            stb.append("     T1.SCHREGNO = TA.SCHREGNO ");
            stb.append("     and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) ");
            stb.append("     and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) ");
            stb.append(" where ");
            stb.append("     T1.PLANSTAT = '1'");
            stb.append(" group by ");
            stb.append("     (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND ");
            stb.append(") ");
            // 最新の年度と受験先種別コードの感想を取得);
            stb.append("select  ");
            stb.append("      T1.SENKOU_KIND ");
            stb.append("     ,T1.STAT_CD ");
            stb.append("     ,T1.THINKEXAM ");
            stb.append("     ,T1.JOB_THINK ");
            stb.append("     ,L1.NAME1 as E017NAME1 ");
            stb.append("     ,L2.NAME1 as E018NAME1 ");
            stb.append("     ,L3.SCHOOL_NAME ");
            stb.append("     ,L5.FACULTYNAME ");
            stb.append("     ,L6.DEPARTMENTNAME ");
            stb.append("     ,L7.ADDR1 AS CAMPUSADDR1 ");
            stb.append("     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ");
            stb.append("     ,L4.COMPANY_NAME ");
            stb.append("     ,L4.ADDR1 AS COMPANYADDR1 ");
            stb.append("     ,L4.ADDR2 AS COMPANYADDR2 ");
            stb.append("     ,TFINSC.FINSCHOOL_NAME ");
            stb.append("from ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append("inner join TA2 on ");
            stb.append("     T1.YEAR = TA2.YEAR ");
            stb.append("     and T1.SCHREGNO = TA2.SCHREGNO ");
            stb.append("     and T1.SENKOU_KIND = TA2.SENKOU_KIND ");
            stb.append("     and T1.SEQ = TA2.SEQ ");
            stb.append("left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD ");
            stb.append("left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD ");
            stb.append("left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD ");
            stb.append("left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD ");
            stb.append("     and L5.FACULTYCD = T1.FACULTYCD ");
            stb.append("left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD ");
            stb.append("     and L6.FACULTYCD = T1.FACULTYCD ");
            stb.append("     and L6.DEPARTMENTCD = T1.DEPARTMENTCD ");
            stb.append("left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD ");
            stb.append("     and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD ");
            stb.append("left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD ");
            stb.append("     and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD ");
            stb.append("left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD ");
            stb.append("left join FINSCHOOL_MST TFINSC on TFINSC.FINSCHOOLCD = T1.STAT_CD ");
            stb.append("where ");
            stb.append("     T1.PLANSTAT = '1' ");
            if (null != param._startYear) {
                stb.append(" AND T1.YEAR >= '" + param._startYear + "' ");
            }
            stb.append("order by ");
            stb.append("     T1.YEAR, T1.SCHREGNO ");
            return stb.toString();
        }
    }

    private static class ClassMst {
        private static final ClassMst Null = new ClassMst("XX", "XX", null, "9", "9", new Integer(99999999));
        final String _classcd;
        final String _schoolKind;
        final String _classname;
        final String _electdiv;
        final String _specialDiv;
        final Integer _showorder;

        private ClassMst(final String classcd, final String schoolKind, final String classname, final String electdiv, final String specialdiv, final Integer showorder) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _classname = classname;
            _electdiv = electdiv;
            _specialDiv = specialdiv;
            _showorder = showorder;
        }

        public ClassMst setClassname(final String classname) {
            return new ClassMst(_classcd, _schoolKind, classname, _electdiv, _specialDiv, _showorder);
        }

        public String getKey(final Param param) {
            return key(param, _classcd, _schoolKind);
        }

        public String toString() {
            return "ClassMst(" + getKey(null) + ", " + _classname + ")";
        }

        public static String key(final Param param, final String classcd, final String schoolKind) {
            if (null != param && !"1".equals(param._useCurriculumcd)) {
                return classcd;
            }
            return classcd + "-" + schoolKind;
        }

        public static ClassMst get(final Map<String, ClassMst> classMstMap, final String key) {
            final ClassMst classMst = classMstMap.get(key);
            if (null == classMst) {
                log.warn(" null class mst:" + key);
                return Null;
            }
            return classMst;
        }

        private static int compareOrder(final Param param, final ClassMst classMst1, final ClassMst classMst2) {
            if (Null == classMst1 && Null == classMst2) {
                return 0;
            } else if (Null == classMst1) {
                return 1;
            } else if (Null == classMst2) {
                return -1;
            }
            int rtn;
            rtn = classMst1._showorder.compareTo(classMst2._showorder);
            if (0 != rtn) {
                return rtn;
            }
            rtn = classMst1._classcd.compareTo(classMst2._classcd);
            if (0 != rtn) {
                return rtn;
            }
            if (null != classMst1._schoolKind && null != classMst2._schoolKind) {
                rtn = classMst1._schoolKind.compareTo(classMst2._schoolKind);
            }
            if (0 != rtn) {
                return rtn;
            }
            if (classMst1._electdiv != classMst2._electdiv) {
                rtn = Integer.parseInt(classMst1._electdiv) - Integer.parseInt(classMst2._electdiv);
            }
            return rtn;
        }

        private static boolean isSameKey(final Param param, final ClassMst cm1, final ClassMst cm2) {
            if (cm1._classcd.equals(cm2._classcd) && (cm1._schoolKind == null && cm2._schoolKind == null || cm1._schoolKind.equals(cm2._schoolKind))) {
                return true;
            }
            return false;
        }
    }

    /**
     * 観点の教科
     */
    private static class ClassView {

        private static class ClassViewComparator implements Comparator<ClassView> {
            final Param _param;
            ClassViewComparator(final Param param) {
                _param = param;
            }
            public int compare(final ClassView cv1, final ClassView cv2) {
                int cmp;
                cmp = ClassMst.compareOrder(_param, cv1._classMst, cv2._classMst);
                if (cmp != 0) {
                    return cmp;
                }
                cmp = cv1._subclasscd.compareTo(cv2._subclasscd);
                return cmp;
            }
        }

        final ClassMst _classMst;
        final String _subclasscd; // 科目コード
        final String _subclassname;  //科目名称
        final List<String> _viewCdList = new ArrayList<String>();
        final Map<String, View> _views;
        boolean _isNameMstA046;

        public ClassView(
                final ClassMst classMst,
                final String subclasscd,
                final String subclassname
        ) {
            _classMst = classMst;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _views = new TreeMap<String, View>();
        }

        public List<View> getPrintViewList(final List<String> years) {
            final List<View> viewList = new ArrayList<View>();
            for (final String viewcd : _viewCdList) {
                final View view = _views.get(viewcd);
                if (null != years) {
                    boolean containsYear = false;
                    for (final String year : view._yearStatusMap.keySet()) {
                        if (years.contains(year)) {
                            containsYear = true;
                            break;
                        }
                    }
                    if (containsYear == false) {
                        continue;
                    }
                }
                viewList.add(view);
            }
            return viewList;
        }

        // 教科名のセット
        private String setPrintClassname(final String classname, final int viewCount) {
            if (classname == null) {
                return "";
            }
            if (viewCount == 0) {
                return classname;
            }
            final int newviewCount = (classname.length() <= viewCount) ? viewCount + 1 : viewCount;  // 教科間の観点行に１行ブランクを挿入
            final String newclassname;

            if (classname.length() < newviewCount) {
                final int i = (newviewCount - classname.length()) / 2;
                final String space = StringUtils.repeat(" ", i); // 教科名のセンタリングのため、空白を挿入
                newclassname = space + classname;
            } else {
                newclassname = classname;
            }
            return newclassname;
        }

        public String toString() {
            return "ClassView(" + _classMst + ":" + _subclasscd + (null == _classMst._electdiv ? "" : ": e = " + _classMst._electdiv) + ")";
        }

        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<ClassView> load(final DB2UDB db2, final Param param, final String schregno, final SchregEntGrdHistDat entGrdHistDat) {
            final List<ClassView> classViewList = new ArrayList<ClassView>();
            if (null == param.gakushu || param.isTokubetsuShien() && Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(param._chiteki)) {
                return classViewList;
            }
            final String psKey;
            final String sql;
            final Object[] psParam;
            if (param._useJviewnameSubMstCurriculumcd && null != entGrdHistDat._curriculumYearCurriculumcd) {
                psKey = "PS_CLASS_VIEW_SET_CURRICULUM_YEAR";
                sql = getViewRecordSql(param, true);
                psParam = new Object[] {schregno, schregno, schregno, entGrdHistDat._curriculumYearCurriculumcd};
            } else {
                psKey = "PS_CLASS_VIEW";
                sql = getViewRecordSql(param, false);
                psParam = new Object[] {schregno, schregno, schregno};
            }
            if (null == param.getPs(psKey)) {
                if (param._isOutputDebugQuery) {
                    log.info(" class view sql = "+ sql);
                }
                param.setPs(psKey, db2, sql);
            }

            final Map<String, ClassView> map = new HashMap<String ,ClassView>();

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), psParam)) {

                //教科コードの変わり目
                final String classcd = getString("CLASSCD", row);
                final String subclasscd = getString("SUBCLASSCD", row);
                final String subclassname = getString("SUBCLASSNAME", row);
                final String viewcd = getString("VIEWCD", row);
                final String viewname = getString("VIEWNAME", row);
                final String status = getString("STATUS", row);
                final String year = getString("YEAR", row);
                final ClassMst classMst = ClassMst.get(param._classMstMap, classcd);

                final String key = classMst.getKey(param) + subclasscd;
                if (!map.containsKey(key)) {
                    final ClassView classView = new ClassView(classMst, subclasscd, subclassname);
                    map.put(key, classView);
                    classViewList.add(classView);
                }

                final ClassView classView = map.get(key);
                if (!classView._viewCdList.contains(viewcd)) {
                    classView._viewCdList.add(viewcd);
                }
                if (!classView._views.containsKey(viewcd)) {
                    classView._views.put(viewcd, new View(subclasscd, viewcd, viewname));
                }
                classView._views.get(viewcd)._yearStatusMap.put(year, status);
            }
            return classViewList;
        }

        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param, final boolean useJviewnameSubMstCurriculumcd) {

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //観点の表
            stb.append("VIEW_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      ,CLASSCD ");
                stb.append("      ,SCHOOL_KIND ");
                stb.append("      ,CURRICULUM_CD ");
            }
            stb.append("     ,VIEWCD ");
            stb.append("     ,YEAR ");
            stb.append("     ,STATUS ");
            stb.append("  FROM ");
            stb.append("     JVIEWSTAT_SUB_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("    AND T1.YEAR <= '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(") ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT  YEAR ");
            stb.append(         ",GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT ");
            stb.append("    JVM.YEAR ");
            stb.append("   ,CM.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   ,CM.CLASSCD || '-' || CM.SCHOOL_KIND AS CLASSCD ");
                stb.append("   ,JVM.CLASSCD || '-' || JVM.SCHOOL_KIND || '-' || JVM.CURRICULUM_CD || '-' || ");
                if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                    stb.append("          VALUE(SBM.SUBCLASSCD2, JVM.SUBCLASSCD) ");
                } else {
                    stb.append("          JVM.SUBCLASSCD ");
                }
                stb.append("          AS SUBCLASSCD ");
            } else {
                stb.append("   ,CM.CLASSCD");
                if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                    stb.append("          , VALUE(SBM.SUBCLASSCD2, JVM.SUBCLASSCD) AS SUBCLASSCD ");
                } else {
                    stb.append("          , JVM.SUBCLASSCD AS SUBCLASSCD ");
                }
            }
            stb.append("   ,VALUE(CM.CLASSORDERNAME1, CM.CLASSNAME) AS CLASSNAME ");
            stb.append("   ,VALUE(");
            if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                stb.append("   SBM2.SUBCLASSORDERNAME1, SBM2.SUBCLASSNAME, ");
            }
            stb.append("       SBM.SUBCLASSORDERNAME1, SBM.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("   ,VALUE(CM.SHOWORDER, -1) AS SHOWORDERCLASS ");
            stb.append("   ,JVM.VIEWCD ");
            stb.append("   ,JVM.VIEWNAME ");
            stb.append("   ,MIN(T1.STATUS) AS STATUS ");
            stb.append("FROM  ( SELECT ");
            stb.append("            W2.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , W1.CLASSCD ");
                stb.append("          , W1.SCHOOL_KIND ");
                stb.append("          , W1.CURRICULUM_CD ");
            }
            stb.append("          , W1.SUBCLASSCD ");
            stb.append("          , W1.VIEWCD ");
            stb.append("          , VIEWNAME ");
            stb.append("          , VALUE(W1.SHOWORDER, -1) AS SHOWORDERVIEW ");
            stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
            stb.append("                INNER JOIN JVIEWNAME_SUB_YDAT W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND W3.CLASSCD = W1.CLASSCD ");
                stb.append("          AND W3.SCHOOL_KIND = W1.SCHOOL_KIND ");
                stb.append("          AND W3.CURRICULUM_CD = W1.CURRICULUM_CD ");
            }
            stb.append("          AND W3.VIEWCD = W1.VIEWCD ");
            stb.append("               INNER JOIN SCHREG_DATA W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        WHERE W1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (useJviewnameSubMstCurriculumcd) {
                stb.append("        AND W1.CURRICULUM_CD = ? ");
            }
            if (null != param._startYear) {
                stb.append(" AND W3.YEAR >= '" + param._startYear + "' ");
            }
            stb.append("      ) JVM ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("INNER JOIN CLASS_MST CM ON CM.CLASSCD = JVM.CLASSCD ");
                stb.append("  AND CM.SCHOOL_KIND = JVM.SCHOOL_KIND ");
            } else {
                stb.append("INNER JOIN CLASS_MST CM ON CM.CLASSCD = SUBSTR(JVM.SUBCLASSCD,1,2)  ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST SBM ON SBM.SUBCLASSCD = JVM.SUBCLASSCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND SBM.CLASSCD = JVM.CLASSCD ");
                stb.append("  AND SBM.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("  AND SBM.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST SBM2 ON SBM2.SUBCLASSCD = SBM.SUBCLASSCD2  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND SBM2.CLASSCD = SBM.CLASSCD ");
                stb.append("  AND SBM2.SCHOOL_KIND = SBM.SCHOOL_KIND ");
                stb.append("  AND SBM2.CURRICULUM_CD = SBM.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = JVM.YEAR ");
            stb.append("    AND T1.VIEWCD = JVM.VIEWCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND T1.CLASSCD = JVM.CLASSCD ");
                stb.append("    AND T1.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                if (!param._isSundaikoufu) {
                    stb.append("    AND T1.CURRICULUM_CD = JVM.CURRICULUM_CD ");
                }
            }
            stb.append("    AND T1.SUBCLASSCD = JVM.SUBCLASSCD  ");
            stb.append("GROUP BY ");
            stb.append("    JVM.YEAR ");
            stb.append("   ,CM.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   ,CM.CLASSCD || '-' || CM.SCHOOL_KIND ");
                stb.append("   ,JVM.CLASSCD || '-' || JVM.SCHOOL_KIND || '-' || JVM.CURRICULUM_CD || '-' || ");
                if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                    stb.append("          VALUE(SBM.SUBCLASSCD2, JVM.SUBCLASSCD) ");
                } else {
                    stb.append("          JVM.SUBCLASSCD ");
                }
            } else {
                stb.append("   ,CM.CLASSCD");
                if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                    stb.append("          , VALUE(SBM.SUBCLASSCD2, JVM.SUBCLASSCD) ");
                } else {
                    stb.append("          , JVM.SUBCLASSCD ");
                }
            }
            stb.append("   ,VALUE(CM.CLASSORDERNAME1, CM.CLASSNAME)  ");
            stb.append("   ,VALUE(");
            if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                stb.append("   SBM2.SUBCLASSORDERNAME1, SBM2.SUBCLASSNAME, ");
            }
            stb.append("       SBM.SUBCLASSORDERNAME1, SBM.SUBCLASSNAME) ");
            stb.append("   ,VALUE(CM.SHOWORDER, -1) ");
            stb.append("   ,JVM.VIEWCD ");
            stb.append("   ,JVM.VIEWNAME, ");
            stb.append("    VALUE(JVM.SHOWORDERVIEW, -1) ");
            stb.append("ORDER BY ");
            stb.append("    VALUE(CM.SHOWORDER, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   CM.CLASSCD || '-' || CM.SCHOOL_KIND, ");
            } else {
                stb.append("   CM.CLASSCD, ");
            }
            stb.append("    VALUE(CM.ELECTDIV, '0'), ");
            stb.append("    VALUE(JVM.SHOWORDERVIEW, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   JVM.CLASSCD || '-' || JVM.SCHOOL_KIND || '-' || JVM.CURRICULUM_CD || '-' || ");
                if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                    stb.append("          VALUE(SBM.SUBCLASSCD2, JVM.SUBCLASSCD), ");
                } else {
                    stb.append("          JVM.SUBCLASSCD, ");
                }
            } else {
                if ("1".equals(param._knja133pUseViewSubclassMstSubclasscd2)) {
                    stb.append("          VALUE(SBM.SUBCLASSCD2, JVM.SUBCLASSCD), ");
                } else {
                    stb.append("          JVM.SUBCLASSCD, ");
                }
            }
            stb.append("    JVM.VIEWCD, ");
            stb.append("    JVM.YEAR ");
            return stb.toString();
        }
    }

    /**
     * 観点データ
     */
    private static class View {
        final String _subclasscd;  //科目コード
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final Map<String, String> _yearStatusMap = new HashMap<String, String>(); // 観点マスタ年度データにあれば年度キーは存在する

        public View(
                final String subclasscd,
                final String viewcd,
                final String viewname
        ) {
            _subclasscd = subclasscd;
            _viewcd = viewcd;
            _viewname = viewname;
        }

        public String toString() {
            return "View(" + _subclasscd + "-" + _viewcd + ":" + _yearStatusMap + ")";
        }
    }

    /**
     * 評定データ
     */
    private static class ValueRecord {
        final String _year;
        final int _g;
        final String _classCd;
        final String _subclassCd;
        final String _electDiv;
        final String _className;
        final String _value; //評定
        public ValueRecord(
                final String year,
                final int g,
                final String classCd,
                final String subclassCd,
                final String electDiv,
                final String className,
                final String value) {
            _year = year;
            _g = g;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _electDiv = electDiv;
            _className = className;
            _value = value;
        }

        public String toString() {
            return "ValueRecord(" + _classCd + ":" + _subclassCd + ":" + _className + ")";
        }

        public static List<ValueRecord> load(final DB2UDB db2, final Param param, final String schregno) {
            final List<ValueRecord> valueRecordList = new ArrayList<ValueRecord>();
            if (null == param.gakushu || param.isTokubetsuShien() && Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(param._chiteki)) {
                return valueRecordList;
            }
            final String psKey = "PS_VALUE_REC";
            if (null == param.getPs(psKey)) {
                final String sql = getValueRecordSql(param);
                if (param._isOutputDebugQuery) {
                    log.info(" value record sql = " + sql);
                }
                param.setPs(psKey, db2, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno, schregno, schregno})) {

                //教科コードの変わり目
                final String year = getString("YEAR", row);
                final int g = param.getGradeCd(year, getString("GRADE", row)); // 学年
                final String electDiv = getString("ELECTDIV", row);
                final String classCd = getString("CLASSCD", row);
                final String subclassCd = getString("SUBCLASSCD", row);
                final String className = getString("CLASSNAME", row);
                //評定出力
                final String value = getString("VALUE", row);

                final ValueRecord valueRecord = new ValueRecord(year, g, classCd, subclassCd, electDiv, className, value);
                valueRecordList.add(valueRecord);
            }

            return valueRecordList;
        }

        /**
         *  priparedstatement作成  成績データ（評定）
         */
        private static String getValueRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //評定の表
            stb.append(" VALUE_DATA AS( ");
            stb.append("   SELECT ");
            stb.append("        ANNUAL ");
            stb.append("       ,CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD ");
            stb.append("       ,YEAR ");
            stb.append("       ,VALUATION AS VALUE ");
            stb.append("   FROM ");
            stb.append("       SCHREG_STUDYREC_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.SCHREGNO = ? ");
            stb.append("       AND T1.YEAR <= '" + param._year + "' ");
            stb.append(" ) ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = ? ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE) ");
            stb.append("  GROUP BY ");
            stb.append("      YEAR ");
            stb.append("      ,ANNUAL ");
            stb.append("      ,GRADE ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT ");
            stb.append("     T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND AS CLASSCD ");
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,T5.SUBCLASSCD ");
            }
            stb.append("    ,MAX(VALUE(T3.CLASSORDERNAME1,T3.CLASSNAME)) AS CLASSNAME ");
            stb.append("    ,MAX(VALUE(T3.SHOWORDER, -1)) AS SHOWORDERCLASS ");
            stb.append("    ,MAX(T5.VALUE) AS VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("GROUP BY ");
            stb.append("    T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,T3.ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,T5.SUBCLASSCD ");
            }
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,T5.SUBCLASSCD ");
            }
            stb.append("    ,T3.ELECTDIV ");
            stb.append("    ,T2.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 様式
     */
    private static abstract class KNJA133P_0 {

        private final Param _param;
        private String _form;
        private Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap();
        private Map<String, SvfForm> _svfFormMap = new HashMap();
        private Set logged = new TreeSet();
        final Vrw32alp _svf;
        private Map _fixFieldInfo = new HashMap();

        boolean _notOutput = false;

        KNJA133P_0(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        protected Param param() {
            return _param;
        }

        protected String modifyForm0(final String form, final Student student, final PersonalInfo pInfo, final Map<String, String> flgMap) {

            String formCreateFlg = Util.mkString(flgMap, "|").toString();
            if (param()._isOutputDebug) {
                log.info(" form config Flg = " + formCreateFlg);
            }
            if (StringUtils.isEmpty(formCreateFlg)) {
                return form;
            }
            formCreateFlg = form + "::" + formCreateFlg;
            if (null != param()._createFormFileMap.get(formCreateFlg)) {
                return param()._createFormFileMap.get(formCreateFlg).getName();
            }
            try {
                final SvfForm svfForm = new SvfForm(new File(_svf.getPath(form)));
                if (svfForm.readFile()) {

                    modifySvfForm(pInfo, svfForm, flgMap);

                    final File newFormFile = svfForm.writeTempFile();
                    param()._createFormFileMap.put(formCreateFlg, newFormFile);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            File newFormFile = param()._createFormFileMap.get(formCreateFlg);
            if (null != newFormFile) {
                return newFormFile.getName();
            }
            return form;
        }


        // 使用する際はoverrideしてください
        /**
         *
         * @param personalInfo
         * @param svfForm
         * @param printGakuseki
         * @param flgMap
         * @return 修正フラグ
         */
        protected boolean modifySvfForm(final PersonalInfo personalInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            log.warn("not implemented.");
            return false;
        }

        protected void printSvfRepeat(final String field, final List<String> list) {
            if (null != list) {
                for (int i = 0 ; i < list.size(); i++) {
                    vrsOutn(field, i + 1, list.get(i));
                    if (_param._isOutputDebugField) {
                        log.info("svf.VrsOutn(" + field + ", " + (i + 1) + ", " + list.get(i) + ")");
                    }
                }
            }
        }

        protected void printSvfRenban(final String fieldname, final String data, final KNJPropertiesShokenSize size, final boolean preferFieldThanProp) {
            if (param()._useSvfFieldAreaInfo) {
                printSvfRenbanUseFieldArea(fieldname, data, size.getKeta());
            } else {
                if (preferFieldThanProp && null != getSvfField(fieldname)) {
                    final SvfField field = getSvfField(fieldname);
                    printSvfRepeat(fieldname, getTokenList(data, field._fieldLength, field._fieldRepeatCount, param()));
                } else {
                    printSvfRepeat(fieldname, getTokenList(data, size.getKeta(), size._gyo, param()));
                }
            }
        }

        protected void printSvfRenban(final String field, final String data, final KNJPropertiesShokenSize size) {
            printSvfRenban(field, data, size, false);
        }

        protected void printSvfRenbanIdx(final String field, final int fieldIdx, final String data, final KNJPropertiesShokenSize size) {
            if (param()._useSvfFieldAreaInfo) {
                printSvfRenbanUseFieldArea(field + String.valueOf(fieldIdx), data, size.getKeta());
                return;
            }
            printSvfRepeat2(field, fieldIdx, data, size);
        }

        protected void printSvfRepeat2(final String field, final int fieldIdx, final String data, final KNJPropertiesShokenSize size) {
            if (param()._isOutputDebugField) {
                log.info(" print \"" + field + "\" => " + size);
            }
            final List<String> list = getTokenList(data, size.getKeta(), param());
            String fieldname = field + String.valueOf(fieldIdx);
            final String fieldname2 = field + String.valueOf(fieldIdx) + "_2";
            final int repeatCount = getFieldRepeatCount(fieldname, size._gyo);
            if (repeatCount < list.size()) {
                log.info(" repeatCount = " + repeatCount + ", list size = " + list.size() + " / hasField fieldname2? = " + (null != getSvfField(fieldname2)) + "(" + getFieldRepeatCount(fieldname2, size._gyo) + ")");
                if (null != getSvfField(fieldname2) && repeatCount < getFieldRepeatCount(fieldname2, size._gyo)) {
                    fieldname = fieldname2;
                }
            }
            printSvfRepeat(fieldname, list);
        }

        protected void printSvfRenbanUseFieldArea(final String fieldname, final String data, final int defKeta) {

            final int repeatCount = getFieldRepeatCount(fieldname, 0);
            final boolean isRepeat = repeatCount > 0;

            final SvfFieldAreaInfo areaInfo = new SvfFieldAreaInfo();
            areaInfo._param._isOutputDebug = param()._isOutputDebugField;
            areaInfo._param._setKinsoku = true;
            areaInfo._param._isOutputDebugKinsoku = param()._isOutputDebugKinsoku;

            final Map modifyFieldInfoMap = areaInfo.getModifyFieldInfoMap(_formFieldInfoMap, _form, fieldname, repeatCount, defKeta, data);

            if (param()._isOutputDebugField) {
                log.info(" !!! modify field " + fieldname + " = " + listString(modifyFieldInfoMap.entrySet(), 0));
            }

            if (isRepeat) {
                int ketai = getInt("FIELD_KETA", modifyFieldInfoMap);
                final int lines = getInt("FIELD_LINE", modifyFieldInfoMap);

                final List attrMapRepeatList = getMappedList(modifyFieldInfoMap, "REPEAT");

                for (int ri = 0; ri < attrMapRepeatList.size(); ri++) {
                    final Map attrMap = (Map) attrMapRepeatList.get(ri);
                    _svf.VrAttributen(fieldname, ri + 1, getString("FIELD_ATTR", attrMap));
                }
                if (defKeta < ketai && getTokenList(data, defKeta, param()).size() <= lines) {
                    // 余裕があるのでデフォルトの桁で表示
                    ketai = defKeta;
                }
                printSvfRepeat(fieldname, getTokenList(data, ketai, lines, param()));
            } else {
                // 未実装
            }
        }

        private static String btos(final byte[] b) {
            final StringBuffer stb = new StringBuffer("[");
            final String[] ns = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
            String spc = "";
            for (int i = 0; i < b.length; i++) {
                final int n = b[i] + (b[i] < 0 ? 256 : 0);
                stb.append(spc).append(ns[n / 16]).append(ns[n % 16]);
                spc = " ";
            }
            return stb.append("]").toString();
        }

        protected SvfField getSvfField(final String fieldname0) {
            if (StringUtils.isBlank(fieldname0)) {
                return null;
            }
            String fieldname = fieldname0;
            if (_fixFieldInfo.containsKey(_form + "." + fieldname0)) {
                fieldname = (String) _fixFieldInfo.get(_form + "." + fieldname0);
            } else {
                if (null != getMappedMap(_formFieldInfoMap, _form).get(fieldname0)) {
                    fieldname = fieldname0;
                } else {
                    for (final String f : getMappedMap(_formFieldInfoMap, _form).keySet()) {
                        if (f.equalsIgnoreCase(fieldname0)) {
                            fieldname = f;
                            _fixFieldInfo.put(_form + "." + fieldname0, _form + "." + fieldname);
                            log.warn(" fix fieldname : " + fieldname0 + " -> " + fieldname + " ( " + _form + " )");
                            break;
                        }
                    }
                }
            }
            return getMappedMap(_formFieldInfoMap, _form).get(fieldname);
        }

        protected KNJPropertiesShokenSize shokenSizeFromField(final String fieldname, final int defMojisu, final int defGyo) {
            KNJPropertiesShokenSize size = new KNJPropertiesShokenSize(defMojisu, defGyo);
            SvfField svfField = getSvfField(fieldname);
            if (null == svfField) {
                if (param()._isOutputDebug) {
                    log.warn("no such field : " + fieldname);
                }
            } else {
                size._gyo = svfField._fieldRepeatCount;
                size.setKeta(svfField._fieldLength);
                if (param()._isOutputDebug) {
                    log.info("set shoken size from field : " + size.getKeta() + ", " + size._gyo + " (" + fieldname + ")");
                }
            }
            return size;
        }

        protected KNJSvfFieldModify getFieldInfo(final String fieldName, final String fieldNameY1, final String fieldNameY2, final int defCharSize, final int minnum) {
            final KNJSvfFieldModify i = new KNJSvfFieldModify();
            try {
                final SvfField m = getSvfField(fieldName);
                i._field = fieldName;
                i._x1 = m.x();
                i._x2 = i._x1 + KNJSvfFieldModify.fieldWidth(m.size(), m._fieldLength);
                i._height = defCharSize;
                i._ystart = m.y();
                if (null != fieldNameY1) {
                    i._field1 = fieldNameY1;
                    i._field1Status = getSvfField(fieldNameY1);
                    if (null != i._field1Status) {
                        i._ystart1 = i._field1Status.y();
                    }
                }
                if (null != fieldNameY2) {
                    i._field2 = fieldNameY2;
                    i._field2Status = getSvfField(fieldNameY2);
                    if (null != i._field2Status) {
                        i._ystart2 = i._field2Status.y();
                    }
                }
                i._minnum = minnum;
                i._maxnum = m._fieldLength;
                if (param()._isOutputDebugField) {
                    final String s = fieldName + ":: x1 = " + i._x1 + ", x2 = " + i._x2 + ",  height = " + i._height + ", ystart = " + i._ystart + (null != fieldNameY1 ? (", ystart1 = " + i._ystart1) : "") + (null != fieldNameY2 ? (", ystart2 = " + i._ystart2) : "");
                    if (!logged.contains(s)) {
                        log.info(s);
                        logged.add(s);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } catch (Throwable t) {
            }
            return i;
        }

        protected static List<String> getTokenList(final String targetsrc, final int keta, final int gyo, final Param param) {
            final List<String> lines = getTokenList(targetsrc, keta, param);
            if (lines.size() > gyo) {
                return lines.subList(0, gyo);
            }
            return lines;
        }

        protected static List<String> getTokenList(final String targetsrc0, final int dividlen, final Param param) {
            if (targetsrc0 == null) {
                return Collections.EMPTY_LIST;
            }
            return KNJ_EditKinsoku.getTokenList(targetsrc0, dividlen);
        }

        protected void setForm(final String formname, final int n) {
            log.info(" set form = " + formname);
            _svf.VrSetForm(formname, n);
            _form = formname;
            if (!_formFieldInfoMap.containsKey(_form)) {
                _formFieldInfoMap.put(_form, null);
                try {
                    _formFieldInfoMap.put(_form, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
                } catch (Throwable t) {
                    log.warn(" no class SvfField.");
                }
            }
            if (!_svfFormMap.containsKey(_form)) {
                _svfFormMap.put(_form, null);
                try {
                    final SvfForm svfForm = new SvfForm(new File(_svf.getPath(_form)));
                    if (svfForm.readFile()) {
                        _svfFormMap.put(_form, svfForm);
                    }
                } catch (Throwable t) {
                    log.warn(" no class SvfForm.", t);
                }
            }
        }

        protected SvfForm getSvfForm(final String formname) {
            return _svfFormMap.get(formname);
        }

        protected int vrsOutSelectField(final List<String> fieldList, final String data) {
            return vrsOut(getFieldForData(fieldList, data), data);
        }

        protected int vrsOut(final String field, final String data) {
            if (null == getSvfField(field)) {
                final String message = " no such field : " + field;
                if (!logged.contains(message)) {
                    log.warn(message);
                    logged.add(message);
                }
                return -100;
            }
            if (_notOutput) {
                _svf.VrAttribute(field, "X=10000");
                return _svf.VrsOut(field, data);
            }
            return _svf.VrsOut(field, data);
        }

        protected int vrImageOut(final String field, final String path) {
            if (null != path) {
                return vrsOut(field, path);
            }
            return -100;
        }

        protected String getFieldForData(final List<String> fieldList, final String data) {
            final int datasize = KNJ_EditEdit.getMS932ByteLength(data);
            String fieldFound = null;
            searchField:
            for (int i = 0; i < fieldList.size(); i++) {
                final String fieldname = fieldList.get(i);
                final SvfField svfField = getSvfField(fieldname);
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

        protected int vrsOutNotNull(final String field, final String data) {
            if (null == data) {
                return -100;
            }
            return vrsOut(field, data);
        }

        protected int vrsOutnSelectField(final List<String> fieldLists, final int gyo, final String data) {
            return vrsOutn(getFieldForData(fieldLists, data), gyo, data);
        }

        protected int vrsOutn(final String field, final int gyo, final String data) {
            if (null == getSvfField(field)) {
                final String message = " no such field : " + field;
                if (!logged.contains(message)) {
                    log.warn(message);
                    logged.add(message);
                }
                return -100;
            }
            return _svf.VrsOutn(field, gyo, data);
        }

        protected String[] getNameLines(final PersonalInfo pInfo) {
            if (pInfo._useRealName &&
                    pInfo._nameOutputFlg &&
                    !StringUtils.isBlank(pInfo._studentRealName + pInfo._studentName) &&
                    !pInfo._studentRealName.equals(pInfo._studentName)
            ) {
                final String printName1 = pInfo._studentRealName;
                final String printName2 = pInfo._studentName;
                return new String[] {printName1, printName2};
            }
            final String printName = pInfo._useRealName ? pInfo._studentRealName : pInfo._studentName;
            return new String[] {printName};
        }

        /**
         * 名前を印字する
         * @param svf
         * @param name 名前
         * @param fieldData フィールドのデータ
         */
        protected void printName(final String name, final KNJSvfFieldModify fieldData) {
            printName(name, fieldData, false);
        }

        /**
         * 名前を印字する
         * @param svf
         * @param name 名前
         * @param fieldData フィールドのデータ
         * @param isCentring 中央寄せするか
         */
        protected void printName(final String name, final KNJSvfFieldModify fieldData, final boolean isCentering) {
            final double charSize = KNJSvfFieldModify.getCharSize(name, fieldData);
            _svf.VrAttribute(fieldData._field, "Size=" + charSize);
            _svf.VrAttribute(fieldData._field, "Y=" + (int) KNJSvfFieldModify.getYjiku(0, charSize, fieldData));
            if (isCentering) {
                if (-1.0f != charSize) {
                    final int offset = KNJSvfFieldModify.getModifiedCenteringOffset(fieldData._x1, fieldData._x2, KNJ_EditEdit.getMS932ByteLength(name), charSize);
                    _svf.VrAttribute(fieldData._field, "X=" + (fieldData._x1 - offset));
                }
            }
            vrsOut(fieldData._field, name);
        }

        protected int getFieldLength(final String fieldname, final int defval) {
            int fieldLength = defval;
            try {
                SvfField f = getSvfField(fieldname);
                if (null == f) {
                    log.info(" no such field : " + fieldname);
                } else {
                    fieldLength = f._fieldLength;
                }
            } catch (Throwable t) {
                log.info(" failsafe = " + t + " / " + _form + "." + fieldname, t);
            }
            return fieldLength;
        }

        protected int getFieldRepeatCount(final String fieldname, final int defval) {
            int repeatCount = defval;
            try {
                SvfField f = getSvfField(fieldname);
                if (null == f) {
                    log.info(" no such field : " + fieldname);
                } else {
                    repeatCount = f._fieldRepeatCount;
                }
            } catch (Throwable t) {
                log.info(" failsafe = " + t + " / " + _form + "." + fieldname, t);
            }
            return repeatCount;
        }

        public abstract int getPrintPage(final Student student);

        public static int getPrintPageDefault(final Student student, final Param param) {
            return student._printEntGrdHistList.size();
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public abstract boolean printSvf(final DB2UDB db2, final Student student, final PersonalInfo personalInfo);

        public void close() {
            // オーバーライドして使用
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理] 生徒指導要録 様式1（学生に関する記録）
     */
    private static class KNJA133P_1 extends KNJA133P_0 {

        KNJA133P_1(final Param param, final Vrw32alp svf) {
            super(param, svf);
        }

        public int getPrintPage(final Student student) {
            return getPrintPageDefault(student, param());
        }

        /**
         * SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            boolean nonedata = false;
            final String form0;
            if (param().isTokubetsuShien()) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    form0 = "KNJA134P_11.frm";
                } else {
                    form0 = "KNJA134P_1.frm";
                }
            } else {
                if (param()._isKyoaiP) {
                    form0 = "KNJA133P_11KYOAI.frm";
                } else if (param()._isHibarigaoka) {
                    form0 = "KNJA133P_11HIBARI.frm";
                } else {
                    form0 = "KNJA133P_11.frm";
                }
            }
            setForm(form0, 1);
            final String form = modifyForm1(form0, student, personalInfo);
            if (!form.equals(form0)) {
                setForm(form, 1);
            }
            printSvfDefault(db2, personalInfo);
            printSchoolInfo(); // 学校情報
            printPersonalInfo(db2, student, personalInfo); // 個人情報
            printAddress(personalInfo); // 住所履歴情報 保護者住所履歴情報
            printTransfer(db2, student, personalInfo); // 異動履歴情報
            printGakuseki1(student, personalInfo); // 年次・ホームルーム・整理番号
            printAftetGraduatedCourse(student);
            _svf.VrEndPage();
            nonedata = true;
            return nonedata;
        }

        private String FLG_REMOVE_INN = "FLG_REMOVE_INN";
        private String FLG_REPLACE_TITLE = "FLG_REPLACE_TITLE";
        private String modifyForm1(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if ("1".equals(param()._seitosidoYorokuNotPrintTextInn)) {
                flgMap.put(FLG_REMOVE_INN, "1");
            }
            if ((param()._isNaraken || param()._isKyoto) && param().isTokubetsuShien()) {
                flgMap.put(FLG_REPLACE_TITLE, param()._isNaraken ? "小学部児童指導要録" : "小学部児童指導要録（様式）");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_REMOVE_INN)) {
                for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("校長氏名印")) {
                    svfForm.move(koteiMoji, koteiMoji.replaceMojiWith("校長氏名"));
                }
                for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("氏名印")) {
                    svfForm.move(koteiMoji, koteiMoji.replaceMojiWith("氏名"));
                }
            }
            if (null != flgMap.get(FLG_REPLACE_TITLE)) {
                for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("小学部児童指導要録")) {
                    svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(flgMap.get(FLG_REPLACE_TITLE)).addX(-200).setEndX(koteiMoji._endX + 200));
                }
            }
            return true;
        }

        private void printSvfDefault(final DB2UDB db2, final PersonalInfo personalInfo) {
            vrsOut("ENTERDIV1", "第　学年　入学");
            vrsOut("ENTERDIV2", "第　学年編入学");
            vrsOut("ENTERDIV3", "第　学年転入学");
            vrsOut("BIRTHDAY", setDateFormat2(param(), null) + "生");
            final String format = setDateFormat(db2, param(), null, param()._year);
            vrsOut("ENTERDATE1", format);
            vrsOut("ENTERDATE3", format);
            vrsOut("TRANSFER_DATE_1", "（" + format + "）");
            vrsOut("TRANSFER_DATE_2", "　" + format + "　");
            vrsOut("TRANSFER_DATE_4", format);
            vrsOut("GRADDATE", format);
            for (int i = 0; i < 6; i++) {
                vrsOut("YEAR_" + (i + 1), setNendoFormat(db2, param(), null, param()._year));
            }
            if (personalInfo._gakusekiList.size() > 0) {
                final Gakuseki gakuseki = personalInfo._gakusekiList.get(0);
                final String setNendoFormat = KNJ_EditDate.setNendoFormat(db2, null, gakuseki._year);
                log.info(" gradeCd = " + gakuseki._gradeCd + " / " + personalInfo._gakusekiList);
                for (int g = 1; g <= gakuseki._gradeCd; g++) {
                    vrsOut("YEAR_" + String.valueOf(g), setNendoFormat);
                }
            }
        }

        /**
         * 学校情報
         */
        private void printSchoolInfo() {
            final SchoolInfo schoolInfo = param()._schoolInfo;
            if (null == schoolInfo) {
                return;
            }
            final String schoolName1 = !StringUtils.isEmpty(schoolInfo._certifSchoolName) ? schoolInfo._certifSchoolName : schoolInfo._schoolName1;
            vrsOut("NAME_gakko1", schoolName1);

            if ("1".equals(param()._schoolzip)) {
                vrsOut("ZIPCODE", schoolInfo._schoolZipCd);
            }
            vrsOut("ADDRESS_gakko1", schoolInfo._schoolAddr1);
            vrsOut("ADDRESS_gakko2", schoolInfo._schoolAddr2);
        }

        /**
         * 個人情報
         */
        private void printPersonalInfo(final DB2UDB db2, final Student student, final PersonalInfo info) {
            final boolean isFinschoolText;
            if (!StringUtils.isBlank(info._entGrdHistDat._nyugakumaeSyussinJouhou)) {
                isFinschoolText = true;
            } else if (param()._isMusashinohigashi) {
                isFinschoolText = false;
            } else {
                isFinschoolText = "1".equals(param()._use_finSchool_teNyuryoku_P);
            }
            if (param()._isOutputDebug) {
                log.info(" isFinschoolText = " + isFinschoolText);
            }

            if (null != info) {
                final int kanaHeight = 40;
                final int kanaMinnum = 20;
                printName(info._studentKana, getFieldInfo("KANA", null, null, kanaHeight, kanaMinnum));

                if (null != param()._simei) {
                    final int nameHeight = 80;
                    final int nameMinnum = 20;
                    final int nameMaxnum = 48;

                    final String name1 = info._studentName1;
                    final String name2 = info._studentNameHistFirst;

                    if (StringUtils.isBlank(name2) || name2.equals(name1)) {
                        // 履歴なしもしくは履歴の名前が現データの名称と同一
                        final KNJSvfFieldModify fieldData1 = getFieldInfo("NAME1", null, null, nameHeight, nameMinnum);
                        printName(name1, fieldData1);
                    } else {
                        final KNJSvfFieldModify fieldData11 = getFieldInfo("NAME1_1", null, null, nameHeight, nameMinnum);
                        final KNJSvfFieldModify fieldData21 = getFieldInfo("NAME2_1", null, null, nameHeight, nameMinnum);
                        printName(name2, fieldData11);
                        printCancelLine(_svf, fieldData11._field, Math.min(KNJ_EditEdit.getMS932ByteLength(name2), nameMaxnum)); // 打ち消し線
                        printName(name1, fieldData21);
                    }
                }

                printName(info._guardKana, getFieldInfo("GUARD_KANA", null, null, kanaHeight, kanaMinnum));
                if (null != param()._simei) {
                    final int nameHeight = param()._isFukuiken ? KNJSvfFieldModify.charSizeToPixel(12) : 80;
                    final int nameMinnum = 20;
                    final int nameMaxnum = 48;

                    final String gname1 = info._guardName;
                    final String gname2 = info._guardNameHistFirst;

                    if (StringUtils.isBlank(gname2) || gname2.equals(gname1)) {
                        // 履歴なしもしくは履歴の名前が現データの名称と同一
                        final KNJSvfFieldModify fieldDataG1 = getFieldInfo("GUARD_NAME1", null, null, nameHeight, nameMinnum);
                        printName(gname1, fieldDataG1);
                    } else {
                        final KNJSvfFieldModify fieldDataG11 = getFieldInfo("GUARD_NAME1_1", null, null, nameHeight, nameMinnum);
                        final KNJSvfFieldModify fieldDataG21 = getFieldInfo("GUARD_NAME2_1", null, null, nameHeight, nameMinnum);
                        printName(gname2, fieldDataG11);
                        printCancelLine(_svf, fieldDataG11._field, Math.min(KNJ_EditEdit.getMS932ByteLength(gname2), nameMaxnum)); // 打ち消し線
                        printName(gname1, fieldDataG21);
                    }
                }
                vrsOut("BIRTHDAY", info._birthdayStr);
                vrsOut("SEX", info._sex);

                if (!isFinschoolText) {
                    vrsOut("J_GRADUATEDDATE_Y", info._finishDate);
                    if (param()._isSundaikoufu) {
                        _svf.VrAttribute("J_GRADUATEDDATE_Y", "Size=8.7");
                    }

                    if (param()._isInstallationDivPrint) {
                        vrsOut("INSTALLATION_DIV", info._installationDiv);
                    }
                    final String typename = "1".equals(param()._notPrintFinschooltypeName) ? "" : StringUtils.defaultString(info._finschoolTypeName);
                    printFinSchool(info._jName, typename + "卒園", "INSTALLATION_DIV", "FINSCHOOL"); // 入学前学歴の学校名編集
                }
            }

            if (param()._isHibarigaoka) {
                final StringBuffer stb = new StringBuffer();
                StringBuffer gengou = new StringBuffer(StringUtils.defaultString(KNJ_EditDate.gengou(db2, Integer.parseInt(NumberUtils.isDigits(info._entYear) ? info._entYear : param()._year))));
                int idx = -1;
                for (int i = gengou.length() - 1; i >= 0; i --) {
                    if (!Character.isDigit(gengou.charAt(i))) {
                        break;
                    }
                    idx = i;
                }
                if (idx == -1 || idx == 0) {
                    gengou.delete(0, gengou.length());
                    gengou.append("　　"); // 元号がない
                } else {
                    gengou.delete(idx, gengou.length());
                }
                stb.append(gengou).append("　　　年４月から").append(gengou).append("　　　年３月まで");
                stb.append(StringUtils.repeat(" ", 60 - KNJ_EditEdit.getMS932ByteLength(stb.toString())));

                stb.append(StringUtils.repeat(" ", 60));

                final StringBuffer stb2 = new StringBuffer(StringUtils.defaultString(info._entGrdHistDat._nyugakumaeSyussinJouhou, "幼稚園")).append("　在園");
                stb.append(StringUtils.repeat(" ", 60 - KNJ_EditEdit.getMS932ByteLength(stb2.toString())));
                stb.append(stb2);

                vrsOut("keireki1", stb.toString());

            } else if (isFinschoolText) {
                final String[] keireki = KNJ_EditEdit.get_token(info._entGrdHistDat._nyugakumaeSyussinJouhou, 50, 4);
                if (null != keireki) {
                    for (int i = 0; i < keireki.length; i++) {
                        vrsOut("KEIREKI" + (i + 1), keireki[i]);
                    }
                }
            }
        }

        /**
         * 個人情報 住所履歴情報 履歴を降順に読み込み、最大件数まで出力
         */
        private void printAddress(final PersonalInfo personalInfo) {
            final int max = 2;
            final int num = personalInfo._addressList.size();
            int i = Math.min(max, num); // 出力件数

            for (final Iterator<Address> it = personalInfo._addressList.iterator(); it.hasNext() && i > 0; i--) {
                final Address addressRecord = it.next();

                if (param()._schzip != null) {
                    vrsOut("ZIPCODE" + i, addressRecord._zipCd);
                    printAddressLine("ZIPCODELINE" + i, addressRecord._zipCd, i, num, max);
                }
                final int n1 = KNJ_EditEdit.getMS932ByteLength(addressRecord._address1);
                final int n2 = KNJ_EditEdit.getMS932ByteLength(addressRecord._address2);
                if (0 < n1) {
                    final boolean use2 = 40 < n1;
                    final String field1 = "ADDRESS" + i + "_1" + (use2 ? "_2" : "_1");
                    final String field2 = "ADDRESSLINE" + i + "_1" + (use2 ? "_2" : "_1");
                    vrsOut(field1, addressRecord._address1);
                    printAddressLine(field2, addressRecord._address1, i, num, max);
                }
                if (0 < n2 && addressRecord._isPrintAddr2) {
                    final boolean use2 = 40 < n2 || 40 < n1;
                    final String field1 = "ADDRESS" + i + "_2" + (use2 ? "_2" : "_1");
                    final String field2 = "ADDRESSLINE" + i +"_2" + (use2 ? "_2" : "_1");
                    vrsOut(field1, addressRecord._address2);
                    printAddressLine(field2, addressRecord._address2, i, num, max);
                }
            }

            final String SAME_TEXT = "児童の欄に同じ";
            final int max2 = 2;
            if (isSameAddressList(take(personalInfo._addressList, max2), take(personalInfo._guardianAddressList, max2))) {
                // 住所が生徒と同一
                vrsOut("GUARDIANADD1_1_1", SAME_TEXT);
                return;
            } else {
                final int num2 = personalInfo._guardianAddressList.size();
                int i2 = Math.min(max2, num2); // 出力件数

                final String addr1 = StringUtils.defaultString(personalInfo._schAddress1);
                final String addr2 = StringUtils.defaultString(personalInfo._schAddress2);

                for (final Iterator<Address> it = personalInfo._guardianAddressList.iterator(); it.hasNext() && i2 > 0; i2--) {
                    final Address guardianAddressRec = it.next();

                    final String address1 = StringUtils.defaultString(guardianAddressRec._address1);
                    final String address2 = StringUtils.defaultString(guardianAddressRec._address2);

                    // 上からmax-i番目の生徒住所とチェック
                    final Address schregAddressRec = getSameLineSchregAddress(personalInfo, i2, max2);
                    boolean isSameAddress = 1 < personalInfo._guardianAddressList.size() && isSameAddress(schregAddressRec, guardianAddressRec);
                    if ((i2 == max2 || i2 == num2) && !isSameAddress) {
                        // 最新の生徒住所とチェック
                        isSameAddress = addr1.equals(address1) && addr2.equals(address2);
                    }

                    if (isSameAddress) {
                        // 内容が生徒と同一
                        vrsOut("GUARDIANADD" + i2 + "_1_1", SAME_TEXT);
                        printAddressLine("GUARDIANADDLINE" + i2 + "_1_1", SAME_TEXT, i2, num2, max2);
                    } else {
                        if (param()._schzip != null) {
                            vrsOut("GUARDZIP" + i2, guardianAddressRec._zipCd);
                            printAddressLine("GUARDZIPLINE" + i2, guardianAddressRec._zipCd, i2, num2, max2);
                        }
                        final int n1 = KNJ_EditEdit.getMS932ByteLength(address1);
                        final int n2 = KNJ_EditEdit.getMS932ByteLength(address2);
                        if (0 < n1) {
                            final boolean use2 = 40 < n1;
                            final String p1 = use2 ? "_1_2" : "_1_1";
                            vrsOut("GUARDIANADD" + i2 + p1, address1);
                            printAddressLine("GUARDIANADDLINE" + i2 + p1, address1, i2, num2, max2);
                        }
                        if (0 < n2 && guardianAddressRec._isPrintAddr2) {
                            final boolean use2 = 40 < n2 || 40 < n1;
                            final String p2 = use2 ? "_2_2" : "_2_1";
                            vrsOut("GUARDIANADD" + i2 + p2, address2);
                            printAddressLine("GUARDIANADDLINE" + i2 + p2, address2, i2, num2, max2);
                        }
                    }
                }
            }
        }

        private boolean isSameAddressList(final List<Address> addrListA, final List<Address> addrListB) {
            boolean rtn = true;
            if (addrListA == null || addrListA.isEmpty() || addrListB == null || addrListB.isEmpty() || addrListA.size() != addrListB.size()) {
                rtn = false;
            } else {
                final int max = addrListA.size(); // == addrList2.size();
                for (int i = 0; i < max; i++) {
                    final Address addressAi = addrListA.get(i);
                    final Address addressBi = addrListB.get(i);
                    if (!isSameAddress(addressAi, addressBi)) {
                        rtn = false;
                        break;
                    }
                }
            }
            return rtn;
        }

        private boolean isSameAddress(final Address addressAi, final Address addressBi) {
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

        private <T> List<T> take(final List<T> list, final int max) {
            final List<T> rtn = new ArrayList<T>();
            int i = 0;
            for (final Iterator<T> it = list.iterator(); it.hasNext() && i < max; i++) {
                final T o = it.next();
                rtn.add(o);
            }
            return rtn;
        }

        private Address getSameLineSchregAddress(final PersonalInfo personalInfo, final int i, final int max) {
            Address rtn = null;
            final int size = personalInfo._addressList.size();
            int idx = -100;
            if (max <= size) {
                idx = max - i;
            } else if (size - i < size) {
                idx = size - i; // 0以上とは限らない
            }
            if (0 <= idx) {
                rtn = personalInfo._addressList.get(idx);
            }
            return rtn;
        }

        /**
         *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
         *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
         *  全角スペースより前半の文字を○○○○○立と見なします。
         *  @param str1 例えば"千代田区　アルプ"
         *  @param str2 例えば"潤オ校卒業"
         *  @param field1
         *  @param field2
         */
        private void printFinSchool(
                final String str1,
                final String str2,
                final String field1,
                final String field2
        ) {
            final String schoolName;
            if (null == str1) {
                schoolName = "";
            } else {
                final int i = str1.indexOf('　');  // 全角スペース
                if (-1 < i && 5 >= i) {
                    final String ritu = str1.substring(0, i);
                    if (null != ritu) {
                        vrsOut(field1,  ritu + "立");
                    }
                    schoolName = str1.substring(i + 1);
                } else {
                    schoolName = str1;
                }
            }
            final int schoolNameLen = KNJ_EditEdit.getMS932ByteLength(schoolName);

            final String kotei = StringUtils.defaultString(str2);
            final int koteiLen = KNJ_EditEdit.getMS932ByteLength(kotei);

            if (schoolNameLen == 0) {
                vrsOut(field2 + "1", kotei);
                if (param()._isSundaikoufu) {
                    _svf.VrAttribute(field2 + "1", "Size=8.7");
                }
            } else if (schoolNameLen + koteiLen <= 40) {
                vrsOut(field2 + "1", schoolName + kotei);
                if (param()._isSundaikoufu && schoolNameLen + koteiLen <= 30) {
                    _svf.VrAttribute(field2 + "1", "Size=8.7");
                }
            } else if(schoolNameLen + koteiLen <= 50) {
                vrsOut(field2 + "2", schoolName + kotei);
            } else {
                vrsOut(field2 + "2", schoolName);
                vrsOut(field2 + "3", kotei);
            }
        }

        /**
         * 住所の取り消し線印刷
         * @param svf
         * @param i
         * @param num
         * @param max 欄の数
         */
        private void printAddressLine(final String field, final String str, final int i, final int num, int max) {
            if (null == str || i == num || i == max) {
                return;
            }
            printCancelLine(_svf, field, KNJ_EditEdit.getMS932ByteLength(str));
        }

        /**
         * 取り消し線印刷
         * @param svf
         * @param keta 桁
         */
        private void printCancelLine(final Vrw32alp svf, final String field, final int keta) {
            if (_notOutput) {
                return;
            }
            svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + keta);
        }

        /**
         * 個人情報 異動履歴情報
         */
        private void printTransfer(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            try {
                printCancelLine(_svf, "LINE1", 14);
                printCancelLine(_svf, "LINE2", 14);

                final int A002_NAMECD2_TENNYUGAKU = 4;
                final int A002_NAMECD2_5 = 5;
                final int A003_NAMECD2_SOTSUGYO = 1;
                final int A003_NAMECD2_TAIGAKU = 2;
                final int A003_NAMECD2_TENGAKU = 3;

                // 入学区分
                if (null != personalInfo._entDiv) {
                    final String gradeStr;
                    if (personalInfo._entYear != null) {
                        final int grade = student.currentGradeCd(param()) - (Integer.parseInt(param()._year) - Integer.parseInt(personalInfo._entYear));
                        gradeStr = (grade <= 0 ? "  " : grade > 9 ? String.valueOf(grade) : param()._isHibarigaoka ? hankakuToZenkaku(String.valueOf(grade)) : " " + String.valueOf(grade));
                    } else {
                        gradeStr = "  ";
                    }
                    final int entdiv = personalInfo._entDiv.intValue();
                    if (entdiv == A002_NAMECD2_TENNYUGAKU) { // 転入学
                        if (personalInfo._entDate != null) {
                            vrsOut("ENTERDATE3", setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._entDate), param()._year));
                        }
                        vrsOut("ENTERDIV3", "第" + gradeStr + "学年転入学");
                        vrsOutNotNull("TRANSFERREASON1_1", personalInfo._entReason);
                        vrsOutNotNull("TRANSFERREASON1_2", personalInfo._entSchool);
                        vrsOutNotNull("TRANSFERREASON1_3", personalInfo._entAddr);
                    } else {
                        if (entdiv != A002_NAMECD2_5) {
                            if (personalInfo._entDate != null) {
                                vrsOut("ENTERDATE1", setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._entDate), param()._year));
                            }
                            _svf.VrAttribute("LINE1", "X=5000");
                            vrsOut("ENTERDIV1", "第" + gradeStr + "学年　入学");
                        } else {
                            if (personalInfo._entDate != null) {
                                vrsOut("ENTERDATE2", setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._entDate), param()._year));
                            }
                            _svf.VrAttribute("LINE2", "X=5000");
                            vrsOut("ENTERDIV2", "第" + gradeStr + "学年編入学");
                        }
                        vrsOutNotNull("ENTERRESONS1", personalInfo._entReason);
                        vrsOutNotNull("ENTERRESONS2", personalInfo._entSchool);
                        vrsOutNotNull("ENTERRESONS3", personalInfo._entAddr);
                    }
                }

                // 除籍区分
                if (null != personalInfo._grdDiv) {
                    final int grddiv = personalInfo._grdDiv.intValue();
                    if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_TENGAKU) {
                        // 2:退学 3:転学
                        if (personalInfo._grdDate != null) {
                            vrsOut("TRANSFER_DATE_1", "（" + setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._grdDate), param()._year) + "）");
                        }
                        vrsOutNotNull("TRANSFERREASON2_1", personalInfo._grdReason);
                        vrsOutNotNull("TRANSFERREASON2_2", personalInfo._grdSchool);
                        vrsOutNotNull("TRANSFERREASON2_3", personalInfo._grdAddr);
                    } else if (grddiv == A003_NAMECD2_SOTSUGYO) {
                        // 1:卒業
                        if (personalInfo._grdDate != null) {
                            vrsOut("TRANSFER_DATE_4", setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._grdDate), param()._year));
                        }
                    }
                }

                if (null == personalInfo._entGrdHistDat._tengakuSakiZenjitu && null != personalInfo._grdDiv && personalInfo._grdDiv.intValue() == A003_NAMECD2_TENGAKU && personalInfo._grdDate != null) {
                    final String dateFormatJp = setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._grdDate), param()._year);
                    vrsOut("TRANSFER_DATE_2", "　" + dateFormatJp + "　");
                } else if (null != personalInfo._entGrdHistDat._tengakuSakiZenjitu) {
                    final String dateFormatJp = setDateFormat(db2, param(), h_format_JP(db2, param(), personalInfo._entGrdHistDat._tengakuSakiZenjitu), param()._year);
                    vrsOut("TRANSFER_DATE_2", "　" + dateFormatJp + "　");
                }

            } catch (Exception ex) {
                log.error("printSvfDetail_4 error!", ex);
            }
        }

        private String hankakuToZenkaku(final String hankaku) {
            final StringBuffer stb = new StringBuffer();
            if (null != hankaku) {
                final Map m = new HashMap();
                for (int i = 0; i < 10; i++) {
                    final char zen = (char) ('０' + i);
                    m.put(String.valueOf(i), String.valueOf(zen));
                }

                for (int i = 0; i < hankaku.length(); i++) {
                    String ch = String.valueOf(hankaku.charAt(i));
                    if (null != m.get(ch)) {
                        stb.append(m.get(ch));
                    } else {
                        stb.append(hankaku.charAt(i));
                    }
                }
            }
            return stb.toString();
        }

        private void printGakuseki1(final Student student, final PersonalInfo personalInfo) {

            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                final String si = String.valueOf(gakuseki._pos);

                if (!PersonalInfo.isTargetYear(student, personalInfo, gakuseki._year, param())) {
                    // log.debug(" skip print year = " + regdRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }

                vrsOutSelectField(Arrays.asList("HR_CLASS1_" + si, "HR_CLASS2_" + si), gakuseki._hrname); // 組

                vrsOut("ATTENDNO_" + si, String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号

                vrsOut("YEAR_" + si, gakuseki._nendo);

                final List<Map<String, String>> principalList = getMappedList(param()._staffInfo._yearPrincipalListMap, gakuseki._year);
                String principalStaffcd1 = null;
                String principalStaffcd2 = null;
                String principal1FromDate = null;
                String principal1ToDate = null;
                String principal2FromDate = null;
                String principal2ToDate = null;
                if (null != principalList && principalList.size() > 0) {
                    final Map<String, String> listLast = principalList.get(principalList.size() - 1);

                    Map<String, String> last = null;
                    if (null != personalInfo._grdDate) {
                        // 生徒の卒業日付以降の校長は対象外
                        for (int j = 0; j < principalList.size(); j++) {
                            final Map principal = principalList.get(j);
                            final String fromDate = KnjDbUtils.getString(principal, "FROM_DATE");
                            if (fromDate.compareTo(personalInfo._grdDate) > 0) {
                                break;
                            }
                            last = principal;
                        }
                    }
                    if (null != KnjDbUtils.getString(last, "FROM_DATE") && !KnjDbUtils.getString(last, "FROM_DATE").equals(KnjDbUtils.getString(listLast, "FROM_DATE"))) {
                        log.info(" principal last date = " + KnjDbUtils.getString(last, "FROM_DATE") + " instead of " + KnjDbUtils.getString(listLast, "FROM_DATE") + " (student grddate = " + personalInfo._grdDate + ")");
                    } else {
                        last = listLast;
                    }
                    principalStaffcd1 = KnjDbUtils.getString(last, "STAFFCD");
                    principal1FromDate = KnjDbUtils.getString(last, "FROM_DATE");
                    principal1ToDate = KnjDbUtils.getString(last, "TO_DATE");
                    final Map<String, String> first = principalList.get(0);
                    principalStaffcd2 = KnjDbUtils.getString(first, "STAFFCD");
                    principal2FromDate = KnjDbUtils.getString(first, "FROM_DATE");
                    principal2ToDate = KnjDbUtils.getString(first, "TO_DATE");
                }
                final Staff principal1 = new Staff(gakuseki._year, StaffMst.get(param()._staffInfo._staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, param()._staffInfo.getStampNo(param(), principalStaffcd1, gakuseki._year));
                final Staff principal2 = new Staff(gakuseki._year, StaffMst.get(param()._staffInfo._staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);

                // 校長氏名
                printStaffName("1", gakuseki._pos, true, gakuseki._principal, principal1, principal2);


                Staff staff1Last = Staff.Null, staff1First = Staff.Null;
                final List<Staff> studentStaff1HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV1, gakuseki._year);
                if (studentStaff1HistList.size() > 0) {
                    staff1Last = studentStaff1HistList.get(studentStaff1HistList.size() - 1);
                    staff1First = studentStaff1HistList.get(0);
                }
                if (param()._isNaraken || param()._isKyoto) {
                    // 担任1、担任2、担任3を表示
                    final List<Staff> studentStaff2HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV2, gakuseki._year);
                    final List<Staff> studentStaff3HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV3, gakuseki._year);
                    final Staff staff1 = Util.last(studentStaff1HistList, Staff.Null);
                    final Staff staff2 = Util.last(studentStaff2HistList, Staff.Null);
                    final Staff staff3 = Util.last(studentStaff3HistList, Staff.Null);
                    final int lineKeta = 26;
                    final String name1 = staff1.getNameString(param(), lineKeta);
                    final String name2 = staff2.getNameString(param(), lineKeta);
                    final String name3 = staff3.getNameString(param(), lineKeta);
                    final String priOrStaff = "2";
                    final int pos = gakuseki._pos;
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_2", name1);
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_3", name2);
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_4", name3);

                } else {
                    // 担任氏名
                    printStaffName("2", gakuseki._pos, false, Staff.Null, staff1Last, staff1First);
                }

                //印影
                if (null != param()._inei) {
                    log.debug("印影："+param()._inei);
                    log.debug("改竄："+gakuseki._kaizanFlg); // 改竄されていないか？
                    log.debug("署名（校長）："+gakuseki._principalSeq);
                    log.debug("署名（担任）："+gakuseki._staffSeq);

                    //署名（校長）しているか？
                    if (null == gakuseki._kaizanFlg && null != gakuseki._principalSeq || Arrays.asList("2", "4").contains(param()._inei)) {
                        final String field;
                        if ("1".equals(param()._colorPrint)) {
                            field = "STAFFBTM_1_" + si + "C";
                        } else {
                            field = "STAFFBTM_1_" + si;
                        }
                        final Staff staff;
                        if (null == principal1._staffMst._staffcd) {
                            staff = gakuseki._principal;
                        } else {
                            staff = principal1;
                        }
                        printStaffStamp(field, staff); // 校長印
                    }
                    //署名（担任）しているか？
                    if (null == gakuseki._kaizanFlg && null != gakuseki._staffSeq || Arrays.asList("2", "3", "4").contains(param()._inei)) {
                        final String field;
                        if ("1".equals(param()._colorPrint)) {
                            field = "STAFFBTM_2_" + si + "C";
                        } else {
                            field = "STAFFBTM_2_" + si;
                        }
                        printStaffStamp(field, staff1Last); // 担任印
                    }
                }
            }
        }

        private void printStaffStamp(final String field, final Staff staff) {
            final String path = param().getImageFilePath(staff._stampNo);
            if (null == staff._stampNo) {
                param().logOnce(" no stampNo : " + staff);
            } else if (null == path) {
                param().logOnce(" no stamp path : " + staff);
            } else {
                vrImageOut(field, path);
            }
        }

        private void printStaffName(final String priOrStaff, final int pos, final boolean isCheckStaff0, final Staff staff0, final Staff staff1, final Staff staff2) {
            final int keta = 26;
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                vrsOutSelectField(Arrays.asList("STAFFNAME_" + priOrStaff + "_" + pos, "STAFFNAME_" + priOrStaff + "_" + pos + "_1"), staff0.getNameString(param(), keta));
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List<String> line = new ArrayList();
                line.addAll(staff1._staffMst.getNameLine(staff1._year, param(), keta));
                if (line.size() == 2) {
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_3", line.get(0));
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_4", line.get(1));
                } else {
                    vrsOutSelectField(Arrays.asList("STAFFNAME_" + priOrStaff + "_" + pos, "STAFFNAME_" + priOrStaff + "_" + pos + "_1"), staff1.getNameString(param(), keta));
                }
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List<String> line = new ArrayList();
                line.addAll(staff2.getNameBetweenLine(param(), keta));
                line.addAll(staff1.getNameBetweenLine(param(), keta));
                if (line.size() == 2) {
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_3", line.get(0));
                    vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_4", line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        vrsOut("STAFFNAME_" + priOrStaff + "_" + pos + "_" + (k + 2), line.get(k));
                    }
                }
            }
        }

        private void printAftetGraduatedCourse(final Student student) {
            if (param()._isSundaikoufu) {
                for (int i = 0; i < student._afterGraduatedCourseTextList.size(); i++) {
                    vrsOut("AFTER_GRADUATION" + String.valueOf(i + 1) + "_2", student._afterGraduatedCourseTextList.get(i));
                }
            } else {
                for (int i = 0; i < student._afterGraduatedCourseTextList.size(); i++) {
                    vrsOut("AFTER_GRADUATION" + String.valueOf(i + 1), student._afterGraduatedCourseTextList.get(i));
                }
            }
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理]  生徒指導要録
     *   各教科・科目の学習の記録
     *   行動の記録
     *   特別活動の記録
     *   出欠の記録
     */
    private static class KNJA133P_3 extends KNJA133P_0 {

        private static int _2017 = 2017;
        private static int _2019 = 2019;
        private static int _9999 = 9999;

        private enum KAITEI {

            _BEFORE_2018(_2017),

            /**
             * 道徳所見欄追加
             */
            _2018_DOUTOKU(_2019),

            /**
             * 評定は観点の教科ごとの最下行に移動。5、6年に英語の評定が追加。
             * 外国語活動所見は3、4年に変更。欄はひとつ。
             */
            _2020(_9999) //
            ;

            int _inclusiveEndYear;
            final int _initializedInclusiveEndYear;
            KAITEI(final int endYear) {
                _inclusiveEndYear = endYear;
                _initializedInclusiveEndYear = endYear;
            }

            public static void init() {
                for (final KAITEI k : KAITEI.values()) {
                    k._inclusiveEndYear = k._initializedInclusiveEndYear;
                }
            }

            public static KAITEI getKaitei(final List<KAITEI> kaiteiList, final String year) {
                if (NumberUtils.isDigits(year)) {
                    final int iyear = Integer.parseInt(year);
                    for (final KAITEI k : kaiteiList) {
                        if (iyear <= k._inclusiveEndYear) {
                            return k;
                        }
                    }
                }
                if (null != year) {
                    log.warn(" KAITEI null : " + year);
                }
                return null;
            }

            public String toString() {
                return super.toString() + "(endYear = " + _inclusiveEndYear + ")";
            }
        }

        private int VIEW_LINE1_MAX = 45;

        KNJA133P_3(final Param param, final Vrw32alp svf) {
            super(param, svf);
        }

        // @Override
        public int getPrintPage(final Student student) {
            int page = 0;
            page = student._printEntGrdHistList.size();
            return page;
        }

        private Map<KAITEI, List<String>> getFormBaseYearList(final List<KAITEI> kaiteiList, final Student student, final PersonalInfo personalInfo) {
            final Set<String> years = new TreeSet<String>();
            for (final Gakuseki g : personalInfo._gakusekiList) {
                if (PersonalInfo.isTargetYear(student, personalInfo, g._year, param())) {
                    years.add(g._year);
                }
            }
            for (final HTrainremarkPdat remark : student._htrainremarkPdatList) {
                if (PersonalInfo.isTargetYear(student, personalInfo, remark._year, param())) {
                    years.add(remark._year);
                }
            }
            for (final ActRecord act : student._actRecordList) {
                if (PersonalInfo.isTargetYear(student, personalInfo, act._year, param())) {
                    years.add(act._year);
                }
            }
            final Map<KAITEI, List<String>> rtn = new TreeMap<KAITEI, List<String>>();
            for (final String year : years) {
                final KAITEI k = KAITEI.getKaitei(kaiteiList, year);
                if (null != k) {
                    getMappedList(rtn, k).add(year);
                }
            }
            if (param()._isOutputDebug) {
                log.info(" formBaseYearList = " + rtn);
            }
            return rtn;
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            boolean hasdata = false;
            final boolean preferNormalFormProcess = param().isTokubetsuShien() && (param()._isNaraken || param()._isKyoto || "1".equals(param().property(Property.useSpecial_Support_School)));

            if (param().isTokubetsuShien() && !preferNormalFormProcess) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    String form = "KNJA134P_13.frm";

                    setForm(form, 4);
                    printGakuseki3(student, personalInfo, null);  //年次・ホームルーム・整理番号
                    printShoken3(student, personalInfo, null, null);  //行動の記録・特別活動の記録
                    printFooter(student, null);
                    printHyotei3(student, personalInfo, null);  //評定
                    if (printKanten(student, personalInfo, null, null)) {
                        hasdata = true;  //観点 ＊ココでsvf.VrEndRecord()
                    }
                } else {
                    return false;
                }
            } else {
                KAITEI.init();
                final Map<KAITEI, String> kaiteiFormMap = getKaiteiFormMap();
                log.info(" kaiteiFormMap = " + kaiteiFormMap);

                final int n = 4;
                final Map<KAITEI, List<String>> formBaseYearList = getFormBaseYearList(new ArrayList<KAITEI>(kaiteiFormMap.keySet()), student, personalInfo);
                for (final Map.Entry<KAITEI, List<String>> e : formBaseYearList.entrySet()) {
                    final KAITEI kaitei = e.getKey();
                    final List<String> years = e.getValue();
                    final String form0 = kaiteiFormMap.get(kaitei);
                    log.info(" kaitei years = " + e.getKey() + ", " + e.getValue() + ", form = " + form0);
                    setForm(form0, n);
                    VIEW_LINE1_MAX = getViewLine1Max(form0);
                    if (param()._isOutputDebug) {
                        log.info(" VIEW_LINE1_MAX = " + VIEW_LINE1_MAX);
                    }
                    String form = modifyForm3(form0, student, personalInfo);
                    if (!form.equals(form0)) {
                        setForm(form, n);
                    }
                    if (KAITEI._2020 == kaitei) {
                        if (param()._isOsakashinnai) {
                            // 大阪信愛は1学年と2学年が対象
                            vrsOut("FOREIGNGRADE1", "1");
                            vrsOut("FOREIGNGRADE2", "2");
                        }
                    }
                    printGakuseki3(student, personalInfo, years);  //年次・ホームルーム・整理番号
                    printShoken3(student, personalInfo, years, kaitei);  //行動の記録・特別活動の記録
                    printFooter(student, years);
                    if (KAITEI._2020 == kaitei) {
                        if (printKanten(student, personalInfo, years, kaitei)) {
                            hasdata = true;
                        }
                    } else {
                        printHyotei3(student, personalInfo, years);  //評定
                        if (printKanten(student, personalInfo, years, kaitei)) {
                            hasdata = true;  //観点 ＊ココでsvf.VrEndRecord()
                        }
                    }
                }
            }
            return hasdata;
        }

        private Map<KAITEI, String> getKaiteiFormMap() {

            final Map<KAITEI, String> kaiteiFormMap = new TreeMap<KAITEI, String>();
            if (param()._isNaraken || param()._isKyoto) {
                if (param()._isKyoto) {
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13_KYOTO.frm");
                } else {
                    // 奈良県
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13NARAKEN.frm");
                }
            } else {
                if (param()._isKyoaiP) {
                    kaiteiFormMap.put(KAITEI._2018_DOUTOKU, "KNJA133P_13KYOAI_SEPARATE_HYOTEI.frm");
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13KYOAI.frm"); // 40行 + 聖書観点3行 + 聖書評定1行
                } else if (param()._isMusashinohigashi) {
                    kaiteiFormMap.put(KAITEI._2018_DOUTOKU, "KNJA133P_13_MUSAHIGA_SEPARATE_HYOTEI.frm");
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13_MUSAHIGA.frm");
                } else if (param()._isHibarigaoka) {
                    kaiteiFormMap.put(KAITEI._2018_DOUTOKU, "KNJA133P_13HIBARI_SEPARATE_HYOTEI.frm");
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13HIBARI.frm");
                } else if (param()._isRitsumeikan) {
                    kaiteiFormMap.put(KAITEI._2018_DOUTOKU, "KNJA133P_13RITSUMEIKAN_SEPARATE_HYOTEI.frm");
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13RITSUMEIKAN.frm");
                } else {
                    kaiteiFormMap.put(KAITEI._BEFORE_2018, "KNJA133P_13_DOUTOKU_NASHI.frm");
                    kaiteiFormMap.put(KAITEI._2018_DOUTOKU, "KNJA133P_13_SEPARATE_HYOTEI.frm");
                    kaiteiFormMap.put(KAITEI._2020, "KNJA133P_13.frm");
                }
                if (kaiteiFormMap.containsKey(KAITEI._BEFORE_2018)) {
                    final String formFilePath1 = super._svf.getPath(kaiteiFormMap.get(KAITEI._BEFORE_2018));
                    if (null == formFilePath1 || !new File(formFilePath1).exists()) {
                        if (null != formFilePath1) {
                            log.warn("no file : " + formFilePath1);
                        }
                        kaiteiFormMap.remove(KAITEI._BEFORE_2018);
                    }
                }
                final String formFilePath3 = super._svf.getPath(kaiteiFormMap.get(KAITEI._2018_DOUTOKU));
                if (new File(formFilePath3).exists()) {
                    // 別名にしたフォームがあるなら、新フォームは適用済みである
                } else {
                    log.warn("no file : " + formFilePath3);
                    kaiteiFormMap.remove(KAITEI._2020);
                    KAITEI._2018_DOUTOKU._inclusiveEndYear = 9999;
                    kaiteiFormMap.put(KAITEI._2018_DOUTOKU, kaiteiFormMap.get(KAITEI._2020));
                }
            }
            return kaiteiFormMap;
        }

        private int getViewLine1Max(String form0) {
            final SvfForm svfForm = getSvfForm(form0);
            int viewLine1Max = -1;
            if (null != svfForm) {
                final SvfForm.Field VIEW1 = svfForm.getField("VIEW1");
                if (null == VIEW1) {
                    log.info(" !!!!!!!!!!!!!!!! NO VIEW1");
                } else {
                    final SvfForm.Record record = svfForm.getRecordOfField(VIEW1);
                    if (null == record) {
                        log.info(" !!!!!!!!!!!!!!!! NO RECORD ");
                    } else {
                        final SvfForm.SubForm subForm = record.getSubForm();
                        if (null == subForm) {
                            log.info(" !!!!!!!!!!!!!!!! NO SUBFORM ");
                        } else {
                            final int recordHeight = record.getHeight();
                            final int subFormHeight = subForm.getHeight();
                            if (recordHeight > 0 && subFormHeight > 0) {
                                viewLine1Max = subFormHeight / recordHeight;
                            }
                        }
                    }
                }
            }
            return viewLine1Max;
        }

        private String FLG_DOUTOKU_SHASEN = "FLG_DOUTOKU_SHASEN";
        private String FLG_HYOTEI_THIN_LINE = "FLG_HYOTEI_THICK_LINE";
        private String FLG_DOUTOKU_TITLE = "FLG_DOUTOKU_TITLE";
        private String modifyForm3(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if (param()._isMusashinohigashi && "2".equals(pInfo._recordDiv)) {
                flgMap.put(FLG_DOUTOKU_SHASEN, "1");
            }
            if (param()._isNaraken) {
                flgMap.put(FLG_HYOTEI_THIN_LINE, "1");
            }
            if (param()._isOsakashinnai) {
                flgMap.put(FLG_DOUTOKU_TITLE, "1");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_DOUTOKU_SHASEN)) {
                for (int g = 1; g <= 6; g++) { // 道徳の欄に斜線
                    final SvfField fieldMoral = getSvfField("MORAL" + String.valueOf(g));
                    final Map attributeMap = fieldMoral.getAttributeMap();
                    final String x = (String) attributeMap.get("X");
                    final String y = (String) attributeMap.get("Y");
                    if (NumberUtils.isDigits(x) && NumberUtils.isDigits(y)) {
                        final SvfForm.Point topLeftPoint = new SvfForm.Point(Integer.parseInt(x), Integer.parseInt(y));
                        final Line upperLine = svfForm.getNearestUpperLine(topLeftPoint);
                        final Line lowerLine = svfForm.getNearestLowerLine(topLeftPoint);
                        final Line leftLine = svfForm.getNearestLeftLine(topLeftPoint);
                        svfForm.addLine(new SvfForm.Line(upperLine._end, new SvfForm.Point(leftLine._start._x, lowerLine._start._y)));
                    }
                }
            }
            if (flgMap.containsKey(FLG_HYOTEI_THIN_LINE)) {
                final SvfForm.Field VIEW1 = svfForm.getField("VIEW1");
                final SvfForm.Record record = svfForm.getRecordOfField(VIEW1);
                final SvfForm.Line leftLine = svfForm.getNearestLeftLine(VIEW1.getPoint());
                final int recordHeight = record.getHeight();
                final SvfForm.SubForm subForm = record.getSubForm();

                final int startY = subForm._point1._y;
                int viewSizeSum = 0;
                for (int cli = 0; cli < pInfo._student._classViewList.size(); cli += 1) {
                    final ClassView cv = pInfo._student._classViewList.get(cli);
                    final int viewSize = cv._viewCdList.size();
                    int y;
                    y = startY + recordHeight * viewSizeSum;
                    svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THIN, record.getAbsPoint1().setY(y - 2), record.getAbsPoint2().setY(y - 2)));
                    viewSizeSum += viewSize;
                    y = startY + recordHeight * viewSizeSum;
                    svfForm.addLine(new SvfForm.Line(SvfForm.LineWidth.THIN, leftLine._start.setY(y), record.getAbsPoint2().setY(y)));
                    viewSizeSum += 1;
                }
            }
            if (flgMap.containsKey(FLG_DOUTOKU_TITLE)) {
                final String text = "特別の教科　道徳";
                final String newText = "宗教　道徳";
                for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(text)) {
                    final int centerX = koteiMoji._point._x + (koteiMoji._endX - koteiMoji._point._x) / 2;
                    final int x = centerX - (centerX - koteiMoji._point._x) * newText.length() / text.length();
                    final int endX = centerX + (koteiMoji._endX - centerX) * newText.length() / text.length();
                    svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(newText).setX(x).setEndX(endX));
                }

            }
            return true;
        }

        /**
         *  評定に表示する教科取得処理
         *
         */
        private List<ClassView> getValuationClass(final Student student, final PersonalInfo personalInfo, final List<String> years) {
            final List<ClassView> valuationViewClassList = new ArrayList<ClassView>();
            for (final ClassView classview : student._classViewList) {

                final List<View> viewCdListList = getViewList(classview, years);

                boolean isSlashAll = true;
                for (int j = 0; j < viewCdListList.size(); j++) {
                    boolean isSlashLine = true;
                    final String recordDiv = param()._isMusashinohigashi ? personalInfo._recordDiv : null;
                    final List<String> slashViewGradeList = param().getSlashViewGradeList(recordDiv, null, classview._subclasscd, j + 1);
                    for (int g = 3; g <= 6; g++) {
                        final boolean isNotSlash = !slashViewGradeList.contains(String.valueOf(g));
                        if (isNotSlash) {
                            isSlashLine = false;
                        }
                    }
                    if (!isSlashLine) {
                        isSlashAll = false;
                    }
                }
                boolean addClassView = !isSlashAll;
                if (param()._isMusashinohigashi) {
                    addClassView = addClassView || StringUtils.split(classview._subclasscd, "-").length >= 4 && "200100".equals(StringUtils.split(classview._subclasscd, "-")[3]);
                }
                if (addClassView) {
                    valuationViewClassList.add(classview);
                }
            }
            return valuationViewClassList;
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目コードの順番を返す。そうでなければ(通常の処理）教科コードを返す。
         * @param student
         * @param classview
         * @return 科目名または教科名
         */
        private String getKeyCd(final Student student, final ClassView classview) {
            final TreeSet<String> set = getSubclassCdSet(student, classview);
            if (set.size() == 1) {
                return classview._classMst.getKey(param());
            } else if (set.size() > 1) {
                final DecimalFormat df = new DecimalFormat("00");
                int order = 0;
                int n = 0;
                for (final String subclassCd : set) {
                    if (classview._subclasscd.equals(subclassCd)) {
                        order = n;
                        break;
                    }
                    n += 1;
                }
                return df.format(order);
            }
            return null;
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
         * @param student
         * @param classview
         * @return 科目名または教科名
         */
        private String getClassShowname(final Student student, final ClassView classview) {
            final TreeSet set = getSubclassCdSet(student, classview);
            if ((set.size() > 1 || "1".equals(classview._classMst._electdiv)) && null != classview._subclassname) {
                return classview._subclassname;
            } else if (set.size() == 1 || classview._isNameMstA046) {
                return classview._classMst._classname;
            }
            return null;
        }

        /**
         * 同一教科の科目コードを得る
         * @param student
         * @param classview
         * @return
         */
        private TreeSet<String> getSubclassCdSet(final Student student, final ClassView classview) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final ClassView classview0 : student._classViewList) {
                if (classview0._classMst.equals(classview._classMst)) {
                    set.add(classview0._subclasscd);
                }
            }
            return set;
        }

        /**
         *  観点出力処理
         *
         */
        private boolean printKanten(final Student student, final PersonalInfo personalInfo, final List<String> years, final KAITEI k) {

            List<ClassView> valuationViewClassList = Collections.emptyList();
            if (KAITEI._2020 == k) {
                valuationViewClassList = hyoteiClassOrderedList(student, personalInfo, years);
                if (param()._isOutputDebugKanten) {
                    log.info(" valuationViewClassList = " + valuationViewClassList);
                }
            }

            final String SPACE_FLG = "#SPACE_FLG";

            final List<Map<String, String>> recordList = new ArrayList<Map<String, String>>();

            for (final ClassView classview : student._classViewList) {
                final String keyCd = getKeyCd(student, classview);
                final String showname = getClassShowname(student, classview);
                final List<View> printViewList = classview.getPrintViewList(years);
                final String printClassname = classview.setPrintClassname(showname, printViewList.size());  // 教科名のセット
                final boolean useClass2 = null != showname && printViewList.size() > 0 && 2 <= showname.length() / printViewList.size();
                final String field = useClass2 ? "CLASS2" : "CLASS1";
                final int inc = useClass2 ? 2 : 1;

                final List<View> viewList = getViewList(classview, years);
                if (viewList.isEmpty()) {
                    log.info(" skip classview = " + classview);
                    continue;
                }
                if (param()._isOutputDebugKanten) {
                    log.info(" classview = " + classview);
                }

                for (int j = 0; j < viewList.size(); j++) {
                    final View view = viewList.get(j);
                    if (param()._isOutputDebugKanten) {
                        log.info(" view " + view._viewcd + " = " + view._yearStatusMap);
                    }

                    final Map<String, String> row = new HashMap<String, String>();
                    recordList.add(row);

                    if (j * inc < printClassname.length()) {
                        row.put(field, printClassname.substring(j * inc, Math.min(printClassname.length(), (j + 1) * inc)));  //教科名称
                    }
                    row.put("CLASSCD1", keyCd);  //教科コード
                    row.put("VIEW1", view._viewname);  //観点名称

                    final List<String> slashViewGradeList = param().getSlashViewGradeList(student._personalInfo._recordDiv, k, classview._subclasscd, j + 1);
                    for (final Map.Entry<String, String> yearStatus : view._yearStatusMap.entrySet()) {
                        final String year = yearStatus.getKey();
                        final String status = yearStatus.getValue();
                        if (!PersonalInfo.isTargetYear(student, personalInfo, year, param())) {
                            continue;
                        }
                        if (null != years && !years.contains(year)) {
                            continue;
                        }
                        final Gakuseki gakuseki = Gakuseki.getGakuseki(year, personalInfo._gakusekiList);
                        if (null != gakuseki && slashViewGradeList.contains(String.valueOf(gakuseki._gradeCd))) {
                            continue;
                        }
                        final int g = param().getGradeCd(year, student.getGrade(year));
                        row.put("ASSESS1_" + g, status);  //観点
                    }
                    for (int g = 1; g <= 6; g++) {
                        if (slashViewGradeList.contains(String.valueOf(g))) {
                            row.put("VIEW_SHASH" + String.valueOf(g), "／");  //観点スラッシュ
                        }
                    }
                }
                int nameline = viewList.size();

                if (KAITEI._2020 == k) {
                    final Map<String, String> row = new HashMap<String, String>();
                    recordList.add(row);

                    if (nameline * inc < printClassname.length()) {
                        row.put(field, printClassname.substring(nameline * inc, Math.min(printClassname.length(), (nameline + 1) * inc)));  //教科名称
                        nameline += 1;
                    }

                    row.put("CLASSCD1", keyCd);  //教科コード
                    row.put("VIEW1", "評定");

                    final int printValueMinGrade = 2;
                    final List<ValueRecord> valueRecordList = getValueRecordList(student, classview);
                    final List<String> slashValueNameMstGradeList = param().getSlashValueNameMstGradeList(student._personalInfo._recordDiv, k, classview._subclasscd);
                    for (final ValueRecord vr : valueRecordList) {
                        if (vr._g <= printValueMinGrade) {
                            continue;
                        }
                        if (!PersonalInfo.isTargetYear(student, personalInfo, vr._year, param())) {
                            //log.info(" skip print year = " + valueRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = ");
                            continue;
                        }
                        if (null != years && !years.contains(vr._year)) {
                            continue;
                        }
                        final boolean valueIsSlash = vr._g <= printValueMinGrade || slashValueNameMstGradeList.contains(String.valueOf(vr._g));
                        if (!valueIsSlash && vr._value != null) {
                            row.put("ASSESS1_" + vr._g, getPrintValue(vr));  //評定
                        }
                    }
                    for (int g = 1; g <= 6; g++) {
                        final boolean valueIsSlash = g <= printValueMinGrade || slashValueNameMstGradeList.contains(String.valueOf(g));
                        if (valueIsSlash) {
                            row.put("VIEW_SHASH" + g, "／");  //評定スラッシュ
                        }
                    }
                }

                if (null != printClassname) {
                    final List<Map<String, String>> rest = new ArrayList<Map<String, String>>();
                    if (nameline * inc < printClassname.length()) {
                        for (int i = nameline * inc; i < printClassname.length(); i += inc) {
                            final Map<String, String> row = new HashMap<String, String>();
                            rest.add(row);
                            row.put(field, printClassname.substring(i));
                        }
                    } else {
                        if (KAITEI._2020 != k && viewList.size() > 1) { // 評定出力なしで行数がオーバーしない場合、レコードを印刷
                            final Map<String, String> row = new HashMap<String, String>();
                            rest.add(row);
                            row.put(field, "");
                            row.put(SPACE_FLG, "");
                        }
                    }
                    for (int i = 0; i < rest.size(); i++) {
                        final Map<String, String> r = rest.get(i);
                        final Map<String, String> row = new HashMap<String, String>(r);
                        recordList.add(row);
                        final List<String> slashViewGradeList = param().getSlashViewGradeList(student._personalInfo._recordDiv, k, classview._subclasscd, nameline + i);
                        for (int g = 1; g <= 6; g++) {
                            if (slashViewGradeList.contains(String.valueOf(g))) {
                                row.put("VIEW_SHASH" + String.valueOf(g), "／");  //観点スラッシュ
                            }
                        }
                        row.put("CLASSCD1", keyCd);  //教科コード
                    }
                }
            }

            if ("1".equals(param()._seitoSidoYorokuNotPrintKantenBlankIfPageOver)) {
                if (VIEW_LINE1_MAX < recordList.size() && recordList.size() % VIEW_LINE1_MAX == 1) {
                    final Map<String, String> last = recordList.get(recordList.size() - 1);
                    if (last.containsKey(SPACE_FLG)) {
                        recordList.remove(last);
                    }
                } else if (VIEW_LINE1_MAX < recordList.size()) {
                    final List<Map<String, String>> spaces = new ArrayList<Map<String, String>>();
                    for (final Map<String, String> row : recordList) {
                        if (row.containsKey(SPACE_FLG)) {
                            spaces.add(row);
                        }
                    }
                    if (spaces.size() > 0) {
                        log.info(" recordList size " + recordList.size() + " > " + VIEW_LINE1_MAX + ", remove " + spaces.size() + " lines ");
                        recordList.removeAll(spaces);

                        Map<String, List<Map<String, String>>> classcd1Group = new HashMap<String, List<Map<String, String>>>();
                        for (final Map<String, String> row : recordList) {
                            getMappedList(classcd1Group, row.get("CLASSCD1")).add(row);
                        }
                        for (final Map.Entry<String, List<Map<String, String>>> e : classcd1Group.entrySet()) {
                            //final String classcd1 = e.getKey();
                            final List<Map<String, String>> sameClasscd1RowList = e.getValue();

                            final StringBuffer classname = new StringBuffer();
                            for (final Map<String, String> row : sameClasscd1RowList) {
                                String class2 = row.get("CLASS2");
                                String class1 = row.get("CLASS1");
                                if (!StringUtils.isBlank(class2)) {
                                    classname.append(class2);
                                    row.put("CLASS2", null);
                                } else if (!StringUtils.isBlank(class1)) {
                                    classname.append(class1);
                                    row.put("CLASS1", null);
                                }
                            }
                            int split = 1;
                            String field = "CLASS1";
                            if (classname.length() <= sameClasscd1RowList.size()) {
                                classname.insert(0, StringUtils.repeat(" ", (sameClasscd1RowList.size() - classname.length()) / 2));
                            } else if (sameClasscd1RowList.size() <= classname.length() / 2) {
                                split = 2;
                                field = "CLASS2";
                            }
                            for (int i = 0; i < classname.length(); i += 1) {
                                if (i < sameClasscd1RowList.size()) {
                                    final String s = classname.substring(i * split, Math.min(classname.length(), (i + 1) * split));
                                    sameClasscd1RowList.get(i).put(field, s);
                                }
                            }
                        }
                    }
                }
            }

            boolean nonedata = false;
            for (final List<Map<String, String>> recordPageList : Util.getPageList(recordList, VIEW_LINE1_MAX)) {
                printPersonalInfo(student._personalInfo);
                for (final Map<String, String> row : recordPageList) {
                    for (final Map.Entry<String, String> e : row.entrySet()) {
                        if (SPACE_FLG.equals(e.getKey())) {
                            continue;
                        }
                        vrsOut(e.getKey(), e.getValue());
                    }
                    _svf.VrEndRecord();
                }
                for (int i = recordPageList.size(); i < VIEW_LINE1_MAX; i++) {
                    vrsOut("CLASSCD1", "A");
                    _svf.VrEndRecord();
                }
                nonedata = true;
            }
            return nonedata;
        }

        /**
         * 同一のVIEWCDグループのリスト
         * @param classview
         * @param years
         * @return
         */
        private List<View> getViewList(final ClassView classview, final List<String> years) {
            final List<View> list = new ArrayList<View>();

            for (final String viewcd : classview._viewCdList) {
                final View view = classview._views.get(viewcd);
                boolean isPrint = false;
                for (final String year : view._yearStatusMap.keySet()) {
                    if (null == years || years.contains(year)) {
                        isPrint = true;
                        break;
                    }
                }
                if (isPrint) {
                    list.add(view);
                }
            }
            return list;
        }

        /**
         *  評定出力処理
         * @param student
         * @param personalInfo
         * @param years 表示対象年度 (nullの場合、全てが対象)
         */
        private void printHyotei3(final Student student, final PersonalInfo personalInfo, final List<String> years) {

            final List<ClassView> valuationViewClassList = hyoteiClassOrderedList(student, personalInfo, years);
            if (param()._isOutputDebugKanten) {
                log.info(" valuationViewClassList = " + valuationViewClassList);
            }
            for (int cli = 0; cli < valuationViewClassList.size(); cli += 1) {
                final ClassView classview = valuationViewClassList.get(cli);
                final int classline1 = cli + 1;
                vrsOutSelectField(Arrays.asList("CLASS" + classline1 + "_1",  "CLASS" + classline1 + "_2"), getClassShowname(student, classview));  //評定教科名

                final List<ValueRecord> valueRecordList = getValueRecordList(student, classview);
                for (final ValueRecord vr : valueRecordList) {
                    final int g = vr._g - 2; // 3年次以上を表示する
                    if (g <= 0) {
                        continue;
                    }
                    if (!PersonalInfo.isTargetYear(student, personalInfo, vr._year, param())) {
                        continue;
                    }
                    if (null != years && !years.contains(vr._year)) {
                        continue;
                    }
                    if (vr._value != null) {
                        vrsOutn("ASSESS" + classline1, g, getPrintValue(vr));  //評定
                    }
                }
                final List<String> slashValueNameMstGradeList = param().getSlashValueNameMstGradeList(student._personalInfo._recordDiv, null, classview._subclasscd);
                for (int gg = 3; gg <= 6; gg++) {
                    final boolean isSlash = slashValueNameMstGradeList.contains(String.valueOf(gg));
                    if (isSlash) {
                        vrsOutn("ASSESS_SHASH" + String.valueOf(classline1), gg - 2, "／");  //評定スラッシュ
                    }
                }
            }
        }

        private List<ClassView> hyoteiClassOrderedList(final Student student, final PersonalInfo personalInfo, final List<String> years) {
            final List<ClassView> valuationViewClassList = getValuationClass(student, personalInfo, years);
            if (param()._a046name1ClassList.size() > 0) {
                final Set<String> valueClassKeySet = new HashSet<String>();
                for (final ClassView classview : valuationViewClassList) {
                    valueClassKeySet.add(classview._classMst.getKey(param()));
                }

                final List<ClassMst> addClassList = new ArrayList<ClassMst>();
                for (final ClassMst cm : param()._a046name1ClassList) {
                    if (!valueClassKeySet.contains(cm.getKey(param()))) {
                        addClassList.add(cm);
                        valueClassKeySet.add(cm.getKey(param()));
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" addClassList = " + addClassList);
                }

                for (final ClassMst cm : addClassList) {
                    final ClassView newView = new ClassView(cm, "", "");
                    newView._isNameMstA046 = true;
                    valuationViewClassList.add(newView);
                }
                Collections.sort(valuationViewClassList, new ClassView.ClassViewComparator(param()));
            }
            return valuationViewClassList;
        }

        private List<ValueRecord> getValueRecordList(final Student student, final ClassView classview) {
            final List<ValueRecord> valueRecordList = new ArrayList<ValueRecord>();
            for (final ValueRecord vr : student._valueRecordList) {
                if (null != vr._classCd && vr._classCd.equals(classview._classMst.getKey(param()))) {
                    boolean isAdd = false;
                    if (param()._isSundaikoufu) {
                        try {
                            final String[] vSplit = StringUtils.split(vr._subclassCd, "-");
                            final String[] cvSplit = StringUtils.split(classview._subclasscd, "-");
                            if (vSplit[0].equals(cvSplit[0]) && vSplit[1].equals(cvSplit[1]) && vSplit[3].equals(cvSplit[3])) {
                                isAdd = true;
                            }
                        } catch (Exception e) {
                            log.error("exception!", e);
                        }
                    } else if (vr._subclassCd.equals(classview._subclasscd) || classview._isNameMstA046) {
                        isAdd = true;
                    }
                    if (isAdd) {
                        valueRecordList.add(vr);
                    }
                }
            }
            return valueRecordList;
        }

        private String getPrintValue(final ValueRecord valuer) {
            final String value;
            if ("1".equals(valuer._electDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                if ("11".equals(valuer._value)) {
                    value = "A";
                } else if ("22".equals(valuer._value)) {
                    value = "B";
                } else if ("33".equals(valuer._value)) {
                    value = "C";
                } else {
                    value = valuer._value;
                }
            } else {
                value = valuer._value;
            }
            return value;
        }

        private void printPersonalInfo(final PersonalInfo personalInfo) {
            if (null != personalInfo) {
                final int height = 50;
                final int minnum = 20;

                if (personalInfo._useRealName && personalInfo._nameOutputFlg && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                    printName(personalInfo._studentRealName,           getFieldInfo("NAME2", null, null, height, minnum));
                    printName("（" + personalInfo._studentName + "）", getFieldInfo("NAME3", null, null, height, minnum));
                } else {
                    final String name = personalInfo._useRealName ? personalInfo._studentRealName : personalInfo._studentName;
                    printName(name, getFieldInfo("NAME1", null, null, height, minnum), param()._isMusashinohigashi);
                }
            }
        }

        /**
         *  個人情報  学籍等履歴情報
         * @param student
         * @param personalInfo
         * @param years 表示対象年度 (nullの場合、全てが対象)
         */
        private void printGakuseki3(final Student student, final PersonalInfo personalInfo, final List<String> years) {
            //生徒名印刷
            printPersonalInfo(personalInfo);

            final SchoolInfo schoolInfo = param()._schoolInfo;
            if (null != schoolInfo) {
                final String schoolName1 = !StringUtils.isEmpty(schoolInfo._certifSchoolName) ? schoolInfo._certifSchoolName : schoolInfo._schoolName1;
                final String fieldForData = getFieldForData(Arrays.asList("SCHOOLNAME1", "SCHOOLNAME2"), schoolName1);
                vrsOut(fieldForData, schoolName1);
                if (param()._isMusashinohigashi) {
                    _svf.VrAttribute(fieldForData, "Hensyu=3"); // 中央割付
                }
            }

            //学籍履歴の取得および印刷
            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {

                if (!PersonalInfo.isTargetYear(student, personalInfo, gakuseki._year, param())) {
                    // log.debug(" skip print year = " + regdRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                if (null != years && !years.contains(gakuseki._year)) {
                    continue;
                }

                vrsOutnSelectField(Arrays.asList("HR_NAME", "HR_NAME_2", "HR_NAME_3_1"), gakuseki._pos, gakuseki._hrname);
                vrsOutn("ATTENDNO", gakuseki._pos, String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号
            }
        }

        /**
         *
         * @param student
         * @param personalInfo
         * @param years 表示対象年度 (nullの場合、全てが対象)
         */
        private void printShoken3(final Student student, final PersonalInfo personalInfo, final List<String> years, final KAITEI kaitei) {

            for (final HTrainremarkPdat remark : student._htrainremarkPdatList) {

                if (!PersonalInfo.isTargetYear(student, personalInfo, remark._year, param())) {
                    // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                if (null != years && !years.contains(remark._year)) {
                    continue;
                }

                // 道徳
                final boolean isPrintDoutoku = !(param()._isMusashinohigashi && "2".equals(student._personalInfo._recordDiv));
                if (isPrintDoutoku) {
                    KNJPropertiesShokenSize shokenSizeMoral = KNJPropertiesShokenSize.getShokenSize(notBlank(param()._HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P, param()._HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P), -1, -1);
                    if (shokenSizeMoral._gyo == -1 || shokenSizeMoral._mojisu == -1) {
                        shokenSizeMoral = shokenSizeFromField("MORAL1", 32, 2);
                    }
                    printSvfRepeat2("MORAL", remark._gradeCdInt, remark._foreignLangAct4, shokenSizeMoral);
                }

                {
                    // 総合的な学習の時間の記録
                    final int g;
                    if (param()._isMusashinohigashi) {
                        g = remark._gradeCdInt;
                    } else {
                        g = remark._gradeCdInt - 2; // 3年次以上を表示する。
                    }
                    if (g > 0) {
                        if (param()._isHibarigaoka) {
                            printSvfRenbanIdx("TOTAL_ACT", g, remark._totalstudyact, KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P, 5, 6));
                            printSvfRenbanIdx("TOTAL_VALUE", g, remark._totalstudyval, KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P, 27, 6));
                        } else {
                            printSvfRenbanIdx("TOTAL_ACT", g, remark._totalstudyact, KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P, 8, 8));
                            printSvfRenbanIdx("TOTAL_VIEW", g, remark._viewremark, KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P, 7, 8));
                            printSvfRenbanIdx("TOTAL_VALUE", g, remark._totalstudyval, KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P, 15, 8));
                        }
                    }
                }

                // 外国語活動
                final boolean isPrintGaikokugo = !param()._isMusashinohigashi;
                if (isPrintGaikokugo) {
                    if (KAITEI._2020 == kaitei) {
                        if (param()._isOsakashinnai) {
                            // 大阪信愛1学年、2学年を表示する。
                            if (remark._gradeCdInt == 1 || remark._gradeCdInt == 2) {
                                final int gg = remark._gradeCdInt;
                                printSvfRenban("FOREIGNVIEW_" + String.valueOf(gg), remark._foreignLangAct1, new KNJPropertiesShokenSize(32, 4));
                            }
                        } else {
                            // 3学年、4学年を表示する。
                            if (remark._gradeCdInt == 3 || remark._gradeCdInt == 4) {
                                final int gg = remark._gradeCdInt - 2;
                                printSvfRenban("FOREIGNVIEW_" + String.valueOf(gg), remark._foreignLangAct1, new KNJPropertiesShokenSize(32, 4));
                            }
                        }
                    } else {
                        final int gg = remark._gradeCdInt - 4; // 5学年以上を表示する。
                        if (gg > 0) {
                            printSvfRenbanIdx("FOREIGNVIEW1", gg, remark._foreignLangAct1, new KNJPropertiesShokenSize(10, 4));
                            printSvfRenbanIdx("FOREIGNVIEW2", gg, remark._foreignLangAct2, new KNJPropertiesShokenSize(10, 4));
                            printSvfRenbanIdx("FOREIGNVIEW3", gg, remark._foreignLangAct3, new KNJPropertiesShokenSize(10, 4));
                        }
                    }
                }
            }

            // 特別活動 所見
            final boolean isPrintTokubetsuKatsudou = !param()._isMusashinohigashi;
            if (isPrintTokubetsuKatsudou) {
                // 特別活動の記録
                final StringBuffer specialActRemark = new StringBuffer();
                String nl = "";
                for (final HTrainremarkPdat remark : student._htrainremarkPdatList) {

                    if (!PersonalInfo.isTargetYear(student, personalInfo, remark._year, param())) {
                        continue;
                    }

                    if (null != remark._specialActRemark) {
                        specialActRemark.append(nl).append(remark._specialActRemark);
                        nl = "\n";
                    }
                }
                final boolean preferFieldThanProp = param()._isKyoto;
                printSvfRenban("SPECIALACTVIEW", specialActRemark.toString(), new KNJPropertiesShokenSize(17, 10), preferFieldThanProp);
            }

            //特別活動の記録 斜線
            final List<SpecialActMst> specialActMstList = new ArrayList<SpecialActMst>(param()._specialActMstMap.values());
            Collections.sort(specialActMstList);
            for (int i = 0; i < specialActMstList.size(); i++) {
                final SpecialActMst specialActMst = specialActMstList.get(i);
                final int line = i + 1;
                vrsOut("SPECIALACT_NAME" + line, specialActMst._name);
                for (int g = 1; g <= 6; g++) { // 学年
                    if (specialActMst.isSlash(param(), student, kaitei, line, g)) {
                        vrsOutn("SPECIALACT_SHASH" + line, g, "／");
                    }
                }
            }

            //特別活動の記録
            for (final ActRecord act : student._actRecordList) {

                if (!PersonalInfo.isTargetYear(student, personalInfo, act._year, param())) {
                    // log.debug(" skip print year = " + actRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                if (null != years && !years.contains(act._year)) {
                    continue;
                }

                if ("1".equals(act._record)) {
                    if ("4".equals(act._div)){
                        for (int i = 0; i < specialActMstList.size(); i++) {
                            final SpecialActMst specialActMst = specialActMstList.get(i);
                            if (specialActMst._code.equals(act._code)) {
                                final int line = i + 1;
                                vrsOutn("SPECIALACT" + line, act._g, "○");
                            }
                        }
                    }
                }
            }
        }

        private void printFooter(final Student student, final List<String> years) {
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理]  生徒指導要録
     *   総合所見及び指導上参考となる事項
     */
    private static class KNJA133P_4 extends KNJA133P_0 {

        KNJA133P_4(final Param param, final Vrw32alp svf) {
            super(param, svf);
        }

        public int getPrintPage(final Student student) {
            return getPrintPageDefault(student, param());
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            boolean nonedata = false;

            String form0 = getForm();
            setForm(form0, 1);
            String form = modifyForm4(form0, student, personalInfo);
            if (!form.equals(form0)) {
                setForm(form, 1);
            }
            printPersonalInfo(personalInfo);  //学籍データ
            printShoken4(student, personalInfo);
            printFooter(student);
            _svf.VrEndPage();
            nonedata = true;
            return nonedata;
        }

        private String getForm() {
            final String form;
            if (param().isTokubetsuShien()) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    // 所見入力はKNJA128P
                    if (param()._isNaraken) {
                        form = "KNJA134P_14_7P.frm";
                    } else if (param()._isKyoto) {
                        form = "KNJA134P_14_KYOTO.frm";
                    } else {
                        form = "KNJA134P_14.frm";
                    }
                } else { // Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(param()._chiteki) {
                    // 所見入力はKNJA127P
                    if (param()._isNaraken) {
                        form = "KNJA134P_4_7P.frm";
                    } else if (param()._isKyoto) {
                        form = "KNJA134P_4_KYOTO.frm";
                    } else {
                        form = "KNJA134P_4.frm";
                    }
                }
            } else {
                if (param()._isKyoaiP) {
                    form = "KNJA133P_14KYOAI.frm";
                } else if (param()._isHibarigaoka) {
                    form = "KNJA133P_14HIBARI.frm";
                } else {
                    form = "KNJA133P_14.frm";
                }
            }
            return form;
        }

        private String FLG_JISHU_JIRITSU2 = "FLG_JISHU_JIRITSU2";
        private String modifyForm4(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if (param()._isNaraken && CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                flgMap.put(FLG_JISHU_JIRITSU2, "1");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_JISHU_JIRITSU2)) {
                for (final SvfForm.KoteiMoji moji : svfForm.getElementList(SvfForm.KoteiMoji.class)) {
                    if ("\"自主・自立\"".equals(moji._moji)) {
                        svfForm.move(moji, moji.replaceMojiWith("\"自主・自律\""));
                    }
                }
            }
            return true;
        }

        private void printFooter(final Student student) {

            printWords(student);
        }

        private void printShoken4(final Student student, final PersonalInfo personalInfo) {
            if (param().isTokubetsuShien()) {
                final KNJPropertiesShokenSize shogainoJoutaiSize;
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    // 行動の記録・特別活動の記録
                    printAct(student, personalInfo);

                    shogainoJoutaiSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P, 9, 15);
                } else {
                    shogainoJoutaiSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P_disability, 9, 18);
                }
                // 入学時の障害の状態
                if (null != student._htrainremarkPHdat._detail2HdatSeq001Remark1) {
                    printSvfRenban("FIELD1", student._htrainremarkPHdat._detail2HdatSeq001Remark1, shogainoJoutaiSize);
                }

                KNJPropertiesShokenSize indepSize;
                KNJPropertiesShokenSize totalremarkSize;
                KNJPropertiesShokenSize attendrecSize;
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    indepSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_P, 22, 7);
                    totalremarkSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_TOTALREMARK_SIZE_P, 28, 8);
                    attendrecSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P, 37, 1);
                } else {
                    indepSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_P_disability, 20, 7);
                    totalremarkSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_TOTALREMARK_SIZE_P_disability, 25, 9);
                    attendrecSize = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P_disability, 37, 1);
                }
                for (final HTrainremarkPdat remark : student._htrainremarkPdatList) {

                    if (!PersonalInfo.isTargetYear(student, personalInfo, remark._year, param())) {
                        // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }

                    if (Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(param()._chiteki)) {
                        KNJPropertiesShokenSize shokenSizeMoral = KNJPropertiesShokenSize.getShokenSize(param()._HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P_disability, -1, -1);
                        if (shokenSizeMoral._gyo == -1 || shokenSizeMoral._mojisu == -1) {
                            shokenSizeMoral = shokenSizeFromField("MORAL1", 32, 3);
                        }
                        printSvfRepeat("MORAL" + remark._gradeCdInt, getTokenList(remark._foreignLangAct4, shokenSizeMoral.getKeta(), shokenSizeMoral._gyo, param()));
                    }

                    final String indep;
                    if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                        // 自立活動の記録
                        indep = remark._detail2DatSeq001Remark1;
                    } else { // Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(param()._chiteki) {
                        // 行動の記録
                        indep = remark._detail2DatSeq002Remark1;
                    }
                    if (null != indep) {
                        printSvfRenban("INDEPENDENCE_REMARK" + remark._gradeCdInt, indep, indepSize);
                    }

                    // 総合所見
                    if (null != remark._totalRemark) {
                        printSvfRenban("TOTALREMARK" + remark._gradeCdInt, remark._totalRemark, totalremarkSize);
                    }

                    // 出欠の記録備考
                    if (remark._attendrecRemark != null) {
                        final List<String> list = getTokenList(remark._attendrecRemark, attendrecSize.getKeta(), attendrecSize._gyo, param());
                        for (int i = 0 ; i < list.size(); i++) {
                            final String n = i == 0 ? "" : String.valueOf(i + 1);
                            vrsOutn("REMARK" + n, remark._gradeCdInt, list.get(i)); //出欠の記録備考
                        }
                    }
                }

                printAttend(student, personalInfo);
            } else {
                printAct(student, personalInfo);

                final KNJPropertiesShokenSize totalRemarkSize = KNJPropertiesShokenSize.getShokenSize(notBlank(param()._HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P, param()._HTRAINREMARK_DAT_TOTALREMARK_SIZE_P), 22, 15);

                // 総合的な学習の時間の記録
                for (final HTrainremarkPdat remark : student._htrainremarkPdatList) {

                    if (!PersonalInfo.isTargetYear(student, personalInfo, remark._year, param())) {
                        // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }

                    printSvfRenban("TOTALREMARK" + remark._gradeCdInt, remark._totalRemark, totalRemarkSize); //総合所見
                }
                Map<Integer, String> attendRemarkMap = new HashMap();
                for (final HTrainremarkPdat remark : student._htrainremarkPdatList) {

                    if (!PersonalInfo.isTargetYear(student, personalInfo, remark._year, param())) {
                        continue;
                    }

                    attendRemarkMap.put(new Integer(remark._gradeCdInt), remark._attendrecRemark);
                }
                if (param()._isMusashinohigashi) {
                    for (int i = 0; i < 6; i++) {
                        if (null == attendRemarkMap.get(new Integer(i + 1))) {
                            attendRemarkMap.put(new Integer(i + 1), null);
                        }
                    }

                    for (final Integer g : attendRemarkMap.keySet()) {
                        final StringBuffer stb = new StringBuffer();

                        String late = null;
                        String early = null;
                        String suspend = null;
                        String mourning = null;
                        // 出欠の記録
                        for (final Attend a : student._attendList) {
                            if (a._g == g.intValue()) {
                                late = a._late;
                                early = a._early;
                                suspend = a._suspend;
                                mourning = a._mourning;
                            }
                        }
                        stb.append("遅刻" + keta(late, 2) + " ");
                        stb.append("早退" + keta(early, 2) +" ");
                        stb.append("忌引" + keta(mourning, 2) + " ");
                        stb.append("出停" + keta(suspend, 2) + " ");
                        stb.append(StringUtils.defaultString((String) attendRemarkMap.get(g)));
                        attendRemarkMap.put(g, stb.toString());
                    }
                }

                for (final Map.Entry<Integer, String> e : attendRemarkMap.entrySet()) {
                    final int g = e.getKey();
                    final String attendrecRemark = e.getValue();
                    if (attendrecRemark != null) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(notBlank(param()._HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P, param()._HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P), 35, 2);

                        final List<String> list = getTokenList(attendrecRemark, size.getKeta(), size._gyo, param());
                        if (null != list) {
                            for (int i = 0 ; i < list.size(); i++) {
                                final String si = i == 0 ? "" : String.valueOf(i + 1);
                                vrsOutn("REMARK" + si, g, list.get(i)); //出欠の記録備考
                            }
                        }
                    }
                }

                printAttend(student, personalInfo);
            }
        }

        private static String keta(String intString, final int keta) {
            if (NumberUtils.isDigits(intString)) {
                intString = String.valueOf(Integer.parseInt(intString));
            } else {
                intString = "";
            }
            return StringUtils.repeat(" ", 2 - intString.length()) + intString;
        }

        private void printAct(final Student student, final PersonalInfo personalInfo) {
            if (null != getSvfField("ACTION_NAME1")) {
                final DecimalFormat df = new DecimalFormat("00");
                for (int cdi = 0; cdi < 10; cdi++) {
                    final String cd = df.format(cdi + 1);
                    vrsOut("ACTION_NAME" + String.valueOf(cdi + 1), param()._d035namecd2Name1Map.get(cd)); // 行動の記録名称
                }
            }

            for (final ActRecord act : student._actRecordList) {
                if (!PersonalInfo.isTargetYear(student, personalInfo, act._year, param())) {
                    continue;
                }

                if ("1".equals(act._record)) {
                    if ("3".equals(act._div)) {
                        vrsOutn("ACTION" + act._code, act._g, "○"); //行動の記録
                    }
                }
            }
        }

        private void printAttend(final Student student, final PersonalInfo personalInfo) {
            // 出欠の記録
            for (final Attend attend : student._attendList) {

                if (!PersonalInfo.isTargetYear(student, personalInfo, attend._year, param())) {
                    continue;
                }

                vrsOutn("LESSON", attend._g, attend._lesson);           //授業日数
                vrsOutn("SUSPEND", attend._g, attend._suspendMourning);  //出停・忌引
                vrsOutn("PRESENT", attend._g, attend._requirePresent);   //要出席
                vrsOutn("ATTEND", attend._g, attend._present);          //出席
                vrsOutn("ABSENCE", attend._g, attend._absent);           //欠席
            }

        }

        private void printWords(final Student student) {
            if (null == param()._mongon) {
                return;
            }
            final String words1 = param()._schoolInfo._remark2;
            // 平成24年月 3月22日　　益城中央小学校長　○○　○○
            final String[] tf = KNJ_EditDate.tate_format(param()._ctrlDateHFormatJp);
            final String y = (Integer.parseInt(tf[1]) >= 10 ? "" : " ") + tf[1];
            final String m = (Integer.parseInt(tf[2]) >= 10 ? "" : " ") + tf[2];
            final String d = (Integer.parseInt(tf[3]) >= 10 ? "" : " ") + tf[3];
            final String dateString = tf[0] + y + "年" + m + "月" + d + "日　　";
            final String remark3 = param()._schoolInfo._remark3;
            final String words2 = (StringUtils.isBlank(words1) ? "" : dateString) + (StringUtils.isBlank(remark3) ? "" : remark3);
            vrsOut("WORDS1", words1);
            _svf.VrAttribute("WORDS1", "Hensyu=3");
            vrsOut("WORDS2", words2);
            _svf.VrAttribute("WORDS2", "Hensyu=3");
        }

        private void printPersonalInfo(final PersonalInfo personalInfo) {
            if (null != personalInfo) {
                final int height = 50;
                final int minnum = 20;

                if (personalInfo._useRealName && personalInfo._nameOutputFlg && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                    printName(personalInfo._studentRealName,           getFieldInfo("NAME2", null, null, height, minnum));
                    printName("（" + personalInfo._studentName + "）", getFieldInfo("NAME3", null, null, height, minnum));
                } else {
                    if (null != getSvfField("NAME1")) {
                        final String name = personalInfo._useRealName ? personalInfo._studentRealName : personalInfo._studentName;
                        printName(name, getFieldInfo("NAME1", null, null, height, minnum), param()._isMusashinohigashi);
                    }
                }
            }
        }

    }

    /**
     * 学習の記録　知的障害者用 文言評価
     */
    private static class KNJA134P_3 extends KNJA133P_0 {

        final String[] monkasyoKyoka = {"生活", "国語", "算数", "音楽", "図画工作", "体育", "特別活動", "自立活動"};

        KNJA134P_3(final Param param, final Vrw32alp svf) {
            super(param, svf);
        }

        // @Override
        public int getPrintPage(final Student student) {
            int page = 0;
            page = student._printEntGrdHistList.size();
            return page;
        }

        public boolean printSvf(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            boolean nonedata = false;
            final String form0;
            int n = 4;
            if (param()._isFukuiken) {
                form0 = "KNJA134P_3FUKUI.frm";
            } else if (param()._isNaraken) {
                form0 = "KNJA134P_3_NARAKEN.frm";
                n = 1;
            } else if (param()._isKyoto) {
                form0 = "KNJA134P_3_KYOTO.frm";
                n = 1;
            } else {
                form0 = "KNJA134P_3.frm";
            }
            setForm(form0, n);
            final String form = modifyForm4(form0, student, personalInfo);
            if (!form.equals(form0)) {
                setForm(form, n);
            }
            printGakuseki3(student, personalInfo);
            final boolean notOutputOld = _notOutput;
            _notOutput = false;
            for (int i = 0; i < monkasyoKyoka.length; i++) {
                vrsOut("CLASS" + String.valueOf(i + 1), monkasyoKyoka[i]);
            }
            _notOutput = notOutputOld;
            if (param()._isFukuiken) {
                printRecordFukuiken(student);
            } else if (param()._isNaraken || param()._isKyoto) {
                printRecordNarakenKyoto(student);
            } else {
                printRecord(student);
            }
            nonedata = true;
            return nonedata;
        }

        private String FLG_ADD_LINE = "FLG_ADD_LINE";
        private String modifyForm4(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if (param()._isNaraken) { // 京都府は罫線なし
                flgMap.put(FLG_ADD_LINE, "1");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            boolean modified = false;
            if (flgMap.containsKey(FLG_ADD_LINE)) {
                final SvfForm.Field fieldTOTAL_RECORD6 = svfForm.getField("TOTAL_RECORD6");
                final SvfForm.Line rightLine = svfForm.getNearestRightLine(fieldTOTAL_RECORD6.getPoint());
                for (int i = 1; i < monkasyoKyoka.length; i++) {
                    final SvfForm.Field field = svfForm.getField("CLASS" + String.valueOf(i + 1));
                    final SvfForm.Line upperLine = svfForm.getNearestUpperLine(field.getPoint());

                    final SvfForm.Line line = new SvfForm.Line(true, SvfForm.LineKind.DOTTED2, SvfForm.LineWidth.THINEST, upperLine._end, upperLine._end.setX(rightLine._end._x));
                    // log.info(" line = " + line);
                    svfForm.addLine(line);
                }
                modified = true;
            }
            return modified;
        }

        private void printRecordNarakenKyoto(final Student student) {

            final String[] classcd30seikatsu = {"30"}; // 視覚障害用は3～6年は理科12と社会14
            final String[] classcd31kokugo = {"31"};
            final String[] classcd32sansu = {"32"};
            final String[] classcd35ongaku = {"35"};
            final String[] classcd36zugakousaku = {"36"};
            final String[] classcd37taiiku = {"37"};
            final String[] classcd93tokkatsu = param()._isKyoto ? new String[] {"91"} : new String[] {"93"};
            final String[] classcd94jiritsukatsudo = param()._isKyoto ? new String[] {"92"} : new String[] {"94"};
            final String[][] classcds = {classcd30seikatsu, classcd31kokugo, classcd32sansu,
                    classcd35ongaku, classcd36zugakousaku, classcd37taiiku,
                    classcd93tokkatsu, classcd94jiritsukatsudo
            };

            final int mojisu;
            final int maxLine;
            if (param()._isKyoto) {
                // 京都府
                mojisu = 9;
                maxLine = 8;
            } else {
                // 奈良県
                mojisu = 12;
                maxLine = 8;
            }
            final Map<Integer, Map<String, List<String>>> gradeCdClassRemarkListMap = new HashMap<Integer, Map<String, List<String>>>();
            for (final String gradeCd : student._gradecdClassRemarkListMap.keySet()) {
                if (!NumberUtils.isDigits(gradeCd) || Integer.parseInt(gradeCd) <= 0 || Integer.parseInt(gradeCd) > 6) {
                    continue;
                }
                final Integer iGradeCd = Integer.valueOf(gradeCd);

                final Map<String, List<String>> classLinesMap = getMappedMap(gradeCdClassRemarkListMap, iGradeCd);

                final List<ClassRemark> classRemarkList = getMappedList(student._gradecdClassRemarkListMap, gradeCd);
                for (int i = 0; i < classRemarkList.size(); i++) {
                    final ClassRemark classRemark = classRemarkList.get(i);

                    final List<String> tokenList = getTokenList(classRemark._remark, mojisu * 2, maxLine, param());

                    getMappedList(classLinesMap, classRemark._classcd).addAll(tokenList);
                }
            }

            for (int iGradeCd = 1; iGradeCd <= student.currentGradeCd(param()); iGradeCd++) {
                final Map<String, List<String>> classLinesMap = getMappedMap(gradeCdClassRemarkListMap, iGradeCd);

                for (int i = 0; i < classcds.length; i++) {

                    final List<String> lines = new ArrayList<String>();
                    for (int j = 0; j < classcds[i].length; j++) {

                        if (CHITEKI3_知的障害_視覚障害等.equals(param()._chiteki) && ArrayUtils.contains(classcd30seikatsu, classcds[i][j]) && 3 <= iGradeCd && iGradeCd <= 6) {
                            for (final String otherClasscd : Arrays.asList("14", "12")) { // 理科と社会を4行ずつ
                                List<String> otherClassLines = getMappedList(classLinesMap, otherClasscd);
                                if (otherClassLines.size() > maxLine / 2) {
                                    otherClassLines = otherClassLines.subList(0, maxLine / 2); // 4行でカット
                                } else if (otherClassLines.size() < maxLine / 2) {
                                    for (int li = otherClassLines.size(); li < maxLine / 2; li++) {
                                        otherClassLines.add(""); // 4行にそろえる
                                    }
                                }
                                lines.addAll(otherClassLines);
                            }
                        } else {
                            lines.addAll(getMappedList(classLinesMap, classcds[i][j]));
                        }
                    }
                    for (int l = 0; l < Math.min(lines.size(), maxLine); l++) {
                        vrsOutn("TOTAL_RECORD" + String.valueOf(iGradeCd), i * maxLine + l + 1, lines.get(l));
                    }
                }
            }
            _svf.VrEndPage();
        }

        private void printRecord(final Student student) {
            final String blankGroup = "--";

            final Map<String, List<PrintLine>> gradeCdLineListMap = new HashMap<String, List<PrintLine>>();
            for (final String gradeCd : student._gradecdClassRemarkListMap.keySet()) {
                if (!NumberUtils.isDigits(gradeCd) || Integer.parseInt(gradeCd) <= 0 || Integer.parseInt(gradeCd) > 6) {
                    continue;
                }
                final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, gradeCd);

                final List<ClassRemark> classRemarkList = getMappedList(student._gradecdClassRemarkListMap, gradeCd);
                for (int i = 0; i < classRemarkList.size(); i++) {
                    final ClassRemark classRemark = classRemarkList.get(i);

                    final String group = gradeCdStr + (i % 2 == 1 ? "1" : "0");

                    final String remark = "（" + StringUtils.defaultString(classRemark._classname) + "）" + "\n" + classRemark._remark;

                    final List<String> tokenList = getTokenList(remark, 18, param());
                    for (final String token : tokenList) {
                        lineList.add(PrintLine.createLine(group, token));
                    }
                }
            }

            final int maxLine = 60;
            for (int li = 0; li < maxLine; li++) {
                boolean hasOutput = false;
                for (final String gradeCd : gradeCdLineListMap.keySet()) {
                    final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                    final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, gradeCd);

                    if (li < lineList.size()) {
                        final PrintLine printLine = lineList.get(li);
                        vrsOut("GRP" + gradeCdStr, printLine._group);
                        vrsOut("TOTAL_RECORD" + gradeCdStr, printLine._data);
                    } else {
                        vrsOut("GRP" + gradeCdStr, blankGroup);
                    }
                    hasOutput = true;
                }
                if (!hasOutput) {
                    vrsOut("GRP1", blankGroup);
                }
                _svf.VrEndRecord();
            }
        }

        private Map<String, String> propMap(final String[] keyValues) {
            final Map<String, String> m = new HashMap<String, String>();
            for (int i = 0; i < keyValues.length; i+= 2) {
                m.put(keyValues[i], keyValues[i + 1]);
            }
            return m;
        }

        private void printRecordFukuiken(final Student student) {
            final String blankGroup = "--";

            final Map<String, List<PrintLine>> gradeCdLineListMap = new TreeMap();
            getMappedList(gradeCdLineListMap, "01");
            getMappedList(gradeCdLineListMap, "02");
            getMappedList(gradeCdLineListMap, "03");
            getMappedList(gradeCdLineListMap, "04");
            getMappedList(gradeCdLineListMap, "05");
            getMappedList(gradeCdLineListMap, "06");

            final List<Map<String, String>> fieldList = new ArrayList<Map<String, String>>();
            fieldList.add(propMap(new String[] {"fieldNo", "1", "Keta", "18", "maxLine", "76"}));
            fieldList.add(propMap(new String[] {"fieldNo", "2", "Keta", "20", "maxLine", "90"}));
            fieldList.add(propMap(new String[] {"fieldNo", "3", "Keta", "24", "maxLine", "107"}));

            for (final String gradeCd : gradeCdLineListMap.keySet()) {
                final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, gradeCd);

                final List<ClassRemark> classRemarkList = getMappedList(student._gradecdClassRemarkListMap, gradeCd);
                Map<String, String> fieldMap = null;
                for (int fi = 0; fi < fieldList.size(); fi++) {
                    fieldMap = fieldList.get(fi);

                    final int keta = Integer.parseInt(fieldMap.get("Keta"));
                    final int maxLine = Integer.parseInt(fieldMap.get("maxLine"));

                    for (int i = 0; i < classRemarkList.size(); i++) {
                        final ClassRemark classRemark = classRemarkList.get(i);

                        final String group = gradeCdStr + (i % 2 == 1 ? "1" : "0");

                        final String classname = StringUtils.defaultString(classRemark._classname);

                        final List<String> tokenList = getTokenList(classRemark._remark, keta, param());

                        for (int j = 0; j < Math.max(classname.length(), tokenList.size()); j++) {
                            String classname1 = null;
                            String line = null;
                            if (j < classname.length()) {
                                classname1 = classname.substring(j, j + 1);
                            }
                            if (j < tokenList.size()) {
                                line = tokenList.get(j);
                            }
                            lineList.add(PrintLine.createLineWithClassname(group, line, classname1));
                        }
                    }
                    if (maxLine < lineList.size() && fi < fieldList.size() - 1) {
                        // 収まらなければ次のfieldMapで再度実行
                        lineList.clear();
                        continue;
                    } else {
                        break;
                    }
                }

                final String fieldNo = fieldMap.get("fieldNo");
                final int maxLine = Integer.parseInt(fieldMap.get("maxLine"));
                log.info(" gradeCd " + gradeCd + " fieldMap = " + fieldMap);
                for (int li = 0; li < maxLine; li++) {

                    if (li < lineList.size()) {
                        final PrintLine printLine = lineList.get(li);
                        vrsOut("GRP" + fieldNo + "_CLASS", printLine._group);
                        vrsOut("CLASSNAME" + fieldNo, printLine._classname);
                        vrsOut("GRP" + fieldNo, printLine._group);
                        vrsOut("TOTAL_RECORD" + fieldNo, printLine._data);
                    } else {
                        vrsOut("GRP" + fieldNo, blankGroup);
                    }
                    _svf.VrEndRecord();
                }
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param student
         */
        private void printGakuseki3(final Student student, final PersonalInfo personalInfo) {

            printName(personalInfo);

            vrsOutSelectField(Arrays.asList("SCHOOLNAME1", "SCHOOLNAME2"), param()._schoolInfo._schoolName1);

            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                // ホームルーム
                vrsOutnSelectField(Arrays.asList("HR_NAME", "HR_NAME_2", "HR_NAME_3_1"), gakuseki._pos, gakuseki._hrname);
                vrsOutn("ATTENDNO", gakuseki._pos, String.valueOf(Integer.parseInt(gakuseki._attendno)));
            }
        }

        private void printName(final PersonalInfo personalInfo) {
            final int height = 50;
            final int minnum = 20;

            if (personalInfo._useRealName && personalInfo._nameOutputFlg && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                printName(personalInfo._studentRealName,           getFieldInfo("NAME2", null, null, height, minnum));
                printName("（" + personalInfo._studentName + "）", getFieldInfo("NAME3", null, null, height, minnum));
            } else {
                final String name = personalInfo._useRealName ? personalInfo._studentRealName : personalInfo._studentName;
                printName(name, getFieldInfo("NAME1", null, null, height, minnum));
            }
        }

        private class PrintGakuseki {
            final List<String> _yearList = new ArrayList();
            final Map _gakusekiMap = new HashMap();
            final Map _grademap = new HashMap();
        }

        private static class PrintLine {
            String _group;
            String _data;

            String _classname;
            boolean _useClassname2;
            String _classname2_1;
            String _classname2_2;

            static PrintLine createLine(final String group, final String line) {
                final PrintLine printLine = new PrintLine();
                printLine._group = group;
                printLine._data = line;
                return printLine;
            }

            static PrintLine createLineWithClassname(final String group, final String line, final String classname) {
                final PrintLine printLine = new PrintLine();
                printLine._group = group;
                printLine._data = line;
                printLine._classname = classname;
                return printLine;
            }

            static PrintLine createLineWithClassname2(final String group, final String line, final String classname2_1, final String classname2_2) {
                final PrintLine printLine = new PrintLine();
                printLine._group = group;
                printLine._data = line;
                printLine._useClassname2 = true;
                printLine._classname2_1 = classname2_1;
                printLine._classname2_2 = classname2_2;
                return printLine;
            }
        }

        private static class ClassRemark {
            final String _classcd;
            final String _schoolKind;
            final String _classname;
            final String _remark;
            public ClassRemark(final String classcd, final String schoolKind, final String classname, final String remark) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _classname = classname;
                _remark = remark;
            }

            private static Map<String, List<ClassRemark>> loadGradecdClassRemarkListMap(final DB2UDB db2, final Param param, final Student student) {
                final Map<String, List<ClassRemark>> gradeCdClassRemarkListMap = new HashMap();
                if (!(param.isTokubetsuShien() && Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(param._chiteki))) {
                    return gradeCdClassRemarkListMap;
                }
                try {
                    final String psKey = "CHITEKI_MONGON_HYOKA";
                    if (null == param.getPs(psKey)) {

                        final StringBuffer stb = new StringBuffer();
                        stb.append(" SELECT ");
                        stb.append("   T1.SCHOOLCD, ");
                        stb.append("   T1.YEAR, ");
                        stb.append("   T1.SCHREGNO, ");
                        stb.append("   T2.GRADE_CD, ");
                        stb.append("   T1.CLASSCD, ");
                        stb.append("   T1.SCHOOL_KIND, ");
                        stb.append("   CM.CLASSNAME, ");
                        stb.append("   T1.CURRICULUM_CD, ");
                        stb.append("   T1.SUBCLASSCD, ");
                        stb.append("   T1.REMARK1 ");
                        stb.append(" FROM SCHREG_STUDYREC_DETAIL_DAT T1 ");
                        stb.append(" INNER JOIN (SELECT L1.SCHREGNO, L1.YEAR, MAX(L2.GRADE_CD) AS GRADE_CD ");
                        stb.append("             FROM SCHREG_REGD_DAT L1 ");
                        stb.append("             INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = L1.YEAR AND L2.GRADE = L1.GRADE ");
                        stb.append("                                     AND L2.SCHOOL_KIND = ? ");
                        stb.append("             GROUP BY L1.SCHREGNO, L1.YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                        stb.append("     AND T2.YEAR = T1.YEAR ");
                        stb.append(" INNER JOIN CLASS_MST CM ON CM.CLASSCD = T1.CLASSCD ");
                        stb.append("     AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append(" LEFT JOIN SUBCLASS_MST SCM2 ON SCM2.CLASSCD = T1.CLASSCD ");
                        stb.append("     AND SCM2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append("     AND SCM2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                        stb.append("     AND SCM2.SUBCLASSCD2 = T1.SUBCLASSCD ");
                        stb.append(" INNER JOIN SUBCLASS_MST SCM ON SCM.CLASSCD = T1.CLASSCD ");
                        stb.append("     AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append("     AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
                        stb.append("     AND SCM.SUBCLASSCD = VALUE(SCM.SUBCLASSCD2, T1.SUBCLASSCD) ");
                        stb.append(" WHERE ");
                        stb.append("   T1.SEQ = '001' ");
                        stb.append("   AND T1.SCHREGNO = ? ");
                        stb.append(" ORDER BY ");
                        stb.append("   T2.GRADE_CD ");
                        stb.append("   , VALUE(CM.SHOWORDER, -1) "); // 表示順教科
                        stb.append("   , CM.CLASSCD");
                        stb.append("   , CM.SCHOOL_KIND");
                        stb.append("   , VALUE(SCM.SHOWORDER, -1) "); // 表示順科目
                        stb.append("   , SCM.CURRICULUM_CD");
                        stb.append("   , SCM.SUBCLASSCD");

                        param.setPs(psKey, db2, stb.toString());
                    }

                    for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { student._schoolKind, student._schregno})) {

                        final ClassRemark classRemark = new ClassRemark(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "REMARK1"));

                        getMappedList(gradeCdClassRemarkListMap, KnjDbUtils.getString(row, "GRADE_CD")).add(classRemark);
                    }

                } catch (Exception e) {
                    log.error("exception!", e);
                }
                return gradeCdClassRemarkListMap;
            }

            public String toString() {
                return "ClassRemark(" + _classcd + "-" + _schoolKind + ":" + _classname + ", " + _remark + ")";
            }
        }
    }

    /**
     * 特例の授業等の記録
     */
    private static class KNJA129Delegate extends KNJA133P_0 {

        KNJA129 _knja129;
        KNJA129.Param _knja129param;
        boolean hasdata = true;

        KNJA129Delegate(final Param param, final Vrw32alp svf, final HttpServletRequest request, final DB2UDB db2) {
            super(param, svf);
            _knja129 = new KNJA129();
            _knja129param = _knja129.getParam(request, db2, param._year, param._gakki, Param.SCHOOL_KIND);
        }

        public int getPrintPage(final Student student) {
            return getPrintPageDefault(student, param());
        }

        public boolean printSvf(final DB2UDB db2, final Student student, final PersonalInfo pInfo) {
            setDetail4(db2, pInfo, null, null);
            return hasdata;
        }

        private void setDetail4(final DB2UDB db2, final PersonalInfo pInfo, final List<Gakuseki> pageGakusekiList, final List<List<String>> csvLines) {
            printPage4(db2, pInfo._student, pInfo, pageGakusekiList, csvLines);
        }

        private void printPage4(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<Gakuseki> pageGakusekiList, final List<List<String>> csvLines) {
            final ArrayList<String> years = new ArrayList<String>(Student.gakusekiYearSet(pInfo._gakusekiList));
            _knja129.printSvf(_knja129param, db2, _svf, pInfo._student._schregno, getNameLines(pInfo), Param.SCHOOL_KIND, years, csvLines);
            if (_knja129.hasData()) {
                hasdata = true;
            }
        }

        public void close() {
            _knja129param.close();
        }
    }

    private static class Semester {
        static final Semester Null = new Semester(null, null, null, null);
        final String _year;
        final String _semester;
        final String _sdate;
        final String _edate;
        Semester(final String year, final String semester, final String sdate, final String edate) {
            _year = year;
            _semester = semester;
            _sdate = sdate;
            _edate = edate;
        }
        public String key() {
            return key(_year, _semester);
        }
        public static String key(final String year, final String semester) {
            return year + "-" + semester;
        }
        public static Semester get(final Param param, final String year, final String semester) {
            final Semester seme = param._semesterMap.get(key(year, semester));
            if (null == seme) {
                return Null;
            }
            return seme;
        }
    }

    private static class SchregRegdDat {
        private String _year;
        private String _semester;
        private String _grade;
        private String _hrClass;
        private String _coursecd;
        private String _majorcd;
        private String _coursecode;

        private SchregRegdDat() {}

        private static Map<String, List<SchregRegdDat>> getYearRegdListMap(final Collection<SchregRegdDat> regdList) {
            final Map<String, List<SchregRegdDat>> rtn = new TreeMap<String, List<SchregRegdDat>>();
            for (final SchregRegdDat regd : regdList) {
                getMappedList(rtn, regd._year).add(regd);
            }
            return rtn;
        }

        private static SchregRegdDat getMaxSemesterRegd(final Collection<SchregRegdDat> regdList, final String year) {
            if (null == regdList || regdList.size() == 0 || null == year) {
                return null;
            }
            final Map<String, TreeMap<String, SchregRegdDat>> m = new TreeMap();
            for (final SchregRegdDat regd : regdList) {
                if (null != regd._year && null != regd._semester) {
                    getMappedTreeMap(m, regd._year).put(regd._semester, regd);
                }
            }
            final TreeMap<String, SchregRegdDat> yearMap = getMappedTreeMap(m, year);
            if (yearMap.isEmpty()) {
                return null;
            }
            return yearMap.get(yearMap.lastKey());
        }

        public String toString() {
            return "Regd(year=" + _year + ", semester=" + _semester + ", grade=" + _grade + ", hrClass=" + _hrClass +", coursecd=" + _coursecd + ", majorcd=" + _majorcd + ", coursecode=" + _coursecode + ")";
        }
    }

    private static class KNJSvfFieldModify {

        int _x1;   //開始位置X(ドット)
        int _x2;   //終了位置X(ドット)
        int _height;  //フィールドの高さ(ドット)
        int _ystart;  //開始位置Y(ドット)
        int _ystart1;  //開始位置Y(ドット)フィールド1
        int _ystart2;  //開始位置Y(ドット)フィールド2
        int _minnum;  //最小設定文字数
        int _maxnum;  //最大設定文字数
        String _field;
        String _field1;
        String _field2;
        SvfField _field1Status = null;
        SvfField _field2Status = null;

        /**
         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
         * @param posx1 フィールドの左端X
         * @param posx2 フィールドの右端X
         * @param num フィールド指定の文字数
         * @param charSize 変更後の文字サイズ
         * @return ずれ幅の値
         */
        public static int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, double charSize) {
            final int maxWidth = getStringLengthPixel(charSize, num); // 文字の大きさを考慮したフィールドの最大幅
            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2 + 10;
            return offset;
        }

        private static int getStringLengthPixel(final double charSize, final int num) {
            return charSizeToPixel(charSize) * num / 2;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public static double getCharSize(final String str, final KNJSvfFieldModify fieldData) {
            return Math.min(pixelToCharSize(fieldData._height), retFieldPoint(fieldData._x2 - fieldData._x1, getStringByteSize(str, fieldData))); //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private static int getStringByteSize(final String str, final KNJSvfFieldModify fieldData) {
            return Math.min(Math.max(KNJ_EditEdit.getMS932ByteLength(str), fieldData._minnum), fieldData._maxnum);
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charSize 文字サイズ
         * @return 文字サイズをピクセル(ドット)に変換した値
         */
        public static int charSizeToPixel(final double charSize) {
            return (int) Math.round(charSize / 72 * 400);
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharSize(final double pixel) {
            return pixel / 400 * 72;
        }

        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public static long getYjiku(final int hnum, final double charSize, final KNJSvfFieldModify fieldData) {
            long jiku = 0;
            try {
                jiku = retFieldY(fieldData._height, charSize) + fieldData._ystart + fieldData._height * hnum;  //出力位置＋Ｙ軸の移動幅
            } catch (Exception ex) {
                log.error("getYjiku error! jiku = " + jiku, ex);
            }
            return jiku;
        }

        /**
         *  文字サイズを設定
         */
        private static double retFieldPoint(final int width, final int num) {
            return (double) Math.round((double) width / (num / 2 + (num % 2 == 0 ? 0 : 1)) * 72 / 400 * 10) / 10;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static long retFieldY(final int height, final double charSize) {
            return Math.round(((double) height - charSizeToPixel(charSize)) / 2);
        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static int fieldWidth(final double charSize, final int keta) {
            return charSizeToPixel(charSize) * keta / 2;
        }
    }

    private static class SpecialActMst implements Comparable<SpecialActMst> {
        final Integer _code;
        final String _name;
        final Integer _order;
        final String[] _flag = new String[7];

        SpecialActMst(final Integer code, final String name, final Integer order) {
            _code = code;
            _name = name;
            _order = order;
        }

        public int compareTo(final SpecialActMst sam) {
            int rtn = 0;
            if (null != _order || null != sam._order) {
                if (null == _order) {
                    rtn = 1;
                } else if (null == sam._order) {
                    rtn = -1;
                } else {
                    rtn = _order.compareTo(sam._order);
                }
            }
            if (rtn != 0) {
                return rtn;
            }
            rtn = _code.compareTo(sam._code);
            return rtn;
        }

        /**
         * 斜線を表示するか
         * @param param
         * @param student
         * @param kaitei
         * @param line 1:学級活動 2:児童会活動 3:クラブ活動 4:学校行事
         * @param g 学年
         * @return 斜線を表示するならtrue
         */
        public boolean isSlash(final Param param, final Student student, final KNJA133P_3.KAITEI kaitei, final int line, final int g) {
            if (param._isMusashinohigashi && "2".equals(student._personalInfo._recordDiv)) {
                if (2 == line || 3 == line) {
                    return true;
                }
                return false;
            }
            if (param._isOsakashinnai) {
                if (kaitei == KNJA133P_3.KAITEI._2020 && 3 == line && 4 == g) {
                    // 2020様式のクラブ活動4年生は斜線なし
                    return false;
                }
            }
            return "1".equals(_flag[g]);
        }

        private static Map<Integer, SpecialActMst> load(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map<Integer, SpecialActMst> rtn = new TreeMap<Integer, SpecialActMst>();
            try {
                final String sql = sql(year);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!NumberUtils.isDigits(rs.getString("NAMECD2"))) {
                        continue;
                    }
                    final Integer code = Integer.valueOf(rs.getString("NAMECD2"));
                    final String name1 = rs.getString("NAME1");
                    final String order = rs.getString("ORDER");
                    final SpecialActMst specialActMst = new SpecialActMst(code, name1, NumberUtils.isDigits(order) ? Integer.valueOf(order) : null);
                    for (int i = 1; i <= 6; i++) {
                        specialActMst._flag[i] = rs.getString("FLG" + i);
                    }
                    rtn.put(code, specialActMst);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private static String sql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.NAMECD2 ");
            stb.append("     ,NAME1 ");
            stb.append("     ,NAME3 AS ORDER ");
            stb.append("     ,ABBV1 AS FLG1 ");
            stb.append("     ,ABBV2 AS FLG2 ");
            stb.append("     ,ABBV3 AS FLG3 ");
            stb.append("     ,NAMESPARE1 AS FLG4 ");
            stb.append("     ,NAMESPARE2 AS FLG5 ");
            stb.append("     ,NAMESPARE3 AS FLG6 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.NAMECD1 = 'D034' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.NAMECD2 ");
            return stb.toString();
        }
    }

    private static class SchregEntGrdHistDat {

        final static SchregEntGrdHistDat NULL = new SchregEntGrdHistDat(null, null, null);

        final String _tengakuSakiZenjitu;
        final String _nyugakumaeSyussinJouhou;
        final String _curriculumYearCurriculumcd;
        public SchregEntGrdHistDat(final String tengakuSakiZenjitu, final String nyugakumaeSyussinJouhou, final String curriculumYearCurriculumcd) {
            _tengakuSakiZenjitu = tengakuSakiZenjitu;
            _nyugakumaeSyussinJouhou = nyugakumaeSyussinJouhou;
            _curriculumYearCurriculumcd = curriculumYearCurriculumcd;
        }

        private static SchregEntGrdHistDat load(final DB2UDB db2, final Param param, final String schregno, final String comebackDate) {
            final String psKey = "PS_SCHREG_ENT_GRD_HIST_DAT";
            if (null == param.getPs(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.TENGAKU_SAKI_ZENJITU ");
                stb.append("   , T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
                stb.append("   , (SELECT MAX(NAMECD2) AS CURRICULUM_CD FROM NAME_MST WHERE NAMECD1 = 'Z018' AND NAME3 <= T1.CURRICULUM_YEAR) AS CURRICULUM_YEAR_CURRICULUM_CD ");
                stb.append(" FROM ");
                if (null != comebackDate) {
                    stb.append("    SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
                } else {
                    stb.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
                }
                stb.append(" WHERE ");
                stb.append("     T1.SCHREGNO = ? ");
                stb.append("     AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
                if (null != comebackDate) {
                    stb.append("    AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
                }
                if (null != param._startYear) {
                    stb.append(" AND FISCALYEAR(T1.ENT_DATE) >= '" + param._startYear + "' ");
                }

                if (param._isOutputDebugQuery) {
                    log.info(" " + psKey + " sql = " + stb.toString());
                }
                param.setPs(psKey, db2, stb.toString());
            }
            SchregEntGrdHistDat rtn = null;
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] {schregno})) {

                final String tengakuSakiZenjitu = getString("TENGAKU_SAKI_ZENJITU", row);
                final String nyugakumaeSyussinJouhou = getString("NYUGAKUMAE_SYUSSIN_JOUHOU", row);
                final String curriculumYearCurriculumcd = getString("CURRICULUM_YEAR_CURRICULUM_CD", row);
                rtn = new SchregEntGrdHistDat(tengakuSakiZenjitu, nyugakumaeSyussinJouhou, curriculumYearCurriculumcd);
            }
            return null == rtn ? NULL : rtn;
        }
    }

    private static class Util {
        /**
         * listを最大数ごとにグループ化したリストを得る
         * @param list
         * @param max 最大数
         * @return listを最大数ごとにグループ化したリスト
         */
        private static <T> List<List<T>> getPageList(final List<T> list, final int max) {
            final List<List<T>> rtn = new ArrayList();
            List<T> current = null;
            for (final T o : list) {
                if (null == current || current.size() >= max) {
                    current = new ArrayList<T>();
                    rtn.add(current);
                }
                current.add(o);
            }
            return rtn;
        }

        private static <T> T head(final List<T> list, final T t) {
            if (list.size() > 0) {
                return list.get(0);
            }
            return t;
        }
        private static <T> T last(final List<T> list, final T t) {
            if (list.size() > 0) {
                return list.get(list.size() - 1);
            }
            return t;
        }
        private static <T, A extends Collection<T>, B extends Collection<A>> List<T> flatten(final B list) {
            final List<T> rtn = new ArrayList<T>();
            for (final Collection<T> t : list) {
                rtn.addAll(t);
            }
            return rtn;
        }
        private static <T> List<T> reverse(final Collection<T> col) {
            final LinkedList<T> rtn = new LinkedList<T>();
            for (final ListIterator<T> it = new ArrayList<T>(col).listIterator(col.size()); it.hasPrevious();) {
                rtn.add(it.previous());
            }
            return rtn;
        }
        private static StringBuffer mkString(final Map<String, String> flgMap, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (final String key : flgMap.keySet()) {
                if (StringUtils.isBlank(key)) {
                    continue;
                }
                final String val = flgMap.get(key);
                stb.append(c).append(key).append("=").append(val);
                c = comma;
            }
            return stb;
        }
        private static Calendar getCalendarOfDate(final String date) {
            final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            final Calendar cal = Calendar.getInstance();
            cal.setTime(sqlDate);
            return cal;
        }

        private static String getDateStringOfCalendar(final Calendar cal) {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(cal.getTime());
        }

        private static String plusDay(final String date, final int amount) {
            if (null == date) {
                return date;
            }
            final Calendar cal = getCalendarOfDate(date);
            cal.add(Calendar.DAY_OF_MONTH, amount);
            return getDateStringOfCalendar(cal);
        }

        private static String minDate(final String date1, final String date2) {
            if (null == date1) {
                return date2;
            } else if (null == date2) {
                return date1;
            }
            if (date1.compareTo(date2) <= 0) {
                return date1;
            }
            return date2;
        }

        private static String maxDate(final String date1, final String date2) {
            if (null == date1) {
                return date2;
            } else if (null == date2) {
                return date1;
            }
            if (date1.compareTo(date2) <= 0) {
                return date2;
            }
            return date1;
        }

        private static String prepend(final String prep, final Object o) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : prep + o.toString();
        }

        private static String append(final Object o, final String app) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : o.toString() + app;
        }
    }

    private static class Tuple<K, V> implements Comparable<Tuple<K, V>> {
        final K _first;
        final V _second;
        private Tuple(final K first, final V second) {
            _first = first;
            _second = second;
        }
        public static <K, V> Tuple<K, V> of(final K first, final V second) {
            return new Tuple<K, V>(first, second);
        }
        public int compareTo(final Tuple<K, V> to) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == to._first && !(to._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(to._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == to._second && !(to._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(to._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    protected enum Property {
        useSpecial_Support_School
    }

    private static class Param {

        final static String SCHOOL_KIND = "P";

        final String _year;
        final String _gakki;
        final String _hrClassType; // KNJA134Pのみ
        final String _gakunenKongou; // KNJA134Pのみ
        final String _notPrintGuarantor; // KNJA134Pのみ
        final String _gradeHrclass;
        final String _useSchregRegdHdat;
        final String _output;
        final String[] _categorySelected;
        final String _inei;
        final String _ctrlDate;
        final String _ctrlDateHFormatJp;
        final Map<String, List<String>> _paramMap;

        final String seito;
        final String gakushu;
        final String koudo;
        final String online;

        final String _simei;
        final String _schzip;
        final String _schoolzip;
        final String _colorPrint;
        final String _documentroot;
        final String _mongon;
        final String _useCurriculumcd;
        final String _use_finSchool_teNyuryoku_P;
        final String _seitoSidoYorokuUseHrClassType;
        final String _seitoSidoYorokuUseHrClassTypeP;
        final String _seitoSidoYorokuNotPrintKantenBlankIfPageOver;
        final String _seitosidoYorokuNotPrintTextInn;
        final String _printBlankPage; // 奇数ページの時は空白ページを印刷する
        final boolean _useJviewnameSubMstCurriculumcd;
        final boolean _useSvfFieldAreaInfo;
        final boolean _hasSchregEntGrdHistComebackDat;
        private Properties _prgInfoPropertiesFilePrperties;
        private Map<String, String> _dbPrgInfoProperties;

        final SchoolInfo _schoolInfo;

        /** 生年月日に西暦を使用するか */
        final boolean _isSeireki;

        private String _startYear = null;

        /** 卒業した学校の設立区分を表示するか */
        private boolean _isInstallationDivPrint;

        final Map<String, String> _gradeCdMap;

        final Map<String, PreparedStatement> _psMap = new HashMap();

        final Map<Integer, SpecialActMst> _specialActMstMap;
        final Map<String, String> _d035namecd2Name1Map;
        protected Map<String, ClassMst> _classMstMap = Collections.EMPTY_MAP;
        Map<String, Semester> _semesterMap;
        final StaffInfo _staffInfo = new StaffInfo();

        final boolean _hasAftGradCourseDat;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE;
        final boolean _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT_TENGAKU_SAKI_GRADE;
        /** FINSCHOOL_MST.FINSCHOOL_TYPE(名称マスタ「L019」、「中学校」「小学校」等)を表示しない */
        final String _notPrintFinschooltypeName;
        final List<ClassMst> _a046name1ClassList;
        final String _chiteki; // 1:知的障害（順ずる以外）  2:知的障害（順ずる）  3:知的障害（視覚障害等）(奈良県のみ)

        final String _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_P;
        final String _HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_P_disability;
        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P;
        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P_disability;
        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_P;
        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_P_disability;
        final String _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P;
        final String _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P_disability;
        final String _HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P;
        final String _HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P_disability;
        final String _HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P;
        final String _HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P;
        final String _HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P;
        final String _HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P;
        final String _HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P;
        final String _HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P;
        final String _HTRAINREMARK_P_DAT_FOREIGNLANGACT_SIZE_P2020;
        final String _knja133pUseViewSubclassMstSubclasscd2;
        final boolean _isChiben;
        final boolean _isMusashinohigashi;
        final boolean _isSundaikoufu;
        final boolean _isKyoaiP;
        final boolean _isFukuiken;
        final boolean _isSagaken;
        final boolean _isHibarigaoka;
        final boolean _isRitsumeikan;
        final boolean _isNaraken;
        final boolean _isKyoto;
        final boolean _isOsakashinnai;
        final Map<String, Map<String, List<String>>> _slashViewFieldIndexMapList;
        final Map<String, List<String>> _slashValueFieldIndexMapList;
        final Map<String, Map<String, List<String>>> _slashViewFieldIndexRecordDiv1MapList;
        final Map<String, List<String>> _slashValueFieldIndexRecordDiv1MapList;
        private Map hmap = null;
        private Map<String, File> _createFormFileMap = new TreeMap();
        final boolean _isOutputDebug;
        final boolean _isOutputDebugStaff;
        final boolean _isOutputDebugKanten;
        final boolean _isOutputDebugAll;
        final boolean _isOutputDebugField;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugKinsoku;
        final Set<String> _logOnce = new TreeSet<String>();

        public Param(final HttpServletRequest request, final DB2UDB db2, final Map<String, String> paramMap) throws SQLException {

            _documentroot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
            if (!StringUtils.isEmpty(_documentroot)) {
                _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
            }
            _dbPrgInfoProperties = getDbPrginfoProperties(db2, StringUtils.defaultString(request.getParameter("PRGID"), "KNJA133P"));
            _paramMap = getParameterMap(request);
            for (final Map.Entry<String, String> e : paramMap.entrySet()) {
                _paramMap.put(e.getKey(), Arrays.asList(e.getValue()));
            }

            final String[] outputDebug = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            log.info(" outputDebug = " + ArrayUtils.toString(outputDebug));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebugStaff = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "staff");
            _isOutputDebugKanten = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "kanten");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugField = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "field");
            _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugKinsoku = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "kinsoku");

            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS"); // 学年・組
            _categorySelected = request.getParameterValues("category_selected");
            _output = request.getParameter("OUTPUT");
            _printBlankPage = request.getParameter("PRINT_BLANK_PAGE");
            if ("1".equals(request.getParameter("inei_print"))) {
                _inei = "3";
            } else if ("1".equals(request.getParameter("inei_print2"))) {
                _inei = "4";
            } else {
                final String INEI = request.getParameter("INEI");
                _inei = StringUtils.isBlank(INEI) ? null : INEI;
            }
            if (!StringUtils.isBlank(_inei)) {
                log.info(" _inei = " + _inei);
            }
            _ctrlDate = request.getParameter("CTRL_DATE");

            seito = request.getParameter("seito");
            gakushu = request.getParameter("gakushu");
            koudo = request.getParameter("koudo");
            online = request.getParameter("online");
            _notPrintGuarantor = request.getParameter("not_print_guarantor");

            _simei = request.getParameter("simei"); // 漢字名出力
            _schzip = request.getParameter("schzip");
            _schoolzip = request.getParameter("schoolzip");
            _colorPrint = request.getParameter("color_print");
            _mongon = request.getParameter("mongon"); // 文言表示

            _useSchregRegdHdat = property("useSchregRegdHdat");
            _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD") ? "1" : property("useCurriculumcd");
            _use_finSchool_teNyuryoku_P = property("use_finSchool_teNyuryoku_P");
            _seitoSidoYorokuUseHrClassType = property("seitoSidoYorokuUseHrClassType");
            _seitoSidoYorokuUseHrClassTypeP = property("seitoSidoYorokuUseHrClassTypeP");

            final String z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            log.info(" z010 name1 = " + z010Name1);
            _isChiben = "CHIBEN".equals(z010Name1);
            _isMusashinohigashi = "musashinohigashi".equals(z010Name1);
            _isSundaikoufu = "sundaikoufu".equals(z010Name1);
            _isKyoaiP = "kyoaiP".equals(z010Name1);
            _isFukuiken = "fukuiken".equals(z010Name1);
            _isSagaken = "sagaken".equals(z010Name1);
            _isHibarigaoka = "hibarigaoka".equals(z010Name1);
            _isRitsumeikan = "Ritsumeikan".equals(z010Name1);
            _isNaraken = "naraken".equals(z010Name1);
            _isKyoto = "kyoto".equals(z010Name1);
            _isOsakashinnai = "osakashinnai".equals(z010Name1);
            if (_isKyoto || _isNaraken) {
                _startYear = "2020";
            }

            _schoolInfo = SchoolInfo.load(db2, this);

            _isSeireki = KNJ_EditDate.isSeireki(db2) || _isKyoaiP;
            _ctrlDateHFormatJp = h_format_JP(db2, this, _ctrlDate);

            _gradeCdMap = getGradeCdMap(db2);

            _useSvfFieldAreaInfo = (isTokubetsuShien() || "1".equals(property("seitoSidoYorokuUseSvfFieldAreaP"))) && !(_isNaraken || _isKyoto || "1".equals(property(Property.useSpecial_Support_School)));
            _useJviewnameSubMstCurriculumcd = _isSundaikoufu;
            log.info(" _useJviewnameSubMstCurriculumcd = " + _useJviewnameSubMstCurriculumcd);

            _specialActMstMap = SpecialActMst.load(db2, _year);
            Map d035namecd2Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'DP35' "), "NAMECD2", "NAME1");
            if (d035namecd2Name1Map.isEmpty()) {
                d035namecd2Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D035' "), "NAMECD2", "NAME1");
            }
            _d035namecd2Name1Map = d035namecd2Name1Map;

            _hasSchregEntGrdHistComebackDat = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _hasAftGradCourseDat = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_DAT", "TENGAKU_SAKI_GRADE");
            _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT_TENGAKU_SAKI_GRADE = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", "TENGAKU_SAKI_GRADE");
            _slashViewFieldIndexMapList = getSlashViewIndexList(db2, null);
            _slashValueFieldIndexMapList = getSlashValueIndexMapList(db2, null);
            _slashViewFieldIndexRecordDiv1MapList = getSlashViewIndexList(db2, "2");
            _slashValueFieldIndexRecordDiv1MapList = getSlashValueIndexMapList(db2, "2");
            _notPrintFinschooltypeName = property("notPrintFinschooltypeName");
            _seitoSidoYorokuNotPrintKantenBlankIfPageOver = property("seitoSidoYorokuNotPrintKantenBlankIfPageOver");
            _seitosidoYorokuNotPrintTextInn = property("seitosidoYorokuNotPrintTextInn");
            setMaster(db2);
            _staffInfo.setInkanMap(db2, this);
            _staffInfo.setYearPrincipalMap(db2, this);
            _staffInfo._staffMstMap = StaffMst.load(db2, _year, this);
            _staffInfo.setStaffClassHistMap(db2, this, _year);

            _a046name1ClassList = new ArrayList<ClassMst>();
            final List<String> a046Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A046' "), "NAME1");
            for (final String name1 : a046Name1List) {
                final ClassMst cm = ClassMst.get(_classMstMap, name1);
                if (null != cm) {
                    _a046name1ClassList.add(cm);
                }
            }
            if (_isOutputDebug) {
                log.info(" A046 " + a046Name1List + ", " + _a046name1ClassList);
            }

            _chiteki = request.getParameter("CHITEKI");
            if (_isOutputDebugKanten) {
                if (true) {
                    log.info(" _slashViewFieldIndexMapList = " + _slashViewFieldIndexMapList);
                    log.info(" _slashValueFieldIndexMapList = " + _slashValueFieldIndexMapList);
                }
            }
            _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_P                 = property("HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_P");
            _HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_P_disability        = property("HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_P_disability");
            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P             = property("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P");
            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P_disability  = property("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_P_disability");
            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_P                        = property("HTRAINREMARK_DAT_TOTALREMARK_SIZE_P");
            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_P_disability             = property("HTRAINREMARK_DAT_TOTALREMARK_SIZE_P_disability");
            _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P                   = property("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P");
            _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P_disability        = property("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P_disability");
            _HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P                    = property("HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P");
            _HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P_disability         = property("HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P_disability");
            _HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P                      = property("HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P");
            _HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P                 = property("HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P");
            _HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P                  = property("HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P");
            _HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P                    = defstr(property("HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P"), property("HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_P"));
            _HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P                       = defstr(property("HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P"), property("HTRAINREMARK_DAT_VIEWREMARK_SIZE_P"));
            _HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P                    = defstr(property("HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P"), property("HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_P"));
            _HTRAINREMARK_P_DAT_FOREIGNLANGACT_SIZE_P2020               = property("HTRAINREMARK_P_DAT_FOREIGNLANGACT_SIZE_P2020");
            _knja133pUseViewSubclassMstSubclasscd2                      = property("knja133pUseViewSubclassMstSubclasscd2");
        }

        protected static Map<String, String> getDbPrginfoProperties(final DB2UDB db2, final String prgid) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = '" + prgid + "' "), "NAME", "VALUE");
        }

        protected Properties loadPropertyFile(final String filename) {
            File file = null;
            if (null != _documentroot) {
                file = new File(new File(_documentroot).getParentFile().getAbsolutePath() + "/config/" + filename);
                if (_isOutputDebug) {
                    log.info("check prop : " + file.getAbsolutePath() + ", exists? " + file.exists());
                }
                if (!file.exists()) {
                    file = null;
                }
            }
            if (null == file) {
                file = new File(_documentroot + "/" + filename);
            }
            if (!file.exists()) {
                if (_isOutputDebug) {
                    log.error("file not exists: " + file.getAbsolutePath());
                }
                return null;
            }
            if (_isOutputDebug) {
                log.error("file : " + file.getAbsolutePath() + ", " + file.length());
            }
            final Properties props = new Properties();
            FileReader r = null;
            try {
                r = new FileReader(file);
                props.load(r);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                if (null != r) {
                    try {
                        r.close();
                    } catch (Exception _ignored) {
                    }
                }
            }
            return props;
        }

        private Map<String, List<String>> getParameterMap(final HttpServletRequest request) {
            final Map<String, List<String>> paramMap = new TreeMap<String, List<String>>();
            for (final Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
                final String name = e.getKey();
                final String[] vals = e.getValue();
                if (null == vals || vals.length == 0) {
                    paramMap.put(name, null);
                } else {
                    paramMap.put(name, Arrays.asList(vals));
                }
            }
            return paramMap;
        }

        protected String property(final Property prop) {
            return property(prop.name());
        }

        protected String property(final String name) {
            if (_paramMap.containsKey(name)) {
                return getParameter(name);
            }
            String val = null;
            if (null != _dbPrgInfoProperties) {
                if (_dbPrgInfoProperties.containsKey(name)) {
                    val = _dbPrgInfoProperties.get(name);
                    if (_isOutputDebug) {
                        logOnce("property in db: " + name + " = " + val);
                    }
                    return val;
                }
            }
            if (null != _prgInfoPropertiesFilePrperties) {
                if (_prgInfoPropertiesFilePrperties.containsKey(name)) {
                    val = _prgInfoPropertiesFilePrperties.getProperty(name);
                    if (_isOutputDebug) {
                        logOnce("property in file: " + name + " = " + val);
                    }
                } else {
                    if (_isOutputDebug) {
                        logOnce("property not exists in file: " + name);
                    }
                }
            }
            return val;
        }

        protected String getParameter(final String name) {
            if (!_paramMap.containsKey(name)) {
                if (_isOutputDebug) {
                    logOnce(" no param : " + name);
                }
                return null;
            }
            if (null == _paramMap.get(name)) {
                if (_isOutputDebug) {
                    logOnce(" parameter " + name + " = null");
                }
                return null;
            }
            final List<String> values = _paramMap.get(name);
            if (_isOutputDebug) {
                logOnce(" parameter " + name + " = " + values);
            }
            final String value = values.get(0);
            return value;
        }

        private void setMaster(final DB2UDB db2) {
            setClassMst(db2);
            setSemester(db2);
        }

        private void setSemester(final DB2UDB db2) {
            _semesterMap = new HashMap();
            final String sql = " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE SEMESTER <> '9' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final Semester semester = new Semester(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE"));
                final String key = Semester.key(semester._year, semester._semester);
                _semesterMap.put(key, semester);
            }
        }

        public void closeForm() {
            for (final File file : _createFormFileMap.values()) {
                if (null != file && file.exists()) {
                    if (_isOutputDebugField) {
                        log.info(" delete file : " + file.getAbsolutePath());
                    }
                    file.delete();
                }
            }
        }

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        public void setPs(final String psKey, final DB2UDB db2, final String sql) {
            try {
                PreparedStatement ps = db2.prepareStatement(sql);
                _psMap.put(psKey, ps);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }

        public void closeStatementQuietly() {
            for (final Iterator<PreparedStatement> it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = it.next();
                DbUtils.closeQuietly(ps);
            }
        }

        /**
         * 帳票作成JAVAクラスをＬＩＳＴへ格納 NO001 Build
         */
        private List<KNJA133P_0> getKnja133List(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf) {
            final List<KNJA133P_0> rtnList = new ArrayList<KNJA133P_0>();
            if (seito!= null) {
                rtnList.add(new KNJA133P_1(this, svf)); // 様式１（学籍に関する記録）
            }
            if (gakushu != null) {
                if (isTokubetsuShien() && Arrays.asList(CHITEKI1_知的障害, CHITEKI3_知的障害_視覚障害等).contains(_chiteki)) {
                    rtnList.add(new KNJA134P_3(this, svf)); // 様式２（指導に関する記録）
                } else {
                    rtnList.add(new KNJA133P_3(this, svf)); // 様式２（指導に関する記録）
                }
            }
            if (koudo != null) {
                rtnList.add(new KNJA133P_4(this, svf)); // 様式３
            }
            if (online != null) {
                rtnList.add(new KNJA129Delegate(this, svf, request, db2)); // 様式３
            }
            return rtnList;
        }

        protected List<String> getSchregnoList(final DB2UDB db2) {
            final List<String> schregnoList = new ArrayList<String>();
            if ("2".equals(_output)) {
                final StringBuffer where1 = new StringBuffer();
                if (isTokubetsuShien()) {
                    where1.append("  AND ");
                    where1.append("     EXISTS ( ");
                    where1.append("         SELECT ");
                    where1.append("             'X' ");
                    where1.append("         FROM ");
                    where1.append("             SCHREG_BASE_MST BASE ");
                    where1.append("         INNER JOIN NAME_MST A025 ON A025.NAMECD1 = 'A025' AND A025.NAMECD2 = BASE.HANDICAP AND A025.NAMESPARE3 = '" + _chiteki + "' ");
                    where1.append("         WHERE BASE.SCHREGNO = T1.SCHREGNO ");
                    where1.append("     ) ");
                }
                if (null != _startYear) {
                    where1.append(" AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR >= '" + _startYear + "') ");
                }

                final StringBuffer stb = new StringBuffer();
                if (isTokubetsuShien() && "2".equals(_hrClassType)) {
                    stb.append(" SELECT T1.SCHREGNO ");
                    stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
                    stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                    stb.append("   AND T1.SEMESTER = '" + _gakki + "' ");
                    stb.append("   AND T1.GHR_CD IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                    stb.append(where1);
                    stb.append("ORDER BY T1.GHR_CD, T1.GHR_ATTENDNO ");

                } else {
                    stb.append(" SELECT T1.SCHREGNO ");
                    stb.append(" FROM SCHREG_REGD_DAT T1 ");
                    stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                    stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                    stb.append("   AND T1.SEMESTER = '" + _gakki + "' ");
                    if (isTokubetsuShien() && "1".equals(_gakunenKongou)) {
                        stb.append("   AND T2.SCHOOL_KIND || '-' || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                    } else {
                        stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                    }
                    stb.append(where1);
                    stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
                }

                schregnoList.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO")); // 任意のHR組の学籍番号取得用

            } else {
                if (isTokubetsuShien()) {
                    for (int i = 0; i < _categorySelected.length; i++) {
                        schregnoList.add(StringUtils.split(_categorySelected[i], "-")[1]);
                    }
                } else {
                    schregnoList.addAll(Arrays.asList(_categorySelected));
                }
                if (null != _startYear) {
                    final String sql = " SELECT DISTINCT SCHREGNO FROM SCHREG_REGD_DAT WHERE SCHREGNO = ? AND YEAR >= '" + _startYear + "' ";
                    PreparedStatement ps = null;
                    try {
                        ps = db2.prepareStatement(sql);
                        for (final Iterator<String> it = schregnoList.iterator(); it.hasNext();) {
                            final String schregno = it.next();
                            if (null == KnjDbUtils.getOne(KnjDbUtils.query(db2, sql, new Object[] { schregno }))) {
                                log.info(" remove schregno " + schregno);
                                it.remove();
                            }
                        }

                    } catch (Exception e) {
                        log.error("exception!", e);
                    } finally {
                        DbUtils.closeQuietly(ps);
                    }
                }
            }
            return schregnoList;
        }

        public String getImageFilePath(final String filename) {
            if (null == _documentroot) {
                return null;
            }
            final String imagePath = "image/stamp";
            final String extension = "bmp";
            // 写真データ存在チェック
            final String path = _documentroot + "/" + imagePath + "/" + filename + "." + extension;
            if (!new File(path).exists()) {
                log.warn("file not found : " + path);
            }
            return path;
        }

        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = _gradeCdMap.get(year + grade);
            if (!NumberUtils.isNumber(gradeCd)) {
                log.warn("not number gradeCd : " + gradeCd + " (year = " + year + ", grade = " + grade + ")");
            }
            return NumberUtils.isNumber(gradeCd) ? Integer.parseInt(gradeCd) : -1;
        }

        private String getZ010(DB2UDB db2, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private Map<String, String> getGradeCdMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR || T1.GRADE AS YEAR_GRADE, T1.* ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (null != _startYear) {
                stb.append(" AND T1.YEAR >= '" + _startYear + "' ");
            }


            final Map<String, String> gdatMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "YEAR_GRADE", "GRADE_CD");
            return gdatMap;
        }

        private boolean isTokubetsuShien() {
            return "TOKUBETSU_SHIEN".equals(getParameter("OUTPUT_KIND")); // KNJA134Pからコールされた場合
        }

        private List<String> getSlashViewGradeList(final String recordDiv, final KNJA133P_3.KAITEI kaitei, final String subclasscd, final int iline) {
            final Map m;
            if ("2".equals(recordDiv)) {
                m = _slashViewFieldIndexRecordDiv1MapList;
            } else {
                m = _slashViewFieldIndexMapList;
            }
            final String line = String.valueOf(iline);
            List<String> gradeList = new ArrayList<String>();
            if (getMappedMap(m, subclasscd).containsKey(line)) {
                gradeList = getMappedList(getMappedMap(m, subclasscd), line);
                if (_isOutputDebugKanten) {
                    log.info(" isSlashView : " + subclasscd + " : " + line + " / recordDiv = " + recordDiv + " = " + gradeList);
                }
            }
            if (_isMusashinohigashi) {
                if ("2".equals(recordDiv) && kaitei == KNJA133P_3.KAITEI._2020) {
                    if (subclasscd.endsWith("120100")) { // 社会
                        if (3 == iline) { // 主体的に学習に取り組む態度
                            gradeList = new ArrayList<String>(gradeList);
                            gradeList.removeAll(Arrays.asList("3", "4", "5", "6"));
                        }
                    }
                }
            }
            return gradeList;
        }

        private List<String> getSlashValueNameMstGradeList(final String recordDiv, final KNJA133P_3.KAITEI kaitei, final String subclasscd) {
            final Map m;
            if ("2".equals(recordDiv)) {
                m = _slashValueFieldIndexRecordDiv1MapList;
            } else {
                m = _slashValueFieldIndexMapList;
            }
            List<String> gradeList = getMappedList(m, subclasscd);
            if (_isMusashinohigashi) {
                if (kaitei == KNJA133P_3.KAITEI._2020) {
                    if (subclasscd.endsWith("150100")) { // 生活
                        gradeList = new ArrayList<String>(gradeList);
                        gradeList.addAll(Arrays.asList("3", "4", "5", "6"));
                    }
                }
            }
            if (_isOutputDebugKanten) {
                log.info(" slash value (" + subclasscd + ") grade list = " + gradeList);
            }
            return gradeList;
        }

        /**
         * スラッシュを表示する観点フィールド(科目、学年、行)のリスト
         */
        private Map<String, Map<String, List<String>>> getSlashViewIndexList(final DB2UDB db2, final String recordDiv) {
            Map<String, Map<String, List<String>>> map = new HashMap<String, Map<String, List<String>>>();
            final String sql;
            if ("2".equals(recordDiv)) {
                sql = "SELECT NAME1 AS SUBCLASSCD, ABBV1 AS GAKUNEN, ABBV2 AS LINE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A038' AND ABBV1 IS NOT NULL AND ABBV2 IS NOT NULL ";
            } else {
                sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN, NAME3 AS LINE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A038' AND NAME2 IS NOT NULL AND NAME3 IS NOT NULL ";
            }
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                if (null != KnjDbUtils.getString(row, "SUBCLASSCD") && null != KnjDbUtils.getString(row, "GAKUNEN") && NumberUtils.isDigits(KnjDbUtils.getString(row, "LINE"))) {
                    getMappedList(getMappedMap(map, KnjDbUtils.getString(row, "SUBCLASSCD")), KnjDbUtils.getString(row, "LINE")).add(KnjDbUtils.getString(row, "GAKUNEN"));
                }
            }
            return map;
        }

        /**
         * スラッシュを表示する評定フィールド(科目、学年)のリスト
         */
        private Map<String, List<String>> getSlashValueIndexMapList(final DB2UDB db2, final String recordDiv) {
            Map<String, List<String>> map = new HashMap<String, List<String>>();
            final String sql;
            if ("2".equals(recordDiv)) {
                sql = "SELECT NAME1 AS SUBCLASSCD, ABBV1 AS GAKUNEN FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A039' AND ABBV1 IS NOT NULL ";
            } else {
                sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A039' AND NAME2 IS NOT NULL ";
            }
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                if (null != KnjDbUtils.getString(row, "SUBCLASSCD") && null != KnjDbUtils.getString(row, "GAKUNEN")) {
                    getMappedList(map, KnjDbUtils.getString(row, "SUBCLASSCD")).add(KnjDbUtils.getString(row, "GAKUNEN"));
                }
            }
            return map;
        }

        private void setClassMst(final DB2UDB db2) {
            _classMstMap = new HashMap();
            final Integer showOrderDefault = new Integer(-1);
            final String sql = " SELECT * FROM CLASS_MST ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                String classcd = KnjDbUtils.getString(row, "CLASSCD");
                String schoolKind = null == row.get("SCHOOL_KIND") ? null : KnjDbUtils.getString(row, "SCHOOL_KIND");
                String classname = StringUtils.defaultString(KnjDbUtils.getString(row, "CLASSORDERNAME1"), KnjDbUtils.getString(row, "CLASSNAME"));
                final String electdiv = StringUtils.defaultString(KnjDbUtils.getString(row, "ELECTDIV"), "0");

                final String specialDiv = StringUtils.defaultString(KnjDbUtils.getString(row, "SPECIALDIV"), "0");
                Integer showorder = KnjDbUtils.getInt(row, "SHOWORDER", null);
                if (null == showorder) {
                    showorder = showOrderDefault;
                }
                final ClassMst cm = new ClassMst(classcd, schoolKind, classname, electdiv, specialDiv, showorder);

                _classMstMap.put(cm.getKey(this), cm);
            }
        }

        private void logOnce(final String l) {
            if (_logOnce.contains(l)) {
                return;
            }
            log.info(l);
            _logOnce.add(l);
        }
    }
}
