/*
 * $Id: 2ad228295124a9bad7ef7129eb413d4a50c85f18 $
 *
 * 作成日: 2009/11/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *   学校教育システム 賢者 [保健管理]
 *
 *                   ＜ＫＮＪＦ１１１Ａ＞  症状別・保健室利用人数集計表
 *
 */

public class KNJF111A {

    private static final Log log = LogFactory.getLog("KNJF111A.class");

    public final String FORM_NAME = "KNJF101A.frm";

    public final String NAIKA = "1";
    public final String GEKA = "2";
    public final String SONOTA = "3";
    public final String SEITO_IGAI = "4";
    public final String KENKO_SODAN_KATSUDO = "5";

    public final String NAIKA_NAME = "内科";
    public final String GEKA_NAME = "外科";
    public final String SONOTA_NAME = "その他";
    public final String SEITO_IGAI_NAME = "生徒以外";
    public final String KENKO_SODAN_KATSUDO_NAME = "健康相談活動";

    private boolean _hasData;

    Param _param;

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

            final boolean useGrade4 = !_param.isCyukoIkkanHeisetu() || "1".equals(_param._useSchool_KindField);
            final int grade_per_page = useGrade4 ? 4 : 3;
            final int jgrades = (Integer.valueOf(_param._jMaxGrade).intValue() - Integer.valueOf(_param._jMinGrade).intValue() + 1);

            if (_param.isCyukoIkkanCyutokyoikugakkou() && !"1".equals(_param._useSchool_KindField)){
                // log.debug(" :: 中高一貫教育学校");
                final String minGrade = _param._jMinGrade;
                final String maxGrade = _param._hMaxGrade;
                final int grades = (Integer.valueOf(maxGrade).intValue() - Integer.valueOf(minGrade).intValue() + 1) + 1;
                final int page = grades / grade_per_page + (grades % grade_per_page == 0 ? 0 : 1);
                List useTypeLines = getUseTypeLines(db2, null, null);
                for (int i = 0; i < page; i++) {
                    boolean isOutputTotal = (i == page - 1);
                    printMain(svf, useTypeLines, isOutputTotal, useGrade4, page, i * grade_per_page, minGrade, maxGrade);
                }
            } else if (_param.isCyukoIkkanHeisetu() && !"1".equals(_param._useSchool_KindField)) {
                // log.debug(" :: 中高一貫併設高");
                for (int j = 0; j < 2; j++) {
                    String minGrade = j == 0 ? _param._jMinGrade : _param._hMinGrade;
                    String maxGrade = j == 0 ? _param._jMaxGrade : _param._hMaxGrade;
                    final int grades = (Integer.valueOf(maxGrade).intValue() - Integer.valueOf(minGrade).intValue() + 1);
                    final int page = grades / grade_per_page + (grades % grade_per_page == 0 ? 0 : 1);
                    final int offset = (j == 0 ? 0 : jgrades);
                    List useTypeLines = getUseTypeLines(db2, minGrade, maxGrade);
                    for (int i = 0; i < page; i++) {
                        boolean isOutputTotal = (i == page - 1);
                        printMain(svf, useTypeLines, isOutputTotal, useGrade4, page, offset + i * grade_per_page, minGrade, maxGrade);
                    }
                }
            } else {
                // log.debug(" ::");
                final String minGrade;
                final String maxGrade;
                boolean notPrint = false;
                final int offset;
                if ("1".equals(_param._useSchool_KindField)) {
                    if ("J".equals(_param._SCHOOLKIND)) {
                        minGrade = _param._jMinGrade;
                        maxGrade = _param._jMaxGrade;
                        offset = 0;
                    } else if ("H".equals(_param._SCHOOLKIND)) {
                        minGrade = _param._hMinGrade;
                        maxGrade = _param._hMaxGrade;
                        offset = NumberUtils.isDigits(_param._jMaxGrade) ? Integer.parseInt(_param._jMaxGrade) : 0;
                    } else if ("P".equals(_param._SCHOOLKIND)) {
                        minGrade = _param._pMinGrade;
                        maxGrade = _param._pMaxGrade;
                        offset = 0;
                    } else {
                        notPrint = true;
                        minGrade = null;
                        maxGrade = null;
                        offset = 0;
                    }
                } else {
                    if (null == _param._hMinGrade && null == _param._pMaxGrade) {
                        minGrade = _param._pMinGrade;
                        maxGrade = _param._pMaxGrade;
                        offset = 0;
                    } else {
                        minGrade = _param._hMinGrade;
                        maxGrade = _param._hMaxGrade;
                        offset = 0;
                    }
                }
                log.info(" min, max = " + minGrade + ", " + maxGrade);
                if (notPrint == false) {
                    final int grades = (Integer.valueOf(maxGrade).intValue() - Integer.valueOf(minGrade).intValue() + 1) + 1;
                    final int page = grades / grade_per_page + (grades % grade_per_page == 0 ? 0 : 1);
                    List useTypeLines = getUseTypeLines(db2, minGrade, maxGrade);
                    for (int i = 0; i < page; i++) {
                        boolean isOutputTotal = (i == page - 1);
                        log.info(" page = " + String.valueOf(i + 1) + " / " + String.valueOf(page));
                        printMain(svf, useTypeLines, isOutputTotal, useGrade4, page, offset + i * grade_per_page, minGrade, maxGrade);
                    }
                }
            }

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

