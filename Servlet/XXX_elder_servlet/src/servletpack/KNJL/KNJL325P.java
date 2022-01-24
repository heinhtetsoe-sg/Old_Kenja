/*
 * $Id: f2a07fcd2e6e3007e0b57183d46a5fac9a814007 $
 *
 * 作成日: 2017/07/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL325P {

    private static final Log log = LogFactory.getLog(KNJL325P.class);

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
        svf.VrSetForm("KNJL325P.frm", 1);

        setTitle(svf);
        int subClassCnt = 1;
        for (Iterator itSubclass = _param._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
            int lineCnt = 1;
            final String subClassCd = (String) itSubclass.next();
            final Subclass subclass = (Subclass) _param._subclassMap.get(subClassCd);

            if ("B".equals(subClassCd)) {
                svf.VrsOutn("POINT_ALL", lineCnt++, subclass._printData._cnt);
                svf.VrsOutn("POINT_ALL", lineCnt++, subclass._printData._avg);
                svf.VrsOutn("POINT_ALL", lineCnt++, subclass._printData._stdDev);
                svf.VrsOutn("POINT_ALL", lineCnt++, subclass._printData._dev);
            } else {
                svf.VrsOut("CLASS_NAME1_" + subClassCnt, subclass._subclassName);
                svf.VrsOutn("POINT" + subClassCnt, lineCnt++, subclass._printData._cnt);
                svf.VrsOutn("POINT" + subClassCnt, lineCnt++, subclass._printData._avg);
                svf.VrsOutn("POINT" + subClassCnt, lineCnt++, subclass._printData._stdDev);
                svf.VrsOutn("POINT" + subClassCnt, lineCnt++, subclass._printData._dev);
            }
            subClassCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　結果");
        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
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
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDate;
        final String _entexamYear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final Map _subclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            _subclassMap = getSubclassMap(db2);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private Map getSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql("L009", "");
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                final String subclassName = "1".equals(_applicantDiv) ? "NAME1" : "NAME2";

                while (rs.next()) {
                    final String nameCd2 = rs.getString("NAMECD2");
                    final String name = rs.getString(subclassName);
                    if (null != name && !"".equals(name)) {
                        final Subclass subclass = new Subclass(db2, nameCd2, name, _entexamYear, _applicantDiv, _testDiv);
                        retMap.put(nameCd2, subclass);
                    }
                }
                final Subclass subclass = new Subclass(db2, "B", "面接なし", _entexamYear, _applicantDiv, _testDiv);
                retMap.put("B", subclass);

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

    }

    /** 科目 */
    private class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final PrintData _printData;

        public Subclass(
                final DB2UDB db2,
                final String subclassCd,
                final String subclassName,
                final String entexamYear,
                final String applicantDiv,
                final String testDiv
        ) throws SQLException {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _printData = setPrintData(db2, entexamYear, applicantDiv, testDiv);
        }

        private PrintData setPrintData(
                final DB2UDB db2,
                final String entexamYear,
                final String applicantDiv,
                final String testDiv
        ) throws SQLException {
            PrintData retPrintData = new PrintData("0", "0", "0", "0");
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAverageSql(entexamYear, applicantDiv, testDiv);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVARAGE_TOTAL");
                    final String stdDev = rs.getString("STDDEV");
                    final String dev = rs.getString("DEV");
                    retPrintData = new PrintData(cnt, avg, stdDev, dev);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retPrintData;
        }

        private String getAverageSql(
                final String entexamYear,
                final String applicantDiv,
                final String testDiv
        ) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     JAVG.COUNT, ");
            stb.append("     JAVG.AVARAGE_TOTAL, ");
            stb.append("     JAVG.STDDEV, ");
            stb.append("     '' AS DEV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_JUDGE_AVARAGE_DAT JAVG ");
            stb.append(" WHERE ");
            stb.append("     JAVG.ENTEXAMYEAR = '" + entexamYear + "' ");
            stb.append("     AND JAVG.APPLICANTDIV = '" + applicantDiv + "' ");
            stb.append("     AND JAVG.TESTDIV = '" + testDiv + "' ");
            stb.append("     AND JAVG.EXAM_TYPE = '1' ");
            stb.append("     AND JAVG.TESTSUBCLASSCD = '" + _subclassCd + "' ");
            return stb.toString();
        }
    }

    /** 印字データ */
    private class PrintData {
        final String _cnt;
        final String _avg;
        final String _stdDev;
        final String _dev;

        public PrintData(
                final String cnt,
                final String avg,
                final String stdDev,
                final String dev
        ) {
            _cnt = cnt;
            _avg = avg;
            _stdDev = stdDev;
            _dev = dev;
        }
    }
}

// eof

