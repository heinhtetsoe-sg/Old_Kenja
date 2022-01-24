/*
 * $Id$
 *
 * 作成日: 2021/03/18
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD626J{

    private static final Log log = LogFactory.getLog(KNJD626J.class);

    private boolean _hasData;

    private static final String SEARCHTESTCD1 = "9-990008";
    private static final String SEARCHSUBCLSCD1 = "900100";
    private static final String SEMEALL = "9";
    private static final String SEX_MANMARK = "○";
    private static final String RYUKYU_MARK = "○";
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
        for (Iterator ite = _param._printChk.iterator();ite.hasNext();) {
            final Integer idx = (Integer)ite.next();
            switch (idx) {
            case 1:
                printSub1(db2, svf);
                _hasData = true;
                break;
            case 2:
                printSub2(db2, svf);
                _hasData = true;
                break;
            case 3:
                printSub3(db2, svf);
                _hasData = true;
                break;
            case 4:
                printSub4(db2, svf);
                _hasData = true;
                break;
            case 5:
                printSub5(db2, svf);
                _hasData = true;
                break;
            case 6:
                printSub6(db2, svf);
                _hasData = true;
                break;
            case 7:
                printSub7(db2, svf);
                //_hasDataは関数内で設定
                break;
            case 8:
                printSub8(db2, svf);
                _hasData = true;
                break;
            case 9:
                printSub9(db2, svf);
                _hasData = true;
                break;
            default:
                break;
            }
        }
    }

    private void printSub1(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_1.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo1(db2);

        final int maxLine = 35;
        int cnt = 0;
        setTitle1(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final List subList = (List)dataInfoMap.get(schregNo);
                final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                if (subList.size() > 0) {
                    printSchInfo1(svf, cnt+1, prtSchObj);
                }
                for (Iterator itd = subList.iterator();itd.hasNext();) {
                    final DataInfo1 d1Obj = (DataInfo1)itd.next();
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle1(db2, svf);
                        cnt = 1;
                        printSchInfo1(svf, cnt, prtSchObj);
                    }
                    svf.VrsOutn("SDATE", cnt, StringUtils.replace(d1Obj._sdate, "-", "/"));  //開始
                    svf.VrsOutn("EDATE", cnt, StringUtils.replace(d1Obj._edate, "-", "/"));  //終了
                }
            }
        }
        if (cnt == 0) {
            svf.VrsOutn("NAME", 1, "該当者なし");  //氏名
        }
        svf.VrsOut("NUM1", String.valueOf(_param._manCnt) + "名");    //男子
        svf.VrsOut("NUM2", String.valueOf(_param._womanCnt) + "名");  //女子
        svf.VrsOut("NUM3", String.valueOf(_param._ttlCnt) + "名");    //合計

        svf.VrEndPage();
    }

    private void printSchInfo1(final Vrw32alp svf, final int cnt, final SchInfo prtSchObj) {
        svf.VrsOutn("HR_NAME", cnt, prtSchObj._hr_Name);  //HR
        svf.VrsOutn("NO", cnt, prtSchObj.getAttendNo());  //NO
        svf.VrsOutn("SEX", cnt, prtSchObj.getSexStr());  //性別
        svf.VrsOutn("NAME", cnt, prtSchObj._name);  //氏名
    }

    private void setTitle1(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
    }

    private void setTitleCommon(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate));//③
        svf.VrsOut("TITLE", "第" +_param._prEst + "期 " + _param._gradeName + " " + "成績会議資料");//①②④
    }

    private void printSub2(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_2.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo2(db2);

        final int maxLine = 35;
        int cnt = 0;
        setTitle2(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                int subclsCnt = 0;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo2 d2Obj = (DataInfo2)subMap.get(kStr);
                    if (d2Obj.calcP2()) {
                        subclsCnt++;
                    }
                }
                if (subclsCnt == subMap.size()) {
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle2(db2, svf);
                        cnt = 1;
                    }
                    final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                    printSchInfo1(svf, cnt, prtSchObj);
                }
            }
        }
        if (cnt == 0) {
            svf.VrsOutn("NAME", 1, "該当者なし");  //氏名
        }
        svf.VrEndPage();
    }

    private void setTitle2(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
    }

    private void printSub3(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_3.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo3(db2);

        final int scoreLine = 3;
        final int maxLine = 35;
        final int maxUnderCnt = 3;
        final int chkTotalCredit = 8;
        int cnt = 0;
        setTitle3(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                int underCnt = 0;
                int totalCredit = 0;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo3 d3Obj = (DataInfo3)subMap.get(kStr);
                    if (!"".equals(StringUtils.defaultString(d3Obj._score, ""))) {
                        if (Integer.parseInt(d3Obj._score) <= scoreLine) {
                            if (!"".equals(StringUtils.defaultString(d3Obj._credits, ""))) {
                                totalCredit += Integer.parseInt(d3Obj._credits);
                            }
                            underCnt++;
                        }
                    } else {
                        underCnt++;
                    }
                }
                if (underCnt < maxUnderCnt || totalCredit < chkTotalCredit) { //SCOREが3以下の科目数が3以上かつ合計単位8以上の"否定"
                    continue;
                }
                final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                printSchInfo3(svf, cnt + 1, prtSchObj);
                boolean fstPrtFlg = true;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String subcls = (String )its.next();
                    final DataInfo3 d3Obj = (DataInfo3)subMap.get(subcls);
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle3(db2, svf);
                        cnt = 1;
                        printSchInfo3(svf, cnt, prtSchObj);
                    }
                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d3Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME" + sFieldId, cnt, d3Obj._subclassName);  //科目名
                    svf.VrsOutn("CREDIT", cnt, d3Obj._credits);     //単位数
                    svf.VrsOutn("SCORE", cnt, d3Obj.printScore());  //10段階評価
                    if (!its.hasNext()) {
                        svf.VrsOutn("TOTAL_SUBCLASS", cnt, String.valueOf(underCnt));   //合計科目数
                        svf.VrsOutn("TOTAL_CREDIT", cnt, String.valueOf(totalCredit));  //合計単位数
                    }
                    if (fstPrtFlg) {
                        svf.VrsOutn("STATUS", cnt, "不可");  //ステータス
                        fstPrtFlg = false;
                    }
                }
            }
        }
        if (cnt == 0) {
            svf.VrsOutn("NAME1", 1, "該当者なし");  //氏名
        }
        svf.VrEndPage();
    }

    private void printSchInfo3(final Vrw32alp svf, final int cnt, final SchInfo prtSchObj) {
        svf.VrsOutn("HR_NAME", cnt, prtSchObj._hr_Name);  //HR
        svf.VrsOutn("NO", cnt, prtSchObj.getAttendNo());      //NO
        svf.VrsOutn("SEX", cnt, prtSchObj.getSexStr());   //性別
        final int nLen = KNJ_EditEdit.getMS932ByteLength(prtSchObj._name);
        final String nFieldId = nLen > 30 ? "3" : nLen > 20 ? "2" : "1";
        svf.VrsOutn("NAME" + nFieldId, cnt, prtSchObj._name);  //氏名
        svf.VrsOutn("BASE_REMARK", cnt, prtSchObj._syussin);   //出身
        svf.VrsOutn("GRADE", cnt, prtSchObj._ryunen1 + prtSchObj._ryunen2 + prtSchObj._ryunen3);  //留級
    }


    private void setTitle3(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
        if ("3".equals(_param._gradeCd)) {
            svf.VrsOut("ITEM", "【３．卒業基準に達しない者】");//⑤
            svf.VrsOut("GRAD_CRITERIA", "(卒業基準：3科目8単位)");//⑥
        } else {
            svf.VrsOut("ITEM", "【３．進級基準に達しない者】");//⑤
            svf.VrsOut("GRAD_CRITERIA", "(進級基準：3科目8単位)");//⑥
        }
    }

    private void printSub4(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_4.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo4(db2);

        final int maxLine = 35;
        final int maxUnderCnt1 = 3;
        final int chkTotalCredit1 = 7;
        final int maxUnderCnt2 = 2;
        final int chkTotalCredit2 = 8;
        int cnt = 0;
        setTitle4(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                int underCnt = 0;
                int totalCredit = 0;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo4 d4Obj = (DataInfo4)subMap.get(kStr);
                    if (!"".equals(StringUtils.defaultString(d4Obj._score, ""))) {
                        if (Integer.parseInt(d4Obj._score) <= 3) {
                            if (!"".equals(StringUtils.defaultString(d4Obj._credits, ""))) {
                                totalCredit += Integer.parseInt(d4Obj._credits);
                            }
                            underCnt++;
                        }
                    } else {
                        underCnt++;
                    }
                }
                if ((underCnt > maxUnderCnt1 || totalCredit < chkTotalCredit1)       //(scoreが3以下の数3個以下、合計単位数7以下)の"否定"
                    && (underCnt > maxUnderCnt2 || totalCredit < chkTotalCredit2)) { //(scoreが3以下の数2個以下、合計単位数8以下)の"否定"
                    continue;
                }
                final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                printSchInfo3(svf, cnt + 1, prtSchObj);
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String subcls = (String )its.next();
                    final DataInfo4 d4Obj = (DataInfo4)subMap.get(subcls);
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle4(db2, svf);
                        cnt = 1;
                        printSchInfo3(svf, cnt, prtSchObj);
                    }

                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d4Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME" + sFieldId, cnt, d4Obj._subclassName);  //科目名
                    svf.VrsOutn("CREDIT", cnt, d4Obj._credits);     //単位数
                    svf.VrsOutn("SCORE", cnt, d4Obj.printScore());  //10段階評価
                    if (!its.hasNext()) {
                        svf.VrsOutn("TOTAL_SUBCLASS", cnt, String.valueOf(underCnt));   //合計科目数
                        svf.VrsOutn("TOTAL_CREDIT", cnt, String.valueOf(totalCredit));  //合計単位数
                    }
                }
            }
        }
        if (cnt == 0) {
            svf.VrsOutn("NAME1", 1, "該当者なし");  //氏名
        }
        svf.VrEndPage();
    }

    private void setTitle4(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
        if ("3".equals(_param._gradeCd)) {
            svf.VrsOut("ITEM", "【４．卒業基準内で不合格科目のある者】");//⑤
            svf.VrsOut("GRAD_CRITERIA", "(卒業基準：3科目8単位)");//⑥
        } else {
            svf.VrsOut("ITEM", "【４．進級基準内で不合格科目のある者】");//⑤
            svf.VrsOut("GRAD_CRITERIA", "(進級基準：3科目8単位)");//⑥
        }
    }

    private void printSub5(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_5.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo5(db2);

        final int maxLine = 35;
        int cnt = 0;
        setTitle5(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                int subclsCnt = 0;
                int totalCredit = 0;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo5 d5Obj = (DataInfo5)subMap.get(kStr);
                    if (d5Obj.calcP2()) {
                        if (!"".equals(StringUtils.defaultString(d5Obj._credits, ""))) {
                            totalCredit += Integer.parseInt(d5Obj._credits);
                        }
                        subclsCnt++;
                    }
                }
                if (subclsCnt == 0) {
                    continue;
                }
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo5 d5Obj = (DataInfo5)subMap.get(kStr);
                    if (!d5Obj.calcP2()) {
                        continue;
                    }
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle5(db2, svf);
                        cnt = 1;
                    }
                    final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                    if (cnt == 1) {
                        printSchInfo3(svf, cnt, prtSchObj);
                    }
                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d5Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME" + sFieldId, cnt, d5Obj._subclassName); //科目名
                    svf.VrsOutn("CREDIT", cnt, d5Obj._credits); //単位
                    svf.VrsOutn("SICK", cnt, String.valueOf(d5Obj.calcSick()));       //欠課時数
                    svf.VrsOutn("LESSON", cnt, String.valueOf(d5Obj.calcLesson()));   //授業時数
                    if (subclsCnt == cnt) {
                        svf.VrsOutn("TOTAL_SUBCLASS", cnt, String.valueOf(cnt));        //合計科目数
                        svf.VrsOutn("TOTAL_CREDIT", cnt, String.valueOf(totalCredit));  //合計単位数
                    }
                }
            }
        }
        if (cnt == 0) {
            svf.VrsOutn("NAME1", 1, "該当者なし");  //氏名
        }
        svf.VrEndPage();
    }

    private void setTitle5(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
    }

    private void printSub6(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_6.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo6(db2);

        final int maxLine = 35;
        int cnt = 0;
        setTitle6(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                int underCnt = 0;
                int totalCredit = 0;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo6 d6Obj = (DataInfo6)subMap.get(kStr);
                    if (!"".equals(StringUtils.defaultString(d6Obj._credits, ""))) {
                        totalCredit += Integer.parseInt(d6Obj._credits);
                    }
                    underCnt++;
                }
                final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                printSchInfo3(svf, cnt + 1, prtSchObj);
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String subcls = (String )its.next();
                    final DataInfo6 d6Obj = (DataInfo6)subMap.get(subcls);
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle6(db2, svf);
                        cnt = 1;
                        printSchInfo3(svf, cnt, prtSchObj);
                    }
                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d6Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME" + sFieldId, cnt, d6Obj._subclassName);  //科目名
                    svf.VrsOutn("CREDIT", cnt, d6Obj._credits);       //単位数
                    svf.VrsOutn("SCORE", cnt, d6Obj.printScore());    //10段階評価
                    if (!its.hasNext()) {
                        svf.VrsOutn("TOTAL_SUBCLASS", cnt, String.valueOf(underCnt));   //合計科目数
                        svf.VrsOutn("TOTAL_CREDIT", cnt, String.valueOf(totalCredit));  //合計単位数
                    }
                }
            }
        }
        if (cnt == 0) {
            svf.VrsOutn("NAME1", 1, "該当者なし");  //氏名
        }
        svf.VrEndPage();
    }

    private void setTitle6(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
    }

    private void printSub7(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_7.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo7(db2);

        final int maxLine = 35;
        setTitle7(db2, svf);
        String bakClassCd1 = "";
        String bakClassCd2 = "";
        for (Iterator ite = dataInfoMap.keySet().iterator();ite.hasNext();) {
            final String cCode = (String)ite.next();
            bakClassCd1 = "";
            bakClassCd2 = "";
            if (!dataInfoMap.containsKey(cCode)) {
                continue;
            }
            final Map subMap = (Map)dataInfoMap.get(cCode);
            if (subMap.size() == 0) {
                continue;
            }

            Iterator ito1 = null;
            Iterator ito2 = null;
            if (subMap.containsKey("0")) {
                final List detList1 = (List)subMap.get("0");  //必修科目
                ito1 = detList1.iterator();
            }
            if (subMap.containsKey("1")) {
                final List detList2 = (List)subMap.get("1");  //選択科目
                ito2 = detList2.iterator();
            }
            int cnt = 0;
            while ((ito1 != null && ito1.hasNext()) || (ito2 != null && ito2.hasNext())) {
                cnt++;
                if (cnt > maxLine) {
                    svf.VrEndPage();
                    setTitle7(db2, svf);
                    cnt = 1;
                    bakClassCd1 = "";
                    bakClassCd2 = "";
                }
                if (ito1 != null && ito1.hasNext()) {
                    final DataInfo7 d7Obj = (DataInfo7)ito1.next();
                    if (!bakClassCd1.equals(d7Obj._classCd)) {
                        svf.VrsOutn("CLASS_NAME1", cnt, d7Obj._className);  //教科名
                    }
                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d7Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME1_" + sFieldId, cnt, d7Obj._subclassName);  //科目名
                    svf.VrsOutn("NUM1", cnt, d7Obj._cnt);  //人数
                    bakClassCd1 = d7Obj._classCd;
                                   }
                if (ito2 != null && ito2.hasNext()) {
                    final DataInfo7 d7Obj = (DataInfo7)ito2.next();
                    if (!bakClassCd2.equals(d7Obj._classCd)) {
                        svf.VrsOutn("CLASS_NAME2", cnt, d7Obj._className);  //教科名
                    }
                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d7Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME2_" + sFieldId, cnt, d7Obj._subclassName);  //科目名
                    svf.VrsOutn("NUM2", cnt, d7Obj._cnt);  //人数
                    bakClassCd2 = d7Obj._classCd;
                }
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private void setTitle7(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
    }

    private void printSub8(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_8.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo8(db2);

        final int maxLine = 35;
        int cnt = 0;
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            setTitle8(db2, svf);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                printSchInfo3(svf, cnt + 1, prtSchObj);
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String subcls = (String )its.next();
                    final DataInfo8 d8Obj = (DataInfo8)subMap.get(subcls);
                    cnt++;
                    if (cnt > maxLine) {
                        svf.VrEndPage();
                        setTitle8(db2, svf);
                        cnt = 1;
                        printSchInfo3(svf, cnt, prtSchObj);
                    }
                    svf.VrsOutn("NENDO", cnt, d8Obj._year);  //年度
                    svf.VrsOutn("GRADE2", cnt, d8Obj._yGrade);  //学年
                    final int sLen = KNJ_EditEdit.getMS932ByteLength(d8Obj._subclassName);
                    final String sFieldId = sLen > 30 ? "3" : sLen > 20 ? "2" : "1";
                    svf.VrsOutn("SUBCLASS_NAME" + sFieldId, cnt, d8Obj._subclassName);  //科目名
                    svf.VrsOutn("CREDIT", cnt, d8Obj._credits);  //単位数
                    svf.VrsOutn("JUDGE", cnt, d8Obj._stat);  //評定
                }
            }
        }
        if (cnt == 0) {
            setTitle8(db2, svf);
            svf.VrsOutn("NAME1", 1, "該当者なし");  //氏名
        }
        svf.VrEndPage();
    }

    private void setTitle8(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
//        svf.VrsOut("", "【8．総合学習　未履修・未修得者】");//⑤
    }

    private void printSub9(final DB2UDB db2, final Vrw32alp svf) {
        final String fmtname = "KNJD626J_9.frm";
        svf.VrSetForm(fmtname, 1);
        final Map dataInfoMap = getDataInfo3(db2);

        final int maxUnderCnt = 3;
        final int chkTotalCredit = 8;
        int ttlNGCnt = 0;
        int manNGCnt = 0;
        int womanNGCnt = 0;
        setTitle3(db2, svf);
        if (dataInfoMap.size() > 0) {
            final Map schInfoMap = getSchInfo(db2, dataInfoMap);
            for (Iterator ite = schInfoMap.keySet().iterator();ite.hasNext();) {
                final String schregNo = (String)ite.next();
                if (!dataInfoMap.containsKey(schregNo)) {
                    continue;
                }
                final Map subMap = (Map)dataInfoMap.get(schregNo);
                if (subMap.size() == 0) {
                    continue;
                }
                int underCnt = 0;
                int totalCredit = 0;
                for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String )its.next();
                    final DataInfo3 d3Obj = (DataInfo3)subMap.get(kStr);
                    if (!"".equals(StringUtils.defaultString(d3Obj._score, ""))) {
                        if (Integer.parseInt(d3Obj._score) <= 3) {
                            if (!"".equals(StringUtils.defaultString(d3Obj._credits, ""))) {
                                totalCredit += Integer.parseInt(d3Obj._credits);
                            }
                            underCnt++;
                        }
                    } else {
                        underCnt++;
                    }
                }
                if (underCnt < maxUnderCnt || totalCredit < chkTotalCredit) { //SCOREが3以下の科目数が3以上かつ合計単位8以上の"否定"
                    continue;
                }
                ttlNGCnt++;
                final SchInfo prtSchObj = (SchInfo)schInfoMap.get(schregNo);
                if ("1".equals(prtSchObj._sex)) {
                    manNGCnt++;
                } else {
                    womanNGCnt++;
                }
            }
        }
        svf.VrsOut("NUM1", String.valueOf(_param._manCnt - manNGCnt));      //男子
        svf.VrsOut("NUM2", String.valueOf(_param._womanCnt - womanNGCnt));  //女子
        svf.VrsOut("NUM3", String.valueOf(_param._ttlCnt - ttlNGCnt));      //全体
        svf.VrEndPage();
    }

    private void setTitle9(final DB2UDB db2, final Vrw32alp svf) {
        setTitleCommon(db2, svf);
    }

    private Map getDataInfo1(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        List subList = null;
        final String sql = getDataInfo1Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String sDate = rs.getString("SDATE");
                final String eDate = rs.getString("EDATE");
                final DataInfo1 addWk = new DataInfo1(schregNo, sDate, eDate);
                if (!retMap.containsKey(schregNo)) {
                    subList = new ArrayList();
                    retMap.put(schregNo, subList);
                } else {
                    subList = (List)retMap.get(schregNo);
                }
                subList.add(addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo1Sql() {
        final StringBuffer stb = new StringBuffer();
        //3/31と4/1を連結させる。
        //そのために、4/1開始、3/31終了データを持ってくる。最後にそれ以外のデータをUNIONする。
        //4/1開始データ
        stb.append(" WITH HASIHASI4_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     TRANSFER_SDATE, ");
        stb.append("     TRANSFER_EDATE, ");
        stb.append("     YEAR(TRANSFER_SDATE) AS CONNYEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("     TRANSFERCD = '1' ");
        stb.append("     AND ");
        stb.append("     (MONTH(TRANSFER_SDATE) = '4' AND DAY(TRANSFER_SDATE) = '1') ");
        //3/31終了データ
        stb.append(" ), HASIHASI3_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     TRANSFER_SDATE, ");
        stb.append("     TRANSFER_EDATE, ");
        stb.append("     YEAR(TRANSFER_EDATE) AS CONNYEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("     TRANSFERCD = '1' ");
        stb.append("     AND ");
        stb.append("     (MONTH(TRANSFER_EDATE) = '3' AND DAY(TRANSFER_EDATE) = '31') ");
        stb.append(" ) ");
        //OUTER JOINで紐づくものは紐づけて、"紐づかない物はそのまま"持ってくる。
        //イメージとしては、下記
        //  T1開始、T1終了、結合条件、T2開始、T2終了
        //  あり    あり       ○      あり    あり  ->T1開始、T2終了 <-連続データ
        //  あり    あり       ×       -       -    ->T1開始、T1終了 <-3/31終了データ
        //   -       -         ×      あり    あり  ->T2開始、T2終了 <-4/1開始データ
        stb.append(" SELECT ");
        stb.append("   CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCHREGNO ELSE T2.SCHREGNO END AS SCHREGNO, ");
        stb.append("   CASE WHEN T1.TRANSFER_SDATE IS NOT NULL THEN T1.TRANSFER_SDATE ELSE T2.TRANSFER_SDATE END AS SDATE, ");
        stb.append("   CASE WHEN T2.TRANSFER_EDATE IS NOT NULL THEN T2.TRANSFER_EDATE ELSE T1.TRANSFER_EDATE END AS EDATE ");
        stb.append(" FROM ");
        stb.append("   HASIHASI3_T T1 ");
        stb.append("   FULL OUTER JOIN HASIHASI4_T T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T1.CONNYEAR = T2.CONNYEAR ");
        //3/31終了，4/1開始"以外"のデータ
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     TRANSFER_SDATE AS SDATE, ");
        stb.append("     TRANSFER_EDATE AS EDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("     TRANSFERCD = '1' ");
        stb.append("     AND ");
        stb.append("     (MONTH(TRANSFER_SDATE) <> '4' OR DAY(TRANSFER_SDATE) <>'1') ");
        stb.append("     AND ");
        stb.append("     (MONTH(TRANSFER_EDATE) <> '3' OR DAY(TRANSFER_EDATE) <> '31') ");
        stb.append(" ORDER BY ");
        stb.append("   SCHREGNO, ");
        stb.append("   SDATE ");
        return stb.toString();
    }

    private class DataInfo1 {
        final String _schregno;
        final String _sdate;
        final String _edate;
        public DataInfo1(final String schregno, final String sdate, final String edate)
        {
            _schregno = schregno;
            _sdate = sdate;
            _edate = edate;
        }
    }

    private Map getDataInfo2(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        final String sql = getDataInfo2Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String appointedDay = rs.getString("APPOINTED_DAY");
                final String lesson = rs.getString("LESSON");
                final String offdays = rs.getString("OFFDAYS");
                final String absent = rs.getString("ABSENT");
                final String suspend = rs.getString("SUSPEND");
                final String mourning = rs.getString("MOURNING");
                final String abroad = rs.getString("ABROAD");
                final String sick = rs.getString("SICK");
                final String notice = rs.getString("NOTICE");
                final String nonotice = rs.getString("NONOTICE");
                final String nurseoff = rs.getString("NURSEOFF");
                final String late = rs.getString("LATE");
                final String early = rs.getString("EARLY");
                final String virus = rs.getString("VIRUS");
                final String koudome = rs.getString("KOUDOME");
                final DataInfo2 addWk = new DataInfo2(schregNo, classCd, schoolKind, curriculumCd, subclassCd, appointedDay, lesson, offdays, absent, suspend, mourning, abroad, sick, notice, nonotice, nurseoff, late, early, virus, koudome);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sndKey = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(sndKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo2Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ATTSUM_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ");
        stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
        stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
        stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
        stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
        stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
        stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
        stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
        stb.append("     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ");
        stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
        stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
        stb.append("     SUM(VALUE(VIRUS, 0)) AS VIRUS, ");
        stb.append("     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     COPYCD = '0' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.APPOINTED_DAY, ");
        stb.append("   T1.LESSON, ");
        stb.append("   T1.OFFDAYS, ");
        stb.append("   T1.ABSENT, ");
        stb.append("   T1.SUSPEND, ");
        stb.append("   T1.MOURNING, ");
        stb.append("   T1.ABROAD, ");
        stb.append("   T1.SICK, ");
        stb.append("   T1.NOTICE, ");
        stb.append("   T1.NONOTICE, ");
        stb.append("   T1.NURSEOFF, ");
        stb.append("   T1.LATE, ");
        stb.append("   T1.EARLY, ");
        stb.append("   T1.VIRUS, ");
        stb.append("   T1.KOUDOME ");
        stb.append(" FROM ");
        stb.append("     ATTSUM_T T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");
        return stb.toString();
    }

    private class DataInfo2 {
        final String _schregNo;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _appointedDay;
        final String _lesson;
        final String _offdays;
        final String _absent;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _sick;
        final String _notice;
        final String _nonotice;
        final String _nurseoff;
        final String _late;
        final String _early;
        final String _virus;
        final String _koudome;
        public DataInfo2(final String schregNo, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String appointedDay, final String lesson, final String offdays, final String absent, final String suspend, final String mourning, final String abroad, final String sick, final String notice, final String nonotice, final String nurseoff, final String late, final String early, final String virus, final String koudome)
        {
            _schregNo = schregNo;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _appointedDay = appointedDay;
            _lesson = lesson;
            _offdays = offdays;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _sick = sick;
            _notice = notice;
            _nonotice = nonotice;
            _nurseoff = nurseoff;
            _late = late;
            _early = early;
            _virus = virus;
            _koudome = koudome;
        }
        public boolean calcP2() {
            return 2 * (Integer.parseInt(_sick) + Integer.parseInt(_notice) + Integer.parseInt(_nonotice)) > Integer.parseInt(_lesson) - Integer.parseInt(_offdays) - Integer.parseInt(_abroad) - Integer.parseInt(_absent) - Integer.parseInt(_mourning);
        }
    }

    private Map getDataInfo3(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        final String sql = getDataInfo3Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String astMk = rs.getString("AST_MK");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final String score = rs.getString("SCORE");
                final DataInfo3 addWk = new DataInfo3(schregNo, astMk, classCd, schoolKind, curriculumCd, subclassCd, subclassName, credits, score);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sndKey = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(sndKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo3Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ATTSUM_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ");
        stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
        stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
        stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
        stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
        stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
        stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
        stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
        stb.append("     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ");
        stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
        stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
        stb.append("     SUM(VALUE(VIRUS, 0)) AS VIRUS, ");
        stb.append("     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     COPYCD = '0' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD ");
        stb.append(" ), MERGESCORE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   TR1.YEAR, ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ");
        stb.append("   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.SCORE END AS SCORE, ");
        stb.append("   T8.CREDITS ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT TR1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = TR1.YEAR ");
        stb.append("      AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("      AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("     LEFT JOIN CREDIT_MST T8 ");
        stb.append("       ON T8.YEAR = TR1.YEAR ");
        stb.append("      AND T8.GRADE = T2.GRADE ");
        stb.append("      AND T8.COURSECD = T2.COURSECD ");
        stb.append("      AND T8.MAJORCD = T2.MAJORCD ");
        stb.append("      AND T8.COURSECODE = T2.COURSECODE ");
        stb.append("      AND T8.CLASSCD = TR1.CLASSCD ");
        stb.append("      AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("      AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("      AND T8.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ");
        stb.append("     ON R1.YEAR = TR1.YEAR ");
        stb.append("    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ");
        stb.append("    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("    AND R1.CALCULATE_CREDIT_FLG = '2' ");
        stb.append(" WHERE ");
        stb.append("     TR1.YEAR = '" + _param._year + "' ");
        stb.append("     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '" + SEARCHTESTCD1 + "' ");
        stb.append("     AND VALUE(TR1.SCORE,0) <= 3 ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND TR1.CLASSCD <= '90' ");
        stb.append("     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ");
        stb.append("                                SELECT DISTINCT ");
        stb.append("                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                                FROM ");
        stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                WHERE ");
        stb.append("                                    REPLACECD = '1' ");
        stb.append("                                    AND YEAR = '" + _param._year + "' ");
        stb.append("                                    AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                               ) ");
        stb.append(" ), SUMMSCORE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   CASE WHEN COUNT(SCORE) = 0 THEN NULL ELSE SUM(VALUE(SCORE, 0)) END AS SCORE, ");
        stb.append("   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ");
        stb.append(" FROM ");
        stb.append("   MERGESCORE_T ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCORE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   CASE WHEN 3 * (T1.SICK + T1.NOTICE + T1.NONOTICE) > (T1.LESSON - T1.OFFDAYS - T1.ABROAD) THEN '*' ELSE '' END AS AST_MK, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD, ");
        stb.append("   T7.SUBCLASSNAME, ");
        stb.append("   TR1.CREDITS, ");
        stb.append("   TR1.SCORE ");
        stb.append(" FROM ");
        stb.append("   SUMMSCORE_T TR1 ");
        stb.append("   LEFT JOIN ATTSUM_T T1 ");
        stb.append("     ON T1.YEAR = TR1.YEAR ");
        stb.append("    AND T1.SCHREGNO = TR1.SCHREGNO ");
        stb.append("    AND T1.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T1.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T1.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T1.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = TR1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("   LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("     ON T7.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");
        return stb.toString();
    }

    private class DataInfo3 {
        final String _schregNo;
        final String _astMk;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _credits;
        final String _score;
        public DataInfo3(final String schregNo, final String astMk, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassName, final String credits, final String score)
        {
            _schregNo = schregNo;
            _astMk = astMk;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits = credits;
            _score = score;
        }
        public String printScore() {
            return "".equals(StringUtils.defaultString(_score, "")) ? "×" : (_astMk + _score);
        }
    }

    private Map getDataInfo4(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        final String sql = getDataInfo4Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String astMk = rs.getString("AST_MK");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final String score = rs.getString("SCORE");
                final DataInfo4 addWk = new DataInfo4(schregNo, astMk, classCd, schoolKind, curriculumCd, subclassCd, subclassName, credits, score);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sndKey = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(sndKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo4Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ATTSUM_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ");
        stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
        stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
        stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
        stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
        stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
        stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
        stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
        stb.append("     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ");
        stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
        stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
        stb.append("     SUM(VALUE(VIRUS, 0)) AS VIRUS, ");
        stb.append("     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     COPYCD = '0' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD ");
        stb.append(" ), MERGESCORE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   TR1.YEAR, ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ");
        stb.append("   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.SCORE END AS SCORE, ");
        stb.append("   T8.CREDITS ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT TR1 ");
        stb.append("   LEFT JOIN ATTSUM_T T1 ");
        stb.append("     ON T1.YEAR = TR1.YEAR ");
        stb.append("    AND T1.SCHREGNO = TR1.SCHREGNO ");
        stb.append("    AND T1.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T1.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T1.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T1.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = TR1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("   LEFT JOIN CREDIT_MST T8 ");
        stb.append("     ON T8.YEAR = TR1.YEAR ");
        stb.append("    AND T8.GRADE = T2.GRADE ");
        stb.append("    AND T8.COURSECD = T2.COURSECD ");
        stb.append("    AND T8.MAJORCD = T2.MAJORCD ");
        stb.append("    AND T8.COURSECODE = T2.COURSECODE ");
        stb.append("    AND T8.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ");
        stb.append("     ON R1.YEAR = TR1.YEAR ");
        stb.append("    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ");
        stb.append("    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("    AND R1.CALCULATE_CREDIT_FLG = '2' ");
        stb.append(" WHERE ");
        stb.append("     TR1.YEAR = '" + _param._year + "' ");
        stb.append("     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '" + SEARCHTESTCD1 + "' ");
        stb.append("     AND VALUE(TR1.SCORE,0) <= 3 ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND TR1.CLASSCD <= '90' ");
        stb.append("     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ");
        stb.append("                                SELECT DISTINCT ");
        stb.append("                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                                FROM ");
        stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                WHERE ");
        stb.append("                                    REPLACECD = '1' ");
        stb.append("                                    AND YEAR = '" + _param._year + "' ");
        stb.append("                                    AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                               ) ");
        stb.append(" ), SUMMSCORE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   CASE WHEN COUNT(SCORE) = 0 THEN NULL ELSE SUM(VALUE(SCORE, 0)) END AS SCORE, ");
        stb.append("   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ");
        stb.append(" FROM ");
        stb.append("   MERGESCORE_T ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCORE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   CASE WHEN 3 * (T1.SICK + T1.NOTICE + T1.NONOTICE) > (T1.LESSON - T1.OFFDAYS - T1.ABROAD) THEN '*' ELSE '' END AS AST_MK, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD, ");
        stb.append("   T7.SUBCLASSNAME, ");
        stb.append("   TR1.CREDITS, ");
        stb.append("   TR1.SCORE ");
        stb.append(" FROM ");
        stb.append("   SUMMSCORE_T TR1 ");
        stb.append("   LEFT JOIN ATTSUM_T T1 ");
        stb.append("     ON T1.YEAR = TR1.YEAR ");
        stb.append("    AND T1.SCHREGNO = TR1.SCHREGNO ");
        stb.append("    AND T1.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T1.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T1.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T1.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = TR1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("   LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("     ON T7.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");
        return stb.toString();
    }

    private class DataInfo4 {
        final String _schregNo;
        final String _astMk;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _credits;
        final String _score;
        public DataInfo4(final String schregNo, final String astMk, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassName, final String credits, final String score)
        {
            _schregNo = schregNo;
            _astMk = astMk;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits = credits;
            _score = score;
        }
        public String printScore() {
            return "".equals(StringUtils.defaultString(_score, "")) ? "×" : (_astMk + _score);
        }
    }

    private Map getDataInfo5(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        final String sql = getDataInfo5Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String appointedDay = rs.getString("APPOINTED_DAY");
                final String lesson = rs.getString("LESSON");
                final String offdays = rs.getString("OFFDAYS");
                final String absent = rs.getString("ABSENT");
                final String suspend = rs.getString("SUSPEND");
                final String mourning = rs.getString("MOURNING");
                final String abroad = rs.getString("ABROAD");
                final String sick = rs.getString("SICK");
                final String notice = rs.getString("NOTICE");
                final String nonotice = rs.getString("NONOTICE");
                final String nurseoff = rs.getString("NURSEOFF");
                final String late = rs.getString("LATE");
                final String early = rs.getString("EARLY");
                final String virus = rs.getString("VIRUS");
                final String koudome = rs.getString("KOUDOME");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final DataInfo5 addWk = new DataInfo5(schregNo, classCd, schoolKind, curriculumCd, subclassCd, appointedDay, lesson, offdays, absent, suspend, mourning, abroad, sick, notice, nonotice, nurseoff, late, early, virus, koudome, subclassName, credits);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sndKey = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(sndKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo5Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ATTSUM_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     SUM(VALUE(APPOINTED_DAY, 0)) AS APPOINTED_DAY, ");
        stb.append("     SUM(VALUE(LESSON, 0)) AS LESSON, ");
        stb.append("     SUM(VALUE(OFFDAYS, 0)) AS OFFDAYS, ");
        stb.append("     SUM(VALUE(ABSENT, 0)) AS ABSENT, ");
        stb.append("     SUM(VALUE(SUSPEND, 0)) AS SUSPEND, ");
        stb.append("     SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
        stb.append("     SUM(VALUE(ABROAD, 0)) AS ABROAD, ");
        stb.append("     SUM(VALUE(SICK, 0)) AS SICK, ");
        stb.append("     SUM(VALUE(NOTICE, 0)) AS NOTICE, ");
        stb.append("     SUM(VALUE(NONOTICE, 0)) AS NONOTICE, ");
        stb.append("     SUM(VALUE(NURSEOFF, 0)) AS NURSEOFF, ");
        stb.append("     SUM(VALUE(LATE, 0)) AS LATE, ");
        stb.append("     SUM(VALUE(EARLY, 0)) AS EARLY, ");
        stb.append("     SUM(VALUE(VIRUS, 0)) AS VIRUS, ");
        stb.append("     SUM(VALUE(KOUDOME, 0)) AS KOUDOME ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     COPYCD = '0' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD ");
        stb.append(" ), MERGECREDIT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE T1.CLASSCD END AS CLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE T1.SCHOOL_KIND END AS SCHOOL_KIND, ");
        stb.append("   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE T1.CURRICULUM_CD END AS CURRICULUM_CD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE T1.SUBCLASSCD END AS SUBCLASSCD, ");
        stb.append("   T3.CREDITS ");
        stb.append(" FROM ");
        stb.append("   ATTSUM_T T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN CREDIT_MST T3 ");
        stb.append("     ON T3.YEAR = T2.YEAR ");
        stb.append("    AND T3.GRADE = T2.GRADE ");
        stb.append("    AND T3.COURSECD = T2.COURSECD ");
        stb.append("    AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("    AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("    AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ");
        stb.append("     ON R1.YEAR = T1.YEAR ");
        stb.append("    AND R1.ATTEND_CLASSCD = T1.CLASSCD ");
        stb.append("    AND R1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND R1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND R1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("    AND R1.CALCULATE_CREDIT_FLG = '2' ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("   AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN ( ");
        stb.append("                              SELECT DISTINCT ");
        stb.append("                                  ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                              FROM ");
        stb.append("                                  SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                              WHERE ");
        stb.append("                                  REPLACECD = '1' ");
        stb.append("                                  AND YEAR = '" + _param._year + "' ");
        stb.append("                                  AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                             ) ");
        stb.append(" ), SUMMCREDIT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ");
        stb.append(" FROM ");
        stb.append("   MERGECREDIT_T ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.APPOINTED_DAY, ");
        stb.append("   T1.LESSON, ");
        stb.append("   T1.OFFDAYS, ");
        stb.append("   T1.ABSENT, ");
        stb.append("   T1.SUSPEND, ");
        stb.append("   T1.MOURNING, ");
        stb.append("   T1.ABROAD, ");
        stb.append("   T1.SICK, ");
        stb.append("   T1.NOTICE, ");
        stb.append("   T1.NONOTICE, ");
        stb.append("   T1.NURSEOFF, ");
        stb.append("   T1.LATE, ");
        stb.append("   T1.EARLY, ");
        stb.append("   T1.VIRUS, ");
        stb.append("   T1.KOUDOME, ");
        stb.append("   T4.SUBCLASSNAME, ");
        stb.append("   T3.CREDITS ");
        stb.append(" FROM ");
        stb.append("     ATTSUM_T T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SUMMCREDIT_T T3 ");
        stb.append("       ON T3.YEAR = T1.YEAR ");
        stb.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("      AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST T4 ");
        stb.append("       ON T4.CLASSCD = T1.CLASSCD ");
        stb.append("      AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN ( ");
        stb.append("                                SELECT DISTINCT ");
        stb.append("                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                                FROM ");
        stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                WHERE ");
        stb.append("                                    REPLACECD = '1' ");
        stb.append("                                    AND YEAR = '" + _param._year + "' ");
        stb.append("                                    AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                               ) ");
        stb.append("     AND (T1.LESSON - T1.OFFDAYS - T1.ABROAD) > 0 ");
        stb.append(" ORDER BY ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");
        return stb.toString();
    }

    private class DataInfo5 {
        final String _schregNo;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _appointedDay;
        final String _lesson;
        final String _offdays;
        final String _absent;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _sick;
        final String _notice;
        final String _nonotice;
        final String _nurseoff;
        final String _late;
        final String _early;
        final String _virus;
        final String _koudome;
        final String _credits;
        public DataInfo5(final String schregNo, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String appointedDay, final String lesson, final String offdays, final String absent, final String suspend, final String mourning, final String abroad, final String sick, final String notice, final String nonotice, final String nurseoff, final String late, final String early, final String virus, final String koudome, final String subclassName, final String credits)
        {
            _schregNo = schregNo;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _appointedDay = appointedDay;
            _lesson = lesson;
            _offdays = offdays;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _sick = sick;
            _notice = notice;
            _nonotice = nonotice;
            _nurseoff = nurseoff;
            _late = late;
            _early = early;
            _virus = virus;
            _koudome = koudome;
            _subclassName = subclassName;
            _credits = credits;
        }
        public boolean calcP2() {
            return 2 * calcSick() >= calcLesson();
        }
        public int calcSick() {
            return Integer.parseInt(_sick) + Integer.parseInt(_notice) + Integer.parseInt(_nonotice);
        }
        public int calcLesson() {
            return Integer.parseInt(_lesson) - Integer.parseInt(_offdays) - Integer.parseInt(_abroad) - Integer.parseInt(_absent) - Integer.parseInt(_mourning);
        }
    }

    private Map getDataInfo6(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        final String sql = getDataInfo6Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credits = rs.getString("CREDITS");
                final String score = rs.getString("SCORE");
                final DataInfo6 addWk = new DataInfo6(schregNo, classCd, schoolKind, curriculumCd, subclassCd, subclassName, credits, score);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sndKey = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(sndKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private class DataInfo6 {
        final String _schregNo;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _credits;
        final String _score;
        public DataInfo6 (final String schregNo, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassName, final String credits, final String score)
        {
            _schregNo = schregNo;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credits = credits;
            _score = score;
        }
        public String printScore() {
            return "".equals(StringUtils.defaultString(_score, "")) ? "×" : _score;
        }
    }

    private String getDataInfo6Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MERGESCORE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   TR1.YEAR, ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ");
        stb.append("   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.SCORE END AS SCORE, ");
        stb.append("   T8.CREDITS ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT TR1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = TR1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("   LEFT JOIN CREDIT_MST T8 ");
        stb.append("     ON T8.YEAR = TR1.YEAR ");
        stb.append("    AND T8.GRADE = T2.GRADE ");
        stb.append("    AND T8.COURSECD = T2.COURSECD ");
        stb.append("    AND T8.MAJORCD = T2.MAJORCD ");
        stb.append("    AND T8.COURSECODE = T2.COURSECODE ");
        stb.append("    AND T8.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ");
        stb.append("     ON R1.YEAR = TR1.YEAR ");
        stb.append("    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ");
        stb.append("    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("    AND R1.CALCULATE_CREDIT_FLG = '2' ");
        stb.append(" WHERE ");
        stb.append("     TR1.YEAR = '" + _param._year + "' ");
        stb.append("     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '" + SEARCHTESTCD1 + "' ");
        stb.append("     AND VALUE(TR1.SCORE,0) <= 2 ");
        stb.append("     AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("     AND TR1.CLASSCD <= '90' ");
        stb.append("     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ");
        stb.append("                                SELECT DISTINCT ");
        stb.append("                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                                FROM ");
        stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                WHERE ");
        stb.append("                                    REPLACECD = '1' ");
        stb.append("                                    AND YEAR = '" + _param._year + "' ");
        stb.append("                                    AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                               ) ");
        stb.append(" ), SUMMSCORE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   CASE WHEN COUNT(SCORE) = 0 THEN NULL ELSE SUM(VALUE(SCORE, 0)) END AS SCORE, ");
        stb.append("   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ");
        stb.append(" FROM ");
        stb.append("   MERGESCORE_T ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCORE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD, ");
        stb.append("   T7.SUBCLASSNAME, ");
        stb.append("   TR1.CREDITS, ");
        stb.append("   TR1.SCORE ");
        stb.append(" FROM ");
        stb.append("     SUMMSCORE_T TR1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = TR1.YEAR ");
        stb.append("      AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("      AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("       ON T7.CLASSCD = TR1.CLASSCD ");
        stb.append("      AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("      AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("      AND T7.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");
        return stb.toString();
    }

    private Map getDataInfo7(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        List detailList = null;
        final String sql = getDataInfo7Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String courseCode = rs.getString("COURSECODE");
                final String requireFlg = rs.getString("REQUIRE_FLG");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String className = rs.getString("CLASSNAME");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String cnt = rs.getString("CNT");
                final DataInfo7 addWk = new DataInfo7(courseCode, requireFlg, classCd, schoolKind, curriculumCd, subclassCd, className, subclassName, cnt);
                if (!retMap.containsKey(courseCode)) {
                    subMap = new LinkedMap();
                    retMap.put(courseCode, subMap);
                } else {
                    subMap = (Map)retMap.get(courseCode);
                }
                if (!subMap.containsKey(requireFlg)) {
                    detailList = new ArrayList();
                    subMap.put(requireFlg, detailList);
                } else {
                    detailList = (List)subMap.get(requireFlg);
                }
                detailList.add(addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo7Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   CASE WHEN T8.REQUIRE_FLG = '1' THEN '1' ELSE '0' END AS REQUIRE_FLG, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD, ");
        stb.append("   T9.CLASSNAME, ");
        stb.append("   T7.SUBCLASSNAME, ");
        stb.append("   COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT TR1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = TR1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("   LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("     ON T7.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN CREDIT_MST T8 ");
        stb.append("     ON T8.YEAR = TR1.YEAR ");
        stb.append("    AND T8.GRADE = T2.GRADE ");
        stb.append("    AND T8.COURSECD = T2.COURSECD ");
        stb.append("    AND T8.MAJORCD = T2.MAJORCD ");
        stb.append("    AND T8.COURSECODE = T2.COURSECODE ");
        stb.append("    AND T8.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN CLASS_MST T9 ");
        stb.append("     ON T9.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T9.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("   TR1.YEAR = '" + _param._year + "' ");
        stb.append("   AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '" + SEARCHTESTCD1 + "' ");
        stb.append("   AND VALUE(TR1.SCORE,0) <= 3 ");
        stb.append("   AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("   AND TR1.CLASSCD <= '90' ");
        stb.append("   AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ");
        stb.append("                              SELECT DISTINCT ");
        stb.append("                                  ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                              FROM ");
        stb.append("                                  SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                              WHERE ");
        stb.append("                                  REPLACECD = '1' ");
        stb.append("                                  AND YEAR = '" + _param._year + "' ");
        stb.append("                                    AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                             ) ");
        stb.append(" GROUP BY ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   T8.REQUIRE_FLG, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD, ");
        stb.append("   T9.CLASSNAME, ");
        stb.append("   T7.SUBCLASSNAME ");
        stb.append(" ORDER BY ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   T8.REQUIRE_FLG, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD ");
        return stb.toString();
    }

    private class DataInfo7 {
        final String _courseCode;
        final String _requireFlg;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _className;
        final String _subclassName;
        final String _cnt;
        public DataInfo7 (final String courseCode, final String requireFlg, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String className, final String subclassName, final String cnt)
        {
            _courseCode = courseCode;
            _requireFlg = requireFlg;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _className = className;
            _subclassName = subclassName;
            _cnt = cnt;
        }
    }

    private Map getDataInfo8(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = null;
        final String sql = getDataInfo8Sql();
        log.debug(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String year = rs.getString("YEAR");
                final String yGrade = rs.getString("YGRADE");
                final String stat = rs.getString("STAT");
                final String credits = rs.getString("CREDITS");
                final DataInfo8 addWk = new DataInfo8(schregNo, classCd, schoolKind, curriculumCd, subclassCd, subclassName, year, yGrade, stat, credits);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                final String sndKey = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                subMap.put(sndKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getDataInfo8Sql() {
        final StringBuffer stb = new StringBuffer();
        //当年度と過年度分を分けて取得して、並べる。
        //最初に単位の集計を行う(先科目に対してCALCULATE_CREDIT_FLGに応じて除外、集計を行う)
        stb.append(" WITH MERGECREDIT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   TR1.YEAR, ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   CASE WHEN R1.COMBINED_CLASSCD IS NOT NULL THEN R1.COMBINED_CLASSCD ELSE TR1.CLASSCD END AS CLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SCHOOL_KIND IS NOT NULL THEN R1.COMBINED_SCHOOL_KIND ELSE TR1.SCHOOL_KIND END AS SCHOOL_KIND, ");
        stb.append("   CASE WHEN R1.COMBINED_CURRICULUM_CD IS NOT NULL THEN R1.COMBINED_CURRICULUM_CD ELSE TR1.CURRICULUM_CD END AS CURRICULUM_CD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN R1.COMBINED_SUBCLASSCD ELSE TR1.SUBCLASSCD END AS SUBCLASSCD, ");
        stb.append("   CASE WHEN R1.COMBINED_SUBCLASSCD IS NOT NULL THEN NULL ELSE TR1.GET_CREDIT END AS GET_CREDIT, ");
        stb.append("   T8.CREDITS ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT TR1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON ((T2.YEAR < TR1.YEAR ");
        stb.append("            AND T2.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR < T2.YEAR AND SEMESTER <> '9') ");
        stb.append("         ) OR (T2.YEAR = TR1.YEAR ");
        stb.append("            AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("         )) ");
        stb.append("    AND T2.SCHREGNO = TR1.SCHREGNO ");
        stb.append("   LEFT JOIN CREDIT_MST T8 ");
        stb.append("     ON T8.YEAR = TR1.YEAR ");
        stb.append("    AND T8.GRADE = T2.GRADE ");
        stb.append("    AND T8.COURSECD = T2.COURSECD ");
        stb.append("    AND T8.MAJORCD = T2.MAJORCD ");
        stb.append("    AND T8.COURSECODE = T2.COURSECODE ");
        stb.append("    AND T8.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T8.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T8.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T8.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT R1 ");
        stb.append("     ON R1.YEAR = TR1.YEAR ");
        stb.append("    AND R1.ATTEND_CLASSCD = TR1.CLASSCD ");
        stb.append("    AND R1.ATTEND_SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND R1.ATTEND_CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND R1.ATTEND_SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append("    AND R1.CALCULATE_CREDIT_FLG = '2' ");
        stb.append(" WHERE ");
        stb.append("     TR1.YEAR <= '" + _param._year + "' ");
        stb.append("     AND TR1.SEMESTER || '-' || TR1.TESTKINDCD || TR1.TESTITEMCD || TR1.SCORE_DIV = '" + SEARCHTESTCD1 + "' ");
        stb.append("     AND TR1.CLASSCD <= '90' ");
        stb.append("     AND (TR1.CLASSCD, TR1.SCHOOL_KIND, TR1.CURRICULUM_CD, TR1.SUBCLASSCD) NOT IN ( ");
        stb.append("                                SELECT DISTINCT ");
        stb.append("                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
        stb.append("                                FROM ");
        stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("                                WHERE ");
        stb.append("                                    REPLACECD = '1' ");
        stb.append("                                    AND YEAR = TR1.YEAR ");
        stb.append("                                    AND CALCULATE_CREDIT_FLG = '1' ");
        stb.append("                               ) ");
        //単位の集計
        stb.append(" ), SUMMCREDIT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   GET_CREDIT, ");
        stb.append("   CASE WHEN COUNT(CREDITS) = 0 THEN NULL ELSE SUM(VALUE(CREDITS, 0)) END AS CREDITS ");
        stb.append(" FROM ");
        stb.append("   MERGECREDIT_T ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SCHREGNO, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   GET_CREDIT ");
        //ベースとなるデータ
        stb.append(" ), STATBASE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   TR1.YEAR, ");
        stb.append("   TR1.SCHREGNO, ");
        stb.append("   TR1.CLASSCD, ");
        stb.append("   TR1.SCHOOL_KIND, ");
        stb.append("   TR1.CURRICULUM_CD, ");
        stb.append("   TR1.SUBCLASSCD, ");
        stb.append("   TR1.GET_CREDIT, ");
        stb.append("   TR1.CREDITS, ");
        stb.append("   T7.SUBCLASSNAME, ");
        stb.append("   CASE WHEN TR1.GET_CREDIT IS NULL THEN '未入力' ELSE '未履修' END AS STAT ");
        stb.append(" FROM ");
        stb.append("   SUMMCREDIT_T TR1 ");
        stb.append("   LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("     ON T7.CLASSCD = TR1.CLASSCD ");
        stb.append("    AND T7.SCHOOL_KIND = TR1.SCHOOL_KIND ");
        stb.append("    AND T7.CURRICULUM_CD = TR1.CURRICULUM_CD ");
        stb.append("    AND T7.SUBCLASSCD = TR1.SUBCLASSCD ");
        stb.append(" ), STAT_Y_T AS ( ");
        //当年度分
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SUBCLASSNAME, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T2.GRADE AS YGRADE, ");
        stb.append("   T1.STAT, ");
        stb.append("   T1.CREDITS ");
        stb.append(" FROM ");
        stb.append("   STATBASE_T T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = '" + _param._useSemester + "' ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T2.GRADE = '" + _param._grade + "' ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SUBCLASSCD = '" + SEARCHSUBCLSCD1 + "' ");
        stb.append("   AND (CASE WHEN T1.GET_CREDIT IS NULL THEN '' ELSE TRIM(T1.GET_CREDIT) END) = '' ");
        //過年度分
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SUBCLASSNAME, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T3.GRADE AS YGRADE, ");
        stb.append("   T1.STAT, ");
        stb.append("   T1.CREDITS ");
        stb.append(" FROM ");
        stb.append("   STATBASE_T T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER = (SELECT MAX(TX.SEMESTER) FROM SEMESTER_MST TX WHERE TX.YEAR = T3.YEAR AND TX.SEMESTER <> '9') ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR < '" + _param._year + "' ");
        stb.append("   AND T1.SUBCLASSCD = '" + SEARCHSUBCLSCD1 + "' ");
        stb.append("   AND (CASE WHEN T1.GET_CREDIT IS NULL THEN '' ELSE TRIM(T1.GET_CREDIT) END) = '' ");
        stb.append("   AND T1.SCHREGNO IN (SELECT T2.SCHREGNO FROM SCHREG_REGD_DAT T2 WHERE T2.YEAR = '" + _param._year + "' AND T2.SEMESTER = '" + _param._useSemester + "' AND T2.GRADE = '" + _param._grade + "') ");
        stb.append(" ) ");
        //並び順を指定
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.SUBCLASSNAME, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.YGRADE, ");
        stb.append("   T1.STAT, ");
        stb.append("   T1.CREDITS ");
        stb.append(" FROM ");
        stb.append("   STAT_Y_T T1 ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.YGRADE ");
        return stb.toString();
    }

    private class DataInfo8 {
        final String _schregNo;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _year;
        final String _yGrade;
        final String _stat;
        final String _credits;
        public DataInfo8(final String schregNo, final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassName, final String year, final String yGrade, final String stat, final String credits)
        {
            _schregNo = schregNo;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _year = year;
            _yGrade = yGrade;
            _stat = stat;
            _credits = credits;
        }
    }

    private Map getSchInfo(final DB2UDB db2, final Map dataInfoMap) {
        final Map retMap = new LinkedMap();
        final String sql = getSchInfoSql(dataInfoMap);
        log.info(" sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String syussin = rs.getString("SYUSSIN");
                final String ryunen1 = rs.getString("RYUNEN1");
                final String ryunen2 = rs.getString("RYUNEN2");
                final String ryunen3 = rs.getString("RYUNEN3");
                final SchInfo addWk = new SchInfo(schregNo, hrClass, hrName, attendNo, name, sex, syussin, ryunen1, ryunen2, ryunen3);
                retMap.put(schregNo, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getSchInfoSql(final Map dataInfoMap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH RYUUNEN_BASE_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("    SCHREGNO, ");
        stb.append("    YEAR, ");
        stb.append("    GRADE ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR <= '" + _param._year + "' ");
        stb.append(" ), RYUUNEN_SUM_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("    SCHREGNO, ");
        stb.append("    COUNT(YEAR) AS CNT, ");
        stb.append("    GRADE ");
        stb.append(" FROM ");
        stb.append("    RYUUNEN_BASE_T ");
        stb.append(" GROUP BY ");
        stb.append("    SCHREGNO, ");
        stb.append("    GRADE ");
        stb.append(" ), RYUUNEN_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T2.CNT, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T3.GRADE_CD ");
        stb.append(" FROM ");
        stb.append("    RYUUNEN_BASE_T T1 ");
        stb.append("    LEFT JOIN RYUUNEN_SUM_T T2 ");
        stb.append("      ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.GRADE = T1.GRADE ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("      ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("    T3.SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _param._year + "' AND GRADE = '" + _param._grade + "') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T2.SCHREGNO, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T4.HR_NAME, ");
        stb.append("   T2.ATTENDNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   T3.SEX, ");
        stb.append("   L1.NAME1 AS SYUSSIN, ");
        stb.append("   CASE WHEN VALUE(T7.GRADE_CD, 0) >= 1 THEN CASE WHEN T6_1.CNT > 1 THEN '" + RYUKYU_MARK + "' ELSE '―' END ELSE '　' END AS RYUNEN1, ");
        stb.append("   CASE WHEN VALUE(T7.GRADE_CD, 0) >= 2 THEN CASE WHEN T6_2.CNT > 1 THEN '" + RYUKYU_MARK + "' ELSE '―' END ELSE '　' END AS RYUNEN2, ");
        stb.append("   CASE WHEN VALUE(T7.GRADE_CD, 0) >= 3 THEN CASE WHEN T6_3.CNT > 1 THEN '" + RYUKYU_MARK + "' ELSE '―' END ELSE '　' END AS RYUNEN3, ");
        stb.append("   '' AS SUBCLS_CNT, ");
        stb.append("   '' AS CREDIT_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("       ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T4 ");
        stb.append("       ON T4.YEAR = T2.YEAR ");
        stb.append("      AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("      AND T4.GRADE = T2.GRADE ");
        stb.append("      AND T4.HR_CLASS = T2.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST T5 ");
        stb.append("       ON T5.SCHREGNO = T2.SCHREGNO ");
        stb.append("      AND T5.BASE_SEQ = '016' ");
        stb.append("     LEFT JOIN V_NAME_MST L1 ");
        stb.append("       ON L1.NAMECD1 = 'A053' ");
        stb.append("      AND L1.NAMECD2 = T5.BASE_REMARK1 ");
        stb.append("     LEFT JOIN RYUUNEN_T T6_1 ");
        stb.append("       ON T6_1.SCHREGNO = T2.SCHREGNO ");
        stb.append("      AND T6_1.GRADE_CD = '01' ");
        stb.append("     LEFT JOIN RYUUNEN_T T6_2 ");
        stb.append("       ON T6_2.SCHREGNO = T2.SCHREGNO ");
        stb.append("      AND T6_2.GRADE_CD = '02' ");
        stb.append("     LEFT JOIN RYUUNEN_T T6_3 ");
        stb.append("       ON T6_3.SCHREGNO = T2.SCHREGNO ");
        stb.append("      AND T6_3.GRADE_CD = '03' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T7 ");
        stb.append("       ON T7.YEAR = T2.YEAR ");
        stb.append("      AND T7.GRADE = T2.GRADE ");
        stb.append(" WHERE ");
        stb.append("      T2.YEAR = '" + _param._year + "' ");
        stb.append("      AND T2.SEMESTER = '" + (SEMEALL.equals(_param._semester) ? _param._lastSemester : _param._semester) + "' ");
        stb.append("      AND T2.GRADE = '" + _param._grade + "' ");
        String schParamStr = "";
        String delim = "";
        for (Iterator ite = dataInfoMap.keySet().iterator();ite.hasNext();) {
            schParamStr += delim + "'" + (String)ite.next() + "'";
            delim = ",";
        }
        stb.append("      AND T2.SCHREGNO IN (" + schParamStr + ") ");
        stb.append(" ORDER BY");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T2.ATTENDNO ");

        return stb.toString();
    }

    private class SchInfo {
        final String _schregno;
        final String _hr_Class;
        final String _hr_Name;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _syussin;
        final String _ryunen1;
        final String _ryunen2;
        final String _ryunen3;
        String _subcls_Cnt;
        String _credit_Cnt;
        public SchInfo (final String schregno, final String hr_Class, final String hr_Name, final String attendNo, final String name, final String sex, final String syussin, final String ryunen1, final String ryunen2, final String ryunen3)
        {
            _schregno = schregno;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _syussin = syussin;
            _ryunen1 = ryunen1;
            _ryunen2 = ryunen2;
            _ryunen3 = ryunen3;
            _subcls_Cnt = "";
            _credit_Cnt = "";
        }
        public String getSexStr() {
            return "1".equals(_sex) ? SEX_MANMARK : "";
        }
        public String getAttendNo() {
            return "".equals(_attendNo) ? "" : String.valueOf(Integer.parseInt(_attendNo));
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _gradeCd;
        private final String _loginDate;

        private final List _printChk;

        private final String _schoolCd;
        private final String _gradeName;
        private final String _prEst;

        private final String _lastSemester;
        private final String _useSemester;

        private int _ttlCnt;
        private int _manCnt;
        private int _womanCnt;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");

            _loginDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _printChk = new ArrayList();
            for (int idx = 1;idx <= 9;idx++) {
                if (String.valueOf(idx).equals(request.getParameter("CHKBOX" + idx))) {
                    _printChk.add(idx);
                }
            }

            _lastSemester = getLastSemester(db2);
            _useSemester = SEMEALL.equals(_semester) ? _lastSemester : _semester;
            final Map getGrInfo = getGradeInfo(db2);
            if (getGrInfo != null) {
                final String gradeNameWk = KnjDbUtils.getString(getGrInfo, "GRADE_NAME1");
                _gradeName = StringUtils.defaultString(gradeNameWk, "");
                String gradeCdWk = KnjDbUtils.getString(getGrInfo, "GRADE_CD");
                if (gradeCdWk != null) {
                    gradeCdWk = String.valueOf(Integer.parseInt(gradeCdWk));
                }
                _gradeCd = StringUtils.defaultString(gradeCdWk, "");
                _prEst = getPeEst(db2);
            } else {
                _gradeName = "";
                _gradeCd =  "";
                _prEst = "";
            }

            if (_printChk.contains(1) || _printChk.contains(9)) {  //在籍者数を出力するなら、計算する。
                setGrdCnt(db2);
            }
            //_applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
        }

        private void setGrdCnt(final DB2UDB db2) {
            _ttlCnt = 0;
            _manCnt = 0;
            _womanCnt = 0;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   COUNT(T1.SCHREGNO) AS CNT, ");
            stb.append("   SUM(CASE WHEN T2.SEX = '1' THEN 1 ELSE 0 END) AS MAN_CNT, ");
            stb.append("   SUM(CASE WHEN T2.SEX = '2' THEN 1 ELSE 0 END) AS WOMAN_CNT ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN SCHREG_BASE_MST T2 ");
            stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER = '" + _useSemester + "' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");
            stb.append("   AND T2.GRD_DIV IS NULL ");
            log.debug(" sql =" + stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _ttlCnt = rs.getInt("CNT");
                    _manCnt = rs.getInt("MAN_CNT");
                    _womanCnt = rs.getInt("WOMAN_CNT");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return;
        }

        private String getLastSemester(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '" + SEMEALL +"' "));
        }
        private Map getGradeInfo(final DB2UDB db2) {
            final List getLst = KnjDbUtils.query(db2, "SELECT GRADE_CD, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
            return getLst == null ? null : (Map)getLst.get(0);
        }
        private String getPeEst(final DB2UDB db2) {
            final String prEst = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRESENT_EST FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOLCD = '" + _schoolCd + "' "));
            return (_gradeCd == null || prEst == null) ? "" : String.valueOf(Integer.parseInt(prEst) - (Integer.parseInt(_gradeCd) - 1));
        }

//        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
//            String rtn = null;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    rtn = rs.getString(field);
//                }
//            } catch (Exception e) {
//                log.error("exception!", e);
//            }
//            return rtn;
//        }

    }
}

// eof

