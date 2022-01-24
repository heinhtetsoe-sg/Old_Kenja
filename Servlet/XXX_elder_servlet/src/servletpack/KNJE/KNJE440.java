/*
 * $Id: 9e8412f3908d8ee67238944430819071ae4f518e $
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

public class KNJE440 {

    private static final Log log = LogFactory.getLog(KNJE440.class);

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
        svf.VrSetForm("KNJE440.frm", 4);
        svf.VrsOut("TITLE", _param._year + "年度　状況別卒業者数");
        svf.VrsOut("DATE", "印刷日：" + _param._ctrlDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate));

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            MajorData majorData = (MajorData) iterator.next();
            final String majorSoeji = getMS932ByteLength(majorData._majorName) > 10 ? "2_1" : "";
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
                for (Iterator itName = _param._nameList.iterator(); itName.hasNext();) {
                    final String nameCd = (String) itName.next();
                    countList.add(rs.getString("CNT" + nameCd));
                }

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

        stb.append(" WITH T_MAJOR (COURSECD, MAJORCD, MAJORNAME) AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.COURSECD, ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T2.MAJORNAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN V_COURSE_MAJOR_MST T2 ");
        stb.append("             ON T2.YEAR = T1.YEAR ");
        stb.append("            AND T2.COURSECD = T1.COURSECD ");
        stb.append("            AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("     WHERE ");
        stb.append("             T1.YEAR     = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
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
        stb.append(" , T_MAJOR_SEX AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.*, ");
        stb.append("         T2.* ");
        stb.append("     FROM ");
        stb.append("         T_MAJOR T1, ");
        stb.append("         T_SEX T2 ");
        stb.append("     ) ");
        stb.append(" , T_ADDITION AS ( ");
        stb.append("     SELECT ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         SEX ");
        for (Iterator itName = _param._nameList.iterator(); itName.hasNext();) {
            final String nameCd = (String) itName.next();
            stb.append("         ,SUM(CASE (LARGE_DIV || MIDDLE_DIV || SMALL_DIV) WHEN '" + nameCd + "' THEN COUNT ELSE 0 END) AS CNT" + nameCd + " ");
        }
        stb.append("     FROM ");
        stb.append("         AFT_DISEASE_ADDITION440_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("         AND YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         SEX ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.MAJORNAME, ");
        stb.append("     T1.SEX_NAME ");
        for (Iterator itName = _param._nameList.iterator(); itName.hasNext();) {
            final String nameCd = (String) itName.next();
            stb.append("     ,L1.CNT" + nameCd );
        }
        stb.append(" FROM ");
        stb.append("     T_MAJOR_SEX T1 ");
        stb.append("     LEFT JOIN T_ADDITION L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD AND L1.SEX = T1.SEX ");
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
        private final List _nameList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _schoolcd = request.getParameter("SCHOOLCD");
            _nameList = getNameList();
        }

        private List getNameList() {
            final List retList = new ArrayList();
            retList.add("0101999");
            retList.add("0102999");
            retList.add("0103999");
            retList.add("0104999");
            retList.add("0105999");
            retList.add("0106999");
            retList.add("0299999");
            retList.add("0301999");
            retList.add("0302999");
            retList.add("0499999");
            retList.add("0599999");
            retList.add("0699999");
            retList.add("0799999");
            retList.add("0899999");
            retList.add("9999999");
            retList.add("9901999");
            retList.add("9902999");
            retList.add("9903999");
            retList.add("9904999");
            retList.add("1001999");
            retList.add("1002999");
            retList.add("1101999");
            retList.add("1102999");
            retList.add("1103999");
            retList.add("1104999");

            return retList;
        }

    }
}

// eof

