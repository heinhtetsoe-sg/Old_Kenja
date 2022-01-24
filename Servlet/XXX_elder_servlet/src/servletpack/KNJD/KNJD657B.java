/*
 * $Id: 2eb6dc2e2b46837668bd7de68f00758cffd05e9f $
 *
 * 作成日: 2010/05/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 智辯五條 個人成績表
 */
public class KNJD657B {

    private static final String SUBCLASSALL = "000000";
    private static final Log log = LogFactory.getLog(KNJD657B.class);

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
            
            printMain(db2, svf);
            
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
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        List students = getStudents(db2);
        for (Iterator it = students.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            _hasData = printStudent(svf, student) || _hasData;
        }
    }
    
    private boolean printStudent(Vrw32alp svf, Student student) {
        svf.VrSetForm("KNJD657.frm", 1);
        
        printHeader(svf, student);
        
        printScoreMark(svf, student);
        
        svf.VrEndPage();
        
        return true;
    }
    
    private int getMockTestIndex(String mockCd) {
        return getSvfIndex(mockCd, _param._mockCds);
    }
    
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
    
    private void printScoreMark(Vrw32alp svf, Student student) {
        
        for (Iterator it = student._mockScores.iterator(); it.hasNext();) {
            MockSubclassScore score = (MockSubclassScore) it.next();
            int idxTest = getMockTestIndex(score._mockCd);
            int idxSubclass = getMockSubclassIndex(score._mockSubclassCd);
            if (idxTest == -1 || idxSubclass == -1) {
                log.debug(" mockCd = " + score._mockCd + " ,  subclassCd = " +  score._mockSubclassCd);
                continue;
            }
            svf.VrsOutn("PERCENTAGE" + idxTest, idxSubclass, score._percentage);
            svf.VrsOutn("LEVEL" + idxTest, idxSubclass, score._assessMark);
        }
        for (Iterator it = student._mockDocumentSdat.iterator(); it.hasNext();) {
            MockDocumentSdat mockDocumentSdat = (MockDocumentSdat) it.next();
            int idxSubclass = getMockSubclassIndex(mockDocumentSdat._mockSubclassCd);
            svf.VrsOutn("REMARK1", idxSubclass, mockDocumentSdat._remark1);
            svf.VrsOutn("REMARK2", idxSubclass, mockDocumentSdat._remark2);
            svf.VrsOutn("REMARK3", idxSubclass, mockDocumentSdat._remark3);
        }
        final String remark4 = (String) _param._mockDocumentGdat.get(SUBCLASSALL);
        if (!StringUtils.isEmpty(remark4)) {
            svf.VrsOut("MARK", "*");
            svf.VrsOut("REMARK4", "*" + remark4);
        }
    }
    
    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }

    private void printHeader(Vrw32alp svf, Student student) {
        svf.VrsOut("SCHOOLNAME", _param._schoolName);
        final String staffName = (String) _param._staffs.get(_param._grade + student._hrclass);
        svf.VrsOut("TEACHER", (_param._remark2 == null ? "" : _param._remark2) + staffName);
        svf.VrsOut("ZIPCD", student.getAddresseeZipCd());
        if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(student.getAddresseeAddr1()) > 50 || getMS932ByteLength(student.getAddresseeAddr2()) > 50)) {
            svf.VrsOut("ADDR1_2", student.getAddresseeAddr1());
            svf.VrsOut("ADDR2_2", student.getAddresseeAddr2());
        } else {
            svf.VrsOut("ADDR1", student.getAddresseeAddr1());
            svf.VrsOut("ADDR2", student.getAddresseeAddr2());
        }
        svf.VrsOut("ADDRESSEE", student.getAddresseeName() == null ? "" : student.getAddresseeName());
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._executeDate) + "（" + KNJ_EditDate.h_format_W(_param._executeDate) + "）本試験実施");
        svf.VrsOut("GRADE", _param.getGradeName());
        final String hrName = (String) _param._hrNames.get(student._hrclass);
        svf.VrsOut("HR_NAME", null == hrName ? String.valueOf(Integer.parseInt(student._hrclass) + "組") : hrName);
        svf.VrsOut("ATTENDNO", null == student._attendno ? "" : String.valueOf(Integer.parseInt(student._attendno)));
        svf.VrsOut("MOCK_GROUPNAME", _param._mockGroupName);
        svf.VrsOut("NAME", student._name);
        for (int i = 0; i < _param._mockCds.length; i++) {
            String mockCd = _param._mockCds[i];
            int idxTest = getMockTestIndex(mockCd);
            svf.VrsOut("MOCK_NAME" + idxTest + "_1", (String) _param._mockNames.get(mockCd));
        }
        for (Iterator it = _param._mockSubclassNames.keySet().iterator(); it.hasNext();) {
            String mockSubclassCd = (String) it.next();
            svf.VrsOutn("SUBCLASS", getMockSubclassIndex(mockSubclassCd), (String) _param._mockSubclassNames.get(mockSubclassCd));
            int idxSubclass = getMockSubclassIndex(mockSubclassCd);
            final String[] token = KNJ_EditEdit.get_token((String) _param._mockDocumentGdat.get(mockSubclassCd), 14, 10);
            if (null != token) {
                for (int i = 0; i < token.length; i++) {
                    svf.VrsOutn("TEST_RANGE" + (i + 1), idxSubclass, token[i]);
                }
            }
        }
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
    
    private List getStudents(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List students = new ArrayList();
        try {
            final String sqlStudent = sqlStudent();
            log.debug(" sqlStudent =" + sqlStudent);
            ps = db2.prepareStatement(sqlStudent);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrclass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String zipCd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String guardZipCd = rs.getString("GUARD_ZIPCD");
                final String guardAddr1 = rs.getString("GUARD_ADDR1");
                final String guardAddr2 = rs.getString("GUARD_ADDR2");
                final String guardianName = rs.getString("GUARD_NAME");
                
                Student student = new Student(schregno, name, hrclass, attendno, zipCd, addr1, addr2, guardZipCd, guardAddr1, guardAddr2, guardianName);
                students.add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        try {
            final String sqlSubclasses = sqlMockSubclasses();
            ps = db2.prepareStatement(sqlSubclasses);
            rs = ps.executeQuery();
            List mockSubclassCds = new ArrayList();
            while (rs.next()) {
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                mockSubclassCds.add(mockSubclassCd);
            }
            _param._mockSubclassCds = new String[mockSubclassCds.size()];
            for (int i = 0; i < mockSubclassCds.size(); i++) {
                _param._mockSubclassCds[i] = (String) mockSubclassCds.get(i); 
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        try {
            final String sqlScore = sqlScore();
            log.debug(" sqlScore =" + sqlScore);
            ps = db2.prepareStatement(sqlScore);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                Student student = getStudent(students, schregno);
                if (student == null) {
                    continue;
                }
                final String mockCd = rs.getString("MOCKCD");
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                final String score = rs.getString("SCORE");
                final String perfect = rs.getString("PERFECT");
                final String percentage = null == perfect || null == score ? "" : rs.getBigDecimal("SCORE").multiply(new BigDecimal(100)).divide(rs.getBigDecimal("PERFECT"), 1, BigDecimal.ROUND_HALF_UP).toString() + "%";
                final String assessMark = rs.getString("ASSESSMARK");
                
                MockSubclassScore mockSubclassScore = new MockSubclassScore(mockCd, mockSubclassCd, score, percentage, assessMark);
                student._mockScores.add(mockSubclassScore); 
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        try {
            final String sqlDocumentSdat = sqlDocumentSdat();
            log.debug(" sqlDocumentSdat =" + sqlDocumentSdat);
            ps = db2.prepareStatement(sqlDocumentSdat);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                Student student = getStudent(students, schregno);
                if (student == null) {
                    continue;
                }
                final String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                MockDocumentSdat sdat = new MockDocumentSdat(mockSubclassCd, remark1, remark2, remark3);
                student._mockDocumentSdat.add(sdat); 
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return students;
    }
    
    private String sqlDocumentSdat() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T1.REMARK1, ");
        stb.append("     T1.REMARK2, ");
        stb.append("     T1.REMARK3 ");
        stb.append(" FROM ");
        stb.append("     MOCK_DOCUMENT_SDAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.GROUP_DIV = '" + _param._groupDiv + "' ");
        stb.append("     AND T1.STF_AUTH_CD = '" + _param._stfAuthCd + "' ");
        stb.append("     AND T1.GROUPCD = '" + _param._groupCd + "' ");
        return stb.toString();
    }

    private String sqlStudent() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T0.NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.ZIPCD, ");
        stb.append("     T3.ADDR1, ");
        stb.append("     T3.ADDR2, ");
        stb.append("     T2.GUARD_ZIPCD, ");
        stb.append("     T2.GUARD_ADDR1, ");
        stb.append("     T2.GUARD_ADDR2, ");
        stb.append("     T2.GUARD_NAME ");
        stb.append(" FROM SCHREG_REGD_DAT T1  ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T0 ON T0.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN GUARDIAN_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND '" + _param._executeDate + "' BETWEEN T3.ISSUEDATE AND VALUE(T3.EXPIREDATE, '9999-12-31') ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        if (_param._isCategoryStudent) {
            stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + _param._hrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        } else {
            stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        }
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    private String sqlScore() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.MOCKCD, ");
        stb.append("     T1.MOCK_SUBCLASS_CD, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T3.PERFECT, ");
        stb.append("     T3.PASS_SCORE, ");
        stb.append("     T4.ASSESSMARK ");
        stb.append(" FROM SCHREG_REGD_DAT T2  ");
        stb.append(" INNER JOIN MOCK_DAT T1 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append(" LEFT JOIN MOCK_PERFECT_COURSE_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.COURSE_DIV = '0' ");
        stb.append("     AND T3.MOCKCD = T1.MOCKCD ");
        stb.append("     AND T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T3.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE T2.GRADE END  ");
        stb.append("     AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = ");
        stb.append("       CASE WHEN DIV = '01' OR DIV = '02' THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ");
        stb.append(" LEFT JOIN MOCK_SUBCLASS_ASSESS_DAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("     AND T4.GRADE = T2.GRADE ");
        stb.append("     AND T4.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("     AND T1.SCORE BETWEEN T4.ASSESSLOW AND T4.ASSESSHIGH ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        if (_param._isCategoryStudent) {
            stb.append("     AND T2.GRADE || '-' || T2.HR_CLASS = '" + _param._hrclass + "' ");
            stb.append("     AND T2.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        } else {
            stb.append("     AND T2.GRADE || '-' || T2.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        }
        stb.append("     AND T1.MOCKCD IN " + SQLUtils.whereIn(true, _param._mockCds) + " ");
        stb.append(" ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        return stb.toString();
    }
    
    private String sqlMockSubclasses() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T1 AS ( ");
        stb.append(sqlScore());
        stb.append(" ) ");
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
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _guardZipCd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _guardianName;
        final List _mockScores = new ArrayList();
        final List _mockDocumentSdat = new ArrayList();
        String _remark4;
        public Student(
                String schregno,
                String name,
                String hrclass,
                String attendno,
                String zipCd,
                String addr1,
                String addr2,
                String guardZipCd,
                String guardAddr1,
                String guardAddr2,
                String guardianName
        ) {
            _schregno = schregno;
            _name = name;
            _hrclass = hrclass;
            _attendno = attendno;
            _zipCd = zipCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _guardZipCd = guardZipCd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardianName = guardianName;
        }
        
        public String getAddresseeZipCd() {
            final String zipcd = "1".equals(_param._useAddress) ? _zipCd : _guardZipCd;
            return zipcd == null ? "" : zipcd;
        }
        public String getAddresseeAddr1() {
            final String addr1 = "1".equals(_param._useAddress) ? _addr1 : _guardAddr1;
            return addr1 == null ? "" : addr1;
        }
        public String getAddresseeAddr2() {
            final String addr2 = "1".equals(_param._useAddress) ? _addr2 : _guardAddr2;
            return addr2 == null ? "" : addr2;
        }
        public String getAddresseeName() {
            final String name = "1".equals(_param._useAddress) ? _name : _guardianName;
            return name == null ? "" : name + "  様";
        }
    }
    
    private String nullBlank(String str) {
        return str == null ? "" : str;
    }
    
    private class MockSubclassScore {
        final String _mockCd;
        final String _mockSubclassCd;
        final String _score;
        final String _percentage;
        final String _assessMark;

        public MockSubclassScore(
                String mockCd,
                String mockSubclassCd,
                String score,
                String percentage,
                String assessMark
        ) {
            _mockCd = mockCd;
            _mockSubclassCd = mockSubclassCd;
            _score = score;
            _percentage = percentage;
            _assessMark = assessMark;
        }
    }
    
    private class MockDocumentSdat {
        final String _mockSubclassCd;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        public MockDocumentSdat(
                String mockSubclassCd,
                String remark1,
                String remark2,
                String remark3
        ) {
            _mockSubclassCd = mockSubclassCd;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
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
        private final boolean _isCategoryStudent;
        
        private final String _groupDiv;
        private final String _targetDiv;
        private final String _stfAuthCd;
        private final String _groupCd;
        
        
        private final String _useAddress;
        private final String _executeDate;
        
        private final String _grade;
        private final String _hrclass;
        private final String[] _categorySelected;
        
        private final Map _mockNames = new TreeMap();
        private String[] _mockCds;
        private final Map _mockSubclassNames = new TreeMap();
        private String[] _mockSubclassCds;
        private final Map _mockDocumentGdat = new TreeMap();
        private final Map _hrNames = new HashMap();
        
        private final Map _staffs = new HashMap();
        private String _remark2;
        private String _schoolName;
        private String _mockGroupName;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _executeDate = request.getParameter("DATE").replace('/', '-');
            
            _groupDiv = request.getParameter("GROUP_DIV");
            _targetDiv = request.getParameter("TARGET_DIV");
            _stfAuthCd = request.getParameter("STF_AUTH_CD");
            _groupCd = request.getParameter("GROUPCD");

            _isCategoryStudent = "2".equals(request.getParameter("CATEGORY_IS_CLASS"));
            _useAddress = request.getParameter("OKURIJOU_JUSYO");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            if (_isCategoryStudent) {
                _hrclass = request.getParameter("HR_CLASS");
            } else {
                _hrclass = null;
            }
            _useAddrField2 = request.getParameter("useAddrField2");
            setMockNames(db2);
            setMockSubclassNames(db2);
            setHrNames(db2);
            setCertifSchool(db2);
            setRegdHdatStaff(db2);
            setDocumentGdat(db2);
        }
        
        private void setDocumentGdat(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _mockDocumentGdat.clear();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.MOCK_SUBCLASS_CD, ");
                stb.append("     T1.REMARK1, ");
                stb.append("     T1.REMARK2 ");
                stb.append(" FROM ");
                stb.append("     MOCK_DOCUMENT_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GROUP_DIV = '" + _groupDiv + "' ");
                stb.append("     AND T1.STF_AUTH_CD = '" + _stfAuthCd + "' ");
                stb.append("     AND T1.GROUPCD = '" + _groupCd + "' ");
                stb.append("     AND T1.GRADE = '" + _grade + "' ");
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String mockSubclassCd = rs.getString("MOCK_SUBCLASS_CD");
                    String remark = SUBCLASSALL.equals(rs.getString("MOCK_SUBCLASS_CD")) ? rs.getString("REMARK2") : rs.getString("REMARK1");
                    _mockDocumentGdat.put(mockSubclassCd, remark);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void setMockNames(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _mockNames.clear();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.GROUP_DIV, T1.TARGET_DIV, T1.STF_AUTH_CD, T1.GROUPCD, T1.MOCK_TARGET_CD AS MOCK_CD, ");
                stb.append("         T2.GROUPNAME1, T3.MOCKNAME2 ");
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
                final String sql = stb.toString();
                // log.debug(" sql mockname = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mockGroupName = rs.getString("GROUPNAME1"); 
                    String mockCd = rs.getString("MOCK_CD");
                    String name = rs.getString("MOCKNAME2");
                    _mockNames.put(mockCd, name);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (0 != _mockNames.size()) {
                _mockCds = new String[_mockNames.keySet().size()];
                int i = 0;
                for (Iterator it = _mockNames.keySet().iterator(); it.hasNext();) {
                    String mockCd = (String) it.next();
                    _mockCds[i] = mockCd;
                    i += 1;
                }
            }
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
                stb.append(" SELECT GRADE, HR_CLASS, HR_CLASS_NAME1 AS HR_NAME ");
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
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year=t2.year AND t1.tr_cd1=t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("staffname");
                    _staffs.put(code, name);
                }
            } catch (final SQLException e) {
                log.warn("担任名の取得でエラー");
            } finally {
                DbUtils.closeQuietly(rs);
            }
        }

        private void setCertifSchool(final DB2UDB db2) throws SQLException {
            final String key = (Integer.parseInt(_grade) < 4) ? "110" : "109";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT school_name, remark2 FROM certif_school_dat"
                    + " WHERE year='" + _year + "' AND certif_kindcd='" + key + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("school_name");
                    _remark2 = rs.getString("remark2");
                }
            } catch (final SQLException e) {
                log.error("学校名取得エラー。");
                throw e;
            }
        }
        
        private boolean isJunior() {
            return Integer.parseInt(_grade) < 4;
        }

        private int getGradeInt() {
            return isJunior() ? Integer.parseInt(_grade) : Integer.parseInt(_grade) - 3;
        }

        public String getGradeName() {
            return "第" + String.valueOf(getGradeInt()) + "学年";
        }
    }
}

// eof

