/*
 * $Id: 1b9446b6a7828f2ae927ae9e175dabfdda629a22 $
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

public class KNJMP964 {

    private static final Log log = LogFactory.getLog(KNJMP964.class);

    private final String SYUUNYUU = "01";
    private final String ZATUSYUUNYUU = "02";
    private final String SISYUTU = "03";
    private final String YOBI = "04";

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

            for (int i = 0; i < _param._selectData.length; i++) {
                final String gradeHr = _param._selectData[i];
                printMain(db2, svf, gradeHr);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String gradeHr) {
        final List printLmst = getList(db2, gradeHr);

        for (Iterator iterator = printLmst.iterator(); iterator.hasNext();) {
            if (_hasData) {
                svf.VrEndPage();
            }
            HrClass hrClass = (HrClass) iterator.next();
            svf.VrSetForm("KNJMP964.frm", 4);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year));
            svf.VrsOut("TITLE", gengou + "年度　クラス別支出伺明細表");
            svf.VrsOut("HR_NAME1", hrClass._hrName);
            svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
            svf.VrsOut("SUB_TITLE", "決定額：" + String.valueOf(hrClass._totalMoney));
            svf.VrsOut("DECISION_MONEY", "未決済額：" + String.valueOf(hrClass._totalRequestMoney));

            final List syuukeiList = new ArrayList();
            for (Iterator itSyuukei = hrClass._syuukei.keySet().iterator(); itSyuukei.hasNext();) {
                String kingaku = (String) itSyuukei.next();
                final Integer syuukeiCnt = (Integer) hrClass._syuukei.get(kingaku);
                final TotalClass totalClass = new TotalClass(kingaku, syuukeiCnt.toString());
                syuukeiList.add(totalClass);
            }
            int dataCnt = 0;
            for (Iterator itSch = hrClass._schList.iterator(); itSch.hasNext();) {
                SchregDat schregDat = (SchregDat) itSch.next();
                svf.VrsOut("NO", schregDat._attendno.substring(1));
                svf.VrsOut("NAME", schregDat._name);
                svf.VrsOut("BILL", String.valueOf(Integer.parseInt(schregDat._requestMoney) + Integer.parseInt(schregDat._outGoMoney)));
                if (syuukeiList.size() > dataCnt) {
                    final TotalClass totalClass = (TotalClass) syuukeiList.get(dataCnt);
                    svf.VrsOut("GRP", "1");
                    svf.VrsOut("TOTAL_BILL", totalClass._money);
                    svf.VrsOut("NUM", totalClass._cnt);
                    svf.VrEndRecord();
                } else if (syuukeiList.size() == dataCnt) {
                    svf.VrsOut("GRP", "2");
                    svf.VrsOut("COUNT", syuukeiList.size() + "件");
                    svf.VrsOut("NUM", hrClass._schList.size() + "人");
                    svf.VrEndRecord();
                } else {
                    svf.VrsOut("GRP", "2");
                    svf.VrEndRecord();
                }
                dataCnt++;
            }
            if (syuukeiList.size() == hrClass._schList.size()) {
                svf.VrsOut("GRP", "2");
                svf.VrsOut("COUNT", syuukeiList.size() + "件");
                svf.VrsOut("NUM", hrClass._schList.size() + "人");
                svf.VrEndRecord();
                dataCnt++;
            }
            for (int i = dataCnt; i < 50; i++) {
                svf.VrsOut("GRP", "2");
                svf.VrEndRecord();
            }
            _hasData = true;
            svf.VrsOut("BILL_TOTAL", String.valueOf(hrClass._totalMoney + hrClass._totalRequestMoney));
            svf.VrEndRecord();
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

    private List getList(final DB2UDB db2, final String gradeHr) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql(gradeHr);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befHrClass = "";
            HrClass setHrClass = null;
            while (rs.next()) {
                final String schregno       = rs.getString("SCHREGNO");
                final String name           = rs.getString("NAME");
                final String grade          = rs.getString("GRADE");
                final String hrClass        = rs.getString("HR_CLASS");
                final String hrName         = rs.getString("HR_NAME");
                final String attendno       = rs.getString("ATTENDNO");
                final String requestMoney   = rs.getString("REQUEST_MONEY");
                final String outgoMoney    = rs.getString("OUTGO_MONEY");

                if (!befHrClass.equals(hrClass)) {
                    setHrClass = new HrClass(grade, hrClass, hrName);
                    retList.add(setHrClass);
                }

                final SchregDat schregDat = new SchregDat(schregno, name, grade, hrClass, hrName, attendno, requestMoney, outgoMoney);
                setHrClass._schList.add(schregDat);
                setHrClass._totalCnt++;
                setHrClass._totalRequestMoney += Integer.parseInt(requestMoney);
                setHrClass._totalMoney += Integer.parseInt(outgoMoney);
                final int setSyuukeiKey = Integer.parseInt(requestMoney) + Integer.parseInt(outgoMoney);
                final String keyOutGo = String.valueOf(setSyuukeiKey);
                if (setHrClass._syuukei.containsKey(keyOutGo)) {
                    int syuukei = ((Integer) setHrClass._syuukei.get(keyOutGo)).intValue();
                    syuukei++;
                    setHrClass._syuukei.put(keyOutGo, new Integer(syuukei));
                } else {
                    setHrClass._syuukei.put(keyOutGo, new Integer(1));
                }
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

    private String getSchregSql(final String gradeHr) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
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
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHr + "' ");
        stb.append(" ), EXISTL AS ( ");
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
        stb.append("           AND VALUE(T1.OUTGO_CANCEL, '0') = '0' ");
        if ("1".equals(_param._outGoDiv)) {
            stb.append("           AND MONTH(T1.REQUEST_DATE) = " + _param._requestMonth + " ");
        } else {
            stb.append("           AND MONTH(T1.OUTGO_DATE) = " + _param._outGoMonth + " ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     LMST.YEAR, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO, ");
        stb.append("     SUM(CASE WHEN VALUE(I1.OUTGO_APPROVAL, '0') = '1' THEN 0 ELSE VALUE(T1.OUTGO_MONEY, 0) END) AS REQUEST_MONEY, ");
        stb.append("     SUM(CASE WHEN VALUE(I1.OUTGO_APPROVAL, '0') = '1' THEN VALUE(T1.OUTGO_MONEY, 0) ELSE 0 END) AS OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     EXISTL LMST ");
        stb.append("     LEFT JOIN SCH_T ON SCH_T.YEAR = LMST.YEAR ");
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT T1 ON LMST.YEAR = T1.YEAR ");
        stb.append("           AND LMST.OUTGO_L_CD = T1.OUTGO_L_CD ");
        stb.append("           AND SCH_T.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT I1 ON T1.YEAR = I1.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = I1.OUTGO_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = I1.OUTGO_M_CD ");
        stb.append("          AND T1.REQUEST_NO = I1.REQUEST_NO ");
        stb.append(" WHERE ");
        stb.append("     VALUE(I1.OUTGO_CANCEL, '0') = '0' ");
        if ("1".equals(_param._outGoDiv)) {
            stb.append("           AND MONTH(I1.REQUEST_DATE) = " + _param._requestMonth + " ");
        } else {
            stb.append("           AND MONTH(I1.OUTGO_DATE) = " + _param._outGoMonth + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     LMST.YEAR, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.ATTENDNO ");
        stb.append(" ORDER BY ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.ATTENDNO ");

        return stb.toString();
    }

    private class TotalClass {
        private final String _money;
        private final String _cnt;
        public TotalClass(
                final String money,
                final String cnt
        ) {
            _money  = money;
            _cnt    = cnt;
        }
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private int _totalCnt;
        private int _totalRequestMoney;
        private int _totalMoney;
        private final List _schList;
        private final Map _syuukei;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName
        ) {
            _grade   = grade;
            _hrClass = hrClass;
            _hrName  = hrName;
            _schList = new ArrayList();
            _syuukei = new TreeMap();
        }
    }

    private class SchregDat {
        private final String _schregno;
        private final String _name;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendno;
        private final String _requestMoney;
        private final String _outGoMoney;

        public SchregDat(
                final String schregno,
                final String name,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String requestMoney,
                final String incomeMoney
        ) {
            _schregno       = schregno;
            _name           = name;
            _grade          = grade;
            _hrClass        = hrClass;
            _hrName         = hrName;
            _attendno       = attendno;
            _requestMoney   = requestMoney;
            _outGoMoney    = incomeMoney;
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
        private final String _semester;
        private final String _grade;
        private final String _outGoDiv;
        private final String _requestMonth;
        private final String _outGoMonth;
        final String[] _selectData;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _outGoDiv = request.getParameter("OUTGO_DIV");
            _requestMonth = request.getParameter("REQUEST_MONTH");
            _outGoMonth = request.getParameter("OUTGO_MONTH");
            _selectData = request.getParameterValues("CLASS_SELECTED");
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

