// kanji=漢字
/*
 * $Id: d8e469b5cdf0a33dcee8942a9e5389ba5a07e440 $
 *
 * 作成日: 2008/03/06 11:22:45 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  学校教育システム 賢者 [XXXX管理] 修了・卒業証明書（中高一貫）
 *
 *  2007/03/13 nakamoto・新規作成
 */

public class KNJWG053 {

    private static final Log log = LogFactory.getLog(KNJWG053.class);
    private static final String CERTIF_KINDCD = "001";

    /**
     *  KNJD.classから最初に起動されるクラス
     */
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            final String kind = request.getParameter("KIND");
            final Map certifSchool = getCertifSchool(db2, request);
            final List students = getStudent(db2, request);
            if (kind.equals("1")) {
                final KNJWG053GradCertif obj = new KNJWG053GradCertif();
                obj.svf_out(request, response, students, (String) certifSchool.get("SCHOOLNAME"), (String) certifSchool.get("STAFFNAME"));
            } else {
                final KNJWG053FinishCertif obj = new KNJWG053FinishCertif();
                obj.svf_out(request, response, students, (String) certifSchool.get("SCHOOLNAME"), (String) certifSchool.get("STAFFNAME"));
            }
        } catch (final Exception e) {
            log.debug("KNJWG053 error", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();                //DBを閉じる
            }
        }
    }

    /** 証明書マスタ */
    private Map getCertifSchool(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        Map rtn = new HashMap();
        final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT "
                         + "WHERE YEAR = '" + request.getParameter("YEAR") + "' AND CERTIF_KINDCD = '" + CERTIF_KINDCD + "'";
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                rtn.put("SCHOOLNAME", rs.getString("SCHOOL_NAME"));
                String pname = rs.getString("JOB_NAME") == null ? "" : rs.getString("JOB_NAME");
                pname += rs.getString("PRINCIPAL_NAME") == null ? "" : "  " + rs.getString("PRINCIPAL_NAME");
                rtn.put("STAFFNAME", pname);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    /** 生徒取得 */
    private List getStudent(
            final DB2UDB db2,
            final HttpServletRequest request
    ) throws Exception {
        final List rtn = new ArrayList();

        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("GAKKI");
        final String output = request.getParameter("OUTPUT");
        final String cmbData = request.getParameter("CMBCLASS");
        final String selected[] = request.getParameterValues("CLASS_SELECTED");

        String sep = "";
        String inState = "IN ('";
        for (int i = 0; i < selected.length; i++) {
            inState += sep + selected[i];
            sep = "','";
        }
        inState += "')";

        final String sql = getStudentSql(year, semester, output, cmbData, inState);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final Map st = new HashMap();
                st.put("SCHREGNO", rs.getString("SCHREGNO"));
                st.put("NAME", rs.getString("NAME"));
                st.put("BIRTHDAY", rs.getString("BIRTHDAY"));
                st.put("ATTENDNO", rs.getString("ATTENDNO"));

                rtn.add(st);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtn;
    }

    /** 生徒取得 */
    private String getStudentSql(
            final String year,
            final String semester,
            final String output,
            final String cmbData,
            final String inState
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L1.NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     L1.BIRTHDAY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SEMESTER = '" + semester + "' ");
        if (output.equals("1")) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS " + inState + " ");
        } else {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + cmbData + "' ");
            stb.append("     AND T1.ATTENDNO " + inState + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }
    
} // KNJWG053

// eof
