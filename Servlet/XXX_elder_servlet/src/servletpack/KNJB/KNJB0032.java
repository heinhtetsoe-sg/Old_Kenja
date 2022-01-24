/*
 * $Id: a4b1114f6fb78a42eda85db186b5c337e285ef03 $
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
import java.util.ArrayList;
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

public class KNJB0032 {

    private static final Log log = LogFactory.getLog(KNJB0032.class);

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
        final List classList = getClassList(db2);
        for (Iterator iterator = classList.iterator(); iterator.hasNext();) {
            final ClassKind classKind = (ClassKind) iterator.next();
            printData(svf, classKind);
        }
    }

    private void printData(final Vrw32alp svf, final ClassKind classKind) {
        svf.VrSetForm("KNJB0032.frm", 4);

        svf.VrsOut("PRINT_DATE", _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("TITLE", "教科別　開講講座情報一覧");
        svf.VrsOut("CLASS_CD", classKind._classCd);
        svf.VrsOut("CLASS_NAME", classKind._className);
        svf.VrsOut("NENDO", _param._year + "年度　" + _param._semesterName);
        int subclassCnt = 0;
        for (Iterator iterator = classKind._subclassMap.keySet().iterator(); iterator.hasNext();) {
            String subclassCd = (String) iterator.next();
            final SubclassDat subclassDat = (SubclassDat) classKind._subclassMap.get(subclassCd);

            for (Iterator itChair = subclassDat._chairMap.keySet().iterator(); itChair.hasNext();) {
                String chairCd = (String) itChair.next();
                final ChairDat chairDat = (ChairDat) subclassDat._chairMap.get(chairCd);

                svf.VrsOut("SUBCLASS_CD", subclassDat._classCd + "-" + subclassDat._schoolKind + "-" + subclassDat._curriculumCd + "-" + subclassDat._subclassCd);
                final String fieldName = null != subclassDat._subclassName ? subclassDat._subclassName.length() > 13 ? "2" : "1" : "1";
                svf.VrsOut("SUBCLASS_NAME" + fieldName, subclassDat._subclassName);
                svf.VrsOut("GRP1", String.valueOf(subclassCnt));

                svf.VrsOut("CHAIRCD", chairDat._chairCd);
                svf.VrsOut("CHAIR_NAME1", chairDat._chairName);
                svf.VrsOut("COMP", chairDat._takesemesName);
                svf.VrsOut("CREDIT", chairDat._creditInfo);
                final String fieldStaffName = null != chairDat._chairStaff ? chairDat._chairStaff.length() > 15 ? "2" : "1" : "1";
                svf.VrsOut("TEACHER_NAME" + fieldStaffName, chairDat._chairStaff);
                svf.VrsOut("SUB_TEACHER", chairDat._fukuTanCnt);

                final String hrClass = chairDat.getHrClass();
                final List printHrList = KNJ_EditKinsoku.getTokenList(hrClass, 15);
                for (Iterator iterator2 = printHrList.iterator(); iterator2.hasNext();) {
                    svf.VrsOut("SUBCLASS_CD", subclassDat._classCd + "-" + subclassDat._schoolKind + "-" + subclassDat._curriculumCd + "-" + subclassDat._subclassCd);
                    svf.VrsOut("GRP1", String.valueOf(subclassCnt));

                    svf.VrsOut("GRP2", chairDat._chairCd);
                    svf.VrsOut("GRP3", chairDat._chairCd);
                    svf.VrsOut("GRP4", chairDat._chairCd);
                    svf.VrsOut("GRP5", chairDat._chairCd);
                    svf.VrsOut("GRP6", chairDat._chairCd);
                    svf.VrsOut("GRP7", chairDat._chairCd);
                    svf.VrsOut("GRP8", chairDat._chairCd);
                    svf.VrsOut("GRP9", chairDat._chairCd);
                    final String printHrData = (String) iterator2.next();
                    svf.VrsOut("ATTEND", printHrData);
                    svf.VrEndRecord();
                }
                if (printHrList.size() == 0) {
                    svf.VrEndRecord();
                }

                subclassCnt++;
                _hasData = true;
            }
        }
    }

    private List getClassList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getClassSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befClassCd = "";
            String befSubClassCd = "";
            ClassKind classKind = null;
            SubclassDat subclassDat = null;
            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String setClassKind = classCd + "-" + schoolKind;
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String setSubClassCd = setClassKind + curriculumCd + subclassCd;
                final String className = rs.getString("CLASSNAME");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String chairCd = rs.getString("CHAIRCD");
                final String groupCd = rs.getString("GROUPCD");
                final String chairName = rs.getString("CHAIRNAME");
                final String takesemesName = rs.getString("TAKESEMES_NAME");
                if (classKind == null || (!"".equals(befClassCd) && !befClassCd.equals(setClassKind))) {
                    classKind = new ClassKind(setClassKind, className);
                    list.add(classKind);
                }
                if (subclassDat == null || (!"".equals(befSubClassCd) && !befSubClassCd.equals(setSubClassCd))) {
                    subclassDat = new SubclassDat(classCd, schoolKind, curriculumCd, subclassCd, subclassName);
                }
                subclassDat.setChairMap(db2, chairCd, groupCd, chairName, takesemesName);
                classKind._subclassMap.put(setSubClassCd, subclassDat);

                befClassCd = setClassKind;
                befSubClassCd = setSubClassCd;
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CLS_M.CLASSNAME, ");
        stb.append("     SUBCLASS.SUBCLASSNAME, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     CASE WHEN T1.TAKESEMES = '0' THEN '通年' ELSE SEME.SEMESTERNAME END AS TAKESEMES_NAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_MST SUBCLASS ON T1.CLASSCD = SUBCLASS.CLASSCD ");
        stb.append("           AND T1.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
        stb.append("           AND T1.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
        stb.append("           AND T1.SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
        stb.append("     INNER JOIN CLASS_MST CLS_M ON T1.CLASSCD = CLS_M.CLASSCD ");
        stb.append("           AND T1.SCHOOL_KIND = CLS_M.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR AND SEME.SEMESTER = T1.TAKESEMES ");
        stb.append("     LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' AND NMA023.NAME1 = CLS_M.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param.selectSchoolKindSql)) {
                stb.append("   AND T1.SCHOOL_KIND IN " + _param.selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append(" AND T1.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     NMA023.NAMECD2, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }

    private class ClassKind {
        final String _classCd;
        final String _className;
        final Map _subclassMap;

        ClassKind(
                final String classCd,
                final String className
                ) {
            _classCd = classCd;
            _className = className;
            _subclassMap = new TreeMap();
        }

        public void setSubclassMap(
                final String subclassCd,
                final SubclassDat subclassDat
        ) throws SQLException {
            _subclassMap.put(subclassCd, subclassDat);
        }
    }

    private class SubclassDat {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final Map _chairMap;
        SubclassDat(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName
                ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _chairMap = new TreeMap();
        }

        public void setChairMap(final DB2UDB db2, final String chairCd, final String groupCd, final String chairName, final String takesemesName) throws SQLException {
            final ChairDat chairDat = new ChairDat(chairCd, groupCd, chairName, takesemesName);
            chairDat.setChairClass(db2);
            chairDat.setChairStaff(db2);
            chairDat.setCredit(db2);
            _chairMap.put(chairCd, chairDat);
        }

    }

    private class ChairDat {
        final String _chairCd;
        final String _groupCd;
        final String _chairName;
        final String _takesemesName;
        final Map _chairClass;
        String _chairStaff;
        String _fukuTanCnt;
        String _creditInfo;

        ChairDat(
                final String chairCd,
                final String groupCd,
                final String chairName,
                final String takesemesName
                ) {
            _chairCd = chairCd;
            _groupCd = groupCd;
            _chairName = chairName;
            _takesemesName = takesemesName;
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
                    stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE AND GDAT.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
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

        public void setChairStaff(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getChairStaffInfoSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String sep = "";
                int fukutanCnt = 0;
                _chairStaff = "";
                while (rs.next()) {
                    if ("1".equals(rs.getString("CHARGEDIV"))) {
                        _chairStaff += sep + rs.getString("STAFFNAME");
                        sep = ",";
                    } else {
                        fukutanCnt++;
                    }
                }
                _fukuTanCnt = String.valueOf(fukutanCnt);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getChairStaffInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.STAFFCD, ");
            stb.append("     VALUE(T1.CHARGEDIV, 0) AS CHARGEDIV, ");
            stb.append("     STF.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append("     INNER JOIN STAFF_MST STF ON T1.STAFFCD = STF.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = '" + _chairCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.STAFFCD ");
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
        log.fatal("$Revision: 59856 $");
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

