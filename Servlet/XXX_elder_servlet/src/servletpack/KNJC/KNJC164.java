// kanji=漢字
/*
 * $Id: 7f9802cd95ca87d6798b6d9fc06b000d498863aa $
 *
 */
package servletpack.KNJC;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 欠課時数統計資料
 */

public class KNJC164 {

    private static final Log log = LogFactory.getLog(KNJC164.class);

    private Param _param;
    
    private boolean _hasdata;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        log.info("$Revision: 72813 $ $Date: 2020-03-09 10:45:24 +0900 (月, 09 3 2020) $ ");
        KNJServletUtils.debugParam(request, log);
        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (java.io.IOException ex) {
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
        
        try {
            _param = new Param(request, db2);
            _hasdata = false;
            printMain(svf, db2);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != _param) {
                DbUtils.closeQuietly(_param._psSubclassAttendance);
            }
            // 終了処理
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    private void printMain(final Vrw32alp svf, final DB2UDB db2) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final String loginDateFormat = KNJ_EditDate.h_format_JP(db2, _param._loginDate);
        final String sdateFormat = KNJ_EditDate.h_format_JP(db2, _param._sdate);
        final String edateFormat = KNJ_EditDate.h_format_JP(db2, _param._edate);

        final String title = "欠課時数統計資料";

        final int MAX_LINE = 50;
        int page = 0;
        for (int i = 0; i < _param._gradeHrclass.length; i++) {
        	log.info(" hr = " + _param._gradeHrclass[i]);
        	final Set<String> nosubclassSet = new HashSet<String>();
            final List<OutputLine> outputList = getOutputList(db2, _param._gradeHrclass[i]);
            if (outputList.size() != 0) {
                page += 1;

                svf.VrSetForm("KNJC164.frm", 4);
                int line = 0;
                String keepNo = "";
                for (final OutputLine outputline : outputList) {
                    final Student student = outputline._student;
                    String showName;
                    try {
                    	showName = _param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
                    } catch (Throwable e) {
                    	showName = student._name;
                    }
                    final SubclassAttendance subclass = outputline._subclass;
                    if (_param._subclassnameMap.get(subclass._subclasscd) == null) {
                    	if (!nosubclassSet.contains(subclass._subclasscd)) {
                    		log.info(" no name subclass : " + subclass._subclasscd);
                    		nosubclassSet.add(subclass._subclasscd);
                    	}
                        continue;
                    }
                    final String subclassname = _param._subclassnameMap.get(subclass._subclasscd);
                    line += 1;
                    if (line > MAX_LINE) {
                        page += 1;
                        line -= MAX_LINE;
                    }
                    
                    svf.VrsOut("NENDO", nendo + " " + title);
                    String subtitle = null;
                    if ("1".equals(_param._output)) {
                        subtitle = "（欠課時数が　" + _param._kekkaJisu + "時数 超過している科目）";
                    } else if ("2".equals(_param._output)) {
                        subtitle = "（授業時数から　" + _param._bunshi + " / " + _param._bunbo + " 超過している科目）";
                    } else if ("3".equals(_param._output)) {
                        subtitle = "（予定時数から　" + _param._bunshi + " / " + _param._bunbo + " 超過している科目）";
                    }
                    svf.VrsOut("SUBTITLE", subtitle); // サブタイトル
					svf.VrsOut("DATE", loginDateFormat); // 印刷日
					svf.VrsOut("PERIOD", sdateFormat + " 〜 " + edateFormat); // 期間

                    svf.VrsOut("PAGE", String.valueOf(page)); // ページ
                    svf.VrsOut("HR_NAME", student._hrName); // 年組
                    svf.VrsOut("ATTEND_NO", NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : student._attendNo); // 出席番号
                    if (!keepNo.equals(student._schregno) || line == 1) {
                        if ("1".equals(_param._use_SchregNo_hyoji)) {
                            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
                            svf.VrsOut("NAME2", showName); // 名前
                        } else  {
                            svf.VrsOut("NAME", showName); // 名前
                        }
                        keepNo = student._schregno;
                    }
                    
                    svf.VrsOut(getMS932ByteLength(subclassname) > 26 ? "SUBCLASS2" : "SUBCLASS", String.valueOf(subclassname)); // 科目名
                    svf.VrsOut("CLASS_COUNT", String.valueOf(subclass._shussekiSubekiJisu)); // 授業時数
                    if (null != subclass._kekkaJisu) {
                    	svf.VrsOut("ABSENT_COUNT", subclass._kekkaJisu.toString()); // 欠課時数
                    }
                    svf.VrEndRecord();
                    _hasdata = true;
                }
            }
        }
    }

