/*
 * $Id: dd0044af0fa05ae7f35f2483476eda9a7dcf8350 $
 *
 * 作成日: 2013/07/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD128F {

    private static final Log log = LogFactory.getLog(KNJD128F.class);

    private static final int _charsPerColumn = 2; // 1列あたりの文字数
    private static final int MAX_COLUMN1 = 14;
    private static final String ALL_SEME = "9";
    private static final String HYOTEI_TESTCD = "9990000";

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

            final Subclass subclass = _param._subclass;
            printMain(db2, svf, subclass);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Subclass subclass) {
        final List chairList = getChairList(db2, _param);
        
        for (final Iterator it = chairList.iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();
            
            for (final Iterator ith = chair._hrClassList.iterator(); ith.hasNext();) {
                final Hrclass hrclass = (Hrclass) ith.next();
                
                svf.VrSetForm("KNJD128F.frm", 4);
                
                printHeader(svf, subclass, chair, hrclass, hrclass._studentList);

                int column = 0;
                // 成績
                for (int i = 0, colsize = _param._testKindItemList.size(); i < colsize; i++) {
                    final TestItem testItem = (TestItem) _param._testKindItemList.get(i);
                    printColumn1(svf, subclass, hrclass._studentList, "成績", i, testItem, colsize, column);
                    column += 1;
                    svf.VrEndRecord();
                }
                
                if (column < MAX_COLUMN1 * 1) {
                    // 備考
                    final int bikoLen = MAX_COLUMN1 * 1 - column;
                    if (bikoLen > 2) {
                        for (int i = 0; i < bikoLen; i++) {
                            // log.debug(" biko " + column);
                            printColumn7(1, svf, hrclass._studentList, subclass, "備考", i, bikoLen);
                            column += 1;
                            svf.VrEndRecord();
                        }
                    }
                }

                printLine(svf);
                column += 1;
                svf.VrEndRecord();
            }
        }
    }
    
    private static String center(final String text, final int columnSize) {
        final boolean isOdd = (columnSize * _charsPerColumn - StringUtils.defaultString(text).length()) % 2 == 1;
        return StringUtils.center(text, columnSize * _charsPerColumn - (isOdd ? 1 : 0), "　") + (isOdd ? "　" : "");
    }

    private void printHeader(final Vrw32alp svf, final Subclass subclass, final Chair chair, final Hrclass hrclass, final List studentList) {
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　成績"); // タイトル
        svf.VrsOut("SUBCLASSCD", "(" + subclass.getKeySubclassCd() + ")"); // 科目コード
        svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "")  , subclass._subclassname); // 科目名
        svf.VrsOut("PRINTDAY", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 印刷日
        svf.VrsOut("CLASS_TNAME", "教科担任氏名"); // 教科担当職名名称
        svf.VrsOut("CLASS_TEACHER" + (getMS932ByteLength(chair._chairStfName) > 20 ? "2" : ""), chair._chairStfName); // 教科担任名

        String course = "講座名　：" + StringUtils.defaultString(chair._chairname);
        svf.VrsOut("COURSE",   course); // コース年組講座名
        
        svf.VrsOut("NO_TITLE", "年組番"); // 番号タイトル

        // 生徒氏名
        final List hyoteiList = new ArrayList();
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            svf.VrsOutn("NO", student._gyo, student.getAttendNoStr()); // 年組番号
            final int len = getMS932ByteLength(student._name);
            svf.VrsOutn("NAME" + (len > 30 ? "3" : len > 20 ? "2" : "1"), student._gyo, student._name); // 氏名
            final String hyotei = student.getScore(subclass, HYOTEI_TESTCD);
            if (null != hyotei) {
                hyoteiList.add(hyotei);
            }
        }
    }

    // 成績
    private void printColumn1(final Vrw32alp svf, final Subclass subclass, final List studentlist, final String text, int i, final TestItem testItem, final int colsize, final int column) {
        final boolean last = isMaxColumn(column) || i == colsize - 1;
        final boolean start = testItem._semester.getTestItemIdx(testItem) == 0;
        final String suf1 = start && last ? "_4" : last ? "_3" : start ? "" : "_2";
        final String suf2 = start && last ? "_4" : last ? "_3" : start ? "_1" : "_2";
        final String title = center(text, colsize);
        svf.VrsOut("SUBTITLE1" + suf1, title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 成績
        final String fieldSem;
        final int keta;
        if (testItem._semester._semestername.length() <= testItem._semester._testItemList.size() * 2 && testItem._semester._semestername.length() > 3) {
            keta = 2;
            fieldSem = "SEM1" + (start && last ? "_7" : last ? "_5" : start ? "_1" : "_3");
        } else {
            keta = 3;
            fieldSem = "SEM1" + (start && last ? "_8" : last ? "_6" : start ? "_2" : "_4");
        }
        final int nameIdx = testItem._semester.getTestItemIdx(testItem) * keta;
        if (nameIdx < testItem._semester._testItemList.size() * keta) {
            svf.VrsOut(fieldSem, StringUtils.center(testItem._semester._semestername, testItem._semester._testItemList.size() * keta,'　').substring(nameIdx)); // 学期
        }
        svf.VrsOut("ITEM1" + suf1, testItem._testitemname); // 素点・評価他
        // log.debug(" ITEM1" + (suf1.length() == 0 ? "  " : suf1) + " = " + testItem._semester._semestername + ":" + testItem._testitemabbv1);

        final List scoreList = new ArrayList();
        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            final String score = student.getScore(subclass, testItem);
            svf.VrsOutn("GRADING1" + suf2, student._gyo, score); // 評価
            if (NumberUtils.isNumber(score)) {
                scoreList.add(score);
            }
        }

        svf.VrsOutn("TOTAL51" + suf2, 1, "0".equals(sum(scoreList)) ? "" : sum(scoreList)); // 合計
        svf.VrsOutn("TOTAL1"  + suf2, 2, 0 == scoreList.size() ? "" : String.valueOf(scoreList.size())); // 人数
        svf.VrsOutn("TOTAL51" + suf2, 3, avg(scoreList)); // 平均
        svf.VrsOutn("TOTAL1"  + suf2, 4, max(scoreList)); // 最高
        svf.VrsOutn("TOTAL1"  + suf2, 5, min(scoreList)); // 最低
    }

    private String percent(final int count, final int size) {
        if (count == 0 || size == 0) {
            return "0";
        }
        return new BigDecimal(count).multiply(new BigDecimal("100")).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String min(List scoreList) {
        int min = Integer.MAX_VALUE;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            min = Math.min(min, Integer.parseInt(score));
        }
        return min == Integer.MAX_VALUE ? null : String.valueOf(min);
    }

    private static String max(List scoreList) {
        int max = Integer.MIN_VALUE;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            max = Math.max(max, Integer.parseInt(score));
        }
        return max == Integer.MIN_VALUE ? null : String.valueOf(max);
    }

    private static String avg(List scoreList) {
        return scoreList.isEmpty() ? null : new BigDecimal(sum(scoreList)).divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString(); 
    }

    private static String sum(List scoreList) {
        int sum = 0;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            sum += Integer.parseInt(score);
        }
        return String.valueOf(sum);
    }

    private static int count(String hyotei, List hyoteiList) {
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

    // 備考
    private void printColumn7(final int flg, final Vrw32alp svf, final List studentlist, final Subclass subclass, final String text, final int i, final int colsize) {
        final boolean first = i == 0;
        final boolean last = i == colsize - 1;
        final String suf1 = last ? "_2" : flg == 2 ? "" : first ? "_3" : "";
//        final String suf2 = last ? "2" : flg == 2 ? "1" : first ? "3" : "1";
        final String fieldGrpCd1 = "GRPCD7" + (last ? "_3" : flg == 2 ? "_1" : first ? "_5" : "_1");
        final String fieldGrpCd2 = "GRPCD7" + (last ? "_4" : flg == 2 ? "_2" : first ? "_6" : "_2");
        svf.VrsOut(fieldGrpCd1, "7"); // グループ1
        svf.VrsOut(fieldGrpCd2, "7"); // グループ1
        final String title = StringUtils.center(text, colsize, "　");
        svf.VrsOut("SUBTITLE7" + suf1, String.valueOf(title.charAt(i))); // 判定
        // log.debug(" filed = " + "SUBTITLE7" + suf1);
    }
    
    private void printLine(final Vrw32alp svf) {
        svf.VrsOut("GRPCD9", "9"); // グループ1
    }
    
    private static class Student {
        private static DecimalFormat df1 = new DecimalFormat("0");
        private static DecimalFormat df2 = new DecimalFormat("00");
        final String _schregno;
        final String _name;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrStfStaffname;
        final String _majorname;
        final String _attendno;
        final int _gyo;
        final Map _subclassMap;
        final Map _creditMstCreditsMap;

        Student(String schregno, String name, String grade, String hrClass, String hrName, String hrStfStaffname,
                String attendno, String majorname, final int gyo) {
            _schregno = schregno;
            _name = name;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrStfStaffname = hrStfStaffname;
            _attendno = attendno;
            _majorname = majorname;
            _gyo = gyo;
            _subclassMap = new HashMap();
            _creditMstCreditsMap = new HashMap();
        }

        public String getGyoNoStr() {
            return "　" + (_gyo < 10 ? " " : "") + String.valueOf(_gyo);
        }

        public String getAttendNoStr() {
            final String gr = !NumberUtils.isDigits(_grade) ? " " : df1.format(Integer.parseInt(_grade));
            final String hr = !NumberUtils.isDigits(_hrClass) ? " " : df1.format(Integer.parseInt(_hrClass));
            final String at = !NumberUtils.isDigits(_attendno) ? "  " : df2.format(Integer.parseInt(_attendno));
            return gr + "-" + hr + "-" + at;
        }

        private StudentSubclass createStudentSubclass(final Subclass subclass) {
            if (null == getStudentSubclass(subclass, _subclassMap)) {
                _subclassMap.put(_subclassMap, new StudentSubclass(subclass));
            }
            return getStudentSubclass(subclass, _subclassMap);
        }

        public SubclassScore getSubclassScore(final Subclass subclass, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            return (SubclassScore) studentSubclass._scoreMap.get(testcd);
        }
        
        public String getScore(final Subclass subclass, final TestItem testItem) {
            final SubclassScore subclassScore = getSubclassScore(subclass, testItem.getTestcd());
            if (null == subclassScore) {
                return null;
            }
            String rtn;
            if (HYOTEI_TESTCD.equals(testItem.getTestcd())) {
                if (testItem._isGakunenKariHyotei && "1".equals(subclassScore._provFlg)
                || !testItem._isGakunenKariHyotei && !"1".equals(subclassScore._provFlg)) {
                    rtn = subclassScore._score;
                } else {
                    rtn = null;
                }
            } else {
                rtn = subclassScore._score;
            }
            return rtn;
        }
        
        public String getScore(final Subclass subclass, final String testcd) {
            final SubclassScore subclassScore = getSubclassScore(subclass, testcd);
//            log.debug(" " + _schregno + ", " + testcd + ", " + subclassScore + ", " + _subclassMap);
            return null == subclassScore ? null : subclassScore._score;
        }
        
        public String getMust(final Subclass subclass, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            return (String) studentSubclass._mustMap.get(testcd);
        }
        
        public String getSick2(final Subclass subclass, final String testcd, final boolean isJissu) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            final String sick2 = (String) studentSubclass._sick2Map.get(testcd);
            return null == sick2 ? null : 0.0 == Double.parseDouble(sick2) ? "" : new BigDecimal(sick2).setScale(isJissu ? 1 : 0, BigDecimal.ROUND_HALF_UP).toString();
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

        Subclass(
            final String classcd,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String subclassname
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        public String getKeySubclassCd() {
            if ("".equals(_classcd) && "".equals(_schoolKind) && "".equals(_curriculumCd)) {
                return _subclasscd;
            }
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }
    }
    
    private static class Chair {
        final String _chaircd;
        final String _chairname;
        final String _chairStfName;
        final List _hrClassList;
        public Chair(String chaircd, String chairname, String chairStfName) {
            _chaircd = chaircd;
            _chairname = chairname;
            _chairStfName = chairStfName;
            _hrClassList = new ArrayList();
        }
    }
    
    private static class Hrclass {
        final String _gradehrclass;
        final String _hrName;
        final String _hrStfStfName;
        final List _studentList;
        public Hrclass(final String gradehrclass, final String hrName, final String hrStfStfName) {
            _gradehrclass = gradehrclass;
            _hrName = hrName;
            _hrStfStfName = hrStfStfName;
            _studentList = new ArrayList();
        }
    }
    
    private static class StudentSubclass {
        final Subclass _subclass;
        final Map _scoreMap;
        final Map _mustMap;
        final Map _sick2Map;

        StudentSubclass(
            final Subclass subclass
        ) {
            _subclass = subclass;
            _scoreMap = new HashMap();
            _mustMap = new HashMap();
            _sick2Map = new HashMap();
        }
    }
    
    private static class SubclassScore {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _score;
        final String _getCredit;
        final String _compCredit;
        final String _provFlg;
        
        SubclassScore(
            final String year,
            final String semester,
            final String testkindcd,
            final String testitemcd,
            final String scoreDiv,
            final String score,
            final String getCredit,
            final String compCredit,
            final String provFlg
        ) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _score = score;
            _getCredit = getCredit;
            _compCredit = compCredit;
            _provFlg = provFlg;
        }

        public String getTestcd() {
            return _semester + _testkindcd + _testitemcd + _scoreDiv;
        }
        public String toString() {
            return "SubclassScore(" + getTestcd() + ": "+ _score + ")";
        }
    }
    
    private static Chair getChair(final String chaircd, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();
            if (chair._chaircd.equals(chaircd)) {
                return chair;
            }
        }
        return null;
    }
    
    public static Hrclass getHrclass(final String gradehrclass, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Hrclass hrclass = (Hrclass) it.next();
            if (hrclass._gradehrclass.equals(gradehrclass)) {
                return hrclass;
            }
        }
        return null;
    }
    
    private static Student getStudent(final String schregno, final List list) {
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

    private List getChairList(final DB2UDB db2, final Param param) {
        final Subclass subclass = param._subclass;
        final List chairlist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            final String sql = getRecordSql(param);
            // log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String chaircd = rs.getString("CHAIRCD");
                if (null == getChair(chaircd, chairlist)) {
                    final String chairname = rs.getString("CHAIRNAME");
                    final String chrStfStaffname = rs.getString("CHR_STF_STAFFNAME");
                    chairlist.add(new Chair(chaircd, chairname, chrStfStaffname));
                }
                final Chair chair = getChair(chaircd, chairlist);
                
                final String key = "1".equals(param._printDiv) ? rs.getString("GRADE") + rs.getString("HR_CLASS") : "ALL";
                if (null == getHrclass(key, chair._hrClassList)) {
                    final String hrName = rs.getString("HR_NAME");
                    final String hrStfStaffname = rs.getString("HR_STF_STAFFNAME");
                    chair._hrClassList.add(new Hrclass(key, hrName, hrStfStaffname));
                }
                final Hrclass hrclass = getHrclass(key, chair._hrClassList);
                
                final String schregno = rs.getString("SCHREGNO");
                if (null == getStudent(schregno, hrclass._studentList)) {
                    final String name = rs.getString("NAME");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrStfStaffname = rs.getString("HR_STF_STAFFNAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String majorname = rs.getString("MAJORNAME");
                    final int gyo = hrclass._studentList.size() + 1;
                    final Student student = new Student(schregno, name, grade, hrClass, hrName, hrStfStaffname, attendno, majorname, gyo);
                    hrclass._studentList.add(student);
                }
                
                final Student student = getStudent(schregno, hrclass._studentList);
                
                StudentSubclass studentSubclass = getStudentSubclass(subclass, student._subclassMap);
                if (null == studentSubclass) {
                    studentSubclass = new StudentSubclass(subclass);
                    student._subclassMap.put(subclass.getKeySubclassCd(), studentSubclass);
                    final String credits = rs.getString("CREDITS");
                    student._creditMstCreditsMap.put(subclass.getKeySubclassCd(), credits);
                }
                
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String testkindcd = rs.getString("TESTKINDCD");
                final String testitemcd = rs.getString("TESTITEMCD");
                final String scoreDiv = rs.getString("SCORE_DIV");
                final String score = rs.getString("SCORE");
                final String getCredit = rs.getString("GET_CREDIT");
                final String compCredit = rs.getString("COMP_CREDIT");
                final String provFlg = rs.getString("PROV_FLG");
                final SubclassScore subclassScore = new SubclassScore(year, semester, testkindcd, testitemcd, scoreDiv, score, getCredit, compCredit, provFlg);
                studentSubclass._scoreMap.put(subclassScore.getTestcd(), subclassScore);
                _hasData = true;
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return chairlist;
    }

    public String getRecordSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STD AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     CHAIRCD ");
        stb.append(" FROM CHAIR_STD_DAT T1 ");
        stb.append(" WHERE YEAR = '" + _param._year + "' ");
        stb.append("  AND SEMESTER = '" + _param._semester + "' ");
        stb.append("  AND CHAIRCD IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("  AND '" + _param._executedate + "' BETWEEN APPDATE AND VALUE(APPENDDATE, '9999-12-31') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T11.CHAIRNAME, ");
        stb.append("     CHR_STF.STAFFNAME AS CHR_STF_STAFFNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     HR_STF.STAFFNAME AS HR_STF_STAFFNAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T21.NAME, ");
        stb.append("     T4.MAJORNAME, ");
        stb.append("     T6.CREDITS, ");
        stb.append("     T12.YEAR, ");
        stb.append("     T12.SEMESTER, ");
        stb.append("     T12.TESTKINDCD, ");
        stb.append("     T12.TESTITEMCD, ");
        stb.append("     T12.SCORE_DIV, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T12.CLASSCD || '-' || T12.SCHOOL_KIND || '-' || T12.CURRICULUM_CD || '-' || T12.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     '---'  ||T12.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     CASE WHEN T12.TESTKINDCD = '99' THEN T12.VALUE ELSE T12.SCORE END AS SCORE, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.GET_CREDIT END AS GET_CREDIT, ");
            stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.ADD_CREDIT END AS ADD_CREDIT, ");
            stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.COMP_CREDIT END AS COMP_CREDIT, ");
            stb.append("     T15.PROV_FLG ");
        } else {
            stb.append("     T12.GET_CREDIT, ");
            stb.append("     T12.ADD_CREDIT, ");
            stb.append("     T12.COMP_CREDIT, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS PROV_FLG ");
        }
        stb.append(" FROM CHAIR_STD T1 ");
        stb.append(" INNER JOIN CHAIR_DAT T11 ON T11.YEAR = '" + _param._year + "' ");
        stb.append("     AND T11.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T11.CHAIRCD = T1.CHAIRCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST T21 ON T21.SCHREGNO = T2.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T3.GRADE = T2.GRADE ");
        stb.append("     AND T3.HR_CLASS = T2.HR_CLASS ");
        stb.append(" LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T2.COURSECD ");
        stb.append("     AND T4.MAJORCD = T2.MAJORCD ");
        stb.append(" LEFT JOIN CHAIR_STF_DAT T5 ON T5.YEAR = T11.YEAR ");
        stb.append("     AND T5.SEMESTER = T11.SEMESTER ");
        stb.append("     AND T5.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T5.CHARGEDIV = 1 ");
        stb.append(" LEFT JOIN STAFF_MST CHR_STF ON CHR_STF.STAFFCD = T5.STAFFCD ");
        stb.append(" LEFT JOIN STAFF_MST HR_STF ON HR_STF.STAFFCD = T3.TR_CD1 ");
        stb.append(" LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T11.YEAR ");
        stb.append("     AND T6.COURSECD = T2.COURSECD ");
        stb.append("     AND T6.MAJORCD = T2.MAJORCD ");
        stb.append("     AND T6.GRADE = T2.GRADE ");
        stb.append("     AND T6.COURSECODE = T2.COURSECODE ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     AND T6.CLASSCD = T11.CLASSCD ");
            stb.append("     AND T6.SCHOOL_KIND = T11.SCHOOL_KIND ");
            stb.append("     AND T6.CURRICULUM_CD = T11.CURRICULUM_CD ");
        }
        stb.append("     AND T6.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT T12 ON T12.YEAR = T11.YEAR ");
        // stb.append("     AND T12.SEMESTER = T11.SEMESTER ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     AND T12.CLASSCD = T11.CLASSCD ");
            stb.append("     AND T12.SCHOOL_KIND = T11.SCHOOL_KIND ");
            stb.append("     AND T12.CURRICULUM_CD = T11.CURRICULUM_CD ");
        }
        stb.append("     AND T12.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append("     AND T12.SCHREGNO = T2.SCHREGNO ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" LEFT JOIN RECORD_PROV_FLG_DAT T15 ON T15.YEAR = T12.YEAR ");
            stb.append("     AND T15.CLASSCD = T12.CLASSCD ");
            stb.append("     AND T15.SCHOOL_KIND = T12.SCHOOL_KIND ");
            stb.append("     AND T15.CURRICULUM_CD = T12.CURRICULUM_CD ");
            stb.append("     AND T15.SUBCLASSCD = T12.SUBCLASSCD ");
            stb.append("     AND T15.SCHREGNO = T12.SCHREGNO ");
        }

        stb.append(" ORDER BY ");
        stb.append(" T1.CHAIRCD, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
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
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
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
    private class Param {
        final String _year;
        final String _semester;
        final String _subclasscd;
        final String[] _categorySelected;
        final String _executedate;
        final String _printDiv;
        final String _ctrlDate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        final List _semesterList;
        final List _testKindItemList;
        final Subclass _subclass;
        
        final String SSEMESTER = "1";
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineCode _definecode;
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categorySelected = new String[] {request.getParameter("CHAIRCD")}; // request.getParameterValues("category_selected");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _executedate = request.getParameter("TEST_DATE");
            _printDiv = null; // request.getParameter("PRINT_DIV");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _subclass = getSubclass(db2, _subclasscd);
            _semesterList = getSemesterList(db2);
            _testKindItemList = getTestKindItemList(db2);
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                _definecode = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                //log.debug(_attendSemesMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = new KNJDefineCode();
            try {
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
        
        private List getTestKindItemList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                    final Semester semester = (Semester) it.next();
                    semesterMap.put(semester._cdSemester, semester);
                }
                
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T11.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW T1 ");
                stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD = '00-00-00-000000' ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T11.SCORE_DIV ");

                // log.debug(" testitem sql ="  + stb.toString());
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
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
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
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!ALL_SEME.equals(semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
//                    if (isGakunenHyotei) {
//                        final TestItem testItemKari = new TestItem(
//                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
//                                semesterDetail);
//                        testItemKari._printKettenFlg = -1;
//                        testItemKari._isGakunenKariHyotei = true;
//                        semester._testItemList.add(testItemKari);
//                        list.add(testItemKari);
//                    }
                    semester._testItemList.add(testItem);
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private Subclass getSubclass(final DB2UDB db2, final String paramSubclasscd) {
            Subclass subclass = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT * FROM SUBCLASS_MST ");
                if ("1".equals(_useCurriculumcd)) {
                    sql.append(" WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + paramSubclasscd + "' ");
                } else {
                    sql.append(" WHERE SUBCLASSCD = '" + paramSubclasscd + "' ");
                }
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("1".equals(_useCurriculumcd)) {
                        final String classcd = rs.getString("CLASSCD");
                        final String schoolKind = rs.getString("SCHOOL_KIND");
                        final String curriculumCd = rs.getString("CURRICULUM_CD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        subclass = new Subclass(classcd, schoolKind, curriculumCd, subclasscd, subclassname);
                    } else {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        subclass = new Subclass("", "", "", subclasscd, subclassname);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return subclass;
        }
    }
}

// eof

