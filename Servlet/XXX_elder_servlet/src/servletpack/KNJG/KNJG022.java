/*
 * $Id: 63ebd902e75ba58163c88edda3fbff9b5d2f0f28 $
 *
 * 作成日: 2010/09/07
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*
 *  学校教育システム 賢者 [事務管理] 証明書交付台帳
 *
 */
public class KNJG022 {

    private static final Log log = LogFactory.getLog(KNJG022.class);

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

            if ("3".equals(_param._sortOrder)) {
                printFormOnly(svf);
            } else {
                printMain(db2, svf);
            }

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

    private void printFormOnly(final Vrw32alp svf) {
        svf.VrSetForm("KNJG022.frm", 1);
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　");
        svf.VrsOut("TITLE", "証明書交付台帳");
        _hasData = true;
        svf.VrEndPage();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJG022.frm", 1);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = certifNoSql();
            log.debug(" certifNosql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            final List certifList = new ArrayList();
            
            while (rs.next()) {
                
                final String issuedate = rs.getString("ISSUEDATE");
                final String certifNo = rs.getString("CERTIF_NO");
                final String kindname = rs.getString("KINDNAME");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String majorname = rs.getString("MAJORNAME");
                
                final CertifIssueDat dat = new CertifIssueDat(issuedate, certifNo, kindname, schregno, name, hrName, attendno, majorname);
                certifList.add(dat);
            }
            
            final int LINE_PER_PAGE = 10;
            for (int i = 0; i < LINE_PER_PAGE * (_param._page - 1) && !certifList.isEmpty(); i++) {
                certifList.remove(0);
            }
            
            int c = 0;
            int page = _param._page;
            for (final Iterator it = certifList.iterator(); it.hasNext();) {
                final CertifIssueDat dat = (CertifIssueDat) it.next();
                c += 1;
                
                if (c == 1) {
                    svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　");
                    svf.VrsOut("TITLE", "証明書交付台帳");
                }
                
                svf.VrsOut("PAGE", String.valueOf(page));
                svf.VrsOutn("ISSUEDAY", c, KNJ_EditDate.h_format_JP(dat._issuedate));
                svf.VrsOutn("CERTIFNO", c, dat._certifNo);
                svf.VrsOutn("KINDNAME", c, dat._kindname);
                
                svf.VrsOutn(byteCountMS932(dat._name) > 24 ? "NAME2" : "NAME1", c, dat._name);
                svf.VrsOutn("HRNAME", c, dat._hrName);
                svf.VrsOutn("ATTENDNO", c, dat._attendno == null ? "" : Integer.parseInt(dat._attendno) + "番");
                svf.VrsOutn("MAJORNAME", c, dat._majorname);
                _hasData = true;
                
                if (c == 10) {
                    svf.VrEndPage();
                    c = 0;
                    page += 1;
                }
            }
            
            if (c != 0) {
                svf.VrEndPage();
            }
            
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    
    protected int byteCountMS932(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }
    
    public class CertifIssueDat {
        
        final String _issuedate;
        final String _certifNo;
        final String _kindname;
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _majorname;
        
        public CertifIssueDat(
                final String issuedate,
                final String certifNo,
                final String kindname,
                final String schregno, 
                final String name,
                final String hrName,
                final String attendno,
                final String majorname)
        {
            _issuedate = issuedate;
            _certifNo = certifNo;
            _kindname = kindname;
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _majorname = majorname;
        }
        
        public String toString() {
            return _issuedate + " : " + _certifNo + " : " + _kindname + " : " + _schregno + " (" + _hrName + " "  + _attendno + " " + _name + ")";
        }
    }

    private String certifNoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.CERTIF_INDEX, ");
        stb.append("     T1.ISSUEDATE, ");
        if ("1".equals(_param._certifNoSyudou)) {
            stb.append("     T2.REMARK1 AS CERTIF_NO, ");
        } else {
            stb.append("     T1.CERTIF_NO AS CERTIF_NO, ");
        }
        stb.append("     T1.CERTIF_KINDCD, ");
        stb.append("     T3.KINDNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T4.NAME, ");
        stb.append("     T8.HR_NAME, ");
        stb.append("     T5.ATTENDNO, ");
        stb.append("     T9.MAJORNAME, ");
        stb.append("     T9.MAJORABBV ");
        stb.append(" FROM ");
        stb.append("     CERTIF_ISSUE_DAT T1 ");
        if ("1".equals(_param._certifNoSyudou)) {
            stb.append("     INNER JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ON ");
            stb.append("         T1.YEAR = T2.YEAR ");
            stb.append("         AND T2.CERTIF_INDEX = T1.CERTIF_INDEX ");
            stb.append("         AND T2.TYPE = '1' ");
        }
        stb.append("     LEFT JOIN CERTIF_KIND_MST T3 ON ");
        stb.append("         T3.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T4 ON ");
        stb.append("         T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(YEAR) AS YEAR FROM SCHREG_REGD_DAT WHERE YEAR <= '" + _param._year + "' ");
        stb.append("         GROUP BY SCHREGNO");
        stb.append("         ) T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT ");
        stb.append("         GROUP BY SCHREGNO, YEAR ");
        stb.append("         ) T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.YEAR = T7.YEAR ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON ");
        stb.append("         T5.SCHREGNO = T4.SCHREGNO ");
        stb.append("         AND T5.YEAR = T7.YEAR ");
        stb.append("         AND T5.SEMESTER = T6.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T8 ON ");
        stb.append("         T8.YEAR = T5.YEAR ");
        stb.append("         AND T8.SEMESTER = T5.SEMESTER ");
        stb.append("         AND T8.GRADE = T5.GRADE ");
        stb.append("         AND T8.HR_CLASS = T5.HR_CLASS ");
        stb.append("     LEFT JOIN MAJOR_MST T9 ON ");
        stb.append("         T9.COURSECD = T5.COURSECD ");
        stb.append("         AND T9.MAJORCD = T5.MAJORCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.ISSUECD = '1' ");
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sortOrder)) {
            stb.append("     VALUE(T1.ISSUEDATE, '9999-12-31') ");
            stb.append("     , ");
            if ("1".equals(_param._certifNoSyudou)) {
                stb.append("     VALUE(CASE WHEN T2.REMARK1 IS NOT NULL THEN CAST(T2.REMARK1 AS INT) ELSE 100000000 END, 100000000) ");
            } else {
                stb.append("     VALUE(CAST(T1.CERTIF_NO AS INT), 100000000) ");
            }
        } else {
            if ("1".equals(_param._certifNoSyudou)) {
                stb.append("     VALUE(CASE WHEN T2.REMARK1 IS NOT NULL THEN CAST(T2.REMARK1 AS INT) ELSE 100000000 END, 100000000) ");
            } else {
                stb.append("     VALUE(CAST(T1.CERTIF_NO AS INT), 100000000) ");
            }
            stb.append("     , VALUE(T1.ISSUEDATE, '9999-12-31') ");
        }
        stb.append("     , T1.CERTIF_KINDCD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final int _page;
        private final String _sortOrder;
        private final String _certifNoSyudou;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _page = request.getParameter("PAGE") == null ? 1 : Integer.parseInt(request.getParameter("PAGE"));
            _sortOrder = request.getParameter("SORT");
            _certifNoSyudou = request.getParameter("certifNoSyudou");
        }

    }
}

// eof

