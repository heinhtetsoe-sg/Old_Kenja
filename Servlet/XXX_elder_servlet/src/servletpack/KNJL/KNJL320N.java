/*
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２０Ｎ＞  入学試験平均点印刷
 **/
public class KNJL320N {

    private static final Log log = LogFactory.getLog(KNJL320N.class);

    private boolean _hasData;

    private Param _param;

    private String TESTSUBCLASSCD_ALL = "A";

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
            
            if (null != _param) {
                DbUtils.closeQuietly(_param._psAvg);
            }

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
    
    private static String sishagonyu(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return s;
        }
        return new BigDecimal(s).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL320N.frm";
        svf.VrSetForm(form, 1);
        
        final String[] maxMins = {"最高点", "最低点"};
        final int maxCourseLine = 5 + maxMins.length;
        final String[] years = {_param._entexamyear, String.valueOf(Integer.parseInt(_param._entexamyear) - 1)};

        // 左（当年度2021）の場合、右（前年度2020）の入試区分は固定で表示
        String testdivKotei = _param._testdiv;
        String testdivAbbv1Kotei = _param._testdivAbbv1;
        if ("2021".equals(_param._entexamyear)) {
            if ("1".equals(_param._testdiv)) {
                testdivKotei = "1";
                testdivAbbv1Kotei = "1次入試";
            } else if ("2".equals(_param._testdiv)) {
                testdivKotei = "1";
                testdivAbbv1Kotei = "1次入試";
            } else if ("3".equals(_param._testdiv)) {
                testdivKotei = "2";
                testdivAbbv1Kotei = "1.5次入試";
            } else if ("4".equals(_param._testdiv)) {
                testdivKotei = "3";
                testdivAbbv1Kotei = "2次入試";
            }
        }
        final String[] testdivs = {_param._testdiv, testdivKotei};
        final String[] testdivAbbv1s = {_param._testdivAbbv1, testdivAbbv1Kotei};
        
        // 入試年度
        for (int yi = 0; yi < years.length; yi++) {
            final String year = years[yi];
            final String yx = String.valueOf(yi + 1);
            final String testdiv = testdivs[yi];
            final String testdivAbbv1 = testdivAbbv1s[yi];
            
            svf.VrsOut("TITLE" + yx, KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度　入学試験平均点（" + StringUtils.defaultString(testdivAbbv1) +"）");

            
            final List testSubclassNameMapList = _param.getTestSubclassList(db2, year);

            // 科目名
            for (int subi = 0; subi < testSubclassNameMapList.size(); subi++) {
                final Map subclassMap = (Map) testSubclassNameMapList.get(subi);
                final String testsubclassname = (String) subclassMap.get("NAME1");
                svf.VrsOut("SUBJECT" + yx + "_" + String.valueOf(subi + 1), testsubclassname);
            }
            
            final List examCourseMapList = _param.getExamcourseMapList(db2, year, testdiv);
            
            final List shdivList = _param.getShdivList(db2, year);

            final Map maxMinMap = _param.getMaxMin(db2, year, testdiv);

            // 単願/併願
            for (int shdivi = 0; shdivi < shdivList.size(); shdivi++) {
                final Map shdivMap = (Map) shdivList.get(shdivi);
                final String shdiv = (String) shdivMap.get("NAMECD2");
                final String shdivname = (String) shdivMap.get("NAME1");

                svf.VrsOut("DIV" + yx + "_" + String.valueOf(shdivi + 1), shdivname);
            
                // コース
                for (int crsi = 0; crsi < examCourseMapList.size(); crsi++) {
                    final int j = shdivi * maxCourseLine + crsi + 1;
                    final Map courseMap = (Map) examCourseMapList.get(crsi);
                    final String coursecd = (String) courseMap.get("COURSECD");
                    final String majorcd = (String) courseMap.get("MAJORCD");
                    final String examcoursecd = (String) courseMap.get("EXAMCOURSECD");
                    final String examcourseAbbv = (String) courseMap.get("EXAMCOURSE_ABBV");
                    
                    if (KNJ_EditEdit.getMS932ByteLength(examcourseAbbv) > 10) {
                        final String[] token = KNJ_EditEdit.get_token(examcourseAbbv, 10, 2);
                        if (null != token) {
                            for (int i = 0; i < token.length; i++) {
                                svf.VrsOutn("COURSE_NAME" + yx + "_2_" + String.valueOf(i + 1), j, token[i]);
                            }
                        }
                    } else {
                        svf.VrsOutn("COURSE_NAME" + yx + "_1", j, examcourseAbbv);
                    }

                    // 科目ごと平均点
                    for (int subi = 0; subi < testSubclassNameMapList.size(); subi++) {
                        final Map subclassMap = (Map) testSubclassNameMapList.get(subi);
                        final String testsubclasscd = (String) subclassMap.get("NAMECD2");
                        
                        final String average = _param.getAverage(db2, year, testdiv, shdiv, coursecd, majorcd, examcoursecd, testsubclasscd);
                        svf.VrsOutn("SCORE" + yx + "_" + String.valueOf(subi + 1), j, average);
                    }
                    
                    // 合計の平均点
                    svf.VrsOutn("TOTAL" + yx, j, _param.getAverage(db2, year, testdiv, shdiv, coursecd, majorcd, examcoursecd, TESTSUBCLASSCD_ALL));
                }

                // 科目ごと最高点・最低点
                for (int mi = 0; mi < maxMins.length; mi++) {
                    final int k = shdivi * maxCourseLine + examCourseMapList.size() + mi + 1;
                    final String maxMinName = maxMins[mi];
                    svf.VrsOutn("COURSE_NAME" + yx + "_1", k, maxMinName);
                    for (int subi = 0; subi < testSubclassNameMapList.size(); subi++) {
                        final Map subclassMap = (Map) testSubclassNameMapList.get(subi);
                        final String testsubclasscd = (String) subclassMap.get("NAMECD2");
                        if (mi == 0) {
                        	svf.VrsOutn("SCORE" + yx + "_" + String.valueOf(subi + 1), k, (String) maxMinMap.get(shdiv + testsubclasscd + "MAX_SCORE"));
                        } else {
                        	svf.VrsOutn("SCORE" + yx + "_" + String.valueOf(subi + 1), k, (String) maxMinMap.get(shdiv + testsubclasscd + "MIN_SCORE"));
                        }
                    }
                }
            }
        }
        
        svf.VrEndPage();
        _hasData = true;
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64266 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _applicantdivAbbv1;
        final String _testdivAbbv1;
        final String _dateStr;

        PreparedStatement _psAvg;
        PreparedStatement _psMaxMin;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(db2, _date);
            
            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
        }
        
