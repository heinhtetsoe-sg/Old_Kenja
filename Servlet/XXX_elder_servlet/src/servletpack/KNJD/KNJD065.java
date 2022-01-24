// kanji=漢字
/*
 * $Id: c6c060424a715328d0ad7482ba79997574f93d9e $
 *
 * 作成日: 2006/12/25 10:27:18 - JST
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.Vrw32alpWrap;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author yamasiro
 * @version $Id: c6c060424a715328d0ad7482ba79997574f93d9e $
 */
public class KNJD065 {
    private static final Log log = LogFactory.getLog(KNJD065.class);

    /** 0 : パラメータ学年/学期成績 */
    private static final String PARAM_GRAD_SEM_KIND = "0";
    /** 9900 : 学年/学期成績 */
    private static final String GRAD_SEM_KIND = "9900";

    private static final String SEMEALL = "9";

    private static final int MAX_COLUMN = 19;
    private static final int MAX_LINE = 50;
    private static final String FROM_TO_MARK = "\uFF5E";

    /** 九段用フォームを使うか否かを判定するキーワード */
    public static final String CHIYODA = "chiyoda";

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");

    private Vrw32alpWrap _svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private String FORM_FILE;
    private KnjCommon _knjCommon;  //成績別処理のクラス

    private List _gradeAvg;

