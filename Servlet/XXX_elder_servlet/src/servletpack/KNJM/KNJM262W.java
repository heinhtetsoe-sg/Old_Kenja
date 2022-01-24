//kanji=漢字
/*
 *
 * 作成日: 2021/03/01
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
*
*   ＜ＫＮＪＭ２６２Ｗ レポート受付通数集計表＞
*/

public class KNJM262W {

    private static final Log log = LogFactory.getLog("KNJM262W.class");

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map<String, Staff> printMap = getPrintMap(db2); //出力データ
        if (printMap.isEmpty()) {
            return;
        }

        svf.VrSetForm("KNJM262W.frm", 4);
        svf.VrsOut("TITLE",  _param._loginYear + "年度　レポート受付通数集計表");
        svf.VrsOut("RECEPT_DATE",  _param._sDate + " ～ " + _param._eDate + " 受付分");
        svf.VrsOut("PRINT_DATE",  "作成日 " + _param._loginDate.replace("-", "/"));

        int cnt = 1;

        for (Iterator<String> stfIte = printMap.keySet().iterator(); stfIte.hasNext();) {
            final String staffCd = stfIte.next();
            final Staff staff = printMap.get(staffCd);

            final int keta = KNJ_EditEdit.getMS932ByteLength(staff._staffName);
            final String field = keta <= 30 ? "1" : "2";
            svf.VrsOut("TR_NAME" + field, staff._staffName);
            for (Iterator<String> repIte = staff.reportMap.keySet().iterator(); repIte.hasNext();) {
                final String subclassCd = repIte.next();
                final Report report = staff.reportMap.get(subclassCd);

                if ("ALL".equals(staff._staffCd)) {
                    svf.VrsOut("TOTAL_NAME2", "合計");
                    svf.VrsOut("TOTAL_COUNT", report._count);
                } else {
                    svf.VrsOut("GRPCD", String.valueOf(cnt));

                    if ("TOTAL".equals(subclassCd)) {
                        svf.VrsOut("TOTAL_NAME1", "計");
                    } else {
                        svf.VrsOut("SUBCLASS_NAME", report._subclassName);
                    }
                    svf.VrsOut("COUNT", report._count);
                }

                svf.VrEndRecord();
                _hasData = true;
            }
            cnt++;
        }

        svf.VrEndPage();
    }

    private Map getPrintMap(final DB2UDB db2) throws SQLException {
        Map<String, Staff> retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getReportSql();
        log.debug(" report sql =" + sql);

        try{
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String staffCd = rs.getString("STAFFCD");
                final String staffName = rs.getString("STAFFNAME_SHOW");
                final String subclassKey = rs.getString("SUBCLASSKEY");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String count = rs.getString("COUNT");

                if (!retMap.containsKey(staffCd)) {
                    retMap.put(staffCd, new Staff(staffCd, staffName));
                }

                final Staff staff = retMap.get(staffCd);
                staff.reportMap.put(subclassKey, new Report(subclassKey, subclassName, count));
            }
        } catch (final SQLException e) {
            log.error("レポート件数取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    //レポート件数取得
    private String getReportSql() {
        final String sDate = _param._sDate.replace("/", "-");
        final String eDate = _param._eDate.replace("/", "-");

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REP_PRESENT AS ( ");
        stb.append(" SELECT ");
        stb.append("     T0.*, ");
        stb.append("     T1.STAFFCD AS STAFFCD2, ");
        stb.append("     T2.STAFFNAME AS STAFFNAME, ");
        stb.append("     T3.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT T0 ");
        stb.append(" INNER JOIN ");
        stb.append("     REP_STF_DAT T1 ");
        stb.append("      ON T1.YEAR = T0.YEAR ");
        stb.append("     AND T1.CLASSCD = T0.CLASSCD ");
        stb.append("     AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
        stb.append("     AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
        stb.append("     AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
        stb.append("     AND T1.CHAIRCD = T0.CHAIRCD ");
        stb.append(" INNER JOIN ");
        stb.append("     STAFF_MST T2 ");
        stb.append("      ON T2.STAFFCD = T1.STAFFCD ");
        stb.append(" INNER JOIN ");
        stb.append("     SUBCLASS_MST T3 ");
        stb.append("      ON T3.CLASSCD = T0.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T0.SCHOOL_KIND ");
        stb.append("     AND T3.CURRICULUM_CD = T0.CURRICULUM_CD ");
        stb.append("     AND T3.SUBCLASSCD = T0.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T0.YEAR = '" + _param._loginYear + "' AND ");
        stb.append("     T0.RECEIPT_DATE BETWEEN '" + sDate + "' AND '" + eDate + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     VALUE(T1.STAFFCD2, 'ALL') AS STAFFCD, ");
        stb.append("     VALUE(T1.STAFFNAME, '') AS STAFFNAME_SHOW, ");
        stb.append("     VALUE(T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, 'TOTAL') AS SUBCLASSKEY, ");
        stb.append("     VALUE(T1.SUBCLASSNAME, '計') AS SUBCLASSNAME, ");
        stb.append("     COUNT(T1.SCHREGNO) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT T1 ");
        stb.append(" GROUP BY ");
        stb.append(" GROUPING SETS( ");
        stb.append("     (), (T1.STAFFCD2, T1.STAFFNAME), (T1.STAFFCD2, T1.STAFFNAME, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SUBCLASSNAME) ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     STAFFCD2, SUBCLASSKEY ");

        return stb.toString();
    }

    /** 教員クラス */
    private class Staff {
        final String _staffCd;
        final String _staffName;
        final Map<String, Report> reportMap = new LinkedMap();

        public Staff(final String staffCd, final String staffName) {
            _staffCd = staffCd;
            _staffName = staffName;
        }
    }

    /** レポートクラス */
    private class Report {
        final String _subclassKey;
        final String _subclassName;
        final String _count; //レポート件数

        public Report(final String subclassKey, final String subclassName, final String count) {
            _subclassKey = subclassKey;
            _subclassName = subclassName;
            _count = count;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _loginYear; //年度
        final String _loginDate; //作成日
        final String _sDate; //開始日
        final String _eDate; //終了日

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE");
            _sDate = request.getParameter("SDATE");
            _eDate = request.getParameter("EDATE");
        }
    }
}

// eof
