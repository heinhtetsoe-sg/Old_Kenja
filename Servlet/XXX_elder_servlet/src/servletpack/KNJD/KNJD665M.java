// kanji=漢字
/*
 *
 * 作成日: 2021/02/25
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [成績管理]  成績個人票
 */

public class KNJD665M {

    private static final Log log = LogFactory.getLog(KNJD665M.class);

    private static final String TOTAL_HRCLSCD = "ZZZ";
    static boolean _hasData = false;
    static Param _param;
    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            printMain(db2, svf);
        } catch (Exception e) {
            log.error("Exception", e);
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
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        svf.VrSetForm("KNJD665M.frm", 1);
        final Map hrClsInfo = getHrClsInfo(db2);
        final Map ttlInfo = getTotalScore(db2, hrClsInfo);
        final List SRange = getScoreRange(ttlInfo);
        List slicePClass = SlicePageClass(hrClsInfo);  //1ページ6組
        List slicePScoreRange = calcSRangeMaxLine(hrClsInfo, SRange);  //1ページ(40Line内)毎の得点出力範囲
        int ruikeiCnt = 0;
        int ruikeibak = 0;
        for (Iterator itps = slicePScoreRange.iterator();itps.hasNext();) {  //1ページ出力点数範囲毎に処理
            final Map rngInfoMap = (Map)itps.next();
            for (Iterator itpc = slicePClass.iterator();itpc.hasNext();) {  //1ページクラス単位に処理
                final List p1classes = (List)itpc.next();
                boolean fstRngLoopFlg = true;  //初回range終了まで
                boolean fstPrtHeadFlg = true;  //1回出力制御
                int prtLine = 1;
                for (Iterator itr = rngInfoMap.keySet().iterator();itr.hasNext();) {
                    final String rngStr = (String)itr.next();
                    final rngLineTotalInfo rngLTObj = (rngLineTotalInfo)rngInfoMap.get(rngStr);
                    ruikeiCnt += rngLTObj._totalCnt;
                    int prtClsCnt = 1;
                    svf.VrsOutn("SCORE1", prtLine, rngStr);  //点数左
                    svf.VrsOutn("SCORE2", prtLine, rngStr);  //点数右
                    if (ruikeibak != ruikeiCnt) {
                        svf.VrsOutn("TOTAL", prtLine, String.valueOf(ruikeiCnt));  //累計人数
                        ruikeibak = ruikeiCnt;
                    }
                    for (Iterator itc = p1classes.iterator();itc.hasNext();) {
                        final HrClsInfo hcWk = (HrClsInfo)itc.next();
                        //ヘッダ出力
                        if (fstPrtHeadFlg) {
                            setTitle(db2, svf);
                            if (ttlInfo.containsKey(TOTAL_HRCLSCD)) {
                                TotalInfo ttlClsObj = (TotalInfo)ttlInfo.get(TOTAL_HRCLSCD);
                                svf.VrsOut("GRADE_NUM", ttlClsObj._cnt);      //人数
                                svf.VrsOut("GRADER_MAX", ttlClsObj._mxScore); //最高
                                svf.VrsOut("GRADE_MIN", ttlClsObj._mnScore);  //最低
                                svf.VrsOut("GRADE_TOTAL", ttlClsObj._total);  //合計
                                svf.VrsOut("GRADE_AVE", ttlClsObj._avg);      //平均
                            }
                            fstPrtHeadFlg = false;
                        }
                        if (fstRngLoopFlg) {
                            svf.VrsOut("HR_NAME1_" + prtClsCnt, hcWk._hrClassName1);      //組名
                            svf.VrsOutn("HR_NAME2", prtClsCnt, hcWk._hrClassName1);       //下表の組名
                            if (hcWk._ttlInfo != null) {
                                svf.VrsOutn("HR_NUM", prtClsCnt, hcWk._ttlInfo._cnt);     //人数
                                svf.VrsOutn("HR_MAX", prtClsCnt, hcWk._ttlInfo._mxScore); //最高
                                svf.VrsOutn("HR_MIN", prtClsCnt, hcWk._ttlInfo._mnScore); //最低
                                svf.VrsOutn("HR_TOTAL", prtClsCnt, hcWk._ttlInfo._total); //合計
                                svf.VrsOutn("HR_AVE", prtClsCnt, hcWk._ttlInfo._avg);     //平均
                            }
                        }
                        if (hcWk._mergeMap.containsKey(rngStr)) {
                            printNameCtlInfo prtNObj = (printNameCtlInfo)hcWk._mergeMap.get(rngStr);
                            int subLineCnt = 0;
                            for(Iterator itn = prtNObj._nameBuffer.iterator();itn.hasNext();) {
                                final String nStr = (String)itn.next();
                                final int nslen = KNJ_EditEdit.getMS932ByteLength(nStr);
                                final String nField = nslen > 28 ? "3" : nslen > 22 ? "2" : "1";
                                svf.VrsOutn("NAMW" + prtClsCnt + "_" + nField, prtLine + subLineCnt, nStr);  //名前出力
                                if (itn.hasNext()) {
                                    subLineCnt++;
                                }
                            }
                        }
                        prtClsCnt++;
                    }
                    prtLine += rngLTObj._maxNameLine;
                    _hasData = true;
                    fstRngLoopFlg = false;
                }
                svf.VrEndPage();
            }
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("SCHOOL_NAME", _param._schoolName + " " + _param._schoolKindName + " " + _param._gradeName);
        svf.VrsOut("TEST_NAME", createTestNameStr());
        svf.VrsOut("SUBCLASS_NAME", createSubclsNameStr());
        svf.VrsOut("DATE", "出力日：" + KNJ_EditDate.h_format_JP(db2, _param._srvDate));
    }

    private String createTestNameStr() {
        String retStr = "";
        String delim = "";
        for (int cnt = 0; cnt < _param._testItem.size(); cnt++) {
            final TestItem testItem = (TestItem) _param._testItem.get(cnt);
            retStr += delim + testItem._testitemname;
            delim = "/";
        }
        return retStr;
    }
    private String createSubclsNameStr() {
        String retStr = "";
        String delim = "";
        for (int cnt = 0; cnt < _param._selectSubclsItem.length; cnt++) {
            SubclassMst mst = SubclassMst.getSubclassMst(_param._subclassMstMap, _param._selectSubclsItem[cnt]);
            if (mst != null) {
                retStr += delim + mst._subclassname;
                delim = "/";
            }
        }
        return retStr;
    }

    private List SlicePageClass(final Map hrClsInfo) {
        final List retList = new ArrayList();
        List subList = new ArrayList();
        final int maxClassCnt = 6;
        int cnt = 0;
        for (Iterator ite = hrClsInfo.keySet().iterator();ite.hasNext();) {
            final String kStr = (String)ite.next();
            final HrClsInfo wObj = (HrClsInfo)hrClsInfo.get(kStr);
            if (cnt % maxClassCnt == 0) {
                subList = new ArrayList();
                retList.add(subList);
            }
            subList.add(wObj);
            cnt++;
        }
        return retList;
    }

    private List calcSRangeMaxLine(final Map hrClsInfo, final List SRange) {
        final List retList = new ArrayList();
        Map subMap = null;
        final int maxLine = 35;
        int cnt = 0;
        for (Iterator its = SRange.iterator();its.hasNext();) {
            final String rngStr = (String)its.next();
            rngLineTotalInfo addWk = new rngLineTotalInfo(rngStr);
            for (Iterator ite = hrClsInfo.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                final HrClsInfo wObj = (HrClsInfo)hrClsInfo.get(kStr);
                if (wObj._mergeMap.containsKey(rngStr)) {
                    printNameCtlInfo pNWk= (printNameCtlInfo)wObj._mergeMap.get(rngStr);
                    if (pNWk._nameBuffer.size() > addWk._maxNameLine) {
                        addWk._maxNameLine = pNWk._nameBuffer.size();
                    }
                    addWk._totalCnt += pNWk._addCnt;
                }
            }
            final int calcAdLine = addWk._maxNameLine == 0 ? 1 : addWk._maxNameLine;  //点数出力分を加味
            if ((cnt == 0 && subMap == null) || cnt + calcAdLine > maxLine) {
                subMap = new LinkedMap();
                retList.add(subMap);
                cnt = 0;
            }
            cnt += calcAdLine;
            subMap.put(rngStr, addWk);
        }
        return retList;
    }

    private List getScoreRange(final Map ttlInfo) {
        final List retList = new ArrayList();
        if (ttlInfo.containsKey(TOTAL_HRCLSCD)) {
            final TotalInfo ctlObj = (TotalInfo)ttlInfo.get(TOTAL_HRCLSCD);
            retList.add(_param._perfectScore);
            int mxScore = Integer.parseInt(ctlObj._mxScore);
            int mnScore = Integer.parseInt(ctlObj._mnScore);
            for (int cnt = mxScore;cnt >= mnScore;cnt--) {
                retList.add(String.valueOf(cnt));
            }
        }
        return retList;
    }

    private Map getTotalScore(final DB2UDB db2, final Map hrClsInfo) {
        final Map retMap = new LinkedMap();
        final String sql = getTotalScoreSql();
        log.debug(sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String cnt = rs.getString("CNT");
                final String mxScore = rs.getString("MXSCORE");
                final String mnScore = rs.getString("MNSCORE");
                final String total = rs.getString("TOTAL");
                final String avg = rs.getString("AVG");
                TotalInfo addWk = new TotalInfo(grade, hrClass, cnt, mxScore, mnScore, total, avg);
                if (hrClsInfo.containsKey(hrClass)) {
                    HrClsInfo ctlObj = (HrClsInfo)hrClsInfo.get(hrClass);
                    ctlObj._ttlInfo = addWk;
                } else {
                    if (TOTAL_HRCLSCD.equals(hrClass)) {
                        retMap.put(hrClass, addWk);
                    }
                }
            }
        } catch (SQLException ex) {
            log.debug("getGradeName exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getTotalScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(getScoreBaseSql());  //SCOREBASE_Tを利用
        stb.append(" ), MAXBASE_T AS ( ");
        stb.append(" select ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO, ");
        stb.append("   SCHREGNO, ");
        stb.append("   NAME, ");
        stb.append("   SUM(VALUE(SCORE, 0)) AS SCORE ");
        stb.append(" FROM ");
        stb.append("   SCOREBASE_T ");
        stb.append(" GROUP BY ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO, ");
        stb.append("   SCHREGNO, ");
        stb.append("   NAME ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   COUNT(SCHREGNO) AS CNT, ");
        stb.append("   MAX(SCORE) AS MXSCORE, ");
        stb.append("   MIN(SCORE) AS MNSCORE, ");
        stb.append("   SUM(VALUE(SCORE, 0)) AS TOTAL, ");
        stb.append("   DECIMAL(( (SUM(VALUE(SCORE, 0)) * 1.0 / COUNT(SCHREGNO) * 1.0) *10.0+0.5)/10.0, 5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("   MAXBASE_T ");
        stb.append(" GROUP BY ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("   GRADE, ");
        stb.append("   'ZZZ' AS HR_CLASS, ");
        stb.append("   COUNT(SCHREGNO) AS CNT, ");
        stb.append("   MAX(SCORE) AS MXSCORE, ");
        stb.append("   MIN(SCORE) AS MNSCORE, ");
        stb.append("   SUM(VALUE(SCORE, 0)) AS TOTAL, ");
        stb.append("   DECIMAL(( (SUM(VALUE(SCORE, 0)) * 1.0 / COUNT(SCHREGNO) * 1.0) *10.0+0.5)/10.0, 5,1) AS AVG ");
        stb.append(" FROM ");
        stb.append("   MAXBASE_T ");
        stb.append(" GROUP BY ");
        stb.append("   GRADE ");
        stb.append(" ORDER BY ");
        stb.append("   GRADE,HR_CLASS ");
        return stb.toString();
    }

    private Map getHrClsInfo(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final Map clsNameMap = getHrNameMap(db2);
        final String sql = getHrClsInfoSql();
        log.debug(sql);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrClassName1 = (String)clsNameMap.get(hrClass);
                final String attendNo = rs.getString("ATTENDNO");
                final String schregNo = rs.getString("SCHREGNO");
                final String inoutCd = rs.getString("INOUTCD");
                final String baseRemark = rs.getString("BASE_REMARK1");
                final String name = rs.getString("NAME");
                final String score = rs.getString("SCORE");
                final HrClsInfo addWk;
                SchregInfo addSchWk = new SchregInfo(attendNo, schregNo, inoutCd, baseRemark, name, score);
                if (!retMap.containsKey(hrClass)) {
                    addWk = new HrClsInfo(grade, hrClass, hrClassName1);
                    retMap.put(hrClass, addWk);
                } else {
                    addWk = (HrClsInfo)retMap.get(hrClass);
                }
                addWk.addScore(schregNo, addSchWk);
            }
        } catch (SQLException ex) {
            log.debug("getGradeName exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getHrClsInfoSql() {
        final String[] semesBuf = new String[_param._testItem.size()];
        for (int cnt = 0; cnt < _param._testItem.size(); cnt++) {
            final TestItem testItem = (TestItem) _param._testItem.get(cnt);
            if (testItem != null) {
                semesBuf[cnt] = testItem._semester._cd;
            }
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(getScoreBaseSql());  //SCOREBASE_Tを利用
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO, ");
        stb.append("   SCHREGNO, ");
        stb.append("   INOUTCD, ");
        stb.append("   BASE_REMARK1, ");
        stb.append("   NAME, ");
        stb.append("   SUM(CASE WHEN SCORE IS NOT NULL THEN SCORE END) AS SCORE ");
        stb.append(" FROM ");
        stb.append("   SCOREBASE_T ");
        stb.append(" GROUP BY ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO, ");
        stb.append("   SCHREGNO, ");
        stb.append("   INOUTCD, ");
        stb.append("   BASE_REMARK1, ");
        stb.append("   NAME ");
        stb.append(" ORDER BY ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO ");
        return stb.toString();
    }

    private Map getHrNameMap(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getHrNameSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
                retMap.put(hr_Class, hr_Class_Name1);
            }
        } catch (SQLException ex) {
            log.debug("getGradeName exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getHrNameSql() {
        //MAP格納時にHR_CLASSベースで格納するので、最終SEMESTERのみ採用となるので注意。
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("   HR_CLASS, ");
        stb.append("   SEMESTER, ");
        stb.append("   HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND GRADE = '" + _param._grade + "' ");
        stb.append("   AND SEMESTER IN " + SQLUtils.whereIn(false, getTestSemesterStrs()) + " ");
        stb.append(" ORDER BY ");
        stb.append("   HR_CLASS, ");
        stb.append("   SEMESTER ");
        return stb.toString();
    }

    private String[] getTestSemesterStrs() {
        final String[] semesBuf = new String[_param._testItem.size()];
        for (int cnt = 0; cnt < _param._testItem.size(); cnt++) {
            final TestItem testItem = (TestItem) _param._testItem.get(cnt);
            if (testItem != null) {
                semesBuf[cnt] = testItem._semester._cd;
            }
        }
        return semesBuf;
    }

    private String getScoreBaseSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PR_CONNECT_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   T3.* ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T3 ");
        stb.append(" WHERE ");
        stb.append("    T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV IN " + SQLUtils.whereIn(false, _param._selectTestCd) + " ");
        stb.append("    AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' ||  T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._selectSubclsItem) + " ");
        stb.append(" ), SCOREBASE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T4.INOUTCD, ");
        stb.append("   T8.BASE_REMARK1, ");
        stb.append("   T4.NAME, ");
        stb.append("   T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV AS TESTCD, ");
        stb.append("   T6.SEMESTERNAME, ");
        stb.append("   T5.TESTITEMNAME, ");
        stb.append("   T5.TESTITEMABBV1, ");
        stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' ||  T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("   T7.SUBCLASSNAME, ");
        stb.append("   T7.SUBCLASSABBV, ");
        stb.append("   T3.SCORE ");
        stb.append(" FROM ");
        stb.append("   schreg_regd_dat T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN PR_CONNECT_T T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T4 ");
        stb.append("     ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_DETAIL_MST T8 ");
        stb.append("     ON T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T8.BASE_SEQ = '003' ");
        stb.append("   LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T5 ");
        stb.append("     ON T5.YEAR = T1.YEAR ");
        stb.append("    AND T5.SEMESTER || '-' || T5.TESTKINDCD || T5.TESTITEMCD || T5.SCORE_DIV = T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV ");
        stb.append("   LEFT JOIN SEMESTER_MST T6 ");
        stb.append("     ON T6.YEAR = T1.YEAR ");
        stb.append("    AND T6.SEMESTER = T1.SEMESTER ");
        stb.append("   LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("     ON T7.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T7.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T7.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T7.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER IN " + SQLUtils.whereIn(false, getTestSemesterStrs()) + " ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T2.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        return stb.toString();
    }

    private class HrClsInfo {
        final String _grade;
        final String _hrClass;
        final String _hrClassName1;
        final Map _mergeMap;
        TotalInfo _ttlInfo;
        public HrClsInfo(final String grade, final String hrClass, final String hrClassName1) {
            _grade = grade;
            _hrClass = hrClass;
            _hrClassName1 = hrClassName1;
            _mergeMap = new LinkedMap();
            _ttlInfo = null;
        }
        public void addScore(final String key, final SchregInfo sch) {
            printNameCtlInfo pnCtl = null;
            final String[] dispMark = {"○", "☆", "◇"};
            final int[][] chkRangeTbl = {{4000, 4999},{5000,5999},{7000,7999}};
            if (sch != null && sch._score != null && !"".equals(sch._score)) {
                if (!_mergeMap.containsKey(sch._score)) {
                    pnCtl = new printNameCtlInfo(sch._score);
                    _mergeMap.put(sch._score, pnCtl);
                } else {
                    pnCtl = (printNameCtlInfo)_mergeMap.get(sch._score);
                }
                final String ioStr = (sch._inoutCd != null && "0".equals(sch._inoutCd)) ? "*" : "";
                String brStr = "";
                if (sch._baseRemark != null && NumberUtils.isNumber(sch._baseRemark)) {
                    final long brVal = Long.parseLong(StringUtils.defaultString(sch._baseRemark, "0"));
                    for (int idx = 0;idx <= 2;idx++) {
                        if (chkRangeTbl[idx][0] <= brVal && brVal <= chkRangeTbl[idx][1]) {
                            brStr = dispMark[idx];
                        }
                    }
                }
                pnCtl.addName(ioStr + brStr + sch._name);
            }
        }
    }

    private class SchregInfo {
        final String _schregNo;
        final String _attendNo;
        final String _inoutCd;
        final String _baseRemark;
        final String _name;
        final String _score;
        public SchregInfo(final String attendNo, final String schregNo, final String inoutCd, final String baseRemark, final String name, final String score) {
            _attendNo = attendNo;
            _schregNo = schregNo;
            _inoutCd = inoutCd;
            _baseRemark = baseRemark;
            _name = name;
            _score = score;
        }
    }

    private class TotalInfo {
        final String _grade;
        final String _hrClass;
        final String _cnt;
        final String _mxScore;
        final String _mnScore;
        final String _total;
        final String _avg;
        public TotalInfo (final String grade, final String hrClass, final String cnt, final String mxScore, final String mnScore, final String total, final String avg)
        {
            _grade = grade;
            _hrClass = hrClass;
            _cnt = cnt;
            _mxScore = mxScore;
            _mnScore = mnScore;
            _total = total;
            _avg = avg;
        }
    }

    private class printNameCtlInfo {
        final String _score;
        int _addCnt;
        final List _nameBuffer;
        printNameCtlInfo(final String score) {
            _score = score;
            _addCnt = 0;
            _nameBuffer = new ArrayList();
        }
        public void addName(final String name) {
            String ctlStr = null;
            if (name != null && !"".equals(name) && _score != null && !"".equals(_score)) {
                final String cutStr;
                if (name.indexOf("　") >= 0) {
                    final String[] spStr = StringUtils.split(name, "　");
                    cutStr = spStr[0];
                } else {
                    cutStr = name;
                }
                if (_addCnt % 3 == 0) {
                    ctlStr = new String(cutStr);
                    _nameBuffer.add(ctlStr);
                } else {
                    ctlStr = (String)_nameBuffer.get(_nameBuffer.size() - 1);
                    ctlStr += "　" + cutStr;
                    _nameBuffer.set(_nameBuffer.size() - 1, ctlStr);
                }
                _addCnt++;
            }
        }
    }

    private class rngLineTotalInfo {
        final String _score;
        int _totalCnt;
        int _maxNameLine;
        rngLineTotalInfo(final String score) {
            _score = score;
            _maxNameLine = 1;
            _totalCnt = 0;
        }
    }

    private static class Semester {
        final String _cd;
        final String _name;
        final String _sdate;
        final String _edate;
        final List _testItemList;
        Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
            _testItemList = new ArrayList();
        }

        public String toString() {
            return "(" + _name + " [" + _sdate + "," + _edate + "])";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final String _scoreDivName;
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf, final String scoreDivName) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _scoreDivName = scoreDivName;
        }
        public String getTestcd() {
            return _semester._cd +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._cd + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private static class SubclassMst {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        boolean _isSaki;
        final Set _attendSubclasscdSet = new TreeSet();
        final Set _combinedSubclasscdSet = new TreeSet();
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
        }
        public String toString() {
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }

        private static SubclassMst getSubclassMst(final Map subclassMstMap, final String subclasscd) {
            if (null == subclassMstMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null);
            }
            return (SubclassMst) subclassMstMap.get(subclasscd);
        }

        private static Map getSubclassMstMap(
                final DB2UDB db2,
                final String year
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, ";
                sql += " VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"));
                    subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            try {
                String sql = "";
                sql += " SELECT ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += "     ,  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String attendSubclasscd = rs.getString("ATTEND_SUBCLASSCD");
                    final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");

                    final SubclassMst atsub = SubclassMst.getSubclassMst(subclassMstMap, attendSubclasscd);
                    if (null == atsub) {
                        log.warn("not found attend subclass : " + attendSubclasscd);
                    } else {
                        atsub._combinedSubclasscdSet.add(combinedSubclasscd);
                    }

                    final SubclassMst comsub = SubclassMst.getSubclassMst(subclassMstMap, combinedSubclasscd);
                    if (null == comsub) {
                        log.warn("not found combined subclass : " + combinedSubclasscd);
                    } else {
                        comsub._isSaki = true;
                        comsub._attendSubclasscdSet.add(attendSubclasscd);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return subclassMstMap;
        }
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade;
        final String _schoolKind;
        final String _classCd;

        final String _ctrlDate;
        final String _srvDate;

        final String[] _selectTestCd;
        final String[] _selectSubclsItem;

        final String _gradeName;
        final String _schoolName;
        final String _schoolKindName;

        final boolean _isSeireki;

        /** 学期・テスト種別と考査名称のマップ */
        final List _testItem;
        final TreeMap _semesterMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _setSchoolKind;

        final KNJSchoolMst _knjSchoolMst;
        final Map _subclassMstMap;
        final String _perfectScore;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, ParseException {

            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeName = getGradeName(db2);
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _classCd = request.getParameter("CLASSCD");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _selectTestCd = request.getParameterValues("CATEGORY_SELECTED");
            _selectSubclsItem = request.getParameterValues("CATEGORY_SELECTED2");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _setSchoolKind = request.getParameter("SCHOOL_KIND");

            _isSeireki = loadNameMstZ012(db2);

            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            _schoolName = StringUtils.defaultString(_knjSchoolMst._schoolName1, "");
            _schoolKindName = getSchoolKindName(db2);

            _semesterMap = loadSemester(db2);
            _testItem = getTestKindItemList(db2, _setSchoolKind);

            _subclassMstMap = SubclassMst.getSubclassMstMap(db2, _year);
            _perfectScore = getPScore(db2);
            _srvDate = getSrvDate();
        }

        private String getSrvDate() {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd" );
            return format.format(cal.getTime());  //現在時刻を取得
        }

        public List getTargetSemester() {
            final List list = new ArrayList();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                if (_semester.compareTo(semester) >= 0) {
                    list.add(semester);
                }
            }
            return list;
        }

        public List getTargetTestcds(final String tgtTestCds) {
            final List list = new ArrayList();
            for (int cnt = 0; cnt < _testItem.size(); cnt++) {
                final TestItem testItem = (TestItem) _testItem.get(cnt);
                if (tgtTestCds.compareTo(testItem.getTestcd()) >= 0) {
                    list.add(testItem.getTestcd());
                }
            }
            return list;
        }

        private List getTestKindItemList(final DB2UDB db2, final String setSchoolKind) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-000000' ");
                stb.append("     AND T1.SCHOOL_KIND = '" +  setSchoolKind + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T12.NAME1 AS SCORE_DIV_NAME ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                stb.append(" LEFT JOIN NAME_MST T12 ON T12.NAMECD1 = 'D053' ");
                stb.append("    AND T12.NAMECD2 = T1.SCORE_DIV ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-"学校校種"-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                stb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN " + SQLUtils.whereIn(false, _selectTestCd));
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) _semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    final String scoreDivName = rs.getString("SCORE_DIV_NAME");

                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf, scoreDivName);
                    semester._testItemList.add(testItem);
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info(" testcd = " + list);
            return list;
        }

        private TreeMap loadSemester(final DB2UDB db2) {
            final TreeMap semesterMap = new TreeMap();
            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    final Semester semester = new Semester(cd, name, sdate, edate);
                    semesterMap.put(cd, semester);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterMap;
        }

        private boolean loadNameMstZ012(final DB2UDB db2) throws SQLException {
            boolean isSeireki = false;
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z012'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAME1");
                if ("2".equals(name)) isSeireki = true;
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            log.debug("(名称マスタZ012):西暦フラグ = " + isSeireki);
            return isSeireki;
        }

        private String getSchoolKindName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   ABBV1 ");
            stb.append(" FROM ");
            stb.append("   V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _year + "' ");
            stb.append("   AND NAMECD1 = 'A023' ");
            stb.append("   AND NAME1 = '" + _schoolKind + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("ABBV1"), "");
                }
            } catch (SQLException ex) {
                log.debug("getSchoolKindName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getPScore(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();

            stb.append(" WITH CODEFILTER_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   T3.YEAR, ");
            stb.append("   T3.SEMESTER, ");
            stb.append("   T3.TESTKINDCD, ");
            stb.append("   T3.TESTITEMCD, ");
            stb.append("   T3.CLASSCD, ");
            stb.append("   T3.SCHOOL_KIND, ");
            stb.append("   T3.CURRICULUM_CD, ");
            stb.append("   T3.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_DAT T3 ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _year + "' ");
            stb.append("   AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV IN " + SQLUtils.whereIn(false, _selectTestCd) + " ");
            stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' ||  T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _selectSubclsItem) + " ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   SUM(VALUE(TX.PERFECT, 100)) AS PMAX ");
            stb.append(" FROM ");
            stb.append("   CODEFILTER_T T3 ");
            stb.append("   LEFT JOIN PERFECT_RECORD_DAT TX ");
            stb.append("     ON TX.YEAR = T3.YEAR ");
            stb.append("    AND TX.SEMESTER || TX.TESTKINDCD || TX.TESTITEMCD = T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD ");
            stb.append("    AND TX.CLASSCD || '-' || TX.SCHOOL_KIND || '-' || TX.CURRICULUM_CD || '-' ||  TX.SUBCLASSCD = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' ||  T3.SUBCLASSCD ");
            stb.append("    AND TX.GRADE = '"+ _grade +"' ");
            log.debug(stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("PMAX");
                }
            } catch (SQLException ex) {
                log.debug("getPScore exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getGradeName(final DB2UDB db2) {
            String retStr = "";
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   GRADE_NAME1 ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _year + "' ");
            stb.append("   AND GRADE = '" + _grade + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("GRADE_NAME1"), "");
                }
            } catch (SQLException ex) {
                log.debug("getGradeName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }
    }
}