    private VisitReasonLine getVisitReasonLine(String type, String visitReason, List lists) {
        for (Iterator it = lists.iterator(); it.hasNext();) {
            UseType useType = (UseType) it.next();
            if (!useType._type.equals(type)) {
                continue;
            }
            for (Iterator itl = useType._visitReasonLine.iterator(); itl.hasNext();) {
                VisitReasonLine vrl = (VisitReasonLine) itl.next();
                if (visitReason.equals(vrl._visitReason)) {
                    return vrl;
                }
            }
            VisitReasonLine vrl = new VisitReasonLine(type, visitReason);
            useType._visitReasonLine.add(vrl);
            return vrl;
        }
        
        UseType useType = new UseType(type);
        lists.add(useType);
        VisitReasonLine vrl = new VisitReasonLine(type, visitReason);
        useType._visitReasonLine.add(vrl);
        return vrl;
    }
    
    private List getUseTypeLines(final DB2UDB db2, final String minGrade, final String maxGrade) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        List useTypeLines = new ArrayList();
        try {
            final String sqlNurseoffVisitrect = sqlNurseoffVisitrec(minGrade, maxGrade);
            log.debug(" nurseoff_visitrec_dat sql =" + sqlNurseoffVisitrect);
            ps = db2.prepareStatement(sqlNurseoffVisitrect);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                final String type = rs.getString("TYPE");
                final String visitReason = rs.getString("VISIT_REASON");
                if (type == null || visitReason == null) {
                    continue;
                }
                
                VisitReasonLine vrl = getVisitReasonLine(type, visitReason, useTypeLines);
                vrl._visitReasonName = rs.getString("VISIT_REASON_NAME");
                
                int grade = rs.getInt("GRADE");
                if (grade < 1) {
                    continue;
                }
                
                int countMan = rs.getInt("MAN_COUNT");
                int countWoman = rs.getInt("WOMAN_COUNT");
                int count = rs.getInt("COUNT");
                Count c = new Count(countMan, countWoman, count);
                vrl.add(new Integer(grade), c);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return useTypeLines;
    }
    
