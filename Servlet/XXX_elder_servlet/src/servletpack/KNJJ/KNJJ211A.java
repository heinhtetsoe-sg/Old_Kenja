/*
 * $Id: 847af17c3d15419b187043eab2aa21857a87c1ca $
 *
 * 作成日: 2017/01/24
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJJ211A {

    private static final Log log = LogFactory.getLog(KNJJ211A.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        boolean isCsv = false;
        try {
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            isCsv = "csv".equals(_param._cmd);

            final List hrClassListAll = Hrclass.getHrclassList(db2, _param);

            if (isCsv) {
                CsvUtils.outputLines(log, response, getTitle() + ".csv", getCsvOutputLines(response, hrClassListAll));

            } else {
                response.setContentType("application/pdf");

                printMain(hrClassListAll, svf);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (isCsv) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private List getCsvOutputLines(final HttpServletResponse response, final List hrList) {
        final List lines = new ArrayList();

        final List header1 = nextLine(lines);
        final List header2 = nextLine(lines);

        header1.addAll(Arrays.asList(new String[] {getTitle(), "", "", "", "", "", KNJ_EditDate.h_format_JP(_param._ctrlDate)})); // タイトル
        header2.add("");

        int maxattendno = 0;
        for (int i = 0; i < hrList.size(); i++) {
            final Hrclass hr = (Hrclass) hrList.get(i);
            for (int sti = 0; sti < hr._studentList.size(); sti++) {
                final Student student = (Student) hr._studentList.get(sti);
                if (NumberUtils.isDigits(student._attendno)) {
                    maxattendno = Math.max(maxattendno, Integer.parseInt(student._attendno));
                }
            }
        }

        final Map lineMap = new HashMap();
        for (int i = 1; i <= maxattendno; i++) {
            final String attendno = String.valueOf(i);
            final List line = nextLine(lines);
            line.add(null);
            lineMap.put(attendno, line);
        }

        for (int hri = 0; hri < hrList.size(); hri++) {
            final Hrclass hr = (Hrclass) hrList.get(hri);
            header2.add(hr._hrName); // 年組名称
            header2.add(null);

            final Map attendnoMap = new HashMap();
            for (int sti = 0; sti < hr._studentList.size(); sti++) {
                final Student student = (Student) hr._studentList.get(sti);
                if (NumberUtils.isDigits(student._attendno)) {
                    attendnoMap.put(String.valueOf(Integer.parseInt(student._attendno)), student);
                }
            }

            for (int i = 1; i <= maxattendno; i++) {
                final String attendno = String.valueOf(i);
                final Student student = (Student) attendnoMap.get(attendno);
                final List line = (List) lineMap.get(attendno);
                if (null == student) {
                    line.add("");
                    line.add("");
                    continue;
                }

                line.set(0, attendno); // 番号

                if ("1".equals(_param._nameNasi) && null != student._grdDiv) {
                    line.add("");
                    line.add("");
                } else {
                    final String name = "1".equals(_param._nameKana) ? student._nameKana : student._name;
                    line.add(name); // 氏名
                    if ("1".equals(_param._nameListOnly)) {
                        line.add(null); // 役職・年度
                    } else {
                        line.add(student.getExecutiveName(_param)); // 役職・年度
                    }
                }
            }
            _hasData = true;
        }
        return lines;
    }

    private static List nextLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
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

    private String getTitle() {
        return KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度 " + StringUtils.defaultString(_param._schoolname1) + " " + StringUtils.defaultString(_param._j006name) + "名簿";
    }

    private void printMain(final List hrclassList, final Vrw32alp svf) {

        final int hrmax = 14;
        final String form = "KNJJ211A.frm";

        final List pageList = getPageList(hrclassList, hrmax);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List hrList = (List) pageList.get(pi);

            svf.VrSetForm(form, 4);
            svf.VrsOut("TITLE", getTitle()); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate) + " " + String.valueOf(pi + 1) + "/" + String.valueOf(pageList.size())); // 日付

            for (int i = 0; i < hrList.size(); i++) {
                final Hrclass hr = (Hrclass) hrList.get(i);
                svf.VrsOut("HR_NAME", hr._hrName); // 年組名称
                for (int sti = 0; sti < hr._studentList.size(); sti++) {
                    final Student student = (Student) hr._studentList.get(sti);
                    if (!NumberUtils.isDigits(student._attendno)) {
                        continue;
                    }
                    final String attendno = String.valueOf(Integer.parseInt(student._attendno));
                    svf.VrsOutn("NO", Integer.parseInt(student._attendno), attendno); // 番号

                    if ("1".equals(_param._nameNasi) && null != student._grdDiv) {
                    } else {
                        final String name = "1".equals(_param._nameKana) ? student._nameKana : student._name;
                        final int namelen = getMS932ByteLength(name);
                        svf.VrsOut("NAME" + attendno + "_" + (namelen <= 10 ? "1" : namelen <= 14 ? "2" : "3"), name); // 氏名
                    }
                    if (!"1".equals(_param._nameListOnly)) {
                        svf.VrsOut("JOB" + attendno, student.getExecutiveName(_param)); // 役職・年度
                    }
                }
                svf.VrEndRecord();
                _hasData = true;
            }
            for (int i = hrList.size(); i < hrmax; i++) {
                svf.VrsOut("HR_NAME", "　\n　"); // 年組名称
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private static class Hrclass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final List _studentList = new ArrayList();

        Hrclass(
            final String grade,
            final String hrClass,
            final String hrName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
        }

        public static List getHrclassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map gradeHrclassMap = new HashMap();
                final Map studentMap = new HashMap();

                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    if (null == gradeHrclassMap.get(grade + hrClass)) {
                        final String hrName = rs.getString("HR_NAME");
                        final Hrclass hr = new Hrclass(grade, hrClass, hrName);
                        list.add(hr);
                        gradeHrclassMap.put(grade + hrClass, hr);
                    }
                    final Hrclass hr = (Hrclass) gradeHrclassMap.get(grade + hrClass);

                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final String nameKana = rs.getString("NAME_KANA");
                        final String grdDiv = rs.getString("GRD_DIV");
                        final Student student = new Student(attendno, name, nameKana, grdDiv);
                        hr._studentList.add(student);
                        studentMap.put(schregno, student);
                    }

                    final String executiveYear = rs.getString("EXECUTIVE_YEAR");
                    if (null != executiveYear) {
                        final String executiveAbbv = rs.getString("EXECUTIVE_ABBV");
                        final Student student = (Student) studentMap.get(schregno);
                        student._executiveMap.put(executiveYear, executiveAbbv);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO ");
            stb.append("   , REGD.GRADE ");
            stb.append("   , REGD.HR_CLASS ");
            stb.append("   , REGDH.HR_NAME ");
            stb.append("   , REGD.ATTENDNO ");
            stb.append("   , BASE.NAME ");
            stb.append("   , BASE.NAME_KANA ");
            stb.append("   , BASE.GRD_DIV ");
            stb.append("   , GCOMD.YEAR AS EXECUTIVE_YEAR ");
            stb.append("   , GCOMM.DIV ");
            stb.append("   , GCOMM.EXECUTIVECD ");
            stb.append("   , GCOMM.ABBV AS EXECUTIVE_ABBV ");
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("     AND REGDG.GRADE = REGD.GRADE ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGDH.GRADE = REGD.GRADE ");
            stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" LEFT JOIN GUARDIAN_COMMITTEE_DAT GCOMD ON GCOMD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND GCOMD.YEAR >= '" + (Integer.parseInt(param._ctrlYear) - 2)  + "' "); // 直近2年
            stb.append("     AND GCOMD.DIV = '" + param._div + "' ");
            stb.append(" LEFT JOIN GUARDIAN_COMMITTEE_YDAT GCOMY ON GCOMY.YEAR = GCOMD.YEAR ");
            stb.append("     AND GCOMY.DIV = GCOMD.DIV ");
            stb.append("     AND GCOMY.EXECUTIVECD = GCOMD.EXECUTIVECD ");
            stb.append(" LEFT JOIN GUARDIAN_COMMITTEE_MST GCOMM ON GCOMM.DIV = GCOMY.DIV ");
            stb.append("     AND GCOMM.EXECUTIVECD = GCOMY.EXECUTIVECD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND REGDG.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            stb.append("     AND (GCOMD.YEAR IS NULL OR GCOMM.DIV IS NOT NULL) ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE ");
            stb.append("   , REGD.HR_CLASS ");
            stb.append("   , REGD.ATTENDNO ");
            stb.append("   , GCOMD.YEAR ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _attendno;
        final String _name;
        final String _nameKana;
        final String _grdDiv;
        final Map _executiveMap = new TreeMap();

        Student(
            final String attendno,
            final String name,
            final String nameKana,
            final String grdDiv
        ) {
            _attendno = attendno;
            _name = name;
            _nameKana = nameKana;
            _grdDiv = grdDiv;
        }

        public String getExecutiveName(final Param param) {
            String executiveName = (String) _executiveMap.get(param._ctrlYear);
            if (null == executiveName) {
                // ログイン年度にデータがなければ過去の年度の西暦下2桁を表示
                final DecimalFormat df = new DecimalFormat("00");
                executiveName = "";
                for (final Iterator it = _executiveMap.keySet().iterator(); it.hasNext();) {
                    final String year = (String) it.next();
                    if (NumberUtils.isDigits(year)) {
                        if (executiveName.length() > 0) {
                            executiveName += " ";
                        }
                        executiveName += df.format(Integer.parseInt(year) % 100); // 西暦下2桁
                    }
                }
            }
            return executiveName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String[] _classSelected;
        final String _div;
        final String _nameNasi; // 転学・退学・卒業生は空欄
        final String _nameKana; // かな氏名
        final String _nameListOnly; // 名簿のみ出力
        final String _schoolKind;
        final String _cmd;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        final String _schoolname1;
        final String _j006name;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _div = request.getParameter("DIV");
            _nameNasi = request.getParameter("NAME_NASI");
            _nameKana = request.getParameter("NAME_KANA");
            _nameListOnly = request.getParameter("NAMELIST_ONLY");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _cmd = request.getParameter("cmd");

            _schoolname1 = getSchoolname1(db2);
            _j006name = (String) getJ006NameMap(db2).get(_div);
        }

        private String getSchoolname1(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    sql += " AND SCHOOL_KIND = '" + _schoolKind + "' ";
                }
                if ("1".equals(_useSchool_KindField) && null != _schoolKind) {
                    sql += " AND SCHOOL_KIND = '" + _schoolKind + "' ";
                }
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static Map getJ006NameMap(final DB2UDB db2) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'J006' ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }
    }
}

// eof

