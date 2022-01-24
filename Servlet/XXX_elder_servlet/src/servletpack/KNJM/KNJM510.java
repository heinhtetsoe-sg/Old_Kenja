// kanji=漢字
/*
 * $Id: ef31070a45079babcb284bba7562bb1e7ec30a36 $
 *
 * 作成日: 2005/07/02 17:20:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [通信制] ＜ＫＮＪＭ５１０＞ 宛名印刷(通信制) 2005/07/02 m-yama 作成日 2006/06/07 m-yama
 * NO001 出力順を番号順に変更
 */

public class KNJM510 {

    private static final Log log = LogFactory.getLog(KNJM510.class);

    Param _param;

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
        try {
            svf.VrSetForm("KNJM510.frm", 1); // セットフォーム

            final Map printDataMap = getPrintData(db2);

            final List attendSorted = new ArrayList(printDataMap.values());
            Collections.sort(attendSorted);

            for (final Iterator iter = attendSorted.iterator(); iter.hasNext();) {
                final PrintData printData = (PrintData) iter.next();

                // ヘッダ
                svf.VrsOut("SCHOOLZIP", _param._schoolZipCd); // 学校郵便番号
                svf.VrsOut("SCHOOLADDRESS1", _param._schoolAddr1); // 学校住所１
                svf.VrsOut("SCHOOLADDRESS2", _param._schoolAddr2); // 学校住所２
                svf.VrsOut("SCHOOLNAME", _param._schoolName); // 学校名

                // 明細
                PrintInfo printInfo = _param._output.equals("2") ? printData._guardInfo : _param._output.equals("4") ? printData._sonotaInfo : printData._studentInfo;
                svf.VrsOut("POSTOFFICE", _param._postOffice); // 郵便局
                svf.VrsOut("POSTPAID", "料金別納"); // 料金支払い区分
                String nameField = "";
                if (null != printInfo) {
                    svf.VrsOut("ZIPCODE", printInfo._zipCd); // 郵便番号
                    if ("1".equals(_param._useAddrField2) && (getMS932ByteLength(printInfo._addr1) > 50 || getMS932ByteLength(printInfo._addr2) > 50)) {
                        svf.VrsOut("ADDRESS1_2", printInfo._addr1); // 住所1
                        svf.VrsOut("ADDRESS2_2", printInfo._addr2); // 住所2
                    } else {
                        svf.VrsOut("ADDRESS1", printInfo._addr1); // 住所1
                        svf.VrsOut("ADDRESS2", printInfo._addr2); // 住所2
                    }
                    final String setName = printInfo._name + "　様";
                    nameField = getMS932ByteLength(setName) > 38 ? "NAME1_2" : "NAME1";
                    svf.VrsOut(nameField, setName);     //生徒名１
                    svf.VrsOut("SCHREGNO", printData._schregNo); // 学籍番号
                }
                if (_param._output.equals("3")) {
                    final String name2 = null == printData._studentInfo ? "" : printData._studentInfo._name;
                    final String name1 = null == printData._guardInfo ? "" : printData._guardInfo._name;
                    final String setName2 = name2 + "　様";
                    final String setName1 = name1 + "　様";
                    final String checkField = setName1.length() > setName2.length() ? setName1 : setName2;
                    final String setField = KNJ_EditEdit.setformatArea("", checkField, 19, "", "_2");
                    svf.VrsOut(nameField, "");
                    svf.VrsOut("NAME2" + setField, setName2); // 上段
                    svf.VrsOut("NAME1" + setField, setName1); // 下段
                }
                nonedata = true;
                svf.VrEndPage();
            }
        } finally {
            db2.commit();
        }
        return nonedata;
    }

    private Map getPrintData(DB2UDB db2) throws SQLException {
        Map retMap = new HashMap();
        ResultSet rsStudent = null;
        ResultSet rsGuard = null;
        try {
            final String studentInfoSql = getStudentInfoSql();
            retMap = setPrintData(db2, retMap, studentInfoSql, "STUDENT");

            final String guardInfoSql = getGuardInfoSql();
            retMap = setPrintData(db2, retMap, guardInfoSql, "GUARD");

            final String sonotaInfoSql = getSonotaInfoSql();
            retMap = setPrintData(db2, retMap, sonotaInfoSql, "SONOTA");

        } finally {
            DbUtils.closeQuietly(rsStudent);
            DbUtils.closeQuietly(rsGuard);
            db2.commit();
        }
        return retMap;
    }

    private Map setPrintData(
            final DB2UDB db2,
            final Map printMap,
            final String studentInfoSql,
            final String setDiv
    ) throws SQLException {
        final Map retMap = printMap;
        ResultSet rs = null;
        try {
            db2.query(studentInfoSql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String attendNo = rs.getString("ATTENDNO");
                final PrintInfo printInfo = new PrintInfo(rs.getString("NAME"), rs.getString("ZIPCD"), rs.getString("ADDR1"), rs.getString("ADDR2"));
                if (!retMap.containsKey(schregNo)) {
                    final PrintData printData = new PrintData(schregNo, attendNo);
                    retMap.put(schregNo, printData);
                }
                final PrintData printData = (PrintData) retMap.get(schregNo);
                if (setDiv.equals("STUDENT")) {
                    printData.setStudentInfo(printInfo);
                } else if (setDiv.equals("GUARD")) {
                    printData.setGuardInfo(printInfo);
                } else {
                    printData.setSonotaInfo(printInfo);
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return retMap;
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
        stb.append("    SCHREGNO in " + _param._studentInState + " ");
        stb.append("GROUP BY ");
        stb.append("    SCHREGNO ");
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,t3.ATTENDNO,t1.ZIPCD,t1.ADDR1,t1.ADDR2,t2.NAME ");
        stb.append("FROM ");
        stb.append("    SCHREG_ADDRESS_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t3 ON t1.SCHREGNO = t3.SCHREGNO "); // NO001
        stb.append("    AND t3.YEAR = '" + _param._year + "' "); // NO001
        stb.append("    AND t3.SEMESTER = '" + _param._semester + "', "); // NO001
        stb.append("    ADTABLE ");
        stb.append("WHERE ");
        stb.append("    t1.SCHREGNO = SCH ");
        stb.append("    AND ISSUEDATE = ISSUE ");
        stb.append("ORDER BY ");
        stb.append("    t3.ATTENDNO ");

        log.debug(stb);

        return stb.toString();

    }// preStat1()の括り

    /** 保護者住所データ 取得* */
    private String getGuardInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH ADTABLE AS ( ");
        stb.append("SELECT ");
        stb.append("    SCHREGNO AS SCH,MAX(RELATIONSHIP) AS RELA ");
        stb.append("FROM ");
        stb.append("    GUARDIAN_DAT ");
        stb.append("WHERE ");
        stb.append("    SCHREGNO in " + _param._studentInState + " ");
        stb.append("GROUP BY ");
        stb.append("    SCHREGNO ");
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,t2.ATTENDNO,GUARD_NAME as NAME,GUARD_ZIPCD as ZIPCD,GUARD_ADDR1 as ADDR1,GUARD_ADDR2 as ADDR2 ");
        stb.append("FROM ");
        stb.append("    GUARDIAN_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO "); // NO001
        stb.append("    AND t2.YEAR = '" + _param._year + "' "); // NO001
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "', "); // NO001
        stb.append("    ADTABLE ");
        stb.append("WHERE ");
        stb.append("    t1.SCHREGNO = SCH ");
        stb.append("    AND RELATIONSHIP = RELA ");
        stb.append("ORDER BY ");
        stb.append("    t2.ATTENDNO ");

        log.debug(stb);

        return stb.toString();

    }// preStat2()の括り

    /** その他住所データ 取得* */
    private String getSonotaInfoSql() {
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    t1.SCHREGNO,t2.ATTENDNO,SEND_NAME as NAME,SEND_ZIPCD as ZIPCD,SEND_ADDR1 as ADDR1,SEND_ADDR2 as ADDR2 ");
        stb.append("FROM ");
        stb.append("    SCHREG_SEND_ADDRESS_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + _param._year + "' ");
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "' ");
        stb.append("WHERE ");
        stb.append("    t1.SCHREGNO IN " + _param._studentInState + " AND t1.DIV = '1' ");
        stb.append("ORDER BY ");
        stb.append("    t2.ATTENDNO ");

        log.debug(stb);

        return stb.toString();

    }// preStat2()の括り

    private class PrintData implements Comparable {
        final String _schregNo;
        final String _attendNo;
        PrintInfo _studentInfo;
        PrintInfo _guardInfo;
        PrintInfo _sonotaInfo;
        final String _postPaid;

        public PrintData(
                final String schregNo,
                final String attendNo
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _postPaid = "料金別納";
        }

        public void setStudentInfo(final PrintInfo studentInfo) {
            _studentInfo = studentInfo;
        }

        public void setGuardInfo(final PrintInfo guardInfo) {
            _guardInfo = guardInfo;
        }

        public void setSonotaInfo(final PrintInfo sonotaInfo) {
            _sonotaInfo = sonotaInfo;
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
            _name = name == null ? "" : name;
            _zipCd = zipCd == null ? "" : zipCd;
            _addr1 = addr1 == null ? "" : addr1;
            _addr2 = addr2 == null ? "" : addr2;
        }
        
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _output;
        final String _studentInState;
        final String _useAddrField2;

        String _schoolName;
        String _schoolZipCd;
        String _schoolAddr1;
        String _schoolAddr2;
        String _postOffice;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _output = request.getParameter("OUTPUT");

            final String students[] = request.getParameterValues("category_name"); // 学年・組
            String studentInState = "( ";
            String sep = "";
            for (int ia = 0; ia < students.length; ia++) {
                studentInState += sep + "'" + students[ia] + "'";
                sep = ", ";
            }
            _studentInState = studentInState + " )";
            _useAddrField2 = request.getParameter("useAddrField2");

            setSchoolInfo(db2, _year);
            setPostOffice(db2);
        }

        private void setSchoolInfo(final DB2UDB db2, final String year) throws SQLException {
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                StringBuffer stb = new StringBuffer();
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
                    _schoolName = rs.getString("SCHOOLNAME1") == null ? "" : rs.getString("SCHOOLNAME1");
                    _schoolZipCd = rs.getString("SCHOOLZIPCD") == null ? "" : rs.getString("SCHOOLZIPCD");
                    _schoolAddr1 = rs.getString("SCHOOLADDR1") == null ? "" : rs.getString("SCHOOLADDR1");
                    _schoolAddr2 = rs.getString("SCHOOLADDR2") == null ? "" : rs.getString("SCHOOLADDR2");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setPostOffice(final DB2UDB db2) throws SQLException {
            ResultSet rs = null;
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'M005' AND NAMECD2 = '1'";
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                _postOffice = "";
                if (rs.next()) {
                    _postOffice = rs.getString("NAME1") == null ? "" : rs.getString("NAME1");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
    }
}// クラスの括り