    private void printMain(final Vrw32alp svf, final List useTypeLines, boolean isOutputTotal, boolean useGrade4, int page, int printOffset, String min, String max) {
        svf.VrSetForm("KNJF111A.frm", 4);
        svf.VrsOut("PERIOD", changePrintDateGengou(_param._sdate) + "〜" + changePrintDateGengou(_param._edate));
        svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        // log.debug(" isOutputTotal=" + isOutputTotal + " useGrade4=" + useGrade4 + ", page=" + page + ", printOffset=" + printOffset + ", grademin=" + min + " grademax=" + max);
        for (final Iterator it = _param._gradeName1.keySet().iterator(); it.hasNext();) {
            final String grade = (String) it.next();
            final int printIndex = Integer.valueOf(grade).intValue() - printOffset;
            
            final String i = (!isOutputTotal && printIndex == 4 && useGrade4) ? "T" : String.valueOf(printIndex);
            svf.VrsOut(i + "GRADE", (String) _param._gradeName1.get(grade));
        }
        if (isOutputTotal) {
            svf.VrsOut("TGRADE", "合計");
        }

        Map gradeTotalCount = new HashMap();
        
        // 利用区分
        for (Iterator it = useTypeLines.iterator(); it.hasNext();) {
            UseType useType = (UseType) it.next();

            for (Iterator itl = useType._visitReasonLine.iterator(); itl.hasNext();) {
            
                VisitReasonLine vrl = (VisitReasonLine) itl.next();
                
                svf.VrsOut("TYPE", getTypeName(useType._type));
                svf.VrsOut("VISIT_REASON1", vrl._visitReasonName);
                
                for (Iterator itg = vrl._gradeCounts.keySet().iterator(); itg.hasNext();) {
                    Integer grade = (Integer) itg.next();
                    Count c = (Count) vrl._gradeCounts.get(grade);

                    final int printIndex = grade.intValue() - printOffset;
                    final String i = (!isOutputTotal && printIndex == 4 && useGrade4) ? "T" : String.valueOf(printIndex);
                    svf.VrsOut(i + "MALE", zeroBlank(c._man));
                    svf.VrsOut(i + "FEMALE", zeroBlank(c._woman));
                    svf.VrsOut(i + "TOTAL", zeroBlank(c._count));
                }
                
                if (isOutputTotal) {
                    Count total = vrl.getTotal(min, max);
                    svf.VrsOut("TMALE", zeroBlank(total._man));
                    svf.VrsOut("TFEMALE", zeroBlank(total._woman));
                    svf.VrsOut("TTOTAL", zeroBlank(total._count));
                }
                svf.VrEndRecord();

                _hasData = true;
            }

            svf.VrsOut("TYPETOTAL", "（利用区分計）");
            printTypeTotal(svf, useType.getGradeCountMap(), isOutputTotal, useGrade4, printOffset);
            svf.VrEndRecord();

            UseType.getGradeCountMap(gradeTotalCount, useType);
        }
        
        if (_hasData) {
            // 総合計行
            svf.VrsOut("TYPETOTAL", "（合　　　計）");
            printTypeTotal(svf, gradeTotalCount, isOutputTotal, useGrade4, printOffset);
            svf.VrEndRecord();
        }
    }
    
    private void printTypeTotal(Vrw32alp svf, Map gradeCounts, boolean isOutputTotal, boolean useGrade4, int printOffset) {
        Count total = new Count(0, 0, 0);
        for (Iterator it = gradeCounts.keySet().iterator(); it.hasNext();) {
            Integer grade = (Integer) it.next();
            final int printIndex = grade.intValue() - printOffset;
            final String i = (!isOutputTotal && printIndex == 4 && useGrade4) ? "T" : String.valueOf(printIndex);
            Count c = (Count) gradeCounts.get(grade);
            
            svf.VrsOut("TYPE" + i + "MALE", zeroBlank(c._man));
            svf.VrsOut("TYPE" + i + "FEMALE", zeroBlank(c._woman));
            svf.VrsOut("TYPE" + i + "TOTAL", zeroBlank(c._count));
            
            total = total.add(c);
        }
        if (isOutputTotal) {
            svf.VrsOut("TYPETMALE", zeroBlank(total._man));
            svf.VrsOut("TYPETFEMALE", zeroBlank(total._woman));
            svf.VrsOut("TYPETTOTAL", zeroBlank(total._count));
        }
    }
    
    private String zeroBlank(int n) {
        return n == 0 ? "" : String.valueOf(n);
    }

    private String getTypeName(String type) {
        if (NAIKA.equals(type)) {
            return NAIKA_NAME;
        } else if (GEKA.equals(type)) {
            return GEKA_NAME;
        } else if (SONOTA.equals(type)) {
            return SONOTA_NAME;
        } else if (SEITO_IGAI.equals(type)) {
            return SEITO_IGAI_NAME;
        } else  if (KENKO_SODAN_KATSUDO.equals(type)) {
            return KENKO_SODAN_KATSUDO_NAME;
        } else {
            return "";
        }
    }