    //  TODO: db2.commit() を意識せよ。by takaesu.
    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        boolean hasData = false;
        Param param = null;
        try {
            // TODO: param はフィールド化しても良いのでは?
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            param = createParam(request, db2);

            try {
                _knjCommon = createKnjCommon(db2, param);  //成績別処理クラスを設定するメソッド
            } catch (final Exception e) {
                log.error("Exception", e);
            }
            log.debug("FORM-FILE:" + FORM_FILE);
            log.fatal(_knjCommon.getClass().getName() + " : semesterName=" + param._semesterName + ", testName=" + _knjCommon._testName);

            _svf.setKurikaeshiNum(MAX_COLUMN);
            _svf.setFieldNum(MAX_LINE);
            sd.setSvfInit(request, response, _svf);

            final String[] hrclass = request.getParameterValues("CLASS_SELECTED");  // 印刷対象HR組

            _gradeAvg = createGradeAvg(db2, param);

            for (int i = 0; i < hrclass.length; i++) {
                final HRInfo hrInfo = new HRInfo(hrclass[i]);
                hrInfo.load(db2, param);

                if (param._seisekifusin) { hrInfo.removeStudent(param); }

                if (printMain(param, hrInfo)) {
                    hasData = true;
                }
            }
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != param) {
                param.closeQuietly();
            }
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != _svf) {
                sd.closeSvf(_svf, hasData);
            }
        }
    }

    private List createGradeAvg(final DB2UDB db2, final Param param) {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlGradeAvg(param, _knjCommon));
            rs = ps.executeQuery();
            while (rs.next()) {
                final BigDecimal bigDecimal = rs.getBigDecimal("AVG");
                if (null != bigDecimal) {
                    rtn.add(bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP));
                }
            }
        } catch (final Exception e) {
            log.error("学年全体の平均値取得に失敗", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        Collections.sort(rtn, new ReverseComparator());
        log.debug("学年全員の平均値一覧=" + rtn);
        return rtn;
    }

    private String sqlGradeAvg(final Param param, final KnjCommon knjCommon) {
        String sql;
        sql = " SELECT"
                + "     t1.schregno,"
                + "     t2.hr_class,"
                + "     AVG(DOUBLE(t1." + knjCommon._fieldname + ")) AS avg"
                + " FROM"
                + "     record_dat t1,"
                + "     schreg_regd_dat t2"
                + " WHERE"
                + "     t1.year = t2.year AND"
                + "     t1.schregno = t2.schregno AND"
                + "     t1.year = '" + param._year + "' AND"
                + "     t2.semester = '" + param._semester + "' AND"
                + "     t2.grade = '" + param._grade + "' AND";
        if ("1".equals(param._useCurriculumcd)) {
            sql +=    "     t1.classcd <> '" + KNJDefineCode.subject_T + "' AND";
        } else {
            sql +=    "     t1.subclasscd NOT LIKE '" + KNJDefineCode.subject_T + "%' AND";
        }
        sql +=    "     t1." + knjCommon._fieldname + " IS NOT NULL"
                + " GROUP BY"
                + "     t1.schregno,"
                + "     t2.hr_class"
                ;
        return sql;
    }

    /**
     *  get parameter doGet()パラメータ受け取り
     *       2005/01/05 最終更新日付を追加(param[15])
     *       2005/05/22 学年・組を配列で受け取る
     */
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    /**
     *  成績別処理クラス設定
     */
    private KnjCommon createKnjCommon(
            final DB2UDB db2,
            final Param param
    ) throws Exception {
        final KnjCommon common;
        if ("KNJD065".equals(param._prgId)) {
            FORM_FILE = "KNJD065.frm";

            // 成績会議判定資料　学期
            if (!param._semester.equals(SEMEALL)) {
                common = new KnjGakki(db2, param, false, true, true, false, true, false);
                common._fieldname = "SEM" + param._semester + "_VALUE";
                common._fieldname2 = null;
                common._testName = "(評定)";
                common._item1Name = ("2".equals(param._item)) ? param._itemName : "単位";
                common._item2Name = "評価";
                common._item3Name = "欠課";
                common._item4Name = "評価合計";
                common._item5Name = "評価平均";
            } else {
                // 成績会議判定資料　学年
                common = new KnjGrade(db2, param, true, true, true, false, true, false);
                common._fieldname = "GRAD_VALUE";
                common._fieldname2 = null;
                common._testName = "(評定)";
                common._item1Name = ("2".equals(param._item)) ? param._itemName : "単位";
                common._item2Name = "評定";
                common._item3Name = "欠課";
                common._item4Name = "評定合計";
                common._item5Name = "評定平均";
            }
        } else {
            FORM_FILE = param._isChiyoda ? "KNJD062_4.frm" : "KNJD062.frm";

            if ("0101".equals(param._testKindCd)) {
                // 成績一覧表(KNJD062) 中間
                common = new KnjD062A_Inter(db2, param, false, false, false, false, false, true);
                common._fieldname  = "SEM" + param._semester + "_INTR_SCORE";
                common._fieldname2 = "SEM" + param._semester + "_INTR";
                common._testName = param._testName;
                common._item1Name = "素点";
                common._item2Name = "評価";
                common._item3Name = "欠課";
                common._item4Name = "総合点";
                common._item5Name = "平均点";
            } else if ("0201".equals(param._testKindCd) || "0202".equals(param._testKindCd)) {
                // 成績一覧表(KNJD062) 期末1 or 期末2
                common = new KnjD062A_Term(db2, param, false, false, false, false, false, true);
                if (param._testKindCd.equals("0201")) {
                    common._fieldname  = "SEM" + param._semester + "_TERM_SCORE";
                    common._fieldname2 = "SEM" + param._semester + "_TERM";
                } else {
                    common._fieldname  = "SEM" + param._semester + "_TERM2_SCORE";
                    common._fieldname2 = "SEM" + param._semester + "_TERM2";
                }
                common._testName = param._testName;
                common._item1Name = "素点";
                common._item2Name = "評価";
                common._item3Name = "欠課";
                common._item4Name = "総合点";
                common._item5Name = "平均点";
            } else if (!param._semester.equals(SEMEALL)) {
                // 成績一覧表(KNJD062) 学期
                common = new KnjD062A_Gakki(db2, param, false, false, false, false, true, false);
                common._fieldname = "SEM" + param._semester + "_VALUE";
                common._fieldname2 = null;
                common._testName = "(評定)";
                common._item1Name = "単位";
                common._item2Name = "評価";
                common._item3Name = "欠課";
                common._item4Name = "評価合計";
                common._item5Name = "評価平均";
            } else {
                // 成績一覧表(KNJD062) 学年
                common = new KnjD062A_Grade(db2, param, true, true, false, false, true, false);
                common._fieldname = "GRAD_VALUE";
                common._fieldname2 = null;
                common._testName = "(評定)";
                common._item1Name = "単位";
                common._item2Name = "評定";
                common._item3Name = "欠課";
                common._item4Name = "評定合計";
                common._item5Name = "評定平均";
            }
        }
        return common;
    }

    private boolean printMain(final Param param, final HRInfo hrInfo) {
        boolean hasData = false;

        final List studentListList = new ArrayList();
        final Map pList = new HashMap();
        for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            final Integer p = new Integer(student._gnum / MAX_LINE + (student._gnum % MAX_LINE == 0 ? 0 : 1));

            final List list;
            if (null == pList.get(p)) {
                list = new ArrayList();
                studentListList.add(list);
                pList.put(p, list);
            } else {
                list = (List) pList.get(p);
            }
            list.add(student);
        }

        for (final Iterator itl = studentListList.iterator(); itl.hasNext();) {
            final List list = (List) itl.next();

            if (printSub(param, hrInfo, list)) {
                hasData = true;
            }
        }

        return hasData;
    }

    private boolean printSub(
            final Param param,
            final HRInfo hrInfo,
            final List students
    ) {
        boolean hasData = false;

        int line = 0;  // 科目の列番号
        final int subclassesNum = hrInfo._subclasses.size();

        for (final Iterator it = hrInfo._subclasses.values().iterator(); it.hasNext();) {
            if (0 == line % MAX_COLUMN) {
                _svf.VrSetForm(FORM_FILE, 4);
                printHeader(hrInfo, param);
                _knjCommon.printHeaderOther(param);
                // 生徒名等を印字
                for (final Iterator sit = students.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();
                    _svf.doNumberingSvfOut("name", student._gnum, student._name);    // 氏名

                    // 出席番号
                    final String attendNo = DEC_FMT2.format(Integer.parseInt(student._attendNo));
                    _svf.doNumberingSvfOutn("NUMBER", student._gnum, 0, attendNo);

                    _svf.doNumberingSvfOut("REMARK", student._gnum, student._transInfo.toString());  // 備考

                    if (_knjCommon._hasJudgementItem) {
                        // 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
                        if (student._isGradePoor) { _svf.doNumberingSvfOutn("CHECK1", student._gnum, 0, "★"); }
                        else if (student._isGradeGood) { _svf.doNumberingSvfOutn("CHECK1", student._gnum, 0, "☆"); }
                        if (student._isAttendPerfect) { _svf.doNumberingSvfOutn("CHECK2", student._gnum, 0, "○"); }
                        else if (student._isKekkaOver) { _svf.doNumberingSvfOutn("CHECK2", student._gnum, 0, "●"); }
                    }
                }
            }
            if (subclassesNum == line + 1) {
                for (final Iterator sit = students.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();
                    printStudentOnLastpage(student, param);
                }
                if (null != hrInfo._avgHrTotalScore) {
                    _svf.VrsOut("TOTAL51", sishaGonyu(hrInfo._avgHrTotalScore));
                }

                if (null != hrInfo._avgHrAverageScore) {
                    _svf.VrsOut("AVERAGE51", sishaGonyu(hrInfo._avgHrAverageScore));
                }

                if (null != hrInfo._perHrPresent) {
                    _svf.VrsOut("PER_ATTEND", sishaGonyu(hrInfo._perHrPresent));
                }

                if (null != hrInfo._perHrAbsent) {
                    _svf.VrsOut("PER_ABSENCE", sishaGonyu(hrInfo._perHrAbsent));
                }

            }  // 生徒別総合成績および出欠を印字

            final SubClass subclass = (SubClass) it.next();
            final int used = printSubclasses(subclass, line, students, param);
            line += used;
            if (0 < used) { hasData = true; }
        }

        return hasData;
    }

    /**
     * ページ見出し・項目・欄外記述を印刷します。
     */
    private void printHeader(
            final HRInfo hrInfo,
            final Param param
    ) {
        // 年度
        final int year = Integer.parseInt(param._year);
        _svf.VrsOut("year2", nao_package.KenjaProperties.gengou(year) + "年度");

        _svf.VrsOut("ymd1", param._now); // 作成日

        // 出欠集計範囲
        final String date_S = KNJ_EditDate.h_format_JP(param._sDate);
        final String date_E = KNJ_EditDate.h_format_JP(param._date);
        _svf.VrsOut("DATE", date_S + FROM_TO_MARK + date_E);
        _svf.VrsOut("DATE2", date_S + FROM_TO_MARK + date_E);

        _svf.VrsOut("teacher", hrInfo._staffName);  //担任名
        _svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
        if (_knjCommon._hasCompCredit) {
            _svf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
        }
        _svf.VrsOut("lesson20", hrInfo._HrMLesson);  // 授業日数

        for (int i = 1; i <= MAX_COLUMN; i++) {
            _svf.VrsOutn("ITEM1",i, _knjCommon._item1Name);
            _svf.VrsOutn("ITEM2",i, _knjCommon._item2Name);
            _svf.VrsOutn("ITEM3",i, _knjCommon._item3Name);
        }
        _svf.VrsOut("ITEM4", _knjCommon._item4Name);
        _svf.VrsOut("ITEM5", _knjCommon._item5Name);
        _svf.VrsOut("ITEM6", _knjCommon._item1Name + "・" + _knjCommon._item2Name + "・" + _knjCommon._item3Name);

        // 一覧表枠外の文言
        _svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
        _svf.VrsOut("NOTE1",  " " );
        _svf.VrsOut("NOTE2",  "：欠課時数超過者" );
    }

    /**
     * 該当科目名および科目別成績等を印字する処理
     * @param subclass
     * @param line：科目の列番号
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private int printSubclasses(
            final SubClass subclass,
            final int line,
            final List stulist,
            final Param param
    ) {
        printSubClass(subclass, line);  // 該当科目名等を印字
        // 生徒別該当科目成績を印字する処理
        for (final Iterator sit = stulist.iterator(); sit.hasNext();) {
            final Student student = (Student) sit.next();
            // 科目別明細の印字処理
            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                printScoreDetail(line, student._gnum, detail, param);
            }
        }
        _svf.VrEndRecord();
        return 1;
    }

    /**
     * 科目項目(教科名・科目名・)を印刷します。
     * @param line 科目の列番
     */
    private void printSubClass(final SubClass subclass, final int line) {
        int i = ((line + 1) % 19 == 0)? 19: (line + 1) % 19;
        if (log.isDebugEnabled()) {
            log.debug("subclassname=" + subclass._subclasscode + " " + subclass._subclassabbv + "   line="+line + "  i="+i);
        }

        //教科名
        _svf.VrsOut("course1", subclass._classabbv);

        //科目名
        if (subclass._electdiv) {
            _svf.VrAttributen("subject1", i, "Paint=(2,70,2),Bold=1");
        }
        _svf.VrsOutn("subject1", i, subclass._subclassabbv);
        if (subclass._electdiv) {
            _svf.VrAttributen("subject1", i, "Paint=(0,0,0),Bold=0");
        }

        //単位数
        if (0 != subclass._maxcredit) {
            if (subclass._maxcredit == subclass._mincredit) {
                _svf.VrsOutn("credit1", i, String.valueOf(subclass._maxcredit));
            } else {
                _svf.VrsOutn("credit1", i, String.valueOf(subclass._mincredit) + " \uFF5E " + String.valueOf(subclass._maxcredit));
            }
        }

        //授業時数
        if (0 != subclass._jisu) {
            _svf.VrsOutn("lesson1", i, String.valueOf(subclass._jisu));
        }

        //学級平均・合計
        if (null != subclass._scoreaverage) {
            _svf.VrsOutn("AVE_CLASS", i, subclass._scoreaverage);
        }
        if (null != subclass._scoretotal) {
            _svf.VrsOutn("TOTAL_SUBCLASS", i, subclass._scoretotal);
        }
    }

    /**
     * 生徒別科目別素点・評定・欠課時数等を印刷します。
     * @param line 科目の列番
     * @param gnum 生徒の行番
     */
    private void printScoreDetail(
            final int line,
            final int gnum,
            final ScoreDetail detail,
            final Param param
    ) {
        int column = line % MAX_COLUMN + 1;
        // 素点・単位・保健室欠課(指導)
        if (_knjCommon._isPrintDetailTani) {
            if ("2".equals(param._paramItem)) {
                // 保健室欠課(指導)
                if (null != detail._nurseoff) {
                    _svf.doNumberingSvfOutn("rate", gnum, column, detail._nurseoff.toString());
                }
            } else {
                // 単位を印刷
                final Integer credit;
                if (_knjCommon._isGakunenmatu) {
                    credit = detail._getCredit;
                } else {
                    credit = detail._credits;
                }
                if (null != credit) {
                    if (null != detail._replaceMoto && 1 <= detail._replaceMoto.intValue()) {
                        _svf.doNumberingSvfOutn("rate", gnum, column, "(" + credit.toString() + ")");
                    } else {
                        _svf.doNumberingVrAttributen("rate", gnum, column, "Hensyu=3");
                        _svf.doNumberingSvfOutn("rate", gnum, column, credit.toString());
                    }
                }
            }
        } else {
            if (null != detail._score) {
                _svf.doNumberingSvfOutn("rate", gnum, column, detail._score._strScore); // 素点を印刷
            }
        }

        // 成績
        if (null != detail._patternAssess) {
            if (param.doPrintCreditDrop(_knjCommon) && NumberUtils.isDigits(detail._patternAssess._strScore) && 1 == Integer.parseInt(detail._patternAssess._strScore)) {
                _svf.doNumberingSvfOutn("late", gnum, column,  "*" + detail._patternAssess._strScore);
            } else {
                _svf.doNumberingSvfOutn("late", gnum, column, detail._patternAssess._strScore);
            }
        }

        // 欠課
        final boolean kasan_type = "2".equals(detail._calculateCreditFlg);
        if (null != detail._absent && false == kasan_type) {
            final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
            if (0 != value) {
                // 欠課時数フィールド名は、欠課時数算定コードにより異なる。
                final String field = (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) ? "kekka2_" : "kekka";

                if (detail._isOver) {
                    _svf.doNumberingVrAttributen(field, gnum, column, "Paint=(2,70,1),Bold=1");
                }
                _svf.doNumberingSvfOutn(field, gnum, column, param.getAbsentFmt().format(detail._absent.floatValue()));
                if (detail._isOver) {
                    _svf.doNumberingVrAttributen(field, gnum, column, "Paint=(0,0,0),Bold=0");   // 網掛けクリア
                }
            }
        }
    }

    /**
     * 最後のページの印字処理（成績総合/出欠の記録）
     */
    private void printStudentOnLastpage(final Student student, final Param param) {
        // 生徒別総合点・平均点・順位を印刷します。
        if (0 < student._total._count) {
            _svf.doNumberingSvfOut("TOTAL", student._gnum, String.valueOf(student._total._total));  // 総合点

            // 平均点
            _svf.doNumberingSvfOut("AVERAGE", student._gnum, sishaGonyu(student._total._avgBigDecimal));

            // 学年順位
            final int gradeRank = 1 + _gradeAvg.indexOf(student._total._avgBigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP));
            if (1 <= gradeRank) {
                _svf.doNumberingSvfOut("RANK2_", student._gnum, String.valueOf(gradeRank));
            }
        }

        final int hrRank = student.rank();
        if (1 <= hrRank) {
            _svf.doNumberingSvfOut("RANK", student._gnum, String.valueOf(hrRank));  // 学級順位
        }

        if (null != student._attendInfo) {
            _svf.doNumberingSvfOutNonZero("PRESENT", student._gnum, student._attendInfo._mLesson);      // 出席すべき日数
            _svf.doNumberingSvfOutNonZero("SUSPEND", student._gnum, student._attendInfo._suspend);      // 出席停止
            _svf.doNumberingSvfOutNonZero("KIBIKI", student._gnum,  student._attendInfo._mourning);     // 忌引
            _svf.doNumberingSvfOutNonZero("ABSENCE", student._gnum, student._attendInfo._absent);       // 欠席日数
            _svf.doNumberingSvfOutNonZero("ATTEND", student._gnum,  student._attendInfo._present);      // 出席日数
            _svf.doNumberingSvfOutNonZero("TOTAL_LATE", student._gnum, student._attendInfo._late);      // 遅刻回数
            _svf.doNumberingSvfOutNonZero("LEAVE", student._gnum,   student._attendInfo._early);        // 早退回数
            _svf.doNumberingSvfOutNonZero("ABROAD", student._gnum,  student._attendInfo._transDays);    // 留学等実績
        }

        if (_knjCommon._hasCompCredit) {
            //今年度履修単位数
            _svf.doNumberingSvfOutNonZero("R_CREDIT", student._gnum, String.valueOf(student._compCredit + student._currentAbroadCredits));
            //今年度修得単位数
            _svf.doNumberingSvfOutNonZero("C_CREDIT", student._gnum, String.valueOf(student._getCredit + student._currentAbroadCredits));
        }
        if (_knjCommon._hasJudgementItem) {
            // 前年度までの単位数を印字
            // 今年度認定単位数
            _svf.doNumberingSvfOutNonZero("A_CREDIT", student._gnum, String.valueOf(student._qualifiedCredits));
            // 前年度までの修得単位数
            _svf.doNumberingSvfOutNonZero("PRE_C_CREDIT", student._gnum, String.valueOf(student._previousCredits + student._previousAbroadCredits));
            // 修得単位数計
            int t = student._getCredit + student._qualifiedCredits + student._previousCredits + student._currentAbroadCredits + student._previousAbroadCredits;
            if (t != 0) {
                int g = _knjCommon._gradCredits;  // 卒業認定単位数
                if (g != 0 && g <= t) {
                    _svf.doNumberingSvfOut("TOTAL_C_CREDIT", student._gnum, "@" + String.valueOf(t));
                } else {
                    _svf.doNumberingSvfOut("TOTAL_C_CREDIT", student._gnum, String.valueOf(t));
                }
            }
            // 前年度までの未履修科目数
            if (param._isChiyoda) {
                _svf.doNumberingSvfOutNonZero("PRE_N_CREDIT", student._gnum, student.getPreviousMirisyu());
            } else {
                _svf.doNumberingSvfOutNonZero("PRE_N_CREDIT", student._gnum, String.valueOf(student._previousMirisyu));
            }
        }
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return DEC_FMT1.format(bd.setScale(1, BigDecimal.ROUND_HALF_UP));
    }

    private static double round10(final int a, final int b) {
        return Math.round(a * 10.0 / b) / 10.0;
    }

    protected String getRecordTableName(final String prgid) {
        return "KNJD062B".equals(prgid) ? "V_RECORD_SCORE_DAT" : "RECORD_DAT";
    }

    protected void vrsOutMarkPrgid(final Param param) {
        _svf.VrsOut("MARK", "/");
        _svf.VrsOut("PRGID", param._prgId);
    }

    //--- 内部クラス -------------------------------------------------------
    // KnjCommon -- KnjGrade -- KNJGakki
    //           |- KnjD062A_Grade -- KnjD062A_Gakki
    //           |- KnjD062A_Inter -- KnjD062A_Term
    /**
     * 基準となる処理クラス
     */
    private abstract class KnjCommon {
        String _fieldname;
        String _fieldname2;
//        String _fieldChaircd;
        protected String _testName;
        protected int _gradCredits;  // 卒業認定単位数
        protected String _item1Name;  // 明細項目名
        protected String _item2Name;  // 明細項目名
        protected String _item3Name;  // 明細項目名
        protected String _item4Name;  // 総合点欄項目名
        protected String _item5Name;  // 平均点欄項目名
        protected Map _noEnableValueCd;  // 評定を無いものとして扱う教科コード

        final boolean _isGakunenmatu; // 学年末はTrue
        final boolean _hasCompCredit; // 履修単位数/修得単位数なし
        final boolean _hasJudgementItem; // 判定会議資料用の項目なし
        final boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrue
        final boolean _isPrintDetailTani; // 明細の"成績"欄に単位を印刷する場合はTrue
        final boolean _isTargetValueScore;

        public KnjCommon(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            _gradCredits = getGradCredits(db2, param);
            setNoEnableValueCd(db2, param);
            _isGakunenmatu = isGakunenmatu;
            _hasCompCredit = hasCompCredit;
            _hasJudgementItem = hasJudgementItem;
            _enablePringFlg = enablePringFlg;
            _isPrintDetailTani = isPrintDetailTani;
            _isTargetValueScore = isTargetValueScore;
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            if (_isTargetValueScore) {
                return d._score;
            }
            return d._patternAssess;
        }

        /**
         * (帳票種別によって異なる)ページ見出し・項目・欄外記述を印刷します。
         */
        abstract void printHeaderOther(final Param param);

        // 卒業認定単位数の取得
        private int getGradCredits(
                final DB2UDB db2,
                final Param param
        ) {
            int gradcredits = 0;
            PreparedStatement ps = null;
            try {
                final String sql = sqlGradCredits(param);
                ps = db2.prepareStatement(sql);
                ResultSet rs = null;
                try {
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        gradcredits = rs.getInt("GRAD_CREDITS");
                    }
                } catch (SQLException e) {
                    log.error("SQLException", e);
                } finally {
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
            return gradcredits;
        }

        /**
         * 卒業認定単位数
         */
        private String sqlGradCredits(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + param._year + "'");
            return stb.toString();
        }

        /**
         * 評定を無いものとして扱う教科コードを設定します。<br>
         * 名称マスター 「D008」に登録されているコード(NAMECD2)をセットします。
         */
        void setNoEnableValueCd(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _noEnableValueCd = new HashMap();
            try {
                final String sql;
                if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                    sql = "SELECT classcd || '-' || school_kind as namecd2, class_remark7 as namespare1 FROM class_detail_dat WHERE year='" + param._year + "' AND class_seq='003' ";
                } else {
                    sql = "SELECT namecd2, namespare1 FROM v_name_mst WHERE year='" + param._year + "' AND namecd1='D008' AND namecd2 IS NOT NULL";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) { _noEnableValueCd.put(rs.getString("namecd2"), new D008(rs.getString("namecd2"), rs.getString("namespare1"))); }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    private static class D008 {
        final String _namecd2;
        final String _namespare1;
        D008(final String namecd2, final String namespare1) {
            _namecd2 = namecd2;
            _namespare1 = namespare1;
        }
        public String toString() {
            return "D008(" + _namecd2 + ", " + _namespare1 + ")";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class KnjGrade extends KnjCommon {
        public KnjGrade(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            super(db2, param, isGakunenmatu, hasCompCredit, hasJudgementItem, enablePringFlg, isPrintDetailTani, isTargetValueScore);
        }

        void printHeaderOther(
                final Param param
        ) {
            _svf.VrsOut("TITLE", param._semesterName + "成績一覧表"); //成績名称
            _svf.VrsOut("MARK1_2",  param._assess.toString());
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class KnjGakki extends KnjGrade {
        public KnjGakki(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            super(db2, param, isGakunenmatu, hasCompCredit, hasJudgementItem, enablePringFlg, isPrintDetailTani, isTargetValueScore);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class KnjD062A_Grade extends KnjCommon {
        public KnjD062A_Grade(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            super(db2, param, isGakunenmatu, hasCompCredit, hasJudgementItem, enablePringFlg, isPrintDetailTani, isTargetValueScore);
        }

        void printHeaderOther(final Param param) {
            _svf.VrsOut("TITLE",  "  成績一覧表（評定）");
            vrsOutMarkPrgid(param);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class KnjD062A_Gakki extends KnjD062A_Grade {
        public KnjD062A_Gakki(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            super(db2, param, isGakunenmatu, hasCompCredit, hasJudgementItem, enablePringFlg, isPrintDetailTani, isTargetValueScore);
        }

        void printHeaderOther(final Param param) {
            _svf.VrsOut("TITLE" , param._semesterName + "成績一覧表");
            vrsOutMarkPrgid(param);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 中間試験成績の処理クラス
     */
    private class KnjD062A_Inter extends KnjCommon {

        KnjD062A_Inter(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            super(db2, param, isGakunenmatu, hasCompCredit, hasJudgementItem, enablePringFlg, isPrintDetailTani, isTargetValueScore);
        }

        void printHeaderOther(final Param param) {
            final String title;
            if ("KNJD062B".equals(param._prgId)) {
                final String one234;
                if ("1".equals(param._semester)) {
                    one234 = param._testKindCd.startsWith("01") ? "１" : "２";
                } else {
                    one234 = param._testKindCd.startsWith("01") ? "３" : "４";
                }
                title = one234 + "学期期末";
            } else {
                title = param._semesterName + " " + _testName;
            }
            _svf.VrsOut("TITLE" , title + " 成績一覧表");

            vrsOutMarkPrgid(param);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 期末試験成績の処理クラス
     */
    private class KnjD062A_Term extends KnjD062A_Inter {
        KnjD062A_Term(
                final DB2UDB db2,
                final Param param,
                final boolean isGakunenmatu,
                final boolean hasCompCredit,
                final boolean hasJudgementItem,
                final boolean enablePringFlg,
                final boolean isPrintDetailTani,
                final boolean isTargetValueScore
        ) throws Exception {
            super(db2, param, isGakunenmatu, hasCompCredit, hasJudgementItem, enablePringFlg, isPrintDetailTani, isTargetValueScore);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学級。
     */
    private class HRInfo implements Comparable {
        private final String _code;
        private String _staffName;
        private String _hrName;
        private List _students = new LinkedList();
        private final Map _subclasses = new TreeMap();
        private List _hrRanking;
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private BigDecimal _perHrPresent;  // 学級の出席率
        private BigDecimal _perHrAbsent;  // 学級の欠席率
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrMLesson;  // 学級の授業日数

        HRInfo(
                final String code
        ) {
            _code = code;
        }

        void load(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            loadHRClassStaff(db2, param, _code);
            loadStudents(db2, param);
            loadStudentsInfo(db2, param);
            loadAttend(db2, param);
            loadAttendSubclassInfo(db2, param);
            loadScoreDetail(db2, param);
            if ("2".equals(param._paramItem)) {
                loadNurseOff(db2, param);
            }

            _hrRanking = createHrRanking();
            log.debug("学級RANK:" + _hrRanking);

            setSubclassAverage();
            loadAbroadCredits(db2, param);  // 今年度および前年度までの留学修得単位数取得
            setHrTotal();  // 学級平均等の算出

            if (_knjCommon._hasJudgementItem) {
                if (param._isChiyoda) {
                    final List creditMst = CreditMst.load(db2, param);

                    loadStudentCourse(db2, creditMst, param._year);
                }

                loadPreviousCredits(db2, param);  // 前年度までの修得単位数取得
                if (param._isChiyoda) {
                    loadPreviousMirisyu(db2, param);  // 前年度までの未履修（必須科目）
                } else {
                    loadPreviousMirisyu0(db2, param);  // 前年度までの未履修数（必須科目）
                }
                loadQualifiedCredits(db2, param);  // 今年度の資格認定単位数
                setGradeAttendCheckMark(param);  // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
            }
        }

        private Student getStudent(String code) {
            if (code == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (code.equals(student._code)) {
                    return student;
                }
            }
            return null;
        }

        /**
         * 「成績不振者のみ印刷」用のメソッドです。<br>
         * HRInfoクラスのStudentリストを成績不振者のリストに置き換えます。<br>
         * また、Studentクラスの行番号変数 int _gnum をシーケンスな整数で上書きします。
         * @param hrInfo
         * @return
         */
        private void removeStudent(final Param param) {
            int count = 0;
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student.isGradePoor(param) || student.isKekkaOver()) {
                    student._gnum = ++count;
                } else {
                    it.remove();
                }
            }
        }

        private void loadNurseOff(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String[] schregnos = getSchregnos();

            // ATTEND_SUBCLASS_DAT から取得
            try {
                final String sql = sqlNurseOff(param, schregnos);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String subClassCd = rs.getString("subclasscd");
                    final Integer nurseoff = KNJServletUtils.getInteger(rs, "nurseoff");

                    final Student student = getStudent(schregno);
                    if (null == student) {
                        continue;
                    }
                    final ScoreDetail sd = (ScoreDetail) student._scoreDetails.get(subClassCd);
                    if (null == sd) {
                        continue;
                    }
                    sd._nurseoff = nurseoff;
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // ATTEND_DAT から端数を取得
            try {
                final String sql = sqlAttendDat(param, schregnos);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String subClassCd = rs.getString("subclasscd");
                    final Student student = getStudent(schregno);
                    if (null == student) {
                        continue;
                    }
                    final ScoreDetail sd = (ScoreDetail) student._scoreDetails.get(subClassCd);
                    if (null == sd) {
                        continue;
                    }
                    if (null != sd._nurseoff) {
                        sd._nurseoff = new Integer(sd._nurseoff.intValue() + 1);
                    } else {
                        sd._nurseoff = new Integer(1);
                    }
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlAttendDat(final Param param, final String[] whereIn) {
            final String tuitati = KNJServletUtils.getFirstDate(param._date);

            String sql;
            sql = "SELECT"
                + "  t1.schregno,";
            if ("1".equals(param._useCurriculumcd)) {
                sql += " t2.classcd || '-' || t2.school_kind || '-' || t2.curriculum_cd || '-' || ";
            }
            sql +="  t2.subclasscd AS subclasscd"
                +" FROM"
                + "  attend_dat t1 INNER JOIN chair_dat t2 ON t1.year=t2.year AND t1.chaircd=t2.chaircd"
                + "     AND t2.semester='" + param._semester + "'"
                + "  LEFT JOIN ATTEND_DI_CD_DAT L1 ON t1.year = L1.year AND t1.DI_CD = L1.DI_CD"
                + " WHERE"
                + "  t1.year='" + param._year + "' and"
                + "  L1.REP_DI_CD = '14' and"
                + "  attenddate BETWEEN '" + tuitati + "' AND '" + param._date + "' and"
                + "  schregno IN " + SQLUtils.whereIn(true, whereIn)
                ;
            return sql;
        }

        private String sqlNurseOff(final Param param, final String[] whereIn) {
            final String excludeMonth = param._date.substring(5, 7);

            String sql;
            sql = "SELECT"
                + "  schregno,";
            if ("1".equals(param._useCurriculumcd)) {
                sql += " classcd || '-' || school_kind || '-' || curriculum_cd || '-' || ";
            }
            sql +="  subclasscd AS subclasscd,"
                + "  sum(nurseoff) as nurseoff"
                + " FROM"
                + "  attend_subclass_dat"
                + " WHERE"
                + "  year='" + param._year + "' and"
                + "  month <> '" + excludeMonth + "' and"
                + "  nurseoff <> 0 and"
                + "  schregno IN " + SQLUtils.whereIn(true, whereIn)
                + " GROUP BY"
                + "  schregno,";
            if ("1".equals(param._useCurriculumcd)) {
                sql += " classcd || '-' || school_kind || '-' || curriculum_cd || '-' || ";
            }
            sql +="  subclasscd";
                ;
            return sql;
        }

        private String[] getSchregnos() {
            final String[] whereIn = new String[_students.size()];
            int i = 0;
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                whereIn[i] = student._code;
                i++;
            }
            return whereIn;
        }

        private void loadStudentCourse(final DB2UDB db2, final List creditMst, final String currentYear) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStudentCourse(currentYear);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._code);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String grade = rs.getString("GRADE");
                        final String courseCd = rs.getString("COURSECD");
                        final String majorCd = rs.getString("MAJORCD");
                        final String courseCode = rs.getString("COURSECODE");

                        final Collection creditMsts = select(creditMst, year, grade, courseCd, majorCd, courseCode);
                        if (null != creditMst) {
                            student._creditMst.put(year, creditMsts);
                        }
                    }
                    log.debug(student + " の前年度までの必履修科目=" + student._creditMst);
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private Collection select(
                final List creditMst,
                final String year,
                final String grade,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            final Collection ans = new ArrayList();
            for (final Iterator it = creditMst.iterator(); it.hasNext();) {
                final CreditMst c = (CreditMst) it.next();

                if (!year.equals(c._year)) {
                    continue;
                }
                if (!grade.equals(c._grade)) {
                    continue;
                }
                if (!courseCd.equals(c._courseCd)) {
                    continue;
                }
                if (!majorCd.equals(c._majorCd)) {
                    continue;
                }
                if (!courseCode.equals(c._courseCode)) {
                    continue;
                }
                ans.add(c);
            }
            return (null == ans) ? null : ans;
        }

        /**
         * @deprecated
         */
        private int findCreditMst(
                final List creditMst,
                final String year,
                final String grade,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            int ans = 0;
            for (final Iterator it = creditMst.iterator(); it.hasNext();) {
                final CreditMst c = (CreditMst) it.next();

                if (!year.equals(c._year)) {
                    continue;
                }
                if (!grade.equals(c._grade)) {
                    continue;
                }
                if (!courseCd.equals(c._courseCd)) {
                    continue;
                }
                if (!majorCd.equals(c._majorCd)) {
                    continue;
                }
                if (!courseCode.equals(c._courseCode)) {
                    continue;
                }
                ans++;
            }
            return ans;
        }

        private String sqlStudentCourse(final String currentYear) {
            final String sql;
            sql = "SELECT"
                + "  year,"
                + "  max(semester) as max_semester,"
                + "  grade,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode"
                + " FROM"
                + "  schreg_regd_dat"
                + " WHERE"
                + "  schregno=? and"
                + "  int(year) < " + Integer.valueOf(currentYear)
                + " GROUP BY"
                + "  year,"
                + "  grade,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode"
                ;
            return sql;
        }

        private void loadHRClassStaff(
                final DB2UDB db2,
                final Param param,
                final String hrClass
        ) {
            final KNJ_Get_Info.ReturnVal returnval = param._getinfo.Hrclass_Staff(
                    db2,
                    param._year,
                    param._semester,
                    hrClass,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private void loadStudents(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassStdList(param);
                ps = db2.prepareStatement(sql);
                ps.setString(1, _code);

                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
                    if (param._noKetuban){
                        gnum++;
                    } else {
                        gnum = rs.getInt("ATTENDNO");
                    }
                    final Student student = new Student(rs.getString("SCHREGNO"), this, gnum);
                    _students.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * SQL HR組の学籍番号を取得するSQL
         */
        private String sqlHrclassStdList(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(SEMEALL)) {
                stb.append(    "AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append(    "AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdNameInfo(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._code);

                    rs = ps.executeQuery();
                    if (rs.next()) {
                        TransInfo transInfo = null;
                        try {
                            final String grd_date = rs.getString("GRD_DATE");
                            final String trs_date = rs.getString("TRS_DATE");
                            final String ent_date = rs.getString("ENT_DATE");
                            if (null != grd_date) {
                                final String grd_name = rs.getString("GRD_NAME");
                                transInfo = new TransInfo(grd_date, grd_name);
                            } else if (null != trs_date) {
                                final String trs_name = rs.getString("TRS_NAME");
                                transInfo = new TransInfo(trs_date, trs_name);
                            } else if (null != ent_date) {
                                final String ent_name = rs.getString("ENT_NAME");
                                transInfo = new TransInfo(ent_date, ent_name);
                            }
                        } catch (final SQLException e) {
                             log.error("SQLException", e);
                        }
                        if (null == transInfo) {
                            transInfo = new TransInfo(null, null);
                        }
                        student._attendNo = rs.getString("ATTENDNO");
                        student._name = rs.getString("NAME");
                        student._transInfo = transInfo;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * SQL 任意の生徒の学籍情報を取得するSQL
         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
         */
        private String sqlStdNameInfo(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
            stb.append(        "W4.GRD_DATE AS GRD_DATE, ");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) AS GRD_NAME, ");
            stb.append(        "W5.TRANSFER_SDATE AS TRS_DATE,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS TRS_NAME, ");
            stb.append(        "W6.ENT_DATE AS ENT_DATE, ");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W6.ENT_DIV) AS ENT_NAME ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = '" + param._semester + "' ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W6 ON W6.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND W6.ENT_DIV IN('4','5') AND W6.ENT_DATE > W2.SDATE ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
            stb.append(    "AND W1.SCHREGNO = ? ");
            if (!param._semester.equals(SEMEALL)) {
                stb.append("AND W1.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("AND W1.SEMESTER = '" + param._semeFlg + "' ");
            }

            return stb.toString();
        }

        private void loadAttend(
                final DB2UDB db2,
                final Param param
        ) {
            ResultSet rs = null;

            try {
                String targetSemes = (param._isRuikei) ? SEMEALL : param._semester;
                if (null == param._psMap.get("ATTEND_SEMES")) {
                    String prestatStdTotalAttend = AttendAccumulate.getAttendSemesSql(
                            param._semesFlg,
                            param._definecode,
                            param._knjSchoolMst,
                            param._year,
                            param.SSEMESTER,
                            param._semester.equals(SEMEALL) ? param._semeFlg : param._semester,
                            (String) param._hasuuMap.get("attendSemesInState"),
                            param._periodInState,
                            (String) param._hasuuMap.get("befDayFrom"),
                            (String) param._hasuuMap.get("befDayTo"),
                            (String) param._hasuuMap.get("aftDayFrom"),
                            (String) param._hasuuMap.get("aftDayTo"),
                            param._grade,
                            "?",
                            null,
                            "SEMESTER",
                            param._useCurriculumcd,
                            param._useVirus,
                            param._useKoudome
                    );
                    PreparedStatement ps = db2.prepareStatement( prestatStdTotalAttend );        //出欠累計データ
                    param._psMap.put("ATTEND_SEMES", ps);
                }
                PreparedStatement ps = (PreparedStatement) param._psMap.get("ATTEND_SEMES");
                ps.setString(1, _code.substring(2));
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    if (!targetSemes.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final AttendInfo attendInfo = new AttendInfo(
                            rs.getInt("LESSON"),
                            rs.getInt("MLESSON"),
                            rs.getInt("SUSPEND"),
                            rs.getInt("MOURNING"),
                            rs.getInt("SICK"),
                            rs.getInt("PRESENT"),
                            rs.getInt("LATE"),
                            rs.getInt("EARLY"),
                            rs.getInt("TRANSFER_DATE")
                    );
                    student._attendInfo = attendInfo;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }

        private void loadAttendSubclassInfo(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlAttendSubclassInfo(param);
                ps = db2.prepareStatement(sql);
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);
                    if (param._definecode.useschchrcountflg) {
                        ps.setString(i++, _code);
                    }
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final AttendSubclassInfo attendSubclassInfo = new AttendSubclassInfo(
                                rs.getInt("JISU"),
                                rs.getInt("ABSENT1"),
                                rs.getInt("LATE_EARLY")
                        );
                        student._attendSubclassInfo = attendSubclassInfo;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         *  欠課・遅刻・早退データの表
         */
        private String sqlAttendSubclassInfo(final Param param) {
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
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append( "    AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            stb.append(" ,TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(" ) ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append(     " W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append(        "AND W2.YEAR = '" + param._year + "' ");
            stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(        "AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND W2.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
            if (!param._isAttendPerfectSubclass90over) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(        "AND (W2.CLASSCD <= '" + KNJDefineCode.subject_U + "' OR W2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                } else {
                    stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
                }
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     ")");

            //欠課数の表
            stb.append(",ATTEND_A AS(");
                                  //出欠データより集計
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            stb.append(                 "COUNT(*) AS JISU, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('4','5','6','14','11','12','13') THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM ( SELECT T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(                 "FROM   SCH_CHR_DAT T1,");
            stb.append(                        "CHAIR_A T2 ");
            stb.append(                 "WHERE  T1.EXECUTEDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "' ");
            stb.append(                    "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(                    "AND T1.YEAR = '" + param._year + "' ");
            stb.append(                    "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(                    "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                    "AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append(                    "AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                                       "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");
            if (param._definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = ? AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(                 "GROUP BY T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(               ")S1 ");
            stb.append(               "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param._year + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(               "INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = S2.YEAR AND L1.DI_CD = S2.DI_CD ");
            stb.append(                                      " AND L1.REP_DI_CD IN('4','5','6','14','15','16','11','12','13','23','24') ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");
                                  //月別科目別出欠集計データより欠課を取得
            stb.append(          "UNION ALL ");
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
            stb.append(                  "SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append("            + VALUE(OFFDAYS, 0) ");
            }
            stb.append(                  ") AS JISU, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append("            + VALUE(OFFDAYS, 0) ");
            }
            if ("1".equals(param._knjSchoolMst._subSuspend)) {
                stb.append("            + VALUE(SUSPEND, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                if ("1".equals(param._knjSchoolMst._subVirus)) {
                    stb.append("            + VALUE(VIRUS, 0) ");
                }
            }
            if ("true".equals(param._useKoudome)) {
                if ("1".equals(param._knjSchoolMst._subKoudome)) {
                    stb.append("            + VALUE(KOUDOME, 0) ");
                }
            }
            if ("1".equals(param._knjSchoolMst._subMourning)) {
                stb.append("            + VALUE(MOURNING, 0) ");
            }
            if ("1".equals(param._knjSchoolMst._subAbsent)) {
                stb.append("            + VALUE(ABSENT, 0) ");
            }
            stb.append(                  ") AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + param._year + "' AND ");
            stb.append(              "W1.SEMESTER <= '" + param._semester + "' AND ");
            stb.append(                  "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + param._semesMonth + "' AND ");
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                   "W1.SUBCLASSCD, W1.SEMESTER ");
            stb.append(     ") ");

            //欠課数の表
            stb.append(",ATTEND_B AS(");
            stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");

            if (param._definecode.absent_cov == 1 || param._definecode.absent_cov == 3) {
            //遅刻・早退を学期で欠課換算する
                stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "      , SUM(JISU)AS JISU ");
                stb.append(     "      , SUM(LATE_EARLY)AS LATE_EARLY ");
                stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, SUM(JISU)AS JISU, ");
                if (param._definecode.absent_cov == 1 || param._semester.equals(SEMEALL)) {
                    stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param._definecode.absent_cov_late + " AS ABSENT1 ");
                } else {
                    stb.append(                 "FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");
                }
                stb.append(     "              , SUM(LATE_EARLY)AS LATE_EARLY ");
                stb.append(             "FROM    ATTEND_A ");
                stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append(             ")W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else if (param._definecode.absent_cov == 2 || param._definecode.absent_cov == 4) {
                //遅刻・早退を年間で欠課換算する
                if (param._definecode.absent_cov == 2) {
                    stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param._definecode.absent_cov_late + " AS ABSENT1 ");
                } else {
                    stb.append(       ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");
                }
                stb.append(     "      , SUM(JISU)AS JISU ");
                stb.append(     "      , SUM(LATE_EARLY)AS LATE_EARLY ");
                stb.append(     "FROM    ATTEND_A ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else {
                //遅刻・早退を欠課換算しない
                stb.append(     "      , SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "      , SUM(JISU)AS JISU ");
                stb.append(     "      , SUM(LATE_EARLY)AS LATE_EARLY ");
                stb.append(     "FROM    ATTEND_A W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            }
            stb.append(     ") ");

            //合併科目の欠課数の表
            stb.append(", ATTEND_C AS(");
            stb.append("   SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.ABSENT1) AS ABSENT1, SUM(T1.JISU) AS JISU, SUM(T1.LATE_EARLY)AS LATE_EARLY");
            stb.append("   FROM    ATTEND_B T1, SUBCLASS_REPLACE_COMBINED_DAT T2");
            stb.append("   WHERE   T2.YEAR = '" + param._year + "'");
            stb.append("       AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("   GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T2.COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SCHREGNO ");
            stb.append("        ,FLOAT(SUM(T5.ABSENT1)) AS ABSENT1 ");
            stb.append("        ,SUM(T5.JISU) AS JISU ");
            stb.append("        ,SUM(T5.LATE_EARLY) AS LATE_EARLY ");
            //対象生徒・講座の表
            stb.append(" FROM(");
            stb.append("     SELECT  W2.SCHREGNO,W2.SUBCLASSCD");
            stb.append("     FROM    CHAIR_A W2");
            if (!param._semester.equals(SEMEALL)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,W2.SUBCLASSCD");
            stb.append(" )T1 ");
            //欠課数の表
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT SUBCLASSCD,ABSENT1,JISU,LATE_EARLY");
            stb.append("   FROM   ATTEND_B W1");
            stb.append("   WHERE  NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W2");
            stb.append("                     WHERE  W2.YEAR = '" + param._year + "'");
            stb.append("                        AND W2.COMBINED_SUBCLASSCD = W1.SUBCLASSCD");
            stb.append("                     GROUP BY W2.COMBINED_SUBCLASSCD)");
            stb.append("   UNION SELECT SUBCLASSCD,ABSENT1,JISU,LATE_EARLY");
            stb.append("   FROM   ATTEND_C W1");
            stb.append(" )T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" GROUP BY T1.SCHREGNO");

            return stb.toString();
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdSubclassDetail(param);
                ps = db2.prepareStatement(sql);
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);
                    if (param._definecode.useschchrcountflg) {
                        ps.setString(i++, _code);
                    }
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (_knjCommon._enablePringFlg) {
                            if ("1".equals(rs.getString("PRINT_FLG"))) {
                                continue;
                            }
                        }
                        final ScoreDetail scoreDetail = createScoreDetail(rs, _subclasses, student, param);
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(final Param param) {
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
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append( "    AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            stb.append(" ,TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(" ) ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W2.CLASSCD || '-' || W2.SCHOOL_KIND AS CLASSCD_SK, ");
                stb.append(     " W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append(        " W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append(        "AND W2.YEAR = '" + param._year + "' ");
            stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(        "AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND W2.SEMESTER <= '" + param._semester + "' ");
            stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(        "AND (W2.CLASSCD <= '" + KNJDefineCode.subject_U + "' OR W2.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
            } else {
                stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + KNJDefineCode.subject_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + KNJDefineCode.subject_T + "') ");
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     ")");

            //欠課数の表
            stb.append(",ATTEND_A AS(");
                                  //出欠データより集計
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            stb.append(                 "COUNT(*) AS JISU, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('4','5','6','14','11','12','13') THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN L1.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM ( SELECT T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(                 "FROM   SCH_CHR_DAT T1,");
            stb.append(                        "CHAIR_A T2 ");
            stb.append(                 "WHERE  T1.EXECUTEDATE BETWEEN '" + param._divideAttendDate + "' AND '" + param._date + "' ");
            stb.append(                    "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(                    "AND T1.YEAR = '" + param._year + "' ");
            stb.append(                    "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(                    "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                    "AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append(                    "AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
            stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");  //NO025
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                                       "AND TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");  //NO025
            if (param._definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = ? AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(                 "GROUP BY T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(               ")S1 ");
            stb.append(               "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param._year + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(               "INNER JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = S2.YEAR AND L1.DI_CD = S2.DI_CD ");
            stb.append(                                      " AND L1.REP_DI_CD IN('4','5','6','14','15','16','11','12','13','23','24') ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");

                                  //月別科目別出欠集計データより欠課を取得
            stb.append(          "UNION ALL ");
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' ||");
            }
            stb.append(                                "W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
            stb.append(                  "SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append("            + VALUE(OFFDAYS, 0) ");
            }
            stb.append(                  ") AS JISU, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append("            + VALUE(OFFDAYS, 0) ");
            }
            if ("1".equals(param._knjSchoolMst._subSuspend)) {
                stb.append("            + VALUE(SUSPEND, 0) ");
            }
            if ("true".equals(param._useVirus)) {
                if ("1".equals(param._knjSchoolMst._subVirus)) {
                    stb.append("            + VALUE(VIRUS, 0) ");
                }
            }
            if ("true".equals(param._useKoudome)) {
                if ("1".equals(param._knjSchoolMst._subKoudome)) {
                    stb.append("            + VALUE(KOUDOME, 0) ");
                }
            }
            if ("1".equals(param._knjSchoolMst._subMourning)) {
                stb.append("            + VALUE(MOURNING, 0) ");
            }
            if ("1".equals(param._knjSchoolMst._subAbsent)) {
                stb.append("            + VALUE(ABSENT, 0) ");
            }
            stb.append(                  ") AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + param._year + "' AND ");
            stb.append(              "W1.SEMESTER <= '" + param._semester + "' AND ");

            stb.append(                  "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + param._semesMonth + "' AND ");   //--NO004 NO007
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' ||");
            }
            stb.append(                                 "W1.SUBCLASSCD, W1.SEMESTER ");
            stb.append(     ") ");

            //欠課数の表
            stb.append(",ATTEND_B AS(");
            stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");

            if (param._definecode.absent_cov == 1 || param._definecode.absent_cov == 3) {
            //遅刻・早退を学期で欠課換算する
                stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "      , SUM(JISU)AS JISU ");
                stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, SUM(JISU)AS JISU, ");
                if (param._definecode.absent_cov == 1 || param._semester.equals(SEMEALL)) {
                    stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param._definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
                } else {
                    stb.append(                 "FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
                }
                stb.append(             "FROM    ATTEND_A ");
                stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
                stb.append(             ")W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else if (param._definecode.absent_cov == 2 || param._definecode.absent_cov == 4) {
                //遅刻・早退を年間で欠課換算する
                if (param._definecode.absent_cov == 2) {
                    stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + param._definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
                } else {
                    stb.append(       ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + param._definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
                }
                stb.append(     "      , SUM(JISU)AS JISU ");
                stb.append(     "FROM    ATTEND_A ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            } else {
                //遅刻・早退を欠課換算しない
                stb.append(     "      , SUM(ABSENT1)AS ABSENT1 ");
                stb.append(     "      , SUM(JISU)AS JISU ");
                stb.append(     "FROM    ATTEND_A W1 ");
                stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            }
            stb.append(     ") ");

            //合併科目の欠課数の表
            stb.append(", ATTEND_C AS(");
//            stb.append("   SELECT  SUBCLASSCD,ABSENT1,JISU");
//            stb.append("   FROM    ATTEND_B T1");
//            stb.append("   WHERE   NOT EXISTS(SELECT 'X' FROM   SUBCLASS_REPLACE_COMBINED_DAT T2");
//            stb.append("                      WHERE  T2.YEAR = '" + param._year + "'");
//            stb.append("                         AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("   SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(            " T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.ABSENT1) AS ABSENT1, SUM(T1.JISU) AS JISU");
            stb.append("   FROM    ATTEND_B T1, SUBCLASS_REPLACE_COMBINED_DAT T2");
            stb.append("   WHERE   T2.YEAR = '" + param._year + "'");
            stb.append("       AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' ||");
            }
            stb.append("           T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("   GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(             "T2.COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||");
            }
            stb.append(        " SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||");
            }
            stb.append(                                                                     " T1.SUBCLASSCD)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SUBCLASSCD, T1.CREDITS");
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + param._year + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                          AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(                               " T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(                  " COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' ||");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(             " COMBINED_SUBCLASSCD");
            stb.append(") ");

            // 欠課数の表
            stb.append(",T_ABSENCE_HIGH AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' ||");
            }
            stb.append(                       " SUBCLASSCD AS SUBCLASSCD, ");
            if (param._knjSchoolMst.isJitu()) {
                stb.append(        " VALUE(T5.COMP_ABSENCE_HIGH, 99) ");
            } else {
                stb.append(        " VALUE(T1.ABSENCE_HIGH, 99) ");
            }
            stb.append(    "AS ABSENCE_HIGH ");
            stb.append(    "FROM ");
            if (param._knjSchoolMst.isJitu()) {
                stb.append(    "SCHREG_ABSENCE_HIGH_DAT T5 ");
                stb.append(    "WHERE T5.YEAR = '" + param._year + "' ");
                stb.append(      "AND T5.DIV = '1' ");
            } else {
                stb.append(    "CREDIT_MST T1 ");
                stb.append(      "INNER JOIN SCHNO_A T2 ON ");
                stb.append(          "T1.GRADE = T2.GRADE ");
                stb.append(          "AND T1.COURSECD = T2.COURSECD ");
                stb.append(          "AND T1.MAJORCD = T2.MAJORCD ");
                stb.append(          "AND T1.COURSECODE = T2.COURSECODE ");
                stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
                stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(     " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||");
                }
                stb.append(              " T1.SUBCLASSCD)");
            }
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' ||");
            }
            stb.append("            W3.SUBCLASSCD AS SUBCLASSCD ");
            if (param._semester.equals(SEMEALL)) {
                //学年成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN " + _knjCommon._fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + _knjCommon._fieldname + ")) ");
                stb.append(             "WHEN " + _knjCommon._fieldname + "_DI IS NOT NULL THEN " + _knjCommon._fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            } else if (param._testKindCd.equals(GRAD_SEM_KIND)) {
                //学期成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN " + _knjCommon._fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + _knjCommon._fieldname + ")) ");
                stb.append(             "WHEN " + _knjCommon._fieldname + "_DI IS NOT NULL THEN " + _knjCommon._fieldname + "_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            } else {
                //中間・期末成績  NO024 Modify
                // fieldname:SEM?_XXXX_SCORE / fieldname2:SEM?_XXXX
                if ("0202".equals(param._testKindCd)) {
                    stb.append(       ",CASE WHEN " + _knjCommon._fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + _knjCommon._fieldname + ")) ");
                    stb.append(             "ELSE NULL END AS SCORE ");
                } else {
                    stb.append(       ",CASE WHEN " + _knjCommon._fieldname + " IS NOT NULL THEN RTRIM(CHAR(" + _knjCommon._fieldname + ")) ");
                    stb.append(             "WHEN " + _knjCommon._fieldname + "_DI IS NOT NULL THEN " + _knjCommon._fieldname + "_DI ");
                    stb.append(             "ELSE NULL END AS SCORE ");
                }
                stb.append(       ",CASE WHEN " + _knjCommon._fieldname2 + "_VALUE IS NOT NULL THEN RTRIM(CHAR(" + _knjCommon._fieldname2 + "_VALUE)) ");
                stb.append(             "WHEN " + _knjCommon._fieldname2 + "_VALUE_DI IS NOT NULL THEN " + _knjCommon._fieldname2 + "_VALUE_DI ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
                stb.append(        ",W3.COMP_CREDIT, W3.GET_CREDIT ");  //NO010
            }
            stb.append(    "FROM ");

            final String recordTable = getRecordTableName(param._prgId);
            stb.append(          recordTable);
            stb.append(                              " W3 ");
            if (log.isDebugEnabled()) { log.debug("成績データ=" + recordTable); }
            stb.append(    "WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.PATTERN_ASSESS ");
            stb.append("        ,T3.COMP_CREDIT ");
            stb.append("        ,T3.GET_CREDIT ");
            stb.append("        ,FLOAT(T5.ABSENT1) AS ABSENT1 ");
            stb.append("        ,T5.JISU ");
            stb.append("        ,T12.ABSENCE_HIGH ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSABBV AS SUBCLASSNAME ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,T9.CALCULATE_CREDIT_FLG");
            //対象生徒・講座の表
            stb.append(" FROM(");
            stb.append("     SELECT  W2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W2.CLASSCD_SK, ");
            }
            stb.append("             W2.SUBCLASSCD");
            stb.append("     FROM    CHAIR_A W2");
            if (!param._semester.equals(SEMEALL)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W2.CLASSCD_SK, ");
            }
            stb.append("              W2.SUBCLASSCD");
            stb.append(" )T1 ");
            //成績の表
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,W3.SCORE,W3.PATTERN_ASSESS,COMP_CREDIT,GET_CREDIT");
            stb.append("   FROM   RECORD_REC W3");
            stb.append(" )T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(            " COMBINED_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(              " COMBINED_SUBCLASSCD");
            stb.append(" )T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' ||");
            }
            stb.append(            " ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' ||");
            }
            stb.append("             ATTEND_SUBCLASSCD");
            stb.append(" )T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            //欠課数の表
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT SUBCLASSCD,ABSENT1,JISU");
            stb.append("   FROM   ATTEND_B W1");
            stb.append("   WHERE  NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W2");
            stb.append("                     WHERE  W2.YEAR = '" + param._year + "'");
            stb.append("                        AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append(                             " W2.COMBINED_SUBCLASSCD = W1.SUBCLASSCD");
            stb.append("                     GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' ||");
            }
            stb.append("                           W2.COMBINED_SUBCLASSCD)");
            stb.append("   UNION SELECT SUBCLASSCD,ABSENT1,JISU");
            stb.append("   FROM   ATTEND_C W1");
            stb.append(" )T5 ON T5.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN T_ABSENCE_HIGH T12 ON T12.SCHREGNO = T1.SCHREGNO AND T12.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(     " T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' ||");
            }
            stb.append("                              T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T8.CLASSCD || '-' || T8.SCHOOL_KIND = T1.CLASSCD_SK ");
            } else {
                stb.append("                           T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
            }

            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubClass(
                final ResultSet rs,
                final Map subclasses
        ) {
            String subclasscode = null;
            int credit = 0;
            int jisu = 0;
            try {
                subclasscode = rs.getString("SUBCLASSCD");
                if (rs.getString("CREDITS") != null) { credit = rs.getInt("CREDITS"); }
                if (rs.getString("JISU") != null) { jisu = rs.getInt("JISU"); }
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            //科目クラスのインスタンスを更新して返す
            if (subclasses.containsKey(subclasscode)) {
                SubClass subclass = (SubClass) subclasses.get(subclasscode);
                final int[] ret = Param.setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = ret[0];
                subclass._mincredit = ret[1];
//                if (0 != credit) {
//                    if (subclass._maxcredit < credit) subclass._maxcredit = credit;
//                    if (credit < subclass._mincredit || 0 == subclass._mincredit) subclass._mincredit = credit;
//                }
                if (0 != jisu && subclass._jisu < jisu) subclass._jisu = jisu;
                return subclass;
            }
            //科目クラスのインスタンスを作成して返す
            String classabbv = null;
            String subclassabbv = null;
            boolean electdiv = false;
            try {
                classabbv = rs.getString("CLASSNAME");
                subclassabbv = rs.getString("SUBCLASSNAME");
                if (null != rs.getString("ELECTDIV") && rs.getString("ELECTDIV").equals("1")) electdiv = true;
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            final SubClass subClass = new SubClass(subclasscode, classabbv, subclassabbv, electdiv, credit, jisu);
            subclasses.put(subclasscode, subClass);
            return subClass;
        }

        ScoreDetail createScoreDetail(
                final ResultSet rs,
                final Map subclasses,
                final Student student,
                final Param param
        ) throws SQLException {
            String pAsses = rs.getString("PATTERN_ASSESS");
            String subClassCd = rs.getString("SUBCLASSCD");
            ScoreValue create = ScoreValue.create(pAsses, subClassCd, _knjCommon, param);

//            log.debug(student + ":" + "pAsses=" + pAsses + ", subclasscd=" + subClassCd + ", create=" + create);
            final ScoreDetail detail = new ScoreDetail(
                    getSubClass(rs, subclasses),
                    (Double) rs.getObject("ABSENT1"),
                    (Integer) rs.getObject("JISU"),
                    ScoreValue.create(rs.getString("SCORE"), subClassCd, _knjCommon, param),
                    create,
                    (Integer) rs.getObject("REPLACEMOTO"),
                    (String) rs.getObject("PRINT_FLG"),
                    (Integer) rs.getObject("COMP_CREDIT"),
                    (Integer) rs.getObject("GET_CREDIT"),
                    (BigDecimal) rs.getObject("ABSENCE_HIGH"),
                    (Integer) rs.getObject("CREDITS"),
//                    rs.getString("CHAIRCD")
                    (String) rs.getObject("CALCULATE_CREDIT_FLG")
            );
            return detail;
        }

        // 今年度および前年度までの留学修得単位数取得
        private void loadAbroadCredits(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdAbroadCredits(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        String div = rs.getString("DIV");
                        if ("1".equals(div)) {
                            student._currentAbroadCredits = rs.getInt("ABROAD_CREDITS");
                        } else if ("2".equals(div)) {
                            student._previousAbroadCredits = rs.getInt("ABROAD_CREDITS");
                        }
                    }
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 今年度および前年度までの留学修得単位数取得
         */
        private String sqlStdAbroadCredits(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     '1' AS DIV, ");
            stb.append("     VALUE(SUM(T1.ABROAD_CREDITS),0) AS ABROAD_CREDITS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? AND ");
            stb.append("     T1.TRANSFERCD = '1' AND ");
            stb.append("     T1.ABROAD_CREDITS IS NOT NULL AND ");
            stb.append("     INT(FISCALYEAR(T1.TRANSFER_SDATE)) = " + param._year + " ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '2' AS DIV, ");
            stb.append("     VALUE(SUM(T1.ABROAD_CREDITS),0) AS ABROAD_CREDITS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? AND ");
            stb.append("     T1.TRANSFERCD = '1' AND ");
            stb.append("     T1.ABROAD_CREDITS IS NOT NULL AND ");
            stb.append("     INT(FISCALYEAR(T1.TRANSFER_SDATE)) < " + param._year + " ");

            return stb.toString();
        }

        // 前年度までの修得単位数計
        private void loadPreviousCredits(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdPreviousCredits(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    if (rs.next()) {
                        student._previousCredits = rs.getInt("CREDIT");
                    }
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 前年度までの修得単位数計
         */
        private String sqlStdPreviousCredits(final Param param) {
            final String sql;

            sql = " SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT"
                    + " FROM   SCHREG_STUDYREC_DAT T1"
                    + " WHERE  T1.SCHREGNO = ?"
                    // TODO: 以下の条件が追加と思われる。Revision1.146 で行った仕様変更に関係する。
//                    + " and T1.YEAR >= '" + param.getCountStartYear() + "'"
                    + " AND T1.YEAR < '" + param._year + "'"
                    + " AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR CLASSCD = '" + KNJDefineCode.subject_T + "'))"
                    + " OR T1.SCHOOLCD != '0')";

            return sql;
        }

        // 前年度までの未履修（必須科目）
        private void loadPreviousMirisyu(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdPreviousMirisyu(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String year = rs.getString("YEAR");
                        final String subClassCd = rs.getString("SUBCLASSCD");
                        student._previousMirisyuKamoku.put(year, subClassCd);
                    }
                    log.debug(student + " の前年度までの未履修科目=" + student._previousMirisyuKamoku);
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 前年度までの未履修（必須科目）数
         */
        private String sqlStdPreviousMirisyu(final Param param) {
            String sql;
            sql = " select t1.year, t1.subclasscd"
                    + " from SCHREG_STUDYREC_DAT T1 inner join V_SUBCLASS_MST T2"
                    + "  on  T1.YEAR = T2.YEAR";
            if ("1".equals(param._useCurriculumcd)) {
                sql+= " and T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ";
                sql+= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
            } else {
                sql +=    "  and T1.SUBCLASSCD = T2.SUBCLASSCD";
            }
            sql +=    " where  T1.SCHREGNO = ?"
                    + " and T1.YEAR >= '" + param.getCountStartYear() + "'"
                    + " and T1.YEAR < '" + param._year + "'"
                    + " and ((T1.SCHOOLCD = '0' and (CLASSCD between '" + KNJDefineCode.subject_D + "' and '" + KNJDefineCode.subject_U + "' or CLASSCD = '" + KNJDefineCode.subject_T + "'))"
                    + " or T1.SCHOOLCD != '0')"
                    + " and value(T2.ELECTDIV,'0') <> '1'"  // TODO: SUBCLASS_MST.REQUIRE_FLG との関係が曖昧
                    + " and value(T1.COMP_CREDIT,0) = 0";
            return sql;
        }

        // 前年度までの未履修（必須科目）数
        private void loadPreviousMirisyu0(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdPreviousMirisyu0(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    if (rs.next()) {
                        student._previousMirisyu = rs.getInt("COUNT");
                    }
                }
            } catch (final SQLException e) {
                log.error("SQLException", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * @return 集計対象の開始年度
         */
        private int getCountStartYear(final Param param) {
            final int grade = Integer.parseInt(param._grade);
            final int nendo = Integer.parseInt(param._year);

            int rtn;
            if (param._isChuKouIkkan && grade > 3) {
                rtn = nendo - grade + 4;    // 4年生の時の年度
            } else {
                rtn = nendo - grade + 1;    // 1年生の時の年度
            }

            log.debug("前年度までの未履修の集計範囲: " + rtn + "<= かつ <" + param._year);
            return rtn;
        }

        /**
         * 前年度までの未履修（必須科目）数
         */
        private String sqlStdPreviousMirisyu0(final Param param) {
            String sql;
            sql = " select count(*) as COUNT"
                    + " from   SCHREG_STUDYREC_DAT T1";
            if ("1".equals(param._useCurriculumcd)) {
                sql+= " inner join SUBCLASS_MST T2 on T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ";
                sql+=                               " T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD";
            } else {
                sql+= " inner join SUBCLASS_MST T2 on T1.SUBCLASSCD = T2.SUBCLASSCD";
            }
            sql+= " where  T1.SCHREGNO = ?"
                    + " and T1.YEAR >= '" + getCountStartYear(param) + "'"
                    + " and T1.YEAR < '" + param._year + "'"
                    + " and ((T1.SCHOOLCD = '0' and (T1.CLASSCD between '" + KNJDefineCode.subject_D + "' and '" + KNJDefineCode.subject_U + "' or T1.CLASSCD = '" + KNJDefineCode.subject_T + "'))"
                    + " or T1.SCHOOLCD != '0')"
                    + " and value(T2.ELECTDIV,'0') <> '1'"
                    + " and value(T1.COMP_CREDIT,0) = 0";

            return sql;
        }

        // 今年度の資格認定単位数
        private void loadQualifiedCredits(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdQualifiedCredits(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    if (rs.next()) {
                        student._qualifiedCredits = rs.getInt("CREDITS");
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 今年度の資格認定単位数
         */
        private String sqlStdQualifiedCredits(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(T1.CREDITS) AS CREDITS");
            stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("  AND T1.YEAR = '" + param._year + "'");

            return stb.toString();
        }

        // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
        private void setGradeAttendCheckMark(final Param param) {
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._isGradeGood = student.isGradeGood(param);
                student._isGradePoor = student.isGradePoor(param);
                student._isAttendPerfect = student.isAttendPerfect(param);
                student._isKekkaOver = student.isKekkaOver();
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
//                    final ScoreValue val = detail.getPatternAsses();
                    final ScoreValue val = _knjCommon.getTargetValue(detail);
                    if (null == val) continue;
                    if (!StringUtils.isNumeric(val._strScore)) continue;
                    final SubClass subClass = detail._subClass;
                    int[] arr = (int[]) map.get(subClass);
                    if (null == arr) {
                        arr = new int[2];
                        map.put(subClass, arr);
                    }
                    arr[0] += Integer.parseInt(val._strScore);
                    arr[1]++;
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
                    }
                }
            }
        }

        /**
         * 学級平均の算出
         */
        private void setHrTotal() {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int mlesson = 0;
            int present = 0;
            int absent = 0;
            int[] arrc = {0,0};  // 履修単位
            int[] arrj = {0,0};  // 授業日数
            int[] arrA = {0,0};  // 留学単位
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null != student._total) {
                    if (0 < student._total._count) {
                        totalT += student._total._total;
                        countT++;
                    }
//                    if (null != totalObj._avgBigDecimal) {
//                        totalA += totalObj._avgBigDecimal.doubleValue();
//                        countA++;
//                    }
//                    if (0< totalObj._avgcount) {
//                        totalA += totalObj._avgtotal;
//                        countA += totalObj._avgcount;
                        if (0< student._total._count) {
                        totalA += student._total._total;
                        countA += student._total._count;
                    }
                }
                final AttendInfo attend = student._attendInfo;
                if (null != attend) {
                    mlesson += attend._mLesson;
                    present += attend._present;
                    absent += attend._absent;
                    final int[] ret = Param.setMaxMin(arrj[0], arrj[1], attend._mLesson);
                    arrj[0] = ret[0];
                    arrj[1] = ret[1];
                }
                final int[] ret = Param.setMaxMin(arrc[0], arrc[1], student._compCredit);
                arrc[0] = ret[0];
                arrc[1] = ret[1];
                final int[] retA = Param.setMaxMin(arrA[0], arrA[1], student._currentAbroadCredits);
                arrA[0] = retA[0];
                arrA[1] = retA[1];
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
            if (0 < mlesson) {
                _perHrPresent = new BigDecimal((float) present / (float) mlesson * 100);
                _perHrAbsent = new BigDecimal((float) absent / (float) mlesson * 100);
            }
            if (0 < arrc[0]) {
                _HrCompCredits = arrc[0] + arrA[0] + "単位";
            }
            if (0 < arrj[0]) {
                _HrMLesson = arrj[0] + "日";
            }
        }

        /**
         * 順位の算出
         */
        private List createHrRanking() {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(student);
                final Total total = student._total;
                if (0 < total._count) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }

        int rank(final Student student) {
            if (0 >= student._total._count) {
                return -1;
            }
            return 1 + _hrRanking.indexOf(student._total);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _code.compareTo(that._code);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class TransInfo {
        private final String _date;
        private final String _name;
        private final String _str;

        TransInfo(
                final String date,
                final String name
        ) {
            _date = date;
            _name = name;
            _str = toString(this);
        }

        public String toString() {
            return _str;
        }

        private String toString(final TransInfo info) {
            if (null == info._date && null == info._name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != info._date) {
                sb.append(KNJ_EditDate.h_format_JP(info._date));
            }
            if (null != info._name) {
                sb.append(info._name);
            }
            return sb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Student implements Comparable {
        private int _gnum;  // 行番号
        private final String _code;  // 学籍番号
        private final HRInfo _hrInfo;
        private String _attendNo;
        private String _name;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo;
        private AttendSubclassInfo _attendSubclassInfo;
        private final Map _scoreDetails = new HashMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _currentAbroadCredits;  // 今年度の留学修得単位数
        private int _previousAbroadCredits; // 前年度までの留学修得単位数
        private boolean _isGradeGood;  // 成績優良者
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者
        /**
         * 前年度までの履修（必須科目）数
         * @deprecated
         */
        private int _previousRisyu = 0;

        /** 前年度までの未履修（必須科目）数. */
        private int _previousMirisyu;

        /** 生徒の年度毎の CREDIT_MST */
        private Map _creditMst = new HashMap(); // <YEAR, CreditMst...>
        /** 生徒の年度毎の未履修科目 */
        private MultiMap _previousMirisyuKamoku = new MultiHashMap(); // <YEAR, subclasscd...>

        Student(final String code, final HRInfo hrInfo, final int gnum) {
            _gnum = gnum;
            _code = code;
            _hrInfo = hrInfo;
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
            if (0 != rtn) return rtn;
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        int rank() {
            return _hrInfo.rank(this);
        }

        public String toString() {
            return _code + ":" + _name;
        }

        /**
         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
         */
        boolean isGradeGood(final Param param) {
            if (null == _total._avgBigDecimal) { return false; }
            float float1 = param._assess.floatValue();
            float float2 = _total._avgBigDecimal.setScale(1,BigDecimal.ROUND_HALF_UP).floatValue();
            if (float1 <= float2) { return true; }
            return false;
        }

        /**
         * @return 成績不振者（評定１が1つでもある）は true を戻します。
         */
        boolean isGradePoor(final Param param) {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final ScoreValue val = detail._patternAssess;
                if (null == val) continue;
                if (StringUtils.isNumeric(val._strScore) && Integer.parseInt(val._strScore) == param._hyotei){ return true; }
            }
            return false;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
         */
        boolean isAttendPerfect(final Param param) {
            if (null == _attendInfo || ! _attendInfo.isAttendPerfect()) {
                return false;
            }

            if (null == _attendSubclassInfo || ! _attendSubclassInfo.isAttendPerfect(param)) {
                return false;
            }
            return true;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        boolean isKekkaOver() {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (true == detail._isOver){ return true; }
            }
            return false;
        }

        private String getPreviousMirisyu() {
            int ans = 0;
            for (final Iterator it = _creditMst.keySet().iterator(); it.hasNext();) {
                final String year = (String) it.next();

                final Collection creditMsts = (Collection) _creditMst.get(year);
                final Collection mirisyuKamoku = (Collection) _previousMirisyuKamoku.get(year);
                if (null == mirisyuKamoku) {
                    continue;
                }

                for (final Iterator it2 = creditMsts.iterator(); it2.hasNext();) {
                    final CreditMst c = (CreditMst) it2.next();
                    if (!mirisyuKamoku.contains(c._subClassCd)) {
                        ans++;
                    }
                }
            }
            return String.valueOf(ans);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class AttendInfo {
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

    private class AttendSubclassInfo {
        private final int _lesson;
        private final int _absent;
        private final int _lateEarly;

        private AttendSubclassInfo(
                final int lesson,
                final int absent,
                final int lateEarly
        ) {
            _lesson = lesson;
            _absent = absent;
            _lateEarly = lateEarly;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        boolean isAttendPerfect(final Param param) {
            if (  param._isAttendPerfectSubclassLateEarly && _absent == 0 && _lateEarly == 0) { return true; }
            if (! param._isAttendPerfectSubclassLateEarly && _absent == 0) { return true; }
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class SubClass {
        private final String _classcode;
        private final String _classabbv;
        private final String _subclasscode;
        private final String _subclassabbv;
        private final boolean _electdiv; // 選択科目
        private int _gnum;  // 行番号
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private int _jisu;  // 授業時数
        private String _scoreaverage;  // 学級平均
        private String _scoretotal;  // 学級合計

        SubClass(
                final String subclasscode,
                final String classabbv,
                final String subclassabbv,
                final boolean electdiv,
                final int credit,
                final int jisu
        ) {
            _classcode = subclasscode.substring(0, 2);
            _classabbv = classabbv;
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _jisu = jisu;  // 授業時数
        }

//        public String toString() {
//            return _subclasscode + ":" + _abbv;
//        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 素点・評定データ。
     */
    private static class ScoreValue {
        private final String _strScore;

        ScoreValue(final String strScore) {
            _strScore = strScore;
        }

        /**
         * 生徒別科目別の素点または評定のインスタンスを作成します。
         * @param strScore 素点または評定
         * @param subclasscd 教科コード
         * @return ScoreValue。
         */
        static ScoreValue create(
                final String strScore,
                final String subclasscd,
                final KnjCommon knjcommon,
                final Param param
        ) {
            if (null == strScore) {
                return null;
            }
            final String classcd = subclasscd.substring(0, 2);
            final String classKey;
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                final String[] split = StringUtils.split(subclasscd, "-");
                classKey = split[0] + "-" + split[1];
            } else {
                classKey = classcd;
            }
            if (knjcommon._isGakunenmatu) {
                if (KNJDefineCode.subject_T.equals(classcd)) {
                    return null;
                }
                if (knjcommon._noEnableValueCd.keySet().contains(classKey)) {
                    return null;
                }
            } else {
                final D008 d008 = (D008) knjcommon._noEnableValueCd.get(classKey);
                if (null != d008) {
                    if ("1".equals(d008._namespare1)) {
                        ; // 表示する
                    } else {
                        return null;
                    }
                } else {
                    if (KNJDefineCode.subject_T.equals(classcd)) {
                        return null; // デフォルトでは表示しない
                    } else {
                        ; // デフォルトでは表示する
                    }
                }
            }
            return new ScoreValue(strScore);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 生徒別科目別データ。
     */
    private class ScoreDetail {
        private final SubClass _subClass;
        private final Double _absent;
        private final Integer _jisu;
        private final ScoreValue _score;
        private final ScoreValue _patternAssess;
        private final Integer _replaceMoto;
        private final String _printFlg;
        private final Integer _compCredit;
        private final Integer _getCredit;
        private final Integer _credits;
        private final boolean _isOver;
//        private final String _chaircd;
        private final String _calculateCreditFlg;
        /** 保健室欠課(指導) */
        private Integer _nurseoff;

        ScoreDetail(
                final SubClass subClass,
                final Double absent,
                final Integer jisu,
                final ScoreValue score,
                final ScoreValue patternAssess,
                final Integer replaceMoto,
                final String printFlg,
                final Integer compCredit,
                final Integer getCredit,
                final BigDecimal absenceHigh,
                final Integer credits,
//                final String chaircd
                final String calculateCreditFlg
        ) {
            _subClass = subClass;
            _absent = absent;
            _jisu = jisu;
            _score = score;
            _patternAssess = patternAssess;
            _replaceMoto = replaceMoto;
            _printFlg = printFlg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
            _isOver = judgeOver(absent, absenceHigh);
//            _chaircd = chaircd;
            _calculateCreditFlg = calculateCreditFlg;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.floatValue()) {
                return true;
            }
            return false;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        Integer getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        Integer getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        boolean enableCredit() {
            if (null != _replaceMoto && _replaceMoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 生徒別総合成績データ。
     */
    private class Total implements Comparable {
        private Student _student;
        private int _total;  // 総合点
        private int _count;  // 件数（成績）
        private BigDecimal _avgBigDecimal;  // 平均点

        /**
         * コンストラクタ。
         * @param student
         */
        Total(final Student student) {
            _student = student;
            compute();
        }

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数を算出します。
         */
        private void compute() {
            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            for (final Iterator it = _student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                final ScoreValue scoreValue = _knjCommon.getTargetValue(detail);
                if (isAddTotal(detail._subClass, scoreValue, detail._replaceMoto, _knjCommon)) {
                    total += Integer.parseInt(scoreValue._strScore);
                    count++;
                }

                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }

                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                }
            }

            _total = total;
            _count = count;
            if (0 < count) {
                final float avg = (float) round10(total, count);
                _avgBigDecimal = new BigDecimal(avg);
            }
            if (0 < compCredit) { _student._compCredit = compCredit; }
            if (0 < getCredit) { _student._getCredit = getCredit; }
        }

        /**
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final SubClass subClass,
                final ScoreValue scoreValue,
                final Integer replacemoto,
                final KnjCommon knjCommon
        ) {
            if (KNJDefineCode.subject_T.equals(subClass._classcode)) { return false; }
            if (null == scoreValue) { return false; }
            if (!StringUtils.isNumeric(scoreValue._strScore)) { return false; }
            if (knjCommon._isGakunenmatu && null != replacemoto && 0 < replacemoto.intValue()) {
                return false;
            }
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

    //--- 内部クラス -------------------------------------------------------

    private static class Param {
        private final String _year;
        private final String _semester;

        /** LOG-IN時の学期（現在学期）*/
        private final String _semeFlg;

        private final String _testKindCd;
        /** テスト名称テーブル */
        private final String _countFlgTable;

        /** 累計か */
        private final boolean _isRuikei;
        /** 出欠集計開始日付 */
        private final String _sDate;
        /** 出欠集計日付 */
        private final String _date;

        private final boolean _seisekifusin;
        private final boolean _creditDrop;
        private final boolean _noKetuban;
        private final Float _assess;
        private final int _hyotei;
        private final String _prgId;
        private final String _grade;

        String _semesMonth;
        String _semesterName;
        String _semesterDateS;
        String _yearDateS;
        String _divideAttendDate;

        private final String _now;

        private boolean _isChiyoda = false;
        private boolean _isChuKouIkkan = false;

        /** 単位⇒1　指導⇒2 */
        private final String _item;
        private String _itemName;

        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private final String SSEMESTER = "1";

        /** 「皆勤者」の判定基準 */
        private boolean _isAttendPerfectSubclassLateEarly;
        private boolean _isAttendPerfectSubclass90over;

        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;

        private String _paramItem;  // TAKAESU: リファクタ対象
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        private KNJ_Get_Info _getinfo;
        private String _testName;
        private Map _psMap = new HashMap();

        Param(final HttpServletRequest request, final DB2UDB db2) {

            final String item = request.getParameter("ITEM");
            _paramItem = item;  // TAKAESU: リファクタ対象

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _testKindCd = PARAM_GRAD_SEM_KIND.equals(request.getParameter("TESTKINDCD")) ? GRAD_SEM_KIND : request.getParameter("TESTKINDCD");;
            _countFlgTable = request.getParameter("COUNTFLG");

            _isRuikei = "1".equals(request.getParameter("ATTEND"));
            _sDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            // 成績不振者のみ印刷
            if (request.getParameter("SEISEKIFUSIN") != null) {
                _seisekifusin = true;
            } else {
                _seisekifusin = false;
            }

            // 単位保留
            if (request.getParameter("OUTPUT4") != null) {
                _creditDrop = true;
            } else {
                _creditDrop = false;
            }

            // 欠番を詰める
            if( request.getParameter("OUTPUT5") != null ) {
                _noKetuban = true;
            } else {
                _noKetuban = false;
            }

            //成績優良者評定平均の基準値
            if (request.getParameter("ASSESS") != null) {
                _assess = new Float(request.getParameter("ASSESS"));
            } else {
                _assess = new Float(4.3);
            }

            if (NumberUtils.isDigits(request.getParameter("HYOTEI"))) {
                _hyotei = Integer.parseInt(request.getParameter("HYOTEI"));
            } else {
                _hyotei = 1;
            }
            _prgId = request.getParameter("PRGID");
            _grade = request.getParameter("GRADE");

            _now = createNow();
            _item = item;
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _definecode = createDefineCode(db2, _year);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _getinfo = new KNJ_Get_Info();

            // 最終集計日の翌日
            final KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _divideAttendDate = obj.date;

            // 最終集計学期＋月
            final String semesMonth = KNJC053_BASE.retSemesterMonthValue(obj.month);
            _semesMonth = semesMonth;

            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = returnval.val1;

            // 学期期間FROM
            if (null == returnval.val2) {
                _semesterDateS = _year + "-04-01";
            } else {
                _semesterDateS = returnval.val2;
            }

            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, SEMEALL);
            _yearDateS = returnval1.val2;
            _testName = getTestName(db2);

            // 名称マスタ Z010 の情報を取得
            setZ010(db2);
            setC001(db2);
            setD050(db2);
            loadAttendSemesArgument(db2);
        }

        private void closeQuietly() {
            for (final Iterator it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2,
                final String year
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();

            definecode.defineCode(db2, year);   //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

            return definecode;
        }

        private String createNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();

            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));

            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        public void setC001(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _itemName = "";

            final String sql = "SELECT name2"
                + " FROM name_mst"
                + " WHERE namecd1='C001'"
                + " AND namecd2='14'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while(rs.next()) {
                    _itemName = StringUtils.defaultString(rs.getString("NAME2"));
                }
            } catch (final SQLException ex) {
                log.error("右記のSQLでエラー:" + sql, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql = "SELECT name1, namespare2"
                + " FROM v_name_mst"
                + " WHERE year='" + _year + "'"
                + " AND namecd1='Z010'"
                + " AND namecd2='00'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                rs.next();
                final String name1 = rs.getString("NAME1");
                final String spare2 = rs.getString("NAMESPARE2");
                if (CHIYODA.equals(name1)) {
                    _isChiyoda = true;
                }
                _isChuKouIkkan = "1".equals(spare2);
                log.debug("中高一貫か？:" + _isChuKouIkkan);
            } catch (final Exception ex) {
                log.error("右記のSQLでエラー:" + sql, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 「皆勤者」の判定基準
         * NAMESPARE1・・・Y:皆勤者かどうかを判断する際に「授業の遅刻・早退」もチェックする
         * NAMESPARE2・・・Y:皆勤者かどうかを判断する際に「教科コードが90より大きい科目」の出欠情報もチェックする
         */
        private void setD050(final DB2UDB db2) {
            _isAttendPerfectSubclassLateEarly = false;
            _isAttendPerfectSubclass90over = false;
            ResultSet rs = null;
            try {
                db2.query("SELECT NAMESPARE1,NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'D050' AND NAMECD2 = '01'");
                rs = db2.getResultSet();
                if (rs.next()) {
                    _isAttendPerfectSubclassLateEarly = "Y".equals(rs.getString("NAMESPARE1"));
                    _isAttendPerfectSubclass90over = "Y".equals(rs.getString("NAMESPARE2"));
                }
                log.debug("皆勤者の判定基準：LateEarly = " + _isAttendPerfectSubclassLateEarly + ", 90over = " + _isAttendPerfectSubclass90over);
            } catch (final Exception e) {
                log.warn("「皆勤者」の判定基準の取得失敗", e);
            } finally{
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
        }

        /**
         * @return 集計対象の開始年度
         */
        private int getCountStartYear() {
            final int grade = Integer.parseInt(_grade);
            final int nendo = Integer.parseInt(_year);

            int rtn;
            if (_isChuKouIkkan && grade > 3) {
                rtn = nendo - grade + 4;    // 4年生の時の年度
            } else {
                rtn = nendo - grade + 1;    // 1年生の時の年度
            }

            log.debug("前年度までの未履修の集計範囲: " + rtn + "<= かつ <" + _year);
            return rtn;
        }

        /**
         * 指定年度の時の学年を得る。
         * @param year 指定年度
         * @return 指定年度の時の学年
         */
        public int getGrade(final String year) {
            final int currentYear = Integer.parseInt(_year);
            final int currentGrade = Integer.parseInt(_grade);

            final int sa = Integer.parseInt(year) - currentYear;
            return currentGrade + sa;
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        public void loadAttendSemesArgument(DB2UDB db2) {
            try {
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 指定開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();

                log.debug(" sDate = " + _sDate);
                log.debug(" attendSemesMap = " + _attendSemesMap);
                log.debug(" hasuuMap = " + _hasuuMap);
                log.debug(" semesFlg = " + _semesFlg);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        private boolean doPrintCreditDrop(final KnjCommon knjcommon) {
            if (knjcommon instanceof KnjD062A_Inter) {
                return false;
            }
            return _creditDrop;
        }

        private DecimalFormat getAbsentFmt() {
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                return DEC_FMT2;
            default:
                return DEC_FMT1;
            }
        }

        private String getTestName(final DB2UDB db2) {
            String testName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = KNJ_Testname.getTestNameSql(_countFlgTable, _year, _semester, _testKindCd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    testName = rs.getString("TESTITEMNAME");
                }
            } catch (final SQLException e) {
                log.error("テスト名称の読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return testName;
        }

        private static int[] setMaxMin(
                int maxInt,
                int minInt,
                final int tergetInt
        ) {
            if (0 < tergetInt) {
                if (maxInt < tergetInt){ maxInt = tergetInt; }
                if (0 == minInt) {
                    minInt = tergetInt;
                } else {
                    if (minInt > tergetInt){ minInt = tergetInt; }
                }
            }
            return new int[] {maxInt, minInt};
        }
    } // Param

    private static class CreditMst {
        private final String _year;
        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final String _grade;
        private final String _subClassCd;

        public CreditMst(
                final String year,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String grade,
                final String subClassCd
        ) {
            _year = year;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _grade = grade;
            _subClassCd = subClassCd;
        }

        static List load(final DB2UDB db2, final Param param) throws SQLException {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlCreditMst(param.getCountStartYear(), param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String grade = rs.getString("GRADE");
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    if (param.getGrade(year) != Integer.parseInt(grade)) {
                        continue;   // 例えば 2007年に3年生なら、2006年は2年生のみ必要
                    }

                    final CreditMst creditMst = new CreditMst(
                            year,
                            courseCd,
                            majorCd,
                            courseCode,
                            grade,
                            subClassCd
                    );
                    log.debug("単位マスタ:年度=" + creditMst._year + ", コース情報=" + creditMst.courseInfo() + ", 学年=" + creditMst._grade + ", 科目コード=" + creditMst._subClassCd);
                    rtn.add(creditMst);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlCreditMst(final int startYear, final Param param) {
            String sql;
            sql = "SELECT"
                + "  year,"
                + "  coursecd,"
                + "  majorcd,"
                + "  coursecode,"
                + "  grade,";
            if ("1".equals(param._useCurriculumcd)) {
                sql +="  classcd || '-' || school_kind || '-' || curriculum_cd || '-' || ";
            }
            sql +="  subclasscd AS subclasscd"
                + " FROM"
                + "  credit_mst"
                + " WHERE"
                + "  require_flg='1' and";
            if ("1".equals(param._useCurriculumcd)) {
                sql +="  int(CLASSCD) < 89 and";
            } else {
                sql +="  int(substr(SUBCLASSCD, 1, 2)) < 89 and";
            }
            sql +=" year >= '" + startYear + "'"
                ;
            return sql;
        }

        private String courseInfo() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String toString() {
            return _year + "/" + courseInfo() + "/" + _grade + "/" + _subClassCd;
        }
    }
}
