// kanji=漢字
/*
 * $Id: 4262c10f38612d4ce35ed0bcd0594dc642ac21a1 $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] クラス別出欠一覧表（朝拝）
 */

public class KNJC162A {

    private static final Log log = LogFactory.getLog(KNJC162A.class);
    private Param _param;
    
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        // ＤＢ接続
        DB2UDB db2 = null;
        
        _param = new Param(request);

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("svf instancing exception! ", ex);
        }
        
        boolean nonedata = false;
        try {
            db2 = new DB2UDB(_param._dbName , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
            
            _param.load(db2);
            
            // 印刷処理
            nonedata = printMain(db2, svf);

        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        } finally {
            // 終了処理
            if (!nonedata) {
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
    private boolean printMain (
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        boolean nonedata = false;
        
        svfPrintHead(db2, svf); //見出し出力のメソッド
        PreparedStatement ps = null;
        ResultSet rs = null;
        //SQL作成
        try {
            // HRのSQL
            log.debug("sql = " + sqlSchregRegdHdat());
            ps = db2.prepareStatement(sqlSchregRegdHdat());
            rs = ps.executeQuery();
            
            final List homerooms = new ArrayList();

            // HRごとに生徒と出欠情報を出力
            while (rs.next()) {
                final String hrname = rs.getString("HR_NAME");
                final String hrclass = rs.getString("HR_CLASS");
                final String staffName = rs.getString("STAFFNAME1");
                homerooms.add(new Homeroom(hrname, staffName, hrclass));
            }
            
            for (final Iterator it = homerooms.iterator(); it.hasNext();) {
                final Homeroom homeroom = (Homeroom) it.next();
                
                svf.VrsOut("HR_NAME", homeroom._hrname);
                //log.debug("staff_name = " + staffName);
                svf.VrsOut("TEACHER", homeroom._staffname);
                

                final List studentList = getStudentList(db2, homeroom._hrclass);

                // 生徒がいなければ処理をスキップ
                if (studentList.size() == 0) {
                    continue;
                }

                nonedata = true;

                final AttendanceCount sum = new AttendanceCount();

                final int MAX_LINE_PER_PAGE = 45;
                int col = 1;
                int lessonDaysMin = Integer.MAX_VALUE; // 授業日数最小
                int lessonDaysMax = Integer.MIN_VALUE; // 授業日数最大
                
                for (Iterator it2 = studentList.iterator(); it2.hasNext();) {
                    final Student student = (Student) it2.next();
                    
                    svf.VrsOut("ATTEND_NO", Integer.valueOf(student._attendNo).toString());
                    svf.VrsOut("NAME", student._name);
                    svf.VrsOut("SEX", student._sex);
                    if (student._attend != null) {
                        
                        final AttendanceCount attend = student._attend;
                        
                        svf.VrsOut("CLASS_DAYS1", String.valueOf(attend._lessonDays));
                        svf.VrsOut("MOURNING", String.valueOf(attend._mourningDays));
                        svf.VrsOut("SUSPEND", String.valueOf(attend._suspendDays));
                        svf.VrsOut("ABROAD", String.valueOf(attend._abroadDays));
                        svf.VrsOut("ATTEND", String.valueOf(attend._mlessonDays));
                        if (!_param._useAbsenceWarn && _param._knjSchoolMst._kessekiOutBunsi != null && _param._knjSchoolMst._kessekiOutBunbo != null) {
                            final String bunsi = _param._knjSchoolMst._kessekiOutBunsi;
                            final String bunbo = _param._knjSchoolMst._kessekiOutBunbo;
                            final BigDecimal sick = new BigDecimal(attend._sickDays);
                            final BigDecimal limit = new BigDecimal(attend._mlessonDays).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
                            if (sick.compareTo(limit) >= 0) {
                                svf.VrAttribute("ABSENT",  "Paint=(1,40,1),Bold=1");
                            }
                        } else if (_param._useAbsenceWarn && _param._knjSchoolMst._kessekiWarnBunsi != null && _param._knjSchoolMst._kessekiWarnBunbo != null) {
                            final String bunsi = _param._knjSchoolMst._kessekiWarnBunsi;
                            final String bunbo = _param._knjSchoolMst._kessekiWarnBunbo;
                            final BigDecimal sick = new BigDecimal(attend._sickDays);
                            final BigDecimal limit = new BigDecimal(attend._mlessonDays).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
                            if (sick.compareTo(limit) >= 0) {
                                svf.VrAttribute("ABSENT",  "Paint=(1,70,1),Bold=1");
                            }
                        }
                        svf.VrsOut("ABSENT", String.valueOf(attend._sickDays));

                        svf.VrsOut("TOTAL_LESSON", String.valueOf(attend._attendDays));
                        svf.VrsOut("LATE", String.valueOf(attend._lateDays));
                        svf.VrsOut("EARLY", String.valueOf(attend._earlyDays));
                        svf.VrsOut("CLASS_ATTEND", attend.getAttendancePercentage());
                        
                        svf.VrsOut("MORNING_PRAY_ABSENT", String.valueOf(attend._chohaiKekka));
                        svf.VrsOut("CLASS_ABSENT", String.valueOf(attend._jugyoKekka));
                        svf.VrsOut("MORNING_PRAY_LATE", String.valueOf(attend._chohaiLate));
                        svf.VrsOut("CLASS_LATE", String.valueOf(attend._jugyoLate));
                    }
                    
                    lessonDaysMin = student.getLessonMin(lessonDaysMin);
                    lessonDaysMax = student.getLessonMax(lessonDaysMax);
                    
                    //log.debug(student.toString());
                    sum.addAttendanceDay(student._attend);
                    
                    svf.VrEndRecord();
                    col += 1;
                    if (col > MAX_LINE_PER_PAGE) {
                        col = 1;
                    }
                }
                
                // 一覧表枠外の文言
                final String comment = _param._useAbsenceWarn ? "注意" : "超過";
                if (_param._useAbsenceWarn) {
                    svf.VrAttribute("NOTE1",  "Paint=(1,70,1),Bold=1");
                } else {
                    svf.VrAttribute("NOTE1",  "Paint=(1,40,1),Bold=1");
                }
                svf.VrsOut("NOTE1",  " " );
                svf.VrsOut("NOTE2",  "：欠席" + comment);
                
                final String outputLessonDays = getOutputFromTo(lessonDaysMin, lessonDaysMax);
                log.debug("outputLessonDays = " + outputLessonDays);
                svf.VrsOut("CLASS_DAYS", outputLessonDays);
                svf.VrsOut("ATTEND_OF_CLASS", sum.getAttendancePercentage());
                
                for (; col <= MAX_LINE_PER_PAGE; col++) {
                    svf.VrsOut("NAME", "\n");
                    svf.VrEndRecord();
                }
            }
            
        } catch (Exception ex) {
            log.error("svfPrint exception! ", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return nonedata;
    }
    
    private class Homeroom {
        final String _hrname;
        final String _staffname;
        final String _hrclass;
        public Homeroom(final String hrname, final String staffname, final String hrclass) {
            _hrname = hrname;
            _staffname = staffname;
            _hrclass = hrclass;
        }
    }
    
    /**
     * 最小値から最大値までの出力用文字列を得る 
     * @param min 最小値
     * @param max 最大値
     * @return 最小値から最大値までの出力用文字列
     */
    private String getOutputFromTo(final int min, final int max) {
        if (min == max) {
            return String.valueOf(min);
        }
        return String.valueOf(min) + "〜" + String.valueOf(max);
    }

    /**
     * studentList から学籍番号が schregno の生徒を取得
     * @param schregno 生徒の学籍番号
     * @param studentList 生徒のリスト
     * @return 対象の生徒
     */
    private Student getStudent(final String schregno, final List studentList) {
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private List getStudentList(final DB2UDB db2, final String hrClass) throws SQLException {
        ResultSet rs = null;
        final List studentList = new ArrayList();
        
        // HRの生徒を取得
        final String sqlSchregRegdDat = sqlSchregRegdDat(hrClass);
        PreparedStatement ps = db2.prepareStatement(sqlSchregRegdDat);
        rs = ps.executeQuery();
        while (rs.next()) {
            Student st = new Student(
                    rs.getString("SCHREGNO"), 
                    rs.getString("ATTENDNO"), 
                    rs.getString("NAME"), 
                    rs.getString("SEX"),
                    rs.getString("GRADE"));
            studentList.add(st);
        }
        DbUtils.closeQuietly(null, ps, rs);
        
        if (studentList.size() == 0) {
            return studentList;
        }
        
        // 1日単位
        final String sql = getAttendSemesSql(
                _param._knjSchoolMst,
                _param._year,
                (String) _param._hasuuMap.get("attendSemesInState"),
                _param._grade,
                hrClass
        );
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final Student student = getStudent(rs.getString("SCHREGNO"), studentList);
            if (student == null) {
                continue;
            }
            student._attend.addAttendanceDay(_param, rs);
        }
        DbUtils.closeQuietly(null, ps, rs);

        return studentList;
    }
    
    private String getAttendSemesSql(
            final KNJSchoolMst knjSchoolMst,
            final String year,
            final String semesInState,
            final String grade,
            final String hrClass
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     MONTH, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     ABROAD, ");
        stb.append("     OFFDAYS, ");
        stb.append("     LESSON - ABROAD ");
        if (!"1".equals(knjSchoolMst._semOffDays)) {
            stb.append("     - OFFDAYS ");
        }
        stb.append("     AS LESSON, ");
        stb.append("     ABSENT, ");
        stb.append("     SUSPEND, ");
        stb.append("     MOURNING, ");
        if ("true".equals(_param._useVirus)) {
            stb.append("     VIRUS, ");
        } else {
            stb.append("     0 AS VIRUS, ");
        }
        if ("true".equals(_param._useKoudome)) {
            stb.append("     KOUDOME, ");
        } else {
            stb.append("     0 AS KOUDOME, ");
        }
        stb.append("     SICK + NOTICE + NONOTICE AS SICK, ");
        stb.append("     LATE, ");
        stb.append("     EARLY, ");
        stb.append("     VALUE(KEKKA_JISU, 0) AS KEKKA_JISU, ");
        stb.append("     VALUE(REIHAI_KEKKA, 0) AS REIHAI_KEKKA, ");
        stb.append("     VALUE(REIHAI_TIKOKU, 0) AS REIHAI_TIKOKU, ");
        stb.append("     VALUE(JYUGYOU_TIKOKU, 0) AS JYUGYOU_TIKOKU ");
        stb.append(" FROM ");
        stb.append("     V_ATTEND_SEMES_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND (T1.SEMESTER || T1.MONTH IN " + semesInState + ") ");
        stb.append("     AND T2.GRADE = '" + grade + "' ");
        stb.append("     AND T2.HR_CLASS = '" + hrClass + "' ");
        return stb.toString();
    }

    /**
     *  印刷処理 見出し出力
     */
    private void svfPrintHead (
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        svf.VrSetForm("KNJC162A.frm", 4);
        svf.VrsOut("NENDO", _param.getNendoString());
        svf.VrsOut("DATE", _param.getPrintDateStr());
        svf.VrsOut("RANGE", _param.getRange());
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName());
        
        svfVrsOutKintaiName(svf, _param.getKintaiName("6"), "ABSENTNAME");
        svfVrsOutKintaiName(svf, _param.getKintaiName("14"), "ANO_ROOM_NAME");
    }
    
    

    private void svfVrsOutKintaiName(Vrw32alp svf, String kintaiName, String field) {
        String fieldName = "";
        if (kintaiName != null && kintaiName.length() > 2) {
            fieldName = field + "2_1";
        } else {
            fieldName = field + "1";
        }
        svf.VrsOut( fieldName, kintaiName);
    }

    /** 学年クラスとクラス名称の列挙を得るSQL */
    private String sqlSchregRegdHdat() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T2.STAFFNAME AS STAFFNAME1, ");
        stb.append("     T3.STAFFNAME AS STAFFNAME2, ");
        stb.append("     T4.STAFFNAME AS STAFFNAME3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST T3 ON T1.TR_CD2 = T3.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST T4 ON T1.TR_CD3 = T4.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '"+_param._year+"' ");
        stb.append("     AND T1.SEMESTER = '"+_param._semester+"' ");
        stb.append("     AND T1.GRADE = '"+_param._grade+"' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS in "+ SQLUtils.whereIn(true, _param._gradeHrclasses));
        return stb.toString();
    }
    
    /** 学生を得るSQL */
    private String sqlSchregRegdDat(final String hrClass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     L1.NAME2 AS SEX ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST L1 ON ");
        stb.append("         L1.NAMECD1 = 'Z002' ");
        stb.append("         AND L1.NAMECD2 = T2.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }
    
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _attendStartDate;
        private final String _attendEndDate;
        private final String _loginDate;
        private final String _dbName;
        private final String _grade;
        private final String[] _gradeHrclasses;
        private final int DEFAULT_LESSON_MINUTES = 50; // 法定授業時分
        private final boolean _useAbsenceWarn; // 注意 or 超過
        private final String _useVirus;
        private final String _useKoudome;

        private String _z012;
        private Map _kintaiNameMap; 
        private String _schoolName;
        private KNJSchoolMst _knjSchoolMst;
        private Map _hasuuMap = new HashMap();
        
        Param(HttpServletRequest request) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _attendStartDate = request.getParameter("SDATE").replace('/', '-');
            _attendEndDate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _dbName = request.getParameter("DBNAME");
            
            _grade = request.getParameter("GRADE");
            _gradeHrclasses = request.getParameterValues("CATEGORY_SELECTED");
            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
        }

        public String getRange() {
            return getDateString(_attendStartDate) + "〜" + getDateString(_attendEndDate);
        }

        // 印刷日
        private String getPrintDateStr() {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            DecimalFormat df = new DecimalFormat("00");
            String time = df.format(hour) + ":" + df.format(minute);

            return getDateString(_loginDate) + "（" + KNJ_EditDate.h_format_W(_loginDate) +"）" + time;
        }
        
        private String getKintaiName(String kintaiCd) {
            return null != _kintaiNameMap.get(kintaiCd) ? (String) _kintaiNameMap.get(kintaiCd) : "";
        }
        
        private String getSchoolName() {
            return _schoolName;
        }

        private void load(DB2UDB db2) {
            try {
                _z012 = getZ012(db2);
                _kintaiNameMap = getKintaiNameMap(db2);
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                _schoolName = loadSchoolName(db2);
                
                // 出欠の情報
                final KNJDefineSchool _definecode = new KNJDefineSchool();       //各学校における定数等設定
                _definecode.defineCode (db2, _year);
                
                final Map attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, null, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap,  _attendStartDate, _attendEndDate);
                log.debug(" hasuuMap = " + _hasuuMap);
                
                KNJDefineSchool defineSchool = new KNJDefineSchool();
                defineSchool.defineCode(db2, _year);
                
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
        }
        
        private String loadSchoolName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String schoolName = null;
            try {
                final StringBuffer sql = new StringBuffer(); 
                sql.append("SELECT ");
                sql.append(" SCHOOLNAME1 ");
                sql.append("FROM ");
                sql.append("   SCHOOL_MST T1 ");
                sql.append("WHERE ");
                sql.append("   YEAR = '" + _year + "' ");
                
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        private String getStaffNameString(String staffName1, String staffName2, String staffName3) {
            String[] staffNames = {staffName1, staffName2, staffName3};
            StringBuffer stb = new StringBuffer();
            String comma = "";
            for (int i = 0; i < staffNames.length; i++) {
                if (staffNames[i] != null) {
                    stb.append(comma + staffNames[i]);
                    comma = "、";
                }
            }
            return stb.toString();
        }
        
        private String getNendoString() {
            if ("2".equals(_z012)) {
                return _year + "年度";
            }
            return KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度"; // デフォルトは和暦
        }

        private String getDateString(String date) {
            if ("2".equals(_z012)) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            }
            return KNJ_EditDate.h_format_JP(date); // デフォルトは和暦
        }
        
        private Map getKintaiNameMap(DB2UDB db2) throws SQLException {

            final Map kintaiNameMap = new HashMap();
            final StringBuffer sql = new StringBuffer(); 
            sql.append("SELECT ");
            sql.append(" T1.DI_CD AS KINTAI_CD, ");
            sql.append(" T1.DI_NAME1 AS KINTAI_NAME ");
            sql.append("FROM ");
            sql.append("   ATTEND_DI_CD_DAT T1 ");
            sql.append("WHERE ");
            sql.append("      T1.YEAR = '" + _year + "' ");
            sql.append("      AND T1.DI_CD in ('4','5','6','14') ");
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String kintaiCd = rs.getString("KINTAI_CD");
                String kintaiName = rs.getString("KINTAI_NAME");
                kintaiNameMap.put(kintaiCd, kintaiName);
            }
            DbUtils.closeQuietly(null, ps, rs);
            return kintaiNameMap;
        }

        private String getZ012(DB2UDB db2) throws SQLException {
            final StringBuffer sql = new StringBuffer(); 
            sql.append("SELECT ");
            sql.append(" T2.NAME1 ");
            sql.append("FROM ");
            sql.append("   NAME_YDAT T1 ");
            sql.append("   INNER JOIN NAME_MST T2 ON T1.NAMECD1 = T2.NAMECD1 ");
            sql.append("         AND T1.NAMECD2 = T2.NAMECD2 ");
            sql.append("WHERE ");
            sql.append("      T1.NAMECD1 = 'Z012' ");
            sql.append("      AND T1.NAMECD2 = '00' ");
            sql.append("      AND T1.YEAR = '" + _year + "' ");
            log.debug("Z012 sql = " + sql);
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            String rtn = null;
            while (rs.next()) {
                rtn = rs.getString("NAME1");
                log.debug("Z012 = " + rtn);
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }
    }
    
    private class Student {
        final String _schregno;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _grade;
        public AttendanceCount _attend;
        public double _compAbsenceHighSpecial999;

        public Student(
                final String schregno,
                final String attendNo,
                final String name, 
                final String sex,
                final String grade) {
            _schregno = schregno;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _grade = grade;
            _attend = new AttendanceCount();
        }
        
        public int getLessonMin(int lessonDays) {
            if (_attend == null) {
                return lessonDays;
            }
            return Math.min(lessonDays, _attend._lessonDays);
        }
        public int getLessonMax(int lessonDays) {
            if (_attend == null) {
                return lessonDays;
            }
            return Math.max(lessonDays, _attend._lessonDays);
        }
        
        public String toString() {
            DecimalFormat df3 = new DecimalFormat("00");
            String attendNo = df3.format(Integer.valueOf(_attendNo).intValue());
            String space = "";
            for (int i=_name.length(); i<7; i++) {
                space += "  ";
            }
            String name = _name + space;
            return attendNo + " , " + name + " , " + _attend;
        }
    }
    
    /** 出欠カウント */
    private class AttendanceCount {
        /** 授業日数 */
        private int _lessonDays;
        /** 忌引日数 */
        private int _mourningDays;
        /** 出停日数 */
        private int _suspendDays;
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

        /** 科目SHR朝拝 の遅刻数・欠時数 */
        private int _chohaiLate;
        private int _chohaiKekka;
        /** 教科コード90以下の科目 の遅刻数合計・欠時数合計 */
        private int _jugyoLate;
        private int _jugyoKekka;

        /**
         * 1日単位の出欠を得る。
         * @param rs
         * @throws SQLException
         */
        private void addAttendanceDay(final Param param, final ResultSet rs) throws SQLException {
            int lesson   = rs.getInt("LESSON"); // 授業日数
            int sick     = rs.getInt("SICK"); // 病欠日数
            int special  = rs.getInt("MOURNING") + rs.getInt("SUSPEND"); // 特別欠席
            if ("true".equals(param._useVirus)) {
                special += rs.getInt("VIRUS");
            }
            if ("true".equals(param._useKoudome)) {
                special += rs.getInt("KOUDOME");
            }
            int mlesson  = lesson - special; // 出席すべき日数
            _mourningDays += rs.getInt("MOURNING");
            _suspendDays  += rs.getInt("SUSPEND");
            if ("true".equals(param._useVirus)) {
                _suspendDays += rs.getInt("VIRUS");
            }
            if ("true".equals(param._useKoudome)) {
                _suspendDays += rs.getInt("KOUDOME");
            }
            _abroadDays   += rs.getInt("ABROAD");
            _mlessonDays  += mlesson;
            _sickDays     += sick;
            _lessonDays   += lesson;
            _attendDays   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
            _lateDays     += rs.getInt("LATE");
            _earlyDays    += rs.getInt("EARLY");
            
            _chohaiKekka  += rs.getInt("REIHAI_KEKKA");
            _jugyoKekka   += rs.getInt("KEKKA_JISU");
            _chohaiLate   += rs.getInt("REIHAI_TIKOKU");
            _jugyoLate    += rs.getInt("JYUGYOU_TIKOKU");
        }
        
        private void addAttendanceDay(final AttendanceCount ac) {
            _mourningDays += ac._mourningDays;
            _suspendDays  += ac._suspendDays;
            _abroadDays   += ac._abroadDays;
            _mlessonDays  += ac._mlessonDays;
            _sickDays     += ac._sickDays;
            _lessonDays   += ac._lessonDays;
            _attendDays   += ac._mlessonDays - ac._sickDays; // 出席日数 = 出席すべき日数 - 欠席日数
            _lateDays     += ac._lateDays;
            _earlyDays    += ac._earlyDays;
            
            _chohaiKekka  += ac._chohaiKekka;
            _jugyoKekka   += ac._jugyoKekka;
            _chohaiLate   += ac._chohaiLate;
            _jugyoLate    += ac._jugyoLate;
        }

        /** 出欠の百分率を得る
         *  (除籍区分がある、もしくはデータが無い なら空白)
         */
        public String getAttendancePercentage() {
            if (_mlessonDays <= 0 || _attendDays <= 0) {
                    return "0.0";
            }
            BigDecimal denom = new BigDecimal(_mlessonDays);
            BigDecimal percentage = new BigDecimal(100.0 * (_attendDays)).divide(denom, 1, BigDecimal.ROUND_HALF_UP);
            return new DecimalFormat("0.0").format(percentage);
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
            + ", LAT=" + df5.format(_lateDays )
            + ", EAR=" + df5.format(_earlyDays);
        }
    }
}
