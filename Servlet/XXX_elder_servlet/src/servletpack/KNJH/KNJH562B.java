// kanji=漢字
/*
 * $Id:  $
 *
 * 作成日: 2021/01/20
 * 作成者: s-shimoji
 *
 * Copyright(C) 2009-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJH562B {

    private static final Log log = LogFactory.getLog("KNJH562.class");

    private static Param _param;
    private boolean _hasData;
    private static final String RANK_GOUKEI = "01";
    private static final String RANK_GRADE = "01";
    private static final int COL_MAX = 9;
    private static final int LINE_MAX = 38;

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

            printMain(svf, db2);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }
    }

    private void printMain(final Vrw32alp svf, DB2UDB db2) throws SQLException {
        svf.VrSetForm("KNJH562B.frm", 4);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        String outputDate = sdf.format(new Date());

        printTitle(svf, outputDate);

        final List<HrClass> hrClassList = getHrClassList(db2);
        final List<Bunpu> bunpuList = getBunpuList(db2);
        final List<Bunpu> cntAndAvgList = getCntAndAvgList(db2, bunpuList);
        final List<Evaluation> evaluationList = getEvaluationList(db2);

        for (int pageCnt = 0; pageCnt < hrClassList.size(); pageCnt += COL_MAX) {
            /**
             * 上段分布のクラス名の印字
             */
            svf.VrsOut("HEADER_TITLE", "");
            for (int hrClassColCnt = 0; ((pageCnt + hrClassColCnt) < hrClassList.size()) && (hrClassColCnt < COL_MAX); hrClassColCnt++) {
                HrClass hrClass = hrClassList.get(pageCnt + hrClassColCnt);
                svf.VrsOut("HR_NAME" + (hrClassColCnt + 1), hrClass._hrClassName);
                _hasData = true;
            }

            /**
             * 上段分布の印字
             */
            for (Bunpu bunpu : bunpuList) {
                svf.VriOut("SCORE1", bunpu._high);
                svf.VrsOut("ITEM1", "　・・・　");
                svf.VriOut("SCORE2", bunpu._low);
                svf.VriOut("TOTAL_NUM1", bunpu._value.setScale(0).intValue());

                for (int bunpuCnt = 0; ((pageCnt + bunpuCnt) < bunpu._valueList.size()) && (bunpuCnt < COL_MAX); bunpuCnt++) {
                    Integer cnt = bunpu._valueList.get(pageCnt + bunpuCnt).setScale(0).intValue();
                    svf.VriOut("NUM1_" + (bunpuCnt + 1), cnt);
                }

                svf.VrEndRecord();
            }

            /**
             * 受験者数
             */
            Bunpu cntBunpu = cntAndAvgList.get(0);
            svf.VrsOut("ITEM1", "受験者数");
            svf.VriOut("TOTAL_NUM1", cntBunpu._value.setScale(0).intValue());

            for (int cntBunpuCnt = 0; ((pageCnt + cntBunpuCnt) < cntBunpu._valueList.size()) && (cntBunpuCnt < COL_MAX); cntBunpuCnt++) {
                BigDecimal value = cntBunpu._valueList.get(pageCnt + cntBunpuCnt);
                svf.VriOut("NUM1_" + (cntBunpuCnt + 1), value.setScale(0).intValue());
            }
            svf.VrEndRecord();

            /**
             * 欠席者数
             */
            Bunpu absentBunpu = cntAndAvgList.get(1);
            svf.VrsOut("ITEM1", "欠　席");
            svf.VriOut("TOTAL_NUM1", absentBunpu._value.setScale(0).intValue());

            for (int absentBunpuCnt = 0; ((pageCnt + absentBunpuCnt) < absentBunpu._valueList.size()) && (absentBunpuCnt < COL_MAX); absentBunpuCnt++) {
                BigDecimal value = absentBunpu._valueList.get(pageCnt + absentBunpuCnt);
                svf.VriOut("NUM1_" + (absentBunpuCnt + 1), value.setScale(0).intValue());
            }
            svf.VrEndRecord();

            /**
             * 総人数
             */
            Bunpu totalCntBunpu = cntAndAvgList.get(2);
            svf.VrsOut("ITEM1", "総人数");
            svf.VriOut("TOTAL_NUM1", totalCntBunpu._value.setScale(0).intValue());

            for (int totalCntBunpuCnt = 0; ((pageCnt + totalCntBunpuCnt) < totalCntBunpu._valueList.size()) && (totalCntBunpuCnt < COL_MAX); totalCntBunpuCnt++) {
                BigDecimal value = totalCntBunpu._valueList.get(pageCnt + totalCntBunpuCnt);
                svf.VriOut("NUM1_" + (totalCntBunpuCnt + 1), value.setScale(0).intValue());
            }
            svf.VrEndRecord();

            /**
             * 平均
             */
            Bunpu avgBunpu = cntAndAvgList.get(3);
            svf.VrsOut("TOTAL_AVE", avgBunpu._value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            for (int avgBunpuCnt = 0; ((pageCnt + avgBunpuCnt) < avgBunpu._valueList.size()) && (avgBunpuCnt < COL_MAX); avgBunpuCnt++) {
                BigDecimal value = avgBunpu._valueList.get(pageCnt + avgBunpuCnt);
                svf.VrsOut("AVE" + (avgBunpuCnt + 1), value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
            svf.VrEndRecord();

            svf.VrsOut("BLANK", "2X");
            svf.VrEndRecord();

            /**
             * 下段分布のクラス名の印字
             */
            svf.VrsOut("HEADER_TITLE", "評　価");
            for (int hrClassColCnt = 0; ((pageCnt + hrClassColCnt) < hrClassList.size()) && (hrClassColCnt < COL_MAX); hrClassColCnt++) {
                HrClass hrClass = hrClassList.get(pageCnt + hrClassColCnt);
                svf.VrsOut("HR_NAME" + (hrClassColCnt + 1), hrClass._hrClassName);
            }
            svf.VrEndRecord();

            /**
             * 下段分布の印字
             */
            for (int j = 0; j < evaluationList.size() - 1; j++) {
                Evaluation evaluation = evaluationList.get(j);
                svf.VrsOut("ITEM1", evaluation._evaluationRank);
                svf.VriOut("TOTAL_NUM1", evaluation._value.setScale(0).intValue());

                for (int evaluationCnt = 0; ((pageCnt + evaluationCnt) < evaluation._valueList.size()) && (evaluationCnt < COL_MAX); evaluationCnt++) {
                    Integer cnt = evaluation._valueList.get(pageCnt + evaluationCnt).setScale(0).intValue();
                    svf.VriOut("NUM1_" + (evaluationCnt + 1), cnt);
                }
                svf.VrEndRecord();
            }
            Evaluation evaluation = evaluationList.get(evaluationList.size() - 1);
            svf.VrsOut("ITEM2", evaluation._evaluationRank);
            svf.VriOut("TOTAL_NUM2", evaluation._value.setScale(0).intValue());
            for (int evaluationCnt = 0; ((pageCnt + evaluationCnt) < evaluation._valueList.size()) && (evaluationCnt < COL_MAX); evaluationCnt++) {
                Integer cnt = evaluation._valueList.get(pageCnt + evaluationCnt).setScale(0).intValue();
                svf.VriOut("NUM2_" + (evaluationCnt + 1), cnt);
            }
            svf.VrEndRecord();

            int blankCnt = bunpuList.size() < 11 ? LINE_MAX - bunpuList.size() - 1 : LINE_MAX - bunpuList.size();
            for (int i = 0; i < blankCnt; i++) {
                svf.VrsOut("BLANK", "2X");
                svf.VrEndRecord();
            }
        }
    }

    private void printTitle(final Vrw32alp svf, final String outputDate) {
        svf.VrsOut("TITLE", _param._year + "年度" + (_param._proficiencyName + "総点"));
        svf.VrsOut("NENDO", _param._proficiencySubclassName);
        svf.VrsOut("DATE", outputDate);
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74810 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _proficiencyDiv;
        final String _proficiencyCd;
        final String _proficiencyName;
        final String _grade;
        final String _gradeName;
        final String _formGroupDiv;
        final String _proficiencySubclassCd;
        final String _proficiencySubclassName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCYCD");
            _proficiencyName = getProficiencyName(db2, _proficiencyDiv, _proficiencyCd);
            _grade = request.getParameter("GRADE");
            _gradeName = getGradeName(db2, _year, _grade);
            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");
            _proficiencySubclassCd = request.getParameter("PROFICIENCY_SUBCLASS_CD");
            _proficiencySubclassName = getProficiencySubclassName(db2, _proficiencySubclassCd);
        }

        private String getProficiencyName(final DB2UDB db2, final String proficiencyDiv, final String proficiencyCd) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + proficiencyDiv + "' AND PROFICIENCYCD = '" + proficiencyCd + "'"));
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'"));
        }

        private String getProficiencySubclassName(final DB2UDB db2, final String proficiencySubclassCd) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SUBCLASS_NAME FROM PROFICIENCY_SUBCLASS_MST WHERE PROFICIENCY_SUBCLASS_CD = '" + proficiencySubclassCd + "'"));
        }
    }

    private List<HrClass> getHrClassList(final DB2UDB db2) throws SQLException {
        List<HrClass> hrClassList = new ArrayList<HrClass>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String hrClassSql = getHrClassSql();
            log.debug(" hrclass sql = " + hrClassSql);
            ps = db2.prepareStatement(hrClassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String hrClassCd = rs.getString("HR_CLASS");
                final String hrClassName = rs.getString("HR_CLASS_NAME1");

                hrClassList.add(new HrClass(hrClassCd, hrClassName));
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return hrClassList;
    }

    private List<Bunpu> getBunpuList(final DB2UDB db2) throws SQLException {
        List<Bunpu> bunpuList = new ArrayList<Bunpu>();
        Bunpu bunpu = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String bunpuSql = getBunpuSql();
            log.debug(" bunpu sql = " + bunpuSql);
            ps = db2.prepareStatement(bunpuSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final int low = rs.getInt("TICK_LOW");
                final int high = rs.getInt("TICK_HIGH");
                final BigDecimal cnt = rs.getBigDecimal("CNT");

                bunpu = new Bunpu(low, high);
                int bunpuIndex = bunpuList.indexOf(bunpu);
                if (bunpuIndex >= 0) {
                    bunpu = bunpuList.get(bunpuIndex);
                } else {
                    bunpuList.add(bunpu);
                }
                bunpu.put(cnt);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return bunpuList;
    }

    private List<Bunpu> getCntAndAvgList(final DB2UDB db2, List<Bunpu> bunpuList) throws SQLException {
        List<Bunpu> cntAndAvgList = new ArrayList<Bunpu>();
        Bunpu cntBunpu = new Bunpu(0, 0);
        Bunpu absentBunpu = new Bunpu(0, 0);
        Bunpu applicantBunpu = new Bunpu(0, 0);
        Bunpu avgBunpu = new Bunpu(0, 0);
        cntAndAvgList.add(cntBunpu);
        cntAndAvgList.add(absentBunpu);
        cntAndAvgList.add(applicantBunpu);
        cntAndAvgList.add(avgBunpu);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String cntAndAvgSql = getCntAndAvgSql();
            log.debug(" cnt and avg sql = " + cntAndAvgSql);
            ps = db2.prepareStatement(cntAndAvgSql);
            rs = ps.executeQuery();

            int totalScore = 0;
            while (rs.next()) {
                final int hrClassScore = rs.getInt("HR_CLASS_SCOER");
                final int hrClassCnt = rs.getInt("HR_CLASS_CNT");
                final int absent = rs.getInt("ABSENT");

                final int applicantCnt = hrClassCnt + absent;
                final double hrClassAvg = (double)hrClassScore / (double)hrClassCnt;
                totalScore += hrClassScore;

                cntBunpu.put(new BigDecimal(hrClassCnt));
                applicantBunpu.put(new BigDecimal(applicantCnt));
                absentBunpu.put(new BigDecimal(absent));
                avgBunpu._valueList.add(new BigDecimal(hrClassAvg));
            }

            if (cntBunpu._value.intValue() == 0) {
                avgBunpu._value = new BigDecimal(0);
            } else {
                final double totalAvg = (double)totalScore / (double)cntBunpu._value.intValue();
                avgBunpu._value = new BigDecimal(totalAvg);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return cntAndAvgList;
    }

    private List<Evaluation> getEvaluationList(final DB2UDB db2) throws SQLException {
        List<Evaluation> evaluationList = new ArrayList<Evaluation>();
        Evaluation evaluation = null;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String evaluationSql = getEvaluationSql();
            log.debug(" evaluation sql = " + evaluationSql);
            ps = db2.prepareStatement(evaluationSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String evaluationRank = rs.getString("EVALUATION_RANK");
                final BigDecimal cnt = rs.getBigDecimal("CNT");

                evaluation = new Evaluation(evaluationRank);
                int evaluationIndex = evaluationList.indexOf(evaluation);
                if (evaluationIndex >= 0) {
                    evaluation = evaluationList.get(evaluationIndex);
                } else {
                    evaluationList.add(evaluation);
                }
                evaluation.put(cnt);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return evaluationList;
    }

    private String getHrClassSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T0.HR_CLASS, ");
        stb.append("     T0.HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON ");
        stb.append("               T1.YEAR     = T0.YEAR ");
        stb.append("           AND T1.SEMESTER = T0.SEMESTER ");
        stb.append("           AND T1.GRADE    = T0.GRADE ");
        stb.append("           AND T1.HR_CLASS = T0.HR_CLASS ");
        stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON ");
        stb.append("               T2.YEAR       = T1.YEAR ");
        stb.append("           AND T2.GRADE      = T1.GRADE ");
        stb.append("           AND T2.COURSECD   = T1.COURSECD ");
        stb.append("           AND T2.MAJORCD    = T1.MAJORCD ");
        stb.append("           AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("     INNER JOIN PROFICIENCY_RANK_DAT L1 ON ");
        stb.append("               L1.YEAR                    = T1.YEAR ");
        stb.append("           AND L1.SEMESTER                = T1.SEMESTER ");
        stb.append("           AND L1.PROFICIENCYDIV          = '" + _param._proficiencyDiv + "' ");
        stb.append("           AND L1.PROFICIENCYCD           = '" + _param._proficiencyCd + "' ");
        stb.append("           AND L1.SCHREGNO                = T1.SCHREGNO ");
        stb.append("           AND L1.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("           AND L1.RANK_DATA_DIV           = '" + RANK_GOUKEI + "' ");
        stb.append("           AND L1.RANK_DIV                = '" + RANK_GRADE + "' ");
        stb.append(" WHERE ");
        stb.append("         T0.YEAR     = '" + _param._year + "' ");
        stb.append("     AND T0.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T0.GRADE    = '" + _param._grade + "' ");
        stb.append("     AND L1.SCORE IS NOT NULL ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             SEQ ");
        stb.append("         FROM ");
        stb.append("             SCHREG_QUALIFIED_HOBBY_DAT L3 ");
        stb.append("         WHERE ");
        stb.append("             L3.YEAR     = T1.YEAR ");
        stb.append("         AND L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T0.HR_CLASS, ");
        stb.append("     T0.HR_CLASS_NAME1 ");
        stb.append(" ORDER BY ");
        stb.append("     T0.HR_CLASS ");
        return stb.toString();
    }

    private String getBunpuSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH RANGE AS ( ");
        stb.append("     SELECT ");
        stb.append("         PROFICIENCYDIV, ");
        stb.append("         PROFICIENCYCD, ");
        stb.append("         TICK_LOW, ");
        stb.append("         TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         PROFICIENCY_TICK_WIDTH_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR                    = '" + _param._year + "' AND ");
        stb.append("         PROFICIENCYDIV          = '" + _param._proficiencyDiv + "' AND ");
        stb.append("         PROFICIENCYCD           = '" + _param._proficiencyCd + "' AND ");
        stb.append("         PROFICIENCY_SUBCLASS_CD = '000000' AND ");
        stb.append("         DIV                     = '1'      AND ");
        stb.append("         GRADE                   = '00'     AND ");
        stb.append("         HR_CLASS                = '000'    AND ");
        stb.append("         COURSECD                = '0'      AND ");
        stb.append("         MAJORCD                 = '000'     AND ");
        stb.append("         COURSECODE              = '0000' ");
        stb.append(" ), ");
        stb.append(" HR_CLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR     = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND GRADE    = '" + _param._grade + "' ");
        stb.append(" ), ");
        stb.append(" RANGE_HR_CLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         HR_CLASS.YEAR, ");
        stb.append("         HR_CLASS.SEMESTER, ");
        stb.append("         HR_CLASS.GRADE, ");
        stb.append("         RANGE.PROFICIENCYDIV, ");
        stb.append("         RANGE.PROFICIENCYCD, ");
        stb.append("         RANGE.TICK_LOW, ");
        stb.append("         RANGE.TICK_HIGH, ");
        stb.append("         HR_CLASS.HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         RANGE CROSS JOIN HR_CLASS ");
        stb.append("     ORDER BY ");
        stb.append("         VALUE(RANGE.TICK_HIGH, 0) DESC, ");
        stb.append("         HR_CLASS.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T0.TICK_LOW, ");
        stb.append("     T0.TICK_HIGH, ");
        stb.append("     T0.HR_CLASS, ");
        stb.append("     COUNT(CASE WHEN (T0.TICK_LOW <= L1.SCORE) AND (L1.SCORE <= T0.TICK_HIGH) THEN '1' ELSE NULL END) AS CNT ");
        stb.append(" FROM ");
        stb.append("     RANGE_HR_CLASS T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON ");
        stb.append("               T1.YEAR     = T0.YEAR ");
        stb.append("           AND T1.SEMESTER = T0.SEMESTER ");
        stb.append("           AND T1.GRADE    = T0.GRADE ");
        stb.append("           AND T1.HR_CLASS = T0.HR_CLASS ");
        stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON ");
        stb.append("               T2.YEAR       = T1.YEAR ");
        stb.append("           AND T2.GRADE      = T1.GRADE ");
        stb.append("           AND T2.COURSECD   = T1.COURSECD ");
        stb.append("           AND T2.MAJORCD    = T1.MAJORCD ");
        stb.append("           AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("     INNER JOIN PROFICIENCY_RANK_DAT L1 ON ");
        stb.append("               L1.YEAR                    = T1.YEAR ");
        stb.append("           AND L1.SEMESTER                = T1.SEMESTER ");
        stb.append("           AND L1.PROFICIENCYDIV          = '" + _param._proficiencyDiv + "' ");
        stb.append("           AND L1.PROFICIENCYCD           = '" + _param._proficiencyCd + "' ");
        stb.append("           AND L1.SCHREGNO                = T1.SCHREGNO ");
        stb.append("           AND L1.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("           AND L1.RANK_DATA_DIV           = '" + RANK_GOUKEI + "' ");
        stb.append("           AND L1.RANK_DIV                = '" + RANK_GRADE + "' ");
        stb.append(" WHERE ");
        stb.append("         L1.SCORE IS NOT NULL ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             SEQ ");
        stb.append("         FROM ");
        stb.append("             SCHREG_QUALIFIED_HOBBY_DAT L3 ");
        stb.append("         WHERE ");
        stb.append("             L3.YEAR     = T1.YEAR ");
        stb.append("         AND L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T0.TICK_LOW, ");
        stb.append("     T0.TICK_HIGH, ");
        stb.append("     T0.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(T0.TICK_HIGH, 0) DESC, ");
        stb.append("     T0.HR_CLASS ");
        return stb.toString();
    }

    private String getCntAndAvgSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(SUM(L1.SCORE), 0) AS HR_CLASS_SCOER, ");
        stb.append("     VALUE(COUNT(L1.SCHREGNO), 0) AS HR_CLASS_CNT, ");
        stb.append("     VALUE(COUNT(L2.SCORE_DI), 0) AS ABSENT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON ");
        stb.append("               T1.YEAR     = T0.YEAR ");
        stb.append("           AND T1.SEMESTER = T0.SEMESTER ");
        stb.append("           AND T1.GRADE    = T0.GRADE ");
        stb.append("           AND T1.HR_CLASS = T0.HR_CLASS ");
        stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON ");
        stb.append("               T2.YEAR       = T1.YEAR ");
        stb.append("           AND T2.GRADE      = T1.GRADE ");
        stb.append("           AND T2.COURSECD   = T1.COURSECD ");
        stb.append("           AND T2.MAJORCD    = T1.MAJORCD ");
        stb.append("           AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("     INNER JOIN PROFICIENCY_RANK_DAT L1 ON ");
        stb.append("               L1.YEAR                    = T1.YEAR ");
        stb.append("           AND L1.SEMESTER                = T1.SEMESTER ");
        stb.append("           AND L1.PROFICIENCYDIV          = '" + _param._proficiencyDiv + "' ");
        stb.append("           AND L1.PROFICIENCYCD           = '" + _param._proficiencyCd + "' ");
        stb.append("           AND L1.SCHREGNO                = T1.SCHREGNO ");
        stb.append("           AND L1.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("           AND L1.RANK_DATA_DIV           = '" + RANK_GOUKEI + "' ");
        stb.append("           AND L1.RANK_DIV                = '" + RANK_GRADE + "' ");
        stb.append("     LEFT JOIN PROFICIENCY_DAT L2 ON ");
        stb.append("               L2.YEAR                    = L1.YEAR ");
        stb.append("           AND L2.SEMESTER                = L1.SEMESTER ");
        stb.append("           AND L2.PROFICIENCYDIV          = L1.PROFICIENCYDIV ");
        stb.append("           AND L2.PROFICIENCYCD           = L1.PROFICIENCYCD ");
        stb.append("           AND L2.SCHREGNO                = L1.SCHREGNO ");
        stb.append("           AND L2.PROFICIENCY_SUBCLASS_CD = L1.PROFICIENCY_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("         T0.YEAR     = '" + _param._year + "' ");
        stb.append("     AND T0.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T0.GRADE    = '" + _param._grade + "' ");
        stb.append("     AND L1.SCORE IS NOT NULL ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             SEQ ");
        stb.append("         FROM ");
        stb.append("             SCHREG_QUALIFIED_HOBBY_DAT L3 ");
        stb.append("         WHERE ");
        stb.append("             L3.YEAR     = T1.YEAR ");
        stb.append("         AND L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T0.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     T0.HR_CLASS ");
        return stb.toString();
    }

    private String getEvaluationSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH RANGE AS ( ");
        stb.append("     SELECT ");
        stb.append("         'A' AS EVALUATION_RANK, ");
        stb.append("         350 AS TICK_LOW, ");
        stb.append("         400 AS TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         SYSIBM.SYSDUMMY1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'B' AS EVALUATION_RANK, ");
        stb.append("         300 AS TICK_LOW, ");
        stb.append("         349 AS TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         SYSIBM.SYSDUMMY1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'C' AS EVALUATION_RANK, ");
        stb.append("         250 AS TICK_LOW, ");
        stb.append("         299 AS TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         SYSIBM.SYSDUMMY1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'D' AS EVALUATION_RANK, ");
        stb.append("         200 AS TICK_LOW, ");
        stb.append("         249 AS TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         SYSIBM.SYSDUMMY1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'E' AS EVALUATION_RANK, ");
        stb.append("         150 AS TICK_LOW, ");
        stb.append("         199 AS TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         SYSIBM.SYSDUMMY1 ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         'F' AS EVALUATION_RANK, ");
        stb.append("         0 AS TICK_LOW, ");
        stb.append("         149 AS TICK_HIGH ");
        stb.append("     FROM ");
        stb.append("         SYSIBM.SYSDUMMY1 ");
        stb.append(" ), ");
        stb.append(" HR_CLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SEMESTER, ");
        stb.append("         GRADE, ");
        stb.append("         HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_HDAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR     = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND GRADE    = '" + _param._grade + "' ");
        stb.append(" ), ");
        stb.append(" RANGE_HR_CLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         HR_CLASS.YEAR, ");
        stb.append("         HR_CLASS.SEMESTER, ");
        stb.append("         HR_CLASS.GRADE, ");
        stb.append("         RANGE.EVALUATION_RANK, ");
        stb.append("         RANGE.TICK_LOW, ");
        stb.append("         RANGE.TICK_HIGH, ");
        stb.append("         HR_CLASS.HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         RANGE CROSS JOIN HR_CLASS ");
        stb.append("     ORDER BY ");
        stb.append("         VALUE(RANGE.TICK_HIGH, 0) DESC, ");
        stb.append("         HR_CLASS.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T0.EVALUATION_RANK, ");
        stb.append("     T0.HR_CLASS, ");
        stb.append("     COUNT(CASE WHEN (T0.TICK_LOW <= L1.SCORE) AND (L1.SCORE <= T0.TICK_HIGH) THEN '1' ELSE NULL END) AS CNT ");
        stb.append(" FROM ");
        stb.append("     RANGE_HR_CLASS T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON ");
        stb.append("               T1.YEAR     = T0.YEAR ");
        stb.append("           AND T1.SEMESTER = T0.SEMESTER ");
        stb.append("           AND T1.GRADE    = T0.GRADE ");
        stb.append("           AND T1.HR_CLASS = T0.HR_CLASS ");
        stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON ");
        stb.append("               T2.YEAR       = T1.YEAR ");
        stb.append("           AND T2.GRADE      = T1.GRADE ");
        stb.append("           AND T2.COURSECD   = T1.COURSECD ");
        stb.append("           AND T2.MAJORCD    = T1.MAJORCD ");
        stb.append("           AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("     INNER JOIN PROFICIENCY_RANK_DAT L1 ON ");
        stb.append("               L1.YEAR                    = T1.YEAR ");
        stb.append("           AND L1.SEMESTER                = T1.SEMESTER ");
        stb.append("           AND L1.PROFICIENCYDIV          = '" + _param._proficiencyDiv + "' ");
        stb.append("           AND L1.PROFICIENCYCD           = '" + _param._proficiencyCd + "' ");
        stb.append("           AND L1.SCHREGNO                = T1.SCHREGNO ");
        stb.append("           AND L1.PROFICIENCY_SUBCLASS_CD = '" + _param._proficiencySubclassCd + "' ");
        stb.append("           AND L1.RANK_DATA_DIV           = '" + RANK_GOUKEI + "' ");
        stb.append("           AND L1.RANK_DIV                = '" + RANK_GRADE + "' ");
        stb.append(" WHERE ");
        stb.append("         L1.SCORE IS NOT NULL ");
        stb.append("     AND NOT EXISTS ( ");
        stb.append("         SELECT ");
        stb.append("             SEQ ");
        stb.append("         FROM ");
        stb.append("             SCHREG_QUALIFIED_HOBBY_DAT L3 ");
        stb.append("         WHERE ");
        stb.append("             L3.YEAR     = T1.YEAR ");
        stb.append("         AND L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T0.EVALUATION_RANK, ");
        stb.append("     T0.TICK_HIGH, ");
        stb.append("     T0.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(T0.TICK_HIGH, 0) DESC, ");
        stb.append("     T0.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        private final String _hrClass;
        private final String _hrClassName;

        private HrClass(final String hrClass, final String hrClassName) {
            _hrClass = hrClass;
            _hrClassName = hrClassName;
        }
    }

    private class Bunpu {
        private final int _low;
        private final int _high;
        private BigDecimal _value;
        private final List<BigDecimal> _valueList;

        private Bunpu(final int low, final int high) {
            _low = low;
            _high = high;
            _value = new BigDecimal(0);
            _valueList = new ArrayList<BigDecimal>();
        }

        private void put(BigDecimal value) {
            _value = _value.add(value);
            _valueList.add(value);
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + _high;
            result = prime * result + _low;

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Bunpu other = (Bunpu) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }

            if (_high != other._high) {
                return false;
            }

            if (_low != other._low) {
                return false;
            }

            return true;
        }

        private KNJH562B getOuterType() {
            return KNJH562B.this;
        }
    }

    private class Evaluation {
        private final String _evaluationRank;
        private BigDecimal _value;
        private final List<BigDecimal> _valueList;

        private Evaluation(final String evaluationRank) {
        	_evaluationRank = evaluationRank;
            _value = new BigDecimal(0);
            _valueList = new ArrayList<BigDecimal>();
        }

        private void put(BigDecimal value) {
            _value = _value.add(value);
            _valueList.add(value);
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((_evaluationRank == null) ? 0 : _evaluationRank.hashCode());

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Evaluation other = (Evaluation) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (_evaluationRank == null) {
                if (other._evaluationRank != null) {
                    return false;
                }
            } else if (!_evaluationRank.equals(other._evaluationRank)) {
                return false;
            }

            return true;
        }

        private KNJH562B getOuterType() {
            return KNJH562B.this;
        }
    }
}

// eof
