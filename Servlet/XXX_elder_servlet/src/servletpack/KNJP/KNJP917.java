/*
 * $Id: 9d4cad69c475e62c7c55c941ee2adc2e91fe8708 $
 *
 * 作成日: 2020/02/26
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP917 {

    private static final Log log = LogFactory.getLog(KNJP917.class);

    private final String SORT_HR_CLASS = "1";

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
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final SchregData sch = (SchregData) iterator.next();

            //表フォーム
            svf.VrSetForm("KNJP917_1.frm", 1);

            //出力日
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
            //学籍番号
            final String attendNoZen = KNJ_EditEdit.convertZenkakuSuuji((sch._attendNo).replaceFirst("^0+", ""));
            final String nenkumiban =  sch._hrName + attendNoZen + "番";
            svf.VrsOut("HR_NAME", nenkumiban);
            //氏名
            final String name = sch._name + "　保護者　様";
            final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 44 ? "_3": KNJ_EditEdit.getMS932ByteLength(name) > 34 ? "_2": "_1";
            svf.VrsOut("NAME1" + nameField, name);

            //学校名
            svf.VrsOut("SCHOOLNAME", _param._certifSchool._schoolName);
            //校長名
            svf.VrsOut("STAFF_NAME1", _param._certifSchool._principalName);

            //タイトル
            svf.VrsOut("TITLE", _param._titeleOmote);
            //文章
            final String formatMoney1 = new DecimalFormat("###,###,###,###").format(sch._beneMaxMoney);
            String writeDoc = StringUtils.replace(_param._textOmote, "MONEY1", formatMoney1);
            final String formatMoney2 = new DecimalFormat("###,###,###,###").format(sch._kyufuTotal);
            writeDoc = StringUtils.replace(writeDoc, "MONEY2", formatMoney2);
            String[] token = KNJ_EditEdit.get_token(writeDoc, 90, 15);
            if (token != null) {
                for (int kk = 0; kk < token.length; kk++) {
                    svf.VrsOutn("TEXT", kk + 1, token[kk]);
                }
            }

            //下段３項目
            svf.VrsOut("INQUIRY1", _param._certifSchool._remark1);
            svf.VrsOut("INQUIRY2", _param._certifSchool._remark2);
            svf.VrsOut("INQUIRY3", _param._certifSchool._remark3);

            svf.VrEndPage();

            //裏フォーム（詳細）
            svf.VrSetForm("KNJP917_2.frm", 4);

            //細目データ取得
            final List saimokuList = getSaimokou(db2, sch._schregNo);

            //出力日
            svf.VrsOut("HR_NAME", nenkumiban);
            //学籍番号
            svf.VrsOut("NAME", sch._name + "　保護者　様");

            int kyufuTotal = 0;
            //細目一覧
            for (Iterator it2 = saimokuList.iterator(); it2.hasNext();) {
                Saimoku saimoku = (Saimoku) it2.next();

                svf.VrsOut("ITEM1", saimoku._mName); //項目名
                svf.VrsOut("ITEM2", saimoku._sName); //細目名
                svf.VrsOut("MONEY1", String.valueOf(saimoku._kyufuMoney)); //給付金額

                kyufuTotal += saimoku._kyufuMoney;
                svf.VrEndRecord();
            }
            //給付金合計
            svf.VrsOut("MONEY2", String.valueOf(kyufuTotal));
            svf.VrEndRecord();

            //タイトル
            svf.VrsOut("TITLE", _param._titeleUra);
            //文章
            String[] tokenUra = KNJ_EditEdit.get_token(_param._textUra, 90, 15);
            if (tokenUra != null) {
                for (int kk = 0; kk < tokenUra.length; kk++) {
                    svf.VrsOut("NOTE", tokenUra[kk]);
                    svf.VrEndRecord();
                }
            }

            _hasData = true;
        }
    }

    private class Saimoku {
        final String _mName;
        final String _sName;
        final int _kyufuMoney;
        public Saimoku(
                final String mName,
                final String sName,
                final int  kyufuMoney
                ) {
            _mName      = mName;
            _sName      = sName;
            _kyufuMoney = kyufuMoney;
        }
    }

    private List getSaimokou(final DB2UDB db2, final String schregno) {
        List retList = new ArrayList();
        String sql = getOutGoSaimokouSql(schregno);
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String mName      = rs.getString("LEVY_M_NAME");
                final String sName      = rs.getString("LEVY_S_NAME");
                final int kyufuMoney = rs.getInt("OUTGO_MONEY");

                Saimoku saimoku = new Saimoku(mName, sName, kyufuMoney);
                retList.add(saimoku);
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retList;
    }

    private String getOutGoSaimokouSql(final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     OSCH.OUTGO_L_CD, ");
        stb.append("     OSCH.OUTGO_M_CD, ");
        stb.append("     OSCH.OUTGO_S_CD, ");
        stb.append("     MMST.LEVY_M_NAME, ");
        stb.append("     SMST.LEVY_S_NAME, ");
        stb.append("     OSCH.OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                          AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     LEFT JOIN LEVY_M_MST MMST ON MMST.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                              AND MMST.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                              AND MMST.YEAR        = OSCH.YEAR ");
        stb.append("                              AND MMST.LEVY_L_CD   = OSCH.OUTGO_L_CD ");
        stb.append("                              AND MMST.LEVY_M_CD   = OSCH.OUTGO_M_CD ");
        stb.append("     LEFT JOIN LEVY_S_MST SMST ON SMST.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                              AND SMST.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                              AND SMST.YEAR        = OSCH.YEAR ");
        stb.append("                              AND SMST.LEVY_L_CD   = OSCH.OUTGO_L_CD ");
        stb.append("                              AND SMST.LEVY_M_CD   = OSCH.OUTGO_M_CD ");
        stb.append("                              AND SMST.LEVY_S_CD   = OSCH.OUTGO_S_CD ");
        stb.append(" WHERE ");
        stb.append("         OSCH.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND OSCH.YEAR        = '" + _param._year + "' ");
        stb.append("     AND OSCH.SCHREGNO    = '" + schregNo + "' ");
        stb.append("     AND OUTG.INCOME_L_CD = '98' ");
        stb.append("     AND OUTG.INCOME_M_CD = '98' ");
        stb.append(" ORDER BY ");
        stb.append("     OSCH.OUTGO_L_CD, ");
        stb.append("     OSCH.OUTGO_M_CD, ");
        stb.append("     OSCH.OUTGO_S_CD ");

        return stb.toString();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String zipCd          = rs.getString("GUARD_ZIPCD");
                final String addr1          = rs.getString("GUARD_ADDR1");
                final String addr2          = rs.getString("GUARD_ADDR2");
                final String hrName         = rs.getString("HR_NAME");
                final String attendNo       = rs.getString("ATTENDNO");
                final String schregNo       = rs.getString("SCHREGNO");
                final String name           = rs.getString("NAME");
                final String guardName      = rs.getString("GUARD_NAME");
                final int beneMaxMoney     = rs.getInt("BENEFIT_MONEY");
                final int kyufuTotal       = rs.getInt("KYUFU_TOTAL");

                final SchregData schData = new SchregData(zipCd, addr1, addr2, hrName, attendNo, schregNo, name, guardName, beneMaxMoney, kyufuTotal);
                retList.add(schData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KYUFU_OUTGO AS ( ");
        stb.append("     SELECT ");
        stb.append("         OSCH.SCHREGNO, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         sum(OSCH.OUTGO_MONEY) as KYUFU_TOTAL ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                              AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                              AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                              AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                              AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                              AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             OSCH.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("         AND OSCH.YEAR        = '" + _param._year + "' ");
        stb.append("         AND OSCH.SCHREGNO    in " + _param._schregNoIn + " ");
        stb.append("         AND OUTG.INCOME_L_CD = '98' "); //給付伝票
        stb.append("         AND OUTG.INCOME_M_CD = '98' ");
        stb.append("     GROUP BY ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OSCH.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BENE.SCHREGNO, ");
        stb.append("     GUARD.GUARD_ZIPCD, ");
        stb.append("     value(GUARD.GUARD_ADDR1, '') AS GUARD_ADDR1, ");
        stb.append("     value(GUARD.GUARD_ADDR2, '') AS GUARD_ADDR2, ");
        stb.append("     GUARD.GUARD_NAME, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     value(BENE.BENEFIT_MONEY, '0') as BENEFIT_MONEY, ");
        stb.append("     value(KYUF.KYUFU_TOTAL, '0') as KYUFU_TOTAL ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_BENEFIT_SCHREG_DAT BENE ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = BENE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = BENE.SCHREGNO ");
        stb.append("                                   AND REGD.YEAR     = CASE WHEN VALUE(BASE.GRD_DIV, '4') <> '4' THEN BENE.YEAR ELSE '" + _param._semeMstYear + "' END ");
        stb.append("                                   AND REGD.SEMESTER = CASE WHEN VALUE(BASE.GRD_DIV, '4') <> '4' THEN '" + _param._ctrlSemester + "' ELSE '" + _param._semeMstSemester + "' END ");
        stb.append("     LEFT JOIN KYUFU_OUTGO KYUF ON KYUF.SCHREGNO    = BENE.SCHREGNO ");
        stb.append("                               AND KYUF.SCHOOL_KIND = BENE.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                                    AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("                                    AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                                    AND HDAT.HR_ClASS = REGD.HR_ClASS ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARD ON GUARD.SCHREGNO = BENE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("         BENE.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND BENE.YEAR     = '" + _param._year + "' ");
        stb.append("     AND BENE.SCHREGNO in " + _param._schregNoIn + " ");
        stb.append(" ORDER BY ");
        if (SORT_HR_CLASS.equals(_param._sortFlg)) {
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_ClASS, ");
            stb.append("     REGD.ATTENDNO ");
        } else {
            stb.append("     BENE.SCHREGNO ");
        }

        return stb.toString();
    }

    private class SchregData {
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _hrName;
        final String _attendNo;
        final String _schregNo;
        final String _name;
        final String _guardName;
        final int _beneMaxMoney;
        final int _kyufuTotal;
        public SchregData(
                final String zipCd,
                final String addr1,
                final String addr2,
                final String hrName,
                final String attendNo,
                final String schregNo,
                final String name,
                final String guardName,
                final int beneMaxMoney,
                final int kyufuTotal
        ) {
            _zipCd          = zipCd;
            _addr1          = addr1;
            _addr2          = addr2;
            _hrName         = hrName;
            _attendNo       = attendNo;
            _schregNo       = schregNo;
            _name           = name;
            _guardName      = guardName;
            _beneMaxMoney   = beneMaxMoney;
            _kyufuTotal     = kyufuTotal;
        }
    }

    //証明書学校データ
    private class CertifSchool {
        final String _schoolName;
        final String _principalName;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        public CertifSchool(
                final String schoolName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark3
        ) {
            _schoolName     = schoolName;
            _principalName  = principalName;
            _remark1        = remark1;
            _remark2        = remark2;
            _remark3        = remark3;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74855 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String[] _schList;
        private final String _schregNoIn;
        private final String _schoolCd;
        private final String _ctrlSemester;
        private final String _printDate;
        private final String _sortFlg;
        final CertifSchool _certifSchool;
        private String _titeleOmote;
        private String _textOmote;
        private String _titeleUra;
        private String _textUra;
        private String _semeMstYear;
        private String _semeMstSemester;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year           = request.getParameter("YEAR");
            _schList        = StringUtils.split(request.getParameter("sendPrintList"), '-');
            _schoolCd       = request.getParameter("SCHOOLCD");
            _ctrlSemester   = request.getParameter("CTRL_SEMESTER");
            _printDate      = request.getParameter("PRINT_DATE");
            _sortFlg        = request.getParameter("sortFlg");
            _certifSchool   = getCertifSchool(db2);

            String inState = "('";
            String sep = "";
            for (int i = 0; i < _schList.length; i++) {
                inState += sep + _schList[i];
                sep = "', '";
            }
            inState += "')";
            _schregNoIn = inState;

            loadDocumentMst(db2);
            loadSemesterMst(db2);
       }

        /** 証明書学校データ */
        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool(null, null, null, null, null);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '148' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String principalName  = rs.getString("PRINCIPAL_NAME");
                    final String remark1        = rs.getString("REMARK1");
                    final String remark2        = rs.getString("REMARK2");
                    final String remark3        = rs.getString("REMARK3");
                    certifSchool = new CertifSchool(schoolName, principalName, remark1, remark2, remark3);
                }
            } catch (SQLException ex) {
                log.debug("getCertif exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

        /** 文面マスタ
         * @throws SQLException */
        private void loadDocumentMst(final DB2UDB db2) throws SQLException {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     DOCUMENTCD, ");
            stb.append("     TITLE, ");
            stb.append("     TEXT ");
            stb.append(" FROM ");
            stb.append("     DOCUMENT_MST ");
            stb.append(" WHERE ");
            stb.append("     DOCUMENTCD in ('C2', 'C3') ");

            String sqlCertifSchool = stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlCertifSchool);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("C2".equals(rs.getString("DOCUMENTCD"))) {
                        _titeleOmote    = rs.getString("TITLE");
                        _textOmote      = rs.getString("TEXT");
                    } else {
                        _titeleUra    = rs.getString("TITLE");
                        _textUra      = rs.getString("TEXT");
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 学期マスタ
         * @throws SQLException */
        private void loadSemesterMst(final DB2UDB db2) throws SQLException {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     '" + _printDate.replace("/", "-") + "' BETWEEN SDATE AND EDATE ");
            stb.append("     AND SEMESTER <> '9' ");
            stb.append(" ORDER BY ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER ");

            String semesterSql = stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;

            _semeMstYear = _year;
            _semeMstSemester = _ctrlSemester;
            try {
                ps = db2.prepareStatement(semesterSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semeMstYear        = rs.getString("YEAR");
                    _semeMstSemester    = rs.getString("SEMESTER");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}

// eof
