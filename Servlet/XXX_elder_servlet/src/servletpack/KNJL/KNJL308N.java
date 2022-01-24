/*
 * $Id: 5e59227c3c49813e6c7948a1b2320069c361a7fe $
 *
 * 作成日: 2015/09/08
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０８Ｎ＞  住所確認表
 **/
public class KNJL308N {

    private static final Log log = LogFactory.getLog(KNJL308N.class);

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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    T4.ZIPCD, ");
        stb.append("    T4.ADDRESS1, ");
        stb.append("    T4.ADDRESS2, ");
        stb.append("    T1.NAME, ");
        stb.append("    T1.EXAMNO ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T4.EXAMNO = T1.EXAMNO ");
        stb.append(" INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND BDETAIL1.EXAMNO = T1.EXAMNO ");
        stb.append("     AND BDETAIL1.SEQ = '001' ");
        stb.append(" INNER JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND CRS1.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND CRS1.TESTDIV = T1.TESTDIV ");
        stb.append("     AND CRS1.COURSECD = BDETAIL1.REMARK8 ");
        stb.append("     AND CRS1.MAJORCD = BDETAIL1.REMARK9 ");
        stb.append("     AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND CRS1.COURSECD || '-' || CRS1.MAJORCD || '-' || CRS1.EXAMCOURSECD = '" + _param._examcourse + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            // log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrSetForm("KNJL308N.frm", 1);

                svf.VrsOut("ZIPNO", rs.getString("ZIPCD")); // 郵便番号
                final String address1 = replaceHyphenMinus(rs.getString("ADDRESS1"));
                final String address2 = replaceHyphenMinus(rs.getString("ADDRESS2"));
                if (StringUtils.defaultString(address1).length() > 25 || StringUtils.defaultString(address2).length() > 25) {
                    svf.VrsOut("ADDR1_3", address1); // 住所３
                    svf.VrsOut("ADDR2_3", address2); // 住所３
                } else if (StringUtils.defaultString(address1).length() > 20 || StringUtils.defaultString(address2).length() > 20) {
                    svf.VrsOut("ADDR1_2", address1); // 住所２
                    svf.VrsOut("ADDR2_2", address2); // 住所２
                } else {
                    svf.VrsOut("ADDR1_1", address1); // 住所１
                    svf.VrsOut("ADDR2_1", address2); // 住所１
                }
                final String name = replaceHyphenMinus(rs.getString("NAME"));
                if (StringUtils.defaultString(name).length() > 20) {
                    svf.VrsOut("NAME1_2", name); // 氏名
                } else {
                    svf.VrsOut("NAME1_1", name); // 氏名
                }
                svf.VrsOut("EXAM_NO", "( " + rs.getString("EXAMNO") + " )"); // 学籍番号

                svf.VrEndPage();
                _hasData = true;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    // 横線を縦書きで縦線に表示されるように変換する
    private String replaceHyphenMinus(final String s) {
        if (null == s) {
            return null;
        }
        final char hyphenMinus = '-'; // NG−
//        final char zenkakuHyphen = '‐'; // OK
        final char minus =  '\u2212'; // NG
        final char zenkakuMinus = '\uFF0D'; // NG
        final char zenkakuDash =  '\u2015'; // OK
        return s.replace(minus, zenkakuDash).replace(hyphenMinus, zenkakuDash).replace(zenkakuMinus, zenkakuDash);
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
        final String _examcourse;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _examcourse = request.getParameter("EXAMCOURSE");
        }
    }
}

// eof

