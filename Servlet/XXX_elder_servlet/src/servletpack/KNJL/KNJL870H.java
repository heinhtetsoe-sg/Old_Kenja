/*
 * $Id: b7ee6dc0fc2ff4f1bbae37764c4dfe02f217dffc $
 *
 * 作成日: 2020/10/02
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL870H {

    private static final Log log = LogFactory.getLog(KNJL870H.class);

    private boolean _hasData;

    private Param _param;

    private static final String ADMITTED = "1"; // 1:済み（入学）

    private static final int NYUGAKUSYA_ICHIRAN_LINE_MAX = 40;

    private static final int NYUGAKUSYA_TACKK_SEAL_LINE_MAX = 6;
    private static final int NYUGAKUSYA_TACKK_SEAL_COL_MAX = 2;

    private static final int SYUSHINKOU_TACK_SEAL_LINE_MAX = 6;
    private static final int SYUSHINKOU_TACK_SEAL_COL_MAX = 2;

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
        if ("1".equals(_param._output)) {
            printNyugakusyaIchiranhyou(db2, svf);
        } else if ("2".equals(_param._output)) {
            printNyugakusyaTackSeal(db2, svf);
        } else if ("3".equals(_param._output)) {
            printSyushinkouTackSeal(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 入学者一覧表を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printNyugakusyaIchiranhyou(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL870H_1.frm", 1);

        List<List<NyugakusyaData>> nyugakusyaList = getNyugakusyaList(db2);

        int lineCnt = 1;
        int pageCnt = 1;

        for (List<NyugakusyaData> retList : nyugakusyaList) {
            svf.VrsOut("PAGE", pageCnt + "頁");
            svf.VrsOut("DATE", _param._loginDate.replace("-", "/"));
            String titleStr = "1".equals(_param._applicantDiv) ? "中学" : "2".equals(_param._applicantDiv) ? "高等学校" : "";
            svf.VrsOut("TITLE", _param._entexamyear + "年度　" + titleStr + "入学者一覧表");

            for (NyugakusyaData nyugakusyaData : retList) {
                svf.VrsOutn("EXAM_NO1", lineCnt, nyugakusyaData._examNo);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(nyugakusyaData._name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, nyugakusyaData._name);

                final int nameKanaByte = KNJ_EditEdit.getMS932ByteLength(nyugakusyaData._nameKana);
                final String nameKanaFieldStr = nameKanaByte > 30 ? "3" : nameKanaByte > 20 ? "2" : "1";
                svf.VrsOutn("KANA" + nameKanaFieldStr, lineCnt, nyugakusyaData._nameKana);

                String finschoolName = null;
                if (nyugakusyaData._abbv1 == null) {
                    finschoolName = StringUtils.defaultString(nyugakusyaData._finschoolName);
                } else {
                    finschoolName = nyugakusyaData._abbv1 + "　" + StringUtils.defaultString(nyugakusyaData._finschoolName);
                }
                final int finschoolNameByte = KNJ_EditEdit.getMS932ByteLength(finschoolName);
                final String finschoolNameFieldStr = finschoolNameByte > 30 ? "3" : finschoolNameByte > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + finschoolNameFieldStr, lineCnt, finschoolName);

                svf.VrsOutn("REMARK3_1", lineCnt, nyugakusyaData._remark);

                lineCnt++;
                _hasData = true;
            }

            svf.VrEndPage();
            lineCnt = 1;
            pageCnt++;
        }
    }

    /**
     * 入学者タックシールを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printNyugakusyaTackSeal(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL870H_2.frm", 1);

        List<NyugakusyaTackSealData> nyugakusyaTackSealList = getNyugakusyaTackSealList(db2);

        int lineMax = NYUGAKUSYA_TACKK_SEAL_LINE_MAX * NYUGAKUSYA_TACKK_SEAL_COL_MAX;
        int lineCnt = 0;

        for (NyugakusyaTackSealData nyugakusyaTackSealData : nyugakusyaTackSealList) {
            // 改ページの制御
            if (lineCnt >= lineMax) {
                svf.VrEndPage();
                lineCnt = 0;
            }

            int lineFiledCnt = (lineCnt / NYUGAKUSYA_TACKK_SEAL_COL_MAX) + 1;
            String lineFiledStr = String.valueOf((lineCnt % NYUGAKUSYA_TACKK_SEAL_COL_MAX) + 1);

            String zipCd = "〒" + StringUtils.defaultString(nyugakusyaTackSealData._zipCd);
            svf.VrsOutn("ZIP_NO" + lineFiledStr, lineFiledCnt, zipCd);

            final int address1Byte = KNJ_EditEdit.getMS932ByteLength(nyugakusyaTackSealData._address1);
            final String address1FieldStr = address1Byte > 50 ? "3" : address1Byte > 40 ? "2" : "1";
            svf.VrsOutn("ADDR" + lineFiledStr + "_1_" + address1FieldStr, lineFiledCnt, nyugakusyaTackSealData._address1);

            final int address2Byte = KNJ_EditEdit.getMS932ByteLength(nyugakusyaTackSealData._address2);
            final String address2FieldStr = address2Byte > 50 ? "3" : address2Byte > 40 ? "2" : "1";
            svf.VrsOutn("ADDR" + lineFiledStr + "_2_" + address2FieldStr, lineFiledCnt, nyugakusyaTackSealData._address2);

            String name = StringUtils.defaultString(nyugakusyaTackSealData._name) + "　様";
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
            final String nameFieldStr = nameByte > 50 ? "3" : nameByte > 40 ? "2" : "1";
            svf.VrsOutn("NAME" + lineFiledStr + "_" + nameFieldStr, lineFiledCnt, name);

            svf.VrsOutn("NO" + lineFiledStr, lineFiledCnt, nyugakusyaTackSealData._examNo);

            lineCnt++;
            _hasData = true;
        }
    }

    /**
     * 出身校タックシールを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printSyushinkouTackSeal(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL870H_2.frm", 1);

        List<SyushinkouTackSealData> syushinkouTackSealList = getSyushinkouTackSealList(db2);

        int lineMax = SYUSHINKOU_TACK_SEAL_LINE_MAX * SYUSHINKOU_TACK_SEAL_COL_MAX;
        int lineCnt = 0;

        for (SyushinkouTackSealData syushinkouTackSealData : syushinkouTackSealList) {
            // 改ページの制御
            if (lineCnt >= lineMax) {
                svf.VrEndPage();
                lineCnt = 0;
            }

            int lineFiledCnt = (lineCnt / SYUSHINKOU_TACK_SEAL_COL_MAX) + 1;
            String lineFiledStr = String.valueOf((lineCnt % SYUSHINKOU_TACK_SEAL_COL_MAX) + 1);

            String zipCd = "〒" + StringUtils.defaultString(syushinkouTackSealData._finschoolZipCd);
            svf.VrsOutn("ZIP_NO" + lineFiledStr, lineFiledCnt, zipCd);

            final int address1Byte = KNJ_EditEdit.getMS932ByteLength(syushinkouTackSealData._finschoolAddr1);
            final String address1FieldStr = address1Byte > 50 ? "3" : address1Byte > 40 ? "2" : "1";
            svf.VrsOutn("ADDR" + lineFiledStr + "_1_" + address1FieldStr, lineFiledCnt, syushinkouTackSealData._finschoolAddr1);

            final int address2Byte = KNJ_EditEdit.getMS932ByteLength(syushinkouTackSealData._finschoolAddr2);
            final String address2FieldStr = address2Byte > 50 ? "3" : address2Byte > 40 ? "2" : "1";
            svf.VrsOutn("ADDR" + lineFiledStr + "_2_" + address2FieldStr, lineFiledCnt, syushinkouTackSealData._finschoolAddr2);

            String finschoolName = StringUtils.defaultString(syushinkouTackSealData._finschoolName) + "　御中";
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(finschoolName);
            final String nameFieldStr = nameByte > 50 ? "3" : nameByte > 40 ? "2" : "1";
            svf.VrsOutn("NAME" + lineFiledStr + "_" + nameFieldStr, lineFiledCnt, finschoolName);

            svf.VrsOutn("NO" + lineFiledStr, lineFiledCnt, syushinkouTackSealData._finschoolCd);

            lineCnt++;
            _hasData = true;
        }
    }

    private List<List<NyugakusyaData>> getNyugakusyaList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        int lineCnt = 1;

        List<List<NyugakusyaData>> nyugakusyaList = new ArrayList<List<NyugakusyaData>>();
        List<NyugakusyaData> retList = new ArrayList<NyugakusyaData>();

        try {
            final String nyugakusyaIchiranhyouSql = getNyugakusyaIchiranhyouSql();
            log.debug(" sql =" + nyugakusyaIchiranhyouSql);
            ps = db2.prepareStatement(nyugakusyaIchiranhyouSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (lineCnt > NYUGAKUSYA_ICHIRAN_LINE_MAX) {
                    nyugakusyaList.add(retList);
                    lineCnt = 1;
                    retList = new ArrayList<NyugakusyaData>();
                }

                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String abbv1 = rs.getString("ABBV1");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String remark = rs.getString("REMARK1");

                final NyugakusyaData kesekisyaData = new NyugakusyaData(examNo, name, nameKana, abbv1, finschoolName, remark);

                retList.add(kesekisyaData);
                lineCnt++;
            }

            if (!retList.isEmpty()) {
                nyugakusyaList.add(retList);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return nyugakusyaList;
    }

    private List<NyugakusyaTackSealData> getNyugakusyaTackSealList(final DB2UDB db2) {
        List<NyugakusyaTackSealData> nyugakusyaTackSealList = new ArrayList<NyugakusyaTackSealData>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String nyugakusyaTackSeaSql = getNyugakusyaTackSeaSql();
            log.debug(" sql =" + nyugakusyaTackSeaSql);
            ps = db2.prepareStatement(nyugakusyaTackSeaSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String zipCd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String name = rs.getString("NAME");
                final String examNo = rs.getString("EXAMNO");

                final NyugakusyaTackSealData nyugakusyaTackSealData = new NyugakusyaTackSealData(zipCd, address1, address2, name, examNo);
                nyugakusyaTackSealList.add(nyugakusyaTackSealData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return nyugakusyaTackSealList;
    }

    private List<SyushinkouTackSealData> getSyushinkouTackSealList(final DB2UDB db2) {
        List<SyushinkouTackSealData> syushinkouTackSealList = new ArrayList<SyushinkouTackSealData>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String syushinkouTackSeaSql = getSyushinkouTackSeaSql();
            log.debug(" sql =" + syushinkouTackSeaSql);
            ps = db2.prepareStatement(syushinkouTackSeaSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String fsZipCd = rs.getString("FINSCHOOL_ZIPCD");
                final String fsAddr1 = rs.getString("FINSCHOOL_ADDR1");
                final String fsAddr2 = rs.getString("FINSCHOOL_ADDR2");
                final String fsName = rs.getString("FINSCHOOL_NAME");
                final String fsCd = rs.getString("FINSCHOOLCD");

                final SyushinkouTackSealData syushinkouTackSealData = new SyushinkouTackSealData(fsZipCd, fsAddr1, fsAddr2, fsName, fsCd);
                syushinkouTackSealList.add(syushinkouTackSealData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return syushinkouTackSealList;
    }

    private String getNyugakusyaIchiranhyouSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("      BASE.EXAMNO, ");
        stb.append("      BASE.NAME, ");
        stb.append("      BASE.NAME_KANA, ");
        stb.append("      L015.ABBV1, ");
        stb.append("      FS.FINSCHOOL_NAME, ");
        stb.append("      DETAIL.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("           AND L013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND L013.SEQ = BASE.JUDGEMENT ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L015 ON L015.SETTING_CD = 'L015' ");
        stb.append("          AND L015.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND L015.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND L015.SEQ = FS.FINSCHOOL_DIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND DETAIL.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND DETAIL.SEQ = '033' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND L013.NAMESPARE1 = '" + ADMITTED + "' ");
        stb.append("     AND BASE.ENTDIV = '" + ADMITTED + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private String getNyugakusyaTackSeaSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("      ADDR.ZIPCD, ");
        stb.append("      ADDR.ADDRESS1, ");
        stb.append("      ADDR.ADDRESS2, ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("      BASE.NAME, ");
        } else if ("2".equals(_param._dataDiv)) {
            stb.append("      ADDR.GNAME AS NAME, ");
        }
        stb.append("      BASE.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("           AND L013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND L013.SEQ = BASE.JUDGEMENT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND ADDR.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND L013.NAMESPARE1 = '" + ADMITTED + "' ");
        stb.append("     AND BASE.ENTDIV = '" + ADMITTED + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private String getSyushinkouTackSeaSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("      FS.FINSCHOOL_ZIPCD, ");
        stb.append("      FS.FINSCHOOL_ADDR1, ");
        stb.append("      FS.FINSCHOOL_ADDR2, ");
        stb.append("      FS.FINSCHOOL_NAME, ");
        stb.append("      FS.FINSCHOOLCD ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_SETTING_MST L013 ON L013.SETTING_CD = 'L013' ");
        stb.append("           AND L013.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND L013.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND L013.SEQ = BASE.JUDGEMENT ");
        stb.append("     INNER JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND L013.NAMESPARE1 = '" + ADMITTED + "' ");
        stb.append("     AND BASE.ENTDIV = '" + ADMITTED + "' ");
        stb.append(" GROUP BY ");
        stb.append("      FS.FINSCHOOL_ZIPCD, ");
        stb.append("      FS.FINSCHOOL_ADDR1, ");
        stb.append("      FS.FINSCHOOL_ADDR2, ");
        stb.append("      FS.FINSCHOOL_NAME, ");
        stb.append("      FS.FINSCHOOLCD ");
        stb.append(" ORDER BY ");
        stb.append("     FS.FINSCHOOLCD ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77312 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class NyugakusyaData {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _abbv1;
        final String _finschoolName;
        final String _remark;

        public NyugakusyaData(
                final String examNo,
                final String name,
                final String nameKana,
                final String abbv1,
                final String finschoolName,
                final String remark
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _abbv1 = abbv1;
            _finschoolName = finschoolName;
            _remark = remark;
        }
    }

    private class NyugakusyaTackSealData {
        final String _zipCd;
        final String _address1;
        final String _address2;
        final String _name;
        final String _examNo;

        public NyugakusyaTackSealData (
                final String zipCd,
                final String address1,
                final String address2,
                final String name,
                final String examNo
        ) {
            _zipCd = zipCd;
            _address1 = address1;
            _address2 = address2;
            _name = name;
            _examNo = examNo;
        }
    }

    private class SyushinkouTackSealData {
        final String _finschoolZipCd;
        final String _finschoolAddr1;
        final String _finschoolAddr2;
        final String _finschoolName;
        final String _finschoolCd;

        public SyushinkouTackSealData (
                final String finschoolZipCd,
                final String finschoolAddr1,
                final String finschoolAddr2,
                final String finschoolName,
                final String finschoolCd
        ) {
            _finschoolZipCd = finschoolZipCd;
            _finschoolAddr1 = finschoolAddr1;
            _finschoolAddr2 = finschoolAddr2;
            _finschoolName = finschoolName;
            _finschoolCd = finschoolCd;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _output;
        private final String _dataDiv;
        private final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _output = request.getParameter("OUTPUT");
            _dataDiv = request.getParameter("DATADIV");
            _loginDate = request.getParameter("LOGIN_DATE");
        }
    }
}

// eof

