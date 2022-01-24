// kanji=漢字
/*
 * $Id: 94e72950d3a6f5c48427fd9f732a5e045d2db214 $
 *
 * 作成日: 2011/04/13 17:39:44 - JST
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
 * @version $Id: 94e72950d3a6f5c48427fd9f732a5e045d2db214 $
 */
public class KNJA170_XLS extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA170_XLS.class");

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
            for (int sCnt = 0; sCnt < _param._selectdata.length; sCnt++) {
                final String schregNo = _param._selectdata[sCnt];
                int setCnt = 1;
                psXls.setString(setCnt++, schregNo);
                psXls.setString(setCnt++, schregNo);
                rsXls = psXls.executeQuery();
                while (rsXls.next()) {
                    final List xlsData = new ArrayList();
                    xlsData.add(_param._hrName);
                    xlsData.add(_param._staffName);
                    for (int i = 0; i < cols.length; i++) {
                        if ("BIRTHDAY".equals(cols[i])) {
                            final String setBirthDay = KNJ_EditDate.h_format_JP(rsXls.getString(cols[i]));
                            xlsData.add(setBirthDay);
                        } else {
                            xlsData.add(rsXls.getString(cols[i]));
                        }
                    }
                    xlsData.add("DUMMY");
                    dataList.add(xlsData);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("年組");
        retList.add("担任名");
        retList.add("生徒手帳番号");
        retList.add("学級番号");
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
                "ATTENDNO",
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
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     DB1.SCHREGNO, ");
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
        stb.append("     VALUE(DB6.FINSCHOOL_NAME,'') AS J_NAME ");
        stb.append(" FROM  ");
        stb.append("     SCHREG_REGD_DAT DB1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST      DB2 ON DB1.SCHREGNO = DB2.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT     DB7 ON DB1.YEAR = DB7.YEAR ");
        stb.append("                                         AND DB1.SEMESTER = DB7.SEMESTER ");
        stb.append("                                         AND DB1.GRADE = DB7.GRADE ");
        stb.append("                                         AND DB1.HR_CLASS = DB7.HR_CLASS ");
        stb.append("     LEFT  JOIN GUARDIAN_DAT         DB4 ON DB2.SCHREGNO = DB4.SCHREGNO ");
        stb.append("     LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ( ");
        stb.append(" SELECT ");
        stb.append("     W1.SCHREGNO, ");
        stb.append("     ZIPCD, ");
        stb.append("     TELNO, ");
        stb.append("     EMERGENCYTELNO, ");
        stb.append("     ADDR1, ");
        stb.append("     ADDR2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT W1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON W1.SCHREGNO = L1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     (W1.SCHREGNO,W1.ISSUEDATE) ");
        stb.append(" IN ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ADDRESS_DAT W2 ");
        stb.append("     WHERE ");
        stb.append("         W2.ISSUEDATE <= '" + _param._sDay + "' AND ");
        stb.append("         (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '" + _param._eDay + "') AND ");
        stb.append("         W2.SCHREGNO = ? ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ))DB3 ON DB3.SCHREGNO = DB1.SCHREGNO ");
        stb.append(" WHERE  ");
        stb.append("     DB1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND DB1.SEMESTER = '" + _param._semester + "' AND  ");
        stb.append("     DB1.GRADE || DB1.HR_CLASS = '" + _param._gradeHr + "' ");
        stb.append("     AND DB1.SCHREGNO = ? ");
        stb.append("     ORDER BY DB1.ATTENDNO ");

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
        private final String _gradeHr;
        private final String _semester;
        private final String _sDay;
        private final String _eDay;
        private final boolean _header;
        private final String _hrName;
        private final String _staffName;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _selectdata = request.getParameterValues("category_name");
            _telFlg = request.getParameter("TEL") == null ? false : true;
            _gradeHr = request.getParameter("GRADE_HR_CLASS");
            _semester = request.getParameter("OUTPUT");
            _sDay = request.getParameter("SDAY");
            _eDay = request.getParameter("EDAY");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _staffName = getStaffName(db2);
            _hrName = getHrName(db2);
        }

        private String getStaffName(final DB2UDB db2) throws SQLException {
            String retStaff = "";
            final String sql = "SELECT STAFFNAME_SHOW FROM STAFF_MST WHERE STAFFCD = (SELECT TR_CD1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' AND GRADE || HR_CLASS = '" + _gradeHr + "') ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                retStaff = rs.getString("STAFFNAME_SHOW");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStaff;
        }

        private String getHrName(final DB2UDB db2) throws SQLException {
            String retHr = "";
            final String sql = "SELECT HR_NAME FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' AND GRADE || HR_CLASS = '" + _gradeHr + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                retHr = rs.getString("HR_NAME");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retHr;
        }

    }
}

// eof
