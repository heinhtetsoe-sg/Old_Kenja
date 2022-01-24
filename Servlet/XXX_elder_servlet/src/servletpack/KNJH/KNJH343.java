// kanji=漢字
/*
 * $Id: 80202de7d61059cecef9efdcff4502e0c69b9531 $
 *
 * 作成日: 2013/03/15 15:42:58 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 80202de7d61059cecef9efdcff4502e0c69b9531 $
 */
public class KNJH343 {

    private static final Log log = LogFactory.getLog("KNJH343.class");

    private final int MAX_ROWCNT = 5;
    private final String ALLSUBCLASS = "999999";
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
        final List studentList = getStudentList(db2);
        svf.VrSetForm("KNJH343.frm", 1);
        int rowCnt = 1;
        String befGradeHr = "";
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (rowCnt > MAX_ROWCNT) {
                svf.VrEndPage();
                rowCnt = 1;
            }
            if (!"".equals(befGradeHr) && !befGradeHr.equals(student._grade + student._hrClass)) {
                svf.VrEndPage();
                rowCnt = 1;
            }
            printStudent(svf, student, rowCnt);
            befGradeHr = student._grade + student._hrClass;
            rowCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    protected void printStudent(final Vrw32alp svf, final Student student, final int rowCnt) {
        log.debug(student);
        svf.VrsOutn("NENDO", rowCnt, KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOutn("SEMESTER", rowCnt, _param._semeName);
        svf.VrsOutn("TESTNAME", rowCnt, _param._mockName);
        svf.VrsOutn("HR_NAME", rowCnt, student._hrName + "(" + student._attendNo + ")");
        svf.VrsOutn("NAME", rowCnt, student._name);
        svf.VrsOutn("CLASS_RANK_NAME", rowCnt, "学級");
        svf.VrsOutn("CLASS_AVG_NAME", rowCnt, "学級");
        svf.VrsOutn("AVG_NAME", rowCnt, "学年");
        svf.VrsOutn("RANK_NAME", rowCnt, "学年");
        int mockSubclassCnt = 1;
        for (final Iterator itMockSub = student._subclassList.iterator(); itMockSub.hasNext();) {
            final MockSubclassData mockSubclassData = (MockSubclassData) itMockSub.next();
            if (ALLSUBCLASS.equals(mockSubclassData._mockSubclassCd)) {
                svf.VrsOutn("TOTAL_SCORE", rowCnt, mockSubclassData._score);
                svf.VrsOutn("AVERAGEL_SCORE", rowCnt, mockSubclassData._avg);
                svf.VrsOutn("CLASS_AVERAGE", rowCnt, mockSubclassData._avgClass);
                svf.VrsOutn("CLASS_RANK", rowCnt, mockSubclassData._classRank);
                svf.VrsOutn("TOTAL_AVERAGE", rowCnt, mockSubclassData._avgGrade);
                svf.VrsOutn("TOTAL_RANK", rowCnt, mockSubclassData._gradeRank);
            } else {
                svf.VrsOutn("SUBCLASS" + mockSubclassCnt, rowCnt, mockSubclassData._subclassAbbv);
                svf.VrsOutn("SCORE" + mockSubclassCnt, rowCnt, mockSubclassData._score);
                svf.VrsOutn("CLASS_AVERAGE" + mockSubclassCnt, rowCnt, mockSubclassData._avgClass);
                svf.VrsOutn("CLASS_RANK" + mockSubclassCnt, rowCnt, mockSubclassData._classRank);
                svf.VrsOutn("AVERAGE" + mockSubclassCnt, rowCnt, mockSubclassData._avgGrade);
                svf.VrsOutn("RANK" + mockSubclassCnt, rowCnt, mockSubclassData._gradeRank);
            }
            mockSubclassCnt++;
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List rtnStudent = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement ps  =null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Student student = new Student(rs.getString("SCHREGNO"),
                                              rs.getString("GRADE"),
                                              rs.getString("HR_CLASS"),
                                              rs.getString("ATTENDNO"),
                                              rs.getString("HR_NAME"),
                                              rs.getString("NAME"));
                student.setStudentInfo(db2);
                rtnStudent.add(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnStudent;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     REG_H.GRADE, ");
        stb.append("     REG_H.HR_CLASS, ");
        stb.append("     REG_H.HR_NAME, ");
        stb.append("     REG_D.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REG_D ON REG_D.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND REG_D.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("          AND T1.SCHREGNO = REG_D.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REG_H ON REG_D.YEAR = REG_H.YEAR ");
        stb.append("          AND REG_D.SEMESTER = REG_H.SEMESTER ");
        stb.append("          AND REG_D.GRADE = REG_H.GRADE ");
        stb.append("          AND REG_D.HR_CLASS = REG_H.HR_CLASS ");
        stb.append(" WHERE ");
        if ("1".equals(_param._selectType)) {
            stb.append("     REG_D.GRADE || '-' || REG_D.HR_CLASS IN " + _param._hrOrSchInState + " ");
        } else {
            stb.append("     T1.SCHREGNO IN " + _param._hrOrSchInState + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REG_H.GRADE, ");
        stb.append("     REG_H.HR_CLASS, ");
        stb.append("     REG_D.ATTENDNO ");

        return stb.toString();
    }

    class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _name;

        final List _subclassList;
        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String name
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _name = name;
            _subclassList = new ArrayList();
        }

        /**
         * @param db2
         */
        public void setStudentInfo(final DB2UDB db2) throws SQLException {

            final String subclassSql = getSubclass();
            PreparedStatement psSub = null;
            ResultSet rsSub = null;
            try {
                psSub = db2.prepareStatement(subclassSql);
                rsSub = psSub.executeQuery();
                while (rsSub.next()) {
                    final MockSubclassData mockSubclassData = new MockSubclassData(
                            rsSub.getString("MOCK_SUBCLASS_CD"),
                            rsSub.getString("SUBCLASS_ABBV"),
                            rsSub.getString("AVG_CLASS"),
                            rsSub.getString("CLASS_RANK"),
                            rsSub.getString("AVG_GRADE"),
                            rsSub.getString("GRADE_RANK"),
                            rsSub.getString("SCORE"),
                            rsSub.getString("AVG")
                            );
                    _subclassList.add(mockSubclassData);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSub, rsSub);
            }

        }

        /**
         * @param db2
         */
        public String getSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.MOCK_SUBCLASS_CD, ");
            stb.append("     MOCK_SM.SUBCLASS_ABBV, ");
            stb.append("     MOCK_AVG_C.AVG AS AVG_CLASS, ");
            stb.append("     T1.CLASS_RANK, ");
            stb.append("     MOCK_AVG_G.AVG AS AVG_GRADE, ");
            stb.append("     T1.GRADE_RANK, ");
            if ("3".equals(_param._mockDiv)) {
                stb.append("     M1.SCORE, ");
                stb.append("     M1.AVG ");
            } else {
                stb.append("     T1.SCORE, ");
                stb.append("     T1.AVG ");
            }
            stb.append(" FROM ");
            stb.append("     MOCK_RANK_DAT T1 ");
            stb.append("     LEFT JOIN MOCK_SUBCLASS_MST MOCK_SM ON T1.MOCK_SUBCLASS_CD = MOCK_SM.MOCK_SUBCLASS_CD ");
            stb.append("     LEFT JOIN MOCK_AVERAGE_DAT MOCK_AVG_C ON T1.YEAR = MOCK_AVG_C.YEAR ");
            stb.append("          AND T1.MOCKCD = MOCK_AVG_C.MOCKCD ");
            stb.append("          AND T1.MOCK_SUBCLASS_CD = MOCK_AVG_C.MOCK_SUBCLASS_CD ");
            stb.append("          AND MOCK_AVG_C.AVG_DIV = '2' ");
            stb.append("          AND MOCK_AVG_C.GRADE = '" + _grade + "' ");
            stb.append("          AND MOCK_AVG_C.HR_CLASS = '" + _hrClass + "' ");
            stb.append("     LEFT JOIN MOCK_AVERAGE_DAT MOCK_AVG_G ON T1.YEAR = MOCK_AVG_G.YEAR ");
            stb.append("          AND T1.MOCKCD = MOCK_AVG_G.MOCKCD ");
            stb.append("          AND T1.MOCK_SUBCLASS_CD = MOCK_AVG_G.MOCK_SUBCLASS_CD ");
            stb.append("          AND MOCK_AVG_G.AVG_DIV = '1' ");
            stb.append("          AND MOCK_AVG_G.GRADE = '" + _grade + "' ");
            stb.append("     LEFT JOIN MOCK_RANK_DAT M1 ON  M1.YEAR = t1.YEAR ");
            stb.append("          AND M1.MOCKCD = t1.MOCKCD ");
            stb.append("          AND M1.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND M1.MOCK_SUBCLASS_CD = t1.MOCK_SUBCLASS_CD ");
            stb.append("          AND M1.MOCKDIV = '1' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.MOCKCD = '" + _param._mockCd + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND T1.MOCKDIV = '" + _param._mockDiv + "' ");
            stb.append("     AND T1.MOCK_SUBCLASS_CD NOT IN ('333333', '555555') ");
            stb.append(" ORDER BY ");
            stb.append("     T1.MOCK_SUBCLASS_CD ");

            return stb.toString();
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }

    private class MockSubclassData {
        private final String _mockSubclassCd;
        private final String _subclassAbbv;
        private final String _avgClass;
        private final String _classRank;
        private final String _avgGrade;
        private final String _gradeRank;
        private final String _score;
        private final String _avg;

        MockSubclassData(
                final String mockSubclassCd,
                final String subclassAbbv,
                final String avgClass,
                final String classRank,
                final String avgGrade,
                final String gradeRank,
                final String score,
                final String avg
        ) {
            _mockSubclassCd = mockSubclassCd;
            _subclassAbbv = subclassAbbv;
            _avgClass = getSisyaGonyu(avgClass);
            _classRank = classRank;
            _avgGrade = getSisyaGonyu(avgGrade);
            _gradeRank = gradeRank;
            _score = score;
            _avg = getSisyaGonyu(avg);
        }

    }

    private static String getSisyaGonyu(final String doubleValue) {
        return null == doubleValue ? null : new BigDecimal(doubleValue).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
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
        private final String _dataDiv;
        private final String _mockCd;
        private final String _selectType;
        private final String[] _hrOrSch;
        private final String _grade;
        private final String _hrClass;
        private String _hrOrSchInState;
        private final String _semeName;
        private final String _mockName;
        private final String _mockDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _dataDiv = request.getParameter("DATA_DIV");
            _mockCd = request.getParameter("MOCKCD");
            _selectType = request.getParameter("CATEGORY_IS_CLASS");
            _hrOrSch = request.getParameterValues("CATEGORY_SELECTED");
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int ia = 0; ia<_hrOrSch.length; ia++) {
                stb.append(sep + "'" + _hrOrSch[ia] + "'");
                sep = ",";
            }
            stb.append(")");
            _hrOrSchInState = stb.toString();
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _semeName = getSemeName(db2);
            _mockName = getMockName(db2);
            final String juni = request.getParameter("JUNI");
            final String rankDivTemp = request.getParameter("useKnjd106cJuni" + juni);
            _mockDiv = StringUtils.isEmpty(rankDivTemp) ? juni : rankDivTemp;
        }

        private String getSemeName(final DB2UDB db2) throws SQLException {
            String ret = "";
            final String semeSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return ret;
        }

        private String getMockName(final DB2UDB db2) throws SQLException {
            String ret = "";
            final String semeSql = "SELECT MOCKNAME1 FROM MOCK_MST WHERE MOCKCD = '" + _mockCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret = rs.getString("MOCKNAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return ret;
        }

    }
}

// eof
