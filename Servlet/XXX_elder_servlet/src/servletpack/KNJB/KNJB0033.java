/*
 * $Id: 89c02f1752960eee6330a63ddaadc622d4e20002 $
 *
 * 作成日: 2015/07/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJB0033 {

    private static final Log log = LogFactory.getLog(KNJB0033.class);

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
        final Map staffMap = getStaffMap(db2);
        for (Iterator iterator = staffMap.keySet().iterator(); iterator.hasNext();) {
            final String staffCd = (String) iterator.next();

            final StaffChair staffChair = (StaffChair) staffMap.get(staffCd);
            printStaff(svf, staffChair);
        }
    }

    private void printStaff(final Vrw32alp svf, final StaffChair staffChair) {
        svf.VrSetForm("KNJB0033.frm", 4);

        svf.VrsOut("PRINT_DATE", _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("TITLE", "職員別　講座担当情報一覧");
        svf.VrsOut("STAFF_NO", staffChair.getStaffCd());
        svf.VrsOut("STAFF_NAME", staffChair._staffName);
        svf.VrsOut("NENDO", _param._year + "年度　" + _param._semesterName);
        for (Iterator iterator = staffChair._chairMap.keySet().iterator(); iterator.hasNext();) {
            String chairCd = (String) iterator.next();
            final ChairDat chairDat = (ChairDat) staffChair._chairMap.get(chairCd);

            svf.VrsOut("CHAIRCD", chairDat._chairCd);
            svf.VrsOut("CHAIR_NAME", chairDat._chairName);
            svf.VrsOut("CREDIT", chairDat._creditInfo);

            final String hrClass = chairDat.getHrClass();
            final List printHrList = KNJ_EditKinsoku.getTokenList(hrClass, 15);
            for (Iterator iterator2 = printHrList.iterator(); iterator2.hasNext();) {
                svf.VrsOut("GRP1", chairDat._chairCd);
                svf.VrsOut("GRP2", chairDat._chairCd);
                svf.VrsOut("GRP3", chairDat._chairCd);
                svf.VrsOut("GRP4", chairDat._chairCd);
                svf.VrsOut("GRP5", chairDat._chairCd);
                final String printHrData = (String) iterator2.next();
                svf.VrsOut("ATTEND", printHrData);
                svf.VrEndRecord();
            }
            if (printHrList.size() == 0) {
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private Map getStaffMap(final DB2UDB db2) {
        final Map list = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getChairStaffSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befStaff = "";
            StaffChair staffChair = null;
            while (rs.next()) {
                final String staffCd = rs.getString("STAFFCD");
                final String staffName = rs.getString("STAFFNAME");
                final String chairCd = rs.getString("CHAIRCD");
                final String groupCd = rs.getString("GROUPCD");
                final String chairName = rs.getString("CHAIRNAME");
                if (staffChair == null || (!"".equals(befStaff) && !befStaff.equals(staffCd))) {
                    staffChair = new StaffChair(staffCd, staffName);
                    list.put(staffCd, staffChair);
                }
                staffChair.setChairMap(db2, chairCd, groupCd, chairName);

                befStaff = staffCd;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getChairStaffSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     STF.STAFFNAME, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     CHAIR.GROUPCD, ");
        stb.append("     CHAIR.CHAIRNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT T1 ");
        stb.append("     INNER JOIN STAFF_MST STF ON T1.STAFFCD = STF.STAFFCD ");
        stb.append("     INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ");
        stb.append("           AND T1.SEMESTER = CHAIR.SEMESTER ");
        stb.append("           AND T1.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND CHAIR.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("     AND CHAIR.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    private class StaffChair {
        final String _staffCd;
        final String _staffName;
        final Map _chairMap;

        StaffChair(
                final String staffCd,
                final String staffName
                ) {
            _staffCd = staffCd;
            _staffName = staffName;
            _chairMap = new TreeMap();
        }

        public void setChairMap(final DB2UDB db2, final String chairCd, final String groupCd, final String chairName) throws SQLException {
            final ChairDat chairDat = new ChairDat(chairCd, groupCd, chairName);
            chairDat.setChairClass(db2);
            chairDat.setCredit(db2);
            _chairMap.put(chairCd, chairDat);
        }

        private String getStaffCd() {
            String retStr = "";
            if (null != _param._simo && !"".equals(_param._simo)) {
                int len = _staffCd.length();
                final int intSimo = Integer.parseInt(_param._simo);
                retStr = _staffCd.substring(len - intSimo, len);
                for (int umeCnt = 0; umeCnt < len; umeCnt++) {
                    retStr = _param._fuseji + retStr;
                }
                final int retStrLen = retStr.length();
                retStr = retStr.substring(intSimo, retStrLen);
                return retStr;
            } else {
                return _staffCd;
            }
        }
    }

    private class ChairDat {
        final String _chairCd;
        final String _groupCd;
        final String _chairName;
        final Map _chairClass;
        String _creditInfo;

        ChairDat(
                final String chairCd,
                final String groupCd,
                final String chairName
                ) {
            _chairCd = chairCd;
            _groupCd = groupCd;
            _chairName = chairName;
            _chairClass = new TreeMap();
        }

        public String getHrClass() {
            String retStr = "";
            String sep = "";
            for (Iterator iterator = _chairClass.keySet().iterator(); iterator.hasNext();) {
                String hrClass = (String) iterator.next();
                final String hrName = (String) _chairClass.get(hrClass);
                retStr += sep + hrName;
                sep = ",";
            }
            return retStr;
        }

        public void setChairClass(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChairClsInfoSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _chairClass.put(rs.getString("HR_CLASS"), rs.getString("HR_NAMEABBV"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getChairClsInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.GRADE || T2.HR_CLASS AS HR_CLASS, ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_CLS_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ");
            stb.append("             ON  T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T2.GRADE = T1.TRGTGRADE ");
            stb.append("             AND T2.HR_CLASS = T1.TRGTCLASS ");
            if ("1".equals(_param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                    stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T2.GRADE ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T2.GRADE AND GDAT.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND (T1.CHAIRCD = '" + _chairCd + "' OR T1.CHAIRCD = '0000000') ");
            stb.append("     AND T1.GROUPCD = '" + _groupCd + "' ");
            if ("1".equals(_param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                    stb.append("   AND GDAT.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     HR_CLASS ");
            return stb.toString();
        }
        
        public void setCredit(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getCreditInfoSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                _creditInfo = "";
                while (rs.next()) {
                    if (null != rs.getString("MIN_CREDITS")) {
                        final int minCredits = rs.getInt("MIN_CREDITS");
                        final int maxCredits = rs.getInt("MAX_CREDITS");
                        if (minCredits == maxCredits) {
                            _creditInfo = String.valueOf(minCredits);
                        } else {
                            _creditInfo = String.valueOf(minCredits) + "\uFF5E" + String.valueOf(maxCredits);
                        }
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getCreditInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MIN(CRE.CREDITS) AS MIN_CREDITS ");
            stb.append("   , MAX(CRE.CREDITS) AS MAX_CREDITS ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = T1.YEAR ");
            stb.append("         AND CHAIR.SEMESTER = T1.SEMESTER ");
            stb.append("         AND CHAIR.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND REGD.YEAR = T1.YEAR ");
            stb.append("         AND REGD.SEMESTER = T1.SEMESTER ");
            stb.append("     INNER JOIN CREDIT_MST CRE ON T1.YEAR = CRE.YEAR ");
            stb.append("         AND REGD.COURSECD = CRE.COURSECD ");
            stb.append("         AND REGD.GRADE = CRE.GRADE ");
            stb.append("         AND REGD.MAJORCD = CRE.MAJORCD ");
            stb.append("         AND REGD.COURSECODE = CRE.COURSECODE ");
            stb.append("         AND CHAIR.CLASSCD = CRE.CLASSCD ");
            stb.append("         AND CHAIR.SCHOOL_KIND = CRE.SCHOOL_KIND ");
            stb.append("         AND CHAIR.CURRICULUM_CD = CRE.CURRICULUM_CD ");
            stb.append("         AND CHAIR.SUBCLASSCD = CRE.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = '" + _chairCd + "' ");
            stb.append("     AND CRE.CREDITS IS NOT NULL ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 60889 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlDate;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;
        final String use_prg_schoolkind;
        final String selectSchoolKind;
        String selectSchoolKindSql = null;
        final String _simo;
        final String _fuseji;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _semesterName = getSemeName(db2);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                final StringBuffer sql = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                for (int i = 0; i < split.length; i++) {
                    sql.append(split[i]);
                    if (i < split.length - 1) {
                        sql.append("','");
                    }
                }
                selectSchoolKindSql = sql.append("')").toString();
            }
            _simo = request.getParameter("simo");
            _fuseji = request.getParameter("fuseji");
        }

        private String getSemeName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String semeSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'";
                ps = db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retStr;
        }

    }
}

// eof

