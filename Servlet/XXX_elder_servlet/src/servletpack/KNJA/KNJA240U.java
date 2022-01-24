// kanji=漢字
/*
 * $Id: 9bcce8e28ee3318e36aef446cc2990459a8d958f $
 *
 * 作成日: 2017/08/14 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 9bcce8e28ee3318e36aef446cc2990459a8d958f $
 */
public class KNJA240U {

    private static final Log log = LogFactory.getLog("KNJA240U.class");

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
            log.fatal("$Revision: 74683 $");
            KNJServletUtils.debugParam(request, log);

            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            final Param param = new Param(db2, request);

            _param = param;

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
        final String form = "KNJA240U.frm";

        svf.VrSetForm(form, 1);
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度" + "　在籍生徒数調査票"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._kijunDate)); // 処理日

        //1.上表
        final int maxG = 3;
        final int maxC = 9;
        final List jhList = getJHList("SCHOOL_KIND", getList(db2, sql3(_param)), maxG);
        final List cList = getHrClassList(getList(db2, sql4(_param)), maxC);
        final Map dataMap = getDataMap(getList(db2, sql1(_param)));
        //学年名
        for (int jh = 0; jh < jhList.size(); jh++) {
            final List gradeList = (List) jhList.get(jh);
            final String schoolKind = getString(firstRow(gradeList), "SCHOOL_KIND");
            for (int g = 0; g < gradeList.size(); g++) {
                final Map gRow = (Map) gradeList.get(g);
                final String gradeCd = getString(gRow, "GRADE_CD");
                svf.VrsOut(schoolKind + "GRADE" + (g + 1), (null != gradeCd && NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : gradeCd) + "年");
            }
        }
        //組名
        for (int c = 0; c < cList.size(); c++) {
            final Map cRow = (Map) cList.get(c);
            svf.VrsOutn("HR_NAME", (c + 1), getString(cRow, "HR_CLASS_NAME1"));
        }
        svf.VrsOutn("HR_NAME", (maxC + 1), "計");
        //組ループ
        String[] fieldName = {"JNUM_1","JNUM_2","JNUM_3","JNUM_4","HNUM_1","HNUM_2","HNUM_3","HNUM_4","TOTAL"};
        int[] gradeTotal = {0,0,0,0,0,0,0,0,0};
        int[] gradeTotalS = {0,0,0,0,0,0,0,0,0};
        for (int c = 0; c < cList.size(); c++) {
            final Map cRow = (Map) cList.get(c);
            final int cLine = c + 1;
            //校種ループ
            for (int jh = 0; jh < jhList.size(); jh++) {
                final List gradeList = (List) jhList.get(jh);
                final String schoolKind = getString(firstRow(gradeList), "SCHOOL_KIND");
                //学年ループ
                for (int g = 0; g < gradeList.size(); g++) {
                    final Map gRow = (Map) gradeList.get(g);
                    final String key = getString(gRow, "GRADE") + getString(cRow, "HR_CLASS");
                    if (!dataMap.containsKey(key)) continue;

                    final Map row = (Map) dataMap.get(key);
                    if (g != 3 && g != 7 && g != 8) {
                        svf.VrsOutn(schoolKind + "NUM_" + (g + 1), cLine, notPrintZero(getString(row, "CNT"), "") + notPrintZero(getString(row, "CNT_S"), "S"));
                    }
                    //横計を計算
                    int sNo = 0;
                    if ("J".equals(schoolKind)) sNo = 0;
                    if ("H".equals(schoolKind)) sNo = maxG + 1;
                    gradeTotal[sNo + g] += Integer.parseInt(getString(row, "CNT"));
                    gradeTotalS[sNo + g] += Integer.parseInt(getString(row, "CNT_S"));
                    gradeTotal[sNo + maxG] += Integer.parseInt(getString(row, "CNT"));
                    gradeTotalS[sNo + maxG] += Integer.parseInt(getString(row, "CNT_S"));
                    gradeTotal[(maxG + 1) * 2] += Integer.parseInt(getString(row, "CNT"));
                    gradeTotalS[(maxG + 1) * 2] += Integer.parseInt(getString(row, "CNT_S"));
                }
                //小計(縦)を印字
                //svf.VrsOutn(schoolKind + "NUM_" + (maxG + 1), cLine, notPrintZero(String.valueOf(num4), "") + notPrintZero(String.valueOf(num4S), "S"));
            }
            //総計(縦)を印字
            //svf.VrsOutn("TOTAL", cLine, notPrintZero(String.valueOf(total), "") + notPrintZero(String.valueOf(totalS), "S"));
        }
        //横計を印字
        if (0 < gradeTotal[8]) {
            for (int i = 0; i < fieldName.length; i++) {
                svf.VrsOutn(fieldName[i], (maxC + 1), notPrintZero(String.valueOf(gradeTotal[i]), "") + notPrintZero(String.valueOf(gradeTotalS[i]), "S"));
            }
        }

