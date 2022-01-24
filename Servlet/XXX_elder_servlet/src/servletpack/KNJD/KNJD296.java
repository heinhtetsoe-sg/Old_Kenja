package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  広島なぎさ 単位認定会議資料
 */
public class KNJD296 {

    private static final Log log = LogFactory.getLog(KNJD296.class);

    private static final String SEMEALL = "9";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    public void outputPdf(final Vrw32alp svf, final HttpServletRequest request) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
    	
    	final String form;
    	final String title;
    	final String schoolkindname = "H".equals(param._schoolKind) ? "高校" : "J".equals(param._schoolKind) ? "中学" : "";
    	final int gradeCdInt = NumberUtils.isDigits(param._gradeCd) ? Integer.parseInt(param._gradeCd) : 0;
    	if ("H".equals(param._schoolKind) && gradeCdInt == 3) {
    		// 高3
            form = "KNJD296_2.frm";
            title = "卒業判定会議資料";
    	} else {
    		form = "KNJD296_1.frm";
    		title = "単位認定会議資料";
    	}
        final int maxSubclassCol = 13;
        final int maxLine = 10;
    	
    	final List<Student> studentAllList = Student.getStudentList(db2, param);
    	final List<Student> targetStudentList = Student.getTargetStudentList(param, studentAllList);
    	if (targetStudentList.size() == 0) {
    		log.info("対象者なし");
    		return;
    	}
    	final Map<String, HR> hrClassStudentListMap = Student.getHrClassStudetListMap(studentAllList);
    	
    	for (final List<Student> studentList : getPageList(targetStudentList, maxLine)) {

            final Map<SubclassMst, TreeMap<String, Integer>> subclassCourseCreditsMap = new TreeMap<SubclassMst, TreeMap<String, Integer>>(); 
            final Set<SubclassMst> subclasses = new TreeSet<SubclassMst>();
            for (final Student student : studentList) {
            	for (final SubclassMst mst : student.getHyoteiTargetSubclass()) {
            		subclasses.add(mst);
            		final String credits = student._creditsMap.get(mst);
            		if (NumberUtils.isDigits(credits)) {
            			getMappedMap(subclassCourseCreditsMap, mst).put(student.course(), Integer.valueOf(credits));
            		}
            	}
            	for (final SubclassMst mst : student.getKekkaTargetSubclass()) {
            		subclasses.add(mst);
            		final String credits = student._creditsMap.get(mst);
            		if (NumberUtils.isDigits(credits)) {
            			getMappedMap(subclassCourseCreditsMap, mst).put(student.course(), Integer.valueOf(credits));
            		}
            	}
            }
            final List<SubclassMst> studentSubclassList = new ArrayList<SubclassMst>(subclasses);
            if (studentSubclassList.isEmpty()) {
            	studentSubclassList.add(null); // dummy
            }
        	for (final List<SubclassMst> subclassList : getPageList(studentSubclassList, maxSubclassCol)) {
        		subclassList.remove(null);
        		
        		svf.VrSetForm(form, 4);
        		
        		svf.VrsOut("TITLE", param._nendo + " " + schoolkindname + (gradeCdInt <= 0 ? " " : String.valueOf(gradeCdInt) + "年生") + title); // タイトル
        		svf.VrsOut("DATE", param._loginDateStr); // 日付
        		
        		final List<String> hrClassList = new ArrayList<String>(hrClassStudentListMap.keySet()); 
        		final int hrMaxCol = 8;
        		int zaisekiCountTotal = 0;
        		for (int hri = 0; hri < Math.min(hrClassList.size(), hrMaxCol); hri++) {
        			final int col = hri + 1;
        			final HR hr = hrClassStudentListMap.get(hrClassList.get(hri));
        			final int ketaStaffname = KNJ_EditEdit.getMS932ByteLength(hr._staffname);
        			svf.VrsOutn("TR_NAME" + (ketaStaffname <= 8 ? "1" : ketaStaffname <= 14 ? "2" : "3"), col, hr._staffname); // 担任名
        			final int zaisekiCount = hr.getZaisekiCount();
        			zaisekiCountTotal += zaisekiCount;
					svf.VrsOutn("ENROLL", col, String.valueOf(zaisekiCount)); // 在籍数
        		}
        		svf.VrsOutn("ENROLL", 9, String.valueOf(zaisekiCountTotal)); // 在籍数
        		
        		for (int subi = 0; subi < Math.min(subclassList.size(), maxSubclassCol); subi++) {
        			final int col = subi + 1;
        			final SubclassMst mst = subclassList.get(subi);
        			final String subclassname = StringUtils.defaultString(mst._subclassabbv, mst._subclassname);
        			final int subclassnameLen = StringUtils.defaultString(subclassname).length();
        			if (subclassnameLen > 8) {
        				svf.VrsOutn("SUBCLASS_NAME3_1", col, subclassname.substring(0, 8)); // 科目名
        				svf.VrsOutn("SUBCLASS_NAME3_2", col, subclassname.substring(8)); // 科目名
        			} else if (subclassnameLen > 5) {
        				svf.VrsOutn("SUBCLASS_NAME2", col, subclassname); // 科目名
        			} else {
        				svf.VrsOutn("SUBCLASS_NAME1", col, subclassname); // 科目名
        			}
        			final TreeMap<String, Integer> courseCredits = getMappedMap(subclassCourseCreditsMap, mst);
        			final TreeSet<Integer> credits = new TreeSet<Integer>(courseCredits.values());
        			if (credits.size() > 0) {
        				if (credits.size() > 1) {
        					log.info(" " + mst + " , credits = " + courseCredits);
        					svf.VrsOutn("CREDIT", col, credits.first().toString() + "~" + credits.last().toString()); // 単位
        				} else {
        					svf.VrsOutn("CREDIT", col, credits.last().toString()); // 単位
        				}
        			}
        		}
        		
        		for (int sti = 0; sti < studentList.size(); sti++) {
        			final Student student = studentList.get(sti);
        			
        			svf.VrsOut("HR_NAME", student._hrClassName1); // 組
        			svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) <= 14 ? "1" : "2"), student._name); // 氏名
        			
