/*
 * $Id: 3abacb004d7a1a4bf37bf219e08de8ef557b42d2 $
 *
 * 作成日: 2017/05/26
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;


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

public class KNJB105 {

    private static final Log log = LogFactory.getLog(KNJB105.class);

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

            for (int courseCnt = 0; courseCnt < _param._categorySelected.length; courseCnt++) {
                final String gcmc = _param._categorySelected[courseCnt];
                printMain(db2, svf, gcmc);
            }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String gcmc) throws SQLException {
        svf.VrSetForm("KNJB105.frm", 4);
        svf.VrsOut("NENDO", _param._ctrlYear + "年度　教科・科目一覧");
        final String[] gcmcArray = StringUtils.split(gcmc, ':');
        final String gradeName = getGradeName(db2, gcmcArray);
        final String majorName = getMajorName(db2, gcmcArray);
        final String courseCodeName = getCourseCodeName(db2, gcmcArray);
        svf.VrsOut("GRADE", gradeName + "　" + majorName + courseCodeName);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(gcmc);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String classname = rs.getString("CLASSNAME");
                final String school_kind = rs.getString("SCHOOL_KIND");
                final String curriculum_cd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String hitsuri = rs.getString("HITSURI");
                final String credits = rs.getString("CREDITS");
                final String authorize = rs.getString("AUTHORIZE");
                final String classField = getMS932ByteLength(classname) > 16 ? "_3" : getMS932ByteLength(classname) > 12 ? "_2" : "_1";
                svf.VrsOut("CLASS_NAME" + classField, classname);
                svf.VrsOut("SUBCLASS_CD", classcd + ":" + school_kind + ":" +  curriculum_cd + ":" +  subclasscd);
                final String subclassField = getMS932ByteLength(subclassname) > 30 ? "_2" : "_1";
                svf.VrsOut("SUBCLASS" + subclassField, subclassname);
                svf.VrsOut("SEL_SUBCLASS", hitsuri);
                svf.VrsOut("CREDIT", credits);
                svf.VrsOut("AUTHORIZE_FLG", authorize);
                _hasData = true;
                svf.VrEndRecord();
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getGradeName(final DB2UDB db2, final String[] gcmcArray) throws SQLException {
        String retGradeName = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND GRADE = '" + gcmcArray[0] + "' ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                retGradeName = rs.getString("GRADE_NAME1");
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retGradeName;
    }

    private String getMajorName(final DB2UDB db2, final String[] gcmcArray) throws SQLException {
        String retMajorName = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MAJORNAME ");
        stb.append(" FROM ");
        stb.append("     MAJOR_MST ");
        stb.append(" WHERE ");
        stb.append("     COURSECD = '" + gcmcArray[1] + "' ");
        stb.append("     AND MAJORCD = '" + gcmcArray[2] + "' ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                retMajorName = rs.getString("MAJORNAME");
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMajorName;
    }

    private String getCourseCodeName(final DB2UDB db2, final String[] gcmcArray) throws SQLException {
        String retCourseCodeName = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     COURSECODE_MST ");
        stb.append(" WHERE ");
        stb.append("     COURSECODE = '" + gcmcArray[3] + "' ");

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                retCourseCodeName = rs.getString("COURSECODENAME");
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retCourseCodeName;
    }

    private String sql(final String gcmc) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CRE.CLASSCD, ");
        stb.append("     CLASSM.CLASSNAME, ");
        stb.append("     CRE.SCHOOL_KIND, ");
        stb.append("     CRE.CURRICULUM_CD, ");
        stb.append("     CRE.SUBCLASSCD, ");
        stb.append("     SUBM.SUBCLASSNAME, ");
        stb.append("     VALUE(Z011.ABBV1, '') AS HITSURI, ");
        stb.append("     CRE.CREDITS, ");
        stb.append("     CASE WHEN VALUE(CRE.AUTHORIZE_FLG, '0') = '1' THEN '半期' ELSE '' END AS AUTHORIZE ");
        stb.append(" FROM ");
        stb.append("     CREDIT_MST CRE ");
        stb.append("     LEFT JOIN CLASS_MST CLASSM ON CRE.CLASSCD = CLASSM.CLASSCD ");
        stb.append("          AND CRE.SCHOOL_KIND = CLASSM.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON CRE.CLASSCD = SUBM.CLASSCD ");
        stb.append("          AND CRE.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
        stb.append("          AND CRE.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
        stb.append("          AND CRE.SUBCLASSCD = SUBM.SUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST Z011 ON  Z011.NAMECD1 = 'Z011' ");
        stb.append("          AND CRE.REQUIRE_FLG = Z011.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     CRE.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND CRE.GRADE || ':' || CRE.COURSECD  || ':' || CRE.MAJORCD  || ':' || CRE.COURSECODE = '" + gcmc + "' ");
        stb.append(" ORDER BY ");
        stb.append("     CRE.CLASSCD, ");
        stb.append("     CRE.SCHOOL_KIND, ");
        stb.append("     CRE.CURRICULUM_CD, ");
        stb.append("     CRE.SUBCLASSCD ");
        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _schoolKind;
        final String[] _categorySelected;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _usecurriculumcd;
        final String _useSchoolDetailGcmDat;
        final String _useschoolKindfield;
        final String _schoolkind;
        final String _schoolcd;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _printLogStaffcd;
        final String _printLogRemoteAddr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useSchoolDetailGcmDat = request.getParameter("use_school_detail_gcm_dat");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
        }

    }
}

// eof

