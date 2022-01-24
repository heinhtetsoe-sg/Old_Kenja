/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 0882f4be3d8ebc100ff88f6b96ba8643e0b57e78 $
 *
 * 作成日: 2018/10/15
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJS340A {

    private static final Log log = LogFactory.getLog(KNJS340A.class);

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
        final List printList = getHrList(db2);
        //日付
        for (Iterator itAttendDate = _param._dateMap.keySet().iterator(); itAttendDate.hasNext();) {
            svf.VrSetForm("KNJS340A.frm", 4);
            final String attendDate = (String) itAttendDate.next();
            final String printDay = (String) _param._dateMap.get(attendDate);
            svf.VrsOut("TITLE", "出欠状況(" + printDay + ")");
            int diCnt = 1;
            svf.VrsOut("ATTEND_NAME" + diCnt, "在籍");
            diCnt++;
            //C001
            for (Iterator itDi = _param._diList.iterator(); itDi.hasNext();) {
                final DiC001 c001 = (DiC001) itDi.next();
                svf.VrsOut("ATTEND_NAME" + diCnt, c001._name1);
                diCnt++;
            }

            //クラス
            for (Iterator itHrClass = printList.iterator(); itHrClass.hasNext();) {
                final HrClass hrClass = (HrClass) itHrClass.next();
                svf.VrsOut("GRADE", hrClass._gradeName);
                svf.VrsOut("HR_NAME", hrClass._name);

                diCnt = 1;
                svf.VrsOut("ATTEND_" + diCnt + "_1", hrClass._maleCnt);
                svf.VrsOut("ATTEND_" + diCnt + "_2", hrClass._feMaleCnt);
                svf.VrsOut("ATTEND_" + diCnt + "_3", hrClass._allCnt);
                diCnt++;
                //C001
                for (Iterator itDi = _param._diList.iterator(); itDi.hasNext();) {
                    final DiC001 c001 = (DiC001) itDi.next();
                    if (hrClass._attendDayMap.containsKey(attendDate)) {
                        final Map attendDIMap = (Map) hrClass._attendDayMap.get(attendDate);
                        if (attendDIMap.containsKey(c001._namecd2)) {
                            final DiData diData = (DiData) attendDIMap.get(c001._namecd2);
                            svf.VrsOut("ATTEND_" + diCnt + "_1", diData._maleCnt);
                            svf.VrsOut("ATTEND_" + diCnt + "_2", diData._feMaleCnt);
                            svf.VrsOut("ATTEND_" + diCnt + "_3", diData._allCnt);
                        }
                    }
                    diCnt++;
                }
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private List getHrList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String hrSql = getHrSql();
            ps = db2.prepareStatement(hrSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String hrClass = rs.getString("HR_CLASS");
                final String name = rs.getString("HR_NAME");
                final String abbv = rs.getString("HR_NAMEABBV");
                final String maleCnt = rs.getString("MALE_CNT");
                final String feMaleCnt = rs.getString("FEMALE_CNT");
                final String allCnt = rs.getString("ALL_CNT");

                final HrClass hrClassObj = new HrClass(grade, gradeName, hrClass, name, abbv, maleCnt, feMaleCnt, allCnt);
                hrClassObj.setAttendInfo(db2);
                retList.add(hrClassObj);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getHrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGH.GRADE, ");
        stb.append("     REGG.GRADE_NAME1, ");
        stb.append("     REGH.HR_CLASS, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV, ");
        stb.append("     SUM(CASE WHEN BASE.SEX = '1' THEN 1 ELSE 0 END) AS MALE_CNT, ");
        stb.append("     SUM(CASE WHEN BASE.SEX = '2' THEN 1 ELSE 0 END) AS FEMALE_CNT, ");
        stb.append("     COUNT(*) AS ALL_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT REGH ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT REGG ON REGH.YEAR = REGG.YEAR ");
        stb.append("           AND REGH.GRADE = REGG.GRADE ");
        stb.append("           AND REGG.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGH.YEAR = REGD.YEAR ");
        stb.append("           AND REGH.SEMESTER = REGD.SEMESTER ");
        stb.append("           AND REGH.GRADE = REGD.GRADE ");
        stb.append("           AND REGH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGH.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" GROUP BY ");
        stb.append("     REGH.GRADE, ");
        stb.append("     REGG.GRADE_NAME1, ");
        stb.append("     REGH.HR_CLASS, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV ");
        stb.append(" ORDER BY ");
        stb.append("     REGH.GRADE, ");
        stb.append("     REGH.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String _name;
        final String _abbv;
        final String _maleCnt;
        final String _feMaleCnt;
        final String _allCnt;
        final Map _attendDayMap;
        public HrClass(
                final String grade,
                final String gradeName,
                final String hrClass,
                final String name,
                final String abbv,
                final String maleCnt,
                final String feMaleCnt,
                final String allCnt
        ) {
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _name = name;
            _abbv = abbv;
            _maleCnt = maleCnt;
            _feMaleCnt = feMaleCnt;
            _allCnt = allCnt;
            _attendDayMap = new HashMap();
        }
        public void setAttendInfo(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AT_DAY.ATTENDDATE, ");
            stb.append("     AT_DAY.DI_CD, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '1' THEN 1 ELSE 0 END) AS MALE_CNT, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '2' THEN 1 ELSE 0 END) AS FEMALE_CNT, ");
            stb.append("     COUNT(*) AS ALL_CNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN ATTEND_DAY_DAT AT_DAY ON REGD.SCHREGNO = AT_DAY.SCHREGNO ");
            stb.append("           AND AT_DAY.ATTENDDATE BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     AND REGD.HR_CLASS = '" + _hrClass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     AT_DAY.ATTENDDATE, ");
            stb.append("     AT_DAY.DI_CD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String attendDate = rs.getString("ATTENDDATE");
                    final String diCd = rs.getString("DI_CD");
                    final String maleCnt = rs.getString("MALE_CNT");
                    final String feMaleCnt = rs.getString("FEMALE_CNT");
                    final String allCnt = rs.getString("ALL_CNT");
                    final DiData diData = new DiData(diCd, maleCnt, feMaleCnt, allCnt);
                    if (!_attendDayMap.containsKey(attendDate)) {
                        _attendDayMap.put(attendDate, new HashMap());
                    }
                    final Map diMap = (Map) _attendDayMap.get(attendDate);
                    diMap.put(diCd, diData);
                    if (!_param._dateMap.containsKey(attendDate)) {
                        _param._dateMap.put(attendDate, KNJ_EditDate.h_format_SeirekiJP(attendDate));
                    }
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private class DiData {
        final String _diCd;
        final String _maleCnt;
        final String _feMaleCnt;
        final String _allCnt;
        public DiData(
                final String diCd,
                final String maleCnt,
                final String feMaleCnt,
                final String allCnt
        ) {
            _diCd = diCd;
            _maleCnt = maleCnt;
            _feMaleCnt = feMaleCnt;
            _allCnt = allCnt;
        }
    }

    private class DiC001 {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _name3;
        final String _abbv1;
        final String _abbv2;
        final String _abbv3;
        final String _spare1;
        final String _spare2;
        final String _spare3;
        public DiC001(
                final String namecd2,
                final String name1,
                final String name2,
                final String name3,
                final String abbv1,
                final String abbv2,
                final String abbv3,
                final String spare1,
                final String spare2,
                final String spare3
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _name3 = name3;
            _abbv1 = abbv1;
            _abbv2 = abbv2;
            _abbv3 = abbv3;
            _spare1 = spare1;
            _spare2 = spare2;
            _spare3 = spare3;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62827 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _dateFrom;
        final String _dateTo;
        final String _prgid;
        final String _printLogStaffcd;
        final String _schoolcd;
        final String _schoolKind;
        final String _semeMstEdate;
        final String _semeMstSdate;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useschoolKindfield;
        final Map _dateMap;
        final List _diList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _dateFrom = request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _semeMstEdate = request.getParameter("SEME_MST_EDATE");
            _semeMstSdate = request.getParameter("SEME_MST_SDATE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _dateMap = new TreeMap();
            _diList = getDiList(db2);
        }

        private List getDiList(final DB2UDB db2) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String c001Sql = getC001Sql(_schoolKind);
                ps = db2.prepareStatement(c001Sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");
                    final String name3 = rs.getString("NAME3");
                    final String abbv1 = rs.getString("ABBV1");
                    final String abbv2 = rs.getString("ABBV2");
                    final String abbv3 = rs.getString("ABBV3");
                    final String spare1 = rs.getString("NAMESPARE1");
                    final String spare2 = rs.getString("NAMESPARE2");
                    final String spare3 = rs.getString("NAMESPARE3");

                    final DiC001 diC001 = new DiC001(namecd2, name1, name2, name3, abbv1, abbv2, abbv3, spare1, spare2, spare3);
                    retList.add(diC001);
                }

                if (retList.size() > 0) {
                    final String c001Sql2 = getC001Sql("0");
                    ps = db2.prepareStatement(c001Sql2);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String namecd2 = rs.getString("NAMECD2");
                        final String name1 = rs.getString("NAME1");
                        final String name2 = rs.getString("NAME2");
                        final String name3 = rs.getString("NAME3");
                        final String abbv1 = rs.getString("ABBV1");
                        final String abbv2 = rs.getString("ABBV2");
                        final String abbv3 = rs.getString("ABBV3");
                        final String spare1 = rs.getString("NAMESPARE1");
                        final String spare2 = rs.getString("NAMESPARE2");
                        final String spare3 = rs.getString("NAMESPARE3");

                        final DiC001 diC001 = new DiC001(namecd2, name1, name2, name3, abbv1, abbv2, abbv3, spare1, spare2, spare3);
                        retList.add(diC001);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String getC001Sql(final String schooLkind) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1, ");
            stb.append("     NAME2, ");
            stb.append("     NAME3, ");
            stb.append("     ABBV1, ");
            stb.append("     ABBV2, ");
            stb.append("     ABBV3, ");
            stb.append("     NAMESPARE1, ");
            stb.append("     NAMESPARE2, ");
            stb.append("     NAMESPARE3 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'C" + schooLkind + "01' ");
            stb.append("     AND NAMECD2 IN ('6', '5', '2', '3', '15', '16') ");
            stb.append(" ORDER BY ");
            stb.append("     CASE NAMECD2 WHEN '6' THEN 1 ");
            stb.append("          WHEN '5' THEN 2 ");
            stb.append("          WHEN '2' THEN 3 ");
            stb.append("          WHEN '3' THEN 4 ");
            stb.append("          WHEN '15' THEN 5 ");
            stb.append("          WHEN '16' THEN 6 ");
            stb.append("     END ");

            return stb.toString();
        }
    }
}

// eof
