// kanji=漢字
/*
 * $Id: 3b56d252381d3ed10da91c36f27c42f06ef142f4 $
 *
 * 作成日: 2007/12/21 20:30:52 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 3b56d252381d3ed10da91c36f27c42f06ef142f4 $
 */
public class KNJL365K {
    private static final Log log = LogFactory.getLog(KNJL365K.class);

    private static final String FORM_FILE = "KNJL365.frm";
    private static final int DATA_CNT = 23;

    Param _param;

    /**
     * KNJL.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

            boolean hasData = false;
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

        } finally {
            close(db2, svf);
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

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }


    public void setInfluenceName(DB2UDB db2, Vrw32alp svf) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + _param._specialReasonDiv + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                svf.VrsOut("VIRUS", name2);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
        }
    }
    
    /**
     * 印刷メイン
     */
    private boolean printMain(DB2UDB db2, Vrw32alp svf) throws SQLException {
        boolean rtnFlg = false;

        svf.VrSetForm(FORM_FILE, 4);
        svf.VrsOut("NENDO", _param._gengou);
        svf.VrsOut("DATE", _param._date);
        setInfluenceName(db2, svf);

        ResultSet rs = null;
        db2.query(getPrintDataSql());
        rs = db2.getResultSet();

        int selCnt = 1;
        while (rs.next()) {
            svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));
            for (int fieldCnt = 1; fieldCnt <= DATA_CNT; fieldCnt++) {
                if (!rs.getString("DATA" + String.valueOf(fieldCnt)).equals("")) {
                    if (9 < rs.getString("DATA" + fieldCnt).length()) {
                        svf.VrsOut("ERROR" + selCnt + "_2", rs.getString("DATA" + fieldCnt));
                        selCnt++;
                    } else {
                        svf.VrsOut("ERROR" + selCnt + "_1", rs.getString("DATA" + fieldCnt));
                        selCnt++;
                    }
                }
                if (selCnt > 5) {
                    svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));
                    svf.VrEndRecord();
                    selCnt = 1;
                }
            }
            if (selCnt > 1) {
                svf.VrEndRecord();
                selCnt = 1;
            }
            rtnFlg = true;
        }

        return rtnFlg;
    }

    /**
     * @return
     */
    private String getPrintDataSql() {
        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("   T1.EXAMNO, ");
        sql.append("   CASE WHEN L1.NAMECD2 IS NULL THEN '試験区分マスタ未登録' ELSE '' END AS DATA1, ");
        sql.append("   CASE WHEN T1.SHDIV IS NULL THEN '専併区分ブランク' ELSE '' END AS DATA2, ");
        sql.append("   CASE WHEN T1.SHDIV IS NOT NULL AND L2.NAMECD2 IS NULL THEN '専併区分マスタ未登録' ELSE '' END AS DATA3, ");
        sql.append("   CASE WHEN T1.APPLICANTDIV IS NULL THEN '出願区分ブランク' ELSE '' END AS DATA4, ");
        sql.append("   CASE WHEN T1.APPLICANTDIV IS NOT NULL AND L3.NAMECD2 IS NULL THEN '出願区分マスタ未登録' ELSE '' END AS DATA5, ");
        sql.append("   CASE WHEN T1.DESIREDIV IS NULL THEN '志望区分ブランク' ELSE '' END AS DATA6, ");
        sql.append("   CASE WHEN T1.DESIREDIV IS NOT NULL AND L4.DESIREDIV IS NULL THEN '志願区分マスタ未登録' ELSE '' END AS DATA7, ");
        sql.append("   CASE WHEN T1.NAME IS NULL THEN '氏名ブランク' ELSE '' END AS DATA8, ");
        sql.append("   CASE WHEN T1.NAME_KANA IS NULL THEN '氏名かなブランク' ELSE '' END AS DATA9, ");
        sql.append("   CASE WHEN T1.SEX IS NULL THEN '性別ブランク' ELSE '' END AS DATA10, ");
        sql.append("   CASE WHEN T1.SEX IS NOT NULL AND L5.NAMECD2 IS NULL THEN '性別マスタ未登録' ELSE '' END AS DATA11, ");
        sql.append("   CASE WHEN T1.BIRTHDAY IS NULL THEN '生年月日ブランク' ELSE '' END AS DATA12, ");
        sql.append("   CASE WHEN T1.ADDRESSCD IS NULL THEN '現住所コードブランク' ELSE '' END AS DATA13, ");
        sql.append("   CASE WHEN T1.ADDRESSCD IS NOT NULL AND L6.NAMECD2 IS NULL THEN '現住所コードマスタ未登録' ELSE '' END AS DATA14, ");
        sql.append("   CASE WHEN L9.ADDRESS IS NOT NULL ");
        sql.append("             AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%都%' ");
        sql.append("             AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%道%' ");
        sql.append("             AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%府%' ");
        sql.append("             AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%県%' THEN '都道府県名なし' ELSE '' END AS DATA15, ");
        sql.append("   CASE WHEN T1.LOCATIONCD IS NULL THEN '出身学校所在地コードブランク' ELSE '' END AS DATA16, ");
        sql.append("   CASE WHEN T1.LOCATIONCD IS NOT NULL AND L7.NAMECD2 IS NULL THEN '所在地コードマスタ未登録' ELSE '' END AS DATA17, ");
        sql.append("   CASE WHEN T1.NATPUBPRIDIV IS NULL THEN '国公私立区分ブランク' ELSE '' END AS DATA18, ");
        sql.append("   CASE WHEN T1.NATPUBPRIDIV IS NOT NULL AND L8.NAMECD2 IS NULL THEN '国公私立区分マスタ未登録' ELSE '' END AS DATA19, ");
        sql.append("   CASE WHEN T1.GNAME IS NULL THEN '保護者氏名ブランク' ELSE '' END AS DATA20, ");
        sql.append("   CASE WHEN T1.GKANA IS NULL THEN '保護者氏名かなブランク' ELSE '' END AS DATA21, ");
        sql.append("   CASE WHEN T1.NAME = T1.GNAME THEN '保護者氏名が志願者と同じ' ELSE '' END AS DATA22, ");
        sql.append("   CASE WHEN T1.NAME_KANA = T1.GKANA THEN '保護者かなが志願者と同じ' ELSE '' END AS DATA23 ");
        sql.append(" FROM ");
        sql.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        sql.append("   LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L1.NAMECD1 = 'L003' ");
        sql.append("        AND L1.NAMECD2 = T1.TESTDIV ");
        sql.append("   LEFT JOIN V_NAME_MST L2 ON L2.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L2.NAMECD1 = 'L006' ");
        sql.append("        AND L2.NAMECD2 = T1.SHDIV ");
        sql.append("   LEFT JOIN V_NAME_MST L3 ON L3.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L3.NAMECD1 = 'L005' ");
        sql.append("        AND L3.NAMECD2 = T1.APPLICANTDIV ");
        sql.append("   LEFT JOIN (SELECT DISTINCT ");
        sql.append("                  LT4.TESTDIV, ");
        sql.append("                  LT4.DESIREDIV ");
        sql.append("              FROM ");
        sql.append("                  ENTEXAM_WISHDIV_MST LT4 ");
        sql.append("              WHERE ");
        sql.append("                  LT4.ENTEXAMYEAR = '" + _param._year + "' ");
        sql.append("        ) L4 ON L4.TESTDIV = T1.TESTDIV ");
        sql.append("             AND L4.DESIREDIV = T1.DESIREDIV ");
        sql.append("   LEFT JOIN V_NAME_MST L5 ON L5.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L5.NAMECD1 = 'Z002' ");
        sql.append("        AND L5.NAMECD2 = T1.SEX ");
        sql.append("   LEFT JOIN V_NAME_MST L6 ON L6.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L6.NAMECD1 = 'L007' ");
        sql.append("        AND L6.NAMECD2 = T1.ADDRESSCD ");
        sql.append("   LEFT JOIN V_NAME_MST L7 ON L7.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L7.NAMECD1 = 'L007' ");
        sql.append("        AND L7.NAMECD2 = T1.LOCATIONCD ");
        sql.append("   LEFT JOIN V_NAME_MST L8 ON L8.YEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L8.NAMECD1 = 'L004' ");
        sql.append("        AND L8.NAMECD2 = T1.NATPUBPRIDIV ");
        sql.append("   LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L9 ON L9.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        sql.append("        AND L9.TESTDIV = T1.TESTDIV ");
        sql.append("        AND L9.EXAMNO = T1.EXAMNO ");
        sql.append(" WHERE ");
        sql.append("   T1.ENTEXAMYEAR = '" + _param._year + "' ");
        if (!"9".equals(_param._specialReasonDiv)) {
            sql.append("   AND T1.SPECIAL_REASON_DIV = '" + _param._specialReasonDiv + "' ");
        }
        if (_param._testdiv != null && !_param._testdiv.equals("")) {
            sql.append("   AND T1.TESTDIV = '" + _param._testdiv + "' ");
        }
        sql.append("   AND (T1.SHDIV IS NULL ");
        sql.append("        OR T1.APPLICANTDIV IS NULL ");
        sql.append("        OR T1.DESIREDIV IS NULL ");
        sql.append("        OR T1.NAME IS NULL ");
        sql.append("        OR T1.NAME_KANA IS NULL ");
        sql.append("        OR T1.SEX IS NULL ");
        sql.append("        OR T1.ADDRESSCD IS NULL ");
        sql.append("        OR T1.LOCATIONCD IS NULL ");
        sql.append("        OR T1.NATPUBPRIDIV IS NULL ");
        sql.append("        OR T1.BIRTHDAY IS NULL ");
        sql.append("        OR T1.GNAME IS NULL ");
        sql.append("        OR T1.GKANA IS NULL ");
        sql.append("        OR L1.NAMECD2 IS NULL ");
        sql.append("        OR L2.NAMECD2 IS NULL ");
        sql.append("        OR L3.NAMECD2 IS NULL ");
        sql.append("        OR L4.DESIREDIV IS NULL ");
        sql.append("        OR L5.NAMECD2 IS NULL ");
        sql.append("        OR L6.NAMECD2 IS NULL ");
        sql.append("        OR L7.NAMECD2 IS NULL ");
        sql.append("        OR L8.NAMECD2 IS NULL ");
        sql.append("        OR T1.NAME = T1.GNAME ");
        sql.append("        OR T1.NAME_KANA = T1.GKANA ");
        sql.append("        OR (L9.ADDRESS IS NOT NULL ");
        sql.append("            AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%都%' ");
        sql.append("            AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%道%' ");
        sql.append("            AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%府%' ");
        sql.append("            AND SUBSTR(VALUE(L9.ADDRESS, '　　　　'), 7, 6) NOT LIKE '%県%') ");
        sql.append("       ) ");
        sql.append(" ORDER BY ");
        sql.append("   T1.TESTDIV, ");
        sql.append("   T1.EXAMNO ");

        return sql.toString();
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
        private final String _year;
        private final String _testdiv;
        private final String _gengou;
        private final String _date;
        private final String _specialReasonDiv;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _testdiv = request.getParameter("TESTDIV");
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _gengou = gengou + "年度";

            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            final StringBuffer stb = new StringBuffer();
            stb.append( nao_package.KenjaProperties.gengou(Integer.parseInt( sdf.format(date) )) );
            sdf = new SimpleDateFormat("年M月d日");
            stb.append( sdf.format(date) );
            _date = stb.toString();
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");
        }
    }
}
 // KNJL365K

// eof
