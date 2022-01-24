/*
 *
 * 作成日: 2020/12/03
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 SATT Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJB043 {

    private static final Log log = LogFactory.getLog(KNJB043.class);

    private static final String COLOR_GRAY = "Paint=(2,70,2)";
    private static final String FROM_TO_MARK = "\uFF5E";

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

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2kenja", DB2UDB.TYPE2);
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
        int pCnt = 0;  //処理に入ってすぐに加算するので0固定。
        int periodMax = 8;
        final SimpleDateFormat wdFmt = new SimpleDateFormat("E(M/d)");
        final int onePageStaffMax = 40;
        final Map staffWkMap = getStaffMap(db2);  //職員情報
        final List printOnePageStaffMapList = divideStaffMap(staffWkMap, onePageStaffMax);  //1ページ40名単位で分割
        //_param._periodMap //x校時情報
        final Map fullScheduleMap = new LinkedMap();     //スケジュール(終日データ)
        final Map detailScheduleMap = new LinkedMap();   //スケジュール(校時データ)
        getScheduleMap(db2, fullScheduleMap, detailScheduleMap);

        final Map fullVacationMap = new LinkedMap();     //休暇(終日データ)
        final Map detailVacationMap = new LinkedMap();   //休暇(校時データ)
        getVacationMap(db2, fullVacationMap, detailVacationMap);

        final Map fullTripMap = new LinkedMap();    //旅行出張(終日データ)
        final Map detailTripMap = new LinkedMap();  //旅行出張(校時データ)
        getTripMap(db2, fullTripMap, detailTripMap);

        for (Iterator itp = printOnePageStaffMapList.iterator();itp.hasNext();) {  //1ページ単位の先生集団
            final Map staffMap = (Map)itp.next();
            for(Iterator itl = _param._useTermDate.iterator();itl.hasNext();) { //3日単位ループ
                final Map halfWeek = (Map)itl.next();
                pCnt++;
                svf.VrSetForm("KNJB043.frm", 4);
                setHeader(db2, svf, pCnt, halfWeek);
                for (Iterator its = staffMap.keySet().iterator();its.hasNext();) {  //職員(縦)
                    final String staffCd = (String)its.next();
                    final StaffData stfD = (StaffData)staffMap.get(staffCd);

                    svf.VrsOut("SECTIONABBV", stfD._sectionname);
                    svf.VrsOut("STAFFNAME_SHOW", stfD._staffname);

                    int hDCnt = 0;
                    int lineIdx = 0;
                    for(Iterator ite = halfWeek.keySet().iterator();ite.hasNext();) {  //日付でループ
                        final String dStr = (String)ite.next();
                        if (hDCnt > 0) {
                            lineIdx = hDCnt * periodMax; //日付が変わったら初期化。但し、フォームの繰り返しフィールドが日が変わっても連続しているので、(1日8時間)*(処理済日数)で初期化
                        }
                        hDCnt++;

                        svf.VrsOut("DATE" + hDCnt, wdFmt.format(sdfParse(dStr)));  //日付

                        final String fstKey = dStr + "_" + staffCd;
                        //終日データ取得
                        List f_ScheduleList = fullScheduleMap.containsKey(fstKey) ? (List)fullScheduleMap.get(fstKey) : new ArrayList();
                        List f_VacationList = fullVacationMap.containsKey(fstKey) ? (List)fullVacationMap.get(fstKey) : new ArrayList();
                        List f_TripList = fullTripMap.containsKey(fstKey) ? (List)fullTripMap.get(fstKey) : new ArrayList();
                        if (f_ScheduleList.size() > 0 || f_VacationList.size() > 0 || f_TripList.size() > 0) {
                            //終日データの場合の処理
                            //休暇、旅行データ(終日)を出力。複数あれば先頭レコードを取得して出力。基本的には1しかないはず。
                            final ScheduleData fSWk = (ScheduleData)getSelData(f_ScheduleList, "SCHE");
                            final ScheduleData fVWk = (ScheduleData)getSelData(f_VacationList, "VACA");
                            final ScheduleData fTWk = (ScheduleData)getSelData(f_TripList, "TRIP");
                            lineIdx++;
                            for (int gCnt = 0;gCnt < periodMax;gCnt++) {
                                if (fTWk != null || fVWk != null) {
                                    //灰色にする。
                                    svf.VrAttributen("CHAIRABBV", lineIdx, COLOR_GRAY);
                                } else if (fSWk != null) {
                                    //fSWk._print_Nameの1文字出力
                                    svf.VrsOutn("CHAIRABBV", lineIdx, StringUtils.substring(fSWk._print_Name, 0, 1));
                                }
                                lineIdx++;
                            }
                        } else {
                            int koujiCnt = 0;
                            for (Iterator itx = _param._periodMap.keySet().iterator();itx.hasNext();) {  //校時(横)
                                final String pCd = (String)itx.next();
                                final PeriodData pWk = (PeriodData)_param._periodMap.get(pCd);
                                if (koujiCnt >= 8) {
                                    continue;
                                } else {
                                    koujiCnt++;
                                    lineIdx++;
                                }
                                final String detKey = fstKey + "_" + pWk._abbv1;
                                //個別時間割の場合の処理
                                //詳細データ取得
                                List d_ScheduleList = detailScheduleMap.containsKey(detKey) ? (List)detailScheduleMap.get(detKey) : new ArrayList();
                                List d_VacationList = detailVacationMap.containsKey(detKey) ? (List)detailVacationMap.get(detKey) : new ArrayList();
                                List d_TripList = detailTripMap.containsKey(detKey) ? (List)detailTripMap.get(detKey) : new ArrayList();
                                boolean prtSchFlg = false;
                                if (d_ScheduleList.size() > 0 || d_VacationList.size() > 0 || d_TripList.size() > 0) {
                                    //休暇、旅行データ(校時)を出力。複数あれば先頭レコードを取得して出力。基本的には1しかないはず。
                                    final ScheduleData dSWk = (ScheduleData)getSelData(d_ScheduleList, "SCHE");
                                    final ScheduleData dVWk = (ScheduleData)getSelData(d_VacationList, "VACA");
                                    final ScheduleData dTWk = (ScheduleData)getSelData(d_TripList, "TRIP");
                                    if (dTWk != null || dVWk != null) {
                                        //灰色にする。
                                        svf.VrAttributen("CHAIRABBV", lineIdx, COLOR_GRAY);
                                    } else if (dSWk != null) {
                                        //dSWk._print_Nameの1文字出力
                                        svf.VrsOutn("CHAIRABBV", lineIdx, StringUtils.substring(dSWk._print_Name, 0, 1));
                                    } else {
                                        prtSchFlg = true;
                                    }
                                } else {
                                    prtSchFlg = true;
                                }
                                if (prtSchFlg) {
                                    //時間割データを出力
                                    final String fkStr = dStr + "_" + pCd;
                                    ChairData putWk1 = stfD.getLittleChairCd(fkStr);  //講座名称
                                    if (putWk1 != null) {
                                        svf.VrsOutn("CHAIRABBV", lineIdx, putWk1._chairabbv);
                                    }
                                    ChairData putWk2 = stfD.getLittleFacCd(fkStr);    //施設名称
                                    if (putWk2 != null) {
                                        svf.VrsOutn("FACILITYABBV", lineIdx, putWk2._facilityabbv);
                                    }
                                }
                            _hasData = true;
                            }
                        }
                    }
                    svf.VrEndRecord();
                }
                svf.VrEndPage();
            }
        }
    }

    private Date sdfParse(final String parseDStr) {
        final SimpleDateFormat wdConb = new SimpleDateFormat("yyyy-MM-dd");
        Date retDObj = new Date();
        try {
            retDObj = wdConb.parse(parseDStr.replace('/', '-'));
        } catch(Exception ex) {
            log.warn(" error in sdfParse. value:" + parseDStr);
        } finally {
        }
        return retDObj;
    }

    private void setHeader(final DB2UDB db2, final Vrw32alp svf, final int pageNo, final Map halfWeek) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._calcd_Start_Date) + "度 教員別時間割一覧表");  //タイトル名
        final String fromCalcStr;
        final String toCalcStr;
        if (halfWeek.size() > 0) {
            Iterator ite = halfWeek.keySet().iterator();
            final String dStr = (String)ite.next();
            final SimpleDateFormat wdFmt2 = new SimpleDateFormat("yyyy/MM/dd");
            fromCalcStr =  wdFmt2.format(getDayOfWeek(sdfParse(dStr), 2));
            toCalcStr = wdFmt2.format(getDayOfWeek(sdfParse(dStr), 7));
        } else {
            fromCalcStr = _param._calcd_Start_Date;
            toCalcStr = _param._calcd_End_Date;
        }
        svf.VrsOut("SUBTITLE", "(" + KNJ_EditDate.h_format_JP(db2, fromCalcStr) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(db2, toCalcStr) + ")");
        svf.VrsOut("PAGE", String.valueOf(pageNo));  //ページNO
    }

    private static Date getDayOfWeek(final Date targetDate, final int dayOfWeek) {
        int firstDayOfWeek = 1;  //日曜start
        Calendar cal = Calendar.getInstance();
        cal.setTime(targetDate);
        cal.setFirstDayOfWeek(firstDayOfWeek);       //週の開始を日曜始まりに設定
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);  //指定日の週の"指定曜日"に設定
        return cal.getTime();
    }

    private List divideStaffMap(final Map staffMap, final int onePageStaffMax) {
        List retList = new ArrayList();
        Map wkMap = null;
        for (Iterator ite = staffMap.keySet().iterator();ite.hasNext(); ) {
            final String staffCd = (String)ite.next();
            StaffData mvObj = (StaffData)staffMap.get(staffCd);
            if (retList.size() == 0 || wkMap.size() >= onePageStaffMax) {
                wkMap = new LinkedMap();
                retList.add(wkMap);
            }
            wkMap.put(staffCd, mvObj);
        }
        return retList;
    }

    private Object getSelData(final List srchList, final String type) {
        Object retObj = null;
        //現時点では、該当日、該当校時のデータは1件だけ、想定のため、いずれも先頭データを返す。
        if ("SCHE".equals(type)) {
            if (srchList.size() > 0) {
                retObj = (Object)srchList.get(0);
            }
        } else if ("VACA".equals(type)) {
            if (srchList.size() > 0) {
                retObj = (Object)srchList.get(0);
            }
        } else if ("TRIP".equals(type)) {
            if (srchList.size() > 0) {
                retObj = (Object)srchList.get(0);
            }
        }
        return retObj;
    }

    private Map getStaffMap(final DB2UDB db2) {
        Map retMap = new LinkedMap();
        final String sql = getStaffMapSql();
        log.debug(" getStaffMap sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String staffcd = rs.getString("STAFFCD");
                final String staffname = rs.getString("STAFFNAME");
                final String sectioncd = rs.getString("SECTIONCD");
                final String sectionname = rs.getString("SECTIONNAME");
                final StaffData sd = new StaffData(staffcd, staffname, sectioncd, sectionname);
                retMap.put(staffcd, sd);
            }
        } catch (Exception ex) {
            log.error("retStringByteValue error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        getChairInfo(db2, retMap);
        return retMap;
    }

    private String getStaffMapSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.STAFFNAME, ");
        stb.append("   T4.FIELD1 AS SECTIONCD, ");
        stb.append("   T3.SECTIONNAME ");
        stb.append(" FROM ");
        stb.append("   STAFF_YDAT T1 ");
        stb.append("   LEFT JOIN STAFF_MST T2 ");
        stb.append("     ON T2.STAFFCD = T1.STAFFCD ");
        stb.append("   LEFT JOIN STAFF_DETAIL_MST T4 ");
        stb.append("     ON T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.STAFFCD = T1.STAFFCD ");
        stb.append("    AND T4.STAFF_SEQ = '002' ");
        stb.append("   LEFT JOIN SECTION_MST T3 ");
        stb.append("     ON T3.SECTIONCD = T4.FIELD1 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T4.FIELD1 IN " + SQLUtils.whereIn(true, _param._category_Selected));
        stb.append(" ORDER BY ");
        stb.append("   T4.FIELD1, ");
        stb.append("   T2.STAFFNAME_KANA ");

        return stb.toString();
    }

    private void getChairInfo(final DB2UDB db2, final Map staffMap) {
        List subList = null;
        final String sql = getChairInfoSql();
        log.debug(" getChairInfo sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String executedate = rs.getString("EXECUTEDATE");
                final String periodcd = rs.getString("PERIODCD");
                final String staffcd = rs.getString("STAFFCD");
                final String chaircd = rs.getString("CHAIRCD");
                final String faccd = rs.getString("FACCD");
                final String chairname = rs.getString("CHAIRNAME");
                final String chairabbv = rs.getString("CHAIRABBV");
                final String facilityname = rs.getString("FACILITYNAME");
                final String facilityabbv = rs.getString("FACILITYABBV");
                final String staffname = rs.getString("STAFFNAME");
                final ChairData addWk = new ChairData(executedate, periodcd, staffcd, chaircd, faccd, chairname, chairabbv, facilityname, facilityabbv, staffname);
                if (staffMap.containsKey(staffcd)) {
                    final StaffData sdWk = (StaffData)staffMap.get(staffcd);
                    final String sndKey = executedate + "_" + periodcd;

                    if (!sdWk._chairMap.containsKey(sndKey)) {
                        subList = new ArrayList();
                        sdWk._chairMap.put(sndKey, subList);
                    } else {
                        subList = (List)sdWk._chairMap.get(sndKey);
                    }
                    subList.add(addWk);
                }
            }
        } catch (Exception ex) {
            log.error("retStringByteValue error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return;
    }

    private String getChairInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_BASEDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.EXECUTEDATE, ");
        stb.append("   T1.PERIODCD, ");
        stb.append("   T1.CHAIRCD, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T2.FACCD, ");
        stb.append("   T3.STAFFCD ");
        stb.append(" FROM ");
        stb.append("   SCH_CHR_DAT T1 ");
        stb.append("   LEFT JOIN SCH_FAC_DAT T2 ");
        stb.append("     ON T2.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("    AND T2.PERIODCD = T1.PERIODCD ");
        stb.append("    AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("   LEFT JOIN SCH_STF_DAT T3 ");
        stb.append("     ON T3.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("    AND T3.PERIODCD = T1.PERIODCD ");
        stb.append("    AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.EXECUTEDATE BETWEEN '" + _param._calcd_Start_Date + "' AND '" + _param._calcd_End_Date + "' ");
        stb.append(" ), CONN_CHAIR AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.EXECUTEDATE, ");
        stb.append("   T1.PERIODCD, ");
        stb.append("   T2.CHAIRCD, ");
        stb.append("   T2.CHAIRNAME, ");
        stb.append("   T2.CHAIRABBV, ");
        stb.append("   CASE WHEN T1.FACCD IS NOT NULL THEN T1.FACCD ELSE T3.FACCD END AS FACCD, ");
        stb.append("   CASE WHEN T1.STAFFCD IS NOT NULL THEN T1.STAFFCD ELSE T5.STAFFCD END AS STAFFCD ");
        stb.append(" FROM ");
        stb.append("   SCH_BASEDAT T1 ");
        stb.append("   LEFT JOIN CHAIR_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("   LEFT JOIN CHAIR_FAC_DAT T3 ");
        stb.append("     ON T3.YEAR = T2.YEAR ");
        stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("   LEFT JOIN CHAIR_STF_DAT T5 ");
        stb.append("     ON T5.YEAR = T2.YEAR ");
        stb.append("    AND T5.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T5.CHAIRCD = T2.CHAIRCD ");
        stb.append("    AND T5.CHARGEDIV = '1' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.EXECUTEDATE, ");
        stb.append("   T1.PERIODCD, ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T1.CHAIRCD, ");
        stb.append("   T1.FACCD, ");
        stb.append("   T1.CHAIRNAME, ");
        stb.append("   T1.CHAIRABBV, ");
        stb.append("   T4.FACILITYNAME, ");
        stb.append("   T4.FACILITYABBV, ");
        stb.append("   T6.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("   CONN_CHAIR T1 ");
        stb.append("   LEFT JOIN V_FACILITY_MST T4 ");
        stb.append("     ON T4.YEAR = T1.YEAR ");
        stb.append("    AND T4.FACCD = T1.FACCD ");
        stb.append("   LEFT JOIN V_STAFF_MST T6 ");
        stb.append("     ON T6.YEAR = T1.YEAR ");
        stb.append("    AND T6.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("   T1.STAFFCD IS NOT NULL ");
        stb.append("   AND T6.SECTIONCD IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   T1.EXECUTEDATE, ");
        stb.append("   T1.PERIODCD, ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T1.CHAIRCD, ");
        stb.append("   T1.FACCD ");
        return stb.toString();

    }

    private void getScheduleMap(final DB2UDB db2, final Map fullMap, final Map detailMap) {
        List detailList = null;
        final String sql = getScheduleMapSql();
        log.debug(" getScheduleMap sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sche_Date = rs.getString("SCHE_DATE");    //対象日(表の横)
                final String abbv1 = rs.getString("ABBV1");            //対象校時(表の横(詳細))
                final String staffcd = rs.getString("STAFFCD");        //対象職員(表の縦)
                final String kubun = rs.getString("KUBUN");            //データ詳細
                final String print_Name = rs.getString("PRINT_NAME");  //枠内出力名称
                ScheduleData addwk = new ScheduleData(sche_Date, staffcd, kubun, abbv1, print_Name);
                if ("3".equals(kubun)) {
                    final String fstKey = sche_Date + "_" + staffcd;  //縦横が決まった状態で読み込む。※ここは終日なので校時は無し。
                    if (!fullMap.containsKey(fstKey)) {
                        detailList = new ArrayList();
                        fullMap.put(fstKey, detailList);
                    } else {
                        detailList = (List)fullMap.get(fstKey);
                    }
                    detailList.add(addwk);
                } else {
                    final String fstKey = sche_Date + "_" + staffcd + "_" + abbv1;  //縦横(詳細含む)が決まった状態で読み込む。
                    if (!detailMap.containsKey(fstKey)) {
                        detailList = new ArrayList();
                        detailMap.put(fstKey, detailList);
                    } else {
                        detailList = (List)detailMap.get(fstKey);
                    }
                    detailList.add(addwk);
                }
            }
        } catch (Exception ex) {
            log.error("retStringByteValue error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getScheduleMapSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERIOD_DAT AS ( ");
        stb.append(" select ");
        stb.append("   NAMECD2, ");
        stb.append("   NAME1, ");
        stb.append("   ABBV1, ");
        stb.append("   ABBV2, ");
        stb.append("   ABBV3 ");
        stb.append(" FROM ");
        stb.append("   V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND NAMECD1 = 'B001' ");
        stb.append("   AND NAMESPARE2 IS NULL");
        stb.append(" ), SAF_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.TIME_TYPE AS KUBUN, ");
        stb.append("   CASE WHEN T2.TIME_TYPE = '1' THEN T3_1.START_TIME ELSE T2.START_TIME END AS START_TIME, ");
        stb.append("   CASE WHEN T2.TIME_TYPE = '1' THEN T3_2.END_TIME ELSE T2.END_TIME END AS END_TIME, ");
        stb.append("   T2.SCHEDULE_NAME AS PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   SAF_SCHEDULE_MEMBER_DAT T1 ");
        stb.append("   LEFT JOIN SAF_SCHEDULE_DAT T2 ");
        stb.append("     ON T2.ID = T1.SCHEDULE_ID ");
        stb.append("    AND T2.SCHOOLID = T1.SCHOOLID ");
        stb.append("    AND T2.DELETE_FLG = '0' ");
        stb.append("   LEFT JOIN SAF_PERIOD_DAT T3_1 ");
        stb.append("     ON T3_1.ID = T2.START_PERIOD ");
        stb.append("    AND T3_1.DELETE_FLG = '0' ");
        stb.append("   LEFT JOIN SAF_PERIOD_DAT T3_2 ");
        stb.append("     ON T3_2.ID = T2.END_PERIOD ");
        stb.append("    AND T3_2.DELETE_FLG = '0' ");
        stb.append(" WHERE ");
        stb.append("   T2.SCHE_DATE BETWEEN '" + _param._calcd_Start_Date + "' AND '" + _param._calcd_End_Date + "' ");
        stb.append("   AND T1.DELETE_FLG = '0' ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.KUBUN, ");
        stb.append("   '' AS ABBV1, ");
        stb.append("   T2.PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   PERIOD_DAT T1,SAF_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("   T2.KUBUN = '3' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.KUBUN, ");
        stb.append("   T1.ABBV1, ");
        stb.append("   T2.PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   PERIOD_DAT T1,SAF_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("   VALUE(T1.ABBV1, '') <> '' AND ");
        stb.append("   T2.KUBUN <> '3' AND ");
        stb.append("   (T1.ABBV2 BETWEEN T2.START_TIME AND T2.END_TIME ");  //包含関係が逆になるのも加味して記載
        stb.append("    OR T1.ABBV3 BETWEEN T2.START_TIME AND T2.END_TIME) ");
        stb.append("   OR ");
        stb.append("   (T2.START_TIME BETWEEN T1.ABBV2 AND T1.ABBV3 ");
        stb.append("    OR T2.END_TIME BETWEEN T1.ABBV2 AND T1.ABBV3) ");
        stb.append(" ORDER BY ");
        stb.append("   SCHE_DATE, ");
        stb.append("   STAFFCD, ");
        stb.append("   KUBUN, ");
        stb.append("   ABBV1 ");
        return stb.toString();
    }

    private void getVacationMap(final DB2UDB db2, final Map fullMap, final Map detailMap) {
        List detailList = null;
        final String sql = getVacationMapSql();
        log.debug(" getVacationMap sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sche_Date = rs.getString("SCHE_DATE");    //対象日(表の横)
                final String abbv1 = rs.getString("ABBV1");            //対象校時(表の横(詳細))
                final String staffcd = rs.getString("STAFFCD");        //対象職員(表の縦)
                final String kubun = rs.getString("KUBUN");            //データ詳細
                final String print_Name = rs.getString("PRINT_NAME");  //枠内出力名称
                ScheduleData addwk = new ScheduleData(sche_Date, staffcd, kubun, abbv1, print_Name);
                if ("1".equals(kubun)) {
                    final String fstKey = sche_Date + "_" + staffcd;  //縦横が決まった状態で読み込む。※ここは終日なので校時は無し。
                    if (!fullMap.containsKey(fstKey)) {
                        detailList = new ArrayList();
                        fullMap.put(fstKey, detailList);
                    } else {
                        detailList = (List)fullMap.get(fstKey);
                    }
                    detailList.add(addwk);
                } else {
                    final String fstKey = sche_Date + "_" + staffcd + "_" + abbv1;  //縦横(詳細含む)が決まった状態で読み込む。
                    if (!detailMap.containsKey(fstKey)) {
                        detailList = new ArrayList();
                        detailMap.put(fstKey, detailList);
                    } else {
                        detailList = (List)detailMap.get(fstKey);
                    }
                    detailList.add(addwk);
                }
            }
        } catch (Exception ex) {
            log.error("retStringByteValue error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getVacationMapSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERIOD_DAT AS ( ");
        stb.append(" select ");
        stb.append("   NAMECD2, ");
        stb.append("   NAME1, ");
        stb.append("   ABBV1, ");
        stb.append("   ABBV2, ");
        stb.append("   ABBV3 ");
        stb.append(" FROM ");
        stb.append("   V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND NAMECD1 = 'B001' ");
        stb.append("   AND NAMESPARE2 IS NULL");
        stb.append(" ), SAF_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T2.VACATION_DATE AS SCHE_DATE, ");
        stb.append("   CASE WHEN T2.ALL_FLG = '1' THEN '1' ELSE T1.VACATION_KUBUN END AS KUBUN, ");  // 1:丸1日、2,3,4,5:V_NAME_MSTと下記START_TIME、END_TIMEで紐づけした時間の時間割。
        stb.append("   CASE WHEN T1.VACATION_KUBUN = '2' THEN T3.AM_START_TIME ");
        stb.append("        WHEN T1.VACATION_KUBUN = '3' THEN T3.PM_START_TIME ");
        stb.append("        ELSE T2.START_TIME END AS START_TIME, ");
        stb.append("   CASE WHEN T1.VACATION_KUBUN = '2' THEN T3.AM_END_TIME ");
        stb.append("        WHEN T1.VACATION_KUBUN = '3' THEN T3.PM_END_TIME ");
        stb.append("        ELSE T2.END_TIME END AS END_TIME, ");
        stb.append("   '' AS PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   SAF_VACATION_DAT T1 ");
        stb.append("   LEFT JOIN SAF_VACATION_DATE_DAT T2 ");
        stb.append("     ON T2.VACATION_ID = T1.ID ");
        stb.append("    AND T2.SCHOOLID = T1.SCHOOLID ");
        stb.append("    AND T2.STAFFCD = T1.STAFFCD ");
        stb.append("   LEFT JOIN SAF_SCHOOL_MST T3 ");
        stb.append("     ON T3.SCHOOLID = T1.SCHOOLID ");
        stb.append("    AND T3.DELETE_FLG = '0' ");
        stb.append(" WHERE ");
        stb.append("   T2.VACATION_DATE BETWEEN '" + _param._calcd_Start_Date + "' AND '" + _param._calcd_End_Date + "' ");
        stb.append("   AND T1.DELETE_FLG = '0' ");
        stb.append("   AND T1.CONDITION_FLG = '3' ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.KUBUN, ");
        stb.append("   '' AS ABBV1, ");
        stb.append("   T2.PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   PERIOD_DAT T1,SAF_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("   T2.KUBUN = '1' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.KUBUN, ");
        stb.append("   T1.ABBV1, ");
        stb.append("   T2.PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   PERIOD_DAT T1,SAF_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("   VALUE(T1.ABBV1, '') <> '' ");
        stb.append("   AND T2.KUBUN <> '1' ");
        stb.append("   AND ( ");
        stb.append("        (T1.ABBV2 BETWEEN T2.START_TIME AND T2.END_TIME ");
        stb.append("        OR T1.ABBV3 BETWEEN T2.START_TIME AND T2.END_TIME) ");
        stb.append("        OR ");
        stb.append("        (T2.START_TIME BETWEEN T1.ABBV2 AND T1.ABBV3 ");  //包含関係が逆になるのも加味して記載
        stb.append("        OR T2.END_TIME BETWEEN T1.ABBV2 AND T1.ABBV3) ");
        stb.append("   ) ");
        stb.append(" ORDER BY ");
        stb.append("   SCHE_DATE, ");
        stb.append("   STAFFCD, ");
        stb.append("   KUBUN, ");
        stb.append("   ABBV1 ");
        return stb.toString();

    }

    private void getTripMap(final DB2UDB db2, final Map fullMap, final Map detailMap) {
        List detailList = null;
        final String sql = getTripMapSql();
        log.debug(" getTripMapSql sql =" + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String sche_Date = rs.getString("SCHE_DATE");    //対象日(表の横)
                final String abbv1 = rs.getString("ABBV1");            //対象校時(表の横(詳細))
                final String staffcd = rs.getString("STAFFCD");        //対象職員(表の縦)
                final String kubun = rs.getString("KUBUN");            //データ詳細
                final String print_Name = rs.getString("PRINT_NAME");  //枠内出力名称
                ScheduleData addwk = new ScheduleData(sche_Date, staffcd, kubun, abbv1, print_Name);
                if ("1".equals(kubun)) {
                    final String fstKey = sche_Date + "_" + staffcd;  //縦横が決まった状態で読み込む。※ここは終日なので校時は無し。
                    if (!fullMap.containsKey(fstKey)) {
                        detailList = new ArrayList();
                        fullMap.put(fstKey, detailList);
                    } else {
                        detailList = (List)fullMap.get(fstKey);
                    }
                    detailList.add(addwk);
                } else {
                    final String fstKey = sche_Date + "_" + staffcd + "_" + abbv1;  //縦横(詳細含む)が決まった状態で読み込む。
                    if (!detailMap.containsKey(fstKey)) {
                        detailList = new ArrayList();
                        detailMap.put(fstKey, detailList);
                    } else {
                        detailList = (List)detailMap.get(fstKey);
                    }
                    detailList.add(addwk);
                }
            }
        } catch (Exception ex) {
            log.error("retStringByteValue error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }
    private String getTripMapSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PERIOD_DAT AS ( ");
        stb.append(" select ");
        stb.append("   NAMECD2, ");
        stb.append("   NAME1, ");
        stb.append("   ABBV1, ");
        stb.append("   ABBV2, ");
        stb.append("   ABBV3 ");
        stb.append(" FROM ");
        stb.append("   V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND NAMECD1 = 'B001' ");
        stb.append("   AND NAMESPARE2 IS NULL");
        stb.append(" ), SAF_DATA AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.STAFFCD, ");
        stb.append("   T2.TRIP_DATE AS SCHE_DATE, ");
        stb.append("   CASE WHEN T2.ALL_FLG = '1' THEN '1' ELSE T1.TRIP_KUBUN END AS KUBUN, ");
        stb.append("   CASE WHEN T1.TRIP_KUBUN IN ('2', '3') THEN T2.START_TIME ");
        stb.append("        ELSE '' END AS START_TIME, ");
        stb.append("   CASE WHEN T1.TRIP_KUBUN IN ('2', '3') THEN T2.END_TIME ");
        stb.append("        ELSE '' END AS END_TIME, ");
        stb.append("   '' AS PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   SAF_TRIP_DAT T1 ");
        stb.append("   LEFT JOIN SAF_TRIP_DATE_DAT T2 ");
        stb.append("     ON T2.TRIP_ID = T1.ID ");
        stb.append("    AND T2.SCHOOLID = T1.SCHOOLID ");
        stb.append("    AND T2.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("   T2.TRIP_DATE BETWEEN '" + _param._calcd_Start_Date + "' AND '" + _param._calcd_End_Date + "' ");
        stb.append("   AND T1.DELETE_FLG = '0' ");
        stb.append("   AND T1.CONDITION_FLG = '3' ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.KUBUN, ");
        stb.append("   '' AS ABBV1, ");
        stb.append("   T2.PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   PERIOD_DAT T1,SAF_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("   T2.KUBUN = '1' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("   T2.SCHE_DATE, ");
        stb.append("   T2.STAFFCD, ");
        stb.append("   T2.KUBUN, ");
        stb.append("   T1.ABBV1, ");
        stb.append("   T2.PRINT_NAME ");
        stb.append(" FROM ");
        stb.append("   PERIOD_DAT T1,SAF_DATA T2 ");
        stb.append(" WHERE ");
        stb.append("   VALUE(T1.ABBV1, '') <> '' ");
        stb.append("   AND T2.KUBUN <> '1' ");
        stb.append("   AND ( ");
        stb.append("        (T1.ABBV2 BETWEEN T2.START_TIME AND T2.END_TIME ");
        stb.append("        OR T1.ABBV3 BETWEEN T2.START_TIME AND T2.END_TIME) ");
        stb.append("        OR ");
        stb.append("        (T2.START_TIME BETWEEN T1.ABBV2 AND T1.ABBV3 ");  //包含関係が逆になるのも加味して記載
        stb.append("        OR T2.END_TIME BETWEEN T1.ABBV2 AND T1.ABBV3) ");
        stb.append("   ) ");
        stb.append(" ORDER BY ");
        stb.append("   SCHE_DATE, ");
        stb.append("   STAFFCD, ");
        stb.append("   KUBUN, ");
        stb.append("   ABBV1 ");
        return stb.toString();
    }

    private class StaffData {
        final String _staffcd;
        final String _staffname;
        final String _sectioncd;
        final String _sectionname;
        final Map _chairMap;

        public StaffData (final String staffcd, final String staffname, final String sectioncd, final String sectionname) {
            _staffcd = staffcd;
            _staffname = staffname;
            _sectioncd = sectioncd;
            _sectionname = sectionname;
            _chairMap = new LinkedMap();
        }

        private ChairData getLittleChairCd(final String fstKey) {
            ChairData retObj = null;
            String chkCdStr = "";
            if (_chairMap.containsKey(fstKey)) {
                final List srchList = (List)_chairMap.get(fstKey);
                for (Iterator ite = srchList.iterator();ite.hasNext();) {
                    ChairData chkObj = (ChairData)ite.next();
                    if (chkObj._chaircd == null) {
                        continue;
                    }
                    if ("".equals(chkCdStr)) {
                        chkCdStr = chkObj._chaircd;
                        retObj = chkObj;
                    } else {
                        if (chkCdStr.compareTo(chkObj._chaircd) > 0) {
                            chkCdStr = chkObj._chaircd;
                            retObj = chkObj;
                        }
                    }
                }
            }
            return retObj;
        }

        private ChairData getLittleFacCd(final String fstKey) {
            ChairData retObj = null;
            String chkCdStr = "";
            if (_chairMap.containsKey(fstKey)) {
                final List srchList = (List)_chairMap.get(fstKey);
                for (Iterator ite = srchList.iterator();ite.hasNext();) {
                    ChairData chkObj = (ChairData)ite.next();
                    if ( chkObj._faccd == null) {
                        continue;
                    }
                    if ("".equals(chkCdStr)) {
                        chkCdStr = chkObj._faccd;
                        retObj = chkObj;
                    } else {
                        if (chkCdStr.compareTo(chkObj._faccd) > 0) {
                            chkCdStr = chkObj._faccd;
                            retObj = chkObj;
                        }
                    }
                }
            }
            return retObj;
        }
    }

    private class ScheduleData {
        final String _sche_Date;
        final String _staffcd;
        final String _kubun;
        final String _abbv1;
        final String _print_Name;
        public ScheduleData (final String sche_Date, final String staffcd, final String kubun, final String abbv1, final String print_Name)
        {
            _sche_Date = sche_Date;
            _staffcd = staffcd;
            _kubun = kubun;
            _abbv1 = abbv1;
            _print_Name = print_Name;
        }
    }

    private class PeriodData {
        final String _namecd2;
        final String _name1;
        final String _abbv1;
        final String _abbv2;
        final String _abbv3;
        public PeriodData (final String namecd2, final String name1, final String abbv1, final String abbv2, final String abbv3)
        {
            _namecd2 = namecd2;
            _name1 = name1;
            _abbv1 = abbv1;
            _abbv2 = abbv2;
            _abbv3 = abbv3;
        }
    }

    private class ChairData {
        final String _executedate;
        final String _periodcd;
        final String _staffcd;
        final String _chaircd;
        final String _faccd;
        final String _chairname;
        final String _chairabbv;
        final String _facilityname;
        final String _facilityabbv;
        final String _staffname;
        public ChairData (final String executedate, final String periodcd, final String staffcd, final String chaircd, final String faccd, final String chairname, final String chairabbv, final String facilityname, final String facilityabbv, final String staffname)
        {
            _executedate = executedate;
            _periodcd = periodcd;
            _staffcd = staffcd;
            _chaircd = chaircd;
            _faccd = faccd;
            _chairname = chairname;
            _chairabbv = chairabbv;
            _facilityname = facilityname;
            _facilityabbv = facilityabbv;
            _staffname = staffname;
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
        private final String _sDate;  //入力値(未使用)
        private final String _eDate;  //入力値(未使用)
        private final String[] _category_Selected;
        private final String _year;
        private final String _calcd_Start_Date;  //開始日(入力値から変換済み)
        private final String _calcd_End_Date;    //終了日(入力値から変換済み)

        final List _useTermDate;
        final Map _periodMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _sDate = request.getParameter("SDATE");
            _eDate = request.getParameter("EDATE");
            _category_Selected = request.getParameterValues("CATEGORY_SELECTED");
            _year = request.getParameter("YEAR");
            _calcd_Start_Date = request.getParameter("CALCD_START_DATE").replace('/', '-');
            _calcd_End_Date = request.getParameter("CALCD_END_DATE").replace('/', '-');
            _useTermDate = getTermDateMap();
            _periodMap = getPeriodMap(db2);
        }

        private List getTermDateMap() {
            List retMap = new ArrayList();
            Map subMap = null;
            final int onePrintSize = 3;
            final int weekMax = 53 * 2;  //52週間で、週を半分(6日を1ページ出力の3日で分けて設定)しているため。

            Calendar cal = Calendar.getInstance();
            final String[] cutSDate = StringUtils.split(_calcd_Start_Date, '-');
            cal.set(Integer.parseInt(cutSDate[0]), Integer.parseInt(cutSDate[1]) - 1, Integer.parseInt(cutSDate[2]));
            String bakCalDateStr = "";
            while (_calcd_End_Date.compareTo(bakCalDateStr) >= 0) {
                if (retMap.size() > weekMax) {  //1年( <= 53週)以上のスケジュールは出力しない。
                    break;
                }
                final int monWk = cal.get(Calendar.MONTH) + 1;
                bakCalDateStr = cal.get(Calendar.YEAR) + "-" + (monWk < 10 ? "0"+monWk : monWk) + "-" + (cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + cal.get(Calendar.DAY_OF_MONTH) : cal.get(Calendar.DAY_OF_MONTH));
                final int Dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
                if (Dow > 0) {  //日曜は除外
                    if (subMap == null || subMap.size() >= onePrintSize) {
                        subMap = new LinkedMap();
                        retMap.add(subMap);
                    }
                    subMap.put(bakCalDateStr, String.valueOf(Dow));
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            return retMap;
        }

        private Map getPeriodMap(final DB2UDB db2) {
            Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   NAMECD2, ");
            stb.append("   NAME1, ");
            stb.append("   ABBV1, ");
            stb.append("   ABBV2, ");
            stb.append("   ABBV3 ");
            stb.append(" FROM ");
            stb.append("   V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _year + "' ");
            stb.append("   AND NAMECD1 = 'B001'   ");
            stb.append("   AND NAMESPARE2 IS NULL");

            log.debug(" getPeriodMap sql =" + stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String abbv1 = rs.getString("ABBV1");
                    final String abbv2 = rs.getString("ABBV2");
                    final String abbv3 = rs.getString("ABBV3");
                    PeriodData addwk = new PeriodData(namecd2, name1, abbv1, abbv2, abbv3);
                    retMap.put(namecd2, addwk);
                }
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retMap;
        }

    }
}

// eof

