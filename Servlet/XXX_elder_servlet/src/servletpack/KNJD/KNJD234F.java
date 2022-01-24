/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 6159e988cb3dc6705f6c02796d98316c6ac8ec6f $
 *
 * 作成日: 2020/07/07
 * 作成者: nakamoto
 *
 * Copyright(C) 2019-2021 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD234F {

    private static final Log log = LogFactory.getLog(KNJD234F.class);

    private boolean _hasData;

    private final String TESTCD = "990008";
    private final int MAX_LINE = 45;
    private final int MAX_COL = 64;

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

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List studentList = getStudentList(db2);
        final List printSubclassList = getSubclassList(db2);

        final List pageStudentList = new ArrayList();
        List tmpStudentList = new ArrayList();
        int cnt = 0;
        for (Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (cnt % MAX_LINE == 0) {
                tmpStudentList = new ArrayList();
                pageStudentList.add(tmpStudentList);
            }
            tmpStudentList.add(student);
            cnt++;
        }

        for (Iterator itp = pageStudentList.iterator(); itp.hasNext();) {
            final List printStudentList = (List) itp.next();

            svf.VrSetForm("KNJD234F.frm", 4);
            svf.VrsOut("year2", _param._ctrlYear + "年度");
            svf.VrsOut("TITLE", _param._gradeName + "　" + _param._semesterName + "期末評価一覧" + _param._outputName);
            svf.VrsOut("ymd1", _param._printDate);

            int stdCnt = 1;
            for (Iterator itStudent = printStudentList.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                
                final String hrClass1 = student._hrClass.substring(student._hrClass.length() - 1);
                final String attendno2 = student._attendno.substring(student._attendno.length() - 2);
                svf.VrsOutn("NUMBER", stdCnt, hrClass1 + attendno2);
                svf.VrsOutn("name1", stdCnt, student._name);
                
                stdCnt++;
            }
            
            final Map printClassCdMap = new HashMap<String, String>();
            for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                final SubclassMst subclass = (SubclassMst) it.next();
                int classAbbvLen = KNJ_EditEdit.getMS932ByteLength(subclass._classAbbv);
                final String classField = classAbbvLen > 6 ? "3" : classAbbvLen > 4 ? "2" : "1";
                svf.VrsOut("GRPCD", subclass._classcd);
                if (!printClassCdMap.containsKey(subclass._classcd)) {
                    svf.VrsOut("course" + classField, subclass._classAbbv);
                }
                if (subclass._subclassname.length() > 9) {
                    final String[] split = KNJ_EditEdit.splitByLength(subclass._subclassname, 10);
                    final String[] subNameField = {"SUBCLASS_1", "SUBCLASS_2"};
                    for (int subNameCnt = 0; subNameCnt < Math.min(split.length, subNameField.length); subNameCnt++) {
                        svf.VrsOut(subNameField[subNameCnt], split[subNameCnt]);
                    }
                } else {
                    svf.VrsOut("SUBCLASS", subclass._subclassname);
                }
                int gyo = 1;
                for (Iterator itstudent = printStudentList.iterator(); itstudent.hasNext();) {
                    final Student student = (Student) itstudent.next();
                    if (student._recordRank.containsKey(subclass._joinSubclassCd)) {
                        final RecordRank recordRank = (RecordRank) student._recordRank.get(subclass._joinSubclassCd);
                        svf.VrsOut("SCORE" + gyo, "*".equals(recordRank._valueDi) ? recordRank._valueDi : recordRank._score);
                    }
                    gyo++;
                }
                svf.VrEndRecord();
                printClassCdMap.put(subclass._classcd, "1");
            }
            for (int i = printSubclassList.size(); i < MAX_COL; i++) {
                svf.VrsOut("GRPCD", "a");
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        final Map schMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql("STU");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");

                final Student student;
                if (schMap.containsKey(schregno)) {
                    student = (Student) schMap.get(schregno);
                } else {
                    student = new Student(schregno, hrClass, attendno, name);
                    retList.add(student);
                    schMap.put(schregno, student);
                }

                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String joinSubclassCd = classcd + schoolKind + curriculumCd + subclassCd;
                final String score = rs.getString("SCORE");
                final String valueDi = rs.getString("VALUE_DI");

                final RecordRank recordRank = new RecordRank(joinSubclassCd, score, valueDi);
                student._recordRank.put(joinSubclassCd, recordRank);
            }

        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private List getSubclassList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql("SUB");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String joinSubclassCd = classcd + schoolKind + curriculumCd + subclassCd;
                final String classAbbv = rs.getString("CLASSABBV");
                final String subclassname = rs.getString("SUBCLASSNAME");

                final SubclassMst subclassMst = new SubclassMst(classcd, schoolKind, curriculumCd, subclassCd, joinSubclassCd, classAbbv, subclassname);
                retList.add(subclassMst);
            }
        } catch (SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql(final String dataDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         BASE.NAME, ");
        stb.append("         REGH.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT REGH ON REGH.YEAR = REGD.YEAR ");
        stb.append("             AND REGH.SEMESTER = REGD.SEMESTER ");
        stb.append("             AND REGH.GRADE = REGD.GRADE ");
        stb.append("             AND REGH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND REGD.SEMESTER = '" + _param._regdSeme + "' ");
        stb.append("         AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("         AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" ), RECORD AS ( ");
        stb.append("     SELECT ");
        stb.append("         R1.CLASSCD, ");
        stb.append("         R1.SCHOOL_KIND, ");
        stb.append("         R1.CURRICULUM_CD, ");
        stb.append("         R1.SUBCLASSCD, ");
        stb.append("         R1.SCHREGNO, ");
        stb.append("         R1.SCORE, ");
        stb.append("         NULL AS VALUE_DI ");
        stb.append("     FROM ");
        stb.append("         RECORD_RANK_SDIV_DAT R1 ");
        stb.append("     WHERE ");
        stb.append("         R1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND R1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '" + TESTCD + "' ");
        stb.append("         AND R1.SUBCLASSCD NOT IN ('333333','555555','999999','99999A','99999B') ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         S1.CLASSCD, ");
        stb.append("         S1.SCHOOL_KIND, ");
        stb.append("         S1.CURRICULUM_CD, ");
        stb.append("         S1.SUBCLASSCD, ");
        stb.append("         S1.SCHREGNO, ");
        stb.append("         S1.SCORE, ");
        stb.append("         S1.VALUE_DI ");
        stb.append("     FROM ");
        stb.append("         RECORD_SCORE_DAT S1 ");
        stb.append("     WHERE ");
        stb.append("         S1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND S1.SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND S1.TESTKINDCD || S1.TESTITEMCD || S1.SCORE_DIV = '" + TESTCD + "' ");
        stb.append("         AND S1.VALUE_DI = '*' ");
        stb.append("         AND NOT EXISTS ( ");
        stb.append("             SELECT ");
        stb.append("                 'X' ");
        stb.append("             FROM ");
        stb.append("                 RECORD_RANK_SDIV_DAT R1 ");
        stb.append("             WHERE ");
        stb.append("                 R1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("                 AND R1.SEMESTER = '" + _param._semester + "' ");
        stb.append("                 AND R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '" + TESTCD + "' ");
        stb.append("                 AND R1.CLASSCD = S1.CLASSCD ");
        stb.append("                 AND R1.SCHOOL_KIND = S1.SCHOOL_KIND ");
        stb.append("                 AND R1.CURRICULUM_CD = S1.CURRICULUM_CD ");
        stb.append("                 AND R1.SUBCLASSCD = S1.SUBCLASSCD ");
        stb.append("                 AND R1.SCHREGNO = S1.SCHREGNO ");
        stb.append("         ) ");
        stb.append(" ), RECORD_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         ROUND(FLOAT(COUNT(SUBCLASSCD)) / 2, 0) AS SUBCNT_MAJORITY, ");
        stb.append("         SUM(CASE WHEN SCORE = 1 THEN 1 ELSE 0 END) AS SCORE_CNT1, ");
        stb.append("         SUM(CASE WHEN SCORE = 2 THEN 1 ELSE 0 END) AS SCORE_CNT2, ");
        stb.append("         SUM(CASE WHEN SCORE = 2 OR SCORE = 3 THEN 1 ELSE 0 END) AS SCORE_CNT23, ");
        stb.append("         SUM(CASE WHEN VALUE_DI = '*' THEN 1 ELSE 0 END) AS VALUE_DI_CNT ");
        stb.append("     FROM ");
        stb.append("         RECORD ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ) ");
        if ("SUB".equals(dataDiv)) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     R1.CLASSCD, ");
            stb.append("     R1.SCHOOL_KIND, ");
            stb.append("     R1.CURRICULUM_CD, ");
            stb.append("     R1.SUBCLASSCD, ");
            stb.append("     CLS_M.CLASSABBV, ");
            stb.append("     SUB_M.SUBCLASSNAME ");
        } else {
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     R1.CLASSCD, ");
            stb.append("     R1.SCHOOL_KIND, ");
            stb.append("     R1.CURRICULUM_CD, ");
            stb.append("     R1.SUBCLASSCD, ");
            stb.append("     CLS_M.CLASSABBV, ");
            stb.append("     SUB_M.SUBCLASSNAME, ");
            stb.append("     R1.SCORE, ");
            stb.append("     R1.VALUE_DI ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG T1 ");
        stb.append("     INNER JOIN RECORD_CNT C1 ON C1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN RECORD R1 ON R1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN CLASS_MST CLS_M ");
        stb.append("          ON CLS_M.CLASSCD = R1.CLASSCD ");
        stb.append("         AND CLS_M.SCHOOL_KIND = R1.SCHOOL_KIND ");
        stb.append("     INNER JOIN SUBCLASS_MST SUB_M ");
        stb.append("          ON SUB_M.CLASSCD = R1.CLASSCD ");
        stb.append("         AND SUB_M.SCHOOL_KIND = R1.SCHOOL_KIND ");
        stb.append("         AND SUB_M.CURRICULUM_CD = R1.CURRICULUM_CD ");
        stb.append("         AND SUB_M.SUBCLASSCD = R1.SUBCLASSCD ");
        stb.append(" WHERE ");
        if ("1".equals(_param._output)) {
            stb.append("     C1.SCORE_CNT1 >= 1 OR C1.SCORE_CNT2 >= 3 OR C1.SCORE_CNT23 >= C1.SUBCNT_MAJORITY ");
        } else {
            stb.append("     C1.VALUE_DI_CNT >= 1 ");
        }
        if ("SUB".equals(dataDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     R1.CLASSCD, ");
            stb.append("     R1.SCHOOL_KIND, ");
            stb.append("     R1.CURRICULUM_CD, ");
            stb.append("     R1.SUBCLASSCD ");
        } else {
            stb.append(" ORDER BY ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     R1.CLASSCD, ");
            stb.append("     R1.SCHOOL_KIND, ");
            stb.append("     R1.CURRICULUM_CD, ");
            stb.append("     R1.SUBCLASSCD ");
        }

        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final Map _recordRank;
        public Student(
                final String schregno,
                final String hrClass,
                final String attendno,
                final String name
        ) {
            _schregno = schregno;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _recordRank = new HashMap<String, RecordRank>();
        }
    }

    private class RecordRank {
        final String _joinSubclassCd;
        final String _score;
        final String _valueDi;
        public RecordRank(
                final String joinSubclassCd,
                final String score,
                final String valueDi
        ) {
            _joinSubclassCd = joinSubclassCd;
            _score = score;
            _valueDi = valueDi;
        }
    }

    private class SubclassMst {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _joinSubclassCd;
        final String _classAbbv;
        final String _subclassname;
        public SubclassMst(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String joinSubclassCd,
                final String classAbbv,
                final String subclassname
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _joinSubclassCd = joinSubclassCd;
            _classAbbv = classAbbv;
            _subclassname = subclassname;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75343 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlDate;
        final String _printDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _grade;
        final String _gradeName;
        final String _semester;
        final String _semesterName;
        final String _regdSeme;
        final String[] _categorySelected;
        final String _output;
        final String _outputName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printDate = _ctrlDate.replace('-', '/');
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _gradeName = getGradeName(db2);
            _semester = request.getParameter("SEMESTER");
            _semesterName = getSemesterName(db2);
            _regdSeme = "9".equals(_semester) ? _ctrlSemester : _semester;
            _categorySelected = request.getParameterValues("category_selected");
            _output = request.getParameter("OUTPUT");
            _outputName = "1".equals(_output) ? "（資料Ａ）" : "（指定生徒）";
        }

        private String getGradeName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GRADE = '" + _grade + "' ");

            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("GRADE_NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getSemesterName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("         YEAR     = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");

            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof
