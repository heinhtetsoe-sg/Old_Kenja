/*
 * $Id: 5de3b8437f6d6d86be3acd04a4fe4d39678b83f0 $
 *
 * 作成日: 2017/11/16
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL351W {

    private static final Log log = LogFactory.getLog(KNJL351W.class);

    private boolean _hasData;
    private final String NYUUGAKUTEIIN = "98";
    private final String BOSYUNINZU = "99";
    private final String GOUKEI = "9";

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

            //db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            //db2.open();

            db2 = new DB2UDB(request.getParameter("DBNAME2"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            for (Iterator itSchool = _param._schoolList.iterator(); itSchool.hasNext();) {
                final EdbSchoolMst edbSchoolMst = (EdbSchoolMst) itSchool.next();
                for (Iterator itRuikei = _param._ruikeiList.iterator(); itRuikei.hasNext();) {
                    final RuikeiData ruikeiData = (RuikeiData) itRuikei.next();
                    printMain(db2, svf, edbSchoolMst, ruikeiData);
                }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final EdbSchoolMst edbSchoolMst, final RuikeiData ruikeiData) {
        svf.VrSetForm("KNJL351W.frm", 4);
        final List titleList = (List) _param._outputTitleMap.get(ruikeiData._ruikeiDiv);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            setTitle(svf, edbSchoolMst, ruikeiData, titleList);

            final String sql = sql(edbSchoolMst, ruikeiData);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            String befCourseCd = "";
            final List cmcList = (List) edbSchoolMst._cmcMap.get(ruikeiData._ruikeiDiv);
            for (Iterator itCmc = cmcList.iterator(); itCmc.hasNext();) {
                final CMCdata cmcData = (CMCdata) itCmc.next();
                if (!"".equals(befCourseCd) && befCourseCd.equals(cmcData._courseCd)) {
                    svf.VrEndPage();
                    setTitle(svf, edbSchoolMst, ruikeiData, titleList);
                }
                if (!GOUKEI.equals(cmcData._courseCd)) {
                    svf.VrsOut("COURSE_NAME", cmcData._courseName);
                }
                final String cmcName = cmcData._cmcName;
                final String setField = KNJ_EditEdit.getMS932ByteLength(cmcName) > 20 ? "2" : "1";
                svf.VrsOut("MAJOR_NAME" + setField, cmcName);
                int numCnt = 1;
                for (Iterator itTest = titleList.iterator(); itTest.hasNext();) {
                    final TitleData titleData = (TitleData) itTest.next();
                    ps.setString(1, cmcData._courseCd);
                    ps.setString(2, cmcData._majorCd);
                    ps.setString(3, cmcData._courseCode);
                    ps.setString(4, titleData._largeDiv);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        svf.VrsOut("NUM" + numCnt, StringUtils.isNotEmpty(rs.getString("COUNT")) ? rs.getString("COUNT") : "");
                        numCnt++;
                    }
                    _hasData = true;
                }
                befCourseCd = cmcData._courseCd;
                svf.VrEndRecord();
            }


            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf, final EdbSchoolMst edbSchoolMst, final RuikeiData ruikeiData, final List titleList) {
        svf.VrsOut("SCHOOL_NAME", edbSchoolMst._edboardSchoolname);
        String titleField = KNJ_EditEdit.getMS932ByteLength(_param._testName) > 30 ? "60" : KNJ_EditEdit.getMS932ByteLength(_param._testName) > 20 ? "30" : KNJ_EditEdit.getMS932ByteLength(_param._testName) > 10 ? "20" : "10";
        svf.VrAttribute("TITLE" + titleField, "PAINT=(0,0,1)");
        svf.VrsOut("TITLE" + titleField, _param._testName);
        svf.VrsOut("DIV", ruikeiData._ruikeiName);
        svf.VrsOut("REPORT_DATE", edbSchoolMst._houkokuDate);
        int gyo = 1;
        for (Iterator itTest = titleList.iterator(); itTest.hasNext();) {
            final TitleData titleData = (TitleData) itTest.next();
            final String largeName = titleData._largeName;
            if (NYUUGAKUTEIIN.equals(titleData._largeDiv) || BOSYUNINZU.equals(titleData._largeDiv)) {
                continue;
            }
            final String setField = KNJ_EditEdit.getMS932ByteLength(largeName) > 18 ? "2" : "1";
            svf.VrsOutn("NUM_NAME" + setField, gyo, largeName);
            gyo++;
        }
    }

    private String sql(final EdbSchoolMst edbSchoolMst, final RuikeiData ruikeiData) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     AFT_SINRO_ADDITION_L350W_DAT ");
        stb.append(" WHERE ");
        stb.append("     EDBOARD_SCHOOLCD = '" + edbSchoolMst._edboardSchoolcd + "' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND RUIKEI_DIV = '" + ruikeiData._ruikeiDiv + "' ");
        stb.append("     AND TESTDIV2 = '" + _param._testdiv2 + "' ");
        stb.append("     AND COURSECD = ? ");
        stb.append("     AND MAJORCD = ? ");
        stb.append("     AND COURSECODE = ? ");
        stb.append("     AND LARGE_DIV = ? ");
        stb.append("     AND SEX = '9' ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 71133 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private class TitleData {
        final String _year;
        final String _applicantdiv;
        final String _testdiv;
        final String _ruikeiDiv;
        final String _testdiv2;
        final String _largeDiv;
        final String _largeName;
        public TitleData(
                final String year,
                final String applicantdiv,
                final String testdiv,
                final String ruikeiDiv,
                final String testdiv2,
                final String largeDiv,
                final String largeName
        ) {
            _year = year;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _ruikeiDiv = ruikeiDiv;
            _testdiv2 = testdiv2;
            _largeDiv = largeDiv;
            _largeName = largeName;
        }
    }

    private class EdbSchoolMst {
        final String _edboardSchoolcd;
        final String _edboardSchoolname;
        final String _houkokuDate;
        final Map _cmcMap;
        public EdbSchoolMst(
                final String edboardSchoolcd,
                final String edboardSchoolname,
                final String houkokuDate,
                final Map cmcMap
        ) {
            _edboardSchoolcd = edboardSchoolcd;
            _edboardSchoolname = edboardSchoolname;
            _houkokuDate = houkokuDate;
            _cmcMap = cmcMap;
        }
    }

    private class CMCdata {
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _courseCode;
        final String _cmcName;
        public CMCdata(
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String courseCode,
                final String cmcName
        ) {
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _cmcName = cmcName;
        }
    }

    private class RuikeiData {
        final String _ruikeiDiv;
        final String _ruikeiName;
        public RuikeiData(
                final String ruikeiDiv,
                final String ruikeiName
        ) {
            _ruikeiDiv = ruikeiDiv;
            _ruikeiName = ruikeiName;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _applicantdiv;
        private final String _testdiv;
        private final String _testdiv2;
        private final String _ruikeiDiv;
        private final String[] _selected;
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _ctrlSemester;
        private final String _prgid;
        private final Map _outputTitleMap;
        private final List _schoolList;
        private final String _testName;
        private final List _ruikeiList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, ParseException {
            _year = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _testdiv2 = StringUtils.isNotEmpty(request.getParameter("TESTDIV2")) ? request.getParameter("TESTDIV2") : "0";
            _ruikeiDiv = request.getParameter("RUIKEI_DIV");
            _selected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _prgid = request.getParameter("PRGID");
            _testName = getNameMst(db2, "L004", _testdiv, "NAME2");
            _ruikeiList = getRuikeiList(db2);
            _outputTitleMap = getTitleMap(db2);
            _schoolList = getSchoolList(db2);
        }

        private List getSchoolList(final DB2UDB db2) throws SQLException, ParseException {
            final List retList = new ArrayList();
            for (int i = 0; i < _selected.length; i++) {
                final String schoolCd = _selected[i];
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String titleSql = getSchoolSql(schoolCd);
                    ps = db2.prepareStatement(titleSql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String edboardSchoolcd = rs.getString("EDBOARD_SCHOOLCD");
                        final String edboardSchoolname = rs.getString("EDBOARD_SCHOOLNAME");
                        final String houkokuDate = getHoukokuData(db2, schoolCd);
                        final Map cmcMap = getCMCMap(db2, schoolCd);
                        final EdbSchoolMst edbSchoolMst = new EdbSchoolMst(edboardSchoolcd, edboardSchoolname, houkokuDate, cmcMap);
                        retList.add(edbSchoolMst);
                    }
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return retList;
        }

        private String getSchoolSql(final String schoolCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     EDBOARD_SCHOOLCD, ");
            stb.append("     EDBOARD_SCHOOLNAME ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     EDBOARD_SCHOOLCD = '" + schoolCd + "' ");

            return stb.toString();
        }

        private Map getCMCMap(final DB2UDB db2, final String schoolCd) throws SQLException {
            final Map retMap = new HashMap();
            for (Iterator iterator = _ruikeiList.iterator(); iterator.hasNext();) {
                final RuikeiData ruikeiData = (RuikeiData) iterator.next();
                final List retList = new ArrayList();
                final String titleSql = getCMCsql(schoolCd, ruikeiData);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(titleSql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String courseCd = rs.getString("COURSECD");
                        final String courseName = rs.getString("COURSENAME");
                        final String majorCd = rs.getString("MAJORCD");
                        final String courseCode = rs.getString("COURSECODE");
                        final String cmcName = rs.getString("CMC_NAME");
                        final CMCdata cmcData = new CMCdata(courseCd, courseName, majorCd, courseCode, cmcName);
                        retList.add(cmcData);
                    }
                    retMap.put(ruikeiData._ruikeiDiv, retList);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return retMap;
        }

        private String getCMCsql(final String schoolCd, final RuikeiData ruikeiData) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH TOTAL_T (COURSECD, COURSENAME, MAJORCD, COURSECODE, CMC_NAME) AS ( ");
            stb.append("     VALUES('9', '', '999', '9999', '合計') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     L350W.COURSECD, ");
            stb.append("     COURSE.COURSENAME, ");
            stb.append("     L350W.MAJORCD, ");
            stb.append("     L350W.COURSECODE, ");
            stb.append("     VALUE(MAJOR.MAJORNAME, '') || VALUE(COURSECODE.COURSECODENAME, '') AS CMC_NAME ");
            stb.append(" FROM ");
            stb.append("     V_AFT_SINRO_ADDITION_L350W_DAT L350W ");
            stb.append("     LEFT JOIN EDBOARD_COURSE_MST COURSE ON L350W.EDBOARD_SCHOOLCD = COURSE.EDBOARD_SCHOOLCD AND L350W.COURSECD = COURSE.COURSECD ");
            stb.append("     LEFT JOIN EDBOARD_MAJOR_MST MAJOR ON L350W.EDBOARD_SCHOOLCD = MAJOR.EDBOARD_SCHOOLCD AND L350W.COURSECD || L350W.MAJORCD = MAJOR.COURSECD || MAJOR.MAJORCD ");
            stb.append("     LEFT JOIN EDBOARD_COURSECODE_MST COURSECODE ON L350W.EDBOARD_SCHOOLCD = COURSECODE.EDBOARD_SCHOOLCD AND L350W.COURSECODE = COURSECODE.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     L350W.YEAR = '" + _year + "' ");
            stb.append("     AND L350W.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("     AND L350W.APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("     AND L350W.TESTDIV = '" + _testdiv + "' ");
            stb.append("     AND L350W.RUIKEI_DIV = '" + ruikeiData._ruikeiDiv + "' ");
            stb.append("     AND L350W.TESTDIV2 = '" + _testdiv2 + "' ");
            stb.append("     AND L350W.COURSECD != '9' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     COURSECD , ");
            stb.append("     COURSENAME , ");
            stb.append("     MAJORCD, ");
            stb.append("     COURSECODE, ");
            stb.append("     CMC_NAME ");
            stb.append(" FROM ");
            stb.append("     TOTAL_T ");
            stb.append(" ORDER BY ");
            stb.append("     COURSECD , ");
            stb.append("     MAJORCD, ");
            stb.append("     COURSECODE ");

            return stb.toString();
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String getFieldName) throws SQLException {
            String retStr = "";
            final String titleSql = getNameMstSql(namecd1, namecd2, getFieldName);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString(getFieldName);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2, final String getFieldName) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + getFieldName + " ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");

            return stb.toString();
        }

        private String getHoukokuData(final DB2UDB db2, final String schoolCd) throws SQLException, ParseException {
            String retStr = "1".equals(_testdiv2) ? "追検査" : "";
            final String titleSql = getHoukokuSql(schoolCd);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String executeDate = rs.getString("EXECUTE_DATE");
                    final String label = rs.getString("LABEL");
                    if (StringUtils.isNotEmpty(executeDate)) {
                        final String[] dateArray = StringUtils.split(executeDate, ".");
                        final String[] dateTime = StringUtils.split(dateArray[0], " ");
                        final String houkokuDate = KNJ_EditDate.h_format_JP(db2, dateTime[0]);
                        final String houkokuWeek = " (" + KNJ_EditDate.h_format_W(dateTime[0]) + ") ";
                        DateFormat sdf1 = new SimpleDateFormat("HH時mm分");
                        final String formatStr = sdf1.format(java.sql.Timestamp.valueOf(executeDate));
                        retStr = houkokuDate + houkokuWeek + " " + formatStr + " " + label;
                    } else {
                        retStr = label;
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getHoukokuSql(final String schoolCd) throws SQLException {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     T1.TESTDIV2, ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     MAX(T1.FIXED_FLG) AS FIXED_FLG, ");
            stb.append("     CASE WHEN T1.TESTDIV2 = '1' THEN '追検査' ELSE '' END AS LABEL ");
            stb.append(" FROM ");
            stb.append("     REPORT_AFT_SINRO_ADDITION_L350W_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L004' ");
            stb.append("          AND L2.NAMECD2 = T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'L040' ");
            stb.append("          AND L3.NAMECD2 = T1.RUIKEI_DIV ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ON T1.REGISTERCD = STAFF.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + schoolCd + "' ");
            stb.append("     AND T1.YEAR             = '" + _year + "' ");
            stb.append("     AND T1.APPLICANTDIV     = '" + _applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV          = '" + _testdiv + "' ");
            stb.append("     AND T1.RUIKEI_DIV       = '" + _ruikeiDiv + "' ");
            stb.append("     AND T1.TESTDIV2         = '" + _testdiv2 + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     T1.TESTDIV2, ");
            stb.append("     T1.EXECUTE_DATE, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     CASE WHEN T1.TESTDIV2 = '1' THEN '追検査' ELSE '' END ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXECUTE_DATE DESC, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     T1.TESTDIV2 ");
            stb.append(" FETCH FIRST 1 ROWS ONLY ");

            return stb.toString();
        }

        private List getRuikeiList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String titleSql = getRuikeiSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String ruikeiDiv = rs.getString("RUIKEI_DIV");
                    final String ruikeiName = rs.getString("NAME1");
                    final RuikeiData ruikeiData = new RuikeiData(ruikeiDiv, ruikeiName);
                    retList.add(ruikeiData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getRuikeiSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.RUIKEI_DIV, ");
            stb.append("     L1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     AFT_SINRO_ADDITION_L350W_FIELD_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L040' ");
            stb.append("          AND T1.RUIKEI_DIV = L1.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("     AND TESTDIV = '" + _testdiv + "' ");
            if (!"ALL".equals(_ruikeiDiv)) {
                stb.append("     AND RUIKEI_DIV = '" + _ruikeiDiv + "' ");
            }
            stb.append("     AND TESTDIV2 = '" + _testdiv2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.RUIKEI_DIV ");

            return stb.toString();
        }

        private Map getTitleMap(final DB2UDB db2) throws SQLException {
            final Map retMap = (Map) new HashMap();
            for (Iterator iterator = _ruikeiList.iterator(); iterator.hasNext();) {
                final RuikeiData ruikeiData = (RuikeiData) iterator.next();
                final List titleList = new ArrayList();
                final String titleSql = getTitleSql(ruikeiData._ruikeiDiv);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(titleSql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String applicantdiv = rs.getString("APPLICANTDIV");
                        final String testdiv = rs.getString("TESTDIV");
                        final String ruikeiDiv = rs.getString("RUIKEI_DIV");
                        final String testdiv2 = rs.getString("TESTDIV2");
                        final String largeDiv = rs.getString("LARGE_DIV");
                        final String largeName = rs.getString("LARGE_NAME");
                        final TitleData titleData = new TitleData(year, applicantdiv, testdiv, ruikeiDiv, testdiv2, largeDiv, largeName);
                        titleList.add(titleData);
                    }
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                }
                retMap.put(ruikeiData._ruikeiDiv, titleList);
            }
            return retMap;
        }

        private String getTitleSql(final String ruikeiDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     AFT_SINRO_ADDITION_L350W_FIELD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("     AND TESTDIV = '" + _testdiv + "' ");
            stb.append("     AND RUIKEI_DIV = '" + ruikeiDiv + "' ");
            stb.append("     AND TESTDIV2 = '" + _testdiv2 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CASE WHEN LARGE_DIV IN ('98', '99') THEN 1 ELSE 2 END, ");
            stb.append("     LARGE_DIV ");

            return stb.toString();
        }

    }
}

// eof
