//kanji=漢字
/*
 *
 * 作成日: 2021/01/27 10:50:00 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *   ＜KNJC110D 出欠集計表印刷（中学）＞
 */

public class KNJC110D {

    private static final Log log = LogFactory.getLog(KNJC110D.class);

    private static final String SEMEALL = "9";
    private boolean _hasData = false;

    private static final String GET_ATT_DAY = "ATT_DDAT";

    private static final String OUTPUT_MONTH = "1";
    private static final String OUTPUT_SEMES = "2";

    private static final String DI_DAY_MOURNING = "2";
    private static final String DI_DAY_SUSPEND = "3";
    private static final String DI_DAY_ABSENT_SICK = "4";
    private static final String DI_DAY_ABSENT_NOTIFY = "5";
    private static final String DI_DAY_ABSENT_NONOTIFY = "6";
    private static final String DI_DAY_LATE = "15";
    private static final String DI_DAY_LATE_KOUNIN1 = "51";
    private static final String DI_DAY_LATE_KOUNIN2 = "56";
    private static final String DI_DAY_EARLY = "16";
    private static final String DI_DAY_EARLY_KOUNIN1 = "52";
    private static final String DI_DAY_EARLY_KOUNIN2 = "57";
    private static final String DI_DAY_ABSENT_KEKKA = "53";
    private static final String DI_DAY_ABSENT_KEKKA_KOUNIN1 = "54";
    private static final String DI_DAY_ABSENT_KEKKA_KOUNIN2 = "55";
    private static final String DI_DAY_LESSONCNT = "ZZ";

    Param _param;

