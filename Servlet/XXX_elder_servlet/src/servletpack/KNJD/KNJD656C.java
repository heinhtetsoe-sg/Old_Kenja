/*
 * $Id: 53c766abff61cd11886da9d092f9382eafa40a39 $
 *
 * 作成日: 2010/05/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
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
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 智辯五條 成績一覧表
 */
public class KNJD656C {

    private static final Log log = LogFactory.getLog(KNJD656C.class);

    private final String[] ASSESSESMARK = new String[]{"A", "B", "C", "D"};

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
            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _param = createParam(db2, request);
            
            _hasData = false;
            
            for (int i = 0; i < _param._categorySelected.length; i++) {
                final String hrClass = _param._categorySelected[i];
                printMain(db2, svf, hrClass);
            }
            
        } catch (final Exception e) {
            log.error("Exception:", e);
        }

        try {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String hrClass) {
        boolean hasData = false;
        svf.VrSetForm("KNJD656C.frm", 1);
        
        List printedPerfect = new ArrayList();
        List students = getStudents(db2, hrClass);
        int[][][] assessCount = new int[_param._mockCds.length][ASSESSESMARK.length][_param._mockSubclassCds.length];
        printHeader(svf, hrClass);
        int line = 1;
        for (Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (printStudent(svf, student, assessCount, line, printedPerfect)) {
                hasData = true;
            }
            line += 1;
        }
        
        printFooter(svf, assessCount);
        if (hasData) {
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printFooter(final Vrw32alp svf, int[][][] assessCount) {
        for (int a = 0; a < ASSESSESMARK.length; a++) {
            svf.VrsOutn("ASSESSMARK", (a + 1), ASSESSESMARK[a]);
            for (int i = 0; i < _param._mockCds.length; i++) {
                final String mockCd = _param._mockCds[i];
                final int idxTest = getMockTestIndex(mockCd);
                if (idxTest == -1) {
                    continue;
                }
                for (int s = 0; s < _param._mockSubclassCds.length; s++) {
                    svf.VrsOutn("TOTAL" + (s + 1) + "_" + idxTest, (a + 1), String.valueOf(assessCount[idxTest - 1][a][s]));
                }
            }
        }
    }

    private void printHeader(final Vrw32alp svf, final String hrClass) {
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._loginDate));
        svf.VrsOut("STAFF_NAME", (String) _param._staffs.get(hrClass));
        svf.VrsOut("YMD1", KNJ_EditDate.h_format_JP(_param._loginDate));
        final String hrName = (String) _param._hrNames.get(hrClass);
        svf.VrsOut("HR_NAME", null == hrName ? String.valueOf(Integer.parseInt(hrClass) + "組") : hrName);
        
        svf.VrsOut("TESTNAME", _param._mockGroupName);
        
        for (int i = 0; i < _param._mockCds.length; i++) {
            String mockCd = _param._mockCds[i];
            int testIdx = getMockTestIndex(mockCd);
            String mockName = (String) _param._mockNames.get(mockCd);
            for (int j = 0; j < _param._mockSubclassCds.length; j++) {
                svf.VrsOut("MOCKNAME" + (j + 1) + "_" + testIdx, mockName);
            }
        }

        for (Iterator it = _param._mockSubclassNames.keySet().iterator(); it.hasNext();) {
            String mockSubclassCd = (String) it.next();
            svf.VrsOut("SUBCLASS" + getMockSubclassIndex(mockSubclassCd), (String) _param._mockSubclassNames.get(mockSubclassCd));
        }
    }

    /**
     * 模試名の表示場所を得る
     * @param mockCd 模試コード
     * @return 模試名の表示場所
     */
    private int getMockTestIndex(String mockCd) {
        return getSvfIndex(mockCd, _param._mockCds);
    }
    
    /**
     * 模試科目名の表示場所を得る
     * @param mockSubclassCd 模試科目コード
     * @return 模試科目名の表示場所
     */
    private int getMockSubclassIndex(String mockSubclassCd) {
        return getSvfIndex(mockSubclassCd, _param._mockSubclassCds);
    }
    
    private int getSvfIndex(String str, String[] arr) {
        if (null != str && null != arr && arr.length != 0) {
            for (int i = 0; i < arr.length; i++) {
                if (str.equals(arr[i])) {
                    return i + 1;
                }
            }
        }
        return -1;
    }
    
    private boolean printStudent(Vrw32alp svf, Student student, int[][][] assessCount, int line, List printedPerfect) {
        boolean hasData = false;
        svf.VrsOutn("ATTENDNO", line, null == student._attendno ? "" : String.valueOf(Integer.parseInt(student._attendno)));
        svf.VrsOutn("NAME", line, student._name);
        
        for (Iterator it = student._mockScores.keySet().iterator(); it.hasNext();) {
            
            final String mockCd = (String) it.next();
            final int idxTest = getMockTestIndex(mockCd);
            if (idxTest == -1) {
                continue;
            }

            for (Iterator it2 = student.getMockScores(mockCd).iterator(); it2.hasNext();) {
                
                 MockSubclassScore score = (MockSubclassScore) it2.next();
                 int idxSubclass = getMockSubclassIndex(score._mockSubclassCd);
                 if (idxSubclass == -1) {
                     // log.debug(" mockCd = " + score._mockCd + " ,  subclassCd = " +  score._mockSubclassCd);
                     continue;
                 }
                 if (idxSubclass - 1 < _param._mockSubclassCds.length && -1 != score._assessMarkCd && score._assessMarkCd <= ASSESSESMARK.length) {
                     assessCount[idxTest - 1][score._assessMarkCd - 1][idxSubclass - 1] += 1;
                 }
                 if (null != score._perfect && !printedPerfect.contains("PERFECT" + idxSubclass)) {
                     svf.VrsOut("PERFECT" + idxSubclass, score._perfect.toString());
                     printedPerfect.add("PERFECT" + idxSubclass);
                 }
                 svf.VrsOutn("MOCKSCORE" + idxSubclass + "_" + idxTest, line, score._score);
                 hasData = true;
            }
        }
        return hasData;
    }
    
    private Student getStudent(List students, String schregno) {
        for (Iterator it = students.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            if (student._schregno != null && student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }
    
    private List getStudents(final DB2UDB db2, final String hrClass) {
        List students = new ArrayList();
        try {
            final String sqlStudent = sqlStudent(hrClass);
            // log.debug(" sqlStudent =" + sqlStudent);
            PreparedStatement ps = db2.prepareStatement(sqlStudent);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrclass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                
                Student student = new Student(schregno, name, hrclass, attendno);
                students.add(student);
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        }
        
        try {
            final String sqlSubclasses = sqlMockSubclasses(hrClass);
            log.debug(" sqlSubclasses = " + sqlSubclasses);
            final PreparedStatement ps = db2.prepareStatement(sqlSubclasses);
            final ResultSet rs = ps.executeQuery();
            List mockSubclassCds = new ArrayList();
            while (rs.next()) {
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                mockSubclassCds.add(mockSubclassCd);
            }
            _param._mockSubclassCds = new String[mockSubclassCds.size()];
            for (int i = 0; i < mockSubclassCds.size(); i++) {
                _param._mockSubclassCds[i] = (String) mockSubclassCds.get(i); 
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        }
        
        try {
            final String sqlScore = sqlScore(hrClass, null);
            // log.debug(" sqlScore =" + sqlScore);
            final PreparedStatement ps = db2.prepareStatement(sqlScore);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                Student student = getStudent(students, schregno);
                if (student == null) {
                    continue;
                }
                final String mockCd = rs.getString("MOCKCD");
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                final String score = rs.getString("SCORE");
                final Integer perfect = null == rs.getString("PERFECT") ? null : Integer.valueOf(rs.getString("PERFECT"));
                final String assessMark = rs.getString("ASSESSMARK");
                int assessMarkCd = -1;
                for (int i = 0; i < ASSESSESMARK.length; i++) {
                    if (ASSESSESMARK[i].equals(assessMark)) {
                        assessMarkCd = i + 1;
                        break;
                    }
                }
                
                MockSubclassScore mockSubclassScore = new MockSubclassScore(mockCd, mockSubclassCd, score, assessMarkCd, perfect);
                student.getMockScores(mockCd).add(mockSubclassScore); 
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        }
        
        return students;
    }
    
    private String sqlStudent(String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T0.NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        stb.append(" FROM SCHREG_REGD_DAT T1  ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T0 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    private String sqlScore(String hrClass, String sqlwith) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_MOCK_PERFECT_COURSE AS (");
        stb.append(" SELECT ");
        stb.append("     T3.YEAR, ");
        stb.append("     T3.MOCKCD, ");
        stb.append("     T3.MOCK_SUBCLASS_CD, ");
        stb.append("     T3.DIV, ");
        stb.append("     T3.GRADE, ");
        stb.append("     T3.COURSECD, ");
        stb.append("     T3.MAJORCD, ");
        stb.append("     T3.COURSECODE, ");
        stb.append("     T3.PASS_SCORE, ");
        stb.append("     T3.PERFECT ");
        stb.append(" FROM ");
        stb.append("     MOCK_PERFECT_COURSE_DAT T3 ");
        stb.append(" WHERE ");
        stb.append("     T3.MOCKCD = '" + _param._mockCds[0] + "' AND ");
        stb.append("     T3.COURSE_DIV = '0' AND ");
        stb.append("     T3.DIV = (SELECT ");
        stb.append("                     MIN(DIV) ");
        stb.append("                 FROM ");
        stb.append("                     MOCK_PERFECT_COURSE_DAT T1 ");
        stb.append("                 WHERE ");
        stb.append("                     T3.YEAR = T1.YEAR                  AND ");
        stb.append("                     T3.COURSE_DIV = T1.COURSE_DIV                   AND ");
        stb.append("                     T3.MOCKCD = T1.MOCKCD                  AND ");
        stb.append("                     T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD                  AND ");
        stb.append("                     T3.GRADE = T1.GRADE  ");
        stb.append("                 ) ");
        stb.append(" ) ");
        if (null != sqlwith) {
            stb.append(" , " + sqlwith + " AS (");
        }
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.MOCKCD, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T4.ASSESSMARK, ");
        stb.append("     T5.PERFECT ");
        stb.append(" FROM SCHREG_REGD_DAT T2  ");
        stb.append(" INNER JOIN MOCK_DAT T1 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append(" LEFT JOIN MOCK_SUBCLASS_ASSESS_DAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("     AND T4.GRADE = T2.GRADE ");
        stb.append("     AND T4.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T1.SCORE BETWEEN T4.ASSESSLOW AND T4.ASSESSHIGH ");
        stb.append(" LEFT JOIN T_MOCK_PERFECT_COURSE T5 ON T5.YEAR = T1.YEAR ");
        stb.append("     AND T5.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T5.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T5.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T2.GRADE END ");
        stb.append("     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = ");
        stb.append("      CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T2.GRADE || '-' || T2.HR_CLASS = '" + hrClass + "' ");
        stb.append("     AND T1.MOCKCD IN " + SQLUtils.whereIn(true, _param._mockCds) + " ");
        stb.append(" ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        if (null != sqlwith) {
            stb.append(" ) ");
        }
        return stb.toString();
    }
    
    private String sqlMockSubclasses(String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(sqlScore(hrClass, "T1"));
        stb.append(" SELECT DISTINCT MOCK_SUBCLASS_CD ");
        stb.append(" FROM T1 ");
        stb.append(" ORDER BY MOCK_SUBCLASS_CD ");
        return stb.toString();
    }
    
    private class Student {
        final String _schregno;
        final String _name;
        final String _hrclass;
        final String _attendno;
        final Map _mockScores = new HashMap();
        public Student(
                String schregno,
                String name,
                String hrclass,
                String attendno
        ) {
            _schregno = schregno;
            _name = name;
            _hrclass = hrclass;
            _attendno = attendno;
        }
        public List getMockScores(String mockCd) {
            if (!_mockScores.containsKey(mockCd)) {
                _mockScores.put(mockCd, new ArrayList());
            }
            return (List) _mockScores.get(mockCd);
        }
    }
    
    private class MockSubclassScore {
        final String _mockCd;
        final String _mockSubclassCd;
        final String _score;
        final int _assessMarkCd;
        final Integer _perfect;

        public MockSubclassScore(
                String mockCd,
                String mockSubclassCd,
                String score,
                int assessMarkCd,
                Integer perfect
        ) {
            _mockCd = mockCd;
            _mockSubclassCd = mockSubclassCd;
            _score = score;
            _assessMarkCd = assessMarkCd;
            _perfect = perfect;
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

        private final String _year;
        private final String _semester;
        
        private final String _groupDiv;
        private final String _targetDiv;
        private final String _stfAuthCd;
        private final String _groupCd;
        private final String _loginDate;
        
        private final String _grade;
        private final String[] _categorySelected;
        private final String[] _mockCds;
        
        private final Map _mockNames = new TreeMap();
        private final Map _mockSubclassNames = new TreeMap();
        private String[] _mockSubclassCds;
        private final Map _hrNames = new HashMap();
        
        private final Map _staffs = new HashMap();
        private String _mockGroupName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _loginDate = request.getParameter("LOGIN_DATE");
            final String mockCd = request.getParameter("MOCKCD");
            
            _groupDiv = request.getParameter("GROUP_DIV");
            _targetDiv = request.getParameter("TARGET_DIV");
            _stfAuthCd = request.getParameter("STF_AUTH_CD");
            _groupCd = request.getParameter("GROUPCD");

            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");

            _mockCds = setMockNames(db2, mockCd);
            setMockSubclassNames(db2);
            setHrNames(db2);
            setRegdHdatStaff(db2);
        }
        
        private String[] setMockNames(DB2UDB db2, final String mockCdParam) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _mockNames.clear();
            final List mockCdList = new ArrayList();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.GROUP_DIV, T1.TARGET_DIV, T1.STF_AUTH_CD, T1.GROUPCD, T1.MOCK_TARGET_CD AS MOCK_CD, ");
                stb.append("         T2.GROUPNAME1, T3.MOCKNAME3 ");
                stb.append(" FROM MOCK_GROUP_DAT T1 ");
                stb.append(" INNER JOIN MOCK_GROUP_MST T2 ON ");
                stb.append("     T2.GROUP_DIV = T1.GROUP_DIV ");
                stb.append("     AND T2.STF_AUTH_CD = T1.STF_AUTH_CD ");
                stb.append("     AND T2.GROUPCD = T1.GROUPCD ");
                stb.append(" LEFT JOIN MOCK_MST T3 ON ");
                stb.append("     T3.MOCKCD = T1.MOCK_TARGET_CD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GROUP_DIV = '" + _groupDiv + "' ");
                stb.append("     AND T1.TARGET_DIV = '" + _targetDiv + "' ");
                stb.append("     AND T1.STF_AUTH_CD = '" + _stfAuthCd + "' ");
                stb.append("     AND T1.GROUPCD = '" + _groupCd + "' ");
                stb.append("     AND T1.MOCK_TARGET_CD <= '" + mockCdParam + "' ");
                final String sql = stb.toString();
                log.debug(" sql mockname = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mockGroupName = rs.getString("GROUPNAME1"); 
                    String mockCd = rs.getString("MOCK_CD");
                    String name = rs.getString("MOCKNAME3");
                    _mockNames.put(mockCd, name);
                    mockCdList.add(mockCd);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final String[] mockCds = new String[mockCdList.size()];
            int i = 0;
            for (Iterator it = mockCdList.iterator(); it.hasNext();) {
                final String mockCd = (String) it.next();
                mockCds[i++] = mockCd;
            }
            return mockCds;
        }

        private void setMockSubclassNames(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _mockSubclassNames.clear();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT MOCK_SUBCLASS_CD, SUBCLASS_NAME ");
                stb.append(" FROM MOCK_SUBCLASS_MST ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                    String name = rs.getString("SUBCLASS_NAME");
                    if (null != mockSubclassCd) {
                        _mockSubclassNames.put(mockSubclassCd, name);
                    }
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setHrNames(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _hrNames.clear();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT GRADE || '-' || HR_CLASS AS HR_CLASS, HR_NAME ");
                stb.append(" FROM SCHREG_REGD_HDAT ");
                stb.append(" WHERE  ");
                stb.append("    YEAR = '" + _year + "' ");
                stb.append("    AND SEMESTER = '" + _semester + "' ");
                stb.append("    AND GRADE = '" + _grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    String hrclass = rs.getString("HR_CLASS");
                    String name = rs.getString("HR_NAME");
                    _hrNames.put(hrclass, name);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void setRegdHdatStaff(final DB2UDB db2) {
            final String sql = " SELECT T1.GRADE || '-' || T1.HR_CLASS AS CODE, T2.STAFFNAME "
                + " FROM SCHREG_REGD_HDAT T1 INNER JOIN V_STAFF_MST T2 ON T1.YEAR = T2.YEAR AND T1.TR_CD1 = T2.STAFFCD"
                + " WHERE T1.YEAR = '" + _year + "'"
                + "   AND T1.SEMESTER = '" + _semester + "'";
            try {
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
                    final String name = rs.getString("STAFFNAME");
                    _staffs.put(code, name);
                }
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            } catch (final SQLException e) {
                log.warn("担任名の取得でエラー", e);
            }
        }
    }
}

// eof
