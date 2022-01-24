package servletpack.KNJH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 実力テスト　成績一覧
 * $Id: 5fa39b21d5fb1f1f623f622152ff532496d1ae49 $
 */
public class KNJH568 {

    private static final Log log = LogFactory.getLog(KNJH568.class);

    private boolean _hasData;
    private Param _param;

    /** 合計の科目コード */
    private static final String SUBCLASSCD_ALL3 = "333333";
    private static final String SUBCLASSCD_ALL5 = "555555";
    private static final String SUBCLASSCD_ALL9 = "999999";

    /**   : クラス */
    private static final int SELECT_DIV_HR = 1;
    /**   : コース */
    private static final int SELECT_DIV_COURSE = 2;
    /**   : 学年 */
    private static final int SELECT_DIV_GRADE = 3;

    private static final String RANK_DATA_DIV01_SOGOTEN = "01"; // 総合点
    private static final String RANK_DIV_SCORE = "01";

    /**
     * KNJH.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 1);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != svf) {
                svf.VrQuit();
            }
        }
    }

    /**
     * 印刷処理メイン
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        for (int i = 0; i < _param._categorySelected.length; i++) {
        	final StudentGroup group = StudentGroup.createStudentGroups(db2, _param, _param._categorySelected[i]);
            if (group._studentList.size() == 0) {
                continue;
            }
            group.load(db2, _param);
            
            Collections.sort(group._studentList, new Student.StudentComparator(_param));
            for (final Iterator it = group._studentList.iterator(); it.hasNext();) {
            	final Student student = (Student) it.next();
            	final SubclassAll all = student._all9;
            	final RankDev rankDev = SELECT_DIV_HR == _param._selectDiv ? all._hr : SELECT_DIV_COURSE == _param._selectDiv ? all._course : all._grade;
            	if (!NumberUtils.isDigits(rankDev._rank) || Integer.parseInt(rankDev._rank) > _param._printCount) {
            		it.remove();
            	}
            }

	        final int MAX_LINE = 40;
	        final int MAX_SUBCLASS = 5;
	
	        final List studentPageList = getPageList(group._studentList, MAX_LINE);
	        final List subclassPageList = getPageList(group._subclassList, MAX_SUBCLASS);
	        
	        for (int page = 0; page < studentPageList.size(); page++) {
	
	            final List pageStudentList = (List) studentPageList.get(page);
	
	            for (int subcPage = 0; subcPage < subclassPageList.size(); subcPage++) {
	            	final List pageSubclassList = (List) subclassPageList.get(subcPage);
	
	                final String form = "KNJH568.frm";
	                svf.VrSetForm(form, 4);
	                
	                String title = "";
	                title += _param.getNendo();
	                title += " " + _param._semesterName;
	                title += " " + _param._testname;
	                title += " " + _param.getSelectDivName();
	                title += " " + _param.getSelectName(group);
	                svf.VrsOut("TITLE", title); // タイトル

	                svf.VrsOut("RANK_NAME" + (KNJ_EditEdit.getMS932ByteLength(_param.getSelectDivName()) <= 8 ? "1" : "2"), _param.getSelectDivName());

	                // 科目名設定
	                for (int si = 0; si < pageSubclassList.size(); si++) {
	                    final String ssi = String.valueOf(si + 1);
	                    final String subclasscd = (String) pageSubclassList.get(si);
	                    final String subclassName = (String) _param._subclassnameMap.get(subclasscd);
	                    svf.VrsOut("SUBCLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(subclassName) <= 6 ? "1" : "2") + "_" + ssi, subclassName); // 科目名
	                }
	                
	                for (int i1 = 0; i1 < pageStudentList.size(); i1++) {
	                    final Student student = (Student) pageStudentList.get(i1);
	
	                	final SubclassAll all = student._all9;
	                	final RankDev rankDev = SELECT_DIV_HR == _param._selectDiv ? all._hr : SELECT_DIV_COURSE == _param._selectDiv ? all._course : all._grade;
	
	                    svf.VrsOut("RANK", rankDev._rank); // 順位
	
	                    svf.VrsOut("HR_NAME", student._hrname); // 
	                    svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) <= 14 ? "1" : "2"), student._name); // 氏名
	
	                    for (int si = 0; si < pageSubclassList.size(); si++) {
	                        final String ssi = String.valueOf(si + 1);
	                        final String subclasscd = (String) pageSubclassList.get(si);
	                        final String score = (String) student._scoreMap.get(subclasscd);
	                        svf.VrsOut("SCORE" + ssi, score);
	                    }
	                    
	                    svf.VrsOut("TOTAL", all._sum); // 総合点
	                    svf.VrEndRecord();
	                    _hasData = true;
	                }
	            }
	        }
        }
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
    
    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static boolean isAll(final String subclassCd) {
        return
        SUBCLASSCD_ALL3.equals(subclassCd) ||
        SUBCLASSCD_ALL5.equals(subclassCd) ||
        SUBCLASSCD_ALL9.equals(subclassCd);
    }

    private static class StudentGroup {
        final String _code;
        String _grade;
        String _gradehrclass;
        String _hrname;
        String _coursecode;
        String _coursecodename;
        String _groupCode;
        String _groupName;

        final List _studentList = new ArrayList();
        List _subclassList = Collections.EMPTY_LIST;

        public StudentGroup(final String code) {
            _code = code;
        }

        public void load(final DB2UDB db2, final Param param) {
            _subclassList = getSubclassList(db2, param);
        }

        private List getSubclassList(final DB2UDB db2, final Param param) {

//            final String sqlRep = getSubclassReplaceCmbSql(param);
//            log.debug(" replace_cmb sql = " + sqlRep);
//            final Set attendSubclasses = new TreeSet(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sqlRep), "ATTEND_SUBCLASSCD"));

            final String sql = getSubclassSql(param);
            log.debug("getSubclass sql = " + sql);
            final List subclassList = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                if (null == KnjDbUtils.getString(row, "SUBCLASS_NAME")) {
                    continue;
                }

                final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
//                if (attendSubclasses.contains(subclassCd)) {
//                    continue;
//                }
                if (!subclassList.contains(subclassCd)) {
                    subclassList.add(subclassCd);
                }
            }
            return subclassList;
        }

        private String getSubclassSql(final Param param) {
            boolean outputBefore = false;

            StringBuffer stb = new StringBuffer();

            if (outputBefore) { stb.append(" , "); }
            else { stb.append(" with "); }
            outputBefore = true;
            stb.append(" T_HIGH AS ( ");
            stb.append("   select ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("   from ");
            stb.append("     PROFICIENCY_RANK_DAT t1");
            stb.append("     inner join SCHREG_REGD_DAT t2 on");
            stb.append("        t1.YEAR = t2.YEAR and");
            stb.append("        t1.SEMESTER = t2.SEMESTER and");
            stb.append("        t2.SCHREGNO = t1.SCHREGNO and");
            stb.append("        t2.GRADE = '" + param._grade + "'");
            stb.append("   where ");
            stb.append("     t1.YEAR = '" + param._year + "' ");
            stb.append("     and t1.SEMESTER = '" + param._semester + "' ");
            stb.append("     and t2.GRADE = '" + param._grade + "' ");
            if (SELECT_DIV_HR == param._selectDiv) {
                stb.append("    and t2.GRADE || t2.HR_CLASS = '" + _code + "' ");
            } else if (SELECT_DIV_COURSE == param._selectDiv) {
                stb.append("    and t2.COURSECD || t2.MAJORCD || t2.COURSECODE = '" + _code + "' ");
            }
            stb.append("   group by ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" ) ");

            stb.append(" select ");
            stb.append("   t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("   t2.SUBCLASS_ABBV AS SUBCLASS_NAME ");
            stb.append(" from T_HIGH t1 ");
            stb.append(" left join PROFICIENCY_SUBCLASS_MST t2 on ");
            stb.append("   t1.PROFICIENCY_SUBCLASS_CD = t2.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" group by ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD, t2.SUBCLASS_ABBV ");
            stb.append(" order by ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD ");

            return stb.toString();
        }

//        /** 合併科目取得SQL */
//        private String getSubclassReplaceCmbSql(final Param param) {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     T1.DIV, T1.COMBINED_SUBCLASSCD, T1.ATTEND_SUBCLASSCD ");
//            stb.append(" FROM ");
//            stb.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.YEAR = '" + param._year + "' ");
//            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
//            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
//            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
//            stb.append("     AND T1.GRADE = '" + param._grade +"' ");
//            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
//            stb.append("       CASE WHEN T1.DIV = '04' THEN '0' || '" + _groupCode + "' || '0000' ");
//            stb.append("       ELSE '" + _coursecode + "' END ");
//            stb.append(" ORDER BY ");
//            stb.append("     T1.DIV, ");
//            stb.append("     T1.COMBINED_SUBCLASSCD, ");
//            stb.append("     T1.ATTEND_SUBCLASSCD ");
//            return stb.toString();
//        }
        