    private String sqlNurseoffVisitrec(final String minGrade, final String maxGrade) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH NURSEOFF_VISITREC AS ( ");
        stb.append("   SELECT ");
        stb.append("       SCHREGNO, VISIT_DATE, TYPE, VISIT_REASON1 AS VISIT_REASON ");
        stb.append("   FROM  ");
        stb.append("       NURSEOFF_VISITREC_DAT  ");
        stb.append("   WHERE ");
        stb.append("       VISIT_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
        stb.append("       AND VISIT_REASON1 IS NOT NULL ");
        stb.append("   UNION ALL ");
        stb.append("   SELECT ");
        stb.append("       SCHREGNO, VISIT_DATE, TYPE, VISIT_REASON2 AS VISIT_REASON ");
        stb.append("   FROM  ");
        stb.append("       NURSEOFF_VISITREC_DAT  ");
        stb.append("   WHERE ");
        stb.append("       VISIT_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
        stb.append("       AND VISIT_REASON2 IS NOT NULL ");
        stb.append("   UNION ALL ");
        stb.append("   SELECT ");
        stb.append("       SCHREGNO, VISIT_DATE, TYPE, VISIT_REASON3 AS VISIT_REASON ");
        stb.append("   FROM  ");
        stb.append("       NURSEOFF_VISITREC_DAT  ");
        stb.append("   WHERE ");
        stb.append("       VISIT_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
        stb.append("       AND VISIT_REASON3 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" , TEMP AS ( ");
        stb.append(" SELECT T1.VISIT_DATE, T1.TYPE, T1.VISIT_REASON, T1.SCHREGNO, T3.GRADE, T4.SEX ");
        stb.append(" FROM  ");
        stb.append("     NURSEOFF_VISITREC T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T1.SCHREGNO = T4.SCHREGNO ");
        stb.append(" WHERE  ");
        stb.append("     T3.YEAR = '" + _param._year + "' ");
        stb.append("     AND T3.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE YEAR = T3.YEAR AND SCHREGNO = T3.SCHREGNO)");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     TT.TYPE, TT.VISIT_REASON, TT.GRADE, ");
        stb.append("     (CASE WHEN TT.TYPE = '1' THEN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F200' AND NAMECD2 = TT.VISIT_REASON) ");
        stb.append("           WHEN TT.TYPE = '2' THEN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F201' AND NAMECD2 = TT.VISIT_REASON) ");
        stb.append("           WHEN TT.TYPE = '3' THEN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F203' AND NAMECD2 = TT.VISIT_REASON) ");
        stb.append("           WHEN TT.TYPE = '4' THEN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F202' AND NAMECD2 = TT.VISIT_REASON) ");
        stb.append("           WHEN TT.TYPE = '5' THEN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F219' AND NAMECD2 = TT.VISIT_REASON) ");
        stb.append("           ELSE NULL END) AS VISIT_REASON_NAME, ");
        stb.append("     SUM(CASE WHEN TT.SEX = '1' THEN 1 ELSE 0 END) AS MAN_COUNT, ");
        stb.append("     SUM(CASE WHEN TT.SEX = '2' THEN 1 ELSE 0 END) AS WOMAN_COUNT, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM TEMP TT ");
        stb.append(" WHERE ");
        stb.append("    TT.TYPE IN " + SQLUtils.whereIn(true, _param._typeSelected) + " ");
        if (null != minGrade) {
            stb.append("    AND TT.GRADE >= '" + minGrade + "' ");
        }
        if (null != maxGrade) {
            stb.append("    AND TT.GRADE <= '" + maxGrade + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     TT.TYPE, TT.VISIT_REASON, TT.GRADE ");
        stb.append(" ORDER BY ");
        stb.append("     (CASE WHEN TT.TYPE = '5' THEN 3 WHEN TT.TYPE >= '3' THEN INT(TT.TYPE) + 1 ELSE INT(TT.TYPE) END), TT.VISIT_REASON, TT.GRADE ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    public String changePrintDateGengou(final String date) {
        final String youbi = KNJ_EditDate.h_format_W(date) != null ? "（" + KNJ_EditDate.h_format_W(date) + "）" : "";
        return KNJ_EditDate.h_format_JP(date) + youbi;
    }

    private static class UseType {
        final String _type;
        final List _visitReasonLine = new ArrayList();
        
        public UseType(String type) {
            _type = type;
        }

        public Map getGradeCountMap() {
            return getGradeCountMap(new HashMap(), this);
        }

        /**
         * useTypeの学年ごとのCountを合計したマップを得る。
         * @param map1
         * @param useType 利用区分
         * @return
         */
        public static Map getGradeCountMap(Map map1, UseType useType) {
            for (Iterator it = useType._visitReasonLine.iterator(); it.hasNext();) {
                VisitReasonLine line = (VisitReasonLine) it.next();
                for (Iterator itg = line._gradeCounts.keySet().iterator(); itg.hasNext();) {
                    Integer grade = (Integer) itg.next();
                    Count c = line.getCount(grade);
                    if (!map1.containsKey(grade)) {
                        map1.put(grade, new Count(0, 0, 0));
                    }
                    Count useDivGradeTotal = (Count) map1.get(grade);
                    useDivGradeTotal = useDivGradeTotal.add(c);
                    map1.put(grade, useDivGradeTotal);
                }
            }
            return map1;
        }
    }

    private static class VisitReasonLine {
        final String _visitReason;
        final Map _gradeCounts = new HashMap();
        String _visitReasonName;
        
        public VisitReasonLine(String type, String visitReason) {
            _visitReason = visitReason;
        }

        public Count getTotal(String gmin, String gmax) {
            int min = gmin == null ? -1 : Integer.parseInt(gmin);
            int max = gmax == null ? -1 : Integer.parseInt(gmax);
            Count total = new Count(0, 0, 0);
            for (Iterator it = _gradeCounts.keySet().iterator(); it.hasNext();) {
                Integer grade = (Integer) it.next();
                if ((min != -1 && min > grade.intValue()) || (max != -1 && max < grade.intValue())) {
                    continue;
                }
                Count c = getCount(grade);
                total = total.add(c);
            }
            return total;
        }

        void add(Integer grade, Count c) {
            _gradeCounts.put(grade, c);
        }
        
        Count getCount(Integer grade) {
            return (Count) _gradeCounts.get(grade);
        }
    }

    private static class Count {
        final int _man;
        final int _woman;
        final int _count;

        public Count() {
            this(0, 0, 0);
        }
        
        public Count(int man, int woman, int count) {
            _man = man;
            _woman = woman;
            _count = count;
        }

        Count add(Count c) {
            if (c == null) {
                return this;
            }
            return new Count(_man + c._man, _woman + c._woman, _count + c._count);
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _sdate;
        private final String _edate;
        private final String _ctrlDate;
        private final String[] _typeSelected;
        private final Map _gradeName1 = new HashMap();
        final String _useSchool_KindField;
        final String _SCHOOLKIND;

        private final String _z010Namespare2;
        private String _jMinGrade;
        private String _jMaxGrade;
        private String _hMinGrade;
        private String _hMaxGrade;
        private String _pMinGrade;
        private String _pMaxGrade;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _edate = request.getParameter("EDATE").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _typeSelected = request.getParameterValues("CATEGORY_SELECTED");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            
            setGradeName(db2);
            
            _z010Namespare2 = getZ010Namespare2(db2);
            setMaxMinGrade(db2);
        }

        private void setGradeName(DB2UDB db2) throws SQLException {
            _gradeName1.clear();
            String sql = " SELECT GRADE, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' ";
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                sql += "   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _gradeName1.put(rs.getString("GRADE"), rs.getString("GRADE_NAME1"));
                }
            } catch (Exception e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setMaxMinGrade(DB2UDB db2) throws SQLException {
            String sql = " SELECT SCHOOL_KIND, MAX(GRADE) AS MAX_GRADE, MIN(GRADE) AS MIN_GRADE FROM SChREG_REGD_GDAT WHERE YEAR = '" + _year + "' GROUP BY SCHOOL_KIND ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                _jMinGrade = "0";
                _jMaxGrade = "0";
                _hMinGrade = "0";
                _hMaxGrade = "0";
                _pMinGrade = "0";
                _pMaxGrade = "0";
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    if ("J".equals(schoolKind)) {
                        _jMinGrade = rs.getString("MIN_GRADE");
                        _jMaxGrade = rs.getString("MAX_GRADE");
                    } else if ("H".equals(schoolKind)) {
                        _hMinGrade = rs.getString("MIN_GRADE");
                        _hMaxGrade = rs.getString("MAX_GRADE");
                    } else if ("P".equals(schoolKind)) {
                        _pMinGrade = rs.getString("MIN_GRADE");
                        _pMaxGrade = rs.getString("MAX_GRADE");
                    }
                }
            } catch (Exception e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getZ010Namespare2(DB2UDB db2) throws SQLException {
            String rtn = null;
            String sql = " SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAMESPARE2");
                }
            } catch (Exception e) {
                log.debug("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        /**
         * 中高一貫併設ならtrue
         * @return
         */
        public boolean isCyukoIkkanHeisetu() {
            return "1".equals(_z010Namespare2);
        }
        
        /**
         * 中高一貫中等教育学校ならtrue
         * @return
         */
        public boolean isCyukoIkkanCyutokyoikugakkou() {
            return "2".equals(_z010Namespare2);
        }
    }
}

// eof

