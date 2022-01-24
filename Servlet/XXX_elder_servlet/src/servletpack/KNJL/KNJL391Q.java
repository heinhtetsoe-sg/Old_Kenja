/*
 * $Id: c0f39a5f8cd205e942a50533cdb927e21b0adb9e $
 *
 * 作成日: 2017/03/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL391Q {

    private static final Log log = LogFactory.getLog(KNJL391Q.class);

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

            if ("1".equals(_param._check0)) {
                printMainScore(db2, svf);
            }
            if ("1".equals(_param._check1)) {
                printMainProofList(db2, svf);
            }
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

    private void printMainScore(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL391Q_1.frm";
        int maxLine = 35;
        final List dataListAll = getList(db2, sqlScore());
        final List pageList = getPageList("PLACECD", dataListAll, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            setTitle(svf);
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));
            svf.VrsOut("APPLY", String.valueOf(dataListAll.size()));

            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("PLACE", getString(row, "PLACEAREA"));

                svf.VrsOut("NO", getString(row, "NO"));
                svf.VrsOut("EXAM_NO", getString(row, "SAT_NO"));
                svf.VrsOut("KANA", getString(row, "KANA1") + "　" + getString(row, "KANA2"));
                svf.VrsOut("NAME", getString(row, "NAME1"));
                svf.VrsOut("SEX", getString(row, "SEX"));
                svf.VrsOut("BIRTHDAY", getString(row, "BIRTHDAY"));
                svf.VrsOut("ATTEND_SCHOOL_NAME", getString(row, "FINSCHOOL_NAME_ABBV"));
                svf.VrsOut("GROUP_NAME", getString(row, "GROUPNAME"));
                svf.VrsOut("SCORE1", getString(row, "SCORE_ENGLISH"));
                svf.VrsOut("SCORE2", getString(row, "SCORE_JAPANESE"));
                svf.VrsOut("SCORE3", getString(row, "SCORE_MATH"));

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private void setTitle(final Vrw32alp svf) {
        final String nendo = _param._ctrlYear + "年度 ";
        svf.VrsOut("TITLE", nendo + "　駿高実戦模試得点チェックリスト");
        final String week = KNJ_EditDate.h_format_W(_param._examdate);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._examdate) + "（" + week + "）実施");
    }

    private String sqlScore() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ROW_NUMBER() OVER(ORDER BY a1.SAT_NO) as NO, ");
        stb.append("     a1.YEAR, ");
        stb.append("     a1.PLACECD, ");
        stb.append("     a7.PLACEAREA, ");
        stb.append("     a1.SAT_NO, ");
        stb.append("     a1.NAME1, ");
        stb.append("     a1.KANA1, ");
        stb.append("     a1.KANA2, ");
        stb.append("     a2.NAME2 as SEX, ");
        stb.append("     a1.BIRTHDAY, ");
        stb.append("     a1.SCHOOLCD, ");
        stb.append("     a3.FINSCHOOL_NAME_ABBV, ");
        stb.append("     a1.GROUPCD, ");
        stb.append("     a4.GROUPNAME, ");
        stb.append("     case when a1.ABSENCE_ENGLISH = 0 then '欠' else CHAR(a1.SCORE_ENGLISH) end as SCORE_ENGLISH, ");
        stb.append("     case when a1.ABSENCE_MATH = 0 then '欠' else CHAR(a1.SCORE_MATH) end as SCORE_MATH, ");
        stb.append("     case when a1.ABSENCE_JAPANESE = 0 then '欠' else CHAR(a1.SCORE_JAPANESE) end as SCORE_JAPANESE, ");
        stb.append("     a5.CNT, ");
        stb.append("     a6.EXAM_DATE ");
        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         t1.YEAR, ");
        stb.append("         t1.SAT_NO, ");
        stb.append("         t1.NAME1, ");
        stb.append("         t1.KANA1, ");
        stb.append("         t1.KANA2, ");
        stb.append("         t1.PLACECD, ");
        stb.append("         t1.SEX, ");
        stb.append("         substr(replace(CAST(t1.BIRTHDAY AS VARCHAR(10)), '-', ''), 3, 6) as BIRTHDAY, ");
        stb.append("         t1.SCHOOLCD, ");
        stb.append("         t1.GROUPCD, ");
        stb.append("         t2.ABSENCE_ENGLISH, ");
        stb.append("         t2.ABSENCE_MATH, ");
        stb.append("         t2.ABSENCE_JAPANESE, ");
        stb.append("         t2.SCORE_ENGLISH, ");
        stb.append("         t2.SCORE_MATH, ");
        stb.append("         t2.SCORE_JAPANESE ");
        stb.append("     FROM ");
        stb.append("         SAT_APP_FORM_MST t1  ");
        stb.append("         left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("     WHERE ");
        stb.append("         t1.YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._place)) {
            stb.append("         AND t1.PLACECD = '" + _param._placeComb + "' ");
        } else if ("3".equals(_param._place) || "4".equals(_param._place)) {
            stb.append("         AND t1.INOUT_KUBUN = '" + _param._place + "' ");
        }
        stb.append("         ORDER BY ");
        stb.append("         t1.SAT_NO ");
        stb.append("     ) a1 ");
        stb.append("     left join NAME_MST a2 on a1.SEX = a2.NAMECD2 and a2.NAMECD1 = 'Z002' ");
        stb.append("     left join FINSCHOOL_MST a3 on a1.SCHOOLCD = a3.FINSCHOOLCD and a3.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join SAT_GROUP_DAT a4 on a1.GROUPCD = a4.GROUPCD and a4.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     left join (SELECT YEAR, PLACECD, COUNT(*) as CNT FROM SAT_APP_FORM_MST WHERE YEAR = '" + _param._ctrlYear + "' GROUP BY YEAR, PLACECD) a5 on a1.YEAR = a5.YEAR AND a1.PLACECD = a5.PLACECD ");
        stb.append("     left join SAT_INFO_MST a6 on a1.YEAR = a6.YEAR ");
        stb.append("     left join SAT_EXAM_PLACE_DAT a7 on a1.PLACECD = a7.PLACECD and a7.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" ORDER BY ");
        stb.append("     a1.SAT_NO ");
        return stb.toString();
    }

    private void printMainProofList(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL391Q_2.frm";
        int maxLine = 20;
        final List dataListAll = getList(db2, sqlProof());
        final List pageList = getPageList("PLACECD", dataListAll, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            setTitleProof(svf);
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));
            svf.VrsOut("APPLY", String.valueOf(dataListAll.size()));

            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("PLACE", getString(row, "PLACEAREA"));

                svf.VrsOut("EXAM_NO", getString(row, "SAT_NO"));
                svf.VrsOut("NAME", getString(row, "NAME1"));
                svf.VrsOut("SEX", getString(row, "SEX"));
                svf.VrsOut("BIRTHDAY", getString(row, "BIRTHDAY"));
                svf.VrsOut("GRD", getString(row, "GRADE"));
                svf.VrsOut("ATTEND_SCHOOL_CD", getString(row, "SCHOOLCD"));
                svf.VrsOut("DIFF_NOTE", "");
                svf.VrsOut("ATTEND_SCHOOL_NAME", getString(row, "FINSCHOOL_NAME_ABBV"));
                svf.VrsOut("ZIP_NO", getString(row, "ZIPCODE"));
                svf.VrsOut("ADDR1", getString(row, "ADDR1"));
                svf.VrsOut("ADDR2", getString(row, "ADDR2"));
                svf.VrsOut("TEL_NO1", getString(row, "TELNO1"));
                svf.VrsOut("TEL_NO2", "");
                svf.VrsOut("GROUP_NO", getString(row, "GROUPCD"));
                svf.VrsOut("GROUP_NAME", getString(row, "GROUPNAME"));
                svf.VrsOut("SCHREG_NO", getString(row, "INSIDERNO"));
                svf.VrsOut("SCORE1", getString(row, "SCORE_ENGLISH"));
                svf.VrsOut("SCORE2", getString(row, "SCORE_JAPANESE"));
                svf.VrsOut("SCORE3", getString(row, "SCORE_MATH"));
                svf.VrsOut("JUDGE", getString(row, "JUDGE"));

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private void setTitleProof(final Vrw32alp svf) {
        final String nendo = _param._ctrlYear + "年度 ";
        svf.VrsOut("TITLE", nendo + "　駿高実戦模試プルーフリスト");
        final String week = KNJ_EditDate.h_format_W(_param._examdate);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._examdate) + "（" + week + "）実施");
    }

    private String sqlProof() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     a1.PLACECD, ");
        stb.append("     a8.PLACEAREA, ");
        stb.append("     a9.CNT, ");
        stb.append("     a10.EXAM_DATE, ");
        stb.append("     a1.YEAR, ");
        stb.append("     a1.SAT_NO, ");
        stb.append("     a1.NAME1, ");
        stb.append("     a1.KANA1, ");
        stb.append("     a1.KANA2, ");
        stb.append("     a2.NAME2 as SEX, ");
        stb.append("     a1.BIRTHDAY, ");
        stb.append("     a3.NAME1 as GRADE, ");
        stb.append("     substr(a1.SCHOOLCD, 3, 5) as SCHOOLCD, ");
        stb.append("     a7.FINSCHOOL_NAME_ABBV, ");
        stb.append("     a1.ZIPCODE, ");
        stb.append("     a1.ADDR1, ");
        stb.append("     a1.ADDR2, ");
        stb.append("     a1.TELNO1, ");
        stb.append("     a1.GROUPCD, ");
        stb.append("     a4.GROUPNAME, ");
        stb.append("     a1.INSIDERNO, ");
        stb.append("     a1.SCORE_ENGLISH, ");
        stb.append("     a1.SCORE_MATH, ");
        stb.append("     a1.SCORE_JAPANESE, ");
        stb.append("     a5.NAME1 as JUDGE ");
        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         t1.YEAR, ");
        stb.append("         t1.SAT_NO, ");
        stb.append("         t1.NAME1, ");
        stb.append("         t1.KANA1, ");
        stb.append("         t1.KANA2, ");
        stb.append("         t1.SEX, ");
        stb.append("         substr(replace(CAST(t1.BIRTHDAY AS VARCHAR(10)), '-', ''), 3, 6) as BIRTHDAY, ");
        stb.append("         t1.GRADUATION, ");
        stb.append("         t1.PLACECD, ");
        stb.append("         t1.SCHOOLCD, ");
        stb.append("         t1.ZIPCODE, ");
        stb.append("         t1.ADDR1, ");
        stb.append("         t1.ADDR2, ");
        stb.append("         t1.TELNO1, ");
        stb.append("         t1.GROUPCD, ");
        stb.append("         t1.INSIDERNO, ");
        stb.append("         case when t2.ABSENCE_ENGLISH = 0 then '欠' else CHAR(t2.SCORE_ENGLISH) end as SCORE_ENGLISH, ");
        stb.append("         case when t2.ABSENCE_MATH = 0 then '欠' else CHAR(t2.SCORE_MATH) end as SCORE_MATH, ");
        stb.append("         case when t2.ABSENCE_JAPANESE = 0 then '欠' else CHAR(t2.SCORE_JAPANESE) end as SCORE_JAPANESE, ");
        stb.append("         t2.JUDGE_SAT ");
        stb.append("     FROM ");
        stb.append("         SAT_APP_FORM_MST t1 ");
        stb.append("         left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
        stb.append("     WHERE ");
        stb.append("         t1.YEAR = '" + _param._ctrlYear + "'  ");
        if ("2".equals(_param._place)) {
            stb.append("         AND t1.PLACECD = '" + _param._placeComb + "' ");
        } else if ("3".equals(_param._place) || "4".equals(_param._place)) {
            stb.append("         AND t1.INOUT_KUBUN = '" + _param._place + "' ");
        }
        stb.append("     ) a1 ");
        stb.append("     left join NAME_MST a2 on a1.SEX = a2.NAMECD2 and a2.NAMECD1 = 'Z002' ");
        stb.append("     left join NAME_MST a3 on a1.GRADUATION = a3.NAMECD2 and a3.NAMECD1 = 'L205' ");
        stb.append("     left join SAT_GROUP_DAT a4 on a1.GROUPCD = a4.GROUPCD and a1.YEAR = a4.YEAR ");
        stb.append("     left join NAME_MST a5 on a1.JUDGE_SAT = a5.NAMECD2 and a5.NAMECD1 = 'L200' ");
        stb.append("     left join SAT_EXAM_PLACE_DAT a6 on a1.PLACECD = a6.PLACECD and a1.YEAR = a6.YEAR ");
        stb.append("     left join FINSCHOOL_MST a7 on a1.SCHOOLCD = a7.FINSCHOOLCD AND a7.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join SAT_EXAM_PLACE_DAT a8 on a1.PLACECD = a8.PLACECD and a1.YEAR = a8.YEAR ");
        stb.append("     left join (SELECT YEAR, PLACECD, COUNT(*) as CNT FROM SAT_APP_FORM_MST GROUP BY YEAR, PLACECD) a9 on a1.YEAR = a9.YEAR AND a1.PLACECD = a9.PLACECD ");
        stb.append("     left join SAT_INFO_MST a10 on a1.YEAR = a10.YEAR ");
        stb.append(" ORDER BY ");
        stb.append("     a1.PLACECD, ");
        stb.append("     a1.SAT_NO ");
        return stb.toString();
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
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        int lineno = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            if (isDiffGroup) {
                lineno = 0;
            }
            lineno += 1;
            row.put("LINE_NO", String.valueOf(lineno));
            current.add(row);
            if (null != groupField) {
                oldGroupVal = getString(row, groupField);
            }
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62121 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _check0;
        private final String _check1;
        private final String _place;
        private final String _placeComb;
        private final String _examdate;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _check0 = request.getParameter("CHECK0");
            _check1 = request.getParameter("CHECK1");
            _place = request.getParameter("PLACE");
            _placeComb = request.getParameter("PLACE_COMB");
            _examdate = request.getParameter("examDate");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof

