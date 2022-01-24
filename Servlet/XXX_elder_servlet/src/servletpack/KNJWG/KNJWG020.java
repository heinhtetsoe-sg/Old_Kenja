// kanji=漢字
/*
 * $Id: d2997d0be4cf8556abac7a68650cbbc79e0f48fb $
 *
 * 作成日: 
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.util.Enumeration;

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

import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/**
 * 学校教育システム 賢者 [事務管理] 証明書交付台帳
 */
public class KNJWG020 {

    private static final Log log = LogFactory.getLog(KNJWG020.class);

    private static final String CERTIFALL = "000";
    private static final String FORM020_1 = "KNJWG020_1.frm";
    private static final String FORM020_2 = "KNJWG020_2.frm";
    private static final String FORM020_3 = "KNJWG020_3.frm";
    private static final String FORM020_4 = "KNJWG020_4.frm";

    Param _param;

    /**
     * KNJD.classから最初に起動されるクラス
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; // Databaseクラスを継承したクラス
        boolean nonedata = false; // 該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }

        // パラメータの取得
        _param = createParam(db2, request);

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // 印刷処理
        nonedata = printSvf(db2, svf);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    /**
     * 印刷処理
     */
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean nonedata = false; // 該当データなしフラグ
        printsvfHead(svf);
        if (_param._form.formPrintOut(svf, db2)) {
            nonedata = true;
        }
        return nonedata;
    }

    /**
     * SVF-FORM 印刷処理
     */
    private void printsvfHead(final Vrw32alp svf) {
        svf.VrSetForm(_param._form.getName(), _param._form.getType());
        svf.VrsOut("NENDO", _param.changePrintYear(_param._year)); // 年度
        svf.VrsOut("SCHOOLNAME", _param._schoolName); // 学校名称
        if (null != _param._page) {
            svf.VrSetPageCount(Integer.parseInt(_param._page), 1); // ページ初期値
        }
    }

    /**
     * SVF-FORM 印刷処理ページ設定 NO002 Build
     */
    private void printsvfPageHead(final Vrw32alp svf, final String pageNum) {
        if (null == _param._page) {
            svf.VrSetPageCount(Integer.parseInt(pageNum), 1); // ページ初期値
        }
        svf.VrsOut("SUBTITLE", "(" + _param._certifKindName + ")");
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

    /** フォームクラス */
    abstract class Form {
        private String _name;
        private int _type;

        void setName(final String name) {
            _name = name;
        }

        void setType(final int type) {
            _type = type;
        }

        String getName() {
            return _name;
        }

        int getType() {
            return _type;
        }

        String selectSql() {
            StringBuffer stb = new StringBuffer();

            // 証明書交付台帳の表
            stb.append("WITH CERTIF_DATA AS ( ");
            stb.append(" SELECT ");
            stb.append("    T1.CERTIF_INDEX, ");
            stb.append("    T1.CERTIF_NO, ");
            stb.append("    T1.CERTIF_KINDCD, ");
            stb.append("    T1.ISSUEDATE, ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.APPLICANTNO, ");
            stb.append("    T1.GRADUATE_FLG ");
            stb.append(" FROM ");
            stb.append("    CERTIF_ISSUE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.ISSUECD = '1' ");
            stb.append("    AND T1.YEAR ='" + _param._year2 + "' ");
            if (!_param._certifKind.equals(CERTIFALL)) {
                stb.append("AND T1.CERTIF_KINDCD = '" + _param._certifKind + "' ");
            } else {
                if (_param._type.equals("1")) {
                    stb.append("AND T1.TYPE IS NULL ");
                } else if (_param._type.equals("2")) {
                    stb.append("AND T1.TYPE = '3' ");
                } else if (_param._type.equals("3")) {
                    stb.append("AND T1.TYPE = '2' ");
                } else {
                    stb.append("AND T1.TYPE = '9' ");
                }
            }
            stb.append(" ) ");

            // 学籍の表
            stb.append(" , SCHREG_DATA AS ( ");
            stb.append(" SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    NAME, ");
            stb.append("    GRD_DATE ");
            stb.append(" FROM ");
            stb.append("    SCHREG_BASE_MST W1 ");
            stb.append(" WHERE ");
            stb.append("    EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
            stb.append(" ) ");

            stb.append(" , SCHREG_ADDR AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ISSUEDATE, ");
            stb.append("     T1.PREF_CD, ");
            stb.append("     L1.PREF_NAME, ");
            stb.append("     VALUE(T1.ADDR1, '') AS ADDR1, ");
            stb.append("     VALUE(T1.ADDR2, '') AS ADDR2, ");
            stb.append("     VALUE(T1.ADDR3, '') AS ADDR3 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ADDRESS_DAT T1 ");
            stb.append("     LEFT JOIN PREF_MST L1 ON T1.PREF_CD = L1.PREF_CD, ");
            stb.append("     (SELECT ");
            stb.append("          F1.SCHREGNO, ");
            stb.append("          MAX(F1.ISSUEDATE) AS ISSUEDATE ");
            stb.append("      FROM ");
            stb.append("          SCHREG_ADDRESS_DAT F1 ");
            stb.append("      GROUP BY ");
            stb.append("          F1.SCHREGNO ");
            stb.append("     ) T2 ");
            stb.append(" WHERE ");
            stb.append("     EXISTS(SELECT 'X' FROM SCHREG_DATA E1 WHERE T1.SCHREGNO = E1.SCHREGNO) ");
            stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T1.ISSUEDATE = T2.ISSUEDATE ");
            stb.append(" ) ");

            // 対象学籍の証明書交付台帳の表
            // 証明書交付台帳CERTIF_ISSUE_DATの学籍番号の妥当性チェックを行う
            stb.append(" , DATA AS ( ");
            stb.append(" SELECT ");
            if (_param._order.equals("2")) {
                stb.append("    ROW_NUMBER() OVER (ORDER BY W1.CERTIF_NO) AS NUMBER, ");
            } else {
                stb.append("    ROW_NUMBER() OVER (ORDER BY W1.ISSUEDATE, W1.CERTIF_NO) AS NUMBER, ");
            }
            stb.append("    W1.CERTIF_INDEX, ");
            stb.append("    W1.CERTIF_NO, ");
            stb.append("    W1.CERTIF_KINDCD, ");
            stb.append("    W1.ISSUEDATE, ");
            stb.append("    W1.SCHREGNO, ");
            stb.append("    W1.APPLICANTNO, ");
            stb.append("    W1.GRADUATE_FLG ");
            stb.append(" FROM ");
            stb.append("    CERTIF_DATA W1 ");
            stb.append(") ");

            // メイン表
            stb.append(" SELECT ");
            stb.append("    T1.CERTIF_NO, ");
            stb.append("    CASE WHEN T1.SCHREGNO IS NOT NULL ");
            stb.append("         THEN T1.SCHREGNO ");
            stb.append("         ELSE T1.APPLICANTNO ");
            stb.append("    END AS SCHREGNO, ");
            stb.append("    T1.ISSUEDATE, ");
            stb.append("    T1.CERTIF_KINDCD, ");
            stb.append("    L1.KINDNAME, ");
            stb.append("    CASE WHEN T1.SCHREGNO IS NOT NULL ");
            stb.append("         THEN SCH.NAME ");
            stb.append("         ELSE APP.NAME ");
            stb.append("    END AS NAME, ");
            stb.append("    L2.REMARK1, ");
            stb.append("    L2.REMARK2, ");
            stb.append("    L2.REMARK3, ");
            stb.append("    L2.REMARK4, ");
            stb.append("    L2.REMARK5, ");
            stb.append("    L2.REMARK6, ");
            stb.append("    L2.REMARK7, ");
            stb.append("    L3.NAME AS HIGH_SCHOOL_NAME, ");
            stb.append("    CASE MOD(T1.NUMBER, 20) WHEN 0 THEN T1.NUMBER/20 ELSE T1.NUMBER/20 + 1 END PAGENUM, ");
            stb.append("    ADDR.ISSUEDATE, ");
            stb.append("    ADDR.PREF_CD, ");
            stb.append("    ADDR.PREF_NAME, ");
            stb.append("    ADDR.ADDR1, ");
            stb.append("    ADDR.ADDR2, ");
            stb.append("    ADDR.ADDR3 ");
            stb.append(" FROM ");
            stb.append("    DATA T1 ");
            stb.append("    LEFT JOIN SCHREG_DATA SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN APPLICANT_BASE_MST APP ON APP.APPLICANTNO = T1.APPLICANTNO ");
            stb.append("    LEFT JOIN CERTIF_SCHOOL_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
            stb.append("         AND L1.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
            stb.append("    LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT L2 ON L2.YEAR = '" + _param._year + "' ");
            stb.append("         AND L2.CERTIF_INDEX = T1.CERTIF_INDEX ");
            stb.append("    LEFT JOIN FIN_HIGH_SCHOOL_MST L3 ON L3.SCHOOL_CD = L2.REMARK1 ");
            stb.append("    LEFT JOIN SCHREG_ADDR ADDR ON ADDR.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("    NUMBER > (SELECT ");
            stb.append("                  MIN(NUMBER) - CASE MOD(MIN(NUMBER),20) WHEN 0 THEN 20 ELSE MOD(MIN(NUMBER),20) END ");
            stb.append("              FROM ");
            stb.append("                  DATA ");
            stb.append("              WHERE ");
            stb.append("                  CHAR(ISSUEDATE) >= '" + _param._date.replace('/', '-') + "') ");
            // 発行番号順
            if (_param._order.equals("2")) {
                stb.append(" ORDER BY T1.CERTIF_NO ");
            } else {
                stb.append(" ORDER BY ");
                stb.append("    T1.ISSUEDATE, ");
                stb.append("    T1.CERTIF_NO");
            }
            log.debug(stb);
            return stb.toString();
        }

        abstract boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException;
    }

    /** フォーム020_1 */
    private class Form020_1 extends Form {
        public Form020_1() {
            setName(FORM020_1);
            setType(4);
        }

        boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
            boolean nonedata = false;
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            try {
                ps1 = db2.prepareStatement(_param._form.selectSql());
                rs = ps1.executeQuery();
                while (rs.next()) {
                    if (!nonedata) {
                        printsvfPageHead(svf, rs.getString("PAGENUM"));
                    }
                    if (rs.getString("CERTIF_NO") != null) {
                        svf.VrsOut("INDEX", rs.getString("CERTIF_NO")); // 発行番号
                    }
                    svf.VrsOut("DATE", _param.changePrintDate(rs.getString("ISSUEDATE"))); // 発行年月日
                    svf.VrsOut("CERTIF_KIND", rs.getString("KINDNAME")); // 証明書の種類
                    svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                    if (null != rs.getString("NAME")) {
                        final String nameField = rs.getString("NAME").length() > 10 ? "2_1" : "1";
                        svf.VrsOut("NAME" + nameField, rs.getString("NAME")); // 氏名
                    }
                    svf.VrEndRecord();
                    nonedata = true;
                }
                rs.close();
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
            return nonedata;
        }

    }

    /** フォーム020_2 */
    private class Form020_2 extends Form {
        public Form020_2() {
            setName(FORM020_2);
            setType(4);
        }

        boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
            boolean nonedata = false;
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            try {
                ps1 = db2.prepareStatement(_param._form.selectSql());
                rs = ps1.executeQuery();
                while (rs.next()) {
                    if (!nonedata) {
                        printsvfPageHead(svf, rs.getString("PAGENUM"));
                    }
                    if (rs.getString("CERTIF_NO") != null) {
                        svf.VrsOut("INDEX", rs.getString("CERTIF_NO")); // 発行番号
                    }
                    svf.VrsOut("DATE", _param.changePrintDate(rs.getString("ISSUEDATE"))); // 発行年月日
                    svf.VrsOut("CERTIF_KIND", rs.getString("KINDNAME")); // 証明書の種類
                    svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                    if (null != rs.getString("NAME")) {
                        final String nameField = rs.getString("NAME").length() > 10 ? "2_1" : "1";
                        svf.VrsOut("NAME" + nameField, rs.getString("NAME")); // 氏名
                    }
                    if (null != rs.getString("REMARK4") && null != rs.getString("REMARK5")) {
                        svf.VrsOut("REMARK1", rs.getString("REMARK4") + "\uFF5E" + rs.getString("REMARK5"));
                    }
                    if (null != rs.getString("REMARK1") && null != rs.getString("REMARK2")) {
                        svf.VrsOut("REMARK2", rs.getString("REMARK1") + "\uFF5E" + rs.getString("REMARK2"));
                    }
                    svf.VrEndRecord();
                    nonedata = true;
                }
                rs.close();
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
            return nonedata;
        }

    }

    /** フォーム020_3 */
    private class Form020_3 extends Form {
        public Form020_3() {
            setName(FORM020_3);
            setType(4);
        }

        String selectSql(final String stbCase, final Param param) {
            return null;
        }

        boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
            boolean nonedata = false;
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            try {
                ps1 = db2.prepareStatement(_param._form.selectSql());
                rs = ps1.executeQuery();
                while (rs.next()) {
                    if (!nonedata) {
                        printsvfPageHead(svf, rs.getString("PAGENUM"));
                    }
                    if (rs.getString("CERTIF_NO") != null) {
                        svf.VrsOut("INDEX", rs.getString("CERTIF_NO")); // 発行番号
                    }
                    svf.VrsOut("DATE", _param.changePrintDate(rs.getString("ISSUEDATE"))); // 発行年月日
                    svf.VrsOut("CERTIF_KIND", rs.getString("KINDNAME")); // 証明書の種類
                    svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                    if (null != rs.getString("NAME")) {
                        final String nameField = rs.getString("NAME").length() > 10 ? "2_1" : "1";
                        svf.VrsOut("NAME" + nameField, rs.getString("NAME")); // 氏名
                    }
                    if (null != rs.getString("HIGH_SCHOOL_NAME")) {
                        final String schoolNameField = rs.getString("HIGH_SCHOOL_NAME").length() > 10 ? "2_1" : "1";
                        svf.VrsOut("SCHOOL_NAME" + schoolNameField, rs.getString("HIGH_SCHOOL_NAME"));
                    }
                    if (rs.getString("CERTIF_KINDCD").equals("301")) {
                        svf.VrsOut("REMARK", rs.getString("REMARK3"));
                    }
                    svf.VrEndRecord();
                    nonedata = true;
                }
                rs.close();
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
            return nonedata;
        }

    }

    /** フォーム020_4 */
    private class Form020_4 extends Form {
        public Form020_4() {
            setName(FORM020_4);
            setType(4);
        }

        boolean formPrintOut(final Vrw32alp svf, final DB2UDB db2) throws SQLException {
            boolean nonedata = false;
            PreparedStatement ps1 = null;
            ResultSet rs = null;
            try {
                ps1 = db2.prepareStatement(_param._form.selectSql());
                rs = ps1.executeQuery();
                while (rs.next()) {
                    if (!nonedata) {
                        printsvfPageHead(svf, rs.getString("PAGENUM"));
                    }
                    if (rs.getString("CERTIF_NO") != null) {
                        svf.VrsOut("INDEX", rs.getString("CERTIF_NO")); // 発行番号
                    }
                    svf.VrsOut("DATE", _param.changePrintDate(rs.getString("ISSUEDATE"))); // 発行年月日
                    svf.VrsOut("CERTIF_KIND", rs.getString("KINDNAME")); // 証明書の種類
                    svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                    if (null != rs.getString("NAME")) {
                        final String nameField = rs.getString("NAME").length() > 10 ? "2_1" : "1";
                        svf.VrsOut("NAME" + nameField, rs.getString("NAME")); // 氏名
                    }
                    if (null != rs.getString("REMARK4") && null != rs.getString("REMARK5")) {
                        svf.VrsOut("REMARK1", rs.getString("REMARK4") + "\uFF5E" + rs.getString("REMARK5"));
                    }

                    final String addr = rs.getString("PREF_NAME") + rs.getString("ADDR1") + rs.getString("ADDR2");
                    final String setAddrField = null != addr && addr.length() > 25 ? "2_1" : "1";
                    svf.VrsOut("ADDRESS" + setAddrField, addr);
                    svf.VrEndRecord();
                    nonedata = true;
                }
                rs.close();
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
            return nonedata;
        }

    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _year2;
        private final String _semester;
        private final String _date;
        private final String _order;
        private final String _type;
        private final String _certifKind;
        private final String _certifKindName;
        private final String _outDiv;
        private final String _page;
        private final String _loginDate;
        private final String _schoolName;
        private final Form _form;

        private boolean _seirekiFlg;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE");
            // 発行日付より年度の取得
            _year2 = KNJ_EditDate.b_year(request.getParameter("DATE"));
            _order = request.getParameter("ORDER");
            _type = request.getParameter("TYPE");
            _certifKind = getCertifKind(request, _type);
            _outDiv = request.getParameter("OUTDIV");
            _page = request.getParameter("PAGE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _form = createForm(_type);

            _certifKindName = setCertifKindName(db2, _year, _certifKind);
            _schoolName = getSchoolInfo(db2, _year);
            setSeirekiFlg(db2);
        }

        private String getCertifKind(final HttpServletRequest request, final String type) {
            if (type.equals("1")) {
                return request.getParameter("CERTIF_KIND1");
            } else if (type.equals("2")) {
                return request.getParameter("CERTIF_KIND2");
            } else if (type.equals("3")) {
                return request.getParameter("CERTIF_KIND3");
            } else {
                return request.getParameter("CERTIF_KIND4");
            }
        }

        private Form createForm(final String type) {
            final Form form;
            if (type.equals("1")) {
                form = new Form020_1();
            } else if (type.equals("2")) {
                form = new Form020_3();
            } else if (type.equals("3")) {
                form = new Form020_4();
            } else {
                form = new Form020_2();
            }
            return form;
        }

        private String setCertifKindName(final DB2UDB db2, final String year, final String certifKind) {
            String retVal = "全て";
            if (!certifKind.equals(CERTIFALL)) {
                try {
                    String sql = "SELECT KINDNAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '" + certifKind + "' ";
                    PreparedStatement ps = db2.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        retVal = rs.getString("KINDNAME");
                    }
                    ps.close();
                    rs.close();
                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                    db2.commit();
                }
            }
            return retVal;
        }

        private String getSchoolInfo(final DB2UDB db2, final String year) throws SQLException {
            String retVal = "";
            ResultSet rs = null;
            PreparedStatement ps1 = null;
            try {
                KNJ_SchoolinfoSql obj_SchoolinfoSql = new KNJ_SchoolinfoSql("10000");
                ps1 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
                int p = 0;
                ps1.setString(++p, year);
                ps1.setString(++p, year);
                rs = ps1.executeQuery();
                if (rs.next()) {
                    if (rs.getString("SCHOOLNAME1") != null) {
                        retVal = rs.getString("SCHOOLNAME1");
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps1, rs);
                db2.commit();
            }
            return retVal;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(date);
                }
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year) {
            if (_seirekiFlg) {
                return year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            }
        }

    }
}// クラスの括り

