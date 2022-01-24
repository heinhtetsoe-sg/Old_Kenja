// kanji=漢字
/*
 * $Id: f84108c6bbfcd0fe7f851e5e4e20eb794dda0ef0 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfFieldAreaInfo;
import servletpack.KNJZ.detail.SvfFieldAreaInfo.ModifyParam;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.ImageField;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 中学校用
 */

public class KNJA133J {
    private static final Log log = LogFactory.getLog(KNJA133J.class);

    private static final String CHITEKI1_知的障害 = "1";
    private static final String CHITEKI2_知的障害以外 = "2";

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        svf_out_ex(request, response, Collections.EMPTY_MAP);
    }

    // KNJA134Jコール
    public void svf_out_ex(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Map paramMap
    ) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        Param param = null;
        boolean nonedata = false;
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
            nonedata = printSvf(request, db2, svf, param);
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                param.close();
            }

            // 終了処理
            sd.closeSvf(svf, nonedata);
            sd.closeDb(db2);
        }
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) {
        log.fatal("$ 2021-03-30 $");
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2, paramMap);
    }

    /**
     * 印刷処理
     */
    private boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean hasdata = false;

        param.setKnja133List(svf, request, db2);

        final List<Student> studentList = getStudentList(db2, param);

        for (final Student student : studentList) {
            if (param._isOutputDebug) {
                log.info(" schregno = " + student._schregno);
            }

            student.load(db2, param);

            final boolean printPage3 = null != param._knja134j_3 || null != param._knja133j_3;
            final boolean printPage4 = null != param._knja133j_4;
            final boolean printPageOnline = null != param._knja129;
            if (null != param._knja133j_1) {
                param._knja133j_1.printSvf(db2, student);
                if (param._knja133j_1.hasdata) {
                    hasdata = true; // 印刷処理
                }
                if ("1".equals(param._printBlankPage) && (printPage3 || printPage4)) {
                    // 「奇数ページの時は空白ページを印刷する」総ページ数が奇数で
                    svf.VrSetForm("BLANK_A4_TATE.frm", 1);
                    svf.VrsOut("BLANK", "BLANK");
                    svf.VrEndPage();
                }
            }

            if (printPage3 || printPage4) {
                if (param.isTokubetsuShien() && CHITEKI1_知的障害.equals(param._chiteki)) {
                    final KNJA134J_3 p3 = param._knja134j_3;
                    if (null != p3) {
                        p3.printSvf(db2, student);
                        if (p3.hasdata) {
                            hasdata = true; // 印刷処理
                        }
                    }
                    final KNJA133J_4 p4 = param._knja133j_4;
                    if (null != p4) {
                        p4.printSvf(db2, student, null, null);
                        if (p4.hasdata) {
                            hasdata = true; // 印刷処理
                        }
                    }
                } else {
                    final FormInfo formInfo = new FormInfo(svf, param, student);
                    final List<Tuple<KaiteiInfo, List<String>>> pageYearsList = formInfo.getPageYearsList();

                    for (final Tuple<KaiteiInfo, List<String>> kaiteiAndPageYears : pageYearsList) {
                        final KaiteiInfo kaiteiInfo = kaiteiAndPageYears._first;
                        final List<String> pageYears = kaiteiAndPageYears._second;

                        final KNJA133J_3 p3 = param._knja133j_3;
                        if (null != p3) {
                            p3.printSvf(db2, student, formInfo, kaiteiInfo, pageYears);
                            if (p3.hasdata) {
                                hasdata = true; // 印刷処理
                            }
                        }
                        final KNJA133J_4 p4 = param._knja133j_4;
                        if (null != p4) {
                            p4.printSvf(db2, student, kaiteiInfo, pageYears);
                            if (p4.hasdata) {
                                hasdata = true; // 印刷処理
                            }
                        }
                    }
                }
            }
            if (printPageOnline) {
                final KNJA129Delegate a129 = param._knja129;
                if (null != a129) {
                    a129.printSvf(db2, student);
                    if (a129.hasdata) {
                        hasdata = true; // 印刷処理
                    }
                }
            }
        }
        final KNJA129Delegate a129 = param._knja129;
        if (null != a129) {
            a129.close();
        }
        return hasdata;
    }

    private List<Student> getStudentList(final DB2UDB db2, final Param param) {

        final List<String> schregnoList = param.getSchregnoList(db2);

        final List<Student> studentList = new ArrayList<Student>();
        for (final String schregno : schregnoList) {
            final Student student = new Student(schregno);
            student.loadGuard(db2, param);
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

    private static String getDateStringOfCalendar(final Calendar cal) {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(cal.getTime());
    }

    private static int getCalendarNendo(final Calendar cal) {
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
            return year - 1;
        }
        return year;
    }

    /**
     * 日付の編集（XXXX年XX月XX日の様式に編集）
     * @param hdate
     * @return
     */
    private static String setDateFormat2(final Param param, final String hdate) {
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
    private static String h_format_JP(final DB2UDB db2, final Param param, String strx) {

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

    private static StringBuilder mkString(final List<String> list, final String comma) {
        final StringBuilder stb = new StringBuilder();
        String c = "";
        for (final String line : list) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            stb.append(c).append(line);
            c = comma;
        }
        return stb;
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

    private static abstract class Cond {

        public static Field field(final String field) {
            return new Field(field);
        }

        public static Value val(final String field) {
            return new Value(field);
        }

        public abstract boolean isValid(final Map row);

        public abstract String toSql();

        private static abstract class Ref {

            public Cond isNull() {
                return new IsNull(this);
            }

            public Cond eq(final Ref v) {
                return new Cmp(this, v, 0);
            }

            public Cond lessThan(final Ref v) {
                return new Cmp(this, v, -1);
            }

            public Cond greaterThan(final Ref v) {
                return new Cmp(this, v, 1);
            }

            public Cond lessEqual(final Ref v) {
                return lessThan(v).or(eq(v));
            }

            public Cond greaterEqual(final Ref v) {
                return greaterThan(v).or(eq(v));
            }

            public Cond between(final Ref v1, final Ref v2) {
                final Cond c = greaterEqual(v1).and(lessEqual(v2));
                return c;
            }

            abstract public String eval(final Map<String, String> row);
        }

        private static class Field extends Ref {
            final String _field;
            Field(final String field) {
                _field = field;
            }
            public String eval(final Map<String, String> row) {
                return KnjDbUtils.getString(row, _field);
            }
            public String toString() {
                return _field;
            }
        }

        private static class Value extends Ref {
            final String _val;
            Value(final String val) {
                _val = val;
            }
            public String eval(final Map<String, String> _) {
                return _val;
            }
            public String toString() {
                return "'" + _val + "'";
            }
        }

        public Cond and(final Cond c2) {
            return new And(this, c2);
        }

        public Cond or(final Cond c2) {
            return new Or(this, c2);
        }

        private static class IsNull extends Cond {
            final Ref _field;
            public IsNull(final Ref field) {
                _field = field;
            }
            public boolean isValid(final Map row) {
                return null == _field.eval(row);
            }
            public String toSql() {
                return _field + " IS NULL ";
            }
        }

        private static class Cmp extends Cond {
            final Ref _field;
            final Ref _val;
            final int _c;
            Cmp(final Ref field, final Ref val, final int c) {
                _field = field;
                _val = val;
                _c = c;
            }
            public boolean isValid(final Map row) {
                final String v = _field.eval(row);
                if (null == v) {
                    return false;
                }
                final int cmp = v.compareTo(_val.eval(row));
                boolean ret;
                if (_c > 0) {
                    ret = cmp > 0;
                } else if (_c < 0) {
                    ret = cmp < 0;
                } else {
                    ret = cmp == 0;
                }
                //log.debug("  " + (_c > 0 ? "GT" : _c < 0 ? "LT" : "EQ") + ": field = " + _field + ", v = " + v + ", _val = " + _val + ", cmp = " + cmp + ", ret = " + ret);
                return ret;
            }
            public String toSql() {
                if (_c > 0) {
                    return _field + " > " + _val + " ";
                } else if (_c < 0) {
                    return _field + " < " + _val + " ";
                } else {
                    return _field + " = " + _val + " ";
                }
            }
        }

        private static class True extends Cond {
            public boolean isValid(final Map row) {
                return true;
            }
            public String toSql() {
                return " (true) ";
            }
        }

        private static class And extends Cond {
            final Cond _c1;
            final Cond _c2;
            public And(final Cond c1, final Cond c2) {
                _c1 = c1;
                _c2 = c2;
            }
            public boolean isValid(final Map row) {
                return _c1.isValid(row) && _c2.isValid(row);
            }
            public String toSql() {
                return "(" + _c1.toSql() + ") AND (" + _c2.toSql() + ")";
            }
        }

        private static class Or extends Cond {
            final Cond _c1;
            final Cond _c2;
            public Or(final Cond c1, final Cond c2) {
                _c1 = c1;
                _c2 = c2;
            }
            public boolean isValid(final Map row) {
                return _c1.isValid(row) || _c2.isValid(row);
            }
            public String toSql() {
                return "(" + _c1.toSql() + ") OR (" + _c2.toSql() + ")";
            }
        }
    }

    private static List<Map<String, String>> filter(final List<Map<String, String>> mapList, final Cond cond) {
        final List<Map<String, String>> rtn = new ArrayList<Map<String, String>>();
        for (final Map row : mapList) {
            if (cond.isValid(row)) {
                rtn.add(row);
            }
        }
        return rtn;
    }

    /**
     * 生徒情報
     */
    private static class Student {
        final String _schregno;
        SchregRegdDat _loginRegd = new SchregRegdDat();
        List<SchregRegdDat> _regdList;
        PersonalInfo _personalInfo;
        List<Attend> _attendList;
        List<HTrainRemarkDat> _htrainRemarkDatList;
        Map _schregBaseMstRow;
        Map _guardianDatRow;
        List<Map<String, String>> _guardianHistDatList;
        List<Map<String, String>> _schregBaseHistDatList;
        HtrainremarkHdat _htrainremarkHdat;
        String _htrainRemarkDetail2Hdat002Remark1;
        List<ActRecord> _actList;
        List<ClassView> _classViewList;
        List<ValueRecord> _valueRecordList;
        List<String> _afterGraduatedCourseTextList;
        List<PersonalInfo> _schregEntGrdHistComebackDatList;
        final Map<String, Boolean> _yearLimitCache = new HashMap<String, Boolean>();
        public Student(final String schregno) {
            _schregno = schregno;
        }

        public List<ClassView> getClassViewList(final List<String> pageYears) {
            final List<ClassView> rtn = new ArrayList<ClassView>();
            for (final ClassView cv : _classViewList) {
                final ClassView enabled = ClassView.enabled(cv, pageYears);
                if (null != enabled) {
                    rtn.add(enabled);
                }
            }
            return rtn;
        }

        public void load(final DB2UDB db2, final Param param) {

            final String psRegdKey = "PS_REGD";
            if (null == param.getPs(psRegdKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T2.GRADE_CD ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE T1.SCHREGNO = ? ");
                stb.append("  AND  T2.YEAR <= '" + param._year + "' ");
                stb.append("  AND  (T2.SCHOOL_KIND IS NULL OR T2.SCHOOL_KIND = 'J') ");
                stb.append(" ORDER BY T1.YEAR, T1.SEMESTER ");

                if (param._isOutputDebugQuery) {
                    log.info(" regd sql = " + stb.toString());
                }

                param.setPs(db2, psRegdKey, stb.toString());
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
                regd._gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                if (param._year.equals(regd._year) && param._gakki.equals(regd._semester)) {
                    _loginRegd = regd;
                }
                _regdList.add(regd);
            }

            if (param._z010.in(Z010.teihachi) && null != param._knja133j_4) {
                final String psKey = "PS_HTRAINREMARK_DETAIL2_HDAT_002 ";
                if (null == param.getPs(psKey)) {
                    final String sql = " SELECT REMARK1 FROM HTRAINREMARK_DETAIL2_HDAT WHERE SCHREGNO = ? AND HTRAIN_SEQ = '002' ";
                    param.setPs(db2, psKey, sql);
                }
                _htrainRemarkDetail2Hdat002Remark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { _schregno }));
            }

            _personalInfo = PersonalInfo.load(db2, param, this, null);
            _attendList = Attend.load(db2, param, this);
            _htrainRemarkDatList = HTrainRemarkDat.load(db2, param, this);
            _htrainremarkHdat = HtrainremarkHdat.load(db2, param, _schregno);
            _actList = ActRecord.load(db2, param, this);
            _classViewList = ClassView.load(db2, param, this);
            _valueRecordList = ValueRecord.load(db2, param, this);
            _afterGraduatedCourseTextList = AfterGraduatedCourse.loadTextList(db2, param, _personalInfo._gakusekiList, this);
        }

        private void loadGuard(final DB2UDB db2, final Param param) {
            _schregBaseMstRow     = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + _schregno + "' "));
            _schregBaseHistDatList = KnjDbUtils.query(db2, "SELECT * FROM SCHREG_BASE_HIST_DAT WHERE SCHREGNO = '" + _schregno + "' ORDER BY ISSUEDATE ");
            _guardianDatRow       = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM GUARDIAN_DAT WHERE SCHREGNO = '" + _schregno + "' "));
            _guardianHistDatList   = KnjDbUtils.query(db2, "SELECT * FROM GUARDIAN_HIST_DAT    WHERE SCHREGNO = '" + _schregno + "' ORDER BY ISSUEDATE ");
        }

        protected boolean isPrintYear(final String regdYear, final Param param) {
            final String _seitoSidoYorokuCyugakuKirikaeNendo = param.property(Property.seitoSidoYorokuCyugakuKirikaeNendo);
            final String _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear = param.property(Property.seitoSidoYorokuCyugakuKirikaeNendoForRegdYear);

            final String year = "1".equals(_seitoSidoYorokuCyugakuKirikaeNendoForRegdYear) ? regdYear : _personalInfo._curriculumYear;
            if (null == _yearLimitCache.get(year)) {
                Boolean rtn;
                if (param.isTokubetsuShien()) {
                    rtn = Boolean.TRUE;
                } else if (!NumberUtils.isDigits(_seitoSidoYorokuCyugakuKirikaeNendo)) {
                    rtn = Boolean.TRUE;
                } else if (!NumberUtils.isDigits(year)) {
                    rtn = Boolean.FALSE;
                } else {
                    rtn = new Boolean(Integer.parseInt(year) >= Integer.parseInt(_seitoSidoYorokuCyugakuKirikaeNendo));
                }
                if (param._isOutputDebug) {
                    log.info(" kirikaeRegd = " + _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear + ", check year = " + year + ", kirikaeNendo = " + _seitoSidoYorokuCyugakuKirikaeNendo + ", print? = " + rtn);
                }
                _yearLimitCache.put(year, rtn);
            }
            return _yearLimitCache.get(year).booleanValue();
        }

        public static TreeSet<String> gakusekiYearSet(final List<Gakuseki> gakusekiList) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        /**
         * 印刷する生徒情報
         */
        private List<PersonalInfo> getPrintSchregEntGrdHistList(final Param param) {
            final List<PersonalInfo> rtn = new ArrayList<PersonalInfo>();
            if (_schregEntGrdHistComebackDatList.size() == 0) {
                return Collections.singletonList(_personalInfo);
            }
            final List<PersonalInfo> personalInfoList = new ArrayList<PersonalInfo>();
            personalInfoList.addAll(_schregEntGrdHistComebackDatList);
            personalInfoList.add(_personalInfo);
            for (final PersonalInfo personalInfo : personalInfoList) {
//              // 復学が同一年度の場合、復学前、復学後を表示
//              // 復学が同一年度ではない場合、復学後のみ表示
//              final int begin = personalInfo.getYearBegin();
//              final int end = personalInfo.getYearEnd(param);
//              if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
//                  rtn.add(personalInfo);
//              }
                rtn.add(personalInfo);
            }
            return rtn;
        }

        public boolean yearIsNotInPersonalInfoRangeYear(final PersonalInfo pi, final Param param, final String year) {
            return null != year && !(pi.getYearBegin() <= Integer.parseInt(year) && Integer.parseInt(year) <= getPersonalInfoYearEnd(pi, param));
        }

        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getPersonalInfoYearEnd(final PersonalInfo target, final Param param) {
            final TreeSet<Integer> yearSetAll = new TreeSet<Integer>();
            final List<PersonalInfo> personalInfoList = getPrintSchregEntGrdHistList(param);
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

        public static List<String> printClassSubclassKeyList(final List<ClassView> classViewList, final List<ValueRecord> valueRecordList) {
            final Set<String> printClassSet = new TreeSet<String>();
            for (final ClassView classView : classViewList) {
                for (final ViewSubclass viewSubclass : classView._viewSubclassList) {
                    if (!"1".equals(classView._electdiv)) {
                        printClassSet.add(classView._classcd + ":" + viewSubclass._subclasscd);
                    } else {
                        for (final View view : viewSubclass._viewList) {
                            for (final ViewStatus viewStatus : view._yearViewMap.values()) {
                                if (null != viewStatus._status) {
                                    printClassSet.add(classView._classcd + ":" + viewSubclass._subclasscd);
                                }
                            }
                        }
                    }
                }
            }
            for (final ValueRecord valueRecord : valueRecordList) {
                if (!"1".equals(valueRecord._classMstElectDiv)) {
                    printClassSet.add(valueRecord._classcd + ":" + valueRecord._subclasscd);
                } else {
                    if (null != valueRecord._value) {
                        printClassSet.add(valueRecord._classcd + ":" + valueRecord._subclasscd);
                    }
                }
            }
            return new ArrayList<String>(printClassSet);
        }

        private List<ValueRecord> getPageYearsValueRecordList(final List<String> pageYears) {
            final List<ValueRecord> rtn = new ArrayList<ValueRecord>();
            for (final ValueRecord vr : _valueRecordList) {
                if (null == pageYears || pageYears.contains(vr._year)) {
                    rtn.add(vr);
                }
            }
            return rtn;
        }

        private List<ValueRecord> getValueRecordList(final List<String> pageYears) {
            final List<ValueRecord> rtn = new ArrayList<ValueRecord>();
            final List<ValueRecord> valueRecordList = getPageYearsValueRecordList(pageYears);
            final List<String> printClassSubclassKeyList = printClassSubclassKeyList(getClassViewList(pageYears), valueRecordList);
            for (final ValueRecord vr : valueRecordList) {
                if (printClassSubclassKeyList.contains(vr._classcd + ":" + vr._subclasscd)) {
                    rtn.add(vr);
                }
            }
            return rtn;
        }

        private static void setSchregEntGrdHistComebackDat(final DB2UDB db2, final Param param, final List<Student> studentList) {
            for (final Student student : studentList) {
                student._schregEntGrdHistComebackDatList = Collections.EMPTY_LIST;
            }
            if (!param._hasSchregEntGrdHistComebackDat) {
                return;
            }
            PreparedStatement ps = null;
            final Map<String, List<String>> schregComebackDateMap = new HashMap<String, List<String>>();
            try {
                final String sql =
                        " SELECT T1.* "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' "
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
        final String _finishDateFormatted;
        final String _installationDiv;
        final String _jName;
        final String _finschoolTypeName;
        final String _finschoolNotPrintSotsugyo;

        final String _entYear;
        private Semester _entSemester;
        final String _entDate;
        final String _entReason;
        final String _entSchool;
        final String _entAddr;
        final Integer _entDiv;
        final String _entDivName;
        final String _grdYear;
        private Semester _grdSemester;
        final String _grdDate;
        final String _grdReason;
        final String _grdSchool;
        final String _grdAddr;
        final String _grdNo;
        final Integer _grdDiv;
        final String _grdDivName;
        final String _curriculumYear;
        final String _tengakuSakiGrade;
        final String _tengakuSakiZenjitu;
        final String _nyugakumaeSyussinJouhou;
        String _comebackDate;

        List<Map<String, String>> _addressRowList;
        List<Address> _addressList;
        List<Map<String, String>> _guardianAddressRowList;
        List<Address> _guardianAddressList;
        List<Gakuseki> _gakusekiList;
        List<Map<String, String>> _transferInfoList;
        Map<String, List<KNJA134J_3.ClassRemark>> _gradecdClassRemarkListMap;

        /**
         * コンストラクタ。
         */
        public PersonalInfo(
                final DB2UDB db2,
                final Param param,
                final Map baseRow,
                final Student student,
                final Map entGrdHistRow,
                final String grdDate,
                final Integer grdDiv
        ) {
            _student = student;
            _entYear    = KnjDbUtils.getString(entGrdHistRow, "ENT_YEAR");
            _entSemester = Semester.get(param, _entYear, KnjDbUtils.getString(entGrdHistRow, "ENT_SEMESTER"));
            _entDate    = KnjDbUtils.getString(entGrdHistRow, "ENT_DATE");
            _entReason  = KnjDbUtils.getString(entGrdHistRow, "ENT_REASON");
            _entSchool  = KnjDbUtils.getString(entGrdHistRow, "ENT_SCHOOL");
            _entAddr    = KnjDbUtils.getString(entGrdHistRow, "ENT_ADDR");
            _entDiv     = StringUtils.isNumeric(KnjDbUtils.getString(entGrdHistRow, "ENT_DIV")) ? Integer.valueOf(KnjDbUtils.getString(entGrdHistRow, "ENT_DIV")) : null;
            _entDivName = KnjDbUtils.getString(entGrdHistRow, "ENT_DIV_NAME");
            _grdYear    = KnjDbUtils.getString(entGrdHistRow, "GRD_YEAR");
            _grdSemester = Semester.get(param, _grdYear, KnjDbUtils.getString(entGrdHistRow, "GRD_SEMESTER"));
            _grdDate    = grdDate;
            _grdDiv     = grdDiv;
            _grdDivName = KnjDbUtils.getString(entGrdHistRow, "GRD_DIV_NAME");
            _grdReason  = KnjDbUtils.getString(entGrdHistRow, "GRD_REASON");
            _grdSchool  = KnjDbUtils.getString(entGrdHistRow, "GRD_SCHOOL");
            _grdAddr    = KnjDbUtils.getString(entGrdHistRow, "GRD_ADDR");
            _grdNo      = KnjDbUtils.getString(entGrdHistRow, "GRD_NO");

            _curriculumYear = KnjDbUtils.getString(entGrdHistRow, "CURRICULUM_YEAR");
            _tengakuSakiZenjitu = KnjDbUtils.getString(entGrdHistRow, "TENGAKU_SAKI_ZENJITU");
            _tengakuSakiGrade = KnjDbUtils.getString(entGrdHistRow, "TENGAKU_SAKI_GRADE");
            _nyugakumaeSyussinJouhou = KnjDbUtils.getString(entGrdHistRow, "NYUGAKUMAE_SYUSSIN_JOUHOU");

            Cond issuedateInRegdStart = getHistCond(param);
            if (param._isOutputDebug) {
                log.info(" histDateFilter issuedateInRegdStart = " + issuedateInRegdStart.toSql());
            }

            //final Cond histFilterCond = Cond.field("EXPIREDATE").isNull().or(Cond.field("EXPIREDATE").lessEqual(StringUtils.defaultString(_grdDate, param._histApplyExpireDate)));
            final Cond histFilterCond = Cond.val(StringUtils.defaultString(_grdDate, param._histApplyExpireDate)).between(Cond.field("ISSUEDATE"), Cond.field("EXPIREDATE"));
            if (param._isOutputDebug) {
                param.logOnce(" histDateFilter histFilterCond = " + histFilterCond.toSql());
            }
            {
                String name = StringUtils.defaultString(KnjDbUtils.getString(student._schregBaseMstRow, "NAME"));
                String nameKana = StringUtils.defaultString(KnjDbUtils.getString(student._schregBaseMstRow, "NAME_KANA"));

                String nameHistFirst = null;
                final List<Map<String, String>> histAllList = filter(student._schregBaseHistDatList, issuedateInRegdStart);
                final List<Map<String, String>> nameHistList = filter(histAllList, Cond.field("NAME_FLG").eq(Cond.val("1")));

                if (nameHistList.isEmpty()) {
                    log.debug(" schreg_base_hist_dat (name_flg) empty.");
                } else {
                    final Map histFirstRow = KnjDbUtils.firstRow(nameHistList);
                    nameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(histFirstRow, "NAME"));


                    final Map<String, String> histLastRow = KnjDbUtils.lastRow(filter(histAllList, histFilterCond));
                    final String nameHistLast = StringUtils.defaultString(KnjDbUtils.getString(histLastRow, "NAME"));
                    if (!StringUtils.isBlank(nameHistLast) && !nameHistLast.equals(name)) {
                        // SCHREG_BASE_MST.NAMEは最新の値。卒業後に変更されているかもしれないのでその際は卒業時点の氏名を表示する。以下同様
                        name = nameHistLast;
                        nameKana = StringUtils.defaultString(KnjDbUtils.getString(histLastRow, "NAME_KANA"));
                        if (param._isOutputDebug) {
                            log.info(" set (name, nameKana) from hist = (" + name + ", " + nameKana + ")");
                        }
                    }
                }

                _useRealName = "1".equals(KnjDbUtils.getString(baseRow, "USE_REAL_NAME")) || "1".equals(param.property(Property.certifPrintRealName));
                _nameOutputFlg = "1".equals(KnjDbUtils.getString(baseRow, "NAME_OUTPUT_FLG"));

                if (_useRealName) {
                    String realName = StringUtils.defaultString(KnjDbUtils.getString(student._schregBaseMstRow, "REAL_NAME"));
                    String realNameKana = StringUtils.defaultString(KnjDbUtils.getString(student._schregBaseMstRow, "REAL_NAME_KANA"));
                    String realNameHistFirst = null;
                    String nameWithRealNameHistFirst = null;

                    final List<Map<String, String>> realNameHistList = filter(student._schregBaseHistDatList, Cond.field("REAL_NAME_FLG").eq(Cond.val("1")).and(issuedateInRegdStart));
                    if (!realNameHistList.isEmpty()) {
                        final Map realNameHistFirstRow = KnjDbUtils.firstRow(realNameHistList);
                        realNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(realNameHistFirstRow, "REAL_NAME"));
                        nameWithRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(realNameHistFirstRow, "NAME"));

                        final Map<String, String> histLastRow = KnjDbUtils.lastRow(filter(histAllList, histFilterCond));
                        String realNameHistLast = StringUtils.defaultString(KnjDbUtils.getString(histLastRow, "REAL_NAME"));
                        if (!StringUtils.isBlank(realNameHistLast) && !realNameHistLast.equals(realName)) {
                            realName = realNameHistLast;
                            realNameKana = StringUtils.defaultString(KnjDbUtils.getString(histLastRow, "REAL_NAME_KANA"));
                            name = StringUtils.defaultString(KnjDbUtils.getString(histLastRow, "NAME"));
                            nameKana = StringUtils.defaultString(KnjDbUtils.getString(histLastRow, "NAME_KANA"));
                            if (param._isOutputDebug) {
                                log.info(" set (realName, realNameKana, name, nameKana) from hist = (" + realName + ", " + realNameKana + ", " + name + ", " + nameKana + ")");
                            }
                        }
                    }
                    if (param._isOutputDebug) {
                        log.info(" set (realName, realNameKana, name, nameKana) from hist = (" + realName + ", " + realNameKana + ", " + name + ", " + nameKana + ")");
                    }
                    _studentName          = name;
                    _studentRealName      = realName;
                    _studentKana          = getPrintName(_useRealName, _nameOutputFlg, realNameKana, nameKana);
                    _studentName1         = getPrintName(_useRealName, _nameOutputFlg, realName, name);
                    _studentNameHistFirst = getPrintName(_useRealName, _nameOutputFlg, realNameHistFirst, nameWithRealNameHistFirst);
                } else {
                    _studentName          = name;
                    _studentRealName      = null;
                    _studentKana          = getPrintName(_useRealName, _nameOutputFlg, null, nameKana);
                    _studentName1         = getPrintName(_useRealName, _nameOutputFlg, null, name);
                    _studentNameHistFirst = getPrintName(_useRealName, _nameOutputFlg, null, nameHistFirst);
                }
            }

            {
                String guardName = StringUtils.defaultString(KnjDbUtils.getString(student._guardianDatRow, "GUARD_NAME"));
                String guardKana = StringUtils.defaultString(KnjDbUtils.getString(student._guardianDatRow, "GUARD_KANA"));

                String guardNameHistFirst = null;
                final List<Map<String, String>> guardHistList = filter(student._guardianHistDatList, Cond.field("GUARD_NAME_FLG").eq(Cond.val("1")).and(issuedateInRegdStart));
                if (!guardHistList.isEmpty()) {
                    final Map<String, String> firstRow = KnjDbUtils.firstRow(guardHistList);
                    guardNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(firstRow, "GUARD_NAME"));

                    final Map lastRow = KnjDbUtils.lastRow(filter(guardHistList, histFilterCond));
                    final String guardNameHistLast = StringUtils.defaultString(KnjDbUtils.getString(lastRow, "GUARD_NAME"));
                    if (!StringUtils.isBlank(guardNameHistLast) && !guardNameHistLast.equals(guardName)) {
                        guardName = guardNameHistLast;
                        guardKana = StringUtils.defaultString(KnjDbUtils.getString(lastRow, "GUARD_KANA"));
                        if (param._isOutputDebug) {
                            log.info(" set (guardName, guardKana) from hist = (" + guardName + ", " + guardKana + ")");
                        }
                    }
                }

                final boolean useGuardRealName = "1".equals(KnjDbUtils.getString(baseRow, "USE_GUARD_REAL_NAME")) || "1".equals(param.property(Property.certifPrintRealName));
                final boolean guardNameOutputFlg = "1".equals(KnjDbUtils.getString(baseRow, "GUARD_NAME_OUTPUT_FLG"));
                if (useGuardRealName) {
                    String guardRealKana = StringUtils.defaultString(KnjDbUtils.getString(student._guardianDatRow, "GUARD_REAL_KANA"));
                    String guardRealName = StringUtils.defaultString(KnjDbUtils.getString(student._guardianDatRow, "GUARD_REAL_NAME"));
                    String guardRealNameHistFirst = null;
                    String guardNameWithGuardRealNameHistFirst = null;

                    final List<Map<String, String>> guardRealNameHistList = filter(student._guardianHistDatList, Cond.field("GUARD_REAL_NAME_FLG").eq(Cond.val("1")).and(issuedateInRegdStart));
                    if (!guardRealNameHistList.isEmpty()) {
                        final Map firstRow = KnjDbUtils.firstRow(guardRealNameHistList);
                        guardRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(firstRow, "GUARD_REAL_NAME"));
                        guardNameWithGuardRealNameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(firstRow, "GUARD_NAME"));

                        final Map lastRow = KnjDbUtils.lastRow(filter(guardRealNameHistList, histFilterCond));
                        final String guardRealNameHistLast = StringUtils.defaultString(KnjDbUtils.getString(lastRow, "GUARD_REAL_NAME"));
                        if (!StringUtils.isBlank(guardRealNameHistLast) && guardRealNameHistLast.equals(guardRealName)) {
                            guardRealKana = StringUtils.defaultString(KnjDbUtils.getString(lastRow, "GUARD_REAL_KANA"));
                            guardRealName = guardRealNameHistLast;
                            guardKana = StringUtils.defaultString(KnjDbUtils.getString(lastRow, "GUARD_KANA"));
                            guardName = StringUtils.defaultString(KnjDbUtils.getString(lastRow, "GUARD_NAME"));
                            if (param._isOutputDebug) {
                                log.info(" set (guardRealName, guardRealKana, guardName, guardKana) from hist = (" + guardRealName + ", " + guardRealKana + ", " + guardName + ", " + guardKana + ")");
                            }
                        }
                    }
                    _guardKana          = getPrintName(useGuardRealName, guardNameOutputFlg, guardRealKana, guardKana);
                    _guardName          = getPrintName(useGuardRealName, guardNameOutputFlg, guardRealName, guardName);
                    _guardNameHistFirst = getPrintName(useGuardRealName, guardNameOutputFlg, guardRealNameHistFirst, guardNameWithGuardRealNameHistFirst);
                } else {
                    _guardKana          = getPrintName(useGuardRealName, guardNameOutputFlg, null, guardKana);
                    _guardName          = getPrintName(useGuardRealName, guardNameOutputFlg, null, guardName);
                    _guardNameHistFirst = getPrintName(useGuardRealName, guardNameOutputFlg, null, guardNameHistFirst);
                }
            }

            _courseName = KnjDbUtils.getString(baseRow, "COURSENAME");
            _majorName = KnjDbUtils.getString(baseRow, "MAJORNAME");

            _birthdayFlg = KnjDbUtils.getString(baseRow, "BIRTHDAY_FLG");
            _birthday = KnjDbUtils.getString(baseRow, "BIRTHDAY");
            _birthdayStr = getBirthday(db2, _birthday, _birthdayFlg, param);
            _sex = KnjDbUtils.getString(baseRow, "SEX");
            final String finishDate = KnjDbUtils.getString(baseRow, "FINISH_DATE");
            _finishDateFormatted = param._isSeireki ? KNJ_EditDate.h_format_Seireki_M(finishDate) : KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP_M(db2, finishDate), param._year);
            _installationDiv = KnjDbUtils.getString(baseRow, "INSTALLATION_DIV");
            _jName = KnjDbUtils.getString(baseRow, "J_NAME");
            _finschoolTypeName = KnjDbUtils.getString(baseRow, "FINSCHOOL_TYPE_NAME");
            _finschoolNotPrintSotsugyo = KnjDbUtils.getString(baseRow, "FINSCHOOL_NOT_PRINT_SOTSUGYO");
        }

        private Cond getHistCond(final Param param) {
            Cond issuedateInRegdStart = (Cond.field("ISSUEDATE").between(Cond.val(_entDate), Cond.val(StringUtils.defaultString(_grdDate, param._histApplyExpireDate))))
                    .or(Cond.field("ISSUEDATE").lessEqual(Cond.val(_entDate)).and(Cond.field("EXPIREDATE").isNull().or(Cond.field("EXPIREDATE").greaterEqual(Cond.val(_entDate)))));
            if (null == _entDate) {
                // 対象は全て
                issuedateInRegdStart = new Cond.True();
            }
            return issuedateInRegdStart;
        }

        private static String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param) {
            if (StringUtils.isBlank(date)) {
                return setDateFormat2(param, "　  年  月  日");
            }
            String birthday = "";
            if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
                birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + setDateFormat2(param, KNJ_EditDate.h_format_JP_MD(date));
            } else {
                birthday = setDateFormat2(param, h_format_JP(db2, param, date));
            }
            if (!StringUtils.isBlank(birthday)) {
                birthday += "生";
            }
            return birthday;
        }

        private static String getPrintName(final boolean useRealName, final boolean outputFlg, final String realName, final String name) {
            final String rtn;
            if (useRealName && outputFlg && !StringUtils.isBlank(realName) && !StringUtils.isBlank(name)) {
                rtn = realName + "（" + name + "）";
            } else if (useRealName && !StringUtils.isBlank(realName)) {
                rtn = realName;
            } else {
                rtn = name;
            }
            return StringUtils.defaultString(rtn);
        }

        public int getYearBegin() {
            return null == _entDate ? 0 : getCalendarNendo(getCalendarOfDate(_entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _grdDate ? 9999 : getCalendarNendo(getCalendarOfDate(_grdDate)));
        }

        /**
         * 生徒情報を得る
         */
        public static PersonalInfo load(final DB2UDB db2, final Param param, final Student student, final String comebackDate) {
            final Map<String, String> entGrdHistRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, sql_state(param, student._schregno, comebackDate)));
            final Integer grdDiv;
            final String grdDate;
            if (null == KnjDbUtils.getString(entGrdHistRow, "GRD_DIV") && null == KnjDbUtils.getString(entGrdHistRow, "GRD_DATE")) {
                final String psKey = "PS_SCHREG_BASE_YEAR_DETAIL_MST_007";
                if (!param._psMap.containsKey(psKey)) {
                    final StringBuffer shuryoSql = new StringBuffer();
                    shuryoSql.append(" SELECT SCM.GRADUATE_DATE ");
                    shuryoSql.append(" FROM SCHREG_BASE_YEAR_DETAIL_MST T1 ");
                    shuryoSql.append(" INNER JOIN SCHOOL_MST SCM ON SCM.YEAR = T1.YEAR ");
                    if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                        shuryoSql.append("   AND SCM.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
                    }
                    shuryoSql.append(" WHERE T1.YEAR = '" + param._year + "' ");
                    shuryoSql.append("   AND T1.BASE_SEQ = '007' ");
                    shuryoSql.append("   AND T1.BASE_REMARK1 = '1' "); // KNJA032の処理
                    shuryoSql.append("   AND T1.SCHREGNO = ? ");
                    param.setPs(db2, psKey, shuryoSql.toString());
                }
                final Map<String, String> shuryoRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno}));
                if (shuryoRow.isEmpty()) {
                    grdDate = null;
                    grdDiv = null;
                } else {
                    grdDate = KnjDbUtils.getString(shuryoRow, "GRADUATE_DATE");
                    grdDiv = 5;
                }
            } else {
                grdDate = KnjDbUtils.getString(entGrdHistRow, "GRD_DATE");
                grdDiv = StringUtils.isNumeric(KnjDbUtils.getString(entGrdHistRow, "GRD_DIV")) ? Integer.valueOf(KnjDbUtils.getString(entGrdHistRow, "GRD_DIV")) : null;
            }

            final Map<String, String> baseRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, sql_info_reg(param, student._schregno, comebackDate)));

            if (param._isOutputDebug) {
                log.info(" schregno = " + student._schregno + ", " + Util.debugMapToStr("entGrdHistRow = ", entGrdHistRow, ", ") + ", " + Util.debugMapToStr(" baseRow = ", baseRow, ", "));
            }

            final PersonalInfo personalInfo = new PersonalInfo(db2, param, baseRow, student, entGrdHistRow, grdDate, grdDiv);

            personalInfo._addressRowList = Address.getRowList(db2, param, false, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._addressList = Address.load(personalInfo._addressRowList);
            personalInfo._guardianAddressRowList = Address.getRowList(db2, param, true, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._guardianAddressList = Address.load(personalInfo._guardianAddressRowList);
            personalInfo._comebackDate = comebackDate;
            personalInfo._gakusekiList = Gakuseki.load(db2, param, student, personalInfo._grdDate);
            personalInfo._gradecdClassRemarkListMap = KNJA134J_3.ClassRemark.loadGradecdClassRemarkListMap(db2, param, student);
            personalInfo._transferInfoList = loadTransferList(db2, param, student, personalInfo._entDate, personalInfo._grdDate);
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
            sql.append(" ORDER BY TRANSFER_SDATE ");
            transferList.addAll(KnjDbUtils.query(db2, sql.toString()));
            return transferList;
        }

        public static String sql_info_reg(final Param param, final String schregno, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();
            final String switch6 = "1";
            sql.append("SELECT ");
            sql.append("  BASE.BIRTHDAY, Z002.ABBV1 AS SEX,");
            sql.append("  T21.BIRTHDAY_FLG, ");
            sql.append("  REGD.GRADE, REGD.ATTENDNO, REGD.ANNUAL,");
            // 課程・学科・コース
            sql.append("  CM.COURSENAME, MJM.MAJORNAME, CCM.COURSECODENAME,");
            // 卒業情報
            sql.append("  EGHIST.FINISH_DATE,");
            sql.append("  FIN_S.FINSCHOOL_NAME AS J_NAME,");
            sql.append("  NML001.NAME1 AS INSTALLATION_DIV,");
            sql.append("  VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            if (param._hasFINSCHOOL_DETAIL_MST) {
                sql.append("  FIN_SD003.REMARK1 AS FINSCHOOL_NOT_PRINT_SOTSUGYO,");
            } else {
                sql.append("  CAST(NULL AS VARCHAR(1)) AS FINSCHOOL_NOT_PRINT_SOTSUGYO,");
            }
            sql.append("  (CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("  T11.NAME_OUTPUT_FLG, ");
            sql.append("  (CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("  T26.GUARD_NAME_OUTPUT_FLG, ");
            sql.append("  REGD.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("  (   SELECT     * ");
            sql.append("  FROM       SCHREG_REGD_DAT T1 ");
            sql.append("  WHERE      T1.SCHREGNO = '" + schregno + "' AND T1.YEAR = '" + param._year + "' ");
            if (switch6.equals("1")) { // 学期を特定
                sql.append("  AND T1.SEMESTER = '" + param._gakki + "' ");
            } else {
                // 最終学期
                sql.append("  AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + schregno + "' AND YEAR = '" + param._year + "')");
            }
            sql.append("  ) REGD ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            sql.append("LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = BASE.SEX ");
            sql.append("LEFT JOIN ");
            if (null != comebackDate) {
                sql.append(" SCHREG_ENT_GRD_HIST_COMEBACK_DAT EGHIST ON EGHIST.COMEBACK_DATE = '" + comebackDate + "' AND ");
            } else {
                sql.append(" SCHREG_ENT_GRD_HIST_DAT EGHIST ON ");
            }
            sql.append("    EGHIST.SCHREGNO = REGD.SCHREGNO AND EGHIST.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = EGHIST.FINSCHOOLCD ");
            if (param._hasFINSCHOOL_DETAIL_MST) {
                sql.append("  LEFT JOIN FINSCHOOL_DETAIL_MST FIN_SD003 ON FIN_SD003.FINSCHOOLCD = EGHIST.FINSCHOOLCD AND FIN_SD003.FINSCHOOL_SEQ = '003' ");
            }
            sql.append("LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST MJM ON MJM.COURSECD = REGD.COURSECD AND MJM.MAJORCD = REGD.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST CCM ON CCM.YEAR = REGD.YEAR AND VALUE(CCM.COURSECODE, '0000') = VALUE(REGD.COURSECODE, '0000')");
            // 生徒住所
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '02' ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = BASE.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");

            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = BASE.SCHREGNO AND T26.DIV = '02' ");
            return sql.toString();
        }

        private static String sql_state(final Param param, final String schregno, final String comebackDate) {
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
            sql.append("    T1.SCHREGNO = '" + schregno + "' AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (null != comebackDate) {
                sql.append("    AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
            }
            return sql.toString();
        }

        public Address getStudentAddressMax() {
            return _addressList == null || _addressList.isEmpty() ? null : _addressList.get(0);
        }
    }

    /**
     * 在籍データ
     */
    private static class Gakuseki {

        final int _i;
        final String _year;
        final String _grade;
        final int _gradeCd;
        final String _hrname;
        final String _attendno;
        final String _nendo;
        final String _nendoFormatted;
        final Staff _principal;
        final String _staffSeq;
        final String _principalSeq;
        final String _kaizanFlg;

        public Gakuseki(
                final int i,
                final String year,
                final String grade,
                final int gradeCd,
                final String hrname,
                final String attendno,
                final String nendo,
                final String nendoFormatted,
                final Staff principal,
                final String chageOpiSeq,
                final String lastOpiSeq,
                final String flg) {
            _i = i;
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrname = hrname;
            _attendno = attendno;
            _nendo = nendo;
            _nendoFormatted = nendoFormatted;
            _staffSeq = chageOpiSeq;
            _principalSeq = lastOpiSeq;
            _kaizanFlg = flg;
            _principal = principal;
        }

        /**
         * 在籍データのリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<Gakuseki> load(final DB2UDB db2, final Param param, final Student student, final String grdDate) {
            final List<Gakuseki> gakusekiList = new ArrayList<Gakuseki>();
            try {
                if (param._hmap == null && !"1".equals(param.property(Property.useSchregRegdHdat))) {
                    param._hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_2 error!", ex);
            } finally {
                db2.commit();
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

                param.setPs(db2, psKey1, stb.toString());
            }
            final Map<String, Map<String, String>> yearAttestMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, param.getPs(psKey1), new Object[] {student._schregno}), "YEAR");

            final String sql = sqlSchGradeRec(param, student, grdDate);
            if (param._isOutputDebugQuery) {
                log.info(" sch grade sql = " + sql);
            }

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");

                final String principalname = KnjDbUtils.getString(row, "PRINCIPALNAME");
                final int i = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), grade); // 学年
                final int gradeCd = param.getGradeCd(KnjDbUtils.getString(row, "YEAR"), grade); // 学年

                String hrname = null;
                if (param.isTokubetsuShien() && "1".equals(param._seitoSidoYorokuUseHrClassTypeJ) && "2".equals(param._hrClassType) && param._z010.in(Z010.sagaken)) {
                    hrname = KnjDbUtils.getString(row, "GHR_NAME");
                } else {
                    if ("1".equals(param.property(Property.useSchregRegdHdat))) {
                        hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    } else if ("0".equals(param.property(Property.useSchregRegdHdat))) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrClass, param._hmap);
                    }
                    if (hrname == null) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrClass);
                    }
                }

                final String nendo = param.getNendo(db2, year) + "年度";

                final String nendoDate;
                if (param._useGengoInApr01) {
                    nendoDate = year + "-04-01"; // 年度開始日
                } else {
                    nendoDate = String.valueOf(Integer.parseInt(year) + 1) + "-03-31"; // 年度終了日
                }
                final String nendoFormatted = param.dateNendo(db2, nendoDate);

                final Staff principal = new Staff(year, new StaffMst(null, principalname, null, null, null), null, null, param._staffInfo.getStampNo(param, KnjDbUtils.getString(row, "PRINCIPALSTAFFCD"), year));

                final Map<String, String> attest = getMappedMap(yearAttestMap, year);
                final String chageOpiSeq = KnjDbUtils.getString(attest, "CHAGE_OPI_SEQ");
                final String lastOpiSeq = KnjDbUtils.getString(attest, "LAST_OPI_SEQ");
                final String flg = KnjDbUtils.getString(attest, "FLG");

                final Gakuseki gakuseki = new Gakuseki(i, year, grade, gradeCd, hrname, attendno, nendo, nendoFormatted, principal, chageOpiSeq, lastOpiSeq, flg);
                gakusekiList.add(gakuseki);

            }
            return gakusekiList;
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param, final Student student, final String grdDate) {
            final String certifKind = "108";
            final boolean useGhrName = param.isTokubetsuShien() && "1".equals(param._seitoSidoYorokuUseHrClassTypeJ) && "2".equals(param._hrClassType) && param._z010.in(Z010.sagaken);
            final StringBuffer stb = new StringBuffer();
            // 印鑑関連 1
            stb.append(" WITH YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MAX(SEMESTER) AS SEMESTER ");
            stb.append("     FROM SCHREG_REGD_DAT ");
            stb.append("     WHERE SCHREGNO = '" + student._schregno + "' ");
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
                stb.append("     WHERE T1.SCHREGNO = '" + student._schregno + "' ");
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
                stb.append(" , GHR_YEAR_SEMESTER AS ( ");
                stb.append("     SELECT ");
                stb.append("     YEAR, ");
                stb.append("     MAX(SEMESTER) AS SEMESTER ");
                stb.append("     FROM SCHREG_REGD_GHR_DAT ");
                stb.append("     WHERE SCHREGNO = '" + student._schregno + "' ");
                stb.append("     GROUP BY YEAR ");
                if (null != grdDate) {
                    stb.append("     UNION ALL ");
                    stb.append("     SELECT ");
                    stb.append("     T1.YEAR, ");
                    stb.append("     T1.SEMESTER ");
                    stb.append("     FROM SCHREG_REGD_GHR_DAT T1 ");
                    stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
                    stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
                    stb.append("         AND '" + grdDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
                    stb.append("     WHERE T1.SCHREGNO = '" + student._schregno + "' ");
                }
                stb.append(" ) ");
                stb.append(" , GHR_MIN_YEAR_SEMESTER AS ( ");
                stb.append("     SELECT ");
                stb.append("     YEAR, ");
                stb.append("     MIN(SEMESTER) AS SEMESTER ");
                stb.append("     FROM GHR_YEAR_SEMESTER ");
                stb.append("     GROUP BY YEAR ");
                stb.append(" ) ");
            }

            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,REGDH.HR_NAME ");
            if ("1".equals(param.property(Property.useSchregRegdHdat))) {
                stb.append("         ,REGDH.HR_CLASS_NAME1");
            }
            if (useGhrName) {
                stb.append("   ,GHRH.GHR_NAME ");
            }
            stb.append("   ,T6.REMARK7 AS PRINCIPALSTAFFCD ");
            stb.append("   ,T6.PRINCIPAL_NAME AS PRINCIPALNAME ");

            stb.append(" FROM SCHREG_REGD_DAT T1");
            stb.append(" INNER JOIN MIN_YEAR_SEMESTER T1_2 ON T1_2.YEAR = T1.YEAR AND T1_2.SEMESTER = T1.SEMESTER ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR AND REGDH.SEMESTER = T1.SEMESTER ");
            stb.append("                                 AND REGDH.GRADE = T1.GRADE AND REGDH.HR_CLASS = T1.HR_CLASS ");
            if (useGhrName) {
                stb.append(" LEFT JOIN GHR_MIN_YEAR_SEMESTER T2 ON T2.YEAR = T1.YEAR ");
                stb.append(" LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = T1.SCHREGNO AND GHR.YEAR = T1.YEAR AND GHR.SEMESTER = T2.SEMESTER ");
                stb.append(" LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = T1.YEAR AND GHRH.SEMESTER = T2.SEMESTER ");
                stb.append("                                    AND GHRH.GHR_CD = GHR.GHR_CD ");
            }
            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '" + certifKind + "'");
            stb.append(" WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("     AND T1.YEAR <= '" + param._year + "'");
            stb.append("     AND T1.YEAR NOT IN ( ");
            stb.append("        SELECT N1.YEAR ");
            stb.append("        FROM SCHREG_REGD_DAT N1 ");
            stb.append("        INNER JOIN SCHREG_REGD_GDAT N2 ON N2.YEAR = N1.YEAR AND N2.GRADE = N1.GRADE ");
            stb.append("        WHERE N1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("          AND N2.SCHOOL_KIND <> '" + Param.SCHOOL_KIND + "' ");
            stb.append("        ) ");

            stb.append(" ORDER BY T1.HR_CLASS ");
            return stb.toString();
        }

//        public static boolean isDoutokuAri(final Gakuseki gakuseki) {
//            return NumberUtils.isDigits(gakuseki._year) && Integer.parseInt(gakuseki._year) >= Param.DOUTOKU_J_START_YEAR;
//        }

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
            _yearStaffNameSetUp = new HashMap<String, Map<String, String>>();
            _yearStaffNameHist = new HashMap<String, Map<String, String>>();
        }
        public boolean isPrintNameBoth(final String year) {
            final Map<String, String> nameSetup = _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year, final Param param) {
            final Map<String, String> nameSetup = _yearStaffNameSetUp.get(year);
            return null != nameSetup || "1".equals(param.property(Property.certifPrintRealName));
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
                        } else if (getMS932ByteLength(nameReal + n) > keta) {
                            nameLine =  new String[]{nameReal, n};
                        } else {
                            nameLine =  new String[]{nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year, param)) {
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
            Map<String, String> histDat = null;
            for (final Map<String, String> histDat0 : sortedMap.values()) {
                final String syear = histDat0.get("SYEAR");
                final String eyear = histDat0.get("EYEAR");
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
                rtn = StringUtils.isEmpty(histDat.get(nameField)) ? defVal : histDat.get(nameField);
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

        public static Map load(final DB2UDB db2, final Param param, final String year) {
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

            final String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT WHERE YEAR <= '" + year + "' AND DIV = '02' ";
            for (final Map<String, String> m : KnjDbUtils.query(db2, sql2)) {
                if (null == rtn.get(KnjDbUtils.getString(m, "STAFFCD"))) {
                    continue;
                }
                final StaffMst s = rtn.get(KnjDbUtils.getString(m, "STAFFCD"));

                final Map<String, String> nameSetupDat = new HashMap<String, String>();
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
            sqlHist.append("   FISCALYEAR(VALUE(T1.EDATE, '" + param._histApplyExpireDate + "')) AS EYEAR, ");
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
            return mkString(_staffMst.getNameLine(_year, param, keta), "").toString();
        }

        public List<String> getNameBetweenLine(final Param param, final int keta) {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";

            final List<String> rtn;
            if (getMS932ByteLength(getNameString(param, keta) + between) > keta) {
                rtn = Arrays.asList(getNameString(param, keta), between);
            } else {
                rtn = Arrays.asList(getNameString(param, keta) + between);
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

        private static List<Staff> getUniqueStaffList(final Staff ... staffs) {
            final List<Staff> rtn = new ArrayList<Staff>();
            final List<StaffMst> msts = new ArrayList<StaffMst>();
            for (final Staff staff : staffs) {
                if (staff == Staff.Null || rtn.contains(staff) || staff._staffMst == StaffMst.Null || msts.contains(staff._staffMst)) {
                    continue;
                }
                rtn.add(staff);
                msts.add(staff._staffMst);
            }
            return rtn;
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
        protected Map<String, List<Map<String, String>>> _yearPrincipalListMap = Collections.emptyMap();
        protected Map _staffMstMap = Collections.EMPTY_MAP;
        protected Map<String, TreeMap<String, List<Staff>>> _staffClassHistMap = Collections.emptyMap();

        private void setYearPrincipalMap(final DB2UDB db2, final Param param) {
            _yearPrincipalListMap = new TreeMap<String, List<Map<String, String>>>();

            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH PRINCIPAL_HIST AS ( ");
            sql.append("     SELECT ");
            sql.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR ORDER BY T2.YEAR, T1.FROM_DATE) AS ORDER ");
            sql.append("     FROM ");
            sql.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,SCHOOL_MST T2 ");
            sql.append("     WHERE ");
            sql.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '" + param._histApplyExpireDate + "')) ");
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
            sql.append(" GROUP BY ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   FISCALYEAR(T1.DATE) ");
            sql.append(" ORDER BY ");
            sql.append("   T1.STAFFCD, ");
            sql.append("   MAX(T1.STAMP_NO) ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                getMappedMap(_inkanMap, KnjDbUtils.getString(row, "STAFFCD")).put(KnjDbUtils.getInt(row, "YEAR", null), KnjDbUtils.getString(row, "STAMP_NO"));
            }
            if (param._isOutputDebugStaff) {
                log.info(" inkanMap = " + _inkanMap);
            }
        }

        private String getStampNo(final Param param, final String staffcd, final String year) {
            if (null == _inkanMap.get(staffcd) || !NumberUtils.isDigits(year)) {
                return null;
            }
            String stampNo = null;
            final Map<Integer, String> yearStampnoMap = getMappedMap(_inkanMap, staffcd);
            if (yearStampnoMap.size() == 1) {
                stampNo = yearStampnoMap.entrySet().iterator().next().getValue();
                return stampNo;
            }
            final Integer iYear = Integer.valueOf(year);
            for (final Map.Entry<Integer, String> e : yearStampnoMap.entrySet()) {
                final Integer inkanYear = e.getKey();
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
            stb.append("         VALUE(MAX(T1.TO_DATE), '" + param._histApplyExpireDate + "') AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR <= '" + maxYear + "' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.TR_DIV, T1.STAFFCD, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append("     ORDER BY T1.TR_DIV, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE, T1.STAFFCD ");
            final String sql = stb.toString();

            final Map<String, TreeMap<String, List<Staff>>> rtn = new HashMap<String, TreeMap<String, List<Staff>>>();
            for (final Map row : KnjDbUtils.query(db2, sql)) {
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
            final List<Staff> staffList = new ArrayList<Staff>();
            final TreeMap<String, List<Staff>> grhrStaffMap = getMappedTreeMap(_staffClassHistMap, staffClassHistKey(year, grade, hrClass, trDiv));
            for (final List<Staff> fromDateStaffList : grhrStaffMap.values()) {
                if (fromDateStaffList.size() > 0) {
                    staffList.add(fromDateStaffList.get(0)); // 同一日付なら最小職員コードの職員
                }
            }
            return staffList;
        }

        public List<Staff> getStudentStaffHistList(final Param param, final Student student, final PersonalInfo pInfo, final String trDiv, final String year) {
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
                            final Staff befStaff = (Staff) studentStaffHistList.get(i);
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
        public static List<Attend> load(final DB2UDB db2, final Param param, final Student student) {
            final List<Attend> attendRecordList = new ArrayList<Attend>();
            if (null == param.gakushu && null == param.koudo) {
                return attendRecordList;
            }
            final String psKey = "PS_ATTEND";
            if (null == param.getPs(psKey)) {
                final String sql = getAttendSql(param);
                param.setPs(db2, psKey, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno, param._year, })) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final int g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                final String lesson = KnjDbUtils.getString(row, "LESSON");
                final String suspendMourning = KnjDbUtils.getString(row, "SUSPEND_MOURNING");
                final String abroad = KnjDbUtils.getString(row, "ABROAD");
                final String requirePresent = KnjDbUtils.getString(row, "REQUIREPRESENT");
                final String present = KnjDbUtils.getString(row, "PRESENT");
                final String absent = KnjDbUtils.getString(row, "ABSENT");
                final String late = KnjDbUtils.getString(row, "LATE");
                final String early = KnjDbUtils.getString(row, "EARLY");

                final Attend attendRecord = new Attend(year, g, lesson, suspendMourning, abroad, requirePresent, present, absent, late, early);
                attendRecordList.add(attendRecord);
            }

            return attendRecordList;
        }

        /**
         *  priparedstatement作成  出欠の記録
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String getAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        SCHREG_ATTENDREC_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("        AND YEAR <= ? ");
            stb.append("   GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR, ");
            stb.append("        T1.ANNUAL, ");
            stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._z010.in(Z010.KINJUNIOR)) {
                stb.append("              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            } else {
                stb.append("              THEN VALUE(CLASSDAYS,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            }
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
            stb.append(        "VALUE(LATE,0) AS LATE, ");
            stb.append(        "VALUE(EARLY,0) AS EARLY ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append(        "INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.ANNUAL = T1.ANNUAL ");
            stb.append(        "LEFT JOIN (SELECT SCHREGNO, YEAR, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
            stb.append(        "           FROM ATTEND_SEMES_DAT ");
            stb.append(        "           GROUP BY SCHREGNO, YEAR ");
            stb.append(        "           ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR ");
            stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append(        " AND S1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
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
            final String sql = isGuardian ? sqlAddress(true, param, startDate, endDate) : sqlAddress(false, param, startDate, endDate);
            return KnjDbUtils.query(db2, sql, new String[] { student._schregno, param._year, });
        }

        public static String sqlAddress(final boolean isGuardianAddress, final Param param, final String startDate, final String endDate) {

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
                stb.append("          OR VALUE(EXPIREDATE, '" + param._histApplyExpireDate + "') BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR ISSUEDATE <= '" + startDate + "' AND '" + endDate + "' <= VALUE(EXPIREDATE, '" + param._histApplyExpireDate + "') ");
                stb.append("          OR '" + startDate + "' <= ISSUEDATE AND VALUE(EXPIREDATE, '" + param._histApplyExpireDate + "') <= '" + endDate + "' ) ");
            } else if (null != startDate && null == endDate) {
                stb.append("       AND ('" + startDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '" + param._histApplyExpireDate + "') OR '" + startDate + "' <= ISSUEDATE) ");
            } else if (null == startDate && null != endDate) {
                stb.append("       AND ('" + endDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '" + param._histApplyExpireDate + "') OR ISSUEDATE <= '" + endDate + "') ");
            }
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE DESC ");
            return stb.toString();
        }
        public String toString() {
            return "AddressRec(" + _issuedate + "," + _address1 + " " + _address2 + ")";
        }
    }

    /**
     * 所見データ
     */
    private static class HtrainremarkHdat {
        String _totalstudyact;
        String _totalstudyval;
        String _detail2HDatSeq001Remark1; // 入学時の障害の状態

        public static HtrainremarkHdat load(final DB2UDB db2, final Param param, final String schregno) {
            HtrainremarkHdat htrainremarkHdat = new HtrainremarkHdat();
            if (null == param.gakushu && null == param.koudo) {
                return htrainremarkHdat;
            }
            final String psKey = "PS_HRHD";
            if (null == param._psMap.get(psKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT ");
                stb.append("    TOTALSTUDYACT ");
                stb.append("   ,TOTALSTUDYVAL ");
                stb.append("FROM    HTRAINREMARK_HDAT T1 ");
                stb.append("WHERE   SCHREGNO = ? ");

                param.setPs(db2, psKey, stb.toString());
            }
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { schregno, })) {

                htrainremarkHdat._totalstudyact = KnjDbUtils.getString(row, "TOTALSTUDYACT");
                htrainremarkHdat._totalstudyval = KnjDbUtils.getString(row, "TOTALSTUDYVAL");
            }
            if (param.isTokubetsuShien()) {
                final String psKey2 = "PS_HRHD_DET2";
                if (null == param._psMap.get(psKey2)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT ");
                    stb.append("    REMARK1 ");
                    stb.append("FROM    HTRAINREMARK_DETAIL2_HDAT T1 ");
                    stb.append("WHERE   SCHREGNO = ? ");
                    stb.append("    AND HTRAIN_SEQ = ? ");

                    param.setPs(db2, psKey2, stb.toString());
                }

                htrainremarkHdat._detail2HDatSeq001Remark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] { schregno, "001"}));
            }
            return htrainremarkHdat;
        }
    }

    /**
     * 総合的な学習の時間の記録・外国語活動の記録
     */
    private static class HTrainRemarkDat {
        final String _year;
        int _g;
        String _totalstudyact;
        String _totalstudyval;
        String _specialActRemark;
        String _totalRemark;
        String _attendrecRemark;
        String _viewremark;
        String _classact;
        String _studentact;
        String _schoolevent;
        String _detail2DatSeq001Remark1; // 自立活動の記録（知的以外）
        String _detail2DatSeq002Remark1; // 行動の記録（知的）
        String _detail2DatSeq004Remark1; // 道徳
        String _detail2DatSeq009Remark1; // 総合所見左(関西学院)
        String _detailTrainRef1;
        String _detailTrainRef2;
        String _detailTrainRef3;
        public HTrainRemarkDat(final String year) {
            _year = year;
        }

        private static HTrainRemarkDat getHTrainRemarkDat(final List<HTrainRemarkDat> list, final String year) {
            for (final HTrainRemarkDat d : list) {
                if (d._year.equals(year)) {
                    return d;
                }
            }
            return null;
        }

        public static List<HTrainRemarkDat> load(final DB2UDB db2, final Param param, final Student student) {
            final List<HTrainRemarkDat> htrainRemarkDatList = new ArrayList<HTrainRemarkDat>();
            if (null == param.gakushu && null == param.koudo) {
                return htrainRemarkDatList;
            }
            final String psKey = "PS_HRD";
            if (null == param._psMap.get(psKey)) {
                final String sql = getHtrainremarkDatSql();
                param.setPs(db2, psKey, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { param._year, student._schregno, })) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                htrainremarkDat._g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                htrainremarkDat._totalstudyact = KnjDbUtils.getString(row, "TOTALSTUDYACT");
                htrainremarkDat._totalstudyval = KnjDbUtils.getString(row, "TOTALSTUDYVAL");
                htrainremarkDat._specialActRemark = KnjDbUtils.getString(row, "SPECIALACTREMARK");
                htrainremarkDat._totalRemark = KnjDbUtils.getString(row, "TOTALREMARK");
                htrainremarkDat._attendrecRemark = KnjDbUtils.getString(row, "ATTENDREC_REMARK");
                htrainremarkDat._viewremark = KnjDbUtils.getString(row, "VIEWREMARK");
                htrainremarkDat._classact = KnjDbUtils.getString(row, "CLASSACT");
                htrainremarkDat._studentact = KnjDbUtils.getString(row, "STUDENTACT");
                htrainremarkDat._schoolevent = KnjDbUtils.getString(row, "SCHOOLEVENT");

                htrainRemarkDatList.add(htrainremarkDat);
            }

            if ("1".equals(param._train_ref_1_2_3_use_J)) {
                final String psKey1 = "PS_HRD_DET";
                if (null == param._psMap.get(psKey1)) {
                    final String sql = getHtrainremarkDetailDatSql();
                    param.setPs(db2, psKey1, sql);
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey1), new String[] { param._year, student._schregno, })) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    if (null == year) {
                        continue;
                    }
                    if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                        final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                        htrainremarkDat._g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                        htrainRemarkDatList.add(htrainremarkDat);
                    }
                    final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                    htrainremarkDat._detailTrainRef1 = KnjDbUtils.getString(row, "TRAIN_REF1");
                    htrainremarkDat._detailTrainRef2 = KnjDbUtils.getString(row, "TRAIN_REF2");
                    htrainremarkDat._detailTrainRef3 = KnjDbUtils.getString(row, "TRAIN_REF3");
                }
            }
            final String psKey2 = "PS_HRD_DET2";
            if (null == param._psMap.get(psKey2)) {
                StringBuffer stb = new StringBuffer();
                stb.append(" WITH REGD_YEAR AS ( ");
                stb.append("    SELECT ");
                stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
                stb.append("    FROM ");
                stb.append("        HTRAINREMARK_DAT T1 ");
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
                final String sql = stb.toString();

                if (param._isOutputDebugQuery) {
                    log.info(" htrainremark_detail2_dat sql = " + sql);
                }
                param.setPs(db2, psKey2, sql);
            }
            if (param.isTokubetsuShien()) {
                final List<Map<String, String>> seq1RowList = KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {param._year, student._schregno, "001"});
                for (final Map row : seq1RowList) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    if (null == year) {
                        continue;
                    }
                    if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                        final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                        htrainremarkDat._g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                        htrainRemarkDatList.add(htrainremarkDat);
                    }
                    final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                    htrainremarkDat._detail2DatSeq001Remark1 = KnjDbUtils.getString(row, "REMARK1");
                }

                final List<Map<String, String>> seq2RowList = KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {param._year, student._schregno, "002"});
                for (final Map row : seq2RowList) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    if (null == year) {
                        continue;
                    }
                    if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                        final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                        htrainremarkDat._g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                        htrainRemarkDatList.add(htrainremarkDat);
                    }
                    final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                    htrainremarkDat._detail2DatSeq002Remark1 = KnjDbUtils.getString(row, "REMARK1");
                }
                if (param._isOutputDebugData) {
                    log.info(" detail2 001 = " + seq1RowList);
                    log.info(" detail2 002 = " + seq2RowList);
                }
            }

            final List<Map<String, String>> seq4RowList = KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {param._year, student._schregno, "004"});
            for (final Map row : seq4RowList) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                    final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                    htrainremarkDat._g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                    htrainRemarkDatList.add(htrainremarkDat);
                }
                final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                htrainremarkDat._detail2DatSeq004Remark1 = KnjDbUtils.getString(row, "REMARK1");
            }

            if (param._isOutputDebugData) {
                log.info(" detail2 004 = " + seq4RowList);
            }

            final List<Map<String, String>> seq9RowList = KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {param._year, student._schregno, "009"});
            for (final Map row : seq9RowList) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
                    final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                    htrainremarkDat._g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                    htrainRemarkDatList.add(htrainremarkDat);
                }
                final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
                htrainremarkDat._detail2DatSeq009Remark1 = KnjDbUtils.getString(row, "REMARK1");
            }

            if (param._isOutputDebugData) {
                log.info(" detail2 009 = " + seq9RowList);
            }

            return htrainRemarkDatList;
        }

        private static String getHtrainremarkDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        YEAR <= ? ");
            stb.append("        AND SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T1.ANNUAL ");
            stb.append("       ,TOTALSTUDYACT ");
            stb.append("       ,TOTALSTUDYVAL ");
            stb.append("       ,SPECIALACTREMARK ");
            stb.append("       ,TOTALREMARK ");
            stb.append("       ,ATTENDREC_REMARK ");
            stb.append("       ,VIEWREMARK ");
            stb.append("       ,BEHAVEREC_REMARK ");
            stb.append("       ,CLASSACT ");
            stb.append("       ,STUDENTACT ");
            stb.append("       ,SCHOOLEVENT ");
            stb.append("FROM    HTRAINREMARK_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("ORDER BY ");
            stb.append("    T1.YEAR, T1.ANNUAL ");
            return stb.toString();
        }

        private static String getHtrainremarkDetailDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= ? ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T2.ANNUAL ");
            stb.append("       ,TRAIN_REF1 ");
            stb.append("       ,TRAIN_REF2 ");
            stb.append("       ,TRAIN_REF3 ");
            stb.append("FROM HTRAINREMARK_DETAIL_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
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
        public static List<ActRecord> load(final DB2UDB db2, final Param param, final Student student) {
            final List<ActRecord> actList = new ArrayList<ActRecord>();
            if (null == param.gakushu && null == param.koudo) {
                return actList;
            }
            final String psKey = "PS_ACT";
            if (null == param._psMap.get(psKey)) {
                param.setPs(db2, psKey, getActRecordSql(param));
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno, })) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                final String record = KnjDbUtils.getString(row, "RECORD");
                final int g = KNJA133J_0.getG(param, year, KnjDbUtils.getString(row, "ANNUAL"));
                final int code = Integer.parseInt(KnjDbUtils.getString(row, "CODE"));
                final String div = KnjDbUtils.getString(row, "DIV");

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
            stb.append("        MAX(T1.YEAR) AS YEAR, T1.SCHREGNO, T1.ANNUAL ");
            stb.append("    FROM ");
            stb.append("        BEHAVIOR_DAT T1 ");
            stb.append("    WHERE   T1.YEAR <= '" + param._year + "' ");
            stb.append("          AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, T1.ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("    ,T1.DIV ");
            stb.append("    ,T1.CODE ");
            stb.append("    ,T1.ANNUAL ");
            stb.append("    ,T1.RECORD ");
            stb.append("FROM    BEHAVIOR_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.YEAR = T1.YEAR ");
            return stb.toString();
        }
    }

    private static class AfterGraduatedCourse {

        public static List<String> loadTextList(final DB2UDB db2, final Param param, final List<Gakuseki> gakusekiList, final Student student) {
            List<String> textList = new ArrayList<String>();
            if (null == param.seito || !param._hasAftGradCourseDat) {
                return textList;
            }

            final String psKey = "AFT_GRAD";

            if (null == param._psMap.get(psKey)) {

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
                stb.append("         SCHREGNO = ? and PLANSTAT = '1' and YEAR <= '" + param._year + "' ");
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
                stb.append("     ,E017.NAME1 as E017NAME1 ");
                stb.append("     ,E018.NAME1 as E018NAME1 ");
                stb.append("     ,L3.SCHOOL_NAME ");
                stb.append("     ,L5.FACULTYNAME ");
                stb.append("     ,L6.DEPARTMENTNAME ");
                stb.append("     ,L7.ADDR1 AS CAMPUSADDR1 ");
                stb.append("     ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ");
                stb.append("     ,L4.COMPANY_NAME ");
                stb.append("     ,L4.ADDR1 AS COMPANYADDR1 ");
                stb.append("     ,L4.ADDR2 AS COMPANYADDR2 ");
                stb.append("     ,TFINSC.FINSCHOOL_NAME ");
                stb.append("     ,TFINSC.FINSCHOOL_ADDR1 ");
                stb.append("     ,TFINSC.FINSCHOOL_ADDR2 ");
                stb.append("from ");
                stb.append("     AFT_GRAD_COURSE_DAT T1 ");
                stb.append("inner join TA2 on ");
                stb.append("     T1.YEAR = TA2.YEAR ");
                stb.append("     and T1.SCHREGNO = TA2.SCHREGNO ");
                stb.append("     and T1.SENKOU_KIND = TA2.SENKOU_KIND ");
                stb.append("     and T1.SEQ = TA2.SEQ ");
                stb.append("left join NAME_MST E017 on E017.NAMECD1 = 'E017' and E017.NAME1 = T1.STAT_CD ");
                stb.append("left join NAME_MST E018 on E018.NAMECD1 = 'E018' and E018.NAME1 = T1.STAT_CD ");
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
                stb.append("order by ");
                stb.append("     T1.YEAR, T1.SCHREGNO ");

                param.setPs(db2, psKey, stb.toString());
            }

            final TreeSet<String> yearSet = Student.gakusekiYearSet(gakusekiList);
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno, minYear, maxYear})) {

                if ("0".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 進学
                    if ((null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E017NAME1")) && null != KnjDbUtils.getString(row, "THINKEXAM")) {
                        final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "THINKEXAM"), 50, 10);
                        if (null != token) {
                            textList.addAll(Arrays.asList(token));
                        }
                    } else {
                        if (null != KnjDbUtils.getString(row, "FINSCHOOL_NAME")) {
                            textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "FINSCHOOL_NAME")));
                            textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "FINSCHOOL_ADDR1")));
                            textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "FINSCHOOL_ADDR2")));
                        } else {
                            textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME")));
                            textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "FACULTYNAME")) + StringUtils.defaultString(KnjDbUtils.getString(row, "DEPARTMENTNAME")));
                            textList.add(StringUtils.defaultString(StringUtils.defaultString(KnjDbUtils.getString(row, "CAMPUSFACULTYADDR1"), KnjDbUtils.getString(row, "CAMPUSADDR1"))));
                        }
                    }
                } else if ("1".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 就職
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E018NAME1")) {
                        final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "JOB_THINK"), 50, 10);
                        if (null != token) {
                            textList.addAll(Arrays.asList(token));
                        }
                    } else {
                        textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANY_NAME")));
                        textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANYADDR1")));
                        textList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANYADDR2")));
                    }
                }
            }
            return textList;
        }
    }

    /**
     * 観点の教科
     */
    private static class ClassView {
        final String _classcd;  //教科コード
        final String _classname;  //教科名称
        final String _electdiv;
        final List<ViewSubclass> _viewSubclassList;

        public ClassView(
                final String classcd,
                final String classname,
                final String electdiv
        ) {
            _classcd = classcd;
            _classname = classname;
            _electdiv = electdiv;
            _viewSubclassList = new ArrayList<ViewSubclass>();
        }

        public static ClassView enabled(final ClassView cv, final List<String> pageYears) {
            final List<ViewSubclass> rtn = new ArrayList<ViewSubclass>();
            for (final ViewSubclass vs : cv._viewSubclassList) {
                final ViewSubclass enabled = ViewSubclass.enabled(vs, pageYears);
                if (null != enabled) {
                    rtn.add(enabled);
                }
            }
            if (rtn.isEmpty()) {
                return null;
            }
            ClassView enabled = new ClassView(cv._classcd, cv._classname, cv._electdiv);
            enabled._viewSubclassList.addAll(rtn);
            return enabled;
        }

        /**
         * 同一教科の科目コードを得る
         * @param student
         * @param classview
         * @return
         */
        private TreeSet<String> getViewSubclassCdSet(final List<String> pageYears) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final ViewSubclass viewSubclass : _viewSubclassList) {
                set.add(viewSubclass._subclasscd);
            }
            return set;
        }

        public int getViewNum(final String subclasscd, final List<String> pageYears) {
            int c = 0;
            if (getViewSubclassCdSet(pageYears).size() <= 1) {
                for (final ViewSubclass viewSubclass : _viewSubclassList) {
                    c += viewSubclass._viewList.size();
                }
            } else {
                for (final ViewSubclass viewSubclass : _viewSubclassList) {
                    if (subclasscd.equals(viewSubclass._subclasscd)) {
                        c += viewSubclass._viewList.size();
                    }
                }
            }
            return c;
        }

        // 教科名のセット
        private String setViewClassname(final String classname, final String subclasscd, final Param param, final List<String> pageYears) {
            if (classname == null) {
                return "";
            }
            final int viewnum = getViewNum(subclasscd, pageYears);
            if (viewnum == 0) {
                return classname;
            }
            final int newviewnum;
            if (classname.length() <= viewnum && !"1".equals(param.property(Property.seitoSidoYorokuCyugakuKantenNoBlank))) {
                newviewnum = viewnum + 1;  // 教科間の観点行に１行ブランクを挿入
            } else {
                newviewnum = viewnum;
            }
            final String newclassname;

            if (classname.length() < newviewnum) {
                final int i = (newviewnum - classname.length()) / 2;
                String space = "";
                for (int j = 0; j < i; j++) {
                    space = " " + space;
                }  // 教科名のセンタリングのため、空白を挿入
                newclassname = space + classname;
            } else {
                newclassname = classname;
            }
            return newclassname;
        }

        public String toString() {
            return "ViewClass(" + _classcd + ":" + _classname + " e = " + _electdiv + ")";
        }

        private static ClassView getClassView(final List<ClassView> classViewList, final String classcd, final String classname, final String electdiv) {
            if (null == classcd) {
                return null;
            }
            ClassView classView = null;
            for (final ClassView classView0 : classViewList) {
                if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._electdiv.equals(electdiv)) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }

        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<ClassView> load(final DB2UDB db2, final Param param, final Student student) {
            final List<ClassView> classViewList = new ArrayList<ClassView>();
            if (param.isTokubetsuShien() && CHITEKI1_知的障害.equals(param._chiteki) || null == param.gakushu) {
                return classViewList;
            }
            final String psKey = "PS_VIEW";
            if (null == param._psMap.get(psKey)) {
                final String sql = getViewRecordSql(param);
                if (param._isOutputDebugQuery) {
                    log.info(" ClassView sql = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno, student._schregno, })) {

                //教科コードの変わり目
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                final String curriculumcd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String viewcd = KnjDbUtils.getString(row, "VIEWCD");
                final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                final String status = KnjDbUtils.getString(row, "STATUS");
                final String electdiv = KnjDbUtils.getString(row, "ELECTDIV");
                final int g = param.getGradeCd(year, KnjDbUtils.getString(row, "GRADE")); // 学年
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String subclassKey;
                if ("1".equals(param._useCurriculumcd)) {
                    subclassKey = classcd + "-" + curriculumcd + "-" + subclasscd;
                } else {
                    subclassKey = subclasscd;
                }
                if (!param.useSubclass(student._loginRegd._grade, subclassKey)) {
                    if (param._isOutputDebug) {
                        log.info(" not use subclass = " + subclassKey);
                    }
                    continue;
                }

                ClassView classView = getClassView(classViewList, classcd, classname, electdiv);
                if (null == classView) {
                    classView = new ClassView(classcd, classname, electdiv);
                    classViewList.add(classView);
                }
                ViewSubclass viewSubclass = ViewSubclass.getViewSubclass(classView._viewSubclassList, subclasscd);
                if (null == viewSubclass) {
                    viewSubclass = new ViewSubclass(classcd, curriculumcd, subclasscd, subclassname);
                    classView._viewSubclassList.add(viewSubclass);
                }
                View view = View.getView(viewSubclass._viewList, viewcd);
                if (null == view) {
                    view = new View(viewcd, viewname);
                    viewSubclass._viewList.add(view);
                }
                view._yearViewMap.put(year, new ViewStatus(curriculumcd, status, year, g, grade));
            }

            return classViewList;
        }

        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //観点の表
            stb.append(" VIEW_DATA AS ( ");
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
            stb.append(" ) ");

            //学籍の表
            stb.append(" , SCHREG_DATA AS ( ");
            stb.append("  SELECT  YEAR ");
            stb.append(         ",GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT T1  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
            stb.append("                     AND YEAR <='" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(" ) ");

            stb.append(" , MAIN AS ( ");
            stb.append("    SELECT ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , JVM.CLASSCD ");
                stb.append("          , JVM.SCHOOL_KIND ");
                stb.append("          , JVM.CURRICULUM_CD ");
            }
            if ("1".equals(param.property(Property.knja133jUseViewSubclassMstSubclasscd2))) {
                stb.append("          , VALUE(SUBM.SUBCLASSCD2, JVM.SUBCLASSCD) AS SUBCLASSCD ");
            } else {
                stb.append("          , JVM.SUBCLASSCD AS SUBCLASSCD ");
            }
            stb.append("          , JVM.VIEWCD ");
            stb.append("          , JVM.VIEWNAME ");
            stb.append("          , VALUE(JVM.SHOWORDER, -1) AS SHOWORDERVIEW ");
            stb.append("          , MAX(VD.STATUS) AS STATUS ");
            stb.append("    FROM    JVIEWNAME_SUB_MST JVM ");
            stb.append("    INNER JOIN JVIEWNAME_SUB_YDAT JVY ON JVY.SUBCLASSCD = JVM.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND JVY.CLASSCD = JVM.CLASSCD ");
                stb.append("          AND JVY.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("          AND JVY.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            stb.append("              AND JVY.VIEWCD = JVM.VIEWCD ");
            stb.append("    INNER JOIN SCHREG_DATA W2 ON W2.YEAR = JVY.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = JVM.CLASSCD ");
                stb.append("          AND CLM.SCHOOL_KIND = JVM.SCHOOL_KIND ");
            } else {
                stb.append("    LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = SUBSTR(JVM.SUBCLASSCD, 1, 2) ");
            }
            stb.append("    LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = JVM.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND SUBM.CLASSCD = JVM.CLASSCD ");
                stb.append("          AND SUBM.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("          AND SUBM.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            stb.append("          AND VALUE(CLM.ELECTDIV, '0') <> '1' ");

            stb.append("    LEFT JOIN VIEW_DATA VD ON VD.YEAR = W2.YEAR ");
            stb.append("       AND VD.VIEWCD = JVM.VIEWCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND VD.CLASSCD = JVM.CLASSCD ");
                stb.append("       AND VD.SCHOOL_KIND = JVM.SCHOOL_KIND ");
                stb.append("       AND VD.CURRICULUM_CD = JVM.CURRICULUM_CD ");
            }
            if ("1".equals(param.property(Property.knja133jUseViewSubclassMstSubclasscd2))) {
                stb.append("       AND (VD.SUBCLASSCD = JVM.SUBCLASSCD ");
                stb.append("           OR VD.SUBCLASSCD = SUBM.SUBCLASSCD2 ");
                stb.append("           ) ");
            } else {
                stb.append("       AND VD.SUBCLASSCD = JVM.SUBCLASSCD ");
            }
            stb.append("    WHERE ");
            stb.append("       JVM.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("    GROUP BY ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , JVM.CLASSCD ");
                stb.append("          , JVM.SCHOOL_KIND ");
                stb.append("          , JVM.CURRICULUM_CD ");
            }
            if ("1".equals(param.property(Property.knja133jUseViewSubclassMstSubclasscd2))) {
                stb.append("          , VALUE(SUBM.SUBCLASSCD2, JVM.SUBCLASSCD) ");
            } else {
                stb.append("          , JVM.SUBCLASSCD ");
            }
            stb.append("          , JVM.VIEWCD ");
            stb.append("          , JVM.VIEWNAME ");
            stb.append("          , VALUE(JVM.SHOWORDER, -1) ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT ");
            stb.append("     MAIN.YEAR ");
            stb.append("   , MAIN.GRADE ");
            stb.append("   , VALUE(CLM.ELECTDIV, '0') AS ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   , CLM.CLASSCD || '-' || CLM.SCHOOL_KIND AS CLASSCD ");
                stb.append("   , MAIN.CURRICULUM_CD ");
                stb.append("   , MAIN.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("   , CLM.CLASSCD");
                stb.append("   , '' AS CURRICULUM_CD");
                stb.append("   , MAIN.SUBCLASSCD");
            }
            stb.append("   , VALUE(CLM.CLASSORDERNAME1, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SUBM.SUBCLASSORDERNAME1, SUBM.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("   , VALUE(CLM.SHOWORDER, -1) AS SHOWORDERCLASS ");
            stb.append("   , MAIN.VIEWCD ");
            stb.append("   , MAIN.VIEWNAME ");
            stb.append("   , MAIN.STATUS ");
            stb.append(" FROM MAIN ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = MAIN.CLASSCD ");
                stb.append("   AND CLM.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
            } else {
                stb.append(" INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = SUBSTR(MAIN.SUBCLASSCD,1,2) ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = MAIN.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND SUBM.CLASSCD = MAIN.CLASSCD ");
                stb.append("  AND SUBM.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
                stb.append("  AND SUBM.CURRICULUM_CD = MAIN.CURRICULUM_CD ");
            }

            stb.append(" ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    VALUE(CLM.ELECTDIV, '0'), ");
            stb.append("    CLM.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" CLM.SCHOOL_KIND, ");
            }
            stb.append("    MAIN.SUBCLASSCD, ");
            stb.append("    VALUE(MAIN.SHOWORDERVIEW, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" MAIN.CURRICULUM_CD, "); // 教育課程の昇順に取得（同一の観点コードの場合、観点名称は教育課程の小さいほうを表示）
            }
            stb.append("    MAIN.VIEWCD, ");
            stb.append("    MAIN.GRADE ");
            return stb.toString();
        }
    }

    /**
     * 観点科目データ
     */
    private static class ViewSubclass {
        final String _classcd;
        final String _curriculumcd;
        final String _subclasscd;  //科目コード
        final String _subclassname;
        final List<View> _viewList = new ArrayList<View>();

        public ViewSubclass(
                final String classcd,
                final String curriculumcd,
                final String subclasscd,
                final String subclassname
        ) {
            _classcd = classcd;
            _curriculumcd = curriculumcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        public static ViewSubclass enabled(final ViewSubclass vs, final List<String> pageYears) {
            final List<View> rtn = new ArrayList<View>();
            for (final View view : vs._viewList) {
                final View enabled = View.enabled(view, pageYears);
                if (null != enabled) {
                    rtn.add(enabled);
                }
            }
            if (rtn.isEmpty()) {
                return null;
            }
            final ViewSubclass nvs = new ViewSubclass(vs._classcd, vs._curriculumcd, vs._subclasscd, vs._subclassname);
            nvs._viewList.addAll(rtn);
            return nvs;
        }

        private static ViewSubclass getViewSubclass(final List<ViewSubclass> viewSubclassList, final String subclasscd) {
            ViewSubclass subclassView = null;
            for (final ViewSubclass viewSubclass0 : viewSubclassList) {
                if (viewSubclass0._subclasscd.equals(subclasscd)) {
                    subclassView = viewSubclass0;
                    break;
                }
            }
            return subclassView;
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname.toString() + ")";
        }
    }

    private static class View {
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final Map<String, ViewStatus> _yearViewMap = new HashMap<String, ViewStatus>();
        private View(
                final String viewcd,
                final String viewname
        ) {
            _viewcd = viewcd;
            _viewname = viewname;
        }

        public static View enabled(final View v, final List<String> pageYears) {
            final Map<String, ViewStatus> filteredYearViewMap = new HashMap<String, ViewStatus>();
            if (null == pageYears) {
                filteredYearViewMap.putAll(v._yearViewMap);
            } else {
                for (final String year : v._yearViewMap.keySet()) {
                    if (pageYears.contains(year)) {
                        filteredYearViewMap.put(year, v._yearViewMap.get(year));
                    }
                }
            }
            //log.info(" years = " + v._yearViewMap.keySet() + " / filtered = " + filteredYearViewMap.keySet());
            if (filteredYearViewMap.isEmpty()) {
                return null;
            }
            final View nv = new View(v._viewcd, v._viewname);
            nv._yearViewMap.putAll(filteredYearViewMap);
            return nv;
        }

        private static View getView(final List<View> viewList, final String viewcd) {
            View view = null;
            for (final View view0 : viewList) {
                if (view0._viewcd.equals(viewcd)) {
                    view = view0;
                    break;
                }
            }
            return view;
        }

        public String toString() {
            return "View(" + _viewcd + ":" + _yearViewMap.toString() + ")";
        }
    }

    /**
     * 観点データ
     */
    private static class ViewStatus {
        final String _curriculumcd;
        final String _status; //観点
        final String _year;
        final int _g; // 学年
        final String _grade; // 学年

        public ViewStatus(
                final String curriculumcd,
                final String status,
                final String year,
                final int g,
                final String grade
        ) {
            _curriculumcd = curriculumcd;
            _status = status;
            _year = year;
            _g = g;
            _grade = grade;
        }

        public String toString() {
            return "(" + _year + "/ curriculumcd = " + _curriculumcd + ":" + StringUtils.defaultString(_status, " ") + ")";
        }
    }

    /**
     * 評定データ
     */
    private static class ValueRecord {
        final String _year;
        final int _g;
        final String _grade;
        final String _classcd;
        final String _curriculumCd;
        final String _subclasscd;
        final String _classMstElectDiv;
        final String _subclassMstElectDiv;
        final String _className;
        final String _subclassName;
        final String _value; //評定
        public ValueRecord(
                final String year,
                final int g,
                final String grade,
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final String classMstElectDiv,
                final String subclassMstElectDiv,
                final String className,
                final String subclassName,
                final String value) {
            _year = year;
            _g = g;
            _grade = grade;
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _classMstElectDiv = classMstElectDiv;
            _subclassMstElectDiv = subclassMstElectDiv;
            _className = className;
            _subclassName = subclassName;
            _value = value;
        }
        public String toString() {
            return "(" + _classcd + ": " + _curriculumCd + ":" + _subclasscd + ", " + _className + ":" + _subclassName + ")";
        }

        public String subclassKey(final Param param) {
            return "1".equals(param._useCurriculumcd) ? _classcd + "-" + _curriculumCd + "-" + _subclasscd : _subclasscd;
        }

        public String getPrintHyotei(final Param param) {
            final String value;
            if ("1".equals(param.property(Property.knja133jElectSubclassHyoteiConv))) {
                if ("1".equals(_subclassMstElectDiv)) {
                    if (param._d087Name1Map.containsKey(_value)) {
                        value = param._d087Name1Map.get(_value);
                    } else {
                        if (null != _value) {
                            log.info(" D087 not defined : " + _value);
                        }
                        value = _value;
                    }
                } else {
                    value = _value;
                }
            } else if (param._d065Name1List.contains(subclassKey(param))) {
                value = param._d001Abbv1Map.get(_value);
            } else if ("1".equals(_classMstElectDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                if ("11".equals(_value)) {
                    value = "A";
                } else if ("22".equals(_value)) {
                    value = "B";
                } else if ("33".equals(_value)) {
                    value = "C";
                } else {
                    value = _value;
                }
            } else {
                value = _value;
            }
            return value;
        }

        public static List<ValueRecord> load(final DB2UDB db2, final Param param, final Student student) {
            final List<ValueRecord> valueRecordList = new ArrayList<ValueRecord>();
            if (param.isTokubetsuShien() && CHITEKI1_知的障害.equals(param._chiteki) || null == param.gakushu) {
                return valueRecordList;
            }
            final String psKey = "PS_VALUE";
            if (null == param._psMap.get(psKey)) {
                final String sql = getValueRecordSql(param);
                if (param._isOutputDebugQuery) {
                    log.info(" sql = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }

            for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new String[] { student._schregno, student._schregno, })) {

                //教科コードの変わり目
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final int g = param.getGradeCd(year, KnjDbUtils.getString(row, "GRADE")); // 学年
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String classMstElectDiv = KnjDbUtils.getString(row, "CLASS_ELECTDIV");
                final String subclassMstElectDiv = KnjDbUtils.getString(row, "SUBCLASS_ELECTDIV");
                final String classCd = KnjDbUtils.getString(row, "CLASSCD");
                final String curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String className = KnjDbUtils.getString(row, "CLASSNAME");
                final String subclassName = KnjDbUtils.getString(row, "SUBCLASSNAME");
                //評定出力
                final String value = KnjDbUtils.getString(row, "VALUE");

                final ValueRecord valueRecord = new ValueRecord(year, g, grade, classCd, curriculumCd, subclassCd, classMstElectDiv, subclassMstElectDiv, className, subclassName, value);
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
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("       AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            stb.append("       AND T1.VALUATION IS NOT NULL ");
            stb.append(" ) ");

            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
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
            stb.append("    ,VALUE(CLM.ELECTDIV, '0') AS CLASS_ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND AS CLASSCD ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,'' AS CURRICULUM_CD ");
            }
            stb.append("    ,VALUE(SUBM_SAKI.SUBCLASSCD, T5.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append("    ,CASE WHEN SUBM_SAKI.SUBCLASSCD IS NOT NULL THEN SUBM_SAKI.ELECTDIV ELSE SUBM.ELECTDIV END AS SUBCLASS_ELECTDIV ");
            stb.append("    ,MAX(VALUE(CLM.CLASSORDERNAME1, CLM.CLASSNAME)) AS CLASSNAME ");
            stb.append("    ,MAX(VALUE(SUBM_SAKI.SUBCLASSORDERNAME1, SUBM_SAKI.SUBCLASSNAME, SUBM.SUBCLASSORDERNAME1, SUBM.SUBCLASSNAME)) AS SUBCLASSNAME ");
            stb.append("    ,MAX(VALUE(CLM.SHOWORDER, -1)) AS SHOWORDERCLASS ");
            stb.append("    ,MAX(T5.VALUE) AS VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND CLM.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST SUBM ON SUBM.SUBCLASSCD = T5.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND SUBM.CLASSCD = T5.CLASSCD ");
                stb.append(" AND SUBM.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append(" AND SUBM.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST SUBM_SAKI ON SUBM_SAKI.SUBCLASSCD = SUBM.SUBCLASSCD2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND SUBM_SAKI.CLASSCD = SUBM.CLASSCD ");
                stb.append(" AND SUBM_SAKI.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                stb.append(" AND SUBM_SAKI.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            }
            stb.append("GROUP BY ");
            stb.append("    T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,VALUE(CLM.ELECTDIV, '0') ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
            }
            stb.append("    ,VALUE(SUBM_SAKI.SUBCLASSCD, T5.SUBCLASSCD) ");
            stb.append("    ,CASE WHEN SUBM_SAKI.SUBCLASSCD IS NOT NULL THEN SUBM_SAKI.ELECTDIV ELSE SUBM.ELECTDIV END ");
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS ");
            stb.append("    ,VALUE(CLM.ELECTDIV, '0') ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
            }
            stb.append("    ,VALUE(SUBM_SAKI.SUBCLASSCD, T5.SUBCLASSCD) ");
            stb.append("    ,T2.GRADE ");
            return stb.toString();
        }
    }

    private static String crlfReplaced(final String src, final Param param) {
        String ret;
        if (src == null) {
            ret = "";
        } else {
            if (param._seitoSidoYorokuKinsokuFormJ) {
                ret = StringUtils.replace(StringUtils.replace(StringUtils.replace(src, "\r\n", "<br/>"), "\r", "<br/>"), "\n", "<br/>");

                final String hanhan = "  "; // 半角スペース2つ
                final String zen = "　"; // 全角スペース1つ
                if (ret.indexOf(hanhan) == 0) { // 頭に半角スペース2つある場合、全角スペース1つに置き換え（SVFの禁則処理にtrimされないようにするため）
                    ret = StringUtils.replaceOnce(ret, hanhan, zen);
                }
                ret = StringUtils.replace(ret, "・", " ・");
            } else {
                ret = src;
            }
        }
        return ret;
    }

    /**
     * 文科省様式改訂
     */
    private enum KAITEI {

        /**
         * 道徳欄なし
         */
        BEFORE_DOUTOKU,

        /**
         * 道徳欄追加
         */
        ADD_DOUTOKU,

        /**
         * 評定は観点の教科ごとの最下行に移動。
         * 評定欄無し
         */
        DEFAULT,

        ;

        boolean hasDoutoku() {
            return this != BEFORE_DOUTOKU;
        }
    }

    private static class KaiteiInfo implements Comparable<KaiteiInfo> {
        final KAITEI _kaitei;
        int _startYear;
        int _endYear;
        String _form;
        KaiteiInfo(final KAITEI kaitei, final int startYear, final int endYear) {
            _kaitei = kaitei;
            _startYear = startYear;
            _endYear = endYear;
        }
        public boolean isInvalid() {
            return _startYear == -1 || _endYear == -1;
        }
        public void setInvalid() {
            _startYear = -1;
            _endYear = -1;
        }
        @Override
        public int compareTo(final KaiteiInfo other) {
            return _kaitei.compareTo(other._kaitei);
        }
        @Override
        public String toString() {
            return "KaiteiInfo(" + _kaitei + ", start = " + _startYear + ", end = " + _endYear + ")";
        }
        public static KaiteiInfo getYearContainedKaiteiInfo(final List<KaiteiInfo> kaiteiInfoList, final String year) {
            if (!NumberUtils.isDigits(year)) {
                return null;
            }
            final int iYear = Integer.parseInt(year);
            KaiteiInfo kaiteiInfo = null;
            for (final KaiteiInfo k : kaiteiInfoList) {
                final boolean rangeContains = k._startYear <= iYear && iYear <= k._endYear;
                if (rangeContains) {
                    kaiteiInfo = k;
                    break;
                }
            }
            return kaiteiInfo;
        }
    }

    private static class FormInfo {
        private int VIEW_LINE1_MAX;

        final Param _param;
        final Student _student;
        private boolean _isKaijyoOldForm;

        final Map<String, SvfForm> _formnameSvfFormMap = new HashMap<String, SvfForm>();
        final Map<String, Map<String, BigDecimal>> _subformRecordCountMap = new HashMap<String, Map<String, BigDecimal>>();

        final String _noDoutokuForm;
        final String _hyoteiRanForm;
        final String _defaultForm;

        final List<KaiteiInfo> _kaiteiInfoList;

        public FormInfo(final Vrw32alp svf, final Param param, final Student student) {
            _param = param;
            _student = student;

            // 左51行 右10行
            VIEW_LINE1_MAX = 61;
            if (param.isTokubetsuShien() && !"1".equals(param.property(Property.useSpecial_Support_School))) {
                if (CHITEKI2_知的障害以外.equals(param._chiteki)) {
                    _noDoutokuForm = "KNJA134J_13.frm";
                    _hyoteiRanForm = null;
                    _defaultForm = null;
                } else { // "1".equals(param._chiteki) {
                    throw new IllegalStateException("not target form : chiteki = " + param._chiteki);
                }
            } else {

                if (param._z010.in(Z010.reitaku)) {
                    _noDoutokuForm = "KNJA133J_13REITAKU_NO_DOUTOKU.frm";
                    _hyoteiRanForm = "KNJA133J_13REITAKU.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.TamagawaSei)) {
                    _noDoutokuForm = "KNJA133J_13_2_NO_DOUTOKU.frm";
                    _hyoteiRanForm = "KNJA133J_13_2.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.chiyodaKudan)) {
                    _noDoutokuForm = "KNJA133J_13KUDAN_NO_DOUTOKU.frm";
                    _hyoteiRanForm = "KNJA133J_13KUDAN.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.sundaikoufu)) {
                    _noDoutokuForm = "KNJA133J_13SUNDAI_NO_DOUTOKU.frm";
                    _hyoteiRanForm = "KNJA133J_13SUNDAI.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.osakashinnai)) {
                    _noDoutokuForm = "KNJA133J_13_NO_DOUTOKU.frm";
                    _hyoteiRanForm = "KNJA133J_13.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.kaijyo)) {
                    _noDoutokuForm = "KNJA133J_13KAIJO_OLD.frm";
                    _hyoteiRanForm = "KNJA133J_13KAIJO.frm";
                    _defaultForm = null; // ???
                } else if (param._z010.in(Z010.bunkyo)) {
                    _noDoutokuForm = null;
                    _hyoteiRanForm = "KNJA133J_13BUNKYO.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.tosa)) {
                    VIEW_LINE1_MAX = 59;
                    _noDoutokuForm = "KNJA133J_13TOSA_NO_DOUTOKU.frm";
                    _hyoteiRanForm = "KNJA133J_13TOSA.frm";
                    _defaultForm = null;
                } else if (param._z010.in(Z010.jogakkan)) {
                    _noDoutokuForm = null;
                    _hyoteiRanForm = "KNJA133J_13_JOGAKKAN.frm";
                    _defaultForm = null;
                } else {
                    if ("KNJA133J_13_2.frm".equals(param.property(Property.knja133jForm3))) { // 玉川聖 = KNJA133J_13_2.frm
                        _noDoutokuForm = "KNJA133J_13_2_NO_DOUTOKU.frm";
                        _hyoteiRanForm = null;
                        _defaultForm = param.property(Property.knja133jForm3);
                    } else if ("KNJA133J_13KUDAN.frm".equals(param.property(Property.knja133jForm3))) { // 千代田九段 = KNJA133J_13KUDAN.frm
                        _noDoutokuForm = "KNJA133J_13KUDAN_NO_DOUTOKU.frm";
                        _hyoteiRanForm = null;
                        _defaultForm = param.property(Property.knja133jForm3);
                    } else if ("KNJA133J_13NAGISA.frm".equals(param.property(Property.knja133jForm3))) { // 広島なぎさ = KNJA133J_13NAGISA.frm
                        _noDoutokuForm = "KNJA133J_13NAGISA_NO_DOUTOKU.frm";
                        _hyoteiRanForm = null;
                        _defaultForm = param.property(Property.knja133jForm3);
                    } else if (param._seitoSidoYorokuKinsokuFormJ) {
                        _noDoutokuForm = null;
                        _hyoteiRanForm = null;
                        _defaultForm = "KNJA133J_13C.frm";
                    } else {
                        _noDoutokuForm = "KNJA133J_13_NO_DOUTOKU.frm";
                        _hyoteiRanForm = "KNJA133J_13_HYOTEIRAN.frm";
                        _defaultForm = "KNJA133J_13.frm";
                    }
                }
            }

            final List<KAITEI> noUseKaiteiList = new ArrayList<KAITEI>();
            for (final Tuple<String, KAITEI> formAndKaitei : Arrays.asList(Tuple.of(_noDoutokuForm, KAITEI.BEFORE_DOUTOKU), Tuple.of(_hyoteiRanForm, KAITEI.ADD_DOUTOKU), Tuple.of(_defaultForm, KAITEI.DEFAULT))) {
                final String form = formAndKaitei._first;
                final KAITEI kaitei = formAndKaitei._second;
                if (null == form) {
                    noUseKaiteiList.add(kaitei);
                    continue;
                }
                final String path = svf.getPath(form);
                final File pathFile = new File(path);
                if (!pathFile.exists()) {
                    log.info(" no form file : " + path);
                    noUseKaiteiList.add(kaitei);
                    continue;
                }
                final SvfForm svfForm = new SvfForm(pathFile);
                if (svfForm.readFile()) {
                    _formnameSvfFormMap.put(form, svfForm);
                }
            }

            for (final SvfForm svfForm : _formnameSvfFormMap.values()) {
                _subformRecordCountMap.put(svfForm._formFile.getName().replaceAll(".xml$", ".frm"), getSubformRecordCountMap(param, svfForm));
            }
            log.info(" recordCountMap = " + _subformRecordCountMap);

            final int beforeDoutokuEndYear = 2018;
            final int addDoutokuStartYear = beforeDoutokuEndYear + 1;
            final int addDoutokuEndYear = 2020;
            final int defStartYear = addDoutokuEndYear + 1;

            final KaiteiInfo beforeDoutoku = new KaiteiInfo(KAITEI.BEFORE_DOUTOKU, 0, beforeDoutokuEndYear);
            final KaiteiInfo addDoutoku = new KaiteiInfo(KAITEI.ADD_DOUTOKU, addDoutokuStartYear, addDoutokuEndYear);
            final KaiteiInfo def = new KaiteiInfo(KAITEI.DEFAULT, defStartYear, 9999);
            final List<KaiteiInfo> kaiteiInfoList = new ArrayList<KaiteiInfo>(Arrays.asList(beforeDoutoku, addDoutoku, def));

            final List<KAITEI> validKaiteiList = new ArrayList<KAITEI>(Arrays.asList(KAITEI.values()));
            validKaiteiList.removeAll(noUseKaiteiList);

            if (param._isOutputDebug) {
                log.info(" validKaiteiList = " + validKaiteiList);
            }

            KaiteiInfo lastInfo = null;
            for (final KaiteiInfo kaiteiInfo : kaiteiInfoList) {
                if (noUseKaiteiList.contains(kaiteiInfo._kaitei)) {
                    if (kaiteiInfo._kaitei == KAITEI.DEFAULT) {
                        if (null == lastInfo) {
                            log.warn("フォームがひとつもない! : noUseKaiteiList = " + noUseKaiteiList);
                        } else {
                            lastInfo._endYear = kaiteiInfo._endYear;
                        }
                    } else {
                        int idx = 0;
                        while (idx < KAITEI.values().length && KAITEI.values()[idx] != kaiteiInfo._kaitei) {
                            idx += 1;
                        }
                        KAITEI validNext = null;
                        for (int i = idx + 1; i < KAITEI.values().length; i += 1) {
                            if (validKaiteiList.contains(KAITEI.values()[i])) {
                                validNext = KAITEI.values()[i];
                                break;
                            }
                        }
                        KaiteiInfo nextInfo = null;
                        if (null != validNext) {
                            for (final KaiteiInfo kaiteiInfoNext : kaiteiInfoList) {
                                if (kaiteiInfoNext._kaitei == validNext) {
                                    nextInfo = kaiteiInfoNext;
                                }
                            }
                        }
                        if (param._isOutputDebug) {
                            log.info(" nextInfo = " + nextInfo + " (" + validNext + ") <- " + kaiteiInfo);
                        }
                        if (null != nextInfo) {
                            nextInfo._startYear = Math.min(kaiteiInfo._startYear, nextInfo._startYear);
                        } else if (null != lastInfo) {
                            lastInfo._endYear = Math.max(lastInfo._endYear, kaiteiInfo._endYear);
                        }
                    }
                    kaiteiInfo.setInvalid();
                } else {
                    lastInfo = kaiteiInfo;
                }
            }
            if (param._isOutputDebug) {
                for (final KaiteiInfo kaiteiInfo : kaiteiInfoList) {
                    log.info(" kaiteiInfo " + kaiteiInfo);
                }
            }

            if (param._z010.in(Z010.KINJUNIOR)) {
                // 近大中学は道徳欄追加のみのフォームを使用しない
                beforeDoutoku._endYear = addDoutokuStartYear;
                addDoutoku.setInvalid();
            } else {
                final boolean isNoDoutokuFormCheckYearGrade = _param._z010.in(Z010.sundaikoufu) || _param._z010.in(Z010.osakashinnai) || "1".equals(_param.property(Property.seitoSidoYorokuCyugakuDoutokuKirikaeCheckGrade));
                if (isNoDoutokuFormCheckYearGrade) {
                    // 道徳フォームは一括変更ではなく学年による
                    final int startYear = addDoutoku._startYear + student.currentGradeCd(_param) - 1;

                    if (!beforeDoutoku.isInvalid() && !addDoutoku.isInvalid()) {
                        beforeDoutoku._endYear = startYear - 1;
                        addDoutoku._startYear = startYear;
                    }

                    if (param._isOutputDebug) {
                        log.info(" noDoutokuFormCheckYearGrade kaiteiInfo " + kaiteiInfoList);
                    }
                }
            }
            _kaiteiInfoList = kaiteiInfoList;
        }

        private Map<String, BigDecimal> getSubformRecordCountMap(final Param param, final SvfForm svfForm) {
            final Map<String, BigDecimal> subformnameRecordCountMap = new HashMap<String, BigDecimal>();
            final SvfForm.Record record = svfForm.getRecord("RECORD1");
            if (null != record) {
                final int recordHeight = record.getHeight();

                if (recordHeight > 0) {
                    if (param._isOutputDebug) {
                        log.info(" record " + record._name + " height = " + recordHeight);
                    }
                    for (final SvfForm.SubForm subForm : svfForm.getElementList(SvfForm.SubForm.class)) {
                        final int subformHeight = subForm.getHeight();
                        final BigDecimal recordCount = new BigDecimal(subformHeight).divide(new BigDecimal(recordHeight), 3, BigDecimal.ROUND_HALF_DOWN);
                        if (param._isOutputDebug) {
                            log.info(" form " + svfForm._formFile.getName() + ", subform " + subForm._name + " height = " + subformHeight + ", recordCount = " + recordCount);
                        }
                        subformnameRecordCountMap.put(subForm._name, recordCount);
                    }
                }
            }
            return subformnameRecordCountMap;
        }

        public int getViewLine1Max(final Vrw32alp svf, final String formname) {
            if (!_subformRecordCountMap.containsKey(formname)) {
                final String path = svf.getPath(formname);
                final File pathFile = new File(path);
                Map<String, BigDecimal> subformRecordCountMap = null;
                if (!pathFile.exists()) {
                    log.info(" no form file : " + path);
                } else {
                    final SvfForm svfForm = new SvfForm(pathFile);
                    if (svfForm.readFile()) {
                        subformRecordCountMap = getSubformRecordCountMap(_param, svfForm);
                    }
                }
                _subformRecordCountMap.put(formname, subformRecordCountMap);
            }
            if (null == _subformRecordCountMap.get(formname)) {
                return VIEW_LINE1_MAX;
            }
            int count = 0;
            for (final Map.Entry<String, BigDecimal> subFormNameViewLineCountEntry : getMappedMap(_subformRecordCountMap, formname).entrySet()) {
                final BigDecimal viewLineCount = subFormNameViewLineCountEntry.getValue();
                count += viewLineCount.intValue();
            }
            return count;
        }

        private List<Tuple<KaiteiInfo, List<String>>> getPageYearsList() {
            final List<Tuple<KaiteiInfo, List<String>>> pageYearList = new ArrayList<Tuple<KaiteiInfo, List<String>>>();
            Tuple<KaiteiInfo, List<String>> years = null;
            for (final SchregRegdDat regd : _student._regdList) {

                KaiteiInfo kaiteiInfo = KaiteiInfo.getYearContainedKaiteiInfo(_kaiteiInfoList, regd._year);
                if (null == kaiteiInfo) {
                    _param.logOnce(" getPageYearsList no kaitei : year = " + regd._year);
                    continue;
                } else {
                    _param.logOnce("getPageYearsList :: year " + regd._year + ", kaiteiInfo = " + kaiteiInfo);
                }
                if (null == getPrintForm(kaiteiInfo)) {
                    // 前のフォーム
                    if (null != years) {
                        kaiteiInfo = years._first;
                    }
                }
                if (null == years || years._first != kaiteiInfo) {
                    final List<String> pageYears = new ArrayList<String>();
                    years = Tuple.of(kaiteiInfo, pageYears);
                    pageYearList.add(years);
                }
                if (!years._second.contains(regd._year)) {
                    years._second.add(regd._year);
                }
            }
            return pageYearList;
        }

        public String getPrintForm(final KaiteiInfo kaiteiInfo) {
            if (null == kaiteiInfo) {
                return _defaultForm;
            }
            _isKaijyoOldForm = false;
            if (_param._z010.in(Z010.kaijyo)) {
                if (kaiteiInfo._kaitei == KAITEI.BEFORE_DOUTOKU) {
                    _isKaijyoOldForm = true;
                    VIEW_LINE1_MAX = 45;
                }
            }
            if (kaiteiInfo._kaitei == KAITEI.BEFORE_DOUTOKU) {
                return _noDoutokuForm;
            } else if (kaiteiInfo._kaitei == KAITEI.ADD_DOUTOKU) {
                return _hyoteiRanForm;
            } else {
                return _defaultForm;
            }
        }
    }

    /**
     * 様式
     */
    private static abstract class KNJA133J_0 {

        protected final Vrw32alp _svf;
        private Param _param;
        protected boolean hasdata;
        protected String _currentForm;
        protected Map<String, String> _modifiedSvfFormCacheMap = new HashMap<String, String>();
        SvfFieldAreaInfo _areaInfo;

        KNJA133J_0(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }

        protected boolean pageYearsContains(final List<String> pageYears, final String year) {
            if (null == pageYears) {
                return true;
            }
            return pageYears.contains(year);
        }

        protected Param param() {
            return _param;
        }

        protected Map<String, String> getConfigFormMap(final SvfForm svfForm, final KaiteiInfo kaiteiInfo, final Student student, final PersonalInfo pi) {
            return new HashMap<String, String>();
        }
        protected void modifyForm(final SvfForm svfForm, final PersonalInfo personalInfo, final Map<String, String> modifyFormFlgMap) {
        }
        protected final String configForm(final String form, final KaiteiInfo kaiteiInfo, final Student student, final PersonalInfo pi) {

            SvfForm svfForm = null;
            File file = null;
            boolean readFile = false;
            try {
                file = new File(_svf.getPath(form));
                svfForm = new SvfForm(file);
                readFile = svfForm.readFile();
                if (!readFile) {
                    log.error("could not read file : " + _svf.getPath(form));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            final Map<String, String> configFormMap = getConfigFormMap(svfForm, kaiteiInfo, student, pi);
            String formCreateFlg = mkString(configFormMap, "").toString();
            if (param()._isOutputDebug) {
                log.info(" form " + form + " config Flg = " + formCreateFlg);
            }
            if (StringUtils.isEmpty(formCreateFlg)) {
                return form;
            }

            formCreateFlg = form + "::" + formCreateFlg;
            if (_modifiedSvfFormCacheMap.containsKey(formCreateFlg)) {
                return _modifiedSvfFormCacheMap.get(formCreateFlg);
            }
            try {
                _modifiedSvfFormCacheMap.put(formCreateFlg, form);
                if (readFile) {

                    modifyForm(svfForm, pi, configFormMap);

                    final File newFormFile = svfForm.writeTempFile();
                    _modifiedSvfFormCacheMap.put(formCreateFlg, newFormFile.getName());
                    param()._deleteFiles.add(newFormFile);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return _modifiedSvfFormCacheMap.get(formCreateFlg);
        }

        public int getFieldRepeatCount(final String fieldname, final int defval) {
            int repeatCount = defval;
            try {
                SvfField f = getMappedMap(param().formFieldInfoMap(), _currentForm).get(fieldname);
                repeatCount = f._fieldRepeatCount;
            } catch (Throwable t) {
                log.info(" failsafe = " + t + ", fieldname = " + fieldname);
            }
            return repeatCount;
        }

        protected void printSvfRenbanUseFieldArePreferPointThanKeta(final String fieldname, final String data, final double pointThanKeta) {
            if (StringUtils.isBlank(data)) {
                return;
            }
            if (param().isOutputDebugField(fieldname)) {
                log.info(" printSvfRenbanUseFieldAreaPreferPoint(" + fieldname + ", " + Util.take(20, data) + "..., " + pointThanKeta + ")");
            }

            final int repeatCount = getFieldRepeatCount(fieldname, 0);
            final boolean isRepeat = repeatCount > 0;

            if (null == _areaInfo) {
                _areaInfo = new SvfFieldAreaInfo();
                _areaInfo._param._setKinsoku = param()._useEditKinsoku;
                _areaInfo._param._isOutputDebugKinsoku = param()._isOutputDebugKinsoku;
            }
            _areaInfo._param._isOutputDebug = param().isOutputDebugField(fieldname);

            final ModifyParam modifyParam = new ModifyParam();
            modifyParam._repeatCount = repeatCount;
            modifyParam._usePreferPointThanKeta = true;
            modifyParam._preferPointThanKeta = pointThanKeta;
            final Map modifyFieldInfoMap = _areaInfo.getModifyFieldInfoMap(param().formFieldInfoMap(), _currentForm, fieldname, modifyParam, data);

            if (param()._isOutputDebug || param().isOutputDebugField(fieldname)) {
                log.info(" !!! modify field " + fieldname + " = " + Util.listString(modifyFieldInfoMap.entrySet(), 0));
            }

            if (isRepeat) {
                int ketai = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_KETA", new Integer(0)).intValue();
                final int lines = KnjDbUtils.getInt(modifyFieldInfoMap, "FIELD_LINE", new Integer(0)).intValue();

                final List attrMapRepeatList = getMappedList(modifyFieldInfoMap, "REPEAT");

                for (int ri = 0; ri < attrMapRepeatList.size(); ri++) {
                    final Map attrMap = (Map) attrMapRepeatList.get(ri);
                    vrAttributen(fieldname, ri + 1, KnjDbUtils.getString(attrMap, "FIELD_ATTR"));
                }
                printSvfRenban(fieldname, getTokenList(data, ketai, lines, param()));

            } else {
                // 未実装
            }
        }

        protected int vrAttribute(final String field, final String data) {
            if (null == getSvfField(field)) {
                if (param().isOutputDebugField(field)) {
                    log.warn(" no field : " + field + " (data = " + data + ")");
                }
            } else {
                if (param().isOutputDebugField(field)) {
                    log.info(" vrAttribute(\"" + field + "\", " + (null == data ? data : "\"" + data + "\"") + ");");
                }
            }
            return _svf.VrAttribute(field, data);
        }

        protected int vrAttributen(final String field, final int gyo, final String data) {
            if (null == getSvfField(field)) {
                if (param().isOutputDebugField(field)) {
                    log.warn(" no field : " + field + " (gyo = " + gyo + ", data = " + data + ")");
                }
            } else {
                if (param().isOutputDebugField(field)) {
                    log.info(" vrAttributen(\"" + field + "\", " + gyo + ", " + (null == data ? data : "\"" + data + "\"") + ");");
                }
            }
            return _svf.VrAttributen(field, gyo, data);
        }

        protected int vrsOut(final String field, final String data) {
            if (null == getSvfField(field)) {
                if (param().isOutputDebugField(field)) {
                    log.warn(" no field : " + field + " (data = " + data + ")");
                } else {
                    _param.logOnce(" no field : " + field);
                }
            } else {
                if (param().isOutputDebugField(field)) {
                    log.info(" vrsOut(\"" + field + "\", " + (null == data ? data : "\"" + data + "\"") + ");");
                }
            }
            return _svf.VrsOut(field, data);
        }

        protected int vrImageOut(final String field, final String path) {
            if (null == path) {
                return -1;
            }
            return vrsOut(field, path);
        }

        protected int vrsOut(final String[] fields, final List data) {
            int dataidx = 0;
            int rtn = -1;
            for (int i = 0; i < fields.length && dataidx < data.size(); i++) {
                if (null != getSvfField(fields[i])) {
                    rtn = vrsOut(fields[i], (String) data.get(dataidx));
                    dataidx += 1;
                }
            }
            return rtn;
        }

        protected int vrsOutn(final String field, final int gyo, final String data) {
            if (null == getSvfField(field)) {
                if (param().isOutputDebugField(field)) {
                    log.warn(" no field : " + field + " (gyo = " + gyo + ", data = " + data + ")");
                } else {
                    _param.logOnce(" no field : " + field);
                }
            } else {
                if (param().isOutputDebugField(field)) {
                    log.info(" vrsOutn(\"" + field + "\", " + gyo + ", " + (null == data ? data : "\"" + data + "\"") + ");");
                }
            }
            return _svf.VrsOutn(field, gyo, data);
        }

        protected void vrsOutSelect(final String[][] fieldLists, final String data) {
            final int datasize = getMS932ByteLength(data);
            String[] fieldFound = null;
            boolean output = false;
            searchField:
            for (int i = 0; i < fieldLists.length; i++) {
                final String[] fieldnameList = fieldLists[i];
                int totalKeta = 0;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getSvfField(fieldname);
                    if (null == svfField) {
                        continue searchField;
                    }
                    totalKeta += svfField._fieldLength;
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                if (datasize <= totalKeta) {
                    final List<String> tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin); // fieldListの桁数はすべて同じ前提
                    if (tokenList.size() <= fieldnameList.length) {
                        for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                            vrsOut(fieldnameList[j], (String) tokenList.get(j));
                        }
                        output = true;
                        break searchField;
                    }
                }
            }
            if (!output && null != fieldFound) {
                final String[] fieldnameList = fieldFound;
                int ketaMin = -1;
                for (int j = 0; j < fieldnameList.length; j++) {
                    final String fieldname = fieldnameList[j];
                    final SvfField svfField = getSvfField(fieldname);
                    if (ketaMin == -1) {
                        ketaMin = svfField._fieldLength;
                    } else {
                        ketaMin = Math.min(ketaMin, svfField._fieldLength);
                    }
                    fieldFound = fieldnameList;
                }
                final List<String> tokenList = KNJ_EditKinsoku.getTokenList(data, ketaMin);
                for (int j = 0; j < Math.min(tokenList.size(), fieldnameList.length); j++) {
                    vrsOut(fieldnameList[j], (String) tokenList.get(j));
                }
                output = true;
            }
        }

        protected void printSvfRenban(final String field, final String data, final ShokenSize size) {
            if (param()._seitoSidoYorokuKinsokuFormJ) {
                vrsOut(field, crlfReplaced(data, _param));
            } else {
                printSvfRenban(field, getTokenList(data, size._mojisu * 2, size._gyo, _param));
            }
        }

        protected void printSvfRenban(final String field, final List list) {
            if (null != list) {
                for (int i = 0 ; i < list.size(); i++) {
                    vrsOutn(field, i + 1, (String) list.get(i));
                }
            }
        }

        protected int svfVrsOutnForKinsoku(final String field, final int n, final String data) {
            if (param()._seitoSidoYorokuKinsokuFormJ) { // 禁則処理で繰り返しを使用に表示不具合があるためフォームで繰り返しを使用しないようにする。
                return vrsOut(field + n, data);
            }
            return vrsOutn(field, n, data);
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

        private static class CharMS932 {
            final String _char;
            final String _b;
            final int _len;
            public CharMS932(final String v, final byte[] b) {
                _char = v;
                _b = btos(b);
                _len = b.length;
            }
            public String toString() {
                return "[" + _char + " : " + _b + " : " + _len + "]";
            }
        }
        private static List<CharMS932> toCharMs932List(final String src) throws Exception {
            final List<CharMS932> rtn = new ArrayList<CharMS932>();
            for (int j = 0; j < src.length(); j++) {
                final String z = src.substring(j, j + 1);             //1文字を取り出す
                final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
                rtn.add(c);
            }
            return rtn;
        }

        protected static List<String> getTokenList(final String targetsrc, final int dividlen, final int dividnum, final Param param) {
            final List<String> lines = getTokenList(targetsrc, dividlen, param);
            if (lines.size() > dividnum) {
                return lines.subList(0, dividnum);
            }
            return lines;
        }

        protected static List<String> getTokenList(String targetsrc, final int dividlen, final Param param) {
            if (targetsrc == null) {
                return Collections.emptyList();
            }
            if (param._useEditKinsoku) {
                return KNJ_EditKinsoku.getTokenList(targetsrc, dividlen);
            }
            final List<String> lines = new ArrayList<String>();         //編集後文字列を格納する配列
            int len = 0;
            StringBuffer stb = new StringBuffer();

            try {
                if (!StringUtils.replace(targetsrc, "\r\n", "\n").equals(targetsrc)) {
//                    log.fatal("改行コードが\\r\\n!");
//                    log.fatal(targetsrc);
//                    log.fatal(btos(targetsrc.getBytes("MS932")));
                    targetsrc = StringUtils.replace(targetsrc, "\r\n", "\n");
                }

                final List<CharMS932> charMs932List = toCharMs932List(targetsrc);

                for (final CharMS932 c : charMs932List) {
                    //log.debug(" c = " + c);

                    if (("\n".equals(c._char) || "\r".equals(c._char))) {
                        if (len <= dividlen) {
                            lines.add(stb.toString());
                            len = 0;
                            stb.delete(0, stb.length());
                        }
                    } else {
                        if (len + c._len > dividlen) {
                            lines.add(stb.toString());
                            len = 0;
                            stb.delete(0, stb.length());
                        }
                        stb.append(c._char);
                        len += c._len;
                    }
                }
                if (0 < len) {
                    lines.add(stb.toString());
                }
            } catch (Exception ex) {
                log.error("retDividString error! ", ex);
            }
            return lines;
        }

        protected void setForm(final String formname, final int n) {
            log.info(" set form = " + formname);
            _svf.VrSetForm(formname, n);

            _currentForm = formname;
            if (null != formname && null == param().formFieldInfoMap().get(_currentForm)) {
                param().formFieldInfoMap().put(_currentForm, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
                //debugFormInfo(param);
            }
        }

        protected SvfField getSvfField(final String fieldname) {
            return getMappedMap(param().formFieldInfoMap(), _currentForm).get(fieldname);
        }

        protected boolean hasSvfField(final String fieldname) {
            return null != getSvfField(fieldname);
        }

        protected int getFieldLength(final String fieldname, final int defval) {
            SvfField field = getSvfField(fieldname);
            return null == field ? defval : field._fieldLength;
        }

        protected String getHrNameField(final String data) {
            // TODO:
            final int hrNameFieldLen = getFieldLength("HR_NAME", 6);
            final int hrName2FieldLen = getFieldLength("HR_NAME_2", 0);
            final int hrName3FieldLen = getFieldLength("HR_NAME_3_1", 0);
            final int keta = getMS932ByteLength(data);
            String field = "HR_NAME";
            if (hrNameFieldLen < keta && hrNameFieldLen < hrName2FieldLen) {
                if (hrName2FieldLen < keta && hrName2FieldLen < hrName3FieldLen) {
                    field = "HR_NAME_3_1";
                } else {
                    field = "HR_NAME_2";
                }
            }
            return field;
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
        protected void printName(
                final String name,
                final SvfFieldData fieldData) {
            final double charSize = KNJSvfFieldModify.getCharSize(name, fieldData);
            _svf.VrAttribute(fieldData._fieldName, "Size=" + charSize);
            _svf.VrAttribute(fieldData._fieldName, "Y=" + (int) KNJSvfFieldModify.getYjiku(0, charSize, fieldData));
            vrsOut(fieldData._fieldName, name);
        }

        public static int getG(final Param param, final String year, final String annual) {
            int rtn = param.getGradeCd(year, annual);
            if (rtn == -1) {
                if (NumberUtils.isDigits(annual)) {
                    return Integer.parseInt(annual);
                }
            }
            return rtn;
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理] 生徒指導要録 様式1（学生に関する記録）
     */
    private static class KNJA133J_1 extends KNJA133J_0 {

        KNJA133J_1(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        /**
         * SVF-FORM 印刷処理
         */
        public int printSvf(final DB2UDB db2, final Student student) {
            final String form;
            if (param().isTokubetsuShien()) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    form = "KNJA134J_11.frm";
                } else { // CHITEKI1_知的障害.equals(param._chiteki) {
                    form = "KNJA134J_1.frm";
                }
            } else if (!StringUtils.isBlank(param().property(Property.knja133jForm1))) {
                form = param().property(Property.knja133jForm1);
            } else if (param()._z010.in(Z010.oomiya)) {
                form = "KNJA133J_11OOMIYA.frm";
            } else if (param()._z010.in(Z010.kaijyo)) {
                form = "KNJA133J_11KAIJO.frm";
            } else if (param()._z010.in(Z010.bunkyo)) {
                form = "KNJA133J_11BUNKYO.frm";
            } else if (param()._z010.in(Z010.tosa)) {
                form = "KNJA133J_11TOSA.frm";
            } else if (param()._z010.in(Z010.chiyodaKudan)) {
                form = "KNJA133J_11KUDAN.frm";
            } else if (param()._z010.in(Z010.naraken)) {
                form = "KNJA133J_11NARA.frm";
            } else {
                form = "KNJA133J_11.frm";
            }
            int page = 0;
            final List<PersonalInfo> personalInfoList = student.getPrintSchregEntGrdHistList(param());
            for (final PersonalInfo personalInfo : personalInfoList) {
                final String form1 = configForm(form, null, student, personalInfo);
                setForm(form1, 1);
                printSvfDefault(db2, personalInfo);
                printSchoolInfo(); // 学校情報
                printPersonalInfo(personalInfo); // 個人情報
                printAddress(personalInfo); // 住所履歴情報
                printGuardianAddress(personalInfo); // 保護者住所履歴情報
                printTransfer(db2, student, personalInfo); // 異動履歴情報
                printRegd1(student, personalInfo); // 年次・ホームルーム・整理番号
                printAftetGraduatedCourse(student);
                printRemark(db2, student, personalInfo);
                _svf.VrEndPage();
                hasdata = true;
                page += 1;
            }
            return page;
        }

        final String FLG_INKAN_SIZE = "INKAN_SIZE";
        @Override
        protected Map<String, String> getConfigFormMap(final SvfForm svfForm, final KaiteiInfo kaiteiInfo, final Student student, final PersonalInfo pi) {
            final Map<String, String> map = new TreeMap<String, String>();
            if (param()._z010.in(Z010.osakatoin)) {

                final List<String> addTanninInkan = new ArrayList<String>();
                for (final Gakuseki gakuseki : pi._gakusekiList) {

                    final List<Staff> studentStaff1HistList = param()._staffInfo.getStudentStaffHistList(param(), student, pi, StaffInfo.TR_DIV1, gakuseki._year);
                    final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
                    final Staff staff1First = Util.head(studentStaff1HistList, Staff.Null);
                    final Staff staff2Last = Util.last(param()._staffInfo.getStudentStaffHistList(param(), student, pi, StaffInfo.TR_DIV2, gakuseki._year), Staff.Null);
                    final Staff staff3Last = Util.last(param()._staffInfo.getStudentStaffHistList(param(), student, pi, StaffInfo.TR_DIV3, gakuseki._year), Staff.Null);

                    final List<Staff> yearStaffList = Util.flatten(Arrays.asList(Staff.getUniqueStaffList(staff1First, staff1Last), Staff.getUniqueStaffList(staff2Last), Staff.getUniqueStaffList(staff3Last)));
                    if (yearStaffList.size() > 2) {
                        log.info(" add tannin inkan " + gakuseki._year + " / " + Util.listString(yearStaffList, 0));
                        addTanninInkan.add(defstr(gakuseki._gradeCd));

                    }
                }
                map.put(FLG_INKAN_SIZE, mkString(addTanninInkan, "|").toString());
            }
            return map;
        }
        @Override
        protected void modifyForm(final SvfForm svfForm, final PersonalInfo personalInfo, final Map<String, String> modifyFormFlgMap) {

            if (modifyFormFlgMap.containsKey(FLG_INKAN_SIZE)) {
                final int scale = 3;
                final Map<String, SvfForm.Field> fieldMap = new HashMap<String, SvfForm.Field>();
                for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                    fieldMap.put(field._fieldname, field);
                }

                final String addTanninInkanStr = modifyFormFlgMap.get(FLG_INKAN_SIZE);
                final List<Integer> addTanninInkan = new ArrayList<Integer>();
                for (final String sg : addTanninInkanStr.split("|")) {
                    addTanninInkan.add(Integer.parseInt(sg));
                }

                for (int g = 1, maxGrade = 3; g <= maxGrade; g++) {
                    final String sg = String.valueOf(g);

                    for (final String kochoSuffix : Arrays.asList("", "C")) {
                        final String fieldname = "STAFFBTM_1_" + sg + kochoSuffix;

                        final ImageField kocho = svfForm.getImageField(fieldname);
                        if (null == kocho) {
                            log.warn(" no field kocho " + fieldname);
                            continue;
                        }
                        // サイズ変更
                        final int y = fieldMap.get("STAFFNAME_1_" + sg + "_3")._position._y;
                        final ImageField modified = kocho.setY(y).setHeight(kocho._height / scale).setEndX(kocho._point._x + (kocho._endX - kocho._point._x) / scale);
                        svfForm.move(kocho, modified);
                    }

                    for (final String tanninSuffix : Arrays.asList("", "_1", "_2", "C", "_1C", "_2C")) {
                        final String fieldname = "STAFFBTM_2_" + sg + tanninSuffix;
                        final ImageField tannin = svfForm.getImageField(fieldname);
                        if (null == tannin) {
                            log.warn(" no field tannin " + fieldname);
                            continue;
                        }
                        // サイズ変更
                        final ImageField kochoImageField = svfForm.getImageField("STAFFBTM_1_" + sg);
                        final int x = kochoImageField._point._x;
                        final String n = fieldname.endsWith("C") ? fieldname.substring(fieldname.length() - 2, fieldname.length() - 1) : fieldname.substring(fieldname.length() - 1);

                        final String nameField;
                        int ygap = 0;
                        if ("1".equals(n)) {
                            nameField = "STAFFNAME_2_" + sg + "_2";
                            if (addTanninInkan.contains(g)) {
                                 ygap = -75;
                            }
                        } else {
                            if (addTanninInkan.contains(g)) {
                                nameField = "STAFFNAME_2_" + sg + "_3";
                                 ygap = -50;
                            } else {
                                nameField = "STAFFNAME_2_" + sg + "_4";
                            }
                        }
                        log.info(" field name + " + fieldname + ", nameField = " + nameField);
                        final int y = fieldMap.get(nameField)._position._y;
                        final ImageField modified = tannin.setX(x).setY(y + ygap).setHeight(tannin._height / scale).setEndX(x + (tannin._endX - x) / scale);
                        svfForm.move(tannin, modified);
                    }
                }
                for (final Integer g : addTanninInkan) {
                    final String sg = g.toString();
                    final SvfForm.ImageField tannin = svfForm.getImageField("STAFFBTM_2_" + sg + "_2");
                    if (null == tannin) {
                        log.warn(" no field tannin " + tannin);
                        continue;
                    }
                    final ImageField kochoImageField = svfForm.getImageField("STAFFBTM_1_" + sg);
                    final int x = kochoImageField._point._x;

                    final int y = svfForm.getField("STAFFNAME_2_" + sg + "_4")._position._y;
                    final int ygap = -20;
                    final SvfForm.ImageField mono = tannin.setFieldname("STAFFBTM_2_" + sg + "_3").setX(x).setY(y + ygap).setHeight(tannin._height / scale).setEndX(x + (tannin._endX - x) / scale);
                    svfForm.addImageField(mono);
                    final SvfForm.ImageField colored = mono.setFieldname(mono._fieldname + "C").setColor("9");
                    svfForm.addImageField(colored);
                    log.info(" add image " + colored);
                }
            }
        }

        private void printSvfDefault(final DB2UDB db2, final PersonalInfo pInfo) {
            vrsOut("ENTERDIV1", "第　学年　入学");
            vrsOut("ENTERDIV2", "第　学年編入学");
            vrsOut("ENTERDIV3", "第　学年転入学");
            vrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(null) + "生");
            final String setDateFormat = param()._isSeireki ? "　　年   月   日" : KNJ_EditDate.setDateFormat(db2, null, param()._year);
            vrsOut("ENTERDATE1", setDateFormat);
//            vrsOut("ENTERDATE2", setDateFormat);
            vrsOut("ENTERDATE3", setDateFormat);
            if (param()._z010.in(Z010.kaijyo)) {
                vrsOut("TRANSFER_DATE_1", setDateFormat);
            } else {
                vrsOut("TRANSFER_DATE_1", "（" + setDateFormat + "）");
            }
            vrsOut("TRANSFER_DATE_2", "　" + setDateFormat + "　");
            vrsOut("TRANSFER_DATE_4", setDateFormat);
            vrsOut("GRADDATE", setDateFormat);
            if (param()._z010.in(Z010.kaijyo)) {
                vrsOut("GRADENAME2", "学年"); // 年次
            } else if (param()._z010.in(Z010.tosa)) {
                vrsOut("GRADENAME1", "学年"); // 年次
            }
            for (int i = 0; i < 6; i++) {
                vrsOut("YEAR_" + (i + 1), param()._isSeireki ? "　　年" : KNJ_EditDate.setNendoFormat(db2, null, param()._year));
            }
            if (pInfo._gakusekiList.size() > 0) {
                final Gakuseki gakuseki = pInfo._gakusekiList.get(0);
                final String iyear = param()._isSeireki ? "　　年" : KNJ_EditDate.setNendoFormat(db2, null, gakuseki._year);
                for (int g = 1; g <= gakuseki._gradeCd; g++) {
                    vrsOut("YEAR_" + String.valueOf(g), iyear);
                }
            }
        }

        /**
         * 学校情報
         */
        private void printSchoolInfo() {
            final String schoolName1 = !StringUtils.isEmpty(param()._certifSchoolName) ? param()._certifSchoolName : param()._schoolName1;
            if (param()._z010.in(Z010.kaijyo)) {
                vrsOut("NAME_gakko1", schoolName1); // 学校名
                vrsOut("NAME_gakko2", null); // 学校名
            } else {
                vrsOut("NAME_gakko1", schoolName1);
                if (param()._z010.in(Z010.tosa)) {
                    _svf.VrAttribute("NAME_gakko1", "Hensyu=3"); // 中央寄
                    vrsOut("NAME_gakko2", param()._schoolTelno); // 学校名
                    _svf.VrAttribute("NAME_gakko2", "Hensyu=3"); // 中央寄
                }
            }
            if (null != param()._schoolzip) {
                vrsOut("ZIPCODE", param()._schoolZipCd);
            }

            if (param()._z010.in(Z010.kaijyo)) {
                final int len1 = getMS932ByteLength(param()._schoolAddr1);
                final int len2 = getMS932ByteLength(param()._schoolAddr2);
                String f;
                if (len1 > 50 || len2 > 50) {
                    f  ="_3";
                } else if (len1 > 50 || len2 > 50) {
                    f  ="_2";
                } else {
                    f  ="";
                }
                vrsOut("ADDRESS_gakko1" + f, param()._schoolAddr1);
                vrsOut("ADDRESS_gakko2" + f, param()._schoolAddr2);
                vrsOut("ADDRESS_gakko3" + f, null);
            } else {
                if (0 < getMS932ByteLength(param()._schoolAddr1)) {
                    vrsOut("ADDRESS_gakko1", param()._schoolAddr1);
                    if (param()._z010.in(Z010.tosa)) {
                        _svf.VrAttribute("ADDRESS_gakko1", "Hensyu=3"); // 中央寄
                    }
                }
                if (0 < getMS932ByteLength(param()._schoolAddr2)) {
                    vrsOut("ADDRESS_gakko2", param()._schoolAddr2);
                }
            }
        }

        /**
         * 個人情報
         */
        private void printPersonalInfo(final PersonalInfo personalInfo) {
            if (param()._z010.in(Z010.kaijyo)) {
                vrsOut("COURSENAME", personalInfo._courseName); // 課程名
                vrsOut("MAJORNAME", personalInfo._majorName); // 学科名
            } else {
                vrsOut("COURSE", personalInfo._courseName);
                vrsOut("MAJOR", personalInfo._majorName);
            }
            if (personalInfo._studentKana != null || personalInfo._guardKana != null) {
                int posx1 = 749;
                int posx2 = posx1 + 800;
                int gposx2 = posx2;
                int height = 40;
                final int minnum = 20;
                final int maxnum = 100;
                int ystart = 929;
                int gystart = 1893;
                if (param()._z010.in(Z010.kaijyo)) {
                    posx1 = 719;
                    posx2 = posx1 + 800;
                    gposx2 = posx1 + 1000;
                    ystart = 933;
                    gystart = 1602;
                } else if (param()._z010.in(Z010.bunkyo)) {
                    ystart = 1007;
                    gystart = 1971;
                } else if (param()._z010.in(Z010.tosa) || param()._z010.in(Z010.chiyodaKudan)) {
                    final SvfField fieldKana = getSvfField("KANA");
                    posx1 = fieldKana.x();
                    ystart = fieldKana.y();
                    posx2 = posx1 + (int) SvfFieldData.fieldWidth(fieldKana, 0);
                    height = (int) SvfFieldData.charHeightPixel("", param()._z010.in(Z010.chiyodaKudan) ? 8.0 : 10.0);

                    final SvfField fieldGuardKana = getSvfField("GUARD_KANA");
                    gystart = fieldGuardKana.y();
                    gposx2 = posx2;
                }
                if (personalInfo._studentKana != null) {
                    final SvfFieldData fieldData = new SvfFieldData("KANA", posx1, posx2, height, minnum, maxnum, ystart);
                    printName(personalInfo._studentKana, fieldData);
                }
                if (personalInfo._guardKana != null) {
                    final SvfFieldData fieldData = new SvfFieldData("GUARD_KANA", posx1, gposx2, height, minnum, maxnum, gystart);
                    printName(personalInfo._guardKana, fieldData);
                }
            }
            if (null != param()._simei) {
                int posx1 = 749;
                int posx2 = 749 + 800;
                int gposx2 = posx2;
                int height = 60;
                int gheight = height;
                final int minnum = 20;
                final int maxnum = 48;
                int ystart = 1080, ystart1 = 1040, ystart2 = 1120;
                int gystart = 2044, gystart1 = 2004, gystart2 = 2084;
                if (param()._z010.in(Z010.kaijyo)) {
                    posx1 = 719;
                    posx2 = posx1 + 780;
                    gposx2 = posx1 + 980;
                    ystart = 1073;
                    ystart1 = 1033;
                    ystart2 = 1113;
                    gystart = 1750 - 10;
                    gystart1 = 1710 - 10;
                    gystart2 = 1790 - 10;
                } else if (param()._z010.in(Z010.bunkyo)) {
                    ystart = 1158;
                    ystart1 = 1118;
                    ystart2 = 1196;
                    gystart = 2122 - 10;
                    gystart1 = 2082 - 10;
                    gystart2 = 2162 - 10;
                } else if (param()._z010.in(Z010.tosa) || param()._z010.in(Z010.chiyodaKudan) || param()._z010.in(Z010.fukuiken)) {
                    final SvfField fieldName1 = getSvfField("NAME1");
                    final SvfField fieldName1_1 = getSvfField("NAME1_1");
                    final SvfField fieldName2_1 = getSvfField("NAME2_1");
                    posx1 = fieldName1.x();
                    ystart = fieldName1.y();
                    ystart1 = fieldName1_1.y();
                    ystart2 = fieldName2_1.y();
                    posx2 = posx1 + (int) SvfFieldData.fieldWidth(fieldName1, 0);
                    if (param()._z010.in(Z010.fukuiken)) {
                        height = 80;
                        gheight = (int) SvfFieldData.charHeightPixel("guard name height", 12);
                    } else {
                        height = (int) SvfFieldData.charHeightPixel("", param()._z010.in(Z010.chiyodaKudan) ? 14.0 : 16.0);
                        gheight = height;
                    }

                    final SvfField fieldGuardName1 = getSvfField("GUARD_NAME1");
                    final SvfField fieldGuardName1_1 = getSvfField("GUARD_NAME1_1");
                    final SvfField fieldGuardName2_1 = getSvfField("GUARD_NAME2_1");
                    gystart = fieldGuardName1.y();
                    gystart1 = fieldGuardName1_1.y();
                    gystart2 = fieldGuardName2_1.y();
                    gposx2 = posx2;
                }

                final String name1 = personalInfo._studentName1;
                final String name2 = personalInfo._studentNameHistFirst;

                if (StringUtils.isBlank(name2) || name2.equals(name1)) {
                    // 履歴なしもしくは履歴の名前が現データの名称と同一
                    final SvfFieldData fieldData1 = new SvfFieldData("NAME1", posx1, posx2, height, minnum, maxnum, ystart);
                    printName(name1, fieldData1);
                } else {
                    final SvfFieldData fieldData11 = new SvfFieldData("NAME1_1", posx1, posx2, height, minnum, maxnum, ystart1);
                    final SvfFieldData fieldData21 = new SvfFieldData("NAME2_1", posx1, posx2, height, minnum, maxnum, ystart2);
                    printName(name2, fieldData11);
                    printCancelLine(fieldData11._fieldName, Math.min(getMS932ByteLength(name2), maxnum)); // 打ち消し線
                    printName(name1, fieldData21);
                }

                final String gname1 = personalInfo._guardName;
                final String gname2 = personalInfo._guardNameHistFirst;

                if (StringUtils.isBlank(gname2) || gname2.equals(gname1)) {
                    // 履歴なしもしくは履歴の名前が現データの名称と同一
                    final SvfFieldData fieldDataG1 = new SvfFieldData("GUARD_NAME1", posx1, gposx2, gheight, minnum, maxnum, gystart);
                    printName(gname1, fieldDataG1);
                } else {
                    final SvfFieldData fieldDataG11 = new SvfFieldData("GUARD_NAME1_1", posx1, gposx2, gheight, minnum, maxnum, gystart1);
                    final SvfFieldData fieldDataG21 = new SvfFieldData("GUARD_NAME2_1", posx1, gposx2, gheight, minnum, maxnum, gystart2);
                    printName(gname2, fieldDataG11);
                    printCancelLine(fieldDataG11._fieldName, Math.min(getMS932ByteLength(gname2), maxnum)); // 打ち消し線
                    printName(gname1, fieldDataG21);
                }
            }
            vrsOut("BIRTHDAY", personalInfo._birthdayStr);
            if (personalInfo._sex != null) {
                vrsOut("SEX", personalInfo._sex);
            }
            printFinSchool(personalInfo); // 入学前学歴の学校名編集
        }

        /**
         * 保護者住所履歴を印刷します。
         * @param student
         */
        private void printGuardianAddress(final PersonalInfo personalInfo) {
            final String SAME_TEXT = "生徒の欄に同じ";
            final int max = param()._z010.in(Z010.kaijyo) ? 1 : 2;
            if (isSameAddressList(take(personalInfo._addressList, max), take(personalInfo._guardianAddressList, max))) {
                // 住所が生徒と同一
                vrsOut("GUARDIANADD1_1_1", SAME_TEXT);
                return;
            }
            final int num = personalInfo._guardianAddressList.size();
            int i = Math.min(max, num); // 出力件数

            final Address studentAddressMax = personalInfo.getStudentAddressMax();
            final String addr1 = null ==  studentAddressMax ? "" : StringUtils.defaultString(studentAddressMax._address1);
            final String addr2 = null ==  studentAddressMax ? "" : StringUtils.defaultString(studentAddressMax._address2);

            for (final Iterator<Address> it = personalInfo._guardianAddressList.iterator(); it.hasNext() && i > 0; i--) {
                final Address guardianAddressRec = it.next();

                final String address1 = StringUtils.defaultString(guardianAddressRec._address1);
                final String address2 = StringUtils.defaultString(guardianAddressRec._address2);

                if ((i == max || i == personalInfo._guardianAddressList.size()) && addr1.equals(address1) && addr2.equals(address2)) {
                    // 最新の住所かつ内容が生徒と同一
                    vrsOut("GUARDIANADD" + i + "_1_1", "生徒の欄に同じ");
                } else {
                    if (param()._schzip != null) {
                        vrsOut("GUARDZIP" + i, guardianAddressRec._zipCd);
                        printAddressLine("GUARDZIPLINE" + i, guardianAddressRec._zipCd, i, num, max);
                    }
                    final int n1 = getMS932ByteLength(address1);
                    final int n2 = getMS932ByteLength(address2);
                    final boolean use3 = (50 < n1 || 50 < n2) && hasSvfField("GUARDIANADD" + i + "_1_3") && hasSvfField("GUARDIANADD" + i + "_2_3");
                    final String p = use3 ? "3" : 40 < n2 || 40 < n1 ? "2" : "1";
                    if (0 < n1) {
                        vrsOut("GUARDIANADD" + i + "_1_" + p, address1);
                        printAddressLine("GUARDIANADDLINE" + i + "_1_" + p, address1, i, num, max);
                    }
                    if (0 < n2 && guardianAddressRec._isPrintAddr2) {
                        vrsOut("GUARDIANADD" + i + "_2_" + p, address2);
                        printAddressLine("GUARDIANADDLINE" + i + "_2_" + p, address2, i, num, max);
                    }
                }
            }
        }


        /**
         * 個人情報 住所履歴情報 履歴を降順に読み込み、最大件数まで出力
         */
        private void printAddress(final PersonalInfo personalInfo) {
            final int max = param()._z010.in(Z010.kaijyo) ? 1 : 2;
            final int num = personalInfo._addressList.size();
            int i = Math.min(max, num); // 出力件数

            for (final Iterator<Address> it = personalInfo._addressList.iterator(); it.hasNext() && i > 0; i--) {
                final Address addressRecord = it.next();

                if (param()._schzip != null) {
                    vrsOut("ZIPCODE" + i, addressRecord._zipCd);
                    printAddressLine("ZIPCODELINE" + i, addressRecord._zipCd, i, num, max);
                }
                final int n1 = getMS932ByteLength(addressRecord._address1);
                final int n2 = getMS932ByteLength(addressRecord._address2);
                final String field = (50 < n2 || 50 < n1) && (hasSvfField("ADDRESS" + i + "_1_3") && hasSvfField("ADDRESS" + i + "_2_3")) ? "3" : 40 < n2 || 40 < n1 ? "2" : "1";
                if (0 < n1) {
                    final String field1 = "ADDRESS" + i + "_1_" + field;
                    final String field2 = "ADDRESSLINE" + i + "_1_" + field;
                    vrsOut(field1, addressRecord._address1);
                    printAddressLine(field2, addressRecord._address1, i, num, max);
                }
                if (0 < n2 && addressRecord._isPrintAddr2) {
                    final String field1 = "ADDRESS" + i + "_2_" + field;
                    final String field2 = "ADDRESSLINE" + i +"_2_" + field;
                    vrsOut(field1, addressRecord._address2);
                    printAddressLine(field2, addressRecord._address2, i, num, max);
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

        private <T> List<T> reverse(final List<T> list) {
            final List<T> rtn = new ArrayList<T>();
            for (final ListIterator<T> it = list.listIterator(list.size()); it.hasPrevious();) {
                final T o = it.previous();
                rtn.add(o);
            }
            return rtn;
        }

        /**
         *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
         *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
         *  全角スペースより前半の文字を○○○○○立と見なします。
         */
        private void printFinSchool(final PersonalInfo pi) {

            vrsOut("J_GRADUATEDDATE_Y", pi._finishDateFormatted);

            if (param()._isInstallationDivPrint) {
                vrsOut("INSTALLATION_DIV", pi._installationDiv);
            }

            final String tname;
            if (param()._z010.in(Z010.CHIBEN) || param()._z010.in(Z010.kaijyo) || "1".equals(param().property(Property.notPrintFinschooltypeName))) {
                tname = "";
            } else {
                tname = StringUtils.defaultString(pi._finschoolTypeName);
            }
            String sotugyo;
            if (param().isTokubetsuShien() && null == pi._jName || param()._hasFINSCHOOL_DETAIL_MST && "1".equals(pi._finschoolNotPrintSotsugyo)) {
                sotugyo = "";
            } else {
                sotugyo = "卒業";
            }

            final String schoolName;
            if (null == pi._jName) {
                schoolName = "";
            } else {
                final int i = pi._jName.indexOf('　');  // 全角スペース
                if (-1 < i && 5 >= i) {
                    final String ritu = pi._jName.substring(0, i);
                    if (null != ritu) {
                        vrsOut("INSTALLATION_DIV",  ritu + "立");
                    }
                    schoolName = pi._jName.substring(i + 1);
                } else {
                    schoolName = pi._jName;
                }
            }
            final int schoolNameLen = getMS932ByteLength(schoolName);

            final String kotei = tname + sotugyo;
            final int koteiLen = getMS932ByteLength(kotei);

            if (schoolNameLen == 0) {
                vrsOut("FINSCHOOL1", kotei);
            } else if (schoolNameLen + koteiLen <= 40) {
                vrsOut("FINSCHOOL1", schoolName + kotei);
            } else if(schoolNameLen + koteiLen <= 50) {
                vrsOut("FINSCHOOL2", schoolName + kotei);
            } else {
                vrsOut("FINSCHOOL2", schoolName);
                vrsOut("FINSCHOOL3", kotei);
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
            printCancelLine(field, getMS932ByteLength(str));
        }

        /**
         * 取り消し線印刷
         * @param svf
         * @param keta 桁
         */
        private void printCancelLine(final String field, final int keta) {
            _svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + keta);
        }

        private String formatDate(final DB2UDB db2, final String date) {
            if (StringUtils.isBlank(date)) {
                return "　   年   月   日";
            }
            if (param()._isSeireki) {
                return KNJ_EditDate.h_format_S(date, "yyyy") + " 年 " + setDateFormat2(param(), KNJ_EditDate.h_format_JP_MD(date));
            }
            return KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP(db2, date), param()._year);
        }

        /**
         * 個人情報 異動履歴情報
         */
        private void printTransfer(final DB2UDB db2, final Student  student, final PersonalInfo pi) {
            printCancelLine("LINE1", 14);
            printCancelLine("LINE2", 14);
            if (param()._z010.in(Z010.kaijyo)) {
                printCancelLine("LINE3", 14);
            }

            final int A002_NAMECD2_TENNYUGAKU = 4;
            final int A002_NAMECD2_5 = 5;
            final int A003_NAMECD2_SOTSUGYO = 1;
            final int A003_NAMECD2_TAIGAKU = 2;
            final int A003_NAMECD2_TENGAKU = 3;
            final int A003_NAMECD2_SHURYOU = 5;
            final int A003_NAMECD2_JOSEKI = 6;

            final boolean isPrintReason = !param()._z010.in(Z010.seijyo);

            // 入学区分
            if (null != pi._entDiv) {
                final String entGradeStr;
                if (pi._entYear != null) {
                    final int diff = Integer.parseInt(param()._year) - Integer.parseInt(pi._entYear);
                    final int currentGradeCd = student.currentGradeCd(param());
                    final int grade = currentGradeCd - diff;
                    entGradeStr = " " + (0 >= grade ? " " : String.valueOf(grade));
                } else {
                    entGradeStr = "  ";
                }
                final int entdiv = pi._entDiv.intValue();
                if (entdiv == A002_NAMECD2_TENNYUGAKU) { // 転入学
                    if (pi._entDate != null) {
                        vrsOut("ENTERDATE3", formatDate(db2, pi._entDate));
                    }
                    vrsOut("ENTERDIV3", "第" + entGradeStr + "学年転入学");
                    _svf.VrAttribute("LINE3", "X=5000");
                    if (param()._z010.in(Z010.kaijyo)) {
                        vrsOut("ENTER_NAME2_" + (getMS932ByteLength(pi._entSchool) > 50 ? "2" : "1"), pi._entSchool); // 転学前名称
                        vrsOut("ENTER_ADDRESS2_" + (getMS932ByteLength(pi._entAddr) > 50 ? "2" : "1"), pi._entAddr); // 転学前所在地
                        vrsOut("TRANSFERREASON1_2", pi._entReason); // 転入学理由
                    } else if (param()._z010.in(Z010.naraken)) {
                        vrsOut("TRANSFERREASON1_2", pi._entSchool); // 転学前名称
                        vrsOut("TRANSFERREASON1_3", pi._entAddr); // 転学前所在地
                        vrsOut("TRANSFERREASON1_1", pi._entReason); // 転入学理由
                    } else {
                        final List<String> data = new ArrayList<String>();
                        data.add(pi._entSchool);
                        data.add(pi._entAddr);
                        if (isPrintReason) {
                            data.add(pi._entReason);
                        }
                        final String[] field = {"2", "3", "1"};
                        for (int i = 0; i < Math.min(field.length, data.size()); i++) {
                            vrsOut("TRANSFERREASON1_" + field[i], data.get(i));
                        }
                    }
                } else {
                    final String title;
                    final String enterdateField;
                    final String enterdivField;
                    final String lineField;
                    if (param()._z010.in(Z010.kaijyo)) {
                        if (entdiv != A002_NAMECD2_5) {
                            title = "　入学";
                        } else {
                            title = "編入学";
                        }
                        enterdateField = "ENTERDATE1";
                        lineField = "LINE1";
                        enterdivField = "ENTERDIV1";
                    } else {
                        if (entdiv != A002_NAMECD2_5) {
                            title = "　入学";
                            enterdateField = "ENTERDATE1";
                            lineField = "LINE1";
                            enterdivField = "ENTERDIV1";
                        } else {
                            title = "編入学";
                            enterdateField = "ENTERDATE2";
                            lineField = "LINE2";
                            enterdivField = "ENTERDIV2";
                        }
                    }
                    if (pi._entDate != null) {
                        vrsOut(enterdateField, formatDate(db2, pi._entDate));
                    }
                    _svf.VrAttribute(lineField, "X=5000");
                    vrsOut(enterdivField, "第" + entGradeStr + "学年" + title);

                    if (param()._z010.in(Z010.kaijyo)) {
                        vrsOut("ENTER_NAME1_" + (getMS932ByteLength(pi._entSchool) > 50 ? "2" : "1"), pi._entSchool); // 入学前名称
                        vrsOut("ENTER_ADDRESS1" + (getMS932ByteLength(pi._entAddr) > 50 ? "_2" : "_1"), pi._entAddr); // 入学前所在地
                        vrsOut("ENTERRESONS2", pi._entReason); // 入学理由
                    } else if (param()._z010.in(Z010.naraken)) {
                        if (entdiv == A002_NAMECD2_5) {
                            int keta = 20;
                            if (null != getSvfField("ENTERRESONS2")) {
                                keta = getSvfField("ENTERRESONS2")._fieldLength;
                            }
                            vrsOut(new String[] {"ENTERRESONS2", "ENTERRESONS2_1_2"}, getTokenList(pi._entReason, keta, param())); // 入学理由
                        }
                    } else {
                        final List<String> data = new ArrayList<String>();
                        data.add(pi._entSchool);
                        data.add(pi._entAddr);
                        if (isPrintReason) {
                            data.add(pi._entReason);
                        }
                        final String[] field = {"2", "3", "1"};
                        for (int i = 0; i < Math.min(field.length, data.size()); i++) {
                            vrsOut("ENTERRESONS" + field[i], data.get(i));
                        }
                    }
                }
            }

            // 除籍区分
            if (null != pi._grdDiv) {
                final String gradeStr;
                if (pi._grdYear != null) {
                    final int diff = Integer.parseInt(pi._grdYear) - Integer.parseInt(param()._year);
                    final int currentGradeCd = student.currentGradeCd(param());
                    final int grade = currentGradeCd + diff;
                    log.debug(" diff = " + diff + ", " + pi._grdYear + " - " + param()._year);
                    log.debug(" grade = " + grade);
                    gradeStr = " " + (0 >= grade ? " " : String.valueOf(grade));
                } else {
                    gradeStr = "  ";
                }
                final String printGradeStr = "第" + gradeStr + "学年";
                final int grddiv = pi._grdDiv.intValue();
                if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_JOSEKI) {
                    vrsOut("TRANSFER_DATE_1", "　" + KNJ_EditDate.setDateFormat(db2, null, param()._year) + "　");
                    vrsOut("TRANSFER_DATE_2", "");
                }
                if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_TENGAKU || grddiv == A003_NAMECD2_JOSEKI) {
                    if (!param()._z010.in(Z010.kaijyo)) {
                        final int fieldKeta = getFieldLength("KUBUN", 0);
                        if (16 <= fieldKeta) {
                            vrsOut("KUBUN", printGradeStr + pi._grdDivName); // 転学・退学
                        } else if (0 < fieldKeta) {
                            vrsOut("KUBUN", pi._grdDivName); // 転学・退学
                        }
                    }
                    if (pi._grdDate != null) {

                        final String formatted = formatDate(db2, pi._grdDate);
                        final String td1;
                        if (param()._z010.in(Z010.kaijyo)) {
                            td1 = formatted + "　" + printGradeStr + "　" + StringUtils.defaultString(pi._grdDivName);
                        } else if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_JOSEKI) {
                            td1 = "　" + formatted + "　";
                        } else {
                            td1 = "（" + formatted + "）";
                        }
                        vrsOut("TRANSFER_DATE_1", td1); // 転学・退学日
                    }

                    if (grddiv == A003_NAMECD2_TENGAKU) {
                        final String transferDate2 = StringUtils.defaultString(pi._tengakuSakiZenjitu, pi._grdDate);
                        if (null != transferDate2) {
                            final String formatted = formatDate(db2, transferDate2);
                            vrsOut("TRANSFER_DATE_2", "　" + formatted + "　");
                        }
                    }
                }

                if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_TENGAKU || grddiv == A003_NAMECD2_JOSEKI) {
                    if (param()._z010.in(Z010.kaijyo)) {
                        vrsOut("TRANSFER_NAME" + (getMS932ByteLength(pi._grdSchool) > 50 ? "2" : "1"), pi._grdSchool); // 転学・退学先名称
                        vrsOut("TRANSFER_ADDRESS" + (getMS932ByteLength(pi._grdAddr) > 50 ? "2" : "1"), pi._grdAddr); // 転学・退学先所在地
                        final String s2 = pi._grdReason;
                        final String s3 = "";
                        final String s1 = "";
                        vrsOut("TRANSFERREASON2_2", s2); // 転学・退学理由
                        vrsOut("TRANSFERREASON2_3" + (getMS932ByteLength(s3) > 50 ? "_2" : ""), s3); // 転学・退学理由
                        vrsOut("TRANSFERREASON2_1" + (getMS932ByteLength(s1) > 50 ? "_2" : ""), s1); // 転学・退学理由
                    } else if (param()._z010.in(Z010.tosa) && grddiv == A003_NAMECD2_TENGAKU) {
                        vrsOut("TRANSFERREASON2_1", pi._grdSchool);
                        vrsOut("TRANSFERREASON2_2", Util.append(pi._tengakuSakiGrade, "　転学"));
                        vrsOut("TRANSFERREASON2_3", pi._grdAddr);
                    } else if (param()._z010.in(Z010.naraken)) {
                        if (grddiv == A003_NAMECD2_TENGAKU) {
                            vrsOut("TRANSFER_NAME1", pi._grdSchool); // 転学・退学先名称
                            vrsOut("TRANSFER_ADDRESS1", pi._grdAddr); // 転学・退学先所在地
                            vrsOut("TRANSFER_GRADE", pi._tengakuSakiGrade);
                            vrsOut(new String[] {"TRANSFER_REASON1", "TRANSFER_REASON2"}, getTokenList(pi._grdReason, 34, param()));

                        } else if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_JOSEKI) {
                            vrsOut(new String[] {"TRANSFER_REASON_TAIGAKU1", "TRANSFER_REASON_TAIGAKU2"}, getTokenList(pi._grdReason, 34, param()));
                        }
                    } else {
                        final List<String> data = new ArrayList<String>();
                        data.add(pi._grdSchool);
                        data.add(pi._grdAddr);
                        if (isPrintReason) {
                            data.add(pi._grdReason);
                        }
                        final String[] field = {"2", "3", "1"};
                        for (int i = 0; i < Math.min(field.length, data.size()); i++) {
                            vrsOut("TRANSFERREASON2_" + field[i], data.get(i));
                        }
                    }
                } else if (grddiv == A003_NAMECD2_SOTSUGYO || grddiv == A003_NAMECD2_SHURYOU) {
                    // 1:卒業 or 5:修了
                    if (pi._grdDate != null) {
                        final String formatted = formatDate(db2, pi._grdDate);
                        vrsOut("TRANSFER_DATE_4", formatted);
                    }
                }
            }

            if (param()._z010.in(Z010.kaijyo)) {
                for (final Map transferInfo : pi._transferInfoList) {

                    final String transferSdate = KnjDbUtils.getString(transferInfo, "TRANSFER_SDATE");
                    final String transferEdate = KnjDbUtils.getString(transferInfo, "TRANSFER_EDATE");
                    final String transferreason = KnjDbUtils.getString(transferInfo, "TRANSFERREASON");
                    final String transferplace = KnjDbUtils.getString(transferInfo, "TRANSFERPLACE");
                    final String transferaddr = KnjDbUtils.getString(transferInfo, "TRANSFERADDR");
                    final String fromTo = KNJ_EditDate.h_format_JP(db2, transferSdate) + " ～ " + formatDate(db2, transferEdate);
                    if ("1".equals(KnjDbUtils.getString(transferInfo, "TRANSFERCD"))) {
                        vrsOut("ABROAD_DATE", fromTo); // 留学日
                        vrsOut("ABROAD_NAME1_" + (getMS932ByteLength(transferreason) > 50 ? "2" : "1"), transferreason); // 留学先名称
                        vrsOut("ABROAD_NAME2_" + (getMS932ByteLength(transferplace) > 50 ? "2" : "1"), transferplace); // 留学先名称
                        vrsOut("ABROAD_ADDRESS" + (getMS932ByteLength(transferaddr) > 50 ? "2" : "1"), transferaddr); // 留学先所在地
                    } else if ("2".equals(KnjDbUtils.getString(transferInfo, "TRANSFERCD"))) {
                        vrsOut("OFFDAYS_DATE", fromTo); // 休学日
                        vrsOut("OFFDAYSREASON2_2", transferreason); // 休学理由
                        vrsOut("OFFDAYSREASON2_3" + (getMS932ByteLength("") > 50 ? "_2" : ""), ""); // 休学理由
                        vrsOut("OFFDAYSREASON2_1" + (getMS932ByteLength("") > 50 ? "_2" : ""), ""); // 休学理由
                    }
                }
            }
        }

        private void printRegd1(final Student student, final PersonalInfo personalInfo) {
            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                if (!student.isPrintYear(gakuseki._year, param())) {
                    continue;
                }
                if (null != gakuseki._year && !(personalInfo.getYearBegin() <= Integer.parseInt(gakuseki._year) && Integer.parseInt(gakuseki._year) <= personalInfo.getYearEnd(param()))) {
                    log.info(" skip print year = " + gakuseki._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd(param()));
                    continue;
                }
                final String si = String.valueOf(gakuseki._i);
                if (param()._z010.in(Z010.bunkyo)) {
                    vrsOut("YEAR_" + si + "_2", gakuseki._nendo); // 組
                }
                vrsOutSelect(new String[][] {{"HR_CLASS1_" + si}, {"HR_CLASS2_" + si}}, gakuseki._hrname); // 組
                vrsOut("ATTENDNO_" + si, String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号

                vrsOut("STAFFNAME_1_" + si, "");
                vrsOut("STAFFNAME_1_" + si + "_1", "");
                vrsOut("STAFFNAME_1_" + si + "_2", "");
                vrsOut("STAFFNAME_1_" + si + "_3", "");
                vrsOut("STAFFNAME_1_" + si + "_4", "");
                vrsOut("STAFFNAME_2_" + si, "");
                vrsOut("STAFFNAME_2_" + si + "_1", "");
                vrsOut("STAFFNAME_2_" + si + "_2", "");
                vrsOut("STAFFNAME_2_" + si + "_3", "");
                vrsOut("STAFFNAME_2_" + si + "_4", "");

                if (param()._z010.in(Z010.tosa)) {
                    vrsOut("GRADE1_" + si, String.valueOf(gakuseki._gradeCd)); // 学年
                }
                if (param()._z010.in(Z010.kaijyo) || param()._z010.in(Z010.tosa)) {
                    vrsOut("GRADE_" + si, String.valueOf(gakuseki._gradeCd)); // 学年
                }
                vrsOut("YEAR_" + si, gakuseki._nendoFormatted);

                if (!(Integer.parseInt(gakuseki._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    continue;
                }

                final List<Map<String, String>> principalList = getMappedList(param()._staffInfo._yearPrincipalListMap, gakuseki._year);
                String principalStaffcd1 = null;
                String principalStaffcd2 = null;
                String principal1FromDate = null;
                String principal1ToDate = null;
                String principal2FromDate = null;
                String principal2ToDate = null;
                if (null != principalList && principalList.size() > 0) {
                    final Map<String, String> listLast = principalList.get(principalList.size() - 1);

                    Map last = null;
                    if (null != personalInfo._grdDate) {
                        // 生徒の卒業日付以降の校長は対象外
                        for (int j = 0; j < principalList.size(); j++) {
                            final Map<String, String> principal = principalList.get(j);
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
                printStaffName("1", gakuseki._i, true, gakuseki._principal, principal1, principal2);

                final List<Staff> studentStaff1HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV1, gakuseki._year);
                final Staff staff1Last = Util.last(studentStaff1HistList, Staff.Null);
                final Staff staff1First = Util.head(studentStaff1HistList, Staff.Null);

                final List<Staff> studentStaff2HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV2, gakuseki._year);
                final Staff staff2Last = Util.last(studentStaff2HistList, Staff.Null);

                final List<Staff> studentStaff3HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV3, gakuseki._year);
                final Staff staff3Last = Util.last(studentStaff3HistList, Staff.Null);

                // 担任氏名
                if (param()._z010.in(Z010.chiyodaKudan) || param()._z010.in(Z010.osakatoin) || param()._z010.in(Z010.osakashinnai)) {
                    printStaffName2("2", gakuseki._i, Arrays.asList(Staff.getUniqueStaffList(staff1First, staff1Last), Staff.getUniqueStaffList(staff2Last), Staff.getUniqueStaffList(staff3Last)));
                } else {
                    printStaffName("2", gakuseki._i, false, Staff.Null, staff1Last, staff1First);
                }


//                log.debug("印影："+param()._inei);
//                log.debug("改竄："+gakuseki._kaizanFlg); //改竄されていないか？
//                log.debug("署名（校長）："+gakuseki._principalSeq); //署名（校長）しているか？
//                log.debug("署名（担任）："+gakuseki._staffSeq); //署名（担任）しているか？
                //印影
                if (null != param()._inei) {
                    final String col = "1".equals(param()._colorPrint) ? "C" : "";
                    if (null == gakuseki._kaizanFlg && null != gakuseki._principalSeq || Arrays.asList("2", "4").contains(param()._inei)) {
                        final String stampNo = null == principal1._staffMst._staffcd ? gakuseki._principal._stampNo : principal1._stampNo;
                        vrImageOut("STAFFBTM_1_" + si + col, param().getImageFilePath(stampNo)); // 校長印
                    }
                    if (null == gakuseki._kaizanFlg && null != gakuseki._staffSeq || Arrays.asList("2", "3", "4").contains(param()._inei)) {
                        if (param()._z010.in(Z010.osakatoin) && staff2Last != Staff.Null) {
                            log.info(" inei staff = " + staff1Last + ", " + staff2Last + ", " + staff3Last);
                            vrImageOut("STAFFBTM_2_" + si + "_1" + col, param().getImageFilePath(staff1Last._stampNo)); // 担任印
                            vrImageOut("STAFFBTM_2_" + si + "_2" + col, param().getImageFilePath(staff2Last._stampNo)); // 担任印
                            vrImageOut("STAFFBTM_2_" + si + "_3" + col, param().getImageFilePath(staff3Last._stampNo)); // 担任印
                        } else {
                            vrImageOut("STAFFBTM_2_" + si + col, param().getImageFilePath(staff1Last._stampNo)); // 担任印
                        }
                    }
                }
            }
        }

        private void printStaffName(final String j, final int i, final boolean isCheckStaff0, final Staff staff0, final Staff staff1, final Staff staff2) {
            final int keta = 26;
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                vrsOutSelect(new String[][] {{"STAFFNAME_" + j + "_" + i}, {"STAFFNAME_" + j + "_" + i + "_1"}}, staff0.getNameString(param(), keta));
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List<String> line = new ArrayList<String>();
                line.addAll(staff1._staffMst.getNameLine(staff1._year, param(), keta));
                if (line.size() == 2) {
                    vrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    vrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    vrsOutSelect(new String[][] {{"STAFFNAME_" + j + "_" + i}, {"STAFFNAME_" + j + "_" + i + "_1"}}, staff1.getNameString(param(), keta));
                }
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final List<String> line = new ArrayList<String>();
                line.addAll(staff2.getNameBetweenLine(param(), keta));
                line.addAll(staff1.getNameBetweenLine(param(), keta));
                if (line.size() == 2) {
                    vrsOut("STAFFNAME_" + j + "_" + i + "_3", line.get(0));
                    vrsOut("STAFFNAME_" + j + "_" + i + "_4", line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        vrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), line.get(k));
                    }
                }
            }
        }

        private void printStaffName2(final String j, final int i, final List<List<Staff>> staffListList) {
            final int keta = 26;
            final List<String> lines = new LinkedList<String>();
            final List<Staff> staffs = new ArrayList<Staff>();
            for (final List<Staff> staff1List : staffListList) {
                if (staff1List.size() == 1) {
                    lines.add(staff1List.get(0).getNameString(param(), keta));
                } else if (staff1List.size() > 1) {
                    for (final Staff staff : staff1List) {
                        lines.addAll(staff._staffMst.getNameLine(staff._year, param(), keta));
                    }
                }
                staffs.addAll(staff1List);
            }
            if (param()._isOutputDebug) {
                if  (lines.size() > 1) {
                    log.info(" staffname " + j + " [" + i + "] lines = " + lines);
                }
            }
            if (lines.size() == 1) {
                // 1人表示。
                vrsOutSelect(new String[][] {{"STAFFNAME_" + j + "_" + i}, {"STAFFNAME_" + j + "_" + i + "_1"}}, lines.get(0));

            } else if (lines.size() == 2) {
                // 2人表示
                if (param()._z010.in(Z010.osakashinnai) && staffs.size() == 2) {
                    // 1行空
                    vrsOut("STAFFNAME_" + j + "_" + i + "_2", lines.get(0));
                    vrsOut("STAFFNAME_" + j + "_" + i + "_4", lines.get(1));
                } else {
                    // 最小2行（中央）
                    vrsOut("STAFFNAME_" + j + "_" + i + "_3", lines.get(0));
                    vrsOut("STAFFNAME_" + j + "_" + i + "_4", lines.get(1));
                }
            } else if (lines.size() >= 3) {
                // 上から順に表示。
                for (int k = 0; k < 4 && k < lines.size(); k++) {
                    vrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), lines.get(k));
                }
            }
        }

        private void printAftetGraduatedCourse(final Student student) {
            int keta = 50;
            try {
                final SvfField field = getSvfField("AFTER_GRADUATION1");
                keta = field._fieldLength;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            final List<String> tokenList = new ArrayList<String>();
            for (int i = 0; i < student._afterGraduatedCourseTextList.size(); i++) {
                final String txt = student._afterGraduatedCourseTextList.get(i);
                final String[] t = KNJ_EditEdit.get_token(txt, keta, 8);
                if (null != t) {
                    for (int j = 0; j < t.length; j++) {
                        if (null != t[j]) {
                            tokenList.add(t[j]);
                        }
                    }
                }
            }
            for (int k = 0; k < tokenList.size(); k++) {
                vrsOut("AFTER_GRADUATION" + String.valueOf(k + 1), tokenList.get(k));
            }
            if (param()._z010.in(Z010.kaijyo)) {
                for (int i = 0; i < 8; i++) {
                    vrsOut("REMARK" + String.valueOf(i), null); // 備考
                }
            }
        }

        private void printRemark(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            if (!param()._z010.in(Z010.chiyodaKudan)) {
                return;
            }

            Cond issuedateInRegdStart = personalInfo.getHistCond(param());

            final Set<String> dateSet = new TreeSet<String>();

            final List<String> baseHistNameChangeList = KnjDbUtils.getColumnDataList(filter(student._schregBaseHistDatList, (Cond.field("NAME_FLG").eq(Cond.val("1")).or(Cond.field("REAL_NAME_FLG").eq(Cond.val("1")))).and(issuedateInRegdStart)), "ISSUEDATE");
            final List<String> guardHistNameChangeList = KnjDbUtils.getColumnDataList(filter(student._guardianHistDatList, (Cond.field("GUARD_NAME_FLG").eq(Cond.val("1")).or(Cond.field("GUARD_REAL_NAME_FLG").eq(Cond.val("1")))).and(issuedateInRegdStart)), "ISSUEDATE");
            List<String> addressRowList = KnjDbUtils.getColumnDataList(reverse(personalInfo._addressRowList), "ISSUEDATE");
            if (!addressRowList.isEmpty()) {
                addressRowList = addressRowList.subList(1, addressRowList.size());
            }
            List<String> guadianAddressRowList = KnjDbUtils.getColumnDataList(reverse(personalInfo._guardianAddressRowList), "ISSUEDATE");
            if (!guadianAddressRowList.isEmpty()) {
                guadianAddressRowList = guadianAddressRowList.subList(1, guadianAddressRowList.size());
            }

            dateSet.addAll(baseHistNameChangeList);
            dateSet.addAll(guardHistNameChangeList);
            dateSet.addAll(addressRowList);
            dateSet.addAll(guadianAddressRowList);
            final StringBuffer stb = new StringBuffer();
            for (final String date : dateSet) {
                final List<String> items = new ArrayList<String>();
                if (baseHistNameChangeList.contains(date)) {
                    items.add("氏名変更");
                }
                if (guardHistNameChangeList.contains(date)) {
                    items.add("保護者氏名変更");
                }
                if (addressRowList.contains(date)) {
                    items.add("転居");
                } else if (guadianAddressRowList.contains(date)) {
                    items.add("保護者転居");
                }
                if (items.isEmpty()) {
                    continue;
                }
                if (stb.length() > 0) {
                    stb.append("\n");
                }
                stb.append(KNJ_EditDate.getAutoFormatDate(db2, date) + " " + mkString(items, "、"));
            }
            final List<String> tokenList = getTokenList(stb.toString(), getFieldLength("REMARK", 46), param());
            for (int i = 0; i < tokenList.size(); i++) {
                vrsOutn("REMARK", i + 1, tokenList.get(i));
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
    private static class KNJA133J_3 extends KNJA133J_0 {

        private FormInfo _formInfo;

        KNJA133J_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public void printSvf(final DB2UDB db2, final Student student, final FormInfo formInfo, final KaiteiInfo kaiteiInfo, final List<String> pageYears) {
            _formInfo = formInfo;

            String form = _formInfo.getPrintForm(kaiteiInfo);
            if (null == form) {
                log.info(" no form : " + form);
                return;
            }

            final List<PersonalInfo> personalInfoList = student.getPrintSchregEntGrdHistList(param());
            for (final PersonalInfo personalInfo : personalInfoList) {

                final String form1 = configForm(form, kaiteiInfo, student, personalInfo);
                setForm(form1, 4);
                printRegd3(student, personalInfo, pageYears);  //年次・ホームルーム・整理番号
                if (param().isTokubetsuShien()) {
                    if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                        printShoken3(student, personalInfo, 2, pageYears); // 所見
                    } else { // CHITEKI1_知的障害.equals(param._chiteki) {
                    }
                } else {
                    printShoken3(student, personalInfo, 0, pageYears); // 所見
                }
                printKantenHyoka(student, personalInfo, kaiteiInfo, student.getClassViewList(pageYears), pageYears);
            }
        }

        private static final String SPECIALACTVIEW = "SPECIALACTVIEW";
        private static final String KUDAN_DOUTOKU_SHASEN_FORM = "KUDAN_DOUTOKU_SHASEN_FORM";
        private static final String KWANSEI_TOKUBETUKATSUDOU_KOUMOKU_LINE = "KWANSEI_TOKUBETUKATSUDOU_KOUMOKU_LINE";
        private static final String REMOVE_NENJI = "REMOVE_NENJI";
        private static final String MOVE_LINE_SOGAKU = "MOVE_LINE_SOGAKU";
        @Override
        protected Map<String, String> getConfigFormMap(final SvfForm svfForm, final KaiteiInfo kaiteiInfo, final Student student, final PersonalInfo personalInfo) {
            final Map<String, String> map = new TreeMap<String, String>();
            if (param()._z010.in(Z010.chiyodaKudan) && kaiteiInfo._kaitei.hasDoutoku()) {

                final List<String> shasenGradeCdList = new ArrayList<String>();
                if (!kaiteiInfo._kaitei.hasDoutoku()) {
                    if (param()._isOutputDebug) {
                        log.info(" shasen year = " + param()._year + ", loginRegd gradeCd = " + student._loginRegd._gradeCd + ", kaitei = " + kaiteiInfo);
                    }
                } else {
                    // 道徳無しフォーム使用
                    for (int g = 1; g <= 3; g++) {
                        final int rowYear = Integer.parseInt(param()._year) - Integer.parseInt(student._loginRegd._gradeCd) + g;
                        if (rowYear < kaiteiInfo._startYear) {
                            shasenGradeCdList.add(String.valueOf(g));
                        }
                    }
                }

                map.put(KUDAN_DOUTOKU_SHASEN_FORM, mkString(shasenGradeCdList, "|").toString());
            }
            if (param()._z010.in(Z010.kwansei)) {
                map.put(KWANSEI_TOKUBETUKATSUDOU_KOUMOKU_LINE, "1");
            }
            if (param()._z010.in(Z010.kwansei) || param().isTokubetsuShien() && CHITEKI2_知的障害以外.equals(param()._chiteki) && "1".equals(param().property(Property.useSpecial_Support_School))) {
                map.put(MOVE_LINE_SOGAKU, "1");
            }
            if (!svfForm.getKoteiMojiListWithText("年　　次").isEmpty()) {
                map.put(REMOVE_NENJI, "1");
            }
            return map;
        }
        @Override
        protected void modifyForm(final SvfForm svfForm, final PersonalInfo personalInfo, final Map<String, String> modifyFormFlgMap) {
            if (modifyFormFlgMap.containsKey(KUDAN_DOUTOKU_SHASEN_FORM)) {
                // 九段中等は道徳が該当しない年度の欄に斜線を表示する
                final String shasenGradeCdListStr = modifyFormFlgMap.get(KUDAN_DOUTOKU_SHASEN_FORM);

                for (final String shasenGradeCd : shasenGradeCdListStr.split("|")) {
                    final SvfForm.Field field = svfForm.getField("MORAL" + String.valueOf(shasenGradeCd));
                    final SvfForm.Line upperLine = svfForm.getNearestUpperLine(field._position);
                    final SvfForm.Line leftLine = svfForm.getNearestLeftLine(field._position);
                    final SvfForm.Line rightLine = svfForm.getNearestRightLine(field._position);
                    final SvfForm.Line lowerLine = svfForm.getNearestLowerLine(field._position);
                    final SvfForm.Line shasenLine = new SvfForm.Line(new SvfForm.Point(leftLine._start._x, lowerLine._start._y), new SvfForm.Point(rightLine._end._x, upperLine._end._y));
                    svfForm.addLine(shasenLine);
                }
            }
            if (modifyFormFlgMap.containsKey(KWANSEI_TOKUBETUKATSUDOU_KOUMOKU_LINE)) {
                final SvfForm.Field fieldSPECIALACTVIEW = svfForm.getField(SPECIALACTVIEW);
                if (null != fieldSPECIALACTVIEW) {
                    svfForm.addField(fieldSPECIALACTVIEW.copyTo("SPECIALACTVIEW1"));
                    final SvfForm.Line rightLine = svfForm.getNearestRightLine(fieldSPECIALACTVIEW._position);
                    for (final String moji : Arrays.asList("生徒会活動", "学校行事")) {
                        final List<SvfForm.KoteiMoji> koteiMojis = svfForm.getKoteiMojiListWithText(moji);
                        if (koteiMojis.size() == 0) {
                            log.warn(" no such koteiMoji : " + moji);
                            continue;
                        }
                        final SvfForm.KoteiMoji koteiMoji = koteiMojis.get(0);
                        if (null != koteiMoji) {
                            final SvfForm.Line line = svfForm.getNearestUpperLine(koteiMoji._point);
                            svfForm.move(line, line.setEnd(line._end.setX(rightLine._end._x)));
                        }
                        if ("生徒会活動".equals(moji)) {
                            svfForm.addField(fieldSPECIALACTVIEW.copyTo("SPECIALACTVIEW2").setY(koteiMoji._point._y - 10));
                        } else if ("学校行事".equals(moji)) {
                            svfForm.addField(fieldSPECIALACTVIEW.copyTo("SPECIALACTVIEW3").setY(koteiMoji._point._y - 10));
                        }
                    }
                }
            }
            if (modifyFormFlgMap.containsKey(REMOVE_NENJI)) {
                for (final SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("年　　次")) {
                    svfForm.move(koteiMoji, koteiMoji.replaceMojiWith("学　　年"));
                }
            }
            if (modifyFormFlgMap.containsKey(MOVE_LINE_SOGAKU)) {

                final ShokenSize actSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J), 7, 10);
                final ShokenSize viewremarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_VIEWREMARK_SIZE_J), 7, 10);
                final ShokenSize totalstudyValSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J), 13, 10);

                final SvfForm.Field totalAct1 = svfForm.getField("TOTAL_ACT1");
                final SvfForm.Field totalView1 = svfForm.getField("TOTAL_VIEW1");
                final SvfForm.Field totalValue1 = svfForm.getField("TOTAL_VALUE1");

                final boolean isMatch = (totalAct1._fieldLength >= actSize._mojisu * 2 && totalView1._fieldLength >= viewremarkSize._mojisu  * 2 && totalValue1._fieldLength >= totalstudyValSize._mojisu * 2);
                if (param()._isOutputDebug) {
                    log.info(" " + svfForm._formFile.getName() + " match? " + isMatch + " sogaku keta : (" + totalAct1._fieldLength + ", " + totalView1._fieldLength + ", " + totalValue1._fieldLength + ") <> (" + (actSize._mojisu * 2) + ", " + (viewremarkSize._mojisu * 2) + ", " + (totalstudyValSize._mojisu * 2) + ") ");
                }
                if (!isMatch) {

                    final SvfForm.Line frameLeftLine = svfForm.getNearestLeftLine(totalAct1.getPoint());
                    final SvfForm.Line frameRightLine = svfForm.getNearestRightLine(totalValue1.getPoint());
                    final int splitCount = (actSize._mojisu + 1) + (viewremarkSize._mojisu + 1) + (totalstudyValSize._mojisu + 1);
                    final SvfForm.Line line2 = svfForm.getNearestLeftLine(totalView1.getPoint());
                    final SvfForm.Line line3 = svfForm.getNearestLeftLine(totalValue1.getPoint());
                    final int width = frameRightLine._start._x - frameLeftLine._start._x;
                    final int newLine2x = frameLeftLine._start._x + (int) (1.0 * width * ((actSize._mojisu + 1)) / splitCount);
                    final int newLine3x = frameLeftLine._start._x + (int) (1.0 * width * ((actSize._mojisu + 1) + (viewremarkSize._mojisu + 1)) / splitCount);
                    svfForm.move(line2, line2.setX(newLine2x));
                    svfForm.move(line3, line3.setX(newLine3x));
                    for (int g = 1; g <= 3; g++) {
                        final SvfForm.Field act = svfForm.getField("TOTAL_ACT" + String.valueOf(g));
                        final SvfForm.Field view = svfForm.getField("TOTAL_VIEW" + String.valueOf(g));
                        final SvfForm.Field value = svfForm.getField("TOTAL_VALUE" + String.valueOf(g));
                        for (final Tuple<SvfForm.Field, SvfForm.Field> oldnew : Arrays.asList(
                                Tuple.of(act, act.setFieldLength(actSize._mojisu * 2).setEndX(act._position._x + (act._endX - act._position._x)  * (actSize._mojisu * 2)/ act._fieldLength)),
                                Tuple.of(view, view.setFieldLength(viewremarkSize._mojisu * 2).setX(newLine2x + 15).setEndX(view._position._x + (view._endX - view._position._x)  * (viewremarkSize._mojisu * 2) / view._fieldLength)),
                                Tuple.of(value, value.setFieldLength(totalstudyValSize._mojisu * 2).setX(newLine3x + 15).setEndX(value._position._x + (value._endX - value._position._x)  * (totalstudyValSize._mojisu * 2) / value._fieldLength))
                                )) {
                            final SvfForm.Field old = oldnew._first;
                            final SvfForm.Field new_ = oldnew._second;
                            //log.info(" " + new_._fieldname + " len = " + old._fieldLength + " -> " + new_._fieldLength + ", pos = " + old._position + " -> " + new_._position + ", endx = " + old._endX + " -> " + new_._endX);
                            svfForm.removeField(old);
                            svfForm.addField(new_);
                        }
                    }

                    final SvfForm.Line upperLine = svfForm.getNearestUpperLine(totalAct1.getPoint());
                    final SvfForm.Line upperLine2 = svfForm.getNearestUpperLine(upperLine.getPoint().addY(-20));
                    for (final SvfForm.KoteiMoji koteiMoji : svfForm.getElementList(SvfForm.KoteiMoji.class )) {
                        if (Util.between(koteiMoji.getPoint()._x, upperLine._start._x, upperLine._end._x) && Util.between(koteiMoji.getPoint()._y, upperLine2._start._y, upperLine._start._y)) {
                            if (koteiMoji._moji.matches(Util.regexSpaced("\"学習活動\""))) {
                                final int dx = (newLine2x - line2.getPoint()._x) / 2;
                                svfForm.move(koteiMoji, koteiMoji.addX(dx));
                            } else if (koteiMoji._moji.matches(Util.regexSpaced("\"観点\""))) {
                                final int dx = (newLine2x - line2.getPoint()._x) / 2 + (newLine3x - line3.getPoint()._x) / 2;
                                svfForm.move(koteiMoji, koteiMoji.addX(dx));
                            } else if (koteiMoji._moji.matches(Util.regexSpaced("\"評価\""))) {
                                final int dx = (newLine3x + 15 - line3.getPoint()._x) / 2;
                                svfForm.move(koteiMoji, koteiMoji.addX(dx));
                            }
                        }
                    }
                }
            }
        }

        private void printShoken3(final Student student, final PersonalInfo personalInfo, final int flg, final List<String> pageYears) {
            // 道徳
            for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                if (!student.isPrintYear(remark._year, param())) {
                    continue;
                }
                if (!pageYearsContains(pageYears, remark._year)) {
                    continue;
                }
                if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), remark._year)) {
                    // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                printSvfRenban("MORAL" + remark._g, remark._detail2DatSeq004Remark1, ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J), 32, 2));
            }


            final boolean isChitekiIgai = flg == 2;
            //総合的な学習の時間の記録・総合所見
            for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                if (!student.isPrintYear(remark._year, param())) {
                    continue;
                }
                if (!pageYearsContains(pageYears, remark._year)) {
                    continue;
                }

                if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), remark._year)) {
                    // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                // 知的以外
                if (isChitekiIgai) {
                    final ShokenSize actSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J), 7, 10);
                    final ShokenSize viewremarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_VIEWREMARK_SIZE_J), 7, 10);
                    final ShokenSize totalstudyValSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J), 13, 10);
                    printSvfRenban("TOTAL_ACT" + remark._g, remark._totalstudyact, actSize); // 総合的な学習の時間の記録 学習活動
                    printSvfRenban("TOTAL_VIEW" + remark._g, remark._viewremark, viewremarkSize); // 総合的な学習の時間の記録 観点
                    printSvfRenban("TOTAL_VALUE" + remark._g, remark._totalstudyval, totalstudyValSize); // 総合的な学習の時間の記録 評価

                } else {
                    if (_formInfo._isKaijyoOldForm) {
                        printSvfRenban("TOTAL_ACT" + remark._g, remark._totalstudyact, new ShokenSize(20, 4));
                        printSvfRenban("TOTAL_VALUE" + remark._g, remark._totalstudyval, new ShokenSize(35, 4));
                    } else {
                        final ShokenSize actSize;
                        final ShokenSize viewremarkSize;
                        final ShokenSize totalstudyValSize;
//                        if (param()._isSundaikoufu) {
//                        	// TODO:
//                            actSize = new ShokenSize(7, 9);
//                            viewremarkSize = new ShokenSize(8, 9);
//                            totalstudyValSize = new ShokenSize(15, 9);
//                        } else
                        if (param()._z010.in(Z010.tosa)) {
                            actSize = new ShokenSize(7, 10);
                            viewremarkSize = new ShokenSize(7, 10);
                            totalstudyValSize = new ShokenSize(13, 10);
                        } else {
                            actSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J), 5, 8);
                            viewremarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_VIEWREMARK_SIZE_J), 10, 8);
                            totalstudyValSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J), 15, 8);
                        }
                        printSvfRenban("TOTAL_ACT" + remark._g, remark._totalstudyact, actSize);
                        printSvfRenban("TOTAL_VIEW" + remark._g, remark._viewremark, viewremarkSize);
                        printSvfRenban("TOTAL_VALUE" + remark._g, remark._totalstudyval, totalstudyValSize);
                    }
                }
            }

            //特別活動の記録
            if (param()._z010.in(Z010.kwansei)) {
                final List<String> specialActView1 = new ArrayList<String>();
                final List<String> specialActView2 = new ArrayList<String>();
                final List<String> specialActView3 = new ArrayList<String>();
                for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                    if (!student.isPrintYear(remark._year, param())) {
                        continue;
                    }
                    if (!pageYearsContains(pageYears, remark._year)) {
                        continue;
                    }
                    if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), remark._year)) {
                        // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }
                    specialActView1.add(remark._classact);
                    specialActView2.add(remark._studentact);
                    specialActView3.add(remark._schoolevent);
                }
                printSvfRenban("SPECIALACTVIEW1", mkString(specialActView1, "\n").toString(), new ShokenSize(17, 3));
                printSvfRenban("SPECIALACTVIEW2", mkString(specialActView2, "\n").toString(), new ShokenSize(17, 3));
                printSvfRenban("SPECIALACTVIEW3", mkString(specialActView3, "\n").toString(), new ShokenSize(17, 3));

            } else {
                final StringBuffer specialActRemark = new StringBuffer();
                String nl = "";
                final ShokenSize spsize;
                for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                    if (!student.isPrintYear(remark._year, param())) {
                        continue;
                    }
                    if (!pageYearsContains(pageYears, remark._year)) {
                        continue;
                    }
                    if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), remark._year)) {
                        // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }
                    if (null != remark._specialActRemark) {
                        if (param()._z010.in(Z010.KINJUNIOR) || param()._z010.in(Z010.meiji) || param()._z010.in(Z010.CHIBEN) || param()._z010.in(Z010.kyoai)) {
                            specialActRemark.delete(0, specialActRemark.length()).append(remark._specialActRemark);
                        } else {
                            specialActRemark.append(nl).append(remark._specialActRemark);
                            nl = "\n";
                        }
                    }
                }

                if (isChitekiIgai) {
                    spsize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J), 14, 7);
                } else {
                    spsize = new ShokenSize(17, 10);
                }
                printSvfRenban(SPECIALACTVIEW, specialActRemark.toString(), spsize);
            }


            // 行動の記録
            for (final ActRecord act : student._actList) {
                if (!student.isPrintYear(act._year, param())) {
                    continue;
                }
                if (!pageYearsContains(pageYears, act._year)) {
                    continue;
                }
                if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), act._year)) {
                    // log.debug(" skip print year = " + actRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                printAct(act);
            }
        }

        public void printAct(final ActRecord actRecord) {
            if ("1".equals(actRecord._record)) {
                if ("2".equals(actRecord._div)){
                    svfVrsOutnForKinsoku("SPECIALACT" + actRecord._code, actRecord._g, "○"); //特別行動の記録
                }
            }
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目コードの順番を返す。そうでなければ(通常の処理）教科コードを返す。
         * @param student
         * @param classview
         * @return 科目名または教科名
         */
        private String getKeyCd(final ClassView classview, final ViewSubclass viewSubclass, final List<String> pageYears) {
            final TreeSet<String> set = classview.getViewSubclassCdSet(pageYears);
            if (set.size() == 1) {
                return classview._classcd;
            } else if (set.size() > 1) {
                final DecimalFormat df = new DecimalFormat("00");
                int order = 0;
                int n = 0;
                for (final String subclassCd : set) {
                    if (viewSubclass._subclasscd.equals(subclassCd)) {
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
        private String getViewShowname(final ClassView classview, final ViewSubclass viewSubclass, final List<String> pageYears) {
            final TreeSet<String> set = classview.getViewSubclassCdSet(pageYears);
            if (set.size() > 1 || "1".equals(classview._electdiv)) {
                return viewSubclass._subclassname;
            } else if (set.size() == 1) {
                return classview._classname;
            }
            return null;
        }

        /**
         * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
         * @param student
         * @param valueRecord
         * @return 科目名または教科名
         */
        private String getHyoteiShowname(final Student student, final ValueRecord valueRecord, final List<String> pageYears) {
            final TreeSet<String> set = getHyoteiSubclassCdSet(student, valueRecord, pageYears);
            if (set.size() > 1 || "1".equals(valueRecord._classMstElectDiv)) {
                return valueRecord._subclassName;
            } else if (set.size() == 1) {
                return valueRecord._className;
            }
            return null;
        }

        /**
         * 同一教科の科目コードを得る
         * @param student
         * @param classview
         * @return
         */
        private TreeSet<String> getHyoteiSubclassCdSet(final Student student, final ValueRecord valueRecord, final List<String> pageYears) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final ValueRecord valueRecord0 : student.getValueRecordList(pageYears)) {
                if (valueRecord0._classcd.equals(valueRecord._classcd)) {
                    set.add(valueRecord0._subclasscd);
                }
            }
            return set;
        }

        /**
         *  観点出力処理
         *
         */
        private void printKantenHyoka(final Student student, final PersonalInfo personalInfo, final KaiteiInfo kaiteiInfo, final List<ClassView> classViewList, final List<String> pageYears) {

            int lineMax = _formInfo.VIEW_LINE1_MAX;
            if (kaiteiInfo._kaitei == KAITEI.DEFAULT) {
                // 観点ヘッダ1行(左、右) + 評定ヘッダ1行 + 評定欄文8行
                //lineMax = lineMax - 7 + 1 * 2 + 1 + 8;
                lineMax = _formInfo.getViewLine1Max(_svf, _currentForm);
                log.info(" lineMax = " + lineMax);
            } else if (kaiteiInfo._kaitei == KAITEI.ADD_DOUTOKU) {
                // 道徳欄文7行
                lineMax = lineMax - 7;
            }
            final Map<String, List<ValueRecord>> subclassKeyValueRecordMap = new HashMap<String, List<ValueRecord>>();
            if (kaiteiInfo._kaitei == KAITEI.DEFAULT) {
                for (final ValueRecord vr : student.getValueRecordList(pageYears)) {
                    getMappedList(subclassKeyValueRecordMap, vr._classcd + "|" + vr._subclasscd).add(vr);
                }
            } else {
                printHyotei(student, personalInfo, pageYears);  //評定
            }

            int line1 = 0;  //欄の出力行数

            for (final ClassView classview : classViewList) {
                if (param()._isOutputDebugKanten) {
                    log.info(" classview = " + classview);
                }

                for (final ViewSubclass viewSubclass : classview._viewSubclassList) {
                    // log.debug("     = " + viewSubclass);
                    final String keyCd = getKeyCd(classview, viewSubclass, pageYears);
                    final String subclasscd = "1".equals(param()._useCurriculumcd) ? classview._classcd + "-" + viewSubclass._curriculumcd + "-" + viewSubclass._subclasscd : viewSubclass._subclasscd;
                    if (param()._isOutputDebugKanten) {
                        log.info(" subclass = " + subclasscd);
                    }
                    final String showname = getViewShowname(classview, viewSubclass, pageYears);
                    final String name = classview.setViewClassname(showname, viewSubclass._subclasscd, param(), pageYears);  // 教科名のセット
                    int i = 0;  //教科名称カウント用変数を初期化
                    final boolean useClass2 =  !_formInfo._isKaijyoOldForm && 2 <= (StringUtils.defaultString(showname).length()) / classview.getViewNum(viewSubclass._subclasscd, pageYears);
                    final String field = useClass2 ? "CLASS2" : "CLASS1";
                    final int inc = useClass2 ? 2 : 1;

                    for (int vi = 0; vi < viewSubclass._viewList.size(); vi++) {
                        final View view = viewSubclass._viewList.get(vi);
                        // log.debug("         = " + view);
                        if (_formInfo._isKaijyoOldForm) {
                            if (vi == 0) {
                                vrsOut(field, showname);  //教科名称
                                i = 999;
                            }
                        } else {
                            if (i < name.length()) {
                                vrsOut(field, name.substring(i, Math.min(i + inc, name.length())));  //教科名称
                                i += inc;
                            }
                        }
                        vrsOut("CLASSCD1", keyCd);  //教科コード
                        vrsOut("VIEW1", view._viewname);  //観点名称

                        for (int gi = 0; gi < 3; gi++) {
                            final String g = String.valueOf(gi + 1);
                            if (param().isSlashView(subclasscd, g, vi + 1)) {
                                vrsOut("VIEW_SHASH" + g, "／");  //観点スラッシュ
                            }
                        }
                        //log.info(" view " + viewSubclass._subclasscd + " : " + view._yearViewMap);
                        for (final ViewStatus vs : view._yearViewMap.values()) {
                            if (!student.isPrintYear(vs._year, param())) {
                                continue;
                            }
                            if (!pageYearsContains(pageYears, vs._year)) {
                                continue;
                            }
                            if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), vs._year)) {
                                continue;
                            }
                            // 観点
                            vrsOut("ASSESS1_" + vs._g, vs._status);
                        }
                        line1++;
                        updateLine1(student);
                        _svf.VrEndRecord();
                        hasdata = true;
                    }
                    if (kaiteiInfo._kaitei == KAITEI.DEFAULT) {
                        if (i < name.length()) {
                            vrsOut(field, name.substring(i, Math.min(i + inc, name.length())));  //教科名称
                            i += inc;
                        }
                        vrsOut("CLASSCD1", keyCd);  //教科コード
                        vrsOut("VIEW1", "評定");  //観点名称
                        for (final ValueRecord vr : getMappedList(subclassKeyValueRecordMap, viewSubclass._classcd + "|" + viewSubclass._subclasscd)) {
                            if (!student.isPrintYear(vr._year, param())) {
                                continue;
                            }
                            if (!pageYearsContains(pageYears, vr._year)) {
                                continue;
                            }
                            if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), vr._year)) {
                                continue;
                            }
                            //評定
                            vrsOut("ASSESS1_" + vr._g, vr.getPrintHyotei(param()));
                        }
                        line1++;
                        updateLine1(student);
                        _svf.VrEndRecord();
                        hasdata = true;
                    }
                    if (null != name) {
                        if (i < name.length()) {
                            for (int j = i; j < name.length(); j += inc) {
                                line1++;
                                updateLine1(student);
                                vrsOut("CLASSCD1", keyCd);  //教科コード
                                vrsOut(field, name.substring(j));  // 教科名称
                                _svf.VrEndRecord();
                                hasdata = true;
                            }
                        } else {
                            if (kaiteiInfo._kaitei != KAITEI.DEFAULT) {
                                if (!"1".equals(param().property(Property.seitoSidoYorokuCyugakuKantenNoBlank))) {
                                    final int isPrint = ((line1 % lineMax != 0) ? 1 : -1);
                                    if (-1 != isPrint) { // 行数がオーバーしない場合、レコードを印刷
                                        line1++;
                                        updateLine1(student);
                                        vrsOut("CLASSCD1", keyCd);  // 教科コード
                                        _svf.VrEndRecord();
                                        hasdata = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int i = line1 != 0 && line1 % lineMax == 0 ? lineMax : line1 % lineMax; i < lineMax; i++) {
                vrsOut("CLASSCD1", "A");  //教科コード
                line1++;
                updateLine1(student);
                _svf.VrEndRecord();
                hasdata = true;
            }
        }

        private void updateLine1(final Student student) {
            printName(student._personalInfo);
        }
        /**
         *  評定出力処理
         */
        private void printHyotei(final Student student, final PersonalInfo personalInfo, final List<String> pageYears) {
            final List<List<ValueRecord>> classListList = new ArrayList<List<ValueRecord>>();
            List<ValueRecord> current = null;
            String oldClasscd = null;  //教科コードの保管用
            String oldSubclasscd = null;  //教科コードの保管用
            for (final ValueRecord valueRecord : student.getValueRecordList(pageYears)) {
                //log.info(" value record = " + valueRecord._classCd + " / " + valueRecord._subclassCd + " / " + valueRecord._subclassName);
                //教科コードの変わり目
                if (oldClasscd == null || oldSubclasscd == null || !oldClasscd.equals(valueRecord._classcd) || !oldSubclasscd.equals(valueRecord._subclasscd)) {
                    current = new ArrayList();
                    classListList.add(current);
                }
                current.add(valueRecord);
                oldClasscd = valueRecord._classcd;  //教科コードの保管
                oldSubclasscd = valueRecord._subclasscd;
            }

            for (int cli = 0; cli < classListList.size(); cli++) {
                final List<ValueRecord> sameClassValueRecordList = classListList.get(cli);
                for (int j = 0; j < sameClassValueRecordList.size(); j++) {
                    final ValueRecord vr = sameClassValueRecordList.get(j);
                    final String line = String.valueOf(cli + 1);
                    if (j == 0) {
                        final String showname = getHyoteiShowname(student, vr, pageYears);
                        if (null != showname) { //教科名出力
                            if (_formInfo._isKaijyoOldForm) {
                                vrsOutn("CLASS3_1", cli + 1, showname);  //評定教科名
                            } else {
                                vrsOutSelect(new String[][] {{"CLASS" + line + "_1"}, {"CLASS" + line + "_2"}, {"CLASS" + line + "_3"}}, showname);  //評定教科名
                            }
                        }
                    }
                    for (int gi = 0; gi < 3; gi++) {
                        final String g = String.valueOf(gi + 1);
                        if (param().isSlashValue(vr.subclassKey(param()), g)) {
                            svfVrsOutnForKinsoku("ASSESS_SHASH" + line, gi + 1, "／");  //観点スラッシュ
                        }
                    }
                    if (!student.isPrintYear(vr._year, param())) {
                        //log.info(" isPrintYear = " + valueRecord._year);
                        continue;
                    }
                    if (!pageYearsContains(pageYears, vr._year)) {
                        continue;
                    }
                    if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), vr._year)) {
                        //log.info(" skip print year = " + valueRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = ");
                        continue;
                    }
                    //log.info(" value record value = " + valueRecord._value);
                    final String hyotei = vr.getPrintHyotei(param());
                    if (hyotei != null) {
                        if (_formInfo._isKaijyoOldForm) {
                            vrsOutn("ASSESS2_" + vr._g, (cli + 1), hyotei);  //評定
                        } else {
                            //log.info(" value record line = ASSESS" + line + ", g = " + valueRecord._g + " / " + valueRecord._value);
                            svfVrsOutnForKinsoku("ASSESS" + line, vr._g, hyotei);  //評定
                        }
                    }
                }
            }
        }

        private void printName(final PersonalInfo personalInfo) {
            int posx1 = 416;
            int posx2 = 950;
            int height = 50;
            final int minnum = 20;
            final int maxnum = 48;
            int ystart = 514, ystart1 = 484, ystart2 = 544;
            if (_formInfo._isKaijyoOldForm) {
                posx1 = 510;
                posx2 = 1080;
                height = 50;
                ystart = 313;
                ystart1 = 291;
                ystart2 = 335;
            } else if (param()._z010.in(Z010.tosa)) {
                final SvfField fieldName1 = getSvfField("NAME1");
                final SvfField fieldName2 = getSvfField("NAME2");
                final SvfField fieldName3 = getSvfField("NAME3");
                posx1 = fieldName1.x();
                ystart = fieldName1.y();
                ystart1 = fieldName2.y();
                ystart2 = fieldName3.y();
                posx2 = posx1 + (int) SvfFieldData.fieldWidth(fieldName1, 0);
                height = (int) SvfFieldData.charHeightPixel("", 12.0);
            }

            if (personalInfo._useRealName && personalInfo._nameOutputFlg && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                printName(personalInfo._studentRealName,           new SvfFieldData("NAME2", posx1, posx2, height, minnum, maxnum, ystart1));
                printName("（" + personalInfo._studentName + "）", new SvfFieldData("NAME3", posx1, posx2, height, minnum, maxnum, ystart2));
            } else {
                final String name = personalInfo._useRealName ? personalInfo._studentRealName : personalInfo._studentName;
                printName(name, new SvfFieldData("NAME1", posx1, posx2, height, minnum, maxnum, ystart));
            }
        }

        /**
         *  個人情報  学籍等履歴情報
         */
        private void printRegd3(final Student student, final PersonalInfo personalInfo, final List<String> pageYears) {
            //生徒名印刷
            printName(personalInfo);

            vrsOutSelect(new String[][] {{"SCHOOLNAME1"},  {"SCHOOLNAME2"}}, StringUtils.isEmpty(param()._certifSchoolName) ? param()._schoolName1 : param()._certifSchoolName);

            //学籍履歴の取得および印刷
            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                if (!student.isPrintYear(gakuseki._year, param())) {
                    continue;
                }
                if (!pageYearsContains(pageYears, gakuseki._year)) {
                    continue;
                }
                if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), gakuseki._year)) {
                    // log.debug(" skip print year = " + regdRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                    continue;
                }
                if (param()._z010.in(Z010.bunkyo)) {
                    vrsOut("YEAR_" + String.valueOf(gakuseki._i) + "_2", gakuseki._nendo); // 組
                }
                svfVrsOutnForKinsoku(getHrNameField(gakuseki._hrname), gakuseki._i, gakuseki._hrname); // 組
                svfVrsOutnForKinsoku("ATTENDNO", gakuseki._i, String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号
            }
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理]  生徒指導要録
     *   総合所見及び指導上参考となる事項
     */
    private static class KNJA133J_4 extends KNJA133J_0 {

        private boolean _isKaijyoOldForm;

        KNJA133J_4(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public int printSvf(final DB2UDB db2, final Student student, final KaiteiInfo kaiteiInfo, final List<String> pageYears) {
            final String form0 = getForm(student, kaiteiInfo);
            int page = 0;
            for (final PersonalInfo personalInfo : student.getPrintSchregEntGrdHistList(param())) {
                final String form = configForm(form0, kaiteiInfo, student, personalInfo);
                setForm(form, 1);
                printRegd4(student, personalInfo, pageYears);  //学籍データ
                printShoken4(student, personalInfo, pageYears); // 所見
                _svf.VrEndPage();
                page += 1;
            }
            hasdata = true;
            return page;
        }

        final String FLG_SPLIT_SOGOSHOKEN = "FLG_SPLIT_SOGOSHOKEN";
        final String FLG_TEIHACHI_RISHUZUMI_REMARK = "FLG_TEIHACHI_RISHUZUMI_REMARK";
        @Override
        protected Map<String, String> getConfigFormMap(final SvfForm svfForm, final KaiteiInfo kaiteiInfo, final Student student, final PersonalInfo pi) {
            final Map<String, String> rtn = new HashMap<String, String>();
            if (param()._z010.in(Z010.kwansei)) {
                rtn.put(FLG_SPLIT_SOGOSHOKEN, "1");
            }
            if (param()._z010.in(Z010.teihachi)) {
                rtn.put(FLG_TEIHACHI_RISHUZUMI_REMARK, "1");
            }
            return rtn;
        }
        @Override
        protected void modifyForm(final SvfForm svfForm, final PersonalInfo personalInfo, final Map<String, String> modifyFormFlgMap) {
            if (modifyFormFlgMap.containsKey(FLG_SPLIT_SOGOSHOKEN)) {
                for (final String fieldname : Arrays.asList("TOTALREMARK1", "TOTALREMARK2", "TOTALREMARK3")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.addField(field.copyTo(fieldname + "_LEFT").setCharPoint10(82).setFieldLength(25 * 2).setEndX(field._position._x + (int) ((field._endX - field._position._x) * 1.0 * 25 / (25 + 30))));
                        svfForm.addField(field.copyTo(fieldname + "_RIGHT").setCharPoint10(82).setFieldLength(30 * 2).setX(1486).setEndX(field._endX));
                    }
                }
            }
            if (modifyFormFlgMap.containsKey(FLG_TEIHACHI_RISHUZUMI_REMARK)) {
                final int boxX1 = 156, boxY1 = 4005, boxX2 = 2912, boxY2 = 4320;
                svfForm.addBox(new SvfForm.Box(SvfForm.LineKind.SOLID, SvfForm.LineWidth.THIN, new SvfForm.Point(boxX1, boxY1), new SvfForm.Point(boxX2, boxY2)));
                final SvfForm.Field.RepeatConfig rcBiko = new SvfForm.Field.RepeatConfig("900", 5, 1, -1, 3).setRepeatPitchPoint(3.61);
                svfForm.addRepeat(new SvfForm.Repeat(rcBiko._repeatNo, boxX1, boxY1 + 10, boxX2, boxY2, 0, rcBiko._repeatCount, rcBiko._repeatPitch, 0, "1"));
                svfForm.addField(new SvfForm.Field(null, "RISHUZUMI", SvfForm.Font.Mincho, 59 * 2, boxX2 - 10, false, new SvfForm.Point(boxX1 + 10, boxY1 + 20), 82, "履修済み備考").setRepeatConfig(rcBiko));
            }
        }

        private String getForm(final Student student, final KaiteiInfo kaiteiInfo) {
            final String form;
            _isKaijyoOldForm = false;
            if (param().isTokubetsuShien()) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    form = "KNJA134J_14.frm";
                } else { // CHITEKI1_知的障害.equals(param._chiteki) {
                    form = "KNJA134J_4.frm";
                }
            } else if (!StringUtils.isBlank(param().property(Property.knja133jForm4))) { // 広島なぎさ KNJA133J_14NAGISA.frm
                form = param().property(Property.knja133jForm4);
            } else if (param()._z010.in(Z010.kaijyo) && !kaiteiInfo._kaitei.hasDoutoku()) {
                form = "KNJA133J_14KAIJO_OLD.frm";
                _isKaijyoOldForm = true;
            } else if (param()._z010.in(Z010.bunkyo)) {
                form = "KNJA133J_14BUNKYO.frm";
            } else if (param()._z010.in(Z010.tosa)) {
                form = "KNJA133J_14TOSA.frm";
            } else if ("1".equals(param()._train_ref_1_2_3_use_J)) {
                // 総合所見3分割
                if (ArrayUtils.isEquals(new Integer[] {new Integer(21), new Integer(14), new Integer(7)}, param()._train_ref_1_2_3_field_size_JMojisu)) { // 21-14-7なら専用のフォームを使用
                    form = "KNJA133J_14_3.frm";
                } else {
                    form = "KNJA133J_14_2.frm";
                }
            } else {
                if (param()._seitoSidoYorokuKinsokuFormJ) {
                    form = "KNJA133J_14C.frm";
                } else {
                    form = "KNJA133J_14.frm";
                }
            }
            return form;
        }

        private void printShoken4(final Student student, final PersonalInfo personalInfo, final List<String> pageYears) {
            if (param().isTokubetsuShien()) {
                // 入学時の障害の状態
                if (null != student._htrainremarkHdat._detail2HDatSeq001Remark1) {
                    final ShokenSize shogainoJoutaiSize;
                    if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                        // 行動の記録
                        for (final ActRecord act : student._actList) {

                            printAct(act);
                        }

                        shogainoJoutaiSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J), 9, 15);
                    } else { // CHITEKI1_知的障害.equals(param._chiteki) {
                        shogainoJoutaiSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability), 10, 12);
                    }

                    printSvfRenban("FIELD1", student._htrainremarkHdat._detail2HDatSeq001Remark1, shogainoJoutaiSize);
                }

                for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                    if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                        // 自立活動の記録
                        if (remark._detail2DatSeq001Remark1 != null) {
                            final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J), 48, 7);
                            printSvfRenban("INDEPENDENCE_REMARK" + remark._g, remark._detail2DatSeq001Remark1, size);
                        }

                    } else { // CHITEKI1_知的障害.equals(param._chiteki) {

                        // 総合的な学習の時間の記録 学習活動
                        if (remark._detail2DatSeq004Remark1 != null) {
                            final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_REMARK1_SIZE_J_disability), 75, 5);
                            printSvfRenban("MORAL" + remark._g, remark._detail2DatSeq004Remark1, size);
                        }

                        // 総合的な学習の時間の記録 学習活動
                        if (remark._totalstudyact != null) {
                            final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability), 16, 5);
                            printSvfRenban("TOTAL_ACT" + remark._g, remark._totalstudyact, size);
                        }

                        // 総合的な学習の時間の記録 観点
                        if (remark._viewremark != null) {
                            final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability), 14, 5);
                            printSvfRenban("TOTAL_VIEW" + remark._g, remark._viewremark, size);
                        }

                        // 総合的な学習の時間の記録 評価
                        if (remark._totalstudyval != null) {
                            final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability), 29, 5);
                            printSvfRenban("TOTAL_VALUE" + remark._g, remark._totalstudyval, size);
                        }

                        // 行動の記録
                        if (remark._detail2DatSeq002Remark1 != null) {
                            final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability), 75, 5);
                            printSvfRenban("INDEPENDENCE_REMARK" + remark._g, remark._detail2DatSeq002Remark1, size);
                        }
                    }

                    // 総合所見
                    if (remark._totalRemark != null) {
                        final ShokenSize totalRemarkSize;
                        if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                            totalRemarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALREMARK_SIZE_J), 60, 8);
                        } else {
                            totalRemarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability), 75, 9);
                        }

                        printSvfRenban("TOTALREMARK13_" + remark._g, remark._totalRemark, totalRemarkSize); //総合所見
                    }

                    // 出欠の記録備考
                    if (remark._attendrecRemark != null) {
                        final ShokenSize attendrecRemarkSize;
                        if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                            attendrecRemarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J), 38, 1);
                        } else {
                            attendrecRemarkSize = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability), param()._z010.in(Z010.bunkyo) ? 52 : 60, 1);
                        }
                        final List<String> list = getTokenList(remark._attendrecRemark, attendrecRemarkSize._mojisu * 2, attendrecRemarkSize._gyo, param());
                        for (int i = 0 ; i < list.size(); i++) {
                            final String n = i == 0 ? "" : String.valueOf(i + 1);
                            vrsOutn("REMARK" + n, remark._g, list.get(i));
                        }
                    }
                }

                //出欠の記録
                for (final Attend attend : student._attendList) {
                    printAttend(attend);
                }
            } else {
                // 行動の記録
                for (final ActRecord act : student._actList) {
                    if (!student.isPrintYear(act._year, param())) {
                        continue;
                    }
                    if (!pageYearsContains(pageYears, act._year)) {
                        continue;
                    }
                    if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), act._year)) {
                        // log.debug(" skip print year = " + actRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }
                    printAct(act);
                    if (_isKaijyoOldForm) {
                        printSpecialAct(act);
                    }
                }

                for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
                    if (!student.isPrintYear(remark._year, param())) {
                        continue;
                    }
                    if (!pageYearsContains(pageYears, remark._year)) {
                        continue;
                    }
                    if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), remark._year)) {
                        // log.debug(" skip print year = " + remarkRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }

                    //総合所見
                    if ("1".equals(param()._train_ref_1_2_3_use_J)) {
                        final int gyosu = 999; // !NumberUtils.isDigits(param()._train_ref_1_2_3_gyo_size_J) ? 0 : Integer.parseInt(param()._train_ref_1_2_3_gyo_size_J);

                        printSvfRenbanYoko("field8_1_", "_2", remark._g, getTokenList(remark._detailTrainRef1, param()._train_ref_1_2_3_field_size_JMojisu[0].intValue() * 2, gyosu, param()));
                        printSvfRenbanYoko("field8_2_", "_2", remark._g, getTokenList(remark._detailTrainRef2, param()._train_ref_1_2_3_field_size_JMojisu[1].intValue() * 2, gyosu, param()));
                        printSvfRenbanYoko("field8_3_", "_2", remark._g, getTokenList(remark._detailTrainRef3, param()._train_ref_1_2_3_field_size_JMojisu[2].intValue() * 2, gyosu, param()));

                    } else if (remark._totalRemark != null) {
                        final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALREMARK_SIZE_J), 44, 10);
                        List<String> lines = getTokenList(remark._totalRemark, size._mojisu * 2, param());

                        if (param()._z010.in(Z010.kwansei)) {
                            final ShokenSize sizeLeft = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_TOTALREMARK_SIZE_J), 25, 10);
                            final List<String> linesLeft = getTokenList(remark._detail2DatSeq009Remark1, sizeLeft._mojisu * 2, sizeLeft._gyo, param());
                            printSvfRenban("TOTALREMARK" + remark._g + "_LEFT", linesLeft);
                            printSvfRenban("TOTALREMARK" + remark._g + "_RIGHT", lines);
                        } else if (_isKaijyoOldForm || param()._z010.in(Z010.tosa) || param()._z010.in(Z010.nagisa) || param()._isKaichi || !param()._seitoSidoYorokuKinsokuFormJ && size._mojisu == 44 && size._gyo == 10) {
                            printSvfRenban("TOTALREMARK" + remark._g, lines);
                        } else if (size._gyo == 13) {
                            printSvfRenban("TOTALREMARK13_" + remark._g, remark._totalRemark, size);
                        } else if (size._gyo < 13 && 10 < lines.size() /* TOTALREMARK repeat size */ && lines.size() <= 13) {
                            printSvfRenban("TOTALREMARK13_" + remark._g, remark._totalRemark, ShokenSize.getShokenSize(null, size._mojisu, lines.size()));
                        } else {
                            deleteLastCrlfOnlyLines(lines);
                            if (param()._seitoSidoYorokuKinsokuFormJ) {
                                final String field;
                                if (lines.size() > 10) {
                                    field = "TOTALREMARK21_" + remark._g;
                                } else {
                                    field = "TOTALREMARK" + remark._g;
                                }
                                vrsOut(field, crlfReplaced(remark._totalRemark, param()));
                            } else {
                                printSvfRenbanUseFieldArePreferPointThanKeta("TOTALREMARK21_" + remark._g, remark._totalRemark, 6.9);
                            }
                        }
                    }

                    //出欠の記録備考
                    if (remark._attendrecRemark != null) {
                        final ShokenSize size = ShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J), 35, 2);

                        if (param()._seitoSidoYorokuKinsokuFormJ) {
                            vrsOut("REMARK" + String.valueOf(remark._g), crlfReplaced(remark._attendrecRemark, param()));
                        } else {
                            final List<String> list = getTokenList(remark._attendrecRemark, size._mojisu * 2, size._gyo, param());
                            for (int i = 0 ; i < list.size(); i++) {
                                final String n = i == 0 ? "" : String.valueOf(i + 1);
                                vrsOutn("REMARK" + n, remark._g, list.get(i));
                            }
                        }
                    }
                }

                //出欠の記録
                for (final Attend attend : student._attendList) {
                    if (!student.isPrintYear(attend._year, param())) {
                        continue;
                    }
                    if (!pageYearsContains(pageYears, attend._year)) {
                        continue;
                    }
                    if (student.yearIsNotInPersonalInfoRangeYear(personalInfo, param(), attend._year)) {
                        // log.debug(" skip print year = " + attendRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }
                    printAttend(attend);
                }

                if (param()._z010.in(Z010.teihachi)) {
                    printSvfRenban("RISHUZUMI", student._htrainRemarkDetail2Hdat002Remark1, ShokenSize.getShokenSize(null, 59, 5));
                }
            }
        }

        private void printSvfRenbanYoko(final String field, final String sfx, final int g, final List<String> lines) {
            for (int i = 0; i < lines.size(); i++) {
                vrsOutn(field + String.valueOf(i + 1) + sfx, g, lines.get(i));
            }
        }

        private void deleteLastCrlfOnlyLines(final List<String> lines) {
            for (final ListIterator<String> it = lines.listIterator(lines.size()); it.hasPrevious();) {
                final String line = it.previous();
                if (line == null || "\n".equals(line) || "".equals(line)) {
                    it.remove();
                } else {
                    break;
                }
            }
        }

        private void printAct(final ActRecord act) {
            if ("1".equals(act._record)) {
                if ("1".equals(act._div)) {
                    svfVrsOutnForKinsoku("ACTION" + act._code, act._g, "○"); //行動の記録
                }
            }
        }

        public void printSpecialAct(final ActRecord act) {
            if ("1".equals(act._record)) {
                if ("2".equals(act._div)){
                    svfVrsOutnForKinsoku("SPECIALACT" + act._g, act._code, "○"); //特別行動の記録
                }
            }
        }

        private void printAttend(final Attend attend) {
            svfVrsOutnForKinsoku("LESSON",  attend._g, attend._lesson);           //授業日数
            svfVrsOutnForKinsoku("SUSPEND", attend._g, attend._suspendMourning);  //出停・忌引
            svfVrsOutnForKinsoku("PRESENT", attend._g, attend._requirePresent);   //要出席
            svfVrsOutnForKinsoku("ATTEND",  attend._g, attend._present);          //出席
            svfVrsOutnForKinsoku("ABSENCE", attend._g, attend._absent);           //欠席
            if (_isKaijyoOldForm || param()._z010.in(Z010.bunkyo)) {
                svfVrsOutnForKinsoku("LATE",  attend._g, attend._late);          //遅刻
                svfVrsOutnForKinsoku("EARLY", attend._g, attend._early);           //早退
            }
        }

        private void printName(final PersonalInfo personalInfo) {
            int posx1;
            int posx2;
            int height;
            int minnum;
            int maxnum;
            int ystart, ystart1, ystart2;
            if (_isKaijyoOldForm) {
                posx1 = 510;
                posx2 = 1062;
                height = 50;
                ystart = 368;
                ystart1 = 343;
                ystart2 = 393;
                minnum = 20;
                maxnum = 48;
            } else if (param()._z010.in(Z010.tosa) || param().isTokubetsuShien()) {
                final SvfField fieldName1 = getSvfField("NAME1");
                final SvfField fieldName2 = getSvfField("NAME2");
                final SvfField fieldName3 = getSvfField("NAME3");
                posx1 = fieldName1.x();
                ystart = fieldName1.y();
                ystart1 = null == fieldName2 ? -1 : fieldName2.y();
                ystart2 = null == fieldName3 ? -1 : fieldName3.y();
                posx2 = posx1 + (int) SvfFieldData.fieldWidth(fieldName1, 0);
                height = (int) SvfFieldData.charHeightPixel("", 12.0);
                minnum = 20;
                maxnum = fieldName1._fieldLength;
            } else {
                posx1 = 176;
                posx2 = 708;
                height = 50;
                ystart = 510;
                ystart1 = 480;
                ystart2 = 540;
                minnum = 20;
                maxnum = 48;
            }

            if (personalInfo._useRealName && personalInfo._nameOutputFlg && !personalInfo._studentRealName.equals(personalInfo._studentName) && ystart1 > 0 && ystart2 > 0) {
                printName(personalInfo._studentRealName,           new SvfFieldData("NAME2", posx1, posx2, height, minnum, maxnum, ystart1));
                printName("（" + personalInfo._studentName + "）", new SvfFieldData("NAME3", posx1, posx2, height, minnum, maxnum, ystart2));
            } else {
                printName(personalInfo._useRealName ? personalInfo._studentRealName : personalInfo._studentName, new SvfFieldData("NAME1", posx1, posx2, height, minnum, maxnum, ystart));
            }
        }

        /**
         *  個人情報  学籍等履歴情報
         */
        private void printRegd4(final Student student, final PersonalInfo personalInfo, final List<String> pageYears) {
            //生徒名印刷
            printName(personalInfo);

            if (param()._z010.in(Z010.bunkyo)) {
                //学籍履歴の取得および印刷
                for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                    if (!student.isPrintYear(gakuseki._year, param())) {
                        continue;
                    }
                    if (!pageYearsContains(pageYears, gakuseki._year)) {
                        continue;
                    }
                    if (null != gakuseki._year && !(personalInfo.getYearBegin() <= Integer.parseInt(gakuseki._year) && Integer.parseInt(gakuseki._year) <= personalInfo.getYearEnd(param()))) {
                        // log.debug(" skip print year = " + regdRecord._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd());
                        continue;
                    }
                    if (param()._z010.in(Z010.bunkyo)) {
                        vrsOut("YEAR_" + String.valueOf(gakuseki._i) + "_2", gakuseki._nendo); // 組
                    }
                    svfVrsOutnForKinsoku("HR_NAME", gakuseki._i, gakuseki._hrname); // 組
                    svfVrsOutnForKinsoku("ATTENDNO", gakuseki._i, String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号
                }
            }
        }
    }

    /**
     * 学習の記録　知的障害者用 文言評価
     */
    private static class KNJA134J_3 extends KNJA133J_0 {

        KNJA134J_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        public int printSvf(final DB2UDB db2, final Student student) {
            final List<PersonalInfo> personalInfoList = student.getPrintSchregEntGrdHistList(param());
            int page = 0;
            for (final PersonalInfo personalInfo : personalInfoList) {

                final String form;
                if (param()._z010.in(Z010.fukuiken)) {
                    form = "KNJA134J_3FUKUI.frm";
                } else {
                    form = "KNJA134J_3.frm";
                }
                setForm(form, 4);
                printRegd3(student, personalInfo);

                final String[] monkashoKyoka = {"国語", "社会", "数学", "理科", "音楽", "美術", "保健体育", "技術・家庭", "その他", "特別活動", "自立活動"};
                for (int i = 0; i < monkashoKyoka.length; i++) {
                    vrsOut("CLASS" + String.valueOf(i + 1), monkashoKyoka[i]);

                }
                if (param()._z010.in(Z010.fukuiken)) {
                    printRecordFukuiken(student, personalInfo);
                } else {
                    printRecord(student, personalInfo);
                }
                hasdata = true;
                page += 1;
            }
            return page;
        }

        private void printRecord(final Student student, final PersonalInfo personalInfo) {
            final String blankGroup = "--";

            final Map<String, List<PrintLine>> gradeCdLineListMap = new HashMap<String, List<PrintLine>>();
            for (final String gradeCd : personalInfo._gradecdClassRemarkListMap.keySet()) {
                if (!NumberUtils.isDigits(gradeCd) || Integer.parseInt(gradeCd) <= 0 || Integer.parseInt(gradeCd) > 3) {
                    continue;
                }
                final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, gradeCd);

                final List<ClassRemark> classRemarkList = getMappedList(personalInfo._gradecdClassRemarkListMap, gradeCd);
                for (int i = 0; i < classRemarkList.size(); i++) {
                    final ClassRemark classRemark = classRemarkList.get(i);

                    final String group = gradeCdStr + (i % 2 == 1 ? "1" : "0");

                    final String remark = "（" + StringUtils.defaultString(classRemark._classname) + "）" + "\n" + classRemark._remark;

                    final List<String> tokenList = getTokenList(remark, 48, param());
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
                        vrsOut("TOTAL_RECORD" + gradeCdStr, printLine._line);
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

        private void printRecordFukuiken(final Student student, final PersonalInfo personalInfo) {

            final Map<String, List<PrintLine>> gradeCdLineListMap = new HashMap<String, List<PrintLine>>();
            for (final String gradeCd : personalInfo._gradecdClassRemarkListMap.keySet()) {
                if (!NumberUtils.isDigits(gradeCd) || Integer.parseInt(gradeCd) <= 0 || Integer.parseInt(gradeCd) > 3) {
                    continue;
                }
                final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, gradeCd);

                final List<ClassRemark> classRemarkList = getMappedList(personalInfo._gradecdClassRemarkListMap, gradeCd);
                for (int i = 0; i < classRemarkList.size(); i++) {
                    final ClassRemark classRemark = classRemarkList.get(i);

                    final String group = gradeCdStr + (i % 2 == 1 ? "1" : "0");

                    final String classname = StringUtils.defaultString(classRemark._classname);

                    final List<String> tokenList = getTokenList(classRemark._remark, 42, param());

                    for (int j = 0; j < Math.max(classname.length(), tokenList.size()); j++) {
                        String classname1 = null;
                        String line = null;
                        if (j < classname.length()) {
                            classname1 = classname.substring(j, j + 1);
                        }
                        if (j < tokenList.size()) {
                            line = (String) tokenList.get(j);
                        }
                        lineList.add(PrintLine.createLineWithClassname(group, line, classname1));
                    }
                }
            }

            final String blankGroup = "--";
            final DecimalFormat df = new DecimalFormat("00");
            final int maxLine = 71;
            for (int gi = 1; gi <= 3; gi++) {
                final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, df.format(gi));
                for (int li = 0; li < maxLine; li++) {
                    if (li < lineList.size()) {
                        final PrintLine printLine = lineList.get(li);
                        vrsOut("GRP1_CLASS", printLine._group);
                        vrsOut("CLASSNAME1", printLine._classname);
                        vrsOut("GRP1", printLine._group);
                        vrsOut("TOTAL_RECORD1", printLine._line);
                    } else {
                        vrsOut("GRP1_CLASS", blankGroup);
                        vrsOut("GRP1", blankGroup);
                    }
                    _svf.VrEndRecord();
                }
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printRegd3(final Student student, final PersonalInfo personalInfo) {

            printName(personalInfo);

            vrsOutSelect(new String[][] {{"SCHOOLNAME1"}, {"SCHOOLNAME2"}}, param()._schoolName1);


            for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                // ホームルーム
                svfVrsOutnForKinsoku(getHrNameField(gakuseki._hrname), gakuseki._i, gakuseki._hrname);
                svfVrsOutnForKinsoku("ATTENDNO", gakuseki._i, String.valueOf(Integer.parseInt(gakuseki._attendno)));
            }
        }

        private void printName(final PersonalInfo personalInfo) {
            final int posx1 = 416;
            final int posx2 = 950;
            final int height = 50;
            final int minnum = 20;
            final int maxnum = 48;

            if (personalInfo._useRealName && personalInfo._nameOutputFlg && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                printName(personalInfo._studentRealName,           new SvfFieldData("NAME2", posx1, posx2, height, minnum, maxnum, 484));
                printName("（" + personalInfo._studentName + "）", new SvfFieldData("NAME3", posx1, posx2, height, minnum, maxnum, 544));
            } else {
                final String name = personalInfo._useRealName ? personalInfo._studentRealName : personalInfo._studentName;
                printName(name, new SvfFieldData("NAME1", posx1, posx2, height, minnum, maxnum, 514));
            }
        }

        private static class PrintLine {
            String _group;
            String _line;

            String _classname;
            boolean _useClassname2;
            String _classname2_1;
            String _classname2_2;

            static PrintLine createLine(final String group, final String line) {
                final PrintLine printLine = new PrintLine();
                printLine._group = group;
                printLine._line = line;
                return printLine;
            }

            static PrintLine createLineWithClassname(final String group, final String line, final String classname) {
                final PrintLine printLine = new PrintLine();
                printLine._group = group;
                printLine._line = line;
                printLine._classname = classname;
                return printLine;
            }

            static PrintLine createLineWithClassname2(final String group, final String line, final String classname2_1, final String classname2_2) {
                final PrintLine printLine = new PrintLine();
                printLine._group = group;
                printLine._line = line;
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

            private static Map loadGradecdClassRemarkListMap(final DB2UDB db2, final Param param, final Student student) {
                final Map gradeCdClassRemarkListMap = new HashMap();
                if (!(param.isTokubetsuShien() && CHITEKI1_知的障害.equals(param._chiteki))) {
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
                        stb.append("   AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
                        stb.append(" ORDER BY ");
                        stb.append("   T2.GRADE_CD ");
                        stb.append("   , VALUE(CM.SHOWORDER, -1) "); // 表示順教科
                        stb.append("   , CM.CLASSCD");
                        stb.append("   , CM.SCHOOL_KIND");
                        stb.append("   , VALUE(SCM.SHOWORDER, -1) "); // 表示順科目
                        stb.append("   , SCM.CURRICULUM_CD");
                        stb.append("   , SCM.SUBCLASSCD");

                        param.setPs(db2, psKey, stb.toString());
                    }

                    for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { student._schregno})) {

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
    private static class KNJA129Delegate extends KNJA133J_0 {

        KNJA129 _knja129;
        KNJA129.Param _knja129param;

        KNJA129Delegate(final Vrw32alp svf, final Param param, final HttpServletRequest request, final DB2UDB db2) {
            super(svf, param);
            _knja129 = new KNJA129();
            _knja129param = _knja129.getParam(request, db2, param._year, param._gakki, Param.SCHOOL_KIND);
        }


        public void printSvf(final DB2UDB db2, final Student student) {
            setDetail4(db2, student._personalInfo, null, null);
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

    /**
     * SVF フィールドのデータ (文字の大きさ調整に使用)
     */
    private static class SvfFieldData {
        private static final double dpi = 400.0;
        private static final double pointPerInch = 72;

        private final String _fieldName; // フィールド名
        private final int _posx1;   // フィールド左端のX
        private final int _posx2;   // フィールド右端のX
        private final int _height;  // フィールドの高さ(ドット)
        private final int _minnum;  // 最小設定文字数
        private final int _maxnum;  // 最大設定文字数
        private final int _ystart;  // フィールド上端のY
        public SvfFieldData(
                final String fieldName,
                final int posx1,
                final int posx2,
                final int height,
                final int minnum,
                final int maxnum,
                final int ystart) {
            _fieldName = fieldName;
            _posx1 = posx1;
            _posx2 = posx2;
            _height = height;
            _minnum = minnum;
            _maxnum = maxnum;
            _ystart = ystart;
        }
        public int getWidth() {
            return _posx2 - _posx1;
        }
        public String toString() {
            return "[SvfFieldData: fieldname = " + _fieldName + " width = "+ (_posx2 - _posx1) + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum + "]";
        }

        /////////////////////////////////////////////////////////////////

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final SvfField field, final int upperOrLower) {
            final double charSize = Double.parseDouble((String) field.getAttributeMap().get(SvfField.AttributeSize));
            final int keta = Integer.parseInt((String) field.getAttributeMap().get(SvfField.AttributeKeta));
            return fieldWidth(charSize, upperOrLower, keta);
        }

        /**
         * フィールドの幅を得る
         * @param charSize 文字サイズ
         * @param keta フィールド桁
         * @return フィールドの幅
         */
        public static double fieldWidth(final double charSize, final int upperOrLower, final int keta) {
            return charPointToPixel("", charSize, upperOrLower) * keta / 2;
        }

        private static double charHeightPixel(final String debugString, final double charSize) {
            return charPointToPixel(debugString, charSize, 0);
        }

//        /**
//         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
//         * @param posx1 フィールドの左端X
//         * @param posx2 フィールドの右端X
//         * @param num フィールド指定の文字数
//         * @param charSize 変更後の文字サイズ
//         * @return ずれ幅の値
//         */
//        public int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, float charSize) {
//            final double maxWidth = fieldWidth((double) charSize, num); // 文字の大きさを考慮したフィールドの最大幅
//            final int offset = ((int) maxWidth / 2) - (posx2 - posx1) / 2 + 10;
//            return offset;
//        }

//        /**
//         *  ポイントの設定
//         *  引数について  String str : 出力する文字列
//         */
//        public float getCharSize(String str) {
//            return Math.min((float) pixelToCharPoint(_height), retFieldPoint(_width, getStringByteSize(str)));                  //文字サイズ
//        }

//        /**
//         * 文字列のバイト数を得る
//         * @param str 文字列
//         * @return 文字列のバイト数
//         */
//        private int getStringByteSize(String str) {
//            final String str1 = str;
//			return Math.min(Math.max(getMS932ByteLength(str1), _minnum), _maxnum);
//        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charPoint 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static double charPointToPixel(final String debugString, final double charPoint, final int upperOrLower) {
            return SvfFieldAreaInfo.KNJSvfFieldModify.charPointToPixel(debugString, charPoint, upperOrLower);
        }


//        /**
//         * 文字サイズをピクセルに変換した値を得る
//         * @param charSize 文字サイズ
//         * @return 文字サイズをピクセルに変換した値
//         */
//        public static int charSizeToPixel(final double charSize) {
//            return (int) Math.round(charSize / 72 * 400);
//        }
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
        private String _gradeCd;

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
            final Map<String, TreeMap<String, SchregRegdDat>> m = new TreeMap<String, TreeMap<String, SchregRegdDat>>();
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
        public static double getCharSize(final String str, final SvfFieldData fieldData) {
            return Math.min(pixelToCharSize(fieldData._height), retFieldPoint(fieldData.getWidth(), getStringByteSize(str, fieldData))); //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private static int getStringByteSize(final String str, final SvfFieldData fieldData) {
            return Math.min(Math.max(getMS932ByteLength(str), fieldData._minnum), fieldData._maxnum);
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
        public static long getYjiku(final int hnum, final double charSize, final SvfFieldData fieldData) {
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
            return (double) Math.round((double) width / (num / 2) * 72 / 400 * 10) / 10;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static long retFieldY(final int height, final double charSize) {
            return Math.round(((double) height - charSizeToPixel(charSize)) / 2);
        }
    }

    private static class Util {

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
        private static String regexSpaced(final String s) {
            if (StringUtils.isEmpty(s)) {
                return "";
            }
            final StringBuilder stb = new StringBuilder();
            String comma = "";
            for (final char c : s.toCharArray()) {
                stb.append(comma).append(c);
                comma = "(　|\\s)*";
            }
            return stb.toString();
        }
        private static boolean between(final int val, final int lower, final int upper) {
            return lower <= val && val <= upper;
        }
        private static String take(final int count, final String s) {
            return s.substring(0, Math.min(count, s.length()));
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

        public static String listString(final Collection coll, final int depth) {
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

        public static String debugMapToStr(final String debugText, final Map map0, final String comma) {
            final Map m = new HashMap();
            m.putAll(map0);
            for (final Iterator<Map.Entry> it = m.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = it.next();
                if (e.getKey() instanceof Integer) {
                    it.remove();
                } else if (e.getKey() instanceof String) {
                    final String key = (String) e.getKey();
                    final int numIndex = StringUtils.indexOfAny(key, "123456789");
                    if (0 <= numIndex && StringUtils.repeat("_", numIndex).equals(key.substring(0, numIndex))) {
                        it.remove();
                    }
                }
            }
            final Map map = new TreeMap(m);
            final StringBuffer stb = new StringBuffer();
            stb.append(defstr(debugText));
            stb.append(" [");
            final List keys = new ArrayList(map.keySet());
            try {
                Collections.sort(keys);
            } catch (Exception e) {
            }
            final List<String> elems = new ArrayList<String>();
            for (int i = 0; i < keys.size(); i++) {
                final Object key = keys.get(i);
                elems.add(key + ": " + map.get(key));
            }
            stb.append(mkString(elems, comma));
            stb.append("]");
            return stb.toString();
        }
    }

    private static class ShokenSize {
        int _mojisu;
        int _gyo;

        ShokenSize(final int mojisu, final int gyo) {
            _mojisu = mojisu;
            _gyo = gyo;
        }

        private static ShokenSize getShokenSize(final String paramString, final int mojisuDefault, final int gyoDefault) {
            final int mojisu = ShokenSize.getParamSizeNum(paramString, 0);
            final int gyo = ShokenSize.getParamSizeNum(paramString, 1);
            if (-1 == mojisu || -1 == gyo) {
                return new ShokenSize(mojisuDefault, gyoDefault);
            }
            return new ShokenSize(mojisu, gyo);
        }

        /**
         * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
         * @param param サイズタイプのパラメータ文字列
         * @param pos split後のインデクス (0:w, 1:h)
         * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
         */
        private static int getParamSizeNum(final String param, final int pos) {
            int num = -1;
            if (StringUtils.isBlank(param)) {
                return num;
            }
            final String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
            if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
                num = -1;
            } else {
                try {
                    num = Integer.valueOf(nums[pos]).intValue();
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
            }
            return num;
        }

        public String toString() {
            return "ShokenSize(" + _mojisu + ", " + _gyo + ")";
        }
    }

    protected static enum Z010 {
        HOUSEI("法政")
      , KINJUNIOR("近大中学")
      , oomiya("大宮開成")
      , tokyoto("東京都")
      , CHIBEN("智辯学園") // 智辯和歌山・智辯奈良五條・智辯奈良カレッジ
      , kyoai("共愛")
      , meiji("明治")
      , bunkyo("文京")
      , kaijyo("海城")
      , musashinohigashi("武蔵野東")
      , sundaikoufu("駿台甲府")
      , seijyo("成城")
      , tosa("土佐塾")
      , fukuiken("福井県")
      , chiyodaKudan("chiyoda", "千代田区九段")
      , osakatoin("大阪桐蔭")
      , TamagawaSei("Tamagawa-sei", "玉川聖")
      , naraken("奈良県")
      , kikan("開智一貫部")
      , ktsushin("開智通信制")
      , sagaken("佐賀県")
      , osakashinnai("大阪信愛")
      , sanonihon("佐野日本")
      , nagisa("広島なぎさ")
      , komazawa("koma", "駒澤大学")
      , reitaku("麗澤")
      , shimaneken("島根県")
      , aoyama("青山学院")
      , kwansei("関西学院")
      , jogakkan("女学館")
      , teihachi("帝京八王子")
      , kenja("賢者")
      ;

        final String _name1;
        final String _gakkoumei;
        Z010(final String gakkoumei) {
            this(null, gakkoumei);
        }
        Z010(final String name1, final String gakkoumei) {
            _name1 = name1;
            _gakkoumei = gakkoumei;
        }

        public boolean in(final Z010 ...z010s) {
            return ArrayUtils.contains(z010s, this);
        }

        public String toString() {
            return "Z010(name = " + this.name() + ", " + _gakkoumei + ")";
        }

        public static Z010 fromName1(final String name1) {
            for (final Z010 z010 : values()) {
                if (null == z010._name1) {
                    if (z010.name().equals(name1)) {
                        return z010;
                    }
                } else {
                    if (z010._name1.equals(name1)) {
                        return z010;
                    }
                }
            }
            return kenja;
        }
    }

    private enum Property {
        HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J,
        HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability,
        HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J,
        HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability,
        HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J,
        HTRAINREMARK_DAT_VIEWREMARK_SIZE_J,
        HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability,
        HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J,
        HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability,
        HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J,
        HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability,
        HTRAINREMARK_DAT_TOTALREMARK_SIZE_J,
        HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability,
        HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J,
        HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability,
        HTRAINREMARK_DAT_REMARK1_SIZE_J,
        HTRAINREMARK_DAT_REMARK1_SIZE_J_disability,
        HTRAINREMARK_DETAIL2_DAT_004_REMARK1_SIZE_J,
        certifPrintRealName,
        knja133jForm1,
        knja133jForm3,
        knja133jForm3NoDoutoku,
        knja133jForm4,
        knja133jUseViewSubclassMstSubclasscd2,
        knja133jElectSubclassHyoteiConv,
        knja133jPrintSogoShokenStrictSize,
        not_print_guarantor,
        notPrintFinschooltypeName, // FINSCHOOL_MST.FINSCHOOL_TYPE(名称マスタ「L019」、「中学校」「小学校」等)を表示しない
        seitoSidoYorokuCyugakuKirikaeNendoForRegdYear,
        seitoSidoYorokuCyugakuKirikaeNendo,
        seitoSidoYorokuCyugakuKantenNoBlank,
        seitoSidoYorokuCyugakuDoutokuKirikaeCheckGrade,
        seitoSidouYorokuUseEditKinsokuJ,
        seitoSidoYorokuKinsokuFormJ,
        train_ref_1_2_3_use_J,
        train_ref_1_2_3_field_size_J,
        train_ref_1_2_3_gyo_size_J,
        useSchregRegdHdat,
        useCurriculumcd,
        useSpecial_Support_School,
    }

    private static class Tuple<A, B> implements Comparable<Tuple<A, B>> {
        final A _first;
        final B _second;
        private Tuple(final A first, final B second) {
            _first = first;
            _second = second;
        }
        public static <A, B> Tuple<A, B> of(A a, B b) {
            return new Tuple<A, B>(a, b);
        }
        public int compareTo(final Tuple<A, B> o) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == o._first && !(o._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(o._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == o._second && !(o._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(o._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    private static class Param {

        final static String SCHOOL_KIND = "J";

        final String _year;
        final String _gakki;
        final String _hrClassType; // KNJA134Jのみ
        final String _gakunenKongou; // KNJA134Jのみ
//        final String _notPrintGuarantor; // KNJA134Jのみ
        final String _gradeHrclass;
        final Map<String, String> _parameterMap;
        final String _output;
        final String[] _categorySelected;
        final String _inei;
        final boolean _isPrintInei;
        final Map _paramMap;
        final Map<String, String> _nendoMap = new TreeMap<String, String>();

        final String seito;
        final String gakushu;
        final String koudo;
        final String online;

        private KNJA133J_1 _knja133j_1;
        private KNJA133J_3 _knja133j_3;
        private KNJA134J_3 _knja134j_3;
        private KNJA133J_4 _knja133j_4;
        private KNJA129Delegate _knja129;

        final String _simei;
        final String _schzip;
        final String _schoolzip;
        final String _colorPrint;
        final String _documentroot;
        final String _useCurriculumcd;
        final String _printBlankPage; // 奇数ページの時は空白ページを印刷する
        final String _seitoSidoYorokuUseHrClassTypeJ;
        final boolean _useEditKinsoku;
        final boolean _seitoSidoYorokuKinsokuFormJ;

        final String _imagePath;
        final String _extension;
        final String _histApplyExpireDate;

        private String _schoolName1;
        private String _certifSchoolName;
        private String _schoolZipCd;
        private String _schoolAddr1;
        private String _schoolAddr2;
        private String _schoolTelno;
        Map<String, Semester> _semesterMap;

        /** 生年月日に西暦を使用するか */
        final boolean _isSeireki;

        final StaffInfo _staffInfo = new StaffInfo();
        final Map<String, String> _gradeCdMap;
        private Map _hmap = null;

        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        final List<File> _deleteFiles = new ArrayList<File>();
        final Set<String> _logSet = new HashSet<String>();

        final Z010 _z010;
        final boolean _isKaichi;

        /** 年度の4/1時点の元号を表示する */
        final boolean _useGengoInApr01;

        /** 卒業した学校の設立区分を表示するか */
        private boolean _isInstallationDivPrint;

        final boolean _hasSchregEntGrdHistComebackDat;
        final boolean _hasAftGradCourseDat;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE;
        final boolean _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT_TENGAKU_SAKI_GRADE;
        final boolean _hasFINSCHOOL_DETAIL_MST;
//        final String _knja133jPrintSogoShokenStrictSize;


        final Map<String, Map<String, List<String>>> _slashViewFieldIndexMapList;
        final Map<String, List<String>> _slashValueFieldIndexMapList;

        final String _chiteki;

//        final String _HTRAINREMARK_DAT_REMARK1_SIZE_J;

        final String _train_ref_1_2_3_use_J;
        final String _train_ref_1_2_3_field_size_J;
        final String _train_ref_1_2_3_gyo_size_J;
        private Integer[] _train_ref_1_2_3_field_size_JMojisu = {-1, -1, -1};

        final List<String> _d065Name1List;
        final Map<String, String> _d001Abbv1Map;
        final Map<String, String> _d087Name1Map;
        final Map<String, Map<String, List<String>>> _a047Name2Name3Map;
        final Map _sessionCache = new HashMap();
        Properties _prgInfoPropertiesFilePrperties = null;
        final Map<String, String> _prgInfoPropertiesDb;

        boolean _isOutputDebugAll;
        boolean _isOutputDebug;
        boolean _isOutputDebugData;
        boolean _isOutputDebugKinsoku;
        boolean _isOutputDebugQuery;
        boolean _isOutputDebugStaff;
        boolean _isOutputDebugKanten;
        boolean _isOutputDebugFormCreate;
        List<String> _outputDebugFieldList = null;

        public Param(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) {

            if (!"KNJA133J".equals(request.getParameter("PRGID"))) {
                log.info(" prgid = " + request.getParameter("PRGID"));
            }
            _documentroot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
            if (!StringUtils.isEmpty(_documentroot)) {
                _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
            }
            _prgInfoPropertiesDb = getDbPrginfoProperties(db2, request.getParameter("PRGID"));

            _year = parameter(request, "YEAR"); // 年度
            _gakki = parameter(request, "GAKKI"); // 学期
            _hrClassType = parameter(request, "HR_CLASS_TYPE");
            _gakunenKongou = parameter(request, "GAKUNEN_KONGOU");
            _gradeHrclass = parameter(request, "GRADE_HR_CLASS"); // 学年・組
            _categorySelected = request.getParameterValues("category_selected");
            _output = parameter(request, "OUTPUT");

            seito = parameter(request, "seito");
            gakushu = parameter(request, "gakushu");
            koudo = parameter(request, "koudo");
            online = parameter(request, "online");
            _parameterMap = new HashMap<String, String>();
            for (final Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
                if (null != e.getValue() && e.getValue().length > 0) {
                    _parameterMap.put(e.getKey(), e.getValue()[0]);
                }
            }
//            _notPrintGuarantor = property(Property.not_print_guarantor);

            _simei = parameter(request, "simei"); // 漢字名出力
            _schzip = parameter(request, "schzip");
            _schoolzip = parameter(request, "schoolzip");
            _colorPrint = parameter(request, "color_print");

            _useCurriculumcd = property(Property.useCurriculumcd);
            _printBlankPage = parameter(request, "PRINT_BLANK_PAGE");
            _seitoSidoYorokuUseHrClassTypeJ = parameter(request, "seitoSidoYorokuUseHrClassTypeJ");
            if ("1".equals(parameter(request, "inei_print2"))) {
                _inei = "4";
            } else if ("1".equals(parameter(request, "inei_print"))) {
                _inei = "3";
            } else if ("1".equals(parameter(request, "seitoSidoYorokuPrintInei"))) {
                _inei = "2";
            } else {
                final String INEI = parameter(request, "INEI");
                _inei = StringUtils.isBlank(INEI) ? null : INEI;
            }
            if (null != _inei) {
                log.info(" inei = " + _inei);
            }
            _isPrintInei = null != _inei;
            _paramMap = paramMap;

            final String[] outputDebug = StringUtils.split(_prgInfoPropertiesDb.get("outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugData = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "data");
            _isOutputDebugKinsoku = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "kinsoku");
            _isOutputDebugQuery = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugStaff = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "staff");
            _isOutputDebugKanten = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "kanten");
            _isOutputDebugFormCreate = ArrayUtils.contains(outputDebug, "SvfFormCreate");
            if (null != outputDebug) {
                if (_isOutputDebugAll) {
                    _outputDebugFieldList = new ArrayList<String>();
                } else {
                    for (int i = 0; i < outputDebug.length; i++) {
                        if (null != outputDebug[i]) {
                            if (outputDebug[i].startsWith("field")) {
                                _outputDebugFieldList = new ArrayList<String>();
                                if (outputDebug[i].startsWith("field=")) {
                                    final String[] split = StringUtils.split(outputDebug[i].substring("field=".length()), ",");
                                    for (int j = 0; j < split.length; j++) {
                                        _outputDebugFieldList.add(split[j]);
                                    }
                                    log.info(" outputDebugFieldList = " + _outputDebugFieldList);
                                }
                            }
                        }
                    }
                }
            }
            if (null != outputDebug && outputDebug.length > 0) {
                log.info(" outputDebug = " + ArrayUtils.toString(outputDebug) + " / " + _isOutputDebugAll + " / " + _isOutputDebugQuery);
            }

//            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
//            _imagePath = null == returnval ? null : returnval.val4; // 写真データ格納フォルダ
//            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _imagePath = "image/stamp";
            _extension = "bmp";
            _histApplyExpireDate = String.valueOf(Integer.parseInt(_year) + 1) + "-03-31";

            _hasSchregEntGrdHistComebackDat = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _hasAftGradCourseDat = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasSCHREG_ENT_GRD_HIST_DAT_TENGAKU_SAKI_GRADE = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_DAT", "TENGAKU_SAKI_GRADE");
            _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT_TENGAKU_SAKI_GRADE = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", "TENGAKU_SAKI_GRADE");
            _hasFINSCHOOL_DETAIL_MST = KnjDbUtils.setTableColumnCheck(db2, "FINSCHOOL_DETAIL_MST", null);

            loadSchoolInfo(db2);

            _isSeireki = getSeireki(db2);

            _gradeCdMap = getGradeCdMap(db2);

            final String z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            _z010 = Z010.fromName1(z010Name1);
            log.info(" z010 = " + _z010 + " (z010 name1 = " + z010Name1 + ")");
            _isKaichi = Arrays.asList("kmirai", "knihon", "knozomi", "kkotou", "ksogo", "kikan").contains(z010Name1);

            _useGengoInApr01 = _z010.in(Z010.chiyodaKudan);

            _isInstallationDivPrint = !("KINDAI".equals(z010Name1) || _z010.in(Z010.KINJUNIOR));

            _useEditKinsoku = !(_z010.in(Z010.kaijyo) || _z010.in(Z010.KINJUNIOR) || _z010.in(Z010.oomiya) || _z010.in(Z010.CHIBEN) || _z010.in(Z010.bunkyo)) || "1".equals(property(Property.seitoSidouYorokuUseEditKinsokuJ));
            _seitoSidoYorokuKinsokuFormJ = !(_z010.in(Z010.kaijyo) || _z010.in(Z010.bunkyo) || _z010.in(Z010.KINJUNIOR)) && (!_useEditKinsoku && "1".equals(property(Property.seitoSidoYorokuKinsokuFormJ)));

            _slashViewFieldIndexMapList = getSlashViewIndexList(db2);
            _slashValueFieldIndexMapList = getSlashValueIndexMapList(db2);

            _chiteki = parameter(request, "CHITEKI");
//            _HTRAINREMARK_DAT_REMARK1_SIZE_J                            = property(Property.HTRAINREMARK_DAT_REMARK1_SIZE_J);
//            _knja133jPrintSogoShokenStrictSize = property(Property.knja133jPrintSogoShokenStrictSize);

            _train_ref_1_2_3_use_J = property(Property.train_ref_1_2_3_use_J);
            _train_ref_1_2_3_field_size_J = property(Property.train_ref_1_2_3_field_size_J);
            _train_ref_1_2_3_gyo_size_J = property(Property.train_ref_1_2_3_gyo_size_J);
            if ("1".equals(_train_ref_1_2_3_use_J)) {
                final String[] mojisu;
                final Integer _14 = new Integer(14);
                if (!StringUtils.isBlank(_train_ref_1_2_3_field_size_J)) {
                    mojisu = StringUtils.split(_train_ref_1_2_3_field_size_J, "-");
                } else {
                    mojisu = new String[] {_14.toString(), _14.toString(), _14.toString()};
                }
                _train_ref_1_2_3_field_size_JMojisu = new Integer[] {getIdxInteger(mojisu, 0, _14), getIdxInteger(mojisu, 1, _14), getIdxInteger(mojisu, 2, _14)};
            }

            _d065Name1List = getD065Name1List(db2);
            _d001Abbv1Map = getD001Abbv1Map(db2);
            _d087Name1Map = getD087Name1Map(db2);
            if (_isOutputDebugKanten) {
                log.info(" _d087Name1Map = " + _d087Name1Map);
            }
            _a047Name2Name3Map= getA047Name2Name3Map(db2);
            setMaster(db2);
            _staffInfo.setInkanMap(db2, this);
            _staffInfo.setYearPrincipalMap(db2, this);
            _staffInfo._staffMstMap = StaffMst.load(db2, this, _year);
            _staffInfo.setStaffClassHistMap(db2, this, _year);
        }

        private boolean isOutputDebugField(final String field) {
            if (null == _outputDebugFieldList) {
                return false;
            }
            if (null == field || _outputDebugFieldList.isEmpty()) {
                return true;
            }
            for (final String debugField : _outputDebugFieldList) {
                if (field.indexOf(debugField) >= 0) {
                    return true;
                }
            }
            return false;
        }

        private void setMaster(final DB2UDB db2) {
            setSemester(db2);
        }

        private void setSemester(final DB2UDB db2) {
            _semesterMap = new HashMap<String, Semester>();
            final String sql = " SELECT YEAR, SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE SEMESTER <> '9' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final Semester semester = new Semester(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE"));
                final String key = Semester.key(semester._year, semester._semester);
                _semesterMap.put(key, semester);
            }
        }

        private Properties loadPropertyFile(final String filename) {
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

        private String parameter(final HttpServletRequest request, final String name) {
            return request.getParameter(name);
        }

        private String property(final Property property) {
            final String name = property.name();
            if (_parameterMap.containsKey(name)) {
                return _parameterMap.get(name);
            }
            String val = null;
            if (_prgInfoPropertiesDb.containsKey(name)) {
                val = _prgInfoPropertiesDb.get(name);
                logOnce("property in db: " + name + " = " + val);
                return val;
            }
            if (null != _prgInfoPropertiesFilePrperties) {
                val = _prgInfoPropertiesFilePrperties.getProperty(name);
                if (_isOutputDebug) {
                    if (!_prgInfoPropertiesFilePrperties.containsKey(name)) {
                        logOnce("property not exists in file: " + name);
                    } else {
                        logOnce("property in file: " + name + " = " + val);
                    }
                }
            }
            return val;
        }

        private static Map<String, String> getDbPrginfoProperties(final DB2UDB db2, final String prgid) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = ? ", new Object[] {StringUtils.defaultString(prgid, "KNJA133J")}), "NAME", "VALUE");
        }

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        public void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }

        public void close() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
            for (final File file : _deleteFiles) {
                log.info(" delete file : " + file.getAbsolutePath() + ", deleted? " + (_isOutputDebugFormCreate ? false : file.delete()));
            }
        }

        public String getNendo(final DB2UDB db2, final String year) {
            if (StringUtils.isBlank(year)) {
                return null;
            }
            if (!_nendoMap.containsKey(year)) {
                _nendoMap.put(year, KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(year)));
            }
            return _nendoMap.get(year);
        }

        /**
         * 日付の和暦年度文言を得る
         * "2019-04-30" -> "平成31年度"
         * @param db2
         * @param date
         * @return
         */
        protected String dateNendo(final DB2UDB db2, final String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            if (_isSeireki) {
                return String.valueOf(getCalendarNendo(getCalendarOfDate(date))) + "年度";
            }
            final String[] format = KNJ_EditDate.tate_format4(db2, date);
            final String gengo = defstr(format[0]);
            String nen = defstr(format[1]);
            String space1 = "";
            String space2 = "";
            final boolean isTatenarabi = _z010.in(Z010.kaijyo);
            if (isTatenarabi) {
                space1 = " ";
                space2 = " ";
            }
            if (NumberUtils.isDigits(month(date)) && Integer.parseInt(month(date)) < 4) {
                if (NumberUtils.isDigits(nen)) {
                    final int nenInt = Integer.parseInt(nen) - 1;
                    if (nenInt == 1) {
                        nen = "元";
                    } else {
                        nen = String.valueOf(nenInt);

                        if (isTatenarabi && nenInt < 10) {
                            space1 += " ";
                        }
                    }
                }
            }
            return gengo + space1 + nen + space2 + "年度";
        }

        private static String month(final String date) {
            try {
                return String.valueOf(getCalendarOfDate(date).get(Calendar.MONTH) + 1);
            } catch (Exception e) {
            }
            return null;
        }

        private Map<String, Map<String, SvfField>> formFieldInfoMap() {
            return getMappedMap(_sessionCache, "FORM_FIELD_INFO");
        }

        private static Integer getIdxInteger(final String[] nums, final int idx, final Integer def) {
            return nums.length < idx || !NumberUtils.isDigits(nums[idx]) ? def : Integer.valueOf(nums[idx]);
        }

        /**
         * 学校データを得る
         */
        private void loadSchoolInfo(final DB2UDB db2) {

            final String certifKindCd = "108";
            final String sqlCertifSchool = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR='" + _year + "' AND CERTIF_KINDCD='" + certifKindCd + "'";

            final Map<String, String> certifSchoolRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sqlCertifSchool));

            _certifSchoolName = KnjDbUtils.getString(certifSchoolRow, "SCHOOL_NAME");
            _schoolTelno = KnjDbUtils.getString(certifSchoolRow, "REMARK1");
            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + _certifSchoolName + "]");

            final Map paramMap = new HashMap();
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                paramMap.put("schoolMstSchoolKind", Param.SCHOOL_KIND);
            }
            String sql = null;
            try {
                sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
            } catch (Throwable e) {
                log.warn("old KNJ_SchoolinfoSql.");
                sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
            }
            for (final Map row : KnjDbUtils.query(db2, sql, new String[] { _year, _year, })) {
                _schoolName1 = KnjDbUtils.getString(row, "SCHOOLNAME1");
                _schoolAddr1 = KnjDbUtils.getString(row, "SCHOOLADDR1");
                _schoolZipCd = KnjDbUtils.getString(row, "SCHOOLZIPCD");
                _schoolAddr2 = KnjDbUtils.getString(row, "SCHOOLADDR2");
            }
        }

        /**
         * 帳票作成JAVAクラスをＬＩＳＴへ格納 NO001 Build
         */
        private void setKnja133List(final Vrw32alp svf, final HttpServletRequest request, final DB2UDB db2) {
            if (seito!= null) {
                _knja133j_1 = new KNJA133J_1(svf, this); // 様式１（学籍に関する記録）
            }
            if (gakushu != null) {
                if (isTokubetsuShien() && CHITEKI1_知的障害.equals(_chiteki)) {
                    _knja134j_3 = new KNJA134J_3(svf, this);
                } else {
                    _knja133j_3 = new KNJA133J_3(svf, this); // 様式２（指導に関する記録）
                }
            }
            if (koudo != null) {
                _knja133j_4 = new KNJA133J_4(svf, this); // 様式３
            }
            if (online != null) {
                _knja129 = new KNJA129Delegate(svf, this, request, db2); // 様式３
            }
        }

        protected List<String> getSchregnoList(final DB2UDB db2) {
            final List<String> schregnoList;
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

                schregnoList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO");
            } else {
                schregnoList = new ArrayList<String>();
                if (isTokubetsuShien()) {
                    for (int i = 0; i < _categorySelected.length; i++) {
                        schregnoList.add(StringUtils.split(_categorySelected[i], "-")[1]);
                    }
                } else {
                    schregnoList.addAll(Arrays.asList(_categorySelected));
                }
            }
            return schregnoList;
        }

        public void logOnce(final String s) {
            if (_logSet.contains(s)) {
                return;
            }
            log.info(s);
            _logSet.add(s);
        }

        public String getImageFilePath(final String filename) {
            String ret = null;
            try {
                if (null != _documentroot && null != _imagePath && null != _extension) {
                    // 写真データ存在チェック
                    final String path = _documentroot + "/" + _imagePath + "/" + filename + "." + _extension;
                    final File file = new File(path);
                    if (_isOutputDebug) {
                        logOnce(" file check: " + file.getAbsolutePath() + " exists? " + file.exists());
                    }
                    if (file.exists()) {
                        ret = path;
                    }
                }
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return ret;
        }

        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = _gradeCdMap.get(year + grade);
            return NumberUtils.isNumber(gradeCd) ? Integer.parseInt(gradeCd) : -1;
        }

        private boolean getSeireki(final DB2UDB db2) {
            return "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00'")));
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        private Map<String, String> getGradeCdMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");

            final Map<String, String> gdatMap = new HashMap();
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                gdatMap.put(year + grade, KnjDbUtils.getString(row, "GRADE_CD"));
            }
            return gdatMap;
        }

        private boolean isTokubetsuShien() {
            return "TOKUBETSU_SHIEN".equals(_paramMap.get("OUTPUT_KIND")); // KNJA134Jからコールされた場合
        }

        private boolean isSlashView(final String subclasscd, final String grade, final int line) {
            final boolean isSlash = getMappedList(getMappedMap(_slashViewFieldIndexMapList, subclasscd), grade).contains(String.valueOf(line));
            //log.debug(" isSlashView = " + isSlash + " : " + subclasscd + " : " + grade + " : " + line + " / " + _slashViewFieldIndexMapList);
            return isSlash;
        }

        private boolean isSlashValue(final String subclasscd, final String grade) {
            final boolean isSlash = getMappedList(_slashValueFieldIndexMapList, subclasscd).contains(grade);
            //log.debug(" isSlashValue = " + isSlash + " : " + subclasscd + " : " + grade + " / " + _slashValueFieldIndexMapList);
            return isSlash;
        }

        /**
         * スラッシュを表示する観点フィールド(科目、学年、行)のリスト
         */
        private Map<String, Map<String, List<String>>> getSlashViewIndexList(final DB2UDB db2) {
            Map<String, Map<String, List<String>>> map = new HashMap<String, Map<String, List<String>>>();
            String sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN, NAME3 AS LINE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A036' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                if (null != KnjDbUtils.getString(row, "SUBCLASSCD") && null != KnjDbUtils.getString(row, "GAKUNEN") && NumberUtils.isDigits(KnjDbUtils.getString(row, "LINE"))) {
                    getMappedList(getMappedMap(map, KnjDbUtils.getString(row, "SUBCLASSCD")), KnjDbUtils.getString(row, "GAKUNEN")).add(KnjDbUtils.getString(row, "LINE"));
                }
            }
            return map;
        }

        /**
         * スラッシュを表示する評定フィールド(科目、学年)のリスト
         */
        private Map<String, List<String>> getSlashValueIndexMapList(final DB2UDB db2) {
            Map<String, List<String>> map = new HashMap<String, List<String>>();
            String sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A037' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                if (null != KnjDbUtils.getString(row, "SUBCLASSCD") && null != KnjDbUtils.getString(row, "GAKUNEN")) {
                    getMappedList(map, KnjDbUtils.getString(row, "SUBCLASSCD")).add(KnjDbUtils.getString(row, "GAKUNEN"));
                }
            }
            return map;
        }

        private List<String> getD065Name1List(final DB2UDB db2) {
            final String sql = " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL ";
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "NAME1");
        }

        private Map<String, String> getD087Name1Map(final DB2UDB db2) {
            if (!"1".equals(property(Property.knja133jElectSubclassHyoteiConv))) {
                return Collections.emptyMap();
            }
            final String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D087' ";
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "NAMECD2", "NAME1");
        }

        private Map<String, String> getD001Abbv1Map(final DB2UDB db2) {
            final String sql = " SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL ";
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "NAMECD2", "ABBV1");
        }

        public boolean useSubclass(final String grade, final String subclasscd) {
            if (_a047Name2Name3Map.isEmpty()) {
                return true;
            }
            if (null == _a047Name2Name3Map.get(grade)) {
                if (_isOutputDebug) {
                    log.info(" no grade " + grade + " in " + _a047Name2Name3Map.keySet() + " => true ");
                }
                return true;
            }
            final String classKey = getClassKey(subclasscd);
            if (!getMappedMap(_a047Name2Name3Map, grade).containsKey(classKey)) {
                if (_isOutputDebug) {
                    log.info(" no class " + classKey + "(grade = " + grade + ") in " + getMappedMap(_a047Name2Name3Map, grade).keySet() + " => true");
                }
                return true;
            }
            final List<String> subclasscds = getMappedList(getMappedMap(_a047Name2Name3Map, grade), classKey);
            if (_isOutputDebug) {
                log.info(" " + subclasscds + " (grade = " + grade + ") contains " + subclasscd + " => " + (subclasscds.contains(subclasscd)));
            }
            return subclasscds.contains(subclasscd);
        }

        private String getClassKey(final String subclasscd) {
            final String classKey;
            if ("1".equals(_useCurriculumcd)) {
                final String[] split = StringUtils.split(subclasscd, "-");
                classKey = split[0] + "-" + split[1];
            } else {
                classKey = subclasscd.substring(0, 2);
            }
            return classKey;
        }

        private Map<String, Map<String, List<String>>> getA047Name2Name3Map(final DB2UDB db2) {
            final String sql = " SELECT NAME2, NAME3 FROM NAME_MST WHERE NAMECD1 = 'A047' AND NAME1 = '" + _year + "' AND NAME2 IS NOT NULL AND NAME3 IS NOT NULL ORDER BY NAME3 ";
            final Map<String, Map<String, List<String>>> gradeSubclassListMap = new TreeMap<String, Map<String, List<String>>>();
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String grade = KnjDbUtils.getString(row, "NAME2");
                final String subclasscd = KnjDbUtils.getString(row, "NAME3");
                getMappedList(getMappedMap(gradeSubclassListMap, grade), getClassKey(subclasscd)).add(subclasscd);
            }
            return gradeSubclassListMap;
        }
    }
}