        			for (int subi = 0; subi < subclassList.size(); subi++) {
        				final int col = subi + 1;
        				final SubclassMst mst = subclassList.get(subi);
        				if (student.getHyoteiTargetSubclass().contains(mst) || student.getKekkaTargetSubclass().contains(mst)) {
        					svf.VrsOutn("VALUE", col, getMappedMap(student._scoreMap, mst).get(TESTCD_GAKUNEN_HYOTEI)); // 評定
        					final SubclassAttendance sa = student._subclassAttendanceMap.get(mst);
        					if (null != sa) {
        						svf.VrsOutn("KEKKA", col, sa._sick.toString()); // 欠課
        						svf.VrsOutn("ATTEND", col, sa._lesson.toString()); // 授業
        					}
        				}
        			}
        			
        			if (null != student._attendance) {
        				svf.VrsOut("LESSON", String.valueOf(student._attendance._lesson)); // 授業日数
        				svf.VrsOut("MUST", String.valueOf(student._attendance._mLesson)); // 出席すべき日数
        				svf.VrsOut("ABSENCE", String.valueOf(student._attendance._absent)); // 欠席
        				svf.VrsOut("LATE", String.valueOf(student._attendance._late)); // 遅刻
        				svf.VrsOut("EARLY", String.valueOf(student._attendance._early)); // 早退
        			}
        			svf.VrEndRecord();
        			_hasData = true;
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
    private static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static <K, V, U> TreeMap<V, U> getMappedMap(final Map<K, TreeMap<V, U>> map, final K key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<V, U>());
        }
        return map.get(key1);
    }

    /**
     * 生徒
     */
    private static class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        String _attendno;
        String _hrClassName1;
        String _notZaisekiFlg;
        String _entyear;
        Attendance _attendance;
        
        final Map<SubclassMst, String> _creditsMap = new HashMap();
        final Map<SubclassMst, TreeMap<String, String>> _scoreMap = new HashMap();
        final Map<SubclassMst, SubclassAttendance> _subclassAttendanceMap = new HashMap();
        
        public String course() {
        	return _coursecd + _majorcd + _coursecode;
        }
        
        public String toString() {
        	return "Student(" + _schregno + ", " + _attendno + ":" + _name + ")";
        }
        
        public List<SubclassMst> getHyoteiTargetSubclass() {
        	final List<SubclassMst> rtn = new ArrayList<SubclassMst>();
    		for (final Map.Entry<SubclassMst, TreeMap<String, String>> e : _scoreMap.entrySet()) {
    			if ("1".equals(e.getValue().get(TESTCD_GAKUNEN_HYOTEI))) { // 評定1を含む
    				rtn.add(e.getKey());
    			}
    		}
    		return rtn;
        }
        
        public List<SubclassMst> getKekkaTargetSubclass() {
        	final List<SubclassMst> rtn = new ArrayList<SubclassMst>();
    		for (final Map.Entry<SubclassMst, SubclassAttendance> e : _subclassAttendanceMap.entrySet()) {
    			final SubclassAttendance att = e.getValue();
    			if (null == att._lesson || att._lesson.doubleValue() <= 0 || null == att._sick || att._sick.doubleValue() <= 0) {
    				continue;
    			}
    			if (att._lesson.doubleValue() / 3 < att._sick.doubleValue()) { // 授業時数 / 3 < 欠課時数
    				rtn.add(e.getKey());
    			}
    		}
    		return rtn;
        }
        
        public static List<Student> getTargetStudentList(final Param param, final List<Student> studentList) {
        	final List<Student> targetList = new ArrayList<Student>();
        	for (final Student student : studentList) {
        		
        		if ("1".equals(student._notZaisekiFlg)) {
        			if (param._isOutputDebug) {
        				log.info(" not target (not zaiseki) : " + student);
        			}
        			continue;
        		}
        		
        		// 評定
        		final boolean isHyoteiTarget = student.getHyoteiTargetSubclass().size() > 0;
        		
        		// 欠課時数
        		final boolean isKekkaTarget = student.getKekkaTargetSubclass().size() > 0;
        		
        		// 欠席日数
        		boolean isKessekiTarget = false;
        		if (null != student._attendance) {
        			isKessekiTarget = 0 < student._attendance._mLesson && student._attendance._mLesson / 3 < student._attendance._absent; 
        		}
        		
        		if (isHyoteiTarget || isKekkaTarget || isKessekiTarget) {
        			if (param._isOutputDebug) {
        				log.info(" target (hyotei? " + isHyoteiTarget + ", kekka? " + isKekkaTarget + ", kesseki? " + isKessekiTarget + ") : " + student);
        			}
        			targetList.add(student);
        		}
        	}
			return targetList;
		}

		public static Map<String, HR> getHrClassStudetListMap(final List<Student> studentList) {
        	final Map<String, HR> rtn = new TreeMap<String, HR>();
        	for (final Student student : studentList) {
        		if (null == rtn.get(student._hrClass)) {
        			rtn.put(student._hrClass, new HR(student._grade, student._hrClass, student._hrname, student._staffname));
        		}
        		rtn.get(student._hrClass)._studentList.add(student);
        	}
        	return rtn;
		}

		/**
         * 生徒を取得
         */
        private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,HDAT.HR_NAME ");
            stb.append("            ,STFM.STAFFNAME ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.HR_CLASS ");
            stb.append("            ,GDAT.GRADE_CD ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECODE ");
            stb.append("            ,HDAT.HR_CLASS_NAME1 ");
            // 転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("            ,CASE WHEN ENTGRD.GRD_DATE < '" + param._loginDate + "' THEN 1 ");
            // 転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("                  WHEN ENTGRD.ENT_DATE > '" + param._loginDate + "' THEN 1 ");
