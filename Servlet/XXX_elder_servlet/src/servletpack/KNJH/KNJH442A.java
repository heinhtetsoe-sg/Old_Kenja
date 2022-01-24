/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: $
 *
 * 作成日: 2020/11/13
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJH442A {

    private static final Log log = LogFactory.getLog(KNJH442A.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV9990009 = "9990009"; //学年5段階評定

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final int MAX_LINE = 47;
    private static final int MAX_LINE_MEISAI = MAX_LINE - 2;

//    private static final String HYOTEI_TESTCD = "9990009";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List studentList = getList(db2);
//        //出欠
//        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
//            final DateRange range = (DateRange) rit.next();
//            Attendance.load(db2, _param, studentList, range);
//        }
//        //欠課
//        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
//            final DateRange range = (DateRange) rit.next();
//            SubclassAttendance.load(db2, _param, studentList, range);
//        }

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //一覧表
        printSvfMain(db2, svf, subclassList, studentList);

    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final List subclassList, final List studentList) {
        final String form = "KNJH442A.frm";
        svf.VrSetForm(form , 1);

        boolean outFlg = false;
        int page = 1;
        String value = "";;
        String before = "";
        int line = 1;
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //改ページ判定
            value = "1".equals(_param._output) ? student._grade + student._hrClass : student._coursecd + student._majorcd + student._coursecode;
            if((outFlg && (!before.equals(value)) || line > MAX_LINE_MEISAI)) {
            	//改ページ処理
            	printAvg(db2, svf, subclassList, value); //クラス平均、コース平均、学年平均の出力
            	svf.VrEndPage();

            	outFlg = false;
                page++;
            	line = 1;
            	svf.VrSetForm(form , 1);
                printTitle(db2, svf, subclassList, student, page); //明細部以外を印字
            }
            before = value;

            if(line == 1) printTitle(db2, svf, subclassList, student, page); //明細部以外を印字

            svf.VrsOutn("HR_NAME_ABBV", line, student._hrClass); //年組
            svf.VrsOutn("ATTEND_NO", line, student._attendno); //出席番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, line, student._name); //氏名

            //評定一覧
            int column = 1;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            	final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                if (student._printSubclassMap.containsKey(subclassCd)) {
                    final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                    //評価
                	svf.VrsOutn("DIV" + column, line, scoreData.score(SDIV9990009));
                	_hasData = true;
                }
                column++;
            }

            //理科履修科目
            final String suSubject = getSubclassName(db2, student._schregno, "05");
            svf.VrsOutn("SC_SUBJECT", line, suSubject);

            //総合成績
            final Map map = getAftSchregRecommendationRank(db2, student._schregno);
            if(map != null) {
                svf.VrsOutn("MOCK_SCORE1", line, (String)map.get("MOCK_NATIONAL_LANGUAGE_AVG"));
                svf.VrsOutn("MOCK_SCORE2", line, (String)map.get("MOCK_MATH_AVG"));
                svf.VrsOutn("MOCK_SCORE3", line, (String)map.get("MOCK_ENGLISH_AVG"));
                svf.VrsOutn("MOCK_SCORE4", line, (String)map.get("TEST_VALUATION_AVG"));
                svf.VrsOutn("MOCK_SCORE5", line, (String)map.get("TEST_VALUATION_PERCENT_SCORE"));
                svf.VrsOutn("MOCK_SCORE6", line, (String)map.get("MOCK_TOTAL_AVG"));
                svf.VrsOutn("MOCK_SCORE7", line, (String)map.get("MOCK_TOTAL_PERCENT_SCORE"));

                //学内成績
                double courseRank = Double.parseDouble((String)map.get("MOCK_TOTAL_SCORE_COURSE_RANK"));
                double gradeRank = Double.parseDouble((String)map.get("MOCK_TOTAL_SCORE_GRADE_RANK"));
                double cnt = Double.parseDouble((String)map.get("CNT"));
                if(courseRank > 0 && cnt > 0) {
                	double courseTop = courseRank / cnt;
                	svf.VrsOutn("COURSE_TOP", line, sishaGonyu(String.valueOf(courseTop), 2));
                }
                if(gradeRank > 0 && cnt > 0) {
                	double gradeTop = gradeRank / cnt;
                	svf.VrsOutn("GRADE_TOP", line, sishaGonyu(String.valueOf(gradeTop), 2));
                }
            	svf.VrsOutn("TOTAL_SCORE", line, (String)map.get("TOTAL_SCORE"));

                //順位
                svf.VrsOutn("RANK1", line, (String)map.get("MOCK_TOTAL_SCORE_CLASS_RANK"));
                svf.VrsOutn("RANK2", line, (String)map.get("MOCK_TOTAL_SCORE_COURSE_RANK"));
                svf.VrsOutn("RANK3", line, (String)map.get("MOCK_TOTAL_SCORE_GRADE_RANK"));
            }
            line++;
            outFlg = true;
        }
        //合計の印字
        if(outFlg) printAvg(db2, svf, subclassList, value); //クラス平均、コース平均、学年平均の出力

        svf.VrEndPage();

    }

	private void printTitle(final DB2UDB db2, final Vrw32alp svf, final List subclassList, final Student student, final int page) {
    	//明細部以外を印字

        //ヘッダ
		svf.VrsOut("NENDO", _param._year + "年度 " + ("1".equals(_param._titlediv) ? "指定校推薦" : "日大付属特別選抜"));
		final String schoolName = _param._certifSchoolSchoolName + " " + getGradeName(db2, _param._grade);
		svf.VrsOut("SCHOOL_NAME", schoolName); //学校名

		final String title = "1".equals(_param._output) ? "学級別学生成績一覧表" : "コース別学生成績一覧表";
		svf.VrsOut("TITLE", title); //タイトル
		final String subTitle = "【学内成績・成績】";
		svf.VrsOut("SUBTITLE", subTitle); //サブタイトル

		svf.VrsOut("PAGE", String.valueOf(page)); //ページ

        final Calendar cal = Calendar.getInstance();
        final String hour = String.valueOf(cal.get(Calendar.HOUR));
        final String minute = String.valueOf(cal.get(Calendar.MINUTE));
        final String date = KNJ_EditDate.h_format_SeirekiJP(_param._logintData) + hour + "時" + minute + "分";
		svf.VrsOut("DATE", date); //日付

        //年組コース
        final String hrCourseName = "1".equals(_param._output) ? student._hrname : "コース";
        svf.VrsOut("HR_COURSE_NAME", hrCourseName);

        //担任名
        final String name = "1".equals(_param._output) ? student._staffname : student._coursename;
        final String trNameField = KNJ_EditEdit.getMS932ByteLength(name) > 36 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 26 ? "2" : "1";
        svf.VrsOut("TR_COURSE_NAME" + trNameField, name);

        //評定教科
        int column = 1;
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
        	final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            svf.VrsOut("SUBCLASS_NAME1_" + column, "評" + subclassMst._subclassname);
            svf.VrsOut("DEFAULT_DIV" + column, "5");
            column++;
        }

        //実力教科
        column = 1;
        final String percentage = getPercentage(db2); //評定の実力に対する割合
        final String percentage2 = String.valueOf(100 - Integer.parseInt(percentage));
        final String str1 = "評" + percentage;
        final String str2 = "実" + percentage2;
		final String[] subclassName2 = {"実国語", "実数学", "実英語", "評定平均", str1, "実平均", str2};
		final String[] defaultMockScore = {"100", "100", "100", "5", percentage, "100", percentage2};
        for (int i = 0; i < subclassName2.length; i++) {
        	svf.VrsOut("SUBCLASS_NAME2_" + column, subclassName2[i]);
        	svf.VrsOut("DEFAULT_MOCK_SCORE" + column, defaultMockScore[i]);
        	column++;
        }
    }

    private void printAvg(final DB2UDB db2, final Vrw32alp svf, final List subclassList, final String value) {

        //平均 の印字 評定
    	final String kbn = "1".equals(_param._output) ? "CLASS" : "COURSE";
    	final Map classCourseMap = getAvgRecordRank(db2, kbn, value); //クラス or コース
    	final Map gradeMap = getAvgRecordRank(db2, "GRADE", _param._grade); //学年
    	if(classCourseMap != null && classCourseMap != null) {
            int column = 1;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            	final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            	final String subclasscd = subclassMst._subclasscd;
                if(classCourseMap != null && classCourseMap.containsKey(subclasscd)) {
                	//クラス平均 or コース平均
                	svf.VrsOutn("DIV" + column, MAX_LINE - 1, (String)classCourseMap.get(subclasscd));
                }
                if(classCourseMap != null && classCourseMap.containsKey(subclasscd)) {
                	//学年平均
                	svf.VrsOutn("DIV" + column, MAX_LINE, (String)gradeMap.get(subclasscd));
                }
                column++;
            }
    	}

    	//平均 の印字 総合成績
    	final Map aftClassCourseMap = getAvgAftSchregRecommendationRank(db2, kbn, value); //クラス or コース
        if(aftClassCourseMap != null) {
            svf.VrsOutn("MOCK_SCORE1", MAX_LINE - 1, (String)aftClassCourseMap.get("MOCK_NATIONAL_LANGUAGE_AVG"));
            svf.VrsOutn("MOCK_SCORE2", MAX_LINE - 1, (String)aftClassCourseMap.get("MOCK_MATH_AVG"));
            svf.VrsOutn("MOCK_SCORE3", MAX_LINE - 1, (String)aftClassCourseMap.get("MOCK_ENGLISH_AVG"));
            svf.VrsOutn("MOCK_SCORE4", MAX_LINE - 1, (String)aftClassCourseMap.get("TEST_VALUATION_AVG"));
            svf.VrsOutn("MOCK_SCORE5", MAX_LINE - 1, (String)aftClassCourseMap.get("TEST_VALUATION_PERCENT_SCORE"));
            svf.VrsOutn("MOCK_SCORE6", MAX_LINE - 1, (String)aftClassCourseMap.get("MOCK_TOTAL_AVG"));
            svf.VrsOutn("MOCK_SCORE7", MAX_LINE - 1, (String)aftClassCourseMap.get("MOCK_TOTAL_PERCENT_SCORE"));

            //学内成績
            svf.VrsOutn("TOTAL_SCORE", MAX_LINE - 1, (String)aftClassCourseMap.get("TOTAL_SCORE")); //総合成績
        }
        final Map aftGradeMap = getAvgAftSchregRecommendationRank(db2, "GRADE", _param._grade); //学年
        if(aftGradeMap != null) {
            svf.VrsOutn("MOCK_SCORE1", MAX_LINE, (String)aftGradeMap.get("MOCK_NATIONAL_LANGUAGE_AVG"));
            svf.VrsOutn("MOCK_SCORE2", MAX_LINE, (String)aftGradeMap.get("MOCK_MATH_AVG"));
            svf.VrsOutn("MOCK_SCORE3", MAX_LINE, (String)aftGradeMap.get("MOCK_ENGLISH_AVG"));
            svf.VrsOutn("MOCK_SCORE4", MAX_LINE, (String)aftGradeMap.get("TEST_VALUATION_AVG"));
            svf.VrsOutn("MOCK_SCORE5", MAX_LINE, (String)aftGradeMap.get("TEST_VALUATION_PERCENT_SCORE"));
            svf.VrsOutn("MOCK_SCORE6", MAX_LINE, (String)aftGradeMap.get("MOCK_TOTAL_AVG"));
            svf.VrsOutn("MOCK_SCORE7", MAX_LINE, (String)aftGradeMap.get("MOCK_TOTAL_PERCENT_SCORE"));

            //学内成績
            svf.VrsOutn("TOTAL_SCORE", MAX_LINE, (String)aftGradeMap.get("TOTAL_SCORE")); //総合成績
        }
    }

     private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
                continue;
            }
