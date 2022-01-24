/*
 * $Id: 8a75166c327de5d232c9fa731450ff7af9ce4424 $
 *
 * 作成日: 2017/09/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJH132 {

    private static final Log log = LogFactory.getLog(KNJH132.class);

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

            printMain(db2, svf, response);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final HttpServletResponse response) {

        if ("csv".equals(_param._cmd)) {
            printCsv(db2, response);
        } else {
            printSvf(db2, svf);
        }
    }

    private void printCsv(final DB2UDB db2, final HttpServletResponse response) {
        final List outputLines = new ArrayList();
        final String title = "地区別児童一覧";
        final Map printAreaMap = getPrintAreaList(db2);
        for (Iterator itArea = printAreaMap.keySet().iterator(); itArea.hasNext();) {
            final List headerList = new ArrayList();
            final List headerList2 = new ArrayList();
            final List headerList3 = new ArrayList();
            final List yohakuList = new ArrayList();

            final String areaCd = (String) itArea.next();
            final Area area = (Area) printAreaMap.get(areaCd);
            final String settingName = area.getTitle(db2);
            headerList.add("");
            headerList.add("地区別" + settingName + "一覧(" + area._areaName + ")");
            outputLines.add(headerList);

            headerList2.add(settingName + "数：" + area._studentList.size() + "名");
            headerList2.add("");
            headerList2.add("");
            headerList2.add("作成日：" + KNJ_EditDate.h_format_JP(_param._loginDate));
            outputLines.add(headerList2);

            headerList3.add("年組番");
            headerList3.add(settingName + "氏名");
            headerList3.add("郵便番号");
            headerList3.add("現住所");
            outputLines.add(headerList3);

            for (Iterator itStudent = area._studentList.iterator(); itStudent.hasNext();) {
                final List lineData = new ArrayList();

                final Student student = (Student) itStudent.next();
                lineData.add(student._hrName + student._attendno + "番");
                lineData.add(student._name);
                lineData.add(null == student._zipcd ? "" : student._zipcd);
                final String addr1 = null == student._addr1 ? "" : student._addr1;
                final String addr2 = null == student._addr2 ? "" : student._addr2;
                lineData.add(addr1 + addr2);
                outputLines.add(lineData);
            }
            outputLines.add(yohakuList);
            _hasData = true;
        }
        if (!outputLines.isEmpty()) {
            CsvUtils.outputLines(log, response, title + ".csv", outputLines);
        }
    }

    private void printSvf(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJH132.frm", 1);
        final int maxLine = 20;
        final Map printAreaMap = getPrintAreaList(db2);
        for (Iterator itArea = printAreaMap.keySet().iterator(); itArea.hasNext();) {
            final String areaCd = (String) itArea.next();
            final Area area = (Area) printAreaMap.get(areaCd);
            final int page1 = area._studentList.size() / maxLine;
            final int page2 = area._studentList.size() % maxLine > 0 ? 1 : 0;
            final int maxPage = page1 + page2;

            final String settingName = area.getTitle(db2);
            setTitle(svf, area, settingName);
            int pageCnt = 1;
            int lineCnt = 1;
            for (Iterator itStudent = area._studentList.iterator(); itStudent.hasNext();) {
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    setTitle(svf, area, settingName);
                    pageCnt++;
                    lineCnt = 1;
                }
                svf.VrsOut("PAGE", pageCnt + "/" + maxPage + "ページ");

                final Student student = (Student) itStudent.next();
                svf.VrsOutn("HR_NAME", lineCnt, student._hrName + student._attendno + "番");
                final String nameField = getMS932ByteLength(student._name) > 60 ? "3" : getMS932ByteLength(student._name) > 40 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
                svf.VrsOutn("ZIP_NO", lineCnt, student._zipcd);
                final String addrField = getMS932ByteLength(student._addr1) > 80 || getMS932ByteLength(student._addr2) > 80 ? "2" : "1";
                svf.VrsOutn("ADDR1_" + addrField, lineCnt, student._addr1);
                svf.VrsOutn("ADDR2_" + addrField, lineCnt, student._addr2);
                lineCnt++;
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final Area area, final String settingName) {
        svf.VrsOut("TITLE", "地区別" + settingName + "一覧(" + area._areaName + ")");
        svf.VrsOut("NAME_HEADER", settingName + "氏名");
        svf.VrsOut("NUM", settingName + "数：" + area._studentList.size() + "名");
        svf.VrsOut("DATE", "作成日：" + KNJ_EditDate.h_format_JP(_param._loginDate));
    }

    private static int getMS932ByteLength(final String s) {
        int len = 0;
        try {
            if (null != s) {
                len = s.getBytes("MS932").length;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return len;
    }

    private Map getPrintAreaList(final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = studentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befSchoolKind = "";
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String areaCd = rs.getString("AREACD");
                final String areaName = rs.getString("AREA_NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");

                Area area = null;
                if (retMap.containsKey(areaCd)) {
                    area = (Area) retMap.get(areaCd);
                    if (!befSchoolKind.equals(schoolKind)) {
                        area._haveSchoolKinds = true;
                    }
                } else {
                    area = new Area(areaCd, areaName);
                    befSchoolKind = "";
                }
                area._schoolKind = schoolKind;
                final Student student = new Student(grade, hrClass, hrName, attendno, schregno, name, zipcd, addr1, addr2);
                area._studentList.add(student);
                retMap.put(areaCd, area);
                befSchoolKind = schoolKind;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String studentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     CASE WHEN VALUE(ADDR.AREACD, '999') = '999' ");
        stb.append("          THEN '999' ");
        stb.append("          ELSE ADDR.AREACD ");
        stb.append("     END AS AREACD, ");
        stb.append("     CASE WHEN VALUE(ADDR.AREACD, '999') = '999' ");
        stb.append("          THEN '未登録' ");
        stb.append("          ELSE A020.NAME1 ");
        stb.append("     END AS AREA_NAME, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDR1, ");
        stb.append("     ADDR.ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("          AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             LADDR.* ");
        stb.append("         FROM ");
        stb.append("             SCHREG_ADDRESS_DAT LADDR, ");
        stb.append("             (SELECT ");
        stb.append("                 MAX_ADDR.SCHREGNO, ");
        stb.append("                 MAX(MAX_ADDR.ISSUEDATE) AS ISSUEDATE ");
        stb.append("              FROM ");
        stb.append("                 SCHREG_ADDRESS_DAT MAX_ADDR ");
        stb.append("              GROUP BY ");
        stb.append("                 MAX_ADDR.SCHREGNO ");
        stb.append("             ) MAX_KEY ");
        stb.append("         WHERE ");
        stb.append("             LADDR.SCHREGNO = MAX_KEY.SCHREGNO ");
        stb.append("             AND LADDR.ISSUEDATE = MAX_KEY.ISSUEDATE ");
        stb.append("     ) ADDR ON REGD.SCHREGNO = ADDR.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST A020 ON A020.NAMECD1 = 'A020' ");
        stb.append("          AND ADDR.AREACD = A020.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._hrClassInState + ") ");
        stb.append("     AND VALUE(ADDR.AREACD, '999') IN (" + _param._areaInState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     ADDR.AREACD, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    private class Area {
        private final String _areaCd;
        private final String _areaName;
        private String _schoolKind;
        private boolean _haveSchoolKinds;
        private final List _studentList;
        public Area(
                final String areaCd,
                final String areaName
        ) {
            _areaCd = areaCd;
            _areaName = areaName;
            _studentList = new ArrayList();
            _haveSchoolKinds = false;
        }

        public String getTitle(final DB2UDB db2) {
            String retStr = "生徒";
            if (_haveSchoolKinds) {
                return retStr;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = settingSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("REMARK1");
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String settingSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARK1 ");
            stb.append(" FROM ");
            stb.append("     SETTING_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHOOLCD = '" + _param._schoolcd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND SEQ = '001' ");
            return stb.toString();
        }
    }

    private class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        public Student(
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schregno,
                final String name,
                final String zipcd,
                final String addr1,
                final String addr2
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
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
        private final String _semester;
        private final String[] _categorySelected;
        private final String[] _areaCategorySelected;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _loginDate;
        private final String _schoolcd;
        private final String _cmd;
        private final String _usePrgSchoolkind;
        private final String _printLogStaffcd;
        private final String _printLogRemoteAddr;
        private final String _hrClassInState;
        private final String _areaInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolcd = request.getParameter("SCHOOLCD");
            _cmd = request.getParameter("cmd");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            String setHrClass = "";
            String sep = "";
            for (int hrCnt = 0; hrCnt < _categorySelected.length; hrCnt++) {
                final String hrClass = _categorySelected[hrCnt];
                setHrClass += sep + "'" + hrClass + "'";
                sep = ",";
            }
            _hrClassInState = setHrClass;

            _areaCategorySelected = request.getParameterValues("AREA_CATEGORY_SELECTED");
            String setArea = "";
            sep = "";
            for (int areaCnt = 0; areaCnt < _areaCategorySelected.length; areaCnt++) {
                final String area = _areaCategorySelected[areaCnt];
                setArea += sep + "'" + area + "'";
                sep = ",";
            }
            _areaInState = setArea;
        }

    }
}
// eof

