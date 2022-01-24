/*
 * $Id: abda8abf200963732f84f9b16023eeef2c6f3551 $
 *
 * 作成日: 2017/04/07
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL338Q {

    private static final Log log = LogFactory.getLog(KNJL338Q.class);

    private boolean _hasData;

    private Param _param;

    private final String SORT_FS_CD = "1";
    private final String SUNDAI_CD_FUTUU = "3008112";
    private final String SUNDAI_CD_SPORT = "3008113";
    private final String SCHOLAR_TOKUBETU = "1";
    private final String SCHOLAR_IPPAN = "2";

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
        final int maxLine = 35;
        final List pageList = getPageList(getList(db2, sql()), maxLine);
        final String form = "KNJL338Q.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度";
        final String title = "公的試験チェックリスト";

        int applyCnt = 0;
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            applyCnt += dataList.size();
        }

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", nendo + "　" + _param._testdiv0Name1 + "　" + title);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
            svf.VrsOut("PAGE1", String.valueOf(pi + 1));
            svf.VrsOut("PAGE2", String.valueOf(pageList.size()));
            svf.VrsOut("APPLY", String.valueOf(applyCnt));

            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);

                svf.VrsOut("ATTEND_SCHOOL_NAME", getString(row, "FINSCHOOL_NAME"));
                svf.VrsOut("EXAM_NO", getString(row, "EXAMNO"));
                svf.VrsOut("NAME", getString(row, "NAME"));
                svf.VrsOut("DIV", getString(row, "TESTDIV0_ABBV1"));
                final String nameField = KNJ_EditEdit.getMS932ByteLength(getString(row, "EXAMCOURSE_NAME")) > 14 ? "2": "";
                svf.VrsOut("COURSE_NAME" + nameField, getString(row, "EXAMCOURSE_NAME"));

                svf.VrsOut("POINT1", getString(row, "PUBLIC_SCORE1"));
                svf.VrsOut("POINT2", getString(row, "PUBLIC_SCORE2"));
                svf.VrsOut("AVE_POINT", getString(row, "PUBLIC_SCORE_AVG"));

                svf.VrsOut("VALUE1", getString(row, "TOTAL3_5"));
                svf.VrsOut("VALUE2", getString(row, "TOTAL3_9"));
                svf.VrsOut("TOTAL_VALUE", getString(row, "S_TOTAL"));
                svf.VrsOut("JUGDE", getString(row, "S_JUDGE"));
                svf.VrsOut("HOPE_SCHOOL_NAME1", getString(row, "SUNDAI_SIBOU_JUNI"));
                svf.VrsOut("HOPE_SCHOOL_NAME2", getString(row, "SH_SCHOOL_NAME1"));
                svf.VrsOut("HOPE_SCHOOL_NAME3", getString(row, "SH_SCHOOL_NAME2"));
                svf.VrsOut("NOTICE1", getString(row, "ABSENCE_DAYS"));
                svf.VrsOut("NOTICE2", getString(row, "ABSENCE_DAYS2"));
                svf.VrsOut("NOTICE3", getString(row, "ABSENCE_DAYS3"));
                svf.VrsOut("SCHOLAR", getString(row, "SCHOLAR_KIBOU"));
                svf.VrsOut("DORMITORY", getString(row, "DORMITORY"));
                svf.VrsOut("PROMISE", getString(row, "KAKUYAKU"));

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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     B1.FS_CD, ");
        stb.append("     F1.FINSCHOOL_NAME, ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        stb.append("     B1.TESTDIV0, ");
        stb.append("     N1.ABBV1 AS TESTDIV0_ABBV1, ");
        stb.append("     B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE AS DAI1_COURSE, ");
        stb.append("     C1.EXAMCOURSE_NAME, ");
        stb.append("     PUB.SCORE1 AS PUBLIC_SCORE1, ");
        stb.append("     PUB.SCORE2 AS PUBLIC_SCORE2, ");
        stb.append("     CASE WHEN PUB.SCORE1 IS NOT NULL AND PUB.SCORE2 IS NOT NULL THEN SMALLINT(ROUND(FLOAT(PUB.SCORE1 + PUB.SCORE2) / 2, 0)) ");
        stb.append("          WHEN PUB.SCORE1 IS NOT NULL AND PUB.SCORE2 IS     NULL THEN PUB.SCORE1 ");
        stb.append("          WHEN PUB.SCORE1 IS     NULL AND PUB.SCORE2 IS NOT NULL THEN PUB.SCORE2 ");
        stb.append("     END AS PUBLIC_SCORE_AVG, ");
//        stb.append("     SMALLINT(PUB.AVG) AS PUBLIC_SCORE_AVG, ");
        stb.append("     AC1.CONFIDENTIAL_RPT01 + AC1.CONFIDENTIAL_RPT02 + AC1.CONFIDENTIAL_RPT03 + AC1.CONFIDENTIAL_RPT04 + AC1.CONFIDENTIAL_RPT09 AS TOTAL3_5, ");
        stb.append("     AC1.CONFIDENTIAL_RPT01 + AC1.CONFIDENTIAL_RPT02 + AC1.CONFIDENTIAL_RPT03 + AC1.CONFIDENTIAL_RPT04 + AC1.CONFIDENTIAL_RPT05 ");
        stb.append("     + AC1.CONFIDENTIAL_RPT06 + AC1.CONFIDENTIAL_RPT07 + AC1.CONFIDENTIAL_RPT08 + AC1.CONFIDENTIAL_RPT09 AS TOTAL3_9, ");
        stb.append("     SAT_E.SCORE_TOTAL AS S_TOTAL, ");
        stb.append("     N2.NAME1 AS S_JUDGE, ");
        //3008112：駿台甲府・普通
        //3008113：駿台甲府・普通（ス）
        stb.append("     CASE WHEN B1.SH_SCHOOLCD1 = '" + SUNDAI_CD_FUTUU + "' OR B1.SH_SCHOOLCD1 = '" + SUNDAI_CD_SPORT + "' THEN '第１志望' ");
        stb.append("          WHEN B1.SH_SCHOOLCD2 = '" + SUNDAI_CD_FUTUU + "' OR B1.SH_SCHOOLCD2 = '" + SUNDAI_CD_SPORT + "' THEN '第２志望' ");
        stb.append("          WHEN B1.SH_SCHOOLCD3 = '" + SUNDAI_CD_FUTUU + "' OR B1.SH_SCHOOLCD3 = '" + SUNDAI_CD_SPORT + "' THEN '第３志望' ");
        stb.append("          WHEN B1.SH_SCHOOLCD4 = '" + SUNDAI_CD_FUTUU + "' OR B1.SH_SCHOOLCD4 = '" + SUNDAI_CD_SPORT + "' THEN '第４志望' ");
        stb.append("     END AS SUNDAI_SIBOU_JUNI, ");
        stb.append("     B1.SH_SCHOOLCD1, ");
        stb.append("     B1.SH_SCHOOLCD2, ");
        stb.append("     SH1.FINSCHOOL_NAME AS SH_SCHOOL_NAME1, ");
        stb.append("     SH2.FINSCHOOL_NAME AS SH_SCHOOL_NAME2, ");
        stb.append("     AC1.ABSENCE_DAYS, ");
        stb.append("     AC1.ABSENCE_DAYS2, ");
        stb.append("     AC1.ABSENCE_DAYS3, ");
        stb.append("     CASE WHEN B1.SCHOLAR_KIBOU = '" + SCHOLAR_TOKUBETU + "' THEN '特別' ");
        stb.append("          WHEN B1.SCHOLAR_KIBOU = '" + SCHOLAR_IPPAN + "' THEN '一般' ");
        stb.append("          ELSE '無' ");
        stb.append("     END AS SCHOLAR_KIBOU, ");
        stb.append("     CASE WHEN B1.DORMITORY_FLG = '1' THEN '希望' END AS DORMITORY, ");
        stb.append("     CASE WHEN PUB.KAKUYAKU_FLG = '1' THEN '確約' END AS KAKUYAKU ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN ENTEXAM_PUBLIC_TEST_DAT PUB ON PUB.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND PUB.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND PUB.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN SAT_EXAM_DAT SAT_E ON SAT_E.YEAR = '" + _param._loginYear + "' ");
        stb.append("             AND SAT_E.SAT_NO = B1.JIZEN_BANGOU ");
        stb.append("     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = SAT_E.YEAR AND N2.NAMECD1 = 'L200' AND N2.NAMECD2 = SAT_E.JUDGE_SAT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT AC1 ON AC1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND AC1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND AC1.EXAMNO = B1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("             AND C1.APPLICANTDIV = B1.APPLICANTDIV ");
        stb.append("             AND C1.TESTDIV = B1.TESTDIV ");
        stb.append("             AND C1.COURSECD = B1.DAI1_COURSECD ");
        stb.append("             AND C1.MAJORCD = B1.DAI1_MAJORCD ");
        stb.append("             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH1 ON SH1.FINSCHOOLCD = B1.SH_SCHOOLCD1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH2 ON SH2.FINSCHOOLCD = B1.SH_SCHOOLCD2 ");
        stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L045' AND N1.NAMECD2 = B1.TESTDIV0 ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND B1.TESTDIV0 = '" + _param._testDiv0 + "' ");
        stb.append(" ORDER BY ");
        if (SORT_FS_CD.equals(_param._taisyou)) {
            stb.append("     B1.FS_CD, ");
        }
        stb.append("     B1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57699 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv0;
        private final String _taisyou;
        private final String _loginDate;
        private final String _loginYear;

        final String _applicantdivName;
        final String _testdiv0Name1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv0 = request.getParameter("TESTDIV");//NAME_MST L045
            _taisyou = request.getParameter("TAISYOU");//1:中学校順、2:受験番号順
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");

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

