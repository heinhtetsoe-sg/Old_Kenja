// kanji=漢字
/*
 * $Id: b19494a72b8b110bb74bfd667e8d84b84915e968 $
 *
 * 作成日: 2011/12/02 16:56:30 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: b19494a72b8b110bb74bfd667e8d84b84915e968 $
 */
public class KNJL345C {

    private static final Log log = LogFactory.getLog("KNJL345C.class");

    private boolean _hasData;
    private final String SUBCLASS_ALL = "99";
    private final String SUBCLASS_ALL2 = "88";

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            if ("9".equals(_param._testDiv)) {
                String[] testDivPrint = new String[]{};
                if("1".equals(_param._applicantDiv)) {
                    testDivPrint = new String[]{"1","2"};
                } else if("2".equals(_param._applicantDiv)) {
                    if (_param.isGojo()) {
                        testDivPrint = new String[]{"3","4","5","7"};
                    } else {
                        testDivPrint = new String[]{"3","4","5"};
                    }
                }
                for (int i = 0; i < testDivPrint.length; i++) {
                    final String testDiv = testDivPrint[i];
                    printMain(db2, svf, testDiv);
                }
            } else {
                printMain(db2, svf, _param._testDiv);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String testDiv) throws SQLException {
        final List subclassMap = getPrintMap(db2, testDiv);
        final List studentMap = getStudent(db2, testDiv);

        for (final Iterator itStudent = studentMap.iterator(); itStudent.hasNext();) {
            final OutPutData outPutData = (OutPutData) itStudent.next();

            setTitle(db2, svf, testDiv, outPutData);
            for (final Iterator itSub = subclassMap.iterator(); itSub.hasNext();) {
                final SubclassData subclassData = (SubclassData) itSub.next();
                //カレッジ中学A日程のⅡ型(国算)受験者の場合、理科と３教科合計を出力しない
                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv) && "2".equals(outPutData._subOrder) && ("3".equals(subclassData._subclassCd) || SUBCLASS_ALL.equals(subclassData._subclassCd))) {
                    continue;
                }
                final String fName = subclassData.getSetField();
                svf.VrsOut(fName, subclassData._subclassName);
                final BigDecimal setVal = new BigDecimal(subclassData._totalScore).divide(new BigDecimal(subclassData._totalNinzu), 1, BigDecimal.ROUND_HALF_UP);
                svf.VrsOut(fName + "_TOTAL", String.valueOf(subclassData._totalNinzu));
                svf.VrsOut(fName + "_AVE", setVal.toString());

                int studentScore = 999999;
                if (outPutData._scoreMap.containsKey(subclassData._subclassCd)) {
                    studentScore = Integer.parseInt((String) outPutData._scoreMap.get(subclassData._subclassCd));
                }
                int gyo = 1;
                for (final Iterator itPrint = subclassData._bunpuList.iterator(); itPrint.hasNext();) {
                    final PrintData printData = (PrintData) itPrint.next();
                    final int jougen = Integer.parseInt(printData._jougen);
                    final int kagen = Integer.parseInt(printData._kagen);
                    String setMaru = "";
                    if (jougen >= studentScore && studentScore >= kagen) {
                        setMaru = "〇";
                    }
                    //カレッジ中学A日程の場合、理科(100点)と２教科合計(300点)
                    if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv) && "3".equals(subclassData._subclassCd) && gyo <= 4 ||
                        _param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv) && SUBCLASS_ALL2.equals(subclassData._subclassCd) && gyo <= 4 ||
                        _param.isCollege() && "1".equals(_param._applicantDiv) && "2".equals(testDiv) && SUBCLASS_ALL.equals(subclassData._subclassCd) && gyo <= 4 ||
                        _param.isCollege() && "1".equals(_param._applicantDiv) && "7".equals(testDiv) && "6".equals(subclassData._subclassCd) && gyo <= 4 ||
                        _param.isCollege() && "1".equals(_param._applicantDiv) && "7".equals(testDiv) && "7".equals(subclassData._subclassCd) && gyo <= 4 ||
                        _param.isCollege() && "1".equals(_param._applicantDiv) && "7".equals(testDiv) && SUBCLASS_ALL.equals(subclassData._subclassCd) && gyo <= 9
                    ) {
                        svf.VrsOutn(fName + "_NUM", gyo, "");
                        svf.VrsOutn(fName + "_SUM", gyo, "");
                    } else {
                        svf.VrsOutn(fName + "_NUM", gyo, setMaru + printData._ninzu);
                        svf.VrsOutn(fName + "_SUM", gyo, printData._ruikei);
                    }
                    gyo++;
                }
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String testDiv, final OutPutData outPutData) {
        if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
            svf.VrSetForm("KNJL345C_C_2.frm", 1);
        } else if (_param.isCollege()) {
            svf.VrSetForm("KNJL345C_C.frm", 1);
        } else {
            svf.VrSetForm("KNJL345C_G.frm", 1);
        }
        svf.VrsOut("NENDO", _param.getNendo());
        final String applicant = _param.getNameMst(db2, "L003", _param._applicantDiv);
        final String test = _param.getNameMst(db2, "L004", testDiv);
        svf.VrsOut("APPLICANTDIV", applicant + test);
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
        svf.VrsOut("DAY", _param.getDateString());

        svf.VrsOut("ZIPCD", outPutData._gZipcd);
        svf.VrsOut("ADDR1" + (getMS932ByteCount(outPutData._gAddr1) > 40 ? "_2" : ""), outPutData._gAddr1);
        svf.VrsOut("ADDR2" + (getMS932ByteCount(outPutData._gAddr2) > 40 ? "_2" : ""), outPutData._gAddr2);
        svf.VrsOut("GURD_NAME" + (getMS932ByteCount(outPutData._gName) > 24 ? "2" : ""), outPutData._gName);
        svf.VrsOut("HR_ATTNO_NAME", outPutData._name);
        svf.VrsOut("EXAMNO", outPutData._examno);
        svf.VrsOut("NAME", outPutData._name);
    }

    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private List getStudent(DB2UDB db2, String testDiv) throws SQLException {
        final List retList = new ArrayList();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     L2.RECEPTNO, ");
        stb.append("     T1.SUB_ORDER, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     L1.GNAME, ");
        stb.append("     L1.GKANA, ");
        stb.append("     L1.GZIPCD, ");
        stb.append("     L1.GADDRESS1, ");
        stb.append("     L1.GADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("          AND T1.EXAMNO = L1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT L2 ON T1.ENTEXAMYEAR = L2.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = L2.APPLICANTDIV ");
        stb.append("          AND T1.TESTDIV = L2.TESTDIV ");
        stb.append("          AND T1.EXAMNO = L2.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
        stb.append("     AND T1.PERSONAL_FLG = '1' ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String testdiv = rs.getString("TESTDIV");
                final String receptno = rs.getString("RECEPTNO");
                final String subOrder = rs.getString("SUB_ORDER");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String gName = rs.getString("GNAME");
                final String gKana = rs.getString("GKANA");
                final String gAddr1 = rs.getString("GADDRESS1");
                final String gAddr2 = rs.getString("GADDRESS2");
                final String gZipcd = rs.getString("GZIPCD");
                final OutPutData outPutData = new OutPutData(
                        db2,
                        testdiv,
                        examno,
                        receptno,
                        subOrder,
                        name,
                        nameKana,
                        gName,
                        gKana,
                        gAddr1,
                        gAddr2,
                        gZipcd
                        );
                retList.add(outPutData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private List getPrintMap(final DB2UDB db2, final String testDiv) throws SQLException {
        final List retList = new ArrayList();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KOTEI(TESTSUBCLASSCD, NAME1, SHOWORDER) AS ( ");
        if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
            stb.append("     VALUES('" + SUBCLASS_ALL2 + "', '２教科合計', " + SUBCLASS_ALL2 + ") ");
            stb.append("     UNION ALL ");
            stb.append("     VALUES('" + SUBCLASS_ALL + "', '３教科合計', " + SUBCLASS_ALL + ") ");
        } else if (_param.isCollege()) {
            stb.append("     VALUES('" + SUBCLASS_ALL + "', '全教科合計', " + SUBCLASS_ALL + ") ");
        } else {
            stb.append("     VALUES('" + SUBCLASS_ALL + "', '全教科合計', " + SUBCLASS_ALL + ") ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.TESTSUBCLASSCD, ");
        stb.append("     L1.NAME1, ");
        stb.append("     T1.SHOWORDER ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L009' ");
        stb.append("          AND L1.NAMECD2 = T1.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + testDiv + "' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     KOTEI ");
        stb.append(" ORDER BY ");
        stb.append("     SHOWORDER ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("TESTSUBCLASSCD");
                final String subclassName = rs.getString("NAME1");
                final String showOrder = rs.getString("SHOWORDER");
                int kizami = 10;
                int maxScore = 150;
                if (SUBCLASS_ALL.equals(subclassCd) || SUBCLASS_ALL2.equals(subclassCd)) {
                    kizami = 20;
                    maxScore = 400;
                }
                final SubclassData subclassData = new SubclassData(db2, testDiv, subclassCd, subclassName, showOrder, kizami, maxScore);
                retList.add(subclassData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private class SubclassData {
        private final String _subclassCd;
        private final String _subclassName;
        private final String _showOrder;
        private final List _bunpuList;
        int _totalNinzu = 0;
        int _totalScore = 0;

        public SubclassData(
                final DB2UDB db2,
                final String testDiv,
                final String subclassCd,
                final String subclassName,
                final String showOrder,
                final int kizami,
                final int maxScore
        ) throws SQLException {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _showOrder = showOrder;
            _bunpuList = new ArrayList();
            int jougen = maxScore;
            int kagen = maxScore - kizami;
            int ruikei = 0;
            for (int i = 0; i < maxScore; i += kizami) {
                final String cntSql = getCntSql(subclassCd, testDiv, kagen, jougen);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(cntSql);
                    rs = ps.executeQuery();
                    String ninzu = "0";
                    while (rs.next()) {
                        ruikei = ruikei + rs.getInt("CNT");
                        ninzu = rs.getString("CNT");
                        _totalNinzu = _totalNinzu + rs.getInt("CNT");
                        _totalScore = _totalScore + rs.getInt("T_SCORE");
                    }
                    final PrintData printData = new PrintData(ninzu, String.valueOf(ruikei), String.valueOf(kagen), String.valueOf(jougen));
                    _bunpuList.add(printData);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }

                if (jougen == maxScore) {
                    jougen = jougen - kizami - 1;
                } else {
                    jougen = jougen - kizami;
                }
                kagen = kagen - kizami;
            }
        }

        private String getCntSql(final String subclassCd, final String testDiv, final int kagen, final int jougen) {
            final StringBuffer stb = new StringBuffer();
            if (SUBCLASS_ALL.equals(subclassCd)) {
                stb.append(" SELECT ");
                stb.append("     COUNT(*) AS CNT, ");
                stb.append("     SUM(T1.TOTAL4) AS T_SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
                stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
                stb.append("     AND T1.TOTAL4 BETWEEN " + kagen + " AND " + jougen + " ");
            } else if (SUBCLASS_ALL2.equals(_subclassCd)) {
                stb.append(" SELECT ");
                stb.append("     COUNT(*) AS CNT, ");
                stb.append("     SUM(T1.TOTAL2) AS T_SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
                stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
                stb.append("     AND T1.TOTAL2 BETWEEN " + kagen + " AND " + jougen + " ");
            } else {
                stb.append(" SELECT ");
                stb.append("     COUNT(*) AS CNT, ");
                stb.append("     SUM(T1.SCORE) AS T_SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_SCORE_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
                stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
                stb.append("     AND T1.TESTSUBCLASSCD = '" + subclassCd + "' ");
                stb.append("     AND T1.SCORE BETWEEN " + kagen + " AND " + jougen + " ");
            }
            return stb.toString();
        }

        private String getSetField() {
            if (SUBCLASS_ALL.equals(_subclassCd)) {
                return  "THREE";
            } else if (SUBCLASS_ALL2.equals(_subclassCd)) {
                return  "TWO";
            } else {
                return  "CLASS" + _showOrder;
            }
        }

        public String toString() {
            return _subclassCd + " : " + _subclassName;
        }
    }

    private class PrintData {
        final String _ninzu;
        final String _ruikei;
        final String _kagen;
        final String _jougen;

        public PrintData(final String ninzu, final String ruikei, final String kagen, final String jougen) {
            _ninzu = ninzu;
            _ruikei = ruikei;
            _kagen = kagen;
            _jougen = jougen;
        }

        public String toString() {
            return _kagen + " - " + _jougen + " : 人数 = " + _ninzu + " 累計 = " + _ruikei;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class OutPutData {
        final String _examno;
        final String _receptno;
        final String _subOrder; //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        final String _name;
        final String _nameKana;
        final String _gName;
        final String _gKana;
        final String _gAddr1;
        final String _gAddr2;
        final String _gZipcd;
        final Map _scoreMap;

        public OutPutData(
                final DB2UDB db2,
                final String testDiv,
                final String examno,
                final String receptno,
                final String subOrder,
                final String name,
                final String nameKana,
                final String gName,
                final String gKana,
                final String addr1,
                final String addr2,
                final String zipcd
        ) throws SQLException {
            _examno       = examno;
            _receptno     = receptno;
            _subOrder     = subOrder;
            _name         = name;
            _nameKana     = nameKana;
            _gName        = gName;
            _gKana        = gKana;
            _gAddr1       = addr1;
            _gAddr2       = addr2;
            _gZipcd       = zipcd;
            _scoreMap = setScoreMap(db2, testDiv);
        }

        private Map setScoreMap(DB2UDB db2, String testDiv) throws SQLException {
            final Map retMap = new HashMap();
            final String cntSql = getScoreSql(testDiv);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(cntSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("TESTSUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    retMap.put(subclasscd, score);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private String getScoreSql(final String testDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     VALUE(T1.SCORE, 999999) AS SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
            stb.append("     AND T1.RECEPTNO = '" + _receptno + "' ");
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     '" + SUBCLASS_ALL2 + "' AS TESTSUBCLASSCD, ");
                stb.append("     VALUE(T1.TOTAL2, 999999) AS SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
                stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
                stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
                stb.append("     AND T1.RECEPTNO = '" + _receptno + "' ");
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + SUBCLASS_ALL + "' AS TESTSUBCLASSCD, ");
            stb.append("     VALUE(T1.TOTAL4, 999999) AS SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + testDiv + "' ");
            stb.append("     AND T1.RECEPTNO = '" + _receptno + "' ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64297 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _date;
        final String _loginDate;

        final boolean _seirekiFlg;
        final String _z010SchoolCode;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _date = request.getParameter("PRINT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);

        }

        String getNendo() {
            return _seirekiFlg ? _year+"年度":
                KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }

        String getDateString() {
            return getDateString(_date);
        }

        String getLoginDateString() {
            return getDateString(_loginDate);
        }

        String getDateString(String dateFormat) {
            if (null != dateFormat) {
                return _seirekiFlg ?
                        dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat):
                            KNJ_EditDate.h_format_JP(dateFormat) ;
            }
            return "";
        }

        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        private String getNameMst(DB2UDB db2, String namecd1,String namecd2) {

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug(ex);
            }

            return name;
        }

        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantDiv)) certifKindCd = "111";
            if ("2".equals(_applicantDiv)) certifKindCd = "112";
            if (certifKindCd == null) return null;

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error(ex);
            }

            return name;
        }

        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode) || isCollege();
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}

// eof
