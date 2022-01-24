// kanji=漢字
/*
 * $Id: 4391bf9bf82548f58e03b8bee8b30dca33999b53 $
 *
 * 作成日: 2010/03/18
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: 4391bf9bf82548f58e03b8bee8b30dca33999b53 $
 */
public class KNJD652 {
    private static final Log log = LogFactory.getLog(KNJD652.class);

    private TestKind _knjdObj;  //成績別処理のクラス

    private final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private final DecimalFormat DEC_FMT2 = new DecimalFormat("0");

    private static final String SPECIAL_ALL = "999";
    
    private static final String TOTAL_RANK_CLASS = "4";

    private static final String TOTAL_RANK_GRADE = "2";

    private static final String TOTAL_RANK_COURSE = "3";

    private static final String ATTRIBUTE_MIRISYU = "Paint=(1,40,1),Bold=1";
    private static final String ATTRIBUTE_MIRISYU2 = "Paint=(1,40,2),Bold=1";
    private static final String ATTRIBUTE_MISYUTOKU = "Paint=(1,70,1),Bold=1";
    private static final String ATTRIBUTE_MISYUTOKU2 = "Paint=(1,70,2),Bold=1";
    private static final String ATTRIBUTE_SLUMP = "Paint=(1,50,1),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";
    private Param _param;

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException {
        log.fatal("$Revision: 75219 $ $Date: 2020-07-02 15:14:02 +0900 (木, 02 7 2020) $"); // CVSキーワードの取り扱いに注意
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alp svf = null;
        boolean hasData = false;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            KNJServletUtils.debugParam(request, log);
            _param = new Param(request, db2);

            _knjdObj = createTestKind();  //成績別処理クラスを設定するメソッド
            log.fatal(_knjdObj);

            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            sd.setSvfInit(request, response, svf);

            final String[] hrclass = request.getParameterValues("CLASS_SELECTED");  //印刷対象HR組
            for (int h = 0; h < hrclass.length; h++) {

                if (_param._outputCourse) {
                    final List courses = Course.createCourses(db2, _param, hrclass[h]);
                    log.debug("コース数=" + courses.size());
                    
                    for (final Iterator it = courses.iterator(); it.hasNext();) {
                        final Course course = (Course) it.next();
                        final HRInfo hrInfo = new HRInfo(hrclass[h]);
                        hrInfo.load(db2, course._coursecd);
                        
                        // 印刷処理
                        if (printMain(db2, svf, hrInfo)) {
                            hasData = true;
                        }
                    }
                } else {
                    final HRInfo hrInfo = new HRInfo(hrclass[h]);
                    hrInfo.load(db2, null);
                    
                    // 印刷処理
                    if (printMain(db2, svf, hrInfo)) {
                        hasData = true;
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
        }
    }

    /**
     *  成績別処理クラス設定
     */
    private TestKind createTestKind() {
        final String testKindCd = _param._testKindCd;
        TestKind knjdObj = new TestKind();
        knjdObj._creditDrop = _param._creditDrop;

        if (testKindCd.startsWith("01") || testKindCd.startsWith("02")) {
            // 成績一覧表 中間 or 期末1 or 期末2
            knjdObj._item1Name = "素点";
            knjdObj._title = _param._semesterName + " " + _param._testItemName + " 成績一覧表";
//            knjdObj._item2Name = "欠課";
//            knjdObj._item4Name = "総合点";
//            knjdObj._item5Name = "平均点";
            knjdObj._targetValueIsPatternAssess = false;
            knjdObj._isGakunenMatu = false;
            knjdObj._hasCompCredit = false;
            knjdObj._isPrintScore = true;
            knjdObj._creditDrop = false;

        } else if ("9".equals(_param._semester) && "9900".equals(testKindCd)) {
            // 成績一覧表 評定
            knjdObj._item1Name = "評定";
            knjdObj._title = "  成績一覧表（" + knjdObj._item1Name + "）";
//            knjdObj._item2Name = "欠課";
//            knjdObj._item4Name = "評定総合点";
//            knjdObj._item5Name = "評定平均点";
            knjdObj._targetValueIsPatternAssess = true;
            knjdObj._isGakunenMatu = "9".equals(_param._semester);
            knjdObj._hasCompCredit = true;
            knjdObj._isPrintDetailTani = true;

        } else if (testKindCd.startsWith("99")) {
            // 成績一覧表 評価
            knjdObj._item1Name = "評価";
            knjdObj._title = "  成績一覧表（" + knjdObj._item1Name + "）";
//            knjdObj._item2Name = "欠課";
//            knjdObj._item4Name = "評価総合点";
//            knjdObj._item5Name = "評価平均点";
            knjdObj._targetValueIsPatternAssess = true;
            knjdObj._isGakunenMatu = "9".equals(_param._semester);
            knjdObj._hasCompCredit = true;
            knjdObj._isPrintDetailTani = true;
        }
        return knjdObj;
    }

    /**
     * 学級ごとの印刷。
     * @param svf
     * @param hrInfo
     * @return
     */
    private boolean printMain(
    		final DB2UDB db2,
            final Vrw32alp svf,
            final HRInfo hrInfo
    ) {
        boolean hasData = false;
        boolean pageFlg = false;
        
        int first = -1;  // 生徒リストのインデックス
        int last = -1;  // 生徒リストのインデックス
        int page = 0;  // ページ
        for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (-1 == first) {
                first = hrInfo._students.indexOf(student);
                pageFlg = true;
            }
            last = hrInfo._students.indexOf(student);

            int nowpage = (int) Math.floor((double) student._gnum / (double) _param.getFormMaxLine());
            if (page != nowpage) {
                page = nowpage;
                List list = hrInfo._students.subList(first, last + 1);
                first = last;
                pageFlg = false;

                if (printHrInfo(db2, svf, hrInfo, list)) hasData = true;
            }
        }
        if (pageFlg && 0 <= last) {
            List list = hrInfo._students.subList(first, last + 1);

            if (printHrInfo(db2, svf, hrInfo, list)) hasData = true;
        }
        
        return hasData;
    }
    
    /**
     * 学級ごと、生徒MAX行ごとの印刷。
     * @param svf
     * @param hrInfo：年組
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private boolean printHrInfo(
    		final DB2UDB db2,
            final Vrw32alp svf,
            final HRInfo hrInfo,
            final List stulist
    ) {
        boolean hasData = false;

        int line = 0;  // 科目の列番号
        int subclassesnum = 0;

        final Collection subClasses = hrInfo._subclasses.values();

        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subclass = (SubClass) it.next();
            final boolean notOutputColumn = "90".equals(subclass._classcode) && _param._notOutputSougou;
            if (notOutputColumn) {
                continue;
            }
            subclassesnum += 1; 
        }

        for (final Iterator it = subClasses.iterator(); it.hasNext();) {

            if (0 == line % _param.getFormMaxColumn()) {
                svf.VrSetForm(_param.getFormFile(), 4);  //SVF-FORM設定
                printHeader(db2, svf, hrInfo);
                for (final Iterator its = stulist.iterator(); its.hasNext();) {
                    final Student student = (Student) its.next();
                    printStudentOnPage(svf, student);
                }  // 生徒名等を印字
            }

            final SubClass subclass = (SubClass) it.next();
            final boolean notOutputColumn = "90".equals(subclass._classcode) && _param._notOutputSougou;
            if (notOutputColumn) {
                //log.debug("subclass classcode = " + subclass.getClasscode() + " ( subclassname = " + subclass.getAbbv() + " , subclasscode = " + subclass.getSubclasscode() + ")");
                continue;
            }
            
            if (subclassesnum == line + 1) { 
                // 生徒別総合成績および出欠を印字
                for (final Iterator it1 = stulist.iterator(); it1.hasNext();) {
                    final Student student = (Student) it1.next();
                    printStudentOnLastpage(svf, student);
                }
                printHrInfoOnLastPage(svf, hrInfo);
            }

            // 該当科目名等を印字
            printSubclass(svf, subclass, line); 
            
            // 生徒別該当科目成績を印字する処理
            for (final Iterator it1 = stulist.iterator(); it1.hasNext();) {
                final Student student = (Student) it1.next();
                printScoreDetail(svf, subclass, line, student);
            }
            svf.VrEndRecord();
            
            final int used = 1;
            line += used;
            if (0 < used) { hasData = true; }
        }

        return hasData;
    }
    
    /**
     * ページ見出し・項目・欄外記述を印刷します。
     * @param svf
     * @param hrInfo
     */
    private void printHeader(
    		final DB2UDB db2,
            final Vrw32alp svf,
            final HRInfo hrInfo
    ) {
        svf.VrsOut("year2", _param.getNendo(db2));
        svf.VrsOut("ymd1", _param.getNow(db2)); // 作成日
        svf.VrsOut("DATE", _param.getTermKekka(db2));  // 欠課の集計範囲
        svf.VrsOut("DATE2", _param.getTermAttend(db2));  // 「出欠の記録」の集計範囲

        svf.VrsOut("teacher", hrInfo._staffName);  //担任名
        svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
        if (_knjdObj._hasCompCredit) {
            svf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
        }
        // svf.VrsOut("lesson20", hrInfo.getHrMLesson());  // 授業日数

        // 一覧表枠外の文言
        svf.VrAttribute("MARK101",  ATTRIBUTE_MIRISYU);
        svf.VrsOut("MARK101",  " " );
        svf.VrsOut("NOTE2_1",  "　：単位保留者");

        String comment = _param._useAbsenceWarn ? "注意" : "超過";
        svf.VrAttribute("MARK102",  ATTRIBUTE_MIRISYU);
        svf.VrsOut("MARK102",  " " );
        svf.VrsOut("NOTE2_2",  "　：未履修" + comment + ",特活進級" + comment );

        svf.VrAttribute("MARK103",  ATTRIBUTE_MISYUTOKU);
        svf.VrsOut("MARK103",  " " );
        svf.VrsOut("NOTE2_3",  "　：未修得" + comment );

        svf.VrAttribute("MARK104",  ATTRIBUTE_SLUMP);
        svf.VrsOut("MARK104",  " " );
        svf.VrsOut("NOTE2_4",  "　：不振");
        svf.VrsOut("TITLE" , _knjdObj._title);
        svf.VrsOut("MARK", "/");
        svf.VrsOut("PRGID", _param._prgId);
        svf.VrsOut("PRINT_DAY", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
    }

    /**
     * 科目項目(教科名・科目名・)を印刷します。
     * @param svf
     * @param line 科目の列番
     */
    private void printSubclass(
            final Vrw32alp svf, 
            SubClass subclass,
            int line
    ) {
        int maxColumn = _param.getFormMaxColumn();
        int i = ((line + 1) % maxColumn == 0)? maxColumn: (line + 1) % maxColumn;
        if (log.isDebugEnabled()) { log.debug("subclassname="+subclass._subclasscode + " " + subclass._subclassabbv+"   line="+line + "  i="+i); }

        svf.VrsOut("course1", subclass._classabbv); //教科名

        //科目名
        svf.VrsOut("subject1", subclass._subclassabbv);
        if (subclass._electdiv) svf.VrAttribute("subject1", "Paint=(2,70,2),Bold=1");

        //単位数
        if (0 != subclass._maxcredit) {
            String credits = "";
            if (subclass._maxcredit == subclass._mincredit) {
                credits = String.valueOf(subclass._maxcredit);
            } else {
                credits = String.valueOf(subclass._mincredit) + " \uFF5E " + String.valueOf(subclass._maxcredit);
            }
            svf.VrsOut("credit1", credits);
        }
        
        svf.VrsOut("SUB_TEACHER", subclass._subclassstaffname);

        //授業時数
        if (0 != subclass._jisu) { svf.VrsOut("lesson1",  String.valueOf(subclass._jisu)); }
        //学級平均・合計
        // if (null != subclass._scoreaverage) { svf.VrsOut("TOTAL_AVERAGE",  subclass._scoreaverage); }
        svf.VrsOut("RANK1", _param.getRankName());
        if (null != subclass._scoresubaverage) { svf.VrsOut("TOTAL_AVERAGE",  subclass._scoresubaverage); }
        if (null != subclass._scoresubtotal) { svf.VrsOut("TOTAL_SUBCLASS",  subclass._scoresubtotal + "/" + subclass._scoresubCount); }
        if (null != subclass._scoresubMax) { svf.VrsOut("TOTAL_MAX",  subclass._scoresubMax); }
        if (null != subclass._scoresubMin) { svf.VrsOut("TOTAL_MIN",  subclass._scoresubMin); }
        if (null != subclass._scoreFailCnt) { svf.VrsOut("TOTAL_FAULT",  subclass._scoreFailCnt); }
        
        svf.VrsOut("ITEM0", _knjdObj.getItem0Name());
        svf.VrsOut("ITEM1", _knjdObj._item1Name);
        svf.VrsOut("ITEM2", "欠時");
        svf.VrsOut("ITEM3", "遅早");
        svf.VrsOut("ITEM4", "公欠");
        if (_param.isPatternC()) {
            svf.VrsOut("ITEM5", "忌引");
            svf.VrsOut("ITEM6", "出停");
        } else {
            svf.VrsOut("ITEM5", "忌引出停");
        }
    }
    
    /**
     * 学級データの印字処理(学級平均)
     * @param svf
     */
    private void printHrInfoOnLastPage(
            final Vrw32alp svf,
            final HRInfo hrInfo
    ) {
        //学級平均
        final int formMaxLine = _param.getFormMaxLine();
        if (null != hrInfo._avgHrTotal) {
            svf.VrsOut("TOTAL" + String.valueOf(formMaxLine + 2), hrInfo._avgHrTotal);
        }
        if (null != hrInfo._avgHrAverage) {
            svf.VrsOut("AVERAGE" + String.valueOf(formMaxLine + 2), hrInfo._avgHrAverage);
        }
        //欠点者数
        if (null != hrInfo._failHrTotal) {
            svf.VrsOut("TOTAL" + String.valueOf(formMaxLine + 4), hrInfo._failHrTotal);
        }
        //最高点
        if (null != hrInfo._maxHrTotal) {
            svf.VrsOut("TOTAL" + String.valueOf(formMaxLine + 5), hrInfo._maxHrTotal);
        }
        //最低点
        if (null != hrInfo._minHrTotal) {
            svf.VrsOut("TOTAL" + String.valueOf(formMaxLine + 6), hrInfo._minHrTotal);
        }
        //出席率・欠席
        if (null != hrInfo._perHrPresent) {
            svf.VrsOut("PER_ATTEND", DEC_FMT1.format(hrInfo._perHrPresent.setScale(1,BigDecimal.ROUND_HALF_UP)));
        }
        if (null != hrInfo._perHrAbsent) {
            svf.VrsOut("PER_ABSENCE", DEC_FMT1.format(hrInfo._perHrAbsent.setScale(1,BigDecimal.ROUND_HALF_UP)));
        }
    }

    /**
     * 生徒別科目別素点・評定・欠課時数等を印刷します。
     * @param svf
     * @param line 科目の列番
     */
    private void printScoreDetail(
            final Vrw32alp svf,
            final SubClass subclass,
            final int line,
            final Student student
    ) {
        final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);

        if (detail == null) {
            return;
        }
        
        if (_knjdObj._isPrintScore) {
            // 素点
            if (null != detail._score) {
//                if (detail._score.getScoreAsInt() <= _param.getFailValue()) {
//                    svf.VrAttribute("rate"+ student._gnum,  ATTRIBUTE3);
//                }
                svf.VrsOut("rate"+ student._gnum,  detail._score._strScore );
            }
            if (detail._isSlump) {
                svf.VrAttribute("rate"+ student._gnum,  ATTRIBUTE_SLUMP);
            }
        } else {
            // 成績
            if (null != detail._patternAssess) {
//              if (detail._patternAssess.getScoreAsInt() <= _param.getFailValue()) {
//                    svf.VrAttribute("rate"+ student._gnum,  ATTRIBUTE3);
//                }
                svf.VrsOut("rate"+ student._gnum,  detail._patternAssess._strScore);
//                if (knjdobj.doPrintCreditDrop() && 1 == detail._patternAssess.getScoreAsInt()) {
//                    svf.VrsOut("rate"+ gnum,  "*" + detail._patternAssess.getScore());
//                } else {
//                    svf.VrsOut("rate"+ gnum,  detail._patternAssess.getScore());
//                }
            }
            if (detail._isSlump) {
                svf.VrAttribute("rate"+ student._gnum,  ATTRIBUTE_SLUMP);
            }
        }
        
//        // 欠課
//        if (null != detail._absent1) {
//            final int value = (int) Math.round(detail._absent1.doubleValue() * 10.0);
//            final String field = _param.getSvfFieldNameKekka();
//            if (0 != value) {
//                if (detail.compIsOver(student)) {
//                    svf.VrAttribute(field+ student._gnum,  ATTRIBUTE1);
//                } else if (detail.getIsOver(student)) {
//                    svf.VrAttribute(field+ student._gnum,  ATTRIBUTE2);
//                }
//                svf.VrsOut(field+ student._gnum,  _param.getAbsentFmt().format(detail._absent1.floatValue()));
//                if (detail.compIsOver(student) || detail.getIsOver(student)) {
//                    svf.VrAttribute(field+ student._gnum,  ATTRIBUTE_NORMAL);
//                }
//            }
//        }
        
        DecimalFormat fmt = new DecimalFormat("0");
        // 欠時
        String kekkaAmikakeAttribute = null;
        if (null != detail._absent0) {
            final String field = "kekka" + student._gnum;
            if (0 != detail._absent0.intValue()) {
                if (detail.compIsOver(student)) {
                    kekkaAmikakeAttribute = ATTRIBUTE_MIRISYU2;
                } else if (detail.getIsOver(student)) {
                    kekkaAmikakeAttribute = ATTRIBUTE_MISYUTOKU2;
                }
                if (null != kekkaAmikakeAttribute) {
                    svf.VrAttribute(field, kekkaAmikakeAttribute);
                }
                svf.VrsOut(field, fmt.format(detail._absent0.intValue()));
                if (null != kekkaAmikakeAttribute) {
                    svf.VrAttribute(field, ATTRIBUTE_NORMAL);
                }
            }
        }
        // 遅早
        if (null != detail._lateEarly1 && detail._lateEarly1.intValue() != 0) {
            final String field = "late" + student._gnum;
            if (null != kekkaAmikakeAttribute) {
                svf.VrAttribute(field, kekkaAmikakeAttribute);
            }
            svf.VrsOut(field, fmt.format(detail._lateEarly1.intValue()));
            if (null != kekkaAmikakeAttribute) {
                svf.VrAttribute(field, ATTRIBUTE_NORMAL);
            }
        }
        // 公欠
        if (null != detail._koketsu && detail._koketsu.intValue() != 0) {
            svf.VrsOut("AUTH_ABSENCE" + student._gnum, fmt.format(detail._koketsu.intValue()));
        }
        if (_param.isPatternC()) {
            // 忌引
            if (null != detail._mourning && detail._mourning.intValue() != 0) {
                svf.VrsOut("MOURNING" + student._gnum, detail._mourning.toString());
            }
            // 出停
            if (null != detail.getSuspendAll() && detail.getSuspendAll().intValue() != 0) {
                svf.VrsOut("SUSPEND" + student._gnum, detail.getSuspendAll().toString());
            }
        } else {
            // 忌引出停
            int suspend = detail.getSuspendAll() == null ? 0 : detail.getSuspendAll().intValue();
            int mourning = detail._mourning == null ? 0 : detail._mourning.intValue();
            if (0 != suspend + mourning) {
                svf.VrsOut("MOURNING" + student._gnum,  String.valueOf(suspend + mourning));
            }
        }
    }
    
    /**
     * ページ毎の印字処理(生徒名/マーク)
     * @param svf
     */
    private void printStudentOnPage(
            final Vrw32alp svf,
            final Student student
    ) {
        svf.VrsOutn("name1", student._gnum, student._name);    // 氏名
        svf.VrsOutn("NUMBER", student._gnum, DEC_FMT2.format(Integer.parseInt(student._attendNo)));  // 出席番号
        svf.VrsOutn("SEX", student._gnum, "2".equals(student._sex) ? "*" : "");  // 性別
        // svf.VrsOutn("REMARK", student._gnum, _transInfo.toString());  // 備考
        
        // 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
        if (_knjdObj._hasJudgementItem) {
            svf.VrsOutn("CHECK1", student._gnum, student.isGradePoor() ? "★" : student.isGradeGood() ? "☆" : "");
            svf.VrsOutn("CHECK2", student._gnum, student.isAttendPerfect() ? "○" : student.isKekkaOver() ? "●" : "");
        }
    }

    /**
     * 最後のページの印字処理（成績総合/出欠の記録）
     * @param svf
     */
    private void printStudentOnLastpage(
            final Vrw32alp svf,
            final Student student
    ) {
        if (student._total != null) {
            // 生徒別総合点・平均点・順位を印刷します。
            final String scoreSum = student._scoreSum;
            final String average = student._scoreAvg;
            if (null != scoreSum) {
                // svf.VrsOutn("TOTAL", student._gnum, scoreSum);  //総合点
                svf.VrsOutn("AVERAGE101", student._gnum, average);  //平均点
            }
            //順位（学級or学年orコース）
            final int rank;
            if (TOTAL_RANK_CLASS.equals(_param._totalRank)) {
                rank = student._classRank;
            } else if (TOTAL_RANK_GRADE.equals(_param._totalRank)) {
                rank = student._gradeRank;
            } else if (TOTAL_RANK_COURSE.equals(_param._totalRank)) {
                rank = student._courseRank;
            } else {
                rank = 0;
            }
            if (1 <= rank) {
                svf.VrsOutn("RANK101", student._gnum, String.valueOf(rank));
            }
            //欠点科目数
            svf.VrsOutn("SLUMP_SUBJECT101", student._gnum, String.valueOf(student._total._countFail));
            //欠点単位数
            svf.VrsOutn("SLUMP_POINT101", student._gnum, String.valueOf(student._total._countFailCredit));
            
            // 特別活動欠課数
            if (!_param._isNotPrintSpecialSubclassKekka) {
                if (0 < student._total._specialAbsent) {
                    final int spAbsenceHigh = null == student._spAbsenceHighMap.get(SPECIAL_ALL) ? 0 : ((BigDecimal) student._spAbsenceHighMap.get(SPECIAL_ALL)).intValue();
                    if (spAbsenceHigh == 0 || student._total._specialAbsent > spAbsenceHigh) {
                        svf.VrAttributen("SP_ABSENT101", student._gnum,  ATTRIBUTE_MIRISYU);
                    }  
                }
                svf.VrsOutn("SP_ABSENT101", student._gnum, String.valueOf(student._total._specialAbsent));
            }
                
        }
        
        if (student._attendInfo != null) {
            AttendInfo ai = student._attendInfo;
            svf.VrsOutn("ATTEND101", student._gnum, String.valueOf(ai._lesson));      // 出席すべき日数
            svf.VrsOutn("PRESENT101", student._gnum, String.valueOf(ai._mLesson));      // 出席すべき日数
            if (_param.isPatternC()) {
                svf.VrsOutn("SUSPEND101", student._gnum, String.valueOf(ai._suspend));      // 出席停止
                svf.VrsOutn("MOURNING101", student._gnum, String.valueOf(ai._mourning));      // 忌引
            } else {
                svf.VrsOutn("SUSPEND101", student._gnum, String.valueOf(ai._suspend + ai._mourning));      // 出席停止忌引
            }
            svf.VrsOutn("ABSENCE101", student._gnum, String.valueOf(ai._absent));       // 欠席日数
            //svf.VrsOutn("ATTEND", student._gnum,  ai._present);      // 出席日数
            //svf.VrsOutn("TOTAL_LATE", student._gnum, ai._late);      // 遅刻回数
            //svf.VrsOutn("LEAVE", student._gnum,   ai._early);        // 早退回数
            //svf.VrsOutn("ABROAD", student._gnum,  ai._transDays);    // 留学等実績
        }
        
        if (student._transInfo != null && student._transInfo._name != null) {
            svf.VrsOutn("REG_STATE101", student._gnum, student._transInfo._name);       // 在籍状況
        }
        
        if (student._remark != null) {
            if (_param.isPatternC()) {
                svf.VrsOutn("REMARK101", student._gnum, student._remark); // 備考
            } else {
                svf.VrsOutn("REMARK1", student._gnum, student._remark); // 備考
            }
        }

        if (_knjdObj._hasCompCredit) {
            //今年度履修単位数
            svf.VrsOutn("COMP_CREDIT", student._gnum, String.valueOf(student._compCredit));
            //今年度修得単位数
            svf.VrsOutn("GET_CREDIT", student._gnum, String.valueOf(student._getCredit));
        }
        if (_knjdObj._hasJudgementItem) {
            // 前年度までの単位数を印字
            // 今年度認定単位数
            svf.VrsOutn("A_CREDIT", student._gnum, String.valueOf(student._qualifiedCredits));
            // 前年度までの修得単位数
            svf.VrsOutn("PRE_C_CREDIT", student._gnum, String.valueOf(student._previousCredits));
            // 修得単位数計
            int t = student._getCredit + student._qualifiedCredits + student._previousCredits;
            if (t != 0) {
                int g = (null != _param._knjSchoolMst._gradCredits ? Integer.parseInt(_param._knjSchoolMst._gradCredits) : 0);  // 卒業認定単位数
                if (g != 0 && g <= t) {
                    svf.VrsOutn("TOTAL_C_CREDIT", student._gnum, "@" + String.valueOf(t));
                } else {
                    svf.VrsOutn("TOTAL_C_CREDIT", student._gnum, String.valueOf(t));
                }
            }
            // 前年度までの未履修科目数
            svf.VrsOutn("PRE_N_CREDIT", student._gnum, String.valueOf(student._previousMirisyu));  
        }
    }

    /**
     * 欠課時分を欠課時数に換算した値を得る
     * @param kekka 欠課時分
     * @return 欠課時分を欠課時数に換算した値
     */
    private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
        final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
        final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
        int hasu = 0;
        final String retSt = bigD.toString();
        final int retIndex = retSt.indexOf(".");
        if (retIndex > 0) {
            hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
        }
        final BigDecimal rtn;
        if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
            rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
        } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
            rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
        } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
            rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
        } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
            rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
        } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
            rtn = bigD;
        } else {
            rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
        }
        return rtn;
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる処理クラス
     */
    private static class TestKind {
        String _title; 
        String _item1Name;  // 明細項目名
//        String _item2Name;  // 明細項目名
//        String _item4Name;  // 総合点欄項目名
//        String _item5Name;  // 平均点欄項目名
        
        boolean _isGakunenMatu;
        boolean _targetValueIsPatternAssess;
        boolean _hasCompCredit; // 履修単位数/修得単位数なし
        final boolean _hasJudgementItem; // 判定会議資料用の項目があるか
        final boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。

        private boolean _isPrintScore;

        private boolean _isPrintDetailTani; // 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
        
        private boolean _creditDrop;
        
//        /**
//         * 欠点 (100段階：30未満, 5段階： 2未満)
//         */
//        protected int _failValue;
        
        public TestKind() {
            _hasJudgementItem = false;
            _enablePringFlg = false;
            
            _isPrintScore = false;
            _isPrintDetailTani = false;
            
//            _failValue = 30;
        }
        
        private ScoreValue getTargetValue(final ScoreDetail d) {
            return _targetValueIsPatternAssess ? d._patternAssess : d._score;
        }
        
        private boolean isGakunenmatu() {
            return _isGakunenMatu;
        }
        
        public String getItem0Name() {
            return (_item1Name == null ? "" : _item1Name + "・") + "欠課・遅早等";
        }

        /** {@inheritDoc} */
        public String toString() {
            return getClass().getName() + ", title = " + _title;
        }
    }

    private static class Course {
        private final String _grade;
        private final String _hrclass;
        private final String _coursecd;
        private final String _name;

        Course(
                final String grade,
                final String hrclass,
                final String coursecd,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _coursecd = coursecd;
            _name = name;
        }

        public String toString() {
            return _coursecd + ":" + _name;
        }
        
        private static List createCourses(final DB2UDB db2, final Param param, final String gradeHrclass) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCourses(gradeHrclass, param);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("COURSECD");
                    final String name = rs.getString("COURSECODENAME");

                    final Course course = new Course(
                            grade,
                            hrclass,
                            coursecd,
                            name
                    );

                    rtn.add(course);
                }
            } catch (final Exception ex) {
                log.error("コースのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlCourses(final String hrclass, final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD || W1.MAJORCD || W1.COURSECODE as COURSECD, ");
            stb.append("     L1.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT W1 ");
            stb.append("     LEFT JOIN COURSECODE_MST L1 ON L1.COURSECODE=W1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + param._year + "' AND ");
            stb.append("     W1.SEMESTER = '" + param.getRegdSemester() + "' AND ");
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE, ");
            stb.append("     L1.COURSECODENAME ");

            return stb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private class HRInfo implements Comparable {
        private final String _hrclassCd;
        private String _staffName;
        private String _hrName;
        private final List _students = new LinkedList();
        private final Map _subclasses = new TreeMap();
        private List _ranking;
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private BigDecimal _perHrPresent;  // 学級の出席率
        private BigDecimal _perHrAbsent;  // 学級の欠席率
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrMLesson;  // 学級の授業日数
        private String _avgHrTotal;   // 総合点の学級平均
        private String _avgHrAverage; // 平均点の学級平均
        private String _maxHrTotal;   // 総合点の最高点
        private String _minHrTotal;   // 総合点の最低点
        private String _failHrTotal;  // 欠点の数

        HRInfo(final String hrclassCd) {
            _hrclassCd = hrclassCd;
        }

        void load(
                final DB2UDB db2,
                final String courseCd
        ) throws Exception {
            loadHRClassStaff(db2);
            loadStudents(db2, courseCd);
            loadStudentsInfo(db2);
            loadAttend(db2);
            loadHrclassAverage(db2, courseCd);
            loadRank(db2);
            loadScoreDetail(db2, _knjdObj);
            _ranking = createRanking();
            log.debug("RANK:" + _ranking);
            setSubclassAverage();        
            setHrTotal();  // 学級平均等の算出
            setHrTotalMaxMin();        
            setHrTotalFail();
            setSubclassGradeAverage(db2, _hrclassCd, courseCd);
            setAbsenceHigh(db2);
            setHtestRemark(db2);
            if (_knjdObj._hasJudgementItem) {
                loadPreviousCredits(db2);  // 前年度までの修得単位数取得
                loadPreviousMirisyu(db2);  // 前年度までの未履修（必須科目）数
                loadQualifiedCredits(db2);  // 今年度の資格認定単位数
            }
        }

        private void loadHRClassStaff(
                final DB2UDB db2
        ) {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            final KNJ_Get_Info.ReturnVal returnval = getinfo.Hrclass_Staff(
                    db2,
                    _param._year,
                    _param._semester,
                    _hrclassCd,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private void loadStudents(
                final DB2UDB db2,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassStdList(_hrclassCd, courseCd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
                    if (_param._noKetuban) {
                        gnum = gnum + 1;
                    } else {
                        gnum = rs.getInt("ATTENDNO") % 100 == 0 ? rs.getInt("ATTENDNO") : (rs.getInt("ATTENDNO") % 100);
                    }
                    final Student student = new Student(rs.getString("SCHREGNO"),
                            this,
                            rs.getString("COURSECD"),
                            rs.getString("MAJORCD"),
                            rs.getString("GRADE"),
                            rs.getString("COURSECODE"),
                            gnum);
                    _students.add(student);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /**
         * SQL HR組の学籍番号を取得するSQL
         */
        private String sqlHrclassStdList(final String hrClass, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W1.COURSECD,W1.MAJORCD,W1.GRADE,W1.COURSECODE ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(    "AND W1.SEMESTER = '" + _param.getRegdSemester() + "' ");
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
            if (_param._outputCourse) {
                stb.append(    "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
            }
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudentsInfo(final DB2UDB db2) {

            final String sql = sqlStdNameInfo();

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final Student student = getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                TransInfo transInfo = new TransInfo(null, null, null);

                final String d1 = KnjDbUtils.getString(row, "KBN_DATE1");
                if (null != d1) {
                    final String n1 = KnjDbUtils.getString(row, "KBN_NAME1");
                    transInfo = new TransInfo(d1, KNJ_EditDate.h_format_JP(db2, d1), n1);
                }

                final String d2 = KnjDbUtils.getString(row, "KBN_DATE2");
                if (null != d2) {
                    final String n2 = KnjDbUtils.getString(row, "KBN_NAME2");
                    transInfo = new TransInfo(d2, KNJ_EditDate.h_format_JP(db2, d2), n2);
                }

                student.setInfo(
                		KnjDbUtils.getString(row, "ATTENDNO"),
                		KnjDbUtils.getString(row, "NAME"),
                		KnjDbUtils.getString(row, "SEX"),
                        transInfo
                );
            }
        }
        
        /**
         * SQL 任意の生徒の学籍情報を取得するSQL
         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
         */
        private String sqlStdNameInfo() {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W3.SEX, W6.HR_NAME, ");
            stb.append(        "CASE WHEN W4_2.GRD_DATE IS NOT NULL THEN W4_2.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4_2.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4_2.GRD_DIV) ");
            stb.append(                                            "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + _param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND W4.ENT_DIV IN ('4','5') AND W4.ENT_DATE > W2.SDATE ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4_2 ON W4_2.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND W4_2.GRD_DIV IN ('2','3') AND W4_2.GRD_DATE < W2.EDATE ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(    "AND W1.SCHREGNO IN ").append(getStudentsqlWhereIn());
            stb.append(    "AND W1.SEMESTER = '" + _param.getRegdSemester() + "' ");
            return stb.toString();
        }

        private Student getStudent(String code) {
            if (code == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (code.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }

        private void loadAttend(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", _param._useCurriculumcd);
                paramMap.put("useVirus", _param._useVirus);
                paramMap.put("useKoudome", _param._useKoudome);
                paramMap.put("DB2UDB", db2);

                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._semesFlg,
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        (String) _param._hasuuMap.get("attendSemesInState"),
                        _param._periodInState,
                        (String) _param._hasuuMap.get("befDayFrom"),
                        (String) _param._hasuuMap.get("befDayTo"),
                        (String) _param._hasuuMap.get("aftDayFrom"),
                        (String) _param._hasuuMap.get("aftDayTo"),
                        _param._grade,
                        _hrclassCd.substring(2, 5),
                        null,
                        "SEMESTER",
                        paramMap
                );
                log.fatal(" semesFlg = " + _param._semesFlg);
                log.fatal(" attendSemesInState = " + _param._hasuuMap.get("attendSemesInState"));
                log.fatal(" befDayFrom = " + _param._hasuuMap.get("befDayFrom"));
                log.fatal(" befDayTo = " + _param._hasuuMap.get("befDayTo"));
                log.fatal(" aftDayFrom = " + _param._hasuuMap.get("aftDayFrom"));
                log.fatal(" aftDayTo = " + _param._hasuuMap.get("aftDayTo"));
                // log.fatal(" sql = " + sql);
                ps = db2.prepareStatement(sql);

                String targetSemes = (_param._isRuikei) ? "9" : _param._semester;
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!targetSemes.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    final AttendInfo attendInfo = new AttendInfo(
                            rs.getInt("LESSON"),
                            rs.getInt("MLESSON"),
                            rs.getInt("SUSPEND") + ("true".equals(_param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(_param._useKoudome) ? rs.getInt("KOUDOME") : 0),
                            rs.getInt("MOURNING"),
                            rs.getInt("SICK"),
                            rs.getInt("PRESENT"),
                            rs.getInt("LATE"),
                            rs.getInt("EARLY"),
                            rs.getInt("TRANSFER_DATE")
                    );
                    student._attendInfo = attendInfo;
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadHrclassAverage(
                final DB2UDB db2,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassAverage(_hrclassCd, courseCd);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                    _avgHrTotal = rs.getString("AVG_HR_TOTAL");
                    _avgHrAverage = rs.getString("AVG_HR_AVERAGE");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private String sqlHrclassAverage(final String hrClass, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if ("9".equals(_param._semester)) {
                stb.append( "    AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
            if (_param._outputCourse) {
                stb.append(         "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
            }
            stb.append(") ");

            //メイン表
            stb.append("SELECT  DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append(  "FROM  " + _param._rankTable + " W3 ");
            stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
            stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
            stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
            stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
            stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");

            return stb.toString();
        }

        private void loadRank(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdTotalRank();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    
                    student._classRank = rs.getInt("CLASS_RANK");
                    student._gradeRank = rs.getInt("GRADE_RANK");
                    student._courseRank = rs.getInt("COURSE_RANK");
                    student._scoreSum = (rs.getString("TOTAL_SCORE"));
                    student._scoreAvg = (rs.getString("TOTAL_AVG"));
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /**
         * SQL 任意の生徒の順位を取得するSQL
         */
        private String sqlStdTotalRank() {
            final StringBuffer stb = new StringBuffer();
            
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if ("9".equals(_param._semester)) {
                stb.append( "    AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.SCHREGNO IN ").append(getStudentsqlWhereIn());
            stb.append(") ");

            //メイン表
            stb.append("SELECT  W3.SCHREGNO ");
            if (_param._useAverageAsKijunten) {
                stb.append(   ",CLASS_AVG_RANK AS CLASS_RANK");
                stb.append(   ",GRADE_AVG_RANK  AS GRADE_RANK ");
                stb.append(   ",COURSE_AVG_RANK AS COURSE_RANK ");
            } else {
                stb.append(   ",CLASS_RANK ");
                stb.append(   ",GRADE_RANK ");
                stb.append(   ",COURSE_RANK ");
            }
            stb.append(       ",W3.SCORE AS TOTAL_SCORE ");
            stb.append(       ",DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
            stb.append(  "FROM  " + _param._rankTable + " W3 ");
            stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
            stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
            stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
            stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
            stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            // 総合点・平均点を算出。順位は空白。法政以外の処理。
            // RECORD_RANK_DAT.SUBCLASSCD = '999999' のレコードがない生徒のみ算出。
            if (!_param.isHousei()) {
                stb.append("UNION ");
                stb.append("SELECT  W3.SCHREGNO ");
                stb.append(       ",cast(null as smallint) AS CLASS_RANK ");
                stb.append(       ",cast(null as smallint) AS GRADE_RANK ");
                stb.append(       ",cast(null as smallint) AS COURSE_RANK ");
                stb.append(       ",SUM(W3.SCORE) AS TOTAL_SCORE ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append(  "FROM  " + _param._rankTable + " W3 ");
                stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SUBCLASS_MST W2 WHERE ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("               W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD = ");
                    stb.append("               W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || W2.SUBCLASSCD ");
                } else {
                    stb.append("               W3.SUBCLASSCD = W2.SUBCLASSCD ");
                }
                stb.append("               ) ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                stb.append(   "AND  W3.SCHREGNO NOT IN( ");
                stb.append(        "SELECT  R1.SCHREGNO ");
                stb.append(          "FROM  " + _param._rankTable + " R1 ");
                stb.append(         "WHERE  R1.YEAR = '" + _param._year + "' ");
                stb.append(           "AND  R1.SEMESTER = '" + _param._semester + "' ");
                stb.append(           "AND  R1.TESTKINDCD || R1.TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append(           "AND  R1.SUBCLASSCD = '999999' ");
                stb.append(           "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE R1.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                stb.append(   ") ");
                stb.append("GROUP BY W3.SCHREGNO ");
            }

            return stb.toString();
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final TestKind knjdObj
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdSubclassDetail(_hrclassCd);
                // log.debug(" subclassDetail = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (knjdObj._enablePringFlg && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    
                    final ScoreDetail scoreDetail = createScoreDetail(rs, _subclasses);
                    student.add(scoreDetail);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", _param._useCurriculumcd);
                paramMap.put("useVirus", _param._useVirus);
                paramMap.put("useKoudome", _param._useKoudome);
                paramMap.put("DB2UDB", db2);

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        _param._semesFlg,
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        (String) _param._hasuuMap.get("attendSemesInState"),
                        _param._periodInState,
                        (String) _param._hasuuMap.get("befDayFrom"),
                        (String) _param._hasuuMap.get("befDayTo"),
                        (String) _param._hasuuMap.get("aftDayFrom"),
                        (String) _param._hasuuMap.get("aftDayTo"),
                        _hrclassCd.substring(0, 2),
                        _hrclassCd.substring(2),
                        null,
                        paramMap
                        );
                log.debug(" attend subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    
                    ScoreDetail scoreDetail = null;
                    for (final Iterator it = student._scoreDetails.keySet().iterator(); it.hasNext();) {
                        final String subclasscd = (String) it.next();
                        if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                            scoreDetail = (ScoreDetail) student._scoreDetails.get(subclasscd);
                            break;
                        }
                    }
                    if (null == scoreDetail) {
                        SubClass subClass = null;
                        for (final Iterator it = _subclasses.keySet().iterator(); it.hasNext();) {
                            final String subclasscd = (String) it.next();
                            if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                                subClass = (SubClass) _subclasses.get(subclasscd);
                                scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null);
                                student._scoreDetails.put(subclasscd, scoreDetail);
                                break;
                            }
                        }
                        if (null == scoreDetail) {
                            log.fatal(" no detail " + student._schregno + ", " + rs.getString("SUBCLASSCD"));
                            continue;
                        }
                    }
                    
                    final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");

                    if (specialGroupCd != null) {
                        Integer specialLessonMinutes = getIntObject(rs, "SPECIAL_LESSON_MINUTES");
                        Integer specialAbsentMinutes = null;

                        String field = null;
                        final String rawSubclasscd;
                        if ("1".equals(_param._useCurriculumcd)) {
                            rawSubclasscd = StringUtils.split(subclassCd, "-")[3];
                        } else {
                            rawSubclasscd = subclassCd;
                        }
                        if (_param._subClassC005.containsKey(rawSubclasscd)) {
                            String is = (String) _param._subClassC005.get(rawSubclasscd);
                            if ("1".equals(is)) {
                                field = "SPECIAL_SICK_MINUTES3";
                            } else if ("2".equals(is)) {
                                field = "SPECIAL_SICK_MINUTES2";
                            }
                        } else {
                            field = "SPECIAL_SICK_MINUTES1";
                        }
                        try {
                            specialAbsentMinutes = new Integer(new Double(rs.getString(field)).intValue());
                        } catch (Exception e) {
                            log.error(" schregno = " + student._schregno + " field = " + field + " , rs.getObject --> " + rs.getObject(field));
                            specialAbsentMinutes = new Integer(0);
                        }
                        add(student._spGroupLessonMinutes, specialGroupCd, subclassCd, specialLessonMinutes.intValue());
                        add(student._spGroupAbsentMinutes, specialGroupCd, subclassCd, specialAbsentMinutes.intValue());
                    }
                    
                    scoreDetail._subClass._spGroupCd = specialGroupCd;
                    if (0 != rs.getInt("MLESSON") && scoreDetail._subClass._jisu < rs.getInt("MLESSON")) {
                        scoreDetail._subClass._jisu = rs.getInt("MLESSON");
                    }
                    if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                        scoreDetail._absent0 = getIntObject(rs, "RAW_REPLACED_SICK");
                        scoreDetail._absent1 = getDoubleObject(rs, "REPLACED_SICK");
                    } else {
                        scoreDetail._absent0 = getIntObject(rs, "SICK1");
                        scoreDetail._absent1 = getDoubleObject(rs, "SICK2");
                    }
                    scoreDetail._koketsu = getIntObject(rs, "ABSENT");
                    scoreDetail._mourning = getIntObject(rs, "MOURNING");
                    scoreDetail._suspend = getIntObject(rs, "SUSPEND");
                    scoreDetail._virus = getIntObject(rs, "VIRUS");
                    scoreDetail._koudome = getIntObject(rs, "KOUDOME");
                    scoreDetail._lateEarly1 = "1".equals(_param._chikokuHyoujiFlg) ? new Integer(rs.getInt("LATE") + rs.getInt("EARLY")) : new Integer(rs.getInt("LATE2") + rs.getInt("EARLY2"));
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private Integer getIntObject(final ResultSet rs, final String field) throws SQLException {
        	final Number o = (Number) rs.getObject(field);
        	return null == o ? null : o.intValue();
        }
        
        private Double getDoubleObject(final ResultSet rs, final String field) throws SQLException {
        	final Number o = (Number) rs.getObject(field);
        	return null == o ? null : o.doubleValue();
        }
        
        public void add(final Map spGroupMinutes, final String specialGroupCd, final String subclasscd, final int minutes) {
            if (!spGroupMinutes.containsKey(specialGroupCd)) {
                spGroupMinutes.put(specialGroupCd, new HashMap());
            }
            final Map subclassMinutes = (Map) spGroupMinutes.get(specialGroupCd);
            if (null == subclassMinutes.get(subclasscd)) {
                subclassMinutes.put(subclasscd, new Integer(0));
            }
            final int min = ((Integer) subclassMinutes.get(subclasscd)).intValue();
            subclassMinutes.put(subclasscd, new Integer(min + minutes));
        }
        
        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(String hrclassCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,0 AS LEAVE");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
//            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
//            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
//            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
//            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END ");
//            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
//            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
//            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
//            stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._date + "' THEN W2.EDATE ELSE '" + _param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if ("9".equals(_param._semester)) {
                stb.append( "    AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + hrclassCd + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "W2.CLASSCD , W2.SCHOOL_KIND , W2.CURRICULUM_CD, ");
                stb.append(        "W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE, MIN(W3.STAFFCD) AS STAFFCD1, MIN(W4.STAFFCD) AS STAFFCD2 ");
            stb.append(     "FROM   CHAIR_STD_DAT W1 ");
            stb.append(     " INNER JOIN CHAIR_DAT W2 ON W1.YEAR = W2.YEAR AND W1.SEMESTER = W2.SEMESTER AND W1.CHAIRCD = W2.CHAIRCD ");
            stb.append(     " LEFT JOIN CHAIR_STF_DAT W3 ON W1.YEAR = W3.YEAR AND W1.SEMESTER = W3.SEMESTER AND W1.CHAIRCD = W3.CHAIRCD AND W3.CHARGEDIV = 1 ");
            stb.append(     " LEFT JOIN CHAIR_STF_DAT W4 ON W1.YEAR = W4.YEAR AND W1.SEMESTER = W4.SEMESTER AND W1.CHAIRCD = W4.CHAIRCD AND W4.CHARGEDIV = 0 ");
            stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     "GROUP BY ");
            stb.append(        "W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "W2.CLASSCD , W2.SCHOOL_KIND , W2.CURRICULUM_CD, ");
                stb.append(        "W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append(        "W2.SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append(     ")");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append(            "SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                    "T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.CREDITS");
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + _param._year + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                          AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("            COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + _param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("             COMBINED_SUBCLASSCD");
            stb.append(") ");       
            
            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
            if (_param._testKindCd.startsWith("01") || _param._testKindCd.startsWith("02")) {
                //中間・期末成績
                stb.append(       ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(       ",'' AS PATTERN_ASSESS ");
            } else {
                //学期・学年成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
            }
            stb.append(    "FROM    " + _param._rankTable + " W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");
            
            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD");
            stb.append(           ",W3.COMP_CREDIT ,GET_CREDIT ");
            stb.append(           ",CASE WHEN VALUE IS NOT NULL THEN RTRIM(CHAR(W3.VALUE)) ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append("            W3.SCORE_DIV = '00' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //不振科目 
            stb.append(",RECORD_SLUMP AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(    "FROM    RECORD_SLUMP_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append("            W3.SLUMP = '1' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W1.SCHREGNO = W3.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //追試試験データの表
            stb.append(",SUPP_EXA AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD,SCORE_FLG ");
            stb.append(           ",CASE WHEN SCORE_PASS IS NOT NULL THEN RTRIM(CHAR(SCORE_PASS)) ");
            stb.append(                 "ELSE NULL END AS SCORE_PASS ");
            stb.append(    "FROM    SUPP_EXA_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //メイン表
            stb.append(" SELECT  value(T7.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T34.SCORE_FLG ");
            stb.append("        ,CASE WHEN T34.SCORE_FLG IS NOT NULL THEN T34.SCORE_PASS ");
            stb.append("              ELSE T3.SCORE END AS SCORE ");
            if ("on".equals(_param._valueFlg)) {
                stb.append("        ,T33.PATTERN_ASSESS ");
            } else {
                stb.append("        ,T3.PATTERN_ASSESS ");
            }
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSABBV AS SUBCLASSNAME ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,T15.STAFFNAME AS SUBCLASS_STAFFNAME ");
            stb.append("        ,T16.SCHREGNO AS SLUMP ");
            //対象生徒・講座の表
            stb.append(" FROM(");
            stb.append("      SELECT W2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("               W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("             W2.SUBCLASSCD,VALUE(W2.STAFFCD1,W2.STAFFCD2) AS STAFFCD ");
            stb.append("      FROM CHAIR_A W2");
            stb.append("      WHERE ");
            stb.append("          W2.CHAIRCD = (SELECT MAX(CHAIRCD) FROM CHAIR_A ");
            stb.append("                        WHERE SCHREGNO = W2.SCHREGNO AND SUBCLASSCD = W2.SUBCLASSCD AND SEMESTER = W2.SEMESTER) ");
            if (!"9".equals(_param._semester)) {
                stb.append("      AND W2.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("      GROUP BY W2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("               W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("               W2.SUBCLASSCD, VALUE(W2.STAFFCD1,W2.STAFFCD2) ");
            stb.append("    ) T1 ");
            //成績の表
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,W3.SCORE,W3.PATTERN_ASSESS");
            stb.append("   FROM   RECORD_REC W3");
            stb.append(" )T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,COMP_CREDIT,GET_CREDIT,PATTERN_ASSESS");
            stb.append("   FROM   RECORD_SCORE W3");
            stb.append(" )T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,SCORE_FLG,SCORE_PASS");
            stb.append("   FROM   SUPP_EXA W3");
            stb.append(" )T34 ON T34.SUBCLASSCD = T1.SUBCLASSCD AND T34.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(           "COMBINED_SUBCLASSCD");
            stb.append(" )T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(           "ATTEND_SUBCLASSCD");
            stb.append(" )T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        " T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append(                        "T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(                        "T8.CLASSCD || '-' || T8.SCHOOL_KIND = ");
                stb.append(                        "T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(                        "T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
            }
            stb.append(" LEFT JOIN STAFF_MST T15 ON T15.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN RECORD_SLUMP T16 ON T16.SCHREGNO = T1.SCHREGNO AND T16.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        // 前年度までの修得単位数計
        private void loadPreviousCredits(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdPreviousCredits();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._previousCredits = rs.getInt("CREDIT");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 前年度までの修得単位数計
         * @return
         */
        private String sqlStdPreviousCredits() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SCHREGNO, SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO IN ").append(getStudentsqlWhereIn());
            stb.append("    AND T1.YEAR < '" + _param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            stb.append(" GROUP BY SCHREGNO ");

            return stb.toString();
        }

        // 前年度までの未履修（必須科目）数
        private void loadPreviousMirisyu(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdPreviousMirisyu();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._previousMirisyu = rs.getInt("COUNT");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 前年度までの未履修（必須科目）数
         * @return
         */
        private String sqlStdPreviousMirisyu() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SCHREGNO, COUNT(*) AS COUNT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" INNER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T2.SUBCLASSCD");
            stb.append(" WHERE  T1.SCHREGNO IN ").append(getStudentsqlWhereIn());
            stb.append("    AND T1.YEAR < '" + _param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            stb.append("    AND VALUE(T2.ELECTDIV,'0') <> '1'");
            stb.append("    AND VALUE(T1.COMP_CREDIT,0) = 0");
            stb.append(" GROUP BY SCHREGNO ");

            return stb.toString();
        }

        // 今年度の資格認定単位数
        private void loadQualifiedCredits(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdQualifiedCredits();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._qualifiedCredits = rs.getInt("CREDITS");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /**
         * 欠点の算出
         */
        private void setHrTotalFail() {
            int countFail = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final Total totalObj = student._total;
                if (null != totalObj) {
                    if (0 < totalObj._countFail) {
                        countFail += totalObj._countFail;
                    }
                }
            }
            if (0 < countFail) {
                _failHrTotal = String.valueOf(countFail);
            }
        }

        /**
         * 最高点・最低点の算出
         */
        private void setHrTotalMaxMin() {
            int totalMax = 0;
            int totalMin = Integer.MAX_VALUE;
            int countT = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final String scoreSum = student._scoreSum;
                if (null == scoreSum) continue;
                countT++;
                final int totalInt = Integer.parseInt(scoreSum);
                //最高点
                totalMax = Math.max(totalMax, totalInt);
                //最低点
                totalMin = Math.min(totalMin, totalInt);
            }
            if (0 < countT) {
                _maxHrTotal = String.valueOf(totalMax);
                _minHrTotal = String.valueOf(totalMin);
            }
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         */
        private void setSubclassAverage() {
            final Map map = new HashMap();

            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                for (final Iterator itD = student._scoreDetails.values().iterator(); itD.hasNext();) {
                    final ScoreDetail detail = (ScoreDetail) itD.next();
                    final ScoreValue val = _knjdObj.getTargetValue(detail);
                    if (null == val || !val.hasIntValue()) {
                        continue;
                    }
                    final SubClass subClass = detail._subClass;
                    int[] arr = (int[]) map.get(subClass);
                    if (null == arr) {
                        arr = new int[5];
                        map.put(subClass, arr);
                    }
                    arr[0] += val.getScoreAsInt();
                    arr[1]++;
                    //最高点
                    arr[2] = Math.max(arr[2], val.getScoreAsInt());
                    //最低点
                    if (arr[1] == 1) {
                        arr[3] = val.getScoreAsInt();
                    }
                    arr[3] = Math.min(arr[3], val.getScoreAsInt());
                    //欠点（赤点）
//                    if (_param.getFailValue() >= val.getScoreAsInt()) {
//                        arr[4]++;
//                    }
                    if (detail._isSlump) {
                        arr[4]++;
                    }
                }
            }

            for (final Iterator it = _subclasses.values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                if (map.containsKey(subclass)) {
                    final int[] val = (int[]) map.get(subclass);
                    double d = 0;
                    if (0 != val[1]) {
                        d = round10(val[0], val[1]);
                        subclass._scoreaverage = DEC_FMT1.format(d);
                        subclass._scoretotal = String.valueOf(val[0]);
                        subclass._scoreCount = String.valueOf(val[1]);
                        subclass._scoreMax = String.valueOf(val[2]);
                        subclass._scoreMin = String.valueOf(val[3]);
                        if (0 != val[4]) subclass._scoreFailCnt = String.valueOf(val[4]);
                    }
                }
            }
            
        }

        private double round10(final int a, final int b) {
            return Math.round(a * 10.0 / b) / 10.0;
        }
        
        /**
         * 学級平均の算出
         */
        private void setHrTotal() {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int totalMlesson = 0;
            int totalPresent = 0;
            int totalAbsent = 0;
            
            int maxCredit = 0;
            int minCredit = Integer.MAX_VALUE;
            int maxLesson = 0;
            int minLesson = Integer.MAX_VALUE;
            
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final Total totalObj = student._total;
                if (null != totalObj) {
                    if (0 < totalObj._count) {
                        totalT += totalObj._total;
                        countT++;
                    }
//                    if (null != totalObj._avgBigDecimal) {
//                        totalA += totalObj._avgBigDecimal.doubleValue();
//                        countA++;
//                    }
//                    if (0< totalObj._avgcount) {
//                        totalA += totalObj._avgtotal;
//                        countA += totalObj._avgcount;
                    if (0< totalObj._count) {
                        totalA += totalObj._total;
                        countA += totalObj._count;
                    }
                }
                final AttendInfo attend = student._attendInfo;
                if (null != attend) {
                    totalMlesson += attend._mLesson;
                    totalPresent += attend._present;
                    totalAbsent += attend._absent;
                    if ("2".equals(_param._lessonDaysPrintFlg) && attend._mLesson > 0) {
                        maxLesson = Math.max(maxLesson, attend._mLesson);
                        minLesson = Math.min(minLesson, attend._mLesson);
                    } else {
                        maxLesson = Math.max(maxLesson, attend._lesson);
                        minLesson = Math.min(minLesson, attend._lesson);
                    }
                }
                maxCredit = Math.max(maxCredit, student._compCredit);
                minCredit = Math.min(minCredit, student._compCredit);
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }                
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
            if (0 < totalMlesson) {
                _perHrPresent = new BigDecimal((float) totalPresent / (float) totalMlesson * 100);
                _perHrAbsent = new BigDecimal((float) totalAbsent / (float) totalMlesson * 100);
            }
            if (0 < maxCredit) {
                _HrCompCredits = maxCredit + "単位";
            }
            if (0 < maxLesson) {
                _HrMLesson = maxLesson + "日";
            }
        }

        /**
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(DB2UDB db2, String hrclassCd, String courseCd) {
            PreparedStatement ps = null;
            ResultSet rs= null;
            final String sql = sqlSubclassGradeAvg(hrclassCd, courseCd);
            //log.debug(" gradeAverage sql = " + sql);
            try {
                ps = db2.prepareStatement(sql);
                
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    String subclassCd = rs.getString("SUBCLASSCD");
                    String electDiv = rs.getString("ELECTDIV");
                    
                    SubClass subclass = (SubClass) _subclasses.get(electDiv + subclassCd);
                    BigDecimal subclassGradeAvg = rs.getBigDecimal("AVG");
                    if (subclass == null || subclassGradeAvg == null) {
                        //log.debug("subclass => " + subclass + " , gradeAvg => " + subclassGradeAvg);
                        continue;
                    }
                    //log.debug("subclass => " + subclass._subclassabbv + " , gradeAvg => " + subclassGradeAvg);
                    subclass._scoresubaverage = subclassGradeAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            } catch (SQLException e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            final String sql1 = sqlHrClassAvg(hrclassCd, courseCd);
            try {
                ps = db2.prepareStatement(sql1);
                
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    String subclassCd = rs.getString("SUBCLASSCD");
                    String electDiv = rs.getString("ELECTDIV");
                    
                    SubClass subclass = (SubClass) _subclasses.get(electDiv + subclassCd);
                    BigDecimal subclassGradeAvg = rs.getBigDecimal("AVG");
                    if (subclass == null || subclassGradeAvg == null) {
                        continue;
                    }
                    subclass._scoresubtotal = rs.getString("SCORE");
                    subclass._scoresubCount = rs.getString("COUNT");
                    subclass._scoresubMax =  rs.getString("HIGHSCORE");
                    subclass._scoresubMin = rs.getString("LOWSCORE");
                }
            }catch (SQLException e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /** 科目の学年平均をもとめるSQL */
        private String sqlSubclassGradeAvg(String hrclassCd, String courseCd) {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    value(T2.ELECTDIV,'0') as ELECTDIV, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.SCORE, ");
            stb.append("    T1.COUNT, ");
            stb.append("    T1.HIGHSCORE, ");
            stb.append("    T1.LOWSCORE, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    " + _param._avgTable + " T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
            stb.append("        T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "'");
            if (null == courseCd) {
                if (TOTAL_RANK_GRADE.equals(_param._totalRank)) {
                    stb.append("    AND T1.AVG_DIV = '1' ");
                    stb.append("    AND T1.HR_CLASS = '000' ");
                    stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
                } else {
                    stb.append("    AND T1.AVG_DIV = '2' ");
                    stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + hrclassCd + "' ");
                    stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
                }
            } else {
                stb.append("    AND T1.AVG_DIV = '3' ");
                stb.append("    AND T1.HR_CLASS = '000' ");
                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + courseCd + "' ");
            }
            stb.append("    AND T1.GRADE = '" + _param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '999999' ");
            return stb.toString();
        }
        
        /** 科目の学年平均をもとめるSQL */
        private String sqlHrClassAvg(String hrclassCd, String coursecd) {
            StringBuffer stb = new StringBuffer();
            if (!StringUtils.isBlank(coursecd)) {
                stb.append("SELECT ");
                stb.append("    value(T2.ELECTDIV,'0') as ELECTDIV, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
                if ("9900".equals(_param._testKindCd)) {
                    stb.append("    SUM(T1.VALUE) AS SCORE, ");
                    stb.append("    COUNT(T1.VALUE) AS COUNT, ");
                    stb.append("    MAX(T1.VALUE) AS HIGHSCORE, ");
                    stb.append("    MIN(T1.VALUE) AS LOWSCORE, ");
                    stb.append("    AVG(1.0 * T1.VALUE) AS AVG ");
                } else {
                    stb.append("    SUM(T1.SCORE) AS SCORE, ");
                    stb.append("    COUNT(T1.SCORE) AS COUNT, ");
                    stb.append("    MAX(T1.SCORE) AS HIGHSCORE, ");
                    stb.append("    MIN(T1.SCORE) AS LOWSCORE, ");
                    stb.append("    AVG(1.0 * T1.SCORE) AS AVG ");
                }
                stb.append("FROM ");
                stb.append("    RECORD_SCORE_DAT T1 ");
                stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T2.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T1.SUBCLASSCD ");
                stb.append("    INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("        AND T3.YEAR = T1.YEAR ");
                stb.append("        AND T3.SEMESTER = '" + _param.getRegdSemester() + "' ");
                stb.append("        AND T3.GRADE || T3.HR_CLASS = '" + hrclassCd + "' ");
                stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '" + coursecd + "' ");
                stb.append("WHERE ");
                stb.append("    T1.YEAR = '" + _param._year + "'");
                stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "'");
                stb.append("GROUP BY ");
                stb.append("    value(T2.ELECTDIV,'0'), ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("    T1.SUBCLASSCD ");

            } else {
                stb.append("SELECT ");
                stb.append("    value(T2.ELECTDIV,'0') as ELECTDIV, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("    T1.SCORE, ");
                stb.append("    T1.COUNT, ");
                stb.append("    T1.HIGHSCORE, ");
                stb.append("    T1.LOWSCORE, ");
                stb.append("    T1.AVG ");
                stb.append("FROM ");
                stb.append("    " + _param._avgTable + " T1 ");
                stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T2.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T1.SUBCLASSCD ");
                stb.append("WHERE ");
                stb.append("    T1.YEAR = '" + _param._year + "'");
                stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
                stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "'");
                stb.append("    AND T1.AVG_DIV = '2' ");
                stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + hrclassCd + "' ");
                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
                stb.append("    AND T1.GRADE = '" + _param._grade + "' ");
                stb.append("    AND T1.SUBCLASSCD <> '999999' ");
            }

            return stb.toString();
        }

        private void setAbsenceHigh(
                final DB2UDB db2
        ) throws SQLException {
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean isSpecial = true;
            String absenceSql = "";
            if (_param._knjSchoolMst.isHoutei()) {
                absenceSql = getHouteiJisuSql(null, _param, isSpecial);
            } else {
                absenceSql = getJituJisuSql(null, _param, isSpecial);
            }
            try {
                ps = db2.prepareStatement(absenceSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._spAbsenceHighMap.put(rs.getString("SPECIAL_GROUP_CD"), (BigDecimal) rs.getObject("ABSENCE_HIGH"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            isSpecial = false;
            if (_param._knjSchoolMst.isHoutei()) {
                absenceSql = getHouteiJisuSql(null, _param, isSpecial);
            } else {
                absenceSql = getJituJisuSql(null, _param, isSpecial);
            }
            try {
                ps = db2.prepareStatement(absenceSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final ScoreDetail detail = student.getScoreDetail(rs.getString("SUBCLASSCD"));
                    if (null == detail) {
                        continue;
                    }
                    detail._compAbsenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                    detail._getAbsenceHigh = rs.getBigDecimal("GET_ABSENCE_HIGH");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String getHouteiJisuSql(final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (isGroup) {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            } else {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            }
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            if (_param._useAbsenceWarn) {
                if (_param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_RISHU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (_param._useAbsenceWarn) {
                if (_param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_SHUTOKU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.GRADE = T1.GRADE AND ");
            stb.append("       T2.COURSECD = T1.COURSECD AND ");
            stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
            stb.append("       T2.YEAR = T1.YEAR AND ");
            stb.append("       T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (isGroup) {
                // stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
            } else {
                if (null != subclassCd) {
                    stb.append("     AND T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T2.SCHREGNO IN ").append(getStudentsqlWhereIn());
            return stb.toString();
        }

        private String getJituJisuSql(final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
            final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (isGroup) {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            } else {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            }
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            if (_param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("        AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (_param._useAbsenceWarn) {
                if (param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(param._attendEndDateSemester) ? "" : param._attendEndDateSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("        AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
            if (isGroup) {
                stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
            } else {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                }
                stb.append("       T3.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.SUBCLASSCD ");
            }
            stb.append("       AND T3.COURSECD = T2.COURSECD ");
            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       AND T3.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.DIV = '2' ");
            if (isGroup) {
//                stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
            } else {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.append("        T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T1.SCHREGNO IN ").append(getStudentsqlWhereIn());
            return stb.toString();
        }
        
        private String getStudentsqlWhereIn() {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                stb.append(comma).append("'").append(student._schregno).append("'");
                comma = ",";
            }
            return "(" + stb.toString() + ")";
        }
        
        private void setHtestRemark(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHREGNO, REMARK ");
                stb.append(" FROM ");
                stb.append("     HTESTREMARK_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._year + "' ");
                stb.append("     AND SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append("     AND DIV = '1' ");
                stb.append("     AND SUBCLASSCD = '999999' ");
                stb.append("     AND SCHREGNO IN ").append(getStudentsqlWhereIn());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    student._remark = rs.getString("REMARK");
                }
                
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 今年度の資格認定単位数
         * @return
         */
        private String sqlStdQualifiedCredits() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SCHREGNO, SUM(T1.CREDITS) AS CREDITS");
            stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO IN ").append(getStudentsqlWhereIn());
            stb.append("    AND T1.YEAR < '" + _param._year + "'");
            stb.append(" GROUP BY SCHREGNO ");

            return stb.toString();
        }
        
        /**
         * 順位の算出
         */
        private List createRanking() {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(student, _knjdObj, _param);
                final Total total = student._total;
                if (0 < total._count) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }
        

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        SubClass getSubClass(
                final ResultSet rs,
                Map subclasses
        ) {
            String subclasscode = null;
            int credit = 0;
            try {
                subclasscode = rs.getString("SUBCLASSCD");
                if (rs.getString("CREDITS") != null) { credit = rs.getInt("CREDITS"); }
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            //科目クラスのインスタンスを更新して返す
            if (subclasses.containsKey(subclasscode)) {
                SubClass subclass = (SubClass) subclasses.get(subclasscode);
                if (subclass._mincredit == 0) {
                    subclass._mincredit = credit;
                }
                if (credit > 0) {
                    subclass._maxcredit = Math.max(subclass._maxcredit, credit);
                    subclass._mincredit = Math.min(subclass._mincredit, credit);
                }
                return subclass;
            }
            //科目クラスのインスタンスを作成して返す
            String classabbv = null;
            String subclassabbv = null;
            boolean electdiv = false;
            String subclassstaffname = null;
            try {
                classabbv = rs.getString("CLASSNAME");
                subclassabbv = rs.getString("SUBCLASSNAME");
                electdiv = "1".equals(rs.getString("ELECTDIV"));
                subclassstaffname = rs.getString("SUBCLASS_STAFFNAME");
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            final SubClass subClass = new SubClass(subclasscode, classabbv, subclassabbv, electdiv, credit, subclassstaffname);
            subclasses.put(subclasscode, subClass);
            return subClass;
        }

        ScoreDetail createScoreDetail(
                final ResultSet rs,
                Map subclasses
        ) throws SQLException {
            
            final String subclassCd = rs.getString("SUBCLASSCD");
            final ScoreValue score;
            final ScoreValue patternAssess;
            
            /**
             * 生徒別科目別の素点または評定のインスタンスを作成します。
             * @param strScore 素点または評定
             * @param subclassCd 教科コード
             * ScoreValueは'総合的な学習の時間'は'null'。
             */
            if (KNJDefineSchool.subject_T.equals(subclassCd.substring(1, 3))) {
                score = null;
                patternAssess = null;
            } else {
                score = rs.getString("SCORE") == null ? null : new ScoreValue(rs.getString("SCORE"));
                patternAssess = rs.getString("PATTERN_ASSESS") == null ? null : new ScoreValue(rs.getString("PATTERN_ASSESS"));
            }

            final String slump;
            if (_param.useKetten()) {
                slump = null != patternAssess && patternAssess.hasIntValue() && patternAssess.getScoreAsInt() <= _param._ketten ? "1" : null;
            } else {
                slump = rs.getString("SLUMP");
            }
            final ScoreDetail detail = new ScoreDetail(
                    getSubClass(rs, subclasses),
                    score,
                    patternAssess,
                    (Integer) rs.getObject("REPLACEMOTO"),
                    (String) rs.getObject("PRINT_FLG"),
                    (String) rs.getObject("SCORE_FLG"),
                    (Integer) rs.getObject("COMP_CREDIT"),
                    (Integer) rs.getObject("GET_CREDIT"),
                    (Integer) rs.getObject("CREDITS"),
                    slump
            );
            return detail;
        }

        int rank(final Student student) {
            final Total total = student._total;
            if (0 >= total._count) {
                return -1;
            }
            return 1 + _ranking.indexOf(total);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _hrclassCd.compareTo(that._hrclassCd);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private class Student implements Comparable {
        private final String _schregno;  // 学籍番号
        private final String _courseCd;
        private final String _majorCd;
        private final String _grade;
        private final String _courseCode;
        private final HRInfo _hrInfo;
        private final int _gnum;
        private String _attendNo;
        private String _name;
        private String _sex;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo;
        private String _scoreSum;
        private String _scoreAvg;
        private int _classRank;
        private int _gradeRank;
        private int _courseRank;
        private final Map _scoreDetails = new HashMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private final Map _spGroupLessonMinutes = new HashMap(); // 特活グループコードごとの授業時分
        private final Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠課時分
        private final Map _spAbsenceHighMap = new HashMap(); // 特活グループコードの履修上限値
        private String _remark;
        
        Student(final String code,
                final HRInfo hrInfo,
                final String courseCd,
                final String majorCd,
                final String grade,
                final String courseCode,
                final int gnum) {
            _schregno = code;
            _hrInfo = hrInfo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _grade = grade;
            _courseCode = courseCode;
            _gnum = gnum;
        }

        void setInfo(
                final String attendNo,
                final String name,
                final String sex,
                final TransInfo tansInfo
        ) {
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _transInfo = tansInfo;
        }

        void add(final ScoreDetail scoreDetail) { _scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail); }

        ScoreDetail getScoreDetail(final String subclassCd) {
            if (subclassCd == null) {
                return null;
            }
            for (final Iterator it = _scoreDetails.keySet().iterator(); it.hasNext();) {
                final String keySubClassCd = (String) it.next();
                if (keySubClassCd.substring(1).equals(subclassCd)) {
                    return (ScoreDetail) _scoreDetails.get(keySubClassCd);
                }
            }
            return null;
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Student)) return -1;
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 == rtn) {
                rtn = _attendNo.compareTo(that._attendNo);
            }
            return rtn;
        }

        int rank() {
            return _hrInfo.rank(this);
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

        /**
         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
         */
        public boolean isGradeGood() {
            final BigDecimal avg = _total._avgBigDecimal;
            if (null == avg) { return false; }
            Float ac = _param._assess;
            if (ac.floatValue() <= avg.doubleValue()) { return true; }
            return false;
        }

        /**
         * @return 成績不振者（評定１が1つでもある）は true を戻します。
         */
        public boolean isGradePoor() {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final ScoreValue val = detail._patternAssess;
                if (null == val || !val.hasIntValue()) continue;
                if (val.getScoreAsInt() == 1){ 
                    return true;
                }
            }
            return false;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
         */
        public boolean isAttendPerfect() {
            if (! _attendInfo.isAttendPerfect()) { return false; }
            
            for (final Iterator it = _scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();
                final Double kekka = detail.getAbsent();
                if (null == kekka) continue;
                if (0 < kekka.doubleValue()){ return false; }
            }            
            return true;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        public boolean isKekkaOver() {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (true == detail.compIsOver(this)){ return true; }
            }            
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class AttendInfo {
        private final int _lesson;
        private final int _mLesson;
        private final int _suspend;
        private final int _mourning;
        private final int _absent;
        private final int _present;
        private final int _late;
        private final int _early;
        private final int _transDays;

        private AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        boolean isAttendPerfect() {
            if (_absent == 0 && _late == 0 && _early == 0) { return true; }
            return false;
        }
    }
    
    //--- 内部クラス -------------------------------------------------------
    private static class TransInfo {
        private final String _date;
        private final String _dateStr;
        private final String _name;

        TransInfo(
                final String date,
                final String dateStr,
                final String name
        ) {
            _date = date;
            _dateStr = dateStr;
            _name = name;
        }

        public String toString() {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _dateStr) {
                sb.append(_dateStr);
            }
            if (null != _name) {
                sb.append(_name);
            }
            return sb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass {
        private final String _classabbv;
        private final String _classcode;
        private final String _subclasscode;
        private final String _subclassabbv;
        private final boolean _electdiv; // 選択科目
        private String _spGroupCd;
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private int _jisu;  // 授業時数
        private String _subclassstaffname; // 科目担当者名
        private String _scoreaverage;  // 学級平均
        private String _scoresubaverage;  // 学年平均
        private String _scoretotal;  // 学級合計
        private String _scoresubtotal;  // 学級合計
        private String _scoreCount;  // 学級人数
        private String _scoresubCount;  // 学級人数
        private String _scoreMax;  // 最高点
        private String _scoresubMax;  // 最高点
        private String _scoreMin;  // 最低点
        private String _scoresubMin;  // 最低点
        private String _scoreFailCnt;  // 欠点者数

        SubClass(
                final String subclasscode,
                final String classabbv, 
                final String subclassabbv,
                final boolean electdiv,
                final int credit,
                final String subclassstaffname
        ) {
            _classabbv = classabbv;
            // (subclasscodeは頭1桁+科目コードなので教科コードは2文字目から2桁)
            _classcode = subclasscode.substring(1, 3); 
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _subclassstaffname = subclassstaffname;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscode + " , " +_subclassabbv + " , spGroupcd = " + _spGroupCd + " , electdiv = " +_electdiv + " , maxcredit = " +_maxcredit + " , mincredit = " +_mincredit + " , jisu = " +_jisu +"]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<素点・評定データのクラスです>>。
     */
    private static class ScoreValue {
        private final String _strScore;

        ScoreValue(final String strScore) {
            _strScore = strScore;
        }

        boolean hasIntValue() { return !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore); }
        int getScoreAsInt() { return Integer.parseInt(_strScore); }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private class ScoreDetail {
        private final SubClass _subClass;
        private Integer _absent0; // 欠時数
        private Double _absent1;  // 欠課数（欠時数 + 欠課換算ペナルティー）
        private final ScoreValue _score;
        private final ScoreValue _patternAssess;
        private final Integer _replacemoto;
        private final String _printFlg;
        private final String _scoreFlg;
        private final Integer _compCredit;
        private final Integer _getCredit;
        private final Integer _credits;
        private final boolean _isSlump;
        private BigDecimal _compAbsenceHigh = BigDecimal.valueOf(0);
        private BigDecimal _getAbsenceHigh = BigDecimal.valueOf(0);
        private Integer _late;     // 生徒ごとの遅刻時数
        private Integer _early;    // 生徒ごとの早退時数
        private Integer _koketsu;  // 生徒ごとの公欠時数
        private Integer _mourning; // 生徒ごとの忌引時数
        private Integer _suspend;  // 生徒ごとの出停時数
        private Integer _virus;  // 生徒ごとの出停時数（伝染病）
        private Integer _koudome;  // 生徒ごとの出停時数（交止）
        private Integer _lateEarly1;  // 生徒ごとの遅刻早退時数

        ScoreDetail(
                final SubClass subClass,
                final ScoreValue score,
                final ScoreValue patternAssess,
                final Integer replacemoto,
                final String print_flg,
                final String score_flg,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits,
                final String slump
        ) {
            _subClass = subClass;
            _score = score;
            _patternAssess = patternAssess;
            _replacemoto = replacemoto;
            _printFlg = print_flg;
            _scoreFlg = score_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
            _isSlump = null != slump;
        }

        public Integer getSuspendAll() {
            if (null == _suspend && null == _virus && null == _koudome) {
                return null;
            }
            final int s1 = null == _suspend ? 0 : _suspend.intValue();
            final int s2 = null == _virus ? 0 : _virus.intValue();
            final int s3 = null == _koudome ? 0 : _koudome.intValue();
            return new Integer(s1 + s2  + s3);
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final SubClass subclass, final Double absent, final BigDecimal absenceHigh, final Student student) {
            if (null == subclass || null == absent) {
                return false;
            }
            if (null == subclass._spGroupCd) {
                if (null == absenceHigh || absenceHigh.doubleValue() < absent.doubleValue()) {
                    return true;
                }
            } else {
                // 特活グループ
                final Map subclassMinutesMap = (Map) student._spGroupAbsentMinutes.get(subclass._spGroupCd);
                final String subclasscd = null != subclass._subclasscode && 1 <= subclass._subclasscode.length() ? subclass._subclasscode.substring(1) : null;
                if (null != subclassMinutesMap && null != subclassMinutesMap.get(subclasscd)) {
                    final Integer minutes = (Integer) subclassMinutesMap.get(subclasscd);
                    if (null != minutes) {
                        final BigDecimal spAbsent = getSpecialAttendExe(minutes.intValue(), _param);
//                        log.debug(" schregno = " + student._schregno + ", subclass = " + subclass + " , spAbsent = " + spAbsent + " (absent = " + absent + "), spAbsenceHighMap = " + student._spAbsenceHighMap);
                        final BigDecimal spAbsenceHigh = (BigDecimal) student._spAbsenceHighMap.get(subclass._spGroupCd);
                        if (null == spAbsenceHigh || spAbsenceHigh.doubleValue() < spAbsent.doubleValue()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        Integer getCompCredit() {
            return enableCredit(_compCredit);
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        Integer getGetCredit() {
            return enableCredit(_getCredit);
        }

        /**
         * @return 合併元科目はnullを、以外はcを戻します。
         */
        Integer enableCredit(Integer c) {
            if (null != _replacemoto && _replacemoto.intValue() >= 1) {
                return null;
            }
            return c;
        }

        /**
         * 単位を印刷します。
         * @param svf
         * @param gnum 印刷する行番号
         * @param column 印刷する列番号
         */
        private void printTani(
            final Vrw32alp svf, 
            final int gnum, 
            final int column,
            final TestKind knjdObj
        ) {
            Integer credit;
            if (knjdObj.isGakunenmatu()) {
                credit = _getCredit;
            } else {
                credit = _credits;
            }
            if (null != credit) {
                if (null != _replacemoto && 1 <= _replacemoto.intValue()) {
                    svf.VrsOut("cre"+ gnum,  "(" + credit.toString() + ")");
                } else {
                    svf.VrAttribute("cre"+ gnum,  "Hensyu=3");
                    svf.VrsOut("cre"+ gnum,  credit.toString());
                }
            }
        }
        
        // 履修上限値オーバー
        public boolean compIsOver(final Student student) {
            return judgeOver(_subClass, _absent1, _compAbsenceHigh, student);
        }

        // 修得上限値オーバー
        public boolean getIsOver(final Student student) {
            return judgeOver(_subClass, _absent1, _getAbsenceHigh, student);
        }

        /**
         * @return absent を戻します。
         */
        Double getAbsent() {
            return _absent1;
        }
        
        public String toString() {
            return (_subClass + " , " + _absent1 + " , " + _score + " , " + _patternAssess + " , " + _replacemoto + " , " 
                    + _printFlg + " , " + _scoreFlg + " , " + _compCredit + " , " + _getCredit + " , " + _compAbsenceHigh + " , "
                    + _credits);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private static class Total implements Comparable {
        final int _total;  // 総合点
        final int _count;  // 件数（成績）
        final BigDecimal _avgBigDecimal;  // 平均点
        final int _countFail;  //欠点科目数
        final int _countFailCredit;  //欠点単位数
        final int _specialLesson; // 特活授業時数
        final int _specialAbsent; // 特活欠課時数

        /**
         * コンストラクタ。
         * @param student
         */
        Total(final Student student, final TestKind knjdObj, final Param param) {

            /**
             * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
             */
            int total = 0;
            int count = 0;
            
            int compCredit = 0;
            int getCredit = 0;
            
            int countFail = 0;
            int countFailCredit = 0;
            
            for (final Iterator it = student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();
            
                final ScoreValue scoreValue = knjdObj.getTargetValue(detail);
                if (isAddTotal(scoreValue, detail._replacemoto, knjdObj)) {
                    total += scoreValue.getScoreAsInt();
                    count++;                    
                    // if (scoreValue.getScoreAsInt() <= param.getFailValue()) countFail++;
                }
                if (detail._isSlump) countFail++;
                
            
                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }
            
                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                    // if (g.intValue() == 0) countFailCredit++;
                }
                if (detail._isSlump && detail._credits != null) countFailCredit += detail._credits.intValue();
            }
            
            BigDecimal specialLesson = new BigDecimal(0);
            for (final Iterator it = student._spGroupLessonMinutes.values().iterator(); it.hasNext();) {
                final Map subclassMinutes = (Map) it.next();
                int groupTotalLessonMinutes = 0;
                for (final Iterator its = subclassMinutes.keySet().iterator(); its.hasNext();) {
                    final String subclasscd = (String) its.next();
                    final Integer lessonMinutes = (Integer) subclassMinutes.get(subclasscd);
                    groupTotalLessonMinutes += lessonMinutes.intValue();
                }
                specialLesson = specialLesson.add(getSpecialAttendExe(groupTotalLessonMinutes, param));
            }
            
            BigDecimal specialAbsent = new BigDecimal(0);
            for (final Iterator it = student._spGroupAbsentMinutes.values().iterator(); it.hasNext();) {
                final Map subclassMinutes = (Map) it.next();
                int groupTotalAbsentMinutes = 0;
                for (final Iterator its = subclassMinutes.keySet().iterator(); its.hasNext();) {
                    final String subclasscd = (String) its.next();
                    final Integer absentMinutes = (Integer) subclassMinutes.get(subclasscd);
                    groupTotalAbsentMinutes += absentMinutes.intValue();
                }
                specialAbsent = specialAbsent.add(getSpecialAttendExe(groupTotalAbsentMinutes, param));
            }
            
            _total = total;
            _count = count;
            if (0 < count) {
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            } else {
                _avgBigDecimal = null;
            }
            if (0 < compCredit) { student._compCredit = compCredit; }
            if (0 < getCredit) { student._getCredit = getCredit; }
            _specialLesson = specialLesson.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            _specialAbsent = specialAbsent.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            _countFail = countFail;
            _countFailCredit = countFailCredit;
        }

        /**
         * @param scoreValue
         * @param replacemoto
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final ScoreValue scoreValue,
                final Integer replacemoto,
                final TestKind knjdObj
        ) {
            if (null == scoreValue || !scoreValue.hasIntValue()) { return false; }
            if (knjdObj.isGakunenmatu() && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }
        
        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Total)) return -1;
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Total)) return false;
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

    private static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;
        /** 日付 */
        final String _ctrlDate;

        /** 学年 */
        final String _grade;

        /** 出欠集計日付 */
        final boolean _isRuikei;
        final String _dateS;
        final String _date;

        final String _testKindCd;

        final String _valueFlg;

        final boolean _useKetten;
        final int _ketten;

        private String _rankTable;
        private String _avgTable;

        /** 総合順位出力 1:学級 2:学年 3:コース */
        final String _totalRank;

        /** 基準点としてRECORD_RANK_DATで*_AVG_RANKを使用するか */
        final boolean _useAverageAsKijunten;

        /** 起動元のプログラムＩＤ */
        final String _prgId;

        /** 成績優良者評定平均の基準値===>KNJD652：未使用 */
        final Float _assess;

        /** フォーム（1:４５名、2:５０名）*/
        final String _formSelect;
        
        /** 科目数 (1:15科目、2:20科目)*/
        final String _subclassMax;
        
        /** 備考欄有り */
        final boolean _hasRemark;
        
        /** 単位保留 */
        final boolean _creditDrop;

        /** 欠番を詰める */
        final boolean _noKetuban;

        /** 同一クラスでのコース毎に改頁あり */
        final boolean _outputCourse;
        
        /** "総合的な学習の時間"を表示しない */
        final boolean _notOutputSougou;
        
        final String _testItemName;
        
        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;
        
        private String _z010Name1;
        private String _z010Name2;
        
        private String _semesterName;
        
        private KNJSchoolMst _knjSchoolMst;
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        private static final String FROM_TO_MARK = "\uFF5E";

        /** 端数計算共通メソッド引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";
        
        /** 注意 or 超過 */
        private final boolean _useAbsenceWarn;

        /** 授業日数/出席すべき日数のどちらを表示するか */
        private final String _lessonDaysPrintFlg;
        
        /** C005：欠課換算法修正 */
        private final Map _subClassC005 = new HashMap();
        
        /** 特別活動欠課数を表示しない */
        private final boolean _isNotPrintSpecialSubclassKekka;
        
        private String _attendEndDateSemester;
        
        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;
        
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;

        private final String _useVirus;
        private final String _useKoudome;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _isRuikei = "1".equals(request.getParameter("DATE_DIV"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _dateS = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            final String testKindCdTemp = request.getParameter("TESTKINDCD");
            _testKindCd = "0".equals(testKindCdTemp) ? "0000" : "9909".equals(testKindCdTemp) ? "9900" : testKindCdTemp;  //テスト・成績種別
            _valueFlg = "9909".equals(testKindCdTemp) ? "on" : "off";  //record_score_dat.valueを出力するか？
            _totalRank = request.getParameter("OUTPUT_RANK");
            
            boolean haskettenParam = false;
            for (final Enumeration enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String paramname = (String) enums.nextElement();
                if ("KETTEN".equals(paramname)) {
                    haskettenParam = true;
                    break;
                }
            }
            _useKetten = haskettenParam;
            _ketten = request.getParameter("KETTEN") == null ? -1 : Integer.parseInt(request.getParameter("KETTEN"));
            
            _creditDrop = (request.getParameter("OUTPUT4") != null);
            _noKetuban = ( request.getParameter("OUTPUT5") != null );
            _outputCourse = "3".equals(_totalRank);
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT_SOUGOU"));
            _assess = (request.getParameter("ASSESS") != null) ? new Float(request.getParameter("ASSESS1")) : new Float(4.3);

            _prgId = request.getParameter("PRGID");
            _formSelect = request.getParameter("FORM_SELECT");
            _subclassMax = request.getParameter("SUBCLASS_MAX");
            _hasRemark = "1".equals(request.getParameter("REMARK"));
            _useAverageAsKijunten = "2".equals(request.getParameter("OUTPUT_KIJUN"));
           
            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
           
            _lessonDaysPrintFlg = request.getParameter("OUTPUT_LESSON");
            
            _isNotPrintSpecialSubclassKekka = "1".equals(request.getParameter("OUTPUT_TOKUBETSU"));

           _rankTable = "RECORD_RANK_DAT";
           _avgTable  = "RECORD_AVERAGE_DAT";
           // log.debug("序列テーブル：" + _rankTable + ", " + _avgTable);
           
           load(db2);
           
           // テスト名称
           _testItemName = getTestItemName(db2, _year, _semester, _testKindCd);
           
           _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
           _useCurriculumcd  =request.getParameter("useCurriculumcd");
           _useVirus  =request.getParameter("useVirus");
           _useKoudome  =request.getParameter("useKoudome");
        }
        
        public boolean useKetten() {
            return _useKetten && ("9900".equals(_testKindCd) || "9901".equals(_testKindCd));
        }

        public String getRegdSemester() {
            return !"9".equals(_semester) ?  _semester : _semeFlg;
        }

        public int getFormMaxLine() {
            return "1".equals(_formSelect) ? 45 : 50;
        }

        public int getFormMaxColumn() {
            return "1".equals(_subclassMax) ? 15 : 20;
        }

//        /**
//         * 欠点
//         * 100段階：29以下
//         *   5段階： 1以下
//         */
//        public int getFailValue() {
//            return (null == _ketten || "".equals(_ketten)) ? 0 : Integer.parseInt(_ketten);
//        }

        /**
         * 欠課換算法修正
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC005(final DB2UDB db2) {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String is = rs.getString("NAMESPARE1");
                    log.debug("(名称マスタ C005):科目コード=" + subclassCd);
                    _subClassC005.put(subclassCd, is);
                }
            } catch (Exception e) {
                log.error(e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }
        
        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC042(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                _absenceWarnIsUnitCount = "1".equals(rs.getString("NAMESPARE1"));
                log.debug("(名称マスタ C042) =" + _absenceWarnIsUnitCount);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        public String getFormFile() {
            String form = null;
            if (isPatternC()) { // 15科目
                form = ("1".equals(_formSelect)) ? "KNJD652_5.frm" : "KNJD652_6.frm"; // Cパターン
            } else {
                if (_hasRemark) {
                    form = ("1".equals(_formSelect)) ? "KNJD652_3.frm" : "KNJD652_4.frm"; // Bパターン
                } else {
                    form = ("1".equals(_formSelect)) ? "KNJD652_1.frm" : "KNJD652_2.frm"; // Aパターン
                }
            }
            return form;
        }
        
        public boolean isPatternC() {
            return "1".equals(_subclassMax);
        }
        
        /** 年度 */
        public String getNendo(final DB2UDB db2) {
            return KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }
        
        /** 欠課の集計範囲 */
        public String getTermKekka(final DB2UDB db2) {
            return KNJ_EditDate.h_format_JP(db2, _dateS) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(db2, _date);
        }
        
        /** 「出欠の記録」の集計範囲 */
        public String getTermAttend(final DB2UDB db2) {
            return KNJ_EditDate.h_format_JP(db2, _dateS) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(db2, _date);
            
        }

        public boolean isHousei() {
            return _z010Name1.equals("HOUSEI");
        }

        public boolean isChiben() {
            return "CHIBEN".equals(_z010Name1);
        }

        public boolean isChiben9909() {
            return isChiben() && "9909".equals(_testKindCd);
        }
        
        /** 総合順位出力の順位欄項目名 */
        private String getRankName() {
            if (TOTAL_RANK_CLASS.equals(_totalRank)) {
                return "クラス";
            } else if (TOTAL_RANK_GRADE.equals(_totalRank)) {
                return "学年";
            } else {
                return "コース";
            }
        }

        public void load(DB2UDB db2) {
            try {
                _definecode = new KNJDefineSchool();
                _definecode.defineCode(db2, _year);         //各学校における定数等設定
                log.debug("semesdiv=" + _definecode.semesdiv + "   absent_cov=" + _definecode.absent_cov + "   absent_cov_late=" + _definecode.absent_cov_late);
                
                loadNameMstZ010(db2);
                loadSemester(db2, _year, _semester);
                loadAttendSemesArgument(db2);
                loadNameMstC005(db2);
                loadNameMstC042(db2);
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                loadAttendEdateSemester(db2);
            } catch (SQLException e) {
                log.error(e);
            }
        }
        
        private String loadAttendEdateSemester(DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer(); 
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM SEMESTER_MST T1 ");
            stb.append(" LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND (('" + _date + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR ('" + _date + "' BETWEEN T1.EDATE AND VALUE(T2.SDATE, '9999-12-30'))) ");

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                _attendEndDateSemester = rs.getString("SEMESTER");
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            return _attendEndDateSemester;
        }

        private void loadAttendSemesArgument(DB2UDB db2) {
            
            try {
                // 出欠の情報
                _periodInState = AttendAccumulate.getPeiodValue(db2, null, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, null, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _dateS, _date);
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.error("loadAttendSemesArgument exception", e);
            }
        }
        
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year, String semes) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                StringBuffer stb = new StringBuffer();
                stb.append("select SEMESTER, SEMESTERNAME, SDATE");
                stb.append(" from SEMESTER_MST");
                stb.append(" where YEAR='" + year + "'");
                stb.append(" order by SEMESTER");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);
                    if (semes != null && semes.equals(semester)) {
                        _semesterName = name;
                    }

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
            
        private void loadNameMstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                    _z010Name2 = rs.getString("NAME2");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String getTestItemName(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String testItemName = null;
            try {
                final String sql = "SELECT TESTITEMNAME "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW "
                                 +  "WHERE YEAR = '" + year + "' "
                                 +    "AND SEMESTER = '" + semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD = '" + testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    testItemName = rs.getString("TESTITEMNAME"); 
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testItemName;
        }
        
        /** 欠課時数フィールド名は、欠課時数算定コードにより異なる。*/
        String getSvfFieldNameKekka() {
            String svfFieldNameKekka;
            int absentConv = 0;
            try {
                absentConv = Integer.parseInt(_knjSchoolMst._absentCov);
            } catch (Exception e) {
                log.error("Exception:", e);
            }
            if (absentConv == 3 || absentConv == 4) {
                svfFieldNameKekka = "kekka";
            } else {
                svfFieldNameKekka = "kekka";
            }
            return svfFieldNameKekka;
        }
        
        DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            int absentConv = 0;
            try {
                absentConv = Integer.parseInt(_knjSchoolMst._absentCov);
            } catch (Exception e) {
                log.error("Exception:", e);
            }
            switch (absentConv) {
            case 0:
            case 1:
            case 2:
            case 5:
                absentFmt = new DecimalFormat("0");
                break;
            default:
                absentFmt = new DecimalFormat("0.0");
            }
            return absentFmt;
        }
    }
}