        private static StudentGroup createStudentGroups(final DB2UDB db2, final Param param, final String selected) {

            final StudentGroup group = new StudentGroup(selected);

            final String sql = Student.getStudentSql(selected, param);
            log.info("student sql =" + sql);

            final Map studentMap = new HashMap();
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();

                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                if (null == studentMap.get(schregno)) {
                    final String hrname = KnjDbUtils.getString(row, "HR_NAME");
                    final Student student = new Student(
                            KnjDbUtils.getString(row, "YEAR"),
                            schregno,
                            KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "CLASS1"),
                            hrname,
                            KnjDbUtils.getString(row, "COURSECODE1"),
                            KnjDbUtils.getString(row, "ATTENDNO"),
                            KnjDbUtils.getString(row, "NAME"),
                            KnjDbUtils.getString(row, "SEX")
                    );
                    group._grade = KnjDbUtils.getString(row, "GRADE");
                    group._gradehrclass = KnjDbUtils.getString(row, "CLASS1");
                    group._hrname = hrname;
                    group._coursecode = KnjDbUtils.getString(row, "COURSECODE1");
                    group._coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                    group._groupCode = KnjDbUtils.getString(row, "GROUP_CD");
                    group._groupName = KnjDbUtils.getString(row, "GROUP_NAME");
                    group._studentList.add(student);
                    studentMap.put(schregno, student);
                }

