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

import static servletpack.KNJA.KNJA130CCommon.*;
import static servletpack.KNJZ.detail.KNJ_EditDate.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJA.KNJA130CCommon.AnotherSubclassMst;
import servletpack.KNJA.KNJA130CCommon.ClassMst;
import servletpack.KNJA.KNJA130CCommon.Property;
import servletpack.KNJA.KNJA130CCommon.Staff;
import servletpack.KNJA.KNJA130CCommon.StaffMst;
import servletpack.KNJA.KNJA130CCommon.SubclassMst;
import servletpack.KNJA.KNJA130CCommon.Util;
import servletpack.KNJA.KNJA130CCommon.Z010;
import servletpack.KNJZ.detail.KNJDefineSchool;
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
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.SvfForm.Line;

/**
 * 高校生徒指導要録を印刷します。
 */
public class KNJA130 {
    private static final Log log = LogFactory.getLog(KNJA130.class);

    private static final String CERTIF_KINDCD = "107";

    private static final String _90 = "90";
    private static final String _92 = "92";
    private static final String _94 = "94";
    private static final String _ABROAD = "abroad";

    private static String MARK_FROM_TO = "\uFF5E";

    private static final String CHITEKI1_知的障害 = "1";
    private static final String CHITEKI2_知的障害以外 = "2";

    public static boolean DEBUG = false;

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        svf_out_ex(request, response, new HashMap());
    }

    // KNJA134Hコール
    public void svf_out_ex(final HttpServletRequest request, final HttpServletResponse response, final Map paramMap) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        boolean nonedata = false;
        Param[] paramArray = null;
        try {
            sd.setSvfInit(request, response, svf);

            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            paramArray = getParamArray(request, db2, paramMap);
            for (int i = 0; i < paramArray.length; i++) {

                // 印刷処理
                nonedata = printSvf(request, db2, svf, paramArray[i]) || nonedata;

                paramArray[i].closeForm();
                paramArray[i].psCloseQuietly();
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            sd.closeSvf(svf, nonedata);
            sd.closeDb(db2);
        }
    }

    private Param[] getParamArray(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) {

        KNJServletUtils.debugParam(request, log);

        if ("KNJI050".equals(request.getParameter("PRGID"))) {
            final String[] empty = {};
            final String[] gYear = null == request.getParameter("G_YEAR") ? empty : StringUtils.split(request.getParameter("G_YEAR"), ",");
            final String[] gSemester = null == request.getParameter("G_SEMESTER") ? empty : StringUtils.split(request.getParameter("G_SEMESTER"), ",");
            final String[] schregno = null == request.getParameter("SCHREGNO") ? empty : StringUtils.split(request.getParameter("SCHREGNO"), ",");
            final int minlen = Math.min(gYear.length, Math.min(gSemester.length, schregno.length));
            final Param[] params = new Param[minlen];
            for (int i = 0; i < minlen; i++) {
                params[i] = new Param(request, db2, "KNJI050", gYear[i], gSemester[i], Collections.singletonList(schregno[i]), paramMap);
            }
            return params;
        }
        return new Param[] {getParam(request, db2, paramMap)};
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) {
        final String flg = request.getParameter("PRGID");
        final String year = request.getParameter("YEAR");
        final String gakki = request.getParameter("GAKKI");

        final Param param = new Param(request, db2, flg, year, gakki, null, paramMap);
        param._schregnoList = Student.createSchregnoList(db2, request, param);
        return param;
    }

    private boolean printSvf(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final Map<String, StaffMst> staffMstMap = StaffMst.loadStaffMstEx(db2, null, param._year);
        final List<KNJA130_0> formList = param.getPrintForm(svf, request, db2);

        boolean nonedata = false;
        String beforeSchoolKind = null;
        for (final String schregno : param._schregnoList) {
            log.info(" student = " + schregno);

            Student student = new Student(schregno, beforeSchoolKind, db2, staffMstMap, param);
            Student.setSchregEntGrdHistComebackDat(db2, param, student, staffMstMap);


            final List<PersonalInfo> entGrdHistList = student.getPrintSchregEntGrdHistList(param);
            for (final PersonalInfo personalInfo : entGrdHistList) {
                final List<List<Gakuseki>> pageGakusekiListList = new ArrayList();
                if (param._isPrintPageOrderByYear) {
                    // 表裏対応
                    List current = null;
                    Gakuseki old = null;
                    for (final Gakuseki gakuseki : personalInfo._gakusekiList) {
                        if (null == current || current.size() >= 4 || null != old && old._isDrop) {
                            current = new ArrayList();
                            pageGakusekiListList.add(current);
                        }
                        current.add(gakuseki);
                        old = gakuseki;
                    }
                } else {
                    pageGakusekiListList.add(personalInfo._gakusekiList);
                }
                for (int i = 0; i < pageGakusekiListList.size(); i++) {
                    final List<Gakuseki> pageGakusekiList = pageGakusekiListList.get(i);
                    for (final KNJA130_0 form : formList) {
                        form.setDetail(db2, student, personalInfo, pageGakusekiList);
                        if (form.nonedata) {
                            nonedata = true;
                        }
                    }
                }
            }
            beforeSchoolKind = student._schoolKind;
            student = null;
        }
        return nonedata;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    private static String getSubclasscd(final StudyRec studyrec, final Param param) {
        return studyrec._subclassMst.getKey(param);
    }

    private static String getSubclasscd(final Map<String, String> row , final Param param) {
        if ("1".equals(param._useCurriculumcd)) {
            return KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
        }
        return KnjDbUtils.getString(row, "SUBCLASSCD");
    }

    private static Integer sum(final Collection<Integer> intlist) {
        if (null == intlist || intlist.isEmpty()) {
            return null;
        }
        int sum = 0;
        for (final Integer e : intlist) {
            sum += e.intValue();
        }
        return new Integer(sum);
    }

    private static String setKeta(final String str, final int keta) {
        return StringUtils.defaultString(str) + StringUtils.repeat(" " , keta - KNJ_EditEdit.getMS932ByteLength(str) % keta);
    }

    private static String defstr(final Object s) {
        return null == s ? "" : StringUtils.defaultString(s.toString());
    }

    private static String defstr(final Object s1, final String s2) {
        return null == s1 ? s2 : StringUtils.defaultString(s1.toString(), s2);
    }

    protected static void debugLogCheck(final Param param, final String key, final String c) {
        if (!param.debugOutputMap().containsKey(key)) {
            log.info(c);
            param.debugOutputMap().put(key, c);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static class Student {
        final String _schregno;
        final String _schoolKind;
        final String _title;
        final int _gradeRecNumber;
        final PersonalInfo _personalInfo;
        /** 異動履歴 */
        final List<TransferRec> _transferRecList;
        final Map<String, AttendRec> _attendRecMap;
        final Map<String, HtrainRemark> _htrainRemarkMap;
        final Shoken _shoken;
        /** [学習の記録] 備考を保持する */
        final GakushuBiko _gakushuBiko = new GakushuBiko();

        private List _schregEntGrdHistComebackDatList;

        /** 学科年度データの学校区分 */
        private String _majorYdatSchooldiv;
        private String _schoolName1;

        private Student(
                final String schregno,
                final String beforeSchoolKind,
                final DB2UDB db2,
                final Map<String, StaffMst> staffMstMap,
                final Param param
        ) {
            _schregno = schregno;
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param._year + "' AND GRADE IN (SELECT MAX(GRADE) FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + _schregno + "' AND YEAR = '" + param._year + "') "));
            if ((null == beforeSchoolKind) != (null == _schoolKind) || null != beforeSchoolKind && !beforeSchoolKind.equals(_schoolKind)) {
                param.loadSchool(db2, _schoolKind);
            }
            if ("1".equals(param.property(Property.useGakkaSchoolDiv))) {
                _majorYdatSchooldiv = getMajorYdatSchooldiv(db2, _schregno, param._year);
            }
            _title = isKoumokuGakunen(param) ? "学年" : "年度";

            _personalInfo = PersonalInfo.loadPersonal(db2, this, _schoolKind, null, param, staffMstMap);
            _gradeRecNumber = setGradeRecNumber(param);
            _transferRecList = TransferRec.loadTransferRec(db2, this, param);

            _attendRecMap = AttendRec.loadAttendRec(db2, this, param);
            _htrainRemarkMap = HtrainRemark.loadHtrainRemark(db2, _schregno, param);
            _shoken = Shoken.load(db2, _schregno, param);
            createStudyRecBiko(db2, _schregno, param, _gakushuBiko);
            _gakushuBiko.createStudyRecBikoSubstitution(param);

            _schoolName1 = param._schoolName1;
            if (isGakunenSei(param) && param._isChuKouIkkan) {
                if (!StringUtils.isEmpty(param._certifSchoolSchoolName)) {
                    _schoolName1 = param._certifSchoolSchoolName;
                }
            }
        }

        public static List<String> createSchregnoList(final DB2UDB db2, final HttpServletRequest request, final Param param) {
            final List<String> schregnos = new ArrayList();

            final String _output;
            final String[] _categorySelected;
//            final String[] _selectedHR;

            _output = request.getParameter("OUTPUT");    // 1=個人, 2=クラス
            _categorySelected = request.getParameterValues("category_selected"); // 複数生徒 or 複数年組

//            if ("2".equals(_output)) {
//                _selectedHR = _categorySelected;
//            } else {
//                _selectedHR = request.getParameterValues("GRADE_HR_CLASS");
//            }

            if ("2".equals(_output)) {
                try {
                    final StringBuffer where1 = new StringBuffer();
                    if (param.isTokubetsuShien()) {
                        where1.append("  AND ");
                        where1.append("     EXISTS ( ");
                        where1.append("         SELECT ");
                        where1.append("             'X' ");
                        where1.append("         FROM ");
                        where1.append("             SCHREG_BASE_MST BASE ");
                        where1.append("         INNER JOIN NAME_MST A025 ON A025.NAMECD1 = 'A025' AND A025.NAMECD2 = BASE.HANDICAP AND A025.NAMESPARE3 = '" + param._chiteki + "' ");
                        where1.append("         WHERE BASE.SCHREGNO = T1.SCHREGNO ");
                        where1.append("     ) ");
                    }

                    final StringBuffer stb = new StringBuffer();

                    if (param.isTokubetsuShien() && "2".equals(param._hrClassType)) {
                        stb.append(" SELECT T1.SCHREGNO ");
                        stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
                        stb.append(" WHERE T1.YEAR = '" +  param._year + "' ");
                        stb.append("   AND T1.SEMESTER = '" +  param._gakki + "' ");
                        stb.append("   AND T1.GHR_CD IN " + SQLUtils.whereIn(true,  _categorySelected) + " ");
                        stb.append(where1);
                        stb.append("ORDER BY T1.GHR_CD, T1.GHR_ATTENDNO ");
                    } else {
                        stb.append(" SELECT T1.SCHREGNO ");
                        stb.append(" FROM SCHREG_REGD_DAT T1 ");
                        stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                        stb.append(" WHERE T1.YEAR = '" + param._year + "' ");
                        stb.append("   AND T1.SEMESTER = '" + param._gakki + "' ");
                        if (param.isTokubetsuShien() && "1".equals(param._gakunenKongou)) {
                            stb.append("   AND T2.SCHOOL_KIND || '-' || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                        } else {
                            stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                        }
                        stb.append(where1);
                        stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
                    }

                    schregnos.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO")); // 任意のHR組の学籍番号取得用

                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                    db2.commit();
                }
            } else {
                if (param.isTokubetsuShien()) {
                    for (int i = 0; i < _categorySelected.length; i++) {
                        schregnos.add(StringUtils.split(_categorySelected[i], "-")[1]);
                    }
                } else {
                    schregnos.addAll(Arrays.asList(_categorySelected));
                }
            }
            return schregnos;
        }

        /**
         * 印刷する生徒情報
         */
        private List<PersonalInfo> getPrintSchregEntGrdHistList(final Param param) {
            final List<PersonalInfo> rtn = new ArrayList<PersonalInfo>();
            if (_schregEntGrdHistComebackDatList.size() == 0) {
                _personalInfo._isFirst = true;
                return Collections.singletonList(_personalInfo);
            }
            // 復学が同一年度の場合、復学前、復学後を表示
            // 復学が同一年度ではない場合、復学後のみ表示
            final List<PersonalInfo> personalInfoList = new ArrayList<PersonalInfo>();
            personalInfoList.addAll(_schregEntGrdHistComebackDatList);
            personalInfoList.add(_personalInfo);
            for (final PersonalInfo personalInfo : personalInfoList) {
//                final int begin = personalInfo.getYearBegin();
//                final int end = personalInfo.getYearEnd(param);
//                if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
//                    rtn.add(personalInfo);
//                }
                rtn.add(personalInfo);
            }
            if (rtn.isEmpty()) {
                log.fatal("対象データがない!");
            } else {
                rtn.get(0)._isFirst = true;
            }
            return rtn;
        }

        public static Collection<String> getDropShowYears(final Param param, final PersonalInfo personalInfo) {
            final Collection chkDropShowYears = new HashSet();
//            if (param._isKyoto && isTengakuTaigaku(personalInfo) && personalInfo._dropYears.isEmpty()) {
//                final String grdYear = getTengakuTaigakuNendoMinus1(personalInfo);
//                if (null != grdYear) {
//                    // 「原級留置した場合、留年時の成績は出力されない」が、
//                    // 再履修の成績データを作成する前に転退学した場合は、留年時の成績を出す。
//                    // （原級留置の年次で改ページはする）
//                    for (final Iterator it = groupByGrade(personalInfo._gakusekiList).values().iterator(); it.hasNext();) {
//                        final List gakuList = (List) it.next();
//                        if (gakuList.size() <= 1) { // 留年ではない
//                            continue;
//                        }
//                        final Gakuseki newGaku = (Gakuseki) gakuList.get(0); // 再履修の学籍
//                        if (grdYear.equals(newGaku._year)) {
//                            final Gakuseki oldGaku = (Gakuseki) gakuList.get(1); // 留年時の最新の学籍
//
//                            boolean hasNewGakuYearStudyrec = false;
//                            for (final Iterator its = personalInfo._studyRecList.iterator(); its.hasNext();) {
//                                final StudyRec studyRec = (StudyRec) its.next();
//                                if (null != studyRec._year && studyRec._year.equals(newGaku._year)) {
//                                    hasNewGakuYearStudyrec = true;
//                                }
//                            }
//                            if (!hasNewGakuYearStudyrec) {
//                                // 再履修の年度の成績がない場合、留年時の年度を表示非対象年度リストから除く
//                                chkDropShowYears.add(oldGaku._year);
//                                log.fatal("再履修年度の成績データが0件.留年時の成績を表示対象とする. year = " + oldGaku._year);
//                            }
//                        }
//                    }
//                }
//            }
            return chkDropShowYears;
        }

        private Collection<String> getDropYears(final List<Gakuseki> gakusekiList, final Param param) {
            final Collection<String> dropYears = new HashSet<String>();
            for (final Gakuseki gaku : gakusekiList) {
                if (isGakunenSei(param)) {
                    if (gaku._isDrop) {
                        dropYears.add(gaku._year);
                    }
                }
            }
            return dropYears;
        }

        /**
         * <pre>
         *  修得単位数を科目別・年度別に集計し、各マップに要素を追加します。
         *  ・科目別修得単位数計 Student._studyRecSubclass。
         * <br />
         *  ・年度別修得単位数計 Student._studyRecYear。
         * </pre>
         */
        private static Map<String, StudyRecSubclassTotal> createStudyRecTotal(final List<StudyRec> studyRecList, final Collection<String> dropYears, final Collection<String> checkDropYears, final Collection<String> tengakuYears, final Param param) {

            final Map<String, List<StudyRec>> subclassStudyrecListMap = new TreeMap();
            for (final StudyRec studyrec : studyRecList) {
                String key = getSubclasscd(studyrec, param);
                if (key.startsWith("90")) {
                    key = "90";
                }
                getMappedList(subclassStudyrecListMap, key).add(studyrec);
            }
            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = new TreeMap();
            for (final Map.Entry<String, List<StudyRec>> e : subclassStudyrecListMap.entrySet()) {
                final StudyRecSubclassTotal subclassTotal = new StudyRecSubclassTotal(e.getValue(), dropYears, checkDropYears, tengakuYears);
                if (param._isOutputDebugSeiseki) {
                    log.info(" subclassTotal = " + subclassTotal.toString(StudyRec.yearSet(studyRecList), param));
                }
                studyRecSubclassMap.put(e.getKey(), subclassTotal);
            }
            return studyRecSubclassMap;
        }

        private static List<String> loadAfterGraduatedCourse(final DB2UDB db2, final Param param, final String schregno, final List gakusekiList) {
            final List<String> afterGraduatedCourseTextList = new ArrayList();
            if (null == param._seito || !param._hasAFT_GRAD_COURSE_DAT) {
                return afterGraduatedCourseTextList;
            }
            final TreeSet<String> yearSet = PersonalInfo.gakusekiYearSet(gakusekiList);
            if (yearSet.isEmpty()) {
                return afterGraduatedCourseTextList;
            }
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            try {

                final String psKey = "AFT_GRAD";
                if (null == param._psMap.get(psKey)) {

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
                        + "         AND YEAR BETWEEN ? AND ? "
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
                        + "where "
                        + "     T1.PLANSTAT = '1' "
                        + "order by "
                        + "     T1.YEAR, T1.SCHREGNO "
                        ;
                        param._psMap.put(psKey, db2.prepareStatement(sql));
                }

//                log.debug(" schregno = " + schregno + " sql = " + sql);
                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, minYear, maxYear}));
                if ("0".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 進学
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E017NAME1")) {
                        final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "THINKEXAM"), 50, 10);
                        if (null != token) {
                            afterGraduatedCourseTextList.addAll(Arrays.asList(token));
                        }
                    } else {
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME")));
                        final String faculutyname = "000".equals(KnjDbUtils.getString(row, "FACULTYCD")) || null == KnjDbUtils.getString(row, "FACULTYNAME") ?  "" : KnjDbUtils.getString(row, "FACULTYNAME");
                        final String departmentname = "000".equals(KnjDbUtils.getString(row, "DEPARTMENTCD")) || null == KnjDbUtils.getString(row, "DEPARTMENTNAME") ? "" : KnjDbUtils.getString(row, "DEPARTMENTNAME");
                        afterGraduatedCourseTextList.add(faculutyname + departmentname);
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "CAMPUSFACULTYADDR1"), KnjDbUtils.getString(row, "CAMPUSADDR1")));
                    }
                } else if ("1".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 就職
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E018NAME1")) {
                        final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "JOB_THINK"), 50, 10);
                        if (null != token) {
                            afterGraduatedCourseTextList.addAll(Arrays.asList(token));
                        }
                    } else {
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANY_NAME")));
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANYADDR1")));
                        afterGraduatedCourseTextList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "COMPANYADDR2")));
                    }
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            }
            return afterGraduatedCourseTextList;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiAttendRec(final DB2UDB db2, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final Map<String, AttendRec> attendRecMap, final Param param) {
            final Map<String, Gakuseki> map = new HashMap();
            for (final Gakuseki gakuseki : gakusekiList) {
                map.put(gakuseki._year, gakuseki);
            }

            for (final String year : attendRecMap.keySet()) {
                if (map.containsKey(year)) {
                    continue;
                }
                if (personalInfo.otherYearsContains(gakusekiList, attendRecMap.values(), year)) {
                    continue;
                }
                map.put(year, new Gakuseki(db2, (YearAnnual) attendRecMap.get(year), param));
            }

            final List<Gakuseki> list = new LinkedList(map.values());
            Collections.sort(list, new Gakuseki.GakusekiComparator());
            return list;
        }

        /**
         * @return Gakusekiリスト（様式２裏対応）を戻します。
         */
        private static List<Gakuseki> createGakusekiStudyRec(final DB2UDB db2, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final List<StudyRec> studyRecList, final Param param) {
            final Map<String, Gakuseki> map = new HashMap();
            for (final Gakuseki gakuseki : gakusekiList) {
                map.put(gakuseki._year, gakuseki);
            }

            for (final StudyRec studyrec : studyRecList) {
                if (map.containsKey(studyrec._year)) {
                    continue;
                }
                if (personalInfo.otherYearsContains(gakusekiList, studyRecList, studyrec._year)) {
                    continue;
                }
                final Gakuseki gakuseki = new Gakuseki(db2, studyrec, param);
                map.put(studyrec._year, gakuseki);
            }

            final List<Gakuseki> list = new LinkedList(map.values());
            Collections.sort(list, new Gakuseki.GakusekiComparator());
            return list;
        }

        /**
         * 学習記録備考クラスを作成し、マップに加えます。
         */
        private void createStudyRecBiko(final DB2UDB db2, final String schregno, final Param param, final GakushuBiko gakushuBiko) {
            if (null == param._gakushu) {
                return;
            }
            try {
                final String psKey = "PS_STUDYREC_BIKO";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlStudyrecBiko(param);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, param._year})) {
                    if (KnjDbUtils.getString(row, "REMARK") == null) {
                        continue;
                    }
                    final String key = _90.equals(KnjDbUtils.getString(row, "CLASSCD")) ? _90 : getSubclasscd(row, param);
                    gakushuBiko.putStudyrecBiko(key, KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "REMARK"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        /**
         * @return 学習の記録備考のＳＱＬ文を戻します。
         */
        private String sqlStudyrecBiko(final Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, T1.REMARK");
            stb.append(" FROM ");
            stb.append("     STUDYRECREMARK_DAT T1");
            stb.append(" LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? AND T1.YEAR <= ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, T1.YEAR");
            return stb.toString();
        }

        /**
         * gradeRecNumberを設定します。
         */
        private int setGradeRecNumber(final Param param) {
            if (!isGakunenSei(param)) {
                return _personalInfo._gakusekiList.size();
            }

            int n = 0;
            for (final Gakuseki gaku : _personalInfo._gakusekiList) {
                if (!gaku._isDrop) {
                    n++;
                }
            }
            return n;
        }

        /**
         * 生徒が転学・退学・卒業したか
         * @return 生徒が転学・退学・卒業したならtrue、そうでなければfalse
         */
        public boolean isGrd(final PersonalInfo personalInfo) {
            final boolean isGrd = 1 == personalInfo.grdDivInt() || personalInfo.isTaigaku() || personalInfo.isTengaku();
            return isGrd;
        }

        /**
         * 学年制で生徒が3/31未満に転学した年度
         * @return 学年制で生徒が3/31未満に転学した年度
         */
        public Collection<String> getGakunenSeiTengakuYears(final PersonalInfo personalInfo, final Param param) {
            if (!isGakunenSei(param)) {
                return Collections.emptyList();
            }
            if (personalInfo.isTengaku() && null != personalInfo._grdDate) {
                final Calendar cal = KNJA130CCommon.getCalendarOfDate(personalInfo._grdDate);
                if (cal.get(Calendar.MONTH) == Calendar.MARCH && cal.get(Calendar.DAY_OF_MONTH) == 31) {
                } else {
                    int year = cal.get(Calendar.YEAR);
                    if (cal.get(Calendar.MONTH) <= Calendar.MARCH) {
                        year -= 1;
                    }
                    final List<String> list = new ArrayList();
                    list.add(String.valueOf(year));
                    return list;
                }
            }
            return Collections.emptyList();
        }

        /**
         * 指定生徒・年度の学科年度データの学校区分を得る
         */
        private String getMajorYdatSchooldiv(final DB2UDB db2, final String schregno, final String year) {
            String majorYdatSchooldiv = null;
            try {
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
                //log.debug(" majorYdatSchooldiv = " + stb.toString());
                majorYdatSchooldiv = KnjDbUtils.getString(KnjDbUtils.lastRow(KnjDbUtils.query(db2, stb.toString())), "SCHOOLDIV");
            } catch (Exception e) {
                log.error("setMajorYdatSchooldiv Exception", e);
            }
            // log.fatal(" schoolmst.schooldiv = " + _definecode.schooldiv + ", majorYdatSchoolDiv = " + _majorYdatSchooldiv + " -> "+  getSchooldiv());
            return majorYdatSchooldiv;
        }

        private String getSchooldiv(final Param param) {
            final String schooldiv;
            if ("1".equals(param.property(Property.useGakkaSchoolDiv))) {
                schooldiv = null != _majorYdatSchooldiv ? _majorYdatSchooldiv : param._definecode.schooldiv;
            } else {
                schooldiv = param._definecode.schooldiv;
            }
            return schooldiv;
        }

        /**
         * 学年制か?
         * @return 学年制なら<code>true</code>
         */
        private boolean isGakunenSei(final Param param) {
            return "0".equals(getSchooldiv(param));
        }

        /**
         * 単位制か?
         * @return 単位制なら<code>true</code>
         */
        private boolean isTanniSei(final Param param) {
            return "1".equals(getSchooldiv(param));
        }

        public boolean isKoumokuGakunen(final Param param) {
            Boolean rtn = null;
            if (null != param.property(Property.seitoSidoYorokuKoumokuMei)) {
                if ("1".equals(param.property(Property.seitoSidoYorokuKoumokuMei))) {
                    rtn = Boolean.TRUE;
                } else if ("2".equals(param.property(Property.seitoSidoYorokuKoumokuMei))) {
                    rtn = Boolean.FALSE;
                }
            }
            if (null == rtn) {
                if (isGakunenSei(param)) {
                    rtn = Boolean.TRUE;
                } else if (isTanniSei(param)) {
                    rtn = Boolean.FALSE;
                }
            }
            if (null == rtn) {
                rtn = Boolean.TRUE;
            }
            return rtn.booleanValue();
        }

        private static void setSchregEntGrdHistComebackDat(final DB2UDB db2, final Param param, final Student student, final Map staffMstMap) {
            final Map<String, List<String>> schregComebackDateMap = new HashMap();
            try {
                student._schregEntGrdHistComebackDatList = Collections.emptyList();
                if (!param._hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT) {
                    return;
                }
                final String sql =
                        " SELECT T1.* "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + student._schoolKind + "' "
                        + " ORDER BY COMEBACK_DATE ";
                // log.debug(" comeback sql = " + sql);
                final String pskey = "PS_SCHREG_ENT_GRD_HIST_COMEBACK";
                if (null == param._psMap.get(pskey)) {
                    param._psMap.put(pskey, db2.prepareStatement(sql));
                }
                getMappedList(schregComebackDateMap, student._schregno).addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, param._psMap.get(pskey), new Object[] {student._schregno}), "COMEBACK_DATE"));
            } catch (Exception e) {
                log.error("SQLException", e);
            }
            if (null == schregComebackDateMap.get(student._schregno)) {
                return;
            }
            student._schregEntGrdHistComebackDatList = new ArrayList();
            final List<String> comebackDateList = schregComebackDateMap.get(student._schregno);
            log.debug(" schregno = " + student._schregno + ",  comebackdate = " + comebackDateList);
            for (final String comebackDate : comebackDateList) {
                final PersonalInfo comebackPersonalInfo = PersonalInfo.loadPersonal(db2, student, student._schoolKind, comebackDate, param, staffMstMap);
                student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
            }
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒の学籍履歴クラス>>。
     */
    private static class Gakuseki implements YearAnnual, Comparable<Gakuseki> {

        private static class GakusekiComparator implements Comparator<Gakuseki> {
            public int compare(final Gakuseki g1, final Gakuseki g2) {
                return g1._year.compareTo(g2._year);
            }
        }

        final String _grade;
        final String _gradeCd;
        final String _gakunenSimple;
        final String _gradeName2;
        final String _nendo;
        final String _hrname;
        final String _attendno;
        final String _year;
        final String _hr_class;
        final String _annual;
        final Staff _principal;
        final Staff _principal1Last;
        final Staff _principal1First;
        final Staff _staff1Last;
        final Staff _staff1First;
        final Staff _staff2Last;
        final Staff _staff2First;
        final String _dataflg; // 在籍データから作成したか
        private boolean _isDrop;

        /**
         * コンストラクタ。
         * @param rs
         * @param hmap
         * @param param
         */
        private Gakuseki(
                final DB2UDB db2,
                final String year,
                final String grade,
                final String gradeCd,
                final String gradeName2,
                final String hrclass,
                final String hrname,
                final String attendno,
                final String annual,
                final Staff principal,
                final Staff principal1Last,
                final Staff principal1First,
                final Staff staff1Last,
                final Staff staff1First,
                final Staff staff2Last,
                final Staff staff2First
        ) {
            _year = year;
            _nendo = gengou(db2, Integer.parseInt(year)) + "年度";

            _grade = grade;
            _gradeCd = gradeCd;
            _gradeName2 = gradeName2;

            final String gakunen = !NumberUtils.isDigits(gradeCd) ? " " : String.valueOf(Integer.parseInt(gradeCd));
            _gakunenSimple = gakunen;
            _hr_class = hrclass;
            _hrname = hrname;
            _attendno = attendno;
            _annual = annual;
            _dataflg = KNJA130CCommon.Util.isNyugakumae(_year) ? "2" : "1";
            _principal = principal;
            _principal1Last = principal1Last;
            _principal1First = principal1First;
            _staff1Last = staff1Last;
            _staff1First = staff1First;
            _staff2Last = staff2Last;
            _staff2First = staff2First;
        }

        /**
         * コンストラクタ。
         */
        private Gakuseki(final DB2UDB db2, final YearAnnual ya, final Param param) {
            _year = ya.getYear();
            if (KNJA130CCommon.Util.isNyugakumae(_year)) {
                _nendo = "入学前";
            } else {
                _nendo = gengou(db2, Integer.parseInt(ya.getYear())) + "年度";
            }
            _grade = ya.getAnnual();
            if (null != _grade) {
                if (0 >= Integer.parseInt(_grade)) {
                    _annual = null;
                    _gradeCd = null;
                    _gakunenSimple = null;
                    _gradeName2 = "入学前";
                    _dataflg = "2";
                } else {
                    _annual = ya.getAnnual();
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
            _principal1Last = Staff.Null;
            _principal1First = Staff.Null;
            _staff1Last = Staff.Null;
            _staff1First = Staff.Null;
            _staff2Last = Staff.Null;
            _staff2First = Staff.Null;
        }

        public String getYear() {
            return _year;
        }

        public String getAnnual() {
            return _annual;
        }

        private String[] nendoArray() {
            if (KNJA130CCommon.Util.isNyugakumae(_year) || _nendo == null || _nendo.length() < 4) {
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
         * @return 元号を除いた年度を戻します。
         */
        private String getNendo2() {
            if (KNJA130CCommon.Util.isNyugakumae(_year)) {
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
     * <<生徒情報クラス>>。
     */
    private static class PersonalInfo {
        static final String SOGOTEKI_NA_GAKUSHU_NO_JIKAN = "総合的な学習の時間";
        static final String SOGOTEKI_NA_TANKYU_NO_JIKAN = "総合的な探究の時間";

        final Student _student;
        final String _studentRealName;
        final String _studentName;
        final boolean _isPrintRealName;
        final boolean _isPrintNameAndRealName;
        /** 最も古い履歴の生徒名 */
        final String _studentNameHistFirst;
        final String _annual;
        final String _courseName;
        final String _majorName;
        final String _schKana;
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
        final String _comebackDate;
        private String _addressGrdHeader;

        /** 保護者のかわりに保証人を表示するか */
        private boolean _isPrintGuarantor;

        /** 住所履歴 */
        private List<Address> _addressList;
        /** 保護者住所履歴 */
        private List<Address> _guardianAddressList;
        /** 学習記録データ */
        private List<StudyRec> _studyRecList;

        private String _entYear;
        private String _entDate;
        private String _entReason;
        private String _entSchool;
        private String _entAddr;
        private String _entAddr2;
        private Integer _entDiv;
        private String _entDivName;
        private String _entYearGrade;
        private String _entYearGradeCd;
        private String _grdYear;
        private String _grdDate;
        private String _grdReason;
        private String _grdSchool;
        private String _grdAddr;
        private String _grdAddr2;
        private String _grdNo;
        private Integer _grdDiv;
        private String _grdDivName;
        private String _grdYearGrade;
        private String _grdYearGradeCd;
        private String _curriculumYear;
        private boolean _isFirst;
        private boolean _isFuhakkou;

        List<Gakuseki> _gakusekiList;
        /** 留年した年度 */
        Collection _dropYears;
        /** 進路/就職情報 */
        List<String> _afterGraduatedCourseTextList;
        Map<String, List<KNJA134H_3.ClassRemark>> _gradecdClassRemarkListMap;
        Map<String, String> _chitekiLessonCount;

        /**
         * コンストラクタ。
         */
        private PersonalInfo(
                final Student student,
                final String studentRealName,
                final String studentName,
                final boolean isPrintRealName,
                final boolean isPrintNameAndRealName,
                final String studentNameHistFirst,
                final String annual,
                final String courseName,
                final String majorName,
                final String schKana,
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
                final String comebackDate) {

            _student = student;
            _studentRealName = studentRealName;
            _studentName = studentName;
            _isPrintRealName = isPrintRealName;
            _isPrintNameAndRealName = isPrintNameAndRealName;
            _studentNameHistFirst = studentNameHistFirst;
            _annual = annual;
            _courseName = courseName;
            _majorName = majorName;
            _schKana = schKana;
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
            _comebackDate = comebackDate;
        }

        public boolean otherYearsContains(final List<Gakuseki> gakusekiList, final Collection yearAnnuals, String year) {
            final List otherGakusekiList = new ArrayList(_gakusekiList);
            otherGakusekiList.removeAll(gakusekiList);
            final List otherGakusekiYearList = getYearAnnualYears(otherGakusekiList);
//        	final List yearAnnualsList = getYearAnnualYears(yearAnnuals);
//        	yearAnnualsList.removeAll(allGakusekiList);
//        	otherGakusekiYearList.addAll(yearAnnualsList);
            final boolean contains = otherGakusekiYearList.contains(year);
            if (contains == false) {
                log.info(" gakusekiList " + gakusekiList + " contains " + year + " = " + contains);
            }
            return contains;
        }

        private int grdDivInt() {
            if (null == _grdDiv) {
                return -1;
            }
            return _grdDiv.intValue();
        }

        private boolean isTaigaku() {
            return 2 == grdDivInt();
        }

        private boolean isTengaku() {
            return 3 == grdDivInt();
        }

        private boolean isJoseki() {
            return 6 == grdDivInt();
        }

        private boolean isTenseki() {
            return 7 == grdDivInt();
        }

        public int getYearBegin() {
            return null == _entDate ? 0 : KNJA130CCommon.getNendo(KNJA130CCommon.getCalendarOfDate(_entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _grdDate ? 9999 : KNJA130CCommon.getNendo(KNJA130CCommon.getCalendarOfDate(_grdDate)));
        }

        private static String getBirthday(final DB2UDB db2, final String date, final String birthdayFlg, final Param param) {
            final String birthday;
            if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
                birthday = KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
            } else {
                birthday = KNJ_EditDate.setDateFormat2(h_format_JP(db2, date));
            }
            return birthday;
        }

        private static String getNameForm(final String name, final String realname, final boolean showreal, final boolean showboth) {
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

        public int gakusekiMinYear() {
            int min = Integer.MAX_VALUE;
            for (final String year : gakusekiYearSet(_gakusekiList)) {
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

        /**
         * @param student
         * @return 最終年度を戻します。
         */
        private static String getLastYear(final List<Gakuseki> gakusekiList) {
            for (ListIterator<Gakuseki> it = gakusekiList.listIterator(gakusekiList.size()); it.hasPrevious();) {
                Gakuseki gakuseki = it.previous();
                if (null != gakuseki) {
                    return gakuseki._year;
                }
                break;
            }
            return null;
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

        public static TreeSet<String> gakusekiYearSet(final List<Gakuseki> gakusekiList) {
            final TreeSet set = new TreeSet();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        public static TreeMap<String, Gakuseki> yearGakusekiMap(final List<Gakuseki> gakusekiList) {
            final TreeMap map = new TreeMap();
            for (final Gakuseki g : gakusekiList) {
                map.put(g._year, g);
            }
            return map;
        }

        /**
         * 個人情報クラスを作成ます。
         */
        private static PersonalInfo loadPersonal(final DB2UDB db2, final Student student, final String schoolKind, final String comebackDate, final Param param, final Map<String, StaffMst> staffMstMap) {

            String studentRealName = null;
            String studentName = null;
            boolean isPrintRealName = false;
            boolean isPrintNameAndRealName = false;
            /** 最も古い履歴の生徒名 */
            String studentNameHistFirst = null;

            String annual = null;
            String courseName = null;
            String majorName = null;
            String schKana = null;
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

            try {
                final String psKey = "PS_PERSONAL" + student._schoolKind;
                if (null == param._psMap.get(psKey)) {
                    final String sql = sql_info_reg(param, student._schoolKind);
                    // log.debug(" sql personal = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] { student._schregno, student._schregno}));

                if (!row.isEmpty()) {

                    final String name = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME"));
                    final String nameHistFirst = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME_HIST_FIRST"));

                    isPrintRealName = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME"));
                    isPrintNameAndRealName = "1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG"));
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

                        schKana = getNameForm(nameKana0, realNameKana, isPrintRealName, isPrintNameAndRealName);

                    } else {
                        schKana = nameKana0;
                    }

                    annual = KnjDbUtils.getString(row, "ANNUAL");
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
                    finishDate = setDateFormat(db2, h_format_JP_M(db2, KnjDbUtils.getString(row, "FINISH_DATE")), param._year);
                    if (!param._isDefinecodeSchoolMarkHiro) {
                        installationDiv = KnjDbUtils.getString(row, "INSTALLATION_DIV");
                    }
                    juniorSchoolName = KnjDbUtils.getString(row, "J_NAME");
                    finschoolTypeName = KnjDbUtils.getString(row, "FINSCHOOL_TYPE_NAME");
                }
            } catch (final Exception e) {
                log.error("個人情報クラス作成にてエラー", e);
            }
            final PersonalInfo pi = new PersonalInfo(
                    student,
                    studentRealName,
                    studentName,
                    isPrintRealName,
                    isPrintNameAndRealName,
                    studentNameHistFirst,
                    annual,
                    courseName,
                    majorName,
                    param._z010.in(Z010.tokyoto) ? hiraganaToKatakana(schKana) : schKana,
                    param._z010.in(Z010.tokyoto) ? hiraganaToKatakana(guardKana) : guardKana,
                    guardName,
                    guardNameHistFirst,
                    param._z010.in(Z010.tokyoto) ? hiraganaToKatakana(guarantorKana) : guarantorKana,
                    guarantorName,
                    guarantorNameHistFirst,
                    birthday,
                    birthdayStr,
                    sex,
                    finishDate,
                    installationDiv,
                    juniorSchoolName,
                    finschoolTypeName,
                    comebackDate
            );
            try {
                final String sql_state = sql_state(param, student._schregno, student._schoolKind, comebackDate);

                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql_state));

                if (!row.isEmpty()) {
                    pi._entYear    = KnjDbUtils.getString(row, "ENT_YEAR");
                    pi._entDate    = KnjDbUtils.getString(row, "ENT_DATE");
                    pi._entReason  = KnjDbUtils.getString(row, "ENT_REASON");
                    pi._entSchool  = KnjDbUtils.getString(row, "ENT_SCHOOL");
                    pi._entAddr    = KnjDbUtils.getString(row, "ENT_ADDR");
                    if ("1".equals(param.property(Property.useAddrField2))) {
                        pi._entAddr2 = KnjDbUtils.getString(row, "ENT_ADDR2");
                    }
                    pi._entDiv     = NumberUtils.isDigits(KnjDbUtils.getString(row, "ENT_DIV")) ? Integer.valueOf(KnjDbUtils.getString(row, "ENT_DIV")) : null;
                    pi._entDivName = KnjDbUtils.getString(row, "ENT_DIV_NAME");
                    pi._entYearGrade = KnjDbUtils.getString(row, "ENT_YEAR_GRADE");
                    pi._entYearGradeCd = KnjDbUtils.getString(row, "ENT_YEAR_GRADE_CD");
                    pi._grdYear    = KnjDbUtils.getString(row, "GRD_YEAR");
                    pi._grdDate    = KnjDbUtils.getString(row, "GRD_DATE");
                    pi._grdReason  = KnjDbUtils.getString(row, "GRD_REASON");
                    pi._grdSchool  = KnjDbUtils.getString(row, "GRD_SCHOOL");
                    pi._grdAddr    = KnjDbUtils.getString(row, "GRD_ADDR");
                    if ("1".equals(param.property(Property.useAddrField2))) {
                        pi._grdAddr2 = KnjDbUtils.getString(row, "GRD_ADDR2");
                    }
                    pi._grdNo      = KnjDbUtils.getString(row, "GRD_NO");
                    pi._grdDiv     = NumberUtils.isDigits(KnjDbUtils.getString(row, "GRD_DIV")) ? Integer.valueOf(KnjDbUtils.getString(row, "GRD_DIV")) : null;
                    pi._grdDivName = KnjDbUtils.getString(row, "GRD_DIV_NAME");
                    pi._grdYearGrade = KnjDbUtils.getString(row, "GRD_YEAR_GRADE");
                    pi._grdYearGradeCd = KnjDbUtils.getString(row, "GRD_YEAR_GRADE_CD");
                    pi._curriculumYear = KnjDbUtils.getString(row, "CURRICULUM_YEAR");
                }
            } catch (final Exception e) {
                log.error("個人情報クラス作成にてエラー", e);
            }
            pi._gakusekiList = loadGakuseki(db2, student, staffMstMap, param, schoolKind, pi._grdDate);
            pi._dropYears = student.getDropYears(pi._gakusekiList, param);
            pi._afterGraduatedCourseTextList = Student.loadAfterGraduatedCourse(db2, param, student._schregno, pi._gakusekiList);

            pi._studyRecList = StudyRec.loadStudyRecList(db2, student, pi._gakusekiList, param, student._gakushuBiko._ssc, pi._dropYears);
            pi._addressList = Address.loadAddress(db2, student._schregno, param, schoolKind, Address.SQL_SCHREG);
            pi._isPrintGuarantor = getPrintGuarantor(param, pi);
            pi._addressGrdHeader = "1".equals(param.property(Property.seitoSidoYorokuPrintTitleHogoshaTou)) || Integer.parseInt(param._year) >= 2021 ? "保護者等" : pi._isPrintGuarantor ? "保証人" : "保護者";
            pi._guardianAddressList = Address.loadAddress(db2, student._schregno, param, schoolKind, pi._isPrintGuarantor ? Address.SQL_GUARANTOR : Address.SQL_GUARDIAN);
            if (param.isTokubetsuShien() && CHITEKI1_知的障害.equals(param._chiteki)) {
                pi._gradecdClassRemarkListMap = KNJA134H_3.ClassRemark.loadGradecdClassRemarkListMap(db2, param, student);
                pi._chitekiLessonCount = getChitekiLessonCount(db2, param, student);
            }

            pi._isFuhakkou = isFuhakkou(pi._grdDate, param._seitoSidoYorokuHozonkikan);

            return pi;
        }

        /**
         * 証明書不発行か
         * @param grdDate 生徒の卒業日付
         * @param hozonkikan 発行を許可する卒業経過年数
         * @return 不発行(卒業日付に経過年数を加算した日付をシステム日付が超える)ならtrue、それ以外はfalse
         */
        private static boolean isFuhakkou(final String grdDate, final int hozonkikan) {
            if (null == grdDate || hozonkikan <= 0) {
                //log.debug(" grdDate = " + grdDate + ", elapsedYears = " + elapsedYears);
                return false;
            }

            final Calendar hakkoulimit = Calendar.getInstance();
            hakkoulimit.setTime(java.sql.Date.valueOf(grdDate));
            hakkoulimit.add(Calendar.YEAR, hozonkikan);

            final Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            final boolean isFuhakkou = hakkoulimit.equals(now) || hakkoulimit.before(now);
            if (isFuhakkou) {
                final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                log.info(" hakkouLimit = " + df.format(hakkoulimit.getTime()) + ", now = " + df.format(now.getTime()) + ", isFuhakkou = " + isFuhakkou);
            }
            return isFuhakkou;
        }

        private static Map<String, String> getChitekiLessonCount(final DB2UDB db2, final Param param, final Student student) {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("   GDAT.GRADE_CD, ");
            sql.append("   T1.REMARK2 AS JYUGYOU_JISU ");
            sql.append(" FROM ");
            sql.append("   HTRAINREMARK_DETAIL2_DAT T1 ");
            sql.append("   INNER JOIN (SELECT SCHREGNO, YEAR, MAX(GRADE) AS GRADE ");
            sql.append("               FROM SCHREG_REGD_DAT ");
            sql.append("               GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T2.GRADE ");
            sql.append(" WHERE ");
            sql.append("   T1.HTRAIN_SEQ = '001' ");
            sql.append("   AND T1.SCHREGNO = '" + student._schregno + "' ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql.toString()), "GRADE_CD", "JYUGYOU_JISU");
        }

        private static String hiraganaToKatakana(final String kana0) {
            if (null == kana0) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < kana0.length(); i++) {
                final char c = kana0.charAt(i);
                final char t;
                if (0x3041 <= c && c <= 0x3093) { // ひらがな
                    t = (char) (c + 96);
                } else {
                    t = c;
                }
                stb.append(t);
            }
            return stb.toString();
        }

        public String getSogoSubclassname(final Param param, final TreeMap<String, Gakuseki> yearGakusekiMap) {
            final int tankyuStartYear = 2019;
            boolean isTankyu = false;
            String minYear = null;
            Gakuseki minYearGakuseki = null;
            if (NumberUtils.isDigits(_curriculumYear)) {
                isTankyu = Integer.parseInt(_curriculumYear) >= tankyuStartYear;
            } else {
                yearGakusekiMap.remove("0");
                if (!yearGakusekiMap.isEmpty()) {
                    minYear = yearGakusekiMap.firstKey();
                    minYearGakuseki = yearGakusekiMap.get(minYear);
                }
                if (null != minYearGakuseki) {
                    final int year = NumberUtils.isDigits(minYearGakuseki._year) ? Integer.parseInt(minYearGakuseki._year) : 9999;
                    final int gradeCdInt = NumberUtils.isDigits(minYearGakuseki._gradeCd) ? Integer.parseInt(minYearGakuseki._gradeCd) : 99;
                    if (year == tankyuStartYear     && gradeCdInt <= 1
                     || year == tankyuStartYear + 1 && gradeCdInt <= 2
                     || year == tankyuStartYear + 2 && gradeCdInt <= 3
                     || year >= tankyuStartYear + 3
                            ) {
                        isTankyu = true;
                    }
                }
            }
            if (param._isOutputDebug) {
                log.info(" 探究? " + isTankyu + ", startYear = " + minYear + ", minYearGakuseki = " + minYearGakuseki + ", curriculumYear = " + _curriculumYear);
            }
            return isTankyu ? SOGOTEKI_NA_TANKYU_NO_JIKAN : SOGOTEKI_NA_GAKUSHU_NO_JIKAN;
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Student student, final Param param, final String schoolKind, final String grdDate) {
            final String certifKind = CERTIF_KINDCD;
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
            stb.append(" ), YEAR_SEMESTER AS ( ");
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

            stb.append(" ), MIN_YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MIN(SEMESTER) AS SEMESTER ");
            stb.append("     FROM YEAR_SEMESTER ");
            stb.append("     GROUP BY YEAR ");

            stb.append(" ), REGD AS ( ");
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO ");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.SCHOOL_KIND = '" + schoolKind + "' ");
            stb.append("      WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER = (SELECT SEMESTER FROM  MIN_YEAR_SEMESTER WHERE YEAR = T1.YEAR) ");
            if (student.isGakunenSei(param) && param._isHeisetuKou) {
                stb.append("      AND T1.GRADE not in " + param._gradeInChugaku + " ");
            }
            stb.append(" ), GRD_DATE_YEAR_SEMESTER AS ( ");
            stb.append("      SELECT  T1.SCHREGNO, T3.GRD_DATE, T4.YEAR, T4.SEMESTER ");
            stb.append("      FROM    (SELECT DISTINCT SCHREGNO FROM REGD) T1");
            stb.append("      LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND T3.SCHOOL_KIND = '" + schoolKind + "' ");
            stb.append("          AND T3.GRD_DIV IN ('1', '2', '3') ");
            stb.append("      LEFT JOIN SEMESTER_MST T4 ON T4.SEMESTER <> '9' ");
            stb.append("          AND T3.GRD_DATE BETWEEN T4.SDATE AND T4.EDATE ");
            stb.append(" ), T_TEACHER AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.TR_DIV, ");
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
            stb.append("         T1.FROM_DATE <= VALUE(T2.GRD_DATE, '9999-12-31') ");
            stb.append("     GROUP BY ");
            stb.append("         T1.TR_DIV, T1.STAFFCD, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), T_MINMAX_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         TR_DIV, ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         MAX(FROM_DATE) AS MAX_FROM_DATE, ");
            stb.append("         MIN(FROM_DATE) AS MIN_FROM_DATE ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER ");
            stb.append("     GROUP BY ");
            stb.append("         TR_DIV, YEAR, GRADE, HR_CLASS ");
            stb.append(" ), T_TEACHER_MIN_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.TR_DIV, MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MIN_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MIN_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.TR_DIV = T1.TR_DIV ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.TR_DIV, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), T_TEACHER_MAX_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.TR_DIV, MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MAX_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MAX_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.TR_DIV = T1.TR_DIV ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.TR_DIV, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), PRINCIPAL_HIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR) AS ORDER ");
            stb.append("     FROM ");
            stb.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,REGD T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHOOL_KIND = '" + schoolKind + "' ");
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
            if ("1".equals(param.property(Property.useSchregRegdHdat))) {
                stb.append("         ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,T10.STAFFCD AS STAFFCD1LAST ");
            stb.append("   ,T11.STAFFCD AS STAFFCD1FIRST ");
            stb.append("   ,T10.FROM_DATE AS STAFF1LAST_FROM_DATE, T10.TO_DATE AS STAFF1LAST_TO_DATE ");
            stb.append("   ,T11.FROM_DATE AS STAFF1FIRST_FROM_DATE, T11.TO_DATE AS STAFF1FIRST_TO_DATE ");
            stb.append("   ,T15.STAFFCD AS STAFFCD2LAST ");
            stb.append("   ,T16.STAFFCD AS STAFFCD2FIRST ");
            stb.append("   ,T15.FROM_DATE AS STAFF2LAST_FROM_DATE, T15.TO_DATE AS STAFF2LAST_TO_DATE ");
            stb.append("   ,T16.FROM_DATE AS STAFF2FIRST_FROM_DATE, T16.TO_DATE AS STAFF2FIRST_TO_DATE ");
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
            stb.append("          AND T10A.TR_DIV = '1' AND T10A.GRADE = T1.GRADE AND T10A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T10 ON T10.STAFFCD = T10A.STAFFCD ");
            stb.append("          AND T10.FROM_DATE = T10A.MAX_FROM_DATE AND T10.YEAR = T1.YEAR AND T10.TR_DIV = T10A.TR_DIV AND T10.GRADE = T1.GRADE AND T10.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MIN_FROM_DATE T11A ON T11A.YEAR = T1.YEAR ");
            stb.append("          AND T11A.TR_DIV = '1' AND T11A.GRADE = T1.GRADE AND T11A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T11 ON T11.STAFFCD = T11A.STAFFCD ");
            stb.append("          AND T11.FROM_DATE = T11A.MIN_FROM_DATE AND T11.YEAR = T1.YEAR AND T11.TR_DIV = T11A.TR_DIV AND T11.GRADE = T1.GRADE AND T11.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MAX_FROM_DATE T15A ON T15A.YEAR = T1.YEAR ");
            stb.append("          AND T15A.TR_DIV = '2' AND T15A.GRADE = T1.GRADE AND T15A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T15 ON T15.STAFFCD = T15A.STAFFCD ");
            stb.append("          AND T15.FROM_DATE = T15A.MAX_FROM_DATE AND T15.YEAR = T1.YEAR AND T15.TR_DIV = T15A.TR_DIV AND T15.GRADE = T1.GRADE AND T15.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MIN_FROM_DATE T16A ON T16A.YEAR = T1.YEAR ");
            stb.append("          AND T16A.TR_DIV = '2' AND T16A.GRADE = T1.GRADE AND T16A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T16 ON T16.STAFFCD = T16A.STAFFCD ");
            stb.append("          AND T16.FROM_DATE = T16A.MIN_FROM_DATE AND T16.YEAR = T1.YEAR AND T16.TR_DIV = T16A.TR_DIV AND T16.GRADE = T1.GRADE AND T16.HR_CLASS = T1.HR_CLASS ");
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

        private static String sql_info_reg(final Param param, final String schoolKind) {
            final String q = "?";

            final StringBuffer sql = new StringBuffer();

            sql.append("WITH PERSONAL_HIST AS ( ");
            sql.append("  SELECT ENTGRD.SCHREGNO ");
            sql.append("        , MAX(L1.ISSUEDATE) AS BS_N_ISSUEDATE ");
            sql.append("        , MAX(L2.ISSUEDATE) AS BS_RN_ISSUEDATE ");
            sql.append("        , MAX(L3.ISSUEDATE) AS GD_N_ISSUEDATE ");
            sql.append("        , MAX(L4.ISSUEDATE) AS GD_RN_ISSUEDATE ");
            sql.append("        , MAX(L5.ISSUEDATE) AS GR_N_ISSUEDATE ");
            sql.append("        , MAX(L6.ISSUEDATE) AS GR_RN_ISSUEDATE ");
            sql.append("  FROM SCHREG_ENT_GRD_HIST_DAT ENTGRD ");
            sql.append("  LEFT JOIN SCHREG_BASE_HIST_DAT L1 ON L1.SCHREGNO = ENTGRD.SCHREGNO AND L1.NAME_FLG = '1' ");
            sql.append("        AND (ENTGRD.ENT_DATE IS NULL OR ENTGRD.ENT_DATE BETWEEN L1.ISSUEDATE AND VALUE(L1.EXPIREDATE, '9999-12-31') OR L1.ISSUEDATE >= ENTGRD.ENT_DATE) ");
            sql.append("  LEFT JOIN SCHREG_BASE_HIST_DAT L2 ON L2.SCHREGNO = ENTGRD.SCHREGNO AND L2.REAL_NAME_FLG = '1'");
            sql.append("        AND (ENTGRD.ENT_DATE IS NULL OR ENTGRD.ENT_DATE BETWEEN L2.ISSUEDATE AND VALUE(L2.EXPIREDATE, '9999-12-31') OR L2.ISSUEDATE >= ENTGRD.ENT_DATE) ");
            sql.append("  LEFT JOIN GUARDIAN_HIST_DAT L3    ON L3.SCHREGNO = ENTGRD.SCHREGNO AND L3.GUARD_NAME_FLG = '1' ");
            sql.append("        AND (ENTGRD.ENT_DATE IS NULL OR ENTGRD.ENT_DATE BETWEEN L3.ISSUEDATE AND VALUE(L3.EXPIREDATE, '9999-12-31') OR L3.ISSUEDATE >= ENTGRD.ENT_DATE) ");
            sql.append("  LEFT JOIN GUARDIAN_HIST_DAT L4    ON L4.SCHREGNO = ENTGRD.SCHREGNO AND L4.GUARD_REAL_NAME_FLG = '1' ");
            sql.append("        AND (ENTGRD.ENT_DATE IS NULL OR ENTGRD.ENT_DATE BETWEEN L4.ISSUEDATE AND VALUE(L4.EXPIREDATE, '9999-12-31') OR L4.ISSUEDATE >= ENTGRD.ENT_DATE) ");
            sql.append("  LEFT JOIN GUARANTOR_HIST_DAT L5   ON L5.SCHREGNO = ENTGRD.SCHREGNO AND L5.GUARANTOR_NAME_FLG = '1' ");
            sql.append("        AND (ENTGRD.ENT_DATE IS NULL OR ENTGRD.ENT_DATE BETWEEN L5.ISSUEDATE AND VALUE(L5.EXPIREDATE, '9999-12-31') OR L5.ISSUEDATE >= ENTGRD.ENT_DATE) ");
            sql.append("  LEFT JOIN GUARANTOR_HIST_DAT L6   ON L6.SCHREGNO = ENTGRD.SCHREGNO AND L6.GUARANTOR_REAL_NAME_FLG = '1' ");
            sql.append("        AND (ENTGRD.ENT_DATE IS NULL OR ENTGRD.ENT_DATE BETWEEN L6.ISSUEDATE AND VALUE(L6.EXPIREDATE, '9999-12-31') OR L6.ISSUEDATE >= ENTGRD.ENT_DATE) ");
            sql.append("  WHERE ENTGRD.SCHREGNO = " + q + " ");
            sql.append("    AND ENTGRD.SCHOOL_KIND = '" + schoolKind + "' ");
            sql.append("  GROUP BY ENTGRD.SCHREGNO ");
            sql.append("  ) ");

            sql.append("SELECT ");
            sql.append("  T2.NAME,");
            sql.append("  T2.REAL_NAME,");
            sql.append("  ENTGRD.GRD_DATE, ");
            sql.append("  T14.NAME AS NAME_HIST_FIRST, ");
            sql.append("  T18.REAL_NAME AS REAL_NAME_HIST_FIRST, ");
            sql.append("  T18.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            sql.append("  T2.NAME_KANA,T2.REAL_NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,");
            sql.append("  T21.BIRTHDAY_FLG,");
            sql.append("  T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,");
            // 課程・学科・コース
            sql.append("  T3.COURSENAME,");
            if (param._hasMAJOR_MST_MAJORNAME2) {
                sql.append("VALUE(T4.MAJORNAME2, T4.MAJORNAME) AS MAJORNAME,");
            } else {
                sql.append("T4.MAJORNAME,");
            }
            sql.append("  T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,");
            // 卒業中学情報
            sql.append("  ENTGRD.FINISH_DATE,");
            sql.append("  FIN_S.FINSCHOOL_NAME AS J_NAME,");
            if (!param._isDefinecodeSchoolMarkHiro) {
                sql.append("  NM_MST.NAME1 AS INSTALLATION_DIV,");
            }
            sql.append("  VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            // 保護者情報
            sql.append("  T12.GUARD_NAME, ");
            sql.append("  T12.GUARD_REAL_NAME, ");
            sql.append("  T16.GUARD_NAME AS GUARD_NAME_HIST_FIRST, ");
            sql.append("  T12.GUARD_REAL_KANA, ");
            sql.append("  T12.GUARD_KANA,");
            sql.append("  T20.GUARD_REAL_NAME AS G_R_NAME_WITH_RN_HIST_FIRST, ");
            sql.append("  T20.GUARD_NAME      AS G_NAME_WITH_RN_HIST_FIRST, ");
            sql.append("  VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,");
            sql.append("  T12.GUARD_ADDR1,T12.GUARD_ADDR2,T12.GUARD_ZIPCD,");

            sql.append("  T12.GUARANTOR_NAME, ");
            sql.append("  T12.GUARANTOR_REAL_NAME, ");
            sql.append("  T23.GUARANTOR_NAME AS GUARANTOR_NAME_HIST_FIRST, ");
            sql.append("  T12.GUARANTOR_REAL_KANA, ");
            sql.append("  T12.GUARANTOR_KANA,");
            sql.append("  T25.GUARANTOR_REAL_NAME AS GRT_R_NAME_WITH_RN_HIST_FIRST, ");
            sql.append("  T25.GUARANTOR_NAME      AS GRT_NAME_WITH_RN_HIST_FIRST, ");
            sql.append("  VALUE(T12.GUARANTOR_ADDR1,'') || VALUE(T12.GUARANTOR_ADDR2,'') AS GUARANTOR_ADDR,");
            sql.append("  T12.GUARANTOR_ADDR1,T12.GUARANTOR_ADDR2,T12.GUARANTOR_ZIPCD,");
            sql.append("  (CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("  T11.NAME_OUTPUT_FLG, ");
            sql.append("  (CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("  T26.GUARD_NAME_OUTPUT_FLG, ");
            sql.append("  T1.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("(   SELECT     * ");
            sql.append("FROM       SCHREG_REGD_DAT T1 ");
            sql.append("WHERE      T1.SCHREGNO= " + q + " AND T1.YEAR= '" + param._year + "' ");
            sql.append("AND T1.SEMESTER= '" + param._gakki + "' ");

            sql.append(") T1 ");
            sql.append("LEFT JOIN PERSONAL_HIST PHIST ON PHIST.SCHREGNO = T1.SCHREGNO ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            sql.append("                        AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            // 卒業情報有りの場合
            sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("        AND T10.SCHOOL_KIND = '" + schoolKind + "' ");
            }
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR ");
            sql.append("    AND REGDG.GRADE = T1.GRADE ");
            sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
            sql.append("    AND ENTGRD.SCHOOL_KIND= REGDG.SCHOOL_KIND ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=T2.SEX ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = T2.FINSCHOOLCD ");
            if (!param._isDefinecodeSchoolMarkHiro) {
                sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            }
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
            sql.append("      AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = T2.SCHREGNO AND T11.DIV = '02' ");
            // 保護者情報
            sql.append("LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ");

            // 生徒名履歴情報
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T14 ON T14.SCHREGNO = PHIST.SCHREGNO AND T14.ISSUEDATE = PHIST.BS_N_ISSUEDATE ");

            // 保護者履歴情報
            sql.append("LEFT JOIN GUARDIAN_HIST_DAT T16 ON T16.SCHREGNO = PHIST.SCHREGNO AND T16.ISSUEDATE = PHIST.GD_N_ISSUEDATE ");
            // 生徒名履歴情報
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T18 ON T18.SCHREGNO = PHIST.SCHREGNO AND T18.ISSUEDATE = PHIST.BS_RN_ISSUEDATE ");

            // 保護者履歴情報
            sql.append("LEFT JOIN GUARDIAN_HIST_DAT T20 ON T20.SCHREGNO = PHIST.SCHREGNO AND T20.ISSUEDATE = PHIST.GD_RN_ISSUEDATE ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");
            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = T2.SCHREGNO AND T26.DIV = '02' ");

            // 保証人履歴情報
            sql.append("LEFT JOIN GUARANTOR_HIST_DAT T23 ON T23.SCHREGNO = PHIST.SCHREGNO AND T23.ISSUEDATE = PHIST.GR_N_ISSUEDATE ");
            sql.append("LEFT JOIN GUARANTOR_HIST_DAT T25 ON T25.SCHREGNO = PHIST.SCHREGNO AND T25.ISSUEDATE = PHIST.GR_RN_ISSUEDATE ");

            return sql.toString();
        }

        private static String sql_state(final Param param, final String schregno, final String schoolKind, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();


            sql.append(" WITH YEARS AS (");
            sql.append("      SELECT '0' AS SCHOOLDIV, '" + schregno + "' AS SCHREGNO, T4.YEAR, T4.GRADE, T4_2.GRADE_CD ");
            sql.append("      FROM SCHOOL_MST T2 ");
            sql.append("      LEFT JOIN ( ");
            sql.append("          SELECT YEAR, GRADE FROM V_REGDYEAR_GRADE_DAT WHERE SCHREGNO = '" + schregno + "' ");
            sql.append("      ) T4 ON T4.YEAR = T2.YEAR ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            sql.append("      WHERE T2.SCHOOLDIV = '0' ");
            sql.append("      UNION ALL ");
            sql.append("      SELECT '1' AS SCHOOLDIV, '" + schregno + "' AS SCHREGNO, T5.YEAR, T5.GRADE, T5_2.GRADE_CD ");
            sql.append("      FROM SCHOOL_MST T2 ");
            sql.append("      LEFT JOIN ( ");
            sql.append("          SELECT YEAR, GRADE FROM V_REGDYEAR_UNIT_DAT WHERE SCHREGNO = '" + schregno + "' ");
            sql.append("      ) T5 ON T5.YEAR = T2.YEAR  ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ");
            sql.append("      WHERE T2.SCHOOLDIV = '1' ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("        AND T2.SCHOOL_KIND = '" + schoolKind + "' ");
            }
            sql.append(" ) ");

            sql.append(" , MAIN AS (SELECT ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            if ("1".equals(param.property(Property.useAddrField2))) {
                sql.append("    ENT_ADDR2,");
            }
            sql.append("    ENT_DIV, ");
            sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
            if ("1".equals(param.property(Property.useAddrField2))) {
                sql.append("    GRD_ADDR2,");
            }
            sql.append("    GRD_NO, ");
            sql.append("    GRD_DIV, ");
            sql.append("    T4.NAME1 AS GRD_DIV_NAME, ");
            sql.append("    T1.CURRICULUM_YEAR, ");
            sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
            sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
            sql.append(" FROM ");
            if (null != comebackDate) {
                sql.append("    SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            } else {
                sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
            }
            sql.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ");
            sql.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ");
            sql.append(" WHERE ");
            sql.append("    T1.SCHREGNO = '" + schregno + "' AND T1.SCHOOL_KIND = '" + schoolKind + "' ");
            if (null != comebackDate) {
                sql.append("    AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
            }
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("    MAIN.*, ");
            sql.append("    YE.GRADE AS ENT_YEAR_GRADE, ");
            sql.append("    YE.GRADE_CD AS ENT_YEAR_GRADE_CD, ");
            sql.append("    YG.GRADE AS GRD_YEAR_GRADE, ");
            sql.append("    YG.GRADE_CD AS GRD_YEAR_GRADE_CD ");
            sql.append(" FROM MAIN ");
            sql.append("    LEFT JOIN YEARS YE ON YE.YEAR = MAIN.ENT_YEAR ");
            sql.append("    LEFT JOIN YEARS YG ON YG.YEAR = MAIN.GRD_YEAR ");
            return sql.toString();
        }

        public Address getStudentAddressMax() {
            return _addressList == null || _addressList.isEmpty() ? null : _addressList.get(_addressList.size() - 1);
        }

        private static PreparedStatement getPsGakuseki(final DB2UDB db2, final Student student, final Param param, final String schoolKind, final String grdDate) throws Exception {
            final String sql = sqlSchGradeRec(student, param, schoolKind, grdDate);
            // log.debug(" sql regd = " + sql);
            final PreparedStatement ps = db2.prepareStatement(sql);
            return ps;
        }

        /**
         * 学籍履歴クラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<Gakuseki> loadGakuseki(final DB2UDB db2, final Student student, final Map<String, StaffMst>staffMstMap, final Param param, final String schoolKind, final String grdDate) {
            final List<Gakuseki> gakusekiList = new LinkedList();
            final Map hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
            PreparedStatement ps = null;
            try {
                ps = getPsGakuseki(db2, student, param, schoolKind, grdDate);
                for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String grade = KnjDbUtils.getString(row, "GRADE");
                    final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                    final String gradeName2 = KnjDbUtils.getString(row, "GRADE_NAME2");
                    final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                    String hrname = null;
                    if ("1".equals(param.property(Property.useSchregRegdHdat))) {
                        hrname = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                    } else if ("0".equals(param.property(Property.useSchregRegdHdat))) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrclass, hmap);
                    }

                    if (null == hrname) {
                        hrname = KNJ_EditEdit.Ret_Num_Str(hrclass);
                    }

                    final String attendno = defstr(KnjDbUtils.getInt(row, "ATTENDNO", null), null);
                    final String annual = defstr(KnjDbUtils.getInt(row, "ANNUAL", null), null);
                    final String principalName = KnjDbUtils.getString(row, "PRINCIPALNAME");
                    final String principalStaffcd = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD");
                    final String principalStaffcd1 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD1");
                    final String principalStaffcd2 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD2");
                    final String staffcd1Last = KnjDbUtils.getString(row, "STAFFCD1LAST");
                    final String staffcd1First = KnjDbUtils.getString(row, "STAFFCD1FIRST");
                    final String staffcd2Last = KnjDbUtils.getString(row, "STAFFCD2LAST");
                    final String staffcd2First = KnjDbUtils.getString(row, "STAFFCD2FIRST");
                    final String staff1LastFromDate = KnjDbUtils.getString(row, "STAFF1LAST_FROM_DATE");
                    final String staff1LastToDate = KnjDbUtils.getString(row, "STAFF1LAST_TO_DATE");
                    final String staff1FirstFromDate = KnjDbUtils.getString(row, "STAFF1FIRST_FROM_DATE");
                    final String staff1FirstToDate = KnjDbUtils.getString(row, "STAFF1FIRST_TO_DATE");
                    final String staff2LastFromDate = KnjDbUtils.getString(row, "STAFF2LAST_FROM_DATE");
                    final String staff2LastToDate = KnjDbUtils.getString(row, "STAFF2LAST_TO_DATE");
                    final String staff2FirstFromDate = KnjDbUtils.getString(row, "STAFF2FIRST_FROM_DATE");
                    final String staff2FirstToDate = KnjDbUtils.getString(row, "STAFF2FIRST_TO_DATE");
                    final String principal1LastFromDate = KnjDbUtils.getString(row, "PRINCIPAL1_FROM_DATE");
                    final String principal1LastToDate = KnjDbUtils.getString(row, "PRINCIPAL1_TO_DATE");
                    final String principal1FirstFromDate = KnjDbUtils.getString(row, "PRINCIPAL2_FROM_DATE");
                    final String principal1FirstToDate = KnjDbUtils.getString(row, "PRINCIPAL2_TO_DATE");

                    final Staff principal = new Staff(year, new StaffMst(principalStaffcd, principalName, null, null, null), null, null, null);
                    final Staff principal1Last = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd1), principal1LastFromDate, principal1LastToDate, null);
                    final Staff principal1First = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd2), principal1FirstFromDate, principal1FirstToDate, null);
                    final Staff staff1Last = new Staff(year, StaffMst.get(staffMstMap, staffcd1Last), staff1LastFromDate, staff1LastToDate, null);
                    final Staff staff1First = new Staff(year, StaffMst.get(staffMstMap, staffcd1First), staff1FirstFromDate, staff1FirstToDate, null);
                    final Staff staff2Last = new Staff(year, StaffMst.get(staffMstMap, staffcd2Last), staff2LastFromDate, staff2LastToDate, null);
                    final Staff staff2First = new Staff(year, StaffMst.get(staffMstMap, staffcd2First), staff2FirstFromDate, staff2FirstToDate, null);

                    final Gakuseki gakuseki = new Gakuseki(db2, year, grade, gradeCd, gradeName2, hrclass, hrname, attendno, annual,
                            principal, principal1Last, principal1First, staff1Last, staff1First, staff2Last, staff2First);
                    if (!param.isGdatH(gakuseki._year, gakuseki._grade)) {
                        continue;
                    }
                    gakusekiList.add(gakuseki);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }

            // リストをソートします。
            Collections.sort(gakusekiList);

            // 学年制の場合、留年対応します。
            if (student.isGakunenSei(param)) {
                String grade = null;
                for (ListIterator<Gakuseki> i = gakusekiList.listIterator(gakusekiList.size()); i.hasPrevious();) {
                    final Gakuseki gaku = i.previous();
                    if (null != grade && gaku._grade.equals(grade)) {
                        gaku._isDrop = true;
                    }
                    grade = gaku._grade;
                }
            }
            return gakusekiList;
        }

        private static boolean getPrintGuarantor(final Param param, final PersonalInfo personalInfo) {
            boolean isPrintGuarantor = false;
            if ("1".equals(param._notPrintGuarantor)) {
                return isPrintGuarantor;
            }
            final String entdate = personalInfo._entDate; // 入学日付
            final String birthday = personalInfo._birthday;
            try {
                final BigDecimal diff = diffYear(birthday, entdate);
                final int age = diff.setScale(0, BigDecimal.ROUND_DOWN).intValue();
                // 入学時の年齢が20歳以上なら保護者ではなく保証人を表示
                if (age >= 20) {
                    isPrintGuarantor = true;
                }
                //log.debug(" student age = " + diff + " [year]  isPrintGuarantor? " + isPrintGuarantor);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return isPrintGuarantor;
        }

        private static BigDecimal diffYear(final String date1, final String date2) {
            //log.debug(" diffYear date1 ='" + date1 + "', date2 = '" + date2 + "' ");
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
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<生徒住所履歴クラス>>。
     */
    private static class Address {
        static final int SQL_SCHREG = 0;
        static final int SQL_GUARDIAN = 1;
        static final int SQL_GUARANTOR = 2;

        final int _idx;
        final String _issuedate;
        final String _address1;
        final String _address2;
        final String _zipCode;
        final boolean _isPrintAddr2;

        private Address(final int idx, final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
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

        private static List<Address> getPrintAddressRecList(final List<Address> addressRecList, final int max) {
            final LinkedList rtn = new LinkedList();
            if (addressRecList.isEmpty()) {
                return rtn;
            }
            rtn.add(addressRecList.get(0));
            rtn.addAll(KNJA130CCommon.Util.reverse(KNJA130CCommon.Util.take(max - rtn.size(), KNJA130CCommon.Util.reverse(KNJA130CCommon.Util.drop(1, addressRecList)))));
            return rtn;
        }

        static boolean isSameAddressList(final List<Address> addrListA, final List<Address> addrListB) {
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

        static boolean isSameAddress(final Address addressAi, final Address addressBi) {
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
        private static List<Address> loadAddress(final DB2UDB db2, final String schregno, final Param param, final String schoolKind, final int sqlflg) {
            if (null == param._seito) {
                return Collections.emptyList();
            }
            final List<Address> addressRecList = new LinkedList();
            try {
                final String key = "PS_ADDRESS_" + schoolKind + String.valueOf(sqlflg);
                if (null == param._psMap.get(key)) {
                    final String sqlAddressDat = sqlAddressDat(sqlflg, schoolKind);
                    param._psMap.put(key, db2.prepareStatement(sqlAddressDat));
                }
                int idx = 0;
                for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(key), new Object[] { schregno, param._year })) {
                    final String issuedate = KnjDbUtils.getString(row, "ISSUEDATE");
                    final String address1 = KnjDbUtils.getString(row, "ADDR1");
                    final String address2 = KnjDbUtils.getString(row, "ADDR2");
                    final String zipCode = KnjDbUtils.getString(row, "ZIPCD");
                    final boolean isPrintAddr2 = "1".equals(KnjDbUtils.getString(row, "ADDR_FLG"));
                    final Address addressRec = new Address(idx, issuedate, address1, address2, zipCode, isPrintAddr2);
                    addressRecList.add(addressRec);
                    idx += 1;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return addressRecList;
        }

        /**
         * 住所のSQLを得る
         * @param sqlflg 0:生徒住所, 1:保護者住所, 2:保証人住所
         * @return
         */
        private static String sqlAddressDat(final int sqlflg, final String schoolKind) {

            StringBuffer stb = new StringBuffer();
            if (Address.SQL_SCHREG == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            } else if (Address.SQL_GUARDIAN == sqlflg) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else if (Address.SQL_GUARANTOR == sqlflg) {
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
            stb.append("            WHERE SCHOOL_KIND = '" + schoolKind + "' ");
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
        final String _nameCode2;
        final String _name;
        final String _sYear;
        final String _sDate;
        final String _sDateStr;
        final String _eDate;
        final String _eDateStr;
        final String _reason;
        final String _place;
        final String _address;

        /**
         * コンストラクタ。
         */
        public TransferRec(
                final String nameCode2,
                final String name,
                final String sYear,
                final String sDate,
                final String sDateStr,
                final String eDate,
                final String eDateStr,
                final String reason,
                final String place,
                final String address) {
            _nameCode2 = nameCode2;
            _name = name;
            _sYear = sYear;
            _sDate = sDate;
            _sDateStr = sDateStr;
            _eDate = eDate;
            _eDateStr = eDateStr;
            _reason = reason;
            _place = place;
            _address = address;
        }

        /**
         * 異動履歴クラスを作成し、リストに加えます。
         */
        private static List<TransferRec> loadTransferRec(final DB2UDB db2, final Student student, final Param param) {
            final List<TransferRec> transferRecList = new LinkedList();
            try {
                final String psKey = "PS_TRANSFER" + student._schoolKind;
                if (null == param._psMap.get(psKey)) {
                    param._psMap.put(psKey, db2.prepareStatement(sql_state(param, student._schoolKind)));
                }
                for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {student._schregno, student._schregno, student._schregno})) {
                    final String nameCode2 = KnjDbUtils.getString(row, "NAMECD2");
                    final String name = StringUtils.defaultString(KnjDbUtils.getString(row, "NAME1"));
                    final String sYear = KnjDbUtils.getString(row, "YEAR");
                    final String sDate = KnjDbUtils.getString(row, "SDATE");
                    final String sDateStr = setDateFormat(db2, h_format_JP(db2, sDate), param._year);
                    final String eDate = KnjDbUtils.getString(row, "EDATE");
                    final String eDateStr = setDateFormat(db2, h_format_JP(db2, eDate), param._year);
                    final String reason = "".equals(KnjDbUtils.getString(row, "REASON")) ? null : KnjDbUtils.getString(row, "REASON");
                    final String place = "".equals(KnjDbUtils.getString(row, "PLACE")) ? null : KnjDbUtils.getString(row, "PLACE");
                    final String address = "".equals(KnjDbUtils.getString(row, "ADDR")) ? null : KnjDbUtils.getString(row, "ADDR");

                    final TransferRec transferRec = new TransferRec(
                            nameCode2,
                            name,
                            sYear,
                            sDate,
                            sDateStr,
                            eDate,
                            eDateStr,
                            reason,
                            place,
                            address);
                    transferRecList.add(transferRec);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return transferRecList;
        }

        private static String sql_state(final Param param, final String schoolKind) {

            final StringBuffer sql = new StringBuffer();

            sql.append(" WITH V_REGDYEAR_GRADE ");
            sql.append(" (SCHREGNO, YEAR, SEMESTER, GRADE, ANNUAL, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ) AS ( ");
            sql.append(" SELECT SCHREGNO, YEAR, SEMESTER, GRADE, ANNUAL, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE  ");
            sql.append(" FROM SCHREG_REGD_DAT TBL  ");
            sql.append(" WHERE SCHREGNO = ? ");
            sql.append(" AND (SCHREGNO, YEAR, GRADE, SEMESTER) IN ( ");
            sql.append("           SELECT SCHREGNO, YEAR, GRADE, MAX(SEMESTER)  ");
            sql.append("           FROM SCHREG_REGD_DAT CHK  ");
            sql.append("           WHERE CHK.SCHREGNO = TBL.SCHREGNO AND CHK.GRADE    = TBL.GRADE ");
            sql.append("               AND (SCHREGNO, GRADE, YEAR) IN ( SELECT SCHREGNO, GRADE, MAX(YEAR)  ");
            sql.append("                                                FROM SCHREG_REGD_DAT WK  ");
            sql.append("                                                WHERE WK.SCHREGNO = CHK.SCHREGNO  ");
            sql.append("                                                 AND (WK.SCHREGNO,WK.YEAR,WK.SEMESTER) IN (SELECT WK2.SCHREGNO,WK2.YEAR,MAX(WK2.SEMESTER)  ");
            sql.append("                                                                                           FROM SCHREG_REGD_DAT WK2  ");
            sql.append("                                                                                           GROUP BY WK2.SCHREGNO,WK2.YEAR )  ");
            sql.append("                                                GROUP BY WK.SCHREGNO, WK.GRADE ) ");
            sql.append("           GROUP BY CHK.SCHREGNO, CHK.YEAR, CHK.GRADE ) ");
            sql.append(" ), V_REGDYEAR_UNIT ");
            sql.append(" (SCHREGNO, YEAR, SEMESTER, GRADE, ANNUAL, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ) AS ( ");
            sql.append(" SELECT SCHREGNO, YEAR, SEMESTER, GRADE, ANNUAL, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE  ");
            sql.append(" FROM  SCHREG_REGD_DAT TBL ");
            sql.append(" WHERE SCHREGNO = ? ");
            sql.append(" AND (SCHREGNO, YEAR, SEMESTER) IN ( ");
            sql.append("           SELECT SCHREGNO, YEAR, MAX(CHK.SEMESTER)  ");
            sql.append("           FROM SCHREG_REGD_DAT CHK WHERE CHK.SCHREGNO = TBL.SCHREGNO AND CHK.GRADE    = TBL.GRADE ");
            sql.append("           GROUP BY CHK.SCHREGNO, CHK.YEAR ) ");
            sql.append(" ) ");

            sql.append("      SELECT ");
            sql.append("      T1.YEAR,");
            sql.append("      T1.SDATE,");
            sql.append("      T1.EDATE,");
            sql.append("      T1.REASON,");
            sql.append("      T1.PLACE,");
            sql.append("      T1.ADDR,");
            if ("1".equals(param.property(Property.useAddrField2))) {
                sql.append("      T1.ADDR2,");
            }
            sql.append("      T1.NAMECD2,");
            sql.append("      T3.NAME1,");
            sql.append("      CASE T2.SCHOOLDIV WHEN '0' THEN T4.GRADE ELSE T5.GRADE END AS GRADE, ");
            sql.append("      CASE T2.SCHOOLDIV WHEN '0' THEN T4_2.GRADE_CD ELSE T5_2.GRADE_CD END AS GRADE_CD ");
            sql.append("  FROM ");
            sql.append("      (");
            sql.append("          SELECT ");
            sql.append("              SCHREGNO,");
            sql.append("              FISCALYEAR(TRANSFER_SDATE) AS YEAR,");
            sql.append("              TRANSFER_SDATE AS SDATE,");
            sql.append("              TRANSFER_EDATE AS EDATE,");
            sql.append("              TRANSFERREASON AS REASON,");
            sql.append("              TRANSFERPLACE AS PLACE,");
            sql.append("              TRANSFERADDR AS ADDR,");
            if ("1".equals(param.property(Property.useAddrField2))) {
                sql.append("              CAST(NULL AS VARCHAR(1)) AS ADDR2,");
            }
            sql.append("              TRANSFERCD AS NAMECD2 ");
            sql.append("          FROM ");
            sql.append("              SCHREG_TRANSFER_DAT ");
            sql.append("          WHERE ");
            sql.append("              SCHREGNO = ? ");
            sql.append("      )T1 ");
            sql.append("      INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("        AND T2.SCHOOL_KIND = '" + schoolKind + "' ");
            }
            sql.append("      INNER JOIN NAME_MST T3 ON T3.NAMECD1 = 'A004' AND T3.NAMECD2 = T1.NAMECD2 ");
            sql.append("      LEFT JOIN(");
            sql.append("          SELECT '0' AS SCHOOLDIV, SCHREGNO, YEAR, GRADE FROM V_REGDYEAR_GRADE ");
            sql.append("      )T4 ON T4.YEAR=T2.YEAR AND T4.SCHOOLDIV=T2.SCHOOLDIV AND T4.SCHREGNO = T1.SCHREGNO ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            sql.append("      LEFT JOIN(");
            sql.append("          SELECT '1' AS SCHOOLDIV, SCHREGNO, YEAR, GRADE FROM V_REGDYEAR_UNIT ");
            sql.append("      )T5 ON T5.YEAR=T2.YEAR AND T5.SCHOOLDIV=T2.SCHOOLDIV AND T5.SCHREGNO = T1.SCHREGNO ");
            sql.append("      LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ");
            sql.append("  WHERE ");
            sql.append("      T1.YEAR <= '" + param._year + "' ");
            sql.append("  ORDER BY ");
            sql.append("      NAMECD2,SDATE");

            return sql.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static interface YearAnnual {
        String getYear();
        String getAnnual();
    }
    private static List<String> getYearAnnualYears(final Collection<YearAnnual> yearAnnuals) {
        final List years = new ArrayList();
        for (final YearAnnual ya : yearAnnuals) {
            final String year = ya.getYear();
            if (null != year) {
                years.add(year);
            }
        }
        Collections.sort(years);
        return years;
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class AttendRec implements YearAnnual, Comparable<AttendRec> {
        final String _year;
        final String _annual;
        final String _attend_1; // 授業日数
        final String _suspend; //出停
        final String _mourning; // 忌引
        final String _abroad; // 留学
        final String _requirepresent; // 要出席
        final String _attend_6; // 欠席
        final String _present; // 出席

        /**
         * コンストラクタ。
         */
        private AttendRec(final String year, final String annual,
                final String attend_1,
                final String suspend,
                final String mourning,
                final String abroad,
                final String requirepresent,
                final String attend_6,
                final String present
                ) {
            _year = year;
            _annual = annual;
            _attend_1 = attend_1;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _requirepresent = requirepresent;
            _attend_6 = attend_6;
            _present = present;
        }

        public String getYear() {
            return _year;
        }

        public String getAnnual() {
            return _annual;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final AttendRec that) {
            int rtn;
            rtn = _year.compareTo(that._year);
            return rtn;
        }

        /**
         * 出欠記録クラスを作成し、マップに加えます。
         * @param db2
         */
        private static Map<String, AttendRec> loadAttendRec(final DB2UDB db2, final Student student, final Param param) {
            if (null == param._shukketsu && null == param._katsudo) {
                return Collections.emptyMap();
            }
            final Map attendRecMap = new HashMap();
            try {
                final boolean notGradeInChugaku = student.isGakunenSei(param) && param._isHeisetuKou;
                final String psKey = "PS_ATTENDREC" + notGradeInChugaku + student._schoolKind;
                if (null == param._psMap.get(psKey)) {
                    param._psMap.put(psKey, db2.prepareStatement(sqlAttendRec(param, notGradeInChugaku, student._schoolKind)));
                }

                for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {student._schregno})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    attendRecMap.put(year, new AttendRec(year, KnjDbUtils.getString(row, "ANNUAL"),
                            KnjDbUtils.getString(row, "ATTEND_1"),
                            KnjDbUtils.getString(row, "SUSPEND"),
                            KnjDbUtils.getString(row, "MOURNING"),
                            KnjDbUtils.getString(row, "ABROAD"),
                            KnjDbUtils.getString(row, "REQUIREPRESENT"),
                            KnjDbUtils.getString(row, "ATTEND_6"),
                            KnjDbUtils.getString(row, "PRESENT")
                            ));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return attendRecMap;
        }

        /**
         * @return 出欠の記録のＳＱＬ文を戻します。
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String sqlAttendRec(final Param param, final boolean notGradeInChugaku, final String schoolKind) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  T1.YEAR, ANNUAL");
            stb.append("       , VALUE(CLASSDAYS,0) AS CLASSDAYS"); // 授業日数
            stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._isDefinecodeSchoolMarkK) {
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
            stb.append("       WHERE  SCHREGNO = ? AND YEAR <= '" + param._year + "' ");
            if (notGradeInChugaku) {
                stb.append("      AND ANNUAL not in " + param._gradeInChugaku + " ");
            }
            if ("1".equals(param.property(Property.seitoSidoYorokuNotPrintAnotherAttendrec))) {
                stb.append("      AND SCHOOLCD <> '1' ");
            }
            stb.append("       GROUP BY YEAR, ANNUAL");
            stb.append("     )T1 ");
            stb.append("     LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("        AND S1.SCHOOL_KIND = '" + schoolKind + "' ");
            }
            return stb.toString();
        }
    }

    // 留年した年度の処理フラグ 1:留年時の有効フラグをチェック。それ以外は0と同じ
    enum CheckDropYearsFlg {
        _CHECK_ENABLE_FLG,
        _ALL,
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRec implements YearAnnual {

        private static final int KIND_CREDIT = 0;
        private static final int KIND_COMP_CREDIT = 1;
        private static final int KIND_CREDIT_MSTCREDIT = 2;

        private static final int KIND_SOGO90 = 10;
        private static final int KIND_SYOKEI = 11;
        private static final int KIND_ABROAD = 12;
        private static final int KIND_TOTAL = 13;
        private static final int KIND_SOGO94 = 14;
        private static final int KIND_SOGO92 = 15;

        private static class StudyRecComparator implements Comparator<StudyRec> {
            final Param _param;
            public StudyRecComparator(final Param param) {
                _param = param;
            }
            public int compare(final StudyRec t1, final StudyRec t2) {
                if (_param._d065Name1List.contains(t1.getKeySubclasscd(_param)) && _param._d065Name1List.contains(t2.getKeySubclasscd(_param))) {
                } else if (_param._d065Name1List.contains(t1.getKeySubclasscd(_param))) {
                    return 1;
                } else if (_param._d065Name1List.contains(t2.getKeySubclasscd(_param))) {
                    return -1;
                }
                int rtn;
                rtn = ClassMst.compareOrder(_param, t1._classMst, t2._classMst);
                if (0 != rtn) {
                    return rtn;
                }
                rtn = SubclassMst.compareOrder(_param, t1._subclassMst, t2._subclassMst);
                if (0 != rtn) {
                    return rtn;
                }
                rtn = t1._year.compareTo(t2._year);
                return rtn;
            }
        }

        private static List<String> yearSet(final List<StudyRec> studyRecList) {
            final List<String> yearSet = new ArrayList<String>();
            for (final StudyRec sr : studyRecList) {
                if (null != sr._year) {
                    yearSet.add(sr._year);
                }
            }
            return yearSet;
        }

        final String _year;
        final String _annual;
        final ClassMst _classMst;
        final SubclassMst _subclassMst;
        private Integer _grades;
        private Integer _credit;
        private Integer _compCredit;
        final String _validFlg;
        boolean _isDropped;

        /**
         * コンストラクタ。
         */
        private StudyRec(final String year, final String annual, final ClassMst classMst, final SubclassMst subclassMst,
                final Integer credit, final Integer compCredit,
                final BigDecimal grades,
                final String validFlg) {
            _year = year;
            _annual = annual;
            _classMst = classMst;
            _subclassMst = subclassMst;
            _credit = credit;
            _compCredit = compCredit;
            if (null != grades) {
                _grades = new Integer((int) ((float) Math.round(grades.floatValue())));
            }
            _validFlg = validFlg;
        }

        /**
         * コンストラクタ。留学。
         */
        private StudyRec(final String year, final String annual, final Integer credit, final boolean abroad) {
            this(year, annual, ClassMst.ABROAD, SubclassMst.ABROAD, credit, new Integer(0), null, null);
        }

        public String getYear() {
            return _year;
        }

        public String getAnnual() {
            return _annual;
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        public boolean isRisyuNomi(final Param param) {
//            if (param._isKyoto) {
//                // 修得単位数が0 かつ 履修単位数が1以上 かつ 評定が1
//                return 0 == intVal(_credit, -1) && 1 <= intVal(_compCredit, -1) && 1 == intVal(_grades, -1);
//            } else {
                // (修得単位数がnullもしくは0) かつ 履修単位数が1以上
                return 0 == intVal(_credit, 0) && 1 <= intVal(_compCredit, -1);
//            }
        }

        /**
         * 未履修か
         * @return 未履修ならtrue
         */
        public boolean isMirisyu(final Param param) {
            // 修得単位数が0 かつ 履修単位数が0 かつ 評定がnull
            return 0 == intVal(_credit, -1) && 0 == intVal(_compCredit, -1) && -1 == intVal(_grades, -1);
        }

        private static int intVal(final Number n, final int def) {
            return null == n ? def : n.intValue();
        }

        private String getKeySubclasscdForTotal(final Param param) {
            final String keySubclasscd = getKeySubclasscd(param);
            if (keySubclasscd.startsWith(_90)) {
                return _90;
            }
            return keySubclasscd;
        }

        private String getKeySubclasscd(final Param param) {
            return _subclassMst.getKey(param);
        }

        public Integer creditForTotal(final int kind) {
            if (KIND_COMP_CREDIT == kind) {
                return _compCredit;
//            } else if (KIND_CREDIT_MSTCREDIT == kind) {
//                return _creditMstCredits;
            } else { // KIND_CREDIT == kind
                return _credit;
            }
        }

        public boolean kindForTotal(final int kind, final Param param) {
            if (KIND_SOGO90 == kind) {
                if (_ABROAD.equals(_classMst._classname)) {
                    return false;
                }
                if (_90.equals(_classMst._classcd)) {
                    return true;
                }
                return false;
            } else if (KIND_SYOKEI == kind) {
                if (_ABROAD.equals(_classMst._classname)) {
                    return false;
                }
                if (param.isTokubetsuShien()) {
                    if (_92.equals(_classMst._classcd)) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else if (KIND_ABROAD == kind) {
                if (_ABROAD.equals(_classMst._classname)) {
                    return true;
                }
                return false;
            } else if (KIND_TOTAL == kind) {
                return true;
            } else if (KIND_SOGO94 == kind) {
                if (_ABROAD.equals(_classMst._classname)) {
                    return false;
                }
                if (param._z010.in(Z010.hirogaku)) {
                    if (_94.equals(_classMst._classcd)) {
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            } else if (KIND_SOGO92 == kind) {
                if (_ABROAD.equals(_classMst._classname)) {
                    return false;
                }
                if (param.isTokubetsuShien()) {
                    if (_92.equals(_classMst._classcd)) {
                        return true;
                    }
                    return false;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        public String toString() {
            return "Studyrec(" + _subclassMst + ":year=" + _year + ":annual=" + _annual + ":credit=" + _credit + (_isDropped ? ":dropped" + (null != _validFlg ? ",validFlg = " + _validFlg : "") : "") + ")";
        }


        /**
         * 学習記録データクラスを作成し、リストに加えます。
         * @param db2
         */
        private static List<StudyRec> loadStudyRecList(final DB2UDB db2, final Student student, final List<Gakuseki> gakusekiList, final Param param, final StudyrecSubstitutionContainer ssc, final Collection<String> dropYears) {
            if (null == param._tani && null == param._gakushu && null == param._katsudo) {
                return Collections.emptyList();
            }
            // 学年制の場合、留年対応します。
            final List<StudyRec> studyRecList = new LinkedList<StudyRec>();
            final TreeMap yearAnnualMap = new TreeMap(); // 在籍データの年度と年次のマップ
            for (final Gakuseki gaku : gakusekiList) {
                if (null != gaku._annual) {
                    yearAnnualMap.put(gaku._year, gaku._annual);
                }
            }

            studyRecList.addAll(createAbroadStudyrec(db2, student, param, yearAnnualMap));

            studyRecList.addAll(createStudyrec(db2, student, param, dropYears));

            if (param._SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount > 0) {
                // 全部/一部代替科目取得
                studyRecList.addAll(createStudyrecSubstitution(db2, student, param, ssc));
            }

            // リストをソートします。
            Collections.sort(studyRecList, new StudyRec.StudyRecComparator(param));
            if (param._isOutputDebugSeiseki) {
                for (final StudyRec sr : studyRecList) {
                    log.info(" studyrec subclasscd = " + sr._subclassMst + " credit = " + sr._credit + ", hyotei = " + sr._grades);
                }
            }
            return studyRecList;
        }

        /**
         * 留年した年度を考慮した合計用のStudyRecのリスト
         * @param studyRecList 対象のStudyRecのリスト
         * @param dropYears 留年した年度
         * @param tengakuYears
         * @param checkDropYearsFlg 留年した年度の処理フラグ 0:留年した年度を除く 1:留年時の有効フラグをチェック。それ以外は0と同じ 2:全て
         * @return 合計用のStudyRecのリスト
         */
        private static List<StudyRec> getTargetStudyRecList(final List<StudyRec> studyRecList, final Collection<String> dropYears, final Collection<String> checkDropYears, final Collection<String> tengakuYears, final CheckDropYearsFlg checkDropYearsFlg, final Param param) {
            final List<StudyRec> notDropped = new ArrayList();
            if (checkDropYearsFlg == CheckDropYearsFlg._ALL) {
                notDropped.addAll(studyRecList);
                return notDropped;
            }
            final List<StudyRec> validFlgOnList = new ArrayList<StudyRec>(); // 原級留置した年度で有効フラグが設定されているStudyRecのリスト
            for (final StudyRec sr : studyRecList) {
                if (tengakuYears.contains(sr._year)) {
                    continue;
                }
                if (dropYears.contains(sr._year) && null != sr._validFlg && (null != checkDropYears && checkDropYears.contains(sr._year))) {
                    validFlgOnList.add(sr);
                } else if (!dropYears.contains(sr._year)) {
                    notDropped.add(sr);
                }
            }
            if (checkDropYearsFlg == CheckDropYearsFlg._CHECK_ENABLE_FLG && validFlgOnList.size() > 0) { // 原級留置した年度で有効フラグが設定されているStudyRecがあればそちらを返す
                final List<StudyRec> rtn = new ArrayList();
                rtn.addAll(validFlgOnList);
                for (final Iterator<StudyRec> nit = notDropped.iterator(); nit.hasNext();) {
                    final StudyRec ndsr = nit.next();
                    boolean hasSameSubclassGrade = false;
                    for (final StudyRec vlsr : validFlgOnList) {
                        if (ndsr.getKeySubclasscdForTotal(param).equals(vlsr.getKeySubclasscdForTotal(param)) && ndsr._annual.equals(vlsr._annual)) {
                            hasSameSubclassGrade = true;
                            break;
                        }
                    }
                    if (hasSameSubclassGrade) {
                        nit.remove();
                    }
                }
                rtn.addAll(notDropped);
                return rtn;
            }
            return notDropped;
        }

        private static List<StudyRec> createStudyrecSubstitution(final DB2UDB db2, final Student student, final Param param, final StudyrecSubstitutionContainer ssc) {
            final List<StudyRec> studyRecList = new ArrayList();

            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map studyrecSubstitution = ssc.getStudyrecSubstitution(substitutionTypeFlg);

                try {
                    final String sql = sqlReplaceSubClassSubstitution(student, param);
                    for (final Map<String, String> row : KnjDbUtils.query(db2, sql, new Object[] {student._schregno, substitutionTypeFlg})) {
                        studyRecList.add(createReplacedStudyRec(row, studyrecSubstitution, param));
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }
            return studyRecList;
        }

        private static List<StudyRec> createStudyrec(final DB2UDB db2, final Student student, final Param param, final Collection dropYears) {
            final List<StudyRec> studyRecList = new ArrayList();
            try {
                final String sql = sqlStudyrec(student, param._year, param);
                if (param._isOutputDebugQuery) {
                    log.info(" studyrec sql = " + sql);
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql, new Object[] {student._schregno})) {
                    final String schoolCd = KnjDbUtils.getString(row, "SCHOOLCD");
                    final String schoolKind;
                    final String curriculumCd;
                    if ("1".equals(param._useCurriculumcd)) {
                        schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                        curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                    } else {
                        schoolKind = null;
                        curriculumCd = null;
                    }
                    final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                    final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");

                    final String classMstKey = ClassMst.key(param, classcd, schoolKind);
                    ClassMst classMst = ClassMst.get(param, param._classMstMap, classMstKey);
                    if ("1".equals(schoolCd)) {
                        classMst = AnotherClassMst.getAnother(param, param._anotherClassMstMap, classMstKey);
                        if (classMst == AnotherClassMst.Null) {
                            classMst = ClassMst.get(param, param._classMstMap, classMstKey);
                        }
                    } else {
                        classMst = ClassMst.get(param, param._classMstMap, classMstKey);
                    }
                    if (null != classname && !defstr(classMst._classname).equals(classname)) {
                        classMst = classMst.setClassname(classname);
                    }

                    SubclassMst subclassMst;
                    final String subclassMstKey = SubclassMst.key(param, classcd, schoolKind, curriculumCd, subclasscd);
                    if ("1".equals(schoolCd)) {
                        subclassMst = AnotherSubclassMst.getAnother(param, param._anotherSubclassMstMap, subclassMstKey);
                        if (subclassMst == AnotherSubclassMst.Null) {
                            subclassMst = SubclassMst.get(param, param._subclassMstMap, subclassMstKey);
                        }
                    } else {
                        subclassMst = SubclassMst.get(param, param._subclassMstMap, subclassMstKey);
                    }

                    if (SubclassMst.Null == subclassMst) {
                        subclassMst = new SubclassMst(classcd, schoolKind, curriculumCd, subclasscd, subclassname, null, SHOWORDER_DEFAULT , null);
                    }
                    if (null != subclassname && !defstr(subclassMst.subclassname()).equals(defstr(subclassname))) {
                        subclassMst = subclassMst.setSubclassordername(subclassname);
                    }

//                    final String specialdiv = param.useSpecialDiv() ? KnjDbUtils.getString(row, "SPECIALDIV") : "0"; // 東京都は専門区分参照
                    final StudyRec studyRec = new StudyRec(KnjDbUtils.getString(row, "YEAR"), KnjDbUtils.getString(row, "ANNUAL"),
                            classMst, subclassMst,
                            KnjDbUtils.getInt(row, "CREDIT", null), KnjDbUtils.getInt(row, "COMP_CREDIT", null),
                            KnjDbUtils.getBigDecimal(row, "GRADES", null),
//                            KnjDbUtils.getInt(row, "SHOWORDERCLASS", null), KnjDbUtils.getInt(row, "SHOWORDERSUBCLASS", null),
                            KnjDbUtils.getString(row, "VALID_FLG"));
//                    if (_year0.equals(studyRec._year) && !param._seitoSidoYorokuZaisekiMae) {
//                        continue;
//                    }
                    studyRec._isDropped = (dropYears.contains(KnjDbUtils.getString(row, "YEAR")));
                    studyRecList.add(studyRec);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return studyRecList;
        }


        private static List<StudyRec> createAbroadStudyrec(final DB2UDB db2, final Student student, final Param param, final TreeMap<String, String> yearAnnualMap) {
            final List<StudyRec> studyRecList = new ArrayList();
            try {
                final String sql = sqlAbroadCredit(student, param._year, param);
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql, new Object[] {student._schregno})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String annual;
                    if (null != yearAnnualMap.get(year)) {
                        annual = yearAnnualMap.get(year);
                    } else { // 在籍データの範囲外の留学
                        if ("1".equals(param.property(Property.seitoSidoYorokuZaisekiMae))) {
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
//                    log.debug(" abroad record = " + studyRec);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return studyRecList;
        }

        /** 代替科目作成 */
        private static StudyRec createReplacedStudyRec(final Map<String, String> row, final Map<String, StudyRecSubstitution> studyrecSubstitutionMap, final Param param) {

            try {
                final String year = KnjDbUtils.getString(row, "YEAR");
                final String annual = KnjDbUtils.getString(row, "ANNUAL");
                final String substitutionClasscd = KnjDbUtils.getString(row, "SUBSTITUTION_CLASSCD");           // 代替先科目教科コード
//                final String substitutionClassName = KnjDbUtils.getString(row, "SUBSTITUTION_CLASSNAME");       // 代替先科目教科名
                final String substitutionSubclasscd = KnjDbUtils.getString(row, "SUBSTITUTION_SUBCLASSCD");     // 代替先科目コード
//                final String substitutionSubClassName = KnjDbUtils.getString(row, "SUBSTITUTION_SUBCLASSNAME"); // 代替先科目名
                final Integer credit = null;
                final BigDecimal grades = null;
//                final Integer showorderClass = Integer.valueOf(KnjDbUtils.getString(row, "SHOWORDERCLASS"));
//                final Integer showorderSubClass = Integer.valueOf(KnjDbUtils.getString(row, "SHOWORDERSUBCLASS"));
//                final String specialDiv = KnjDbUtils.getString(row, "SPECIALDIV");
                final String substitutionSchoolKind;
                final String substitutionCurriculumCd;
                final String mapKey;
                if ("1".equals(param._useCurriculumcd)) {
                    substitutionSchoolKind = KnjDbUtils.getString(row, "SUBSTITUTION_SCHOOL_KIND");
                    substitutionCurriculumCd = KnjDbUtils.getString(row, "SUBSTITUTION_CURRICULUM_CD");
                    mapKey = substitutionClasscd + "-" + substitutionSchoolKind + "-" + substitutionCurriculumCd + "-" + substitutionSubclasscd;
                } else {
                    substitutionSchoolKind = null;
                    substitutionCurriculumCd = null;
                    mapKey = substitutionSubclasscd;
                }

                final ClassMst substClassMst = ClassMst.get(param, param._classMstMap, ClassMst.key(param, substitutionClasscd, substitutionSchoolKind));
                final SubclassMst substSubclassMst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, substitutionClasscd, substitutionSchoolKind, substitutionCurriculumCd, substitutionSubclasscd));
                if (SubclassMst.Null == substSubclassMst) {
                    return null;
                }

                final StudyRec replacedStudyRec = new StudyRec(
                        year, annual, substClassMst, substSubclassMst,
                        credit, null, grades, null);

                final String attendSubclassCd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"); // 代替元科目コード
                final String attendClassCd;
                final String attendSchoolKind;
                final String attendCurriculumCd;
                if ("1".equals(param._useCurriculumcd)) {
                    attendClassCd = KnjDbUtils.getString(row, "ATTEND_CLASSCD");
                    attendSchoolKind = KnjDbUtils.getString(row, "ATTEND_SCHOOL_KIND");
                    attendCurriculumCd = KnjDbUtils.getString(row, "ATTEND_CURRICULUM_CD");
                } else {
                    attendClassCd = null;
                    attendSchoolKind = null;
                    attendCurriculumCd = null;
                }
//                final String attendClassName = KnjDbUtils.getString(row, "ATTEND_CLASSNAME"); // 代替元教科名称
//                final String attendSubclassName = KnjDbUtils.getString(row, "ATTEND_SUBCLASSNAME"); // 代替元科目名称
                final String substitutionCredit = KnjDbUtils.getString(row, "SUBSTITUTION_CREDIT") == null ? " " : KnjDbUtils.getString(row, "SUBSTITUTION_CREDIT"); // 代替先単位


                final ClassMst attendClassMst = ClassMst.get(param, param._classMstMap, ClassMst.key(param, attendClassCd, attendSchoolKind));
                final SubclassMst attendSubclassMst = SubclassMst.get(param, param._subclassMstMap, SubclassMst.key(param, attendClassCd, attendSchoolKind, attendCurriculumCd, attendSubclassCd));
                if (null == studyrecSubstitutionMap.get(mapKey)) { // すでに同一の代替元科目がある場合
                    final StudyRecSubstitution studyRecSubstitution = new StudyRecSubstitution(
                            null, annual, substClassMst, substSubclassMst,
                            credit, null, grades);
                    studyrecSubstitutionMap.put(mapKey, studyRecSubstitution);
                }
                final StudyRecSubstitution studyRecSubstitution = studyrecSubstitutionMap.get(mapKey);
                studyRecSubstitution.addAttendSubclass(year, attendClassMst, attendSubclassMst, substitutionCredit);

                return replacedStudyRec;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private static String sqlReplaceSubClassSubstitution(final Student student, final Param param) {
            StringBuffer stb = new StringBuffer();
            if (param._z010.in(Z010.KINDAI)) {
                log.debug("String pre_sql_Replace() ");
            }
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST('" + param._year + "' AS VARCHAR(4)))) ");
            stb.append(" , DATA AS(");
            pre_sql(student, param, stb);
            stb.append(" )");
            stb.append(" ,MAX_SEMESTER AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
            stb.append("   FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR, SCHREGNO ");
            stb.append(" ) ");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.SCHOOL_KIND");
                stb.append("     ,T1.CURRICULUM_CD");
            }
            stb.append("       ,VALUE(SCM.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD");
            stb.append("       ,T1.GRADES,T1.CREDIT,T1.CLASSNAME,T1.SUBCLASSNAME ");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND SCM.CLASSCD = T1.CLASSCD ");
                stb.append("     AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE  T1.CREDIT > 0 ");
            stb.append("   AND  T1.SCHOOLCD = '0' ");
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD, T1.STUDYREC_SUBCLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.SCHOOL_KIND ");
                stb.append("     ,T1.CURRICULUM_CD ");
            }
//            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
//            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append(" FROM DATA2 T1");
            stb.append(" INNER JOIN MAX_SEMESTER T2 ON T1.SCHREGNO = T2.SCHREGNO");
            stb.append("       AND T1.YEAR = T2.YEAR");
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD,T1.SUBCLASSCD, T1.STUDYREC_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.SCHOOL_KIND ");
                stb.append("     ,T1.CURRICULUM_CD ");
            }
            stb.append(" )");
            stb.append(" SELECT  T1.YEAR, T1.ANNUAL ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       , T4.SUBSTITUTION_CLASSCD");
                stb.append("       , T4.SUBSTITUTION_SCHOOL_KIND");
                stb.append("       , T4.SUBSTITUTION_CURRICULUM_CD");
            } else {
                stb.append("       , SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) AS SUBSTITUTION_CLASSCD");
            }
            stb.append("       , T4.SUBSTITUTION_SUBCLASSCD ");
//            stb.append("       , SUBSTCM.CLASSNAME AS SUBSTITUTION_CLASSNAME");
//            stb.append("       , VALUE(SCM.SUBCLASSORDERNAME1, SCM.SUBCLASSNAME) AS SUBSTITUTION_SUBCLASSNAME");
            stb.append("       , CRED.CREDITS AS SUBSTITUTION_CREDIT");
//            stb.append("       , VALUE(SUBSTCM.SHOWORDER, -1) AS SHOWORDERCLASS"); // 表示順教科
//            stb.append("       , VALUE(SCM.SHOWORDER, -1) AS SHOWORDERSUBCLASS"); // 表示順科目
            stb.append("       , T1.SUBCLASSCD AS ATTEND_SUBCLASSCD"); // 代替元科目(表示する行のグループコード科目)
//          stb.append("       , T4.ATTEND_SUBCLASSCD"); // 代替元科目
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       , T4.ATTEND_CLASSCD "); // 代替元科目
                stb.append("       , T4.ATTEND_SCHOOL_KIND "); // 代替元科目
                stb.append("       , T4.ATTEND_CURRICULUM_CD "); // 代替元科目
            }
            stb.append("       , T4.ATTEND_SUBCLASSCD AS SRC_ATTEND_SUBCLASSCD"); // 代替元科目
//            stb.append("       , ATTCM.CLASSNAME AS ATTEND_CLASSNAME");
//            stb.append("       , VALUE(ATTSUB.SUBCLASSORDERNAME1, ATTSUB.SUBCLASSNAME) AS ATTEND_SUBCLASSNAME"); // 代替元科目
//            stb.append("       , value(SUBSTCM.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            stb.append(" FROM   STUDYREC T1 ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_DAT T4 ON T1.YEAR = T4.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("        AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T4.ATTEND_SUBCLASSCD = T1.STUDYREC_SUBCLASSCD ");
            stb.append("        AND T4.SUBSTITUTION_TYPE_FLG = ? ");
            stb.append(" INNER JOIN MAX_SEMESTER SEM ON SEM.YEAR = T1.YEAR AND SEM.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = SEM.SEMESTER AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT T5 ON ");
            stb.append("        T5.YEAR = T4.YEAR AND T5.SUBSTITUTION_SUBCLASSCD = T4.SUBSTITUTION_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T5.SUBSTITUTION_CLASSCD = T4.SUBSTITUTION_CLASSCD ");
                stb.append("        AND T5.SUBSTITUTION_SCHOOL_KIND = T4.SUBSTITUTION_SCHOOL_KIND ");
                stb.append("        AND T5.SUBSTITUTION_CURRICULUM_CD = T4.SUBSTITUTION_CURRICULUM_CD ");
            }
            stb.append("        AND T5.ATTEND_SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T5.ATTEND_CLASSCD = T4.ATTEND_CLASSCD ");
                stb.append("        AND T5.ATTEND_SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.append("        AND T5.ATTEND_CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            }
            stb.append("        AND T5.MAJORCD = REGD.MAJORCD AND T5.COURSECD = REGD.COURSECD ");
            stb.append("        AND T5.GRADE = REGD.GRADE AND T5.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN CREDIT_MST CRED ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("           CRED.CLASSCD = T4.ATTEND_CLASSCD ");
                stb.append("           AND CRED.SCHOOL_KIND = T4.ATTEND_SCHOOL_KIND ");
                stb.append("           AND CRED.CURRICULUM_CD = T4.ATTEND_CURRICULUM_CD ");
            } else {
                stb.append("           CRED.CLASSCD = SUBSTR(T4.ATTEND_SUBCLASSCD, 1, 2) ");
            }
            stb.append("       AND CRED.SUBCLASSCD = T4.ATTEND_SUBCLASSCD ");
            stb.append("       AND CRED.YEAR = REGD.YEAR ");
            stb.append("       AND CRED.GRADE = REGD.GRADE ");
            stb.append("       AND CRED.COURSECD = REGD.COURSECD ");
            stb.append("       AND CRED.MAJORCD = REGD.MAJORCD ");
            stb.append("       AND CRED.COURSECODE = REGD.COURSECODE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" WHERE  T4.SUBSTITUTION_CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append("     OR T4.SUBSTITUTION_CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            } else {
                stb.append(" WHERE  SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append("     OR SUBSTR(T4.SUBSTITUTION_SUBCLASSCD, 1, 2) = '" + KNJDefineSchool.subject_T + "'");
            }

            return stb.toString();
        }

        private static void pre_sql(final Student student, final Param param, final StringBuffer stb) {
            stb.append(" SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        ,T1.SCHOOL_KIND ");
                stb.append("        ,T1.CURRICULUM_CD ");
            }
            stb.append("        ,T1.SUBCLASSCD, VALUATION AS GRADES ");
            stb.append("        ,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
            stb.append("        ,CASE WHEN VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) = 0 THEN VALUE(T1.COMP_CREDIT, 0) ELSE 0 END AS COMP_CREDIT ");
            stb.append("        ,CLASSNAME,SUBCLASSNAME ");
            stb.append("        ,T1.SCHOOLCD ");
            if (param._hasSCHREG_STUDYREC_DETAIL_DAT) {
                stb.append("        , TDET.REMARK1 AS VALID_FLG ");
            } else {
                stb.append("        , CAST(NULL AS VARCHAR(1)) AS VALID_FLG ");
            }
            stb.append(" FROM    SCHREG_STUDYREC_DAT T1 ");
            if (param._hasSTUDYREC_PROV_FLG_DAT) {
                stb.append("         LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND L3.YEAR = T1.YEAR ");
                stb.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
                stb.append("            AND L3.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND L3.PROV_FLG = '1' ");
            }
            if (param._hasSCHREG_STUDYREC_DETAIL_DAT) {
                stb.append("         LEFT JOIN SCHREG_STUDYREC_DETAIL_DAT TDET ON TDET.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("            AND TDET.YEAR = T1.YEAR ");
                stb.append("            AND TDET.SCHREGNO = T1.SCHREGNO ");
                stb.append("            AND TDET.CLASSCD = T1.CLASSCD ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("            AND TDET.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("            AND TDET.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("            AND TDET.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND TDET.SEQ = '002' ");
            }
            stb.append(" WHERE   EXISTS(SELECT 'X' FROM SCHBASE T2 WHERE T1.SCHREGNO = T2.SCHREGNO AND T1.YEAR <= T2.YEAR) ");
            if (param._z010.in(Z010.KINDAI)) {
                stb.append("     AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "') ");
            } else {
                stb.append("     AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                if (param._z010.in(Z010.hirogaku)) {
                    stb.append("          OR T1.CLASSCD = '" + _94 + "' ");
                } else if (param.isTokubetsuShien()) {
                    stb.append("          OR T1.CLASSCD = '" + _92 + "' ");
                }
                stb.append("         ) ");
            }
            if ("1".equals(param.property(Property.seitoSidoYorokuNotPrintAnotherStudyrec))) {
                stb.append("      AND T1.SCHOOLCD <> '1' ");
            }
            if (!param._z010.in(Z010.KINDAI)) {
                if (student.isGakunenSei(param) && param._isHeisetuKou) {
                    stb.append("      AND T1.ANNUAL not in " + param._gradeInChugaku + " ");
                }
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
                stb.append("    ) ");
            }
            if (!param._isPrintRisyuTourokuNomi) {
                stb.append("      AND (");
                stb.append("         NOT (T1.COMP_CREDIT IS NULL ");
                stb.append("               AND CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END IS NULL) ");
                stb.append("    ) ");
            }
            if (param._z010.in(Z010.KINDAI)) {
                stb.append("     AND NOT EXISTS(SELECT  'X' ");
                stb.append("                    FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                stb.append("                    WHERE   T2.YEAR = T1.YEAR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                            AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                    stb.append("                            AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("                            AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("                            AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
            }
            if (param._hasSTUDYREC_PROV_FLG_DAT) {
                stb.append("          AND L3.SUBCLASSCD IS NULL ");
            }
        }

        /**
         * @return 学習記録データのＳＱＬ文を戻します。
         */
        private static String sqlStudyrec(final Student student, final String year, final Param param) {
            // 同一年度同一科目の場合単位は合計とします。
            //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
            String gradesCase = "case when 0 < T1.GRADES then GRADES end";
            String creditCase = "case when 0 < T1.GRADES then CREDIT end";

            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST('" + year + "' AS VARCHAR(4))))");
            stb.append(" , DATA AS(");
            pre_sql(student, param, stb);
            stb.append(" )");
            stb.append(" ,DATA2 AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("       ,VALUE(SCM.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
            stb.append("       ,T1.SCHOOLCD,T1.GRADES,T1.CREDIT,T1.COMP_CREDIT,T1.CLASSNAME,T1.SUBCLASSNAME ");
            stb.append("       ,T1.VALID_FLG");
            stb.append(" FROM DATA T1");
            stb.append(" LEFT JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND SCM.CLASSCD = T1.CLASSCD ");
                stb.append("       AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" )");
            stb.append(" ,STUDYREC AS(");
            stb.append(" SELECT T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("       ,T1.SUBCLASSCD");
            stb.append("       ,case when COUNT(*) = 1 then MAX(T1.GRADES) ");//１レコードの場合、評定はそのままの値。
            stb.append("            when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
            stb.append("            when SC.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+") * T1.CREDIT)) / SUM("+creditCase+"),0)");
            stb.append("            else MAX(T1.GRADES) end AS GRADES");
            stb.append("       ,SUM(T1.CREDIT) AS CREDIT");
            stb.append("       ,SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
            stb.append("       ,MIN(T1.CLASSNAME) AS CLASSNAME");
            stb.append("       ,MIN(T1.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       ,MIN(T1.SCHOOLCD) AS SCHOOLCD");
            stb.append("       ,MIN(T1.VALID_FLG) AS VALID_FLG");
            stb.append(" FROM DATA2 T1 ");
            stb.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("        AND SC.SCHOOL_KIND = '" + student._schoolKind + "' ");
            }
            stb.append(" GROUP BY T1.SCHREGNO,T1.YEAR,T1.ANNUAL,T1.CLASSCD");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("          ,T1.SUBCLASSCD,SC.GVAL_CALC ");
            stb.append(" )");

            stb.append(" SELECT  T1.SCHOOLCD, T1.YEAR, T1.ANNUAL, T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,T1.SCHOOL_KIND");
                stb.append("       ,T1.CURRICULUM_CD");
            }
            stb.append("       , T1.SUBCLASSCD");
//            stb.append("       , VALUE(");
//            if (param._hasANOTHER_CLASS_MST) {
//                stb.append("               CASE WHEN SCHOOLCD = '1' THEN ANT2.CLASSNAME END, ");
//            }
//            stb.append("               T1.CLASSNAME, CM.CLASSNAME) AS CLASSNAME");
            stb.append("             , T1.CLASSNAME ");
//            stb.append("       , VALUE(");
//            if (param._hasANOTHER_SUBCLASS_MST) {
//                stb.append("               CASE WHEN SCHOOLCD = '1' THEN ANT3.SUBCLASSORDERNAME1 END, ");
//                stb.append("               CASE WHEN SCHOOLCD = '1' THEN ANT3.SUBCLASSNAME END, ");
//            }
//            stb.append("               T1.SUBCLASSNAME ");
//            stb.append("             , SCM.SUBCLASSORDERNAME1 ");
//            stb.append("             , SCM.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("             , T1.SUBCLASSNAME ");
            stb.append("       , T1.CREDIT");
            stb.append("       , T1.COMP_CREDIT");
            stb.append("       , T1.GRADES");
            stb.append("       , T1.SCHOOLCD");
//            stb.append("       , VALUE(CM.SHOWORDER, -1) AS SHOWORDERCLASS"); // 表示順教科
//            stb.append("       , VALUE(SCM.SHOWORDER, -1) AS SHOWORDERSUBCLASS"); // 表示順科目
//            stb.append("       , value(CM.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
            stb.append("       , T1.VALID_FLG ");
            stb.append(" FROM   STUDYREC T1 ");
//            stb.append(" LEFT JOIN CLASS_MST CM ON CM.CLASSCD = T1.CLASSCD ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("       AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
//            }
//            stb.append(" LEFT JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("       AND SCM.CLASSCD = T1.CLASSCD ");
//                stb.append("       AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                stb.append("       AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
//            }
//            if (param._hasANOTHER_CLASS_MST) {
//                stb.append(" LEFT JOIN ANOTHER_CLASS_MST ANT2 ON ANT2.CLASSCD = T1.CLASSCD ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("       AND ANT2.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                }
//            }
//            if (param._hasANOTHER_SUBCLASS_MST) {
//                stb.append(" LEFT JOIN ANOTHER_SUBCLASS_MST ANT3 ON ANT3.SUBCLASSCD = T1.SUBCLASSCD ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("       AND ANT3.CLASSCD = T1.CLASSCD ");
//                    stb.append("       AND ANT3.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                    stb.append("       AND ANT3.CURRICULUM_CD = T1.CURRICULUM_CD ");
//                }
//            }
            stb.append(" WHERE  (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("          OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            if (param._z010.in(Z010.hirogaku)) {
                stb.append("          OR T1.CLASSCD = '" + _94 + "' ");
            } else if (param.isTokubetsuShien()) {
                stb.append("          OR T1.CLASSCD = '" + _92 + "' ");
            }
            stb.append("         ) ");

            return stb.toString();
        }

        /**
         * @return 留学単位のＳＱＬ文を戻します。
         * @see 年度別の単位。(留年の仕様に対応)
         */
        private static String sqlAbroadCredit(final Student student, final String year, final Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHBASE(SCHREGNO, YEAR) AS (VALUES(CAST(? AS VARCHAR(8)), CAST('" + year + "' AS VARCHAR(4)))) ");
            stb.append(" SELECT TRANSFER_YEAR AS YEAR, SUM(ABROAD_CREDITS) AS CREDIT ");
            stb.append(" FROM(");
            stb.append("      SELECT  ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
            stb.append("      FROM    SCHREG_TRANSFER_DAT W1 ");
            stb.append("      WHERE   EXISTS(SELECT 'X' FROM SCHBASE W2 WHERE W1.SCHREGNO = W2.SCHREGNO) ");
            stb.append("          AND TRANSFERCD = '1' ");
            stb.append("     )ST1 ");
            if (!"1".equals(param.property(Property.seitoSidoYorokuZaisekiMae))) {
                stb.append("     , (");
                stb.append("      SELECT  W1.YEAR ");
                stb.append("      FROM    SCHREG_REGD_DAT W1 ");
                stb.append("      WHERE   EXISTS(SELECT 'X' FROM SCHBASE W2 WHERE W1.SCHREGNO = W2.SCHREGNO AND W1.YEAR <= W2.YEAR) ");
                if (student.isGakunenSei(param) && param._isHeisetuKou) {
                    stb.append("      AND W1.GRADE not in " + param._gradeInChugaku + " ");
                }
                stb.append("      GROUP BY W1.YEAR ");
                stb.append("     )ST2 ");
                stb.append(" WHERE  INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
            }
            stb.append("GROUP BY TRANSFER_YEAR ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データ科目別単位数のクラス>>。 学習記録データクラスを科目別に集計しました。
     */
    private static class StudyRecSubclassTotal {

        private static class StudyRecSubclassTotalComparator implements Comparator<StudyRecSubclassTotal> {
            final Param _param;
            public StudyRecSubclassTotalComparator(final Param param) {
                _param = param;
            }

            public int compare(final StudyRecSubclassTotal t1, final StudyRecSubclassTotal t2) {

                if (_param._d065Name1List.contains(t1.studyrec().getKeySubclasscd(_param)) && _param._d065Name1List.contains(t2.studyrec().getKeySubclasscd(_param))) {
                } else if (_param._d065Name1List.contains(t1.studyrec().getKeySubclasscd(_param))) {
                    return 1;
                } else if (_param._d065Name1List.contains(t2.studyrec().getKeySubclasscd(_param))) {
                    return -1;
                }
                int rtn;
                rtn = ClassMst.compareOrder(_param, t1.studyrec()._classMst, t2.studyrec()._classMst);
                if (0 != rtn) {
                    return rtn;
                }
                rtn = SubclassMst.compareOrder(_param, t1.studyrec()._subclassMst, t2.studyrec()._subclassMst);
                return rtn;
            }
        }

        final List<StudyRec> _studyrecList;

        final Collection<String> _dropYears;
        final Collection<String> _checkDropYears;
        final Collection<String> _tengakuYears;

        /**
         * コンストラクタ。
         *
         * @param rs
         */
        private StudyRecSubclassTotal(final List<StudyRec> studyrecList, final Collection<String> dropYears, final Collection<String> checkDropYears, final Collection<String> tengakuYears) {
            _studyrecList = studyrecList;
            _dropYears = dropYears;
            _checkDropYears = checkDropYears;
            _tengakuYears = tengakuYears;
        }

        private static List<StudyRec> getStudyRecInYearList(final Collection<StudyRec> studyrecList, final Collection<String> yearSet) {
            return getStudyRecWithValidFlgInYearList(studyrecList, yearSet, false);
        }

        private static List<StudyRec> getStudyRecWithValidFlgInYearList(final Collection<StudyRec> studyrecList, final Collection<String> yearSet, final boolean checkValidFlg) {
            final List studyRecInYearList = new ArrayList();
            for (final StudyRec sr : studyrecList) {
                if (!yearSet.contains(sr._year)) {
                    continue;
                }
                if (checkValidFlg && sr._validFlg == null) {
                    continue;
                }
                studyRecInYearList.add(sr);
            }
            return studyRecInYearList;
        }

        private List<Map<String, Integer>> creditKindYearCreditMapList(final int studyRecKindCredit, final Collection<String> yearSet, final CheckDropYearsFlg checkDropYears, final Param param) {
            final List<Map<String, Integer>> mapList = new ArrayList();
            final Collection<String> studyRecEnabledYears = null == _checkDropYears ? yearSet : CollectionUtils.union(yearSet, _checkDropYears);
            for (final StudyRec sr : getStudyRecInYearList(StudyRec.getTargetStudyRecList(_studyrecList, _dropYears, _checkDropYears, _tengakuYears, checkDropYears, param), studyRecEnabledYears)) {
                final Integer tgt = sr.creditForTotal(studyRecKindCredit);
                if (null != tgt) {
                    final Map<String, Integer> m = new HashMap();
                    m.put(sr._year, tgt);
                    mapList.add(m);
                }
            }
            return mapList;
        }

        private List<Integer> values(final List<Map<String, Integer>> mapList) {
            final List<Integer> rtn = new ArrayList();
            for (final Map<String, Integer> m : mapList) {
                rtn.addAll(m.values());
            }
            return rtn;
        }

        private boolean hasRecord(final Collection<String> yearSet, final Collection<String> beforeDropYears) {
            boolean hasRecord = getStudyRecWithValidFlgInYearList(_studyrecList, yearSet, false).size() > 0;
            boolean hasValidFlgOnRecord = getStudyRecWithValidFlgInYearList(_studyrecList, beforeDropYears, true).size() > 0;
            return hasRecord || hasValidFlgOnRecord;
        }

        private Collection<Integer> creditList(final Collection<String> yearSet, final CheckDropYearsFlg flg, final Param param) {
            final List<Map<String, Integer>> creditKindYearCreditMapList = creditKindYearCreditMapList(StudyRec.KIND_CREDIT, yearSet, flg, param);
            if (param._isOutputDebugSeiseki) {
                log.info(" creditKindYearCreditMapList(" + flg + ") = " + creditKindYearCreditMapList + " in " + this.toString());
            }
            return values(creditKindYearCreditMapList);
        }

        private Collection<Integer> compCreditList(final Collection<String> yearSet, final Param param) {
            return values(creditKindYearCreditMapList(StudyRec.KIND_COMP_CREDIT, yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param));
        }

        /**
         * 履修のみ（「不認定」）か
         * @return 履修のみならtrue
         */
        private boolean isRisyuNomi(final Param param) {
            boolean rtn = true;
            for (final StudyRec sr : _studyrecList) {
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
            for (final StudyRec sr : _studyrecList) {
                if (!sr.isMirisyu(param)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        private static String comment(final String year, final Collection<String> dropYears, final Collection<String> tengakuYears) {
            return dropYears.contains(year) ? ",dropped" : tengakuYears.contains(year) ? ",tengaku" : "";
        }

        private static List<String> creditInfoList(final List<StudyRec> studyrecList, final Collection<String> dropYears, final Collection<String> tengakuYears) {
            final List<String> rtn = new ArrayList();
            for (final StudyRec sr : studyrecList) {
                rtn.add("[" + sr._year + "," + sr._credit + "" + comment(sr._year, dropYears, tengakuYears) + "]");
            }
            return rtn;
        }

        private static List<String> compCreditInfoList(final List<StudyRec> studyrecList, final Collection<String> dropYears, final Collection<String> tengakuYears) {
            final List<String> rtn = new ArrayList();
            for (final StudyRec sr : studyrecList) {
                rtn.add("[" + sr._year + "," + sr._compCredit + "" + comment(sr._year, dropYears, tengakuYears) + "]");
            }
            return rtn;
        }

        private String info(final List<StudyRec> studyrecList, final Collection<String> yearSet, final Collection<String> dropYears, final Collection<String> tengakuYears, final Param param) {
            final List<String> creditList = creditInfoList(studyrecList, dropYears, tengakuYears);
            final List<String> compCreditList = compCreditInfoList(studyrecList, dropYears, tengakuYears);
            final String compCreditInfo = " " + (sum(compCreditList(yearSet, param)) != null ? (" compcredit = " + compCreditList.toString()) : "");
            return (sum(creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param)) != null && sum(creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param)).intValue() == 0) ? compCreditInfo : (" credit = " + creditList.toString());
        }

        private StudyRec studyrec() {
            return _studyrecList.get(_studyrecList.size() - 1);
        }

        /**
         * データが全て非対象か
         * @return
         *   全てのレコードが原級留置の年度に含まれるが、有効のフラグがある場合、false <br>
         *   全てのレコードが原級留置の年度に含まれ、有効のフラグもない場合、true<br>
         *   それ以外の場合、false (前提条件:レコードは1つ以上存在する)<br>
         */
        public boolean isAllNotTarget(final Param param) {
            return StudyRec.getTargetStudyRecList(_studyrecList, _dropYears, _checkDropYears, _tengakuYears, CheckDropYearsFlg._CHECK_ENABLE_FLG, param).size() == 0 ? true : false;
        }

        public String toString(final Collection<String> studyRecYearSet, final Param param) {
            return "SubclassTotal(" + studyrec()._subclassMst + " " + info(_studyrecList, studyRecYearSet, _dropYears, _tengakuYears, param) + ")";
        }

        public String toString() {
            return "SubclassTotal(" + studyrec()._subclassMst + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class StudyRecSubstitution extends StudyRec {

        private List<SubstitutionAttendSubclass> attendSubclasses = new ArrayList();

        /**
         * コンストラクタ。
         */
        private StudyRecSubstitution(final String year, final String annual, final ClassMst classMst, final SubclassMst subclassMst, final Integer credit, final Integer compCredit,
                final BigDecimal grades) {
            super(year, annual, classMst, subclassMst, credit, compCredit,
                    grades, null);
        }

        private String daitaiName(final String substitutionTypeFlg) {
            return StudyrecSubstitutionContainer.ICHIBU.equals(substitutionTypeFlg) ? "一部代替" : "代替";
        }

        /**
         * 「修得単位の記録」「学習の記録」の代替科目備考データを得る。
         * @return
         */
        private Object[] getBikoSubstitutionInfo(final Param param) {
            final Set addedKey = new HashSet();

            String totalSubCredit = "";
            final StringBuffer attendSubClassNames = new StringBuffer();
            String sp = "";

            for (final SubstitutionAttendSubclass attendSubclasss : attendSubclasses) {

                final String keyCd = attendSubclasss._attendSubclassMst.getKey(param);
                final String attendSubClassName = attendSubclasss._attendSubclassMst.subclassname();
                final String subCredit = attendSubclasss._substitutionCredit;

                if (!addedKey.contains(keyCd)) {
                    attendSubClassNames.append(sp + attendSubClassName);
                    sp = "、";
                    addedKey.add(keyCd);
                }

                if (subCredit == null || "".equals(subCredit) || " ".equals(subCredit)) {
                    totalSubCredit = "";
                    continue;
                }
                if (totalSubCredit == null || "".equals(totalSubCredit) || " ".equals(totalSubCredit)) {
                    totalSubCredit = subCredit;
                    continue;
                }
                totalSubCredit = String.valueOf(Integer.parseInt(subCredit) + Integer.parseInt(totalSubCredit));
            }
            return new Object[]{attendSubClassNames.toString(), totalSubCredit};
        }

        /**
         * 「修得単位の記録」「学習の記録」の代替科目備考文字列を得る。
         * @return
         */
        public String getBikoSubstitution(final String substitutionTypeFlg, final Param param) {
            final Object[] info = getBikoSubstitutionInfo(param);
            final String attendSubClassNames = (String) info[0];
            final String totalSubCredit = (String) info[1];
            return "「" + daitaiName(substitutionTypeFlg) + "・" + attendSubClassNames + totalSubCredit + "単位」";
        }

        /**
         * 「活動の記録用」の代替科目備考文字列を得る。
         * @return
         */
        public String getBikoSubstitution90(final String year, final String substitutionTypeFlg, final Param param) {
            final Set addedKey = new HashSet();

            final StringBuffer attendSubClassNames = new StringBuffer();
            String sp = "";

            for (final SubstitutionAttendSubclass attendSubclasss : attendSubclasses) {

                final String attendClassName = StringUtils.isEmpty(attendSubclasss._attendClassMst._classname) ? "" : attendSubclasss._attendClassMst._classname;
                final String attendSubClassName = attendClassName + "・" + (defstr(attendSubclasss._attendSubclassMst.subclassname()));

                final String keyCd = attendSubclasss._attendSubclassMst.getKey(param);
                if (!addedKey.contains(keyCd) && (year == null || year.equals(attendSubclasss._attendyear))) {
                    attendSubClassNames.append(sp + attendSubClassName);
                    sp = "、";
                    addedKey.add(keyCd);
                }
            }
            if (attendSubClassNames.length() != 0) {
                final String substSubClassName = _subclassMst.subclassname() != null ? _subclassMst.subclassname() : "";
                return substSubClassName + "は" + attendSubClassNames.toString() + "で" + daitaiName(substitutionTypeFlg);
            }
            return "";
        }

        /**
         * 専門科目のデータを追加する
         * @param attendyear 受講年度(代替年度)
         * @param attendSubclassCd 専門科目コード
         * @param attendSubclassName 専門科目名
         * @param substitutionCredit 代替単位
         */
        public void addAttendSubclass(
                final String attendyear,
                final ClassMst attendClassMst,
                final SubclassMst attendSubclassMst,
                final String substitutionCredit) {
            attendSubclasses.add(new SubstitutionAttendSubclass(attendyear, attendClassMst, attendSubclassMst, substitutionCredit));
        }

        /**
         * 履修科目の最大年度を得る
         * @return 履修科目の最大年度
         */
        public String getMaxAttendSubclassYear() {
            String maxyear = null;
            for (final SubstitutionAttendSubclass array : attendSubclasses) {
                if (maxyear == null || array._attendyear != null && maxyear.compareTo(array._attendyear) < 0) {
                    maxyear = array._attendyear;
                }
            }
            return maxyear;
        }

        private static class SubstitutionAttendSubclass {
            final String _attendyear;
            final ClassMst _attendClassMst;
            final SubclassMst _attendSubclassMst;
            final String _substitutionCredit;
            public SubstitutionAttendSubclass(final String attendyear, final ClassMst attendClassMst, final SubclassMst attendSubclassMst, final String substitutionCredit) {
                _attendyear = attendyear;
                _attendClassMst = attendClassMst;
                _attendSubclassMst = attendSubclassMst;
                _substitutionCredit = substitutionCredit;
            }
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private static class StudyrecSubstitutionContainer {

        static final String ZENBU = "1";  // 代替フラグ:全部
        static final String ICHIBU = "2";  // 代替フラグ:一部

        static final List<String> TYPE_FLG_LIST = Arrays.asList(StudyrecSubstitutionContainer.ZENBU, StudyrecSubstitutionContainer.ICHIBU);

        final Map studyRecSubstitutions = new HashMap();

        public Map<String, StudyRecSubstitution> getStudyrecSubstitution(final String substitutionTypeFlg) {
            return getMappedMap(studyRecSubstitutions, substitutionTypeFlg);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習記録データクラス>>。
     */
    private static class HtrainRemark implements Comparable<HtrainRemark> {
        final String _year;
        final String _annual;
        /** 特別活動 */
        private String _specialActRemark;
        /** 所見 */
        private String _totalRemark;
        /** 出欠備考 */
        private String _attendRecRemark;
        /** 総合的な学習の時間学習活動 */
        private String _totalStudyAct;
        /** 総合的な学習の時間評価 */
        private String _totalStudyVal;

        private String _detail2DatSeq001Remark1;

        private HtrainRemark(
                final String year,
                final String annual
        ) {
            _year = year;
            _annual = annual;
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

        /**
         * 所見クラスを作成し、マップに加えます。
         */
        private static Map loadHtrainRemark(final DB2UDB db2, final String schregno, final Param param) {
            if (null == param._katsudo) {
                return Collections.emptyMap();
            }
            final Map<String, HtrainRemark> htrainRemarkMap = new HashMap();
            try {
                final String psKey = "PS_HTRAINREMARK_DAT";
                if (null == param._psMap.get(psKey)) {
                    final String sql = "SELECT  YEAR, ANNUAL, TOTALSTUDYACT, TOTALSTUDYVAL, SPECIALACTREMARK, TOTALREMARK, ATTENDREC_REMARK"
                            + " FROM HTRAINREMARK_DAT"
                            + " WHERE SCHREGNO = ?"
                            + " AND YEAR <= ?"
                            ;
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }

                for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno, param._year})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String annual = KnjDbUtils.getString(row, "ANNUAL");
                    final String specialActRemark = KnjDbUtils.getString(row, "SPECIALACTREMARK");
                    final String totalRemark = KnjDbUtils.getString(row, "TOTALREMARK");
                    final String attendRecRemark = KnjDbUtils.getString(row, "ATTENDREC_REMARK");
                    final String totalStudyAct = KnjDbUtils.getString(row, "TOTALSTUDYACT");
                    final String totalStudyVal = KnjDbUtils.getString(row, "TOTALSTUDYVAL");

                    final HtrainRemark htrainRemark = new HtrainRemark(
                            year,
                            annual);

                    htrainRemark._specialActRemark = specialActRemark;
                    htrainRemark._totalRemark = totalRemark;
                    htrainRemark._attendRecRemark = attendRecRemark;
                    htrainRemark._totalStudyAct = totalStudyAct;
                    htrainRemark._totalStudyVal = totalStudyVal;

                    htrainRemarkMap.put(year, htrainRemark);
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            }
            if (param.isTokubetsuShien()) {
                try {
                    for (final Map<String, String> row : KnjDbUtils.query(db2, getHtrainremarkDetail2HDatSql(), new Object[] {param._year, schregno, "001"})) {
                        final String year = KnjDbUtils.getString(row, "YEAR");
                        if (null == year) {
                            continue;
                        }
                        if (null == htrainRemarkMap.get(year))  {
                            final HtrainRemark htrainremarkDat = new HtrainRemark(year, KnjDbUtils.getString(row, "ANNUAL"));
                            htrainRemarkMap.put(year, htrainremarkDat);
                        }
                        final HtrainRemark htrainremarkDat = htrainRemarkMap.get(year);
                        htrainremarkDat._detail2DatSeq001Remark1 = KnjDbUtils.getString(row, "REMARK1");
                    }
                } catch (final Exception e) {
                    log.error("Exception", e);
                }
            }
            return htrainRemarkMap;
        }

        private static String getHtrainremarkDetail2HDatSql() {
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
            return stb.toString();
        }
    }

    private static class Shoken {
        /** 総合学習活動(HTRAINREMARK_HDAT.TOTALSTUDYACT) */
        private String _htrainRemarkHdatTotalstudyact;
        /** 総合学習評価(HTRAINREMARK_HDAT.TOTALSTUDYVAL) */
        private String _htrainRemarkHdatTotalstudyval;
        /** 東京都奉仕・活動(HTRAINREMARK_HDAT.TOTALSTUDYACT2) */
        private String _htrainRemarkHdatAct2;
        /** 東京都奉仕・評価(HTRAINREMARK_HDAT.TOTALSTUDYVAL2) */
        private String _htrainRemarkHdatVal2;
        /** 東京都修得単位の記録備考(HTRAINREMARK_HDAT.CREDITREMARK) */
        private String _htrainRemarkHdatCreditremark;

        private String _htrainRemarkDetail2HdatSeq001Remark1;

        private static Shoken load(final DB2UDB db2, final String schregno, final Param param) {
            final Shoken shoken = new Shoken();
            if (null == param._katsudo && (!param._z010.in(Z010.tokyoto) || param._z010.in(Z010.tokyoto) && null == param._tani)) {
                return shoken;
            }

            final String psKey = "PS_HTRAINREMARK_HDAT";
            try {
                if (null == param._psMap.get(psKey)) {
                    final String sql0 = "select TOTALSTUDYACT, TOTALSTUDYVAL"
                            + (param._z010.in(Z010.tokyoto) ? " ,TOTALSTUDYACT2, TOTALSTUDYVAL2, CREDITREMARK" : "")
                            + " from HTRAINREMARK_HDAT"
                            + " where SCHREGNO = ?"
                            ;
                    param._psMap.put(psKey, db2.prepareStatement(sql0));
                }
                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {schregno}));
                shoken._htrainRemarkHdatTotalstudyact = KnjDbUtils.getString(row, "TOTALSTUDYACT");
                shoken._htrainRemarkHdatTotalstudyval = KnjDbUtils.getString(row, "TOTALSTUDYVAL");
                if (param._z010.in(Z010.tokyoto)) {
                    shoken._htrainRemarkHdatAct2 = KnjDbUtils.getString(row, "TOTALSTUDYACT2");
                    shoken._htrainRemarkHdatVal2 = KnjDbUtils.getString(row, "TOTALSTUDYVAL2");
                    shoken._htrainRemarkHdatCreditremark = KnjDbUtils.getString(row, "CREDITREMARK");
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            }
            if (param.isTokubetsuShien()) {
                final String psKey2 = "PS_HTRAINREMARK_DETAIL2_HDAT";

                try {
                    if (null == param._psMap.get(psKey2)) {
                        final String sql = "select REMARK1 "
                                + " from HTRAINREMARK_DETAIL2_HDAT"
                                + " where "
                                + "  SCHREGNO = ?"
                                + "  AND HTRAIN_SEQ = '001'"
                                ;
                        param._psMap.put(psKey2, db2.prepareStatement(sql));
                    }
                    shoken._htrainRemarkDetail2HdatSeq001Remark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, param._psMap.get(psKey2), new Object[] {schregno}));
                } catch (final Exception e) {
                    log.error("Exception", e);
                }
            }
            return shoken;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<学習備考>>。
     */
    private static class GakushuBiko {

        private Map _risyuTannibiko = new HashMap();
        private Map _studyrecbiko = new HashMap();
        private Map _studyrecbikoSubstitution = new HashMap();

        final StudyrecSubstitutionContainer _ssc = new StudyrecSubstitutionContainer();

        private Map getSubclassRisyuTanniBikoMap(final String subclassCd) {
            return getMappedMap(_risyuTannibiko, subclassCd);
        }

        /**
         * 科目の年度の学習記録履修単位備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param rishuTanniBiko 履修単位備考
         */
        public void putRisyuTanniBiko(final String subclassCd, final String year, final String rishuTanniBiko) {
            getSubclassRisyuTanniBikoMap(subclassCd).put(year, rishuTanniBiko);
        }

        private Map getStudyrecBikoMap(final String subclassCd) {
            return getMappedMap(_studyrecbiko, subclassCd);
        }

        /**
         * 科目の年度の学習記録備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param studyrecBiko 学習記録備考
         */
        public void putStudyrecBiko(final String subclassCd, final String year, final String studyrecBiko) {
            getStudyrecBikoMap(subclassCd).put(year, studyrecBiko);
        }


        private Map getStudyrecSubstitutionBikoMap(final String subclassCd, final String substitutionTypeFlg) {
            return getMappedMap(getMappedMap(_studyrecbikoSubstitution, subclassCd), substitutionTypeFlg);
        }

        /**
         * 科目の年度の学習記録代替科目備考をセットする。
         * @param subclassCd 科目コード
         * @param year 年度
         * @param studyrecSubstitutionBiko 学習記録代替科目備考
         */
        public void putStudyrecSubstitutionBiko(final String subclassCd, final String substitutionTypeFlg, final String year, final String studyrecSubstitutionBiko) {
            getStudyrecSubstitutionBikoMap(subclassCd, substitutionTypeFlg).put(year, studyrecSubstitutionBiko);
        }

        /**
         * 最小年度から最大年度までの備考の連結文字列を得る。
         * @param map 年度をキーとする備考のマップ
         * @param yearMin 最小年度
         * @param yearMax 最大年度
         * @param spStr 区切り文字
         * @return 最小年度から最大年度までの備考の連結文字列
         */
        private StringBuffer getBiko(final Map map, final String yearMin, final String yearMax, final String spStr) {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (final Iterator it = map.keySet().iterator(); it.hasNext();) {
                final String year = (String) it.next();
                final String biko = (String) map.get(year);
                if ((yearMin == null || yearMin.compareTo(year) <= 0) && (yearMax == null || year.compareTo(yearMax) <= 0) && biko.length() != 0) {
                    stb.append(comma).append(biko.toString());
                    comma = spStr;
                }
            }
            return stb;
        }

        /**
         * 科目コードのyearMinからyearMaxまでの履修単位備考をコンマ連結で得る。
         * @param subclassCd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getRisyuTanniBiko(final String subclassCd, final String yearMin, final String yearMax) {
            return getBiko(getSubclassRisyuTanniBikoMap(subclassCd), yearMin, yearMax, "、");
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録備考を得る。
         * @param subclassCd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecBiko(final String subclassCd, final String yearMin, final String yearMax) {
            return getBiko(getStudyrecBikoMap(subclassCd), yearMin, yearMax, "");
        }

        /**
         * 科目コードのyearMinからyearMaxまでの学習記録代替科目備考を得る。
         * @param subclassCd 科目コード
         * @param yearMin year最小
         * @param yearMax year最大
         * @return
         */
        public StringBuffer getStudyrecSubstitutionBiko(final String subclassCd, final String substitutionTypeFlg, final String yearMin, final String yearMax) {
            return getBiko(getStudyrecSubstitutionBikoMap(subclassCd, substitutionTypeFlg), yearMin, yearMax, "");
        }

        /**
         * 科目コードの学習記録代替科目の単位を得る。
         * @param subclassCd 科目コード
         * @return
         */
        public Integer getStudyrecSubstitutionCredit(final String subclassCd, final String substitutionTypeFlg, final Param param) {
            if (null == subclassCd || 0 == param._SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount) {
                return null;
            }
            final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);
            final List<Object[]> list = new ArrayList();
            for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {
                if (!subclassCd.equals(substitutionSubClassCd)) {
                    continue;
                }
                final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);
                final Object[] info = studyRecSubstitution.getBikoSubstitutionInfo(param);
                list.add(info);
            }
            if (list.isEmpty()) {
                return null;
            }
            int total = 0;
            for (final Object[] info : list) {
                total += ((Integer) info[1]).intValue();
            }
            return new Integer(total);
        }

        /**
         * 代替科目の学習記録備考を作成し、マップに加えます。
         */
        private void createStudyRecBikoSubstitution(final Param param) {
            if (null == param._tani && null == param._gakushu && null == param._katsudo || 0 == param._SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount) {
                return;
            }
            for (final String substitutionTypeFlg : StudyrecSubstitutionContainer.TYPE_FLG_LIST) {

                final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);

                // 代替科目備考追加処理
                for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {

                    final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);
                    final String keyCd = _90.equals(substitutionSubClassCd.substring(0, 2)) ? _90 : substitutionSubClassCd;

                    final String bikoSubstitution = studyRecSubstitution.getBikoSubstitution(substitutionTypeFlg, param);


                    putStudyrecSubstitutionBiko(keyCd, substitutionTypeFlg, studyRecSubstitution.getMaxAttendSubclassYear(), bikoSubstitution);
                }
            }
        }

        /**
         * 総合的な学習の時間の代替科目の学習記録備考を作成し、マップに加えます。
         */
        private Map getStudyRecBikoSubstitution90(final String substitutionTypeFlg, final List<Gakuseki> gakusekiList, final String keyAll, final Param param) {

            final Map map = new HashMap();
            if (0 == param._SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount) {
                return map;
            }

            final Map<String, StudyRecSubstitution> studyRecSubstitutionMap = _ssc.getStudyrecSubstitution(substitutionTypeFlg);
            for (final String substitutionSubClassCd : studyRecSubstitutionMap.keySet()) {

                if (!_90.equals(substitutionSubClassCd.substring(0,2))) {
                    continue;
                }
                final StudyRecSubstitution studyRecSubstitution = studyRecSubstitutionMap.get(substitutionSubClassCd);

                for (final Gakuseki gakuseki : gakusekiList) {
                    if (null == gakuseki._year) {
                        continue;
                    }
                    getMappedList(map, gakuseki._year).add(studyRecSubstitution.getBikoSubstitution90(gakuseki._year, substitutionTypeFlg, param));
                }

                getMappedList(map, keyAll).add(studyRecSubstitution.getBikoSubstitution90(null, substitutionTypeFlg, param));
            }
            return map;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    private abstract static class KNJA130_0 extends KNJA130CCommon.KNJA130_0 {
        private Param _param;

        protected boolean nonedata; // データ有りフラグ

        KNJA130_0(final Vrw32alp svf, final Param param) {
            super(svf, param);
            _param = param;
        }

        public abstract void setDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList);

        protected Param param() {
            return _param;
        }

        /**
         * <pre>
         *  学年・年度表示欄の印字位置（番号）を戻します。
         *  ・学年制の場合は学年による固定位置
         *  ・単位制の場合は連番
         * </pre>
         *
         * @param i：連番
         * @param gakuseki
         * @return
         */
        protected int getGradeColumnNum(final Student student, final int i, final Gakuseki gakuseki, final int column) {
            return getGradeColumnNum(student, i, gakuseki, column, 0);
        }

        protected int getGradeColumnNum(final Student student, final int i, final Gakuseki gakuseki, final int column, final int flg) {
            final boolean includeZaisekiMae = 1 == flg;
            // final boolean isSeq = 2 == flg;
            final int rtn;
            if (student.isGakunenSei(param()) && !includeZaisekiMae) {
                if (null == gakuseki._gradeCd) {
                    return -1;
                }
                int j = Integer.parseInt(gakuseki._gradeCd);
                rtn = (0 == j % column) ? column : j % column;
                if (param()._isOutputDebug) {
                    log.info(" gradeColumnNum 1 (" + i + ", " + gakuseki + ", " + column + ", " + flg + ") = " + rtn + " (j = " + j + ", gakunensei = " + student.isGakunenSei(param()) + ")");
                }
            } else {
                rtn = i;
                if (param()._isOutputDebug) {
                    log.info(" gradeColumnNum 2 (" + i + ", " + gakuseki + ", " + column + ", " + flg + ") = " + rtn + " (gakunensei = " + student.isGakunenSei(param()) + ")");
                }
            }
            return rtn;
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
            final List<CharMS932> rtn = new ArrayList();
            for (int j = 0; j < src.length(); j++) {
                final String z = src.substring(j, j + 1);             //1文字を取り出す
                final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
                rtn.add(c);
            }
            return rtn;
        }

        protected static List<String> getTokenList(final String targetsrc, final int dividlen, final int dividnum, final Param param) {
            final List<String> tokenList = getTokenList(targetsrc, dividlen, param);
            if (tokenList.size() > dividnum) {
                return tokenList.subList(0, dividnum);
            }
            return tokenList;
        }

        protected static List<String> getTokenList(final String targetsrc0, final int dividlen, final Param param) {
            if (param._useEditKinsoku) {
                return KNJ_EditKinsoku.getTokenList(targetsrc0, dividlen);
            }
            if (targetsrc0 == null) {
                return Collections.emptyList();
            }
            final List lines = new ArrayList();         //編集後文字列を格納する配列
            int len = 0;
            StringBuffer stb = new StringBuffer();

            try {
                final String targetsrc;
                if (!StringUtils.replace(targetsrc0, "\r\n", "\n").equals(targetsrc0)) {
                    targetsrc = StringUtils.replace(targetsrc0, "\r\n", "\n");
                } else {
                    targetsrc = targetsrc0;
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

        protected SvfField getSvfField(final String fieldname) {
            return _form._formInfo.getSvfField(fieldname, false);
        }

        protected String getFieldForData(final List<String> fields, final String data) {
            return _form._formInfo.getFieldForData(fields, data);
        }

        protected String getHrNameField(final String data) {
            return getFieldForData(Arrays.asList("HR_NAME", "HR_NAME_2", "HR_NAME_3_1"), data);
        }

        protected String[] getNameLines(final PersonalInfo pInfo) {
            if (pInfo._isPrintRealName &&
                    pInfo._isPrintNameAndRealName &&
                    !StringUtils.isBlank(pInfo._studentRealName + pInfo._studentName) &&
                    !pInfo._studentRealName.equals(pInfo._studentName)
            ) {
                final String printName1 = pInfo._studentRealName;
                final String printName2 = pInfo._studentName;
                return new String[] {printName1, printName2};
            }
            final String printName = pInfo._isPrintRealName ? pInfo._studentRealName : pInfo._studentName;
            return new String[] {printName};
        }

        protected void printName(final PersonalInfo personalInfo, final KNJSvfFieldInfo fi) {
            final String field = fi._fieldname + "1";
            final String field1 = fi._fieldname + "2";
            final String field2 = fi._fieldname + "3";

            if (personalInfo._isPrintRealName &&
                    personalInfo._isPrintNameAndRealName &&
                    !StringUtils.isBlank(personalInfo._studentRealName + personalInfo._studentName) &&
                    !personalInfo._studentRealName.equals(personalInfo._studentName)
            ) {
                final String printName1 = personalInfo._studentRealName;
                final String printName2 = personalInfo._studentName;
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(field1, fi._x2 - fi._x1, fi._height, fi._ystart1, fi._minnum, fi._maxnum);
                final double charSize1 = modify1.getCharSize(printName1);
                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(field2, fi._x2 - fi._x1, fi._height, fi._ystart2, fi._minnum, fi._maxnum);
                final double charSize2 = modify2.getCharSize(printName2);
                final double charSize = Math.min(charSize1, charSize2);
                svfVrAttribute(field1, "Size=" + charSize);
                svfVrAttribute(field2, "Size=" + charSize);
                svfVrsOut(field1, printName1);
                svfVrsOut(field2, printName2);
            } else {
                final String printName = personalInfo._isPrintRealName ? personalInfo._studentRealName : personalInfo._studentName;
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(field, fi._x2 - fi._x1, fi._height, fi._ystart, fi._minnum, fi._maxnum);
                final double charSize = modify.getCharSize(printName);
                svfVrAttribute(field, "Size=" + charSize);
                svfVrsOut(field, printName);
            }
        }

        protected boolean isNewForm(final Param param, final PersonalInfo pInfo) {
            boolean rtn = false;
            if (param._z010.in(Z010.tokyoto) || param.isTokubetsuShien()) {
                final int checkYear = 2013; // 切替年度
                if (NumberUtils.isDigits(pInfo._curriculumYear)) {
                    // 教育課程年度が入力されている場合
                    if (checkYear > Integer.parseInt(pInfo._curriculumYear)) {
                        rtn = false;
                    } else {
                        rtn = true;
                    }
                } else if (NumberUtils.isDigits(pInfo._entYear)) {
                    final int iEntYear = Integer.parseInt(pInfo._entYear);
                    if (checkYear > iEntYear) {
                        rtn = false;
                    } else if (checkYear <= iEntYear) {
                        if (NumberUtils.isDigits(pInfo._entYearGradeCd)) {
                            final int iAnnual = Integer.parseInt(pInfo._entYearGradeCd);
                            if ((checkYear + 0) == iEntYear && iAnnual >= 2 ||
                                (checkYear + 1) == iEntYear && iAnnual >= 3 ||
                                (checkYear + 2) == iEntYear && iAnnual >= 4) { // 転入生を考慮
                                rtn = false;
                            } else {
                                rtn = true;
                            }
                        } else {
                            rtn = true;
                        }
                    }
                }
            }
            return rtn;
        }

        protected int svfVrsOutNotBlank(final String field, final String data) {
            if (StringUtils.isBlank(data)) {
                return 0;
            }
            return svfVrsOut(field, data);
        }

        protected void printFuhakkouText(final Param param) {
            int length = getSvfFormFieldLength("FOOTER1", 110);
//            log.info(" footerl = " + length);
            if (length > 110) {
                final String text = "教科・科目の評定等、指導要録における指導に関する記録については、法令で定められた保存期間（卒業後" + param._seitoSidoYorokuHozonkikan + "年）が経過しているため、証明できません。";
                final List<String> tokenList = getTokenList(text, length, param());
                for (int i = 0; i < tokenList.size(); i++) {
                    final String token = tokenList.get(i);
                    svfVrsOut("FOOTER" + String.valueOf(i + 1), token);
                }
            } else {
                final String FUHAKKOU_TEXT1 = "　　教科・科目の評定等、指導要録における指導に関する記録については、法令で定められた保存期間（卒業後";
                final String FUHAKKOU_TEXT2 = "　　" + param._seitoSidoYorokuHozonkikan + "年）が経過しているため、証明できません。";
                svfVrsOut("FOOTER1", FUHAKKOU_TEXT1);
                svfVrsOut("FOOTER2", FUHAKKOU_TEXT2);
            }
        }

        protected static class KNJSvfFieldInfo {
            String _fieldname;
            int _x1;   //開始位置X(ドット)
            int _x2;   //終了位置X(ドット)
            int _height;  //フィールドの高さ(ドット)
            int _ystart;  //開始位置Y(ドット)
            int _ystart1;  //開始位置Y(ドット)フィールド1
            int _ystart2;  //開始位置Y(ドット)フィールド2
            int _minnum;  //最小設定文字数
            int _maxnum;  //最大設定文字数
            public KNJSvfFieldInfo(final String fieldname, final int x1, final int x2, final int height, final int ystart, final int ystart1, final int ystart2, final int minnum, final int maxnum) {
                //log.info(" fieldname = " + fieldname + ", x1 = " + x1 + ", x2 = " + x2 + ", height = " + height);
                _fieldname = fieldname;
                _x1 = x1;
                _x2 = x2;
                _height = height;
                _ystart = ystart;
                _ystart1 = ystart1;
                _ystart2 = ystart2;
                _minnum = minnum;
                _maxnum = maxnum;
            }
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
            if (null != _form._createFormFiles.get(formCreateFlg)) {
                return _form._createFormFiles.get(formCreateFlg).getName();
            }
            try {
                final SvfForm svfForm = new SvfForm(new File(_form._svf.getPath(form)));
                if (svfForm.readFile()) {

                    modifySvfForm(pInfo, svfForm, flgMap);

                    final File newFormFile = svfForm.writeTempFile();
                    _form._createFormFiles.put(formCreateFlg, newFormFile);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

            File newFormFile = _form._createFormFiles.get(formCreateFlg);
            if (null != newFormFile) {
                return newFormFile.getName();
            }
            return form;
        }

        // 使用する際はoverrideしてください
        /**
         *
         * @param pInfo
         * @param svfForm
         * @param flgMap
         * @return 修正フラグ
         */
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            log.warn("not implemented.");
            return false;
        }

        protected String modifyFormTankyu(String form, final PersonalInfo pInfo, final List<Gakuseki> gakusekiList) {
            final List<String> flgList = new ArrayList<String>();
            final String TANKYU = "TANKYU";

            final String mae = PersonalInfo.SOGOTEKI_NA_GAKUSHU_NO_JIKAN;
            final String ato = pInfo.getSogoSubclassname(param(), PersonalInfo.yearGakusekiMap(gakusekiList));
            if (!mae.equals(ato)) {
                flgList.add(TANKYU);
            }

            if (StringUtils.isEmpty(KNJA130CCommon.Util.mkString(flgList, "").toString().toString())) {
                return form;
            }
            final String formCreateFlg = form + KNJA130CCommon.Util.mkString(flgList, "").toString().toString();
            if (param()._isOutputDebug) {
                log.info(" form config Flg = " + formCreateFlg);
            }
            if (null != _form._createFormFiles.get(formCreateFlg)) {
                return _form._createFormFiles.get(formCreateFlg).getName();
            }
            try {
                final SvfForm svfForm = new SvfForm(new File(_form._svf.getPath(form)));

                if (svfForm.readFile()) {
                    if (flgList.contains(TANKYU)) {

                        for (final KoteiMoji koteiMoji : svfForm.getElementList(SvfForm.KoteiMoji.class)) {
                            if (koteiMoji._moji.contains(mae)) {
                                svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(koteiMoji._moji.replaceAll(mae, ato)));
                            }
                        }
                    }

                    File file = svfForm.writeTempFile();
                    if (null != file) {
                        _form._createFormFiles.put(formCreateFlg, file);
                        form = file.getName();
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            }
            return form;
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /*
     * 学校教育システム 賢者 [学籍管理] 生徒指導要録 学籍の記録 2004/08/18
     * yamashiro・組のデータ型が数値でも文字でも対応できるようにする 2006/04/13
     * yamashiro・潤オ文字が?と出力される不具合を修正 --NO001 ・編入のデータ仕様変更に対応 ( SCHREG_TRANSFER_DAT =>
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

        private KNJSvfFieldInfo _kana;
        private KNJSvfFieldInfo _gKana;
        private KNJSvfFieldInfo _name;
        private KNJSvfFieldInfo _gName;
        final int _addressMax;
        boolean _useSvfFieldInfo = false;
        final int pt9 = KNJSvfFieldModify.charSizeToPixel(9.0);
        final int pt14 = KNJSvfFieldModify.charSizeToPixel(14.0);
        final int nameMinimum24 = 24;
        final int kanaMinimum12 = 12;

        KNJA130_1(final Vrw32alp svf, final Param param) {
            super(svf, param);
            _addressMax = param()._z010.in(Z010.tokyoto) ? 2 : 3;
        }

        private String getForm(final Student student) {
            final String form;
            if (param().isTokubetsuShien()) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    form = "KNJA134H_11.frm";
                } else { // if (CHITEKI1_知的障害.equals(param()._chiteki)) {
                    form = "KNJA134H_1.frm";
                }
            } else {
//                if (param()._isYuushinkan) { // 湧心館専用フォーム
//                    if (4 < student._gradeRecNumber) {
//                        form = "KNJA130_5Y.frm";
//                    } else {
//                        form = "KNJA130_1Y.frm";
//                    }
//                } else if (param()._isKindaiHigh) {
                if (param()._z010.in(Z010.KINDAI)) {
                    form = "KNJA130_1K.frm";
                } else if (param()._z010.in(Z010.tokyoto)) {
                    form = "KNJA130_1TOKYO.frm";
                } else {
                    if (4 < student._gradeRecNumber) {
                        form = "KNJA130_5.frm";
                    } else {
                        form = "KNJA130_1.frm";
                    }
                }
            }
            final int maxnum1 = 48;
            final int maxnum2 = 100;
            int pt = pt9;
            int x1, x2, x2_2, kanaY, gkanaY, nameY, nameYPlus, gnameY;
            _useSvfFieldInfo = false;
            if (!param()._z010.in(Z010.fukuiken) && param().isTokubetsuShien()) {
                pt = KNJSvfFieldModify.charSizeToPixel(8.0);
                x1 = 749;
                x2 = 1550;
                x2_2 = 1750;
                kanaY = 925;
                gkanaY = 1887;
                nameY = 1080;
                nameYPlus = 40;
                gnameY = 2044;
            } else if (param()._z010.in(Z010.tokyoto)) {
                x1 = 689;
                x2 = 1574;
                x2_2 = 1702;
                kanaY = 1304;
                gkanaY = 2210;
                nameY = 1458;
                nameYPlus = 40;
                gnameY = 2324;
            } else {
                x1 = 1003;
                x2 = 1947;
                x2_2 = 2103;
                kanaY = 1590;
                gkanaY = 2790;
                nameY = 1763;
                nameYPlus = 50;
                gnameY = 2963;
                _useSvfFieldInfo = true;
            }
            if (_useSvfFieldInfo) {
                _kana = null;
                _gKana = null;
                _name = null;
                _gName = null;
            } else {
                _kana = new KNJSvfFieldInfo("KANA", x1, x2, pt, kanaY, -1, -1, kanaMinimum12, maxnum2);
                _gKana = new KNJSvfFieldInfo("GUARD_KANA", x1, x2_2, pt, gkanaY, -1, -1, kanaMinimum12, maxnum2);
                _name = new KNJSvfFieldInfo("NAME", x1, x2, pt14, nameY, nameY - nameYPlus, nameY + nameYPlus, nameMinimum24, maxnum1);
                _gName = new KNJSvfFieldInfo("GUARD_NAME", x1, x2_2, pt14, gnameY, gnameY - nameYPlus, gnameY + nameYPlus, nameMinimum24, maxnum1);
            }
            return form;
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<Gakuseki> pageGakusekiList) {
            String form = getForm(student);
            form = modifyForm1(form, student, pInfo);
            svfVrSetForm(form, 1);
            if (_useSvfFieldInfo) {
                final SvfField fKana = getSvfField("KANA");
                //log.info(" KANA = " + svfFieldKana + ", " + KNJSvfFieldModify.getStringLengthPixel(svfFieldKana.size(), svfFieldKana._fieldLength));
                _kana = new KNJSvfFieldInfo("KANA", fKana.x(), fKana.x() + KNJSvfFieldModify.getStringLengthPixel(fKana.size(), fKana._fieldLength), pt9, fKana.y(), -1, -1, kanaMinimum12, fKana._fieldLength);

                final SvfField fgKana = getSvfField("GUARD_KANA");
                //log.info(" GUARD_KANA = " + svfFieldgKana + ", " + KNJSvfFieldModify.getStringLengthPixel(svfFieldgKana.size(), svfFieldgKana._fieldLength));
                _gKana = new KNJSvfFieldInfo("GUARD_KANA", fgKana.x(), fgKana.x() + KNJSvfFieldModify.getStringLengthPixel(fgKana.size(), fgKana._fieldLength), pt9, fgKana.y(), -1, -1, kanaMinimum12, fgKana._fieldLength);

                final int fheight = param()._z010.in(Z010.fukuiken) ? 80 : pt14;
                final int nameMinimum = param()._z010.in(Z010.fukuiken) ? 20 : nameMinimum24;
                final SvfField fName1 = getSvfField("NAME1");
                final SvfField fName11 = getSvfField("NAME1_1");
                final SvfField fName21 = getSvfField("NAME2_1");
                _name = new KNJSvfFieldInfo("NAME", fName1.x(), fName1.x() + KNJSvfFieldModify.getStringLengthPixel(fName1.size(), fName1._fieldLength), fheight, fName1.y(), fName11.y(), fName21.y(), nameMinimum, fName1._fieldLength);

                final int fgheight = param()._z010.in(Z010.fukuiken) ? KNJSvfFieldModify.charSizeToPixel(12) : pt14;
                final SvfField fgName1 = getSvfField("GUARD_NAME1");
                final SvfField fgName11 = getSvfField("GUARD_NAME1_1");
                final SvfField fgName21 = getSvfField("GUARD_NAME2_1");
                _gName = new KNJSvfFieldInfo("GUARD_NAME", fgName1.x(), fgName1.x() + KNJSvfFieldModify.getStringLengthPixel(fgName1.size(), fgName1._fieldLength), fgheight, fgName1.y(), fgName11.y(), fgName21.y(), nameMinimum24, fgName1._fieldLength);
            }

            // 印刷処理
            printDetail(db2, student, pInfo, pageGakusekiList);
            int i = 0;
            boolean beforeDrop =  false;
            for (final Gakuseki gakuseki : pageGakusekiList) {

                // 留年以降を改ページします。
                if (beforeDrop) {
                    svfVrEndPage();
                    printDetail(db2, student, pInfo, pageGakusekiList);
                    i = 0;
                }

                i = getGradeColumnNum(student, i, gakuseki);
                printGradeRecDetail(db2, student, i, gakuseki);
                printStaff(i, gakuseki);

                beforeDrop = gakuseki._isDrop;
            }
            svfVrEndPage();
            nonedata = true;
        }

        final String FLG_TOKUSHI_SENKOUKA_TITLE = "FLG_TOKUSHI_TITLE";
        private String modifyForm1(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();

            if ("1".equals(param().property(Property.useSpecial_Support_School)) && param()._isSenkouka) {
                flgMap.put(FLG_TOKUSHI_SENKOUKA_TITLE, "1");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        /**
         *
         * @param pInfo
         * @param svfForm
         * @param flgMap
         * @return 修正フラグ
         */
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            boolean modify = false;
            if ("1".equals(flgMap.get(FLG_TOKUSHI_SENKOUKA_TITLE))) {
                for (final SvfForm.KoteiMoji moji : svfForm.getKoteiMojiListWithText("高等部生徒指導要録")) {
                    final int divideX = moji.getPoint()._x + (moji._endX - moji.getPoint()._x) * (3 + 4 / 2) / (9 + 8 / 2 /*スペース8個*/);
                    svfForm.move(moji, moji.replaceMojiWith("生徒指導要録").setX(divideX + 10).setEndX(moji._endX));
                    final KoteiMoji left = moji.addX(-30).setEndX(divideX - 10).setMojiPoint(100).addX(210);
                    svfForm.addKoteiMoji(left.replaceMojiWith("高等部").addY(-18));
                    svfForm.addKoteiMoji(left.replaceMojiWith("専攻科").addY(48));
                }
                modify = true;
            }
            return modify;
        }

        /**
         * {@inheritDoc}
         */
        protected int getGradeColumnNum(final Student student, int i, final Gakuseki gakuseki) {
            if (student.isGakunenSei(param())) {
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
                if (param()._isHeisetuKou) {
                    final int grade = Integer.parseInt(transGrade);
                    return (grade >= 4) ? String.valueOf(grade - 3) : String.valueOf(Integer.parseInt(transGrade));
                } else {
                    final String sGrade = NumberUtils.isDigits(transGrade) ? String.valueOf(Integer.parseInt(transGrade)) : " ";
                    return sGrade;
                }
            } else {
                return "1";
            }
        }

        /**
         * 変動しない(ページで)項目を印刷します。
         * @param student
         */
        private void printDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List pageGakusekiList) {
            final int column = param()._z010.in(Z010.tokyoto) ? 6 : 4;
            // デフォルト印刷
            printSvfDefault(db2, column, student, personalInfo);

            // 学校情報印刷
            printSchoolInfo(student);

            // 個人情報印刷
            printPersonalInfo(personalInfo);

            // 住所履歴印刷
            printAddressRec(personalInfo);

            // 保護者住所履歴印刷
            printGuardianAddressRec(personalInfo);

            // 異動履歴印刷
            printTransferRec(db2, student, personalInfo, pageGakusekiList);

            // 学籍等履歴項目名印刷
            printGradeRecTitle(student);

            // 進学先・就職先等印刷
            printAfterGraduatedCourse(personalInfo);
        }

        /**
         * 異動情報を印刷します。
         * @param param
         * @param student
         */
        private void printTransferRec(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List pageGakusekiList) {
            final String torikeshisen = (student.isGakunenSei(param())) ? "＝＝＝＝＝＝＝" : "＝＝＝";
            final int charWidth = 55;
            final int x = (param().isTokubetsuShien() ? 2737 : param()._z010.in(Z010.tokyoto) ? 2775 : 3315) + (student.isGakunenSei(param()) ? 0 : charWidth * 4);
            final int keta = KNJ_EditEdit.getMS932ByteLength(torikeshisen);
            svfVrAttribute("LINE1", "X=" + x + ", UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            svfVrAttribute("LINE2", "X=" + x + ", UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線
            if (student.isGakunenSei(param())) {
                svfVrsOut("ENTERDIV1", "第  学年　入学");
                svfVrsOut("ENTERDIV2", "第  学年編入学");
                svfVrsOut("TENNYU", "第  学年転入学");
            } else {
                svfVrsOut("ENTERDIV1", "入　学");
                svfVrsOut("ENTERDIV2", "編入学");
            }

            printNyugaku(db2, student, personalInfo);

            printJoseki(db2, personalInfo);

            int ia = 1; // 休学・留学回数
            for (final TransferRec tr : student._transferRecList) {
                int namecd2 = Integer.parseInt(tr._nameCode2);
                if (namecd2 == 1) { // 留学
                    if (printRyugakuKyugaku(student, personalInfo, pageGakusekiList, tr, ia)) {
                        ia++;
                    }
                }
                if (namecd2 == 2) { // 休学
                    if (printRyugakuKyugaku(student, personalInfo, pageGakusekiList, tr, ia)) {
                        ia++;
                    }
                }
            }
        }

        protected void printJoseki(final DB2UDB db2, final PersonalInfo personalInfo) {
            if (personalInfo.isTengaku() || personalInfo.isTaigaku() || personalInfo.isTenseki() || personalInfo.isJoseki()) {
                svfVrsOut("TRANSFER_DATE_2", setDateFormat(db2, h_format_JP(db2, personalInfo._grdDate), param()._year));
                final String sGrade = NumberUtils.isDigits(personalInfo._grdYearGradeCd) ? String.valueOf(Integer.parseInt(personalInfo._grdYearGradeCd)) : " ";
                svfVrsOut("tengaku_GRADE", sGrade);
                if (param()._z010.in(Z010.KINDAI)) {
                    if (personalInfo._grdReason != null)
                        svfVrsOut("TRANSFERREASON2_1", personalInfo._grdReason);
                    if (personalInfo._grdSchool != null)
                        svfVrsOut("TRANSFERREASON2_2", personalInfo._grdSchool); // NO007
                    if (personalInfo._grdAddr != null) {
                        svfVrsOut("TRANSFERREASON2_3", personalInfo._grdAddr); // NO007
                    }
                } else {
//                      if (tr._grdReason != null)
//                      svfVrsOut("TRANSFERREASON2_1", tr._grdReason);
                    if (personalInfo._grdSchool != null)
                        svfVrsOut("TRANSFERREASON2_1", personalInfo._grdSchool); // NO007
                }
                if ("1".equals(param().property(Property.useAddrField2))) {
                    final boolean useField2 = KNJ_EditEdit.getMS932ByteLength(personalInfo._grdAddr) > 50 || KNJ_EditEdit.getMS932ByteLength(personalInfo._grdAddr2) > 50;
                    if (null != personalInfo._grdAddr2) {
                        svfVrsOut("TRANSFERREASON2_2" + (useField2 ? "_2" : ""), personalInfo._grdAddr);
                        svfVrsOut("TRANSFERREASON2_3" + (useField2 ? "_2" : ""), personalInfo._grdAddr2);
                    } else {
                        svfVrsOut("TRANSFERREASON2_2" + (useField2 ? "_2" : ""), personalInfo._grdAddr);
                    }
                } else {
                    if (param()._z010.in(Z010.KINDAI)) {
                    } else {
                        if (personalInfo._grdAddr != null) {
                            svfVrsOut("TRANSFERREASON2_2", personalInfo._grdAddr); // NO007
                        }
                        if (param()._z010.in(Z010.tokyoto)) {
                            svfVrsOut("TRANSFERREASON2_3", personalInfo._grdReason);
                        }
                    }
                }
                String kubun = "";
                if (personalInfo.isTengaku()) { // 転学
                    kubun = "転学";
                } else if (personalInfo.isTaigaku()) { // 退学
                    kubun = "退学";
                } else if (personalInfo.isTenseki()) { // 転籍
                    kubun = "転籍";
                } else if (personalInfo.isJoseki()) { // 除籍
                    kubun = "除籍";
                }
                svfVrsOut("KUBUN", kubun);
            } else if (1 == personalInfo.grdDivInt()) { // 卒業
                svfVrsOut("TRANSFER_DATE_4", setDateFormat(db2, h_format_JP(db2, personalInfo._grdDate), param()._year));
//                if (!param()._isYuushinkan && personalInfo._grdNo != null)
//                    svfVrsOut("FIELD1", personalInfo._grdNo); // 卒業台帳番号
                if (personalInfo._grdNo != null) {
                    svfVrsOut("FIELD1", personalInfo._grdNo); // 卒業台帳番号
                }
            }
        }

        // 入学・編入学・転入学
        protected void printNyugaku(final DB2UDB db2, final Student student, final PersonalInfo personalInfo) {
            final String namecd2 = String.valueOf(personalInfo._entDiv);
            final String fieldEnterDiv;
            final StringBuffer enterDiv = new StringBuffer();
            if ("4".equals(namecd2)) {
                // 転入学を印字します。
                svfVrsOut("TRANSFER_DATE_1", setDateFormat(db2, h_format_JP(db2, personalInfo._entDate), param()._year));

                if (null != personalInfo._entSchool) {
                    svfVrsOut("TRANSFERREASON1_1", personalInfo._entSchool);
                }
                if ("1".equals(param().property(Property.useAddrField2))) {
                    final boolean useField2 = KNJ_EditEdit.getMS932ByteLength(personalInfo._entAddr) > 50 || KNJ_EditEdit.getMS932ByteLength(personalInfo._entAddr2) > 50;
                    if (null != personalInfo._entAddr2) {
                        svfVrsOut("TRANSFERREASON1_2" + (useField2 ? "_2" : ""), personalInfo._entAddr);
                        svfVrsOut("TRANSFERREASON1_3" + (useField2 ? "_2" : ""), personalInfo._entAddr2);
                    } else {
                        svfVrsOut("TRANSFERREASON1_2" + (useField2 ? "_2" : ""), personalInfo._entAddr);
                    }
                } else {
                    if (null != personalInfo._entAddr) {
                        svfVrsOut("TRANSFERREASON1_2", personalInfo._entAddr);
                    }
                    if (param()._z010.in(Z010.KINDAI)) {
                        if (null != personalInfo._entReason) {
                            svfVrsOut("TRANSFERREASON1_3", "(" + personalInfo._entReason + ")");
                        }
                    } else if (param()._z010.in(Z010.tokyoto)) {
                        svfVrsOut("TRANSFERREASON1_3", personalInfo._entReason);
                    }
                }
                fieldEnterDiv = "TENNYU";
                final String sGrade = NumberUtils.isDigits(personalInfo._entYearGradeCd) ? String.valueOf(Integer.parseInt(personalInfo._entYearGradeCd)) : " ";;
                enterDiv.append("第 ").append(sGrade).append("学年").append("転入学");
            } else if ("5".equals(namecd2)) {
                // 編入学を印字します。
                svfVrsOut("ENTERDATE2", setDateFormat(db2, h_format_JP(db2, personalInfo._entDate), param()._year));
                svfVrAttribute("LINE2", "X=10000"); // 打ち消し線消去
                if (param()._z010.in(Z010.KINDAI)) {
                    if (null != personalInfo._entReason) {
                        svfVrsOut("ENTERRESONS3", "(" + personalInfo._entReason + ")");
                    } else if (param()._z010.in(Z010.tokyoto)) {
                        svfVrsOut("TRANSFERREASON1_3", personalInfo._entReason);
                    }
                }
                fieldEnterDiv = "ENTERDIV2";
                final String sGrade = NumberUtils.isDigits(personalInfo._entYearGradeCd) ? String.valueOf(Integer.parseInt(personalInfo._entYearGradeCd)) : " ";;
                enterDiv.append("第 ").append(sGrade).append("学年").append("編入学");
            } else {
                // 入学を印字します。
                svfVrsOut("ENTERDATE1", setDateFormat(db2, h_format_JP(db2, personalInfo._entDate), param()._year));
                svfVrAttribute("LINE1", "X=10000"); // 打ち消し線消去
                if (param()._z010.in(Z010.KINDAI)) {
                    if (null != personalInfo._entReason) {
                        svfVrsOut("ENTERRESONS3", "(" + personalInfo._entReason + ")");
                    }
                } else if (param()._z010.in(Z010.tokyoto)) {
                    svfVrsOut("TRANSFERREASON1_3", personalInfo._entReason);
                }
                fieldEnterDiv = "ENTERDIV1";
                enterDiv.append("第 ").append(getTransGrade(personalInfo._entYearGradeCd)).append("学年").append("入　学");
            }
            if (student.isGakunenSei(param())) {
                svfVrsOut(fieldEnterDiv, enterDiv.toString());
            }
        }

        /**
         * 留学・休学を印字します。
         * @param tr
         */
        private boolean printRyugakuKyugaku(final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList, final TransferRec tr, final int ia) {
            if (!("1".equals(param().property(Property.seitoSidoYorokuZaisekiMae)) && Integer.parseInt(tr._sYear) < personalInfo.gakusekiMinYear() || PersonalInfo.gakusekiYearSet(pageGakusekiList).contains(tr._sYear))) {
                return false;
            } else if (param()._z010.in(Z010.tokyoto) && ia > 3) {
                return false;
            } else if (!param()._z010.in(Z010.tokyoto) && ia > 2) {
                return false;
            }
            svfVrsOut("TRANSFER_DATE3_" + ia, tr._sDateStr + MARK_FROM_TO + tr._eDateStr);
            final String reason = tr._reason == null ? "" : "／" + tr._reason;
            svfVrsOut("TRANSFERREASON3_" + ia + "_1", tr._name + reason);
            if (tr._place != null) {
                svfVrsOut("TRANSFERREASON3_" + ia + "_2", tr._place);
            }
            return true;
        }

        /**
         * 生徒住所履歴を印刷します。
         * @param student
         */
        private void printAddressRec(final PersonalInfo personalInfo) {
            final List<Address> printAddressRecList = Address.getPrintAddressRecList(personalInfo._addressList, _addressMax);
            for (int i = 1, num = printAddressRecList.size(); i <= num; i++) {
                final Address addressRec = printAddressRecList.get(i - 1);
                final boolean islast = i == num;
                printZipCode("ZIPCODE", i, addressRec._zipCode, islast, "ZIPCODELINE");

                final int n1 = KNJ_EditEdit.getMS932ByteLength(addressRec._address1);
                printAddr1("ADDRESS", i, addressRec._address1, n1, islast, "ADDRESSLINE");

                if (addressRec._isPrintAddr2) {
                    printAddr2("ADDRESS", i, addressRec._address2, n1, islast, "ADDRESSLINE");
                }
            }
        }

        private void printAddr1(final String field, int i, final String addr1, final int keta1, final boolean islast, final String linefield) {
            String p = null;
            if ("1".equals(param().property(Property.useAddrField2)) && 50 < keta1) {
                p = "_1_3";
            } else if (40 < keta1) {
                p = "_1_2";
            } else if (0 < keta1) {
                p = "_1_1";
            }
            if (p != null) {
                svfVrsOut(field + i + p, addr1);
                printAddressLine(addr1, islast, linefield + i + p);
            }
        }

        private void printAddr2(final String field, int i, final String addr2, final int keta1, final boolean islast, final String linefield) {
//            if (param()._isYuushinkan) {
//            } else {
                final int keta2 = KNJ_EditEdit.getMS932ByteLength(addr2);
                String p = null;
                if ("1".equals(param().property(Property.useAddrField2)) && (50 < keta2 || 50 < keta1)) {
                    p = "_2_3";
                } else if (40 < keta2 || 40 < keta1) {
                    p = "_2_2";
                } else if (0 < keta2) {
                    p = "_2_1";
                }
                if (p != null) {
                    svfVrsOut(field + i + p, addr2);
                    printAddressLine(addr2, islast, linefield + i + p);
                }
//            }
        }

        private Address getSameLineSchregAddress(final List<Address> printAddressRecList, final int i) {
            Address rtn = null;
            if (printAddressRecList.size() > i) {
                rtn = printAddressRecList.get(i);
            }
            return rtn;
        }

        /**
         * 保護者住所履歴を印刷します。
         * @param student
         */
        private void printGuardianAddressRec(final PersonalInfo personalInfo) {
            svfVrsOut("GRD_HEADER", personalInfo._addressGrdHeader);
            final String SAME_TEXT = "生徒の欄に同じ";
            final List<Address> printAddressRecList = Address.getPrintAddressRecList(personalInfo._addressList, _addressMax);
            final List<Address> guardPrintAddressRecList = Address.getPrintAddressRecList(personalInfo._guardianAddressList, _addressMax);
            if (Address.isSameAddressList(printAddressRecList, guardPrintAddressRecList)) {
                // 住所が生徒と同一
                svfVrsOut("GUARDIANADD1_1_1", SAME_TEXT);
                return;
            }
            for (int i = 1, num = guardPrintAddressRecList.size(); i <= num; i++) {
                final Address guardianAddressRec = guardPrintAddressRecList.get(i - 1);
                final boolean islast = i == num;
                final String guardianAddress1 = StringUtils.defaultString(guardianAddressRec._address1);
                final String guardianAddress2 = StringUtils.defaultString(guardianAddressRec._address2);

                final Address schregAddressRec = getSameLineSchregAddress(printAddressRecList, i - 1);
                boolean isSameAddress = Address.isSameAddress(schregAddressRec, guardianAddressRec);
                if (islast && !isSameAddress) {
                    final Address studentAddressMax = personalInfo.getStudentAddressMax();
                    // 最新の生徒住所とチェック
                    final String addr1 = null == studentAddressMax ? "" : StringUtils.defaultString(studentAddressMax._address1);
                    final String addr2 = null == studentAddressMax ? "" : StringUtils.defaultString(studentAddressMax._address2);
                    isSameAddress = addr1.equals(guardianAddress1) && addr2.equals(guardianAddress2);
                }

                if (isSameAddress) {
                    // 内容が生徒と同一
                    svfVrsOut("GUARDIANADD" + i + "_1_1", SAME_TEXT);
                    printAddressLine(SAME_TEXT, islast, "GUARDIANADDLINE" + i + "_1_1");
                } else {
                    printZipCode("GUARDZIP", i, guardianAddressRec._zipCode, islast, "GUARDZIPLINE");

                    final int keta1 = KNJ_EditEdit.getMS932ByteLength(guardianAddress1);
                    printAddr1("GUARDIANADD", i, guardianAddress1, keta1, islast, "GUARDIANADDLINE");

                    if (guardianAddressRec._isPrintAddr2) {
                        printAddr2("GUARDIANADD", i, guardianAddress2, keta1, islast, "GUARDIANADDLINE");
                    }
                }
            }
        }

        private void printZipCode(final String field, int i, final String zipCode, final boolean islast, final String linefield) {
            if (param()._printZipcd) {
                svfVrsOut(field + i, zipCode);
                printAddressLine(zipCode, islast, linefield + i);
            }
        }

        /**
         * かなを表示する
         */
        private void printKana(final String fieldKana, final String schKana, final KNJSvfFieldInfo fi) {

            final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldKana, fi._x2 - fi._x1, fi._height, fi._ystart, fi._minnum, fi._maxnum);
            final double charSize = modify.getCharSize(schKana);
            svfVrAttribute(fieldKana, "Size=" + charSize);
            svfVrAttribute(fieldKana, "Y=" + (int) modify.getYjiku(0, charSize));
            svfVrsOut(fieldKana, schKana);
        }

        /**
         * 名前を表示する。
         */
        private void printName(final String nameHistFirst, String name, final KNJSvfFieldInfo fi) {

            if (StringUtils.isBlank(nameHistFirst) || nameHistFirst.equals(name)) {
                // 履歴なしもしくは最も古い履歴の名前が現データの名称と同一
                final String fieldname = fi._fieldname + "1";
                final KNJSvfFieldModify modify = new KNJSvfFieldModify(fieldname, fi._x2 - fi._x1, fi._height, fi._ystart, fi._minnum, fi._maxnum);
                final double charSize = modify.getCharSize(name);
                svfVrAttribute(fieldname, "Size=" + charSize);
                svfVrAttribute(fieldname, "Y=" + (int) modify.getYjiku(0, charSize));
                svfVrsOut(fieldname, name);
            } else {
                final int keta = Math.min(KNJ_EditEdit.getMS932ByteLength(nameHistFirst), fi._maxnum);
                final String fieldname1 = fi._fieldname + "1_1";
                final KNJSvfFieldModify modify1 = new KNJSvfFieldModify(fieldname1, fi._x2 - fi._x1, fi._height, fi._ystart1, fi._minnum, fi._maxnum);
                final double charSize1 = modify1.getCharSize(nameHistFirst);
                svfVrAttribute(fieldname1, "Size=" + charSize1);
                svfVrAttribute(fieldname1, "Y=" + (int) modify1.getYjiku(0, charSize1));
                svfVrsOut(fieldname1, nameHistFirst);
                svfVrAttribute(fieldname1, "UnderLine=(0,3,5), Keta=" + keta); // 打ち消し線

                final String fieldname2 = fi._fieldname + "2_1";
                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(fieldname2, fi._x2 - fi._x1, fi._height, fi._ystart2, fi._minnum, fi._maxnum);
                final double charSize2 = modify2.getCharSize(name);
                svfVrAttribute(fieldname2, "Size=" + charSize2);
                svfVrAttribute(fieldname2, "Y=" + (int) modify2.getYjiku(0, charSize2));
                svfVrsOut(fieldname2, name);
            }
        }

        /**
         * 生徒情報を印刷します。
         * @param student
         * @param personalinfo
         */
        private void printPersonalInfo(final PersonalInfo personalInfo) {
            svfVrsOut("COURSENAME", personalInfo._courseName);
            svfVrsOut("MAJORNAME", personalInfo._majorName);

            printKana("KANA", personalInfo._schKana, _kana);

            printKana("GUARD_KANA", personalInfo._isPrintGuarantor ? personalInfo._guarantorKana : personalInfo._guardKana, _gKana);
            if (param()._simei != null) { // 漢字名指定あり？
                final String printName;
                if (personalInfo._isPrintRealName && personalInfo._isPrintNameAndRealName && !personalInfo._studentRealName.equals(personalInfo._studentName)) {
                    printName = personalInfo._studentRealName + personalInfo._studentName;
                } else if (personalInfo._isPrintRealName) {
                    printName = personalInfo._studentRealName;
                } else {
                    printName = personalInfo._studentName;
                }
                printName(personalInfo._studentNameHistFirst, printName, _name);

                final String guarName = personalInfo._isPrintGuarantor ? personalInfo._guarantorName : personalInfo._guardName;
                final String guarNameHistFirst = personalInfo._isPrintGuarantor ? personalInfo._guarantorNameHistFirst : personalInfo._guardNameHistFirst;
                printName(guarNameHistFirst, guarName, _gName);
            }
            svfVrsOut("BIRTHDAY", personalInfo._birthdayStr + "生");
            svfVrsOut("SEX", personalInfo._sex);
            svfVrsOut("J_GRADUATEDDATE_Y", personalInfo._finishDate);
            boolean printedInstallationDiv = false;
            if (!(param()._z010.in(Z010.KINDAI) || param()._z010.in(Z010.tokyoto)) && !param()._isDefinecodeSchoolMarkHiro) {
//                if (param()._z010.in(Z010.HOUSEI)) {
//                    final String ritu = personalInfo._installationDiv;
//                    if (null != ritu) {
//                        svfVrsOut("INSTALLATION_DIV",  ritu + "立");
//                        printedInstallationDiv = true;
//                    }
//                } else {
                svfVrsOut("INSTALLATION_DIV", personalInfo._installationDiv);
                    printedInstallationDiv = true;
//                }
            }

            final boolean juniorSchoolNameContainsSchoolKindName = "1".equals(param().property(Property.notPrintFinschooltypeName));

            // 入学前学歴の学校名編集
            if (param()._z010.in(Z010.KINDAI)) {
                printSvfFinSchool(printedInstallationDiv, personalInfo._juniorSchoolName, "中学校卒業");
            } else if (param()._z010.in(Z010.CHIBEN)) {
                printSvfFinSchool(printedInstallationDiv, personalInfo._juniorSchoolName, "卒業");
//            } else if (param()._z010.in(Z010.kumamoto)) {
//                if (juniorSchoolNameContainsSchoolKindName) {
//                    printSvfFinSchoolKumamoto(personalInfo._installationDiv, personalInfo._juniorSchoolName, "卒業");
//                } else {
//                    printSvfFinSchoolKumamoto(personalInfo._installationDiv, personalInfo._juniorSchoolName, personalInfo._finschoolTypeName + "卒業");
//                }
            } else {
                if (juniorSchoolNameContainsSchoolKindName) {
                    printSvfFinSchool(printedInstallationDiv, personalInfo._juniorSchoolName, "卒業");
                } else {
                    printSvfFinSchool(printedInstallationDiv, personalInfo._juniorSchoolName, personalInfo._finschoolTypeName + "卒業");
                }
            }
            // 編入学の場合事由、学校名、学校住所を表示
            if ("5".equals(String.valueOf(personalInfo._entDiv))) {
                if (param()._z010.in(Z010.tokyoto)) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(setKeta(personalInfo._entReason, 50));
                    stb.append(setKeta(personalInfo._entSchool, 50));
                    stb.append(setKeta(personalInfo._entAddr, 50));
                    svfVrsOut("keireki1", stb.toString());
                }
            }
        }

        /**
         *  SVF-FORMに入学前学歴の学校名を編集して印刷します。
         *  高校指導要録・中学指導要録・中等学校指導要録の様式１で使用しています。
         *  先頭から全角５文字以内に全角スペースが１個入っていた場合、
         *  全角スペースより前半の文字を○○○○○立と見なします。
         *  @param str1 例えば"千代田区　アルプ"
         *  @param koteiSrc 例えば"小学校卒業"
         */
        private void printSvfFinSchool(final boolean printedInstallationDiv, final String str1, final String koteiSrc) {
            final String schoolName;
            if (null == str1) {
                schoolName = "";
            } else {
                final char splitchar = param()._z010.in(Z010.KINDAI) ? ' ' : '　';
                final int i = str1.indexOf(splitchar);
                if (-1 < i && 5 >= i) {
                    final String ritu = str1.substring(0, i);
                    if (null != ritu) {
                        if (!printedInstallationDiv) {
                            if (param()._z010.in(Z010.KINDAI)) {
                                svfVrsOut("INSTALLATION_DIV",  ritu);
                            } else {
                                svfVrsOut("INSTALLATION_DIV",  ritu + "立");
                            }
                        }
                    }
                    schoolName = str1.substring(i + 1);
                } else {
                    schoolName = str1;
                }
            }
            final int schoolNameLen = KNJ_EditEdit.getMS932ByteLength(schoolName);

            final String kotei = StringUtils.defaultString(koteiSrc);
            final int koteiLen = KNJ_EditEdit.getMS932ByteLength(kotei);

            final String finschool1 = param()._isDefinecodeSchoolMarkHiro ? "FINSCHOOL1_HIRO" : "FINSCHOOL1";
            final String finschool2 = param()._isDefinecodeSchoolMarkHiro ? "FINSCHOOL2_HIRO" : "FINSCHOOL2";
            final String finschool3 = param()._isDefinecodeSchoolMarkHiro ? "FINSCHOOL3_HIRO" : "FINSCHOOL3";

            if (schoolNameLen == 0) {
                svfVrsOut(finschool1, kotei);
            } else if (schoolNameLen + koteiLen <= 40) {
                svfVrsOut(finschool1, schoolName + kotei);
            } else if(schoolNameLen + koteiLen <= 50) {
                svfVrsOut(finschool2, schoolName + kotei);
            } else {
                svfVrsOut(finschool2, schoolName);
                svfVrsOut(finschool3, kotei);
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
                final Vrw32alp svf,
                final String nameMstL001,
                final String str1,
                final String str2
        ) {
            final String schoolName;
            if (null == str1) {
                schoolName = "";
            } else {
                schoolName = nameMstL001 + str1;
            }
            final int schoolNameLen = KNJ_EditEdit.getMS932ByteLength(schoolName);

            final String kotei = (null == str2) ? "" : str2;
            final int koteiLen = KNJ_EditEdit.getMS932ByteLength(kotei);

            final String finschool1 = "FINSCHOOL1";
            final String finschool2 = "FINSCHOOL2";
            final String finschool3 = "FINSCHOOL3";

            if (schoolNameLen == 0) {
                svfVrsOut(finschool1, kotei);
            } else if (schoolNameLen + koteiLen <= 40) {
                svfVrsOut(finschool1, schoolName + kotei);
            } else if(schoolNameLen + koteiLen <= 50) {
                svfVrsOut(finschool2, schoolName + kotei);
            } else {
                svfVrsOut(finschool2, schoolName);
                svfVrsOut(finschool3, kotei);
            }
        }

        /**
         * 学校情報を印刷します。
         */
        private void printSchoolInfo(final Student student) {
            svfVrsOut("NAME_gakko1", student._schoolName1);
            if (!StringUtils.isBlank(param()._bunkouSchoolName)) {
                svfVrsOut("NAME_gakko2", "（" + param()._bunkouSchoolName + "）");
            }
            final String addr = param()._schoolAddress1 + param()._schoolAddress2;
            svfVrsOut("ADDRESS_gakko1" + ("1".equals(param().property(Property.useAddrField2)) && 50 < KNJ_EditEdit.getMS932ByteLength(addr) ? "_3" : 40 < KNJ_EditEdit.getMS932ByteLength(addr) ? "_2" : ""), addr);
            final String addrBunkouSrc = param()._bunkouSchoolAddress1 + param()._bunkouSchoolAddress2;
            if (!StringUtils.isBlank(addrBunkouSrc)) {
                final String addrBunkou = "（" + addrBunkouSrc + "）";
                svfVrsOut("ADDRESS_gakko2" + ("1".equals(param().property(Property.useAddrField2)) && 50 < KNJ_EditEdit.getMS932ByteLength(addrBunkou) ? "_3" : 40 < KNJ_EditEdit.getMS932ByteLength(addrBunkou) ? "_2" : ""), addrBunkou);
            }
            if (param()._printSchoolZipcd) {
                //if (param()._isKindaiHigh || param()._z010.in(Z010.Yuushinkan)) {
                if (param()._z010.in(Z010.KINDAI)) {
                    svfVrsOut("ZIPCODE", param()._schoolZipcode);
                } else {
                    svfVrsOut("ZIPCODE", "〒" + param()._schoolZipcode);
                }
            }
        }

        /**
         * 住所の取り消し線印刷
         * @param svf
         * @param i
         */
        private void printAddressLine(final String val, final boolean islast, final String field) {
            if (null == val || islast) {
                return;
            }
            svfVrAttribute(field, "UnderLine=(0,3,5), Keta=" + KNJ_EditEdit.getMS932ByteLength(val));
        }

        /**
         * 学籍履歴項目のタイトルを印刷します。
         * @param student
         */
        private void printGradeRecTitle(final Student student) {
            svfVrsOut("GRADENAME1", student._title);

            if (student.isGakunenSei(param())) {
                svfVrsOut("GRADENAME2", student._title);
            } else if (!student.isGakunenSei(param()) && param()._z010.in(Z010.tokyoto)) {
                svfVrsOut("GRADENAME2", "学年");
            }
        }

        /**
         * 学籍履歴を印刷します。
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(
                final DB2UDB db2,
                final Student student,
                final int i,
                final Gakuseki gakuseki
        ) {
            // 学年
            if (student.isKoumokuGakunen(param())) {
                svfVrsOut("GRADE2_" + i, gakuseki._gakunenSimple);
            } else {
                final String[] nendoArray = gakuseki.nendoArray();
                svfVrsOut("GRADE1_" + i, nendoArray[0]);
                svfVrsOut("GRADE2_" + i, nendoArray[1]);
                svfVrsOut("GRADE3_" + i, nendoArray[2]);
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            svfVrsOut(hrClassField + i, gakuseki._hrname);

            // 整理番号
            svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);

            //
            svfVrsOut("YEAR_" + i, setNendoFormat(db2, gakuseki._nendo, null));
            if (student.isGakunenSei(param()) || param()._z010.in(Z010.tokyoto)) {
                svfVrsOut("GRADE_" + i, gakuseki._gakunenSimple);
            }
        }

        private void printStaff(final int i, final Gakuseki gakuseki) {
            // 校長氏名
            printStaffName("1", i, true, gakuseki._principal, gakuseki._principal1Last, Staff.Null, gakuseki._principal1First);

            // 担任氏名
            printStaffName("2", i, false, Staff.Null, gakuseki._staff1Last, gakuseki._staff2Last, gakuseki._staff1First);

            final Staff stampPrincipalStaff;
            if (null == gakuseki._principal1Last._staffMst._staffcd) {
                stampPrincipalStaff = gakuseki._principal;
            } else if (StaffMst.Null == gakuseki._principal1First._staffMst || gakuseki._principal1First._staffMst == gakuseki._principal1Last._staffMst) {
                stampPrincipalStaff = gakuseki._principal1Last;
            } else {
                stampPrincipalStaff = gakuseki._principal1First;
            }

            if (null != param()._inei) {
                final String img1 = getImageFile(stampPrincipalStaff._staffMst._staffcd);
                if (img1 != null) {
                    svfVrsOut("STAFFBTM_1_" + i, img1); // 校長印
                }
                final String img2 = getImageFile(gakuseki._staff1Last._staffMst._staffcd);
                if (img2 != null) {
                    svfVrsOut("STAFFBTM_2_" + i, img2); // 担任印
                }
            }
        }

        private void printStaffName(final String j, final int i, final boolean isPrincipal, final Staff staff0, final Staff staff1Last, final Staff staff2Last, final Staff staff1First) {
            final String prop = null;
            final int keta = 26;
            final boolean isCheckStaff0 = isPrincipal;
            if (isCheckStaff0 && null == staff1Last._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                final String name = staff0.getNameString(prop, keta);
                svfVrsOut("STAFFNAME_" + j + "_" + i + (KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "_1" : ""), name);
            } else if ((StaffMst.Null == staff1First._staffMst || staff1First._staffMst == staff1Last._staffMst) && (!param()._z010.in(Z010.tokyoto) || (param()._z010.in(Z010.tokyoto) && StaffMst.Null == staff2Last._staffMst || staff2Last._staffMst == staff1Last._staffMst))) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                final List line = new ArrayList();
                line.addAll(staff1Last._staffMst.getNameLine(staff1Last._year, prop, keta));
                if (line.size() == 2) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", (String) line.get(0));
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", (String) line.get(1));
                } else {
                    final String name = staff1Last.getNameString(prop, keta);
                    svfVrsOut("STAFFNAME_" + j + "_" + i + (KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "_1" : ""), name);
                }
            } else {
                final List line = new ArrayList();
                if (param()._z010.in(Z010.tokyoto) && StaffMst.Null != staff2Last._staffMst && staff2Last._staffMst != staff1Last._staffMst) {
                    // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                    line.addAll(staff1Last._staffMst.getNameLine(staff1Last._year, prop, keta));
                    line.addAll(staff2Last._staffMst.getNameLine(staff2Last._year, prop, keta));
                } else {
                    // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                    line.addAll(staff1First.getNameBetweenLine(prop, keta));
                    line.addAll(staff1Last.getNameBetweenLine(prop, keta));
                }
                if (line.size() == 2) {
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_3", (String) line.get(0));
                    svfVrsOut("STAFFNAME_" + j + "_" + i + "_4", (String) line.get(1));
                } else {
                    // 上から順に表示。
                    for (int k = 0; k < 4 && k < line.size(); k++) {
                        svfVrsOut("STAFFNAME_" + j + "_" + i + "_" + (k + 2), (String) line.get(k));
                    }
                }
            }
        }

        /**
         * 進学先・就職先等の情報を印刷します。
         * @param student
         */
        private void printAfterGraduatedCourse(final PersonalInfo personalInfo) {
            final List<String> textList = personalInfo._afterGraduatedCourseTextList;
            for (int i = 0; i < textList.size(); i++) {
                final String line = textList.get(i);
                final String field = "AFTER_GRADUATION" + String.valueOf(i + 1) + (KNJ_EditEdit.getMS932ByteLength(line) > 50 ? "_2" : "") ;
                svfVrsOut(field, line);
            }
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String filename) {
            if (null == param()._documentroot) {
                return null;
            } // DOCUMENTROOT
            if (null == param()._imageDir) {
                return null;
            }
            if (null == param()._imageExt) {
                return null;
            }
            StringBuffer stb = new StringBuffer();
            stb.append(param()._documentroot);
            stb.append("/");
            stb.append(param()._imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(param()._imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

        /**
         * SVF-FORM 印刷処理 初期印刷
         */
        private void printSvfDefault(final DB2UDB db2, final int column, final Student student, final PersonalInfo pInfo) {
            try {
                final String setDateFormat = setDateFormat(db2, null, param()._year);
                svfVrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(null) + "生");
                svfVrsOut("J_GRADUATEDDATE_YE", setDateFormat);
                svfVrsOut("ENTERDATE1", setDateFormat);
                svfVrsOut("TRANSFER_DATE_1", setDateFormat);
                svfVrsOut("TRANSFER_DATE_2", setDateFormat);
                svfVrsOut("TRANSFER_DATE3_1", setDateFormat + MARK_FROM_TO + setDateFormat);
                svfVrsOut("TRANSFER_DATE_4", setDateFormat);
                for (int i = 0; i < column; i++) {
                    svfVrsOut("YEAR_" + (i + 1), setNendoFormat(db2, null, param()._year));
                }
                if (student.isGakunenSei(param())) {
                    if (pInfo._gakusekiList.size() > 0) {
                        final Gakuseki gakuseki = pInfo._gakusekiList.get(0);
                        if (NumberUtils.isDigits(gakuseki._gradeCd)) {
                            final String setNendoFormat = KNJ_EditDate.setNendoFormat(db2, null, gakuseki._year);
                            for (int g = 1; g <= Integer.parseInt(gakuseki._gradeCd); g++) {
                                svfVrsOut("YEAR_" + String.valueOf(g), setNendoFormat);
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
        private final int L1;
        /** 2列目までの行数 */
        private final int L2;
        /** 1ページの最大行数 (3列目までの行数) */
        private final int L3;

        private KNJSvfFieldInfo _name;

        private final String CLASSNAME;
        private final String CLASSNAME_CD;
        private final String SUBCLASSNAME;
        private final String EDU_DIV;
        private final String EDU_DIV_CD;
        private final String CREDIT;

        KNJA130_2(final Vrw32alp svf, final Param param) {
            super(svf, param);
            if (param()._z010.in(Z010.tokyoto)) {
                L1 = 39;
                L2 = 78;
                L3 = 114;
                CLASSNAME = "CLASSNAME1";
                SUBCLASSNAME = "SUBCLASSNAME1";
                EDU_DIV = "EDU_DIV1";
                CREDIT = "CREDIT1";
            } else {
                L1 = 35;
                L2 = 70;
                if (param().isTokubetsuShien()) {
                    L3 = 99;
                } else {
                    L3 = 101;
                }
                CLASSNAME = "CLASSNAME";
                SUBCLASSNAME = "SUBCLASSNAME";
                EDU_DIV = "EDU_DIV";
                CREDIT = "CREDIT";
            }
            CLASSNAME_CD = "CLASSNAME2";
            EDU_DIV_CD = "EDU_DIV2";
        }

        private String getForm(final Student student) {
            final String form;
            if (param().isTokubetsuShien()) {
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    form = "KNJA134H_12.frm";
                    _name = new KNJSvfFieldInfo("NAME", 397, 1197, KNJSvfFieldModify.charSizeToPixel(10.0), 404, 364, 444, 24, 48);
                } else { // if (CHITEKI1_知的障害.equals(param()._chiteki)) {
                    return null; // 出力なし
                }
            } else {
                if (param()._z010.in(Z010.KINDAI)) {
                    form = "KNJA130_2K.frm";
                    _name = new KNJSvfFieldInfo("NAME", 397, 1197, KNJSvfFieldModify.charSizeToPixel(12.0), 404, 364, 444, 24, 48);
                } else if (param()._z010.in(Z010.tokyoto)) {
                    form = "KNJA130_2TOKYO.frm";
                    _name = new KNJSvfFieldInfo("NAME", 2559, 3306, KNJSvfFieldModify.charSizeToPixel(12.0), 345, 315, 375, 24, 48);
                } else {
                    form = "KNJA130_2.frm";
                    _name = new KNJSvfFieldInfo("NAME", 397, 1197, KNJSvfFieldModify.charSizeToPixel(12.0), 404, 364, 444, 24, 48);
                }
            }
            return form;
        }

        final String FLG_TOKUSHI_SENKOUKA_REMOVE_SOGAKU = "FLG_TOKUSHI_SENKOUKA_REMOVE_SOGAKU";
        private String modifyForm2(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();

            if ("1".equals(param().property(Property.useSpecial_Support_School)) && param()._isSenkouka) {
                 // 専攻科目は総合的な学習の時間を表示しない
                flgMap.put(FLG_TOKUSHI_SENKOUKA_REMOVE_SOGAKU, "1");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        /**
         *
         * @param pInfo
         * @param svfForm
         * @param flgMap
         * @return 修正フラグ
         */
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            boolean modify = false;
            if ("1".equals(flgMap.get(FLG_TOKUSHI_SENKOUKA_REMOVE_SOGAKU))) {
                // 総合的な学習の時間単位数欄をカット
                for (final SvfForm.KoteiMoji moji : svfForm.getKoteiMojiListWithText("総合的な学習の時間")) {
                    svfForm.removeKoteiMoji(moji);
                    final Line l = svfForm.getNearestRightLine(moji._point);
                    if (null != l) {
                        svfForm.removeLine(l);
                    }
                    for (final SvfForm.Box box : svfForm.getElementList(SvfForm.Box.class)) {
                        if (box.contains(moji._point)) {
                            svfForm.removeBox(box);
                        }
                    }
                    break; // たかだか1個
                }
                // 総合的な学習のフィールドをカット
                for (final String fieldname : Arrays.asList("time", "CREDIT4", "CREDIT5", "CREDIT6_1", "CREDIT6_2", "CREDIT6_3")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                    }
                }
                modify = true;
            }
            return modify;
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {
            String form0 = getForm(student);
            form0 = modifyForm2(form0, student, personalInfo);
            final Map<Integer, List<Gakuseki>> pageGakusekiListMap = getPageGakusekiListMap2(db2, personalInfo, pageGakusekiList);
            Collection<String> beforeDropYears = Collections.emptySet();
            for (final Integer page : pageGakusekiListMap.keySet()) {
                final List<Gakuseki> gakusekiList = pageGakusekiListMap.get(page);

                final String form = modifyFormTankyu(form0, personalInfo, pageGakusekiList);
                svfVrSetForm(form, 4);

                printGradeRec(student, personalInfo, gakusekiList);

                setStudyDetail2(student, personalInfo, gakusekiList, beforeDropYears);

                beforeDropYears = CollectionUtils.intersection(personalInfo._dropYears, PersonalInfo.gakusekiYearSet(gakusekiList));
            }
        }

        /**
         * ページごとの年度リストのマップを得る。
         * @param student
         */
        private Map<Integer, List<Gakuseki>> getPageGakusekiListMap2(final DB2UDB db2, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {
            final int max = param()._z010.in(Z010.tokyoto) ? 6 : 4;
            final Map<Integer, List<Gakuseki>> rtn = new TreeMap();
            int page = 1;

            for (final Gakuseki gakuseki : Student.createGakusekiStudyRec(db2, personalInfo, pageGakusekiList, personalInfo._studyRecList, param())) {
                final Integer ip = new Integer(page);
                final List<Gakuseki> gakusekiListPerPage = getMappedList(rtn, ip);
                gakusekiListPerPage.add(gakuseki);
                if (gakuseki._isDrop || gakusekiListPerPage.size() > max) {
                    page += 1; // 改ページ
                }
            }
            return rtn;
        }

        private static StudyrecTotalClass getStudyrecTotalClass(final Student student, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final Collection beforeDropYears, final Param param, final String classcd) {
            if (null == classcd) {
                return null;
            }
            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = Student.createStudyRecTotal(personalInfo._studyRecList, personalInfo._dropYears, beforeDropYears, student.getGakunenSeiTengakuYears(personalInfo, param), param);
            final Collection<String> yearSet = PersonalInfo.gakusekiYearSet(gakusekiList);
            final Map<String, StudyRecSubclassTotal> studyRecSubclassPrintMap = getStudyRecSubclassPrintMap(studyRecSubclassMap, yearSet, beforeDropYears);
            final List<StudyRecSubclassTotal> studyRecSubclassList = new ArrayList<StudyRecSubclassTotal>(studyRecSubclassPrintMap.values());
            Collections.sort(studyRecSubclassList, new StudyRecSubclassTotal.StudyRecSubclassTotalComparator(param));

            final List<StudyrecTotalSpecialDiv> studyrecTotalSpecialDivList = getStudyrecTotalSpecialDivList2(param, studyRecSubclassList);
            StudyrecTotalClass rtn = null;
            sploop:
            for (final StudyrecTotalSpecialDiv studyrectotalSpecialDiv : studyrecTotalSpecialDivList) {

                for (final StudyrecTotalClass studyrectotalClass : studyrectotalSpecialDiv._classes) {

                    if (classcd.equals(studyrectotalClass.first().studyrec()._classMst._classcd)) {
                        rtn = studyrectotalClass;
                        break sploop;
                    }
                }
            }
            return rtn;
        }

        private static Map<String, Integer> getYearCreditsMap(final StudyrecTotalClass stc) {
            final Map<String, Integer> map = new TreeMap();
            if (null != stc) {
                for (StudyrecTotalSubclass sts : stc._subclasses) {
                    for (final StudyRecSubclassTotal st : sts._totals) {
                        for (final StudyRec s : st._studyrecList) {
                            if (null == map.get(s._year)) {
                                map.put(s._year, s._credit);
                            } else if (null != s._credit) {
                                map.put(s._year, new Integer(s._credit.intValue() + ((Integer) map.get(s._year)).intValue()));
                            }
                        }
                    }
                }
            }
            return map;
        }

        private void setStudyDetail2(final Student student, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final Collection<String> beforeDropYears) {
            StudyrecTotalClass studyrecClassTotal94 = null;
            if (null != beforeDropYears && !beforeDropYears.isEmpty()) {
                log.info(" beforeDropYears = " + beforeDropYears);
            }
            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = Student.createStudyRecTotal(personalInfo._studyRecList, personalInfo._dropYears, beforeDropYears, student.getGakunenSeiTengakuYears(personalInfo, param()), param());
            final Collection<String> yearSet = PersonalInfo.gakusekiYearSet(gakusekiList);
            final Map<String, StudyRecSubclassTotal> studyRecSubclassPrintMap = getStudyRecSubclassPrintMap(studyRecSubclassMap, yearSet, beforeDropYears);
            final List<StudyRecSubclassTotal> studyRecSubclassList = new ArrayList(studyRecSubclassPrintMap.values());
            Collections.sort(studyRecSubclassList, new StudyRecSubclassTotal.StudyRecSubclassTotalComparator(param()));

            final List<StudyrecTotalSpecialDiv> studyrecTotalSpecialDivList = getStudyrecTotalSpecialDivList2(param(), studyRecSubclassList);

            String specialDiv = "00";
            int linex = 0;
            for (final StudyrecTotalSpecialDiv studyrectotalSpecialDiv : studyrecTotalSpecialDivList) {
                if (studyrectotalSpecialDiv.isAllNotTarget()) {
                    continue;
                }

                final List list_specialname; // 普通・専門名のリスト
                if (param().useSpecialDiv()) {
                    specialDiv = studyrectotalSpecialDiv.first().studyrec()._classMst._specialDiv;
                    final String s_specialname = param().getSpecialDivName(isNewForm(param(), personalInfo), specialDiv);
                    list_specialname = toCharStringList(s_specialname); // 普通・専門名のリスト
                } else {
                    list_specialname = Collections.emptyList();
                }

                for (final StudyrecTotalClass studyrectotalClass : studyrectotalSpecialDiv._classes) {
                    // 総合的な学習の時間・留学は回避します。
                    if (studyrectotalClass.first().studyrec().kindForTotal(StudyRec.KIND_SOGO90, param()) ||
                        studyrectotalClass.first().studyrec().kindForTotal(StudyRec.KIND_ABROAD, param()) || studyrectotalClass.isAllNotTarget()) {
                        continue;
                    } else if (studyrectotalClass.first().studyrec().kindForTotal(StudyRec.KIND_SOGO94, param())) {
                        studyrecClassTotal94 = studyrectotalClass;
                        continue;
                    } else if (studyrectotalClass.first().studyrec().kindForTotal(StudyRec.KIND_SOGO92, param())) {
                        if (param()._isOutputDebug) {
                            log.info(" skip 92 " + studyrectotalClass);
                        }
                        continue;
                    }

                    final String classcd = studyrectotalClass.first().studyrec()._classMst._classcd; // 教科コードの保存
                    final List<String> list_classname = toCharStringList(studyrectotalClass.first().studyrec()._classMst._classname); // 教科名のリスト

                    final List<Object[]> list_subclass = new LinkedList();
                    for (final StudyrecTotalSubclass studyrectotalSubclass : studyrectotalClass._subclasses) {
                        if (studyrectotalSubclass.isAllNotTarget()) {
                            continue;
                        }

                        for (final StudyRecSubclassTotal sst : studyrectotalSubclass._totals) {
                            final Object[] arr = new Object[]{getSubclasscd(sst.studyrec(), param()), sst.studyrec()._subclassMst.subclassname(), sum(sst.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param())), sum(sst.compCreditList(yearSet, param()))};
                            list_subclass.add(arr);
                        }
                    }

                    // 教科名文字数と科目数で多い方を教科の行数にする。教科間の科目が続く場合は、空行を出力する [[最終行の扱い次第では代替処理その2を使用]]
                    final int nameline = list_classname.size() <= list_subclass.size() ? (list_subclass.size() + 1) : list_classname.size();

                    // 教科が次列に跨らないために、空行を出力する
                    if ((linex < L1 && L1 < linex + nameline) ||
                        (idxin(L1, linex, L2) && L2 < linex + nameline) ||
                        (idxin(L2, linex, L3) && L3 < linex + nameline)) {
                        final int max = (linex < L1) ? L1 : (linex < L2) ? L2 : L3;
                        for (int j = linex; j < max; j++) {
                            if (param().useSpecialDiv()) {
                                if (0 < list_specialname.size()) {
                                    svfVrsOut(EDU_DIV, str(list_specialname.remove(0))); // 普通・専門名
                                }
                            }
                            svfVrEndRecord(param(), student, personalInfo, specialDiv, null);
                            linex++;
                        }
                    }

                    for (int i = 0; i < nameline; i++) {
                        if (i < list_classname.size()) {
                            svfVrsOut(CLASSNAME, str(list_classname.get(i))); // 教科名
                        }
                        if (i < list_subclass.size()) {
                            final Object[] subclass = list_subclass.get(i);
//                            final String subclasscd = (String) subclass[0];
                            final String subclassname = (String) subclass[1];
                            final Integer credit = (Integer) subclass[2];
                            final Integer compCredit = (Integer) subclass[3];

                            svfFieldAttribute(SUBCLASSNAME, subclassname, linex); // SVF-FIELD属性変更のメソッド
                            svfVrsOut(SUBCLASSNAME, subclassname); // 科目名

                            String creVal = "";
                            if (credit != null) {
                                if (credit.intValue() == 0) {
                                    if (null != compCredit && compCredit.intValue() > 0) {
                                        // 履修単位数
                                        if (param()._z010.in(Z010.tokyoto)) {
                                            creVal = null; // 東京都・様式1裏は履修のみは単位の欄は空欄
                                        } else {
                                            creVal = "(" + compCredit.intValue() + ")";
                                        }
                                    } else {
                                        creVal = credit.toString();
                                    }
                                } else {
                                    // 修得単位数
                                    creVal = credit.toString();
                                }
                            }

                            boolean isOutputCredit = false;

//                            if (null != subclasscd) {
//                                final String substBikoZenbu = student._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ZENBU, null, null).toString();
//                                final String substBikoIchibu = student._gakushuBiko.getStudyrecSubstitutionBiko(subclasscd, StudyrecSubstitutionContainer.ICHIBU, null, null).toString();
//                                if (!StringUtils.isBlank(substBikoZenbu)) {
//                                    final List biko = getTokenList(substBikoZenbu, 10, 2); // 全部代替科目備考
//                                    for (int j = 0; j < biko.size(); j++) {
//                                        svfVrsOut(CREDIT + (2 + j), str(biko.get(j)));
//                                    }
//                                    isOutputCredit = true;   // 全部代替科目備考を表示する場合、修得単位数は表示しない
//                                } else if (!StringUtils.isBlank(substBikoIchibu)) {
//                                    svfVrsOut("CREDIT4_1", creVal);
//                                    final List biko = getTokenList(substBikoIchibu, 14, 2); // 一部代替科目備考
//                                    for (int j = 0; j < biko.size(); j++) {
//                                        svfVrsOut("CREDIT4_" + (2 + j), str(biko.get(j)));
//                                    }
//                                    isOutputCredit = true;
//                                }
//                            }
                            if (!isOutputCredit) {
                                svfVrsOut(CREDIT, creVal);
                            }
                        }

                        if (param().useSpecialDiv()) {
                            if (0 < list_specialname.size()) {
                                svfVrsOut(EDU_DIV, str(list_specialname.remove(0))); // 普通・専門名
                            }
                        }
                        svfVrEndRecord(param(), student, personalInfo, specialDiv, classcd);
                        linex++;
                    }

                    if (linex == L3) {
                        linex = 0;
                    }
                }

                // 普通・専門名文字数
                if (0 != list_specialname.size()) {
                    for (int i = 0; i < list_specialname.size(); i++) {
                        if (param().useSpecialDiv()) {
                            svfVrsOut(EDU_DIV, str(list_specialname.get(i))); // 普通・専門名
                        }
                        svfVrEndRecord(param(), student, personalInfo, specialDiv, null);
                        linex++;
                    }
                    // 普通・専門名のリストを削除する
                    list_specialname.clear();
                    if (linex == L3) {
                        linex = 0;
                    }
                }
            }
            printTotalCredits2(student, personalInfo, yearSet, studyRecSubclassMap);

            if (linex < L3) {
                for (int i = linex; i < L3 - 1; i++) {
                    svfVrEndRecord(param(), student, personalInfo, specialDiv, "");
                    linex++;
                }

                // 広島国際なら最後の1行にLHR (教科コード94の科目) を表示する。 (教科名無し)
                if (param()._z010.in(Z010.hirogaku) && studyrecClassTotal94 != null) {
                    for (final StudyrecTotalSubclass totalSubclass : studyrecClassTotal94._subclasses) {
                        for (final StudyRecSubclassTotal subclassTotal : totalSubclass._totals) {
                            final String credit = sum(subclassTotal.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param())) != null ? sum(subclassTotal.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param())).toString() : "";
                            svfFieldAttribute(SUBCLASSNAME, subclassTotal.studyrec()._subclassMst.subclassname(), linex); // SVF-FIELD属性変更のメソッド
                            svfVrsOut(SUBCLASSNAME, subclassTotal.studyrec()._subclassMst.subclassname());
                            svfVrsOut("CREDIT", credit);
                        }
                    }
                }

                svfVrEndRecord(param(), student, personalInfo, specialDiv, "");
            }
            nonedata = true;
        }

        private static <T extends Comparable<T>> Map<T, StudyRecSubclassTotal> getStudyRecSubclassPrintMap(final Map<T, StudyRecSubclassTotal> studyRecSubclassMap, final Collection<String> yearSet, final Collection<String> beforeDropYears) {
            final Map<T, StudyRecSubclassTotal> rtn = new TreeMap();
            for (final Map.Entry<T, StudyRecSubclassTotal> e : studyRecSubclassMap.entrySet()) {
                final StudyRecSubclassTotal total = e.getValue();
                if (total.hasRecord(yearSet, beforeDropYears)) {
                    rtn.put(e.getKey(), total);
                }
            }
            return rtn;
        }

        private void svfVrEndRecord(final Param param, final Student student, final PersonalInfo personalInfo, final String specialDiv, final String classcd) {
            printGradeRecSub(student, personalInfo);
            if (param().useSpecialDiv()) {
                svfVrsOut(EDU_DIV_CD, specialDiv);
            }
            svfVrsOut(CLASSNAME_CD, classcd); // 教科コード
            if (param._z010.in(Z010.tokyoto)) {
                final List token = getTokenList(student._shoken._htrainRemarkHdatCreditremark, 88, 5, param());
                for (int j = 0; j < token.size(); j++) {
                    svfVrsOutn("REMARK", j + 1, (String) token.get(j));
                }
            }
            svfVrEndRecord();
        }

        private static boolean idxin(final int minidx, final int i, final int length) {
            return minidx <= i && i < length;
        }

        private static List<String> toCharStringList(final String s) {
            final List rtn = new LinkedList();
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
        private void printGradeRec(final Student student, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList) {
            final String lastyear = PersonalInfo.getLastYear(gakusekiList);
            final int max = param()._z010.in(Z010.tokyoto) ? 6 : 4;
            int i = 1;
            printGradeRecDetailClear(student);
            for (final Gakuseki gakuseki : gakusekiList) {
                printGradeRecSub(student, personalInfo);
                if (student.isKoumokuGakunen(param()) && NumberUtils.isDigits(gakuseki._gradeCd)) {
                    i = Integer.parseInt(gakuseki._gradeCd);
                }
                final int j = getGradeColumnNum(student, i, gakuseki, max, 1);
                printGradeRecDetail(student, j, gakuseki);
                final boolean islastyear = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                if (!islastyear && gakuseki._isDrop) {
                    //printTurnOverThePages(student);
                } else if (!islastyear && max == i) {
                    //printTurnOverThePages(student);
                    i = 1;
                } else {
                    i++;
                }
            }
        }

//        /**
//         * 改ページします。
//         * @param svf
//         * @param student
//         */
//        private void printTurnOverThePages(final Student student) {
//            for (int j = 0; j < L3; j++) {
//                svfVrEndRecord(param(), student, null, null);
//            }
//            setForm(student);
//        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub(final Student student, final PersonalInfo personalInfo) {
            svfVrsOut("GRADENAME", student._title);

            printName(personalInfo, _name);
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetailClear(final Student student) {
            for (int i = 1; i <= 6; i++) {
                if (student.isKoumokuGakunen(param())) {
                    svfVrsOut("GRADE1_" + i, "");
                } else {
                    svfVrsOut("GRADE2_" + i, "");
                }
                // ホームルーム
                svfVrsOut("HR_CLASS1_" + i, "");
                svfVrsOut("HR_CLASS2_" + i, "");
                svfVrsOut("ATTENDNO_" + i, "");
            }
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail(final Student student, final int i, final Gakuseki gakuseki) {
            if (student.isKoumokuGakunen(param())) {
                svfVrsOut("GRADE1_" + i, gakuseki._gakunenSimple);
            } else {
                svfVrsOut("GRADE2_" + i, gakuseki._nendo);
            }
            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            svfVrsOut(hrClassField + i, gakuseki._hrname);
            svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        /**
         * 修得単位数総合計を集計後印字します。（総合的な学習の時間・小計・留学・合計）
         */
        private void printTotalCredits2(final Student student, final PersonalInfo personalInfo, final Collection yearSet, final Map<String, StudyRecSubclassTotal> studyRecSubclassMap) {

            final List subject90s = new ArrayList();
            final List subject90Comps = new ArrayList();
            final List subjects = new ArrayList();
            final List abroads = new ArrayList();
            final List totals = new ArrayList();
            final List totals92 = new ArrayList();

            for (final StudyRecSubclassTotal tot : studyRecSubclassMap.values()) {
                if (tot.studyrec().kindForTotal(StudyRec.KIND_SOGO90, param())) {
                    subject90s.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                    subject90Comps.addAll(tot.compCreditList(yearSet, param()));
                }
                if (tot.studyrec().kindForTotal(StudyRec.KIND_SYOKEI, param())) {
                    subjects.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                }
                if (tot.studyrec().kindForTotal(StudyRec.KIND_ABROAD, param())) {
                    abroads.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                }
                if (tot.studyrec().kindForTotal(StudyRec.KIND_TOTAL, param())) {
                    totals.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                }
                if (tot.studyrec().kindForTotal(StudyRec.KIND_SOGO92, param())) {
                    totals92.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                }
            }

            final String time;
            if (!subject90s.isEmpty()) {
                final Integer sum = sum(subject90s); // not null
                if (sum.intValue() == 0 && !subject90Comps.isEmpty() && sum(subject90Comps).intValue() > 0) {
                    time = "(" + String.valueOf(sum(subject90Comps)) + ")";
                } else {
                    time = String.valueOf(sum);
                }
            } else {
                if (!subject90Comps.isEmpty() && sum(subject90Comps).intValue() > 0) {
                    time = "(" + String.valueOf(sum(subject90Comps)) + ")";
                } else {
                    time = "0";
                }
            }
            svfVrsOutNotBlank("time", time);
            svfVrsOutNotBlank("SUBTOTAL", !subjects.isEmpty() ? String.valueOf(sum(subjects)) : "0");
            svfVrsOutNotBlank("ABROAD", !abroads.isEmpty() ? String.valueOf(sum(abroads)) : "0");
            svfVrsOutNotBlank("TOTAL", !totals.isEmpty() ? String.valueOf(sum(totals)) : "0");
            if (param().isTokubetsuShien()) {
                svfVrsOutNotBlank("INDEPENDENCE", !totals92.isEmpty() ? String.valueOf(sum(totals92)) : "0");
            }
        }

        /**
         * 科目名の文字数により文字ピッチ及びＹ軸を変更します。（SVF-FORMのフィールド属性変更）
         *
         * @param subclassname:科目名
         * @param line:出力行(通算)
         */
        private void svfFieldAttribute(final String field, final String subclassname, final int line) {
            int ln = line + 1;

            final int width; // フィールドの幅(ドット)
            final int height; // フィールドの高さ(ドット)
            final int ystart; // 開始位置(ドット)
            final int xL1;
            final int xL2;
            final int xL3;
            if (param().isTokubetsuShien()) {
                width = 1067 - 482;
                height = 1042 - 944;
                ystart = 944 - (1042 - 944);
                xL1 = 472;
                xL2 = 1496;
                xL3 = 2520;
            } else if (param()._z010.in(Z010.tokyoto)) {
                width = 1082 - 561;
                height = 1022 - 944;
                ystart = 944 - (1022 - 944);
                xL1 = 542;
                xL2 = 1566;
                xL3 = 2590;
            } else {
                width = 611;
                height = 136;
                ystart = 612;
                xL1 = 252;
                xL2 = 1416;
                xL3 = 2576;
            }
            svfobj.width = width;
            svfobj.height = height;
            svfobj.ystart = ystart;
            svfobj.minnum = 20; // 最小設定文字数
            svfobj.maxnum = 40; // 最大設定文字数
            try {
                final int fieldLength = getSvfFormFieldLength(field, 40);
                if (svfobj.maxnum < fieldLength) {
                    svfobj.maxnum = fieldLength;
                }
            } catch (Throwable e) {
                if (param()._isOutputDebugSvfOut) {
                    log.error("exception!", e);
                }
            }
            svfobj.setRetvalue(subclassname, (ln % L1 == 0) ? L1 : ln % L1);
            if (ln <= L1) {
                svfVrAttribute(field, "X=" + (xL1 + 21)); // 左列の開始Ｘ軸
            } else if (ln <= L2) {
                svfVrAttribute(field, "X=" + (xL2 + 21)); // 中列の開始Ｘ軸
            } else {
                svfVrAttribute(field, "X=" + (xL3 + 21)); // 右列の開始Ｘ軸
            }

            svfVrAttribute(field, "Y=" + svfobj.jiku); // 開始Ｙ軸
            svfVrAttribute(field, "Size=" + svfobj.size); // 文字サイズ
        }


        private static List<StudyrecTotalSpecialDiv> getStudyrecTotalSpecialDivList2(final Param param, final List<StudyRecSubclassTotal> studyRecSubclassList) {
            final List<StudyrecTotalSpecialDiv> rtn = new ArrayList();
            for (final StudyRecSubclassTotal studyrectotal : studyRecSubclassList) {
//                if (param()._isKyoto && (studyrectotal.isRisyuNomi(param()) || studyrectotal.isMirisyu(param()))) {
//                    // 京都府は単位不認定（履修のみ）もしくは履修のみの場合様式1裏に表示しない
//                    continue;
//                }
                StudyrecTotalSpecialDiv stsd = StudyrecTotalSpecialDiv.getStudyrecTotalSpecialDiv(studyrectotal.studyrec()._classMst._specialDiv, rtn);
                if (null == stsd) {
                    stsd = new StudyrecTotalSpecialDiv();
                    rtn.add(stsd);
                }
                StudyrecTotalClass stc = StudyrecTotalClass.getStudyrecTotalClass(param, studyrectotal.studyrec()._classMst, stsd._classes);
                if (null == stc) {
                    stc = new StudyrecTotalClass();
                    stsd._classes.add(stc);
                }
                StudyrecTotalSubclass sts = StudyrecTotalSubclass.getStudyrecTotalSubclass(param, studyrectotal.studyrec()._subclassMst, stc._subclasses);
                if (null == sts) {
                    sts = new StudyrecTotalSubclass();
                    stc._subclasses.add(sts);
                }
                sts._totals.add(studyrectotal);
            }
            return rtn;
        }

        private static class StudyrecTotalSubclass {
            final List<StudyRecSubclassTotal> _totals = new ArrayList();
            private StudyRecSubclassTotal first() {
                return _totals.get(0);
            }
            /** データがすべて留年した年度か */
            public boolean isAllNotTarget() {
                return false;
//                boolean isAllNotTarget = true;
//                for (final Iterator it = _totals.iterator(); it.hasNext();) {
//                    final StudyRecSubclassTotal studyrecSubclassTotal = (StudyRecSubclassTotal) it.next();
//                    if (!studyrecSubclassTotal.isAllNotTarget()) {
//                        isAllNotTarget = false;
//                    }
//                }
//                return isAllNotTarget;
            }

            private static StudyrecTotalSubclass getStudyrecTotalSubclass(final Param param, final SubclassMst subclassMst, final List<StudyrecTotalSubclass> studyRecTotalSubclassList) {
                StudyrecTotalSubclass rtn = null;
                for (final StudyrecTotalSubclass sts : studyRecTotalSubclassList) {
                    if (SubclassMst.compareOrder(param, sts.first().studyrec()._subclassMst, subclassMst) == 0) {
                        rtn = sts;
                        break;
                    }
                }
                return rtn;
            }
        }

        private static class StudyrecTotalClass {
            final List<StudyrecTotalSubclass> _subclasses = new ArrayList();

            private StudyRecSubclassTotal first() {
                return _subclasses.get(0).first();
            }

            /** データがすべて留年した年度か */
            public boolean isAllNotTarget() {
                return false;
//                boolean isAllNotTarget = true;
//                for (final Iterator it = _subclasses.iterator(); it.hasNext();) {
//                    final StudyrecTotalSubclass studyrectotalSubclass = (StudyrecTotalSubclass) it.next();
//                    if (!studyrectotalSubclass.isAllNotTarget()) {
//                        isAllNotTarget = false;
//                    }
//                }
//                return isAllNotTarget;
            }


            private static StudyrecTotalClass getStudyrecTotalClass(final Param param, final ClassMst classMst, final List<StudyrecTotalClass> studyRecTotalClassList) {
                StudyrecTotalClass rtn = null;
                for (final StudyrecTotalClass stc : studyRecTotalClassList) {
                    if (ClassMst.compareOrder(param, stc.first().studyrec()._classMst, classMst) == 0) {
                        rtn = stc;
                        break;
                    }
                }
                return rtn;
            }
        }

        private static class StudyrecTotalSpecialDiv {
            final List<StudyrecTotalClass> _classes = new ArrayList();

            private StudyRecSubclassTotal first() {
                return _classes.get(0).first();
            }
            /** データがすべて留年した年度か */
            public boolean isAllNotTarget() {
                return false;
//                boolean isAllNotTarget = true;
//                for (final Iterator it = _classes.iterator(); it.hasNext();) {
//                    final StudyrecTotalClass studyrectotalClass = (StudyrecTotalClass) it.next();
//                    if (!studyrectotalClass.isAllNotTarget()) {
//                        isAllNotTarget = false;
//                    }
//                }
//                return isAllNotTarget;
            }

            private static StudyrecTotalSpecialDiv getStudyrecTotalSpecialDiv(final String specialDiv, final List<StudyrecTotalSpecialDiv> studyRecTotalSpecialDivList) {
                StudyrecTotalSpecialDiv rtn = null;
                for (final StudyrecTotalSpecialDiv stc : studyRecTotalSpecialDivList) {
                    if (stc.first().studyrec()._classMst._specialDiv.equals(specialDiv)) {
                        rtn = stc;
                        break;
                    }
                }
                return rtn;
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

        private int MAX_LINE_PER_PAGE;

        private final int MAX_LINE_PER_PAGE2_TOKYO = 51;

        private KNJSvfFieldInfo _name;

        KNJA130_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
            if (param()._z010.in(Z010.tokyoto)) {
                _name = new KNJSvfFieldInfo("NAME", 571, 1288, KNJSvfFieldModify.charSizeToPixel(12.0), 363, 333, 393, 24, 64);
            } else {
                _name = new KNJSvfFieldInfo("NAME", 585, 1385, KNJSvfFieldModify.charSizeToPixel(12.0), 322, 282, 362, 24, 48);
            }
        }

        private String getForm(final Student student, final int flg) {
            final String form;
            MAX_LINE_PER_PAGE = 65;
            if (param().isTokubetsuShien()) { // && CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                if (param()._isSenkouka) {
                    form = "KNJA134A_13.frm";
                    MAX_LINE_PER_PAGE = 66;
                } else {
                    form = "KNJA134H_13.frm";
                }
            } else {
                if (param()._z010.in(Z010.KINDAI)) {
                    form = "KNJA130_3K.frm";
                } else if (param()._z010.in(Z010.tokyoto)) {
                    MAX_LINE_PER_PAGE = 55;
                    if (flg == 1) {
                        form = "KNJA130_5TOKYO.frm";
                    } else {
                        form = "KNJA130_4TOKYO.frm";
                    }
                } else {
                    form = "KNJA130_3.frm";
                }
            }
            return form;
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {
            int i = 1;
//            boolean hasZeroprintflg = false;
            PrintGakuseki printGakuseki = new PrintGakuseki();
            final List<PrintGakuseki> grademapList = new ArrayList();
            grademapList.add(printGakuseki);
            final int max = param()._z010.in(Z010.tokyoto) ? 6 : 4;
            final String lastyear = StringUtils.defaultString(PersonalInfo.getLastYear(pageGakusekiList));

            final List<Gakuseki> gakusekiStudyRecList = Student.createGakusekiStudyRec(db2, personalInfo, pageGakusekiList, personalInfo._studyRecList, param());
//            boolean includeZaisekimae = false;
            for (final Gakuseki gakuseki : gakusekiStudyRecList) {
                boolean zaisekimae = false;
                if ("1".equals(gakuseki._dataflg) && !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                    continue;
                } else if ("2".equals(gakuseki._dataflg)) {
//                    includeZaisekimae = true;
                    zaisekimae = true;
                    printGakuseki._includeZaisekimae = true;
                }
                // final int j = zaisekimae ? max : getGradeColumnNum(i, gakuseki, max, includeZaisekimae);
                 final int j = getGradeColumnNum(student, i, gakuseki, max, printGakuseki._includeZaisekimae ? 1 : 0);

                printGakuseki._yearList.add(gakuseki._year);
                printGakuseki._grademap.put(gakuseki._year, new Integer(j));
                printGakuseki._gakusekiMap.put(gakuseki._year, gakuseki);
                final boolean lastyearflg = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                if (zaisekimae) {
                    i++;
                } else if (!lastyearflg && gakuseki._isDrop) {
                    printGakuseki._dropGakuseki = gakuseki;
                    printGakuseki = new PrintGakuseki();
                    grademapList.add(printGakuseki);
//                    includeZaisekimae = false;
//                    if (hasZeroprintflg) {
//                        i = !NumberUtils.isDigits(gakuseki._gradeCd) ? -1 : Integer.parseInt(gakuseki._gradeCd);
//                        hasZeroprintflg = false;
//                    }
                } else if (!lastyearflg && max == i) {
                    printGakuseki = new PrintGakuseki();
                    grademapList.add(printGakuseki);
//                    includeZaisekimae = false;
                    i = 1;
                } else {
                    i++;
                }
//                if (_year0.equals(gakuseki._year)) {
//                    hasZeroprintflg = true; // 入学前を出力した。
//                }
            }

            for (int j = 0; j < grademapList.size(); j++) {
                final PrintGakuseki printgakuseki = grademapList.get(j);
                String form = getForm(student, 0);
                form = modifyFormTankyu(form, personalInfo, pageGakusekiList);
                svfVrSetForm(form, 4);
                printGradeRecSub3(student, personalInfo, printgakuseki);
                if (param()._isOutputDebug) {
                    log.info(" print gradeMap = " + printgakuseki._grademap);
                }
                Collection checkDropYears = CollectionUtils.intersection(personalInfo._dropYears, PersonalInfo.gakusekiYearSet(new ArrayList(printgakuseki._gakusekiMap.values())));

                printStudyDetail3(db2, student, personalInfo, pageGakusekiList, printgakuseki, checkDropYears);
            }
            nonedata = true;
        }

        /**
         * 学習の記録明細を印刷します。
         */
        private void printStudyDetail3(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList, final PrintGakuseki printGakuseki, final Collection<String> checkDropYears) {

            final Collection<String> studyRecYearSet = PersonalInfo.gakusekiYearSet(Student.createGakusekiStudyRec(db2, personalInfo, pageGakusekiList, personalInfo._studyRecList, param()));

            final Map<String, StudyRecSubclassTotal> studyRecSubclassMap = Student.createStudyRecTotal(personalInfo._studyRecList, personalInfo._dropYears, checkDropYears, student.getGakunenSeiTengakuYears(personalInfo, param()), param());
            final TreeSet<String> yearSet = new TreeSet(printGakuseki._grademap.keySet());
            final String minYear;
            final String maxYear;
            if (yearSet.isEmpty()) {
                minYear = null;
                maxYear = null;
            } else {
                minYear = yearSet.first();
                maxYear = yearSet.last();
            }

            final List<StudyRec> studyrecList = new ArrayList<StudyRec>();
            for (final StudyRec studyrec : personalInfo._studyRecList) {
                if (!yearSet.contains(studyrec._year)) {
                    continue;
                }
                if (param().isTokubetsuShien() && studyrec.kindForTotal(StudyRec.KIND_SOGO92, param())) {
                    if (param()._isOutputDebug) {
                        log.info(" skip 92 studyrec " + studyrec);
                    }
                    continue;
                }
                studyrecList.add(studyrec);
            }
            Collections.sort(studyrecList, new StudyRec.StudyRecComparator(param()));

            final List studyrecSpecialDivList = getStudyrecSpecialDivList3(studyrecList, param());

            List<OutputLine> outputLineList = getOutputLineList(student, personalInfo, minYear, maxYear, studyrecSpecialDivList);

//          for (final Iterator it = outputLineList.iterator(); it.hasNext();) {
//              final OutputLine outputLine = (OutputLine) it.next();
//              log.debug(" outputLine = " + outputLine);
//          }

            final OutputLine finalLine = outputLineList.get(outputLineList.size() - 1);
            outputLineList = outputLineList.subList(0, outputLineList.size() - 1);
            final List<List<OutputLine>> pageList = KNJA130CCommon.getPageList(outputLineList, MAX_LINE_PER_PAGE);
            for (int i = 0; i < pageList.size(); i++) {
                final List<OutputLine> currentOutputLineList = pageList.get(i);

                if (param()._z010.in(Z010.tokyoto) && i != 0 && i == pageList.size() - 1) {
                    svfVrSetForm(getForm(student, 1), 4);
                }

                for (final OutputLine outputLine : currentOutputLineList) {
                    printLine(student, personalInfo, pageGakusekiList, printGakuseki, studyRecSubclassMap, studyRecYearSet, outputLine);
                    svfVrEndRecord();
                }
            }

            printYearCredits3(getBiko(student, personalInfo, minYear, maxYear, null, _90), student, personalInfo, pageGakusekiList, printGakuseki, studyRecSubclassMap);

            printLine(student, personalInfo, pageGakusekiList, printGakuseki, studyRecSubclassMap, studyRecYearSet, finalLine);
            svfVrEndRecord();
            nonedata = true;
        }

        public List<OutputLine> getOutputLineList(final Student student, final PersonalInfo personalInfo, final String minYear, final String maxYear, final List<StudyrecSpecialDiv> studyrecSpecialDivList) {

            final List<OutputLine> outputLineList = new ArrayList();
            int linex = 0; // 行数
            String specialDiv = "";
            StudyrecClass studyrec94 = null;
            for (final StudyrecSpecialDiv studyrecSpecialDiv : studyrecSpecialDivList) {
                specialDiv = studyrecSpecialDiv.studyrec()._classMst._specialDiv;
                final String specialName = param().getSpecialDivName(isNewForm(param(), personalInfo), studyrecSpecialDiv.studyrec()._classMst._specialDiv);

                int lineSpecialDiv = 0; // 普通・専門毎の行数
                // 教科毎の表示
                for (final StudyrecClass studyrecClass : studyrecSpecialDiv._studyrecClassList) {

                    // 総合的な学習の時間・留学は回避します。
                    if (studyrecClass.studyrec().kindForTotal(StudyRec.KIND_SOGO90, param())) {
                        for (final StudyrecSubClass studyrecSubClass : studyrecClass._studyrecSubclassList) {

                            for (final StudyRec studyrec : studyrecSubClass._studyrecList) {
                                final String compVal = getRisyuTanniBiko(student, personalInfo, studyrec);
                                if (!"".equals(compVal)) {
                                    student._gakushuBiko.putRisyuTanniBiko(_90, studyrec._year, compVal);
                                }
                            }
                        }
                        continue;
                    } else if (studyrecClass.studyrec().kindForTotal(StudyRec.KIND_ABROAD, param())) {
                        continue;
                    } else if (studyrecClass.studyrec().kindForTotal(StudyRec.KIND_SOGO94, param())) {
                        studyrec94 = studyrecClass;
                        continue;
                    }

                    int lineClasscd = 0;
                    // 科目毎の表示
                    for (final StudyrecSubClass studyrecSubClass : studyrecClass._studyrecSubclassList) {

                        boolean hasDropValid = false;
                        for (final StudyRec studyRec : personalInfo._studyRecList) {
                            if (studyRec.getKeySubclasscd(param()).equals(studyrecSubClass.studyrec().getKeySubclasscd(param()))) {
                                if (personalInfo._dropYears.contains(studyRec._year) && null != studyRec._validFlg) {
                                    hasDropValid = true;
                                }
                            }
                        }

                        final OutputLine outputLine = new OutputLine(linex);
                        outputLineList.add(outputLine);
                        outputLine._studyrecList = studyrecSubClass._studyrecList;
                        outputLine._hasDropValid = hasDropValid;

                        for (final StudyRec studyrec : studyrecSubClass._studyrecList) {

                            outputLine._keySubclasscd = getSubclasscd(studyrec, param());
                            outputLine._subclassname = studyrec._subclassMst.subclassname();
                            outputLine._biko = getBiko(student, personalInfo, minYear, maxYear, studyrec, outputLine._keySubclasscd);
                        }
                        outputLine._classname = substr(studyrecClass.studyrec()._classMst._classname, lineClasscd, 1);
                        outputLine._classcd2 = studyrecClass.studyrec()._classMst._classcd;
                        lineClasscd++;
                        if (param().useSpecialDiv()) {
                            if (lineSpecialDiv < specialName.length()) {
                                outputLine._edudiv = substr(specialName, lineSpecialDiv, 1);
                            }
                            outputLine._edudiv2 = specialDiv;
                        }
                        lineSpecialDiv++;
                        linex++;
                    }

                    boolean outputNokori = false;
                    while (lineClasscd < StringUtils.defaultString(studyrecClass.studyrec()._classMst._classname).length()) {
                        final OutputLine outputLine = new OutputLine(linex);
                        outputLineList.add(outputLine);

                        if (param().useSpecialDiv()) {
                            if (lineSpecialDiv < specialName.length()) {
                                outputLine._edudiv = substr(specialName, lineSpecialDiv, 1);
                            }
                            outputLine._edudiv2 = specialDiv;
                            lineSpecialDiv++;
                        }
                        outputLine._classcd2 = studyrecClass.studyrec()._classMst._classcd;
                        outputLine._classname = substr(studyrecClass.studyrec()._classMst._classname, lineClasscd, 1);
                        lineClasscd++;
                        linex++;
                        outputNokori = true;
                    }
                    if (!outputNokori) {
                        final OutputLine outputLine = new OutputLine(linex);
                        boolean outputName = false;
                        if (param().useSpecialDiv()) {
                            if (lineSpecialDiv < specialName.length()) {
                                outputLine._edudiv = substr(specialName, lineSpecialDiv, 1);

                                outputName = true;
                            }
                            lineSpecialDiv++;
                        }
                        if (outputName || linex != MAX_LINE_PER_PAGE) {
                            if (param().useSpecialDiv()) {
                                outputLine._edudiv2 = specialDiv;
                            }
                            outputLine._classcd2 = studyrecClass.studyrec()._classMst._classcd;
                            outputLineList.add(outputLine);
                            linex++;
                        }
                    }
                }

                if (param().useSpecialDiv()) {
                    while (lineSpecialDiv < specialName.length()) {
                        final OutputLine outputLine = new OutputLine(linex);
                        outputLineList.add(outputLine);
                        outputLine._edudiv = substr(specialName, lineSpecialDiv, 1);
                        outputLine._edudiv2 = specialDiv;
                        lineSpecialDiv++;
                        linex++;
                    }
                }
            }

            final int currentPage;
            final int maxLine;
            if (param()._z010.in(Z010.tokyoto)) {
                if (linex <= MAX_LINE_PER_PAGE) {
                    currentPage = 1;
                    maxLine = currentPage * MAX_LINE_PER_PAGE + MAX_LINE_PER_PAGE2_TOKYO;
                } else {
                    currentPage = linex / MAX_LINE_PER_PAGE + (linex % MAX_LINE_PER_PAGE == 0 ? 0 : 1);
                    maxLine = (currentPage - 1) * MAX_LINE_PER_PAGE + MAX_LINE_PER_PAGE2_TOKYO;
                }
            } else {
                currentPage = linex / MAX_LINE_PER_PAGE + (linex % MAX_LINE_PER_PAGE == 0 ? 0 : 1);
                maxLine = (0 == currentPage ? 1 : currentPage)  * MAX_LINE_PER_PAGE;
            }
            // log.fatal(" linex = " + linex + ", currentPage = " + currentPage + ", maxLinePage = " + maxLine);
            if (!(currentPage > 0 && linex == maxLine)) {
                for (int i = 0, nokori = maxLine - linex; i < nokori; i++) { // --NO001
                    final OutputLine outputLine = new OutputLine(linex);
                    outputLineList.add(outputLine);
                    outputLine._edudiv2 = specialDiv;
                    outputLine._classcd2 = "";
                    linex++;
                }
            }

            // 広島国際なら最後の1行にLHR (教科コード94の科目) を表示する。 (教科名無し)
            if (param()._z010.in(Z010.hirogaku) && null != studyrec94) {
                final OutputLine outputLine1 = outputLineList.get(outputLineList.size() - 1);
                for (final StudyrecSubClass studyrecSubClass : studyrec94._studyrecSubclassList) {

                    outputLine1._studyrecList = studyrecSubClass._studyrecList;

                    for (final StudyRec studyrec : studyrecSubClass._studyrecList) {

                        if (studyrec._year == null) {
                            continue;
                        }
                        outputLine1._subclassname = studyrec._subclassMst.subclassname();
                    }
                }
            }

            return outputLineList;
        }

        private void printLine(
                final Student student,
                final PersonalInfo personalInfo,
                final List pageGakusekiList,
                final PrintGakuseki printGakuseki,
                final Map studyRecSubclassMap,
                final Collection studyRecYearSet,
                final OutputLine outputLine) {

            final boolean lastyearflg = printGakuseki._grademap.containsKey(StringUtils.defaultString(PersonalInfo.getLastYear(pageGakusekiList)));
            final boolean isPrintTotalCredits = param()._isPrintYoshiki2OmoteTotalCreditByPage || lastyearflg;

            final String CLASSNAME;
            final String CLASSNAME_CD = "CLASSNAME2";
            final String SUBCLASSNAME;
            final String EDU_DIV;
            final String EDU_DIV_CD = "EDU_DIV2";
            final String BIKO;

            if (param().isTokubetsuShien()) {
                CLASSNAME = "CLASSNAME";
                SUBCLASSNAME = "SUBCLASSNAME";
                EDU_DIV = "EDU_DIV";
                BIKO = "biko";
            } else if (param()._z010.in(Z010.tokyoto)) {
                CLASSNAME = "CLASSNAME1";
                SUBCLASSNAME = "SUBCLASSNAME1";
                EDU_DIV = "EDU_DIV1";
                BIKO = "biko1_";
            } else {
                CLASSNAME = "CLASSNAME";
                SUBCLASSNAME = "SUBCLASSNAME";
                EDU_DIV = "EDU_DIV";
                BIKO = "biko";
            }

            printGradeRecSub3(student, personalInfo, printGakuseki);
            svfVrsOut(EDU_DIV, outputLine._edudiv);
            svfVrsOut(EDU_DIV_CD, outputLine._edudiv2);

            svfVrsOut(CLASSNAME, outputLine._classname); // 教科名
            svfVrsOut(CLASSNAME_CD, outputLine._classcd2);
            svfVrsOut(SUBCLASSNAME, outputLine._subclassname);

            Integer pageTotalCredit = null;
            for (final StudyRec studyrec : outputLine._studyrecList) {
                if (null != studyrec._credit && (!personalInfo._dropYears.contains(studyrec._year) && !outputLine._hasDropValid || outputLine._hasDropValid && null != studyrec._validFlg) && !student.getGakunenSeiTengakuYears(personalInfo, param()).contains(studyrec._year)) {
                    pageTotalCredit = KNJA130CCommon.addNumber(pageTotalCredit, studyrec._credit);
                }
            }

            for (final StudyRec studyrec : outputLine._studyrecList) {
                if (student.getGakunenSeiTengakuYears(personalInfo, param()).contains(studyrec._year)) {
                    continue;
                }
                final String keysubclasscd = getSubclasscd(studyrec, param());
                printHyoteiTanni(personalInfo, printGakuseki, studyrec, isPrintTotalCredits, pageTotalCredit, (StudyRecSubclassTotal) studyRecSubclassMap.get(keysubclasscd), studyRecYearSet);
            }

            printSvfBiko(BIKO, outputLine._biko);

            if (personalInfo._isFuhakkou) {
                printFuhakkouText(param());
            }
        }

        // 学年ごとの出力
        private void printHyoteiTanni(final PersonalInfo personalInfo, final PrintGakuseki printGakuseki, final StudyRec studyrec, final boolean isPrintTotalCredits, final Integer pageTotalCredit, final StudyRecSubclassTotal studyobj, final Collection studyRecYearSet) {

            final String CREDIT = param()._z010.in(Z010.tokyoto) ? "CREDIT1_" : "CREDIT";
            final String SLASH = param()._z010.in(Z010.tokyoto) ? "SLASH1_" : null;
            final String LASTCREDIT = param()._z010.in(Z010.tokyoto) ? "CREDIT1" : "CREDIT";
            final String GRADES = param()._z010.in(Z010.tokyoto) ? "GRADES1_" : "GRADES";

            final Integer column = (Integer) printGakuseki._grademap.get(studyrec._year);
            if (null != column && column.intValue() != 0) {
                final int intColumn = column.intValue();
//                if (param()._isKyoto && studyrec.isMirisyu(param())) {
//                    nonedata = true;
//                } else {
                    if (personalInfo._isFuhakkou) {
                        // 不発行は評定印字しない
                    } else if (null != studyrec._grades) {
                        if ("1".equals(param().property(Property.seitoSidoYorokuHyotei0ToBlank)) && studyrec._grades.intValue() == 0) {
                        } else if (param()._d065Name1List.contains(studyrec.getKeySubclasscd(param()))) {
                            svfVrsOut(GRADES + intColumn, param()._d001Abbv1Map.get(studyrec._grades));
                        } else {
                            svfVrsOut(GRADES + intColumn, studyrec._grades.toString()); // 評定
                        }
                        nonedata = true;
                    }
                    if (param()._z010.in(Z010.tokyoto) && (null == studyrec._credit || 0 == studyrec._credit.intValue()) && null != studyrec._compCredit && studyrec._compCredit.intValue() > 0) {
                        svfVrsOut(SLASH + intColumn, "／"); // 履修のみ（修得単位なしかつ履修単位あり）は斜線
                        nonedata = true;
                    } else if (null != studyrec._credit) {
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
                        svfVrsOut(CREDIT + intColumn, creditVal); // 単位
                        nonedata = true;
                    }
//                }
            }

            if (isPrintTotalCredits) {
                if (param()._isPrintYoshiki2OmoteTotalCreditByPage) {
                    if (null != pageTotalCredit) {
                        svfVrsOut(LASTCREDIT, pageTotalCredit.toString()); // 科目別修得単位数
                    }
                } else {
//                  final Integer substitutionIchibuCredit = student._gakushuBiko.getStudyrecSubstitutionCredit(subclasscd, StudyrecSubstitutionContainer.ICHIBU);
                    final boolean creditHasValue = null != studyobj && null != sum(studyobj.creditList(studyRecYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param())) && sum(studyobj.creditList(studyRecYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param())).intValue() != 0;
//                    final boolean substitutionIchibuCreditHasValue = null != substitutionIchibuCredit && substitutionIchibuCredit.intValue() != 0;
//                    if (creditHasValue || substitutionIchibuCreditHasValue) {
                    if (creditHasValue) {
//                        final int credit = (creditHasValue ? studyobj._credit.intValue() : 0) + (substitutionIchibuCreditHasValue ? substitutionIchibuCredit.intValue() : 0);
                        final int credit = (creditHasValue ? sum(studyobj.creditList(studyRecYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param())).intValue() : 0);
                        svfVrsOut(LASTCREDIT, String.valueOf(credit)); // 科目別修得単位数
                    }
                }
            }
        }

        private void printSvfBiko(final String fieldBIKO, final String biko) {
            if (param()._z010.in(Z010.tokyoto)) {
                for (int j = 1; j <= 5; j++) {
                    svfVrsOut(fieldBIKO + j, ""); // クリア処理
                }
                final int len = KNJ_EditEdit.getMS932ByteLength(biko);
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
                    svfVrsOut(fieldBIKO + i, biko);
                }
            } else {
                svfVrsOut(fieldBIKO, biko);
            }
        }

        private String substr(final String s, final int idx, final int c) {
            if (null == s || s.length() <= idx) {
                return null;
            }
            return s.substring(idx, idx + Math.min(s.length() - idx, c));
        }

        private String getBiko(final Student student, final PersonalInfo personalInfo, final String minYear, final String maxYear, final StudyRec studyrec, final String keysubclasscd) {
            if (null != studyrec) {
                final String compVal = getRisyuTanniBiko(student, personalInfo, studyrec);
                if (!"".equals(compVal)) {
                    student._gakushuBiko.putRisyuTanniBiko(keysubclasscd, studyrec._year, compVal);
                }
            }
            final String gakusyuubiko = student._gakushuBiko.getStudyrecBiko(keysubclasscd, minYear, maxYear).toString();
            final String substitutionBikoZenbu = student._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, StudyrecSubstitutionContainer.ZENBU, minYear, maxYear).toString();
            final String substitutionBikoIchibu = student._gakushuBiko.getStudyrecSubstitutionBiko(keysubclasscd, StudyrecSubstitutionContainer.ICHIBU, minYear, maxYear).toString();
            final String rishuTanniBiko = student._gakushuBiko.getRisyuTanniBiko(keysubclasscd, minYear, maxYear).toString();
            final String biko = rishuTanniBiko.toString() + gakusyuubiko  + substitutionBikoZenbu + substitutionBikoIchibu;
            return biko;
        }

        private String getRisyuTanniBiko(final Student student, final PersonalInfo personalInfo, final StudyRec studyrec) {
            final String rtn;
            final String head = getRisyuTanniBikoHead(student, personalInfo, studyrec); // 第○学年 or ○○○年度
            final String compCre = null == studyrec._compCredit ? "" : studyrec._compCredit.toString();
            if (studyrec.isMirisyu(param())) {
                // 未履修の場合の備考処理
                if (null != param()._mirishuRemarkFormat) {
                    rtn = formatRemark(param()._mirishuRemarkFormat, head, compCre);
                } else {
                    rtn = "";
                }
            } else if (studyrec.isRisyuNomi(param())) {
                // 履修のみの場合の備考処理
                if (null != param()._rishunomiRemarkFormat) {
                    rtn = formatRemark(param()._rishunomiRemarkFormat, head, compCre);
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

        private String getRisyuTanniBikoHead(final Student student, final PersonalInfo personalInfo, final StudyRec studyrec) {
            final Gakuseki gakuseki = personalInfo.getGakuseki(studyrec._year, studyrec._annual);
            if (gakuseki == null) {
                return null;
            }
            final String head = student.isKoumokuGakunen(param()) ? gakuseki._gradeName2 : gakuseki.getNendo2();
            return StringUtils.defaultString(head);
        }

        /**
         * 年度・学年別修得単位数を印字します。（総合的な学習の時間・小計・留学・合計）
         * @param gakusyuubikomap
         * @param lastyearflg
         * @param studyrecyear
         * @param isGrd
         * @param grademap
         */
        private void printYearCredits3(final String biko, final Student student, final PersonalInfo personalInfo, final List pageGakusekiList, final PrintGakuseki printGakuseki, final Map<String, StudyRecSubclassTotal> studyRecSubclassMap) {

            final Map<String, Integer> grademap = printGakuseki._grademap;
            final boolean lastyearflg = grademap.containsKey(StringUtils.defaultString(PersonalInfo.getLastYear(pageGakusekiList)));
            final boolean isPrintTotalCredits = param()._isPrintYoshiki2OmoteTotalCreditByPage || lastyearflg;

            final Collection chkDropYears = new HashSet(personalInfo._dropYears);
            final Collection chkDropShowYears = Student.getDropShowYears(param(), personalInfo);
            final Collection gakunenSeiTengakuYears = student.getGakunenSeiTengakuYears(personalInfo, param());

            final boolean isGrd = student.isGrd(personalInfo);
            String subclassname92 = null;
            final List totals92 = new ArrayList();
            for (final String year : grademap.keySet()) {

                final int intColumn = grademap.get(year).intValue();

                final List subject90Alls = new ArrayList();
                final List subject90s = new ArrayList();
                final List subjects = new ArrayList();
                final List abroads = new ArrayList();
                final List totals = new ArrayList();
                final Set thisYearSet = Collections.singleton(year);

                if (!gakunenSeiTengakuYears.contains(year)) {
                    for (final StudyRecSubclassTotal tot : studyRecSubclassMap.values()) {

                        final boolean is90 = tot.studyrec().kindForTotal(StudyRec.KIND_SOGO90, param());
                        if (is90) {
                            subject90Alls.addAll(tot.creditList(thisYearSet, CheckDropYearsFlg._ALL, param()));
                            subject90s.addAll(tot.creditList(thisYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                        }
                        final boolean isSyokei = tot.studyrec().kindForTotal(StudyRec.KIND_SYOKEI, param());
                        if (isSyokei) {
                            subjects.addAll(tot.creditList(thisYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                        }
                        final boolean isAbroad = tot.studyrec().kindForTotal(StudyRec.KIND_ABROAD, param());
                        if (isAbroad) {
                            abroads.addAll(tot.creditList(thisYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                        }
                        final boolean isTotal = tot.studyrec().kindForTotal(StudyRec.KIND_TOTAL, param());
                        if (isTotal) {
                            totals.addAll(tot.creditList(thisYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                        }
                        final boolean is92 = tot.studyrec().kindForTotal(StudyRec.KIND_SOGO92, param());
                        if (is92) {
                            subclassname92 = tot.studyrec()._subclassMst.subclassname();
                            totals92.addAll(tot.creditList(thisYearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                        }
                        if (param()._isOutputDebugSeiseki) {
                            log.info(" 90 = " + is90 + ",shokei = " + isSyokei + ",abroad = " + isAbroad + ",total =  " + isTotal + ", total = " + tot);
                        }
                    }
                }

                final String sgj = (!subject90Alls.isEmpty()) ? sum(subject90Alls).toString() : param()._z010.in(Z010.tokyoto) ? "0" : null;
                svfVrsOutNotBlank("tani_" + intColumn + "_sgj", sgj);
                final boolean dropContains = chkDropYears.contains(year) || chkDropShowYears.contains(year);
                final String rg = (!abroads.isEmpty()) ? sum(abroads).toString() : param()._z010.in(Z010.KINDAI) && !totals.isEmpty() ? "0" : dropContains ? null : "0";
                svfVrsOutNotBlank("tani_" + intColumn + "_rg", rg);
                final String sk = (!subjects.isEmpty()) ? sum(subjects).toString() : dropContains ? null : isGrd ? "0" : null;
                svfVrsOutNotBlank("tani_" + intColumn + "_sk", sk);
                final String gk = (!totals.isEmpty()) ? sum(totals).toString() : dropContains ? null : isGrd ? "0" : null;
                svfVrsOutNotBlank("tani_" + intColumn + "_gk", gk);
            }
            if (!totals92.isEmpty()) {
                final String tanni92 = sum(totals92).toString();
                final String remark92 = StringUtils.defaultString(subclassname92) + tanni92 + "単位含む";
                final int len = KNJ_EditEdit.getMS932ByteLength(remark92);
                svfVrsOut("biko_total" + String.valueOf(len <= 40 ? "" : len <= 60 ? "2" : "3"), remark92);
            }
            final int bikoLen = KNJ_EditEdit.getMS932ByteLength(biko);
            final int bikoSgjSize = getSvfFormFieldLength("biko_sgj", 0);
            final int bikoSgj2Size = getSvfFormFieldLength("biko_sgj2", 0);
            final int bikoSgj3Size = getSvfFormFieldLength("biko_sgj3", 0);
            final int bikoSgj4Size = getSvfFormFieldLength("biko_sgj4_1", 0);
            String bikoField = "biko_sgj";
            if (bikoSgjSize > 0 && bikoSgjSize < bikoLen) {
                if (bikoSgj2Size > bikoSgjSize) {
                    bikoField = "biko_sgj2";
                    if (bikoSgj2Size < bikoLen) {
                        if (bikoSgj3Size > bikoSgj2Size) {
                            bikoField = "biko_sgj3";
                            if (bikoSgj3Size < bikoLen) {
                                if (bikoSgj4Size > bikoSgj3Size) {
                                    bikoField = "biko_sgj4_1";
                                }
                            }
                        }
                    }
                }
            }
            svfVrsOut(bikoField, biko);

            if (isPrintTotalCredits) {
                final int intColumn = (param()._z010.in(Z010.tokyoto) ? 6 : 4) + 1;
                final TreeSet yearSet = new TreeSet(grademap.keySet());

                final List subject90s = new ArrayList();
                final List subjects = new ArrayList();
                final List abroads = new ArrayList();
                final List totals = new ArrayList();

                for (final Iterator it = studyRecSubclassMap.values().iterator(); it.hasNext();) {
                    final StudyRecSubclassTotal tot = (StudyRecSubclassTotal) it.next();
                    if (tot.studyrec().kindForTotal(StudyRec.KIND_SOGO90, param())) {
                        subject90s.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                    }
                    if (tot.studyrec().kindForTotal(StudyRec.KIND_SYOKEI, param())) {
                        subjects.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                    }
                    if (tot.studyrec().kindForTotal(StudyRec.KIND_ABROAD, param())) {
                        abroads.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                    }
                    if (tot.studyrec().kindForTotal(StudyRec.KIND_TOTAL, param())) {
                        totals.addAll(tot.creditList(yearSet, CheckDropYearsFlg._CHECK_ENABLE_FLG, param()));
                    }
                }

                svfVrsOutNotBlank("tani_" + intColumn + "_sgj", (!subject90s.isEmpty()) ? String.valueOf(sum(subject90s)) : param()._z010.in(Z010.tokyoto) ? "0" : null);
                svfVrsOutNotBlank("tani_" + intColumn + "_sk", (!subjects.isEmpty()) ?  String.valueOf(sum(subjects)) : isGrd ? "0" : null);
                svfVrsOutNotBlank("tani_" + intColumn + "_rg", (!abroads.isEmpty()) ? String.valueOf(sum(abroads)) : (param()._z010.in(Z010.tokyoto) || isGrd) ? "0" : null);
                svfVrsOutNotBlank("tani_" + intColumn + "_gk", (!totals.isEmpty()) ? String.valueOf(sum(totals)) : isGrd ? "0" : null);
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub3(final Student student, final PersonalInfo personalInfo, final PrintGakuseki printgakuseki) {
            svfVrsOut("GRADENAME1", student._title);
            svfVrsOut("GRADENAME2", student._title);

            printName(personalInfo, _name);

            svfVrsOutForData(Arrays.asList("SCHOOLNAME1", "SCHOOLNAME2"), student._schoolName1);

            for (final String year : printgakuseki._grademap.keySet()) {
                final Integer ii = printgakuseki._grademap.get(year);
                final Gakuseki gakuseki = printgakuseki._gakusekiMap.get(year);
                printRegd(student, ii.intValue(), gakuseki);
            }
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param svf
         * @param i
         * @param gakuseki
         */
        private void printRegd(final Student student, final int i, final Gakuseki gakuseki) {
            if (student.isKoumokuGakunen(param())) {
                svfVrsOut("GRADE1_" + i, gakuseki._gakunenSimple);
                svfVrsOut("GRADE2_" + i, gakuseki._gradeName2);
            } else {
                svfVrsOut("GRADE3_" + i, gakuseki._nendo);
                svfVrsOut("GRADE2_" + i, gakuseki.getNendo2());
            }

            // ホームルーム
            final String hrClassField = "HR_CLASS" + (10 < KNJ_EditEdit.getMS932ByteLength(gakuseki._hrname) ? "2_" : "1_");
            svfVrsOut(hrClassField + i, gakuseki._hrname);
            svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        private List<StudyrecSpecialDiv> getStudyrecSpecialDivList3(final List<StudyRec> studyrecList, final Param param) {
            final List<StudyrecSpecialDiv> studyrecSpecialDivList = new ArrayList();
            for (final StudyRec studyrec : studyrecList) {
                if (ClassMst.Null == studyrec._classMst || SubclassMst.Null == studyrec._subclassMst) {
                    continue;
                }
                final StudyrecSpecialDiv ssd = getStudyrecSpecialDiv(studyrecSpecialDivList, studyrec._classMst._specialDiv);
                final StudyrecClass sc = getStudyrecClass(ssd._studyrecClassList, studyrec._classMst, param);
                final StudyrecSubClass ssc = getStudyrecSubClass(sc._studyrecSubclassList, studyrec._subclassMst, param);
                ssc._studyrecList.add(studyrec);
            }
            return studyrecSpecialDivList;
        }

        private StudyrecSpecialDiv getStudyrecSpecialDiv(final List<StudyrecSpecialDiv> list, final String specialDiv) {
            StudyrecSpecialDiv studyrecSpecialDiv = null;
            for (final StudyrecSpecialDiv studyrecSpecialDiv0 : list) {
                if (specialDiv.equals(studyrecSpecialDiv0.studyrec()._classMst._specialDiv)) {
                    studyrecSpecialDiv = studyrecSpecialDiv0;
                    break;
                }
            }
            if (null == studyrecSpecialDiv) {
                studyrecSpecialDiv = new StudyrecSpecialDiv();
                list.add(studyrecSpecialDiv);
            }
            return studyrecSpecialDiv;
        }

        private StudyrecClass getStudyrecClass(final List<StudyrecClass> list, final ClassMst classMst, final Param param) {
            StudyrecClass studyrecClass = null;
            for (final StudyrecClass studyrecClass0 : list) {
                if (ClassMst.compareOrder(param, studyrecClass0.studyrec()._classMst, classMst) == 0) {
                    studyrecClass = studyrecClass0;
                    break;
                }
            }
            if (null == studyrecClass) {
                studyrecClass = new StudyrecClass();
                list.add(studyrecClass);
            }
            return studyrecClass;
        }

        private StudyrecSubClass getStudyrecSubClass(final List<StudyrecSubClass> list, final SubclassMst subclassMst, final Param param) {
            StudyrecSubClass studyrecSubClass = null;
            for (final StudyrecSubClass studyrecSubClass0 : list) {
                if (SubclassMst.compareOrder(param, studyrecSubClass0.studyrec()._subclassMst, subclassMst) == 0) {
                    studyrecSubClass = studyrecSubClass0;
                    break;
                }
            }
            if (null == studyrecSubClass) {
                studyrecSubClass = new StudyrecSubClass();
                list.add(studyrecSubClass);
            }
            return studyrecSubClass;
        }

        private class StudyrecSpecialDiv {
            final List<StudyrecClass> _studyrecClassList = new ArrayList();
            public StudyRec studyrec() {
                return _studyrecClassList.get(0).studyrec();
            }
        }

        private class StudyrecClass {
            final List<StudyrecSubClass> _studyrecSubclassList = new ArrayList();
            public StudyRec studyrec() {
                return _studyrecSubclassList.get(0).studyrec();
            }
        }

        private class StudyrecSubClass {
            final List<StudyRec> _studyrecList = new ArrayList();
            public StudyRec studyrec() {
                return _studyrecList.get(0);
            }
        }

        private class PrintGakuseki {
            final List<String> _yearList = new ArrayList();
            final Map<String, Gakuseki> _gakusekiMap = new HashMap();
            final Map<String, Integer> _grademap = new HashMap();
            boolean _includeZaisekimae = false;
            public Gakuseki _dropGakuseki = null;
        }


        private static class OutputLine {
            final int _line;
            String _edudiv;
            String _edudiv2;
            String _classname;
            String _classcd2;
            String _keySubclasscd;
            String _subclassname;
            String _biko;
            List<StudyRec> _studyrecList = Collections.emptyList();
            boolean _hasDropValid;

            OutputLine(final int line) {
                _line = line;
            }
            public String toString() {
                return _line + " = " + _edudiv + " : " + _edudiv2 + ", " + _classname + " : " + _classcd2 + ", " + _keySubclasscd + " : " + _subclassname;
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

        KNJA130_4(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        private String getForm(final Student student) {
            _name = new KNJSvfFieldInfo("NAME", 801, 1601, KNJSvfFieldModify.charSizeToPixel(12.0), 162, 122, 202, 24, 48);
            final String form;
            if (param().isTokubetsuShien()) {
                _name = new KNJSvfFieldInfo("NAME", 801, 1601, KNJSvfFieldModify.charSizeToPixel(10.0), 162, 122, 202, 24, 48);
                if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                    if (param()._isSenkouka) {
                        form = "KNJA134A_14.frm";
                    } else {
                        form = "KNJA134H_14.frm";
                    }
                } else { // if (CHITEKI1_知的障害.equals(param()._chiteki)) {
                    form = "KNJA134H_4.frm";
                }
            } else {
                if (param()._z010.in(Z010.tokyoto)) {
                    form = StringUtils.isBlank(student._shoken._htrainRemarkHdatVal2) ? "KNJA130_6_2TOKYO.frm" : "KNJA130_6TOKYO.frm";
                    _name = new KNJSvfFieldInfo("NAME", 531, 1246, KNJSvfFieldModify.charSizeToPixel(12.0), 323, 293, 353, 24, 54);
                } else {
                    if (param()._z010.in(Z010.KINDAI)) {
                        form = "KNJA130_4K.frm";
                    } else {
                        form = param()._isNendogoto ? "KNJA130_4A.frm" : "KNJA130_4.frm";
                    }
                }
            }
            return form;
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {

//            boolean hasZeroPrintFlg = false;
            final int max = param()._z010.in(Z010.tokyoto) ? 6 : 4;
            final List<Gakuseki> gakusekiAttendRecList = Student.createGakusekiAttendRec(db2, personalInfo, pageGakusekiList, student._attendRecMap, param());

            final List<Map<Gakuseki, Integer>> grademapList = getGrademapList(student, max, gakusekiAttendRecList);

            for (final Map<Gakuseki, Integer> grademap : grademapList) {
                String form = getForm(student);
                form = modifyFormTankyu(form, personalInfo, pageGakusekiList);
                svfVrSetForm(form, 1);

                printGradeRecSub(student, personalInfo);

                for (final Gakuseki gakuseki : grademap.keySet()) {
                    final String pf = grademap.get(gakuseki).toString();

                    printGradeRecDetail(student, pf, gakuseki);
                }

                if (personalInfo._isFuhakkou) {
                    printFuhakkouText(param());
                } else if (param().isTokubetsuShien()) {
                    if (CHITEKI2_知的障害以外.equals(param()._chiteki)) {
                        final KNJA130_2.StudyrecTotalClass total92 = KNJA130_2.getStudyrecTotalClass(student, personalInfo, personalInfo._gakusekiList, new ArrayList(), param(), _92);
                        printShokenChitekiIgai(student, grademap, KNJA130_2.getYearCreditsMap(total92));
                    } else { // CHITEKI1_知的障害.equals(param()._chiteki)) {
                        printShokenChiteki(student, grademap);
                    }
                } else {
                    printShoken(student, grademap);
                }

                svfVrEndPage();
                nonedata = true;
            }
        }

        private void printShokenChitekiIgai(final Student student, final Map<Gakuseki, Integer> grademap, final Map yearCredit92Map) {
            // 「総合的な学習の時間の記録」を印字
            // 「学習活動」の欄
            if (param()._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                final StringBuffer act = new StringBuffer();
                final StringBuffer val = new StringBuffer();
                for (final Gakuseki gakuseki : grademap.keySet()) {

                    // 所見データを印刷
                    final HtrainRemark remark = student._htrainRemarkMap.get(gakuseki._year);
                    if (null != remark) {
                        if (null != remark._totalStudyAct) {
                            if (act.length() > 0) {
                                act.append("\n");
                            }
                            act.append(remark._totalStudyAct);
                        }
                        if (null != remark._totalStudyVal) {
                            if (val.length() > 0) {
                                val.append("\n");
                            }
                            val.append(remark._totalStudyVal);
                        }
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" act = " + act);
                    log.info(" val = " + val);
                }
                // 「学習活動」の欄
                if (act.length() > 0) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize), 48, 9);
                    printDetail("rec_1_2", getTokenList(act.toString(), size._mojisu * 2, param()));
                }
                // 「評価」の欄
                if (val.length() > 0) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyvalSize), 48, 12);
                    printDetail("rec_2_2", getTokenList(val.toString(), size._mojisu * 2, param()));
                }
            } else {
                if (null != student._shoken._htrainRemarkHdatTotalstudyact) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize), 60, 6);

                    printDetail("rec_1_2", getTokenList(student._shoken._htrainRemarkHdatTotalstudyact, size._mojisu * 2, size._gyo, param()));
                }
                // 「評価」の欄
                if (null != student._shoken._htrainRemarkHdatTotalstudyval) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyvalSize), 60, 6);

                    printDetail("rec_2_2", getTokenList(student._shoken._htrainRemarkHdatTotalstudyval, size._mojisu * 2, size._gyo, param()));
                }
            }
            // 入学時の障害の状態
            if (null != student._shoken._htrainRemarkDetail2HdatSeq001Remark1) {
                final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H), 11, 6);

                printDetail("FIELD1", getTokenList(student._shoken._htrainRemarkDetail2HdatSeq001Remark1, size._mojisu * 2, size._gyo, param()));
            }

            for (final Gakuseki gakuseki : grademap.keySet()) {
                final int j = grademap.get(gakuseki).intValue();
                final String pf = String.valueOf(j);

                printGradeRecDetail(student, pf, gakuseki);

                // 所見データを印刷
                final HtrainRemark htrainRemark = student._htrainRemarkMap.get(gakuseki._year);
                if (null != htrainRemark) {
                    // 特別活動の記録
                    if (null != htrainRemark._specialActRemark) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_SpecialactremarkSize), 15, 6);

                        printDetail("SPECIALACTREMARK_" + pf + "_2", getTokenList(htrainRemark._specialActRemark, size._mojisu * 2, size._gyo, param()));
                    }

                    // 自立活動の記録
                    if (null != htrainRemark._detail2DatSeq001Remark1) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_H), 64, 7);

                        printDetail("INDEPENDENCE_REMARK" + pf, getTokenList(htrainRemark._detail2DatSeq001Remark1, size._mojisu * 2, size._gyo, param()));
                    }

                    // 自立活動の単位数
                    final Integer credit92 = (Integer) yearCredit92Map.get(gakuseki._year);
                    if (null != credit92) {
                        svfVrsOut("IND_CREDIT" + pf, credit92.toString());
                    }

                    // 所見
                    if (null != htrainRemark._totalRemark) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalremarkSize), 66, 8);

                        printDetail("rec_3_" + pf + "_2", getTokenList(htrainRemark._totalRemark, size._mojisu * 2, size._gyo, param()));
                    }

                    // 出欠備考
                    if (null != htrainRemark._attendRecRemark) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_Attendrec_RemarkSize), 35, 2);

                        final List attendRemarkTokenList = getTokenList(htrainRemark._attendRecRemark, size._mojisu * 2, size._gyo, param());
                        for (int i = 0; i < attendRemarkTokenList.size(); i++) {
                            final String attendRemark = (String) attendRemarkTokenList.get(i);
                            svfVrsOutn("REMARK" + String.valueOf(i == 0 ? "" : String.valueOf(i + 1)), j, attendRemark);
                        }
                    }
                }

                // 出欠データを印刷
                final AttendRec attendrec = student._attendRecMap.get(gakuseki._year);
                if (null != attendrec) {
                    svfVrsOutn("LESSON", j, attendrec._attend_1); // 授業日数
                    svfVrsOutn("SUSPEND", j, KNJA130CCommon.addNumber(attendrec._suspend, attendrec._mourning)); // 忌引日数
                    svfVrsOutn("ABROAD", j, attendrec._abroad); // 留学日数
                    svfVrsOutn("PRESENT", j, attendrec._requirepresent); // 要出席日数
                    svfVrsOutn("ABSENCE", j, attendrec._attend_6); // 欠席日数
                    svfVrsOutn("ATTEND", j, attendrec._present); // 出席日数
                }
            }
        }

        private void printShokenChiteki(final Student student, final Map<Gakuseki, Integer> grademap) {
            // 「総合的な学習の時間の記録」を印字
            if (param()._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                final StringBuffer act = new StringBuffer();
                final StringBuffer val = new StringBuffer();
                for (final Gakuseki gakuseki : grademap.keySet()) {

                    // 所見データを印刷
                    final HtrainRemark remark = student._htrainRemarkMap.get(gakuseki._year);
                    if (null != remark) {
                        if (null != remark._totalStudyAct) {
                            if (act.length() > 0) {
                                act.append("\n");
                            }
                            act.append(remark._totalStudyAct);
                        }
                        if (null != remark._totalStudyVal) {
                            if (val.length() > 0) {
                                val.append("\n");
                            }
                            val.append(remark._totalStudyVal);
                        }
                    }
                }
                if (param()._isOutputDebug) {
                    log.info(" act = " + act);
                    log.info(" val = " + val);
                }
                // 「学習活動」の欄
                if (act.length() > 0) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize_disability), 48, 9);
                    printDetail("TOTAL_ACT", getTokenList(act.toString(), size._mojisu * 2, param()));
                }
                // 「評価」の欄
                if (val.length() > 0) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyvalSize_disability), 48, 12);
                    printDetail("TOTAL_VALUE", getTokenList(val.toString(), size._mojisu * 2, param()));
                }
            } else {
                // 「学習活動」の欄
                if (null != student._shoken._htrainRemarkHdatTotalstudyact) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyactSize_disability), 48, 9);
                    printDetail("TOTAL_ACT", getTokenList(student._shoken._htrainRemarkHdatTotalstudyact, size._mojisu * 2, size._gyo, param()));
                }
                // 「評価」の欄
                if (null != student._shoken._htrainRemarkHdatTotalstudyval) {
                    final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalstudyvalSize_disability), 48, 12);
                    printDetail("TOTAL_VALUE", getTokenList(student._shoken._htrainRemarkHdatTotalstudyval, size._mojisu * 2, size._gyo, param()));
                }
            }
            // 入学時の障害の状態
            if (null != student._shoken._htrainRemarkDetail2HdatSeq001Remark1) {
                final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H_disability), 11, 22);
                printDetail("FIELD1", getTokenList(student._shoken._htrainRemarkDetail2HdatSeq001Remark1, size._mojisu * 2, size._gyo, param()));
            }

            for (final Iterator git = grademap.keySet().iterator(); git.hasNext();) {
                final Gakuseki gakuseki = (Gakuseki) git.next();
                final int j = ((Integer) grademap.get(gakuseki)).intValue();
                final String pf = String.valueOf(j);

                printGradeRecDetail(student, pf, gakuseki);

                // 所見データを印刷
                final HtrainRemark htrainRemark = student._htrainRemarkMap.get(gakuseki._year);
                if (null != htrainRemark) {
                    // 所見
                    if (null != htrainRemark._totalRemark) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_TotalremarkSize_disability), 60, 12);
                        printDetail("TOTALREMARK13_" + pf, getTokenList(htrainRemark._totalRemark, size._mojisu * 2, size._gyo, param()));
                    }

                    // 出欠備考
                    if (null != htrainRemark._attendRecRemark) {
                        final KNJPropertiesShokenSize size = KNJPropertiesShokenSize.getShokenSize(param().property(Property.seitoSidoYoroku_dat_Attendrec_RemarkSize_disability), 35, 2);
                        final List attendRemarkTokenList = getTokenList(htrainRemark._attendRecRemark, size._mojisu * 2, size._gyo, param());
                        for (int i = 0; i < attendRemarkTokenList.size(); i++) {
                            final String attendRemark = (String) attendRemarkTokenList.get(i);
                            svfVrsOutn("REMARK" + String.valueOf(i == 0 ? "" : String.valueOf(i + 1)), j, attendRemark);
                        }
                    }
                }

                // 出欠データを印刷
                final AttendRec attendrec = student._attendRecMap.get(gakuseki._year);
                if (null != attendrec) {
                    svfVrsOutn("LESSON", j, attendrec._attend_1); // 授業日数
                    svfVrsOutn("SUSPEND", j, KNJA130CCommon.addNumber(attendrec._suspend, attendrec._mourning)); // 忌引日数
                    svfVrsOutn("ABROAD", j, attendrec._abroad); // 留学日数
                    svfVrsOutn("PRESENT", j, attendrec._requirepresent); // 要出席日数
                    svfVrsOutn("ABSENCE", j, attendrec._attend_6); // 欠席日数
                    svfVrsOutn("ATTEND", j, attendrec._present); // 出席日数
                }
            }
        }

        private void printShoken(final Student student, final Map grademap) {
            if (!param()._isNendogoto) { // 「通年」か?
                if (param()._seitoSidoYorokuTotalStudyCombineHtrainremarkDat) {
                    // 実装保留...
                } else if (param()._z010.in(Z010.tokyoto)) {
                    // 「総合的な学習の時間の記録」を印字
                    printDetail("rec_1", getTokenList(student._shoken._htrainRemarkHdatTotalstudyact, 44 * 2, 4, param()));
                    // 「学習活動」の欄
                    printDetail("rec_2", getTokenList(student._shoken._htrainRemarkHdatTotalstudyval, 44 * 2, 6, param()));
                    // 「評価」の欄
                    // 奉仕
                    printDetail("rec_3", getTokenList(student._shoken._htrainRemarkHdatAct2, 44 * 2, 4, param()));
                    // 「活動」の欄
                    printDetail("rec_4", getTokenList(student._shoken._htrainRemarkHdatVal2, 44 * 2, 6, param()));    // 「評価」の欄
                } else {
                    // 「総合的な学習の時間の記録」を印字
                    printDetail("REC_1", getTokenList(student._shoken._htrainRemarkHdatTotalstudyact, 44 * 2, 4, param()));
                    // 「学習活動」の欄
                    printDetail("REC_2", getTokenList(student._shoken._htrainRemarkHdatTotalstudyval, 44 * 2, 6, param()));    // 「評価」の欄
                }
            }

            for (final Iterator git = grademap.keySet().iterator(); git.hasNext();) {
                final Gakuseki gakuseki = (Gakuseki) git.next();
                final String pf = String.valueOf(((Integer) grademap.get(gakuseki)).intValue());

                printGradeRecDetail(student, pf, gakuseki);

                // 所見データを印刷
                final HtrainRemark remark = student._htrainRemarkMap.get(gakuseki._year);
                if (null != remark) {
                    if (param()._isNendogoto) {
                        printDetail("rec_1_" + pf, getTokenList(remark._totalStudyAct, 22, 4, param()));   // 総合的な学習の時間学習活動
                        printDetail("rec_2_" + pf, getTokenList(remark._totalStudyVal, 22, 6, param()));   // 総合的な学習の時間評価
                    }

                    // 特別活動
                    if (param()._z010.in(Z010.chukyo)) {
                        printDetail("SPECIALACTREMARK_2_" + pf, getTokenList(remark._specialActRemark, 44, 10, param()));
                    } else {
                        printDetail("SPECIALACTREMARK_" + pf, getTokenList(remark._specialActRemark, 22, 6, param()));
                    }

                    // 所見
                    if (param()._z010.in(Z010.chukyo)) {
                        printDetail("rec_3_2_" + pf, getTokenList(remark._totalRemark, 132, 7, param()));
                    } else {
                        printDetail("rec_3_" + pf, getTokenList(remark._totalRemark, 88, 6, param()));
                    }

                    // 出欠備考
                    printDetail("syuketu_8_" + pf, getTokenList(remark._attendRecRemark, 40, 2, param()));
                }

                // 出欠データを印刷
                final AttendRec attendrec = student._attendRecMap.get(gakuseki._year);
                if (null != attendrec) {
                    svfVrsOut("syuketu_1_" + pf, attendrec._attend_1); // 授業日数
                    if (param()._z010.in(Z010.KINDAI)) {
                        svfVrsOut("syuketu_2_" + pf, attendrec._suspend); // 出停 + 忌引日数
                        svfVrsOut("syuketu_3_" + pf, attendrec._mourning); // // 出停日数
                    } else {
                        svfVrsOut("SUSPEND"    + pf, KNJA130CCommon.addNumber(attendrec._suspend, attendrec._mourning)); // 忌引日数
                    }
                    svfVrsOut("syuketu_4_" + pf, attendrec._abroad); // 留学日数
                    svfVrsOut("syuketu_5_" + pf, attendrec._requirepresent); // 要出席日数
                    svfVrsOut("syuketu_6_" + pf, attendrec._attend_6); // 欠席日数
                    svfVrsOut("syuketu_7_" + pf, attendrec._present); // 出席日数
                }
            }
        }

        private List<Map<Gakuseki, Integer>> getGrademapList(final Student student, final int max, final List<Gakuseki> createGakusekiAttendRec) {
            final List<Map<Gakuseki, Integer>> grademapList = new ArrayList();
            int i = 1;
            Map<Gakuseki, Integer> grademap = new TreeMap<Gakuseki, Integer>();
            grademapList.add(grademap);
            boolean beforeDrop = false;
            for (final Gakuseki gakuseki : createGakusekiAttendRec) {
                if (beforeDrop) {
                    grademap = new TreeMap<Gakuseki, Integer>();
                    grademapList.add(grademap);
//                    if (hasZeroPrintFlg) {
//                        i = !NumberUtils.isDigits(gakuseki._gradeCd) ? -1 : Integer.parseInt(gakuseki._grade);
//                        hasZeroPrintFlg = false;
//                    }
                    beforeDrop = false;
                }
                if (student.isTanniSei(param()) && KNJA130CCommon.Util.isNyugakumae(gakuseki._year) || !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                    continue;
                }
                final int j = getGradeColumnNum(student, i, gakuseki, max, 0);
                grademap.put(gakuseki, new Integer(j));

                // 留年以降を改ページします。
                if (gakuseki._isDrop) {
                    beforeDrop = true;
                } else if (max == i) {
                    i = 1;
                } else {
                    i++;
                }
//                if (_year0.equals(gakuseki._year)) {
//                    hasZeroPrintFlg = true;
//                }
            }
            return grademapList;
        }

        protected void printDetail(
                final String strField,
                final List<String> tokenList
        ) {
            for (int j = 0; j < tokenList.size(); j++) {
                if (null == tokenList.get(j)) {
                    continue;
                }
                svfVrsOutn(strField, j + 1, tokenList.get(j));
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
        private void printGradeRecSub(final Student student, final PersonalInfo personalInfo) {
            svfVrsOut("GRADENAME1", student._title);
            svfVrsOut("GRADENAME2", student._title);

            printName(personalInfo, _name);
        }

        /**
         * ヘッダー（学籍履歴）を印刷します。
         * @param pf
         * @param gakuseki
         */
        private void printGradeRecDetail(
                final Student student,
                final String pf,
                final Gakuseki gakuseki
        ) {
            final boolean enableYear = (null != gakuseki._year) && (0 != Integer.parseInt(gakuseki._year));
            final String n;
            if (student.isKoumokuGakunen(param())) {

                if (enableYear) {
                    svfVrsOut("GRADE4_" + pf, gakuseki._gakunenSimple);
                    svfVrsOut("GRADE3_" + pf + "_2", gakuseki._gakunenSimple);
                } else {
                    svfVrsOut("GRADE3_" + pf + "_2", gakuseki._nendo);
                }

                n = gakuseki._gradeName2;
            } else {
                if (enableYear) {
                    svfVrsOut("GRADE3_" + pf, gakuseki._nendo);
                    final String[] nendoArray = gakuseki.nendoArray();
                    svfVrsOut("GRADE3_" + pf + "_1", nendoArray[0]);
                    svfVrsOut("GRADE3_" + pf + "_2", nendoArray[1]);
                    svfVrsOut("GRADE3_" + pf + "_3", nendoArray[2]);
                } else {
                    svfVrsOut("GRADE3_" + pf + "_2", gakuseki._nendo);
                }

                n = gakuseki._nendo;
            }
            svfVrsOut("GRADE1_" + pf, n); // 特別活動の記録
            svfVrsOut("GRADE2_" + pf, n); // 総合所見及び...
            svfVrsOut("GRADE5_" + pf, n); // 総合的な学習の時間の記録
            if (param().isTokubetsuShien()) {
                svfVrsOut("GRADE2_" + pf + "_2", n);
            }

            // ホームルーム
            String hrClassField = "HR_CLASS" + (gakuseki._hrname != null && gakuseki._hrname.length() > 5 ? "2_" : "1_");
            svfVrsOut(hrClassField + pf, gakuseki._hrname);
            svfVrsOut("ATTENDNO_" + pf, gakuseki._attendno);
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * 出欠の記録（東京都用）
     */
    private static class KNJA130_4T extends KNJA130_4 {

        private KNJSvfFieldInfo _name = new KNJSvfFieldInfo("NAME", 722, 1662, KNJSvfFieldModify.charSizeToPixel(12.0), 951, 911, 991, 24, 48);

        KNJA130_4T(final Vrw32alp svf, final Param param) {
            super(svf, param);
        }

        public void VrsOutToken(final String[] field, final int len, final String s) {
            final List<String> token = getTokenList(s, len, field.length, param());
            for (int i = 0; i < token.size(); i++) {
                svfVrsOut(field[i], token.get(i));
            }
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {
            final String frm = "KNJA130_3TOKYO.frm";
            svfVrSetForm(frm, 1);

            if (KNJ_EditEdit.getMS932ByteLength(student._schoolName1) > 28) {
                VrsOutToken(new String[] {"SCHOOLNAME2", "SCHOOLNAME3"}, 28, student._schoolName1);
            } else {
                svfVrsOut("SCHOOLNAME1", student._schoolName1);
            }

            final String majorCourseName = StringUtils.defaultString(personalInfo._courseName) + "　" + StringUtils.defaultString(personalInfo._majorName);
            if (KNJ_EditEdit.getMS932ByteLength(majorCourseName) > 28) {
                VrsOutToken(new String[] {"COURSENAME2", "COURSENAME3"}, 28, majorCourseName);
            } else {
                svfVrsOut("COURSENAME1", majorCourseName);
            }

            int i = 1;
//            boolean hasZeroPrintFlg = false;
            final int max = param()._z010.in(Z010.tokyoto) ? 6 : 4;
            for (final Gakuseki gakuseki : Student.createGakusekiAttendRec(db2, personalInfo, pageGakusekiList, student._attendRecMap, param())) {

                if (student.isTanniSei(param()) && KNJA130CCommon.Util.isNyugakumae(gakuseki._year) || !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                    continue;
                }

                final int j = getGradeColumnNum(student, i, gakuseki, max, 1);

                printGradeRecDetail4(student, j, gakuseki);
                printGradeRecSub4(student, personalInfo);

                if (personalInfo._isFuhakkou) {
                    printFuhakkouText(param());
                } else {
                    printShokenData(j, gakuseki._year, student);
                }

                // 留年以降を改ページします。
                if (gakuseki._isDrop) {
                    svfVrEndPage();
//                    if (hasZeroPrintFlg) {
//                        i = !NumberUtils.isDigits(gakuseki._gradeCd) ? -1 : Integer.parseInt(gakuseki._grade);
//                        hasZeroPrintFlg = false;
//                    }
                } else if (max == i) {
                    svfVrEndPage();
                    i = 1;
                } else {
                    i++;
                }
//                if (_year0.equals(gakuseki._year)) {
//                    hasZeroPrintFlg = true;
//                }
            }
            svfVrEndPage();
            nonedata = true;
        }


        private void printShokenData(final int j, final String year, Student student) {
            // 所見データを印刷
            final HtrainRemark remark = student._htrainRemarkMap.get(year);
            if (null != remark) {
                // 出欠備考
                printDetail("syuketu_8_" + String.valueOf(j), getTokenList(remark._attendRecRemark, 20, 4, param()));
            }

            // 出欠データを印刷
            final AttendRec attendrec = student._attendRecMap.get(year);
            if (null != attendrec) {
                svfVrsOut("syuketu_1_" + j, attendrec._attend_1); // 授業日数
                if (param()._z010.in(Z010.KINDAI)) {
                    svfVrsOut("syuketu_2_" + j, attendrec._suspend); // 出停 + 忌引日数
                    svfVrsOut("syuketu_3_" + j, attendrec._mourning); // 出停日数
                } else if (param()._z010.in(Z010.tokyoto)) {
                    svfVrsOut("syuketu_2_" + j, KNJA130CCommon.addNumber(attendrec._suspend, attendrec._mourning)); // 出停 + 忌引日数
                } else {
                    svfVrsOut("SUSPEND"    + j, KNJA130CCommon.addNumber(attendrec._suspend, attendrec._mourning)); // 忌引日数
                }
                svfVrsOut("syuketu_4_" + j, attendrec._abroad); // 留学日数
                svfVrsOut("syuketu_5_" + j, attendrec._requirepresent); // 要出席日数
                svfVrsOut("syuketu_6_" + j, attendrec._attend_6); // 欠席日数
                svfVrsOut("syuketu_7_" + j, attendrec._present); // 出席日数
            }
        }

        /**
         * 学籍履歴を印刷します。
         * @param i
         * @param gakuseki
         */
        private void printGradeRecDetail4(
                final Student student,
                final int i,
                final Gakuseki gakuseki
        ) {
            // 学年
            if (student.isKoumokuGakunen(param())) {
                svfVrsOut("GRADE2_" + i, gakuseki._gakunenSimple);
                svfVrsOut("GRADE2_" + i + "_2", gakuseki._gakunenSimple);
            } else {
                final String[] nendoArray = gakuseki.nendoArray();
                svfVrsOut("GRADE1_" + i, nendoArray[0]);
                svfVrsOut("GRADE2_" + i, nendoArray[1]);
                svfVrsOut("GRADE3_" + i, nendoArray[2]);

                svfVrsOut("GRADE2_" + i + "_1", nendoArray[0]);
                svfVrsOut("GRADE2_" + i + "_2", nendoArray[1]);
                svfVrsOut("GRADE2_" + i + "_3", nendoArray[2]);
            }

            // ホームルーム
            if (KNJ_EditEdit.getMS932ByteLength(gakuseki._hrname) > 10) {
                svfVrsOut("HR_CLASS_" + String.valueOf(i) + "_2", gakuseki._hrname);
            } else {
                svfVrsOut("HR_CLASS_" + String.valueOf(i), gakuseki._hrname);
            }
            // 整理番号
            svfVrsOut("ATTENDNO_" + i, gakuseki._attendno);
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printGradeRecSub4(final Student student, final PersonalInfo personalInfo) {
            svfVrsOut("GRADENAME1", student._title);
            svfVrsOut("GRADENAME2", student._title);

            printName(personalInfo, _name);
        }
    }

    /**
     * 特例の授業等の記録
     */
    private static class KNJA129Delegate extends KNJA130_0 {

        KNJA129 _knja129;
        KNJA129.Param _knja129param;

        KNJA129Delegate(final Vrw32alp svf, final Param param, final HttpServletRequest request, final DB2UDB db2) {
            super(svf, param);
            _knja129 = new KNJA129();
            _knja129param = _knja129.getParam(request, db2, param._year, param._gakki, param.SCHOOL_KIND);
            _gradeLineMax = 6;
        }


        @Override
        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<Gakuseki> pageGakusekiList) {
            setDetail4(db2, pInfo, pageGakusekiList, null);
        }

        private void setDetail4(final DB2UDB db2, final PersonalInfo pInfo, final List<Gakuseki> pageGakusekiList, final List<List<String>> csvLines) {

            printPage4(db2, pInfo._student, pInfo, pageGakusekiList, csvLines);
        }

        private void printPage4(final DB2UDB db2, final Student student, final PersonalInfo pInfo, final List<Gakuseki> pageGakusekiList, final List<List<String>> csvLines) {
            ArrayList<String> years = new ArrayList<String>(PersonalInfo.gakusekiYearSet(pageGakusekiList));
            log.info(" years = " + years);
            _knja129.printSvf(_knja129param, db2, super._form._svf, pInfo._student._schregno, getNameLines(pInfo), param().SCHOOL_KIND, years, csvLines);
            if (_knja129.hasData()) {
                nonedata = true;
            }
        }

        public void close() {
            super.close();
            _knja129param.close();
        }
    }

    /**
     * 学習の記録　知的障害者用 文言評価
     */
    private static class KNJA134H_3 extends KNJA130_0 {

        private KNJSvfFieldInfo _name;

        KNJA134H_3(final Vrw32alp svf, final Param param) {
            super(svf, param);
            _name = new KNJSvfFieldInfo("NAME", 416, 950, KNJSvfFieldModify.charSizeToPixel(10.0), 514, 484, 544, 24, 48);
        }

        public void setDetail(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {
//            boolean hasZeroprintflg = false;
            final List<PrintGakuseki> printGakusekiList = getPrintGakusekiList(db2, student, personalInfo, pageGakusekiList);

            final String form;
            if (param()._z010.in(Z010.fukuiken)) {
                form = "KNJA134H_3FUKUI.frm";
            } else {
                form = "KNJA134H_3.frm";
            }

            for (int j = 0; j < printGakusekiList.size(); j++) {
                final PrintGakuseki printGakuseki = printGakusekiList.get(j);
                svfVrSetForm(form, 4);
                printRegd3(student, personalInfo, printGakuseki);
                final String[] monkashouKyokamei = {"国語", "社会", "数学", "理科", "音楽", "美術", "保健体育", "職業", "家庭", "専門教科", "その他", "特別活動", "自立活動"};
                for (int i = 0; i < monkashouKyokamei.length; i++) {
                    svfVrsOut("CLASS" + String.valueOf(i + 1), monkashouKyokamei[i]);
                }
                if (param()._z010.in(Z010.fukuiken)) {
                    printRecordFukuiken(student, personalInfo, printGakuseki);
                } else {
                    printRecord(student, personalInfo, printGakuseki);
                }
            }
            nonedata = true;
        }

        private List<PrintGakuseki> getPrintGakusekiList(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> pageGakusekiList) {
            int i = 1;
            final List<PrintGakuseki> printGakusekiList = new ArrayList();
            PrintGakuseki printGakuseki = new PrintGakuseki();
            printGakusekiList.add(printGakuseki);
            final int max = 3;
            final String lastyear = StringUtils.defaultString(PersonalInfo.getLastYear(pageGakusekiList));

            final List<Gakuseki> gakusekiStudyRecList = Student.createGakusekiStudyRec(db2, personalInfo, pageGakusekiList, personalInfo._studyRecList, param());
//            boolean includeZaisekimae = false;
            for (final Gakuseki gakuseki : gakusekiStudyRecList) {
                boolean zaisekimae = false;
                if ("1".equals(gakuseki._dataflg) && !param().isGdatH(gakuseki._year, gakuseki._grade)) {
                    continue;
                } else if ("2".equals(gakuseki._dataflg)) {
//                    includeZaisekimae = true;
                    zaisekimae = true;
                }
                // final int j = zaisekimae ? max : getGradeColumnNum(i, gakuseki, max, includeZaisekimae);
                 final int j = getGradeColumnNum(student, i, gakuseki, max, 1);

                printGakuseki._yearList.add(gakuseki._year);
                printGakuseki._grademap.put(gakuseki._year, new Integer(j));
                printGakuseki._gakusekiMap.put(gakuseki._year, gakuseki);
                final boolean lastyearflg = lastyear.equals(gakuseki._year);
                // 留年以降を改ページします。
                if (zaisekimae) {
                    i++;
                } else if (!lastyearflg && gakuseki._isDrop) {
                    printGakuseki._dropGakuseki = gakuseki;
                    printGakuseki = new PrintGakuseki();
                    printGakusekiList.add(printGakuseki);
//                    includeZaisekimae = false;
//                    if (hasZeroprintflg) {
//                        i = !NumberUtils.isDigits(gakuseki._gradeCd) ? -1 : Integer.parseInt(gakuseki._gradeCd);
//                        hasZeroprintflg = false;
//                    }
                } else if (!lastyearflg && max == i) {
                    printGakuseki = new PrintGakuseki();
                    printGakusekiList.add(printGakuseki);
//                    includeZaisekimae = false;
                    i = 1;
                } else {
                    i++;
                }
//                if (_year0.equals(gakuseki._year)) {
//                    hasZeroprintflg = true; // 入学前を出力した。
//                }
            }
            return printGakusekiList;
        }

        private void printRecord(final Student student, final PersonalInfo personalInfo, final PrintGakuseki printGakuseki) {
            final String blankGroup = "--";

            final Map<String, List<PrintLine>> gradeCdLineListMap = new HashMap();
            for (final String gradeCd : personalInfo._gradecdClassRemarkListMap.keySet()) {
                if (!NumberUtils.isDigits(gradeCd) || Integer.parseInt(gradeCd) <= 0 || Integer.parseInt(gradeCd) > 6) {
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

            final int maxLine = 70;
            for (int li = 0; li < maxLine; li++) {
                boolean hasOutput = false;
                for (final String gradeCd : gradeCdLineListMap.keySet()) {
                    final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                    final List<PrintLine> lineList = getMappedList(gradeCdLineListMap, gradeCd);

                    if (li < lineList.size()) {
                        final PrintLine printLine = lineList.get(li);
                        svfVrsOut("GRP" + gradeCdStr, printLine._group);
                        svfVrsOut("TOTAL_RECORD" + gradeCdStr, printLine._line);
                    } else {
                        svfVrsOut("GRP" + gradeCdStr, blankGroup);
                    }
                    hasOutput = true;
                }
                if (!hasOutput) {
                    svfVrsOut("GRP1", blankGroup);
                }
                svfVrsOut("CLASS14_1", "総授業");
                svfVrsOut("CLASS14_2", "時数");

                for (final String gradeCd : personalInfo._chitekiLessonCount.keySet()) {
                    final String pf = String.valueOf(Integer.parseInt(gradeCd));
                    svfVrsOut("LESSON_COUNT" + pf, personalInfo._chitekiLessonCount.get(gradeCd));
                }
                svfVrEndRecord();
            }
        }

        private void printRecordFukuiken(final Student student, final PersonalInfo personalInfo, final PrintGakuseki printGakuseki) {
            final String blankGroup = "--";

            final Map gradeCdLineListMap = new HashMap();
            for (final Iterator git = personalInfo._gradecdClassRemarkListMap.keySet().iterator(); git.hasNext();) {
                final String gradeCd = (String) git.next();
                if (!NumberUtils.isDigits(gradeCd) || Integer.parseInt(gradeCd) <= 0 || Integer.parseInt(gradeCd) > 3) {
                    continue;
                }
                final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                final List lineList = getMappedList(gradeCdLineListMap, gradeCd);

                final List classRemarkList = getMappedList(personalInfo._gradecdClassRemarkListMap, gradeCd);
                for (int i = 0; i < classRemarkList.size(); i++) {
                    final ClassRemark classRemark = (ClassRemark) classRemarkList.get(i);

                    final String group = gradeCdStr + (i % 2 == 1 ? "1" : "0");

                    final String classname = StringUtils.defaultString(classRemark._classname);

                    final List tokenList = getTokenList(classRemark._remark, 42, param());

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

            final int maxLine = 66;
            for (int li = 0; li < maxLine; li++) {
                boolean hasOutput = false;
                for (final Iterator git = gradeCdLineListMap.keySet().iterator(); git.hasNext();) {
                    final String gradeCd = (String) git.next();
                    final String gradeCdStr = String.valueOf(Integer.parseInt(gradeCd));

                    final List lineList = getMappedList(gradeCdLineListMap, gradeCd);

                    if (li < lineList.size()) {
                        final PrintLine printLine = (PrintLine) lineList.get(li);
                        svfVrsOut("GRP" + gradeCdStr + "_CLASS", printLine._group);
                        svfVrsOut("CLASSNAME" + gradeCdStr, printLine._classname);
                        svfVrsOut("GRP" + gradeCdStr, printLine._group);
                        svfVrsOut("TOTAL_RECORD" + gradeCdStr, printLine._line);
                    } else {
                        svfVrsOut("GRP" + gradeCdStr, blankGroup);
                    }
                    hasOutput = true;
                }
                if (!hasOutput) {
                    svfVrsOut("GRP1", blankGroup);
                }
                svfVrsOut("CLASS14_1", "総授業");
                svfVrsOut("CLASS14_2", "時数");

                for (final Iterator it = personalInfo._chitekiLessonCount.keySet().iterator(); it.hasNext();) {
                    final String gradeCd = (String) it.next();
                    final String pf = String.valueOf(Integer.parseInt(gradeCd));
                    svfVrsOut("LESSON_COUNT" + pf, (String) personalInfo._chitekiLessonCount.get(gradeCd));
                }
                svfVrEndRecord();
            }
        }

        /**
         * ヘッダー（生徒名・学年/年度タイトル）を印刷します。
         * @param svf
         * @param student
         */
        private void printRegd3(final Student student, final PersonalInfo personalInfo, final PrintGakuseki printgakuseki) {
            svfVrsOut("GRADENAME1", student._title);
            svfVrsOut("GRADENAME2", student._title);

            printName(personalInfo, _name);

            svfVrsOutForData(Arrays.asList("SCHOOLNAME1", "SCHOOLNAME2"), student._schoolName1);

            for (final String year : printgakuseki._grademap.keySet()) {
                final Integer ii = printgakuseki._grademap.get(year);
                final Gakuseki gakuseki = printgakuseki._gakusekiMap.get(year);
                // ホームルーム
                svfVrsOutn(getHrNameField(gakuseki._hrname), ii.intValue(), gakuseki._hrname);
                svfVrsOutn("ATTENDNO", ii.intValue(), gakuseki._attendno);
            }
        }

        private class PrintGakuseki {
            final List _yearList = new ArrayList();
            final Map<String, Gakuseki> _gakusekiMap = new HashMap<String, Gakuseki>();
            final Map<String, Integer> _grademap = new HashMap<String, Integer>();
            public Gakuseki _dropGakuseki = null;
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

            private static Map<String, List<ClassRemark>> loadGradecdClassRemarkListMap(final DB2UDB db2, final Param param, final Student student) {
                final Map<String, List<ClassRemark>> gradeCdClassRemarkListMap = new HashMap();
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

                    for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { student._schoolKind, student._schregno})) {

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

    //--- 内部クラス -------------------------------------------------------
    public static class KNJSvfFieldModify {

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

        private static int getStringLengthPixel(final double charSize, final int num) {
            return charSizeToPixel(charSize) * num / 2;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public double getCharSize(String str) {
            final double retFieldPoint = retFieldPoint(_width, getStringByteSize(str));
            final double heightPoint = pixelToCharSize(_height);
            //log.info(" fieldname = " + _fieldname + ", width = " + _width + ", byteSize = " + getStringByteSize(str) + ", retPoint = " + retFieldPoint + ", heightPoint = " + heightPoint + ", min = " + Math.min(heightPoint, retFieldPoint));
            return Math.min(heightPoint, retFieldPoint);                  //文字サイズ
        }

        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private int getStringByteSize(String str) {
            return Math.min(Math.max(retStringByteValue(str), _minnum), _maxnum);
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
        public double getYjiku(final int hnum, final double charSize) {
            double jiku = 0;
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
        private static double retFieldPoint(final int width, final int num) {
            return new BigDecimal((double) width / (num / 2) * 72 / 400).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static double retFieldY(final int height, final double charSize) {
            return Math.round((height - (charSize / 72 * 400)) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: fieldname = " + _fieldname + " width = "+ _width + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    private static class Param extends KNJA130CCommon.Param {

        final String _hrClassType; // KNJA134Hのみ
        final String _gakunenKongou; // KNJA134Hのみ
        final String _notPrintGuarantor; // KNJA134Hのみ
        final String _gradeInChugaku = "('01','02','03')";
        List<String> _schregnoList;

        /** 生徒指導要録 */
        final String _seito;
        /** 修得単位の記録 */
        final String _tani;
        /** 出欠の記録 */
        final String _shukketsu;
        /** 学習の記録 */
        final String _gakushu;
        /** 活動の記録 */
        final String _katsudo;
        /** 特例の授業等の記録 */
        final String _online;
        /** 生徒・保護者氏名出力(生徒指導要録に関係する) */
        final String _simei;
        /** 特別支援学校の知的区分 1:知的障害者 2:知的障害者以外 */
        final String _chiteki;
        /** 履修のみ科目出力 */
        final boolean _isPrintRisyuNomi;
        /** 未履修科目出力 */
        final boolean _isPrintMirisyu;
        /** 履修登録のみ科目出力 */
        final boolean _isPrintRisyuTourokuNomi;
        /** 陰影出力(生徒指導要録に関係する) */
        final String _inei;
        /** 現住所の郵便番号を出力 */
        final boolean _printZipcd; // 0C
        /** 学校所在地の郵便番号を出力 */
        final boolean _printSchoolZipcd; // 0C

        KNJA130_1 _knja130_1;
        KNJA130_2 _knja130_2;
        KNJA130_4T _knja130_4t;
        KNJA134H_3 _knja134h_3;
        KNJA130_3 _knja130_3;
        KNJA130_4 _knja130_4;
        KNJA129Delegate _knja129;

        /** 「活動の記録」にて「総合的な学習の時間の記録」欄は年度毎か? */
        final boolean _isNendogoto;
        /** 「活動の記録」にて「総合的な学習の時間の記録」欄は年度毎データを結合するか */
        final boolean _seitoSidoYorokuTotalStudyCombineHtrainremarkDat;

        protected boolean _isPrintYoshiki2OmoteTotalCreditByPage;
        final int _SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount;
        KNJDefineSchool _definecode = new KNJDefineSchool();

        private String _schoolName1;
        private String _schoolAddress1;
        private String _schoolAddress2;
        private String _schoolZipcode;
        private String _certifSchoolSchoolName;
        private String _bunkouSchoolAddress1;
        private String _bunkouSchoolAddress2;
        private String _bunkouSchoolName;

        /** 学科年度データの学校区分 */
        private String _majorYdatSchooldiv;
        private int _subclassSubstRecordCount = 0;

        Param(final HttpServletRequest request, final DB2UDB db2, final String prgId, final String year, final String gakki, final List<String> schregnoList, final Map otherParamMap) {
            super(request, db2, prgId, year, gakki, otherParamMap);
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _inei = "".equals(request.getParameter("INEI")) ? null : request.getParameter("INEI"); // 陰影出力
            _schregnoList = schregnoList;

            _simei = request.getParameter("simei"); // 漢字名出力

            _seito = request.getParameter("seito");
            _tani = request.getParameter("tani");
            _shukketsu = request.getParameter("shukketsu");
            _gakushu = request.getParameter("gakushu");
            _katsudo = request.getParameter("katsudo");
            _online = request.getParameter("online");
            _notPrintGuarantor = request.getParameter("not_print_guarantor");
            _chiteki = request.getParameter("CHITEKI");
            _isPrintRisyuNomi = "1".equals(StringUtils.defaultString(request.getParameter("RISYU"), "1"));
            _isPrintMirisyu = "1".equals(StringUtils.defaultString(request.getParameter("MIRISYU"), "1"));
            _isPrintRisyuTourokuNomi = "1".equals(StringUtils.defaultString(request.getParameter("RISYUTOUROKU"), "1"));

            _isNendogoto = "KNJA130A".equals(prgId) || _z010.in(Z010.KINDAI) && "KNJI050".equals(prgId);
            _seitoSidoYorokuTotalStudyCombineHtrainremarkDat = isTokubetsuShien();
            log.debug("「活動の記録」にて「総合的な学習の時間の記録」欄を年度毎にするか?⇒" + _isNendogoto + " / 結合して印字 = " + _seitoSidoYorokuTotalStudyCombineHtrainremarkDat);

            _printZipcd = "1".equals(request.getParameter("schzip"));
            _printSchoolZipcd = "1".equals(request.getParameter("schoolzip"));

            _isPrintYoshiki2OmoteTotalCreditByPage = true;
            _definecode.setSchoolCode(db2, _year);

            _subclassSubstRecordCount = Util.toInt(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) AS COUNT FROM SUBCLASS_REPLACE_SUBSTITUTION_DAT ")), 0);
            _SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount = Util.toInt(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) AS COUNT FROM SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT")), 0);
            log.fatal(" tableDataCount " + "SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT" + " = " + _SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DATCount);

            setClassMst(db2);
            setSubclassMst(db2);
        }

        public void closeForm() {
            for (final KNJA130_0 f : Arrays.asList(_knja130_1, _knja130_2, _knja130_4t, _knja134h_3, _knja130_3, _knja130_4)) {
                if (null != f) {
                    f.close();
                }
            }
        }

        private void loadSchool(final DB2UDB db2, final String schoolKind) {
            try {
                final Map paramMap = new HashMap();
                if (_hasSCHOOL_MST_SCHOOL_KIND) {
                    paramMap.put("schoolMstSchoolKind", schoolKind);
                }

                KNJ_SchoolinfoSql obj = new KNJ_SchoolinfoSql("10000");
                final String sql = obj.pre_sql(paramMap);
                if (_isOutputDebugQuery) {
                    log.info(" schoolinfoSql sql = " + sql);
                }
                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql, new Object[] {_year, _year}));
                _schoolAddress1 = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOLADDR1"));
                _schoolAddress2 = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOLADDR2"));
                _schoolZipcode = KnjDbUtils.getString(row, "SCHOOLZIPCD");
                _schoolName1 = KnjDbUtils.getString(row, "SCHOOLNAME1"); // SCHOOL_MST
            } catch (Exception e) {
                log.error("Exception", e);
            }

            final String certifKindCd = CERTIF_KINDCD;
            final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT"
                    + " WHERE YEAR='" + _year + "'"
                    + " AND CERTIF_KINDCD='" + certifKindCd + "'";

            _certifSchoolSchoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + _certifSchoolSchoolName + "]");

            try {
                _bunkouSchoolAddress1 = "";
                _bunkouSchoolAddress2 = "";
                _bunkouSchoolName = "";
                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "' "));
                _bunkouSchoolAddress1 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK8"));
                _bunkouSchoolAddress2 = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK9"));
                _bunkouSchoolName = KnjDbUtils.getString(row, "REMARK4");
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private List<KNJA130_0> getPrintForm(final Vrw32alp svf, final HttpServletRequest request, final DB2UDB db2) {
            final List<KNJA130_0> rtn = new ArrayList<KNJA130_0>();

            // 様式１（学籍に関する記録）
            if (null != _seito) {
                if (null == _knja130_1) {
                    _knja130_1 = new KNJA130_1(svf, this);
                }
                rtn.add(_knja130_1);
            }

            // 様式１の裏（修得単位の記録）
            if (null != _tani) {
                if (null == _knja130_2) {
                    _knja130_2 = new KNJA130_2(svf, this);
                }
                rtn.add(_knja130_2);
            }

            if (_z010.in(Z010.tokyoto) && null != _shukketsu) {
                if (null == _knja130_4t) {
                    _knja130_4t = new KNJA130_4T(svf, this);
                }
                rtn.add(_knja130_4t);
            }

            // 様式２（指導に関する記録）
            if (null != _gakushu) {
                if (isTokubetsuShien() && CHITEKI1_知的障害.equals(_chiteki)) {
                    if (null == _knja134h_3) {
                        _knja134h_3 = new KNJA134H_3(svf, this);
                    }
                    rtn.add(_knja134h_3);
                } else {
                    if (null == _knja130_3) {
                        _knja130_3 = new KNJA130_3(svf, this);
                    }
                    rtn.add(_knja130_3);
                }
            }

            // 様式２の裏（所見等）
            if (null != _katsudo) {
                if (null == _knja130_4) {
                    _knja130_4 = new KNJA130_4(svf, this);
                }
                rtn.add(_knja130_4);
            }

            // 特例の授業等の記録
            if (null != _online) {
                if (null == _knja129) {
                    _knja129 = new KNJA129Delegate(svf, this, request, db2);
                }
                rtn.add(_knja129);
            }

            return rtn;
        }
    }
}
