/*
 * $Id: 46681eea34ca1cbea2c9202a7e7c2b51930c71ad $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL509J_1 {

    private static final Log log = LogFactory.getLog(KNJL509J_1.class);

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
		final List list = PrintData.load(db2, _param);
		for (int line = 0; line < list.size(); line++) {
			final PrintData printData = (PrintData) list.get(line);
	            print1(svf, printData);
		}
    }

    public void print1(final Vrw32alp svf, final PrintData putwk) {

        svf.VrSetForm("KNJL509J_1.frm", 1);

        svf.VrsOut("EXAM_NO", putwk._receptNo); // 受験番号
        int nlen = KNJ_EditEdit.getMS932ByteLength(putwk._name);
        String nfield = nlen > 30 ? "3" : nlen > 22 ? "2" : "1";
        svf.VrsOut("NAME" + nfield, putwk._name); // 氏名

        svf.VrsOut("TEXT", _param._applicantdivName); // 本文

        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._printDate)); // 日付

        svf.VrsOut("JOB_NAME1", (String) _param._certifSchoolMap.get("JOB_NAME")); // 役職名1
        svf.VrsOut("STAFF_NAME1", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME")); // 職員名1
        svf.VrsOut("JOB_NAME2", (String) _param._certifSchoolMap.get("REMARK8")); // 役職名2
        svf.VrsOut("STAFF_NAME2", (String) _param._certifSchoolMap.get("REMARK9")); // 職員名2

        svf.VrEndPage();
        _hasData = true;
    }

    private static class PrintData {
        final String _receptNo;
        final String _name;

        PrintData(
            final String receptNo,
            final String name
        ) {
        	_receptNo = receptNo;
            _name = name;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String receptNo = rs.getString("RECEPTNO");
                    final String name = rs.getString("NAME");
                    final PrintData printData = new PrintData(receptNo, name);
                    list.add(printData);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("  T1.RECEPTNO, ");
            stb.append("  T2.NAME ");
            stb.append(" FROM ");
            stb.append("  ENTEXAM_RECEPT_DAT T1 ");
            stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
            stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
            stb.append("  LEFT JOIN NAME_MST T3_L013 ");
            stb.append("    ON T3_L013.NAMECD1 = 'L013' ");
            stb.append("   AND T3_L013.NAMECD2 = T1.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '"+param._entexamyear+"' ");
            stb.append("     AND T1.APPLICANTDIV = '"+param._applicantdiv+"' ");
            if (!"".equals(param._testdiv)) {
                stb.append("     AND T1.TESTDIV = '"+param._testdiv+"' ");
            }
            stb.append("     AND T3_L013.NAMESPARE1 = '1' ");
            stb.append("     AND T1.RECEPTNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            if ("2".equals(param._targetDiv)) {
                stb.append("     AND T1.PROCEDUREDIV1  = '1' ");
                stb.append("     AND T1.ADJOURNMENTDIV = '1' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     RECEPTNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70878 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _applicantdivName;
        final String _testdiv;
        final String _loginDate;
        final String _printDate; // 発行年月日
        final String _targetDiv; // 1:合格者、2:手続修了者
        final String[] _categorySelected;
        final Map _certifSchoolMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");

            _applicantdivName = getNameMst(db2, "L003", _applicantdiv);
            _loginDate = request.getParameter("LOGIN_DATE");
            _printDate = request.getParameter("PRINT_DATE").replace('/', '-');
            _targetDiv = request.getParameter("TARGET_DIV");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");

            _certifSchoolMap = getCertifScholl(db2);

        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();

        	stb.append(" SELECT DISTINCT ");
        	stb.append("     NAME1 ");
        	stb.append(" FROM ");
        	stb.append("     V_NAME_MST ");
        	stb.append(" WHERE ");
        	stb.append("     YEAR    = '" + _entexamyear + "' AND ");
        	stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
            	stb.append(" AND NAMECD2 = '" + namecd2 + "' ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                // log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retStr = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("SCHOOL_NAME", StringUtils.defaultString(rs.getString("SCHOOL_NAME")));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", StringUtils.defaultString(rs.getString("PRINCIPAL_NAME")));
                    rtnMap.put("REMARK8", StringUtils.defaultString(rs.getString("REMARK8")));
                    rtnMap.put("REMARK9", StringUtils.defaultString(rs.getString("REMARK9")));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }



    }
}

// eof

