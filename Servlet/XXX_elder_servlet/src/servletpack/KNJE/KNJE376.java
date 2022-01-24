// kanji=漢字
/*
 * $Id: 7fe834fa09f8d89003ce63eaaf0b80955f646466 $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 進路先一覧
 */
public class KNJE376 {

    private static final Log log = LogFactory.getLog(KNJE376.class);

    private boolean _hasData;

    private Param _param;
    
    private static final String csv = "csv";
    
    private static final String SENKOU_KIND_0 = "0";
    private static final String SENKOU_KIND_1 = "1";

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            response.setContentType("application/pdf");

            _param = createParam(db2, request);

            _hasData = false;

            isCsv = csv.equals(_param._cmd);
            if (!isCsv) {
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());
            }
            final List csvOutputLines = new ArrayList();
            
            printMain(db2, isCsv, svf, csvOutputLines);
            
            if (isCsv) {
                final String filename = getTitle(null, null) + ".csv";
                CsvUtils.outputLines(log, response, filename, csvOutputLines);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }

            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final boolean isCsv, final Vrw32alp svf, final List csvOutputLines) throws SQLException {
        for (int i = 0; i < _param._categorySelected.length; i++) {
            final String grade = _param._categorySelected[i];
            
            if (null != _param._shingaku) {
                printHr(db2, _param, grade, SENKOU_KIND_0, isCsv, svf, csvOutputLines);
            }
            
            if (null != _param._syusyoku) {
                printHr(db2, _param, grade, SENKOU_KIND_1, isCsv, svf, csvOutputLines);
            }
        }
    }
    
    private void csvAddHorizontalBlank(final List lines) {
        for (int i = 0; i < lines.size(); i++) {
            final List line = (List) lines.get(i);
            line.add(null);
        }
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
    
    private void printHr(final DB2UDB db2, final Param param, final String grade, final String senkouKind, final boolean isCsv, final Vrw32alp svf, final List csvOutputLines) {

        final List hrclassMapAllList = getHrClassMapList(db2, param, grade, senkouKind);
        
        if (isCsv) {

            final List columnMapList = new ArrayList();
            final String[] headerHr = {"ATTENDNO", "NAME", "STATNAME"};
            for (int i = 0; i < headerHr.length; i++) {
                final Map columnMap = new HashMap();
                columnMap.put("FIELD", headerHr[i]);
                columnMapList.add(columnMap);
            }
            
            List linesHrAll = new ArrayList();
            for (int i = 0; i < hrclassMapAllList.size(); i++) {
                
                final List studentMapList = (List) hrclassMapAllList.get(i);
                final Map studentMap0 = (Map) studentMapList.get(0);

                final List linesHr = new ArrayList();
                CsvUtils.newLine(linesHr).addAll(new ArrayList(Arrays.asList(new Object[] {studentMap0.get("HR_NAME")})));
                CsvUtils.newLine(linesHr).addAll(new ArrayList(Arrays.asList(new Object[] {studentMap0.get("STAFFNAME")})));
                linesHr.addAll(CsvUtils.getDataOutputLines(false, columnMapList, studentMapList));
                
                csvAddHorizontalBlank(linesHr);
                
                if (linesHrAll.isEmpty()) {
                    linesHrAll.addAll(linesHr);
                } else {
                    linesHrAll = CsvUtils.horizontalUnionLines(linesHrAll, linesHr);
                }
            }

            List linesFooterAll = new ArrayList();

            int total = 0;
            int genekiTotal = 0;
            int retryTotal = 0;

            for (int i = 0; i < hrclassMapAllList.size(); i++) {
                
                final List studentMapList = (List) hrclassMapAllList.get(i);

                int geneki = 0;
                int retry = 0;
                
                for (int sti = 0; sti < studentMapList.size(); sti++) {
                    final Map studentMap = (Map) studentMapList.get(sti);
                    if (null != studentMap.get("STATNAME")) {
                        geneki += 1;
                    } else {
                        retry += 1;
                    }
                }
                
                genekiTotal += geneki;
                retryTotal += retry;
                total += studentMapList.size();
                
                final List linesFooter = new ArrayList();
                CsvUtils.newLine(linesFooter).addAll(Arrays.asList(new Object[] {"", "現役" + senkouName(senkouKind), String.valueOf(geneki)}));
                CsvUtils.newLine(linesFooter).addAll(Arrays.asList(new Object[] {"", "再挑戦", String.valueOf(retry)}));
                csvAddHorizontalBlank(linesFooter);
                
                if (linesFooterAll.isEmpty()) {
                    linesFooterAll.addAll(linesFooter);
                } else {
                    linesFooterAll = CsvUtils.horizontalUnionLines(linesFooterAll, linesFooter);
                }
            }

            final List linesFooterTotal = new ArrayList();
            CsvUtils.newLine(linesFooterTotal).addAll(Arrays.asList(new Object[] {"", "現役" + senkouName(senkouKind) + "合計", String.valueOf(genekiTotal)}));
            CsvUtils.newLine(linesFooterTotal).addAll(Arrays.asList(new Object[] {"", "再挑戦合計", String.valueOf(retryTotal)}));
            
            if (0 != total) {
                final String percentage = new BigDecimal(genekiTotal).multiply(new BigDecimal(100)).divide(new BigDecimal(total), 1, BigDecimal.ROUND_HALF_UP).toString();
                CsvUtils.newLine(linesFooterTotal).addAll(Arrays.asList(new Object[] {"", "合計" + senkouName(senkouKind) + "率", percentage}));
            }

            csvAddHorizontalBlank(linesFooterTotal);
            linesFooterAll = CsvUtils.horizontalUnionLines(linesFooterAll, linesFooterTotal);

            final String title = getTitle(grade, senkouKind);
            CsvUtils.newLine(csvOutputLines).addAll(new ArrayList(Arrays.asList(new Object[] {title, "", "", "", "", "", KNJ_EditDate.h_format_JP(_param._ctrlDate)})));
            csvOutputLines.addAll(linesHrAll);
            csvOutputLines.addAll(linesFooterAll);
            csvOutputLines.add(new ArrayList());
            csvOutputLines.add(new ArrayList());
            
        } else {
            final String form = "KNJE376.frm";
            final int maxHr = 7;

            int genekiTotal = 0;
            int retryTotal = 0;
            int total = 0;
            
            final List pageList = getPageList(hrclassMapAllList, maxHr);
            
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List hrclassMapList = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);
                
                svf.VrsOut("TITLE", getTitle(grade, senkouKind)); // タイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 印刷日付
                
                for (int i = 0; i < maxHr; i++) {
                    final int col = i + 1;
                    svf.VrsOut("COURSE_NAME" + col, "現役" + senkouName(senkouKind)); // 進学就職名称
                }

                for (int i = 0; i < hrclassMapList.size(); i++) {
                    final int col = i + 1;
                    
                    final List studentMapList = (List) hrclassMapList.get(i);
                    final Map studentMap0 = (Map) studentMapList.get(0);
                    
                    svf.VrsOut("HR_NAME" + col, (String) studentMap0.get("HR_NAME")); // 年組名称
                    svf.VrsOut("TEACHER_NAME" + col, (String) studentMap0.get("STAFFNAME")); // 担任名
                    
                    int geneki = 0;
                    int retry = 0;
                    
                    for (int j = 0; j < studentMapList.size(); j++) {
                        final int line = j + 1;
                        final Map studentMap = (Map) studentMapList.get(j);
                        final String attendno = (String) studentMap.get("ATTENDNO");
                        svf.VrsOutn("NO" + col, line, NumberUtils.isDigits(attendno) ? String.valueOf(Integer.parseInt(attendno)) : attendno); // 番号
                        
                        final String stat = (String) studentMap.get("STATNAME");
                        final int ketaStat = getMS932ByteLength(stat);
                        svf.VrsOutn("AFT" + col + "_" + (ketaStat <= 10 ? "1" : ketaStat <= 14 ? "2" : "3"), line, stat);
                        
                        final String name = (String) studentMap.get("NAME");
                        final int ketaName = getMS932ByteLength(name);
                        svf.VrsOutn("NAME" + col + "_" + (ketaName <= 10 ? "1" : ketaName <= 14 ? "2" : "3"), line, name);
                        
                        if (null != studentMap.get("STATNAME")) {
                            geneki += 1;
                        } else {
                            retry += 1;
                        }
                    }
                    
                    svf.VrsOut("SUBTOTAL" + col + "_1", String.valueOf(geneki)); // 現役合格
                    svf.VrsOut("SUBTOTAL" + col + "_2", String.valueOf(retry)); // 再挑戦
                    
                    genekiTotal += geneki;
                    retryTotal += retry;
                    total += studentMapList.size();
                }
                
                if (pi == pageList.size() - 1) {
                    svf.VrsOut("TOTAL1", String.valueOf(genekiTotal)); // 現役合格合計
                    svf.VrsOut("TOTAL2", String.valueOf(retryTotal)); // 再挑戦合計
                    
                    svf.VrsOut("COURSE_PER_NAME1", "合計" + senkouName(senkouKind) + "率"); // 進学就職率
                    svf.VrsOut("COURSE_TOTAL_NAME1", "現役" + senkouName(senkouKind) + "合計"); // 進学就職合計名称
                    if (0 != total) {
                        final String percentage = new BigDecimal(genekiTotal).multiply(new BigDecimal(100)).divide(new BigDecimal(total), 1, BigDecimal.ROUND_HALF_UP).toString();
                        svf.VrsOut("PER", percentage); // 現役進学率
                    }
                }
                
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private String getTitle(final String grade, final String senkouKind) {
        return KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度 " + StringUtils.defaultString((String) _param._gradeName1Map.get(grade)) + " " + senkouName(senkouKind) + "先一覧";
    }

    private String senkouName(final String senkouKind) {
        final String senkouName = SENKOU_KIND_0.equals(senkouKind) ? "進学" : SENKOU_KIND_1.equals(senkouKind) ? "就職" : ((null != _param._shingaku ? "進学" : "") + (null != _param._syusyoku ? "就職" : ""));
        return senkouName;
    }
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }
    
    private List getHrClassMapList(final DB2UDB db2, final Param param, final String grade, final String senkouKind) {
        final List studentMapList = new ArrayList();
        
        final StringBuffer stb = new StringBuffer();
        stb.append("  select ");
        stb.append("          REGD.HR_CLASS, ");
        stb.append("          REGD.ATTENDNO, ");
        stb.append("          REGD.SCHREGNO, ");
        stb.append("          BASE.NAME, ");
        stb.append("          REGDH.HR_NAME, ");
        stb.append("          VALUE(STFM1.STAFFNAME, STFM2.STAFFNAME, STFM3.STAFFNAME) AS STAFFNAME ");
        stb.append("  from ");
        stb.append("          SCHREG_REGD_DAT REGD ");
        stb.append("          INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("          INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("          LEFT JOIN STAFF_MST STFM1 ON STFM1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("          LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("          LEFT JOIN STAFF_MST STFM3 ON STFM3.STAFFCD = REGDH.TR_CD3 ");
        stb.append("  where ");
        stb.append("          REGD.YEAR = '" + param._ctrlYear + "' ");
        stb.append("          AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
        stb.append("          AND REGD.GRADE = '" + grade + "' ");
        stb.append("  order by ");
        stb.append("          REGD.HR_CLASS ");
        stb.append("          , REGD.ATTENDNO ");
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            ResultSetMetaData meta = rs.getMetaData();
            
            while (rs.next()) {
                final Map studentMap = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    studentMap.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                studentMapList.add(studentMap);
            }
            
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        setAftGradCourse(db2, param, studentMapList, senkouKind);
        
        final List hrClassList = new ArrayList();
        List currentHr = null;
        String hrClass = null;
        for (final Iterator it = studentMapList.iterator(); it.hasNext();) {
            final Map studentMap = (Map) it.next();
            if (null == currentHr || !studentMap.get("HR_CLASS").equals(hrClass)) {
                currentHr = new ArrayList();
                hrClassList.add(currentHr);
            }
            currentHr.add(studentMap);
            hrClass = (String) studentMap.get("HR_CLASS");
        }
        
        return hrClassList;
    }
    
    private void setAftGradCourse(final DB2UDB db2, final Param param, final List studentMapList, final String senkouKind) {
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" with TA as( select ");
        stb.append("          SCHREGNO, ");
        stb.append("          '0' as SCH_SENKOU_KIND, ");
        stb.append("          MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, ");
        stb.append("          '1' as COMP_SENKOU_KIND, ");
        stb.append("          MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR ");
        stb.append("  from ");
        stb.append("          AFT_GRAD_COURSE_DAT ");
        stb.append("  where ");
        stb.append("          SCHREGNO = ?  and PLANSTAT = '1' ");
        stb.append("          AND YEAR = '" + param._ctrlYear + "' ");
        stb.append("          AND SENKOU_KIND = '" + senkouKind + "' ");
        stb.append("  group by ");
        stb.append("          SCHREGNO ");
        stb.append(" ), TA2 as( select ");
        stb.append("      (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, ");
        stb.append("      T1.SCHREGNO, ");
        stb.append("      T1.SENKOU_KIND, ");
        stb.append("      MAX(T1.SEQ) AS SEQ ");
        stb.append("  from ");
        stb.append("      AFT_GRAD_COURSE_DAT T1 ");
        stb.append("  inner join TA on ");
        stb.append("      T1.SCHREGNO = TA.SCHREGNO ");
        stb.append("      and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) ");
        stb.append("      and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) ");
        stb.append("  where ");
        stb.append("      T1.PLANSTAT = '1' ");
        stb.append("  group by ");
        stb.append("      (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), ");
        stb.append("      T1.SCHREGNO, ");
        stb.append("      T1.SENKOU_KIND ");
        stb.append(" ) ");
        stb.append(" select  ");
        stb.append("       T1.SENKOU_KIND ");
        stb.append("      ,T1.STAT_CD ");
        stb.append("      ,T1.THINKEXAM ");
        stb.append("      ,T1.JOB_THINK ");
        stb.append("      ,L1.NAME1 as E017NAME1 ");
        stb.append("      ,L2.NAME1 as E018NAME1 ");
        stb.append("      ,L3.SCHOOL_NAME ");
        stb.append("      ,VALUE(L3.SCHOOL_NAME_SHOW1, '') || '・' || VALUE(L5.FACULTYNAME_SHOW1, '') AS PRINT_SHINGAKU ");
        stb.append("      ,T1.FACULTYCD ");
        stb.append("      ,L5.FACULTYNAME ");
        stb.append("      ,T1.DEPARTMENTCD ");
        stb.append("      ,L6.DEPARTMENTNAME ");
        stb.append("      ,L7.ADDR1 AS CAMPUSADDR1 ");
        stb.append("      ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ");
        stb.append("      ,L4.COMPANY_NAME ");
        stb.append("      ,L4.ADDR1 AS COMPANYADDR1 ");
        stb.append("      ,L4.ADDR2 AS COMPANYADDR2 ");
        stb.append(" from ");
        stb.append("      AFT_GRAD_COURSE_DAT T1 ");
        stb.append(" inner join TA2 on ");
        stb.append("      T1.YEAR = TA2.YEAR ");
        stb.append("      and T1.SCHREGNO = TA2.SCHREGNO ");
        stb.append("      and T1.SENKOU_KIND = TA2.SENKOU_KIND ");
        stb.append("      and T1.SEQ = TA2.SEQ ");
        stb.append(" left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD ");
        stb.append(" left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD ");
        stb.append(" left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD ");
        stb.append(" left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD ");
        stb.append("      and L5.FACULTYCD = T1.FACULTYCD ");
        stb.append(" left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD ");
        stb.append("      and L6.FACULTYCD = T1.FACULTYCD ");
        stb.append("      and L6.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append(" left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD ");
        stb.append("      and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD ");
        stb.append(" left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD ");
        stb.append("      and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD ");
        stb.append(" left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD ");
        stb.append(" where ");
        stb.append("      T1.PLANSTAT = '1' ");
        stb.append(" order by ");
        stb.append("      T1.YEAR, T1.SCHREGNO ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());

            for (int i = 0; i < studentMapList.size(); i++) {
                final Map studentMap = (Map) studentMapList.get(i);
                
                ps.setString(1, (String) studentMap.get("SCHREGNO"));
                rs = ps.executeQuery();
            
                while (rs.next()) {
                    if (SENKOU_KIND_0.equals(rs.getString("SENKOU_KIND"))) { // 進学
                        studentMap.put("STATNAME", rs.getString("PRINT_SHINGAKU"));
                    } else if (SENKOU_KIND_1.equals(rs.getString("SENKOU_KIND"))) { // 就職
                        studentMap.put("STATNAME", rs.getString("COMPANY_NAME"));
                    }
                }
                DbUtils.closeQuietly(rs);
            }
            
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
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
        final String[] _categorySelected;
        final String _shingaku;
        final String _syusyoku;
        final String _cmd;
        
        final Map _gradeName1Map;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _shingaku = request.getParameter("SHINGAKU");
            _syusyoku = request.getParameter("SYUSYOKU");
            _cmd = request.getParameter("cmd");
            
            _gradeName1Map = getGradename1Map(db2);
        }
        
        private Map getGradename1Map(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map gradename1Map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.GRADE, T1.GRADE_NAME1 ");
                stb.append(" FROM SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE T1.YEAR = '" + _ctrlYear + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    gradename1Map.put(rs.getString("GRADE"), rs.getString("GRADE_NAME1"));
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradename1Map;
        }

    }
}

// eof