        public String getAverage(final DB2UDB db2, final String year, final String testdiv, final String shdiv, final String coursecd, final String majorcd, final String examcoursecd, final String testsubclasscd) {
            String rtn = null;
            ResultSet rs = null;
            try {
                if (null == _psAvg) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("     AVERAGE_MEN, ");
                    stb.append("     AVERAGE_WOMEN, ");
                    stb.append("     AVERAGE_TOTAL ");
                    stb.append(" FROM ");
                    stb.append("   ENTEXAM_APPLICANT_AVERAGE_DAT AVGD ");
                    stb.append(" WHERE ");
                    stb.append("     ENTEXAMYEAR = ? ");
                    stb.append("     AND APPLICANTDIV = '" + _applicantdiv + "' ");
                    stb.append("     AND TESTDIV = ? ");
                    stb.append("     AND EXAM_TYPE = '1' ");
                    stb.append("     AND SHDIV = ?  ");
                    stb.append("     AND COURSECD = ? ");
                    stb.append("     AND MAJORCD = ?  ");
                    stb.append("     AND EXAMCOURSECD = ? ");
                    stb.append("     AND TESTSUBCLASSCD = ? ");
                    _psAvg = db2.prepareStatement(stb.toString());
                }
                int pi = 0;
                //log.debug(" avg param = " + ArrayUtils.toString(new String[] {year, shdiv, coursecd, majorcd, examcoursecd, testsubclasscd}));
                _psAvg.setString(++pi, year);
                _psAvg.setString(++pi, testdiv);
                _psAvg.setString(++pi, shdiv);
                _psAvg.setString(++pi, coursecd);
                _psAvg.setString(++pi, majorcd);
                _psAvg.setString(++pi, examcoursecd);
                _psAvg.setString(++pi, testsubclasscd);
                rs = _psAvg.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("AVERAGE_TOTAL");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        public Map getMaxMin(final DB2UDB db2, final String year, final String testdiv) {
            final Map rtnMap = new HashMap();
            ResultSet rs = null;
            try {
                if (null == _psMaxMin) {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("     I1.SHDIV, ");
                    stb.append("     I3.TESTSUBCLASSCD, ");
                    stb.append("     MAX(I3.SCORE) AS MAX_SCORE, ");
                    stb.append("     MIN(I3.SCORE) AS MIN_SCORE ");
                    stb.append(" FROM ");
                    stb.append("     ENTEXAM_RECEPT_DAT T1 ");
                    stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT I1 ON T1.ENTEXAMYEAR = I1.ENTEXAMYEAR ");
                    stb.append("           AND T1.APPLICANTDIV = I1.APPLICANTDIV ");
                    stb.append("           AND T1.EXAMNO = I1.EXAMNO ");
                    stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT I2 ON T1.ENTEXAMYEAR = I2.ENTEXAMYEAR ");
                    stb.append("           AND T1.EXAMNO = I2.EXAMNO ");
                    stb.append("           AND I2.SEQ = '001' ");
                    stb.append("           AND I2.REMARK8 || ':' || I2.REMARK9 || ':' || I2.REMARK10 IS NOT NULL ");
                    stb.append("     INNER JOIN ENTEXAM_SCORE_DAT I3 ON T1.ENTEXAMYEAR = I3.ENTEXAMYEAR ");
                    stb.append("           AND T1.APPLICANTDIV = I3.APPLICANTDIV ");
                    stb.append("           AND T1.TESTDIV = I3.TESTDIV ");
                    stb.append("           AND T1.EXAM_TYPE = I3.EXAM_TYPE ");
                    stb.append("           AND T1.RECEPTNO = I3.RECEPTNO ");
                    stb.append("           AND I3.ATTEND_FLG = '1' ");
                    stb.append(" WHERE ");
                    stb.append("     T1.ENTEXAMYEAR = ? ");
                    stb.append("     AND T1.APPLICANTDIV = '" + _applicantdiv + "' ");
                    stb.append("     AND T1.TESTDIV = ? ");
                    stb.append("     AND T1.EXAM_TYPE = '1' ");
                    stb.append("     AND (T1.JUDGEDIV IS NULL OR T1.JUDGEDIV IN('1', '2', '3')) ");
                    stb.append(" GROUP BY ");
                    stb.append("     I1.SHDIV, ");
                    stb.append("     I3.TESTSUBCLASSCD ");
                    _psMaxMin = db2.prepareStatement(stb.toString());
                }
                int pi = 0;
                _psMaxMin.setString(++pi, year);
                _psMaxMin.setString(++pi, testdiv);
                rs = _psMaxMin.executeQuery();
                while (rs.next()) {
                    rtnMap.put(rs.getString("SHDIV") + rs.getString("TESTSUBCLASSCD") + "MAX_SCORE", rs.getString("MAX_SCORE"));
                    rtnMap.put(rs.getString("SHDIV") + rs.getString("TESTSUBCLASSCD") + "MIN_SCORE", rs.getString("MIN_SCORE"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
            }
            return rtnMap;
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(db2, date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private List getExamcourseMapList(final DB2UDB db2, final String year, final String testdiv) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     CRS.COURSECD, ");
                stb.append("     CRS.MAJORCD, ");
                stb.append("     CRS.EXAMCOURSECD, ");
                stb.append("     CRS.EXAMCOURSE_ABBV ");
                stb.append(" FROM ");
                stb.append("   ENTEXAM_COURSE_MST CRS ");
                stb.append(" WHERE ");
                stb.append("     CRS.ENTEXAMYEAR = '" + year + "' ");
                stb.append("     AND CRS.APPLICANTDIV = '" + _applicantdiv + "' ");
                stb.append("     AND CRS.TESTDIV = '" + testdiv + "' ");
                stb.append(" ORDER BY ");
                stb.append("     CRS.COURSECD, ");
                stb.append("     CRS.MAJORCD, ");
                stb.append("     CRS.EXAMCOURSECD ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("COURSECD", rs.getString("COURSECD"));
                    m.put("MAJORCD", rs.getString("MAJORCD"));
                    m.put("EXAMCOURSECD", rs.getString("EXAMCOURSECD"));
                    m.put("EXAMCOURSE_ABBV", rs.getString("EXAMCOURSE_ABBV"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private List getShdivList(final DB2UDB db2, final String year) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'L006' ";
                sql += " ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("NAMECD2", rs.getString("NAMECD2"));
                    m.put("NAME1", rs.getString("NAME1"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private List getTestSubclassList(final DB2UDB db2, final String year) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'L009' ";
                sql += " ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("NAMECD2", rs.getString("NAMECD2"));
                    m.put("NAME1", rs.getString("NAME1"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
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
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof

