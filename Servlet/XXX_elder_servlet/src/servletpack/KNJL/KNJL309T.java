/*
 * $Id: 4023e6f32bbd5a63ddc241796158f10bf70c6e73 $
 *
 * 作成日: 2009/12/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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
 *  学校教育システム 賢者 [入試処理] 鳥取・推薦入学結果通知書
 */
public class KNJL309T {

    private static final Log log = LogFactory.getLog(KNJL309T.class);

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
        svf.VrSetForm("KNJL309T.frm", 4);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
        svf.VrsOut("LIMITDATE1", (KNJ_EditDate.h_format_JP(_param._tDate) + "（" + KNJ_EditDate.h_format_W(_param._tDate) + "）" + ("1".equals(_param._timeDiv) ? "午前" : "2".equals(_param._timeDiv) ? "午後" : "") + (_param._hour + "時")));
        svf.VrsOut("LIMITDATE2", KNJ_EditDate.h_format_JP(_param._sDate) + "（" + KNJ_EditDate.h_format_W(_param._sDate) + "）");
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            String oldFsCd = "";
            
            while (rs.next()) {
                
                String fsCd = rs.getString("FS_CD");
                if (fsCd != null && !"".equals(fsCd) && !fsCd.equals(oldFsCd)) {
                    svf.VrEndPage();

                    svf.VrSetForm("KNJL309T.frm", 4);
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
                    svf.VrsOut("LIMITDATE1", (KNJ_EditDate.h_format_JP(_param._tDate) + "（" + KNJ_EditDate.h_format_W(_param._tDate) + "）" + ("1".equals(_param._timeDiv) ? "午前" : "2".equals(_param._timeDiv) ? "午後" : "") + (_param._hour + "時")));
                    svf.VrsOut("LIMITDATE2", KNJ_EditDate.h_format_JP(_param._sDate) + "（" + KNJ_EditDate.h_format_W(_param._sDate) + "）");

                    String fsName = rs.getString("FINSCHOOL_NAME") == null ? "" : rs.getString("FINSCHOOL_NAME") + rs.getString("FINSCHOOL_TYPE_NAME") + "長　様"; 
                    svf.VrsOut("JHSCHOOL_NAME", fsName);
                    
                    svf.VrsOut("SCHOOL_NAME", _param._schoolName);
                    svf.VrsOut("PRINCIPAL_NAME", _param._principalName);
                }
                
                svf.VrsOut("COURSE", rs.getString("MAJORSNAME"));
                svf.VrsOut("EXAM_NO", String.valueOf(rs.getInt("EXAMNO")));
                svf.VrsOut("RESULT", ("1".equals(rs.getString("NML013_NAMECD2"))) ? "合格内定" : "不合格");
                svf.VrsOut("REMARK", rs.getString("SHIFT_DESIRE_FLG_NAME"));
                svf.VrEndRecord();
                _hasData = true;
                
                oldFsCd = fsCd;
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHOOL_ORDER AS( ");
        stb.append("     SELECT ");
        stb.append("         T1.FS_CD, ");
        stb.append("         MIN(EXAMNO) AS MIN_EXAMNO, ");
        stb.append("         COUNT(*) AS COUNT, ");
        stb.append("         ROWNUMBER() OVER() AS ROW_NUMBER ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("         INNER JOIN NAME_MST NML003 ON ");
        stb.append("             NML003.NAMECD1 = 'L003' ");
        stb.append("             AND NML003.NAMECD2 = T1.APPLICANTDIV ");
        stb.append("             AND NML003.NAMESPARE1 = '1' ");
        stb.append("         LEFT JOIN NAME_MST NML013 ON ");
        stb.append("             NML013.NAMECD1 = 'L013' ");
        stb.append("             AND NML013.NAMECD2 = T1.JUDGEMENT ");
        stb.append("             AND NML013.NAMESPARE1 = '1' ");
        stb.append("         LEFT JOIN FINSCHOOL_MST T4 ON ");
        stb.append("             T4.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     WHERE ");
        stb.append("         T1.ENTEXAMYEAR = '" + _param._year  + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.FS_CD ");
        stb.append("     ORDER BY ");
        stb.append("         MIN(EXAMNO) ");
        stb.append(" )  ");
        stb.append(" SELECT ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     T4.FINSCHOOL_NAME, ");
        stb.append("     VALUE(NML019.NAME1,'') AS FINSCHOOL_TYPE_NAME, ");
        stb.append("     VALUE(NML026.NAME1,'') AS SHIFT_DESIRE_FLG_NAME, ");
        stb.append("     T1.JUDGEMENT, ");
        stb.append("     NML003.NAMESPARE1 AS NML003_NAMESPARE1, ");
        stb.append("     NML013.NAMECD2 AS NML013_NAMECD2, ");
        stb.append("     T2.MAJORLCD, ");
        stb.append("     T2.MAJORSCD, ");
        stb.append("     (CASE WHEN T2.MAJORSCD = '0' THEN T3.MAJORLNAME ELSE T3.MAJORSNAME END) AS MAJORSNAME");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN NAME_MST NML003 ON ");
        stb.append("         NML003.NAMECD1 = 'L003' ");
        stb.append("         AND NML003.NAMECD2 = T1.APPLICANTDIV ");
        stb.append("         AND NML003.NAMESPARE1 = '1' ");
        stb.append("     INNER JOIN SCHOOL_ORDER ORDER ON ");
        stb.append("         ORDER.FS_CD = T1.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON ");
        stb.append("         NML013.NAMECD1 = 'L013' ");
        stb.append("         AND NML013.NAMECD2 = T1.JUDGEMENT ");
        stb.append("         AND NML013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTWISH_DAT T2 ON ");
        stb.append("         T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("         AND T2.WISHNO = '1' ");
        stb.append("     LEFT JOIN V_ENTEXAM_MAJOR_MST T3 ON ");
        stb.append("         T3.MAJORCD = T2.MAJORLCD || T2.MAJORSCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T4 ON ");
        stb.append("         T4.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST NML019 ON ");
        stb.append("         NML019.NAMECD1 = 'L019' ");
        stb.append("         AND NML019.NAMECD2 = T4.FINSCHOOL_TYPE ");
        stb.append("     LEFT JOIN NAME_MST NML026 ON ");
        stb.append("         NML026.NAMECD1 = 'L026' ");
        stb.append("         AND NML026.NAMECD2 = T1.SHIFT_DESIRE_FLG ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     ORDER.ROW_NUMBER, ");
        stb.append("     T2.MAJORLCD, ");
        stb.append("     T2.MAJORSCD, ");
        stb.append("     T1.EXAMNO ");

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
        private final String _programid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _date;
        private final String _tDate;
        private final String _timeDiv;
        private final String _hour;
        private final String _sDate;
        
        private String _schoolName;
        private String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE");
            _tDate = request.getParameter("T_DATE");
            _timeDiv = request.getParameter("TIME_DIV");
            _hour = request.getParameter("HOUR");
            _sDate = request.getParameter("S_DATE");
            
            setSchoolName(db2);
            setPrincipalName(db2);
        }
        
        private void setSchoolName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT * FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOLNAME1");
                }
                
            } catch (Exception e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        private void setPrincipalName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.* ");
                stb.append(" FROM ");
                stb.append("     STAFF_MST T1, ");
                stb.append("     STAFF_YDAT T2 ");
                stb.append(" WHERE ");
                stb.append("     T1.STAFFCD = T2.STAFFCD ");
                stb.append("     AND T1.STAFFCD IN ( ");
                stb.append("         SELECT ");
                stb.append("             MIN(STAFFCD) ");
                stb.append("         FROM ");
                stb.append("             STAFF_MST ");
                stb.append("         WHERE JOBCD = '0001' AND ");
                stb.append("             STAFFCD = T1.STAFFCD) ");
                stb.append("     AND T2.YEAR = '" + _ctrlYear + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _principalName = rs.getString("STAFFNAME");
                }
                
            } catch (Exception e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}

// eof

