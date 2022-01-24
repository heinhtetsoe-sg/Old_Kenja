/*
 * $Id: 76677af8c4d69ed1f315a6f24407fe72043321a3 $
 *
 * 作成日: 2017/03/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


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

public class KNJJ193 {

    private static final Log log = LogFactory.getLog(KNJJ193.class);

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
        svf.VrSetForm("KNJJ193.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql();

            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxRowCnt = 7;
            final int maxColCnt = 2;
            int rowCnt = Integer.parseInt(_param._sRow);
            int colCnt = Integer.parseInt(_param._sCol);
            while (rs.next()) {
                final String guardZipcd = rs.getString("GUARD_ZIPCD");
                final String guardAddr1 = rs.getString("GUARD_ADDR1");
                final String guardAddr2 = rs.getString("GUARD_ADDR2");
                final String name = rs.getString("NAME");
                final String hrNameabbv = rs.getString("HR_NAMEABBV");
                final String attendno = rs.getString("ATTENDNO");

                if (colCnt > maxColCnt) {
                    colCnt = 1;
                    rowCnt++;
                }
                if (rowCnt > maxRowCnt) {
                    svf.VrEndPage();
                    colCnt = 1;
                    rowCnt = 1;
                }
                svf.VrsOutn("ZIPCODE" + colCnt, rowCnt, guardZipcd);
                final int addr1Len = getMS932ByteCount(guardAddr1);
                svf.VrsOutn("ADDRESS"  + colCnt + "_1_" + (addr1Len > 50 ? "3" : addr1Len > 40 ? "2" : "1"), rowCnt, guardAddr1);
                final int addr2Len = getMS932ByteCount(guardAddr2);
                svf.VrsOutn("ADDRESS"  + colCnt + "_2_" + (addr2Len > 50 ? "3" : addr2Len > 40 ? "2" : "1"), rowCnt, guardAddr2);
                svf.VrsOutn("NAME" + colCnt + "_1", rowCnt, name);
                svf.VrsOutn("NAME" + colCnt + "_2", rowCnt, hrNameabbv + "-" + attendno);
                colCnt++;
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

    private String getTyoushi() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" , TAISYOU_FAMILY AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     FAMILY_NO ");
        stb.append(" FROM ");
        stb.append("     FAMILY_DAT ");
        stb.append(" WHERE ");
        stb.append("     VALUE(TYOUSHI_FLG, '0') = '1' ");
        stb.append(" ), TYOUSHI_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T2.RELA_SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     TAISYOU_FAMILY T1, ");
        stb.append("     FAMILY_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.FAMILY_NO = T2.FAMILY_NO ");
        stb.append("     AND VALUE(T2.TYOUSHI_FLG, '0') <> '1' ");
        stb.append("     AND T2.RELA_SCHREGNO IS NOT NULL ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getSql() {
        String widthSql = "";
        if ("1".equals(_param._dataDiv)) {
            widthSql = getGradeSql();
        } else if ("2".equals(_param._dataDiv)) {
            widthSql = getHrSql();
        } else if ("3".equals(_param._dataDiv)) {
            widthSql = getKojinSql();
        } else if ("4".equals(_param._dataDiv)) {
            widthSql = getBranchSql();
        } else if ("5".equals(_param._dataDiv)) {
            widthSql = getJitakuSql();
        } else if ("6".equals(_param._dataDiv)) {
            widthSql = getRyouSql();
        } else if ("7".equals(_param._dataDiv)) {
            widthSql = getGeshukuSql();
        } else if ("8".equals(_param._dataDiv)) {
            widthSql = getRyouGesyukuSql();
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(widthSql);
        if ("1".equals(_param._tyoushiFlg)) {
            stb.append(getTyoushi());
        }
        stb.append(" SELECT ");
        stb.append("     BRANCH.GUARD_ZIPCD, ");
        stb.append("     BRANCH.GUARD_ADDR1, ");
        stb.append("     BRANCH.GUARD_ADDR2, ");
        stb.append("     CASE WHEN BRANCH.SEND_NAME IS NOT NULL ");
        stb.append("          THEN BRANCH.SEND_NAME ");
        stb.append("          ELSE BRANCH.GUARD_NAME ");
        stb.append("     END AS NAME, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BRANCH_DAT BRANCH ");
        stb.append("     INNER JOIN TAISYOU ON BRANCH.SCHREGNO = TAISYOU.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON BRANCH.YEAR = REGD.YEAR ");
        stb.append("           AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND BRANCH.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ");
        stb.append("          AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("          AND REGD.GRADE = HDAT.GRADE ");
        stb.append("          AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("          AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON REGD.SCHREGNO = ENTGRD.SCHREGNO ");
        stb.append("          AND GDAT.SCHOOL_KIND = ENTGRD.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     BRANCH.YEAR = '" + _param._ctrlYear + "' ");
        if ("1".equals(_param._tyoushiFlg)) {
            stb.append("     AND NOT EXISTS(SELECT 'x' FROM TYOUSHI_T WHERE BRANCH.SCHREGNO = TYOUSHI_T.RELA_SCHREGNO) ");
        }
        if (!"1".equals(_param._idouFlg)) {
            stb.append("     AND (ENTGRD.GRD_DIV IS NULL ");
            stb.append("       OR ENTGRD.GRD_DIV = '4' ");
            stb.append("       OR ENTGRD.GRD_DIV IS NOT NULL AND '" + _param._idouDate + "' <= ENTGRD.GRD_DATE ");
            stb.append("         ) ");
            stb.append("     AND NOT EXISTS(SELECT 'X' ");
            stb.append("                    FROM SCHREG_TRANSFER_DAT ");
            stb.append("                    WHERE SCHREGNO = REGD.SCHREGNO ");
            stb.append("                      AND '" + _param._idouDate + "' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
            stb.append("                    ) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    private String getGradeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.GRADE IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getHrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.GRADE || '-' || REGD.HR_CLASS IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getKojinSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getBranchSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     BRANCH.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BRANCH_DAT BRANCH ");
        stb.append(" WHERE ");
        stb.append("     BRANCH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND BRANCH.BRANCHCD IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getJitakuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getRyouSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getGeshukuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private String getRyouGesyukuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TAISYOU AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN (" + _param._selectInState + ") ");
        stb.append(" ) ");
        return stb.toString();
    }

    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
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
        final String _semester;
        final String _dataDiv;
        final String _tyoushiFlg;
        final String _idouFlg;
        final String _idouDate;
        final String _grade;
        final String[] _classSelected;
        private String _selectInState;
        final String _sRow;
        final String _sCol;
        final String _prgid;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _usecurriculumcd;
        final String _useschoolKindfield;
        final String _schoolcd;
        final String _schoolKind;
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            //1:学年 2:クラス 3:個人 4:支部 5:自宅生 6:寮生 7:下宿生 8:寮生/下宿生
            _dataDiv = request.getParameter("DATA_DIV");
            _tyoushiFlg = request.getParameter("TYOUSHI_FLG");
            _idouFlg = request.getParameter("IDOU_FLG");
            _idouDate = StringUtils.replace(request.getParameter("IDOU_DATE"), "/", "-");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            String sep = "";
            _selectInState = "";
            for (int i = 0; i < _classSelected.length; i++) {
                _selectInState += sep + "'" + _classSelected[i] + "'";
                sep = ",";
            }
            _sRow = request.getParameter("S_ROW");
            _sCol = request.getParameter("S_COL");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("LOGIN_YEAR");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolcd = request.getParameter("LOGIN_SCHOOLCD");
            _schoolKind = request.getParameter("LOGIN_SCHOOL_KIND");
        }

    }

}
// eof
