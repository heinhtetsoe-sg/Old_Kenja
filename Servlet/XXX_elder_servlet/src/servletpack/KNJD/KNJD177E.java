// kanji=漢字
/*
 * $Id: 60e43e477e377c31102ed1c45ee7f03c6d6d8d7f $
 *
 * 作成日: 2008/05/07 13:50:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * @version $Id: 60e43e477e377c31102ed1c45ee7f03c6d6d8d7f $
 */
public class KNJD177E extends KNJD177FormAbstract {

    private static final String GAKKITESTKIND = "99";
    private static final String GAKKITESTITEM = "00";

    private static final String SUBCLASSALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD177E.class);
    
    private Map _seqMap = new TreeMap();

    /**
     * コンストラクタ。
     * @param param
     */
    public KNJD177E(final HttpServletResponse response, final Param param) throws Exception {
        super(response, param);
    }
    
    private int getSeq(final String semester, final String testKindItem) {
        final Integer seq = (Integer) _seqMap.get(semester + testKindItem);
        return null == seq ? -1 : seq.intValue();
    }

    /**
     * {@inheritDoc}
     */
    protected void printScore(final Map subclassMap, final List testList, final Map scoreMap, final DB2UDB db2) {
        int seq = 1;
        for (final Iterator iter = testList.iterator(); iter.hasNext();) {
            final TestItem testItem = (TestItem) iter.next();
            final String testKindItem = testItem._testKindCd + testItem._testItemCd;
            if (!testItem._testDiv.equals("9")) {
                _svf.VrsOut("TESTNAME" + seq, testItem._testItemName);
                seq++;
            }
            if (testKindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                _svf.VrsOut("SEM_TESTNAME" + testItem._semester, testItem._testItemName);
            }
        }
        _svf.VrsOut("ITEM", _param.getItemName());

        int valueCnt = 0;
        int totalValue = 0;
        final List subclassOrder = (List) subclassMap.get(_param._keySubclassOrder);
        for (final Iterator iter = subclassOrder.iterator(); iter.hasNext();) {
            final String subclassCd = (String) iter.next();
            final Subclass subclass = (Subclass) subclassMap.get(subclassCd);
            printSubclassname(subclass);
            if (scoreMap.containsKey(subclass._subclassCd)) {
                totalValue += printSubclass(scoreMap, subclass);
            }

            if (scoreMap.containsKey(SUBCLASSALL)) {
                printSubclassAll(scoreMap, SUBCLASSALL);
            }
            if (scoreMap.containsKey(_param.getSubclassCd())) {
                printSubclassAll(scoreMap, _param.getSubclassCd());
            }
            valueCnt++;
//            final float valueAvg = totalValue / valueCnt * 10 / 10;
//            _svf.VrsOut("VALUE_AVG", String.valueOf(valueAvg));
            _svf.VrEndRecord();
        }
        _svf.VrEndRecord();
    }

    private void printSubclassname(final Subclass subclass) {
        if (subclass._className != null) {
            if (2 < subclass._className.length()) {
                _svf.VrAttribute("CLASS", "Size=5.5,Keta=6,Y=994");
            } else {
                _svf.VrAttribute("CLASS", "Size=8.0,Keta=4");
            }
        }
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
    }

    private int printSubclass(final Map scoreMap, final Subclass subclass) {
        final Score score = (Score) scoreMap.get(subclass._subclassCd);
        if (null != score._rankMap) {
            for (final Iterator itSc = score._rankMap.keySet().iterator(); itSc.hasNext();) {
                final String semester = (String) itSc.next();
                log.debug(semester + " 科目：" + subclass._subclassCd);
                final Map rankMap = (Map) score._rankMap.get(semester);
                if (null != rankMap) {
                    for (final Iterator rankIt = rankMap.keySet().iterator(); rankIt.hasNext();) {
                        final String kindItem = (String) rankIt.next();
                        final TestScore testScore = (TestScore) rankMap.get(kindItem);
                        if (null != testScore._seq) {
                            _svf.VrsOut("SCORE" + testScore._seq, testScore._score);
                            if (null != testScore._bdAvg) {
                                final String avg = testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                                _svf.VrsOut("DEVIATION" + testScore._seq, avg);
                            }
                            final String assessLevel;
                            if ("1".equals(_param._dankaiYomikae) && ("1".equals(testScore._assessLevel) || "2".equals(testScore._assessLevel))) {
                                assessLevel = "3";
                            } else {
                                assessLevel = testScore._assessLevel;
                            }
                            _svf.VrsOut("STAGE" + testScore._seq, assessLevel);
                        }
                        if (kindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                            if ("9".equals(semester) && !"9".equals(_param._paramSemester)) continue;
                            _svf.VrsOut("SEM_SCORE" + semester, testScore._score);
                            if (null != testScore._bdAvg) {
                                final String avg = testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                                _svf.VrsOut("SEM_DEVIATION" + semester, avg);
                            }
                            final String assessLevel;
                            if ("1".equals(_param._dankaiYomikae) && ("1".equals(testScore._assessLevel) || "2".equals(testScore._assessLevel))) {
                                assessLevel = "3";
                            } else {
                                assessLevel = testScore._assessLevel;
                            }
                            _svf.VrsOut("SEM_STAGE" + semester, assessLevel);
                        }
                    }
                }
            }
        }
        _svf.VrsOut("KEKKA", score._attend);
        if ("9".equals(_param._paramSemester)) {
            _svf.VrsOut("GRAD_VALUE", _param.isGakunenMatu() ? score._value : "");
            _svf.VrsOut("COMP_CREDIT", _param.isGakunenMatu() ? score._getCredit : "");
        }
        return Integer.parseInt(null == score._value || score._value.equals("") ? "0" : score._value);
    }

    private void printSubclassAll(final Map scoreMap, final String setSubClass) {
        final Score score = (Score) scoreMap.get(setSubClass);
        for (final Iterator itSc = score._rankMap.keySet().iterator(); itSc.hasNext();) {
            final String semester = (String) itSc.next();
            log.debug(semester);
            final Map rankMap = (Map) score._rankMap.get(semester);
            for (final Iterator rankIt = rankMap.keySet().iterator(); rankIt.hasNext();) {
                final String kindItem = (String) rankIt.next();
                final TestScore testScore = (TestScore) rankMap.get(kindItem);
                final String avgStr = testScore._bdAvg != null ? testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
                log.debug(testScore);
                if (kindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                    if ("9".equals(semester) && !"9".equals(_param._paramSemester)) {
                    } else {
                        _svf.VrsOut("SEM_TOTAL" + semester, testScore._score);
                        _svf.VrsOut("SEM_AVERAGE" + semester, avgStr);
//                        if (null == (String) _param._rankPrintSemMap.get(semester)) {
//                            _svf.VrsOut("SEM_RANK" + semester, testScore._rank);
//                            _svf.VrsOut("SEM_RANK_TOTAL" + semester, testScore._cnt);
//                            _svf.VrsOut("CLASS_SEM_RANK" + semester, testScore._classRank);
//                            _svf.VrsOut("CLASS_SEM_RANK_TOTAL" + semester, testScore._classCnt);
//                        }
                    }
                }
                if (null != testScore._seq) {
                    if ("9".equals(semester) && !"9".equals(_param._paramSemester)) {
                    } else {
                        _svf.VrsOut("TOTAL" + testScore._seq, testScore._score);
                        _svf.VrsOut("AVERAGE" + testScore._seq, avgStr);
//                        if (null == (String) _param._rankPrintMap.get(testScore._seq)) {
//                            _svf.VrsOut("RANK" + testScore._seq, testScore._rank);
//                            _svf.VrsOut("RANK_TOTAL" + testScore._seq, testScore._cnt);
//                            _svf.VrsOut("CLASS_RANK" + testScore._seq, testScore._classRank);
//                            _svf.VrsOut("CLASS_RANK_TOTAL" + testScore._seq, testScore._classCnt);
//                        }
                    }
                }
            }
        }
    }
}
