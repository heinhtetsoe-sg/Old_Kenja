// kanji=漢字
/*
 * $Id: 81769954da542e52a1537e91a0cb642571038519 $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*
 * 学校教育システム 賢者 [出欠管理] 出欠状況一覧
 */

public class KNJC039 {

    private static final Log log = LogFactory.getLog(KNJC039.class);

    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        _param = new Param(request);

        try {
            response.setContentType("application/pdf");
            svf.VrInit(); // クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); // PDFファイル名の設定
        } catch (IOException ex) {
            log.error("svf instancing exception! ", ex);
        }

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(_param._dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2); // Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }
        _param.load(db2);

        boolean nonedata = false;
        try {
            // 印刷処理
            nonedata = svfPrint(db2, svf);

        } catch (Exception ex) {
            log.error(ex);
        } finally {
            // 終了処理
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            db2.close();
            svf.VrQuit();
        }
    }

    /**
     * 印刷処理
     */
    private boolean svfPrint(final DB2UDB db2, final Vrw32alp svf) {

        final List<StudentHolder> holderList = getStudentHolderList(db2);

        final String form;
        if ("2".equals(_param._output)) {
            form = "KNJC039_2.frm";
        } else {
            form = "KNJC039.frm";
        }
        svf.VrSetForm(form, 4);

        // SVF出力
        boolean nonedata = false;
        final int maxLine = 50;
        int line = 0;
        int page = 1;
        for (final StudentHolder holder : holderList) {
            if (line >= maxLine) {
                line -= maxLine;
                page += 1;
            }

            final String chairCd = "2".equals(_param._output) ? holder.getCode() : null;

            if (!holder.hasOutput(chairCd)) {
                continue;
            }

            svfPrintHeader(db2, svf, page); // 見出し出力のメソッド

            if ("2".equals(_param._output)) {
                final Chair chair = (Chair) holder;
                svf.VrsOut("CHAIRCD", chair.getCode());
                svf.VrsOut("CHAIRNAME", chair._chairName);
                svf.VrsOut("TEACHER", chair._chairStaffName);
            } else {
                final HomeRoom hr = (HomeRoom) holder;
                svf.VrsOut("HR_NAME", hr._hrName);
                svf.VrsOut("TEACHER", hr._teacherName);
            }

            log.debug(holder.toString());
            for (final Student st : holder.getStudentList()) {

                if (!st.hasOutput(chairCd)) {
                    continue;
                }
                log.debug(st.toString());
                if ("2".equals(_param._output)) {
                    svf.VrsOut("HR_NAME", st._hrName + Integer.valueOf(st._attendNo).toString() + "番");
                } else {
                    svf.VrsOut("ATTEND_NO", Integer.valueOf(st._attendNo).toString());
                }
                svf.VrsOut("NAME", st._name);

                for (final String date : st._dateScheduleMap.keySet()) {
                    final KintaiSchedule kintaiSchedule = st.getSchedule(date);

                    if (!kintaiSchedule.hasOutput(chairCd)) {
                        continue;
                    }

                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_MD(date));
                    nonedata = true;
//                    log.debug(kintaiSchedule.toString());

                    int lineCount = 0;
                    for (final Kintai kintai : _param.kintaiList) {
                        if (!kintai._isPrint || kintaiSchedule.getKintaiCdSet(kintai._cd).size() == 0) {
                            lineCount += 0;
                        } else {
                            if ("2".equals(_param._output)) {
                                final List<String> sessionNameList = kintaiSchedule.getSessionNameList(kintai, chairCd);
                                for (final String sessionName : sessionNameList) {
                                    svf.VrsOut("ABSENCE_NAME", kintai.getName());
                                    svf.VrsOut("ABSENCE_CD", kintai._cd);
                                    svf.VrsOut("SESSION_1", sessionName);
                                    svf.VrEndRecord();
                                    lineCount += 1;
                                }
                            } else {
                                svf.VrsOut("ABSENCE_NAME", kintai.getName());
                                final String sessionName = kintaiSchedule.getSessionName(kintai, chairCd);
                                final int length = sessionName.getBytes().length;
                                final String fieldName = length > 78 ? "SESSION_3" : length > 54 ? "SESSION_2" : "SESSION_1";
                                svf.VrsOut(fieldName, sessionName);
                                svf.VrEndRecord();
                                lineCount += 1;
                            }
                        }
                    }
                    line += lineCount;
                }
            }
        }
        log.debug("nonedata = " + nonedata);
        return nonedata;
    }

    private List<StudentHolder> getStudentHolderList(final DB2UDB db2) {
        final List<StudentHolder> list = new ArrayList<StudentHolder>();
        String sql = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        // DB読み込み
        try {
            sql = sqlAttend(_param._year, _param._semester, _param._grade, _param._sDate, _param._eDate);
            ps = db2.prepareStatement(sql);
            log.debug("attend sql = " + sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                final String code = "2".equals(_param._output) ? rs.getString("CHAIRCD") : rs.getString("HR_CLASS");
                StudentHolder holder = null;

                for (final StudentHolder h : list) {
                    if (null != h.getCode() && h.getCode().equals(code)) {
                        holder = h;
                        break;
                    }
                }
                if (null == holder) {
                    if ("2".equals(_param._output)) {
                        holder = new Chair(code, rs.getString("CHAIRNAME"), rs.getString("CHAIRSTAFFNAME"));
                    } else {
                        holder = new HomeRoom(code, rs.getString("HR_NAME"), rs.getString("STAFFNAME"));
                    }
                    list.add(holder);
                }

                if (null == holder.getStudent(rs.getString("SCHREGNO"))) {
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("HR_NAME"), rs.getString("ATTENDNO"), rs.getString("NAME"));
                    holder.addStudent(student);
                }
                final Student student = holder.getStudent(rs.getString("SCHREGNO"));
                final String date = rs.getString("EXECUTEDATE");
                final String kintaiCd = rs.getString("DI_CD");
                final String periodCd = rs.getString("PERIODCD");
                final String chairCd = rs.getString("CHAIRCD");
                student.addSchedule(date, kintaiCd, periodCd, chairCd);
            }
        } catch (Exception e) {
            log.error("Exception!" + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlAttend(final String year, final String semester, final String grade, final String sdate, final String edate) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HRSTAFF AS ( ");
        stb.append("     SELECT ");
        stb.append("       YEAR, ");
        stb.append("       SEMESTER, ");
        stb.append("       GRADE, ");
        stb.append("       HR_CLASS, ");
        stb.append("       HR_NAME, ");
        stb.append("       (CASE WHEN T2.STAFFNAME IS NOT NULL THEN T2.STAFFNAME ");
        stb.append("             WHEN T3.STAFFNAME IS NOT NULL THEN T3.STAFFNAME ");
        stb.append("            ELSE T4.STAFFNAME END) AS STAFFNAME ");
        stb.append("     FROM ");
        stb.append("       SCHREG_REGD_HDAT ");
        stb.append("       LEFT JOIN STAFF_MST T2 ON TR_CD1 = T2.STAFFCD ");
        stb.append("       LEFT JOIN STAFF_MST T3 ON TR_CD2 = T3.STAFFCD ");
        stb.append("       LEFT JOIN STAFF_MST T4 ON TR_CD3 = T4.STAFFCD ");
        stb.append("     WHERE ");
        stb.append("       YEAR = '" + year + "' ");
        stb.append("       AND SEMESTER = '" + semester + "' ");
        stb.append(" ), CHAIRSTAFF AS ( ");
        stb.append("     SELECT ");
        stb.append("       T1.YEAR, ");
        stb.append("       T1.SEMESTER, ");
        stb.append("       T1.CHAIRCD, ");
        stb.append("       T4.STAFFNAME AS CHAIRSTAFFNAME ");
        stb.append("     FROM ");
        stb.append("       CHAIR_DAT T1 ");
        stb.append("       INNER JOIN (SELECT YEAR, SEMESTER, CHAIRCD, MIN(VALUE(CHARGEDIV, 0)) AS CHARGEDIV FROM CHAIR_STF_DAT ");
        stb.append("                   GROUP BY YEAR, SEMESTER, CHAIRCD) T2 ON T2.YEAR = T1.YEAR");
        stb.append("            AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("            AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("       INNER JOIN (SELECT YEAR, SEMESTER, CHAIRCD, VALUE(CHARGEDIV, 0) AS CHARGEDIV, MIN(STAFFCD) AS STAFFCD FROM CHAIR_STF_DAT ");
        stb.append("                   GROUP BY YEAR, SEMESTER, CHAIRCD, VALUE(CHARGEDIV, 0)) T3 ON T3.YEAR = T2.YEAR ");
        stb.append("            AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("            AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("            AND T3.CHARGEDIV = T2.CHARGEDIV ");
        stb.append("       INNER JOIN STAFF_MST T4 ON T4.STAFFCD = T3.STAFFCD ");
        stb.append("     WHERE ");
        stb.append("       T1.YEAR = '" + year + "' ");
        stb.append("       AND T1.SEMESTER = '" + semester + "' ");
        stb.append(" ), ATTENDDAT AS ( ");
        stb.append("       SELECT SCHREGNO, ATTENDDATE ");
        stb.append("     FROM ");
        stb.append("       ATTEND_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("       T1.ATTENDDATE BETWEEN '" + sdate + "' AND '" + edate + "'");
        stb.append("       AND DI_CD IN ( ");
        stb.append("           SELECT ");
        stb.append("               I1.DI_CD ");
        stb.append("           FROM ");
        stb.append("               ATTEND_DI_CD_DAT I1 ");
        stb.append("           WHERE ");
        stb.append("               I1.YEAR = '" + year + "' ");
        stb.append("               AND (I1.REP_DI_CD    IN ('1', '2', '3', '14', '19', '25') OR  ");
        stb.append("                    I1.ONEDAY_DI_CD IN ('1', '2', '3', '14', '19', '25')) ");
        stb.append("       ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T2.GRADE || T2.HR_CLASS AS HR_CLASS, ");
        stb.append("     T7.HR_NAME, ");
        stb.append("     T7.STAFFNAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     CASE WHEN ATTEND_DI.REP_DI_CD IN ('-' ");
        if ("true".equals(_param._useVirus)) {
            stb.append(", '19', '20' ");
        }
        if ("true".equals(_param._useKoudome)) {
            stb.append(", '25', '26' ");
        }
        stb.append("          ) THEN '2' ELSE T5.DI_CD ");
        stb.append("     END AS DI_CD, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T6.CHAIRCD, ");
        stb.append("     T6.CHAIRNAME, ");
        stb.append("     T8.CHAIRSTAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T4 ON T1.YEAR = T4.YEAR ");
        stb.append("        AND T1.SEMESTER = T4.SEMESTER ");
        stb.append("        AND T1.CHAIRCD = T4.CHAIRCD ");
        stb.append("        AND T1.EXECUTEDATE BETWEEN T4.APPDATE AND T4.APPENDDATE ");
        stb.append("     INNER JOIN CHAIR_DAT T6 ON T6.YEAR = T4.YEAR ");
        stb.append("        AND T6.SEMESTER = T4.SEMESTER ");
        stb.append("        AND T6.CHAIRCD = T4.CHAIRCD ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T1.YEAR = T2.YEAR ");
        stb.append("        AND T2.SEMESTER = '" + semester + "' ");
        stb.append("        AND T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN ATTEND_DAT T5 ON T5.YEAR = T1.YEAR ");
        stb.append("        AND T5.ATTENDDATE = T1.EXECUTEDATE ");
        stb.append("        AND T5.CHAIRCD = T1.CHAIRCD ");
        stb.append("        AND T5.PERIODCD = T1.PERIODCD ");
        stb.append("        AND T5.SCHREGNO = T4.SCHREGNO ");
        stb.append("     LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = T5.YEAR AND ATTEND_DI.DI_CD = T5.DI_CD ");
        stb.append("     LEFT JOIN HRSTAFF T7 ON T7.YEAR = T1.YEAR ");
        stb.append("        AND T7.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T7.GRADE = T2.GRADE ");
        stb.append("        AND T7.HR_CLASS = T2.HR_CLASS ");
        stb.append("     LEFT JOIN CHAIRSTAFF T8 ON T8.YEAR = T6.YEAR ");
        stb.append("        AND T8.SEMESTER = T6.SEMESTER ");
        stb.append("        AND T8.CHAIRCD = T6.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     EXISTS (SELECT 'X' ");
        stb.append("             FROM ATTENDDAT ");
        stb.append("             WHERE SCHREGNO = T2.SCHREGNO ");
        stb.append("               AND ATTENDDATE = T1.EXECUTEDATE) ");
        if (!_param._allGrade.equals(grade)) {
            stb.append("    AND T2.GRADE = '" + grade + "' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._output)) {
            stb.append("    T6.CHAIRCD, ");
        }
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     ATTEND_DI.REP_DI_CD ");

        return stb.toString();
    }

    /**
     * 印刷処理 見出し出力
     */
    private void svfPrintHeader(final DB2UDB db2, final Vrw32alp svf, final int page) {
        svf.VrsOut("PAGE", String.valueOf(page));
        svf.VrsOut("PRINTDAY", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
        svf.VrsOut("ABSENCE_TITLE", _param.getAbsenceTitle());
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
        final String sdate = _param._sDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._sDate);
        final String edate = _param._sDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._eDate);
        svf.VrsOut("RANGE", sdate + "〜" + edate);
    }

    private class Param {
        private final String _year;

        private final String _semester;

        private final String _sDate;

        private final String _eDate;

        private final String _loginDate;

        private final String _grade;

        private final String _dbName;

        private final String _output;

        private final String _useVirus;
        private final String _useKoudome;
        private final String _taisyouDiCd;

        private final String _allGrade = "99";

        /** 校時名称マップ */
        final Map<String, String> _periodNameMap = new TreeMap<String, String>();

        /** 勤怠名称マップ */
        final Map<String, String> _kintaiNameMap = new TreeMap<String, String>();

        /** 1日出欠勤怠コードの読み替えマップ */
        final Map<String, String> _onedayKintaiReplaceMap = new HashMap<String, String>();

        final List<Kintai> kintaiList = new ArrayList<Kintai>();

        Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _sDate = request.getParameter("SDATE").replace('/', '-');
            _eDate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _dbName = request.getParameter("DBNAME");
            _output = request.getParameter("OUTPUT");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _taisyouDiCd = request.getParameter("taisyouDiCd");
            final String[] taisyouDiCdArray = StringUtils.split(_taisyouDiCd, ",");
            for (int i = 0; i < taisyouDiCdArray.length; i++) {
                final String taisyouDiCd = taisyouDiCdArray[i];
                boolean _printDi = "on".equals(request.getParameter("DI_CD" + taisyouDiCd));
                // ※1日出欠の勤怠は別に読替テーブルで対応する。
                kintaiList.add(new Kintai(taisyouDiCd, _printDi));
            }

            log.debug("$Id: 81769954da542e52a1537e91a0cb642571038519 $");
        }

        public String getAbsenceTitle() {
            final StringBuffer stb = new StringBuffer(20);
            String nakaguro = "";

            for (final Kintai kintai : kintaiList) {
                if (kintai._isPrint) {
                    stb.append(nakaguro + kintai.getName());
                    nakaguro = "・";
                }
            }

            return "（" + stb.toString() + "）";
        }

        public String getKintaiName(final String periodCd) {
            return _kintaiNameMap.get(periodCd);
        }

        public String getPeriodName(final String periodCd) {
            return _periodNameMap.get(periodCd);
        }

        public String getReplacedKintaiCd(final String kintaiCd) {
            if (_onedayKintaiReplaceMap.get(kintaiCd) == null) {
                return kintaiCd;
            }
            return _onedayKintaiReplaceMap.get(kintaiCd);
        }

        public void load(DB2UDB db2) {
            loadPeriod(db2);
            loadKintai(db2);
        }

        private void loadPeriod(DB2UDB db2) {
            // SQL作成
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlPeriod());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String periodCd = rs.getString("PERIODCD");
                    final String periodName = rs.getString("PERIODNAME");
                    _periodNameMap.put(periodCd, periodName);
                }

            } catch (Exception ex) {
                log.error("svfPrint exception! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 学年クラスとクラス名称の列挙を得るSQL */
        private String sqlPeriod() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.NAMECD2 AS PERIODCD, ");
            stb.append("     T1.NAME1 AS PERIODNAME ");
            stb.append(" FROM ");
            stb.append("     NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.NAMECD1 = 'B001' ");
            return stb.toString();
        }

        private void loadKintai(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlKintaiCd());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String kintaiCd = rs.getString("KINTAICD");
                    final String name = rs.getString("KINTAINAME");
                    final String replaced = rs.getString("REPLACED");

                    _kintaiNameMap.put(kintaiCd, name);

                    if (replaced == null) {
                        continue;
                    }

                    _onedayKintaiReplaceMap.put(kintaiCd, replaced);
                }

            } catch (Exception ex) {
                log.error("svfPrint exception! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String sqlKintaiCd() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.DI_CD AS KINTAICD, ");
            stb.append("     T1.DI_NAME1 AS KINTAINAME, ");
            stb.append("     T1.ONEDAY_DI_CD AS REPLACED ");
            stb.append(" FROM ");
            stb.append("     ATTEND_DI_CD_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            return stb.toString();
        }
    }

    private static abstract class StudentHolder {

        private final String _code;

        private final List<Student> _studentList;

        StudentHolder(final String code) {
            _code = code;
            _studentList = new ArrayList<Student>();
        }

        public String getCode() {
            return _code;
        }

        public void addStudent(final Student student) {
            _studentList.add(student);
        }

        public List<Student> getStudentList() {
            return Collections.unmodifiableList(_studentList);
        }

        public Student getStudent(final String schregNo) {
            for (final Student s : _studentList) {
                if (schregNo != null && schregNo.equals(s._schregNo)) {
                    return s;
                }
            }
            return null;
        }

        /** 出力するデータがあればtrueを返す */
        protected boolean hasOutput(final String code) {
            for (final Student st : _studentList) {
                if (st.hasOutput(code)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** ホームルーム */
    private static class HomeRoom extends StudentHolder {

        private final String _grade;

        private final String _hrName;

        private final String _teacherName;

        public HomeRoom(final String hrClass, final String hrName, final String staffName) {
            super(hrClass);
            _grade = hrClass.substring(0, 2);
            _hrName = hrName;
            _teacherName = staffName;
        }

        public String toString() {
            return _hrName + " : " + _teacherName;
        }
    }

    /** 講座 */
    private static class Chair extends StudentHolder {

        private final String _chairName;

        private final String _chairStaffName;

        public Chair(final String chairCd, final String chairName, final String chairStaffName) {
            super(chairCd);
            _chairName = chairName;
            _chairStaffName = chairStaffName;
        }

        public String toString() {
            return _chairName + " : " + _chairStaffName;
        }
    }

    /** 生徒ごとの出欠状況 */
    private class Student {

        private String _schregNo;

        private String _hrName;

        private String _attendNo;

        private String _name;

        private Map<String, KintaiSchedule> _dateScheduleMap;

        public Student(final String schregNo, final String hrName, final String attendNo, final String name) {
            _schregNo = schregNo;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _dateScheduleMap = new TreeMap();
        }

        public void addSchedule(final String date, final String kintaiCd, final String periodCd, final String chairCd) {
            if (null == _dateScheduleMap.get(date)) {
                _dateScheduleMap.put(date, new KintaiSchedule(date));
            }
            final KintaiSchedule scheduleSet = _dateScheduleMap.get(date);
            scheduleSet.addPeriod(kintaiCd, periodCd, chairCd);
        }

        public KintaiSchedule getSchedule(final String date) {
            return _dateScheduleMap.get(date);
        }

        /** 出力するデータがあればtrueを返す */
        public boolean hasOutput(final String code) {
            for (final String date : _dateScheduleMap.keySet()) {
                final KintaiSchedule ks = _dateScheduleMap.get(date);
                if (ks.hasOutput(code)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return _schregNo + "," + _attendNo + "," + _name + "";
        }
    }

    private class KintaiSchedule {
        // key
        final String _date;

        final Set<String> _scheduleSet;

        final Map<String, Set<StudentKintai>> _kintaiPeriodSetMap;

        KintaiSchedule(final String date) {
            _date = date;
            _kintaiPeriodSetMap = new HashMap<String, Set<StudentKintai>>();

            _scheduleSet = new TreeSet<String>();

            for (final Kintai kintai : _param.kintaiList) {
                _kintaiPeriodSetMap.put(kintai._cd, new TreeSet<StudentKintai>());
            }
        }

        private void addPeriod(final String kintaiCd, final String periodCd, final String chairCd) {
            _scheduleSet.add(periodCd);

            final String replaced = _param.getReplacedKintaiCd(kintaiCd);

            if (null == _kintaiPeriodSetMap.get(replaced)) {
                // 勤怠が公欠、出停、忌引のいずれでもない
                return;
            }
            final Set<StudentKintai> kintaiSet =  _kintaiPeriodSetMap.get(replaced);
            kintaiSet.add(new StudentKintai(kintaiCd, periodCd, chairCd));
        }

        /** 出力するデータがあればtrueを返す */
        private boolean hasOutput(final String chairCd) {
            for (final Kintai kintai : _param.kintaiList) {
                if (0 == getKintaiCdSet(kintai._cd).size() || !kintai._isPrint) {
                    continue;
                }
                if (null == chairCd) {
                    return true;
                } else {
                    for (final StudentKintai sk : getKintaiCdSet(kintai._cd)) {
                        if (chairCd.equals(sk._chairCd)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private Set<StudentKintai> getKintaiCdSet(final String kintaiCd) {
            return _kintaiPeriodSetMap.get(kintaiCd);
        }

        public String toString() {
            final StringBuffer stb = new StringBuffer();
            stb.append("  日付" + _date);
            for (final Kintai kintai : _param.kintaiList) {
                if (getKintaiCdSet(kintai._cd).size() != 0 && kintai._isPrint) {
                    stb.append(" " + kintai.getName() + ":");
                    stb.append(getSessionName(kintai, null));
                }
            }
            return stb.toString();
        }

        /** 帳票の「校時」の出力名称リストを得る */
        private List<String> getSessionNameList(final Kintai kintai, final String chairCd) {
            final List<String> list = new ArrayList();
            for (final StudentKintai studentKintai : getKintaiCdSet(kintai._cd)) {
                if (null != chairCd && !chairCd.equals(studentKintai._chairCd)) {
                    continue;
                }
                list.add(_param.getPeriodName(studentKintai._periodCd));
            }
            return list;
        }

        /** 帳票の「校時」の出力名称を得る */
        private String getSessionName(final Kintai kintai, final String chairCd) {
            final StringBuffer stb = new StringBuffer();
            String comma = "";
            for (final StudentKintai studentKintai : getKintaiCdSet(kintai._cd)) {
                if (null != chairCd && !chairCd.equals(studentKintai._chairCd)) {
                    continue;
                }
                stb.append(comma + _param.getPeriodName(studentKintai._periodCd));
                comma = "、";
            }
            return stb.toString();
        }
    }

    private class StudentKintai implements Comparable<StudentKintai> {
        final String _kintaiCd;
        final String _chairCd;
        final String _periodCd;
        public StudentKintai(
                final String kintaiCd,
                final String periodCd,
                final String chairCd) {
            _kintaiCd = kintaiCd;
            _chairCd = chairCd;
            _periodCd = periodCd;
        }
        public int compareTo(final StudentKintai sk) {
            return _periodCd.compareTo(sk._periodCd);
        }
    }

    private class Kintai {
        final String _cd;

        final boolean _isPrint;

        Kintai(final String cd, final boolean isPrint) {
            _cd = cd;
            _isPrint = isPrint;
        }

        String getName() {
            return _param.getKintaiName(_cd);
        }
    }
}
