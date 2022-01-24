// kanji=漢字
/*
 * $Id: f9a6c09a09a19ebd03f3356d267a15735aa28c3a $
 *
 * 作成日: 2008/05/12 15:19:55 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.KNJD182.KanjiMockScore;
import servletpack.KNJD.KNJD182.KanjiMockTest;
import servletpack.KNJD.KNJD182.Param;
import servletpack.KNJD.KNJD182.Score;
import servletpack.KNJD.KNJD182.Student;
import servletpack.KNJD.KNJD182.Subclass;
import servletpack.KNJD.KNJD182.TestItem;
import servletpack.KNJD.KNJD182.TestScore;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: f9a6c09a09a19ebd03f3356d267a15735aa28c3a $
 */
public class KNJD182Form {
    private static final String SUBCLASSALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD182Form.class);
    
    private static final int AVG_JUDGE_NULL = 0;
    private static final int AVG_JUDGE_PASSED = 1;
    private static final int AVG_JUDGE_NOT_PASSEED = 2;

    protected final Vrw32alp _svf;
    protected final Param _param;

    public KNJD182Form(final HttpServletResponse response, final Param param) throws Exception {
        _param = param;
        _svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        init(response);

        log.debug(_param._inState);
    }

    private void init(
            final HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/pdf");

        _svf.VrInit();
        _svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    public void noneData() {
        _svf.VrSetForm("MES001.frm", 0);
        _svf.VrsOut("note", "note");
        _svf.VrEndPage();
    }

    protected void setHead(final String formName) {
        _svf.VrSetForm(formName, 4);
        final String nendo = _param.changePrintYear();
        _svf.VrsOut("NENDO", nendo);
        final String semeName = _param.isGakunenMatu() ? (String) _param._semesterMap.get(_param._ctrlSeme) : (String) _param._semesterMap.get(_param._semester);
        _svf.VrsOut("SEMESTER",  semeName);
        if (null != _param._certifSchool) {
            _svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
            _svf.VrsOut("JOB_NAME", _param._certifSchool._jobName);
            _svf.VrsOut("PRINCIPAL_NAME", _param._certifSchool._principalName);
        }
    }

    protected void printStudent(final Student student) {
        log.debug(student);
        if (null != _param._certifSchool && null != _param._certifSchool._remark2) {
            _svf.VrsOut("STAFFNAME", _param._certifSchool._remark2 + student._hrStaffName);
        }
        _svf.VrsOut("HR_NAME", student._hrName + String.valueOf(Integer.parseInt(student._attendNo)) + "番");
        _svf.VrsOut("COURSE", student._courseName + student._majorName);
        _svf.VrsOut("NAME", student._name);
        if (null != _param._addrPrint) {
            _svf.VrsOut("ZIPCD", student._gZip);
            _svf.VrsOut("ADDR1", student._gAddr1);
            _svf.VrsOut("ADDR2", student._gAddr2);
            _svf.VrsOut("ADDRESSEE", student._gName == null ? "" : student._gName + "  様");
        }
    }

    protected void printAttend(
            final int line,
            final String semesterName,
            final String lesson,
            final String mourning,
            final String present,
            final String attend,
            final String absence,
            final String late,
            final String early
    ) {
        _svf.VrsOutn("SEMESTERNAME", line, semesterName);
        _svf.VrsOutn("LESSON", line, lesson);
        _svf.VrsOutn("MOURNING", line, mourning);
        _svf.VrsOutn("PRESENT", line, present);
        _svf.VrsOutn("ATTEND", line, attend);
        _svf.VrsOutn("ABSENCE", line, absence);
        _svf.VrsOutn("LATE", line, late);
        _svf.VrsOutn("EARLY", line, early);
    }

    protected void printHreport(
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        KNJObjectAbs knjobj = new KNJEditString();
        ArrayList arrlist = knjobj.retDividString(str, size, lineCnt);
        if ( arrlist != null ) {
            for (int i = 0; i < arrlist.size(); i++) {
                _svf.VrsOutn(fieldName, i+1,  (String)arrlist.get(i) );
            }
        }
    }

    /**
     * @param db2
     * @param student
     * @param subject_d
     * @param subject_t
     */
    public void printGetCredit(
            final String field,
            final String credit
    ) throws SQLException {
        _svf.VrsOut(field, credit);
    }
    
    // ----------------------------------------------------------------------------------------------------------
    
    protected void printKanjiMockScore(final Map scoreMap, final DB2UDB db2, final Student student) {
        KanjiMockScore kanjiMockScore = (KanjiMockScore) scoreMap.get("KANJI_MOCK_SCORE");
        int i = 1;
        for(Iterator it = _param._kanjiMockTestMap.keySet().iterator(); it.hasNext();) {
            final String mockCd = (String) it.next();
            KanjiMockTest k = (KanjiMockTest) _param._kanjiMockTestMap.get(mockCd);
            final int idx = (k._isRetest) ? 6 : i; // 追試の描画
            _svf.VrsOutn("KANJI_KAI", idx, k._mockName2);
            if (k._avgUsed) {
                _svf.VrsOutn("KOME", idx, "*");
            }
            i += 1;
        }
        _svf.VrsOutn("KANJI_KAI", 5, "平均");

        i = 1;
        int count = 0;
        int total = 0;
        for(Iterator it = _param._kanjiMockTestMap.keySet().iterator(); it.hasNext();) {
            final String mockCd = (String) it.next();
            KanjiMockTest k = (KanjiMockTest) _param._kanjiMockTestMap.get(mockCd);
            String score = (String) kanjiMockScore._scoreMap.get(mockCd);
            Boolean isPassed = (Boolean) kanjiMockScore._isPassedMap.get(mockCd);
            final int idx = (k._isRetest) ? 6 : i; // 追試の描画
            if (i != 6 || null != score) {
                _svf.VrsOutn("KANJI_POINT", idx, score);
                String sPassed = isPassed == null ? "" : isPassed.booleanValue() ? "合格" : "不合格";
                _svf.VrsOutn("KANJI_PASS", idx, sPassed);
            }
            i += 1;
            if (k._avgUsed && null != score) {
                total += Integer.parseInt(score);
                count += 1;
            }
        }
        if (0 != count) {
//            final String avg = new BigDecimal(total).divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
//            _svf.VrsOutn("KANJI_POINT", 5, avg);
            final BigDecimal avgBigDecimal = new BigDecimal(total).divide(new BigDecimal(count), 0, BigDecimal.ROUND_FLOOR);
            _svf.VrsOutn("KANJI_POINT", 5, avgBigDecimal.toString());
            _svf.VrsOutn("KANJI_PASS" , 5, getJudgename(judgePassScoreALL9(student._passScoreALL9, avgBigDecimal)));
        }
    }

    /**
     * 合否を判定します。
     * @param passScore 合格点
     * @param avg 平均
     * @return
     */
    private int judgePassScoreALL9(final Double passScore, final BigDecimal avg) {
        if (null == passScore || null == avg) {
            return AVG_JUDGE_NULL;
        }
        if (passScore.floatValue() <= avg.floatValue()) {
            return AVG_JUDGE_PASSED;
        }
        return AVG_JUDGE_NOT_PASSEED;
    }
    
    /**
     * 合否の判定名称
     * @param judge 合否判定
     * @return
     */
    private String getJudgename(int judge) {
        if (AVG_JUDGE_PASSED == judge) {
            return "合格";
        } else if (AVG_JUDGE_NOT_PASSEED == judge) {
            return "不合格";
        }
        return "";
    }
    
    protected void printScore(final Map subclassMap, final List testList, final Map scoreMap, final DB2UDB db2) {
        for (final Iterator iter = testList.iterator(); iter.hasNext();) {
            final TestItem testItem = (TestItem) iter.next();
            final String kindItem = testItem._testKindCd + testItem._testItemCd;
            if (!"9".equals(testItem._semester)) {
                if ("9900".equals(kindItem)) {
                    continue;
                }
                if (!("0101".equals(kindItem) || "0201".equals(kindItem))) {
                    continue;
                }
            }
            final String semeName = (String) _param._semesterMap.get(testItem._semester);
            final String field = "01".equals(testItem._testKindCd) ? "1" : "9";
            if (!"3".equals(testItem._semester)) {
                String seme = ("9".equals(testItem._semester)) ? "3" : testItem._semester;
                _svf.VrsOut("SEM_NAME" + seme, ("9".equals(testItem._semester)) ? "学年" : semeName);
                _svf.VrsOut("SEM_TESTNAME" + seme + "_" + field, testItem._testItemName);
            }
        }
        _svf.VrsOut("ITEM", _param.getItemName());

        int valueCnt = 0;
        int totalValue = 0;
        for (final Iterator iter = subclassMap.keySet().iterator(); iter.hasNext();) {
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
                        if (!"9".equals(semester)) {
                            if ("9900".equals(kindItem)) {
                                continue;
                            }
                            if (!("0101".equals(kindItem) || "0201".equals(kindItem))) {
                                continue;
                            }
                        }
                        final TestScore testScore = (TestScore) rankMap.get(kindItem);
                        final String avgStr = testScore._bdAvg != null ? testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
                        log.debug(testScore);
                        final String field = kindItem.startsWith("01") ? "1" : "9";
                        if (!"3".equals(semester)) {
                            String seme = ("9".equals(semester) && _param.isGakunenMatu()) ? "3" : semester;
                            _svf.VrsOut("SCORE" + seme + "_" + field, testScore._score);
                            _svf.VrsOut("DEVIATION" + seme + "_" + field, avgStr);
                        }
                    }
                }
            }
        }
        _svf.VrsOut("GRAD_VALUE", _param.isGakunenMatu() ? score._value : "");
        _svf.VrsOut("KEKKA", score._attend);
        _svf.VrsOut("COMP_CREDIT", _param.isGakunenMatu() ? score._getCredit : "");
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
                if (!"9".equals(semester)) {
                    if ("9900".equals(kindItem)) {
                        continue;
                    }
                    if (!("0101".equals(kindItem) || "0201".equals(kindItem))) {
                        continue;
                    }
                }
                final TestScore testScore = (TestScore) rankMap.get(kindItem);
                final String avgStr = testScore._bdAvg != null ? testScore._bdAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : "";
