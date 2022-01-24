/*
 * $Id: a65f2370fe44dcc13e8d253b709389cf3f150e53 $
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


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

public class KNJMP963 {

    private static final Log log = LogFactory.getLog(KNJMP963.class);

    private final String SYUUNYUU = "01";
    private final String ZATUSYUUNYUU = "02";
    private final String SISYUTU = "03";
    private final String YOBI = "04";

    private final String INCOME = "1";
    private final String OUTGO = "2";

    private final int MAXLETU = 8;

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
        final List printLmst = getList(db2);

        for (Iterator iterator = printLmst.iterator(); iterator.hasNext();) {
            if (_hasData) {
                svf.VrEndPage();
            }
            Lmst lmst = (Lmst) iterator.next();
            svf.VrSetForm("KNJMP963.frm", 1);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year));
            svf.VrsOut("TITLE", gengou + "年度　" + lmst._lName + "　学年別支出伺明細表");
            if ("1".equals(_param._printDiv)) {
                svf.VrsOut("SUBTITLE2", "伝票番号：" + _param._requestNo);
            } else {
                if ("1".equals(_param._outGoDiv)) {
                    svf.VrsOut("SUBTITLE2", "支出伺日：" + _param._requestMonthF + "月\uFF5E" + _param._requestMonthT + "月");
                } else {
                    svf.VrsOut("SUBTITLE2", "支出決定日：" + _param._outGoMonthF + "月\uFF5E" + _param._outGoMonthT + "月");
                }
            }
            svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));

            int retuCnt = 1;
            int billCnt = 0;
            for (Iterator itHr = lmst._hrClass.iterator(); itHr.hasNext();) {
                if (MAXLETU < retuCnt) {
                    svf.VrEndPage();
                }
                HrClass hrClass = (HrClass) itHr.next();
                svf.VrsOut("HR_NAME" + retuCnt, hrClass._hrName);

                int lineCnt = 1;
                int hrMoney = 0;
                for (Iterator itSch = hrClass._schList.iterator(); itSch.hasNext();) {
                    SchregDat schregDat = (SchregDat) itSch.next();
                    svf.VrsOutn("NO" + retuCnt, lineCnt, schregDat._attendno);
                    final String nameSoeji = getMS932Length(schregDat._name) > 12 ? "2" : "1";
                    svf.VrsOutn("NAME" + retuCnt + "_" + nameSoeji, lineCnt, schregDat._name);
                    svf.VrsOutn("BILL" + retuCnt, lineCnt, schregDat._outGoMoney);
                    lineCnt++;
                    billCnt += Integer.parseInt(schregDat._outGoMoney) > 0 ? 1 : 0;
                    hrMoney += Integer.parseInt(schregDat._outGoMoney);
                }
                svf.VrsOutn("BILL" + retuCnt, 51, String.valueOf(hrMoney));
                retuCnt++;
                _hasData = true;
            }
            svf.VrsOut("TOTAL_NUM", billCnt + "名/" + lmst._totalCnt + "名");
            svf.VrsOut("TOTAL_MONEY", String.valueOf(lmst._totalMoney));
        }
        if (_hasData) {
            svf.VrEndPage();
        }

    }

    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befLcd = "";
            String befHrClass = "";
            Lmst lmst = null;
            HrClass setHrClass = null;
            while (rs.next()) {
                final String outGoLCd      = rs.getString("OUTGO_L_CD");
                final String levyLName      = rs.getString("LEVY_L_NAME");
                final String schregno       = rs.getString("SCHREGNO");
                final String name           = rs.getString("NAME");
                final String grade          = rs.getString("GRADE");
                final String hrClass        = rs.getString("HR_CLASS");
                final String hrName         = rs.getString("HR_NAME");
                final String attendno       = rs.getString("ATTENDNO");
                final String outGoMoney    = rs.getString("OUTGO_MONEY");

                if (!befLcd.equals(outGoLCd)) {
                    lmst = new Lmst(outGoLCd, levyLName);
                    retList.add(lmst);
                }
                if (!befHrClass.equals(hrClass)) {
                    setHrClass = new HrClass(grade, hrClass, hrName);
                    lmst._hrClass.add(setHrClass);
                }

                final SchregDat schregDat = new SchregDat(outGoLCd, levyLName, schregno, name, grade, hrClass, hrName, attendno, outGoMoney);
                setHrClass._schList.add(schregDat);
                lmst._totalCnt++;
                lmst._totalMoney += Integer.parseInt(outGoMoney);

                befLcd = outGoLCd;
                befHrClass = hrClass;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     MAX(T1.SEMESTER) AS SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L2.NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     L1.HR_NAME, ");
        stb.append("     T1.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.GRADE = L1.GRADE ");
        stb.append("          AND T1.HR_CLASS = L1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO, ");
        stb.append("     SCH_MAX T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(" ), EXISTL AS ( ");
        if ("1".equals(_param._printDiv)) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.OUTGO_L_CD, ");
            stb.append("     L1.LEVY_L_NAME ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        } else {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.OUTGO_L_CD, ");
            stb.append("     L1.LEVY_L_NAME ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND VALUE(T1.OUTGO_CANCEL, '0') = '0' ");
            if ("1".equals(_param._outGoDiv)) {
                stb.append("           AND MONTH(T1.REQUEST_DATE) BETWEEN " + _param._requestMonthF + " AND " + _param._requestMonthT + " ");
            } else {
                stb.append("           AND MONTH(T1.OUTGO_DATE) BETWEEN " + _param._outGoMonthF + " AND " + _param._outGoMonthT + " ");
            }
        }
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     LMST.YEAR, ");
        stb.append("     LMST.OUTGO_L_CD, ");
        stb.append("     LMST.LEVY_L_NAME, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO, ");
        stb.append("     SUM(VALUE(T1.OUTGO_MONEY, 0)) AS OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     EXISTL LMST ");
        stb.append("     LEFT JOIN SCH_T ON SCH_T.YEAR = LMST.YEAR ");
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ON LMST.YEAR = T1.YEAR ");
        stb.append("          AND LMST.OUTGO_L_CD = T1.OUTGO_L_CD ");
        stb.append("          AND SCH_T.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._printDiv)) {
            stb.append("           AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("           AND T1.OUTGO_L_CD = L1.OUTGO_L_CD ");
        stb.append("           AND T1.OUTGO_M_CD = L1.OUTGO_M_CD ");
        stb.append("           AND T1.REQUEST_NO = L1.REQUEST_NO ");
        stb.append(" WHERE ");
        stb.append("     VALUE(L1.OUTGO_CANCEL, '0') = '0' ");
        if ("2".equals(_param._printDiv)) {
            if ("1".equals(_param._outGoDiv)) {
                stb.append("           AND MONTH(L1.REQUEST_DATE) BETWEEN " + _param._requestMonthF + " AND " + _param._requestMonthT + " ");
            } else {
                stb.append("           AND MONTH(L1.OUTGO_DATE) BETWEEN " + _param._outGoMonthF + " AND " + _param._outGoMonthT + " ");
            }
        }
        stb.append(" GROUP BY ");
        stb.append("     LMST.YEAR, ");
        stb.append("     LMST.OUTGO_L_CD, ");
        stb.append("     LMST.LEVY_L_NAME, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     LMST.OUTGO_L_CD, ");
        stb.append("     LMST.LEVY_L_NAME, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO, ");
        stb.append("     VALUE(MAIN_T.OUTGO_MONEY, 0) AS OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     EXISTL LMST ");
        stb.append("     LEFT JOIN SCH_T ON SCH_T.YEAR = LMST.YEAR ");
        stb.append("     LEFT JOIN MAIN_T ON SCH_T.SCHREGNO = MAIN_T.SCHREGNO ");
        stb.append("          AND LMST.YEAR = MAIN_T.YEAR ");
        stb.append("          AND LMST.OUTGO_L_CD = MAIN_T.OUTGO_L_CD ");
        stb.append(" ORDER BY ");
        stb.append("     LMST.OUTGO_L_CD, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.ATTENDNO ");

        return stb.toString();
    }

    private class Lmst {
        private final String _lCd;
        private final String _lName;
        private int _totalCnt;
        private int _totalMoney;
        private final List _hrClass;
        public Lmst(
                final String lCd,
                final String lName
        ) {
            _lCd = lCd;
            _lName = lName;
            _hrClass = new ArrayList();
            _totalCnt = 0;
            _totalMoney = 0;
        }
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final List _schList;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName
        ) {
            _grade   = grade;
            _hrClass = hrClass;
            _hrName  = hrName;
            _schList = new ArrayList();
        }
    }

    private class SchregDat {
        private final String _outGoLCd;
        private final String _levyLName;
        private final String _schregno;
        private final String _name;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendno;
        private final String _outGoMoney;

        public SchregDat(
                final String outGoLCd,
                final String levyLName,
                final String schregno,
                final String name,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String outGoMoney
        ) {
            _outGoLCd      = outGoLCd;
            _levyLName      = levyLName;
            _schregno       = schregno;
            _name           = name;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _attendno       = attendno;
            _outGoMoney    = outGoMoney;
        }

    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _grade;
        private final String _printDiv;
        private final String _requestNo;
        private final String _outGoDiv;
        private final String _requestMonthF;
        private final String _requestMonthT;
        private final String _outGoMonthF;
        private final String _outGoMonthT;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
            _printDiv = request.getParameter("PRINT_DIV");
            _requestNo = request.getParameter("REQUEST_NO");
            _outGoDiv = request.getParameter("OUTGO_DIV");
            _requestMonthF = request.getParameter("REQUEST_MONTH_F");
            _requestMonthT = request.getParameter("REQUEST_MONTH_T");
            _outGoMonthF = request.getParameter("OUTGO_MONTH_F");
            _outGoMonthT = request.getParameter("OUTGO_MONTH_T");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _year);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

    }
}

// eof

