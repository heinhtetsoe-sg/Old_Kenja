/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 190f546ce10873db6b63985b51a9e73d3302921f $
 *
 * 作成日: 2018/05/22
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;

public class KNJD185H {
    private static final Log log = LogFactory.getLog(KNJD185H.class);
    private static final String H_SCHOOL = "H";
    private static final String J_SCHOOL = "J";

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        DB2UDB db2 = null;
        String schoolKind = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String year = request.getParameter("CTRL_YEAR");
                final String disp = request.getParameter("DISP");
                final String grade = request.getParameter("GRADE");
                final String gradeHrClass = request.getParameter("GRADE_HR_CLASS");
                String setGrade = grade;
                if ("2".equals(disp)) {
                    setGrade = gradeHrClass.substring(0, 2);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     GDAT.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT GDAT ");
                stb.append(" WHERE ");
                stb.append("     GDAT.YEAR = '" + year + "' ");
                stb.append("     AND GDAT.GRADE = '" + setGrade + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
        try {
            if (H_SCHOOL.equals(schoolKind)) {
                new KNJD185H_H().svf_out(request, response);
            } else {
                new KNJD185H_J().svf_out(request, response);
            }
        } catch (Exception e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _year);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

    }
}

// eof
