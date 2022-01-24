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

import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJPropertiesShokenSize;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.SvfForm.Line;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 幼稚園用
 */

public class KNJA133K {
    private static final Log log = LogFactory.getLog(KNJA133K.class);

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
            nonedata = printSvf(db2, svf, param);
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                param.closeStatementQuietly();
                param.closeForm();
            }

            // 終了処理
            sd.closeSvf(svf, nonedata);
            sd.closeDb(db2);
        }
    }

    private Param getParam(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) throws SQLException {
        log.fatal("$Id$"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2, paramMap);
    }

    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;
        final Map<String, StaffMst> staffMstMap = StaffMst.load(db2, param._year);

        final List<KNJA133K_0> knjobj = param.getKnja133List(svf);

        final List<Student> studentList = getStudentList(db2, param, staffMstMap);

        for (final Student student : studentList) {
            log.debug(" schregno = " + student._schregno);
            for (final KNJA133K_0 knja133k : knjobj) {
                if (knja133k.printSvf(db2, student)) {
                    nonedata = true; // 印刷処理
                }
            }
        }
        return nonedata;
    }

    private List<Student> getStudentList(final DB2UDB db2, final Param param, final Map<String, StaffMst> staffMstMap) {

        final List<String> schregnoList = param.getSchregnoList(db2);

        final List<Student> studentList = new ArrayList();
        for (final String schregno : schregnoList) {
            final Student student = new Student(schregno);
            studentList.add(student);
        }
        Student.setSchregEntGrdHistComebackDat(db2, param, studentList, staffMstMap);
        for (final Student student : studentList) {
            student.load(db2, param, staffMstMap);
        }
        return studentList;
    }

    private static String defstr(final Object str1) {
        return Util.defstr(str1);
    }

    private static String defstr(final Object str1, final String ... str2) {
        return Util.defstr(str1, str2);
    }

    /**
     * 園児情報
     */
    private static class Student {
        final String _schregno;
        List<SchregRegdDat> _regdList = Collections.emptyList();
        Map<String, Map<String, String>> _yearCertifSchoolMap = Collections.emptyMap();
        PersonalInfo _personalInfo;
        List<Attend> _attendList;
        HtrainremarkHdat _htrainremarkHdat;
        Map<String, HtrainremarkKDat> _htrainRemarkKDatMap;
        List<String> _afterGraduatedCourseTextList;
        List<PersonalInfo> _schregEntGrdHistComebackDatList;
        public Student(final String schregno) {
            _schregno = schregno;
        }

        public void load(final DB2UDB db2, final Param param, final Map<String, StaffMst> staffMstMap) {
            _yearCertifSchoolMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, " SELECT YEAR, T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME FROM CERTIF_SCHOOL_DAT T6 WHERE T6.CERTIF_KINDCD = '" + Param.CERTIF_KINDCD + "'"), "YEAR");

            final String psRegdKey = "PS_REGD";
            if (null == param.getPs(psRegdKey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT SCHREGNO, YEAR, SEMESTER, GRADE, HR_CLASS, ATTENDNO, COURSECD, MAJORCD, COURSECODE ");
                stb.append(" FROM SCHREG_REGD_DAT ");
                stb.append(" WHERE SCHREGNO = ? ");
                stb.append(" ORDER BY YEAR, SEMESTER ");

                if (param._isOutputDebugQuery) {
                    log.info(" regd sql = " + stb.toString());
                }

                param.setPs(psRegdKey, db2, stb.toString());
            }

            _regdList = new ArrayList<SchregRegdDat>();
            for (final Map row : KnjDbUtils.query(db2, param.getPs(psRegdKey), new String[] { _schregno })) {
                SchregRegdDat regd = SchregRegdDat.create();
                regd._year = KnjDbUtils.getString(row, "YEAR");
                regd._semester = KnjDbUtils.getString(row, "SEMESTER");
                regd._grade = KnjDbUtils.getString(row, "GRADE");
                regd._hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                regd._coursecd = KnjDbUtils.getString(row, "COURSECD");
                regd._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                regd._coursecode = KnjDbUtils.getString(row, "COURSECODE");
                _regdList.add(regd);
            }

            _personalInfo = PersonalInfo.load(db2, param, this, staffMstMap, null);
            _attendList = Attend.load(db2, param, this);
            _htrainremarkHdat = HtrainremarkHdat.load(db2, param, this);
            _htrainRemarkKDatMap = HtrainremarkKDat.load(db2, param, this);
            _afterGraduatedCourseTextList = AfterGraduatedCourse.loadTextList(db2, param, _personalInfo._gakusekiList, this);
        }

        public static TreeMap<String, Gakuseki> gakusekiYearMap(final List<Gakuseki> gakusekiList) {
            final TreeMap<String, Gakuseki> set = new TreeMap<String, Gakuseki>();
            for (final Gakuseki g : gakusekiList) {
                set.put(g._year, g);
            }
            return set;
        }

        /**
         * 印刷する園児情報
         */
        private List<PersonalInfo> getPrintSchregEntGrdHistList(final Param param) {
            final List<PersonalInfo> rtn = new ArrayList<PersonalInfo>();
            if (_schregEntGrdHistComebackDatList.size() == 0) {
                return Collections.singletonList(_personalInfo);
            }
            // 復学が同一年度の場合、復学前、復学後を表示
            // 復学が同一年度ではない場合、復学後のみ表示
            final List<PersonalInfo> personalInfoList = new ArrayList<PersonalInfo>();
            personalInfoList.addAll(_schregEntGrdHistComebackDatList);
            personalInfoList.add(_personalInfo);
            for (final PersonalInfo personalInfo : personalInfoList) {
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd(param);
                if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
                    rtn.add(personalInfo);
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
            final TreeSet yearSetAll = new TreeSet();
            final List<PersonalInfo> personalInfoList = getPrintSchregEntGrdHistList(param);
            for (final ListIterator<PersonalInfo> it = personalInfoList.listIterator(personalInfoList.size()); it.hasPrevious();) { // 新しい生徒情報順
                final PersonalInfo personalInfo = it.previous();
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd(param);
                final TreeSet yearSet = new TreeSet();
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
                    return ((Integer) yearSet.last()).intValue();
                }
            }
            return -1; // 対象の生徒情報は成績等は表示しない
        }

