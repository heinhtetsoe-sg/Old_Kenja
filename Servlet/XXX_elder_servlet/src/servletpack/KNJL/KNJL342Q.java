/*
 * $Id: 90ee2653723b8747389960a36802ee5a35a11b0e $
 *
 * 作成日: 2017/04/11
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL342Q {

    private static final Log log = LogFactory.getLog(KNJL342Q.class);

    private boolean _hasData;

    private Param _param;

    private final String SUISEN = "2";
    private final String IPPAN = "3";

    private final String KOKUGO = "1";
    private final String SUUGAKU = "2";
    private final String RIKA = "3";
    private final String EIGO = "5";
    private final String SHOURON = "6";
    private final String KISOGAKU = "S";
    private final String GOUKEI = "T";

    private final String COURSE_FT = "11";
    private final String COURSE_SP = "12";

    private final String KENNAI = "19";

    private final String KOUCHOU_SUISEN = "3";
    private final String JIKO_SUISEN = "4";
    private final String SUNCHUUSEI = "9";

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
        final String form = "KNJL342Q.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度";

        svf.VrSetForm(form, 4);
        svf.VrsOut("TITLE", nendo + "　" + _param._testdiv0Name1 + "　成績結果平均点一覧");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));

        if (SUISEN.equals(_param._testDiv0)) {
            svf.VrsOut("CLASS_NAME1", getString(_param._testSubclassName, EIGO));
            svf.VrsOut("CLASS_NAME2", getString(_param._testSubclassName, SUUGAKU));
            svf.VrsOut("CLASS_NAME3", getString(_param._testSubclassName, KOKUGO));
            svf.VrsOut("CLASS_NAME4", getString(_param._testSubclassName, RIKA));
            svf.VrsOut("CLASS_NAME5", "合計");
            svf.VrsOut("CLASS_NAME6", getString(_param._testSubclassName, SHOURON));
            svf.VrsOut("CLASS_NAME7", "総計");
        } else {
            svf.VrsOut("CLASS_NAME1", getString(_param._testSubclassName, KOKUGO));
            svf.VrsOut("CLASS_NAME2", getString(_param._testSubclassName, EIGO));
            svf.VrsOut("CLASS_NAME3", getString(_param._testSubclassName, SUUGAKU));
            svf.VrsOut("CLASS_NAME7", "合計");
        }
        svf.VrsOut("CLASS_NAME8", "人数");

        final Map itemMap = getItemMap();
        for (Iterator itItem = itemMap.keySet().iterator(); itItem.hasNext();) {
            final String itemNo = (String) itItem.next();

            svf.VrsOut("ITEM", getString(itemMap, itemNo));
            final List dataList = getList(db2, sql(itemNo));
            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                if (SUISEN.equals(_param._testDiv0)) {
                    if (EIGO.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE1", getString(row, "AVG"));
                    }
                    if (SUUGAKU.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE2", getString(row, "AVG"));
                    }
                    if (KOKUGO.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE3", getString(row, "AVG"));
                    }
                    if (RIKA.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE4", getString(row, "AVG"));
                    }
                    if (KISOGAKU.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE5", getString(row, "AVG"));
                    }
                    if (SHOURON.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE6", getString(row, "AVG"));
                    }
                } else {
                    if (KOKUGO.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE1", getString(row, "AVG"));
                    }
                    if (EIGO.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE2", getString(row, "AVG"));
                    }
                    if (SUUGAKU.equals(getString(row, "TESTSUBCLASSCD"))) {
                        svf.VrsOut("SCORE3", getString(row, "AVG"));
                    }
                }
                if (GOUKEI.equals(getString(row, "TESTSUBCLASSCD"))) {
                    svf.VrsOut("SCORE7", getString(row, "AVG"));
                    svf.VrsOut("SCORE8", getString(row, "CNT"));
                }
            }
            svf.VrEndRecord();
            _hasData = true;
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

    private static String getStringKeyEndsWith(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (m.containsKey(field)) {
            return getString(m, field);
        }
        final List keyList = new ArrayList(m.keySet());
        Collections.sort(keyList);
        String key = null;
        for (final Iterator keyIt = keyList.iterator(); keyIt.hasNext();) {
            final String k = (String) keyIt.next();
            if (null == k && null == field || null != k && k.endsWith(field)) { // フィールドをソートして最初に「field like '%{k}'」条件が合致した値
                key = k;
                break;
            }
        }
        return (String) m.get(key);
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

    private Map getItemMap() {
        final Map map = new TreeMap();
        if (SUISEN.equals(_param._testDiv0)) {
            map.put("01", "県内");
            map.put("02", "県外");
            map.put("03", getStringKeyEndsWith(_param._courseNameMap, COURSE_FT));
            map.put("04", getStringKeyEndsWith(_param._courseNameMap, COURSE_SP));
            map.put("05", "全体");
        }
        if (IPPAN.equals(_param._testDiv0)) {
            map.put("01", getString(_param._testDiv0Abbv1Map, IPPAN) + "（県内男）");
            map.put("02", getString(_param._testDiv0Abbv1Map, IPPAN) + "（県内女）");
            map.put("03", getString(_param._testDiv0Abbv1Map, IPPAN) + "（県内）");
            map.put("04", getString(_param._testDiv0Abbv1Map, IPPAN) + "（県外）");
            map.put("05", getString(_param._testDiv0Abbv1Map, IPPAN) + "（" + getStringKeyEndsWith(_param._courseAbbvMap, COURSE_FT) + "）");
            map.put("06", getString(_param._testDivAbbv1Map, KOUCHOU_SUISEN));
            map.put("07", getString(_param._testDivAbbv1Map, KOUCHOU_SUISEN) + "（県内）");
            map.put("08", getString(_param._testDivAbbv1Map, KOUCHOU_SUISEN) + "（県外）");
            map.put("09", getString(_param._testDivAbbv1Map, JIKO_SUISEN));
            map.put("10", getString(_param._testDiv0Abbv1Map, SUNCHUUSEI));
            map.put("11", getString(_param._testDiv0Abbv1Map, IPPAN) + "（" + getStringKeyEndsWith(_param._courseAbbvMap, COURSE_SP) + "）");
            map.put("12", getString(_param._testDiv0Abbv1Map, SUISEN) + "（" + getStringKeyEndsWith(_param._courseAbbvMap, COURSE_SP) + "）");
            map.put("13", "全体");
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

    private String sql(final String itemNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_RECEPT AS ( ");
        stb.append("     SELECT ");
        stb.append("         B1.DAI1_COURSECODE AS COURSE, ");
        stb.append("         B1.TESTDIV0 AS BASE_TESTDIV0, ");
        stb.append("         B1.TESTDIV AS BASE_TESTDIV, ");
        stb.append("         CASE WHEN F1.FINSCHOOL_PREF_CD  = '19' THEN '1' ");
        stb.append("              WHEN F1.FINSCHOOL_PREF_CD != '19' THEN '2' ");
        stb.append("         END AS KEN_NAIGAI, ");
        stb.append("         B1.SEX, ");
        stb.append("         R1.* ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECEPT_DAT R1 ");
        stb.append("         LEFT JOIN V_NAME_MST N1 ON N1.YEAR = R1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = R1.TESTDIV ");
        stb.append("         LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ON R1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("                 AND R1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("                 AND R1.EXAMNO = B1.EXAMNO ");
        stb.append("         LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     WHERE ");
        stb.append("         R1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("         AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND N1.ABBV3 = '" + _param._testDiv0 + "' ");
        if (SUISEN.equals(_param._testDiv0)) {
            if ("01".equals(itemNo)) {
                //map.put("01", "県内");
                stb.append("         AND B1.DAI1_COURSECODE LIKE= '%" + COURSE_FT + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD = '" + KENNAI + "' ");
            } else 
            if ("02".equals(itemNo)) {
                //map.put("02", "県外");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD <> '" + KENNAI + "' ");
            } else 
            if ("03".equals(itemNo)) {
                //map.put("03", "普通科");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
            } else 
            if ("04".equals(itemNo)) {
                //map.put("04", "スポーツ");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SP + "' ");
            } else 
            if ("05".equals(itemNo)) {
                //map.put("05", "全体");
            }
        }
        if (IPPAN.equals(_param._testDiv0)) {
            if ("01".equals(itemNo)) {
                //map.put("01", "一般（県内男）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + IPPAN + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD = '" + KENNAI + "' ");
                stb.append("         AND B1.SEX = '1' ");
            } else 
            if ("02".equals(itemNo)) {
                //map.put("02", "一般（県内女）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + IPPAN + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD = '" + KENNAI + "' ");
                stb.append("         AND B1.SEX = '2' ");
            } else 
            if ("03".equals(itemNo)) {
                //map.put("03", "一般（県内）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + IPPAN + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD = '" + KENNAI + "' ");
            } else 
            if ("04".equals(itemNo)) {
                //map.put("04", "一般（県外）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + IPPAN + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD <> '" + KENNAI + "' ");
            } else 
            if ("05".equals(itemNo)) {
                //map.put("05", "一般（普通科）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + IPPAN + "' ");
            } else 
            if ("06".equals(itemNo)) {
                //map.put("06", "校長");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV = '" + KOUCHOU_SUISEN + "' ");
            } else 
            if ("07".equals(itemNo)) {
                //map.put("07", "校長（県内）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV = '" + KOUCHOU_SUISEN + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD = '" + KENNAI + "' ");
            } else 
            if ("08".equals(itemNo)) {
                //map.put("08", "校長（県外）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV = '" + KOUCHOU_SUISEN + "' ");
                stb.append("         AND F1.FINSCHOOL_PREF_CD <> '" + KENNAI + "' ");
            } else 
            if ("09".equals(itemNo)) {
                //map.put("09", "自己");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV = '" + JIKO_SUISEN + "' ");
            } else 
            if ("10".equals(itemNo)) {
                //map.put("10", "駿中生");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_FT + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + SUNCHUUSEI + "' ");
            } else 
            if ("11".equals(itemNo)) {
                //map.put("11", "一般（スポーツ）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SP + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + IPPAN + "' ");
            } else 
            if ("12".equals(itemNo)) {
                //map.put("12", "推薦（スポーツ）");
                stb.append("         AND B1.DAI1_COURSECODE LIKE '%" + COURSE_SP + "' ");
                stb.append("         AND B1.TESTDIV0 = '" + SUISEN + "' ");
            } else 
            if ("13".equals(itemNo)) {
                //map.put("13", "全体");
            }
        }
        stb.append(" ) ");
        stb.append(" , T_AVG AS ( ");
        stb.append("     SELECT ");
        stb.append("         'T' AS TESTSUBCLASSCD, ");
        stb.append("         DECIMAL(ROUND(AVG(FLOAT(R1.TOTAL4)) * 10, 0) / 10, 5, 1) AS AVG, ");
        stb.append("         SUM(R1.TOTAL4) AS TOTAL, ");
        stb.append("         COUNT(R1.TOTAL4) AS CNT ");
        stb.append("     FROM ");
        stb.append("         T_RECEPT R1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'S' AS TESTSUBCLASSCD, ");
        stb.append("         DECIMAL(ROUND(AVG(FLOAT(R1.TOTAL2)) * 10, 0) / 10, 5, 1) AS AVG, ");
        stb.append("         SUM(R1.TOTAL2) AS TOTAL, ");
        stb.append("         COUNT(R1.TOTAL2) AS CNT ");
        stb.append("     FROM ");
        stb.append("         T_RECEPT R1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         S1.TESTSUBCLASSCD, ");
        stb.append("         DECIMAL(ROUND(AVG(FLOAT(S1.SCORE)) * 10, 0) / 10, 5, 1) AS AVG, ");
        stb.append("         SUM(S1.SCORE) AS TOTAL, ");
        stb.append("         COUNT(S1.SCORE) AS CNT ");
        stb.append("     FROM ");
        stb.append("         T_RECEPT R1 ");
        stb.append("         LEFT JOIN ENTEXAM_SCORE_DAT S1 ON R1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("                 AND S1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("                 AND S1.TESTDIV = R1.TESTDIV ");
        stb.append("                 AND S1.RECEPTNO = R1.RECEPTNO ");
        stb.append("     GROUP BY ");
        stb.append("         S1.TESTSUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     T_AVG ");
        stb.append(" WHERE ");
        stb.append("     0 < CNT ");
        stb.append(" ORDER BY ");
        stb.append("     TESTSUBCLASSCD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56907 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv0;
        private final String _loginDate;

        final String _applicantdivName;
        final String _testdiv0Name1;
        final Map _testSubclassName;
        final Map _testDiv0Abbv1Map;
        final Map _testDivAbbv1Map;
        final Map _courseAbbvMap;
        final Map _courseNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv0 = request.getParameter("TESTDIV");//NAME_MST L045
            _loginDate = request.getParameter("LOGIN_DATE");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdiv0Name1 = StringUtils.defaultString(getNameMst(db2, "NAME1", "L045", _testDiv0));
            _testSubclassName = getNameMstMap(db2, "L009");
            _testDiv0Abbv1Map = getNameMstMap(db2, "L045");
            _testDivAbbv1Map = getNameMstMap(db2, "L004");
            _courseAbbvMap = getCourseMstMap(db2, "EXAMCOURSE_ABBV");
            _courseNameMap = getCourseMstMap(db2, "EXAMCOURSE_NAME");
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

        private Map getNameMstMap(final DB2UDB db2, final String namecd1) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2 ";
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("L009".equals(namecd1)) {
                        map.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                    }
                    if ("L045".equals(namecd1)) {
                        map.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                    }
                    if ("L004".equals(namecd1)) {
                        map.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private Map getCourseMstMap(final DB2UDB db2, final String field) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlCourse();
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("EXAMCOURSECD"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private String sqlCourse() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     C1.EXAMCOURSECD, ");
            stb.append("     C1.EXAMCOURSE_NAME, ");
            stb.append("     C1.EXAMCOURSE_ABBV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST C1 ");
            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = C1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = C1.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("     C1.ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND C1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND N1.ABBV3 = '" + _testDiv0 + "' ");
            stb.append(" GROUP BY ");
            stb.append("     C1.EXAMCOURSECD, ");
            stb.append("     C1.EXAMCOURSE_NAME, ");
            stb.append("     C1.EXAMCOURSE_ABBV ");
            return stb.toString();
        }

    }
}

// eof

