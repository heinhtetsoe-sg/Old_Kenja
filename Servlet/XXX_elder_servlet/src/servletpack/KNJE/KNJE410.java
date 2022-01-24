/*
 * $Id: d7945d058bdcf732afdeed46cafa5e27ebf0fe96 $
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

public class KNJE410 {

    private static final Log log = LogFactory.getLog(KNJE410.class);

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
        svf.VrSetForm("KNJE410.frm", 4);
        svf.VrsOut("TITLE", _param._year + "年度　卒業予定者の進路希望状況");
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
                svf.VrsOutn("NUM1", gyo, lineData._shingakuDaigaku);
                svf.VrsOutn("NUM2", gyo, lineData._shingakuTandai);
                svf.VrsOutn("NUM3", gyo, lineData._shingakuSenmon);
                svf.VrsOutn("NUM4", gyo, lineData._shingakuSonota);
                svf.VrsOutn("NUM5", gyo, lineData._shingakuRemark);
                svf.VrsOutn("NUM6", gyo, lineData._shingakuGoukei);
                svf.VrsOutn("NUM7", gyo, lineData._shushokuKennai);
                svf.VrsOutn("NUM8", gyo, lineData._shushokuKennaiKoumuin);
                svf.VrsOutn("NUM9", gyo, lineData._shushokuKengai);
                svf.VrsOutn("NUM10", gyo, lineData._shushokuKengaiKoumuin);
                svf.VrsOutn("NUM11", gyo, lineData._shushokuSonota);
                svf.VrsOutn("NUM12", gyo, lineData._shushokuRemark);
                svf.VrsOutn("NUM13", gyo, lineData._shushokuGoukei);
                svf.VrsOutn("NUM14", gyo, lineData._sonotaGoukei);
                svf.VrsOutn("NUM15", gyo, lineData._zentaiGoukei);
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
                final String shingakuDaigaku = rs.getString("SHINGAKU_DAIGAKU");
                final String shingakuTandai = rs.getString("SHINGAKU_TANDAI");
                final String shingakuSenmon = rs.getString("SHINGAKU_SENMON");
                final String shingakuSonota = rs.getString("SHINGAKU_SONOTA");
                final String shingakuRemark = rs.getString("SHINGAKU_REMARK");
                final String shingakuGoukei = rs.getString("SHINGAKU_GOUKEI");
                final String shushokuKennai = rs.getString("SHUSHOKU_KENNAI");
                final String shushokuKennaiKoumuin = rs.getString("SHUSHOKU_KENNAI_KOUMUIN");
                final String shushokuKengai = rs.getString("SHUSHOKU_KENGAI");
                final String shushokuKengaiKoumuin = rs.getString("SHUSHOKU_KENGAI_KOUMUIN");
                final String shushokuSonota = rs.getString("SHUSHOKU_SONOTA");
                final String shushokuRemark = rs.getString("SHUSHOKU_REMARK");
                final String shushokuGoukei = rs.getString("SHUSHOKU_GOUKEI");
                final String sonotaGoukei = rs.getString("SONOTA_GOUKEI");
                final String zentaiGoukei = rs.getString("ZENTAI_GOUKEI");
                if (befMajor.equals("")) {
                    majorData = new MajorData(courseCd, majorCd, majorName);
                    retList.add(majorData);
                } else if (!befMajor.equals(courseCd + majorCd)) {
                    majorData = new MajorData(courseCd, majorCd, majorName);
                    retList.add(majorData);
                }
                majorData.setLineData(sex, sexName, shingakuDaigaku, shingakuTandai, shingakuSenmon, shingakuSonota, shingakuRemark, shingakuGoukei, shushokuKennai, shushokuKennaiKoumuin, shushokuKengai, shushokuKengaiKoumuin, shushokuSonota, shushokuRemark, shushokuGoukei, sonotaGoukei, zentaiGoukei);
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
        stb.append("         V_AFT_DISEASE_ADDITION410_DAT ");
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
        stb.append("     L1.SHINGAKU_DAIGAKU, ");
        stb.append("     L1.SHINGAKU_TANDAI, ");
        stb.append("     L1.SHINGAKU_SENMON, ");
        stb.append("     L1.SHINGAKU_SONOTA, ");
        stb.append("     L1.SHINGAKU_REMARK, ");
        stb.append("     L1.SHINGAKU_GOUKEI, ");
        stb.append("     L1.SHUSHOKU_KENNAI, ");
        stb.append("     L1.SHUSHOKU_KENNAI_KOUMUIN, ");
        stb.append("     L1.SHUSHOKU_KENGAI, ");
        stb.append("     L1.SHUSHOKU_KENGAI_KOUMUIN, ");
        stb.append("     L1.SHUSHOKU_SONOTA, ");
        stb.append("     L1.SHUSHOKU_REMARK, ");
        stb.append("     L1.SHUSHOKU_GOUKEI, ");
        stb.append("     L1.SONOTA_GOUKEI, ");
        stb.append("     L1.ZENTAI_GOUKEI ");
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
                final String shingakuDaigaku,
                final String shingakuTandai,
                final String shingakuSenmon,
                final String shingakuSonota,
                final String shingakuRemark,
                final String shingakuGoukei,
                final String shushokuKennai,
                final String shushokuKennaiKoumuin,
                final String shushokuKengai,
                final String shushokuKengaiKoumuin,
                final String shushokuSonota,
                final String shushokuRemark,
                final String shushokuGoukei,
                final String sonotaGoukei,
                final String zentaiGoukei
        ) {
            final InjiLineData lineData = new InjiLineData(sex, sexName, shingakuDaigaku, shingakuTandai, shingakuSenmon, shingakuSonota, shingakuRemark, shingakuGoukei, shushokuKennai, shushokuKennaiKoumuin, shushokuKengai, shushokuKengaiKoumuin, shushokuSonota, shushokuRemark, shushokuGoukei, sonotaGoukei, zentaiGoukei);
            _injiLineList.add(lineData);
        }
    }

    private class InjiLineData {
        final String _sex;
        final String _sexName;
        final String _shingakuDaigaku;
        final String _shingakuTandai;
        final String _shingakuSenmon;
        final String _shingakuSonota;
        final String _shingakuRemark;
        final String _shingakuGoukei;
        final String _shushokuKennai;
        final String _shushokuKennaiKoumuin;
        final String _shushokuKengai;
        final String _shushokuKengaiKoumuin;
        final String _shushokuSonota;
        final String _shushokuRemark;
        final String _shushokuGoukei;
        final String _sonotaGoukei;
        final String _zentaiGoukei;
        public InjiLineData(
                final String sex,
                final String sexName,
                final String shingakuDaigaku,
                final String shingakuTandai,
                final String shingakuSenmon,
                final String shingakuSonota,
                final String shingakuRemark,
                final String shingakuGoukei,
                final String shushokuKennai,
                final String shushokuKennaiKoumuin,
                final String shushokuKengai,
                final String shushokuKengaiKoumuin,
                final String shushokuSonota,
                final String shushokuRemark,
                final String shushokuGoukei,
                final String sonotaGoukei,
                final String zentaiGoukei
        ) {
            _sex                    = sex;
            _sexName                = sexName;
            _shingakuDaigaku        = shingakuDaigaku;
            _shingakuTandai         = shingakuTandai;
            _shingakuSenmon         = shingakuSenmon;
            _shingakuSonota         = shingakuSonota;
            _shingakuRemark         = shingakuRemark;
            _shingakuGoukei         = shingakuGoukei;
            _shushokuKennai         = shushokuKennai;
            _shushokuKennaiKoumuin  = shushokuKennaiKoumuin;
            _shushokuKengai         = shushokuKengai;
            _shushokuKengaiKoumuin  = shushokuKengaiKoumuin;
            _shushokuSonota         = shushokuSonota;
            _shushokuRemark         = shushokuRemark;
            _shushokuGoukei         = shushokuGoukei;
            _sonotaGoukei           = sonotaGoukei;
            _zentaiGoukei           = zentaiGoukei;
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