//            if (_param._isNoPrintMoto && subclassMst.isMoto() || !_param._isPrintSakiKamoku && subclassMst.isSaki()) {
//            	//元科目を表示しない場合、先科目が存在するコードを削除
//            	//先科目を表示しない場合、元科目が存在するコードを削除
//                it.remove();
//            }
            if (subclassMst.isMoto()) {
                //先科目を表示し、元科目は表示しない
                it.remove();
            }
        }
        return retList;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 4;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("GRADE_NAME2");
//                student._hrname = Integer.parseInt(rs.getString("GRADE_CD")) + "年" + rs.getString("HR_CLASS_NAME1") + "組" ;
                student._hrname = rs.getString("HR_NAMEABBV") + "組" ;
                student._trcd = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._coursecode = rs.getString("COURSECODE");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._coursename = rs.getString("COURSENAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._guard_zipcd = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._guard_addr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._guard_addr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._guard_name = StringUtils.defaultString(rs.getString("GUARD_NAME"));
                student._communication = StringUtils.defaultString(rs.getString("COMMUNICATION"));

                student.setSubclass(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._year + "' ");
        stb.append("        AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if("1".equals(_param._output)) {
            stb.append("        AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("        AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,REGDH.HR_NAMEABBV ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECODE ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,COURSE.COURSECODENAME ");
        stb.append("            ,VALUE(COURSE_M.COURSENAME, '') || VALUE(MAJOR.MAJORNAME, '') || VALUE(COURSE.COURSECODENAME, '') AS COURSENAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,GADDR.GUARD_ADDR1 ");
        stb.append("            ,GADDR.GUARD_ADDR2 ");
        stb.append("            ,GUARDIAN.GUARD_NAME ");
        stb.append("            ,R1.COMMUNICATION ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN COURSE_MST COURSE_M ");
        stb.append("            ON COURSE_M.COURSECD = REGD.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSE ");
        stb.append("            ON COURSE.COURSECODE = REGD.COURSECODE ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("            ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ON GADDR.SCHREGNO = L_GADDR.SCHREGNO AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE ");
        stb.append("     LEFT JOIN HREPORTREMARK_DAT R1 ");
        stb.append("            ON R1.YEAR     = REGD.YEAR ");
        stb.append("           AND R1.SEMESTER = REGD.SEMESTER ");
        stb.append("           AND R1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._year + "' ");
        stb.append("         AND REGD.SEMESTER = '" + _param._semester + "' ");
        if("1".equals(_param._output)) {
        	stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
        	stb.append("         AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }

        stb.append("     ORDER BY ");
        if("2".equals(_param._output)) {
        	stb.append("         REGD.COURSECD, ");
        	stb.append("         REGD.MAJORCD, ");
        	stb.append("         REGD.COURSECODE, ");
        }
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val, final int keta) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(keta, BigDecimal.ROUND_HALF_UP).toString();
    }

    //学年名称を取得
    private String getGradeName (final DB2UDB db2, final String grade) {
    	String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_GDAT ");
        stb.append(" WHERE ");
        stb.append("       YEAR  = '"+ _param._year +"' ");
        stb.append("   AND GRADE = '"+ grade +"' ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
        	log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	rtnStr = StringUtils.defaultString(rs.getString("GRADE_NAME1"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr;
    }

    //評定の実力に対する割合を取得
    private String getPercentage (final DB2UDB db2) {
    	String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   AFT_RECOMMENDATION_RANK_HEAD_DAT ");
        stb.append(" WHERE ");
        stb.append("       YEAR  = '"+ _param._year +"' ");
        stb.append("   AND GRADE = '"+ _param._grade +"' ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
        	log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	rtnStr = StringUtils.defaultString(rs.getString("PERCENTAGE"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr;
    }

    //過年度含む履修科目を取得
    private String getSubclassName (final DB2UDB db2, final String schregno, final String classcd) {
    	final StringBuffer rtnStr = new StringBuffer();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO_A AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     GDAT.GRADE_CD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("             ON GDAT.YEAR  = REGD.YEAR ");
        stb.append("            AND GDAT.GRADE = REGD.GRADE ");
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR     <= '"+ _param._year +"' ");
        stb.append("     AND REGD.SCHREGNO = '"+ schregno +"' ");
        stb.append(" ), SCHNO AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     SCHNO_A T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ");
        stb.append(" ) , CHAIR_A AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     S1.SCHREGNO, ");
        stb.append("     S2.CLASSCD, ");
        stb.append("     S2.SCHOOL_KIND, ");
        stb.append("     S2.CURRICULUM_CD, ");
        stb.append("     S2.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT S1 ");
        stb.append("     INNER JOIN CHAIR_DAT S2 ");
        stb.append("             ON S2.YEAR     = S1.YEAR ");
        stb.append("            AND S2.SEMESTER = S1.SEMESTER ");
        stb.append("            AND S2.CHAIRCD  = S1.CHAIRCD ");
        stb.append("     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) S3 ");
        stb.append("             ON S3.YEAR     = S1.YEAR ");
        stb.append("            AND S3.SCHREGNO = S1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T4.CLASSCD, ");
        stb.append("   T4.SCHOOL_KIND, ");
        stb.append("   T4.CURRICULUM_CD, ");
        stb.append("   T4.SUBCLASSCD, ");
        stb.append("   T4.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("   CHAIR_A T1 ");
        stb.append("   INNER JOIN CLASS_MST T3 ");
        stb.append("      ON T3.CLASSCD     = T1.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("   INNER JOIN SUBCLASS_MST T4 ");
        stb.append("      ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("     AND T4.CLASSCD       = T1.CLASSCD ");
        stb.append("     AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("     AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.CLASSCD = '"+ classcd +"' ");
        stb.append(" ORDER BY SUBCLASSCD ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
        	log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String seq = "";
            while (rs.next()) {
            	rtnStr.append(seq);
            	rtnStr.append(StringUtils.defaultString(rs.getString("SUBCLASSNAME")));
            	seq = ",";
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    //合併先科目を取得
    private String getCombinedSubclass (final DB2UDB db2, final String subclasscd) {
        String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT COMBINED_CLASSCD ||'-'|| COMBINED_SCHOOL_KIND ||'-'|| COMBINED_CURRICULUM_CD ||'-'|| COMBINED_SUBCLASSCD AS SUBCLASSCD ");
        stb.append("   FROM SUBCLASS_REPLACE_COMBINED_DAT ");
        stb.append("  WHERE YEAR = '"+ _param._year +"' ");
        stb.append("    AND ATTEND_CLASSCD ||'-'|| ATTEND_SCHOOL_KIND ||'-'|| ATTEND_CURRICULUM_CD ||'-'|| ATTEND_SUBCLASSCD = '"+ subclasscd +"' ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnStr = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    //指定校推薦総合成績を取得
    private Map getAftSchregRecommendationRank (final DB2UDB db2, final String schregno) {
        Map rtnMap = null;
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.*, ");
        stb.append("   T1.TEST_VALUATION_AVG + T1.TEST_VALUATION_AVG_PERCENT + T1.TEST_VALUATION_PERCENT_SCORE AS TOTAL_SCORE, ");
        stb.append("   T2.CNT ");
        stb.append(" FROM ");
        stb.append("   AFT_SCHREG_RECOMMENDATION_RANK_DAT T1 ");
        stb.append("   INNER JOIN (SELECT YEAR, COUNT(*) AS CNT ");
        stb.append("               FROM AFT_SCHREG_RECOMMENDATION_RANK_DAT  ");
        stb.append("               GROUP BY YEAR) T2 ");
        stb.append("           ON T2.YEAR = T1.YEAR ");
        stb.append(" WHERE ");
        stb.append("       T1.YEAR     = '"+ _param._year +"' ");
        stb.append("   AND T1.SCHREGNO = '"+ schregno +"' ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	rtnMap = new HashMap();
            	rtnMap.put("MOCK_NATIONAL_LANGUAGE_AVG", sishaGonyu(StringUtils.defaultString(rs.getString("MOCK_NATIONAL_LANGUAGE_AVG")), 2));
            	rtnMap.put("MOCK_MATH_AVG", sishaGonyu(StringUtils.defaultString(rs.getString("MOCK_MATH_AVG")), 2));
            	rtnMap.put("MOCK_ENGLISH_AVG", sishaGonyu(StringUtils.defaultString(rs.getString("MOCK_ENGLISH_AVG")), 2));
            	rtnMap.put("MOCK_TOTAL_SCORE", sishaGonyu(StringUtils.defaultString(rs.getString("MOCK_TOTAL_SCORE")), 2));
            	rtnMap.put("MOCK_TOTAL_AVG", sishaGonyu(StringUtils.defaultString(rs.getString("MOCK_TOTAL_AVG")), 2));
            	rtnMap.put("MOCK_TOTAL_PERCENT_SCORE", sishaGonyu(StringUtils.defaultString(rs.getString("MOCK_TOTAL_PERCENT_SCORE")), 2));
            	rtnMap.put("TEST_VALUATION_AVG", sishaGonyu(StringUtils.defaultString(rs.getString("TEST_VALUATION_AVG")), 2));
            	rtnMap.put("TEST_VALUATION_AVG_PERCENT", sishaGonyu(StringUtils.defaultString(rs.getString("TEST_VALUATION_AVG_PERCENT")), 2));
            	rtnMap.put("TEST_VALUATION_PERCENT_SCORE", sishaGonyu(StringUtils.defaultString(rs.getString("TEST_VALUATION_PERCENT_SCORE")), 2));
            	rtnMap.put("MOCK_TOTAL_SCORE_GRADE_RANK", StringUtils.defaultString(rs.getString("MOCK_TOTAL_SCORE_GRADE_RANK"), "0"));
            	rtnMap.put("MOCK_TOTAL_SCORE_CLASS_RANK", StringUtils.defaultString(rs.getString("MOCK_TOTAL_SCORE_CLASS_RANK"), "0"));
            	rtnMap.put("MOCK_TOTAL_SCORE_COURSE_RANK", StringUtils.defaultString(rs.getString("MOCK_TOTAL_SCORE_COURSE_RANK"), "0"));
            	rtnMap.put("TOTAL_SCORE", sishaGonyu(StringUtils.defaultString(rs.getString("TOTAL_SCORE")), 2));
            	rtnMap.put("CNT", StringUtils.defaultString(rs.getString("CNT"), "0"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    //平均の取得
    private Map getAvgRecordRank (final DB2UDB db2, final String kbn, String value) {
        Map rtnMap = new HashMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS( ");
        stb.append("   SELECT DISTINCT ");
        stb.append("     YEAR, SCHREGNO ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append("   WHERE ");
        stb.append("       YEAR = '"+ _param._year +"' ");
        if("GRADE".equals(kbn)) {
            stb.append("   AND GRADE = '"+ value +"' ");
        } else if("CLASS".equals(kbn)) {
            stb.append("   AND GRADE || HR_CLASS = '"+ value +"' ");
        } else if("COURSE".equals(kbn)) {
            stb.append("   AND COURSECD || MAJORCD || COURSECODE = '"+ value +"' ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T2.CLASSCD ||'-'|| T2.SCHOOL_KIND ||'-'|| T2.CURRICULUM_CD ||'-'|| T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("   SUM(T2.SCORE) AS SCORE, ");
        stb.append("   AVG(T2.SCORE) AS SCORE_AVG ");
        stb.append(" FROM ");
        stb.append("   SCHNO T1 ");
        stb.append("   INNER JOIN RECORD_RANK_SDIV_DAT T2 ");
        stb.append("           ON T2.YEAR = T1.YEAR ");
        stb.append("          AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990009' ");
        stb.append("          AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN SUBCLASS_YDAT SUBY ");
        stb.append("           ON SUBY.YEAR          = T2.YEAR ");
        stb.append("          AND SUBY.SUBCLASSCD    = T2.SUBCLASSCD ");
        stb.append("          AND SUBY.CLASSCD       = T2.CLASSCD ");
        stb.append("          AND SUBY.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("          AND SUBY.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append(" GROUP BY ");
        stb.append("   T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
            	rtnMap.put(subclasscd, StringUtils.defaultString(rs.getString("SCORE_AVG")));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }


    //総合成績の平均取得
    private Map getAvgAftSchregRecommendationRank (final DB2UDB db2, final String kbn, String value) {
        Map rtnMap = new HashMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS( ");
        stb.append("   SELECT DISTINCT ");
        stb.append("     YEAR, SCHREGNO ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append("   WHERE ");
        stb.append("       YEAR = '"+ _param._year +"' ");
        if("GRADE".equals(kbn)) {
            stb.append("   AND GRADE = '"+ value +"' ");
        } else if("CLASS".equals(kbn)) {
            stb.append("   AND GRADE || HR_CLASS = '"+ value +"' ");
        } else if("COURSE".equals(kbn)) {
            stb.append("   AND COURSECD || MAJORCD || COURSECODE = '"+ value +"' ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T2.YEAR, ");
        stb.append("     AVG(T2.MOCK_NATIONAL_LANGUAGE_AVG) AS MOCK_NATIONAL_LANGUAGE_AVG, ");
        stb.append("     AVG(T2.MOCK_MATH_AVG) AS MOCK_MATH_AVG, ");
        stb.append("     AVG(T2.MOCK_ENGLISH_AVG) AS MOCK_ENGLISH_AVG, ");
        stb.append("     AVG(T2.TEST_VALUATION_AVG) AS TEST_VALUATION_AVG, ");
        stb.append("     AVG(T2.TEST_VALUATION_PERCENT_SCORE) AS TEST_VALUATION_PERCENT_SCORE, ");
        stb.append("     AVG(T2.MOCK_TOTAL_AVG) AS MOCK_TOTAL_AVG, ");
        stb.append("     AVG(T2.MOCK_TOTAL_PERCENT_SCORE) AS MOCK_TOTAL_PERCENT_SCORE, ");
        stb.append("     AVG(T2.TEST_VALUATION_AVG + T2.TEST_VALUATION_AVG_PERCENT + T2.TEST_VALUATION_PERCENT_SCORE) AS TOTAL_SCORE ");
        stb.append(" FROM ");
        stb.append("     SCHNO T1 ");
        stb.append("     INNER JOIN AFT_SCHREG_RECOMMENDATION_RANK_DAT T2 ");
        stb.append("           ON T2.YEAR = T1.YEAR ");
        stb.append("          AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" GROUP BY T2.YEAR ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

            	rtnMap.put("MOCK_NATIONAL_LANGUAGE_AVG", StringUtils.defaultString(rs.getString("MOCK_NATIONAL_LANGUAGE_AVG")));
            	rtnMap.put("MOCK_MATH_AVG", StringUtils.defaultString(rs.getString("MOCK_MATH_AVG")));
            	rtnMap.put("MOCK_ENGLISH_AVG", StringUtils.defaultString(rs.getString("MOCK_ENGLISH_AVG")));
            	rtnMap.put("TEST_VALUATION_AVG", StringUtils.defaultString(rs.getString("TEST_VALUATION_AVG")));
            	rtnMap.put("TEST_VALUATION_PERCENT_SCORE", StringUtils.defaultString(rs.getString("TEST_VALUATION_PERCENT_SCORE")));
            	rtnMap.put("MOCK_TOTAL_AVG", StringUtils.defaultString(rs.getString("MOCK_TOTAL_AVG")));
            	rtnMap.put("MOCK_TOTAL_PERCENT_SCORE", StringUtils.defaultString(rs.getString("MOCK_TOTAL_PERCENT_SCORE")));
            	rtnMap.put("TOTAL_SCORE", StringUtils.defaultString(rs.getString("TOTAL_SCORE")));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }


    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _trcd;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        String _course;
        String _majorname;
        String _coursename;
        String _hrClassName1;
        String _entyear;
        String _guard_zipcd;
        String _guard_addr1;
        String _guard_addr2;
        String _guard_name;
        String _communication;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, Map<String, SubclassAttendance>> _attendSubClassMap = new HashMap();
        final Map _totalScoreAvgMap = new TreeMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
        	log.info(" scoreSql = " + scoreSql);
            if (_param._isOutputDebug) {
            	log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                    	_printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname));
                    }
                    if (null == rs.getString("SEMESTER")) {
                    	continue;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    final String score = sishaGonyu(StringUtils.defaultString(rs.getString("SCORE")), 2);
                    final String avg = sishaGonyu(StringUtils.defaultString(rs.getString("AVG")), 2);
                    scoreData._scoreMap.put(testcd, score);
                	scoreData._avgMap.put(testcd, avg);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();

            final String[] sdivs = {SDIV9990009};
            final StringBuffer divStr = divStr("", sdivs);

            stb.append(" WITH SCHNO_A AS( ");
            //過年度含めた学籍の取得
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("             ON GDAT.YEAR  = REGD.YEAR ");
            stb.append("            AND GDAT.GRADE = REGD.GRADE ");
            stb.append(" WHERE ");
            stb.append("         REGD.YEAR     <= '" + _param._year + "'  ");
            stb.append("     AND REGD.SCHREGNO = '" + _schregno + "'  ");
            stb.append(" ), SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     SCHNO_A T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ");
            //講座の表
            stb.append(" ) , CHAIR_A AS( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     S1.SCHREGNO, ");
            stb.append("     S2.CLASSCD, ");
            stb.append("     S2.SCHOOL_KIND, ");
            stb.append("     S2.CURRICULUM_CD, ");
            stb.append("     S2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1 ");
            stb.append("     INNER JOIN CHAIR_DAT S2 ");
            stb.append("             ON S2.YEAR     = S1.YEAR ");
            stb.append("            AND S2.SEMESTER = S1.SEMESTER ");
            stb.append("            AND S2.CHAIRCD  = S1.CHAIRCD ");
            stb.append("            AND S2.SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("            AND S2.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) S3 ");
            stb.append("             ON S3.YEAR     = S1.YEAR ");
            stb.append("            AND S3.SCHREGNO = S1.SCHREGNO ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN ( SELECT DISTINCT YEAR, SCHREGNO FROM SCHNO ) T2 ");
            stb.append("             ON T2.YEAR     = T1.YEAR ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         (" + divStr + ") ");
            stb.append("         AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("         AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L2.SCORE ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("            ON SUBY.YEAR          = T1.YEAR ");
            stb.append("           AND SUBY.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND SUBY.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND SUBY.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND SUBY.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" ) ,RECORD_M AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , SUM(T1.SCORE) AS SCORE ");
            stb.append("            , AVG(T1.SCORE) AS AVG ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     GROUP BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD_M T1 ");
            stb.append("     INNER JOIN CHAIR_A T5 ");
            stb.append("        ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T5.SCHREGNO      = T1.SCHREGNO ");
            stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("      FROM RECORD_M T1 ");
            stb.append("      WHERE T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD,  ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV  ");

            return stb.toString();
        }

        /**
         * 学期+テスト種別のWHERE句を作成
         * @param tab テーブル別名
         * @param sdivs 学期+テスト種別
         * @return 作成した文字列
         */
		private StringBuffer divStr(final String tab, final String[] sdivs) {
			final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
            	final String semester = sdivs[i].substring(0, 1);
            	final String testkindcd = sdivs[i].substring(1, 3);
            	final String testitemcd = sdivs[i].substring(3, 5);
            	final String scorediv = sdivs[i].substring(5);
            	divStr.append(or).append(" " + tab + "SEMESTER = '" + semester + "' AND " + tab + "TESTKINDCD = '" + testkindcd + "' AND " + tab + "TESTITEMCD = '" + testitemcd + "' AND " + tab + "SCORE_DIV = '" + scorediv + "' ");
            	or = " OR ";
            }
            divStr.append(" ) ");
			return divStr;
		}

    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 平均点

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

		public String score(final String sdiv) {
			return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
		}

		public String avg(final String sdiv) {
			return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
		}

		public String toString() {
			return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
		}
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
    }


    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int abroad
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE")
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }


    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString(), 1)  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange) {
            log.info(" subclass attendance dateRange = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }

            for (final Iterator it2 = param._attendSemesterDetailList.iterator(); it2.hasNext();) {
            	final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                if (null == semesDetail) {
                    continue;
                }

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            dateRange._key,
                            dateRange._sdate,
                            dateRange._edate,
                            param._attendParamMap
                    );

                    ps = db2.prepareStatement(sql);

                    for (final Iterator it3 = studentList.iterator(); it3.hasNext();) {
                        final Student student = (Student) it3.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclasscd = rs.getString("SUBCLASSCD");

                            final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                            if (null == mst) {
                                log.warn("no subclass : " + subclasscd);
                                continue;
                            }
                            final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                            //if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                                final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                Map setSubAttendMap = null;

                                if (student._attendSubClassMap.containsKey(subclasscd)) {
                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                                } else {
                                    setSubAttendMap = new TreeMap();
                                }

//                                setMap.put(semesDetail._cdSemesterDetail, subclassAttendance);

                                setSubAttendMap.put(dateRange._key, subclassAttendance);

                                student._attendSubClassMap.put(subclasscd, setSubAttendMap);
                            }

                        }

                        DbUtils.closeQuietly(rs);
                    }

                } catch (Exception e) {
                    log.fatal("exception!", e);
                } finally {
                    DbUtils.closeQuietly(ps);
                    db2.commit();
                }
            }
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
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }
    }


    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
        	if (!(o instanceof Semester)) {
        		return 0;
        	}
        	Semester s = (Semester) o;
        	return _semester.compareTo(s._semester);
        }
    }


    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
        	return null != _combined;
        }
        public boolean isSaki() {
        	return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }


    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }


    private static class SemesterDetail implements Comparable<SemesterDetail> {
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
        public int compareTo(final SemesterDetail sd) {
    		int rtn;
        	rtn = _semester.compareTo(sd._semester);
        	if (rtn != 0) {
        		return rtn;
        	}
        	rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
        	return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
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
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
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
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 75874 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {

    	final String _prgid;
        final String _year;
        final String _semester;
    	final String _titlediv;
        final String _output;
        final String[] _categorySelected;
        final String _logintData;
        final String _grade;
        final String _nendo;
        final String _date;
        final String _edate;
        final String _schoolKind;
        final String _schoolKindName;
        private final Map _testItemMap;

        final Map _stampMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private Map<String, Map<String, String>> _creditMstMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto; //元科目の表示  TRUE：表示しない  FALSE：表示する
        private boolean _isPrintSakiKamoku; //先科目の表示  TRUE：表示する  FALSE：表示しない

        private boolean _isOutputDebug;

        private final List _attendTestKindItemList;
        private final List _attendSemesterDetailList;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_titlediv = request.getParameter("TITLEDIV");
        	_output = request.getParameter("OUTPUT"); //1:学級別  2:コース別
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _logintData = request.getParameter("LOGIN_DATE").replace('/', '-');
            _grade = request.getParameter("GRADE");
            _year = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _date = getSemesterMst(db2, SEMEALL, "SDATE"); //年度開始日
            _edate = getSemesterMst(db2, SEMEALL, "EDATE"); //年度終了日
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
            _stampMap = getStampNoMap(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);
            setCreditMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJH442A' AND NAME = '" + propName + "' "));
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _year + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
            	final StringBuffer stb = new StringBuffer();
            	stb.append(" WITH SCHNO AS( ");
            	stb.append("   SELECT DISTINCT ");
            	stb.append("     YEAR, SCHREGNO ");
            	stb.append("   FROM ");
            	stb.append("     SCHREG_REGD_DAT ");
            	stb.append("   WHERE ");
            	stb.append("         YEAR = '"+ _year +"' ");
                if("1".equals(_output)) {
                    stb.append(" AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected));
                } else {
                    stb.append(" AND COURSECD || MAJORCD || COURSECODE IN " + SQLUtils.whereIn(true, _categorySelected));
                }
            	stb.append(" ), RECORD AS( ");
            	stb.append("   SELECT DISTINCT ");
            	stb.append("     T2.CLASSCD, ");
            	stb.append("     T2.SCHOOL_KIND, ");
            	stb.append("     T2.CURRICULUM_CD, ");
            	stb.append("     T2.SUBCLASSCD ");
            	stb.append("   FROM ");
            	stb.append("     SCHNO T1 ");
            	stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT T2 ");
            	stb.append("             ON T2.YEAR = T1.YEAR ");
            	stb.append("            AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990009' ");
            	stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
            	stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            	stb.append("             ON SUBY.YEAR          = T2.YEAR ");
            	stb.append("            AND SUBY.SUBCLASSCD    = T2.SUBCLASSCD ");
            	stb.append("            AND SUBY.CLASSCD       = T2.CLASSCD ");
            	stb.append("            AND SUBY.SCHOOL_KIND   = T2.SCHOOL_KIND ");
            	stb.append("            AND SUBY.CURRICULUM_CD = T2.CURRICULUM_CD ");
            	stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ");
                stb.append("   T1.CLASSCD, ");
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   T2.CLASSABBV, ");
                stb.append("   VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, ");
                stb.append("   T1.SUBCLASSABBV, ");
                stb.append("   VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("   COMB1.CALCULATE_CREDIT_FLG, ");
                stb.append("   VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ");
                stb.append("   VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ");;
                stb.append("   ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ");
                stb.append("   COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append("      LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("      LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + _year + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("      LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + _year + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            	stb.append("      INNER JOIN RECORD R1 ");
            	stb.append("              ON R1.SUBCLASSCD    = T1.SUBCLASSCD ");
            	stb.append("             AND R1.CLASSCD       = T1.CLASSCD ");
            	stb.append("             AND R1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            	stb.append("             AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                		final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                		_subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                	}
                	final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD")); //合併先
                	if (null != combined) {
                		final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                		mst._combined = combined;
                	}
                	final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD")); //合併元
                	if (null != attend) {
                		final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                		mst._attendSubclassList.add(attend);
                	}
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getCredits(final Student student, final String subclasscd) {
        	final String regdKey = student._coursecd + student._majorcd + student._grade + student._coursecode;
        	final Map<String, String> subclasscdCreditMap = _creditMstMap.get(regdKey);
			if (null == subclasscdCreditMap) {
        		return null;
        	}
        	final String credits = subclasscdCreditMap.get(subclasscd);
        	if (!subclasscdCreditMap.containsKey(subclasscd)) {
        		log.info(" no credit_mst : " + subclasscd);
        	}
			return credits;
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.COURSECD || T1.MAJORCD || T1.GRADE || T1.COURSECODE AS REGD_KEY, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T1.CREDITS ";
                sql += " FROM CREDIT_MST T1 ";
                sql += " WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String regdKey = rs.getString("REGD_KEY");
					if (!_creditMstMap.containsKey(regdKey)) {
                		_creditMstMap.put(regdKey, new TreeMap());
                	}
                	_creditMstMap.get(regdKey).put(rs.getString("SUBCLASSCD"), rs.getString("CREDITS"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map settestItemMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTITEM, TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _year + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String testitem = rs.getString("TESTITEM");
                    final String testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                    if (!map.containsKey(testitem)) {
                        map.put(testitem, testitemname);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
        	final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }


        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public String getStaffImageFilePath(final String staffCd) {
            final String stampNo = (String) _stampMap.get(staffCd);
            final String path = _documentroot + "/image/stamp/" + stampNo + ".bmp";
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
        	// 初期値：表示する
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
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
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
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

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
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
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
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
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = SDIV9990009.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
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

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._semester.equals(SEMEALL)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, SEMEALL, "学年", semester9._dateRange._sdate, semester9._dateRange._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(SEMEALL, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }


        //学期情報の取得
        private String getSemesterMst(DB2UDB db2, final String semester, final String column) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
            	final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   * ");
                stb.append(" FROM ");
                stb.append("   SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ _year +"' ");
                stb.append("   AND SEMESTER = '"+ semester +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString(column));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

    }
}

// eof