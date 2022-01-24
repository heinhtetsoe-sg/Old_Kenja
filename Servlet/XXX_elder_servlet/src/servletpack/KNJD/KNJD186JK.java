/*
 *
 * 作成日: 2014/12/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 *  学校教育システム 賢者 IB通知表印刷(MYP, DP)
 */
public class KNJD186JK {

    private static final Log log = LogFactory.getLog(KNJD186JK.class);

    private static final String SEMEALL = "9";

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
        FormCommon form = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            if (_param.isFormDP()) {
                form = new FormDP();
            } else {
                form = new FormMYP(_param);
            }
            if (_param._isCsv) {
                CsvUtil.outputLines(response, form.getCsvTitle(db2) + ".csv", form.getCsvLines(db2));
            } else {
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                form.printMain(db2, svf);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != _param) {
                _param.psClose();
            }
            if (null != _param && _param._isCsv) {
            } else {
                if (null == form || !form._hasData) {
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

    private static List newLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private static Map getMappedMap(final Map m, final String key) {
        if (null == m) {
            return new HashMap();
        }
        if (null == m.get(key)) {
            m.put(key, new HashMap());
        }
        return (Map) m.get(key);
    }

    private static String getString(final Map m, final String key) {
        return (String) m.get(key);
    }

    private static String addNum(final Object s1, final Object s2) {
        if ((null == s1 || !NumberUtils.isDigits(s1.toString())) && (null == s2 || !NumberUtils.isDigits(s2.toString()))) {
            return null;
        }
        final int n1 = null == s1 ? 0 : Integer.parseInt(s1.toString());
        final int n2 = null == s2 ? 0 : Integer.parseInt(s2.toString());
        final String rtn = String.valueOf(n1 + n2);
        //log.debug(" addNum " + s1 + ", " + s2 + " -> " + rtn);
        return rtn;
    }

    private static String sql(final String psSql, final Object[] args) {
        String tmp = psSql;

        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            final String q =  o instanceof Number ? o.toString() : quote(o);
            tmp = StringUtils.replaceOnce(tmp, "?", q);
        }
        return tmp;
    }

    private static String quote(Object o) {
        if (o instanceof Number) {
            return o.toString();
        }
        return null == o ? "null" : ("'" + o.toString() + "'");
    }

    private abstract class FormCommon {

        protected abstract void printHyoshi(final Vrw32alp svf, final Student student);

        protected abstract void printSeisekiHoukoku(final DB2UDB db2, final Vrw32alp svf, final Student student);

        protected abstract void printIbSubclass(final DB2UDB db2, final Vrw32alp svf, final Student student);
        protected abstract void csvIbSubclass(final DB2UDB db2, final List list, final Student student);

        protected abstract void printShomei(final Vrw32alp svf, final Student student);

        protected String maru1 = "\u2460";
        protected String maru2 = "\u2461";

        private int _maxPage = 0;
        private int _currentPage = 0;
        private boolean _addBlank = false;

        protected boolean _hasData = false;

        protected final DecimalFormat _df02 = new DecimalFormat("00");

        protected String currentform;
        protected List printSchoolNameJpEngList = new ArrayList();

        protected int setForm(final Vrw32alp svf, final String form, final int n) {
            currentform = form;
            return svf.VrSetForm(form, n);
        }

        public void printMain(final DB2UDB db2, final Vrw32alp svf) {

            final List studentList = getStudentList(db2);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                log.debug(" schregno = " + student._schregno);

                _addBlank = false;
                _maxPage = 0;
                if (_param._isPrintHyoshi) {
                    if (_param.isFormDP()) {
                        _maxPage += 2;
                    } else {
                        _maxPage += 4;
                    }
                }
                if (_param._isPrintSeisekiHoukoku) {
                    _maxPage += 2;
                }
                if (_param._isPrintHyouka1 || _param._isPrintHyouka2) {
                    if (_param._isPrintHyouka1) {
                        _maxPage += student._ibsubclassList.size();
                    }
                    if (_param._isPrintHyouka2) {
                        _maxPage += student._ibsubclassList.size();
                    }
                }
                if (_param._isPrintShomei) {
                    if (_maxPage % 2 == 1) {
                        // 確認書のページが奇数になるようにブランクのページを出力
                        _addBlank = true;
                        _maxPage += 1;
                    }
                    _maxPage += 2;
                }
                _currentPage = 1;

                if (_param._isPrintHyoshi) {
                    printHyoshi(svf, student);
                }
                if (_param._isPrintSeisekiHoukoku) {
                    printSeisekiHoukoku(db2, svf, student);
                }
                if (_param._isPrintHyouka1 || _param._isPrintHyouka2) {
                    printIbSubclass(db2, svf, student);
                }
                if (_param._isPrintShomei) {
                    printShomei(svf, student);
                }
            }
        }

        public List getCsvLines(final DB2UDB db2) {
            final List studentList = getStudentList(db2);

            final List lines = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                csvIbSubclass(db2, lines, student);
            }
            return lines;
        }

        private String getCsvTitle(final DB2UDB db2) {
            final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
            final String title = StringUtils.defaultString(_param._semesterName);
            final String gradename = "IB評価の記録" + ("KNJD186I".equals(_param._prgid) ? "DP" : "MYP");
            final String shubetsu = "評価の記録" + (_param._isPrintHyouka1 ? maru1 : _param._isPrintHyouka2 ? maru2 : "");
            return nendo + title + gradename + shubetsu;
        }

        protected void endPage(final Vrw32alp svf, final int n) {
            if (n == 1) {
                svf.VrEndPage();
            }
            _currentPage += 1;
        }

        protected void printHyoshi(final Vrw32alp svf, final String form1, final String form2, final String form3, final String form4, final Student student) {
            final int n = 1;
            setForm(svf, form1, n);
            printHeader(svf, student);
            svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolSchoolName); // 学校名
            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolRemark3); // 学校名
            if (null != _param.getImagePath("SCHOOLLOGO." + _param._extension)) {
                svf.VrsOut("SCHOOL_LOGO", _param.getImagePath("SCHOOLLOGO." + _param._extension)); //
            }
            printFooter(svf, student);
            endPage(svf, n);

            setForm(svf, form2, n);
            printFooter(svf, student);
            endPage(svf, n);

            if (null != form3) {
                setForm(svf, form3, n);
                printFooter(svf, student);
                endPage(svf, n);
            }

            if (null != form4) {
                setForm(svf, form4, n);
                printFooter(svf, student);
                endPage(svf, n);
            }

            _hasData = true;
        }

        protected void printShomei(final Vrw32alp svf, final String form1, final String form2, final Student student) {
            final int n = 1;
            if (_addBlank) {
                setForm(svf, form2, n);
                printFooter(svf, student);
                endPage(svf, n);
            }

            setForm(svf, form1, n);
            printHeader(svf, student);
            printFooter(svf, student);
            endPage(svf, n);

            setForm(svf, form2, n);
            printHeader(svf, student);
            printFooter(svf, student);
            endPage(svf, n);
            _hasData = true;
        }

        protected List getStudentList(final DB2UDB db2) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = Student.getStudentSql(_param);
                //log.debug(" student sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendno = rs.getString("ATTENDNO");
                    final String tr1Name = rs.getString("TR1_NAME");
                    final String tr2Name = rs.getString("TR2_NAME");
                    final String tr3Name = rs.getString("TR3_NAME");
                    final Student student = new Student(schregno, name, schoolKind, hrClass, attendno, tr1Name, tr2Name, tr3Name);
                    student._footerAttendno = _param._gradeCd + "-" + _param.intToString(hrClass, "") + "-" + _param.intToString(attendno, "");
                    studentList.add(student);
                }

            } catch (Exception e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            final StringBuffer subclassPhaseSql = new StringBuffer();
            subclassPhaseSql.append(" SELECT ");
            subclassPhaseSql.append("    T1.IBCLASSCD || '-' || T1.IBPRG_COURSE AS IBCLASSCD_PRG_COURSE, ");
            subclassPhaseSql.append("    T1.SCORE ");
            subclassPhaseSql.append(" FROM IBSUBCLASS_GRADE_PHASE_DAT T1 ");
            subclassPhaseSql.append(" WHERE T1.IBYEAR = '" + _param._year + "' ");
            subclassPhaseSql.append("   AND T1.SEMESTER = '" + _param._phaseSemester + "' ");
            subclassPhaseSql.append("   AND T1.SCHREGNO = ? ");
            if (_param._isOutputDebug) {
                log.info(" phase sql = " + subclassPhaseSql);
            }
            PreparedStatement subclassPhasePs = null;
            try {
                subclassPhasePs = db2.prepareStatement(subclassPhaseSql.toString());
            } catch (Exception e) {
                log.error("exception!!", e);
            }

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                if (null != subclassPhasePs) {
                    for (final Iterator pit = KnjDbUtils.query(db2, subclassPhasePs, new Object[] { student._schregno } ).iterator(); pit.hasNext();) {
                        final Map row = (Map) pit.next();

                        student._ibclasscdPrgCourseGradePhaseMap.put(KnjDbUtils.getString(row, "IBCLASSCD_PRG_COURSE"), KnjDbUtils.getString(row, "SCORE"));
                    }
                }

                if (_param._isPrintSeisekiHoukoku) {
                    final Attend att1 = Attend.getAttendSemesDat(db2, _param, student._schregno, (PreparedStatement) _param._psMap.get("ATTEND_SEMES"));
                    att1._sdate = _param._sdate;
                    att1._edate = _param._date;
                    student._attendSemesMap.put(_param._year, att1);
                    if (_param._isDp2) {
                        final Attend att2 = Attend.getAttendSemesDat(db2, _param, student._schregno, (PreparedStatement) _param._psMap.get("ATTEND_SEMES_LAST_YEAR"));
                        att2._sdate = _param._sdateBeforeYear;
                        att2._edate = _param._edateBeforeYear;
                        student._attendSemesMap.put(_param._yearBefore, att2);
                    }
                    setHreportRemarkDetailDatMap(db2, student);
                }
                if (_param._isPrintHyouka1 || _param._isPrintHyouka2 || _param._isPrintSeisekiHoukoku) {
                    IBSubclass.setIBSubclassList(db2, _param, student);
                    final Map attsub1 = Attend.getAttendSubclassDatMap(db2, _param, student._schregno, (PreparedStatement) _param._psMap.get("ATTEND_SUBCLASS"));
                    attsub1.put("SDATE", _param._sdate);
                    attsub1.put("EDATE", _param._date);
                    student._attendSubclassMap.put(_param._year, attsub1);
                    if (_param._isDp2) {
                        final Map attsub2 = Attend.getAttendSubclassDatMap(db2, _param, student._schregno, (PreparedStatement) _param._psMap.get("ATTEND_SUBCLASS_LAST_YEAR"));
                        attsub2.put("SDATE", _param._sdateBeforeYear);
                        attsub2.put("EDATE", _param._edateBeforeYear);
                        student._attendSubclassMap.put(_param._yearBefore, attsub2);
                    }
                }
            }

            DbUtils.closeQuietly(subclassPhasePs);

            return studentList;
        }


        private void setHreportRemarkDetailDatMap(final DB2UDB db2, final Student student) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            student._hreportremarkDetailDatMap = new HashMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT * FROM HREPORTREMARK_DETAIL_DAT ");
                sql.append(" WHERE (YEAR = '" + _param._year + "' ");
                if (_param._isDp2) {
                    sql.append("    OR YEAR = '" + _param._yearBefore + "' ");
                }
                sql.append("    ) AND SCHREGNO = '" + student._schregno + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key = rs.getString("YEAR") + rs.getString("SEMESTER") + rs.getString("DIV") + rs.getString("CODE");
                    final Map data = getMappedMap(student._hreportremarkDetailDatMap, key);
                    data.put("REMARK1", rs.getString("REMARK1"));
                    data.put("REMARK2", rs.getString("REMARK2"));
                    data.put("REMARK3", rs.getString("REMARK3"));
                    data.put("REMARK4", rs.getString("REMARK4"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        protected List retDividString(final String targetsrc0, final int ketamax) {
            if (targetsrc0 == null) {
                return Collections.EMPTY_LIST;
            }
            final List lines = new ArrayList();         //編集後文字列を格納する配列
            int ketacurrent = 0;
            StringBuffer stb = new StringBuffer();

            try {
                final String targetsrc;
                if (!StringUtils.replace(targetsrc0, "\r\n", "\n").equals(targetsrc0)) {
                    targetsrc = StringUtils.replace(targetsrc0, "\r\n", "\n");
                } else {
                    targetsrc = targetsrc0;
                }

                final List charList = getCharStringList(targetsrc);

                for (final Iterator it = charList.iterator(); it.hasNext();) {
                    final String c = (String) it.next();
                    //log.debug(" c = " + c);
                    final int cketa = KNJ_EditEdit.getMS932ByteLength(c);
                    if (("\n".equals(c) || "\r".equals(c))) {
                        if (ketacurrent <= ketamax) {
                            lines.add(stb.toString());
                            ketacurrent = 0;
                            stb.delete(0, stb.length());
                        }
                    } else {
                        if (ketacurrent + cketa > ketamax) {
                            lines.add(stb.toString());
                            ketacurrent = 0;
                            stb.delete(0, stb.length());
                        }
                        stb.append(c);
                        ketacurrent += cketa;
                    }
                }
                if (0 < ketacurrent) {
                    lines.add(stb.toString());
                }
            } catch (Exception ex) {
                log.error("retDividString error! ", ex);
            }
            return lines;
        }

        protected LinkedList retDividStringMojisu(final String targetsrc0, final int mojisu) {
            final LinkedList lines = new LinkedList();         //編集後文字列を格納する配列
            if (targetsrc0 == null) {
                return lines;
            }
            int len = 0;
            StringBuffer stb = new StringBuffer();

            try {
                final String targetsrc;
                if (!StringUtils.replace(targetsrc0, "\r\n", "\n").equals(targetsrc0)) {
                    targetsrc = StringUtils.replace(targetsrc0, "\r\n", "\n");
                } else {
                    targetsrc = targetsrc0;
                }

                final List charList = getCharStringList(targetsrc);

                for (final Iterator it = charList.iterator(); it.hasNext();) {
                    final String c = (String) it.next();
                    //log.debug(" c = " + c);
                    final int clen = 1;
                    if (("\n".equals(c) || "\r".equals(c))) {
                        if (len <= mojisu) {
                            lines.add(stb.toString());
                            len = 0;
                            stb.delete(0, stb.length());
                        }
                    } else {
                        if (len + clen > mojisu) {
                            lines.add(stb.toString());
                            len = 0;
                            stb.delete(0, stb.length());
                        }
                        stb.append(c);
                        len += clen;
                    }
                }
                if (0 < len) {
                    lines.add(stb.toString());
                }
            } catch (Exception ex) {
                log.error("retDividStringMojisu error! ", ex);
            }
            return lines;
        }

        private List getCharStringList(final String source) {
            final List list = new ArrayList();
            for (int i = 0, len = source.length(); i < len; i++) {
                list.add(String.valueOf(source.charAt(i)));
            }
            return list;
        }

        protected String trimLeft(final String s) {
            if (null == s) {
                return s;
            }
            int start = 0;
            int i = 0;
            while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '　')) {
                i++;
                start = i;
            }
            if (start >= s.length()) {
                return "";
            }
            return s.substring(start);
        }

        protected String percentageString(final String s) {
            if (!NumberUtils.isNumber(s)) {
                return "";
            }
            return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "%";
        }

        protected void svfVrsOutnKurikaeshi(final Vrw32alp svf, final String field, final List list) {
            for (int i = 0; i < list.size(); i++) {
                svf.VrsOutn(field, (i + 1), (String) list.get(i));
            }
        }

        protected void printHeader(final Vrw32alp svf, final Student student) {
            svf.VrsOut("NAME", student._name); // 氏名
            svf.VrsOut("YEAR", _param._year); // 年度
            svf.VrsOut("SEMESTER", _param._semesterName); // 学期
            svf.VrsOut("GRADE", NumberUtils.isDigits(_param._grade) ? String.valueOf(Integer.parseInt(_param._grade)) : ""); // 学年
            svf.VrsOut("MYP_YEAR", _param._gradeName3); // MYP学年
            svf.VrsOut("SCHREG_NO", student._schregno); // 学籍番号
            svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 校長名
            svf.VrsOut("TEACHER_NAME", StringUtils.defaultString(student._tr1Name) + "　　　" + StringUtils.defaultString(student._tr2Name) + "　　　" + StringUtils.defaultString(student._tr3Name)); // 担任氏名

            if (printSchoolNameJpEngList.contains(currentform)) {
                svf.VrsOut("SCHOOL_NAME", StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(_param._certifSchoolRemark3)); // 学校名
            } else {
                svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); // 学校名
            }
        }

        protected void printFooter(final Vrw32alp svf, final Student student) {
            svf.VrsOut("HR_NUM", student._footerAttendno);
            svf.VrsOut("PAGE", String.valueOf(_currentPage) + "/" + String.valueOf(_maxPage));
        }

        protected void printIBSubclassUnitSubclassname(final Vrw32alp svf, final IBSubclass ibsubclass) {
            int subji = 1;
            for (final Iterator it = ibsubclass.getUnitSubclassnameMap(_param).entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclassname = (String) e.getValue();
                svf.VrsOut("SUBJECT" + String.valueOf(subji) + (KNJ_EditEdit.getMS932ByteLength(subclassname) > 10 ? "_2" : ""), subclassname); // 科目
                subji += 1;
            }
        }

        protected void csvIBSubclassUnitSubclassname(final List list, final IBSubclass ibsubclass) {
            for (final Iterator it = ibsubclass.getUnitSubclassnameMap(_param).entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclassname = (String) e.getValue();
                list.add(subclassname);
            }
        }

        protected String getHyotei(final DB2UDB db2, final Param param, final String year, final String semester, final String schregno, final String unitSubclasscd) {
            final Map dataMap = getJviewStatRecordDatMap(db2, _param, _param._year, "1".equals(_param._semester) ? "1" : "9", schregno, unitSubclasscd);
            final Map cdMap = (Map) dataMap.get("0");
            if (null == cdMap) {
                return null;
            }
            return getString(cdMap, "STATUS");
        }

        protected Map getJviewStatRecordDatMap(final DB2UDB db2, final Param param, final String year, final String semester, final String schregno, final String unitSubclasscd) {
            final Map rtn = new HashMap();
            try {
                final String psKey = "JVIEWSTAT_RECORD";
                if (null == param._psMap.get(psKey)) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                    sql.append("     T1.VIEWCD, ");
                    sql.append("     T1.STATUS, ");
                    sql.append("     T2.REMARK1, ");
                    sql.append("     T2.REMARK2, ");
                    sql.append("     T2.REMARK3 ");
                    sql.append(" FROM JVIEWSTAT_RECORD_DAT T1 ");
                    sql.append(" INNER JOIN JVIEWSTAT_RECORD_DETAIL_DAT T2 ON T2.YEAR = T1.YEAR ");
                    sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
                    sql.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                    sql.append("     AND T2.CLASSCD = T1.CLASSCD ");
                    sql.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    sql.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    sql.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                    sql.append("     AND T2.VIEWCD = T1.VIEWCD ");
                    sql.append(" WHERE ");
                    sql.append("     T1.YEAR = ? ");
                    sql.append("     AND T1.SEMESTER = ? ");
                    sql.append("     AND T1.SCHREGNO = ? ");
                    sql.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ? ");

                    param._psMap.put(psKey, db2.prepareStatement(sql.toString()));
                }

                for (final Iterator it = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {year, semester, schregno, unitSubclasscd}).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    if (!NumberUtils.isDigits(KnjDbUtils.getString(row, "VIEWCD"))) {
                        continue;
                    }
                    final String cd = String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "VIEWCD")) % 10);
                    final Map cdMap = new HashMap();
                    cdMap.put("STATUS", KnjDbUtils.getString(row, "STATUS"));
                    cdMap.put("REMARK1", KnjDbUtils.getString(row, "REMARK1"));
                    cdMap.put("REMARK2", KnjDbUtils.getString(row, "REMARK2"));
                    cdMap.put("REMARK3", KnjDbUtils.getString(row, "REMARK3"));
                    rtn.put(cd, cdMap);
                }

                if (null == rtn.get("0") || null == getMappedMap(rtn, "0").get("STATUS")) {
                    // KNJD126Jで更新した際、VIEWCD=0000のJVIEWSTAT_RECORD_DATが消える。
                    final String psKey2 = "JVIEWSTAT_RECORD2";
                    if (null == param._psMap.get(psKey2)) {
                        final StringBuffer sql = new StringBuffer();
                        sql.append(" SELECT ");
                        sql.append("     '0000' AS VIEWCD, ");
                        sql.append("     T1.SCORE AS STATUS, ");
                        sql.append("     T2.REMARK1, ");
                        sql.append("     T2.REMARK2, ");
                        sql.append("     T2.REMARK3 ");
                        sql.append(" FROM RECORD_SCORE_DAT T1 ");
                        sql.append(" INNER JOIN JVIEWSTAT_RECORD_DETAIL_DAT T2 ON T2.YEAR = T1.YEAR ");
                        sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
                        sql.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                        sql.append("     AND T2.CLASSCD = T1.CLASSCD ");
                        sql.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        sql.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                        sql.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
                        sql.append("     AND T2.VIEWCD = '0000' ");
                        sql.append(" WHERE ");
                        sql.append("     T1.YEAR = ? ");
                        sql.append("     AND T1.SEMESTER = ? ");
                        sql.append("     AND T1.SCHREGNO = ? ");
                        sql.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ? ");

                        param._psMap.put(psKey2, db2.prepareStatement(sql.toString()));
                    }

                    for (final Iterator it = KnjDbUtils.query(db2, param.getPs(psKey2), new Object[] {year, semester, schregno, unitSubclasscd}).iterator(); it.hasNext();) {
                        final Map row = (Map) it.next();
                        if (!NumberUtils.isDigits(KnjDbUtils.getString(row, "VIEWCD"))) {
                            continue;
                        }
                        final String cd = String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "VIEWCD")) % 10);
                        final Map cdMap = new HashMap();
                        cdMap.put("STATUS", KnjDbUtils.getString(row, "STATUS"));
                        cdMap.put("REMARK1", KnjDbUtils.getString(row, "REMARK1"));
                        cdMap.put("REMARK2", KnjDbUtils.getString(row, "REMARK2"));
                        cdMap.put("REMARK3", KnjDbUtils.getString(row, "REMARK3"));
                        rtn.put(cd, cdMap);
                    }
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
            }
            return rtn;
        }
    }

    private class FormMYP extends FormCommon {
        final String form5;
        final String form6;
        final String form7;
        final String form8;

        FormMYP(final Param param) {
            form5 = "H".equals(param._schoolKind) ? "KNJD186J_5.frm" : "KNJD186J_5SL.frm";
            form6 = "KNJD186J_6.frm";
            form7 = "KNJD186J_7.frm";
            form8 = "H".equals(param._schoolKind) ? "KNJD186J_8.frm" : "KNJD186J_8SL.frm";
            printSchoolNameJpEngList.add(form5);
            printSchoolNameJpEngList.add(form6);
            printSchoolNameJpEngList.add(form7);
            printSchoolNameJpEngList.add(form8);
        }

        protected void printHyoshi(final Vrw32alp svf, final Student student) {
            final String form1 = "KNJD186J_1.frm";
            final String form2 = "KNJD186J_2.frm";
            final String form3 = "KNJD186J_3.frm";
            final String form4 = "KNJD186J_4.frm";
            printHyoshi(svf, form1, form2, form3, form4, student);
        }

        protected void printSeisekiHoukoku(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            printSeisekiHoukoku1(db2, svf, student);

            printSeisekiHoukoku2(svf, student);
        }

        private void printSeisekiHoukoku1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            final int n = 4;
            setForm(svf, form5, n);
            printHeader(svf, student);

            svf.VrsOut("TITLE", "成績報告書　PROGRESS　REPORT");

            // 成績
            int line = 1;
            String p;
            p = "1";
            final List chairSubclassList = student.getChairSubclassList(_param);
            for (final Iterator csit = chairSubclassList.iterator(); csit.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) csit.next();
                if (_param._d026Name1List.contains(cs._subclasscd)) {
                    csit.remove();
                }
            }
            for (int i = 0, len = student._ibsubclassList.size(); i < len; i++) {
                final IBSubclass ibsubclass = (IBSubclass) student._ibsubclassList.get(i);
                final String grp = String.valueOf(i + 1);
                final Map chairSubclasscdMap = ibsubclass.getUnitSubclassnameMap(_param);
                int printChrsubclasscdidx = 0;
                if (_param._isOutputDebug) {
                    log.info(" ib = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);
                }
                for (final Iterator it = chairSubclasscdMap.keySet().iterator(); it.hasNext();) {
                    final String chrSubclasscd = (String) it.next();
                    final ChairSubclass chrSubclass = ChairSubclass.getChairSubclass(chairSubclassList, chrSubclasscd);
                    if (null == chrSubclass) {
                        continue;
                    }
                    if (_param._isOutputDebug) {
                        log.info("  cs = " + chrSubclass._subclasscd + " : " + chrSubclass._subclassname);
                    }
                    printSeisekiHoukokuIBSubclassname(svf, ibsubclass, printChrsubclasscdidx, grp);
                    printSeisekiHoukokuChrSubclass(db2, svf, student, p, chrSubclass, true);
                    line += 1;
                    printSeisekiHoukokuRecordEtc(svf, student);
                    printFooter(svf, student);
                    svf.VrEndRecord();
                    printChrsubclasscdidx += 1;
                }
                if (printChrsubclasscdidx == 0) {
                    printSeisekiHoukokuIBSubclassname(svf, ibsubclass, 0, grp);
                    line += 1;
                    printSeisekiHoukokuRecordEtc(svf, student);
                    printFooter(svf, student);
                    svf.VrEndRecord();
                }
            }
            p = "2";
            for (int i = 0, len = chairSubclassList.size(); i < len; i++) {
                final ChairSubclass chrSubclass = (ChairSubclass) chairSubclassList.get(i);
                if (chrSubclass._hasIbSubclass) {
                    continue;
                }
                if (_param._isOutputDebug) {
                    log.info(" no ib chair subclass = " + chrSubclass._subclasscd + ": " + chrSubclass._subclassname);
                }
                printSeisekiHoukokuChrSubclass(db2, svf, student, p, chrSubclass, false);
                line += 1;

                printSeisekiHoukokuRecordEtc(svf, student);
                printFooter(svf, student);
                svf.VrEndRecord();
            }

            p = "2";
            for (int i = line, max = 14; i <= max; i++) {
                svf.VrsOut("CLASS_NAME" + p, "　"); // 教科名
                printFooter(svf, student);
                svf.VrEndRecord();
            }
            endPage(svf, n);
            _hasData = true;
        }

        public void printSeisekiHoukokuChrSubclass(final DB2UDB db2, final Vrw32alp svf, final Student student, String p, final ChairSubclass chrSubclass, final boolean hasIbsubclass) {
            if (KNJ_EditEdit.getMS932ByteLength(chrSubclass._classname) > 24) {
                svf.VrsOut("CLASS_NAME" + p + "_2", chrSubclass._classname); // 教科名
            } else {
                svf.VrsOut("CLASS_NAME" + p, chrSubclass._classname); // 教科名
            }

            svf.VrsOut("SUBJECT_NAME" + p + (KNJ_EditEdit.getMS932ByteLength(chrSubclass._subclassname) > 20 ? "_2" : ""), chrSubclass._subclassname); // 科目名
            svf.VrsOut("PEROID" + p, getString(chrSubclass._takeSemesNameMap, _param._year)); // 履修期間
            //svf.VrsOut("UNIT_HOUR" + p, getString(getMappedMap(getMappedMap(student._attendSubclassMap, _param._year), "LESSON"), chrSubclass._subclasscd)); // 単位時間数
            svf.VrsOut("UNIT_HOUR" + p, chrSubclass._timeUnit); // 単位時間数
            svf.VrsOut("ATTEND_TIME" + p, getString(getMappedMap(getMappedMap(student._attendSubclassMap, _param._year), "KEKKA"), chrSubclass._subclasscd)); // 欠課時数
            if (!"1".equals(_param._semester)) {
                if (chrSubclass._subclasscd.startsWith("90")) {
                    svf.VrsOut("DIV" + p, "※" + maru1); // 評定
                } else if (chrSubclass._subclasscd.startsWith("93")) {
                    svf.VrsOut("DIV" + p, "※" + maru2); // 評定
                } else {
                    String hyotei = null;
                    if (hasIbsubclass) {
                        hyotei = getHyotei(db2, _param, _param._year, _param._semester, student._schregno, chrSubclass._subclasscd);
                    }
                    if (null == hyotei) {
                        hyotei = (String) chrSubclass._hyoteiMap.get(_param._year);
                    }
                    if (_param._d072SubclasscdList.contains(chrSubclass._subclasscd)) {
                        //log.info(" D072 hyotei : " + hyotei + " -> " + _param._d071hyoteiAlphabetMap.get(hyotei));
                        hyotei = (String) _param._d071hyoteiAlphabetMap.get(hyotei);
                    }
                    svf.VrsOut("DIV" + p, hyotei); // 評定
                }
                if ("H".equals(student._schoolKind)) {
                    final String credit = (String) chrSubclass._getCreditMap.get(_param._year);
                    svf.VrsOut("CREDIT" + p, credit); // 評定
                }
            }
        }

        public void printSeisekiHoukokuIBSubclassname(final Vrw32alp svf, final IBSubclass ibsubclass, final int idx, final String grp) {
            if (idx == 0) {
                svf.VrsOut("ENG_SUBJECT_NAME1", ibsubclass._ibclassnameEng); // 英語科目名
                //svf.VrsOut("ENG_COURSE_NAME1", ibsubclass._ibsubclassnameEng); // 英語コース名
                if (SEMEALL.equals(_param._semester)) {
                   svf.VrsOut("MYP_GRADE1", ibsubclass._ibGrade); // MYPグレード
                }
            }
            svf.VrsOut("GRP1", grp); // グループ1
            svf.VrsOut("GRP2", grp); // グループ2
            svf.VrsOut("GRP3", grp); // グループ2
        }

        public void printSeisekiHoukokuRecordEtc(final Vrw32alp svf, final Student student) {
            // 総合的な学習の時間
            svfVrsOutnKurikaeshi(svf, "TOTAL_STUDY", retDividString(getString(getMappedMap(student._hreportremarkDetailDatMap, _param._year + "90101"), "REMARK1"), 30 * 2));

            // 道徳
            svfVrsOutnKurikaeshi(svf, "MORALITY", retDividString(getString(getMappedMap(student._hreportremarkDetailDatMap, _param._year + "90201"), "REMARK1"), 30 * 2));

            // 出欠
            final Attend attend = (Attend) student._attendSemesMap.get(_param._year);
            svf.VrsOut("PERIOD", _param.attendRange(attend._sdate, attend._edate)); // 出欠期間
            svf.VrsOut("SCHOOL_DAYS", attend._lesson); // 授業日数
            svf.VrsOut("SUSPEND", addNum(attend._suspend, attend._mourning)); // 出席停止等
            svf.VrsOut("ABROAD", attend._transferDate); // 留学
            svf.VrsOut("MUST", attend._mlesson); // 出席すべき日数
            svf.VrsOut("ABSENCE", attend._sick); // 欠席日数
            svf.VrsOut("ATTEND", attend._present); // 出席日数
            svf.VrsOut("REMARK", null); // 備考
        }

        private void printSeisekiHoukoku2(final Vrw32alp svf, final Student student) {
            final int n = 1;
            setForm(svf, form6, n);

            svf.VrsOut("TITLE", "成果報告書　PROGRESS　REPORT");

            printHeader(svf, student);
            svfVrsOutnKurikaeshi(svf, "ACTIVE1", retDividString(getString(getMappedMap(student._hreportremarkDetailDatMap, _param._year + "90301"), "REMARK4"), 30 * 2));

            printFooter(svf, student);
            endPage(svf, n);
            _hasData = true;
        }

        protected void printIbSubclass(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            for (final Iterator iit = student._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();

                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);
                if (_param._isPrintHyouka1) {
                    printIbSubclass1(svf, student, ibsubclass);
                }
                if (_param._isPrintHyouka2) {
                    printIbSubclass2(svf, db2, student, ibsubclass);
                }
            }
        }

        protected void csvIbSubclass(final DB2UDB db2, final List lines, final Student student) {
            for (final Iterator iit = student._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();

                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);
                if (_param._isPrintHyouka1) {
                    csvIbSubclass1(lines, student, ibsubclass);
                }
                if (_param._isPrintHyouka2) {
                    csvIbSubclass2(lines, db2, student, ibsubclass);
                }
            }
        }

        private void printIbSubclass1(final Vrw32alp svf, final Student student, final IBSubclass ibsubclass) {
            final int n = 4;
            setForm(svf, form7, n);
            printHeader(svf, student);

            // 成績
            svf.VrsOut("ENG_SUBJECT_NAME1", ibsubclass._ibsubclassnameEng); // 英語科目名
            svf.VrsOut("TITLE", "評価の記録" + maru1 + "　ASSESSMENT RECORD " + maru1); // タイトル

            printIBSubclassUnitSubclassname(svf, ibsubclass);

            final int viewmax = 12;
            String ibevalGradeSum = null;
            for (int evali = 0, evallen = Math.min(viewmax, ibsubclass._evalList.size()); evali < evallen; evali++) {
                final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);

                final int line = evali + 1;
                final int len1 = KNJ_EditEdit.getMS932ByteLength(eval._ibevalNameEng);
                final int len2 = KNJ_EditEdit.getMS932ByteLength(eval._ibevalName);
                svf.VrsOutn("VIEW_NO", line, eval._ibevalMark); // 観点番号
                svf.VrsOutn("ENG_VIEW" + (len1 <= 34 ? "1" : len1 <= 40 ? "2" : "3"), line, eval._ibevalNameEng); // 英語観点
                svf.VrsOutn("VIEW" + (len2 <= 34 ? "1" : len2 <= 40 ? "2" : "3"), line, eval._ibevalName); // 観点
                svf.VrsOutn("VIEW_DIV", line, eval._ibevalPerfect); // 観点評価
                svf.VrsOutn("SUB_TOTAL_VIEW", line, eval._ibevalGrade); // 観点総合値
                ibevalGradeSum = addNum(ibevalGradeSum, eval._ibevalGrade);
            }
            svf.VrsOut("TOTAL_VIEW", ibevalGradeSum); // 観点合計値
            if (SEMEALL.equals(_param._semester)) {
                svf.VrsOut("MYP_GRADE", ibsubclass._ibGrade); // MYPグレード
            }
            final String phase = (String) student._ibclasscdPrgCourseGradePhaseMap.get(ibsubclass._ibclasscdPrgCourse);
            if (!StringUtils.isEmpty(phase)) {
                svf.VrsOut("PHASE", "Phase" + phase); // Phase
            }

            final int unitMax = 12;
            for (int unii = 0, unilen = unitMax; unii < unilen; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                    //log.debug(" unit ibseq = " + unit._ibseq);
                    for (int evali = 0, evallen = Math.min(viewmax, ibsubclass._evalList.size()); evali < evallen; evali++) {
                        final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);

                        final Map viewcdScoreMap = getMappedMap(eval._unitViewcdScoreMap, unit._ibseq);
                        for (final Iterator vit = viewcdScoreMap.keySet().iterator(); vit.hasNext();) {
                            final String viewcd = (String) vit.next();
                            if (!NumberUtils.isDigits(viewcd)) {
                                continue;
                            }
                            final int vn = Integer.parseInt(viewcd);
                            final String score = (String) viewcdScoreMap.get(viewcd);
                            svf.VrsOutn("UNIT_VIEW_DIV" + String.valueOf(vn % 10), evali + 1, score); // unit観点評定
                        }
                    }
                    svf.VrsOut("UNIT_SUBJECT" + (KNJ_EditEdit.getMS932ByteLength(unit._unitSubclassabbv) > 10 ? "2_1" : "1"), unit._unitSubclassabbv); // 科目名
                    svf.VrsOut("UNIT_NAME", "Unit " + String.valueOf(unii + 1)); // ユニット名
                    svf.VrsOut("UNIT_SEMESTER", (NumberUtils.isDigits(unit._year) ? _df02.format(Integer.parseInt(unit._year) % 100) : "") + StringUtils.defaultString(unit._semestername)); // 学期
                }
                if (_param._isPrintHyoukaFooter) {
                    printFooter(svf, student);
                }
                svf.VrEndRecord();
                _hasData = true;
            }

            endPage(svf, n);
        }

        private void printIbSubclass2(final Vrw32alp svf, final DB2UDB db2, final Student student, final IBSubclass ibsubclass) {
            final int n = 1;
            setForm(svf, form8, n);
            printHeader(svf, student);

            // 成績
            svf.VrsOut("ENG_SUBJECT_NAME1", ibsubclass._ibsubclassnameEng); // 英語科目名
            svf.VrsOut("TITLE", "評価の記録" + maru2 + "　ASSESSMENT RECORD " + maru2); // タイトル

            final Map subclasscdChairSubclassMap = ibsubclass.getChairSubclasscdMap(_param, student._chairsubclassList);

            printIBSubclassUnitSubclassname(svf, ibsubclass);

            final Map unitSubclassnameMap = ibsubclass.getUnitSubclassnameMap(_param);
//            final Map hyokaMap = ibsubclass.getHyokaMap(_param);

            int subline = 1;
            for (final Iterator it = unitSubclassnameMap.keySet().iterator(); it.hasNext();) {
                final String unitSubclasscd = (String) it.next();
                final String subclassname = (String) unitSubclassnameMap.get(unitSubclasscd);

                //log.debug(" unit subclass = " + unitSubclasscd + " : " + subclassname);
                svf.VrsOutn("SUBCLASS_NAME", subline, subclassname); // 科目名

                if (!"1".equals(_param._semester)) {
                    final Map dataMap = getJviewStatRecordDatMap(db2, _param, _param._year, "1".equals(_param._semester) ? "1" : "9", student._schregno, unitSubclasscd);

                    for (int cdi = 1; cdi <= 5; cdi++) {
                        final String cd = String.valueOf(cdi);
                        final Map cdMap = (Map) dataMap.get(cd);
                        if (null != cdMap) {
                            svf.VrsOutn("VAL_PERFECT" + cd, subline, getString(cdMap, "REMARK2")); // 評価満点
                            svf.VrsOutn("VAL_TOTAL" + cd, subline, getString(cdMap, "REMARK1")); // 評価合計
                            svf.VrsOutn("VAL_REACH" + cd, subline, percentageString(getString(cdMap, "REMARK3"))); // 到達度
                            svf.VrsOutn("VAL" + cd, subline, getString(cdMap, "STATUS")); // 評価
                        }
                    }

                    final Map cdMap = (Map) dataMap.get("0");
                    if (null != cdMap) {
                        String hyotei = getString(cdMap, "STATUS");
                        if (_param._d072SubclasscdList.contains(unitSubclasscd)) {
                            hyotei = (String) _param._d071hyoteiAlphabetMap.get(hyotei);
                        }
                        svf.VrsOutn("DIV_PERFECT", subline, getString(cdMap, "REMARK2")); // 評定満点
                        svf.VrsOutn("DIV_TOTAL", subline, getString(cdMap, "REMARK1")); // 評定満点
                        svf.VrsOutn("DIV_REACH", subline, percentageString(getString(cdMap, "REMARK3"))); // 到達度
                        svf.VrsOutn("DIV_VAL", subline, hyotei); // 評価評定

                        svf.VrsOutn("DIV", subline, hyotei); // 評定

                    }

                    if ("H".equals(student._schoolKind)) {
                        final ChairSubclass chairSubclass = (ChairSubclass) subclasscdChairSubclassMap.get(unitSubclasscd);
                        if (null != chairSubclass) {
                            svf.VrsOutn("CREDIT", subline, getString(chairSubclass._getCreditMap, _param._year)); // 評定
                        }
                    }
                }

                svf.VrsOutn("ABSENT", subline, (String) getMappedMap(getMappedMap(student._attendSubclassMap, _param._year), "KEKKA").get(unitSubclasscd)); // 欠課

//                final ChairSubclass chairSubclass = (ChairSubclass) subclasscdChairSubclassMap.get(unitSubclasscd);
//                if (null != chairSubclass) {
//                    svf.VrsOutn("DIV", subline, getString(chairSubclass._hyoteiMap, _param._year)); // 評定
//                }
                subline += 1;
            }
            if (_param._isPrintHyoukaFooter) {
                printFooter(svf, student);
            }
            endPage(svf, n);
            _hasData = true;
        }

        private void csvIbSubclass1(final List lines, final Student student, final IBSubclass ibsubclass) {

            List line = null;
            line = newLine(lines);
            line.add("");
            line.add(trimLeft(StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(_param._certifSchoolRemark3)));
            line = newLine(lines);
            line.add("");
            line.add("評価の記録" + maru1 + "　ASSESSMENT RECORD " + maru1); // タイトル

            line = newLine(lines);
            line.add("MYP Subjects");
            line.add("Year");
            line.add("Semester");
            line.add("Grade");
            line.add("MYP Year");
            line.add("対応する学習指導要領上の教科・科目");
            line = newLine(lines);
            line.add(ibsubclass._ibsubclassnameEng); // 英語科目名
            line.add(_param._year); // 年度
            line.add(_param._semesterName); // 学期
            line.add(NumberUtils.isDigits(_param._grade) ? String.valueOf(Integer.parseInt(_param._grade)) : ""); // 学年
            line.add(_param._gradeName3); // MYP学年
            csvIBSubclassUnitSubclassname(line, ibsubclass);

            line = newLine(lines);
            line.add("学籍番号/Student ID：");
            line.add(student._schregno); // 学籍番号
            line.add("");
            line.add("生徒氏名/Student Name:");
            line.add(student._name); // 氏名


            final int unitMax = 12;
            line = newLine(lines);
            line.add("Unit");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    line.add("Unit " + String.valueOf(unii + 1));
                } else {
                    line.add("");
                }
                for (int i = 1; i < 5; i++) {
                    line.add("");
                }
            }
            line.add("総合値");
            line.add("合計値");

            line = newLine(lines);
            line.add("学期");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                    line.add((NumberUtils.isDigits(unit._year) ? _df02.format(Integer.parseInt(unit._year) % 100) : "") + StringUtils.defaultString(unit._semestername)); // 学期
                } else {
                    line.add("");
                }
                for (int i = 1; i < 5; i++) {
                    line.add("");
                }
            }

            line = newLine(lines);
            line.add("対応する学習指導要領上の教科・科目");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                    line.add(StringUtils.defaultString(unit._unitSubclassabbv)); // 学期
                } else {
                    line.add("");
                }
                for (int i = 1; i < 5; i++) {
                    line.add("");
                }
            }

            line = newLine(lines);
            line.add("MYP assessment criteria＼教科・科目の観点");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                for (int i = 0; i < 5; i++) {
                    line.add(String.valueOf(i + 1));
                }
            }

            final String kantenNo = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String ibevalGradeSum = null;
            final int viewmax = 12;
            for (int evali = 0, evallen = Math.min(viewmax, ibsubclass._evalList.size()); evali < evallen; evali++) {
                final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);
                ibevalGradeSum = addNum(ibevalGradeSum, eval._ibevalGrade);
            }

            for (int evali = 0, evallen = Math.min(viewmax, ibsubclass._evalList.size()); evali < evallen; evali++) {
                final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);

                for (int k = 0; k < 2; k++) { // 2行ずつ出力
                    line = newLine(lines);
                    if (k == 0) {
                        line.add(evali < kantenNo.length() ? String.valueOf(kantenNo.charAt(evali)) : ""); // 観点番号
                        line.add(eval._ibevalNameEng); // 英語観点
                        line.add(eval._ibevalPerfect); // 観点評価

                        for (int unii = 0; unii < unitMax; unii++) {
                            if (unii < ibsubclass._unitList.size()) {
                                final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                                //log.debug(" unit ibseq = " + unit._ibseq);

                                final Map viewcdScoreMap = getMappedMap(eval._unitViewcdScoreMap, unit._ibseq);
                                String[] scores = new String[10];
                                for (final Iterator vit = viewcdScoreMap.keySet().iterator(); vit.hasNext();) {
                                    final String viewcd = (String) vit.next();
                                    if (!NumberUtils.isDigits(viewcd)) {
                                        continue;
                                    }
                                    final int vn = Integer.parseInt(viewcd);
                                    final String score = (String) viewcdScoreMap.get(viewcd);
                                    scores[vn % 10] = score;
                                }
                                for (int j = 0; j < 5; j++) {
                                    line.add(scores[j + 1]);
                                }
                            } else {
                                for (int j = 0; j < 5; j++) {
                                    line.add("");
                                }
                            }
                        }

                        line.add(eval._ibevalGrade); // 観点総合値
                        if (evali == 0) {
                            line.add(ibevalGradeSum);
                        }

                    } else if (k == 1) {
                        line.add("");
                        line.add(eval._ibevalName); // 観点
                        line.add(""); // 観点評価

                        for (int unii = 0; unii < unitMax; unii++) {
                            for (int j = 0; j < 5; j++) {
                                line.add("");
                            }
                        }

                        line.add(""); // 観点総合値
                    }
                }
            }

            line = newLine(lines);
            line.add("");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                for (int i = 0; i < 5; i++) {
                    line.add("");
                }
            }
            line.set(line.size() - 1, "MYP Grade");
            if (SEMEALL.equals(_param._semester)) {
                line.add(ibsubclass._ibGrade); // MYPグレード
            }
            newLine(lines); // 1行空け
        }

        private void csvIbSubclass2(final List lines, final DB2UDB db2, final Student student, final IBSubclass ibsubclass) {

            List line = null;
            line = newLine(lines);
            line.add("");
            line.add(trimLeft(StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(_param._certifSchoolRemark3)));
            line = newLine(lines);
            line.add("");
            line.add("評価の記録" + maru2 + "　ASSESSMENT RECORD " + maru2); // タイトル

            line = newLine(lines);
            line.add("MYP Subjects");
            line.add("Year");
            line.add("Semester");
            line.add("Grade");
            line.add("MYP Year");
            line.add("対応する学習指導要領上の教科・科目");
            line = newLine(lines);
            line.add(ibsubclass._ibsubclassnameEng); // 英語科目名
            line.add(_param._year); // 年度
            line.add(_param._semesterName); // 学期
            line.add(NumberUtils.isDigits(_param._grade) ? String.valueOf(Integer.parseInt(_param._grade)) : ""); // 学年
            line.add(_param._gradeName3); // MYP学年
            csvIBSubclassUnitSubclassname(line, ibsubclass);

            line = newLine(lines);
            line.add("学籍番号/Student ID：");
            line.add(student._schregno); // 学籍番号
            line.add("");
            line.add("生徒氏名/Student Name:");
            line.add(student._name); // 氏名


            final Map unitSubclassnameMap = ibsubclass.getUnitSubclassnameMap(_param);
//            final Map hyokaMap = ibsubclass.getHyokaMap(_param);

            final int max = 4;
            final List subclasscdSet = new ArrayList(unitSubclassnameMap.keySet());
            for (int i = 0; i < max; i++) {
                line = newLine(lines);
                line.add("学習指導要領上の教科・科目");
                line.add("");
                for (int cdi = 1; cdi <= 5; cdi++) {
                    final String cd = String.valueOf(cdi);
                    line.add("観点" + cd);
                }
                line.add("評定");
                line.add("欠課時数");
                line.add("評定");
                line.add("修得単位数");

                if (i < subclasscdSet.size()) {
                    final String unitSubclasscd = (String) subclasscdSet.get(i);
                    final String subclassname = (String) unitSubclassnameMap.get(unitSubclasscd);

                    final Map dataMap = getJviewStatRecordDatMap(db2, _param, _param._year, "1".equals(_param._semester) ? "1" : "9", student._schregno, unitSubclasscd);

                    //log.debug(" unit subclass = " + unitSubclasscd + " : " + subclassname);
                    for (int li = 0; li < 4; li++) {
                        line = newLine(lines);
                        if (li == 0) {
                            line.add(subclassname); // 科目名
                        } else {
                            line.add("");
                        }
                        if (li == 0) {
                            line.add("評価満点");
                        } else if (li == 1) {
                            line.add("評価合計");
                        } else if (li == 2) {
                            line.add("到達度");
                        } else if (li == 3) {
                            line.add("評価");
                        }
                        for (int cdi = 1; cdi <= 5; cdi++) {
                            final String cd = String.valueOf(cdi);
                            final Map cdMap = (Map) dataMap.get(cd);
                            if (null != cdMap) {
                                if (li == 0) {
                                    line.add(getString(cdMap, "REMARK2")); // 評価満点
                                } else if (li == 1) {
                                    line.add(getString(cdMap, "REMARK1")); // 評価合計
                                } else if (li == 2) {
                                    line.add(percentageString(getString(cdMap, "REMARK3"))); // 到達度
                                } else if (li == 3) {
                                    line.add(getString(cdMap, "STATUS")); // 評価
                                }
                            } else {
                                line.add("");
                            }
                        }

                        final Map cdMap = (Map) dataMap.get("0");
                        if (null != cdMap) {
                            String hyotei = getString(cdMap, "STATUS");
                            if (_param._d072SubclasscdList.contains(unitSubclasscd)) {
                                hyotei = (String) _param._d071hyoteiAlphabetMap.get(hyotei);
                            }
                            if (li == 0) {
                                line.add(getString(cdMap, "REMARK2")); // 評定満点
                                line.add((String) getMappedMap(getMappedMap(student._attendSubclassMap, _param._year), "KEKKA").get(unitSubclasscd)); // 欠課
                                line.add(hyotei); // 評定
                            } else if (li == 1) {
                                line.add(getString(cdMap, "REMARK1")); // 評定満点
                                line.add("");
                                line.add("");
                            } else if (li == 2) {
                                line.add(percentageString(getString(cdMap, "REMARK3"))); // 到達度
                                line.add("");
                                line.add("");
                            } else if (li == 3) {
                                line.add(hyotei); // 評価評定
                                line.add("");
                                line.add("");
                            }
                        }
                    }
                } else {
                    for (int li = 0; li < 4; li++) {
                        line = newLine(lines);
                        line.add("");
                        if (li == 0) {
                            line.add("評価満点");
                        } else if (li == 1) {
                            line.add("評価合計");
                        } else if (li == 2) {
                            line.add("到達度");
                        } else if (li == 3) {
                            line.add("評価");
                        }
                    }
                }
            }
            newLine(lines); // 1行空け
        }

        protected void printShomei(final Vrw32alp svf, final Student student) {
            final String form1 = "KNJD186J_9.frm";
            final String form2 = "KNJD186J_10.frm";
            printShomei(svf, form1, form2, student);
        }
    }

    private class FormDP extends FormCommon {

        final String form3 = "KNJD186K_3.frm";
        final String form4 = "KNJD186K_4.frm";
        final String form5 = "KNJD186K_5.frm";
        final String form6 = "KNJD186K_6.frm";

        FormDP() {
            printSchoolNameJpEngList.add(form3);
            printSchoolNameJpEngList.add(form4);
            printSchoolNameJpEngList.add(form5);
            printSchoolNameJpEngList.add(form6);
        }

        protected void printHyoshi(final Vrw32alp svf, final Student student) {
            final String form1 = "KNJD186K_1.frm";
            final String form2 = "KNJD186K_2.frm";
            printHyoshi(svf, form1, form2, null, null, student);
        }

        protected void printSeisekiHoukoku(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            printSeisekiHoukoku1(db2, svf, student);

            printSeisekiHoukoku2(svf, student);
        }

        private String[] getDispyears() {
            return _param._isDp2 ? new String[] { _param._yearBefore, _param._year} : new String[] { _param._year};
        }

        private void printSeisekiHoukoku1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            final int n = 4;
            setForm(svf, form3, n);
            printHeader(svf, student);

            svf.VrsOut("ANNUAL1", "５年次"); // 年次
            svf.VrsOut("ANNUAL2", "６年次"); // 年次
            svf.VrsOutn("NENDO", 1, "５年次"); // 年度年次
            svf.VrsOutn("NENDO", 2, "６年次"); // 年度年次
            svf.VrsOut("TITLE", "成績報告書　PROGRESS　REPORT");

            // 成績
            final String[] dispyears = getDispyears();

            int reccount = 0;
            int ibsubi = 0;
            String recp;
            recp = "1";
            List chairSubclassList = student.getChairSubclassList(_param);
            for (final Iterator csit = chairSubclassList.iterator(); csit.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) csit.next();
                if (_param._d026Name1List.contains(cs._subclasscd)) {
                    csit.remove();
                }
            }
            for (final Iterator it = student._ibsubclassList.iterator(); it.hasNext();) {
                ibsubi += 1;
                final String group = String.valueOf(ibsubi);
                final IBSubclass ibsubclass = (IBSubclass) it.next();

                final Map chairSubclasscdMap = ibsubclass.getChairSubclasscdMap(_param, chairSubclassList);

                final List chrSubclasslist = new ArrayList();
                for (final Iterator uit = ibsubclass.getUnitSubclassnameMap(_param).keySet().iterator(); uit.hasNext();) {
                    final String unitsubclasscd = (String) uit.next();
                    final ChairSubclass chrSubclass = (ChairSubclass) chairSubclasscdMap.get(unitsubclasscd);

                    if (null == chrSubclass) {
                        continue;
                    }
                    chrSubclasslist.add(chrSubclass);
                }

                final List ibclassnameEngList = retDividString(ibsubclass._ibclassnameEng, 30);
                final List ibsubclassnameEngList = retDividString(ibsubclass._ibsubclassnameEng, 24);

                final int printlinesize = Math.max(chrSubclasslist.size(), Math.max(ibclassnameEngList.size(), ibsubclassnameEngList.size()));

                for (int li = 0; li < printlinesize; li++) {

                    svf.VrsOut("ENG_SUBJECT_NAME1", li < ibclassnameEngList.size() ? (String) ibclassnameEngList.get(li) : ""); // 英語科目名
                    svf.VrsOut("ENG_COURSE_NAME1", li < ibsubclassnameEngList.size() ? (String) ibsubclassnameEngList.get(li) : ""); // 英語コース名
                    if (li == 0 && SEMEALL.equals(_param._semester)) {
                        svf.VrsOut("MYP_GRADE1", ibsubclass._ibGrade);
                    }

                    printAttendSemes(svf, student, dispyears);

                    if (li < chrSubclasslist.size()) {
                        final ChairSubclass chrSubclass = (ChairSubclass) chrSubclasslist.get(li);

                        svf.VrsOut("CLASS_NAME" + recp, chrSubclass._classname); // 教科名
                        svf.VrsOut("SUBJECT_NAME" + recp, chrSubclass._subclassname); // 科目名

                        for (int gi = 0; gi < dispyears.length; gi++) {
                            final String year = dispyears[gi];
                            final String f = String.valueOf(gi + 1);

                            svf.VrsOut("PEROID" + recp + "_" + f, (String) chrSubclass._takeSemesNameMap.get(year)); // 履修期間
                            svf.VrsOut("ATTEND_TIME" + recp + "_" + f, (String) getMappedMap(getMappedMap(student._attendSubclassMap, year), "KEKKA").get(chrSubclass._subclasscd)); // 欠課時数
                            String jviewStatRecordDatSemester;
                            if (year.equals(_param._yearBefore)) {
                                jviewStatRecordDatSemester = "9";
                            } else {
                                jviewStatRecordDatSemester = _param._semester;
                            }
                            svf.VrsOut("DIV" + recp + "_" + f, getHyotei(db2, _param, year, jviewStatRecordDatSemester, student._schregno, chrSubclass._subclasscd)); // 評定
                            svf.VrsOut("CREDIT" + recp + "_" + f, (String) chrSubclass._getCreditMap.get(year)); // 修得単位数
                        }
                    }

                    svf.VrsOut("GRP1", group); // グループ1
                    svf.VrsOut("GRP2", group); // グループ2
                    svf.VrsOut("GRP3", group); // グループ3
                    printFooter(svf, student);
                    svf.VrEndRecord();
                    reccount += 1;
                }
            }

            recp = "2";
            for (int i = 0, len = chairSubclassList.size(); i < len; i++) {
                final ChairSubclass chrSubclass = (ChairSubclass) chairSubclassList.get(i);
                if (chrSubclass._hasIbSubclass) {
                    continue;
                }

                svf.VrsOut("CLASS_NAME" + recp, chrSubclass._classname); // 教科名
                svf.VrsOut("SUBJECT_NAME" + recp, chrSubclass._subclassname); // 科目名

                for (int gi = 0; gi < dispyears.length; gi++) {
                    final String year = dispyears[gi];
                    final String f = String.valueOf(gi + 1);
                    svf.VrsOut("PEROID" + recp + "_" + f, (String) chrSubclass._takeSemesNameMap.get(year)); // 履修期間
                    svf.VrsOut("ATTEND_TIME" + recp + "_" + f, (String) getMappedMap(getMappedMap(student._attendSubclassMap, year), "KEKKA").get(chrSubclass._subclasscd)); // 欠課時数
                    svf.VrsOut("DIV" + recp + "_" + f, (String) chrSubclass._hyoteiMap.get(year)); // 評定
                    svf.VrsOut("CREDIT" + recp + "_" + f, (String) chrSubclass._getCreditMap.get(year)); // 修得単位数
                }
                printFooter(svf, student);
                svf.VrEndRecord();
                reccount += 1;
            }

            for (int i = reccount, max = 17; i < max; i++) {
                final String group = String.valueOf(i);
                svf.VrsOut("GRP1", group); // グループ1
                svf.VrsOut("GRP2", group); // グループ2
                svf.VrsOut("GRP3", group); // グループ3
                printFooter(svf, student);
                svf.VrEndRecord();
            }

            endPage(svf, n);
            _hasData = true;
        }

        private void printAttendSemes(final Vrw32alp svf, final Student student, final String[] dispyears) {
            for (int atti = 0; atti < dispyears.length; atti++) {
                final String year = dispyears[atti];
                final Attend attend = (Attend) student._attendSemesMap.get(year);
                if (null == attend) {
                    continue;
                }
                final int line = atti + 1;
                svf.VrsOutn("PERIOD", line, _param.attendRange(attend._sdate, attend._edate)); // 出欠期間
                svf.VrsOutn("SCHOOL_DAYS", line, attend._lesson); // 授業日数
                svf.VrsOutn("SUSPEND", line, addNum(attend._suspend, attend._mourning)); // 出席停止等
                svf.VrsOutn("ABROAD", line, attend._transferDate); // 留学
                svf.VrsOutn("MUST", line, attend._mlesson); // 出席すべき日数
                svf.VrsOutn("ABSENCE", line, attend._sick); // 欠席日数
                svf.VrsOutn("ATTEND", line, attend._present); // 出席日数
                svf.VrsOutn("REMARK", line, null); // 備考
            }
        }

        private void printSeisekiHoukoku2(final Vrw32alp svf, final Student student) {
            final int n = 1;
            setForm(svf, form4, n);
            printHeader(svf, student);

            final String[] dispyears = getDispyears();
            for (int yi = 0; yi < dispyears.length; yi++) {
                svfVrsOutnKurikaeshi(svf, "ACTIVE" + String.valueOf(yi + 1), retDividString(getString(getMappedMap(student._hreportremarkDetailDatMap, dispyears[yi] + "90401"), "REMARK4"), 30 * 2));
            }
            printFooter(svf, student);
            endPage(svf, n);
            _hasData = true;
        }

        protected void printIbSubclass(final DB2UDB db2, final Vrw32alp svf, final Student student) {
            for (final Iterator iit = student._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();

                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);

                if (_param._isPrintHyouka1) {
                    printIbSubclass1(svf, student, ibsubclass);
                }
                if (_param._isPrintHyouka2) {
                    printIbSubclass2(svf, db2, student, ibsubclass);
                }
            }
        }

        private void printIbSubclass1(final Vrw32alp svf, final Student student, final IBSubclass ibsubclass) {
            final int n = 4;
            setForm(svf, form5, n);
            printHeader(svf, student);

            // 成績
            svf.VrsOut("ENG_SUBJECT_NAME1", ibsubclass._ibsubclassnameEng); // 英語科目名
            svf.VrsOut("TITLE", "評価の記録" + maru1 + "　ASSESSMENT RECORD " + maru1); // タイトル

            printIBSubclassUnitSubclassname(svf, ibsubclass);

            if (SEMEALL.equals(_param._semester)) {
                svf.VrsOut("MYP_GRADE", ibsubclass._ibGrade); // MYPグレード
            }

            for (int unii = 0, unilen = ibsubclass._unitList.size(); unii < unilen; unii++) {
                final String un = String.valueOf(unii + 1);
                final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                //log.debug(" unit ibseq = " + unit._ibseq);

                svf.VrsOut("TASK_SUBJECT" + un + "_" + (KNJ_EditEdit.getMS932ByteLength(unit._unitSubclassabbv) > 10 ? "2" : "1"), unit._unitSubclassabbv); // 科目名
                svf.VrsOut("TASK_NAME" + un, "Task " + un); // ユニット名
                svf.VrsOut("TASK_SEMESTER" + un, (NumberUtils.isDigits(unit._year) ? _df02.format(Integer.parseInt(unit._year) % 100) : "") + StringUtils.defaultString(unit._semestername)); // 学期
            }

            final String viewcd = "0000"; // DPはviewcdは"0000"
            final List evalMapList = getEvalMapList(ibsubclass);
            String lastGrp1 = null;
            String lastGrp2 = null;
            for (int vi = 0, max = 24; vi < max; vi++) {
                String grp1 = String.valueOf(vi);
                String evaldiv1Name1 = null;
                String evaldiv1Name2 = null;
                String evaldiv1Name3 = null;
                String grp2 = String.valueOf(vi);
                String evaldiv2Name = null;
                String sumIbevalGrade = null;
                if (vi < evalMapList.size()) {
                    final Map evalMap = (Map) evalMapList.get(vi);

                    if (null != evalMap.get("EVAL")) {
                        final IBSubclass.Eval eval = (IBSubclass.Eval) evalMap.get("EVAL");
                        svf.VrsOut("CRITERION", eval._ibevalNameEng); // 基準
                        svf.VrsOut("VIEW_DIV", eval._ibevalPerfect); // 観点評価

                        for (int unii = 0, unilen = ibsubclass._unitList.size(); unii < unilen; unii++) {
                            final String un = String.valueOf(unii + 1);
                            final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);

                            svf.VrsOut("VIEW" + un, getString(getMappedMap(eval._unitViewcdScoreMap, unit._ibseq), viewcd)); // 観点評価
                        }

                        svf.VrsOut("SUB_TOTAL_VIEW", eval._ibevalGrade); // 観点総合値

                        lastGrp1 = grp1;
                        lastGrp2 = grp2;
                    } else {
                        grp1 = lastGrp1;
                        grp2 = lastGrp2;
                    }
                    grp1 = getString(evalMap, "GRP1");
                    evaldiv1Name1 = getString(evalMap, "IBEVAL_DIV1_NAME1");
                    evaldiv1Name2 = getString(evalMap, "IBEVAL_DIV1_NAME2");
                    evaldiv1Name3 = getString(evalMap, "IBEVAL_DIV1_NAME3");
                    grp2 = getString(evalMap, "GRP2");
                    evaldiv2Name = getString(evalMap, "IBEVAL_DIV2_NAME");
                    sumIbevalGrade = getString(evalMap, "SUM_IBEVAL_GRADE"); // IBEVAL_DIV1, IBEVAL_DIV2ごとのibevalGradeの合計値
                }

                svf.VrsOut("GRP1", grp1); // グループ1
                svf.VrsOut("VIEW_DIV1", evaldiv1Name1); // 評定区分
                svf.VrsOut("VIEW_DIV2", evaldiv1Name2); // 評定区分
                svf.VrsOut("VIEW_DIV3", evaldiv1Name3); // 評定区分
                svf.VrsOut("GRP2", grp2); // グループ2
                svf.VrsOut("ENG_VIEW", evaldiv2Name); // 英語観点
                svf.VrsOut("GRP3", grp2); // グループ3
                svf.VrsOut("TOTAL_VIEW", sumIbevalGrade); // 観点合計値
                if (_param._isPrintHyoukaFooter) {
                    printFooter(svf, student);
                }

                svf.VrEndRecord();
                _hasData = true;
            }

            endPage(svf, n);
        }

        private Map getEvaldivMap(final List list, final String key, final String value) {
            Map rtn = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                if (value.equals(m.get(key))) {
                    rtn = m;
                    break;
                }
            }
            return rtn;
        }

        private List getEvalMapList(final IBSubclass ibsubclass) {
            final String IBEVAL_DIV1 = "IBEVAL_DIV1";
            final String IBEVAL_DIV2 = "IBEVAL_DIV2";

            final List groupedEvalMapList = new ArrayList();
            for (final Iterator it = ibsubclass._evalList.iterator(); it.hasNext();) {
                final IBSubclass.Eval eval = (IBSubclass.Eval) it.next();

                if (null == getEvaldivMap(groupedEvalMapList, IBEVAL_DIV1, eval._ibevalDiv1)) {
                    final Map div1Map = new HashMap();
                    div1Map.put(IBEVAL_DIV1, eval._ibevalDiv1);
                    div1Map.put("LIST", new ArrayList());
                    div1Map.put("NAME", eval._ibevalDiv1Name);
                    groupedEvalMapList.add(div1Map);
                }
                final List div1List = (List) ((Map) getEvaldivMap(groupedEvalMapList, IBEVAL_DIV1, eval._ibevalDiv1)).get("LIST");

                if (null == getEvaldivMap(div1List, IBEVAL_DIV2, eval._ibevalDiv2)) {
                    final Map div2Map = new HashMap();
                    div2Map.put(IBEVAL_DIV2, eval._ibevalDiv2);
                    div2Map.put("LIST", new ArrayList());
                    div2Map.put("NAME", eval._ibevalDiv2Name);
                    div1List.add(div2Map);
                }
                final List div2List = (List) ((Map) getEvaldivMap(div1List, IBEVAL_DIV2, eval._ibevalDiv2)).get("LIST");

                final Map evalMap = new HashMap();
                evalMap.put("EVAL", eval);
                div2List.add(evalMap);
            }

            final int evaldiv2keta = 18;
            final List allEvalMapList = new ArrayList();

            for (final Iterator it = groupedEvalMapList.iterator(); it.hasNext();) {
                final Map div1Map = (Map) it.next();
                final String div1Name = (String) div1Map.get("NAME");
                final List div2List = (List) div1Map.get("LIST");

                final List div1AllEvalMapList = new ArrayList();
                for (final Iterator d1it = div2List.iterator(); d1it.hasNext();) {
                    final Map div2Map = (Map) d1it.next();
                    final String div2Name = (String) div2Map.get("NAME");
                    final List evalMapList = (List) div2Map.get("LIST");

                    String sumIbevalGrade = null;
                    for (Iterator eit = evalMapList.iterator(); eit.hasNext();) {
                        final Map evalMap = (Map) eit.next();
                        final IBSubclass.Eval eval = (IBSubclass.Eval) evalMap.get("EVAL");
                        sumIbevalGrade = addNum(sumIbevalGrade, eval._ibevalGrade);
                    }

                    final List div2NamePrintList = retDividString(div2Name, evaldiv2keta);
                    for (int i = 0, len = Math.max(evalMapList.size(), div2NamePrintList.size()); i < len; i++) {
                        if (i >= evalMapList.size()) {
                            evalMapList.add(new HashMap());
                        }
                        final Map evalMap = (Map) evalMapList.get(i);
                        evalMap.put("GRP2", getString(div1Map, IBEVAL_DIV1) + getString(div2Map, IBEVAL_DIV2));
                        if (i < div2NamePrintList.size()) {
                            evalMap.put("IBEVAL_DIV2_NAME", div2NamePrintList.get(i));
                        }
                    }

                    ((Map) evalMapList.get((evalMapList.size() - 1) / 2)).put("SUM_IBEVAL_GRADE", sumIbevalGrade); // 中央のみに表示

                    div1AllEvalMapList.addAll(evalMapList);
                }

                final LinkedList div1NamePrintList = retDividStringMojisu(reverse(StringUtils.isEmpty(div1Name) ? "" : div1Name + " "), 3); // 2文字ずつに分割
                if (div1NamePrintList.size() < div1AllEvalMapList.size()) {
                    // 区分1の名称が短ければセンタリング
                    final int spaceCount = (div1AllEvalMapList.size() - div1NamePrintList.size()) / 2;
                    for (int i = 0; i < spaceCount; i++) {
                        div1NamePrintList.add(0, "");
                    }
                }
                for (int i = 0, len = Math.max(div1AllEvalMapList.size(), div1NamePrintList.size()); i < len; i++) {
                    if (i >= div1AllEvalMapList.size()) {
                        div1AllEvalMapList.add(new HashMap());
                    }
                    final Map evalMap = (Map) div1AllEvalMapList.get(i);
                    evalMap.put("GRP1", div1Map.get(IBEVAL_DIV1));
                    if (i < div1NamePrintList.size()) {
                        final String ibevalDiv1Name = (String) div1NamePrintList.get(i);
                        // // 下から表示
                        if (ibevalDiv1Name.length() == 3) {
                            evalMap.put("IBEVAL_DIV1_NAME1", ibevalDiv1Name.substring(0, 1));
                            evalMap.put("IBEVAL_DIV1_NAME2", ibevalDiv1Name.substring(1, 2));
                            evalMap.put("IBEVAL_DIV1_NAME3", ibevalDiv1Name.substring(2));
                        } else if (ibevalDiv1Name.length() == 2) {
                            evalMap.put("IBEVAL_DIV1_NAME1", ibevalDiv1Name.substring(0, 1));
                            evalMap.put("IBEVAL_DIV1_NAME2", ibevalDiv1Name.substring(1));
                        } else {
                            evalMap.put("IBEVAL_DIV1_NAME1", ibevalDiv1Name);
                        }
                    }
                }
                allEvalMapList.addAll(div1AllEvalMapList);
            }

            return allEvalMapList;
        }

        private String reverse(final String s) {
            if (null == s) return "";
            final StringBuffer stb = new StringBuffer();
            for (int i = s.length() - 1; i >= 0; i--) {
                stb.append(s.charAt(i));
            }
            return stb.toString();
        }

        private void printIbSubclass2(final Vrw32alp svf, final DB2UDB db2, final Student student, final IBSubclass ibsubclass) {
            final int n = 1;
            setForm(svf, form6, n);
            printHeader(svf, student);
            // 成績
            svf.VrsOut("ENG_SUBJECT_NAME1", ibsubclass._ibsubclassnameEng); // 英語科目名
            svf.VrsOut("TITLE", "評価の記録" + maru2 + "　ASSESSMENT RECORD " + maru2); // タイトル
            for (int i = 0; i < 4; i++) {
                svf.VrsOutn("ANNUAL1", i + 1, "５年次");
                svf.VrsOutn("ANNUAL2", i + 1, "６年次");
            }

            final Map subclasscdChairSubclassMap = ibsubclass.getChairSubclasscdMap(_param, student.getChairSubclassList(_param));

            printIBSubclassUnitSubclassname(svf, ibsubclass);

            final Map unitSubclassnameMap = ibsubclass.getUnitSubclassnameMap(_param);
//            final Map hyokaMap = ibsubclass.getHyokaMap(_param);

            int subline = 1;
            for (final Iterator it = unitSubclassnameMap.keySet().iterator(); it.hasNext();) {
                final String unitSubclasscd = (String) it.next();
                final String subclassname = (String) unitSubclassnameMap.get(unitSubclasscd);

                log.debug(" unit subclass = " + unitSubclasscd + " : " + subclassname);
                svf.VrsOutn("SUBCLASS_NAME", subline, subclassname); // 科目名

                String totalCreditSum = null;
                final String[] dispyears = getDispyears();
                for (int i = 0; i < dispyears.length; i++) {
                    final String year = dispyears[i];
                    final String p = String.valueOf(i + 1);

                    svf.VrsOutn("ABSENT" + p, subline, (String) getMappedMap(getMappedMap(student._attendSubclassMap, year), "KEKKA").get(unitSubclasscd)); // 欠課
//                    if (null != hyokaMap.get(unitSubclasscd) && null != ((Map) hyokaMap.get(unitSubclasscd)).get(year)) {
//                        final Map dataMap =  (Map) ((Map) hyokaMap.get(unitSubclasscd)).get(year);
//
//                        final Map cdMap = (Map) dataMap.get("HYOTEI");
//                        if (null != cdMap) {
//                            svf.VrsOutn("VAL_PERFECT" + p, subline, getString(cdMap, "PERFECT")); // 評定満点
//                            svf.VrsOutn("VAL_TOTAL" + p, subline, getString(cdMap, "SUM")); // 評定満点
//                            svf.VrsOutn("VAL_REACH" + p, subline, getString(cdMap, "PERCENTAGE")); // 到達度
//                        }
//                    }

                    String jviewStatRecordDatSemester;
                    if (year.equals(_param._yearBefore)) {
                        jviewStatRecordDatSemester = "9";
                    } else {
                        jviewStatRecordDatSemester = _param._semester;
                    }
                    final Map dataMap = getJviewStatRecordDatMap(db2, _param, year, jviewStatRecordDatSemester, student._schregno, unitSubclasscd);
                    final Map cdMap = (Map) dataMap.get("0");
                    if (null != cdMap) {
                        svf.VrsOutn("VAL_PERFECT" + p, subline, getString(cdMap, "REMARK2")); // 評定満点
                        svf.VrsOutn("VAL_TOTAL" + p, subline, getString(cdMap, "REMARK1")); // 評定満点
                        svf.VrsOutn("VAL_REACH" + p, subline, percentageString(getString(cdMap, "REMARK3"))); // 到達度
                    }
                    final ChairSubclass chairSubclass = (ChairSubclass) subclasscdChairSubclassMap.get(unitSubclasscd);
                    if (null != chairSubclass) {
                        final String getCredit = getString(chairSubclass._getCreditMap, year);
                        totalCreditSum = addNum(totalCreditSum, getCredit);
                        svf.VrsOutn("CREDIT" + p, subline, getCredit); // 欠課
                        svf.VrsOutn("DIV" + p, subline, getHyotei(db2, _param, year, jviewStatRecordDatSemester, student._schregno, chairSubclass._subclasscd)); // 評定
                    }
                }
                svf.VrsOutn("TOTAL_CREDIT", subline, totalCreditSum); // 欠課

                subline += 1;
            }
            if (_param._isPrintHyoukaFooter) {
                printFooter(svf, student);
            }
            endPage(svf, n);
            _hasData = true;
        }

        protected void csvIbSubclass(final DB2UDB db2, final List lines, final Student student) {
            for (final Iterator iit = student._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();

                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);
                if (_param._isPrintHyouka1) {
                    csvIbSubclass1(lines, student, ibsubclass);
                }
                if (_param._isPrintHyouka2) {
                    csvIbSubclass2(lines, db2, student, ibsubclass);
                }
            }
        }

        private void csvIbSubclass1(final List lines, final Student student, final IBSubclass ibsubclass) {
            List line = null;
            line = newLine(lines);
            line.add("");
            line.add(trimLeft(StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(_param._certifSchoolRemark3)));
            line = newLine(lines);
            line.add("");
            line.add("評価の記録" + maru1 + "　ASSESSMENT RECORD " + maru1); // タイトル

            line = newLine(lines);
            line.add("DP Subjects/Course");
            line.add("Year");
            line.add("Semester");
            line.add("Grade");
            line.add("DP Year");
            line.add("対応する学習指導要領上の教科・科目");
            line = newLine(lines);
            line.add(ibsubclass._ibsubclassnameEng); // 英語科目名
            line.add(_param._year); // 年度
            line.add(_param._semesterName); // 学期
            line.add(NumberUtils.isDigits(_param._grade) ? String.valueOf(Integer.parseInt(_param._grade)) : ""); // 学年
            line.add(_param._gradeName3); // MYP学年
            csvIBSubclassUnitSubclassname(line, ibsubclass);

            line = newLine(lines);
            line.add("学籍番号/Student ID：");
            line.add(student._schregno); // 学籍番号
            line.add("");
            line.add("生徒氏名/Student Name:");
            line.add(student._name); // 氏名


            final int unitMax = 12;
            line = newLine(lines);
            line.add("Task");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    line.add("Task " + String.valueOf(unii + 1));
                } else {
                    line.add("");
                }
            }
            line.add("総合値");
            line.add("合計値");

            line = newLine(lines);
            line.add("学期");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                    line.add((NumberUtils.isDigits(unit._year) ? _df02.format(Integer.parseInt(unit._year) % 100) : "") + StringUtils.defaultString(unit._semestername)); // 学期
                } else {
                    line.add("");
                }
            }

            line = newLine(lines);
            line.add("対応する学習指導要領上の教科・科目");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                if (unii < ibsubclass._unitList.size()) {
                    final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                    line.add(StringUtils.defaultString(unit._unitSubclassabbv)); // 学期
                } else {
                    line.add("");
                }
            }

            line = newLine(lines);
            line.add("DP assessment criteria");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                line.add("");
            }

            String ibevalGradeSum = null;
            final int viewmax = 12;
            for (int evali = 0, evallen = Math.min(viewmax, ibsubclass._evalList.size()); evali < evallen; evali++) {
                final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);
                ibevalGradeSum = addNum(ibevalGradeSum, eval._ibevalGrade);
            }

            final String viewcd = "0000"; // DPはviewcdは"0000"
            for (int evali = 0, evallen = Math.min(viewmax, ibsubclass._evalList.size()); evali < evallen; evali++) {
                final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);

                line = newLine(lines);

                line.add(eval._ibevalDiv1Name);
                line.add(eval._ibevalDiv2Name);
                line.add(eval._ibevalNameEng); // 英語観点
                line.add(eval._ibevalPerfect); // 観点評価

                for (int unii = 0; unii < unitMax; unii++) {
                    if (unii < ibsubclass._unitList.size()) {
                        final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                        //log.debug(" unit ibseq = " + unit._ibseq);

                        line.add(getString(getMappedMap(eval._unitViewcdScoreMap, unit._ibseq), viewcd)); // 観点評価
                    } else {
                        line.add("");
                    }
                }

                line.add(eval._ibevalGrade); // 観点総合値
                if (evali == 0) {
                    line.add(ibevalGradeSum);
                }
            }

            line = newLine(lines);
            line.add("");
            line.add("");
            line.add("");
            line.add("");
            for (int unii = 0; unii < unitMax; unii++) {
                line.add("");
            }
            line.set(line.size() - 1, "DP Grade");
            if (SEMEALL.equals(_param._semester)) {
                line.add(ibsubclass._ibGrade);
            }
            newLine(lines); // 1行空け
        }

        private void csvIbSubclass2(final List lines, final DB2UDB db2, final Student student, final IBSubclass ibsubclass) {
            List line = null;
            line = newLine(lines);
            line.add("");
            line.add(trimLeft(StringUtils.defaultString(_param._certifSchoolSchoolName) + "　" + StringUtils.defaultString(_param._certifSchoolRemark3)));
            line = newLine(lines);
            line.add("");
            line.add("評価の記録" + maru2 + "　ASSESSMENT RECORD " + maru2); // タイトル

            line = newLine(lines);
            line.add("DP Subjects/Course");
            line.add("Year");
            line.add("Semester");
            line.add("Grade");
            line.add("DP Year");
            line.add("対応する学習指導要領上の教科・科目");
            line = newLine(lines);
            line.add(ibsubclass._ibsubclassnameEng); // 英語科目名
            line.add(_param._year); // 年度
            line.add(_param._semesterName); // 学期
            line.add(NumberUtils.isDigits(_param._grade) ? String.valueOf(Integer.parseInt(_param._grade)) : ""); // 学年
            line.add(_param._gradeName3); // MYP学年
            csvIBSubclassUnitSubclassname(line, ibsubclass);

            line = newLine(lines);
            line.add("学籍番号/Student ID：");
            line.add(student._schregno); // 学籍番号
            line.add("");
            line.add("生徒氏名/Student Name:");
            line.add(student._name); // 氏名


            final Map subclasscdChairSubclassMap = ibsubclass.getChairSubclasscdMap(_param, student.getChairSubclassList(_param));

            final Map unitSubclassnameMap = ibsubclass.getUnitSubclassnameMap(_param);
//            final Map hyokaMap = ibsubclass.getHyokaMap(_param);

            final int max = 4;
            final List subclasscdSet = new ArrayList(unitSubclassnameMap.keySet());
            for (int i = 0; i < max; i++) {
                line = newLine(lines);
                line.add("学習指導要領上の教科・科目");
                line.add("項目");
                line.add("５年次");
                line.add("");
                line.add("");
                line.add("");
                line.add("６年次");
                line.add("");
                line.add("");
                line.add("");
                line.add("修得単位数合計");

                line = newLine(lines);
                line.add("");
                line.add("");
                line.add("欠課時数");
                line.add("評価");
                line.add("評定");
                line.add("修得単位数");
                line.add("欠課時数");
                line.add("評価");
                line.add("評定");
                line.add("修得単位数");
                line.add("");

                if (i < subclasscdSet.size()) {
                    final String unitSubclasscd = (String) subclasscdSet.get(i);
                    final String subclassname = (String) unitSubclassnameMap.get(unitSubclasscd);

                    //log.debug(" unit subclass = " + unitSubclasscd + " : " + subclassname);
                    for (int li = 0; li < 3; li++) {
                        line = newLine(lines);
                        if (li == 0) {
                            line.add(subclassname); // 科目名
                        } else {
                            line.add("");
                        }
                        if (li == 0) {
                            line.add("評価満点");
                        } else if (li == 1) {
                            line.add("評価合計");
                        } else if (li == 2) {
                            line.add("到達度");
                        }

                        String totalCreditSum = null;
                        final String[] dispyears = getDispyears();
                        for (int yi = 0; yi < 2; yi++) { // 5年次、6年次
                            if (yi < dispyears.length) {
                                final String year = dispyears[yi];

                                if (li == 0) {
                                    line.add(getMappedMap(getMappedMap(student._attendSubclassMap, year), "KEKKA").get(unitSubclasscd)); // 欠課
                                } else {
                                    line.add("");
                                }

                                String jviewStatRecordDatSemester;
                                if (year.equals(_param._yearBefore)) {
                                    jviewStatRecordDatSemester = "9";
                                } else {
                                    jviewStatRecordDatSemester = _param._semester;
                                }

                                final Map dataMap = getJviewStatRecordDatMap(db2, _param, year, jviewStatRecordDatSemester, student._schregno, unitSubclasscd);
                                final Map cdMap = (Map) dataMap.get("0");
                                if (null != cdMap) {
                                    if (li == 0) {
                                        line.add(getString(cdMap, "REMARK2")); // 評定満点
                                    } else if (li == 1) {
                                        line.add(getString(cdMap, "REMARK1")); // 評定満点
                                    } else if (li == 2) {
                                        log.debug(" totatsudo = " + getString(cdMap, "REMARK3"));
                                        line.add(percentageString(getString(cdMap, "REMARK3"))); // 到達度
                                    }
                                } else {
                                    line.add("");
                                }
                                if (li == 0) {
                                    final ChairSubclass chairSubclass = (ChairSubclass) subclasscdChairSubclassMap.get(unitSubclasscd);
                                    if (null != chairSubclass) {
                                        final String getCredit = getString(chairSubclass._getCreditMap, year);
                                        totalCreditSum = addNum(totalCreditSum, getCredit);
                                        line.add(getHyotei(db2, _param, year, jviewStatRecordDatSemester, student._schregno, chairSubclass._subclasscd)); // 評定
                                        line.add(getCredit);
                                    }
                                } else {
                                    line.add("");
                                    line.add("");
                                }
                            } else {
                                line.add("");
                                line.add("");
                                line.add("");
                                line.add("");
                            }
                        }
                        if (li == 0) {
                            line.add(totalCreditSum);
                        }
                    }
                } else {
                    for (int li = 0; li < 3; li++) {
                        line = newLine(lines);
                        line.add("");
                        if (li == 0) {
                            line.add("評価満点");
                        } else if (li == 1) {
                            line.add("評価合計");
                        } else if (li == 2) {
                            line.add("到達度");
                        }
                    }
                }
                newLine(lines); // 1行空け
            }
            newLine(lines); // 1行空け
            newLine(lines); // 1行空け
        }

        protected void printShomei(final Vrw32alp svf, final Student student) {
            final String form1 = "KNJD186K_7.frm";
            final String form2 = "KNJD186K_8.frm";
            printShomei(svf, form1, form2, student);
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _schoolKind;
        final String _hrClass;
        final String _attendno;
        final String _tr1Name;
        final String _tr2Name;
        final String _tr3Name;
        String _footerAttendno = null;

        private List _ibsubclassList = Collections.EMPTY_LIST;
        private List _chairsubclassList = Collections.EMPTY_LIST;

        private Map _ibclasscdPrgCourseGradePhaseMap = new HashMap();
        private Map _attendSemesMap = new HashMap();
        private Map _attendSubclassMap = new HashMap();
        private Map _hreportremarkDetailDatMap = null;

        public Student(
                final String schregno,
                final String name,
                final String schoolKind,
                final String hrClass,
                final String attendno,
                final String tr1Name,
                final String tr2Name,
                final String tr3Name) {
            _schregno = schregno;
            _name = name;
            _schoolKind = schoolKind;
            _hrClass = hrClass;
            _attendno = attendno;
            _tr1Name = tr1Name;
            _tr2Name = tr2Name;
            _tr3Name = tr3Name;
        }

        public List getChairSubclassList(final Param param) {
            final List rtn = new ArrayList();
            final Set subclasscdSet = new HashSet();
            for (final Iterator it = _chairsubclassList.iterator(); it.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) it.next();
                if (getMappedMap(param._replaceCombined, cs._subclasscd).isEmpty()) {
                    rtn.add(cs);
                } else {
                    final String combinedSubclasscd = param.getCombinedSubclasscd(cs._subclasscd);
                    if (!subclasscdSet.contains(combinedSubclasscd)) {
                        subclasscdSet.add(combinedSubclasscd);
                        String timeUnit = null;
                        boolean hasIbSubclass = false;
                        final Map takeSemesMap = new HashMap();
                        final Map takeSemesNameMap = new HashMap();
                        final Map hyoteiMap = new HashMap();
                        final Map getCreditMap = new HashMap();
                        for (final Iterator its = _chairsubclassList.iterator(); its.hasNext();) {
                            final ChairSubclass ics = (ChairSubclass) its.next();
                            if (combinedSubclasscd.equals(param.getCombinedSubclasscd(ics._subclasscd))) {
                                timeUnit = addNum(timeUnit, ics._timeUnit);
                                if (cs._hasIbSubclass) {
                                    hasIbSubclass = true;
                                }
                                takeSemesMap.putAll(ics._takeSemesMap);
                                takeSemesNameMap.putAll(ics._takeSemesNameMap);
                                for (final Iterator mit = ics._hyoteiMap.keySet().iterator(); mit.hasNext();) {
                                    final String year = (String) mit.next();
                                    hyoteiMap.put(year, addNum(hyoteiMap.get(year), ics._hyoteiMap.get(year)));
                                }
                                for (final Iterator mit = ics._getCreditMap.keySet().iterator(); mit.hasNext();) {
                                    final String year = (String) mit.next();
                                    getCreditMap.put(year, addNum(getCreditMap.get(year), ics._getCreditMap.get(year)));
                                }
                            }
                        }
                        final String combinedClassName = param.getCombinedClassName(cs._subclasscd);
                        final String combinedSubclassName = param.getCombinedSubclassName(cs._subclasscd);
                        final String combinedSubclassAbbv = param.getCombinedSubclassAbbv(cs._subclasscd);
                        final ChairSubclass combined = new ChairSubclass(combinedSubclasscd, combinedClassName, combinedSubclassName, combinedSubclassAbbv, timeUnit);
                        combined._takeSemesMap.putAll(takeSemesMap);
                        combined._takeSemesNameMap.putAll(takeSemesNameMap);
                        combined._hyoteiMap.putAll(hyoteiMap);
                        combined._getCreditMap.putAll(getCreditMap);
                        combined._hasIbSubclass = hasIbSubclass;
                        rtn.add(combined);
                    }
                }
            }
            return rtn;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("            , SEMESTER_MST T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._regdSemester +"' ");
            stb.append("        AND T1.YEAR = T2.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            //stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("    ) ");
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, ");
            stb.append("        T1.HR_CLASS, ");
            stb.append("        GDAT.SCHOOL_KIND, ");
            stb.append("        T7.HR_NAME, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T5.NAME, ");
            stb.append("        T5.REAL_NAME, ");
            stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("        T8.STAFFNAME AS TR1_NAME, ");
            stb.append("        T9.STAFFNAME AS TR2_NAME, ");
            stb.append("        T10.STAFFNAME AS TR3_NAME ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + param._year + "' AND GDAT.GRADE = T1.GRADE ");
            stb.append("        LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T7.TR_CD1 ");
            stb.append("        LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T7.TR_CD2 ");
            stb.append("        LEFT JOIN STAFF_MST T10 ON T10.STAFFCD = T7.TR_CD3 ");
            stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO");
            return stb.toString();
        }
    }

    /**
     * 出欠の記録
     */
    private static class Attend {

        String _semester;
        String _lesson;
        String _suspend;
        String _mourning;
        String _mlesson;
        String _sick;
        String _absent;
        String _present;
        String _late;
        String _early;
        String _transferDate;
        String _offdays;
        String _kekkaJisu;
        String _virus;
        String _koudome;

        String _sdate;
        String _edate;

        public static Attend getAttendSemesDat(final DB2UDB db2, final Param param, final String schregno, final PreparedStatement ps) {
            ResultSet rs = null;
            final Attend attend = new Attend();
            try {
                ps.setString(1, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    if (!"9".equals(semester)) {
                        continue;
                    }
                    attend._semester = semester;
                    attend._lesson = rs.getString("LESSON");
                    attend._suspend = rs.getString("SUSPEND");
                    attend._mourning = rs.getString("MOURNING");
                    attend._mlesson = rs.getString("MLESSON");
                    attend._sick = rs.getString("SICK");
                    attend._absent = rs.getString("ABSENT");
                    attend._present = rs.getString("PRESENT");
                    attend._late = rs.getString("LATE");
                    attend._early = rs.getString("EARLY");
                    attend._transferDate = rs.getString("TRANSFER_DATE");
                    attend._offdays = rs.getString("OFFDAYS");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return attend;
        }


        public static Map getAttendSubclassDatMap(final DB2UDB db2, final Param param, final String schregno, final PreparedStatement ps) {
            ResultSet rs = null;
            final Map rtn = new HashMap();
            rtn.put("KEKKA", new HashMap());
            //rtn.put("LESSON", new HashMap());
            try {
                ps.setString(1, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    if (!"9".equals(semester)) {
                        continue;
                    }
                    if ("1".equals(rs.getString("IS_COMBINED_SUBCLASS"))) {
                        getMappedMap(rtn, "KEKKA").put(rs.getString("SUBCLASSCD"), rs.getString("REPLACED_SICK"));
                    } else {
                        getMappedMap(rtn, "KEKKA").put(rs.getString("SUBCLASSCD"), rs.getString("SICK2"));
                    }
                    getMappedMap(rtn, "LESSON").put(rs.getString("SUBCLASSCD"), rs.getString("LESSON"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtn;
        }
    }

    private static class ChairSubclass {
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _subclassabbv;
        final String _timeUnit;
        boolean _hasIbSubclass = false;

        ChairSubclass(final String subclasscd,
                final String classname,
                final String subclassname,
                final String subclassabbv,
                final String timeUnit
                ) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _timeUnit = timeUnit;
        }
        final Map _takeSemesMap = new HashMap();
        final Map _takeSemesNameMap = new HashMap();
        final Map _hyoteiMap = new HashMap();
        final Map _getCreditMap = new HashMap();

        private static ChairSubclass getChairSubclass(final List chairsubclasslist, final String subclasscd) {
            for (final Iterator it = chairsubclasslist.iterator(); it.hasNext();) {
                final ChairSubclass e = (ChairSubclass) it.next();
                if (e._subclasscd.equals(subclasscd)) {
                    return e;
                }
            }
            return null;
        }
    }

    private static class IBSubclass {
        final String _ibclasscdPrgCourse;
        final String _ibSubclasscd;
        final String _ibclassnameEng;
        final String _ibsubclassnameEng;
        final String _ibGrade;

        final List _unitList = new ArrayList();
        final List _evalList = new ArrayList();

        IBSubclass(
            final String ibclasscdPrgCourse,
            final String ibSubclasscd,
            final String ibclassnameEng,
            final String ibsubclassnameEng,
            final String ibGrade
        ) {
            _ibclasscdPrgCourse = ibclasscdPrgCourse;
            _ibSubclasscd = ibSubclasscd;
            _ibclassnameEng = ibclassnameEng;
            _ibsubclassnameEng = ibsubclassnameEng;
            _ibGrade = ibGrade;
        }

        public Map getChairSubclasscdMap(final Param param, final List chairSubclassList) {
            final Map rtn = new TreeMap();
            for (final Iterator it = getUnitSubclassnameMap(param).keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final ChairSubclass chrSubclass = ChairSubclass.getChairSubclass(chairSubclassList, subclasscd);
                if (null != chrSubclass) {
                    rtn.put(subclasscd, chrSubclass);
                }
            }
            return rtn;
        }

        public Map getUnitSubclassnameMap(final Param param) {
            final Map rtn = new HashMap();
            final Set subclasscdSet = new HashSet();
            for (final Iterator uit = _unitList.iterator(); uit.hasNext();) {
                final Unit unit = (Unit) uit.next();

                if (getMappedMap(param._replaceCombined, unit._unitSubclasscd).isEmpty()) {
                    if (null != unit._unitSubclasscd) {
                        rtn.put(unit._unitSubclasscd, unit._unitSubclassname);
                    }
                } else {
                    final String combinedSubclasscd = param.getCombinedSubclasscd(unit._unitSubclasscd);
                    if (!subclasscdSet.contains(combinedSubclasscd)) {
                        subclasscdSet.add(combinedSubclasscd);
                        final String combinedSubclassName = param.getCombinedSubclassName(unit._unitSubclasscd);
                        rtn.put(combinedSubclasscd, combinedSubclassName);
                    }
                }
            }
            return rtn;
        }

//        public Map getHyokaMap(final Param param) {
//            final Map rtn = new TreeMap();
//            for (final Iterator uit = _unitList.iterator(); uit.hasNext();) {
//                final Unit unit = (Unit) uit.next();
//                if (null == unit._unitSubclasscd) {
//                    continue;
//                }
//                final Map dataMap =  getMappedMap(getMappedMap(rtn, unit._unitSubclasscd), unit._year);
//
//                for (final Iterator eit = _evalList.iterator(); eit.hasNext();) {
//                    final Eval eval = (Eval) eit.next();
//
//                    final Map viewcdScoreMap = (Map) eval._unitViewcdScoreMap.get(unit._ibseq);
//                    if (null == viewcdScoreMap) {
//                        continue;
//                    }
//
//                    for (final Iterator sit = viewcdScoreMap.entrySet().iterator(); sit.hasNext();) {
//                        final Map.Entry e = (Map.Entry) sit.next();
//                        final String viewcd = (String) e.getKey();
//                        final String score = (String) e.getValue();
//
//                        if (!NumberUtils.isDigits(viewcd)) {
//                            continue;
//                        }
//
//                        final String cd = String.valueOf(Integer.parseInt(viewcd) % 100);
//                        final Map cdMap = getMappedMap(dataMap, cd);
//
//                        final String newPerfect = addNum(cdMap.get("PERFECT"), eval._ibevalPerfect);
//                        final String newSum = addNum(cdMap.get("SUM"), score);
//                        final String percentage = percentage(newSum, newPerfect);
//                        cdMap.put("PERFECT", newPerfect);
//                        cdMap.put("SUM", newSum);
//                        cdMap.put("PERCENTAGE", percentage);
//                        //log.debug(" " + unit._unitSubclasscd + " cdMap " + cd + ", perfect = " + eval._ibevalPerfect + ", score = " + score + " = " + cdMap);
//                    }
//                }
//            }
//
//            for (final Iterator uit = rtn.keySet().iterator(); uit.hasNext();) {
//                final String unitSubclasscd = (String) uit.next();
//                final Map yearMap = (Map) rtn.get(unitSubclasscd);
//
//                for (final Iterator yit = yearMap.values().iterator(); yit.hasNext();) {
//                    final Map dataMap = (Map) yit.next();
//
//                    final Map hyoteiMap = new HashMap();
//                    for (final Iterator dit = dataMap.entrySet().iterator(); dit.hasNext();) {
//                        final Map.Entry e = (Map.Entry) dit.next();
//                        //final String cd = (String) e.getKey();
//                        final Map cdMap = (Map) e.getValue();
//
//                        final String newPerfect = addNum(cdMap.get("PERFECT"), hyoteiMap.get("PERFECT"));
//                        final String newSum = addNum(cdMap.get("SUM"), hyoteiMap.get("SUM"));
//                        final String percentage = percentage(newSum, newPerfect);
//                        hyoteiMap.put("PERFECT", newPerfect);
//                        hyoteiMap.put("SUM", newSum);
//                        hyoteiMap.put("PERCENTAGE", percentage);
//                    }
//                    dataMap.put("HYOTEI", hyoteiMap);
//                }
//            }
//            return rtn;
//        }

        private static String percentage(final String sum, final String perfect) {
            if (!NumberUtils.isNumber(sum) || !NumberUtils.isNumber(perfect) || 0.0 == Double.parseDouble(perfect)) {
                return null;
            }
            return new BigDecimal(sum).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public static void setIBSubclassList(final DB2UDB db2, final Param param, final Student student) {
            student._ibsubclassList = new ArrayList();
            student._chairsubclassList = new ArrayList();
            ResultSet rs = null;
            try {
                if (null == param._psMap.get("PS_IBSUB")) {
                    final String sql = getIBSubclassSql(param);
                    if (param._isOutputDebug) {
                        log.info(" ib subclass sql = " + sql);
                    }
                    param._psMap.put("PS_IBSUB", db2.prepareStatement(sql));
                }
                PreparedStatement ps = (PreparedStatement) param._psMap.get("PS_IBSUB");
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String subclasscd = rs.getString("CHR_SUBCLASSCD");

                    if (null == ChairSubclass.getChairSubclass(student._chairsubclassList, subclasscd)) {
                        final String classname = rs.getString("CHR_CLASSNAME");
                        final String subclassname = rs.getString("CHR_SUBCLASSNAME");
                        final String subclassabbv = rs.getString("CHR_SUBCLASSABBV");
                        final String timeUnit = NumberUtils.isNumber(rs.getString("TIME_UNIT")) ? new BigDecimal(rs.getString("TIME_UNIT")).setScale(0, BigDecimal.ROUND_HALF_UP).toString() : null;

                        student._chairsubclassList.add(new ChairSubclass(subclasscd, classname, subclassname, subclassabbv, timeUnit));
                    }

                    final ChairSubclass chairSubclass = ChairSubclass.getChairSubclass(student._chairsubclassList, subclasscd);

                    final String hyoteiYear = rs.getString("HYOTEI_YEAR");
                    chairSubclass._takeSemesMap.put(hyoteiYear, rs.getString("CHAIR_TAKESEMES"));
                    chairSubclass._takeSemesNameMap.put(hyoteiYear, "0".equals(rs.getString("CHAIR_TAKESEMES")) ? "通年" : rs.getString("CHAIR_TAKESEMES_NAME"));
                    chairSubclass._hyoteiMap.put(hyoteiYear, rs.getString("HYOTEI"));
                    chairSubclass._getCreditMap.put(hyoteiYear, rs.getString("GET_CREDIT"));

                    final String ibclasscdPrgCourse = rs.getString("IBCLASSCD_PRG_COURSE");
                    final String ibSubclasscd = rs.getString("IB_SUBCLASSCD");

                    if (null != ibSubclasscd) {
                        chairSubclass._hasIbSubclass = true;

                        final String ibseq = rs.getString("IBSEQ");
                        if (null == ibseq) {
                            log.info(" ibseq null : ibSubclasscd = " + ibSubclasscd);
                            continue;
                        }

                        if (null == getIBSubclass(student._ibsubclassList, ibSubclasscd)) {
                            final String ibclassnameEng = rs.getString("IBCLASSNAME_ENG");
                            final String ibsubclassnameEng = rs.getString("IBSUBCLASSNAME_ENG");
                            final String ibGrade = rs.getString("IB_GRADE");
                            student._ibsubclassList.add(new IBSubclass(ibclasscdPrgCourse, ibSubclasscd, ibclassnameEng, ibsubclassnameEng, ibGrade));
                        }
                        final IBSubclass ibsubclass = getIBSubclass(student._ibsubclassList, ibSubclasscd);

                        if (null == getUnit(ibsubclass._unitList, ibseq)) {
                            final String unitYear = rs.getString("UNIT_YEAR");
                            final String unitSemester = rs.getString("UNIT_SEMESTER");
                            final String unitSemestername = rs.getString("UNIT_SEMESTERNAME");
                            final String unitSubclasscd = rs.getString("UNIT_SUBCLASSCD");
                            final String unitSubclassname = rs.getString("UNIT_SUBCLASSNAME");
                            final String unitSubclassabbv = rs.getString("UNIT_SUBCLASSABBV");

                            ibsubclass._unitList.add(new Unit(ibseq, unitYear, unitSemester, unitSemestername, unitSubclasscd, unitSubclassname, unitSubclassabbv));
                        }

                        final String ibevalDiv1 = rs.getString("IBEVAL_DIV1");
                        final String ibevalDiv2 = rs.getString("IBEVAL_DIV2");
                        final String ibevalMark = rs.getString("IBEVAL_MARK");

                        if (null != ibevalDiv1 && null != ibevalDiv2 && null != ibevalMark) {
                            final Unit unit = getUnit(ibsubclass._unitList, ibseq);

                            if (null == getEval(ibsubclass._evalList, ibevalDiv1, ibevalDiv2, ibevalMark)) {
                                final String ibevalDiv1Name = rs.getString("IBEVAL_DIV1_NAME");
                                final String ibevalDiv2Name = rs.getString("IBEVAL_DIV2_NAME");
                                final String ibevalName = rs.getString("IBEVAL_NAME");
                                final String ibevalNameEng = rs.getString("IBEVAL_NAME_ENG");
                                final String ibevalPerfect = rs.getString("IBEVAL_PERFECT");
                                final String ibevalGrade = rs.getString("IBEVAL_GRADE");
                                ibsubclass._evalList.add(new Eval(ibevalDiv1, ibevalDiv1Name, ibevalDiv2, ibevalDiv2Name, ibevalMark, ibevalName, ibevalNameEng, ibevalPerfect, ibevalGrade));
                            }

                            final Eval eval = getEval(ibsubclass._evalList, ibevalDiv1, ibevalDiv2, ibevalMark);
                            final String viewcd = rs.getString("VIEWCD");
                            final String score = rs.getString("SCORE");

                            getMappedMap(eval._unitViewcdScoreMap, unit._ibseq).put(viewcd, score);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private static IBSubclass getIBSubclass(final List ibsubclasslist, final String ibsubclasscd) {
            for (final Iterator it = ibsubclasslist.iterator(); it.hasNext();) {
                final IBSubclass e = (IBSubclass) it.next();
                if (e._ibSubclasscd.equals(ibsubclasscd)) {
                    return e;
                }
            }
            return null;
        }

        private static class Unit {

            final String _ibseq;
            final String _year;
            final String _semester;
            final String _semestername;
            final String _unitSubclasscd;
            final String _unitSubclassname;
            final String _unitSubclassabbv;

            Unit(
                final String ibseq,
                final String year,
                final String semester,
                final String semestername,
                final String unitSubclasscd,
                final String unitSubclassname,
                final String unitSubclassabbv
            ) {
                _ibseq = ibseq;
                _year = year;
                _semester = semester;
                _semestername = semestername;
                _unitSubclasscd = unitSubclasscd;
                _unitSubclassname = unitSubclassname;
                _unitSubclassabbv = unitSubclassabbv;
            }
        }

        private static Unit getUnit(final List unitlist, final String ibseq) {
            for (final Iterator it = unitlist.iterator(); it.hasNext();) {
                final Unit u = (Unit) it.next();
                if (u._ibseq.equals(ibseq)) {
                    return u;
                }
            }
            return null;
        }

        private static class Eval {

            final String _ibevalDiv1;
            final String _ibevalDiv1Name;
            final String _ibevalDiv2;
            final String _ibevalDiv2Name;
            final String _ibevalMark;
            final String _ibevalName;
            final String _ibevalNameEng;
            final String _ibevalPerfect;
            final String _ibevalGrade;

            final Map _unitViewcdScoreMap = new HashMap();

            Eval(
                final String ibevalDiv1,
                final String ibevalDiv1Name,
                final String ibevalDiv2,
                final String ibevalDiv2Name,
                final String ibevalMark,
                final String ibevalName,
                final String ibevalNameEng,
                final String ibevalPerfect,
                final String ibevalGrade
            ) {
                _ibevalDiv1 = ibevalDiv1;
                _ibevalDiv1Name = ibevalDiv1Name;
                _ibevalDiv2 = ibevalDiv2;
                _ibevalDiv2Name = ibevalDiv2Name;
                _ibevalMark = ibevalMark;
                _ibevalName = ibevalName;
                _ibevalNameEng = ibevalNameEng;
                _ibevalPerfect = ibevalPerfect;
                _ibevalGrade = ibevalGrade;
            }
        }

        private static Eval getEval(final List evallist, final String ibevalDiv1, final String ibevalDiv2, final String ibevalMark) {
            for (final Iterator it = evallist.iterator(); it.hasNext();) {
                final Eval e = (Eval) it.next();
                if (e._ibevalDiv1.equals(ibevalDiv1) && e._ibevalDiv2.equals(ibevalDiv2) && e._ibevalMark.equals(ibevalMark)) {
                    return e;
                }
            }
            return null;
        }

        public static String getIBSubclassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_CHAIRSTD AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   CHR.YEAR AS CHAIR_YEAR, ");
            stb.append("   CHR.CLASSCD, CHR.SCHOOL_KIND, CHR.CURRICULUM_CD, CHR.SUBCLASSCD, ");
            stb.append("   CHR.TAKESEMES, ");
            stb.append("   CHR_REGD.GRADE AS CHAIR_GRADE, ");
            stb.append("   CRE.TIME_UNIT ");
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append(" LEFT JOIN CHAIR_STD_DAT CHRSTD ON (");
            if (param._isDp2) {
                stb.append("     CHRSTD.YEAR = '" + param._yearBefore + "' OR ");
            }
            stb.append("     CHRSTD.YEAR = REGD.YEAR AND CHRSTD.SEMESTER <= REGD.SEMESTER ");
            stb.append("     ) AND CHRSTD.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = CHRSTD.YEAR ");
            stb.append("     AND CHR.SEMESTER = CHRSTD.SEMESTER ");
            stb.append("     AND CHR.CHAIRCD = CHRSTD.CHAIRCD ");
            stb.append(" LEFT JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) CHR_REGD_MAX ON CHR_REGD_MAX.SCHREGNO = REGD.SCHREGNO");
            stb.append("     AND CHR_REGD_MAX.YEAR = CHR.YEAR ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT CHR_REGD ON CHR_REGD.SCHREGNO = CHR_REGD_MAX.SCHREGNO");
            stb.append("     AND CHR_REGD.YEAR = CHR_REGD_MAX.YEAR ");
            stb.append("     AND CHR_REGD.SEMESTER = CHR_REGD_MAX.SEMESTER ");
            stb.append(" LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = REGD.YEAR ");
            stb.append("     AND CRE.COURSECD = REGD.COURSECD ");
            stb.append("     AND CRE.MAJORCD = REGD.MAJORCD ");
            stb.append("     AND CRE.GRADE = REGD.GRADE ");
            stb.append("     AND CRE.COURSECODE = REGD.COURSECODE ");
            stb.append("     AND CRE.CLASSCD = CHR.CLASSCD ");
            stb.append("     AND CRE.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("     AND CRE.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("     AND CRE.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append(" REGD.YEAR = '" + param._year + "' ");
            stb.append(" AND REGD.SEMESTER = '" + param._regdSemester + "' ");
            stb.append(" AND REGD.SCHREGNO = ? ");
            stb.append(" AND (CHR.CLASSCD <= '90' OR CHR.CLASSCD = '93' OR CHR.CLASSCD = '95') ");
            stb.append(" ), IBSUBCLASS AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   CHRSTD.SCHREGNO, ");
            stb.append("   CHRSTD.CLASSCD || '-' || CHRSTD.SCHOOL_KIND AS CHR_CLASSCD, ");
            stb.append("   CHRSTD.CLASSCD || '-' || CHRSTD.SCHOOL_KIND || '-' || CHRSTD.CURRICULUM_CD || '-' || CHRSTD.SUBCLASSCD AS CHR_SUBCLASSCD, ");
            stb.append("   IBR.IBCLASSCD || '-' || IBR.IBPRG_COURSE AS IB_CLASSCD, ");
            stb.append("   IBR.IBCLASSCD || '-' || IBR.IBPRG_COURSE || '-' || IBR.IBCURRICULUM_CD || '-' || IBR.IBSUBCLASSCD AS IB_SUBCLASSCD, ");
            stb.append("   CHRSTD.TIME_UNIT, ");
            stb.append("   SGD9.GRADE AS IB_GRADE, ");
            stb.append("   VNY.IBEVAL_DIV1, ");
            stb.append("   VNY.IBEVAL_DIV2, ");
            stb.append("   VNY.IBEVAL_MARK, ");
            stb.append("   VNY.IBEVAL_NAME, ");
            stb.append("   VNY.IBEVAL_NAME_ENG, ");
            stb.append("   VNY.IBSORT AS IBEVAL_SORT, ");
            stb.append("   VNY.IBPERFECT AS IBEVAL_PERFECT, ");
            stb.append("   SGD1.GRADE AS IBEVAL_GRADE, ");
            stb.append("   UNIT.LINK_NO ");
            stb.append(" FROM REGD_CHAIRSTD CHRSTD ");
            stb.append(" LEFT JOIN IBSUBCLASS_REPLACE_DAT IBR ON IBR.IBYEAR = CHRSTD.CHAIR_YEAR ");
            stb.append("     AND IBR.IBGRADE = CHRSTD.CHAIR_GRADE ");
            stb.append("     AND IBR.CLASSCD = CHRSTD.CLASSCD ");
            stb.append("     AND IBR.SCHOOL_KIND = CHRSTD.SCHOOL_KIND ");
            stb.append("     AND IBR.CURRICULUM_CD = CHRSTD.CURRICULUM_CD ");
            stb.append("     AND IBR.SUBCLASSCD = CHRSTD.SUBCLASSCD ");
            stb.append(" LEFT JOIN IBSUBCLASS_UNIT_DAT UNIT ON UNIT.IBYEAR = IBR.IBYEAR ");
            stb.append("     AND UNIT.IBGRADE = IBR.IBGRADE ");
            stb.append("     AND UNIT.IBCLASSCD = IBR.IBCLASSCD ");
            stb.append("     AND UNIT.IBPRG_COURSE = IBR.IBPRG_COURSE ");
            stb.append("     AND UNIT.IBCURRICULUM_CD = IBR.IBCURRICULUM_CD ");
            stb.append("     AND UNIT.IBSUBCLASSCD = IBR.IBSUBCLASSCD ");
            stb.append(" LEFT JOIN IBVIEW_NAME_YMST VNY ON VNY.IBYEAR = IBR.IBYEAR ");
            stb.append("     AND VNY.IBGRADE = IBR.IBGRADE ");
            stb.append("     AND VNY.IBCLASSCD = IBR.IBCLASSCD ");
            stb.append("     AND VNY.IBPRG_COURSE = IBR.IBPRG_COURSE ");
            stb.append("     AND VNY.IBSUBCLASSCD = IBR.IBSUBCLASSCD ");
            stb.append(" LEFT JOIN IBSUBCLASS_GRADE_DAT SGD9 ON SGD9.IBYEAR = IBR.IBYEAR ");
            stb.append("     AND SGD9.IBGRADE = IBR.IBGRADE ");
            stb.append("     AND SGD9.SCHREGNO = CHRSTD.SCHREGNO ");
            stb.append("     AND SGD9.IBCLASSCD = IBR.IBCLASSCD ");
            stb.append("     AND SGD9.IBPRG_COURSE = IBR.IBPRG_COURSE ");
            stb.append("     AND SGD9.IBCURRICULUM_CD = IBR.IBCURRICULUM_CD ");
            stb.append("     AND SGD9.IBSUBCLASSCD = IBR.IBSUBCLASSCD ");
            stb.append("     AND SGD9.SEMESTER = '9' ");
            stb.append("     AND SGD9.IBEVAL_DIV1 = '0' ");
            stb.append("     AND SGD9.IBEVAL_DIV2 = '0' ");
            stb.append("     AND SGD9.IBEVAL_MARK = '00' ");
            stb.append("     AND SGD9.VIEWCD = '0000' ");
            stb.append(" LEFT JOIN IBSUBCLASS_GRADE_DAT SGD1 ON SGD1.IBYEAR = IBR.IBYEAR ");
            stb.append("     AND SGD1.IBGRADE = IBR.IBGRADE ");
            stb.append("     AND SGD1.SCHREGNO = CHRSTD.SCHREGNO ");
            stb.append("     AND SGD1.IBCLASSCD = IBR.IBCLASSCD ");
            stb.append("     AND SGD1.IBPRG_COURSE = IBR.IBPRG_COURSE ");
            stb.append("     AND SGD1.IBCURRICULUM_CD = IBR.IBCURRICULUM_CD ");
            stb.append("     AND SGD1.IBSUBCLASSCD = IBR.IBSUBCLASSCD ");
            stb.append("     AND SGD1.SEMESTER = '" + ("1".equals(param._semester) ? param._semester : "8") + "' ");
            stb.append("     AND SGD1.IBEVAL_DIV1 = VNY.IBEVAL_DIV1 ");
            stb.append("     AND SGD1.IBEVAL_DIV2 = VNY.IBEVAL_DIV2 ");
            stb.append("     AND SGD1.IBEVAL_MARK = VNY.IBEVAL_MARK ");
            stb.append("     AND SGD1.VIEWCD = '0000' ");
            if (null != param._printHyoukaIbsubclasscd) {
                stb.append(" WHERE ");
                stb.append("   IBR.IBCLASSCD || '-' || IBR.IBPRG_COURSE || '-' || IBR.IBCURRICULUM_CD || '-' || IBR.IBSUBCLASSCD = '" + param._printHyoukaIbsubclasscd + "' ");
            }
            stb.append(" ), CHR_MAX_TAKESEMES AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIR_YEAR, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS CHR_SUBCLASSCD, ");
            stb.append("     MAX(T1.TAKESEMES) AS TAKESEMES ");
            stb.append(" FROM REGD_CHAIRSTD T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIR_YEAR, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.CHR_CLASSCD, ");
            stb.append("   T1.CHR_SUBCLASSCD, ");
            stb.append("   CM.CLASSNAME AS CHR_CLASSNAME, ");
            stb.append("   VALUE(SBM.SUBCLASSORDERNAME2, SBM.SUBCLASSNAME) AS CHR_SUBCLASSNAME, ");
            stb.append("   SBM.SUBCLASSABBV AS CHR_SUBCLASSABBV, ");
            stb.append("   TKSM.TAKESEMES AS CHAIR_TAKESEMES, ");
            stb.append("   TKSMS.SEMESTERNAME AS CHAIR_TAKESEMES_NAME, ");
            stb.append("   TKSM.CHAIR_YEAR AS HYOTEI_YEAR, ");
            stb.append("   T1.TIME_UNIT, ");
            stb.append("   RSD.VALUE AS HYOTEI, ");
            stb.append("   RSD.GET_CREDIT, ");
            // --
            stb.append("   T1.IB_CLASSCD AS IBCLASSCD_PRG_COURSE, ");
            stb.append("   T1.IB_SUBCLASSCD, ");
            stb.append("   ICM.IBCLASSNAME_ENG, ");
            stb.append("   ISBM.IBSUBCLASSNAME_ENG, ");
            stb.append("   T1.IB_GRADE, ");
            stb.append("   T1.IBEVAL_DIV1, ");
            stb.append("   NMZ035.NAME1 AS IBEVAL_DIV1_NAME, ");
            stb.append("   T1.IBEVAL_DIV2, ");
            stb.append("   CASE T1.IBEVAL_DIV1 WHEN '1' THEN NMZ037.NAME1 WHEN '2' THEN NMZ038.NAME1 ELSE NMZ036.NAME1 END AS IBEVAL_DIV2_NAME, ");
            stb.append("   T1.IBEVAL_MARK, ");
            stb.append("   T1.IBEVAL_NAME, ");
            stb.append("   T1.IBEVAL_NAME_ENG, ");
            stb.append("   T1.IBEVAL_SORT, ");
            stb.append("   T1.IBEVAL_PERFECT, ");
            stb.append("   T1.IBEVAL_GRADE, ");
            stb.append("   UNIT.LINK_NO, ");
            stb.append("   UNIT.IBSEQ, ");
            stb.append("   UNIT.YEAR AS UNIT_YEAR, ");
            stb.append("   UNIT.SEMESTER AS UNIT_SEMESTER, ");
            stb.append("   UNITSM.SEMESTERNAME AS UNIT_SEMESTERNAME, ");
            stb.append("   UNIT.CLASSCD || '-' || UNIT.SCHOOL_KIND || '-' || UNIT.CURRICULUM_CD || '-' || UNIT.SUBCLASSCD AS UNIT_SUBCLASSCD, ");
            stb.append("   USBM.SUBCLASSNAME AS UNIT_SUBCLASSNAME, ");
            stb.append("   USBM.SUBCLASSABBV AS UNIT_SUBCLASSABBV, ");
            stb.append("   UPLAN.VIEWCD, ");
            stb.append("   SSD.SCORE ");
            stb.append(" FROM IBSUBCLASS T1 ");
            stb.append(" INNER JOIN CLASS_MST CM ON CM.CLASSCD || '-' || CM.SCHOOL_KIND = T1.CHR_CLASSCD ");
            stb.append(" INNER JOIN SUBCLASS_MST SBM ON SBM.CLASSCD || '-' || SBM.SCHOOL_KIND || '-' || SBM.CURRICULUM_CD || '-' || SBM.SUBCLASSCD = T1.CHR_SUBCLASSCD ");
            stb.append(" LEFT JOIN CHR_MAX_TAKESEMES TKSM ON TKSM.CHR_SUBCLASSCD = T1.CHR_SUBCLASSCD ");
            stb.append("     AND TKSM.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SEMESTER_MST TKSMS ON TKSMS.YEAR = TKSM.CHAIR_YEAR ");
            stb.append("     AND TKSMS.SEMESTER = TKSM.TAKESEMES ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT RSD ON RSD.YEAR = TKSM.CHAIR_YEAR ");
            stb.append("     AND RSD.SEMESTER = '9' ");
            stb.append("     AND RSD.TESTKINDCD = '99' ");
            stb.append("     AND RSD.TESTITEMCD = '00' ");
            stb.append("     AND RSD.SCORE_DIV = '09' ");
            stb.append("     AND RSD.CLASSCD || '-' || RSD.SCHOOL_KIND || '-' || RSD.CURRICULUM_CD || '-' || RSD.SUBCLASSCD = T1.CHR_SUBCLASSCD ");
            stb.append("     AND RSD.SCHREGNO = T1.SCHREGNO ");
            // --
            stb.append(" LEFT JOIN IBSUBCLASS_UNIT_DAT UNIT ON UNIT.LINK_NO = T1.LINK_NO ");
            stb.append(" LEFT JOIN IBSUBCLASS_UNITPLAN_DAT UPLAN ON UPLAN.IBYEAR = UNIT.IBYEAR ");
            stb.append("     AND UPLAN.IBGRADE = UNIT.IBGRADE ");
            stb.append("     AND UPLAN.IBCLASSCD = UNIT.IBCLASSCD ");
            stb.append("     AND UPLAN.IBPRG_COURSE = UNIT.IBPRG_COURSE ");
            stb.append("     AND UPLAN.IBCURRICULUM_CD = UNIT.IBCURRICULUM_CD ");
            stb.append("     AND UPLAN.IBSUBCLASSCD = UNIT.IBSUBCLASSCD ");
            stb.append("     AND UPLAN.IBSEQ = UNIT.IBSEQ ");
            stb.append("     AND UPLAN.IBEVAL_DIV1 = T1.IBEVAL_DIV1 ");
            stb.append("     AND UPLAN.IBEVAL_DIV2 = T1.IBEVAL_DIV2 ");
            stb.append("     AND UPLAN.IBEVAL_MARK = T1.IBEVAL_MARK ");
            stb.append(" LEFT JOIN IBSUBCLASS_SCORE_DAT SSD ON SSD.IBYEAR = UNIT.IBYEAR ");
            stb.append("     AND SSD.IBGRADE = UNIT.IBGRADE ");
            stb.append("     AND SSD.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND SSD.IBCLASSCD = UNIT.IBCLASSCD ");
            stb.append("     AND SSD.IBPRG_COURSE = UNIT.IBPRG_COURSE ");
            stb.append("     AND SSD.IBCURRICULUM_CD = UNIT.IBCURRICULUM_CD ");
            stb.append("     AND SSD.IBSUBCLASSCD = UNIT.IBSUBCLASSCD ");
            stb.append("     AND SSD.IBSEQ = UNIT.IBSEQ ");
            stb.append("     AND SSD.IBEVAL_DIV1 = T1.IBEVAL_DIV1 ");
            stb.append("     AND SSD.IBEVAL_DIV2 = T1.IBEVAL_DIV2 ");
            stb.append("     AND SSD.IBEVAL_MARK = T1.IBEVAL_MARK ");
            stb.append("     AND SSD.VIEWCD = UPLAN.VIEWCD ");
            stb.append("     AND (UNIT.YEAR < '" + param._year + "' OR UNIT.YEAR = '" + param._year + "' AND UNIT.SEMESTER <= '" + param._semester + "') ");
            stb.append(" LEFT JOIN IBSUBCLASS_MST ISBM ON ISBM.IBCLASSCD || '-' || ISBM.IBPRG_COURSE || '-' || ISBM.IBCURRICULUM_CD || '-' || ISBM.IBSUBCLASSCD = T1.IB_SUBCLASSCD ");
            stb.append(" LEFT JOIN IBCLASS_MST ICM ON ICM.IBCLASSCD || '-' || ICM.IBPRG_COURSE = T1.IB_CLASSCD ");
            stb.append(" LEFT JOIN SUBCLASS_MST USBM ON USBM.CLASSCD || '-' || USBM.SCHOOL_KIND || '-' || USBM.CURRICULUM_CD || '-' || USBM.SUBCLASSCD = UNIT.CLASSCD || '-' || UNIT.SCHOOL_KIND || '-' || UNIT.CURRICULUM_CD || '-' || UNIT.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ035 ON NMZ035.NAMECD1 = 'Z035' AND NMZ035.NAMECD2 = T1.IBEVAL_DIV1 ");
            stb.append(" LEFT JOIN NAME_MST NMZ036 ON NMZ036.NAMECD1 = 'Z036' AND NMZ036.NAMECD2 = T1.IBEVAL_DIV2 ");
            stb.append(" LEFT JOIN NAME_MST NMZ037 ON NMZ037.NAMECD1 = 'Z037' AND NMZ037.NAMECD2 = T1.IBEVAL_DIV2 ");
            stb.append(" LEFT JOIN NAME_MST NMZ038 ON NMZ038.NAMECD1 = 'Z038' AND NMZ038.NAMECD2 = T1.IBEVAL_DIV2 ");
            stb.append(" LEFT JOIN SEMESTER_MST UNITSM ON UNITSM.YEAR = UNIT.YEAR ");
            stb.append("     AND UNITSM.SEMESTER = UNIT.SEMESTER ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.LINK_NO, ");
            stb.append("   UNIT.IBSEQ, ");
            stb.append("   T1.IBEVAL_SORT, ");
            stb.append("   T1.IBEVAL_DIV1, ");
            stb.append("   T1.IBEVAL_DIV2, ");
            stb.append("   T1.IBEVAL_MARK, ");
            stb.append("   UPLAN.VIEWCD ");
            return stb.toString();
        }
    }

    private static class CsvUtil {
        private static String join(final List columns, final String spl1) {
            final StringBuffer stb = new StringBuffer();
            String spl = "";
            for (final Iterator it = columns.iterator(); it.hasNext();) {
                final String s = (String) it.next();
                stb.append(spl).append(StringUtils.defaultString(s));
                spl = spl1;
            }
            return stb.toString();
        }

        private static String columnListListToData(final List columnListList) {
            final List rtn = new ArrayList();
            for (int i = 0; i < columnListList.size(); i++) {
                final List columnList = (List) columnListList.get(i);
                rtn.add(join(columnList, ","));
            }
            return join(rtn, "\n");
        }

        public static void outputLines(final HttpServletResponse response, final String filename, final List lines) {
            OutputStream os = null;
            try {
                final String outputData = columnListListToData(lines);
                final byte[] data = outputData.getBytes("MS932");
                response.setContentType("text/octet-stream");
                response.setHeader("Accept-Ranges", "none");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(filename.getBytes("MS932"), "ISO8859-1") + "\"");
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Content-Length", String.valueOf(data.length));

                os = new BufferedOutputStream(response.getOutputStream());
                os.write(data);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                try {
                    if (null != os) {
                        os.close();
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 66191 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _cmd;
        final boolean _isCsv;
        final String _year;
        final String _yearBefore;
        final String _semester;
        final String _regdSemester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final boolean _isPrintHyoshi;
        final boolean _isPrintSeisekiHoukoku;
        final boolean _isPrintHyouka1;
        final boolean _isPrintHyouka2;
        final boolean _isPrintHyoukaFooter;
        final String _printHyoukaIbsubclasscd;
        final boolean _isPrintShomei;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _prgid;

        private String _sdate;
        private String _sdateBeforeYear;
        private String _edateBeforeYear;

        private String _gradeCd;
        private String _schoolKind;
        private String _gradeName3;
        private String _certifSchoolSchoolName;
        private String _certifSchoolRemark3;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolJobName;
        private String _ibPrgCourse;
        private boolean _isDp2;
        private String _semesterName;
        final String _phaseSemester;

        private Map _replaceCombined;

        private Map _psMap = new HashMap();
        final Map _attendParamMap;
        final Map _attendParamLastYearMap;

        final List _d026Name1List;
        final Map _d071hyoteiAlphabetMap;
        final List _d072SubclasscdList;

        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _cmd = request.getParameter("cmd");
            _year = request.getParameter("CTRL_YEAR");
            _yearBefore = String.valueOf(Integer.parseInt(_year) - 1);
            _semester = request.getParameter("SEMESTER");
            _regdSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEMESTER") : _semester;
            _date = null == request.getParameter("DATE") ? null : request.getParameter("DATE").replace('/', '-');
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _prgid = request.getParameter("PRGID");
            _isCsv = "csv".equals(_cmd) && ("KNJD186H".equals(_prgid) || "KNJD186I".equals(_prgid));
            _isPrintHyoshi  = null != request.getParameter("PRINT_SIDE1");
            _isPrintSeisekiHoukoku = null != request.getParameter("PRINT_SIDE2");
            if ("KNJD186H".equals(_prgid) || "KNJD186I".equals(_prgid)) {
                _gradeHrclass = null;
                _grade = request.getParameter("GRADE");
                _isPrintHyouka1 = "1".equals(request.getParameter("PRINT_SIDE3")); // ラジオ
                _isPrintHyouka2 = "2".equals(request.getParameter("PRINT_SIDE3")); // ラジオ
                _printHyoukaIbsubclasscd = request.getParameter("IBSUBCLASSCD");
                _isPrintHyoukaFooter = false;
            } else {
                _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
                _grade = _gradeHrclass.substring(0, 2);
                _isPrintHyouka1 = null != request.getParameter("PRINT_SIDE3_1");
                _isPrintHyouka2 = null != request.getParameter("PRINT_SIDE3_2");
                _printHyoukaIbsubclasscd = null;
                _isPrintHyoukaFooter = true;
            }
            _isPrintShomei  = null != request.getParameter("PRINT_SIDE4");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子

            setIBPrgCourse(db2, _year, _grade);
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
            _phaseSemester = "9".equals(_semester) ? KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' ")) : _semester;

            setSchregRegdGdat(db2, _year, _grade);
            setCertifSchoolDat(db2);
            setSubclassReplaceCombined(db2, _year);

            _d026Name1List = getD026Name1List(db2);
            _d071hyoteiAlphabetMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D071' "), "NAMECD2", "NAME1");
            _d072SubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D072' AND NAME1 IS NOT NULL "), "NAME1");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamLastYearMap = new HashMap();
            _attendParamLastYearMap.put("DB2UDB", db2);
            _attendParamLastYearMap.put("HttpServletRequest", request);

            // 学期名称 _arrsemesName をセットします。
            loadAttendSemesArgument(db2);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD186JK' AND NAME = '" + propName + "' "));
        }

        public boolean isFormDP() {
            return "KNJD186K".equals(_prgid) || "KNJD186I".equals(_prgid);
        }


        private List getD026Name1List(final DB2UDB db2) {
            final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            final String sql = " SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1' ";
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD");
        }

        public PreparedStatement getPs(final String psKey) {
            return (PreparedStatement) _psMap.get(psKey);
        }

        public String getCuttingDat(final DB2UDB db2, final String year, final String grade, final String unitSubclasscd, final String div, final String percentage) {
            if (!NumberUtils.isNumber(percentage)) {
                return null;
            }
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                if (null == _psMap.get("CUTTING")) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                    sql.append("  CUTTING_MARK ");
                    sql.append(" FROM IBVIEW_CUTTING_DAT T1 ");
                    sql.append(" WHERE ");
                    sql.append("  YEAR = ? ");
                    sql.append("  AND GRADE = ? ");
                    sql.append("  AND IBPRG_COURSE = '" + _ibPrgCourse + "' ");
                    sql.append("  AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = ? ");
                    sql.append("  AND DATA_DIV = ? ");
                    sql.append("  AND ? BETWEEN CUTTING_LOW AND CUTTING_HIGH ");

                    _psMap.put("CUTTING", db2.prepareStatement(sql.toString()));
                }

                //log.debug(" sql1 = " + sql(sql.toString(), new Object[] {unitSubclasscd, div, new BigDecimal(percentage)}));
                int i;
                i = 0;
                boolean hasRecord = false;
                ps = (PreparedStatement) _psMap.get("CUTTING");
                ps.setString(++i, year);
                ps.setString(++i, grade);
                ps.setString(++i, unitSubclasscd);
                ps.setString(++i, div);
                ps.setBigDecimal(++i, new BigDecimal(percentage));
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("CUTTING_MARK");
                    hasRecord = true;
                }
                DbUtils.closeQuietly(rs);

                if (!hasRecord) {
                    i = 0;
                    ps.setString(++i, year);
                    ps.setString(++i, grade);
                    ps.setString(++i, "00-00-00-000000");
                    ps.setString(++i, div);
                    ps.setBigDecimal(++i, new BigDecimal(percentage));
                    //log.debug(" sql2 = " + sql(sql.toString(), new Object[] {"00-00-00-000000", div, new BigDecimal(percentage)}));
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        rtn = rs.getString("CUTTING_MARK");
                    }
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtn;
        }

        private void setIBPrgCourse(final DB2UDB db2, final String year, final String grade) {
            _ibPrgCourse = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean firstGrade = false;
            try {
                final String sql = " SELECT NAME1, NAME2 FROM NAME_MST WHERE NAMECD1 = 'A034' AND '" + grade + "' BETWEEN NAME2 AND NAME3 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _ibPrgCourse = rs.getString("NAME1");
                    firstGrade = StringUtils.defaultString(grade).equals(rs.getString("NAME2"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _isDp2 = isFormDP() && !firstGrade;
        }

        public String getImagePath(final String filename) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + filename;
            if (new File(path).exists()) {
                log.info(" exists file " + path);
                return path;
            }
            log.warn(" no file " + path);
            return null;
        }

        private String attendRange(final String sdate, final String edate) {
            final String FROM_TO_MARK = "\uFF5E";
            final DateFormat df = new SimpleDateFormat("M/d");
            final StringBuffer stb = new StringBuffer();
            try {
                stb.append(df.format(Date.valueOf(sdate)));
            } catch (Exception e) {
                log.error("exception!", e);
            }
            stb.append(FROM_TO_MARK);
            try {
                stb.append(df.format(Date.valueOf(edate)));
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return stb.toString();
        }

        /**
         * AttendAccumulate使用引数
         * @param db2
         * @throws Exception
         */
        private void loadAttendSemesArgument(final DB2UDB db2) {

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
//                final String sql = "SELECT SDATE, EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '9' AND GRADE = '" + _grade + "' ORDER BY SEMESTER";
                final String sql = "SELECT SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '9' ORDER BY SEMESTER";
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _sdate = rs.getString("SDATE");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            try {
//                final String beforeGrade = new DecimalFormat("00").format(Integer.parseInt(_grade) - 1);
//                final String sql = "SELECT SDATE, EDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _yearBefore + "' AND SEMESTER = '9' AND GRADE = '" + beforeGrade + "' ORDER BY SEMESTER";
                final String sql = "SELECT SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _yearBefore + "' AND SEMESTER = '9' ORDER BY SEMESTER";
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _sdateBeforeYear = rs.getString("SDATE");
                    _edateBeforeYear = rs.getString("EDATE");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            String sql;

            if (_isPrintSeisekiHoukoku) {
                try {
                    sql = getAttendSemesSql(db2, _year, _semester, _sdate, _date, _attendParamMap);
                    _psMap.put("ATTEND_SEMES", db2.prepareStatement(sql));

                    if (_isDp2) {
                        sql = getAttendSemesSql(db2, _yearBefore, "9", _sdateBeforeYear, _edateBeforeYear, _attendParamLastYearMap);
                        _psMap.put("ATTEND_SEMES_LAST_YEAR", db2.prepareStatement(sql));
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            if (_isPrintHyouka1 || _isPrintHyouka2 || _isPrintSeisekiHoukoku) {
                try {
                    sql = getAttendSubclassSql(db2, _year, _semester, _sdate, _date, _attendParamMap);
                    _psMap.put("ATTEND_SUBCLASS", db2.prepareStatement(sql));

                    if (_isDp2) {
                        sql = getAttendSubclassSql(db2, _yearBefore, "9", _sdateBeforeYear, _edateBeforeYear, _attendParamLastYearMap);
                        _psMap.put("ATTEND_SUBCLASS_LAST_YEAR", db2.prepareStatement(sql));
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
        }

        private String getAttendSemesSql(final DB2UDB db2, final String year, final String semester, final String sdate, final String edate, final Map paramMap) throws SQLException, ParseException {
            paramMap.put("schregno", "?");
            final String sql = AttendAccumulate.getAttendSemesSql(
                    year,
                    semester,
                    sdate,
                    edate,
                    paramMap
            );
            return sql;
        }

        private String getAttendSubclassSql(final DB2UDB db2, final String year, final String semester, final String sdate, final String edate, final Map paramMap) throws SQLException, ParseException {
            paramMap.put("schregno", "?");
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    year,
                    semester,
                    sdate,
                    edate,
                    paramMap
            );
            return sql;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _certifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolRemark3 = rs.getString("REMARK3");
                    _certifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolJobName = rs.getString("JOB_NAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSchregRegdGdat(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeCd = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _gradeCd = intToString(rs.getString("GRADE_CD"), "");
                    _gradeName3 = rs.getString("GRADE_NAME3");
                    _schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String intToString(final String v, final String def) {
            if (NumberUtils.isDigits(v)) {
                return String.valueOf(Integer.parseInt(v));
            }
            return StringUtils.defaultString(v, def);
        }

        public void psClose() {
            for (final Iterator it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(ps);
            }
        }

        public String getCombinedSubclasscd(final String attendsubclasscd) {
            return getString(getMappedMap(_replaceCombined, attendsubclasscd), "COMBINED_SUBCLASSCD");
        }

        public String getCombinedSubclassName(final String attendsubclasscd) {
            return getString(getMappedMap(_replaceCombined, attendsubclasscd), "COMBINED_SUBCLASSNAME");
        }

        public String getCombinedSubclassAbbv(final String attendsubclasscd) {
            return getString(getMappedMap(_replaceCombined, attendsubclasscd), "COMBINED_SUBCLASSABBV");
        }

        public String getCombinedClassName(final String attendsubclasscd) {
            return getString(getMappedMap(_replaceCombined, attendsubclasscd), "COMBINED_CLASSNAME");
        }

        private void setSubclassReplaceCombined(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _replaceCombined = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T3.CLASSNAME, ");
                stb.append("     T3.CLASSABBV, ");
                stb.append("     T2.SUBCLASSNAME, ");
                stb.append("     T2.SUBCLASSABBV, ");
                stb.append("     T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                stb.append("     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
                stb.append(" FROM ");
                stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
                stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.COMBINED_CLASSCD AND T2.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD AND T2.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND AND T2.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ");
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.COMBINED_CLASSCD AND T3.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedMap(_replaceCombined, rs.getString("ATTEND_SUBCLASSCD")).put("COMBINED_SUBCLASSCD", rs.getString("COMBINED_SUBCLASSCD"));
                    getMappedMap(_replaceCombined, rs.getString("ATTEND_SUBCLASSCD")).put("COMBINED_SUBCLASSNAME", rs.getString("SUBCLASSNAME"));
                    getMappedMap(_replaceCombined, rs.getString("ATTEND_SUBCLASSCD")).put("COMBINED_SUBCLASSABBV", rs.getString("SUBCLASSABBV"));
                    getMappedMap(_replaceCombined, rs.getString("ATTEND_SUBCLASSCD")).put("COMBINED_CLASSNAME", rs.getString("CLASSNAME"));
                    getMappedMap(_replaceCombined, rs.getString("ATTEND_SUBCLASSCD")).put("COMBINED_CLASSABBV", rs.getString("CLASSABBV"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (_isOutputDebug) {
                log.info(" _replaceCombined = " + _replaceCombined);
            }
        }
    }
}

// eof

