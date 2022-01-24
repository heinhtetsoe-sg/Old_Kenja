/*
 * $Id: 074fdea0c697920a2c5e5b4bae49727eb1ce9162 $
 *
 * 作成日: 2017/01/19
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJE375 {

    private static final Log log = LogFactory.getLog(KNJE375.class);

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

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "KNJE375.frm";
        final String title = KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + ("P".equals(_param._schoolKind) ? "出身園" : "出身学校") + "別人数一覧";
        final List finschoolPageList = getPageList(Finschool.getFinschoolList(db2, _param), 75); // 行
        final List yearPageList = getPageList(_param._yearList, 13); // 列
        
        for (int finpi = 0; finpi < finschoolPageList.size(); finpi++) {

            for (int ypi = 0; ypi < yearPageList.size(); ypi++) {
                final List yearList = (List) yearPageList.get(ypi);

                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日
                svf.VrsOut("SCHOOL_HEADER", "P".equals(_param._schoolKind) ? "園名" : "学校名"); // 学校ヘッダ
                
                for (int yi = 0; yi < yearList.size(); yi++) {
                    final String year = (String) yearList.get(yi);
                    svf.VrsOut("YEAR" + String.valueOf(yi + 1), year); // 年度
                }

                final List dataList = (List) finschoolPageList.get(finpi);
                for (int j = 0; j < dataList.size(); j++) {
                    final Finschool finschool = (Finschool) dataList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("SCHOOL_CD", line, finschool._finschoolcd); // 学校コード
                    svf.VrsOutn("CITY_NAME", line, finschool._finschoolDistcdName); // 市町村名
                    svf.VrsOutn("DIV", line, finschool._finschoolDivName); // 国公立区分
                    svf.VrsOutn(getMS932ByteLength(finschool._finschoolName) <= 30 ? "SCHOOL_NAME1" : "SCHOOL_NAME2", line, finschool._finschoolName); // 学校名
                    for (int yi = 0; yi < yearList.size(); yi++) {
                        final String year = (String) yearList.get(yi);
                        svf.VrsOutn("ENT" + String.valueOf(yi + 1), line, (String) finschool._yearRegdcountMap.get(year)); // 入学者数
                    }
                    svf.VrsOutn("ENT_SUM", line, finschool.getTotal()); // 入学者数計
                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
        
    }

    private static class Finschool {
        final String _finschoolcd;
        final String _finschoolName;
        final String _finschoolDiv;
        final String _finschoolDivName;
        final String _finschoolDistcd;
        final String _finschoolDistcdName;
        final Map _yearRegdcountMap = new HashMap();

        Finschool(
            final String finschoolcd,
            final String finschoolName,
            final String finschoolDiv,
            final String finschoolDivName,
            final String finschoolDistcd,
            final String finschoolDistcdName
        ) {
            _finschoolcd = finschoolcd;
            _finschoolName = finschoolName;
            _finschoolDiv = finschoolDiv;
            _finschoolDivName = finschoolDivName;
            _finschoolDistcd = finschoolDistcd;
            _finschoolDistcdName = finschoolDistcdName;
        }

        public String getTotal() {
            int total = 0;
            for (final Iterator it = _yearRegdcountMap.values().iterator(); it.hasNext();) {
                final String count = (String) it.next();
                if (NumberUtils.isDigits(count)) {
                    total += Integer.parseInt(count);
                }
            }
            return String.valueOf(total);
        }

        public static List getFinschoolList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            final Map finschoolMap = new HashMap();
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String finschoolcd = rs.getString("FINSCHOOLCD");
                    if (null == finschoolMap.get(finschoolcd)) {
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        final String finschoolDiv = rs.getString("FINSCHOOL_DIV");
                        final String finschoolDivName = rs.getString("FINSCHOOL_DIV_NAME");
                        final String finschoolDistcd = rs.getString("FINSCHOOL_DISTCD");
                        final String finschoolDistcdName = rs.getString("FINSCHOOL_DISTCD_NAME");
                        final Finschool finschool = new Finschool(finschoolcd, finschoolName, finschoolDiv, finschoolDivName, finschoolDistcd, finschoolDistcdName);
                        finschoolMap.put(finschoolcd, finschool);
                        list.add(finschool);
                    }
                    final Finschool finschool = (Finschool) finschoolMap.get(finschoolcd);
                    final String year = rs.getString("YEAR");
                    final String count = rs.getString("COUNT");
                    finschool._yearRegdcountMap.put(year, count);
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
            stb.append(" WITH REGD_MIN_GRADE AS ( ");
            stb.append("     SELECT T1.SCHREGNO, MIN(T1.GRADE) AS GRADE ");
            stb.append("     FROM SCHREG_REGD_DAT T1  ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     GROUP BY T1.SCHREGNO ");
            stb.append(" ), REGD_FINSCHOOL AS ( ");
            stb.append("     SELECT ");
            stb.append("         REGD.YEAR ");
            stb.append("         , BASE.FINSCHOOLCD ");
            stb.append("     FROM (SELECT DISTINCT T1.YEAR, T1.SCHREGNO ");
            stb.append("         FROM SCHREG_REGD_DAT T1 ");
            stb.append("         INNER JOIN REGD_MIN_GRADE T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         ) REGD ");
            stb.append("         INNER JOIN SCHREG_ENT_GRD_HIST_DAT BASE ON BASE.SCHREGNO = REGD.SCHREGNO AND BASE.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR BETWEEN '" + String.valueOf(param._minYear) + "' AND '" + String.valueOf(param._maxYear) + "' ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            stb.append("     T1.YEAR ");
            stb.append("     , T1.FINSCHOOLCD ");
            stb.append("     , FINM.FINSCHOOL_NAME ");
            stb.append("     , FINM.FINSCHOOL_DIV ");
            stb.append("     , NML015.NAME1 AS FINSCHOOL_DIV_NAME ");
            stb.append("     , FINM.FINSCHOOL_DISTCD ");
            stb.append("     , NML001.NAME1 AS FINSCHOOL_DISTCD_NAME ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append(" FROM REGD_FINSCHOOL T1 ");
            stb.append("     INNER JOIN FINSCHOOL_MST FINM ON FINM.FINSCHOOLCD = T1.FINSCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST NML015 ON NML015.NAMECD1 = 'L015' ");
            stb.append("         AND NML015.NAMECD2 = FINM.FINSCHOOL_DIV ");
            stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' ");
            stb.append("         AND NML001.NAMECD2 = FINM.FINSCHOOL_DISTCD ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");
            stb.append("     , T1.FINSCHOOLCD ");
            stb.append("     , FINM.FINSCHOOL_NAME ");
            stb.append("     , FINM.FINSCHOOL_DIV ");
            stb.append("     , FINM.FINSCHOOL_DISTCD ");
            stb.append("     , NML015.NAME1 ");
            stb.append("     , NML001.NAME1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.FINSCHOOLCD ");
            stb.append("     , T1.YEAR ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _schoolKind;
        final int _minYear;
        final int _maxYear;
        
        final List _yearList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            final String _paramMinYear = request.getParameter("MIN_YEAR");
            final String _paramMaxYear = request.getParameter("MAX_YEAR");
            _maxYear = NumberUtils.isDigits(_paramMaxYear) ? Integer.parseInt(_paramMaxYear) : Integer.parseInt(_ctrlYear);
            _minYear = NumberUtils.isDigits(_paramMinYear) ? Integer.parseInt(_paramMinYear) : Integer.parseInt(_ctrlYear);
            
            _yearList = new ArrayList();
            for (int y = _minYear; y <= _maxYear; y++) {
                _yearList.add(String.valueOf(y));
            }
        }
    }
}

// eof

