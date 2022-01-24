/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 55710ef686cf07ad2108fd45270a187a888eb967 $
 *
 * 作成日: 2018/07/19
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD105J {

    private static final Log log = LogFactory.getLog(KNJD105J.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final int MAXCOL = 10;  //1行に出力する
    private static final int SCORE_RANGE = 10; //ゲージ1つに対して点数いくつになるか、で設定
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJD105J.frm", 1);
        boolean warnoutflg = false;
        final List studentList = createStudents(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            svf.VrsOut("TITLE", _param._year + "年度　個人成績票(" + _param._testName + ")");
            final Student student = (Student) iterator.next();
            final String spStr = student._attendNo.length() > 2 ? "" : " ";
            svf.VrsOut("HR_NAME", student._gradeName + student._hrClassName + "組" + spStr + (NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : StringUtils.defaultString(student._attendNo)) + "番");
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);
            final int colLastTotal5Cnt = 9;
            final int rowLastTotal5Cnt = 2;
    	    final int colLastTotalCnt = 10;
    	    final int rowLastTotalCnt = 2;
            if ("H".equals(student._schoolKind)) {
            	svf.VrsOut("COURSE_RANK_NAME", "コース順位");
            } else {
                svf.VrsOutn("SUBCLASS_NAME2_" + rowLastTotal5Cnt, colLastTotal5Cnt, "5教科合計"); //科目
                svf.VrsOut("SLASH_DEVI1", _param._SlashImagePath);
                svf.VrsOut("SLASH_RANK1", _param._SlashImagePath);
                svf.VrsOut("SLASH_POINT1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR1_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR2_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR3_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR4_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR5_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR6_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR7_1", _param._SlashImagePath);
                svf.VrsOut("SLASH_BAR8_1", _param._SlashImagePath);
            }
            svf.VrsOutn("SUBCLASS_NAME2_" + rowLastTotalCnt, colLastTotalCnt, "総合計"); //科目
            svf.VrsOut("SLASH_DEVI2", _param._SlashImagePath);
            svf.VrsOut("SLASH_RANK2", _param._SlashImagePath);
            svf.VrsOut("SLASH_POINT2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR1_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR2_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR3_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR4_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR5_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR6_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR7_2", _param._SlashImagePath);
            svf.VrsOut("SLASH_BAR8_2", _param._SlashImagePath);
            if ("H".equals(student._schoolKind)) {
                svf.VrsOut("SLASH_AVERAGE2", _param._SlashImagePath);
            }
            int mstoutCnt = 0;
            for (Iterator itTest = _param._testItemMstMap.keySet().iterator(); itTest.hasNext();) {
                final String testKey = (String) itTest.next();
                final TestItemMst testItemMst = (TestItemMst) _param._testItemMstMap.get(testKey);
                if (mstoutCnt >= 5) {
                	log.warn("学期成績の最大名称出力件数を超えています。");
                	continue;
                }
                mstoutCnt++;
                svf.VrsOutn("TEST_NAME", mstoutCnt, testItemMst._semesterName + testItemMst._testitemName);
            }

            int datacnt = 0;
            int rowCnt = 1;
            int colCnt = 1;
            int abilityCnt = 6;
            boolean totalPrintNgFlg = false;
            for (Iterator itSubclass = student._subclassRankList.iterator(); itSubclass.hasNext();) {
                final SubclassRank subclassRank = (SubclassRank) itSubclass.next();
                final String key = subclassRank.getKey();
                if (datacnt > ("H".equals(student._schoolKind) ? 18 : 17)) {
                	log.warn("科目の最大出力件数を超えています。");
                	continue;
                }
                if (_param._subclassBunpuMap.containsKey(key)) {
                    if (colCnt > (rowCnt == 1 ? 10 : ("H".equals(student._schoolKind) ? 9 : 8))) {
                        colCnt = 1;
                        rowCnt++;
                    }
                    final SubclassBunpu bunpu = (SubclassBunpu) _param._subclassBunpuMap.get(key);
                    final int scnlen = KNJ_EditEdit.getMS932ByteLength(bunpu._subclassOrderName3);
                    final String scnfield = scnlen > 12 ? "_3" : (scnlen > 8 ? "_2" : "_1");
                    svf.VrsOutn("SUBCLASS_NAME" + rowCnt + scnfield, colCnt, bunpu._subclassOrderName3); //科目
                    //本試験/追試験両方入るパターンがあるようなので、「追試欠課を出力するなら、追試験」
                    boolean outscorechkflg = "1".equals(_param._printTuishi) && !"".equals(StringUtils.defaultString(subclassRank._addExScore, ""));
                    final String outScore = !outscorechkflg ? String.valueOf(subclassRank._score) : "*" + StringUtils.defaultString(subclassRank._addExScore, "");
                    if (outscorechkflg) totalPrintNgFlg = true;
                    svf.VrsOutn("SCORE" + rowCnt, colCnt, outScore);  //得点
                    if (!_param._d017Name1List.contains(subclassRank._classcd + "-" + subclassRank._schoolKind + "-" + subclassRank._curriculumCd + "-" + subclassRank._subclasscd)) {
                    	if (!outscorechkflg) {
                    	    final String gAvg = null != subclassRank._gAvg && !"".equals(subclassRank._gAvg) ? subclassRank._gAvg : "0";
                    	    final BigDecimal setVal = new BigDecimal(gAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
                    	    svf.VrsOutn("DEVI" + rowCnt, colCnt, subclassRank._gradeDeviation);  //偏差値
                    	    svf.VrsOutn("RANK" + rowCnt, colCnt, subclassRank._gradeRank);       //順位/総数
                    	    if (rowCnt > 1) svf.VrsOutn("RANK_SLASH", colCnt, "/");
                    	    svf.VrsOutn("EXAM_NUM" + rowCnt, colCnt, subclassRank._gCnt);        //順位/総数
                    	    svf.VrsOutn("MAX_POINT" + rowCnt, colCnt, subclassRank._highScore);  //最高/最低
                    	    if (rowCnt > 1) svf.VrsOutn("MAX_POINT_SLASH", colCnt, "/");
                    	    svf.VrsOutn("MIN_POINT" + rowCnt, colCnt, subclassRank._lowScore);   //最高/最低
                    	    svf.VrsOutn("AVERAGE" + rowCnt, colCnt, setVal.toString());          //平均点
                            //NULLなら-1指定でグラフ範囲外にする。
                    	    final int subclsScore = Integer.parseInt(("".equals(StringUtils.defaultString(subclassRank._score)) ? "-1" :  subclassRank._score));
                    	    int rangeMax = Math.max(bunpu._score9, Math.max(bunpu._score8, Math.max(bunpu._score7, Math.max(bunpu._score6, Math.max(bunpu._score5, Math.max(bunpu._score4, Math.max(bunpu._score3, bunpu._score0)))))));
                    	    printRange(svf, bunpu._score9, 1, colCnt, rowCnt, 90, 100, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score8, 2, colCnt, rowCnt, 80, 89, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score7, 3, colCnt, rowCnt, 70, 79, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score6, 4, colCnt, rowCnt, 60, 69, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score5, 5, colCnt, rowCnt, 50, 59, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score4, 6, colCnt, rowCnt, 40, 49, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score3, 7, colCnt, rowCnt, 30, 39, subclsScore, rangeMax);
                    	    printRange(svf, bunpu._score0, 8, colCnt, rowCnt, 0, 29, subclsScore, rangeMax);
                    	}
                    }
                    colCnt++;
                    datacnt++;
                }
                if (ALL5.equals(subclassRank._subclasscd)) {
                    //上の表に出力するので、指示画面で選択したものと一致するのかチェックが必要
                    //中学のみ出力する列
                    if (subclassRank._testcd.equals(_param._semester + _param._testcd) && "J".equals(student._schoolKind) && !totalPrintNgFlg) {
                        if (!"".equals(subclassRank._score)) {
                            svf.VrsOutn("SCORE" + rowLastTotal5Cnt, colLastTotal5Cnt, String.valueOf(subclassRank._score));  //得点
                        }
                        if (!"".equals(StringUtils.defaultString(subclassRank._gAvg))) {
                            final BigDecimal avgBD = new BigDecimal(subclassRank._gAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOutn("AVERAGE" + rowLastTotal5Cnt, colLastTotal5Cnt, avgBD.toString()); //平均点
                        }
                	}
                }
                if (ALL9.equals(subclassRank._subclasscd)) {
                	//上の表に出力するので、指示画面で選択したものと一致するのかチェックが必要
                	if (subclassRank._testcd.equals(_param._semester + _param._testcd) && !totalPrintNgFlg) {
                        if (!"".equals(subclassRank._score)) {
                            svf.VrsOutn("SCORE" + rowLastTotalCnt, colLastTotalCnt, String.valueOf(subclassRank._score));  //得点
                        }
                        if (!"".equals(StringUtils.defaultString(subclassRank._gAvg)) && "J".equals(student._schoolKind)) {
                            final BigDecimal avgBD = new BigDecimal(subclassRank._gAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
                	        svf.VrsOutn("AVERAGE" + rowLastTotalCnt, colLastTotalCnt, avgBD.toString()); //平均点
                        }
                	}

                    int all9Cnt = 1;
                    for (Iterator itTest = _param._testItemMstMap.keySet().iterator(); itTest.hasNext();) {
                        final String testKey = (String) itTest.next();
                        if (all9Cnt > 5) {
                        	continue;
                        }
                        final TestItemMst testItemMst = (TestItemMst) _param._testItemMstMap.get(testKey);
                        if (subclassRank != null && subclassRank._testcd.equals(testItemMst.getKey()) && subclassRank._avg != null) {
                        	//表示出力圏外のコード、または追試出力なので、skip
                            if (subclassRank._testcd.equals(_param._semester + _param._testcd) && totalPrintNgFlg || "1".equals(_param._printTuishi) && student._kesshiTestcdList.contains(subclassRank._testcd)) {
                            	continue;
                            }
                            if (student._subclassAvgMap.containsKey(testKey)) {
                                final AllAvg allAvg = (AllAvg) student._subclassAvgMap.get(testKey);
                                svf.VrsOutn("SUBCLASS_DEVI_AVE", all9Cnt, allAvg._deviationAvg);
                            }
                            if (!"".equals(StringUtils.defaultString(subclassRank._avg))) {
                                final BigDecimal setVal = new BigDecimal(subclassRank._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                                svf.VrsOutn("SUBCLASS_SCORE_AVE", all9Cnt, setVal.toString());
                            }
                            svf.VrsOutn("GRADE_RANK", all9Cnt, subclassRank._gradeAvgRank);
                            if ("H".equals(student._schoolKind)) {
                                svf.VrsOutn("COURSE_RANK", all9Cnt, subclassRank._courseAvgRank);
                            }
                        }
                        all9Cnt++;
                    }
                }
            }
            if (student._proficiencyList.size() > 0) {
            	//Listに降順で入れているので、「最新の4つを取得」して古い方から出力するので、
            	//4番目から前のデータを出力する。
            	int strtidx = student._proficiencyList.size() > 4 ? 4 : student._proficiencyList.size();
            	for (int ii = strtidx;ii > 0;ii--) {
            		ProficiencyData getwk = (ProficiencyData)student._proficiencyList.get(ii-1);
            		if (getwk != null && getwk._avg != null) {
                        svf.VrsOutn("TEST_NAME", abilityCnt, getwk._proficiencyname1);
                        if (!"".equals(StringUtils.defaultString(getwk._avgDevi, ""))) {
                            final BigDecimal setVal1 = new BigDecimal(getwk._avgDevi).setScale(1, BigDecimal.ROUND_HALF_UP);
                            svf.VrsOutn("SUBCLASS_DEVI_AVE", abilityCnt, setVal1.toString());
                        }
                        final BigDecimal setVal2 = new BigDecimal(getwk._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
                        svf.VrsOutn("SUBCLASS_SCORE_AVE", abilityCnt, setVal2.toString());
                        svf.VrsOutn("GRADE_RANK", abilityCnt, getwk._grade_Rank);
                        if ("H".equals(student._schoolKind)) {
                            svf.VrsOutn("COURSE_RANK", abilityCnt, getwk._course_Rank);
                        }
                        abilityCnt++;
            		}
            	}
            	if (!warnoutflg && student._proficiencyList.size() > 4) {
                	log.warn("実力テストの最大出力件数を超えています。");
                	warnoutflg = true;
            	}
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printRange(final Vrw32alp svf, final int score, final int setField, final int colCnt, final int rowCnt, final int minScore, final int maxScore, final int stdScore, final int rangeMax) {
        final int rangeCnt = (int) (((double)score * (double)SCORE_RANGE) / (double)rangeMax) ;
        final int amariCnt = rangeCnt == SCORE_RANGE ? 0 : ((score * SCORE_RANGE) % rangeMax > 0 ? 1 : 0);
        final int totalCnt = rangeCnt + amariCnt;
        String setSp = score == 0 ? "  " : score < 100 ? " " : "";
        boolean thisRange = minScore <= stdScore && stdScore <= maxScore;
        if (thisRange) {
            svf.VrsOutn("STAR" + rowCnt + "_" + setField, colCnt, "★");
        }
        for (int squareCnt = 0; squareCnt < totalCnt; squareCnt++) {
            if (thisRange) {
                setSp = setSp + " ";
                svf.VrAttributen("BAR" + rowCnt + "_" + setField + "_" + (squareCnt + 1), colCnt, "Paint=(1,1,1),Bold=1");
            } else {
                setSp = setSp + " ";
                svf.VrAttributen("BAR" + rowCnt + "_" + setField + "_" + (squareCnt + 1), colCnt, "Paint=(1,60,1),Bold=1");
            }
        }
        svf.VrsOutn("BAR_NUM" + rowCnt + "_" + setField, colCnt, setSp + score);
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List retList = new LinkedList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.GRADE_NAME1, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_CLASS_NAME1, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("          AND GDAT.GRADE = REGD.GRADE ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO  ");

            //log.debug(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String hrclass = rs.getString("hr_class");
                final String hrClassName = rs.getString("HR_CLASS_NAME1");
                final String attendno = rs.getString("attendno");
                final String name = rs.getString("name");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODENAME");

                final Student student = new Student(
                		schoolKind,
                        schregno,
                        grade,
                        gradeName,
                        hrclass,
                        hrClassName,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName,
                        courseCodeName
                );
                student.setSubclassRank(db2);
                student.setProficiency(db2);
                retList.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private class Student {
        final String _schregno;
        final String _schoolKind;
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String _hrClassName;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final List _subclassRankList;
        final List _proficiencyList;
        final Map _subclassAvgMap;
        final List _kesshiTestcdList;

        public Student(
        		final String schoolKind,
                final String schregno,
                final String grade,
                final String gradeName,
                final String hrClass,
                final String hrClassName,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName
        ) {
        	_schoolKind = schoolKind;
            _schregno = schregno;
            _grade = grade;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrClassName = hrClassName;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _subclassRankList = new ArrayList();
            _subclassAvgMap = new TreeMap();
            _proficiencyList = new ArrayList();
            _kesshiTestcdList = new ArrayList();
        }

        public void setSubclassRank(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MERGE_SDIV_KEY AS ( ");
            stb.append("  SELECT ");
            stb.append("    T1.YEAR, ");
            stb.append("    T1.SEMESTER, ");
            stb.append("    T1.TESTKINDCD, ");
            stb.append("    T1.TESTITEMCD, ");
            stb.append("    T1.SCORE_DIV, ");
            stb.append("    T1.CLASSCD, ");
            stb.append("    T1.SCHOOL_KIND, ");
            stb.append("    T1.CURRICULUM_CD, ");
            stb.append("    T1.SUBCLASSCD, ");
            stb.append("    T1.SCHREGNO ");
            stb.append("  FROM ");
            stb.append("    RECORD_RANK_SDIV_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' ");
            stb.append("    AND ");
            if (!"1".equals(_param._semester)) {
            	stb.append(" (");
                stb.append("    T1.SEMESTER < '" + _param._semester + "' OR ");
            	stb.append("   ( ");
            }
            stb.append("    T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <= '" + _param._testcd + "' ");
            if (!"1".equals(_param._semester)) {
            	stb.append("   ) ");
            	stb.append(" ) ");
            }
            stb.append("    AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("    AND T1.SUBCLASSCD NOT IN ('99999A', '99999B') ");
            stb.append("  UNION ");
            stb.append("  SELECT ");
            stb.append("    T2.YEAR, ");
            stb.append("    T2.SEMESTER, ");
            stb.append("    T2.TESTKINDCD, ");
            stb.append("    T2.TESTITEMCD, ");
            stb.append("    T2.SCORE_DIV, ");
            stb.append("    T2.CLASSCD, ");
            stb.append("    T2.SCHOOL_KIND, ");
            stb.append("    T2.CURRICULUM_CD, ");
            stb.append("    T2.SUBCLASSCD, ");
            stb.append("    T2.SCHREGNO ");
            stb.append("  FROM ");
            stb.append("    RECORD_SCORE_DAT T2 ");
            stb.append("  WHERE ");
            stb.append("    T2.VALUE_DI = '*'");
            stb.append("    AND T2.YEAR = '" + _param._year + "' ");
            stb.append("    AND ");
            if (!"1".equals(_param._semester)) {
            	stb.append(" (");
                stb.append("    T2.SEMESTER < '" + _param._semester + "' OR ");
            	stb.append("   ( ");
            }
            stb.append("    T2.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV <= '" + _param._testcd + "' ");
            if (!"1".equals(_param._semester)) {
            	stb.append("   ) ");
            	stb.append(" ) ");
            }
            stb.append("    AND T2.SCHREGNO = '" + _schregno + "' ");
            stb.append("    AND T2.SUBCLASSCD NOT IN ('99999A', '99999B') ");
            stb.append(" ), MERGE_DIST_KEY AS (");
            stb.append("   SELECT DISTINCT * FROM MERGE_SDIV_KEY ");
            stb.append(" ), MERGE_SDIV_DAT AS ( ");
            stb.append(" SELECT ");
            stb.append("   CASE WHEN T1.YEAR IS NULL THEN T2.YEAR ELSE T1.YEAR END AS YEAR, ");
            stb.append("   CASE WHEN T1.SEMESTER IS NULL THEN T2.SEMESTER ELSE T1.SEMESTER END AS SEMESTER, ");
            stb.append("   CASE WHEN T1.TESTKINDCD IS NULL THEN T2.TESTKINDCD ELSE T1.TESTKINDCD END AS TESTKINDCD, ");
            stb.append("   CASE WHEN T1.TESTITEMCD IS NULL THEN T2.TESTITEMCD ELSE T1.TESTITEMCD END AS TESTITEMCD, ");
            stb.append("   CASE WHEN T1.SCORE_DIV IS NULL THEN T2.SCORE_DIV ELSE T1.SCORE_DIV END AS SCORE_DIV, ");
            stb.append("   CASE WHEN T1.CLASSCD IS NULL THEN T2.CLASSCD ELSE T1.CLASSCD END AS CLASSCD, ");
            stb.append("   CASE WHEN T1.SCHOOL_KIND IS NULL THEN T2.SCHOOL_KIND ELSE T1.SCHOOL_KIND END AS SCHOOL_KIND, ");
            stb.append("   CASE WHEN T1.CURRICULUM_CD IS NULL THEN T2.CURRICULUM_CD ELSE T1.CURRICULUM_CD END AS CURRICULUM_CD, ");
            stb.append("   CASE WHEN T1.SUBCLASSCD IS NULL THEN T2.SUBCLASSCD ELSE T1.SUBCLASSCD END AS SUBCLASSCD, ");
            stb.append("   CASE WHEN T1.SCHREGNO IS NULL THEN T2.SCHREGNO ELSE T1.SCHREGNO END AS SCHREGNO, ");
            stb.append("   T1.SCORE, ");
            stb.append("   T1.AVG, ");
            stb.append("   T1.GRADE_RANK, ");
            stb.append("   T1.GRADE_AVG_RANK, ");
            stb.append("   T1.GRADE_DEVIATION, ");
            stb.append("   T1.GRADE_DEVIATION_RANK, ");
            stb.append("   T1.CLASS_RANK, ");
            stb.append("   T1.CLASS_AVG_RANK, ");
            stb.append("   T1.CLASS_DEVIATION, ");
            stb.append("   T1.CLASS_DEVIATION_RANK, ");
            stb.append("   T1.COURSE_RANK, ");
            stb.append("   T1.COURSE_AVG_RANK, ");
            stb.append("   T1.COURSE_DEVIATION, ");
            stb.append("   T1.COURSE_DEVIATION_RANK, ");
            stb.append("   T1.MAJOR_RANK, ");
            stb.append("   T1.MAJOR_AVG_RANK, ");
            stb.append("   T1.MAJOR_DEVIATION, ");
            stb.append("   T1.MAJOR_DEVIATION_RANK, ");
            stb.append("   T1.COURSE_GROUP_RANK, ");
            stb.append("   T1.COURSE_GROUP_AVG_RANK, ");
            stb.append("   T1.COURSE_GROUP_DEVIATION, ");
            stb.append("   T1.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("   T1.CHAIR_GROUP_RANK, ");
            stb.append("   T1.CHAIR_GROUP_AVG_RANK, ");
            stb.append("   T1.CHAIR_GROUP_DEVIATION, ");
            stb.append("   T1.CHAIR_GROUP_DEVIATION_RANK, ");
            stb.append("   T2.VALUE_DI ");
            stb.append("  FROM ");
            stb.append("    MERGE_SDIV_KEY T3 ");
            stb.append("    LEFT JOIN RECORD_RANK_SDIV_DAT T1 ");
            stb.append("      ON T1.YEAR = T3.YEAR ");
            stb.append("     AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("     AND T1.TESTKINDCD = T3.TESTKINDCD ");
            stb.append("     AND T1.TESTITEMCD = T3.TESTITEMCD ");
            stb.append("     AND T1.SCORE_DIV = T3.SCORE_DIV ");
            stb.append("     AND T1.CLASSCD = T3.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND T1.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND T1.SCHREGNO = T3.SCHREGNO ");
            stb.append("    LEFT JOIN RECORD_SCORE_DAT T2 ");
            stb.append("      ON T2.YEAR = T3.YEAR ");
            stb.append("     AND T2.SEMESTER = T3.SEMESTER ");
            stb.append("     AND T2.TESTKINDCD = T3.TESTKINDCD ");
            stb.append("     AND T2.TESTITEMCD = T3.TESTITEMCD ");
            stb.append("     AND T2.SCORE_DIV = T3.SCORE_DIV ");
            stb.append("     AND T2.CLASSCD = T3.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND T2.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND T2.SCHREGNO = T3.SCHREGNO ");
            stb.append(" ) ");
            //成績データ(5/9はUNIONで分けて取得)
            stb.append(" SELECT ");
            stb.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
            if ("1".equals(_param._useCurriculumCd)) {
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
            }
            stb.append("     REC_RANK.SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     REC_RANK.SCORE, ");
            stb.append("     REC_RANK.AVG, ");
            stb.append("     REC_AVG.COUNT AS GCNT, ");
            stb.append("     REC_AVG.AVG AS GAVG, ");
            stb.append("     REC_RANK.GRADE_RANK, ");
            stb.append("     REC_RANK.GRADE_AVG_RANK, ");
            stb.append("     REC_RANK.GRADE_DEVIATION, ");
            stb.append("     REC_RANK.GRADE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CLASS_RANK, ");
            stb.append("     REC_RANK.CLASS_AVG_RANK, ");
            stb.append("     REC_RANK.CLASS_DEVIATION, ");
            stb.append("     REC_RANK.CLASS_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_RANK, ");
            stb.append("     REC_RANK.COURSE_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.MAJOR_RANK, ");
            stb.append("     REC_RANK.MAJOR_AVG_RANK, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION_RANK, ");
            stb.append("     REC_AVG.HIGHSCORE, ");
            stb.append("     REC_AVG.LOWSCORE, ");
            stb.append("     REC_SLMP.SCORE AS ADD_EX_SCORE, ");
            stb.append("     REC_RANK.VALUE_DI ");
            stb.append(" FROM ");
            stb.append("     MERGE_SDIV_DAT REC_RANK ");
            stb.append("     INNER JOIN SUBCLASS_MST SUBM ");
            stb.append("        ON REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumCd)) {
                stb.append("       AND REC_RANK.CLASSCD = SUBM.CLASSCD ");
                stb.append("       AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                stb.append("       AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT REC_AVG ");
            stb.append("       ON REC_RANK.YEAR = REC_AVG.YEAR ");
            stb.append("      AND REC_RANK.SEMESTER = REC_AVG.SEMESTER ");
            stb.append("      AND REC_RANK.TESTKINDCD = REC_AVG.TESTKINDCD ");
            stb.append("      AND REC_RANK.TESTITEMCD = REC_AVG.TESTITEMCD ");
            stb.append("      AND REC_RANK.SCORE_DIV = REC_AVG.SCORE_DIV ");
            if ("1".equals(_param._useCurriculumCd)) {
                stb.append("      AND REC_RANK.CLASSCD = REC_AVG.CLASSCD ");
                stb.append("      AND REC_RANK.SCHOOL_KIND = REC_AVG.SCHOOL_KIND ");
                stb.append("      AND REC_RANK.CURRICULUM_CD = REC_AVG.CURRICULUM_CD ");
            }
            stb.append("      AND REC_RANK.SUBCLASSCD = REC_AVG.SUBCLASSCD ");
            stb.append("      AND REC_AVG.AVG_DIV = '1' ");
            stb.append("      AND REC_AVG.GRADE = '" + _param._grade + "' ");
            stb.append("     LEFT JOIN RECORD_SLUMP_SDIV_DAT REC_SLMP ");
            stb.append("       ON REC_SLMP.YEAR = REC_RANK.YEAR ");
            stb.append("      AND REC_SLMP.SEMESTER = REC_RANK.SEMESTER ");
            stb.append("      AND REC_SLMP.TESTKINDCD = REC_RANK.TESTKINDCD ");
            stb.append("      AND REC_SLMP.TESTITEMCD = REC_RANK.TESTITEMCD ");
            stb.append("      AND REC_SLMP.SCORE_DIV = REC_RANK.SCORE_DIV ");
            if ("1".equals(_param._useCurriculumCd)) {
                stb.append("      AND REC_SLMP.CLASSCD = REC_RANK.CLASSCD ");
                stb.append("      AND REC_SLMP.SCHOOL_KIND = REC_RANK.SCHOOL_KIND ");
                stb.append("      AND REC_SLMP.CURRICULUM_CD = REC_RANK.CURRICULUM_CD ");
            }
            stb.append("      AND REC_SLMP.SUBCLASSCD = REC_RANK.SUBCLASSCD ");
            stb.append("      AND REC_SLMP.SCHREGNO = REC_RANK.SCHREGNO ");

            stb.append(" WHERE ");
            stb.append("     REC_RANK.YEAR = '" + _param._year + "' ");
            stb.append("     AND REC_RANK.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("     AND REC_RANK.SCHREGNO = '" + _schregno + "' ");
            //成績データ(9)
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     MDIST.SEMESTER || MDIST.TESTKINDCD || MDIST.TESTITEMCD || MDIST.SCORE_DIV AS TESTCD, ");
            if ("1".equals(_param._useCurriculumCd)) {
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
            }
            stb.append("     CASE WHEN REC_RANK.SUBCLASSCD IS NULL THEN '" + ALL9 + "' ELSE REC_RANK.SUBCLASSCD END AS SUBCLASSCD, ");
            stb.append("     '全科目' AS SUBCLASSNAME, ");
            stb.append("     REC_RANK.SCORE, ");
            stb.append("     REC_RANK.AVG, ");
            stb.append("     0 AS GCNT, ");
            stb.append("     CASE WHEN RAD.AVG IS NULL THEN NULL ELSE DECIMAL(ROUND( ( RAD.AVG )* 10 , 0 ) / 10 , 5, 1 ) END AS GAVG, ");
            stb.append("     REC_RANK.GRADE_RANK, ");
            stb.append("     REC_RANK.GRADE_AVG_RANK, ");
            stb.append("     REC_RANK.GRADE_DEVIATION, ");
            stb.append("     REC_RANK.GRADE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CLASS_RANK, ");
            stb.append("     REC_RANK.CLASS_AVG_RANK, ");
            stb.append("     REC_RANK.CLASS_DEVIATION, ");
            stb.append("     REC_RANK.CLASS_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_RANK, ");
            stb.append("     REC_RANK.COURSE_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.MAJOR_RANK, ");
            stb.append("     REC_RANK.MAJOR_AVG_RANK, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION_RANK, ");
            stb.append("     CAST(NULL AS INTEGER) AS HIGHSCORE, ");
            stb.append("     CAST(NULL AS INTEGER) AS LOWSCORE, ");
            stb.append("     CAST(NULL AS INTEGER) AS ADD_EX_SCORE, ");
            stb.append("     '' AS VALUE_DI ");
            stb.append(" FROM ");
            stb.append("     MERGE_DIST_KEY MDIST ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT REC_RANK ");
            stb.append("       ON REC_RANK.YEAR = MDIST.YEAR ");
            stb.append("      AND REC_RANK.SEMESTER = MDIST.SEMESTER ");
            stb.append("      AND REC_RANK.TESTKINDCD = MDIST.TESTKINDCD ");
            stb.append("      AND REC_RANK.TESTITEMCD = MDIST.TESTITEMCD ");
            stb.append("      AND REC_RANK.SCORE_DIV = MDIST.SCORE_DIV ");
            stb.append("      AND REC_RANK.CLASSCD = MDIST.CLASSCD ");
            stb.append("      AND REC_RANK.SCHOOL_KIND = MDIST.SCHOOL_KIND ");
            stb.append("      AND REC_RANK.CURRICULUM_CD = MDIST.CURRICULUM_CD ");
            stb.append("      AND REC_RANK.SUBCLASSCD = MDIST.SUBCLASSCD ");
            stb.append("      AND REC_RANK.SCHREGNO = MDIST.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAD ");
            stb.append("       ON RAD.YEAR = REC_RANK.YEAR ");
            stb.append("      AND RAD.SEMESTER = REC_RANK.SEMESTER ");
            stb.append("      AND RAD.TESTKINDCD = REC_RANK.TESTKINDCD ");
            stb.append("      AND RAD.TESTITEMCD = REC_RANK.TESTITEMCD ");
            stb.append("      AND RAD.CLASSCD = REC_RANK.CLASSCD ");
            stb.append("      AND RAD.SCHOOL_KIND = REC_RANK.SCHOOL_KIND ");
            stb.append("      AND RAD.CURRICULUM_CD = REC_RANK.CURRICULUM_CD ");
            stb.append("      AND RAD.SUBCLASSCD = REC_RANK.SUBCLASSCD ");
            stb.append("      AND RAD.SCORE_DIV = MDIST.SCORE_DIV ");
            stb.append("      AND RAD.AVG_DIV = '1' ");
            stb.append("      AND RAD.GRADE = '" + _param._grade + "' ");
            stb.append("      AND RAD.HR_CLASS = '000' ");
            stb.append("      AND RAD.COURSECD = '0' ");
            stb.append("      AND RAD.MAJORCD = '000' ");
            stb.append("      AND RAD.COURSECODE = '0000' ");
            stb.append(" WHERE ");
            stb.append("     MDIST.YEAR = '" + _param._year + "' ");
            stb.append("     AND MDIST.SCORE_DIV = '01' ");
            stb.append("     AND MDIST.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND MDIST.SUBCLASSCD = '" + ALL9 + "' ");
            //成績データ(5)
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     MDIST.SEMESTER || MDIST.TESTKINDCD || MDIST.TESTITEMCD || MDIST.SCORE_DIV AS TESTCD, ");
            if ("1".equals(_param._useCurriculumCd)) {
                stb.append("     REC_RANK.CLASSCD, ");
                stb.append("     REC_RANK.SCHOOL_KIND, ");
                stb.append("     REC_RANK.CURRICULUM_CD, ");
            }
            stb.append("     CASE WHEN REC_RANK.SUBCLASSCD IS NULL THEN '" + ALL5 + "' ELSE REC_RANK.SUBCLASSCD END AS SUBCLASSCD, ");
            stb.append("     '全科目' AS SUBCLASSNAME, ");
            stb.append("     REC_RANK.SCORE, ");
            stb.append("     REC_RANK.AVG, ");
            stb.append("     0 AS GCNT, ");
            stb.append("     CASE WHEN RAD.AVG IS NULL THEN NULL ELSE DECIMAL(ROUND( ( RAD.AVG )* 10 , 0 ) / 10 , 5, 1 ) END AS GAVG, ");
            stb.append("     REC_RANK.GRADE_RANK, ");
            stb.append("     REC_RANK.GRADE_AVG_RANK, ");
            stb.append("     REC_RANK.GRADE_DEVIATION, ");
            stb.append("     REC_RANK.GRADE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CLASS_RANK, ");
            stb.append("     REC_RANK.CLASS_AVG_RANK, ");
            stb.append("     REC_RANK.CLASS_DEVIATION, ");
            stb.append("     REC_RANK.CLASS_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_RANK, ");
            stb.append("     REC_RANK.COURSE_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_DEVIATION_RANK, ");
            stb.append("     REC_RANK.MAJOR_RANK, ");
            stb.append("     REC_RANK.MAJOR_AVG_RANK, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION, ");
            stb.append("     REC_RANK.MAJOR_DEVIATION_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.COURSE_GROUP_DEVIATION_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_AVG_RANK, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION, ");
            stb.append("     REC_RANK.CHAIR_GROUP_DEVIATION_RANK, ");
            stb.append("     CAST(NULL AS INTEGER) AS HIGHSCORE, ");
            stb.append("     CAST(NULL AS INTEGER) AS LOWSCORE, ");
            stb.append("     CAST(NULL AS INTEGER) AS ADD_EX_SCORE, ");
            stb.append("     '' AS VALUE_DI ");
            stb.append(" FROM ");
            stb.append("     MERGE_DIST_KEY MDIST ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT REC_RANK ");
            stb.append("       ON REC_RANK.YEAR = MDIST.YEAR ");
            stb.append("      AND REC_RANK.SEMESTER = MDIST.SEMESTER ");
            stb.append("      AND REC_RANK.TESTKINDCD = MDIST.TESTKINDCD ");
            stb.append("      AND REC_RANK.TESTITEMCD = MDIST.TESTITEMCD ");
            stb.append("      AND REC_RANK.SCORE_DIV = MDIST.SCORE_DIV ");
            stb.append("      AND REC_RANK.CLASSCD = MDIST.CLASSCD ");
            stb.append("      AND REC_RANK.SCHOOL_KIND = MDIST.SCHOOL_KIND ");
            stb.append("      AND REC_RANK.CURRICULUM_CD = MDIST.CURRICULUM_CD ");
            stb.append("      AND REC_RANK.SUBCLASSCD = MDIST.SUBCLASSCD ");
            stb.append("      AND REC_RANK.SCHREGNO = MDIST.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAD ");
            stb.append("       ON RAD.YEAR = REC_RANK.YEAR ");
            stb.append("      AND RAD.SEMESTER = REC_RANK.SEMESTER ");
            stb.append("      AND RAD.TESTKINDCD = REC_RANK.TESTKINDCD ");
            stb.append("      AND RAD.TESTITEMCD = REC_RANK.TESTITEMCD ");
            stb.append("      AND RAD.CLASSCD = REC_RANK.CLASSCD ");
            stb.append("      AND RAD.SCHOOL_KIND = REC_RANK.SCHOOL_KIND ");
            stb.append("      AND RAD.CURRICULUM_CD = REC_RANK.CURRICULUM_CD ");
            stb.append("      AND RAD.SUBCLASSCD = REC_RANK.SUBCLASSCD ");
            stb.append("      AND RAD.AVG_DIV = '1' ");
            stb.append("      AND RAD.SCORE_DIV = MDIST.SCORE_DIV ");
            stb.append("      AND RAD.GRADE = '" + _param._grade + "' ");
            stb.append("      AND RAD.HR_CLASS = '000' ");
            stb.append("      AND RAD.COURSECD = '0' ");
            stb.append("      AND RAD.MAJORCD = '000' ");
            stb.append("      AND RAD.COURSECODE = '0000' ");
            stb.append(" WHERE ");
            stb.append("     MDIST.YEAR = '" + _param._year + "' ");
            stb.append("     AND MDIST.SCORE_DIV = '01' ");
            stb.append("     AND MDIST.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND MDIST.SUBCLASSCD = '" + ALL5 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     TESTCD, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");

            //log.debug(stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testcd = rs.getString("TESTCD");
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    final String avg = StringUtils.defaultString(rs.getString("AVG"));
                    final String gCnt = StringUtils.defaultString(rs.getString("GCNT"));
                    final String gAvg = StringUtils.defaultString(rs.getString("GAVG"));
                    final String gradeRank = StringUtils.defaultString(rs.getString("GRADE_RANK"));
                    final String gradeAvgRank = StringUtils.defaultString(rs.getString("GRADE_AVG_RANK"));
                    final String gradeDeviation = StringUtils.defaultString(rs.getString("GRADE_DEVIATION"));
                    final String gradeDeviationRank = StringUtils.defaultString(rs.getString("GRADE_DEVIATION_RANK"));
                    final String classRank = StringUtils.defaultString(rs.getString("CLASS_RANK"));
                    final String classAvgRank = StringUtils.defaultString(rs.getString("CLASS_AVG_RANK"));
                    final String classDeviation = StringUtils.defaultString(rs.getString("CLASS_DEVIATION"));
                    final String classDeviationRank = StringUtils.defaultString(rs.getString("CLASS_DEVIATION_RANK"));
                    final String courseRank = StringUtils.defaultString(rs.getString("COURSE_RANK"));
                    final String courseAvgRank = StringUtils.defaultString(rs.getString("COURSE_AVG_RANK"));
                    final String courseDeviation = StringUtils.defaultString(rs.getString("COURSE_DEVIATION"));
                    final String courseDeviationRank = StringUtils.defaultString(rs.getString("COURSE_DEVIATION_RANK"));
                    final String majorRank = StringUtils.defaultString(rs.getString("MAJOR_RANK"));
                    final String majorAvgRank = StringUtils.defaultString(rs.getString("MAJOR_AVG_RANK"));
                    final String majorDeviation = StringUtils.defaultString(rs.getString("MAJOR_DEVIATION"));
                    final String majorDeviationRank = StringUtils.defaultString(rs.getString("MAJOR_DEVIATION_RANK"));
                    final String courseGroupRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_RANK"));
                    final String courseGroupAvgRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_AVG_RANK"));
                    final String courseGroupDeviation = StringUtils.defaultString(rs.getString("COURSE_GROUP_DEVIATION"));
                    final String courseGroupDeviationRank = StringUtils.defaultString(rs.getString("COURSE_GROUP_DEVIATION_RANK"));
                    final String chairGroupRank = StringUtils.defaultString(rs.getString("CHAIR_GROUP_RANK"));
                    final String chairGroupAvgRank = StringUtils.defaultString(rs.getString("CHAIR_GROUP_AVG_RANK"));
                    final String chairGroupDeviation = StringUtils.defaultString(rs.getString("CHAIR_GROUP_DEVIATION"));
                    final String chairGroupDeviationRank = StringUtils.defaultString(rs.getString("CHAIR_GROUP_DEVIATION_RANK"));
                    final String addExScore = rs.getString("ADD_EX_SCORE");
                    final String valueDI = StringUtils.defaultString(rs.getString("VALUE_DI"));
                    final String highScore = StringUtils.defaultString(rs.getString("HIGHSCORE"));
                    final String lowScore = StringUtils.defaultString(rs.getString("LOWSCORE"));

                    final SubclassRank subclassRank = new SubclassRank(
                            testcd,
                            classcd,
                            schoolKind,
                            curriculumCd,
                            subclasscd,
                            subclassname,
                            score,
                            avg,
                            gCnt,
                            gAvg,
                            gradeRank,
                            gradeAvgRank,
                            gradeDeviation,
                            gradeDeviationRank,
                            classRank,
                            classAvgRank,
                            classDeviation,
                            classDeviationRank,
                            courseRank,
                            courseAvgRank,
                            courseDeviation,
                            courseDeviationRank,
                            majorRank,
                            majorAvgRank,
                            majorDeviation,
                            majorDeviationRank,
                            courseGroupRank,
                            courseGroupAvgRank,
                            courseGroupDeviation,
                            courseGroupDeviationRank,
                            chairGroupRank,
                            chairGroupAvgRank,
                            chairGroupDeviation,
                            chairGroupDeviationRank,
                            addExScore,
                            valueDI,
                            highScore,
                            lowScore
                    );
                    _subclassRankList.add(subclassRank);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            final StringBuffer stbAvg = new StringBuffer();
            stbAvg.append(" SELECT ");
            stbAvg.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV AS TESTCD, ");
            stbAvg.append("     DECIMAL(ROUND(AVG(FLOAT(REC_RANK.GRADE_DEVIATION))*10,0)/10,5,1) AS DEVIATION_AVG ");
            stbAvg.append(" FROM ");
            stbAvg.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
            stbAvg.append("     INNER JOIN SUBCLASS_MST SUBM ");
            stbAvg.append("        ON REC_RANK.CLASSCD = SUBM.CLASSCD ");
            stbAvg.append("       AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stbAvg.append("       AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stbAvg.append("       AND REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stbAvg.append(" WHERE ");
            stbAvg.append("     REC_RANK.YEAR = '" + _param._year + "' ");
            stbAvg.append("     AND REC_RANK.SCORE_DIV = '01' ");
            stbAvg.append("     AND REC_RANK.SCHREGNO = '" + _schregno + "' ");
            stbAvg.append("     AND REC_RANK.CLASSCD || '-' || REC_RANK.SCHOOL_KIND || '-' || REC_RANK.CURRICULUM_CD || '-' || REC_RANK.SUBCLASSCD ");
            stbAvg.append("          NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'D017') ");
            stbAvg.append(" GROUP BY ");
            stbAvg.append("     REC_RANK.SEMESTER || REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV ");

            PreparedStatement psAvg = null;
            ResultSet rsAvg = null;
            try {
                psAvg = db2.prepareStatement(stbAvg.toString());
                rsAvg = psAvg.executeQuery();
                while (rsAvg.next()) {
                    final String testcd = rsAvg.getString("TESTCD");
                    final String deviationAvg = StringUtils.defaultString(rsAvg.getString("DEVIATION_AVG"));

                    final AllAvg allAvg = new AllAvg(
                            testcd,
                            deviationAvg
                    );
                    _subclassAvgMap.put(testcd, allAvg);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psAvg, rsAvg);
            }

            if ("1".equals(_param._printTuishi)) {
            	final StringBuffer stbKesshiTest = new StringBuffer();
            	stbKesshiTest.append(" SELECT DISTINCT ");
            	stbKesshiTest.append("     REC.SEMESTER || REC.TESTKINDCD || REC.TESTITEMCD || REC.SCORE_DIV AS TESTCD ");
            	stbKesshiTest.append(" FROM ");
            	stbKesshiTest.append("     RECORD_SCORE_DAT REC ");
            	stbKesshiTest.append("     INNER JOIN SUBCLASS_MST SUBM ");
            	stbKesshiTest.append("        ON REC.CLASSCD = SUBM.CLASSCD ");
            	stbKesshiTest.append("       AND REC.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            	stbKesshiTest.append("       AND REC.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            	stbKesshiTest.append("       AND REC.SUBCLASSCD = SUBM.SUBCLASSCD ");
            	stbKesshiTest.append(" WHERE ");
            	stbKesshiTest.append("     REC.YEAR = '" + _param._year + "' ");
            	stbKesshiTest.append("     AND REC.SCORE_DIV = '01' ");
            	stbKesshiTest.append("     AND REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD ");
            	stbKesshiTest.append("          NOT IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._year + "' AND NAMECD1 = 'D017') ");
            	stbKesshiTest.append("     AND REC.SCHREGNO = '" + _schregno + "' ");
            	stbKesshiTest.append("     AND REC.VALUE_DI IS NOT NULL ");
            	stbKesshiTest.append(" GROUP BY ");
            	stbKesshiTest.append("     REC.SEMESTER || REC.TESTKINDCD || REC.TESTITEMCD || REC.SCORE_DIV ");
            	
            	PreparedStatement psKesshitest = null;
            	ResultSet rsKesshitest = null;
            	try {
            		psKesshitest = db2.prepareStatement(stbKesshiTest.toString());
            		rsKesshitest = psKesshitest.executeQuery();
            		while (rsKesshitest.next()) {
            			_kesshiTestcdList.add(rsKesshitest.getString("TESTCD"));
            		}
            	} catch (final SQLException e) {
            		log.error("生徒の基本情報取得でエラー", e);
            		throw e;
            	} finally {
            		db2.commit();
            		DbUtils.closeQuietly(null, psKesshitest, rsKesshitest);
            	}
            }

        }
        public void setProficiency(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            //キー項目を取り出す
            stb.append(" WITH DEVI_CALC_DIST_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     TA1.YEAR, ");
            stb.append("     TA1.SEMESTER, ");
            stb.append("     TA1.PROFICIENCYDIV, ");
            stb.append("     TA1.PROFICIENCYCD, ");
            stb.append("     TA1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_RANK_DAT TA1 ");
            stb.append(" WHERE ");
            stb.append("   TA1.YEAR = '" + _param._year + "' AND TA1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("   AND TA1.SCHREGNO = ? ");
            stb.append("   AND TA1.RANK_DATA_DIV = '03' ");
            stb.append("   AND TA1.RANK_DIV = '01' ");
            stb.append(" UNION ");
            // 欠席分の試験も加味するため、PROFICIENCY_DATも計上
            stb.append(" SELECT ");
            stb.append("     TB1.YEAR, ");
            stb.append("     TB1.SEMESTER, ");
            stb.append("     TB1.PROFICIENCYDIV, ");
            stb.append("     TB1.PROFICIENCYCD, ");
            stb.append("     TB1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   PROFICIENCY_DAT TB1 ");
            stb.append(" WHERE ");
            stb.append("   TB1.YEAR = '" + _param._year + "' AND TB1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("   AND TB1.SCHREGNO = ? ");
            // キーを一意(unique)にする
            stb.append(" ), CALC_DEVI_DIST AS ( ");
            stb.append("  SELECT DISTINCT ");
            stb.append("    * ");
            stb.append("  FROM ");
            stb.append("   DEVI_CALC_DIST_BASE ");
            // 科目偏差値平均を算出
            stb.append(" ), CALC_DEVI AS ( ");
            stb.append(" SELECT ");
            stb.append("     TC1.*, ");
            stb.append("     AVG(TD1.DEVIATION) AS AVG_DEVI ");
            stb.append(" FROM ");
            stb.append("     CALC_DEVI_DIST TC1 ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT TD1 ");
            stb.append("       ON TD1.YEAR = TC1.YEAR ");
            stb.append("      AND TD1.SEMESTER = TC1.SEMESTER ");
            stb.append("      AND TD1.SCHREGNO = TC1.SCHREGNO ");
            stb.append("      AND TD1.PROFICIENCYDIV = TC1.PROFICIENCYDIV ");
            stb.append("      AND TD1.PROFICIENCYCD = TC1.PROFICIENCYCD ");
            stb.append("      AND TD1.RANK_DATA_DIV = '03' ");
            stb.append("      AND TD1.RANK_DIV = '01' ");
            stb.append(" GROUP BY ");
            stb.append("     TC1.YEAR, ");
            stb.append("     TC1.SEMESTER, ");
            stb.append("     TC1.PROFICIENCYDIV, ");
            stb.append("     TC1.PROFICIENCYCD, ");
            stb.append("     TC1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.PROFICIENCYDIV, ");
            stb.append("     T1.PROFICIENCYCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR || T1.SEMESTER || T1.PROFICIENCYDIV || T1.PROFICIENCYCD AS SEMTESTCD, ");
            stb.append("     T2.PROFICIENCYNAME1, ");
            stb.append("     T12.AVG_DEVI, ");
            stb.append("     T1.RANK AS GRADE_RANK, ");
            stb.append("     T1.AVG, ");
            stb.append("     T13.RANK AS COURSE_RANK ");
            stb.append(" FROM  ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append(" INNER JOIN PROFICIENCY_MST T2 ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append(" LEFT JOIN PROFICIENCY_SUBCLASS_MST T9 ON T9.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" LEFT JOIN CALC_DEVI T12 ");
            stb.append("   ON T12.YEAR = T1.YEAR ");
            stb.append("  AND T12.SEMESTER = T1.SEMESTER ");
            stb.append("  AND T12.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("  AND T12.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append("  AND T12.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN PROFICIENCY_RANK_DAT T13 ");
            stb.append("   ON T13.YEAR = T1.YEAR ");
            stb.append("  AND T13.SEMESTER = T1.SEMESTER ");
            stb.append("  AND T13.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("  AND T13.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append("  AND T13.SCHREGNO = T1.SCHREGNO ");
            stb.append("  AND T13.RANK_DATA_DIV = '01' ");
            stb.append("  AND T13.RANK_DIV = '03' ");
            stb.append("  AND T13.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append("     AND T1.PROFICIENCY_SUBCLASS_CD = '" + ALL9 + "' ");
            stb.append("     AND T1.RANK_DATA_DIV = '01' ");
            stb.append("     AND T1.RANK_DIV = '01' ");
            stb.append(" ORDER BY ");
            stb.append("  T1.SEMESTER DESC, ");
            stb.append("  T1.PROFICIENCYDIV DESC, ");
            stb.append("  T1.PROFICIENCYCD DESC ");

            //log.debug(stb.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                ps.setString(1, _schregno);
                ps.setString(2, _schregno);
                ps.setString(3, _schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String year = rs.getString("YEAR");
                	final String semester = rs.getString("SEMESTER");
                	final String proficiencydiv = rs.getString("PROFICIENCYDIV");
                	final String proficiencycd = rs.getString("PROFICIENCYCD");
                	final String schregno = rs.getString("SCHREGNO");
                	final String semtestcd = rs.getString("SEMTESTCD");
                	final String proficiencyname1 = rs.getString("PROFICIENCYNAME1");
                	final String avg = rs.getString("AVG");
                	final String grade_Rank = rs.getString("GRADE_RANK");
                	final String avgDevi = rs.getString("AVG_DEVI");
                	final String courseRank = rs.getString("COURSE_RANK");

                	ProficiencyData addwk = new ProficiencyData(year, semester, proficiencydiv, proficiencycd, schregno, semtestcd, proficiencyname1, avg, grade_Rank, avgDevi, courseRank);
                	_proficiencyList.add(addwk);
                }
            } catch (final SQLException e) {
                log.error("生徒の実力テスト情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private class AllAvg {
        final String _testcd;
        final String _deviationAvg;

        public AllAvg(
                final String testcd,
                final String deviationAvg
        ) {
            _testcd = testcd;
            _deviationAvg = deviationAvg;
        }
    }


    private class SubclassRank {
        final String _testcd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _score;
        final String _avg;
        final String _gCnt; //指定学年(SVG_DIV=1)で指定科目の人数
        final String _gAvg;
        final String _gradeRank;
        final String _gradeAvgRank;
        final String _gradeDeviation;
        final String _gradeDeviationRank;
        final String _classRank;
        final String _classAvgRank;
        final String _classDeviation;
        final String _classDeviationRank;
        final String _courseRank;
        final String _courseAvgRank;
        final String _courseDeviation;
        final String _courseDeviationRank;
        final String _majorRank;
        final String _majorAvgRank;
        final String _majorDeviation;
        final String _majorDeviationRank;
        final String _courseGroupRank;
        final String _courseGroupAvgRank;
        final String _courseGroupDeviation;
        final String _courseGroupDeviationRank;
        final String _chairGroupRank;
        final String _chairGroupAvgRank;
        final String _chairGroupDeviation;
        final String _chairGroupDeviationRank;
        final String _addExScore;
        final String _valueDI;
        final String _highScore;
        final String _lowScore;

        public SubclassRank(
                final String testcd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String score,
                final String avg,
                final String gCnt,
                final String gAvg,
                final String gradeRank,
                final String gradeAvgRank,
                final String gradeDeviation,
                final String gradeDeviationRank,
                final String classRank,
                final String classAvgRank,
                final String classDeviation,
                final String classDeviationRank,
                final String courseRank,
                final String courseAvgRank,
                final String courseDeviation,
                final String courseDeviationRank,
                final String majorRank,
                final String majorAvgRank,
                final String majorDeviation,
                final String majorDeviationRank,
                final String courseGroupRank,
                final String courseGroupAvgRank,
                final String courseGroupDeviation,
                final String courseGroupDeviationRank,
                final String chairGroupRank,
                final String chairGroupAvgRank,
                final String chairGroupDeviation,
                final String chairGroupDeviationRank,
                final String addExScore,
                final String valueDI,
                final String highScore,
                final String lowScore
        ) {
            _testcd = testcd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _avg = avg;
            _gCnt = gCnt;
            _gAvg = gAvg;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _gradeDeviation = gradeDeviation;
            _gradeDeviationRank = gradeDeviationRank;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classDeviation = classDeviation;
            _classDeviationRank = classDeviationRank;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseDeviation = courseDeviation;
            _courseDeviationRank = courseDeviationRank;
            _majorRank = majorRank;
            _majorAvgRank = majorAvgRank;
            _majorDeviation = majorDeviation;
            _majorDeviationRank = majorDeviationRank;
            _courseGroupRank = courseGroupRank;
            _courseGroupAvgRank = courseGroupAvgRank;
            _courseGroupDeviation = courseGroupDeviation;
            _courseGroupDeviationRank = courseGroupDeviationRank;
            _chairGroupRank = chairGroupRank;
            _chairGroupAvgRank = chairGroupAvgRank;
            _chairGroupDeviation = chairGroupDeviation;
            _chairGroupDeviationRank = chairGroupDeviationRank;
            _addExScore = addExScore;
            _valueDI = valueDI;
            _highScore = highScore;
            _lowScore = lowScore;
        }

        public String getKey() {
            return _classcd + _schoolKind + _curriculumCd + _subclasscd;
        }
    }
    private class ProficiencyData {
        final String _year;
        final String _semester;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _schregno;
        final String _semtestcd;
        final String _proficiencyname1;
        final String _avg;
        final String _grade_Rank;
        final String _course_Rank;
        final String _avgDevi;
        public ProficiencyData (final String year, final String semester, final String proficiencydiv, final String proficiencycd, final String schregno, final String semtestcd, final String proficiencyname1, final String avg, final String grade_Rank, final String avgDevi, final String course_Rank)
        {
            _year = year;
            _semester = semester;
            _proficiencydiv = proficiencydiv;
            _proficiencycd = proficiencycd;
            _schregno = schregno;
            _semtestcd = semtestcd;
            _proficiencyname1 = proficiencyname1;
            _avg = avg;
            _grade_Rank = grade_Rank;
            _avgDevi = avgDevi;
            _course_Rank = course_Rank;
        }
    }

    private class SubclassBunpu {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassOrderName3;
        final int _score0;
        final int _score1;
        final int _score2;
        final int _score3;
        final int _score4;
        final int _score5;
        final int _score6;
        final int _score7;
        final int _score8;
        final int _score9;

        public SubclassBunpu(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassOrderName3,
                final int score0,
                final int score1,
                final int score2,
                final int score3,
                final int score4,
                final int score5,
                final int score6,
                final int score7,
                final int score8,
                final int score9
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassOrderName3 = subclassOrderName3;
            _score0 = score0;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;
            _score4 = score4;
            _score5 = score5;
            _score6 = score6;
            _score7 = score7;
            _score8 = score8;
            _score9 = score9;
        }
    }

    private class TestItemMst {
        final String _semester;
        final String _semesterName;
        final String _testKindCd;
        final String _testItemCd;
        final String _scoreDiv;
        final String _testitemName;

        public TestItemMst(
                final String semester,
                final String semesterName,
                final String testKindCd,
                final String testItemCd,
                final String scoreDiv,
                final String testitemName
        ) {
            _semester = semester;
            _semesterName = semesterName;
            _testKindCd = testKindCd;
            _testItemCd = testItemCd;
            _scoreDiv = scoreDiv;
            _testitemName = testitemName;
        }

        public String getKey() {
            return _semester + _testKindCd + _testItemCd + _scoreDiv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70802 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _testcd;
        final String _testName;
        final String _grade;
        final String _hrClass;
        final String[] _categorySelected;
        final String _year;
        final String _ctrlSeme;
        final String _documentroot;
        final String _imagePath;
        final String _useCurriculumCd;
        final String _printTuishi;
        final String _SlashImagePath;
        final Map _testItemMstMap;
        final Map _subclassBunpuMap;
        final List _d017Name1List;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _testcd = request.getParameter("TESTKINDCD");
            _categorySelected = request.getParameterValues("category_selected");
            _printTuishi = request.getParameter("PRINT_TUISHI");
            _year = request.getParameter("YEAR");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _useCurriculumCd = request.getParameter("useCurriculumcd");

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagePath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _testName = getTestname(db2);
            _testItemMstMap = getTestMap(db2);
            _subclassBunpuMap = getSubclassBunpu(db2);
            _d017Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year +"' AND NAMECD1 = 'D017' "), "NAME1");
            _SlashImagePath = getImageFilePath("slash.jpg");
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private String getTestname(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.TESTITEMNAME, ");
                stb.append("     SM.SEMESTERNAME ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV");
                stb.append("     LEFT JOIN SEMESTER_MST SM ");
                stb.append("        ON SM.YEAR = SDIV.YEAR ");
                stb.append("       AND SM.SEMESTER = SDIV.SEMESTER");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _year + "' ");
                stb.append("     AND SDIV.SEMESTER = '" + _semester + "' ");
                stb.append("     AND SDIV.SCORE_DIV = '01' ");
                stb.append("     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _testcd + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("SEMESTERNAME")) + StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

        private Map getTestMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SDIV.*, ");
                stb.append("     SM.SEMESTERNAME ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV SDIV");
                stb.append("     LEFT JOIN SEMESTER_MST SM ");
                stb.append("        ON SM.YEAR = SDIV.YEAR ");
                stb.append("       AND SM.SEMESTER = SDIV.SEMESTER");
                stb.append(" WHERE ");
                stb.append("     SDIV.YEAR = '" + _year + "' ");
                stb.append("     AND SDIV.SCORE_DIV = '01' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD <> '9900' ");
                stb.append(" ORDER BY ");
                stb.append("     SDIV.SEMESTER, ");
                stb.append("     SDIV.TESTKINDCD, ");
                stb.append("     SDIV.TESTITEMCD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                    final String semesterName = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
                    final String testKindCd = StringUtils.defaultString(rs.getString("TESTKINDCD"));
                    final String testItemCd = StringUtils.defaultString(rs.getString("TESTITEMCD"));
                    final String scoreDiv = StringUtils.defaultString(rs.getString("SCORE_DIV"));
                    final String testitemName = StringUtils.defaultString(rs.getString("TESTITEMNAME"));

                    final TestItemMst testItemMst = new TestItemMst(semester, semesterName, testKindCd, testItemCd, scoreDiv, testitemName);
                    final String setTestCd = semester + testKindCd + testItemCd + scoreDiv;
                    retMap.put(setTestCd, testItemMst);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getSubclassBunpu(final DB2UDB db2) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                if ("1".equals(_useCurriculumCd)) {
                    stb.append("     REC_RANK.CLASSCD, ");
                    stb.append("     REC_RANK.SCHOOL_KIND, ");
                    stb.append("     REC_RANK.CURRICULUM_CD, ");
                }
                stb.append("     REC_RANK.SUBCLASSCD, ");
                stb.append("     MAX(SUBM.SUBCLASSORDERNAME3) AS SUBCLASSORDERNAME3, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 0 AND 29 THEN 1 ELSE 0 END) AS SCORE0, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS SCORE3, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS SCORE4, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS SCORE5, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 60 AND 69 THEN 1 ELSE 0 END) AS SCORE6, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 70 AND 79 THEN 1 ELSE 0 END) AS SCORE7, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 80 AND 89 THEN 1 ELSE 0 END) AS SCORE8, ");
                stb.append("     SUM(CASE WHEN REC_RANK.SCORE BETWEEN 90 AND 100 THEN 1 ELSE 0 END) AS SCORE9 ");
                stb.append(" FROM ");
                stb.append("     RECORD_RANK_SDIV_DAT REC_RANK ");
                stb.append("     INNER JOIN SUBCLASS_MST SUBM ");
                stb.append("        ON REC_RANK.SUBCLASSCD = SUBM.SUBCLASSCD ");
                if ("1".equals(_useCurriculumCd)) {
                    stb.append("       AND REC_RANK.CLASSCD = SUBM.CLASSCD ");
                    stb.append("       AND REC_RANK.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
                    stb.append("       AND REC_RANK.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
                }
                stb.append(" WHERE ");
                stb.append("     REC_RANK.YEAR = '" + _year + "' ");
                stb.append("     AND REC_RANK.SEMESTER = '" + _semester + "' ");
                stb.append("     AND REC_RANK.TESTKINDCD || REC_RANK.TESTITEMCD || REC_RANK.SCORE_DIV = '" + _testcd + "' ");
                stb.append("     AND EXISTS ( ");
                stb.append("         SELECT ");
                stb.append("             'x' ");
                stb.append("         FROM ");
                stb.append("             SCHREG_REGD_DAT REGD ");
                stb.append("         WHERE ");
                stb.append("             REC_RANK.YEAR = REGD.YEAR ");
                stb.append("             AND REC_RANK.SEMESTER = REGD.SEMESTER ");
                stb.append("             AND REGD.GRADE = '" + _grade + "' ");
                stb.append("             AND REC_RANK.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     ) ");
                stb.append(" GROUP BY ");
                if ("1".equals(_useCurriculumCd)) {
                    stb.append("     REC_RANK.CLASSCD, ");
                    stb.append("     REC_RANK.SCHOOL_KIND, ");
                    stb.append("     REC_RANK.CURRICULUM_CD, ");
                }
                stb.append("     REC_RANK.SUBCLASSCD ");

                log.debug(stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculumCd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassOrderName3 = StringUtils.defaultString(rs.getString("SUBCLASSORDERNAME3"));
                    final int score0 = rs.getInt("SCORE0");
                    final int score3 = rs.getInt("SCORE3");
                    final int score4 = rs.getInt("SCORE4");
                    final int score5 = rs.getInt("SCORE5");
                    final int score6 = rs.getInt("SCORE6");
                    final int score7 = rs.getInt("SCORE7");
                    final int score8 = rs.getInt("SCORE8");
                    final int score9 = rs.getInt("SCORE9");

                    final SubclassBunpu subclassBunpu = new SubclassBunpu(classcd, schoolKind, curriculumCd, subclasscd, subclassOrderName3, score0, 0, 0, score3, score4, score5, score6, score7, score8, score9);
                    final String setSubclassCd = classcd + schoolKind + curriculumCd + subclasscd;
                    retMap.put(setSubclassCd, subclassBunpu);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}

// eof
