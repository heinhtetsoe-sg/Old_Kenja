// kanji=漢字
/*
 * $Id: 27f1f06e9a08a838e5164cd3517e09f583b3e76c $
 *
 * 作成日: 2008/05/07 13:50:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.KNJD177.Param;
import servletpack.KNJD.KNJD177.Score;
import servletpack.KNJD.KNJD177.Subclass;
import servletpack.KNJD.KNJD177.TestItem;
import servletpack.KNJD.KNJD177.TestScore;

/**
 * 通知表(熊本)。
 * @author m-yama
 * @version $Id: 27f1f06e9a08a838e5164cd3517e09f583b3e76c $
 */
public class KNJD177B extends KNJD177FormAbstract {

    private static final String GAKKITESTKIND = "99";
    private static final String GAKKITESTITEM = "00";

    private static final String SUBCLASSALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD177B.class);

    /**
     * コンストラクタ。
     * @param param
     */
    public KNJD177B(final HttpServletResponse response, final Param param) throws Exception {
        super(response, param);
    }

    /**
     * {@inheritDoc}
     */
    protected void printScore(final Map subclassMap, final List testList, final Map scoreMap, final DB2UDB db2) {
        final List testSemesterKindItemList = new ArrayList();
        for (final Iterator iter = testList.iterator(); iter.hasNext();) {
            final TestItem testItem = (TestItem) iter.next();
            final String testKindItem = testItem._testKindCd + testItem._testItemCd;
            if (testKindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                _svf.VrsOut("SEM_TESTNAME" + testItem._semester, testItem._testItemName);
            }
            testSemesterKindItemList.add(testItem._semester + testKindItem);
        }
        //評定段階区分の文言
        _svf.VrsOut("VALUE_RANK", _param._dankaiItem);

        int failCnt[] = {0,0,0,0}; //学期毎に欠点科目数を保管
        int valueCnt = 0;
        int totalValue = 0;
        boolean hasdata = false;
        final List subclassOrder = (List) subclassMap.get(_param._keySubclassOrder);
        for (final Iterator iter = subclassOrder.iterator(); iter.hasNext();) {
            final String subclassCd = (String) iter.next();
            final Subclass subclass = (Subclass) subclassMap.get(subclassCd);
            _svf.VrsOut("CLASS", subclass._className);
            final String subclassName = subclass._subclassName;
            if (subclassName != null) {
                if (16 < subclassName.length()) {
                    _svf.VrsOut("SUBCLASS2_1", subclassName.substring(0, 8));
                    _svf.VrsOut("SUBCLASS2_2", subclassName.substring(8, 16));
                } else if (8 < subclassName.length()) {
                    _svf.VrsOut("SUBCLASS2_1", subclassName.substring(0, 8));
                    _svf.VrsOut("SUBCLASS2_2", subclassName.substring(8));
                } else if (6 < subclassName.length()) {
                    _svf.VrsOut("SUBCLASS2_1", subclassName);
                } else {
                    _svf.VrsOut("SUBCLASS1", subclassName);
                }
            }
            if (scoreMap.containsKey(subclass._subclassCd)) {
                totalValue += printSubclass(scoreMap, testSemesterKindItemList, subclass, failCnt);
            }

            if (scoreMap.containsKey(SUBCLASSALL)) {
                printSubclassAll(scoreMap, testSemesterKindItemList, failCnt);
            }
            valueCnt++;
//            final float valueAvg = totalValue / valueCnt * 10 / 10;
//            _svf.VrsOut("VALUE_AVG", String.valueOf(valueAvg));
            _svf.VrEndRecord();
            hasdata = true;
        }
        if (!hasdata) {
            _svf.VrEndRecord();
        }
    }

    private int printSubclass(final Map scoreMap, final List testSemesterKindItemList, final Subclass subclass, int failCnt[]) {
        final Score score = (Score) scoreMap.get(subclass._subclassCd);
        if (null != score._rankMap) {
            for (final Iterator itSc = score._rankMap.keySet().iterator(); itSc.hasNext();) {
                final String semester = (String) itSc.next();
                log.debug(semester + " 科目：" + subclass._subclassCd);
                final Map rankMap = (Map) score._rankMap.get(semester);
                if (null != rankMap) {
                    for (final Iterator rankIt = rankMap.keySet().iterator(); rankIt.hasNext();) {
                        final String kindItem = (String) rankIt.next();
                        if (!testSemesterKindItemList.contains(semester + kindItem)) {
                            continue;
                        }
                        if (kindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                            final TestScore testScore = (TestScore) rankMap.get(kindItem);
                            log.debug(testScore);
                            if (null != testScore._score) {
                                if (isKetten(testScore)) {
                                    _svf.VrAttribute("SEM_SCORE" + semester, "Paint=(1,50,1),Bold=1");
                                    int number = "9".equals(semester) ? 3 : Integer.parseInt(semester) - 1 ;
                                    failCnt[number]++;
                                }
                                _svf.VrsOut("SEM_SCORE" + semester, testScore._score);
                                if ("9".equals(_param._paramSemester)) {
                                    if ("9".equals(semester) && _param.isGakunenMatu()) {
                                        if (isKetten(testScore)) {
                                            _svf.VrAttribute("GRAD_VALUE100", "Paint=(1,50,1),Bold=1");
                                        }
                                        _svf.VrsOut("GRAD_VALUE100", testScore._score);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (_param._isPrintKekka) {
            if (null != _param._defineSchoolCode && (_param._defineSchoolCode.absent_cov == 3 || _param._defineSchoolCode.absent_cov == 4)) {
                _svf.VrsOut("KEKKA2", score._attend);
            } else {
                _svf.VrsOut("KEKKA", score._attend);
            }
        }
        if ("9".equals(_param._paramSemester)) {
            if (null != score._value && _param.isGakunenMatu()) {
                if (Integer.parseInt(score._value) == 1) {
                    _svf.VrAttribute("GRAD_VALUE5", "Paint=(1,50,1),Bold=1");
                }
            }
            _svf.VrsOut("GRAD_VALUE5", _param.isGakunenMatu() ? score._value : "");
            _svf.VrsOut("COMP_CREDIT", _param.isGakunenMatu() ? score._getCredit : "");
//          log.debug("failCnt1="+failCnt[0]+", failCnt2="+failCnt[1]+", failCnt3="+failCnt[2]+", failCnt9="+failCnt[3]);
        }
        return Integer.parseInt(null == score._value || score._value.equals("") ? "0" : score._value);
    }
    
    private boolean isKetten(TestScore testScore) {
        final boolean isKetten;
        if ("2".equals(_param._checkKettenDiv)) {
            // 満点マスタ合格点未満
            isKetten = null == testScore._passScore ? false : Integer.parseInt(testScore._score) < Integer.parseInt(testScore._passScore);
        } else {
            if ("1".equals(_param._useAssessSubclassMst)) {
                // 科目別評定マスタで判定する。なければ評定マスタで判定する
                int dankaiHigh1 = null == testScore._dankaiHigh1 ? _param._dankaiHigh1 : Integer.parseInt(testScore._dankaiHigh1);
                isKetten = Integer.parseInt(testScore._score) <= dankaiHigh1;
            } else {
                // 評定マスタで判定する
                isKetten = Integer.parseInt(testScore._score) <= _param._dankaiHigh1;
            }
        }
        return isKetten;
    }

    private void printSubclassAll(final Map scoreMap, final List testSemesterKindItemList, int failCnt[]) {
        final Score score = (Score) scoreMap.get(SUBCLASSALL);
        for (final Iterator itSc = score._rankMap.keySet().iterator(); itSc.hasNext();) {
            final String semester = (String) itSc.next();
            log.debug(semester);
            final Map rankMap = (Map) score._rankMap.get(semester);
            for (final Iterator rankIt = rankMap.keySet().iterator(); rankIt.hasNext();) {
                final String kindItem = (String) rankIt.next();
                if (!testSemesterKindItemList.contains(semester + kindItem)) {
                    continue;
                }
                if (kindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                    final TestScore testScore = (TestScore) rankMap.get(kindItem);
                    final String avgStr = testScore._bdAvg != null ? testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
                    log.debug(testScore);
                    int number = "9".equals(semester) ? 3 : Integer.parseInt(semester) - 1 ;
                    String failCount = String.valueOf(failCnt[number]);
                    if (!semester.equals("9")) {
                        _svf.VrsOut("SEM_TOTAL" + semester, testScore._score);
                        _svf.VrsOut("SEM_AVERAGE" + semester, avgStr);
                        if (_param._isRankPrintAll) {
                            _svf.VrsOut("SEM_RANK" + semester, testScore._rank);
                            _svf.VrsOut("SEM_RANK_TOTAL" + semester, testScore._cnt);
                            _svf.VrsOut("CLASS_SEM_RANK" + semester, testScore._classRank);
                            _svf.VrsOut("CLASS_SEM_RANK_TOTAL" + semester, testScore._classCnt);
                        }
                        _svf.VrsOut("SEM_FAIL" + semester, failCount);
                    } else if ("9".equals(_param._paramSemester)) {
                        _svf.VrsOut("GRAD_TOTAL100", testScore._score);
                        _svf.VrsOut("GRAD_AVERAGE100", avgStr);
                        if (_param._isRankPrintAll) {
                            _svf.VrsOut("GRAD_RANK100", testScore._rank);
                            _svf.VrsOut("GRAD_RANK_TOTAL100", testScore._cnt);
                            _svf.VrsOut("CLASS_GRAD_RANK100", testScore._classRank);
                            _svf.VrsOut("CLASS_GRAD_RANK_TOTAL100", testScore._classCnt);
                        }
                        _svf.VrsOut("GRAD_FAIL100", failCount);
                    }
                }
            }
        }
    }
}
 // KNJD177B

// eof
