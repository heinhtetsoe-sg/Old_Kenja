/*
 * $Id: 71be9920a25a043c7c691266b296df6a3143c191 $
 *
 * 作成日: 2019/02/26
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJP909 {

    private static final Log log = LogFactory.getLog(KNJP909.class);

    private static final int DETAILPAGEMAXLINE = 50;
    private static final int BUDGETPAGE_MAXLINE = 50;
    private static final String FIELD_ATTRIBUTE = "!!FIELD_ATTRIBUTE";
    private static final String ATTR_RIGHT = "Hensyu=1"; // 右詰
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
        final List printList = getList(db2);

        NumberFormat nfNum = NumberFormat.getNumberInstance();    //カンマ区切り形式

        try {
            final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
            final String printDate = KNJ_EditDate.h_format_JP(db2, _param._ctrlDate);

            for (final Iterator iterator = printList.iterator(); iterator.hasNext();) {
                final PrintData pd = (PrintData) iterator.next();
                if ("1".equals(_param._useLevyBudget)) {
                    pd.detailPageList = getPageList(getOutputBudgetLines(pd), BUDGETPAGE_MAXLINE);
                } else {
                    pd.detailPageList = getPageList(getOutputLines(pd), DETAILPAGEMAXLINE);
                }

                svf.VrSetForm("KNJP909.frm", 4);

                svf.VrsOut("PAGE", "1/" + String.valueOf(1 + pd.detailPageList.size()));

                // ヘッダー情報
                svf.VrsOut("DATE", printDate);
                //住所
                if (!StringUtils.isBlank(pd._zipCd)) {
                    svf.VrsOut("ZIPNO", "〒" + pd._zipCd);
                }
                final int add1Len = KNJ_EditEdit.getMS932ByteLength(pd._addr1);
                final int add2Len = KNJ_EditEdit.getMS932ByteLength(pd._addr2);
                final String addIdx = add1Len > 50 || add2Len > 50 ? "4": add1Len > 40 || add2Len > 40 ? "3": add1Len > 30 || add2Len > 30 ? "2": "1";
                svf.VrsOut("ADDRESS1_" + addIdx, pd._addr1);
                svf.VrsOut("ADDRESS2_" + addIdx, pd._addr2);

                svf.VrsOut("HR_NAME", pd._hrName); // 年組

                final String setName = pd._name + "　保護者様";
                final String nameIdx = KNJ_EditEdit.getMS932ByteLength(setName) > 44 ? "3": KNJ_EditEdit.getMS932ByteLength(setName) > 34 ? "2": "1";
                svf.VrsOut("NAME1_" + nameIdx, setName);

                //学校名
                svf.VrsOut("SCHOOLNAME", _param._certifSchool._schoolName);
                //職名
                svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);
                //校長名
                svf.VrsOut("STAFF_NAME1", _param._certifSchool._principalName);

                // 問合せ
                svf.VrsOut("SCHOOL_NAME2", _param._certifSchool._schoolName2);  // 学校名
                svf.VrsOut("TELNO", _param._certifSchool._schoolTel);           // 電話番号
                svf.VrsOut("CHARGE", _param._certifSchool._staffName);          // 担当者名

                // タイトル
                final String title = nendo + "年度学校徴収金の返金について";
                svf.VrsOut("TITLE", title);
                if ("1".equals(_param._useFormKNJP909)) {
                	svf.VrsOut("BACK_TITLE", "　返金が遅くなりまして申し訳ありませんでした。");                	
                }
                
                // ３.精算金額内訳
                //給付額
                if (pd._benefitMoney > 0) {
                    pd.totalIncomeMoney    += pd._benefitMoney;
                    pd.totalHenkinMoney    += pd._benefitMoney;
                    svf.VrEndRecord();
                }

                // １.清算金額
                svf.VrsOut("METHOD_NAME", "１　清算金額");
                final String text = String.valueOf(nfNum.format(pd.totalHenkinMoney)) + "円を返金致します。";
                svf.VrsOut("METHOD", text);
                svf.VrEndRecord();

                svf.VrsOut("TEXT", "　　(清算金額内訳)");
                svf.VrEndRecord();

                //内訳
                final String recordName = pd.totalcarryOverMoney > 0 ? "1" : "2";
                svf.VrsOut("HEADER" + recordName, "空");
                for (Iterator itUchiwake = pd.printUchiwakeList.iterator(); itUchiwake.hasNext();) {
                    final PrintUchiwake printUchiwake = (PrintUchiwake) itUchiwake.next();

                    final int kindLen = KNJ_EditEdit.getMS932ByteLength(printUchiwake._lName);
                    final String kindIdx = kindLen > 18 ? "_3_1": kindLen > 12 ? "_2": "_1";
                    svf.VrsOut("KIND"  + recordName + kindIdx, printUchiwake._lName);       // 種別
                    svf.VrsOut("MONEY" + recordName + "_1", String.valueOf(printUchiwake._incomeMoney));      // 納入額
                    svf.VrsOut("MONEY" + recordName + "_2", String.valueOf(printUchiwake._outgoMoney));       // 支出額
                    svf.VrsOut("MONEY" + recordName + "_3", String.valueOf(printUchiwake._carryOverMoney));   // 翌年度積立額
                    svf.VrsOut("MONEY" + recordName + "_4", String.valueOf(printUchiwake._henkinMoney));      // 精算額
                    svf.VrEndRecord();
                }
                //給付額
                if (pd._benefitMoney > 0) {
                    svf.VrsOut("KIND"  + recordName + "_3_1", Param.KYUFU_TITLE);// 種別
                    svf.VrsOut("MONEY" + recordName + "_1", String.valueOf(pd._benefitMoney));// 納入額
                    svf.VrsOut("MONEY" + recordName + "_2", "0");                              // 支出額
                    svf.VrsOut("MONEY" + recordName + "_3", "0");                              // 翌年度積立額
                    svf.VrsOut("MONEY" + recordName + "_4", String.valueOf(pd._benefitMoney));// 精算額
                    svf.VrEndRecord();
                }

                //合計
                svf.VrsOut("KIND" + recordName + "_1", "合　計"); // 種別
                svf.VrsOut("MONEY" + recordName + "_1", String.valueOf(pd.totalIncomeMoney));     // 納入額
                svf.VrsOut("MONEY" + recordName + "_2", String.valueOf(pd.totaloutgoMoney));      // 支出額
                svf.VrsOut("MONEY" + recordName + "_3", String.valueOf(pd.totalcarryOverMoney));  // 翌年度積立額
                svf.VrsOut("MONEY" + recordName + "_4", String.valueOf(pd.totalHenkinMoney));     // 精算額
                svf.VrEndRecord();

                svf.VrsOut("TEXT", "　 ※清算金額の詳細については裏面参照");
                svf.VrEndRecord();
                svf.VrsOut("BLANK", "空");
                svf.VrEndRecord();

                // ２.返金方法
                svf.VrsOut("METHOD_NAME", "２　返金方法");
                if (!"1".equals(_param._useFormKNJP909)) {
                	svf.VrsOut("METHOD", pd._bankName + "通常貯金口座への振込みになります。");
                } else {
                    svf.VrsOut("METHOD", "学校徴収金の引き落とし口座（" + pd._bankName + "通常貯金");
                    svf.VrEndRecord();
                    svf.VrsOut("METHOD", "口座）へ振込みます。"); 
                }
                svf.VrEndRecord();
                if (!"1".equals(_param._useFormKNJP909)) {
                	svf.VrsOut("METHOD", "（振込手数料" + _param._tesuryo + "円は各自負担となります。）");
                } else {
                    svf.VrsOut("METHOD", "（振込手数料" + _param._tesuryo + "円は御負担願います。）");
                }
                svf.VrEndRecord();
                svf.VrsOut("BLANK", "空");
                svf.VrEndRecord();

                // ３.返金日
                if (!"1".equals(_param._useFormKNJP909)) {
                	svf.VrsOut("METHOD_NAME", "３　返金日");
                } else {
                    svf.VrsOut("METHOD_NAME", "３　返金予定日");	
                }
                final String youbi = KNJ_EditDate.h_format_W(_param._henkinDate);
                svf.VrsOut("METHOD", KNJ_EditDate.h_format_JP(db2, _param._henkinDate) + "（" + youbi + "）");
                svf.VrEndRecord();
                svf.VrsOut("BLANK", "空");
                svf.VrEndRecord();

                if (!"1".equals(_param._useFormKNJP909)) {
	                if (!(NumberUtils.isDigits(_param._grade) && 6 == Integer.parseInt(_param._grade))) {
	                    // ４.今回同封した書類
	                    svf.VrsOut("METHOD_NAME", "４　今回同封した書類");
	                    svf.VrEndRecord();
	                    final String nextNendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear) + 1);
	                    svf.VrsOut("TEXT", "　　" + nextNendo + "年度学校徴収金予算内訳");
	                    svf.VrEndRecord();
	                }
                }

                // 2枚目（詳細）を出力
                printDetailInfo(svf, pd);

                _hasData = true;
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        }
    }

    private Map getNewLine(final List lines) {
        final Map newLine = new HashMap();
        lines.add(newLine);
        return newLine;
    }

    /** 2枚目詳細ページ（裏） */
    private void printDetailInfo(final Vrw32alp svf, final PrintData pd){

        final int maxPage = pd.detailPageList.size();

        for (int pi = 0; pi < pd.detailPageList.size(); pi++) {
            final int iPage = pi + 1;

            final String form = ("1".equals(_param._useLevyBudget)) ? "KNJP909_3.frm": "KNJP909_2.frm";
            svf.VrSetForm(form, 4);

            //生徒名
            final String setName = pd._hrName + "　" + pd._name + "　保護者様";
            svf.VrsOut("HR_NAME", setName);

            svf.VrsOut("PAGE",  String.valueOf(iPage + 1) + "/" + (maxPage + 1));

            final List pageLines = (List) pd.detailPageList.get(pi);
            for (int li = 0; li < pageLines.size(); li++) {
                final Map line = (Map) pageLines.get(li);
                log.info(" pi = " + pi + ", li = " + li + ", line = " + line);
                Map attributeMap = null;
                for (final Iterator it = line.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String field = (String) e.getKey();
                    if (FIELD_ATTRIBUTE.equals(field)) {
                        attributeMap = getMappedMap(line, FIELD_ATTRIBUTE);
                        continue;
                    } else {
                        final String value = (String) e.getValue();
                        svf.VrsOut(field, value);
                    }
                }
                if (null != attributeMap) {
                    for (final Iterator it = attributeMap.entrySet().iterator(); it.hasNext();) {
                        final Map.Entry e = (Map.Entry) it.next();
                        final String field = (String) e.getKey();
                        final String attribute = (String) e.getValue();
                        svf.VrAttribute(field, attribute);
                    }
                }
                svf.VrEndRecord();
            }
        }

        //生徒単位で奇数ページで終わる時は、一枚空ページを印字する
        if ((maxPage + 1) % 2 == 1) {
            svf.VrSetForm("BLANK_A4_TATE.frm", 1);
            svf.VrsOut("BLANK", "BLANK");
            svf.VrEndPage();
        }
    }

    private static HashMap getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (HashMap) map.get(key1);
    }

    private static List getPageList(final List lines, final int maxCount) {
        final List pageList = new ArrayList();
        List page = null;
        for (int i = 0; i < lines.size(); i++) {
            final Object o = lines.get(i);
            if (null == page || page.size() >= maxCount) {
                page = new ArrayList();
                pageList.add(page);
            }
            page.add(o);
        }
        return pageList;
    }

    private List getOutputLines(final PrintData pd) {

        //L_NAMEタイトル
        final String tileLname = "L_NAME_2";

        final List lines = new ArrayList();
        Map line;

        final Set printMcdSet = new HashSet();
        for (final Iterator itm = pd._mergeKeyMap.keySet().iterator(); itm.hasNext();) {
            long income_total = 0;

            if (!lines.isEmpty()) {
                line = getNewLine(lines);
                line.put("BLANK", "a");
            }

            final String mkey = (String) itm.next();
            //収入①
            String grpval = "1";
            line = getNewLine(lines);
            final List inclist = (List) pd._inComeMap.get(mkey);
            final List outglist = (List) pd._outGoingMap.get(mkey);

            int carryOverMoney = 0;
            if (inclist != null) {
                InComeDetailInfo i_info = (InComeDetailInfo) inclist.get(0);
                line.put(tileLname, i_info._levyLName);
                final String carryOverMoneyS = (String) pd.carryOverMoneyMap.get(i_info._levyLCd);
                if (NumberUtils.isNumber(carryOverMoneyS)) {
                    carryOverMoney += Integer.parseInt(carryOverMoneyS);
                }
            } else if (outglist != null) {
                OutGoingDetailInfo o_info = (OutGoingDetailInfo) outglist.get(0);
                line.put(tileLname, o_info._levyLName);
            }
            boolean isFirstShunyu = true;
            line = getNewLine(lines);
            line.put("GRP1_1", grpval);
            line.put("GRP1_4", grpval);
            line.put("GRP1_2", grpval);
            line.put("GRP1_3", grpval);
            line.put("INOUT_NAME1", "収入");
            if (inclist != null) {
                for (final Iterator iti = inclist.iterator(); iti.hasNext();) {
                    InComeDetailInfo i_info = (InComeDetailInfo) iti.next();
                    if (!isFirstShunyu) {
                        line = getNewLine(lines);
                        line.put("GRP1_1", grpval);
                        line.put("GRP1_4", grpval);
                        line.put("GRP1_2", grpval);
                        line.put("GRP1_3", grpval);
                    }
                    line.put("SUMMARY1", i_info._levyMName);
                    line.put("MONEY1", String.valueOf(i_info._incomeMoney));
                    isFirstShunyu = false;
                    income_total += i_info._incomeMoney;
                }
            }
            if (!isFirstShunyu) {
                line = getNewLine(lines);
            }
            line.put("GRP1_1", grpval);
            line.put("GRP1_4", grpval);
            line.put("GRP1_2", "91");
            line.put("GRP1_3", "91");
            line.put("SUMMARY1", "①合計");
            getMappedMap(line, FIELD_ATTRIBUTE).put("SUMMARY1", ATTR_RIGHT);
            line.put("MONEY1", String.valueOf(income_total));

            //支出②
            long outcome_total = 0;
            String grp;
            final String field;
            //給付がない生徒はレコード出力を変更
            field = "6";
            grp = "2";
            int mcdCount = 1;
            grpval = grp + String.valueOf(mcdCount);
            line = getNewLine(lines);
            line.put("INOUT_NAME" + field, "支出");
            line.put("GRP" + field + "_1", grp);
            line.put("GRP" + field + "_4", grpval);
            line.put("GRP" + field + "_2", grpval);
            line.put("GRP" + field + "_3", grpval);
            boolean isfirst = true;
            if (outglist != null) {
                for (final Iterator ito = outglist.iterator(); ito.hasNext();) {
                    OutGoingDetailInfo o_info = (OutGoingDetailInfo) ito.next();
                    if (!isfirst) {
                        line = getNewLine(lines);
                    }
                    if (!printMcdSet.contains(o_info._levyLCd + o_info._levyMCd)) {
                        line.put("INOUT_NAME" + field + "_2", o_info._levyMName);
                        printMcdSet.add(o_info._levyLCd + o_info._levyMCd);
                        if (!isfirst) {
                            mcdCount += 1;
                            grpval = grp + String.valueOf(mcdCount);
                        }
                    }
                    line.put("GRP" + field + "_1", grp);
                    line.put("GRP" + field + "_4", grpval);
                    line.put("GRP" + field + "_2", grpval);
                    line.put("GRP" + field + "_3", grpval);
                    line.put("SUMMARY" + field, o_info._levySName);
                    line.put("MONEY" + field, String.valueOf(o_info._outGoingMoney));
                    outcome_total += o_info._outGoingMoney;
                    isfirst = false;
                }
            }
            if (!isfirst) {
                line = getNewLine(lines);
            }
            line.put("GRP" + field + "_1", grp);
            line.put("GRP" + field + "_4", grpval);
            line.put("GRP" + field + "_2", "92");
            line.put("GRP" + field + "_3", "92");
            line.put("SUMMARY" + field, "②合計");
            getMappedMap(line, FIELD_ATTRIBUTE).put("SUMMARY" + field, ATTR_RIGHT);
            line.put("MONEY" + field, String.valueOf(outcome_total));

            String name1;
            if (carryOverMoney > 0) {
                line = getNewLine(lines);
                // 翌年度積立額
                line.put("GRP1_1", "3");
                line.put("GRP1_4", "3");
                line.put("GRP1_2", "93");
                line.put("GRP1_3", "93");
                line.put("INOUT_NAME1", "翌年度積立額");
                line.put("MONEY1", String.valueOf(carryOverMoney));

                line = getNewLine(lines);
                line.put("GRP1_1", "3");
                line.put("GRP1_4", "3");
                line.put("GRP1_2", "93");
                line.put("GRP1_3", "93");
                line.put("SUMMARY1", "③合計");
                getMappedMap(line, FIELD_ATTRIBUTE).put("SUMMARY1", ATTR_RIGHT);
                line.put("MONEY1", String.valueOf(carryOverMoney));
                name1 = "①－②－③";
            } else {
                name1 = "①－②";
            }
            line = getNewLine(lines);
            line.put("GRP1_1", "99");
            line.put("GRP1_4", "99");
            line.put("GRP1_2", "99");
            line.put("GRP1_3", "99");
            line.put("SUMMARY1", name1);
            getMappedMap(line, FIELD_ATTRIBUTE).put("SUMMARY1", ATTR_RIGHT);
            line.put("MONEY1", String.valueOf(income_total - outcome_total - carryOverMoney));
        }
        return lines;
    }

    /** 予算・実績詳細ページ */
    private List getOutputBudgetLines(final PrintData pd) {

        final List lines = new ArrayList();
        Map line;

        //科目毎（L_CD）に印刷
        for (final Iterator itm = pd._outBudgetMap.keySet().iterator(); itm.hasNext();) {
            final String keyLcd = (String) itm.next();
            List printList  = (List) pd._outBudgetMap.get(keyLcd);

            //空白行
            if (!lines.isEmpty()) {
                line = getNewLine(lines);
                line.put("BLANK", "a");
            }

            //科目名(L_NAME)
            line = getNewLine(lines);
            final String lName = (String) _param._levyLMstMap.get(keyLcd);
            line.put("L_NAME", lName);

            int totalBudget = 0;
            int totalOutgo  = 0;
            String befGpCd2 = "";
            for (final Iterator ito = printList.iterator(); ito.hasNext();) {
                final OutBudgetInfo ob = (OutBudgetInfo) ito.next();
                final String lmCd = StringUtils.substring(ob._outGoLMScd, 0, 4);
                final String sCd = StringUtils.substring(ob._outGoLMScd, 4);
                final String budgetScd = StringUtils.substring(ob._budgetLMScd, 4);

                final String gpCd2 = !"".equals(budgetScd) ? budgetScd: sCd;

                if (TOTAL_SCD.equals(sCd)) {
                    line = getNewLine(lines);
                    //項目名(M_NAME)
                    final String mName = (String) _param._levyMMstMap.get(lmCd);
                    line.put("INOUT_NAME1", mName);
                    //予算
                    line.put("BADGET1", String.valueOf(ob._budgetMoney));
                    //実績
                    line.put("MONEY1", String.valueOf(ob._outGoMoney));

                    totalBudget += ob._budgetMoney;
                    totalOutgo  += ob._outGoMoney;
                } else {
                    //実績CDと、使用予算のCDが不一致の時は、"XXXの代替として"の文言を追加する
                    boolean printFlg = true;
                    if (!"".equals(ob._budgetLMScd) && !ob._outGoLMScd.equals(ob._budgetLMScd)) {
                        line = getNewLine(lines);
                        final String sName = (YOBIHI_CD.equals(budgetScd)) ? "予備費": (String) _param._levySMstMap.get(ob._budgetLMScd);
                        final String setSName = sName + "の代替として";
                        line.put("SUMMARY1", setSName);
                        line.put("GRP1", sCd);
                        line.put("GRP2", gpCd2);
                        line.put("GRP3", sCd);
                        //予算
                        if (!befGpCd2.equals(gpCd2)) {
                            final String budgetMoney = (_param._notPriBudgetMap.containsKey(lmCd)) ? "": String.valueOf(ob._budgetMoney);
                            line.put("BADGET2", budgetMoney);
                        }
                        //実績
                        line.put("MONEY2", String.valueOf(ob._outGoMoney));
                        printFlg = false;
                    }

                    line = getNewLine(lines);
                    //細目名(S_NAME)
                    final String sName = (String) _param._levySMstMap.get(ob._outGoLMScd);
                    line.put("SUMMARY1", sName);
                    line.put("GRP1", sCd);
                    line.put("GRP2", gpCd2);
                    line.put("GRP3", sCd);

                    if (printFlg) {
                        //予算
                        if (!befGpCd2.equals(gpCd2)) {
                            final String budgetMoney = (_param._notPriBudgetMap.containsKey(lmCd)) ? "": String.valueOf(ob._budgetMoney);
                            line.put("BADGET2", budgetMoney);
                        }
                        //実績
                        line.put("MONEY2", String.valueOf(ob._outGoMoney));
                    }

                }

                befGpCd2 = !"".equals(budgetScd) ? budgetScd: sCd;
            }

            //合計欄
            line = getNewLine(lines);
            line.put("INOUT_NAME2", "合計");
            line.put("BADGET3", String.valueOf(totalBudget)); //予算
            line.put("MONEY3", String.valueOf(totalOutgo)); //実績

            //差額欄
            line = getNewLine(lines);
            line.put("INOUT_NAME2", "差額");
            line.put("MONEY3", String.valueOf(totalBudget - totalOutgo));

            totalBudget = 0;
            totalOutgo = 0;
        }

        return lines;
    }

    private String getMoneyInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE_TABLE AS ( ");
        stb.append("     SELECT ");
        stb.append("         ISCH.SCHOOLCD, ");
        stb.append("         ISCH.SCHOOL_KIND, ");
        stb.append("         ISCH.YEAR, ");
        stb.append("         ISCH.INCOME_L_CD, ");
        stb.append("         ISCH.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_INCOME_SCHREG_DAT ISCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_INCOME_DAT INCO ON INCO.SCHOOLCD    = ISCH.SCHOOLCD ");
        stb.append("                                               AND INCO.SCHOOL_KIND = ISCH.SCHOOL_KIND ");
        stb.append("                                               AND INCO.YEAR        = ISCH.YEAR ");
        stb.append("                                               AND INCO.INCOME_L_CD = ISCH.INCOME_L_CD ");
        stb.append("                                               AND INCO.INCOME_M_CD = ISCH.INCOME_M_CD ");
        stb.append("                                               AND INCO.REQUEST_NO  = ISCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             INCO.INCOME_APPROVAL = '1' ");
        stb.append("         AND INCO.INCOME_CANCEL   is null ");
        stb.append("         AND INCO.INCOME_L_CD not in ('99') ");
        stb.append("     GROUP BY ");
        stb.append("         ISCH.SCHOOLCD, ");
        stb.append("         ISCH.SCHOOL_KIND, ");
        stb.append("         ISCH.YEAR, ");
        stb.append("         ISCH.INCOME_L_CD, ");
        stb.append("         ISCH.SCHREGNO ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                              AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                              AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                              AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                              AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                              AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             OUTG.OUTGO_APPROVAL = '1' ");
        stb.append("         AND OUTG.OUTGO_CANCEL   is null ");
        stb.append("         AND OUTG.OUTGO_L_CD not in ('99') ");
        stb.append("         AND OUTG.INCOME_L_CD not in ('98') "); //給付は除く
        stb.append("     GROUP BY ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO ");
        // 納入データ
        stb.append(" ), INCOME_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         ISCH.SCHOOLCD, ");
        stb.append("         ISCH.SCHOOL_KIND, ");
        stb.append("         ISCH.YEAR, ");
        stb.append("         ISCH.INCOME_L_CD, ");
        stb.append("         ISCH.SCHREGNO, ");
        stb.append("         sum(ISCH.INCOME_MONEY) AS TOTAL_INCOME_MONEY ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_INCOME_SCHREG_DAT ISCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_INCOME_DAT INCO ON INCO.SCHOOLCD    = ISCH.SCHOOLCD ");
        stb.append("                                               AND INCO.SCHOOL_KIND = ISCH.SCHOOL_KIND ");
        stb.append("                                               AND INCO.YEAR        = ISCH.YEAR ");
        stb.append("                                               AND INCO.INCOME_L_CD = ISCH.INCOME_L_CD ");
        stb.append("                                               AND INCO.INCOME_M_CD = ISCH.INCOME_M_CD ");
        stb.append("                                               AND INCO.REQUEST_NO  = ISCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             INCO.INCOME_APPROVAL = '1' ");
        stb.append("         AND INCO.INCOME_CANCEL   is null ");
        stb.append("         AND INCO.INCOME_L_CD not in ('99') ");
        stb.append("     GROUP BY ");
        stb.append("         ISCH.SCHOOLCD, ");
        stb.append("         ISCH.SCHOOL_KIND, ");
        stb.append("         ISCH.YEAR, ");
        stb.append("         ISCH.INCOME_L_CD, ");
        stb.append("         ISCH.SCHREGNO ");
        // 支出データ
        stb.append(" ), OUTGO_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO, ");
        stb.append("         sum(OSCH.OUTGO_MONEY) AS TOTAL_OUTGO_MONEY ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                              AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                              AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                              AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                              AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                              AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             OUTG.OUTGO_APPROVAL = '1' ");
        stb.append("         AND OUTG.OUTGO_CANCEL   is null ");
        stb.append("         AND OUTG.OUTGO_L_CD not in ('99') ");
        stb.append("     GROUP BY ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO ");
        // 積立（繰越）データ
        stb.append(" ), CARRY_OVER AS ( ");
        stb.append("     SELECT ");
        stb.append("         CARY.SCHOOLCD, ");
        stb.append("         CARY.SCHOOL_KIND, ");
        stb.append("         CARY.YEAR, ");
        stb.append("         CARY.INCOME_L_CD, ");
        stb.append("         CARY.SCHREGNO, ");
        stb.append("         sum(CARY.CARRY_OVER_MONEY) AS CARRY_OVER_MONEY ");
        stb.append("     FROM ");
        stb.append("         LEVY_CARRY_OVER_DAT CARY ");
        stb.append("     WHERE ");
        stb.append("             CARY.CARRY_CANCEL is null ");
        stb.append("     GROUP BY ");
        stb.append("         CARY.SCHOOLCD, ");
        stb.append("         CARY.SCHOOL_KIND, ");
        stb.append("         CARY.YEAR, ");
        stb.append("         CARY.INCOME_L_CD, ");
        stb.append("         CARY.SCHREGNO ");
        // 返金データ
        stb.append(" ), HENKIN AS ( ");
        stb.append("     SELECT ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO, ");
        stb.append("         sum(OSCH.OUTGO_MONEY) AS HENKIN_MONEY ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                              AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                              AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                              AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                              AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                              AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             OUTG.OUTGO_APPROVAL  = '1' ");
        stb.append("         AND OUTG.OUTGO_CANCEL    is null ");
        stb.append("         AND OUTG.OUTGO_L_CD      in ('99') ");
        stb.append("     	 AND OUTG.REQUEST_NO not in ( ");
        stb.append("                            SELECT ");
        stb.append("                                INS_REQUEST_NO ");
        stb.append("                            FROM ");
        stb.append("                                LEVY_REQUEST_HASUU_WORK_DAT ");
        stb.append("                            WHERE ");
        stb.append("                                    SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("                                AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("                                AND YEAR        = '" + _param._ctrlYear + "' ");
        stb.append(" 			) ");
        stb.append("     GROUP BY ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO ");
        stb.append(" ) ");
        // メインsql
        stb.append(" SELECT ");
        stb.append("     BASE.INCOME_L_CD, ");
        stb.append("     value(LMST.LEVY_L_NAME, '') AS LEVY_L_NAME, ");
        stb.append("     value(INCM.TOTAL_INCOME_MONEY, 0) AS TOTAL_INCOME_MONEY, ");
        stb.append("     value(OUTG.TOTAL_OUTGO_MONEY, 0) AS TOTAL_OUTGO_MONEY, ");
        stb.append("     value(CARY.CARRY_OVER_MONEY, 0) AS CARRY_OVER_MONEY, ");
        stb.append("     value(HKIN.HENKIN_MONEY, 0) AS HENKIN_MONEY ");
        stb.append(" FROM ");
        stb.append("     BASE_TABLE BASE ");
        stb.append("     LEFT JOIN INCOME_DATA INCM ON INCM.SCHOOLCD    = BASE.SCHOOLCD ");
        stb.append("                               AND INCM.SCHOOL_KIND = BASE.SCHOOL_KIND ");
        stb.append("                               AND INCM.YEAR        = BASE.YEAR ");
        stb.append("                               AND INCM.INCOME_L_CD = BASE.INCOME_L_CD ");
        stb.append("                               AND INCM.SCHREGNO    = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN OUTGO_DATA OUTG ON OUTG.SCHOOLCD    = BASE.SCHOOLCD ");
        stb.append("                              AND OUTG.SCHOOL_KIND = BASE.SCHOOL_KIND ");
        stb.append("                              AND OUTG.YEAR        = BASE.YEAR ");
        stb.append("                              AND OUTG.INCOME_L_CD = BASE.INCOME_L_CD ");
        stb.append("                              AND OUTG.SCHREGNO    = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN CARRY_OVER CARY ON CARY.SCHOOLCD     = BASE.SCHOOLCD ");
        stb.append("                              AND CARY.SCHOOL_KIND  = BASE.SCHOOL_KIND ");
        stb.append("                              AND CARY.YEAR         = BASE.YEAR ");
        stb.append("                              AND CARY.INCOME_L_CD  = BASE.INCOME_L_CD ");
        stb.append("                              AND CARY.SCHREGNO     = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN HENKIN HKIN ON HKIN.SCHOOLCD    = BASE.SCHOOLCD ");
        stb.append("                          AND HKIN.SCHOOL_KIND = BASE.SCHOOL_KIND ");
        stb.append("                          AND HKIN.YEAR        = BASE.YEAR ");
        stb.append("                          AND HKIN.INCOME_L_CD = BASE.INCOME_L_CD ");
        stb.append("                          AND HKIN.SCHREGNO    = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN LEVY_L_MST LMST ON LMST.SCHOOLCD    = BASE.SCHOOLCD ");
        stb.append("                              AND LMST.SCHOOL_KIND = BASE.SCHOOL_KIND ");
        stb.append("                              AND LMST.YEAR        = BASE.YEAR ");
        stb.append("                              AND LMST.LEVY_L_CD   = BASE.INCOME_L_CD ");
        stb.append("     WHERE ");
        stb.append("             BASE.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("         AND BASE.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("         AND BASE.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("         AND BASE.SCHREGNO    = ? ");
        stb.append("     ORDER BY ");
        stb.append("         BASE.INCOME_L_CD ");

        return stb.toString();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps  = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        try {
            final String sql = getSchregSql();
            log.debug(" SchregSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo   = rs.getString("SCHREGNO");
                final String hrName     = rs.getString("HR_NAME");
                final String name       = rs.getString("NAME");
                final String zipCd      = rs.getString("GUARD_ZIPCD");
                final String addr1      = rs.getString("GUARD_ADDR1");
                final String addr2      = rs.getString("GUARD_ADDR2");
                final String bankName   = rs.getString("BANKNAME");

                final PrintData printData = new PrintData(schregNo, hrName, name, zipCd, addr1, addr2, bankName);
                retList.add(printData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }


        try {
            final String sql = getMoneyInfoSql();
            log.fatal("moneyInfo_sql" + sql);
            ps = db2.prepareStatement(sql);

            if ("1".equals(_param._useLevyBudget)) {
	            final String sqlBudget = getBudgetSql();
	            log.debug("sqlBudget_sql" + sqlBudget);
	            ps2 = db2.prepareStatement(sqlBudget);
            }

            for (Iterator iterator = retList.iterator(); iterator.hasNext();) {
                final PrintData pd = (PrintData) iterator.next();
                ps.setString(1, pd._schregNo);
                rs = ps.executeQuery();
                pd.carryOverMoneyMap = new HashMap();
                while (rs.next()) {
                    final String incomeLcd      = rs.getString("INCOME_L_CD");
                    final String lName          = rs.getString("LEVY_L_NAME");
                    final int incomeMoney       = rs.getInt("TOTAL_INCOME_MONEY");
                    final int outgoMoney        = rs.getInt("TOTAL_OUTGO_MONEY");
                    final int carryOverMoney    = rs.getInt("CARRY_OVER_MONEY");
                    final int henkinMoney       = rs.getInt("HENKIN_MONEY");
                    final PrintUchiwake printUchiwake = new PrintUchiwake(lName, incomeMoney, outgoMoney, carryOverMoney, henkinMoney);
                    pd.carryOverMoneyMap.put(incomeLcd, rs.getString("CARRY_OVER_MONEY"));

                    pd.totalIncomeMoney    += incomeMoney;
                    pd.totaloutgoMoney     += outgoMoney;
                    pd.totalcarryOverMoney += carryOverMoney;
                    pd.totalHenkinMoney    += henkinMoney;

                    pd.printUchiwakeList.add(printUchiwake);
                }
                DbUtils.closeQuietly(rs);
                
                //端数返金
                final String hasuuSql = hasuuSql(pd._schregNo);
                PreparedStatement hasuuPs = db2.prepareStatement(hasuuSql);
                ResultSet hasuuRs = hasuuPs.executeQuery();
                while (hasuuRs.next()) {
                    final int henkinMoney       = hasuuRs.getInt("HENKIN_MONEY");
                    final PrintUchiwake printUchiwake = new PrintUchiwake("端数繰越", 0, 0, 0, henkinMoney);

                    pd.totalHenkinMoney    += henkinMoney;

                    pd.printUchiwakeList.add(printUchiwake);
                }
                DbUtils.closeQuietly(hasuuRs);
				
                
                
                pd._benefitMoney = getBenefitMoney(db2, pd._schregNo);
//                pd._inComeLcdList = getInComeLcdData(db2, pd._schregNo);
//                pd._outGoingLcdList = getOutGoingLcdData(db2, pd._schregNo);
                pd._inComeMap = getInComeData(db2, pd._schregNo);
                pd._outGoingMap = getOutGoingData(db2, pd._schregNo);

                pd._mergeKeyMap = new TreeMap();
                //incomeのLCDを登録
                for (final Iterator iti = pd._inComeMap.keySet().iterator(); iti.hasNext();) {
                    String ikey = (String) iti.next();
                    List inclist = (List) pd._inComeMap.get(ikey);
                    int listcnt = inclist.size();
                    final String carryOverMoneyS = (String) pd.carryOverMoneyMap.get(ikey);
                    if (NumberUtils.isNumber(carryOverMoneyS)) {
                        listcnt += 2;
                    }
                    listcnt += 1; // ① - ②
                    pd._mergeKeyMap.put(ikey, String.valueOf(listcnt));
                }
                //outgoingのLCDを登録
                for (final Iterator ito = pd._outGoingMap.keySet().iterator(); ito.hasNext();) {
                    String okey = (String) ito.next();
                    List outdlist = (List) pd._outGoingMap.get(okey);
                    int listcnt = outdlist.size();
                    if (pd._mergeKeyMap.containsKey(okey)) {
                        String cnt = (String) pd._mergeKeyMap.get(okey);
                        listcnt += Integer.parseInt(cnt);
                    }
                    pd._mergeKeyMap.put(okey, String.valueOf(listcnt));
                }
                //マージしたリストで、LCD毎(つまり表毎)の利用行数を算出する。
                for (final Iterator itm = pd._mergeKeyMap.keySet().iterator(); itm.hasNext();) {
                    String mkey = (String) itm.next();
                    String cnt = (String) pd._mergeKeyMap.get(mkey);
                    int incnotcnt = pd._inComeMap.containsKey(mkey) ? 0 : 1;
                    int outgnotcnt = pd._outGoingMap.containsKey(mkey) ? 0 : 1;
                    int totalline = Integer.parseInt(cnt) + 1 + 2 + 1 + incnotcnt + outgnotcnt; //出力行数 + head + 合計2行 + 空白行
                    pd._mergeKeyMap.put(mkey, String.valueOf(totalline));
                }

                if ("1".equals(_param._useLevyBudget)) {
	                //予算・実績関連
	                ps2.setString(1, pd._schregNo);
	                rs2 = ps2.executeQuery();
	                String befBudGetLMScd = "";
	                pd._outBudgetMap = new TreeMap();
	                while (rs2.next()) {
	                    final String _outgoLcd  = rs2.getString("LCD");
	                    final String _outgoMcd  = rs2.getString("MCD");
	                    final String _outgoScd  = rs2.getString("SCD");
	                    final String _budgetLcd = rs2.getString("BUDGET_L_CD");
	                    final String _budgetMcd = rs2.getString("BUDGET_M_CD");
	                    final String _budgetScd = rs2.getString("BUDGET_S_CD");
	                    final int _budgetMoney  = rs2.getInt("BUDGET_MONEY");
	                    final int _outGoMoney   = rs2.getInt("OUTGO_MONEY");
	
	                    //予備費のレコードはリストに入れない
	                    if (YOBIHI_CD.equals(_outgoScd)) continue;
	
	                    final String outGoLMScd  = _outgoLcd + _outgoMcd + _outgoScd;
	                    final String budgetLMScd = _budgetLcd + _budgetMcd + _budgetScd;
	
	                    final OutBudgetInfo outB = new OutBudgetInfo(outGoLMScd, budgetLMScd, _budgetMoney, _outGoMoney);
	                    List addList;
	                    if (!pd._outBudgetMap.containsKey(_outgoLcd)) {
	                        addList = new ArrayList();
	                        addList.add(outB);
	                    } else {
	                        addList = (List) pd._outBudgetMap.get(_outgoLcd);
	
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
	                    pd._outBudgetMap.put(_outgoLcd, addList);
	                }
                }
                DbUtils.closeQuietly(rs2);
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.closeQuietly(null, ps2, rs2);
            db2.commit();
        }
        return retList;
    }

    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_ISUU AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_ADDRESS_DAT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), ADDR_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GUARD_ZIPCD, ");
        stb.append("         T1.GUARD_ADDR1, ");
        stb.append("         T1.GUARD_ADDR2 ");
        stb.append("     FROM ");
        stb.append("         GUARDIAN_ADDRESS_DAT T1 ");
        stb.append("     INNER JOIN MAX_ISUU T2 ON T2.SCHREGNO  = T1.SCHREGNO ");
        stb.append("                           AND T2.ISSUEDATE = T1.ISSUEDATE ");
        stb.append(" )");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        if (!_param._henkinDateSemesterRow.isEmpty()) {
            stb.append("     VALUE(HDAT2.HR_NAME, HDAT.HR_NAME) AS HR_NAME, ");
        } else {
            stb.append("     HDAT.HR_NAME, ");
        }
        stb.append("     BASE.NAME, ");
        stb.append("     ADDR.GUARD_ZIPCD, ");
        stb.append("     ADDR.GUARD_ADDR1, ");
        stb.append("     ADDR.GUARD_ADDR2, ");
        stb.append("     BANK.BANKNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                                    AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("                                    AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN ADDR_T ADDR ON ADDR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN REGISTBANK_DAT REGI ON REGI.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("                                  AND REGI.SCHREGNO    = REGD.SCHREGNO ");
        stb.append("                                  AND REGI.SEQ         = '1' ");
        stb.append("     LEFT JOIN BANK_MST BANK ON BANK.BANKCD   = REGI.BANKCD ");
        stb.append("                            AND BANK.BRANCHCD = REGI.BRANCHCD ");
        if (!_param._henkinDateSemesterRow.isEmpty()) {
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD2 ON REGD2.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                                    AND REGD2.YEAR     = '" + KnjDbUtils.getString(_param._henkinDateSemesterRow, "YEAR") + "' ");
            stb.append("                                    AND REGD2.SEMESTER = '" + KnjDbUtils.getString(_param._henkinDateSemesterRow, "SEMESTER") + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT2 ON HDAT2.YEAR    = REGD2.YEAR ");
            stb.append("                                    AND HDAT2.SEMESTER = REGD2.SEMESTER ");
            stb.append("                                    AND HDAT2.GRADE    = REGD2.GRADE ");
            stb.append("                                    AND HDAT2.HR_CLASS = REGD2.HR_CLASS ");
        }
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE    = '" + _param._grade + "' ");
        stb.append("     AND REGD.SCHREGNO NOT IN ( ");
        stb.append("         SELECT ");
        stb.append("             SEISAN_DONE.SCHREGNO ");
        stb.append("         FROM ");
        stb.append("             LEVY_REQUEST_SEISAN_DONE_SCHREG_DAT SEISAN_DONE ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _schregNo;
        final String _hrName;
        final String _name;
        final String _zipCd;
        final String _addr1;
        final String _addr2;
        final String _bankName;
//        List _inComeLcdList;
//        List _outGoingLcdList;
        long _benefitMoney;
        Map _inComeMap;

        final List printUchiwakeList = new ArrayList();
        Map carryOverMoneyMap = new HashMap();
        int totalIncomeMoney    = 0;
        int totaloutgoMoney     = 0;
        int totalcarryOverMoney = 0;
        int totalHenkinMoney    = 0;
        List detailPageList;

        Map _outGoingMap;
        Map _mergeKeyMap;
        Map _outBudgetMap;
        public PrintData(
                final String schregNo,
                final String hrName,
                final String name,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String bankName
        ) {
            _schregNo   = schregNo;
            _hrName     = hrName;
            _name       = name;
            _zipCd      = zipcd;
            _addr1      = addr1;
            _addr2      = addr2;
            _bankName   = bankName;
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

    private class PrintUchiwake {
        final String _lName;
        final int _incomeMoney;
        final int _outgoMoney;
        final int _carryOverMoney;
        final int _henkinMoney;
        PrintUchiwake(final String lName, final int incomeMoney, final int outgoMoney, final int carryOverMoney, final int henkinMoney) {
            _lName = lName;
            _incomeMoney = incomeMoney;
            _outgoMoney = outgoMoney;
            _carryOverMoney = carryOverMoney;
            _henkinMoney = henkinMoney;
        }
    }

    private Map getInComeData(final DB2UDB db2, final String schregno) {
        Map retMap = new HashMap();
        String sql = getInComeDataSql(schregno);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                InComeDetailInfo addwk = new InComeDetailInfo(rs.getString("INCOME_L_CD"), rs.getString("LEVY_L_NAME"), rs.getString("LEVY_M_NAME"), rs.getLong("INCOME_MONEY"));
                if (!retMap.containsKey(rs.getString("INCOME_L_CD"))) {
                    retMap.put(rs.getString("INCOME_L_CD"), new ArrayList());
                }
                List addlist = (List) retMap.get(rs.getString("INCOME_L_CD"));
                addlist.add(addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
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
        stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCS ON INCD.SCHOOLCD    = INCS.SCHOOLCD ");
        stb.append("                                                   AND INCD.SCHOOL_KIND = INCS.SCHOOL_KIND ");
        stb.append("                                                   AND INCD.YEAR        = INCS.YEAR ");
        stb.append("                                                   AND INCD.INCOME_L_CD = INCS.INCOME_L_CD ");
        stb.append("                                                   AND INCD.INCOME_M_CD = INCS.INCOME_M_CD ");
        stb.append("                                                   AND INCD.REQUEST_NO  = INCS.REQUEST_NO ");
        stb.append("                                                   AND INCS.SCHREGNO    = '" + schregno + "' ");
        stb.append("     LEFT JOIN LEVY_M_MST LEVYM ON INCD.SCHOOLCD    = LEVYM.SCHOOLCD ");
        stb.append("                               AND INCD.SCHOOL_KIND = LEVYM.SCHOOL_KIND ");
        stb.append("                               AND INCD.YEAR        = LEVYM.YEAR ");
        stb.append("                               AND INCD.INCOME_L_CD = LEVYM.LEVY_L_CD ");
        stb.append("                               AND INCD.INCOME_M_CD = LEVYM.LEVY_M_CD ");
        stb.append("     LEFT JOIN LEVY_L_MST LEVYL ON INCD.SCHOOLCD    = LEVYL.SCHOOLCD ");
        stb.append("                               AND INCD.SCHOOL_KIND = LEVYL.SCHOOL_KIND ");
        stb.append("                               AND INCD.YEAR        = LEVYL.YEAR ");
        stb.append("                               AND INCD.INCOME_L_CD = LEVYL.LEVY_L_CD ");
        stb.append(" WHERE ");
        stb.append("         INCD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND VALUE(INCD.INCOME_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(INCD.INCOME_CANCEL, '0')   = '0' ");
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

    private class OutGoingDetailInfo {
        final String _levyLCd;
        final String _levyLName;
        final String _levyMCd;
        final String _levyMName;
        final String _levySName;
        final long _outGoingMoney;
        OutGoingDetailInfo(final String levyLCd, final String levyLName, final String levyMCd, final String levyMName, final String levySName, final long outGoingMoney) {
            _levyLCd   = levyLCd;
            _levyLName = levyLName;
            _levyMCd   = levyMCd;
            _levyMName = levyMName;
            _levySName = levySName;
            _outGoingMoney = outGoingMoney;
        }
    }

    private Map getOutGoingData(final DB2UDB db2, final String schregno) {
        Map retMap = new HashMap();
        String sql = getOutGoingDataSql(schregno);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                OutGoingDetailInfo addwk = new OutGoingDetailInfo(rs.getString("OUTGO_L_CD"), rs.getString("LEVY_L_NAME"), rs.getString("OUTGO_M_CD"), rs.getString("LEVY_M_NAME"), rs.getString("LEVY_S_NAME"), rs.getLong("OUTGO_MONEY"));
                if (!retMap.containsKey(rs.getString("OUTGO_L_CD"))) {
                    retMap.put(rs.getString("OUTGO_L_CD"), new ArrayList());
                }
                List addlist = (List) retMap.get(rs.getString("OUTGO_L_CD"));
                addlist.add(addwk);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
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
        stb.append("     LEVYM.LEVY_M_NAME, ");
        stb.append("     OUTS.OUTGO_S_CD, ");
        stb.append("     LEVYS.LEVY_S_NAME, ");
        stb.append("     SUM(VALUE(OUTS.OUTGO_MONEY, 0)) AS OUTGO_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_DAT OUTD ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD    = OUTS.SCHOOLCD ");
        stb.append("                                                  AND OUTD.SCHOOL_KIND = OUTS.SCHOOL_KIND ");
        stb.append("                                                  AND OUTD.YEAR        = OUTS.YEAR ");
        stb.append("                                                  AND OUTD.OUTGO_L_CD  = OUTS.OUTGO_L_CD ");
        stb.append("                                                  AND OUTD.OUTGO_M_CD  = OUTS.OUTGO_M_CD ");
        stb.append("                                                  AND OUTD.REQUEST_NO  = OUTS.REQUEST_NO ");
        stb.append("                                                  AND OUTS.SCHREGNO    = '" + schregno + "' ");
        stb.append("     LEFT JOIN LEVY_S_MST LEVYS ON OUTS.SCHOOLCD    = LEVYS .SCHOOLCD ");
        stb.append("                               AND OUTS.SCHOOL_KIND = LEVYS .SCHOOL_KIND ");
        stb.append("                               AND OUTS.YEAR        = LEVYS .YEAR ");
        stb.append("                               AND OUTS.OUTGO_L_CD  = LEVYS .LEVY_L_CD ");
        stb.append("                               AND OUTS.OUTGO_M_CD  = LEVYS .LEVY_M_CD ");
        stb.append("                               AND OUTS.OUTGO_S_CD  = LEVYS .LEVY_S_CD ");
        stb.append("     LEFT JOIN LEVY_L_MST LEVYL ON OUTD.SCHOOLCD    = LEVYL.SCHOOLCD ");
        stb.append("                               AND OUTD.SCHOOL_KIND = LEVYL.SCHOOL_KIND ");
        stb.append("                               AND OUTD.YEAR        = LEVYL.YEAR ");
        stb.append("                               AND OUTD.OUTGO_L_CD  = LEVYL.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST LEVYM ON OUTS.SCHOOLCD    = LEVYM .SCHOOLCD ");
        stb.append("                               AND OUTS.SCHOOL_KIND = LEVYM .SCHOOL_KIND ");
        stb.append("                               AND OUTS.YEAR        = LEVYM .YEAR ");
        stb.append("                               AND OUTS.OUTGO_L_CD  = LEVYM .LEVY_L_CD ");
        stb.append("                               AND OUTS.OUTGO_M_CD  = LEVYM .LEVY_M_CD ");
        stb.append(" WHERE ");
        stb.append("         OUTD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(OUTD.OUTGO_CANCEL, '0')   = '0' ");
        stb.append("     AND OUTD.OUTGO_L_CD != '99' ");
        stb.append("     AND OUTD.OUTGO_M_CD != '99' ");
        stb.append("     AND OUTD.INCOME_L_CD != '98' ");//給付は除く
        stb.append("     AND OUTD.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND OUTD.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append(" GROUP BY ");
        stb.append("     OUTS.OUTGO_L_CD, ");
        stb.append("     LEVYL.LEVY_L_NAME, ");
        stb.append("     OUTS.OUTGO_M_CD, ");
        stb.append("     LEVYM.LEVY_M_NAME, ");
        stb.append("     OUTS.OUTGO_S_CD, ");
        stb.append("     LEVYS.LEVY_S_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     OUTS.OUTGO_L_CD, ");
        stb.append("     OUTS.OUTGO_M_CD, ");
        stb.append("     OUTS.OUTGO_S_CD ");
        return stb.toString();
    }

    private String hasuuSql(final String schregno) {
    	final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT ");
        stb.append("         OSCH.SCHOOLCD, ");
        stb.append("         OSCH.SCHOOL_KIND, ");
        stb.append("         OSCH.YEAR, ");
        stb.append("         OUTG.INCOME_L_CD, ");
        stb.append("         OSCH.SCHREGNO, ");
        stb.append("         OSCH.OUTGO_MONEY AS HENKIN_MONEY ");
        stb.append("     FROM ");
        stb.append("         LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
        stb.append("                                              AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
        stb.append("                                              AND OUTG.YEAR        = OSCH.YEAR ");
        stb.append("                                              AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
        stb.append("                                              AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
        stb.append("                                              AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
        stb.append("     WHERE ");
        stb.append("             OUTG.OUTGO_APPROVAL  = '1' ");
        stb.append("         AND OUTG.OUTGO_CANCEL    is null ");
        stb.append("         AND OUTG.OUTGO_L_CD      in ('99') ");
        stb.append("     	 AND OUTG.REQUEST_NO in ( ");
        stb.append("                            SELECT ");
        stb.append("                                INS_REQUEST_NO ");
        stb.append("                            FROM ");
        stb.append("                                LEVY_REQUEST_HASUU_WORK_DAT ");
        stb.append("                            WHERE ");
        stb.append("                                    SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("                                AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("                                AND YEAR        = '" + _param._ctrlYear + "' ");
        stb.append(" 			) ");
        stb.append("         AND OSCH.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("         AND OSCH.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("         AND OSCH.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("         AND OSCH.SCHREGNO    = '" + schregno + "' ");


    	return stb.toString();
    }
    
    private long getBenefitMoney(final DB2UDB db2, String schregno) {
        long retmoney = 0;
        String sql = getBenefitMoneySql(schregno);
        log.fatal("benefit sql" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                retmoney = rs.getLong("BENEFIT_MONEY");
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retmoney;
    }

    private String getBenefitMoneySql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     sum(OUTD.REQUEST_GK) as BENEFIT_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_DAT OUTD ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTS ON OUTD.SCHOOLCD    = OUTS.SCHOOLCD ");
        stb.append("                                                  AND OUTD.SCHOOL_KIND = OUTS.SCHOOL_KIND ");
        stb.append("                                                  AND OUTD.YEAR        = OUTS.YEAR ");
        stb.append("                                                  AND OUTD.OUTGO_L_CD  = OUTS.OUTGO_L_CD ");
        stb.append("                                                  AND OUTD.OUTGO_M_CD  = OUTS.OUTGO_M_CD ");
        stb.append("                                                  AND OUTD.REQUEST_NO  = OUTS.REQUEST_NO ");
        stb.append("                                                  AND OUTS.SCHREGNO    = '" + schregno + "' ");
        stb.append(" WHERE ");
        stb.append("         OUTD.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND OUTD.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND OUTD.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("     AND VALUE(OUTD.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(OUTD.OUTGO_CANCEL, '0')   = '0' ");
        stb.append("     AND OUTD.INCOME_L_CD = '98' "); // 98:給付
        stb.append("     AND OUTD.INCOME_M_CD = '98' ");
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

    private String getBudgetSql() {
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
        stb.append("             OSCH.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("         AND OSCH.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("         AND OSCH.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("         AND OSCH.SCHREGNO    = ? ");
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
        stb.append("             BJET.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("         AND BJET.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("         AND BJET.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("         AND BJET.BUDGET_L_CD || BJET.BUDGET_M_CD ");
        stb.append("                             in (SELECT DISTINCT ");
        stb.append("                                     OUTGO_L_CD || OUTGO_M_CD ");
        stb.append("                                 FROM ");
        stb.append("                                     OUTGO_DAT ");
        stb.append("                                 ) ");
        stb.append(" ), MINI_T AS  ( ");
        stb.append("     SELECT ");
        stb.append("         MAIN.LCD, ");
        stb.append("         MAIN.MCD, ");
        stb.append("         MAIN.SCD, ");
        stb.append("         BMEI.BUDGET_L_CD, ");
        stb.append("         BMEI.BUDGET_M_CD, ");
        stb.append("         BMEI.BUDGET_S_CD, ");
        stb.append("         case ");
        stb.append("             when value(OUTG.OUTGO_MONEY, '0') = '0' then BGET2.BUDGET_MONEY ");
        stb.append("             when    BMEI.OUTGO_L_CD = BMEI.BUDGET_L_CD ");
        stb.append("                 and BMEI.OUTGO_M_CD = BMEI.BUDGET_M_CD ");
        stb.append("                 and BMEI.OUTGO_S_CD = BMEI.BUDGET_S_CD then BGET2.BUDGET_MONEY ");
        stb.append("             else BGET1.BUDGET_MONEY ");
        stb.append("         end as BUDGET_MONEY, ");
        stb.append("         value(OUTG.OUTGO_MONEY, '0') as OUTGO_MONEY ");
        stb.append("     FROM ");
        stb.append("         MAIN_LMS MAIN ");
        stb.append("         LEFT JOIN OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = MAIN.SCHOOLCD ");
        stb.append("                                 AND OUTG.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
        stb.append("                                 AND OUTG.YEAR        = MAIN.YEAR ");
        stb.append("                                 AND OUTG.OUTGO_L_CD  = MAIN.LCD ");
        stb.append("                                 AND OUTG.OUTGO_M_CD  = MAIN.MCD ");
        stb.append("                                 AND OUTG.OUTGO_S_CD  = MAIN.SCD ");
        stb.append("         LEFT JOIN LEVY_BUDGET_MEISAI_DAT BMEI ON BMEI.SCHOOLCD    = MAIN.SCHOOLCD ");
        stb.append("                                              AND BMEI.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
        stb.append("                                              AND BMEI.YEAR        = MAIN.YEAR ");
        stb.append("                                              AND BMEI.REQUEST_NO  = OUTG.REQUEST_NO ");
        stb.append("                                              AND BMEI.LINE_NO     = OUTG.LINE_NO ");
        stb.append("                                              AND BMEI.OUTGO_L_CD  = OUTG.OUTGO_L_CD ");
        stb.append("                                              AND BMEI.OUTGO_M_CD  = OUTG.OUTGO_M_CD ");
        stb.append("                                              AND BMEI.OUTGO_S_CD  = OUTG.OUTGO_S_CD ");
        stb.append("         LEFT JOIN LEVY_BUDGET_DAT BGET1 ON BGET1.SCHOOLCD    = MAIN.SCHOOLCD ");
        stb.append("                                        AND BGET1.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
        stb.append("                                        AND BGET1.YEAR        = MAIN.YEAR ");
        stb.append("                                        AND BGET1.BUDGET_L_CD = BMEI.BUDGET_L_CD ");
        stb.append("                                        AND BGET1.BUDGET_M_CD = BMEI.BUDGET_M_CD ");
        stb.append("                                        AND BGET1.BUDGET_S_CD = BMEI.BUDGET_S_CD ");
        stb.append("         LEFT JOIN LEVY_BUDGET_DAT BGET2 ON BGET2.SCHOOLCD    = MAIN.SCHOOLCD ");
        stb.append("                                        AND BGET2.SCHOOL_KIND = MAIN.SCHOOL_KIND ");
        stb.append("                                        AND BGET2.YEAR        = MAIN.YEAR ");
        stb.append("                                        AND BGET2.BUDGET_L_CD = MAIN.LCD ");
        stb.append("                                        AND BGET2.BUDGET_M_CD = MAIN.MCD ");
        stb.append("                                        AND BGET2.BUDGET_S_CD = MAIN.SCD ");
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

    /** 証明書学校データ */
    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _schoolName2;
        final String _schoolTel;
        final String _staffName;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String schoolName2,
                final String schoolTel,
                final String staffName
        ) {
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
            _schoolName2    = schoolName2;
            _schoolTel      = schoolTel;
            _staffName      = staffName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 74954 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private static final String KYUFU_TITLE = "　東京都立学校等　給付型奨学金";

        private final String _grade;
        private final String _henkinDate;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _useFormKNJP909;
        private final String _tesuryo;
        private final String _useLevyBudget; //予実管理使用するか(1：使用、その他：未使用)
        final CertifSchool _certifSchool;
        Map _henkinDateSemesterRow = new HashMap();
        final Map _levyLMstMap;
        final Map _levyMMstMap;
        final Map _levySMstMap;
        final Map _notPriBudgetMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade        = request.getParameter("GRADE");
            _henkinDate   = request.getParameter("HENKIN_DATE");
            _ctrlYear     = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate     = request.getParameter("CTRL_DATE");
            _schoolCd     = request.getParameter("SCHOOLCD");
            _schoolKind   = request.getParameter("SCHOOL_KIND");
            _useFormKNJP909   = request.getParameter("useFormKNJP909");
            _tesuryo      	= getTesuryo(db2);
            _useLevyBudget = request.getParameter("LevyBudget");
            _certifSchool = getCertifSchool(db2);
            _henkinDateSemesterRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT YEAR, SEMESTER FROM SEMESTER_MST WHERE SEMESTER <> '9' AND '" + StringUtils.replace(_henkinDate, "/", "-") + "' BETWEEN SDATE AND EDATE "));
            _levyLMstMap      = getLevyLMst(db2);
            _levyMMstMap      = getLevyMMst(db2);
            _levySMstMap      = getLevySMst(db2);
            _notPriBudgetMap  = getBudgetDat(db2);
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
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("     AND YEAR        = '" + _ctrlYear + "' ");
                stb.append("     AND BUDGET_S_CD = 'AAA' ");
                stb.append("     AND BUDGET_L_CD || BUDGET_M_CD in ( ");
                stb.append("                 SELECT ");
                stb.append("                     BUDGET_L_CD || BUDGET_M_CD ");
                stb.append("                 FROM ");
                stb.append("                     LEVY_BUDGET_DAT ");
                stb.append("                 WHERE ");
                stb.append("                         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("                     AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("                     AND YEAR        = '" + _ctrlYear + "' ");
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
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("     AND YEAR        = '" + _ctrlYear + "' ");

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
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("     AND YEAR        = '" + _ctrlYear + "' ");

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
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("     AND YEAR        = '" + _ctrlYear + "' ");

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
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND SCHOOL_KIND IN ('" + _schoolKind + "', '99') ");
                stb.append("     AND YEAR        = '" + _ctrlYear + "' ");
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

        /** 証明書学校データ */
        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = new CertifSchool("", "", "", "", "", "");
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '147' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName     = StringUtils.defaultString(rs.getString("SCHOOL_NAME"));
                    final String jobName        = StringUtils.defaultString(rs.getString("JOB_NAME"));
                    final String principalName  = StringUtils.defaultString(rs.getString("PRINCIPAL_NAME"));
                    final String schoolName2    = StringUtils.defaultString(rs.getString("REMARK1"));
                    final String schoolTel      = StringUtils.defaultString(rs.getString("REMARK2"));
                    final String staffName      = StringUtils.defaultString(rs.getString("REMARK3"));
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, schoolName2, schoolTel, staffName);
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
}

// eof
