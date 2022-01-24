/*
 * $Id: 48f0c61dd03a1dc09dc5b042c14133f90ca85ea8 $
 *
 * 作成日: 2017/06/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


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

public class KNJM442M {

    private static final Log log = LogFactory.getLog(KNJM442M.class);

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
        svf.VrSetForm("KNJM442M.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int printCnt = 1;
            while (rs.next()) {
                if (printCnt > 4) {
                    svf.VrEndPage();
                    printCnt = 1;
                }
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String staffname = rs.getString("STAFFNAME");

                svf.VrsOutn("DATE", printCnt, KNJ_EditDate.h_format_JP(_param._testDay) + "　" + _param._testHour + "時" + _param._testMinute + "分より");
                svf.VrsOutn("HR_NAME", printCnt, hrName);
                svf.VrsOutn("SCHREG_NO", printCnt, schregno);
                final String staffField = getMS932ByteLength(staffname) > 30 ? "3" : getMS932ByteLength(staffname) > 20 ? "2" : "1";
                svf.VrsOutn("TEACHER_NAME" + staffField, printCnt, staffname);
                final String nameField = getMS932ByteLength(name) > 30 ? "3" : getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, printCnt, name);
                final String subclassField = getMS932ByteLength(subclassname) > 30 ? "3" : getMS932ByteLength(subclassname) > 20 ? "2" : "1";
                svf.VrsOutn("SUBCLASS_NAME" + subclassField, printCnt, subclassname);
                svf.VrsOutn("DIV", printCnt, "新");
                printCnt++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CSTF AS ( ");
        stb.append(" SELECT ");
        stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     MIN(CSTF.STAFFCD) AS STAFFCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT CSTF ");
        stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CSTF.YEAR = CHAIR.YEAR ");
        stb.append("           AND CSTF.SEMESTER = CHAIR.SEMESTER ");
        stb.append("           AND CSTF.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("           AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = '" + _param._subclass + "' ");
        stb.append(" WHERE ");
        stb.append("     CSTF.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND CSTF.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND CSTF.CHARGEDIV = 1 ");
        stb.append(" GROUP BY ");
        stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     STD_PASS.SCHREGNO, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     STD_PASS.CLASSCD || '-' || STD_PASS.SCHOOL_KIND || '-' || STD_PASS.CURRICULUM_CD || '-' || STD_PASS.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     SUBM.SUBCLASSNAME, ");
        stb.append("     STF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_STD_PASS_DAT STD_PASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON STD_PASS.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON STD_PASS.YEAR = REGD.YEAR ");
        stb.append("           AND STD_PASS.SEMESTER = REGD.SEMESTER ");
        stb.append("           AND STD_PASS.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("           AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("           AND REGD.GRADE = REGDH.GRADE  ");
        stb.append("           AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     INNER JOIN SUBCLASS_MST SUBM ON STD_PASS.SUBCLASSCD = SUBM.SUBCLASSCD ");
        stb.append("           AND STD_PASS.CLASSCD = SUBM.CLASSCD ");
        stb.append("           AND STD_PASS.SCHOOL_KIND = SUBM.SCHOOL_KIND  ");
        stb.append("           AND STD_PASS.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
        stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON STD_PASS.YEAR = CHAIR.YEAR ");
        stb.append("           AND STD_PASS.SEMESTER = CHAIR.SEMESTER ");
        stb.append("           AND SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD = CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD ");
        stb.append("     LEFT JOIN CHAIR_STD_DAT CSTD ON STD_PASS.YEAR = CSTD.YEAR ");
        stb.append("           AND STD_PASS.SEMESTER = CSTD.SEMESTER ");
        stb.append("           AND STD_PASS.SCHREGNO = CSTD.SCHREGNO ");
        stb.append("           AND CHAIR.CHAIRCD = CSTD.CHAIRCD ");
        stb.append("     LEFT JOIN CSTF ON CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = CSTF.SUBCLASSCD ");
        stb.append("     LEFT JOIN STAFF_MST STF ON CSTF.STAFFCD = STF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     STD_PASS.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND STD_PASS.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND STD_PASS.CLASSCD || '-' || STD_PASS.SCHOOL_KIND || '-' || STD_PASS.CURRICULUM_CD || '-' || STD_PASS.SUBCLASSCD = '" + _param._subclass + "' ");
        stb.append("     AND STD_PASS.SEM_PASS_FLG = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     STD_PASS.SCHREGNO, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     STD_PASS.CLASSCD || '-' || STD_PASS.SCHOOL_KIND || '-' || STD_PASS.CURRICULUM_CD || '-' || STD_PASS.SUBCLASSCD, ");
        stb.append("     SUBM.SUBCLASSNAME, ");
        stb.append("     STF.STAFFNAME ");
        stb.append(" ORDER BY ");
        stb.append("     STD_PASS.SCHREGNO ");
        return stb.toString();
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
        final String _semester;
        final String _subclass;
        final String _testDay;
        final String _testHour;
        final String _testMinute;
        final String _prgid;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useschoolKindfield;
        final String _schoolcd;
        final String _schoolkind;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _subclass = request.getParameter("SUBCLASS");
            _testDay = request.getParameter("TEST_DAY");
            _testHour = request.getParameter("TEST_HOUR");
            _testMinute = request.getParameter("TEST_MINUTE");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
        }

    }
}

// eof

