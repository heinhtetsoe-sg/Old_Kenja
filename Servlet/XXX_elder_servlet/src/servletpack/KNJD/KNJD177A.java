// kanji=漢字
/*
 * $Id: edf4301a1995d9fd1e083fb03bcc62c996a8b251 $
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
 * @version $Id: edf4301a1995d9fd1e083fb03bcc62c996a8b251 $
 */
public class KNJD177A extends KNJD177FormAbstract {

    private static final String SUBCLASSALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD177A.class);

    /**
     * コンストラクタ。
     * @param param
     */
    public KNJD177A(final HttpServletResponse response, final Param param) throws Exception {
        super(response, param);
    }

    /**
     * {@inheritDoc}
     */
    protected void printScore(final Map subclassMap, final List testList, final Map scoreMap, final DB2UDB db2) {
        for (final Iterator iter = testList.iterator(); iter.hasNext();) {
            final TestItem testItem = (TestItem) iter.next();
            final String semeName = (String) _param._semesterMap.get(testItem._semester);
            final String field = String.valueOf(Integer.parseInt(testItem._testKindCd)).substring(0, 1);
            if (!"3".equals(testItem._semester)) {
                String seme = ("9".equals(testItem._semester)) ? "3" : testItem._semester;
                _svf.VrsOut("SEM_NAME" + seme, ("9".equals(testItem._semester)) ? "学年" : semeName);
                _svf.VrsOut("SEM_TESTNAME" + seme + "_" + field, testItem._testItemName);
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
                    _svf.VrAttribute("CLASS", "Size=5.5,Keta=6,Y=1134");
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
                printSubclassAll(scoreMap);
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
                log.debug(semester);
                final Map rankMap = (Map) score._rankMap.get(semester);
                if (null != rankMap) {
                    for (final Iterator rankIt = rankMap.keySet().iterator(); rankIt.hasNext();) {
                        final String kindItem = (String) rankIt.next();
                        final TestScore testScore = (TestScore) rankMap.get(kindItem);
                        final String avgStr = testScore._bdAvg != null ? testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
                        log.debug(testScore);
                        final String field = String.valueOf(Integer.parseInt(kindItem)).substring(0, 1);
                        if (!"3".equals(semester)) {
                            String seme = ("9".equals(semester) && _param.isGakunenMatu()) ? "9".equals(_param._paramSemester) ? "3" : "_" : semester;
                            _svf.VrsOut("SCORE" + seme + "_" + field, testScore._score);
                            _svf.VrsOut("DEVIATION" + seme + "_" + field, avgStr);
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

    private void printSubclassAll(final Map scoreMap) {
        final Score score = (Score) scoreMap.get(SUBCLASSALL);
        for (final Iterator itSc = score._rankMap.keySet().iterator(); itSc.hasNext();) {
            final String semester = (String) itSc.next();
            log.debug(semester);
            final Map rankMap = (Map) score._rankMap.get(semester);
            for (final Iterator rankIt = rankMap.keySet().iterator(); rankIt.hasNext();) {
                final String kindItem = (String) rankIt.next();
                final TestScore testScore = (TestScore) rankMap.get(kindItem);
                final String avgStr = testScore._bdAvg != null ? testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
//                if (semester.equals("9")) {
//                    _svf.VrsOut("GRAD_TOTAL", testScore._score);
//                    _svf.VrsOut("GRAD_AVERAGE", avgStr);
//                    _svf.VrsOut("GRAD_RANK", testScore._rank);
//                    _svf.VrsOut("GRAD_RANK_TOTAL", testScore._cnt);
//                }
                if (!"3".equals(semester)) {
                    String seme = ("9".equals(semester)) ? "9".equals(_param._paramSemester) ? "3" : "_" : semester;
                    final String field = String.valueOf(Integer.parseInt(kindItem)).substring(0, 1);
                    log.debug(field + " " + testScore);
                    _svf.VrsOut("TOTAL" + seme + "_" + field, testScore._score);
                    _svf.VrsOut("AVERAGE" + seme + "_" + field, avgStr);
                    if (_param._isRankPrintAll) {
                        _svf.VrsOut("RANK" + seme + "_" + field, testScore._rank);
                        _svf.VrsOut("RANK_TOTAL" + seme + "_" + field, testScore._cnt);
                        _svf.VrsOut("CLASS_RANK" + seme + "_" + field, testScore._classRank);
                        _svf.VrsOut("CLASS_RANK_TOTAL" + seme + "_" + field, testScore._classCnt);
                    }
                }
            }
        }
    }
}
 // KNJD177A

// eof
