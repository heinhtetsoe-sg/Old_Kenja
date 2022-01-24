/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id$
 *
 * 作成日: 2018/10/25
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

public class KNJP907 {

    private static final Log log = LogFactory.getLog(KNJP907.class);

    private static final int DETAILPAGEMAXLINE = 50;
    private static final int LCDPAGEMAXLINE = 38;
    private static final int BUDGETPAGE_MAXLINE = 50;
    private static final String FORMNAME_KNJP907_3 = "KNJP907_3.frm";
    private static final String TOTAL_SCD = "000";
    private static final String YOBIHI_CD = "AAA";
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
        for (int ii = 0;ii < _param._categorySelected.length;ii++) {
            Student student = getPrintInfo(db2, _param._categorySelected[ii]);

            student._maxpagecnt = printPageCnt1(svf, student) + printPageCnt2(svf, student);

            //1枚目（表）
            printLcdInfo(db2, svf, student);

            //2枚目（裏）
            if ("1".equals(_param._useLevyBudget)) {
                printDetailInfoBudget(svf, student);
            } else {
                printDetailInfo(svf, student);
            }

        }

        _hasData = true;
    }

    private int printPageCnt1(final Vrw32alp svf, final Student student){
        int retval = 0;
        retval = roundup(student._inComeLcdList.size() + 1 + student._outGoingLcdList.size() + 1 + 1 + 4, LCDPAGEMAXLINE);
        return retval;
    }

    private int printPageCnt2(final Vrw32alp svf, final Student student){
        int retval = 0;
        retval = student.getPrintPageCnt();
        return retval;
    }

    private int roundup(final int val, final int div_base) {
        BigDecimal bd1 = new BigDecimal(val);
        BigDecimal bd2 = new BigDecimal(div_base);
        return bd1.divide(bd2, 0, BigDecimal.ROUND_UP).intValue();
    }

    /** 1枚目（表） */
    private void printLcdInfo(final DB2UDB db2, final Vrw32alp svf, final Student student){
        final String setForm = ("1".equals(_param._address)) ? "KNJP907_ADDR.frm": "KNJP907.frm";
        svf.VrSetForm(setForm, 4);
        int pageCnt = 1;
        svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
        long income_total = 0;
        long outgoing_total = 0;

        if ("1".equals(_param._address)) {
            //住所
            svf.VrsOut("ZIPNO", student._addr._zipCd);
            final String addIdx1 = KNJ_EditEdit.getMS932ByteLength(student._addr._addr1) > 50 ? "4": KNJ_EditEdit.getMS932ByteLength(student._addr._addr1) > 40 ? "3": KNJ_EditEdit.getMS932ByteLength(student._addr._addr1) > 30 ? "2": "1";
            svf.VrsOut("ADDRESS1_" + addIdx1, student._addr._addr1);
            final String addIdx2 = KNJ_EditEdit.getMS932ByteLength(student._addr._addr2) > 50 ? "4": KNJ_EditEdit.getMS932ByteLength(student._addr._addr2) > 40 ? "3": KNJ_EditEdit.getMS932ByteLength(student._addr._addr2) > 30 ? "2": "1";
            svf.VrsOut("ADDRESS2_" + addIdx2, student._addr._addr2);

            svf.VrsOut("HR_NAME", _param._hrName);

            final String setName = student._name + "　保護者様";
            final String nameIdx = KNJ_EditEdit.getMS932ByteLength(setName) > 44 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 34 ? "2": "1";
            svf.VrsOut("NAME1_" + nameIdx, setName);

            //学校名
            svf.VrsOut("SCHOOLNAME", _param._certifSchool._schoolName);
            //職名
            svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);
            //校長名
            svf.VrsOut("STAFF_NAME1", _param._certifSchool._principalName);
        } else {
            //生徒名
            final String setName = _param._hrName + "　" + student._name + "　保護者様";
            svf.VrsOut("HR_NAME", setName);
        }

        //日付
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._printDate, "/", "-")));
        //svf.VrsOut("PAY_DATE1", _param._printlimitdate.substring(0, 2) + "月" + _param._printlimitdate.substring(2) + "日");

        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
        //校長名
        svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);

        //給付対象使用時
        //収入①
        for (final Iterator iti = student._inComeLcdList.iterator(); iti.hasNext();) {
            InComeInfo i_info = (InComeInfo) iti.next();
            income_total += i_info._incomeMoney;
        }
        //支出②
        for (final Iterator ito = student._outGoingLcdList.iterator(); ito.hasNext();) {
            OutGoingInfo o_info = (OutGoingInfo) ito.next();
            outgoing_total += o_info._outGoingMoney;
        }

        long money1 = 0;
        long money2 = 0;
        if ("1".equals(_param._useBenefit)) {
            final long inout_total = income_total - outgoing_total;
            final int kyufuTotal = getKyufuTotal(db2, student);
            money1 = (inout_total + kyufuTotal);
        } else {
            final long inout_total = income_total - outgoing_total;
            money1 = (inout_total);
        }
        money2 = money1 - Integer.valueOf(StringUtils.defaultIfEmpty(_param._tesuryo, "0"));

        //文章
        String formatted = new DecimalFormat("###,###,###,###").format(money1);
        String writeDoc = StringUtils.replace(_param._writeDoc, "MONEY1", formatted);
        formatted = new DecimalFormat("###,###,###,###").format(money2);
        writeDoc = StringUtils.replace(writeDoc, "MONEY2", formatted);
        final String setIdouDate = KNJ_EditDate.h_format_JP(db2, _param._idouDate);
        writeDoc = StringUtils.replace(writeDoc, "DATE1", setIdouDate);
        String[] token = KNJ_EditEdit.get_token(writeDoc, 90, 15);
        if (token != null) {
            for (int kk = 0;kk < token.length;kk++) {
                svf.VrsOutn("TEXT", kk + 1, token[kk]);
            }
        }

        income_total = 0;
        outgoing_total = 0;
        //収入①
        String grpstr1 = "1";
        String grpstr2 = "1";
        svf.VrsOut("INOUT_NAME1", "収入");
        svf.VrsOut("PAY_TITLE1", "給付");
        int incomelinesubcnt = 0;
        for (final Iterator iti = student._inComeLcdList.iterator(); iti.hasNext();) {
            InComeInfo i_info = (InComeInfo) iti.next();
            if (incomelinesubcnt >= LCDPAGEMAXLINE) {
                svf.VrEndPage();
                svf.VrSetForm("KNJP907.frm", 4);
                pageCnt++;
                svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                incomelinesubcnt = 0;
            }
            setgrpstr(svf, "1", grpstr1, grpstr2);
            svf.VrsOut("SUMMARY1", i_info._levyName);
            svf.VrsOut("MONEY1", String.valueOf(i_info._incomeMoney));
            incomelinesubcnt++;
            income_total += i_info._incomeMoney;
            svf.VrEndRecord();
        }

        svf.VrsOut("TOTAL_NAME1", "①合計");
        svf.VrsOut("MONEY2", String.valueOf(income_total));
        svf.VrEndRecord();

        if (incomelinesubcnt+1 >= LCDPAGEMAXLINE) {
            svf.VrEndPage();
            svf.VrSetForm("KNJP907.frm", 4);
            pageCnt++;
            svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
            incomelinesubcnt = 0;
        }
        //支出②
        grpstr1 = "1";
        grpstr2 = "1";
        svf.VrsOut("INOUT_NAME2", "支出");
        int outgoinglinesubcnt = 0;
        for (final Iterator ito = student._outGoingLcdList.iterator(); ito.hasNext();) {
            OutGoingInfo o_info = (OutGoingInfo) ito.next();
            if (incomelinesubcnt + outgoinglinesubcnt >= LCDPAGEMAXLINE) {
                svf.VrEndPage();
                svf.VrSetForm("KNJP907.frm", 4);
                pageCnt++;
                svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                outgoinglinesubcnt = 0;
                incomelinesubcnt = 0;
            }
            setgrpstr(svf, "2", grpstr1, grpstr2);
            svf.VrsOut("SUMMARY2", o_info._levyName);
            svf.VrsOut("MONEY3", String.valueOf(o_info._outGoingMoney));
            outgoing_total += o_info._outGoingMoney;
            outgoinglinesubcnt++;
            svf.VrEndRecord();
        }
        svf.VrsOut("TOTAL_NAME2", "②合計");
        svf.VrsOut("MONEY4", String.valueOf(outgoing_total));
        svf.VrEndRecord();
        if (incomelinesubcnt + outgoinglinesubcnt+1 >= LCDPAGEMAXLINE) {
            svf.VrEndPage();
            svf.VrSetForm("KNJP907.frm", 4);
            pageCnt++;
            svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
            outgoinglinesubcnt = 0;
            incomelinesubcnt = 0;
        }

        //給付対象使用時
        if ("1".equals(_param._useBenefit)) {
            //給付額
            int kyufuTotal = getKyufuTotal(db2, student);
            if (kyufuTotal > 0) {
                svf.VrsOut("INOUT_NAME6", "給付額");
                svf.VrsOut("INOUT_NAME6_2", "東京都立学校等給付型奨学金");
                svf.VrsOut("INOUT_NAME6_3", "③合計");
                DecimalFormat dformat = new DecimalFormat();
                dformat.applyPattern("###,###,###,###,###");
                svf.VrsOut("MONEY8", String.valueOf(kyufuTotal));
                svf.VrEndRecord();

                //返金額(③+④)
                svf.VrsOut("INOUT_NAME6", "返金額");
                svf.VrsOut("INOUT_NAME6_2", "① + ③ - ②");
                final long inout_total = income_total - outgoing_total;
                svf.VrsOut("MONEY8_2", String.valueOf(inout_total + kyufuTotal));
            } else {
                //返金額(①-②)
                svf.VrsOut("INOUT_NAME6", "返金額");
                svf.VrsOut("INOUT_NAME6_2", "① - ②");
                final long inout_total = income_total - outgoing_total;
                svf.VrsOut("MONEY8_2", String.valueOf(inout_total));
            }
        } else {
            //返金額(①-②)
            svf.VrsOut("INOUT_NAME6", "返金額");
            svf.VrsOut("INOUT_NAME6_2", "① - ②");
            final long inout_total = income_total - outgoing_total;
            svf.VrsOut("MONEY8_2", String.valueOf(inout_total));
        }
        svf.VrEndRecord();

        svf.VrsOut("COMMENT", "※ 明細は裏面のとおり");
        svf.VrEndRecord();

        svf.VrEndPage();
    }

    private int getKyufuTotal(final DB2UDB db2, final Student student) {
        int retval = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getKyufuSql(student._schregno);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                retval = rs.getInt("KYUFU_TOTAL");
            }

        } catch (SQLException ex) {
            log.debug("ExceptionKyufu:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retval;
    }

    private String getKyufuSql(final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     sum(OSCH.OUTGO_MONEY) as KYUFU_TOTAL ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                          AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append(" WHERE ");
        stb.append("         OSCH.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("     AND OSCH.YEAR        = '" + _param._year + "' ");
        stb.append("     AND OSCH.SCHREGNO    = '" + schregNo + "' ");
        stb.append("     AND OUTG.INCOME_L_CD = '98' ");
        stb.append("     AND OUTG.INCOME_M_CD = '98' ");

        return stb.toString();
    }

    /** 2枚目（裏面） */
    private void printDetailInfo(final Vrw32alp svf, final Student student){
        svf.VrSetForm("KNJP907_2.frm", 4);
        int pageCnt = printPageCnt1(svf, student)+1;
        svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);

        //生徒名
        final String setName = _param._hrName + "　" + student._name + "　保護者様";
        svf.VrsOut("HR_NAME", setName);
        //日付
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._printDate, "/", "-")));
        //svf.VrsOut("PAY_DATE1", _param._printlimitdate.substring(0, 2) + "月" + _param._printlimitdate.substring(2) + "日");

        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
        //校長名
        svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);

        int incomelinesubcnt = 0;
        int outgoinglinesubcnt = 0;
        for (final Iterator itm = student._mergeKeyMap.keySet().iterator(); itm.hasNext();) {
            long income_total = 0;
            long outcome_total = 0;
            String mkey = (String)itm.next();
            //収入①
            String grpstr1 = "1";
            String grpstr2 = "1";
            svf.VrsOut("INOUT_NAME1", "収入");
            incomelinesubcnt++;
            List inclist = (List)student._inComeMap.get(mkey);
            List outglist = (List)student._outGoingMap.get(mkey);

            if (inclist != null) {
                InComeDetailInfo i_info = (InComeDetailInfo)inclist.get(0);
                svf.VrsOut("L_NAME_2", i_info._levyLName);
            } else if (outglist != null) {
                OutGoingDetailInfo o_info = (OutGoingDetailInfo)outglist.get(0);
                svf.VrsOut("L_NAME_2", o_info._levyLName);
            }
            if (inclist != null) {
                for (final Iterator iti = inclist.iterator(); iti.hasNext();) {
                    InComeDetailInfo i_info = (InComeDetailInfo) iti.next();
                    if (incomelinesubcnt >= DETAILPAGEMAXLINE) {
                        svf.VrEndPage();
                        svf.VrSetForm("KNJP907_2.frm", 4);
                        pageCnt++;
                        svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                        incomelinesubcnt = 0;
                    }
                    setgrpstr(svf, "1", grpstr1, grpstr2);
                    svf.VrsOut("SUMMARY1", i_info._levyMName);
                    svf.VrsOut("MONEY1", String.valueOf(i_info._incomeMoney));
                    incomelinesubcnt++;
                    income_total += i_info._incomeMoney;
                    svf.VrEndRecord();
                }
            } else {
                setgrpstr(svf, "1", grpstr1, grpstr2);
                incomelinesubcnt++;
                svf.VrEndRecord();
            }
            svf.VrsOut("TOTAL_NAME1", "①合計");
            svf.VrsOut("MONEY2", String.valueOf(income_total));
            incomelinesubcnt++;
            svf.VrEndRecord();
            if (incomelinesubcnt + outgoinglinesubcnt+1 >= DETAILPAGEMAXLINE) {
                svf.VrEndPage();
                svf.VrSetForm("KNJP907_2.frm", 4);
                pageCnt++;
                svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                incomelinesubcnt = 0;
            }

            //支出②
            grpstr1 = "2";
            grpstr2 = "2";
            svf.VrsOut("INOUT_NAME1", "支出");
            //List outglist = (List)student._outGoingMap.get(mkey);
            if (outglist != null) {
                for (final Iterator ito = outglist.iterator(); ito.hasNext();) {
                    OutGoingDetailInfo o_info = (OutGoingDetailInfo) ito.next();
                    if (incomelinesubcnt + outgoinglinesubcnt >= DETAILPAGEMAXLINE) {
                        svf.VrEndPage();
                        svf.VrSetForm("KNJP907_2.frm", 4);
                        pageCnt++;
                        svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                        outgoinglinesubcnt = 0;
                        incomelinesubcnt = 0;
                    }
                    setgrpstr(svf, "1", grpstr1, grpstr2);
                    svf.VrsOut("SUMMARY1", o_info._levySName);
                    svf.VrsOut("MONEY1", String.valueOf(o_info._outGoingMoney));
                    outcome_total += o_info._outGoingMoney;
                    outgoinglinesubcnt++;
                    svf.VrEndRecord();
                }
            } else {
                setgrpstr(svf, "1", grpstr1, grpstr2);
                outgoinglinesubcnt++;
                svf.VrEndRecord();
            }
            svf.VrsOut("TOTAL_NAME3", "②合計");
            svf.VrsOut("MONEY5", String.valueOf(outcome_total));
            outgoinglinesubcnt++;
            svf.VrEndRecord();

            if (incomelinesubcnt + outgoinglinesubcnt+1 >= DETAILPAGEMAXLINE) {
                svf.VrEndPage();
                if (itm.hasNext()) {
                    svf.VrSetForm("KNJP907_2.frm", 4);
                    pageCnt++;
                    svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                    outgoinglinesubcnt = 0;
                    incomelinesubcnt = 0;
                }
            } else {
                svf.VrsOut("BLANK", "a");
                outgoinglinesubcnt++;
                svf.VrEndRecord();
            }

            //次に出力する表が中途半端に切れる場合は、改ページする
            String nextCnt = (String)student._mergeKeyMap.get(mkey);
            if (incomelinesubcnt + outgoinglinesubcnt + Integer.parseInt(nextCnt)-1 > DETAILPAGEMAXLINE) {
                svf.VrEndPage();
                svf.VrSetForm("KNJP907_2.frm", 4);
                pageCnt++;
                svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
                outgoinglinesubcnt = 0;
                incomelinesubcnt = 0;
            }
        }
    }

    /*
     * 予算実行額プロパティ使用時の詳細ページ
     */
    private void printDetailInfoBudget(final Vrw32alp svf, final Student student) {
        svf.VrSetForm(FORMNAME_KNJP907_3, 4);
        int pageCnt = printPageCnt1(svf, student)+1;
        svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);

        //生徒名
        final String setName = _param._hrName + "　" + student._name + "　保護者様";
        svf.VrsOut("HR_NAME", setName);
        //日付
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._printDate, "/", "-")));
        //svf.VrsOut("PAY_DATE1", _param._printlimitdate.substring(0, 2) + "月" + _param._printlimitdate.substring(2) + "日");

        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
        //校長名
        svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);

        //科目毎（L_CD）に印刷
        int lineCnt = 0;
        String befLcd = "";
        for (final Iterator itm = student._outBudgetMap.keySet().iterator(); itm.hasNext();) {
            final String keyLcd = (String) itm.next();
            List printList  = (List) student._outBudgetMap.get(keyLcd);

            //空白行
            if (!"".equals(befLcd) && !befLcd.equals(keyLcd) && lineCnt != 0) {
                svf.VrsOut("BLANK", "a");
                lineCnt++;
                svf.VrEndRecord();

                //改ページ
                if (lineCnt >= BUDGETPAGE_MAXLINE) {
                    pageCnt++;
                    setNextForm(svf, student, pageCnt);
                    lineCnt = 0;
                }
            }

            //科目名(L_NAME)
            final String lName = (String) _param._levyLMstMap.get(keyLcd);
            svf.VrsOut("L_NAME", lName);
            lineCnt++;
            svf.VrEndRecord();

            //改ページ
            if (lineCnt >= BUDGETPAGE_MAXLINE) {
                pageCnt++;
                setNextForm(svf, student, pageCnt);
                lineCnt = 0;
            }

            int totalBudget = 0;
            int totalOutgo  = 0;
            String befGpCd2 = "";
            for (final Iterator ito = printList.iterator(); ito.hasNext();) {
                final OutBudgetInfo ob = (OutBudgetInfo) ito.next();
                final String lmCd = StringUtils.substring(ob._outGoLMScd, 0, 4);
                final String sCd = StringUtils.substring(ob._outGoLMScd, 4);
                final String budgetScd = StringUtils.substring(ob._budgetLMScd, 4);

                //改ページ
                if (lineCnt >= BUDGETPAGE_MAXLINE) {
                    pageCnt++;
                    setNextForm(svf, student, pageCnt);
                    lineCnt = 0;
                }

                final String gpCd2 = !"".equals(budgetScd) ? budgetScd: sCd;

                if (TOTAL_SCD.equals(sCd)) {
                    //項目名(M_NAME)
                    final String mName = (String) _param._levyMMstMap.get(lmCd);
                    svf.VrsOut("INOUT_NAME1", mName);
                    //予算
                    svf.VrsOut("BADGET1", String.valueOf(ob._budgetMoney));
                    //実績
                    svf.VrsOut("MONEY1", String.valueOf(ob._outGoMoney));

                    totalBudget += ob._budgetMoney;
                    totalOutgo  += ob._outGoMoney;
                } else {
                    //実績CDと、使用予算のCDが不一致の時は、"XXXの代替として"の文言を追加する
                    boolean printFlg = true;
                    if (!"".equals(ob._budgetLMScd) && !ob._outGoLMScd.equals(ob._budgetLMScd)) {
                        final String sName = (YOBIHI_CD.equals(budgetScd)) ? "予備費": (String) _param._levySMstMap.get(ob._budgetLMScd);
                        final String setSName = sName + "の代替として";
                        svf.VrsOut("SUMMARY1", setSName);
                        svf.VrsOut("GRP1", sCd);
                        svf.VrsOut("GRP2", gpCd2);
                        svf.VrsOut("GRP3", sCd);
                        //予算
                        if (!befGpCd2.equals(gpCd2)) {
                            final String budgetMoney = (_param._notPriBudgetMap.containsKey(lmCd)) ? "": String.valueOf(ob._budgetMoney);
                            svf.VrsOut("BADGET2", budgetMoney);
                        }
                        //実績
                        svf.VrsOut("MONEY2", String.valueOf(ob._outGoMoney));
                        printFlg = false;
                        lineCnt++;
                        svf.VrEndRecord();
                    }

                    //細目名(S_NAME)
                    final String sName = (String) _param._levySMstMap.get(ob._outGoLMScd);
                    svf.VrsOut("SUMMARY1", sName);
                    svf.VrsOut("GRP1", sCd);
                    svf.VrsOut("GRP2", gpCd2);
                    svf.VrsOut("GRP3", sCd);

                    if (printFlg) {
                        //予算
                        if (!befGpCd2.equals(gpCd2)) {
                            final String budgetMoney = (_param._notPriBudgetMap.containsKey(lmCd)) ? "": String.valueOf(ob._budgetMoney);
                            svf.VrsOut("BADGET2", budgetMoney);
                        }
                        //実績
                        svf.VrsOut("MONEY2", String.valueOf(ob._outGoMoney));
                    }

                }

                befGpCd2 = !"".equals(budgetScd) ? budgetScd: sCd;
                lineCnt++;
                svf.VrEndRecord();
            }

            //合計欄
            svf.VrsOut("INOUT_NAME2", "合計");
            svf.VrsOut("BADGET3", String.valueOf(totalBudget)); //予算
            svf.VrsOut("MONEY3", String.valueOf(totalOutgo)); //実績
            lineCnt++;
            svf.VrEndRecord();

            //改ページ
            if (lineCnt >= BUDGETPAGE_MAXLINE) {
                pageCnt++;
                setNextForm(svf, student, pageCnt);
                lineCnt = 0;
            }

            //差額欄
            svf.VrsOut("INOUT_NAME2", "差額");
            svf.VrsOut("MONEY3", String.valueOf(totalBudget - totalOutgo));
            lineCnt++;
            svf.VrEndRecord();

            totalBudget = 0;
            totalOutgo = 0;

            if (lineCnt + 1 > BUDGETPAGE_MAXLINE) {
                svf.VrEndPage();
                if (itm.hasNext()) {
                    pageCnt++;
                    setNextForm(svf, student, pageCnt);
                    lineCnt = 0;
                }
            }

            befLcd = keyLcd;
        }
    }

    /** 改ページ */
    private void setNextForm(final Vrw32alp svf, final Student student, int pageCnt) {
        svf.VrSetForm(FORMNAME_KNJP907_3, 4);
        svf.VrsOut("PAGE",  pageCnt + "/" + student._maxpagecnt);
    }

    private void setgrpstr(final Vrw32alp svf, final String inoutflg, final String grpstr1, final String grpstr2) {
        if ("1".equals(inoutflg)) {
            svf.VrsOut("GRP1_1", grpstr1);
            svf.VrsOut("GRP1_2", grpstr2);
            svf.VrsOut("GRP1_3", grpstr2);
            svf.VrsOut("GRP1_4", grpstr1);
        } else {
            svf.VrsOut("GRP2_1", grpstr1);
            svf.VrsOut("GRP2_2", grpstr2);
            svf.VrsOut("GRP2_3", grpstr2);
            svf.VrsOut("GRP2_4", grpstr1);
        }
    }
    private Student getPrintInfo(final DB2UDB db2, final String schregno) {
        Student ctrlStudent = new Student(schregno, getSchregName(db2, schregno), getAddr(db2, schregno));
        ctrlStudent.setIncomeLcdList(getInComeLcdData(db2, schregno));
        ctrlStudent.setOutGoingLcdList(getOutGoingLcdData(db2, schregno));
        ctrlStudent.setIncomeMap(getInComeData(db2, schregno));
        ctrlStudent.setOutGoingMap(getOutGoingData(db2, schregno));
        ctrlStudent.setMergeKeyMap();
        if ("1".equals(_param._useLevyBudget)) {
        	ctrlStudent.setOutBudgetMap(getOutBudgetData(db2, schregno));
        }
        return ctrlStudent;
    }

    private class Student {
        final String _schregno;
        final String _name;
        final AddrInfo _addr;
        List _inComeLcdList;
        List _outGoingLcdList;
        Map _inComeMap;
        //List _inComeList;
        Map _outGoingMap;
        //List _outGoingList;
        Map _mergeKeyMap;
        Map _outBudgetMap;
        int _maxpagecnt;
        Student(final String schregno, final String name, final AddrInfo addr) {
            _schregno       = schregno;
            _name           = name;
            _addr           = addr;
            _maxpagecnt     = 0;
        }
        private void setIncomeLcdList(List inComeList) {
            _inComeLcdList = inComeList;
        }
        private void setOutGoingLcdList(List outGoingList) {
            _outGoingLcdList = outGoingList;
        }
        private void setIncomeMap(Map inComeMap) {
            _inComeMap = inComeMap;
        }
        private void setOutGoingMap(Map outGoingMap) {
            _outGoingMap = outGoingMap;
        }
        private void setOutBudgetMap(Map outBudgetMap) {
            _outBudgetMap = outBudgetMap;
        }
        private void setMergeKeyMap() {
            _mergeKeyMap = new TreeMap();
            //incomeのLCDを登録
            for (final Iterator iti = _inComeMap.keySet().iterator(); iti.hasNext();) {
                String ikey = (String)iti.next();
                List inclist = (List)_inComeMap.get(ikey);
                int listcnt = inclist.size();
                _mergeKeyMap.put(ikey, String.valueOf(listcnt));
            }
            //outgoingのLCDを登録
            for (final Iterator ito = _outGoingMap.keySet().iterator(); ito.hasNext();) {
                String okey = (String)ito.next();
                List outdlist = (List)_outGoingMap.get(okey);
                int listcnt = outdlist.size();
                if (_mergeKeyMap.containsKey(okey)) {
                    String cnt = (String)_mergeKeyMap.get(okey);
                    listcnt += Integer.parseInt(cnt);
                }
                _mergeKeyMap.put(okey, String.valueOf(listcnt));
            }
            //マージしたリストで、LCD毎(つまり表毎)の利用行数を算出する。
            for (final Iterator itm = _mergeKeyMap.keySet().iterator(); itm.hasNext();) {
                String mkey = (String)itm.next();
                String cnt = (String) _mergeKeyMap.get(mkey);
                int incnotcnt = _inComeMap.containsKey(mkey) ? 0 : 1;
                int outgnotcnt = _outGoingMap.containsKey(mkey) ? 0 : 1;
                int totalline = Integer.parseInt(cnt) + 1 + 2 + 1 + incnotcnt + outgnotcnt; //出力行数 + head + 合計2行 + 空白行
                _mergeKeyMap.put(mkey, String.valueOf(totalline));
            }
        }
        private int getPrintPageCnt() {
            int retpagecnt = 1;
            int totalinpageline = 0;
            //予算・実績管使用プロパティ
            if ("1".equals(_param._useLevyBudget)) {
                if (_outBudgetMap.isEmpty()) {
                    return 0;
                }
                String befLcd = "";
                for (final Iterator itm = _outBudgetMap.keySet().iterator(); itm.hasNext();) {
                    String keyLcd = (String)itm.next();
                    totalinpageline += 3; //科目名欄、合計欄、差額欄
                    totalinpageline += (!"".equals(befLcd) && !befLcd.equals(keyLcd)) ? 1: 0; //空白

                    List printList  = (List) _outBudgetMap.get(keyLcd);

                    for (final Iterator ito = printList.iterator(); ito.hasNext();) {
                        final OutBudgetInfo ob = (OutBudgetInfo) ito.next();

                        if (totalinpageline > BUDGETPAGE_MAXLINE) {
                            retpagecnt++;
                            totalinpageline = 0;
                        }

                        if (!"".equals(ob._budgetLMScd) && !ob._outGoLMScd.equals(ob._budgetLMScd)) {
                            totalinpageline++;
                            if (totalinpageline > BUDGETPAGE_MAXLINE) {
                                retpagecnt++;
                                totalinpageline = 0;
                            }
                        }

                        totalinpageline++;
                    }
                    befLcd = keyLcd;
                }
            } else {
                if (_mergeKeyMap.isEmpty()) {
                    return 0;
                }
                for (final Iterator itm = _mergeKeyMap.keySet().iterator(); itm.hasNext();) {
                    String mkey = (String)itm.next();
                    String cnt = (String) _mergeKeyMap.get(mkey);
                    totalinpageline += Integer.parseInt(cnt); //出力行数 + head + 合計2行 + 空白行
                    if (totalinpageline - 1 > DETAILPAGEMAXLINE) {
                        retpagecnt += roundup(totalinpageline, DETAILPAGEMAXLINE)-1;
                        totalinpageline = 0;
                    }
                }
            }
            return retpagecnt;
        }
    }

    private String getSchregName(final DB2UDB db2, final String schregno) {
        String retstr = "";
        String sql = "SELECT NAME FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + schregno + "' ";
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                retstr = rs.getString("NAME");
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retstr;
    }

    /** 住所取得 */
    private AddrInfo getAddr(final DB2UDB db2, final String schregno) {
        AddrInfo retAddr = new AddrInfo(null, null, null);

        //（1:生徒 2:保護者）
        final String table = ("1".equals(_param._addrDiv)) ? "SCHREG_ADDRESS_DAT": "GUARDIAN_ADDRESS_DAT";
        final String guStr = ("1".equals(_param._addrDiv)) ? "": "GUARD_";

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     value(A1." + guStr + "ZIPCD, '') as ZIPCD, ");
        stb.append("     value(A1." + guStr + "ADDR1, '') as ADDR1, ");
        stb.append("     value(A1." + guStr + "ADDR2, '') as ADDR2 ");
        stb.append(" FROM ");
        stb.append("     " + table + " A1 ");
        stb.append("     INNER JOIN ( ");
        stb.append("         SELECT ");
        stb.append("             SCHREGNO, ");
        stb.append("             MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("         FROM ");
        stb.append("             " + table + " ");
        stb.append("         WHERE ");
        stb.append("             SCHREGNO = '" + schregno + "' ");
        stb.append("         GROUP BY ");
        stb.append("             SCHREGNO ");
        stb.append("         ) A2 ON  A2.SCHREGNO  = A1.SCHREGNO ");
        stb.append("              AND A2.ISSUEDATE = A1.ISSUEDATE ");

        try {
            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                retAddr = new AddrInfo(rs.getString("ZIPCD"), rs.getString("ADDR1"), rs.getString("ADDR2"));
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retAddr;
    }

    private class AddrInfo {
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        AddrInfo(final String zipCd, final String addr1, final String addr2) {
            _zipCd = zipCd;
            _addr1 = addr1;
            _addr2 = addr2;
        }
    }

    private class InComeDetailInfo {
        final String _levyLName;
        final String _levyLCd;
        final String _levyMName;
        final long _incomeMoney;
        InComeDetailInfo(final String levyLCd, final String levyLName, final String levyMName, final long incomeMoney) {
            _levyLName = levyLName;
            _levyLCd = levyLCd;
            _levyMName = levyMName;
            _incomeMoney = incomeMoney;
        }
    }

    private Map getInComeData(final DB2UDB db2, final String schregno) {
        Map retMap = new HashMap();
        String sql = getInComeDataSql(schregno);
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InComeDetailInfo addwk = new InComeDetailInfo(rs.getString("INCOME_L_CD"), rs.getString("LEVY_L_NAME"), rs.getString("LEVY_M_NAME"), rs.getLong("INCOME_MONEY"));
                List addlist;
                if (!retMap.containsKey(rs.getString("INCOME_L_CD"))) {
                    addlist = new ArrayList();
                } else {
                    addlist = (List)retMap.get(rs.getString("INCOME_L_CD"));
                }
                addlist.add(addwk);
                retMap.put(rs.getString("INCOME_L_CD"), addlist);
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retMap;
    }

    private String getInComeDataSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     INCS.INCOME_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     INCS.INCOME_M_CD, ");
        stb.append("     INCS.LINE_NO, ");
        stb.append("     LEVYM.LEVY_M_NAME, ");
        stb.append("     SUM(VALUE(INCS.INCOME_MONEY, 0)) AS INCOME_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_INCOME_DAT INCD ");
        stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCS ON INCD.SCHOOLCD = INCS.SCHOOLCD ");
        stb.append("           AND INCD.SCHOOL_KIND= INCS.SCHOOL_KIND ");
        stb.append("           AND INCD.YEAR = INCS.YEAR ");
        stb.append("           AND INCD.INCOME_L_CD = INCS.INCOME_L_CD ");
        stb.append("           AND INCD.INCOME_M_CD = INCS.INCOME_M_CD ");
        stb.append("           AND INCD.REQUEST_NO = INCS.REQUEST_NO ");
        stb.append("           AND INCS.SCHREGNO = '" + schregno + "' ");
        stb.append("     LEFT JOIN LEVY_M_MST LEVYM ON INCD.SCHOOLCD = LEVYM.SCHOOLCD ");
        stb.append("          AND INCD.SCHOOL_KIND= LEVYM.SCHOOL_KIND ");
        stb.append("          AND INCD.YEAR = LEVYM.YEAR ");
        stb.append("          AND INCD.INCOME_L_CD = LEVYM.LEVY_L_CD ");
        stb.append("          AND INCD.INCOME_M_CD = LEVYM.LEVY_M_CD ");
        stb.append("     LEFT JOIN LEVY_L_MST LEVYL ON INCD.SCHOOLCD = LEVYL.SCHOOLCD ");
        stb.append("          AND INCD.SCHOOL_KIND= LEVYL.SCHOOL_KIND ");
        stb.append("          AND INCD.YEAR = LEVYL.YEAR ");
        stb.append("          AND INCD.INCOME_L_CD = LEVYL.LEVY_L_CD ");
        stb.append(" WHERE ");
        stb.append("     INCD.YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(INCD.INCOME_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(INCD.INCOME_CANCEL, '0') = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     INCS.INCOME_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     INCS.INCOME_M_CD, ");
        stb.append("     INCS.LINE_NO, ");
        stb.append("     LEVYM.LEVY_M_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     INCS.INCOME_L_CD, ");
        stb.append("     INCS.INCOME_M_CD, ");
        stb.append("     INCS.LINE_NO ");
        return stb.toString();
    }

    private class InComeInfo {
        final String _levyName;
        final long _incomeMoney;
        InComeInfo(final String levyName, final long incomeMoney) {
            _levyName = levyName;
            _incomeMoney = incomeMoney;
        }
    }

    private List getInComeLcdData(final DB2UDB db2, final String schregno) {
        List retlist = new ArrayList();
        String sql = getInComeLcdDataSql(schregno);
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InComeInfo addwk = new InComeInfo(rs.getString("LEVY_L_NAME"), rs.getLong("INCOME_MONEY"));
                retlist.add(addwk);
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retlist;
    }

    private String getInComeLcdDataSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     INCS.INCOME_L_CD, ");
        stb.append("     INCS.LINE_NO, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     SUM(VALUE(INCS.INCOME_MONEY, 0)) AS INCOME_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_INCOME_DAT INCD ");
        stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCS ON INCD.SCHOOLCD = INCS.SCHOOLCD ");
        stb.append("           AND INCD.SCHOOL_KIND= INCS.SCHOOL_KIND ");
        stb.append("           AND INCD.YEAR = INCS.YEAR ");
        stb.append("           AND INCD.INCOME_L_CD = INCS.INCOME_L_CD ");
        stb.append("           AND INCD.INCOME_M_CD = INCS.INCOME_M_CD ");
        stb.append("           AND INCD.REQUEST_NO = INCS.REQUEST_NO ");
        stb.append("           AND INCS.SCHREGNO = '" + schregno + "' ");
        stb.append("     LEFT JOIN LEVY_L_MST LEVYL ON INCD.SCHOOLCD = LEVYL.SCHOOLCD ");
        stb.append("          AND INCD.SCHOOL_KIND= LEVYL.SCHOOL_KIND ");
        stb.append("          AND INCD.YEAR = LEVYL.YEAR ");
        stb.append("          AND INCD.INCOME_L_CD = LEVYL.LEVY_L_CD ");
        stb.append(" WHERE ");
        stb.append("     INCD.YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(INCD.INCOME_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(INCD.INCOME_CANCEL, '0') = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     INCS.INCOME_L_CD, ");
        stb.append("     INCS.LINE_NO, ");
        stb.append("     LEVYL.LEVY_L_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     INCS.INCOME_L_CD, ");
        stb.append("     INCS.LINE_NO ");
        return stb.toString();
    }

    private class OutGoingDetailInfo {
        final String _levyLName;
        final String _levyLCd;
        final String _levySName;
        final long _outGoingMoney;
        OutGoingDetailInfo(final String levyLCd, final String levyLName, final String levySName, final long outGoingMoney) {
            _levyLCd = levyLCd;
            _levyLName = levyLName;
            _levySName = levySName;
            _outGoingMoney = outGoingMoney;
        }
    }

    private Map getOutGoingData(final DB2UDB db2, final String schregno) {
        Map retMap = new HashMap();
        String sql = getOutGoingDataSql(schregno);
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OutGoingDetailInfo addwk = new OutGoingDetailInfo(rs.getString("OUTGO_L_CD"), rs.getString("LEVY_L_NAME"), rs.getString("LEVY_S_NAME"), rs.getLong("OUTGO_MONEY"));
                List addlist;
                if (!retMap.containsKey(rs.getString("OUTGO_L_CD"))) {
                    addlist = new ArrayList();
                } else {
                    addlist = (List)retMap.get(rs.getString("OUTGO_L_CD"));
                }
                addlist.add(addwk);
                retMap.put(rs.getString("OUTGO_L_CD"), addlist);
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retMap;
    }

    private String getOutGoingDataSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     OUTS.OUTGO_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     OUTS.OUTGO_M_CD, ");
        stb.append("     OUTS.OUTGO_S_CD, ");
        stb.append("     LEVYS.LEVY_S_NAME, ");
        stb.append("     SUM(VALUE(OUTS.OUTGO_MONEY, 0)) AS OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_DAT OUTD ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD = OUTS.SCHOOLCD ");
        stb.append("           AND OUTD.SCHOOL_KIND= OUTS.SCHOOL_KIND ");
        stb.append("           AND OUTD.YEAR = OUTS.YEAR ");
        stb.append("           AND OUTD.OUTGO_L_CD = OUTS.OUTGO_L_CD ");
        stb.append("           AND OUTD.OUTGO_M_CD = OUTS.OUTGO_M_CD ");
        stb.append("           AND OUTD.REQUEST_NO = OUTS.REQUEST_NO ");
        stb.append("           AND OUTS.SCHREGNO = '" + schregno + "' ");
        stb.append("     LEFT JOIN LEVY_S_MST LEVYS ON OUTS.SCHOOLCD = LEVYS .SCHOOLCD ");
        stb.append("          AND OUTS.SCHOOL_KIND= LEVYS .SCHOOL_KIND ");
        stb.append("          AND OUTS.YEAR = LEVYS .YEAR ");
        stb.append("          AND OUTS.OUTGO_L_CD = LEVYS .LEVY_L_CD ");
        stb.append("          AND OUTS.OUTGO_M_CD = LEVYS .LEVY_M_CD ");
        stb.append("          AND OUTS.OUTGO_S_CD = LEVYS .LEVY_S_CD ");
        stb.append("     LEFT JOIN LEVY_L_MST LEVYL ON OUTD.SCHOOLCD = LEVYL.SCHOOLCD ");
        stb.append("          AND OUTD.SCHOOL_KIND= LEVYL.SCHOOL_KIND ");
        stb.append("          AND OUTD.YEAR = LEVYL.YEAR ");
        stb.append("          AND OUTD.OUTGO_L_CD = LEVYL.LEVY_L_CD ");
        stb.append(" WHERE ");
        stb.append("     OUTD.YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(OUTD.OUTGO_CANCEL, '0') = '0' ");
        stb.append("     AND OUTD.OUTGO_L_CD != '99' AND OUTD.OUTGO_M_CD != '99' ");
        stb.append("     AND OUTD.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("     AND OUTD.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("     AND OUTD.INCOME_L_CD not in ('98') "); //給付は除く
        stb.append(" GROUP BY ");
        stb.append("     OUTS.OUTGO_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     OUTS.OUTGO_M_CD, ");
        stb.append("     OUTS.OUTGO_S_CD, ");
        stb.append("     LEVYS.LEVY_S_NAME ");
        return stb.toString();
    }

    private class OutGoingInfo {
        final String _levyName;
        final long _outGoingMoney;
        OutGoingInfo(final String levyName, final long outGoingMoney) {
            _levyName = levyName;
            _outGoingMoney = outGoingMoney;
        }
    }

    private List getOutGoingLcdData(final DB2UDB db2, final String schregno) {
        List retlist = new ArrayList();
        String sql = getOutGoingLcdDataSql(schregno);
        log.debug("out sql = "+ sql);
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OutGoingInfo addwk = new OutGoingInfo(rs.getString("LEVY_L_NAME"), rs.getLong("OUTGO_MONEY"));
                retlist.add(addwk);
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retlist;
    }

    private String getOutGoingLcdDataSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     OUTS.OUTGO_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     SUM(VALUE(OUTS.OUTGO_MONEY, 0)) AS OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_DAT OUTD ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD = OUTS.SCHOOLCD ");
        stb.append("           AND OUTD.SCHOOL_KIND= OUTS.SCHOOL_KIND ");
        stb.append("           AND OUTD.YEAR = OUTS.YEAR ");
        stb.append("           AND OUTD.OUTGO_L_CD = OUTS.OUTGO_L_CD ");
        stb.append("           AND OUTD.OUTGO_M_CD = OUTS.OUTGO_M_CD ");
        stb.append("           AND OUTD.REQUEST_NO = OUTS.REQUEST_NO ");
        stb.append("           AND OUTS.SCHREGNO = '" + schregno + "' ");
        stb.append("     LEFT JOIN LEVY_L_MST LEVYL ON OUTS.SCHOOLCD = LEVYL .SCHOOLCD ");
        stb.append("          AND OUTS.SCHOOL_KIND= LEVYL.SCHOOL_KIND ");
        stb.append("          AND OUTS.YEAR = LEVYL.YEAR ");
        stb.append("          AND OUTS.OUTGO_L_CD = LEVYL.LEVY_L_CD ");
        stb.append(" WHERE ");
        stb.append("     OUTD.YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(OUTD.OUTGO_CANCEL, '0') = '0' ");
        stb.append("     AND OUTD.OUTGO_L_CD != '99' AND OUTD.OUTGO_M_CD != '99' ");
        stb.append("     AND OUTD.SCHOOLCD = '" + _param._schoolcd + "' ");
        stb.append("     AND OUTD.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append(" GROUP BY ");
        stb.append("     OUTS.OUTGO_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME ");
        return stb.toString();
    }

    /** 予算 */
    private class OutBudgetInfo {
        final String _outGoLMScd;
        final String _budgetLMScd;
        int _budgetMoney;
        int _outGoMoney;
        OutBudgetInfo(
                final String outGoLMScd,
                final String budgetLMScd,
                final int budgetMoney,
                final int outGoingMoney
                ) {
            _outGoLMScd     = outGoLMScd;
            _budgetLMScd    = budgetLMScd;
            _budgetMoney    = budgetMoney;
            _outGoMoney     = outGoingMoney;
        }
    }

    private Map getOutBudgetData(final DB2UDB db2, final String schregno) {
        Map retMap = new TreeMap();
        String sql = getOutBudgetDataSql(schregno);
        String befBudGetLMScd = "";
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String _outgoLcd  = rs.getString("LCD");
                final String _outgoMcd  = rs.getString("MCD");
                final String _outgoScd  = rs.getString("SCD");
                final String _budgetLcd = rs.getString("BUDGET_L_CD");
                final String _budgetMcd = rs.getString("BUDGET_M_CD");
                final String _budgetScd = rs.getString("BUDGET_S_CD");
                final int _budgetMoney  = rs.getInt("BUDGET_MONEY");
                final int _outGoMoney   = rs.getInt("OUTGO_MONEY");

                //予備費のレコードはリストに入れない
                if (YOBIHI_CD.equals(_outgoScd)) continue;

                final String outGoLMScd  = _outgoLcd + _outgoMcd + _outgoScd;
                final String budgetLMScd = _budgetLcd + _budgetMcd + _budgetScd;

                final OutBudgetInfo outB = new OutBudgetInfo(outGoLMScd, budgetLMScd, _budgetMoney, _outGoMoney);
                List addList;
                if (!retMap.containsKey(_outgoLcd)) {
                    addList = new ArrayList();
                    addList.add(outB);
                } else {
                    addList = (List) retMap.get(_outgoLcd);

                    //リストの最終要素を取得。SQLのソートを変更する時は、ロジックの修正必要あり
                    final int indx = addList.size() - 1;
                    final OutBudgetInfo miniB = (OutBudgetInfo) addList.get(indx);

                    //予算明細が同じものは合算する、別は帳票に印字する為リストに追加
                    if (miniB._outGoLMScd.equals(outGoLMScd) && miniB._budgetLMScd.equals(budgetLMScd)) {
                        miniB._outGoMoney += _outGoMoney;
                    } else {
                        addList.add(outB);
                    }

                    //使用予算が同じデータを予算合計から引く
                    if (!"".equals(befBudGetLMScd) && befBudGetLMScd.equals(budgetLMScd)) {
                        for (final Iterator it2 = addList.iterator(); it2.hasNext();) {
                            final OutBudgetInfo ob2 = (OutBudgetInfo) it2.next();
                            final String sCd = StringUtils.substring(ob2._outGoLMScd, 4);

                            if (TOTAL_SCD.equals(sCd)) {
                                ob2._budgetMoney -= _budgetMoney;
                                break;
                            }
                        }
                    }
                }
                befBudGetLMScd = budgetLMScd;
                retMap.put(_outgoLcd, addList);
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception getOutBudgetData", e);
        } finally {
            db2.commit();
        }

        return retMap;
    }

    private String getOutBudgetDataSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH OUTGO_DAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OSCH.OUTGO_L_CD, ");
        stb.append("         OSCH.OUTGO_M_CD, ");
        stb.append("         OSCH.OUTGO_S_CD, ");
        stb.append("         OSCH.REQUEST_NO, ");
        stb.append("         OSCH.LINE_NO, ");
        stb.append("         OSCH.OUTGO_MONEY ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                              AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                              AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                              AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                              AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                              AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             OSCH.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("         AND OSCH.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("         AND OSCH.YEAR        = '" + _param._year + "' ");
        stb.append("         AND OSCH.SCHREGNO    = '" + schregno + "' ");
        stb.append("         AND value(OUTG.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("         AND value(OUTG.OUTGO_CANCEL, '0')   = '0' ");
        stb.append("         AND OUTG.OUTGO_L_CD  <> '99' ");
        stb.append("         AND OUTG.OUTGO_M_CD  <> '99' ");
        stb.append("         AND OUTG.INCOME_L_CD <> '98' ");
        stb.append("         AND OUTG.INCOME_M_CD <> '98' ");
        stb.append(" ), MAIN_LMS AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         SCHOOLCD, ");
        stb.append("         SCHOOL_KIND, ");
        stb.append("         YEAR, ");
        stb.append("         OUTGO_L_CD as LCD, ");
        stb.append("         OUTGO_M_CD as MCD, ");
        stb.append("         OUTGO_S_CD as SCD ");
        stb.append("     FROM ");
        stb.append("         OUTGO_DAT ");
        stb.append("     UNION ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         BJET.SCHOOLCD, ");
        stb.append("         BJET.SCHOOL_KIND, ");
        stb.append("         BJET.YEAR, ");
        stb.append("         BJET.BUDGET_L_CD AS OUTGO_L_CD, ");
        stb.append("         BJET.BUDGET_M_CD AS OUTGO_M_CD, ");
        stb.append("         BJET.BUDGET_S_CD AS OUTGO_S_CD ");
        stb.append("     FROM ");
        stb.append("         LEVY_BUDGET_DAT BJET ");
        stb.append("     WHERE ");
        stb.append("             BJET.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("         AND BJET.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("         AND BJET.YEAR        = '" + _param._year + "' ");
        stb.append("         AND BJET.BUDGET_L_CD || BJET.BUDGET_M_CD ");
        stb.append("                             in (SELECT DISTINCT ");
        stb.append("                                     OUTGO_L_CD || OUTGO_M_CD ");
        stb.append("                                 FROM ");
        stb.append("                                     OUTGO_DAT ");
        stb.append("                                 ) ");
        stb.append(" ), MINI_T_MAIN AS (  ");
        stb.append("     SELECT ");
        stb.append("         MAIN.LCD ");
        stb.append("         , MAIN.MCD ");
        stb.append("         , MAIN.SCD ");
        stb.append("         , BMEI.BUDGET_L_CD ");
        stb.append("         , BMEI.BUDGET_M_CD ");
        stb.append("         , BMEI.BUDGET_S_CD ");
        stb.append("         , case  ");
        stb.append("             when value (OUTG.OUTGO_MONEY, '0') = '0'  ");
        stb.append("                 then BGET2.BUDGET_MONEY  ");
        stb.append("             when BMEI.OUTGO_L_CD = BMEI.BUDGET_L_CD  ");
        stb.append("             and BMEI.OUTGO_M_CD = BMEI.BUDGET_M_CD  ");
        stb.append("             and BMEI.OUTGO_S_CD = BMEI.BUDGET_S_CD  ");
        stb.append("                 then BGET2.BUDGET_MONEY  ");
        stb.append("             else BGET1.BUDGET_MONEY  ");
        stb.append("             end as BUDGET_MONEY ");
        stb.append("         , value (OUTG.OUTGO_MONEY, '0') as OUTGO_MONEY  ");
        stb.append("     FROM ");
        stb.append("         MAIN_LMS MAIN  ");
        stb.append("         LEFT JOIN OUTGO_DAT OUTG  ");
        stb.append("             ON OUTG.SCHOOLCD = MAIN.SCHOOLCD  ");
        stb.append("             AND OUTG.SCHOOL_KIND = MAIN.SCHOOL_KIND  ");
        stb.append("             AND OUTG.YEAR = MAIN.YEAR  ");
        stb.append("             AND OUTG.OUTGO_L_CD = MAIN.LCD  ");
        stb.append("             AND OUTG.OUTGO_M_CD = MAIN.MCD  ");
        stb.append("             AND OUTG.OUTGO_S_CD = MAIN.SCD  ");
        stb.append("         LEFT JOIN LEVY_BUDGET_MEISAI_DAT BMEI  ");
        stb.append("             ON BMEI.SCHOOLCD = MAIN.SCHOOLCD  ");
        stb.append("             AND BMEI.SCHOOL_KIND = MAIN.SCHOOL_KIND  ");
        stb.append("             AND BMEI.YEAR = MAIN.YEAR  ");
        stb.append("             AND BMEI.REQUEST_NO = OUTG.REQUEST_NO  ");
        stb.append("             AND BMEI.LINE_NO = OUTG.LINE_NO  ");
        stb.append("             AND BMEI.OUTGO_L_CD = OUTG.OUTGO_L_CD  ");
        stb.append("             AND BMEI.OUTGO_M_CD = OUTG.OUTGO_M_CD  ");
        stb.append("             AND BMEI.OUTGO_S_CD = OUTG.OUTGO_S_CD  ");
        stb.append("         LEFT JOIN LEVY_BUDGET_DAT BGET1  ");
        stb.append("             ON BGET1.SCHOOLCD = MAIN.SCHOOLCD  ");
        stb.append("             AND BGET1.SCHOOL_KIND = MAIN.SCHOOL_KIND  ");
        stb.append("             AND BGET1.YEAR = MAIN.YEAR  ");
        stb.append("             AND BGET1.BUDGET_L_CD = BMEI.BUDGET_L_CD  ");
        stb.append("             AND BGET1.BUDGET_M_CD = BMEI.BUDGET_M_CD  ");
        stb.append("             AND BGET1.BUDGET_S_CD = BMEI.BUDGET_S_CD  ");
        stb.append("         LEFT JOIN LEVY_BUDGET_DAT BGET2  ");
        stb.append("             ON BGET2.SCHOOLCD = MAIN.SCHOOLCD  ");
        stb.append("             AND BGET2.SCHOOL_KIND = MAIN.SCHOOL_KIND  ");
        stb.append("             AND BGET2.YEAR = MAIN.YEAR  ");
        stb.append("             AND BGET2.BUDGET_L_CD = MAIN.LCD  ");
        stb.append("             AND BGET2.BUDGET_M_CD = MAIN.MCD  ");
        stb.append("             AND BGET2.BUDGET_S_CD = MAIN.SCD ");
        stb.append(" ), MINI_T_NA AS (  ");
        stb.append("     SELECT ");
        stb.append("         T1.LCD ");
        stb.append("         , T1.MCD ");
        stb.append("         , T1.SCD ");
        stb.append("         , T1.BUDGET_L_CD ");
        stb.append("         , T1.BUDGET_M_CD ");
        stb.append("         , T1.BUDGET_S_CD ");
        stb.append("         , T1.BUDGET_MONEY ");
        stb.append("         , T1.OUTGO_MONEY  ");
        stb.append("     FROM ");
        stb.append("         MINI_T_MAIN T1 ");
        stb.append("         INNER JOIN MINI_T_MAIN L1 ON T1.LCD = L1.BUDGET_L_CD ");
        stb.append("               AND T1.MCD = L1.BUDGET_M_CD ");
        stb.append("               AND T1.SCD = L1.BUDGET_S_CD ");
        stb.append("     WHERE ");
        stb.append("         VALUE(T1.OUTGO_MONEY, 0) = 0 ");
        stb.append(" ), MINI_T AS (  ");
        stb.append("     SELECT ");
        stb.append("         T1.LCD ");
        stb.append("         , T1.MCD ");
        stb.append("         , T1.SCD ");
        stb.append("         , T1.BUDGET_L_CD ");
        stb.append("         , T1.BUDGET_M_CD ");
        stb.append("         , T1.BUDGET_S_CD ");
        stb.append("         , T1.BUDGET_MONEY ");
        stb.append("         , T1.OUTGO_MONEY  ");
        stb.append("     FROM ");
        stb.append("         MINI_T_MAIN T1 ");
        stb.append("     WHERE ");
        stb.append("         NOT EXISTS( ");
        stb.append("             SELECT ");
        stb.append("                 'x' ");
        stb.append("             FROM ");
        stb.append("                 MINI_T_NA E1 ");
        stb.append("             WHERE ");
        stb.append("                 T1.LCD = E1.LCD ");
        stb.append("                 AND T1.MCD = E1.MCD ");
        stb.append("                 AND T1.SCD = E1.SCD ");
        stb.append("         ) ");
        stb.append(" ), MAIN_T AS  ( ");
        stb.append("     SELECT ");
        stb.append("         LCD, ");
        stb.append("         MCD, ");
        stb.append("         SCD, ");
        stb.append("         value(BUDGET_L_CD, '') as BUDGET_L_CD, ");
        stb.append("         value(BUDGET_M_CD, '') as BUDGET_M_CD, ");
        stb.append("         value(BUDGET_S_CD, '') as BUDGET_S_CD, ");
        stb.append("         BUDGET_MONEY, ");
        stb.append("         OUTGO_MONEY ");
        stb.append("     FROM ");
        stb.append("         MINI_T ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         LCD, ");
        stb.append("         MCD, ");
        stb.append("         '000' AS SCD, ");
        stb.append("         '00' AS BUDGET_L_CD, ");
        stb.append("         '00' AS BUDGET_M_CD, ");
        stb.append("         '000' AS BUDGET_S_CD, ");
        stb.append("         sum(case when BUDGET_S_CD = '" + YOBIHI_CD + "' then 0 else BUDGET_MONEY end) AS BUDGET_MONEY, ");
        stb.append("         sum(OUTGO_MONEY) AS OUTGO_MONEY ");
        stb.append("     FROM ");
        stb.append("         MINI_T ");
        stb.append("     GROUP BY ");
        stb.append("         LCD, ");
        stb.append("         MCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     LCD, ");
        stb.append("     MCD, ");
        stb.append("     SCD, ");
        stb.append("     BUDGET_L_CD, ");
        stb.append("     BUDGET_M_CD, ");
        stb.append("     BUDGET_S_CD, ");
        stb.append("     BUDGET_MONEY, ");
        stb.append("     OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" ORDER BY ");
        stb.append("     LCD, ");
        stb.append("     MCD, ");
        stb.append("     case when BUDGET_S_CD = '' then '000' else BUDGET_S_CD end, ");
        stb.append("     SCD ");

        return stb.toString();
    }

    //証明書学校データ
    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76885 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _idouDate;
        private final String _printDate;
        private final String _year;
        private final String _schoolcd;
        private final String _schoolkind;
        private final String _gradeHrClass;
        private final String _hrName;
        private final String _address;
        private final String _addrDiv;
        private final String _useBenefit;
        private final String _useLevyBudget; //予実管理使用するか(1：使用、その他：未使用)
        final String[] _categorySelected;
        final Map _levyLMstMap;
        final Map _levyMMstMap;
        final Map _levySMstMap;
        final Map _notPriBudgetMap;
        final String _writeDoc;
        final CertifSchool _certifSchool;
        private final String _tesuryo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _idouDate        = request.getParameter("TENTAI_DATE");
            _printDate        = request.getParameter("HENKIN_KAKUTEI");
            _year             = request.getParameter("YEAR");
            _schoolcd         = request.getParameter("SCHOOLCD");
            _schoolkind       = request.getParameter("SCHOOL_KIND");
            _gradeHrClass     = request.getParameter("GRADE_HR_CLASS");
            _hrName           = getHrName(db2, _year, _gradeHrClass);
            _address          = request.getParameter("ADDRESS");
            _addrDiv          = request.getParameter("ADDR_DIV"); //（1:生徒 2:保護者）
            _useBenefit       = request.getParameter("useBenefit");
            _useLevyBudget    = request.getParameter("LevyBudget");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _levyLMstMap      = getLevyLMst(db2);
            _levyMMstMap      = getLevyMMst(db2);
            _levySMstMap      = getLevySMst(db2);
            _notPriBudgetMap  = getBudgetDat(db2);
            _writeDoc         = getWriteDocument(db2);
            _certifSchool     = getCertifSchool(db2);
            _tesuryo          = getTesuryo(db2);
        }

        /** 手数料 */
        private String getTesuryo(final DB2UDB db2) {
            String schoolKindFee = "";
            String allFee = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     BANK_TRANSFER_FEE ");
                stb.append(" FROM ");
                stb.append("     COLLECT_SCHOOL_BANK_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("     AND SCHOOL_KIND IN ('" + _schoolkind + "', '99') ");
                stb.append("     AND YEAR        = '" + _year + "' ");
                stb.append("     AND FORMAT_DIV  = '2' ");   // 1:引落 2:返金
                stb.append("     AND SEQ         = '001' "); // 固定
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("99".equals(rs.getString("SCHOOL_KIND"))) {
                        allFee = StringUtils.defaultString(rs.getString("BANK_TRANSFER_FEE"));
                    } else {
                        schoolKindFee = StringUtils.defaultString(rs.getString("BANK_TRANSFER_FEE"));
                    }
                }
            } catch (SQLException ex) {
                log.debug("getTesuryo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return !"".equals(schoolKindFee) ? schoolKindFee : allFee;
        }

        /** LEVY_BUDGET_DAT */
        private Map getBudgetDat(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     BUDGET_L_CD, ");
                stb.append("     BUDGET_M_CD, ");
                stb.append("     BUDGET_S_CD ");
                stb.append(" FROM ");
                stb.append("     LEVY_BUDGET_DAT ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolkind + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");
                stb.append("     AND BUDGET_S_CD = 'AAA' ");
                stb.append("     AND BUDGET_L_CD || BUDGET_M_CD in ( ");
                stb.append("                 SELECT ");
                stb.append("                     BUDGET_L_CD || BUDGET_M_CD ");
                stb.append("                 FROM ");
                stb.append("                     LEVY_BUDGET_DAT ");
                stb.append("                 WHERE ");
                stb.append("                         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("                     AND SCHOOL_KIND = '" + _schoolkind + "' ");
                stb.append("                     AND YEAR        = '" + _year + "' ");
                stb.append("                 GROUP BY ");
                stb.append("                     BUDGET_L_CD, ");
                stb.append("                     BUDGET_M_CD ");
                stb.append("                 HAVING ");
                stb.append("                     count(*) = 1 ");
                stb.append("             ) ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd    = rs.getString("BUDGET_L_CD");
                    final String mCd    = rs.getString("BUDGET_M_CD");
                    final String sCd    = rs.getString("BUDGET_S_CD");

                    final String setKey = lCd + mCd;

                    retMap.put(setKey, sCd);
                }
            } catch (SQLException ex) {
                log.debug("getBudgetDat:exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** LEVY_L_MST */
        private Map getLevyLMst(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     LEVY_L_CD, ");
                stb.append("     LEVY_L_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_L_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolkind + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd    = rs.getString("LEVY_L_CD");
                    final String lName  = rs.getString("LEVY_L_NAME");

                    retMap.put(lCd, lName);
                }
            } catch (SQLException ex) {
                log.debug("getLevyLMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** LEVY_M_MST */
        private Map getLevyMMst(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     LEVY_L_CD, ");
                stb.append("     LEVY_M_CD, ");
                stb.append("     LEVY_M_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_M_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolkind + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd    = rs.getString("LEVY_L_CD");
                    final String mCd    = rs.getString("LEVY_M_CD");
                    final String mName  = rs.getString("LEVY_M_NAME");

                    retMap.put(lCd + mCd, mName);
                }
            } catch (SQLException ex) {
                log.debug("getLevyMMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** LEVY_S_MST */
        private Map getLevySMst(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     LEVY_L_CD, ");
                stb.append("     LEVY_M_CD, ");
                stb.append("     LEVY_S_CD, ");
                stb.append("     LEVY_S_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_S_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolkind + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd    = rs.getString("LEVY_L_CD");
                    final String mCd    = rs.getString("LEVY_M_CD");
                    final String sCd    = rs.getString("LEVY_S_CD");
                    final String sName  = rs.getString("LEVY_S_NAME");

                    retMap.put(lCd + mCd + sCd, sName);
                }
            } catch (SQLException ex) {
                log.debug("getLevySMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** 証明書学校データ */
        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool(null, null, null);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '146' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = rs.getString("SCHOOL_NAME");
                    final String jobName        = rs.getString("JOB_NAME");
                    final String principalName  = rs.getString("PRINCIPAL_NAME");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName);
                }
            } catch (SQLException ex) {
                log.debug("getCertif exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

    }

    private String getHrName(final DB2UDB db2, final String year, final String hrClass) {
        String retstr = "";
        String sql = getRegdDatSql(year, hrClass);
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                retstr = (rs.getString("HR_NAME"));
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retstr;
    }

    private String getRegdDatSql(final String year, final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("         YEAR = '" + year +"' ");
        stb.append("     AND GRADE || HR_CLASS = '" + hrClass +"' ");
        return stb.toString();
    }

    private String getWriteDocument(final DB2UDB db2) {
        String retstr = "";
        String sql = getWriteDocumentSql();
        try {
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                retstr = (rs.getString("TEXT"));
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        return retstr;
    }

    private String getWriteDocumentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     TEXT ");
        stb.append(" FROM ");
        stb.append("     DOCUMENT_MST ");
        stb.append(" WHERE ");
        stb.append("     DOCUMENTCD = 'C1' ");
        return stb.toString();
    }
}

// eof
