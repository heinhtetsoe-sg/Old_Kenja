/*
 * $Id: 9bb67f0aa56acc5a5f6344fc1d1c339af02c9e1b $
 *
 * 作成日: 2017/10/24
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL330U {

    private static final Log log = LogFactory.getLog(KNJL330U.class);

    private boolean _hasData;
    private final String KOKUGO = "1";
    private final String SANSUU = "2";
    private final String RIKA = "3";
    private final String SYAKAI = "4";
    private final String GOUKEI = "B";

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            if (!_param._iscsv) {
                svf = new Vrw32alp();
                response.setContentType("application/pdf");
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());
            }

            _hasData = false;
            if (_param._iscsv) {
                final List outputLines = new ArrayList();
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                outputCsv(db2, outputLines);
                final String title = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01") + "度　" + _param._applicantdivName + "　" + _param._testdivName + "　入試結果";
                CsvUtils.outputLines(log, response, title + ".csv", outputLines, csvParam);
            } else {
                printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_param._iscsv) {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_param._iscsv) {
                svf.VrQuit();
            }
        }

    }

    private void outputCsv(final DB2UDB db2, final List outputLineList) {
        final List printList = getList(db2);
        final String title = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01") + "度　" + _param._applicantdivName + "　" + _param._testdivName + "　入試結果";

        outputLineList.add(Arrays.asList(new String[] {title}));
        outputLineList.add(Arrays.asList(new String[] {_param._dateString}));

        //表上）志願者数など
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            outputLineList.add(Arrays.asList(new String[] {"", "人数"}));
            outputLineList.add(Arrays.asList(new String[] {"定員", _param._capacity}));
            outputLineList.add(Arrays.asList(new String[] {"応募者（志願者）", printData._sigan_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"実受験者", printData._juken_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"欠席者", printData._kesseki_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"合格者", printData._pass_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"正規合格者", printData._sei_pass_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"追加合格者", printData._add_pass_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"入学予定者", printData._ent_yotei_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"辞退者", printData._jitai_cnt}));
            outputLineList.add(Arrays.asList(new String[] {"入学者", printData._ent_cnt}));
        }

        outputLineList.add(Arrays.asList(new String[] {}));

        //表下）科目名、平均点、最高点、最低点など
        final List[] outputLines2 = new ArrayList[16];
        for (int i = 0; i < 16; i++) {
            outputLines2[i] = new ArrayList();
            outputLineList.add(outputLines2[i]);
        }
        outputLines2[0].add("");
        outputLines2[1].add("受験者");
        outputLines2[2].add("平均点");
        outputLines2[3].add("最高点");
        outputLines2[4].add("最低点");
        outputLines2[5].add("合格者平均点");
        outputLines2[6].add("合格者最高点");
        outputLines2[7].add("合格者最低点");
        outputLines2[8].add("正規合格者平均点");
        outputLines2[9].add("正規合格者最高点");
        outputLines2[10].add("正規合格者最低点");
        outputLines2[11].add("正規合格者基準点");
        outputLines2[12].add("追加合格者平均点");
        outputLines2[13].add("追加合格者最高点");
        outputLines2[14].add("追加合格者最低点");
        outputLines2[15].add("追加合格者基準点");

        int kamokuCnt = 1;
        for (Iterator itTestKamou = _param._testKamokuList.iterator(); itTestKamou.hasNext();) {
            final TestKamoku testKamoku = (TestKamoku) itTestKamou.next();
            outputLines2[0].add(testKamoku._name);
            //受験者
            setAvgDataCsv(outputLines2, "JUKEN", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
            setAvgDataCsv(outputLines2, "PASS", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
            setAvgDataCsv(outputLines2, "SEI_PASS", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
            setAvgDataCsv(outputLines2, "ADD_PASS", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
            kamokuCnt++;
        }
        outputLines2[0].add("合計");
        setAvgDataCsv(outputLines2, "JUKEN", "B", "SCORE5", _param._avgMap);
        setAvgDataCsv(outputLines2, "PASS", "B", "SCORE5", _param._avgMap);
        setAvgDataCsv(outputLines2, "SEI_PASS", "B", "SCORE5", _param._avgMap);
        setAvgDataCsv(outputLines2, "ADD_PASS", "B", "SCORE5", _param._avgMap);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);
        svf.VrSetForm("KNJL330U.frm", 1);
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01") + "度　" + _param._applicantdivName + "　" + _param._testdivName + "　入試結果");
        svf.VrsOut("DATE", _param._dateString);

        //表上）志願者数など
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            svf.VrsOutn("NUM", 1, _param._capacity);
            svf.VrsOutn("NUM", 2, printData._sigan_cnt);
            svf.VrsOutn("NUM", 3, printData._juken_cnt);
            svf.VrsOutn("NUM", 4, printData._kesseki_cnt);
            svf.VrsOutn("NUM", 5, printData._pass_cnt);
            svf.VrsOutn("NUM", 6, printData._sei_pass_cnt);
            svf.VrsOutn("NUM", 7, printData._add_pass_cnt);
            svf.VrsOutn("NUM", 8, printData._ent_yotei_cnt);
            svf.VrsOutn("NUM", 9, printData._jitai_cnt);
            svf.VrsOutn("NUM", 10, printData._ent_cnt);

            _hasData = true;
        }

        //表下）科目名、平均点、最高点、最低点など
        if (_hasData) {
            int kamokuCnt = 1;
            for (Iterator itTestKamou = _param._testKamokuList.iterator(); itTestKamou.hasNext();) {
                final TestKamoku testKamoku = (TestKamoku) itTestKamou.next();
                svf.VrsOut("CLASS_NAME" + kamokuCnt, testKamoku._name);//科目名
                //受験者
                setAvgData(svf, "JUKEN", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
                setAvgData(svf, "PASS", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
                setAvgData(svf, "SEI_PASS", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
                setAvgData(svf, "ADD_PASS", testKamoku._cd, "SCORE" + kamokuCnt, _param._avgMap);
                kamokuCnt++;
            }
            svf.VrsOut("CLASS_NAME5", "合計");
            setAvgData(svf, "JUKEN", "B", "SCORE5", _param._avgMap);
            setAvgData(svf, "PASS", "B", "SCORE5", _param._avgMap);
            setAvgData(svf, "SEI_PASS", "B", "SCORE5", _param._avgMap);
            setAvgData(svf, "ADD_PASS", "B", "SCORE5", _param._avgMap);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setAvgData(final Vrw32alp svf, final String type, final String cd, final String setField, final Map avgMap) {
        final String key = type + cd;
        if (avgMap.containsKey(key)) {
            final AvgData avgData = (AvgData) avgMap.get(key);
            final String setAvg;
            if (null == avgData._avg) {
                setAvg = null;
            } else {
                final BigDecimal setVal = new BigDecimal(avgData._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                setAvg = setVal.toString();
            }
            if ("JUKEN".equals(type)) {
                svf.VrsOutn(setField, 1, avgData._cnt);
                svf.VrsOutn(setField, 2, setAvg);
                svf.VrsOutn(setField, 3, avgData._maxScore);
                svf.VrsOutn(setField, 4, avgData._minScore);
            } else if ("PASS".equals(type)) {
                svf.VrsOutn(setField, 5, setAvg);
                svf.VrsOutn(setField, 6, avgData._maxScore);
                svf.VrsOutn(setField, 7, avgData._minScore);
            } else if ("SEI_PASS".equals(type)) {
                svf.VrsOutn(setField, 8, setAvg);
                svf.VrsOutn(setField, 9, avgData._maxScore);
                svf.VrsOutn(setField, 10, avgData._minScore);
                if ("B".equals(cd)) {
                    svf.VrsOutn(setField, 11, _param._borderScore);
                }
            } else if ("ADD_PASS".equals(type)) {
                svf.VrsOutn(setField, 12, setAvg);
                svf.VrsOutn(setField, 13, avgData._maxScore);
                svf.VrsOutn(setField, 14, avgData._minScore);
                if ("B".equals(cd)) {
                    svf.VrsOutn(setField, 15, avgData._minScore);
                }
            }
        }
    }

    private void setAvgDataCsv(final List[] outputLines2, final String type, final String cd, final String setField, final Map avgMap) {
        final String key = type + cd;
        if (avgMap.containsKey(key)) {
            final AvgData avgData = (AvgData) avgMap.get(key);
            final String setAvg;
            if (null == avgData._avg) {
                setAvg = null;
            } else {
                final BigDecimal setVal = new BigDecimal(avgData._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                setAvg = setVal.toString();
            }
            if ("JUKEN".equals(type)) {
                outputLines2[1].add(avgData._cnt);
                outputLines2[2].add(setAvg);
                outputLines2[3].add(avgData._maxScore);
                outputLines2[4].add(avgData._minScore);
            } else if ("PASS".equals(type)) {
                outputLines2[5].add(setAvg);
                outputLines2[6].add(avgData._maxScore);
                outputLines2[7].add(avgData._minScore);
            } else if ("SEI_PASS".equals(type)) {
                outputLines2[8].add(setAvg);
                outputLines2[9].add(avgData._maxScore);
                outputLines2[10].add(avgData._minScore);
                if ("B".equals(cd)) {
                    outputLines2[11].add(_param._borderScore);
                } else {
                    outputLines2[11].add("");
                }
            } else if ("ADD_PASS".equals(type)) {
                outputLines2[12].add(setAvg);
                outputLines2[13].add(avgData._maxScore);
                outputLines2[14].add(avgData._minScore);
                if ("B".equals(cd)) {
                    outputLines2[15].add(avgData._minScore);
                } else {
                    outputLines2[15].add("");
                }
            }
        } else {
            if ("JUKEN".equals(type)) {
                outputLines2[1].add("");
                outputLines2[2].add("");
                outputLines2[3].add("");
                outputLines2[4].add("");
            } else if ("PASS".equals(type)) {
                outputLines2[5].add("");
                outputLines2[6].add("");
                outputLines2[7].add("");
            } else if ("SEI_PASS".equals(type)) {
                outputLines2[8].add("");
                outputLines2[9].add("");
                outputLines2[10].add("");
                outputLines2[11].add("");
            } else if ("ADD_PASS".equals(type)) {
                outputLines2[12].add("");
                outputLines2[13].add("");
                outputLines2[14].add("");
                outputLines2[15].add("");
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getCntSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sigan_cnt = rs.getString("SIGAN_CNT");
                final String juken_cnt = rs.getString("JUKEN_CNT");
                final String kesseki_cnt = rs.getString("KESSEKI_CNT");
                final String pass_cnt = rs.getString("PASS_CNT");
                final String sei_pass_cnt = rs.getString("SEI_PASS_CNT");
                final String add_pass_cnt = rs.getString("ADD_PASS_CNT");
                final String ent_yotei_cnt = rs.getString("ENT_YOTEI_CNT");
                final String jitai_cnt = rs.getString("JITAI_CNT");
                final String ent_cnt = rs.getString("ENT_CNT");

                final PrintData printData = new PrintData(sigan_cnt, juken_cnt, kesseki_cnt, pass_cnt, sei_pass_cnt, add_pass_cnt, ent_yotei_cnt, jitai_cnt, ent_cnt);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getCntSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN R1.EXAMNO IS NOT NULL THEN 1 ELSE 0 END) AS SIGAN_CNT, ");
        stb.append("     SUM(CASE WHEN R1.TOTAL4 IS NOT NULL THEN 1 ELSE 0 END) AS JUKEN_CNT, ");
        stb.append("     SUM(CASE WHEN R1.TOTAL4 IS NULL THEN 1 ELSE 0 END) AS KESSEKI_CNT, ");
        stb.append("     SUM(CASE WHEN N1.NAMESPARE1 = '1' THEN 1 ELSE 0 END) AS PASS_CNT, ");
        stb.append("     SUM(CASE WHEN N1.NAMESPARE1 = '1' AND R1.JUDGEDIV <> '3' THEN 1 ELSE 0 END) AS SEI_PASS_CNT, ");
        stb.append("     SUM(CASE WHEN N1.NAMESPARE1 = '1' AND R1.JUDGEDIV = '3' THEN 1 ELSE 0 END) AS ADD_PASS_CNT, ");
        stb.append("     SUM(CASE WHEN B1.PROCEDUREDIV = '1' THEN 1 ELSE 0 END) AS ENT_YOTEI_CNT, ");
        stb.append("     SUM(CASE WHEN B1.PROCEDUREDIV = '1' AND B1.ENTDIV = '2' THEN 1 ELSE 0 END) AS JITAI_CNT, ");
        stb.append("     SUM(CASE WHEN B1.PROCEDUREDIV = '1' AND B1.ENTDIV = '1' THEN 1 ELSE 0 END) AS ENT_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT R1 ");
        stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = R1.JUDGEDIV ");
        stb.append(" WHERE ");
        stb.append("         R1.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("     AND R1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("1".equals(_param._ruikeiFlg)) {
            stb.append("     AND R1.TESTDIV     <= '" + _param._testDiv + "' ");//累計
        } else {
            stb.append("     AND R1.TESTDIV      = '" + _param._testDiv + "' ");
        }
        return stb.toString();
    }

    private class PrintData {
        final String _sigan_cnt;
        final String _juken_cnt;
        final String _kesseki_cnt;
        final String _pass_cnt;
        final String _sei_pass_cnt;
        final String _add_pass_cnt;
        final String _ent_yotei_cnt;
        final String _jitai_cnt;
        final String _ent_cnt;
        public PrintData(
                final String sigan_cnt,
                final String juken_cnt,
                final String kesseki_cnt,
                final String pass_cnt,
                final String sei_pass_cnt,
                final String add_pass_cnt,
                final String ent_yotei_cnt,
                final String jitai_cnt,
                final String ent_cnt
        ) {
            _sigan_cnt = sigan_cnt;
            _juken_cnt = juken_cnt;
            _kesseki_cnt = kesseki_cnt;
            _pass_cnt = pass_cnt;
            _sei_pass_cnt = sei_pass_cnt;
            _add_pass_cnt = add_pass_cnt;
            _ent_yotei_cnt = ent_yotei_cnt;
            _jitai_cnt = jitai_cnt;
            _ent_cnt = ent_cnt;
        }
    }

    private class TestKamoku {
        final String _cd;
        final String _name;
        public TestKamoku(
                final String cd,
                final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }

    private class AvgData {
        final String _type;
        final String _cd;
        final String _cnt;
        final String _avg;
        final String _maxScore;
        final String _minScore;
        public AvgData(
                final String type,
                final String cd,
                final String cnt,
                final String avg,
                final String maxScore,
                final String minScore
        ) {
            _type = type;
            _cd = cd;
            _cnt = cnt;
            _avg = avg;
            _maxScore = maxScore;
            _minScore = minScore;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65481 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _ruikeiFlg;
        private final String _loginYear;
        private final String _dateString;
        private final boolean _iscsv;

        final String _applicantdivName;
        final String _testdivName;
        final String _capacity;
        final String _borderScore;
        private final List _testKamokuList;
        private final Map _avgMap = new TreeMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _ruikeiFlg = request.getParameter("RUIKEI_FLG");//1:指定回までの累計
            _loginYear = request.getParameter("LOGIN_YEAR");

            _iscsv = "csv".equals(request.getParameter("cmd"));//CSV

            final Calendar cal = Calendar.getInstance();
            _dateString = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(cal.getTime());

            _applicantdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _capacity = StringUtils.defaultString(getCapacity(db2));
            _borderScore = StringUtils.defaultString(getBorder(db2));
            _testKamokuList = getTestKamokuList(db2);
            _avgMap.putAll(getAvgMap(db2, "JUKEN"));
            _avgMap.putAll(getAvgMap(db2, "PASS"));
            _avgMap.putAll(getAvgMap(db2, "SEI_PASS"));
            _avgMap.putAll(getAvgMap(db2, "ADD_PASS"));
        }

        private String getCapacity(final DB2UDB db2) {
            String rtn = null;
            final String sql = getCapacitySql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("CAPACITY");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getCapacitySql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SUM(CAPACITY) AS CAPACITY ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            if ("1".equals(_ruikeiFlg)) {
                stb.append("     AND TESTDIV <= '" + _testDiv + "' ");//累計
            } else {
                stb.append("     AND TESTDIV  = '" + _testDiv + "' ");
            }
            return stb.toString();
        }

        private String getBorder(final DB2UDB db2) {
            String rtn = null;
            final String sql = getBorderSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("BORDER_SCORE");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getBorderSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MIN(BORDER_SCORE) AS BORDER_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PASSINGMARK_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            if ("1".equals(_ruikeiFlg)) {
                stb.append("     AND TESTDIV <= '" + _testDiv + "' ");//累計
            } else {
                stb.append("     AND TESTDIV  = '" + _testDiv + "' ");
            }
            return stb.toString();
        }

        private Map getAvgMap(final DB2UDB db2, final String type) {
            final Map retMap = new TreeMap();
            final String sql = getAvgSql(type);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("TESTSUBCLASSCD");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("CALC_AVG");
                    final String maxScore = rs.getString("MAX_SCORE");
                    final String minScore = rs.getString("MIN_SCORE");
                    final AvgData avgData = new AvgData(type, cd, cnt, avg, maxScore, minScore);
                    final String key = type + cd;
                    retMap.put(key, avgData);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
        }

        private String getAvgSql(final String type) {
            final StringBuffer stb = new StringBuffer();
            //得点
            stb.append(" SELECT ");
            stb.append("     '" + type + "' AS TYPE, ");
            stb.append("     S1.TESTSUBCLASSCD, ");
            stb.append("     COUNT(S1.SCORE) AS COUNT, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(S1.SCORE))*100000,0)/100000,8,5) AS CALC_AVG, ");
            stb.append("     MAX(S1.SCORE) AS MAX_SCORE, ");
            stb.append("     MIN(S1.SCORE) AS MIN_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT S1 ");
            stb.append("          ON S1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("         AND S1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND S1.TESTDIV = R1.TESTDIV ");
            stb.append("         AND S1.EXAM_TYPE = R1.EXAM_TYPE ");
            stb.append("         AND S1.RECEPTNO = R1.RECEPTNO ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = R1.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("         R1.ENTEXAMYEAR  = '" + _entexamyear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _applicantDiv + "' ");
            if ("1".equals(_ruikeiFlg)) {
                stb.append("     AND R1.TESTDIV     <= '" + _testDiv + "' ");//累計
            } else {
                stb.append("     AND R1.TESTDIV      = '" + _testDiv + "' ");
            }
            if (!"JUKEN".equals(type)) {
                stb.append("     AND N1.NAMESPARE1   = '1' ");//合格
            }
            if ("SEI_PASS".equals(type)) {
                stb.append("     AND R1.JUDGEDIV    <> '3' ");//正規合格
            } else if ("ADD_PASS".equals(type)) {
                stb.append("     AND R1.JUDGEDIV     = '3' ");//追加合格
            }
            stb.append(" GROUP BY ");
            stb.append("     S1.TESTSUBCLASSCD ");
            //合計(B)(TOTAL4)
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + type + "' AS TYPE, ");
            stb.append("     'B' AS TESTSUBCLASSCD, ");
            stb.append("     COUNT(TT4.TOTAL) AS COUNT, ");
            stb.append("     DECIMAL(ROUND(AVG(FLOAT(TT4.TOTAL))*100000,0)/100000,8,5) AS CALC_AVG, ");
            stb.append("     MAX(TT4.TOTAL) AS MAX_SCORE, ");
            stb.append("     MIN(TT4.TOTAL) AS MIN_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT R1 ");
            stb.append("    LEFT JOIN (SELECT ");
            stb.append("       T1.RECEPTNO, ");
            stb.append("       T1.TESTDIV, ");
            stb.append("       SUM(T1.SCORE) AS TOTAL ");
            stb.append("     FROM ");
            stb.append("       ENTEXAM_SCORE_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("       T1.ENTEXAMYEAR  = '" + _entexamyear + "' ");
            stb.append("       AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("    GROUP BY ");
            stb.append("       T1.RECEPTNO, ");
            stb.append("       T1.TESTDIV ");
            stb.append("    ) TT4 ON TT4.TESTDIV = R1.TESTDIV AND TT4.RECEPTNO = R1.RECEPTNO ");
            stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
            stb.append("         AND B1.APPLICANTDIV = R1.APPLICANTDIV ");
            stb.append("         AND B1.EXAMNO = R1.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = R1.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("         R1.ENTEXAMYEAR  = '" + _entexamyear + "' ");
            stb.append("     AND R1.APPLICANTDIV = '" + _applicantDiv + "' ");
            if ("1".equals(_ruikeiFlg)) {
                stb.append("     AND R1.TESTDIV     <= '" + _testDiv + "' ");//累計
            } else {
                stb.append("     AND R1.TESTDIV      = '" + _testDiv + "' ");
            }
            if (!"JUKEN".equals(type)) {
                stb.append("     AND N1.NAMESPARE1   = '1' ");//合格
            }
            if ("SEI_PASS".equals(type)) {
                stb.append("     AND R1.JUDGEDIV    <> '3' ");//正規合格
            } else if ("ADD_PASS".equals(type)) {
                stb.append("     AND R1.JUDGEDIV     = '3' ");//追加合格
            }
            stb.append(" ORDER BY ");
            stb.append("     TESTSUBCLASSCD ");
            return stb.toString();
        }

        private List getTestKamokuList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' AND NAME1 IS NOT NULL ORDER BY NAMECD2";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    final TestKamoku testKamoku = new TestKamoku(cd, name);
                    retList.add(testKamoku);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retList;
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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
