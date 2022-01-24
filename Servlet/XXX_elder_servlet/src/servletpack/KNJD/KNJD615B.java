// kanji=漢字
/*
 * $Id: 6f1d7acf6dc09dd04d230d1406f00e4630b62bb2 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.CsvUtils;
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
 * @version $Id: 6f1d7acf6dc09dd04d230d1406f00e4630b62bb2 $
 */
public class KNJD615B {
    private static final Log log = LogFactory.getLog(KNJD615B.class);

    private KNJD065_COMMON _knjdobj;  //成績別処理のクラス

    private final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    
    private static final String ATTRIBUTE_FAIL = "Paint=(1,50,1),Bold=1";
    private static final String ATTRIBUTE_KEKKA_OVER = "Paint=(1,70,1),Bold=1";
    private static final String ATTRIBUTE_CLEAR = "Paint=(0,0,0),Bold=0";

    private Param _param;
    private boolean _hasData = false;

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        boolean isCsv = false;
        DB2UDB db2 = null;
        Vrw32alpWrap svf = null;
        List csvOutputLines = null;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            _param = createParam(request, db2);
            
            try {
                _knjdobj = createKnjd065Obj(db2, _param);  //成績別処理クラスを設定するメソッド
            } catch (Exception e) {
                log.error("Exception", e);
            }
            log.fatal(_knjdobj);
            
            isCsv = "csv".equals(_param._cmd);
            if (isCsv) {
                csvOutputLines = new ArrayList();
                
                final List hrInfoList = new ArrayList();
                for (int h = 0; h < _param._classSelected.length; h++) {
                    final HRInfo hrInfo = new HRInfo(_param._classSelected[h]);
                    hrInfo.load(db2, null);
                    hrInfoList.add(hrInfo);
                }
                
                setCsvOutputLine(csvOutputLines, hrInfoList);

            } else {
                svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
                svf.setKurikaeshiNum(_param._formMaxColumn);
                svf.setFieldNum(_param._formMaxLine);
                sd.setSvfInit(request, response, svf);
                
                for (int h = 0; h < _param._classSelected.length; h++) {

                    final List courses;
                    if (_param._outputCourse) {
                        courses = Course.createCourses(db2, _param._classSelected[h], _param);
                        log.debug("コース数=" + courses.size());
                    } else {
                        courses = new ArrayList();
                        courses.add(null);
                    }
                    
                    for (final Iterator it = courses.iterator(); it.hasNext();) {
                        final Course course = (Course) it.next();
                        final HRInfo hrInfo = new HRInfo(_param._classSelected[h]);
                        hrInfo.load(db2, null == course ? null : course._coursecd);
                        
                        // 印刷処理
                        printMain(svf, hrInfo);
                    }

                }
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (isCsv) {
                CsvUtils.outputLines(log, response, "成績一覧表.csv", csvOutputLines);
            } else {
                if (null != svf) {
                    sd.closeSvf(svf, _hasData);
                }
            }
            if (null != db2) {
                sd.closeDb(db2);
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
        
        private static List createCourses(final DB2UDB db2, final String gradeHrclass, final Param param) throws SQLException {
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
            if (!"9".equals(param._semester)) {
                stb.append(" W1.SEMESTER = '" + param._semester + "' AND ");
            } else {
                stb.append(" W1.SEMESTER = '" + param._semeFlg + "' AND ");
            }
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

    /**
     *  成績別処理クラス設定
     */
    private static KNJD065_COMMON createKnjd065Obj(
            final DB2UDB db2,
            final Param param
    ) throws Exception {
        final String testKindCd = param._testKindCd;
        log.debug(" testKindCd = " + testKindCd + " , isChiben = " + param.isChiben());

        // 成績一覧表(KNJD062) 中間
        if ("0101".equals(testKindCd)) { return new KNJD062A_INTER(db2, param); }

        // 成績一覧表(KNJD062) 期末1 or 期末2
        if ("0201".equals(testKindCd) || "0202".equals(testKindCd)) { return new KNJD062A_TERM(db2, param); }

        // 成績一覧表(KNJD062) 学期
        if (!"9".equals(param._semester)) { return new KNJD062A_GAKKI(db2, param); }

        // 成績一覧表(KNJD062) 学年(智辯・評価)
        if (!param.isChiben9909() && "9900".equals(testKindCd) && param.isChiben()) { return new KNJD062A_GRADE_HYOKA(db2, param); }
        
        // 学年(評定：5段階)・・・指示画面で9909を指定。
        if ("on".equals(param._valueFlg)) { return new KNJD062A_GRADE_HYOTEI(db2, param); }

        // 学年(成績：100段階)・・・指示画面で9900を指定。
        return  new KNJD062A_GRADE(db2, param);
    }
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    /**
     * 学級ごとの印刷。
     * @param svf
     * @param hrInfo
     * @return
     */
    private void printMain(
            final Vrw32alpWrap svf,
            final HRInfo hrInfo
    ) {

        final Map pageStudentListMap = new TreeMap();
        for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            final int nowpage = (int)Math.floor((double)student._gnum / (double)_param._formMaxLine);
            getMappedList(pageStudentListMap, new Integer(nowpage)).add(student);
        }
        for (final Iterator listit = pageStudentListMap.values().iterator(); listit.hasNext();) {
            final List list = (List) listit.next();
            printSub(svf, hrInfo, list);
        }
    }
    
    private void setCsvOutputLine(final List lines, final List hrInfoList) {
        final List headerLine1 = CsvUtils.newLine(lines);
        final List headerLine2 = CsvUtils.newLine(lines);
        
        final Map subclassMap = new TreeMap();
        for (final Iterator it = hrInfoList.iterator(); it.hasNext();) {
            final HRInfo hrInfo = (HRInfo) it.next();
            final List subClassAllList = new ArrayList(hrInfo._subclasses.values());

            for (final Iterator subit = subClassAllList.iterator(); subit.hasNext();) {
                final SubClass subclass = (SubClass) subit.next();
                final boolean notOutputColumn = "90".equals(subclass._classcode) && _param._notOutputSougou;
                if (notOutputColumn) {
                    continue;
                }
                
                if (null == subclassMap.get(subclass._subclasscode)) {
                    subclassMap.put(subclass._subclasscode, subclass);
                }
            }
        }
        final List subclassList = new ArrayList(subclassMap.values());
        
        headerLine1.addAll(Arrays.asList(new String[] {"年度", "学期", "学籍番号", "学年", "組", "出席番号", "生徒氏名", "テスト種別"}));
        headerLine2.addAll(Arrays.asList(new String[] {"出欠集計日付：" + StringUtils.replace(StringUtils.defaultString(_param._date), "-", "/") , "", "", "", "", "", "", ""}));
        for (int i = 0; i < subclassList.size(); i++) {
            final SubClass subClass = (SubClass) subclassList.get(i);
            log.info(" subClass " + String.valueOf(i) + " = " + subClass.getSubclasscd() + " " + subClass._subclassname);
            headerLine1.add(subClass._subclassname);
            headerLine1.add(_knjdobj._item2Name);
            headerLine2.add(subClass.getSubclasscd());
            headerLine2.add("");
        }
        headerLine1.addAll(Arrays.asList(new String[] {"総合点", "平均点", "学級順位", _param.getRankName() + "順位", "欠点科目数"}));
        headerLine1.addAll(Arrays.asList(new String[] {"特別欠席出停", "特別欠席忌引", "留学中の授業日数", "出席すべき日数", "出席日数", "欠席日数", "早退回数", "遅刻回数", "備考"}));
        for (final Iterator it = hrInfoList.iterator(); it.hasNext();) {
            final HRInfo hrInfo = (HRInfo) it.next();
            for (int sti = 0; sti < hrInfo._students.size(); sti++) {
                final Student student = (Student) hrInfo._students.get(sti);
                final List line = CsvUtils.newLine(lines);
                
                line.add(_param._year);
                line.add(_param._semesterName);
                line.add(student._code);
                line.add(hrInfo._hrclassCd.substring(0, 2));
                line.add(hrInfo._hrclassCd.substring(2));
                line.add(student._attendNo);
                line.add(student._name);
                line.add(_param._testItemName);
                
                for (int i = 0; i < subclassList.size(); i++) {
                    final SubClass subClass = (SubClass) subclassList.get(i);
                    final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subClass._subclasscode);
                    if (null == detail) {
                        line.add(null);
                        line.add(null);
                    } else {
                        ScoreValue sv;
                        if (_knjdobj.isPrintScore()) {
                            // 素点
                            sv = detail._score;
                        } else {
                            // 成績
                            sv = detail._patternAssess;
                        }
                        if (null == sv) {
                            line.add("");
                        } else {
                            line.add(sv.getScore());
                        }
                        if (_param._outputAssessLevel) {
                            line.add(detail._assessLevel);
                        } else {
                            if (null == detail._absent) {
                                line.add("");
                            } else {
                                final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                                if (0 == value) {
                                    line.add("");
                                } else {
                                    line.add(_param._absentFmt.format(detail._absent.floatValue()));
                                }
                            }
                        }
                    }
                }
                line.add(student._scoreSum);
                line.add(student._scoreAvg);
                line.add(1 <= student._classRank ? String.valueOf(student._classRank) : "");
                line.add(1 <= student._rank ? String.valueOf(student._rank) : "");
                line.add(1 <= student._total._countFail ? String.valueOf(student._total._countFail) : "");

                final AttendInfo att = null == student._attendInfo ? new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0) : student._attendInfo;
                line.add(zeroToBlank(att._suspend));      // 出席停止
                line.add(zeroToBlank(att._mourning));     // 忌引
                line.add(zeroToBlank(att._transDays));    // 留学等実績
                line.add(zeroToBlank(att._mLesson));      // 出席すべき日数
                line.add(zeroToBlank(att._present));      // 出席日数
                line.add(zeroToBlank(att._absent));       // 欠席日数
                line.add(zeroToBlank(att._early));        // 早退回数
                line.add(zeroToBlank(att._late));      // 遅刻回数

                line.add(student._transInfo.toString()); // 備考
            }
        }
    }
    
