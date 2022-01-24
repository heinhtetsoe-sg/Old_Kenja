// kanji=漢字
/*
 * $Id: 311b89426f00d60ad36c6b037f2a71a0df1d73a3 $
 *
 * 作成日: 2011/03/03 11:12:49 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 311b89426f00d60ad36c6b037f2a71a0df1d73a3 $
 */
public class KNJD123X extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJD123X.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("学籍番号");
        retList.add("氏名");
        if ("1".equals(_param._useCurriculumcd)) {
            retList.add("教科コード");
            retList.add("学校校種");
            retList.add("教育課程コード");
        }
        retList.add("科目コード");
        retList.add("科目名");
        retList.add("講座コード");
        retList.add("講座名");
        retList.add("前期_中間評価");
        retList.add("前期_評価");
        retList.add("後期_中間評価");
        retList.add("後期_評価");
        retList.add("学年評定");
        retList.add("履修単位数");
        retList.add("修得単位");
        retList.add("単位数");
        retList.add("前期_時数");
        retList.add("前期_遅刻早退");
        retList.add("前期_欠時数");
        retList.add("前期_欠課数");
        retList.add("後期_時数");
        retList.add("後期_遅刻早退");
        retList.add("後期_欠時数");
        retList.add("後期_欠課数");
        retList.add("備考");
        return retList;
    }
    
    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                dataList.add(toXlsData(rsXls));
            }
        } catch (Exception e) {
            log.error("Exception!", e);
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }
    

    /** XLSデータ出力 */
    protected void outPutXls(final HttpServletResponse response, final boolean header) throws IOException {
        //出力用のシート
        HSSFSheet outPutSheet = _tmpBook.getSheetAt(1);
        HSSFRow setRow;

        //ヘッダの行の書式を使用する為
        HSSFRow headerRow = outPutSheet.getRow(0);
        setRow = outPutSheet.getRow(0);
        int hedCol = 0;
        for (final Iterator itHead = _headList.iterator(); itHead.hasNext();) {
            final String setXlsHedData = (String) itHead.next();
            setCellData(setRow, headerRow, hedCol++, setXlsHedData);
        }

        //最初の行の書式を使用する為
        HSSFRow firstRow = null;
        int line = 0;
        for (final Iterator iter = _dataList.iterator(); iter.hasNext();) {
            final List xlsData = (List) iter.next();
            final int rowLine = header ? line + 1 : line;
            setRow = outPutSheet.getRow(rowLine);
            firstRow = line == 0 ? outPutSheet.getRow(line + 1) : firstRow;
            if (setRow == null) {
                setRow = outPutSheet.createRow(rowLine);
            }
            int col = 0;
            for (final Iterator itXlsData = xlsData.iterator(); itXlsData.hasNext();) {
                final String setXlsData = (String) itXlsData.next();
                setCellData(setRow, firstRow, col++, setXlsData);
            }
            line++;
        }
        //送信
        response.setHeader("Content-Disposition", "inline;filename=noufu_0.xls");
        response.setContentType("application/vnd.ms-excel");
        _tmpBook.write(response.getOutputStream());
    }
    
    private List toXlsData(final ResultSet rs) throws SQLException {
        final List xlsData = new ArrayList();
        xlsData.add(rs.getString("GRADE"));
        xlsData.add(rs.getString("HR_CLASS"));
        xlsData.add(rs.getString("ATTENDNO"));
        xlsData.add(rs.getString("SCHREGNO"));
        xlsData.add(rs.getString("NAME_SHOW"));
        if ("1".equals(_param._useCurriculumcd)) {
            xlsData.add(rs.getString("CLASSCD"));
            xlsData.add(rs.getString("SCHOOL_KIND"));
            xlsData.add(rs.getString("CURRICULUM_CD"));
        }
        xlsData.add(rs.getString("SUBCLASSCD"));
        xlsData.add(rs.getString("SUBCLASSNAME"));
        xlsData.add(rs.getString("CHAIRCD"));
        xlsData.add(rs.getString("CHAIRNAME"));
        xlsData.add(!StringUtils.isEmpty(rs.getString("SEM1_INTR_VALUE_DI")) ? rs.getString("SEM1_INTR_VALUE_DI") : rs.getString("SEM1_INTR_VALUE"));
        xlsData.add(!StringUtils.isEmpty(rs.getString("SEM1_VALUE_DI")) ? rs.getString("SEM1_VALUE_DI") : rs.getString("SEM1_VALUE"));
        xlsData.add(!StringUtils.isEmpty(rs.getString("SEM2_INTR_VALUE_DI")) ? rs.getString("SEM2_INTR_VALUE_DI") : rs.getString("SEM2_INTR_VALUE"));
        xlsData.add(!StringUtils.isEmpty(rs.getString("SEM2_VALUE_DI")) ? rs.getString("SEM2_VALUE_DI") : rs.getString("SEM2_VALUE"));
        xlsData.add(!StringUtils.isEmpty(rs.getString("GRAD_VALUE_DI")) ? rs.getString("GRAD_VALUE_DI") : rs.getString("GRAD_VALUE"));
        xlsData.add(rs.getString("COMP_CREDIT"));
        xlsData.add(rs.getString("GET_CREDIT"));
        xlsData.add(rs.getString("CREDITS"));
        int late_early = 0;
        int kekka_jisuu_1 = 0;
        int late_early_1 = toInt(rs, "LATE_1") + toInt(rs, "EARLY_1");
        int ketu_jisu_1  = toInt(rs, "SICK_1") + toInt(rs, "NOTICE_1") + toInt(rs, "NONOTICE_1") + toInt(rs, "NURSEOFF_1");
        if ("1".equals(_param._knjSchoolMst._subOffDays))  ketu_jisu_1 += toInt(rs, "OFFDAYS_1");
        if ("1".equals(_param._knjSchoolMst._subAbsent))   ketu_jisu_1 += toInt(rs, "ABSENT_1");
        if ("1".equals(_param._knjSchoolMst._subSuspend))  ketu_jisu_1 += toInt(rs, "SUSPEND_1");
        if ("1".equals(_param._knjSchoolMst._subMourning)) ketu_jisu_1 += toInt(rs, "MOURNING_1");
        if ("1".equals(_param._knjSchoolMst._subVirus))    ketu_jisu_1 += toInt(rs, "VIRUS_1");
        if ("1".equals(_param._knjSchoolMst._subKoudome))  ketu_jisu_1 += toInt(rs, "KOUDOME_1");
        
        final String absentCov = _param._knjSchoolMst._absentCov;
        if (StringUtils.isNumeric(_param._knjSchoolMst._absentCovLate) && ("1".equals(absentCov) || "3".equals(absentCov))) {
            if ("1".equals(absentCov)) {
                late_early = late_early_1 - late_early_1 % Integer.parseInt(_param._knjSchoolMst._absentCovLate); //余りが出ないようにする
                kekka_jisuu_1 = ketu_jisu_1 + late_early / Integer.parseInt(_param._knjSchoolMst._absentCovLate);
            } else {
                late_early = late_early_1;
                kekka_jisuu_1 = ketu_jisu_1 + late_early / Integer.parseInt(_param._knjSchoolMst._absentCovLate);
            }
        } else {
            kekka_jisuu_1 = ketu_jisu_1;
        }
        xlsData.add(rs.getString("LESSON_1"));
        xlsData.add(0 == late_early_1 ? "" : String.valueOf(late_early_1));
        xlsData.add(0 == ketu_jisu_1 ? "" : String.valueOf(ketu_jisu_1));
        xlsData.add(0 == kekka_jisuu_1 ? "" : String.valueOf(kekka_jisuu_1));
        int late_early_2 = toInt(rs, "LATE_2") + toInt(rs, "EARLY_2");
        int ketu_jisu_2  = toInt(rs, "SICK_2") + toInt(rs, "NOTICE_2") + toInt(rs, "NONOTICE_2") + toInt(rs, "NURSEOFF_2");
        int kekka_jisuu_2 = 0;
        if ("1".equals(_param._knjSchoolMst._subOffDays))  ketu_jisu_2 += toInt(rs, "OFFDAYS_2");
        if ("1".equals(_param._knjSchoolMst._subAbsent))   ketu_jisu_2 += toInt(rs, "ABSENT_2");
        if ("1".equals(_param._knjSchoolMst._subSuspend))  ketu_jisu_2 += toInt(rs, "SUSPEND_2");
        if ("1".equals(_param._knjSchoolMst._subMourning)) ketu_jisu_2 += toInt(rs, "MOURNING_2");
        if ("1".equals(_param._knjSchoolMst._subVirus))    ketu_jisu_2 += toInt(rs, "VIRUS_2");
        if ("1".equals(_param._knjSchoolMst._subKoudome))  ketu_jisu_2 += toInt(rs, "KOUDOME_2");
        if (StringUtils.isNumeric(_param._knjSchoolMst._absentCovLate) && ("1".equals(absentCov) || "3".equals(absentCov))) {
            if ("1".equals(absentCov)) {
                late_early = late_early_2 - late_early_2 % Integer.parseInt(_param._knjSchoolMst._absentCovLate); //余りが出ないようにする
                kekka_jisuu_2 = ketu_jisu_2 + late_early / Integer.parseInt(_param._knjSchoolMst._absentCovLate);
            } else {
                late_early = late_early_2;
                kekka_jisuu_2 = ketu_jisu_2 + late_early / Integer.parseInt(_param._knjSchoolMst._absentCovLate);
            }
        } else {
            kekka_jisuu_2 = ketu_jisu_2;
        }
        xlsData.add(rs.getString("LESSON_2"));
        xlsData.add(0 == late_early_2 ? "" : String.valueOf(late_early_2));
        xlsData.add(0 == ketu_jisu_2 ? "" : String.valueOf(ketu_jisu_2));
        xlsData.add(0 == kekka_jisuu_2 ? "" : String.valueOf(kekka_jisuu_2));
        xlsData.add(rs.getString("REMARK"));
        return xlsData;
    }
    
    private int toInt(final ResultSet rs, final String field) throws SQLException {
        return StringUtils.isNumeric(rs.getString(field)) ? rs.getInt(field) : 0;
    }
    
    protected String[] getCols() {
        return null;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         COURSECODE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("         GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
        stb.append("     GROUP BY ");
        stb.append("         YEAR, ");
        stb.append("         SCHREGNO, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS, ");
        stb.append("         ATTENDNO, ");
        stb.append("         COURSECD, ");
        stb.append("         MAJORCD, ");
        stb.append("         COURSECODE ");

        stb.append(" ), ATTEND AS ( ");
        stb.append("     SELECT ");
        stb.append("         TT1.YEAR, ");
        stb.append("         TT1.SEMESTER, ");
        stb.append("         TT1.SCHREGNO, ");
        stb.append("         TT1.CLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     TT1.SCHOOL_KIND, ");
            stb.append("     TT1.CURRICULUM_CD, ");
        }
        stb.append("         TT1.SUBCLASSCD, ");
        stb.append("         SUM(TT1.LESSON) AS LESSON, ");
        stb.append("         SUM(TT1.OFFDAYS) AS OFFDAYS, ");
        stb.append("         SUM(TT1.ABSENT) AS ABSENT, ");
        stb.append("         SUM(TT1.SUSPEND) AS SUSPEND, ");
        stb.append("         SUM(TT1.MOURNING) AS MOURNING, ");
        if ("true".equals(_param._useVirus)) {
        	stb.append("         SUM(TT1.VIRUS) AS VIRUS, ");
        }
        if ("true".equals(_param._useKoudome)) {
        	stb.append("         SUM(TT1.KOUDOME) AS KOUDOME, ");
        }        
        stb.append("         SUM(TT1.LATE) AS LATE, ");
        stb.append("         SUM(TT1.EARLY) AS EARLY, ");
        stb.append("         SUM(TT1.SICK) AS SICK, ");
        stb.append("         SUM(TT1.NOTICE) AS NOTICE, ");
        stb.append("         SUM(TT1.NONOTICE) AS NONOTICE, ");
        stb.append("         SUM(TT1.NURSEOFF) AS NURSEOFF ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SUBCLASS_DAT TT1 ");
        stb.append("     INNER JOIN ");
        stb.append("         SCHREG LL1 ON  LL1.YEAR = TT1.YEAR ");
        stb.append("                    AND LL1.SCHREGNO = TT1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         TT1.COPYCD = '0' ");
        stb.append("     GROUP BY ");
        stb.append("         TT1.YEAR, ");
        stb.append("         TT1.SEMESTER, ");
        stb.append("         TT1.SCHREGNO, ");
        stb.append("         TT1.CLASSCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     TT1.SCHOOL_KIND, ");
            stb.append("     TT1.CURRICULUM_CD, ");
        }
        stb.append("         TT1.SUBCLASSCD ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L8.NAME_SHOW, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L1.CLASSCD, ");
            stb.append("     L1.SCHOOL_KIND, ");
            stb.append("     L1.CURRICULUM_CD, ");
        }
        stb.append("     L1.SUBCLASSCD, ");
        stb.append("     L5.SUBCLASSNAME, ");
        stb.append("     L1.CHAIRCD, ");
        stb.append("     L6.CHAIRNAME, ");
        stb.append("     L1.SEM1_INTR_VALUE, ");
        stb.append("     L1.SEM1_INTR_VALUE_DI, ");
        stb.append("     L1.SEM1_VALUE, ");
        stb.append("     L1.SEM1_VALUE_DI, ");
        stb.append("     L1.SEM2_INTR_VALUE, ");
        stb.append("     L1.SEM2_INTR_VALUE_DI, ");
        stb.append("     L1.SEM2_VALUE, ");
        stb.append("     L1.SEM2_VALUE_DI, ");
        stb.append("     L1.GRAD_VALUE, ");
        stb.append("     L1.GRAD_VALUE_DI, ");
        stb.append("     L1.COMP_CREDIT, ");
        stb.append("     L1.GET_CREDIT, ");
        stb.append("     L7.CREDITS, ");

        stb.append("     L2.LESSON   AS LESSON_1, ");
        stb.append("     L2.OFFDAYS  AS OFFDAYS_1, ");
        stb.append("     L2.ABSENT   AS ABSENT_1, ");
        stb.append("     L2.SUSPEND  AS SUSPEND_1, ");
        stb.append("     L2.MOURNING AS MOURNING_1, ");
        if ("true".equals(_param._useVirus)) {
        	stb.append("         L2.VIRUS AS VIRUS_1, ");
        }
        if ("true".equals(_param._useKoudome)) {
        	stb.append("         L2.KOUDOME AS KOUDOME_1, ");
        }        
        stb.append("     L2.LATE     AS LATE_1, ");
        stb.append("     L2.EARLY    AS EARLY_1, ");
        stb.append("     L2.SICK     AS SICK_1, ");
        stb.append("     L2.NOTICE   AS NOTICE_1, ");
        stb.append("     L2.NONOTICE AS NONOTICE_1, ");
        stb.append("     L2.NURSEOFF AS NURSEOFF_1, ");

        stb.append("     L3.LESSON   AS LESSON_2, ");
        stb.append("     L3.OFFDAYS  AS OFFDAYS_2, ");
        stb.append("     L3.ABSENT   AS ABSENT_2, ");
        stb.append("     L3.SUSPEND  AS SUSPEND_2, ");
        stb.append("     L3.MOURNING AS MOURNING_2, ");
        if ("true".equals(_param._useVirus)) {
        	stb.append("         L3.VIRUS AS VIRUS_2, ");
        }
        if ("true".equals(_param._useKoudome)) {
        	stb.append("         L3.KOUDOME AS KOUDOME_2, ");
        }         
        stb.append("     L3.LATE     AS LATE_2, ");
        stb.append("     L3.EARLY    AS EARLY_2, ");
        stb.append("     L3.SICK     AS SICK_2, ");
        stb.append("     L3.NOTICE   AS NOTICE_2, ");
        stb.append("     L3.NONOTICE AS NONOTICE_2, ");
        stb.append("     L3.NURSEOFF AS NURSEOFF_2, ");

        stb.append("     L4.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREG T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_DAT L1 ON  L1.YEAR     = T1.YEAR ");
        stb.append("                   AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ATTEND L2 ON  L2.YEAR       = T1.YEAR ");
        stb.append("               AND L2.SEMESTER   = '1' ");
        stb.append("               AND L2.SCHREGNO   = T1.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND L2.CLASSCD = L1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append("               AND L2.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ATTEND L3 ON  L3.YEAR       = T1.YEAR ");
        stb.append("               AND L3.SEMESTER   = '2' ");
        stb.append("               AND L3.SCHREGNO   = T1.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("           AND L3.CLASSCD = L1.CLASSCD ");
            stb.append("           AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("           AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append("               AND L3.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_REMARK_DAT L4 ON  L4.YEAR       = T1.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                      AND L4.CLASSCD = L1.CLASSCD ");
            stb.append("                      AND L4.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("                      AND L4.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append("                          AND L4.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append("                          AND L4.SCHREGNO   = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_MST L5 ON L5.SUBCLASSCD = L1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("                   AND L5.CLASSCD = L1.CLASSCD ");
            stb.append("                   AND L5.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("                   AND L5.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_DAT L6 ON  L6.YEAR     = T1.YEAR ");
        stb.append("                  AND L6.SEMESTER = '1' ");
        stb.append("                  AND L6.CHAIRCD  = L1.CHAIRCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     CREDIT_MST L7 ON  L7.YEAR       = T1.YEAR ");
        stb.append("                   AND L7.COURSECD   = T1.COURSECD ");
        stb.append("                   AND L7.MAJORCD    = T1.MAJORCD ");
        stb.append("                   AND L7.GRADE      = T1.GRADE ");
        stb.append("                   AND L7.COURSECODE = T1.COURSECODE ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("               AND L7.CLASSCD = L1.CLASSCD ");
            stb.append("               AND L7.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("               AND L7.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append("                   AND L7.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_BASE_MST L8 ON L8.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L1.CLASSCD, ");
            stb.append("     L1.SCHOOL_KIND, ");
            stb.append("     L1.CURRICULUM_CD, ");
        }
        stb.append("     L1.SUBCLASSCD, ");
        stb.append("     L1.CHAIRCD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String[] _classSelected;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final boolean _header;
        private final String _templatePath;
        private final String _mode;
        private final KNJSchoolMst _knjSchoolMst;
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            
            _header = true; // request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _mode = request.getParameter("XLS_MODE");
            
            KNJSchoolMst ksm = null;
            try {
                ksm = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("exception!", ex);
            }
            _knjSchoolMst = ksm;
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");            
        }

    }
}

// eof