    /**
     * 生徒と1日出欠、科目別出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private List<OutputLine> getOutputList(final DB2UDB db2, final String gradeHrclass) {
        final List<Student> studentList = new ArrayList();        
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_ENG, ");
        stb.append("     T2.SEX ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
        stb.append("         T1.YEAR = T3.YEAR ");
        stb.append("         AND T1.SEMESTER = T3.SEMESTER ");
        stb.append("         AND T1.GRADE = T3.GRADE ");
        stb.append("         AND T1.HR_CLASS = T3.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON ");
        stb.append("         GDAT.YEAR = T1.YEAR ");
        stb.append("         AND GDAT.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");
        
        String schoolKind = null;
        // HRの生徒を取得
        for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
        	final Student st = new Student(
        			KnjDbUtils.getString(row, "SCHREGNO"),
        			KnjDbUtils.getString(row, "SCHOOL_KIND"),
        			KnjDbUtils.getString(row, "HR_NAME"),
        			KnjDbUtils.getString(row, "ATTENDNO"), 
        			KnjDbUtils.getString(row, "NAME"),
        			KnjDbUtils.getString(row, "NAME_ENG"),
        			KnjDbUtils.getString(row, "SEX"),
        			KnjDbUtils.getString(row, "GRADE"),
        			KnjDbUtils.getString(row, "COURSECD"),
        			KnjDbUtils.getString(row, "MAJORCD"),
        			KnjDbUtils.getString(row, "COURSECODE")
        			);
        	studentList.add(st);
        	if (null == schoolKind && null != KnjDbUtils.getString(row, "SCHOOL_KIND")) {
        		schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
        	}
        }

        KNJSchoolMst _knjSchoolMst = null;
        try {
            if (_param._hasSCHOOL_MST_SCHOOL_KIND) {
            	_knjSchoolMst = _param._knjSchoolMstMap.get(schoolKind);
            } else {
            	_knjSchoolMst = _param._knjSchoolMstMap.get("00");
            }
            
            final String sql = AttendAccumulate.getAttendSubclassSql(
                        false,
                        _param.defineSchool,
                        _knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param.ESEMESTER,
                        (String) _param.hasuuMap.get("attendSemesInState"),
                        _param.periodInState,
                        null, // (String) hasuuMap.get("befDayFrom"),
                        null, // (String) hasuuMap.get("befDayTo"),
                        _param._sdate, // (String) hasuuMap.get("aftDayFrom"),
                        _param._edate, // (String) hasuuMap.get("aftDayTo"),
                        gradeHrclass.substring(0, 2),
                        gradeHrclass.substring(2),
                        null,
                        _param._attendParamMap
                        );
            log.debug("attendsubclass sql = " + sql);
            _param._psSubclassAttendance = db2.prepareStatement(sql);
            
            if (!"1".equals(_param._output)) {
                _param._bunbo = NumberUtils.isDigits(_knjSchoolMst._risyuBunbo) ? Integer.parseInt(_knjSchoolMst._risyuBunbo) : 1;
                _param._bunshi = NumberUtils.isDigits(_knjSchoolMst._risyuBunsi) ? Integer.parseInt(_knjSchoolMst._risyuBunsi) : 0;
                _param._bunbo = 0 >= _param._bunbo ? 1 : _param._bunbo; // 除算対応
                _param._bunshi = 0 > _param._bunshi ? 0 : _param._bunshi; // 除算対応
            }

            int kekkaScale = null != _knjSchoolMst && ("3".equals(_knjSchoolMst._absentCov) || "4".equals(_knjSchoolMst._absentCov)) ? 1 : 0;
            final Integer zero = Integer.valueOf(0);
            for (final Map<String, String> row : KnjDbUtils.query(db2, _param._psSubclassAttendance, null)) {
                if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER")) ) {
                    continue;
                }
                final Student student = getStudent(KnjDbUtils.getString(row, "SCHREGNO"), studentList);
                if (null == student) {
                    continue;
                }
                if (null != getSubclass(KnjDbUtils.getString(row, "SUBCLASSCD"), student._subclassList)) {
                    continue;
                }
                BigDecimal sick2 = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                if (null != sick2) {
                	sick2 = sick2.setScale(kekkaScale, BigDecimal.ROUND_HALF_UP);
                }
				final SubclassAttendance subclass = new SubclassAttendance(KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getInt(row, "MLESSON", zero), sick2);
                student._subclassList.add(subclass);
            }
        } catch (SQLException ex) {
            log.error("SQL exception!", ex);
        } finally {
        	DbUtils.closeQuietly(_param._psSubclassAttendance);
        }
        
        
        final List<OutputLine> outputList = new ArrayList();
        for (final Student student : studentList) {
            for (final SubclassAttendance subclass : student._subclassList) {
                double limit = 9999;
                if ("1".equals(_param._output)) {
                    limit = _param._kekkaJisu.doubleValue();
                } else if ("2".equals(_param._output)) {
                    limit = subclass._shussekiSubekiJisu * (double) _param._bunshi / _param._bunbo;
                } else if ("3".equals(_param._output)) {
                    final String key = student._courseCd + student._majorCd + student._grade + student._courseCode + subclass._subclasscd;
                    final CreditMst creditMst = _param._creditMstMap.get(key);
                    if (null != creditMst && NumberUtils.isDigits(creditMst._credits) && null != _knjSchoolMst && NumberUtils.isDigits(_knjSchoolMst._jituSyusu)) {
                        final int total = Integer.parseInt(creditMst._credits) * Integer.parseInt(_knjSchoolMst._jituSyusu);
                        limit = total * (double) _param._bunshi / _param._bunbo;
                    }
                }
                if (limit >= 0 && subclass._kekkaJisu.doubleValue() > limit) {
                    outputList.add(new OutputLine(student, subclass));
                }
            }
        }
        log.info(" output list size = " + outputList.size());
        return outputList;
    }
    
    /**
     * studentList から学籍番号が schregno の生徒を取得
     * @param schregno 生徒の学籍番号
     * @param studentList 生徒のリスト
     * @return 対象の生徒
     */
    private Student getStudent(final String schregno, final List<Student> studentList) {
        for (final Student student : studentList) {
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }
    
    /**
     * subclassList から科目コードが subclasscd の科目を取得
     * @param subclasscd 科目コード
     * @param subclassList 科目のリスト
     * @return 対象の科目
     */
    private SubclassAttendance getSubclass(final String subclasscd, final List<SubclassAttendance> subclassList) {
        for (final SubclassAttendance subclass : subclassList) {
            if (subclass._subclasscd.equals(subclasscd)) {
                return subclass;
            }
        }
        return null;
    }
    
    private static class OutputLine {
        final Student _student;
        final SubclassAttendance _subclass;
        OutputLine(final Student student, final SubclassAttendance subclass) {
            _student = student;
            _subclass = subclass;
        }
        public String toString() {
        	return "OutputLine(" + _student._schregno + ", " + _subclass._subclasscd + ")";
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _schoolKind;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _nameEng;
        final String _sex;
        final String _grade;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final List<SubclassAttendance> _subclassList;

        public Student(
                final String schregno,
                final String schoolKind,
                final String hrName,
                final String attendNo,
                final String name,
                final String nameEng,                
                final String sex,
                final String grade,
                final String courseCd,
                final String majorCd,
                final String courseCode
        ) {
            _schregno = schregno;
            _schoolKind = schoolKind;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _nameEng = nameEng;
            _sex = sex;
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _subclassList = new ArrayList();
        }
    }
    
    private static class SubclassAttendance {
        final String _subclasscd;
        final int _shussekiSubekiJisu;
        final BigDecimal _kekkaJisu;
        public SubclassAttendance(final String subclasscd, final int jugyoJisu, final BigDecimal kekkaJisu) {
            _subclasscd = subclasscd;
            _shussekiSubekiJisu = jugyoJisu;
            _kekkaJisu = null == kekkaJisu ? new BigDecimal(0) : kekkaJisu;
        }
    }
    
    private static class CreditMst {
        final String _coursecd;
        final String _majorcd;
        final String _grade;
        final String _coursecode;
        final String _classcd;
        final String _subclasscd;
        final String _credits;
        final String _absenceHigh;
        final String _getAbsenceHigh;
        final String _absenceWarn;
        final String _absenceWarn2;
        final String _absenceWarn3;

        CreditMst(
                final String coursecd,
                final String majorcd,
                final String grade,
                final String coursecode,
                final String classcd,
                final String subclasscd,
                final String credits,
                final String absenceHigh,
                final String getAbsenceHigh,
                final String absenceWarn,
                final String absenceWarn2,
                final String absenceWarn3
        ) {
            _coursecd = coursecd;
            _majorcd = majorcd;
            _grade = grade;
            _coursecode = coursecode;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _credits = credits;
            _absenceHigh = absenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
            _absenceWarn = absenceWarn;
            _absenceWarn2 = absenceWarn2;
            _absenceWarn3 = absenceWarn3;
        }
    }
    
    private static class Param {
        final String _year;
        final String _semester;
        final String _sdate;
        final String _edate;
        final String _loginDate;
        final String[] _gradeHrclass;
        final String _output;
        final Integer _kekkaJisu;
        
		// 出欠の情報
        KNJDefineSchool _definecode;
        KNJDefineCode definecode0;
        String z010Name1;
        final String SSEMESTER = "1";
        final String ESEMESTER = "9";
        String periodInState;
        Map attendSemesMap;
        Map hasuuMap;
        KNJDefineSchool defineSchool;
        final Map _attendParamMap;

        private PreparedStatement _psSubclassAttendance; // 時数単位の出欠
        private Map<String, KNJSchoolMst> _knjSchoolMstMap = new HashMap<String, KNJSchoolMst>();
        private int _bunbo = 1;
        private int _bunshi = 0;
        private Map<String, String> _subclassnameMap;
        private Map<String, CreditMst> _creditMstMap;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;
        
        /** 生徒氏名（英語・日本語）切替処理用 */
        final String _staffCd;
        StaffInfo _staffInfo;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        
        Param(final HttpServletRequest request, final DB2UDB db2) throws Exception {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _edate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED");
            _output = request.getParameter("OUTPUT");
            _attendParamMap = new HashMap();
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));
            _kekkaJisu = (NumberUtils.isDigits(request.getParameter("KEKKA_JISU")) ? new Integer(request.getParameter("KEKKA_JISU")) : new Integer(0));  
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            try {
            	_staffInfo = new StaffInfo(db2, _staffCd);
            } catch (Throwable t) {
            	log.error(t);
            }
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            try {
            	if (_hasSCHOOL_MST_SCHOOL_KIND) {
            		for (final String schoolKind : KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT DISTINCT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' "), "SCHOOL_KIND")) {
            			final Map paramMap = new HashMap();
            			paramMap.put("SCHOOL_KIND", schoolKind);
            			_knjSchoolMstMap.put(schoolKind, new KNJSchoolMst(db2, _year, paramMap));
            		}
            	} else {
        			_knjSchoolMstMap.put("00", new KNJSchoolMst(db2, _year));
            	}
            } catch (Exception e) {
            	log.error("exception!", e);
            }
            setAttendSubclass(db2);
            setSubclassname(db2);
            if ("3".equals(_output)) {
                setCreditMstMap(db2);
            }
        }
        
        private void setAttendSubclass(final DB2UDB db2) throws SQLException, ParseException {
            // 出欠の情報
            _definecode = new KNJDefineSchool();       //各学校における定数等設定
            _definecode.defineCode (db2, _year);
            definecode0 = new KNJDefineCode();
            definecode0.defineCode(db2, _year);         //各学校における定数等設定
            z010Name1 = getZ010Name1(db2);
            periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, ESEMESTER);
            attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
            hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap,  _sdate, _edate);
            log.debug(" hasuuMap = " + hasuuMap);
            
