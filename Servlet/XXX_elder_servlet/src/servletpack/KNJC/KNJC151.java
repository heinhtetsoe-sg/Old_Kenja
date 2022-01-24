// kanji=漢字
/*
 * $Id: fd18546b83b30b86f67b62c1096dce2eb51cac08 $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 遅刻指導対象者のお知らせ（レッドカード）
 */
public class KNJC151 {

    private static final Log log = LogFactory.getLog(KNJC151.class);

    private boolean _hasData;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (IOException ex) {
            log.error("svf instancing exception! ", ex);
        }
        
        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }
        
        log.info(" $Id: fd18546b83b30b86f67b62c1096dce2eb51cac08 $ ");
        KNJServletUtils.debugParam(request, log);

        try {
            Param param = new Param(request, db2);

            // 印刷処理
            printMain(db2, svf, param);
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            db2.close();
        }
    }
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return rtn;
    }

    /**
     *  印刷処理
     */
    private void printMain (
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        final List studentList = Student.loadStudentList(db2, param);        
        // 生徒がいなければ処理をスキップ
        if (studentList.size() == 0) {
            return;
        }
        setAttendData(db2, param, studentList);

        for (final Iterator it2 = studentList.iterator(); it2.hasNext();) {
            final Student student = (Student) it2.next();
            
            BigDecimal rate = student.getLateRate();
            if (null == rate || rate.compareTo(param._rate) <= 0) {
            	// 指定遅刻率以下は対象外
            	continue;
            }

            final String form = "KNJC151.frm";
            svf.VrSetForm(form, 1);
            
            final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
            svf.VrsOut("NAME" + (ketaName <= 16 ? "1" : ketaName <= 20 ? "2" : "3"), student._name); // 氏名

            svf.VrsOut("HR_NAME", student._hrName); // 年組
            svf.VrsOut("DATE", param._printDateStr); // 日付
            svf.VrsOut("SEND_NAME", append(student._guardName, "　様")); // 宛先
            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
            svf.VrsOut("MONTH", String.valueOf(Integer.parseInt(param._month)) + "月"); // 月
            svf.VrsOut("LESSON", String.valueOf(student._daysAttendance._lessonDays)); // 授業日数
            svf.VrsOut("ATTEND", String.valueOf(student._daysAttendance._attendDays)); // 出席日数
            svf.VrsOut("LATE", String.valueOf(student._daysAttendance._lateDays)); // 遅刻日数
            svf.VrsOut("LATE_PER", append(rate.toString(), "%")); // 遅刻率
            svf.VrsOut("LATE_PER_LIMIT", append(param._rate.toString(), "％"));
            
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static String append(final String a, final String b) {
    	if (StringUtils.isBlank(a)) {
    		return "";
    	}
    	return a + b;
    }

    private static List getMappedList(final Map m, final Object key) {
        if (null == m.get(key)) {
            m.put(key, new ArrayList());
        }
        return (List) m.get(key);
    }
    
    private static Map getMappedMap(final Map m, final Object key) {
        if (null == m.get(key)) {
            m.put(key, new TreeMap());
        }
        return (Map) m.get(key);
    }
    
    /**
     * 生徒と1日出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private void setAttendData(final DB2UDB db2, final Param param, final List studentList) {
    	final Map gradeStudentMapMap = new HashMap();
    	for (final Iterator git = studentList.iterator(); git.hasNext();) {
    		final Student student = (Student) git.next();
    		getMappedMap(gradeStudentMapMap, student._grade).put(student._schregno, student);
    	}
    	
    	for (final Iterator git = gradeStudentMapMap.keySet().iterator(); git.hasNext();) {
    		final String grade = (String) git.next();
    		final Map schregnoStudentMap = (Map) gradeStudentMapMap.get(grade);
    		
    		final Map semesterDateRangeMap = getMappedMap(param._gradeDateRangeListMap, grade);
    		
    		for (final Iterator dit = semesterDateRangeMap.keySet().iterator(); dit.hasNext();) {
    			final String semester = (String) dit.next();
    			final Map dateRange = (Map) semesterDateRangeMap.get(semester);
    			
                String sql = null;
                try {
                    // 出欠の情報
                    final String sdate = KnjDbUtils.getString(dateRange, "SDATE");
                    final String edate = KnjDbUtils.getString(dateRange, "EDATE");
                    
                    // 1日単位
                    param._attendParamMap.put("grade", grade);
                    sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            "9",
                            sdate,
                            edate,
                            param._attendParamMap
                    );
                    log.debug("get AttendSemes sql = " + sql);
                    
                    // 1日単位
                    for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                    	
                    	final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                    	final Student student = (Student) schregnoStudentMap.get(schregno);
                    	if (null == student) {
                    		continue;
                    	}

                        if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        student._daysAttendance.add(row);
                    }

                } catch (Exception ex) {
                    log.error("exception!", ex);
                }
    		}
    	}
    }
    
    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _grade;
        final String _course;
        final String _guardName;
        final DayAttendance _daysAttendance;
        
        public Student(
                final String schregno,
                final String hrName,
                final String attendno,
                final String name, 
                final String sex,
                final String grade,
                final String course,
                final String guardName) {
            _schregno = schregno;
            _hrName = hrName;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _grade = grade;
            _course = course;
            _guardName = guardName;
            _daysAttendance = new DayAttendance();
        }

        // 遅刻率
        public BigDecimal getLateRate() {
        	if (0 >= _daysAttendance._attendDays) {
        		return null;
        	}
        	if (0 == _daysAttendance._lateDays) {
        		return new BigDecimal(0);
        	}
			return new BigDecimal(_daysAttendance._lateDays).multiply(new BigDecimal(100)).divide(new BigDecimal(_daysAttendance._attendDays), 1, BigDecimal.ROUND_HALF_UP);
		}

		public static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        public static List loadStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();        
            
            try {
                // HRの生徒を取得
                final String sql = sqlSchregRegdDat(param);
                //log.debug("schreg_regd_dat sql = " + sql);
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final Student st = new Student(
                            KnjDbUtils.getString(row, "SCHREGNO"), 
                            KnjDbUtils.getString(row, "HR_NAME"),
                            KnjDbUtils.getString(row, "ATTENDNO"), 
                            KnjDbUtils.getString(row, "NAME"), 
                            KnjDbUtils.getString(row, "SEX"),
                            KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "COURSE"),
                            KnjDbUtils.getString(row, "GUARD_NAME")
                            );
                    studentList.add(st);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return studentList;
        }
        
        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T4.GUARD_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ");
            stb.append("         AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T1.GRADE = T3.GRADE ");
            stb.append("         AND T1.HR_CLASS = T3.HR_CLASS ");
            stb.append("     LEFT JOIN GUARDIAN_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
            return stb.toString();
        }
    }
    
    /** 1日出欠カウント */
    private static class DayAttendance {
        /** 授業日数 */
        private int _lessonDays;
        /** 忌引日数 */
        private int _mourningDays;
        /** 出停日数 */
        private int _suspendDays;
        private int _virusDays;
        private int _koudomeDays;
        /** 留学日数 */
        private int _abroadDays;
        /** 出席すべき日数 */
        private int _mlessonDays;
        /** 欠席日数 */
        private int _sickDays;
        /** 出席日数 */
        private int _attendDays;
        /** 遅刻日数 */
        private int _lateDays;
        /** 早退日数 */
        private int _earlyDays;

        /**
         * @param rs
         * @throws SQLException
         */
        public void add(Map row) {
        	final Integer zero = new Integer(0);
            int lesson   = KnjDbUtils.getInt(row, "LESSON", zero).intValue(); // 授業日数
            int sick     = KnjDbUtils.getInt(row, "SICK", zero).intValue(); // 病欠日数
            int special  = KnjDbUtils.getInt(row, "MOURNING", zero).intValue() + KnjDbUtils.getInt(row, "SUSPEND", zero).intValue() + KnjDbUtils.getInt(row, "VIRUS", zero).intValue() + KnjDbUtils.getInt(row, "KOUDOME", zero).intValue(); // 特別欠席
            int mlesson  = lesson - special; // 出席すべき日数
            _lessonDays   += lesson;
            _mourningDays += KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
            _suspendDays  += KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
            _virusDays  += KnjDbUtils.getInt(row, "VIRUS", zero).intValue();
            _koudomeDays  += KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
            _abroadDays   += KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue();
            _mlessonDays  += mlesson;
            _sickDays     += sick;
            _attendDays   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
            _lateDays     += KnjDbUtils.getInt(row, "LATE", zero).intValue();
            _earlyDays    += KnjDbUtils.getInt(row, "EARLY", zero).intValue();
        }
        
        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return 
            "LESSON=" + df5.format(_lessonDays)
            + ", MOR=" + df5.format(_mourningDays)
            + ", SSP=" + df5.format(_suspendDays)
            + ", ABR=" + df5.format(_abroadDays)
            + ", MLS=" + df5.format(_mlessonDays)
            + ", SCK=" + df5.format(_sickDays)
            + ", ATE=" + df5.format(_attendDays)
            + ", LAT=" + df5.format(_lateDays)
            + ", EAL=" + df5.format(_earlyDays);
        }
    }
    
    private static class Param {

        final String _year;
        final String _semester;
        final String _month;
        final BigDecimal _rate;
        final String _loginDate;
        final String _printDateStr;
        final String[] _classSelected;
        final boolean _seirekiFlg;
        
        private KNJSchoolMst _knjSchoolMst;

        final Map _attendParamMap;
        
        private Map _gradeDateRangeListMap;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _month = request.getParameter("MONTH");
            _rate = !NumberUtils.isNumber(request.getParameter("RATE")) ? new BigDecimal(0) : new BigDecimal(request.getParameter("RATE"));
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _printDateStr = getDateString(db2, request.getParameter("PRINT_DATE"));
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ")));
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", "?");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                log.debug(" KNJSchoolMst.lesson_flg = " + _knjSchoolMst._jugyouJisuFlg);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            } finally {
                db2.commit();
            }
            
            _gradeDateRangeListMap = getGradeDateRangeListMap(db2, _year);
        }

        private Map getGradeDateRangeListMap(final DB2UDB db2, final String year) {
        	final Set gradeSet = new TreeSet();
        	for (int i = 0; i < _classSelected.length; i++) {
        		gradeSet.add(_classSelected[i].substring(0, 2));
        	}
        	final String[] gradeArray = new String[gradeSet.size()];
        	int arrIdx = 0;
        	for (final Iterator it = gradeSet.iterator(); it.hasNext();) {
        		final String grade = (String) it.next();
        		gradeArray[arrIdx++] = grade;
        	}
        	
        	final Map rtn = new TreeMap();
        	final Calendar cal = Calendar.getInstance();
        	final String dateNen;
        	if (Integer.parseInt(_month) < 4) {
        		dateNen = String.valueOf(Integer.parseInt(_year) + 1);
        	} else {
        		dateNen = _year;
        	}
        	cal.set(Calendar.YEAR, Integer.parseInt(dateNen));
        	cal.set(Calendar.MONTH, Integer.parseInt(_month) - 1 + 1); // 翌月
        	cal.set(Calendar.DAY_OF_MONTH, 1); // 1日
        	cal.add(Calendar.DATE, -1); // の前日 = 指定月の最大日付
        	final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        	final String minDate = dateNen + "-" + _month + "-" + "01";
        	final String maxDate = dateNen + "-" + _month + "-" + cal.get(Calendar.DAY_OF_MONTH);
        	log.info(" minDate = " + minDate + ", maxDate = " + maxDate);

        	final Calendar maxDateCalendar = Calendar.getInstance();
        	maxDateCalendar.setTime(java.sql.Date.valueOf(maxDate));
        	final String sql = "SELECT SEMESTER, GRADE, SDATE, EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER <> '9' AND GRADE IN " + SQLUtils.whereIn(true, gradeArray) + " AND ? BETWEEN SDATE AND EDATE  ";
        	PreparedStatement ps = null;
        	try {
        		log.info(" sql1 = " + sql);
            	ps = db2.prepareStatement(sql);
            	cal.setTime(java.sql.Date.valueOf(minDate));
            	while (cal.before(maxDateCalendar)) {
            		for (final Map row : KnjDbUtils.query(db2, ps, new Object[] { df.format(cal.getTime()) })) {
                		if (null == row) {
                			continue;
                		}
                		final String semester = KnjDbUtils.getString(row, "SEMESTER");
                		final Map gradeMap = getMappedMap(rtn, KnjDbUtils.getString(row, "GRADE"));
                		if (!gradeMap.containsKey(semester)) {
                			java.sql.Date sdate = java.sql.Date.valueOf(KnjDbUtils.getString(row, "SDATE"));
                			if (sdate.before(java.sql.Date.valueOf(minDate))) {
                				sdate = java.sql.Date.valueOf(minDate);
                			}
                			java.sql.Date edate = java.sql.Date.valueOf(KnjDbUtils.getString(row, "EDATE"));
                			if (edate.after(java.sql.Date.valueOf(maxDate))) {
                				edate = java.sql.Date.valueOf(maxDate);
                			}
                			row.put("SDATE", df.format(sdate));
                			row.put("EDATE", df.format(edate));
                			
                			gradeMap.put(semester, row);
                		}
            		}
            		
            		cal.add(Calendar.DAY_OF_MONTH, 1);
            	}
        	} catch (Exception e) {
        		log.error("exception!", e);
        	} finally {
        		DbUtils.closeQuietly(ps);
        	}
        	log.info(" rtn = " + rtn);
            return rtn;
        }
        
        private String getDateString(final DB2UDB db2, final String date) {
        	if (null == date) {
        		return null;
        	}
            final String nen = _seirekiFlg ? String.valueOf(Integer.parseInt(date.substring(0,  4))) + "年" : KNJ_EditDate.h_format_JP_N(db2, date);
            return  nen + KNJ_EditDate.h_format_JP_MD(date);
        }
    }
}
