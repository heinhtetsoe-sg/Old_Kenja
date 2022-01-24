// kanji=漢字
/*
 * $Id: 2972ea94fc673dc06a83375fe5d1e536801b9c99 $
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
import java.util.HashMap;
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
 * @version $Id: 2972ea94fc673dc06a83375fe5d1e536801b9c99 $
 */
public class KNJH112A {

    private static final Log log = LogFactory.getLog("KNJH112A.class");

    private static final String FORMID_HR = "KNJH112A.frm";
    private static final String FORMID_STD = "KNJH112A_2.frm";
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
            printSeitoBetu(db2, svf, printStudents);
        } else {
            printHrBetu(db2, svf, printStudents);
        }
    }

    private void printHrBetu(final DB2UDB db2, final Vrw32alp svf, final List printStudents) {
        String befGradeHr = "";
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            final String gradeHr = student._grade + student._hrClass;
            if (!befGradeHr.equals(gradeHr)) {
                setHeadHr(db2, svf, student);
            }
            if (student._qualifiedList.size() > 0) {
                for (final Iterator itQualified = student._qualifiedList.iterator(); itQualified.hasNext();) {
                    svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)));
                    svf.VrsOut("NAME", student._name);
                    svf.VrsOut("SEX", student._sex);
                    final QualifiedDat qualifiedDat = (QualifiedDat) itQualified.next();
                    svf.VrsOut("REGDDATE", KNJ_EditDate.h_format_JP(db2, qualifiedDat._regddate));

                    svf.VrsOut("RANK", qualifiedDat._rank);
                    svf.VrsOut("SCORE", qualifiedDat._score);
                    final String receiptstr = "1".equals(qualifiedDat._certificate) ? "済" : "";
                    svf.VrsOut("RECEIPT", receiptstr);

                    if (qualifiedDat._qualifiedMst != null) {
                        svf.VrsOut("CONTENTS", qualifiedDat._qualifiedMst._qualifiedName);
                        svf.VrsOut("CONDITION_DIV", qualifiedDat._qualifiedMst._conditionDiv);
                        svf.VrsOut("ORGANIZER", qualifiedDat._qualifiedMst._promoter);
                    }
                    svf.VrsOut("REMARK", qualifiedDat._remark);
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
            befGradeHr = gradeHr;
        }
    }

    private void setHeadHr(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORMID_HR, 4);
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        svf.VrsOut("HR_NAME", student._hrName);
        if (_param._sDate != null && _param._eDate != null) {
            svf.VrsOut("PERIOD", KNJ_EditDate.h_format_JP_MD(_param._sDate) + "〜" + KNJ_EditDate.h_format_JP_MD(_param._eDate));
        }
    }

    private void printSeitoBetu(final DB2UDB db2, final Vrw32alp svf, final List printStudents) {
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            setHeadStd(db2, svf, student);
            if (student._qualifiedList.size() > 0) {
                for (final Iterator itQualified = student._qualifiedList.iterator(); itQualified.hasNext();) {
                    final QualifiedDat qualifiedDat = (QualifiedDat) itQualified.next();
                    svf.VrsOut("REGDDATE", KNJ_EditDate.h_format_JP(db2, qualifiedDat._regddate));
                    svf.VrsOut("RANK", qualifiedDat._rank);
                    final String receiptstr = "1".equals(qualifiedDat._certificate) ? "済" : "";
                    svf.VrsOut("RECEIPT", receiptstr);

                    if (qualifiedDat._qualifiedMst != null) {
                        svf.VrsOut("CONTENTS", qualifiedDat._qualifiedMst._qualifiedName);
                        svf.VrsOut("CONDITION_DIV", qualifiedDat._qualifiedMst._conditionDiv);
                        svf.VrsOut("ORGANIZER", qualifiedDat._qualifiedMst._promoter);
                    }
                    svf.VrsOut("REMARK", qualifiedDat._remark);
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }

    private void setHeadStd(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORMID_STD, 4);
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　生徒別資格（検定）取得一覧表");
        svf.VrsOut("NAME", student._hrName + " " + String.valueOf(Integer.parseInt(student._attendno)) + "番　" + student._name + "(" + student._majorName + student._coursecodeName + ")");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        if (_param._sDate != null && _param._eDate != null) {
            svf.VrsOut("PERIOD", KNJ_EditDate.h_format_JP_MD(_param._sDate) + "〜" + KNJ_EditDate.h_format_JP_MD(_param._eDate));
        }
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
            sql.append("   NMH312.NAME1 AS RANK_NAME");
            sql.append(" FROM ");
            sql.append("   SCHREG_QUALIFIED_HOBBY_DAT T1 ");
            sql.append("   LEFT JOIN NAME_MST NMH312 ON NMH312.NAMECD1 = 'H312' AND NMH312.NAMECD2 = RANK ");
            sql.append(" WHERE ");
            sql.append("   T1.SCHREGNO = '" + _schregno + "' ");
            if (_param._sDate != null && _param._eDate != null) {
                sql.append("   AND T1.REGDDATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
            }
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
                    final String subclasscd;
                    if ("1".equals(_param._useCurriculumcd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    final String contents = rs.getString("CONTENTS");
                    final String remark = rs.getString("REMARK");
                    final String credits = rs.getString("CREDITS");
                    final String rank = rs.getString("RANK_NAME");
                    final String score = rs.getString("SCORE");
                    final String certificate = rs.getString("CERTIFICATE");
                    final QualifiedDat qualifiedDat = new QualifiedDat(
                                                            year,
                                                            schregno,
                                                            seq,
                                                            regddate,
                                                            subclasscd,
                                                            contents,
                                                            remark,
                                                            credits,
                                                            rank,
                                                            score,
                                                            certificate,
                                                            _param.getQualifiedMst(rs.getString("QUALIFIED_CD")));
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
        final String _subclasscd;
        final String _contents;
        final String _remark;
        final String _credits;
        final String _rank;
        final String _score;
        final String _certificate;
        final QualifiedMst _qualifiedMst;

        public QualifiedDat(
                final String year,
                final String schregno,
                final String seq,
                final String regddate,
                final String subclasscd,
                final String contents,
                final String remark,
                final String credits,
                final String rank,
                final String score,
                final String certificate,
                final QualifiedMst qualifiedMst
        ) {
            _year = year;
            _schregno = schregno;
            _seq = seq;
            _regddate = regddate;
            _subclasscd = subclasscd;
            _contents =contents;
            _remark = remark;
            _credits = credits;
            _rank = rank;
            _score = score;
            _certificate = certificate;
            _qualifiedMst = qualifiedMst;
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
        log.fatal("$Revision: 74911 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String[] _classSelected;
        private final String _sDate;
        private final String _eDate;
        private final String _useCurriculumcd;
        private final boolean _seitoBetu;
        private Map _qualifiedMstMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            final String sdate = request.getParameter("SDATE");
            final String edate = request.getParameter("EDATE");
            _sDate = (sdate == null) ? null : sdate.replace('/', '-');
            _eDate = (edate == null) ? null : edate.replace('/', '-');
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _seitoBetu = "1".equals(request.getParameter("SEITO_BETU"));
            loadQualifiedMst(db2);
        }

        private void loadQualifiedMst(DB2UDB db2) throws SQLException {
            final String sql =
                " SELECT T1.*, NMH311.NAME1 AS CONDITION_NAME FROM QUALIFIED_MST T1"
                + " LEFT JOIN NAME_MST NMH311 ON NMH311.NAMECD1 = 'H311' AND NMH311.NAMECD2 = CONDITION_DIV ";

            _qualifiedMstMap = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String qualifiedCd = rs.getString("QUALIFIED_CD");
                    final String conditionDiv = rs.getString("CONDITION_NAME");
                    final String qualifiedName = rs.getString("QUALIFIED_NAME");
                    final String promoter = rs.getString("PROMOTER");

                    QualifiedMst qualifiedMst = new QualifiedMst(qualifiedCd, conditionDiv, qualifiedName, promoter);

                    _qualifiedMstMap.put(qualifiedCd, qualifiedMst);
                }
            } finally {
                db2.commit();
            }
        }

        public QualifiedMst getQualifiedMst(String licenseCd) {
            return (QualifiedMst) _qualifiedMstMap.get(licenseCd);
        }

    }
}

// eof
