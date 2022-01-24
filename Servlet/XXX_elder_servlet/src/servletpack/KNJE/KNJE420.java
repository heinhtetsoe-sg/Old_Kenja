/*
 * $Id: 3377b3be301f7811ae5ac2c01b5f50ed1bf52da2 $
 *
 * 作成日: 2015/08/20
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJE420 {

    private static final Log log = LogFactory.getLog(KNJE420.class);

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
        svf.VrSetForm("KNJE420.frm", 4);
        svf.VrsOut("TITLE", _param._year + "年度　就職内定状況集計表");
        svf.VrsOut("DATE", "印刷日：" + _param._ctrlDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate));
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            MajorData majorData = (MajorData) iterator.next();
            final String majorSoeji = getMS932ByteLength(majorData._majorName) > 14 ? "2_1" : "";
            svf.VrsOut("MAJOR_NAME" + majorSoeji, majorData._majorName);
            int gyo = 1;
            for (Iterator itLine = majorData._injiLineList.iterator(); itLine.hasNext();) {
                final InjiLineData lineData = (InjiLineData) itLine.next();

                gyo = "2".equals(lineData._sex) ? 2 : gyo;
                gyo = "9".equals(lineData._sex) ? 3 : gyo;
                svf.VrsOutn("NUM1",  gyo, lineData._zentaiGoukei);
                svf.VrsOutn("NUM2",  gyo, lineData._shingakuGoukei);
                svf.VrsOutn("NUM3",  gyo, lineData._shushokuKibouTotalGakkou);
                svf.VrsOutn("NUM4",  gyo, lineData._shushokuKibouKennaiGakkou);
                svf.VrsOutn("NUM5",  gyo, lineData._shushokuKibouKengaiGakkou);
                svf.VrsOutn("NUM6",  gyo, lineData._shushokuKibouTotalJibun);
                svf.VrsOutn("NUM7",  gyo, lineData._shushokuKibouKennaiJibun);
                svf.VrsOutn("NUM8",  gyo, lineData._shushokuKibouKengaiJibun);
                svf.VrsOutn("NUM9",  gyo, lineData._shushokuKibouTotalKoumuin);
                svf.VrsOutn("NUM10", gyo, lineData._shushokuKibouKennaiKoumuin);
                svf.VrsOutn("NUM11", gyo, lineData._shushokuKibouKengaiKoumuin);
                svf.VrsOutn("NUM12", gyo, lineData._shushokuKibouTotalGoukei);
                svf.VrsOutn("NUM13", gyo, lineData._shushokuKibouKennaiGoukei);
                svf.VrsOutn("NUM14", gyo, lineData._shushokuKibouKengaiGoukei);
                svf.VrsOutn("NUM15", gyo, lineData._sonotaKeikakuAri);
                svf.VrsOutn("NUM16", gyo, lineData._sonotaKeikakuNashi);
                svf.VrsOutn("NUM17", gyo, lineData._shushokuNaiteiTotalGakkou);
                svf.VrsOutn("NUM18", gyo, lineData._shushokuNaiteiKennaiGakkou);
                svf.VrsOutn("NUM19", gyo, lineData._shushokuNaiteiKengaiGakkou);
                svf.VrsOutn("NUM20", gyo, lineData._shushokuNaiteiTotalJibun);
                svf.VrsOutn("NUM21", gyo, lineData._shushokuNaiteiKennaiJibun);
                svf.VrsOutn("NUM22", gyo, lineData._shushokuNaiteiKengaiJibun);
                svf.VrsOutn("NUM23", gyo, lineData._shushokuNaiteiTotalKoumuin);
                svf.VrsOutn("NUM24", gyo, lineData._shushokuNaiteiKennaiKoumuin);
                svf.VrsOutn("NUM25", gyo, lineData._shushokuNaiteiKengaiKoumuin);
                svf.VrsOutn("NUM26", gyo, lineData._shushokuNaiteiTotalGoukei);
                svf.VrsOutn("NUM27", gyo, lineData._shushokuNaiteiKennaiGoukei);
                svf.VrsOutn("NUM28", gyo, lineData._shushokuNaiteiKengaiGoukei);
                svf.VrsOutn("NUM29", gyo, lineData._shingakuShushoku);
                svf.VrsOutn("NUM30", gyo, lineData._shushokuKibouTotalMinaitei);
                svf.VrsOutn("NUM31", gyo, lineData._shushokuKibouKennaiMinaitei);
                svf.VrsOutn("NUM32", gyo, lineData._shushokuKibouKengaiMinaitei);
                svf.VrsOutn("NUM33", gyo, lineData._shingakuIgaiMikketei);
            }
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befMajor = "";
            MajorData majorData = null;

            while (rs.next()) {
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String sex = rs.getString("SEX");
                final String majorName = rs.getString("MAJORNAME");
                final String sexName = rs.getString("SEX_NAME");
                final String zentaiGoukei = rs.getString("ZENTAI_GOUKEI");
                final String shingakuGoukei = rs.getString("SHINGAKU_GOUKEI");
                final String shushokuKibouTotalGakkou = rs.getString("SHUSHOKU_KIBOU_TOTAL_GAKKOU");
                final String shushokuKibouKennaiGakkou = rs.getString("SHUSHOKU_KIBOU_KENNAI_GAKKOU");
                final String shushokuKibouKengaiGakkou = rs.getString("SHUSHOKU_KIBOU_KENGAI_GAKKOU");
                final String shushokuKibouTotalJibun = rs.getString("SHUSHOKU_KIBOU_TOTAL_JIBUN");
                final String shushokuKibouKennaiJibun = rs.getString("SHUSHOKU_KIBOU_KENNAI_JIBUN");
                final String shushokuKibouKengaiJibun = rs.getString("SHUSHOKU_KIBOU_KENGAI_JIBUN");
                final String shushokuKibouTotalKoumuin = rs.getString("SHUSHOKU_KIBOU_TOTAL_KOUMUIN");
                final String shushokuKibouKennaiKoumuin = rs.getString("SHUSHOKU_KIBOU_KENNAI_KOUMUIN");
                final String shushokuKibouKengaiKoumuin = rs.getString("SHUSHOKU_KIBOU_KENGAI_KOUMUIN");
                final String shushokuKibouTotalGoukei = rs.getString("SHUSHOKU_KIBOU_TOTAL_GOUKEI");
                final String shushokuKibouKennaiGoukei = rs.getString("SHUSHOKU_KIBOU_KENNAI_GOUKEI");
                final String shushokuKibouKengaiGoukei = rs.getString("SHUSHOKU_KIBOU_KENGAI_GOUKEI");
                final String sonotaKeikakuAri = rs.getString("SONOTA_KEIKAKU_ARI");
                final String sonotaKeikakuNashi = rs.getString("SONOTA_KEIKAKU_NASHI");
                final String shushokuNaiteiTotalGakkou = rs.getString("SHUSHOKU_NAITEI_TOTAL_GAKKOU");
                final String shushokuNaiteiKennaiGakkou = rs.getString("SHUSHOKU_NAITEI_KENNAI_GAKKOU");
                final String shushokuNaiteiKengaiGakkou = rs.getString("SHUSHOKU_NAITEI_KENGAI_GAKKOU");
                final String shushokuNaiteiTotalJibun = rs.getString("SHUSHOKU_NAITEI_TOTAL_JIBUN");
                final String shushokuNaiteiKennaiJibun = rs.getString("SHUSHOKU_NAITEI_KENNAI_JIBUN");
                final String shushokuNaiteiKengaiJibun = rs.getString("SHUSHOKU_NAITEI_KENGAI_JIBUN");
                final String shushokuNaiteiTotalKoumuin = rs.getString("SHUSHOKU_NAITEI_TOTAL_KOUMUIN");
                final String shushokuNaiteiKennaiKoumuin = rs.getString("SHUSHOKU_NAITEI_KENNAI_KOUMUIN");
                final String shushokuNaiteiKengaiKoumuin = rs.getString("SHUSHOKU_NAITEI_KENGAI_KOUMUIN");
                final String shushokuNaiteiTotalGoukei = rs.getString("SHUSHOKU_NAITEI_TOTAL_GOUKEI");
                final String shushokuNaiteiKennaiGoukei = rs.getString("SHUSHOKU_NAITEI_KENNAI_GOUKEI");
                final String shushokuNaiteiKengaiGoukei = rs.getString("SHUSHOKU_NAITEI_KENGAI_GOUKEI");
                final String shingakuShushoku = rs.getString("SHINGAKU_SHUSHOKU");
                final String shushokuKibouTotalMinaitei = rs.getString("SHUSHOKU_KIBOU_TOTAL_MINAITEI");
                final String shushokuKibouKennaiMinaitei = rs.getString("SHUSHOKU_KIBOU_KENNAI_MINAITEI");
                final String shushokuKibouKengaiMinaitei = rs.getString("SHUSHOKU_KIBOU_KENGAI_MINAITEI");
                final String shingakuIgaiMikketei = rs.getString("SHINGAKU_IGAI_MIKKETEI");
                if (befMajor.equals("")) {
                    majorData = new MajorData(courseCd, majorCd, majorName);
                    retList.add(majorData);
                } else if (!befMajor.equals(courseCd + majorCd)) {
                    majorData = new MajorData(courseCd, majorCd, majorName);
                    retList.add(majorData);
                }
                majorData.setLineData(sex, sexName, zentaiGoukei, shingakuGoukei, shushokuKibouTotalGakkou, shushokuKibouKennaiGakkou, shushokuKibouKengaiGakkou, shushokuKibouTotalJibun, shushokuKibouKennaiJibun, shushokuKibouKengaiJibun, shushokuKibouTotalKoumuin, shushokuKibouKennaiKoumuin, shushokuKibouKengaiKoumuin, shushokuKibouTotalGoukei, shushokuKibouKennaiGoukei, shushokuKibouKengaiGoukei, sonotaKeikakuAri, sonotaKeikakuNashi, shushokuNaiteiTotalGakkou, shushokuNaiteiKennaiGakkou, shushokuNaiteiKengaiGakkou, shushokuNaiteiTotalJibun, shushokuNaiteiKennaiJibun, shushokuNaiteiKengaiJibun, shushokuNaiteiTotalKoumuin, shushokuNaiteiKennaiKoumuin, shushokuNaiteiKengaiKoumuin, shushokuNaiteiTotalGoukei, shushokuNaiteiKennaiGoukei, shushokuNaiteiKengaiGoukei, shingakuShushoku, shushokuKibouTotalMinaitei, shushokuKibouKennaiMinaitei, shushokuKibouKengaiMinaitei, shingakuIgaiMikketei);
                befMajor = courseCd + majorCd;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_MAJOR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.COURSECD , ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T3.MAJORNAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         INNER JOIN MAJOR_MST T3 ");
        stb.append("             ON  T3.COURSECD     = T1.COURSECD ");
        stb.append("            AND  T3.MAJORCD     = T1.MAJORCD ");
        stb.append("     WHERE ");
        stb.append("             T1.YEAR     = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.COURSECD , ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T3.MAJORNAME ");
        stb.append("     ) ");
        stb.append(" , T_SEX (SEX, SEX_NAME) AS ( ");
        stb.append("     SELECT ");
        stb.append("         NAMECD2, ");
        stb.append("         ABBV1 ");
        stb.append("     FROM ");
        stb.append("         NAME_MST ");
        stb.append("     WHERE ");
        stb.append("         NAMECD1 = 'Z002' ");
        stb.append("     UNION ALL ");
        stb.append("     VALUES('9', '合計') ");
        stb.append("     ) ");
        stb.append(" , T_MAIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.*, ");
        stb.append("         T2.* ");
        stb.append("     FROM ");
        stb.append("         T_MAJOR T1, ");
        stb.append("         T_SEX T2 ");
        stb.append("     ) ");
        stb.append(" , T_ADDITION1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         * ");
        stb.append("     FROM ");
        stb.append("         V_AFT_DISEASE_ADDITION420_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("         AND YEAR = '" + _param._year + "' ");
        stb.append("     ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.MAJORNAME, ");
        stb.append("     T1.SEX_NAME, ");
        stb.append("     VALUE(L1.ZENTAI_GOUKEI, 0) AS ZENTAI_GOUKEI, ");
        stb.append("     VALUE(L1.SHINGAKU_GOUKEI, 0) AS SHINGAKU_GOUKEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_GAKKOU, 0) AS SHUSHOKU_KIBOU_TOTAL_GAKKOU, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_GAKKOU, 0) AS SHUSHOKU_KIBOU_KENNAI_GAKKOU, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_GAKKOU, 0) AS SHUSHOKU_KIBOU_KENGAI_GAKKOU, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_JIBUN, 0) AS SHUSHOKU_KIBOU_TOTAL_JIBUN, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_JIBUN, 0) AS SHUSHOKU_KIBOU_KENNAI_JIBUN, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_JIBUN, 0) AS SHUSHOKU_KIBOU_KENGAI_JIBUN, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_KOUMUIN, 0) AS SHUSHOKU_KIBOU_TOTAL_KOUMUIN, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_KOUMUIN, 0) AS SHUSHOKU_KIBOU_KENNAI_KOUMUIN, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_KOUMUIN, 0) AS SHUSHOKU_KIBOU_KENGAI_KOUMUIN, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_GOUKEI, 0) AS SHUSHOKU_KIBOU_TOTAL_GOUKEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_GOUKEI, 0) AS SHUSHOKU_KIBOU_KENNAI_GOUKEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_GOUKEI, 0) AS SHUSHOKU_KIBOU_KENGAI_GOUKEI, ");
        stb.append("     VALUE(L1.SONOTA_KEIKAKU_ARI, 0) AS SONOTA_KEIKAKU_ARI, ");
        stb.append("     VALUE(L1.SONOTA_KEIKAKU_NASHI, 0) AS SONOTA_KEIKAKU_NASHI, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_GAKKOU, 0) AS SHUSHOKU_NAITEI_TOTAL_GAKKOU, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_GAKKOU, 0) AS SHUSHOKU_NAITEI_KENNAI_GAKKOU, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_GAKKOU, 0) AS SHUSHOKU_NAITEI_KENGAI_GAKKOU, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_JIBUN, 0) AS SHUSHOKU_NAITEI_TOTAL_JIBUN, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_JIBUN, 0) AS SHUSHOKU_NAITEI_KENNAI_JIBUN, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_JIBUN, 0) AS SHUSHOKU_NAITEI_KENGAI_JIBUN, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_KOUMUIN, 0) AS SHUSHOKU_NAITEI_TOTAL_KOUMUIN, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_KOUMUIN, 0) AS SHUSHOKU_NAITEI_KENNAI_KOUMUIN, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_KOUMUIN, 0) AS SHUSHOKU_NAITEI_KENGAI_KOUMUIN, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_TOTAL_GOUKEI, 0) AS SHUSHOKU_NAITEI_TOTAL_GOUKEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENNAI_GOUKEI, 0) AS SHUSHOKU_NAITEI_KENNAI_GOUKEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_NAITEI_KENGAI_GOUKEI, 0) AS SHUSHOKU_NAITEI_KENGAI_GOUKEI, ");
        stb.append("     VALUE(L1.SHINGAKU_SHUSHOKU, 0) AS SHINGAKU_SHUSHOKU, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_TOTAL_MINAITEI, 0) AS SHUSHOKU_KIBOU_TOTAL_MINAITEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENNAI_MINAITEI, 0) AS SHUSHOKU_KIBOU_KENNAI_MINAITEI, ");
        stb.append("     VALUE(L1.SHUSHOKU_KIBOU_KENGAI_MINAITEI, 0) AS SHUSHOKU_KIBOU_KENGAI_MINAITEI, ");
        stb.append("     VALUE(L1.SHINGAKU_IGAI_MIKKETEI, 0) AS SHINGAKU_IGAI_MIKKETEI ");
        stb.append(" FROM ");
        stb.append("     T_MAIN T1 ");
        stb.append("     LEFT JOIN T_ADDITION1 L1 ON L1.COURSECD = T1.COURSECD ");
        stb.append("                             AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("                             AND L1.SEX = T1.SEX ");
        stb.append(" ORDER BY ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX ");

        return stb.toString();
    }

    private class MajorData {
        final String _courseCd;
        final String _majorCd;
        final String _majorName;
        final List _injiLineList;
        public MajorData(
                final String courseCd,
                final String majorCd,
                final String majorName
        ) {
            _courseCd       = courseCd;
            _majorCd        = majorCd;
            _majorName      = majorName;
            _injiLineList   = new ArrayList();
        }

        private void setLineData(
                final String sex,
                final String sexName,
                final String zentaiGoukei,
                final String shingakuGoukei,
                final String shushokuKibouTotalGakkou,
                final String shushokuKibouKennaiGakkou,
                final String shushokuKibouKengaiGakkou,
                final String shushokuKibouTotalJibun,
                final String shushokuKibouKennaiJibun,
                final String shushokuKibouKengaiJibun,
                final String shushokuKibouTotalKoumuin,
                final String shushokuKibouKennaiKoumuin,
                final String shushokuKibouKengaiKoumuin,
                final String shushokuKibouTotalGoukei,
                final String shushokuKibouKennaiGoukei,
                final String shushokuKibouKengaiGoukei,
                final String sonotaKeikakuAri,
                final String sonotaKeikakuNashi,
                final String shushokuNaiteiTotalGakkou,
                final String shushokuNaiteiKennaiGakkou,
                final String shushokuNaiteiKengaiGakkou,
                final String shushokuNaiteiTotalJibun,
                final String shushokuNaiteiKennaiJibun,
                final String shushokuNaiteiKengaiJibun,
                final String shushokuNaiteiTotalKoumuin,
                final String shushokuNaiteiKennaiKoumuin,
                final String shushokuNaiteiKengaiKoumuin,
                final String shushokuNaiteiTotalGoukei,
                final String shushokuNaiteiKennaiGoukei,
                final String shushokuNaiteiKengaiGoukei,
                final String shingakuShushoku,
                final String shushokuKibouTotalMinaitei,
                final String shushokuKibouKennaiMinaitei,
                final String shushokuKibouKengaiMinaitei,
                final String shingakuIgaiMikketei
        ) {
            final InjiLineData lineData = new InjiLineData(sex, sexName, zentaiGoukei, shingakuGoukei, shushokuKibouTotalGakkou, shushokuKibouKennaiGakkou, shushokuKibouKengaiGakkou, shushokuKibouTotalJibun, shushokuKibouKennaiJibun, shushokuKibouKengaiJibun, shushokuKibouTotalKoumuin, shushokuKibouKennaiKoumuin, shushokuKibouKengaiKoumuin, shushokuKibouTotalGoukei, shushokuKibouKennaiGoukei, shushokuKibouKengaiGoukei, sonotaKeikakuAri, sonotaKeikakuNashi, shushokuNaiteiTotalGakkou, shushokuNaiteiKennaiGakkou, shushokuNaiteiKengaiGakkou, shushokuNaiteiTotalJibun, shushokuNaiteiKennaiJibun, shushokuNaiteiKengaiJibun, shushokuNaiteiTotalKoumuin, shushokuNaiteiKennaiKoumuin, shushokuNaiteiKengaiKoumuin, shushokuNaiteiTotalGoukei, shushokuNaiteiKennaiGoukei, shushokuNaiteiKengaiGoukei, shingakuShushoku, shushokuKibouTotalMinaitei, shushokuKibouKennaiMinaitei, shushokuKibouKengaiMinaitei, shingakuIgaiMikketei);
            _injiLineList.add(lineData);
        }
    }

    private class InjiLineData {
        final String _sex;
        final String _sexName;
        final String _zentaiGoukei;
        final String _shingakuGoukei;
        final String _shushokuKibouTotalGakkou;
        final String _shushokuKibouKennaiGakkou;
        final String _shushokuKibouKengaiGakkou;
        final String _shushokuKibouTotalJibun;
        final String _shushokuKibouKennaiJibun;
        final String _shushokuKibouKengaiJibun;
        final String _shushokuKibouTotalKoumuin;
        final String _shushokuKibouKennaiKoumuin;
        final String _shushokuKibouKengaiKoumuin;
        final String _shushokuKibouTotalGoukei;
        final String _shushokuKibouKennaiGoukei;
        final String _shushokuKibouKengaiGoukei;
        final String _sonotaKeikakuAri;
        final String _sonotaKeikakuNashi;
        final String _shushokuNaiteiTotalGakkou;
        final String _shushokuNaiteiKennaiGakkou;
        final String _shushokuNaiteiKengaiGakkou;
        final String _shushokuNaiteiTotalJibun;
        final String _shushokuNaiteiKennaiJibun;
        final String _shushokuNaiteiKengaiJibun;
        final String _shushokuNaiteiTotalKoumuin;
        final String _shushokuNaiteiKennaiKoumuin;
        final String _shushokuNaiteiKengaiKoumuin;
        final String _shushokuNaiteiTotalGoukei;
        final String _shushokuNaiteiKennaiGoukei;
        final String _shushokuNaiteiKengaiGoukei;
        final String _shingakuShushoku;
        final String _shushokuKibouTotalMinaitei;
        final String _shushokuKibouKennaiMinaitei;
        final String _shushokuKibouKengaiMinaitei;
        final String _shingakuIgaiMikketei;
        public InjiLineData(
                final String sex,
                final String sexName,
                final String zentaiGoukei,
                final String shingakuGoukei,
                final String shushokuKibouTotalGakkou,
                final String shushokuKibouKennaiGakkou,
                final String shushokuKibouKengaiGakkou,
                final String shushokuKibouTotalJibun,
                final String shushokuKibouKennaiJibun,
                final String shushokuKibouKengaiJibun,
                final String shushokuKibouTotalKoumuin,
                final String shushokuKibouKennaiKoumuin,
                final String shushokuKibouKengaiKoumuin,
                final String shushokuKibouTotalGoukei,
                final String shushokuKibouKennaiGoukei,
                final String shushokuKibouKengaiGoukei,
                final String sonotaKeikakuAri,
                final String sonotaKeikakuNashi,
                final String shushokuNaiteiTotalGakkou,
                final String shushokuNaiteiKennaiGakkou,
                final String shushokuNaiteiKengaiGakkou,
                final String shushokuNaiteiTotalJibun,
                final String shushokuNaiteiKennaiJibun,
                final String shushokuNaiteiKengaiJibun,
                final String shushokuNaiteiTotalKoumuin,
                final String shushokuNaiteiKennaiKoumuin,
                final String shushokuNaiteiKengaiKoumuin,
                final String shushokuNaiteiTotalGoukei,
                final String shushokuNaiteiKennaiGoukei,
                final String shushokuNaiteiKengaiGoukei,
                final String shingakuShushoku,
                final String shushokuKibouTotalMinaitei,
                final String shushokuKibouKennaiMinaitei,
                final String shushokuKibouKengaiMinaitei,
                final String shingakuIgaiMikketei
        ) {
            _sex                            = sex;
            _sexName                        = sexName;
            _zentaiGoukei                   = zentaiGoukei;
            _shingakuGoukei                 = shingakuGoukei;
            _shushokuKibouTotalGakkou       = shushokuKibouTotalGakkou;
            _shushokuKibouKennaiGakkou      = shushokuKibouKennaiGakkou;
            _shushokuKibouKengaiGakkou      = shushokuKibouKengaiGakkou;
            _shushokuKibouTotalJibun        = shushokuKibouTotalJibun;
            _shushokuKibouKennaiJibun       = shushokuKibouKennaiJibun;
            _shushokuKibouKengaiJibun       = shushokuKibouKengaiJibun;
            _shushokuKibouTotalKoumuin      = shushokuKibouTotalKoumuin;
            _shushokuKibouKennaiKoumuin     = shushokuKibouKennaiKoumuin;
            _shushokuKibouKengaiKoumuin     = shushokuKibouKengaiKoumuin;
            _shushokuKibouTotalGoukei       = shushokuKibouTotalGoukei;
            _shushokuKibouKennaiGoukei      = shushokuKibouKennaiGoukei;
            _shushokuKibouKengaiGoukei      = shushokuKibouKengaiGoukei;
            _sonotaKeikakuAri               = sonotaKeikakuAri;
            _sonotaKeikakuNashi             = sonotaKeikakuNashi;
            _shushokuNaiteiTotalGakkou      = shushokuNaiteiTotalGakkou;
            _shushokuNaiteiKennaiGakkou     = shushokuNaiteiKennaiGakkou;
            _shushokuNaiteiKengaiGakkou     = shushokuNaiteiKengaiGakkou;
            _shushokuNaiteiTotalJibun       = shushokuNaiteiTotalJibun;
            _shushokuNaiteiKennaiJibun      = shushokuNaiteiKennaiJibun;
            _shushokuNaiteiKengaiJibun      = shushokuNaiteiKengaiJibun;
            _shushokuNaiteiTotalKoumuin     = shushokuNaiteiTotalKoumuin;
            _shushokuNaiteiKennaiKoumuin    = shushokuNaiteiKennaiKoumuin;
            _shushokuNaiteiKengaiKoumuin    = shushokuNaiteiKengaiKoumuin;
            _shushokuNaiteiTotalGoukei      = shushokuNaiteiTotalGoukei;
            _shushokuNaiteiKennaiGoukei     = shushokuNaiteiKennaiGoukei;
            _shushokuNaiteiKengaiGoukei     = shushokuNaiteiKengaiGoukei;
            _shingakuShushoku               = shingakuShushoku;
            _shushokuKibouTotalMinaitei     = shushokuKibouTotalMinaitei;
            _shushokuKibouKennaiMinaitei    = shushokuKibouKennaiMinaitei;
            _shushokuKibouKengaiMinaitei    = shushokuKibouKengaiMinaitei;
            _shingakuIgaiMikketei           = shingakuIgaiMikketei;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _ctrlSemester;
        private final String _schoolcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schoolcd = request.getParameter("SCHOOLCD");
        }

    }
}

// eof

