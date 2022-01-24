/*
 * $Id: ccee644183c6cfc5bbd2b959a3859d0890044851 $
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

public class KNJE410A {

    private static final Log log = LogFactory.getLog(KNJE410A.class);

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
        printCover(svf);

        if ("1".equals(_param._dataDiv)) {
            svf.VrSetForm("KNJE410.frm", 4);
            printOut(db2, svf, null, "");
        } else {
            svf.VrSetForm("KNJE410A.frm", 4);
            for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
                EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
                printOut(db2, svf, edboardSchool, "1_");
            }
        }
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final EdboardSchool edboardSchool, final String fieldName) {
        svf.VrsOut("TITLE", _param._year + "年度　卒業予定者の進路希望状況");
        svf.VrsOut("DATE", "印刷日：" + _param._ctrlDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate));
        final List printList = getList(db2, edboardSchool);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (null != edboardSchool) {
                svf.VrsOut("SCHOOL_NAME1_1",  edboardSchool._schoolName);
            }
            MajorData majorData = (MajorData) iterator.next();
            final String majorSoeji = getMS932ByteLength(majorData._majorName) > 14 ? "2_1" : "";
            svf.VrsOut("MAJOR_NAME" + majorSoeji, majorData._majorName);
            int gyo = 1;
            for (Iterator itLine = majorData._injiLineList.iterator(); itLine.hasNext();) {
                final InjiLineData lineData = (InjiLineData) itLine.next();

                gyo = "2".equals(lineData._sex) ? 2 : gyo;
                gyo = "9".equals(lineData._sex) ? 3 : gyo;
                svf.VrsOutn("NUM" + fieldName + "1", gyo, lineData._shingakuDaigaku);
                svf.VrsOutn("NUM" + fieldName + "2", gyo, lineData._shingakuTandai);
                svf.VrsOutn("NUM" + fieldName + "3", gyo, lineData._shingakuSenmon);
                svf.VrsOutn("NUM" + fieldName + "4", gyo, lineData._shingakuSonota);
                svf.VrsOutn("NUM" + fieldName + "5", gyo, lineData._shingakuRemark);
                svf.VrsOutn("NUM" + fieldName + "6", gyo, lineData._shingakuGoukei);
                svf.VrsOutn("NUM" + fieldName + "7", gyo, lineData._shushokuKennai);
                svf.VrsOutn("NUM" + fieldName + "8", gyo, lineData._shushokuKennaiKoumuin);
                svf.VrsOutn("NUM" + fieldName + "9", gyo, lineData._shushokuKengai);
                svf.VrsOutn("NUM" + fieldName + "10", gyo, lineData._shushokuKengaiKoumuin);
                svf.VrsOutn("NUM" + fieldName + "11", gyo, lineData._shushokuSonota);
                svf.VrsOutn("NUM" + fieldName + "12", gyo, lineData._shushokuRemark);
                svf.VrsOutn("NUM" + fieldName + "13", gyo, lineData._shushokuGoukei);
                svf.VrsOutn("NUM" + fieldName + "14", gyo, lineData._sonotaGoukei);
                svf.VrsOutn("NUM" + fieldName + "15", gyo, lineData._zentaiGoukei);
            }
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void printCover(final Vrw32alp svf) {
        svf.VrSetForm("KNJE_COVER.frm", 1);
        svf.VrsOut("TITLE", _param._year + "年度　卒業予定者の進路希望状況");
        svf.VrsOut("DATE", "印刷日：" + _param._ctrlDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate));
        int fieldName = 1;
        int hyousiGyo = 1;
        for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
            if (hyousiGyo > 30) {
                hyousiGyo = 1;
                fieldName++;
            }
            EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
            svf.VrsOutn("SCHOOL_NAME" + fieldName, hyousiGyo, edboardSchool._schoolName);
            svf.VrsOutn("DATE" + fieldName, hyousiGyo, edboardSchool._executeDate);
            hyousiGyo++;
        }
        svf.VrEndPage();
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

    private List getList(final DB2UDB db2, final EdboardSchool edboardSchool) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql(edboardSchool);
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

    private String sql(final EdboardSchool edboardSchool) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_MAJOR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.COURSECD , ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T3.MAJORNAME ");
        stb.append("     FROM ");
        stb.append("         V_AFT_DISEASE_ADDITION410_DAT T1 ");
        stb.append("         INNER JOIN MAJOR_MST T3 ");
        stb.append("             ON  T3.COURSECD     = T1.COURSECD ");
        stb.append("            AND  T3.MAJORCD     = T1.MAJORCD ");
        stb.append("     WHERE ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("         T1.EDBOARD_SCHOOLCD IN (" + _param._schoolInState + ") ");
        } else {
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' ");
        }
        stb.append("         AND T1.YEAR = '" + _param._year + "' ");
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
        if ("1".equals(_param._dataDiv)) {
            stb.append("         EDBOARD_SCHOOLCD IN (" + _param._schoolInState + ") ");
        } else {
            stb.append("         EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' ");
        }
        stb.append("         AND YEAR = '" + _param._year + "' ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.MAJORNAME, ");
        stb.append("     T1.SEX_NAME, ");
        stb.append("     SUM(L1.SHINGAKU_DAIGAKU) AS SHINGAKU_DAIGAKU, ");
        stb.append("     SUM(L1.SHINGAKU_TANDAI) AS SHINGAKU_TANDAI, ");
        stb.append("     SUM(L1.SHINGAKU_SENMON) AS SHINGAKU_SENMON, ");
        stb.append("     SUM(L1.SHINGAKU_SONOTA) AS SHINGAKU_SONOTA, ");
        stb.append("     SUM(L1.SHINGAKU_REMARK) AS SHINGAKU_REMARK, ");
        stb.append("     SUM(L1.SHINGAKU_GOUKEI) AS SHINGAKU_GOUKEI, ");
        stb.append("     SUM(L1.SHUSHOKU_KENNAI) AS SHUSHOKU_KENNAI, ");
        stb.append("     SUM(L1.SHUSHOKU_KENNAI_KOUMUIN) AS SHUSHOKU_KENNAI_KOUMUIN, ");
        stb.append("     SUM(L1.SHUSHOKU_KENGAI) AS SHUSHOKU_KENGAI, ");
        stb.append("     SUM(L1.SHUSHOKU_KENGAI_KOUMUIN) AS SHUSHOKU_KENGAI_KOUMUIN, ");
        stb.append("     SUM(L1.SHUSHOKU_SONOTA) AS SHUSHOKU_SONOTA, ");
        stb.append("     SUM(L1.SHUSHOKU_REMARK) AS SHUSHOKU_REMARK, ");
        stb.append("     SUM(L1.SHUSHOKU_GOUKEI) AS SHUSHOKU_GOUKEI, ");
        stb.append("     SUM(L1.SONOTA_GOUKEI) AS SONOTA_GOUKEI, ");
        stb.append("     SUM(L1.ZENTAI_GOUKEI) AS ZENTAI_GOUKEI ");
        stb.append(" FROM ");
        stb.append("     T_MAIN T1 ");
        stb.append("     LEFT JOIN T_ADDITION1 L1 ON L1.COURSECD = T1.COURSECD ");
        stb.append("                             AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("                             AND L1.SEX = T1.SEX ");
        stb.append(" GROUP BY ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.MAJORNAME, ");
        stb.append("     T1.SEX_NAME ");
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
        private final String _dataDiv;
        private final List _schoolList;
        private String _schoolInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            String schoolCd[] = request.getParameterValues("CATEGORY_SELECTED");
            _dataDiv = request.getParameter("DATA_DIV");
            _schoolList = getSchoolList(db2, schoolCd);
        }

        private List getSchoolList(final DB2UDB db2, final String[] schoolCd) throws SQLException {
            final List retList = new ArrayList();
            _schoolInState = "";
            String sep = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            for (int ia = 0 ; ia < schoolCd.length ; ia++) {
                final String setSchoolCd = schoolCd[ia].substring(1);
                final String schoolSql = getSchoolSql(setSchoolCd);
                try {
                    _schoolInState += sep + "'" + setSchoolCd + "'";
                    sep = ",";
                    ps = db2.prepareStatement(schoolSql);
                    rs = ps.executeQuery();
                    String schoolName = "";
                    String executeDate = "";
                    while (rs.next()) {
                        schoolName = rs.getString("EDBOARD_SCHOOLNAME");
                        executeDate = rs.getString("EXECUTE_DATE");
                    }
                    final EdboardSchool edboardSchool = new EdboardSchool(setSchoolCd, schoolName, executeDate);
                    retList.add(edboardSchool);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
                _schoolInState = "".equals(_schoolInState) ? "''" : _schoolInState;
            }
            return retList;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EDBOARD_SCHOOLNAME, ");
            stb.append("     MAX(L1.EXECUTE_DATE) AS EXECUTE_DATE ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST T1 ");
            stb.append("     LEFT JOIN REPORT_AFT_DISEASE_ADDITION410_DAT L1 ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ");
            stb.append("          AND L1.YEAR = '" + _year + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.EDBOARD_SCHOOLNAME ");

            return stb.toString();
        }

    }

    private class EdboardSchool {
        private final String _schoolCd;
        private final String _schoolName;
        private final String _executeDate;
        public EdboardSchool(
            final String schoolCd,
            final String schoolName,
            final String executeDate
        ) {
            _schoolCd = schoolCd;
            _schoolName = schoolName;
            _executeDate = null != executeDate && !"".equals(executeDate) ? executeDate.replace('-', '/') : "";
        }
    }
}

// eof

