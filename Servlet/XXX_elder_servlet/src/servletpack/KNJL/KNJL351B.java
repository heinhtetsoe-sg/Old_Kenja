/*
 * $Id: b65862702b0325883fac3ba18cafa30d585b9697 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５１Ｂ＞  科目別成績統計表
 **/
public class KNJL351B {

    private static final Log log = LogFactory.getLog(KNJL351B.class);
    
    private static final String SUBCLASSCD_TOTAL = "999999";

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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int[] subclassCount;
        if ("1".equals(_param._testdiv)) {
            subclassCount = new int[] {3};
        } else { // if ("2".equals(_param._testdiv)) {
            subclassCount = new int[] {3, 5};
        }
        for (int i = 0; i < subclassCount.length; i++) {
            final List statList = Stat.load(db2, _param, subclassCount[i]);
            if (statList.size() == 0) {
                continue;
            }
            
            final String form = "KNJL351B.frm";
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            svf.VrsOut("APPLICANTDIV", _param._testdivAbbv1); // 入試制度
            // svf.VrsOut("TESTDIV", null); // 入試所属
            
            int line = 0;
            for (int j = 0; j < statList.size(); j++) {
                final Stat stat = (Stat) statList.get(j);
                if (SUBCLASSCD_TOTAL.equals(stat._testsubclasscd)) {
                    line = 20;
                } else {
                    line += 1;
                    svf.VrsOutn("NUMBER", line, String.valueOf(line)); // 番号
                }
                svf.VrsOutn("SUBCLASSNAME", line, stat._testsubclassname); // 科目名
                svf.VrsOutn("APPLICANT_CNT", line, stat._countShigansha); // 志願者数
                svf.VrsOutn("ABSENT_CNT", line, stat._countKesseki); // 欠席者数
                svf.VrsOutn("EXAMINEE_CNT", line, stat._countJuken); // 受験者数
                svf.VrsOutn("PERFECT_SCORE", line, stat._perfect); // 配点
                svf.VrsOutn("MAX_SCORE", line, stat._max); // 最高点
                svf.VrsOutn("MIN_SCORE", line, stat._min); // 最低点
                svf.VrsOutn("AVERAGE_SCORE", line, sishagonyu(stat._avg)); // 平均点
                svf.VrsOutn("STANDARD_DEVIATION", line, sishagonyu(stat._stddev)); // 標準偏差
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static String sishagonyu(final String s) {
        if (null == s) {
            return s;
        }
        return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }


    private static class Stat {
        final String _testsubclasscd;
        final String _testsubclassname;
        final String _perfect;
        final String _countShigansha;
        final String _countKesseki;
        final String _countJuken;
        final String _max;
        final String _min;
        final String _avg;
        final String _stddev;

        Stat(
            final String testsubclasscd,
            final String testsubclassname,
            final String perfect,
            final String countShigansha,
            final String countKesseki,
            final String countJuken,
            final String max,
            final String min,
            final String avg,
            final String stddev
        ) {
            _testsubclasscd = testsubclasscd;
            _testsubclassname = testsubclassname;
            _perfect = perfect;
            _countShigansha = countShigansha;
            _countKesseki = countKesseki;
            _countJuken = countJuken;
            _max = max;
            _min = min;
            _avg = avg;
            _stddev = stddev;
        }
        
        public static List load(final DB2UDB db2, final Param param, final int subclasscount) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sql(param, subclasscount);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String testsubclassname = rs.getString("TESTSUBCLASSNAME");
                    final String perfect = rs.getString("PERFECT");
                    final String countShigansha = rs.getString("COUNT_SHIGANSHA");
                    final String countKesseki = rs.getString("COUNT_KESSEKI");
                    final String countJuken = rs.getString("COUNT_JUKEN");
                    final String max = rs.getString("MAX");
                    final String min = rs.getString("MIN");
                    final String avg = rs.getString("AVG");
                    final String stddev = rs.getString("STDDEV");
                    final Stat stat = new Stat(testsubclasscd, testsubclassname, perfect, countShigansha, countKesseki, countJuken, max, min, avg, stddev);
                    list.add(stat);
                }
            } catch (Exception ex) {
                log.fatal("exception! sql = " + sql, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param, final int subclasscount) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH TESTSUBCLASSCDS AS ( ");
            stb.append("     SELECT ");
            stb.append("         'SUBCLASSCD' AS KEY, ");
            stb.append("         T1.NAMECD2 AS TESTSUBCLASSCD, ");
            stb.append("         T1.NAME1 AS TESTSUBCLASSNAME, ");
            stb.append("         L1.PERFECT ");
            stb.append("     FROM ");
            stb.append("         V_NAME_MST T1 ");
            stb.append("         LEFT JOIN ENTEXAM_PERFECT_MST L1 ON L1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("             AND L1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("             AND L1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("             AND L1.COURSECD = '0' ");
            stb.append("             AND L1.MAJORCD = '000' ");
            stb.append("             AND L1.EXAMCOURSECD = '0000' ");
            stb.append("             AND L1.TESTSUBCLASSCD = T1.NAMECD2 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._entexamyear + "' ");
            stb.append("         AND T1.NAMECD1 = 'L009' ");
            if (subclasscount == 3) {
                stb.append("         AND T1.NAMESPARE2 = '1' ");
            } else if (subclasscount == 5) {
                stb.append("         AND T1.NAMESPARE3 = '1' ");
            }
            stb.append(" ), RECEPT AS ( ");
            stb.append("     SELECT ");
            stb.append("         RECEPT.ENTEXAMYEAR, ");
            stb.append("         RECEPT.APPLICANTDIV, ");
            stb.append("         RECEPT.TESTDIV, ");
            stb.append("         RECEPT.EXAM_TYPE, ");
            stb.append("         RECEPT.RECEPTNO, ");
            stb.append("         RECEPT.ATTEND_ALL_FLG, ");
            if (subclasscount == 3) {
                stb.append("         RECEPT.TOTAL3 AS TOTAL, ");
                stb.append("         RECEPT.AVARAGE3 AS AVARAGE ");
            } else if (subclasscount == 5) {
                stb.append("         RECEPT.TOTAL1 AS TOTAL, ");
                stb.append("         RECEPT.AVARAGE1 AS AVARAGE ");
            }
            stb.append("     FROM ");
            stb.append("         ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append("     WHERE ");
            stb.append("         RECEPT.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("         AND RECEPT.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND RECEPT.TESTDIV = '" + param._testdiv + "' ");
            stb.append("         AND RECEPT.EXAM_TYPE = '1' ");
            stb.append(" ), SCORES AS ( ");
            stb.append("     SELECT ");
            stb.append("         RECEPT.RECEPTNO, ");
            stb.append("         RECEPT.ATTEND_ALL_FLG, ");
            stb.append("         CDS.TESTSUBCLASSCD, ");
            stb.append("         CDS.TESTSUBCLASSNAME, ");
            stb.append("         CDS.PERFECT, ");
            stb.append("         TSCORE.RECEPTNO AS SCORE_RECEPTNO, ");
            stb.append("         TSCORE.SCORE, ");
            stb.append("         TSCORE.ATTEND_FLG ");
            stb.append("     FROM ");
            stb.append("         RECEPT ");
            stb.append("     LEFT JOIN TESTSUBCLASSCDS CDS ON CDS.KEY = 'SUBCLASSCD' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("         AND TSCORE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("         AND TSCORE.TESTDIV = RECEPT.TESTDIV ");
            stb.append("         AND TSCORE.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("         AND TSCORE.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("         AND TSCORE.TESTSUBCLASSCD = CDS.TESTSUBCLASSCD ");
            stb.append(" ), ATTEND_ALL_SUBCLASS_STAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         TESTSUBCLASSCD, ");
            stb.append("         MAX(SCORE) AS MAX, ");
            stb.append("         MIN(SCORE) AS MIN, ");
            stb.append("         AVG(FLOAT(SCORE)) AS AVG, ");
            stb.append("         STDDEV(FLOAT(SCORE)) AS STDDEV ");
            stb.append("     FROM ");
            stb.append("         SCORES ");
            stb.append("     WHERE ");
            stb.append("         SCORE IS NOT NULL ");
            stb.append("     GROUP BY ");
            stb.append("         TESTSUBCLASSCD ");
            stb.append(" ), ATTEND_ALL_RECEPT_STAT AS ( ");
            stb.append("     SELECT ");
            stb.append("         '" + SUBCLASSCD_TOTAL + "' AS TESTSUBCLASSCD, ");
            stb.append("         MAX(TOTAL) AS MAX, ");
            stb.append("         MIN(TOTAL) AS MIN, ");
            stb.append("         AVG(FLOAT(TOTAL)) AS AVG, ");
            stb.append("         STDDEV(FLOAT(TOTAL)) AS STDDEV ");
            stb.append("     FROM ");
            stb.append("         RECEPT ");
            stb.append("     WHERE ");
            stb.append("         AVARAGE IS NOT NULL ");
            stb.append(" ), SUBCLASS_STAT AS ( ");
            stb.append("     SELECT  ");
            stb.append("         T1.TESTSUBCLASSCD, ");
            stb.append("         T1.TESTSUBCLASSNAME, ");
            stb.append("         T1.PERFECT, ");
            stb.append("         SUM(CASE WHEN T1.SCORE_RECEPTNO IS NOT NULL THEN 1 ELSE 0 END) AS COUNT_SHIGANSHA, ");
            stb.append("         SUM(CASE WHEN T1.SCORE_RECEPTNO IS NOT NULL AND SCORE IS NULL THEN 1 ELSE 0 END) AS COUNT_KESSEKI, ");
            stb.append("         SUM(CASE WHEN T1.SCORE_RECEPTNO IS NOT NULL AND SCORE IS NOT NULL THEN 1 ELSE 0 END) AS COUNT_JUKEN ");
            stb.append("     FROM ");
            stb.append("         SCORES T1 ");
            stb.append("     GROUP BY ");
            stb.append("         T1.TESTSUBCLASSCD, ");
            stb.append("         T1.TESTSUBCLASSNAME, ");
            stb.append("         T1.PERFECT ");
            stb.append(" ), RECEPT_STAT AS ( ");
            stb.append("     SELECT  ");
            stb.append("         '" + SUBCLASSCD_TOTAL + "' AS TESTSUBCLASSCD, ");
            stb.append("         '総合' AS TESTSUBCLASSNAME, ");
            stb.append("         COUNT(*) AS COUNT_SHIGANSHA, ");
            stb.append("         SUM(CASE WHEN VALUE(T1.ATTEND_ALL_FLG, '') <> '1' THEN 1 ELSE 0 END) AS COUNT_KESSEKI  ");
            stb.append("     FROM RECEPT T1 ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     T1.TESTSUBCLASSNAME, ");
            stb.append("     T1.PERFECT, ");
            stb.append("     T1.COUNT_SHIGANSHA, ");
            stb.append("     T1.COUNT_KESSEKI,  ");
            stb.append("     T1.COUNT_JUKEN,  ");
            stb.append("     L1.MAX, ");
            stb.append("     L1.MIN, ");
            stb.append("     L1.AVG, ");
            stb.append("     L1.STDDEV ");
            stb.append(" FROM SUBCLASS_STAT T1 ");
            stb.append(" LEFT JOIN ATTEND_ALL_SUBCLASS_STAT L1 ON L1.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     T1.TESTSUBCLASSNAME, ");
            stb.append("     CAST(NULL AS SMALLINT) AS PERFECT, ");
            stb.append("     T1.COUNT_SHIGANSHA, ");
            stb.append("     T1.COUNT_KESSEKI,  ");
            stb.append("     T1.COUNT_SHIGANSHA - T1.COUNT_KESSEKI AS COUNT_JUKEN,  ");
            stb.append("     L1.MAX, ");
            stb.append("     L1.MIN, ");
            stb.append("     L1.AVG, ");
            stb.append("     L1.STDDEV ");
            stb.append(" FROM RECEPT_STAT T1 ");
            stb.append(" LEFT JOIN ATTEND_ALL_RECEPT_STAT L1 ON L1.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("     TESTSUBCLASSCD ");
            return stb.toString();
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
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _testdivAbbv1;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            
        }
        
        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
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