            defineSchool = new KNJDefineSchool();
            defineSchool.defineCode(db2, _year);
            
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String getZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }
        
        private void setSubclassname(final DB2UDB db2) {
            String sql;
            if (KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD")) {
                sql = "SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, SUBCLASSNAME FROM SUBCLASS_MST ";
            } else {
                sql = "SELECT SUBCLASSCD, SUBCLASSNAME FROM SUBCLASS_MST ";
            }

            _subclassnameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "SUBCLASSCD", "SUBCLASSNAME");
        }
        
        private void setCreditMstMap(final DB2UDB db2) {
			final boolean _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD");
            _creditMstMap = new HashMap();
        	final String sql = "SELECT T1.* FROM CREDIT_MST T1 WHERE YEAR = '" + _year + "' ";
        	
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                final String subclasscd;
                if (_useCurriculumcd) {
                    subclasscd = classcd + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
                } else {
                    subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }
                final String credits = KnjDbUtils.getString(row, "CREDITS");
                final String absenceHigh = KnjDbUtils.getString(row, "ABSENCE_HIGH");
                final String getAbsenceHigh = KnjDbUtils.getString(row, "GET_ABSENCE_HIGH");
                final String absenceWarn = KnjDbUtils.getString(row, "ABSENCE_WARN");
                final String absenceWarn2 = KnjDbUtils.getString(row, "ABSENCE_WARN2");
                final String absenceWarn3 = KnjDbUtils.getString(row, "ABSENCE_WARN3");
        
                final CreditMst creditMst = new CreditMst(coursecd, majorcd, grade, coursecode, classcd, subclasscd, credits, absenceHigh, getAbsenceHigh, absenceWarn, absenceWarn2, absenceWarn3);
                final String key = coursecd + majorcd + grade + coursecode + subclasscd;
                _creditMstMap.put(key, creditMst);
            }
        }
    }
}
