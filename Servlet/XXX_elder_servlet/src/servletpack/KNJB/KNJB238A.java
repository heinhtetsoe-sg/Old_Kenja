package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
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
 * クラス別希望状況一覧
 */
public class KNJB238A {

    private static final Log log = LogFactory.getLog(KNJB238A.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            // 生徒データを取得
            final List<Kouza> kouzaList = Kouza.createKouzaInfoData(_param, db2);
            _hasData = false;
            for (final Kouza kouza : kouzaList) {
            	if (kouza._studentList.size() == 0) {
            		continue;
            	}
            	printMain(svf, kouza);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void printMain(final Vrw32alp svf, final Kouza kouza) {
        svf.VrSetForm("KNJB238A.frm", 4);

        svf.VrsOut("TITLE", "講座別　出欠状況集計一覧");
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("CHAIRCD", kouza._chaircd);
        svf.VrsOut("CHAIR_NAME", kouza._chairname);
        svf.VrsOut("PERIOD", _param.getDate(_param._dateFrom) + " \uFF5E " + _param.getDate(_param._dateTo));
        String staffM = "";
        String staffS = "";
        String seqM = "";
        String seqS = "";
        for (final Staff staff : kouza._staffList) {
            if ("1".equals(staff._chargediv)) {
                staffM += seqM + staff._staffname;
                seqM = ",";
            } else {
                staffS += seqS + staff._staffname;
                seqS = ",";
            }
        }
		final String str = staffM;
        final String fieldLenM = (30 < KNJ_EditEdit.getMS932ByteLength(str)) ? "_2" : "";
		final String str1 = staffS;
        final String fieldLenS = (30 < KNJ_EditEdit.getMS932ByteLength(str1)) ? "_2" : "";
        svf.VrsOut("STAFF_NAME1" + fieldLenM, staffM);
        svf.VrsOut("STAFF_NAME2"  + fieldLenS, staffS);
        final int kensuu = kouza._studentList.size();
        svf.VrsOut("ATTEND_NUM", "受講者数：　" + String.valueOf(kensuu) + "名");

        int no = 0;
        int creditsMax = 0;
        int lessonMax = 0;
        for (final Student student : kouza._studentList) {
            no++;
            svf.VrsOut("NO", String.valueOf(no));
            svf.VrsOut("SCHREG_NO", student._schregno);
            svf.VrsOut("NAME" + ((30 < KNJ_EditEdit.getMS932ByteLength(student._name)) ? "2" : "1"), student._name);
            svf.VrsOut("SEX", student._sexname);
            final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
            svf.VrsOut("ATTEND_NO", student._hrNameAbbv + "-" + attendno);

            final int credits = (NumberUtils.isDigits(student._credits)) ? Integer.parseInt(student._credits) : 0;
            if (creditsMax < credits) creditsMax = credits;
            svf.VrsOut("CREDIT", (0 < creditsMax) ? String.valueOf(creditsMax) : "");

            for (final Attend attend : student._attendList) {
                final int lesson = (NumberUtils.isDigits(attend._lesson)) ? Integer.parseInt(attend._lesson) : 0;
                log.debug("lesson="+lesson);
                if (lessonMax < lesson) lessonMax = lesson;
                svf.VrsOut("KEKKA", attend._sick2);
                svf.VrsOut("KETUJI", attend._sick1);
                svf.VrsOut("LATE", attend._late);
                svf.VrsOut("EARLY", attend._early);
                svf.VrsOut("KOUKETU", attend._absent);
                final int suspend = (NumberUtils.isDigits(attend._suspend)) ? Integer.parseInt(attend._suspend) : 0;
                final int mourning = (NumberUtils.isDigits(attend._mourning)) ? Integer.parseInt(attend._mourning) : 0;
                svf.VrsOut("SUSPEND", String.valueOf(suspend + mourning));
            }
            if (0 < student._lateMinutes) {
            	svf.VrsOut("LE_TOTAL", String.valueOf(student._lateMinutes));
            }

            svf.VrsOut("EXEC_TIME", (0 < lessonMax) ? String.valueOf(lessonMax) : "");

            svf.VrEndRecord();
        	_hasData = true;
        }
    }

    /** 講座データクラス */
    private static class Kouza {
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _takesemesName;
        List<Staff> _staffList = new ArrayList();
        List<Student> _studentList = new ArrayList();

        Kouza(
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String takesemesName
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _takesemesName = takesemesName;
        }
        
        private static List<Kouza> createKouzaInfoData(final Param param, final DB2UDB db2) throws SQLException {
            final List<Kouza> rtnList = new ArrayList();
            final String sql = getKouzaInfoSql(param);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final Kouza kouzaInfo = new Kouza(
                        KnjDbUtils.getString(row, "SUBCLASSCD"),
                        KnjDbUtils.getString(row, "SUBCLASSNAME"),
                        KnjDbUtils.getString(row, "CHAIRCD"),
                        KnjDbUtils.getString(row, "CHAIRNAME"),
                        KnjDbUtils.getString(row, "TAKESEMES_NAME")
                        );
                rtnList.add(kouzaInfo);
            }
            try {
            	Staff.createStaffInfoData(param, db2, rtnList);
            	Student.createStudentInfoData(param, db2, rtnList);
            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
                db2.commit();
            }
            return rtnList;
        }

        private static String getKouzaInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.CHAIRNAME, ");
            stb.append("     T1.TAKESEMES, ");
            stb.append("     CASE WHEN T1.TAKESEMES = '0' THEN '通年' ELSE L1.SEMESTERNAME END AS TAKESEMES_NAME, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append(" T1.SUBCLASSCD AS SUBCLASSCD, ");
            }
            stb.append("     T2.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     INNER JOIN SUBCLASS_MST T2 ");
            stb.append("             ON  T2.SUBCLASSCD = T1.SUBCLASSCD ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.TAKESEMES ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.CHAIRCD IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            return stb.toString();
        }
    }

    /** 講座職員データクラス */
    private static class Staff {
        final String _staffcd;
        final String _staffname;
        final String _chargediv;

        Staff(
                final String staffcd,
                final String staffname,
                final String chargediv
        ) {
            _staffcd = staffcd;
            _staffname = staffname;
            _chargediv = chargediv;
        }
        
        private static void createStaffInfoData(final Param param, final DB2UDB db2, final List<Kouza> kouzaList) throws SQLException {
            PreparedStatement ps = null;
            try {
                final String sql = getStaffInfoSql(param);
                ps = db2.prepareStatement(sql);
                for (final Kouza kouza : kouzaList) {
                	for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {kouza._chaircd})) {
                		final Staff staffInfo = new Staff(
                				KnjDbUtils.getString(row, "STAFFCD"),
                				KnjDbUtils.getString(row, "STAFFNAME"),
                				KnjDbUtils.getString(row, "CHARGEDIV")
                				);
                		kouza._staffList.add(staffInfo);
                	}
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String getStaffInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.STAFFCD, ");
            stb.append("     T1.CHARGEDIV, ");
            stb.append("     T2.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append("     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T2.STAFFCD ");
            return stb.toString();
        }
    }

    /** 生徒データクラス */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendno;
        final String _credits;
        List<Attend> _attendList = new ArrayList();
        int _lateMinutes;

        Student(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String hrName,
                final String hrNameAbbv,
                final String attendno,
                final String credits
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendno = attendno;
            _credits = credits;
        }


        /**
         * 生徒データ取得処理
         * @param db2           ＤＢ接続オブジェクト
         * @return              帳票出力対象データリスト
         * @throws Exception
         */
        private static List<Student> createStudentInfoData(final Param param, final DB2UDB db2, final List<Kouza> kouzaList) throws SQLException {
            final List<Student> rtnList = new ArrayList<Student>();
            PreparedStatement ps = null;
            final Map<String, Student> studentMap = new HashMap<String, Student>();
            try {
                final String sql = getStudentInfoSql(param);
                ps = db2.prepareStatement(sql);
                for (final Kouza kouza : kouzaList) {
                	for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {kouza._chaircd})) {
                		final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                		if (!studentMap.containsKey(schregno)) {
                			final Student studentInfo = new Student(
                					schregno,
                					KnjDbUtils.getString(row, "NAME"),
                					KnjDbUtils.getString(row, "SEX"),
                					KnjDbUtils.getString(row, "SEX_NAME"),
                					KnjDbUtils.getString(row, "HR_NAME"),
                					KnjDbUtils.getString(row, "HR_NAMEABBV"),
                					KnjDbUtils.getString(row, "ATTENDNO"),
                					KnjDbUtils.getString(row, "CREDITS")
                					);
                			studentMap.put(schregno, studentInfo);
                		}
                		final Student studentInfo = studentMap.get(schregno);
                        kouza._studentList.add(studentInfo);
                	}
                }
                Student.createAttendInfoData(param, db2, studentMap);

            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtnList;
        }
        

        private static String getStudentInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T3.NAME, ");
            stb.append("     T3.SEX, ");
            stb.append("     N1.ABBV1 AS SEX_NAME, ");
            stb.append("     T5.HR_NAME, ");
            stb.append("     T5.HR_NAMEABBV, ");
            stb.append("     T4.ATTENDNO, ");
            stb.append("     L1.CREDITS ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T4 ");
            stb.append("             ON  T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND T4.YEAR = T1.YEAR ");
            stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T5 ");
            stb.append("             ON  T5.YEAR = T4.YEAR ");
            stb.append("             AND T5.SEMESTER = T4.SEMESTER ");
            stb.append("             AND T5.GRADE = T4.GRADE ");
            stb.append("             AND T5.HR_CLASS = T4.HR_CLASS ");
            stb.append("     LEFT JOIN CREDIT_MST L1  ");
            stb.append("             ON  L1.YEAR = T4.YEAR ");
            stb.append("             AND L1.GRADE = T4.GRADE ");
            stb.append("             AND L1.COURSECD = T4.COURSECD ");
            stb.append("             AND L1.MAJORCD = T4.MAJORCD ");
            stb.append("             AND L1.COURSECODE = T4.COURSECODE ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '" + param._subclasscd + "' ");
            } else {
                stb.append("         AND L1.SUBCLASSCD = '" + param._subclasscd + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.CHAIRCD = ? ");
            stb.append("     AND (( ");
            stb.append("     CASE WHEN DATE('" + param._dateFrom + "') BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("          THEN DATE('" + param._dateFrom + "') ");
            stb.append("          ELSE T2.SDATE ");
            stb.append("     END ");
            stb.append("     BETWEEN T1.APPDATE AND T1.APPENDDATE) OR ( ");
            stb.append("     CASE WHEN DATE('" + param._dateTo + "') BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("          THEN DATE('" + param._dateTo + "') ");
            stb.append("          ELSE T2.EDATE ");
            stb.append("     END ");
            stb.append("     BETWEEN T1.APPDATE AND T1.APPENDDATE)) ");
            stb.append(" GROUP BY ");
            stb.append("     T4.GRADE, ");
            stb.append("     T4.HR_CLASS, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T3.NAME, ");
            stb.append("     T3.SEX, ");
            stb.append("     N1.ABBV1, ");
            stb.append("     T5.HR_NAME, ");
            stb.append("     T5.HR_NAMEABBV, ");
            stb.append("     T4.ATTENDNO, ");
            stb.append("     L1.CREDITS ");
            stb.append(" ORDER BY ");
            stb.append("     T4.GRADE, ");
            stb.append("     T4.HR_CLASS, ");
            stb.append("     T4.ATTENDNO ");
            return stb.toString();
        }

        private static void createAttendInfoData(final Param param, final DB2UDB db2, final Map<String, Student> studentMap) throws SQLException {
            try {
                param._attendParamMap.put("subclasscd", param._subclasscd);
                final String sql = AttendAccumulate.getAttendSubclassSql(
                		((Boolean) param._hasuuMap.get("semesFlg")).booleanValue(),
                		param._definecode,
                		param._knjSchoolMst,
                		param._year,
                		param.SSEMESTER,
                		param._semester,
                		(String) param._hasuuMap.get("attendSemesInState"),
                		param._periodInState,
                		(String) param._hasuuMap.get("befDayFrom"),
                		(String) param._hasuuMap.get("befDayTo"),
                		(String) param._hasuuMap.get("aftDayFrom"),
                		(String) param._hasuuMap.get("aftDayTo"),
                		null,
                		null,
                		null,
                		param._attendParamMap
                		);

            	for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
            		if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER")) || !param._subclasscd.equals(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
            			continue;
            		}
            		final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            		final Student student = studentMap.get(schregno);
            		if (null == student) {
            			continue;
            		}
            		final Attend attendInfo = new Attend(
            				KnjDbUtils.getString(row, "LESSON"),
            				KnjDbUtils.getString(row, "SICK2"),
            				KnjDbUtils.getString(row, "SICK1"),
            				KnjDbUtils.getString(row, "LATE"),
            				KnjDbUtils.getString(row, "EARLY"),
            				KnjDbUtils.getString(row, "ABSENT"),
            				KnjDbUtils.getString(row, "SUSPEND"),
            				KnjDbUtils.getString(row, "MOURNING")
            				);
            		student._attendList.add(attendInfo);
            	}
            } catch (Exception e) {
            	log.error("exception!", e);
            }
            final String sql = getLateMinuteSql(param);
            //log.info(" sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
            	final String minutes = KnjDbUtils.getString(row, "REMARK1");
            	if (null == minutes) {
            		continue;
            	}
            	if (!NumberUtils.isDigits(minutes)) {
            		log.warn(" not integer : " + minutes);
            		continue;
            	}
        		final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
        		final Student student = studentMap.get(schregno);
        		if (null == student) {
        			continue;
        		}
        		student._lateMinutes += Integer.parseInt(minutes);
            }
        }
        
        
        private static String getLateMinuteSql(final Param param) {
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("     T1.EXECUTEDATE ");
        	stb.append("   , T1.PERIODCD ");
        	stb.append("   , T1.CHAIRCD ");
        	stb.append("   , L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD AS SUBCLASSCD ");
        	stb.append("   , L3.SCHREGNO ");
        	stb.append("   , L4.DI_CD ");
        	stb.append("   , L5.REMARK1 ");
        	stb.append(" FROM SCH_CHR_DAT T1 ");
        	stb.append(" INNER JOIN SEMESTER_MST L1 ON ");
        	stb.append("      L1.YEAR = T1.YEAR ");
        	stb.append("    AND L1.SEMESTER <> '9' ");
        	stb.append("    AND T1.EXECUTEDATE BETWEEN L1.SDATE AND L1.EDATE ");
        	stb.append(" INNER JOIN CHAIR_DAT L2 ON ");
        	stb.append("      L2.YEAR = L1.YEAR ");
        	stb.append("    AND L2.SEMESTER = L1.SEMESTER ");
        	stb.append("    AND L2.CHAIRCD = T1.CHAIRCD ");
        	stb.append(" INNER JOIN CHAIR_STD_DAT L3 ON ");
        	stb.append("      L3.YEAR = L2.YEAR ");
        	stb.append("    AND L3.SEMESTER = L2.SEMESTER ");
        	stb.append("    AND L3.CHAIRCD = L2.CHAIRCD ");
        	stb.append("    AND T1.EXECUTEDATE BETWEEN L3.APPDATE AND L3.APPENDDATE ");
        	stb.append(" INNER JOIN ATTEND_DAT L4 ON ");
        	stb.append("      L4.SCHREGNO = L3.SCHREGNO ");
        	stb.append("    AND L4.ATTENDDATE = T1.EXECUTEDATE ");
        	stb.append("    AND L4.PERIODCD = T1.PERIODCD ");
        	stb.append("    AND L4.CHAIRCD = T1.CHAIRCD ");
        	stb.append(" INNER JOIN ATTEND_DI_CD_DAT L5 ON ");
        	stb.append("      L5.YEAR = L1.YEAR ");
        	stb.append("    AND L5.DI_CD = L4.DI_CD ");
        	stb.append("  WHERE ");
        	stb.append("        L2.YEAR = '" + param._year + "' ");
        	stb.append("    AND T1.EXECUTEDATE BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' ");
        	stb.append("    AND L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '" + param._subclasscd + "' ");
        	stb.append("    AND L5.REP_DI_CD IN ('15', '16') ");
        	return stb.toString(); 
        }
        
    }

    /** 出欠クラス */
    private static class Attend {
        final String _lesson;
        final String _sick2;
        final String _sick1;
        final String _late;
        final String _early;
        final String _absent;
        final String _suspend;
        final String _mourning;

        Attend(
                final String lesson,
                final String sick2,
                final String sick1,
                final String late,
                final String early,
                final String absent,
                final String suspend,
                final String mourning
        ) {
            _lesson = lesson;
            _sick2 = sick2;
            _sick1 = sick1;
            _late = late;
            _early = early;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 73284 $ $Date: 2020-03-25 22:57:45 +0900 (水, 25 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _subclasscd;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlDate;
        final String _useCurriculumcd;

        final String SSEMESTER = "1";
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool _definecode;
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _subclasscd  = request.getParameter("SUBCLASSCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                _definecode = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _dateFrom, _dateTo);
                //log.debug(_attendSemesMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
            
    		_attendParamMap = new HashMap();
    		_attendParamMap.put("HttpServletRequest", request);
    		_attendParamMap.put("DB2UDB", db2);
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }

        private KNJDefineSchool setClasscode0(final DB2UDB db2) {
        	KNJDefineSchool definecode = new KNJDefineSchool();
            try {
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        private String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }
    }

}// クラスの括り
