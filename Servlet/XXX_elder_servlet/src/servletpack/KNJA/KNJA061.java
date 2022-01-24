// kanji=漢字
/*
 * $Id: 16c249423f748c931d64b404186302491705827a $
 *
 * 作成日: 2013/12/11 11:09:53 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 16c249423f748c931d64b404186302491705827a $
 */
public class KNJA061 {

    private static final Log log = LogFactory.getLog("KNJA061.class");

    private boolean _hasData;
    private static final String EXAM_TYPE4 = "2";
    private static final int MAX_LINE = 13;

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

    private int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = getStudentList(db2);
        svf.VrSetForm("KNJA061.frm", 1);
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            svf.VrsOut("COURSE_NAME", student._coursename + "　" + student._majorname + "　" + student._coursecodename);
            svf.VrsOut("HR_NAME", student._hrName + "　" + student._attendno + "番");
            svf.VrsOut("TEACHER", "担任：" + student._staffname);

            svf.VrsOut("KANA1", student._nameKana);
            svf.VrsOut("NAME1", student._name);
            svf.VrsOut("SEX", student._sexAbbv1);
            svf.VrsOut("FINSCHOOL_NAME", student._finschoolName);
            svf.VrsOut("ZIPNO1", student._zipcd);
            svf.VrsOut("TELNO1", student._telno);
            if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(student._addr1) > 54 || getMS932ByteLength(student._addr2) > 54)) {
                svf.VrsOut("ADDR1_1_2", student._addr1);
                svf.VrsOut("ADDR1_2_2", student._addr2);
            } else {
                svf.VrsOut("ADDR1_1", student._addr1);
                svf.VrsOut("ADDR1_2", student._addr2);
            }
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(student._birthday));
            svf.VrsOut("ADDR1_3", student._areacdName1);

            svf.VrsOut("KANA2", student._guardKana);
            svf.VrsOut("NAME2", student._guardName);
            svf.VrsOut("ZIPNO2", student._guardZipcd);
            svf.VrsOut("TELNO2", student._guardTelno);
            if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(student._guardAddr1) > 54 || getMS932ByteLength(student._guardAddr2) > 54)) {
                svf.VrsOut("ADDR2_1_2", student._guardAddr1);
                svf.VrsOut("ADDR2_2_2", student._guardAddr2);
            } else {
                svf.VrsOut("ADDR2_1", student._guardAddr1);
                svf.VrsOut("ADDR2_2", student._guardAddr2);
            }

            _hasData = true;
            svf.VrEndPage();
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String telno = rs.getString("TELNO");
                final String guardName = rs.getString("GUARD_NAME");
                final String guardKana = rs.getString("GUARD_KANA");
                final String guardZipcd = rs.getString("GUARD_ZIPCD");
                final String guardAddr1 = rs.getString("GUARD_ADDR1");
                final String guardAddr2 = rs.getString("GUARD_ADDR2");
                final String guardTelno = rs.getString("GUARD_TELNO");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String staffname = rs.getString("STAFFNAME");
                final String coursename = rs.getString("COURSENAME") != null ? rs.getString("COURSENAME") : "";
                final String majorname = rs.getString("MAJORNAME") != null ? rs.getString("MAJORNAME") : "";
                final String coursecodename = rs.getString("COURSECODENAME") != null ? rs.getString("COURSECODENAME") : "";
                final String sexAbbv1 = rs.getString("SEX_ABBV1");
                final String areacdName1 = rs.getString("AREACD_NAME1");

                final Student student = new Student(schregno, attendno, hrName, name, nameKana, birthday, zipcd, addr1, addr2, telno, guardName, guardKana, guardZipcd, guardAddr1, guardAddr2, guardTelno, finschoolName, staffname, coursename, majorname, coursecodename, sexAbbv1, areacdName1);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_SCHREG_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), SCHREG_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         T4.* ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT T4 ");
        stb.append("         INNER JOIN MAX_SCHREG_ADDRESS W4 ");
        stb.append("             ON  W4.SCHREGNO  = T4.SCHREGNO ");
        stb.append("             AND W4.ISSUEDATE = T4.ISSUEDATE ");
        stb.append(" ), MAX_GUARDIAN_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), GUARDIAN_ADDRESS AS ( ");
        stb.append("     SELECT ");
        stb.append("         T7.* ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_ADDRESS_DAT T7 ");
        stb.append("         INNER JOIN MAX_GUARDIAN_ADDRESS W7 ");
        stb.append("             ON  W7.SCHREGNO  = T7.SCHREGNO ");
        stb.append("             AND W7.ISSUEDATE = T7.ISSUEDATE ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     smallint(T1.ATTENDNO) AS ATTENDNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T2.TR_CD1, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.NAME_KANA, ");
        stb.append("     T3.BIRTHDAY, ");
        stb.append("     T3.SEX, ");
        stb.append("     T3.FINSCHOOLCD, ");
        stb.append("     T4.ZIPCD, ");
        stb.append("     T4.AREACD, ");
        stb.append("     T4.ADDR1, ");
        stb.append("     T4.ADDR2, ");
        stb.append("     T4.TELNO, ");
        stb.append("     T6.GUARD_NAME, ");
        stb.append("     T6.GUARD_KANA, ");
        stb.append("     CASE WHEN T7.SCHREGNO IS NULL THEN T6.GUARD_ZIPCD ELSE T7.GUARD_ZIPCD END AS GUARD_ZIPCD, ");
        stb.append("     CASE WHEN T7.SCHREGNO IS NULL THEN T6.GUARD_ADDR1 ELSE T7.GUARD_ADDR1 END AS GUARD_ADDR1, ");
        stb.append("     CASE WHEN T7.SCHREGNO IS NULL THEN T6.GUARD_ADDR2 ELSE T7.GUARD_ADDR2 END AS GUARD_ADDR2, ");
        stb.append("     CASE WHEN T7.SCHREGNO IS NULL THEN T6.GUARD_TELNO ELSE T7.GUARD_TELNO END AS GUARD_TELNO, ");
        stb.append("     VALUE(M1.FINSCHOOL_NAME, T3.REMARK3) AS FINSCHOOL_NAME, ");
        stb.append("     M2.STAFFNAME, ");
        stb.append("     M3.COURSENAME, ");
        stb.append("     M4.MAJORNAME, ");
        stb.append("     M5.COURSECODENAME, ");
        stb.append("     N1.ABBV1 AS SEX_ABBV1, ");
        stb.append("     N2.NAME1 AS AREACD_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_ADDRESS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST M1 ON M1.FINSCHOOLCD = T3.FINSCHOOLCD ");
        stb.append("     LEFT JOIN STAFF_MST M2 ON M2.STAFFCD = T2.TR_CD1 ");
        stb.append("     LEFT JOIN COURSE_MST M3 ON M3.COURSECD = T1.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST M4 ON M4.COURSECD = T1.COURSECD AND M4.MAJORCD  = T1.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST M5 ON M5.COURSECODE = T1.COURSECODE ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A020' AND N2.NAMECD2 = T4.AREACD ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR     = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._choice)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + _param._selectedIn + " ");
        } else {
            stb.append("     AND T1.SCHREGNO IN " + _param._selectedIn + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒 */
    private class Student {
        private final String _schregno;
        private final String _attendno;
        private final String _hrName;
        private final String _name;
        private final String _nameKana;
        private final String _birthday;
        private final String _zipcd;
        private final String _addr1;
        private final String _addr2;
        private final String _telno;
        private final String _guardName;
        private final String _guardKana;
        private final String _guardZipcd;
        private final String _guardAddr1;
        private final String _guardAddr2;
        private final String _guardTelno;
        private final String _finschoolName;
        private final String _staffname;
        private final String _coursename;
        private final String _majorname;
        private final String _coursecodename;
        private final String _sexAbbv1;
        private final String _areacdName1;

        Student(final String schregno,
                final String attendno,
                final String hrName,
                final String name,
                final String nameKana,
                final String birthday,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno,
                final String guardName,
                final String guardKana,
                final String guardZipcd,
                final String guardAddr1,
                final String guardAddr2,
                final String guardTelno,
                final String finschoolName,
                final String staffname,
                final String coursename,
                final String majorname,
                final String coursecodename,
                final String sexAbbv1,
                final String areacdName1
        ) throws SQLException {
            _schregno = schregno;
            _attendno = attendno;
            _hrName = hrName;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
            _guardName = guardName;
            _guardKana = guardKana;
            _guardZipcd = guardZipcd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardTelno = guardTelno;
            _finschoolName = finschoolName;
            _staffname = staffname;
            _coursename = coursename;
            _majorname = majorname;
            _coursecodename = coursecodename;
            _sexAbbv1 = sexAbbv1;
            _areacdName1 = areacdName1;
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
        log.fatal("$Revision: 66719 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _choice;
        private String _selectedIn = "";
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _choice = request.getParameter("CHOICE");
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年＋組または学籍番号
            _selectedIn = "(";
            for (int i = 0; i < categorySelected.length; i++) {
                if (categorySelected[i] == null)
                    break;
                if (i > 0)
                    _selectedIn = _selectedIn + ",";
                _selectedIn = _selectedIn + "'" + categorySelected[i] + "'";
            }
            _selectedIn = _selectedIn + ")";

            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useAddrField2 = request.getParameter("useAddrField2");
        }
    }
}

// eof
