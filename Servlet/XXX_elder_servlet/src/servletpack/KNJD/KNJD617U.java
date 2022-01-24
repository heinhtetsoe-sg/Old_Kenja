/*
 * $Id: 8198245b2b101ab06b56c087411f85f2216be97d $
 *
 * 作成日: 2017/08/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD617U {

    private static final Log log = LogFactory.getLog(KNJD617U.class);

    private static final String GAKKIMATSU = "9";

    private static final String CLASSCD_ALL = "99";
    private static final String CURRICULUM_ALL = "99";
    private static final String SUBCLASSCD_ALL = "999999";

    private boolean _hasData;

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
        svf.VrSetForm("KNJD617U.frm", 1);
        final int maxLineCnt = 40;
        final int maxTableCnt = 5;
        String befCourse = "";
        int lineCnt = 1;
        int tableCnt = 1;
        final List studentList = getStudentList(db2);
        for (int line = 0; line < studentList.size(); line++) {
            final Student student = (Student) studentList.get(line);
            final String cmc = student._coursecd + student._majorcd + student._coursecode;
            if ("2".equals(_param._groupDiv) && !"".equals(befCourse) && !befCourse.equals(cmc)) {
                svf.VrEndPage();
                svf.VrSetForm("KNJD617U.frm", 1);
                lineCnt = 1;
                tableCnt = 1;
            }
            if (lineCnt > maxLineCnt) {
                lineCnt = 1;
                tableCnt++;
            }
            if (tableCnt > maxTableCnt) {
                svf.VrEndPage();
                lineCnt = 1;
                tableCnt = 1;
            }
            if ("1".equals(_param._groupDiv)) {
                setTitle(svf, _param._gradeName, studentList.size());
            } else {
                final Title title = (Title) _param._coureMap.get(cmc);
                setTitle(svf, title._title, title._cnt);
            }
            svf.VrsOutn("RANK" + tableCnt, lineCnt, student._rank);
            BigDecimal avgDecimal = new BigDecimal(student._avg);
            svf.VrsOutn("AVE" + tableCnt, lineCnt, String.valueOf(avgDecimal.setScale(1, BigDecimal.ROUND_HALF_UP)));
            svf.VrsOutn("HR_NAME" + tableCnt, lineCnt, student._hrClassName1);
            svf.VrsOutn("NO" + tableCnt, lineCnt, student._attendno);
            final String nameField = getMS932ByteLength(student._name) > 30 ? "_3" : getMS932ByteLength(student._name) > 18 ? "_2" : "_1";
            svf.VrsOutn("NAME" + tableCnt + nameField, lineCnt, student._name);
            svf.VrsOutn("SEX" + tableCnt, lineCnt, student._sex);
            befCourse = cmc;
            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void setTitle(final Vrw32alp svf, final String subTitle, final int ninzu) {
        final String setGroupTitle = "1".equals(_param._groupDiv) ? "学年" : "コース";
        svf.VrsOut("TITLE", _param._now + "年度" + "　" + _param._schoolkindName + "　" + _param._semesterName + "　" + _param._testName + "　" + setGroupTitle + "別定期試験順位表");
        svf.VrsOut("GRADE", subTitle);
        svf.VrsOut("SUM", ninzu + "　名");
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String majorName = rs.getString("MAJORNAME");
                final String coursecodeName = rs.getString("COURSECODENAME");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String hrName = rs.getString("HR_NAME");
                final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                final String rank = rs.getString(_param._rankField + "_RANK");
                final String avg = rs.getString("AVG");
                final Student student = new Student(schregno, grade, hrClass, attendno, coursecd, majorcd, coursecode, majorName, coursecodeName, name, sex, hrName, hrClassName1, rank, avg);
                retList.add(student);
                if (_param._coureMap.containsKey(coursecd + majorcd + coursecode)) {
                    final Title title = (Title) _param._coureMap.get(coursecd + majorcd + coursecode);
                    title._cnt++;
                } else {
                    final Title title = new Title(majorName + coursecodeName);
                    title._cnt++;
                    _param._coureMap.put(coursecd + majorcd + coursecode, title);
                }
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECORD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     VALUE(MAJOR.MAJORNAME, '') AS MAJORNAME, ");
        stb.append("     VALUE(COURSECODE.COURSECODENAME, '') AS COURSECODENAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_CLASS_NAME1, ");
        stb.append("     RECORD." + _param._rankField + "_RANK, ");
        stb.append("     RECORD.AVG ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT RECORD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON RECORD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON RECORD.YEAR = REGD.YEAR ");
        stb.append("          AND REGD.SEMESTER = '" + _param.getSeme() + "' ");
        stb.append("          AND RECORD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("          AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON REGD.COURSECD = MAJOR.COURSECD ");
        stb.append("          AND REGD.MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE ON REGD.COURSECODE = COURSECODE.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     RECORD.YEAR = '" + _param._year + "' ");
        stb.append("     AND RECORD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND RECORD.TESTKINDCD || RECORD.TESTITEMCD || RECORD.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("     AND RECORD.CLASSCD = '" + CLASSCD_ALL + "' ");
        stb.append("     AND RECORD.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("     AND RECORD.CURRICULUM_CD = '" + CURRICULUM_ALL + "' ");
        stb.append("     AND RECORD.SUBCLASSCD = '" + SUBCLASSCD_ALL + "' ");
        stb.append(" ORDER BY ");
        if ("2".equals(_param._groupDiv)) {
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE, ");
        }
        if ("2".equals(_param._sortDiv)) {
            stb.append("     RECORD." + _param._rankField + "_RANK, ");
        }
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _majorName;
        final String _coursecodeName;
        final String _name;
        final String _sex;
        final String _hrName;
        final String _hrClassName1;
        final String _rank;
        final String _avg;
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String majorName,
                final String coursecodeName,
                final String name,
                final String sex,
                final String hrName,
                final String hrClassName1,
                final String rank,
                final String avg
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _majorName = majorName;
            _coursecodeName = coursecodeName;
            _name = name;
            _sex = sex;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _rank = rank;
            _avg = avg;
        }
    }

    /** タイトルクラス */
    private class Title {
        final String _title;
        int _cnt = 0;
        public Title(
                final String title
        ) {
            _title = title;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 68749 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _major;
        final String _grade;
        final String _testcd;
        final String _groupDiv;
        final String _rankField;
        final String _sortDiv;
        final String _year;
        final String _ctrlSeme;
        final String _loginDate;
        final String _prgid;
        final String _subclassGroup;
        final String _usecurriculumcd;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _useSchoolDetailGcmDat;
        final String _useSchoolKindField;
        final String _paraSchoolkind;
        final String _gradeName;
        final String _schoolkind;
        final String _schoolkindName;
        final String _schoolcd;
        final String _printLogStaffcd;
        final String _semesterName;
        final String _testName;
        final String _now;
        final String _ctrlDate;
        Map _coureMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _major = request.getParameter("MAJOR");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _groupDiv = request.getParameter("GROUP_DIV");
            _rankField = "1".equals(_groupDiv) ? "GRADE_AVG" : "COURSE_AVG";
            _sortDiv = request.getParameter("SORT_DIV");
            _year = request.getParameter("YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _subclassGroup = request.getParameter("SUBCLASS_GROUP");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _useSchoolDetailGcmDat = request.getParameter("use_school_detail_gcm_dat");
            _useSchoolKindField = request.getParameter("useSchool_KindField");
            _paraSchoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            final String[] schoolkindData = getSchoolKind(db2);
            _gradeName = schoolkindData[0];
            _schoolkind = schoolkindData[1];
            _schoolkindName = schoolkindData[2];
            _semesterName = getSemesterName(db2);
            _testName = getTestName(db2);
            _now = StringUtils.replace(getNow(db2), "null", "");
        }

        private String[] getSchoolKind(final DB2UDB db2) throws SQLException {
            String[] retStr = {"", "", ""};
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolKind();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr[0] = rs.getString("GRADE_NAME1");
                    retStr[1] = rs.getString("SCHOOL_KIND");
                    retStr[2] = rs.getString("ABBV1");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolKind() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     A023.ABBV1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
            stb.append("          AND GDAT.SCHOOL_KIND = A023.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _year + "' ");
            stb.append("     AND GDAT.GRADE = '" + _grade + "' ");
            return stb.toString();
        }

        public String getSeme() {
            return GAKKIMATSU.equals(_semester) ? _ctrlSeme : _semester;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemester();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemester() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            return stb.toString();
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getTestNameSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("TESTITEMNAME");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getTestNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME ");
            stb.append(" FROM ");
            if ("1".equals(_useSchoolDetailGcmDat)) {
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ");
            } else {
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV ");
            }
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ");
            if ("1".equals(_useSchoolDetailGcmDat)) {
                stb.append("             AND GRADE = '00' ");
                stb.append("             AND COURSECD || '-' || MAJORCD = '" + _major + "' ");
                if ("1".equals(_useSchoolKindField)) {
                    stb.append(" AND SCHOOL_KIND  = '" + _schoolkind + "' ");
                    stb.append(" AND SCHOOLCD     = '" + _schoolcd + "' ");
                }
            }

            return stb.toString();
        }
        
        private String getNow(final DB2UDB db2) {
        	final boolean isSeireki = KNJ_EditDate.isSeireki(db2);
    		final Calendar cal = Calendar.getInstance();
    		cal.setTime(Date.valueOf(_ctrlDate));
    		final String nengo;
    		final String nen;
        	if (isSeireki) {
        		nengo = "";
        		nen = String.valueOf(cal.get(Calendar.YEAR));
        	} else {
            	final String[] tate = KNJ_EditDate.tate_format4(db2, _ctrlDate.replace('/', '-'));
            	nengo = tate[0];
            	nen = tate[1];
        	}

        	return nengo + nen;
        }


    }
}

// eof