//            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("             END AS NOT_ZAISEKI_FLG "); // 非在籍フラグ
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND HDAT.GRADE = REGD.GRADE ");
            stb.append("                  AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     INNER JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST SEMEG ON SEMEG.YEAR = '" + param._year + "' AND SEMEG.SEMESTER = REGD.SEMESTER AND SEMEG.GRADE = REGD.GRADE ");
//            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
//            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
//            stb.append("                  AND CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND REGD.GRADE = '" + param._grade + "' ");
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List<Student> studentList = new ArrayList<Student>();

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final Student student = new Student();
                student._schregno = KnjDbUtils.getString(row, "SCHREGNO");
                student._name = KnjDbUtils.getString(row, "NAME");
                student._hrname = KnjDbUtils.getString(row, "HR_NAME");
                student._staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                student._attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) + "番" : KnjDbUtils.getString(row, "ATTENDNO");
                student._grade = KnjDbUtils.getString(row, "GRADE");
                student._hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                student._coursecd = KnjDbUtils.getString(row, "COURSECD");
                student._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                student._coursecode = KnjDbUtils.getString(row, "COURSECODE");
                student._hrClassName1 = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                student._notZaisekiFlg = KnjDbUtils.getString(row, "NOT_ZAISEKI_FLG");
                studentList.add(student);
            }
            
        	final Map<String, Student> studentMap = new HashMap<String, Student>();
        	for (final Student student : studentList) {
        		studentMap.put(student._schregno, student);
        	}

        	Attendance.load(db2, param, studentMap);
            setSubclassList(db2, param, studentMap);
            SubclassAttendance.load(db2, param, studentList);

            return studentList;
        }
        
        public static void setSubclassList(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) {
            final String sql = getSubclassSql(param);
            if (param._isOutputDebug) {
            	log.info(" subclass sql = " + sql);
            }
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = studentMap.get(schregno);
                if (null == student) {
                	continue;
                }
                final SubclassMst mst = param._subclassMstMap.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
                if (null == mst) {
                	continue;
                }
                if (null != KnjDbUtils.getString(row, "SEM_TESTCD")) {
                	getMappedMap(student._scoreMap, mst).put(KnjDbUtils.getString(row, "SEM_TESTCD"), KnjDbUtils.getString(row, "SCORE"));
                }
                student._creditsMap.put(mst, KnjDbUtils.getString(row, "CREDITS"));
            }
        }

        public static String getSubclassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR_SUBCLASS AS ( ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
            stb.append("    , L2.CLASSCD, L2.SCHOOL_KIND, L2.CURRICULUM_CD, L2.SUBCLASSCD ");
            stb.append("    , L3.CREDITS ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN CHAIR_STD_DAT L1 ON ");
            stb.append("      L1.YEAR = T1.YEAR ");
            stb.append("    AND L1.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN CHAIR_DAT L2 ON ");
            stb.append("      L2.YEAR = T1.YEAR ");
            stb.append("    AND L2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L2.CHAIRCD = L1.CHAIRCD ");
            stb.append(" LEFT JOIN CREDIT_MST L3 ON ");
            stb.append("      L3.YEAR = T1.YEAR ");
            stb.append("    AND L3.GRADE = T1.GRADE ");
            stb.append("    AND L3.COURSECD = T1.COURSECD ");
            stb.append("    AND L3.MAJORCD = T1.MAJORCD ");
            stb.append("    AND L3.COURSECODE = T1.COURSECODE ");
            stb.append("    AND L3.CLASSCD = L2.CLASSCD ");
            stb.append("    AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("    AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("    AND L3.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
            stb.append("    , L2.CLASSCD, L2.SCHOOL_KIND, L2.CURRICULUM_CD, L2.SUBCLASSCD ");
            stb.append("    , L3.CREDITS ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
            stb.append("    , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    , T1.CREDITS ");
            stb.append("    , REC.SEMESTER || REC.TESTKINDCD || REC.TESTITEMCD || REC.SCORE_DIV AS SEM_TESTCD ");
            stb.append("    , REC.SCORE ");
            stb.append(" FROM CHAIR_SUBCLASS T1 ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC ON ");
            stb.append("      REC.YEAR = T1.YEAR ");
            stb.append("    AND REC.SEMESTER = '9' ");
            stb.append("    AND REC.TESTKINDCD = '99' ");
            stb.append("    AND REC.TESTITEMCD = '00' ");
            stb.append("    AND REC.SCORE_DIV = '09' ");
            stb.append("    AND REC.CLASSCD = T1.CLASSCD ");
            stb.append("    AND REC.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND REC.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND REC.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND REC.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.CLASSCD <= '90' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , T1.SUBCLASSCD ");
            return stb.toString();
        }
    }
    
    private static class HR {
    	final String _grade;
    	final String _hrclass;
    	final String _hrname;
    	final String _staffname;
    	final List<Student> _studentList = new ArrayList<Student>();
    	int _zaisekiCount = -1;
    	public HR(final String grade, final String hrclass, final String hrname, final String staffname) {
    		_grade = grade;
    		_hrclass = hrclass;
    		_hrname = hrname;
    		_staffname = staffname;
    	}
		public int getZaisekiCount() {
			if (_zaisekiCount == -1) {
				final List<Student> notZaisekiList = new ArrayList<Student>();
				for (final Student student : _studentList) {
					if (null != student._notZaisekiFlg) {
						notZaisekiList.add(student);
					}
				}
				if (notZaisekiList.size() != 0) {
					log.info(" " + _grade + _hrclass + " notZaiseki (size = " + notZaisekiList.size() + ") = " + notZaisekiList);
				}
				_zaisekiCount = _studentList.size() - notZaisekiList.size();
			}
			return _zaisekiCount;
		}
    }
    
    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _abroad;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int abroad,
                final int absent,
                final int present,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
        }

        public String toString() {
        	return "Att(les=" + _lesson + ",mles=" + _mLesson + ",susp" + _suspend + ",mourn=" + _mourning  + ",abroad=" + _abroad + ",absent=" + _absent + ",prese=" + _present + ",late=" + _late + ",early=" + _early + ")";
        }

        private static void load(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        param._sdate,
                        param._edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                final Integer zero = Integer.valueOf(0);
                for (final Student student : studentMap.values()) {

                    for (final Map<String, String> rs : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        if (!SEMEALL.equals(KnjDbUtils.getString(rs, "SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                KnjDbUtils.getInt(rs, "LESSON", zero),
                                KnjDbUtils.getInt(rs, "MLESSON", zero),
                                KnjDbUtils.getInt(rs, "SUSPEND", zero),
                                KnjDbUtils.getInt(rs, "MOURNING", zero),
                                KnjDbUtils.getInt(rs, "TRANSFER_DATE", zero),
                                KnjDbUtils.getInt(rs, "SICK", zero),
                                KnjDbUtils.getInt(rs, "PRESENT", zero),
                                KnjDbUtils.getInt(rs, "LATE", zero),
                                KnjDbUtils.getInt(rs, "EARLY", zero)
                        );
                        student._attendance = attendance;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }
    
    private static class SubclassAttendance {
        BigDecimal _lesson;
        BigDecimal _attend;
        BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
        }

        private static void load(final DB2UDB db2, final Param param, final List<Student> studentList) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        SEMEALL,
                        param._sdate,
                        param._edate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);
                
                for (final Student student : studentList) {

                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        final SubclassMst mst = param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {

                            final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "MLESSON", null);
                            final BigDecimal rawSick = KnjDbUtils.getBigDecimal(row, "SICK1", null);
                            final BigDecimal sick = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                            final BigDecimal rawReplacedSick = KnjDbUtils.getBigDecimal(row, "RAW_REPLACED_SICK", null);
                            final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", null);

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final SubclassAttendance sa = new SubclassAttendance(lesson, attend, sick2);

//                            log.debug(" schregno = " + student._schregno + ", sa = " + subclassAttendance);
                            student._subclassAttendanceMap.put(mst, sa);
                        }
                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }
    
    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _electdiv;
        boolean _isSaki;
        boolean _isMoto;
        String _calculateCreditFlg;
        SubclassMst _sakikamoku;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String subclassabbv, final String subclassname,
                final Integer electdiv) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
        }
        public int compareTo(final SubclassMst os) {
            int rtn;
            rtn = _subclasscd.compareTo(os._subclasscd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
        
        private static Map<String, SubclassMst> getSubclassMst(
                final DB2UDB db2,
                final Param param,
                final String year
        ) {
        	Map<String, SubclassMst> subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += "   VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += "   T1.CLASSCD, ";
                sql += "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += "   T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, T1.SUBCLASSNAME, ";
                sql += "   VALUE(T1.ELECTDIV, '0') AS ELECTDIV ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "SPECIALDIV"), KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD")
                    		, KnjDbUtils.getString(row, "SUBCLASSABBV")
                    		, KnjDbUtils.getString(row, "SUBCLASSNAME")
                    		, KnjDbUtils.getInt(row, "ELECTDIV", new Integer(999)));
                    subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, CALCULATE_CREDIT_FLG,  ";
                sql += " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT ";
                sql += " WHERE YEAR = '" + year + "' ";
                if (param._isOutputDebug) {
                	log.info(" repl sub sql = " + sql);
                }
                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    
                    final SubclassMst combined = subclassMstMap.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = subclassMstMap.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null != combined && null != attend) {
                    	combined._isSaki = true;
                    	attend._isMoto = true;
                    	combined._calculateCreditFlg = KnjDbUtils.getString(row, "CALCULATE_CREDIT_FLG");
                    	attend._sakikamoku = combined;
                    } else {
                    	if (param._isOutputDebug) {
                    		log.warn(" combined = " + combined + ", attend = " + attend + " in " + row);
                    	}
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return subclassMstMap;
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 70881 $ $Date: 2019-11-26 15:34:14 +0900 (火, 26 11 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _loginDateStr;

        final String _grade;
        final String _gradeCd;
        final String _schoolKind;
        
        final boolean _isOutputDebug;

        private Map<String, SubclassMst> _subclassMstMap;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;
        final String _sdate;
        final String _edate;

        private KNJSchoolMst _knjSchoolMst;

        final DecimalFormat _df;
        final boolean _isSeireki;
        final String _nendo;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginDateStr = KNJ_EditDate.getAutoFormatDate(db2, _loginDate);
            _grade = request.getParameter("GRADE");
            final Map<String, String> gdat = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT GRADE_CD, SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _gradeCd = KnjDbUtils.getString(gdat, "GRADE_CD");
            _schoolKind = KnjDbUtils.getString(gdat, "SCHOOL_KIND");
            
            final Map<String, String> seme9 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '9' "));
            _sdate = KnjDbUtils.getString(seme9, "SDATE");
            _edate = KnjDbUtils.getString(seme9, "EDATE");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            try {
            	final Map knjSchoolMstMap = new HashMap();
            	if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
            		knjSchoolMstMap.put("SCHOOL_KIND", _schoolKind);
            	}
                _knjSchoolMst = new KNJSchoolMst(db2, _year, knjSchoolMstMap);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            _subclassMstMap = SubclassMst.getSubclassMst(db2, this, _year);

            _df = null != _knjSchoolMst && ("3".equals(_knjSchoolMst._absentCov) || "4".equals(_knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            _nendo = _isSeireki ? StringUtils.defaultString(_year) + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD296' AND NAME = '" + propName + "' "));
        }

		private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                //log.info("科目マスタなし:" + subclasscd);
                return null;
            }
            return _subclassMstMap.get(subclasscd);
        }
    }
}
