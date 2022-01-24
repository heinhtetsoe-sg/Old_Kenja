/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/01/25
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD627I {

    private static final Log log = LogFactory.getLog(KNJD627I.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV010101 = "010101"; //中間
    private static final String SDIV990008 = "990008"; //期末

    private static final String CHUKAN1  = "1-010101"; //1学期中間
    private static final String KIMATSU1 = "1-990008"; //1学期期末
    private static final String CHUKAN2  = "2-010101"; //2学期中間
    private static final String KIMATSU2 = "2-990008"; //2学期期末
    private static final String KIMATSU3 = "3-990008"; //3学期期末
    private static final String KIMATSU9 = "9-990008"; //学年末

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

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

        if(studentList.isEmpty()) return;
        if(_param._iqShow) {
        	svf.VrSetForm("KNJD627I.frm" , 4);
        } else {
        	svf.VrSetForm("KNJD627I.frm" , 1);
        }

        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            //明細部以外を印字
            printTitle(svf, student);

            final String semester[] = {"1", "2", "3", "9"};
            printMeisai(svf, student, student._printSubclassMap.values(), semester);

            //出欠の記録
            if(student._year1 != null) {
            	printAttend(svf, student, student._year1, "1", semester);
            }
            if(student._year2 != null) {
            	printAttend(svf, student, student._year2, "2", semester);
            }
            if(student._year3 != null) {
            	printAttend(svf, student, student._year3, "3", semester);
            }

            if(_param._iqShow) {
            	svf.VrsOut("RANK_NAME","入試序列");
            	svf.VrsOut("EXAM_RANK", KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT REMARK1 FROM ENTEXAM_RECEPT_DETAIL_DAT WHERE ENTEXAMYEAR = '" + student._entYear + "' AND APPLICANTDIV = '1' AND TESTDIV = '" + student._testdiv + "' AND SEQ = '015' AND RECEPTNO = '" + student._examno + "'")));
            	svf.VrEndRecord();
            	svf.VrsOut("RANK_NAME","NX-IQ");
                svf.VrsOut("EXAM_RANK", KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT IQ FROM SCHREG_IQ_DAT WHERE YEAR = '" + student._entYear + "' AND SCHREGNO = '" + student._schregno + "'")));
            	svf.VrEndRecord();
        	}
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private String add(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private void printMeisai(final Vrw32alp svf, final Student student, final Collection<Map<String, String>> subclasses, final String[] semester) {
    	int retsu = 1;

    	//講座毎のループ
        for(final Map<String, String> printSubclassMap : subclasses) {

        	svf.VrsOutn("SUBCLASS_NAME1", retsu, printSubclassMap.get("SUBCLASSABBV"));

        	final String subclassKey = printSubclassMap.get("SUBCLASSKEY");

        	if(student._year1 != null) {
        		printScore(svf, student._scoreMap, student._year1,semester, subclassKey, retsu, "1");
        	}
        	if(student._year2 != null) {
        		printScore(svf, student._scoreMap, student._year2,semester, subclassKey, retsu, "2");
        	}
        	if(student._year3 != null) {
        		printScore(svf, student._scoreMap, student._year3,semester, subclassKey, retsu, "3");
        	}

        	retsu++;
        }
    }

    private void printScore(final Vrw32alp svf, final Map scoreMap, final String year, final String[] semester, final String subclassKey, final int retsu, final String gradeField) {
    	int gyo = 1;
    	final String testcd[] = {CHUKAN1,KIMATSU1,CHUKAN2,KIMATSU2,KIMATSU3,KIMATSU9};
    	for(String cd : testcd) {
    		final String key = year + "-" + cd + "-" + subclassKey;
    		final Score score = (Score)scoreMap.get(key);
    			if(score != null && !"*".equals(score._score)) {
    				final String field;
    				if(SEMEALL.equals(cd.substring(0,1))) {
    					field = "SCORE" + gradeField + "_" + SEMEALL;
    				} else {
    					field = "SCORE" + gradeField + "_" + gyo;
    				}
    				if("1".equals(score._passflg)) {
    					svf.VrsOutn(field, retsu, score._score);
    				} else { //0:54点以下網掛け
    					svf.VrAttributen(field, retsu, "Paint=(9,70,2),Bold=1");
    					svf.VrsOutn(field, retsu, score._score);
    					svf.VrAttributen(field, retsu, "Paint=(0,0,0),Bold=0");
    				}
    			}
    		gyo++;
    	}

        //総点、平均点、序列
    	if("1".equals(gradeField)) {
    		gyo = 1;
    	} else if("2".equals(gradeField)) {
    		gyo = 7;
    	} else {
    		gyo = 13;
    	}
        for(String cd : testcd) {
        	final String scoreKey = year + "-" + cd + "-" + "99-J-99-999999";
        	if (scoreMap.containsKey(scoreKey)) {
        		final Score score = (Score) scoreMap.get(scoreKey);
        		if(score != null) {
                	svf.VrsOutn("TOTAL_SCORE1", gyo, score._score);
        			svf.VrsOutn("TOTAL_RANK1", gyo, score._grade_Rank);

        			if("1".equals(score._avgPassFlg)) {
        				svf.VrsOutn("TOTAL_AVE1", gyo, sishaGonyu(score._avg));
    				} else { //0:64.4点以下網掛け
    					svf.VrAttributen("TOTAL_AVE1", gyo, "Paint=(9,70,2),Bold=1");
    					svf.VrsOutn("TOTAL_AVE1", gyo, sishaGonyu(score._avg));
    					svf.VrAttributen("TOTAL_AVE1", gyo, "Paint=(0,0,0),Bold=0");
    				}
        		}
        	}
        	gyo++;
        }
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
    	final String sex = "1".equals(student._sex) ? "男" : "女";
    	final String name = student._name + " (" + sex + ")";
    	final int keta = KNJ_EditEdit.getMS932ByteLength(name);
    	final String field = keta <= 26 ? "1" : keta <= 34 ? "2" : "3";
    	svf.VrsOut("NAME" + field, name);
    	svf.VrsOut("KANA", student._kana);
    	svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_SeirekiJP(student._birthday));
    	svf.VrsOut("FINSCHOOL_NAME", student._finSchoolName);
    	svf.VrsOut("SCHREG_NO", student._schregno);
    	svf.VrsOut("ENT_YEAR", student._entYear + "年");
    	if(student._year1 != null) {
    		printGrade(svf, "1", student._hrclassName1, student._attendNo1, student._staffName1);
    	}
    	if(student._year2 != null) {
    		printGrade(svf, "2", student._hrclassName2, student._attendNo2, student._staffName2);
    	}
    	if(student._year3 != null) {
    		printGrade(svf, "3", student._hrclassName3, student._attendNo3, student._staffName3);
    	}
    }

    private void printGrade(final Vrw32alp svf, final String gradeField, final String hrclassName, final String attendNo, final String staffName) {
    	svf.VrsOutn("GRADE", Integer.valueOf(gradeField), gradeField);
    	svf.VrsOutn("HR", Integer.valueOf(gradeField), hrclassName);
    	svf.VrsOutn("ATTEND_NO", Integer.valueOf(gradeField), Integer.valueOf(attendNo).toString());
        final int keta = KNJ_EditEdit.getMS932ByteLength(staffName);
        final String field = keta <= 24 ? "1" : keta <= 30 ? "2" : "3";
        svf.VrsOutn("TR_NAME" + field, Integer.valueOf(gradeField), staffName);
    }

    private void printAttend(final Vrw32alp svf, final Student student, final String year, final String gradeField, final String[] semester) {
    	int gyo;
    	if("1".equals(gradeField)) {
    		gyo = 1;
    	} else if("2".equals(gradeField)) {
    		gyo = 5;
    	} else {
    		gyo = 9;
    	}
        for(String seme : semester) {
        	final Map attendMap = (Map)student._attendMap.get(year + "-" + seme);
        	if(attendMap != null) {
            	svf.VrsOutn("LESSON1", gyo, "0".equals(attendMap.get("LESSON").toString()) ? "" : attendMap.get("LESSON").toString());
            	svf.VrsOutn("SUSPEND", gyo,"0".equals(attendMap.get("SUSPEND").toString()) ? "" : attendMap.get("SUSPEND").toString());
            	svf.VrsOutn("MUST", gyo,"0".equals(attendMap.get("MLESSON").toString()) ? "" : attendMap.get("MLESSON").toString());
            	svf.VrsOutn("ABSENCE1", gyo, "0".equals(attendMap.get("SICK").toString()) ? "" : attendMap.get("SICK").toString());
            	svf.VrsOutn("PRESENT1", gyo, "0".equals(attendMap.get("PRESENT").toString()) ? "" : attendMap.get("PRESENT").toString());
            	svf.VrsOutn("LATE1", gyo, "0".equals(attendMap.get("LATE").toString()) ? "" : attendMap.get("LATE").toString());
            	svf.VrsOutn("EARLY", gyo, "0".equals(attendMap.get("EARLY").toString()) ? "" : attendMap.get("EARLY").toString());
            	svf.VrsOutn("KEKKA1", gyo, "0".equals(attendMap.get("M_KEKKA_JISU").toString()) ? "" : attendMap.get("M_KEKKA_JISU").toString());

            	String dosuuTotal = "0";
            	dosuuTotal = add(dosuuTotal, attendMap.get("SICK").toString());
            	dosuuTotal = add(dosuuTotal, attendMap.get("LATE").toString());
            	dosuuTotal = add(dosuuTotal, attendMap.get("EARLY").toString());
            	dosuuTotal = add(dosuuTotal, attendMap.get("M_KEKKA_JISU").toString());
            	svf.VrsOutn("ATTEND_TOTAL", gyo, dosuuTotal);
        	}
        	gyo++;
        }
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
                student._sex = rs.getString("SEX");
                student._kana = rs.getString("NAME_KANA");
                student._birthday = rs.getString("BIRTHDAY");
                student._entYear = rs.getString("ENT_YEAR");
                student._finSchoolName = rs.getString("FINSCHOOL_NAME");
                student._examno = rs.getString("BASE_REMARK1");
                student._testdiv = rs.getString("BASE_REMARK2");
                student.setGradeHrclass(db2);

                final String date = _param._date;
                final List yearList = new ArrayList(); //対象年度

                //各学年の出欠、科目データ取得
                if(student._year1 != null) {
                	student.loadAttendance(db2, _param, student._year1, SEMEALL, date, new HashMap(_param._attendParamMap));
                	student.setSubclass(db2, student._year1);
                	yearList.add(student._year1);
                }
                if(student._year2 != null) {
                	student.loadAttendance(db2, _param, student._year2, SEMEALL, date, new HashMap(_param._attendParamMap));
                	student.setSubclass(db2, student._year2);
                	yearList.add(student._year2);
                }
                if(student._year3 != null) {
                	student.loadAttendance(db2, _param, student._year3, SEMEALL, date, new HashMap(_param._attendParamMap));
                	student.setSubclass(db2, student._year3);
                	yearList.add(student._year3);
                }
                student.setScore(db2, yearList);
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
        //選択された年組の生徒
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     T2.SEX, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     LEFT(T2.ENT_DATE,4) ENT_YEAR, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME, ");
        stb.append("     BDTL.BASE_REMARK1, ");
        stb.append("     BDTL.BASE_REMARK2 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST T2 ");
        stb.append("      ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ");
        stb.append("      ON SCHOOL.FINSCHOOLCD = T2.FINSCHOOLCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_BASE_DETAIL_MST BDTL ");
        stb.append("      ON BDTL.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND BDTL.BASE_SEQ = '003' ");
        stb.append(" WHERE   T1.YEAR     = '" + _param._loginYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        stb.append("     AND T1.GRADE    = '" + _param._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + _param._hrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._studentsSelected));
        stb.append(" ORDER BY T1.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
    	String _schregno;
        String _name;
        String _kana;
        String _sex;
        String _year1;
        String _year2;
        String _year3;
        String _grade1;
        String _grade2;
        String _grade3;
        String _gradeCd1;
        String _gradeCd2;
        String _gradeCd3;
        String _hrclass1;
        String _hrclass2;
        String _hrclass3;
        String _hrclassName1;
        String _hrclassName2;
        String _hrclassName3;
        String _attendNo1;
        String _attendNo2;
        String _attendNo3;
        String _entYear;
        String _staffName1;
        String _staffName2;
        String _staffName3;
        String _birthday;
        String _finSchoolName;
        String _hrClass;
        String _examno;
        String _testdiv;
        final Map _attendMap = new TreeMap();
        final Map _attendSubclassMap = new TreeMap();
        final Map<String, Map<String, String>> _printSubclassMap = new LinkedHashMap();
        final Map _scoreMap = new TreeMap();

        private void setGradeHrclass(final DB2UDB db2) {
        	final String ghrSql = getGradeHrclassSql();
            log.debug(" ghrSql = " + ghrSql);
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(ghrSql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String hrclassName = rs.getString("HR_CLASS_NAME1");
                    final String staffName = rs.getString("STAFFNAME");

                    if("01".equals(gradeCd)) {
                    	_year1 = year;
                    	_grade1 = grade;
                    	_gradeCd1 = gradeCd;
                    	_hrclass1 = hrclass;
                    	_attendNo1 = attendNo;
                    	_hrclassName1 = hrclassName;
                    	_staffName1 = staffName;
                    } else if("02".equals(gradeCd)) {
                    	_year2 = year;
                    	_grade2 = grade;
                    	_gradeCd2 = gradeCd;
                    	_hrclass2 = hrclass;
                    	_attendNo2 = attendNo;
                    	_hrclassName2 = hrclassName;
                    	_staffName2 = staffName;
                    } else if("03".equals(gradeCd)) {
                    	_year3 = year;
                    	_grade3 = grade;
                    	_gradeCd3 = gradeCd;
                    	_hrclass3 = hrclass;
                    	_attendNo3 = attendNo;
                    	_hrclassName3 = hrclassName;
                    	_staffName3 = staffName;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getGradeHrclassSql() {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" WITH SCHNO_HIST_A AS( ");
        	stb.append("     SELECT DISTINCT ");
        	stb.append("         REGD.YEAR, ");
        	stb.append("         REGD.SEMESTER, ");
        	stb.append("         REGD.SCHREGNO, ");
        	stb.append("         REGD.GRADE, ");
        	stb.append("         GDAT.GRADE_CD, ");
        	stb.append("         REGD.HR_CLASS, ");
        	stb.append("         REGD.ATTENDNO ");
        	stb.append("     FROM ");
        	stb.append("         SCHREG_REGD_DAT REGD ");
        	stb.append("     INNER JOIN ");
        	stb.append("         SCHREG_REGD_GDAT GDAT ");
        	stb.append("          ON GDAT.YEAR  = REGD.YEAR ");
        	stb.append("         AND GDAT.GRADE = REGD.GRADE ");
        	stb.append("     WHERE ");
        	stb.append("         REGD.YEAR <= '" + _param._loginYear + "' ");
        	stb.append("         AND REGD.SCHREGNO = '" + _schregno + "'     ");
        	stb.append(" ) , SCHNO_HIST AS( ");
        	stb.append("     SELECT  ");
        	stb.append("         T1.* ");
        	stb.append("     FROM    SCHNO_HIST_A T1 ");
        	stb.append("     WHERE   T1.YEAR || T1.SEMESTER = (SELECT MAX(YEAR) || MAX(SEMESTER) FROM SCHNO_HIST_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ");
        	stb.append(" ) ");
        	stb.append("     SELECT ");
        	stb.append("         T6.YEAR, ");
        	stb.append("         T6.GRADE, ");
        	stb.append("         T6.GRADE_CD, ");
        	stb.append("         T6.HR_CLASS, ");
        	stb.append("         T6.ATTENDNO, ");
        	stb.append("         REGDH.HR_CLASS_NAME1, ");
        	stb.append("         STF.STAFFNAME ");
        	stb.append("     FROM ");
        	stb.append("         SCHNO_HIST T6 ");
        	stb.append("     LEFT JOIN ");
        	stb.append("         SCHREG_REGD_HDAT REGDH ");
        	stb.append("          ON REGDH.YEAR = T6.YEAR ");
        	stb.append("         AND REGDH.SEMESTER = T6.SEMESTER ");
        	stb.append("         AND REGDH.GRADE = T6.GRADE ");
        	stb.append("         AND REGDH.HR_CLASS = T6.HR_CLASS ");
        	stb.append("     LEFT JOIN ");
        	stb.append("         STAFF_MST STF ");
        	stb.append("          ON STF.STAFFCD = REGDH.TR_CD1 ");
        	stb.append("     ORDER BY ");
        	stb.append("         T6.GRADE ");

        	return stb.toString();
        }

        private String setSubclassSql(final String year) {

        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT DISTINCT ");
        	stb.append("     STD.YEAR, ");
        	stb.append("     STD.CHAIRCD, ");
        	stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSKEY, ");
        	stb.append("     SUBCLASS.SUBCLASSABBV, ");
        	stb.append("     CASE WHEN SAKI.YEAR IS NOT NULL THEN '1' END AS SAKI, ");
        	stb.append("     CASE WHEN MOTO.YEAR IS NOT NULL THEN '1' END AS MOTO, ");
        	stb.append("     VALUE(CLASS.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ");
        	stb.append("     VALUE(SUBCLASS.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ");
        	stb.append(" FROM ");
        	stb.append("     CHAIR_STD_DAT STD ");
        	stb.append(" INNER JOIN ");
        	stb.append("     CHAIR_DAT CHAIR ");
        	stb.append("      ON CHAIR.YEAR = STD.YEAR ");
        	stb.append("     AND CHAIR.SEMESTER = STD.SEMESTER ");
        	stb.append("     AND CHAIR.CHAIRCD = STD.CHAIRCD ");
        	stb.append("     AND CHAIR.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
        	stb.append(" INNER JOIN ");
        	stb.append("     SUBCLASS_MST SUBCLASS ");
        	stb.append("      ON SUBCLASS.CLASSCD = CHAIR.CLASSCD ");
        	stb.append("     AND SUBCLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
        	stb.append("     AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        	stb.append("     AND SUBCLASS.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        	stb.append(" INNER JOIN ");
        	stb.append("     CLASS_MST CLASS ");
        	stb.append("      ON CLASS.CLASSCD = CHAIR.CLASSCD ");
        	stb.append("     AND CLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
        	stb.append(" LEFT JOIN ");
        	stb.append("     SUBCLASS_REPLACE_COMBINED_DAT SAKI ");
        	stb.append("      ON SAKI.YEAR = STD.YEAR ");
        	stb.append("     AND SAKI.COMBINED_CLASSCD = SUBCLASS.CLASSCD ");
        	stb.append("     AND SAKI.COMBINED_SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
        	stb.append("     AND SAKI.COMBINED_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
        	stb.append("     AND SAKI.COMBINED_SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
        	stb.append(" LEFT JOIN ");
        	stb.append("     SUBCLASS_REPLACE_COMBINED_DAT MOTO ");
        	stb.append("      ON MOTO.YEAR = STD.YEAR ");
        	stb.append("     AND MOTO.ATTEND_CLASSCD = SUBCLASS.CLASSCD ");
        	stb.append("     AND MOTO.ATTEND_SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
        	stb.append("     AND MOTO.ATTEND_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
        	stb.append("     AND MOTO.ATTEND_SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
        	stb.append(" WHERE ");
        	stb.append("     STD.SCHREGNO = '" + _schregno + "' ");
        	stb.append("     AND STD.YEAR = '" + year + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("     VALUE(CLASS.SHOWORDER3, 999), VALUE(SUBCLASS.SHOWORDER3, 999), STD.YEAR, SUBCLASSKEY ");

        	return stb.toString();
        }

        private void setScore(final DB2UDB db2, final List yearList) {
            PreparedStatement ps = null;
            ResultSet rs = null;

			try {
				final String sql = setScoreSql(yearList);
				ps = db2.prepareStatement(sql);
				rs = ps.executeQuery();

				while(rs.next()) {
					final String year = rs.getString("YEAR");
					final String semester = rs.getString("SEMESTER");
					final String testkindcd = rs.getString("TESTKINDCD");
					final String testitemcd = rs.getString("TESTITEMCD");
					final String score_Div = rs.getString("SCORE_DIV");
					final String classcd = rs.getString("CLASSCD");
					final String school_Kind = rs.getString("SCHOOL_KIND");
					final String curriculum_Cd = rs.getString("CURRICULUM_CD");
					final String subclasscd = rs.getString("SUBCLASSCD");
					final String score = rs.getString("SCORE");
					final String passflg = rs.getString("PASSFLG");
					final String grade_Rank = rs.getString("GRADE_RANK");
					final String avg = rs.getString("AVG");
					final String avgPassFlg = rs.getString("AVG_PASSFLG");
					final String key = year + "-" + semester + "-" + testkindcd + testitemcd + score_Div + "-" + classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;

					if(!_scoreMap.containsKey(key)) {
						final Score wk = new Score(score, passflg, grade_Rank, avg, avgPassFlg);
						_scoreMap.put(key, wk);
					}
				}

			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(ps);
				db2.commit();
			}
        }

        private String setScoreSql(final List yearList) {

        	final String[] year = (String[])yearList.toArray(new String[yearList.size()]);
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("     RANK.YEAR, ");
        	stb.append("     RANK.SEMESTER, ");
        	stb.append("     RANK.TESTKINDCD, ");
        	stb.append("     RANK.TESTITEMCD, ");
        	stb.append("     RANK.SCORE_DIV, ");
        	stb.append("     RANK.CLASSCD, ");
        	stb.append("     RANK.SCHOOL_KIND, ");
        	stb.append("     RANK.CURRICULUM_CD, ");
        	stb.append("     RANK.SUBCLASSCD, ");
        	stb.append("     RANK.SCORE, ");
        	stb.append("     CASE WHEN 54 >= RANK.SCORE THEN '0' ELSE '1' END AS PASSFLG, ");
        	stb.append("     RANK.GRADE_RANK, ");
        	stb.append("     RANK.AVG, ");
        	stb.append("     CASE WHEN 64.4 >= RANK.AVG THEN '0' ELSE '1' END AS AVG_PASSFLG ");
        	stb.append(" FROM ");
        	stb.append("     RECORD_RANK_SDIV_DAT RANK ");
        	stb.append(" WHERE ");
        	stb.append("     RANK.SCHREGNO = '" + _schregno + "' ");
        	stb.append("     AND RANK.YEAR IN " +  SQLUtils.whereIn(true, year) + " ");
        	stb.append("     AND (RANK.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' OR RANK.SUBCLASSCD = '" + ALL9 + "') ");
        	stb.append("     AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV IN ('" + SDIV010101 + "','" + SDIV990008 + "') ");
        	stb.append("     AND RANK.SUBCLASSCD NOT IN ('" + ALL3 + "','" + ALL5 + "') ");
        	stb.append("     AND RANK.SCHOOL_KIND = 'J' ");

        	return stb.toString();
        }

        private void loadAttendance(
                final DB2UDB db2,
                final Param param,
                final String year,
                final String semester,
                final String date,
                final Map attendParamMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = AttendAccumulate.getAttendSemesSql(year, semester, null, date, attendParamMap);
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

				ps.setString(1, _schregno);
				rs = ps.executeQuery();

				while (rs.next()) {
					final Map semes = getMappedMap(_attendMap, year + "-" + rs.getString("SEMESTER"));

					semes.put("LESSON", rs.getString("LESSON"));
					semes.put("MLESSON", rs.getString("MLESSON"));
					semes.put("SUSPEND", rs.getString("SUSPEND"));
					semes.put("MOURNING", rs.getString("MOURNING"));
					semes.put("SICK_ONLY", rs.getString("SICK_ONLY"));
					semes.put("NOTICE_ONLY", rs.getString("NOTICE_ONLY"));
					semes.put("SICK_NOTICE", add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")));
					final String putWk = add(add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")),
							rs.getString("NONOTICE_ONLY"));
					semes.put("SICK", putWk);
					final String putWk2 = add(rs.getString("SUSPEND"), rs.getString("MOURNING"));
					semes.put("SUSPEND", putWk2);
					semes.put("PRESENT", rs.getString("PRESENT"));
					semes.put("VIRUS", rs.getString("VIRUS"));
					semes.put("LATE", rs.getString("LATE"));
					semes.put("EARLY", rs.getString("EARLY"));
					semes.put("M_KEKKA_JISU", rs.getString("M_KEKKA_JISU"));
				}
				DbUtils.closeQuietly(rs);
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(ps);
				db2.commit();
			}
        }

        private void setSubclass(final DB2UDB db2, final String year) {
        	PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = setSubclassSql(year);
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);
				rs = ps.executeQuery();

				while (rs.next()) {

					final Map subclass;
					if(_printSubclassMap.containsKey(rs.getString("SUBCLASSKEY"))) {
						continue;
					} else {
						_printSubclassMap.put(rs.getString("SUBCLASSKEY"), new TreeMap());
					}
					subclass = (Map)_printSubclassMap.get(rs.getString("SUBCLASSKEY"));

					subclass.put("SUBCLASSKEY", rs.getString("SUBCLASSKEY"));
					subclass.put("SUBCLASSABBV", rs.getString("SUBCLASSABBV"));
					subclass.put("SAKI", rs.getString("SAKI"));
					subclass.put("MOTO", rs.getString("MOTO"));
				}
				DbUtils.closeQuietly(rs);
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(ps);
				db2.commit();
			}
        }
    }

    private class Score {
    	final String _score;
    	final String _passflg;
    	final String _grade_Rank;
    	final String _avg;
    	final String _avgPassFlg;

    	public Score(final String score, final String passflg, final String grade_Rank, final String avg, final String avgPassFlg) {
    		_score = score;
    		_passflg = passflg;
    		_grade_Rank = grade_Rank;
    		_avg = avg;
    		_avgPassFlg = avgPassFlg;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Id$");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _studentsSelected;
        final String _grade;
        final String _semester;
        final String _hrclass;
        final String _prgid;
        final String _loginSemester;
        final String _loginYear;
        final String _date;
        final String _schoolCd;
        final String _documentRoot;
        final String _printKind;
        final boolean _iqShow;

        private boolean _isOutputDebug;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _studentsSelected = request.getParameterValues("STUDENTS_SELECTED");
            _grade = request.getParameter("HID_GRADE");
            _hrclass = request.getParameter("HID_HR_CLASS");
            _semester = request.getParameter("HID_SEMESTER");
            _prgid = request.getParameter("PRGID");
            _loginSemester = request.getParameter("HID_SEMESTER");
            _loginYear = request.getParameter("HID_YEAR");
            _date = request.getParameter("LOGIN_DATE").replace('/', '-');
            _printKind = request.getParameter("PRINT_TARGET_KIND");
            _schoolCd = request.getParameter("HID_SCHOOLCD");
            _iqShow = "1".equals(request.getParameter("INCLUDE_RANK_AND_IQ"));
            _documentRoot = request.getParameter("DOCUMENTROOT");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "1");
        }

    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }
}

// eof
