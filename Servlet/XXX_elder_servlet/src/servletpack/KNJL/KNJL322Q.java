/*
 * $Id$
 *
 * 作成日: 2017/04/05
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL322Q {

    private static final Log log = LogFactory.getLog(KNJL322Q.class);

    private boolean _hasData;

    private Param _param;

    private final String SUISEN = "2";
    private final String IPPAN = "3";
    private final String RECEPT_TESTDIV = "5";

    private final String KOKUGO = "1";
    private final String SUUGAKU = "2";
    private final String RIKA = "3";
    private final String SYAKAI = "4";
    private final String EIGO = "5";
    private final String SHOURON = "6";

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
        final int maxLine = 43;
        final List pageList = getPageList(getList(db2, sql()), maxLine);
        final String form = SUISEN.equals(_param._testDiv0) ? "KNJL322Q_1.frm" : "KNJL322Q_2.frm";
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        final String sortName = "2".equals(_param._sortDiv) ? "高得点順" : "受験番号順";
        final Map testSubclassName = getTestSubclassNameMap(db2);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", nendo + "　" + _param._testdiv0Name1 + "　得点チェックリスト（" + sortName + "）");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));
            if (IPPAN.equals(_param._testDiv0)) {
                svf.VrsOut("CLASS_NAME1", getString(testSubclassName, KOKUGO));
                svf.VrsOut("CLASS_NAME2", getString(testSubclassName, EIGO));
                svf.VrsOut("CLASS_NAME3", getString(testSubclassName, SUUGAKU));
                svf.VrsOut("CLASS_NAME4", "合計");
                svf.VrsOut("CLASS_NAME5", "面接");
            }
            if (SUISEN.equals(_param._testDiv0)) {
                svf.VrsOut("CLASS_NAME1", getString(testSubclassName, EIGO));
                svf.VrsOut("CLASS_NAME2", getString(testSubclassName, SUUGAKU));
                svf.VrsOut("CLASS_NAME3", getString(testSubclassName, KOKUGO));
                svf.VrsOut("CLASS_NAME4", getString(testSubclassName, RIKA));
                svf.VrsOut("CLASS_NAME5", getString(testSubclassName, SYAKAI));
                svf.VrsOut("CLASS_NAME6", "合計");
                svf.VrsOut("CLASS_NAME7", "面接");
                svf.VrsOut("CLASS_NAME8", "作文");
            }

            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("EXAM_NO", getString(row, "EXAMNO"));
                svf.VrsOut("NAME", getString(row, "NAME"));
                if (IPPAN.equals(_param._testDiv0)) {
                    svf.VrsOut("EXAM_DIV", getString(row, "TESTDIV0_ABBV1"));
                }
                svf.VrsOut("JHSCHOOL_NAME", getString(row, "FINSCHOOL_NAME"));
                if (IPPAN.equals(_param._testDiv0)) {
                    svf.VrsOut("SCORE1", getString(row, "KOKUGO_SCORE"));
                    svf.VrsOut("SCORE2", getString(row, "EIGO_SCORE"));
                    svf.VrsOut("SCORE3", getString(row, "SUUGAKU_SCORE"));
                    svf.VrsOut("SCORE4", getString(row, "TOTAL4"));
                    svf.VrsOut("SCORE5", getString(row, "INTERVIEW_NAME"));
                }
                if (SUISEN.equals(_param._testDiv0)) {
                    svf.VrsOut("SCORE1", getString(row, "EIGO_SCORE"));
                    svf.VrsOut("SCORE2", getString(row, "SUUGAKU_SCORE"));
                    svf.VrsOut("SCORE3", getString(row, "KOKUGO_SCORE"));
                    svf.VrsOut("SCORE4", getString(row, "RIKA_SCORE"));
                    svf.VrsOut("SCORE5", getString(row, "SYAKAI_SCORE"));
                    svf.VrsOut("SCORE6", getString(row, "TOTAL4"));
                    svf.VrsOut("SCORE7", getString(row, "INTERVIEW_NAME"));
                    svf.VrsOut("SCORE8", getString(row, "COMPOSITION_NAME"));
                }

                svf.VrEndRecord();
                _hasData = true;
            }
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

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
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

    private Map getTestSubclassNameMap(final DB2UDB db2) {
        final Map map = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' ORDER BY NAMECD2 ";
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return map;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map m = new HashMap();
                m.put("EXAMNO", rs.getString("EXAMNO"));
                m.put("NAME", rs.getString("NAME"));
                m.put("TESTDIV0", rs.getString("TESTDIV0"));
                m.put("TESTDIV0_ABBV1", rs.getString("TESTDIV0_ABBV1"));
                m.put("FS_CD", rs.getString("FS_CD"));
                m.put("FINSCHOOL_NAME", rs.getString("FINSCHOOL_NAME"));
                m.put("KOKUGO_CD", rs.getString("KOKUGO_CD"));
                m.put("KOKUGO_SCORE", rs.getString("KOKUGO_SCORE"));
                m.put("SUUGAKU_CD", rs.getString("SUUGAKU_CD"));
                m.put("SUUGAKU_SCORE", rs.getString("SUUGAKU_SCORE"));
                m.put("RIKA_CD", rs.getString("RIKA_CD"));
                m.put("RIKA_SCORE", rs.getString("RIKA_SCORE"));
                m.put("SYAKAI_CD", rs.getString("SYAKAI_CD"));
                m.put("SYAKAI_SCORE", rs.getString("SYAKAI_SCORE"));
                m.put("EIGO_CD", rs.getString("EIGO_CD"));
                m.put("EIGO_SCORE", rs.getString("EIGO_SCORE"));
                m.put("TOTAL2", rs.getString("TOTAL2"));
                m.put("TOTAL4", rs.getString("TOTAL4"));
                m.put("INTERVIEW_VALUE", rs.getString("INTERVIEW_VALUE"));
                m.put("INTERVIEW_NAME", rs.getString("INTERVIEW_NAME"));
                m.put("COMPOSITION_VALUE", rs.getString("COMPOSITION_VALUE"));
                m.put("COMPOSITION_NAME", rs.getString("COMPOSITION_NAME"));
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        stb.append("     B1.TESTDIV0, ");
        stb.append("     N2.ABBV1 AS TESTDIV0_ABBV1, ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        stb.append("     S1.TESTSUBCLASSCD AS KOKUGO_CD, ");
        stb.append("     S1.SCORE AS KOKUGO_SCORE, ");
        stb.append("     S2.TESTSUBCLASSCD AS SUUGAKU_CD, ");
        stb.append("     S2.SCORE AS SUUGAKU_SCORE, ");
        stb.append("     S3.TESTSUBCLASSCD AS RIKA_CD, ");
        stb.append("     S3.SCORE AS RIKA_SCORE, ");
        stb.append("     S4.TESTSUBCLASSCD AS SYAKAI_CD, ");
        stb.append("     S4.SCORE AS SYAKAI_SCORE, ");
        stb.append("     S5.TESTSUBCLASSCD AS EIGO_CD, ");
        stb.append("     S5.SCORE AS EIGO_SCORE, ");
        stb.append("     R1.TOTAL2, ");
        stb.append("     R1.TOTAL4, ");
        stb.append("     I1.INTERVIEW_VALUE, ");
        //基準テスト対象者の面接は、空欄とする。
        if (IPPAN.equals(_param._testDiv0)) {
            stb.append("     CASE WHEN B1.TESTDIV0 != '" + _param._testDiv0 + "' THEN '' ELSE N1.NAME1 END AS INTERVIEW_NAME, ");
        } else {
            stb.append("     N1.NAME1 AS INTERVIEW_NAME, ");
        }
        stb.append("     I1.COMPOSITION_VALUE, ");
        stb.append("     N3.NAME1 AS COMPOSITION_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L045' AND N2.NAMECD2 = B1.TESTDIV0 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");

        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND R1.APPLICANTDIV = B1.APPLICANTDIV ");
        //一般入試選択で「基準テストを含める・含めない・のみ」
        if (IPPAN.equals(_param._testDiv0)) {
            stb.append("             AND R1.TESTDIV = '" + RECEPT_TESTDIV + "' ");
        } else {
            stb.append("             AND R1.TESTDIV = B1.TESTDIV ");
        }
        stb.append("             AND R1.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT S1 ON S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("             AND S1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("             AND S1.TESTDIV = R1.TESTDIV ");
        stb.append("             AND S1.RECEPTNO = R1.RECEPTNO ");
        stb.append("             AND S1.TESTSUBCLASSCD = '" + KOKUGO + "' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT S2 ON S2.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("             AND S2.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("             AND S2.TESTDIV = R1.TESTDIV ");
        stb.append("             AND S2.RECEPTNO = R1.RECEPTNO ");
        stb.append("             AND S2.TESTSUBCLASSCD = '" + SUUGAKU + "' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT S3 ON S3.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("             AND S3.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("             AND S3.TESTDIV = R1.TESTDIV ");
        stb.append("             AND S3.RECEPTNO = R1.RECEPTNO ");
        stb.append("             AND S3.TESTSUBCLASSCD = '" + RIKA + "' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT S4 ON S4.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("             AND S4.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("             AND S4.TESTDIV = R1.TESTDIV ");
        stb.append("             AND S4.RECEPTNO = R1.RECEPTNO ");
        stb.append("             AND S4.TESTSUBCLASSCD = '" + SYAKAI + "' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT S5 ON S5.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("             AND S5.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("             AND S5.TESTDIV = R1.TESTDIV ");
        stb.append("             AND S5.RECEPTNO = R1.RECEPTNO ");
        stb.append("             AND S5.TESTSUBCLASSCD = '" + EIGO + "' ");

        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT I1 ON I1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND I1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND I1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND I1.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L027' AND N1.NAMECD2 = I1.INTERVIEW_VALUE ");
        stb.append("     LEFT JOIN V_NAME_MST N3 ON N3.YEAR = B1.ENTEXAMYEAR AND N3.NAMECD1 = 'L047' AND N3.NAMECD2 = I1.COMPOSITION_VALUE ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VALUE(R1.JUDGEDIV,'') <> '4' ");
        //一般入試選択で「基準テストを含める・含めない・のみ」
        if (IPPAN.equals(_param._testDiv0)) {
            if ("1".equals(_param._kijunTestDiv)) {
                //全て(一般入試、基準テスト)
            } else if ("2".equals(_param._kijunTestDiv)) {
                //一般入試のみ
                stb.append("     AND B1.TESTDIV0  = '" + _param._testDiv0 + "' ");
            } else if ("3".equals(_param._kijunTestDiv)) {
                //基準テストのみ
                stb.append("     AND B1.TESTDIV0 != '" + _param._testDiv0 + "' ");
            }
        } else {
            stb.append("     AND B1.TESTDIV0 = '" + _param._testDiv0 + "' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sortDiv)) {
            stb.append("     VALUE(R1.TOTAL4,-1) DESC, ");
        }
        stb.append("     B1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv0;
        private final String _sortDiv;
        private final String _kijunTestDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdiv0Name1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv0 = request.getParameter("TESTDIV");//NAME_MST L045
            _sortDiv = request.getParameter("TAISYOU");//1:受験番号順、2:高得点順
            _kijunTestDiv = request.getParameter("KIJUN_TEST_DIV");//基準テスト（1:含める　2:含めない　3:のみ）
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdiv0Name1 = StringUtils.defaultString(getNameMst(db2, "NAME1", "L045", _testDiv0));
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof

