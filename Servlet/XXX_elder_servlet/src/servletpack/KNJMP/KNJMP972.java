/*
 * $Id: c45ae478d09003a0f53f26aedb21386c1c828aee $
 *
 * 作成日: 2015/04/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class KNJMP972 {

    private static final Log log = LogFactory.getLog(KNJMP972.class);

    private final int MAX_ROW = 33;
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws ParseException, UnsupportedEncodingException {
        for (Iterator iterator = _param._lList.iterator(); iterator.hasNext();) {
            Lmst lmst = (Lmst) iterator.next();
            final List hrClassList = getList(db2, lmst._lCd);

            for (Iterator itPrint = hrClassList.iterator(); itPrint.hasNext();) {
                svf.VrSetForm("KNJMP972.frm", 4);
                HrClass hrClass = (HrClass) itPrint.next();

                //収入
                int lineCnt = 1;
                int totalIncome = 0;
                for (Iterator itSch = hrClass._schList.iterator(); itSch.hasNext();) {
                    final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear));
                    svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                    svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                    svf.VrsOut("HR_NAME", hrClass._hrName);
                    svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                    svf.VrsOut("DATE", _param.getNow());

                    final PrintSchregData schregData = (PrintSchregData) itSch.next();
                    svf.VrsOutn("NO", lineCnt, schregData._attendno);
                    svf.VrsOutn("NAME1", lineCnt, schregData._schName);

                    svf.VrsOut("ITEM1", "収入");
                    svf.VrsOutn("MONEY1", lineCnt, String.valueOf(schregData._incomeMoney));
                    totalIncome += schregData._incomeMoney;
                    lineCnt++;
                }
                svf.VrsOut("ITEM1", "収入");
                svf.VrsOutn("MONEY1", 46, String.valueOf(totalIncome));
                svf.VrEndRecord();

                int rowCnt = 1;
                for (Iterator itHr = hrClass._sMstMap.keySet().iterator(); itHr.hasNext();) {
                    final String lmsCd = (String) itHr.next();
                    final String sName = (String) hrClass._sMstMap.get(lmsCd);

                    final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear));
                    svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                    svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                    svf.VrsOut("HR_NAME", hrClass._hrName);
                    svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                    svf.VrsOut("DATE", _param.getNow());

                    lineCnt = 1;
                    int totalOutGo = 0;
                    for (Iterator itSch = hrClass._schList.iterator(); itSch.hasNext();) {
                        final PrintSchregData schregData = (PrintSchregData) itSch.next();
                        if (12 == rowCnt) {
                            svf.VrsOut("ITEM_TITLE", "支");
                        } else if (14 == rowCnt) {
                            svf.VrsOut("ITEM_TITLE", "出");
                        } else if (16 == rowCnt) {
                            svf.VrsOut("ITEM_TITLE", "明");
                        } else if (18 == rowCnt) {
                            svf.VrsOut("ITEM_TITLE", "細");
                        }
                        int check_len  = sName.getBytes("MS932").length;
                        if (check_len > 40) {
                            svf.VrsOut("ITEM2_3_1", sName);
                        } else if (check_len > 20) {
                            svf.VrsOut("ITEM2_2_1", sName);
                        } else if (check_len > 16) {
                            svf.VrsOut("ITEM2_1_1", sName);
                        } else {
                            svf.VrsOut("ITEM2", sName);
                        }

                        final OutGoMoney outGoMoney = (OutGoMoney) schregData._outGoMap.get(lmsCd);
                        if (null != outGoMoney) {
                            if (outGoMoney._outGoMoney <= 9999) {
                                svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                            } else {
                                svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                            }
                            totalOutGo += outGoMoney._outGoMoney;
                        }
                        lineCnt++;
                    }
                    if (totalOutGo <= 9999) {
                        svf.VrsOutn("MONEY2_1", 46, String.valueOf(totalOutGo));
                    } else {
                        svf.VrsOutn("MONEY2_2", 46, String.valueOf(totalOutGo));
                    }
                    svf.VrEndRecord();

                    rowCnt++;
                }
                for (int i = rowCnt; i <= MAX_ROW; i++) {
                    if (12 == i) {
                        svf.VrsOut("ITEM_TITLE", "支");
                    } else if (14 == i) {
                        svf.VrsOut("ITEM_TITLE", "出");
                    } else if (16 == i) {
                        svf.VrsOut("ITEM_TITLE", "明");
                    } else if (18 == i) {
                        svf.VrsOut("ITEM_TITLE", "細");
                    }
                    svf.VrsOut("BLANK", "あ");
                    svf.VrEndRecord();
                }
                //支出計
                lineCnt = 1;
                int totalOutGo = 0;
                for (Iterator itSch = hrClass._schList.iterator(); itSch.hasNext();) {
                    final PrintSchregData schregData = (PrintSchregData) itSch.next();
                    svf.VrsOut("ITEM1", "支出計");
                    svf.VrsOutn("MONEY1", lineCnt, String.valueOf(schregData._totalOutGoMoney));
                    totalOutGo += schregData._totalOutGoMoney;
                    lineCnt++;
                }
                svf.VrsOut("ITEM1", "支出計");
                svf.VrsOutn("MONEY1", 46, String.valueOf(totalOutGo));
                svf.VrEndRecord();

                //差額
                lineCnt = 1;
                int totalSagaku = 0;
                for (Iterator itSch = hrClass._schList.iterator(); itSch.hasNext();) {
                    final PrintSchregData schregData = (PrintSchregData) itSch.next();
                    svf.VrsOut("ITEM1", "差額");
                    final int sagaku = schregData._incomeMoney - schregData._totalOutGoMoney;
                    svf.VrsOutn("MONEY1", lineCnt, String.valueOf(sagaku));
                    totalSagaku += sagaku;
                    lineCnt++;
                }
                svf.VrsOut("ITEM1", "差額");
                svf.VrsOutn("MONEY1", 46, String.valueOf(totalSagaku));
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private List getList(final DB2UDB db2, final String lCd) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql(lCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befGradeHrClass = "";
            HrClass setHrClass = null;
            while (rs.next()) {
                final String schregno   = rs.getString("SCHREGNO");
                final String grade      = rs.getString("GRADE");
                final String hrClass    = rs.getString("HR_CLASS");
                final String hrName     = rs.getString("HR_NAME");
                final String attendno   = rs.getString("ATTENDNO");
                final String schName    = rs.getString("SCH_NAME");
                final String staffName  = rs.getString("STAFFNAME");
                final int incomeMoney   = rs.getInt("INCOME_MONEY");
                final PrintSchregData printSchregData = new PrintSchregData(schregno, grade, hrClass, hrName, attendno, schName, staffName, incomeMoney);
                printSchregData.setOutGoList(db2, lCd);
                if (!befGradeHrClass.equals(grade + hrClass)) {
                    setHrClass = new HrClass(grade, hrClass, hrName, staffName);
                    retList.add(setHrClass);
                }
                for (Iterator pri = printSchregData._outGoMap.keySet().iterator(); pri.hasNext();) {
                    final String lmsCd = (String) pri.next();
                    OutGoMoney outGoMoney = (OutGoMoney) printSchregData._outGoMap.get(lmsCd);
                    setHrClass._sMstMap.put(outGoMoney._outgoLCd + outGoMoney._outgoMCd + outGoMoney._outgoSCd, outGoMoney._sName);
                }
                setHrClass._schList.add(printSchregData);
                befGradeHrClass = grade + hrClass;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchregSql(final String lCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME AS SCH_NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     SUM(VALUE(INCOME_SCH.INCOME_MONEY, 0)) AS INCOME_MONEY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN LEVY_REQUEST_INCOME_DAT INCOME ON REGD.YEAR = INCOME.YEAR ");
        stb.append("          AND INCOME.INCOME_L_CD = '" + lCd + "' ");
        stb.append("          AND VALUE(INCOME.INCOME_APPROVAL, '0') = '1' ");
        stb.append("          AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ");
        stb.append("     LEFT JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ");
        stb.append("          AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
        stb.append("          AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
        stb.append("          AND INCOME.REQUEST_NO = INCOME_SCH.REQUEST_NO ");
        stb.append("          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + _param._gradeHrInState);
        stb.append(" GROUP BY ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _staffName;
        private final List _schList;
        private final Map _sMstMap;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffName
        ) {
            _grade      = grade;
            _hrClass    = hrClass;
            _hrName     = hrName;
            _staffName  = staffName;
            _schList    = new ArrayList();
            _sMstMap    = new TreeMap();
        }
    }

    private class PrintSchregData {
        private final String _schregNo;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendno;
        private final String _schName;
        private final String _staffName;
        private final int _incomeMoney;
        int _totalOutGoMoney;
        private final Map _outGoMap;
        public PrintSchregData(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schName,
                final String staffName,
                final int incomeMoney
        ) {
            _schregNo    = schregno;
            _grade       = grade;
            _hrClass     = hrClass;
            _hrName      = hrName;
            _attendno    = attendno;
            _schName     = schName;
            _staffName   = staffName;
            _incomeMoney = incomeMoney;
            _totalOutGoMoney  = 0;
            _outGoMap = new TreeMap();
        }

        public void setOutGoList(final DB2UDB db2, final String lCd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getOutGoSql(lCd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String outgoLCd   = rs.getString("OUTGO_L_CD");
                    final String outgoMCd   = rs.getString("OUTGO_M_CD");
                    final String outgoSCd   = rs.getString("OUTGO_S_CD");
                    final String sName      = rs.getString("LEVY_S_NAME");
                    final int outGoMoney    = rs.getInt("OUTGO_MONEY");
                    final OutGoMoney goMoney = new OutGoMoney(outgoLCd, outgoMCd, outgoSCd, sName, outGoMoney);
                    _outGoMap.put(outgoLCd + outgoMCd + outgoSCd, goMoney);
                    _totalOutGoMoney += outGoMoney;
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getOutGoSql(final String lCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     OUTGO_SCH.OUTGO_L_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_M_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_S_CD, ");
            stb.append("     SMST.LEVY_S_NAME, ");
            stb.append("     VALUE(OUTGO_SCH.OUTGO_MONEY, 0) AS OUTGO_MONEY ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_DAT T1 ");
            stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON T1.YEAR = OUTGO_SCH.YEAR ");
            stb.append("           AND T1.OUTGO_L_CD = OUTGO_SCH.OUTGO_L_CD ");
            stb.append("           AND T1.OUTGO_M_CD = OUTGO_SCH.OUTGO_M_CD ");
            stb.append("           AND T1.REQUEST_NO = OUTGO_SCH.REQUEST_NO ");
            stb.append("           AND OUTGO_SCH.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     LEFT JOIN LEVY_S_MST SMST ON OUTGO_SCH.YEAR = SMST.YEAR ");
            stb.append("          AND OUTGO_SCH.OUTGO_L_CD = SMST.LEVY_L_CD ");
            stb.append("          AND OUTGO_SCH.OUTGO_M_CD = SMST.LEVY_M_CD ");
            stb.append("          AND OUTGO_SCH.OUTGO_S_CD = SMST.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND VALUE(T1.OUTGO_APPROVAL, '0') = '1' ");
            stb.append("     AND VALUE(T1.OUTGO_CANCEL, '0') = '0' ");
            stb.append("     AND T1.INCOME_L_CD = '" + lCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     OUTGO_SCH.OUTGO_L_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_M_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_S_CD ");

            return stb.toString();
        }
    }

    private class OutGoMoney {
        private final String _outgoLCd;
        private final String _outgoMCd;
        private final String _outgoSCd;
        private final String _sName;
        private final int _outGoMoney;
        public OutGoMoney(
                final String outgoLCd,
                final String outgoMCd,
                final String outgoSCd,
                final String sName,
                final int outGoMoney
        ) {
            _outgoLCd   = outgoLCd;
            _outgoMCd   = outgoMCd;
            _outgoSCd   = outgoSCd;
            _sName      = sName;
            _outGoMoney = outGoMoney;
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
        private final String _grade;
        private final String _incomeLCd;
        private final String[] _gradeHrSelected;
        private final String _gradeHrInState;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        private final List _lList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _incomeLCd = request.getParameter("INCOME_L_CD");
            //リストToリスト
            _gradeHrSelected = request.getParameterValues("CATEGORY_SELECTED");
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _gradeHrSelected.length; ia++) {
                if (_gradeHrSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_gradeHrSelected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _gradeHrInState = sbx.toString();

            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _ctrlYear);
            _lList = getLList(db2, _ctrlYear, _incomeLCd, _semester, _grade);
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

        private List getLList(final DB2UDB db2, final String year, final String incomeLCd, final String semester, final String grade) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.INCOME_L_CD, ");
                stb.append("     L1.LEVY_L_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_REQUEST_INCOME_DAT T1 ");
                stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND T1.INCOME_L_CD = L1.LEVY_L_CD ");
                if ("99".equals(incomeLCd)) {
                    stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ");
                    stb.append("          AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
                    stb.append("          AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
                    stb.append("          AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ");
                    stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ");
                    stb.append("          AND REGD.SEMESTER = '" + semester + "' ");
                    stb.append("          AND REGD.GRADE = '" + grade + "' ");
                    stb.append("          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ");
                }
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ");
                stb.append("     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ");
                if (!"99".equals(incomeLCd)) {
                    stb.append("     AND T1.INCOME_L_CD = '" + incomeLCd + "' ");
                }
                stb.append(" ORDER BY ");
                stb.append("     T1.INCOME_L_CD ");
                final String sql = stb.toString();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd = rs.getString("INCOME_L_CD");
                    final String lName = rs.getString("LEVY_L_NAME");
                    final Lmst lmst = new Lmst(lCd, lName);
                    retList.add(lmst);
                }
            } catch (SQLException ex) {
                log.debug("CERTIF_SCHOOL_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

    }
    private class Lmst {
        private final String _lCd;
        private final String _lName;
        public Lmst(
                final String lCd,
                final String lName
        ) {
            _lCd   = lCd;
            _lName = lName;
        }
    }
}

// eof

