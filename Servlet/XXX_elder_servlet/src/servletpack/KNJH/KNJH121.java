// kanji=漢字
/*
 * $Id: 0111900dfa32d6f500c83616f2db7270d190b780 $
 *
 * 作成日: 2009/10/28 22:58:15 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [生徒指導情報システム]
 *
 *                  ＜ＫＮＪＨ１１２Ａ＞ 資格取得一覧表
 *
 * @author m-yama
 * @version $Id: 0111900dfa32d6f500c83616f2db7270d190b780 $
 */
public class KNJH121 {

    private static final Log log = LogFactory.getLog("KNJH121.class");

    private static final String FORMID_HR = "KNJH121_1.frm";
    private static final String FORMID_STD = "KNJH121_2.frm";
    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printStudents = getPrintStudent(db2);
        if (_param._seitoBetu) {
            printSeitoBetu(svf, printStudents);
        } else {
            printHrBetu(svf, printStudents);
        }
    }

    private void printHrBetu(final Vrw32alp svf, final List printStudents) {
        String befGradeHr = "";
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            final String gradeHr = student._grade + student._hrClass;
            if (!befGradeHr.equals(gradeHr)) {
                setHeadHr(svf, student);
            }
            if (student._qualifiedList.size() > 0) {
                for (final Iterator itQualified = student._qualifiedList.iterator(); itQualified.hasNext();) {
                    svf.VrsOut("HR_NAME", student._hrNameAbbv + "-" + String.valueOf(Integer.parseInt(student._attendno)));
                    svf.VrsOut("NAME", student._name);
                    svf.VrsOut("SEX", student._sex);
                    final QualifiedDat qualifiedDat = (QualifiedDat) itQualified.next();
                    svf.VrsOut("CONDITION_DIV", qualifiedDat._settei);
                    svf.VrsOut("CONTENTS", qualifiedDat._contents);
                    svf.VrsOut("REGDDATE", KNJ_EditDate.h_format_JP(qualifiedDat._regddate));
                    svf.VrsOut("AUTH_CLASS", qualifiedDat._className);
                    final String subField = getMS932ByteLength(qualifiedDat._subclassName) > 20 ? "2" : "1";
                    svf.VrsOut("AUTH_SUBCLASS" + subField, qualifiedDat._subclassName);
                    svf.VrsOut("AUTH_METHOD", "");
                    svf.VrsOut("CREDIT", qualifiedDat._credits);
                    svf.VrsOut("GRAD_CREDIT", "");
                    svf.VrsOut("AUTH_DATE", "");
                    svf.VrsOut("REMARK", qualifiedDat._remark);

                    svf.VrEndRecord();
                }
                _hasData = true;
            }
            befGradeHr = gradeHr;
        }
    }

    private void setHeadHr(final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORMID_HR, 4);
        svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　学校外学修一覧表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("HR_NAME", student._hrName);
    }

    private void printSeitoBetu(final Vrw32alp svf, final List printStudents) {
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            setHeadStd(svf, student);
            if (student._qualifiedList.size() > 0) {
                for (final Iterator itQualified = student._qualifiedList.iterator(); itQualified.hasNext();) {
                    final QualifiedDat qualifiedDat = (QualifiedDat) itQualified.next();
                    svf.VrsOut("CONDITION_DIV", qualifiedDat._settei);
                    svf.VrsOut("CONTENTS", qualifiedDat._contents);
                    svf.VrsOut("REGDDATE", KNJ_EditDate.h_format_JP(qualifiedDat._regddate));
                    svf.VrsOut("AUTH_CLASS", qualifiedDat._className);
                    final String subField = getMS932ByteLength(qualifiedDat._subclassName) > 20 ? "2" : "1";
                    svf.VrsOut("AUTH_SUBCLASS" + subField, qualifiedDat._subclassName);
                    svf.VrsOut("AUTH_METHOD", "");
                    svf.VrsOut("CREDIT", qualifiedDat._credits);
                    svf.VrsOut("GRAD_CREDIT", "");
                    svf.VrsOut("AUTH_DATE", "");
                    svf.VrsOut("REMARK", qualifiedDat._remark);
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }

    private void setHeadStd(final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORMID_STD, 4);
        svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　生徒別学校外学修一覧表");
        svf.VrsOut("NAME", student._hrName + " " + String.valueOf(Integer.parseInt(student._attendno)) + "番　" + student._name + "(" + student._majorName + student._coursecodeName + ")");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private List getPrintStudent(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement psSt = null;
        ResultSet rsSt = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            final String classSelected = _param._classSelected[i];
            final String studentSql = getStudentSql(classSelected);
            try {
                psSt = db2.prepareStatement(studentSql);
                rsSt = psSt.executeQuery();
                while (rsSt.next()) {
                    final String schregno = rsSt.getString("SCHREGNO");
                    final String grade = rsSt.getString("GRADE");
                    final String hrClass = rsSt.getString("HR_CLASS");
                    final String hrName = rsSt.getString("HR_NAME");
                    final String hrNameAbbv = rsSt.getString("HR_NAMEABBV");
                    final String attendno = rsSt.getString("ATTENDNO");
                    final String courseName = rsSt.getString("COURSENAME");
                    final String majorName = rsSt.getString("MAJORNAME");
                    final String coursecodeName = rsSt.getString("COURSECODENAME");
                    final String name = rsSt.getString("NAME");
                    final String sex = rsSt.getString("SEX");
                    final Student student = new Student(schregno,
                                                        grade,
                                                        hrClass,
                                                        hrName,
                                                        hrNameAbbv,
                                                        attendno,
                                                        courseName,
                                                        majorName,
                                                        coursecodeName,
                                                        name,
                                                        sex);
                    student.setQualifiedList(db2);
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, psSt, rsSt);
                db2.commit();
            }
        }
        return rtnList;
    }

    private String getStudentSql(final String selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     HR.HR_NAME, ");
        stb.append("     HR.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     COURSE.COURSENAME, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     COURSEC.COURSECODENAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     N1.NAME2 AS SEX ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ");
        stb.append("          AND T1.SEMESTER = HR.SEMESTER ");
        stb.append("          AND T1.GRADE = HR.GRADE ");
        stb.append("          AND T1.HR_CLASS = HR.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = N1.NAMECD2 ");
        stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
        stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
        stb.append("          AND T1.MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendno;
        final String _courseName;
        final String _majorName;
        final String _coursecodeName;
        final String _name;
        final String _sex;
        final List _qualifiedList;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String attendno,
                final String courseName,
                final String majorName,
                final String coursecodeName,
                final String name,
                final String sex
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendno = attendno;
            _courseName = courseName;
            _majorName = majorName;
            _coursecodeName = coursecodeName;
            _name = name;
            _sex = sex;
            _qualifiedList = new ArrayList();
        }

        public void setQualifiedList(final DB2UDB db2) throws SQLException {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("   T1.*, ");
            sql.append("   CASE WHEN T1.CONDITION_DIV = '2' ");
            sql.append("        THEN '学校外認定' ");
            sql.append("        ELSE '高卒認定' ");
            sql.append("   END AS SETTEI, ");
            sql.append("   CASE WHEN T1.CONDITION_DIV = '2' ");
            sql.append("        THEN NM305.NAME1 ");
            sql.append("        ELSE T1.CONTENTS ");
            sql.append("   END AS CONTENTS, ");
            sql.append("   CLASS_M.CLASSNAME, ");
            sql.append("   SUBCLASS_M.SUBCLASSNAME ");
            sql.append(" FROM ");
            sql.append("   SCHREG_QUALIFIED_DAT T1 ");
            sql.append("   LEFT JOIN NAME_MST NM305 ON NM305.NAMECD1 = 'H305' ");
            sql.append("        AND NM305.NAMECD2 = T1.CONTENTS ");
            sql.append("   LEFT JOIN CLASS_MST CLASS_M ON CLASS_M.CLASSCD = T1.CLASSCD ");
            sql.append("        AND CLASS_M.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("   LEFT JOIN SUBCLASS_MST SUBCLASS_M ON SUBCLASS_M.CLASSCD = T1.CLASSCD ");
            sql.append("        AND SUBCLASS_M.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("        AND SUBCLASS_M.CURRICULUM_CD = T1.CURRICULUM_CD ");
            sql.append("        AND SUBCLASS_M.SUBCLASSCD = T1.SUBCLASSCD ");
            sql.append(" WHERE ");
            sql.append("   T1.SCHREGNO = '" + _schregno + "' ");
            sql.append("   AND T1.CONDITION_DIV IN ('2', '3') ");
            sql.append(" ORDER BY ");
            sql.append("   T1.REGDDATE, T1.SEQ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String schregno = rs.getString("SCHREGNO");
                    final String seq = rs.getString("SEQ");
                    final String regddate = rs.getString("REGDDATE");
                    final String settei = rs.getString("SETTEI");
                    final String className = rs.getString("CLASSNAME");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String contents = rs.getString("CONTENTS");
                    final String remark = rs.getString("REMARK");
                    final String credits = rs.getString("CREDITS");
                    final QualifiedDat qualifiedDat = new QualifiedDat(
                                                            year,
                                                            schregno,
                                                            seq,
                                                            regddate,
                                                            settei,
                                                            className,
                                                            subclassName,
                                                            contents,
                                                            remark,
                                                            credits);
                    _qualifiedList.add(qualifiedDat);
                }
            } finally {
                db2.commit();
            }
        }
    }

    public class QualifiedDat {
        final String _year;
        final String _schregno;
        final String _seq;
        final String _regddate;
        final String _settei;
        final String _className;
        final String _subclassName;
        final String _contents;
        final String _remark;
        final String _credits;

        public QualifiedDat(
                final String year,
                final String schregno,
                final String seq,
                final String regddate,
                final String settei,
                final String className,
                final String subclassName,
                final String contents,
                final String remark,
                final String credits
        ) {
            _year = year;
            _schregno = schregno;
            _seq = seq;
            _regddate = regddate;
            _settei = settei;
            _className = className;
            _subclassName = subclassName;
            _contents =contents;
            _remark = remark;
            _credits = credits;
        }
    }

    public class QualifiedMst {
        final String _qualifiedCd;
        final String _conditionDiv;
        final String _qualifiedName;
        final String _promoter;

        public QualifiedMst(
                final String qualifiedCd,
                final String conditionDiv,
                final String qualifiedName,
                final String promoter) {
            _qualifiedCd = qualifiedCd;
            _conditionDiv = conditionDiv;
            _qualifiedName = qualifiedName;
            _promoter = promoter;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String[] _classSelected;
        private final String _useCurriculumcd;
        private final boolean _seitoBetu;
        private Map _qualifiedMstMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _seitoBetu = "1".equals(request.getParameter("SEITO_BETU"));
        }

    }
}

// eof
