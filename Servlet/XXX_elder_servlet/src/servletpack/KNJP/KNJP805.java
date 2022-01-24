/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 6869d0d1c0a84eff761cd5d97a555fe779a45940 $
 *
 * 作成日: 2018/03/16
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP805 {

    private static final Log log = LogFactory.getLog(KNJP805.class);

    private boolean _hasData;
    private static final String FRM805_2 = "KNJP805_2";
    private static final String FRM805_3 = "KNJP805_3";
    private static final String FRM805_4 = "KNJP805_4";

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
        String formName = "KNJP805";
        String setKeisyou = "様";
        if (FRM805_2.equals(_param._useFormNameP805)) {
            formName = _param._useFormNameP805;
            setKeisyou = "保護者様";
        } else if (FRM805_3.equals(_param._useFormNameP805)) {
            formName = _param._useFormNameP805;
        } else if (FRM805_4.equals(_param._useFormNameP805)) {
            formName = _param._useFormNameP805;
        }
        svf.VrSetForm(formName + ".frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintStudent printStudent = (PrintStudent) iterator.next();
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
            if (!StringUtils.isBlank(printStudent._guardZipcd)) {
            	svf.VrsOut("ZIPNO", "〒" + printStudent._guardZipcd);
            }

            final int addr1Len = KNJ_EditEdit.getMS932ByteLength(printStudent._guardAddr1);
            final int addr2Len = KNJ_EditEdit.getMS932ByteLength(printStudent._guardAddr2);
            final String addrField = addr1Len > 50 || addr2Len > 50 ? "_4" : addr1Len > 40 || addr2Len > 40 ? "_3" : addr1Len > 30 || addr2Len > 30 ? "_2" : "_1";

            svf.VrsOut("ADDRESS1" + addrField, printStudent._guardAddr1);
            svf.VrsOut("ADDRESS2" + addrField, printStudent._guardAddr2);
            final String schoolName = _param._schoolNameMap.containsKey(printStudent._schoolKind) ? (String) _param._schoolNameMap.get(printStudent._schoolKind) : "";
            if (FRM805_4.equals(_param._useFormNameP805)) {
                svf.VrsOut("HR_NAME", schoolName + "　" + printStudent._hrName + "　" + Integer.parseInt(printStudent._attendno) + "番");
            } else {
                svf.VrsOut("HR_NAME", printStudent._hrName + "　" + String.valueOf(Integer.parseInt(printStudent._attendno)) + "番");
            }
            if ("1".equals(_param._documentMstSchregnoFlg)) {
                svf.VrsOut("SCHREG_NO", "（学籍番号：" + printStudent._schregno + "）");
            }

            final int nameLen = KNJ_EditEdit.getMS932ByteLength(printStudent._name + "　" + setKeisyou);
            final String nameField = nameLen > 44 ? "_3" : nameLen > 34 ? "_2" : "_1";
            svf.VrsOut("NAME1" + nameField, printStudent._name + "　" + setKeisyou);

            if (FRM805_3.equals(_param._useFormNameP805)) {
                svf.VrsOut("NAME2_1", "保護者　様");
            } else {
                final int gnameLen = KNJ_EditEdit.getMS932ByteLength(printStudent._guardName + "　" + setKeisyou);
                final String gnameField = gnameLen > 44 ? "_3" : gnameLen > 34 ? "_2" : "_1";
                svf.VrsOut("NAME2" + gnameField, printStudent._guardName + "　" + setKeisyou);
            }

            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("JOB_NAME", _param._jobName);
            svf.VrsOut("STAFF_NAME1", _param._tantouName);

            final int byteTitle = (FRM805_2.equals(_param._useFormNameP805)) ? 72: 80;
            svf.VrsOut("TITLE", printStudent._title);
            final String[] setText = KNJ_EditEdit.get_token(printStudent._text, byteTitle, 13);
            if (null != setText) {
                for (int textCnt = 0; textCnt < setText.length; textCnt++) {
                    final int textField = textCnt + 1;
                    svf.VrsOut("TEXT" + textField, setText[textCnt]);
                }
            }
            svf.VrsOut("METHOD", printStudent._collectPatternName);

            int moneyInfoCnt = 1;
            int totalMoney = 0;
            for (Iterator itMoneyInfo = printStudent._moneyList.iterator(); itMoneyInfo.hasNext();) {
                final MoneyInfo moneyInfo = (MoneyInfo) itMoneyInfo.next();

                svf.VrsOutn("MONTH", moneyInfoCnt, moneyInfo._month + "月分");
                if (moneyInfo._paidFlg) {
                    svf.VrsOutn("FIN", moneyInfoCnt, "済");
                } else {
                    svf.VrsOutn("MONEY", moneyInfoCnt, String.valueOf(moneyInfo._planMoney));
                    totalMoney += moneyInfo._planMoney;
                }
                moneyInfoCnt++;
            }
            svf.VrsOut("MONTH_TOTAL", "合計");
            svf.VrsOut("MONEY_TOTAL", String.valueOf(totalMoney));

            final String nextMD = KNJ_EditDate.h_format_JP_MD(_param._sendPaidDate1);
            final String nextWeek = KNJ_EditDate.h_format_W(_param._sendPaidDate1);
            if (null != _param._sendPaidDate1 && !"".equals(_param._sendPaidDate1)) {
                if (FRM805_2.equals(_param._useFormNameP805)) {
                    svf.VrsOut("NEXT_PAY_TITLE", "次回引き落とし日");
                } else if (FRM805_4.equals(_param._useFormNameP805)) {
                    svf.VrsOut("NEXT_PAY_TITLE", "納入期限");
                    svf.VrsOut("PAY_METHOD_TITLE", "納入方法");
                    svf.VrsOut("PAY_METHOD", "銀行振込");
                } else {
                    svf.VrsOut("NEXT_PAY_TITLE", "次回振替月");
                }
                svf.VrsOut("NEXT_PAY", nextMD + "(" + nextWeek + ")");
            }

            if (null != _param._sendPaidDate2 && !"".equals(_param._sendPaidDate2)) {
                final String nextMD2 = KNJ_EditDate.h_format_JP_MD(_param._sendPaidDate2);
                final String nextWeek2 = KNJ_EditDate.h_format_W(_param._sendPaidDate2);
                svf.VrsOut("NEXT_PAY2", nextMD2 + "(" + nextWeek2 + ")");
            }

            svf.VrsOut("CHARGE", printStudent._staffname);
            svf.VrsOut("TELNO", _param._schoolPhoneNumber);

            svf.VrsOut("REMARK1", _param._remark2);
            svf.VrsOut("REMARK2", _param._remark3);
            svf.VrsOut("REMARK3", _param._remark4);
            svf.VrsOut("REMARK4", _param._remark5);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schoolcd           = rs.getString("SCHOOLCD");
                final String schoolKind         = rs.getString("SCHOOL_KIND");
                final String year               = rs.getString("YEAR");
                final String slipNo             = rs.getString("SLIP_NO");
                final String schregno           = rs.getString("SCHREGNO");
                final String gradeCd            = rs.getString("GRADE_CD");
                final String grade              = rs.getString("GRADE");
                final String hrClass            = rs.getString("HR_CLASS");
                final String hrName             = rs.getString("HR_NAME");
                final String attendno           = rs.getString("ATTENDNO");
                final String name               = rs.getString("NAME");
                final String guardName          = rs.getString("GUARD_NAME");
                final String guardZipcd         = rs.getString("GUARD_ZIPCD");
                final String guardAddr1         = rs.getString("GUARD_ADDR1");
                final String guardAddr2         = rs.getString("GUARD_ADDR2");
                final String collectPatternName = rs.getString("COLLECT_PATTERN_NAME");
                final String documentcd         = rs.getString("DOCUMENTCD");
                final String title              = rs.getString("TITLE");
                final String text               = StringUtils.defaultString(rs.getString("TEXT"));
                final String reminderStaffcd    = rs.getString("REMINDER_STAFFCD");
                final String staffname          = rs.getString("STAFFNAME");

                final PrintStudent printStudent = new PrintStudent(db2, schoolcd, schoolKind, year, slipNo, schregno, gradeCd, grade, hrClass, hrName, attendno, name, guardName, guardZipcd, guardAddr1, guardAddr2, collectPatternName, documentcd, title, text, reminderStaffcd, staffname);
                printStudent.setMoneyList(db2);
                retList.add(printStudent);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SLIPD.SCHOOLCD, ");
        stb.append("     SLIPD.SCHOOL_KIND, ");
        stb.append("     SLIPD.YEAR, ");
        stb.append("     SLIPD.SLIP_NO, ");
        stb.append("     SLIPD.SCHREGNO, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     GUARD.GUARD_NAME, ");
        stb.append("     GUARD.GUARD_ZIPCD, ");
        stb.append("     VALUE(GUARD.GUARD_ADDR1, '') AS GUARD_ADDR1, ");
        stb.append("     VALUE(GUARD.GUARD_ADDR2, '') AS GUARD_ADDR2, ");
        stb.append("     DEFAULT_SET.COLLECT_PATTERN_NAME ");
        stb.append(" FROM ");
        stb.append("     COLLECT_SLIP_DAT SLIPD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON SLIPD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("          AND SLIPD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_ClASS = REGDH.HR_ClASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("          AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARD ON SLIPD.SCHREGNO = GUARD.SCHREGNO ");
        stb.append("     LEFT JOIN COLLECT_DEFAULT_SETTINGS_MST DEFAULT_SET ON SLIPD.SCHOOLCD = DEFAULT_SET.SCHOOLCD ");
        stb.append("          AND SLIPD.SCHOOL_KIND = DEFAULT_SET.SCHOOL_KIND ");
        stb.append("          AND SLIPD.YEAR = DEFAULT_SET.YEAR ");
        stb.append("          AND SLIPD.COLLECT_PATTERN_CD = DEFAULT_SET.COLLECT_PATTERN_CD ");
        stb.append(" WHERE ");
        stb.append("     SLIPD.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND SLIPD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append("     AND SLIPD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND SLIPD.SLIP_NO IN " + _param._slipInState + " ");
        stb.append(" ), DOCUMENT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.SLIP_NO, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     MAX_REMINDER.DOCUMENTCD, ");
        stb.append("     DOC_M.TITLE, ");
        stb.append("     DOC_M.TEXT, ");
        stb.append("     MAX_REMINDER.REMINDER_STAFFCD, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        if ("read".equals(_param._sendCmd)) {
            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             REMINDER.* ");
            stb.append("         FROM ");
            stb.append("             ( ");
            stb.append("             SELECT ");
            stb.append("                 REMINDER.SCHOOLCD, ");
            stb.append("                 REMINDER.SCHOOL_KIND, ");
            stb.append("                 REMINDER.YEAR, ");
            stb.append("                 REMINDER.SLIP_NO, ");
            stb.append("                 MAX(INT(SEQ)) AS SEQ ");
            stb.append("             FROM ");
            stb.append("                 SCH_T, ");
            stb.append("                 COLLECT_SLIP_REMINDER_DAT REMINDER ");
            stb.append("             WHERE ");
            stb.append("                 SCH_T.SCHOOLCD = REMINDER.SCHOOLCD ");
            stb.append("                 AND SCH_T.SCHOOL_KIND = REMINDER.SCHOOL_KIND ");
            stb.append("                 AND SCH_T.YEAR = REMINDER.YEAR ");
            stb.append("                 AND SCH_T.SLIP_NO = REMINDER.SLIP_NO ");
            stb.append("             GROUP BY ");
            stb.append("                 REMINDER.SCHOOLCD, ");
            stb.append("                 REMINDER.SCHOOL_KIND, ");
            stb.append("                 REMINDER.YEAR, ");
            stb.append("                 REMINDER.SLIP_NO ");
            stb.append("             ) AS MAX_SEQ ");
            stb.append("             LEFT JOIN COLLECT_SLIP_REMINDER_DAT REMINDER ON MAX_SEQ.SCHOOLCD = REMINDER.SCHOOLCD ");
            stb.append("                  AND MAX_SEQ.SCHOOLCD = REMINDER.SCHOOLCD ");
            stb.append("                  AND MAX_SEQ.SCHOOL_KIND = REMINDER.SCHOOL_KIND ");
            stb.append("                  AND MAX_SEQ.YEAR = REMINDER.YEAR ");
            stb.append("                  AND MAX_SEQ.SLIP_NO = REMINDER.SLIP_NO ");
            stb.append("                  AND MAX_SEQ.SEQ = INT(REMINDER.SEQ) ");
            stb.append("     ) AS MAX_REMINDER ON SCH_T.SCHOOLCD = MAX_REMINDER.SCHOOLCD ");
            stb.append("     AND SCH_T.SCHOOL_KIND = MAX_REMINDER.SCHOOL_KIND ");
            stb.append("     AND SCH_T.YEAR = MAX_REMINDER.YEAR ");
            stb.append("     AND SCH_T.SLIP_NO = MAX_REMINDER.SLIP_NO ");
        } else {
            stb.append("     LEFT JOIN COLLECT_SLIP_REMINDER_TESTPRINT_DAT MAX_REMINDER ON SCH_T.SCHOOLCD = MAX_REMINDER.SCHOOLCD ");
            stb.append("     AND SCH_T.SCHOOL_KIND = MAX_REMINDER.SCHOOL_KIND ");
            stb.append("     AND SCH_T.YEAR = MAX_REMINDER.YEAR ");
            stb.append("     AND SCH_T.SLIP_NO = MAX_REMINDER.SLIP_NO ");
        }
        stb.append("     LEFT JOIN DOCUMENT_MST DOC_M ON MAX_REMINDER.DOCUMENTCD = DOC_M.DOCUMENTCD ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON MAX_REMINDER.REMINDER_STAFFCD = STAFF.STAFFCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.*, ");
        stb.append("     DOCUMENT_T.DOCUMENTCD, ");
        stb.append("     DOCUMENT_T.TITLE, ");
        stb.append("     DOCUMENT_T.TEXT, ");
        stb.append("     DOCUMENT_T.REMINDER_STAFFCD, ");
        stb.append("     DOCUMENT_T.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     LEFT JOIN DOCUMENT_T ON SCH_T.SLIP_NO = DOCUMENT_T.SLIP_NO ");
        stb.append(" ORDER BY ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.ATTENDNO ");

        return stb.toString();
    }

    private class PrintStudent {
        final String _schoolcd;
        final String _schoolKind;
        final String _year;
        final String _slipNo;
        final String _schregno;
        final String _gradeCd;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _name;
        final String _guardName;
        final String _guardZipcd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _collectPatternName;
        final String _documentcd;
        final String _title;
        final String _text;
        final String _reminderStaffcd;
        final String _staffname;
        final String _nextDate;
        final List _moneyList;
        public PrintStudent(
                final DB2UDB db2,
                final String schoolcd,
                final String schoolKind,
                final String year,
                final String slipNo,
                final String schregno,
                final String gradeCd,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String name,
                final String guardName,
                final String guardZipcd,
                final String guardAddr1,
                final String guardAddr2,
                final String collectPatternName,
                final String documentcd,
                final String title,
                final String text,
                final String reminderStaffcd,
                final String staffname
        ) {
            _schoolcd           = schoolcd;
            _schoolKind         = schoolKind;
            _year               = year;
            _slipNo             = slipNo;
            _schregno           = schregno;
            _gradeCd            = gradeCd;
            _grade              = grade;
            _hrClass            = hrClass;
            _hrName             = hrName;
            _attendno           = attendno;
            _name               = name;
            _guardName          = guardName;
            _guardZipcd         = guardZipcd;
            _guardAddr1         = guardAddr1;
            _guardAddr2         = guardAddr2;
            _collectPatternName = collectPatternName;
            _documentcd         = documentcd;
            _title              = title;
            _text               = text;
            _reminderStaffcd    = reminderStaffcd;
            _staffname          = staffname;
            _nextDate           = getNextDate(db2);
            _moneyList          = new ArrayList();
        }

        private void setMoneyList(final DB2UDB db2) {

            StringBuffer stb = new StringBuffer();
            stb.append(" WITH PRINT_T AS ( ");
            stb.append("     SELECT ");
            stb.append("         PLAN_M.SCHOOLCD, ");
            stb.append("         PLAN_M.SCHOOL_KIND, ");
            stb.append("         PLAN_M.YEAR, ");
            stb.append("         PLAN_M.SLIP_NO, ");
            stb.append("         PLAN_M.SCHREGNO, ");
            stb.append("         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) - SUM(VALUE(PLAN_M.PAID_MONEY, 0)) AS PLAN_MONEY, ");
            stb.append("         LIMIT_D.PAID_LIMIT_MONTH, ");
            stb.append("         LIMIT_D.PAID_LIMIT_DATE ");
            stb.append("     FROM ");
            stb.append("         COLLECT_SLIP_PLAN_M_DAT PLAN_M ");
            stb.append("         LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN_M.SCHOOLCD ");
            stb.append("                                        AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
            stb.append("                                        AND SL_D.YEAR        = PLAN_M.YEAR ");
            stb.append("                                        AND SL_D.SLIP_NO     = PLAN_M.SLIP_NO ");
            stb.append("         INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ON PLAN_M.SCHOOLCD         = LIMIT_D.SCHOOLCD ");
            stb.append("                                                           AND PLAN_M.SCHOOL_KIND      = LIMIT_D.SCHOOL_KIND ");
            stb.append("                                                           AND PLAN_M.YEAR             = LIMIT_D.YEAR ");
            stb.append("                                                           AND PLAN_M.SCHREGNO         = LIMIT_D.SCHREGNO ");
            stb.append("                                                           AND PLAN_M.SLIP_NO          = LIMIT_D.SLIP_NO ");
            stb.append("                                                           AND PLAN_M.PLAN_YEAR        = LIMIT_D.PLAN_YEAR ");
            stb.append("                                                           AND PLAN_M.PLAN_MONTH       = LIMIT_D.PLAN_MONTH ");
            stb.append("                                                           AND LIMIT_D.PAID_LIMIT_DATE < '" + _param._limitDate + "' ");
            stb.append("     WHERE ");
            stb.append("             PLAN_M.SCHOOLCD    = '" + _schoolcd + "' ");
            stb.append("         AND PLAN_M.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("         AND PLAN_M.YEAR        = '" + _year + "' ");
            stb.append("         AND PLAN_M.SLIP_NO     = '" + _slipNo + "' ");
            stb.append("     GROUP BY ");
            stb.append("         PLAN_M.SCHOOLCD, ");
            stb.append("         PLAN_M.SCHOOL_KIND, ");
            stb.append("         PLAN_M.YEAR, ");
            stb.append("         PLAN_M.SLIP_NO, ");
            stb.append("         PLAN_M.SCHREGNO, ");
            stb.append("         LIMIT_D.PAID_LIMIT_MONTH, ");
            stb.append("         LIMIT_D.PAID_LIMIT_DATE ");
            stb.append(" ), MAIN_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     PRINT_T.SCHREGNO, ");
            stb.append("     PRINT_T.SLIP_NO, ");
            stb.append("     CASE WHEN VALUE(PRINT_T.PLAN_MONEY, 0) <= 0 ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '2' ");
            stb.append("     END AS PAID_FLG, ");
            stb.append("     VALUE(PRINT_T.PLAN_MONEY, 0) - ");
            stb.append("     VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) - ");
            stb.append("     VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) - ");
            stb.append("     VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0) - ");
            stb.append("     VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0) - ");
            stb.append("     VALUE(BURDEN_1.BURDEN_CHARGE, 0) - ");
            stb.append("     VALUE(BURDEN_2.BURDEN_CHARGE, 0) - ");
            stb.append("     VALUE(SCHOOL_1.PLAN_MONEY, 0) - ");
            stb.append("     VALUE(SCHOOL_2.PLAN_MONEY, 0) AS DISP_PLAN_MONEY, ");
            stb.append("     PRINT_T.PAID_LIMIT_DATE ");
            stb.append(" FROM ");
            stb.append("     PRINT_T ");
            stb.append("     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON PRINT_T.SCHOOLCD         = REDUC_C.SCHOOLCD ");
            stb.append("                                                 AND PRINT_T.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ");
            stb.append("                                                 AND PRINT_T.YEAR             = REDUC_C.YEAR ");
            stb.append("                                                 AND PRINT_T.SLIP_NO          = REDUC_C.SLIP_NO ");
            stb.append("                                                 AND PRINT_T.PAID_LIMIT_MONTH = REDUC_C.PLAN_MONTH ");
            stb.append("     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON PRINT_T.SCHOOLCD          = REDUC_D1.SCHOOLCD ");
            stb.append("                                          AND PRINT_T.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ");
            stb.append("                                          AND PRINT_T.YEAR              = REDUC_D1.YEAR ");
            stb.append("                                          AND REDUC_D1.REDUCTION_TARGET = '1' ");
            stb.append("                                          AND PRINT_T.SLIP_NO           = REDUC_D1.SLIP_NO ");
            stb.append("                                          AND PRINT_T.PAID_LIMIT_MONTH  = REDUC_D1.PLAN_MONTH ");
            stb.append("     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON PRINT_T.SCHOOLCD          = REDUC_D2.SCHOOLCD ");
            stb.append("                                          AND PRINT_T.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ");
            stb.append("                                          AND PRINT_T.YEAR              = REDUC_D2.YEAR ");
            stb.append("                                          AND REDUC_D2.REDUCTION_TARGET = '2' ");
            stb.append("                                          AND PRINT_T.SLIP_NO           = REDUC_D2.SLIP_NO ");
            stb.append("                                          AND PRINT_T.PAID_LIMIT_MONTH  = REDUC_D2.PLAN_MONTH ");
            stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON PRINT_T.SCHOOLCD        = BURDEN_1.SCHOOLCD ");
            stb.append("                                                      AND PRINT_T.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ");
            stb.append("                                                      AND PRINT_T.YEAR              = BURDEN_1.YEAR ");
            stb.append("                                                      AND BURDEN_1.REDUCTION_TARGET = '1' ");
            stb.append("                                                      AND PRINT_T.SLIP_NO           = BURDEN_1.SLIP_NO ");
            stb.append("                                                      AND PRINT_T.PAID_LIMIT_MONTH  = BURDEN_1.PLAN_MONTH ");
            stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON PRINT_T.SCHOOLCD        = BURDEN_2.SCHOOLCD ");
            stb.append("                                                      AND PRINT_T.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ");
            stb.append("                                                      AND PRINT_T.YEAR              = BURDEN_2.YEAR ");
            stb.append("                                                      AND BURDEN_2.REDUCTION_TARGET = '2' ");
            stb.append("                                                      AND PRINT_T.SLIP_NO           = BURDEN_2.SLIP_NO ");
            stb.append("                                                      AND PRINT_T.PAID_LIMIT_MONTH  = BURDEN_2.PLAN_MONTH ");
            stb.append("     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON PRINT_T.SCHOOLCD          = SCHOOL_1.SCHOOLCD ");
            stb.append("                                                 AND PRINT_T.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ");
            stb.append("                                                 AND PRINT_T.YEAR              = SCHOOL_1.YEAR ");
            stb.append("                                                 AND SCHOOL_1.REDUCTION_TARGET = '1' ");
            stb.append("                                                 AND PRINT_T.SLIP_NO           = SCHOOL_1.SLIP_NO ");
            stb.append("                                                 AND PRINT_T.PAID_LIMIT_MONTH  = SCHOOL_1.PLAN_MONTH ");
            stb.append("     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON PRINT_T.SCHOOLCD          = SCHOOL_2.SCHOOLCD ");
            stb.append("                                                 AND PRINT_T.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ");
            stb.append("                                                 AND PRINT_T.YEAR              = SCHOOL_2.YEAR ");
            stb.append("                                                 AND SCHOOL_2.REDUCTION_TARGET = '2' ");
            stb.append("                                                 AND PRINT_T.SLIP_NO           = SCHOOL_2.SLIP_NO ");
            stb.append("                                                 AND PRINT_T.PAID_LIMIT_MONTH  = SCHOOL_2.PLAN_MONTH ");
            stb.append(" ), RESULT_DATE AS ( ");
            stb.append("      SELECT ");
            stb.append("        YEAR (PAID_LIMIT_DATE) AS PLAN_YEAR, ");
            stb.append("        MONTH (PAID_LIMIT_DATE) AS PLAN_MONTH, ");
            stb.append("        SUM(DISP_PLAN_MONEY) AS PLAN_MONEY, ");
            stb.append("        CASE ");
            stb.append("          WHEN SUM(DISP_PLAN_MONEY) <= 0 ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '2' ");
            stb.append("          END AS PAID_FLG ");
            stb.append("      FROM ");
            stb.append("        MAIN_T ");
            stb.append("      GROUP BY ");
            stb.append("        PAID_LIMIT_DATE ");
            stb.append("      ORDER BY ");
            stb.append("        PAID_LIMIT_DATE ");
            stb.append(" ) ");
            stb.append("    SELECT ");
            stb.append("      PLAN_YEAR, ");
            stb.append("      PLAN_MONTH, ");
            stb.append("      SUM(PLAN_MONEY) AS PLAN_MONEY, ");
            stb.append("      CASE ");
            stb.append("        WHEN SUM(PLAN_MONEY) <= 0 ");
            stb.append("        THEN '1' ");
            stb.append("        ELSE '2' ");
            stb.append("        END AS PAID_FLG ");
            stb.append("    FROM ");
            stb.append("      RESULT_DATE ");
            stb.append("    GROUP BY ");
            stb.append("      PLAN_YEAR, ");
            stb.append("      PLAN_MONTH ");

            final String sql = stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("PLAN_YEAR");
                    final String month = rs.getString("PLAN_MONTH");
                    final int planMoney = rs.getInt("PLAN_MONEY");
                    final boolean paidFlg = "1".equals(rs.getString("PAID_FLG"));
                    final MoneyInfo moneyInfo = new MoneyInfo(year, month, planMoney, paidFlg);
                    _moneyList.add(moneyInfo);
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }

        private String getNextDate(final DB2UDB db2) {
            String retStr = "";

            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MIN(LIMIT_D.PAID_LIMIT_DATE) AS PAID_LIMIT_DATE ");
            stb.append(" FROM ");
            stb.append("     COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ");
            stb.append(" WHERE ");
            stb.append("         LIMIT_D.SCHOOLCD    = '" + _schoolcd + "' ");
            stb.append("     AND LIMIT_D.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND LIMIT_D.YEAR        = '" + _year + "' ");
            stb.append("     AND LIMIT_D.SLIP_NO     = '" + _slipNo + "' ");
            stb.append("     AND LIMIT_D.PAID_LIMIT_DATE > '" + _param._limitDate + "' ");

            final String sql = stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("PAID_LIMIT_DATE");
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
            return retStr;
        }

    }

    /** 生徒データクラス */
    private class MoneyInfo {
        final String _year;
        final String _month;
        final int _planMoney;
        final boolean _paidFlg;

        MoneyInfo(
                final String year,
                final String month,
                final int planMoney,
                final boolean paidFlg
        ) {
            _year       = year;
            _month      = month;
            _planMoney  = planMoney;
            _paidFlg    = paidFlg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72746 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _paidLimitMonth;
        private final String _reminderStaffCd;
        private final String[] _printSlipNos;
        private final String _slipInState;
        private final String _limitDate;
        private final String _sendCmd;
        private final String _sendPaidDate1;
        private final String _sendPaidDate2;
        private final Map _schoolNameMap;
        private String _schoolName;
        private String _jobName;
        private String _tantouName;
        private String _schoolPhoneNumber;
        private String _remark2;
        private String _remark3;
        private String _remark4;
        private String _remark5;
        private final String _useFormNameP805;
        private final String _documentMstSchregnoFlg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _paidLimitMonth = request.getParameter("PAID_LIMIT_MONTH");
            _reminderStaffCd = request.getParameter("REMINDER_STAFFCD");
            _useFormNameP805 = request.getParameter("useFormNameP805");
            _documentMstSchregnoFlg = request.getParameter("documentMstSchregnoFlg");
            _printSlipNos = StringUtils.split(request.getParameter("PRINT_SLIP_NO"), ',');
            String inState = "('";
            String sep = "";
            for (int i = 0; i < _printSlipNos.length; i++) {
                inState += sep + _printSlipNos[i];
                sep = "','";
            }
            inState += "')";
            _slipInState = inState;
            _limitDate = request.getParameter("LIMIT_DATE");
            _sendCmd = request.getParameter("SENDCMD");
            _sendPaidDate1 = request.getParameter("SEND_PAID_DATE1");
            _sendPaidDate2 = request.getParameter("SEND_PAID_DATE2");
            _schoolNameMap = getSchoolNameMap(db2);
            loadCertifSchool(db2);
        }

        private Map getSchoolNameMap(final DB2UDB db2) {
            final Map retMap = new HashMap<String, String>();

            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     SCHOOLNAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOLCD = '" + _schoolCd + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String schoolName = rs.getString("SCHOOLNAME1");
                    retMap.put(schoolKind, schoolName);
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
            return retMap;
        }

        private void loadCertifSchool(final DB2UDB db2) {

            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     JOB_NAME, ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2, ");
            stb.append("     REMARK3, ");
            stb.append("     REMARK4, ");
            stb.append("     REMARK5, ");
            stb.append("     REMARK5 AS SCHOOL_PHONE_NUMBER ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD = '143' ");

            String sqlCertifSchool = stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlCertifSchool);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _tantouName = rs.getString("REMARK1");
                    _schoolPhoneNumber = rs.getString("SCHOOL_PHONE_NUMBER");
                    _remark2 = rs.getString("REMARK2");
                    _remark3 = rs.getString("REMARK3");
                    _remark4 = rs.getString("REMARK4");
                    _remark5 = rs.getString("REMARK5");
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }

    }
}

// eof