    /**
     * @param request
     * @param response
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        Map schregMap = getSchregInfo(db2);
        List schregPageList = divideSchregMap(schregMap);
        final Map topKeyMap = new TreeMap();
        Map attendFullMap = getAttendInfo(db2, topKeyMap);

        final Calendar _now = Calendar.getInstance();
        _now.setTime(new Date());
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd H:m");
        final String strtDTStr = sdf.format(_now.getTime());

        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            printSub2(db2, svf, strtDTStr, schregPageList, attendFullMap, topKeyMap);
        } else {
            printSub3(db2, svf, strtDTStr, schregPageList, attendFullMap, topKeyMap);
        }
    }

    private List divideSchregMap(final Map schBaseMap) {
        final int maxSchSize = 50;  //1ページ50人出力
        final List retList = new ArrayList();
        Map subMap = new LinkedMap();
        for (Iterator ite = schBaseMap.keySet().iterator();ite.hasNext();) {
            final String schregNo = (String)ite.next();
            final SchregInfo obj = (SchregInfo)schBaseMap.get(schregNo);
            if (subMap.size() == maxSchSize) {
                retList.add(subMap);
                subMap = new LinkedMap();
            }
            subMap.put(schregNo, obj);
        }
        retList.add(subMap);
        return retList;
    }

    private Map srchDetailMap(final Map srchMap, final String schregNo, final String srchKey) {
        final Map subMap = srchMap.containsKey(schregNo) ? (Map)srchMap.get(schregNo) : new LinkedMap();
        return subMap.containsKey(srchKey) ? (Map)subMap.get(srchKey) : new LinkedMap();
    }

    private String printWithSrchDiCdLst(final Map dMap, final List srchLst, final String beforeVal) {
        int idxCnt = 0;
        String cmdStr = "";
        int totalPrintVal = 0;
        boolean dataExistFlg = false;
        for (Iterator ite = srchLst.iterator();ite.hasNext();) {
            final String chkCd = (String)ite.next();
            if (idxCnt == 0) {
                cmdStr = chkCd;
                if ("C1".equals(cmdStr)) {
                    if (!"".equals(beforeVal)) {
                        totalPrintVal = Integer.parseInt(beforeVal);
                    }
                } else if ("C2".equals(cmdStr)) {
                    return String.valueOf(beforeVal);
                }
            } else {
                if (dMap.containsKey(chkCd)) {
                    final AttendInfo att = (AttendInfo)dMap.get(chkCd);
                    dataExistFlg = true;
                    if ("+".equals(cmdStr)) {
                        totalPrintVal += Integer.parseInt(att._cnt);
                    } else if ("-".equals(cmdStr) && idxCnt > 1) {
                        totalPrintVal -= Integer.parseInt(att._cnt);
                    } else if ("C1".equals(cmdStr)) {
                        totalPrintVal = Integer.parseInt(att._cnt) - totalPrintVal;
                    } else {
                        totalPrintVal = Integer.parseInt(att._cnt);
                    }
                }
            }
            idxCnt++;
        }
        return dataExistFlg ? String.valueOf(totalPrintVal) : "";
    }

    private void printSub2(final DB2UDB db2, final Vrw32alp svf, final String strtDTStr, final List schregPageList, final Map attendFullMap, final Map topKeyMap) {
        final String form = "KNJC110D.frm";
        svf.VrSetForm(form, 1);
        final int maxCol = 6;  //最大列

        Map attDDatMap = (Map)attendFullMap.get(GET_ATT_DAY);

        final List attendKeys1 = new ArrayList();  //1枠目
        attendKeys1.add(Arrays.asList(new String[] {"+", DI_DAY_MOURNING, DI_DAY_SUSPEND}));
        attendKeys1.add(Arrays.asList(new String[] {"C1", DI_DAY_LESSONCNT}));  //ここでは(取得値-前回値)の計算->backup
        attendKeys1.add(Arrays.asList(new String[] {"+", DI_DAY_ABSENT_SICK, DI_DAY_ABSENT_NOTIFY, DI_DAY_ABSENT_NONOTIFY}));
        attendKeys1.add(Arrays.asList(new String[] {"C2", DI_DAY_LESSONCNT}));  //ここでは(backup-前回値)計算
        final List attendKeys2 = new ArrayList();  //2枠目
        attendKeys2.add(Arrays.asList(new String[] {"", DI_DAY_LATE}));
        attendKeys2.add(Arrays.asList(new String[] {"+", DI_DAY_LATE_KOUNIN1, DI_DAY_LATE_KOUNIN2}));
        final List attendKeys3 = new ArrayList();  //3枠目
        attendKeys3.add(Arrays.asList(new String[] {"", DI_DAY_EARLY}));
        attendKeys3.add(Arrays.asList(new String[] {"+", DI_DAY_EARLY_KOUNIN1, DI_DAY_EARLY_KOUNIN2}));
        final List attendKeys4 = new ArrayList();  //4枠目
        attendKeys4.add(Arrays.asList(new String[] {"", DI_DAY_ABSENT_KEKKA}));
        attendKeys4.add(Arrays.asList(new String[] {"+", DI_DAY_ABSENT_KEKKA_KOUNIN1, DI_DAY_ABSENT_KEKKA_KOUNIN2}));

        int pageNo = 1;
        for (Iterator ite = _param._semesterList.iterator();ite.hasNext();) {  //ページ管理しているキー
            final String semeStr = (String)ite.next();
            //上位キーがデータに存在するなら出力。無ければ次学期にskip
            boolean tSrchFlg = false;
            for (Iterator itt = topKeyMap.keySet().iterator();itt.hasNext();) {
                final String tKeyStr = (String)itt.next();
                if (tKeyStr.startsWith(semeStr)) {
                    tSrchFlg = true;
                    break;
                }
            }
            if (!tSrchFlg) {
                continue;
            }
            for (Iterator itl = schregPageList.iterator();itl.hasNext();) {  //生徒数でページ切替
                final Map schregMap = (Map)itl.next();
                final Map ttlSchCtrlMap = new HashMap();
                //タイトル出力
                setTitle(db2, svf, strtDTStr, pageNo, semeStr);
                int schCnt = 1;
                for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                    final String schregNo = (String)its.next();
                    final SchregInfo stu = (SchregInfo)schregMap.get(schregNo);
                    //NO
                    svf.VrsOutn("NO", schCnt, stu._attendNo);
                    //NAME
                    svf.VrsOutn("NAME1", schCnt, stu._name);
                    schCnt++;
                }
                boolean fstPrtFlg = false;
                //月でループloop
                int prtCol = 1;
                int ttlLesson = 0;
                for (Iterator itw = _param._monthList.iterator();itw.hasNext();) {
                    final String mthStr = (String)itw.next();
                    final String mwcKey = semeStr + "-" + mthStr;
                    //データが無いなら次へ。
                    if (!topKeyMap.containsKey(semeStr + "-" + mthStr)) {
                        continue;
                    }
                    if (prtCol > maxCol) {
                        svf.VrEndPage();
                        pageNo++;
                        svf.VrSetForm(form, 1);
                        setTitle(db2, svf, strtDTStr, pageNo, semeStr);
                        prtCol = 1;
                        fstPrtFlg = false;
                        schCnt = 1;
                        for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                            final String schregNo = (String)its.next();
                            final SchregInfo stu = (SchregInfo)schregMap.get(schregNo);
                            //NO
                            svf.VrsOutn("NO", schCnt, stu._attendNo);
                            //NAME
                            svf.VrsOutn("NAME1", schCnt, stu._name);
                            schCnt++;
                        }
                    }
                    //月文字列(データが無いなら出力不要
                    svf.VrsOut("MONTH" + prtCol, Integer.parseInt(mthStr) + "月");
                    if (_param._lessonDaysMap.containsKey(mthStr)) {
                        final Map ldatMap = (Map)_param._lessonDaysMap.get(mthStr);
                        ttlLesson += Integer.parseInt((String)ldatMap.get("LESSON"));
                        svf.VrsOut("PRESENT" + prtCol, (String)ldatMap.get("LESSON"));
                    }
                    //生徒毎に出力
                    schCnt = 1;
                    for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                        final String schregNo = (String)its.next();
                        final Map totalctrlMap;
                        if (!ttlSchCtrlMap.containsKey(schregNo)) {
                            totalctrlMap = new HashMap();
                            ttlSchCtrlMap.put(schregNo, totalctrlMap);
                        } else {
                            totalctrlMap = (Map)ttlSchCtrlMap.get(schregNo);
                        }
                        ////キーでデータを取得
                        final Map attDDatSubMap = srchDetailMap(attDDatMap, schregNo, mwcKey);
                        //////ATTEND*_1～16
                        int strtCnt = 0;
                        String beforeVal = "";
                        String backupVal = "";
                        for (int k1Cnt = 0; k1Cnt < attendKeys1.size(); k1Cnt++) {
                            final String retVal;
                            if ("C2".equals(((List)attendKeys1.get(k1Cnt)).get(0))) {
                                if ("".equals(beforeVal) && "".equals(backupVal)) {
                                    retVal = "";
                                } else {
                                    backupVal = "".equals(backupVal) ? "0" : backupVal;
                                    beforeVal = "".equals(beforeVal) ? "0" : beforeVal;
                                    retVal = String.valueOf(Integer.parseInt(backupVal) - Integer.parseInt(beforeVal));
                                }
                            } else {
                                retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys1.get(k1Cnt), beforeVal);
                            }
                            mapAdd(totalctrlMap, strtCnt + k1Cnt + 1, retVal);
                            svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k1Cnt + 1), schCnt, retVal);
                            beforeVal = retVal;
                            if ("C1".equals(((List)attendKeys1.get(k1Cnt)).get(0))) {
                                backupVal = "".equals(retVal) ? "" : retVal;
                            }
                        }
                        strtCnt += attendKeys1.size();
                        for (int k2Cnt = 0;k2Cnt < attendKeys2.size();k2Cnt++) {
                            final String retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys2.get(k2Cnt), "");
                            mapAdd(totalctrlMap, strtCnt + k2Cnt + 1, retVal);
                            svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k2Cnt + 1), schCnt, retVal);
                        }
                        strtCnt += attendKeys2.size();
                        for (int k3Cnt = 0;k3Cnt < attendKeys3.size();k3Cnt++) {
                            final String retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys3.get(k3Cnt), "");
                            mapAdd(totalctrlMap, strtCnt + k3Cnt + 1, retVal);
                            svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k3Cnt + 1), schCnt, retVal);
                        }
                        strtCnt += attendKeys3.size();
                        for (int k4Cnt = 0;k4Cnt < attendKeys4.size();k4Cnt++) {
                            final String retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys4.get(k4Cnt), "");
                            mapAdd(totalctrlMap, strtCnt + k4Cnt + 1, retVal);
                            svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k4Cnt + 1), schCnt, retVal);
                        }
                        schCnt++;
                    }
                    fstPrtFlg = true;

                    if (fstPrtFlg) {
                        _hasData = true;
                    }
                    prtCol++;
                }
                if (prtCol > maxCol) {  //端っこまで出力していたら改ページ
                    svf.VrEndPage();
                    svf.VrSetForm(form, 1);
                    setTitle(db2, svf, strtDTStr, pageNo, semeStr);
                    prtCol = 1;
                    fstPrtFlg = false;
                } else {
                    prtCol = maxCol;
                }
                svf.VrsOut("MONTH" + prtCol, "学期集計");
                svf.VrsOut("PRESENT" + prtCol, String.valueOf(ttlLesson));
                //生徒毎に出力
                schCnt = 1;
                for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                    final String schregNo = (String)its.next();
                    final SchregInfo stu = (SchregInfo)schregMap.get(schregNo);
                    if (!fstPrtFlg) {
                        //NO
                        svf.VrsOutn("NO", schCnt, stu._attendNo);
                        //NAME  40
                        svf.VrsOutn("NAME1", schCnt, stu._name);
                        schCnt++;
                    }
                    if (ttlSchCtrlMap.containsKey(schregNo)) {
                        final Map totalctrlMap = (Map)ttlSchCtrlMap.get(schregNo);
                        for (Iterator itp = totalctrlMap.keySet().iterator();itp.hasNext();) {
                            final String idx = (String)itp.next();
                            final Integer ttlVal = (Integer)totalctrlMap.get(idx);
                            svf.VrsOutn("ATTEND" + prtCol + "_" + idx, schCnt, String.valueOf(ttlVal));
                        }
                    }
                    schCnt++;
                }
                svf.VrEndPage();
                pageNo++;
            }
        }
    }

    private void mapAdd(final Map totalctrlMap, final int idx, final String valStr) {
        if (!totalctrlMap.containsKey(String.valueOf(idx))) {
            if (!"".equals(StringUtils.defaultString(valStr, ""))) {
                totalctrlMap.put(String.valueOf(idx), new Integer(Integer.parseInt(valStr)));
            }
        } else {
            if (!"".equals(StringUtils.defaultString(valStr, ""))) {
                final Integer locVal = (Integer)totalctrlMap.get(String.valueOf(idx));
                totalctrlMap.put(String.valueOf(idx), new Integer(locVal + Integer.parseInt(valStr)));
            }
        }
    }
    private void printSub3(final DB2UDB db2, final Vrw32alp svf, final String strtDTStr, final List schregPageList, final Map attendFullMap, final Map topKeyMap) {
        final String form = "KNJC110D.frm";
        svf.VrSetForm(form, 1);
        final int maxCol = 6;  //最大列

        Map attDDatMap = (Map)attendFullMap.get(GET_ATT_DAY);

        final List attendKeys1 = new ArrayList();  //1枠目
        attendKeys1.add(Arrays.asList(new String[] {"+", DI_DAY_MOURNING, DI_DAY_SUSPEND}));
        attendKeys1.add(Arrays.asList(new String[] {"C1", DI_DAY_LESSONCNT}));  //ここでは(取得値-前回値)の計算->backup
        attendKeys1.add(Arrays.asList(new String[] {"+", DI_DAY_ABSENT_SICK, DI_DAY_ABSENT_NOTIFY, DI_DAY_ABSENT_NONOTIFY}));
        attendKeys1.add(Arrays.asList(new String[] {"C2", DI_DAY_LESSONCNT}));  //ここでは(backup-前回値)計算
        final List attendKeys2 = new ArrayList();  //2枠目
        attendKeys2.add(Arrays.asList(new String[] {"", DI_DAY_LATE}));
        attendKeys2.add(Arrays.asList(new String[] {"+", DI_DAY_LATE_KOUNIN1, DI_DAY_LATE_KOUNIN2}));
        final List attendKeys3 = new ArrayList();  //3枠目
        attendKeys3.add(Arrays.asList(new String[] {"", DI_DAY_EARLY}));
        attendKeys3.add(Arrays.asList(new String[] {"+", DI_DAY_EARLY_KOUNIN1, DI_DAY_EARLY_KOUNIN2}));
        final List attendKeys4 = new ArrayList();  //4枠目
        attendKeys4.add(Arrays.asList(new String[] {"", DI_DAY_ABSENT_KEKKA}));
        attendKeys4.add(Arrays.asList(new String[] {"+", DI_DAY_ABSENT_KEKKA_KOUNIN1, DI_DAY_ABSENT_KEKKA_KOUNIN2}));

        int pageNo = 1;
        for (Iterator itl = schregPageList.iterator();itl.hasNext();) {  //生徒数でページ切替
            final Map schregMap = (Map)itl.next();
            boolean fstPrtFlg = false;
            int ttlLesson = 0;
            int prtCol = 1;
            final Map ttlSchCtrlMap = new HashMap();
            for (Iterator ite = _param._semesterList.iterator();ite.hasNext();) {  //ページ管理しているキー
                final String semeStr = (String)ite.next();
                //データが存在するなら出力。無ければ次学期にskip
                if (!topKeyMap.containsKey(semeStr)) {
                    continue;
                }
                //タイトル出力
                setTitle(db2, svf, strtDTStr, pageNo, semeStr);
                int schCnt = 1;
                for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                    final String schregNo = (String)its.next();
                    final SchregInfo stu = (SchregInfo)schregMap.get(schregNo);
                    if (!fstPrtFlg) {
                        //NO
                        svf.VrsOutn("NO", schCnt, stu._attendNo);
                        //NAME
                        svf.VrsOutn("NAME1", schCnt, stu._name);
                        schCnt++;
                    }
                }

                //学期文字列(データが無いなら出力不要
                if (_param._semesterMap.containsKey(semeStr)) {
                    final Map semeObj = (Map)_param._semesterMap.get(semeStr);
                    svf.VrsOut("MONTH" + prtCol, KnjDbUtils.getString(semeObj, "SEMESTERNAME"));
                } else {
                    svf.VrsOut("MONTH" + prtCol, semeStr + "学期");
                }
                if (_param._lessonDaysMap.containsKey(semeStr)) {
                    final Map ldatMap = (Map)_param._lessonDaysMap.get(semeStr);
                    ttlLesson += Integer.parseInt((String)ldatMap.get("LESSON"));
                    svf.VrsOut("PRESENT" + prtCol, (String)ldatMap.get("LESSON"));
                }

                //生徒毎に出力
                schCnt = 1;
                for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                    final String schregNo = (String)its.next();
                    final Map totalctrlMap;
                    if (!ttlSchCtrlMap.containsKey(schregNo)) {
                        totalctrlMap = new HashMap();
                        ttlSchCtrlMap.put(schregNo, totalctrlMap);
                    } else {
                        totalctrlMap = (Map)ttlSchCtrlMap.get(schregNo);
                    }
                    ////キーでデータを取得
                    final Map attDDatSubMap = srchDetailMap(attDDatMap, schregNo, semeStr);
                    //////ATTEND*_1～16
                    int strtCnt = 0;
                    String beforeVal = "";
                    String backupVal = "";
                    for (int k1Cnt = 0;k1Cnt < attendKeys1.size();k1Cnt++) {
                        final String retVal;
                        if ("C2".equals(((List)attendKeys1.get(k1Cnt)).get(0))) {
                            if ("".equals(beforeVal) && "".equals(backupVal)) {
                                retVal = "";
                            } else {
                                backupVal = "".equals(backupVal) ? "0" : backupVal;
                                beforeVal = "".equals(beforeVal) ? "0" : beforeVal;
                                retVal = String.valueOf(Integer.parseInt(backupVal) - Integer.parseInt(beforeVal));
                            }
                        } else {
                            retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys1.get(k1Cnt), beforeVal);
                        }
                        mapAdd(totalctrlMap, strtCnt + k1Cnt + 1, retVal);
                        svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k1Cnt + 1), schCnt, retVal);
                        beforeVal = retVal;
                        if ("C1".equals(((List)attendKeys1.get(k1Cnt)).get(0))) {
                            backupVal = "".equals(retVal) ? "" : retVal;
                        }
                    }
                    strtCnt += attendKeys1.size();
                    for (int k2Cnt = 0;k2Cnt < attendKeys2.size();k2Cnt++) {
                        final String retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys2.get(k2Cnt), "");
                        mapAdd(totalctrlMap, strtCnt + k2Cnt + 1, retVal);
                        svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k2Cnt + 1), schCnt, retVal);
                    }
                    strtCnt += attendKeys2.size();
                    for (int k3Cnt = 0;k3Cnt < attendKeys3.size();k3Cnt++) {
                        final String retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys3.get(k3Cnt), "");
                        mapAdd(totalctrlMap, strtCnt + k3Cnt + 1, retVal);
                        svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k3Cnt + 1), schCnt, retVal);
                    }
                    strtCnt += attendKeys3.size();
                    for (int k4Cnt = 0;k4Cnt < attendKeys4.size();k4Cnt++) {
                        final String retVal = printWithSrchDiCdLst(attDDatSubMap, (List)attendKeys4.get(k4Cnt), "");
                        mapAdd(totalctrlMap, strtCnt + k4Cnt + 1, retVal);
                        svf.VrsOutn("ATTEND" + prtCol + "_" + (strtCnt + k4Cnt + 1), schCnt, retVal);
                    }
                    schCnt++;
                }
                fstPrtFlg = true;

                prtCol++;
            }
            prtCol = maxCol; //フォームの端っこを指定
            svf.VrsOut("MONTH" + prtCol, "年間集計");
            svf.VrsOut("PRESENT" + prtCol, String.valueOf(ttlLesson));
            //生徒毎に出力
            int schCnt = 1;
            for (Iterator its = schregMap.keySet().iterator();its.hasNext();) {
                final String schregNo = (String)its.next();
                if (ttlSchCtrlMap.containsKey(schregNo)) {
                    final Map totalctrlMap = (Map)ttlSchCtrlMap.get(schregNo);
                    for (Iterator itp = totalctrlMap.keySet().iterator();itp.hasNext();) {
                        final String idx = (String)itp.next();
                        final Integer ttlVal = (Integer)totalctrlMap.get(idx);
                        svf.VrsOutn("ATTEND" + prtCol + "_" + idx, schCnt, String.valueOf(ttlVal));
                    }
                }
                schCnt++;
            }

            if (fstPrtFlg && schregMap.size() > 0) {
                _hasData = true;
                svf.VrEndPage();
                pageNo++;
            }
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String strtDTStr, final int pageNo, final String mthStr) {
        final String ttlPatStr;
        final String ymStr;
        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            ttlPatStr = "月別";
            ymStr = String.format("%s年度", _param._year);
        } else {
            ttlPatStr = "学期別";
            ymStr = String.format("%s年度", _param._year);
        }
        //TITLE
        svf.VrsOut("TITLE", "□ 出欠状況集計表 (" + ttlPatStr + ") □");
        //HR_NAME
        svf.VrsOut("HR_NAME", ymStr + " " + StringUtils.defaultString(_param._schKindName) + " " + StringUtils.defaultString(_param._majorName) + " " + StringUtils.defaultString(_param._hrClsName) + " " + "(担任:" + StringUtils.defaultString(_param._staffName, "　　　　　") + ")");
        //PAGE
        svf.VrsOut("PAGE", pageNo + "ページ");
        //DATE
        svf.VrsOut("DATE", strtDTStr);
    }

    private Map getSchregInfo(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getSchregInfoSql();
        log.info("getSchregInfo sql:" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final String courseCodeAbbv1 = rs.getString("COURSECODEABBV1");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendNo = rs.getString("ATTENDNO");
                final String schregNo = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final SchregInfo addWk = new SchregInfo(year, semester, courseCd, majorCd, courseCode, courseCodeAbbv1, grade, hr_Class, attendNo, schregNo, name);
                retMap.put(schregNo , addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private String getSchregInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.COURSECD, ");
        stb.append("   T1.MAJORCD, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T4.COURSECODEABBV1, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.NAME ");
        stb.append(" from ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN COURSECODE_MST T4 ");
        stb.append("     ON T4.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + _param._grHrStr + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.ATTENDNO ");
        return stb.toString();
    }

    private class SchregInfo {
        final String _year;
        final String _semester;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _courseCodeAbbv1;
        final String _grade;
        final String _hr_Class;
        final String _attendNo;
        final String _schregNo;
        final String _name;
        public SchregInfo (final String year, final String semester, final String courseCd, final String majorCd, final String courseCode, final String courseCodeAbbv1, final String grade, final String hr_Class, final String attendNo, final String schregNo, final String name)
        {
            _year = year;
            _semester = semester;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _courseCodeAbbv1 = courseCodeAbbv1;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendNo = attendNo;
            _schregNo = schregNo;
            _name = name;
        }
    }


    private Map getAttendInfo(final DB2UDB db2, final Map topKeyMap) {
        final Map retMap = new LinkedMap();
        retMap.put(GET_ATT_DAY, getAttendInfoSub(db2, topKeyMap, GET_ATT_DAY, ""));
        return retMap;
    }

    private Map getAttendInfoSub(final DB2UDB db2, final Map topKeyMap, final String getDataType, final String getSpSubclsCd) {
        final Map<String, Map<String, Map<String, AttendInfo>>> retMap = new LinkedMap();
        Map subMap = null;
        Map detailMap = null;
        final String sql = getAttendInfoSql(getDataType, getSpSubclsCd);
        log.info("getAttendInfoSub sql:" + sql);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String semester = rs.getString("SEMESTER");
                final String monthVal = rs.getString("MONTHVAL");
                final String di_Cd = rs.getString("DI_CD");
                final String cnt = rs.getString("CNT");
                final AttendInfo addWk = new AttendInfo(schregNo, semester, monthVal, di_Cd, cnt);
                if (!retMap.containsKey(schregNo)) {
                    subMap = new LinkedMap();
                    retMap.put(schregNo, subMap);
                } else {
                    subMap = (Map)retMap.get(schregNo);
                }
                String subKey = "";  //出力パターンによって、出力キー+一つ上位のキーでキー設定する。それが1ページ単位になる。
                if (OUTPUT_MONTH.equals(_param._outputDiv)) {
                    subKey = semester + "-" + String.format("%02d", Integer.parseInt(monthVal));
                } else  {
                    subKey = semester;
                }
                ////subKey一覧を保持する
                if (!topKeyMap.containsKey(subKey)) {
                    topKeyMap.put(subKey, "");
                }
                if (!subMap.containsKey(subKey)) {
                    detailMap = new LinkedMap();
                    subMap.put(subKey, detailMap);
                } else {
                    detailMap = (Map)subMap.get(subKey);
                }
                detailMap.put(di_Cd , addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        // 授業日数のみの月・学期を除去
        final Set<String> allSubkeys = new TreeSet<String>();
        for (final String schregno : retMap.keySet()) {
            final Map<String, Map<String, AttendInfo>> subMapWk = retMap.get(schregno);
            allSubkeys.addAll(subMapWk.keySet());
        }
        final List<String> removeSubkeys = new ArrayList<String>();
        for (final String subKey : allSubkeys) {
            boolean hasShussekiNissuIgai = false;
            for (final String schregno : retMap.keySet()) {
                final Map<String, Map<String, AttendInfo>> subMapWk = retMap.get(schregno);
                final Map<String, AttendInfo> detailMapWk = subMapWk.get(subKey);
                if (null == detailMapWk) {
                    continue;
                }
                if (detailMapWk.keySet().size() == 1 && DI_DAY_LESSONCNT.equals(detailMapWk.keySet().iterator().next())) {
                    continue;
                }
                hasShussekiNissuIgai = true;
            }
            if (hasShussekiNissuIgai == false) {
                removeSubkeys.add(subKey);
            }
        }
        for (final String subKey : removeSubkeys) {
            for (final String schregno : retMap.keySet()) {
                final Map<String, Map<String, AttendInfo>> subMapWk = retMap.get(schregno);
                subMapWk.remove(subKey);
            }
            topKeyMap.remove(subKey);
        }
        // 授業日数のみの生徒を除去
        final List<String> removeSchregnos = new ArrayList<String>();
        for (final String schregno : retMap.keySet()) {
            boolean hasShussekiNissuIgai = false;
            final Map<String, Map<String, AttendInfo>> subMapWk = retMap.get(schregno);
            for (final String subKey : subMapWk.keySet()) {
                final Map<String, AttendInfo> detailMapWk = subMapWk.get(subKey);
                if (detailMapWk.keySet().size() == 1 && DI_DAY_LESSONCNT.equals(detailMapWk.keySet().iterator().next())) {
                    continue;
                }
                hasShussekiNissuIgai = true;
            }
            if (hasShussekiNissuIgai == false) {
                removeSchregnos.add(schregno);
            }
        }
        for (final String schregno : removeSchregnos) {
            retMap.remove(schregno);
        }
        return retMap;
    }


    private String getAttendInfoSql(final String getDataType, final String getSpSubclsCd) {
        final StringBuffer stb = new StringBuffer();
        //出欠データを学期別にする。
        stb.append(" WITH CONN_SEME AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.*, ");
        stb.append("   T2.SEMESTER, ");
        stb.append("   DATE_PART('YEAR',ATTENDDATE) || '-' || (CASE WHEN DATE_PART('MONTH',ATTENDDATE) < 10 THEN '0' ELSE '' END) || DATE_PART('MONTH',ATTENDDATE) || '-01' AS TOPDATE ");
        stb.append(" FROM ");
        stb.append("   attend_day_dat T1 ");
        stb.append("   INNER JOIN SEMESTER_MST T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("    AND T2.SEMESTER <> '" + SEMEALL + "' ");
        //各DI_CD別、画面選択に応じた集計単位別に集計する。
        stb.append(" ), CNT_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("   SCHREGNO, ");
        stb.append("   SEMESTER, ");
        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            stb.append("   DATE_PART('MONTH',ATTENDDATE) AS MONTHVAL, ");
        } else {
            stb.append("   CAST(NULL AS VARCHAR(2)) AS MONTHVAL, ");
        }
        stb.append("   DI_CD, ");
        stb.append("   COUNT(*) AS CNT ");
        stb.append(" from ");
        stb.append("   CONN_SEME ");
        stb.append(" where ");
        stb.append("   ATTENDDATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO, ");
        stb.append("   SEMESTER, ");
        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            stb.append("   DATE_PART('MONTH',ATTENDDATE), ");
        }
        //stb.append("   VALUE(DATE_PART('WEEK',ATTENDDATE), 0) - VALUE(DATE_PART('WEEK', TOPDATE),0) + 1, ");
        stb.append("   DI_CD ");
        //月単位の授業日数を取得
        stb.append(" ), LESSON_BASE_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   ATTEND_LESSON_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        //学科情報を取得。
        //関西学院高校は1学科のみ。念のため、クラス間の差異を考慮してクラスで取得して先頭の学科で取得。
        stb.append(" ), SEL_COURSEMAJOR AS ( ");
        stb.append("  SELECT ");
        stb.append("    SEMESTER, ");
        stb.append("    MAX(COURSECD || '-' || MAJORCD) AS COURSEMAJOR ");
        stb.append("  FROM ");
        stb.append("    SCHREG_REGD_DAT ");
        stb.append("  WHERE ");
        stb.append("    YEAR = '" + _param._year + "' ");
        stb.append("    AND GRADE || HR_CLASS = '" + _param._grHrStr + "' ");
        stb.append(" GROUP BY ");
        stb.append("   SEMESTER ");
        //授業日数を集計単位で集計する
        stb.append(" ), GETLESSON_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            stb.append("   T1.MONTH, ");
        } else {
            stb.append("   CAST(NULL AS VARCHAR(2)) AS MONTH, ");
        }
        stb.append("   SUM(T1.LESSON) AS LESSON ");
        stb.append(" FROM ");
        stb.append("   LESSON_BASE_DAT T1 ");
        stb.append("   LEFT JOIN SEL_COURSEMAJOR TS ");
        stb.append("     ON TS.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("   COURSECD = (CASE WHEN EXISTS ( ");
        stb.append("                           SELECT DISTINCT ");
        stb.append("                             TW1.COURSECD ");
        stb.append("                           FROM ");
        stb.append("                             LESSON_BASE_DAT TW1 ");
        stb.append("                           WHERE ");
        stb.append("                             TW1.YEAR = T1.YEAR ");
        stb.append("                             AND TW1.MONTH = T1.MONTH ");
        stb.append("                             AND TW1.SEMESTER = T1.SEMESTER ");
        stb.append("                             AND TW1.GRADE = T1.GRADE ");
        stb.append("                             AND TW1.COURSECD = SUBSTR(TS.COURSEMAJOR, 1,1) ");
        stb.append("                         ) THEN SUBSTR(TS.COURSEMAJOR, 1,1) ");
        stb.append("                    ELSE '0' END) ");
        stb.append("   AND ");
        stb.append("   MAJORCD = (CASE WHEN EXISTS ( ");
        stb.append("                          SELECT DISTINCT ");
        stb.append("                            TW2.MAJORCD ");
        stb.append("                          FROM ");
        stb.append("                            LESSON_BASE_DAT TW2 ");
        stb.append("                          WHERE ");
        stb.append("                            TW2.YEAR = T1.YEAR ");
        stb.append("                            AND TW2.MONTH = T1.MONTH ");
        stb.append("                            AND TW2.SEMESTER = T1.SEMESTER ");
        stb.append("                            AND TW2.GRADE = T1.GRADE ");
        stb.append("                            AND TW2.COURSECD = SUBSTR(TS.COURSEMAJOR, 1,1) ");
        stb.append("                            AND TW2.MAJORCD = SUBSTR(TS.COURSEMAJOR, 3,3) ");
        stb.append("                        ) THEN SUBSTR(TS.COURSEMAJOR, 3,3) ");
        stb.append("                   ELSE '000' END) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER ");
        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            stb.append("    , T1.MONTH ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   CNT_BASE T1 ");
        if (OUTPUT_MONTH.equals(_param._outputDiv) || OUTPUT_SEMES.equals(_param._outputDiv)) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.SEMESTER, ");
            if (OUTPUT_MONTH.equals(_param._outputDiv)) {
                stb.append("   T2.MONTH AS MONTH, ");
            } else {
                stb.append("   CAST(NULL AS VARCHAR(2)) AS MONTH, ");
            }
            stb.append("   'ZZ' AS DI_CD, ");
            stb.append("   T2.LESSON AS CNT ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN GETLESSON_DAT T2 ");
            stb.append("     ON T2.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' ");
            stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + _param._grHrStr + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        if (OUTPUT_MONTH.equals(_param._outputDiv)) {
            stb.append("     MONTHVAL, ");
        }
        stb.append("     DI_CD ");
        return stb.toString();
    }

    private class AttendInfo {
        final String _schregNo;
        final String _semester;
        final String _monthVal;
        final String _di_Cd;
        final String _cnt;
        public AttendInfo (final String schregNo, final String semester, final String monthVal, final String di_Cd, final String cnt)
        {
            _schregNo = schregNo;
            _semester = semester;
            _monthVal = monthVal;
            _di_Cd = di_Cd;
            _cnt = cnt;
        }
    }

    /** パラメータ取得 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70480 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _grHrStr;
        private final String _grade;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        private final String _outputDiv;
        private final String _sDate;
        private final String _eDate;

        private final String _gradeName;
        private final String _hrClsName;
        private final String _staffName;
        private final String _majorName;
        private final String _schKindName;

        final List _semesterList;

        private static final String GRNAMEKEY = "GRADE_NAME1";
        private static final String HRNAMEKEY = "HR_NAMEABBV";
        private static final String SFNAMEKEY = "STAFFNAME";

        private final List _wCntList = Arrays.asList(new String[] {"1", "2", "3", "4", "5", "6"});
        private final List _monthList = Arrays.asList(new String[] {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"});
        private final Map _lessonDaysMap;
        private final Map _semesterMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _grHrStr = request.getParameter("GRADE_HRCLASS");
            _grade = _grHrStr.length() > 2 ? _grHrStr.substring(0, 2) : "";
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = convSlashHaifun(request.getParameter("CTRL_DATE"));
            _outputDiv = request.getParameter("OUTPUT_DIV");

            _semesterMap = getSemesterMstMap(db2);

            if (OUTPUT_MONTH.equals(_outputDiv)) {
                _sDate = convSlashHaifun(request.getParameter("SMONTH"));
                _eDate = convSlashHaifun(request.getParameter("EMONTH"));
            } else {
                if (_semesterMap.containsKey("1")) {
                    final Map s_Semes = (Map)_semesterMap.get("1");
                    _sDate = KnjDbUtils.getString(s_Semes, "SDATE");
                } else {
                    _sDate = "";
                }
                if (_semesterMap.containsKey(_ctrlSemester)) {
                    final Map s_Semes = (Map)_semesterMap.get(_ctrlSemester);
                    _eDate = KnjDbUtils.getString(s_Semes, "EDATE");
                } else {
                    _eDate = "";
                }
            }

            final Map retMap = getGrHrInfo(db2);
            if (retMap.containsKey(GRNAMEKEY)) {
                _gradeName = (String)retMap.get(GRNAMEKEY);
            } else {
                _gradeName = "";
            }
            if (retMap.containsKey(HRNAMEKEY)) {
                _hrClsName = (String)retMap.get(HRNAMEKEY);
            } else {
                _hrClsName = "";
            }
            if (retMap.containsKey(SFNAMEKEY)) {
                _staffName = (String)retMap.get(SFNAMEKEY);
            } else {
                _staffName = "";
            }
            _majorName = getMajorName(db2);
            _schKindName = getSchKindName(db2);
            _semesterList = getSemesterList(db2);
            _lessonDaysMap = getCntLessonDaysMap(db2);

        }
        public String getYStr(final String mthStr) {
            return String.valueOf(Integer.parseInt(_year) + (Integer.parseInt(mthStr) < 4 ? 1 : 0));
        }
        public int getMaxWeekCntofMonth(final String yStr, final String mthStr) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, Integer.parseInt(yStr));
            cal.set(Calendar.MONTH, Integer.parseInt(mthStr) - 1);
            int lastDayOfMonth = cal.getActualMaximum(Calendar.DATE);
            cal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
            return cal.get(Calendar.WEEK_OF_MONTH);
        }
        private Map getSemesterMstMap(final DB2UDB db2) {
            Map retMap = new HashMap();
            for (final Map row : KnjDbUtils.query(db2, "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '" + SEMEALL + "' ORDER BY SEMESTER ")) {
                retMap.put(KnjDbUtils.getString(row, "SEMESTER"), row);
            }
            return retMap;
        }

        private String convSlashHaifun(final String baseStr) {
            return !"".equals(StringUtils.defaultString(baseStr, "")) ? baseStr.replace('/', '-') : "";
        }
        private List getSemesterList(final DB2UDB db2) {
            List retList = new ArrayList();
            for (final Map row : KnjDbUtils.query(db2, "SELECT SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '" + SEMEALL + "' ORDER BY SEMESTER ")) {
                retList.add(KnjDbUtils.getString(row, "SEMESTER"));
            }
            return retList;
        }

        private String getSchKindName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ) "));
        }
        private String getMajorName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T2.MAJORNAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN MAJOR_MST T2 ");
            stb.append("     ON T2.COURSECD = T1.COURSECD ");
            stb.append("    AND T2.MAJORCD = T1.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND GRADE || HR_CLASS = '" + _grHrStr + "' ");
            stb.append(" FETCH FIRST ROW ONLY ");
            //関西学院高校は1学科のみ。念のため、クラス間の差異を考慮してクラスで取得して先頭の学科で取得。
            final String retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            return retStr;
        }
        private Map getGrHrInfo(final DB2UDB db2) {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T5.GRADE_NAME1, ");
            stb.append("   T3.HR_NAMEABBV, ");
            stb.append("   T6.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_HDAT T3 ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T5 ");
            stb.append("     ON T5.YEAR = T3.YEAR ");
            stb.append("    AND T5.GRADE = T3.GRADE ");
            stb.append("   LEFT JOIN STAFF_MST T6 ");
            stb.append("     ON T6.STAFFCD = T3.TR_CD1 ");
            stb.append(" WHERE ");
            stb.append("   T3.YEAR = '" + _year + "' ");
            stb.append("   AND T3.SEMESTER = '" + _ctrlSemester + "' ");
            stb.append("   AND T3.GRADE || T3.HR_CLASS = '" + _grHrStr + "' ");
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                retMap.put(GRNAMEKEY, KnjDbUtils.getString(row, GRNAMEKEY));
                retMap.put(HRNAMEKEY, KnjDbUtils.getString(row, HRNAMEKEY));
                retMap.put(SFNAMEKEY, KnjDbUtils.getString(row, SFNAMEKEY));
            }
            return retMap;
        }
        private Map getCntLessonDaysMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH LESSON_BASE_DAT AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   ATTEND_LESSON_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.GRADE = '" + _grade + "' ");
            stb.append(" ), SEL_COURSEMAJOR AS ( ");
            stb.append("  SELECT ");
            stb.append("    SEMESTER, ");
            stb.append("    MAX(COURSECD || '-' || MAJORCD) AS COURSEMAJOR ");
            stb.append("  FROM ");
            stb.append("    SCHREG_REGD_DAT ");
            stb.append("  WHERE ");
            stb.append("    YEAR = '" + _year + "' ");
            stb.append("    AND GRADE || HR_CLASS = '" + _grHrStr + "' ");
            stb.append(" GROUP BY ");
            stb.append("   SEMESTER ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.SEMESTER, ");
            if (OUTPUT_MONTH.equals(_outputDiv)) {
                stb.append("   T1.MONTH, ");
            } else {
                stb.append("   CAST(NULL AS VARCHAR(2)) AS MONTH, ");
            }
            stb.append("   SUM(T1.LESSON) AS LESSON ");
            stb.append(" FROM ");
            stb.append("   LESSON_BASE_DAT T1 ");
            stb.append("   LEFT JOIN SEL_COURSEMAJOR TS ");
            stb.append("     ON TS.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("   COURSECD = (CASE WHEN EXISTS ( ");
            stb.append("                           SELECT DISTINCT ");
            stb.append("                             TW1.COURSECD ");
            stb.append("                           FROM ");
            stb.append("                             LESSON_BASE_DAT TW1 ");
            stb.append("                           WHERE ");
            stb.append("                             TW1.YEAR = T1.YEAR ");
            stb.append("                             AND TW1.MONTH = T1.MONTH ");
            stb.append("                             AND TW1.SEMESTER = T1.SEMESTER ");
            stb.append("                             AND TW1.GRADE = T1.GRADE ");
            stb.append("                             AND TW1.COURSECD = SUBSTR(TS.COURSEMAJOR, 1,1) ");
            stb.append("                         ) THEN SUBSTR(TS.COURSEMAJOR, 1,1) ");
            stb.append("                    ELSE '0' END) ");
            stb.append("   AND ");
            stb.append("   MAJORCD = (CASE WHEN EXISTS ( ");
            stb.append("                          SELECT DISTINCT ");
            stb.append("                            TW2.MAJORCD ");
            stb.append("                          FROM ");
            stb.append("                            LESSON_BASE_DAT TW2 ");
            stb.append("                          WHERE ");
            stb.append("                            TW2.YEAR = T1.YEAR ");
            stb.append("                            AND TW2.MONTH = T1.MONTH ");
            stb.append("                            AND TW2.SEMESTER = T1.SEMESTER ");
            stb.append("                            AND TW2.GRADE = T1.GRADE ");
            stb.append("                            AND TW2.COURSECD = SUBSTR(TS.COURSEMAJOR, 1,1) ");
            stb.append("                            AND TW2.MAJORCD = SUBSTR(TS.COURSEMAJOR, 3,3) ");
            stb.append("                        ) THEN SUBSTR(TS.COURSEMAJOR, 3,3) ");
            stb.append("                   ELSE '000' END) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER ");
            if (OUTPUT_MONTH.equals(_outputDiv)) {
                stb.append("    , T1.MONTH ");
            }
            log.debug("sql:" + stb.toString());
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String kStr;
                if (OUTPUT_SEMES.equals(_outputDiv)) {
                    kStr = KnjDbUtils.getString(row, "SEMESTER");
                } else {
                    kStr = String.format("%02d", Integer.parseInt(KnjDbUtils.getString(row, "MONTH")));
                }
                retMap.put(kStr, row);
            }
            return retMap;
        }
    }
}

// eof

