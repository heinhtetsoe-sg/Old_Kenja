// kanji=漢字
/*
 * $Id: 1d31f818ec2e0995ee5f3d66bfa034fbad4b53bb $
 *
 * 作成日: 2007/07/02 17:19:09 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 1d31f818ec2e0995ee5f3d66bfa034fbad4b53bb $
 */
public class KNJD233V {

    private static final String THIS_YEAR    = "0";
    private static final String LAST_YEAR    = "1";

    private static final String SCHOOLCD_HONKOU = "0";
    private static final String SCHOOLCD_ZENSEKI = "1";

    private static final String SOUGOU_CLASS = "90";

    private static final Log log = LogFactory.getLog(KNJD233V.class);

    private Param _param;
    private boolean _hasData;

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

            printMain(db2, svf);
        } catch (Exception e) {
            log.error("exception!", e);
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
            if (null != svf) {
                svf.VrQuit();
            }
        }
    }

    /** 印刷処理メイン */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printDataList = getPrintDataList(db2, _param);

        final String form = "KNJD233V.frm";
        svf.VrSetForm(form, 4);
        int line = 1;
        for (final Iterator it = printDataList.iterator(); it.hasNext();) {
            final OutputData outputData = (OutputData) it.next();

            if (null != _param._outputDiv1) {
                line = printData(svf, _param, outputData.getGradPass(_param), _param._titlePass, line);
            }
            if (null != _param._outputDiv2) {
                line = printData(svf, _param, outputData.getGradUnPass(_param), _param._titleUnPass, line);
            }
        }

