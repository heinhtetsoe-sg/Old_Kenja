/*
 * $Id: e7c151eeff612bbd01bfc9c213c9cc525083f2d2 $
 *
 * 作成日: 2017/10/25
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
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

public class KNJL347Q {

    private static final Log log = LogFactory.getLog(KNJL347Q.class);

    private boolean _hasData;
    private final String GOUKEI = "B";
    private final String HEIKIN = "G";

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
        svf.VrSetForm("KNJL347Q.frm", 1);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度入試　平均点表");

        //科目名、平均
        int kamokuCnt = 1;
        for (Iterator itTestKamou = _param._testKamokuList.iterator(); itTestKamou.hasNext();) {
            final TestKamoku testKamoku = (TestKamoku) itTestKamou.next();
            svf.VrsOutn("CLASS_NAME", kamokuCnt, testKamoku._name);
            kamokuCnt++;
        }
        svf.VrsOutn("CLASS_NAME", 6, _param._testKamokuList.size() + "教科合計");
        svf.VrsOutn("CLASS_NAME", 7, _param._testKamokuList.size() + "教科平均");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int colCnt = 1;
            String befKey = "";
            int lineCnt = 1;
            while (rs.next()) {
                final String year = rs.getString("ENTEXAMYEAR");
                final String testDiv = rs.getString("TESTDIV");
                final String testDivName = rs.getString("ABBV1");
                final String testSubclasscd = rs.getString("TESTSUBCLASSCD");
                final String avg = rs.getString("CALC_AVG");
                final String setKey = year + testDiv;
                if (!"".equals(befKey) && !befKey.equals(setKey)) {
                    colCnt++;
                    lineCnt = 1;
                }
                if (GOUKEI.equals(testSubclasscd)) {
                    lineCnt = 6;
                }
                if (HEIKIN.equals(testSubclasscd)) {
                    lineCnt = 7;
                }
                svf.VrsOut("AVERAGE_NAME" + colCnt, year + testDivName);
                setAvgData(svf, avg, "AVERAGE" + colCnt, lineCnt);
                lineCnt++;
                befKey = setKey;
                _hasData = true;
            }
            svf.VrEndPage();

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setAvgData(final Vrw32alp svf, final String avg, final String setField, final int lineCnt) {
        final BigDecimal setVal = new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP);
        svf.VrsOutn(setField, lineCnt, setVal.toString());
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     JUDGE.ENTEXAMYEAR, ");
        stb.append("     JUDGE.TESTDIV, ");
        stb.append("     L024.ABBV1, ");
        stb.append("     JUDGE.TESTSUBCLASSCD, ");
        stb.append("     JUDGE.CALC_AVG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_JUDGE_AVARAGE_DAT JUDGE ");
        stb.append("     LEFT JOIN NAME_MST L024 ON L024.NAMECD1 = 'L024' ");
        stb.append("          AND JUDGE.TESTDIV = L024.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     JUDGE.ENTEXAMYEAR BETWEEN '" + (Integer.parseInt(_param._entexamyear) - 2) + "' AND '" + _param._entexamyear + "' ");
        stb.append("     AND JUDGE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     JUDGE.ENTEXAMYEAR DESC, ");
        stb.append("     CASE WHEN JUDGE.TESTDIV = '9' THEN '1' ELSE JUDGE.TESTDIV END, ");
        stb.append("     JUDGE.TESTDIV, ");
        stb.append("     TESTSUBCLASSCD ");
        return stb.toString();
    }

    private class TestKamoku {
        final String _cd;
        final String _name;
        public TestKamoku(
                final String cd,
                final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57216 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _schoolkind;
        private final List _testKamokuList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _testKamokuList = getTestKamokuList(db2);
        }

        private List getTestKamokuList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = "SELECT NAMECD2, NAME2 FROM NAME_MST WHERE NAMECD1 = 'L009' AND NAME2 IS NOT NULL ORDER BY NAMECD2";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME2");
                    final TestKamoku testKamoku = new TestKamoku(cd, name);
                    retList.add(testKamoku);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retList;
        }

    }
}

// eof
