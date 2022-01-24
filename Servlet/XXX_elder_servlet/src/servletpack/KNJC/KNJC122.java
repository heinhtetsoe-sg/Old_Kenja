// kanji=漢字
/*
 * $Id: f1b34b840ef1db4af1b026452383df66f12e0c9c $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] クラス別欠課数リスト
 */

public class KNJC122 {

    private static final Log log = LogFactory.getLog(KNJC122.class);
    private static final String SPECIAL_ALL = "999";
    private static final String SPECIAL_SUBCLASSCD = "SPECIAL";
    private Param _param;
    private boolean _hasData;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

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
            _param = createParam(request, db2);

            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

            _hasData = false;
            for (int i = 0; i < _param._gradeHrclass.length; i++) {
                final String gradeHrclass = _param._gradeHrclass[i];

                _param.load(db2, gradeHrclass);

                final List studentList = getStudentList(db2, gradeHrclass);

                // 印刷処理
                printMain(db2, svf, studentList);
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            // 終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            if (null != _param) {
                DbUtils.closeQuietly(_param._psAttendance);
                DbUtils.closeQuietly(_param._psSchregAbsenceHigh);
                DbUtils.closeQuietly(_param._psSchregAbsenceHighSpecialLesson);
                DbUtils.closeQuietly(_param._psSchregAbsenceHighSubclass);
            }
            svf.VrQuit();
            db2.close();
        }
    }

    /**
     *  印刷処理
     */
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List studentList) {
        // 生徒がいなければ処理をスキップ
        if (studentList.size() == 0) {
        	return;
        }

        log.debug("nonedata = " + _hasData);

        svf.VrSetForm("KNJC122.frm", 4);
        svf.VrsOut("NENDO", _param._nendo);
        svf.VrsOut("DATE", _param._loginDateStr);
        svf.VrsOut("PERIOD", _param._attendDateRange);
        svf.VrsOut("SUBTITLE", _param._isCheckSyutoku ? "（修得上限値警告）" : "（履修上限値警告）");

        final int MAX_LINE_PER_PAGE = 50;

        int line = 1;
        String keepNo = "";

        for (final Iterator it2 = studentList.iterator(); it2.hasNext();) {
            final Student student = (Student) it2.next();
            // log.debug(student);
            if (!student.hasData()) {
                continue;
            }
            // 科目ごとに出欠表示
            for (final Iterator it = student._subclassAttendanceMap.keySet().iterator(); it.hasNext();) {
                if (line > MAX_LINE_PER_PAGE) {
                    line = 1;
                }
                final String subclassCd = (String) it.next();
                final SubclassAttendance sa = (SubclassAttendance) student._subclassAttendanceMap.get(subclassCd);

                boolean output = false;
                double markDouble = 0;
                if (sa._kekka.intValue() != 0) {
                    if (_param._isSundaikoufu) {
                        if (isSundaiKekkaOver(sa._kekka.doubleValue(), sa._mlesson)) {
                            output = true;
                            markDouble = new BigDecimal(sa._mlesson).divide(new BigDecimal(6), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
                        }
                    } else if (!_param._isCheckSyutoku && 0 < sa._compAbsenceHighTyui.intValue() && sa._compAbsenceHighTyui.compareTo(sa._kekka) < 0) {
                        output = true;
                        markDouble = sa._compAbsenceHighTyui.doubleValue();
                    } else if (_param._isCheckSyutoku && (0 < sa._getAbsenceHighTyui.intValue() && sa._getAbsenceHighTyui.compareTo(sa._kekka) < 0)) {
                        output = true;
                        markDouble = sa._getAbsenceHighTyui.doubleValue();
                    }
                }
                if (_param._isOutputDebug) {
                	log.info(" " + student._schregno + " " + subclassCd + " kekka = " + sa._kekka + ", (comp, get) = (" + sa._compAbsenceHighTyui + ", " + sa._getAbsenceHighTyui + "), output = " + output);
                }
                if (output) {
                    if (sa._kekka.doubleValue() != 0) {
                        log.debug(" student = " + student._name + " subclass = " + sa._subclassName + " kekka = " + sa._kekka + " (" + sa._getAbsenceHighTyui + " , " + sa._compAbsenceHighTyui + ")");
                    }
                    svf.VrsOut("TEACHER_NAME", sa._staffName);
                    svf.VrsOut(sa._subclassName != null && sa._subclassName.length() > 13 ? "SUBCLASS2" : "SUBCLASS", sa._subclassName);
                    // svf.VrsOut("CLASS_COUNT",  String.valueOf(_mlesson));
                    if (_param._isSundaikoufu) {
                        svf.VrsOut("CLASS_COUNT",  String.valueOf(sa._mlesson));
                    } else {
                        svf.VrsOut("CLASS_COUNT",  String.valueOf(sa._yearMlesson));
                    }
                    svf.VrsOut("ABSENCE_COUNT",  String.valueOf(sa._ketuji));
                    svf.VrsOut("LATE_EARLY_COUNT",  String.valueOf(sa._lateEarly));
                    final String kekkas = ("2".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov)) ? sa._kekka.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : sa._kekka.setScale(0, BigDecimal.ROUND_FLOOR).toString();
                    svf.VrsOut("ABSENT_COUNT" + (kekkas.length() > 3 ? "_2" : ""), kekkas);
                    svf.VrsOut("WARNING_DIV", 0.0 == markDouble ? "" : String.valueOf((int) markDouble));

                    _hasData = true;
                    svf.VrsOut("HR_NAME", student._hrName);
                    svf.VrsOut("ATTEND_NO", Integer.valueOf(student._attendNo).toString());
                    if (!keepNo.equals(student._schregno) || line == 1) {
                        if ("1".equals(_param._use_SchregNo_hyoji)) {
                            svf.VrsOut("SCHREGNO", student._schregno);
                            svf.VrsOut("NAME2", student._name);
                        } else  {
                            svf.VrsOut("NAME", student._name);
                        }
                        keepNo = student._schregno;
                    }
                    svf.VrsOut("GRADE", String.valueOf(Integer.valueOf(student._grade)));
                    svf.VrEndRecord();
                    line += 1;
                    // log.debug(" " + student._name + "  科目=" + sa._subclassName + "  欠課数=" + sa._kekka + " 履修上限値注意=" + sa._compAbsenceHighTyui + " 修得上限値注意=" + sa._getAbsenceHighTyui);
                }
            }
            if (line > MAX_LINE_PER_PAGE) {
                line = 1;
            }
            // 特別活動の出欠表示
            final SpecialSubclassAttendance spAtt = student._specialSubclassAttendance;
            final BigDecimal absent = spAtt.getTotalJisu(spAtt._specialSubclassKekka, _param, "absent");
//          final BigDecimal mlesson = spAtt.getTotalJisu(_specialSubclassMlesson, _param, "mlesson");
            final String yearMlesson;
            if (!_param.isHoutei()) {
                yearMlesson = StringUtils.defaultString(spAtt._group999jituLesson);
            } else {
                yearMlesson = spAtt.getTotalJisu(spAtt._yearSpecialSubclassMlesson, _param, "yearMlesson").toString();
            }

            boolean output = false;
            int markInt = 0;
            if (absent.intValue() != 0) {
                if (0 < spAtt._compAbsenceHighTyui.intValue() && spAtt._compAbsenceHighTyui.compareTo(absent) < 0) {
                    output = true;
                    markInt = spAtt._compAbsenceHighTyui.intValue();
                }
                // log.debug(_schregno + " output = " + output + " , absent = " + absent + " , absencehigh = " + _compAbsenceHighTyui);
            }
            if (output) {
                svf.VrsOut(spAtt._subclassName != null && spAtt._subclassName.length() > 13 ? "SUBCLASS2" : "SUBCLASS", spAtt._subclassName);
                // svf.VrsOut("CLASS_COUNT",  String.valueOf(mlesson));
                if (NumberUtils.isNumber(yearMlesson) && Double.parseDouble(yearMlesson) > 0.0) {
                    svf.VrsOut("CLASS_COUNT",  String.valueOf(yearMlesson));
                }

//                svf.VrsOut("ABSENCE_COUNT",  String.valueOf(ketuji));
//                svf.VrsOut("LATE_EARLY_COUNT",  String.valueOf(lateEarly));
                final String absents = ("2".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov)) ? absent.setScale(1, BigDecimal.ROUND_HALF_UP).toString() : absent.setScale(0, BigDecimal.ROUND_FLOOR).toString();
                svf.VrsOut("ABSENT_COUNT" + (absents.length() > 3 ? "_2" : ""), absents);
                svf.VrsOut("WARNING_DIV", 0 == markInt ? "" : String.valueOf(markInt));

                _hasData = true;
                svf.VrsOut("HR_NAME", student._hrName);
                svf.VrsOut("ATTEND_NO", Integer.valueOf(student._attendNo).toString());
                if (!keepNo.equals(student._schregno) || line == 1) {
                    if ("1".equals(_param._use_SchregNo_hyoji)) {
                        svf.VrsOut("SCHREGNO", student._schregno);
                        svf.VrsOut("NAME2", student._name);
                    } else  {
                        svf.VrsOut("NAME", student._name);
                    }
                    keepNo = student._schregno;
                }
                svf.VrsOut("GRADE", String.valueOf(Integer.valueOf(student._grade)));
                svf.VrEndRecord();
                line += 1;
            }
//            // 改ページ処理
//            for (; col <= MAX_LINE_PER_PAGE; col++) {
//                svf.VrsOut("SUBCLASS", "\n");
//                svf.VrEndRecord();
//            }

            // 特別活動
            if (line > MAX_LINE_PER_PAGE) {
                line = 1;
            }
        }
    }

    /*
     * 欠課数オーバー(授業時数の1/6以上)か
     */
    private static boolean isSundaiKekkaOver(final double kekka, final int jisu) {
        if (0 == kekka || 0 == jisu) { return false; }
        return new BigDecimal(kekka).compareTo(new BigDecimal(jisu).divide(new BigDecimal(6), 10, BigDecimal.ROUND_HALF_UP)) >= 0;
    }

    /**
     * 生徒と1日出欠、科目別出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private List getStudentList(final DB2UDB db2, final String gradeHrclass) {
        final List studentList = new ArrayList();
        // HRの生徒を取得
        final String sqlSchregRegdDat = sqlSchregRegdDat(gradeHrclass);
        //log.debug("schreg_regd_dat sql = " + sqlSchregRegdDat);
        for (final Iterator it = KnjDbUtils.query(db2, sqlSchregRegdDat).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            Student st = new Student(
                    KnjDbUtils.getString(row, "SCHREGNO"),
                    KnjDbUtils.getString(row, "HR_NAME"),
                    KnjDbUtils.getString(row, "ATTENDNO"),
                    KnjDbUtils.getString(row, "NAME"),
                    KnjDbUtils.getString(row, "SEX"),
                    KnjDbUtils.getString(row, "GRADE"),
                    KnjDbUtils.getString(row, "COURSECD"),
                    KnjDbUtils.getString(row, "MAJORCD"),
                    KnjDbUtils.getString(row, "COURSECODE"));
            studentList.add(st);
        }
        
        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
        	final Student student = (Student) it.next();
        	studentMap.put(student._schregno, student);
        }

        setSubclassAttendance(db2, studentMap);

        setSubclassAbsenceHigh(db2, studentMap);

        setSpecialSubclassAbsenceHigh(db2, studentMap);

        setSubclassStaffName(db2, gradeHrclass, studentMap);

        return studentList;
    }

    private void setSubclassAttendance(final DB2UDB db2, final Map studentMap) {
        // 時数単位
        for (final Iterator it = KnjDbUtils.query(db2, _param._psAttendance, null).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
			final Student student = (Student) studentMap.get(schregno);
            if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                continue;
            }

            if (KnjDbUtils.getString(row, "SPECIAL_GROUP_CD") != null) {
                student.addSpecialSubclassAttendance(row, _param);
            } else {
                student.addSubclassAttendance(row, _param);
            }
        }
    }

    private void setSubclassStaffName(final DB2UDB db2, final String gradeHrclass, final Map studentMap) {
        final String sql = sqlStaffname(gradeHrclass);
        log.debug(" staffname sql = " + sql);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
            if (student == null) {
                continue;
            }
            final SubclassAttendance sa = student.getSubclassAttendance(KnjDbUtils.getString(row, "SUBCLASSCD"));
            if (sa == null) {
                continue;
            }
            sa._staffName = KnjDbUtils.getString(row, "STAFFNAME");
        }
    }

    private void setSubclassAbsenceHigh(final DB2UDB db2, final Map studentMap) {
    	final BigDecimal bd0 = new BigDecimal(0);
        // 上限値
        for (final Iterator it = KnjDbUtils.query(db2, _param._psSchregAbsenceHighSubclass, null).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
            final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
            if (student == null || subclassCd == null || student.getSubclassAttendance(subclassCd) == null) {
                continue;
            }

            final SubclassAttendance sa = student.getSubclassAttendance(subclassCd);

            if (KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH_TYUI", bd0).doubleValue() > 0) {
                sa._compAbsenceHighTyui = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH_TYUI", bd0);
            }
            if (KnjDbUtils.getBigDecimal(row, "GET_ABSENCE_HIGH_TYUI", bd0).doubleValue() > 0.0) {
                sa._getAbsenceHighTyui = KnjDbUtils.getBigDecimal(row, "GET_ABSENCE_HIGH_TYUI", bd0);
            }
        }
    }

    private void setSpecialSubclassAbsenceHigh(final DB2UDB db2, final Map studentMap) {
    	final BigDecimal bd0 = new BigDecimal(0);
        // 上限値
        for (final Iterator it = KnjDbUtils.query(db2, _param._psSchregAbsenceHigh, null).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
            if (student == null) {
                continue;
            }

            if (KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH_TYUI", bd0).doubleValue() != 0.0) {
                student._specialSubclassAttendance._compAbsenceHighTyui = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH_TYUI", bd0);
            }
        }

        if (!_param.isHoutei()) {
            for (final Iterator it = KnjDbUtils.query(db2, _param._psSchregAbsenceHighSpecialLesson, null).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (student == null) {
                    continue;
                }
                student._specialSubclassAttendance._group999jituLesson = KnjDbUtils.getString(row, "LESSON");
            }
        }
    }

    /** 学生を得るSQL */
    private String sqlSchregRegdDat(final String gradeHrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
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
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.info(" $Revision: 63195 $ $Date: 2018-11-02 11:44:02 +0900 (金, 02 11 2018) $ ");
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _attendStartDate;
        final String _attendEndDate;
        final String _loginDate;
        final String _grade;
        final String[] _gradeHrclass;
        final String _nendo;
        final String _loginDateStr;
        final String _attendDateRange;

        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;

        private PreparedStatement _psAttendance; // 時数単位の出欠
        private PreparedStatement _psSchregAbsenceHighSubclass; // 欠課数上限値
        private PreparedStatement _psSchregAbsenceHigh; // 欠課数上限値
        private PreparedStatement _psSchregAbsenceHighSpecialLesson; // 欠課数上限値
        private Map _subclassNameMap;
        private KNJSchoolMst _knjSchoolMst;
        /** C005：欠課換算法修正 */
        private Map _subClassC005 = new HashMap();
        private List _zenkiKamokuSubclasscdList;

        private boolean _isCheckSyutoku;

        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String _useTestCountflg;

        private String _attendEndDateSemester;
        private boolean _hasSchChrDatExecuteDiv;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        final String _useSchool_KindField;
        final String _use_school_detail_gcm_dat;
        final String SCHOOLCD;
        final String SCHOOLKIND;
        final String _z010Name1;
        final boolean _isSundaikoufu;
        final boolean _isOutputDebug;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _attendStartDate = request.getParameter("SDATE").replace('/', '-');
            _attendEndDate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED");
            for (int i = 0; i < _gradeHrclass.length; i++) {
                _gradeHrclass[i] = _grade + _gradeHrclass[i];
            }
            _nendo = KNJ_EditDate.h_format_JP_N(db2, _year + "-01-01") + "度"; // デフォルトは和暦
            _loginDateStr = getDateString(db2, _loginDate);
            _attendDateRange = getDateString(db2, _attendStartDate) + " ～ " + getDateString(db2, _attendEndDate);

            _isCheckSyutoku = "2".equals(request.getParameter("OUTPUT"));
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _z010Name1 = setZ010Name1(db2);
            _hasSchChrDatExecuteDiv = KnjDbUtils.setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
            try {
                setAttendEdateSemester(db2);
                setSubclassMstMap(db2);
                loadNameMstC005(db2);
                loadNameMstC042(db2);
                setZenkiKamokuList(db2);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            _isSundaikoufu = "sundaikoufu".equals(_z010Name1);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC122' AND NAME = '" + propName + "' "));
        }

        public boolean isHoutei() {
            if ("3".equals(_knjSchoolMst._jugyouJisuFlg)) {
                return "9".equals(_semester) || null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester);
            }
            return "1".equals(_knjSchoolMst._jugyouJisuFlg) || null == _knjSchoolMst._jugyouJisuFlg;
        }

        private KNJDefineSchool setClasscode0(final DB2UDB db2) {
        	KNJDefineSchool definecode = null;
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }
        
        private void setZenkiKamokuList(final DB2UDB db2) {
        	if (KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_DETAIL_DAT", null)) {
            	final String sqlZenkiKamoku = " SELECT " + ("1".equals(_useCurriculumcd) ? " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD " : " SUBCLASSCD ")  + " FROM SUBCLASS_DETAIL_DAT WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1' ";
            	_zenkiKamokuSubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sqlZenkiKamoku), "SUBCLASSCD");
        	} else {
        		_zenkiKamokuSubclasscdList = new ArrayList();
        	}
        }

        private void load(final DB2UDB db2, final String gradeHrclass) {
            try {
                // 出欠の情報
                final KNJDefineSchool _definecode = new KNJDefineSchool();       //各学校における定数等設定
                _definecode.defineCode (db2, _year);
                final KNJDefineSchool definecode0 = setClasscode0(db2);
                final String sdate = getSDate(db2, _year, _attendStartDate);
                final String SSEMESTER = "1";
                final String ESEMESTER = "9";
                final String periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, ESEMESTER);
                final Map attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, _z010Name1, _year);
                final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap,  sdate, _attendEndDate);
                log.debug(" hasuuMap = " + hasuuMap);

                final KNJDefineSchool defineSchool = new KNJDefineSchool();
                defineSchool.defineCode(db2, _year);

                try {
                    final Map paramMap = new HashMap();
                    if ("1".equals(_useSchool_KindField)) {
                        paramMap.put("SCHOOL_KIND", SCHOOLKIND);
                        paramMap.put("SCHOOLCD", SCHOOLCD);
                    }
                    if ("1".equals(_use_school_detail_gcm_dat)) {
                        paramMap.put("TABLENAME", "V_SCHOOL_GCM_MST");
                    }
//                    paramMap.put("outputDebug", "1");
                    _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
                } catch (Throwable e) {
                    log.fatal("exception!", e);
                    _knjSchoolMst = new KNJSchoolMst(db2, _year);
                }

                // 時数単位
                String sql = getAttendSubclassSql(
                        defineSchool,
                        _knjSchoolMst,
                        _year,
                        SSEMESTER,
                        ESEMESTER,
                        (String) hasuuMap.get("attendSemesInState"),
                        periodInState,
                        (String) hasuuMap.get("befDayFrom"),
                        (String) hasuuMap.get("befDayTo"),
                        (String) hasuuMap.get("aftDayFrom"),
                        (String) hasuuMap.get("aftDayTo"),
                        gradeHrclass.substring(0, 2),
                        gradeHrclass.substring(2),
                        null,
                        _useCurriculumcd,
                        _useVirus,
                        _useKoudome,
                        _useTestCountflg
                        );
                log.debug("get AttendSubclass sql = " + sql);
                _psAttendance = db2.prepareStatement(sql);

                log.debug(" 授業時数管理区分:" + (isHoutei() ? "法定授業数管理" : "実授業数管理"));
                // 欠課数上限値
                if (isHoutei()) {
                    sql = getHouteiJisuSql(null, this, gradeHrclass, false);
                } else {
                    sql = getJituJisuSql(null, this, gradeHrclass, false);
                }
                log.debug("get AbsenceHighSubclass sql = " + sql);
                _psSchregAbsenceHighSubclass = db2.prepareStatement(sql);

                // 欠課数上限値
                if (isHoutei()) {
                    sql = getHouteiJisuSql(null, this, gradeHrclass, true);
                } else {
                    sql = getJituJisuSql(null, this, gradeHrclass, true);
                }
                log.debug("get AbsenceHigh sql = " + sql);
                _psSchregAbsenceHigh = db2.prepareStatement(sql);

                if (!isHoutei()) {
                    sql = getSchregAbsenceHighSpecialSql(this, gradeHrclass);
                    _psSchregAbsenceHighSpecialLesson = db2.prepareStatement(sql);
                }


            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
        }

        /**
         * 欠課換算法修正
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC005(final DB2UDB db2) {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'"; // TODOO: ??
        	final List rowList = KnjDbUtils.query(db2, sql);
            if (rowList.size() > 0) {
            	final Map row = (Map) rowList.get(0);
                final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String is = KnjDbUtils.getString(row, "NAMESPARE1");
                log.debug("(名称マスタ C005):科目コード=" + subclassCd);
                _subClassC005.put(subclassCd, is);
            }
        }

        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC042(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
        	final List rowList = KnjDbUtils.query(db2, sql);
            if (rowList.size() > 0) {
                _absenceWarnIsUnitCount = "1".equals(KnjDbUtils.getOne(rowList));
                log.debug("(名称マスタ C042) =" + _absenceWarnIsUnitCount);
            }
        }

        private String getSDate(DB2UDB db2, String year, String defaultSdate) {
            String sdate = defaultSdate;
        	final List rowList = KnjDbUtils.query(db2, "SELECT SDATE FROM SEMESTER_MST WHERE SEMESTER = '1' AND YEAR = '" + year + "' ");
        	if (rowList.size() > 0) {
                sdate = KnjDbUtils.getOne(rowList);
                if (null != sdate) {
                	if (sdate.compareTo(defaultSdate) <= 0) {
                		sdate = defaultSdate;
                	}
                }
        	}
            return sdate;
        }

        private String getStaffNameString(final String staffName1, final String staffName2, final String staffName3) {
            final String[] staffNames = {staffName1, staffName2, staffName3};
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (int i = 0; i < staffNames.length; i++) {
                if (staffNames[i] != null) {
                    stb.append(comma + staffNames[i]);
                    comma = "、";
                }
            }
            return stb.toString();
        }

        private String getDateString(final DB2UDB db2, final String date) {
            final String gengo = KNJ_EditDate.h_format_JP_N(db2, date);
            final int month = Integer.parseInt(date.substring(5, 7));
            final int day = Integer.parseInt(date.substring(8, 10));

            final String monthStr = ((month < 10) ? " " : "")  + String.valueOf(month);
            final String dayStr = ((day < 10) ? " " : "")  + String.valueOf(day);

            return gengo + String.valueOf(monthStr) + "月" + String.valueOf(dayStr) + "日";
        }

        private void setSubclassMstMap(final DB2UDB db2) throws SQLException {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("   SUBCLASSCD AS SUBCLASSCD, SUBCLASSNAME ");
            stb.append("FROM ");
            stb.append("   SUBCLASS_MST ");

            _subclassNameMap = new TreeMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SUBCLASSCD", "SUBCLASSNAME"));
        }

        private String setAttendEdateSemester(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM SEMESTER_MST T1 ");
            stb.append(" LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND (('" + _attendEndDate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR ('" + _attendEndDate + "' BETWEEN T1.EDATE AND VALUE(T2.SDATE, '9999-12-30'))) ");

            final List rowList = KnjDbUtils.query(db2, stb.toString());
            if (rowList.size() > 0) {
                _attendEndDateSemester = KnjDbUtils.getString(KnjDbUtils.firstRow(rowList), "SEMESTER");
            }
            return _attendEndDateSemester;
        }

        /**
         * 科目別出欠データSQLを返す
         * -- 学期またがり可
         * -- 開始日付の端数可
         * -- 終了日付の端数可
         * 実行結果の SEMESTER が "9" は学期の総合計
         * @param defineSchoolCode    KNJDefineSchool
         * @param year          年度
         * @param sSemester     対象学期範囲From
         * @param eSemester     対象学期範囲To
         * @param semesInState  ATTEND_SUBCLASS_DATの対象(学期＋月)
         * @param periodInState 対象校時
         * @param befDayFrom    開始日付の端数用From
         * @param befDayTo      開始日付の端数用To
         * @param aftDayFrom    終了日付の端数用From
         * @param aftDayTo      終了日付の端数用To
         * @param grade         学年：指定しない場合は、Null
         * @param hrClass       クラス：指定しない場合は、Null
         * @param schregno      学籍番号：指定しない場合は、Null
         * @param useCurriculumcd 1=教育課程コードを使用する
         * @return 出欠データSQL<code>String</code>を返す
         */
        private String getAttendSubclassSql(
                final KNJDefineSchool defineSchoolCode,
                final KNJSchoolMst knjSchoolMst,
                final String year,
                final String sSemester,
                final String eSemester,
                final String semesInState,
                final String periodInState,
                final String befDayFrom,
                final String befDayTo,
                final String aftDayFrom,
                final String aftDayTo,
                final String grade,
                final String hrClass,
                final String schregno,
                final String useCurriculumcd,
                final String useVirus,
                final String useKoudome,
                final String useTestCountflg
        ) {
            final StringBuffer stb = new StringBuffer();

            //対象生徒
            stb.append("WITH SCHNO AS( ");
            stb.append(" SELECT ");
            stb.append("    W1.SCHREGNO, ");
            stb.append("    W1.GRADE, ");
            stb.append("    W1.SEMESTER, ");
            stb.append("    W1.HR_CLASS, ");
            stb.append("    W1.COURSECD, ");
            stb.append("    W1.MAJORCD, ");
            stb.append("    W1.COURSECODE ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT W1 ");
            stb.append(" WHERE ");
            stb.append("    W1.YEAR = '" + year + "' ");
            stb.append("    AND W1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
            if (schregno != null) {
                stb.append("    AND W1.SCHREGNO = '" + schregno + "' ");
            }
            if (grade != null) {
                stb.append("    AND W1.GRADE = '" + grade + "' ");
            }
            if ("?".equals(hrClass)) {
                stb.append("    AND W1.HR_CLASS = ? ");
            } else if (hrClass != null) {
                stb.append("    AND W1.HR_CLASS = '" + hrClass + "' ");
            }

            //端数計算有無の判定
            if (befDayFrom != null || aftDayFrom != null) {
                //対象生徒の時間割データ
                stb.append(" ), SCHEDULE_SCHREG_R AS( ");
                stb.append(" SELECT ");
                stb.append("    T2.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.CHAIRCD, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("    SCH_CHR_DAT T1, ");
                stb.append("    CHAIR_STD_DAT T2 ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + year + "' ");
                stb.append("    AND T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
                stb.append("    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
                if (befDayFrom != null && aftDayFrom != null) {
                    stb.append("    AND (T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                    stb.append("         OR T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') ");
                } else if (befDayFrom != null) {
                    stb.append("    AND T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                } else if (aftDayFrom != null) {
                    stb.append("    AND T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
                }
                stb.append("    AND T1.YEAR = T2.YEAR ");
                stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
                stb.append("    AND T1.CHAIRCD = T2.CHAIRCD ");
                if (defineSchoolCode != null && defineSchoolCode.usefromtoperiod)
                    stb.append("    AND T1.PERIODCD IN " + periodInState + " ");
                stb.append("    AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO) ");
                stb.append("    AND NOT EXISTS(SELECT ");
                stb.append("                       'X' ");
                stb.append("                   FROM ");
                stb.append("                       SCHREG_BASE_MST T3 ");
                stb.append("                   WHERE ");
                stb.append("                       T3.SCHREGNO = T2.SCHREGNO ");
                stb.append("                       AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE) ");
                stb.append("                             OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)) ");
                stb.append("                  ) ");
                stb.append("    AND NOT EXISTS(SELECT ");
                stb.append("                       'X' ");
                stb.append("                   FROM ");
                stb.append("                       ATTEND_DAT T4 ");
                stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
                stb.append("                   WHERE ");
                stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
                stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
                stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
                stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
                stb.append("                  ) ");
                // 勤怠コード'28'は時間割にカウントしない
                stb.append("    AND NOT EXISTS(SELECT ");
                stb.append("                       'X' ");
                stb.append("                   FROM ");
                stb.append("                       ATTEND_DAT T4 ");
                stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T4.YEAR AND L1.DI_CD = T4.DI_CD ");
                stb.append("                   WHERE ");
                stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
                stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
                stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
                stb.append("                       AND L1.REP_DI_CD = '28' ");
                stb.append("                  ) ");
                if (_hasSchChrDatExecuteDiv) {
                    stb.append("    AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
                }
                stb.append(" GROUP BY ");
                stb.append("    T2.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.CHAIRCD, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    T1.DATADIV ");

                stb.append(" ), SCHEDULE_SCHREG AS( ");
                stb.append(" SELECT ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE, ");
                stb.append("    T1.CHAIRCD, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG_R T1 ");
                stb.append("    INNER JOIN (SELECT ");
                stb.append("       T1.SCHREGNO, ");
                stb.append("       T1.SEMESTER, ");
                stb.append("       T1.EXECUTEDATE, ");
                stb.append("       MIN(T1.CHAIRCD) AS CHAIRCD, ");
                stb.append("       T1.PERIODCD ");
                stb.append("    FROM ");
                stb.append("       SCHEDULE_SCHREG_R T1 ");
                stb.append("    WHERE ");
                stb.append("       NOT EXISTS(SELECT ");
                stb.append("                          'X' ");
                stb.append("                      FROM ");
                stb.append("                          SCHREG_TRANSFER_DAT T3 ");
                stb.append("                      WHERE ");
                stb.append("                          T3.SCHREGNO = T1.SCHREGNO ");
                stb.append("                          AND TRANSFERCD IN('1','2') ");
                stb.append("                          AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ");
                stb.append("                     ) ");
                stb.append("    GROUP BY ");
                stb.append("       T1.SCHREGNO, ");
                stb.append("       T1.SEMESTER, ");
                stb.append("       T1.EXECUTEDATE, ");
                stb.append("       T1.PERIODCD ");
                stb.append(" ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SEMESTER = T1.SEMESTER AND T2.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append("         AND T2.CHAIRCD = T1.CHAIRCD AND T2.PERIODCD = T1.PERIODCD ");

                stb.append(" ), TEST_COUNTFLG AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.EXECUTEDATE, ");
                stb.append("         T1.PERIODCD, ");
                stb.append("         T1.CHAIRCD, ");
                stb.append("         '2' AS DATADIV ");
                stb.append("     FROM ");
                stb.append("         SCH_CHR_TEST T1, ");
                if ("TESTITEM_MST_COUNTFLG".equals(useTestCountflg)) {
                    stb.append("         TESTITEM_MST_COUNTFLG T2 ");
                    stb.append("     WHERE ");
                    stb.append("         T2.YEAR       = T1.YEAR ");
                } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(useTestCountflg)) {
                    stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
                    stb.append("     WHERE ");
                    stb.append("         T2.YEAR       = T1.YEAR ");
                    stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
                    stb.append("         AND T2.SCORE_DIV  = '01' ");
                } else {
                    stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
                    stb.append("     WHERE ");
                    stb.append("         T2.YEAR       = T1.YEAR ");
                    stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
                }
                stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("         AND T2.COUNTFLG   = '0' ");
            }

            //端数計算有無の判定
            if (befDayFrom != null || aftDayFrom != null) {
                //対象生徒の出欠データ
                stb.append(" ), T_ATTEND_DAT AS ( ");
                stb.append(" SELECT ");
                stb.append("    T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("    T2.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    '000' AS DI_CD, ");
                stb.append("    '' AS ATSUB_REPL_DI_CD, ");
                stb.append("    '' AS MULTIPLY, ");
                stb.append("    T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG_R T1 ");
                stb.append("    INNER JOIN CHAIR_DAT T2 ON T2.SEMESTER = T1.SEMESTER ");
                stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append("        AND T2.YEAR = '" + _year + "' ");

                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("    T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("    T2.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.SEMESTER, ");
                stb.append("    T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("    T1.PERIODCD, ");
                stb.append("    L1.REP_DI_CD AS DI_CD, ");
                stb.append("    L1.ATSUB_REPL_DI_CD, ");
                stb.append("    L1.MULTIPLY, ");
                stb.append("    T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("    SCHEDULE_SCHREG T1 ");
                stb.append("    INNER JOIN CHAIR_DAT T2 ON T2.SEMESTER = T1.SEMESTER ");
                stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
                stb.append("        AND T2.YEAR = '" + _year + "' ");
                stb.append("    INNER JOIN ATTEND_DAT T0 ON T0.SCHREGNO = T1.SCHREGNO ");
                stb.append("        AND T0.ATTENDDATE = T1.EXECUTEDATE ");
                stb.append("        AND T0.PERIODCD = T1.PERIODCD ");
                stb.append("    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = T0.YEAR AND L1.DI_CD = T0.DI_CD ");

                // 留学中の授業日数
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     VALUE(T1.SEMESTER, '9') AS SEMESTER, ");
                stb.append("     T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("     T1.PERIODCD, ");
                stb.append("     '01' AS DI_CD, ");
                stb.append("     '' AS ATSUB_REPL_DI_CD, ");
                stb.append("     '' AS MULTIPLY, ");
                stb.append("     T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("     SCHEDULE_SCHREG_R T1, ");
                stb.append("     SCHREG_TRANSFER_DAT T2, ");
                stb.append("     CHAIR_DAT T3 ");
                stb.append(" WHERE ");
                stb.append("     EXISTS (SELECT ");
                stb.append("                 'X' ");
                stb.append("             FROM ");
                stb.append("                 SCHNO E1 ");
                stb.append("             WHERE ");
                stb.append("                 E1.SCHREGNO = T2.SCHREGNO ");
                stb.append("             ) ");
                stb.append("     AND T2.TRANSFERCD IN('1') ");
                stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
                stb.append("     AND T3.YEAR = '" + year + "' ");
                stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T3.CHAIRCD = T1.CHAIRCD  ");
                stb.append(" GROUP BY ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("     GROUPING SETS ((T1.CHAIRCD, T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ");
                    stb.append("                    (T1.CHAIRCD, T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD, T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ");
                } else {
                    stb.append("     GROUPING SETS ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ");
                    stb.append("                    (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ");
                }

                // 休学中の授業日数
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CHAIRCD, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     VALUE(T1.SEMESTER, '9') AS SEMESTER, ");
                stb.append("     T1.EXECUTEDATE AS ATTENDDATE, ");
                stb.append("     T1.PERIODCD, ");
                stb.append("     '00' AS DI_CD, ");
                stb.append("     '' AS ATSUB_REPL_DI_CD, ");
                stb.append("     '' AS MULTIPLY, ");
                stb.append("     T1.DATADIV ");
                stb.append(" FROM ");
                stb.append("     SCHEDULE_SCHREG_R T1, ");
                stb.append("     SCHREG_TRANSFER_DAT T2, ");
                stb.append("     CHAIR_DAT T3 ");
                stb.append(" WHERE ");
                stb.append("     EXISTS (SELECT ");
                stb.append("                 'X' ");
                stb.append("             FROM ");
                stb.append("                 SCHNO E1 ");
                stb.append("             WHERE ");
                stb.append("                 E1.SCHREGNO = T2.SCHREGNO ");
                stb.append("             ) ");
                stb.append("     AND T2.TRANSFERCD IN('2') ");
                stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
                stb.append("     AND T3.YEAR = '" + year + "' ");
                stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T3.CHAIRCD = T1.CHAIRCD  ");
                stb.append(" GROUP BY ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("     GROUPING SETS ((T1.CHAIRCD, T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ");
                    stb.append("                    (T1.CHAIRCD, T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD, T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ");

                } else {
                    stb.append("     GROUPING SETS ((T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV), ");
                    stb.append("                    (T1.CHAIRCD, T3.SUBCLASSCD, T1.SCHREGNO, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV)) ");
                }
            }

            stb.append(" ), T_SCHREG_YEAR_LESSON AS ( ");
            if (isHoutei()) {
                stb.append(" SELECT ");
                stb.append("   T1.SCHREGNO, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("   T2.SUBCLASSCD AS SUBCLASSCD, VALUE(T2.CREDITS, 0) * VALUE(INT(JITU_SYUSU), 0) AS YEAR_LESSON ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN CREDIT_MST T2 ON ");
                stb.append("   T2.YEAR = T1.YEAR ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" INNER JOIN V_SCHOOL_GCM_MST T3 ON ");
                    stb.append("   T3.YEAR = T1.YEAR ");
                    stb.append("   AND T3.GRADE = '00' ");
                    stb.append("   AND T3.COURSECD = T1.COURSECD ");
                    stb.append("   AND T3.MAJORCD = T1.MAJORCD ");
                } else {
                    stb.append(" INNER JOIN V_SCHOOL_MST T3 ON ");
                    stb.append("   T3.YEAR = T1.YEAR ");
                }
                if ("1".equals(_useSchool_KindField)) {
                    stb.append("   AND T3.SCHOOLCD = '" + SCHOOLCD + "' ");
                    stb.append("   AND T3.SCHOOL_KIND = '" + SCHOOLKIND + "' ");
                }
                stb.append("   AND T2.GRADE = T1.GRADE ");
                stb.append("   AND T2.COURSECD = T1.COURSECD ");
                stb.append("   AND T2.MAJORCD = T1.MAJORCD ");
                stb.append("   AND T2.COURSECODE = T1.COURSECODE ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            } else {
                stb.append(" SELECT ");
                stb.append("   T1.SCHREGNO, ");
                if ("1".equals(useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("   T1.SUBCLASSCD AS SUBCLASSCD, VALUE(T1.LESSON, 0) AS YEAR_LESSON ");
                stb.append(" FROM ");
                stb.append("   SCHREG_ABSENCE_HIGH_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + year + "' ");
                stb.append("   AND T1.DIV = '2' ");
                stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            }

            //生徒・科目・学期別欠課集計の表（出欠データと集計テーブルを合算）
            stb.append("), SCH_ATTEND_SUM AS(");
            //端数計算有無の判定
            if (befDayFrom != null || aftDayFrom != null) {
                stb.append("    SELECT ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SUBCLASSCD, ");
                stb.append("        T1.SEMESTER, ");
                stb.append("        SUM(CASE WHEN DI_CD = '000' THEN 1 ELSE 0 END) ");
                stb.append("     - SUM(CASE WHEN DI_CD IN ( ");
                if (!"1".equals(knjSchoolMst._subOffDays)) {
                    stb.append("    '00' , ");
                }
                stb.append("        '01') THEN 1 ELSE 0 END) AS LESSON, ");
                stb.append("        SUM(CASE WHEN DI_CD = '000' THEN 1 ELSE 0 END) ");
                stb.append("     - SUM(CASE WHEN DI_CD IN ( ");
                if (!"1".equals(knjSchoolMst._subOffDays)) {
                    stb.append("    '00' , ");
                }
                stb.append("        '01') THEN 1 ELSE 0 END) ");
                stb.append("     - SUM(CASE WHEN DI_CD IN ( ");
                String comma = "";
                if (!"1".equals(knjSchoolMst._subSuspend)) {
                    stb.append("   " + comma + " '2', '9' ");
                    comma = ",";
                }
                if (!"1".equals(knjSchoolMst._subMourning)) {
                    stb.append("   " + comma + " '3', '10' ");
                    comma = ",";
                }
                if ("".equals(comma)) {
                    stb.append(" '' ");
                }
                stb.append("          ) THEN 1 ELSE 0 END) AS MLESSON, ");
                stb.append("        SUM(CASE WHEN DI_CD = '01' THEN 1 ELSE 0 END) AS ABROAD, ");
                stb.append("        SUM(CASE WHEN DI_CD = '00' THEN 1 ELSE 0 END) AS OFFDAYS, ");
                stb.append("        SUM(CASE WHEN DI_CD IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ");
                if ("true".equals(useVirus)) {
                    stb.append("        SUM(CASE WHEN DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
                } else {
                    stb.append("        0 AS VIRUS, ");
                }
                if ("true".equals(useKoudome)) {
                    stb.append("        SUM(CASE WHEN DI_CD IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME, ");
                } else {
                    stb.append("        0 AS KOUDOME, ");
                }
                stb.append("        SUM(CASE WHEN DI_CD IN ('3', '10') THEN 1 ELSE 0 END) AS MOURNING, ");
                stb.append("        SUM(CASE WHEN DI_CD = '14' THEN 1 ELSE 0 END) AS NURSEOFF, ");
                stb.append("        SUM(CASE WHEN DI_CD IN ('15','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY,'1')) ELSE 0 END) AS LATE, ");
                stb.append("        SUM(CASE WHEN DI_CD = '16' THEN 1 ELSE 0 END) AS EARLY, ");
                stb.append("        SUM(CASE WHEN (CASE WHEN T1.DI_CD IN ('29','30','31') THEN VALUE(T1.ATSUB_REPL_DI_CD, T1.DI_CD) ELSE T1.DI_CD END) IN( ");
                if (null != knjSchoolMst._subOffDays && knjSchoolMst._subOffDays.equals("1")) {
                    stb.append("    '00', ");
                }
                if (null != knjSchoolMst._subSuspend && knjSchoolMst._subSuspend.equals("1")) {
                    stb.append("    '2', '9', ");
                }
                if ("true".equals(useVirus)) {
                    if (null != knjSchoolMst._subVirus && knjSchoolMst._subVirus.equals("1")) {
                        stb.append("    '19', '20', ");
                    }
                }
                if ("true".equals(useKoudome)) {
                    if (null != knjSchoolMst._subKoudome && knjSchoolMst._subKoudome.equals("1")) {
                        stb.append("    '25', '26', ");
                    }
                }
                if (null != knjSchoolMst._subMourning && knjSchoolMst._subMourning.equals("1")) {
                    stb.append("    '3', '10', ");
                }
                if (null != knjSchoolMst._subAbsent && knjSchoolMst._subAbsent.equals("1")) {
                    stb.append("    '1', '8', ");
                }
                stb.append("        '4','5','6','14','11','12','13') THEN 1 ELSE 0 END) ");
                stb.append("        AS ABSENT1, ");
                stb.append("        SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(T1.MULTIPLY,'1')) ELSE 0 END)AS LATE_EARLY ");
                stb.append("    FROM ");
                stb.append("        T_ATTEND_DAT T1 ");
                stb.append("        , SCHNO T0 ");
                stb.append("    WHERE ");
                stb.append("        T1.SCHREGNO = T0.SCHREGNO ");
                stb.append("        AND T1.SEMESTER = T0.SEMESTER ");
                if (defineSchoolCode.useschchrcountflg) {
                    stb.append("        AND NOT EXISTS(SELECT ");
                    stb.append("                           'X' ");
                    stb.append("                       FROM ");
                    stb.append("                           SCH_CHR_COUNTFLG T4 ");
                    stb.append("                       WHERE ");
                    stb.append("                           T4.EXECUTEDATE = T1.ATTENDDATE ");
                    stb.append("                           AND T4.PERIODCD = T1.PERIODCD ");
                    stb.append("                           AND T4.CHAIRCD = T1.CHAIRCD ");
                    stb.append("                           AND T4.GRADE = T0.GRADE ");
                    stb.append("                           AND T4.HR_CLASS = T0.HR_CLASS ");
                    stb.append("                           AND T1.DATADIV IN ('0', '1') ");
                    stb.append("                           AND T4.COUNTFLG = '0') ");
                    stb.append("        AND NOT EXISTS(SELECT ");
                    stb.append("                           'X' ");
                    stb.append("                       FROM ");
                    stb.append("                           TEST_COUNTFLG TEST ");
                    stb.append("                       WHERE ");
                    stb.append("                           TEST.EXECUTEDATE  = T1.ATTENDDATE ");
                    stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
                    stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
                    stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
                }
                stb.append("    GROUP BY ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SUBCLASSCD, ");
                stb.append("        T1.SEMESTER ");
                stb.append("    UNION ALL ");
            }
            stb.append("    SELECT ");
            stb.append("        T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("  T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||   ");
            }
            stb.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("        SEMESTER, ");
            stb.append("        SUM(VALUE(T1.LESSON,0) ");
            if (!"1".equals(knjSchoolMst._subOffDays)) {
                stb.append("            - VALUE(T1.OFFDAYS, 0) ");
            }
            stb.append("            - VALUE(T1.ABROAD, 0)");
            stb.append("        ) AS LESSON, ");
            stb.append("        SUM(VALUE(T1.LESSON,0) ");
            if (!"1".equals(knjSchoolMst._subOffDays)) {
                stb.append("            - VALUE(T1.OFFDAYS, 0) ");
            }
            stb.append("            - VALUE(T1.ABROAD, 0)");
            if (!"1".equals(knjSchoolMst._subSuspend)) {
                stb.append("            - VALUE(T1.SUSPEND, 0) ");
            }
            if (!"1".equals(knjSchoolMst._subMourning)) {
                stb.append("            - VALUE(T1.MOURNING, 0) ");
            }
            if ("true".equals(useVirus)) {
                if (!"1".equals(knjSchoolMst._subVirus)) {
                    stb.append("            - VALUE(T1.VIRUS, 0) ");
                }
            }
            if ("true".equals(useKoudome)) {
                if (!"1".equals(knjSchoolMst._subKoudome)) {
                    stb.append("            - VALUE(T1.KOUDOME, 0) ");
                }
            }
            stb.append("        ) AS MLESSON, ");
            stb.append("        SUM(VALUE(T1.ABROAD, 0)) AS ABROAD, ");
            stb.append("        SUM(VALUE(T1.OFFDAYS, 0)) AS OFFDAYS, ");
            stb.append("        SUM(VALUE(T1.SUSPEND, 0)) AS SUSPEND, ");
            if ("true".equals(useVirus)) {
                stb.append("        SUM(VALUE(T1.VIRUS, 0)) AS VIRUS, ");
            } else {
                stb.append("        0 AS VIRUS, ");
            }
            if ("true".equals(useKoudome)) {
                stb.append("        SUM(VALUE(T1.KOUDOME, 0)) AS KOUDOME, ");
            } else {
                stb.append("        0 AS KOUDOME, ");
            }
            stb.append("        SUM(VALUE(T1.MOURNING, 0)) AS MOURNING, ");
            stb.append("        SUM(VALUE(T1.NURSEOFF,0)) AS NURSEOFF, ");
            stb.append("        SUM(VALUE(T1.LATE,0)) AS LATE, ");
            stb.append("        SUM(VALUE(T1.EARLY,0)) AS EARLY, ");
            stb.append("        SUM(VALUE(T1.SICK,0) + VALUE(T1.NOTICE,0) + VALUE(T1.NONOTICE,0) + VALUE(T1.NURSEOFF,0) ");
            if (null != knjSchoolMst._subOffDays && knjSchoolMst._subOffDays.equals("1")) {
                stb.append("            + VALUE(T1.OFFDAYS, 0) ");
            }
            if (null != knjSchoolMst._subSuspend && knjSchoolMst._subSuspend.equals("1")) {
                stb.append("            + VALUE(T1.SUSPEND, 0) ");
            }
            if ("true".equals(useVirus)) {
                if (null != knjSchoolMst._subVirus && knjSchoolMst._subVirus.equals("1")) {
                    stb.append("            + VALUE(T1.VIRUS, 0) ");
                }
            }
            if ("true".equals(useKoudome)) {
                if (null != knjSchoolMst._subKoudome && knjSchoolMst._subKoudome.equals("1")) {
                    stb.append("            + VALUE(T1.KOUDOME, 0) ");
                }
            }
            if (null != knjSchoolMst._subMourning && knjSchoolMst._subMourning.equals("1")) {
                stb.append("            + VALUE(T1.MOURNING, 0) ");
            }
            if (null != knjSchoolMst._subAbsent && knjSchoolMst._subAbsent.equals("1")) {
                stb.append("            + VALUE(T1.ABSENT, 0) ");
            }
            stb.append("        ) AS ABSENT1, ");
            stb.append("        SUM(VALUE(T1.LATE,0) + VALUE(T1.EARLY,0)) AS LATE_EARLY ");
            stb.append("    FROM ");
            stb.append("        ATTEND_SUBCLASS_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR = '" + year + "' ");
            stb.append("        AND T1.SEMESTER BETWEEN '" + sSemester + "' AND '" + eSemester + "' ");
            stb.append("        AND T1.SEMESTER || T1.MONTH  IN " + semesInState + " ");
            stb.append("        AND EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       SCHNO T2 ");
            stb.append("                   WHERE ");
            stb.append("                       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                   GROUP BY ");
            stb.append("                       SCHREGNO) ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.SEMESTER, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("  T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||   ");
            }
            stb.append("        T1.SUBCLASSCD ");

            stb.append(" ) ,SCH_ATTEND_UNI AS ( ");
            stb.append(" SELECT ");
            stb.append("    TT0.SCHREGNO, ");
            stb.append("    TT0.SEMESTER, ");
            stb.append("    TT0.SUBCLASSCD, ");
            stb.append("    SUM(TT0.LESSON) AS LESSON, ");
            stb.append("    SUM(TT0.MLESSON) AS MLESSON, ");
            stb.append("    SUM(TT0.ABROAD) AS ABROAD, ");
            stb.append("    SUM(TT0.OFFDAYS) AS OFFDAYS, ");
            stb.append("    SUM(TT0.SUSPEND) AS SUSPEND, ");
            stb.append("    SUM(TT0.VIRUS) AS VIRUS, ");
            stb.append("    SUM(TT0.KOUDOME) AS KOUDOME, ");
            stb.append("    SUM(TT0.MOURNING) AS MOURNING, ");
            stb.append("    SUM(TT0.NURSEOFF) AS NURSEOFF, ");
            stb.append("    SUM(TT0.LATE) + SUM(TT0.EARLY) AS LATE_EARLY, ");
            stb.append("    SUM(TT0.ABSENT1) AS ABSENT1 ");
            stb.append(" FROM ");
            stb.append("    SCH_ATTEND_SUM TT0 ");
            stb.append(" GROUP BY ");
            stb.append("    TT0.SCHREGNO, TT0.SEMESTER, TT0.SUBCLASSCD");

            //ペナルティー欠課を加味した生徒欠課集計の表（出欠データと集計テーブルを合算）
            stb.append("), ATTEND_B AS(");
            if ("1".equals(knjSchoolMst._absentCov) || "3".equals(knjSchoolMst._absentCov)) {
                //学期でペナルティ欠課を算出する場合
                stb.append("    SELECT ");
                stb.append("        SCHREGNO, ");
                stb.append("        SEMESTER, ");
                stb.append("        SUBCLASSCD, ");
                stb.append("        SUM(VALUE(ABSENT,0)) AS PENALTY_ABSENT, ");
                stb.append("        SUM(VALUE(SUB_LATE_EARLY,0)) AS SUB_LATE_EARLY ");
                stb.append("    FROM (SELECT ");
                stb.append("              SCHREGNO, ");
                stb.append("              SUBCLASSCD, ");
                stb.append("              SEMESTER, ");
                if ("3".equals(knjSchoolMst._absentCov)) {
                    stb.append("              VALUE(SUM(LATE_EARLY),0) / FLOAT(" + knjSchoolMst._absentCovLate + ") AS ABSENT, ");
                } else {
                    stb.append("              VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " AS ABSENT, ");
                }
                stb.append("              VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " * " + knjSchoolMst._absentCovLate + " AS SUB_LATE_EARLY ");
                stb.append("          FROM ");
                stb.append("              SCH_ATTEND_UNI T1 ");
                stb.append("          GROUP BY ");
                stb.append("              SCHREGNO, ");
                stb.append("              SUBCLASSCD, ");
                stb.append("              SEMESTER ");
                stb.append("          ) T1 ");
                stb.append("    GROUP BY ");
                stb.append("        SCHREGNO, ");
                stb.append("        SEMESTER, ");
                stb.append("        SUBCLASSCD ");
                stb.append("   UNION ALL ");
                stb.append("    SELECT ");
                stb.append("        SCHREGNO, ");
                stb.append("        '9' AS SEMESTER, ");
                stb.append("        SUBCLASSCD, ");
                stb.append("        SUM(VALUE(ABSENT,0)) AS PENALTY_ABSENT, ");
                stb.append("        SUM(VALUE(SUB_LATE_EARLY,0)) AS SUB_LATE_EARLY ");
                stb.append("    FROM (SELECT ");
                stb.append("              SCHREGNO, ");
                stb.append("              SUBCLASSCD, ");
                stb.append("              SEMESTER, ");
                if ("3".equals(knjSchoolMst._absentCov)) {
                    stb.append("              VALUE(SUM(LATE_EARLY),0) / FLOAT(" + knjSchoolMst._absentCovLate + ") AS ABSENT, ");
                } else {
                    stb.append("              VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " AS ABSENT, ");
                }
                stb.append("              VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " * " + knjSchoolMst._absentCovLate + " AS SUB_LATE_EARLY ");
                stb.append("          FROM ");
                stb.append("              SCH_ATTEND_UNI T1 ");
                stb.append("          GROUP BY ");
                stb.append("              SCHREGNO, ");
                stb.append("              SUBCLASSCD, ");
                stb.append("              SEMESTER ");
                stb.append("          ) T1 ");
                stb.append("    GROUP BY ");
                stb.append("        SCHREGNO, ");
                stb.append("        SUBCLASSCD ");
            } else if ("2".equals(knjSchoolMst._absentCov) || "4".equals(knjSchoolMst._absentCov)) {
                //通年でペナルティ欠課を算出する場合
                //学期の欠課時数は学期別で換算したペナルティ欠課を加算、学年の欠課時数は年間で換算する
                stb.append("    SELECT ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SUBCLASSCD, ");
                stb.append("        T1.SEMESTER, ");
                stb.append("        VALUE(T2.ABSENT_SEM,0) AS PENALTY_ABSENT, ");
                stb.append("        VALUE(T2.SUB_LATE_EARLY_SEM,0) AS SUB_LATE_EARLY ");
                stb.append("    FROM ");
                stb.append("        (SELECT ");
                stb.append("             SCHREGNO, ");
                stb.append("             SEMESTER, ");
                stb.append("             SUBCLASSCD ");
                stb.append("         FROM ");
                stb.append("             SCH_ATTEND_UNI T1 ");
                stb.append("         GROUP BY ");
                stb.append("             SCHREGNO, ");
                stb.append("             SEMESTER, ");
                stb.append("             SUBCLASSCD ");
                stb.append("        )T1, ");
                stb.append("        (SELECT ");
                stb.append("             SCHREGNO, ");
                stb.append("             SUBCLASSCD, ");
                stb.append("             SEMESTER, ");
                stb.append("             ABSENT AS ABSENT_SEM, ");
                stb.append("             SUB_LATE_EARLY AS SUB_LATE_EARLY_SEM ");
                stb.append("         FROM ");
                stb.append("             (SELECT ");
                stb.append("                  SCHREGNO, ");
                stb.append("                  SUBCLASSCD, ");
                stb.append("                  SEMESTER, ");
                if ("4".equals(knjSchoolMst._absentCov)) {
                    stb.append("                  VALUE(SUM(LATE_EARLY),0) / FLOAT(" + knjSchoolMst._absentCovLate + ") AS ABSENT, ");
                } else {
                    stb.append("                  VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " AS ABSENT, ");
                }
                stb.append("                  VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " * " + knjSchoolMst._absentCovLate + " AS SUB_LATE_EARLY ");
                stb.append("              FROM ");
                stb.append("                  SCH_ATTEND_UNI T1 ");
                stb.append("              GROUP BY ");
                stb.append("                  SCHREGNO, ");
                stb.append("                  SEMESTER, ");
                stb.append("                  SUBCLASSCD ");
                stb.append("              ) T1 ");
                stb.append("         GROUP BY ");
                stb.append("             SCHREGNO, ");
                stb.append("             SEMESTER, ");
                stb.append("             SUBCLASSCD, ");
                stb.append("             ABSENT, ");
                stb.append("             SUB_LATE_EARLY ");
                stb.append("        ) T2 ");
                stb.append("    WHERE ");
                stb.append("        T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
                stb.append("        AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append("    SELECT ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SUBCLASSCD, ");
                stb.append("        '9' AS SEMESTER, ");
                if ("4".equals(knjSchoolMst._absentCov)) {
                    stb.append("                  VALUE(SUM(LATE_EARLY),0) / FLOAT(" + knjSchoolMst._absentCovLate + ") AS PENALTY_ABSENT, ");
                } else {
                    stb.append("                  VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " AS PENALTY_ABSENT, ");
                }
                stb.append("        VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " * " + knjSchoolMst._absentCovLate + " AS SUB_LATE_EARLY ");
                stb.append("    FROM ");
                stb.append("        SCH_ATTEND_UNI T1 ");
                stb.append("    GROUP BY  ");
                stb.append("        T1.SCHREGNO, T1.SUBCLASSCD ");
            } else if ("5".equals(knjSchoolMst._absentCov)) {
                //通年でペナルティ欠課 + 余り繰り上げを算出する場合
                stb.append("    SELECT ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SUBCLASSCD, ");
                stb.append("        T1.SEMESTER, ");
                stb.append("        VALUE(T2.ABSENT_SEM,0) AS PENALTY_ABSENT, ");
                stb.append("        VALUE(T2.LATE_EARLY_SEM,0) AS SUB_LATE_EARLY ");
                stb.append("    FROM ");
                stb.append("        (SELECT ");
                stb.append("             SCHREGNO, ");
                stb.append("             SEMESTER, ");
                stb.append("             SUBCLASSCD ");
                stb.append("         FROM ");
                stb.append("             SCH_ATTEND_UNI T1 ");
                stb.append("         GROUP BY ");
                stb.append("             SCHREGNO, ");
                stb.append("             SEMESTER, ");
                stb.append("             SUBCLASSCD ");
                stb.append("        )T1, ");
                stb.append("        (SELECT ");
                stb.append("             SCHREGNO, ");
                stb.append("             SUBCLASSCD, ");
                stb.append("             SEMESTER, ");
                stb.append("             ABSENT AS ABSENT_SEM, ");
                stb.append("             SUB_LATE_EARLY AS LATE_EARLY_SEM ");
                stb.append("         FROM ");
                stb.append("             (SELECT ");
                stb.append("                  SCHREGNO, ");
                stb.append("                  SUBCLASSCD, ");
                stb.append("                  SEMESTER, ");
                stb.append("                  VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate);
                stb.append("                   +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + knjSchoolMst._absentCovLate + ") >= " + knjSchoolMst._amariKuriage + " THEN 1 ELSE 0 END)AS ABSENT, ");
                stb.append("                  VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " * " + knjSchoolMst._absentCovLate + " ");
                stb.append("                   +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + knjSchoolMst._absentCovLate + ") >= " + knjSchoolMst._amariKuriage + " THEN " + knjSchoolMst._amariKuriage + " ELSE 0 END)AS SUB_LATE_EARLY ");
                stb.append("              FROM ");
                stb.append("                  SCH_ATTEND_UNI T1 ");
                stb.append("              GROUP BY ");
                stb.append("                  SCHREGNO, ");
                stb.append("                  SEMESTER, ");
                stb.append("                  SUBCLASSCD ");
                stb.append("              ) T1 ");
                stb.append("         GROUP BY ");
                stb.append("             SCHREGNO, ");
                stb.append("             SEMESTER, ");
                stb.append("             SUBCLASSCD, ");
                stb.append("             ABSENT, ");
                stb.append("             SUB_LATE_EARLY ");
                stb.append("        ) T2 ");
                stb.append("    WHERE ");
                stb.append("        T1.SCHREGNO = T2.SCHREGNO ");
                stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
                stb.append("        AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append("    SELECT ");
                stb.append("        T1.SCHREGNO, ");
                stb.append("        T1.SUBCLASSCD, ");
                stb.append("        '9' AS SEMESTER, ");
                stb.append("        VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate);
                stb.append("          +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + knjSchoolMst._absentCovLate + ") >= " + knjSchoolMst._amariKuriage + " THEN 1 ELSE 0 END)AS PENALTY_ABSENT, ");
                stb.append("        VALUE(SUM(LATE_EARLY),0) / " + knjSchoolMst._absentCovLate + " * " + knjSchoolMst._absentCovLate + "  ");
                stb.append("          +  (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + knjSchoolMst._absentCovLate + ") >= " + knjSchoolMst._amariKuriage + " THEN " + knjSchoolMst._amariKuriage + " ELSE 0 END)AS SUB_LATE_EARLY ");
                stb.append("    FROM ");
                stb.append("        SCH_ATTEND_UNI T1 ");
                stb.append("    GROUP BY  ");
                stb.append("        T1.SCHREGNO, T1.SUBCLASSCD ");
            } else {
                //ペナルティ欠課なしの場合
                stb.append("    SELECT ");
                stb.append("        SCHREGNO, ");
                stb.append("        SUBCLASSCD, ");
                stb.append("        SEMESTER, ");
                stb.append("        0 AS PENALTY_ABSENT, ");
                stb.append("        0 AS SUB_LATE_EARLY ");
                stb.append("    FROM ");
                stb.append("        SCH_ATTEND_UNI T1 ");
                stb.append("    GROUP BY ");
                stb.append("        SCHREGNO, ");
                stb.append("        SUBCLASSCD, ");
                stb.append("        SEMESTER ");
            }

            stb.append("), ATTEND_SEMES_9 AS (");
            stb.append(" SELECT ");
            stb.append("    TT0.SCHREGNO, ");
            stb.append("    '9' AS SEMESTER, ");
            stb.append("    TT0.SUBCLASSCD, ");
            stb.append("    SUM(TT0.LESSON) AS LESSON, ");
            stb.append("    SUM(TT0.MLESSON) AS MLESSON, ");
            stb.append("    SUM(TT0.ABROAD) AS ABROAD, ");
            stb.append("    SUM(TT0.OFFDAYS) AS OFFDAYS, ");
            stb.append("    SUM(TT0.SUSPEND) AS SUSPEND, ");
            stb.append("    SUM(TT0.VIRUS) AS VIRUS, ");
            stb.append("    SUM(TT0.KOUDOME) AS KOUDOME, ");
            stb.append("    SUM(TT0.MOURNING) AS MOURNING, ");
            stb.append("    SUM(TT0.NURSEOFF) AS NURSEOFF, ");
            stb.append("    SUM(TT0.LATE_EARLY) AS LATE_EARLY1, ");
            stb.append("    SUM(TT0.LATE_EARLY) - VALUE(TT1.SUB_LATE_EARLY, 0) AS LATE_EARLY2, ");
            stb.append("    SUM(TT0.ABSENT1) AS ABSENT1, ");
            stb.append("    SUM(TT0.ABSENT1) + VALUE(TT1.PENALTY_ABSENT, 0) AS ABSENT2, ");
            stb.append("    TT3.SPECIAL_GROUP_CD, ");
            stb.append("    TT3.MINUTES, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.LESSON) AS SPECIAL_LESSON_MINUTES, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.MLESSON) AS SPECIAL_MLESSON_MINUTES, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.LATE_EARLY) AS SPECIAL_LATE_EARLY1_MINUTES, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.LATE_EARLY - VALUE(TT1.SUB_LATE_EARLY, 0))  AS SPECIAL_LATE_EARLY2_MINUTES, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.ABSENT1 + VALUE(TT1.PENALTY_ABSENT, 0)) AS SPECIAL_ABSENT_MINUTES1, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.ABSENT1) AS SPECIAL_ABSENT_MINUTES2, ");
            stb.append("    INT(VALUE(TT3.MINUTES, '0')) * SUM(TT0.ABSENT1 + TT0.LATE_EARLY) AS SPECIAL_ABSENT_MINUTES3 ");
            stb.append(" FROM ");
            stb.append("    SCH_ATTEND_UNI TT0 ");
            stb.append(" LEFT JOIN  ");
            stb.append("    ATTEND_B TT1 ON ");
            stb.append("       TT0.SCHREGNO = TT1.SCHREGNO ");
            stb.append("       AND '9' = TT1.SEMESTER ");
            stb.append("       AND TT0.SUBCLASSCD = TT1.SUBCLASSCD ");
            stb.append(" LEFT JOIN  ");
            stb.append("    ATTEND_SUBCLASS_SPECIAL_DAT TT3 ON ");
            stb.append("       TT3.YEAR = '" + _year + "'");
            stb.append("       AND TT0.SUBCLASSCD = ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("   TT3.CLASSCD || '-' || TT3.SCHOOL_KIND || '-' || TT3.CURRICULUM_CD || '-' || ");
            }
            stb.append("       TT3.SUBCLASSCD ");
            stb.append(" GROUP BY ");
            stb.append("    TT0.SCHREGNO, TT0.SUBCLASSCD, TT3.SPECIAL_GROUP_CD, TT3.MINUTES, TT1.PENALTY_ABSENT, TT1.SUB_LATE_EARLY ");

            //メイン表
            stb.append(") SELECT ");
            stb.append("    TT0.SCHREGNO, ");
            stb.append("    TT0.SEMESTER, ");
            stb.append("    TT0.SUBCLASSCD, ");
            stb.append("    TT0.LESSON, ");
            stb.append("    TT0.MLESSON, ");
            stb.append("    TT0.OFFDAYS, ");
            stb.append("    TT0.SUSPEND, ");
            stb.append("    TT0.VIRUS, ");
            stb.append("    TT0.KOUDOME, ");
            stb.append("    TT0.MOURNING, ");
            stb.append("    TT0.NURSEOFF, ");
            stb.append("    TT0.LATE_EARLY1, ");
            stb.append("    TT0.LATE_EARLY2, ");
            stb.append("    TT0.ABSENT1, ");
            stb.append("    TT0.ABSENT2, ");
            stb.append("    TT0.SPECIAL_GROUP_CD, ");
            stb.append("    TT0.SPECIAL_LESSON_MINUTES, ");
            stb.append("    TT0.SPECIAL_MLESSON_MINUTES, ");
            stb.append("    TT0.SPECIAL_LATE_EARLY1_MINUTES, ");
            stb.append("    TT0.SPECIAL_LATE_EARLY2_MINUTES, ");
            stb.append("    TT0.SPECIAL_ABSENT_MINUTES1, ");
            stb.append("    TT0.SPECIAL_ABSENT_MINUTES2, ");
            stb.append("    TT0.SPECIAL_ABSENT_MINUTES3, ");
            stb.append("    VALUE(TT4.YEAR_LESSON, 0) ");
            if (isHoutei()) { // 実授業制の場合、考慮された値が計算済みのため以下の処理は不要
                if (!"1".equals(knjSchoolMst._subOffDays)) {
                    stb.append("        - VALUE(TT0.OFFDAYS, 0) ");
                }
                stb.append("        - VALUE(TT0.ABROAD, 0) ");
            }
            if (!"1".equals(knjSchoolMst._subSuspend)) {
                stb.append("        - VALUE(TT0.SUSPEND, 0) ");
            }
            if (!"1".equals(knjSchoolMst._subMourning)) {
                stb.append("        - VALUE(TT0.MOURNING, 0) ");
            }
            stb.append("    AS YEAR_MLESSON, ");

            stb.append("    INT(VALUE(TT0.MINUTES, '0')) * (VALUE(TT4.YEAR_LESSON, 0) ");
            if (isHoutei()) { // 実授業制の場合、考慮された値が計算済みのため以下の処理は不要
                if (!"1".equals(knjSchoolMst._subOffDays)) {
                    stb.append("        - VALUE(TT0.OFFDAYS, 0) ");
                }
                stb.append("        - VALUE(TT0.ABROAD, 0) ");
            }
            if (!"1".equals(knjSchoolMst._subSuspend)) {
                stb.append("        - VALUE(TT0.SUSPEND, 0) ");
            }
            if (!"1".equals(knjSchoolMst._subMourning)) {
                stb.append("        - VALUE(TT0.MOURNING, 0) ");
            }
            stb.append("     ) AS YEAR_SPECIAL_MLESSON_MINUTES ");

            stb.append(" FROM ");
            stb.append("    ATTEND_SEMES_9 TT0 ");
            stb.append(" LEFT JOIN  ");
            stb.append("    T_SCHREG_YEAR_LESSON TT4 ON ");
            stb.append("       TT0.SCHREGNO = TT4.SCHREGNO ");
            stb.append("       AND TT0.SUBCLASSCD = TT4.SUBCLASSCD ");

            return stb.toString();
        }


    }

    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final Map _subclassAttendanceMap;
        final SpecialSubclassAttendance _specialSubclassAttendance;

        public Student(
                final String schregno,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex,
                final String grade,
                final String coursecd,
                final String majorcd,
                final String coursecode) {
            _schregno = schregno;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _grade = grade;
            _coursecd = grade;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _subclassAttendanceMap = new TreeMap();
            _specialSubclassAttendance = new SpecialSubclassAttendance(_schregno);
        }

        public void addSubclassAttendance(final Map row, final Param param) {
        	
            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            if (!"1".equals(param._semester) && param._zenkiKamokuSubclasscdList.contains(subclasscd)) {
            	if (param._isOutputDebug) {
            		log.info(_schregno + " skip zenki kamoku " + subclasscd);
            	}
            	return;
            }

            final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
            if (_subclassAttendanceMap.get(subclassCd)== null) {
                final SubclassAttendance sa = new SubclassAttendance(subclassCd, (String) param._subclassNameMap.get(subclassCd));
                _subclassAttendanceMap.put(subclassCd, sa);
            }
            final SubclassAttendance sa = getSubclassAttendance(subclassCd);
            sa.add(row, param);
        }

        public void addSpecialSubclassAttendance(final Map row, final Param param) {
        	final Integer _0 = new Integer(0);
            final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
            final String specialGroupCd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
            int mlessonMinutes = KnjDbUtils.getInt(row, "SPECIAL_MLESSON_MINUTES", _0).intValue();
            int yearMlessonMinutes = KnjDbUtils.getInt(row, "YEAR_SPECIAL_MLESSON_MINUTES", _0).intValue();

//            int ketujiMinutes = rs.getInt("SPECIAL_ABSENT_MINUTES2");

//            int lateearlyMinutes = 0;
//            if (_param._subClassC005.containsKey(subclassCd)) {
//                String is = (String) _param._subClassC005.get(subclassCd);
//                if ("1".equals(is)) {
//                    lateearlyMinutes = rs.getInt("SPECIAL_LATE_EARLY1_MINUTES");
//                } else if ("2".equals(is)) {
//                    lateearlyMinutes = rs.getInt("SPECIAL_LATE_EARLY2_MINUTES");
//                }
//            } else {
//                lateearlyMinutes = rs.getInt("SPECIAL_LATE_EARLY2_MINUTES");
//            }

            int kekkaMinutes = 0;
            if (param._subClassC005.containsKey(subclassCd)) {
                String is = (String) param._subClassC005.get(subclassCd);
                if ("1".equals(is)) {
                    kekkaMinutes = KnjDbUtils.getInt(row, "SPECIAL_ABSENT_MINUTES3", _0).intValue();
                } else if ("2".equals(is)) {
                    kekkaMinutes = KnjDbUtils.getInt(row, "SPECIAL_ABSENT_MINUTES2", _0).intValue();
                }
            } else {
                kekkaMinutes = KnjDbUtils.getInt(row, "SPECIAL_ABSENT_MINUTES1", _0).intValue();
            }
            if (kekkaMinutes != 0) {
                log.debug(_schregno + " add " + specialGroupCd + " (" + param._subClassC005.get(subclassCd) + ") : " + mlessonMinutes + " , " + kekkaMinutes);
            // if (kekkaMinutes != 0 || ketujiMinutes != 0 || lateearlyMinutes != 0) {
                //log.debug(_schregno + " add " + specialGroupCd + " (" + _param._subClassC005.get(subclassCd) + ") : " + mlessonMinutes + " , " + ketujiMinutes + " , "+ lateearlyMinutes + " , " + kekkaMinutes);
            }
            _specialSubclassAttendance.add(specialGroupCd, mlessonMinutes, yearMlessonMinutes, kekkaMinutes);
        }

        public boolean hasData() {
            return _subclassAttendanceMap.size() != 0;
        }

        public SubclassAttendance getSubclassAttendance(String subclassCd) {
            return (SubclassAttendance) _subclassAttendanceMap.get(subclassCd);
        }

        public String toString() {
            DecimalFormat df3 = new DecimalFormat("00");
            String attendNo = df3.format(Integer.valueOf(_attendNo).intValue());
            String space = "";
            for (int i=_name.length(); i<7; i++) {
                space += "  ";
            }
            StringBuffer stb = new StringBuffer(_schregno + " : " + attendNo + " , " + _name + space);
            for (final Iterator it = _subclassAttendanceMap.keySet().iterator(); it.hasNext();) {
                String subclassCd = (String) it.next();
                SubclassAttendance sa = getSubclassAttendance(subclassCd);
                stb.append("\n" + sa.toString());
            }
            return stb.toString();
        }
    }

    private String sqlStaffname(final String gradeHrclass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH TEMP1 AS ( ");
        stb.append("   SELECT T1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("   T2.SUBCLASSCD AS SUBCLASSCD, T1.CHAIRCD, MAX(T1.APPDATE) AS APPDATE ");
        stb.append("   FROM CHAIR_STD_DAT T1 ");
        stb.append("   INNER JOIN CHAIR_DAT T2 ON ");
        stb.append("     T1.YEAR = T2.YEAR AND ");
        stb.append("     T1.SEMESTER = T2.SEMESTER AND  ");
        stb.append("     T1.CHAIRCD = T2.CHAIRCD ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT T3 ON ");
        stb.append("     T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T3.GRADE || T3.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append("     GROUP BY T1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("              T2.SUBCLASSCD, T1.CHAIRCD ");
        stb.append(" ), TEMP2 AS ( ");
        stb.append("   SELECT SCHREGNO, SUBCLASSCD, MAX(TEMP1.APPDATE) AS APPDATE ");
        stb.append("   FROM TEMP1 ");
        stb.append("   GROUP BY SCHREGNO, SUBCLASSCD  ");
        stb.append(" ), TEMP3 AS( ");
        stb.append("   SELECT T1.SCHREGNO, T1.SUBCLASSCD, MAX(T2.CHAIRCD) AS CHAIRCD, T1.APPDATE ");
        stb.append("   FROM TEMP2 T1 ");
        stb.append("   INNER JOIN TEMP1 T2 ON ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND T1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("     AND T1.APPDATE = T2.APPDATE ");
        stb.append("   GROUP BY T1.SCHREGNO, T1.SUBCLASSCD, T1.APPDATE ");
        stb.append(" ), TEMP4 AS( ");
        stb.append("   SELECT ");
        stb.append("       T1.SCHREGNO, T1.SUBCLASSCD, T1.CHAIRCD, MAX(STAFFCD) AS STAFFCD ");
        stb.append("   FROM TEMP3 T1 ");
        stb.append("   INNER JOIN CHAIR_STD_DAT T2 ON ");
        stb.append("       T2.APPDATE = T1.APPDATE ");
        stb.append("       AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN CHAIR_STF_DAT T3 ON ");
        stb.append("       T3.YEAR = T2.YEAR ");
        stb.append("       AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("       AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("   WHERE ");
        stb.append("       T2.YEAR = '" + _param._year + "' ");
        stb.append("   GROUP BY ");
        stb.append("       T1.SCHREGNO, T1.SUBCLASSCD, T1.CHAIRCD ");
        stb.append(" ) ");
        stb.append(" SELECT T1.SCHREGNO, T1.SUBCLASSCD, T1.CHAIRCD, T1.STAFFCD, T2.STAFFNAME ");
        stb.append(" FROM TEMP4 T1 ");
        stb.append(" LEFT JOIN STAFF_MST T2 ON ");
        stb.append("     T2.STAFFCD = T1.STAFFCD ");
        return stb.toString();
    }


    /** 出欠カウント */
    private static class SubclassAttendance {
        /** 科目コード */
        private final String _subclassCd;
        /** 科目名 */
        private final String _subclassName;
        /** スタッフ名 */
        private String _staffName;

        /** 出席すべき授業時数 */
        private int _mlesson;
        /** 年間の出席すべき授業時数 */
        private int _yearMlesson;
        /** 欠席時数 */
        private int _ketuji;

        /** 欠課時数+ペナルティー  */
        private BigDecimal _kekka = new BigDecimal(0);
        /** 遅刻早退時数 */
        private int _lateEarly;

        /** 履修上限値注意 */
        private BigDecimal _compAbsenceHighTyui = new BigDecimal(0);

        /** 修得上限値注意 */
        private BigDecimal _getAbsenceHighTyui = new BigDecimal(0);

        public SubclassAttendance(final String subclassCd, final String subclassName) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }

        /**
         * 時数単位
         * @param rs
         */
        public void add(final Map row, final Param param) {
        	final Integer _0 = new Integer(0);
        	final BigDecimal bd0 = new BigDecimal(0);
            _mlesson += KnjDbUtils.getInt(row, "MLESSON", _0).intValue(); // 出席すべき授業時数
            _ketuji += KnjDbUtils.getInt(row, "ABSENT1", _0).intValue(); // 欠課時数
            _lateEarly += "1".equals(param._chikokuHyoujiFlg) ? KnjDbUtils.getInt(row, "LATE_EARLY1", _0).intValue() : KnjDbUtils.getInt(row, "LATE_EARLY2", _0).intValue(); // 換算差し引き後の遅刻・早退
            _kekka  = _kekka.add(KnjDbUtils.getBigDecimal(row, "ABSENT2", bd0)); // 欠課
            _yearMlesson += KnjDbUtils.getInt(row, "YEAR_MLESSON", _0).intValue();
        }

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return "SUBCLASSCD = " + _subclassCd
            + ", MLS=" + df5.format(_mlesson)
            + ", AB1=" + df5.format(_ketuji)
            + ", AB2=" + df5.format(_kekka.doubleValue())
            + ", LAT=" + df5.format(_lateEarly);
        }
    }

    private static String getHouteiJisuSql(final String subclassCd, final Param parameter, final String gradeHrclass, final boolean isGroup) {
        final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        if (isGroup) {
            stb.append("     T1.SPECIAL_GROUP_CD, ");
        } else {
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
        if (parameter._absenceWarnIsUnitCount) {
            final String sem = "1".equals(parameter._attendEndDateSemester) ? "" : parameter._attendEndDateSemester;
            stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(ABSENCE_WARN_RISHU_SEM" + parameter._attendEndDateSemester + ", 0) ");
        }
        stb.append("       AS ABSENCE_HIGH_TYUI, ");
        stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
        if (parameter._absenceWarnIsUnitCount) {
            final String sem = "1".equals(parameter._attendEndDateSemester) ? "" : parameter._attendEndDateSemester;
            stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(ABSENCE_WARN_SHUTOKU_SEM" + parameter._attendEndDateSemester + ", 0) ");
        }
        stb.append("       AS GET_ABSENCE_HIGH_TYUI ");
        stb.append(" FROM ");
        stb.append("     " + tableName + " T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("       T2.GRADE = T1.GRADE AND ");
        stb.append("       T2.COURSECD = T1.COURSECD AND ");
        stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
        stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
        stb.append("       T2.YEAR = T1.YEAR AND ");
        stb.append("       T2.SEMESTER = '" + parameter._semester + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + parameter._year + "' ");
        if (isGroup) {
            stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
        } else {
            if (null != subclassCd) {
                stb.append("     AND ");
                if ("1".equals(parameter._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD = '" + subclassCd + "' ");
            }
        }
        stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + gradeHrclass + "' ");
        return stb.toString();
    }

    private static String getJituJisuSql(final String subclassCd, final Param parameter, final String gradeHrclass, final boolean isGroup) {
        final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
        final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        if (isGroup) {
            stb.append("     T1.SPECIAL_GROUP_CD, ");
        } else {
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
        if (parameter._absenceWarnIsUnitCount) {
            final String sem = "1".equals(parameter._attendEndDateSemester) ? "" : parameter._attendEndDateSemester;
            stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + parameter._attendEndDateSemester + ", 0) ");
        }
        stb.append("        AS ABSENCE_HIGH_TYUI, ");
        stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
        if (parameter._absenceWarnIsUnitCount) {
            final String sem = "1".equals(parameter._attendEndDateSemester) ? "" : parameter._attendEndDateSemester;
            stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + parameter._attendEndDateSemester + ", 0) ");
        }
        stb.append("        AS GET_ABSENCE_HIGH_TYUI ");
        stb.append(" FROM ");
        stb.append("     " + tableName + " T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.YEAR = T1.YEAR ");
        stb.append("       AND T2.SEMESTER = '" + parameter._semester + "' ");
        stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
        if (isGroup) {
            stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
        } else {
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append("       T3.SUBCLASSCD = ");
            if ("1".equals(parameter._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("           T1.SUBCLASSCD ");
        }
        stb.append("       AND T3.COURSECD = T2.COURSECD ");
        stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("       AND T3.GRADE = T2.GRADE ");
        stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("       AND T3.YEAR = T1.YEAR ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + parameter._year + "' ");
        stb.append("     AND T1.DIV = '2' ");
        if (isGroup) {
            stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
        } else {
            if (null != subclassCd) {
                stb.append("     AND ");
                if ("1".equals(parameter._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD = '" + subclassCd + "' ");
            }
        }
        stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + gradeHrclass + "' ");
        return stb.toString();
    }

    private static String getSchregAbsenceHighSpecialSql(final Param parameter, final String gradeHrclass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     LESSON ");
        stb.append(" FROM SCHREG_ABSENCE_HIGH_SPECIAL_DAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.SEMESTER = '" + parameter._semester + "' ");
        stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + gradeHrclass + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + parameter._year + "' ");
        stb.append("     AND T1.DIV = '2' ");
        stb.append("     AND T1.SPECIAL_GROUP_CD = '999' ");
        return stb.toString();
    }

    private static class SpecialSubclassAttendance {
        final String _schregno;
        final String _subclassName = "特別活動";
        final Map _specialSubclassMlesson;
        final Map _yearSpecialSubclassMlesson;
        final Map _specialSubclassKekka;
        BigDecimal _compAbsenceHighTyui = BigDecimal.valueOf(0);
        public String _group999jituLesson;
        Integer _specialSubclassGroupMlesson = Integer.valueOf("0");

        SpecialSubclassAttendance(final String schregno) {
            _schregno = schregno;
            _specialSubclassMlesson = new TreeMap();
            _yearSpecialSubclassMlesson = new TreeMap();
            _specialSubclassKekka = new TreeMap();
        }

        public void add(final String specialGroupCd, final int mlessonMinutes, final int yearMlessonMinutes, final int kekkaMinutes) {
            if (specialGroupCd != null) {
                addMinutes(_specialSubclassMlesson, specialGroupCd, mlessonMinutes, "mlesson");
                addMinutes(_yearSpecialSubclassMlesson, specialGroupCd, yearMlessonMinutes, "yearMlesson");
                addMinutes(_specialSubclassKekka, specialGroupCd, kekkaMinutes, "kekka");
            }
        }

        private void addMinutes(final Map specialSubclassMinutes, final String specialGroupCd, final int minutes, final String debug) {
            if (!specialSubclassMinutes.containsKey(specialGroupCd)) {
                specialSubclassMinutes.put(specialGroupCd, new Integer(0));
            }
            int totalKekkaMinutes = ((Integer) specialSubclassMinutes.get(specialGroupCd)).intValue();
            totalKekkaMinutes += minutes;
            specialSubclassMinutes.put(specialGroupCd, new Integer(totalKekkaMinutes));
            if (minutes != 0) {
                // log.debug(_schregno + " " + specialGroupCd + " : " + debug + " : " + minutes + " => " + totalKekkaMinutes);
            }
        }

        private BigDecimal getTotalJisu(final Map subclassGroupMinutes, final Param param, final String debug) {
            BigDecimal totalJisu = new BigDecimal(0);
            for (final Iterator it = subclassGroupMinutes.keySet().iterator(); it.hasNext();) {
                final String specialGroupCd = (String) it.next();
                final int minutes = ((Integer) subclassGroupMinutes.get(specialGroupCd)).intValue();
                final BigDecimal jisu = getSpecialAttendExe(minutes, param);
                totalJisu = totalJisu.add(jisu);
                if (minutes != 0) {
                    // log.debug(_schregno + " : " + debug + " " + specialGroupCd + " : " + minutes + " => " + jisu);
                }
            }
            if (totalJisu.intValue() != 0) {
                // log.debug(_schregno + " : " + debug + " total = " + totalJisu);
            }
            return totalJisu;
        }

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
            final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
            int hasu = 0;
            final String retSt = bigD.toString();
            final int retIndex = retSt.indexOf(".");
            if (retIndex > 0) {
                hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
            }
            final BigDecimal rtn;
            if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
                rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
            } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
            } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
            } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
                rtn = bigD;
            } else {
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            }
            return rtn;
        }
    }

}
