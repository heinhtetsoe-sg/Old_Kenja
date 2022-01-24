// kanji=漢字
/*
 * $Id: 14d0a693b5d54bfb4ded4311fc91acd14de8ce32 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @author nakamoto
 * @version $Id: 14d0a693b5d54bfb4ded4311fc91acd14de8ce32 $
 */
public class KNJD610 {
    private static final Log log = LogFactory.getLog(KNJD610.class);

    private static final int MAX_COLUMN = 19;
    private static final int MAX_LINE = 50;
    private static final String FROM_TO_MARK = "\uFF5E";

    private String FORM_FILE;
    private KNJD065_COMMON _knjdobj;  //成績別処理のクラス
    
    private final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private final DecimalFormat DEC_FMT2 = new DecimalFormat("0");

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alpWrap svf = null;
        boolean hasData = false;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            final Param paramap = createParamap(request, db2);

            try {
                _knjdobj = createKnjd065Obj(db2,paramap);  //成績別処理クラスを設定するメソッド
            } catch (Exception e) {
                log.error("Exception", e);
            }
            log.fatal(_knjdobj);

            svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.setKurikaeshiNum(MAX_COLUMN);
            svf.setFieldNum(MAX_LINE);
            sd.setSvfInit(request, response, svf);

            final String[] hrclass = request.getParameterValues("CLASS_SELECTED");  //印刷対象HR組
            for (int h = 0; h < hrclass.length; h++) {
                paramap.HRCLASS = hrclass[h];  //HR組

                if (null != paramap.OUTPUT_COURSE) {
                    final List courses = Course.createCourses(db2, paramap);
                    log.debug("コース数=" + courses.size());
                    
                    for (final Iterator it = courses.iterator(); it.hasNext();) {
                        final Course course = (Course) it.next();
                        paramap.COURSECD = course._coursecd;
                        
                        final HRInfo hrInfo = query(db2, paramap, hrclass[h]);
                        
                        // 印刷処理
                        if (printMain(svf, paramap, hrInfo)) {
                            hasData = true;
                        }
                    }
                } else {
                    final HRInfo hrInfo = query(db2, paramap, hrclass[h]);
                    
                    // 印刷処理
                    if (printMain(svf, paramap, hrInfo)) {
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
        
        private static List createCourses(final DB2UDB db2, final Param paramap) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCourses(paramap);

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

        private static String sqlCourses(final Param paramap) {
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
            stb.append("     W1.YEAR = '" + paramap.YEAR + "' AND ");
            if (!paramap.SEMESTER.equals("9")) {
                stb.append(" W1.SEMESTER = '" + paramap.SEMESTER + "' AND ");
            } else {
                stb.append(" W1.SEMESTER = '" + paramap.SEME_FLG + "' AND ");
            }
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + paramap.HRCLASS + "' ");
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

    private HRInfo query(
            final DB2UDB db2,
            final Param paramap,
            final String hrclass
    ) throws Exception {
        final HRInfo hrInfo = new HRInfo(paramap.HRCLASS);
        hrInfo.load(db2, paramap);
        return hrInfo;
    }

    /**
     *  get parameter doGet()パラメータ受け取り
     *       2005/01/05 最終更新日付を追加(param[15])
     *       2005/05/22 学年・組を配列で受け取る
     *       2007/05/14 総合順位出力を追加
     */
    private Param createParamap(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param paramap = new Param(request, db2);
        return paramap;
    }

    /**
     *  成績別処理クラス設定
     */
    private KNJD065_COMMON createKnjd065Obj(
            final DB2UDB db2,
            final Param paramap
    ) throws Exception {
        final String prgid = paramap.PRGID;
        if ("KNJD065".equals(prgid)) {
            FORM_FILE = "KNJD065.frm";
            // 成績会議判定資料　学期
            if (!paramap.SEMESTER.equals("9")) { return new KNJD065_GAKKI(db2,paramap); }
            // 成績会議判定資料　学年
            return  new KNJD065_GRADE(db2,paramap);
        }

//        FORM_FILE = "KNJD062.frm";
        FORM_FILE = "KNJD610.frm";
        final String testKindCd = paramap.TESTKINDCD;
        log.debug(" testKindCd = " + testKindCd + " , isChiben = " + paramap._isChiben);

        // 成績一覧表(KNJD062) 中間
        if ("0101".equals(testKindCd)) { return new KNJD062A_INTER(db2,paramap); }

        // 成績一覧表(KNJD062) 期末1 or 期末2
        if ("0201".equals(testKindCd) || "0202".equals(testKindCd)) { return new KNJD062A_TERM(db2,paramap); }

        // 成績一覧表(KNJD062) 学期
        if (!paramap.SEMESTER.equals("9")) { return new KNJD062A_GAKKI(db2,paramap); }

        // 成績一覧表(KNJD062) 学年(智辯・評価)
        if (!paramap._isChiben9909 && "9900".equals(testKindCd) && paramap._isChiben) { return new KNJD062A_GRADE_HYOKA(db2,paramap); }
        
        // 成績一覧表(KNJD062) 学年(評定)
        return  new KNJD062A_GRADE(db2,paramap);
    }

    /**
     * 学級ごとの印刷。
     * @param svf
     * @param paramap
     * @param hrInfo
     * @return
     */
    private boolean printMain(
            final Vrw32alpWrap svf,
            final Param paramap,
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

            int nowpage = (int)Math.floor((double)student._gnum / (double)MAX_LINE);
            if (page != nowpage) {
                page = nowpage;
                List list = hrInfo._students.subList(first, last + 1);
                first = last;
                pageFlg = false;

                if (printSub(svf, paramap, hrInfo, list)) hasData = true;
            }
        }
        if (pageFlg && 0 <= last) {
            List list = hrInfo._students.subList(first, last + 1);

            if (printSub(svf, paramap, hrInfo, list)) hasData = true;
        }
        
        return hasData;
    }
    
    /**
     * 学級ごと、生徒MAX行ごとの印刷。
     * @param svf
     * @param paramap
     * @param hrInfo：年組
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private boolean printSub(
            final Vrw32alpWrap svf,
            final Param paramap,
            final HRInfo hrInfo,
            final List stulist
    ) {
        boolean hasData = false;

        int line = 0;  // 科目の列番号
        final int subclassesnum = hrInfo._subclasses.size();

        final Collection subClasses = hrInfo._subclasses.values();
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            if (0 == line % MAX_COLUMN) {
                // ヘッダー出力項目を設定および印字
                svf.VrSetForm(FORM_FILE, 4);  //SVF-FORM設定
                _knjdobj.printHeader(svf, hrInfo, paramap);
                _knjdobj.printHeaderOther(svf,paramap);
                // 生徒名等を印字
                for (final Iterator sit = stulist.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();
                    svf.doNumberingSvfOut("name", student._gnum, student._name);    // 氏名
                    svf.doNumberingSvfOutn("NUMBER", student._gnum, 0, DEC_FMT2.format(Integer.parseInt(student._attendNo)));  // 出席番号
                    svf.doNumberingSvfOut("REMARK", student._gnum, student._transInfo.toString());  // 備考
                    if (_knjdobj.hasJudgementItem()) {
                        // 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
                        if (student._isGradePoor) { svf.doNumberingSvfOutn("CHECK1", student._gnum, 0, "★"); }
                        else if (student._isGradeGood) { svf.doNumberingSvfOutn("CHECK1", student._gnum, 0, "☆"); } 
                        if (student._isAttendPerfect) { svf.doNumberingSvfOutn("CHECK2", student._gnum, 0, "○"); }
                        else if (student._isKekkaOver) { svf.doNumberingSvfOutn("CHECK2", student._gnum, 0, "●"); } 
                    }
                }
            }
            if (subclassesnum == line + 1) { 
                for (final Iterator sit = stulist.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();
                    // 生徒別総合点・平均点・順位を印刷します。
                    if (null != student._scoreSum) {
                        svf.doNumberingSvfOut("TOTAL", student._gnum, student._scoreSum);  //総合点
                        svf.doNumberingSvfOut("AVERAGE", student._gnum, student._scoreAvg);  //平均点
                    }

                    if (1 <= student._rank) {
                        svf.doNumberingSvfOut("RANK", student._gnum, String.valueOf(student._rank));  //順位
                    }

                    if (null != student._attendInfo) {
                        svf.doNumberingSvfOutNonZero("PRESENT", student._gnum, student._attendInfo._mLesson);      // 出席すべき日数
                        svf.doNumberingSvfOutNonZero("SUSPEND", student._gnum, student._attendInfo._suspend);      // 出席停止
                        svf.doNumberingSvfOutNonZero("KIBIKI", student._gnum,  student._attendInfo._mourning);     // 忌引
                        svf.doNumberingSvfOutNonZero("ABSENCE", student._gnum, student._attendInfo._absent);       // 欠席日数
                        svf.doNumberingSvfOutNonZero("ATTEND", student._gnum,  student._attendInfo._present);      // 出席日数
                        svf.doNumberingSvfOutNonZero("TOTAL_LATE", student._gnum, student._attendInfo._late);      // 遅刻回数
                        svf.doNumberingSvfOutNonZero("LEAVE", student._gnum,   student._attendInfo._early);        // 早退回数
                        svf.doNumberingSvfOutNonZero("ABROAD", student._gnum,  student._attendInfo._transDays);    // 留学等実績
                    }

                    // 最後のページの印字処理（成績総合/出欠の記録）
                    if (_knjdobj.hasCompCredit()) {
                        //今年度履修単位数
                        svf.doNumberingSvfOutNonZero("R_CREDIT", student._gnum, String.valueOf(student._compCredit));
                        //今年度修得単位数
                        svf.doNumberingSvfOutNonZero("C_CREDIT", student._gnum, String.valueOf(student._getCredit));
                    }
                    if (_knjdobj.hasJudgementItem()) {
                        // 前年度までの単位数を印字
                        // 今年度認定単位数
                        svf.doNumberingSvfOutNonZero("A_CREDIT", student._gnum, String.valueOf(student._qualifiedCredits));
                        // 前年度までの修得単位数
                        svf.doNumberingSvfOutNonZero("PRE_C_CREDIT", student._gnum, String.valueOf(student._previousCredits));
                        // 修得単位数計
                        int t = student._getCredit + student._qualifiedCredits + student._previousCredits;
                        if (t != 0) {
                            int g = _knjdobj._gradCredits;  // 卒業認定単位数
                            if (g != 0 && g <= t) {
                                svf.doNumberingSvfOut("TOTAL_C_CREDIT", student._gnum, "@" + String.valueOf(t));
                            } else {
                                svf.doNumberingSvfOut("TOTAL_C_CREDIT", student._gnum, String.valueOf(t));
                            }
                        }
                        // 前年度までの未履修科目数
                        svf.doNumberingSvfOutNonZero("PRE_N_CREDIT", student._gnum, String.valueOf(student._previousMirisyu));
                    }
                }
                // 学級データの印字処理(学級平均)
                if (null != hrInfo._avgHrTotal) {
                    svf.VrsOut("TOTAL51", hrInfo._avgHrTotal);
                }
                if (null != hrInfo._avgHrAverage) {
                    svf.VrsOut("AVERAGE51", hrInfo._avgHrAverage);
                }
                if (null != hrInfo._perHrPresent) {
                    svf.VrsOut("PER_ATTEND", DEC_FMT1.format(hrInfo._perHrPresent.setScale(1,BigDecimal.ROUND_HALF_UP)));
                }
                if (null != hrInfo._perHrAbsent) {
                    svf.VrsOut("PER_ABSENCE", DEC_FMT1.format(hrInfo._perHrAbsent.setScale(1,BigDecimal.ROUND_HALF_UP)));
                }
            }  // 生徒別総合成績および出欠を印字

            final SubClass subclass = (SubClass) it.next();
            final int used = printSubclasses(svf, subclass, line, stulist);
            line += used;
            if (0 < used) { hasData = true; }
        }

        return hasData;
    }

    /**
     * 該当科目名および科目別成績等を印字する処理
     * @param svf
     * @param subclass
     * @param line：科目の列番号
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private int printSubclasses(
            final Vrw32alpWrap svf,
            final SubClass subclass,
            final int line,
            final List stulist
    ) {
        printSubclass(svf, subclass, line);  // 該当科目名等を印字
        // 生徒別該当科目成績を印字する処理
        for (final Iterator sit = stulist.iterator(); sit.hasNext();) {
            final Student student = (Student) sit.next();
            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                printScoreDetail(svf, detail, line, student._gnum);
            }
        }        
        svf.VrEndRecord();
        return 1;
    }
    
    /**
     * 生徒別科目別素点・評定・欠課時数等を印刷します。
     * @param svf
     * @param line 科目の列番
     * @param gnum 生徒の行番
     */
    private void printScoreDetail(
            final Vrw32alpWrap svf,
            final ScoreDetail detail,
            final int line,
            final int gnum
    ) {
        int column = line % MAX_COLUMN + 1;
        // 素点・単位
        if (_knjdobj.isPrintDetailTani()) {
            printTani(svf, detail, gnum, column);
        } else {
            printSoten(svf, detail, gnum, column);
        }
        
        // 成績
        if (null != detail._patternAssess) {
            if (_knjdobj.doPrintCreditDrop() && 1 == detail._patternAssess.getScoreAsInt()) {
                svf.VrsOut("late"+ gnum,  "*" + detail._patternAssess.getScore());
            } else {
                svf.VrsOut("late"+ gnum,  detail._patternAssess.getScore());
            }
        }

        // 欠課
        if (null != detail._absent) {
            final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
            final String field = _knjdobj.getSvfFieldNameKekka();
            if (0 != value) {
                if (detail._isOver) {
                    svf.VrAttribute(field+ gnum,  "Paint=(2,70,1),Bold=1");
                }
                svf.VrsOut(field+ gnum,  _knjdobj._absentFmt.format(detail._absent.floatValue()));
                if (detail._isOver) {
                    svf.VrAttribute(field+ gnum,  "Paint=(0,0,0),Bold=0");
                }
            }
        }
    }

    /**
     * 素点を印刷します。
     * @param svf
     * @param gnum 印刷する行番号
     * @param column 印刷する列番号
     */
    private void printSoten(
            final Vrw32alpWrap svf, 
            final ScoreDetail detail,
            final int gnum, 
            final int column
    ) {
        if (null == detail._score) { return; }
        if (null != detail._score_flg) {
            svf.VrAttribute("rate"+ gnum,  "Paint=(1,50,1),Bold=1");
        }
        svf.VrsOut("rate"+ gnum,  detail._score.getScore() );
        if (null != detail._score_flg) {
            svf.VrAttribute("rate"+ gnum,  "Paint=(0,0,0),Bold=0");
        }
    }

    /**
     * 単位を印刷します。
     * @param svf
     * @param gnum 印刷する行番号
     * @param column 印刷する列番号
     */
    private void printTani(
        final Vrw32alpWrap svf, 
        final ScoreDetail detail,
        final int gnum, 
        final int column
    ) {
        Integer credit;
        if (_knjdobj.isGakunenMatu()) {
            credit = detail._getCredit;
        } else {
            credit = detail._credits;
        }
        if (null == credit) { return; }
        if (null != detail._replacemoto && 1 <= detail._replacemoto.intValue()) {
            svf.VrsOut("rate"+ gnum,  "(" + credit.toString() + ")");
        } else {
            svf.VrAttribute("rate"+ gnum,  "Hensyu=3");
            svf.VrsOut("rate"+ gnum,  credit.toString());
        }
    }
    
    /**
     * 科目項目(教科名・科目名・)を印刷します。
     * @param svf
     * @param line 科目の列番
     */
    private void printSubclass(
            final Vrw32alpWrap svf,
            final SubClass subclass, 
            int line
    ) {
        int i = ((line + 1) % MAX_COLUMN == 0)? MAX_COLUMN: (line + 1) % MAX_COLUMN;
if (log.isDebugEnabled()) { log.debug("subclassname="+subclass._subclasscode + " " + subclass._subclassabbv+"   line="+line + "  i="+i); }
        //教科名
        svf.VrsOut("course1", subclass._classabbv);
        //科目名
        if (subclass._electdiv) svf.VrAttribute("subject1", "Paint=(2,70,2),Bold=1");
        svf.VrsOut("subject1", subclass._subclassabbv);
        if (subclass._electdiv) svf.VrAttribute("subject1", "Paint=(0,0,0),Bold=0");
        //単位数
        if (0 != subclass._maxcredit) {
            if (subclass._maxcredit == subclass._mincredit) { svf.VrsOut("credit1",  String.valueOf(subclass._maxcredit)); }
            else  { svf.VrsOut("credit1",  String.valueOf(subclass._mincredit) + " \uFF5E " + String.valueOf(subclass._maxcredit)); }
        }
        //授業時数
        if (0 != subclass._jisu) { svf.VrsOut("lesson1",  String.valueOf(subclass._jisu)); }
        //学級平均・合計
        if (null != subclass._scoreaverage) { svf.VrsOut("AVE_CLASS",  subclass._scoreaverage); }
        if (null != subclass._scoretotal) { svf.VrsOut("TOTAL_SUBCLASS",  subclass._scoretotal + "/" + subclass._scoreCount); }
        //項目名
        svf.VrsOut("ITEM1", _knjdobj._item1Name);
        svf.VrsOut("ITEM2", _knjdobj._item2Name);
        svf.VrsOut("ITEM3", _knjdobj._item3Name);
    }
    
    /*
     *  PrepareStatement作成 --> 科目別平均の表
     */
//    private String sqlSubclassAverage(final Param paramap) {
//        final StringBuffer stb = new StringBuffer();
//
//        stb.append(" SELECT  SUBCLASSCD");
//        stb.append("        ," + _knjdobj._fieldChaircd + " AS CHAIRCD");
//        stb.append("        ,ROUND(AVG(FLOAT(" + _knjdobj._fieldname + "))*10,0)/10 AS AVG_SCORE");
//        stb.append(" FROM RECORD_DAT W1");
//        stb.append(" WHERE YEAR = '" + paramap.YEAR + "'");
//        stb.append(" GROUP BY SUBCLASSCD");
//        stb.append("," + _knjdobj._fieldChaircd);
//        stb.append(" HAVING " + _knjdobj._fieldChaircd +" IS NOT NULL");
//
//        return stb.toString();
//    }

    private static double round10(final int a, final int b) {
        return Math.round(a * 10.0 / b) / 10.0;
    }

    protected String getRecordTableName(final String prgid) {
        return "KNJD062B".equals(prgid) ? "V_RECORD_SCORE_DAT" : "RECORD_DAT";
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる処理クラス
     */
    private abstract class KNJD065_COMMON {
        String _fieldname;
        String _fieldname2;
        String _svfFieldNameKekka;  // 欠課時数フィールド名は、欠課時数算定コードにより異なる。
        DecimalFormat _absentFmt;
//        String _fieldChaircd;
        protected final boolean _creditDrop;
        protected final String _semesterName;
        protected final String _semester;
        protected final String _testKindCd;
        protected String _testName;
        protected int _gradCredits;  // 卒業認定単位数
        protected String _item1Name;  // 明細項目名
        protected String _item2Name;  // 明細項目名
        protected String _item3Name;  // 明細項目名
        protected String _item4Name;  // 総合点欄項目名
        protected String _item5Name;  // 平均点欄項目名
        protected String _item7Name;  // 順位欄項目名

        public KNJD065_COMMON(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            _creditDrop = "1".equals(paramap.CREDIT_DROP);
            _testKindCd = paramap.TESTKINDCD;
            _semester = paramap.SEMESTER;
            _semesterName = paramap.SEMESTERNAME;
            _gradCredits = getGradCredits(db2, paramap);
            setSvfFieldNameKekka(paramap);
            setAbsentFmt(paramap);
            _item7Name = paramap.RANKNAME;
        }

        String getSvfFieldNameKekka() {
            return _svfFieldNameKekka;
        }

        void setSvfFieldNameKekka(final Param paramap) {
            int absent_cov = paramap._defineSchool.absent_cov;
            if (absent_cov == 3 || absent_cov == 4) {
                _svfFieldNameKekka = "kekka2_";
            } else {
                _svfFieldNameKekka = "kekka";
            }
        }

        void setAbsentFmt(final Param paramap) {
            switch (paramap._defineSchool.absent_cov) {
            case 0:
            case 1:
            case 2:
                _absentFmt = new DecimalFormat("0");    // TODO: グローバルインスタンスを使えないか?
                break;
            default:
                _absentFmt = new DecimalFormat("0.0");
            }
        }

        boolean doPrintCreditDrop() {
            return _creditDrop;
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d._patternAssess;
        }

        /**
         * (帳票種別によって異なる)ページ見出し・項目・欄外記述を印刷します。
         * @param svf
         * @param paramap
         */
        abstract void printHeaderOther(Vrw32alpWrap svf,Param paramap);

        /**
         * ページ見出し・項目・欄外記述を印刷します。
         * @param svf
         * @param hrInfo
         * @param paramap
         */
        void printHeader(
                final Vrw32alpWrap svf,
                final HRInfo hrInfo,
                final Param paramap
        ) {
            svf.VrsOut("year2", paramap.NENDO);
            svf.VrsOut("ymd1", paramap.NOW); // 作成日
            svf.VrsOut("DATE", paramap.TERM_KEKKA);  // 欠課の集計範囲
            svf.VrsOut("DATE2", paramap.TERM_ATTEND);  // 「出欠の記録」の集計範囲

            svf.VrsOut("teacher", hrInfo._staffName);  //担任名
            svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
            if (hasCompCredit()) {
                svf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
            }
            svf.VrsOut("lesson20", hrInfo._HrMLesson);  // 授業日数

            svf.VrsOut("ITEM4", _item4Name);
            svf.VrsOut("ITEM5", _item5Name);
            svf.VrsOut("ITEM6", _item1Name + "・" + _item2Name + "・" + _item3Name);
            svf.VrsOut("ITEM7", _item7Name);

            // 一覧表枠外の文言
            svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );
            if (paramap.TESTKINDCD.equals("0101") || paramap.TESTKINDCD.equals("0201") || paramap.TESTKINDCD.equals("0202")) {
                svf.VrAttribute("NOTE3",  "Paint=(1,50,1),Bold=1");
                svf.VrsOut("NOTE3",  " " );
                svf.VrsOut("NOTE4",  "：追試・見込点、再試験点" );
            }
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }
        
        boolean hasJudgementItem() {
            // 判定会議資料用の項目なし
            return false;
        }

        /** {@inheritDoc} */
        public String toString() {
            return getClass().getName() + " : semesterName=" + _semesterName + ", testName=" + _testName;
        }

        // 卒業認定単位数の取得
        private int getGradCredits(
                final DB2UDB db2,
                final Param paramap
        ) {
            int gradcredits = 0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlGradCredits(paramap);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    gradcredits = rs.getInt("GRAD_CREDITS");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradcredits;
        }
        
        /**
         * 卒業認定単位数
         * @param paramap
         * @return
         */
        private String sqlGradCredits(final Param paramap) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + paramap.YEAR + "'");
            return stb.toString();
        }
        
        /**
         * @return 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
         */
        boolean enablePringFlg() {
            return false;
        }
        
        /**
         * @return 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
         */
        boolean isPrintDetailTani() {
            return false;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }

        protected void vrsOutMarkPrgid(Vrw32alpWrap svf, Param paramap) {
            svf.VrsOut("MARK", "/");
            svf.VrsOut("PRGID", paramap.PRGID);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class KNJD065_GRADE extends KNJD065_COMMON {
        public KNJD065_GRADE(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "GRAD_VALUE";
            _fieldname2 = null;
            _testName = "(評定)";
            _item1Name = "単位";
            _item2Name = "評定";
            _item3Name = "欠課";
            _item4Name = "評定合計";
            _item5Name = "評定平均";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE", _semesterName + "成績一覧表"); //成績名称
            svf.VrsOut("MARK1_2",  ((Float)paramap.ASSESS).toString());
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }
        
        boolean hasJudgementItem() {
            // 判定会議資料用の項目あり
            return true;
        }
        
        /**
         * @return 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
         */
        boolean enablePringFlg() {
            return false;
        }

        /**
         * @return 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
         */
        boolean isPrintDetailTani() {
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return true;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class KNJD065_GAKKI extends KNJD065_GRADE {
        public KNJD065_GAKKI(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "SEM" + _semester + "_VALUE";
            _fieldname2 = null;
            _item2Name = "評価";
            _item4Name = "評価合計";
            _item5Name = "評価平均";
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数なし
            return true;
        }
        
        boolean hasJudgementItem() {
            // 判定会議資料用の項目あり
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class KNJD062A_GRADE extends KNJD065_COMMON {
        public KNJD062A_GRADE(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "GRAD_VALUE";
            _fieldname2 = null;
            _testName = paramap.TESTITEMNAME;
            _item1Name = "単位";
            _item2Name = "評定";
            _item3Name = "欠課";
            _item4Name = "評定総合点";
            _item5Name = "評定平均点";
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE",  "  成績一覧表（評定）");
            vrsOutMarkPrgid(svf, paramap);
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d._patternAssess;
        }

        boolean isPrintAverage() {
            return false;
        }

        void loadAverage(final DB2UDB db2, final Param paramap) {}
        
        /**
         * @return 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
         */
        boolean enablePringFlg() {
            return false;
        }

        /**
         * @return 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
         */
        boolean isPrintDetailTani() {
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return true;
        }
    }

    /**
     * 学年成績の処理クラス
     */
    private class KNJD062A_GRADE_HYOKA extends KNJD062A_GRADE {
        public KNJD062A_GRADE_HYOKA(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            
            _item2Name = "評価";
            _item4Name = "評価総合点";
            _item5Name = "評価平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE",  "  成績一覧表（評価）");
            vrsOutMarkPrgid(svf, paramap);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class KNJD062A_GAKKI extends KNJD062A_GRADE {
        public KNJD062A_GAKKI(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "SEM" + _semester + "_VALUE";
            _fieldname2 = null;
            _item2Name = "評価";
            _item4Name = "評価総合点";
            _item5Name = "評価平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE" , _semesterName + " 成績一覧表");
            vrsOutMarkPrgid(svf, paramap);
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }
        
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 中間試験成績の処理クラス
     */
    private class KNJD062A_INTER extends KNJD065_COMMON {

        KNJD062A_INTER(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname  = "SEM" + _semester + "_INTR_SCORE";
            _fieldname2 = "SEM" + _semester + "_INTR";
            _testName = paramap.TESTITEMNAME;
            _item1Name = "素点";
            _item2Name = "評価";
            _item3Name = "欠課";
            _item4Name = "総合点";
            _item5Name = "平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE" , _semesterName + " " + _testName + " 成績一覧表");

            vrsOutMarkPrgid(svf, paramap);
        }

        private String getTitle(final Param paramap) {
            final String one234;
            final String testKindCd = paramap.TESTKINDCD;

            if ("1".equals(_semester)) {
                one234 = testKindCd.startsWith("01") ? "１" : "２";
            } else {
                one234 = testKindCd.startsWith("01") ? "３" : "４";
            }

            return one234 + "学期期末";
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d._score;
        }

        boolean isPrintAverage() {
            return true;
        }

//        void loadAverage(final DB2UDB db2, final Param paramap) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                final String sql = sqlSubclassAverage(paramap);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String chaircd = rs.getString("CHAIRCD");
//                    final Double avgScore = (Double) rs.getObject("AVG_SCORE");
//
//                    if (null != chaircd && null != avgScore) {
//                        addAverage(chaircd, avgScore);
//                    }
//                }
//            } catch (final Exception ex) {
//                log.error("error! ", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//
//        }

        boolean doPrintCreditDrop() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 期末試験成績の処理クラス
     */
    private class KNJD062A_TERM extends KNJD062A_INTER {
        KNJD062A_TERM(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            if (_testKindCd.equals("0201")) {
                _fieldname  = "SEM" + _semester + "_TERM_SCORE";
                _fieldname2 = "SEM" + _semester + "_TERM";
//                _testName = "期末";
            } else {
                _fieldname  = "SEM" + _semester + "_TERM2_SCORE";
                _fieldname2 = "SEM" + _semester + "_TERM2";
//                _testName = "期末２";
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private class HRInfo implements Comparable {
        private final String _code;
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

        HRInfo(
                final String code
        ) {
            _code = code;
        }

        void load(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            loadHRClassStaff(db2, paramap);
            loadStudents(db2, paramap);
            loadStudentsInfo(db2, paramap);
            loadAttend(db2, paramap);
            loadHrclassAverage(db2, paramap);
            loadRank(db2, paramap);
            loadScoreDetail(db2, paramap);
            _ranking = createRanking();
            log.debug("RANK:" + _ranking);
            setSubclassAverage(this);        
            setHrTotal(this, paramap);  // 学級平均等の算出
            if (_knjdobj.hasJudgementItem()) {
                loadPreviousCredits(db2, paramap);  // 前年度までの修得単位数取得
                loadPreviousMirisyu(db2, paramap);  // 前年度までの未履修（必須科目）数
                loadQualifiedCredits(db2, paramap);  // 今年度の資格認定単位数
                setGradeAttendCheckMark(paramap);  // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
            }
        }

        private void loadHRClassStaff(
                final DB2UDB db2,
                final Param paramap
        ) {
            final KNJ_Get_Info.ReturnVal returnval = paramap._getinfo.Hrclass_Staff(
                    db2,
                    paramap.YEAR,
                    paramap.SEMESTER,
                    paramap.HRCLASS,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private void loadStudents(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassStdList(paramap);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
                    if (null != paramap.NO_KETUBAN){
                        gnum++;
                    } else {
                        gnum = rs.getInt("ATTENDNO");
                    }
                    final Student student = new Student(rs.getString("SCHREGNO"), this, gnum);
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
        private String sqlHrclassStdList(final Param paramap) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + paramap.YEAR + "' ");
            if (!paramap.SEMESTER.equals("9")) {
                stb.append(    "AND W1.SEMESTER = '" + paramap.SEMESTER + "' ");
            } else {
                stb.append(    "AND W1.SEMESTER = '" + paramap.SEME_FLG + "' ");
            }
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = '" + paramap.HRCLASS + "' ");
            if (null != paramap.OUTPUT_COURSE) {
                stb.append(    "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + paramap.COURSECD + "' ");
            }
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdNameInfo(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            TransInfo transInfo = null;
                            try {
                                final String d1 = rs.getString("KBN_DATE1");
                                final String d2 = rs.getString("KBN_DATE2");
                                if (null != d1) {
                                    final String n1 = rs.getString("KBN_NAME1");
                                    transInfo = new TransInfo(d1, n1);
                                } else if (null != d2) {
                                    final String n2 = rs.getString("KBN_NAME2");
                                    transInfo = new TransInfo(d2, n2);
                                }
                            } catch (final SQLException e) {
                                 log.error("SQLException", e);
                            }
                            if (null == transInfo) {
                                transInfo = new TransInfo();
                            }

                            student.setInfo(
                                    rs.getString("ATTENDNO"),
                                    rs.getString("NAME"),
                                    transInfo
                            );
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
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
         * SQL 任意の生徒の学籍情報を取得するSQL
         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
         */
        private String sqlStdNameInfo(final Param paramap) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + paramap.YEAR + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + paramap.YEAR + "' AND W2.SEMESTER = '" + paramap.SEMESTER + "' ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
            stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("WHERE   W1.YEAR = '" + paramap.YEAR + "' ");
            stb.append(    "AND W1.SCHREGNO = ? ");
            if (!paramap.SEMESTER.equals("9")) {
                stb.append("AND W1.SEMESTER = '" + paramap.SEMESTER + "' ");
            } else {
                stb.append("AND W1.SEMESTER = '" + paramap.SEME_FLG + "' ");
            }

            return stb.toString();
        }

        private void loadAttend(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", paramap._useCurriculumcd);
                paramMap.put("useVirus", paramap._useVirus);
                paramMap.put("useKoudome", paramap._useKoudome);
                paramMap.put("DB2UDB", db2);

                String targetSemes = paramap.SEMESTER;
                final String sql = AttendAccumulate.getAttendSemesSql(
                        paramap._semesFlg,
                        paramap._defineSchool,
                        paramap._knjSchoolMst,
                        paramap.YEAR,
                        paramap.SSEMESTER,
                        targetSemes,
                        (String) paramap._hasuuMap.get("attendSemesInState"),
                        paramap._periodInState,
                        (String) paramap._hasuuMap.get("befDayFrom"),
                        (String) paramap._hasuuMap.get("befDayTo"),
                        (String) paramap._hasuuMap.get("aftDayFrom"),
                        (String) paramap._hasuuMap.get("aftDayTo"),
                        paramap.GRADE,
                        (paramap.HRCLASS).substring(2, 5),
                        null,
                        "SEMESTER",
                        paramMap
                );
                log.debug(" attendSemesInState = " + paramap._hasuuMap.get("attendSemesInState"));
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!targetSemes.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    
                    final AttendInfo attendInfo = new AttendInfo(
                            rs.getInt("LESSON"),
                            rs.getInt("MLESSON"),
                            rs.getInt("SUSPEND") + ("true".equals(paramap._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(paramap._useKoudome) ? rs.getInt("KOUDOME") : 0),
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
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassAverage(paramap);
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
        private String sqlHrclassAverage(final Param paramap) {
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
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + paramap.YEAR + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + paramap.YEAR + "' ");
            if ("9".equals(paramap.SEMESTER)) {
                stb.append( "    AND W1.SEMESTER = '" + paramap.SEME_FLG + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE||W1.HR_CLASS = '" + paramap.HRCLASS + "' ");
            if (null != paramap.OUTPUT_COURSE) {
                stb.append(         "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + paramap.COURSECD + "' ");
            }
            stb.append(") ");

            //メイン表
            if (paramap._isChiben9909) {
                stb.append("SELECT  DECIMAL(ROUND(AVG(FLOAT(W3.TOTAL_SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.TOTAL_AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
                stb.append(  "FROM  ( ");
                stb.append("SELECT  W3.SCHREGNO ");
                stb.append(       ",SUM(W3.VALUE) AS TOTAL_SCORE ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.VALUE))*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append(  "FROM  RECORD_SCORE_DAT W3 ");
                stb.append( "WHERE  W3.YEAR = '" + paramap.YEAR + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SUBCLASS_MST W2 WHERE W3.SUBCLASSCD = W2.SUBCLASSCD) ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                stb.append("GROUP BY W3.SCHREGNO ");
                stb.append(  ") W3 ");
            } else {
                stb.append("SELECT  DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
                stb.append(  "FROM  RECORD_RANK_DAT W3 ");
                stb.append( "WHERE  W3.YEAR = '" + paramap.YEAR + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' ");
                stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
                stb.append(                "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            }

            return stb.toString();
        }

        private void loadRank(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdTotalRank(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._rank = rs.getInt("TOTAL_RANK");
                            student._scoreSum = rs.getString("TOTAL_SCORE");
                            student._scoreAvg = rs.getString("TOTAL_AVG");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
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
         * SQL 任意の生徒の順位を取得するSQL
         */
        private String sqlStdTotalRank(final Param paramap) {
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
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + paramap.YEAR + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + paramap.YEAR + "' ");
            if ("9".equals(paramap.SEMESTER)) {
                stb.append( "    AND W1.SEMESTER = '" + paramap.SEME_FLG + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            //メイン表
            if (paramap._isChiben9909) {
                stb.append("SELECT  W3.SCHREGNO ");
                stb.append(       ",cast(null as smallint) AS TOTAL_RANK ");
                stb.append(       ",SUM(W3.VALUE) AS TOTAL_SCORE ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.VALUE))*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append(  "FROM  RECORD_SCORE_DAT W3 ");
                stb.append( "WHERE  W3.YEAR = '" + paramap.YEAR + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SUBCLASS_MST W2 WHERE W3.SUBCLASSCD = W2.SUBCLASSCD) ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                stb.append("GROUP BY W3.SCHREGNO ");
            } else {
                stb.append("SELECT  W3.SCHREGNO ");
                if (paramap.OUTPUT_KIJUN.equals("2")) {
                    if (paramap.TOTAL_RANK.equals("1")) stb.append(   ",CLASS_AVG_RANK  AS TOTAL_RANK ");
                    if (paramap.TOTAL_RANK.equals("2")) stb.append(   ",GRADE_AVG_RANK  AS TOTAL_RANK ");
                    if (paramap.TOTAL_RANK.equals("3")) stb.append(   ",COURSE_AVG_RANK AS TOTAL_RANK ");
                } else {
                    if (paramap.TOTAL_RANK.equals("1")) stb.append(   ",CLASS_RANK  AS TOTAL_RANK ");
                    if (paramap.TOTAL_RANK.equals("2")) stb.append(   ",GRADE_RANK  AS TOTAL_RANK ");
                    if (paramap.TOTAL_RANK.equals("3")) stb.append(   ",COURSE_RANK AS TOTAL_RANK ");
                }
                stb.append(       ",W3.SCORE AS TOTAL_SCORE ");
                stb.append(       ",DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append(  "FROM  RECORD_RANK_DAT W3 ");
                stb.append( "WHERE  W3.YEAR = '" + paramap.YEAR + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' ");
                stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
                stb.append(                "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                // 総合点・平均点を算出。順位は空白。法政以外の処理。
                // RECORD_RANK_DAT.SUBCLASSCD = '999999' のレコードがない生徒のみ算出。
                if (!paramap._schoolName.equals("HOUSEI")) {
                    stb.append("UNION ");
                    stb.append("SELECT  W3.SCHREGNO ");
                    stb.append(       ",cast(null as smallint) AS TOTAL_RANK ");
                    stb.append(       ",SUM(W3.SCORE) AS TOTAL_SCORE ");
                    stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS TOTAL_AVG ");
                    stb.append(  "FROM  RECORD_RANK_DAT W3 ");
                    stb.append( "WHERE  W3.YEAR = '" + paramap.YEAR + "' ");
                    stb.append(   "AND  W3.SEMESTER = '" + paramap.SEMESTER + "' ");
                    stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' ");
                    stb.append(   "AND  EXISTS(SELECT 'X' FROM SUBCLASS_MST W2 WHERE W3.SUBCLASSCD = W2.SUBCLASSCD) ");
                    stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                    stb.append(   "AND  W3.SCHREGNO NOT IN( ");
                    stb.append(        "SELECT  R1.SCHREGNO ");
                    stb.append(          "FROM  RECORD_RANK_DAT R1 ");
                    stb.append(         "WHERE  R1.YEAR = '" + paramap.YEAR + "' ");
                    stb.append(           "AND  R1.SEMESTER = '" + paramap.SEMESTER + "' ");
                    stb.append(           "AND  R1.TESTKINDCD || R1.TESTITEMCD = '" + paramap.TESTKINDCD + "' ");
                    stb.append(           "AND  R1.SUBCLASSCD = '999999' ");
                    stb.append(           "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE R1.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                    stb.append(   ") ");
                    stb.append("GROUP BY W3.SCHREGNO ");
                }
            }

            return stb.toString();
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

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlStdSubclassDetail(paramap, _code));
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (_knjdobj.enablePringFlg() && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    
                    final ScoreDetail scoreDetail = new ScoreDetail(
                            getSubClass(rs, _subclasses),
                            ScoreValue.create(rs.getString("SCORE"),rs.getString("SUBCLASSCD")),
                            ScoreValue.create(rs.getString("PATTERN_ASSESS"),rs.getString("SUBCLASSCD")),
                            (Integer) rs.getObject("REPLACEMOTO"),
                            (String) rs.getObject("PRINT_FLG"),
                            (String) rs.getObject("SCORE_FLG"),
                            (Integer) rs.getObject("COMP_CREDIT"),
                            (Integer) rs.getObject("GET_CREDIT"),
                            (Integer) rs.getObject("CREDITS"),
                            paramap
//                            rs.getString("CHAIRCD")
                    );
                    student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            
            try {
                final Map paramMap = new HashMap();
                paramMap.put("absenceDiv", "1");
                paramMap.put("useCurriculumcd", paramap._useCurriculumcd);
                paramMap.put("useVirus", paramap._useVirus);
                paramMap.put("useKoudome", paramap._useKoudome);
                paramMap.put("DB2UDB", db2);

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        paramap._semesFlg,
                        paramap._defineSchool,
                        paramap._knjSchoolMst,
                        paramap.YEAR,
                        paramap.SSEMESTER,
                        paramap.SEMESTER,
                        (String) paramap._hasuuMap.get("attendSemesInState"),
                        paramap._periodInState,
                        (String) paramap._hasuuMap.get("befDayFrom"),
                        (String) paramap._hasuuMap.get("befDayTo"),
                        (String) paramap._hasuuMap.get("aftDayFrom"),
                        (String) paramap._hasuuMap.get("aftDayTo"),
                        _code.substring(0, 2),
                        _code.substring(2),
                        null,
                        paramMap
                        );
                log.debug(" attend sql = " + sql);
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
                    
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    
                    ScoreDetail scoreDetail = null;
                    for (final Iterator it = student._scoreDetails.keySet().iterator(); it.hasNext();) {
                        final String subclasscd = (String) it.next();
                        if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
                            scoreDetail = (ScoreDetail) student._scoreDetails.get(subclasscd);
                            break;
                        }
                    }
                    if (null == scoreDetail) {
//                        SubClass subClass = null;
//                        for (final Iterator it = _subclasses.keySet().iterator(); it.hasNext();) {
//                            final String subclasscd = (String) it.next();
//                            if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
//                                subClass = (SubClass) _subclasses.get(subclasscd);
//                                scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null);
//                                student._scoreDetails.put(subclasscd, scoreDetail);
//                                break;
//                            }
//                        }
                        if (null == scoreDetail) {
                            // log.fatal(" no detail " + student._code + ", " + rs.getString("SUBCLASSCD"));
                            continue;
                        }
                    }

                    final String classCd = subclassCd == null || "".equals(subclassCd) ? "" : subclassCd.substring(0, 2);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        scoreDetail._jisu = Integer.valueOf(rs.getString("MLESSON"));
                        if (null != scoreDetail._jisu) {
                            if (0 != scoreDetail._jisu.intValue() && scoreDetail._subClass._jisu < scoreDetail._jisu.intValue()) scoreDetail._subClass._jisu = scoreDetail._jisu.intValue();
                        }
                        if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                            scoreDetail._absent = Double.valueOf(rs.getString("REPLACED_SICK"));
                        } else {
                            scoreDetail._absent = Double.valueOf(rs.getString("SICK2"));
                        }
                        scoreDetail._absenceHigh = new BigDecimal(StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "99.0"));
//                        final String absenceWarn = rs.getString("ABSENCE_WARN" + ("1".equals(_param._warnSemester) ? "" : _param._warnSemester));
//                        final String credits = rs.getString("CREDITS");
//                        if (_param._useAbsenceWarn && null != credits && null != absenceWarn) {
//                            scoreDetail._absenceHigh = scoreDetail._absenceHigh.subtract(new BigDecimal(Integer.parseInt(credits) * Integer.parseInt(absenceWarn)));
//                        }
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh, paramap);
                    }
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
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(final Param paramap, final String gradeHrclass) {
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
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + paramap.YEAR + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + paramap.DATE + "' THEN W2.EDATE ELSE '" + paramap.DATE + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            
            stb.append("     WHERE   W1.YEAR = '" + paramap.YEAR + "' ");
            if ("9".equals(paramap.SEMESTER)) {
                stb.append( "    AND W1.SEMESTER = '" + paramap.SEME_FLG + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + paramap.SEMESTER + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + gradeHrclass + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
                stb.append(              "W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + paramap.YEAR + "' ");
            stb.append(        "AND W2.YEAR = '" + paramap.YEAR + "' ");
            stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(        "AND W1.SEMESTER <= '" + paramap.SEMESTER + "' ");
            stb.append(        "AND W2.SEMESTER <= '" + paramap.SEMESTER + "' ");
            stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
            stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     ")");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + paramap.YEAR + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                   "T1.SUBCLASSCD)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SUBCLASSCD, T1.CREDITS");
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + paramap.YEAR + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                          AND ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(                               "T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + paramap.YEAR + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "COMBINED_SUBCLASSCD");
            stb.append(") ");       
            
            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
            if (paramap.TESTKINDCD.equals("0101") || paramap.TESTKINDCD.equals("0201") || paramap.TESTKINDCD.equals("0202")) {
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
            stb.append(    "FROM    RECORD_RANK_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + paramap.YEAR + "' AND ");
            stb.append("            W3.SEMESTER = '" + paramap.SEMESTER + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");
            
            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(           ",W3.COMP_CREDIT ,W3.GET_CREDIT ");
            stb.append(           ",CASE WHEN W3.VALUE IS NOT NULL THEN RTRIM(CHAR(W3.VALUE)) ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + paramap.YEAR + "' AND ");
            stb.append("            W3.SEMESTER = '" + paramap.SEMESTER + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' AND ");
            stb.append("            W3.SCORE_DIV = '00' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //追試試験データの表
            stb.append(",SUPP_EXA AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ,SCORE_FLG ");
            stb.append(           ",CASE WHEN SCORE_PASS IS NOT NULL THEN RTRIM(CHAR(SCORE_PASS)) ");
            stb.append(                 "ELSE NULL END AS SCORE_PASS ");
            stb.append(    "FROM    SUPP_EXA_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + paramap.YEAR + "' AND ");
            stb.append("            W3.SEMESTER = '" + paramap.SEMESTER + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap.TESTKINDCD + "' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //メイン表
            stb.append(" SELECT  value(T7.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T34.SCORE_FLG ");
            stb.append("        ,CASE WHEN T34.SCORE_FLG IS NOT NULL THEN T34.SCORE_PASS ");
            stb.append("              ELSE T3.SCORE END AS SCORE ");
            if (paramap.VALUE_FLG.equals("on")) {
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
            //対象生徒・講座の表
            stb.append(" FROM(");
            stb.append("     SELECT  W2.SCHREGNO,");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append(             "W2.SUBCLASSCD");
            stb.append("     FROM    CHAIR_A W2");
            if (!paramap.SEMESTER.equals("9")) {
                stb.append(" WHERE   W2.SEMESTER = '" + paramap.SEMESTER + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO, ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append(              "W2.SUBCLASSCD");
            stb.append(" )T1 ");
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
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(            "COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + paramap.YEAR + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(              "COMBINED_SUBCLASSCD");
            stb.append(" )T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(              "ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + paramap.YEAR + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(              "ATTEND_SUBCLASSCD");
            stb.append(" )T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append(                               "T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(                            " T8.CLASSCD || '-' || T8.SCHOOL_KIND = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(                            " T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
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
                final int[] ret = Param.setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = ret[0];
                subclass._mincredit = ret[1];               
//                if (0 != credit) {
//                    if (subclass._maxcredit < credit) subclass._maxcredit = credit;
//                    if (credit < subclass._mincredit || 0 == subclass._mincredit) subclass._mincredit = credit;
//                }
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
            final SubClass subClass = new SubClass(subclasscode, classabbv, subclassabbv, electdiv, credit);
            subclasses.put(subclasscode, subClass);
            return subClass;
        }

        // 前年度までの修得単位数計
        private void loadPreviousCredits(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdPreviousCredits(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._previousCredits = rs.getInt("CREDIT");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
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
         * 前年度までの修得単位数計
         * @param paramap
         * @return
         */
        private String sqlStdPreviousCredits(final Param paramap) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + paramap.YEAR + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");

            return stb.toString();
        }

        // 前年度までの未履修（必須科目）数
        private void loadPreviousMirisyu(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdPreviousMirisyu(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._previousMirisyu = rs.getInt("COUNT");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
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
         * @param paramap
         * @return
         */
        private String sqlStdPreviousMirisyu(final Param paramap) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT COUNT(*) AS COUNT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" INNER JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                " T1.SUBCLASSCD = ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                " T2.SUBCLASSCD");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + paramap.YEAR + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            stb.append("    AND VALUE(T2.ELECTDIV,'0') <> '1'");
            stb.append("    AND VALUE(T1.COMP_CREDIT,0) = 0");

            return stb.toString();
        }

        // 今年度の資格認定単位数
        private void loadQualifiedCredits(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdQualifiedCredits(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._qualifiedCredits = rs.getInt("CREDITS");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
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
         * 今年度の資格認定単位数
         * @param paramap
         * @return
         */
        private String sqlStdQualifiedCredits(final Param paramap) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(T1.CREDITS) AS CREDITS");
            stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + paramap.YEAR + "'");

            return stb.toString();
        }
        
        // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
        private void setGradeAttendCheckMark(Param paramap) {
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._isGradeGood = student.isGradeGood(paramap);
                student._isGradePoor = student.isGradePoor();
                student._isAttendPerfect = student.isAttendPerfect();
                student._isKekkaOver = student.isKekkaOver();
            }
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         * @param hrInfo
         */
        private void setSubclassAverage(
                final HRInfo hrInfo
        ) {
            final KNJD065_COMMON knjdobj = _knjdobj;
            final Map map = new HashMap();

            for (final Iterator itS = hrInfo._students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                for (final Iterator itD = student._scoreDetails.values().iterator(); itD.hasNext();) {
                    final ScoreDetail detail = (ScoreDetail) itD.next();
//                    final ScoreValue val = detail.getPatternAsses();
                    final ScoreValue val = knjdobj.getTargetValue(detail);
                    if (null == val) continue;
                    if (!val.hasIntValue()) continue;
                    final SubClass subClass = detail._subClass;
                    int[] arr = (int[]) map.get(subClass);
                    if (null == arr) {
                        arr = new int[2];
                        map.put(subClass, arr);
                    }
                    arr[0] += val.getScoreAsInt();
                    arr[1]++;
                }
            }

            for (final Iterator it = hrInfo._subclasses.values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                if (map.containsKey(subclass)) {
                    final int[] val = (int[]) map.get(subclass);
                    double d = 0;
                    if (0 != val[1]) {
                        d = round10(val[0], val[1]);
                        subclass._scoreaverage = DEC_FMT1.format(d);
                        subclass._scoretotal = String.valueOf(val[0]);
                        subclass._scoreCount = String.valueOf(val[1]);
                    }
                }
            }
        }
        
        /**
         * 学級平均の算出
         */
        private void setHrTotal(
                final HRInfo hrInfo,
                final Param param
        ) {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int mlesson = 0;
            int present = 0;
            int absent = 0;
            int[] arrc = {0,0};  // 履修単位
            int[] arrj = {0,0};  // 授業日数
            for (final Iterator itS = hrInfo._students.iterator(); itS.hasNext();) {
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
                    mlesson += attend._mLesson;
                    present += attend._present;
                    absent += attend._absent;
                    int[] ret = Param.setMaxMin(arrj[0], arrj[1], param._isChiben ? attend._lesson : attend._mLesson);
                    arrj[0] = ret[0];
                    arrj[1] = ret[1];
                }
                int[] ret = Param.setMaxMin(arrc[0], arrc[1], student._compCredit);
                arrc[0] = ret[0];
                arrc[1] = ret[1];
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
                _HrCompCredits = arrc[0] + "単位";
            }
            if (0 < arrj[0]) {
                _HrMLesson = arrj[0] + "日";
            }
        }

        /**
         * 順位の算出
         */
        private List createRanking() {
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
            final Total total = student._total;
            if (0 >= total._count) {
                return -1;
            }
            return 1 + _ranking.indexOf(total);
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

        TransInfo() {
            this(null, null);
        }

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
    /**
     * <<生徒のクラス>>。
     */
    private class Student implements Comparable {
        private final int _gnum;  // 行番号
        private final String _code;  // 学籍番号
        private final HRInfo _hrInfo;
        private String _attendNo;
        private String _name;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo;
        private String _scoreSum;
        private String _scoreAvg;
        private int _rank;
        private final Map _scoreDetails = new HashMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private boolean _isGradeGood;  // 成績優良者
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者

        Student(final String code, final HRInfo hrInfo, final int gnum) {
            _gnum = gnum;
            _code = code;
            _hrInfo = hrInfo;
        }

        void setInfo(
                final String attendNo,
                final String name,
                final TransInfo tansInfo
        ) {
            _attendNo = attendNo;
            _name = name;
            _transInfo = tansInfo;
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
            return _attendNo + ":" + _name;
        }

        /**
         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
         */
        boolean isGradeGood(Param paramap) {
            final BigDecimal avg = _total._avgBigDecimal;
            if (null == avg) { return false; }
            Float ac = (Float) paramap.ASSESS;
            if (ac.floatValue() <= avg.doubleValue()) { return true; }
            return false;
        }

        /**
         * @return 成績不振者（評定１が1つでもある）は true を戻します。
         */
        boolean isGradePoor() {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final ScoreValue val = detail._patternAssess;
                if (null == val) continue;
                if (!val.hasIntValue()) continue;
                if (val._val == 1){ return true; }
            }
            return false;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
         */
        boolean isAttendPerfect() {
            if (! _attendInfo.isAttendPerfect()) { return false; }
            
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final Double kekka = detail._absent;
                if (null == kekka) continue;
                if (0 < kekka.doubleValue()){ return false; }
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

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
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
        private String _scoreCount;  // 学級人数
        

        SubClass(
                final String subclasscode, 
                final String classabbv, 
                final String subclassabbv,
                final boolean electdiv,
                final int credit
        ) {
            _classcode = subclasscode.substring(1, 3);
            _classabbv = classabbv;
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
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
     * <<素点・評定データのクラスです>>。
     */
    private static class ScoreValue {
        private final String _strScore;
        private final boolean _isInt;
        private int _val;

        ScoreValue(final String strScore) {
            _strScore = strScore;
            _isInt = !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore);
            if (_isInt) {
                _val = Integer.parseInt(_strScore);
            }
        }

        static ScoreValue create(final String strScore) {
            if (null == strScore) return null;
            return new ScoreValue(strScore);
        }

        /**
         * 生徒別科目別の素点または評定のインスタンスを作成します。
         * @param strScore 素点または評定
         * @param classcd 教科コード
         * @return ScoreValue。'総合的な学習の時間'は'null'を戻します。
         */
        static ScoreValue create(
                final String strScore,
                final String classcd
        ) {
            if (null == strScore) return null;
            if (KNJDefineSchool.subject_T.equals(classcd.substring(1, 3))) return null;
            return new ScoreValue(strScore);
        }

        String getScore() { return _strScore; }
        boolean hasIntValue() { return _isInt; }
        int getScoreAsInt() { return _val; }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private class ScoreDetail {
        private final SubClass _subClass;
        private Double _absent;
        private Integer _jisu;
        private final ScoreValue _score;
        private final ScoreValue _patternAssess;
        private final Integer _replacemoto;
        private final String _print_flg;
        private final String _score_flg;
        private final Integer _compCredit;
        private final Integer _getCredit;
        private BigDecimal _absenceHigh;
        private final Integer _credits;
        private boolean _isOver;
//        private final String _chaircd;

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
                final Param paramap
//                final String chaircd
        ) {
            _subClass = subClass;
            _score = score;
            _patternAssess = patternAssess;
            _replacemoto = replacemoto;
            _print_flg = print_flg;
            _score_flg = score_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
//            _chaircd = chaircd;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh, final Param paramap) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            final boolean absenceHighIsDecimal = null != paramap && "1".equals(paramap.creditMstAbsenceHighIsDecimal);
            if (0.1 > absent.floatValue() || 0 == (absenceHighIsDecimal? absenceHigh.doubleValue() : absenceHigh.intValue())) {
                return false;
            }
            if ((absenceHighIsDecimal? absenceHigh.doubleValue() : absenceHigh.intValue()) < absent.floatValue()) {
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
            if (null != _replacemoto && _replacemoto.intValue() >= 1) {
                return false;
            }
            return true;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
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
            final KNJD065_COMMON knjdObj = _knjdobj;

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            for (final Iterator it = _student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                final ScoreValue scoreValue = knjdObj.getTargetValue(detail);
                if (isAddTotal(scoreValue, detail._replacemoto, knjdObj)) {
                    total += scoreValue.getScoreAsInt();
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
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            }
            if (0 < compCredit) { _student._compCredit = compCredit; }
            if (0 < getCredit) { _student._getCredit = getCredit; }
        }

        
        /**
         * @param scoreValue
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final ScoreValue scoreValue,
                final Integer replacemoto,
                final KNJD065_COMMON knjdObj
        ) {
            if (null == scoreValue) { return false; }
            if (!scoreValue.hasIntValue()) { return false; }
            if (knjdObj.isGakunenMatu() && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
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
        
        final String YEAR;
        final String SEMESTER;
        final String SEME_FLG;
        final String GRADE;
        final String TESTKINDCD;
        final String VALUE_FLG;
        final String DATE;
        String CREDIT_DROP;
        String NO_KETUBAN;
        String OUTPUT_COURSE = null;
        final String TOTAL_RANK;
        final String RANKNAME;
        final String OUTPUT_KIJUN;
        final Float ASSESS;
        String PRGID;
        final String creditMstAbsenceHighIsDecimal;
        final boolean _isChiben;
        final boolean _isChiben9909;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        
        final String SEMESTERNAME;
        final String SEMESTERDATE_S;
        final String YEARDATE_S;
        final String TESTITEMNAME;
        
        final String NENDO;
        final String NOW;
        final String TERM_KEKKA;
        final String TERM_ATTEND;
        
        String HRCLASS;
        String COURSECD;

        final String _schoolName;
        // 端数計算共通メソッド引数
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";

        private KNJDefineSchool _defineSchool;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        private KNJ_Get_Info _getinfo;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            final String totalRank = request.getParameter("OUTPUT_RANK");

            _schoolName = setZ010Name1(db2);
            _isChiben = "CHIBEN".equals(_schoolName);
            final String testKindCd = request.getParameter("TESTKINDCD");
            _isChiben9909 = _isChiben && "9909".equals(testKindCd);

            YEAR = request.getParameter("YEAR");  //年度
            SEMESTER = request.getParameter("SEMESTER");  //学期
            SEME_FLG = request.getParameter("SEME_FLG");  //LOG-IN時の学期（現在学期）
            GRADE = request.getParameter("GRADE");  //学年
            TESTKINDCD = "0".equals(testKindCd) ? "0000" : "9909".equals(testKindCd) ? "9900" : testKindCd;  //テスト・成績種別
            VALUE_FLG = "9909".equals(testKindCd) ? "on" : "off";  //record_score_dat.valueを出力するか？
            DATE = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));  //出欠集計日付
            if( request.getParameter("OUTPUT4") != null) { CREDIT_DROP = "1"; }  //単位保留
            if( request.getParameter("OUTPUT5") != null ) { NO_KETUBAN =  "1"; }  //欠番を詰める
            if (request.getParameter("OUTPUT_COURSE") != null) { OUTPUT_COURSE = "1"; }  //同一クラスでのコース毎に改頁あり
            TOTAL_RANK = totalRank;  //総合順位出力 1:学級 2:学年 3:コース
            //総合順位出力の順位欄項目名
            if (totalRank.equals("1")) {
                RANKNAME = "学級順位";
            } else if (totalRank.equals("2")) {
                RANKNAME = "学年順位";
            } else {
                RANKNAME = "コース順位";
            }
            OUTPUT_KIJUN = request.getParameter("OUTPUT_KIJUN") == null ? "" : request.getParameter("OUTPUT_KIJUN");
            //成績優良者評定平均の基準値===>KNJD610：未使用
            if( request.getParameter("ASSESS") != null ) {
                ASSESS = new Float(request.getParameter("ASSESS1"));
            } else {
                ASSESS = new Float(4.3);
            }
            // 起動元のプログラムＩＤ
            if( request.getParameter("PRGID") != null ) { PRGID = request.getParameter("PRGID"); }
            // CREDIT_MSTのABSENCE_HIGHがDECIMALか
            creditMstAbsenceHighIsDecimal = request.getParameter("creditMstAbsenceHighIsDecimal");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            
            _defineSchool = createDefineCode(db2);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, YEAR);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _getinfo = new KNJ_Get_Info();
            
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, YEAR, SEMESTER);
            SEMESTERNAME = returnval.val1;  //学期名称

            // 学期期間FROM
            if (null == returnval.val2) {
                SEMESTERDATE_S = YEAR + "-04-01";
            } else {
                SEMESTERDATE_S = returnval.val2;
            }

            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, YEAR, "9");
            YEARDATE_S = returnval1.val2;

            // テスト名称
            final String testitemname = getTestName(db2, YEAR, SEMESTER, TESTKINDCD);
            TESTITEMNAME = testitemname;

            loadAttendSemesArgument(db2);
            
            // 年度
            NENDO = nao_package.KenjaProperties.gengou(Integer.parseInt(YEAR)) + "年度";

            // 作成日
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            NOW = stb.toString();

            // 出欠集計範囲(欠課数の集計範囲)
            final String date_E = KNJ_EditDate.h_format_JP(DATE);
            TERM_KEKKA = KNJ_EditDate.h_format_JP(YEARDATE_S) + FROM_TO_MARK + date_E;

            //「出欠の記録」の日付範囲
            final String fromDate = KNJ_EditDate.h_format_JP(SEMESTERDATE_S);
            TERM_ATTEND = fromDate + FROM_TO_MARK + date_E;
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2, final String year) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }
        
        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();

            definecode.defineCode(db2, YEAR);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
            String name1 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT VALUE(NAME1, '') AS NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
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

        private void loadAttendSemesArgument(final DB2UDB db2) {
            
            try {
                loadSemester(db2, YEAR);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2, YEAR);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, YEAR, SSEMESTER, SEMESTER);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, YEAR);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, DATE); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }


    /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

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
        
        private String sqlSemester(final String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
        
        private String getTestName(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) {
            String testitemname = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW "
                                 +  "WHERE YEAR = '" + year + "' "
                                 +    "AND SEMESTER = '" + semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD = '" + testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    testitemname = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testitemname;
        }
        
        private static int[] setMaxMin(
                int maxInt,
                int minInt,
                int tergetInt
        ) {
            if (0 < tergetInt) {
                if (maxInt < tergetInt){ maxInt = tergetInt; }
                if (0 == minInt) {
                    minInt = tergetInt;
                } else {
                    if (minInt > tergetInt){ minInt = tergetInt; }
                }
            }
            return new int[]{maxInt,minInt};
        }
    }
}
