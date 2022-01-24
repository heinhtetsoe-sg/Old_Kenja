/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2020/03/04
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD626A {

    private static final Log log = LogFactory.getLog(KNJD626A.class);

    /** 3教科科目コード */
    private static final String ALL3 = "333333";
    /** 5教科科目コード */
    private static final String ALL5 = "555555";
    /** 7教科科目コード */
    private static final String ALL7 = "777777";
    /** 9教科科目コード */
    private static final String ALL9 = "999999";
    /** 特殊科目コードA */
    private static final String ALL9A = "99999A";
    /** 特殊科目コードB */
    private static final String ALL9B = "99999B";
    /** 教科評定平均検索用コード */
    private static final String ALLZ = "ZZZZZZ";

    private static final String SEMEALL = "9";

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
            for (Iterator ite = _param._psHandleMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                PreparedStatement endPs = (PreparedStatement)_param._psHandleMap.get(kStr);
                if (endPs != null) {
                    DbUtils.closeQuietly(endPs);
                }
            }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String form = "KNJD626A.frm";
        if (SEMEALL.equals(_param._semester)) {
            form = "KNJD626A_2.frm";
        }

        final Map gh_Map = getGHMapInfo(db2);
        for (Iterator ite = gh_Map.keySet().iterator(); ite.hasNext();) {
            final String getKey = (String)ite.next();
            final GradeHrCls hrClSObj = (GradeHrCls)gh_Map.get(getKey);
            log.info("schregMap = " +hrClSObj._schregMap.keySet());

            //一度、表以外の項目を埋めるために、ページ内出力生徒でループ
            int prtCnt = 0;
            for (Iterator its = hrClSObj._schregMap.keySet().iterator();its.hasNext();) {
                svf.VrSetForm(form, 4);
                final String getSubKey = (String)its.next();
                final Student student = (Student)hrClSObj._schregMap.get(getSubKey);

                //タイトルなどのヘッダ部
                setExceptSpread(db2, svf, hrClSObj, student);
                prtCnt++;
                totalCalc sumwk = new totalCalc();

                //生徒氏名
                final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2": "1";
                svf.VrsOutn("NAME" + nfield, prtCnt, student._name);

                //出欠
                int semesVal = 1;
                AttData totalCnt = new AttData("0", "0", 0,0,0,0,0,0,0); //学期合計用
                AttData prtZero = new AttData("0", "0", 0,0,0,0,0,0,0); //データなし出力用
                for (int month = 1;month <= 12;month++) {
                    if ("1".equals(_param._semester)) {
                        //7月超えたらcontinue
                        if (month > 4) continue;
                    } else if ("2".equals(_param._semester)) {
                        //12月超えたらcontinue
                        if (month > 9) continue;
                    }
                    //final int r_month = month <= 9 ? month + 3 : month - 9;
                    if (student._attendMonthMap.containsKey(String.valueOf(month))) {
                        AttData outwk = (AttData)student._attendMonthMap.get(String.valueOf(month));
                        printAttendRange(svf, "ATTEND", String.valueOf(month), outwk);
                        totalCnt.add(outwk);
                    } else {
                        //指定学期範囲内なら、指定学期になるまで0で埋める
                        printAttendRange(svf, "ATTEND", String.valueOf(month), prtZero);
                    }
                    if (month == 4 || month == 9 || month == 12) {  //7/12/3月なら学期合計出力
                        //学期合計を出力
                        printAttendRange(svf, "ATTEND_TOTAL", String.valueOf(semesVal), totalCnt);
                        semesVal++;
                        totalCnt.clr();
                    }
                }
                for (int gradeRow = 1;gradeRow <= 3;gradeRow++) {
                    if (student._attendGradeMap.containsKey("0" + String.valueOf(gradeRow))) {
                        AttData outwk = (AttData)student._attendGradeMap.get("0" + String.valueOf(gradeRow));
                        printAttendRange(svf, "ATTEND_GRADE_TOTAL", String.valueOf(gradeRow), outwk);
                    }
                }
                //総合的な学習の時間
                if (student._totalStudyAct != null && !"".equals(student._totalStudyAct)) {
                    final String[] tsaStr = KNJ_EditEdit.get_token(student._totalStudyAct, 86, 2);
                    for (int idx = 1;idx <= tsaStr.length;idx++) {
                        svf.VrsOutn("TOTAL_ACT1", idx, tsaStr[idx - 1]);
                    }
                }
                if (student._totalStudyTime != null && !"".equals(student._totalStudyTime)) {
                    final String[] tstStr = KNJ_EditEdit.get_token(student._totalStudyTime, 86, 2);
                    for (int idx = 1;idx <= tstStr.length;idx++) {
                        svf.VrsOutn("TOTAL_ACT2", idx, tstStr[idx - 1]);
                    }
                }
                log.info(student._attendNo + " totalStudyTime = " + student._totalStudyTime + " totalStudyAct = " + student._totalStudyAct);
                if (student._remark1 != null && !"".equals(student._remark1)) {
                    final String[] cmnStr = KNJ_EditEdit.get_token(student._remark1, Integer.parseInt(_param._use_remark1_moji) * 2, Integer.parseInt(_param._use_remark1_gyou));
                    for (int idx = 1;idx <= cmnStr.length;idx++) {
                        svf.VrsOutn("TOTAL_ACT3", idx, cmnStr[idx - 1]);
                    }
                }

                //総合/順位(評価)
                if (student._tounendo_subclassScoreMap.size() > 0) {
                    int scoreIdxCnt = 0;
                    for (Iterator ittest = _param._testCdList.iterator();ittest.hasNext();) {
                        final String seme_testcd = (String)ittest.next();
                        final String scoreKStr = seme_testcd + "-" + "99-H-99-999999";
                        scoreIdxCnt++;
                        if (student._tounendo_subclassScoreMap.containsKey(scoreKStr)) {
                            final SubclassRank prtwk = (SubclassRank)student._tounendo_subclassScoreMap.get(scoreKStr);
                            svf.VrsOut("TOTAL_SCORE1_"+scoreIdxCnt, prtwk._score);
                            svf.VrsOut("HR_RANK1_"+scoreIdxCnt, prtwk._clsAvg_Rank);
                            svf.VrsOut("GRADE_RANK1_"+scoreIdxCnt, prtwk._grdAvg_Rank);
                        }
                    }
                }

                //評定平均列の算出
                Iterator itMidTbl = student._subclsInfoMap.keySet().iterator(); //上表の科目Iterator
                Map midSubMap = null;
                Iterator itMidSub = null;
                String midKstr = "";
                final int maxMidColCnt = 55;
                int prtMidCnt = 0;
                Iterator itUpTbl = student._tounendo_subclsInfoMap.keySet().iterator();  //中表の科目Iterator
                while (itMidTbl.hasNext()) {
                        //中段の表の処理
                        if (itMidTbl.hasNext()) {
                            int midColCnt = 0;
                            for (midColCnt = prtMidCnt;midColCnt < Math.min(maxMidColCnt, student._subclsInfoMapCnt - prtMidCnt);midColCnt++) {  //1ページの出力列数をここで制御
                            if (itMidSub != null && !itMidSub.hasNext()) {
                                midSubMap = null;
                            }
                            if (!itMidTbl.hasNext() && !itMidSub.hasNext()) break;
                            if (itMidTbl.hasNext() && (midSubMap == null || !itMidSub.hasNext())) {
                                    //student._subclsInfoMapから次のマップを取得
                                    midKstr = (String)itMidTbl.next();
                                    midSubMap = (Map) student.getFilteredSubClsMap(midKstr);//._subclsInfoMap.get(midKstr);
                                    itMidSub = midSubMap.keySet().iterator();
                                }
                                if (itMidSub != null && !itMidSub.hasNext()) continue;
                                final String midSubKstr = (String)itMidSub.next();

                                //当年度分から過年度分までを出力。
                                List keysetMidList = Arrays.asList(_param._gradeMap.keySet().toArray());
                                if (keysetMidList.contains(hrClSObj._gradeCd)) {
                                    int baseidx = keysetMidList.indexOf(hrClSObj._gradeCd);
                                    for (int cnt = baseidx;cnt >= 0;cnt --) {
                                        final String gradeCd = (String)keysetMidList.get(cnt);
                                        final String grade = (String)_param._gradeMap.get(gradeCd);

                                           final String sKey1 = grade + "-" + midSubKstr;
                                           if (student._subclassValMap.containsKey(sKey1)) {
                                               SubclassValuation prtwk1 = (SubclassValuation)student._subclassValMap.get(sKey1);
                                               final int classcd = Integer.parseInt(midKstr.substring(0,2));
                                               if (90 > classcd) {
                                                   //90以下の教科が対象。但し、90は対象外とする。
                                                   if (prtwk1._score != null) {
                                                       BigDecimal totalAvg = new BigDecimal(prtwk1._score);
                                                       if(student._totalAvgMap.containsKey(grade)) {
                                                           totalAvg = totalAvg.add((BigDecimal)student._totalAvgMap.get(grade));
                                                       }
                                                       student._totalAvgMap.put(grade, totalAvg);  //合計

                                                       int totalAvgCount = 1;
                                                       if(student._totalAvgCountMap.containsKey(grade)) {
                                                           totalAvgCount += ((Integer)student._totalAvgCountMap.get(grade)).intValue();
                                                       }
                                                       student._totalAvgCountMap.put(grade, new Integer(totalAvgCount)); //数
                                                   }
                                               }
                                           }
                                       }
                                   }
                            }
                            prtMidCnt += midColCnt;
                        }
                    }


                //総合/順位(評定)
                //当年度分から過年度分までを出力。
                List keysetList = Arrays.asList(_param._gradeMap.keySet().toArray());
                if (keysetList.contains(hrClSObj._gradeCd)) {
                    int baseidx = keysetList.indexOf(hrClSObj._gradeCd);
                    for (int cnt = baseidx;cnt >= 0;cnt --) {
                        final String gradeCd = (String)keysetList.get(cnt);
                        final String grdIdx = String.valueOf(Integer.parseInt(gradeCd));
                        final String grade = (String)_param._gradeMap.get(gradeCd);

                        final String getSubclsRMapKey = grade + "-99-" + hrClSObj._schoolKind + "-99-" + ALL9;
                        if (student._subclassRankMap.containsKey(getSubclsRMapKey)) {
                            SubclassRank prtwk = (SubclassRank)student._subclassRankMap.get(getSubclsRMapKey);

                            svf.VrsOut("TOTAL_AVE" + grdIdx, (new BigDecimal(prtwk._avg).setScale(0,BigDecimal.ROUND_HALF_UP).toString())); //単位一総成
                            svf.VrsOut("TOTAL_CREDIT_RANK" + grdIdx, prtwk._grdAvg_Rank); //学年順位
                        }
                        if(student._totalAvgMap.containsKey(grade)) {
                            final BigDecimal totalAvg = (BigDecimal)student._totalAvgMap.get(grade);
                            if(student._totalAvgCountMap.containsKey(grade)) {
                                final int totalAvgCount = ((Integer)student._totalAvgCountMap.get(grade)).intValue();
                                final BigDecimal totalDiv = totalAvgCount == 0 ? new BigDecimal(0.0) : totalAvg.divide(new BigDecimal(totalAvgCount), BigDecimal.ROUND_HALF_UP);
                                svf.VrsOut("TOTAL_DIV_AVE" + grdIdx, String.valueOf((totalDiv).setScale(1, BigDecimal.ROUND_HALF_UP))); //評定平均
                            }
                        }
                    }
                }

                int grd1CrdCnt = 0;
                int grd2CrdCnt = 0;
                int grd3CrdCnt = 0;
                for (Iterator itr = student._subclsInfoMap.keySet().iterator();itr.hasNext();) {
                    final String key1 = (String)itr.next();  //classcd - schkind
                    final int classcd = Integer.parseInt(midKstr.substring(0,2));
                     if (90 >= classcd) {
                        //90以下の教科が対象。
                         final Map subMap = (Map)student.getFilteredSubClsMap(key1); //._subclsInfoMap.get(key1);
                         for (Iterator itCntSub = subMap.keySet().iterator();itCntSub.hasNext();) {
                             final String key2 = (String)itCntSub.next();
                             final SubclsInfo si = (SubclsInfo)subMap.get(key2);
                             if (si._credits1 != null && !"".equals(si._credits1)) {
                                 grd1CrdCnt += Integer.parseInt(si._credits1);
                             }
                             if (si._credits2 != null && !"".equals(si._credits2)) {
                                 grd2CrdCnt += Integer.parseInt(si._credits2);
                             }
                             if (si._credits3 != null && !"".equals(si._credits3)) {
                                 grd3CrdCnt += Integer.parseInt(si._credits3);
                             }
                         }
                     }
                }
                //単位合計
                svf.VrsOut("TOTAL_CREDIT1", grd1CrdCnt == 0 ? "" : String.valueOf(grd1CrdCnt));
                svf.VrsOut("TOTAL_CREDIT2", grd2CrdCnt == 0 ? "" : String.valueOf(grd2CrdCnt));
                svf.VrsOut("TOTAL_CREDIT3", grd3CrdCnt == 0 ? "" : String.valueOf(grd3CrdCnt));
                svf.VrsOut("TOTAL_CREDIT4", String.valueOf(grd1CrdCnt + grd2CrdCnt + grd3CrdCnt));

                //備考 評定平均
                final String key = "99" + "-" + "99-" + _param._schoolKind + "-99-" + ALLZ;
                BigDecimal taSum = new BigDecimal(0.0);
                Integer tcSum = new Integer(0);
                for (Iterator itg = student._totalAvgMap.keySet().iterator();itg.hasNext();) {
                    final String grade = (String)itg.next();
                    final BigDecimal totalAvg = (BigDecimal)student._totalAvgMap.get(grade);
                       taSum = taSum.add(totalAvg);

                }
                for (Iterator itgc = student._totalAvgCountMap.keySet().iterator();itgc.hasNext();) {
                    final String gcGrade = (String)itgc.next();
                    final Integer totalAvgCount = (Integer)student._totalAvgCountMap.get(gcGrade);
                    tcSum = new Integer(tcSum.intValue() + totalAvgCount.intValue());
                }
                if(student._totalAvgMap.size() > 0) {
                    if (tcSum.intValue() > 0) {
                        final String prtMark = _param.findAssessMst(taSum.divide(new BigDecimal(tcSum.intValue()), BigDecimal.ROUND_HALF_UP));
                        svf.VrsOut("TOTAL_AVE_RANK",  prtMark + "段階");
                        svf.VrsOut("TOTAL_AVE", taSum.divide(new BigDecimal(tcSum.intValue()), BigDecimal.ROUND_HALF_UP).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    }
                }

                //表の出力
                //2つの表に出力するデータを1件数づつ取得し、出力していく。
                ////具体的には、表毎に分けた2群のIteratorを管理して、取得->出力->次レコード(EndRecord)を制御する。

                itMidTbl = student._subclsInfoMap.keySet().iterator(); //上表の科目Iterator
                midSubMap = null;
                itMidSub = null;
                boolean chkMidFstFlg = false;
                List prtMidClsNameIdxList = null;
                int prtMidClsAbbvIdx = 0;
                int clsnameMidPutStrtPt = 0;
                midKstr = "";
                prtMidCnt = 0;

                itUpTbl = student._tounendo_subclsInfoMap.keySet().iterator();  //中表の科目Iterator
                Map upSubMap = null;
                Iterator itUpSub = null;
                boolean chkUpFstFlg = false;
                List prtUpClsNameIdxList = null;
                int prtUpClsAbbvIdx = 0;
                int clsnameUpPutStrtPt = 0;
                String upKstr = "";
                final int maxUpColCnt = 35;
                int prtUpCnt = 0;

                while (itMidTbl.hasNext() || itUpTbl.hasNext()) {
                //for (int lpCnt = 0;lpCnt < Math.max(hrClSObj._subclsInfoMapCnt, student._tounendo_subclsInfoMapCnt);lpCnt++) {
                    //上段の処理
                    int upColCnt = 0;
                    if (itUpTbl.hasNext()) {

                        // 総合成績　平均の平均算出用
                        int sougouClsCnt = 0;
                        int sougouGrdCnt = 0;
                        BigDecimal sougouCls =  new BigDecimal(0.0);
                        BigDecimal sougouGrd =  new BigDecimal(0.0);

                        for (upColCnt = prtUpCnt;upColCnt < Math.min(maxUpColCnt, student._tounendo_subclsInfoMapCnt - prtUpCnt);upColCnt++) {  //1ページの出力列数をここで制御
                            if (upSubMap == null || !itUpSub.hasNext()) {
                                //student._tounendo_subclsInfoMapから次のマップを取得
                                upKstr = (String)itUpTbl.next();
                                upSubMap = (Map) student._tounendo_subclsInfoMap.get(upKstr);
                                itUpSub = upSubMap.keySet().iterator();
                                chkUpFstFlg = false; //サブマップ(教科単位のデータマップ)を取得するタイミングでクリア
                                prtUpClsAbbvIdx = 0;
                                clsnameUpPutStrtPt = upColCnt;
                            }
                            final String upSubKstr = (String)itUpSub.next();
                            final SubclsInfo si = (SubclsInfo)upSubMap.get(upSubKstr);
                            if (!chkUpFstFlg) {
                                if(si._classAbbv == null) log.info("教科略称がnull");
                                prtUpClsNameIdxList = getClsNPrtIdx(si._classAbbv, upSubMap.size() * 2);
                                chkUpFstFlg = true;
                            }
                            //教科名
                            if (prtUpClsAbbvIdx <= si._classAbbv.length() - 1) {
                                if (prtUpClsNameIdxList == null) {
                                    if (si._classAbbv != null) {
                                           svf.VrsOut("CLASS_NAME1_1", StringUtils.defaultString(si._classAbbv, ""));
                                           prtUpClsAbbvIdx++;
                                    }
                                } else {
                                    final int prtpoint = (upColCnt * 2) - (clsnameUpPutStrtPt * 2) + 1;
                                    if (prtUpClsNameIdxList.contains(String.valueOf(prtpoint)) || prtUpClsNameIdxList.contains(String.valueOf(prtpoint+1))) {
                                        if ( (Integer.parseInt((String)prtUpClsNameIdxList.get(0)) == prtpoint
                                              || Integer.parseInt((String)prtUpClsNameIdxList.get(0)) == prtpoint + 1
                                              )&& Integer.parseInt((String)prtUpClsNameIdxList.get(0)) % 2 == 0) { //先頭で偶数開始なら、先頭は空白を入れる必要あり。
                                            svf.VrsOut("CLASS_NAME1_1", "　" + si._classAbbv.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx+1));
                                            prtUpClsAbbvIdx++;
                                        } else {
                                            //もう1文字取る必要があるか、確認。
                                            if (prtUpClsNameIdxList.contains(String.valueOf(prtpoint+1))) {
                                                svf.VrsOut("CLASS_NAME1_1", si._classAbbv.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx+2));
                                                prtUpClsAbbvIdx += 2;
                                            } else {
                                                svf.VrsOut("CLASS_NAME1_1", si._classAbbv.substring(prtUpClsAbbvIdx, prtUpClsAbbvIdx+1));
                                                prtUpClsAbbvIdx++;
                                            }
                                        }
                                    }
                                }
                            }
                            svf.VrsOut("SUBCLASS_NAME1", si._subclassOrder2 == null ? si._subclassName : si._subclassOrder2);

                            svf.VrsOut("CREDIT", si._credits1); //成績の単位数は"1"学年に指定で格納。
                            svf.VrsOut("GRPCD1", upKstr);

                            //テストコードに該当するデータを出力。
                            int scoreIdxCnt = 0;
                            for (Iterator ittest = _param._testCdList.iterator();ittest.hasNext();) {
                                final String seme_testcd = (String)ittest.next();
                                final String scoreKStr = seme_testcd + "-" + upSubKstr;
                                scoreIdxCnt++;
                                if (student._tounendo_subclassScoreMap.containsKey(scoreKStr)) {
                                    //取得して出力
                                    final SubclassRank prtwk = (SubclassRank)student._tounendo_subclassScoreMap.get(scoreKStr);
                                       if (upKstr.startsWith("90") && SEMEALL.equals(_param._semester)) {
                                           //'90'系はABC変換
                                         if (!"".equals(StringUtils.defaultString(prtwk._score, ""))) {
                                             final String prtMark = _param.findValuationLevel((new BigDecimal(prtwk._score)).setScale(0, BigDecimal.ROUND_HALF_UP).toString());
                                             svf.VrsOut("SCORE" + scoreIdxCnt, prtMark);    //ランク変換して出力
                                         }
                                       } else {
                                        svf.VrsOut("SCORE" + scoreIdxCnt, prtwk._score);
                                       }
                                    if (seme_testcd.equals(_param._semester + "-" + _param._testcd)) {
                                        if (!upKstr.startsWith("90")) {
                                            // クラス平均、学年平均を出力
                                            if (prtwk._clsAvg != null && !"".equals(prtwk._clsAvg)) {
                                                svf.VrsOut("AVE1", prtwk._clsAvg);
                                                sougouCls = sougouCls.add(new BigDecimal(prtwk._clsAvg));
                                                sougouClsCnt++;
                                            }
                                            if (prtwk._grdAvg != null && !"".equals(prtwk._grdAvg)) {
                                                svf.VrsOut("AVE2", prtwk._grdAvg);
                                                sougouGrd = sougouGrd.add(new BigDecimal(prtwk._grdAvg));
                                                sougouGrdCnt++;
                                            }
                                        }
                                    }
                                }
                            }
                            svf.VrEndRecord();
                            _hasData = true;
                        }
                        // 総合成績 クラス、学年平均
                        if(sougouClsCnt > 0) {
                            svf.VrsOut("TOTAL_AVE1_1", String.valueOf( sougouCls.divide(new BigDecimal(sougouClsCnt), 1, BigDecimal.ROUND_HALF_UP)));
                        }
                        if(sougouGrdCnt > 0) {
                            svf.VrsOut("TOTAL_AVE1_2", String.valueOf( sougouGrd.divide(new BigDecimal(sougouGrdCnt), 1, BigDecimal.ROUND_HALF_UP)));
                        }
                        prtUpCnt += upColCnt;
                    }
                    if (upColCnt < maxUpColCnt) {
                        //空行出力
                        for (int spCnt = upColCnt; spCnt < maxUpColCnt;spCnt++) {
                            svf.VrsOut("GRPCD1", "a");
                            svf.VrEndRecord();
                            _hasData = true;
                        }
                    }
                    //中段の表の処理
                    BigDecimal calcAvgSubcls = null;
                    int midColCnt = 0;
                    if (itMidTbl.hasNext()) {
                        for (midColCnt = prtMidCnt;midColCnt < Math.min(maxMidColCnt, student._subclsInfoMapCnt - prtMidCnt);midColCnt++) {  //1ページの出力列数をここで制御
                            if (itMidSub != null && !itMidSub.hasNext()) {
                                midSubMap = null;
                            }
                            if (!itMidTbl.hasNext() && !itMidSub.hasNext()) break;
                            if (midSubMap == null) {
                                //student._subclsInfoMapから次のマップを取得
                                if (itMidTbl.hasNext() && (itMidSub == null || !itMidSub.hasNext())) {
                                midKstr = (String)itMidTbl.next();
                                midSubMap = student.getFilteredSubClsMap(midKstr);//student._subclsInfoMap.get(midKstr);
                                }
                                calcAvgSubcls = getFilteredClsCdAvgMap(student, midKstr);  //1教科の平均を算出(表示)
                                itMidSub = midSubMap.keySet().iterator();
                                chkMidFstFlg = false; //サブマップ(教科単位のデータマップ)を取得するタイミングでクリア
                                prtMidClsAbbvIdx = 0;
                                clsnameMidPutStrtPt = midColCnt;
                            }
                            final String midSubKstr = (String)itMidSub.next();
                            final SubclsInfo si = (SubclsInfo)midSubMap.get(midSubKstr);

                            if (!chkMidFstFlg) {
                                prtMidClsNameIdxList = getClsNPrtIdx(si._classAbbv, midSubMap.size());
                                chkMidFstFlg = true;
                            }
                            //教科名
                            if (prtMidClsAbbvIdx <= si._classAbbv.length() - 1) {
                                if (prtMidClsNameIdxList == null) {
                                    if (si._classAbbv != null && si._classAbbv.length() > 2) {
                                        svf.VrsOut("CLASS_NAME2_2",  si._classAbbv);
                                        prtMidClsAbbvIdx = si._classAbbv.length();
                                    } else {
                                        svf.VrsOut("CLASS_NAME2_1", StringUtils.defaultString(si._classAbbv, ""));
                                        prtMidClsAbbvIdx++;
                                    }
                                } else {
                                    if (prtMidClsNameIdxList.contains(String.valueOf(midColCnt - clsnameMidPutStrtPt + 1))) {
                                        if (midSubMap.size() < si._classAbbv.length()) {
                                            if (prtMidClsAbbvIdx+1 == si._classAbbv.length()) {
                                                svf.VrsOut("CLASS_NAME2_2", si._classAbbv.substring(prtMidClsAbbvIdx, prtMidClsAbbvIdx+1));
                                            } else {
                                                svf.VrsOut("CLASS_NAME2_2", si._classAbbv.substring(prtMidClsAbbvIdx, prtMidClsAbbvIdx+2));
                                            }
                                            prtMidClsAbbvIdx = prtMidClsAbbvIdx + 2;
                                        } else {
                                            svf.VrsOut("CLASS_NAME2_1", si._classAbbv.substring(prtMidClsAbbvIdx, prtMidClsAbbvIdx+1));
                                            prtMidClsAbbvIdx++;
                                        }
                                    }
                                }
                            }
                            svf.VrsOut("SUBCLASS_NAME", si._subclassOrder2 == null ? si._subclassName : si._subclassOrder2);

                            //当年度分から過年度分までを出力。
                            List keysetMidList = Arrays.asList(_param._gradeMap.keySet().toArray());
                            if (keysetMidList.contains(hrClSObj._gradeCd)) {
                                int baseidx = keysetMidList.indexOf(hrClSObj._gradeCd);
                                int totalCredit = 0;
                                for (int cnt = baseidx;cnt >= 0;cnt --) {
                                    final String gradeCd = (String)keysetMidList.get(cnt);
                                    final String grdIdx = String.valueOf(Integer.parseInt(gradeCd));
                                    final String grade = (String)_param._gradeMap.get(gradeCd);

                                       final String sKey1 = grade + "-" + midSubKstr;
                                       if (student._subclassValMap.containsKey(sKey1)) {
                                           SubclassValuation prtwk1 = (SubclassValuation)student._subclassValMap.get(sKey1);
                                           if (midKstr.startsWith("90")) {
                                               //同等のデータが_totalStudyScoreMapに入っているはず。無ければ出力しない。
                                               //'90'系はABC変換
                                               if (student._totalStudyScoreMap.containsKey(sKey1)) {
                                                   SubclassValuation pwk2 = (SubclassValuation)student._totalStudyScoreMap.get(sKey1);
                                                   if (!"".equals(StringUtils.defaultString(pwk2._score, ""))) {
                                                       final String prtMark = _param.findValuationLevel(pwk2._score);
                                                       svf.VrsOut("DIV2_" + grdIdx, prtMark);    //評定をランク変換して出力
                                                   }
                                               }
                                           } else {
                                               if (prtwk1._score != null) {
                                                   svf.VrsOut("DIV2_" + grdIdx, String.valueOf((new BigDecimal(prtwk1._score)).setScale(0, BigDecimal.ROUND_HALF_UP)));  //評定
                                               }
                                           }
                                    }
                                       final String prtCredit = "1".equals(grdIdx) ? si._credits1 : "2".equals(grdIdx) ? si._credits2 : si._credits3;
                                       svf.VrsOut("DIV1_" + grdIdx, prtCredit);  //単位
                                   }
                                totalCredit = Integer.parseInt(StringUtils.defaultString(si._credits1, "0")) + Integer.parseInt(StringUtils.defaultString(si._credits2, "0")) + Integer.parseInt(StringUtils.defaultString(si._credits3, "0"));
                                if (!"".equals(StringUtils.defaultString(si._credits1, "")) || !"".equals(StringUtils.defaultString(si._credits2, "")) || !"".equals(StringUtils.defaultString(si._credits3, ""))) {
                                       svf.VrsOut("AVE_DIV1", String.valueOf(totalCredit));  //単位合計
                                }
                               }
                               //教科平均
                               if (!midKstr.startsWith("90")) {
                                   if (calcAvgSubcls != null) {
                                       svf.VrsOut("AVE_DIV2", calcAvgSubcls.setScale(1, BigDecimal.ROUND_HALF_UP).toString());  //評定
                                   }
                               }
                            svf.VrsOut("GRPCD2", midKstr);
                            svf.VrEndRecord();
                        }
                        prtMidCnt += midColCnt;
                    }
                    if (midColCnt < maxMidColCnt) {
                        //空行出力
                        for (int spCnt = midColCnt; spCnt < maxMidColCnt;spCnt++) {
                            svf.VrsOut("GRPCD2", "b");
                            svf.VrEndRecord();
                        }
                    }
                    svf.VrEndRecord();
                    _hasData = true;
                }
                svf.VrEndPage();
            }
        }
    }

    private BigDecimal getFilteredClsCdAvgMap(final Student student, final String midKstr) {
        BigDecimal retBd = null;
        Map srchMap = student.getFilteredSubClsMap(midKstr);
        int addCnt = 0;
        if (srchMap != null) {
            for (Iterator ite = srchMap.keySet().iterator();ite.hasNext();) {
                final String subclassCd = (String)ite.next();
                //student._subclassValMapのキーにはgradeが含まれるので、"後ろのSubclassCdが一致する"物を拾う。
                for (Iterator its = student._subclassValMap.keySet().iterator();its.hasNext();) {
                    final String kStr = (String)its.next();
                    if (kStr.endsWith(subclassCd)) {
                        SubclassValuation prtwk1 = (SubclassValuation)student._subclassValMap.get(kStr);
                        if (prtwk1._score != null) {
                               if (!midKstr.startsWith("90")) {
                                   if (retBd == null) retBd = new BigDecimal(0.0);
                                   retBd = retBd.add(new BigDecimal(prtwk1._score));
                                addCnt++;
                               }
                        }
                    }
                }
            }
            if (retBd != null) {
                retBd = retBd.divide(new BigDecimal(addCnt), BigDecimal.ROUND_HALF_UP);
            }
        }

        return retBd;
    }

    private void printAttendRange(final Vrw32alp svf, final String baseFieldStr, final String fieldIdx, final AttData outwk) {
        svf.VrsOutn(baseFieldStr+fieldIdx, 1, String.valueOf(outwk._j_Nisu));
        svf.VrsOutn(baseFieldStr+fieldIdx, 2, String.valueOf(outwk._suspend));
        svf.VrsOutn(baseFieldStr+fieldIdx, 3, String.valueOf(outwk._must));
        svf.VrsOutn(baseFieldStr+fieldIdx, 4, String.valueOf(outwk._absence));
        svf.VrsOutn(baseFieldStr+fieldIdx, 5, String.valueOf(outwk._attend));
        svf.VrsOutn(baseFieldStr+fieldIdx, 6, String.valueOf(outwk._late));
        svf.VrsOutn(baseFieldStr+fieldIdx, 7, String.valueOf(outwk._early));
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

    private void setExceptSpread(final DB2UDB db2, final Vrw32alp svf, final GradeHrCls hrClSObj, final Student student) {
        //タイトル
        final String grdName = "第" + String.valueOf(Integer.parseInt(hrClSObj._gradeCd)) + "学年";
        svf.VrsOut("TITLE", grdName + " 成績連絡票");

        //年組
        svf.VrsOut("HR_NAME", hrClSObj._hrName + " " + Integer.parseInt(student._attendNo) + "番 " + student._name);

        //学校名
        svf.VrsOut("SCHOOL_NAME", (String)_param._certifInfo.get("SCHOOL_NAME"));
        //学校長名
        svf.VrsOut("PRINCIPAL_NAME", (String)_param._certifInfo.get("PRINCIPAL_NAME"));
        //担任名
        svf.VrsOut("TR_NAME", hrClSObj._staffName);
        //日付
        svf.VrsOut("DATE", _param._ctrlDate);
        //総合的な学習の時間(タイトル)
        final boolean ttlChkFlg = ("2020".compareTo(_param._year) >= 0 && "03".equals(hrClSObj._gradeCd)) || ("2019".compareTo(_param._year) >= 0 && ("02".equals(hrClSObj._gradeCd) || "03".equals(hrClSObj._gradeCd))) || ("2018".compareTo(_param._year) >= 0);
        final String ttlStr = ttlChkFlg ? "総合的な学習の時間" : "総合的な探究の時間";
        svf.VrsOut("TOTAL_ACT_NAME", ttlStr);
        //定期考査成績の学年末名称(タイトル)
        svf.VrsOut("TEST_ITEM_NAME", Integer.parseInt(hrClSObj._gradeCd) + "学年総合");
    }

    private Map getGHMapInfo(final DB2UDB db2) throws SQLException {
        final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
        Map retMap = new LinkedMap();
        GradeHrCls ghCls = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  A023.ABBV1 AS SKNAME, ");
            stb.append("  GDAT.GRADE_CD, ");
            stb.append("  GDAT.GRADE_NAME1, ");
            stb.append("  GDAT.GRADE_NAME2, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  HDAT.HR_NAMEABBV, ");
            stb.append("  SM.STAFFNAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("          AND GDAT.GRADE = REGD.GRADE ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append("    LEFT JOIN SEMESTER_MST SEM_MST ON SEM_MST.SEMESTER = REGD.SEMESTER ");
            stb.append("    LEFT JOIN STAFF_MST SM ON SM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("    LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + useSemester + "' ");
            stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            Semester datechk = (Semester)_param._semesterMap.get(useSemester);
            // 学期の最終日を基準にして、転校・留学などをチェック
            stb.append("  AND ( ");
            stb.append("       BASE.GRD_DATE IS NULL OR (");
            stb.append("         BASE.GRD_DATE IS NOT NULL AND ( ");
            stb.append("           (BASE.GRD_DIV IN('2','3') AND BASE.GRD_DATE > (CASE WHEN SEM_MST.EDATE < '" + datechk._dateRange._edate + "' THEN SEM_MST.EDATE ELSE '" + datechk._dateRange._edate + "' END) ");
            stb.append("           OR (BASE.ENT_DIV IN('4','5') AND BASE.ENT_DATE <= CASE WHEN SEM_MST.SDATE < '" + datechk._dateRange._edate + "' THEN SEM_MST.SDATE ELSE '" + datechk._dateRange._edate + "' END)) ");
            stb.append("         )");
            stb.append("       )");

            stb.append("  )");
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO  ");

            //log.debug(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String skName = rs.getString("SKNAME");
                final String gradeCd = rs.getString("GRADE_CD");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String gradeName2 = rs.getString("GRADE_NAME2");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String staffName = rs.getString("STAFFNAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODENAME");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        hrName,
                        hrAbbv,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName,
                        courseCodeName
                );
                student.setSubclassValuation(db2);
                student.setTounendoSubclassRank(db2);
                student.setSubclassRank(db2);
                student.setValuation(db2, schoolKind);
                student.setAttendMonth(db2);
                student.setAttendGrade(db2);
                student.setAttendRemarks(db2);
                student.setTotalStudyTime(db2);
                //student.setTounendoSubclassInfo(db2);
                student.setSubclassInfo(db2, grade + hrclass, schregno);
                student.setTotalStudyScore(db2);
                final String rmKey = grade + "-" + hrclass;
                if (retMap.containsKey(rmKey)) {
                    ghCls = (GradeHrCls)retMap.get(rmKey);
                } else {
                    ghCls = new GradeHrCls(grade, schoolKind, skName, gradeCd, gradeName, gradeName2, hrclass, hrName, hrAbbv, staffName);
                    retMap.put(rmKey, ghCls);
                }
                if (!ghCls._schregMap.containsKey(schregno)) {
                    ghCls._schregMap.put(schregno, student);
                }
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private class GradeHrCls {
        final String _grade;
        final String _schoolKind;
        final String _skName;
        final String _gradeCd;
        final String _gradeName;
        final String _gradeName2;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _staffName;
        final Map _subclsInfoMap;
        int _subclsInfoMapCnt;
        final Map _schregMap;
        GradeHrCls(
                final String grade,
                final String schoolKind,
                final String skName,
                final String gradeCd,
                final String gradeName,
                final String gradeName2,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String staffName
                ) {
            _grade = grade;
            _schoolKind = schoolKind;
            _skName = skName;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
            _gradeName2 = gradeName2;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _staffName = staffName;
            _subclsInfoMap = new LinkedMap();
            _schregMap = new LinkedMap();
            _subclsInfoMapCnt = 0;
        }
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final Map _subclassValMap;
        final Map _subclassRankMap;
        final Map _attendMonthMap;
        final Map _attendGradeMap;
        final Map _attendRemarkMap;
        String _totalStudyTime;
        String _totalStudyAct;
        String _remark1;

        final Map _tounendo_subclsInfoMap;
        final Map _tounendo_subclassScoreMap;
        int _tounendo_subclsInfoMapCnt;
        final Map _valuationDetailDat;
        final Map _subclsInfoMap;
        int _subclsInfoMapCnt = 0;

        //評定平均算出用
        final Map _totalAvgMap;
        final Map _totalAvgCountMap;

        final Map _totalStudyScoreMap;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _subclassValMap = new  LinkedMap();
            _subclassRankMap = new LinkedMap();
            _attendMonthMap = new LinkedMap();
            _attendGradeMap = new LinkedMap();
            _attendRemarkMap = new LinkedMap();
            _totalStudyTime = "";
            _totalStudyAct = "";
            _remark1 = "";
            _tounendo_subclsInfoMap = new LinkedMap();
            _tounendo_subclassScoreMap = new LinkedMap();
            _tounendo_subclsInfoMapCnt = 0;
            _valuationDetailDat = new LinkedMap();
            _totalAvgMap = new LinkedMap();
            _totalAvgCountMap = new LinkedMap();
            _subclsInfoMap = new LinkedMap();
            _totalStudyScoreMap = new LinkedMap();
        }


        public void setSubclassValuation(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            ResultSet rs = null;
            final String pskey = "setSubclassValuation";

            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SYUUYAKU_WK_SUBBASE AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBだけでは留年した年のデータが抜けないため、過年度～当年度の各学年で最後の年(最大の年)で一度集約)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBBASEで集約した年度の学期を取得する。)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append("     INNER JOIN SYUUYAKU_WK_SUBBASE TW3 ");
                stb.append("       ON TW3.YEAR = TW1.YEAR ");
                stb.append("      AND TW3.GRADE_CD = TW2.GRADE_CD ");
                stb.append("      AND TW3.SCHREGNO = TW1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
                stb.append(" SELECT ");
                stb.append("    TK1.YEAR, ");
                stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
                stb.append("    TK1.GRADE_CD, ");
                stb.append("    TK1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK1 ");
                stb.append(" WHERE ");
                stb.append("    TK1.YEAR < '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TK1.YEAR, ");
                stb.append("     TK1.GRADE_CD, ");
                stb.append("     TK1.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     TK2.YEAR, ");
                stb.append("     TK2.SEMESTER, ");
                stb.append("     TK2.GRADE_CD, ");
                stb.append("     TK2.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK2 ");
                stb.append(" WHERE ");
                stb.append("    TK2.YEAR = '" + _param._year + "' ");
                stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
                stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得(過年度はクラスでは取得できないので、学籍番号で取得しないといけない)
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
                stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
                stb.append("     ON TW1.YEAR = T1.YEAR ");
                stb.append("    AND TW1.SEMESTER = T1.SEMESTER ");
                stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ), PRTBASE_DAT AS ( ");
                // 過年度評定データ
                stb.append(" select ");
                stb.append("   T2.YEAR, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   NULL AS PROV_FLG, ");
                stb.append("   T2.VALUATION AS SCORE ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN SCHREG_STUDYREC_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
                stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR < '" + _param._year + "' ");
                stb.append("   AND T2.YEAR IS NOT NULL ");
                stb.append(" UNION ALL ");
                // 当年度データ
                stb.append(" select ");
                stb.append("   T2.YEAR, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   NULL AS PROV_FLG, ");
                stb.append("   T2.SCORE ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                if ("1".equals(_param._useProvFlg)) {
                    stb.append("   LEFT JOIN RECORD_PROV_FLG_DAT P1 ");
                    stb.append("     ON P1.YEAR = T2.YEAR ");
                    stb.append("    AND P1.CLASSCD = T2.CLASSCD ");
                    stb.append("    AND P1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb.append("    AND P1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                    stb.append("    AND P1.SUBCLASSCD = T2.SUBCLASSCD ");
                    stb.append("    AND P1.SCHREGNO = T2.SCHREGNO ");
                }
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
                stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _param._cutTestCd + "09' ");  //09固定で取得
                stb.append("   AND T2.YEAR IS NOT NULL ");
                stb.append("   AND T2.SUBCLASSCD NOT IN ('" + ALL3 + "', '"+ALL7+"', '"+ALL5+"', '" + ALL9A + "', '" + ALL9B + "') ");
                stb.append(" ), KEKKA AS ( ");
                // 評定データ(過年度含む)
                stb.append(" SELECT ");
                stb.append("   TF1.YEAR, ");
                stb.append("   TF1.GRADE, ");
                stb.append("   TF1.SCHREGNO, ");
                stb.append("   TF1.CLASSCD, ");
                stb.append("   TF1.SCHOOL_KIND, ");
                stb.append("   TF1.CURRICULUM_CD, ");
                stb.append("   TF1.SUBCLASSCD, ");
                stb.append("   TF1.PROV_FLG, ");
                stb.append("   DECIMAL(CAST(TF1.SCORE AS double), 5, 1) AS SCORE ");
                stb.append(" FROM ");
                stb.append("   PRTBASE_DAT TF1 ");
                stb.append(" UNION ALL ");
                // 評定データ(過年度含む教科別の平均を算出)
                stb.append(" select ");
                stb.append("   '9999' AS YEAR, ");
                stb.append("   '99' AS GRADE, ");
                stb.append("   TF2.SCHREGNO, ");
                stb.append("   TF2.CLASSCD, ");
                stb.append("   TF2.SCHOOL_KIND, ");
                stb.append("   '99' AS CURRICULUM_CD, ");
                stb.append("   '"+ALLZ+"' AS SUBCLASSCD, ");
                stb.append("   TF2.PROV_FLG, ");
                stb.append("   DECIMAL(ROUND(AVG(CAST(TF2.SCORE AS double))*10,0)/10,5,1) AS SCORE ");
                stb.append(" FROM ");
                stb.append("   PRTBASE_DAT TF2 ");
                stb.append(" WHERE ");
                stb.append("   TF2.SUBCLASSCD NOT IN ('" + ALL3 + "', '"+ALL7+"', '" + ALL9A + "', '" + ALL9B + "') ");
                stb.append("   AND TF2.SUBCLASSCD NOT IN ('" + ALL9 + "', '" + ALL5 + "') ");
                if (_param._ignoreSubclsList.size() > 0) {
                    stb.append("   AND TF2.CLASSCD || '-' || TF2.SCHOOL_KIND || '-' || TF2.CURRICULUM_CD || '-' || TF2.SUBCLASSCD NOT IN " + SQLUtils.whereIn(false, (String[])_param._ignoreSubclsList.toArray(new String[_param._ignoreSubclsList.size()])));
                }
                stb.append(" GROUP BY ");
                stb.append("   TF2.SCHREGNO, ");
                stb.append("   TF2.CLASSCD, ");
                stb.append("   TF2.SCHOOL_KIND, ");
                stb.append("   TF2.PROV_FLG ");
                stb.append(" ORDER BY ");
                stb.append("   SCHREGNO ASC, ");
                stb.append("   GRADE ASC, ");
                stb.append("   YEAR ASC, ");
                stb.append("   CLASSCD, ");
                stb.append("   SCHOOL_KIND, ");
                stb.append("   CURRICULUM_CD, ");
                stb.append("   SUBCLASSCD ");
                stb.append(" ), DANKAIAVG AS( "); //段階用　評定AVG
                stb.append(" select ");
                stb.append("   '9999' AS YEAR, ");
                stb.append("   '99' AS GRADE, ");
                stb.append("   TF2.SCHREGNO, ");
                stb.append("   '99' AS CLASSCD, ");
                stb.append("   '" + _param._schoolKind + "' AS SCHOOL_KIND, ");
                stb.append("   '99' AS CURRICULUM_CD, ");
                stb.append("   '"+ALLZ+"' AS SUBCLASSCD, ");
                stb.append("   '99' AS PROV_FLG, ");
                stb.append("   DECIMAL(ROUND(AVG(CAST(TF2.SCORE AS double))*10,0)/10,5,1) AS SCORE ");
                stb.append(" FROM ");
                stb.append("   PRTBASE_DAT TF2 ");
                stb.append(" WHERE ");
                stb.append("   TF2.SUBCLASSCD NOT IN ('" + ALL3 + "', '"+ALL7+"', '" + ALL9A + "', '" + ALL9B + "') ");
                stb.append("   AND TF2.SUBCLASSCD NOT IN ('" + ALL9 + "', '" + ALL5 + "') ");
                stb.append("   AND TF2.CLASSCD < '90' ");
                stb.append(" GROUP BY ");
                stb.append("   TF2.SCHREGNO ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.* ");
                stb.append(" FROM ");
                stb.append("   KEKKA T1 ");
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("   T1.* ");
                stb.append(" FROM ");
                stb.append("   DANKAIAVG T1 ");
                log.info(stb.toString());

                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);

            try {
                ps.setString(1, _grade + _hrClass);
                ps.setString(2, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    final String schregno = rs.getString("SCHREGNO");
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String prov_Flg = rs.getString("PROV_FLG");
                    final String score = rs.getString("SCORE");

                    final SubclassValuation subclsVal = new SubclassValuation(year, grade, schregno, classcd, schoolKind, curriculumCd, subclasscd, prov_Flg, score);
                    final String mKey = grade + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    _subclassValMap.put(mKey, subclsVal);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }
        public void setTotalStudyScore(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            ResultSet rs = null;
            final String pskey = "setTotalStudyScore";

            if (!_param._psHandleMap.containsKey(pskey)) {
                StringBuffer stb = new StringBuffer();
                stb.append(" WITH SYUUYAKU_WK_SUBBASE AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBだけでは留年した年のデータが抜けないため、過年度～当年度の各学年で最後の年(最大の年)で一度集約)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBBASEで集約した年度の学期を取得する。)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append("     INNER JOIN SYUUYAKU_WK_SUBBASE TW3 ");
                stb.append("       ON TW3.YEAR = TW1.YEAR ");
                stb.append("      AND TW3.GRADE_CD = TW2.GRADE_CD ");
                stb.append("      AND TW3.SCHREGNO = TW1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
                stb.append(" SELECT ");
                stb.append("    TK1.YEAR, ");
                stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
                stb.append("    TK1.GRADE_CD, ");
                stb.append("    TK1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK1 ");
                stb.append(" WHERE ");
                stb.append("    TK1.YEAR < '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TK1.YEAR, ");
                stb.append("     TK1.GRADE_CD, ");
                stb.append("     TK1.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     TK2.YEAR, ");
                stb.append("     TK2.SEMESTER, ");
                stb.append("     TK2.GRADE_CD, ");
                stb.append("     TK2.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK2 ");
                stb.append(" WHERE ");
                stb.append("    TK2.YEAR = '" + _param._year + "' ");
                stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
                stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得(過年度はクラスでは取得できないので、学籍番号で取得しないといけない)
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
                stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
                stb.append("     ON TW1.YEAR = T1.YEAR ");
                stb.append("    AND TW1.SEMESTER = T1.SEMESTER ");
                stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.YEAR, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   T2.SCORE ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   ( ");
                stb.append("     (T2.YEAR < '" + _param._year + "' AND T2.TESTKINDCD || T2.TESTITEMCD = '9900' AND T2.SEMESTER = '9') ");
                stb.append("     OR  ");
                stb.append("     (T2.YEAR = '" + _param._year + "' AND T2.TESTKINDCD || T2.TESTITEMCD = '" + _param._cutTestCd + "' AND T2.SEMESTER = '" + _param._semester + "') ");
                stb.append("   ) ");
                stb.append("   AND T2.SCORE_DIV = '09' ");
                stb.append("   AND T2.CLASSCD = '90' ");

                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);

            try {
                ps.setString(1, _grade + _hrClass);
                ps.setString(2, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    final String schregno = rs.getString("SCHREGNO");
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String prov_Flg = "";
                    final String score = rs.getString("SCORE");
                    final SubclassValuation subclsVal = new SubclassValuation(year, grade, schregno, classcd, schoolKind, curriculumCd, subclasscd, prov_Flg, score);
                    final String mKey = grade + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    _totalStudyScoreMap.put(mKey, subclsVal);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }

        }

        public void setTounendoSubclassRank(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            ResultSet rs = null;
            final String pskey = "setTounendoSubclassRank";

            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH TESTDAT_TBL AS ( ");
                stb.append(" SELECT ");
                stb.append("   T1.YEAR, ");
                stb.append("   T1.SEMESTER, ");
                stb.append("   T1.TESTKINDCD, ");
                stb.append("   T1.TESTITEMCD, ");
                stb.append("   T1.SCORE_DIV, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T1.TESTITEMNAME ");
                stb.append(" FROM ");
                stb.append("   TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append("   INNER JOIN ADMIN_CONTROL_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("     AND T2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("     AND T2.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("     AND T2.CLASSCD      = '00' ");
                stb.append("     AND T2.CURRICULUM_CD  = '00' ");
                stb.append("     AND T2.SUBCLASSCD  = '000000' ");
                stb.append(" ), KEKKA AS (");
                stb.append(" SELECT ");
                stb.append("  T2.HR_CLASS, ");
                stb.append("  T2.ATTENDNO, ");
                stb.append("  T1.SCHREGNO, ");
                stb.append("  T1.SEMESTER, ");
                stb.append("  T1.TESTKINDCD, ");
                stb.append("  T1.TESTITEMCD, ");
                stb.append("  T1.SCORE_DIV, ");
                stb.append("  L2.TESTITEMNAME, ");
                stb.append("  T1.CLASSCD, ");
                stb.append("  T1.SCHOOL_KIND, ");
                stb.append("  T1.CURRICULUM_CD, ");
                stb.append("  T1.SUBCLASSCD, ");
                stb.append("  T1.SCORE, ");
                stb.append("  T1.AVG, ");
                stb.append("  CAST(NULL AS DOUBLE) AS CLASS_AVG, ");
                stb.append("  CAST(NULL AS DOUBLE) AS GRADE_AVG, ");
                stb.append("  T1.CLASS_RANK, ");
                stb.append("  T1.CLASS_AVG_RANK, ");
                stb.append("  T1.GRADE_RANK, ");
                stb.append("  T1.GRADE_AVG_RANK ");
                stb.append(" FROM ");
                stb.append("  SCHREG_REGD_DAT T2 ");
                stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T1 ");
                stb.append("    ON T1.YEAR = T2.YEAR ");
                stb.append("   AND T1.SEMESTER = T2.SEMESTER ");
                stb.append("   AND T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("   AND T1.SCORE_DIV = '08' ");
                stb.append("  LEFT JOIN TESTDAT_TBL L2 ");
                stb.append("    ON L2.YEAR = T1.YEAR ");
                stb.append("   AND L2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND L2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND L2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND L2.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("  T2.YEAR = '" + _param._year + "' ");
                if (SEMEALL.equals(_param._semester)) {
                    //"9"学期指定の場合は、"9"学期以外を取得する。
                    stb.append("  AND T2.SEMESTER <= '" + useSemester + "' ");
                } else {
                    stb.append("  AND T2.SEMESTER < '" + useSemester + "' ");
                }
                stb.append("  AND T2.SCHREGNO = ? ");
                stb.append("  AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9A + "', '" + ALL9B + "') ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("  T2.HR_CLASS, ");
                stb.append("  T2.ATTENDNO, ");
                stb.append("  T1.SCHREGNO, ");
                stb.append("  T1.SEMESTER, ");
                stb.append("  T1.TESTKINDCD, ");
                stb.append("  T1.TESTITEMCD, ");
                stb.append("  T1.SCORE_DIV, ");
                stb.append("  L2.TESTITEMNAME, ");
                stb.append("  T1.CLASSCD, ");
                stb.append("  T1.SCHOOL_KIND, ");
                stb.append("  T1.CURRICULUM_CD, ");
                stb.append("  T1.SUBCLASSCD, ");
                stb.append("  T1.SCORE, ");
                stb.append("  T1.AVG, ");
                stb.append("  T3_1.AVG AS CLASS_AVG, ");
                stb.append("  T3_2.AVG AS GRADE_AVG, ");
                stb.append("  T1.CLASS_RANK, ");
                stb.append("  T1.CLASS_AVG_RANK, ");
                stb.append("  T1.GRADE_RANK, ");
                stb.append("  T1.GRADE_AVG_RANK ");
                stb.append(" FROM ");
                stb.append("  SCHREG_REGD_DAT T2 ");
                stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T1 ");
                stb.append("    ON T1.YEAR = T2.YEAR ");
                stb.append("   AND T1.SEMESTER = '" + _param._semester + "' "); //指定学期のデータのみ取得する。それ以前は前のUNIONで取得する。
                stb.append("   AND T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("   AND T1.SCORE_DIV = '08' ");
                stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3_1 ");
                stb.append("    ON T3_1.YEAR = T1.YEAR ");
                stb.append("   AND T3_1.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T3_1.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND T3_1.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND T3_1.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND T3_1.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3_1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3_1.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   AND T3_1.AVG_DIV = '2' ");
                stb.append("   AND T3_1.GRADE = T2.GRADE ");
                stb.append("   AND T3_1.HR_CLASS = T2.HR_CLASS ");
                stb.append("   AND T3_1.COURSECD = '0' ");
                stb.append("   AND T3_1.MAJORCD = '000' ");
                stb.append("   AND T3_1.COURSECODE = '0000' ");
                stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3_2 ");
                stb.append("    ON T3_2.YEAR = T1.YEAR ");
                stb.append("   AND T3_2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T3_2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND T3_2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND T3_2.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND T3_2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3_2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   AND T3_2.AVG_DIV = '1' ");
                stb.append("   AND T3_2.GRADE = T2.GRADE ");
                stb.append("   AND T3_2.HR_CLASS = '000' ");
                stb.append("   AND T3_1.COURSECD = '0' ");
                stb.append("   AND T3_1.MAJORCD = '000' ");
                stb.append("   AND T3_1.COURSECODE = '0000' ");
                stb.append("  LEFT JOIN TESTDAT_TBL L2 ");
                stb.append("    ON L2.YEAR = T1.YEAR ");
                stb.append("   AND L2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND L2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND L2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND L2.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("  T2.YEAR = '" + _param._year + "' ");
                stb.append("  AND T2.SEMESTER = '"+useSemester+"' ");
                stb.append("  AND T2.SCHREGNO = ? ");
                stb.append("  AND T1.TESTKINDCD || T1.TESTITEMCD <= '" + _param._cutTestCd + "' ");
                stb.append("  AND T1.SCORE_DIV = '" + _param._cutScoreDiv + "' ");
                stb.append("  AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9A + "', '" + ALL9B + "') ");
                stb.append("  AND T1.CLASSCD NOT IN ('90') ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("  T2.HR_CLASS, ");
                stb.append("  T2.ATTENDNO, ");
                stb.append("  T1.SCHREGNO, ");
                stb.append("  T1.SEMESTER, ");
                stb.append("  T1.TESTKINDCD, ");
                stb.append("  T1.TESTITEMCD, ");
                stb.append("  '08' AS SCORE_DIV, ");  //'90'の場合は、SCORE_DIVは'09'のみ取得するが、データの紐づけの問題から、'08'固定にする。
                stb.append("  L2.TESTITEMNAME, ");
                stb.append("  T1.CLASSCD, ");
                stb.append("  T1.SCHOOL_KIND, ");
                stb.append("  T1.CURRICULUM_CD, ");
                stb.append("  T1.SUBCLASSCD, ");
                stb.append("  T1.SCORE, ");
                stb.append("  T1.AVG, ");
                stb.append("  T3_1.AVG AS CLASS_AVG, ");
                stb.append("  T3_2.AVG AS GRADE_AVG, ");
                stb.append("  T1.CLASS_RANK, ");
                stb.append("  T1.CLASS_AVG_RANK, ");
                stb.append("  T1.GRADE_RANK, ");
                stb.append("  T1.GRADE_AVG_RANK ");
                stb.append(" FROM ");
                stb.append("  SCHREG_REGD_DAT T2 ");
                stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T1 ");
                stb.append("    ON T1.YEAR = T2.YEAR ");
                stb.append("   AND T1.SEMESTER = '" + _param._semester + "' "); //指定学期のデータのみ取得する。それ以前は前のUNIONで取得する。
                stb.append("   AND T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("   AND T1.SCORE_DIV = '09' ");
                stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3_1 ");
                stb.append("    ON T3_1.YEAR = T1.YEAR ");
                stb.append("   AND T3_1.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T3_1.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND T3_1.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND T3_1.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND T3_1.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3_1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3_1.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   AND T3_1.AVG_DIV = '2' ");
                stb.append("   AND T3_1.GRADE = T2.GRADE ");
                stb.append("   AND T3_1.HR_CLASS = T2.HR_CLASS ");
                stb.append("   AND T3_1.COURSECD = '0' ");
                stb.append("   AND T3_1.MAJORCD = '000' ");
                stb.append("   AND T3_1.COURSECODE = '0000' ");
                stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3_2 ");
                stb.append("    ON T3_2.YEAR = T1.YEAR ");
                stb.append("   AND T3_2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T3_2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND T3_2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND T3_2.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND T3_2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3_2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   AND T3_2.AVG_DIV = '1' ");
                stb.append("   AND T3_2.GRADE = T2.GRADE ");
                stb.append("   AND T3_2.HR_CLASS = '000' ");
                stb.append("   AND T3_1.COURSECD = '0' ");
                stb.append("   AND T3_1.MAJORCD = '000' ");
                stb.append("   AND T3_1.COURSECODE = '0000' ");
                stb.append("  LEFT JOIN TESTDAT_TBL L2 ");
                stb.append("    ON L2.YEAR = T1.YEAR ");
                stb.append("   AND L2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND L2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND L2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND L2.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("  T2.YEAR = '" + _param._year + "' ");
                stb.append("  AND T2.SEMESTER = '" + useSemester + "' ");
                stb.append("  AND T2.SCHREGNO = ? ");
                stb.append("  AND T1.TESTKINDCD || T1.TESTITEMCD <= '" + _param._cutTestCd + "' ");
                stb.append("  AND T1.SCORE_DIV = '09' ");
                stb.append("  AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9A + "', '" + ALL9B + "') ");
                stb.append("  AND T1.CLASSCD IN ('90') ");
                stb.append(" ORDER BY ");
                stb.append("  SEMESTER, ");
                stb.append("  TESTKINDCD, ");
                stb.append("  TESTITEMCD, ");
                stb.append("  SCORE_DIV, ");
                stb.append("  CLASSCD, ");
                stb.append("  SCHOOL_KIND, ");
                stb.append("  CURRICULUM_CD, ");
                stb.append("  SUBCLASSCD ");
                stb.append(" )");
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   L1.CLASSNAME, ");
                stb.append("   L1.CLASSABBV, ");
                stb.append("   L2.SUBCLASSNAME, ");
                stb.append("   L2.SUBCLASSABBV, ");
                stb.append("   L2.SUBCLASSORDERNAME2, ");
                stb.append("   C1.CREDITS");
                stb.append(" FROM ");
                stb.append("  KEKKA T1 ");
                stb.append("   LEFT JOIN CLASS_MST L1 ");
                stb.append("     ON L1.CLASSCD = T1.CLASSCD ");
                stb.append("    AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   LEFT JOIN SUBCLASS_MST L2 ");
                stb.append("     ON L2.CLASSCD = T1.CLASSCD ");
                stb.append("    AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   LEFT JOIN CREDIT_MST C1 ");
                stb.append("     ON C1.YEAR =  '" + _param._year + "'");
                stb.append("    AND C1.GRADE = '" + _param._grade + "'");
                stb.append("    AND C1.CLASSCD = T1.CLASSCD ");
                stb.append("    AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND C1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND C1.SUBCLASSCD = T1.SUBCLASSCD ");

                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);

            try {
                ps.setString(1, _schregno);
                ps.setString(2, _schregno);
                ps.setString(3, _schregno);
                rs = ps.executeQuery();

                Map subMap = null;
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    String temp = "";
                    if("999999".equals(subclasscd)) {
                        if(rs.getBigDecimal("AVG") != null && !"".equals(rs.getString("AVG"))) {
                            BigDecimal b1 = rs.getBigDecimal("AVG");
                            temp = b1.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
                        }
                    }else {
                        temp = rs.getString("SCORE");
                    }
                    final String score = temp; // 総合成績はAVG

                    BigDecimal b2 = rs.getBigDecimal("CLASS_AVG");
                    final String clsAvg = rs.getBigDecimal("CLASS_AVG") != null && !"".equals(rs.getString("CLASS_AVG")) ? b2.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
                    BigDecimal b3 = rs.getBigDecimal("GRADE_AVG");
                    final String grdAvg = rs.getBigDecimal("GRADE_AVG") != null && !"".equals(rs.getString("GRADE_AVG")) ? b3.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
                    final String class_Rank = rs.getString("CLASS_RANK");
                    final String grade_Rank = rs.getString("GRADE_RANK");
                    final String classAvg_Rank = rs.getString("CLASS_AVG_RANK");
                    final String gradeAvg_Rank = rs.getString("GRADE_AVG_RANK");


                    final String semester = rs.getString("SEMESTER");
                    final String testKindCd = rs.getString("TESTKINDCD");
                    final String testItemCd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String testItemName = rs.getString("TESTITEMNAME");

                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String classname = rs.getString("CLASSNAME");
                    final String classabbv = rs.getString("CLASSABBV");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final String subclassOrder2 = rs.getString("SUBCLASSORDERNAME2");
                    final String credits = rs.getString("CREDITS");


                    //当年度分なので、year、gradeが無くても問題無いが、学期、考査コード、科目コードの切り分けが必要。
                    final SubclassRank subclassRank = new SubclassRank("", "", schregno, classcd, schoolKind, curriculumCd, subclasscd, score, "", clsAvg, grdAvg, class_Rank, classAvg_Rank, grade_Rank, gradeAvg_Rank, testItemName);
                    final String mKey = semester + "-" + testKindCd + testItemCd + scoreDiv + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    log.debug("mKey:"+mKey);
                    _tounendo_subclassScoreMap.put(mKey, subclassRank);


                    if(Integer.parseInt(classcd) > 90) continue;

                    //上段表科目
                    SubclsInfo addwk = new SubclsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, classname, classabbv, subclassname, subclassabbv, subclassOrder2, false, false, credits, "0", "0");
                    final String cKey = classcd + "-" + school_Kind;
                    if (_tounendo_subclsInfoMap.containsKey(cKey)) {
                        subMap = (Map)_tounendo_subclsInfoMap.get(cKey);
                    } else {
                        subMap = new LinkedMap();
                        _tounendo_subclsInfoMap.put(cKey, subMap);
                    }
                    final String subKey = classcd + "-" + school_Kind + "-" + curriculum_Cd  + "-" + subclasscd;
                    subMap.put(subKey, addwk);

                }
                for (Iterator ite = _tounendo_subclsInfoMap.keySet().iterator();ite.hasNext();) {
                    final String kStr = (String)ite.next();
                    Map chkMap = (Map)_tounendo_subclsInfoMap.get(kStr);
                    _tounendo_subclsInfoMapCnt += chkMap.size();
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }

        public void setSubclassRank(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            ResultSet rs = null;
            final String pskey = "setSubclassRank";

            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SYUUYAKU_WK_SUBBASE AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBだけでは留年した年のデータが抜けないため、過年度～当年度の各学年で最後の年(最大の年)で一度集約)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBBASEで集約した年度の学期を取得する。)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append("     INNER JOIN SYUUYAKU_WK_SUBBASE TW3 ");
                stb.append("       ON TW3.YEAR = TW1.YEAR ");
                stb.append("      AND TW3.GRADE_CD = TW2.GRADE_CD ");
                stb.append("      AND TW3.SCHREGNO = TW1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
                stb.append(" SELECT ");
                stb.append("    TK1.YEAR, ");
                stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
                stb.append("    TK1.GRADE_CD, ");
                stb.append("    TK1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK1 ");
                stb.append(" WHERE ");
                stb.append("    TK1.YEAR < '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TK1.YEAR, ");
                stb.append("     TK1.GRADE_CD, ");
                stb.append("     TK1.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     TK2.YEAR, ");
                stb.append("     TK2.SEMESTER, ");
                stb.append("     TK2.GRADE_CD, ");
                stb.append("     TK2.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK2 ");
                stb.append(" WHERE ");
                stb.append("    TK2.YEAR = '" + _param._year + "' ");
                stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
                stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得(過年度はクラスでは取得できないので、学籍番号で取得しないといけない)
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
                stb.append("     ON TW1.YEAR = T1.YEAR ");
                stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
                stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ) ");
                //-- 過年度成績(評価)データ
                stb.append(" select ");
                stb.append("   T2.YEAR, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   T2.SCORE, ");
                stb.append("   DECIMAL(ROUND(T2.AVG, 0), 5, 1) AS AVG, ");
                stb.append("   T2.CLASS_RANK, ");
                stb.append("   T2.CLASS_AVG_RANK, ");
                stb.append("   T2.GRADE_RANK, ");
                stb.append("   T2.GRADE_AVG_RANK ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = '9' ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR < '" + _param._year + "' ");
                stb.append("   AND T2.YEAR IS NOT NULL ");
                stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' ");  //過年度は年度末評価固定
                stb.append("   AND T2.SUBCLASSCD = '" + ALL9 + "' ");
                stb.append(" UNION ALL");
                //-- 当年度成績(評価)データ
                stb.append(" select ");
                stb.append("   T2.YEAR, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   T2.SCORE, ");
                stb.append("   DECIMAL(ROUND(T2.AVG, 0), 5, 1) AS AVG, ");
                stb.append("   T2.CLASS_RANK, ");
                stb.append("   T2.CLASS_AVG_RANK, ");
                stb.append("   T2.GRADE_RANK, ");
                stb.append("   T2.GRADE_AVG_RANK ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _param._cutTestCd + "08' ");  //08固定で
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
                stb.append("   AND T2.YEAR IS NOT NULL ");
                stb.append("   AND T2.SUBCLASSCD = '" + ALL9 + "' ");
                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);

            try {
                ps.setString(1, _grade + _hrClass);
                ps.setString(2, _schregno);
                ps.setString(3, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    final String schregno = rs.getString("SCHREGNO");
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String class_Rank = rs.getString("CLASS_RANK");
                    final String class_avg_Rank = rs.getString("CLASS_AVG_RANK");
                    final String grade_Rank = rs.getString("GRADE_RANK");
                    final String grade_avg_Rank = rs.getString("GRADE_AVG_RANK");

                    final SubclassRank subclassRank = new SubclassRank(year, grade, schregno, classcd, schoolKind, curriculumCd, subclasscd, score, avg, "", "", class_Rank, class_avg_Rank, grade_Rank, grade_avg_Rank, "");
                    final String mKey = grade + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    _subclassRankMap.put(mKey, subclassRank);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }

        private void setValuation(final DB2UDB db2, final String schoolKind) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            final String pskey = "setValuation";
            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SYUUYAKU_WK_SUBBASE AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBだけでは留年した年のデータが抜けないため、過年度～当年度の各学年で最後の年(最大の年)で一度集約)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBBASEで集約した年度の学期を取得する。)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append("     INNER JOIN SYUUYAKU_WK_SUBBASE TW3 ");
                stb.append("       ON TW3.YEAR = TW1.YEAR ");
                stb.append("      AND TW3.GRADE_CD = TW2.GRADE_CD ");
                stb.append("      AND TW3.SCHREGNO = TW1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
                stb.append(" SELECT ");
                stb.append("    TK1.YEAR, ");
                stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
                stb.append("    TK1.GRADE_CD, ");
                stb.append("    TK1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK1 ");
                stb.append(" WHERE ");
                stb.append("    TK1.YEAR < '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TK1.YEAR, ");
                stb.append("     TK1.GRADE_CD, ");
                stb.append("     TK1.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     TK2.YEAR, ");
                stb.append("     TK2.SEMESTER, ");
                stb.append("     TK2.GRADE_CD, ");
                stb.append("     TK2.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK2 ");
                stb.append(" WHERE ");
                stb.append("    TK2.YEAR = '" + _param._year + "' ");
                stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
                stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
                stb.append("     ON TW1.YEAR = T1.YEAR ");
                stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
                stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.SCHREGNO = ? ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.* ");
                   stb.append(", T2.GRADE ");
                stb.append(" FROM ");
                stb.append("   RECORD_RANK_SDIV_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_SCHREG T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR < '" + _param._year + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
                stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-" + schoolKind + "-99-999999' ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   T1.* ");
                  stb.append(", T2.GRADE ");
                stb.append(" FROM ");
                stb.append("   RECORD_RANK_SDIV_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_SCHREG T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._cutTestCd + "09' ");  //"09"固定で取得
                stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-" + schoolKind + "-99-999999' ");

                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                        log.error("生徒の基本情報取得でエラー", e);
                        throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);
            ResultSet rs = null;

            try {
                ps.setString(1, _schregno);
                rs = ps.executeQuery();
                   while (rs.next()){
                       final String grade = rs.getString("GRADE");
                       final String avg = rs.getString("AVG");
                       _valuationDetailDat.put(grade, avg);
                   }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }
        private void setAttendMonth(final DB2UDB db2) throws SQLException {
            final String pskey = "setAttendMonth";
            if (!_param._psHandleMap.containsKey(pskey)) {
                try {
                    final PreparedStatement ps = db2.prepareStatement(getAttendMonthSql());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);
            ResultSet rs = null;

            try {
                ps.setString(1, _schregno);
                ps.setString(2, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final int month = rs.getInt("MONTH");
                    final String r_month = String.valueOf((month > 3) ? month - 3 : month + 9);       //出欠データより取得した月 ４月を１行目に換算する
                    final int j_nisu = rs.getInt("J_NISU");      //授業日数
                    final int suspend = rs.getInt("SUSPEND") + rs.getInt("MOURNING");    //出停・忌引
                    final int must = rs.getInt("J_NISU") - suspend;    //出席しなければならない日数
                    final int absence = rs.getInt("ABSENCE");    //欠席
                    final int attend = must - rs.getInt("ABSENCE");    //出席回数
                    final int late = rs.getInt("LATE");          //遅刻回数
                    final int early = rs.getInt("EARLY");        //早退回数
                    AttData addwk = new AttData(r_month, String.valueOf(month), j_nisu, suspend, must, absence, attend, late, early);
                    _attendMonthMap.put(r_month, addwk);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }
        private String getAttendMonthSql() {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;

            //  月別集計
            final StringBuffer stb = new StringBuffer();
            stb.append(    "SELECT  ");
            stb.append("        VALUE(T1.SEMESTER, T2.SEMESTER) AS SEMESTER, ");
            stb.append("        VALUE(T1.MONTH,T2.MONTH) AS MONTH,");
            stb.append(        "VALUE(T1.J_NISU,0) AS J_NISU, VALUE(T1.SUSPEND,0) AS SUSPEND, VALUE(T1.MOURNING,0) AS MOURNING, VALUE(T1.LATE,0) AS LATE, VALUE(T1.EARLY,0) AS EARLY, VALUE(T1.ABSENCE,0) AS ABSENCE, ");
            stb.append(        "VALUE(T2.SHR_LATE,0) AS SHR_LATE, ");
            stb.append(        "VALUE(T2.CLASS_LATE,0) AS CLASS_LATE, ");
            stb.append(        "VALUE(T2.CLASS_ABSENCE,0) AS CLASS_ABSENCE, ");
            stb.append(        "VALUE(T2.LHR_LATE,0) AS LHR_LATE ");
            stb.append("FROM (   SELECT  SEMESTER,MONTH, ");
            stb.append(                 "SUM(VALUE(LESSON, 0))AS J_NISU, ");
            stb.append(                 "SUM(VALUE(SUSPEND, 0)) ");
            if ("true".equals(_param._useVirus)) {
                stb.append(                 " + SUM(VALUE(VIRUS, 0)) ");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append(                 " + SUM(VALUE(KOUDOME, 0)) ");
            }
            stb.append(                 " AS SUSPEND,  ");
            stb.append(                 "SUM(VALUE(MOURNING, 0))AS MOURNING, ");
            stb.append(                 "SUM(VALUE(LATE, 0))AS LATE, ");
            stb.append(                 "SUM(VALUE(EARLY, 0))AS EARLY, ");
            stb.append(                 "SUM(VALUE(SICK, 0)+VALUE(NOTICE, 0)+VALUE(NONOTICE, 0)");
            if ("1".equals(_param._knjSchoolMst._semOffDays)) {
            stb.append(                    "+ VALUE(OFFDAYS, 0) ");
            }
            stb.append(                ") AS ABSENCE ");
            //授業日数・出停忌引・病欠・事故欠（届／無届）・ＳＨＲ遅刻
            stb.append(         "FROM    ATTEND_SEMES_DAT ");
            stb.append(         "WHERE   SCHREGNO = ? AND YEAR = '" + _param._year + "' AND SEMESTER <= '" + useSemester + "' ");
            stb.append(         "GROUP BY SEMESTER, MONTH ");
            stb.append(    ") T1 ");

            stb.append(    "FULL JOIN(");
            stb.append(            "SELECT ");
            stb.append(                "SEMESTER,MONTH, ");
            stb.append(                "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_S + "' THEN VALUE(LATE,0) ELSE NULL END) AS SHR_LATE,"); // 92
            stb.append(                "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(LATE,0) "); // 01 ~ 89
            stb.append(                                                            "ELSE NULL END) AS CLASS_LATE,");
            stb.append(                "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(SICK,0) ");
            stb.append(                        "+VALUE(NOTICE,0)+VALUE(NONOTICE,0)+VALUE(NURSEOFF,0) ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                "+ VALUE(OFFDAYS,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                "+ VALUE(SUSPEND, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subVirus)) {
                stb.append(                "+ VALUE(VIRUS, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(                "+ VALUE(KOUDOME, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                "+ VALUE(MOURNING, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(                "+ VALUE(ABSENT, 0) ");
            }
            stb.append(                " ELSE NULL END) AS CLASS_ABSENCE,");
            stb.append(                "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_L + "'THEN VALUE(LATE,0) ELSE NULL END) AS LHR_LATE "); // 94
            stb.append(            "FROM     ATTEND_SUBCLASS_DAT W1 ");
            stb.append(            "WHERE    W1.SCHREGNO = ? AND W1.YEAR='" + _param._year + "'AND W1.SEMESTER <= '" + useSemester + "'");
            stb.append(                "AND (W1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR W1.CLASSCD IN('" + KNJDefineSchool.subject_T + "', '91', '" + KNJDefineSchool.subject_E + "','" + KNJDefineSchool.subject_L + "') ");
            stb.append(                                                        "OR W1.SUBCLASSCD = '" + KNJDefineSchool.subject_S_A + "') ");
            stb.append(            "GROUP BY SEMESTER,MONTH ");
            stb.append(    ") T2 ON T2.SEMESTER = T1.SEMESTER AND T2.MONTH = T1.MONTH ");

            stb.append("ORDER BY SEMESTER, INT(MONTH) + CASE WHEN MONTH < '04' THEN 12 ELSE 0 END");
            return stb.toString();
        }
        private void setAttendGrade(final DB2UDB db2) throws SQLException {
            final String pskey = "setAttendGrade";
            if (!_param._psHandleMap.containsKey(pskey)) {
                try {
                    final PreparedStatement ps = db2.prepareStatement(getAttendGradeSql());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);
            ResultSet rs = null;

            try {
                ps.setString(1, _grade + _hrClass);
                ps.setString(2, _schregno);
                ps.setString(3, _schregno);
                ps.setString(4, _schregno);
                ps.setString(5, _schregno);
                ps.setString(6, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String semester = rs.getString("SEMESTER");
                    if (!SEMEALL.equals(semester)) continue;
                    if ("99".equals(gradeCd)) continue;
                    final String year = rs.getString("YEAR");
                    final int j_nisu = rs.getInt("J_NISU");      //授業日数
                    final int suspend = rs.getInt("SUSPEND") + rs.getInt("MOURNING");    //出停・忌引
                    final int must = rs.getInt("J_NISU") - suspend;    //出席しなければならない日数
                    final int absence = rs.getInt("ABSENCE");    //欠席
                    final int attend = must - rs.getInt("ABSENCE");    //出席回数
                    final int late = rs.getInt("LATE");          //遅刻回数
                    final int early = rs.getInt("EARLY");        //早退回数
                    AttData addwk = new AttData(gradeCd, year, j_nisu, suspend, must, absence, attend, late, early);
                    _attendGradeMap.put(gradeCd, addwk);  //留年時のデータも取得されるが、ソートしているので、上書きされて同学年で最終年度のデータが最後になる。
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }
        private String getAttendGradeSql() {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            //  全体集計
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SYUUYAKU_WK_SUBBASE AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBだけでは留年した年のデータが抜けないため、過年度～当年度の各学年で最後の年(最大の年)で一度集約)
            stb.append(" SELECT ");
            stb.append("     MAX(TW1.YEAR) AS YEAR, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT TW1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
            stb.append("       ON TW2.YEAR = TW1.YEAR ");
            stb.append("      AND TW2.GRADE = TW1.GRADE ");
            stb.append(" WHERE ");
            stb.append("    TW1.YEAR <= '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" ), SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBBASEで集約した年度の学期を取得する。)
            stb.append(" SELECT ");
            stb.append("     MAX(TW1.YEAR) AS YEAR, ");
            stb.append("     TW1.SEMESTER, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT TW1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
            stb.append("       ON TW2.YEAR = TW1.YEAR ");
            stb.append("      AND TW2.GRADE = TW1.GRADE ");
            stb.append("     INNER JOIN SYUUYAKU_WK_SUBBASE TW3 ");
            stb.append("       ON TW3.YEAR = TW1.YEAR ");
            stb.append("      AND TW3.GRADE_CD = TW2.GRADE_CD ");
            stb.append("      AND TW3.SCHREGNO = TW1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("    TW1.YEAR <= '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TW1.SEMESTER, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
            stb.append(" SELECT ");
            stb.append("    TK1.YEAR, ");
            stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
            stb.append("    TK1.GRADE_CD, ");
            stb.append("    TK1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_WK_SUB TK1 ");
            stb.append(" WHERE ");
            stb.append("    TK1.YEAR < '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TK1.YEAR, ");
            stb.append("     TK1.GRADE_CD, ");
            stb.append("     TK1.SCHREGNO ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     TK2.YEAR, ");
            stb.append("     TK2.SEMESTER, ");
            stb.append("     TK2.GRADE_CD, ");
            stb.append("     TK2.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_WK_SUB TK2 ");
            stb.append(" WHERE ");
            stb.append("    TK2.YEAR = '" + _param._year + "' ");
            stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
            stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得(過年度はクラスでは取得できないので、学籍番号で取得しないといけない)
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T2.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.GRADE = T1.GRADE ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
            stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   T2.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
            stb.append("     ON TW1.YEAR = T1.YEAR ");
            stb.append("    AND TW1.SEMESTER = T1.SEMESTER ");
            stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.GRADE = T1.GRADE ");
            stb.append(" WHERE ");
            stb.append("   T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ) ");
            stb.append("SELECT ");
            stb.append(     "T1.YEAR, T1.ANNUAL, T1.GRADE_CD, T1.SEMESTER, T2.SEMESTERNAME, ");
            stb.append(     "VALUE(T3.J_NISU,0) AS J_NISU, ");
            stb.append(     "VALUE(T3.SUSPEND,0) AS SUSPEND, ");
            stb.append(     "VALUE(T3.MOURNING,0) AS MOURNING, ");
            stb.append(     "VALUE(T3.LATE,0) AS LATE, ");
            stb.append(     "VALUE(T3.EARLY,0) AS EARLY, ");
            stb.append(     "VALUE(T3.ABSENCE,0) AS ABSENCE, ");
            stb.append(     "VALUE(T4.SHR_LATE,0) AS SHR_LATE, ");
            stb.append(     "VALUE(T4.CLASS_LATE,0) AS CLASS_LATE, ");
            stb.append(     "VALUE(T4.CLASS_ABSENCE,0) AS CLASS_ABSENCE, ");
            stb.append(     "VALUE(T4.LHR_LATE,0) AS LHR_LATE ");
            stb.append( "FROM ( SELECT    VALUE(REGD.YEAR, '9999') AS YEAR,");
            stb.append(                  "VALUE(REGD.SEMESTER, '9') AS SEMESTER,");
            stb.append(                  "VALUE(REGD.ANNUAL, '99') AS ANNUAL, ");
            stb.append(                  "VALUE(GDAT.GRADE_CD, '99') AS GRADE_CD ");
            stb.append(         "FROM     SCHREG_REGD_DAT REGD ");
            stb.append(         "LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
            stb.append(         "WHERE    REGD.SCHREGNO = ? AND REGD.YEAR IN (");
            stb.append(                         "SELECT   MAX(YEAR) AS YEAR ");
            stb.append(                         "FROM     SCHREG_REGD_DAT ");
            stb.append(                         "WHERE    SCHREGNO = ? AND (YEAR < '" + _param._year + "' OR (YEAR = '" + _param._year + "' AND SEMESTER <= '" + useSemester + "')) ");
            stb.append(                         "GROUP BY ANNUAL) ");
            stb.append("                  AND REGD.YEAR IN (SELECT YEAR FROM SYUUYAKU_SCHREG )");
            stb.append(         "GROUP BY GROUPING SETS ");
            stb.append(             "((REGD.ANNUAL, GDAT.GRADE_CD, REGD.YEAR, REGD.SEMESTER), (REGD.ANNUAL, GDAT.GRADE_CD, REGD.YEAR), ()) ");
            stb.append(     ") T1 ");
            stb.append(     "LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(     "LEFT JOIN (SELECT  ");
            stb.append(                     "VALUE(YEAR,'9999') AS YEAR, ");
            stb.append(                     "VALUE(SEMESTER,'9') AS SEMESTER,");
            stb.append(                     "SUM(VALUE(LESSON, 0))  AS J_NISU, ");
            stb.append(                     "SUM(VALUE(SUSPEND, 0)) ");
            if ("true".equals(_param._useVirus)) {
                stb.append(                 " + SUM(VALUE(VIRUS, 0)) ");
            }
            if ("true".equals(_param._useKoudome)) {
                stb.append(                 " + SUM(VALUE(KOUDOME, 0)) ");
            }
            stb.append(                     " AS SUSPEND,  ");
            stb.append(                     "SUM(VALUE(MOURNING, 0)) AS MOURNING, ");
            stb.append(                     "SUM(VALUE(LATE, 0)) AS LATE, ");
            stb.append(                     "SUM(VALUE(EARLY, 0)) AS EARLY, ");
            stb.append(                     "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(_param._knjSchoolMst._semOffDays)) {
                stb.append(                    "+ VALUE(OFFDAYS,0) ");
            }
            stb.append(                    ") AS ABSENCE ");
            //授業日数・出停忌引・病欠・事故欠（届／無届）・ＳＨＲ遅刻
            stb.append(             "FROM     ATTEND_SEMES_DAT ");
            stb.append(             "WHERE    SCHREGNO = ? AND (YEAR < '" + _param._year + "' OR (YEAR = '" + _param._year + "' AND SEMESTER <= '" + useSemester + "')) ");
            stb.append(             "GROUP BY GROUPING SETS((YEAR,SEMESTER),(YEAR),())");
            stb.append(     ") T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");

            stb.append(     "LEFT JOIN (SELECT ");
            stb.append(                 "VALUE(YEAR,'9999') AS YEAR, ");
            stb.append(                 "VALUE(SEMESTER,'9') AS SEMESTER,");
            stb.append(                 "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_S + "' THEN VALUE(LATE,0) ELSE NULL END) AS SHR_LATE,");
            stb.append(                 "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(LATE,0) ELSE NULL END) AS CLASS_LATE,");

            stb.append(                 "SUM(CASE WHEN CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' THEN VALUE(SICK,0) ");
            stb.append(                         "+ VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                        "+ VALUE(OFFDAYS,0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                        "+ VALUE(SUSPEND, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subVirus)) {
                stb.append(                        "+ VALUE(VIRUS, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(                        "+ VALUE(KOUDOME, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                        "+ VALUE(MOURNING, 0) ");
            }
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(                        "+ VALUE(ABSENT, 0) ");
            }
            stb.append(                        "ELSE NULL END) AS CLASS_ABSENCE,");
            stb.append(                 "SUM(CASE WHEN CLASSCD = '" + KNJDefineSchool.subject_L + "' THEN VALUE(LATE,0) ELSE NULL END) AS LHR_LATE ");
            stb.append(             "FROM     ATTEND_SUBCLASS_DAT ");
            stb.append(             "WHERE    SCHREGNO = ? AND (YEAR < '" + _param._year + "' OR (YEAR = '" + _param._year + "' AND SEMESTER <= '" + useSemester + "')) ");
            stb.append(                         "AND(CLASSCD BETWEEN'" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR ");
            stb.append(                             "CLASSCD IN ('" + KNJDefineSchool.subject_T + "', '91', '" + KNJDefineSchool.subject_E + "', '" + KNJDefineSchool.subject_L + "', '96') OR SUBCLASSCD = '" + KNJDefineSchool.subject_S + "0100') ");
            stb.append(             "GROUP BY GROUPING SETS((YEAR,SEMESTER),(YEAR),())");
            stb.append(     ") T4  ON T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ");

            stb.append( "ORDER BY T1.ANNUAL, T1.GRADE_CD, T1.YEAR, T1.SEMESTER");
            return stb.toString();
        }
        private void setAttendRemarks(final DB2UDB db2) throws SQLException {
            final String pskey = "setAttendRemarks";
            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   SEMESTER, ");
                stb.append("   MONTH, ");
                stb.append("   REMARK1 ");
                stb.append(" FROM ");
                stb.append("   ATTEND_SEMES_REMARK_DAT ");
                stb.append(" WHERE ");
                stb.append("   YEAR = '" + _param._year + "' AND ");
                stb.append("   SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("   YEAR, ");
                stb.append("   SEMESTER, ");
                stb.append("   VALUE(MONTH, 0) > 3 DESC, ");  //4月以上優先
                stb.append("   VALUE(MONTH, 0) <= 3 ASC, ");   //3月以下のソート
                stb.append("   MONTH ");   //上記分離した内部のソート
                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);
            ResultSet rs = null;

            try {
                ps.setString(1, _schregno);
                rs = ps.executeQuery();
                List addwkList = null;
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    //final String month = rs.getString("MONTH");
                    final String remark = rs.getString("REMARK1");

                    if (_attendRemarkMap.containsKey(semester)) {
                        addwkList = (List)_attendRemarkMap.get(semester);
                    } else {
                        addwkList = new ArrayList();
                        _attendRemarkMap.put(semester, addwkList);
                    }
                    addwkList.add(remark);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }
        private void setTotalStudyTime(final DB2UDB db2) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            final String pskey = "setTotalStudyTime";
            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" select ");
                stb.append("  T2.TOTALSTUDYTIME, ");
                stb.append("  T2.TOTALSTUDYACT, ");
                stb.append("  T3.REMARK1 ");
                stb.append(" from ");
                stb.append("  SCHREG_REGD_DAT T1 ");
                stb.append("  LEFT JOIN RECORD_TOTALSTUDYTIME_DAT T2 ");
                stb.append("    ON T2.YEAR = T1.YEAR ");
                stb.append("   AND T2.SEMESTER = '" + _param._semester + "' ");
                stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("  LEFT JOIN HEXAM_RECORD_REMARK_SDIV_DAT T3 ");
                stb.append("    ON T3.YEAR = T1.YEAR ");
                stb.append("   AND T3.SEMESTER = '" + _param._semester + "' ");  //指定学期で(9学期なら9学期)で取得。
                stb.append("   AND T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("   AND T3.REMARK_DIV = '4' ");
                stb.append("   AND T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + _param._testcd + "' ");
                stb.append(" WHERE ");
                stb.append("  T1.YEAR = '" + _param._year + "' ");
                stb.append("  AND T1.SEMESTER = '" + useSemester + "' ");
                stb.append("  AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY T2.SUBCLASSCD DESC ");  //90系優先のため、降順にして先頭データを取得。
                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);
            ResultSet rs = null;

            try {
                ps.setString(1, _schregno);
                rs = ps.executeQuery();
                if (rs.next()) {  //1件だけ取得。
                    _totalStudyTime = rs.getString("TOTALSTUDYTIME");
                    _totalStudyAct = rs.getString("TOTALSTUDYACT");
                    _remark1 = rs.getString("REMARK1");
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }

        public void setSubclassInfo(final DB2UDB db2, final String ghrClass, final String schregno) throws SQLException {
            final String useSemester = SEMEALL.equals(_param._semester) ? _param._ctrlSeme : _param._semester;
            ResultSet rs = null;
            final String pskey = "setSubclassInfo";
            if (!_param._psHandleMap.containsKey(pskey)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SYUUYAKU_WK_SUBBASE AS ( ");  // 留年を加味したデータにする(SYUUYAKU_WK_SUBだけでは留年した年のデータが抜けないため、過年度～当年度の各学年で最後の年(最大の年)で一度集約)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜く)
                stb.append(" SELECT ");
                stb.append("     MAX(TW1.YEAR) AS YEAR, ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW2.SCHOOL_KIND, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT TW1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("       ON TW2.YEAR = TW1.YEAR ");
                stb.append("      AND TW2.GRADE = TW1.GRADE ");
                stb.append("     INNER JOIN SYUUYAKU_WK_SUBBASE TW3 ");
                stb.append("       ON TW3.YEAR = TW1.YEAR ");
                stb.append("      AND TW3.GRADE_CD = TW2.GRADE_CD ");
                stb.append("      AND TW3.SCHREGNO = TW1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("    TW1.YEAR <= '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TW2.SCHOOL_KIND, ");
                stb.append("     TW2.GRADE_CD, ");
                stb.append("     TW1.SEMESTER, ");
                stb.append("     TW1.SCHREGNO ");
                stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、紐づけるデータが重複しないようにするために、過年度分は最終学期、当年度分は指定学期までのデータだけを抽出
                stb.append(" SELECT ");
                stb.append("    TK1.YEAR, ");
                stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
                stb.append("    TK1.GRADE_CD, ");
                stb.append("    TK1.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK1 ");
                stb.append(" WHERE ");
                stb.append("    TK1.YEAR < '" + _param._year + "' ");
                stb.append(" GROUP BY ");
                stb.append("     TK1.YEAR, ");
                stb.append("     TK1.GRADE_CD, ");
                stb.append("     TK1.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     TK2.YEAR, ");
                stb.append("     TK2.SEMESTER, ");
                stb.append("     TK2.GRADE_CD, ");
                stb.append("     TK2.SCHREGNO ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_WK_SUB TK2 ");
                stb.append(" WHERE ");
                stb.append("    TK2.YEAR = '" + _param._year + "' ");
                stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
                stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.GRADE_CD ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
                stb.append("     ON TW1.YEAR = T1.YEAR ");
                stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
                stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR <= '" + _param._year + "' ");
                stb.append("   AND T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
                stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_FILTER) ");
                stb.append(" ), REPL AS ( ");  //元科目、先科目情報
                stb.append("  SELECT ");
                stb.append("   YEAR, ");
                stb.append("   '1' AS DIV, ");
                stb.append("    COMBINED_CLASSCD AS CLASSCD, ");
                stb.append("    COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
                stb.append("    COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
                stb.append("    COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("    CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG ");
                stb.append("  FROM ");
                stb.append("    SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("  GROUP BY ");
                stb.append("    YEAR, ");
                stb.append("    COMBINED_CLASSCD, ");
                stb.append("    COMBINED_SCHOOL_KIND, ");
                stb.append("    COMBINED_CURRICULUM_CD, ");
                stb.append("    COMBINED_SUBCLASSCD ");
                stb.append("  UNION ");
                stb.append("  SELECT ");
                stb.append("   YEAR, ");
                stb.append("    '2' AS DIV, ");
                stb.append("    ATTEND_CLASSCD AS CLASSCD, ");
                stb.append("    ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ");
                stb.append("    ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ");
                stb.append("    ATTEND_SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("    MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG ");
                stb.append("  FROM ");
                stb.append("    SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("  GROUP BY ");
                stb.append("    YEAR, ");
                stb.append("    ATTEND_CLASSCD, ");
                stb.append("    ATTEND_SCHOOL_KIND, ");
                stb.append("    ATTEND_CURRICULUM_CD, ");
                stb.append("    ATTEND_SUBCLASSCD ");
                stb.append(" ), CREDIT_CASE1_MST AS ( ");  //1年の時の単位
                stb.append(" SELECT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   C1_1.CLASSCD, ");
                stb.append("   C1_1.SCHOOL_KIND, ");
                stb.append("   C1_1.CURRICULUM_CD, ");
                stb.append("   C1_1.SUBCLASSCD, ");
                stb.append("   C1_1.CREDITS ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   INNER JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("     ON TW2.YEAR = T1.YEAR ");
                stb.append("    AND TW2.GRADE = T1.GRADE ");
                stb.append("    AND TW2.GRADE_CD = '01' ");
                stb.append("   LEFT JOIN CREDIT_MST C1_1 ");
                stb.append("     ON C1_1.YEAR = T1.YEAR ");
                stb.append("    AND C1_1.COURSECD = T1.COURSECD ");
                stb.append("    AND C1_1.MAJORCD = T1.MAJORCD ");
                stb.append("    AND C1_1.GRADE = T1.GRADE ");
                stb.append("    AND C1_1.COURSECODE = T1.COURSECODE ");
                stb.append(" ), CREDIT_CASE2_MST AS ( ");  //2年の時の単位
                stb.append(" SELECT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   C1_1.CLASSCD, ");
                stb.append("   C1_1.SCHOOL_KIND, ");
                stb.append("   C1_1.CURRICULUM_CD, ");
                stb.append("   C1_1.SUBCLASSCD, ");
                stb.append("   C1_1.CREDITS ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   INNER JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("     ON TW2.YEAR = T1.YEAR ");
                stb.append("    AND TW2.GRADE = T1.GRADE ");
                stb.append("    AND TW2.GRADE_CD = '02' ");
                stb.append("   LEFT JOIN CREDIT_MST C1_1 ");
                stb.append("     ON C1_1.YEAR = T1.YEAR ");
                stb.append("    AND C1_1.COURSECD = T1.COURSECD ");
                stb.append("    AND C1_1.MAJORCD = T1.MAJORCD ");
                stb.append("    AND C1_1.GRADE = T1.GRADE ");
                stb.append("    AND C1_1.COURSECODE = T1.COURSECODE ");
                stb.append(" ), CREDIT_CASE3_MST AS ( ");  //3年の時の単位
                stb.append(" SELECT ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   C1_1.CLASSCD, ");
                stb.append("   C1_1.SCHOOL_KIND, ");
                stb.append("   C1_1.CURRICULUM_CD, ");
                stb.append("   C1_1.SUBCLASSCD, ");
                stb.append("   C1_1.CREDITS ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   INNER JOIN SCHREG_REGD_GDAT TW2 ");
                stb.append("     ON TW2.YEAR = T1.YEAR ");
                stb.append("    AND TW2.GRADE = T1.GRADE ");
                stb.append("    AND TW2.GRADE_CD = '03' ");
                stb.append("   LEFT JOIN CREDIT_MST C1_1 ");
                stb.append("     ON C1_1.YEAR = T1.YEAR ");
                stb.append("    AND C1_1.COURSECD = T1.COURSECD ");
                stb.append("    AND C1_1.MAJORCD = T1.MAJORCD ");
                stb.append("    AND C1_1.GRADE = T1.GRADE ");
                stb.append("    AND C1_1.COURSECODE = T1.COURSECODE ");
                stb.append(" ), SUBCLSTBL_BASE AS ( ");
                stb.append(" select ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   L1.CLASSNAME, ");
                stb.append("   L1.CLASSABBV, ");
                stb.append("   L2.SUBCLASSNAME, ");
                stb.append("   L2.SUBCLASSABBV, ");
                stb.append("   L2.SUBCLASSORDERNAME2 ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN SCHREG_STUDYREC_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
                stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   LEFT JOIN CLASS_MST L1 ");
                stb.append("     ON L1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   LEFT JOIN SUBCLASS_MST L2 ");
                stb.append("     ON L2.CLASSCD = T2.CLASSCD ");
                stb.append("    AND L2.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("    AND L2.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("    AND L2.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR < '" + _param._year + "' ");
                stb.append("   AND T2.CLASSCD <= '90' ");
                stb.append("   AND T2.YEAR IS NOT NULL ");
                stb.append(" UNION ");
                // 当年度データ
                stb.append(" select ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   L1.CLASSNAME, ");
                stb.append("   L1.CLASSABBV, ");
                stb.append("   L2.SUBCLASSNAME, ");
                stb.append("   L2.SUBCLASSABBV, ");
                stb.append("   L2.SUBCLASSORDERNAME2 ");
                stb.append(" FROM ");
                stb.append("   SYUUYAKU_SCHREG T1 ");
                stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
                stb.append("     ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
                if ("1".equals(_param._useProvFlg)) {
                    stb.append("   LEFT JOIN RECORD_PROV_FLG_DAT P1 ");
                    stb.append("     ON P1.YEAR = T2.YEAR ");
                    stb.append("    AND P1.CLASSCD = T2.CLASSCD ");
                    stb.append("    AND P1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb.append("    AND P1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                    stb.append("    AND P1.SUBCLASSCD = T2.SUBCLASSCD ");
                    stb.append("    AND P1.SCHREGNO = T2.SCHREGNO ");
                }
                stb.append("   LEFT JOIN CLASS_MST L1 ");
                stb.append("     ON L1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   LEFT JOIN SUBCLASS_MST L2 ");
                stb.append("     ON L2.CLASSCD = T2.CLASSCD ");
                stb.append("    AND L2.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("    AND L2.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("    AND L2.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _param._year + "' ");
                stb.append("   AND T2.CLASSCD <= '90' ");
                stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
                stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _param._cutTestCd + "09' ");
                stb.append("   AND T2.YEAR IS NOT NULL ");
                stb.append("   AND T2.SUBCLASSCD NOT IN ('333333', '777777', '99999A', '99999B') ");
                stb.append("   AND T2.SUBCLASSCD NOT IN ('" + ALL9 + "', '" + ALL5 + "') ");
                stb.append(" GROUP BY ");
                stb.append("   T2.SCHREGNO, ");
                stb.append("   T2.CLASSCD, ");
                stb.append("   T2.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T2.SUBCLASSCD, ");
                stb.append("   L1.CLASSNAME, ");
                stb.append("   L1.CLASSABBV, ");
                stb.append("   L2.SUBCLASSNAME, ");
                stb.append("   L2.SUBCLASSABBV, ");
                stb.append("   L2.SUBCLASSORDERNAME2 ");
                stb.append(" ), KEKKA AS ( ");
                stb.append(" SELECT ");
                stb.append("   T1.*, ");
                stb.append("   CASE WHEN T2_1.YEAR = '" + _param._year + "' THEN D1_1.COMP_CREDIT WHEN T2_1.YEAR < '" + _param._year + "' THEN C1_1.COMP_CREDIT ELSE NULL END AS CREDITS1, ");
                stb.append("   CASE WHEN T2_2.YEAR = '" + _param._year + "' THEN D1_2.COMP_CREDIT WHEN T2_2.YEAR < '" + _param._year + "' THEN C1_2.COMP_CREDIT ELSE NULL END AS CREDITS2, ");
                stb.append("   D1_3.COMP_CREDIT AS CREDITS3 ");
                stb.append(" FROM ");
                stb.append("   SUBCLSTBL_BASE T1 ");
                stb.append("   LEFT JOIN SYUUYAKU_SCHREG T2_1 ");
                stb.append("      ON T2_1.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2_1.GRADE_CD = '01' ");
                stb.append("   LEFT JOIN SCHREG_STUDYREC_DAT C1_1 ");
                stb.append("     ON C1_1.YEAR = T2_1.YEAR ");
                stb.append("    AND C1_1.ANNUAL = T2_1.ANNUAL ");
                stb.append("    AND C1_1.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND C1_1.CLASSCD = T1.CLASSCD ");
                stb.append("    AND C1_1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND C1_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND C1_1.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   LEFT JOIN RECORD_SCORE_DAT D1_1 ");
                stb.append("     ON D1_1.YEAR = T2_1.YEAR ");
                stb.append("    AND D1_1.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND D1_1.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND D1_1.TESTKINDCD || D1_1.TESTITEMCD = '" + _param._cutTestCd + "' ");
                stb.append("    AND D1_1.SCORE_DIV = '09' ");
                stb.append("    AND D1_1.CLASSCD = T1.CLASSCD ");
                stb.append("    AND D1_1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND D1_1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND D1_1.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   LEFT JOIN SYUUYAKU_SCHREG T2_2 ");
                stb.append("      ON T2_2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2_2.GRADE_CD = '02' ");
                stb.append("   LEFT JOIN SCHREG_STUDYREC_DAT C1_2 ");
                stb.append("     ON C1_2.YEAR = T2_2.YEAR ");
                stb.append("    AND C1_2.ANNUAL = T2_2.ANNUAL ");
                stb.append("    AND C1_2.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND C1_2.CLASSCD = T1.CLASSCD ");
                stb.append("    AND C1_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND C1_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND C1_2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   LEFT JOIN RECORD_SCORE_DAT D1_2 ");
                stb.append("     ON D1_2.YEAR = T2_2.YEAR ");
                stb.append("    AND D1_2.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND D1_2.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND D1_2.TESTKINDCD || D1_2.TESTITEMCD = '" + _param._cutTestCd + "' ");
                stb.append("    AND D1_2.SCORE_DIV = '09' ");
                stb.append("    AND D1_2.CLASSCD = T1.CLASSCD ");
                stb.append("    AND D1_2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND D1_2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND D1_2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("   LEFT JOIN SYUUYAKU_SCHREG T2_3 ");
                stb.append("      ON T2_3.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2_3.GRADE_CD = '03' ");
                stb.append("   LEFT JOIN RECORD_SCORE_DAT D1_3 ");
                stb.append("     ON D1_3.YEAR = T2_3.YEAR ");
                stb.append("    AND D1_3.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND D1_3.SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND D1_3.TESTKINDCD || D1_3.TESTITEMCD = '" + _param._cutTestCd + "' ");
                stb.append("    AND D1_3.SCORE_DIV = '09' ");
                stb.append("    AND D1_3.CLASSCD = T1.CLASSCD ");
                stb.append("    AND D1_3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND D1_3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("    AND D1_3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" ORDER BY ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   T1.CURRICULUM_CD, ");
                stb.append("   T1.SUBCLASSCD ");
                stb.append(" ), SOUGOU AS ("); //総合学習
                stb.append(" SELECT ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   MIN(T1.SUBCLASSCD) AS SUBCLASSCD");
                stb.append(" FROM ");
                stb.append("   KEKKA T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.CLASSCD = '90' ");
                stb.append(" GROUP BY ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SCHOOL_KIND ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   T1.CURRICULUM_CD, ");
                stb.append("   T1.SUBCLASSCD, ");
                stb.append("   T1.CLASSNAME, ");
                stb.append("   T1.CLASSABBV, ");
                stb.append("   T1.SUBCLASSNAME, ");
                stb.append("   T1.SUBCLASSABBV, ");
                stb.append("   T1.SUBCLASSORDERNAME2, ");
                stb.append("   MIN(T1.CREDITS1) AS CREDITS1, ");
                stb.append("   MIN(T1.CREDITS2) AS CREDITS2, ");
                stb.append("   MIN(T1.CREDITS3) AS CREDITS3");
                stb.append(" FROM ");
                stb.append("   KEKKA T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.CLASSCD < '90' ");
                stb.append(" GROUP BY ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   T1.CURRICULUM_CD, ");
                stb.append("   T1.SUBCLASSCD, ");
                stb.append("   T1.CLASSNAME, ");
                stb.append("   T1.CLASSABBV, ");
                stb.append("   T1.SUBCLASSNAME, ");
                stb.append("   T1.SUBCLASSABBV, ");
                stb.append("   T1.SUBCLASSORDERNAME2 ");
                //総合学習
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   T2.CURRICULUM_CD, ");
                stb.append("   T1.SUBCLASSCD, ");
                stb.append("   T2.CLASSNAME, ");
                stb.append("   T2.CLASSABBV, ");
                stb.append("   T2.SUBCLASSNAME, ");
                stb.append("   T2.SUBCLASSABBV, ");
                stb.append("   T2.SUBCLASSORDERNAME2, ");
                stb.append("   T2.CREDITS1 AS CREDITS1, ");
                stb.append("   T2.CREDITS2 AS CREDITS2, ");
                stb.append("   T2.CREDITS3 AS CREDITS3");
                stb.append(" FROM ");
                stb.append("   SOUGOU T1 ");
                stb.append("   LEFT JOIN KEKKA T2 ");
                stb.append("   ON T1.CLASSCD = T2.CLASSCD ");
                stb.append("   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND T1.SUBCLASSCD = T2.SUBCLASSCD ");

                try {
                    final PreparedStatement ps = db2.prepareStatement(stb.toString());
                    _param._psHandleMap.put(pskey, ps);
                } catch (final SQLException e) {
                    log.error("生徒の基本情報取得でエラー", e);
                    throw e;
                }
            }
            final PreparedStatement ps = (PreparedStatement)_param._psHandleMap.get(pskey);

            Map subMap = null;
            try {
                ps.setString(1, ghrClass);
                ps.setString(2, schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String classabbv = rs.getString("CLASSABBV");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final String subclassOrderName2 = rs.getString("SUBCLASSORDERNAME2");
                    final String credits1 = rs.getString("CREDITS1");
                    final String credits2 = rs.getString("CREDITS2");
                    final String credits3 = rs.getString("CREDITS3");
                    final boolean isSaki = false;
                    final boolean isMoto = false;
                    SubclsInfo addwk = new SubclsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, classname, classabbv, subclassname, subclassabbv, subclassOrderName2, isSaki, isMoto, credits1, credits2, credits3);
                    final String mKey = classcd + "-" + school_Kind;
                    if (_subclsInfoMap.containsKey(mKey)) {
                        subMap = (Map)_subclsInfoMap.get(mKey);
                    } else {
                        subMap = new LinkedMap();
                        _subclsInfoMap.put(mKey, subMap);
                    }
                    final String subKey = classcd + "-" + school_Kind + "-" + curriculum_Cd  + "-" + subclasscd;
                    subMap.put(subKey, addwk);
                    log.info(subKey + " " + subclassabbv + "," + credits1 + "," + credits2 + "," + credits3);
                }
                for (Iterator ite = _subclsInfoMap.keySet().iterator();ite.hasNext();) {
                    final String kStr = (String)ite.next();
                    Map chkMap = (Map)_subclsInfoMap.get(kStr);
                    _subclsInfoMapCnt += chkMap.size();
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            }
        }
        private Map getFilteredSubClsMap(final String midKstr) {
            Map retMap = new LinkedMap();
            if (!_subclsInfoMap.containsKey(midKstr)) return retMap;
            Map srchMap = (Map)_subclsInfoMap.get(midKstr);
            for (Iterator ite = srchMap.keySet().iterator();ite.hasNext();) {
                final String subclassCd = (String)ite.next();
                if (!_param._ignoreSubclsList.contains(subclassCd)) {
                    SubclsInfo okikaeObj = (SubclsInfo)srchMap.get(subclassCd);
                    retMap.put(subclassCd, okikaeObj);
                }
            }
            return retMap;
        }
    }

    private class SubclassValuation {
        final String _year;
        final String _grade;
        final String _schregno;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _prov_Flg;
        final String _score;
        public SubclassValuation (final String year, final String grade, final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String prov_Flg, final String score)
        {
            _year = year;
            _grade = grade;
            _schregno = schregno;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _prov_Flg = prov_Flg;
            _score = score;
        }
    }

    private class SubclassRank {
        final String _year;
        final String _grade;
        final String _schregno;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _score;
        final String _avg;
        final String _clsAvg;
        final String _grdAvg;
        final String _class_Rank;
        final String _clsAvg_Rank;
        final String _grade_Rank;
        final String _grdAvg_Rank;
        final String _testItemName;

        public SubclassRank (final String year, final String grade, final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd,
                              final String subclasscd, final String score, final String avg, final String clsAvg, final String grdAvg, final String class_Rank, final String clsAvg_Rank, final String grade_Rank, final String grdAvg_Rank,
                              final String testItemName)
        {
            _year = year;
            _grade = grade;
            _schregno = schregno;
            _classcd = classcd;
            _schoolKind = school_Kind;
            _curriculumCd = curriculum_Cd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _clsAvg = clsAvg;
            _grdAvg = grdAvg;
            _class_Rank = class_Rank;
            _clsAvg_Rank = clsAvg_Rank;
            _grade_Rank = grade_Rank;
            _grdAvg_Rank = grdAvg_Rank;
            _testItemName = testItemName;
        }

        public String getKey() {
            return _classcd + _schoolKind + _curriculumCd + _subclasscd;
        }
    }

    private class SubclsInfo {
        final String _classCd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclassCd;
        final String _className;
        final String _classAbbv;
        final String _subclassName;
        final String _subclassAbbv;
        final String _subclassOrder2;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _credits1;
        final String _credits2;
        final String _credits3;
        public SubclsInfo (final String classCd, final String school_Kind, final String curriculum_Cd, final String subclassCd, final String className, final String classAbbv
                            , final String subclassName, final String subclassAbbv, final String subclassOrder2, final boolean isSaki, final boolean isMoto, final String credits1, final String credits2, final String credits3)
        {
            _classCd = classCd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclassCd = subclassCd;
            _className = className;
            _classAbbv = classAbbv;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
            _subclassOrder2 = subclassOrder2;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _credits1 = credits1;
            _credits2 = credits2;
            _credits3 = credits3;
        }
    }

    private class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private class AssessDat {
        final String _assesslevel;
        final String _assessmark;
        final String _assesslow;
        final String _assesshigh;
        public AssessDat (final String assesslevel, final String assessmark, final String assesslow, final String assesshigh)
        {
            _assesslevel = assesslevel;
            _assessmark = assessmark;
            _assesslow = assesslow;
            _assesshigh = assesshigh;
        }
    }

    private class totalCalc {
        BigDecimal _totalVal;
        int _cnt;
        totalCalc() {
            _totalVal = new BigDecimal(0.0);
            _cnt = 0;
        }
        private void add(final String addwk) {
            if (!"".equals(StringUtils.defaultString(addwk, ""))) {
                _totalVal = _totalVal.add(new BigDecimal(addwk));
                _cnt++;
            }
        }
        private BigDecimal calc() {

            if (_cnt == 0) {
                return null;
            } else {
                return _totalVal.divide(new BigDecimal(_cnt), 1, BigDecimal.ROUND_HALF_UP);
            }
        }

    }

    private class ValuationLevel {
        final String _levCd;
        final String _name;
        final String _levelMark;
        final String _levelMin;
        final String _levelMax;
        private ValuationLevel (final String levCd, final String name, final String levelMark, final String levelMin, final String levelMax)
        {
            _levCd = levCd;
            _name = name;
            _levelMark = levelMark;
            _levelMin = levelMin;
            _levelMax = levelMax;
        }
    }

    private class AttData {
        final String _index;
        final String _month;
        int _j_Nisu;
        int _suspend;
        int _must;
        int _absence;
        int _attend;
        int _late;
        int _early;
        public AttData (final String index, final String month, final int j_Nisu, final int suspend, final int must, final int absence, final int attend, final int late, final int early)
        {
            _index = index;
            _month = month;
            _j_Nisu = j_Nisu;
            _suspend = suspend;
            _must = must;
            _absence = absence;
            _attend = attend;
            _late = late;
            _early = early;
        }
        private void clr() {
            _j_Nisu = 0;
            _suspend = 0;
            _must = 0;
            _absence = 0;
            _attend = 0;
            _late = 0;
            _early = 0;
        }
        private void add(final AttData dat) {
            _j_Nisu += dat._j_Nisu;
            _suspend += dat._suspend;
            _must += dat._must;
            _absence += dat._absence;
            _attend += dat._attend;
            _late += dat._late;
            _early += dat._early;
        }
    }



    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75905 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ctrlSeme;
        final String _ctrlDate;
        final String _semester;
        final String _semesterName;
        final String _testcd;
        final String _cutTestCd;
        final String _cutScoreDiv;
        final String[] _categorySelected;
        final String _gradeHrClass;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _schoolKind;
        final String _z010;
        final boolean _isKomazawa;

        final String _useVirus;
        final String _useKoudome;
        final String _useProvFlg;
        final String _use_remark1_moji;
        final String _use_remark1_gyou;

        final String _lastSemester;

        final Map _certifInfo;
        final Map _gradeMap;
        final List _testCdList;
        final List _assessList;
        final List _valuationLevList;
        private Map _semesterMap;

        final Map _psHandleMap;

        private KNJSchoolMst _knjSchoolMst;

        final List _ignoreSubclsList;  //真ん中の表で、出力対象外となるSubclassCd

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass =  request.getParameter("GRADE_HR_CLASS");
            _grade = StringUtils.substring(_gradeHrClass, 0, 2);
            _hrClass = StringUtils.substring(_gradeHrClass, 3);
            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            log.info(" z010 = " + _z010);
            _isKomazawa = "koma".equals(_z010);

            if(_isKomazawa) {
                if(!"9".equals(_semester) && "990008".equals(request.getParameter("TEST_CD"))){
                    _testcd = "020108";
                } else {
                    _testcd = request.getParameter("TEST_CD");
                }
            } else {
                _testcd = request.getParameter("TEST_CD");
            }
            log.info("test_cd = " + _testcd);

            _cutTestCd = StringUtils.substring(_testcd, 0, 4);
            _cutScoreDiv = StringUtils.substring(_testcd, 4, 6);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _schoolKind = request.getParameter("setSchoolKind");

            _useProvFlg = request.getParameter("useProvFlg");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _use_remark1_moji = request.getParameter("hexam_record_remark_dat_remark1_moji");
            _use_remark1_gyou = request.getParameter("hexam_record_remark_dat_remark1_gyou");

            _psHandleMap = new HashMap();

            _lastSemester = getLastSemester(db2);
            _semesterName = getSemesterName(db2);
            _certifInfo = getCertifInfo(db2);
            _gradeMap = getGradeMap(db2);
            _assessList = getAssessMst(db2);
            _semesterMap = loadSemester(db2, _year, _grade);
            _valuationLevList = getValuationLevel(db2, _year);
            _testCdList = getTestCd();
            _gradeCd = getGradeCd(db2);

            _ignoreSubclsList = setIgnoreSubclsCd(db2);
            try {
                final Map smParamMap = new HashMap();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                    final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
                    smParamMap.put("SCHOOL_KIND", schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
        }

        private String getGradeCd(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "' "));
        }
        private List getTestCd() {
            final List retList = new ArrayList();

            if(_isKomazawa) {
                retList.add("1-010108");
                retList.add("1-020108");
                retList.add("2-010108");
                retList.add("2-020108");
                retList.add("9-990008");
            } else {
                retList.add("1-010108");
                retList.add("1-990008");
                retList.add("2-010108");
                retList.add("2-990008");
                retList.add("9-990008");
            }

            return retList;
        }
        private String getLastSemester(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT MAX(SEMESTER) FROM V_SEMESTER_GRADE_MST WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "' AND SEMESTER <> '9' "));

        }
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("loadSemester exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private Map getGradeMap(final DB2UDB db2) {
            Map retMap = new LinkedMap();
            final String sql = "SELECT GRADE_CD, GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = 'H' ORDER BY GRADE_CD, GRADE";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String grade = rs.getString("GRADE");
                    if (!retMap.containsKey(gradeCd)) {
                        retMap.put(gradeCd, grade);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.info("GradeKey = " + retMap.keySet());
            log.info("GradeVal = " + retMap.values());
            return retMap;
        }

        private List getValuationLevel(final DB2UDB db2, final String year) {
            List rtnList = new ArrayList();
            ValuationLevel addwk = null;
            addwk = new ValuationLevel("11", "", "Ａ", "", "");
            rtnList.add(addwk);
            addwk = new ValuationLevel("22", "", "Ｂ", "", "");
            rtnList.add(addwk);
            addwk = new ValuationLevel("33", "", "Ｃ", "", "");
            rtnList.add(addwk);

            return rtnList;
        }
        private String findValuationLevel(final String asVal) {
            String retStr = "";
            if (asVal != null && _valuationLevList.size() > 0) {
                for (Iterator ite = _valuationLevList.iterator();ite.hasNext();) {
                    ValuationLevel getObj = (ValuationLevel)ite.next();
                    if (asVal.equals(getObj._levCd)) {
                        retStr = getObj._levelMark;
                        break;
                    }
                }
            }
            return retStr;
        }

        private String getSemesterName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }

        private Map getCertifInfo(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT * from CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + ("H".equals(_schoolKind) ? "104" : "103") + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
                    final String kStr1 = "SCHOOL_NAME";
                    retMap.put(kStr1, rs.getString(kStr1));
                    final String kStr2 = "JOB_NAME";
                    retMap.put(kStr2, rs.getString(kStr2));
                    final String kStr3 = "PRINCIPAL_NAME";
                    retMap.put(kStr3, rs.getString(kStr3));
                }
            } catch (SQLException ex) {
                log.debug("getCertifInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private List getAssessMst(final DB2UDB db2) {
            final List retList = new ArrayList();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   ASSESSLEVEL, ");
            stb.append("   ASSESSMARK, ");
            stb.append("   ASSESSLOW, ");
            stb.append("   ASSESSHIGH ");
            stb.append(" FROM ");
            stb.append("   ASSESS_MST ");
            stb.append(" WHERE ");
            stb.append("   ASSESSCD = '4' ");
            stb.append(" ORDER BY ");
            stb.append("   ASSESSLOW ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
                    final String assesslevel = rs.getString("ASSESSLEVEL");
                    final String assessmark = rs.getString("ASSESSMARK");
                    final String assesslow = rs.getString("ASSESSLOW");
                    final String assesshigh = rs.getString("ASSESSHIGH");
                    AssessDat addwk = new AssessDat(assesslevel, assessmark, assesslow, assesshigh);
                    retList.add(addwk);
                }
            } catch (SQLException ex) {
                log.debug("getCertifInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String findAssessMst(final BigDecimal asVal) {
            String retStr = "";
            if (asVal != null && _assessList.size() > 0) {
                AssessDat getFstObj = (AssessDat)_assessList.get(0);
                retStr = getFstObj._assessmark;
                for (Iterator ite = _assessList.iterator();ite.hasNext();) {
                    AssessDat getObj = (AssessDat)ite.next();
                    BigDecimal getLowVal = new BigDecimal(getObj._assesslow);
                    if (getLowVal.compareTo(asVal) <= 0) {
                        retStr = getObj._assessmark;
                    } else {
                        break;
                    }
                }
            }
            return retStr;
        }
        private List setIgnoreSubclsCd(final DB2UDB db2) {
            List retList = new ArrayList();
            final String sql = getIgnoreSubclsCdSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
                    final String subclsCd = rs.getString("SUBCLASSCD");
                    retList.add(subclsCd);
                }
            } catch (SQLException ex) {
                log.debug("getCertifInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;

        }
        private String getIgnoreSubclsCdSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("  SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append(" WHERE ");
            stb.append("  REPLACECD = '1' ");
            stb.append("  AND YEAR = '" + _year + "' ");
            stb.append("  AND COMBINED_SCHOOL_KIND = '" + _schoolKind + "' ");
            return stb.toString();
        }
    }
}

// eof
