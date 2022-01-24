/*
 * $Id: 081de4fb8ebb3b74af261d009e3471966e773432 $
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

public class KNJE433 {

    private static final Log log = LogFactory.getLog(KNJE433.class);

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
        svf.VrSetForm("KNJE433.frm", 4);
        svf.VrsOut("TITLE", _param._year + "年度　職業別就職者数");
        svf.VrsOut("DATE", "印刷日：" + _param._ctrlDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate));
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            MajorData majorData = (MajorData) iterator.next();
            final String majorSoeji = getMS932ByteLength(majorData._majorName) > 14 ? "2_1" : "";
            svf.VrsOut("MAJOR_NAME" + majorSoeji, majorData._majorName);
            int gyo = 1;
            for (Iterator itLine = majorData._injiLineList.iterator(); itLine.hasNext();) {
                final InjiLineData lineData = (InjiLineData) itLine.next();

                gyo = "2".equals(lineData._sex) ? 2 : gyo;
                svf.VrsOutn("MAJOR_CD",  gyo, majorData._majorCd);
                svf.VrsOutn("COURSECD",  gyo, majorData._courseCd);
                int soeji = 1;
                for (Iterator itCount = lineData._countList.iterator(); itCount.hasNext();) {
                    String setData = (String) itCount.next();
                    svf.VrsOutn("NUM" + String.valueOf(soeji),  gyo, setData);
                    soeji++;
                }
            }
            svf.VrEndRecord();
            _hasData = true;
        }
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

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
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
                for (int i = 1; i <= 16; i++) {
                    countList.add(rs.getString("COUNT" + String.valueOf(i)));
                }
                countList.add(rs.getString("COUNT97"));
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_COURSE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.COURSECD, ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T3.MAJORNAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         INNER JOIN V_COURSE_MAJOR_MST T3 ");
        stb.append("             ON  T3.YEAR     = T1.YEAR ");
        stb.append("             AND T3.COURSECD = T1.COURSECD ");
        stb.append("             AND T3.MAJORCD  = T1.MAJORCD ");
        stb.append("     WHERE ");
        stb.append("             T1.YEAR     = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
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
        for (int i = 1; i <= 16; i++) {
            stb.append("     L" + i + ".COUNT AS COUNT" + i + ", ");
        }
        stb.append("     L97.COUNT AS COUNT97, ");
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
        stb.append("         AFT_DISEASE_ADDITION433_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR = '" + _param._year + "' ");
        stb.append("             AND EDBOARD_SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("     GROUP BY ");
        stb.append("         EDBOARD_SCHOOLCD, ");
        stb.append("         YEAR, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         SEX ");
        stb.append("     ) T1 ");
        for (int i = 1; i <= 16; i++) {
            final String setDiv = i < 10 ? "0" + i : String.valueOf(i);
            stb.append("     LEFT JOIN AFT_DISEASE_ADDITION433_DAT L" + i + " ON T1.EDBOARD_SCHOOLCD = L" + i + ".EDBOARD_SCHOOLCD ");
            stb.append("          AND T1.YEAR = L" + i + ".YEAR ");
            stb.append("          AND T1.COURSECD = L" + i + ".COURSECD ");
            stb.append("          AND T1.MAJORCD = L" + i + ".MAJORCD ");
            stb.append("          AND T1.SEX = L" + i + ".SEX ");
            stb.append("          AND L" + i + ".LARGE_DIV = '" + setDiv + "' ");
        }
        stb.append("     LEFT JOIN AFT_DISEASE_ADDITION433_DAT L97 ON T1.EDBOARD_SCHOOLCD = L97.EDBOARD_SCHOOLCD ");
        stb.append("          AND T1.YEAR = L97.YEAR ");
        stb.append("          AND T1.COURSECD = L97.COURSECD ");
        stb.append("          AND T1.MAJORCD = L97.MAJORCD ");
        stb.append("          AND T1.SEX = L97.SEX ");
        stb.append("          AND L97.LARGE_DIV = '99' ");
        stb.append("          AND L97.MIDDLE_DIV = '99' ");
        stb.append("     LEFT JOIN AFT_DISEASE_ADDITION433_DAT L98 ON T1.EDBOARD_SCHOOLCD = L98.EDBOARD_SCHOOLCD ");
        stb.append("          AND T1.YEAR = L98.YEAR ");
        stb.append("          AND T1.COURSECD = L98.COURSECD ");
        stb.append("          AND T1.MAJORCD = L98.MAJORCD ");
        stb.append("          AND T1.SEX = L98.SEX ");
        stb.append("          AND L98.LARGE_DIV = '99' ");
        stb.append("          AND L98.MIDDLE_DIV = '01' ");
        stb.append("     LEFT JOIN AFT_DISEASE_ADDITION433_DAT L99 ON T1.EDBOARD_SCHOOLCD = L99.EDBOARD_SCHOOLCD ");
        stb.append("          AND T1.YEAR = L99.YEAR ");
        stb.append("          AND T1.COURSECD = L99.COURSECD ");
        stb.append("          AND T1.MAJORCD = L99.MAJORCD ");
        stb.append("          AND T1.SEX = L99.SEX ");
        stb.append("          AND L99.LARGE_DIV = '99' ");
        stb.append("          AND L99.MIDDLE_DIV = '02' ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.MAJORNAME, ");
        stb.append("     T1.SEX_NAME, ");
        for (int i = 1; i <= 16; i++) {
            stb.append("     L1.COUNT" + i + " AS COUNT" + i + ", ");
        }
        stb.append("     L1.COUNT97, ");
        stb.append("     L1.COUNT98, ");
        stb.append("     L1.COUNT99 ");
        stb.append(" FROM ");
        stb.append("     T_COURSE_SEX T1 ");
        stb.append("     LEFT JOIN T_ADDITION1 L1 ON L1.COURSECD = T1.COURSECD ");
        stb.append("          AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("          AND L1.SEX = T1.SEX ");
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
        private final String _schoolcd;
        private final List _industryList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schoolcd = request.getParameter("SCHOOLCD");
            _industryList = getIndustryList(db2);
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
}

// eof

