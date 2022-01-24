// kanji=漢字
/*
 * $Id: 07b88da98b996dc2470c836f3c13e9a5c82443ff $
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
 * @version $Id: 07b88da98b996dc2470c836f3c13e9a5c82443ff $
 */
public class KNJD177C extends KNJD177FormAbstract {

    private static final String GAKKITESTKIND = "99";
    private static final String GAKKITESTITEM = "00";

    private static final String SUBCLASSALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD177C.class);

    /**
     * コンストラクタ。
     * @param param
     */
    public KNJD177C(final HttpServletResponse response, final Param param) throws Exception {
        super(response, param);
    }

    /**
     * {@inheritDoc}
     */
    protected void printScore(final Map subclassMap, final List testList, final Map scoreMap, final DB2UDB db2) {
        int testName = 1;
        for (final Iterator iter = testList.iterator(); iter.hasNext();) {
            final TestItem testItem = (TestItem) iter.next();
            final String testKindItem = testItem._testKindCd + testItem._testItemCd;
            if (!testItem._testDiv.equals("9")) {
                _svf.VrsOut("TESTNAME" + testName, testItem._testItemName);
                testName++;
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
            if (subclass._className != null) {
                if (2 < subclass._className.length()) {
                    _svf.VrAttribute("CLASS", "Size=5.5,Keta=6,Y=816");
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
                            _svf.VrsOut("DEVIATION" + testScore._seq, testScore._deviation);
                        }
                        if (kindItem.equals(GAKKITESTKIND + GAKKITESTITEM)) {
                            if ("9".equals(semester) && !"9".equals(_param._paramSemester)) continue;
                            log.debug(testScore);
                            _svf.VrsOut("SEM_SCORE" + semester, testScore._score);
                        }
                    }
                }
            }
        }
        if (null != _param._defineSchoolCode && (_param._defineSchoolCode.absent_cov == 3 || _param._defineSchoolCode.absent_cov == 4)) {
            _svf.VrsOut("KEKKA2", score._attend);
        } else {
            _svf.VrsOut("KEKKA", score._attend);
        }
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
                        if (null == (String) _param._rankPrintSemMap.get(semester)) {
                            _svf.VrsOut("SEM_RANK" + semester, testScore._rank);
                            _svf.VrsOut("SEM_RANK_TOTAL" + semester, testScore._cnt);
                            _svf.VrsOut("CLASS_SEM_RANK" + semester, testScore._classRank);
                            _svf.VrsOut("CLASS_SEM_RANK_TOTAL" + semester, testScore._classCnt);
                        }
                    }
                }
                if (null != testScore._seq) {
                    if ("9".equals(semester) && !"9".equals(_param._paramSemester)) {
                    } else {
                        _svf.VrsOut("TOTAL" + testScore._seq, testScore._score);
                        _svf.VrsOut("AVERAGE" + testScore._seq, avgStr);
                        if (null == (String) _param._rankPrintMap.get(testScore._seq)) {
                            _svf.VrsOut("RANK" + testScore._seq, testScore._rank);
                            _svf.VrsOut("RANK_TOTAL" + testScore._seq, testScore._cnt);
                            _svf.VrsOut("CLASS_RANK" + testScore._seq, testScore._classRank);
                            _svf.VrsOut("CLASS_RANK_TOTAL" + testScore._seq, testScore._classCnt);
                        }
                    }
                }
            }
        }
    }
}
 // KNJD177C

// eof
