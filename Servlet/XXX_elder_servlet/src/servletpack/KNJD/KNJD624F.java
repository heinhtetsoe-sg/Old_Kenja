/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: a0746b6b3f48b2866d8a3eb2f29b66bacd99c1d7 $
 *
 * 作成日: 2018/10/09
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD624F {

    private static final Log log = LogFactory.getLog(KNJD624F.class);

    private boolean _hasData;

    private static final String SCHOOLKIND_J = "J";
    private static final String SCHOOLKIND_H = "H";

    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        int Col555555 = 18;
        int Col999999 = 19;
        if (SCHOOLKIND_J.equals(_param._schoolKind)) {
            svf.VrSetForm("KNJD624F_1.frm", 1);
        } else {
            svf.VrSetForm("KNJD624F_2.frm", 1);
            Col999999 = 21;
        }
        final List printHrClassList = getHrClassList(db2);
        for (Iterator itHrClass = printHrClassList.iterator(); itHrClass.hasNext();) {
            final HrClass hrClass = (HrClass) itHrClass.next();
            
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　" + hrClass._hrName + "　" + _param._testName + "　得点分布表");
            int colCnt = 1;
            for (Iterator itSubclass = hrClass._subclassList.iterator(); itSubclass.hasNext();) {
                final Subclass subclass = (Subclass) itSubclass.next();
                if (SUBCLASS5.equals(subclass._subclasscd)) {
                    colCnt = Col555555;
                } else if (SUBCLASS9.equals(subclass._subclasscd)) {
                    colCnt = Col999999;
                }
                final String subclassField = KNJ_EditEdit.getMS932ByteLength(subclass._subclassabbv) > 12 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(subclass._subclassabbv) > 6 ? "2_1" : "1";
                svf.VrsOutn("SUBCLASS_NAME" + subclassField, colCnt, subclass._subclassabbv);
                if (!SUBCLASS9.equals(subclass._subclasscd)) {
                    svf.VrsOutn("PERFECT", colCnt, StringUtils.defaultString(subclass._perfect));
                } else {
                    svf.VrsOutn("PERFECT", colCnt, "100");
                }
                if (!SUBCLASS5.equals(subclass._subclasscd) && !SUBCLASS9.equals(subclass._subclasscd)) {
                    svf.VrsOutn("AVERAGE", colCnt, StringUtils.defaultString(subclass._classAvg));
                }
                for (Iterator itScoreRange = subclass._scoreMap.keySet().iterator(); itScoreRange.hasNext();) {
                    final String key = (String) itScoreRange.next();
                    final Bunpu bunpu = (Bunpu) subclass._scoreMap.get(key);
                    svf.VrsOutn("NUM" + key, colCnt, String.valueOf(bunpu._cnt));
                }
                colCnt++;
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getHrClassList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getHrClassSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final HrClass hrClassObj = new HrClass(grade, hrClass, hrName);
                hrClassObj.setSubclassList(db2);
                retList.add(hrClassObj);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getHrClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT REGDH ");
        stb.append(" WHERE ");
        stb.append("     REGDH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGDH.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGDH.GRADE = '" + _param._grade + "' ");
        stb.append("     AND REGDH.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final List _subclassList;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _subclassList = new ArrayList();
        }
        public void setSubclassList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sqlSubclass = getSubclassSql();
                ps = db2.prepareStatement(sqlSubclass);
                rs = ps.executeQuery();

                Subclass subclass = null;
                String befSubclass = "";
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final String classAvg = rs.getString("CLASS_AVG");
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    final String perfect = StringUtils.defaultString(rs.getString("PERFECT"), "100");
                    if (!befSubclass.equals(subclasscd)) {
                        subclass = new Subclass(subclasscd, subclassabbv, classAvg, perfect);
                        _subclassList.add(subclass);
                    }
                    subclass.setScoreMap(score);

                    befSubclass = subclasscd;
                }
                ps.close();
                rs.close();

                //5科、9科
                final String inState = SCHOOLKIND_J.equals(_param._schoolKind) ? "'" + SUBCLASS5 + "', '" + SUBCLASS9 + "'" : "'" + SUBCLASS9 + "'";
                final String sqlSubclassAll = getSubclassSqlAll(inState);
                ps = db2.prepareStatement(sqlSubclassAll);
                rs = ps.executeQuery();

                Subclass subclassAll = null;
                befSubclass = "";
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final String classAvg = rs.getString("CLASS_AVG");
                    final String score = rs.getString("SCORE");
                    final String perfect = StringUtils.defaultString(rs.getString("PERFECT"));
                    if (!befSubclass.equals(subclasscd)) {
                        subclassAll = new Subclass(subclasscd, subclassabbv, classAvg, perfect);
                        _subclassList.add(subclassAll);
                    }
                    subclassAll.setScoreMap(score);

                    befSubclass = subclasscd;
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 科目取得SQL */
        private String getSubclassSql() {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     REGD.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     AND REGD.HR_CLASS = '" + _hrClass + "' ");
            stb.append(" ), CLASS_AVG AS ( ");
            // クラス平均
            stb.append("     SELECT ");
            stb.append("         SEMESTER, ");
            stb.append("         TESTKINDCD, ");
            stb.append("         TESTITEMCD, ");
            stb.append("         SCORE_DIV, ");
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
            stb.append("         SUBCLASSCD, ");
            stb.append("         AVG ");
            stb.append("     FROM ");
            stb.append("         RECORD_AVERAGE_SDIV_DAT ");
            stb.append("     WHERE ");
            stb.append("             YEAR = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testkindCd + "' ");
            stb.append("         AND AVG_DIV = '2' ");
            stb.append("         AND GRADE = '" + _grade + "' ");
            stb.append("         AND HR_CLASS = '" + _hrClass + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SDIV.SEMESTER, ");
            stb.append("     SDIV.SCHREGNO, ");
            stb.append("     SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSABBV, ");
            stb.append("     DECIMAL(ROUND(FLOAT(CLSS.AVG)*10,0)/10,5,1) AS CLASS_AVG, ");
            stb.append("     SDIV.SCORE, ");
            stb.append("     PERF.PERFECT ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("     INNER JOIN SCH_T ON SDIV.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN SUBCLASS_MST SUBM ON SDIV.CLASSCD   = SUBM.CLASSCD ");
            stb.append("                             AND SDIV.SCHOOL_KIND   = SUBM.SCHOOL_KIND ");
            stb.append("                             AND SDIV.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("                             AND SDIV.SUBCLASSCD    = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN CLASS_AVG CLSS ON SDIV.SEMESTER      = CLSS.SEMESTER ");
            stb.append("                             AND SDIV.TESTKINDCD    = CLSS.TESTKINDCD ");
            stb.append("                             AND SDIV.TESTITEMCD    = CLSS.TESTITEMCD ");
            stb.append("                             AND SDIV.SCORE_DIV     = CLSS.SCORE_DIV ");
            stb.append("                             AND SDIV.CLASSCD       = CLSS.CLASSCD ");
            stb.append("                             AND SDIV.SCHOOL_KIND   = CLSS.SCHOOL_KIND ");
            stb.append("                             AND SDIV.CURRICULUM_CD = CLSS.CURRICULUM_CD ");
            stb.append("                             AND SDIV.SUBCLASSCD    = CLSS.SUBCLASSCD ");
            stb.append("     LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("                             AND PERF.SEMESTER      = SDIV.SEMESTER ");
            stb.append("                             AND PERF.TESTKINDCD    = SDIV.TESTKINDCD ");
            stb.append("                             AND PERF.TESTITEMCD    = SDIV.TESTITEMCD ");
            stb.append("                             AND PERF.CLASSCD       = SDIV.CLASSCD ");
            stb.append("                             AND PERF.SCHOOL_KIND   = SDIV.SCHOOL_KIND ");
            stb.append("                             AND PERF.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
            stb.append("                             AND PERF.SUBCLASSCD    = SDIV.SUBCLASSCD ");
            stb.append("                             AND PERF.GRADE = CASE WHEN PERF.DIV = '01' THEN '00' ELSE SCH_T.GRADE END ");
            stb.append("                             AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE SCH_T.COURSECD || SCH_T.MAJORCD || SCH_T.COURSECODE END ");
            stb.append(" WHERE ");
            stb.append("     SDIV.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _param._testkindCd + "' ");
            stb.append("     AND SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD NOT IN ( ");
            stb.append("            SELECT ");
            stb.append("                COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append("            FROM ");
            stb.append("                SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("            WHERE ");
            stb.append("                YEAR = '" + _param._ctrlYear + "' ) ");
            stb.append(" ORDER BY ");
            stb.append("     SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD, ");
            stb.append("     SDIV.SCORE DESC ");

            return stb.toString();
        }

        /** 科目取得SQL */
        private String getSubclassSqlAll(final String inState) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     REGD.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     AND REGD.HR_CLASS = '" + _hrClass + "' ");
            stb.append(" ), CLASS_AVG AS ( ");
            // クラス平均
            stb.append("     SELECT ");
            stb.append("         SEMESTER, ");
            stb.append("         TESTKINDCD, ");
            stb.append("         TESTITEMCD, ");
            stb.append("         SCORE_DIV, ");
            stb.append("         CLASSCD, ");
            stb.append("         SCHOOL_KIND, ");
            stb.append("         CURRICULUM_CD, ");
            stb.append("         SUBCLASSCD, ");
            stb.append("         AVG ");
            stb.append("     FROM ");
            stb.append("         RECORD_AVERAGE_SDIV_DAT ");
            stb.append("     WHERE ");
            stb.append("             YEAR = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testkindCd + "' ");
            stb.append("         AND AVG_DIV = '2' ");
            stb.append("         AND GRADE = '" + _grade + "' ");
            stb.append("         AND HR_CLASS = '" + _hrClass + "' ");
            stb.append(" ), PERFECT AS ( ");
            stb.append(" SELECT ");
            stb.append("     CASE WHEN REC_G.GROUP_DIV = '5' ");
            stb.append("          THEN '" + SUBCLASS5 + "' ");
            stb.append("          ELSE '" + SUBCLASS9 + "' ");
            stb.append("     END AS SUBCLASSCD, ");
            stb.append("     REC_G.GRADE, ");
            stb.append("     REC_G.COURSECD, ");
            stb.append("     REC_G.MAJORCD, ");
            stb.append("     REC_G.COURSECODE, ");
            stb.append("     SUM(VALUE(PERF.PERFECT, 100)) AS PERFECT ");
            stb.append(" FROM ");
            stb.append("     REC_SUBCLASS_GROUP_DAT REC_G ");
            stb.append("     LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = REC_G.YEAR ");
            stb.append("                             AND PERF.SEMESTER || PERF.TESTKINDCD || PERF.TESTITEMCD = '" + _param._testkindCd.substring(0, 5) + "' ");
            stb.append("                             AND PERF.CLASSCD       = REC_G.CLASSCD ");
            stb.append("                             AND PERF.SCHOOL_KIND   = REC_G.SCHOOL_KIND ");
            stb.append("                             AND PERF.CURRICULUM_CD = REC_G.CURRICULUM_CD ");
            stb.append("                             AND PERF.SUBCLASSCD    = REC_G.SUBCLASSCD ");
            stb.append("                             AND PERF.GRADE = CASE WHEN PERF.DIV = '01' THEN '00' ELSE REC_G.GRADE END ");
            stb.append("                             AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE REC_G.COURSECD || REC_G.MAJORCD || REC_G.COURSECODE END ");
            stb.append(" WHERE ");
            stb.append("     REC_G.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REC_G.GROUP_DIV IN ('5', '9') ");
            stb.append(" GROUP BY ");
            stb.append("     REC_G.GROUP_DIV, ");
            stb.append("     REC_G.GRADE, ");
            stb.append("     REC_G.COURSECD, ");
            stb.append("     REC_G.MAJORCD, ");
            stb.append("     REC_G.COURSECODE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SDIV.SEMESTER, ");
            stb.append("     SDIV.SCHREGNO, ");
            stb.append("     SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CASE WHEN SDIV.SUBCLASSCD = '" + SUBCLASS5 + "' ");
            stb.append("          THEN '５教科' ");
            stb.append("          ELSE '平均' ");
            stb.append("     END AS SUBCLASSABBV, ");
            stb.append("     DECIMAL(ROUND(FLOAT(CLSS.AVG)*10,0)/10,5,1) AS CLASS_AVG, ");
            stb.append("     CAST(TRUNCATE(SDIV.AVG, 0) AS INT) AS SCORE, ");
            stb.append("     PERFECT.PERFECT ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("     INNER JOIN SCH_T ON SDIV.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     LEFT JOIN CLASS_AVG CLSS ON SDIV.SEMESTER      = CLSS.SEMESTER ");
            stb.append("                             AND SDIV.TESTKINDCD    = CLSS.TESTKINDCD ");
            stb.append("                             AND SDIV.TESTITEMCD    = CLSS.TESTITEMCD ");
            stb.append("                             AND SDIV.SCORE_DIV     = CLSS.SCORE_DIV ");
            stb.append("                             AND SDIV.CLASSCD       = CLSS.CLASSCD ");
            stb.append("                             AND SDIV.SCHOOL_KIND   = CLSS.SCHOOL_KIND ");
            stb.append("                             AND SDIV.CURRICULUM_CD = CLSS.CURRICULUM_CD ");
            stb.append("                             AND SDIV.SUBCLASSCD    = CLSS.SUBCLASSCD ");
            stb.append("     LEFT JOIN PERFECT ON PERFECT.SUBCLASSCD = SDIV.SUBCLASSCD ");
            stb.append("                             AND PERFECT.GRADE = SCH_T.GRADE ");
            stb.append("                             AND PERFECT.COURSECD || PERFECT.MAJORCD || PERFECT.COURSECODE = SCH_T.COURSECD || SCH_T.MAJORCD || SCH_T.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     SDIV.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _param._testkindCd + "' ");
            stb.append("     AND SDIV.SUBCLASSCD IN (" + inState + ") ");
            stb.append(" ORDER BY ");
            stb.append("     SDIV.SUBCLASSCD, ");
            stb.append("     SDIV.SCORE DESC ");

            return stb.toString();
        }

    }

    private class Subclass {
        final String _subclasscd;
        final String _subclassabbv;
        final String _classAvg;
        final String _perfect;
        final Map _scoreMap;
        public Subclass(
                final String subclasscd,
                final String subclassabbv,
                final String classAvg,
                final String perfect
        ) {
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _classAvg = classAvg;
            _perfect = perfect;
            _scoreMap = new TreeMap();
            _scoreMap.put("1", new Bunpu("100", "100"));
            _scoreMap.put("2", new Bunpu("90", "99"));
            _scoreMap.put("3", new Bunpu("80", "89"));
            _scoreMap.put("4", new Bunpu("70", "79"));
            _scoreMap.put("5", new Bunpu("60", "69"));
            _scoreMap.put("6", new Bunpu("50", "59"));
            _scoreMap.put("7", new Bunpu("40", "49"));
            _scoreMap.put("8", new Bunpu("30", "39"));
            _scoreMap.put("9", new Bunpu("20", "29"));
            _scoreMap.put("10", new Bunpu("0", "19"));
        }
        public void setScoreMap(final String score) {
            if (null == score || "".equals(score)) {
                return;
            }
            final int checkScore = Integer.parseInt(score);
            for (Iterator itPerfect = _scoreMap.keySet().iterator(); itPerfect.hasNext();) {
                final String key = (String) itPerfect.next();
                final Bunpu bunpu = (Bunpu) _scoreMap.get(key);
                if (bunpu.inRange(checkScore)) {
                    bunpu._cnt++;
                }
            }
        }
    }

    private class Bunpu {
        final String _lowScore;
        final String _highScore;
        int _cnt;
        public Bunpu(
                final String lowScore,
                final String highScore
        ) {
            _lowScore = lowScore;
            _highScore = highScore;
            _cnt = 0;
        }
        public boolean inRange(int checkScore) {
            if (Integer.parseInt(_lowScore) <= checkScore && Integer.parseInt(_highScore) >= checkScore) {
                return true;
            }
            return false;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63336 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _testkindCd;
        final String _grade;
        final String[] _categorySelected;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _prgid;
        final String _usecurriculumcd;
        final String _testName;
        final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _testkindCd = request.getParameter("TESTKIND_CD");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _prgid = request.getParameter("PRGID");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _testName = getTestName(db2);
            _schoolKind = getSchoolKind(db2);
        }

        private String getTestName(final DB2UDB db2) {
            String retTestName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.TESTITEMNAME ");
                stb.append(" FROM ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR     = '" + _ctrlYear + "' ");
                stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testkindCd + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retTestName = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retTestName;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String retSchoolKind = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOL_KIND ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR     = '" + _ctrlYear + "' ");
                stb.append("     AND GRADE = '" + _grade + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolKind;
        }

    }
}

// eof
