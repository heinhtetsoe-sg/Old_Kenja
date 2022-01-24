/*
 * $Id: e78d942cfd951477bbce5039be3e5edc32cbe46b $
 *
 * 作成日: 2010/09/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.detail.KNJ_AttendrecSql;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJE374 {

    private static final Log log = LogFactory.getLog(KNJE374.class);

    private boolean _hasData;
    private static final String PATTERNA = "1";
    private static final String OUTPUT2 = "2";

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(svf, db2);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) {
        int maxLine = 35;
        final String form;
        if (OUTPUT2.equals(_param._output)) {
            form = "KNJE374_4.frm";
            maxLine = 25;
        } else if (PATTERNA.equals(_param._knje374Pattern)) {
            form = "KNJE374_3.frm";
        } else if (Param.PRINT_GRD.equals(_param._student)) {
            form = _param._isKindai ? "KNJE374_2KIN.frm" : "KNJE374_2.frm";
        } else {
            form = _param._isKindai ? "KNJE374_1KIN.frm" : "KNJE374_1.frm";
        }

        svf.VrSetForm(form, 1);

        final List finschools = Student.getFinschools(db2, _param);

        for (final Iterator it = finschools.iterator(); it.hasNext();) {
            final Finschool finschool = (Finschool) it.next();
            if (finschool._students.size() == 0) {
                continue;
            }

            final List pageList = getPageList(finschool._students, maxLine);

            for (int pi = 0; pi < pageList.size(); pi++) {
                final List studentList = (List) pageList.get(pi);

                if (OUTPUT2.equals(_param._output)) {
                    svf.VrsOut("NENDO", "出身学校別生徒一覧"); // 年度　タイトル
                    svf.VrsOut("SUBTITLE", "科目名・成績（" + StringUtils.defaultString(_param._recSemesterName) + StringUtils.defaultString(_param._recTestkindName) + "）"); // サブタイトル

                    svf.VrsOut("DATE3", _param.getDateRange()); // 出欠の記録の集計範囲
                    svf.VrsOut("T_TOTAL", "総合点"); // 項目
                    svf.VrsOut("T_AVERAGE", "平均点"); // 項目
                    svf.VrsOut("T_RANK", "学級順位"); // 項目
                    svf.VrsOut("T_RANK2", "コース順位"); // 項目
                } else {
                    svf.VrsOut("PAGE", String.valueOf(pi + 1));
                }
                svf.VrsOut("FINSCHOOL_NAME", finschool._finschoolName);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));

                for (int li = 0; li < studentList.size(); li++) {
                    final Student student = (Student) studentList.get(li);

                    final int line = li + 1;
                    if (OUTPUT2.equals(_param._output)) {
                        svf.VrsOutn(ms932ByteLength(student.getGradeHrclassAttendno()) > 8 ? "HR_NAME2" : "HR_NAME1", line, student.getGradeHrclassAttendno()); // 年組番
                        svf.VrsOutn(ms932ByteLength(student._name) > 16 ? "NAME2" : "NAME1", line, student._name); // 生徒氏名
                        svf.VrsOutn(ms932ByteLength(student._coursecodename) > 10 ? "COURSE_NAME2" : "COURSE_NAME", line, student._coursecodename); // コース名

                        int c = 1;
                        for (int i = 0; i < student._scoreMapList.size(); i++) {
                            final Map scoreMap = (Map) student._scoreMapList.get(i);
                            if ("999999".equals(scoreMap.get("SUBCLASSCD"))) {
                                svf.VrsOutn("TOTAL", line, (String) scoreMap.get("SCORE")); // 総合点
                                svf.VrsOutn("AVERAGE", line, sishagonyu((String) scoreMap.get("AVG"))); // 平均点
                                svf.VrsOutn("RANK", line, (String) scoreMap.get("CLASS_RANK")); // 学級順位
                                svf.VrsOutn("RANK2", line, (String) scoreMap.get("COURSE_RANK")); // コース順位
                            } else {
                                final String sc = String.valueOf(c);
                                svf.VrsOutn("SUBJECT" + sc, line, (String) scoreMap.get("SUBCLASSNAME")); // 科目名
                                svf.VrsOutn("GRADING" + sc, line, (String) scoreMap.get("SCORE")); // 成績
                                c += 1;
                            }
                        }

                        svf.VrsOutn("ABSENCE", line, student._sick); // 欠席日数
                        svf.VrsOutn("LATE", line, student._late); // 遅刻回数
                        svf.VrsOutn("LEAVE", line, student._early); // 早退回数
                    } else {
                        svf.VrsOutn("SCHREGNO", line, student._schregno);
                        svf.VrsOutn("GRADE", line, StringUtils.isNumeric(student._grade) ? String.valueOf(Integer.parseInt(student._grade)) : student._grade);
                        if (_param._isKindai) {
                            svf.VrsOutn("HR_CLASS", line, student._hrClass);
                        } else {
                            svf.VrsOutn("HR_CLASS", line, null != student._hrClassName1 ? student._hrClassName1 : student._hrClass);
                        }
                        svf.VrsOutn("ATTENDNO", line, student._attendno);
                        svf.VrsOutn(ms932ByteLength(student._name) > 20 ? "NAME2" : "NAME1", line, student._name);

                        if (PATTERNA.equals(_param._knje374Pattern)) {
                            svf.VrsOutn("SEX", line, student._sex);
                            svf.VrsOutn("BIRTHDAY", line, student._birthday.replace('-', '/'));
                            svf.VrsOutn("ZIPNO", line, student._zipcd);
                            final String addr = student._addr1 + student._addr2;
                            svf.VrsOutn(ms932ByteLength(addr) > 40 ? "ADDR2" : "ADDR1", line, addr);
                            svf.VrsOutn("TELNO1", line, student._telno);
                            svf.VrsOutn(ms932ByteLength(student._guard_name) > 20 ? "GURD_NAME2" : "GURD_NAME1", line, student._guard_name);
                            svf.VrsOutn("TELNO2", line, student._guard_telno);
                        } else {
                            svf.VrsOut("RECORD_NAME1", _param._recSemesterName);
                            svf.VrsOut("RECORD_NAME2", _param._recTestkindName);
                            svf.VrsOutn("VALUE", line, student._gakkiSeiseki);
                            svf.VrsOutn("CLASS_RANK", line, student._classRank);
                            svf.VrsOutn("VALUATION", line, student._hyoteiHeikin);
                            svf.VrsOutn("ABSENCE", line, student._attendrecKesseki);

                            svf.VrsOutn(ms932ByteLength(student._name) < 40 ? "CLUB1" : "CLUB2", line, student.getClubNames());
                            svf.VrsOutn(ms932ByteLength(student._name) < 40 ? "COMMITTEE1" : "COMMITTEE2", line, student.getCommiteeNames());

                            if (Param.PRINT_GRD.equals(_param._student)) {
                                svf.VrsOutn("YEAR", line, student._year);
                                svf.VrsOutn(ms932ByteLength(student._college1) < 40 ? "COLLEGE1_1" : "COLLEGE1_2", line, student._college1);
                                svf.VrsOutn(ms932ByteLength(student._faculty1) < 40 ? "FACULTY1_1" : "FACULTY1_2", line, student._faculty1);
                                svf.VrsOutn(ms932ByteLength(student._department1) < 40 ? "DEPARTMENT1_1" : "DEPARTMENT1_2", line, student._department1);
                            }
                        }
                    }
                    _hasData = true;
                }

                for (int li = studentList.size() + 1; li < maxLine; li++) {
                    svf.VrsOutn("SCHREGNO", li, "");
                }
                svf.VrEndPage();
            }
        }
    }

    private String sishagonyu(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return null;
        }
        return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static int ms932ByteLength(String name) {
        int count = 0;
        if (name != null) {
            try {
                count = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return count;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrClassName1;
        final String _attendno;
        final String _name;

        // OUTPUT=1 && knje374Pattern != PATTERNA
        String _year;
        String _recSemester;
        String _committee1Name;
        String _committee1Chargename;
        String _committee2Name;
        String _committee2Chargename;
        String _club1Name;
        String _club1Executivename;
        String _club2Name;
        String _club2Executivename;
        String _attendrecKesseki;
        String _hyoteiHeikin;
        String _classRank;
        String _gakkiSeiseki;
        String _college1;
        String _faculty1;
        String _department1;

        // knje374Pattern = PATTERNA
        String _sex;
        String _birthday;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _telno;
        String _guard_name;
        String _guard_telno;

        // OUTPUT=2, NOT GRD
        String _coursecodename;
        List _scoreMapList = null;
        String _sick;
        String _late;
        String _early;

        public Student(final String schregno, final String grade, final String hrClass, final String hrClassName1, final String attendno, final String name) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrClassName1 = hrClassName1;
            _attendno = attendno;
            _name = name;
        }

        public String getGradeHrclassAttendno() {
            final StringBuffer stb = new StringBuffer();
            stb.append(NumberUtils.isDigits(_grade) ? String.valueOf(Integer.parseInt(_grade)) : StringUtils.defaultString(_grade));
            stb.append("-");
            stb.append(NumberUtils.isDigits(_hrClass) ? String.valueOf(Integer.parseInt(_hrClass)) : StringUtils.defaultString(_hrClass));
            stb.append("-");
            stb.append(StringUtils.defaultString(_attendno));
            return stb.toString();
        }

        public String getClubNames() {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            if (_club1Name != null) {
                stb.append(comma);
                stb.append(_club1Name);
                if (_club1Executivename != null && !"".equals(_club1Executivename)) {
                    stb.append("（").append(_club1Executivename).append("）");
                }
                comma = "、";
            }
            if (_club2Name != null) {
                stb.append(comma);
                stb.append(_club2Name);
                if (_club2Executivename != null && !"".equals(_club2Executivename)) {
                    stb.append("（").append(_club2Executivename).append("）");
                }
                comma = "、";
            }
            return stb.toString();
        }

        public String getCommiteeNames() {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            if (_committee1Name != null) {
                stb.append(comma);
                stb.append(_committee1Name);
                if (_committee1Chargename != null && !"".equals(_committee1Chargename)) {
                    stb.append("（").append(_committee1Chargename).append("）");
                }
                comma = "、";
            }
            if (_committee2Name != null) {
                stb.append(comma);
                stb.append(_committee2Name);
                if (_committee2Chargename != null && !"".equals(_committee2Chargename)) {
                    stb.append("（").append(_committee2Chargename).append("）");
                }
                comma = "、";
            }
            return stb.toString();
        }

        public String toString() {
            return _schregno + " : " + _name;
        }

        private static List getFinschools(final DB2UDB db2, final Param param) {
            final List finschools = new ArrayList();
            final Map finschoolMap = new HashMap();
            final Map students = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getRegdSql(param);
                log.debug(" regd sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String finschoolcd = rs.getString("FINSCHOOLCD");
                    if (null == finschoolMap.get(finschoolcd)) {
                        final Finschool finschool = new Finschool(finschoolcd, rs.getString("FINSCHOOL_NAME"));
                        finschools.add(finschool);
                        finschoolMap.put(finschoolcd, finschool);
                    }

                    final Finschool finschool = (Finschool) finschoolMap.get(finschoolcd);

                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = !StringUtils.isBlank(rs.getString("GRADE_CD")) ? rs.getString("GRADE_CD") : rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    final String attendno = rs.getString("ATTENDNO");
                    final String studentName = param._staffInfo.getStrEngOrJp(rs.getString("NAME"), rs.getString("NAME_ENG"));

                    final Student student = new Student(schregno, grade, hrClass, hrClassName1, attendno, studentName);
                    finschool._students.add(student);
                    students.put(schregno, student);

                    if (OUTPUT2.equals(param._output)) {
                        student._coursecodename = rs.getString("COURSECODENAME");

                    } else if (PATTERNA.equals(param._knje374Pattern)) {
                        student._sex = rs.getString("SEX");
                        student._birthday = rs.getString("BIRTHDAY");
                        student._zipcd = rs.getString("ZIPCD");
                        student._addr1 = rs.getString("ADDR1");
                        student._addr2 = rs.getString("ADDR2");
                        student._telno = rs.getString("TELNO");
                        student._guard_name = rs.getString("GUARD_NAME");
                        student._guard_telno = rs.getString("GUARD_TELNO");
                    } else {
                        student._year = rs.getString("YEAR");
                        student._recSemester = rs.getString("SEMESTER");
                        student._committee1Name = rs.getString("COMMITTEENAME1");
                        student._committee1Chargename = rs.getString("CHARGENAME1");
                        student._committee2Name = rs.getString("COMMITTEENAME2");
                        student._committee2Chargename = rs.getString("CHARGENAME2");
                        student._club1Name = rs.getString("CLUBNAME1");
                        student._club1Executivename = rs.getString("EXECUTIVENAME1");
                        student._club2Name = rs.getString("CLUBNAME2");
                        student._club2Executivename = rs.getString("EXECUTIVENAME2");
                    }

                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (OUTPUT2.equals(param._output)) {
                setAttendSemes(db2, students.values(), param);
                setRecordScore(db2, students.values(), param);
            } else if (!PATTERNA.equals(param._knje374Pattern)) {
                setHeikinRank(db2, students.values(), param);
                setHyoteiHeikin(db2, students.values(), param);
                setAttendrecKesseki(db2, students.values(), param);
                if (Param.PRINT_GRD.equals(param._student)) {
                    setAftGradCourseDat(db2, students.values(), param);
                }
            }
            return finschools;
        }

        private static void setRecordScore(final DB2UDB db2, final Collection students, final Param param) {
            if (param._recTestKindCd == null || param._recTestKindCd.length() < 4) {
                return;
            }
            log.debug(" set record rank count = " + students.size());
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("    T1.SUBCLASSCD ");
            sql.append("    , VALUE(T2.SUBCLASSABBV, T2.SUBCLASSNAME) AS SUBCLASSNAME ");
            sql.append("    , T1.SCORE ");
            sql.append("    , T1.AVG ");
            sql.append("    , T1." + param.getRankField("CLASS") + " AS CLASS_RANK ");
            sql.append("    , T1." + param.getRankField("COURSE") + " AS COURSE_RANK ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                sql.append(" FROM RECORD_RANK_SDIV_DAT T1");
            } else {
                sql.append(" FROM RECORD_RANK_DAT T1");
            }
            sql.append(" LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("    AND T1.CLASSCD = T2.CLASSCD ");
                sql.append("    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                sql.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            sql.append(" WHERE ");
            sql.append("    T1.YEAR = '" + param._ctrlYear + "' ");
            sql.append("    AND T1.SEMESTER = '" + param._recSemester + "' ");
            sql.append("    AND T1.TESTKINDCD = '" + param._recTestKindCd.substring(0, 2) + "' ");
            sql.append("    AND T1.TESTITEMCD = '" + param._recTestKindCd.substring(2, 4) + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                sql.append("    AND T1.SCORE_DIV = '" + param._recTestKindCd.substring(4) + "' ");
            }
            sql.append("    AND T1.SCHREGNO = ? ");
            sql.append("    AND (T2.SUBCLASSCD IS NOT NULL OR T1.SUBCLASSCD = '999999') ");
            sql.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append(" T1.CLASSCD, ");
                sql.append(" T1.SCHOOL_KIND, ");
                sql.append(" T1.CURRICULUM_CD, ");
            }
            sql.append(" T1.SUBCLASSCD ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    //log.debug(" set record rank student = " + student._schregno);

                    student._scoreMapList = new ArrayList();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    try {
                        ResultSetMetaData meta = rs.getMetaData();
                        while (rs.next()) {
                            final Map map = new HashMap();
                            for (int i = 1; i <= meta.getColumnCount(); i++) {
                                final String fieldname = meta.getColumnName(i);
                                map.put(fieldname, rs.getString(fieldname));
                            }
                            student._scoreMapList.add(map);
                        }
                    } catch (SQLException e) {
                        log.error("record rank exception!", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("record rank exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static void setAttendSemes(final DB2UDB db2, final Collection students, final Param param) {
            if (StringUtils.isBlank(param._useTestCountflg)) {
                param._attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW");
            } else {
                param._attendParamMap.put("useTestCountflg", param._useTestCountflg);
            }
            param._attendParamMap.put("schregno", "?");

            log.debug(" set attend count = " + students.size());

            final String sql = AttendAccumulate.getAttendSemesSql(param._ctrlYear, Param.SEM_ALL, param._attendSdate, param._attendEdate, param._attendParamMap);
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    // log.debug(" set attend student = " + student._schregno);

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            if (!Param.SEM_ALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            student._sick = rs.getString("SICK");
                            student._late = rs.getString("LATE");
                            student._early = rs.getString("EARLY");
                        }
                    } catch (SQLException e) {
                        log.error("attend semes exception!", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("attend semes exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String getRecordDatSql(final Param param, final String year, final String semester, final String grade, final String hrClass) {
            final String fieldName;
            if ("KIN_RECORD_DAT".equals(param._useRecordDat)) {
                if (Param.SEM_ALL.equals(param._recSemester)) {
                    // "0000".equals(_param._recTestKindCd)
                    fieldName = "GRADE_RECORD";
                } else {
                    if ("0101".equals(param._recTestKindCd)) {
                        fieldName = "SEM" + param._recSemester + "_INTER_REC";
                    } else if ("0201".equals(param._recTestKindCd)) {
                        fieldName = "SEM" + param._recSemester + "_TERM_REC";
                    } else { // "0000".equals(_param._recTestKindCd)
                        fieldName = "SEM" + param._recSemester + "_REC";
                    }
                }
            } else { // RECORD_DAT
                if (Param.SEM_ALL.equals(param._recSemester)) {
                    fieldName = "GRAD_VALUE";
                } else {
                    fieldName = "SEM" + param._recSemester + "_VALUE";
                }
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_SEMESTER_REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            if (Param.PRINT_GRD.equals(param._student)) {
                stb.append("     INNER JOIN ( ");
                stb.append("         SELECT ");
                stb.append("             SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
                stb.append("         FROM ");
                stb.append("             SCHREG_REGD_DAT ");
                stb.append("         GROUP BY ");
                stb.append("             SCHREGNO, ");
                stb.append("             YEAR ");
                stb.append("     ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            if (Param.SEM_ALL.equals(semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     SUM(DECIMAL(T2." + fieldName + ", 4, 1)) AS SUM, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(T2." + fieldName + ")), 1), 4, 1) AS AVG, ");
            stb.append("     RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS ORDER BY ROUND(AVG(FLOAT(VALUE(T2." + fieldName + ", 0))), 1) DESC) AS CLASS_RANK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN " + param._useRecordDat + " T2 ON ");
            stb.append("         T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.YEAR = T1.YEAR               ");
            stb.append("     INNER JOIN MAX_SEMESTER_REGD T3 ON ");
            stb.append("         T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.GRADE = T1.GRADE ");
            stb.append("         AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T2." + fieldName + " IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, CLASS_RANK ");
            return stb.toString();
        }

        private static void setHeikinRank(final DB2UDB db2, final Collection students, final Param param) {
            final List recGradeList = RecGrade.getRecGradeList(students);
            log.debug(" recGrade size = " + recGradeList.size());
            int i = 1;
            for (final Iterator it = recGradeList.iterator(); it.hasNext();) {
                final RecGrade recGrade = (RecGrade) it.next();
                log.debug(" recGrade (" + i + ") = " + recGrade);
                i += 1;
                final String sql;
                if ("KIN_RECORD_DAT".equals(param._useRecordDat) || "RECORD_DAT".equals(param._useRecordDat)) {
                    sql = getRecordDatSql(param, recGrade._year, recGrade._semester, recGrade._grade, recGrade._hrClass);
                    // log.debug(" record_dat students sql = " + sql);
                } else {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("     T1.SCHREGNO, T1.AVG, T1." + param.getRankField("CLASS") +" AS CLASS_RANK ");
                    if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                        stb.append(" FROM RECORD_RANK_SDIV_DAT T1");
                    } else {
                        stb.append(" FROM RECORD_RANK_DAT T1");
                    }
                    stb.append(" WHERE ");
                    stb.append("     T1.YEAR = '" + recGrade._year + "' ");
                    if (StringUtils.isBlank(param._recTestKindCd)) {
                        stb.append("     AND T1.SEMESTER = '" + recGrade._semester + "' ");
                        stb.append("     AND T1.TESTKINDCD = '99' ");
                        stb.append("     AND T1.TESTITEMCD = '00' ");
                    } else if (param._recTestKindCd.length() < 4) {
                        stb.append("     AND T1.SEMESTER = '" + param._recSemester + "' ");
                        stb.append("     AND T1.TESTKINDCD = '" + param._recTestKindCd.substring(0, 2) + "' ");
                        stb.append("     AND T1.TESTITEMCD = '" + param._recTestKindCd.substring(2) + "' ");
                    } else {
                        stb.append("     AND T1.SEMESTER = '" + param._recSemester + "' ");
                        stb.append("     AND T1.TESTKINDCD = '" + param._recTestKindCd.substring(0, 2) + "' ");
                        stb.append("     AND T1.TESTITEMCD = '" + param._recTestKindCd.substring(2, 4) + "' ");
                        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
                            stb.append("    AND T1.SCORE_DIV = '" + param._recTestKindCd.substring(4) + "' ");
                        }
                    }

                    stb.append("     AND T1.SUBCLASSCD = '999999' ");

                    sql = stb.toString();
                    // log.debug(" record_rank_dat sql = " + sql);
                }

                if (null == sql) {
                    log.warn(" 成績テーブルがない");
                    return;
                }

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final Student student = recGrade.getStudent(rs.getString("SCHREGNO"));
                        if (null == student) {
                            continue;
                        }

                        final String avg = rs.getString("AVG");
                        final String avgStr = (null == avg || !NumberUtils.isNumber(avg)) ? avg : new BigDecimal(avg).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
                        student._gakkiSeiseki = avgStr;
                        if (null != student._gakkiSeiseki) {
                            student._classRank = rs.getString("CLASS_RANK");
                        }
                        // log.debug(" student = " + student + "  seiseki = " + student._gakkiSeiseki + " classRank = "+ student._classRank);
                    }
                } catch (SQLException e) {
                    log.error("RecordRank exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }

        private static void setHyoteiHeikin(final DB2UDB db2, final Collection students, final Param param) {
            log.debug(" hyotei kindai stduent count = " + students.size());
            int c = 1;

            final String sql;
            if (param._isKindai) {
                sql = psSchnoAssessKindai(param);
                // log.debug(" hyotei kindai sql = " + sql);
            } else {
                sql = psSchnoAssess(param);
                // log.debug(" hyotei sql = " + sql);
            }
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    if (param._isKindai) {
                        ps.setString(1, student._year);
                        ps.setString(2, student._recSemester);
                        ps.setString(3, student._schregno);
                        ps.setString(4, student._year);
                        ps.setString(5, student._year);
                    } else {
                        ps.setString(1, student._year);
                        ps.setString(2, student._recSemester);
                        ps.setString(3, student._schregno);
                        ps.setString(4, student._year);
                    }
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._hyoteiHeikin = rs.getString("ALL_VAL");
                    }
                    DbUtils.closeQuietly(rs);

                    c += 1;
                    if (c != 0 && c % 100 == 0) {
                        log.debug(c + " passed. ");
                    }
                }

            } catch (SQLException e) {
                log.error("setHyoteiHeikin exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         *  生徒毎の評定平均値を抽出
         *   KNJE130T.java (rev. 1.5)
         */
        private static String psSchnoAssess(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO, GRADE, HR_CLASS, ATTENDNO, ANNUAL, COURSECD, MAJORCD, COURSECODE ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR = ? AND SEMESTER = ? AND ");
            stb.append("           SCHREGNO = ? ");
            stb.append("    ) ");
            stb.append(",STUDYREC AS ( ");
            stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= ? AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            if (param.isGakuennsei()) {
                stb.append("       AND YEAR IN (SELECT DISTINCT MAX(T2.YEAR) AS YEAR");
                stb.append("       FROM SCHREG_STUDYREC_DAT T2  ");
                stb.append("       WHERE T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("       GROUP BY ANNUAL) ");
            }
            stb.append("    ) ");
            //全体の評定平均値
            stb.append(",ASSESS_ALL AS ( ");
            stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,4,1) AS VAL ");
            stb.append("           ,SCHREGNO ");
            stb.append("           ,MAX(ANNUAL) AS GRADE_MAX ");
            stb.append("    FROM   STUDYREC ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    ) ");
            //全体の評定平均値・段階
            stb.append(",ASSESS_ALL2 AS ( ");
            stb.append("    SELECT W1.SCHREGNO, W1.VAL ");
            stb.append("    FROM   ASSESS_ALL W1 ");
            stb.append("           LEFT JOIN SCHNO T1 ON T1.SCHREGNO = W1.SChREGNO ");
            stb.append("    ) ");
            //メイン
            stb.append("SELECT K1.SCHREGNO, ");
            stb.append("       K3.VAL AS ALL_VAL ");
            stb.append("FROM   SCHNO K1 ");
            stb.append("       LEFT JOIN ASSESS_ALL2 K3 ON K3.SCHREGNO = K1.SCHREGNO ");
            return stb.toString();

        } //psSchnoAssess()の括り

        /**
         *  生徒毎の評定平均値を抽出
         *   KNJE130.java (rev. 1.4) からコピー
         */
        private static String psSchnoAssessKindai(final Param param) {
            final StringBuffer stb = new StringBuffer();

            //在籍
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR = ? AND SEMESTER = ? AND ");
            stb.append("           SCHREGNO = ? ");
            stb.append("    ) ");
            stb.append(",SCHREG_STUDYREC AS ( ");
            stb.append("    SELECT T1.YEAR,SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
            stb.append("    FROM   SCHREG_STUDYREC_DAT T1 ");
            stb.append("    WHERE  T1.CLASSCD < '90' AND 0 < VALUATION AND YEAR <= ? AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
            stb.append(        "NOT EXISTS(SELECT  'X' ");
            stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append(                   "WHERE   T2.YEAR = T1.YEAR AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                           "T2.ATTEND_CLASSCD = T1.CLASSCD AND ");
                stb.append(                           "T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                stb.append(                           "T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append(                           "T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
            stb.append("    ) ");
            //留年生は、MAX年度を取得（つまり、留年した年度は対象外）
            stb.append(", T_SCHNO AS ( ");
            stb.append("    SELECT YEAR,SCHREGNO,GRADE ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR <= ? AND ");
            stb.append("           SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            stb.append("    GROUP BY YEAR,SCHREGNO,GRADE ");
            stb.append("    ) ");
            stb.append(", RYUNEN_SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,GRADE ");
            stb.append("    FROM   T_SCHNO T1 ");
            stb.append("    GROUP BY SCHREGNO,GRADE ");
            stb.append("    HAVING 1 < COUNT(*) ");
            stb.append("    ) ");
            stb.append(", RYUNEN_MAX_YEAR AS ( ");
            stb.append("    SELECT MAX(YEAR) AS YEAR,SCHREGNO,GRADE ");
            stb.append("    FROM   T_SCHNO T1 ");
            stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM RYUNEN_SCHNO) ");
            stb.append("    GROUP BY SCHREGNO,GRADE ");
            stb.append("    ) ");
            stb.append(",STUDYREC AS ( ");
            stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
            stb.append("    FROM   SCHREG_STUDYREC T1 ");
            stb.append("    WHERE  SCHREGNO NOT IN (SELECT SCHREGNO FROM RYUNEN_SCHNO) ");
            stb.append("    UNION ALL ");
            stb.append("    SELECT SCHREGNO,ANNUAL,CLASSCD,VALUATION ");
            stb.append("    FROM   SCHREG_STUDYREC T1 ");
            stb.append("    WHERE  EXISTS(SELECT  'X' ");
            stb.append("                  FROM    RYUNEN_MAX_YEAR T3 ");
            stb.append("                  WHERE   T3.YEAR = T1.YEAR AND ");
            stb.append("                          T3.SCHREGNO = T1.SCHREGNO AND ");
            stb.append("                          T3.GRADE = T1.ANNUAL) ");
            stb.append("    ) ");
            //全体の評定平均値
            stb.append(",ASSESS_ALL AS ( ");
            stb.append("    SELECT DECIMAL(ROUND(AVG(FLOAT(VALUATION)) * 10, 0) / 10, 4, 1) AS VAL ");
            stb.append("           ,SCHREGNO ");
            stb.append("    FROM   STUDYREC ");
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    ) ");

            //メイン
            stb.append("SELECT K1.SCHREGNO, ");
            stb.append("       K3.VAL AS ALL_VAL ");
            stb.append("FROM   SCHNO K1 ");
            stb.append("       LEFT JOIN ASSESS_ALL K3 ON K3.SCHREGNO = K1.SCHREGNO ");

            return stb.toString();

        }//psSchnoAssess()の括り

        /**
         * 調査書で年度(または年次)ごとに表示している欠席数の合計
         * @param db2
         * @param students 生徒リスト
         */
        private static void setAttendrecKesseki(final DB2UDB db2, final Collection students, final Param param) {
            KNJ_AttendrecSql o = new KNJ_AttendrecSql();
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(o.pre_sql(db2, param._ctrlYear));

                for (Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._attendrecKesseki = "";

                    ps.setString(1, student._schregno);
                    ps.setString(2, student._year);

                    ResultSet rs = ps.executeQuery();
                    int kesseki = 0;
                    try {
                        while (rs.next()) {
                            if (StringUtils.isNumeric(rs.getString("ATTEND_6"))) {
                                kesseki += Integer.parseInt(rs.getString("ATTEND_6"));
                            }
                       }
                    } catch (SQLException e) {
                        log.error("AttendrecSql exception!", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                    if (kesseki != 0) {
                        student._attendrecKesseki = String.valueOf(kesseki);
                    }
                }
            } catch (SQLException e) {
                log.error("AttendrecSql exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static void setAftGradCourseDat(final DB2UDB db2, final Collection students, final Param param) {
            for (final Iterator it = RecGrade.getRecGradeList(students).iterator(); it.hasNext();) {
                final RecGrade recGrade = (RecGrade) it.next();
                final String sql = getAftGradCourseDatSql(param._ctrlYear);

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final Student student = recGrade.getStudent(rs.getString("SCHREGNO"));
                        if (null == student) {
                            continue;
                        }
                        student._college1 = rs.getString("COLLEGE1");
                        if ("0".equals(rs.getString("SENKOU_KIND"))) {
                            student._faculty1 = rs.getString("FACULTY1");
                            student._department1 = rs.getString("DEPARTMENT1");
                        }
                        // log.debug(" student = " + student + "  seiseki = " + student._gakkiSeiseki + " classRank = "+ student._classRank);
                    }
                } catch (SQLException e) {
                    log.error("aftGradCourseDat exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
        }

        private static String getAftGradCourseDatSql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH AFT_GRAD_COURSE0 AS ( ");
            stb.append(" SELECT  ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT T1 ");
            stb.append("     INNER JOIN ( ");
            stb.append("         SELECT  ");
            stb.append("             YEAR, ");
            stb.append("             SCHREGNO, ");
            stb.append("             SENKOU_KIND, ");
            stb.append("             MAX(SEQ) AS SEQ ");
            stb.append("         FROM ");
            stb.append("             AFT_GRAD_COURSE_DAT T1 ");
            stb.append("         WHERE ");
            stb.append("             ((T1.SENKOU_KIND = '0' AND T1.DECISION = '1' AND T1.PLANSTAT = '1') ");
            stb.append("                 OR (T1.SENKOU_KIND = '1' AND T1.PLANSTAT = '1')) ");
            stb.append("         GROUP BY ");
            stb.append("             YEAR, SCHREGNO, SENKOU_KIND ");
            stb.append("      ) T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO AND T1.SENKOU_KIND = T2.SENKOU_KIND AND T2.SEQ = T1.SEQ ");
            stb.append(" ), AFT_GRAD_COURSE AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE0 T1 ");
            stb.append("     INNER JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             YEAR, SCHREGNO, MIN(SENKOU_KIND) AS SENKOU_KIND ");
            stb.append("         FROM ");
            stb.append("             AFT_GRAD_COURSE0 T2  ");
            stb.append("         GROUP BY ");
            stb.append("             YEAR, SCHREGNO ");
            stb.append("     ) T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO AND T2.SENKOU_KIND = T1.SENKOU_KIND ");
            stb.append(" ) SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SENKOU_KIND, ");
            stb.append("     T1.STAT_CD, ");
            stb.append("     (CASE T1.SENKOU_KIND WHEN '0' THEN T2.SCHOOL_NAME WHEN '1' THEN T3.COMPANY_NAME END) AS COLLEGE1, ");
            stb.append("     (CASE T1.SENKOU_KIND WHEN '0' THEN L3.FACULTYNAME WHEN '1' THEN '(NULL)' END) AS FACULTY1, ");
            stb.append("     (CASE T1.SENKOU_KIND WHEN '0' THEN L4.DEPARTMENTNAME WHEN '1' THEN '(NULL)' END) AS DEPARTMENT1 ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE T1 ");
            stb.append("     LEFT JOIN COLLEGE_MST T2 ON T2.SCHOOL_CD = T1.STAT_CD ");
            stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD ");
            stb.append("                                     AND L3.FACULTYCD = T1.FACULTYCD ");
            stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L4 ON L4.SCHOOL_CD = T1.STAT_CD ");
            stb.append("                                        AND L4.FACULTYCD = T1.FACULTYCD ");
            stb.append("                                        AND L4.DEPARTMENTCD = T1.DEPARTMENTCD ");
            stb.append("     LEFT JOIN COMPANY_MST T3 ON T3.COMPANY_CD = T1.STAT_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR <= '" + year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR ");
            return stb.toString();
        }

        private static String getRegdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH ");
            if (Param.PRINT_GRD.equals(param._student)) {
                stb.append(" MAX_YEAR AS ( ");
                stb.append(" SELECT ");
                stb.append("     SCHREGNO, ");
                stb.append("     MAX(YEAR) AS YEAR");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR >= '" + param._grdYear + "' ");
                stb.append(" GROUP BY ");
                stb.append("     SCHREGNO ");
                stb.append(" ), ");
                stb.append(" MAX_REGD AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.GRADE, ");
                stb.append("     T1.HR_CLASS ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN ( ");
                stb.append("     SELECT ");
                stb.append("         TT1.SCHREGNO, TT1.YEAR, MAX(TT1.SEMESTER) AS SEMESTER ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT TT1 ");
                stb.append("         INNER JOIN MAX_YEAR TT2 ON TT1.YEAR = TT2.YEAR AND TT1.SCHREGNO = TT2.SCHREGNO ");
                stb.append("     GROUP BY ");
                stb.append("         TT1.SCHREGNO, TT1.YEAR ");
                stb.append("     ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("     INNER JOIN GRD_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
                stb.append(" ) ");
                stb.append(" , ");
            }

            stb.append(" SCHNO AS ( ");
            stb.append(" SELECT ");
            stb.append("     T4.FINSCHOOLCD, ");
            stb.append("     T4.FINSCHOOL_NAME, ");
            stb.append("     T1.SCHREGNO, ");
            if (PATTERNA.equals(param._knje374Pattern)) {
                stb.append("     N1.NAME2 AS SEX, ");
                stb.append("     T2.BIRTHDAY, ");
                stb.append("     ADDR.ZIPCD, ");
                stb.append("     VALUE(ADDR.ADDR1, '') AS ADDR1, ");
                stb.append("     VALUE(ADDR.ADDR2, '') AS ADDR2, ");
                stb.append("     ADDR.TELNO, ");
                stb.append("     G1.GUARD_NAME, ");
                stb.append("     G1.GUARD_TELNO, ");
            }
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T3.HR_CLASS_NAME1, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_ENG, ");
            stb.append("     T2.GRD_DATE, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.YEAR || '-04-01' AS YEAR_SDATE, ");
            stb.append("     CAST(INT(T1.YEAR) + 1 AS CHAR(4)) || '-03-31'  AS YEAR_EDATE ");
            if(param._isNishiyama) { //京都西山は略称
            	stb.append("     , CRCM.COURSECODEABBV1 AS COURSECODENAME");
            } else {
            	stb.append("     , CRCM.COURSECODENAME ");
            }
            stb.append("     , GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            if (Param.PRINT_GRD.equals(param._student)) {
                stb.append("     INNER JOIN MAX_REGD TT1 ON ");
                stb.append("         TT1.SCHREGNO = T1.SCHREGNO AND TT1.YEAR = T1.YEAR AND TT1.SEMESTER = T1.SEMESTER ");
            }
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.GRADE = T1.GRADE ");
            stb.append("         AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append("     INNER JOIN FINSCHOOL_MST T4 ON T4.FINSCHOOLCD = T2.FINSCHOOLCD ");
            stb.append("     LEFT JOIN COURSECODE_MST CRCM ON CRCM.COURSECODE = T1.COURSECODE ");
            if (PATTERNA.equals(param._knje374Pattern)) {
                stb.append("     LEFT JOIN (SELECT ");
                stb.append("                    T1.SCHREGNO, ");
                stb.append("                    T1.ZIPCD, ");
                stb.append("                    T1.ADDR1, ");
                stb.append("                    T1.ADDR2, ");
                stb.append("                    T1.TELNO ");
                stb.append("                FROM ");
                stb.append("                    SCHREG_ADDRESS_DAT T1, ");
                stb.append("                    (SELECT ");
                stb.append("                          SCHREGNO, ");
                stb.append("                          MAX(ISSUEDATE) AS ISSUEDATE ");
                stb.append("                        FROM ");
                stb.append("                          SCHREG_ADDRESS_DAT ");
                stb.append("                        GROUP BY ");
                stb.append("                          SCHREGNO ");
                stb.append("                    ) T2 ");
                stb.append("                WHERE  ");
                stb.append("                    T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("                    AND T1.ISSUEDATE = T2.ISSUEDATE ");
                stb.append("         ) ADDR ON T1.SCHREGNO = ADDR.SCHREGNO ");
                stb.append("     LEFT JOIN GUARDIAN_DAT G1 ON T1.SCHREGNO = G1.SCHREGNO ");
                stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND T2.SEX = N1.NAMECD2 ");
            }
            if (Param.PRINT_GRD.equals(param._student)) {
                stb.append(" WHERE ");
                stb.append("     T2.GRD_DATE IS NOT NULL ");
            } else {
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._ctrlYear + "'      AND ");
                stb.append("     T1.SEMESTER = '" + param._ctrlSemester + "'      AND ");
                stb.append("     T2.GRD_DATE IS NULL ");
            }
            if ("1".equals(param._useSchool_KindField)) {
                if (Param.PRINT_GRD.equals(param._student)) {
                    stb.append("   AND GDAT.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
                }else {
                    //在校生の場合、指定の学校種別を参照
                    stb.append("   AND GDAT.SCHOOL_KIND = '" + param._schoolKind + "' ");
                }
            }
            stb.append(" ) ");
            if (!PATTERNA.equals(param._knje374Pattern)) {
                // 委員会データ
                stb.append(" , COMMITTEES AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T3.COMMITTEENAME, ");
                stb.append("     T1.CHARGENAME, ");
                stb.append("     T1.SEQ, ");
                stb.append("     ROW_NUMBER() OVER(PARTITION BY T1.SCHREGNO ORDER BY T1.SEQ) AS NUMBER ");
                stb.append("  FROM ");
                stb.append("     SCHREG_COMMITTEE_HIST_DAT T1 ");
                stb.append("     INNER JOIN SCHNO T2 ON ");
                stb.append("         T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T2.YEAR = T1.YEAR ");
                if ("1".equals(param._useSchool_KindField)) {
                    stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append("     INNER JOIN COMMITTEE_MST T3 ON ");
                stb.append("         T3.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
                stb.append("         AND T3.COMMITTEECD = T1.COMMITTEECD ");
                if ("1".equals(param._useSchool_KindField)) {
                    stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append(" ) ");
                // クラブ活動
                stb.append(" , CLUBS AS (SELECT ");
                stb.append("     T2.YEAR, ");
                stb.append("     T2.YEAR_SDATE, ");
                stb.append("     T2.YEAR_EDATE, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.CLUBCD, ");
                stb.append("     T4.CLUBNAME, ");
                stb.append("     T1.EXECUTIVECD, ");
                stb.append("     T3.NAME1 AS EXECUTIVENAME, ");
                stb.append("     T1.SDATE, ");
                stb.append("     T1.EDATE, ");
                stb.append("     ROW_NUMBER() OVER(PARTITION BY T1.SCHREGNO ORDER BY T1.CLUBCD, T1.SDATE) AS NUMBER ");
                stb.append("  FROM ");
                stb.append("     SCHREG_CLUB_HIST_DAT T1 ");
                stb.append("     INNER JOIN SCHNO T2 ON  ");
                stb.append("         T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND (T1.SDATE <= T2.YEAR_EDATE ");
                stb.append("             AND (T1.EDATE IS NULL OR T1.EDATE BETWEEN T2.YEAR_SDATE AND T2.YEAR_EDATE)) ");
                if ("1".equals(param._useSchool_KindField)) {
                    stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append("     LEFT JOIN NAME_MST T3 ON ");
                stb.append("         T3.NAMECD1 = 'J001' ");
                stb.append("         AND T3.NAMECD2 = T1.EXECUTIVECD ");
                stb.append("     INNER JOIN CLUB_MST T4 ON ");
                stb.append("         T4.CLUBCD = T1.CLUBCD ");
                if ("1".equals(param._useSchool_KindField)) {
                    stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append("  ");
                stb.append(" ) ");
            }
            stb.append(" SELECT ");
            stb.append("     T1.FINSCHOOLCD, ");
            stb.append("     T1.FINSCHOOL_NAME, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_CLASS_NAME1, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_ENG, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.COURSECODENAME, ");
            if (PATTERNA.equals(param._knje374Pattern)) {
                stb.append("     T1.SEX, ");
                stb.append("     T1.BIRTHDAY, ");
                stb.append("     T1.ZIPCD, ");
                stb.append("     T1.ADDR1, ");
                stb.append("     T1.ADDR2, ");
                stb.append("     T1.TELNO, ");
                stb.append("     T1.GUARD_NAME, ");
                stb.append("     T1.GUARD_TELNO ");
            } else {
                stb.append("     T2.COMMITTEENAME AS COMMITTEENAME1, ");
                stb.append("     T2.CHARGENAME AS CHARGENAME1, ");
                stb.append("     T3.COMMITTEENAME AS COMMITTEENAME2, ");
                stb.append("     T3.CHARGENAME AS CHARGENAME2, ");
                stb.append("     T4.CLUBNAME AS CLUBNAME1, ");
                stb.append("     T4.EXECUTIVENAME AS EXECUTIVENAME1, ");
                stb.append("     T5.CLUBNAME AS CLUBNAME2, ");
                stb.append("     T5.EXECUTIVENAME AS EXECUTIVENAME2 ");
            }
            stb.append(" FROM ");
            stb.append("     SCHNO T1 ");
            if (!PATTERNA.equals(param._knje374Pattern)) {
                stb.append("     LEFT JOIN COMMITTEES T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.NUMBER = 1 ");
                stb.append("     LEFT JOIN COMMITTEES T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.NUMBER = 2 ");
                stb.append("     LEFT JOIN CLUBS T4 ON T4.SCHREGNO = T1.SCHREGNO AND T4.NUMBER = 1 ");
                stb.append("     LEFT JOIN CLUBS T5 ON T5.SCHREGNO = T1.SCHREGNO AND T5.NUMBER = 2 ");
            }
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            if (null != param._categorySelected) {
                stb.append(" WHERE ");
                stb.append("         T1.FINSCHOOLCD IN " +  param.sqlWhereInFinSchoolCds());
            }
            stb.append(" ORDER BY ");
            if (OUTPUT2.equals(param._output)) {
                stb.append("     T1.FINSCHOOLCD, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO ");
            } else if (Param.PRINT_GRD.equals(param._student)) {
                stb.append("     T1.FINSCHOOLCD, T1.YEAR, T1.SCHREGNO ");
            } else {
                stb.append("     T1.FINSCHOOLCD, T1.SCHREGNO ");
            }
            return stb.toString();
        }
    }

    private static class Finschool {
        final String _finschoolCd;
        final String _finschoolName;
        final List _students;

        public Finschool(final String finschoolCd, final String finschoolName) {
            _finschoolCd = finschoolCd;
            _finschoolName = finschoolName;
            _students = new ArrayList();
        }

        public String toString() {
            return _finschoolCd + " : " + _finschoolName;
        }
    }

    private static class RecGrade {
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final List _students;
        final Map _studentMap;

        public RecGrade(final String year, final String semester, final String grade, final String hrClass) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _students = new ArrayList();
            _studentMap = new HashMap();
        }

        public Student getStudent(final String schregno) {
            return (Student) _studentMap.get(schregno);
        }

        public void createMap() {
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                _studentMap.put(student._schregno, student);
            }
        }

        public String toString() {
            return _year + " : " + _semester + " : " + _grade + ": " + _hrClass + " (count = " + _students.size() + ")";
        }

        private static List getRecGradeList(final Collection students) {
            final List gradeList = new ArrayList();
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                RecGrade recGrade = null;
                for (final Iterator itg = gradeList.iterator(); itg.hasNext();) {
                    final RecGrade recGrade1 = (RecGrade) itg.next();
                    if (recGrade1._year.equals(student._year) && recGrade1._semester.equals(student._recSemester) && recGrade1._grade.equals(student._grade) && recGrade1._hrClass.equals(student._hrClass)) {
                        recGrade = recGrade1;
                    }
                }

                if (null == recGrade) {
                    recGrade = new RecGrade(student._year, student._recSemester, student._grade, student._hrClass);
                    gradeList.add(recGrade);
                }
                recGrade._students.add(student);
            }
            for (final Iterator it = gradeList.iterator(); it.hasNext();) {
                final RecGrade recGrade = (RecGrade) it.next();
                recGrade.createMap();
            }
            return gradeList;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 68494 $ $Date: 2019-07-04 17:06:19 +0900 (木, 04 7 2019) $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private static final String SEM_ALL = "9";

        private static final String PRINT_GRD = "2";

        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _student;             // 1:在校生 2:卒業生
        final String _recSemester;         // 学期・学年末成績の学期
        final String _recTestKindCd;       // 学期・学年末成績のテスト種別
        final String _schoolKind;          // STUDENT=1のみ学校種別
        final String _grdYear;             // STUDENT=2のみ卒業年度「〜年度以降」
        final String[] _categorySelected;  // 選択出身学校
        final String _useRecordDat;
        final String _standard;            // 1:総合点 2:平均点 3:偏差値
        final String _output;              // 1:クラブ名・委員会名 2:科目別成績
        final String _attendSdate;         // OUTPUT=2のみ出欠開始日付
        final String _attendEdate;         // OUTPUT=2のみ出欠終了日付
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _useTestCountflg;
        final Map _attendParamMap;

        final String _useCurriculumcd;
        final String _knje374Pattern;
        final String _staffCd;
        final StaffInfo _staffInfo;

        private KNJSchoolMst _knjSchoolMst;
        private boolean _isKindai;
        private boolean _isNishiyama;
        private String _recSemesterName;
        private String _recTestkindName;

        public Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _student = request.getParameter("STUDENT");
            _recSemester = request.getParameter("REC_SEMESTER");
            _recTestKindCd = request.getParameter("TESTKINDCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _grdYear = request.getParameter("GRD_YEAR");
            _categorySelected = request.getParameterValues("category_selected");
            _useRecordDat = request.getParameter("useRecordDat");
            _standard = request.getParameter("STANDARD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _knje374Pattern = request.getParameter("knje374Pattern");
            _output = request.getParameter("OUTPUT");
            final String paramAttendSdate = request.getParameter("ATTEND_SDATE");
            final String paramAttendEdate = request.getParameter("ATTEND_EDATE");
            _attendSdate = StringUtils.isBlank(paramAttendSdate) ? paramAttendSdate : paramAttendSdate.replace('/', '-');
            _attendEdate = StringUtils.isBlank(paramAttendEdate) ? paramAttendEdate : paramAttendEdate.replace('/', '-');
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _staffInfo = new StaffInfo(db2, _staffCd);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.error(" KNJSchoolMst const exception!", e);
            }
            setNameMstZ010(db2);
            setRecSemesterName(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }

        public String getDateRange() {
            final StringBuffer stb = new StringBuffer();
            stb.append("（");
            final String FROM_TO_MARK = "\uFF5E";
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(Date.valueOf(_attendSdate));
                stb.append(cal.get(Calendar.MONTH) + 1).append("月").append(cal.get(Calendar.DAY_OF_MONTH)).append("日");
            } catch (Exception e) {
            }
            stb.append(FROM_TO_MARK);
            try {
                cal.setTime(Date.valueOf(_attendEdate));
                stb.append(cal.get(Calendar.MONTH) + 1).append("月").append(cal.get(Calendar.DAY_OF_MONTH)).append("日");
            } catch (Exception e) {
            }
            stb.append("）");
            return stb.toString();
        }

        private String sqlWhereInFinSchoolCds() {
            final StringBuffer stb = new StringBuffer("(");
            String comma = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                stb.append(comma);
                stb.append("'").append(_categorySelected[i]).append("'");
                comma = ",";
            }
            stb.append(")");
            return stb.toString();
        }

        private void setNameMstZ010(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    _isKindai = "KINDAI".equals(rs.getString("NAME1")) || "KINJUNIOR".equals(rs.getString("NAME1"));
                    _isNishiyama = "nishiyama".equals(rs.getString("NAME1"));
                }
            } catch (SQLException e) {
                log.error(" NAME_MST Z010 exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (_isKindai) {
                if ("0101".equals(_recTestKindCd)) {
                    _recTestkindName = "中間成績";
                } else if ("0201".equals(_recTestKindCd)) {
                    _recTestkindName = "期末成績";
                } else {
                    _recTestkindName = "評価成績";
                }
            } else {
                if (StringUtils.isBlank(_recTestKindCd)) {
                    _recTestkindName = "成績";
                } else {
                    try {
                        final String testYear = Param.PRINT_GRD.equals(_student) ? _grdYear : _ctrlYear;
                        final String sql;
                        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                            sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + testYear + "' AND SEMESTER = '" + _recSemester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _recTestKindCd + "' ";
                        } else {
                            sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + testYear + "' AND SEMESTER = '" + _recSemester + "' AND TESTKINDCD || TESTITEMCD = '" + _recTestKindCd + "' ";
                        }
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            _recTestkindName = rs.getString("TESTITEMNAME");
                        }
                    } catch (SQLException e) {
                        log.error(" TESTITEMNAME exception!", e);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                }
            }
        }

        private void setRecSemesterName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _recSemester + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    _recSemesterName = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
                }
            } catch (SQLException e) {
                log.error(" NAME_MST Z010 exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getRankField(final String head) {
            final String rankField;
            if ("2".equals(_standard)) {
                rankField = head + "_AVG_RANK";
            } else if ("3".equals(_standard)){
                rankField = head + "_DEVIATION_RANK";
            } else {
                rankField = head + "_RANK";
            }
            return rankField;
        }

        private boolean isGakuennsei() {
            return "0".equals(_knjSchoolMst._schoolDiv);
        }
    }
}

// eof

