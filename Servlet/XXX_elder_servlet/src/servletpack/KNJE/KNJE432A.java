/*
 * $Id: ff48bcced0492455a5180a90f85d900cef3a1200 $
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

public class KNJE432A {

    private static final Log log = LogFactory.getLog(KNJE432A.class);

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
            svf.VrSetForm("KNJE432.frm", 4);
            printOut(db2, svf, null, "", "");
        } else {
            svf.VrSetForm("KNJE432A.frm", 4);
            for (Iterator itEdbord = _param._schoolList.iterator(); itEdbord.hasNext();) {
                EdboardSchool edboardSchool = (EdboardSchool) itEdbord.next();
                printOut(db2, svf, edboardSchool, "1_", "1");
            }
        }
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final EdboardSchool edboardSchool, final String fieldName, final String fieldName2) {
        svf.VrsOut("TITLE", _param._year + "年度　就職先の産業別就職者数");
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
                svf.VrsOutn("MAJOR_CD" + fieldName2,  gyo, majorData._majorCd);
                svf.VrsOutn("COURSECD" + fieldName2,  gyo, majorData._courseCd);
                int soeji = 1;
                for (Iterator itCount = lineData._countList.iterator(); itCount.hasNext();) {
                    String setData = (String) itCount.next();
                    svf.VrsOutn("NUM" + fieldName + String.valueOf(soeji),  gyo, setData);
                    soeji++;
                }
            }
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void printCover(final Vrw32alp svf) {
        svf.VrSetForm("KNJE_COVER.frm", 1);
        svf.VrsOut("TITLE", _param._year + "年度　就職先の産業別就職者数");
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
                final List countList = new ArrayList();
                for (int industryCnt = 1; industryCnt <= (_param._industryList.size() - 1) * 2; industryCnt++) {
                    countList.add(rs.getString("COUNT" + String.valueOf(industryCnt)));
                }
                countList.add(rs.getString("COUNT98"));
                countList.add(rs.getString("COUNT99"));

                if (befMajor.equals("")) {
                    majorData = new MajorData(courseCd, majorCd, majorName);
                    retList.add(majorData);
                } else if (!befMajor.equals(courseCd + majorCd)) {
                    majorData = new MajorData(courseCd, majorCd, majorName);
                    retList.add(majorData);
                }
                majorData.setLineData(sex, sexName, countList);
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
        stb.append(" WITH T_COURSE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.COURSECD, ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T3.MAJORNAME ");
        stb.append("     FROM ");
        stb.append("         AFT_DISEASE_ADDITION432_DAT T1 ");
        stb.append("         INNER JOIN V_COURSE_MAJOR_MST T3 ");
        stb.append("             ON  T3.YEAR     = T1.YEAR ");
        stb.append("             AND T3.COURSECD = T1.COURSECD ");
        stb.append("             AND T3.MAJORCD  = T1.MAJORCD ");
        stb.append("     WHERE ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("         T1.EDBOARD_SCHOOLCD IN (" + _param._schoolInState + ") ");
        } else {
            stb.append("         T1.EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' ");
        }
        stb.append("         AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.COURSECD, ");
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
        stb.append("     ) ");
        stb.append(" , T_COURSE_SEX AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.*, ");
        stb.append("         T2.* ");
        stb.append("     FROM ");
        stb.append("         T_COURSE T1, ");
        stb.append("         T_SEX T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.SEX != '9' ");
        stb.append("     ) ");
        stb.append(" , T_ADDITION1 AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EDBOARD_SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        for (int industryCnt = 1; industryCnt <= (_param._industryList.size() - 1) * 2; industryCnt++) {
            stb.append("     L" + industryCnt + ".COUNT AS COUNT" + industryCnt + ", ");
        }
        stb.append("     L98.COUNT AS COUNT98, ");
        stb.append("     L99.COUNT AS COUNT99 ");
        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         EDBOARD_SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         SEX ");
        stb.append("     FROM ");
        stb.append("         AFT_DISEASE_ADDITION432_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("             AND EDBOARD_SCHOOLCD IN (" + _param._schoolInState + ") ");
        } else {
            stb.append("             AND EDBOARD_SCHOOLCD = '" + edboardSchool._schoolCd + "' ");
        }
        stb.append("     GROUP BY ");
        stb.append("         EDBOARD_SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         SEX ");
        stb.append("     ) T1 ");
        int setCnt = 1;
        for (int industryCnt = 1; industryCnt <= (_param._industryList.size() - 1); industryCnt++) {
            final String setDiv = industryCnt < 10 ? "0" + industryCnt : String.valueOf(industryCnt);
            //県内
            stb.append("     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L" + setCnt + " ON T1.EDBOARD_SCHOOLCD = L" + setCnt + ".EDBOARD_SCHOOLCD ");
            stb.append("          AND T1.YEAR = L" + setCnt + ".YEAR ");
            stb.append("          AND T1.COURSECD = L" + setCnt + ".COURSECD ");
            stb.append("          AND T1.MAJORCD = L" + setCnt + ".MAJORCD ");
            stb.append("          AND T1.SEX = L" + setCnt + ".SEX ");
            stb.append("          AND L" + setCnt + ".LARGE_DIV = '" + setDiv + "' ");
            stb.append("          AND L" + setCnt + ".SMALL_DIV = '001' ");
            setCnt++;
            //県外
            stb.append("     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L" + setCnt + " ON T1.EDBOARD_SCHOOLCD = L" + setCnt + ".EDBOARD_SCHOOLCD ");
            stb.append("          AND T1.YEAR = L" + setCnt + ".YEAR ");
            stb.append("          AND T1.COURSECD = L" + setCnt + ".COURSECD ");
            stb.append("          AND T1.MAJORCD = L" + setCnt + ".MAJORCD ");
            stb.append("          AND T1.SEX = L" + setCnt + ".SEX ");
            stb.append("          AND L" + setCnt + ".LARGE_DIV = '" + setDiv + "' ");
            stb.append("          AND L" + setCnt + ".SMALL_DIV = '002' ");
            setCnt++;
        }
        stb.append("     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L98 ON T1.EDBOARD_SCHOOLCD = L98.EDBOARD_SCHOOLCD ");
        stb.append("          AND T1.YEAR = L98.YEAR ");
        stb.append("          AND T1.COURSECD = L98.COURSECD ");
        stb.append("          AND T1.MAJORCD = L98.MAJORCD ");
        stb.append("          AND T1.SEX = L98.SEX ");
        stb.append("          AND L98.LARGE_DIV = '99' ");
        stb.append("          AND L98.SMALL_DIV = '001' ");
        stb.append("     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L99 ON T1.EDBOARD_SCHOOLCD = L99.EDBOARD_SCHOOLCD ");
        stb.append("          AND T1.YEAR = L99.YEAR ");
        stb.append("          AND T1.COURSECD = L99.COURSECD ");
        stb.append("          AND T1.MAJORCD = L99.MAJORCD ");
        stb.append("          AND T1.SEX = L99.SEX ");
        stb.append("          AND L99.LARGE_DIV = '99' ");
        stb.append("          AND L99.SMALL_DIV = '002' ");
        stb.append("     ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.MAJORNAME, ");
        stb.append("     T1.SEX_NAME, ");
        for (int industryCnt = 1; industryCnt <= (_param._industryList.size() - 1) * 2; industryCnt++) {
            stb.append("     SUM(L1.COUNT" + industryCnt + ") AS COUNT" + industryCnt + ", ");
        }
        stb.append("     SUM(L1.COUNT98) AS COUNT98, ");
        stb.append("     SUM(L1.COUNT99) AS COUNT99 ");
        stb.append(" FROM ");
        stb.append("     T_COURSE_SEX T1 ");
        stb.append("     LEFT JOIN T_ADDITION1 L1 ON L1.COURSECD = T1.COURSECD ");
        stb.append("          AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("          AND L1.SEX = T1.SEX ");
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
                final List countList
        ) {
            final InjiLineData lineData = new InjiLineData(sex, sexName, countList);
            _injiLineList.add(lineData);
        }
    }

    private class InjiLineData {
        final String _sex;
        final String _sexName;
        final List _countList;
        public InjiLineData(
                final String sex,
                final String sexName,
                final List countList
        ) {
            _sex        = sex;
            _sexName    = sexName;
            _countList  = countList;
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
        private final List _industryList;
        private final List _schoolList;
        private String _schoolInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _industryList = getIndustryList(db2);
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

        private List getIndustryList(final DB2UDB db2) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String prefSql = getIndustry();
                ps = db2.prepareStatement(prefSql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final IndustryL industryL = new IndustryL(rs.getString("INDUSTRY_LCD"), rs.getString("INDUSTRY_LNAME"));
                    retList.add(industryL);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String getIndustry() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH INDUSTRY (INDUSTRY_LCD, INDUSTRY_LNAME, SORT) AS ( ");
            stb.append("     SELECT ");
            stb.append("         INDUSTRY_LCD, ");
            stb.append("         INDUSTRY_LNAME, ");
            stb.append("         INDUSTRY_LCD ");
            stb.append("     FROM ");
            stb.append("         INDUSTRY_L_MST ");
            stb.append("     UNION ");
            stb.append("         VALUES('', '計', 'ZZ') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     INDUSTRY ");
            stb.append(" ORDER BY ");
            stb.append("     SORT ");

            return stb.toString();
        }

    }

    private class IndustryL {
        final String _industryLcd;
        final String _industryLname;
        public IndustryL(
                final String industryLcd,
                final String industryLname
        ) {
            _industryLcd    = industryLcd;
            _industryLname  = industryLname;
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

