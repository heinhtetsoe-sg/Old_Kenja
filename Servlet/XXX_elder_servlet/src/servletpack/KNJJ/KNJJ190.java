/*
 * $Id: 24ee3bb599465bd3277cbe9161ba0608014ae9eb $
 *
 * 作成日: 2017/03/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJJ190 {

    private static final Log log = LogFactory.getLog(KNJJ190.class);

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
        svf.VrSetForm("KNJJ190.frm", 1);

        final String setLimitDate = KNJ_EditDate.h_format_S(_param._limitDate, "M/d");
        final String setWeek = "(" + KNJ_EditDate.h_format_W(_param._limitDate) + ")";

        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement psFamily = null;
        ResultSet rsFamily = null;
        PreparedStatement psFamilyGrdDiv1 = null;
        ResultSet rsFamilyGrdDiv1 = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String jhName = rs.getString("JHNAME");
                final String gradeName = rs.getString("GRADE_NAME2");
                final String hrName = rs.getString("HR_CLASS_NAME1");
                final String attendno = rs.getString("ATTENDNO");
                final String guardName = rs.getString("GUARD_NAME");
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String familyNo = rs.getString("FAMILY_NO");
                final String sex = rs.getString("SEX");
                final String schoolj = rs.getString("SCHOOLJ");
                final String schoolh = rs.getString("SCHOOLH");
                final String guardZipcd = rs.getString("GUARD_ZIPCD");
                final String guardAddr1 = rs.getString("GUARD_ADDR1");
                final String guardAddr2 = rs.getString("GUARD_ADDR2");
                final String guardTelno = rs.getString("GUARD_TELNO");
                final String branchName = rs.getString("BRANCHNAME");
                final String sendName = rs.getString("SEND_NAME");
                final String jyukyo = rs.getString("JYUKYO");
                svf.VrsOut("LIMIT_DATE", setLimitDate + setWeek);
                svf.VrsOut("JH_NAME", jhName);
                svf.VrsOut("GRADE", gradeName);
                svf.VrsOut("CLASS_NAME", hrName);
                svf.VrsOut("ATTEND_NO", attendno);
                svf.VrsOut("SCHREG_NO", schregNo);
                final int guardNameLen = getMS932ByteCount(guardName);
                svf.VrsOut("GUARD_NAME" + (guardNameLen > 40 ? "2" : "1"), guardName);
                final int namelen = getMS932ByteCount(name);
                svf.VrsOut("NAME" + (namelen > 40 ? "2" : "1"), name);
                final int kanalen = getMS932ByteCount(nameKana);
                svf.VrsOut("KANA" + (kanalen > 40 ? "2" : "1"), nameKana);
                svf.VrsOut("SEX", sex);
                svf.VrsOut("SCHOOL_NAME1", schoolj);
                svf.VrsOut("SCHOOL_NAME2", schoolh);
                svf.VrsOut("ZIP_NO", guardZipcd);
                svf.VrsOut("ADDR1", guardAddr1);
                svf.VrsOut("ADDR2", guardAddr2);
                svf.VrsOut("TEL_NO", guardTelno);
                svf.VrsOut("BRANCH_NAME", branchName);
                svf.VrsOut("GO_TO_SCHOOL", jyukyo);
                svf.VrsOut("BIRTHDAY", StringUtils.replace(birthday, "-", "/"));
                svf.VrsOut("SEND_NAME", sendName);

                final String sqlFamilyGrdDiv1 = getFamilyGrdDiv1Sql(familyNo);
                psFamilyGrdDiv1 = db2.prepareStatement(sqlFamilyGrdDiv1);
                rsFamilyGrdDiv1 = psFamilyGrdDiv1.executeQuery();
                final String sqlFamily = getFamilySql(familyNo);
                psFamily = db2.prepareStatement(sqlFamily);
                rsFamily = psFamily.executeQuery();
                String family = "";
                String sepFamily = "";
                final List grdFamilyList = new ArrayList();
                while (rsFamilyGrdDiv1.next()) {
                    if (null != rsFamilyGrdDiv1.getString("RELA_SCHREGNO") && rsFamilyGrdDiv1.getString("RELA_SCHREGNO").equals(rs.getString("SCHREGNO"))) {
                        continue;
                    }
                    grdFamilyList.add(rsFamilyGrdDiv1.getString("OCCUPATION") + "　" + namaeOnly(rsFamilyGrdDiv1.getString("RELANAME")));
                }
                while (rsFamily.next()) {
                    if (null != rsFamily.getString("SCHREGNO") && rsFamily.getString("SCHREGNO").equals(rs.getString("SCHREGNO"))) {
                        continue;
                    }
                    
                    if ("".equals(rsFamily.getString("GRD_DIV"))) {
                        family += sepFamily + rsFamily.getString("HR_NAME") + "　" + namaeOnly(rsFamily.getString("NAME"));
                        sepFamily = "、";
                    } else {
                        grdFamilyList.add(rsFamily.getString("HR_NAME") + "　" + namaeOnly(rsFamily.getString("NAME")) + "(" + rsFamily.getString("GRD_TERM") + ")");
                    }
                }
                String grdFamily1 = "";
                String grdFamily2 = "";
                if (grdFamilyList.size() > 0) {
                    grdFamily1 += grdFamilyList.get(0);
                }
                if (grdFamilyList.size() > 1) {
                    grdFamily1 += "、" + grdFamilyList.get(1);
                }
                if (grdFamilyList.size() > 2) {
                    grdFamily2 += grdFamilyList.get(2);
                }
                if (grdFamilyList.size() > 3) {
                    grdFamily2 += "、" + grdFamilyList.get(3);
                }
                svf.VrsOut("BRO_SIS_NAME", family);
                svf.VrsOut("GRD_FAMILY1", grdFamily1);
                svf.VrsOut("GRD_FAMILY2", grdFamily2);

                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.closeQuietly(null, psFamily, rsFamily);
            DbUtils.closeQuietly(null, psFamilyGrdDiv1, rsFamilyGrdDiv1);
        }
    }
    
    /**
     * スペースの後の文字列を返す
     */
    private String namaeOnly(final String name) {
        if (null == name) {
            return "";
        }
        if (-1 == name.indexOf("　") && -1 == name.indexOf(" ")) {
            return name;
        }
        int idx = name.indexOf("　");
        if (-1 == idx) {
            idx = name.indexOf(" ");
        }
        return name.substring(idx + 1);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     A023.ABBV1 AS JHNAME, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.GRADE_NAME2, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     HDAT.HR_CLASS_NAME1, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BRANCH.GUARD_NAME, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     BASED.BASE_REMARK1 AS FAMILY_NO, ");
        stb.append("     Z001.NAME2 AS SEX, ");
        stb.append("     ENT_GRDJ.FINSCHOOLCD, ");
        stb.append("     FSJ.FINSCHOOL_NAME AS SCHOOLJ, ");
        stb.append("     ENT_GRDH.FINSCHOOLCD, ");
        stb.append("     FSH.FINSCHOOL_NAME AS SCHOOLH, ");
        stb.append("     BRANCH.GUARD_ZIPCD, ");
        stb.append("     BRANCH.GUARD_ADDR1, ");
        stb.append("     BRANCH.GUARD_ADDR2, ");
        stb.append("     BRANCH.GUARD_TELNO, ");
        stb.append("     BRANCHM.BRANCHNAME, ");
        stb.append("     BRANCH.SEND_NAME, ");
        stb.append("     VALUE(J008.NAME1, '自宅') AS JYUKYO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST BASED ON BASE.SCHREGNO = BASED.SCHREGNO ");
        stb.append("          AND BASED.BASE_SEQ = '009' ");
        stb.append("     LEFT JOIN NAME_MST Z001 ON Z001.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z001.NAMECD2 ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENT_GRDJ ON REGD.SCHREGNO = ENT_GRDJ.SCHREGNO ");
        stb.append("          AND ENT_GRDJ.SCHOOL_KIND = 'J' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSJ ON ENT_GRDJ.FINSCHOOLCD = FSJ.FINSCHOOLCD ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENT_GRDH ON REGD.SCHREGNO = ENT_GRDH.SCHREGNO ");
        stb.append("          AND ENT_GRDH.SCHOOL_KIND = 'H' ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSH ON ENT_GRDH.FINSCHOOLCD = FSH.FINSCHOOLCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("          AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("          AND GDAT.SCHOOL_KIND = A023.NAME1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ");
        stb.append("          AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("          AND REGD.GRADE = HDAT.GRADE ");
        stb.append("          AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BRANCH_DAT BRANCH ON BRANCH.SCHOOLCD = '000000000000' ");
        stb.append("          AND GDAT.SCHOOL_KIND = BRANCH.SCHOOL_KIND ");
        stb.append("          AND GDAT.YEAR = BRANCH.YEAR ");
        stb.append("          AND REGD.SCHREGNO = BRANCH.SCHREGNO ");
        stb.append("     LEFT JOIN BRANCH_MST BRANCHM ON BRANCH.BRANCHCD = BRANCHM.BRANCHCD ");
        stb.append("     LEFT JOIN NAME_MST J008 ON J008.NAMECD1 = 'J008' ");
        stb.append("          AND BRANCH.RESIDENTCD = J008.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._categoryIsClass)) {
            stb.append("     AND REGD.GRADE IN (" + _param._selectInState + ") ");
        } else {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._selectInState + ") ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    private String getFamilyGrdDiv1Sql(final String familyNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(FAMILY.RELANAME, '') AS RELANAME, ");
        stb.append("     VALUE(FAMILY.RELA_SCHREGNO, '') AS RELA_SCHREGNO, ");
        stb.append("     VALUE(FAMILY.OCCUPATION, '') AS OCCUPATION ");
        stb.append(" FROM ");
        stb.append("     FAMILY_DAT FAMILY ");
        stb.append(" WHERE ");
        stb.append("     FAMILY.FAMILY_NO = '" + familyNo + "' ");
        stb.append("     AND FAMILY.GRD_DIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     FAMILY.RELANO ");
        return stb.toString();
    }

    private String getFamilySql(final String familyNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     VALUE(BASE.GRD_DIV, '') AS GRD_DIV, ");
        stb.append("     VALUE(BASE.GRD_TERM, '') AS GRD_TERM, ");
        stb.append("     BASE.NAME, ");
        stb.append("     HDAT.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     FAMILY_DAT FAMILY ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON FAMILY.RELA_SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("          AND BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ");
        stb.append("          AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("          AND REGD.GRADE = HDAT.GRADE ");
        stb.append("          AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     FAMILY.FAMILY_NO = '" + familyNo + "' ");
        stb.append("     AND FAMILY.RELA_SCHREGNO IS NOT NULL ");
        stb.append(" ), GRD_REGD AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD2.YEAR, ");
        stb.append("     REGD2.SCHREGNO, ");
        stb.append("     MAX(REGD2.SEMESTER) AS SEMESTER ");
        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         MAIN_T.SCHREGNO, ");
        stb.append("         MAX(YEAR) AS YEAR ");
        stb.append("     FROM ");
        stb.append("         MAIN_T ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT REGD ON MAIN_T.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     GROUP BY ");
        stb.append("         MAIN_T.SCHREGNO ");
        stb.append("     ) MAXYEAR ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD2 ON MAXYEAR.SCHREGNO = REGD2.SCHREGNO ");
        stb.append("           AND MAXYEAR.YEAR = REGD2.YEAR ");
        stb.append(" GROUP BY ");
        stb.append("     REGD2.YEAR, ");
        stb.append("     REGD2.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.SCHREGNO, ");
        stb.append("     MAIN_T.GRD_DIV, ");
        stb.append("     MAIN_T.GRD_TERM, ");
        stb.append("     MAIN_T.NAME, ");
        stb.append("     MAIN_T.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" WHERE ");
        stb.append("     MAIN_T.GRD_DIV = '' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.SCHREGNO, ");
        stb.append("     MAIN_T.GRD_DIV, ");
        stb.append("     MAIN_T.GRD_TERM, ");
        stb.append("     MAIN_T.NAME, ");
        stb.append("     HDAT.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append("     INNER JOIN GRD_REGD ON MAIN_T.SCHREGNO = GRD_REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON GRD_REGD.YEAR = REGD.YEAR ");
        stb.append("          AND GRD_REGD.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND GRD_REGD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ");
        stb.append("          AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("          AND REGD.GRADE = HDAT.GRADE ");
        stb.append("          AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     MAIN_T.GRD_DIV != '' ");
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
        private final String _semester;
        private final String _categoryIsClass;
        private final String _limitDate;
        private final String[] _classSelected;
        private String _selectInState;
        private final String _grade;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _usecurriculumcd;
        private final String _useschoolKindfield;
        private final String _schoolcd;
        private final String _schoolKind;
        private final String _printLogStaffcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _limitDate = request.getParameter("LIMIT_DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            String sep = "";
            _selectInState = "";
            for (int i = 0; i < _classSelected.length; i++) {
                _selectInState += sep + "'" + _classSelected[i] + "'";
                sep = ",";
            }
            _grade = request.getParameter("GRADE");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
        }

    }
}

// eof