//                if (semester.equals("9")) {
//                    _svf.VrsOut("GRAD_TOTAL", testScore._score);
//                    _svf.VrsOut("GRAD_AVERAGE", avgStr);
//                    _svf.VrsOut("GRAD_RANK", testScore._rank);
//                    _svf.VrsOut("GRAD_RANK_TOTAL", testScore._cnt);
//                }
                if (!"3".equals(semester)) {
                    String seme = ("9".equals(semester)) ? "3" : semester;
                    final String field = kindItem.startsWith("01") ? "1" : "9";
                    log.debug(field + " " + testScore);
                    _svf.VrsOut("TOTAL" + seme + "_" + field, testScore._score);
                    _svf.VrsOut("AVERAGE" + seme + "_" + field, avgStr);
                    _svf.VrsOut("GRAD_TOTAL"            , score._value);
                    if (score._gradAvg != null) {
                        BigDecimal bdGradAvg = new BigDecimal(score._gradAvg);
                        _svf.VrsOut("GRAD_AVERAGE"  , bdGradAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                    } else {
                        _svf.VrsOut("GRAD_AVERAGE"  , "");
                    }
                    if (_param._isRankPrintAll) {
                        _svf.VrsOut("RANK" + seme + "_" + field, testScore._rank);
                        _svf.VrsOut("RANK_TOTAL" + seme + "_" + field, testScore._cnt);
                        _svf.VrsOut("CLASS_RANK" + seme + "_" + field, testScore._classRank);
                        _svf.VrsOut("CLASS_RANK_TOTAL" + seme + "_" + field, testScore._classCnt);
                        _svf.VrsOut("GRAD_RANK"             , score._gradRank);
                        _svf.VrsOut("GRAD_RANK_TOTAL"       , score._gradCnt);
                        _svf.VrsOut("CLASS_GRAD_RANK"       , score._gradClassRank);
                        _svf.VrsOut("CLASS_GRAD_RANK_TOTAL" , score._gradClassCnt);
                    }
                }
            }
        }
    }
}
 // KNJD177FormAbstract

// eof
