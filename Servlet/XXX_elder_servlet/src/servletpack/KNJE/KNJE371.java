// kanji=漢字
/*
 * 作成日: 2010/06/28 13:39:41 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJE371 {

    private static final Log log = LogFactory.getLog("KNJE371.class");

    private boolean _hasData;

    Param _param;

    private static final String FORM_FILE = "KNJE371.frm";

    private static final int MAX_LINE = 50;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        svf.VrSetForm(FORM_FILE, 4);
        int line = 0;
        final List hrclassList = createHrClass(db2);
        for (final Iterator it = hrclassList.iterator(); it.hasNext();) {
            final HrClass hrclass = (HrClass) it.next();

            log.debug("hrName = " + hrclass._hrName);
            printHeader(svf, hrclass);
            line = printStudent(svf, hrclass);
            log.debug("line = " + line);
            line = printKaraLine(svf, line);
            log.debug("line = " + line);
            line = printFooter(svf, hrclass);
            log.debug("line = " + line);
        }
    }

    private void printHeader(final Vrw32alp svf, final HrClass hrclass) {
        svf.VrsOut("GRAD_NENDO" , _param._nendo );
        svf.VrsOut("HR_NAME"    , hrclass._hrName );
        svf.VrsOut("STAFFNAME"  , hrclass._staffName );
        if (_param._isKindai) {
            svf.VrsOut("REMARK_ITEM"  , "海外の学校・予備校等");
        }
        //実人数をカウント（表枠外）
        for (int ii = 0; ii < 4; ii++) {
            int cnt = hrclass.totalWakugai[ii];
            String field = "";
            if (ii == 1) field = "BOY";
            if (ii == 2) field = "GIRL";
            if (ii == 0) field = "TOTAL";
            if (ii == 3) field = "CLASS_TOTAL";
            svf.VrsOut(field    , String.valueOf(cnt) );
        }
    }

    private int printStudent(final Vrw32alp svf, final HrClass hrclass) {
        int line = 0;
        boolean lineFlg = false;
        for (final Iterator it = hrclass._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (line == MAX_LINE) {
                log.debug("line = " + line);
                line = printFooter(svf, hrclass);
                log.debug("line = " + line);
            }

            if (line % 5 == 0) {
                lineFlg = !lineFlg;
            }

            String no = (line % 5 == 4) ? "2" : "1";
            svf.VrsOut("ATTENDNO" + no  , student.getAttendNo() );
            svf.VrsOut("NAME"     + no  , student._name );

            if (0 < student._aftGradCourseList.size()) {
                line = printAftGradCourse(svf, hrclass, student, line);
            } else {
                svf.VrEndRecord();
                line++;
            }

            _hasData = true;
        }
        return line;
    }

    private int printAftGradCourse(final Vrw32alp svf, final HrClass hrclass, final Student student, int line) {
        for (final Iterator it = student._aftGradCourseList.iterator(); it.hasNext();) {
            final AftGradCourse aftGradCourse = (AftGradCourse) it.next();

            if (line == MAX_LINE) {
                log.debug("line = " + line);
                line = printFooter(svf, hrclass);
                log.debug("line = " + line);
            }

            String no = (line % 5 == 4) ? "2" : "1";
            if (aftGradCourse.isCollege()) {
                svf.VrsOut("COLLEGE"    + no + getFieldNo(aftGradCourse._schoolName)        , aftGradCourse._schoolName );
                svf.VrsOut("FACULTY"    + no + getFieldNo(aftGradCourse._facultyName)       , aftGradCourse._facultyName );
                svf.VrsOut("DEPARTMENT" + no + getFieldNo(aftGradCourse._departmentName)    , aftGradCourse._departmentName );

            } else if (aftGradCourse.isSenmonkou()) {
                svf.VrsOut("SPE_TRANING" + no + getFieldNo(aftGradCourse._schoolName)       , aftGradCourse._schoolName );

            } else if (aftGradCourse.isCompany()) {
                svf.VrsOut("COMPANY"    + no + getFieldNo(aftGradCourse._companyName)       , aftGradCourse._companyName );

            } else if (aftGradCourse.isYobikou()) {
                svf.VrsOut("REMARK"     + no + getFieldNo(aftGradCourse._schoolName)        , aftGradCourse._schoolName );

            }
            svf.VrEndRecord();
            line++;
        }
        return line;
    }

    private String getFieldNo(String str) {
        if (null == str) return "_1";
        if (20 < str.length()) return "_4";
        if (15 < str.length()) return "_3";
        if (10 < str.length()) return "_2";
        return "_1";
    }

    private int printFooter(final Vrw32alp svf, final HrClass hrclass) {
        for (int i = 2; i >= 0; i--) {
            String item = "";
            if (i == 2) item = "女子小計";
            if (i == 1) item = "男子小計";
            if (i == 0) item = "　合計　";
            svf.VrsOut("ITEM_TOTAL"         , item );
            for (int ii = 0; ii < 4; ii++) {
                int cnt = hrclass.total[i][ii];
                String field = "";
                if (ii == 1) field = "COLLEGE_CNT";
                if (ii == 2) field = "SPE_TRANING_CNT";
                if (ii == 0) field = "COMPANY_CNT";
                if (ii == 3) field = "REMARK_CNT";
                svf.VrsOut(field    , String.valueOf(cnt) );
            }
            svf.VrEndRecord();
        }
        return 0;
    }

    private int printKaraLine(final Vrw32alp svf, int line) {
        while (line < MAX_LINE) {
            String no = (line % 5 == 4) ? "2" : "1";
            svf.VrsOut("KARA" + no      , String.valueOf(line) );
            svf.VrEndRecord();
            line++;
        }
        return line;
    }

    private List createHrClass(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            final String selected = _param._classSelected[i];
            final String sql = sqlHrClass(selected);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");

                    final HrClass hrclass = new HrClass(grade, hrClass, hrName, staffName);
                    hrclass.load(db2);
                    rtn.add(hrclass);
                }
            } catch (final Exception ex) {
                log.error("クラスのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        return rtn;
    }

    private String sqlHrClass(final String selected) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("              ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("              ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T3.GRADE = T1.GRADE ");
        stb.append("             AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L1 ");
        stb.append("              ON L1.STAFFCD = T3.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _staffName;

        private List _studentList;
        private int total[][] = {{0,0,0,0},{0,0,0,0},{0,0,0,0}}; //延べ人数をカウント（縦計）
        private int totalWakugai[] = {0,0,0,0}; //実人数をカウント（表枠外）

        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffName = staffName;
        }

        private void load(final DB2UDB db2) throws SQLException {
            _studentList = createStudent(db2);
            setTotal();
        }

        private List createStudent(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String selected = _grade + _hrClass;
            final String sql = sqlStudent(selected);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");

                    final Student student = new Student(schregno, grade, hrClass, attendNo, name, sex);
                    student.load(db2);
                    rtn.add(student);
                }
            } catch (final Exception ex) {
                log.error("生徒のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlStudent(final String selected) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ");
            stb.append("              ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ");
            stb.append("              ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T3.GRADE = T1.GRADE ");
            stb.append("             AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");
            return stb.toString();
        }

        private void setTotal() {
            try {
                for (int i = 0; i < 3; i++) {
                    for (int ii = 0; ii < 4; ii++) {
                        total[i][ii] = 0;
                        totalWakugai[ii] = 0;
                    }
                }
                for (final Iterator its = _studentList.iterator(); its.hasNext();) {
                    final Student student = (Student) its.next();
                    final int sex = Integer.parseInt(student._sex);
                    //延べ人数をカウント（縦計）
                    for (final Iterator ita = student._aftGradCourseList.iterator(); ita.hasNext();) {
                        final AftGradCourse aftGradCourse = (AftGradCourse) ita.next();
                        if (aftGradCourse.isCollege()) {
                            total[sex][1] += 1;
                            total[0][1] += 1;
                        } else if (aftGradCourse.isSenmonkou()) {
                            total[sex][2] += 1;
                            total[0][2] += 1;
                        } else if (aftGradCourse.isCompany()) {
                            total[sex][0] += 1;
                            total[0][0] += 1;
                        } else if (aftGradCourse.isYobikou()) {
                            total[sex][3] += 1;
                            total[0][3] += 1;
                        }
                    }
                    //実人数をカウント（表枠外）
                    if (0 < student._aftGradCourseList.size()) {
                        //決定人数
                        totalWakugai[sex] += 1;
                        totalWakugai[0]   += 1;
                    }
                    //クラス人数
                    totalWakugai[3] += 1;
                }
            } catch (final Exception ex) {
                log.error("合計でエラー", ex);
            }
        }

        public String toString() {
            return _grade + _hrClass + ":" + _hrName;
        }
    }

    private class Student {
        private final String _schregno;
        private final String _grade;
        private final String _hrClass;
        private final String _attendNo;
        private final String _name;
        private final String _sex;

        private List _aftGradCourseList;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String name,
                final String sex
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
        }

        private String getAttendNo() {
            return (null == _attendNo) ? "" : String.valueOf(Integer.parseInt(_attendNo));
        }

        private void load(final DB2UDB db2) throws SQLException {
            _aftGradCourseList = createAftGradCourse(db2);
        }

        private List createAftGradCourse(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlAftGradCourse(_schregno);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String senkouKind = rs.getString("SENKOU_KIND");
                    final String planstat = rs.getString("PLANSTAT");
                    final String taishouFlg = rs.getString("TAISHOU_FLG");
                    final String seq = rs.getString("SEQ");
                    final String statCd = rs.getString("STAT_CD");
                    final String companyName = rs.getString("COMPANY_NAME");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String facultyCd = rs.getString("FACULTYCD");
                    final String facultyName = rs.getString("FACULTYNAME");
                    final String departmentCd = rs.getString("DEPARTMENTCD");
                    final String departmentName = rs.getString("DEPARTMENTNAME");

                    final AftGradCourse aftGradCourse = new AftGradCourse(schregno, senkouKind, planstat, taishouFlg, seq, statCd, companyName, schoolName, facultyCd, facultyName, departmentCd, departmentName);
                    rtn.add(aftGradCourse);
                }
            } catch (final Exception ex) {
                log.error("進路のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlAftGradCourse(final String schno) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.SCHREGNO, ");
            stb.append("     L1.SENKOU_KIND, ");
            stb.append("     L1.PLANSTAT, ");
            stb.append("     N1.NAMESPARE1 AS TAISHOU_FLG, ");
            stb.append("     L1.SEQ, ");
            stb.append("     L1.STAT_CD, ");
            stb.append("     L9.COMPANY_NAME, ");
            stb.append("     L2.SCHOOL_NAME, ");
            stb.append("     L1.FACULTYCD, ");
            stb.append("     L3.FACULTYNAME, ");
            stb.append("     L1.DEPARTMENTCD, ");
            stb.append("     L4.DEPARTMENTNAME ");
            stb.append(" FROM ");
            stb.append("     AFT_GRAD_COURSE_DAT L1  ");
            stb.append("     LEFT JOIN COMPANY_MST L9 ON L9.COMPANY_CD = L1.STAT_CD ");
            stb.append("     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.STAT_CD ");
            stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L3 ON L3.SCHOOL_CD = L1.STAT_CD ");
            stb.append("                                     AND L3.FACULTYCD = L1.FACULTYCD ");
            stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L4 ON L4.SCHOOL_CD = L1.STAT_CD ");
            stb.append("                                        AND L4.FACULTYCD = L1.FACULTYCD ");
            stb.append("                                        AND L4.DEPARTMENTCD = L1.DEPARTMENTCD ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'E012' ");
            stb.append("                          AND N1.NAMECD2 = L2.SCHOOL_GROUP ");
            stb.append(" WHERE ");
            stb.append("         L1.YEAR = '" + _param._year + "' ");
            stb.append("     AND L1.SCHREGNO = '" + schno + "' ");
            stb.append("     AND L1.PLANSTAT = '1' ");
            stb.append("     AND ((L1.SENKOU_KIND = '0' AND N1.NAMESPARE1 IN ('1','2','3')) OR (L1.SENKOU_KIND IN ('1','2'))) ");
            stb.append(" ORDER BY ");
            stb.append("     L1.SENKOU_KIND, ");
            stb.append("     N1.NAMESPARE1 ");
            return stb.toString();
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private class AftGradCourse {
        private final String _schregno;
        private final String _senkouKind;
        private final String _planstat;
        private final String _taishouFlg;
        private final String _seq;
        private final String _statCd;
        private final String _companyName;
        private final String _schoolName;
        private final String _facultyCd;
        private final String _facultyName;
        private final String _departmentCd;
        private final String _departmentName;

        public AftGradCourse(
                final String schregno,
                final String senkouKind,
                final String planstat,
                final String taishouFlg,
                final String seq,
                final String statCd,
                final String companyName,
                final String schoolName,
                final String facultyCd,
                final String facultyName,
                final String departmentCd,
                final String departmentName
        ) {
            _schregno = schregno;
            _senkouKind = senkouKind;
            _planstat = planstat;
            _taishouFlg = taishouFlg;
            _seq = seq;
            _statCd = statCd;
            _companyName = companyName;
            _schoolName = schoolName;
            _facultyCd = facultyCd;
            _facultyName = facultyName;
            _departmentCd = departmentCd;
            _departmentName = departmentName;
        }

        private boolean isCompany() {
            return "1".equals(_senkouKind) || "2".equals(_senkouKind);
        }

        private boolean isCollege() {
            return "0".equals(_senkouKind) && "1".equals(_taishouFlg);
        }

        private boolean isSenmonkou() {
            return "0".equals(_senkouKind) && "2".equals(_taishouFlg);
        }

        private boolean isYobikou() {
            return "0".equals(_senkouKind) && "3".equals(_taishouFlg);
        }
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
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String[] _classSelected;
        private final String _nendo;
        private final boolean _isKindai;

        //系列大学
        private String _mainCollegeCode;
        private String _mainCollegeName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _nendo = gengou + "年度";
            setMainCollege(db2);
            String z010 = getZ010(db2);
            _isKindai = "KINDAI".equals(z010);
        }

        private String getMainCollegeName() {
            return (null == _mainCollegeName) ? "" : _mainCollegeName;
        }

        private void setMainCollege(final DB2UDB db2) throws SQLException {
            _mainCollegeCode = null;
            _mainCollegeName = null;
            final String sql = sqlMainCollege();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _mainCollegeCode = rs.getString("ABBV3");
                    _mainCollegeName = rs.getString("SCHOOL_NAME");
                }
            } catch (final Exception ex) {
                log.error("系列大学のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlMainCollege() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L1.ABBV3, ");
            stb.append("     L2.SCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L1 ");
            stb.append("     LEFT JOIN COLLEGE_MST L2 ON L2.SCHOOL_CD = L1.ABBV3 ");
            stb.append(" WHERE ");
            stb.append("     L1.NAMECD1 = 'Z010' AND ");
            stb.append("     L1.NAMECD2 = '00' ");
            return stb.toString();
        }

        private String getZ010(final DB2UDB db2) {
            String rtn = "";

            final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (final Exception ex) {
                log.error("Z010でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }
    }
}

// eof
