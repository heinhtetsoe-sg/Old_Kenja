// kanji=漢字
/*
 * $Id: bf0ae9b5b35069acb102537e85a1c1e52e28e49f $
 *
 * 作成日: 2009/10/28 22:58:15 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: bf0ae9b5b35069acb102537e85a1c1e52e28e49f $
 */
public class KNJH112 {

    private static final Log log = LogFactory.getLog("KNJH112.class");

    private static final String FORMID = "KNJH112.frm";
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

        String befGradeHr = "";
        for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            final String gradeHr = student._grade + student._hrClass;
            if (!befGradeHr.equals(gradeHr)) {
                setHead(svf, student);
            }
            svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)));
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("NAME", student._name);
            if (student._qualifiedList.size() > 0) {
                for (final Iterator itQualified = student._qualifiedList.iterator(); itQualified.hasNext();) {
                    svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)));
                    svf.VrsOut("SCHREGNO", student._schregno);
                    svf.VrsOut("NAME", student._name);
                    final QualifiedDat qualifiedDat = (QualifiedDat) itQualified.next();
                    svf.VrsOut("REGDDATE", KNJ_EditDate.h_format_JP(qualifiedDat._regddate));
                    svf.VrsOut("CONDITION_DIV", "1".equals(qualifiedDat._conditionDiv) ? "資格" : "その他");
                    svf.VrsOut("CONTENTS", qualifiedDat._contents);
                    svf.VrsOut("REMARK", qualifiedDat._remark);
                    svf.VrEndRecord();
                }
            } else {
                svf.VrEndRecord();
            }
            befGradeHr = gradeHr;
            _hasData = true;
        }
    }

    private void setHead(final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORMID, 4);
        svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("HR_NAME", student._hrName);
    }

    private List getPrintStudent(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement psSt = null;
        ResultSet rsSt = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            // 01001(年組) OR 20051015-02001004(学籍-年組番)
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
                    final String name = rsSt.getString("NAME");
                    final Student student = new Student(schregno,
                                                        grade,
                                                        hrClass,
                                                        hrName,
                                                        attendno,
                                                        name);
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
        stb.append("     BASE.NAME ");
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
        final String _name;
        final List _qualifiedList;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _name = name;
            _qualifiedList = new ArrayList();
        }

        public void setQualifiedList(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT * FROM SCHREG_QUALIFIED_HOBBY_DAT WHERE SCHREGNO = '" + _schregno + "' ORDER BY REGDDATE, SEQ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
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
                    final String conditionDiv = rs.getString("CONDITION_DIV");
                    final String contents = rs.getString("CONTENTS");
                    final String remark = rs.getString("REMARK");
                    final String credits = rs.getString("CREDITS");
                    final QualifiedDat qualifiedDat = new QualifiedDat(
                                                            year,
                                                            schregno,
                                                            seq,
                                                            regddate,
                                                            subclasscd,
                                                            conditionDiv,
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
        final String _subclasscd;
        final String _conditionDiv;
        final String _contents;
        final String _remark;
        final String _credits;

        public QualifiedDat(
                final String year,
                final String schregno,
                final String seq,
                final String regddate,
                final String subclasscd,
                final String conditionDiv,
                final String contents,
                final String remark,
                final String credits
        ) {
            _year = year;
            _schregno = schregno;
            _seq = seq;
            _regddate = regddate;
            _subclasscd = subclasscd;
            _conditionDiv = conditionDiv;
            _contents =contents;
            _remark = remark;
            _credits = credits;
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

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

    }
}

// eof
