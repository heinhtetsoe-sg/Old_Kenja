/*
 * $Id: 2ba40592c8ae5e5613fb15447bd3a521dc31b8fc $
 *
 * 作成日: 2017/12/19
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJH114C {

    private static final Log log = LogFactory.getLog(KNJH114C.class);

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            final Map schJohoMap = getSikakuMap(db2, _param);

            if (_param._isCsv) {
                outputCsv(response, schJohoMap);
            } else {
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                if (!schJohoMap.isEmpty()) {
                    printMain(svf, schJohoMap);
                }
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (null != _param && _param._isCsv) {
            } else {
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
            svf.VrQuit();
        }

    }

    private void outputCsv(final HttpServletResponse response, final Map schJohoMap) {
        final List lines = getCsvOutputLines(schJohoMap);

        CsvUtils.outputLines(log, response, getTitle() + ".csv" , lines);
    }

    private List getCsvOutputLines(final Map schJohoMap) {
        final List lines = new ArrayList();

        String befHrName = "";
        newLine(lines).addAll(Arrays.asList(new String[] {getTitle(), "", getPrintDate()}));
        newLine(lines).addAll(Arrays.asList(new String[] {""}));//空行

        final List header1 = newLine(lines);
        header1.addAll(Arrays.asList(new String[] {"年組", "担任", "番号", "氏名",}));
        final List sikakuList = new ArrayList();
        for (Iterator itSikaku = _param._sikakuCdNameMap.keySet().iterator(); itSikaku.hasNext();) {
            final String sikakuCd = (String) itSikaku.next();
            final String nameAbbv = (String) _param._sikakuCdNameMap.get(sikakuCd);
            sikakuList.add(nameAbbv + "_級");
            sikakuList.add(nameAbbv + "_取得日");
        }
        header1.addAll(sikakuList);//資格

        for (Iterator itSchreg = _param._schregMap.keySet().iterator(); itSchreg.hasNext();) {
            final String gradeHrclassNo = (String) itSchreg.next();
            final PrintSchBaseData printData = (PrintSchBaseData) _param._schregMap.get(gradeHrclassNo);

            if (!"".equals(befHrName) && !befHrName.equals(printData._hrName)) {
                newLine(lines).addAll(Arrays.asList(new String[] {""})); //クラスが変わったら改行
            }
            final List line = newLine(lines);

            line.add(printData._hrName);        // 年組
            line.add(printData._staffName);     // 担任
            line.add(printData._attendNo);      // 出席番号
            line.add(printData._name);          // 氏名
            for (Iterator itSikaku = _param._sikakuCdNameMap.keySet().iterator(); itSikaku.hasNext();) {
                final String sikakuCd = (String) itSikaku.next();
                if (null != schJohoMap.get(printData._schregNo + "-" + sikakuCd)) {
                    final PrintSchData schData = (PrintSchData) schJohoMap.get(printData._schregNo + "-" + sikakuCd);
                    final String testNameAbbv = (String) _param._jukenKyuNameMap.get(sikakuCd + "-" + schData._resultCd);

                    line.add(testNameAbbv);             // 級
                    line.add(KNJ_EditDate.h_format_thi(schData._testDate, 0));          // 取得日
                } else {
                    line.add("");
                    line.add("");
                }
            }

            befHrName = printData._hrName;
        }

        _hasData = true;

        return lines;
    }

    private List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private String getTitle() {
        final String title = "検定取得状況一覧表";
        return title;
    }

    private String getPrintDate() {
        final String printDate = "作成日：" + KNJ_EditDate.h_format_JP(_param._ctrlDate);
        return printDate;
    }

    private void printMain(final Vrw32alp svf, final Map schJohoMap) {
        svf.VrSetForm("KNJH114C.frm", 1);

        final int maxLine = 50;
        int printLine = 1;
        String befHrName = "";

        for (Iterator itSchreg = _param._schregMap.keySet().iterator(); itSchreg.hasNext();) {
            final String gradeHrclassNo = (String) itSchreg.next();
            final PrintSchBaseData printData = (PrintSchBaseData) _param._schregMap.get(gradeHrclassNo);

            if (!"".equals(befHrName) && !befHrName.equals(printData._hrName)) {
                svf.VrEndPage();
                printLine = 1;
            }
            if (printLine > maxLine) {
                svf.VrEndPage();
                printLine = 1;
            }

            svf.VrsOut("TITLE", getTitle());    //タイトル
            svf.VrsOut("DATE", getPrintDate()); //作成日

            int kenteiNameField = 1;
            for (Iterator itSikaku = _param._sikakuCdNameMap.keySet().iterator(); itSikaku.hasNext();) {
                final String sikakuCd = (String) itSikaku.next();
                final String nameAbbv = (String) _param._sikakuCdNameMap.get(sikakuCd);
                svf.VrsOut("PROFICIENCY" + kenteiNameField, nameAbbv);//検定名
                kenteiNameField++;
            }

            svf.VrsOut("HR_NAME", printData._hrName);             // 年組
            svf.VrsOut("TEACHER_NAME", printData._staffName);     // 担任
            svf.VrsOutn("NO", printLine, printData._attendNo);    // 出席番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "2": "1";
            svf.VrsOutn("NAME" + nameField, printLine, printData._name);      // 氏名

            int kenteiRankField = 1;
            for (Iterator itSikaku = _param._sikakuCdNameMap.keySet().iterator(); itSikaku.hasNext();) {
                final String sikakuCd = (String) itSikaku.next();

                if (null != schJohoMap.get(printData._schregNo + "-" + sikakuCd)) {
                    final PrintSchData schData = (PrintSchData) schJohoMap.get(printData._schregNo + "-" + sikakuCd);
                    final String testNameAbbv = (String) _param._jukenKyuNameMap.get(sikakuCd + "-" + schData._resultCd);

                    final String rankField = KNJ_EditEdit.getMS932ByteLength(testNameAbbv) > 10 ? "2": "1";
                    svf.VrsOutn("RANK" + kenteiRankField + "_" + rankField, printLine, testNameAbbv);             // 級
                    svf.VrsOutn("GET_DATE" + kenteiRankField, printLine, KNJ_EditDate.h_format_thi(schData._testDate, 0));          // 取得日
                }
                kenteiRankField++;
            }

            printLine++;
            befHrName = printData._hrName;

            _hasData = true;
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private  Map getSikakuMap(final DB2UDB db2, final Param param) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql(param);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String qualifiedCd   = rs.getString("QUALIFIED_CD");
                final String resultCd      = rs.getString("RESULT_CD");
                final String testDate      = rs.getString("TEST_DATE");

                final PrintSchData printSchData = new PrintSchData(qualifiedCd, resultCd, testDate);
                retMap.put(rs.getString("SCHREGNO") + "-" + rs.getString("QUALIFIED_CD"), printSchData);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private static String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     TEST.SCHREGNO, ");
        stb.append("     TEST.QUALIFIED_CD,     ");
        stb.append("     MIN(TEST.RESULT_CD) AS RESULT_CD, ");
        stb.append("     MAX(TEST_DATE) AS TEST_DATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_QUALIFIED_TEST_DAT TEST ");
        stb.append("     LEFT JOIN QUALIFIED_RESULT_MST RS_M ON TEST.YEAR         = RS_M.YEAR ");
        stb.append("                                        AND TEST.QUALIFIED_CD = RS_M.QUALIFIED_CD ");
        stb.append("                                        AND TEST.RESULT_CD    = RS_M.RESULT_CD ");
        stb.append("     LEFT JOIN QUALIFIED_TEST_MST TS_M ON TEST.YEAR         = TS_M.YEAR ");
        stb.append("                                      AND TEST.QUALIFIED_CD = TS_M.QUALIFIED_CD ");
        stb.append("                                      AND TEST.TEST_CD      = TS_M.TEST_CD ");
        stb.append("     LEFT JOIN QUALIFIED_MST QU_M ON TEST.QUALIFIED_CD = QU_M.QUALIFIED_CD ");
        stb.append(" WHERE ");
        stb.append("         TEST.RESULT_CD IS NOT NULL ");
        stb.append("     AND TEST.RESULT_CD NOT IN ('8888', '9999') ");
        stb.append("     AND RS_M.CERT_FLG  = 'T' ");
        stb.append("     AND QU_M.MANAGEMENT_FLG  = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     TEST.SCHREGNO, ");
        stb.append("     TEST.QUALIFIED_CD ");

        return stb.toString();
    }


    private class PrintSchData {
        final String _qualifiedCd;
        final String _resultCd;
        final String _testDate;
        public PrintSchData(
                final String qualifiedCd,
                final String resultCd,
                final String testDate
        ) {
            _qualifiedCd = qualifiedCd;
            _resultCd    = resultCd;
            _testDate    = testDate;
        }
    }

    private class PrintSchBaseData {
        final String _schregNo;
        final String _hrName;
        final String _staffName;
        final String _attendNo;
        final String _name;
        public PrintSchBaseData(
                final String schregNo,
                final String hrName,
                final String staffName,
                final String attendNo,
                final String name
        ) {
            _schregNo  = schregNo;
            _hrName    = hrName;
            _staffName = staffName;
            _attendNo  = attendNo;
            _name      = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58057 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _cmd;
        private final boolean _isCsv;
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _taisyou;
        private final String _categorySelectedIn;
        private final Map _sikakuCdNameMap;
        private final Map _jukenKyuNameMap;
        private final Map _schregMap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _cmd          = request.getParameter("cmd");
            _isCsv        = "csv".equals(_cmd);
            _year         = request.getParameter("CTRL_YEAR");
            _semester     = request.getParameter("CTRL_SEMESTER");
            _ctrlDate     = request.getParameter("LOGIN_DATE");
            _taisyou      = request.getParameter("TAISYOU");
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _categorySelectedIn = getCategorySelectedIn(categorySelected);
            _sikakuCdNameMap    = getsikakuCdNameMap(db2, _year);
            _jukenKyuNameMap    = getJukenKyuNameMap(db2, _year);
            _schregMap          = getSchregMap(db2);
        }

        private String getCategorySelectedIn(final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + categorySelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

        private Map getsikakuCdNameMap(final DB2UDB db2, final String year) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT DISTINCT T1.QUALIFIED_CD,T2.QUALIFIED_ABBV FROM QUALIFIED_RESULT_MST T1 LEFT JOIN QUALIFIED_MST T2 ON T1.QUALIFIED_CD = T2.QUALIFIED_CD WHERE T1.YEAR = '" + year + "' ORDER BY T1.QUALIFIED_CD ");
                rs = ps.executeQuery();
                while (rs.next()) {
                  retMap.put(rs.getString("QUALIFIED_CD"), rs.getString("QUALIFIED_ABBV"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getSchregMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchreg();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregNo  = rs.getString("SCHREGNO");
                    final String hrName    = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");
                    final String attendNo  = rs.getString("ATTENDNO");
                    final String name      = rs.getString("NAME");

                    final PrintSchBaseData printSchBaseData = new PrintSchBaseData(schregNo, hrName, staffName, attendNo, name);
                    retMap.put(rs.getString("GRADE") + rs.getString("HR_CLASS") + rs.getString("ATTENDNO"), printSchBaseData);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getSchreg() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     H1.HR_NAME, ");
            stb.append("     S1.STAFFNAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     L1.NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN ");
            stb.append("     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ");
            stb.append("                        AND H1.SEMESTER = T1.SEMESTER ");
            stb.append("                        AND H1.GRADE    = T1.GRADE ");
            stb.append("                        AND H1.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN ");
            stb.append("     STAFF_MST S1 ON H1.TR_CD1 = S1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR     = '"+ _year +"' ");
            stb.append("     AND T1.SEMESTER = '"+ _semester +"' ");
            if ("1".equals(_taisyou)) {
                stb.append("     AND T1.GRADE || T1.HR_CLASS IN "+ _categorySelectedIn +" ");
            } else {
                stb.append("     AND T1.SCHREGNO IN "+ _categorySelectedIn +" ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }

        private Map getJukenKyuNameMap(final DB2UDB db2, final String year) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getJukenKyuName(year);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retMap.put(rs.getString("QUALIFIED_CD") + "-" + rs.getString("TEST_CD"), rs.getString("TEST_NAME_ABBV"));
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getJukenKyuName(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.QUALIFIED_CD, ");
            stb.append("         T1.TEST_CD, ");
            stb.append("         T1.TEST_NAME_ABBV ");
            stb.append("     FROM ");
            stb.append("         QUALIFIED_TEST_MST T1 ");
            stb.append("         LEFT JOIN QUALIFIED_RESULT_MST T3 ON T1.YEAR         = T3.YEAR ");
            stb.append("                                          AND T1.QUALIFIED_CD = T3.QUALIFIED_CD ");
            stb.append("                                          AND T1.TEST_CD      = T3.RESULT_CD ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR     = '"+ year +"' ");
            stb.append("         AND T3.CERT_FLG = 'T' ");
            stb.append("     ORDER BY ");
            stb.append("         T1.QUALIFIED_CD ");

            return stb.toString();
        }
    }
}

// eof
