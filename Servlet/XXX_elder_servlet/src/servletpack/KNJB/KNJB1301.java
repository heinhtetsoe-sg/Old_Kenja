/*
 * $Id: 46341f82c4aa4ce946c6668f012b093a1872f518 $
 *
 * 作成日: 2009/10/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [時間割管理]
 *
 *                  ＜ＫＮＪＢ１０１＞  科目別受講生徒一覧
 */
public class KNJB1301 {

    private static final Log log = LogFactory.getLog(KNJB1301.class);

    private boolean _hasData;

    Param _param;

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = subclassStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final String formName = "KNJB101.frm";

            String oldSubclassCd = "";
            String oldChairCd = "";
            String oldSubclassGroupCd = "";

            svf.VrSetForm(formName, 4);
            int studentCount = 0; // 人数計
            int manCount = 0; // 人数男
            int womanCount = 0;// 人数女

            svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "-01-01") + "度");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
            svf.VrsOut("TIME", _param._loginHour + "時" + _param._loginMinutes + "分");
            svf.VrsOut("STAFFNAME_SHOW", _param._loginStaffName);
            svf.VrsOut("SELECT", _param.tableSubclassStdSelectDat.equals(_param._selectTable) ? "履修登録名簿" : "講座名簿");
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("NOTICE", _param._requireInfo);

            int line = 0;
            final int linePerPage = 40;

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String chairCd = rs.getString("CHAIRCD");
                boolean isNewPage = false;

                if (_param._isBasedSubclassGroup && !"".equals(oldSubclassGroupCd) && !oldSubclassGroupCd.equals(rs.getString("SUBCLASS_GROUPCD"))) {
                    log.debug("科目グループ設定による改ページ " + oldSubclassGroupCd + " => " + rs.getString("SUBCLASS_GROUPCD"));
                    isNewPage = true;
                } else if (_param.tableChairStdDat.equals(_param._selectTable) && !"".equals(oldChairCd) && !oldChairCd.equals(chairCd)) {
                    log.debug("講座コードによる改ページ " + oldChairCd + " => " + chairCd);
                    isNewPage = true;
                } else if (!"".equals(oldSubclassCd) && !oldSubclassCd.equals(subclassCd)) {
                    log.debug("科目コードによる改ページ " + oldSubclassCd + " => " + subclassName);
                    isNewPage = true;
                }

                if (isNewPage) {  // 改ページ処理
                    final String total;
                    if (_param.tableSubclassStdSelectDat.equals(_param._selectTable)) {
                        total = "計" + studentCount + "人／男" + manCount + "名、女" + womanCount + "名";
                    } else {
                        total = "講座別人数：計" + studentCount + "人／男" + manCount + "名、女" + womanCount + "名";
                    }
                    log.debug(" total = "+ total);
                    svf.VrsOut("TOTAL", total);
                    if (line % linePerPage != 0) {
                        for (int i = line % linePerPage; i < linePerPage; i++) {
                            svf.VrEndRecord();
                            svf.VrsOut("SUBCLASS", "　");
                        }
                    }
                    svf.VrEndRecord();
                    line = 0;

                    studentCount = 0;
                    manCount = 0;
                    womanCount = 0;
                } else if (!"".equals(oldChairCd)) {
                    svf.VrsOut("TOTAL", "");
                    svf.VrEndRecord();
                }

                String gradeName = (_param._gradeName != null) ? _param._gradeName : "";
                if (_param._isBasedSubclassGroup) {
                    gradeName += rs.getString("SUBCLASS_GROUP_NAME") != null ? "　(" + rs.getString("SUBCLASS_GROUP_NAME") + ")" : "";
                }
                svf.VrsOut("GRADE", gradeName);
                svf.VrsOut("CLASS_NAME", rs.getString("CLASSNAME"));
                svf.VrsOut("SEL_SUBCLASS", rs.getString("REQUIRE_NAME"));
                svf.VrsOut("SUBCLASS", subclassName);
                svf.VrsOut("FIELD1", rs.getString("CHAIRNAME"));
                svf.VrsOut("CREDIT", rs.getString("CREDITS"));

                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO", Integer.valueOf(rs.getString("ATTENDNO")).toString());
                svf.VrsOut("SCHREGNO", schregno);
                svf.VrsOut("NAME_SHOW", rs.getString("NAME"));
                svf.VrsOut("SEX", rs.getString("SEXNAME"));

                oldChairCd = chairCd;
                oldSubclassCd = subclassCd;
                if (_param._isBasedSubclassGroup) {
                    oldSubclassGroupCd = rs.getString("SUBCLASS_GROUPCD");
                }
                studentCount += 1;

                if ("1".equals(rs.getString("SEX"))) {
                    manCount += 1;
                } else if ("2".equals(rs.getString("SEX"))) {
                    womanCount += 1;
                }

                line += 1;

                _hasData = true;
            }

            if (_hasData) {
                if (line % linePerPage != 0) {
                    for (int i = line % linePerPage; i < linePerPage; i++) {
                        svf.VrEndRecord();
                        svf.VrsOut("SUBCLASS", "　");
                    }
                }
                final String total;
                if (_param.tableSubclassStdSelectDat.equals(_param._selectTable)) {
                    total = "計" + studentCount + "人／男" + manCount + "名、女" + womanCount + "名";
                } else {
                    total = "講座別人数：計" + studentCount + "人／男" + manCount + "名、女" + womanCount + "名";
                }
                svf.VrsOut("TOTAL", total);
                svf.VrEndRecord();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String subclassStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS_SELECT AS ( ");
        if (_param.tableSubclassStdSelectDat.equals(_param._selectTable)) {
            stb.append(" SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("     ,T1.SCHREGNO ");
            stb.append("     ,T1.GROUPCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      ,T1.CLASSCD ");
                stb.append("      ,T1.SCHOOL_KIND ");
                stb.append("      ,T1.CURRICULUM_CD ");
            }
            stb.append("      ,T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     ,CAST (NULL AS VARCHAR(4)) AS CHAIRCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.RIREKI_CODE = '" + _param._rirekiCode + "' ");
            stb.append("     AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._subclassCd) + " ");
            if ("1".equals(_param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("   AND T1.SCHOOL_KIND IN (" + _param._selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
            }
        } else {
            stb.append(" SELECT ");
            stb.append("     T0.YEAR ");
            stb.append("     ,T0.SCHREGNO ");
            stb.append("     ,CAST (NULL AS VARCHAR(4)) AS GROUPCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      ,T1.CLASSCD ");
                stb.append("      ,T1.SCHOOL_KIND ");
                stb.append("      ,T1.CURRICULUM_CD ");
            }
            stb.append("     ,T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     ,T0.CHAIRCD AS CHAIRCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T0 ");
            stb.append("     INNER JOIN CHAIR_DAT T1 ON ");
            stb.append("         T0.YEAR = T1.YEAR ");
            stb.append("         AND T0.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T0.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("         T1.YEAR = T2.YEAR ");
            stb.append("         AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("         AND T0.SCHREGNO = T2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._subclassCd) + " ");
            stb.append("     AND '" + _param._appDate + "'  BETWEEN T0.APPDATE AND T0.APPENDDATE ");
            if ("1".equals(_param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                    stb.append("   AND T1.SCHOOL_KIND IN (" + _param._selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T0.YEAR, T0.SEMESTER, T0.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      T1.CLASSCD, ");
                stb.append("      T1.SCHOOL_KIND, ");
                stb.append("      T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, T0.CHAIRCD ");
        }
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T8.CLASSCD ");
        stb.append("     ,T8.CLASSNAME ");
        stb.append("     , ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
        }
        stb.append("      T4.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("     ,T4.SUBCLASSNAME ");
        if (_param.tableSubclassStdSelectDat.equals(_param._selectTable)) {
            stb.append("     ,CAST (NULL AS VARCHAR(7)) AS CHAIRCD ");
            stb.append("     ,CAST (NULL AS VARCHAR(30)) AS CHAIRNAME ");
        } else {
            stb.append("     ,T10.CHAIRCD ");
            stb.append("     ,T10.CHAIRNAME ");
        }
        stb.append("     ,CM.CREDITS ");
        stb.append("     ,T2.GRADE ");
        stb.append("     ,T2.HR_CLASS ");
        stb.append("     ,T6.HR_NAME ");
        stb.append("     ,T2.ATTENDNO ");
        stb.append("     ,T1.SCHREGNO ");
        stb.append("     ,T3.NAME ");
        stb.append("     ,T3.SEX ");
        stb.append("     ,T5.NAME2 AS SEXNAME ");
        stb.append("     ,CM.REQUIRE_FLG ");
        stb.append("     ,T6.NAMESPARE1 AS REQUIRE_NAME ");
        if (_param._isBasedSubclassGroup) {
            stb.append("     ,T11.GROUPCD AS SUBCLASS_GROUPCD ");
            stb.append("     ,T12.NAME AS SUBCLASS_GROUP_NAME ");
        }
        stb.append(" FROM SUBCLASS_SELECT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON ");
        stb.append("         T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("         T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T2.GRADE AND GDAT.SCHOOL_KIND IN (" + _param._selectSchoolKindIn + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T2.GRADE AND GDAT.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T6 ON ");
        stb.append("         T6.GRADE = T2.GRADE ");
        stb.append("         AND T6.HR_CLASS = T2.HR_CLASS ");
        stb.append("         AND T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.SEMESTER = T2.SEMESTER ");
        stb.append("     INNER JOIN SUBCLASS_MST T4 ON ");
        stb.append("         T4.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append("     AND T4.SCHOOL_KIND IN (" + _param._selectSchoolKindIn + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("     AND T4.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
        }
        stb.append("     INNER JOIN CLASS_MST T8 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      T8.CLASSCD = T1.CLASSCD ");
            stb.append("      AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        } else {
            stb.append("      T8.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
        }
        if (_param._isBasedSubclassGroup) {
            stb.append("     INNER JOIN SUBCLASS_COMP_SELECT_DAT T11 ON");
            stb.append("         T11.YEAR = T1.YEAR AND T11.GRADE = T2.GRADE AND T11.CLASSCD = T8.CLASSCD AND T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.GROUPCD = T1.GROUPCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      AND T11.SCHOOL_KIND = T8.SCHOOL_KIND AND T11.CURRICULUM_CD = T4.CURRICULUM_CD ");
                if ("1".equals(_param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                        stb.append("     AND T11.SCHOOL_KIND IN (" + _param._selectSchoolKindIn + ") ");
                    }
                } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOL_KIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
                    stb.append("     AND T11.SCHOOL_KIND = '" + _param._SCHOOL_KIND + "' ");
                }
            }
            stb.append("      AND T11.COURSECD = T2.COURSECD AND T11.MAJORCD = T2.MAJORCD AND T11.COURSECODE = T2.COURSECODE ");
            stb.append("     INNER JOIN SUBCLASS_COMP_SELECT_MST T12 ON");
            stb.append("         T12.YEAR = T11.YEAR AND T12.GRADE = T11.GRADE AND T12.GROUPCD = T11.GROUPCD");
            stb.append("         AND T12.COURSECD = T11.COURSECD AND T12.MAJORCD = T11.MAJORCD AND T12.COURSECODE = T11.COURSECODE ");
        }
        stb.append("     LEFT JOIN CREDIT_MST CM ON ");
        stb.append("         CM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND CM.YEAR = T2.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("      AND CM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("         AND CM.COURSECD = T2.COURSECD ");
        stb.append("         AND CM.MAJORCD = T2.MAJORCD ");
        stb.append("         AND CM.GRADE = T2.GRADE ");
        stb.append("         AND CM.COURSECODE = T2.COURSECODE ");
        stb.append("     LEFT JOIN NAME_MST T5 ON ");
        stb.append("         T5.NAMECD1 = 'Z002' ");
        stb.append("         AND T5.NAMECD2 = T3.SEX ");
        stb.append("     LEFT JOIN NAME_MST T6 ON ");
        stb.append("         T6.NAMECD1 = 'Z011' ");
        stb.append("         AND T6.NAMECD2 = CM.REQUIRE_FLG ");
        if (!_param.tableSubclassStdSelectDat.equals(_param._selectTable)) {
            stb.append("     LEFT JOIN CHAIR_DAT T10 ON ");
            stb.append("         T2.YEAR = T10.YEAR ");
            stb.append("         AND T2.SEMESTER = T10.SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         AND T1.CLASSCD = T10.CLASSCD ");
                stb.append("         AND T1.SCHOOL_KIND = T10.SCHOOL_KIND ");
                stb.append("         AND T1.CURRICULUM_CD = T10.CURRICULUM_CD ");
            }
            stb.append("         AND T1.SUBCLASSCD = T10.SUBCLASSCD ");
            stb.append("         AND T1.CHAIRCD = T10.CHAIRCD ");
            stb.append("     LEFT JOIN CHAIR_STD_DAT T9 ON ");
            stb.append("         T9.YEAR = T10.YEAR ");
            stb.append("         AND T9.SEMESTER = T10.SEMESTER ");
            stb.append("         AND T9.CHAIRCD = T10.CHAIRCD ");
            stb.append("         AND T9.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");
        if (!"99".equals(_param._grade)) {
            stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        }
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("      T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T4.SUBCLASSCD, ");
        if (!_param.tableSubclassStdSelectDat.equals(_param._selectTable)) {
            stb.append("     T10.CHAIRCD, ");
        }
        if (_param._isBasedSubclassGroup) {
            stb.append("     T11.GROUPCD, ");
        }
        stb.append("     T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61598 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        public final String tableSubclassStdSelectDat = "2"; // 科目履修名簿を参照する
        public final String tableChairStdDat = "1"; // 講座名簿を参照する
        final String _year;
        final String _semester;
        final String _grade;
        final String _selectTable;
        final String[] _subclassCd;
        final String _loginDate;
        final String _loginStaffCd;
        final String _appDate;
        final boolean _isBasedSubclassGroup; // グループ設定をもとにする
        final String _useCurriculumcd;
        private final String _rirekiCode;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;

        private final String _use_prg_schoolkind;
        private final String _selectSchoolKind;

        /** ログイン時間 */
        final String _loginHour;
        final String _loginMinutes;

        String _gradeName = null;
        String _schoolName = null;
        String _loginStaffName = null;
        String _requireInfo = null;

        String _selectSchoolKindIn = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _selectTable = request.getParameter("TAISYOU_MEIBO");
            _subclassCd = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("CTRL_DATE");
            _loginStaffCd = request.getParameter("STAFFCD");
            _appDate = request.getParameter("DATE").replace('/', '-');
            _isBasedSubclassGroup = "1".equals(request.getParameter("KIJUN1"));
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _rirekiCode = request.getParameter("RIREKI_CODE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");

            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");

            final Calendar cal = Calendar.getInstance();
            _loginHour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            _loginMinutes = String.valueOf(cal.get(Calendar.MINUTE));

            if (!StringUtils.isBlank(_selectSchoolKind)) {
                String[] split = StringUtils.split(_selectSchoolKind, ":");
                String comma = "";
                _selectSchoolKindIn = "";
                for (int i = 0; i < split.length; i++) {
                	_selectSchoolKindIn += comma + "'" + split[i] + "'";
                    comma = ",";
                }
                log.info(" selectSchoolKindIn = " + _selectSchoolKindIn);
            }

            setGradeName(db2);
            setSchoolName(db2);
            setLoginStaffName(db2);
            setRequireInfo(db2);
        }


        /** スタッフ名取得 */
        private void setLoginStaffName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _loginStaffName = null;
            try {
                String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _loginStaffCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _loginStaffName = rs.getString("STAFFNAME");
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 学年名取得 */
        private void setGradeName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeName = null;
            try {
                String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_selectSchoolKind)) {
                        sql += ("   AND SCHOOL_KIND IN (" + _selectSchoolKindIn + ") ");
                    }
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += ("   AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _gradeName = rs.getString("GRADE_NAME1") != null ? rs.getString("GRADE_NAME1") : "";
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 学校名取得 */
        private void setSchoolName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _schoolName = null;
            try {
                String sql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOLNAME1");
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 必履修備考取得 */
        private void setRequireInfo(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _requireInfo = null;
            StringBuffer info = new StringBuffer();
            try {
                String sql = "SELECT VALUE(NAMESPARE1,'') || ':' || VALUE(NAME1,'') AS NOTE FROM NAME_MST WHERE NAMECD1 = 'Z011' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                info.append("※必履修区分…");
                String noteSep = "";
                while (rs.next()) {
                    info.append(noteSep + rs.getString("NOTE"));
                    noteSep = "、";
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            _requireInfo = info.toString();
        }
    }
}

// eof