//        if (log.isDebugEnabled()) {
//            // debug出力
//            for (final Iterator it = printDataList.iterator(); it.hasNext();) {
//                final OutputData outputData = (OutputData) it.next();
//                for (final Iterator itPass = outputData.getGradPass(_param).iterator(); itPass.hasNext();) {
//                    final Student student = (Student) itPass.next();
//                    log.debug(student);
//                }
//                for (final Iterator itUnPass = outputData.getGradUnPass(_param).iterator(); itUnPass.hasNext();) {
//                    final Student student = (Student) itUnPass.next();
//                    log.debug(student);
//                }
//            }
//        }
    }

    public List getPrintDataList(final DB2UDB db2, final Param param) {
        final List printDataList = new ArrayList();

        for (int i = 0; i < param._hrClass.length; i++) {
            final OutputData o = new OutputData();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getStudent(param, param._hrClass[i]);
                log.info(" student sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String studyrecCreditZenseki = rs.getString("STUDYREC_CREDIT_ZENSEKI");
                    final String studyrecCreditUntilLastYear = rs.getString("STUDYREC_CREDIT_UNTIL_LAST");
                    final String abroadCreditUntilLastYear = rs.getString("ABROAD_CREDIT_UNTIL_LAST");
                    final String recordDatCredit = rs.getString("RECORD_DAT_CREDIT");
                    final String abroadCreditThisYear = rs.getString("ABROAD_CREDIT_THIS_YEAR");
                    final String qualifiedCredit = rs.getString("QUALIFIED_CREDIT");
                    final String mikomiCredit = rs.getString("MIKOMI_CREDIT");

                    final Student student = new Student(
                            rs.getString("HOMEROOMATEND"),
                            rs.getString("SCHREGNO"),
                            rs.getString("NAME"),
                            add(null, studyrecCreditZenseki),
                            studyrecCreditUntilLastYear,
                            abroadCreditUntilLastYear,
                            recordDatCredit,
                            abroadCreditThisYear,
                            qualifiedCredit,
                            mikomiCredit
                    );

                    student._subclassCredit.add(new UnPassSubclass("", rs.getString("UNFINISHCREDIT_UNTIL_LAST_YEAR")));

                    o._studentList.add(student);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            load(param, db2, o._studentList);

            printDataList.add(o);
        }
        return printDataList;
    }

    private int printData(
            final Vrw32alp svf,
            final Param param,
            final List list,
            final String title,
            final int line
    ) {
        final int maxLine = 50;
        int rtnLine = line;
        for (final Iterator its = list.iterator(); its.hasNext();) {
            final Student student = (Student) its.next();

            svf.VrsOut("NUMBER1", student._no);
            svf.VrsOut("GRADE_CLASS_ATTENDNO1", student._homeRoomAtend);
            svf.VrsOut("SCHREGNO1", student._schregno);
            svf.VrsOut("NAME1_" + (20 < KNJ_EditEdit.getMS932ByteLength(student._name) ? "2" : "1"), student._name);
            svf.VrsOut("BEF_CREDIT1", student._studyrecCreditZenseki); // 前籍校の修得単位
            svf.VrsOut("PRE_CREDIT1", add(student._studyrecCreditUntilLastYear, student._abroadCreditUntilLastYear)); // 前年度までの修得単位
            svf.VrsOut("C_CREDIT1", add(add(student._recordDatCredit, student._abroadCreditThisYear), student._qualifiedCredit)); // 今年度の修得単位
            svf.VrsOut("A_CREDIT1", student._mikomiCredit); // 修得（見込）単位数計
            svf.VrsOut("TOTAL_C_CREDIT1", student._totalCredits); // 修得単位数計

            if (student._subclassCredit.size() == 0) {
                student._subclassCredit.add(new UnPassSubclass("", "")); // dummy
            }
            for (final Iterator it = student._subclassCredit.iterator(); it.hasNext();) {
                final UnPassSubclass unPassSubclass = (UnPassSubclass) it.next();

                svf.VrsOut("NENDO", param._gengou);
                svf.VrsOut("SEMESTER", param._semesterName);
                svf.VrsOut("TITLE", title);
                svf.VrsOut("DATE", param._dateString);
                if (_param.isTannisei()) {
                    svf.VrsOut("ITEM", "未履修");
                } else {
                    svf.VrsOut("ITEM", "未修得");
                }

                svf.VrsOut("SUBCLASS1_" + (40 < KNJ_EditEdit.getMS932ByteLength(unPassSubclass._name) ? "2" : "1"), unPassSubclass._name);
                svf.VrsOut("PRE_N_CREDIT1", unPassSubclass._credit);
                rtnLine++;
                svf.VrEndRecord();
                _hasData = true;
                if (rtnLine > maxLine) {
                    rtnLine = 1;
                }
            }
        }
        if (rtnLine > 1) {
            for (;rtnLine <= maxLine; rtnLine++) {
                svf.VrEndRecord();
            }
            rtnLine = 1;
        }
        return rtnLine;
    }

    /** 出力データクラス */
    private static class OutputData {
        final List _studentList = new ArrayList();

        public List getGradPass(final Param param) {
            return getStudent(param, true);
        }

        public List getGradUnPass(final Param param) {
            return getStudent(param, false);
        }


        private List getStudent(final Param param, final boolean flg) {
            final List list = new ArrayList();
            int no = 1;
            for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (param._gradCredits <= toInt(student._totalCredits) == flg) {
                    student._no = String.valueOf(no);
                    list.add(student);
                    no++;
                }
            }
            return list;
        }
    }

    private static String getStudent(final Param param, final String hrClass) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCH_INFO AS ( ");
        stb.append(" SELECT ");
        stb.append("     L1.HR_NAMEABBV || '-' || T1.ATTENDNO AS HOMEROOMATEND ");
        stb.append("     , T1.SCHREGNO ");
        stb.append("     , L2.NAME ");
        stb.append("     , L2.GRD_DATE ");
        stb.append("     , T1.GRADE ");
        stb.append("     , T1.COURSECD ");
        stb.append("     , T1.MAJORCD ");
        stb.append("     , T1.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("          AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
        stb.append("          AND GDAT.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        if (null != param._ctrlDate) {
            stb.append("     AND NOT (ENTGRD.GRD_DATE IS NOT NULL AND ENTGRD.GRD_DATE < '" + param._ctrlDate + "' AND ENTGRD.GRD_DIV NOT IN ('1', '4')) "); // 転退学した人を除く
        }
        stb.append(" ), SCH_INF_LAST_YEAR AS ( ");
        stb.append(getSchInfoLastYear(param, null));

        stb.append(" ), BSD AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     CASE WHEN SUM(T1.GET_CREDIT) IS NULL AND SUM(T1.ADD_CREDIT) IS NULL THEN NULL ");
        stb.append("      ELSE VALUE(SUM(T1.GET_CREDIT), 0) + VALUE(SUM(T1.ADD_CREDIT), 0) END AS STUDYREC_CREDIT_ZENSEKI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + SCHOOLCD_ZENSEKI + "' ");
        stb.append("     AND EXISTS (SELECT 'X' FROM SCH_INFO T3 WHERE T3.SCHREGNO = T1.SCHREGNO) ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");

        stb.append(" ), SSD AS ( ");
        stb.append(getStudyRec(param, THIS_YEAR));

        stb.append(" ), ABT AS ( ");
        stb.append(getAbroad(param, THIS_YEAR));

        stb.append(" ), ABL AS ( ");
        stb.append(getAbroad(param, LAST_YEAR));

        stb.append(" ), REC AS ( ");
        stb.append(getRecordDat(param));

        stb.append(getMikomiCredit(param, " CHAIR ", " REC_SUBCLASS ", " MKM "));

        stb.append(" ), SQD AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(VALUE(CREDITS, 0)) AS QUALIFIED_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_QUALIFIED_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + param._year + "' ");
        stb.append("     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");

        stb.append(" ), UNSSD AS ( ");
        stb.append(getStudyRec(param, LAST_YEAR));

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.HOMEROOMATEND, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     BSD.STUDYREC_CREDIT_ZENSEKI, ");
        stb.append("     SSD.STUDYREC_CREDIT_UNTIL_LAST, ");
        stb.append("     ABL.ABROAD_CREDITS AS ABROAD_CREDIT_UNTIL_LAST, ");
        stb.append("     REC.RECORD_DAT_CREDIT, ");
        stb.append("     ABT.ABROAD_CREDITS AS ABROAD_CREDIT_THIS_YEAR, ");
        stb.append("     SQD.QUALIFIED_CREDIT, ");
        stb.append("     UNSSD.UNFINISHCREDIT_UNTIL_LAST_YEAR, ");
        stb.append("     MKM.MIKOMI_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SCH_INFO T1 ");
        stb.append("     LEFT JOIN SSD ON SSD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN REC ON REC.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SQD ON SQD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN UNSSD ON UNSSD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN ABT ON ABT.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN ABL ON ABL.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN BSD ON BSD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN MKM ON MKM.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        if ("2".equals(param._outputOrder)) {
            stb.append("     VALUE(SSD.STUDYREC_CREDIT_UNTIL_LAST, 0) + VALUE(ABL.ABROAD_CREDITS, 0) DESC, ");
        } else if ("3".equals(param._outputOrder)) {
            stb.append("     VALUE(REC.RECORD_DAT_CREDIT, 0) + VALUE(ABT.ABROAD_CREDITS, 0) DESC, ");
        }
        stb.append("     T1.HOMEROOMATEND ");

        return stb.toString();
    }

    private static String getSchInfoLastYear(final Param param, final String schregno) {

        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        if (param.isGakuensei()) {
            stb.append("     MAX(T1.YEAR) AS YEAR ");
        } else {
            stb.append("     T1.YEAR ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR < '" + param._year + "' ");
        if (null != schregno) {
            stb.append("     AND T1.SCHREGNO = ? ");
        } else {
            stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
        }
        if (param.isGakuensei()) {
            stb.append("     AND T1.GRADE IN (" + param._gradeInState + ") ");
            stb.append("     AND EXISTS ( ");
            stb.append("            SELECT ");
            stb.append("                'x' ");
            stb.append("            FROM ");
            stb.append("                (SELECT ");
            stb.append("                        SCHREGNO, ");
            stb.append("                    GRADE, ");
            stb.append("                    MAX(YEAR || SEMESTER) AS YEAR_SEM ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_REGD_DAT ");
            stb.append("                WHERE ");
            stb.append("                    YEAR < '" + param._year + "' ");
            if (null != schregno) {
                stb.append("                    AND SCHREGNO = ? ");
            } else {
                stb.append("                    AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
            }
            stb.append("                        AND GRADE IN (" + param._gradeInState + ") ");
            stb.append("                GROUP BY ");
            stb.append("                    SCHREGNO, ");
            stb.append("                    GRADE) E1 ");
            stb.append("            WHERE ");
            stb.append("                T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("                AND T1.GRADE = E1.GRADE ");
            stb.append("                AND T1.YEAR || T1.SEMESTER = E1.YEAR_SEM ");
            stb.append("        ) ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        if (param.isTannisei()) {
            stb.append("     T1.YEAR, ");
        }
        stb.append("     T1.GRADE ");

        return stb.toString();
    }

    private static String getStudyRec(final Param param, final String div) {
        final StringBuffer stb = new StringBuffer();
        if (THIS_YEAR.equals(div)) {
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS STUDYREC_CREDIT_UNTIL_LAST ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     INNER JOIN (SELECT DISTINCT YEAR, SCHREGNO FROM SCH_INF_LAST_YEAR) T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOLCD = '" + SCHOOLCD_HONKOU + "' ");
            stb.append("     AND T1.YEAR < '" + param._year + "' ");
            stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
        } else {
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(VALUE(L1.CREDITS, 0)) AS UNFINISHCREDIT_UNTIL_LAST_YEAR ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     LEFT JOIN SCH_INF_LAST_YEAR T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST L1 ON L1.YEAR = T1.YEAR ");
            stb.append("          AND L1.COURSECD = T2.COURSECD ");
            stb.append("          AND L1.MAJORCD = T2.MAJORCD ");
            stb.append("          AND L1.GRADE = T2.GRADE ");
            stb.append("          AND L1.COURSECODE = T2.COURSECODE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND L1.CLASSCD = T1.CLASSCD ");
                stb.append("          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("          AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOLCD = '" + SCHOOLCD_HONKOU + "' ");
            stb.append("     AND T1.YEAR < '" + param._year + "' ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            if (param.isTannisei()) {
                stb.append("     AND VALUE(T1.COMP_CREDIT, 0) = 0 ");
            } else {
                stb.append("     AND VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) = 0 ");
            }
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD IN (SELECT ");
                stb.append("                               CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
            } else {
                stb.append("     AND T1.SUBCLASSCD IN (SELECT ");
                stb.append("                               SUBCLASSCD ");
            }
            stb.append("                           FROM ");
            stb.append("                               SUBCLASS_MST ");
            stb.append("                           WHERE ");
            stb.append("                               ELECTDIV <> '1' ");
            stb.append("                          ) ");
            stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
        }
        return stb.toString();
    }

    /**
     * 見込み単位
     * @param param
     * @param tabChair
     * @param tabRecordSubclass
     * @param tabMikomi 見込み単位テーブル名
     * @return
     */
    private static String getMikomiCredit(final Param param, final String tabChair, final String tabRecordSubclass, final String tabMikomi) {
        final StringBuffer stb = new StringBuffer();

        final String table;
        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(param._useTestCountflg)) {
            table = "RECORD_SCORE_DAT";
        } else if (param.isUseRecordScoreDat()) {
            table = "RECORD_SCORE_DAT";
        } else {
            table = "RECORD_DAT";
        }

        stb.append(" ), " + tabChair + " AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || ");
        }
        stb.append(" T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append(" MAX(CRE.CREDITS) AS CREDITS ");
        stb.append(" FROM CHAIR_STD_DAT T1 ");
        stb.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append(" INNER JOIN SCH_INFO T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
        stb.append("     AND CRE.COURSECD = T3.COURSECD ");
        stb.append("     AND CRE.MAJORCD = T3.MAJORCD ");
        stb.append("     AND CRE.GRADE = T3.GRADE ");
        stb.append("     AND CRE.COURSECODE = T3.COURSECODE ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     AND CRE.CLASSCD = T2.CLASSCD ");
            stb.append("     AND CRE.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND CRE.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("     AND CRE.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
        stb.append("     AND substr(T2.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append("     AND NOT EXISTS (SELECT 'X' ");
        stb.append("                     FROM SUBCLASS_REPLACE_COMBINED_DAT E1 ");
        stb.append("                     WHERE E1.YEAR = '" + param._year + "' ");
        stb.append("                       AND E1.CALCULATE_CREDIT_FLG = '1' "); // 1:固定 => 先科目の単位数を加算するので元科目を除く
        stb.append("                       AND E1.ATTEND_CLASSCD = T2.CLASSCD ");
        stb.append("                       AND E1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("                       AND E1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("                       AND E1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("                     UNION ALL ");
        stb.append("                     SELECT 'X' ");
        stb.append("                     FROM SUBCLASS_REPLACE_COMBINED_DAT E1 ");
        stb.append("                     WHERE E1.YEAR = '" + param._year + "' ");
        stb.append("                       AND E1.CALCULATE_CREDIT_FLG = '2' "); // 2:加算 => 元科目の単位数を加算するので先科目を除く
        stb.append("                       AND E1.COMBINED_CLASSCD = T2.CLASSCD ");
        stb.append("                       AND E1.COMBINED_SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("                       AND E1.COMBINED_CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("                       AND E1.COMBINED_SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("                     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || ");
        }
        stb.append(" T2.SUBCLASSCD ");

        stb.append(" ), " + tabRecordSubclass + " AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || ");
        }
        stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     CASE WHEN T1.GET_CREDIT IS NOT NULL OR T1.ADD_CREDIT IS NOT NULL THEN ");
        stb.append("          VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) ");
        stb.append("     END AS RECORD_DAT_CREDIT ");
        stb.append(" FROM " + table + " T1 ");
        if (param._hasRECORD_PROV_FLG_DAT) {
            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT L1 ON L1.YEAR = T1.YEAR ");
            stb.append("         AND L1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L1.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(param._useTestCountflg)) {
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.TESTKINDCD = '99' ");
            stb.append("     AND T1.TESTITEMCD = '00' ");
            stb.append("     AND T1.SCORE_DIV = '09' ");
            if (param._hasRECORD_PROV_FLG_DAT) {
                stb.append("     AND L1.PROV_FLG IS NULL ");
            }
        } else if (param.isUseRecordScoreDat()) {
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.TESTKINDCD = '99' ");
            stb.append("     AND T1.TESTITEMCD = '00' ");
            stb.append("     AND T1.SCORE_DIV = '00' ");
        }
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append("     AND NOT EXISTS (SELECT 'X' ");
        stb.append("                     FROM SUBCLASS_REPLACE_COMBINED_DAT E1 ");
        stb.append("                     WHERE E1.YEAR = '" + param._year + "' ");
        stb.append("                       AND E1.CALCULATE_CREDIT_FLG = '1' "); // 1:固定 => 先科目の単位数を加算するので元科目を除く
        stb.append("                       AND E1.ATTEND_CLASSCD = T1.CLASSCD ");
        stb.append("                       AND E1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("                       AND E1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                       AND E1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("                     UNION ALL ");
        stb.append("                     SELECT 'X' ");
        stb.append("                     FROM SUBCLASS_REPLACE_COMBINED_DAT E1 ");
        stb.append("                     WHERE E1.YEAR = '" + param._year + "' ");
        stb.append("                       AND E1.CALCULATE_CREDIT_FLG = '2' "); // 2:加算 => 元科目の単位数を加算するので先科目を除く
        stb.append("                       AND E1.COMBINED_CLASSCD = T1.CLASSCD ");
        stb.append("                       AND E1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("                       AND E1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                       AND E1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("                     ) ");

        stb.append(" ), " + tabMikomi + " AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(T1.CREDITS) AS MIKOMI_CREDIT ");
        stb.append(" FROM " + tabChair + " T1 ");
        stb.append(" LEFT JOIN " + tabRecordSubclass + " T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" INNER JOIN SCH_INFO T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE (T3.GRD_DATE IS NULL ");
        stb.append("          OR NOT T3.GRD_DATE >= (SELECT EDATE FROM SEMESTER_MST WHERE YEAR = '" + param._year + "' AND SEMESTER = '" + param._semester + "') ");
        stb.append("         ) ");
        stb.append("     AND T2.RECORD_DAT_CREDIT IS NULL ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");


        return stb.toString();
    }

    private static String getRecordDat(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO ");
        stb.append("     , SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS RECORD_DAT_CREDIT ");
        stb.append(" FROM ");
        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(param._useTestCountflg)) {
            stb.append("     RECORD_SCORE_DAT T1 ");
            if (param._hasRECORD_PROV_FLG_DAT) {
                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT L1 ON L1.YEAR = T1.YEAR ");
                stb.append("         AND L1.CLASSCD = T1.CLASSCD ");
                stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("         AND L1.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.TESTKINDCD = '99' ");
            stb.append("     AND T1.TESTITEMCD = '00' ");
            stb.append("     AND T1.SCORE_DIV = '09' ");
            if (param._hasRECORD_PROV_FLG_DAT) {
                stb.append("     AND L1.PROV_FLG IS NULL ");
            }
        } else if (param.isUseRecordScoreDat()) {
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '9' ");
            stb.append("     AND TESTKINDCD = '99' ");
            stb.append("     AND TESTITEMCD = '00' ");
            stb.append("     AND SCORE_DIV = '00' ");
        } else {
            stb.append("     RECORD_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
        }
        stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append("     AND NOT EXISTS (SELECT 'X' ");
        stb.append("                     FROM SUBCLASS_REPLACE_COMBINED_DAT E1 ");
        stb.append("                     WHERE E1.YEAR = '" + param._year + "' ");
        stb.append("                       AND E1.CALCULATE_CREDIT_FLG = '1' "); // 1:固定 => 先科目の単位数を加算するので元科目を除く
        stb.append("                       AND E1.ATTEND_CLASSCD = T1.CLASSCD ");
        stb.append("                       AND E1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("                       AND E1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                       AND E1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("                     UNION ALL ");
        stb.append("                     SELECT 'X' ");
        stb.append("                     FROM SUBCLASS_REPLACE_COMBINED_DAT E1 ");
        stb.append("                     WHERE E1.YEAR = '" + param._year + "' ");
        stb.append("                       AND E1.CALCULATE_CREDIT_FLG = '2' "); // 2:加算 => 元科目の単位数を加算するので先科目を除く
        stb.append("                       AND E1.COMBINED_CLASSCD = T1.CLASSCD ");
        stb.append("                       AND E1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("                       AND E1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                       AND E1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("                     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");

        return stb.toString();
    }

    private static String getAbroad(final Param param, final String div) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(VALUE(ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("   TRANSFERCD = '1' ");
        if (THIS_YEAR.equals(div)) {
            stb.append("     AND FISCALYEAR(TRANSFER_SDATE) = '" + param._year + "' ");
        } else {
            stb.append("     AND FISCALYEAR(TRANSFER_SDATE) < '" + param._year + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");

        return stb.toString();
    }

    public void load(
            final Param param,
            final DB2UDB db2,
            final List studentList
    ) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_INF_LAST_YEAR AS ( ");
        stb.append(getSchInfoLastYear(param, ""));
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     SUM(VALUE(L1.CREDITS, 0)) AS UNFINISHCREDIT_UNTIL_LAST_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN SCH_INF_LAST_YEAR T2 ON T1.YEAR || T1.SCHREGNO = T2.YEAR || T2.SCHREGNO ");
        stb.append("     LEFT JOIN CREDIT_MST L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.COURSECD = T2.COURSECD ");
        stb.append("          AND L1.MAJORCD = T2.MAJORCD ");
        stb.append("          AND L1.GRADE = T2.GRADE ");
        stb.append("          AND L1.COURSECODE = T2.COURSECODE ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("          AND L1.CLASSCD = T1.CLASSCD ");
            stb.append("          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("          AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("          AND L2.CLASSCD = T1.CLASSCD ");
            stb.append("          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + SCHOOLCD_HONKOU + "' ");
        stb.append("     AND T1.YEAR < '" + param._year + "' ");
        stb.append("     AND T1.YEAR || T1.SCHREGNO = T2.YEAR || T2.SCHREGNO ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        if (_param.isTannisei()) {
            stb.append("     AND VALUE(T1.COMP_CREDIT, 0) = 0 ");
        } else {
            stb.append("     AND VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) = 0 ");
        }

        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD IN (SELECT ");
            stb.append("                               CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        } else {
            stb.append("     AND T1.SUBCLASSCD IN (SELECT ");
            stb.append("                               SUBCLASSCD ");
        }
        stb.append("                           FROM ");
        stb.append("                               SUBCLASS_MST ");
        stb.append("                           WHERE ");
        stb.append("                               ELECTDIV <> '1' ");
        stb.append("                          ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     , T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
        } else {
            stb.append("     , T1.SUBCLASSCD, ");
        }
        stb.append("     L2.SUBCLASSNAME ");
        stb.append(" ORDER BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        } else {
            stb.append("     T1.SUBCLASSCD ");
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                if (param.isGakuensei()) {
                    ps.setString(2, student._schregno);
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    student._subclassCredit.add(new UnPassSubclass(rs.getString("SUBCLASSNAME"), rs.getString("UNFINISHCREDIT_UNTIL_LAST_YEAR")));
                }
                DbUtils.closeQuietly(rs);
            }

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(ps);
        }
    }


    /** 生徒 */
    private static class Student {
        String _no;
        final String _homeRoomAtend;
        final String _schregno;
        final String _name;
        final String _studyrecCreditZenseki;
        final String _studyrecCreditUntilLastYear;
        final String _abroadCreditUntilLastYear;
        final String _recordDatCredit;
        final String _abroadCreditThisYear;
        final String _qualifiedCredit;
        final String _mikomiCredit;
        final String _totalCredits;
        final List _subclassCredit = new ArrayList();

        public Student(
                final String homeRoomAtend,
                final String schregno,
                final String name,
                final String studyrecCreditZenseki,
                final String studyrecCreditUntilLastYear,
                final String abroadCreditUntilLastYear,
                final String recordDatCredit,
                final String abroadCreditThisYear,
                final String qualifiedCredit,
                final String mikomiCredit
        ) {
            _homeRoomAtend = homeRoomAtend;
            _schregno = schregno;
            _name = name;
            _studyrecCreditZenseki = studyrecCreditZenseki;
            _studyrecCreditUntilLastYear = studyrecCreditUntilLastYear;
            _abroadCreditUntilLastYear = abroadCreditUntilLastYear;
            _recordDatCredit = recordDatCredit;
            _abroadCreditThisYear = abroadCreditThisYear;
            _qualifiedCredit = qualifiedCredit;
            _mikomiCredit = mikomiCredit;
            _totalCredits =
                    add("0",
                            add(_studyrecCreditZenseki,
                                    add(_studyrecCreditUntilLastYear,
                                            add(_abroadCreditUntilLastYear,
                                                    add(_recordDatCredit,
                                                            add(_abroadCreditThisYear,
                                                                    add(_qualifiedCredit, _mikomiCredit)))))));

        }

        public String toString() {
            final String info = "No：" + _no
                             + " 出席番号：" + _homeRoomAtend
                             + " 学籍番号：" + _schregno
                             + " 氏名：" + _name
                             + " 単位１：" + add(_studyrecCreditUntilLastYear, _abroadCreditUntilLastYear)
                             + " 単位２：" + add(_recordDatCredit, _abroadCreditThisYear)
                             + " 単位３：" + _qualifiedCredit
                             + " 合計単位：" + _totalCredits + "\n";

            final StringBuffer stb = new StringBuffer();
            for (final Iterator it = _subclassCredit.iterator(); it.hasNext();) {
                final UnPassSubclass unPassSubclass = (UnPassSubclass) it.next();
                stb.append(unPassSubclass + "\n");
            }
            return info + stb.toString();
        }
    }

    /** 未履修科目 */
    private static class UnPassSubclass {
        private final String _name;
        private final String _credit;
        UnPassSubclass(final String name, final String credit) {
            _name = name;
            _credit = credit;
        }

        public String toString() {
            return "科目名：" + _name + " 単位：" + _credit;
        }
    }

    private static int toInt(final String s) {
        return NumberUtils.isDigits(s) ? Integer.parseInt(s) : 0;
    }

    private static String add(final String num1, final String num2) {
        return NumberUtils.isDigits(num1) || NumberUtils.isDigits(num2) ? String.valueOf(toInt(num1) + toInt(num2)) : null;
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 68937 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _gengou;
        final String _semester;
        final String _ctrlDate;
        final String _semesterName;
        final String _grade;
        final String _outputDiv1;
        final String _outputDiv2;
        final String _gradeInState;
        final String[] _hrClass;
        final String _dateString;
        private int _gradCredits = 0;
        private int _schooldiv = 0;
        final String _outputOrder; // 1:出力順、2:前年度履修単位順、3:今年度修得単位
        /** 名称マスタ（学校等） */
        private String _z010Name1; //学校
        private String _z010NameSpare1; //record_score_dat使用フラグ
        final String _useCurriculumcd;
        final String _useTestCountflg;
        final String _titlePass;
        final String _titleUnPass;
        boolean _hasRECORD_PROV_FLG_DAT;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameterValues("CLASS_SELECTED");
            _outputDiv1 = request.getParameter("OUTPUT_DIV1");
            _outputDiv2 = request.getParameter("OUTPUT_DIV2");
            _outputOrder = request.getParameter("SYUTURYOKUJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _gradeInState = getGradeInState(db2, _grade);

            if ("1".equals(request.getParameter("GRADUATE"))) {
                _titlePass = "卒業認定対象者一覧";
                _titleUnPass = "卒業認定見込みが立たない対象者一覧";
            } else {
                _titlePass = "単位認定対象者一覧";
                _titleUnPass = "単位認定見込みが立たない対象者一覧";
            }
            _dateString = getDate(db2);

            // DBより取得
            try {
                KNJSchoolMst knjSchoolMst = new KNJSchoolMst(db2, _year);
                _schooldiv = NumberUtils.isDigits(knjSchoolMst._schoolDiv) ? Integer.parseInt(knjSchoolMst._schoolDiv) : 0;
                _gradCredits = NumberUtils.isDigits(knjSchoolMst._gradCredits) ? Integer.parseInt(knjSchoolMst._gradCredits) : 0;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            _semesterName = getSemesterName(db2);
            setNameMst(db2);

            log.info("卒業単位=" + _gradCredits);

            _hasRECORD_PROV_FLG_DAT = KnjDbUtils.setTableColumnCheck(db2, "RECORD_PROV_FLG_DAT", null);
        }

        public String getDate(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        private String getSemesterName(final DB2UDB db2) {
            String rtn = "";

            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeInState(final DB2UDB db2, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT "
                + "    GRADE "
                + "FROM "
                + "    SCHREG_REGD_GDAT "
                + "WHERE "
                + "    YEAR = '" + _year + "' "
                + "    AND GRADE < '" + grade + "' "
                + "    AND SCHOOL_KIND IN (SELECT "
                + "          SCHOOL_KIND "
                + "      FROM "
                + "          SCHREG_REGD_GDAT "
                + "      WHERE "
                + "          YEAR = '" + _year + "' "
                + "          AND GRADE = '" + grade + "' "
                + "  ) "
                + "ORDER BY "
                + "    GRADE ";

            String comma = "";
            StringBuffer gradeInState = new StringBuffer();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    gradeInState.append(comma).append("'" + rs.getString("GRADE") + "'");
                    comma = ",";
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if ("".equals(comma)) {
                gradeInState.append("''");
            }
            //log.debug(" gradeInState = "  + gradeInState);
            return gradeInState.toString();
        }

        private void setNameMst(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                    _z010NameSpare1 = rs.getString("NAMESPARE1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学校=" + _z010Name1 + "、成績テーブル=" + (isUseRecordScoreDat() ? "RECORD_SCORE_DAT" : "RECORD_DAT"));
        }

        /**
         * record_score_dat使用か?。
         * @return is not nullならtrue
         */
        private boolean isUseRecordScoreDat() {
            return _z010NameSpare1 != null;
        }

        private boolean isGakuensei() {
            final int GAKUNENSEI = 0;
            return GAKUNENSEI == _schooldiv;
        }

        private boolean isTannisei() {
            final int TANISEI    = 1;
            return TANISEI == _schooldiv;
        }
    }
}
 // KNJD233V

// eof
