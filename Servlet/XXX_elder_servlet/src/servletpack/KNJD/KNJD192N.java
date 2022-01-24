// kanji=漢字
/*
 * $Id: 4e90b7e99e6694505a7dd88024668ec53b7273d7 $
 *
 * 作成日: 2019/05/20 10:10:00 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4e90b7e99e6694505a7dd88024668ec53b7273d7 $
 */
public class KNJD192N {

    private static final Log log = LogFactory.getLog("KNJD192N.class");

    private Param _param;

//    private static final String SPECIAL_ALL = "999";

    private static final String SEMEALL = "9";

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

    private boolean _hasData;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            log.fatal("$Revision: 70341 $ $Date: 2019-10-24 17:07:26 +0900 (木, 24 10 2019) $"); // CVSキーワードの取り扱いに注意

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            _hasData = false;
            hasData = printMain(db2, svf);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    private List getPageList(final List studentList, final int maxLine) {
        final List rtn = new ArrayList();
        List current = null;
        String befGradeClass = "";
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (null == current || current.size() >= maxLine || !befGradeClass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }
        return rtn;
    }

//    private static List getCuttingPageList(final List studentList, final int maxLine) {
//        //アルゴリズムとして、先にクラス毎に何ページ出力するかを確認しつつ生徒データ格納用オブジェクトをページ数分用意して、
//        //クラス内で何番目の生徒なのかで、出力NO % 作成ページ数 で何番目のページかを確定する。
//        final List rtn = new ArrayList();
//        List current = null;
//        String befGradeClass = "";
//        final ArrayList classcntlist = new ArrayList();
//        StudentCnt objCnt = null;
//        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
//            final Student student = (Student) iter.next();
//            if (!befGradeClass.equals(student._grade + student._hrClass)) {
//                if (null != objCnt) {
//                    setPageCnt(objCnt, maxLine);
//                    for (int pcnt = 0;pcnt < objCnt._pagecnt;pcnt++) {
//                        current = new ArrayList();
//                        rtn.add(current);
//                    }
//                }
//                objCnt = new StudentCnt();
//                   objCnt._cnt = 0;
//                classcntlist.add(objCnt);
//            }
//            objCnt._cnt++;
//            befGradeClass = student._grade + student._hrClass;
//            if (!iter.hasNext()) {
//                setPageCnt(objCnt, maxLine);
//                for (int pcnt = 0;pcnt < objCnt._pagecnt;pcnt++) {
//                    current = new ArrayList();
//                    rtn.add(current);
//                }
//            }
//        }
//
//        befGradeClass = "";
//        int reccnt = 0;
//        int totalPageCnt = 0;
//        int classCnt = 0;
//        objCnt = null;
//        current = null;
//        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
//            final Student student = (Student) iter.next();
//            if (null == objCnt || !befGradeClass.equals(student._grade + student._hrClass)) {
//                if (null != objCnt) {
//                    totalPageCnt += objCnt._pagecnt;
//                }
//                objCnt = (StudentCnt)classcntlist.get(classCnt);
//                classCnt++;
//                reccnt = 0;
//            }
//            reccnt++;
//            current = (ArrayList)rtn.get(totalPageCnt + getPageNo(objCnt, reccnt));
//            current.add(student);
//            befGradeClass = student._grade + student._hrClass;
//        }
//
//        return rtn;
//    }

//    private static class StudentCnt {
//        int _cnt;
//        int _pagecnt;
//    }
//    private static void setPageCnt(StudentCnt sobj, final int maxLine) {
//        final BigDecimal bigCnt = new BigDecimal(sobj._cnt);
//        final BigDecimal bigMaxLine = new BigDecimal(maxLine);
//        sobj._pagecnt = bigCnt.divide(bigMaxLine, BigDecimal.ROUND_CEILING).intValue();
//    }
//    private static int getPageNo(StudentCnt sobj,final int studentno) {
//        int retpageno = 0;
//        retpageno = ((studentno - 1) % sobj._pagecnt);
//        return retpageno;
//    }

