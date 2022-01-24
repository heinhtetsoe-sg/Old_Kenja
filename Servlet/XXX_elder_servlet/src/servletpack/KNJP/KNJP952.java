/*
 * $Id$
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

public class KNJP952 {

    private static final Log log = LogFactory.getLog(KNJP952.class);

    private boolean _hasData;
    private final int ONELINEMAXCNT = 75;
    private final int ONEPAGECOLUMNS = 4;

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

        //生徒詳細情報セット(LEVY_REQUEST_OUTGO_SCHREG_DAT)
        final Map schMap = setOutgoSchregDat(db2);

        NumberFormat nfNum = NumberFormat.getNumberInstance();    //カンマ区切り形式

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final String setForm = ("1".equals(_param._useLevyIraisyo)) ? "KNJP952_3.frm": "KNJP952.frm";
            svf.VrSetForm(setForm, 1);
            OutGoDat outGoDat = (OutGoDat) iterator.next();
//            svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(outGoDat._year)) + "年度");
            svf.VrsOut("NENDO", outGoDat._year + "年度");
            svf.VrsOut("BILL_NAME", outGoDat._outgoLName);
            svf.VrsOut("ITEM_NAME", outGoDat._outgoMName);
            svf.VrsOut("BILL_NO", outGoDat._requestNo);

            // 印鑑等
            printStamp(svf);

            final int tesuuryo = null != outGoDat._requestTesuuryou ? Integer.parseInt(outGoDat._requestTesuuryou) : 0;
            final String reqestGk = String.valueOf(Integer.parseInt(outGoDat._requestGk) + tesuuryo);
            svf.VrsOut("TOTAL_MONEY", "\\" + reqestGk);
            if ("1".equals(_param._useLevyIraisyo)) {
                svf.VrsOut("RESOLUTION_REASON", outGoDat._traderName);
            } else {
                svf.VrsOut("RESOLUTION_REASON", outGoDat._requestReason);
            }
//            svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, outGoDat._requestDate));
            svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(outGoDat._requestDate));

            String lineNo = "0";
            int meisaiTotal = null != outGoDat._requestTesuuryou ? Integer.parseInt(outGoDat._requestTesuuryou) : 0;
            int meisaiTotalTax = 0;
            for (Iterator itMeisai = outGoDat._meisai.iterator(); itMeisai.hasNext();) {
                OutGoMeisai outgoMeisai = (OutGoMeisai) itMeisai.next();

                svf.VrsOutn("NO", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._lineNo);                  // No
                final String billField = KNJ_EditEdit.getMS932ByteLength(outgoMeisai._commodityName) > 32 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(outgoMeisai._commodityName) > 24 ? "2" : "1";
                svf.VrsOutn("BILL_NAME" + billField, Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._commodityName);   // 品名等
                svf.VrsOutn("PRICE", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._commodityPrice);       // 単価
                svf.VrsOutn("SUM", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._commodityCnt);           // 数量
                svf.VrsOutn("SUB_TOTAL", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._totalPrice);       // 金額

                final String remarkField = KNJ_EditEdit.getMS932ByteLength(outgoMeisai._remark) > 10 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(outgoMeisai._remark) > 6 ? "2" : "1";
                svf.VrsOutn("REMARK" + remarkField, Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._remark);

                //明細行
                final String setKey = outGoDat._requestNo + "-" + outgoMeisai._lineNo;
                final List getArryWk = (List)schMap.get(setKey);
                String hrNameSchName = "";
                if (getArryWk != null && getArryWk.size() > 0) {
                    final Student outwk = (Student)getArryWk.get(0);
                    hrNameSchName = outwk == null ? "" : StringUtils.defaultString(outwk.retHr_Name());
                }

                if (outgoMeisai != null && !"".equals(hrNameSchName)) {
                    final int schTotalMnoey = (outgoMeisai._schCnt == null || outgoMeisai._schPrice == null) ? 0 : Integer.parseInt(outgoMeisai._schCnt) * Integer.parseInt(outgoMeisai._schPrice);
                    final String hasuu = (outgoMeisai._hasuu != null && Integer.parseInt(outgoMeisai._hasuu) > 0) ? "　端数 " + nfNum.format(Integer.valueOf(outgoMeisai._hasuu)): "";
                    final String printHrSchName = "1".equals(_param._KNJP952_PrintStdNameMeisai) ? "　　" + hrNameSchName + "　他計 " : "計 ";
                    final String meisai = printHrSchName + StringUtils.defaultString(outgoMeisai._schCnt, "0") + "名×＠" + StringUtils.defaultString(outgoMeisai._schPrice, "0") + "＝" + nfNum.format(schTotalMnoey) + hasuu;
                    svf.VrsOutn("DETAIL", Integer.parseInt(outgoMeisai._lineNo), meisai);
                }

                meisaiTotal += Integer.parseInt(outgoMeisai._totalPrice); // 摘要
                meisaiTotal += Integer.parseInt(outgoMeisai._totalTax);
                meisaiTotalTax += Integer.parseInt(outgoMeisai._totalTax);
                lineNo = outgoMeisai._lineNo;
            }

            if ("1".equals(_param._useLevyIraisyo) && tesuuryo != 0) {
                final String setLineNo = String.valueOf(Integer.parseInt(lineNo) + 1);
                svf.VrsOutn("NO", Integer.parseInt(setLineNo), setLineNo);                        // No
                svf.VrsOutn("BILL_NAME1", Integer.parseInt(setLineNo), _param._titleTesuuryo);    // 品名等
                svf.VrsOutn("PRICE", Integer.parseInt(setLineNo), outGoDat._requestTesuuryou);    // 単価
                svf.VrsOutn("SUM", Integer.parseInt(setLineNo), "1");                             // 数量
                svf.VrsOutn("SUB_TOTAL", Integer.parseInt(setLineNo), outGoDat._requestTesuuryou);// 金額

                final String remarkField = KNJ_EditEdit.getMS932ByteLength(outGoDat._tesuuryoSummary) > 10 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(outGoDat._tesuuryoSummary) > 6 ? "2" : "1";
                svf.VrsOutn("REMARK" + remarkField, Integer.parseInt(setLineNo), outGoDat._tesuuryoSummary);
            }

            if (0 < meisaiTotalTax) {
                svf.VrsOut("TAX_NAME", "　　 消　費　税");
                svf.VrsOutn("SUB_TOTAL2", 1, String.valueOf(meisaiTotalTax)); //消費税
            }
            svf.VrsOutn("SUB_TOTAL2", 2, String.valueOf(meisaiTotal)); //計

            if ("1".equals(_param._useLevyIraisyo)) {
                if (null != outGoDat._remark) {
                final String[] summaryArray = KNJ_EditEdit.get_token(outGoDat._remark, 60, 2);
                    for (int i = 0; i < summaryArray.length; i++) {
                        String sumarry = summaryArray[i];
                        svf.VrsOut("REMARK1" + String.valueOf(i + 1), sumarry); //備考
                    }
                }
            }

            svf.VrEndPage();

            int onePageColumns = ONEPAGECOLUMNS;
            int oneLineMaxCnt = ONELINEMAXCNT;
            for (Iterator itMeisai = outGoDat._meisai.iterator(); itMeisai.hasNext();) {
                String bakGrHrCls = "";
                svf.VrSetForm("KNJP952_2.frm", 4);

                OutGoMeisai outgoMeisai = (OutGoMeisai) itMeisai.next();
                final String setKey = outGoDat._requestNo + "-" + outgoMeisai._lineNo;
                final List outArryWk = (List)schMap.get(setKey);
                if (outArryWk == null || outArryWk.size() == 0) {
                    //ヘッダ部、空の表だけ出るようにして抜ける。
                    svf.VrsOut("NO", outgoMeisai._lineNo); // No
                    svf.VrsOut("BILL_NAME", outgoMeisai._commodityName);   // 品名等
                    svf.VrsOut("PAGE", "1 / 1");
                    if ("1".equals(_param._KNJP952_PrintStdCntMeisai)) {
                        svf.VrsOut("TOTAL_NUM", "0名");
                    }
                    svf.VrEndRecord();

                    continue;
                }
                int prtRowCnt = 1;
                int prtColCnt = 1;
                int pageCnt = 1;
                int pageMaxCnt = getPrintPageMax(outArryWk);
                for (Iterator its = outArryWk.iterator();its.hasNext();) {
                    final Student outwk = (Student)its.next();
                    //改列判定
                    if (!"1".equals(_param._chgClass)) {
                        if (prtRowCnt > oneLineMaxCnt * onePageColumns) {
                            prtRowCnt = 1;
                            prtColCnt = 1;
                            pageCnt++;
                            bakGrHrCls = "";
                            svf.VrSetForm("KNJP952_2.frm", 4);
                        }
                    } else {
                        if(prtRowCnt > ONELINEMAXCNT || !"".equals(bakGrHrCls) && !bakGrHrCls.equals(outwk.getGr_HrClass())) {
                            //余白対応
                            if (prtRowCnt <= oneLineMaxCnt) {
                                while (prtRowCnt <= oneLineMaxCnt) {
                                    svf.VrEndRecord();
                                    prtRowCnt++;
                                }
                            }
                            prtRowCnt = 1;
                            //1ページの列数を超えた?
                            prtColCnt++;
                            if (onePageColumns < prtColCnt) {
                                prtColCnt = 1;
                                pageCnt++;
                                bakGrHrCls = "";
                                svf.VrSetForm("KNJP952_2.frm", 4);
                            }
                        }
                    }
                    if (prtRowCnt == 1) {
                        svf.VrsOut("NO", outgoMeisai._lineNo); // No
                        svf.VrsOut("BILL_NAME", outgoMeisai._commodityName);   // 品名等
                        svf.VrsOut("PAGE", pageCnt + " / " + pageMaxCnt);
                        if ("1".equals(_param._KNJP952_PrintStdCntMeisai) && pageCnt == 1) {
                            //1ページ目に生徒の合計数を表示
                            svf.VrsOut("TOTAL_NUM", outgoMeisai._schCnt + "名");
                        }
                    }
                    //明細行
                    if (outwk != null && outwk._name != null && !"".equals(outwk._name)) {
                        svf.VrsOut("HR_NAME1", outwk._hrAbbv + "-" + outwk.getAttendNo());
                        svf.VrsOut("NAME1", outwk._name);
                        prtRowCnt++;
                        bakGrHrCls = outwk.getGr_HrClass();
                        svf.VrEndRecord();
                    }
                }
            }

            _hasData = true;
        }
    }
    private int getPrintPageMax(final List outArryWk) {
        int pageCnt = 1;
        if (!"1".equals(_param._chgClass)) {
            pageCnt = (int)Math.ceil((double)outArryWk.size() / ((double)ONEPAGECOLUMNS * ONELINEMAXCNT));
        } else {
            int rowCnt = 1;
            int colCnt = 1;
            String bakGrHr = "";
            for (Iterator ite = outArryWk.iterator();ite.hasNext();) {
                Student getWk = (Student)ite.next();
                //改列判定
                if (!"1".equals(_param._chgClass)) {
                    if (rowCnt > ONELINEMAXCNT * ONEPAGECOLUMNS) {
                        rowCnt = 1;
                        colCnt = 1;
                        pageCnt++;
                    }
                } else {
                    if (rowCnt > ONELINEMAXCNT || (getWk != null && !"".equals(bakGrHr) && !bakGrHr.equals(getWk.getGr_HrClass()))) {
                        rowCnt = 1;
                        colCnt++;
                        if (colCnt > ONEPAGECOLUMNS) {
                            pageCnt++;
                            colCnt = 0;
                        }
                    }
                }
                rowCnt++;
                bakGrHr = getWk.getGr_HrClass();
            }
        }
        return pageCnt;
    }

    /**
     * 印鑑
     */
    private void printStamp(final Vrw32alp svf) {
        for (Iterator iterator = _param._stampData.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final StampData stampData = (StampData) _param._stampData.get(key);

            // 役職
            svf.VrsOut("JOB_NAME" + (Integer.parseInt(stampData._seq)), stampData._title);

            // 印鑑
            svf.VrsOut("STAMP" + (Integer.parseInt(stampData._seq)), _param.getStampImageFile(stampData._stampName));
        }
    }

    /**
     *  明細行で使用する生徒を取得
     * @param db2
     * @return
     */
    private Map setOutgoSchregDat(final DB2UDB db2) {
        final Map retMap = new TreeMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getOutGoSchregSql();
            log.debug("outgoSchregSQL =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            List addArry = new ArrayList();
            while (rs.next()) {
                final String reqestNo   = rs.getString("REQUEST_NO");
                final String lineNo     = rs.getString("LINE_NO");

                final String setKey = reqestNo + "-" + lineNo;

                final String grade_hr_attend = rs.getString("SORT");
                final String hrName     = rs.getString("HR_NAME");
                final String hrabbv     = rs.getString("HR_NAMEABBV");
                final String name       = rs.getString("NAME");

                //retMap.put(setKey, hrName + "　" + name);
                if (!retMap.containsKey(setKey)) {
                    addArry = new ArrayList();
                    retMap.put(setKey, addArry);
                }
                Student addWk = new Student(grade_hr_attend, hrName, hrabbv, name);
                addArry.add(addWk);
            }

        } catch (SQLException ex) {
            log.debug("OutGo_Schreg_Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private String getOutGoSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_SEME AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         YEAR, ");
        stb.append("         max(SEMESTER) AS MAX_SEMESTER ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        stb.append("         YEAR ");
        stb.append(" ), REGD_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO as SORT, ");
        stb.append("         HDAT.HR_NAME, ");
        stb.append("         HDAT.HR_NAMEABBV, ");
        stb.append("         BASE.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         INNER JOIN MAX_SEME T2 ON T2.SCHREGNO     = REGD.SCHREGNO ");
        stb.append("                               AND T2.YEAR         = REGD.YEAR ");
        stb.append("                               AND T2.MAX_SEMESTER = REGD.SEMESTER ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                                        AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("                                        AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                                        AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     O_SCH.REQUEST_NO, ");
        stb.append("     O_SCH.LINE_NO, ");
        stb.append("     REGD.HR_NAME, ");
        stb.append("     REGD.HR_NAMEABBV, ");
        stb.append("     REGD.SORT, ");
        stb.append("     REGD.NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT O_SCH ");
        stb.append("     LEFT JOIN REGD_DATA REGD ON REGD.SCHREGNO = O_SCH.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("         O_SCH.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND O_SCH.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND O_SCH.YEAR        = '" + _param._year + "' ");
        stb.append("     AND O_SCH.OUTGO_L_CD || O_SCH.OUTGO_M_CD = '" + _param._outgoLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND O_SCH.REQUEST_NO  = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     O_SCH.REQUEST_NO, ");
        stb.append("     O_SCH.LINE_NO, ");
        stb.append("     REGD.SORT ");
        return stb.toString();
    }

    private class Student {
        final String _grade_hr_attend;
        final String _hrName;
        final String _hrAbbv;
        final String _name;
        private Student(final String grade_hr_attend, final String hrName, final String hrAbbv, final String name) {
            _grade_hr_attend = grade_hr_attend;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _name = name;
        }
        public String retHr_Name() {
            return _hrName + "　" + _name;
        }
        public String getGr_HrClass() {
            return _grade_hr_attend.substring(0,5);
        }
        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_grade_hr_attend.substring(_grade_hr_attend.length() - 3,_grade_hr_attend.length())));
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getOutGoSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year                    = rs.getString("YEAR");
                final String outgoLcd                = rs.getString("OUTGO_L_CD");
                final String outgoLName              = rs.getString("LEVY_L_NAME");
                final String outgoMcd                = rs.getString("OUTGO_M_CD");
                final String outgoMName              = rs.getString("LEVY_M_NAME");
                final String requestNo               = rs.getString("REQUEST_NO");
                final String requestDate             = rs.getString("REQUEST_DATE");
                final String requestReason           = rs.getString("REQUEST_REASON");
                final String requestStaff            = rs.getString("REQUEST_STAFF");
                final String requestStaffName        = rs.getString("REQUEST_STAFF_NAME");
                final String requestGk               = rs.getString("REQUEST_GK");
                final String traderName              = rs.getString("TRADER_NAME");
                final String requestTesuuryou        = rs.getString("REQUEST_TESUURYOU");
                final String outgoCheck1             = rs.getString("OUTGO_CHECK1");
                final String outgoCheck1Date         = rs.getString("OUTGO_CHECK1_DATE");
                final String outgoCheck1Staff        = rs.getString("OUTGO_CHECK1_STAFF");
                final String outgoStaffName          = rs.getString("OUTGO_STAFF_NAME");
                final String outgoCheck2             = rs.getString("OUTGO_CHECK2");
                final String outgoCheck3             = rs.getString("OUTGO_CHECK3");
                final String outgoDate               = rs.getString("OUTGO_DATE");
                final String outgoExpenseFlg         = rs.getString("OUTGO_EXPENSE_FLG");
                final String outgoCertificateCnt     = rs.getString("OUTGO_CERTIFICATE_CNT");
                final String outgoCancel             = rs.getString("OUTGO_CANCEL");
                final String outgoApproval           = rs.getString("OUTGO_APPROVAL");
                final String kounyuNo                = rs.getString("KOUNYU_NO");
                final String sekouNo                 = rs.getString("SEKOU_NO");
                final String seisanNo                = rs.getString("SEISAN_NO");
                final String tesuuryoSummary         = rs.getString("TESUURYOU_SUMMARY");
                final String remark                  = rs.getString("REMARK");

                final OutGoDat outgoDat = new OutGoDat(db2, year, outgoLcd, outgoLName, outgoMcd, outgoMName, requestNo, requestDate,
                                                          requestReason, requestStaff, requestStaffName, requestGk, traderName, requestTesuuryou, outgoCheck1,
                                                          outgoCheck1Date, outgoCheck1Staff, outgoStaffName, outgoCheck2, outgoCheck3, outgoDate,
                                                          outgoExpenseFlg, outgoCertificateCnt, outgoCancel, outgoApproval, kounyuNo, sekouNo, seisanNo, tesuuryoSummary, remark);
                outgoDat.setMeisai(db2);
                retList.add(outgoDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getOutGoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     STF.STAFFNAME AS REQUEST_STAFF_NAME, ");
        stb.append("     STF2.STAFFNAME AS OUTGO_STAFF_NAME, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     L2.LEVY_M_NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON T1.REQUEST_STAFF = STF.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON T1.OUTGO_CHECK1_STAFF = STF2.STAFFCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.SCHOOLCD = L2.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '" + _param._outgoLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.OUTGO_L_CD, ");
        stb.append("     T1.OUTGO_M_CD, ");
        stb.append("     T1.REQUEST_NO ");
        return stb.toString();
    }

    private class OutGoDat {
        private final String _year;
        private final String _outgoLcd;
        private final String _outgoLName;
        private final String _outgoMcd;
        private final String _outgoMName;
        private final String _requestNo;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestStaff;
        private final String _requestStaffName;
        private final String _requestGk;
        private final String _traderName;
        private final String _requestTesuuryou;
        private final String _outgoCheck1;
        private final String _outgoCheck1Date;
        private final String _outgoCheck1Staff;
        private final String _outgoStaffName;
        private final String _outgoCheck2;
        private final String _outgoCheck3;
        private final String _outgoDate;
        private final String _outgoExpenseFlg;
        private final String _outgoCertificateCnt;
        private final String _outgoCancel;
        private final String _outgoApproval;
        private final String _kounyuNo;
        private final String _sekouNo;
        private final String _seisanNo;
        private final String _tesuuryoSummary;
        private final String _remark;
        private final List _meisai;

        public OutGoDat(
                final DB2UDB db2,
                final String year,
                final String outgoLcd,
                final String outgoLName,
                final String outgoMcd,
                final String outgoMName,
                final String requestNo,
                final String requestDate,
                final String requestReason,
                final String requestStaff,
                final String requestStaffName,
                final String requestGk,
                final String traderName,
                final String requestTesuuryou,
                final String outgoCheck1,
                final String outgoCheck1Date,
                final String outgoCheck1Staff,
                final String outgoStaffName,
                final String outgoCheck2,
                final String outgoCheck3,
                final String outgoDate,
                final String outgoExpenseFlg,
                final String outgoCertificateCnt,
                final String outgoCancel,
                final String outgoApproval,
                final String kounyuNo,
                final String sekouNo,
                final String seisanNo,
                final String tesuuryoSummary,
                final String remark
        ) {
            _year                   = year;
            _outgoLcd               = outgoLcd;
            _outgoLName             = outgoLName;
            _outgoMcd               = outgoMcd;
            _outgoMName             = outgoMName;
            _requestNo              = requestNo;
            _requestDate            = requestDate;
            _requestReason          = requestReason;
            _requestStaff           = requestStaff;
            _requestStaffName       = requestStaffName;
            _requestGk              = requestGk;
            _traderName             = traderName;
            _requestTesuuryou       = requestTesuuryou;
            _outgoCheck1            = outgoCheck1;
            _outgoCheck1Date        = outgoCheck1Date;
            _outgoCheck1Staff       = outgoCheck1Staff;
            _outgoStaffName         = outgoStaffName;
            _outgoCheck2            = outgoCheck2;
            _outgoCheck3            = outgoCheck3;
            _outgoDate              = outgoDate;
            _outgoExpenseFlg        = outgoExpenseFlg;
            _outgoCertificateCnt    = outgoCertificateCnt;
            _outgoCancel            = outgoCancel;
            _outgoApproval          = outgoApproval;
            _kounyuNo               = kounyuNo;
            _sekouNo                = sekouNo;
            _seisanNo               = seisanNo;
            _tesuuryoSummary        = tesuuryoSummary;
            _remark                 = remark;
            _meisai = new ArrayList();
        }

        private void setMeisai(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getMesaiSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String lineNo         = rs.getString("LINE_NO");
                    final String commodityName  = rs.getString("LEVY_S_NAME");
                    final String commodityPrice = rs.getString("COMMODITY_PRICE");
                    final String commodityCnt   = rs.getString("COMMODITY_CNT");
                    final String totalPrice     = rs.getString("TOTAL_PRICE_ZEINUKI");
                    final String totalTax       = rs.getString("TOTAL_TAX");
                    final String schPrice       = rs.getString("SCH_PRICE");
                    final String schCnt         = rs.getString("SCH_CNT");
                    final String hasuu          = rs.getString("HASUU");
                    final String remark         = rs.getString("REMARK");

                    final OutGoMeisai outgoMeisai = new OutGoMeisai(lineNo, commodityName, commodityPrice, commodityCnt, totalPrice, totalTax, schPrice, schCnt, hasuu, remark);
                    _meisai.add(outgoMeisai);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getMesaiSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     L1.LEVY_S_NAME ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_S_MST L1 ON T1.SCHOOLCD    = L1.SCHOOLCD ");
            stb.append("                            AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("                            AND T1.YEAR        = L1.YEAR ");
            stb.append("                            AND T1.OUTGO_L_CD  = L1.LEVY_L_CD ");
            stb.append("                            AND T1.OUTGO_M_CD  = L1.LEVY_M_CD ");
            stb.append("                            AND T1.OUTGO_S_CD  = L1.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.OUTGO_L_CD = '" + _outgoLcd + "' ");
            stb.append("     AND T1.OUTGO_M_CD = '" + _outgoMcd + "' ");
            stb.append("     AND T1.REQUEST_NO = '" + _requestNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.LINE_NO ");
            return stb.toString();
        }
    }

    private class OutGoMeisai {
        private final String _lineNo;
        private final String _commodityName;
        private final String _commodityPrice;
        private final String _commodityCnt;
        private final String _totalPrice;
        private final String _totalTax;
        private final String _schPrice;
        private final String _schCnt;
        private final String _hasuu;
        private final String _remark;
        public OutGoMeisai(
                final String lineNo,
                final String commodityName,
                final String commodityPrice,
                final String commodityCnt,
                final String totalPrice,
                final String totalTax,
                final String schPrice,
                final String schCnt,
                final String hasuu,
                final String remark
        ) {
            _lineNo         = lineNo;
            _commodityName  = commodityName;
            _commodityPrice = commodityPrice;
            _commodityCnt   = commodityCnt;
            _totalPrice     = totalPrice;
            _totalTax       = totalTax;
            _schPrice       = schPrice;
            _schCnt         = schCnt;
            _hasuu          = hasuu;
            _remark         = remark;
        }
    }

    private class StampData {
        private final String _seq;
        private final String _title;
        private final String _stampName;
        public StampData(
                final String seq,
                final String title,
                final String stampName
                ) {
            _seq       = seq;
            _title     = title;
            _stampName = stampName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73404 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _outgoLMcd;
        private final String _requestNo;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        private final String _chgClass;
        private final String _KNJP952_PrintStdNameMeisai;
        private final String _KNJP952_PrintStdCntMeisai;
        private final String _useLevyIraisyo;
        private final String _titleTesuuryo;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        private final Map _stampData;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _outgoLMcd = request.getParameter("OUTGO_L_M_CD");
            _requestNo = request.getParameter("REQUEST_NO");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _chgClass = request.getParameter("CHANGE_CLASS");
            _KNJP952_PrintStdNameMeisai = request.getParameter("KNJP952_PrintStdNameMeisai");
            _KNJP952_PrintStdCntMeisai = request.getParameter("KNJP952_PrintStdCntMeisai");
            _useLevyIraisyo = request.getParameter("useLevyIraisyo");
            _schoolName = getSchoolName(db2, _year);
            _imageDir = "image/stamp";
            _imageExt = "bmp";
            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _stampData = getStampData(db2);
            _titleTesuuryo = getTesuryo(db2);
        }

        private String getTesuryo(final DB2UDB db2) {
            String retName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'P008' AND NAMECD2 = '001' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retName = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getP008 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
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

        private Map getStampData(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            PreparedStatement psStampNo = null;
            ResultSet rsStampNo = null;
            final String stamPSql = getStampData();
            try {
                ps = db2.prepareStatement(stamPSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String stampNoSql = getMaxStampNo(rs.getString("FILE_NAME"));
                    psStampNo = db2.prepareStatement(stampNoSql);
                    rsStampNo = psStampNo.executeQuery();
                    rsStampNo.next();
                    final String stampNo = rsStampNo.getString("STAMP_NO");
                    final StampData stampData = new StampData(rs.getString("SEQ"), rs.getString("TITLE"), stampNo);
                    retMap.put(rs.getString("SEQ"), stampData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(null, psStampNo, rsStampNo);
                db2.commit();
            }
            return retMap;
        }

        private String getStampData() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     FILE_NAME, ");
            stb.append("     TITLE ");
            stb.append(" FROM ");
            stb.append("     PRG_STAMP_DAT ");
            stb.append(" WHERE ");
            stb.append("         YEAR        = '" + _year + "' ");
            stb.append("     AND SEMESTER    = '9' ");
            stb.append("     AND SCHOOLCD    = '" + _schoolCd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND PROGRAMID   = 'KNJP952' ");
            return stb.toString();
        }

        private String getMaxStampNo(final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     STAFFCD = '" + staffcd + "' ");

            return stb.toString();
        }

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String filename) {
            if (null == filename) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

    }
}

// eof

