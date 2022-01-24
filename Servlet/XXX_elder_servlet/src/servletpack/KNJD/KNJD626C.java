/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id$
 *
 * 作成日: 2019/10/30
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD626C {

    private static final Log log = LogFactory.getLog(KNJD626C.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

    private static final int MAX_LINE = 50;

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

        //科目リスト
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);
        Map classMstcnt = new LinkedMap();

        final List printSubclassMstMap = new ArrayList();
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            // 教科の科目数をカウント
            if (_param._printSublassMap.containsKey(subclassMst._subclasscd)) {
                printSubclassMstMap.add(subclassMst);
                final String sKey = subclassMst._classcd + "-" + subclassMst._schoolKind;
                if(classMstcnt.containsKey(sKey)) {
                    int cnt = ((Integer)classMstcnt.get(sKey)).intValue();
                    classMstcnt.put(sKey, ++cnt);
                }
                else {
                    classMstcnt.put(sKey, 1);
                }
            }
        }

        //年組リスト
        final List gradeHrClassList = getList(db2);

        svf.VrSetForm("KNJD626C.frm", 4);

        //明細部以外を印字
        printTitle(db2, svf, printSubclassMstMap, gradeHrClassList);


        int sougouMax = 0;
        int sougouMin = 0;
        int sougouNum = 0;
        BigDecimal sougouAvg = new BigDecimal(0.0);
        BigDecimal sougouDev = new BigDecimal(0.0);
        int sougouCnt = 0;
        for (Iterator itSubclass = printSubclassMstMap.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();

            final String subclassCd = subclassMst._subclasscd;
            final Map resultMap = getScoreData2(db2, subclassCd, _param._grade);
            boolean isTarget = false;
            String max = (String)resultMap.get("MAX_SCORE"); //最高点
            if (NumberUtils.isDigits(max)) {
                sougouMax += Integer.parseInt(max);
                isTarget = true;
            }
            String min = (String)resultMap.get("MIN_SCORE"); //最低点
            if (NumberUtils.isDigits(min)) {
                sougouMin += Integer.parseInt(min);
                isTarget = true;
            }
            String cnt =  (String)resultMap.get("COUNT"); //人数
            if (NumberUtils.isDigits(cnt)) {
                sougouNum += Integer.parseInt(cnt);
                isTarget = true;
            }
            String avg = (String)resultMap.get("AVG"); //平均点
            if (NumberUtils.isNumber(avg)) {
                sougouAvg = sougouAvg.add(new BigDecimal(avg));
                isTarget = true;
            }
            String dev = (String)resultMap.get("STDDEV"); //標準偏差
            if (NumberUtils.isNumber(dev)) {
                sougouDev = sougouDev.add(new BigDecimal(dev));
                isTarget = true;
            }
            if (isTarget) {
                sougouCnt++;
            }
        }

        if (sougouCnt > 0) {
            //総合成績
            svf.VrsOut("TOTAL_SCORE1", String.valueOf(sougouMax / sougouCnt)); //最高点
            svf.VrsOut("TOTAL_SCORE2", String.valueOf(sougouMin / sougouCnt)); //最低点
            svf.VrsOut("TOTAL_SCORE3", String.valueOf(sougouNum / sougouCnt)); //人数
            svf.VrsOut("TOTAL_SCORE4", String.valueOf(sougouAvg.divide(new BigDecimal(sougouCnt), 1, BigDecimal.ROUND_HALF_UP))); //平均点
            svf.VrsOut("TOTAL_SCORE5", String.valueOf(sougouDev.divide(new BigDecimal(sougouCnt), 1, BigDecimal.ROUND_HALF_UP))); //標準偏差
        }


        String defClassCd = "";
        int grp = 1;
        int grp2 = 99;
        List prtUpClsNameIdxList = null;
        int prtUpClsAbbvIdx = 0;
        boolean chkUpFstFlg = false;
        int upColCnt = 0;


        for (Iterator itSubclass = printSubclassMstMap.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            if(!"".equals(defClassCd) && !(subclassMst._classcd + "-" + subclassMst._schoolKind).equals(defClassCd)) {
                grp++;
                grp2--;
                chkUpFstFlg = false;
                prtUpClsAbbvIdx = 0;
                upColCnt = 0;
            }

            svf.VrsOut("GRPCD1", String.valueOf(grp));
            svf.VrsOut("GRPCD2", String.valueOf(grp));

            //教科名、科目名
            int cnt = ((Integer)classMstcnt.get(subclassMst._classcd + "-" + subclassMst._schoolKind)).intValue();

            if (!chkUpFstFlg) {
                prtUpClsNameIdxList = getClsNPrtIdx(subclassMst._classname, cnt * 2);
                chkUpFstFlg = true;
            }

            if (prtUpClsNameIdxList == null) {
                if (subclassMst._classname != null) {
                       svf.VrsOut("CLASS_NAME1_1", subclassMst._classname);
                       svf.VrsOut("CLASS_NAME2_1", subclassMst._classname);
                       prtUpClsAbbvIdx++;
                }
            } else {
                final int prtpoint = (upColCnt * 2) + 1;
                if (prtUpClsNameIdxList.contains(String.valueOf(prtpoint)) || prtUpClsNameIdxList.contains(String.valueOf(prtpoint+1))) {
                    if ( (Integer.parseInt((String)prtUpClsNameIdxList.get(0)) == prtpoint
                          || Integer.parseInt((String)prtUpClsNameIdxList.get(0)) == prtpoint + 1
                          )&& Integer.parseInt((String)prtUpClsNameIdxList.get(0)) % 2 == 0) { //先頭で偶数開始なら、先頭は空白を入れる必要あり。
                        svf.VrsOut("CLASS_NAME1_1", "　" + subclassMst._classname.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx+1));
                        svf.VrsOut("CLASS_NAME2_1", "　" + subclassMst._classname.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx+1));
                        prtUpClsAbbvIdx++;
                    } else {
                        //もう1文字取る必要があるか、確認。
                        if (prtUpClsNameIdxList.contains(String.valueOf(prtpoint+1))) {
                            if(cnt == 1 &&  prtUpClsNameIdxList.size() > 2) {
                                svf.VrsOut("CLASS_NAME1_2", subclassMst._classname);
                                svf.VrsOut("CLASS_NAME2_2", subclassMst._classname);
                                prtUpClsAbbvIdx += 2;
                            } else {
                                final String prtStr = subclassMst._classname.length() - prtUpClsAbbvIdx >= 2 ? subclassMst._classname.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx + 2) : subclassMst._classname.length() - prtUpClsAbbvIdx >= 1 ? subclassMst._classname.substring(prtUpClsAbbvIdx) : "";
                                svf.VrsOut("CLASS_NAME1_1", prtStr);
                                svf.VrsOut("CLASS_NAME2_1", prtStr);
                                prtUpClsAbbvIdx += 2;
                            }
                        } else {
                            final String prtStr = subclassMst._classname.length() - prtUpClsAbbvIdx >= 1 ? subclassMst._classname.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx + 1) : "";
                            svf.VrsOut("CLASS_NAME1_1", prtStr);
                            svf.VrsOut("CLASS_NAME2_1", prtStr);
                            prtUpClsAbbvIdx++;
                        }
                    }
                }
            }


            final String subclassName = (String) _param._printSublassMap.get(subclassMst._subclasscd);
            svf.VrsOut("SUBCLASS_NAME1", subclassName); //科目名
            svf.VrsOut("SUBCLASS_NAME2", subclassName); //科目名

            //クラス別評定の度数分布表
            printSvfMain(db2, svf, subclassMst, gradeHrClassList, grp);

            //学年成績類型とクラス別平均
            printTotal(db2, svf, subclassMst, gradeHrClassList, grp);

            svf.VrEndRecord();
            defClassCd = subclassMst._classcd + "-" + subclassMst._schoolKind;
            upColCnt++;
        }
        svf.VrEndPage();
    }

    private List getClsNPrtIdx(final String classAbbv, final int subMapsize) {
        List retList = null;  //nullなら、1文字出力か、出力文字列が空文字orNULL
        if (subMapsize >= 1 && classAbbv.length() <= 2 * subMapsize) {
            //文字の出力開始位置の求め方
            //もし、入る文字数<出力文字数なら、半分のフォントサイズ(入る文字数は倍)にする。
            int useSize = 0;
            if (subMapsize < classAbbv.length()) {
                useSize = subMapsize * 2;
            } else {
                useSize = subMapsize;
            }
            //文字列の開始位置は、入る文字数の中間点(端数切り上げ)と出力文字の中間点(端数は調整。%の計算部分がそれ)の差を求めて、そこに1加算する。
            //計算上、端数は出ずに整数となる。
            BigDecimal calcWakuSize = new BigDecimal(Math.ceil(useSize/2.0));
            int calcwk = classAbbv.length() - ((useSize+1) % 2);
            BigDecimal calcStrLen = new BigDecimal(Math.floor(calcwk/2.0));
            int strtPt = calcWakuSize.subtract(calcStrLen).intValue();
            for (int cnt = 0;cnt < classAbbv.length();cnt++) {
                if (cnt < useSize) {
                    if (retList == null ) retList = new ArrayList();
                    if((useSize == 3 ||  useSize == 5 ) && cnt == 1 && classAbbv.length() == 2) {
                        retList.add(String.valueOf(strtPt+cnt+1));
                    } else {
                        retList.add(String.valueOf(strtPt+cnt));
                    }
                }
            }
        }
        return retList;
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final List subclassList, final List gradeHrClassList) {
        //明細部以外を印字
        String tiele = _param._nendo +"　"+ _param._testkindName + "【" + _param._gradeName +"】5段階評定度数分布表";
        svf.VrsOut("TITLE", tiele); //タイトル
        svf.VrsOut("DATE", _param._ctrlDate); //日付

        //合計の印字
        int line = 1;

        int gradeNum1 = 0;
        int gradeNum2 = 0;
        int gradeNum3 = 0;
        int gradeNum4 = 0;
        int gradeNum5 = 0;
        int gradeCnt = 0;

        for (Iterator iterator = gradeHrClassList.iterator(); iterator.hasNext();) {
            int totalNum1 = 0;
            int totalNum2 = 0;
            int totalNum3 = 0;
            int totalNum4 = 0;
            int totalNum5 = 0;

            int totalScore = 0;
            int totalScoreCount = 0;

            final GradeHrClass gradeHrClass = (GradeHrClass) iterator.next();
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();

                if (!_param._printSublassMap.containsKey(subclassMst._subclasscd)) {
                    continue;
                }

                final String subclassCd = subclassMst._subclasscd;
                final ScoreData scoreData = (ScoreData) gradeHrClass._printSubclassMap.get(subclassCd);
                if(scoreData == null) continue;
                final Map scaleMap = (Map)scoreData._5pointScaleMap.get(_param._sdiv);
                if(scaleMap == null) continue;

                //合計人数
                if(scaleMap.containsKey("1")) totalNum1 += Integer.parseInt((String)scaleMap.get("1"));
                if(scaleMap.containsKey("2")) totalNum2 += Integer.parseInt((String)scaleMap.get("2"));
                if(scaleMap.containsKey("3")) totalNum3 += Integer.parseInt((String)scaleMap.get("3"));
                if(scaleMap.containsKey("4")) totalNum4 += Integer.parseInt((String)scaleMap.get("4"));
                if(scaleMap.containsKey("5")) totalNum5 += Integer.parseInt((String)scaleMap.get("5"));

                //総合成績
                totalScore += Integer.parseInt((String)scoreData._scoreMap.get(_param._sdiv));
                totalScoreCount += Integer.parseInt((String)scoreData._scoreTotalCountMap.get(_param._sdiv));
            }
            //合計の人数、合計人数の比
            final int count = totalNum1 + totalNum2 + totalNum3 + totalNum4 + totalNum5;

            //学年合計カウント
            gradeNum1 += totalNum1;
            gradeNum2 += totalNum2;
            gradeNum3 += totalNum3;
            gradeNum4 += totalNum4;
            gradeNum5 += totalNum5;
            gradeCnt += count;

            if (totalNum1 > 0) {
                svf.VrsOutn("TOTAL_NUM5", line, String.valueOf(totalNum1));
                svf.VrsOutn("TOTAL_PER5", line, perCalculation(totalNum1, count, "%"));
            }
            if (totalNum2 > 0) {
                svf.VrsOutn("TOTAL_NUM4", line, String.valueOf(totalNum2));
                svf.VrsOutn("TOTAL_PER4", line, perCalculation(totalNum2, count, "%"));
            }
            if (totalNum3 > 0) {
                svf.VrsOutn("TOTAL_NUM3", line, String.valueOf(totalNum3));
                svf.VrsOutn("TOTAL_PER3", line, perCalculation(totalNum3, count, "%"));
            }
            if (totalNum4 > 0) {
                svf.VrsOutn("TOTAL_NUM2", line, String.valueOf(totalNum4));
                svf.VrsOutn("TOTAL_PER2", line, perCalculation(totalNum4, count, "%"));
            }
            if (totalNum5 > 0) {
                svf.VrsOutn("TOTAL_NUM1", line, String.valueOf(totalNum5));
                svf.VrsOutn("TOTAL_PER1", line, perCalculation(totalNum5, count, "%"));
            }

            //総合成績 クラス平均
            svf.VrsOut("TOTAL_AVE"+line, perCalculation(totalScore, totalScoreCount, ""));
            line++;
        }

        //学年合計
        if (gradeNum1 > 0) {
            svf.VrsOutn("TOTAL_NUM5", 15, String.valueOf(gradeNum1));
            svf.VrsOutn("TOTAL_PER5", 15, perCalculation(gradeNum1, gradeCnt, "%"));
        }
        if (gradeNum2 > 0) {
            svf.VrsOutn("TOTAL_NUM4", 15, String.valueOf(gradeNum2));
            svf.VrsOutn("TOTAL_PER4", 15, perCalculation(gradeNum2, gradeCnt, "%"));
        }
        if (gradeNum3 > 0) {
            svf.VrsOutn("TOTAL_NUM3", 15, String.valueOf(gradeNum3));
            svf.VrsOutn("TOTAL_PER3", 15, perCalculation(gradeNum3, gradeCnt, "%"));
        }
        if (gradeNum4 > 0) {
            svf.VrsOutn("TOTAL_NUM2", 15, String.valueOf(gradeNum4));
            svf.VrsOutn("TOTAL_PER2", 15, perCalculation(gradeNum4, gradeCnt, "%"));
        }
        if (gradeNum5 > 0) {
            svf.VrsOutn("TOTAL_NUM1", 15, String.valueOf(gradeNum5));
            svf.VrsOutn("TOTAL_PER1", 15, perCalculation(gradeNum5, gradeCnt, "%"));
        }
    }

    private String perCalculation(final int num, final int count, final String str) {
        int bai = "".equals(str) ? 1 : 100;
        if(count <= 0) return "";
        final double per = ((double)num / count) * bai;
        final String rtn = (!"".equals(String.valueOf(per))) ? sishaGonyu(String.valueOf(per),1)+str : "";
        return rtn;
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final SubclassMst subclassMst, final List gradeHrClassList, final int grp) {
        final String subclassCd = subclassMst._subclasscd;

        //クラス別評定の度数分布表
        int line = 1;
        int gradeNum1 = 0;
        int gradeNum2 = 0;
        int gradeNum3 = 0;
        int gradeNum4 = 0;
        int gradeNum5 = 0;
        for (Iterator iterator = gradeHrClassList.iterator(); iterator.hasNext();) {

            final GradeHrClass gradeHrClass = (GradeHrClass) iterator.next();
            if (gradeHrClass._printSubclassMap.containsKey(subclassCd)) {
                final ScoreData scoreData = (ScoreData) gradeHrClass._printSubclassMap.get(subclassCd);
                if(scoreData != null) {
                    final Map scaleMap = (Map)scoreData._5pointScaleMap.get(_param._sdiv);
                    if(scaleMap.containsKey("1")) {
                        svf.VrsOutn("DIV1_5", line, "0".equals((String)scaleMap.get("1"))? "" : (String)scaleMap.get("1"));
                        String tmp = (String)scaleMap.get("1");
                        gradeNum1 += Integer.parseInt(tmp);
                    }
                    if(scaleMap.containsKey("2")) {
                        svf.VrsOutn("DIV1_4", line, "0".equals((String)scaleMap.get("2"))? "" : (String)scaleMap.get("2"));
                        String tmp = (String)scaleMap.get("2");
                        gradeNum2 += Integer.parseInt(tmp);
                    }
                    if(scaleMap.containsKey("3")) {
                        svf.VrsOutn("DIV1_3", line, "0".equals((String)scaleMap.get("3"))? "" : (String)scaleMap.get("3"));
                        String tmp = (String)scaleMap.get("3");
                        gradeNum3 += Integer.parseInt(tmp);
                    }
                    if(scaleMap.containsKey("4")) {
                        svf.VrsOutn("DIV1_2", line, "0".equals((String)scaleMap.get("4"))? "" : (String)scaleMap.get("4"));
                        String tmp = (String)scaleMap.get("4");
                        gradeNum4 += Integer.parseInt(tmp);
                    }
                    if(scaleMap.containsKey("5")) {
                        svf.VrsOutn("DIV1_1", line, "0".equals((String)scaleMap.get("5"))? "" : (String)scaleMap.get("5"));
                        String tmp = (String)scaleMap.get("5");
                        gradeNum5 += Integer.parseInt(tmp);
                    }
                    _hasData = true;
                }
            }
            line++;
        }

        //学年
        svf.VrsOutn("DIV1_5", 15, "0".equals(String.valueOf(gradeNum1))? "" : String.valueOf(gradeNum1));
        svf.VrsOutn("DIV1_4", 15, "0".equals(String.valueOf(gradeNum2))? "" : String.valueOf(gradeNum2));
        svf.VrsOutn("DIV1_3", 15, "0".equals(String.valueOf(gradeNum3))? "" : String.valueOf(gradeNum3));
        svf.VrsOutn("DIV1_2", 15, "0".equals(String.valueOf(gradeNum4))? "" : String.valueOf(gradeNum4));
        svf.VrsOutn("DIV1_1", 15, "0".equals(String.valueOf(gradeNum5))? "" : String.valueOf(gradeNum5));
    }

    private void printTotal(final DB2UDB db2, final Vrw32alp svf, final SubclassMst subclassMst, final List gradeHrClassList, final int grp) {
        final String subclassCd = subclassMst._subclasscd;
        final Map resultMap = getScoreData2(db2, subclassCd, _param._grade);
        svf.VrsOut("DIV2_1", (String)resultMap.get("MAX_SCORE")); //最高点
        svf.VrsOut("DIV2_2", (String)resultMap.get("MIN_SCORE")); //最低点
        svf.VrsOut("DIV2_3", (String)resultMap.get("COUNT")); //人数
        svf.VrsOut("DIV2_4", (String)resultMap.get("AVG")); //平均点
        svf.VrsOut("DIV2_5", sishaGonyu((String)resultMap.get("STDDEV"),1)); //標準偏差

        //学年成績類型とクラス別平均
        int line = 1;
        for (Iterator iterator = gradeHrClassList.iterator(); iterator.hasNext();) {

            final GradeHrClass gradeHrClass = (GradeHrClass) iterator.next();
            final ScoreData scoreData = (ScoreData) gradeHrClass._printSubclassMap.get(subclassCd);
            if(scoreData != null) svf.VrsOut("AVE"+line, (String)scoreData._avgMap.get(_param._sdiv)); //クラス平均
            line++;
        }
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
                it.remove();
            }
        }
        return retList;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getGradeHrClassSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final GradeHrClass gradeHrClass = new GradeHrClass();
                gradeHrClass._schoolKind = rs.getString("SCHOOL_KIND");
                gradeHrClass._grade = rs.getString("GRADE");
                gradeHrClass._hrClass = rs.getString("HR_CLASS");
                gradeHrClass._hrName = rs.getString("HR_NAME");