                final Student student = (Student) studentMap.get(schregno);

                //log.debug(" get Student="+student);

                final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String score = KnjDbUtils.getString(row, "SCORE");
                if (!isAll(subclassCd)) {
				    student._scoreMap.put(subclassCd, score);
				} else {
				    if (SUBCLASSCD_ALL3.equals(subclassCd)) {
				    } else if (SUBCLASSCD_ALL5.equals(subclassCd)) {
				    } else if (SUBCLASSCD_ALL9.equals(subclassCd)) {
				        student._all9._sum = score;
				    }
				}
                if (isAll(subclassCd)) {
                    student.setAllData(
                            subclassCd,
                            KnjDbUtils.getString(row, "AVG"),
                            KnjDbUtils.getString(row, "CLASS_RANK"),
                            KnjDbUtils.getString(row, "GRADE_RANK"),
                            KnjDbUtils.getString(row, "COURSE_RANK"),
                            KnjDbUtils.getString(row, "COURSEGROUP_RANK"),
                            KnjDbUtils.getString(row, "MAJOR_RANK"),
                            KnjDbUtils.getString(row, "CLASS_DEVIATION"),
                            KnjDbUtils.getString(row, "GRADE_DEVIATION"),
                            KnjDbUtils.getString(row, "COURSE_DEVIATION"),
                            KnjDbUtils.getString(row, "COURSEGROUP_DEVIATION"),
                            KnjDbUtils.getString(row, "MAJOR_DEVIATION")
                            );
                }
            }
            return group;
        }
    }

    private static class SubclassAll {
        String _sum;
        String _avg;
        RankDev _hr = new RankDev();
        RankDev _grade  = new RankDev();
        RankDev _course = new RankDev();
        RankDev _major = new RankDev();
        RankDev _coursegroup = new RankDev();
    }

    private static class RankDev {
        String _rank;
        String _dev;
    }

    /** 生徒クラス */
    private static class Student {
        final String _year;
        final String _schregno;
        final String _grade;
        final String _classNo;
        final String _hrname;
        final String _courseCd;
        final String _attendno;
        final String _name;
        final String _sex;

        final HashMap _scoreMap = new HashMap();// 最大9科目

        final SubclassAll _all9 = new SubclassAll();
        
        Student(final String year, final String schregno, final String grade, final String classNo, final String hrname, final String courseCd, final String attendno,
                final String name, String sex
        ) {
            _year = year;
            _schregno = schregno;
            _grade = grade;
            _classNo = classNo;
            _hrname = hrname;
            _courseCd = courseCd;
            _attendno = attendno;
            _name = name;
            _sex  =sex;
        }

        private static class StudentComparator implements Comparator {
        	final Param _param;
        	public StudentComparator(final Param param) {
        		_param = param;
        	}
        	
            public int compare(Object o1, Object o2) {
            	final Student s1 = (Student) o1;
            	final Student s2 = (Student) o2;

                final int cmpGrade = Integer.valueOf(s1._grade).compareTo(Integer.valueOf(s2._grade));
                if (0 != cmpGrade) {
                    return cmpGrade;
                }
                final int cmpclassno = Integer.valueOf(s1._classNo).compareTo(Integer.valueOf(s2._classNo));
                if (SELECT_DIV_HR == _param._selectDiv && 0 != cmpclassno) {
                    return cmpclassno;
                } else if (SELECT_DIV_COURSE == _param._selectDiv && Integer.valueOf(s1._courseCd).compareTo(Integer.valueOf(s2._courseCd)) != 0) {
                    return Integer.valueOf(s1._courseCd).compareTo(Integer.valueOf(s2._courseCd));
                }

                final String rankStr = s1.getRankStr(_param);
                final String anRankStr = s2.getRankStr(_param);

                final Integer thisRank = null == rankStr ? new Integer("9999") :Integer.valueOf(rankStr);
                final Integer otherRank = null == anRankStr ? new Integer("9999") :Integer.valueOf(anRankStr);

                final int cmpRank = thisRank.compareTo(otherRank);
                if (0 == cmpRank) {
                    if (0 == cmpGrade) {
                        if (0 == cmpclassno) {
                            return Integer.valueOf(s1._attendno).compareTo(Integer.valueOf(s2._attendno));
                        }
                        return cmpclassno;
                    }
                    return cmpGrade;
                }
                return cmpRank;
            }
        }

        private String getRankStr(final Param param) {
            SubclassAll _sa = _all9;
            RankDev rd = new RankDev();;
            if (SELECT_DIV_HR == param._selectDiv) {
                rd = _sa._hr;
            } else if (SELECT_DIV_GRADE == param._selectDiv) {
                rd = _sa._grade;
            } else if (SELECT_DIV_COURSE == param._selectDiv) {
                rd = _sa._course;
            }
            return rd._rank;
        }

        public void setAllData(final String subclassCd, final String avg,
                final String classRank, final String gradeRank, final String courseRank, final String coursegroupRank, final String majorRank,
                final String classDev, final String gradeDev, final String courseDev, final String coursegroupDev, final String majorDev) {
            SubclassAll all = new SubclassAll();

            if (SUBCLASSCD_ALL3.equals(subclassCd)) {
            } else if (SUBCLASSCD_ALL5.equals(subclassCd)) {
            } else if (SUBCLASSCD_ALL9.equals(subclassCd)) {
                all = _all9;
            }
            all._avg = avg;
            all._hr._rank = classRank;
            all._grade._rank = gradeRank;
            all._course._rank = courseRank;
            all._coursegroup._rank = coursegroupRank;
            all._major._rank = majorRank;
            all._hr._dev = classDev;
            all._grade._dev = gradeDev;
            all._course._dev = courseDev;
            all._coursegroup._dev = coursegroupDev;
            all._major._dev = majorDev;

        }

        public boolean equals(Object o) {
            if (!(o instanceof Student)) {
                return false;
            }
            Student other = (Student) o;
            return (other._year.equals(_year) && other._schregno.equals(_schregno));
        }

        /**
         * 帳票出力対象データ抽出ＳＱＬ生成処理
         * @return              SQL文字列
         * @throws Exception
         */
        public static String getStudentSql(final String selected, final Param param) {

            final String RANK_DIV_GRADE = "01";
            final String RANK_DIV_HRCLASS = "02";
            final String RANK_DIV_COURSE = "03";
            final String RANK_DIV_MAJOR = "04";
            final String RANK_DIV_COURSEGROUP = "05";

            String gradeClass = "";
            String courseCdMajorCdCourseCode = "";

            if (param._selectDiv == SELECT_DIV_HR) {
                gradeClass = selected;
            } else if (param._selectDiv == SELECT_DIV_COURSE) {
                courseCdMajorCdCourseCode = selected;
            }

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH RANK_T AS ( ");
            stb.append(" select ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SEMESTER, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PROFICIENCYDIV, ");
            stb.append("     t1.PROFICIENCYCD AS TESTCD, ");
            stb.append("     t1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     t1.SCORE, ");
            stb.append("     t1.AVG, ");
//            stb.append("     CAST(NULL AS VARCHAR(2)) AS SCORE_DI, ");
            stb.append("     L2.RANK AS CLASS_RANK, ");
            stb.append("     t1.RANK AS GRADE_RANK, ");
            stb.append("     L3.RANK AS COURSE_RANK, ");
            stb.append("     L4.RANK AS COURSEGROUP_RANK, ");
            stb.append("     L7.RANK AS MAJOR_RANK, ");
            stb.append("     L2.DEVIATION AS CLASS_DEVIATION, ");
            stb.append("     t1.DEVIATION AS GRADE_DEVIATION, ");
            stb.append("     L3.DEVIATION AS COURSE_DEVIATION, ");
            stb.append("     L4.DEVIATION AS COURSEGROUP_DEVIATION, ");
            stb.append("     L7.DEVIATION AS MAJOR_DEVIATION, ");
            stb.append("     t4.GRADE || t3.HR_CLASS AS CLASS1, ");
            stb.append("     t4.HR_NAMEABBV AS HR_NAME, ");
            stb.append("     t3.COURSECD || t3.MAJORCD || t3.COURSECODE AS COURSECODE1, ");
            stb.append("     t7.COURSECODENAME, ");
            stb.append("     L5.GROUP_CD, ");
            stb.append("     L6.GROUP_NAME, ");
            stb.append("     t4.GRADE, ");
            stb.append("     t5.NAME, ");
            stb.append("     t6.ABBV1 AS SEX, ");
            stb.append("     t3.ATTENDNO ");
            stb.append(" from PROFICIENCY_RANK_DAT t1 ");
            stb.append("     inner join SCHREG_REGD_DAT t3 on ");
            stb.append("         t1.YEAR = t3.YEAR and ");
            stb.append("         t1.SEMESTER = t3.SEMESTER and ");
            stb.append("         t1.SCHREGNO = t3.SCHREGNO ");
            stb.append("     left join SCHREG_REGD_HDAT t4 on ");
            stb.append("         t3.YEAR = t4.YEAR and ");
            stb.append("         t3.SEMESTER = t4.SEMESTER and ");
            stb.append("         t3.GRADE = t4.GRADE and ");
            stb.append("         t3.HR_CLASS = t4.HR_CLASS ");
            stb.append("     left join SCHREG_BASE_MST t5 on ");
            stb.append("         t1.SCHREGNO = t5.SCHREGNO ");
            stb.append("     left join NAME_MST t6 on ");
            stb.append("         t6.NAMECD1 = 'Z002' and ");
            stb.append("         t6.NAMECD2 = t5.SEX ");
            stb.append("     left join COURSECODE_MST t7 on ");
            stb.append("         t3.COURSECODE = t7.COURSECODE ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT M1 ON  M1.YEAR = t1.YEAR ");
            stb.append("          AND M1.SEMESTER = t1.SEMESTER ");
            stb.append("          AND M1.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND M1.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND M1.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND M1.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L2 ON  L2.YEAR = t1.YEAR ");
            stb.append("          AND L2.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L2.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L2.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L2.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L2.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L2.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L2.RANK_DIV = '" + RANK_DIV_HRCLASS + "' ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L3 ON  L3.YEAR = t1.YEAR ");
            stb.append("          AND L3.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L3.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L3.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L3.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L3.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L3.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L3.RANK_DIV = '" + RANK_DIV_COURSE + "' ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L4 ON  L4.YEAR = t1.YEAR ");
            stb.append("          AND L4.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L4.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L4.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L4.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L4.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L4.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L4.RANK_DIV = '" + RANK_DIV_COURSEGROUP + "' ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT L5 ON  L5.YEAR = t3.YEAR ");
            stb.append("          AND L5.GRADE = t3.GRADE ");
            stb.append("          AND L5.COURSECD = t3.COURSECD ");
            stb.append("          AND L5.MAJORCD = t3.MAJORCD ");
            stb.append("          AND L5.COURSECODE = t3.COURSECODE ");
            stb.append("    LEFT JOIN COURSE_GROUP_CD_HDAT L6 ON  L6.YEAR = L5.YEAR ");
            stb.append("          AND L6.GRADE = L5.GRADE ");
            stb.append("          AND L6.GROUP_CD = L5.GROUP_CD ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L7 ON  L7.YEAR = t1.YEAR ");
            stb.append("          AND L7.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L7.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L7.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L7.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L7.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L7.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L7.RANK_DIV = '" + RANK_DIV_MAJOR + "' ");
            stb.append(" where ");
            stb.append("     t1.YEAR = '" + param._year + "' ");
            stb.append("     and t1.SEMESTER = '" + param._semester + "' ");
            stb.append("     and t1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     and t1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            stb.append("     and t1.RANK_DATA_DIV = '" + RANK_DATA_DIV01_SOGOTEN + "' ");
            stb.append("     and t1.RANK_DIV = '" + RANK_DIV_GRADE + "' ");
            stb.append("     and t3.GRADE = '" + param._grade + "' ");
            if (SELECT_DIV_HR == param._selectDiv) {
                stb.append("     and t4.GRADE || t3.HR_CLASS = '" + gradeClass + "' ");
            }
            if (SELECT_DIV_COURSE == param._selectDiv) {
                stb.append("     and t3.COURSECD || t3.MAJORCD || t3.COURSECODE = '" + courseCdMajorCdCourseCode + "' ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     RANK_T ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE ");
            if(SELECT_DIV_HR == param._selectDiv || SELECT_DIV_GRADE == param._selectDiv) {
                stb.append("     , CLASS1 ");
            }
            if(SELECT_DIV_COURSE == param._selectDiv) {
                stb.append("     , COURSECODE1 ");
            }
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59812 $ $Date: 2018-04-21 05:38:20 +0900 (土, 21 4 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _loginDate;
        final int _printCount;

        final String _grade;
        final String[] _categorySelected;
//        final String _schoolCd;
//        final String _selectSchoolKind;
        final String _cmd;

        final int _selectDiv; // クラス(1)/コース(2)/学年(3)

        final String _proficiencydiv;
        final String _proficiencycd;

        final boolean _seirekiFlg;

        final String _testname;
        final String _semesterName;
        final Map _subclassnameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _printCount = NumberUtils.isDigits(request.getParameter("PRINT_COUNT")) ? Integer.parseInt(request.getParameter("PRINT_COUNT")) : 9999;
            _grade = request.getParameter("GRADE");
//            _schoolCd = request.getParameter("SCHOOLCD");
//            _selectSchoolKind = request.getParameter("SELECT_SCHOOLKIND");
            _cmd = request.getParameter("cmd");

            if ("1".equals(request.getParameter("SELECT_DIV"))) {
                _selectDiv = SELECT_DIV_HR;
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            } else if ("2".equals(request.getParameter("SELECT_DIV"))) {
                _selectDiv = SELECT_DIV_COURSE;
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            } else { // _printType == '3'(grade) or default
                _selectDiv = SELECT_DIV_GRADE;
                _categorySelected = new String[]{_grade};
            }

            _seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ")));
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");

            _testname = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " select VALUE(PROFICIENCYNAME1, '') AS PROFICIENCYNAME1 from PROFICIENCY_MST where PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' ")));
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " select VALUE(SEMESTERNAME, '') AS SEMESTERNAME from SEMESTER_MST where YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
            _subclassnameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT PROFICIENCY_SUBCLASS_CD, SUBCLASS_NAME FROM PROFICIENCY_SUBCLASS_MST "), "PROFICIENCY_SUBCLASS_CD", "SUBCLASS_NAME");
        }

        public String getSelectDivName() {
            switch (_selectDiv) {
            case SELECT_DIV_HR:
                return "クラス順位";
            case SELECT_DIV_COURSE:
                return "コース順位";
            case SELECT_DIV_GRADE:
                return "学年順位";
            default:
                return "";
            }
        }

        /**
         * 画面で選択したクラス/ホームルーム/コースの名称を得る
         * @param studentGroup 表示する学生
         * @return クラス/ホームルーム/コースの名称
         */
        public String getSelectName(final StudentGroup studentGroup) {
            switch (_selectDiv) {
            case SELECT_DIV_HR:
                return studentGroup._hrname;
            case SELECT_DIV_COURSE:
                return studentGroup._coursecodename;
            case SELECT_DIV_GRADE:
                return ""; // Integer.valueOf(_grade).toString() + "年生";
            default:
                return "";
            }
        }

        /**
         * 年度の名称を得る
         * @return 年度の名称
         */
        public String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDateStr() {
            return _seirekiFlg ? (_loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate)) : (KNJ_EditDate.h_format_JP( _loginDate));
        }
    }

}
