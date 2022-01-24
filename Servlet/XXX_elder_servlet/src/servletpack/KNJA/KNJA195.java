// kanji=漢字
/*
 * $Id: b2f54fa74c70ed434818bf93c29db149787439f4 $
 *
 * 作成日: 2005/07/02 17:20:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 学校教育システム 賢者 ＜ＫＮＪＡ１９５＞ 
 */

public class KNJA195 {

    private static final Log log = LogFactory.getLog(KNJA195.class);

    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {

        Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス

        try {
            // ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = createParam(db2, request);

            // print設定
            response.setContentType("application/pdf");

            // svf設定
            svf.VrInit(); // クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); // PDFファイル名の設定

            // ＳＶＦ作成処理
            boolean nonedata = false; // 該当データなしフラグ

            // SVF出力
            if (setSvfMain(db2, svf)) {
                nonedata = true; // 帳票出力のメソッド
            }

            // 該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            // 終了処理
            svf.VrQuit();
            db2.commit();
            db2.close(); // DBを閉じる
        }

    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    /**
     * svf print 印刷処理
     */
    private boolean setSvfMain(DB2UDB db2, Vrw32alp svf) throws SQLException {
        boolean nonedata = false;
        svf.VrSetForm("KNJA195.frm", 1); // セットフォーム

        final List printDataList = getOrderList(db2);
        final Map printDataMap = new HashMap();
        setPrintData(db2, printDataMap, "STUDENT");
        setPrintData(db2, printDataMap, "GUARD");
        setPrintData(db2, printDataMap, "FUTAN");
        setPrintData(db2, printDataMap, "SONOTA1");
        setPrintData(db2, printDataMap, "SONOTA2");

        for (final Iterator it = printDataList.iterator(); it.hasNext();) {
            final String printMapKey = (String) it.next();
            final PrintData printData = (PrintData) printDataMap.get(printMapKey);

            // 明細
            PrintInfo printInfo = null;
            if ("1".equals(_param._output)) {
                printInfo = printData._guardInfo;
            } else if ("3".equals(_param._output)) {
                printInfo = printData._futanInfo;
            } else if ("4".equals(_param._output)) {
                printInfo = printData._sonotaInfo1;
            } else if ("5".equals(_param._output)) {
                printInfo = printData._sonotaInfo2;
            } else if ("2".equals(_param._output)) {
                printInfo = printData._studentInfo;
            }

            if (null != printInfo) {
                log.debug(" schregno = " + printMapKey);
                
                if (!StringUtils.isBlank(printInfo._zipCd)) {
                    svf.VrsOut("ZIPCODE", "〒" + printInfo._zipCd); // 郵便番号
                }
                if (getMS932ByteLength(printInfo._addr1) > 40 || getMS932ByteLength(printInfo._addr2) > 40) {
                    svf.VrsOut("ADDR1_2", printInfo._addr1); // 住所1
                    svf.VrsOut("ADDR2_2", printInfo._addr2); // 住所2
                } else {
                    svf.VrsOut("ADDR1", printInfo._addr1); // 住所1
                    svf.VrsOut("ADDR2", printInfo._addr2); // 住所2
                }
//                if ("3".equals(_param._output) || "4".equals(_param._output) || "1".equals(_param._printSchregno)) {
//                    svf.VrsOut("SCHREGNO", printData._schregNo); // 学籍番号
//                }
                if ("1".equals(_param._printSchregName)) {
                    final String name1 = StringUtils.isBlank(printInfo._name) ? "" : printInfo._name + "　様";
                    final String name2 = (null == printData._studentInfo) ? "" : StringUtils.isBlank(printData._studentInfo._name) ? "" : printData._studentInfo._name + "　様";
                    final int checkLen = Math.max(getMS932ByteLength(name1), getMS932ByteLength(name2));
                    svf.VrsOut("NAME1" + (checkLen > 26 ? "_2" : ""), name1);
                    svf.VrsOut("NAME2" + (checkLen > 26 ? "_2" : ""), name2);
                } else {
                    final String name = StringUtils.isBlank(printInfo._name) ? "" : printInfo._name + "　様";
                    final int checkLen = getMS932ByteLength(name);
                    svf.VrsOut("NAME1" + (checkLen > 26 ? "_2" : ""), name);
                }
                
                final String hrname = StringUtils.defaultString(printData._hrname);
                final String attendno = NumberUtils.isDigits(printData._attendNo) ? String.valueOf(Integer.parseInt(printData._attendNo)) + "番": StringUtils.defaultString(printData._attendNo);
                if (!StringUtils.isBlank(hrname + attendno)) {
                    svf.VrsOut("HR_NAME", "（" + hrname + " " + attendno + "）"); // 年組番号
                }

                nonedata = true;
                svf.VrEndPage();
            }
        }
        return nonedata;
    }

    private List getOrderList(
            final DB2UDB db2
    ) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = null;
        try {
            sql = getStudentInfoSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                retList.add(rs.getString("SCHREGNO"));
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private void setPrintData(
            final DB2UDB db2,
            final Map printMap,
            final String setDiv
    ) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = null;
            if ("STUDENT".equals(setDiv)) {
                sql = getStudentInfoSql();
            } else if ("GUARD".equals(setDiv)) {
                sql = getGuardInfoSql();
            } else if ("FUTAN".equals(setDiv)) {
                sql = getFutanInfoSql();
            } else if ("SONOTA1".equals(setDiv)) {
                sql = getSonotaInfoSql("1");
            } else if ("SONOTA2".equals(setDiv)) {
                sql = getSonotaInfoSql("2");
            }
            if (null == sql) {
                log.warn(" sql null");
                return;
            }
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String hrname = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final PrintInfo printInfo = new PrintInfo(rs.getString("NAME"), rs.getString("ZIPCD"), rs.getString("ADDR1"), rs.getString("ADDR2"));
                if (!printMap.containsKey(schregNo)) {
                    final PrintData printData = new PrintData(schregNo, hrname, attendNo);
                    printMap.put(schregNo, printData);
                }
                final PrintData printData = (PrintData) printMap.get(schregNo);
                if ("STUDENT".equals(setDiv)) {
                    printData._studentInfo = printInfo;
                } else if ("GUARD".equals(setDiv)) {
                    printData._guardInfo = printInfo;
                } else if ("FUTAN".equals(setDiv)) {
                    printData._futanInfo = printInfo;
                } else if ("SONOTA1".equals(setDiv)) {
                    printData._sonotaInfo1 = printInfo;
                } else if ("SONOTA2".equals(setDiv)) {
                    printData._sonotaInfo2 = printInfo;
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** 生徒住所データ 取得* */
    private String getStudentInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH ADTABLE AS ( ");
        stb.append("SELECT ");
        stb.append("    SCHREGNO AS SCH,MAX(ISSUEDATE) AS ISSUE ");
        stb.append("FROM ");
        stb.append("    SCHREG_ADDRESS_DAT ");
        stb.append("WHERE ");
        stb.append("    SCHREGNO IN " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        stb.append("GROUP BY ");
        stb.append("    SCHREGNO ");
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,REGDH.HR_NAME,t3.ATTENDNO,t2.ZIPCD,t2.ADDR1,t2.ADDR2,t1.NAME ");
        stb.append("FROM ");
        stb.append("    SCHREG_BASE_MST t1 ");
        stb.append("    LEFT JOIN ADTABLE AD1 ON t1.SCHREGNO = AD1.SCH ");
        stb.append("    LEFT JOIN SCHREG_ADDRESS_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("         AND t2.ISSUEDATE = AD1.ISSUE ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t3 ON t1.SCHREGNO = t3.SCHREGNO ");
        stb.append("         AND t3.YEAR = '" + _param._year + "' ");
        stb.append("         AND t3.SEMESTER = '" + _param._semester + "' ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = t3.YEAR AND REGDH.SEMESTER = t3.SEMESTER AND REGDH.GRADE = t3.GRADE AND REGDH.HR_CLASS = t3.HR_CLASS ");
        stb.append("WHERE ");
        stb.append("    t1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        if ("1".equals(_param._grdDiv)) {
            stb.append(" AND NOT ((T1.GRD_DIV IS NOT NULL AND T1.GRD_DIV <> '4') AND T1.GRD_DATE < '" + _param._ctrlDate + "' ) ");
        }
        stb.append("ORDER BY ");
        if ("2".equals(_param._output2)) {
            stb.append("t3.GRADE, t3.HR_CLASS, t3.ATTENDNO, t1.SCHREGNO");
        } else {
            stb.append("t1.SCHREGNO");
        }
        return stb.toString();

    }

    /** 保護者住所データ 取得* */
    private String getGuardInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH ADTABLE AS ( ");
        stb.append("SELECT ");
        stb.append("    SCHREGNO AS SCH,MAX(RELATIONSHIP) AS RELA ");
        stb.append("FROM ");
        stb.append("    GUARDIAN_DAT ");
        stb.append("WHERE ");
        stb.append("    SCHREGNO in " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        stb.append("GROUP BY ");
        stb.append("    SCHREGNO ");
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,REGDH.HR_NAME,t2.ATTENDNO,GUARD_NAME as NAME,GUARD_ZIPCD as ZIPCD,GUARD_ADDR1 as ADDR1,GUARD_ADDR2 as ADDR2 ");
        stb.append("FROM ");
        stb.append("    GUARDIAN_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + _param._year + "' ");
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "' ");
        stb.append("    INNER JOIN ADTABLE ON t1.SCHREGNO = SCH AND RELATIONSHIP = RELA ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = t2.YEAR AND REGDH.SEMESTER = t2.SEMESTER AND REGDH.GRADE = t2.GRADE AND REGDH.HR_CLASS = t2.HR_CLASS ");
        stb.append("ORDER BY ");
        if ("2".equals(_param._output2)) {
            stb.append("t2.GRADE, t2.HR_CLASS, t2.ATTENDNO, t1.SCHREGNO");
        } else {
            stb.append("t1.SCHREGNO");
        }
        return stb.toString();

    }

    /** 負担者住所データ 取得* */
    private String getFutanInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH ADTABLE AS ( ");
        stb.append("SELECT ");
        stb.append("    SCHREGNO AS SCH,MAX(RELATIONSHIP) AS RELA ");
        stb.append("FROM ");
        stb.append("    GUARDIAN_DAT ");
        stb.append("WHERE ");
        stb.append("    SCHREGNO in " + SQLUtils.whereIn(true, _param._categoryName) + " ");
        stb.append("GROUP BY ");
        stb.append("    SCHREGNO ");
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,REGDH.HR_NAME,t2.ATTENDNO,GUARANTOR_NAME as NAME,GUARANTOR_ZIPCD as ZIPCD,GUARANTOR_ADDR1 as ADDR1,GUARANTOR_ADDR2 as ADDR2 ");
        stb.append("FROM ");
        stb.append("    GUARDIAN_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + _param._year + "' ");
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "' ");
        stb.append("    INNER JOIN ADTABLE ON t1.SCHREGNO = SCH AND RELATIONSHIP = RELA ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = t2.YEAR AND REGDH.SEMESTER = t2.SEMESTER AND REGDH.GRADE = t2.GRADE AND REGDH.HR_CLASS = t2.HR_CLASS ");
        stb.append("ORDER BY ");
        if ("2".equals(_param._output2)) {
            stb.append("t2.GRADE, t2.HR_CLASS, t2.ATTENDNO, t1.SCHREGNO");
        } else {
            stb.append("t1.SCHREGNO");
        }
        return stb.toString();
    }

    /** その他住所データ1 取得* */
    private String getSonotaInfoSql(final String div) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,REGDH.HR_NAME,t2.ATTENDNO,SEND_NAME as NAME,SEND_ZIPCD as ZIPCD,SEND_ADDR1 as ADDR1,SEND_ADDR2 as ADDR2 ");
        stb.append("FROM ");
        stb.append("    SCHREG_SEND_ADDRESS_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + _param._year + "' ");
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "' ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = t2.YEAR AND REGDH.SEMESTER = t2.SEMESTER AND REGDH.GRADE = t2.GRADE AND REGDH.HR_CLASS = t2.HR_CLASS ");
        stb.append("WHERE ");
        stb.append("    t1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categoryName) + " AND t1.DIV = '" + div + "' ");
        stb.append("ORDER BY ");
        if ("2".equals(_param._output2)) {
            stb.append("t2.GRADE, t2.HR_CLASS, t2.ATTENDNO, t1.SCHREGNO");
        } else {
            stb.append("t1.SCHREGNO");
        }
        return stb.toString();

    }// preStat2()の括り

    private class PrintData implements Comparable {
        final String _schregNo;
        final String _hrname;
        final String _attendNo;
        PrintInfo _studentInfo;
        PrintInfo _guardInfo;
        PrintInfo _futanInfo;
        PrintInfo _sonotaInfo1;
        PrintInfo _sonotaInfo2;

        public PrintData(
                final String schregNo,
                final String hrname,
                final String attendNo
        ) {
            _schregNo = schregNo;
            _hrname = hrname;
            _attendNo = attendNo;
        }

        public int compareTo(Object o) {
            if (!(o instanceof PrintData)) {
                return -1;
            }
            final PrintData that = (PrintData) o;
            return _attendNo.compareTo(that._attendNo);
        }
    }

    private class PrintInfo {
        final String _name;
        final String _zipCd;
        final String _addr1;
        final String _addr2;

        public PrintInfo(
                final String name,
                final String zipCd,
                final String addr1,
                final String addr2
        ) {
            _name = StringUtils.defaultString(name);
            _zipCd = StringUtils.defaultString(zipCd);
            _addr1 = StringUtils.defaultString(addr1);
            _addr2 = StringUtils.defaultString(addr2);
        }
        
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _output;
        final String _output2;
//        final String _printSchregno;
        final String _printSchregName;
        final String _grdDiv;
        final String _ctrlDate;
        final String[] _categoryName;

        String _schoolName;
        String _schoolZipCd;
        String _schoolAddr1;
        String _schoolAddr2;
        String _postOffice;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _output = request.getParameter("OUTPUT");
//            _printSchregno = request.getParameter("CHECK2");    // 学籍番号印刷
            _printSchregName = request.getParameter("CHECK1");  // 生徒名出力
            _output2 = request.getParameter("OUTPUT2");         // 出力順
            _grdDiv = request.getParameter("GRDDIV");           // 出力条件
            _ctrlDate = request.getParameter("CTRL_DATE");      // 日付
            final String[] categoryName = request.getParameterValues("category_name");
            _categoryName = new String[categoryName.length]; // 学年・組
            for (int i = 0; i < _categoryName.length; i++) {
                _categoryName[i] = StringUtils.split(categoryName[i], "-")[0];
            }

            setSchoolInfo(db2, _year);
            setPostOffice(db2);
        }

        private void setSchoolInfo(final DB2UDB db2, final String year) throws SQLException {
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT ");
                stb.append("    SCHOOLZIPCD,SCHOOLADDR1,SCHOOLADDR2,SCHOOLNAME1 ");
                stb.append("FROM ");
                stb.append("    SCHOOL_MST ");
                stb.append("WHERE ");
                stb.append("    YEAR = '" + year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                _schoolName = "";
                _schoolZipCd = "";
                _schoolAddr1 = "";
                _schoolAddr2 = "";
                if (rs.next()) {
                    _schoolName = StringUtils.defaultString(rs.getString("SCHOOLNAME1"));
                    _schoolZipCd = StringUtils.defaultString(rs.getString("SCHOOLZIPCD"));
                    _schoolAddr1 = StringUtils.defaultString(rs.getString("SCHOOLADDR1"));
                    _schoolAddr2 = StringUtils.defaultString(rs.getString("SCHOOLADDR2"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setPostOffice(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'M005' AND NAMECD2 = '1'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                _postOffice = "";
                if (rs.next()) {
                    _postOffice = StringUtils.defaultString(rs.getString("NAME1"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}// クラスの括り
