// kanji=漢字
/*
 * 作成日: 2021/01/25
 * 作成者: s-shimoji
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJE373C {

    private static final Log log = LogFactory.getLog(KNJE373C.class);

    private boolean _hasData;

    private Param _param;

    private static final int LINE_MAX = 40;

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
            SQLUtils.whereIn(true, new String[] {});
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
        svf.VrSetForm("KNJE373C.frm", 1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String outputDate = sdf.format(new Date());
        Map<String, Faculty> facultyMap = getFacultyMap(db2);

        int pageCnt = 1;

        for(Faculty faculty : facultyMap.values()) {
            printTitle(svf, pageCnt, outputDate, faculty);
            int lineCnt = 1;

            for(Student student : faculty._studentList) {
                // 改ページの制御
                if (LINE_MAX < lineCnt) {
                    lineCnt = 1;
                    pageCnt++;
                    svf.VrEndPage();
                    printTitle(svf, pageCnt, outputDate, faculty);
                }

                svf.VrsOutn("SCHREGNO", lineCnt, student._schregno);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String nameFieldStr = nameByte > 50 ? "4" : nameByte > 40 ? "3" : nameByte > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, student._name);

                final int kanaByte = KNJ_EditEdit.getMS932ByteLength(student._nameKana);
                final String kanaFieldStr = kanaByte > 50 ? "4" : kanaByte > 40 ? "3" : kanaByte > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaFieldStr, lineCnt, student._nameKana);

                final int faculryByte = KNJ_EditEdit.getMS932ByteLength(student._faculryName);
                final String faculryFieldStr = faculryByte > 30 ? "_3" : faculryByte > 20 ? "_2" : "1";
                svf.VrsOutn("FACULTYCD" + faculryFieldStr, lineCnt, student._faculryName);

                final int department1Byte = KNJ_EditEdit.getMS932ByteLength(student._departmentName);
                final String department1FieldStr = department1Byte > 50 ? "4" : department1Byte > 40 ? "3" : department1Byte > 30 ? "2" : "1";
                svf.VrsOutn("DEPARTMENTCD1_" + department1FieldStr, lineCnt, student._departmentName);


                final int department2Byte = KNJ_EditEdit.getMS932ByteLength(student._sensyu);
                final String department2FieldStr = department2Byte > 50 ? "4" : department2Byte > 40 ? "3" : department2Byte > 30 ? "2" : "1";
                svf.VrsOutn("DEPARTMENTCD2_" + department2FieldStr, lineCnt, student._sensyu);

                lineCnt++;
                _hasData = true;
            }

            pageCnt++;
            svf.VrEndPage();
        }
    }

    /**
     * @param svf
     * @param outputDate
     * @param hrClass
     */
    private void printTitle(final Vrw32alp svf, final int pageCnt, final String outputDate, final Faculty faculty) {
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("HR_NAME", StringUtils.defaultString(faculty._majorName) + "　" + faculty._faculryName);
        svf.VriOut("PAGE", pageCnt);
        svf.VrsOut("DATE", outputDate);
    }

    private Map<String, Faculty> getFacultyMap(final DB2UDB db2) {
        Map<String, Faculty> faculryMap = new LinkedHashMap<String, Faculty>();
        Faculty faculty = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String faculryCd = rs.getString("FACULTYCD");
                final String faculryName = rs.getString("FACULTYNAME");
                final String majorName = rs.getString("MAJORNAME");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String departmentName = rs.getString("DEPARTMENTNAME");
                final String sensyu = rs.getString("SENSYU");

                if (faculryMap.containsKey(faculryCd)) {
                    faculty = faculryMap.get(faculryCd);
                } else {
                    faculty = new Faculty(faculryCd, faculryName, majorName);
                    faculryMap.put(faculryCd, faculty);
                }

                Student student = new Student(schregno, name, nameKana, faculryName, departmentName, sensyu);
                faculty._studentList.add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return faculryMap;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH COLLEGE AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHOOL_CD, ");
        stb.append("         FACULTYCD, ");
        stb.append("         FACULTYNAME ");
        stb.append("     FROM ");
        stb.append("         COLLEGE_FACULTY_MST ");
        stb.append(" ), ");
        stb.append(" DEPARTMENT AS ( ");
        stb.append("     SELECT ");
        stb.append("         COLLEGE_FACULTY_MST.SCHOOL_CD, ");
        stb.append("         COLLEGE_FACULTY_MST.FACULTYCD, ");
        stb.append("         COLLEGE_DEPARTMENT_MST.DEPARTMENTCD, ");
        stb.append("         COLLEGE_DEPARTMENT_MST.DEPARTMENTNAME ");
        stb.append("     FROM ");
        stb.append("         COLLEGE_DEPARTMENT_MST ");
        stb.append("         INNER JOIN COLLEGE_FACULTY_MST ON ");
        stb.append("                    COLLEGE_FACULTY_MST.SCHOOL_CD = COLLEGE_DEPARTMENT_MST.SCHOOL_CD ");
        stb.append("                AND COLLEGE_FACULTY_MST.FACULTYCD = COLLEGE_DEPARTMENT_MST.FACULTYCD ");
        stb.append(" ), ");
        stb.append(" AFT_SCH_MAX AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(SEQ) AS SEQ ");
        stb.append("     FROM ");
        stb.append("         AFT_GRAD_COURSE_DAT ");
        stb.append("     WHERE ");
        stb.append("         SENKOU_KIND = '0' ");
        stb.append("     AND STAT_CD     = '" + _param._schoolCd + "' ");
        stb.append("     AND PLANSTAT    = '1' "); // 1:決定
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO ");
        stb.append(" ), ");
        stb.append(" AFT_SCH AS ( ");
        stb.append("     SELECT ");
        stb.append("         AFT_DAT.YEAR, ");
        stb.append("         AFT_DAT.SCHREGNO, ");
        stb.append("         AFT_DAT.STAT_CD, ");
        stb.append("         AFT_DAT.FACULTYCD, ");
        stb.append("         AFT_DAT.DEPARTMENTCD ");
        stb.append("     FROM ");
        stb.append("         AFT_GRAD_COURSE_DAT AFT_DAT ");
        stb.append("         INNER JOIN AFT_SCH_MAX ON ");
        stb.append("                    AFT_SCH_MAX.YEAR     = AFT_DAT.YEAR ");
        stb.append("                AND AFT_SCH_MAX.SEQ      = AFT_DAT.SEQ ");
        stb.append("                AND AFT_SCH_MAX.SCHREGNO = AFT_DAT.SCHREGNO ");
        stb.append(" ), ");
        stb.append(" MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         VALUE(AFT_SCH.FACULTYCD, 'ZZZZ') AS FACULTYCD, ");
        stb.append("         CASE WHEN AFT_SCH.SCHREGNO IS NOT NULL THEN COLLEGE.FACULTYNAME ELSE '非推薦' END AS FACULTYNAME, ");
        stb.append("         DAT.COURSECD, ");
        stb.append("         DAT.MAJORCD, ");
        stb.append("         DAT.SCHREGNO, ");
        stb.append("         BASE.NAME, ");
        stb.append("         BASE.NAME_KANA, ");
        stb.append("         VALUE(AFT_SCH.DEPARTMENTCD, 'ZZZZ') AS DEPARTMENTCD, ");
        stb.append("         CASE WHEN AFT_SCH.SCHREGNO IS NOT NULL THEN DEPARTMENT.DEPARTMENTNAME ELSE '' END AS DEPARTMENTNAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT DAT ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("                   HDAT.YEAR     = DAT.YEAR ");
        stb.append("               AND HDAT.SEMESTER = DAT.SEMESTER ");
        stb.append("               AND HDAT.GRADE    = DAT.GRADE ");
        stb.append("               AND HDAT.HR_CLASS = DAT.HR_CLASS ");
        stb.append("         LEFT JOIN AFT_SCH ON ");
        stb.append("                   AFT_SCH.YEAR     = DAT.YEAR ");
        stb.append("               AND AFT_SCH.SCHREGNO = DAT.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("                   BASE.SCHREGNO = DAT.SCHREGNO ");
        stb.append("         LEFT JOIN COLLEGE ON ");
        stb.append("                   COLLEGE.SCHOOL_CD = AFT_SCH.STAT_CD ");
        stb.append("               AND COLLEGE.FACULTYCD = AFT_SCH.FACULTYCD ");
        stb.append("         LEFT JOIN DEPARTMENT ON ");
        stb.append("                   DEPARTMENT.SCHOOL_CD    = AFT_SCH.STAT_CD ");
        stb.append("               AND DEPARTMENT.FACULTYCD    = AFT_SCH.FACULTYCD ");
        stb.append("               AND DEPARTMENT.DEPARTMENTCD = AFT_SCH.DEPARTMENTCD ");
        stb.append("     WHERE ");
        stb.append("         DAT.YEAR     = '" + _param._year + "' AND ");
        stb.append("         DAT.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("         DAT.GRADE    = '" + _param._grade + "' ");
        stb.append(" ), ");
        stb.append(" FACULTY_MAJOR AS ( ");
        stb.append("     SELECT ");
        stb.append("         FACULTYCD, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD ");
        stb.append("     FROM ");
        stb.append("         MAIN ");
        stb.append("     GROUP BY ");
        stb.append("         FACULTYCD, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD ");
        stb.append(" ), ");
        stb.append(" MAJOR AS ( ");
        stb.append("     SELECT ");
        stb.append("         FACULTY_MAJOR.FACULTYCD, ");
        stb.append("         LISTAGG(MAJOR.MAJORNAME, '、') WITHIN GROUP (ORDER BY MAJOR.MAJORCD) AS MAJORNAME ");
        stb.append("     FROM ");
        stb.append("         FACULTY_MAJOR ");
        stb.append("         LEFT JOIN MAJOR_MST MAJOR ON ");
        stb.append("                   MAJOR.COURSECD = FACULTY_MAJOR.COURSECD ");
        stb.append("               AND MAJOR.MAJORCD  = FACULTY_MAJOR.MAJORCD ");
        stb.append("     GROUP BY ");
        stb.append("         FACULTY_MAJOR.FACULTYCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     MAIN.SCHREGNO, ");
        stb.append("     MAIN.NAME, ");
        stb.append("     MAIN.NAME_KANA, ");
        stb.append("     MAIN.FACULTYCD, ");
        stb.append("     MAIN.FACULTYNAME, ");
        stb.append("     MAIN.DEPARTMENTCD, ");
        stb.append("     MAIN.DEPARTMENTNAME, ");
        stb.append("     '' AS SENSYU ");
        stb.append(" FROM ");
        stb.append("     MAIN ");
        stb.append("     LEFT JOIN MAJOR ON MAJOR.FACULTYCD = MAIN.FACULTYCD ");
        stb.append(" ORDER BY ");
        stb.append("     MAIN.FACULTYCD, ");
        stb.append("     MAIN.DEPARTMENTCD, ");
        stb.append("     MAIN.SCHREGNO ");
        return stb.toString();
    }

    private class Faculty {
        private final String _faculryCd;
        private final String _faculryName;
        private final String _majorName;
        private final List<Student> _studentList;

        Faculty (final String faculryCd, final String faculryName, final String majorName) {
            _faculryCd   = faculryCd;
            _faculryName = faculryName;
            _majorName   = majorName;
            _studentList = new ArrayList<Student>();
        }
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _nameKana;
        final String _faculryName;
        final String _departmentName;
        final String _sensyu;

        private Student (
            final String schregno,
            final String name,
            final String nameKana,
            final String faculryName,
            final String departmentName,
            final String sensyu
        ) {
            _schregno       = schregno;
            _name           = name;
            _nameKana       = nameKana;
            _faculryName    = faculryName;
            _departmentName = departmentName;
            _sensyu         = sensyu;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _schoolCd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year     = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade    = request.getParameter("GRADE");
            _schoolCd = getSchoolCd(db2);
        }

        private String getSchoolCd(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String schoolCd = null;

            try {
                ps = db2.prepareStatement(" SELECT ABBV3 FROM V_NAME_MST WHERE YEAR='" + _year + "' AND NAMECD1='Z010' AND NAMECD2='00' ");
                rs = ps.executeQuery();

                while (rs.next()) {
                    schoolCd = rs.getString("ABBV3");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return schoolCd;
        }

        private String getHrClassName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String hrClassName = null;

            try {
                ps = db2.prepareStatement("SELECT HR_NAME FROM SCHREG_REGD_HDAT WHERE YEAR='" + _year + "' AND SEMESTER='" + _semester + "' AND GRADE || HR_CLASS = '" + _grade + "' ");
                rs = ps.executeQuery();

                while (rs.next()) {
                    hrClassName = rs.getString("HR_NAME");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return hrClassName;
        }
    }

}
