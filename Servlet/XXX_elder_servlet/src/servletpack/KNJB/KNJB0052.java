package servletpack.KNJB;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ００５２＞  講座受講生徒の重複チェック
 *
 **/

public class KNJB0052 {

    private static final Log log = LogFactory.getLog(KNJB0052.class);

    private static final String cmd_csv = "csv";
    private static final String cmd_csvContents = "csvContents";

    private Param _param;

    private boolean _hasData = false;   // 該当データ無しフラグ

    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        DB2UDB db2 = null;

        try {
            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch (Exception ex) {
                log.debug("DB2 open error!", ex);
            }
            _param = createParam(request, db2);

            // 印字メイン

            final List chairList = ChairStudent.load(db2, _param);
            final List studentList = ChairStudent.getPrintStudentList(_param, chairList);
            if ("1".equals(_param._csv) || cmd_csvContents.equals(_param._cmd)) {
                final String title = "講座受講生徒の重複チェック";
                final List<List<String>> outputLines = outputCsv(request, response, title, studentList, chairList);

                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                if (cmd_csvContents.equals(_param._cmd)) {
                    final Map map = new HashMap();
                    map.put("TITLE", title);
                    map.put("OUTPUT_LINES", outputLines);
                    CsvUtils.outputJson(log, request, response, CsvUtils.toJson(map), csvParam);

                } else {
                    final String filename = title + ".csv";
                    CsvUtils.outputLines(log, response, filename, outputLines, csvParam);
                }

            } else {
                printMain(response, studentList, chairList);
            }
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private List<List<String>> outputCsv(final HttpServletRequest request, final HttpServletResponse response, final String title, final List<ChairStudent> studentList, final List<Chair> chairList) {
        final List<List<String>> outputLines = new ArrayList();
        try {
            outputLines.add(Arrays.asList(new String[] {null, null, null, title}));
            outputLines.add(Arrays.asList(new String[] {null, null, null, null, null, null, _param._now})); // 作成日時

            final List header = new ArrayList();
            header.addAll(Arrays.asList(new String[] {null, null, null}));
            for (int coli = 0; coli < chairList.size(); coli++) {
                final Chair chair = (Chair) chairList.get(coli);
                header.add(chair._chairname); // 講座名
            }
            outputLines.add(header);
            for (int linei = 0; linei < studentList.size(); linei++) {
                final ChairStudent student = studentList.get(linei);
                final List dataline = new ArrayList();
                outputLines.add(dataline);
                dataline.add(StringUtils.defaultString(student._hrName) + (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) + "番" : student._attendno)); // 年組番
                dataline.add(student._schregno); // 学籍番号
                dataline.add(student._name); // 生徒氏名

                for (int coli = 0; coli < chairList.size(); coli++) {
                    final Chair chair = chairList.get(coli);
                    if (chair._studentList.contains(student)) { // 名簿がある
                        dataline.add("○"); // 重複チェック
                    } else {
                        dataline.add(null);
                    }
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return outputLines;
    }

    private static List getPageList(final List studentlist, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = studentlist.iterator(); it.hasNext();) {
            final ChairStudent student = (ChairStudent) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
        }
        return rtn;
    }

    /** 印刷処理メイン */
    private void printMain(final HttpServletResponse response, final List studentList, final List chairList) {
        final String form = "KNJB0052.frm";
        final int maxLine = 50;

        final Vrw32alp svf = new Vrw32alp();
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());


            for (final Iterator it = getPageList(studentList, maxLine).iterator(); it.hasNext();) {
                final List pageStudents = (List) it.next();

                svf.VrSetForm(form, 1);
                svf.VrsOut("TIMESTAMP", _param._now); // 作成日時

                for (int coli = 0; coli < chairList.size(); coli++) {
                    final Chair chair = (Chair) chairList.get(coli);
                    final String col = String.valueOf(coli + 1);
                    if (null != chair._chairname && chair._chairname.length() > 5) {
                        final String name2 = chair._chairname.substring(0, 5);
                        final String name3 = chair._chairname.substring(5, Math.min(10, chair._chairname.length()));
                        svf.VrsOut("CHAIRNAME" + col + "_2", name2); // 講座名
                        svf.VrsOut("CHAIRNAME" + col + "_3", name3); // 講座名
                    } else {
                        svf.VrsOut("CHAIRNAME" + col, chair._chairname); // 講座名
                    }
                }
                for (int linei = 0; linei < pageStudents.size(); linei++) {
                    final ChairStudent student = (ChairStudent) pageStudents.get(linei);
                    final int line = linei + 1;
                    svf.VrsOutn("ATTENDNO", line, StringUtils.defaultString(student._hrName) + (NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) + "番" : student._attendno)); // 年組番
                    svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                    svf.VrsOutn("NAME", line, student._name); // 生徒氏名

                    for (int coli = 0; coli < chairList.size(); coli++) {
                        final Chair chair = (Chair) chairList.get(coli);
                        final String col = String.valueOf(coli + 1);
                        if (chair._studentList.contains(student)) { // 名簿がある
                            svf.VrsOutn("CHECK" + col, line, "○"); // 重複チェック
                        }
                    }
                }
                svf.VrEndPage();
                _hasData = true;
            }
        } catch (Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != svf) {
                svf.VrQuit();
            }
        }
    }

    private static class Chair {
        final String _chaircd;
        final String _chairname;
        final List _studentList = new ArrayList();

        Chair(
            final String chaircd,
            final String chairname
        ) {
            _chaircd = chaircd;
            _chairname = chairname;
        }

        private static Chair getChair(final List list, final String chaircd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Chair chair = (Chair) it.next();
                if (chair._chaircd.equals(chaircd)) {
                    return chair;
                }
            }
            return null;
        }
    }

    private static class ChairStudent {
        final String _schregno;
        final String _name;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _hrName;

        ChairStudent(
            final String schregno,
            final String name,
            final String grade,
            final String hrClass,
            final String attendno,
            final String hrName
        ) {
            _schregno = schregno;
            _name = name;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _hrName = hrName;
        }

        private static class StudentSorter1 implements Comparator<ChairStudent> {
            public int compare(final ChairStudent s1, final ChairStudent s2) {
                return s1._schregno.compareTo(s2._schregno);
            }
        }

        private static class StudentSorter2 implements Comparator<ChairStudent> {
            public int compare(final ChairStudent s1, final ChairStudent s2) {
                int cmp;
                if (null == s1._grade) return 1;
                if (null == s2._grade) return -1;
                cmp = s1._grade.compareTo(s2._grade);
                if (0 != cmp) { return cmp; }
                if (null == s1._hrClass) return 1;
                if (null == s2._hrClass) return -1;
                cmp = s1._hrClass.compareTo(s2._hrClass);
                if (0 != cmp) { return cmp; }
                if (null == s1._attendno) return 1;
                if (null == s2._attendno) return -1;
                cmp = s1._attendno.compareTo(s2._attendno);
                return cmp;
            }
        }

        public static List getPrintStudentList(final Param param, final List chairList) {
            final Set students = new HashSet();
            for (final Iterator it = chairList.iterator(); it.hasNext();) {
                final Chair chair = (Chair) it.next();
                if ("2".equals(param._outDiv)) {
                    if (param._kijikuChaircd.equals(chair._chaircd)) {
                        students.addAll(chair._studentList);
                    }
                } else { // if ("1".equals(param._output)) {
                    students.addAll(chair._studentList);
                }
            }
            final List studentList = new ArrayList(students);
            if ("2".equals(param._orderDiv)) {
                Collections.sort(studentList, new StudentSorter2());
            } else {
                Collections.sort(studentList, new StudentSorter1());
            }
            log.debug(" studentList size = " + studentList.size());
            return studentList;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List chairList = new ArrayList();
            final Map students = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String chaircd = rs.getString("CHAIRCD");

                    if (null == Chair.getChair(chairList, chaircd)) {
                        final String chairname = rs.getString("CHAIRNAME");
                        final Chair chair = new Chair(chaircd, chairname);
                        chairList.add(chair);
                    }

                    final Chair chair = Chair.getChair(chairList, chaircd);

                    final String schregno = rs.getString("SCHREGNO");
                    if (null == schregno) {
                        continue;
                    }
                    if (null == students.get(schregno)) {
                        final String name = rs.getString("NAME");
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String attendno = rs.getString("ATTENDNO");
                        final String hrName = rs.getString("HR_NAME");
                        final ChairStudent chairStudent = new ChairStudent(schregno, name, grade, hrClass, attendno, hrName);
                        students.put(schregno, chairStudent);
                    }
                    final ChairStudent chairStudent = (ChairStudent) students.get(schregno);
                    chair._studentList.add(chairStudent);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            if ("2".equals(param._outDiv)) {
                Chair target = null;
                // 基軸を除く
                for (final Iterator it = chairList.iterator(); it.hasNext();) {
                    final Chair chair = (Chair) it.next();
                    if (param._kijikuChaircd.equals(chair._chaircd)) {
                        target = chair;
                        it.remove();
                        break;
                    }
                }
                // 基軸の名簿と生徒が重複する講座を除く
                for (final Iterator it = chairList.iterator(); it.hasNext();) {
                    final Chair chair = (Chair) it.next();
                    boolean daburi = false;
                    for (Iterator its = chair._studentList.iterator(); its.hasNext();) {
                        final ChairStudent student = (ChairStudent) its.next();
                        if (target._studentList.contains(student)) {
                            daburi = true;
                            break;
                        }
                    }
                    if (daburi) {
                        it.remove();
                    }
                }
                chairList.add(0, target); // 基軸を先頭に追加
            }
            return chairList;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH REGD AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T3.NAME, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T5.HR_NAME ");
            stb.append("   FROM SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("   INNER JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("     AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T5.GRADE = T1.GRADE ");
            stb.append("     AND T5.HR_CLASS = T1.HR_CLASS ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE AND GDAT.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
            stb.append("   WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.CHAIRNAME, ");
            stb.append("     T3.SCHREGNO, ");
            stb.append("     T3.NAME, ");
            stb.append("     T3.GRADE, ");
            stb.append("     T3.HR_CLASS, ");
            stb.append("     T3.ATTENDNO, ");
            stb.append("     T3.HR_NAME ");
            stb.append(" FROM CHAIR_DAT T1 ");
            stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     AND '" + param._date + "' BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(" LEFT JOIN REGD T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                if (!"".equals(param._selectSchoolKind)) {
                    stb.append(" AND T1.SCHOOL_KIND IN (" + param._selectSchoolKind + ") ");
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append(" AND T1.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
            if ("2".equals(param._outDiv)) {
                stb.append("     AND (T1.CHAIRCD IN " + SQLUtils.whereIn(true, param._categorySelected) + " OR T1.CHAIRCD = '" + param._kijikuChaircd + "' ) ");
            } else {
                stb.append("     AND T1.CHAIRCD IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T3.GRADE, ");
            stb.append("     T3.HR_CLASS, ");
            stb.append("     T3.ATTENDNO ");
            return stb.toString();
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75910 $ $Date: 2020-08-07 15:38:56 +0900 (金, 07 8 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    /**
     * パラメータクラス
     */
    private class Param {
        final String _cmd;
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _date;
        final String _outDiv; // 1:すべて 2:基軸
        final String _orderDiv; // 1:学籍番号 2:出席番号
        final String _now;
        final String[] _categorySelected;
        final String _csv;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOL_KIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        String _kijikuChaircd = "";

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _cmd = request.getParameter("cmd");
            _year = StringUtils.split(request.getParameter("YEAR_SEMESTER"), "-")[0];
            _semester = StringUtils.split(request.getParameter("YEAR_SEMESTER"), "-")[1];
            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("EXECUTEDATE").replace('/', '-');
            _outDiv = request.getParameter("OUT_DIV");
            if (cmd_csvContents.equals(_cmd)) {
                _categorySelected = StringUtils.split(request.getParameter("category_selected"), ",");
            } else {
                _categorySelected = request.getParameterValues("category_selected");
            }
            _csv = request.getParameter(cmd_csv);
            if ("2".equals(_outDiv)) {
                _kijikuChaircd = request.getParameter("KIJIKU_CHAIRCD");
            }
            final Calendar cal = Calendar.getInstance();
            final String hour = String.valueOf(cal.get(Calendar.HOUR));
            final String minute = String.valueOf(cal.get(Calendar.MINUTE));
            _now = KNJ_EditDate.getAutoFormatDate(db2, _ctrlDate) + hour + "時" + minute + "分";
            _orderDiv = request.getParameter("ORDER_DIV");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");
            _use_prg_schoolkind= request.getParameter("use_prg_schoolkind");
            String selectSchoolKind = "";
            String sep = "";
            if (!StringUtils.isEmpty(request.getParameter("selectSchoolKind"))) {
                String[] schoolKind = request.getParameter("selectSchoolKind").split(":");
                for (int i = 0; i < schoolKind.length; i++) {
                    selectSchoolKind += sep + "'" + schoolKind[i] + "'";
                    sep = ",";
                }
            }
            _selectSchoolKind= selectSchoolKind;
        }
    }

}  //クラスの括り