//        public int currentGradeCd(final Param param) {
//            final int paramYear = Integer.parseInt(param._year);
//            int diffyear = 100;
//            int currentGrade = -1;
//            for (final Gakuseki gakuseki : _personalInfo._gakusekiList) {
//                if (!StringUtils.isNumeric(gakuseki._year) || -1 == gakuseki._gradeCd) {
//                    continue;
//                }
//                final int dy = paramYear - Integer.parseInt(gakuseki._year);
//                if (dy >= 0 && diffyear > dy) {
//                    currentGrade = gakuseki._gradeCd;
//                    diffyear = dy;
//                }
//            }
//            return currentGrade;
//        }

        private static void setSchregEntGrdHistComebackDat(final DB2UDB db2, final Param param, final List<Student> studentList, final Map<String, StaffMst> staffMstMap) {
            PreparedStatement ps = null;
            final Map<String, List<String>> schregComebackDateMap = new HashMap<String, List<String>>();
            try {
                for (final Student student : studentList) {
                    student._schregEntGrdHistComebackDatList = Collections.EMPTY_LIST;
                }
                if (!param._hasSchregEntGrdHistComebackDat) {
                    return;
                }
                final String sql =
                        " SELECT T1.* "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' "
                        + " ORDER BY COMEBACK_DATE ";
                // log.debug(" comeback sql = " + sql);
                ps = db2.prepareStatement(sql);
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
            for (final Student student : studentList) {
                for (final Map<String ,String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                    Util.getMappedList(schregComebackDateMap, student._schregno).add(KnjDbUtils.getString(row, "COMEBACK_DATE"));
                }
            }
            for (final Student student : studentList) {
                if (null == schregComebackDateMap.get(student._schregno)) {
                    continue;
                }
                student._schregEntGrdHistComebackDatList = new ArrayList();
                final List<String> comebackDateList = Util.getMappedList(schregComebackDateMap, student._schregno);
                log.debug(" schregno = " + student._schregno + ",  comebackdate = " + comebackDateList);
                for (final String comebackDate : comebackDateList) {
                    PersonalInfo comebackPersonalInfo = PersonalInfo.load(db2, param, student, staffMstMap, comebackDate);
                    student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
                }
            }
        }
    }

    /**
     * 生徒情報
     */
    private static class PersonalInfo {
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
        final String _sex;
        final String _baseDetailMst002Remark1;

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
        String _curriculumYear;
        String _tengakuSakiZenjitu;
        String _nyugakumaeSyussinJouhou;
        String _comebackDate;

        List<Address> _addressList;
        List<Address> _guardianAddressList;
        List<Gakuseki> _gakusekiList;
        List _transferInfoList;

        /**
         * コンストラクタ。
         */
        public PersonalInfo(final Map<String, String> row) {
            final String name = defstr(KnjDbUtils.getString(row, "NAME"));
            final String nameHistFirst = defstr(KnjDbUtils.getString(row, "NAME_HIST_FIRST"));
            final String realName = defstr(KnjDbUtils.getString(row, "REAL_NAME"));
            final String realNameHistFirst = defstr(KnjDbUtils.getString(row, "REAL_NAME_HIST_FIRST"));
            final String nameWithRealNameHistFirst = defstr(KnjDbUtils.getString(row, "NAME_WITH_RN_HIST_FIRST"));

//            final String finishDate = KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP_M(db2, KnjDbUtils.getString(row, "FINISH_DATE")), param._year);
            final String nameKana = defstr(KnjDbUtils.getString(row, "NAME_KANA"));
            final String guardKana = defstr(KnjDbUtils.getString(row, "GUARD_KANA"));
            final String guardName = defstr(KnjDbUtils.getString(row, "GUARD_NAME"));
            final String guardNameHistFirst = defstr(KnjDbUtils.getString(row, "GUARD_NAME_HIST_FIRST"));
            final String realNameKana = defstr(KnjDbUtils.getString(row, "REAL_NAME_KANA"));
            final String guardRealKana = defstr(KnjDbUtils.getString(row, "GUARD_REAL_KANA"));
            final String guardRealName = defstr(KnjDbUtils.getString(row, "GUARD_REAL_NAME"));
            final String guardRealNameHistFirst = defstr(KnjDbUtils.getString(row, "G_R_NAME_WITH_RN_HIST_FIRST"));
            final String guardNameWithGuardRealNameHistFirst = defstr(KnjDbUtils.getString(row, "G_NAME_WITH_RN_HIST_FIRST"));
            _useRealName = "1".equals(KnjDbUtils.getString(row, "USE_REAL_NAME"));
            _nameOutputFlg = "1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG"));
            _studentName = name;
            _studentRealName = realName;
            if (_useRealName) {
                if (_nameOutputFlg) {
                    _studentKana = StringUtils.isBlank(realNameKana + nameKana) ? "" : realNameKana + "（" + nameKana + "）";
                    _studentName1         = StringUtils.isBlank(realName + name) ? "" : realName + "（" + name + "）";
                    _studentNameHistFirst = StringUtils.isBlank(realNameHistFirst + nameWithRealNameHistFirst) ? "" : realNameHistFirst + "（" + nameWithRealNameHistFirst + "）";
                } else {
                    _studentKana = realNameKana;
                    _studentName1         = realName;
                    _studentNameHistFirst = realNameHistFirst;
                }
            } else {
                _studentKana = nameKana;
                _studentName1         = name;
                _studentNameHistFirst = nameHistFirst;
            }
            if ("1".equals(KnjDbUtils.getString(row, "USE_GUARD_REAL_NAME"))) {
                if ("1".equals(KnjDbUtils.getString(row, "GUARD_NAME_OUTPUT_FLG"))) {
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
                _guardNameHistFirst = guardNameHistFirst;
            }

            _courseName = KnjDbUtils.getString(row, "COURSENAME");
            _majorName = KnjDbUtils.getString(row, "MAJORNAME");

            _birthdayFlg = KnjDbUtils.getString(row, "BIRTHDAY_FLG");
            _birthday = KnjDbUtils.getString(row, "BIRTHDAY");
            _sex = KnjDbUtils.getString(row, "SEX");
            _baseDetailMst002Remark1 = KnjDbUtils.getString(row, "BDM002REMARK1");
        }

        public Tuple<Integer, Integer> getAgeMonth(final String year) {
            String calcDate = year + "-04-01";
            if (null != _entDate && _entDate.compareTo(calcDate) > 0) {
                calcDate = _entDate;
            }
            //log.debug(" calcDate = " + calcDate + " / " + _entDate);
            return Util.diffYearMonth(_birthday, calcDate);
        }

        public String getAgeMonthString(final String year) {
            final Tuple<Integer, Integer> yearMonth = getAgeMonth(year);
            final int age = yearMonth._first;
            final int month = yearMonth._second;
            return age + "歳" + month + "か月";

        }

        public int getYearBegin() {
            return null == _entDate ? 0 : Util.getNendo(Util.getCalendarOfDate(_entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _grdDate ? 9999 : Util.getNendo(Util.getCalendarOfDate(_grdDate)));
        }

        public boolean isTennyu() {
            return null != _entDiv && _entDiv.intValue() == 4;
        }

        public boolean isTengaku() {
            return null != _grdDiv && _grdDiv.intValue() == 3;
        }

        public boolean isTaigaku() {
            return null != _grdDiv && _grdDiv.intValue() == 2;
        }

        /**
         * 生徒情報を得る
         */
        public static PersonalInfo load(final DB2UDB db2, final Param param, final Student student, final Map<String, StaffMst> staffMstMap, final String comebackDate) {
            final String sql = sql_info_reg(param, student._schregno, comebackDate);
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));

            final PersonalInfo personalInfo = new PersonalInfo(row);
            final String sql_state = sql_state(param, student._schregno, comebackDate);
            final Map rowState = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql_state));

            personalInfo._entYear    = KnjDbUtils.getString(rowState, "ENT_YEAR");
            personalInfo._entSemester = Semester.get(param, personalInfo._entYear, KnjDbUtils.getString(rowState, "ENT_SEMESTER"));

            personalInfo._entDate    = KnjDbUtils.getString(rowState, "ENT_DATE");
            personalInfo._entReason  = KnjDbUtils.getString(rowState, "ENT_REASON");
            personalInfo._entSchool  = KnjDbUtils.getString(rowState, "ENT_SCHOOL");
            personalInfo._entAddr    = KnjDbUtils.getString(rowState, "ENT_ADDR");
            personalInfo._entDiv     = StringUtils.isNumeric(KnjDbUtils.getString(rowState, "ENT_DIV")) ? Integer.valueOf(KnjDbUtils.getString(rowState, "ENT_DIV")) : null;
            personalInfo._entDivName = KnjDbUtils.getString(rowState, "ENT_DIV_NAME");
            personalInfo._grdYear    = KnjDbUtils.getString(rowState, "GRD_YEAR");
            personalInfo._grdSemester = Semester.get(param, personalInfo._grdYear, KnjDbUtils.getString(rowState, "GRD_SEMESTER"));
            personalInfo._grdDate    = KnjDbUtils.getString(rowState, "GRD_DATE");
            personalInfo._grdReason  = KnjDbUtils.getString(rowState, "GRD_REASON");
            personalInfo._grdSchool  = KnjDbUtils.getString(rowState, "GRD_SCHOOL");
            personalInfo._grdAddr    = KnjDbUtils.getString(rowState, "GRD_ADDR");
            personalInfo._grdNo      = KnjDbUtils.getString(rowState, "GRD_NO");
            personalInfo._grdDiv     = StringUtils.isNumeric(KnjDbUtils.getString(rowState, "GRD_DIV")) ? Integer.valueOf(KnjDbUtils.getString(rowState, "GRD_DIV")) : null;
            personalInfo._grdDivName = KnjDbUtils.getString(rowState, "GRD_DIV_NAME");

            final String curriculumYear = KnjDbUtils.getString(rowState, "CURRICULUM_YEAR");
            final String tengakuSakiZenjitu = KnjDbUtils.getString(rowState, "TENGAKU_SAKI_ZENJITU");
            final String nyugakumaeSyussinJouhou = KnjDbUtils.getString(rowState, "NYUGAKUMAE_SYUSSIN_JOUHOU");

            personalInfo._curriculumYear = curriculumYear;
            personalInfo._tengakuSakiZenjitu = tengakuSakiZenjitu;
            personalInfo._nyugakumaeSyussinJouhou = nyugakumaeSyussinJouhou;
            personalInfo._addressList = Address.load(db2, param, false, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._guardianAddressList = Address.load(db2, param, true, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._comebackDate = comebackDate;
            personalInfo._gakusekiList = Gakuseki.load(db2, param, staffMstMap, student, personalInfo._grdDate);
            personalInfo._transferInfoList = loadTransferList(db2, param, student, personalInfo._entDate, personalInfo._grdDate);
            return personalInfo;
        }

        /**
         * 住所履歴を得る
         */
        public static List<Map<String, String>> loadTransferList(final DB2UDB db2, final Param param, final Student student, final String startDate, final String endDate) {
            final List<Map<String, String>> transferList = new ArrayList<Map<String, String>>();
            if (null == param._seito) {
                return transferList;
            }
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * ");
            sql.append(" FROM SCHREG_TRANSFER_DAT ");
            sql.append(" WHERE SCHREGNO = '" + student._schregno + "' ");
            sql.append(" ORDER BY TRANSFER_SDATE ");
            transferList.addAll(KnjDbUtils.query(db2, sql.toString()));
            return transferList;
        }

        public static String sql_info_reg(final Param param, final String schregno, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append("  T2.NAME,");
            sql.append("  T2.NAME_KANA,");
            sql.append("  EGHIST.GRD_DATE, ");
            sql.append("  T14.NAME AS NAME_HIST_FIRST, ");
            sql.append("  T18.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            sql.append("  T2.BIRTHDAY, T2.SEX AS SEX_FLG, T7.ABBV1 AS SEX,");
            sql.append("  T21.BIRTHDAY_FLG, ");
            sql.append("  T2.REAL_NAME, ");
            sql.append("  T2.REAL_NAME_KANA, ");
            sql.append("  T18.REAL_NAME AS REAL_NAME_HIST_FIRST, ");
            sql.append("  T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,");
            // 課程・学科・コース
            sql.append("  T3.COURSENAME,T4.MAJORNAME,T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,");
            // 卒業
            sql.append("  CASE WHEN EGHIST.GRD_DATE IS NULL THEN RTRIM(CHAR(INT(T1.YEAR)+case t1.annual when '01' then 3 when '02' then 2 else 1 end)) || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-'  || RTRIM(CHAR(DAY(T10.GRADUATE_DATE))) ELSE VARCHAR(EGHIST.GRD_DATE) END AS GRADU_DATE,");
            sql.append("  CASE WHEN EGHIST.GRD_DATE IS NULL THEN '卒業見込み' ELSE ");
            sql.append("  (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' ");
            sql.append("  AND EGHIST.GRD_DIV = ST2.NAMECD2) END AS GRADU_NAME,");
            // 入学
            sql.append("  EGHIST.ENT_DATE,EGHIST.ENT_DIV,");
            sql.append("  (SELECT DISTINCT ANNUAL FROM SCHREG_REGD_DAT ST1,SCHREG_BASE_MST ST2 ");
            sql.append("  WHERE ST1.SCHREGNO=ST2.SCHREGNO AND ST1.YEAR=FISCALYEAR(ST2.ENT_DATE) AND ");
            sql.append("  ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,");

            // 卒業情報
            sql.append("  EGHIST.FINISH_DATE,");
            sql.append("  FIN_S.FINSCHOOL_NAME AS J_NAME,");
            sql.append("  NM_MST.NAME1 AS INSTALLATION_DIV,");
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
            sql.append("  (CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("  T11.NAME_OUTPUT_FLG, ");
            sql.append("  (CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("  T26.GUARD_NAME_OUTPUT_FLG, ");
            sql.append("  BDM002.BASE_REMARK1 AS BDM002REMARK1, ");
            sql.append("  T1.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("  (   SELECT     * ");
            sql.append("  FROM       SCHREG_REGD_DAT T1 ");
            sql.append("  WHERE      T1.SCHREGNO='" + schregno + "' AND T1.YEAR= '" + param._year + "' ");
            sql.append("  AND T1.SEMESTER= '" + param._gakki + "' "); // 学期を特定
            sql.append("  ) T1 ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            sql.append("  AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            // 卒業情報有りの場合
            sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("  AND T10.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=T2.SEX ");
            sql.append("LEFT JOIN ");
            if (null != comebackDate) {
                sql.append(" SCHREG_ENT_GRD_HIST_COMEBACK_DAT EGHIST ON EGHIST.COMEBACK_DATE = '" + comebackDate + "' AND ");
            } else {
                sql.append(" SCHREG_ENT_GRD_HIST_DAT EGHIST ON ");
            }
            sql.append("    EGHIST.SCHREGNO = T1.SCHREGNO AND EGHIST.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = EGHIST.FINSCHOOLCD ");
            sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
            sql.append("  AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
            // 生徒住所
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = T2.SCHREGNO AND T11.DIV = '02' ");
            // 保護者情報
            sql.append("LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ");
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T13 ON T13.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T14 ON T14.SCHREGNO = T13.SCHREGNO AND T14.ISSUEDATE = T13.ISSUEDATE ");

            // 保護者履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T15 ON T15.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN GUARDIAN_HIST_DAT T16 ON T16.SCHREGNO = T15.SCHREGNO AND T16.ISSUEDATE = T15.ISSUEDATE ");
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T17 ON T17.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT T18 ON T18.SCHREGNO = T17.SCHREGNO AND T18.ISSUEDATE = T17.ISSUEDATE ");

            // 保護者履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) T19 ON T19.SCHREGNO = T2.SCHREGNO ");
            sql.append("LEFT JOIN GUARDIAN_HIST_DAT T20 ON T20.SCHREGNO = T19.SCHREGNO AND T20.ISSUEDATE = T19.ISSUEDATE ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");

            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = T2.SCHREGNO AND T26.DIV = '02' ");
            sql.append("LEFT JOIN SCHREG_BASE_DETAIL_MST BDM002 ON BDM002.SCHREGNO = T2.SCHREGNO AND BDM002.BASE_SEQ = '002' ");

            return sql.toString();
        }

        private static String sql_state(final Param param, final String schregno, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ESEME.SEMESTER AS ENT_SEMESTER, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            sql.append("    ENT_DIV, ");
            sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GSEME.SEMESTER AS GRD_SEMESTER, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
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

        public List<List<Gakuseki>> splitByAge(final int splitAge) {
            final List<List<Gakuseki>> rtn = new ArrayList<List<Gakuseki>>();
            int idx = -1;
            for (int i = 0; i < _gakusekiList.size(); i++) {
                final Gakuseki g = _gakusekiList.get(i);
                final Tuple<Integer, Integer> ageMonth = getAgeMonth(g._year);
                final int age = ageMonth._first;
                if (splitAge <= age) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) {
                rtn.add(_gakusekiList);
            } else {
                rtn.add(_gakusekiList.subList(0, idx));
                rtn.add(_gakusekiList.subList(idx, _gakusekiList.size()));
            }
            return rtn;
        }

        public List<List<Gakuseki>> splitByGradeCd(final String splitGradeCd) {
            final List<List<Gakuseki>> rtn = new ArrayList<List<Gakuseki>>();
            int idx = -1;
            for (int i = 0; i < _gakusekiList.size(); i++) {
                final Gakuseki g = _gakusekiList.get(i);
                if (null == g._gradeCd || splitGradeCd.compareTo(g._gradeCd) <= 0) {
                    idx = i;
                    break;
                }
            }
            if (idx == -1) {
                rtn.add(_gakusekiList);
            } else {
                rtn.add(_gakusekiList.subList(0, idx));
                rtn.add(_gakusekiList.subList(idx, _gakusekiList.size()));
            }
            return rtn;
        }
    }

    /**
     * 在籍データ
     */
    private static class Gakuseki {

        final String _year;
        final String _grade;
        final String _hrname;
        final String _attendno;
        final String _nendo;
        final String _gradeCd;
        final Staff _principal;
        final Staff _principal1;
        final Staff _principal2;
        final String _staffSeq;
        final String _principalSeq;
        final String _kaizanFlg;

        public Gakuseki(
                final String year,
                final String grade,
                final String hrname,
                final String attendno,
                final String nendo,
                final String gradeCd,
                final Staff principal,
                final Staff principal1,
                final Staff principal2,
                final String chageOpiSeq,
                final String lastOpiSeq,
                final String flg,
                final String lastStampNo,
                final String lastStampNo1) {
            _year = year;
            _grade = grade;
            _hrname = hrname;
            _attendno = attendno;
            _nendo = nendo;
            _gradeCd = gradeCd;
            _staffSeq = chageOpiSeq;
            _principalSeq = lastOpiSeq;
            _kaizanFlg = flg;
            _principal = principal;
            _principal1 = principal1;
            _principal2 = principal2;
        }

        /**
         * 在籍データのリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List<Gakuseki> load(final DB2UDB db2, final Param param, final Map<String, StaffMst> staffMstMap, final Student student, final String grdDate) {
            final List<Gakuseki> gakusekiList = new ArrayList();
            final String sql = sqlSchGradeRec(param, student, grdDate);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {

                final String year = KnjDbUtils.getString(row, "YEAR");
                final String grade = KnjDbUtils.getString(row, "GRADE");
//                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
//                final String principalstaffcd = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD");
                final String principalStaffcd1 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD1");
                final String principalStaffcd2 = KnjDbUtils.getString(row, "PRINCIPALSTAFFCD2");

                final String principalname = KnjDbUtils.getString(row, "PRINCIPALNAME");

                final String principal1FromDate = KnjDbUtils.getString(row, "PRINCIPAL1_FROM_DATE");
                final String principal1ToDate = KnjDbUtils.getString(row, "PRINCIPAL1_TO_DATE");
                final String principal2FromDate = KnjDbUtils.getString(row, "PRINCIPAL2_FROM_DATE");
                final String principal2ToDate = KnjDbUtils.getString(row, "PRINCIPAL2_TO_DATE");

                final String chageOpiSeq = KnjDbUtils.getString(row, "CHAGE_OPI_SEQ");
                final String lastOpiSeq = KnjDbUtils.getString(row, "LAST_OPI_SEQ");
                final String flg = KnjDbUtils.getString(row, "FLG");
//                final String chageStampNo = KnjDbUtils.getString(row, "CHAGE_STAMP_NO");
                final String lastStampNo = KnjDbUtils.getString(row, "LAST_STAMP_NO");
                final String lastStampNo1 = KnjDbUtils.getString(row, "LAST_STAMP_NO1");

                final String hrname = KnjDbUtils.getString(row, "HR_NAME");
                final String nendo = KNJ_EditDate.setNendoFormat(db2, KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度", param._year);
                final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");

                final Staff principal = new Staff(year, new StaffMst(null, principalname, null, null, null), null, null, lastStampNo);
                final Staff principal1 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, lastStampNo1);
                final Staff principal2 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);

                final Gakuseki gakuseki = new Gakuseki(year, grade, hrname, attendno, nendo, gradeCd,
                        principal, principal1, principal2,
                        chageOpiSeq, lastOpiSeq, flg, lastStampNo, lastStampNo1);
                gakusekiList.add(gakuseki);
            }
            return gakusekiList;
        }

        public String toString() {
            return "Gakuseki(year = " + _year + ", grade = " + _grade + ", gradeCd = " + _gradeCd + ")";
        }

        public int getPos(final Param param) {
            final int pos;
            if ("1".equals(param._useSpecial_Support_School)) {
                pos = param.getGradeCd(_year, _grade); // [1, 2, 3]
            } else {
                pos = param.getGradeCd(_year, _grade) - 1; // [3, 4, 5, 6] - 1
            }
            return pos;
        }

        public static Gakuseki minGakuseki(final List<Gakuseki> gakusekiList) {
            return gakusekiList.size() > 0 ? gakusekiList.get(0) : null;
        }

        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param, final Student student, final String grdDate) {
            final StringBuffer stb = new StringBuffer();
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
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER = (SELECT SEMESTER FROM  MIN_YEAR_SEMESTER WHERE YEAR = T1.YEAR) ");
            stb.append(" ), PRINCIPAL_HIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR ORDER BY T1.FROM_DATE) AS ORDER ");
            stb.append("     FROM ");
            stb.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,REGD T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
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
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,T3.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("         ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,VALUE(GDAT.GRADE_CD, GDAT2.GRADE_CD) AS GRADE_CD ");
            stb.append("   ,T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME ");
            stb.append("   ,T13.STAFFCD AS PRINCIPALSTAFFCD1 ");
            stb.append("   ,T14.STAFFCD AS PRINCIPALSTAFFCD2 ");
            stb.append("   ,T12.PRINCIPAL1_FROM_DATE, T12.PRINCIPAL1_TO_DATE ");
            stb.append("   ,T12.PRINCIPAL2_FROM_DATE, T12.PRINCIPAL2_TO_DATE ");

            // 印鑑関連 2
            stb.append("   ,ATTEST.CHAGE_OPI_SEQ ");
            stb.append("   ,ATTEST.LAST_OPI_SEQ ");
            stb.append("   ,ATTEST.FLG ");
            stb.append("   ,IN2.STAMP_NO AS LAST_STAMP_NO ");
            stb.append("   ,IN21.STAMP_NO AS LAST_STAMP_NO1 ");
            stb.append("   ,IN22.STAMP_NO AS LAST_STAMP_NO2 ");

            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("                              AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT2 ON GDAT2.YEAR = '" + param._year + "' AND GDAT2.GRADE = T1.GRADE ");

            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '" + Param.CERTIF_KINDCD + "'");

//            // 印鑑関連 3
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

            stb.append(" ORDER BY T1.YEAR, T1.HR_CLASS ");
            return stb.toString();
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
        public StaffMst(final String staffcd, final String name, final String kana, final String nameReal, final String kanaReal) {
            _staffcd = staffcd;
            _name = name;
            _kana = kana;
            _nameReal = nameReal;
            _kanaReal = kanaReal;
            _yearStaffNameSetUp = new HashMap();
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

        public List<String> getNameLine(final String year) {
            final String[] name;
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    if (StringUtils.isBlank(_name)) {
                        name = new String[]{_nameReal};
                    } else {
                        final String n = "（" + _name + "）";
                        if ((null == _nameReal ? "" : _nameReal).equals(_name)) {
                            name =  new String[]{_nameReal};
                        } else if (KNJ_EditEdit.getMS932ByteLength(_nameReal + n) > 26) {
                            name =  new String[]{_nameReal, n};
                        } else {
                            name =  new String[]{_nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    name = new String[]{_nameReal};
                }
            } else {
                name = new String[]{_name};
            }
            return Arrays.asList(name);
        }

        public static StaffMst get(final Map<String, StaffMst> staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return staffMstMap.get(staffcd);
        }

        public static Map<String, StaffMst> load(final DB2UDB db2, final String year) {
            final Map<String, StaffMst> rtn = new HashMap();

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
                if (null == rtn.get(m.get("STAFFCD"))) {
                    continue;
                }
                final StaffMst s = rtn.get(m.get("STAFFCD"));

                final Map nameSetupDat = new HashMap();
                nameSetupDat.put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
                s._yearStaffNameSetUp.put(m.get("YEAR"), nameSetupDat);
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + ", nameSetupDat=" + _yearStaffNameSetUp + ")";
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
            final List<String> name = _staffMst.getNameLine(_year);
            for (int i = 0; i < name.size(); i++) {
                if (null == name.get(i)) continue;
                stb.append(name.get(i));
            }
            return stb.toString();
        }

        public List<String> getNameBetweenLine(final int keta) {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";

            final List rtn;
            if (KNJ_EditEdit.getMS932ByteLength(getNameString() + between) > keta) {
                rtn = Arrays.asList(getNameString(), between);
            } else {
                rtn = Arrays.asList(getNameString() + between);
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
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            sql.append(" ) ");
            sql.append("     SELECT ");
            sql.append("         T1.YEAR, T1.STAFFCD, T1.FROM_DATE, T1.TO_DATE ");
            sql.append("     FROM PRINCIPAL_HIST T1 ");
            sql.append("     ORDER BY T1.YEAR, T1.ORDER ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                Util.getMappedList(_yearPrincipalListMap, KnjDbUtils.getString(row, "YEAR")).add(row);
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
                Util.getMappedMap(_inkanMap, KnjDbUtils.getString(row, "STAFFCD")).put(KnjDbUtils.getInt(row, "YEAR", null), KnjDbUtils.getString(row, "STAMP_NO"));
            }
        }

        private String getStampNo(final Param param, final String staffcd, final String year) {
            if (null == _inkanMap.get(staffcd) || !NumberUtils.isDigits(year)) {
                return null;
            }
            String stampNo = null;
            final Map<Integer, String> yearStampnoMap = Util.getMappedMap(_inkanMap, staffcd);
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

                Util.getMappedList(Util.getMappedTreeMap(rtn, staffClassHistKey(year, grade, hrClass, trDiv)), staffFromDate).add(staff);
            }
            _staffClassHistMap = rtn;
        }

        private static String staffClassHistKey(final String year, final String grade, final String hrClass, final String trDiv) {
            return year + ":" + grade + ":" + hrClass + ":" + trDiv;
        }

        private List<Staff> getHistStaffList(final String year, final String grade, final String hrClass, final String trDiv) {
            final List<Staff> staffList = new ArrayList();
            final TreeMap<String, List<Staff>> grhrStaffMap = Util.getMappedTreeMap(_staffClassHistMap, staffClassHistKey(year, grade, hrClass, trDiv));
            for (final List<Staff> fromDateStaffList : grhrStaffMap.values()) {
                if (fromDateStaffList.size() > 0) {
                    staffList.add(fromDateStaffList.get(0)); // 同一日付なら最小職員コードの職員
                }
            }
            return staffList;
        }

        public List<Staff> getStudentStaffHistList(final Param param, final Student student, final PersonalInfo pInfo, final String trDiv, final String year) {
            final List<SchregRegdDat> regdInYear = Util.getMappedList(SchregRegdDat.getYearRegdListMap(student._regdList), year);
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
            final List<Attend> attendList = new ArrayList();
            try {
                if (null == param._psMap.get("PS_ATTEND")) {
                    final String sql = getAttendSql(param);
                    param._psMap.put("PS_ATTEND", db2.prepareStatement(sql));
                }
            } catch (Exception ex) {
                log.error("printSvfAttend error!", ex);
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get("PS_ATTEND"), new Object[] { student._schregno, param._year})) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final int g = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                final String lesson = KnjDbUtils.getString(row, "LESSON");
                final String suspendMourning = KnjDbUtils.getString(row, "SUSPEND_MOURNING");
                final String abroad = KnjDbUtils.getString(row, "ABROAD");
                final String requirePresent = KnjDbUtils.getString(row, "REQUIREPRESENT");
                final String present = KnjDbUtils.getString(row, "PRESENT");
                final String absent = KnjDbUtils.getString(row, "ABSENT");
                final String late = KnjDbUtils.getString(row, "LATE");
                final String early = KnjDbUtils.getString(row, "EARLY");

                final Attend attend = new Attend(year, g, lesson, suspendMourning, abroad, requirePresent, present, absent, late, early);
                attendList.add(attend);
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
            if (param._definecode.schoolmark.substring(0, 1).equals("K")) {
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
                stb.append("  AND S1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            return stb.toString();
        }
    }

    /**
     * 住所データ
     */
    private static class Address {
        final String _issuedate;
        final String _addr1;
        final String _addr2;
        final String _zipCd;
        final boolean _isPrintAddr2;

        private Address(final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
            _issuedate = issuedate;
            _addr1 = addr1;
            _addr2 = addr2;
            _zipCd = zip;
            _isPrintAddr2 = isPrintAddr2;
        }

        /**
         * 住所履歴を得る
         */
        public static List<Address> load(final DB2UDB db2, final Param param, final boolean isGuardian, final Student student, final String startDate, final String endDate) {
            final List<Address> addressList = new ArrayList<Address>();
            if (null == param._seito) {
                return addressList;
            }
            final String sql = isGuardian ? sqlAddress(true, startDate, endDate) : sqlAddress(false, startDate, endDate);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql, new Object[] {student._schregno, param._year})) {
                final String issuedate = KnjDbUtils.getString(row, "ISSUEDATE");
                final String address1 = KnjDbUtils.getString(row, "ADDR1");
                final String address2 = KnjDbUtils.getString(row, "ADDR2");
                final boolean isPrintAddr2 = "1".equals(KnjDbUtils.getString(row, "ADDR_FLG"));
                final String zipCd = KnjDbUtils.getString(row, "ZIPCD");

                final Address address = new Address(issuedate, address1, address2, zipCd, isPrintAddr2);
                addressList.add(address);
            }
            return addressList;
        }

        public static String sqlAddress(final boolean isGuardianAddress, final String startDate, final String endDate) {

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
            return "AddressRec(" + _issuedate + "," + _addr1 + " " + _addr2 + ")";
        }
    }

    private static class HtrainremarkHdat {
//        String _totalstudyact;
//        String _totalstudyval;
        String _detail2HdatSeq001Remark1;

        public static HtrainremarkHdat load(final DB2UDB db2, final Param param, final Student student) {
            HtrainremarkHdat remarkHdat = new HtrainremarkHdat();
//            if (null == param.koudo || null == param.gakushu) {
//                return remarkHdat;
//            }
//
//            final String psKey = "PS_HRP_HD";
//            if (null == param.getPs(psKey)) {
//
//                final StringBuffer stb = new StringBuffer();
//                stb.append("SELECT ");
//                stb.append("    TOTALSTUDYACT ");
//                stb.append("   ,TOTALSTUDYVAL ");
//                stb.append("FROM    HTRAINREMARK_P_HDAT T1 ");
//                stb.append("WHERE   SCHREGNO = ? ");
//
//                param.setPs(psKey, db2, stb.toString());
//            }
//
//            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno}));
//
//            remarkHdat._totalstudyact = KnjDbUtils.getString(row, "TOTALSTUDYACT");
//            remarkHdat._totalstudyval = KnjDbUtils.getString(row, "TOTALSTUDYVAL");

            if ("1".equals(param._useSpecial_Support_School)) { // param.isTokubetsuShien()) {
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

                // 入学時の障害の状態
                remarkHdat._detail2HdatSeq001Remark1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, param._psMap.get(psKey2), new Object[] {student._schregno, "001"}));
            }
            return remarkHdat;
        }
    }

    private static class HtrainremarkKDat {
        final String _year;
        String _totalstudyact;
        String _totalstudyval;
        String _specialActRemark;
        String _totalRemark;
        String _attendrecRemark;
        String _viewremark;
        String _detail2DatSeq001Remark1;
        String _detail2DatSeq002Remark1;
        public HtrainremarkKDat(final String year) {
            _year = year;
        }

        private static HtrainremarkKDat getHTrainRemarkDat(final List<HtrainremarkKDat> list, final String year) {
            for (final HtrainremarkKDat d : list) {
                if (d._year.equals(year)) {
                    return d;
                }
            }
            return null;
        }

        public static Map<String, HtrainremarkKDat> load(final DB2UDB db2, final Param param, final Student student) {
            final Map<String, HtrainremarkKDat> rtn = new TreeMap<String, HtrainremarkKDat>();
            if (null == param._sidou) {
                return rtn;
            }
            final String psKey = "PS_HRD";
            try {
                if (null == param._psMap.get(psKey)) {
                    final String sql = getHtrainremarkDatSql();
                    if (param._isOutputDebug) {
                        log.info(" htrain sql = " + sql);
                    }
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                db2.commit();
            }
            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {param._year, student._schregno})) {
                final String year = KnjDbUtils.getString(row, "YEAR");
                if (null == year) {
                    continue;
                }
                final String totalstudyact = KnjDbUtils.getString(row, "TOTALSTUDYACT");
                final String totalstudyval = KnjDbUtils.getString(row, "TOTALSTUDYVAL");
                final String specialActRemark = KnjDbUtils.getString(row, "SPECIALACTREMARK");
                final String totalRemark = KnjDbUtils.getString(row, "TOTALREMARK");
                final String attendrecRemark = KnjDbUtils.getString(row, "ATTENDREC_REMARK");
                final String viewremark = KnjDbUtils.getString(row, "VIEWREMARK");

                final HtrainremarkKDat rem = new HtrainremarkKDat(year);
                rem._totalstudyact = totalstudyact;
                rem._totalstudyval = totalstudyval;
                rem._specialActRemark = specialActRemark;
                rem._totalRemark = totalRemark;
                rem._attendrecRemark = attendrecRemark;
                rem._viewremark = viewremark;

                rtn.put(year, rem);
            }

            if ("1".equals(param._useSpecial_Support_School)) { // param.isTokubetsuShien()) {
                final String ps2Key = "PS_HRDD";
                if (null == param.getPs(ps2Key)) {
                    final String sql = getHtrainremarkDetail2HDatSql();
                    if (param._isOutputDebug) {
                        log.info(" htrain det2 hdat sql = " + sql);
                    }
                    param.setPs(ps2Key, db2, sql);
                }

                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(ps2Key), new Object[] {param._year, student._schregno, "001"})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    if (null == year) {
                        continue;
                    }
                    if (null == rtn.get(year))  {
                        rtn.put(year, new HtrainremarkKDat(year));
                    }
                    final HtrainremarkKDat htrainremarkDat = rtn.get(year);
                    htrainremarkDat._detail2DatSeq001Remark1 = KnjDbUtils.getString(row, "REMARK1");
                }
                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(ps2Key), new Object[] {param._year, student._schregno, "002"})) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    if (null == year) {
                        continue;
                    }
                    if (null == rtn.get(year))  {
                        rtn.put(year, new HtrainremarkKDat(year));
                    }
                    final HtrainremarkKDat htrainremarkDat = rtn.get(year);
                    htrainremarkDat._detail2DatSeq002Remark1 = KnjDbUtils.getString(row, "REMARK1");
                }
            }
            return rtn;
        }

        private static String getHtrainremarkDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,TOTALSTUDYACT ");
            stb.append("       ,TOTALSTUDYVAL ");
            stb.append("       ,SPECIALACTREMARK ");
            stb.append("       ,TOTALREMARK ");
            stb.append("       ,ATTENDREC_REMARK ");
            stb.append("       ,VIEWREMARK ");
            stb.append("       ,BEHAVEREC_REMARK ");
            stb.append("FROM    HTRAINREMARK_K_DAT T1 ");
            stb.append("WHERE ");
            stb.append("    YEAR <= ? ");
            stb.append("    AND SCHREGNO = ? ");
            stb.append("ORDER BY ");
            stb.append("    T1.YEAR ");
            return stb.toString();
        }

        private static String getHtrainremarkDetail2HDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,REMARK1 ");
            stb.append("FROM HTRAINREMARK_DETAIL2_DAT T1 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR <= ? ");
            stb.append("    AND T1.SCHREGNO = ? ");
            stb.append("    AND T1.HTRAIN_SEQ = ? ");
            return stb.toString();
        }
    }

    private static class AfterGraduatedCourse {

        public static List<String> loadTextList(final DB2UDB db2, final Param param, final List<Gakuseki> gakusekiList, final Student student) {
            final List<String> textList = new ArrayList();
            if (null == param._seito || !param._hasAftGradCourseDat) {
                return textList;
            }

            final String psKey = "AFT_GRAD";
            try {

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
                    stb.append("where ");
                    stb.append("     T1.PLANSTAT = '1' ");
                    stb.append("order by ");
                    stb.append("     T1.YEAR, T1.SCHREGNO ");

                    param._psMap.put(psKey, db2.prepareStatement(stb.toString()));
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            }

            final TreeSet<String> yearSet = new TreeSet<String>(Student.gakusekiYearMap(gakusekiList).keySet());
            final String minYear = yearSet.first();
            final String maxYear = yearSet.last();

            for (final Map<String, String> row : KnjDbUtils.query(db2, param._psMap.get(psKey), new Object[] {student._schregno, minYear, maxYear})) {
                if ("0".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 進学
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E017NAME1")) {
                        final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "THINKEXAM"), 50, 10);
                        if (null != token) {
                            textList.addAll(Arrays.asList(token));
                        }
                     } else {
                         textList.add(defstr(KnjDbUtils.getString(row, "SCHOOL_NAME")));
                         textList.add(defstr(KnjDbUtils.getString(row, "FACULTYNAME")) + defstr(KnjDbUtils.getString(row, "DEPARTMENTNAME")));
                         textList.add(defstr(defstr(KnjDbUtils.getString(row, "CAMPUSFACULTYADDR1"), KnjDbUtils.getString(row, "CAMPUSADDR1"))));
                     }
                 } else if ("1".equals(KnjDbUtils.getString(row, "SENKOU_KIND"))) { // 就職
                    if (null == KnjDbUtils.getString(row, "STAT_CD") || null != KnjDbUtils.getString(row, "E018NAME1")) {
                        final String[] token = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "JOB_THINK"), 50, 10);
                        if (null != token) {
                            textList.addAll(Arrays.asList(token));
                        }
                     } else {
                        textList.add(defstr(KnjDbUtils.getString(row, "COMPANY_NAME")));
                        textList.add(defstr(KnjDbUtils.getString(row, "COMPANYADDR1")));
                        textList.add(defstr(KnjDbUtils.getString(row, "COMPANYADDR2")));
                     }
                 }
            }

            return textList;
        }
    }

    /**
     * 様式
     */
    private static abstract class KNJA133K_0 {
        static final int TOKUSHI_SPLIT_PAGE_AGE = 5;

        protected Vrw32alp _svf;
        private Param _param;
        private String _currentFormname;
        protected Map<String, Map<String, SvfField>> _fieldInfoMap = new HashMap<String, Map<String, SvfField>>();
        private Map _fixFieldInfo = new HashMap();

        KNJA133K_0(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;
        }

        protected Param param() {
            return _param;
        }

        protected int VrsOut(final String field, final String data) {
            if (null == getSvfField(field)) {
                final String message = " no such field : " + field;
                if (_param._isOutputDebug) {
                    if (!param().logged.contains(message)) {
                        log.warn(message);
                        param().logged.add(message);
                    }
                }
                return -100;
            }
            return _svf.VrsOut(field, data);
        }

        protected int VrsOutForData(final List<String> fields, final String data) {
            return VrsOut(getFieldForData(fields, data), data);
        }

        protected int VrsOutnForData(final List<String> fields, final int gyo, final String data) {
            return VrsOutn(getFieldForData(fields, data), gyo, data);
        }

        protected String getFieldForData(final List<String> fields, final String data) {
            final int datasize = KNJ_EditEdit.getMS932ByteLength(data);
            String fieldFound = null;
            searchField:
            for (int i = 0; i < fields.size(); i++) {
                final String fieldname = fields.get(i);
                final SvfField svfField = getSvfField(fieldname, false);
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

        protected int VrsOutn(final String field, final int gyo, final String data) {
            if (null == getSvfField(field)) {
                final String message = " no such field : " + field;
                if (!param().logged.contains(message)) {
                    log.warn(message);
                    param().logged.add(message);
                }
                return -100;
            }
            return _svf.VrsOutn(field, gyo, data);
        }

        protected int VrAttributen(final String field, final int gyo, final String attribute) {
            return _svf.VrAttributen(field, gyo, attribute);
        }

        protected void VrsOutRenban(final String field, final List<String> list) {
            if (null == list) {
                return;
            }
            for (int i = 0 ; i < list.size(); i++) {
                VrsOut(field + String.valueOf(i + 1), list.get(i));
            }
        }

        protected void VrsOutnRepeat(final String field, final List<String> list) {
            if (null == list) {
                return;
            }
            for (int i = 0 ; i < list.size(); i++) {
                VrsOutn(field, i + 1, list.get(i));
            }
        }

        protected List<String> getTokenList(final String targetsrc, final int dividlen, final int dividnum) {
            final List<String> lines = getTokenList(targetsrc, dividlen);
            if (lines.size() > dividnum) {
                return lines.subList(0, dividnum);
            }
            return lines;
        }

        protected static List<String> getTokenList(String targetsrc, final int dividlen) {
            return KNJ_EditKinsoku.getTokenList(targetsrc, dividlen);
        }

        protected SvfField getSvfField(final String fieldname0) {
            if (StringUtils.isBlank(fieldname0)) {
                return null;
            }
            String fieldname = fieldname0;
            if (_fixFieldInfo.containsKey(_currentFormname + "." + fieldname0)) {
                fieldname = (String) _fixFieldInfo.get(_currentFormname + "." + fieldname0);
            } else {
                if (null != Util.getMappedMap(_fieldInfoMap, _currentFormname).get(fieldname0)) {
                    fieldname = fieldname0;
                } else {
                    for (final String f : Util.getMappedMap(_fieldInfoMap, _currentFormname).keySet()) {
                        if (f.equalsIgnoreCase(fieldname0)) {
                            fieldname = f;
                            _fixFieldInfo.put(_currentFormname + "." + fieldname0, _currentFormname + "." + fieldname);
                            log.warn(" fix fieldname : " + fieldname0 + " -> " + fieldname + " ( " + _currentFormname + " )");
                            break;
                        }
                    }
                }
            }
            return Util.getMappedMap(_fieldInfoMap, _currentFormname).get(fieldname);
        }

        protected void setForm(final String formname, final int n) {
            log.info(" set form = " + formname);
            _svf.VrSetForm(formname, n);
            _currentFormname = formname;
            if (!_fieldInfoMap.containsKey(formname)) {
                _fieldInfoMap.put(formname, null);
                try {
                    _fieldInfoMap.put(formname, new TreeMap<String, SvfField>(SvfField.getSvfFormFieldInfoMapGroupByName(_svf)));
                } catch (Throwable t) {
                    log.warn(" no class SvfField.");
                }
            }
        }

        public SvfField getSvfField(final String fieldname, final boolean isLog) {
            try {
                final Map<String, SvfField> fieldMap = Util.getMappedMap(_fieldInfoMap, _currentFormname);
                SvfField f = fieldMap.get(fieldname);
                if (null == f) {
                    final Map correctMap = Util.getMappedMap(_fieldInfoMap, _currentFormname + ":CORRECT");
                    if (!correctMap.containsKey(fieldname)) {
                        for (final String cand : fieldMap.keySet()) {
                            if (cand.equalsIgnoreCase(fieldname)) {
//                                if (_param.isOutputDebugField(fieldname)) {
//                                    log.info(" correct fieldname:" + fieldname + " -> " + cand);
//                                }
                                correctMap.put(fieldname, cand);
                                break;
                            }
                        }
                        if (!correctMap.containsKey(fieldname)) {
//                            if (_param.isOutputDebugField(fieldname)) {
//                                log.info(" not found correct fieldname:" + fieldname);
//                            }
                            correctMap.put(fieldname, null);
                        }
                    }
                    f = fieldMap.get(correctMap.get(fieldname));
                }
                return f;
            } catch (Throwable t) {
                //log.info("exception!", t);
            }
            return null;
        }

        protected String[] getFieldGroupForData(final String[][] fieldGroups, final String data) {
            String[] fieldGroupFound = {};
            searchFieldGroup:
            for (int i = 0; i < fieldGroups.length; i++) {
                final String[] fieldGroup = fieldGroups[i];
                for (final String fieldname : fieldGroup) {
                    final SvfField svfField = getSvfField(fieldname, false);
                    if (null == svfField) {
                        continue searchFieldGroup;
                    }
                }
                fieldGroupFound = fieldGroup;
                if (dataFitsFieldGroup(data, fieldGroup)) {
                    return fieldGroup;
                }
            }
            return fieldGroupFound;
        }

        protected boolean dataFitsFieldGroup(final String data, final String[] fieldGroup) {
            List<String> splitToFieldSize = splitToFieldSize(fieldGroup, data);
            final boolean isFits = splitToFieldSize.size() <= fieldGroup.length;
//            if (_param._isOutputDebugSvfOut) {
//                log.info(" isFits? " + isFits + ", fieldGroup = " + ArrayUtils.toString(fieldGroup) + ", ketas = " + getFieldKetaList(fieldGroup) + ", data = " + data);
//            }
            return isFits;
        }

        protected List<String> splitToFieldSize(final String[] fields, final String data) {
            final List<Integer> ketas = getFieldKetaList(fields);
            if (ketas.size() == 0) {
                return Collections.emptyList();
            }
            final List<StringBuffer> wrk = new ArrayList<StringBuffer>();
            StringBuffer currentLine = null;
            for (final char ch : data.toCharArray()) {
                if (null == currentLine) {
                    currentLine = new StringBuffer();
                    wrk.add(currentLine);
                }
                if (ch == '\n') {
                    currentLine = new StringBuffer();
                    wrk.add(currentLine);
                    continue;
                }

                if (wrk.size() <= ketas.size()) {
                    final String chs = String.valueOf(ch);
                    final int lineKeta = wrk.size() < ketas.size() ? ketas.get(wrk.size() - 1) : ketas.get(ketas.size() - 1); // 行あふれした場合最後のフィールドを使用しておく
                    if (lineKeta < KNJ_EditEdit.getMS932ByteLength(currentLine.toString() + chs)) {
                        currentLine = new StringBuffer();
                        wrk.add(currentLine);
                    }
                    currentLine.append(chs);
                } else {
                    break;
                }
            }
            final List<String> rtn = new ArrayList<String>();
            for (final StringBuffer stb : wrk) {
                rtn.add(stb.toString());
            }
            return rtn;
        }

        protected List<Integer> getFieldKetaList(final String[] fields) {
            final List<Integer> ketas = new ArrayList<Integer>();
            for (final String fieldname : fields) {
                final SvfField svfField = getSvfField(fieldname, false);
                if (null == svfField) {
                    continue;
                }
                ketas.add(svfField._fieldLength);
            }
            return ketas;
        }

        /**
         * 名前を印字する
         * @param svf
         * @param name 名前
         * @param fieldData フィールドのデータ
         */
        protected void printName(
                final Vrw32alp svf,
                final String name,
                final SvfFieldData fieldData) {
            final double charSize = KNJSvfFieldModify.getCharSize(name, fieldData);
            svf.VrAttribute(fieldData._fieldName, "Size=" + charSize);
            svf.VrAttribute(fieldData._fieldName, "Y=" + (int) KNJSvfFieldModify.getYjiku(0, charSize, fieldData));
            VrsOut(fieldData._fieldName, name);
        }

        protected static String formatDate(final DB2UDB db2, final String date, final String fmt, final String space) {
            final String[] tateFormat = KNJ_EditDate.tate2_format(KNJ_EditDate.h_format_JP(db2, date));
            final String nendo = (NumberUtils.isDigits(tateFormat[1]) && Integer.parseInt(tateFormat[1]) < 10 ? " " : "") + String.valueOf(tateFormat[1]);
            final String month = (Integer.parseInt(tateFormat[3]) < 10 ? " " : "") + String.valueOf(tateFormat[3]);
            final String day = (Integer.parseInt(tateFormat[5]) < 10 ? " " : "") + String.valueOf(tateFormat[5]);
            final StringBuffer stb = new StringBuffer();
            if (-1 != fmt.indexOf("G")) {
                stb.append(tateFormat[0] + "　" + space + "年");
            } else if (-1 != fmt.indexOf("Y")) {
                stb.append(tateFormat[0] + nendo + space + "年");
            }
            if (-1 != fmt.indexOf("M")) {
                stb.append(month + space + "月");
            }
            if (-1 != fmt.indexOf("D")) {
                stb.append(day + space + "日");
            }
            return stb.toString();
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public abstract boolean printSvf(final DB2UDB db2, final Student student);

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
    }

    /**
     * 学校教育システム 賢者 [学籍管理] 幼児指導要録 様式1（学生に関する記録）
     */
    private static class KNJA133K_1 extends KNJA133K_0 {

        KNJA133K_1(final Param param, final Vrw32alp svf) {
            super(param, svf);
        }

        /**
         * SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Student student) {
            boolean nonedata = false;
            final String form0;
            form0 = "KNJA133K_1.frm";
            final List<PersonalInfo> personalInfoList = student.getPrintSchregEntGrdHistList(param());
            if (param()._isNaraken) {
                for (final Indexed<PersonalInfo> i : Indexed.indexed(personalInfoList)) {
                    final PersonalInfo personalInfo = i._val;
                    final Map<Integer, List<Gakuseki>> pageGakusekiMap = new HashMap<Integer, List<Gakuseki>>();
                    for (final Indexed<List<Gakuseki>> spli : Indexed.indexed(1, i._val.splitByAge(TOKUSHI_SPLIT_PAGE_AGE + 1))) { // 3,4,5歳とそれ以上で頁を分ける
                        pageGakusekiMap.put(spli._idx, spli._val);
                    }

                    for (final Map.Entry<Integer, List<Gakuseki>> e : pageGakusekiMap.entrySet()) {
                        final List<Gakuseki> gakusekiList = e.getValue();
                        if (gakusekiList.isEmpty()) {
                            continue;
                        }
                        setForm(form0, 1);
                        final String form = modifyForm1(form0, student, personalInfo);
                        if (!form.equals(form0)) {
                            setForm(form, 1);
                        }

                        final Gakuseki minGakuseki = Gakuseki.minGakuseki(gakusekiList);
                        int posGeta = 0;
                        log.info(" minGakuseki = " + minGakuseki + ", posGeta = " + posGeta + ", gakusekiList = " + gakusekiList);
                        printMain1(db2, student, personalInfo, gakusekiList, posGeta);
                        _svf.VrEndPage();
                        nonedata = true;
                    }
                }
            } else {
                for (final PersonalInfo personalInfo : personalInfoList) {
                    setForm(form0, 1);
                    final String form = modifyForm1(form0, student, personalInfo);
                    if (!form.equals(form0)) {
                        setForm(form, 1);
                    }
                    printMain1(db2, student, personalInfo, personalInfo._gakusekiList, 0);
                    _svf.VrEndPage();
                    nonedata = true;
                }
            }
            return nonedata;
        }

        private void printMain1(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final int posGeta) {
            pritSvfDefault(db2);
            printSchoolInfo(); // 学校情報
            printPersonalInfo(db2, personalInfo); // 個人情報
            printAddress(personalInfo); // 住所履歴情報 保護者住所履歴情報
            printTransfer(db2, student, personalInfo); // 異動履歴情報
            printRegd(db2, student, personalInfo, gakusekiList, posGeta); // 年次・ホームルーム・整理番号
            printAftetGraduatedCourse(student);
        }


        private String FLG_TOKUSHI = "FLG_TOKUSHI";
        private String FLG_NYUEN_TO_NYUGAKU = "FLG_NYUEN_TO_NYUGAKU";
        private String modifyForm1(final String form, final Student student, final PersonalInfo pInfo) {
            final Map<String, String> flgMap = new TreeMap<String, String>();
            if ("1".equals(param()._useSpecial_Support_School)) {
                flgMap.put(FLG_TOKUSHI, "1");
                flgMap.put(FLG_NYUEN_TO_NYUGAKU, "1");
            }
            return modifyForm0(form, student, pInfo, flgMap);
        }

        @Override
        protected boolean modifySvfForm(final PersonalInfo pInfo, final SvfForm svfForm, final Map<String, String> flgMap) {
            if (flgMap.containsKey(FLG_NYUEN_TO_NYUGAKU)) {
                for (final String[]  fromTo : new String[][] {{"幼稚園名", "学校名"}}) {
                    final String from = fromTo[0];
                    final String to = fromTo[1];
                    for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(from)) {
                        final int x = koteiMoji._point._x;
                        final int endX = koteiMoji._endX;
                        final int centerX = x + (endX - x) / 2;
                        final int newX = x + (centerX - x) / 4;
                        final int newEndX = endX - (endX - centerX) / 4;
                        svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(to).setPoint(koteiMoji._point.setX(newX)).setEndX(newEndX));
                    }
                }
                for (final String[]  fromTo : new String[][] {{"入園", "入学"}, {"転入園", "転入学"}, {"転・退園", "転・退学"}, {"入園前の", "入学前の"}, {"年度及び入園(転入園)", "年度及び入学(転入学)"}, {"園長", "校長"}}) {
                    final String from = fromTo[0];
                    final String to = fromTo[1];
                    for (final KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(from)) {
                        svfForm.move(koteiMoji, koteiMoji.replaceMojiWith(to));
                    }
                }
            }
            if (flgMap.containsKey(FLG_TOKUSHI)) {

                for (final Tuple<String, Double> fieldnameAndPitch : Arrays.asList(Tuple.of("NENDO1", 23.7), Tuple.of("NENDO2", 47.3))) {
                    final String fieldname = fieldnameAndPitch._first;
                    final double pitch = fieldnameAndPitch._second;
                    final SvfForm.Field NENDO = svfForm.getField(fieldname);
                    final SvfForm.Repeat rep = svfForm.getRepeat(NENDO._repeatConfig._repeatNo);

                    // 罫線
                    final Line areaLeftLine = svfForm.getNearestLeftLine(NENDO._position);
                    final int areaLeftX = areaLeftLine.getPoint()._x;
                    final int areaRightX = svfForm.getNearestRightLine(NENDO.addX(rep._pitch * 3)._position).getPoint()._x;
                    final int width = areaRightX - areaLeftX;
                    Line line = null;
                    for (int i = 0; i < 4 - 1; i++) {
                        final SvfForm.Field f1 = NENDO.addX(rep._pitch * i);
                        final Line rightLine = svfForm.getNearestRightLine(f1._position);
                        if (null == line) {
                            line = rightLine;
                        }
                        svfForm.removeLine(rightLine);
                    }
                    if (null != line) {
                        for (int i = 1; i < 3; i++) {
                            final SvfForm.Line newLine = line.setX(areaLeftLine.getPoint()._x + i * width / 3);
                            svfForm.addLine(newLine);
                        }
                    }
                    final int addX = (width / 3 - width / 4) / 2;

                    // フィールド
                    for (SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                        if (field._repeatConfig._repeatNo.equals(rep._no)) {
                            svfForm.removeField(field);
                            field = field.setRepeatConfig(field._repeatConfig.setRepeatCount(3 - 1).setRepeatPitchPoint(pitch));
                            field = field.addX(addX);

                            if (field._fieldname.matches("TEACHER2_[1-2]_[1-3]")) {
                                svfForm.addField(field.addY(-40));

                                if (field._fieldname.matches("TEACHER2_2_[1-3]")) {
                                    final String n = field._fieldname.split("_")[2];

                                    svfForm.addField(field.copyTo("TEACHER2_3_" + n).addY(40));
                                }
                            } else {
                                svfForm.addField(field);
                            }
                        }
                    }
                }
            }
            return true;
        }

        private void pritSvfDefault(final DB2UDB db2) {

            if ("1".equals(param()._useSpecial_Support_School)) {
                VrsOut("FORM1", "（様式1）");
                VrsOut("TITLE", "特別支援学校幼稚部幼児指導要録（学籍に関する記録）");
            }

            VrsOut("DATE1", KNJ_EditDate.setDateFormat(db2, null, param()._year));
            VrsOut("DATE2", KNJ_EditDate.setDateFormat(db2, null, param()._year));
            VrsOut("DATE3", KNJ_EditDate.setDateFormat(db2, null, param()._year));
            VrsOut("DATE4", KNJ_EditDate.setDateFormat(db2, null, param()._year));
            VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat(db2, null, param()._year) + "生");

//            VrsOut("ENTERDIV1", "第　学年　入学");
//            VrsOut("ENTERDIV2", "第　学年編入学");
//            VrsOut("ENTERDIV3", "第　学年転入学");
//            VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(null) + "生");
//            VrsOut("ENTERDATE1", KNJ_EditDate.setDateFormat(null, param()._year));
////            VrsOut("ENTERDATE2", KNJ_EditDate.setDateFormat(null, param._year));
//            VrsOut("ENTERDATE3", KNJ_EditDate.setDateFormat(null, param()._year));
//            VrsOut("TRANSFER_DATE_1", "（" + KNJ_EditDate.setDateFormat(null, param()._year) + "）");
//            VrsOut("TRANSFER_DATE_2", "　" + KNJ_EditDate.setDateFormat(null, param()._year) + "　");
//            VrsOut("TRANSFER_DATE_4", KNJ_EditDate.setDateFormat(null, param()._year));
//            VrsOut("GRADDATE", KNJ_EditDate.setDateFormat(null, param()._year));
//            for (int i = 0; i < 6; i++) {
//                VrsOut("YEAR_" + (i + 1), KNJ_EditDate.setNendoFormat(null, param()._year));
//            }
        }

        /**
         * 学校情報
         */
        private void printSchoolInfo() {
            final String schoolName1 = !StringUtils.isEmpty(param()._certifSchoolName) ? param()._certifSchoolName : param()._schoolName1;
//            VrsOut("NAME_gakko1", schoolName1);
//                if (null != param()._schoolzip) {
//                    VrsOut("ZIPCODE", param()._schoolZipCd);
//                }

//            if (0 < getMS932ByteLength(param()._schoolAddr1)) {
//                VrsOut("ADDRESS_gakko1", param()._schoolAddr1);
//            }
//            if (0 < getMS932ByteLength(param()._schoolAddr2)) {
//                VrsOut("ADDRESS_gakko2", param()._schoolAddr2);
//            }
            if (param()._isMusashinohigashi) {
                VrsOut("SCHOOL_TEXT1", defstr(param()._certifSchoolRemark1) + defstr(param()._certifSchoolRemark2) + "　" + defstr(schoolName1));
            } else {
                VrsOut("CORP_NAME1", param()._certifSchoolRemark1);
                VrsOut("CORP_NAME2", param()._certifSchoolRemark2);
                VrsOut("SCHOOL_NAME1_1", schoolName1);
                VrsOut("SCHOOL_NAME2", param()._certifSchoolRemark4);
                VrsOut("SCHOOL_ADDR1_1", param()._certifSchoolRemark3);
                VrsOut("SCHOOL_ADDR2_1", param()._certifSchoolRemark5);
            }
        }

        /**
         * 個人情報
         */
        private void printPersonalInfo(final DB2UDB db2, final PersonalInfo pInfo) {
            if (pInfo._studentKana != null || pInfo._guardKana != null) {
//                int posx1 = 749;
//                int posx2 = posx1 + 800;
//                int gposx2 = posx2;
//                int height = 40;
//                final int minnum = 20;
//                final int maxnum = 100;
//                int ystart = 929;
//                int gystart = 1893;
//                if (personalInfo._studentKana != null) {
//                    final SvfFieldData fieldData = new SvfFieldData("KANA", posx1, posx2, height, minnum, maxnum, ystart);
//                    printName(svf, personalInfo._studentKana, fieldData);
//                }
//                if (personalInfo._guardKana != null) {
//                    final SvfFieldData fieldData = new SvfFieldData("GUARD_KANA", posx1, gposx2, height, minnum, maxnum, gystart);
//                    printName(svf, personalInfo._guardKana, fieldData);
//                }
                VrsOutForData(Arrays.asList("KANA1_1", "KANA1_2"), pInfo._studentKana);
                VrsOutForData(Arrays.asList("GRD_KANA1_1", "GRD_KANA1_2"), pInfo._guardKana);
            }
            if (null != param()._simei) {
//                int posx1 = 749;
//                int posx2 = 749 + 800;
//                int gposx2 = posx2;
//                final int height = 60;
//                final int minnum = 20;
//                final int maxnum = 48;
//                int ystart = 1080, ystart1 = 1040, ystart2 = 1120;
//                int gystart = 2044, gystart1 = 2004, gystart2 = 2084;
//
//                final String name1 = personalInfo._studentName1;
//                final String name2 = personalInfo._studentNameHistFirst;
//
//                if (StringUtils.isBlank(name2) || name2.equals(name1)) {
//                    // 履歴なしもしくは履歴の名前が現データの名称と同一
//                    final SvfFieldData fieldData1 = new SvfFieldData("NAME1", posx1, posx2, height, minnum, maxnum, ystart);
//                    printName(svf, name1, fieldData1);
//                } else {
//                    final SvfFieldData fieldData11 = new SvfFieldData("NAME1_1", posx1, posx2, height, minnum, maxnum, ystart1);
//                    final SvfFieldData fieldData21 = new SvfFieldData("NAME2_1", posx1, posx2, height, minnum, maxnum, ystart2);
//                    printName(svf, name2, fieldData11);
//                    printCancelLine(svf, fieldData11._fieldName, Math.min(getMS932ByteLength(name2), maxnum)); // 打ち消し線
//                    printName(svf, name1, fieldData21);
//                }
//
//                final String gname1 = personalInfo._guardName;
//                final String gname2 = personalInfo._guardNameHistFirst;
//
//                if (StringUtils.isBlank(gname2) || gname2.equals(gname1)) {
//                    // 履歴なしもしくは履歴の名前が現データの名称と同一
//                    final SvfFieldData fieldDataG1 = new SvfFieldData("GUARD_NAME1", posx1, gposx2, height, minnum, maxnum, gystart);
//                    printName(svf, gname1, fieldDataG1);
//                } else {
//                    final SvfFieldData fieldDataG11 = new SvfFieldData("GUARD_NAME1_1", posx1, gposx2, height, minnum, maxnum, gystart1);
//                    final SvfFieldData fieldDataG21 = new SvfFieldData("GUARD_NAME2_1", posx1, gposx2, height, minnum, maxnum, gystart2);
//                    printName(svf, gname2, fieldDataG11);
//                    printCancelLine(svf, fieldDataG11._fieldName, Math.min(getMS932ByteLength(gname2), maxnum)); // 打ち消し線
//                    printName(svf, gname1, fieldDataG21);
//                }
                VrsOutForData(Arrays.asList("NAME1_1", "NAME1_2"), pInfo._studentName);
                VrsOutForData(Arrays.asList("GRD_NAME1_1", "GRD_NAME1_2"), pInfo._guardName);
            }
            final String birthdayStr;
            if ("2".equals(param()._birthdayFormat)) { // パラメータがセットされ2:西暦の場合、西暦表示
                birthdayStr = KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_SeirekiJP(pInfo._birthday));
            } else {
                birthdayStr = KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP(db2, pInfo._birthday));
            }
            VrsOut("BIRTHDAY", birthdayStr + "生");
            VrsOut("SEX", pInfo._sex);

            // 入園前の状況
            final List<String> nyugakumaeCand = new ArrayList<String>();
            if (param()._isNaraken) {
                nyugakumaeCand.add(pInfo._entSchool);
                nyugakumaeCand.add(pInfo._entAddr);
                nyugakumaeCand.add(pInfo._entReason);
            }
            nyugakumaeCand.add(pInfo._nyugakumaeSyussinJouhou);
            VrsOutRenban("BEFORE_STATUS", getTokenList(Util.mkString(Util.filterNotBlank(nyugakumaeCand), "\n"), 50));
            // 進学先等
            final List<String> shingakusakiCand = new ArrayList<String>();
            if (param()._isNaraken) {
                if (pInfo.isTengaku()) {
                    // 転学
                    shingakusakiCand.add(pInfo._grdSchool);
                    shingakusakiCand.add(pInfo._grdAddr);
                    shingakusakiCand.add(pInfo._grdReason);
                } else if (pInfo.isTaigaku()) {
                    // 退学
                    shingakusakiCand.add(pInfo._grdReason);
                }
            }
            shingakusakiCand.add(pInfo._baseDetailMst002Remark1);
            VrsOutRenban("NEXT_PLACE", getTokenList(Util.mkString(Util.filterNotBlank(shingakusakiCand), "\n"), 50));
        }

        /**
         * 個人情報 住所履歴情報 履歴を降順に読み込み、最大件数まで出力
         */
        private void printAddress(final PersonalInfo personalInfo) {
            final int max = 1;
            final int num = personalInfo._addressList.size();
            int i = Math.min(max, num); // 出力件数

            for (final Iterator<Address> it = personalInfo._addressList.iterator(); it.hasNext() && i > 0; i--) {
                final Address address = it.next();

//                if (param()._schzip != null) {
//                    VrsOut("ZIPCODE" + i, address._zipCd);
//                    //printAddressLine(svf, "ZIPCODELINE" + i, address._zipCd, i, num, max);
//                }
                final int n1 = KNJ_EditEdit.getMS932ByteLength(address._addr1);
                final int n2 = KNJ_EditEdit.getMS932ByteLength(address._addr2);
                if (0 < n1) {
                    VrsOut("ADDR" + i + "_1", address._addr1);
                    //printAddressLine(svf, "ADDRESSLINE" + i + "_1", address._addr1, i, num, max);
                }
                if (0 < n2 && address._isPrintAddr2) {
                    VrsOut("ADDR" + i + "_2", address._addr2);
                    //printAddressLine(svf, "ADDRESSLINE" + i +"_2", address._addr2, i, num, max);
                }
            }

            // 保護者住所履歴を印刷
            final String SAME_TEXT;
            if ("1".equals(param()._useSpecial_Support_School)) {
                SAME_TEXT = "幼児の欄に同じ";
            } else {
                SAME_TEXT = "園児の欄に同じ";
            }
            final int max2 = 1;
            if (isSameAddressList(Util.take(personalInfo._addressList, max2), Util.take(personalInfo._guardianAddressList, max2))) {
                // 住所が生徒と同一
                VrsOut("GRD_ADDR1_1", SAME_TEXT);
            } else {
                final int num2 = personalInfo._guardianAddressList.size();
                int i2 = Math.min(max2, num2); // 出力件数

                final Address studentAddressMax = personalInfo.getStudentAddressMax();
                final String addr1 = null ==  studentAddressMax ? "" : defstr(studentAddressMax._addr1);
                final String addr2 = null ==  studentAddressMax ? "" : defstr(studentAddressMax._addr2);

                for (final Iterator<Address> it = personalInfo._guardianAddressList.iterator(); it.hasNext() && i2 > 0; i2--) {
                    final Address guardianAddressRec = it.next();

                    final String address1 = defstr(guardianAddressRec._addr1);
                    final String address2 = defstr(guardianAddressRec._addr2);

                    if ((i2 == max2 || i2 == personalInfo._guardianAddressList.size()) && addr1.equals(address1) && addr2.equals(address2)) {
                        // 最新の住所かつ内容が生徒と同一
                        VrsOut("GRD_ADDR" + i2 + "_1", SAME_TEXT);
                    } else {
//                    if (param()._schzip != null) {
//                        VrsOut("GUARDZIP" + i, guardianAddressRec._zipCd);
//                        //printAddressLine(svf, "GUARDZIPLINE" + i, guardianAddressRec._zipCd, i, num, max);
//                    }
                        final int n1 = KNJ_EditEdit.getMS932ByteLength(address1);
                        final int n2 = KNJ_EditEdit.getMS932ByteLength(address2);
                        if (0 < n1) {
                            VrsOut("GRD_ADDR" + i2 + "_1", address1);
                            //printAddressLine(svf, "GUARDIANADDLINE" + i + "_1", address1, i, num, max);
                        }
                        if (0 < n2 && guardianAddressRec._isPrintAddr2) {
                            VrsOut("GRD_ADDR" + i2 + "_2", address2);
                            //printAddressLine(svf, "GUARDIANADDLINE" + i + "_2", address2, i, num, max);
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
                if (null == addressAi._addr1 && null == addressBi._addr1) {
                } else if (null == addressAi._addr1 || null == addressBi._addr1 || !addressAi._addr1.equals(addressBi._addr1)) {
                    rtn = false;
                }
                if (null == addressAi._addr2 && null == addressBi._addr2) {
                } else if (!addressAi._isPrintAddr2 && !addressBi._isPrintAddr2) {
                } else if (null == addressAi._addr2 || null == addressBi._addr2 || !addressAi._addr2.equals(addressBi._addr2)) {
                    rtn = false;
                }
            }
            return rtn;
        }

        /**
         * 取り消し線印刷
         * @param svf
         * @param keta 桁
         */
        private void printCancelLine(final String field, final int keta) {
            _svf.VrAttribute(field, "UnderLine=(0,3,5), Keta=" + keta);
        }

        /**
         * 個人情報 異動履歴情報
         */
        private void printTransfer(final DB2UDB db2, final Student  student, final PersonalInfo personalInfo) {
            printCancelLine("LINE1", 14);
            printCancelLine("LINE2", 14);

            final int A002_NAMECD2_TENNYUGAKU = 4;
//            final int A002_NAMECD2_5 = 5;
            final int A003_NAMECD2_SOTSUGYO = 1;
            final int A003_NAMECD2_TAIGAKU = 2;
            final int A003_NAMECD2_TENGAKU = 3;

            final String[] tateFormat = KNJ_EditDate.tate2_format(KNJ_EditDate.h_format_JP(db2, Util.nendoDefaultBaseDate(param()._year)));
            VrsOut("DATE1", tateFormat[0] + "　年　月　日");
            VrsOut("DATE2", tateFormat[0] + "　年　月　日");
            VrsOut("DATE3", tateFormat[0] + "　年　月　日");
            VrsOut("DATE4", tateFormat[0] + "　年　月　日");

            // 入学区分
            if (null != personalInfo._entDiv) {
//                final String gradeStr;
//                if (personalInfo._entYear != null) {
//                    final int diff = Integer.parseInt(param()._year) - Integer.parseInt(personalInfo._entYear);
//                    final int currentGradeCd = student.currentGradeCd(param());
//                    final int grade = currentGradeCd - diff;
//                    gradeStr = " " + (0 >= grade ? " " : String.valueOf(grade));
//                } else {
//                    gradeStr = "  ";
//                }
                final int entdiv = personalInfo._entDiv.intValue();
                if (entdiv == A002_NAMECD2_TENNYUGAKU) { // 転入学
                    if (personalInfo._entDate != null) {
                        VrsOut("DATE2", formatDate(db2, personalInfo._entDate, "YMD", ""));
                    }
//                    if (personalInfo._entDate != null) {
//                        VrsOut("ENTERDATE3", KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._entDate), param()._year));
//                    }
//                    VrsOut("ENTERDIV3", "第" + gradeStr + "学年転入学");
//                    VrAttribute("LINE3", "X=5000");
//                    if (personalInfo._entReason != null) {
//                        VrsOut("TRANSFERREASON1_1", personalInfo._entReason);
//                    }
//                    if (personalInfo._entSchool != null) {
//                        VrsOut("TRANSFERREASON1_2", personalInfo._entSchool);
//                    }
//                    if (personalInfo._entAddr != null) {
//                        VrsOut("TRANSFERREASON1_3", personalInfo._entAddr);
//                    }
                } else {
                    if (personalInfo._entDate != null) {
                        VrsOut("DATE1", formatDate(db2, personalInfo._entDate, "YMD", ""));
                    }
//                    if (entdiv != A002_NAMECD2_5) {
//                        if (personalInfo._entDate != null) {
//                            VrsOut("ENTERDATE1", KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._entDate), param()._year));
//                        }
//                        VrAttribute("LINE1", "X=5000");
//                        VrsOut("ENTERDIV1", "第" + gradeStr + "学年　入学");
//                    } else {
//                        if (personalInfo._entDate != null) {
//                            VrsOut("ENTERDATE2", KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._entDate), param()._year));
//                        }
//                        VrAttribute("LINE2", "X=5000");
//                        VrsOut("ENTERDIV2", "第" + gradeStr + "学年編入学");
//                    }
//                    if (personalInfo._entReason != null) {
//                        VrsOut("ENTERRESONS1", personalInfo._entReason);
//                    }
//                    if (personalInfo._entSchool != null) {
//                        VrsOut("ENTERRESONS2", personalInfo._entSchool);
//                    }
//                    if (personalInfo._entAddr != null) {
//                        VrsOut("ENTERRESONS3", personalInfo._entAddr);
//                    }
                }
            }

            // 除籍区分
            if (null != personalInfo._grdDiv) {
                final int grddiv = personalInfo._grdDiv.intValue();
                if (grddiv == A003_NAMECD2_TAIGAKU || grddiv == A003_NAMECD2_TENGAKU) {
                    // 2:退学 3:転学
                    if (personalInfo._grdDate != null) {
                        VrsOut("DATE3", formatDate(db2, personalInfo._grdDate, "YMD", ""));
                    }
//                    if (personalInfo._grdDate != null) {
//                        VrsOut("TRANSFER_DATE_1", "（" + KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._grdDate), param()._year) + "）");
//                    }
//                    if (personalInfo._grdReason != null) {
//                        VrsOut("TRANSFERREASON2_1", personalInfo._grdReason);
//                    }
//                    if (personalInfo._grdSchool != null) {
//                        VrsOut("TRANSFERREASON2_2", personalInfo._grdSchool);
//                    }
//                    if (personalInfo._grdAddr != null) {
//                        VrsOut("TRANSFERREASON2_3", personalInfo._grdAddr);
//                    }
                } else if (grddiv == A003_NAMECD2_SOTSUGYO) {
                    // 1:卒業
                    if (personalInfo._grdDate != null) {
                        VrsOut("DATE4", formatDate(db2, personalInfo._grdDate, "YMD", ""));
                    }
//                    if (personalInfo._grdDate != null) {
//                        VrsOut("TRANSFER_DATE_4", KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._grdDate), param()._year));
//                    }
                }
            }

//            if (null == personalInfo._tengakuSakiZenjitu && null != personalInfo._grdDiv && personalInfo._grdDiv.intValue() == A003_NAMECD2_TENGAKU && personalInfo._grdDate != null) {
//                final String dateFormatJp = KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._grdDate), param()._year);
//                VrsOut("TRANSFER_DATE_2", "　" + dateFormatJp + "　");
//            } else if (null != personalInfo._tengakuSakiZenjitu) {
//                final String dateFormatJp = KNJ_EditDate.setDateFormat(KNJ_EditDate.h_format_JP(personalInfo._tengakuSakiZenjitu), param()._year);
//                VrsOut("TRANSFER_DATE_2", "　" + dateFormatJp + "　");
//            }
        }

        private void printRegd(final DB2UDB db2, final Student student, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final int posGeta) {

            final List<Gakuseki> printGakusekiList = new ArrayList<Gakuseki>();
            int ctrlPos = 0;
            for (final Gakuseki gakuseki : gakusekiList) {
                if (null != gakuseki._year && !(personalInfo.getYearBegin() <= Integer.parseInt(gakuseki._year) && Integer.parseInt(gakuseki._year) <= personalInfo.getYearEnd(param()))) {
                    param().logOnce(" skip print year = " + gakuseki._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd(param()));
                    continue;
                }
                printGakusekiList.add(gakuseki);
                if (param()._year.equals(gakuseki._year)) {
                    ctrlPos = gakuseki.getPos(param()) + posGeta;
                }
            }

            for (int i = 1; i <= 4; i++) {
                VrsOutn("NENDO1", i, formatDate(db2, Util.nendoDefaultBaseDate(String.valueOf(Integer.parseInt(param()._year) + (i - ctrlPos))), "G", "") + "度");
                VrsOutn("NENDO2", i, formatDate(db2, Util.nendoDefaultBaseDate(String.valueOf(Integer.parseInt(param()._year) + (i - ctrlPos))), "G", " ") + "度");
                VrsOutn("AGE", i, " 歳　か月");
            }

            // log.info(" _gakusekiList = " + personalInfo._gakusekiList);
            for (final Gakuseki gakuseki : printGakusekiList) {
                final int pos = gakuseki.getPos(param());
                VrsOutnForData(Arrays.asList("HR_NAME1_1", "HR_NAME1_2"), pos, gakuseki._hrname); // 組
                VrsOutn("NO1", pos, String.valueOf(Integer.parseInt(gakuseki._attendno))); // 出席番号

                for (int j = 1; j <= 2; j++) {
                    for (int k = 1; k <= 2; k++) {
                        for (int l = 1; l <= 3; l++) {
                            VrsOutn("TEACHER" + j + "_" + k + "_" + l, pos, "");
                        }
                    }
                }

                VrsOutn("NENDO1", pos, formatDate(db2, Util.nendoDefaultBaseDate(String.valueOf(Integer.parseInt(gakuseki._year))), "Y", "") + "度");
                VrsOutn("NENDO2", pos, formatDate(db2, Util.nendoDefaultBaseDate(String.valueOf(Integer.parseInt(gakuseki._year))), "Y", "") + "度");
                if (null != gakuseki._year && null != personalInfo._birthday) {
                    VrsOutn("AGE", pos, personalInfo.getAgeMonthString(gakuseki._year));
                }

                if (!(Integer.parseInt(gakuseki._year) <= student.getPersonalInfoYearEnd(personalInfo, param()))) {
                    continue;
                }

                // 校長氏名
                printStaffName("1", pos, true, gakuseki._principal, gakuseki._principal1, gakuseki._principal2);

                final List<Staff> studentStaff1HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV1, gakuseki._year);
                if (param()._isNaraken) {
                    final List<Staff> studentStaff2HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV2, gakuseki._year);
                    final List<Staff> studentStaff3HistList = param()._staffInfo.getStudentStaffHistList(param(), student, personalInfo, StaffInfo.TR_DIV3, gakuseki._year);
                    final Staff staff1 = Util.last(studentStaff1HistList, Staff.Null);
                    final Staff staff2 = Util.last(studentStaff2HistList, Staff.Null);
                    final Staff staff3 = Util.last(studentStaff3HistList, Staff.Null);
                    final int keta = Math.max(Math.max(KNJ_EditEdit.getMS932ByteLength(staff1.getNameString()), KNJ_EditEdit.getMS932ByteLength(staff2.getNameString())), KNJ_EditEdit.getMS932ByteLength(staff3.getNameString()));
                    final String k = (keta > 20 ? "3" : keta > 14 ? "2" : "1");
                    final String priOrStaff = "2";
                    log.info(" staffs " + ArrayUtils.toString(new String[] {staff1.getNameString(), staff2.getNameString(), staff3.getNameString()}));
                    VrsOutn("TEACHER" + priOrStaff + "_1_" + k, pos, staff1.getNameString());
                    VrsOutn("TEACHER" + priOrStaff + "_2_" + k, pos, staff2.getNameString());
                    VrsOutn("TEACHER" + priOrStaff + "_3_" + k, pos, staff3.getNameString());

                } else {
                    if (!studentStaff1HistList.isEmpty()) {
                        final Staff staff1 = studentStaff1HistList.get(0);
                        final Staff staff2 = studentStaff1HistList.get(studentStaff1HistList.size() - 1);
                        // 担任氏名
                        printStaffName("2", pos, false, Staff.Null, staff1, staff2);
                    }
                }

//                log.debug("印影："+param()._inei);
//                log.debug("改竄："+gakuseki._kaizanFlg); //改竄されていないか？
//                log.debug("署名（校長）："+gakuseki._principalSeq); //署名（校長）しているか？
//                log.debug("署名（担任）："+gakuseki._staffSeq); //署名（担任）しているか？
//                //印影
//                if (null != param()._inei) {
//                    if (null == gakuseki._kaizanFlg && null != gakuseki._principalSeq || "2".equals(param()._inei)) {
//                        final String stampNo = null == gakuseki._principal1._staffMst._staffcd ? gakuseki._principal._stampNo : gakuseki._principal1._stampNo;
//                        final String path1 = param().getImageFilePath(stampNo);
//                        if (path1 != null) {
//                            VrsOut("STAFFBTM_1_" + si + ("1".equals(param()._colorPrint) ? "C" : ""), path1); // 校長印
//                        }
//                    }
//                    if (null == gakuseki._kaizanFlg && null != gakuseki._staffSeq || "2".equals(param()._inei)) {
//                        final String path2 = param().getImageFilePath(gakuseki._staff1._stampNo);
//                        if (path2 != null) {
//                            VrsOut("STAFFBTM_2_" + si + ("1".equals(param()._colorPrint) ? "C" : ""), path2); // 担任印
//                        }
//                    }
//                }
            }
        }

        private void printStaffName(final String priOrStaff, final int pos, final boolean isCheckStaff0, final Staff staff0, final Staff staff1, final Staff staff2) {
            if (isCheckStaff0 && null == staff1._staffMst._staffcd) {
                // 1人表示（校長の場合のみ）。戸籍名表示無し。
                VrsOutnForData(Arrays.asList("TEACHER" + priOrStaff + "_1_1", "TEACHER" + priOrStaff + "_1_2", "TEACHER" + priOrStaff + "_1_3"), pos, staff0.getNameString());
            } else if (StaffMst.Null == staff2._staffMst || staff2._staffMst == staff1._staffMst) {
                // 1人表示。戸籍名表示ありの場合最大2行（中央）。
                VrsOutnForData(Arrays.asList("TEACHER" + priOrStaff + "_1_1", "TEACHER" + priOrStaff + "_1_2", "TEACHER" + priOrStaff + "_1_3"), pos, Util.mkString(staff1._staffMst.getNameLine(staff1._year), ""));
            } else {
                // 2人表示。最小2行（中央）。期間表示ありの場合を含めて最大4行。
                final int lineKeta = 26;
                final String str2 = Util.mkString(staff2.getNameBetweenLine(lineKeta), "");
                final String str1 = Util.mkString(staff1.getNameBetweenLine(lineKeta), "");
                final int keta = Math.max(KNJ_EditEdit.getMS932ByteLength(str1), KNJ_EditEdit.getMS932ByteLength(str2));
                final String k = (keta > 20 ? "3" : keta > 14 ? "2" : "1");
                VrsOutn("TEACHER" + priOrStaff + "_1_" + k, pos, str2);
                VrsOutn("TEACHER" + priOrStaff + "_2_" + k, pos, str1);
            }
        }

        private void printAftetGraduatedCourse(final Student student) {
            for (int i = 0; i < student._afterGraduatedCourseTextList.size(); i++) {
                VrsOut("NEXT_PLACE" + String.valueOf(i + 1), student._afterGraduatedCourseTextList.get(i));
            }
        }
    }

    /**
     * 学校教育システム 賢者 [学籍管理]  幼児指導要録
     *   指導の記録
     */
    private static class KNJA133K_2 extends KNJA133K_0 {

        KNJA133K_2(final Param param, final Vrw32alp svf) {
            super(param, svf);
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public boolean printSvf(final DB2UDB db2, final Student student) {
            boolean nonedata = false;
            if ("1".equals(param()._useSpecial_Support_School)) {
                final String form1 = "KNJA133K_2_TOKUSHI.frm"; // 5歳以前用
                final String form2 = "KNJA133K_2_TOKUSHI2.frm"; // 5歳用
                final List<PersonalInfo> pInfoList = student.getPrintSchregEntGrdHistList(param());
                for (final Indexed<PersonalInfo> i : Indexed.indexed(pInfoList)) {

                    final Map<Integer, List<Gakuseki>> pageGakusekiMap = new HashMap<Integer, List<Gakuseki>>();
                    List<Indexed<List<Gakuseki>>> indexed;
                    if (param()._isNaraken) {
                        indexed = Indexed.indexed(1, i._val.splitByAge(TOKUSHI_SPLIT_PAGE_AGE)); // 3,4歳と5歳でフォームを分ける
                    } else {
                        indexed = Indexed.indexed(1, i._val.splitByGradeCd("03")); // 01, 02と03でフォームを分ける
                    }
                    for (final Indexed<List<Gakuseki>> spli : indexed) {
                        pageGakusekiMap.put(spli._idx, spli._val);
                    }
                    log.info(" page gakuseki map = " + pageGakusekiMap);

                    for (final Indexed<String> form : Indexed.indexed(1, Arrays.asList(form1, form2))) {
                        setForm(form._val, 1);
                        printName(db2, i._val);  //学籍データ
                        List<Gakuseki> printGakusekiList = pageGakusekiMap.get(form._idx);
                        if (null == printGakusekiList) {
                            printGakusekiList = new ArrayList<Gakuseki>();
                        }
                        final Gakuseki minGakuseki = Gakuseki.minGakuseki(printGakusekiList);
                        int posGeta = 0;
                        if (null != minGakuseki) {
                            posGeta = - (minGakuseki.getPos(param()) - 1);
                        }
                        log.info(" page = " + form._idx + ", minGakuseki = " + minGakuseki + ", posGeta = " + posGeta + ", gakusekiList = " + printGakusekiList);
                        printHeader(db2, i._val, i._val._gakusekiList, printGakusekiList, posGeta);
                        printShoken(db2, student, i._val, printGakusekiList, posGeta); // 所見
                        printTokushiRemark(form._idx);
                        _svf.VrEndPage();
                    }
                }
            } else {
                final String form = "KNJA133K_2.frm";
                final List<PersonalInfo> pInfoList = student.getPrintSchregEntGrdHistList(param());
                for (final Indexed<PersonalInfo> i : Indexed.indexed(1, pInfoList)) {
                    setForm(form, 1);
                    printName(db2, i._val);  //学籍データ
                    printHeader(db2, i._val, i._val._gakusekiList, i._val._gakusekiList, 0);
                    printShoken(db2, student, i._val, i._val._gakusekiList, 0); // 所見
                    _svf.VrEndPage();
                }
            }
            nonedata = true;
            return nonedata;
        }

        private void printHeader(final DB2UDB db2, final PersonalInfo personalInfo, final List<Gakuseki> gakusekiList, final List<Gakuseki> printGakusekiList, final int posGeta) {
            // ねらい（発達を捉える視点）
            for (int i = 0; i < param()._jviewPointList.size(); i++) {
                final JViewPoint jvp = param()._jviewPointList.get(i);
                final String linei = String.valueOf(i + 1);

                for (int j = 0; j < jvp._mList.size(); j++) {
                    final JViewPoint.M jvpm = jvp._mList.get(j);
                    final String linej = String.valueOf(j + 1);

                    final String fieldname = "TARGET" + linei + "_" + linej + "_1";
                    final SvfField field = getSvfField(fieldname);
                    if (null == field) {
                        if (param()._isOutputDebug) {
                            log.info(" field " + fieldname);
                        }
                    } else {
                        final List<String> token = getTokenList(jvpm._remarkM, field._fieldLength);
                        for (int k = 0; k < token.size(); k++) {
                            VrsOut("TARGET" + linei + "_" + linej + "_" + String.valueOf(k + 1), token.get(k));
                        }
                    }
                }
            }

            int ctrlPos = -1;
            for (final Gakuseki gakuseki : gakusekiList) {
                if (null != gakuseki._year && !(personalInfo.getYearBegin() <= Integer.parseInt(gakuseki._year) && Integer.parseInt(gakuseki._year) <= personalInfo.getYearEnd(param()))) {
                    log.info(" skip print year = " + gakuseki._year + ", begin = " + personalInfo.getYearBegin() + ", end = " + personalInfo.getYearEnd(param()));
                    continue;
                }
                if (param()._year.equals(gakuseki._year)) {
                    ctrlPos = gakuseki.getPos(param()) + posGeta;
                }
            }

            final TreeMap<String, Gakuseki> yearGakusekiMap = Student.gakusekiYearMap(printGakusekiList);
            for (int i = 1; i <= 4; i++) {
                final String year = String.valueOf(Integer.parseInt(param()._year) + i - ctrlPos);
                VrsOut("NENDO1_" + i, formatDate(db2, Util.nendoDefaultBaseDate(year), "G", "") + "度"); //年度
                VrsOutn("NENDO2_1", i, "　年度"); //年度

                final Gakuseki gakuseki = yearGakusekiMap.get(year);
                log.info(" i = " + i + ", ctrlPos = " + ctrlPos + ", year = " + year + ", " + gakuseki + " / " + yearGakusekiMap);
                if (null != gakuseki) {
                    final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
                    VrsOut("NENDO1_" + i, nendo); //年度
                    if ("1".equals(param()._useSpecial_Support_School)) {
                        VrsOutn("NENDO2_1", i, nendo); //年度
                    } else {
                        final String[] nendoArray = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, Util.nendoDefaultBaseDate(year)));
                        if (null != nendoArray && nendoArray.length >= 2) {
                            VrsOutn("NENDO2_1", i, defstr(nendoArray[1], "　") + "年度"); //年度
                        }
                    }
                }
            }
        }

        private void printShoken(final DB2UDB db2, final Student student, final PersonalInfo pi, final List<Gakuseki> gakusekiList, final int posGeta) {

            // 総合的な学習の時間の記録・総合所見
            if (param()._isOutputDebug) {
                log.info(" htrainRemarkKDat years = " + student._htrainRemarkKDatMap.keySet());
            }
            final TreeMap<String, Gakuseki> yearGakusekiMap = Student.gakusekiYearMap(gakusekiList);
            for (final HtrainremarkKDat remark : student._htrainRemarkKDatMap.values()) {
                if (null != remark._year && !(pi.getYearBegin() <= Integer.parseInt(remark._year) && Integer.parseInt(remark._year) <= student.getPersonalInfoYearEnd(pi, param()))) {
                    param().logOnce(" skip print year = " + remark._year + ", begin = " + pi.getYearBegin() + ", end = " + pi.getYearEnd(param()));
                    continue;
                }
                final Gakuseki gakuseki = yearGakusekiMap.get(remark._year);
                if (null == gakuseki) {
                    continue;
                }
                final int pos = gakuseki.getPos(param()) + posGeta;

                printShoken("LEAD_GRADE" + pos, remark._totalstudyact, param()._HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_K, 8, 5, "学年の重点");

                printShoken("LEAD_PERSON" + pos, remark._totalstudyval, param()._HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_K, 8, 5, "個人の重点");

                printShoken("LEAD_MATTER" + pos, remark._totalRemark, param()._HTRAINREMARK_DAT_TOTALREMARK_SIZE_K, 10, 50, "総合所見");

                printShoken("ATTEND_REMARK" + pos, remark._attendrecRemark, param()._HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_K, 8, 4, "出欠備考");

                if ("1".equals(param()._useSpecial_Support_School)) {
                    final String prop;
                    final int mojisu;
                    final int gyo;
                    final String data;
                    if ("2".equals(param()._chiteki)) {
                        prop = param()._HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_K;
                        mojisu = 21;
                        gyo = 6;
                        data = remark._detail2DatSeq001Remark1;
                    } else {
                        prop = param()._HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_K_disability;
                        mojisu = 21;
                        gyo = 6;
                        data = remark._detail2DatSeq002Remark1;
                    }
                    printShoken("INDEPENDENCE_REMARK" + pos, data, prop, mojisu, gyo, "自立活動の内容に重点を置いた指導");
                }
            }

            // 入学時の障害の状態
            if ("1".equals(param()._useSpecial_Support_School)) {
                final String prop;
                final int mojisu;
                final int gyo;
                if ("2".equals(param()._chiteki)) {
                    prop = param()._HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K;
                    mojisu = 9;
                    gyo = 15;
                } else {
                    prop = param()._HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K_disability;
                    mojisu = 9;
                    gyo = 15;
                }
                printShoken("FIELD1", student._htrainremarkHdat._detail2HdatSeq001Remark1, prop, mojisu, gyo, "入学時の障害の状態");
            }

            // 出欠の記録
            for (final Attend attend : student._attendList) {
                if (null != attend._year && !(pi.getYearBegin() <= Integer.parseInt(attend._year) && Integer.parseInt(attend._year) <= student.getPersonalInfoYearEnd(pi, param()))) {
                    param().logOnce(" skip print year = " + attend._year + ", begin = " + pi.getYearBegin() + ", end = " + pi.getYearEnd(param()));
                    continue;
                }
                final Gakuseki gakuseki = yearGakusekiMap.get(attend._year);
                if (null == gakuseki) {
                    continue;
                }
                final int pos = gakuseki.getPos(param()) + posGeta;
                if (param()._isOutputDebug) {
                    log.info(" year = " + attend._year + ", posGeta = " + posGeta + ", pos = " + pos);
                }
                VrsOutn("EDU_DAYS", pos, attend._lesson);           //授業日数
                VrsOutn("ATTEND_DAYS", pos, attend._present);   //要出席
            }
        }

        private void printTokushiRemark(final int formidx) {

            final List<String> tokushiRemark = new ArrayList<String>();
            tokushiRemark.add("学年の重点：年度当初に、教育課程に基づき長期の見通しとして設定したものを記入");
            tokushiRemark.add("個人の重点：１年間を振り返って、当該幼児の指導について特に重視してきた点を記入");
            tokushiRemark.add("自立活動の内容に重点を置いた指導：自立活動の内容に重点を置いた指導を行った場合に、１年間を振り返って、当該幼児の指導のねらい、指導内容等について特に重視してきた点を記入すること。");
            if (formidx == 1) {
                tokushiRemark.add("入学時の障害の状態等：入学又は転入学時の幼児の障害の状態等について記入すること。");
            }
            tokushiRemark.add("指導上参考となる事項：");
            tokushiRemark.add("(1) 次の事項について記入すること。");
            tokushiRemark.add("　　①１年間の指導の過程と幼児の発達の姿について以下の事項を踏まえ記入すること。");
            if (formidx == 1) {
                tokushiRemark.add("　　・特別支援学校幼稚部教育要領第２章「ねらい及び内容」に示された各領域のねらいを視点として当該幼児の発達の実情から向上が著しいと思われるもの。");
                tokushiRemark.add("　　　その際、他の幼児との比較や一定の基準に対する達成度についての評定によって捉えるものではないことに留意すること。");
                tokushiRemark.add("　　・幼稚部における生活を通して全体的、総合的に捉えた幼児の発達の姿。");
            } else if (formidx == 2) {
                tokushiRemark.add("　　　・特別支援学校幼稚部教育要領第２章「ねらい及び内容」に示された各領域のねらいを視点として、当該幼児の発達の実情から向上が著しいと思われるもの。");
                tokushiRemark.add("　　　　その際、他の幼児との比較や一定の基準に対する達成度についての評定によって捉えるものではないことに留意すること。");
                tokushiRemark.add("　　　・幼稚部における生活を通して全体的、総合的に捉えた幼児の発達の姿。");
            }
            tokushiRemark.add("　　②次の年度の指導に必要と考えられる配慮事項等について記入すること。");
            if (formidx == 2) {
                tokushiRemark.add("　　③最終年度の記入に当たっては、特に小学校等における児童の指導に生かされるよう、特別支援学校幼稚部教育要領第１章総則に示された「幼児期の終わりまでに育ってほしい姿」を活用して幼");
                tokushiRemark.add("　　児に育まれている資質・能力を捉え、指導の過程と育ちつつある姿を分かりやすく記入するように留意すること。その際、「幼児期の終わりまでに育ってほしい姿」が到達すべき目標ではないこ");
                tokushiRemark.add("　　とに留意し、項目別に幼児の育ちつつある姿を記入するのではなく、全体的、総合的に捉えて記入すること。");
            }
            tokushiRemark.add("(2) 幼児の健康の状況等指導上特に留意する必要がある場合等について記入すること。");
            tokushiRemark.add("備考：教育課程に係る教育時間の終了後等に行う教育活動を行っている場合には、必要に応じて当該教育活動を通した幼児の発達の姿を記入すること。");

            final SvfField REMARK_TEXT = getSvfField("REMARK_TEXT");
            final List<String> tokenList = getTokenList(Util.mkString(tokushiRemark, "\n"), REMARK_TEXT._fieldLength);
//            log.info(formidx + " = " + Util.debugCollectionToStr("naraken", tokenList, "\n"));
            VrsOutnRepeat(REMARK_TEXT._name, tokenList);

            if (formidx == 2) {
                final String sugata = "「幼児期の終わりまでに育ってほしい姿」は、幼稚部教育要領第２章に"
                        + "示すねらい及び内容に基づいて、各学校で、幼児期にふさわしい遊びや"
                        + "生活を積み重ねることにより、幼稚部における教育において育みたい資"
                        + "質・能力が育まれている幼児の具体的な姿であり、特に５歳児後半に見"
                        + "られるようになる姿である。「幼児期の終わりまでに育ってほしい姿」"
                        + "は、とりわけ幼児の自発的な活動としての遊びを通して、一人一人の発"
                        + "達の特性に応じて、これらの姿が育っていくものであり、全ての幼児に"
                        + "同じように見られるものではないことに留意すること。";

                final SvfField SUGATA = getSvfField("SUGATA");

                final List<String> tokenList2 = getTokenList(sugata, SUGATA._fieldLength);
//              log.info(formidx + " = " + Util.debugCollectionToStr("naraken", tokenList, "\n"));
                VrsOutnRepeat(SUGATA._name, tokenList2);

                final String[] sugataItemTitles = {
                        "健康な心と体",
                        "自立心",
                        "協同性",
                        "道徳性・規範意識の芽生え",
                        "社会生活との関わり",
                        "思考力の芽生え",
                        "自然との関わり・生命尊重",
                        "数量や図形、標識や文字などへの関心・感覚",
                        "言葉による伝え合い",
                        "豊かな感性と表現",
                };
                for (int i = 0; i < sugataItemTitles.length; i++) {
                    final List<String> tokenList3 = getTokenList(sugataItemTitles[i], 6 * 2);
                    for (int j = 0; j < tokenList3.size(); j++) {
                        VrsOutn("SUGATA_ITEM_TITLE" + String.valueOf(i + 1), j + 1, tokenList3.get(j));
                    }
                }

                final String[] sugataItems = {
                        "幼稚部における生活の中で、充実感をもって自分のやりたいことに向かって心と体を十分に働かせ、見通しをもって行動し、自ら健康で安全な生活をつくり出すようになる。",
                        "身近な環境に主体的に関わり様々な活動を楽しむ中で、しなければならないことを自覚し、自分の力で行うために考えたり、工夫したりしながら、諦めずにやり遂げることで達成感を味わい、自信をもって行動するようになる。",
                        "友達と関わる中で、互いの思いや考えなどを共有し、共通の目的の実現に向けて、考えたり、工夫したり、協力したりし、充実感をもってやり遂げるようになる。",
                        "友達と様々な体験を重ねる中で、してよいことや悪いことが分かり、自分の行動を振り返ったり、友達の気持ちに共感したりし、相手の立場に立って行動するようになる。また、きまりを守る必要性が分かり、自分の気持ちを調整し、友達と折り合いを付けながら、きまりをつくったり、守ったりするようになる。",
                        "家族を大切にしようとする気持ちをもつとともに、地域の身近な人と触れ合う中で、人との様々な関わり方に気付き、相手の気持ちを考えて関わり、自分が役に立つ喜びを感じ、地域に親しみをもつようになる。また、学校内外の様々な環境に関わる中で、遊びや生活に必要な情報を取り入れ、情報に基づき判断したり、情報を伝え合ったり、活用したりするなど、情報を役立てながら活動するようになるとともに、公共の施設を大切に利用するなどして、社会とのつながりなどを意識するようになる。",
                        "身近な事象に積極的に関わる中で、物の性質や仕組みなどを感じ取ったり、気付いたりし、考えたり、予想したり、工夫したりするなど、多様な関わりを楽しむようになる。また、友達の様々な考えに触れる中で、自分と異なる考えがあることに気付き、自ら判断したり、考え直したりするなど、新しい考えを生み出す喜びを味わいながら、自分の考えをよりよいものにするようになる。",
                        "自然に触れて感動する体験を通して、自然の変化などを感じ取り、好奇心や探究心をもって考え言葉などで表現しながら、身近な事象への関心が高まるとともに、自然への愛情や畏敬の念をもつようになる。また、身近な動植物に心を動かされる中で、生命の不思議さや尊さに気付き、身近な動植物への接し方を考え、命あるものとしていたわり、大切にする気持ちをもって関わるようになる。",
                        "遊びや生活の中で、数量や図形、標識や文字などに親しむ体験を重ねたり、標識や文字の役割に気付いたりし、自らの必要感に基づきこれらを活用し、興味や関心、感覚をもつようになる。",
                        "先生や友達と心を通わせる中で、絵本や物語などに親しみながら、豊かな言葉や表現を身に付け、経験したことや考えたことなどを言葉で伝えたり、相手の話を注意して聞いたりし、言葉による伝え合いを楽しむようになる。",
                        "心を動かす出来事などに触れ感性を働かせる中で、様々な素材の特徴や表現の仕方などに気付き、感じたことや考えたことを自分で表現したり、友達同士で表現する過程を楽しんだりし、表現する喜びを味わい、意欲をもつようになる。",
                };

                int totalgyo = 0;
                for (int i = 0; i < sugataItems.length; i++) {
                    totalgyo += 1;
                    final List<String> tokenList3 = getTokenList(sugataItems[i], 25 * 2);

                    for (int j = 0; j < tokenList3.size(); j++) {
                        final int gyo = totalgyo + j + 1;
                        VrsOutn("SUGATA_ITEM1", gyo, tokenList3.get(j));
                        if (j < tokenList3.size() - 1) {
                            VrAttributen("SUGATA_ITEM1", gyo, "Hensyu=4"); // 均等割
                        }
                    }
                    totalgyo += tokenList3.size();
                    totalgyo += 1;
                }
            }
        }

        private void printShoken(final String fieldname, final String data, final String prop, final int mojisu, final int gyo, final String debug) {
            if (null == data) {
                return;
            }
            final KNJPropertiesShokenSize size;
            if (StringUtils.isBlank(prop)) {
                final SvfField field = getSvfField(fieldname);
                if (null == field) {
                    log.info(" no field : " + fieldname);
                    size = KNJPropertiesShokenSize.getShokenSize(prop, mojisu, gyo);
                } else {
                    size = KNJPropertiesShokenSize.getShokenSize(prop, field._fieldLength / 2, field._fieldRepeatCount);
                }
            } else {
                size = KNJPropertiesShokenSize.getShokenSize(prop, mojisu, gyo);
            }
            VrsOutnRepeat(fieldname, getTokenList(data, size.getKeta(), size._gyo)); //備考
        }

        private void printName(final DB2UDB db2, final PersonalInfo personalInfo) {
            VrsOutForData(Arrays.asList("KANA1", "KANA2"), personalInfo._studentKana);
            VrsOutForData(Arrays.asList("NAME1", "NAME2", "NAME3"), personalInfo._studentName);
            VrsOut("BIRTHDAY", KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP(db2, personalInfo._birthday)) + "生");
            VrsOut("SEX", personalInfo._sex);
        }
    }

    private static class JViewPoint {
        final String _pointDiv;
        final String _pointLCd;
        final String _remarkL;
        final List<JViewPoint.M> _mList = new ArrayList<JViewPoint.M>();

        JViewPoint(
            final String pointDiv,
            final String pointLCd,
            final String remarkL
        ) {
            _pointDiv = pointDiv;
            _pointLCd = pointLCd;
            _remarkL = remarkL;
        }

        private static class M {
            final String _pointMCd;
            final String _remarkM;

            M(
                final String pointMCd,
                final String remarkM
            ) {
                _pointMCd = pointMCd;
                _remarkM = remarkM;
            }
        }

        public static List<JViewPoint> getList(final DB2UDB db2, final Param param) {
            final List<JViewPoint> list = new ArrayList();

            final Map<String, JViewPoint> lMap = new HashMap<String, JViewPoint>();
            final String sql = sql(param);
            for (final Map<String ,String> row : KnjDbUtils.query(db2, sql)) {
                final String pointDiv = KnjDbUtils.getString(row, "POINT_DIV");
                final String pointLCd = KnjDbUtils.getString(row, "POINT_L_CD");
                final String remarkL = KnjDbUtils.getString(row, "REMARK_L");
                final String pointMCd = KnjDbUtils.getString(row, "POINT_M_CD");
                final String remarkM = KnjDbUtils.getString(row, "REMARK_M");
                if (!lMap.containsKey(pointDiv + pointLCd)) {
                    final JViewPoint jviewpoint = new JViewPoint(pointDiv, pointLCd, remarkL);
                    list.add(jviewpoint);
                    lMap.put(pointDiv + pointLCd, jviewpoint);
                }
                lMap.get(pointDiv + pointLCd)._mList.add(new M(pointMCd, remarkM));
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.POINT_DIV, ");
            stb.append("   T1.POINT_L_CD, ");
            stb.append("   T1.REMARK_L, ");
            stb.append("   T2.POINT_M_CD, ");
            stb.append("   T2.REMARK_M ");
            stb.append(" FROM JVIEW_POINT_L_MST T1 ");
            stb.append(" LEFT JOIN JVIEW_POINT_M_MST T2 ON T2.POINT_DIV = T1.POINT_DIV ");
            stb.append("     AND T2.POINT_L_CD = T1.POINT_L_CD ");
            stb.append(" ORDER BY ");
            stb.append("    T1.POINT_DIV, ");
            stb.append("    T1.POINT_L_CD, ");
            stb.append("    T2.POINT_M_CD ");
            stb.append("  ");
            return stb.toString();
        }
    }

    /**
     * SVF フィールドのデータ (文字の大きさ調整に使用)
     */
    private static class SvfFieldData {
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

        static SchregRegdDat create() {
            return new SchregRegdDat();
        }

        static Map<String, List<SchregRegdDat>> getYearRegdListMap(final Collection<SchregRegdDat> regdList) {
            final Map<String, List<SchregRegdDat>> rtn = new TreeMap<String, List<SchregRegdDat>>();
            for (final SchregRegdDat regd : regdList) {
                Util.getMappedList(rtn, regd._year).add(regd);
            }
            return rtn;
        }

        static SchregRegdDat getMaxSemesterRegd(final Collection<SchregRegdDat> regdList, final String year) {
            if (null == regdList || regdList.size() == 0 || null == year) {
                return null;
            }
            final TreeMap<String, TreeMap<String, SchregRegdDat>> m = new TreeMap<String, TreeMap<String, SchregRegdDat>>();
            for (final SchregRegdDat regd : regdList) {
                if (null != regd._year && null != regd._semester) {
                    Util.getMappedTreeMap(m, regd._year).put(regd._semester, regd);
                }
            }
            final TreeMap<String, SchregRegdDat> yearMap = Util.getMappedTreeMap(m, year);
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

    protected static class Indexed<T> {

        final int _idx;
        final T _val;
        public Indexed(final int idx, final T val) {
            _idx = idx;
            _val = val;
        }

        public String idxStr() {
            return String.valueOf(_idx);
        }

        public static <T> List<Indexed<T>> indexed(final List<T> list) {
            return indexed(0, list);
        }

        public static <T> List<Indexed<T>> indexed(final int startIdx, final List<T> list) {
            final List<Indexed<T>> rtn = new ArrayList<Indexed<T>>();
            int idx = startIdx;
            for (final T t : list) {
                rtn.add(new Indexed(idx, t));
                idx += 1;
            }
            return rtn;
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
        private static List<String> filterNotBlank(final List<String> list) {
            final List<String> rtn = new ArrayList<String>();
            for (final String data : list) {
                if (!StringUtils.isBlank(data)) {
                    rtn.add(data);
                }
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
        private static <T> List<T> take(final List<T> list, final int max) {
            final List<T> rtn = new ArrayList<T>();
            for (final T t : list) {
                if (rtn.size() >= max) {
                    break;
                }
                rtn.add(t);
            }
            return rtn;
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

        private static String nendoDefaultBaseDate(final String year) {
            return year + "-12-31";
        }

        private static Tuple<Integer, Integer> diffYearMonth(final String startDate, final String endDate) {

            final Calendar calStart = Calendar.getInstance();
            calStart.setTime(Date.valueOf(startDate));
            int startYear = calStart.get(Calendar.YEAR);
            int startMonth = calStart.get(Calendar.MONTH) + 1;
            int startDay = calStart.get(Calendar.DAY_OF_MONTH);

            final Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(Date.valueOf(endDate));
            int endYear = calEnd.get(Calendar.YEAR);
            int endMonth = calEnd.get(Calendar.MONTH) + 1;
            int endDay = calEnd.get(Calendar.DAY_OF_MONTH);

            if (startDay > endDay) {
                startDay += 30;
                startMonth += 1;
                if (startMonth > 12) {
                    startYear += 1;
                    startMonth -= 12;
                }
            }
            if (startMonth > endMonth) {
                endMonth += 12;
                endYear -= 1;
            }
            return Tuple.of(endYear - startYear, endMonth - startMonth);
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
        private static String mkString(final List<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (final String s : list) {
                if (StringUtils.isBlank(s)) {
                    continue;
                }
                stb.append(c).append(s);
                c = comma;
            }
            return stb.toString();
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

        private static int getNendo(final Calendar cal) {
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH);
            if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
                return year - 1;
            }
            return year;
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

        private static <T> String debugCollectionToStr(final String debugText, final Collection<T> col, final String comma) {
            final StringBuffer stb = new StringBuffer();
            final List<T> list = new ArrayList<T>(col);
            stb.append(defstr(debugText) + " [\n");
            for (int i = 0; i < list.size(); i++) {
                stb.append(i == 0 ? StringUtils.repeat(" ", StringUtils.defaultString(comma).length()) : comma).append(i).append(": ").append(list.get(i)).append("\n");
            }
            stb.append("]");
            return stb.toString();
        }

        private static String defstr(final Object str1) {
            return defstr(str1, "");
        }

        private static String defstr(final Object str1, final String ... str2) {
            return null == str1 ? defval(null, str2) : str1.toString();
        }

        private static <T> T defval(final T v, final T ... vs) {
            if (null != v) {
                return v;
            }
            if (null != vs) {
                for (final T s : vs) {
                    if (null != s) {
                        return s;
                    }
                }
            }
            return null;
        }

        private static <T> T defval(final Collection<T> vs) {
            if (null != vs) {
                for (final T s : vs) {
                    if (null != s) {
                        return s;
                    }
                }
            }
            return null;
        }
    }

    private static class Param {

        final static String CERTIF_KINDCD = "133";
        final static String SCHOOL_KIND = "K";

        final String _year;
        final String _gakki;
        final String _gradeHrclass;
        final String _useSchregRegdHdat;
        final String _output;
        final String[] _categorySelected;
        final String _inei;
        final String _chiteki = "2"; // 1:知的障害 2:知的障害以外  奈良県は知的以外の気がする 固定2としておく
        final String _birthdayFormat; // 奈良県のみ 1:和暦 2:西暦
        final String _useSpecial_Support_School; // 1:特別支援
        final Map _paramMap;

        final String _seito;
        final String _sidou;

        final String _simei;
//        final String _schzip;
//        final String _schoolzip;
        final String _colorPrint;
        final String _documentroot;

        final KNJDefineCode _definecode; // 各学校における定数等設定
//        Map hmap = null;

        final String _imagePath;
        final String _extension;

        Map<String, Semester> _semesterMap;
        final StaffInfo _staffInfo = new StaffInfo();

        /** 生年月日に西暦を使用するか */
        final boolean _isSeireki;

        final Map<String, String> _gradeCdMap;

        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();

        private boolean _isNaraken;
        private boolean _isMusashinohigashi;

        final boolean _hasSchregEntGrdHistComebackDat;
        final boolean _hasAftGradCourseDat;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final List<JViewPoint> _jviewPointList;

        private String _schoolName1;
        private String _certifSchoolName;
        private String _schoolZipCd;
        private String _schoolAddr1;
        private String _schoolAddr2;
        private String _certifSchoolRemark1;
        private String _certifSchoolRemark2;
        private String _certifSchoolRemark3;
        private String _certifSchoolRemark4;
        private String _certifSchoolRemark5;

        final String _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_K;
        final String _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_K;
        final String _HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_K;
        final String _HTRAINREMARK_DAT_VIEWREMARK_SIZE_K;
        final String _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_K;
        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_K;
        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K;
        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K_disability;
        final String _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_K;
        final String _HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_K_disability;

        private Map<String, String> _dbPrgInfoProperties;
        private Properties _prgInfoPropertiesFilePrperties;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugStaff;
        private Map<String, File> _createFormFileMap = new TreeMap();
        private Set logged = new TreeSet();

        public Param(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) throws SQLException {
            _documentroot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
            if (!StringUtils.isEmpty(_documentroot)) {
                _prgInfoPropertiesFilePrperties = loadPropertyFile("prgInfo.properties");
            }
            _dbPrgInfoProperties = getDbPrginfoProperties(db2);
            final String[] outputDebug = StringUtils.split(_dbPrgInfoProperties.get("outputDebug"));
            log.info(" outputDebug = " + ArrayUtils.toString(outputDebug));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugStaff = ArrayUtils.contains(outputDebug, "staff");

            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS"); // 学年・組
            _categorySelected = request.getParameterValues("category_selected");
            _output = request.getParameter("OUTPUT");

            _seito = request.getParameter("seito");
            _sidou = request.getParameter("sidou");

            _simei = request.getParameter("simei"); // 漢字名出力
//            _schzip = request.getParameter("schzip");
//            _schoolzip = request.getParameter("schoolzip");
            _colorPrint = request.getParameter("color_print");

            _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
            if ("1".equals(request.getParameter("seitoSidoYorokuPrintInei"))) {
                _inei = "2";
            } else {
                _inei = "".equals(request.getParameter("INEI")) ? null : request.getParameter("INEI");
            }
            _birthdayFormat = request.getParameter("BIRTHDAY_FORMAT");
            _useSpecial_Support_School = property(request, "useSpecial_Support_School");
            _paramMap = paramMap;

            _definecode = new KNJDefineCode(); // 各学校における定数等設定
            _definecode.setSchoolCode(db2, _year);

            _imagePath = "image/stamp";
            _extension = "bmp";

            loadSchoolName(db2, this);

            _isSeireki = KNJ_EditDate.isSeireki(db2);

            _gradeCdMap = getGradeCdMap(db2);

            final String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            _isMusashinohigashi = "musashinohigashi".equals(name1);
            _isNaraken = "naraken".equals(name1);

            _hasSchregEntGrdHistComebackDat = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _hasAftGradCourseDat = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _jviewPointList = JViewPoint.getList(db2, this);
            setMaster(db2);
            _staffInfo.setInkanMap(db2, this);
            _staffInfo.setYearPrincipalMap(db2, this);
            _staffInfo._staffMstMap = StaffMst.load(db2, _year);
            _staffInfo.setStaffClassHistMap(db2, this, _year);

            _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_K                      = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_K");
            _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_K                      = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_K");
            _HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_K                   = request.getParameter("HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_K");
            _HTRAINREMARK_DAT_VIEWREMARK_SIZE_K                         = request.getParameter("HTRAINREMARK_DAT_VIEWREMARK_SIZE_K");
            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_K                        = request.getParameter("HTRAINREMARK_DAT_TOTALREMARK_SIZE_K");
            _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_K                   = request.getParameter("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_K");
            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K             = request.getParameter("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K");
            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K_disability  = request.getParameter("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K_disability");
            _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_K                 = request.getParameter("HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_K");
            _HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_K_disability        = request.getParameter("HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_K_disability");
        }

        protected static Map<String, String> getDbPrginfoProperties(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA133K' "), "NAME", "VALUE");
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

        protected String property(final HttpServletRequest request, final String name) {
            if (request.getParameterMap().containsKey(name)) {
                return request.getParameter(name);
            }
            String val = null;
            if (null != _dbPrgInfoProperties) {
                if (_dbPrgInfoProperties.containsKey(name)) {
                    val = _dbPrgInfoProperties.get(name);
                    if (_isOutputDebug) {
                        log.info("property in db: " + name + " = " + val);
                    }
                    return val;
                }
            }
            if (null != _prgInfoPropertiesFilePrperties) {
                if (_prgInfoPropertiesFilePrperties.containsKey(name)) {
                    val = _prgInfoPropertiesFilePrperties.getProperty(name);
                    if (_isOutputDebug) {
                        log.info("property in file: " + name + " = " + val);
                    }
                } else {
                    if (_isOutputDebug) {
                        log.warn("property not exists in file: " + name);
                    }
                }
            }
            return val;
        }

        public void closeForm() {
            for (final File file : _createFormFileMap.values()) {
                if (null != file && file.exists()) {
                    file.delete();
                }
            }
        }

        private void setMaster(final DB2UDB db2) {
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

        /**
         * 学校データを得る
         */
        private void loadSchoolName(final DB2UDB db2, final Param param) {

            final Map row1 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR='" + param._year + "' AND CERTIF_KINDCD='" + Param.CERTIF_KINDCD + "'"));
            final String rtn = KnjDbUtils.getString(row1, "SCHOOL_NAME");
            _certifSchoolRemark1 = KnjDbUtils.getString(row1, "REMARK1");
            _certifSchoolRemark2 = KnjDbUtils.getString(row1, "REMARK2");
            _certifSchoolRemark3 = KnjDbUtils.getString(row1, "REMARK3");
            _certifSchoolRemark4 = KnjDbUtils.getString(row1, "REMARK4");
            _certifSchoolRemark5 = KnjDbUtils.getString(row1, "REMARK5");

            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + rtn + "]");

            _certifSchoolName = rtn;

            final Map paramMap = new HashMap();
            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                paramMap.put("schoolMstSchoolKind", Param.SCHOOL_KIND);
            }
            final String sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql, new Object[] {param._year, param._year}));
            _schoolName1 = KnjDbUtils.getString(row, "SCHOOLNAME1");
            _schoolZipCd = KnjDbUtils.getString(row, "SCHOOLZIPCD");
            _schoolAddr1 = KnjDbUtils.getString(row, "SCHOOLADDR1");
            _schoolAddr2 = KnjDbUtils.getString(row, "SCHOOLADDR2");
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
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
        }

        private List<KNJA133K_0> getKnja133List(final Vrw32alp svf) {
            final List<KNJA133K_0> rtnList = new ArrayList<KNJA133K_0>();
            if (_seito!= null) {
                rtnList.add(new KNJA133K_1(this, svf)); // 在籍に関する記録
            }
            if (_sidou != null) {
                rtnList.add(new KNJA133K_2(this, svf)); // 指導の記録
            }
            return rtnList;
        }

        protected List<String> getSchregnoList(final DB2UDB db2) {
            final List<String> schregnoList = new ArrayList<String>();
            if ("2".equals(_output)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.SCHREGNO ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _gakki + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");

                schregnoList.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO"));
            } else {
                schregnoList.addAll(Arrays.asList(_categorySelected));
            }
            return schregnoList;
        }

        public String getImageFilePath(final String filename) {
            String ret = null;
            try {
                if (null != _documentroot && null != _imagePath && null != _extension) {
                    // 写真データ存在チェック
                    final String path = _documentroot + "/" + _imagePath + "/" + filename + "." + _extension;
                    final File file = new File(path);
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
            if (_isOutputDebug) {
                logOnce(" year = " + year + ", grade = " + grade + ", gradeCd = " + gradeCd);
            }
            return NumberUtils.isNumber(gradeCd) ? Integer.parseInt(gradeCd) : -1;
        }

        private void logOnce(final String message) {
            if (logged.contains(message)) {
                return;
            }
            log.info(message);
        }

        private String getZ010(DB2UDB db2, final String field) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private Map getGradeCdMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     YEAR || GRADE AS KEY, GRADE_CD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "KEY", "GRADE_CD");
        }
    }
}
