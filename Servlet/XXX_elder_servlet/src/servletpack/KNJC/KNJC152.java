// kanji=漢字
/*
 * $Id: 0b7f2c8fb80422f31b9307fb766df3da6a0ee842 $
 *
 */
package servletpack.KNJC;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
 *  学校教育システム 賢者 [出欠管理] 立志舎 立志会特別補講出席状況のお知らせ
 */

public class KNJC152 {

    private static final Log log = LogFactory.getLog(KNJC152.class);

    private String SEMEALL = "9";
    
    private boolean _hasData;
    
    private Param _param;
    
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (Exception ex) {
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
        
        log.info(" $Id: 0b7f2c8fb80422f31b9307fb766df3da6a0ee842 $ ");
        KNJServletUtils.debugParam(request, log);

        _hasData = false;
        try {
            _param = new Param(request, db2);

            // 印刷処理
            printMain(db2, svf);
            
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
    
    /**
     *  印刷処理
     */
    private void printMain (
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        final List studentList = Student.loadStudentList(db2, _param);        

        setAttendData(db2, studentList);

        final String form = "KNJC152.frm";
        
        final String HOKOU_CLASSCD = "94";

        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);

            svf.VrSetForm(form, 1);

            svf.VrsOut("DATE", getDateString(_param._printDate));
			
	        svf.VrsOut("SEND_NAME", append(student._guardName, "　様")); // 宛先
	        svf.VrsOut("UNTIL_DATE", KNJ_EditDate.h_format_JP_MD(_param._attendEndDate)); // 日付まで
	        final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._name);
	        svf.VrsOut("NAME" + (ketaName <= 16 ? "1" : ketaName <= 20 ? "2" : "3"), student._name); // 氏名
	        svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号

            final DayAttendance da = student._daysAttendance;
	        svf.VrsOut("LESSON", String.valueOf(da._lessonDays)); // 授業日数
	        svf.VrsOut("ATTEND", String.valueOf(da._attendDays)); // 出席日数
	        svf.VrsOut("NOTICE", String.valueOf(da._sickDays)); // 欠席日数
	        
	        final SubclassAttendance att94 = student.getTotalSubclassAttendance(HOKOU_CLASSCD);
	        svf.VrsOut("TOTAL_LESSON", String.valueOf(att94._lesson)); // 総授業時間数
	        svf.VrsOut("TOTAL_ATTEND", String.valueOf(att94._attend)); // 総出席時間数
	        if (att94._lesson > 0) {
		        svf.VrsOut("ATTEND_PER", percentage(att94._attend, att94._lesson)); // 出席率
	        } else {
		        svf.VrsOut("ATTEND_PER", "0.0%"); // 出席率
	        }
	        svf.VrsOut("HR_NAME", student._hrName); // 年組
	        svf.VrsOut("TEACHER_NAME", getStaffNameString(student._trcd1Staffname, student._trcd2Staffname, student._trcd3Staffname)); // 担当職員名
	        
	        svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private String percentage(final int bunshi, final int bunbo) {
		return new BigDecimal(bunshi).multiply(new BigDecimal(100)).divide(new BigDecimal(bunbo), 1, BigDecimal.ROUND_HALF_UP).toString() + "%";
	}

	private String append(final String a, final String b) {
    	if (StringUtils.isBlank(a)) {
    		return "";
    	}
    	return a + b;
    }

    private String getDateString(final String date) {
    	if (null == date) {
    		return null;
    	}
        final String nen = _param._seirekiFlg ? String.valueOf(Integer.parseInt(date.substring(0,  4))) + "年" : KNJ_EditDate.h_format_JP_N(date);
        return  nen + KNJ_EditDate.h_format_JP_MD(date);
    }

    
    private String getStaffNameString(final String staffName1, final String staffName2, final String staffName3) {
        final String[] staffNames = {staffName1, staffName2, staffName3};
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (int i = 0; i < staffNames.length; i++) {
            if (staffNames[i] != null) {
                stb.append(comma + staffNames[i]);
                comma = "・";
            }
        }
        return stb.toString();
    }

    /**
     * 生徒と1日出欠、科目別出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private void setAttendData(final DB2UDB db2, final List studentList) {
    	PreparedStatement ps = null;
        String sql = null;
        try {
            // 出欠の情報
            final String sdate = _param.getSdate(db2, _param._year, _param._attendStartDate);
            
            // 1日単位
            _param._attendParamMap.put("schregno", "?");
            sql = AttendAccumulate.getAttendSemesSql(
                    _param._year,
                    SEMEALL,
                    sdate,
                    _param._attendEndDate,
                    _param._attendParamMap
            );
            log.debug("get AttendSemes sql = " + sql);
            ps = db2.prepareStatement(sql);
            
            // 1日単位
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                for (final Iterator dit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); dit.hasNext();) {
                	final Map row = (Map) dit.next();
                    if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    student._daysAttendance.add(row);
                }
            }
            DbUtils.closeQuietly(ps);

            // 時数単位
            _param._attendParamMap.put("schregno", "?");
            sql = AttendAccumulate.getAttendSubclassSql(
                    _param._year,
                    SEMEALL,
                    sdate,
                    _param._attendEndDate,
                    _param._attendParamMap
                    );
            
            log.debug("get AttendSubclass sql = " + sql);
            ps = db2.prepareStatement(sql);

            // 時数単位
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                for (final Iterator dit = KnjDbUtils.query(db2, ps, new Object[] { student._schregno }).iterator(); dit.hasNext();) {
                	final Map row = (Map) dit.next();
                    if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }

                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    if (student._subclassAttendanceMap.get(subclassCd) == null) {
                        student._subclassAttendanceMap.put(subclassCd, new SubclassAttendance(subclassCd));
                    }
                    final SubclassAttendance sa = (SubclassAttendance) student._subclassAttendanceMap.get(subclassCd);
                    sa.add(row, _param);
                }
            }
            DbUtils.closeQuietly(ps);
            
        } catch (Exception ex) {
            log.error("exception!", ex);
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _grade;
        final String _course;
        final String _trcd1Staffname;
        final String _trcd2Staffname;
        final String _trcd3Staffname;
        final String _guardName;
        final Map _subclassAttendanceMap;
        final DayAttendance _daysAttendance;
        
        public Student(
                final String schregno,
                final String hrName,
                final String attendNo,
                final String name, 
                final String sex,
                final String grade,
                final String course,
                final String trcd1Staffname,
                final String trcd2Staffname,
                final String trcd3Staffname,
                final String guardName) {
            _schregno = schregno;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _grade = grade;
            _course = course;
            _trcd1Staffname = trcd1Staffname;
            _trcd2Staffname = trcd2Staffname;
            _trcd3Staffname = trcd3Staffname;
            _guardName = guardName;
            _daysAttendance = new DayAttendance();
            _subclassAttendanceMap = new TreeMap();
        }
        
        public SubclassAttendance getTotalSubclassAttendance(final String classcd) {
        	final SubclassAttendance total = new SubclassAttendance(classcd + "_TOTAL");
        	for (final Iterator it = _subclassAttendanceMap.keySet().iterator(); it.hasNext();) {
        		final String subclasscd = (String) it.next();
        		if (null != subclasscd && subclasscd.startsWith(classcd)) {
        			final SubclassAttendance att = (SubclassAttendance) _subclassAttendanceMap.get(subclasscd);
        			total.add(att);
        		}
        	}
			return total;
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
                            KnjDbUtils.getString(row, "TR_CD1_STAFFNAME"),
                            KnjDbUtils.getString(row, "TR_CD2_STAFFNAME"),
                            KnjDbUtils.getString(row, "TR_CD3_STAFFNAME"),
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
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("     STF1.STAFFNAME AS TR_CD1_STAFFNAME, ");
            stb.append("     STF2.STAFFNAME AS TR_CD2_STAFFNAME, ");
            stb.append("     STF3.STAFFNAME AS TR_CD3_STAFFNAME, ");
            stb.append("     GUR.GUARD_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON ");
            stb.append("         REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON ");
            stb.append("         REGD.YEAR = REGDH.YEAR ");
            stb.append("         AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("         AND REGD.GRADE = REGDH.GRADE ");
            stb.append("         AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
            stb.append("     LEFT JOIN STAFF_MST STF3 ON STF3.STAFFCD = REGDH.TR_CD3 ");
            stb.append("     LEFT JOIN GUARDIAN_DAT GUR ON GUR.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregno) + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.ATTENDNO ");
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
        public void add(final Map row) {
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
    }

    /** 出欠カウント */
    private static class SubclassAttendance {
        /** 科目コード */
        private String _subclassCd;

        /** 授業時数 */
        private int _lesson;
        /** 忌引時数 */
        private int _mourning;
        /** 出停時数 */
        private int _suspend;
        private int _virus;
        private int _koudome;
        /** 留学時数 */
        private int _abroad;
        /** 出席すべき時数 */
        private int _mlesson;
        /** 欠席時数 */
        private int _absent1;
        /** 出席時数 */
        private int _attend;
        
        /** 欠課時数+ペナルティー  */
        private BigDecimal _absent2 = new BigDecimal(0);

        public SubclassAttendance(final String subclassCd) {
            _subclassCd = subclassCd;
        }
        
        /**
         * 時数単位
         */
        public void add(final Map row, final Param param) {
        	final Integer zero = new Integer(0);
            int lesson   = KnjDbUtils.getInt(row, "LESSON", zero).intValue(); // 授業時数
            int absent1   = KnjDbUtils.getInt(row, "SICK1", zero).intValue(); // 欠課時数
            int mlesson  = KnjDbUtils.getInt(row, "MLESSON", zero).intValue(); // 出席すべき時数
            _lesson   += lesson;
            _mourning += KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
            _suspend  += KnjDbUtils.getInt(row, "SUSPEND", zero).intValue();
            _virus += KnjDbUtils.getInt(row, "VIRUS", zero).intValue();
            _koudome  += KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
            _mlesson  += mlesson;
            _absent1  += absent1;
            _attend   += mlesson - absent1; // 出席時数 = 出席すべき時数 - 欠課時数

        	final BigDecimal bdZero = new BigDecimal(0);
            if ("1".equals(KnjDbUtils.getString(row, "IS_COMBINED_SUBCLASS"))) {
                _absent2  = _absent2.add(KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", bdZero)); // 欠課
            } else {
                _absent2  = _absent2.add(KnjDbUtils.getBigDecimal(row, "SICK2", bdZero)); // 欠課
            }
        }
        
        /**
         * 時数単位
         */
        public void add(final SubclassAttendance att) {
            _lesson   += att._lesson;
            _mourning += att._mourning;
            _suspend  += att._suspend;
            _virus += att._virus;
            _koudome  += att._koudome;
            _mlesson  += att._mlesson;
            _absent1  += att._absent1;
            _attend   += att._attend;
            _absent2  = _absent2.add(att._absent2); // 欠課
        }
    }
    
    private static class Param {

        final String _year;
        final String _semester;
        final String _attendStartDate;
        final String _attendEndDate;
        final String _printDate;
        final String _gradeHrclass;
        final String[] _schregno;
        
        private KNJSchoolMst _knjSchoolMst;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        
        final boolean _seirekiFlg;

        final Map _attendParamMap;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _attendStartDate = request.getParameter("SDATE").replace('/', '-');
            _attendEndDate = request.getParameter("EDATE").replace('/', '-');
            _printDate = request.getParameter("PRINT_DATE");
            
            _gradeHrclass = request.getParameter("GRADE_HRCLASS");
            _schregno = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ")));
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _gradeHrclass.substring(0, 2));
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                log.debug(" KNJSchoolMst.lesson_flg = " + _knjSchoolMst._jugyouJisuFlg);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            } finally {
                db2.commit();
            }
        }

        private String getSdate(final DB2UDB db2, final String year, final String defaultSdate) {
            String sdate = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER = '1' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' ")), defaultSdate);
            if (sdate.compareTo(defaultSdate) <= 0) {
                sdate = defaultSdate;
            }
            return sdate;
        }
    }
}
