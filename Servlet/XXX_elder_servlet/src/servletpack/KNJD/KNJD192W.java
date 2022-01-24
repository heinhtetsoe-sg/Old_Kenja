// kanji=漢字
/*
 * $Id: b7ece097a71817ed0fe5b62315dd62e1f03c08ae $
 *
 * 作成日: 2018/11/02 17:54:00 - JST
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @version $Id: b7ece097a71817ed0fe5b62315dd62e1f03c08ae $
 */
public class KNJD192W {

    private static final Log log = LogFactory.getLog("KNJD192W.class");

    private Param _param;

    private static final String SPECIAL_ALL = "999";

    private static final String SEMEALL = "9";

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
    private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";
    private static final String HYOTEI_TESTCD = "9990009";

    private static final int COLCNTMAX = 12;
    private static final int ONEPAGEMAX = 2;
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            log.fatal("$Revision: 63333 $ $Date: 2018-11-12 19:59:19 +0900 (月, 12 11 2018) $"); // CVSキーワードの取り扱いに注意

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(db2, svf);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
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

    private static List getCuttingPageList(final List studentList, final int maxLine) {
        //アルゴリズムとして、先にクラス毎に何ページ出力するかを確認しつつ生徒データ格納用オブジェクトをページ数分用意して、
        //クラス内で何番目の生徒なのかで、出力NO % 作成ページ数 で何番目のページかを確定する。
        final List rtn = new ArrayList();
        List current = null;
        String befGradeClass = "";
        final ArrayList classcntlist = new ArrayList();
        StudentCnt objCnt = null;
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (!befGradeClass.equals(student._grade + student._hrClass)) {
                if (null != objCnt) {
                    setPageCnt(objCnt, maxLine);
                    for (int pcnt = 0;pcnt < objCnt._pagecnt;pcnt++) {
                        current = new ArrayList();
                        rtn.add(current);
                    }
                }
                objCnt = new StudentCnt();
                   objCnt._cnt = 0;
                classcntlist.add(objCnt);
            }
            objCnt._cnt++;
            befGradeClass = student._grade + student._hrClass;
            if (!iter.hasNext()) {
                setPageCnt(objCnt, maxLine);
                for (int pcnt = 0;pcnt < objCnt._pagecnt;pcnt++) {
                    current = new ArrayList();
                    rtn.add(current);
                }
            }
        }

