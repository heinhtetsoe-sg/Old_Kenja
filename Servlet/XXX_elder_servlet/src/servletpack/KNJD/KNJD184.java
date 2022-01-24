/*
 * $Id: b1814b90945b05381b5ea204b5842ce1bc8b00d6 $
 *
 * 作成日: 2012/02/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 * 学校教育システム 賢者 [成績管理] 面談資料
 */

public class KNJD184 {

    private static final Log log = LogFactory.getLog(KNJD184.class);

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
        try {
            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _param = createParam(db2, request);
            
            _hasData = false;
            
            printMain(db2, svf);
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List viewClassList = ViewClass.getViewClassList(db2, _param);

        final List studentList = getStudentList(db2);
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            student.load(db2, _param);
            
            svf.VrSetForm("KNJD184.frm", 4);

            printSvfHeader(svf, student);
            
            printSvfViewRecord(svf, viewClassList, student);
            
            _hasData = true;
        }
    }

    private void printSvfHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("SEMESTER", _param._semestername);
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("ATTENDNO", student._attendno);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._descDate));
    }
    
    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                String attendno = rs.getString("ATTENDNO");
                attendno = null == attendno ? "" : (NumberUtils.isDigits(attendno)) ? (String.valueOf(Integer.parseInt(attendno)) + "番"): attendno;
                final Student student = new Student(schregno, name, hrName, attendno);
                studentList.add(student);
            }
            
        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }
    
    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("  SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SEMESTER ");
        stb.append("  FROM    SCHREG_REGD_DAT T1 ");
        stb.append("          , SEMESTER_MST T2 ");
        stb.append("  WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T7.HR_NAME, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T5.NAME ");
        stb.append(" FROM ");
        stb.append("    SCHNO_A T1 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = T1.YEAR AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append(" ORDER BY ATTENDNO");
        return stb.toString();
    }
    
    private static int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
    }
    
    private static String notZero(int n) {
        return String.valueOf(n);
    }

    private static class CharMS932 {
        final String _char;
        final int _len;
        public CharMS932(final String v, final byte[] b) {
            _char = v;
            _len = b.length;
        }
        public String toString() {
            return "[" + _char + " : " + _len + "]";
        }
    }
    private static List toCharMs932List(final String src) throws Exception {
        final List rtn = new ArrayList();
        for (int j = 0; j < src.length(); j++) {
            final String z = src.substring(j, j + 1);             //1文字を取り出す
            final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
            rtn.add(c);
        }
        return rtn;
    }

    protected static List retDividString(String targetsrc, final int dividlen, final int dividnum) {
        if (targetsrc == null) {
            return Collections.EMPTY_LIST;
        }
        final List lines = new ArrayList(dividnum);         //編集後文字列を格納する配列
        int len = 0;
        StringBuffer stb = new StringBuffer();
        
        try {
            if (!StringUtils.replace(targetsrc, "\r\n", "\n").equals(targetsrc)) {
//                log.fatal("改行コードが\\r\\n!:" + targetsrc);
                targetsrc = StringUtils.replace(targetsrc, "\r\n", "\n");
            }

            final List charMs932List = toCharMs932List(targetsrc);

            for (final Iterator it = charMs932List.iterator(); it.hasNext();) {
                final CharMS932 c = (CharMS932) it.next();
                //log.debug(" c = " + c);
                
                if (("\n".equals(c._char) || "\r".equals(c._char))) {
                    if (len <= dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                } else {
                    if (len + c._len > dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                    stb.append(c._char);
                    len += c._len;
                }
            }
            if (0 < len) {
                lines.add(stb.toString());
            }
        } catch (Exception ex) {
            log.error("retDividString error! ", ex);
        }
        if (lines.size() > dividnum) {
            return lines.subList(0, dividnum);
        }
        return lines;
    }
    
    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final List viewClassList, final Student student) {
        final int minCommentLine = 5; // 観点コメント行数
        final int maxLine = 51;
        final int keta = 20 * 2;
        int line = 0;
        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();
            final List classnameCharList = viewClass.getClassnameCharacterList(minCommentLine);
            
            final List remarkList;
            if (_param._isKinsoku) {
            	remarkList = KNJ_EditKinsoku.getTokenList(student.getViewstatReportRemark(viewClass._subclasscd), keta);
            	for (int i = remarkList.size(); i < minCommentLine; i++) {
            		remarkList.add("");
            	}
            } else {
            	remarkList = retDividString(student.getViewstatReportRemark(viewClass._subclasscd), keta, minCommentLine);
            }
            
            int vi = 0;
            int classline = 0;
            while (classline < classnameCharList.size()) {
                
                final String viewName = (viewClass.getViewSize() <= vi) ? "　" : viewClass.getViewName(vi);
                
                final List viewRecordList = (viewClass.getViewSize() <= vi) ? Collections.EMPTY_LIST : student.getViewList(viewClass.getViewCd(vi));

                final List viewNameLineList;
                if (_param._isKinsoku) {
                    viewNameLineList = KNJ_EditKinsoku.getTokenList(viewName, ViewClass.viewnamesize, 5); // 観点名称の行のリスト
                } else {
                    viewNameLineList = retDividString(viewName, ViewClass.viewnamesize, 5); // 観点名称の行のリスト
                }
                final List classnameCharp = classnameCharList.subList(classline, classline + viewNameLineList.size());

                for (int j = 0; j < viewNameLineList.size(); j++) {
                    svf.VrsOut("VIEWNAME", (String) viewNameLineList.get(j)); // 観点名称
                    svf.VrsOut("SUBJECTGRPNAME", (String) classnameCharp.get(j));
                    svf.VrsOut("SUBJECTGRP", viewClass._classcd);
                    svf.VrsOut("VIEWGRP", String.valueOf(vi));
                    svf.VrsOut("REMAKGRP", viewClass._classcd);
                    for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                        final ViewRecord vr = (ViewRecord) itv.next();
                        svf.VrsOut("VIEW", vr._d029Namespare1); // 観点
                    }
                    if (remarkList.size() > classline) {
                        svf.VrsOut("REMARK", (String) remarkList.get(classline)); // 観点コメント
                    }
                    svf.VrEndRecord();
                    classline += 1;
                }
                vi += 1;
            }
            line += classline;
        }
        
        // 空行挿入
        for (int i = line % maxLine == 0 ? maxLine : line % maxLine; i < maxLine; i++) {
            svf.VrsOut("SUBJECTGRP", String.valueOf(i));
            svf.VrsOut("VIEWGRP", String.valueOf(i));
            svf.VrsOut("REMAKGRP", String.valueOf(i));
            svf.VrEndRecord();
        }
    }
    
    /**
     * 観点の教科
     */
    private static class ViewClass {
        
        static final int viewnamesize = 56;
        
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final List _viewList;
        ViewClass(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }
        
        public void addView(final String viewcd, final String viewname) {
            final int ms932byte = getMS932ByteCount(viewname);
            _viewList.add(new Object[]{viewcd, viewname, new Integer(ms932byte / viewnamesize + (ms932byte % viewnamesize == 0 ? 0 : 1))});
        }
        
        public List getClassnameCharacterList(final int minCommentLine) {
            final List rtn = new ArrayList();
            String classname = _classname;
            if (null == classname || "".equals(classname)) {
                String space = "";
                for (int i = 0; i < minCommentLine; i++) {
                    space += "　";
                }
                classname = space;
            }
            final int viewLineSize = Math.max(minCommentLine, getViewNameLineSize());
            if (classname.length() >= viewLineSize) {
                for (int i = 0; i < classname.length(); i++) {
                    rtn.add(String.valueOf(classname.charAt(i)));
                }
            } else {
                final int st = (viewLineSize / 2) - (classname.length() / 2 + classname.length() % 2); // センタリング
                for (int i = 0; i < st; i++) {
                    rtn.add("");
                }
                for (int i = st, ci = 0; i < st + classname.length(); i++, ci++) {
                    rtn.add(String.valueOf(classname.charAt(ci)));
                }
                for (int i = st + classname.length(); i < viewLineSize; i++) {
                    rtn.add("");
                }
            }
            return rtn;
        }
        
        public String getViewCd(final int i) {
            return (String) ((Object[]) _viewList.get(i))[0];
        }
        
        public String getViewName(final int i) {
            return (String) ((Object[]) _viewList.get(i))[1];
        }
        
        public int getViewNameLine(final int i) {
            return ((Integer) ((Object[]) _viewList.get(i))[2]).intValue();
        }
        
        public int getViewSize() {
            return _viewList.size();
        }
        
        public int getViewNameLineSize() {
            int rtn = 0;
            for (int i = 0; i < _viewList.size(); i++) {
                rtn += getViewNameLine(i);
            }
            return rtn;
        }
        
        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    
                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._classcd.equals(classcd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }
                    
                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, classname, subclasscd, subclassname);
                        list.add(viewClass);
                    }
                    
                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD < '90' ");
            } else {
                stb.append("     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '90' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        List _viewRecordList = Collections.EMPTY_LIST;
        List _viewstatReportRemarkList = Collections.EMPTY_LIST;
        
        public Student(final String schregno, final String name, final String hrName, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            _viewRecordList = ViewRecord.getViewRecordList(db2, param, _schregno);
            _viewstatReportRemarkList = ViewstatReportRemark.getViewstatReportRemarkList(db2, param, _schregno);
        }
        
        /**
         * 観点コードの観点のリストを得る
         * @param viewcd 観点コード 
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (final Iterator it = _viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewcd.equals(viewRecord._viewcd)) {
                        rtn.add(viewRecord);
                    }
                }
            }
            return rtn;
        }
        
        /**
         * 科目コードの観点のコメントを得る
         * @param subclasscd 科目コード 
         * @return 科目コードの観点のコメント
         */
        public String getViewstatReportRemark(final String subclasscd) {
            if (null != subclasscd) {
                for (final Iterator it = _viewstatReportRemarkList.iterator(); it.hasNext();) {
                    final ViewstatReportRemark vrr = (ViewstatReportRemark) it.next();
                    if (subclasscd.equals(vrr._subclasscd)) {
                        return vrr._remark1;
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {
        
        final String _semester;
        final String _viewcd;
        final String _status;
        final String _d029Namecd2;
        final String _d029Namespare1;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String d029Namecd2,
                final String d029Namespare1,
                final String grade,
                final String viewname,
                final String classcd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _d029Namecd2 = d029Namecd2;
            _d029Namespare1 = d029Namespare1;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }
        
        public static List getViewRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param, schregno);
//                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String semester = rs.getString("SEMESTER");
                    final String viewcd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    final String d029Namecd2 = rs.getString("D029NAMECD2");
                    final String d029Namespare1 = rs.getString("D029NAMESPARE1");
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");
                    
                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, d029Namecd2, d029Namespare1, grade, viewname, classcd, classMstShoworder, showorder);
                    
                    list.add(viewRecord);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T5.NAMECD2 AS D029NAMECD2 ");
            stb.append("     , T5.NAMESPARE1 AS D029NAMESPARE1 ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     INNER JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR "); 
            stb.append("         AND T3.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'D029' ");
            stb.append("         AND T5.ABBV1 = T3.STATUS ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD < '90' ");
            } else {
                stb.append("     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '90' ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD ");
            } else {
                stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            return stb.toString();
        }
    }
    
    /**
     * 観点のコメント
     */
    private static class ViewstatReportRemark {
        
        final String _semester;
        final String _subclasscd;
        final String _remark1;
        ViewstatReportRemark(
                final String semester,
                final String subclasscd,
                final String remark1
                ) {
            _semester = semester;
            _subclasscd = subclasscd;
            _remark1 = remark1;
        }
        
        public static List getViewstatReportRemarkList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewstatReportRemarkSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String semester = rs.getString("SEMESTER");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String remark1 = rs.getString("REMARK1");
                    
                    final ViewstatReportRemark vrr = new ViewstatReportRemark(semester, subclasscd, remark1);
                    
                    list.add(vrr);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewstatReportRemarkSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("     , T1.SEMESTER ");
            stb.append("     , ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T1.REMARK1 ");
            stb.append(" FROM JVIEWSTAT_REPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
        
        public String toString() {
            return "ViewstatReportRemark(" + _semester + "," + _subclasscd + "," + _remark1 + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 59196 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _descDate;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _useCurriculumcd;
        final String _semestername;
        boolean _isKinsoku;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _descDate = request.getParameter("DESC_DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _semestername = getSemestername(db2);
            _isKinsoku = isKinsoku("KIND184", request.getParameter("kinsokuProgramid"));
        }
        
        private boolean isKinsoku(final String programid, final String kinsokuProgramid) {
        	if (null == programid) {
        		return false;
        	}
            if (null != kinsokuProgramid) {
            	final String[] split = StringUtils.split(kinsokuProgramid, ",");
            	if (null != split) {
            		for (int i = 0; i < split.length; i++) {
            			if (programid.equals(StringUtils.trim(split[i]))) {
            				return true;
            			}
            		}
            	}
            }
            return false;
		}

		private String getSemestername(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("SEMESTERNAME")) {
                        rtn = rs.getString("SEMESTERNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