        //2.下表
        final List kaigyoList = getKaigyoList(getList(db2, sql2(_param)));
        int line = 0;
        for (int k = 0; k < kaigyoList.size(); k++) {
            final List dataList2 = (List) kaigyoList.get(k);

            line += 1;
            svf.VrsOutn("DIV", line, getString(firstRow(dataList2), "IDOU_NAME")); // 区分
            svf.VrsOutn("HR_NAME2", line, String.valueOf(dataList2.size()) + " 名"); // 人数

            for (int j = 0; j < dataList2.size(); j++) {
                final Map row = (Map) dataList2.get(j);

                line += 1;
                svf.VrsOutn("SCHREG_NO", line, getString(row, "SCHREGNO")); // 学籍番号
                svf.VrsOutn("HR_NAME2", line, getString(row, "GRADE_NAME1") + getString(row, "HR_CLASS_NAME1")); // 学年組
                svf.VrsOutn("NAME" + (getMS932ByteLength(getString(row, "NAME")) <= 20 ? "1" : getMS932ByteLength(getString(row, "NAME")) <= 30 ? "2" : "3"), line, getString(row, "NAME")); // 氏名
                svf.VrsOutn("GRD_DATE", line, StringUtils.defaultString(getString(row, "IDOU_DATE")).replace('-', '/')); // 異動年月日
                svf.VrsOutn("GRD_REASON", line, getString(row, "IDOU_REASON")); // 異動事由
            }
            //余白(1行)を追加
            line += 1;
        }

        //上表があれば1ページ出力（改ページなし）
        if (0 < gradeTotal[8]) {
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static String notPrintZero(final String val, final String div) {
        if ("0".equals(val)) {
            return "";
        }
        return ("S".equals(div)) ? "(" + val + ")" : val;
    }

    private static Map firstRow(final List list) {
        if (null == list || list.isEmpty()) {
            return new HashMap();
        }
        return (Map) list.get(0);
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

    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static List getJHList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final String groupVal = getString(row, groupField);
            final boolean isDiffGroup = (null == oldGroupVal && null != groupVal || null != oldGroupVal && !oldGroupVal.equals(groupVal));
            if (null == current || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            if (current.size() < max) {
                current.add(row);
            }
            oldGroupVal = groupVal;
        }
        return rtn;
    }

    private static List getHrClassList(final List list, final int max) {
        final List rtn = new ArrayList();
        int cnt = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            if (cnt < max) {
                rtn.add(row);
            }
            cnt += 1;
        }
        return rtn;
    }

