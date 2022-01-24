/*
 * $Id$
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２４Ｎ＞  出願者数・受験者数・合格者数一覧
 **/
public class KNJL324N {

    private static final Log log = LogFactory.getLog(KNJL324N.class);

    private static final String FROM_TO_MARK = "\uFF5E";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String COURSE1_GURO = "0001"; // グロリア探究
        final String COURSE2_BURA = "0002"; // ブライト学修
        final String COURSE3_MIKO = "0003"; // ミコリエ保育
        final String COURSE4_ADA = "0004"; // アダプト進学
        final String COURSE5_SPO = "0005"; // スポーツ

        final List examCourseList = Arrays.asList(
                COURSE1_GURO,
                COURSE2_BURA,
                COURSE3_MIKO,
                COURSE4_ADA,
                COURSE5_SPO
        );

        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear));

        final int TOTAL_IDX = examCourseList.size();// "合計"のインデクス

        final String JUDGEMENT_GOUKAKU = "('1')";
        final String JUDGEMENT_MAWASHI = "('3')";
        final String JUDGEMENT_GOUKAKU_OR_MAWASHI = "('1', '3')";
        final String JUDGEMENT_KESSEKI = "('4')";
        final String JUDGEMENT_JITAI = "('5')";
        final String JUDGEMENT_FUGOUKAKU = "('2')";

        // 単願/併願/推薦
        final List pageList = getPageList(_param._shdivList, 2);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List shdivList = (List) pageList.get(pi);

            final String form = pi == 0 ? "KNJL324N.frm" : "KNJL324N_2.frm";

            svf.VrSetForm(form, 1);
            svf.VrsOut("TITLE", gengou + "年度　出願者数・受験者数・合格者数一覧表"); // タイトル
            svf.VrsOut("SUBTITLE", "（" + StringUtils.defaultString(_param._testdivAbbv1) +"）"); // タイトル
            svf.VrsOut("PRINT_DATE", _param._dateStr); // 印刷日時

            for (int shdivi = 0; shdivi < shdivList.size(); shdivi++) {
                final Map shdivMap = (Map) shdivList.get(shdivi);
                final String shdiv = (String) shdivMap.get("NAMECD2");
                //log.debug(" shdiv = " + shdiv);
                //final String shdivname = (String) shdivMap.get("NAME1");

                final int[][] count = new int[examCourseList.size() + 1][25];

                // コース
                for (int crsi = 0; crsi < examCourseList.size(); crsi++) {
                    final String examcoursecd = (String) examCourseList.get(crsi);
                    //log.debug(" examcoursecd = " + examcoursecd);
                    boolean judgement45igai;
                    final boolean courseMatch = false;
                    String bd9Remark9 = null;
                    judgement45igai = false;
                    // 出願者数
                    count[crsi][0] = getCount(db2, shdiv, examcoursecd, null, null, bd9Remark9, judgement45igai, courseMatch);
                    // 辞退
                    count[crsi][1] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_JITAI, bd9Remark9, judgement45igai, courseMatch);
                    // 欠席
                    count[crsi][2] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_KESSEKI, bd9Remark9, judgement45igai, courseMatch);
                    // 受験者数
                    count[crsi][3] = count[crsi][0] - count[crsi][1] - count[crsi][2];

                    judgement45igai = true;
                    // 事前○の受験生
                    bd9Remark9 = "1";
                    count[crsi][4] = getCount(db2, shdiv, examcoursecd, null, null, bd9Remark9, judgement45igai, courseMatch);
                    // 志望コース合格
                    count[crsi][5] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_GOUKAKU, bd9Remark9, judgement45igai, courseMatch);

                    // 事前△の受験生
                    bd9Remark9 = "3";
                    count[crsi][6] = getCount(db2, shdiv, examcoursecd, null, null, bd9Remark9, judgement45igai, courseMatch);
                    // 志望コース合格
                    count[crsi][7] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_GOUKAKU, bd9Remark9, judgement45igai, courseMatch);
                    // 志望コース不合格
                    count[crsi][8] = getCount(db2, shdiv, examcoursecd, null, null, bd9Remark9, judgement45igai, true);
                    // グロリア探究 -> ブライト学修
                    if (COURSE1_GURO.equals(examcoursecd)) {
                        count[crsi][9] = getCount(db2, shdiv, examcoursecd, COURSE2_BURA, JUDGEMENT_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    }
                    // ブライト学修 -> ミコリエ保育
                    if (COURSE2_BURA.equals(examcoursecd)) {
                        count[crsi][10] = getCount(db2, shdiv, examcoursecd, COURSE3_MIKO, JUDGEMENT_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    }
                    // ブライト学修 -> アダブト進学
                    if (COURSE2_BURA.equals(examcoursecd)) {
                        count[crsi][11] = getCount(db2, shdiv, examcoursecd, COURSE4_ADA, JUDGEMENT_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    }
                    // ミコリエ保育 -> アダブト進学
                    if (COURSE3_MIKO.equals(examcoursecd)) {
                        count[crsi][12] = getCount(db2, shdiv, examcoursecd, COURSE4_ADA, JUDGEMENT_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    }
                    // 不合格者
                    count[crsi][13] = getCount(db2, shdiv, null, null, JUDGEMENT_FUGOUKAKU, bd9Remark9, judgement45igai, courseMatch);

                    // 事前▲の受験生
                    bd9Remark9 = "4";
                    count[crsi][14] = getCount(db2, shdiv, examcoursecd, null, null, bd9Remark9, judgement45igai, courseMatch);
                    // 合格者数
                    count[crsi][15] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_GOUKAKU_OR_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    // 不合格者
                    count[crsi][16] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_FUGOUKAKU, bd9Remark9, judgement45igai, courseMatch);


                    // 事前×の受験生
                    bd9Remark9 = "2";
                    count[crsi][17] = getCount(db2, shdiv, examcoursecd, null, null, bd9Remark9, judgement45igai, courseMatch);
                    // 合格者数
                    count[crsi][18] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_GOUKAKU_OR_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    // 不合格者
                    count[crsi][19] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_FUGOUKAKU, bd9Remark9, judgement45igai, courseMatch);


                    // 面接
                    bd9Remark9 = null;
                    // 仕様不明

                    // 集計
                    bd9Remark9 = null;
                    // 合格者数
                    count[crsi][22] = getCount(db2, shdiv, null, examcoursecd, JUDGEMENT_GOUKAKU_OR_MAWASHI, bd9Remark9, judgement45igai, courseMatch);
                    // 不合格者
                    count[crsi][23] = getCount(db2, shdiv, examcoursecd, null, JUDGEMENT_FUGOUKAKU, bd9Remark9, judgement45igai, courseMatch);
                    // 合計
                    count[crsi][24] = count[crsi][22] + count[crsi][23];

                    for (int i = 0; i < count[TOTAL_IDX].length; i++) {
                        count[TOTAL_IDX][i] += count[crsi][i];
                    }
                }

                final String[][] field = new String[25][2];
                field[0] = new String[] {"APPLI1", "APPLI2", }; // 出願者数
                field[1] = new String[] {"DECLEN1", "DECLEN2"}; // 辞退者数
                field[2] = new String[] {"ABSENCE1", "ABSENCE2"}; // 辞退者数
                field[3] = new String[] {"EXAM1", "EXAM2"}; // 受験者数

                field[4] = new String[] {"PRE1", "PRE7"}; // 事前者数
                field[5] = new String[] {"PASS1_1", "PASS7_1"}; // 合格者数

                field[6] = new String[] {"PRE2", "PRE8"}; // 事前者数
                field[7] = new String[] {"PASS2_1", "PASS8_1"}; // 合格者数
                field[8] = new String[] {"FAILURE2_1", "FAILURE8_1"}; // 不合格者数
                field[9] = new String[] {"PASS2_2", "PASS8_2"}; // 合格者数
                field[10] = new String[] {"PASS2_3", "PASS8_3"}; // 合格者数
                field[11] = new String[] {"PASS2_4", "PASS8_4"}; // 合格者数
                field[12] = new String[] {"PASS2_5", "PASS8_5"}; // 合格者数
                field[13] = new String[] {"FAILURE2_2", "FAILURE8_2"}; // 不合格者数

                field[14] = new String[] {"PRE3", "PRE9"}; // 事前者数
                field[15] = new String[] {"PASS3", "PASS9"}; // 合格者数
                field[16] = new String[] {"FAILURE3", "FAILURE9"}; // 不合格者数

                field[17] = new String[] {"PRE4", "PRE10"}; // 事前者数
                field[18] = new String[] {"PASS4", "PASS10"}; // 合格者数
                field[19] = new String[] {"FAILURE4", "FAILURE10"}; // 不合格者数

                field[20] = new String[] {"COUNSELING5", "COUNSELING11"}; // 合格者数
                field[21] = new String[] {"FAILURE5", "FAILURE11"}; // 不合格者数

                field[22] = new String[] {"PASS6", "PASS12"}; // 合格者数
                field[23] = new String[] {"FAILURE6", "FAILURE12"}; // 不合格者数
                field[24] = new String[] {"TOTAL6", "TOTAL12"}; // 合計

                for (int i = 0; i < 25; i++) {
                    if ("1".equals(_param._gouhiBef) && ((i == 5) || (i >= 7 && i <= 13) || (i >= 15 && i <= 16) || (i >= 18))) {
                        continue;
                    }
                    for (int j = 0; j < count.length; j++) {
                        svf.VrsOutn(field[i][shdivi], j + 1, count[j][i] == 0 ? "" : String.valueOf(count[j][i]));
                    }
                }
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private int getCount(final DB2UDB db2, final String shdiv, final String examcoursecd, final String sucExamcoursecd, final String judgement, final String bd9rem9, final boolean judgement45Igai, final boolean courseMatch) {
        int rtn = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("   INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("      AND BDETAIL1.EXAMNO = BASE.EXAMNO  ");
            stb.append("      AND BDETAIL1.SEQ = '001'   ");
            stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("      AND BDETAIL9.EXAMNO = BASE.EXAMNO  ");
            stb.append("      AND BDETAIL9.SEQ = '009'   ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
            if (!"9".equals(_param._testdiv)) {
                stb.append("     AND BASE.TESTDIV = '" + _param._testdiv + "' ");
            }
            stb.append("     AND BASE.SHDIV = '" + shdiv + "' ");
            if (null != examcoursecd) {
                stb.append("     AND BDETAIL1.REMARK10 = '" + examcoursecd + "' ");
            }
            if (null != sucExamcoursecd) {
                stb.append("     AND BASE.SUC_COURSECODE = '" + sucExamcoursecd + "' ");
            }
            if (null != judgement) {
                stb.append("     AND BASE.JUDGEMENT IN " + judgement + " ");
            }
            if (null != bd9rem9) {
                stb.append("     AND BDETAIL9.REMARK9 = '" + bd9rem9 + "' ");
            }
            if (judgement45Igai) {
                stb.append("     AND VALUE(BASE.JUDGEMENT, '') NOT IN ('4', '5') ");
            }
            if (courseMatch) {
                stb.append("     AND BDETAIL1.REMARK10 <> BASE.SUC_COURSECODE ");
            }
            //log.debug(" sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                rtn = rs.getInt("COUNT");
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 72182 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _gouhiBef; //1:合否判定前用出力

        final String _applicantdivAbbv1;
        final String _testdivAbbv1;
        final List _shdivList;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(db2, _date);
            _gouhiBef = request.getParameter("GOUHI_BEF");
            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
            _testdivAbbv1 = ("9".equals(_testdiv)) ? "全て" : getNameMst(db2, "ABBV1", "L004", _testdiv);
            _shdivList = getShdivList(db2);
        }

        private List getExamcourseMapList(final DB2UDB db2) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     CRS.COURSECD, ");
                stb.append("     CRS.MAJORCD, ");
                stb.append("     CRS.EXAMCOURSECD, ");
                stb.append("     CRS.EXAMCOURSE_NAME ");
                stb.append(" FROM ");
                stb.append("   ENTEXAM_COURSE_MST CRS ");
                stb.append(" WHERE ");
                stb.append("     CRS.ENTEXAMYEAR = '" + _entexamyear + "' ");
                stb.append("     AND CRS.APPLICANTDIV = '" + _applicantdiv + "' ");
                if (!"9".equals(_testdiv)) {
                    stb.append("     AND CRS.TESTDIV = '" + _testdiv + "' ");
                }
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
                    m.put("EXAMCOURSE_NAME", rs.getString("EXAMCOURSE_NAME"));
                    rtn.add(m);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private List getShdivList(final DB2UDB db2) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L006' ";
//                if ("1".equals(_testdiv)) {
//                    sql += " AND NAMESPARE2 = '1' ";
//                } else if ("2".equals(_testdiv)) {
//                    sql += " AND NAMESPARE3 = '1' ";
//                }
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

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final String hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
            final String min = df.format(cal.get(Calendar.MINUTE));
            cal.setTime(Date.valueOf(date));
            return KNJ_EditDate.h_format_JP(db2, date) + "　" + hour + ":" + min;
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

