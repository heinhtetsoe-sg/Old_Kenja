/*
 * $Id: 367b602a43eebb5b833b6277d317d0c5b43bad4e $
 *
 * 作成日: 2009/10/26
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


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

/**
 *
 *   学校教育システム 賢者 [保健管理]
 *
 *                   ＜ＫＮＪＦ０３２＞  既往症一覧
 */
public class KNJF032 {

    private static final Log log = LogFactory.getLog(KNJF032.class);

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
        svf.VrSetForm("KNJF032.frm", 1);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final int maxline = 50;
        
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            String oldGradeHrclass = "";

            int line = 0;
            while (rs.next()) {
                line += 1;
                if ("1".equals(_param._selectOrder) && !"".equals(oldGradeHrclass) && !oldGradeHrclass.equals(rs.getString("HR_CLASS"))) {
                    // 年組番号順指定ではHR毎に改ページする。
                    svf.VrEndPage();
                    line = (line / maxline + (line % maxline == 0 ? 0 : 1)) * maxline + 1;
                }
                
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
                svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(KNJ_EditDate.b_year(_param._loginDate) + "-04-01") + "度");
                final int page = line / maxline + (line % maxline == 0 ? 0 : 1);
                final int l = (line % maxline == 0) ? maxline : line % maxline; 
                svf.VrsOut("PAGE", String.valueOf(page));

                svf.VrsOutn("ATTENDNO", l, rs.getString("HR_NAME") + String.valueOf(Integer.parseInt(rs.getString("ATTENDNO")) + "番"));
                svf.VrsOutn("SCHREGNO", l, rs.getString("SCHREGNO"));
                svf.VrsOutn("NAME", l, rs.getString("NAME"));
                svf.VrsOutn("SEX", l, rs.getString("SEX"));
                svf.VrsOutn("MEDICAL_HISTORY1", l, rs.getString("MEDICAL_HISTORY1"));
                svf.VrsOutn("MEDICAL_HISTORY2", l, rs.getString("MEDICAL_HISTORY2"));
                svf.VrsOutn("MEDICAL_HISTORY3", l, rs.getString("MEDICAL_HISTORY3"));
                
                oldGradeHrclass = rs.getString("HR_CLASS");
                
                _hasData = true;
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        stb.append("     T1.GRADE || T1.HR_CLASS AS HR_CLASS,");
        stb.append("     T2.HR_NAME,");
        stb.append("     T1.ATTENDNO,");
        stb.append("     T3.SCHREGNO,");
        stb.append("     T3.NAME,");
        stb.append("     T4.NAME2 AS SEX,");
        stb.append("     T6.NAME1 AS MEDICAL_HISTORY1,");
        stb.append("     T7.NAME1 AS MEDICAL_HISTORY2,");
        stb.append("     T8.NAME1 AS MEDICAL_HISTORY3");
        stb.append(" FROM");
        stb.append("     SCHREG_REGD_DAT T1");
        stb.append("     INNER JOIN MEDEXAM_DET_DAT T5 ON");
        stb.append("         T1.YEAR = T5.YEAR AND ");
        stb.append("         T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T2 ON");
        stb.append("         T1.YEAR = T2.YEAR");
        stb.append("         AND T1.SEMESTER = T2.SEMESTER");
        stb.append("         AND T1.GRADE = T2.GRADE");
        stb.append("         AND T1.HR_CLASS = T2.HR_CLASS");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON");
        stb.append("         T3.SCHREGNO = T1.SCHREGNO");
        stb.append("     LEFT JOIN NAME_MST T4 ON");
        stb.append("         T4.NAMECD1 = 'Z002'");
        stb.append("         AND T4.NAMECD2 = T3.SEX");
        stb.append("     LEFT JOIN NAME_MST T6 ON");
        stb.append("         T6.NAMECD1 = 'F143'");
        stb.append("         AND T6.NAMECD2 = T5.MEDICAL_HISTORY1");
        stb.append("     LEFT JOIN NAME_MST T7 ON");
        stb.append("         T7.NAMECD1 = 'F143'");
        stb.append("         AND T7.NAMECD2 = T5.MEDICAL_HISTORY2");
        stb.append("     LEFT JOIN NAME_MST T8 ON");
        stb.append("         T8.NAMECD1 = 'F143'");
        stb.append("         AND T8.NAMECD2 = T5.MEDICAL_HISTORY3");
        stb.append(" WHERE");
        stb.append("     T1.YEAR = '" + _param._year + "'");
        stb.append("     AND T1.SEMESTER = '" + _param._semester +"'");
        stb.append("     AND (T5.MEDICAL_HISTORY1 IS NOT NULL ");
        stb.append("          OR T5.MEDICAL_HISTORY2 IS NOT NULL ");
        stb.append("          OR T5.MEDICAL_HISTORY3 IS NOT NULL) ");
        stb.append(" ORDER BY ");
        if ("2".equals(_param._selectOrder)) {
            stb.append("     T1.SCHREGNO ");
        } else {
            stb.append("     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        }
        
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
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _selectOrder; // 1=年組番号順、2=学籍番号順

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _selectOrder = request.getParameter("OUTPUT");
        }
    }
}

// eof