    private static Map getDataMap(final List list) {
        final Map m = new HashMap();
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final String key = getString(row, "GRADE") + getString(row, "HR_CLASS");
            m.put(key, row);
        }
        return m;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private static List getKaigyoList(final List list) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final String groupVal = getString(row, "ORDER_CD") + "_" + getString(row, "IDOU_DIV");
            final boolean isDiffGroup = (null == oldGroupVal && null != groupVal || null != oldGroupVal && !oldGroupVal.equals(groupVal));
            if (null == current || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(row);
            oldGroupVal = groupVal;
        }
        return rtn;
    }

    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(row);
            if (null != groupField) {
                oldGroupVal = getString(row, groupField);
            }
        }
        return rtn;
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

    private String sql3(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //学年名称リスト
        stb.append(" SELECT ");
        stb.append("     G.SCHOOL_KIND, ");
        stb.append("     G.GRADE, ");
        stb.append("     G.GRADE_CD, ");
        stb.append("     G.GRADE_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT G ");
        stb.append("     LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("         G.YEAR      = '" + param._year + "' ");
        stb.append("     AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("     AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append(" ORDER BY ");
        stb.append("     G.SCHOOL_KIND DESC, ");
        stb.append("     G.GRADE ");
        return stb.toString();
    }

    private String sql4(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //組名称リスト
        stb.append(" SELECT ");
        stb.append("     Z1.HR_CLASS, ");
        stb.append("     Z1.HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT Z1 ");
        stb.append("     INNER JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             MIN(Z1.GRADE) AS GRADE, ");
        stb.append("             Z1.HR_CLASS ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_HDAT Z1 ");
        stb.append("             INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z1.YEAR AND G.GRADE = Z1.GRADE ");
        stb.append("             LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("         WHERE ");
        stb.append("                 Z1.YEAR      = '" + param._year + "' ");
        stb.append("             AND Z1.SEMESTER  = '" + param._semester + "' ");
        stb.append("             AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("             AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append("         GROUP BY ");
        stb.append("             Z1.HR_CLASS ");
        stb.append("     ) G_MIN ON G_MIN.GRADE = Z1.GRADE AND G_MIN.HR_CLASS = Z1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("         Z1.YEAR      = '" + param._year + "' ");
        stb.append("     AND Z1.SEMESTER  = '" + param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     Z1.HR_CLASS ");
        return stb.toString();
    }

    private String sql1(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //年組
        stb.append(" WITH TBL1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         G.SCHOOL_KIND, ");
        stb.append("         Z1.GRADE, ");
        stb.append("         Z1.HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT Z1 ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z1.YEAR AND G.GRADE = Z1.GRADE ");
        stb.append("         LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("     WHERE ");
        stb.append("             Z1.YEAR      = '" + param._year + "' ");
        stb.append("         AND Z1.SEMESTER  = '" + param._semester + "' ");
        stb.append("         AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("         AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append("     GROUP BY ");
        stb.append("         G.SCHOOL_KIND, ");
        stb.append("         Z1.GRADE, ");
        stb.append("         Z1.HR_CLASS ");
        stb.append(" ) ");

        //CNT(実数)
        stb.append(" , TBL2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         COUNT(Z.SCHREGNO) AS CNT, ");
        stb.append("         G.SCHOOL_KIND, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT Z ");
        stb.append("         INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("         LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("         LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("     WHERE ");
        stb.append("             Z.YEAR       = '" + param._year + "' ");
        stb.append("         AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("         AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("         AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");

        stb.append("         AND ((M2.ENT_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");
        stb.append("         AND ((M2.GRD_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");

        stb.append("         AND NOT EXISTS ( ");
        stb.append("                 SELECT ");
        stb.append("                     'X' ");
        stb.append("                 FROM ");
        stb.append("                     SCHREG_ENT_GRD_HIST_DAT T1 ");
        stb.append("                 WHERE ");
        stb.append("                     T1.SCHREGNO = Z.SCHREGNO ");
        stb.append("                     AND T1.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("                     AND T1.GRD_DIV IN ('1','2','3','6') ");
        stb.append("                     AND T1.GRD_DATE < '" + param._kijunDate + "' ");
        stb.append("                 ) ");
        stb.append("     GROUP BY ");
        stb.append("         G.SCHOOL_KIND, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS ");
        stb.append(" ) ");

        //CNT(休学)
        stb.append(" , TBL3 AS ( ");
        stb.append("     SELECT ");
        stb.append("         COUNT(Z.SCHREGNO) AS CNT, ");
        stb.append("         G.SCHOOL_KIND, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT Z ");
        stb.append("         INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("         LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("         LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("     WHERE ");
        stb.append("             Z.YEAR       = '" + param._year + "' ");
        stb.append("         AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("         AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("         AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");

        stb.append("         AND ((M2.ENT_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");
        stb.append("         AND ((M2.GRD_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");

        stb.append("         AND EXISTS ( ");
        stb.append("                 SELECT ");
        stb.append("                     'X' ");
        stb.append("                 FROM ");
        stb.append("                     SCHREG_TRANSFER_DAT D ");
        stb.append("                 WHERE ");
        stb.append("                     D.SCHREGNO = Z.SCHREGNO ");
        stb.append("                     AND '" + param._kijunDate + "' BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE ");
        stb.append("                     AND D.TRANSFERCD IN ('1', '2') ");
        stb.append("                 ) ");
        stb.append("     GROUP BY ");
        stb.append("         G.SCHOOL_KIND, ");
        stb.append("         Z.GRADE, ");
        stb.append("         Z.HR_CLASS ");
        stb.append(" ) ");

        //メイン
        stb.append(" SELECT ");
        stb.append("     VALUE(TBL2.CNT, 0) AS CNT, ");
        stb.append("     VALUE(TBL3.CNT, 0) AS CNT_S, ");
        stb.append("     TBL1.SCHOOL_KIND, ");
        stb.append("     TBL1.GRADE, ");
        stb.append("     TBL1.HR_CLASS ");
        stb.append(" FROM ");
        stb.append("     TBL1 ");
        stb.append("     LEFT JOIN TBL2 ON TBL2.GRADE = TBL1.GRADE AND TBL2.HR_CLASS = TBL1.HR_CLASS ");
        stb.append("     LEFT JOIN TBL3 ON TBL3.GRADE = TBL1.GRADE AND TBL3.HR_CLASS = TBL1.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     TBL1.SCHOOL_KIND DESC, ");
        stb.append("     TBL1.GRADE, ");
        stb.append("     TBL1.HR_CLASS ");
        return stb.toString();
    }

    private String sql2(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //TRANSFERCD 1:留学 2:休学
        stb.append(" SELECT ");
        stb.append("     '1' AS ORDER_CD, ");
        stb.append("     D.TRANSFERCD AS IDOU_DIV, ");
        stb.append("     N1.NAME1 AS IDOU_NAME, ");
        stb.append("     D.TRANSFER_SDATE AS IDOU_DATE, ");
        stb.append("     D.TRANSFERREASON AS IDOU_REASON, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.ATTENDNO, ");
        stb.append("     G.GRADE_NAME1, ");
        stb.append("     Z1.HR_CLASS_NAME1, ");
        stb.append("     M.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("     LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT D ON D.SCHREGNO = Z.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A004' AND N1.NAMECD2 = D.TRANSFERCD ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       = '" + param._year + "' ");
        stb.append("     AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("     AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("     AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append("     AND D.TRANSFERCD IN ('1','2') ");
        stb.append("     AND '" + param._kijunDate + "'  BETWEEN D.TRANSFER_SDATE AND D.TRANSFER_EDATE ");

        stb.append("         AND ((M2.ENT_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");
        stb.append("         AND ((M2.GRD_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");

        //GRD_DIV 2:退学 3:転学
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '2' AS ORDER_CD, ");
        stb.append("     M2.GRD_DIV AS IDOU_DIV, ");
        stb.append("     N1.NAME1 AS IDOU_NAME, ");
        stb.append("     M2.GRD_DATE AS IDOU_DATE, ");
        stb.append("     CASE WHEN M2.GRD_DIV = '3' ");
        stb.append("          THEN M2.GRD_SCHOOL ");
        stb.append("          ELSE M2.GRD_REASON ");
        stb.append("     END AS IDOU_REASON, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.ATTENDNO, ");
        stb.append("     G.GRADE_NAME1, ");
        stb.append("     Z1.HR_CLASS_NAME1, ");
        stb.append("     M.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("     LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A003' AND N1.NAMECD2 = M2.GRD_DIV ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       = '" + param._year + "' ");
        stb.append("     AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("     AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("     AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append("     AND M2.GRD_DIV IN ('2','3') ");
        stb.append("     AND M2.GRD_DATE  BETWEEN '" + param._year + "-04-01' AND '" + param._kijunDate + "' ");

        stb.append("         AND ((M2.ENT_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");
        stb.append("         AND ((M2.GRD_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");

        //COMEBACK_DATEがある人 復学
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '3' AS ORDER_CD, ");
        stb.append("     '1' AS IDOU_DIV, ");
        stb.append("     '復学' AS IDOU_NAME, ");
        stb.append("     M1.COMEBACK_DATE AS IDOU_DATE, ");
        stb.append("     M2.ENT_REASON AS IDOU_REASON, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.ATTENDNO, ");
        stb.append("     G.GRADE_NAME1, ");
        stb.append("     Z1.HR_CLASS_NAME1, ");
        stb.append("     M.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("     LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_COMEBACK_DAT M1 ON M1.SCHREGNO = Z.SCHREGNO AND M1.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       = '" + param._year + "' ");
        stb.append("     AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("     AND G.SCHOOL_KIND IN ('H') ");
        stb.append("     AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append("     AND M1.COMEBACK_DATE  BETWEEN '" + param._year + "-04-01' AND '" + param._kijunDate + "' ");

        stb.append("         AND ((M2.ENT_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");
        stb.append("         AND ((M2.GRD_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");

        //編入・転入
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '4' AS ORDER_CD, ");
        stb.append("     M2.ENT_DIV AS IDOU_DIV, ");
        stb.append("     N1.NAME1 AS IDOU_NAME, ");
        stb.append("     M2.ENT_DATE AS IDOU_DATE, ");
        stb.append("     M2.ENT_REASON AS IDOU_REASON, ");
        stb.append("     Z.SCHREGNO, ");
        stb.append("     Z.GRADE, ");
        stb.append("     Z.HR_CLASS, ");
        stb.append("     Z.ATTENDNO, ");
        stb.append("     G.GRADE_NAME1, ");
        stb.append("     Z1.HR_CLASS_NAME1, ");
        stb.append("     M.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT Z ");
        stb.append("     INNER JOIN SCHREG_BASE_MST M ON M.SCHREGNO = Z.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT Z1 ON Z1.YEAR = Z.YEAR AND Z1.SEMESTER = Z.SEMESTER AND Z1.GRADE = Z.GRADE AND Z1.HR_CLASS = Z.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT G ON G.YEAR = Z.YEAR AND G.GRADE = Z.GRADE ");
        stb.append("     LEFT JOIN V_NAME_MST A023 ON A023.YEAR = G.YEAR AND A023.NAME1 = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT M2 ON M2.SCHREGNO = Z.SCHREGNO AND M2.SCHOOL_KIND = G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A002' AND N1.NAMECD2 = M2.ENT_DIV ");
        stb.append(" WHERE ");
        stb.append("         Z.YEAR       = '" + param._year + "' ");
        stb.append("     AND Z.SEMESTER   = '" + param._semester + "' ");
        stb.append("     AND G.SCHOOL_KIND IN ('J','H') ");
        stb.append("     AND G.GRADE BETWEEN A023.NAME2 AND A023.NAME3 ");
        stb.append("     AND M2.ENT_DIV IN ('4', '5')");
        stb.append("     AND M2.ENT_DATE BETWEEN '" + param._year + "-04-01' AND '" + param._kijunDate + "' ");

        stb.append("         AND ((M2.ENT_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");
        stb.append("         AND ((M2.GRD_DIV IS NULL) ");
        stb.append("               OR (M2.ENT_DIV IS NOT NULL AND M2.ENT_DATE <= '" + _param._kijunDate + "'))");

        stb.append(" ORDER BY ");
        stb.append("     ORDER_CD, ");
        stb.append("     IDOU_DIV, ");
        stb.append("     IDOU_DATE, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO ");
        return stb.toString();
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _kijunDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _kijunDate = StringUtils.defaultString(request.getParameter("DATE")).replace('/', '-');
        }
    }
}

// eof
