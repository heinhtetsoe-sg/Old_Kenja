/*
 * $Id: 579abd081f187f4d1fc1a13c724fec12d0600f46 $
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP956 {

    private static final Log log = LogFactory.getLog(KNJP956.class);

    private final String SYUUNYUU = "01";
    private final String ZATUSYUUNYUU = "02";
    private final String SISYUTU = "03";
    private final String YOBI = "04";

    private final String INCOME = "1";
    private final String OUTGO = "2";

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
        final Map printLmst = getLmst(db2);

        svf.VrSetForm("KNJP956.frm", 1);

        for (Iterator iterator = printLmst.keySet().iterator(); iterator.hasNext();) {
            final String lCd = (String) iterator.next();
            final String lName = (String) printLmst.get(lCd);

            final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
            final String schoolName = _param._schoolName + "　学校長　殿";
            svf.VrsOut("TITLE", nendo + schoolName + "　" + lName + "会計予算書");

            //合計用
            final TotalData totalSyuuNyuu = new TotalData();

            //収入
            final List printSyuuNyuu = getList(db2, SYUUNYUU, INCOME, lCd);
            printOut(svf, printSyuuNyuu, totalSyuuNyuu, INCOME, 1, 6);
            //雑収入
            final List printZatu = getList(db2, ZATUSYUUNYUU, INCOME, lCd);
            printOut(svf, printZatu, totalSyuuNyuu, INCOME, 7, 7);
            //合計
            setPrintData(svf, INCOME, "", String.valueOf(totalSyuuNyuu._yosan), String.valueOf(totalSyuuNyuu._zenYosan), String.valueOf(totalSyuuNyuu._zouGen), "", 8);

            //合計用
            final TotalData totalSisyutu = new TotalData();

            //支出
            final List printSisyutu = getList(db2, SISYUTU, OUTGO, lCd);
            printOut(svf, printSisyutu, totalSisyutu, OUTGO, 1, 10);
            //予備費
            final List printYobi = getList(db2, YOBI, OUTGO, lCd);
            printOut(svf, printYobi, totalSisyutu, OUTGO, 11, 11);
            //合計
            setPrintData(svf, OUTGO, "", String.valueOf(totalSisyutu._yosan), String.valueOf(totalSisyutu._zenYosan), String.valueOf(totalSisyutu._zouGen), "", 12);

            svf.VrEndPage();
        }
    }

    private Map getLmst(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YOSAN_L_CD, ");
            stb.append("     L1.LEVY_L_NAME ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_YOSAN_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.YOSAN_L_CD = L1.LEVY_L_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YOSAN_L_CD, ");
            stb.append("     L1.LEVY_L_NAME ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YOSAN_L_CD ");

            log.debug(" sql =" + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String levyLCd    = rs.getString("YOSAN_L_CD");
                final String levyLName  = rs.getString("LEVY_L_NAME");

                retMap.put(levyLCd, levyLName);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    /**
     *
     * @param svf
     * @param printList 印字リスト
     * @param totalData 合計クラス
     * @param soeji     収入/支出区分
     * @param startLine 印字開始行
     * @param endLine   印字最終行
     */
    private void printOut(final Vrw32alp svf, final List printList, final TotalData totalData, final String soeji, final int startLine, final int endLine) {

        int lineCnt = startLine;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (endLine < lineCnt) {
                break;
            }
            YosanDat yosanDat = (YosanDat) iterator.next();

            setPrintData(svf, soeji, yosanDat._levyMName, yosanDat._requestGk, yosanDat._lastRequestGk, yosanDat._zougen, yosanDat._requestReason, lineCnt);
            totalData._yosan += Integer.parseInt(yosanDat._requestGk);
            totalData._zenYosan += Integer.parseInt(yosanDat._lastRequestGk);
            totalData._zouGen += Integer.parseInt(yosanDat._zougen);

            _hasData = true;
            lineCnt++;
        }
    }

    private void setPrintData(final Vrw32alp svf, final String soeji, final String levyMName, final String requestGk, final String lastRequestGk, final String zougen, final String remark, final int line) {
        final String billField = KNJ_EditEdit.getMS932ByteLength(levyMName) > 24 ? "_3" : KNJ_EditEdit.getMS932ByteLength(levyMName) > 16 ? "_2" : "_1";
        svf.VrsOutn("BILL_NAME" + soeji + billField, line, levyMName);
        svf.VrsOutn("BUDGET" + soeji, line, requestGk);
        svf.VrsOutn("LAST_BUDGET" + soeji, line, lastRequestGk);
        svf.VrsOutn("FLUCT" + soeji, line, zougen);
        svf.VrsOutn("REMARK" + soeji, line, remark);
    }

    private List getList(final DB2UDB db2, final String setYosanDiv, final String inOutDiv, final String lCd) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getYosanSql(setYosanDiv, inOutDiv, lCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year           = rs.getString("YEAR");
                final String yosanDiv       = rs.getString("YOSAN_DIV");
                final String yosanLCd       = rs.getString("YOSAN_L_CD");
                final String levyLName      = rs.getString("LEVY_L_NAME");
                final String yosanMCd       = rs.getString("YOSAN_M_CD");
                final String levyMName      = ZATUSYUUNYUU.equals(setYosanDiv) || YOBI.equals(setYosanDiv) ? "" : rs.getString("LEVY_M_NAME");
                final String requestDate    = rs.getString("REQUEST_DATE");
                final String requestReason  = rs.getString("REQUEST_REASON");
                final String requestGk      = rs.getString("REQUEST_GK");
                final String remark         = rs.getString("REMARK");
                final String lastRequestGk  = rs.getString("LAST_REQUEST_GK");
                final String zougen         = rs.getString("ZOUGEN");

                final YosanDat yosanDat = new YosanDat(year, yosanDiv, yosanLCd, levyLName, yosanMCd, levyMName, requestDate, requestReason, requestGk, remark, lastRequestGk, zougen);
                retList.add(yosanDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getYosanSql(final String yosanDiv, final String inOutDiv, final String lCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.YOSAN_DIV, ");
        stb.append("     T1.YOSAN_L_CD, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     T1.YOSAN_M_CD, ");
        stb.append("     L2.LEVY_M_NAME, ");
        stb.append("     T1.REQUEST_DATE, ");
        stb.append("     T1.REQUEST_REASON, ");
        stb.append("     VALUE(T1.REQUEST_GK, 0) AS REQUEST_GK, ");
        stb.append("     T1.REMARK, ");
        stb.append("     VALUE(T2.REQUEST_GK, 0) AS LAST_REQUEST_GK, ");
        stb.append("     VALUE(T1.REQUEST_GK, 0) - VALUE(T2.REQUEST_GK, 0) AS ZOUGEN ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_YOSAN_DAT T1 ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.YOSAN_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.YOSAN_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.YOSAN_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '" + inOutDiv + "' ");
        stb.append("     LEFT JOIN LEVY_REQUEST_YOSAN_DAT T2 ON T2.YEAR = '" + (Integer.parseInt(_param._year) - 1) + "' ");
        stb.append("          AND T1.YOSAN_DIV = T2.YOSAN_DIV ");
        stb.append("          AND T1.YOSAN_L_CD = T2.YOSAN_L_CD ");
        stb.append("          AND T1.YOSAN_M_CD = T2.YOSAN_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.YOSAN_DIV = '" + yosanDiv + "' ");
        stb.append("     AND T1.YOSAN_L_CD = '" + lCd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YOSAN_L_CD, ");
        stb.append("     T1.YOSAN_M_CD ");
        return stb.toString();
    }

    private class YosanDat {
        private final String _year;
        private final String _yosanDiv;
        private final String _yosanLCd;
        private final String _levyLName;
        private final String _yosanMCd;
        private final String _levyMName;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestGk;
        private final String _remark;
        private final String _lastRequestGk;
        private final String _zougen;

        public YosanDat(
                final String year,
                final String yosanDiv,
                final String yosanLCd,
                final String levyLName,
                final String yosanMCd,
                final String levyMName,
                final String requestDate,
                final String requestReason,
                final String requestGk,
                final String remark,
                final String lastRequestGk,
                final String zougen
        ) {
                _year           = year;
                _yosanDiv       = yosanDiv;
                _yosanLCd       = yosanLCd;
                _levyLName      = levyLName;
                _yosanMCd       = yosanMCd;
                _levyMName      = levyMName;
                _requestDate    = requestDate;
                _requestReason  = requestReason;
                _requestGk      = requestGk;
                _remark         = remark;
                _lastRequestGk  = lastRequestGk;
                _zougen         = zougen;
        }

    }

    private class TotalData {
        private int _yosan;
        private int _zenYosan;
        private int _zouGen;
        public TotalData() {
            _yosan = 0;
            _zenYosan = 0;
            _zouGen = 0;
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
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgid = request.getParameter("PRGID");
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

