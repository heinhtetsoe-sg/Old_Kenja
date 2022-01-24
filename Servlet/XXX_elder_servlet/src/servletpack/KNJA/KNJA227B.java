/*
 * $Id: 3b4505ea446364a5c89af6b83c609dfc591c9dd5 $
 *
 * 作成日: 2017/07/10
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class KNJA227B {

    private static final Log log = LogFactory.getLog(KNJA227B.class);

    private boolean _hasData;

    private Param _param;

    private final String SORT_FS_CD = "1";
    private final String SUNDAI_CD_FUTUU = "3008112";
    private final String SUNDAI_CD_SPORT = "3008113";
    private final String SCHOLAR_TOKUBETU = "1";
    private final String SCHOLAR_IPPAN = "2";

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
        for (int i = 0; i < _param._prischoolcds.length; i++) {
            final String[] _cds = StringUtils.split(_param._prischoolcds[i], "-");
            final String priCd = _cds[0];
            final String priClsCd = _cds[1];

            final List priList = getList(db2, sqlPri(priCd,priClsCd));
            for (int pi = 0; pi < priList.size(); pi++) {
                final Map pMap = (Map) priList.get(pi);
                
                svf.VrSetForm("KNJA227B.frm", 1);
                svf.VrsOut("DATE", _param._briefingDateStr); //説明会日時
                svf.VrsOut("ZIPNO", getString(pMap, "PRISCHOOL_ZIPCD"));
                final String setAddr1 = getString(pMap, "PRISCHOOL_ADDR1");
                final String setAddr2 = getString(pMap, "PRISCHOOL_ADDR2");
                if (getMS932Bytecount(setAddr1) <= 40 && getMS932Bytecount(setAddr2) <= 40) {
                    svf.VrsOut("ADDR1_1", setAddr1);
                    svf.VrsOut("ADDR2_1", setAddr2);
                } else {
                    svf.VrsOut("ADDR1_2", setAddr1);
                    svf.VrsOut("ADDR2_2", setAddr2);
                }
                final String setName1 = getString(pMap, "PRISCHOOL_NAME");
                final String setName2 = getString(pMap, "PRISCHOOL_CLASS_NAME");
                if (getMS932Bytecount(setName1) <= 34 && getMS932Bytecount(setName2) <= 34) {
                    svf.VrsOut("PRISCHOOL_NAME1_1", setName1);
                    svf.VrsOut("PRISCHOOL_NAME2_1", setName2);
                } else if (getMS932Bytecount(setName1) <= 44 && getMS932Bytecount(setName2) <= 44) {
                    svf.VrsOut("PRISCHOOL_NAME1_2", setName1);
                    svf.VrsOut("PRISCHOOL_NAME2_2", setName2);
                } else {
                    svf.VrsOut("PRISCHOOL_NAME1_3", setName1);
                    svf.VrsOut("PRISCHOOL_NAME2_3", setName2);
                }
                svf.VrsOut("PRISCHOOL_CD", getString(pMap, "PRISCHOOLCD"));
                svf.VrsOut("SEND_DATE", _param._sendDateStr); //資料送付日
                
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            return "";
//            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlPri(final String prischoolcd, final String prischoolClassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     P1.PRISCHOOLCD, ");
        stb.append("     P1.PRISCHOOL_NAME, ");
        stb.append("     P1.PRISCHOOL_ZIPCD, ");
        stb.append("     P1.PRISCHOOL_ADDR1, ");
        stb.append("     P1.PRISCHOOL_ADDR2, ");
        stb.append("     P2.PRISCHOOL_CLASS_CD, ");
        stb.append("     P2.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ");
        stb.append("     P2.PRISCHOOL_ZIPCD AS PRISCHOOL_CLASS_ZIPCD, ");
        stb.append("     P2.PRISCHOOL_ADDR1 AS PRISCHOOL_CLASS_ADDR1, ");
        stb.append("     P2.PRISCHOOL_ADDR2 AS PRISCHOOL_CLASS_ADDR2 ");
        stb.append(" FROM ");
        stb.append("     PRISCHOOL_MST P1 ");
        stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST P2 ");
        stb.append("          ON P2.PRISCHOOLCD = P1.PRISCHOOLCD ");
        stb.append("         AND P2.PRISCHOOL_CLASS_CD = '" + prischoolClassCd + "' ");
        stb.append(" WHERE ");
        stb.append("     P1.PRISCHOOLCD = '" + prischoolcd + "' ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61230 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String[] _prischoolcds;
        private final String _briefingDate;
        private final String _briefingAmPm;
        private final String _briefingHour;
        private final String _briefingMinute;
        private final String _sendDate;

        final String _briefingDateStr;
        final String _sendDateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE");
            _prischoolcds = request.getParameterValues("SCHOOL_SELECTED");
            _briefingDate = request.getParameter("BRIEFING_DATE");
            _briefingAmPm = request.getParameter("BRIEFING_AMPM");
            _briefingHour = request.getParameter("BRIEFING_HOUR");
            _briefingMinute = request.getParameter("BRIEFING_MINUTE");
            _sendDate = request.getParameter("SEND_DATE");

            _briefingDateStr = setBriefingDate() + setBriefingAmPm() + setBriefingHour() + setBriefingMinute() + "からの";
            _sendDateStr = setSendDate();
        }

        private String setBriefingDate() {
            return !StringUtils.isBlank(_briefingDate) ? KNJ_EditDate.h_format_JP_MD(_briefingDate) + "(" + KNJ_EditDate.h_format_W(_briefingDate) + ")" : "";
        }

        private String setBriefingAmPm() {
            return "1".equals(_briefingAmPm) ? "午前" : "2".equals(_briefingAmPm) ? "午後" : "";
        }

        private String setBriefingHour() {
            return !StringUtils.isBlank(_briefingHour) ? _briefingHour + "時" : "";
        }

        private String setBriefingMinute() {
            return !StringUtils.isBlank(_briefingMinute) ? _briefingMinute + "分" : "";
        }

        private String setSendDate() {
            return !StringUtils.isBlank(_sendDate) ? KNJ_EditDate.h_format_JP_MD(_sendDate) : "";
        }
    }
}

// eof

