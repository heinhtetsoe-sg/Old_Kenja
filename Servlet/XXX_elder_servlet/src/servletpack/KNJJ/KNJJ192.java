/*
 * $Id: 6f644076d2f7681c6919d56abfba008ece04558c $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJJ192 {

    private static final Log log = LogFactory.getLog(KNJJ192.class);

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
        svf.VrSetForm("KNJJ192.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement psFamily = null;
        ResultSet rsFamily = null;
        final int maxLine = 45;
        int lineCnt = 1;
        String befHr = "";

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String gradeHr = rs.getString("GRADEHR");
                final String attendNo = rs.getString("ATTENDNO");
                final String hrName = rs.getString("HR_NAME");
                final String staffname = rs.getString("STAFFNAME");
                final String stafftelno = rs.getString("STAFFTELNO");
                final String guardName = rs.getString("GUARD_NAME");
                final String name = rs.getString("NAME");
                final String familyNo = rs.getString("FAMILY_NO");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String guardZipcd = rs.getString("GUARD_ZIPCD");
                final String guardAddr1 = rs.getString("GUARD_ADDR1");
                final String guardAddr2 = rs.getString("GUARD_ADDR2");
                final String guardTelno = rs.getString("GUARD_TELNO");
                final String branchname = rs.getString("BRANCHNAME");
                final String positionName = rs.getString("POSITION_NAME");
                final String jyukyo = rs.getString("JYUKYO");

                if (!"".equals(befHr) && !befHr.equals(gradeHr)) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }

                svf.VrsOut("TITLE", _param._ctrlYear + "年度　連絡名簿");
                final String tosaStr = "tosa".equals(_param._naNameMst._name1) ? "ホーム" : "";
                svf.VrsOut("HR_NAME", hrName + tosaStr);
                final int staffNamelen = getMS932ByteCount(staffname);
                svf.VrsOut("TEACHER_NSME" + (staffNamelen > 20 ? "2" : "1"), staffname);
                svf.VrsOut("TEACHER_TELNO", stafftelno);

                svf.VrsOutn("ATTEND_NO", lineCnt, attendNo);
                final int gnamelen = getMS932ByteCount(guardName);
                svf.VrsOutn("GUARD_NAME" + (gnamelen > 30 ? "3" : gnamelen > 20 ? "2" : "1"), lineCnt, guardName);
                final int namelen = getMS932ByteCount(name);
                svf.VrsOutn("NAME" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), lineCnt, name);
                final int finschoolNamelen = getMS932ByteCount(finschoolName);
                svf.VrsOutn("FINSCHOOL_NAME" + (finschoolNamelen > 30 ? "3" : finschoolNamelen > 20 ? "2" : "1"), lineCnt, finschoolName);
                svf.VrsOutn("ZIP_NO", lineCnt, guardZipcd);
                final int addrlen = getMS932ByteCount(guardAddr1 + guardAddr2);
                svf.VrsOutn("ADDR" + (addrlen > 40 ? "2" : "1"), lineCnt, guardAddr1 + guardAddr2);
                svf.VrsOutn("TELNO", lineCnt, guardTelno);
                svf.VrsOutn("BRANCH_NAME", lineCnt, branchname);
                svf.VrsOutn("GOTOSCHOOL_DIV", lineCnt, jyukyo);
                svf.VrsOutn("JOB_NAME", lineCnt, positionName);
                final String sqlFamily = getFamilySql(familyNo);
                psFamily = db2.prepareStatement(sqlFamily);
                rsFamily = psFamily.executeQuery();
                String family = "";
                String sepFamily = "";
                while (rsFamily.next()) {
                    if (null != rsFamily.getString("SCHREGNO") && rsFamily.getString("SCHREGNO").equals(rs.getString("SCHREGNO"))) {
                        continue;
                    }
                    if ("".equals(rsFamily.getString("GRD_DIV"))) {
                        family += sepFamily + rsFamily.getString("HR_NAME") + "　" + namaeOnly(rsFamily.getString("NAME"));
                        sepFamily = "、";
                    }
                }
                final int familylen = getMS932ByteCount(family);
                svf.VrsOutn("BROSIS" + (familylen > 30 ? "3" : familylen > 20 ? "2" : "1"), lineCnt, family);
                befHr = gradeHr;
                lineCnt++;
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
            DbUtils.closeQuietly(null, psFamily, rsFamily);
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
        stb.append("     HDAT.GRADE || HDAT.HR_CLASS AS GRADEHR, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     STAFF.STAFFTELNO, ");
        stb.append("     BRANCH.GUARD_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     BASED.BASE_REMARK1 AS FAMILY_NO, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     BRANCH.GUARD_ZIPCD, ");
        stb.append("     VALUE(BRANCH.GUARD_ADDR1, '') AS GUARD_ADDR1, ");
        stb.append("     VALUE(BRANCH.GUARD_ADDR2, '') AS GUARD_ADDR2, ");
        stb.append("     BRANCH.GUARD_TELNO, ");
        stb.append("     BRANCHM.BRANCHNAME, ");
        stb.append("     J007.NAME1 AS POSITION_NAME, ");
        stb.append("     J008.NAME1 AS JYUKYO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT HDAT ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON HDAT.TR_CD1 = STAFF.STAFFCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON HDAT.YEAR = REGD.YEAR ");
        stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND HDAT.GRADE = REGD.GRADE ");
        stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON BASE.FINSCHOOLCD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST BASED ON BASE.SCHREGNO = BASED.SCHREGNO ");
        stb.append("          AND BASED.BASE_SEQ = '009' ");
        stb.append("     LEFT JOIN SCHREG_BRANCH_DAT BRANCH ON HDAT.YEAR = BRANCH.YEAR ");
        stb.append("          AND REGD.SCHREGNO = BRANCH.SCHREGNO ");
        stb.append("     LEFT JOIN BRANCH_MST BRANCHM ON BRANCH.BRANCHCD = BRANCHM.BRANCHCD ");
        stb.append("     LEFT JOIN NAME_MST J007 ON J007.NAMECD1 = 'J007' ");
        stb.append("          AND BRANCH.BRANCH_POSITION = J007.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST J008 ON J008.NAMECD1 = 'J008' ");
        stb.append("          AND BRANCH.RESIDENTCD = J008.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     HDAT.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND HDAT.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND HDAT.GRADE || '-' || HDAT.HR_CLASS IN (" + _param._selectInState + ") ");
        stb.append("     AND NOT EXISTS(SELECT ");
        stb.append("                        'x' ");
        stb.append("                    FROM ");
        stb.append("                        SCHREG_TRANSFER_DAT TRANS ");
        stb.append("                    WHERE ");
        stb.append("                        TRANS.TRANSFERCD IN ('1', '2') ");
        stb.append("                        AND '" + _param._ctrlDate + "' BETWEEN TRANS.TRANSFER_SDATE AND VALUE(TRANS.TRANSFER_EDATE, '9999-12-31') ");
        stb.append("                        AND BASE.SCHREGNO = TRANS.SCHREGNO ");
        stb.append("                   ) ");
        stb.append(" AND NOT EXISTS(SELECT ");
        stb.append("                    'x' ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_BASE_MST BASE2 ");
        stb.append("                WHERE ");
        stb.append("                        BASE2.GRD_DIV IN ('2', '3', '6', '7') "); //2:退学 3:転学 6:除籍 7:転籍
        stb.append("                    AND value(BASE2.GRD_DATE, '9999-12-31') <= '" + _param._ctrlDate + "' ");
        stb.append("                    AND BASE2.SCHREGNO =  REGD.SCHREGNO ");
        stb.append("               ) ");
        stb.append(" ORDER BY ");
        stb.append("     HDAT.GRADE || HDAT.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
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

    private class NameMst {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _name3;
        final String _abbv1;
        final String _abbv2;
        final String _abbv3;
        final String _spare1;
        final String _spare2;
        final String _spare3;
        public NameMst(
                final String namecd2,
                final String name1,
                final String name2,
                final String name3,
                final String abbv1,
                final String abbv2,
                final String abbv3,
                final String spare1,
                final String spare2,
                final String spare3
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _name3 = name3;
            _abbv1 = abbv1;
            _abbv2 = abbv2;
            _abbv3 = abbv3;
            _spare1 = spare1;
            _spare2 = spare2;
            _spare3 = spare3;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 60320 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
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
        private final NameMst _naNameMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
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
            _naNameMst = getNameMst(db2, "Z010", "00");
        }

        private NameMst getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            NameMst nameMst = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                rs.next();
                nameMst = new NameMst(rs.getString("NAMECD2"), rs.getString("NAME1"), rs.getString("NAME2"), rs.getString("NAME3"), rs.getString("ABBV1"), rs.getString("ABBV2"), rs.getString("ABBV3"), rs.getString("NAMESPARE1"), rs.getString("NAMESPARE2"), rs.getString("NAMESPARE3"));
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return nameMst;
        }

    }
}

// eof