        befGradeClass = "";
        int reccnt = 0;
        int totalPageCnt = 0;
        int classCnt = 0;
        objCnt = null;
        current = null;
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
        	final Student student = (Student)iter.next();
            if (null == objCnt || !befGradeClass.equals(student._grade + student._hrClass)) {
                if (null != objCnt) {
                    totalPageCnt += objCnt._pagecnt;
                }
                objCnt = (StudentCnt)classcntlist.get(classCnt);
                classCnt++;
                reccnt = 0;
            }
            reccnt++;
            current = (ArrayList)rtn.get(totalPageCnt + getPageNo(objCnt, reccnt));
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }

        return rtn;
    }

    private static class StudentCnt {
        int _cnt;
        int _pagecnt;
    }
    private static void setPageCnt(StudentCnt sobj, final int maxLine) {
        final BigDecimal bigCnt = new BigDecimal(sobj._cnt);
        final BigDecimal bigMaxLine = new BigDecimal(maxLine);
        sobj._pagecnt = bigCnt.divide(bigMaxLine, BigDecimal.ROUND_CEILING).intValue();
    }
    private static int getPageNo(StudentCnt sobj,final int studentno) {
        int retpageno = 0;
        retpageno = ((studentno - 1) % sobj._pagecnt);
        return retpageno;
    }

    private static String roundHalfUp(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
    	final Map studentMap = new TreeMap();
        final List studentList = Student.getStudentList(db2, _param, studentMap);

        Student.setMapTestSubclass(db2, _param, studentMap);

        TestScore.setTotalRankAverage(db2, _param, studentMap);

        return studentList;
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;
        final List studentAllList = getStudentList(db2);
        log.debug(" studentList size = " + studentAllList.size());

        final String form = "KNJD192W.frm";

        final int maxLine = ONEPAGEMAX;
        final List pageList = _param._sortCutting ? getCuttingPageList(studentAllList, maxLine) : getPageList(studentAllList, maxLine);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List studentList = (List) it.next();

            svf.VrSetForm(form, 1);

            int nextgyo = 0;
            for (int gyo = 1; gyo <=  studentList.size(); gyo++) {
                final Student student = (Student) studentList.get(gyo - 1);

                setPrintOut(svf, student, gyo + nextgyo);
                if (student._testSubclassScore.size() > COLCNTMAX) {
                	if (roundupInt((student._testSubclassScore.size() == 0 ? 1 : student._testSubclassScore.size()), COLCNTMAX) % 2 == 0) {
                        svf.VrEndPage();
                        nextgyo = -1;
                	}
                } else {
                    nextgyo = 0;
                }
                hasData = true;
            }
            if (nextgyo == 0) {
                svf.VrEndPage();
            }
        }
        return hasData;
    }

    private static int roundupInt(final int v1, final int v2) {
        return new BigDecimal(v1).divide(new BigDecimal(v2), 0, BigDecimal.ROUND_UP).intValue();
    }
    private static String divide(final int v1, final int v2) {
        return new BigDecimal(v1).divide(new BigDecimal(v2), 1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static String sishagonyu(final String value) {
        return !NumberUtils.isNumber(value) ? null : new BigDecimal(value).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int gyo) {
        int subrowcnt = 0;
    	//ヘッダを出力
    	setTitle(svf, student, gyo + subrowcnt);
    	//合計・平均を出力
    	setTotal(svf, student, gyo + subrowcnt);

    	//科目別に出力
        int cntmax = COLCNTMAX;
        int colcnt = 1;
        for (final Iterator iter = student._testSubclassScore.keySet().iterator(); iter.hasNext();) {
            final String kstr = (String)iter.next();
            final TestScore ts = (TestScore)student._testSubclassScore.get(kstr);
            if (ts._score == null && ts._count1 == null) {
            	continue;
            }
            if (colcnt > cntmax) {
            	colcnt = 1;
            	subrowcnt++;
            	if (subrowcnt >= ONEPAGEMAX) {
            		svf.VrEndPage();
            		subrowcnt = 0;
            	}
            	//ヘッダを出力
            	setTitle(svf, student, gyo + subrowcnt);
            	//合計・平均を出力
            	setTotal(svf, student, gyo + subrowcnt);
            }
            svf.VrsOutn("SUBCLASS"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._subclassAbbv, ""));
            if (ts._chairAbbv != null && !"".equals(ts._chairAbbv)) {
                final int chairlen = KNJ_EditEdit.getMS932ByteLength(ts._chairAbbv);
                final String chairfield = chairlen > 12 ? "_3" : chairlen > 8 ? "_2" : "_1";
                svf.VrsOutn("CHAIR_NAME"+(colcnt)+chairfield, gyo + subrowcnt, StringUtils.defaultString(ts._chairAbbv, ""));
            }
            svf.VrsOutn("SCORE"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._score, ""));
            if (!_param._notUseKetten) {
                if (Integer.parseInt(ts._score) <= ts.getFailValue(_param)) {
            	    svf.VrAttributen("SCORE"+(colcnt), gyo + subrowcnt, "Paint=(1,70,1),Bold=1");
                }
            }
            svf.VrsOutn("CLASS_RANK"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._rank1, ""));
            svf.VrsOutn("CLASS_STUDENT"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._count1, ""));
            svf.VrsOutn("CLASS_AVERAGE"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(sishagonyu(ts._avg1), ""));
            svf.VrsOutn("CLASS_MAX"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._highScore1, ""));
            svf.VrsOutn("CLASS_MIN"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._lowScore1, ""));
            svf.VrsOutn("CLASS_DIV"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(sishagonyu(ts._deviation1), ""));
            if (null != ts._chairGroupCd) {
            	// 講座グループ順位
                svf.VrsOutn("RANK"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._rank2, ""));
                svf.VrsOutn("STUDENT"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._count2, ""));
                svf.VrsOutn("AVERAGE"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(sishagonyu(ts._avg2), ""));
                svf.VrsOutn("MAX"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._highScore2, ""));
                svf.VrsOutn("MIN"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._lowScore2, ""));
                svf.VrsOutn("DIV"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(sishagonyu(ts._deviation2), ""));
            } else {
            	// 講座グループの設定が無ければ、講座順位を出力する
                svf.VrsOutn("RANK"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._rank1, ""));
                svf.VrsOutn("STUDENT"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._count1, ""));
                svf.VrsOutn("AVERAGE"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(sishagonyu(ts._avg1), ""));
                svf.VrsOutn("MAX"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._highScore1, ""));
                svf.VrsOutn("MIN"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(ts._lowScore1, ""));
                svf.VrsOutn("DIV"+(colcnt), gyo + subrowcnt, StringUtils.defaultString(sishagonyu(ts._deviation1), ""));
            }
            colcnt++;
            svf.VrEndRecord();
        }
    }

    private void setTitle(final Vrw32alp svf, final Student student, final int gyo) {
    	//ヘッダ出力
    	////年度
        String warekistr = _param._nendo + "年度";
        svf.VrsOutn("NENDO", gyo, warekistr);
    	////学校名
        svf.VrsOutn("SCHOOL_NAME", gyo, String.valueOf(_param._knjSchoolMst._schoolName1) + "　" + StringUtils.defaultString(_param._semesterName) + "　" + StringUtils.defaultString(_param._testName));
    	////第xx学年
        svf.VrsOutn("GRADE", gyo, "第" + _param._gradeCd + "学年");
    	////学科名
        svf.VrsOutn("MAJOR_NAME", gyo, student._majorName);
    	////クラス名+出席番号
        svf.VrsOutn("HR_NAME", gyo, student._hrName + String.valueOf(Integer.parseInt(student._attendNo)) + "番");
    	////氏名
        svf.VrsOutn("NAME", gyo, student._name);
    }

    private void setTotal(final Vrw32alp svf, final Student student, final int gyo) {
        //合計を出力
        svf.VrsOutn("TOTAL_SCORE", gyo, String.valueOf(student._totalScore));

        //平均を出力
        svf.VrsOutn("AVERAGE_TITLE1", gyo, "平均");
        if (student._testSubclassScore.size() > 0) {
            svf.VrsOutn("AVERAGEL_SCORE", gyo, divide(student._totalScore, student._testSubclassScore.size()));
        }
        svf.VrsOutn("CLASS_RANK", gyo, StringUtils.defaultString(student._testAll._rank1, ""));
        svf.VrsOutn("CLASS_STUDENT", gyo, StringUtils.defaultString(student._testAll._count1, ""));
        svf.VrsOutn("TOTAL_CLASS_AVERAGE", gyo, StringUtils.defaultString(sishagonyu(student._testAll._avg1), ""));
        svf.VrsOutn("TOTAL_CLASS_MAX", gyo, StringUtils.defaultString(student._testAll._highScore1, ""));
        svf.VrsOutn("TOTAL_CLASS_MIN", gyo, StringUtils.defaultString(student._testAll._lowScore1, ""));
        svf.VrsOutn("AVERAGEL_CLASS_DIV", gyo, StringUtils.defaultString(sishagonyu(student._testAll._deviation1), ""));
        svf.VrsOutn("TOTAL_RANK", gyo, StringUtils.defaultString(student._testAll._rank2, ""));
        svf.VrsOutn("TOTAL_STUDENT", gyo, StringUtils.defaultString(student._testAll._count2, ""));
        svf.VrsOutn("TOTAL_AVERAGE", gyo, StringUtils.defaultString(sishagonyu(student._testAll._avg2), ""));
        svf.VrsOutn("TOTAL_MAX", gyo, StringUtils.defaultString(student._testAll._highScore2, ""));
        svf.VrsOutn("TOTAL_MIN", gyo, StringUtils.defaultString(student._testAll._lowScore2, ""));
        svf.VrsOutn("AVERAGEL_DIV", gyo, StringUtils.defaultString(sishagonyu(student._testAll._deviation2), ""));
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseGroupCd;

        final Map _testSubclassScore;  //成績縦1列分をMap保持。
        int _totalScore; //合計点
        final TestScore _testAll; //平均点1列分を保持

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
                final String name,
                final String courseCd,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseGroupCd
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseGroupCd = courseGroupCd;

            _testSubclassScore = new TreeMap();
            _testAll = new TestScore(param);
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        private static List getStudentList(final DB2UDB db2, final Param param, final Map studentMap) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
                log.debug("getStudentInfoSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(param,
                    		                      rs.getString("SCHREGNO"),
                                                  rs.getString("GRADE"),
                                                  rs.getString("HR_CLASS"),
                                                  rs.getString("ATTENDNO"),
                                                  rs.getString("HR_NAME"),
                                                  rs.getString("NAME"),
                                                  rs.getString("COURSECD"),
                                                  rs.getString("MAJORCD"),
                                                  rs.getString("MAJORNAME"),
                                                  rs.getString("COURSECODE"),
                                                  rs.getString("GROUP_CD"));
                    studentMap.put(rs.getString("SCHREGNO"), student);
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

            stb.append(" SELECT ");
            stb.append("  T3.MAJORNAME, ");
            stb.append("  T1.GRADE, ");
            stb.append("  T4.HR_CLASS, ");
            stb.append("  T4.HR_NAME, ");
            stb.append("  T1.ATTENDNO, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T2.NAME, ");
            stb.append("  T1.COURSECD, ");
            stb.append("  T1.MAJORCD, ");
            stb.append("  T1.COURSECODE, ");
            stb.append("  T5.GROUP_CD ");
            stb.append(" FROM ");
            stb.append("  SCHREG_REGD_DAT T1 ");
            stb.append("  LEFT JOIN SCHREG_BASE_MST T2 ");
            stb.append("    ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN MAJOR_MST T3 ");
            stb.append("    ON T3.COURSECD = T1.COURSECD ");
            stb.append("   AND T3.MAJORCD = T1.MAJORCD ");
            stb.append("  LEFT JOIN SCHREG_REGD_HDAT T4 ");
            stb.append("    ON T4.YEAR = T1.YEAR ");
            stb.append("   AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T4.GRADE = T1.GRADE ");
            stb.append("   AND T4.HR_CLASS = T1.HR_CLASS ");
            stb.append("  LEFT JOIN COURSE_GROUP_CD_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("   AND T5.GRADE = T1.GRADE ");
            stb.append("   AND T5.COURSECD = T1.COURSECD ");
            stb.append("   AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("   AND T5.COURSECODE = T1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER = '" + param._schregSemester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("  AND T1.GRADE || T1.HR_CLASS IN " + param._selectDataIn + " ");
            } else {
                stb.append("  AND T1.SCHREGNO IN " + param._selectDataIn + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("  T1.GRADE, ");
            stb.append("  T1.HR_CLASS, ");
            stb.append("  T1.ATTENDNO, ");
            stb.append("  T1.SCHREGNO ");

            return stb.toString();
        }

        private static void setMapTestSubclass(final DB2UDB db2, final Param param, final Map studentMap) throws SQLException {

            final String sql = TestScore.getRecordScoreSql(param);
            log.debug("testsubclass sql = " + sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = null;

            try {
                rs = ps.executeQuery();

                while (rs.next()) {
                	if (studentMap.containsKey(rs.getString("SCHREGNO"))) {
                		Student stuwk = (Student)studentMap.get(rs.getString("SCHREGNO"));
                		TestScore addwk = new TestScore(param);
                		addwk.setScore(rs.getString("SUBCLASSCD"),
                				rs.getString("SUBCLASSNAME"),
                				rs.getString("SUBCLASSABBV"),
                				rs.getString("CHAIRNAME"),
                				rs.getString("CHAIRABBV"),
                				rs.getString("SCORE"),
                				rs.getString("GRADE_RANK"),
                				rs.getString("COUNT1"),
                				rs.getString("AVG1"),
                				rs.getString("HIGHSCORE1"),
                				rs.getString("LOWSCORE1"),
                				rs.getString("DEVIATION1"),
                				rs.getString("CHAIR_GROUP_CD"),
                				rs.getString("CHAIR_GROUP_RANK"),
                				rs.getString("COUNT2"),
                				rs.getString("AVG2"),
                				rs.getString("HIGHSCORE2"),
                				rs.getString("LOWSCORE2"),
                				rs.getString("DEVIATION2"));
                		stuwk._testSubclassScore.put(rs.getString("SUBCLASSCD"), addwk);
                		stuwk._totalScore += Integer.parseInt(StringUtils.defaultString(rs.getString("SCORE"), "0"));
                	}
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
    }

    private static class TestScore {
    	String _subclassCd;
    	String _subclassName;
    	String _subclassAbbv;
    	String _chairName;
    	String _chairAbbv;
        String _score;
        String _rank1;
        String _count1;
        String _avg1;
        String _highScore1;
        String _lowScore1;
        String _deviation1;
        String _chairGroupCd;
        String _rank2;
        String _count2;
        String _avg2;
        String _highScore2;
        String _lowScore2;
        String _deviation2;
        final Param _param;
        public TestScore(final Param param) {
        	_param = param;
        }

        private void setScore(
        		final String subclassCd,
        		final String subclassName,
        		final String subclassAbbv,
        		final String chairName,
        		final String chairAbbv,
                final String score,
                final String rank1,
                final String count1,
                final String avg1,
                final String highScore1,
                final String lowScore1,
                final String deviation1,
                final String chairGroupCd,
                final String rank2,
                final String count2,
                final String avg2,
                final String highScore2,
                final String lowScore2,
                final String deviation2
        		) {
        	_subclassCd = subclassCd;
        	_subclassName = subclassName;
        	_subclassAbbv = subclassAbbv;
        	_chairName = chairName;
        	_chairAbbv = chairAbbv;
            _score = score;
            _rank1 = rank1;
            _count1 = count1;
            _avg1 = avg1;
            _highScore1 = highScore1;
            _lowScore1 = lowScore1;
            _deviation1 = deviation1;
            _chairGroupCd = chairGroupCd;
            _rank2 = rank2;
            _count2 = count2;
            _avg2 = avg2;
            _highScore2 = highScore2;
            _lowScore2 = lowScore2;
            _deviation2 = deviation2;
        }
        private int getFailValue(final Param param) {
        	if (!"1".equals(param._checkKettenDiv) && !"2".equals(param._checkKettenDiv)) {
                if (null != param._ketten && !"".equals(param._ketten)) {
                    return Integer.parseInt(param._ketten);
                }
        	}
            return -1;
        }

        public String toString() {
            return "得点1：" + _score
                    + " 平均1：" + _avg1
                    + " 席次1：" + _rank1
                    + " 平均2：" + _avg2
                    + " 席次2：" + _rank2;
        }

        private static String getRecordScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH CHAIR_GROUP AS ( ");
            stb.append("  SELECT ");
            stb.append("      T1.CHAIRCD ");
            stb.append("      , MIN(T1.CHAIR_GROUP_CD) AS CHAIR_GROUP_CD "); // KNJD210Vに合わせる
            stb.append("  FROM CHAIR_GROUP_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("      T1.YEAR = '" + param._year + "' ");
            stb.append("      AND T1.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("      AND T1.TESTKINDCD = '00' "); // プロパティーuseKoteiTestCd=1
            stb.append("      AND T1.TESTITEMCD = '00' ");
            stb.append("  GROUP BY ");
            stb.append("      T1.CHAIRCD ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  (T2.CLASSCD || '-' ||  T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD) AS SUBCLASSCD, ");
            } else {
                stb.append("  T2.SUBCLASSCD, ");
            }
            stb.append("  T6.SUBCLASSNAME, ");
            stb.append("  T6.SUBCLASSABBV, ");
            stb.append("  T5.CHAIRNAME, ");
            stb.append("  T5.CHAIRABBV, ");
            stb.append("  T2.SCORE, ");
            stb.append("  T2.GRADE_RANK, ");
            stb.append("  T31.COUNT AS COUNT1, ");
            stb.append("  T31.AVG AS AVG1, ");
            stb.append("  T31.HIGHSCORE AS HIGHSCORE1, ");
            stb.append("  T31.LOWSCORE AS LOWSCORE1, ");
            stb.append("  T2.GRADE_DEVIATION AS DEVIATION1, ");
            stb.append("  SUBSTR(T_GRP.CHAIR_GROUP_CD, LENGTH(T_GRP.CHAIR_GROUP_CD) - 2, 3) AS CHAIR_GROUP_CD, ");
            stb.append("  T22.CHAIR_GROUP_RANK, ");
            stb.append("  T32.COUNT AS COUNT2, ");
            stb.append("  T32.AVG AS AVG2, ");
            stb.append("  T32.HIGHSCORE AS HIGHSCORE2, ");
            stb.append("  T32.LOWSCORE AS LOWSCORE2, ");
            stb.append("  T22.CHAIR_GROUP_DEVIATION AS DEVIATION2 ");
            stb.append(" FROM ");
            stb.append("  SCHREG_REGD_DAT T1 ");
            stb.append("  LEFT JOIN RECORD_RANK_CHAIR_SDIV_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("   AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   AND NOT(T2.SUBCLASSCD = '" + SUBCLASS9 + "') ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT T31 ");
            stb.append("    ON T31.YEAR = T1.YEAR ");
            stb.append("   AND T31.SEMESTER = T2.SEMESTER ");
            stb.append("   AND T31.GRADE = T1.GRADE ");
            stb.append("   AND T31.HR_CLASS = '000' ");
            stb.append("   AND T31.COURSECD = '0' ");
            stb.append("   AND T31.MAJORCD = '000' ");
            stb.append("   AND T31.AVG_DIV = '1' ");     //1:学年
            stb.append("   AND T31.COURSECODE = '0000' ");
            stb.append("   AND T31.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("   AND T31.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("   AND T31.SCORE_DIV = T2.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   AND T31.CLASSCD = T2.CLASSCD ");
                stb.append("   AND T31.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND T31.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("   AND T31.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("   AND T31.CHAIRCD = T2.CHAIRCD ");
            stb.append("  LEFT JOIN CHAIR_GROUP T_GRP ON T_GRP.CHAIRCD = T2.CHAIRCD ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T22 ");
            stb.append("    ON T22.YEAR = T1.YEAR ");
            stb.append("   AND T22.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND T22.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("   AND T22.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("   AND T22.SCORE_DIV = T2.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   AND T22.CLASSCD = T2.CLASSCD ");
                stb.append("   AND T22.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND T22.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("   AND T22.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("   AND T22.SCHREGNO = T1.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T32 ");
            stb.append("    ON T32.YEAR = T1.YEAR ");
            stb.append("   AND T32.SEMESTER = T2.SEMESTER ");
            stb.append("   AND T32.GRADE = T1.GRADE ");
            stb.append("   AND T32.HR_CLASS = '000' ");
            stb.append("   AND T32.COURSECD = '0' ");
            stb.append("   AND T32.MAJORCD = SUBSTR(T_GRP.CHAIR_GROUP_CD, LENGTH(T_GRP.CHAIR_GROUP_CD) - 2, 3) ");
            stb.append("   AND T32.COURSECODE = '0000' ");
            stb.append("   AND T32.AVG_DIV = '6' ");  //6:講座グループ
            stb.append("   AND T32.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("   AND T32.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("   AND T32.SCORE_DIV = T2.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   AND T32.CLASSCD = T2.CLASSCD ");
                stb.append("   AND T32.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND T32.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("   AND T32.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  LEFT JOIN SUBCLASS_MST T6 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ON T6.CLASSCD = T2.CLASSCD ");
                stb.append("   AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("   AND T6.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  LEFT JOIN CHAIR_DAT T5 ");
            stb.append("    ON T5.YEAR = T2.YEAR ");
            stb.append("   AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T5.CHAIRCD = T2.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("  AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + param._testcd + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("  AND T1.GRADE || T1.HR_CLASS IN " + param._selectDataIn + " ");
            } else {
                stb.append("  AND T1.SCHREGNO IN " + param._selectDataIn + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("  T1.GRADE, ");
            stb.append("  T1.HR_CLASS, ");
            stb.append("  T1.ATTENDNO, ");
            stb.append("  T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T2.CLASSCD, ");
                stb.append("  T2.CURRICULUM_CD, ");
            }
            stb.append("  T2.SUBCLASSCD, ");
            stb.append("  SUBSTR(T_GRP.CHAIR_GROUP_CD, LENGTH(T_GRP.CHAIR_GROUP_CD) - 2, 3) DESC ");

            return stb.toString();
        }

        private static void setTotalRankAverage(final DB2UDB db2, final Param param, final Map studentMap) throws SQLException {

        	if ("H".equals(param._schoolKind)) {
        		// 偏差値平均順位を算出
            	for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
            		final Student student = (Student) it.next();
            		
            		// 母集団年組 (上段用)
            		final String cacheMapKey1 = "HR_CLASS:" + student._hrClass;
            		if (null == param._calculationCacheMap.get(cacheMapKey1)) {
            			final String sql = sqlCalculationDeviationAvg(param, 1, student._hrClass, null);
            			param._calculationCacheMap.put(cacheMapKey1, KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, sql), "SCHREGNO"));
            			//log.info(" cache " + cacheMapKey1 + " = " + sql);
            			//log.info(" cache " + cacheMapKey1 + " = " + param._calculationCacheMap.get(cacheMapKey1));
            		}
            		final Map rank1Map = getMappedMap(getMappedMap(param._calculationCacheMap, cacheMapKey1), student._schregno);
            		student._testAll._rank1 = KnjDbUtils.getString(rank1Map, "DEVIATION_AVG_RANK"); // 偏差値平均順位
            		student._testAll._count1 = KnjDbUtils.getString(rank1Map, "DEVIATION_AVG_COUNT"); // 偏差値平均母集団数
            		student._testAll._avg1 = KnjDbUtils.getString(rank1Map, "SCORE_SUM_AVG"); // 得点平均点
            		student._testAll._highScore1 = KnjDbUtils.getString(rank1Map, "SCORE_SUM_HIGHSCORE"); // 得点最高点
            		student._testAll._lowScore1 = KnjDbUtils.getString(rank1Map, "SCORE_SUM_LOWSCORE"); // 得点最低点
            		student._testAll._deviation1 = KnjDbUtils.getString(rank1Map, "DEVIATION_AVG"); // 偏差値平均

            		// 母集団コースグループ。コースグループが未設定なら学年 (下段用)
            		final String cacheMapKey2;
            		if (null == student._courseGroupCd) {
            			cacheMapKey2 = "GRADE:" + student._grade;
            		} else {
            			cacheMapKey2 = "COURSE_GROUP_CD:" + student._courseGroupCd;
            		}
            		if (null == param._calculationCacheMap.get(cacheMapKey2)) {
            			final String sql = sqlCalculationDeviationAvg(param, 2, null, student._courseGroupCd);
            			param._calculationCacheMap.put(cacheMapKey2, KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, sql), "SCHREGNO"));
            			//log.info(" cache " + cacheMapKey2 + " = " + sql);
            			//log.info(" cache " + cacheMapKey2 + " = " + param._calculationCacheMap.get(cacheMapKey2));
            		}
            		final Map rank2Map = getMappedMap(getMappedMap(param._calculationCacheMap, cacheMapKey2), student._schregno);
            		student._testAll._rank2 = KnjDbUtils.getString(rank2Map, "DEVIATION_AVG_RANK"); // 偏差値平均順位
            		student._testAll._count2 = KnjDbUtils.getString(rank2Map, "DEVIATION_AVG_COUNT"); // 偏差値平均母集団数
            		student._testAll._avg2 = KnjDbUtils.getString(rank2Map, "SCORE_SUM_AVG"); // 得点平均点
            		student._testAll._highScore2 = KnjDbUtils.getString(rank2Map, "SCORE_SUM_HIGHSCORE"); // 得点最高点
            		student._testAll._lowScore2 = KnjDbUtils.getString(rank2Map, "SCORE_SUM_LOWSCORE"); // 得点最低点
            		student._testAll._deviation2 = KnjDbUtils.getString(rank2Map, "DEVIATION_AVG"); // 偏差値平均
            	}
        	} else {
        		final String sql = sqlTotalRankAverage(param);
        		log.debug("average sql = " + sql);
        		PreparedStatement ps = null;
        		ResultSet rs = null;
        		
        		try {
        			ps = db2.prepareStatement(sql);
        			rs = ps.executeQuery();
        			while (rs.next()) {
        				if (studentMap.containsKey(rs.getString("SCHREGNO"))) {
        					Student stuwk = (Student)studentMap.get(rs.getString("SCHREGNO"));
        					stuwk._testAll.setScore(rs.getString("SUBCLASSCD"), "", "", "", "",
        							rs.getString("SCORE"), rs.getString("RANK1"), rs.getString("COUNT1"),
        							rs.getString("AVG1"), rs.getString("HIGHSCORE1"), rs.getString("LOWSCORE1"), rs.getString("DEVIATION1"),
        							null,
        							rs.getString("RANK2"), rs.getString("COUNT2"), rs.getString("AVG2"),
        							rs.getString("HIGHSCORE2"), rs.getString("LOWSCORE2"), rs.getString("DEVIATION2"));
        				}
        			}
        		} catch (Exception e) {
        			log.fatal("exception!", e);
        		} finally {
        			DbUtils.closeQuietly(null, ps, rs);
        			db2.commit();
        		}
        	}
        }

        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private static String sqlTotalRankAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  (T2.CLASSCD || '-' ||  T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD) AS SUBCLASSCD, ");
            } else {
                stb.append("  T2.SUBCLASSCD, ");
            }
            stb.append("  T2.SCORE, ");
        	stb.append("  T2.CLASS_RANK AS RANK1, ");
        	stb.append("  TAVG_HR.COUNT AS COUNT1, ");
        	stb.append("  TAVG_HR.AVG AS AVG1, ");
        	stb.append("  TAVG_HR.HIGHSCORE AS HIGHSCORE1, ");
        	stb.append("  TAVG_HR.LOWSCORE AS LOWSCORE1, ");
        	stb.append("  T2.CLASS_DEVIATION AS DEVIATION1, ");
        	stb.append("  T2.GRADE_RANK AS RANK2, ");
        	stb.append("  TAVG_GRADE.COUNT AS COUNT2, ");
        	stb.append("  TAVG_GRADE.AVG AS AVG2, ");
        	stb.append("  TAVG_GRADE.HIGHSCORE AS HIGHSCORE2, ");
        	stb.append("  TAVG_GRADE.LOWSCORE AS LOWSCORE2, ");
        	stb.append("  T2.GRADE_DEVIATION AS DEVIATION2");
            stb.append(" FROM ");
            stb.append("  SCHREG_REGD_DAT T1 ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("   AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   AND T2.SUBCLASSCD = '" + SUBCLASS9 + "' ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT TAVG_HR ");
            stb.append("    ON TAVG_HR.YEAR = T1.YEAR ");
            stb.append("   AND TAVG_HR.SEMESTER = T2.SEMESTER ");
            stb.append("   AND TAVG_HR.GRADE = T1.GRADE ");
            stb.append("   AND TAVG_HR.HR_CLASS = T1.HR_CLASS ");
            stb.append("   AND TAVG_HR.COURSECD = '0' ");
            stb.append("   AND TAVG_HR.MAJORCD = '000' ");
            stb.append("   AND TAVG_HR.COURSECODE = '0000' ");
            stb.append("   AND TAVG_HR.AVG_DIV = '2' ");  //2:HR
            stb.append("   AND TAVG_HR.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("   AND TAVG_HR.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("   AND TAVG_HR.SCORE_DIV = T2.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   AND TAVG_HR.CLASSCD = T2.CLASSCD ");
                stb.append("   AND TAVG_HR.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND TAVG_HR.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("   AND TAVG_HR.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT TAVG_GRADE ");
            stb.append("    ON TAVG_GRADE.YEAR = T1.YEAR ");
            stb.append("   AND TAVG_GRADE.SEMESTER = T2.SEMESTER ");
            stb.append("   AND TAVG_GRADE.GRADE = T1.GRADE ");
            stb.append("   AND TAVG_GRADE.HR_CLASS = '000' ");
            stb.append("   AND TAVG_GRADE.COURSECD = '0' ");
            stb.append("   AND TAVG_GRADE.MAJORCD = '000' ");
            stb.append("   AND TAVG_GRADE.AVG_DIV = '1' ");  //1:学年
            stb.append("   AND TAVG_GRADE.COURSECODE = '0000' ");
            stb.append("   AND TAVG_GRADE.TESTKINDCD = T2.TESTKINDCD ");
            stb.append("   AND TAVG_GRADE.TESTITEMCD = T2.TESTITEMCD ");
            stb.append("   AND TAVG_GRADE.SCORE_DIV = T2.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   AND TAVG_GRADE.CLASSCD = T2.CLASSCD ");
                stb.append("   AND TAVG_GRADE.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("   AND TAVG_GRADE.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("   AND TAVG_GRADE.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER = '"+param._schregSemester+"' ");
            stb.append("  AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+param._testcd+"' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("  AND T1.GRADE || T1.HR_CLASS IN " + param._selectDataIn + " ");
            } else {
                stb.append("  AND T1.SCHREGNO IN " + param._selectDataIn + " ");
            }
            stb.append("  AND T2.SUBCLASSCD IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("  T1.GRADE, ");
            stb.append("  T1.HR_CLASS, ");
            stb.append("  T1.ATTENDNO, ");
            stb.append("  T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  T2.CLASSCD, ");
                stb.append("  T2.CURRICULUM_CD, ");
            }
            stb.append("  T2.SUBCLASSCD ");

            return stb.toString();
        }
        
        private static Map getMappedMap(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new TreeMap());
            }
            return (Map) map.get(key1);
        }

        private static String sqlCalculationDeviationAvg(final Param param, final int flg, final String hrClass, final String courseGroupCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH CHAIR_GROUP AS ( ");
            stb.append("  SELECT ");
            stb.append("      T1.CHAIRCD ");
            stb.append("      , MIN(T1.CHAIR_GROUP_CD) AS CHAIR_GROUP_CD "); // KNJD210Vに合わせる
            stb.append("  FROM CHAIR_GROUP_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("      T1.YEAR = '" + param._year + "' ");
            stb.append("      AND T1.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("      AND T1.TESTKINDCD = '00' "); // プロパティーuseKoteiTestCd=1
            stb.append("      AND T1.TESTITEMCD = '00' ");
            stb.append("  GROUP BY ");
            stb.append("      T1.CHAIRCD ");
            stb.append(" ) ");

            stb.append(" , T_SCORE_DEVIATIONS AS ( "); // 科目毎の得点と講座グループ偏差値or学年偏差値
            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T2.SCORE AS SCORE_SUM, ");
            stb.append("  T_RANK.CLASSCD || '-' ||  T_RANK.SCHOOL_KIND || '-' || T_RANK.CURRICULUM_CD || '-' || T_RANK.SUBCLASSCD AS SUBCLASSCD, ");
            if (flg == 1) {
                stb.append("  DECIMAL(ROUND(T_CHAIRRANK.GRADE_DEVIATION, 1), 5, 1) AS GROUP_DEVIATION ");
            } else if (flg == 2) {
                stb.append("  DECIMAL(ROUND(CASE WHEN TCG.CHAIR_GROUP_CD IS NOT NULL THEN T_RANK.CHAIR_GROUP_DEVIATION ELSE T_CHAIRRANK.GRADE_DEVIATION END, 1), 5, 1) AS GROUP_DEVIATION ");
            }
            stb.append(" FROM ");
            stb.append("  SCHREG_REGD_DAT T1 ");
            stb.append("  INNER JOIN RECORD_RANK_SDIV_DAT T2 ");
            stb.append("    ON T2.YEAR = T1.YEAR ");
            stb.append("   AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   AND T2.SUBCLASSCD = '" + SUBCLASS9 + "' ");
            stb.append("  LEFT JOIN COURSE_GROUP_CD_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("   AND T5.GRADE = T1.GRADE ");
            stb.append("   AND T5.COURSECD = T1.COURSECD ");
            stb.append("   AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("   AND T5.COURSECODE = T1.COURSECODE ");
        	stb.append("  INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ");
        	stb.append("    ON T_RANK.YEAR = T1.YEAR ");
        	stb.append("   AND T_RANK.SEMESTER = T2.SEMESTER ");
        	stb.append("   AND T_RANK.TESTKINDCD = T2.TESTKINDCD ");
        	stb.append("   AND T_RANK.TESTITEMCD = T2.TESTITEMCD ");
        	stb.append("   AND T_RANK.SCORE_DIV = T2.SCORE_DIV ");
        	stb.append("   AND T_RANK.SCHREGNO = T1.SCHREGNO ");
        	stb.append("  LEFT JOIN RECORD_RANK_CHAIR_SDIV_DAT T_CHAIRRANK ");
        	stb.append("    ON T_CHAIRRANK.YEAR = T1.YEAR ");
        	stb.append("   AND T_CHAIRRANK.SEMESTER = T2.SEMESTER ");
        	stb.append("   AND T_CHAIRRANK.TESTKINDCD = T2.TESTKINDCD ");
        	stb.append("   AND T_CHAIRRANK.TESTITEMCD = T2.TESTITEMCD ");
        	stb.append("   AND T_CHAIRRANK.SCORE_DIV = T2.SCORE_DIV ");
        	stb.append("   AND T_CHAIRRANK.CLASSCD = T_RANK.CLASSCD ");
        	stb.append("   AND T_CHAIRRANK.SCHOOL_KIND = T_RANK.SCHOOL_KIND ");
        	stb.append("   AND T_CHAIRRANK.CURRICULUM_CD = T_RANK.CURRICULUM_CD ");
        	stb.append("   AND T_CHAIRRANK.SUBCLASSCD = T_RANK.SUBCLASSCD ");
        	stb.append("   AND T_CHAIRRANK.SCHREGNO = T1.SCHREGNO ");
        	stb.append("  LEFT JOIN CHAIR_GROUP TCG ON TCG.CHAIRCD = T_CHAIRRANK.CHAIRCD ");
            stb.append("  INNER JOIN SUBCLASS_MST SBM ON SBM.CLASSCD = T_RANK.CLASSCD ");
            stb.append("   AND SBM.SCHOOL_KIND = T_RANK.SCHOOL_KIND ");
            stb.append("   AND SBM.CURRICULUM_CD = T_RANK.CURRICULUM_CD ");
            stb.append("   AND SBM.SUBCLASSCD = T_RANK.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("  AND T1.GRADE = '" + param._grade + "' ");
            if (flg == 1) {
                stb.append("  AND T1.HR_CLASS = '" + hrClass + "' ");
            } else if (flg == 2) {
            	// コースグループがない場合、母集団は学年とする
            	if (null != courseGroupCd) {
            		stb.append("  AND T5.GROUP_CD = '" + courseGroupCd + "' ");
            	}
            }
            stb.append("  AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + param._testcd + "' ");
            stb.append(" ) ");
            stb.append(" , T_SCHREG_DEVIATION_AVG AS ( "); // 偏差値の平均
            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  MAX(T1.SCORE_SUM) AS SCORE_SUM, ");
            stb.append("  DECIMAL(ROUND(AVG(GROUP_DEVIATION), 1), 5, 1) AS DEVIATION_AVG ");
            stb.append(" FROM ");
            stb.append("  T_SCORE_DEVIATIONS T1 ");
            stb.append(" GROUP BY ");
            stb.append("  T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" , T_SCHREG_DEVIATION_AVG_STAT AS ( "); // 偏差値の平均の母集団平均
            stb.append(" SELECT ");
            stb.append("  AVG(DEVIATION_AVG) AS DEVIATION_AVG_AVG, ");
            stb.append("  COUNT(DEVIATION_AVG) AS DEVIATION_AVG_COUNT, ");
            stb.append("  DECIMAL(ROUND(AVG(1.0 * SCORE_SUM), 1), 5, 1) AS SCORE_SUM_AVG, ");
            stb.append("  MAX(SCORE_SUM) AS SCORE_SUM_HIGHSCORE, ");
            stb.append("  MIN(SCORE_SUM) AS SCORE_SUM_LOWSCORE ");
            stb.append(" FROM ");
            stb.append("  T_SCHREG_DEVIATION_AVG T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.SCORE_SUM, ");
            stb.append("  RANK() OVER(ORDER BY T1.DEVIATION_AVG DESC) AS DEVIATION_AVG_RANK, "); // 順位
            stb.append("  STAT.DEVIATION_AVG_COUNT, "); // 順位母集団数
            stb.append("  STAT.SCORE_SUM_AVG, "); // 平均
            stb.append("  STAT.SCORE_SUM_HIGHSCORE, "); // 最高点
            stb.append("  STAT.SCORE_SUM_LOWSCORE, "); // 最低点
            stb.append("  T1.DEVIATION_AVG "); // 偏差値
            stb.append(" FROM ");
            stb.append("  T_SCHREG_DEVIATION_AVG T1, T_SCHREG_DEVIATION_AVG_STAT STAT ");

            return stb.toString();
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
        final String _gradeCd;
        final String[] _selectData;
        final String _selectDataIn;
        final String _z010;
        final String _ketten;
        final String _checkKettenDiv; //欠点プロパティ 1,2,設定無し(1,2以外)
        final String _schoolKind;

        final String _schregSemester;
        private String _semesterName;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        /** 欠点を使用しない */
        final boolean _notUseKetten;
        /** 裁断用にソートして出力する */
        final boolean _sortCutting;

        private KNJSchoolMst _knjSchoolMst;

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        final String _nendo;
        final Map _calculationCacheMap = new HashMap();
        
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
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _selectDataIn = getCategorySelectedIn(_categoryIsClass, _grade, _selectData);

            _z010 = setNameMst(db2, "Z010", "00");

            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _notUseKetten = "1".equals(request.getParameter("notUseKetten"));

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _sortCutting = false; //"1".equals(request.getParameter("SORT_CUTTING"));

            _schoolKind = getSchregRegdGdat(db2, "SCHOOL_KIND");
            try {
            	final Map paramMap = new HashMap();
            	paramMap.put("SCHOOL_KIND", _schoolKind);
                _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }

            _definecode = createDefineCode(db2);

            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year));
            _gradeCd = getSchregRegdGdat(db2, "GRADE_CD");
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
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String getCategorySelectedIn(final String categoryIsClass, final String grade, final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                if ("1".equals(categoryIsClass)) {
                    stb.append("'" + grade + categorySelected[i] + "'");
                } else {
                    stb.append("'" + categorySelected[i] + "'");
                }
            }
            stb.append(")");
            return stb.toString();
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT " + field + " FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                log.debug(" gdat sql = "+ sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException ex) {
                log.error("SCHREG_REGD_GDAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof
