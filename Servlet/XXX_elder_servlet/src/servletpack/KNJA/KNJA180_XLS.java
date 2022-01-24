// kanji=漢字
/*
 * $Id: 5c93c8400ba335640ae9cc05cf6f2a94e8e9ab86 $
 *
 * 作成日: 2011/04/11 18:11:59 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 5c93c8400ba335640ae9cc05cf6f2a94e8e9ab86 $
 */
public class KNJA180_XLS extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA180_XLS.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }

    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        final String[] cols = getCols();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                xlsData.add(_param._courseMajorName);
                xlsData.add(_param._courseCodeName);
                for (int i = 0; i < cols.length; i++) {
                    if ("BIRTHDAY".equals(cols[i])) {
                        final String setBirthDay = KNJ_EditDate.h_format_JP(rsXls.getString(cols[i]));
                        xlsData.add(setBirthDay);
                    } else if ("HR_NAMEABBV".equals(cols[i])) {
                        final String setHrNameAbbv = rsXls.getString(cols[i]);
                        final int setAttendNo = rsXls.getInt("ATTENDNO");
                        xlsData.add(setHrNameAbbv + "-" + setAttendNo);
                    } else {
                        xlsData.add(rsXls.getString(cols[i]));
                    }
                }
                xlsData.add("DUMMY");
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("課程学科");
        retList.add("コース");
        retList.add("生徒手帳番号");
        retList.add("年組-番号");
        retList.add("氏名");
        retList.add("ふりがな");
        retList.add("生年月日");
        retList.add("郵便番号");
        retList.add("現住所");
        retList.add("電話番号");
        retList.add("保護者氏名");
        retList.add("急用電話番号");
        retList.add("中学校");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "HR_NAMEABBV",
                "SEITO_KANJI",
                "SEITO_KANA",
                "BIRTHDAY",
                "ZIPCD1",
                "ADDRESS1",
                "TELNO1",
                "GUARD_NAME",
                "TELNO2",
                "J_NAME",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer hrInState = new StringBuffer();
        String sep = "";
        for (int i = 0; i < _param._selectdata.length; i++) {
            hrInState.append(sep + "'" + _param._selectdata[i] + "'");
            sep = ",";
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T3, ");
        stb.append("     SCHREG_REGD_DAT T1, ");
        stb.append("     SCHREG_REGD_HDAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     T2.YEAR     = '" + _param._ctrlYear + "' AND ");
        stb.append("     T2.GRADE || T2.HR_CLASS IN (" + hrInState + ") AND ");
        stb.append("     T1.YEAR     = T2.YEAR AND ");
        stb.append("     T1.SEMESTER = T2.SEMESTER AND ");
        stb.append("     T1.GRADE    = T2.GRADE AND ");
        stb.append("     T1.HR_CLASS = T2.HR_CLASS AND ");
        stb.append("     T1.SCHREGNO = T3.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     DB1.GRADE, ");
        stb.append("     DB1.HR_CLASS, ");
        stb.append("     DB1.SCHREGNO, ");
        stb.append("     DB7.HR_NAMEABBV, ");
        stb.append("     DB1.ATTENDNO, ");
        stb.append("     DB2.NAME AS SEITO_KANJI, ");
        stb.append("     VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ");
        stb.append("     VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY, ");
        stb.append("     VALUE(DB3.ZIPCD,'') AS ZIPCD1, ");
        stb.append("     VALUE(DB3.ADDR1,'') AS ADDRESS1, ");
        if (_param._telFlg) {
            stb.append("     VALUE(DB3.TELNO,'') AS TELNO1, ");
        } else {
            stb.append("     '' AS TELNO1, ");
        }
        stb.append("     VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME, ");
        if (_param._telFlg) {
            stb.append("     VALUE(DB3.EMERGENCYTELNO,'') AS TELNO2, ");
        } else {
            stb.append("     '' AS TELNO2, ");
        }
        stb.append("     VALUE(DB6.FINSCHOOL_NAME,'')    AS J_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT                 DB1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST      DB2 ON DB1.SCHREGNO = DB2.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT     DB7 ON DB1.YEAR = DB7.YEAR ");
        stb.append("                                            AND DB1.SEMESTER = DB7.SEMESTER ");
        stb.append("                                            AND DB1.GRADE = DB7.GRADE ");
        stb.append("                                            AND DB1.HR_CLASS = DB7.HR_CLASS ");
        stb.append("     LEFT  JOIN GUARDIAN_DAT         DB4 ON DB2.SCHREGNO = DB4.SCHREGNO ");
        stb.append("     LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                     W1.SCHREGNO, ");
        stb.append("                     ZIPCD, ");
        stb.append("                     TELNO, ");
        stb.append("                     EMERGENCYTELNO, ");
        stb.append("                 ADDR1, ");
        stb.append("                 ADDR2 ");
        stb.append("                 FROM ");
        stb.append("                     SCHREG_ADDRESS_DAT W1 ");
        stb.append("                     LEFT JOIN SCHREG_BASE_MST L1 ON W1.SCHREGNO = L1.SCHREGNO ");
        stb.append("                 WHERE ");
        stb.append("                     (W1.SCHREGNO,W1.ISSUEDATE) IN (SELECT ");
        stb.append("                                                         SCHREGNO, ");
        stb.append("                                                         MAX(ISSUEDATE) ");
        stb.append("                                                     FROM ");
        stb.append("                                                         SCHREG_ADDRESS_DAT W2 ");
        stb.append("                                                     WHERE ");
        stb.append("                                                         W2.ISSUEDATE <= '" + _param._eDay + "' ");
        stb.append("                                                         AND (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '" + _param._sDay + "') AND ");
        stb.append("                                                         W2.SCHREGNO IN (SELECT II1.SCHREGNO FROM SCH_T II1) ");
        stb.append("                                                     GROUP BY ");
        stb.append("                                                         SCHREGNO ");
        stb.append("                                                     ) ");
        stb.append("                 )DB3 ON DB3.SCHREGNO = DB1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     DB1.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("      AND DB1.SEMESTER = '" + _param._semester + "' ");
        stb.append("      AND DB1.SCHREGNO IN (SELECT SCH_T.SCHREGNO FROM SCH_T) ");
        stb.append("      AND DB1.COURSECD || DB1.MAJORCD = '" + _param._courseMajor + "' ");
        stb.append("      AND DB1.COURSECODE = '" + _param._courseCode + "' ");
        stb.append(" ORDER BY DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO ");

        return stb.toString();
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
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String[] _selectdata;
        private final boolean _telFlg;
        private final String _semester;
        private final String _sDay;
        private final String _eDay;
        private final boolean _header;
        private final String _courseMajor;
        private final String _courseCode;
        private final String _courseMajorName;
        private final String _courseCodeName;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _selectdata = request.getParameterValues("CLASS_SELECTED");
            _telFlg = request.getParameter("TEL") == null ? false : true;
            _semester = request.getParameter("GAKKI");
            _sDay = request.getParameter("SDAY");
            _eDay = request.getParameter("EDAY");
            _header = true;
            _courseMajor = request.getParameter("COURSE_MAJOR_NAME");
            _courseCode = request.getParameter("COURSECODE");
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _courseCodeName = getCourseCode(db2);
            _courseMajorName = getCourseMajor(db2);
        }

        private String getCourseCode(final DB2UDB db2) throws SQLException {
            String retCourse = "";
            final String sql = "SELECT VALUE(COURSECODENAME,'') AS NAME FROM V_COURSECODE_MST WHERE YEAR = '" + _ctrlYear + "' AND COURSECODE = '" + _courseCode + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                retCourse = rs.getString("NAME");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retCourse;
        }

        private String getCourseMajor(final DB2UDB db2) throws SQLException {
            String retCourseMajor = "";
            final String sql = "SELECT VALUE(COURSENAME,'') || VALUE(MAJORNAME,'') AS COURSE FROM V_COURSE_MAJOR_MST WHERE YEAR = '" + _ctrlYear + "' AND COURSECD || MAJORCD ='" + _courseMajor + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                retCourseMajor = rs.getString("COURSE");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retCourseMajor;
        }

    }
}

// eof
