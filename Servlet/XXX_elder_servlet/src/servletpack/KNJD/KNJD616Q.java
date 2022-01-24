/*
 * $Id: 12a4cae7f89492d0801f24e7aec4ae428d563bbc $
 *
 * 作成日: 2013/07/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD616Q {

    private static final Log log = LogFactory.getLog(KNJD616Q.class);

    private static final String FROM_TO_MARK = "\uFF5E";

    private static final int _charsPerColumn = 2; // 1列あたりの文字数
    private static final int MAX_COLUMN1 = 8;
    private static final String ALL_SEME = "9";
    private static final String HYOTEI_TESTCD = "9990009";

    private static final String KARIHYOUTEI_SCORE_DIV = "09";

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
            if (null != _param) {
                DbUtils.closeQuietly(_param._ps1);
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

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (s != null) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private static String getByteSubstring(final String str, final int keta, final int range) {
        if (null == str) {
            return null;
        }
        int totalbytelen = 0;
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            final String ch = str.substring(i, i + 1);
            totalbytelen += getMS932ByteLength(ch);
            if (keta < totalbytelen && totalbytelen <= keta + range) {
                stb.append(ch);
            }
        }
        return stb.toString();
    }
    
    private List groupList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student s = (Student) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            s._gyo = current.size() + 1;
            current.add(s);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int lineMax = 50;
        final String form1 = "KNJD616Q.frm";

        for (final Iterator itsub = _param._subclassList.iterator(); itsub.hasNext();) {
            final Subclass subclass = (Subclass) itsub.next();
            
            final List testKindItemList = _param.getTestKindItemList(subclass.getKeySubclassCd());
            final List hrclassList = getHrclassList(db2, _param, subclass);

            if (hrclassList.size() > 0) {
                log.info(" subclasscd = " + subclass.getKeySubclassCd() + ", hrclassList size = " + hrclassList.size() + ", testcds = " + testKindItemList);
            }

            for (final Iterator ith = hrclassList.iterator(); ith.hasNext();) {
                final Hrclass hrclass = (Hrclass) ith.next();
                
                for (final Iterator sit = groupList(hrclass._studentList, lineMax).iterator(); sit.hasNext();) {
                    final List studentList = (List) sit.next();
                    
                    svf.VrSetForm(form1, 4);
                    
                    printHeader(svf, subclass, null, hrclass, studentList, hrclass._studentList);
                    
                    int column = 0;
                    // 成績
                    for (int i = 0, colsize = testKindItemList.size(); i < colsize; i++) {
                        final TestItem testItem = (TestItem) testKindItemList.get(i);
                        printColumn1(svf, subclass, studentList, hrclass._studentList, "成績", i, testItem, colsize, column, testKindItemList);
                        column += 1;
                        svf.VrEndRecord();
                    }
                    
//                // 仮評定
//                final List testKindItemKariHyouteiList = _param._kariHyouteiList;
//                for (int i = 0, colsize = testKindItemKariHyouteiList.size(); i < colsize; i++) {
//                    final TestItem testItem = (TestItem) testKindItemKariHyouteiList.get(i);
//                    printColumn5(svf, chair._studentList, subclass, "仮評定", i, testItem, colsize);
//                    column += 1;
//                    svf.VrEndRecord();
//                }
                    
                    if (column < MAX_COLUMN1 * 1) {
                        // 備考
                        final int bikoLen = MAX_COLUMN1 * 1 - column;
                        if (bikoLen > 2) {
                            for (int i = 0; i < bikoLen; i++) {
                                // log.debug(" biko " + column);
                                column += 1;
                                svf.VrEndRecord();
                            }
                        }
                    }
                    
//                    printLine(svf);
//                    column += 1;
//                    svf.VrEndRecord();
                }
            }
        }
        
    }
    
    private static String center(final String text, final int columnSize) {
        final boolean isOdd = (columnSize * _charsPerColumn - StringUtils.defaultString(text).length()) % 2 == 1;
        return StringUtils.center(text, columnSize * _charsPerColumn - (isOdd ? 1 : 0), "　") + (isOdd ? "　" : "");
    }
    
    private static String mkString(final List list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String c = "";
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String s = (String) it.next();
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            stb.append(c).append(s);
            c = comma;
        }
        return stb.toString();
    }

    private void printHeader(final Vrw32alp svf, final Subclass subclass, final Chair chair, final Hrclass hrclass, final List studentList, final List studentAllList) {
        final String gakunen = StringUtils.defaultString(_param._gradeName1) + "学年";
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + gakunen + "　" + StringUtils.defaultString(subclass._subclassname) + "　評定一覧表（クラス番号順）"); // タイトル
//        svf.VrsOut("SUBCLASSCD", "(" + subclass.getKeySubclassCd() + ")"); // 科目コード
//        final String kuroMaru = subclass._isSaki ? "●" : "";
//        svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "")  , kuroMaru + subclass._subclassname); // 科目名
//        svf.VrsOut("PRINTDAY", _param._now); // 印刷日
//        svf.VrsOut("CLASS_TNAME", "教科担任氏名"); // 教科担当職名名称

//        if ("1".equals(_param._printTannin)) {
//            final String staffName = mkString(chair._chairStfNameList, "、");
//            svf.VrsOut("CLASS_TEACHER" + (getMS932ByteLength(staffName) > 40 ? "2" : ""), staffName); // 教科担任名
//        } else {
//            svf.VrsOut("INKAN1", "印");
//            svf.VrsOut("INKAN_MARU1", "○");
//            svf.VrsOut("INKAN2", "印");
//            svf.VrsOut("INKAN_MARU2", "○");
//            if (chair._chairStfNameList.size() > 0) {
//                final String staffName = (String) chair._chairStfNameList.get(0);
//                svf.VrsOut("CLASS_TEACHER" + (getMS932ByteLength(staffName) > 20 ? "2" : ""), staffName); // 教科担任名
//            }
//        }
        
//        String course = "";
//        if ("1".equals(_param._printDiv)) {
//            svf.VrsOut("HR_TNAME", "学級担任氏名"); // 学級担任職名名称
//            svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrclass._hrStfStfName) > 20 ? "2" : ""), hrclass._hrStfStfName); // 学級担任名
//            final String hrName = StringUtils.defaultString(hrclass._hrName);
//            String majorname = "";
//            if (!studentAllList.isEmpty()) {
//                final Student student = (Student) studentAllList.get(0);
//                majorname = StringUtils.defaultString(student._majorname);
//            }
//            course = majorname + "　" + hrName;
//        } else {
//            course = "講座名　：" + StringUtils.defaultString(chair._chairname);
//        }
//        svf.VrsOut("COURSE",   course); // コース年組講座名
        
        svf.VrsOut("NO_TITLE", "年組番"); // 番号タイトル

        // 生徒氏名
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            // svf.VrsOutn("NO", student._gyo, "1".equals(_param._printDiv) ? student.getAttendNoStr() : student.getGyoNoStr()); // 年組番号
            svf.VrsOutn("NO", student._gyo, student.getAttendNoStr()); // 年組番号
            final int len = getMS932ByteLength(student._name);
            if ("1".equals(_param._use_SchregNo_hyoji)) {
                svf.VrsOutn("NAME" + (len > 30 ? "3" : len > 20 ? "2" : "1") + "_2", student._gyo, student._name); // 氏名
                svf.VrsOutn("SCHREGNO", student._gyo, student._schregno); // 学籍番号
            } else  {
                svf.VrsOutn("NAME" + (len > 30 ? "3" : len > 20 ? "2" : "1"), student._gyo, student._name); // 氏名
            }
        }
//        final List hyoteiList = new ArrayList();
//        for (int i = 0; i < studentAllList.size(); i++) {
//            final Student student = (Student) studentAllList.get(i);
//            final String hyotei = student.getScore(subclass, HYOTEI_TESTCD);
//            if (null != hyotei) {
//                hyoteiList.add(hyotei);
//            }
//        }
    }

    private static String take(final String name, final int count) {
        return null == name || name.length() < count ? name : name.substring(0, count);
    }

    // 成績
    private void printColumn1(final Vrw32alp svf, final Subclass subclass, final List studentlist, final List studetAllList, final String text, int i, final TestItem testItem, final int colsize, final int column, final List testKindItemList) {
//        final boolean last = isMaxColumn(column) || i == colsize - 1;
//        final boolean start = testItem._semester.getTestItemIdx(testItem) == 0;
//        final String suf1 = start && last ? "_4" : last ? "_3" : start ? "" : "_2";
//        final String suf2 = start && last ? "_4" : last ? "_3" : start ? "_1" : "_2";
//        final String title = center(text, colsize);
//        svf.VrsOut("SUBTITLE1" + suf1, title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 成績
//        final String fieldSem;
//        final int keta;
//        if (testItem._semester._semestername.length() <= testItem._semester._testItemList.size() * 2) {
//            keta = 2;
//            fieldSem = "SEM1" + (start && last ? "_7" : last ? "_5" : start ? "_1" : "_3");
//        } else {
//            keta = 3;
//            fieldSem = "SEM1" + (start && last ? "_8" : last ? "_6" : start ? "_2" : "_4");
//        }
//        final int nameIdx = testItem._semester.getTestItemIdx(testItem) * keta;
//        if (nameIdx < testItem._semester._testItemList.size() * keta) {
//            svf.VrsOut(fieldSem, StringUtils.center(testItem._semester._semestername, testItem._semester._testItemList.size() * keta,'　').substring(nameIdx)); // 学期
//        }
        if (null != testItem._gokeiHeikin) {
            svf.VrsOut("TESTNAME", take(testItem._gokeiHeikin, 5)); // 素点・評価他
        } else {
            svf.VrsOut("TESTNAME", take(testItem._testitemabbv1, 5)); // 素点・評価他
        }
        // log.debug(" ITEM1" + (suf1.length() == 0 ? "  " : suf1) + " = " + testItem._semester._semestername + ":" + testItem._testitemabbv1);

        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            final String score = student.getScore(subclass, testItem);
            if ("合計点".equals(testItem._gokeiHeikin) || "平均点".equals(testItem._gokeiHeikin)) {
                final List scoreList = new ArrayList();
                for (int k = 0; k < testKindItemList.size(); k++) {
                    final TestItem ti = (TestItem) testKindItemList.get(k);
                    if (!HYOTEI_TESTCD.equals(ti.getTestcd())) {
                        final String score1 = student.getScore(subclass, ti);
                        if (NumberUtils.isDigits(score1)) {
                            scoreList.add(score1);
                        }
                    }
                }
//                log.debug(" schregno = " + student._schregno + ", scoreList = " + scoreList);
                
                if ("合計点".equals(testItem._gokeiHeikin)) {
                    svf.VrsOutn("SCORE", student._gyo, sum(scoreList)); // 評価
                } else {
                    svf.VrsOutn("SCORE", student._gyo, avg(scoreList)); // 評価
                }
            } else {
                svf.VrsOutn("SCORE", student._gyo, score); // 評価
            }
        }

//        final List scoreList = new ArrayList();
//        for (int j = 0; j < studetAllList.size(); j++) {
//            final Student student = (Student) studetAllList.get(j);
//            final String score = student.getScore(subclass, testItem);
//            if (NumberUtils.isNumber(score)) {
//                scoreList.add(score);
//            }
//        }

    }

    private String percent(final int count, final int size) {
        if (count == 0 || size == 0) {
            return "0";
        }
        return new BigDecimal(count).multiply(new BigDecimal("100")).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String min(List scoreList) {
        int min = Integer.MAX_VALUE;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            min = Math.min(min, Integer.parseInt(score));
        }
        return min == Integer.MAX_VALUE ? null : String.valueOf(min);
    }

    private String max(List scoreList) {
        int max = Integer.MIN_VALUE;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            max = Math.max(max, Integer.parseInt(score));
        }
        return max == Integer.MIN_VALUE ? null : String.valueOf(max);
    }

    private String avg(List scoreList) {
        return scoreList.isEmpty() ? null : new BigDecimal(sum(scoreList)).divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString(); 
    }

    private String sum(List scoreList) {
        int sum = 0;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            sum += Integer.parseInt(score);
        }
        return String.valueOf(sum);
    }

    private int count(String hyotei, List hyoteiList) {
        int count = 0;
        for (final Iterator it = hyoteiList.iterator(); it.hasNext();) {
            final String e = (String) it.next();
            if (hyotei.equals(e)) {
                count ++;
            }
        }
        return count;
    }
    
    private boolean isMaxColumn(final int i) {
        return i % MAX_COLUMN1 == MAX_COLUMN1 - 1;
    }

//    // 仮評定
//    private void printColumn5(final Vrw32alp svf, final List studentlist, final Subclass subclass, final String text, int i, final TestItem testItem, final int colsize) {
//        final String title = center(text, colsize);
//        svf.VrsOut("SUBTITLE5", title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 仮評定
//        svf.VrsOut("ITEM5", take(testItem._testitemabbv1, 5); // 素点・評価他
//        svf.VrsOut("GRPCD5_1", "5"); // グループ1
//        for (int j = 0; j < studentlist.size(); j++) {
//            final Student student = (Student) studentlist.get(j);
//            svf.VrsOutn("TVALUE1_1", student._gyo, student.getScore(subclass, testItem.getTestcd())); // 仮評定
//        }
//    }

//    private void printBlankColumn(final Vrw32alp svf) {
//        svf.VrsOut("GRPCD8_1", "8"); // グループ1
//        svf.VrsOut("GRPCD8_2", "8"); // グループ1
//    }
    
//    private void printLine(final Vrw32alp svf) {
//        svf.VrsOut("GRPCD9", "9"); // グループ1
//    }
    
    private static class Student {
        private static DecimalFormat df1 = new DecimalFormat("0");
        private static DecimalFormat df2 = new DecimalFormat("00");
        final String _schregno;
        final String _name;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _hrStfStaffname;
        final String _majorname;
        final String _attendno;
        int _gyo;
        final Map _subclassMap;

        Student(String schregno, String name, String grade, String hrClass, String hrName, String hrClassName1, String hrStfStaffname,
                String attendno, String majorname) {
            _schregno = schregno;
            _name = name;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _hrStfStaffname = hrStfStaffname;
            _attendno = attendno;
            _majorname = majorname;
            _subclassMap = new HashMap();
        }

        public String getGyoNoStr() {
            return "　" + (_gyo < 10 ? " " : "") + String.valueOf(_gyo);
        }

        public String getAttendNoStr() {
//            final String gr = !NumberUtils.isDigits(_grade) ? " " : df1.format(Integer.parseInt(_grade));
//            final String hr = !NumberUtils.isDigits(_hrClass) ? " " : df1.format(Integer.parseInt(_hrClass));
            final String at = !NumberUtils.isDigits(_attendno) ? "  " : df2.format(Integer.parseInt(_attendno));
//            return gr + "-" + hr + "-" + at;
            return StringUtils.defaultString(_hrClassName1) + "-" + at;
        }

        private StudentSubclass createStudentSubclass(final Subclass subclass) {
            if (null == getStudentSubclass(subclass, _subclassMap)) {
                _subclassMap.put(subclass, new StudentSubclass(subclass));
            }
            return getStudentSubclass(subclass, _subclassMap);
        }

        public SubclassScore getSubclassScore(final Subclass subclass, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            return (SubclassScore) studentSubclass._scoreMap.get(testcd);
        }
        
        public String getScore(final Subclass subclass, final TestItem testItem) {
            final String score = getScore(subclass, testItem.getTestcd());
            String rtn = null;
            if (HYOTEI_TESTCD.equals(testItem.getTestcd())) {
                final SubclassScore subclassScore = getSubclassScore(subclass, testItem.getTestcd());
                if (null != subclassScore) {
//                    if (testItem._isGakunenKariHyotei && "1".equals(subclassScore._provFlg)
//                    || !testItem._isGakunenKariHyotei && !"1".equals(subclassScore._provFlg)) {
                        rtn = score;
//                    } else {
//                        rtn = null;
//                    }
                }
            } else {
                rtn = score;
            }
            return rtn;
        }
        
        public String getScore(final Subclass subclass, final String testcd) {
            final SubclassScore subclassScore = getSubclassScore(subclass, testcd);
//            log.debug(" " + _schregno + ", " + testcd + ", " + subclassScore + ", " + _subclassMap);
            if (null == subclassScore) {
                return null;
            }
            if (null != subclassScore._valueDi) {
                return subclassScore._valueDi;
            }
            return subclassScore._score;
        }
        
        public String toString() {
            return "Student(" + _schregno + ":" + _name + ")";
        }
    }
    
    private static class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;

        Subclass(
            final String classcd,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String subclassname,
            final boolean isSaki,
            final boolean isMoto
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }

        public String getKeySubclassCd() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }
    }
    
    private static class Chair {
        final String _chaircd;
        final String _chairname;
        final List _chairStfNameList;
        final List _hrClassList;
        public Chair(String chaircd, String chairname) {
            _chaircd = chaircd;
            _chairname = chairname;
            _chairStfNameList = new ArrayList();
            _hrClassList = new ArrayList();
        }
    }
    
    private static class Hrclass {
        final String _gradehrclass;
        final String _hrName;
        final String _hrStfStfName;
        final List _studentList;
        final Map _studentMap;
        public Hrclass(final String gradehrclass, final String hrName, final String hrStfStfName) {
            _gradehrclass = gradehrclass;
            _hrName = hrName;
            _hrStfStfName = hrStfStfName;
            _studentList = new ArrayList();
            _studentMap = new HashMap();
        }
    }
    
    private static class StudentSubclass {
        final Subclass _subclass;
        final Map _scoreMap;

        StudentSubclass(
            final Subclass subclass
        ) {
            _subclass = subclass;
            _scoreMap = new HashMap();
        }
    }
    
    private static class SubclassScore {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _score;
        final String _valueDi;
        final String _getCredit;
        final String _compCredit;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;
        final String _slumpRemark;
        final String _provFlg;
        
        SubclassScore(
            final String year,
            final String semester,
            final String testkindcd,
            final String testitemcd,
            final String scoreDiv,
            final String score,
            final String valueDi,
            final String getCredit,
            final String compCredit,
            final String slump,
            final String slumpMark,
            final String slumpScore,
            final String slumpRemark,
            final String provFlg
        ) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _score = score;
            _valueDi = valueDi;
            _getCredit = getCredit;
            _compCredit = compCredit;
            _slump = slump;
            _slumpMark = slumpMark;
            _slumpScore = slumpScore;
            _slumpRemark = slumpRemark;
            _provFlg = provFlg;
        }

        public String getTestcd() {
            return _semester + _testkindcd + _testitemcd + _scoreDiv;
        }
        public String toString() {
            return "SubclassScore(" + getTestcd() + ": "+ _score + ")";
        }
    }
    
    private Chair getChair(final String chaircd, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();
            if (chair._chaircd.equals(chaircd)) {
                return chair;
            }
        }
        return null;
    }
    
    public Hrclass getHrclass(final String gradehrclass, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Hrclass hrclass = (Hrclass) it.next();
            if (hrclass._gradehrclass.equals(gradehrclass)) {
                return hrclass;
            }
        }
        return null;
    }
    
    private Student getStudent(final String schregno, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private static StudentSubclass getStudentSubclass(final Subclass subclass, Map subclassMap) {
        return (StudentSubclass) subclassMap.get(subclass.getKeySubclassCd());
    }
    
    private boolean hasData(final DB2UDB db2, final String sql) throws SQLException {
        boolean hasData = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            hasData = true;
            break;
        }
        DbUtils.closeQuietly(null, ps, rs);
        return hasData;
    }
    
    private String getCtrlSemester(final DB2UDB db2) throws SQLException {
        String ctrlSemester = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(" SELECT CTRL_SEMESTER FROM CONTROL_MST WHERE CTRL_NO = '01' ");
        rs = ps.executeQuery();
        while (rs.next()) {
            ctrlSemester = rs.getString("CTRL_SEMESTER");
            break;
        }
        DbUtils.closeQuietly(null, ps, rs);
        return ctrlSemester;
    }

    public List getHrclassList(final DB2UDB db2, final Param param, final Subclass subclass) {
        final List hrclassList = new ArrayList();
        
        ResultSet rs = null;

        try {
            if (null == param._ps1) {
                String paramSemester = null;
//                String paramDate = null;

                paramSemester = param._semester;
//                paramDate = param._loginDate;
//            if (!hasData(db2, getRecordSql(paramSemester, paramDate))) {
//                log.warn("講座名簿がない: semester = " + paramSemester + ", date = " + paramDate);
//                // DBのCTRL_SEMESTERとCTRL_SEMESTER学期の終了日に設定
//                paramSemester = getCtrlSemester(db2);
//                if (null != paramSemester) {
//                    for (final Iterator it = _param._semesterList.iterator(); it.hasNext();) {
//                        final Semester s = (Semester) it.next();
//                        if (paramSemester.equals(s._cdSemester)) {
//                            paramDate = s._edate;
//                            break;
//                        }
//                    }
//                    log.warn("講座名簿取得学期/日付: semester = " + paramSemester + ", date = " + paramDate);
//                }
//            }
                
                final String sql = getRecordSql(paramSemester, param);
                log.debug(" sql = " + sql);
                param._ps1 = db2.prepareStatement(sql);
            }
            
            param._ps1.setString(1, subclass.getKeySubclassCd());
            rs = param._ps1.executeQuery();
            while (rs.next()) {
                
                final String key = rs.getString("GRADE") + rs.getString("HR_CLASS");
                if (null == getHrclass(key, hrclassList)) {
                    final String hrName = rs.getString("HR_NAME");
                    final String hrStfStaffname = rs.getString("HR_STF_STAFFNAME");
                    hrclassList.add(new Hrclass(key, hrName, hrStfStaffname));
                }
                final Hrclass hrclass = getHrclass(key, hrclassList);
                
                final String schregno = rs.getString("SCHREGNO");
                if (null == getStudent(schregno, hrclass._studentList)) {
                    final String name = rs.getString("NAME");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    final String hrStfStaffname = rs.getString("HR_STF_STAFFNAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String majorname = rs.getString("MAJORNAME");
                    final Student student = new Student(schregno, name, grade, hrClass, hrName, hrClassName1, hrStfStaffname, attendno, majorname);
                    hrclass._studentList.add(student);
                }
                
                final Student student = getStudent(schregno, hrclass._studentList);
                
                StudentSubclass studentSubclass = getStudentSubclass(subclass, student._subclassMap);
                if (null == studentSubclass) {
                    studentSubclass = new StudentSubclass(subclass);
                    student._subclassMap.put(subclass.getKeySubclassCd(), studentSubclass);
                }
                
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String testkindcd = rs.getString("TESTKINDCD");
                final String testitemcd = rs.getString("TESTITEMCD");
                final String scoreDiv = rs.getString("SCORE_DIV");
                final String score = rs.getString("SCORE");
                final String valueDi = rs.getString("VALUE_DI");
                final String getCredit = rs.getString("GET_CREDIT");
                final String compCredit = rs.getString("COMP_CREDIT");
                final String slump = rs.getString("SLUMP");
                final String slumpMark = rs.getString("SLUMP_MARK");
                final String slumpScore = rs.getString("SLUMP_SCORE");
                final String slumpRemark = rs.getString("SLUMP_REMARK");
                final String provFlg = rs.getString("PROV_FLG");
                final SubclassScore subclassScore = new SubclassScore(year, semester, testkindcd, testitemcd, scoreDiv, score, valueDi, getCredit, compCredit, slump, slumpMark, slumpScore, slumpRemark, provFlg);
                studentSubclass._scoreMap.put(subclassScore.getTestcd(), subclassScore);
                _hasData = true;
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
//        for (final Iterator it = chairlist.iterator(); it.hasNext();) {
//            final Chair chair = (Chair) it.next();
//            for (final Iterator it2 = chair._hrClassList.iterator(); it2.hasNext();) {
//                final Hrclass hr = (Hrclass) it2.next();
//                for (final Iterator it3 = hr._studentList.iterator(); it3.hasNext();) {
//                    final Student st = (Student) it3.next();
//                    for (final Iterator it4 = st._subclassMap.values().iterator(); it4.hasNext();) {
//                        final StudentSubclass ssub = (StudentSubclass) it4.next();
//                        for (final Iterator it5 = ssub._scoreMap.values().iterator(); it5.hasNext();) {
//                            final SubclassScore sscore = (SubclassScore) it5.next();
//                            if (null != sscore._slumpRemark) {
//                                log.info(st._schregno + " " + sscore._year + sscore._semester + sscore._testkindcd + sscore._testitemcd + " remark = " + sscore._slumpRemark);
//                            }
//                        }
//                    }
//                }
//            }
//        }
        
//        param.setChairStaff(db2, chairlist);
        
        return hrclassList;
    }

    public String getRecordSql(final String semester, final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STD AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
//        stb.append("     T2.CHAIRCD ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append(" WHERE T1.YEAR = '" + _param._year + "' ");
        stb.append("  AND T1.SEMESTER = '" + semester + "' ");
        stb.append("  AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("  AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = ? ");
        stb.append(" ), TESTCDS AS ( ");
        stb.append(" SELECT DISTINCT ");
//        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T12.YEAR, ");
        stb.append("     T12.SEMESTER, ");
        stb.append("     T12.TESTKINDCD, ");
        stb.append("     T12.TESTITEMCD, ");
        stb.append("     T12.SCORE_DIV, ");
        stb.append("     T12.CLASSCD, T12.SCHOOL_KIND,  T12.CURRICULUM_CD,  T12.SUBCLASSCD ");
        stb.append(" FROM CHAIR_STD T1 ");
        stb.append(" INNER JOIN RECORD_SCORE_DAT T12 ON T12.YEAR = T1.YEAR ");
        stb.append("     AND T12.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T12.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND T12.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND T12.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND T12.SCHREGNO = T1.SCHREGNO ");
//        stb.append(" UNION ALL ");
//        stb.append(" SELECT ");
//        stb.append("     T1.CHAIRCD, ");
//        stb.append("     T1.SCHREGNO, ");
//        stb.append("     T13.YEAR, ");
//        stb.append("     T13.SEMESTER, ");
//        stb.append("     T13.TESTKINDCD, ");
//        stb.append("     T13.TESTITEMCD, ");
//        stb.append("     T13.SCORE_DIV, ");
//        stb.append("     T13.CLASSCD, T13.SCHOOL_KIND,  T13.CURRICULUM_CD,  T13.SUBCLASSCD ");
//        stb.append(" FROM CHAIR_STD T1 ");
//        stb.append(" INNER JOIN CHAIR_DAT T11 ON T11.YEAR = '" + _param._year + "' ");
//        stb.append("     AND T11.SEMESTER = '" + semester + "' ");
//        stb.append("     AND T11.CHAIRCD = T1.CHAIRCD ");
//        stb.append(" INNER JOIN RECORD_SLUMP_SDIV_DAT T13 ON T13.YEAR = T11.YEAR ");
//        stb.append("     AND T13.CLASSCD = T11.CLASSCD ");
//        stb.append("     AND T13.SCHOOL_KIND = T11.SCHOOL_KIND ");
//        stb.append("     AND T13.CURRICULUM_CD = T11.CURRICULUM_CD ");
//        stb.append("     AND T13.SUBCLASSCD = T11.SUBCLASSCD ");
//        stb.append("     AND T13.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
//        stb.append("     T1.CHAIRCD, ");
//        stb.append("     T11.CHAIRNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T3.HR_CLASS_NAME1, ");
        stb.append("     HR_STF.STAFFNAME AS HR_STF_STAFFNAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T21.NAME, ");
        stb.append("     T4.MAJORNAME, ");
        stb.append("     T6.CREDITS, ");
        stb.append("     TTEST.YEAR, ");
        stb.append("     TTEST.SEMESTER, ");
        stb.append("     TTEST.TESTKINDCD, ");
        stb.append("     TTEST.TESTITEMCD, ");
        stb.append("     TTEST.SCORE_DIV, ");
        stb.append("     TTEST.CLASSCD || '-' || TTEST.SCHOOL_KIND || '-' || TTEST.CURRICULUM_CD || '-' || TTEST.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T12.SCORE, ");
        stb.append("     T12.VALUE_DI, ");
        stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.GET_CREDIT END AS GET_CREDIT, ");
        stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.ADD_CREDIT END AS ADD_CREDIT, ");
        stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.COMP_CREDIT END AS COMP_CREDIT, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SLUMP_SCORE, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_REMARK, ");
//        stb.append("     T13.SLUMP, ");
//        stb.append("     T14.NAME1 AS SLUMP_MARK, ");
//        stb.append("     T13.SCORE AS SLUMP_SCORE, ");
//        stb.append("     T13.REMARK AS SLUMP_REMARK, ");
        stb.append("     T15.PROV_FLG ");
        stb.append(" FROM CHAIR_STD T1 ");
//        stb.append(" INNER JOIN CHAIR_DAT T11 ON T11.YEAR = '" + _param._year + "' ");
//        stb.append("     AND T11.SEMESTER = '" + semester + "' ");
//        stb.append("     AND T11.CHAIRCD = T1.CHAIRCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + semester + "' ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND GDAT.GRADE = T2.GRADE ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T21 ON T21.SCHREGNO = T2.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T3.GRADE = T2.GRADE ");
        stb.append("     AND T3.HR_CLASS = T2.HR_CLASS ");
        stb.append(" LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T2.COURSECD ");
        stb.append("     AND T4.MAJORCD = T2.MAJORCD ");
        stb.append(" LEFT JOIN STAFF_MST HR_STF ON HR_STF.STAFFCD = T3.TR_CD1 ");
        stb.append(" LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("     AND T6.COURSECD = T2.COURSECD ");
        stb.append("     AND T6.MAJORCD = T2.MAJORCD ");
        stb.append("     AND T6.GRADE = T2.GRADE ");
        stb.append("     AND T6.COURSECODE = T2.COURSECODE ");
        stb.append("     AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("     AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" LEFT JOIN TESTCDS TTEST ON TTEST.YEAR = '" + _param._year + "' ");
//        stb.append("     AND TTEST.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND TTEST.CLASSCD = T1.CLASSCD ");
        stb.append("     AND TTEST.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     AND TTEST.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("     AND TTEST.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND TTEST.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT T12 ON T12.YEAR = TTEST.YEAR ");
        stb.append("     AND T12.SEMESTER = TTEST.SEMESTER ");
        stb.append("     AND T12.TESTKINDCD = TTEST.TESTKINDCD ");
        stb.append("     AND T12.TESTITEMCD = TTEST.TESTITEMCD ");
        stb.append("     AND T12.SCORE_DIV = TTEST.SCORE_DIV ");
        stb.append("     AND T12.CLASSCD = TTEST.CLASSCD ");
        stb.append("     AND T12.SCHOOL_KIND = TTEST.SCHOOL_KIND ");
        stb.append("     AND T12.CURRICULUM_CD = TTEST.CURRICULUM_CD ");
        stb.append("     AND T12.SUBCLASSCD = TTEST.SUBCLASSCD ");
        stb.append("     AND T12.SCHREGNO = TTEST.SCHREGNO ");
//        stb.append(" LEFT JOIN RECORD_SLUMP_SDIV_DAT T13 ON T13.YEAR = TTEST.YEAR ");
//        stb.append("     AND T13.SEMESTER = TTEST.SEMESTER ");
//        stb.append("     AND T13.TESTKINDCD = TTEST.TESTKINDCD ");
//        stb.append("     AND T13.TESTITEMCD = TTEST.TESTITEMCD ");
//        stb.append("     AND T13.SCORE_DIV = TTEST.SCORE_DIV ");
//        stb.append("     AND T13.CLASSCD = TTEST.CLASSCD ");
//        stb.append("     AND T13.SCHOOL_KIND = TTEST.SCHOOL_KIND ");
//        stb.append("     AND T13.CURRICULUM_CD = TTEST.CURRICULUM_CD ");
//        stb.append("     AND T13.SUBCLASSCD = TTEST.SUBCLASSCD ");
//        stb.append("     AND T13.SCHREGNO = TTEST.SCHREGNO ");
//        stb.append("  LEFT JOIN NAME_MST T14 ON T14.NAMECD1 = 'D054' ");
//        stb.append("     AND T14.NAMECD2 = T13.MARK ");
        stb.append(" LEFT JOIN RECORD_PROV_FLG_DAT T15 ON T15.YEAR = T12.YEAR ");
        stb.append("     AND T15.CLASSCD = T12.CLASSCD ");
        stb.append("     AND T15.SCHOOL_KIND = T12.SCHOOL_KIND ");
        stb.append("     AND T15.CURRICULUM_CD = T12.CURRICULUM_CD ");
        stb.append("     AND T15.SUBCLASSCD = T12.SUBCLASSCD ");
        stb.append("     AND T15.SCHREGNO = T12.SCHREGNO ");
        if ("1".equals(_param._use_school_detail_gcm_dat)) {
            stb.append(" WHERE ");
            stb.append("     T2.COURSECD || '-' || T2.MAJORCD = '" + _param._COURSE_MAJOR + "' ");
            stb.append("     AND GDAT.SCHOOL_KIND = '" + _param._PRINT_SCHOOLKIND + "' ");
        }

        stb.append(" ORDER BY ");
//        stb.append(" T1.CHAIRCD, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        stb.append(" T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        return stb.toString();
    }
    
    private static class Semester {
        final String _year;
        final String _cdSemester;
        final String _semestername;
        final String _sdate;
        final String _edate;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(String year, String semester, String semestername, final String sdate, final String edate) {
            _year = year;
            _cdSemester = semester;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//            log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
            return _semesterDetailList.indexOf(semesterDetail);
        }
    }
    
    private static class SemesterDetail {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._cdSemester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }
    
    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
//        boolean _isGakunenKariHyotei;
        String _gokeiHeikin;
//        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._cdSemester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._cdSemester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _subclasscd;
        final String[] _categorySelected;
        final String _grade;
        final String _schoolKind;
        final String _gradeName1;
        final String _printKekka0;
        final String _printTannin;
        final String _loginDate;
        final String _prgid;
        final String _useCurriculumcd;
        final String _use_SchregNo_hyoji;
        final String _COURSE_MAJOR;
        final String _PRINT_SCHOOLCD;
        final String _PRINT_SCHOOLKIND;
        final String _useSchool_KindField;
        final String _use_school_detail_gcm_dat;

        final List _semesterList;
//        final List _kariHyouteiList;
        final List _subclassList;
        final String _remarkTestcd;
        final String _now;
        
        private KNJSchoolMst _knjSchoolMst;
        private final Map _testKindItemListMap;
        private PreparedStatement _ps1 = null;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _grade = request.getParameter("GRADE");
            _schoolKind = getSchoolKind(db2, _year, _grade);
            _gradeName1 = getGradeName1(db2, _year, _grade);
            _printKekka0 = request.getParameter("PRINT_KEKKA0");
            _printTannin = request.getParameter("PRINT_TANNIN");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _remarkTestcd = request.getParameter("REMARK_TESTCD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            
            _COURSE_MAJOR = request.getParameter("COURSE_MAJOR");
            _PRINT_SCHOOLCD = request.getParameter("PRINT_SCHOOLCD");
            _PRINT_SCHOOLKIND = request.getParameter("PRINT_SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");

            _subclassList = getSubclassList(db2, _subclasscd, _year);
            _semesterList = getSemesterList(db2);
 //           _kariHyouteiList = getTestKindItemKariHyouteiList(_testKindItemList);
            _now = getNow();
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
            
            final String z010 = setZ010Name1(db2);
            log.info(" z010 name1 = " + z010);
            
            _testKindItemListMap = getTestKindItemListMap(db2);
        }
        
//        private void setChairStaff(final DB2UDB db2, final List chairlist) {
//            String sql = "";
//            sql += " SELECT DISTINCT T1.STAFFCD, STAFFNAME FROM CHAIR_STF_DAT T1 ";
//            sql += " INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ";
//            sql += " WHERE YEAR = '" + _year + "' AND SEMESTER <= '" + _semester + "' AND CHAIRCD = ? AND CHARGEDIV = 1 ";
//            sql += " ORDER BY T1.STAFFCD ";
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sql);
//                for (final Iterator it = chairlist.iterator(); it.hasNext();) {
//                    final Chair chair = (Chair) it.next();
//                    chair._chairStfNameList.clear();
//                    ps.setString(1, chair._chaircd);
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        if (StringUtils.isEmpty(rs.getString("STAFFNAME")) || chair._chairStfNameList.contains(rs.getString("STAFFNAME"))) {
//                            continue;
//                        }
//                        chair._chairStfNameList.add(rs.getString("STAFFNAME"));
//                        if (chair._chairStfNameList.size() >= 4) { // 最大4件まで
//                            break;
//                        }
//                    }
//                    DbUtils.closeQuietly(rs);
//                }
//            } catch (Exception e) {
//                log.fatal("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

        private String getNow() {
            final String[] tate = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(_loginDate));
            final String nengo = null != tate && tate.length > 0 &&  tate[0].length() > 0 ? tate[0] : "";
            final String nen = null != tate && tate.length > 1 ? tate[1] : "";

            final Calendar cal = Calendar.getInstance();
            final DecimalFormat z2 = new DecimalFormat("00");
            final String hour = z2.format(cal.get(Calendar.HOUR_OF_DAY));
            final String minute = z2.format(cal.get(Calendar.MINUTE));
            cal.setTime(Date.valueOf(_loginDate));
            final String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            final String dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            return nengo + nen + "." + month + "." + dayOfMonth + ". " + hour + ":" + minute;
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
        
        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(year, semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
//        private List getTestKindItemSidouInputList() {
//            final List rtn = new ArrayList();
//            for (int i = 0; i < _testKindItemList.size(); i++) {
//                final TestItem testItem = (TestItem) _testKindItemList.get(i);
//                if ("1".equals(testItem._sidouInput)) {
//                    rtn.add(testItem);
//                }
//            }
//            return rtn;
//        }
        
//        private List getTestKindItemKariHyouteiList(final List testKindItemList) {
//            final List rtn = new ArrayList();
//            for (final Iterator it = testKindItemList.iterator(); it.hasNext();) {
//                final TestItem testItem = (TestItem) it.next();
//                if (!ALL_SEME.equals(testItem._semester._cdSemester) && "99".equals(testItem._testkindcd) && "00".equals(testItem._testitemcd) && KARIHYOUTEI_SCORE_DIV.equals(testItem._scoreDiv)) {
//                    rtn.add(testItem);
//                    // it.remove();
//                    // testItem._semester._testItemList.remove(testItem);
//                }
//            }
//            return rtn;
//        }
        
        private List getTestKindItemList(final String subclasscd) {
            final String[] subclasscds = {subclasscd, "00-" + _schoolKind + "-00-000000", "00-00-00-000000"};
            for (int i = 0; i < subclasscds.length; i++) {
                if (null != _testKindItemListMap.get(subclasscds[i])) {
                    return getMappedList(_testKindItemListMap, subclasscds[i]);
                }
            }
            return Collections.EMPTY_LIST;
        }

        private Map getTestKindItemListMap(final DB2UDB db2) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                    final Semester semester = (Semester) it.next();
                    semesterMap.put(semester._cdSemester, semester);
                }
                final StringBuffer stb = new StringBuffer();
//                stb.append(" WITH ADMIN_CONTROL_SDIV AS (");
//                if ("1".equals(_use_school_detail_gcm_dat)) {
//                    stb.append("   SELECT T1.* ");
//                    stb.append("   FROM ADMIN_CONTROL_SDIV_GCM_DAT T1 ");
//                    stb.append("   WHERE ");
//                    stb.append("    T1.SCHOOLCD = '" + _PRINT_SCHOOLCD + "' ");
//                    stb.append("    AND T1.SCHOOL_KIND = '" + _PRINT_SCHOOLKIND + "' ");
//                    stb.append("    AND T1.GRADE = '00' ");
//                    stb.append("    AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
//                } else {
//                    stb.append("   SELECT T1.* ");
//                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
//                }
//                stb.append(" ), ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
//                stb.append("   SELECT DISTINCT ");
//                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
//                stb.append("   FROM ADMIN_CONTROL_SDIV T1 ");
//                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
//                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _subclasscd + "' ");
//                stb.append("   UNION ALL ");
//                stb.append("   SELECT DISTINCT ");
//                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
//                stb.append("   FROM ADMIN_CONTROL_SDIV T1 ");
//                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
//                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
//                stb.append("   UNION ALL ");
//                stb.append("   SELECT DISTINCT ");
//                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
//                stb.append("   FROM ADMIN_CONTROL_SDIV T1 ");
//                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
//                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-" + _schoolKind + "-00-000000' ");
//                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                }
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T11.GRADE = T1.GRADE ");
                    stb.append("    AND T11.COURSECD = T1.COURSECD ");
                    stb.append("    AND T11.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
//                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
//                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T1.SCHOOLCD = '" + _PRINT_SCHOOLCD + "' ");
                    stb.append("    AND T1.SCHOOL_KIND = '" + _PRINT_SCHOOLKIND + "' ");
                    stb.append("    AND T1.GRADE = '00' ");
                    stb.append("    AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
                
//                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                String adminSdivSubclasscd = null;
                Map semesterDetailMap = new HashMap();
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
//                    if (isGakunenHyotei) {
//                        testItem._printKettenFlg = 1;
//                    } else if (!ALL_SEME.equals(semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
//                        testItem._printKettenFlg = -1;
//                    } else {
//                        testItem._printKettenFlg = 2;
//                    }
                    if (isGakunenHyotei) {
                        final TestItem itemGokei = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "合計点", "合計点", sidouInput, sidouInputInf,
                                semesterDetail);
//                        testItemKari._printKettenFlg = -1;
                        itemGokei._gokeiHeikin = "合計点";
                        semester._testItemList.add(itemGokei);
                        getMappedList(map, adminSdivSubclasscd).add(itemGokei);
                        
                        final TestItem itemHeikin = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "平均点", "平均点", sidouInput, sidouInputInf,
                                semesterDetail);
                        itemHeikin._gokeiHeikin = "平均点";
                        semester._testItemList.add(itemHeikin);
                        getMappedList(map, adminSdivSubclasscd).add(itemHeikin);
                    }
                    semester._testItemList.add(testItem);
                    getMappedList(map, adminSdivSubclasscd).add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscds = " + map.keySet());

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
//            log.debug(" testcd = " + map);
            return map;
        }

        private String getGradeName1(final DB2UDB db2, final String year, final String grade) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append(" T1.* ");
                sql.append(" FROM SCHREG_REGD_GDAT T1 ");
                sql.append(" WHERE T1.YEAR = '" + year + "' AND T1.GRADE = '" + _grade + "' ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE_NAME1");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getSchoolKind(final DB2UDB db2, final String year, final String grade) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append(" T1.* ");
                sql.append(" FROM SCHREG_REGD_GDAT T1 ");
                sql.append(" WHERE T1.YEAR = '" + year + "' AND T1.GRADE = '" + _grade + "' ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private List getSubclassList(final DB2UDB db2, final String paramSubclasscd, final String paramYear) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" WITH REPLACE AS ( ");
                sql.append(" SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + paramYear + "' ");
                sql.append(" UNION ");
                sql.append(" SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + paramYear + "' ");
                sql.append(" ) ");
                sql.append(" SELECT ");
                sql.append(" T1.*, ");
                sql.append(" CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
                sql.append(" CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
                sql.append(" FROM SUBCLASS_MST T1 ");
                sql.append(" LEFT JOIN REPLACE L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ");
                sql.append(" LEFT JOIN REPLACE L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ");
                sql.append(" WHERE T1.SCHOOL_KIND = '" + _schoolKind + "' ");
                sql.append("   AND T1.CLASSCD < '" + "90" + "' ");
                if (!"ALL".equals(paramSubclasscd)) {
                    sql.append(" AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + paramSubclasscd + "' ");
                }
                sql.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final Subclass subclass = new Subclass(classcd, schoolKind, curriculumCd, subclasscd, subclassname, isSaki, isMoto);
                    list.add(subclass);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}

// eof

