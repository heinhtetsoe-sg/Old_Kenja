/*
 * $Id: 7795b1b7a7b2946b6d19ef4286a3e32fc4e42d7c $
 *
 * 作成日: 2015/12/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KNJA171A {

    private static final Log log = LogFactory.getLog(KNJA171A.class);

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

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            for (int i = 0; i < _param._categorySelected.length; i++) {
                final String sql = hrSql(_param._categorySelected[i]);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");

                    final HrClass hrClass = new HrClass(cd, hrName, staffName);
                    hrClass.setStudent(db2);
                    retList.add(hrClass);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJA171A.frm", 1);
        final List hrList = getList(db2);
        for (Iterator itHr = hrList.iterator(); itHr.hasNext();) {
            HrClass hrClass = (HrClass) itHr.next();
            int lineCnt = 1;
            int page = 1;
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
            svf.VrsOut("BELONGING_NAME", "クラス：" + hrClass._name + "　担任：" + (null != hrClass._staffName ? hrClass._staffName : ""));
            for (Iterator itStudent = hrClass._studentList.iterator(); itStudent.hasNext();) {
                Student student = (Student) itStudent.next();
                if ("1".equals(_param._zenseki) && "".equals(student._anotherCd)) {
                    continue;
                }
                if (lineCnt > 50) {
                    lineCnt = 1;
                    page++;
                    svf.VrEndPage();
                }
                svf.VrsOut("PAGE", String.valueOf(page));
                svf.VrsOutn("NUMBER", lineCnt, student._attendNo);
                svf.VrsOutn("SCHREGNO", lineCnt, student._schregNo);
                final String nameField = getMS932ByteLength(student._name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
                svf.VrsOutn("SEX", lineCnt, student._sexName);
                svf.VrsOutn("IN_DIV", lineCnt, student._entDivName);
                svf.VrsOutn("IN_DATE", lineCnt, KNJ_EditDate.h_format_JP(student._entDate));
                svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(student._birthday));
                svf.VrsOutn("ZIP", lineCnt, student._zipCd);
                svf.VrsOutn("PREF_NAME", lineCnt, student._pref);
                final String addr = StringUtils.defaultString(student._addr1) + StringUtils.defaultString(student._addr2);
                final String addrField = getMS932ByteLength(addr) > 40 ? "2" : "1";
                svf.VrsOutn("ADDRESS" + addrField, lineCnt, addr);
                svf.VrsOutn("TEL1", lineCnt, student._telNo);
                svf.VrsOutn("MOBILE", lineCnt, student._faxNo);
                svf.VrsOutn("EMAIL", lineCnt, student._email);
                svf.VrsOutn("TEL2", lineCnt, student._emergencyTelno);
                final String hogoField = getMS932ByteLength(student._emergencyName) > 20 ? "2" : "1";
                svf.VrsOutn("PERSON" + hogoField, lineCnt, student._emergencyName);
                final String schoolField = getMS932ByteLength(student._finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FIN_SCHOOL" + schoolField, lineCnt, student._finschoolName);
                final String anotherField = getMS932ByteLength(student._anotherName) > 20 ? "2" : "1";
                svf.VrsOutn("ANOTHER_SCHOOL" + anotherField, lineCnt, student._anotherName);

                lineCnt++;
            }
            if (lineCnt > 1) {
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private String hrSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     L2.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L2 ON REGH.TR_CD1 = L2.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._year + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("     AND REGD.HR_CLASS = '" + hrClass + "' ");
        return stb.toString();
    }

    private class HrClass {
        final String _cd;
        final String _name;
        final String _staffName;
        final List _studentList;

        /**
         * コンストラクタ。
         */
        HrClass(
                final String cd,
                final String name,
                final String staffName
        ) {
            _cd = cd;
            _name = name;
            _staffName = staffName;
            _studentList = new ArrayList();
        }
        /**
         * @param db2
         */
        public void setStudent(final DB2UDB db2) throws SQLException {
            final String studentSql = getStudentSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo       = rs.getString("SCHREGNO");
                    final String name           = rs.getString("NAME");
                    final String nameKana       = rs.getString("NAME_KANA");
                    final String sexName        = rs.getString("SEX_NAME");
                    final String entDivName     = rs.getString("ENT_DIV_NAME");
                    final String entDate        = rs.getString("ENT_DATE");
                    final String birthday       = rs.getString("BIRTHDAY");
                    final String grade          = rs.getString("GRADE");
                    final String hrClass        = rs.getString("HR_CLASS");
                    final String hrName         = rs.getString("HR_NAME");
                    final String hrNameabbv     = rs.getString("HR_NAMEABBV");
                    final String attendNo       = rs.getString("ATTENDNO");
                    final String zipCd          = rs.getString("ZIPCD");
                    final String pref           = rs.getString("PREF");
                    final String addr1          = rs.getString("ADDR1");
                    final String addr2          = rs.getString("ADDR2");
                    final String telNo          = rs.getString("TELNO");
                    final String faxNo          = rs.getString("FAXNO");
                    final String email          = rs.getString("EMAIL");
                    final String emergencyTelno = rs.getString("EMERGENCYTELNO");
                    final String emergencyName  = rs.getString("EMERGENCYNAME");
                    final String finschoolName  = rs.getString("FINSCHOOL_NAME");

                    final Student student = new Student(schregNo, name, nameKana, sexName, entDivName, entDate, birthday, grade, hrClass, hrName, hrNameabbv, attendNo, zipCd, pref, addr1, addr2, telNo, faxNo, email, emergencyTelno, emergencyName, finschoolName, db2);
                    _studentList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * @return
         */
        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     N1.NAME2 AS SEX_NAME, ");
            stb.append("     N2.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     BASE.ENT_DATE, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGH.HR_NAMEABBV, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ZIP.PREF, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2, ");
            stb.append("     ADDR.TELNO, ");
            stb.append("     ADDR.FAXNO, ");
            stb.append("     ADDR.EMAIL, ");
            stb.append("     BASE.EMERGENCYTELNO, ");
            stb.append("     BASE.EMERGENCYNAME, ");
            stb.append("     FINS.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FINSCHOOLCD = FINS.FINSCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = N1.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A002' ");
            stb.append("          AND BASE.ENT_DIV = N2.NAMECD2 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT  ");
            stb.append("              *  ");
            stb.append("         FROM  ");
            stb.append("              SCHREG_ADDRESS_DAT  ");
            stb.append("         WHERE  ");
            stb.append("             (SCHREGNO,ISSUEDATE) IN  ");
            stb.append("             ( SELECT  ");
            stb.append("                   SCHREGNO,MAX(ISSUEDATE)  ");
            stb.append("               FROM  ");
            stb.append("                   SCHREG_ADDRESS_DAT  ");
            stb.append("               GROUP BY ");
            stb.append("                   SCHREGNO  ");
            stb.append("               HAVING  ");
            stb.append("                   SCHREGNO IN ( ");
            stb.append("                        SELECT  ");
            stb.append("                            SCHREGNO  ");
            stb.append("                        FROM  ");
            stb.append("                            SCHREG_REGD_DAT  ");
            stb.append("                        WHERE  ");
            stb.append("                            YEAR = '" + _param._year + "'  ");
            stb.append("                            AND SEMESTER = '" + _param._semester + "'  ");
            stb.append("                   ) ");
            stb.append("             )  ");
            stb.append("     ) ADDR ON (REGD.SCHREGNO = ADDR.SCHREGNO)  ");
            stb.append("     LEFT JOIN ZIPCD_MST ZIP ON ADDR.ZIPCD = ZIP.NEW_ZIPCD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
            stb.append("     AND REGD.HR_CLASS = '" + _cd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }

    }

    private class Student {
        final String _schregNo;
        final String _name;
        final String _nameKana;
        final String _sexName;
        final String _entDivName;
        final String _entDate;
        final String _birthday;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _attendNo;
        final String _zipCd;
        final String _pref;
        final String _addr1;
        final String _addr2;
        final String _telNo;
        final String _faxNo;
        final String _email;
        final String _emergencyTelno;
        final String _emergencyName;
        final String _finschoolName;
        String _anotherCd;
        String _anotherName;

        /**
         * コンストラクタ。
         * @throws SQLException
         */
        Student(
                final String schregNo,
                final String name,
                final String nameKana,
                final String sexName,
                final String entDivName,
                final String entDate,
                final String birthday,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrNameabbv,
                final String attendNo,
                final String zipCd,
                final String pref,
                final String addr1,
                final String addr2,
                final String telNo,
                final String faxNo,
                final String email,
                final String emergencyTelno,
                final String emergencyName,
                final String finschoolName,
                final DB2UDB db2
        ) throws SQLException {
            _schregNo       = schregNo;
            _name           = name;
            _nameKana       = nameKana;
            _sexName        = sexName;
            _entDivName     = entDivName;
            _entDate        = entDate;
            _birthday       = birthday;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _hrNameabbv     = hrNameabbv;
            _attendNo       = attendNo;
            _zipCd          = zipCd;
            _pref           = pref;
            _addr1          = addr1;
            _addr2          = addr2;
            _telNo          = telNo;
            _faxNo          = faxNo;
            _email          = email;
            _emergencyTelno = emergencyTelno;
            _emergencyName  = emergencyName;
            _finschoolName  = finschoolName;
            _anotherCd      = "";
            _anotherName    = "";

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(getAnotherSchool());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _anotherCd = rs.getString("FORMER_REG_SCHOOLCD");
                    _anotherName = rs.getString("FINSCHOOL_NAME");
                    break;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getAnotherSchool() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.FORMER_REG_SCHOOLCD, ");
            stb.append("     L1.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     ANOTHER_SCHOOL_HIST_DAT T1 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST L1 ON T1.FORMER_REG_SCHOOLCD = L1.FINSCHOOLCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD_S_DATE DESC, ");
            stb.append("     T1.SEQ DESC ");
            return stb.toString();
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
        private final String _grade;
        private final String[] _categorySelected;
        private final String _date;
        private final String _zenseki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _date = request.getParameter("DATE");
            _zenseki = request.getParameter("ZENSEKI");
        }

    }
}

// eof