    private static boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASS9.equals(subclassCd) || "99999A".equals(subclassCd) || "99999B".equals(subclassCd);
    }

    private static String getSubclassCd(final ResultSet rs, final Param param) throws SQLException {
        final String subclassCd;
        if (!isSubclassAll(rs.getString("SUBCLASSCD"))) {
            subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
        } else {
            subclassCd = rs.getString("SUBCLASSCD");
        }
        return subclassCd;
    }

    private static String roundHalfUp(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List studentList = Student.getStudentList(db2, _param);

        Student.setMapTestSubclass(db2, _param, studentList);
        TestScore.setChairStd(db2, _param, studentList);

        TestScore.setRank(db2, _param, studentList);
        TestScore.setAvg(db2, _param, studentList);
        if (!_param.isKetten()) {
            TestScore.setKetten(db2, _param, studentList);
        }
        TestScore.setRankAvgAverage(db2, _param, studentList);

        return studentList;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        final List studentListAll = getStudentList(db2);
        log.debug(" studentList size = " + studentListAll.size());

        final String form;
        if ("2".equals(_param._outputSubclass)) {
            form = "KNJD192N_2.frm";
        } else {
            form = "KNJD192N.frm";
        }

        final int maxLine = 4;
//        final List pageList = _param._sortCutting ? getCuttingPageList(studentListAll, maxLine) : getPageList(studentListAll, maxLine);
        final List pageList = getPageList(studentListAll, maxLine);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List studentList = (List) it.next();

            for (int gyo = 1; gyo <=  studentList.size(); gyo++) {
                final Student student = (Student) studentList.get(gyo - 1);

                svf.VrSetForm(form, 4);

                if ("1".equals(_param._notPrintKekka)) {
                    for (int gyos = 1; gyos <= maxLine; gyos++) {
                        for (int i = 0; i < 20; i++) {
                            svf.VrAttributen("SLASH" + String.valueOf(i), gyos, "X=10000");
                        }
                    }
                }

                setPrintOut(svf, student, gyo);
                svf.VrEndPage();

            }
        }
        return _hasData;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int gyo) {
        //ベースの処理で結構なパラメータがあったので、そのまま残しているが、
        //paramの殆どの変数は、固定値となっている。
        svf.VrsOut("TITLE", _param._nendo + _param._semesterName + _param._testName + " 成績個人票");
        if (_param._isOutputDebug) {
            log.info(" schregno = " + student._schregno);
        }

        final String hrNameVal;
        if ("1".equals(_param._use_SchregNo_hyoji)) {  //生徒氏名
            hrNameVal = student._hrName + "(" + student._schregno + ")　" + student._name; //学籍番号
        } else  {
            hrNameVal = student._hrName + "(" + student._attendNo + ")　" + student._name; //年組番
        }
        final String hrNameField;
        if ("tosajoshi".equals(_param._z010)) {
            hrNameField = KNJ_EditEdit.getMS932ByteLength(hrNameVal) > 48 ? "HR_NAME2_3" : KNJ_EditEdit.getMS932ByteLength(hrNameVal) > 36 ? "HR_NAME2_2" : "HR_NAME2_1";
        } else  {
            hrNameField = "HR_NAME1";
        }
        svf.VrsOut(hrNameField, hrNameVal);
        svf.VrAttribute(hrNameField, "UnderLine=(0,1,1)"); //アンダーライン

        if (student._hasScore) {
            svf.VrsOut("TOTAL_SCORE", student._testAll._score);  //②得点
            svf.VrsOut("AVERAGE_SCORE", student._testAll._average);  //③平均
            if (!"1".equals(_param._notPrintRank)) {
            	svf.VrsOut("TOTAL_RANK", student._testAll._rankHr);  //④クラス順位
            }
//            if (!"1".equals(_param._notPrintAvg) && _param._useTotalAverage) {
//                svf.VrsOut("AVERAGEL_SCORE", student._testAll._average);  //学年平均
//            }
            if (!"1".equals(_param._notPrintRank)) {
            	svf.VrsOut("TOTAL_EXEC_NO", String.valueOf(student._testAll._cntHr));  //⑤クラス受験者数
            }
        }

        //レコード出力対応のフォームなので、先に合計を出力する。
        if ("H".equals(_param._schoolKind)) {
        	final List avgHrList = new ArrayList();
        	BigDecimal sum = new BigDecimal(0);
            for (final Iterator its = student._testSubclass.values().iterator(); its.hasNext();) {
                final TestScore testScore = (TestScore) its.next();
                if (NumberUtils.isNumber(testScore._avgHr)) {
                	final BigDecimal avgHrBd = new BigDecimal(testScore._avgHr);
                	sum = sum.add(avgHrBd);
					avgHrList.add(avgHrBd);
                }
            }
            if (!avgHrList.isEmpty()) {
                if (!"1".equals(_param._notPrintAvg)) {
                	final BigDecimal avg = sum.divide(new BigDecimal(avgHrList.size()), 10, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("TOTAL_AVERAGE", roundHalfUp(sum));  // 各科目の平均点の合計
					svf.VrsOut("AVERAGE_AVERAGE", roundHalfUp(avg));     // 各科目の平均点の合計 / 科目数
                }
            }
        } else {
            if (!"1".equals(_param._notPrintAvg)) {
                svf.VrsOut("TOTAL_AVERAGE", student._testAll._totalHrAvg);  //⑥クラスの平均
                svf.VrsOut("AVERAGE_AVERAGE", student._testAll._avgHr);     //⑦各生徒の平均値の平均
            }
        }

        //科目別成績の出力
        final List printScoreList = new ArrayList(student._testSubclass.values());
        Collections.sort(printScoreList);
        for (final Iterator its = printScoreList.iterator(); its.hasNext();) {
            final TestScore testScore = (TestScore) its.next();
            if (_param._isOutputDebug) {
                log.info(" subclass = " + testScore._subclasscd + " " + testScore._classAbbv + " " + testScore._name + " (electDiv = " + testScore._electdiv + ")");
            }
            final String credit = null != testScore._credit ? "(" + testScore._credit + ")" : "";
            svf.VrsOut("CLASS_NAME", StringUtils.defaultString(testScore._classAbbv) + credit); //教科
            final String subClsName = StringUtils.defaultString(testScore._name);
            final int scnlen = KNJ_EditEdit.getMS932ByteLength(subClsName);
            final String scnfield = scnlen > 18 ? "_2" : "_1";
            svf.VrsOut("SUBCLASS_NAME1" + scnfield, StringUtils.defaultString(testScore._name) + credit);  //科目

            if (student._recordScoreSubclass.containsKey(testScore._subclasscd)) {
                final String valuDi = (String) student._recordScoreSubclass.get(testScore._subclasscd);
                if ("*".equals(valuDi)) {
                    testScore._score = "欠";
                }
            }

            svf.VrsOut("SCORE", testScore._score);  //得点
            if (NumberUtils.isDigits(testScore._score)) {
                final int score = Integer.parseInt(testScore._score);
                if (_param._useKetten && !_param._notUseKetten) {
                    if (testScore.isKetten(score, _param)) {
                        svf.VrAttribute("SCORE", "Palette=9");
                    }
                }
            }

            final String chkKey = testScore._subclasscd;
            if (!_param._ignoreRankClsCdMap.containsKey(chkKey)) {
                svf.VrsOut("RANK", testScore._rankHr);  //ホーム順位
            }
            svf.VrsOut("EXEC_NUM", String.valueOf(testScore._cntHr)); //受験者数
            svf.VrsOut("AVERAGE", testScore._avgHr);  //ホーム平均
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final boolean _majorHas1Hr;
        final Map _testSubclass;
        final Map _recordScoreSubclass;
        final TestScore _testAll;
        boolean _hasScore;
        /** 個人の平均点 */
        String _averageScore;  //生徒の平均点(SUM(各科目の得点)/科目数)

        /**
         * コンストラクタ。
         */
        public Student(
                final Param param,
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName,
                final boolean majorHas1Hr
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _majorHas1Hr = majorHas1Hr;
            _testSubclass = new TreeMap();
            _recordScoreSubclass = new TreeMap();
            _testAll = new TestScore(param, "999999");
            _testAll._score = "";
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
//                log.debug("getStudentInfoSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(param,
                                                  rs.getString("SCHREGNO"),
                                                  rs.getString("GRADE"),
                                                  rs.getString("HR_CLASS"),
                                                  rs.getString("ATTENDNO"),
                                                  rs.getString("HR_NAME"),
                                                  rs.getString("HR_NAMEABBV"),
                                                  "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"),
                                                  rs.getString("COURSECD"),
                                                  rs.getString("COURSENAME"),
                                                  rs.getString("MAJORCD"),
                                                  rs.getString("MAJORNAME"),
                                                  rs.getString("COURSECODE"),
                                                  rs.getString("COURSECODENAME"),
                                                  "1".equals(rs.getString("MAJOR_HAS_1_HR")));
                    studentList.add(student);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return studentList;
        }

        private static String getStudentInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAJOR_HR_COUNT AS ( ");
            stb.append(" SELECT ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     COUNT(DISTINCT HR_CLASS) AS MAJOR_HR_COUNT "); // 学科ごとのHR数
            stb.append(" FROM ");
            stb.append("     V_SCHREG_INFO VSCH ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VSCH.SCHREGNO, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO, ");
            stb.append("     VSCH.HR_NAME, ");
            stb.append("     VSCH.HR_NAMEABBV, ");
            stb.append("     VSCH.NAME, ");
            stb.append("     BASE.REAL_NAME, ");
            stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     L1.COURSENAME, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     L1.MAJORNAME, ");
            stb.append("     VSCH.COURSECODE, ");
            stb.append("     L2.COURSECODENAME, ");
            stb.append("     CASE WHEN VALUE(MAJOR_HR_COUNT, 0) = 1 THEN '1' END AS MAJOR_HAS_1_HR ");
            stb.append(" FROM ");
            stb.append("     V_SCHREG_INFO VSCH ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
            stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
            stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
            stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
            stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
            stb.append("     LEFT JOIN MAJOR_HR_COUNT L5 ON L5.COURSECD = VSCH.COURSECD AND L5.MAJORCD = VSCH.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._schregSemester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS IN " + SQLUtils.whereIn(true, param._selectData) + " ");
            } else {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("     AND VSCH.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectData) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }

        private static void setMapTestSubclass(final DB2UDB db2, final Param param, final List studentList) throws SQLException {

            final String sql = TestScore.getRecordScoreSql(param, param._year, param._testcd);
            PreparedStatement ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ResultSet rs = null;
                try {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        student._recordScoreSubclass.put(getSubclassCd(rs, param), rs.getString("VALUE_DI"));
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            }
            DbUtils.closeQuietly(ps);
        }
    }

    private static class TestScore implements Comparable {
        final String _subclasscd;
        String _name;
        String _classAbbv;
        String _combinedSubclasscd;
        String _electdiv;
        String _score;      // 生徒の得点 or 生徒の総得点
        // testAllの平均値については、*のついている方で再設定しているので注意。
//        String _avg;        // クラスの科目別平均 or クラス(&コース)の総合平均 or *クラスの平均値平均(999999限定)
        String _avgHr;      // クラスの科目別平均 or クラスの各科目得点の合計から割り出した平均(9科目外も対象) or *各生徒の平均値の平均(999999限定)
        String _totalHrAvg; // *クラスの総合得点の平均(999999限定)
//        String _rank;       //学年順位
        String _rankHr;     //学級順位
        String _credit;
//        String _absenceHigh;
//        String _getAbsenceHigh;
        String _average;    //その生徒の科目総合平均
        String _slumpScore;
        String _slumpMark;
//        String _sidouinput;
//        String _sidouinputinf;
//        String _passScore;
//        Double _kekka;
//        int _cnt;
        int _cntHr;
        final Param _param;
        public TestScore(final Param param, final String subclasscd) {
            _param = param;
            _subclasscd = subclasscd;
        }

        private String getClassCd() {
        	return _subclasscd.substring(0, 2);
        }

        private int getFailValue(final Param param) {
            if (param.isKetten() && null != param._ketten && !"".equals(param._ketten)) {
                return "J".equals(param._schoolKind) ? 25 : 30;
            }
            return -1;
        }

        private boolean isKetten(int score, final Param param) {
            if (param.isRecordSlump()) {
                if (null != param._testcd && param._testcd.endsWith("09")) { // 評定、仮評定は1の場合欠点
                    return 1 == score;
                }
                return "1".equals(_slumpScore);
            } else {
                return score < getFailValue(param);
            }
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
//                    + " 平均：" + _avg
                    + " 席次：" + _rankHr;
        }

        private static String getRecordScoreSql(final Param param, final String year, final String testcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = '" + param._schregSemester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static void setRank(final DB2UDB db2, final Param param, final List studentList) throws SQLException {

            final String sql = getRecordRankTestAppointSql(param._year, param._semester, param._testcd);
            log.debug("setRank" + sql);
            PreparedStatement ps = null;
            try {
//                log.debug(rankSql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    int subclassCount = 0;
                    boolean hasScore = false;
                    int totalScore = 0;
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            hasScore = true;
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

                            testScore._score = rs.getString("SCORE");

//                            testScore._rank = rs.getString(param.getRankField() + param.getRankAvgField() + "RANK");
                            testScore._rankHr = rs.getString("CLASS_" + param.getRankAvgField() + "RANK");

                            if (!"欠".equals(testScore._score)) {
                                totalScore += Integer.parseInt(testScore._score);
                            }
                            subclassCount += 1;
                        }
                        if (subclassCd.equals(SUBCLASS9)) {
                            student._testAll._score = rs.getString("SCORE");
//                            student._testAll._rank = rs.getString(param.getRankField() + param.getRankAvgField() + "RANK");
                            student._testAll._rankHr = rs.getString("CLASS_" + param.getRankAvgField() + "RANK");
                            student._testAll._average = roundHalfUp(rs.getBigDecimal("AVG"));
                        }
                    }
                    DbUtils.closeQuietly(rs);
                    student._hasScore = hasScore;
                    if (hasScore) {
                        student._averageScore = divide(totalScore, subclassCount);
                    }
                }

            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 席次データの取得
         * @return sql
         */
        public static String getRecordRankTestAppointSql(
                final String year,
                final String semester,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static String getAverageParam(final int paramdiv, final String avgDiv, final Student student) {
            final String AVG_DIV_GRADE = "1";
            final String AVG_DIV_HR = "2";
            final String AVG_DIV_COURSE = "3";
            final String AVG_DIV_MAJOR = "4";
            String rtn = null;
            if (paramdiv == 2) { // HR
                if (AVG_DIV_GRADE.equals(avgDiv)) {
                    rtn = "000";
                } else if (AVG_DIV_HR.equals(avgDiv)) {
                    rtn = student._hrClass;
                } else if (AVG_DIV_COURSE.equals(avgDiv)) {
                    rtn = "000";
                } else if (AVG_DIV_MAJOR.equals(avgDiv)) {
                    rtn = "000";
                }
            } else if (paramdiv == 3) { // コース
                if (AVG_DIV_GRADE.equals(avgDiv)) {
                    rtn = "00000000";
                } else if (AVG_DIV_HR.equals(avgDiv)) {
                    rtn = "00000000";
                } else if (AVG_DIV_COURSE.equals(avgDiv)) {
                    rtn = student._courseCd + student._majorCd + student._courseCode;
                } else if (AVG_DIV_MAJOR.equals(avgDiv)) {
                    rtn = student._courseCd + student._majorCd + "0000";
                }
            }
            return rtn;
        }

        private static void setAvg(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            PreparedStatement ps = null;
//            // 学年/コースの平均
//            try {
//                final String avgDiv = param.getAvgDiv();
//                final String sql = getAverageSql(param._year, param._semester, avgDiv, param._testcd);
//                ps = db2.prepareStatement(sql);
//
//                for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    ps.setString(1, student._grade);
//                    ps.setString(2, getAverageParam(2, avgDiv, student));
//                    ps.setString(3, getAverageParam(3, avgDiv, student));
//
//                    ResultSet rs = ps.executeQuery();
//
//                    while (rs.next()) {
//                        final String subclassCd = getSubclassCd(rs, param);
//                        if (student._testSubclass.containsKey(subclassCd)) {
//                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
//                            testScore._avg = roundHalfUp(rs.getBigDecimal("AVG")); //クラスの科目別平均
//                        }
//                        if (SUBCLASS9.equals(subclassCd)) {
//                            student._testAll._cnt = rs.getInt("COUNT");  //クラスの受験者数
//                            student._testAll._totalHrAvg = roundHalfUp(rs.getBigDecimal("AVG")); //クラスの総合平均
//                        }
//                    }
//                    DbUtils.closeQuietly(rs);
//
//                }
//            } finally {
//                DbUtils.closeQuietly(ps);
//                db2.commit();
//            }

            // 学級の平均
            try {
                final String avgDiv = "2";
                final String sql = getAverageSql(param._year, param._semester, avgDiv, param._testcd);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._grade);
                    ps.setString(2, getAverageParam(2, avgDiv, student));
                    ps.setString(3, getAverageParam(3, avgDiv, student));

                    ResultSet rs = ps.executeQuery();
//                    int totalScore = 0;
//                    int totalCount = 0;
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                            testScore._avgHr = roundHalfUp(rs.getBigDecimal("AVG")); //クラスの科目別平均
                            testScore._cntHr = rs.getInt("COUNT");
                        }
                        if (SUBCLASS9.equals(subclassCd)) {
                            student._testAll._cntHr = rs.getInt("COUNT");
                            student._testAll._totalHrAvg = roundHalfUp(rs.getBigDecimal("AVG")); //クラスの総合平均
                        }
//                        if (!isSubclassAll(subclassCd)) {
//                            totalScore += rs.getInt("SCORE");
//                            totalCount += rs.getInt("COUNT");
//                        }
                    }
                    DbUtils.closeQuietly(rs);
//                    if (totalCount > 0) {
//                        student._testAll._avgHr = divide(totalScore, totalCount);  //クラスの各科目得点の合計から割り出した平均(9科目外も対象)
//                    }
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 平均データの取得
         * @return sql
         */
        public static String getAverageSql(
                final String year,
                final String semester,
                final String avgDiv,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");

            return stb.toString();
        }

        private static void setKetten(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            PreparedStatement ps = null;
            // 欠点対象
            try {
                final String sql = getKettenSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
//                            testScore._sidouinput = rs.getString("SIDOU_INPUT");
//                            testScore._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                            testScore._slumpScore = rs.getString("SLUMP_SCORE");
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String getKettenSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
                //成績不振科目データの表
                stb.append("   WITH REL_COUNT AS (");
                stb.append("   SELECT SUBCLASSCD");
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
                stb.append("     , COUNT(*) AS COUNT ");
                stb.append("          FROM RELATIVEASSESS_MST ");
                stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
                stb.append("   GROUP BY SUBCLASSCD");
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
                stb.append("   ) ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     cast(null as smallint) as PASS_SCORE, ");
                stb.append("     W1.SIDOU_INPUT, ");
                stb.append("     W1.SIDOU_INPUT_INF, ");
                stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM RELATIVEASSESS_MST L3 ");
                stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
                stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("             AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     AND L3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("          ) ELSE ");
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          ) ");
                stb.append("    END AS SLUMP_SCORE ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT T1 ");
                stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON T1.YEAR = W1.YEAR ");
                stb.append("            AND T1.SEMESTER = W1.SEMESTER ");
                stb.append("            AND T1.TESTKINDCD = W1.TESTKINDCD ");
                stb.append("            AND T1.TESTITEMCD = W1.TESTITEMCD ");
                stb.append("            AND T1.SCORE_DIV = W1.SCORE_DIV ");
                stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' AND ");
                stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' AND ");
                stb.append("     T1.SCHREGNO = ? ");
            return stb.toString();
        }

        private static void setChairStd(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            ResultSet rs;
            PreparedStatement ps;
            String sql = getChairStdSql(param);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = getSubclassCd(rs, param);
                    String strKetu = "";
                    //if (student._recordScoreSubclass.containsKey(subclassCd)) {
                    //    strKetu = "欠";
                    //}
                    if (!param._printTestOnly || param._printTestOnly && student._recordScoreSubclass.containsKey(subclassCd)) {
                        final TestScore testScore = new TestScore(param, subclassCd);
                        testScore._score = strKetu;
                        testScore._electdiv = rs.getString("ELECTDIV");
                        testScore._name = rs.getString("SUBCLASSABBV");
                        testScore._classAbbv = rs.getString("CLASSABBV");
                        testScore._combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");
                        student._testSubclass.put(subclassCd, testScore);
                    }
                }
                DbUtils.closeQuietly(rs);
            }
            DbUtils.closeQuietly(ps);
        }

        private static String getChairStdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH COMBINED_SUBCLASS AS (");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.COMBINED_CLASSCD, ");
            stb.append("     T1.COMBINED_SCHOOL_KIND, ");
            stb.append("     T1.COMBINED_CURRICULUM_CD, ");
            stb.append("     T1.COMBINED_SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append(" )");

            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     L0.CLASSABBV, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L1.ELECTDIV, ");
            stb.append("     L2.COMBINED_SUBCLASSCD, ");
            stb.append("     L1.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("     LEFT JOIN CLASS_MST L0 ");
            stb.append("       ON L0.CLASSCD = T1.CLASSCD ");
            stb.append("      AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     LEFT JOIN COMBINED_SUBCLASS L2 ON L2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T1.CLASSCD = L2.COMBINED_CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = L2.COMBINED_SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ");
            stb.append("     , CHAIR_STD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
            stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
            stb.append("     T2.YEAR = T1.YEAR AND ");
            stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append("     T2.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }

        private static String divide(final int v1, final int v2) {
            return new BigDecimal(v1).divide(new BigDecimal(v2), 1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static void setRankAvgAverage(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            final Map cache = new HashMap();

            final String avgDivHr = "2";
            final String sql = sqlRankAvgAverage(param, avgDivHr);
            PreparedStatement ps = db2.prepareStatement(sql);
            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();

                final String cacheKey = student._hrClass;
                if (!getMappedMap(cache, avgDivHr).containsKey(cacheKey)) {

                    ps.setString(1, student._hrClass);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        getMappedMap(cache, avgDivHr).put(cacheKey, rs.getString("AVG_HR_AVERAGE"));
                    }
                    DbUtils.closeQuietly(rs);

                }
                student._testAll._avgHr = (String) getMappedMap(cache, avgDivHr).get(cacheKey);  //各生徒の平均値の平均

            }
            DbUtils.closeQuietly(ps);


//            final String avgDiv = param.getAvgDiv();
//            final String sql2 = sqlRankAvgAverage(param, avgDiv);
//            PreparedStatement ps2 = db2.prepareStatement(sql2);
//            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
//
//                final Student student = (Student) stit.next();
//                String cacheKey = null;
//
//                if ("1".equals(avgDiv)) {
//                    cacheKey = student._grade;
//                } else if ("3".equals(avgDiv)) {
//                    cacheKey = student._courseCd + student._majorCd + student._courseCode;
//                } else if ("4".equals(avgDiv)) {
//                    cacheKey = student._courseCd + student._majorCd;
//                }
//
//                if (!getMappedMap(cache, avgDiv).containsKey(cacheKey)) {
//                    log.info(" q " + cacheKey);
//                    if ("1".equals(avgDiv)) {
//                    } else if ("3".equals(avgDiv)) {
//                        ps2.setString(1, student._courseCd);
//                        ps2.setString(2, student._majorCd);
//                        ps2.setString(3, student._courseCode);
//                    } else if ("4".equals(avgDiv)) {
//                        ps2.setString(1, student._courseCd);
//                        ps2.setString(2, student._majorCd);
//                    }
//
//                    ResultSet rs = ps2.executeQuery();
//                    while (rs.next()) {
//                        getMappedMap(cache, avgDiv).put(cacheKey, rs.getString("AVG_HR_AVERAGE"));
//                    }
//                    DbUtils.closeQuietly(rs);
//                }
//                student._testAll._avg = (String) getMappedMap(cache, avgDiv).get(cacheKey);  //クラスの平均値平均
//            }
//            DbUtils.closeQuietly(ps2);
        }

        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private static String sqlRankAvgAverage(final Param param, final String avgDiv) {
            final String semester = param._testcd.substring(0, 1);
            final String testkindcd = param._testcd.substring(1, 3);
            final String testitemcd = param._testcd.substring(3, 5);
            final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._schregSemester + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = '" + param._grade + "' ");
            if ("2".equals(avgDiv)) {
                stb.append("            AND W1.HR_CLASS = ? ");
            } else if ("1".equals(avgDiv)) {
            } else if ("3".equals(avgDiv)) {
                stb.append("            AND W1.COURSECD = ? AND W1.MAJORCD = ? AND W1.COURSECODE = ? ");
            } else if ("4".equals(avgDiv)) {
                stb.append("            AND W1.COURSECD = ? AND W1.MAJORCD = ? ");
            }
            stb.append(") ");

            stb.append("SELECT ");
            stb.append("        DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append("  INNER JOIN SCHNO_A T2 ON T2.SCHREGNO = W3.SCHREGNO ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + semester + "' AND W3.TESTKINDCD = '" + testkindcd + "' AND W3.TESTITEMCD = '" + testitemcd + "' AND W3.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + SUBCLASS9 + "' ");

            return stb.toString();
        }

        public int compareTo(Object o) {
            if (o instanceof TestScore) {
                TestScore ts = (TestScore) o;
                int cmp;
                if ("1".equals(_param._orderUseElectdiv)) {
                    cmp = StringUtils.defaultString(_electdiv, "0").compareTo(StringUtils.defaultString(ts._electdiv, "0"));
                    if (0 != cmp) {
                        return cmp;
                    }
                }
                cmp = _subclasscd.compareTo(ts._subclasscd);
                return cmp;
            }
            return 0;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _categoryIsClass;
        final String _testcd;
        final String _testName;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _groupDiv;
//        final String _outputKijun;
        final String[] _selectData;
        final String _z010;
//        final boolean _isSeireki;
        final String _ketten;
        final String _schregSemester;
        final String _orderUseElectdiv;
        private String _semesterName;
//        private String _d054Namecd2Max;

        /** 試験科目のみ出力する */
        final boolean _printTestOnly;
        /** 順位を出力しない */
        final String _notPrintRank;
        /** 平均点を出力しない */
        final String _notPrintAvg;
        /** 欠課を出力しない */
        final String _notPrintKekka;
        /** 欠点を使用しない */
        final boolean _useKetten;
        /** 欠点を使用しない */
        final boolean _notUseKetten;
//        /** 裁断用にソートして出力する */
//        final boolean _sortCutting;

        private KNJSchoolMst _knjSchoolMst;
        final Map _attendParamMap;
        final boolean _isOutputDebug;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        final String _nendo;
        final String _schoolKind;
        final String _outputSubclass;

        final Map _ignoreRankClsCdMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _testcd = request.getParameter("SUB_TESTCD");
            _semester = request.getParameter("SEMESTER");
            _schregSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _groupDiv = "1";
//            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _orderUseElectdiv = request.getParameter("ORDER_USE_ELECTDIV");

            _z010 = setNameMst(db2, "Z010", "00");
//            _isSeireki = "2".equals(setNameMst(db2, "Z012", "01"));

            _ketten = "30";  //request.getParameter("KETTEN");
            _useKetten = true;
            _notUseKetten = "1".equals(request.getParameter("notUseKetten"));

            _printTestOnly = true; // null != request.getParameter("TEST_ONLY");
            _notPrintRank = request.getParameter("NOT_PRINT_RANK");
            _notPrintAvg = request.getParameter("NOT_PRINT_AVG");
            _notPrintKekka = request.getParameter("NOT_PRINT_KEKKA");

            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");

//            _sortCutting = "1".equals(request.getParameter("SORT_CUTTING"));

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }

//            setD054Namecd2Max(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _definecode = createDefineCode(db2);

            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _outputSubclass = request.getParameter("OUTPUT_SUBCLASS");
            _ignoreRankClsCdMap = getIgnoreRankClsCdMap(db2);
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD192N' AND NAME = '" + propName + "' "));
        }

//        private void setD054Namecd2Max(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                StringBuffer stb = new StringBuffer();
//                stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
//                stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    _d054Namecd2Max = rs.getString("NAMECD2");
//                }
//            } catch (SQLException ex) {
//                log.debug("getZ010 exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

        private String getTestName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String seme = rs.getString("SEMESTER");
                    if (_semester.equals(seme)) {
                        _semesterName = rs.getString("SEMESTERNAME");
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNameMst(_year, namecd1, namecd2);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + ("".equals(namecd2) ? "" : "     AND NAMECD2 = '" + namecd2 + "'");
            return rtnSql;
        }

//        private String getRankField() {
//            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
//                return "GRADE_";
//            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
//                return "COURSE_";
//            } else if ("5".equals(_groupDiv)) {
//                return "MAJOR_";
//            }
//            return null;
//        }

        private String getRankAvgField() {
            return "H".equals(_schoolKind) ? "AVG_" : "";
        }

        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return false; //"1".equals(_checkKettenDiv) && (null == _ketten || "".equals(_ketten));
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        private boolean isKetten() {
            return !isRecordSlump();
        }

        private Map getIgnoreRankClsCdMap(final DB2UDB db2) {
        	Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNameMst(_year, "D103", "");
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAME1"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        	return retMap;
        }

    }
}

// eof
