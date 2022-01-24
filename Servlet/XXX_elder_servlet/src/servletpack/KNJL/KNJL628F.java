/*
 * $Id: e4cf65d835b7b859a6b60be45f849d45ebda6df4 $
 *
 * 作成日: 2019/12/23
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL628F {

    private static final Log log = LogFactory.getLog(KNJL628F.class);

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
        String dateStr = KNJ_EditDate.h_format_JP(db2,_param._printDate);
        String entDate = KNJ_EditDate.h_format_JP(db2, _param._entexamyear + "-04-01");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
//                final String examNo = StringUtils.defaultString(rs.getString("EXAMNO"), "");
                final String name = StringUtils.defaultString(rs.getString("NAME"), "");
                final String birth = StringUtils.defaultString(rs.getString("BIRTHDAY"), "");
                final String addr1 = StringUtils.defaultString(rs.getString("ADDRESS1"), "");
                final String addr2 = StringUtils.defaultString(rs.getString("ADDRESS2"), "");

                svf.VrSetForm("KNJL628F.frm", 1); //入学許可証

                svf.VrsOut("DATE", dateStr);
                svf.VrsOut("SCHOOLNAME", (String)_param._certifSchoolMap.get("SCHOOL_NAME"));  //学校名(証明書系の学校名)
                svf.VrsOut("JOBNAME", (String)_param._certifSchoolMap.get("JOB_NAME"));
                svf.VrsOut("STAFFNAME", (String)_param._certifSchoolMap.get("PRINCIPAL_NAME"));  //校長名

                svf.VrsOut("ENT_DATE", "上記の者に、" + entDate + "付をもって本学院中学校第１学年に");

                final int nlen = KNJ_EditEdit.getMS932ByteLength(name);
                final String nfield = nlen > 30 ? "2" : "1";
                svf.VrsOut("NAME" + nfield, name);
                final String bStr = KNJ_EditDate.h_format_JP(db2, birth.replace('/', '-'));
                svf.VrsOut("BIRTHDATY", bStr);
                final String addr = addr1 + addr2;
                final int addrlen = KNJ_EditEdit.getMS932ByteLength(addr);
                final String addrfield = addrlen > 50 ? "4" : addrlen > 40 ? "3" : addrlen > 30 ? "2" : "1";
                svf.VrsOut("ADDR" + addrfield, addr);

                svf.VrEndPage();
                _hasData = true;
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
        stb.append("   MIN(RECEPT.RECEPTNO) AS SORT_NUMS, ");
        stb.append("   BASEDAT.EXAMNO, ");
        stb.append("   BASEDAT.NAME, ");
        stb.append("   BASEDAT.BIRTHDAY, ");
        stb.append("   ADDR.ADDRESS1, ");
        stb.append("   ADDR.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASEDAT ");
        stb.append("       ON BASEDAT.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("      AND BASEDAT.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("      AND BASEDAT.EXAMNO = RECEPT.EXAMNO ");
        //合格者が対象
        stb.append("     INNER JOIN NAME_MST L013 ");
        stb.append("           ON L013.NAMECD2 = BASEDAT.JUDGEMENT ");
        stb.append("          AND L013.NAMECD1 = 'L013' ");
        stb.append("          AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ");
        stb.append("       ON ADDR.ENTEXAMYEAR = BASEDAT.ENTEXAMYEAR ");
        stb.append("      AND ADDR.APPLICANTDIV = BASEDAT.APPLICANTDIV ");
        stb.append("      AND ADDR.EXAMNO = BASEDAT.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '1' "); //対象は中学のみ
        if ("2".equals(_param._printDiv)) {
            stb.append("     AND RECEPT.RECEPTNO = '" + _param._passReceptno + "' ");
        }
        stb.append("     AND L013.NAMESPARE1 = '1' ");       //合格フラグが立っている
        //stb.append("     AND BASEDAT.PROCEDUREDIV = '1' ");  //入学手続き済み
        stb.append(" GROUP BY ");
        stb.append("   BASEDAT.EXAMNO, ");
        stb.append("   BASEDAT.NAME, ");
        stb.append("   BASEDAT.BIRTHDAY, ");
        stb.append("   ADDR.ADDRESS1, ");
        stb.append("   ADDR.ADDRESS2 ");
        stb.append(" ORDER BY ");
        stb.append("     SORT_NUMS ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71715 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _printDate;
        private final String _printDiv;
        private final String _passReceptno;
        private final Map _certifSchoolMap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _printDate = request.getParameter("PRINT_DATE").replace('/', '-');
            _printDiv = request.getParameter("PRINT_DIV");
            _passReceptno = StringUtils.defaultString(request.getParameter("PASS_EXAMNO"));
            _certifSchoolMap = getCertifScholl(db2);
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String cdStr = "105";
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + cdStr + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("CORP_NAME", rs.getString("REMARK6"));
                    rtnMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

    }
}

// eof

