/*
 * $Id: d4436f54e3e033be63d95ad97ad0d45d84d47e62 $
 *
 * 作成日: 2018/12/27
 * 作成者: matsushima
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

public class KNJL112Q {

    private static final Log log = LogFactory.getLog(KNJL112Q.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL112Q.frm", 1); //生徒情報確認用紙

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befExamNo = "";
            while (rs.next()) {
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTEND_NO");
                final String examno = rs.getString("EXAMNO");
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String kana = StringUtils.defaultString(rs.getString("KANA"));
                final String sex = rs.getString("SEX");
                final String finschoolName = rs.getString("FINSCHOOL_NAME_ABBV");
                final String zipNo = rs.getString("ZIP_NO");
                final String addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                final String addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                final String telNo = rs.getString("TEL_NO");
                final String guardName = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                final String guardKana = StringUtils.defaultString(rs.getString("GUARD_KANA"));
                final String relationship = rs.getString("RELATION");
                final String guardZipNo = rs.getString("GUARD_ZIP_NO");
                final String guardAddr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                final String guardAddr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                final String guardTelNo = rs.getString("GUARD_TEL_NO");

                if (!"".equals(befExamNo) && !befExamNo.equals(examno)) {
                    svf.VrEndPage();
                }
                svf.VrsOut("HR_NAME", hrName); //仮クラス
                svf.VrsOut("ATTEND_NO", attendNo); //仮クラス番号
                svf.VrsOut("EXAM_NO", examno); //受験番号
                final String nameField = getMS932Bytecount(name) > 30 ? "3" : getMS932Bytecount(name) > 24 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name); //氏名
                final String kanaField = getMS932Bytecount(kana) > 30 ? "3" : getMS932Bytecount(kana) > 24 ? "2" : "1";
                svf.VrsOut("KANA" + kanaField, kana); //ふりがな
                svf.VrsOut("SEX", sex); //性別
                svf.VrsOut("FINSCHOOL_NAME", finschoolName); //出身中学校名
                svf.VrsOut("ZIP_NO", zipNo); //生徒郵便番号
                final String addr1Field = getMS932Bytecount(addr1) > 66 ? "_2" : "_1";
                svf.VrsOut("ADDR1" + addr1Field, addr1); //生徒住所1
                final String addr2Field = getMS932Bytecount(addr2) > 66 ? "_2" : "_1";
                svf.VrsOut("ADDR2" + addr2Field, addr2); //生徒住所2
                svf.VrsOut("TEL_NO", telNo); //生徒電話番号
                final String guardNameField = getMS932Bytecount(guardName) > 30 ? "3" : getMS932Bytecount(guardName) > 24 ? "2" : "1";
                svf.VrsOut("GUARD_NAME" + guardNameField, guardName); //保護者氏名
                final String guardKanaField = getMS932Bytecount(guardKana) > 30 ? "3" : getMS932Bytecount(guardKana) > 24 ? "2" : "1";
                svf.VrsOut("GUARD_KANA" + guardKanaField, guardKana); //保護者ふりがな
                svf.VrsOut("RELATION", relationship); //続柄
                svf.VrsOut("GUARD_ZIP_NO", guardZipNo); //保護者郵便番号
                final String guardAddr1Field = getMS932Bytecount(guardAddr1) > 66 ? "_2" : "_1";
                svf.VrsOut("GUARD_ADDR1" + guardAddr1Field, guardAddr1); //保護者住所1
                final String guardAddr2Field = getMS932Bytecount(guardAddr2) > 66 ? "_2" : "_1";
                svf.VrsOut("GUARD_ADDR2" + guardAddr2Field, guardAddr2); //保護者住所2
                svf.VrsOut("GUARD_TEL_NO", guardTelNo); //保護者電話番号

                befExamNo = examno;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L063.NAME1 AS HR_NAME, ");
        stb.append("     S035.REMARK2 AS ATTEND_NO, ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA AS KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("     ADDR.ZIPCD AS ZIP_NO, ");
        stb.append("     ADDR.ADDRESS1 AS ADDR1, ");
        stb.append("     ADDR.ADDRESS2 AS ADDR2, ");
        stb.append("     ADDR.TELNO AS TEL_NO, ");
        stb.append("     ADDR.GNAME AS GUARD_NAME, ");
        stb.append("     ADDR.GKANA AS GUARD_KANA, ");
        stb.append("     H201.NAME1 AS RELATION, ");
        stb.append("     ADDR.GZIPCD AS GUARD_ZIP_NO, ");
        stb.append("     ADDR.GADDRESS1 AS GUARD_ADDR1, ");
        stb.append("     ADDR.GADDRESS2 AS GUARD_ADDR2, ");
        stb.append("     ADDR.GTELNO AS GUARD_TEL_NO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     INNER JOIN NAME_MST L013 ON L013.NAMECD2 = VBASE.JUDGEMENT ");
        stb.append("          AND L013.NAMECD1 = 'L013' ");
        stb.append("          AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S035 ON S035.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S035.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S035.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S035.SEQ          = '035' ");
        stb.append("     LEFT JOIN NAME_MST L063 ON L063.NAMECD2 = S035.REMARK1 ");
        stb.append("          AND L063.NAMECD1 = 'L063' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND ADDR.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND ADDR.EXAMNO       = VBASE.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
        stb.append("          AND ADDR.RELATIONSHIP = H201.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND value(VBASE.ENTDIV, '')       = '1' "); // 入学区分
        stb.append("     AND value(VBASE.PROCEDUREDIV, '') = '1' "); // 手続き区分
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65337 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
        }
    }
}

// eof