//                final String hr_name = Integer.parseInt(rs.getString("GRADE_CD")) + "年" + rs.getString("HR_CLASS_NAME1") + "組" ;
                gradeHrClass.setSubclass(db2);
                retList.add(gradeHrClass);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getGradeHrClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT  REGDG.YEAR");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGDH.HR_CLASS ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("     FROM    SCHREG_REGD_GDAT REGDG ");
        stb.append("             INNER JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("                     ON REGDH.YEAR     = REGDG.YEAR ");
        stb.append("                    AND REGDH.GRADE    = REGDG.GRADE ");
        if("9".equals(_param._semester)) {
            stb.append("                    AND REGDH.SEMESTER = '" + _param._lastSemester + "' ");
        } else {
            stb.append("                    AND REGDH.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("     WHERE   REGDG.YEAR = '" + _param._year + "' ");
        stb.append("         AND REGDG.GRADE = '" + _param._grade + "' ");
        stb.append("     ORDER BY ");
        stb.append("         REGDG.GRADE, ");
        stb.append("         REGDH.HR_CLASS ");
        return stb.toString();
    }

    private static String sishaGonyu(final String val, final int keta) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(keta, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class GradeHrClass {
        String _year;
        String _schoolKind;
        String _grade;
        String _hrClass;
        String _hrName;
        String _attendno;
        String _schregno;
        final Map _printSubclassMap = new TreeMap();
        private void setSubclass(final DB2UDB db2) {
            final String sql = prestatementSubclass();
            if (_param._isOutputDebug) {
                log.info(" prestatementSubclass = " + sql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname));
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._scoreTotalCountMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE_TOTAL_COUNT"),"0"));
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE_TOTAL"),"0"));
                    scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE_AVG")));
                    //5段階の振り分け
                    final Map map = new TreeMap();
                    if(!"".equals(StringUtils.defaultString(rs.getString("SCORE_COUNT1")))) map.put("1", rs.getString("SCORE_COUNT1"));
                    if(!"".equals(StringUtils.defaultString(rs.getString("SCORE_COUNT2")))) map.put("2", rs.getString("SCORE_COUNT2"));
                    if(!"".equals(StringUtils.defaultString(rs.getString("SCORE_COUNT3")))) map.put("3", rs.getString("SCORE_COUNT3"));
                    if(!"".equals(StringUtils.defaultString(rs.getString("SCORE_COUNT4")))) map.put("4", rs.getString("SCORE_COUNT4"));
                    if(!"".equals(StringUtils.defaultString(rs.getString("SCORE_COUNT5")))) map.put("5", rs.getString("SCORE_COUNT5"));
                    scoreData._5pointScaleMap.put(testcd, map); //5段階

                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SCHNO AS( ");
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T2.COURSECD, ");
            stb.append("     T2.MAJORCD, ");
            stb.append("     T2.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("         T2.YEAR     = '"+ _param._year +"' ");
            stb.append("     AND T2.GRADE    = '"+ _grade +"' ");
            stb.append("     AND T2.HR_CLASS = '"+ _hrClass +"' ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '"+ _param._year +"'  ");
            stb.append("                          AND W2.SEMESTER <= '"+ _param._semester +"'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
            stb.append(" ) ,RECORD00_BASE AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("          INNER JOIN (SELECT ");
            stb.append("                       SCHREGNO ");
            stb.append("                     FROM ");
            stb.append("                       SCHNO T2 ");
            stb.append("                     GROUP BY ");
            stb.append("                       SCHREGNO ");
            stb.append("                  ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '"+ _param._year +"'  ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '"+ _param._sdiv +"' ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('"+ ALL3 +"', '"+ ALL5 +"') ");
            stb.append(" UNION ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("          INNER JOIN (SELECT ");
            stb.append("                       SCHREGNO ");
            stb.append("                     FROM ");
            stb.append("                       SCHNO T2 ");
            stb.append("                     GROUP BY ");
            stb.append("                       SCHREGNO ");
            stb.append("                  ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '"+ _param._year +"'  ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '"+ _param._sdiv +"' ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND (T1.SCORE IS NOT NULL OR T1.VALUE_DI IS NOT NULL) ");
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD00_BASE T1 ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            stb.append("            , COUNT(L2.SCORE) AS SCORE_TOTAL_COUNT");
            stb.append("            , COUNT(CASE WHEN L3.SCORE = '1' THEN 1 ELSE NULL END) AS SCORE_COUNT1");
            stb.append("            , COUNT(CASE WHEN L3.SCORE = '2' THEN 1 ELSE NULL END) AS SCORE_COUNT2");
            stb.append("            , COUNT(CASE WHEN L3.SCORE = '3' THEN 1 ELSE NULL END) AS SCORE_COUNT3");
            stb.append("            , COUNT(CASE WHEN L3.SCORE = '4' THEN 1 ELSE NULL END) AS SCORE_COUNT4");
            stb.append("            , COUNT(CASE WHEN L3.SCORE = '5' THEN 1 ELSE NULL END) AS SCORE_COUNT5");
            stb.append("            , SUM(VALUE(L2.SCORE, 0)) AS SCORE_TOTAL ");
            //stb.append("            , DECIMAL(ROUND(AVG(CAST(L2.SCORE AS double)),2),5,1) AS SCORE_AVG ");
            stb.append("            , DECIMAL(((SUM(VALUE(L2.SCORE, 0)) * 1.0 / COUNT(L2.SCORE) * 1.0) * 10.0 + 0.5) / 10.0 , 5, 1) AS SCORE_AVG ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("          LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("                 ON L2.YEAR          = T1.YEAR ");
            stb.append("                AND L2.SEMESTER      = T1.SEMESTER ");
            stb.append("                AND L2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("                AND L2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("                AND L2.SCORE_DIV     = '08' ");
            stb.append("                AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("                AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("                AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("                AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("          LEFT JOIN RECORD_RANK_SDIV_DAT L3 ");
            stb.append("                 ON L3.YEAR          = T1.YEAR ");
            stb.append("                AND L3.SEMESTER      = T1.SEMESTER ");
            stb.append("                AND L3.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("                AND L3.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("                AND L3.SCORE_DIV     = '09' ");
            stb.append("                AND L3.CLASSCD       = T1.CLASSCD ");
            stb.append("                AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("                AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("                AND L3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("      FROM RECORD0 T1 ");
            stb.append("      LEFT JOIN ( ");
            stb.append("            SELECT T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("              FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("             WHERE T1.YEAR = '"+ _param._year +"' ");
            stb.append("               AND T1.SUBCLASSCD = '"+ ALL9 +"' ");
            stb.append("             GROUP BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV  ");
            stb.append("      ) T2 ON T2.YEAR       = T1.YEAR ");
            stb.append("          AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND T2.SCORE_DIV  = T1.SCORE_DIV ");
            stb.append("      WHERE T1.SUBCLASSCD = '"+ ALL9 +"' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");
            stb.append("    ,T1.SCHOOL_KIND ");
            stb.append("    ,T1.CURRICULUM_CD ");

            return stb.toString();
        }

    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final Map _scoreMap = new HashMap(); // クラスの合計点
        final Map _scoreTotalCountMap = new HashMap(); // クラスの合計点
        final Map _avgMap = new HashMap(); // クラスの平均点
        final Map _5pointScaleMap = new HashMap(); //5段階

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String scoreTotalCount(final String sdiv) {
            return StringUtils.defaultString((String) _scoreTotalCountMap.get(sdiv), "");
        }

        public String avg(final String sdiv) {
            return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
        }
    }


    //組番号順を選択した場合のみ表示される、"クラス合計", "クラス平均", "科目平均"
    private Map getScoreData2 (final DB2UDB db2, final String subclassCd, final String grade) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHNO AS( ");
        stb.append(" SELECT ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.COURSECD, ");
        stb.append("     T2.MAJORCD, ");
        stb.append("     T2.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("         T2.YEAR     = '"+ _param._year +"' ");
        stb.append("     AND T2.GRADE    = '"+ grade +"' ");
        stb.append("     AND T2.SEMESTER = (SELECT ");
        stb.append("                          MAX(SEMESTER) ");
        stb.append("                        FROM ");
        stb.append("                          SCHREG_REGD_DAT W2 ");
        stb.append("                        WHERE ");
        stb.append("                          W2.YEAR = '"+ _param._year +"'  ");
        stb.append("                          AND W2.SEMESTER <= '"+ _param._semester +"'  ");
        stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
        stb.append("                     ) ");
        stb.append(" ) ,RECORD00_BASE AS( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
        stb.append("          INNER JOIN (SELECT ");
        stb.append("                       SCHREGNO ");
        stb.append("                     FROM ");
        stb.append("                       SCHNO T2 ");
        stb.append("                     GROUP BY ");
        stb.append("                       SCHREGNO ");
        stb.append("                  ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+ _param._year +"'  ");
        stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '"+ _param._sdiv +"' ");
        if(ALL9.equals(subclassCd)) {
            stb.append("     AND T1.SUBCLASSCD = '"+ subclassCd +"' ");
        } else {
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '"+ subclassCd +"' ");
        }
        stb.append(" UNION ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("     FROM RECORD_SCORE_DAT T1 ");
        stb.append("          INNER JOIN (SELECT ");
        stb.append("                       SCHREGNO ");
        stb.append("                     FROM ");
        stb.append("                       SCHNO T2 ");
        stb.append("                     GROUP BY ");
        stb.append("                       SCHREGNO ");
        stb.append("                  ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+ _param._year +"'  ");
        stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '"+ _param._sdiv +"' ");
        if(ALL9.equals(subclassCd)) {
            stb.append("     AND T1.SUBCLASSCD = '"+ subclassCd +"' ");
        } else {
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '"+ subclassCd +"' ");
        }
        stb.append("     AND (T1.SCORE IS NOT NULL OR T1.VALUE_DI IS NOT NULL) ");
        stb.append(" ) ,RECORD00 AS( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append("     FROM RECORD00_BASE T1 ");
        stb.append(" ) ,RECORD AS( ");
        stb.append("     SELECT ");
        stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
//        stb.append("            , CASE WHEN MAX(L3.VALUE_DI) IS NOT NULL THEN MAX(L3.VALUE_DI) ELSE CAST(MAX(L2.SCORE) AS VARCHAR(3)) END AS MAX_SCORE ");
        stb.append("            , CAST(MAX(L2.SCORE) AS VARCHAR(3)) AS MAX_SCORE ");
//        stb.append("            , CASE WHEN MAX(L3.VALUE_DI) IS NOT NULL THEN MIN(L3.VALUE_DI) ELSE CAST(MIN(L2.SCORE) AS VARCHAR(3)) END AS MIN_SCORE ");
        stb.append("            , CAST(MIN(L2.SCORE) AS VARCHAR(3)) AS MIN_SCORE ");
        stb.append("            , COUNT(L2.SCORE) AS COUNT ");
//        stb.append("            , DECIMAL(ROUND(AVG(CAST(L2.SCORE AS double)),2),5,1) AS AVG ");
        stb.append("            , DECIMAL(((SUM(VALUE(L2.SCORE, 0)) * 1.0 / count(L2.SCORE) * 1.0) * 10.0 + 0.5) / 10.0, 5, 1) AS AVG ");
        stb.append("     FROM RECORD00 T1 ");
        stb.append("          LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
        stb.append("                 ON L2.YEAR          = T1.YEAR ");
        stb.append("                AND L2.SEMESTER      = T1.SEMESTER ");
        stb.append("                AND L2.TESTKINDCD    = T1.TESTKINDCD ");
        stb.append("                AND L2.TESTITEMCD    = T1.TESTITEMCD ");
        stb.append("                AND L2.SCORE_DIV     = T1.SCORE_DIV ");
        stb.append("                AND L2.CLASSCD       = T1.CLASSCD ");
        stb.append("                AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("                AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("                AND L2.SCHREGNO      = T1.SCHREGNO ");
        stb.append("          LEFT JOIN RECORD_SCORE_DAT L3 ");
        stb.append("                 ON L3.YEAR          = T1.YEAR ");
        stb.append("                AND L3.SEMESTER      = T1.SEMESTER ");
        stb.append("                AND L3.TESTKINDCD    = T1.TESTKINDCD ");
        stb.append("                AND L3.TESTITEMCD    = T1.TESTITEMCD ");
        stb.append("                AND L3.SCORE_DIV     = T1.SCORE_DIV ");
        stb.append("                AND L3.CLASSCD       = T1.CLASSCD ");
        stb.append("                AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("                AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("                AND L3.SCHREGNO      = T1.SCHREGNO ");
        stb.append("     WHERE L3.VALUE_DI IS NULL ");
        stb.append("     GROUP BY ");
        stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        stb.append(" ) ");
        stb.append("     SELECT ");
        stb.append("              T1.*, ");
        stb.append("               DECIMAL(((L2.STDDEV * 10.0 + 0.5) / 10.0), 5, 1) AS STDDEV ");
        stb.append("     FROM RECORD T1 ");
        stb.append("          LEFT JOIN RECORD_AVERAGE_SDIV_DAT L2 ");
        stb.append("                 ON L2.YEAR          = T1.YEAR ");
        stb.append("                AND L2.SEMESTER      = T1.SEMESTER ");
        stb.append("                AND L2.TESTKINDCD    = T1.TESTKINDCD ");
        stb.append("                AND L2.TESTITEMCD    = T1.TESTITEMCD ");
        stb.append("                AND L2.SCORE_DIV     = T1.SCORE_DIV ");
        stb.append("                AND L2.CLASSCD       = T1.CLASSCD ");
        stb.append("                AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("                AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("                AND L2.AVG_DIV       = '1' ");
        stb.append("                AND L2.GRADE         = '"+ grade +"' ");
        stb.append("                AND L2.HR_CLASS      = '000' ");
        stb.append("                AND L2.COURSECD || L2.MAJORCD || L2.COURSECODE = '00000000' ");
        final String sql =  stb.toString();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Map resultMap = new HashMap() ;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                resultMap.put("MAX_SCORE", StringUtils.defaultString(rs.getString("MAX_SCORE"),"0"));
                resultMap.put("MIN_SCORE", StringUtils.defaultString(rs.getString("MIN_SCORE"),"0"));
                resultMap.put("COUNT", StringUtils.defaultString(rs.getString("COUNT"),"0"));
                resultMap.put("AVG", StringUtils.defaultString(rs.getString("AVG"),"0"));
                resultMap.put("STDDEV", StringUtils.defaultString(rs.getString("STDDEV"),"0"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return resultMap;
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _schoolKind;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(
                final String specialDiv,
                final String classcd,
                final String schoolKind,
                final String subclasscd,
                final String classabbv,
                final String classname,
                final String subclassabbv,
                final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75528 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _testcd;
        final String _scoreFlg;
        final String _subTestcd;
        final String _ctrlDate;
        final String _prgid;
        final String _printLogStaffcd;
        final String _lastSemester;

        final String _nendo;
        final String _semesterName;
        final String _schoolKind;
        final String _sdiv;
        final String _testkindName;
        final String _gradeName;
        final String _printLogStaffName;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        private final Map _testItemMap;
        private final Map _printSublassMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        private Map _subclassMstMap;
        private List _d026List = new ArrayList();

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _scoreFlg = request.getParameter("SCORE_FLG");
            _subTestcd = request.getParameter("SUB_TESTCD");
            _ctrlDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _lastSemester = getLastSemester(db2);

            _nendo = _year + "年度";
            _semesterName = getSemesterName(db2);
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _sdiv = _semester + _subTestcd;
            _testkindName = getTestitemName(db2);
            _gradeName = getGradeName(db2);
            _printLogStaffName = getStaffName(db2);

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            _testItemMap = settestItemMap(db2);
            _printSublassMap = setPrintSubClassMap(db2);

            setCertifSchoolDat(db2);
            setSubclassMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new LinkedMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.SCHOOL_KIND, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTERNAME ");
                sql.append(" FROM SEMESTER_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SEMESTER = '"+ _semester +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getTestitemName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '"+ _sdiv +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT GRADE_NAME2 ");
                sql.append(" FROM SCHREG_REGD_GDAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND GRADE = '"+ _grade +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getStaffName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT STAFFNAME ");
                sql.append(" FROM STAFF_MST ");
                sql.append(" WHERE STAFFCD = '" + _printLogStaffcd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("STAFFNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private Map settestItemMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTITEM, TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _year + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String testitem = rs.getString("TESTITEM");
                    final String testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                    if (!map.containsKey(testitem)) {
                        map.put(testitem, testitemname);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private Map setPrintSubClassMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" WITH SUBCLASS_GET_DAT AS ( ");
                sql.append(" SELECT ");
                sql.append("   SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD AS SUBCLASSCD, ");
                sql.append("   T2.SUBCLASSABBV ");
                sql.append(" FROM ");
                sql.append("   SCHREG_REGD_DAT REGD ");
                sql.append("   INNER JOIN RECORD_SCORE_DAT SDIV ");
                sql.append("           ON SDIV.YEAR     = REGD.YEAR ");
                sql.append("          AND SDIV.SCHREGNO = REGD.SCHREGNO ");
                sql.append("          AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _sdiv + "' ");
                sql.append("          AND SDIV.SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
                sql.append("   LEFT JOIN SUBCLASS_MST T2 ");
                sql.append("          ON T2.CLASSCD       = SDIV.CLASSCD ");
                sql.append("         AND T2.SCHOOL_KIND   = SDIV.SCHOOL_KIND ");
                sql.append("         AND T2.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
                sql.append("         AND T2.SUBCLASSCD    = SDIV.SUBCLASSCD ");
                sql.append(" WHERE ");
                sql.append("   REGD.YEAR = '" + _year + "' ");
                sql.append("   AND REGD.GRADE      = '" + _grade + "' ");
                sql.append("   AND REGD.SEMESTER   = '" + _semester + "' ");
                sql.append(" UNION ");
                sql.append(" SELECT ");
                sql.append("   SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD AS SUBCLASSCD, ");
                sql.append("   T2.SUBCLASSABBV ");
                sql.append(" FROM ");
                sql.append("   SCHREG_REGD_DAT REGD ");
                sql.append("   INNER JOIN RECORD_RANK_SDIV_DAT SDIV ");
                sql.append("           ON SDIV.YEAR     = REGD.YEAR ");
                sql.append("          AND SDIV.SCHREGNO = REGD.SCHREGNO ");
                sql.append("          AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _sdiv + "' ");
                sql.append("          AND SDIV.SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
                sql.append("   LEFT JOIN SUBCLASS_MST T2 ");
                sql.append("          ON T2.CLASSCD       = SDIV.CLASSCD ");
                sql.append("         AND T2.SCHOOL_KIND   = SDIV.SCHOOL_KIND ");
                sql.append("         AND T2.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
                sql.append("         AND T2.SUBCLASSCD    = SDIV.SUBCLASSCD ");
                sql.append(" WHERE ");
                sql.append("   REGD.YEAR = '" + _year + "' ");
                sql.append("   AND REGD.GRADE      = '" + _grade + "' ");
                if("9".equals(_semester)) {
                    sql.append("   AND REGD.SEMESTER   = '" + _lastSemester + "' ");
                } else {
                    sql.append("   AND REGD.SEMESTER   = '" + _semester + "' ");
                }
                sql.append(" ) ");
                sql.append(" SELECT DISTINCT SUBCLASSCD, SUBCLASSABBV FROM SUBCLASS_GET_DAT ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final String subClassName = StringUtils.defaultString(rs.getString("SUBCLASSABBV"));
                    if (!map.containsKey(subClassCd)) {
                        map.put(subClassCd, subClassName);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
           return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private String getLastSemester(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT MAX(SEMESTER) FROM V_SEMESTER_GRADE_MST WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "' AND SEMESTER <> '9' "));

        }
    }
}

// eof
