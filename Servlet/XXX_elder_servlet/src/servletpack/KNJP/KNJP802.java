/*
 * $Id$
 *
 * 作成日: 2018/03/02
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP802 {

    private static final Log log = LogFactory.getLog(KNJP802.class);
    private static final String[] _monthArray = {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};

    boolean _hasData;

    BeforeGrpDataCls _grpchkObj;
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
        final List list = getList(db2);
        if ("2".equals(_param._output)) {
            svf.VrSetForm("KNJP802.frm", 4);
            settitle(db2, svf);
        }

        _grpchkObj = null;
        for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
            final Student studentdat = (Student) iterator1.next();
            if ("1".equals(_param._output)) {
                svf.VrSetForm("KNJP802.frm", 4);
                settitle(db2, svf);
            }
            int lineCnt = 1;
            for (Iterator iterator2 = studentdat._outputlist.iterator();iterator2.hasNext();) {
                if (lineCnt > 33) {
                    lineCnt = 1;
                    if ("1".equals(_param._output)) {
                        svf.VrSetForm("KNJP802.frm", 4);
                        settitle(db2, svf);
                    }
                }
                final Map datamap = (Map)iterator2.next();
                outsvfLine(svf, studentdat, datamap, lineCnt);
                _hasData = true;
                svf.VrEndRecord();
                lineCnt++;
            }
        }
    }

    private void settitle(final DB2UDB db2, final Vrw32alp svf) {
        String datasetwk = "";
        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._Year + "/04/01");
        datasetwk = setYear + "度クラス別生徒納入状況一覧";
        svf.VrsOut("TITLE", datasetwk);

        String calwkstr = _param._ctrlDate;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(calwkstr));
        datasetwk = KNJ_EditDate.h_format_JP(db2, calwkstr) + "現在";
        svf.VrsOut("PRINT_DATE", datasetwk);
        svf.VrsOut("HR_NAME_HEADER", "学籍番号");
    }

    private void outsvfLine(final Vrw32alp svf, final Student stwk, final Map datamap, final int lineCnt) {

        //GRP1
        final String grp1str = stwk._schregno;
        final String grp1SubStr = stwk._hrName + "-" + stwk._attendno;;

        if (lineCnt == 1) {
            svf.VrsOut("HR_NAME", grp1str);
        } else if (lineCnt == 2) {
            svf.VrsOut("HR_NAME", "(" + grp1SubStr + ")");
        }

        //GRP2
        final String grp2str = stwk._name;
        final String grp2SubStr = stwk._nameKana;
        if (lineCnt == 1) {
            printByLength(svf, "NAME", grp2SubStr, new int[] {10, 20, 30});
        } else if (lineCnt == 2) {
            printByLength(svf, "NAME", grp2str, new int[] {10, 20, 30});
        }

        //GRP3
        boolean noukiflg = false;

        //collect_m_name
        int setdispsize = 20;
        final String grp3str = (String)datamap.get("COLLECT_M_NAME");
        if (null != grp3str) {
            if (_grpchkObj == null || (_grpchkObj != null && !_grpchkObj.chkGrpData(3, grp1str, grp2str, grp3str))) {
                final Set kset = datamap.keySet();
                if (kset.contains("COLLECT_M_NAME")) {
                    if (grp3str != null && !"".equals(grp3str)) {
                        if (KNJ_EditEdit.getMS932ByteLength(grp3str) <= setdispsize) {
                            svf.VrsOut("CLASS_NAME1", grp3str);
                        } else {
                            String[] dispstrcutwk = KNJ_EditEdit.get_token(grp3str, setdispsize, 2);
                            svf.VrsOut("CLASS_NAME1", dispstrcutwk[0]);
                        }
                    }
                }
            } else if (_grpchkObj != null && _grpchkObj.chkGrpData(3, grp1str, grp2str, grp3str)) {
                if (KNJ_EditEdit.getMS932ByteLength(grp3str) > setdispsize) {
                    if (_grpchkObj._grp3samecnt == 1) {
                        String[] dispstrcutwk = KNJ_EditEdit.get_token(grp3str, setdispsize, 2);
                        svf.VrsOut("CLASS_NAME1", dispstrcutwk[1]);
                    }
                }
            }
        }
        String datasetwk = setSvfData(svf, "CLASS_NAME2", datamap, "SUB_TATLE");
        if ("納期限".equals(datasetwk) || "入金日付".equals(datasetwk)) {
            noukiflg = true;
        }

        if (_grpchkObj == null) {
            _grpchkObj = new BeforeGrpDataCls(grp1str, grp2str, grp3str);
        }
        _grpchkObj.chgGrpData(svf, grp1str, grp2str, grp3str);

        //sub_tatle

        //4月～3月
        if (!noukiflg) {
            setSvfData(svf, "MONEY1", datamap, "COLLECT_MONTH_04");
            setSvfData(svf, "MONEY2", datamap, "COLLECT_MONTH_05");
            setSvfData(svf, "MONEY3", datamap, "COLLECT_MONTH_06");
            setSvfData(svf, "MONEY4", datamap, "COLLECT_MONTH_07");
            setSvfData(svf, "MONEY5", datamap, "COLLECT_MONTH_08");
            setSvfData(svf, "MONEY6", datamap, "COLLECT_MONTH_09");
            setSvfData(svf, "MONEY7", datamap, "COLLECT_MONTH_10");
            setSvfData(svf, "MONEY8", datamap, "COLLECT_MONTH_11");
            setSvfData(svf, "MONEY9", datamap, "COLLECT_MONTH_12");
            setSvfData(svf, "MONEY10", datamap, "COLLECT_MONTH_01");
            setSvfData(svf, "MONEY11", datamap, "COLLECT_MONTH_02");
            setSvfData(svf, "MONEY12", datamap, "COLLECT_MONTH_03");

            //計
            setSvfData(svf, "MONEY13", datamap, "COLLECT_MONTH_TOTAL");
        } else {
            setSvfData(svf, "DATE1", datamap, "COLLECT_MONTH_04");
            setSvfData(svf, "DATE2", datamap, "COLLECT_MONTH_05");
            setSvfData(svf, "DATE3", datamap, "COLLECT_MONTH_06");
            setSvfData(svf, "DATE4", datamap, "COLLECT_MONTH_07");
            setSvfData(svf, "DATE5", datamap, "COLLECT_MONTH_08");
            setSvfData(svf, "DATE6", datamap, "COLLECT_MONTH_09");
            setSvfData(svf, "DATE7", datamap, "COLLECT_MONTH_10");
            setSvfData(svf, "DATE8", datamap, "COLLECT_MONTH_11");
            setSvfData(svf, "DATE9", datamap, "COLLECT_MONTH_12");
            setSvfData(svf, "DATE10", datamap, "COLLECT_MONTH_01");
            setSvfData(svf, "DATE11", datamap, "COLLECT_MONTH_02");
            setSvfData(svf, "DATE12", datamap, "COLLECT_MONTH_03");

            //計
            setSvfData(svf, "DATE13", datamap, "COLLECT_MONTH_TOTAL");
        }

    }

    private void printByLength(final Vrw32alp svf, final String fieldPrefix, final String value, final int[] pattern) {
        //バイト長取得
        final int byteLength = KNJ_EditEdit.getMS932ByteLength(value);

        //フィールド名末尾の数字を取得
        int fieldCnt = 1;
        for (int threshold : pattern) {
            if (threshold >= byteLength) {
                break;
            }
            fieldCnt++;
        }

        //印字
        final String fieldName = fieldPrefix + fieldCnt;
        svf.VrsOut(fieldName, value);
    }

    private class BeforeGrpDataCls {
        private String _grp1;
        private String _grp2;
        private String _grp3;
        private int _grp1cnt;
        private int _grp2cnt;
        private int _grp3cnt;
        private int _grp1samecnt;
        private int _grp2samecnt;
        private int _grp3samecnt;

        BeforeGrpDataCls(final String grp1, final String grp2, final String grp3) {
            _grp1 = grp1;
            _grp1samecnt = 0;
            _grp1cnt = 0;
            _grp2 = grp2;
            _grp2samecnt = 0;
            _grp2cnt = 0;
            _grp3 = grp3;
            _grp3samecnt = 0;
            _grp3cnt = 0;
        }
        private boolean chkGrpData(final int chkptn, final String grp1, final String grp2, final String grp3) {
            boolean retflg = true;
            if (chkptn >=1 && !grp1.equals(_grp1)) {
                retflg = false;
            }
            if (chkptn >= 2 && !grp2.equals(_grp2)) {
                retflg = false;
            }
            if (chkptn >= 3 && !grp3.equals(_grp3)) {
                retflg = false;
            }
            return retflg;
        }
        private boolean chgGrpData(final Vrw32alp svf, final String grp1, final String grp2, final String grp3) {
            boolean retflg = true;
            if (!grp1.equals(_grp1)) {
                svf.VrsOut("GRP1", String.valueOf(++_grp1cnt));
                _grp1 = grp1;
                retflg = false;
                _grp1samecnt = 1;
            } else {
                svf.VrsOut("GRP1", String.valueOf(_grp1cnt));
                _grp1samecnt++;
            }
            if (!retflg || !grp2.equals(_grp2)) {
                svf.VrsOut("GRP2", String.valueOf(++_grp2cnt));
                _grp2 = grp2;
                retflg = false;
                _grp2samecnt = 1;
            } else {
                svf.VrsOut("GRP2", String.valueOf(_grp2cnt));
                _grp1samecnt++;
            }
            if (null != grp3 && (!retflg || !grp3.equals(_grp3))) {
                svf.VrsOut("GRP3", String.valueOf(++_grp3cnt));
                _grp3 = grp3;
                retflg = false;
                _grp3samecnt = 1;
            } else {
                svf.VrsOut("GRP3", String.valueOf(_grp3cnt));
                _grp3samecnt++;
            }
            return retflg;
        }
    }
    private String setSvfData(final Vrw32alp svf, final String svffmtname, final Map datamap, final String setDataName) {
        String datasetwk = "";
        final Set kset = datamap.keySet();
        if (kset.contains(setDataName)) {
            datasetwk = (String)datamap.get(setDataName);
            if (datasetwk != null && !"".equals(datasetwk)) {
                svf.VrsOut(svffmtname, datasetwk);
            }
        }
        return datasetwk;
    }

    private List getList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        PreparedStatement ps3 = null;
        ResultSet rs3 = null;
        PreparedStatement ps4 = null;
        ResultSet rs4 = null;
        PreparedStatement ps5 = null;
        ResultSet rs5 = null;

        try {
            //生徒数分ループ
            for (int ia = 0; ia < _param._category_selected.length; ia++) {
                if (_param._category_selected[ia] == null || "".equals(_param._category_selected[ia])) continue;
                final String schregno = _param._category_selected[ia];
                Student studentdat = new Student(db2, schregno);

                boolean hasDatawk = false;
                //支援、補助、学校減免取得
                final String sql1 = sqlReductionInfo(schregno);
                log.debug(" sql1 =" + sql1);
                ps1 = db2.prepareStatement(sql1);
                rs1 = ps1.executeQuery();
                String slipReducTotalMoney = "0";
                String allReducTotalMoney = "0";
                   String refundTotalMoney = "0";
                while (rs1.next()) {
                    //後の設定はputするだけ。ただ、加算する必要があるので、取得して加算して設定が必要。
                    //$reducMonthTotalMoney

                    //支援、補助、減免の月合計。後で、入金必要額から引く(相殺フラグがあるもののみ)
                    if ("1".equals(rs1.getString("OFFSET_FLG"))) {

                           //伝票単位
                           String slipReducMoney = "";
                           if (studentdat._slipReducMonthTotalMoney.containsKey(rs1.getString("SLIP_NO") + "-" + rs1.getString("PLAN_MONTH"))) {
                            final String sum_moneywk = (String)studentdat._slipReducMonthTotalMoney.get(rs1.getString("SLIP_NO") + "-" + rs1.getString("PLAN_MONTH"));
                            slipReducMoney = String.valueOf(Long.parseLong(sum_moneywk) + (rs1.getString("PLAN_MONEY") != null ? Long.parseLong(rs1.getString("PLAN_MONEY")) : 0));
                        } else {
                            slipReducMoney = rs1.getString("PLAN_MONEY") != null ? rs1.getString("PLAN_MONEY") : "0";
                        }
                           studentdat._slipReducMonthTotalMoney.put(rs1.getString("SLIP_NO") + "-" + rs1.getString("PLAN_MONTH"), slipReducMoney);

                           //伝票単位 横計
                           slipReducTotalMoney = String.valueOf(Long.parseLong(slipReducTotalMoney) + Long.parseLong(rs1.getString("PLAN_MONEY")));
                        studentdat._slipReducMonthTotalMoney.put("99", slipReducTotalMoney);


                        //伝票合算
                        String allReducMoney = "";
                        if (studentdat._allReducMonthTotalMoney.containsKey(rs1.getString("PLAN_MONTH"))) {
                            final String sum_moneywk = (String)studentdat._allReducMonthTotalMoney.get(rs1.getString("PLAN_MONTH"));
                            allReducMoney = String.valueOf(Long.parseLong(sum_moneywk) + (rs1.getString("PLAN_MONEY") != null ? Long.parseLong(rs1.getString("PLAN_MONEY")) : 0));
                        } else {
                            allReducMoney = rs1.getString("PLAN_MONEY") != null ? rs1.getString("PLAN_MONEY") : "0";
                        }
                           studentdat._allReducMonthTotalMoney.put(rs1.getString("PLAN_MONTH"), allReducMoney);

                           //伝票合算 横計
                           allReducTotalMoney = String.valueOf(Long.parseLong(allReducTotalMoney) + Long.parseLong(rs1.getString("PLAN_MONEY")));
                        studentdat._allReducMonthTotalMoney.put("99", allReducTotalMoney);
                    }

                    //還付金
                    if ("1".equals(rs1.getString("REFUND_FLG"))) {
                        String refundMoney = "";
                           if (studentdat._refundMoneyArr.containsKey(rs1.getString("PLAN_MONTH"))) {
                            final String sum_moneywk = (String)studentdat._refundMoneyArr.get(rs1.getString("PLAN_MONTH"));
                            refundMoney = String.valueOf(Long.parseLong(sum_moneywk) + (rs1.getString("PLAN_MONEY") != null ? Long.parseLong(rs1.getString("PLAN_MONEY")) : 0));
                        } else {
                            refundMoney = rs1.getString("PLAN_MONEY") != null ? rs1.getString("PLAN_MONEY") : "0";
                        }
                           studentdat._refundMoneyArr.put(rs1.getString("PLAN_MONTH"), refundMoney);

                           refundTotalMoney = String.valueOf(Long.parseLong(refundTotalMoney) + Long.parseLong(rs1.getString("PLAN_MONEY")));
                        studentdat._refundMoneyArr.put("99", refundTotalMoney);
                    }

                    final String planMoney = (rs1.getString("PLAN_MONEY") != null && !"".equals(rs1.getString("PLAN_MONEY"))) ? rs1.getString("PLAN_MONEY") : "";
                    RedctionDetailDataCls adddetail = new RedctionDetailDataCls(rs1.getString("PLAN_MONTH"), planMoney);
                    addDetailData(studentdat._reductionInfo, rs1.getString("SLIP_NO"), rs1.getString("SORT"), rs1.getString("KOUMOKU"), rs1.getString("SUB_TATLE"), adddetail);
                }

                //メイン（各伝票情報）
                ArrayList nonCheckCd = new ArrayList();
                nonCheckCd.add("101");
                ArrayList setIdArr = new ArrayList();
                boolean jugyouryouFlg = false;
                boolean jugyouryouFlg2 = false;
                boolean nyuugakukinFlg = false;
                boolean nyuugakukinFlg2 = false;
                int befChangFlg = 0;
                String befSlipNo = "";
                Map setData = null;

                if (!"".equals(schregno)) {
                    setData = new LinkedMap();
                    befSlipNo = "";
                    befChangFlg = 0;
                    jugyouryouFlg = false;
                    jugyouryouFlg2 = false;
                    nyuugakukinFlg = false;
                    nyuugakukinFlg2 = false;
                    String collectmname_bak = "";

                    final String sql2 = sqlMainQuery(schregno);
                    log.debug(" sql2 =" + sql2);
                    ps2 = db2.prepareStatement(sql2);
                    rs2 = ps2.executeQuery();

                    while (rs2.next()) {
                        final mainQDataCls rdat = new mainQDataCls(rs2.getString("SLIP_NO"), rs2.getInt("COLLECT_L_CD"), rs2.getString("SHOW_ORDER"), rs2.getInt("COLLECT_M_CD"),rs2.getString("COLLECT_M_NAME"),rs2.getInt("PLAN_PAID"), rs2.getString("JUGYOURYOU_FLG"),rs2.getString("PLAN_MONTH"), rs2.getString("PLAN_MONEY"));
                        // データセット
                        //伝票内の項目が変わったらデータをセットして初期化
                        if (befChangFlg != 0 && (befChangFlg != rdat._plan_paid)) {
                            if (!"2".equals(_param._output)) {
                                studentdat._outputlist.add(setData);  //各種情報
                            }
                            setData = new LinkedMap();
                        }

                        //支援金、補助金、減免情報をセット
                        if ((jugyouryouFlg || nyuugakukinFlg) && (!"".equals(befSlipNo) && !befSlipNo.equals(rdat._slipno))) {
                            for (final Iterator ri_it = studentdat._reductionInfo.iterator();ri_it.hasNext();) {
                                final ReductionDataCls rdcdat = (ReductionDataCls) ri_it.next();

                                if (befSlipNo.equals(rdcdat._slipno)) {
                                    setData = new LinkedMap();
                                    setData.put("ROWSPAN", "1");
                                    setData.put("disPlanCollect", "1");

                                    //名称//
                                    setData.put("COLLECT_M_NAME", rdcdat._koumoku);
                                    setData.put("SUB_TATLE", rdcdat._subtatle);

                                    for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                        final int idxwk = rdcdat.findDetailIndex(_monthArray[ma_idx]);
                                        setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], idxwk >= 0 ? ((RedctionDetailDataCls)rdcdat._reductiondetail.get(idxwk))._planmoney : "0");
                                    }
                                    final int totalidxwk = rdcdat.findDetailIndex("99");
                                    setData.put("COLLECT_MONTH_TOTAL",  (totalidxwk >=0 ? ((RedctionDetailDataCls)rdcdat._reductiondetail.get(totalidxwk))._planmoney : "0"));

                                    if (!"2".equals(_param._output)) {
                                        studentdat._outputlist.add(setData);  //支援金、補助金、減免情報
                                    }
                                    setData = new LinkedMap();
                                }
                            }
                            jugyouryouFlg = false;
                            nyuugakukinFlg = false;
                        }

                        //電票情報セット(入金必要額、入金月の累計、入金、入金日)
                        if (!"".equals(befSlipNo) && !befSlipNo.equals(rdat._slipno)) {
                            final Map slipTotalInfo = new LinkedMap();
                            long firstSetMoney = 0;
                            String totalmoneystr = "";
                            String commaelementstr = "";
                            String sql3 = getMainQuery2(schregno, befSlipNo);
                            log.debug(" sql3 =" + sql3);
                            ps3 = db2.prepareStatement(sql3);
                            rs3 = ps3.executeQuery();
                            SlipTotalCls sliptotal1 = null;
                            //入金必要額を取得
                            while (rs3.next()) {
                                sliptotal1 = new SlipTotalCls(rs3.getString("SORT"), rs3.getString("M_NAME"));
                                totalmoneystr = "";
                                //必要額から支援、補助、学校減免を引く
                                if ("1".equals(sliptotal1._sort) && (jugyouryouFlg2 || nyuugakukinFlg2)) {
                                    final long totalmoneywk;
                                    if (studentdat._slipReducMonthTotalMoney.get(befSlipNo + "-" + rs3.getString("PLAN_MONTH")) != null) {
                                        totalmoneywk = Long.parseLong(rs3.getString("ELEMENT")) - Long.parseLong((String)studentdat._slipReducMonthTotalMoney.get(befSlipNo + "-" + rs3.getString("PLAN_MONTH")));
                                    } else {
                                        totalmoneywk = Long.parseLong(rs3.getString("ELEMENT"));
                                    }

                                    totalmoneystr = String.valueOf(totalmoneywk);
                                } else {
                                    totalmoneystr = rs3.getString("ELEMENT");
                                }

                                //金額カンマ区切り,
                                if (!"3".equals(sliptotal1._sort)) {
                                    commaelementstr = ((totalmoneystr != null && totalmoneystr.length() > 0) ? totalmoneystr : "0");
                                } else {
                                    commaelementstr = totalmoneystr != null && totalmoneystr.length() > 0 ? totalmoneystr.substring(0, 4) + "/" + totalmoneystr.substring(4, 6) + "/" + totalmoneystr.substring(6, 8): "";
                                }
                                if ("1".equals(sliptotal1._sort) && "04".equals(rs3.getString("PLAN_MONTH"))) firstSetMoney = Long.parseLong(totalmoneystr);
                                addTotalData(slipTotalInfo, sliptotal1, rs3.getString("PLAN_MONTH"), commaelementstr, totalmoneystr);
                            }
                            //電票合算情報セット(入金必要額、入金月の累計、入金、入金日付)
                            List sortArr = new ArrayList();
                            Set kset = slipTotalInfo.keySet();
                            String nyukinSuffix = "（" + befSlipNo.substring(befSlipNo.length() - 3) + "）";
                            for (final Iterator it = kset.iterator(); it.hasNext();) {
                                final String kstr = (String)it.next();
                                sliptotal1 = (SlipTotalCls)slipTotalInfo.get(kstr);
                                setData = new LinkedMap();
                                sortArr.add(kstr);
                                setData.put("COLLECT_M_NAME", "入金額" + nyukinSuffix);
                                if ("1".equals(kstr)) {
                                    setData.put("disPlanCollect", "1");
                                    setData.put("ROWSPAN", "4");
                                }

                                // データセット
                                //入金月の累計
                                if ("2".equals(kstr)) {
                                    setData.put("SUB_TATLE", "入金必要額累計");
                                    //foreach($model->monthArray as $key => $month) {
                                    long befCuTotal = 0;
                                    for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                        if ("04".equals(_monthArray[ma_idx])) {
                                            setData.put("COLLECT_MONTH_04", String.valueOf(firstSetMoney));
                                            befCuTotal = firstSetMoney;
                                        } else {
                                            int intMonth = Integer.parseInt(_monthArray[ma_idx]) == 1 ? 13 : Integer.parseInt(_monthArray[ma_idx]);
                                            String befMonth = new DecimalFormat("00").format(intMonth -1);
                                            final int cutotalwkidx1 = sliptotal1.findDetailIndex(befMonth);
                                            final String cutotalwk1 =  cutotalwkidx1 >= 0 ? ((SlipTotalDetailCls)sliptotal1._slipttldetail.get(cutotalwkidx1))._element : "0";
                                            final int cutotalwkidx2 = ((SlipTotalCls)slipTotalInfo.get("1")).findDetailIndex(_monthArray[ma_idx]);
                                            final String cutotalwk2 =  cutotalwkidx2 >= 0 ? ((SlipTotalDetailCls)((SlipTotalCls)slipTotalInfo.get("1"))._slipttldetail.get(cutotalwkidx2))._element : "0";
                                            final long setCuTotal = befCuTotal - Long.parseLong(cutotalwk1) + Long.parseLong(cutotalwk2);
                                            final String plmchk =  cutotalwkidx2 >= 0 ? ((SlipTotalDetailCls)((SlipTotalCls)slipTotalInfo.get("1"))._slipttldetail.get(cutotalwkidx2))._commaelement : "";
                                            if ("0".equals(plmchk)) {
                                                setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], "0");
                                            } else {
                                                setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], String.valueOf(setCuTotal));
                                            }
                                            befCuTotal = setCuTotal;
                                        }
                                    }
                                    final int cutotalwkidx3 = ((SlipTotalCls)slipTotalInfo.get("2")).findDetailIndex("03");
                                    final String cutotalwk3 =  cutotalwkidx3 >= 0 ? ((SlipTotalDetailCls)sliptotal1._slipttldetail.get(cutotalwkidx3))._element : "0";
                                    final long setCuTotal = befCuTotal - Long.parseLong(cutotalwk3);
                                    setData.put("COLLECT_MONTH_TOTAL", String.valueOf(setCuTotal));

                                    if (!"2".equals(_param._output)) {
                                        studentdat._outputlist.add(setData); // 入金額(入金月累計)
                                    }
                                    setData = new LinkedMap();
                                    setData.put("COLLECT_M_NAME", "入金額" + nyukinSuffix);
                                }
                                //名称セット
                                setData.put("SUB_TATLE", sliptotal1._mname);

                                //各項目セット
                                for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                    final int eleaddidx = sliptotal1.findDetailIndex(_monthArray[ma_idx]);
                                    SlipTotalDetailCls eleaddobj = null;
                                    if (eleaddidx >= 0) eleaddobj = (SlipTotalDetailCls)sliptotal1._slipttldetail.get(eleaddidx);
                                    if ("1".equals(sliptotal1._sort)  || "2".equals(sliptotal1._sort)) {
                                        setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], (eleaddobj != null && eleaddobj._element.length() > 0) ? eleaddobj._element: "0");
                                    } else {
                                        setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], (eleaddobj != null && eleaddobj._element.length() > 0) ? eleaddobj._element: "");
                                    }
                                }
                                final int mtotalidx = sliptotal1.findDetailIndex("99");
                                final String mtotalwk = (mtotalidx >= 0 ? ((SlipTotalDetailCls)sliptotal1._slipttldetail.get(mtotalidx))._commaelement : "0");
                                if (mtotalwk != null && !"".equals(mtotalwk) && !"3".equals(kstr)) {
                                    setData.put("COLLECT_MONTH_TOTAL", mtotalwk);
                                }else {
                                    setData.put("COLLECT_MONTH_TOTAL", "");
                                }
                                studentdat._outputlist.add(setData); // 入金額(入金必要額、入金額計)
                            }
                            //入金日がないときは、空行をセット
                            if (!sortArr.contains("3")) {
                                setData = new LinkedMap();
                                setData.put("COLLECT_M_NAME", "入金額" + nyukinSuffix);
                                setData.put("SUB_TATLE", "入金日付");
                                studentdat._outputlist.add(setData); // 入金額(入金日付)
                            }
                            setData = new LinkedMap();
                            jugyouryouFlg2 = false;
                            nyuugakukinFlg2 = false;
                        }


                        // データ作成
                        //ROWSPAN
                        if (rdat._plan_paid == 1) {
                            setData.put("ROWSPAN", "2");
                            setData.put("disPlanCollect", "1");
                        } else {
                            setData.put("ROWSPAN", "1");
                            setData.put("disPlanCollect", "");
                        }

                        //0埋め
                        final String clcd = new DecimalFormat("00").format(rdat._collect_l_cd);
                        final String cmcd = new DecimalFormat("00").format(rdat._collect_m_cd);

                        //金額カンマ区切り
                        String pm = "";//rs2.getString("PLAN_MONEY")
                        if (!"102".equals(cmcd)) {
                            pm = (rdat._plan_money != null && rdat._plan_money.length() > 0) ? String.valueOf(Integer.parseInt(rdat._plan_money)): "0";
                        } else {
                            //COLLECT_M_CD"102"は納期限日付
                            pm = (rdat._plan_money != null && rdat._plan_money.length() > 0) ? rdat._plan_money.substring(0, 4) + "/" + rdat._plan_money.substring(4, 6) + "/" + rdat._plan_money.substring(6, 8): "";
                        }
                        //IDセット
                        final String setMonth = "99".equals(rdat._plan_month) ? "TOTAL": rdat._plan_month;
                        final String setChkId = rdat._slipno + "-" + clcd + "-" + cmcd;
                        String setId    = setChkId + "-" + setMonth;
                        setIdArr.add(setId);//更新用

                        //項目名称
                        //項目（計画、入金額）
                        setData.put("SUB_TATLE", rdat._plan_paid == 1 ? "計画": "入金額");
                        //納期限はチェックボックスを付属
                        if ("102".equals(clcd)) {
                            //102の前レコードで持つM_NAMEを利用する。
                            setData.put("COLLECT_M_NAME", collectmname_bak);
                            setData.put("SUB_TATLE", "納期限");
                        } else {
                            setData.put("COLLECT_M_NAME", rdat._collect_m_name);
                            //102の前レコードで持つM_NAMEを保持する。
                            collectmname_bak = rdat._collect_m_name;
                        }

                        //各項目テキストボックス（チェックon時のみ表示）
                        //横計以外にセット
                        setData.put("COLLECT_MONTH_" + setMonth, pm);

                        if ("1".equals(rdat._jyugyouryou_flg)) jugyouryouFlg = true;
                        if ("1".equals(rdat._jyugyouryou_flg)) jugyouryouFlg2 = true;
                        if ("2".equals(rdat._jyugyouryou_flg)) nyuugakukinFlg = true;
                        if ("2".equals(rdat._jyugyouryou_flg)) nyuugakukinFlg2 = true;
                        befSlipNo   = rdat._slipno;
                        befChangFlg = rdat._plan_paid;
                        hasDatawk = true;
                    }//sqlFin
                    //SQLを回しながら最終伝票行はセット出来ないので（SQLの取得方法のため）最終情報をセット
                    if (hasDatawk) {
                        if (!"".equals(befSlipNo) && setData != null) {
                            if (!"2".equals(_param._output)) {
                                studentdat._outputlist.add(setData);  //各種項目
                            }
                        }

                        //最終伝票行に授業料が含まれている際、（支援金、補助金、減免情報）をセット
                        if ((jugyouryouFlg || nyuugakukinFlg)) {
                            for (final Iterator ri_it = studentdat._reductionInfo.iterator();ri_it.hasNext();) {
                                final ReductionDataCls rdcdat = (ReductionDataCls) ri_it.next();

                                if (befSlipNo.equals(rdcdat._slipno)) {
                                    setData = new LinkedMap();
                                    setData.put("ROWSPAN", "1");
                                    setData.put("disPlanCollect", "1");

                                    //名称
                                    setData.put("COLLECT_M_NAME", rdcdat._koumoku);
                                    setData.put("SUB_TATLE", rdcdat._subtatle);

                                    for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                        final int idxwk = rdcdat.findDetailIndex(_monthArray[ma_idx]);
                                        setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], idxwk >= 0 ? ((RedctionDetailDataCls)rdcdat._reductiondetail.get(idxwk))._planmoney : "0");
                                    }
                                    final int totalidxwk = rdcdat.findDetailIndex("99");
                                    setData.put("COLLECT_MONTH_TOTAL",  (totalidxwk >=0 ? ((RedctionDetailDataCls)rdcdat._reductiondetail.get(totalidxwk))._planmoney : "0"));

                                    if (!"2".equals(_param._output)) {
                                        studentdat._outputlist.add(setData);  //支援金、補助金、減免情報(最終伝票行に授業料が含まれている際)
                                    }
                                    setData = new LinkedMap();
                                }
                            }
                            jugyouryouFlg = false;
                            nyuugakukinFlg = false;
                        }

                        //最終電票情報取得
                        Map slipTotalInfo = new LinkedMap();
                        long firstSetMoney = 0;
                        String totalmoneystr = "";
                        String commaelementstr = "";
                        String sql4 = getMainQuery2(schregno, befSlipNo);
                        log.debug(" sql4 =" + sql4);
                        ps4 = db2.prepareStatement(sql4);
                        rs4 = ps4.executeQuery();
                        SlipTotalCls sliptotal2 = null;
                        //入金必要額を取得
                        while (rs4.next()) {
                            sliptotal2 = new SlipTotalCls(rs4.getString("SORT"), rs4.getString("M_NAME"));
                            totalmoneystr = "";
                            //必要額から支援、補助、学校減免を引く
                            if ("1".equals(sliptotal2._sort) && (jugyouryouFlg2 || nyuugakukinFlg2)) {
                                final long totalmoneywk;
                                if (studentdat._slipReducMonthTotalMoney.get(befSlipNo + "-" + rs4.getString("PLAN_MONTH")) != null) {
                                    totalmoneywk = Long.parseLong(rs4.getString("ELEMENT")) - Long.parseLong((String)studentdat._slipReducMonthTotalMoney.get(befSlipNo + "-" + rs4.getString("PLAN_MONTH")));
                                } else {
                                    totalmoneywk = Long.parseLong(rs4.getString("ELEMENT"));
                                }

                                totalmoneystr = String.valueOf(totalmoneywk);
                            } else {
                                totalmoneystr = rs4.getString("ELEMENT");
                            }

                            //金額カンマ区切り,
                            if (!"3".equals(sliptotal2._sort)) {
                                commaelementstr = ((totalmoneystr != null && totalmoneystr.length() > 0) ? totalmoneystr : "0");
                            } else {
                                commaelementstr = totalmoneystr != null && totalmoneystr.length() > 0 ? totalmoneystr.substring(0, 4) + "/" + totalmoneystr.substring(4, 6) + "/" + totalmoneystr.substring(6, 8): "";
                            }
                            if ("1".equals(sliptotal2._sort) && "04".equals(rs4.getString("PLAN_MONTH"))) firstSetMoney = Long.parseLong(totalmoneystr);
                            addTotalData(slipTotalInfo, sliptotal2, rs4.getString("PLAN_MONTH"), commaelementstr, totalmoneystr);
                        }
                        //電票合算情報セット(入金必要額、入金月の累計、入金、入金日付)
                        List sortArr = new ArrayList();
                        Set kset = slipTotalInfo.keySet();
                        String nyukinSuffix = "（" + befSlipNo.substring(befSlipNo.length() - 3) + "）";
                        for (final Iterator it = kset.iterator(); it.hasNext();) {
                            final String kstr = (String)it.next();
                            sliptotal2 = (SlipTotalCls)slipTotalInfo.get(kstr);
                            setData = new LinkedMap();
                            sortArr.add(kstr);
                            setData.put("COLLECT_M_NAME", "入金額" + nyukinSuffix);
                            if ("1".equals(kstr)) {
                                setData.put("disPlanCollect", "1");
                                setData.put("ROWSPAN", "4");
                            }

                            // データセット
                            //入金月の累計
                            if ("2".equals(kstr)) {
                                setData.put("SUB_TATLE", "入金必要額累計");
                                //foreach($model->monthArray as $key => $month) {
                                long befCuTotal = 0;
                                for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                    if ("04".equals(_monthArray[ma_idx])) {
                                        setData.put("COLLECT_MONTH_04", String.valueOf(firstSetMoney));
                                        befCuTotal = firstSetMoney;
                                    } else {
                                        int intMonth = Integer.parseInt(_monthArray[ma_idx]) == 1 ? 13 : Integer.parseInt(_monthArray[ma_idx]);
                                        String befMonth = new DecimalFormat("00").format(intMonth -1);
                                        final int cutotalwkidx1 = sliptotal2.findDetailIndex(befMonth);
                                        final String cutotalwk1 =  cutotalwkidx1 >= 0 ? ((SlipTotalDetailCls)sliptotal2._slipttldetail.get(cutotalwkidx1))._element : "0";
                                        final int cutotalwkidx2 = ((SlipTotalCls)slipTotalInfo.get("1")).findDetailIndex(_monthArray[ma_idx]);
                                        final String cutotalwk2 =  cutotalwkidx2 >= 0 ? ((SlipTotalDetailCls)((SlipTotalCls)slipTotalInfo.get("1"))._slipttldetail.get(cutotalwkidx2))._element : "0";
                                        final long setCuTotal = befCuTotal - Long.parseLong(cutotalwk1) + Long.parseLong(cutotalwk2);
                                        final String plmchk =  cutotalwkidx2 >= 0 ? ((SlipTotalDetailCls)((SlipTotalCls)slipTotalInfo.get("1"))._slipttldetail.get(cutotalwkidx2))._commaelement : "";
                                        if ("0".equals(plmchk)) {
                                            setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], "0");
                                        } else {
                                            setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], String.valueOf(setCuTotal));
                                        }
                                        befCuTotal = setCuTotal;
                                    }
                                }
                                final int cutotalwkidx3 = ((SlipTotalCls)slipTotalInfo.get("2")).findDetailIndex("03");
                                final String cutotalwk3 =  cutotalwkidx3 >= 0 ? ((SlipTotalDetailCls)sliptotal2._slipttldetail.get(cutotalwkidx3))._element : "0";
                                final long setCuTotal = befCuTotal - Long.parseLong(cutotalwk3);
                                setData.put("COLLECT_MONTH_TOTAL", String.valueOf(setCuTotal));

                                if (!"2".equals(_param._output)) {
                                    studentdat._outputlist.add(setData); // 入金額(入金月累計)
                                }
                                setData = new LinkedMap();
                                setData.put("COLLECT_M_NAME", "入金額" + nyukinSuffix);
                            }
                            //名称セット
                            setData.put("SUB_TATLE", sliptotal2._mname);

                            //各項目セット
                            for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                final int eleaddidx = sliptotal2.findDetailIndex(_monthArray[ma_idx]);
                                SlipTotalDetailCls eleaddobj = null;
                                if (eleaddidx >= 0) eleaddobj = (SlipTotalDetailCls)sliptotal2._slipttldetail.get(eleaddidx);
                                if ("1".equals(sliptotal2._sort)  || "2".equals(sliptotal2._sort)) {
                                    setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], (eleaddobj != null && eleaddobj._element.length() > 0) ? eleaddobj._element: "0");
                                } else {
                                    setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], (eleaddobj != null && eleaddobj._element.length() > 0) ? eleaddobj._element: "");
                                }
                            }
                            final int mtotalidx = sliptotal2.findDetailIndex("99");
                            final String mtotalwk = (mtotalidx >= 0 ? ((SlipTotalDetailCls)sliptotal2._slipttldetail.get(mtotalidx))._commaelement : "0");
                            if (mtotalwk != null && !"".equals(mtotalwk) && !"3".equals(kstr)) {
                                setData.put("COLLECT_MONTH_TOTAL", mtotalwk);
                            }else {
                                setData.put("COLLECT_MONTH_TOTAL", "");
                            }
                            studentdat._outputlist.add(setData); // 入金額(入金必要額、入金額計)
                        }
                        //入金日がないときは、空行をセット
                        if (!sortArr.contains("3")) {
                            setData = new LinkedMap();
                            setData.put("COLLECT_M_NAME", "入金額" + nyukinSuffix);
                            setData.put("SUB_TATLE", "入金日付");
                            studentdat._outputlist.add(setData); // 入金額(入金日付)
                        }
                        setData = new LinkedMap();

                        //電票合算情報取得
                        Map slipTotalInfo2 = new LinkedMap();
                        long firstSetMoney2 = 0;
                        String totalmoneystr2 = "";
                        String commaelementstr2 = "";
                        String sql5 = getMainQuery2(schregno);
                        log.debug(" sql5 =" + sql5);
                        ps5 = db2.prepareStatement(sql5);
                        rs5 = ps5.executeQuery();
                        SlipTotalCls sliptotal3 = null;
                        //入金必要額を取得
                        while (rs5.next()) {
                            sliptotal3 = new SlipTotalCls(rs5.getString("SORT"), rs5.getString("M_NAME"));
                            totalmoneystr2 = "";
                            //必要額から支援、補助、学校減免を引く
                            if ("1".equals(sliptotal3._sort)) {
                                final long totalmoneywk;
                                if (studentdat._allReducMonthTotalMoney.get(rs5.getString("PLAN_MONTH")) != null) {
                                    totalmoneywk = Long.parseLong(rs5.getString("ELEMENT")) - Long.parseLong((String)studentdat._allReducMonthTotalMoney.get(rs5.getString("PLAN_MONTH")));
                                } else {
                                    totalmoneywk = Long.parseLong(rs5.getString("ELEMENT"));
                                }

                                totalmoneystr2 = String.valueOf(totalmoneywk);
                            } else {
                                totalmoneystr2 = rs5.getString("ELEMENT");
                            }
                            //金額カンマ区切り,
                            if (!"3".equals(sliptotal3._sort)) {
                                commaelementstr2 = ((totalmoneystr2 != null && totalmoneystr2.length() > 0) ? totalmoneystr2 : "0");
                            } else {
                                commaelementstr2 = totalmoneystr2 != null && totalmoneystr2.length() > 0 ? totalmoneystr2.substring(0, 4) + "/" + totalmoneystr2.substring(4, 6) + "/" + totalmoneystr2.substring(6, 8): "";
                            }
                            if ("1".equals(sliptotal3._sort) && "04".equals(rs5.getString("PLAN_MONTH"))) firstSetMoney2 = Long.parseLong(totalmoneystr2);
                            addTotalData(slipTotalInfo2, sliptotal3, rs5.getString("PLAN_MONTH"), commaelementstr2, totalmoneystr2);
                        }
                        //電票合算情報セット(入金必要額、入金月の累計、入金、入金日付)
                        List sortArr2 = new ArrayList();
                        Set kset2 = slipTotalInfo2.keySet();
                        for (final Iterator it = kset2.iterator(); it.hasNext();) {
                            final String kstr = (String)it.next();
                            sliptotal3 = (SlipTotalCls)slipTotalInfo2.get(kstr);
                            setData = new LinkedMap();
                            sortArr2.add(kstr);
                            setData.put("COLLECT_M_NAME", "入金総額");
                            if ("1".equals(kstr)) {
                                setData.put("disPlanCollect", "1");
                                setData.put("ROWSPAN", "4");
                            }

                            // データセット
                            //入金月の累計
                            if ("2".equals(kstr)) {
                                setData.put("SUB_TATLE", "入金必要額累計");
                                //foreach($model->monthArray as $key => $month) {
                                long befCuTotal = 0;
                                for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                    if ("04".equals(_monthArray[ma_idx])) {
                                        setData.put("COLLECT_MONTH_04", String.valueOf(firstSetMoney2));
                                        befCuTotal = firstSetMoney2;
                                    } else {
                                        int intMonth = Integer.parseInt(_monthArray[ma_idx]) == 1 ? 13 : Integer.parseInt(_monthArray[ma_idx]);
                                        String befMonth = new DecimalFormat("00").format(intMonth -1);
                                        final int cutotalwkidx1 = sliptotal3.findDetailIndex(befMonth);
                                        final String cutotalwk1 =  cutotalwkidx1 >= 0 ? ((SlipTotalDetailCls)sliptotal3._slipttldetail.get(cutotalwkidx1))._element : "0";
                                        final int cutotalwkidx2 = ((SlipTotalCls)slipTotalInfo2.get("1")).findDetailIndex(_monthArray[ma_idx]);
                                        final String cutotalwk2 =  cutotalwkidx2 >= 0 ? ((SlipTotalDetailCls)((SlipTotalCls)slipTotalInfo2.get("1"))._slipttldetail.get(cutotalwkidx2))._element : "0";
                                        final long setCuTotal = befCuTotal - Long.parseLong(cutotalwk1) + Long.parseLong(cutotalwk2);
                                        final String plmchk =  cutotalwkidx2 >= 0 ? ((SlipTotalDetailCls)((SlipTotalCls)slipTotalInfo2.get("1"))._slipttldetail.get(cutotalwkidx2))._commaelement : "";
                                        if ("0".equals(plmchk)) {
                                            setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], "0");
                                        } else {
                                            setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], String.valueOf(setCuTotal));
                                        }
                                        befCuTotal = setCuTotal;
                                    }
                                }
                                final int cutotalwkidx3 = ((SlipTotalCls)slipTotalInfo2.get("2")).findDetailIndex("03");
                                final String cutotalwk3 =  cutotalwkidx3 >= 0 ? ((SlipTotalDetailCls)sliptotal3._slipttldetail.get(cutotalwkidx3))._element : "0";
                                final long setCuTotal = befCuTotal - Long.parseLong(cutotalwk3);
                                setData.put("COLLECT_MONTH_TOTAL", String.valueOf(setCuTotal));

                                if (!"2".equals(_param._output)) {
                                    studentdat._outputlist.add(setData); // 入金額(入金月累計)
                                }
                                setData = new LinkedMap();
                                setData.put("COLLECT_M_NAME", "入金総額");
                            }
                            //名称セット
                            setData.put("SUB_TATLE", sliptotal3._mname);

                            //各項目セット
                            for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                                final int eleaddidx = sliptotal3.findDetailIndex(_monthArray[ma_idx]);
                                SlipTotalDetailCls eleaddobj = null;
                                if (eleaddidx >= 0) eleaddobj = (SlipTotalDetailCls)sliptotal3._slipttldetail.get(eleaddidx);
                                if ("1".equals(sliptotal3._sort)  || "2".equals(sliptotal3._sort)) {
                                    setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], (eleaddobj != null && eleaddobj._element.length() > 0) ? eleaddobj._element: "0");
                                } else {
                                    setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], (eleaddobj != null && eleaddobj._element.length() > 0) ? eleaddobj._element: "");
                                }
                            }
                            final int mtotalidx = sliptotal3.findDetailIndex("99");
                            final String mtotalwk = (mtotalidx >= 0 ? ((SlipTotalDetailCls)sliptotal3._slipttldetail.get(mtotalidx))._commaelement : "0");
                            if (mtotalwk != null && !"".equals(mtotalwk) && !"3".equals(kstr)) {
                                setData.put("COLLECT_MONTH_TOTAL", mtotalwk);
                            }else {
                                setData.put("COLLECT_MONTH_TOTAL", "");
                            }
                            studentdat._outputlist.add(setData); // 入金額(入金必要額、入金額計)
                        }
                        //入金日がないときは、空行をセット
                        if (!sortArr2.contains("3")) {
                            setData = new LinkedMap();
                            setData.put("COLLECT_M_NAME", "入金総額");
                            setData.put("SUB_TATLE", "入金日付");
                            studentdat._outputlist.add(setData); // 入金額(入金日付)
                        }

                        //累計過不足金
                        setData = new LinkedMap();

                        setData.put("disPlanCollect", "1");
                        setData.put("COLLECT_M_NAME", "累計過不足金");
                        setData.put("SUB_TATLE", "過不足");
                        SlipTotalCls rdatwk1 = (SlipTotalCls)slipTotalInfo2.get("2");
                        SlipTotalCls rdatwk2 = (SlipTotalCls)slipTotalInfo2.get("1");
                        long totalIntoMoney = 0;
                        long totalNeedMoney = 0;
                        long setCuInsuMoney = 0;
                        Number chgnumberwk;
                        for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                            final int rdatwk1_idx = rdatwk1.findDetailIndex(_monthArray[ma_idx]);
                            final String rdatwk1_val = rdatwk1_idx >= 0 ? ((SlipTotalDetailCls)rdatwk1._slipttldetail.get(rdatwk1_idx))._element : "0";
                            final int rdatwk2_idx = rdatwk2.findDetailIndex(_monthArray[ma_idx]);
                            final String rdatwk2_val = rdatwk2_idx >= 0 ? ((SlipTotalDetailCls)rdatwk2._slipttldetail.get(rdatwk2_idx))._element : "0";
                            try {
                                chgnumberwk = NumberFormat.getInstance().parse(rdatwk1_val);
                                totalIntoMoney += chgnumberwk.longValue();//入金累計
                                chgnumberwk = NumberFormat.getInstance().parse(rdatwk2_val);
                                totalNeedMoney += chgnumberwk.longValue();//入金必要額累計
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            setCuInsuMoney  = totalIntoMoney - totalNeedMoney;
                            final String plmchk =  rdatwk2_idx >= 0 ? ((SlipTotalDetailCls)rdatwk2._slipttldetail.get(rdatwk2_idx))._commaelement : "";
                            if ("0".equals(plmchk)) {
                                setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], "0");
                            } else {
                                setData.put("COLLECT_MONTH_" + _monthArray[ma_idx], String.valueOf(setCuInsuMoney));
                            }
                        }
                        setData.put("COLLECT_MONTH_TOTAL", String.valueOf(setCuInsuMoney));
                        studentdat._outputlist.add(setData);  //累計過不足金

                        //還付金
                        setData = new LinkedMap();
                        setData.put("disPlanCollect", "1");
                        setData.put("COLLECT_M_NAME", "還付金");
                        String setRefundMoney = "";
                        for (int ma_idx = 0;ma_idx < _monthArray.length; ma_idx++) {
                            String refundMoneyMonth = StringUtils.isEmpty((String)studentdat._refundMoneyArr.get(_monthArray[ma_idx])) ? "0" : (String)studentdat._refundMoneyArr.get(_monthArray[ma_idx]);
                            setRefundMoney = (Long.parseLong(refundMoneyMonth) < 0) ? "0": refundMoneyMonth;
                                setData.put("COLLECT_MONTH_"+_monthArray[ma_idx], setRefundMoney);
                        }
                        String refundMoneyMonthTotal = StringUtils.isEmpty((String)studentdat._refundMoneyArr.get("99")) ? "0" : (String)studentdat._refundMoneyArr.get("99");
                        String setRefundMoneyTotal = (Long.parseLong(refundMoneyMonthTotal) < 0) ? "0": refundMoneyMonthTotal;
                        setData.put("COLLECT_MONTH_TOTAL", setRefundMoneyTotal);

                        if (!"2".equals(_param._output)) {
                            studentdat._outputlist.add(setData); //還付金
                        }
                    }
                }
                list.add(studentdat);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs1);
            DbUtils.closeQuietly(null, ps2, rs2);
            DbUtils.closeQuietly(null, ps3, rs3);
            DbUtils.closeQuietly(null, ps4, rs4);
            DbUtils.closeQuietly(null, ps5, rs5);
            db2.commit();
        }
        return list;
    }

    private void addDetailData(final List searchlst, final String slipno, final String sortval, final String koumoku, final String subtatle ,final RedctionDetailDataCls adddat) {
        boolean findflg = false;
        for (final Iterator it = searchlst.iterator();it.hasNext();) {
            final ReductionDataCls searchdat = (ReductionDataCls) it.next();
            if (searchdat._slipno.equals(slipno) && searchdat._sort.equals(sortval)) {
                findflg = true;
                searchdat._koumoku = koumoku;
                searchdat._subtatle = subtatle;
                searchdat.addDetailData(adddat);
            }
        }
        if (!findflg) {
            List addlistwk = new ArrayList();
            ReductionDataCls addwk = new ReductionDataCls(slipno, sortval, koumoku, subtatle, addlistwk);
            addwk.addDetailData(adddat);
            searchlst.add(addwk);
        }
    }

    private class ReductionDataCls {
        private String _slipno;
        private String _sort;
        private String _koumoku;
        private String _subtatle;
        private List _reductiondetail;
        ReductionDataCls(final String slipno, final String sortval, final String koumoku, final String subtatle, final List reductionarray) {
            _slipno = slipno;
            _sort = sortval;
            _koumoku = koumoku;
            _subtatle = subtatle;
            _reductiondetail = reductionarray;
        }
        public void addDetailData(final RedctionDetailDataCls addwk) {
            _reductiondetail.add(addwk);
        }
        public int findDetailIndex(String findmonth) {
            boolean findflg = false;
            int retindex = 0;
            for (final Iterator it = _reductiondetail.iterator();it.hasNext();) {
                final RedctionDetailDataCls searchdat = (RedctionDetailDataCls) it.next();
                if (searchdat._planmonth.equals(findmonth)) {
                    findflg = true;
                    break;
                } else {
                    retindex++;
                }
            }
            if (!findflg) {
                retindex = -1;
            }
            return retindex;
        }
    }
    private class RedctionDetailDataCls {
        private String _planmonth;
        private String _planmoney;

        RedctionDetailDataCls(final String planmonth, final String planmoney) {
            _planmonth = planmonth;
            _planmoney = planmoney;
        }
    }
    private class mainQDataCls {
        private String _slipno;
        private int _collect_l_cd;
        private String _showorder;
        private int _collect_m_cd;
        private String _collect_m_name;
        private int _plan_paid;
        private String _jyugyouryou_flg;
        private String _plan_month;
        private String _plan_money;

        mainQDataCls (final String slipno, final int collect_l_cd, final String showorder, final int collect_m_cd,
                final String collect_m_name, final int plan_paid, final String jyugyouryouflg, final String planmonth, final String planmoney) {
            _slipno = slipno;
            _collect_l_cd = collect_l_cd;
            _showorder = showorder;
            _collect_m_cd = collect_m_cd;
            _collect_m_name = collect_m_name;
            _plan_paid = plan_paid;
            _jyugyouryou_flg = jyugyouryouflg;
            _plan_month = planmonth;
            _plan_money = planmoney;
        }
    }

    private void addTotalData(final Map searchlst, SlipTotalCls chkdat, final String planmonth, final String element, final String commaelement) {
        boolean findflg = false;
        Set kset = searchlst.keySet();
        for (final Iterator it = kset.iterator();it.hasNext();) {
            final String searchkey = (String) it.next();
            final SlipTotalCls chkobj = (SlipTotalCls)searchlst.get(searchkey);
            if (chkobj._sort.equals(chkdat._sort)) {
                findflg = true;
                SlipTotalDetailCls adddetail = new SlipTotalDetailCls(planmonth, element, commaelement);
                chkobj.addDetailData(adddetail);
            }
        }
        if (!findflg) {
            SlipTotalDetailCls adddetail = new SlipTotalDetailCls(planmonth, element, commaelement);
            chkdat.addDetailData(adddetail);
            searchlst.put(chkdat._sort, chkdat);
        }
    }

    private class SlipTotalCls {
        private String _sort;
        private String _mname;
        List _slipttldetail;

        SlipTotalCls (final String sort, final String mname) {
            _sort = sort;
            _mname = mname;
            _slipttldetail = new ArrayList();
        }
        public void addDetailData(final SlipTotalDetailCls addwk) {
            _slipttldetail.add(addwk);
        }
        public int findDetailIndex(String findmonth) {
            boolean findflg = false;
            int retindex = 0;
            for (final Iterator it = _slipttldetail.iterator();it.hasNext();) {
                final SlipTotalDetailCls searchdat = (SlipTotalDetailCls) it.next();
                if (searchdat._planmonth.equals(findmonth)) {
                    findflg = true;
                    break;
                } else {
                    retindex++;
                }
            }
            if (!findflg) {
                retindex = -1;
            }
            return retindex;
        }
    }
    private class SlipTotalDetailCls {
        private String _planmonth;
        private String _element;
        private String _commaelement;
        SlipTotalDetailCls (final String planmonth, final String element, final String commaelement) {
            _planmonth = planmonth;
            _element = element;
            _commaelement = commaelement;
        }
    }

    private String sqlReductionInfo(final String schregno) {
        final StringBuffer stb =  new StringBuffer();
        List setTable = new ArrayList();
        setTable.add("COUNTRY_PLAN");
        setTable.add("PLAN");
        setTable.add("SCHOOL_PLAN");
        setTable.add("BURDEN_CHARGE_PLAN");
        String sep = "";

        stb.append(" WITH ");
        for (final Iterator it1 = setTable.iterator();it1.hasNext();) {
            final String tableName = (String) it1.next();
            stb.append(" " + sep + tableName + " AS ( ");
            stb.append("         SELECT ");
            stb.append("             PLAN.* ");
            stb.append("         FROM ");
            stb.append("             REDUCTION_" + tableName + "_DAT PLAN ");
            stb.append("             LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN.SCHOOLCD ");
            stb.append("                                            AND SL_D.SCHOOL_KIND = PLAN.SCHOOL_KIND ");
            stb.append("                                            AND SL_D.YEAR        = PLAN.YEAR ");
            stb.append("                                            AND SL_D.SLIP_NO     = PLAN.SLIP_NO ");
            stb.append("         WHERE ");
            stb.append("                 PLAN.SCHOOLCD    = '" + _param._schoolcd + "' ");
            stb.append("             AND PLAN.SCHOOL_KIND = '" + _param._schoolkind + "' ");
            stb.append("             AND PLAN.YEAR        = '" + _param._Year + "' ");
            stb.append("             AND PLAN.SCHREGNO    = '" + schregno + "' ");
            stb.append("             AND SL_D.CANCEL_DATE IS NULL ");
            sep = " ), ";
        }
        stb.append(" ) ");

        Map setTable2 = new LinkedMap();
        setTable2.put("1", "COUNTRY_PLAN:就学支援金:基本額");
        setTable2.put("2", "COUNTRY_PLAN:就学支援金:加算額");
        setTable2.put("3", "PLAN:補助金");
        setTable2.put("4", "SCHOOL_PLAN:学校減免");
        setTable2.put("5", "BURDEN_CHARGE_PLAN:学校負担金");

        String unionstr = "";
        final Set kset = setTable2.keySet();
        for (final Iterator it = kset.iterator(); it.hasNext();) {
            final String sortidx = (String) it.next();
            final String sort = ("3".equals(sortidx) || "4".equals(sortidx)) ? "INT('" + sortidx + "' || REDUCTION_TARGET)": sortidx;
            final String tableInfo = (String)setTable2.get(sortidx);

            String[] cutwk = StringUtils.split(tableInfo,":", 0);
            final String tableName = cutwk[0];
            final String title = cutwk[1];
            final String subTitle;
            if (cutwk.length > 2) {
                subTitle = cutwk[2];
            } else {
                subTitle = "";
            }
            String fieldName = "";
            if ("2".equals(sortidx)) {
                fieldName = "ADD_DECISION_MONEY";
            } else if ("5".equals(sortidx)) {
                fieldName = "BURDEN_CHARGE";
            } else {
                fieldName = "DECISION_MONEY";
            }

            stb.append(" " + unionstr + " ");
            stb.append(" SELECT ");
            stb.append("     " + sortidx + " AS BASE_SORT, ");
            stb.append("     " + sort + " AS SORT, ");
            stb.append("     SLIP_NO, ");
            stb.append("     '" + title + "' AS KOUMOKU, ");
            if ("3".equals(sortidx) || "4".equals(sortidx)) {
                stb.append("     CASE WHEN REDUCTION_TARGET = '1' THEN '授業料等' ");
                stb.append("          WHEN REDUCTION_TARGET = '2' THEN '入学金' ");
                stb.append("          ELSE '' ");
                stb.append("     END AS SUB_TATLE, ");
            } else {
                stb.append("     '" + subTitle + "' AS SUB_TATLE, ");
            }
            if ("2".equals(sortidx)) {
                stb.append("     ADD_OFFSET_FLG AS OFFSET_FLG, ");
                stb.append("     ADD_REFUND_FLG AS REFUND_FLG, ");
            } else if ("5".equals(sortidx)) {
                stb.append("     '1' AS OFFSET_FLG, ");
                stb.append("     '' AS REFUND_FLG, ");
            } else {
                stb.append("     OFFSET_FLG, ");
                stb.append("     REFUND_FLG, ");
            }
            stb.append("     PLAN_MONTH, ");
            stb.append("     SUM(VALUE(" + fieldName + ", 0)) AS PLAN_MONEY ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " ");
            stb.append(" GROUP BY ");
            stb.append("     SLIP_NO, ");
            if ("3".equals(sortidx) || "4".equals(sortidx)) {
                stb.append("     REDUCTION_TARGET, ");
            }
            if ("2".equals(sortidx)) {
                stb.append("     ADD_OFFSET_FLG, ");
                stb.append("     ADD_REFUND_FLG, ");
            } else if ("5".equals(sortidx)) {
            } else {
                stb.append("     OFFSET_FLG, ");
                stb.append("     REFUND_FLG, ");
            }
            stb.append("     PLAN_MONTH ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     " + sortidx + " AS BASE_SORT, ");
            stb.append("     " + sort + " AS SORT, ");
            stb.append("     SLIP_NO, ");
            stb.append("     '" + title + "' AS KOUMOKU, ");
            if ("3".equals(sortidx) || "4".equals(sortidx)) {
                stb.append("     CASE WHEN REDUCTION_TARGET = '1' THEN '授業料等' ");
                stb.append("          WHEN REDUCTION_TARGET = '2' THEN '入学金' ");
                stb.append("          ELSE '' ");
                stb.append("     END AS SUB_TATLE, ");
            } else {
                stb.append("     '" + subTitle + "' AS SUB_TATLE, ");
            }
            stb.append("     '99' AS OFFSET_FLG, ");
            stb.append("     '99' AS REFUND_FLG, ");
            stb.append("     '99' AS PLAN_MONTH, ");
            stb.append("     sum(" + fieldName + ") AS PLAN_MONEY ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " ");
            stb.append(" GROUP BY ");
            stb.append("     SLIP_NO ");
            if ("3".equals(sortidx) || "4".equals(sortidx)) {
                stb.append("     , REDUCTION_TARGET ");
            }
            unionstr = " UNION ";
        }

        stb.append(" ORDER BY ");
        stb.append("     BASE_SORT, ");
        stb.append("     SLIP_NO, ");
        stb.append("     SORT ");
        return stb.toString();
    }

    private String sqlMainQuery(final String schregno) {
        final StringBuffer stb =  new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append("         SELECT ");
        stb.append("             PM_D.*, ");
        stb.append("             CM_M.SHOW_ORDER, ");
        stb.append("             CM_M.COLLECT_M_NAME, ");
        stb.append("             CM_M.GAKUNOKIN_DIV ");
        stb.append("         FROM ");
        stb.append("             COLLECT_SLIP_PLAN_M_DAT PM_D ");
        stb.append("             LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PM_D.SCHOOLCD ");
        stb.append("                                            AND SL_D.SCHOOL_KIND = PM_D.SCHOOL_KIND ");
        stb.append("                                            AND SL_D.YEAR        = PM_D.YEAR ");
        stb.append("                                            AND SL_D.SCHREGNO    = PM_D.SCHREGNO ");
        stb.append("                                            AND SL_D.SLIP_NO     = PM_D.SLIP_NO ");
        stb.append("             LEFT JOIN COLLECT_M_MST CM_M ON CM_M.SCHOOLCD     = PM_D.SCHOOLCD ");
        stb.append("                                         AND CM_M.SCHOOL_KIND  = PM_D.SCHOOL_KIND ");
        stb.append("                                         AND CM_M.YEAR         = PM_D.YEAR ");
        stb.append("                                         AND CM_M.COLLECT_L_CD = PM_D.COLLECT_L_CD ");
        stb.append("                                         AND CM_M.COLLECT_M_CD = PM_D.COLLECT_M_CD ");
        stb.append("         WHERE ");
        stb.append("                 PM_D.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("             AND PM_D.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("             AND PM_D.YEAR        = '" + _param._Year + "' ");
        stb.append("             AND PM_D.SCHREGNO    = '" + schregno + "' ");
        stb.append("             AND SL_D.CANCEL_DATE IS NULL ");
        stb.append(" ), PAID_T AS ( ");
        stb.append("         SELECT ");
        stb.append("             MAIN.*, ");
        stb.append("             VALUE(PAID.PLAN_PAID_MONEY, 0) AS PLAN_PAID_MONEY ");
        stb.append("         FROM ");
        stb.append("             MAIN_T MAIN ");
        stb.append("             LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PAID ON MAIN.SCHOOLCD     = PAID.SCHOOLCD ");
        stb.append("                                                        AND MAIN.SCHOOL_KIND  = PAID.SCHOOL_KIND ");
        stb.append("                                                        AND MAIN.YEAR         = PAID.YEAR ");
        stb.append("                                                        AND MAIN.SCHREGNO     = PAID.SCHREGNO ");
        stb.append("                                                        AND MAIN.SLIP_NO      = PAID.SLIP_NO ");
        stb.append("                                                        AND MAIN.COLLECT_L_CD = PAID.COLLECT_L_CD ");
        stb.append("                                                        AND MAIN.COLLECT_M_CD = PAID.COLLECT_M_CD ");
        stb.append("                                                        AND MAIN.PLAN_YEAR    = PAID.PLAN_YEAR ");
        stb.append("                                                        AND MAIN.PLAN_MONTH   = PAID.PLAN_MONTH ");
        stb.append(" ) ");

        //伝票毎の各項目(計画)
        stb.append(", MERGETBL AS (");
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     INT(COLLECT_L_CD) AS COLLECT_L_CD, ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     INT(COLLECT_M_CD) AS COLLECT_M_CD, ");
        stb.append("     COLLECT_M_NAME, ");
        stb.append("     1 AS PLAN_PAID, ");
        stb.append("     GAKUNOKIN_DIV AS JUGYOURYOU_FLG, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        //各項目の横計(計画)
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     INT(COLLECT_L_CD) AS COLLECT_L_CD, ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     INT(COLLECT_M_CD) AS COLLECT_M_CD, ");
        stb.append("     COLLECT_M_NAME, ");
        stb.append("     1 AS PLAN_PAID, ");
        stb.append("     '' AS JUGYOURYOU_FLG, ");
        stb.append("     '99' AS PLAN_MONTH, ");
        stb.append("     sum(PLAN_MONEY) AS PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     SLIP_NO,   ");
        stb.append("     COLLECT_L_CD, ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     COLLECT_M_CD, ");
        stb.append("     COLLECT_M_NAME ");

        //伝票毎の各項目(入金)
        stb.append(" UNION  ");
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     INT(COLLECT_L_CD) AS COLLECT_L_CD, ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     INT(COLLECT_M_CD) AS COLLECT_M_CD, ");
        stb.append("     COLLECT_M_NAME, ");
        stb.append("     2 AS PLAN_PAID, ");
        stb.append("     GAKUNOKIN_DIV AS JUGYOURYOU_FLG, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     PLAN_PAID_MONEY AS PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     PAID_T ");
        //各項目の横計(入金)
        stb.append(" UNION  ");
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     INT(COLLECT_L_CD) AS COLLECT_L_CD, ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     INT(COLLECT_M_CD) AS COLLECT_M_CD, ");
        stb.append("     COLLECT_M_NAME, ");
        stb.append("     2 AS PLAN_PAID, ");
        stb.append("     '' AS JUGYOURYOU_FLG, ");
        stb.append("     '99' AS PLAN_MONTH, ");
        stb.append("     sum(PLAN_PAID_MONEY) AS PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     PAID_T ");
        stb.append(" GROUP BY ");
        stb.append("     SLIP_NO, ");
        stb.append("     COLLECT_L_CD, ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     COLLECT_M_CD, ");
        stb.append("     COLLECT_M_NAME ");

        //電票毎の各月の縦計
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     101 AS COLLECT_L_CD, ");
        stb.append("     '' AS SHOW_ORDER, ");
        stb.append("     101 AS COLLECT_M_CD, ");
        stb.append("     '伝票合計（' || substr(SLIP_NO, 13, 3) || '）' AS COLLECT_M_NAME, ");
        stb.append("     1 AS PLAN_PAID, ");
        stb.append("     '' AS JUGYOURYOU_FLG, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     sum(PLAN_MONEY) AS PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     SLIP_NO,   ");
        stb.append("     PLAN_MONTH ");
        //電票毎の縦計の横計
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SLIP_NO, ");
        stb.append("     101 AS COLLECT_L_CD, ");
        stb.append("     '' AS SHOW_ORDER, ");
        stb.append("     101 AS COLLECT_M_CD, ");
        stb.append("     '伝票合計（' || substr(SLIP_NO, 13, 3) || '）' AS COLLECT_M_NAME, ");
        stb.append("     1 AS PLAN_PAID, ");
        stb.append("     '' AS JUGYOURYOU_FLG, ");
        stb.append("     '99' AS PLAN_MONTH, ");
        stb.append("     sum(PLAN_MONEY) AS PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     SLIP_NO ");

        //納期限
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     MAIN.SLIP_NO, ");
        stb.append("     102 AS COLLECT_L_CD, ");
        stb.append("     '' AS SHOW_ORDER, ");
        stb.append("     102 AS COLLECT_M_CD, ");
        stb.append("     '納期限' AS COLLECT_M_NAME, ");
        stb.append("     2 AS PLAN_PAID, ");
        stb.append("     '' AS JUGYOURYOU_FLG, ");
        stb.append("     MAIN.PLAN_MONTH, ");
        stb.append("     INT(LIMI.PAID_LIMIT_DATE) AS PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T MAIN ");
        stb.append("     LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMI ON MAIN.SCHOOLCD    = LIMI.SCHOOLCD ");
        stb.append("                                                   AND MAIN.SCHOOL_KIND = LIMI.SCHOOL_KIND ");
        stb.append("                                                   AND MAIN.YEAR        = LIMI.YEAR ");
        stb.append("                                                   AND MAIN.SCHREGNO    = LIMI.SCHREGNO ");
        stb.append("                                                   AND MAIN.SLIP_NO     = LIMI.SLIP_NO ");
        stb.append("                                                   AND MAIN.PLAN_YEAR   = LIMI.PLAN_YEAR ");
        stb.append("                                                   AND MAIN.PLAN_MONTH  = LIMI.PLAN_MONTH ");

        stb.append(" ORDER BY ");
        stb.append("     SLIP_NO, ");
        stb.append("     INT(COLLECT_L_CD), ");
        stb.append("     SHOW_ORDER, ");
        stb.append("     INT(COLLECT_M_CD), ");
        stb.append("     PLAN_PAID ");
        stb.append(" ) ");
        stb.append("SELECT ");
        stb.append("     MTBL.SLIP_NO, ");
        stb.append("     MTBL.COLLECT_L_CD, ");
        stb.append("     MTBL.SHOW_ORDER, ");
        stb.append("     MTBL.COLLECT_M_CD, ");
        stb.append("     MTBL.COLLECT_M_NAME, ");
        stb.append("     MTBL.PLAN_PAID, ");
        stb.append("     MTBL.JUGYOURYOU_FLG, ");
        stb.append("     MTBL.PLAN_MONTH, ");
        stb.append("     MTBL.PLAN_MONEY ");
        stb.append("FROM ");
        stb.append("    MERGETBL MTBL");

        return stb.toString();
    }

    //入金総額用
    private String getMainQuery2(final String schregno) {
        return getMainQuery2(schregno, "");
    }

    //伝票毎の入金額用
    private String getMainQuery2(final String schregno, final String slipNo) {
        final StringBuffer stb =  new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append("         SELECT ");
        stb.append("             PM_D.*, ");
        stb.append("             CM_M.SHOW_ORDER, ");
        stb.append("             CM_M.COLLECT_M_NAME, ");
        stb.append("             CM_M.GAKUNOKIN_DIV ");
        stb.append("         FROM ");
        stb.append("             COLLECT_SLIP_PLAN_M_DAT PM_D ");
        stb.append("             LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PM_D.SCHOOLCD ");
        stb.append("                                            AND SL_D.SCHOOL_KIND = PM_D.SCHOOL_KIND ");
        stb.append("                                            AND SL_D.YEAR        = PM_D.YEAR ");
        stb.append("                                            AND SL_D.SCHREGNO    = PM_D.SCHREGNO ");
        stb.append("                                            AND SL_D.SLIP_NO     = PM_D.SLIP_NO ");
        stb.append("             LEFT JOIN COLLECT_M_MST CM_M ON CM_M.SCHOOLCD     = PM_D.SCHOOLCD ");
        stb.append("                                         AND CM_M.SCHOOL_KIND  = PM_D.SCHOOL_KIND ");
        stb.append("                                         AND CM_M.YEAR         = PM_D.YEAR ");
        stb.append("                                         AND CM_M.COLLECT_L_CD = PM_D.COLLECT_L_CD ");
        stb.append("                                         AND CM_M.COLLECT_M_CD = PM_D.COLLECT_M_CD ");
        stb.append("         WHERE ");
        stb.append("                 PM_D.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("             AND PM_D.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("             AND PM_D.YEAR        = '" + _param._Year + "' ");
        if (!"".equals(slipNo)) {
            stb.append("             AND PM_D.SLIP_NO     = '"+ slipNo +"' ");
        }
        stb.append("             AND PM_D.SCHREGNO    = '" + schregno + "' ");
        stb.append("             AND SL_D.CANCEL_DATE IS NULL ");
        //納入（入金）データ
        stb.append(" ), PAID_DATA AS ( ");
        stb.append("         SELECT ");
        stb.append("             PAID.* ");
        stb.append("         FROM ");
        stb.append("             COLLECT_SLIP_PLAN_PAID_DAT PAID ");
        stb.append("             LEFT JOIN COLLECT_SLIP_DAT SL_D ");
        stb.append("                  ON SL_D.SCHOOLCD    = PAID.SCHOOLCD ");
        stb.append("                 AND SL_D.SCHOOL_KIND = PAID.SCHOOL_KIND ");
        stb.append("                 AND SL_D.YEAR        = PAID.YEAR ");
        stb.append("                 AND SL_D.SLIP_NO     = PAID.SLIP_NO ");
        stb.append("         WHERE ");
        stb.append("                 PAID.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("             AND PAID.SCHOOL_KIND = '" + _param._schoolkind + "' ");
        stb.append("             AND PAID.YEAR        = '" + _param._Year + "' ");
        stb.append("             AND PAID.SCHREGNO    = '" + schregno + "' ");
        if (!"".equals(slipNo)) {
            stb.append("             AND PAID.SLIP_NO     = '"+ slipNo +"' ");
        }
        stb.append("             AND SL_D.CANCEL_DATE IS NULL ");
        stb.append(" ) ");
        //入金必要額(各月)
        stb.append(" SELECT ");
        stb.append("     1 AS SORT, ");
        stb.append("     '入金必要額' AS M_NAME, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     sum(PLAN_MONEY) AS ELEMENT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     PLAN_MONTH ");
        //入金必要額(各月)の横計
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     1 AS SORT, ");
        stb.append("     '入金必要額' AS M_NAME, ");
        stb.append("     '99' AS PLAN_MONTH, ");
        stb.append("     sum(PLAN_MONEY) AS ELEMENT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        //入金(各月)
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     2 AS SORT, ");
        stb.append("     '入金額計' AS M_NAME, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     sum(PLAN_PAID_MONEY) AS ELEMENT ");
        stb.append(" FROM ");
        stb.append("     PAID_DATA ");
        stb.append(" GROUP BY ");
        stb.append("     PLAN_MONTH ");
        //入金(各月)の横計
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     2 AS SORT, ");
        stb.append("     '入金額計' AS M_NAME, ");
        stb.append("     '99' AS PLAN_MONTH, ");
        stb.append("     sum(PLAN_PAID_MONEY) AS ELEMENT ");
        stb.append(" FROM ");
        stb.append("     PAID_DATA ");
        //入金日付(各月)
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     3 AS SORT, ");
        stb.append("     '入金日付' AS M_NAME, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     INT(max(PLAN_PAID_MONEY_DATE)) AS ELEMENT ");
        stb.append(" FROM ");
        stb.append("     PAID_DATA ");
        stb.append(" GROUP BY ");
        stb.append("     PLAN_MONTH ");
        stb.append(" ORDER BY ");
        stb.append("     SORT ");

        return stb.toString();
    }

    private class Student {
        final String _schregno;
        String _grade;
        String _hrClass;
        String _hrName;
        String _name;
        String _nameKana;
        String _attendno;
        final Map _slipReducMonthTotalMoney;
        final Map _allReducMonthTotalMoney;
        final Map _refundMoneyArr;
        final List _reductionInfo;
        final List _outputlist;

        public Student(
                final DB2UDB db2,
                final String schregno
        ) {
            _schregno = schregno;
            setBaseInfo(db2, schregno);
            _slipReducMonthTotalMoney = new LinkedMap();
            _allReducMonthTotalMoney = new LinkedMap();
            _refundMoneyArr = new LinkedMap();
            _reductionInfo = new ArrayList();
            _outputlist = new ArrayList();
        }
        private void setBaseInfo(final DB2UDB db2, final String schregno) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql0 = StudentInfosql(schregno);
                log.debug("sql0 = " + sql0);
                ps = db2.prepareStatement(sql0);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _grade = rs.getString("GRADE");
                    _hrClass = rs.getString("HR_CLASS");
                    _hrName = rs.getString("HR_NAME");
                    _attendno = rs.getString("ATTENDNO");
                    _name = rs.getString("NAME");
                    _nameKana = rs.getString("NAME_KANA");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
    }

    private String StudentInfosql(final String schregno) {
        final StringBuffer stb =  new StringBuffer();
        if ("KNJP730".equals(_param._callPrgid) && "1".equals(_param._studentDiv)) {
            stb.append(" SELECT ");
            stb.append("        FRSH.SCHREGNO AS SCHREGNO, ");
            stb.append("        FRSH.GRADE AS GRADE, ");
            stb.append("        FRSH.HR_CLASS AS HR_CLASS, ");
            stb.append("        HDAT.HR_NAME AS HR_NAME, ");
            stb.append("        FRSH.ATTENDNO AS ATTENDNO, ");
            stb.append("        FRSH.NAME AS NAME, ");
            stb.append("        FRSH.NAME_KANA AS NAME_KANA ");
            stb.append(" FROM ");
            stb.append("        FRESHMAN_DAT FRSH ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("          ON FRSH.ENTERYEAR = HDAT.YEAR ");
            stb.append("          AND HDAT.SEMESTER = '1' ");
            stb.append("          AND FRSH.GRADE = HDAT.GRADE ");
            stb.append("          AND FRSH.HR_CLASS = HDAT.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("        FRSH.SCHREGNO = '" + schregno + "' ");
            stb.append("        AND FRSH.ENTERYEAR = '" + _param._Year + "' ");
        } else {
            stb.append(" SELECT ");
            stb.append("        SRD.SCHREGNO AS SCHREGNO, ");
            stb.append("        SRD.GRADE AS GRADE, ");
            stb.append("        HDAT.HR_CLASS AS HR_CLASS, ");
            stb.append("        HDAT.HR_NAME AS HR_NAME, ");
            stb.append("        SRD.ATTENDNO AS ATTENDNO, ");
            stb.append("        SBM.NAME AS NAME, ");
            stb.append("        SBM.NAME_KANA AS NAME_KANA ");
            stb.append(" FROM ");
            stb.append("        SCHREG_REGD_DAT SRD ");
            stb.append("        LEFT JOIN SCHREG_BASE_MST SBM ");
            stb.append("          ON SRD.SCHREGNO = SBM.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("          ON SRD.YEAR = HDAT.YEAR ");
            stb.append("          AND SRD.SEMESTER = HDAT.SEMESTER ");
            stb.append("          AND SRD.GRADE = HDAT.GRADE ");
            stb.append("          AND SRD.HR_CLASS = HDAT.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("        SRD.SCHREGNO = '" + schregno + "' ");
            stb.append("        AND SRD.YEAR = '" + _param._Year + "' ");
            stb.append("        AND SRD.SEMESTER = '" + _param._gakki + "' ");
            stb.append("        AND SRD.GRADE || SRD.HR_CLASS = '" + _param._grade_hrclass + "' ");
        }

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.debug("Revision: 56595 ");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _output;
        final String _grade;
        final String _grade_hrclass;
        final String[] _category_selected;
        final String _instate;
        final String _Year;
        final String _gakki;
        final String _schoolcd;
        final String _schoolkind;
        final String _ctrlDate;
        final String _callPrgid;
        final String _studentDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
            _grade_hrclass = request.getParameter("GRADE_HR_CLASS");
            _output = request.getParameter("OUTPUT");
            _category_selected = request.getParameterValues("CATEGORY_SELECTED");

            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _category_selected.length; ia++) {
                if (_category_selected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_category_selected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _instate = sbx.toString();

            _Year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolkind = request.getParameter("SCHOOL_KIND");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _callPrgid = request.getParameter("CALL_PRGID");
            _studentDiv = request.getParameter("STUDENT_DIV");
        }

    }
}

// eof