    private static String zeroToBlank(int num) {
        return 0 == num ? "" : String.valueOf(num);
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    /**
     * 学級ごと、生徒MAX行ごとの印刷。
     * @param svf
     * @param hrInfo：年組
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private void printSub(
            final Vrw32alpWrap svf,
            final HRInfo hrInfo,
            final List studentList
    ) {
        final List subClassAllList = new ArrayList(hrInfo._subclasses.values());

        for (final Iterator it = subClassAllList.iterator(); it.hasNext();) {
            final SubClass subclass = (SubClass) it.next();
            final boolean notOutputColumn = "90".equals(subclass._classcode) && _param._notOutputSougou;
            if (notOutputColumn) {
                it.remove();
            }
        }
        final String form = _param.getFormFile();
        
        final List subclassPageList = getPageList(subClassAllList, _param._formMaxColumn);
        
        for (int pi = 0; pi < subclassPageList.size(); pi++) {

            svf.VrSetForm(form, 4);  //SVF-FORM設定
            _knjdobj.printHeader(svf, hrInfo);
            _knjdobj.printHeaderOther(svf);
            
            printInkanran(svf);

            // 生徒の氏名・備考を印字
            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();
                
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

            final List subClassList = (List) subclassPageList.get(pi);
            for (int subi = 0; subi < subClassList.size(); subi++) {
                
                final SubClass subclass = (SubClass) subClassList.get(subi);
                
                if (subClassList.size() - 1 == subi) { 
                    // 生徒別最終出力の処理
                    for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                        final Student student = (Student) stit.next();
                        
                        // 生徒別総合点・平均点・順位を印刷します。
                        final int gnum = student._gnum;
                        if (null != student._scoreSum) {
                            svf.doNumberingSvfOut("TOTAL", gnum, student._scoreSum);  //総合点
                            svf.doNumberingSvfOut("AVERAGE", gnum, student._scoreAvg);  //平均点
                        }
                        
                        //順位（学級）
                        if (1 <= student._classRank) {
                            svf.doNumberingSvfOut("CLASS_RANK", gnum, String.valueOf(student._classRank));
                        }
                        //順位（学年orコース）
                        if (1 <= student._rank) {
                            svf.doNumberingSvfOut("RANK", gnum, String.valueOf(student._rank));
                        }
                        if (null != student._total) {
                            //欠点科目数
                            if (0 < student._total._countFail) {
                                svf.doNumberingSvfOut("FAIL", gnum, String.valueOf(student._total._countFail));
                            }
                        }
                        
                        if (null != student._attendInfo) {
                            svf.doNumberingSvfOutNonZero("PRESENT", gnum, student._attendInfo._mLesson);      // 出席すべき日数
                            svf.doNumberingSvfOutNonZero("SUSPEND", gnum, student._attendInfo._suspend);      // 出席停止
                            svf.doNumberingSvfOutNonZero("KIBIKI", gnum,  student._attendInfo._mourning);     // 忌引
                            svf.doNumberingSvfOutNonZero("ABSENCE", gnum, student._attendInfo._absent);       // 欠席日数
                            svf.doNumberingSvfOutNonZero("ATTEND", gnum,  student._attendInfo._present);      // 出席日数
                            svf.doNumberingSvfOutNonZero("TOTAL_LATE", gnum, student._attendInfo._late);      // 遅刻回数
                            svf.doNumberingSvfOutNonZero("LEAVE", gnum,   student._attendInfo._early);        // 早退回数
                            svf.doNumberingSvfOutNonZero("ABROAD", gnum,  student._attendInfo._transDays);    // 留学等実績
                        }
                        if (_knjdobj.hasCompCredit()) {
                            //今年度履修単位数
                            svf.doNumberingSvfOutNonZero("COMP_CREDIT", gnum, String.valueOf(student._compCredit));
                            //今年度修得単位数
                            svf.doNumberingSvfOutNonZero("GET_CREDIT", gnum, String.valueOf(student._getCredit));
                        }
                        if (_knjdobj.hasJudgementItem()) {
                            // 前年度までの単位数を印字
                            
                            // 今年度認定単位数
                            svf.doNumberingSvfOutNonZero("A_CREDIT", gnum, String.valueOf(student._qualifiedCredits));
                            // 前年度までの修得単位数
                            svf.doNumberingSvfOutNonZero("PRE_C_CREDIT", gnum, String.valueOf(student._previousCredits));
                            // 修得単位数計
                            int t = student._getCredit + student._qualifiedCredits + student._previousCredits;
                            if (t != 0) {
                                int g = _knjdobj._gradCredits;  // 卒業認定単位数
                                if (g != 0 && g <= t) {
                                    svf.doNumberingSvfOut("TOTAL_C_CREDIT", gnum, "@" + String.valueOf(t));
                                } else {
                                    svf.doNumberingSvfOut("TOTAL_C_CREDIT", gnum, String.valueOf(t));
                                }
                            }
                            // 前年度までの未履修科目数
                            svf.doNumberingSvfOutNonZero("PRE_N_CREDIT", gnum, String.valueOf(student._previousMirisyu));
                        }
//                    if (0 < _specialAbsent) {
//                        svf.doNumberingSvfOutNonZero("SP_KEKKA", student._gnum, String.valueOf(student._specialAbsent));
//                    }
                        
                    }
                    
                    // 学級データの印字処理(学級平均)
                    //学級平均
                    if (null != hrInfo._avgHrTotal) { svf.VrsOut("TOTAL" + String.valueOf(_param._formMaxLine + 2), hrInfo._avgHrTotal); }
                    if (null != hrInfo._avgHrAverage) { svf.VrsOut("AVERAGE" + String.valueOf(_param._formMaxLine + 2), hrInfo._avgHrAverage); }
                    //欠点者数
                    if (null != hrInfo._failHrTotal) { svf.VrsOut("TOTAL" + String.valueOf(_param._formMaxLine + 4), hrInfo._failHrTotal); }
                    //最高点
                    if (null != hrInfo._maxHrTotal) { svf.VrsOut("TOTAL" + String.valueOf(_param._formMaxLine + 5), hrInfo._maxHrTotal); }
                    //最低点
                    if (null != hrInfo._minHrTotal) { svf.VrsOut("TOTAL" + String.valueOf(_param._formMaxLine + 6), hrInfo._minHrTotal); }
                    //出席率・欠席
                    if (null != hrInfo._perHrPresent) { svf.VrsOut("PER_ATTEND", DEC_FMT1.format(hrInfo._perHrPresent.setScale(1,BigDecimal.ROUND_HALF_UP))); }
                    if (null != hrInfo._perHrAbsent) { svf.VrsOut("PER_ABSENCE", DEC_FMT1.format(hrInfo._perHrAbsent.setScale(1,BigDecimal.ROUND_HALF_UP))); }
                    
                }  // 生徒別総合成績および出欠を印字
                
                printSubclass(svf, subclass, subi, studentList);
                _hasData = true;
            }
        }
    }

    private void printInkanran(final Vrw32alpWrap svf) {
        final File file = new File(_param._documentroot + "/image/KNJD615_keninwaku.jpg");
        String path = null;
        if (file.exists()) {
            path = file.getPath();
        } else {
            log.warn("file not found:" + file.getPath());
        }

        for (int i = 0; i < _param._jobMapList.size(); i++) {
            final Map jobMap = (Map) _param._jobMapList.get(i);
            final String jobname = (String) jobMap.get("JOBNAME");
            final String pos = String.valueOf(8 - i - (8 - _param._jobMapList.size()));
            //log.info(" pos = " + pos + " / " + jobname);
            svf.VrsOut("STAMP" + pos, path);
            svf.VrsOut("JOB" + pos, jobname);
        }
    }

    /**
     * 該当科目名および科目別成績等を印字する処理
     * @param svf
     * @param subclass
     * @param line：科目の列番号
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private void printSubclass(
            final Vrw32alpWrap svf,
            final SubClass subclass,
            final int line,
            final List studentList
    ) {
        if (null != subclass) {
            // 科目項目(教科名・科目名・)を印刷します。
            //教科名
            svf.VrsOut("course1", subclass._classabbv);
            //科目名
            if (subclass._electdiv) svf.VrAttribute("subject1", "Paint=(2,70,2),Bold=1");
            svf.VrsOut("subject1", subclass._subclassabbv);
            if (subclass._electdiv) svf.VrAttribute("subject1", ATTRIBUTE_CLEAR);
            //単位数
            if (0 != subclass._maxcredit) {
                if (subclass._maxcredit == subclass._mincredit) { svf.VrsOut("credit1",  String.valueOf(subclass._maxcredit)); }
                else  { svf.VrsOut("credit1",  String.valueOf(subclass._mincredit) + " \uFF5E " + String.valueOf(subclass._maxcredit)); }
            }
            //授業時数
            if (0 != subclass._jisu) { svf.VrsOut("lesson1",  String.valueOf(subclass._jisu)); }
            //学級平均・合計
            if (null != subclass._scoreaverage) { svf.VrsOut("AVE_CLASS",  subclass._scoreaverage); }
            if (null != subclass._scoresubaverage) { svf.VrsOut("AVE_SUBCLASS",  subclass._scoresubaverage); }
            if (null != subclass._scoretotal) { svf.VrsOut("TOTAL_SUBCLASS",  subclass._scoretotal + "/" + subclass._scoreCount); }
            if (null != subclass._scoreMax) { svf.VrsOut("MAX_SCORE",  subclass._scoreMax); }
            if (null != subclass._scoreMin) { svf.VrsOut("MIN_SCORE",  subclass._scoreMin); }
            if (null != subclass._scoreFailCnt) { svf.VrsOut("FAIL_STD",  subclass._scoreFailCnt); }
            //項目名
            svf.VrsOut("ITEM1", _knjdobj._item1Name);
            svf.VrsOut("ITEM2", _knjdobj._item2Name);
        }
        
        // 該当科目の生徒別成績を印字する処理
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            
            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                final int gnum = student._gnum;
                
//              int column = line % MAX_COLUMN + 1;
                ScoreValue sv;
                
                if (_knjdobj.isPrintScore()) {
                    // 素点
                    sv = detail._score;
                    if (null != sv) {
                        if (detail.isFail(_param, sv.getScoreAsInt())) {
                            svf.VrAttribute("SCORE"+ gnum,  ATTRIBUTE_FAIL);
                        }
                        svf.VrsOut("SCORE"+ gnum,  sv.getScore() );
                        if (detail.isFail(_param, sv.getScoreAsInt())) {
                            svf.VrAttribute("SCORE"+ gnum,  ATTRIBUTE_CLEAR);
                        }
                    }
                } else {
                    // 成績
                    sv = detail._patternAssess;
                    if (null != sv) {
                        if (detail.isFail(_param, sv.getScoreAsInt())) {
                            svf.VrAttribute("SCORE"+ gnum,  ATTRIBUTE_FAIL);
                        }
                        svf.VrsOut("SCORE"+ gnum,  sv.getScore() );
                        if (detail.isFail(_param, sv.getScoreAsInt())) {
                            svf.VrAttribute("SCORE"+ gnum,  ATTRIBUTE_CLEAR);
                        }
//                        if (knjdobj.doPrintCreditDrop() && 1 == sv.getScoreAsInt()) {
//                            svf.VrsOut("SCORE"+ gnum,  "*" + sv.getScore());
//                        } else {
//                            svf.VrsOut("SCORE"+ gnum,  sv.getScore());
//                        }
                    }
                }
                
                if (_param._outputAssessLevel) {
                    svf.VrsOut("kekka" + gnum, detail._assessLevel);
                } else {
                    // 欠課
                    if (null != detail._absent) {
                        final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                        final String field = _knjdobj.getSvfFieldNameKekka();
                        if (0 != value) {
                            if (detail._isOver) {
                                svf.VrAttribute(field+ gnum,  ATTRIBUTE_KEKKA_OVER);
                            }
                            svf.VrsOut(field+ gnum,  _param._absentFmt.format(detail._absent.floatValue()));
                            if (detail._isOver) {
                                svf.VrAttribute(field+ gnum,  ATTRIBUTE_CLEAR);
                            }
                        }
                    }
                }
            }

        }        

        svf.VrEndRecord();
    }
    
    /*
     *  PrepareStatement作成 --> 科目別平均の表
     */
//    private String sqlSubclassAverage() {
//        final StringBuffer stb = new StringBuffer();
//
//        stb.append(" SELECT  SUBCLASSCD");
//        stb.append("        ," + _knjdobj._fieldChaircd + " AS CHAIRCD");
//        stb.append("        ,ROUND(AVG(FLOAT(" + _knjdobj._fieldname + "))*10,0)/10 AS AVG_SCORE");
//        stb.append(" FROM RECORD_DAT W1");
//        stb.append(" WHERE YEAR = '" + _param._year + "'");
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
    private static abstract class KNJD065_COMMON {
        String _fieldname;
        String _fieldname2;
//        String _fieldChaircd;
        private final Param _param;
        protected final boolean _creditDrop;
        protected final String _semesterName;
        protected final String _semester;
        protected final String _testKindCd;
        protected String _testName;
        protected int _gradCredits;  // 卒業認定単位数
        protected String _item1Name;  // 明細項目名
        protected String _item2Name;  // 明細項目名
        protected String _item4Name;  // 総合点欄項目名
        protected String _item5Name;  // 平均点欄項目名

        public KNJD065_COMMON(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            _param = param;
            _creditDrop = _param._creditDrop;
            _testKindCd = _param._testKindCd;
            _semester = _param._semester;
            _semesterName = _param._semesterName;
            _gradCredits = getGradCredits(db2);
        }

        /** 欠課時数フィールド名は、欠課時数算定コードにより異なる。*/
        String getSvfFieldNameKekka() {
            String svfFieldNameKekka;
            int absent_cov = _param._definecode.absent_cov;
            if (absent_cov == 3 || absent_cov == 4) {
                svfFieldNameKekka = "kekka2_";
            } else {
                svfFieldNameKekka = "kekka";
            }
            return svfFieldNameKekka;
        }

        boolean doPrintCreditDrop() {
            return _creditDrop;
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d._patternAssess;
        }
        
        boolean isPrintScore() {
            return false;
        }

        /**
         * 欠点
         * 100段階：30未満
         *   5段階： 2未満
         */
        int getFailValue() {
            return 30;
        }

        /**
         * (帳票種別によって異なる)ページ見出し・項目・欄外記述を印刷します。
         * @param svf
         * @param paramap
         */
        abstract void printHeaderOther(Vrw32alpWrap svf);

        /**
         * ページ見出し・項目・欄外記述を印刷します。
         * @param svf
         * @param hrInfo
         */
        final void printHeader(
                final Vrw32alpWrap svf,
                final HRInfo hrInfo
        ) {
            svf.VrsOut("year2", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("ymd1", _param.getNow()); // 作成日
            svf.VrsOut("DATE", _param.getTermKekka());  // 欠課の集計範囲
            svf.VrsOut("DATE2", _param.getTermAttend());  // 「出欠の記録」の集計範囲

            svf.VrsOut("teacher", hrInfo._staffName);  //担任名
            svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
            if (hasCompCredit()) {
                svf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
            }
            svf.VrsOut("lesson20", hrInfo._HrMLesson);  // 授業日数

            svf.VrsOut("ITEM4", _item4Name);
            svf.VrsOut("ITEM5", _item5Name);
            svf.VrsOut("ITEM6", _item1Name + "・" + _item2Name);
            svf.VrsOut("ITEM7", _param.getRankName() + "順位");
            svf.VrsOut("ITEM8", "学年");

            // 一覧表枠外の文言
            svf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKA_OVER);
            svf.VrsOut("NOTE1",  " " );
            svf.VrsOut("NOTE2",  "：欠課時数超過者" );
            svf.VrAttribute("NOTE3",  ATTRIBUTE_FAIL);
            svf.VrsOut("NOTE3",  " " );
            svf.VrsOut("NOTE4",  "：欠点" );
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
                final DB2UDB db2
        ) {
            int gradcredits = 0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _param._year + "'";
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
        
        protected Param getParam() {
            return _param;
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

        protected void vrsOutMarkPrgid(Vrw32alpWrap svf) {
            svf.VrsOut("MARK", "/");
            svf.VrsOut("PRGID", _param._prgId);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private static class KNJD065_GRADE extends KNJD065_COMMON {
        public KNJD065_GRADE(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
            _fieldname = "GRAD_VALUE";
            _fieldname2 = null;
            _testName = "(評定)";
            _item1Name = "評定";
            _item2Name = param._outputAssessLevel ? "段階値" : "欠課";
            _item4Name = "評定合計";
            _item5Name = "評定平均";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf
        ) {
            svf.VrsOut("TITLE", _semesterName + "成績一覧表"); //成績名称
            svf.VrsOut("MARK1_2",  getParam()._assess.toString());
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
    private static class KNJD065_GAKKI extends KNJD065_GRADE {
        public KNJD065_GAKKI(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
            _fieldname = "SEM" + _semester + "_VALUE";
            _fieldname2 = null;
            _item1Name = "評価";
            _item2Name = param._outputAssessLevel ? "段階値" : "欠課";
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
    private static class KNJD062A_GRADE extends KNJD065_COMMON {
        public KNJD062A_GRADE(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
            _fieldname = "GRAD_VALUE";
            _fieldname2 = null;
            _testName = param._testItemName;
            _item1Name = "評定";
            _item2Name = param._outputAssessLevel ? "段階値" : "欠課";
            _item4Name = "評定総合点";
            _item5Name = "評定平均点";
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }

        void printHeaderOther(
                final Vrw32alpWrap svf
        ) {
            if (getParam().isGakunenSeiseki()) {
                svf.VrsOut("TITLE",  "  学年　成績一覧表");
            } else {
                svf.VrsOut("TITLE",  "  成績一覧表（評定）");
            }
            vrsOutMarkPrgid(svf);
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d._patternAssess;
        }
        
        boolean isPrintScore() {
            return false;
        }

        boolean isPrintAverage() {
            return false;
        }

        void loadAverage(final DB2UDB db2, final Map paramap) {}
        
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
    private static class KNJD062A_GRADE_HYOKA extends KNJD062A_GRADE {
        public KNJD062A_GRADE_HYOKA(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
            
            _item1Name = "評価";
            _item2Name = param._outputAssessLevel ? "段階値" : "欠課";
            _item4Name = "評価総合点";
            _item5Name = "評価平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Map paramap
        ) {
            svf.VrsOut("TITLE",  "  成績一覧表（評価）");
            vrsOutMarkPrgid(svf);
        }
    }

    /**
     * 学年成績の処理クラス
     */
    private static class KNJD062A_GRADE_HYOTEI extends KNJD062A_GRADE {
        public KNJD062A_GRADE_HYOTEI(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
        }

        /**
         * 欠点
         * 100段階：30未満
         *   5段階： 2未満
         */
        int getFailValue() {
            return 2;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private static class KNJD062A_GAKKI extends KNJD062A_GRADE {
        public KNJD062A_GAKKI(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
            _fieldname = "SEM" + _semester + "_VALUE";
            _fieldname2 = null;
            _item1Name = "評価";
            _item2Name = param._outputAssessLevel ? "段階値" : "欠課";
            _item4Name = "評価総合点";
            _item5Name = "評価平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf
        ) {
            svf.VrsOut("TITLE" , _semesterName + " 成績一覧表");
            vrsOutMarkPrgid(svf);
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
    private static class KNJD062A_INTER extends KNJD065_COMMON {

        KNJD062A_INTER(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
            _fieldname  = "SEM" + _semester + "_INTR_SCORE";
            _fieldname2 = "SEM" + _semester + "_INTR";
            _testName = param._testItemName;
            _item1Name = "素点";
            _item2Name = param._outputAssessLevel ? "段階値" : "欠課";
            _item4Name = "総合点";
            _item5Name = "平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf
        ) {
            svf.VrsOut("TITLE" , _semesterName + " " + _testName + " 成績一覧表");

            vrsOutMarkPrgid(svf);
        }

        private String getTitle() {
            final String one234;
            if ("1".equals(_semester)) {
                one234 = (getParam()._testKindCd).startsWith("01") ? "１" : "２";
            } else {
                one234 = (getParam()._testKindCd).startsWith("01") ? "３" : "４";
            }

            return one234 + "学期期末";
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d._score;
        }
        
        boolean isPrintScore() {
            return true;
        }

        boolean isPrintAverage() {
            return true;
        }

//        void loadAverage(final DB2UDB db2, final Map paramap) {
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
    private static class KNJD062A_TERM extends KNJD062A_INTER {
        KNJD062A_TERM(
                final DB2UDB db2,
                final Param param
        ) throws Exception {
            super(db2, param);
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
        private final String _hrclassCd;
        private String _staffName;
        private String _hrName;
        private final List _students = new LinkedList();
        private final Map _subclasses = new TreeMap();
//        private List _ranking;
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

        HRInfo(
                final String hrclassCd
        ) {
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
            loadScoreDetail(db2);
            createRanking();
//            _ranking = createRanking();
//            log.debug("RANK:" + _ranking);
            setSubclassAverage();        
            setHrTotal();  // 学級平均等の算出
            setHrTotalMaxMin();        
            setHrTotalFail();
            setSubclassGradeAverage(db2);
            if (_knjdobj.hasJudgementItem()) {
                loadPreviousCredits(db2);  // 前年度までの修得単位数取得
                loadPreviousMirisyu(db2);  // 前年度までの未履修（必須科目）数
                loadQualifiedCredits(db2);  // 今年度の資格認定単位数

                // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
                for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                    final Student student = (Student) itS.next();
                    student._isGradeGood = student.isGradeGood();
                    student._isGradePoor = student.isGradePoor();
                    student._isAttendPerfect = student.isAttendPerfect();
                    student._isKekkaOver = student.isKekkaOver();
                }
            }
        }

        private void loadHRClassStaff(
                final DB2UDB db2
        ) {
            final KNJ_Get_Info.ReturnVal returnval = _param._getinfo.Hrclass_Staff(
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
        private String sqlHrclassStdList(final String hrClass, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            if (!"9".equals(_param._semester)) {
                stb.append(    "AND W1.SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append(    "AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            }
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
            if (null != courseCd) {
                stb.append(    "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
            }
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudentsInfo(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdNameInfo();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._attendNo = rs.getString("ATTENDNO");
                            student._name = rs.getString("NAME");
                            student._transInfo = createTransInfo(rs);
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
        private String sqlStdNameInfo() {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + _param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
            stb.append("LEFT   JOIN SEMESTER_MST    W7 ON W7.YEAR = '" + _param._year + "' AND W7.SEMESTER = '9' ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE <= W7.EDATE) ");
            stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE >= W7.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                  "AND W5.TRANSFERCD IN ('1','2') ");
            stb.append(                                  "AND ( ");
            stb.append(                                  "      W7.SDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append(                                  " OR   W7.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append(                                  " OR   W7.SDATE <= W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE <= W7.EDATE ");
            stb.append(                                  "    ) ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append(    "AND W1.SCHREGNO = ? ");
            if (!_param._semester.equals("9")) {
                stb.append("AND W1.SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append("AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            }

            return stb.toString();
        }

        TransInfo createTransInfo(final ResultSet rs) throws SQLException {
            final String d1 = rs.getString("KBN_DATE1");
            if (null != d1) {
                final String n1 = rs.getString("KBN_NAME1");
                return new TransInfo(d1, n1);
            }

            final String d2 = rs.getString("KBN_DATE2");
            if (null != d2) {
                final String n2 = rs.getString("KBN_NAME2");
                return new TransInfo(d2, n2);
            }
            return new TransInfo(null, null);
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

        private void loadAttend(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;

            try {
                String targetSemes = _param._semester;
                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._semesFlg,
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        targetSemes,
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
                        _param._attendParamMap
                );
                log.debug(" attendSemesInState = " + _param._hasuuMap.get("attendSemesInState"));
                ps = db2.prepareStatement(sql);

                final ResultSet rs = ps.executeQuery();
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
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
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
            // 右側の評定総合点・評定平均点を算出。順位は空白。
            // 「9909：学年評定(5段階)」を選択した時の処理。TODO:熊本から問題視。とりあえずの仕様。
            if ("on".equals(_param._valueFlg)) {
                stb.append("SELECT  DECIMAL(ROUND(AVG(FLOAT(TBL1.TOTAL_SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(TBL1.TOTAL_AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
                stb.append(  "FROM  ( ");
                stb.append("SELECT  W3.SCHREGNO ");
                stb.append(       ",SUM(W3.VALUE) AS TOTAL_SCORE ");
                stb.append(       ",AVG(FLOAT(W3.VALUE)) AS TOTAL_AVG ");
                stb.append(  "FROM  RECORD_SCORE_DAT W3 ");
                stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append(   "AND  W3.SCORE_DIV = '00' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                stb.append("GROUP BY W3.SCHREGNO ");
                stb.append(  ") TBL1");
            } else {
                stb.append("SELECT  DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
                stb.append(  "FROM  RECORD_RANK" + _param.getTableNameDatorVDat() + " W3 ");
                stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
                stb.append(                "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            }

            return stb.toString();
        }

        private void loadRank(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdTotalRank();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._code);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._classRank = rs.getInt("CLASS_RANK");
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
            stb.append("         AND W1.SCHREGNO = ? ");
            stb.append(") ");

            // 右側の評定総合点・評定平均点を算出。順位は空白。
            // 「9909：学年評定(5段階)」を選択した時の処理。TODO:熊本から問題視。とりあえずの仕様。
            if ("on".equals(_param._valueFlg)) {
                stb.append("SELECT  W3.SCHREGNO ");
                stb.append(       ",cast(null as smallint) AS CLASS_RANK ");
                stb.append(       ",cast(null as smallint) AS TOTAL_RANK ");
                stb.append(       ",SUM(W3.VALUE) AS TOTAL_SCORE ");
                stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.VALUE))*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append(  "FROM  RECORD_SCORE_DAT W3 ");
                stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
                stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
                stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append(   "AND  W3.SCORE_DIV = '00' ");
                stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(   "AND  W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD NOT IN(");
                    stb.append(        "SELECT  R2.ATTEND_CLASSCD || '-' || R2.ATTEND_SCHOOL_KIND || '-' || R2.ATTEND_CURRICULUM_CD || '-' R2.ATTEND_SUBCLASSCD ");
                } else {
                    stb.append(   "AND  W3.SUBCLASSCD NOT IN( ");
                    stb.append(        "SELECT  R2.ATTEND_SUBCLASSCD ");
                }
                stb.append(          "FROM  SUBCLASS_REPLACE_COMBINED_DAT R2 ");
                stb.append(         "WHERE  R2.YEAR = '" + _param._year + "' ");
                stb.append(           "AND  R2.REPLACECD = '1' ");
                stb.append(   ") ");
                stb.append("GROUP BY W3.SCHREGNO ");
            } else {
                final boolean rankUseAvg = _param._useRankAverage;
                final boolean rankUseDev = _param._useRankDeviation;
                log.debug("rank_score_dat use avg_rank ? = " + rankUseAvg);
                log.debug("rank_score_dat use deviation_rank ? = " + rankUseDev);
                //メイン表
                stb.append("SELECT  W3.SCHREGNO ");
                if (rankUseAvg) {
                    stb.append(   ",CLASS_AVG_RANK AS CLASS_RANK");
                    if ("2".equals(_param._totalRank)) stb.append(   ",GRADE_AVG_RANK  AS TOTAL_RANK ");
                    if ("3".equals(_param._totalRank)) stb.append(   ",COURSE_AVG_RANK AS TOTAL_RANK ");
                } else if (rankUseDev) {
                    stb.append(   ",CLASS_DEVIATION_RANK AS CLASS_RANK");
                    if ("2".equals(_param._totalRank)) stb.append(   ",GRADE_DEVIATION_RANK  AS TOTAL_RANK ");
                    if ("3".equals(_param._totalRank)) stb.append(   ",COURSE_DEVIATION_RANK AS TOTAL_RANK ");
                } else {
                    stb.append(   ",CLASS_RANK ");
                    if ("2".equals(_param._totalRank)) stb.append(   ",GRADE_RANK  AS TOTAL_RANK ");
                    if ("3".equals(_param._totalRank)) stb.append(   ",COURSE_RANK AS TOTAL_RANK ");
                }
                stb.append(       ",W3.SCORE AS TOTAL_SCORE ");
                stb.append(       ",DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append(  "FROM  RECORD_RANK" + _param.getTableNameDatorVDat() + " W3 ");
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
                    stb.append(       ",cast(null as smallint) AS TOTAL_RANK ");
                    stb.append(       ",SUM(W3.SCORE) AS TOTAL_SCORE ");
                    stb.append(       ",DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS TOTAL_AVG ");
                    stb.append(  "FROM  RECORD_RANK" + _param.getTableNameDatorVDat() + " W3 ");
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
                    stb.append(          "FROM  RECORD_RANK" + _param.getTableNameDatorVDat() + " R1 ");
                    stb.append(         "WHERE  R1.YEAR = '" + _param._year + "' ");
                    stb.append(           "AND  R1.SEMESTER = '" + _param._semester + "' ");
                    stb.append(           "AND  R1.TESTKINDCD || R1.TESTITEMCD = '" + _param._testKindCd + "' ");
                    stb.append(           "AND  R1.SUBCLASSCD = '999999' ");
                    stb.append(           "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE R1.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                    stb.append(   ") ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append(   "AND  W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD NOT IN( ");
                        stb.append(        "SELECT  R2.ATTEND_CLASSCD || '-' || R2.ATTEND_SCHOOL_KIND || '-' || R2.ATTEND_CURRICULUM_CD || '-' || R2.ATTEND_SUBCLASSCD ");
                    } else {
                        stb.append(   "AND  W3.SUBCLASSCD NOT IN( ");
                        stb.append(        "SELECT  R2.ATTEND_SUBCLASSCD ");
                    }
                    stb.append(          "FROM  SUBCLASS_REPLACE_COMBINED_DAT R2 ");
                    stb.append(         "WHERE  R2.YEAR = '" + _param._year + "' ");
                    stb.append(           "AND  R2.REPLACECD = '1' ");
                    stb.append(   ") ");
                    stb.append("GROUP BY W3.SCHREGNO ");
                }
            }

            return stb.toString();
        }

        private void loadScoreDetail(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdSubclassDetail(_hrclassCd);
                log.debug(" subclass sql " + _hrclassCd + " = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (_knjdobj.enablePringFlg() && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String classCd = subclassCd == null ? "" : subclassCd.substring(1, 3);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        final ScoreDetail scoreDetail = createScoreDetail(rs,_subclasses);
                        student.add(scoreDetail);
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
            
            try {

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
                        _param._attendParamMap
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
                    
//                    final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    
//                    if (specialGroupCd != null) {
//                        final Integer specialAbsentMinutes = (Integer) rs.getObject("SPECIAL_SICK_MINUTES2");
//
//                        if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
//                            student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
//                        }
//                        final Integer minute = ((Integer) student._spGroupAbsentMinutes.get(specialGroupCd)).intValue();
//                        student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
//                    }
                    
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
                        scoreDetail._jisu = Integer.valueOf(rs.getString("LESSON"));
                        if (null != scoreDetail._jisu) {
                            if (0 != scoreDetail._jisu.intValue() && scoreDetail._subClass._jisu < scoreDetail._jisu.intValue()) scoreDetail._subClass._jisu = scoreDetail._jisu.intValue();
                        }
                        if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                            scoreDetail._absent = Double.valueOf(rs.getString("REPLACED_SICK"));
                        } else {
                            scoreDetail._absent = Double.valueOf(rs.getString("SICK2"));
                        }
                        scoreDetail._absenceHigh = new BigDecimal(StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "99"));
//                        final String absenceWarn = rs.getString("ABSENCE_WARN" + ("1".equals(_param._warnSemester) ? "" : _param._warnSemester));
//                        final String credits = rs.getString("CREDITS");
//                        if (_param._useAbsenceWarn && null != credits && null != absenceWarn) {
//                            scoreDetail._absenceHigh = scoreDetail._absenceHigh.subtract(new BigDecimal(Integer.parseInt(credits) * Integer.parseInt(absenceWarn)));
//                        }
                        scoreDetail._isOver = ScoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);
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
        private String sqlStdSubclassDetail(String hrclassCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
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
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + hrclassCd + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
                stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append(           " W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(        "AND W1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND W2.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     ")");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
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
                stb.append(           " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                   " T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO)");
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
                stb.append(           " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                 COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + _param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("             COMBINED_SUBCLASSCD");
            stb.append(") ");       
            
            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(    " W3.SUBCLASSCD AS SUBCLASSCD ");
            if (_param._testKindCd.equals("0101") || _param._testKindCd.equals("0201") || _param._testKindCd.equals("0202")) {
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
            if (_param._outputAssessLevel) {
                stb.append(       ",L1.ASSESSLEVEL ");
            } else {
                stb.append(       ",'' AS ASSESSLEVEL ");
            }
            stb.append(    "FROM    RECORD_RANK" + _param.getTableNameDatorVDat() + " W3 ");
            stb.append(    "INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append(    "   AND W1.LEAVE = 0 ");
            if (_param._outputAssessLevel) {
                final String assessLevelDiv = "1".equals(_param._totalRank) ? "2" : "3".equals(_param._totalRank) ? "3" : "1";
                stb.append("     LEFT JOIN ASSESS_LEVEL_MST L1 ON L1.YEAR = W3.YEAR ");
                stb.append("         AND L1.SEMESTER = W3.SEMESTER ");
                stb.append("         AND L1.TESTKINDCD = W3.TESTKINDCD ");
                stb.append("         AND L1.TESTITEMCD = W3.TESTITEMCD ");
                stb.append("         AND L1.SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append("         AND L1.DIV = '" + assessLevelDiv + "' ");
                stb.append("         AND L1.GRADE = W1.GRADE ");
                if ("2".equals(assessLevelDiv)) {
                    stb.append("         AND L1.HR_CLASS = W1.HR_CLASS ");
                    stb.append("         AND L1.COURSECD = '0' ");
                    stb.append("         AND L1.MAJORCD = '000' ");
                    stb.append("         AND L1.COURSECODE = '0000' ");
                } else if ("3".equals(assessLevelDiv)) {
                    stb.append("         AND L1.HR_CLASS = '000' ");
                    stb.append("         AND L1.COURSECD = W1.COURSECD ");
                    stb.append("         AND L1.MAJORCD = W1.MAJORCD ");
                    stb.append("         AND L1.COURSECODE = W1.COURSECODE ");
                } else {
                    stb.append("         AND L1.HR_CLASS = '000' ");
                    stb.append("         AND L1.COURSECD = '0' ");
                    stb.append("         AND L1.MAJORCD = '000' ");
                    stb.append("         AND L1.COURSECODE = '0000' ");
                }
                stb.append("         AND W3.SCORE BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ");
            }
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
            stb.append(     ") ");
            
            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(          " W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(           ",W3.COMP_CREDIT ,W3.GET_CREDIT ");
            stb.append(           ",CASE WHEN W3.VALUE IS NOT NULL THEN RTRIM(CHAR(W3.VALUE)) ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append("            W3.SCORE_DIV = '00' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //追試試験データの表
            stb.append(",SUPP_EXA AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
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

            if (_param.isRecordSlump()) {
                //成績不振科目データの表
                stb.append(",RECORD_SLUMP AS(");
                stb.append(    "SELECT  W3.SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(           " W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD, W3.SLUMP ");
                stb.append(    "FROM    RECORD_SLUMP_DAT W3 ");
                stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
                stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
                stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param.getRecordSlumpTestcd() + "' AND ");
                stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
                stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                stb.append(     ") ");
            }
            if (_param.isPerfectRecord()) {
                //満点マスタの表
                stb.append(" , PERFECT_T AS ( ");
                stb.append(" SELECT ");
                stb.append("     YEAR, ");
                stb.append("     SEMESTER, ");
                stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
                stb.append("     CLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(           " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
                }
                stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     MIN(DIV) AS DIV ");
                stb.append(" FROM ");
                stb.append("     PERFECT_RECORD_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._year + "' ");
                stb.append("     AND SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._testKindCd + "' ");
                stb.append(" GROUP BY ");
                stb.append("     YEAR, ");
                stb.append("     SEMESTER, ");
                stb.append("     TESTKINDCD || TESTITEMCD, ");
                stb.append("     CLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(           " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
                }
                stb.append("     SUBCLASSCD ");
                stb.append(" ), PERFECT_MAIN AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.* ");
                stb.append(" FROM ");
                stb.append("     PERFECT_RECORD_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     EXISTS( ");
                stb.append("         SELECT ");
                stb.append("             'x' ");
                stb.append("         FROM ");
                stb.append("             PERFECT_T E1 ");
                stb.append("         WHERE ");
                stb.append("             E1.YEAR = T1.YEAR ");
                stb.append("             AND E1.SEMESTER = T1.SEMESTER ");
                stb.append("             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ");
                stb.append("             AND E1.CLASSCD = T1.CLASSCD ");
                stb.append("             AND E1.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(           " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(                                " T1.SUBCLASSCD ");
                stb.append("             AND E1.DIV = T1.DIV ");
                stb.append("     ) ");
                stb.append(" ), SCH_PERFECT AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(           " L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     L1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     L1.PASS_SCORE ");
                stb.append(" FROM ");
                stb.append("     SCHNO_A T1 ");
                stb.append("     LEFT JOIN PERFECT_MAIN L1 ON L1.YEAR = T1.YEAR ");
                stb.append("          AND L1.GRADE = CASE WHEN L1.DIV = '01' THEN '00' ELSE T1.GRADE END ");
                stb.append("          AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN L1.DIV IN ('01','02') THEN '00000000' ELSE T1.COURSECD || T1.MAJORCD || T1.COURSECODE END ");
                stb.append(" WHERE ");
                stb.append("     T1.LEAVE = 0 ");
                stb.append(" ) ");
            }

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
            stb.append("        ,T3.ASSESSLEVEL ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSNAME  ");
            stb.append("        ,T7.SUBCLASSABBV ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            if (_param.isRecordSlump()) {
                stb.append("    ,K1.SLUMP ");
            } else {
                stb.append("    ,cast(null as varchar(1)) as SLUMP ");
            }
            if (_param.isPerfectRecord()) {
                stb.append("    ,K2.PASS_SCORE ");
            } else {
                stb.append("    ,cast(null as smallint) as PASS_SCORE ");
            }
            //対象生徒・講座の表
            stb.append(" FROM(");
            stb.append("     SELECT  W2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("             W2.SUBCLASSCD");
            stb.append("     FROM    CHAIR_A W2");
            if (!"9".equals(_param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("              W2.SUBCLASSCD");
            stb.append(" )T1 ");
            //成績の表
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,W3.SCORE,W3.PATTERN_ASSESS,W3.ASSESSLEVEL");
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
                stb.append(           " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" )T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" )T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(           " T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T7.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T8 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" T8.CLASSCD || '-' || T8.SCHOOL_KIND = ");
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append(" T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
            }
            if (_param.isRecordSlump()) {
                //成績不振科目データの表
                stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");
            }
            if (_param.isPerfectRecord()) {
                //満点マスタの表
                stb.append(" LEFT JOIN SCH_PERFECT K2 ON K2.SCHREGNO = T1.SCHREGNO AND K2.SUBCLASSCD = T1.SUBCLASSCD");
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
                int[] maxMin = Param.setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = maxMin[0];
                subclass._mincredit = maxMin[1];               
//                if (0 != credit) {
//                    if (subclass._maxcredit < credit) subclass._maxcredit = credit;
//                    if (credit < subclass._mincredit || 0 == subclass._mincredit) subclass._mincredit = credit;
//                }
                return subclass;
            }
            //科目クラスのインスタンスを作成して返す
            String classabbv = null;
            String subclassname = null;
            String subclassabbv = null;
            boolean electdiv = false;
            try {
                classabbv = rs.getString("CLASSNAME");
                subclassname = rs.getString("SUBCLASSNAME");
                subclassabbv = rs.getString("SUBCLASSABBV");
                if (null != rs.getString("ELECTDIV") && rs.getString("ELECTDIV").equals("1")) electdiv = true;
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            final SubClass subClass = new SubClass(subclasscode, classabbv, subclassname, subclassabbv, electdiv, credit);
            subclasses.put(subclasscode, subClass);
            return subClass;
        }

        private ScoreDetail createScoreDetail(
                final ResultSet rs,
                Map subclasses
        ) throws SQLException {
            final ScoreDetail detail = new ScoreDetail(
                    getSubClass(rs, subclasses),
                    ScoreValue.create(rs.getString("SCORE"),rs.getString("SUBCLASSCD")),
                    ScoreValue.create(rs.getString("PATTERN_ASSESS"),rs.getString("SUBCLASSCD")),
                    (Integer) rs.getObject("REPLACEMOTO"),
                    (String) rs.getObject("PRINT_FLG"),
                    (String) rs.getObject("SCORE_FLG"),
                    rs.getString("SLUMP"),
                    rs.getString("PASS_SCORE"),
                    rs.getString("ASSESSLEVEL"),
                    (Integer) rs.getObject("COMP_CREDIT"),
                    (Integer) rs.getObject("GET_CREDIT"),
                    (Integer) rs.getObject("CREDITS")
//                    rs.getString("CHAIRCD")
            );
            return detail;
        }

        // 前年度までの修得単位数計
        private void loadPreviousCredits(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdPreviousCredits();
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
         * @return
         */
        private String sqlStdPreviousCredits() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + _param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");

            return stb.toString();
        }

        // 前年度までの未履修（必須科目）数
        private void loadPreviousMirisyu(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdPreviousMirisyu();
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
         * @return
         */
        private String sqlStdPreviousMirisyu() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT COUNT(*) AS COUNT");
            stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
            stb.append(" INNER JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("            T1.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.SUBCLASSCD");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + _param._year + "'");
            stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
            stb.append("      OR T1.SCHOOLCD != '0')");
            stb.append("    AND VALUE(T2.ELECTDIV,'0') <> '1'");
            stb.append("    AND VALUE(T1.COMP_CREDIT,0) = 0");

            return stb.toString();
        }

        // 今年度の資格認定単位数
        private void loadQualifiedCredits(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdQualifiedCredits();
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
         * @return
         */
        private String sqlStdQualifiedCredits() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT SUM(T1.CREDITS) AS CREDITS");
            stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
            stb.append(" WHERE  T1.SCHREGNO = ?");
            stb.append("    AND T1.YEAR < '" + _param._year + "'");

            return stb.toString();
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
                final String total = student._scoreSum;
                if (null == total) continue;
                countT++;
                final int totalInt = Integer.parseInt(total);
                //最高点
                totalMax = Math.max(totalMax, totalInt);
                //最低点
                totalMin = Math.min(totalMin, totalInt);
//              log.debug("total="+total+", totalMax="+totalMax+", totalMin="+totalMin);
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
            final KNJD065_COMMON knjdobj = _knjdobj;
            final Map map = new HashMap();

            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
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
                        arr = new int[5];
                        map.put(subClass, arr);
                    }
                    arr[0] += val.getScoreAsInt();
                    arr[1]++;
                    //最高点
                    if (arr[2] < val.getScoreAsInt()) {
                        arr[2] = val.getScoreAsInt();
                    }
                    //最低点
                    if (arr[3] > val.getScoreAsInt() || arr[1] == 1) {
                        arr[3] = val.getScoreAsInt();
                    }
                    //欠点（赤点）
                    if (detail.isFail(_param, val.getScoreAsInt())) {
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
                    mlesson += attend._mLesson;
                    present += attend._present;
                    absent += attend._absent;
                    arrj = Param.setMaxMin(arrj[0], arrj[1], attend._mLesson);
                }
                arrc = Param.setMaxMin(arrc[0], arrc[1], student._compCredit);
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
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(DB2UDB db2) {
            final String sql = sqlSubclassGradeAvg();
            //log.debug(" gradeAverage sql = " + sql);
            try {
                PreparedStatement ps = db2.prepareStatement(sql);
                
                final ResultSet rs = ps.executeQuery();
                
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
            }catch (SQLException e) {
                log.debug("exception!", e);
            }
        }
        
        /** 科目の学年平均をもとめるSQL */
        private String sqlSubclassGradeAvg() {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    T2.ELECTDIV, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE" + _param.getTableNameDatorVDat() + " T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T1.SUBCLASSCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "'");
            stb.append("    AND T1.AVG_DIV = '1' ");
            stb.append("    AND T1.GRADE = '" + _param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '999999' ");
            return stb.toString();
        }

        /**
         * 順位の算出
         */
        private List createRanking() {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(student);
//                final Total total = student._total;
//                if (0 < total._count) {
//                    list.add(total);
//                }
            }

//            Collections.sort(list);
            return list;
        }

//        int rank(final Student student) {
//            final Total total = student._total;
//            if (0 >= total._count) {
//                return -1;
//            }
//            return 1 + _ranking.indexOf(total);
//        }

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
    private class TransInfo {
        private final String _date;
        private final String _name;

        TransInfo(
                final String date,
                final String name
        ) {
            _date = date;
            _name = name;
        }

        public String toString() {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _date) {
                sb.append(KNJ_EditDate.h_format_JP(_date));
            }
            if (null != _name) {
                sb.append(_name);
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
        private int _classRank;
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
//        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠課時分
//        private int _specialAbsent; // 特活欠課時数

        Student(final String code, final HRInfo hrInfo, final int gnum) {
            _gnum = gnum;
            _code = code;
            _hrInfo = hrInfo;
        }

        void add(final ScoreDetail scoreDetail) { _scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail); }

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

//        int rank() {
//            return _hrInfo.rank(this);
//        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

        /**
         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
         */
        boolean isGradeGood() {
            final BigDecimal avg = _total._avgBigDecimal;
            if (null == avg) { return false; }
            Float ac = _param._assess;
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
                if (null == val || !val.hasIntValue()) continue;
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
        private final String _classabbv;
        private final String _classcode;
        private final String _subclasscode;
        private final String _subclassname;
        private final String _subclassabbv;
        private final boolean _electdiv; // 選択科目
        private int _gnum;  // 行番号
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private int _jisu;  // 授業時数
        private String _scoreaverage;  // 学級平均
        private String _scoresubaverage;  // 学年平均
        private String _scoretotal;  // 学級合計
        private String _scoreCount;  // 学級人数
        private String _scoreMax;  // 最高点
        private String _scoreMin;  // 最低点
        private String _scoreFailCnt;  // 欠点者数
        

        SubClass(
                final String subclasscode, 
                final String classabbv, 
                final String subclassname,
                final String subclassabbv,
                final boolean electdiv,
                final int credit
        ) {
            _classabbv = classabbv;
            // (subclasscodeは頭1桁+科目コードなので教科コードは2文字目から2桁)
            _classcode = subclasscode.substring(1, 3); 
            _subclasscode = subclasscode;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
        }

//        public String toString() {
//            return _subclasscode + ":" + _abbv;
//        }
        
        public String getSubclasscd() {
            return _subclasscode.substring(1);
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
            return "["+_classabbv + " , " +_subclasscode + " , " +_subclassabbv + " , " +_electdiv + " , " +_gnum + " , " +_maxcredit + " , " +_mincredit + " , " +_jisu +"]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<素点・評定データのクラスです>>。
     */
    private static class ScoreValue {
        private final String _strScore;
        private int _val;

        ScoreValue(final String strScore) {
            _strScore = strScore;
            if (hasIntValue()) {
                _val = Integer.parseInt(_strScore);
            }
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
            // if (KNJDefineSchool.subject_T.equals(classcd.substring(1, 3))) return null;
            return new ScoreValue(strScore);
        }

        String getScore() { return _strScore; }
        boolean hasIntValue() { return !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore); }
        int getScoreAsInt() { return _val; }
        
        public String toString() {
            return _strScore;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
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
        private final String _slump;
        private final String _passScore;
        private final String _assessLevel;

        ScoreDetail(
                final SubClass subClass,
                final ScoreValue score,
                final ScoreValue patternAssess,
                final Integer replacemoto,
                final String print_flg,
                final String score_flg,
                final String slump,
                final String passScore,
                final String assessLevel,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits
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
            _slump = slump;
            _passScore = passScore;
            _assessLevel = assessLevel;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private static boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
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

        private int getFailValue(final Param param) {
            if (param.isPerfectRecord() && null != _passScore) {
                return Integer.parseInt(_passScore);
            } else if (param.isKetten() && null != param._ketten && !"".equals(param._ketten)) {
                return Integer.parseInt(param._ketten);
            }
            return -1;
        }

        boolean isFail(final Param param, int score) {
            if (param.isRecordSlump()) {
                return "1".equals(_slump);
            } else {
                return score < getFailValue(param);
            }
        }

//        /**
//         * 素点を印刷します。
//         * @param svf
//         * @param gnum 印刷する行番号
//         * @param column 印刷する列番号
//         */
//        private void printSoten(
//                final Vrw32alpWrap svf, 
//                final int gnum, 
//                final int column
//        ) {
//            if (null == _score) { return; }
//            if (null != _score_flg) {
//                svf.VrAttribute("SCORE"+ gnum,  ATTRIBUTE_FAIL);
//            }
//            svf.VrsOut("SCORE"+ gnum,  _score.getScore() );
//            if (null != _score_flg) {
//                svf.VrAttribute("SCORE"+ gnum,  ATTRIBUTE_CLEAR);
//            }
//        }

//        /**
//         * 単位を印刷します。
//         * @param svf
//         * @param gnum 印刷する行番号
//         * @param column 印刷する列番号
//         */
//        private void printTani(
//            final Vrw32alpWrap svf, 
//            final int gnum, 
//            final int column
//        ) {
//            Integer credit;
//            if (_knjdobj.isGakunenMatu()) {
//                credit = _getCredit;
//            } else {
//                credit = _credits;
//            }
//            if (null == credit) { return; }
//            if (null != _replacemoto && 1 <= _replacemoto.intValue()) {
//                svf.VrsOut("rate"+ gnum,  "(" + credit.toString() + ")");
//            } else {
//                svf.VrAttribute("rate"+ gnum,  "Hensyu=3");
//                svf.VrsOut("rate"+ gnum,  credit.toString());
//            }
//        }
        
        public String toString() {
            return (_subClass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _patternAssess + " , " + _replacemoto + " , " + _print_flg + " , " + _score_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isOver);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private class Total implements Comparable {
        private int _total;  // 総合点
        private int _count;  // 件数（成績）
        private BigDecimal _avgBigDecimal;  // 平均点
        private int _countFail;  //欠点科目数

        /**
         * コンストラクタ。
         * @param student
         */
        Total(final Student student) {
            compute(student);
        }

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
         */
        private void compute(final Student student) {
            final KNJD065_COMMON knjdObj = _knjdobj;

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            int countFail = 0;
            
            for (final Iterator it = student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                final ScoreValue scoreValue = knjdObj.getTargetValue(detail);
                if (isAddTotal(scoreValue, detail._replacemoto, knjdObj)) {
                    total += scoreValue.getScoreAsInt();
                    count++;                    
                    if (detail.isFail(_param, scoreValue.getScoreAsInt())) countFail++;
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
            
//            int specialAbsent = 0;
//            for (final Iterator it = _student._spGroupAbsentMinutes.values().iterator(); it.hasNext();) {
//                final Integer groupAbsentMinutes = (Integer) it.next();
//                specialAbsent += getSpecialAttendExe(groupAbsentMinutes.intValue());
//            }

            _total = total;
            _count = count;
            if (0 < count) {
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            }
            if (0 < compCredit) { student._compCredit = compCredit; }
            if (0 < getCredit) { student._getCredit = getCredit; }
//            _student._specialAbsent = specialAbsent;
            _countFail = countFail;
        }
        
//        /**
//         * 欠課時分を欠課時数に換算した値を得る
//         * @param kekka 欠課時分
//         * @return 欠課時分を欠課時数に換算した値
//         */
//        private int getSpecialAttendExe(final int kekka) {
//            final int jituJifun = (_param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(_param._knjSchoolMst._jituJifunSpecial);
//            final BigDecimal bigKekka = new BigDecimal(kekka);
//            final BigDecimal bigJitu = new BigDecimal(jituJifun);
//            BigDecimal bigD = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN);
//            String retSt = bigD.toString();
//            final int retIndex = retSt.indexOf(".");
//            int seisu = 0;
//            if (retIndex > 0) {
//                seisu = Integer.parseInt(retSt.substring(0, retIndex));
//                final int hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
//                seisu = hasu < 5 ? seisu : seisu + 1;
//            } else {
//                seisu = Integer.parseInt(retSt);
//            }
//            return seisu;
//        }
        
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
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;
        
        final String _cmd;

        /** 学年 */
        final String _grade;

        /** 出欠集計日付 */
        final String _date;

        final String _testKindCd;

        final String _valueFlg;

        final String _scoreFlg;

        final String[] _classSelected;  //印刷対象HR組
        final String[] _jobSelected; // 検印職

        /** 総合順位出力 1:学級 2:学年 3:コース */
        final String _totalRank;

        /** 基準点としてRECORD_RANK_DATで*_AVG_RANKを使用するか */
        final boolean _useRankAverage;

        /** 基準点としてRECORD_RANK_DATで*_DEVIATION_RANKを使用するか */
        final boolean _useRankDeviation;

        /** 欠点 */
        final String _ketten;

        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
        final String _checkKettenDiv;

        /** 起動元のプログラムＩＤ */
        final String _prgId;

        /** 成績優良者評定平均の基準値===>KNJD615：未使用 */
        final Float _assess;

        /** フォーム（1:４５名、2:５０名）*/
        final String _formSelect;
        final int _formMaxLine;
        
        /** 科目数　（1:15科目、2:20科目) */
        final String _subclassMax;
        final int _formMaxColumn;
        
        /** 単位保留 */
        final boolean _creditDrop;

        /** 欠番を詰める */
        final boolean _noKetuban;

        /** 同一クラスでのコース毎に改頁あり */
        final boolean _outputCourse;
        
        /** "総合的な学習の時間"を表示しない */
        final boolean _notOutputSougou;
        
        /** 段階値を表示する */
        final boolean _outputAssessLevel;
        
        final String _documentroot;
        
        private String _schoolName;
        
        private String FORM_FILE;

        private String _yearDateS;
        private String _divideAttendDate;
        private String _divideAttendMonth;
        private String _semesMonth;
        private String _semesterName;
        private String _semesterDateS;
        private String _testItemName;
        
        private static final String FROM_TO_MARK = "\uFF5E";
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        
        /** 端数計算共通メソッド引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";

        private KNJSchoolMst _knjSchoolMst;
        private KNJ_Get_Info _getinfo;

        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        private final Map _attendParamMap;
        private final List _jobMapList;
        private final DecimalFormat _absentFmt;

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            final String testKindCdTemp = request.getParameter("TESTKINDCD");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _cmd = request.getParameter("cmd");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _jobSelected = request.getParameterValues("JOB_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _testKindCd = "0".equals(testKindCdTemp) ? "0000" : "9909".equals(testKindCdTemp) ? "9900" : testKindCdTemp;  //テスト・成績種別
            _valueFlg = "9909".equals(testKindCdTemp) ? "on" : "off";  //record_score_dat.valueを出力するか？
            _totalRank = request.getParameter("OUTPUT_RANK");
            _scoreFlg = request.getParameter("SCORE_FLG");
            _creditDrop = (request.getParameter("OUTPUT4") != null);
            _noKetuban = ( request.getParameter("OUTPUT5") != null );
            _outputCourse = (request.getParameter("OUTPUT_COURSE") != null);
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT_SOUGOU"));
            _assess = (request.getParameter("ASSESS") != null) ? new Float(request.getParameter("ASSESS1")) : new Float(4.3);
            _formSelect = request.getParameter("FORM_SELECT");
            _formMaxLine = "1".equals(_formSelect) ? 45 : 50 ;
            _subclassMax = request.getParameter("SUBCLASS_MAX");
            _formMaxColumn = "1".equals(_subclassMax) ? 15 : 20;
            _useRankAverage = "2".equals(request.getParameter("OUTPUT_KIJUN"));
            _useRankDeviation = "3".equals(request.getParameter("OUTPUT_KIJUN"));
            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _prgId = request.getParameter("PRGID");
            _outputAssessLevel = (request.getParameter("ASSESS_LEVEL") != null);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _documentroot = request.getParameter("DOCUMENTROOT");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _getinfo = new KNJ_Get_Info();
            load(db2);
            getParam2(db2, _getinfo);  //Map要素設定
            String str = "なし";
            if (isRecordSlump())     str = "RECORD_SLUMP_DAT";
            if (isPerfectRecord())   str = "PERFECT_RECORD_DAT";
            if (isKetten())          str = "指示画面の欠点";
            log.debug("欠点参照：" + str);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useCurriculumcd", _useCurriculumcd);
            _attendParamMap.put("useVirus", _useVirus);
            _attendParamMap.put("useKoudome", _useKoudome);
            _attendParamMap.put("DB2UDB", db2);
            
            _jobMapList = getJobList(db2);
            _absentFmt = getAbsentFmt();
        }
        
        DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                absentFmt = new DecimalFormat("0");
                break;
            default:
                absentFmt = new DecimalFormat("0.0");
            }
            return absentFmt;
        }

        public String getFormFile() {
//            if (isKumamoto() || isChiben()) {
                if ("1".equals(_formSelect)) {
                    return "1".equals(_subclassMax) ? "KNJD615B_2.frm" : "KNJD615B_4.frm";
                } else {
                    return "1".equals(_subclassMax) ? "KNJD615B_1.frm" : "KNJD615B_3.frm";
                }
//            } else {
//                if ("1".equals(_formSelect)) {
//                    return "1".equals(_subclassMax) ? "KNJD615_6.frm" : "KNJD615_8.frm";
//                } else {
//                    return "1".equals(_subclassMax) ? "KNJD615_5.frm" : "KNJD615_7.frm";
//                }
//            }
        }
        
        private List getJobList(final DB2UDB db2) throws SQLException {
            final List list = new ArrayList();
            if (null != _jobSelected) {
                for (int i = 0; i < _jobSelected.length; i++) {
                    final String jobcd = _jobSelected[i];
                    
                    final String sql = " SELECT JOBCD, JOBNAME FROM JOB_MST WHERE JOBCD = '" + jobcd + "' ORDER BY JOBCD ";
                    PreparedStatement ps = db2.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        final Map m = new HashMap();
                        m.put("JOBCD", rs.getString("JOBCD"));
                        m.put("JOBNAME", rs.getString("JOBNAME"));
                        list.add(m);
                    }
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                    
                    if (list.size() >= 8) { // 最大8個まで
                        break;
                    }
                }
            }
            return list;
            
        }

        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }
        
        private String getDateE() {
            return KNJ_EditDate.h_format_JP(_date);
        }

        /** 欠課の集計範囲 */
        public String getTermKekka() {
            return KNJ_EditDate.h_format_JP(_yearDateS) + FROM_TO_MARK + getDateE();
        }
        
        /** 「出欠の記録」の集計範囲 */
        public String getTermAttend() {
            return KNJ_EditDate.h_format_JP(_semesterDateS) + FROM_TO_MARK + getDateE();
            
        }

        /** RECORD_SLUMP_DATを参照するテストコード */
        public String getRecordSlumpTestcd() {
            if (isKyoto()) {
                if ( "1".equals(_semester) && "9901".equals(_testKindCd)) return "0201";
                if ( "2".equals(_semester) && "9901".equals(_testKindCd)) return "0102";
                if (!"9".equals(_semester) && "9900".equals(_testKindCd)) return "0201";
            }
            return _testKindCd;
        }

        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        public boolean isRecordSlump() {
            return "1".equals(_checkKettenDiv) && !"9".equals(_semester);
        }

        /** 欠点対象：満点マスタ(PERFECT_RECORD_DAT)の合格点(PASS_SCORE)を参照して判断するか */
        public boolean isPerfectRecord() {
            return "2".equals(_checkKettenDiv) && !isGakunenHyotei();
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        public boolean isKetten() {
            return !isRecordSlump() && !isPerfectRecord();
        }
        
        public boolean isGakunenHyotei() {
            return "9".equals(_semester) && "9900".equals(_testKindCd) && "2".equals(_scoreFlg);
        }

        public boolean isHousei() {
            return _schoolName.equals("HOUSEI");
        }

        public boolean isChiben() {
            return "CHIBEN".equals(_schoolName);
        }

        public boolean isChiben9909() {
            return isChiben() && "9909".equals(_testKindCd);
        }
        
        public boolean isKumamoto() {
            return "kumamoto".equals(_schoolName);
        }
        
        public boolean isKyoto() {
            return "kyoto".equals(_schoolName);
        }
        
        public boolean isGakunenSeiseki() {
            return "9".equals(_semester) && "9900".equals(_testKindCd) && "1".equals(_scoreFlg);
        }

        public String getTableNameDatorVDat() {
            if (isGakunenSeiseki()) return "_V_DAT";
            return "_DAT";
        }

        public void load(DB2UDB db2) {
            _schoolName = getSchoolName(db2);
            loadAttendSemesArgument(db2);
            _definecode = createDefineCode(db2);
        }
        
        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }
        
        /**
         *  パラメータセット 2005/01/29
         *      _divideAttendDate :attend_semes_datの最終集計日の翌日をセット
         *      _divideAttendMonth:attend_semes_datの最終集計学期＋月をセット
         *  2005/02/20 Modify getDivideAttendDateクラスより取得
         */
        public void getParam2(
                final DB2UDB db2,
                final KNJ_Get_Info getinfo
        ) {
            final KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _divideAttendDate = obj.date;  //最終集計日の翌日
            _divideAttendMonth = obj.month; //最終集計学期＋月
            _semesMonth = KNJC053_BASE.retSemesterMonthValue(_divideAttendMonth);
            
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = getinfo.Semester(db2, _year, _semester);
            _semesterName = returnval.val1;  //学期名称
            
            // 学期期間FROM
            if (null == returnval.val2) {
                _semesterDateS = _year + "-04-01";
            } else {
                _semesterDateS = returnval.val2;
            }
            
            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = getinfo.Semester(db2, _year, "9");
            _yearDateS = returnval1.val2;
            
            // テスト名称
            final String testitemname = getTestName(db2, _year, _semester, _testKindCd);
            _testItemName = testitemname;
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
        
        /** 総合順位出力の順位欄項目名 */
        private String getRankName() {
            if ("1".equals(_totalRank)) {
                return "学級";
            } else if ("2".equals(_totalRank)) {
                return "学年";
            } else {
                return "コース";
            }
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

        private void loadAttendSemesArgument(DB2UDB db2) {
            
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2, _year);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
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
            
        private String sqlSemester(String year) {
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
        
        private String getSchoolName(final DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolName = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
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
            return new int[]{maxInt, minInt};
        }
    }
}
