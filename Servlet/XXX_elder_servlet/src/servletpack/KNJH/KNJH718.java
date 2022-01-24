package servletpack.KNJH;

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
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJH718 {

    private static final Log log = LogFactory.getLog(KNJH718.class);

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
        //出力データ。校時→科目→施設（人数）
        final Map printMap = getPrintMap(db2);

        for (Iterator itPage = _param._facilityData.keySet().iterator(); itPage.hasNext();) {
            final String page = (String) itPage.next();
            final List facilityList = (List) _param._facilityData.get(page);
            setTitle(svf, facilityList);

            for (Iterator iterator = printMap.keySet().iterator(); iterator.hasNext();) {
                final String period = (String) iterator.next();
                final PeriodData periodData = (PeriodData) printMap.get(period);
                final int recordName = periodData._subclassMap.size() > 2 ? 3 : periodData._subclassMap.size() > 1 ? 2 : 1;

                //時限、配布、試験時間
                svf.VrsOut("PERIOD" + recordName, periodData._periodName);
                svf.VrsOut("DISTRI_TIME" + recordName, "問題配布 " + periodData._haifuTime);
                svf.VrsOut("EXAM_TIME" + recordName, periodData._startTime + "\uFF5E" + periodData._endTime);

                //科目
                int subclassCnt = 1;
                for (Iterator itSubclass = periodData._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
                    final String subclassKey = (String) itSubclass.next();
                    final SubclassData subclassData = (SubclassData) periodData._subclassMap.get(subclassKey);
                    final String nameField = recordName > 1 ? String.valueOf(recordName) + "_" : "";
                    svf.VrsOut("SUBCLASS_NAME" + nameField + subclassCnt, subclassData._subclassAbbv);

                    //施設(人数)
                    int rowCnt = 1;
                    for (Iterator itFacility = facilityList.iterator(); itFacility.hasNext();) {
                        final FacilityData facilityData = (FacilityData) itFacility.next();
                        if (subclassData._facilityCntMap.containsKey(facilityData._facCd)) {
                            final String numField = recordName + "_" + subclassCnt + "_" + rowCnt;
                            final String setCnt = (String) subclassData._facilityCntMap.get(facilityData._facCd);
                            svf.VrsOut("NUM" + numField, setCnt);

                            final String numTotalField = recordName + "_" + rowCnt;
                            final String setTotalCnt = (String) periodData._facilityTotalMap.get(facilityData._facCd);
                            svf.VrsOut("TOTAL_NUM" + numTotalField, "計 " + setTotalCnt);
                        }
                        rowCnt++;
                    }

                    subclassCnt++;
                }

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private void setTitle(final Vrw32alp svf, final List facilityList) {
        svf.VrSetForm("KNJH718.frm", 4);
        final AcademicTestMst academicTestMst = _param._academicTestMst;
        svf.VrsOut("TITLE", _param._ctrlYear + "年度　　　" + academicTestMst._testName + "　時間割");
        svf.VrsOut("DATE", "出力日：" + _param._ctrlDate.replace("-", "/"));
        svf.VrsOut("EXEC_DATE", KNJ_EditDate.h_format_SeirekiJP(academicTestMst._examDate) + "実施");
        svf.VrsOut("EXAM_DATE", academicTestMst.getMD());
        svf.VrsOut("EXAM_DAY", "(" + academicTestMst._examWeek + ")");

        //施設情報
        int rowCnt = 1;
        for (Iterator itFacility = facilityList.iterator(); itFacility.hasNext();) {
            final FacilityData facilityData = (FacilityData) itFacility.next();
            svf.VrsOut("FACULTYNAME" + rowCnt, facilityData._facilityName);
            svf.VrsOut("FACULTYABBV" + rowCnt, facilityData._facilityAbbv);
            rowCnt++;
        }
    }

    private Map getPrintMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map retMap = new TreeMap<String, PeriodData>();
        try {
            final String sql = getMainSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            PeriodData periodData = null;
            SubclassData subclassData = null;
            while (rs.next()) {
                final String periodId = rs.getString("PERIODID");
                final String periodName = rs.getString("PERIODNAME");
                final int startHour = rs.getInt("START_HOUR");
                final int startMinute = rs.getInt("START_MINUTE");
                final int endHour = rs.getInt("END_HOUR");
                final int endMinute = rs.getInt("END_MINUTE");
                final String bunriDiv = rs.getString("BUNRIDIV");
                final String classCd = rs.getString("CLASSCD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                final String facCd = rs.getString("FACCD");
                final String cnt = StringUtils.defaultString(rs.getString("CNT"), "");

                //校時データ
                if (retMap.containsKey(periodId)) {
                    periodData = (PeriodData) retMap.get(periodId);
                } else {
                    periodData = new PeriodData(periodId, periodName, startHour, startMinute, endHour, endMinute);
                }
                //科目データ
                final String subclassKey = bunriDiv + classCd + subclassCd;
                if (periodData._subclassMap.containsKey(subclassKey)) {
                    subclassData = (SubclassData) periodData._subclassMap.get(subclassKey);
                } else {
                    subclassData = new SubclassData(bunriDiv, classCd, subclassCd, subclassAbbv);
                }
                //施設毎のカウント
                periodData.setTotalCnt(facCd, cnt);
                subclassData._facilityCntMap.put(facCd, cnt);
                periodData._subclassMap.put(subclassKey, subclassData);

                retMap.put(periodId, periodData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getMainSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     ACT_SCH.TESTID, ");
        stb.append("     ACT_SCH.PERIODID, ");
        stb.append("     H321.ABBV2 AS PERIODNAME, ");
        stb.append("     ACT_SCH.START_HOUR, ");
        stb.append("     ACT_SCH.START_MINUTE, ");
        stb.append("     ACT_SCH.END_HOUR, ");
        stb.append("     ACT_SCH.END_MINUTE, ");
        stb.append("     ACT_SCH.BUNRIDIV, ");
        stb.append("     ACT_SCH.CLASSCD, ");
        stb.append("     ACT_SCH.SUBCLASSCD, ");
        stb.append("     ACT_SUB.SUBCLASSNAME, ");
        stb.append("     ACT_SUB.SUBCLASSABBV, ");
        stb.append("     ACT_FAC.FACCD, ");
        stb.append("     COUNT(ACT_FAC.SCHREGNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("     ACADEMICTEST_SCH_DAT ACT_SCH ");
        stb.append("     LEFT JOIN ACADEMICTEST_SUBCLASS_DAT ACT_SUB ");
        stb.append("           ON ACT_SUB.YEAR       = ACT_SCH.YEAR ");
        stb.append("          AND ACT_SUB.TESTDIV    = '" + _param._testDiv + "' ");
        stb.append("          AND ACT_SUB.BUNRIDIV   = ACT_SCH.BUNRIDIV ");
        stb.append("          AND ACT_SUB.CLASSCD    = ACT_SCH.CLASSCD ");
        stb.append("          AND ACT_SUB.SUBCLASSCD = ACT_SCH.SUBCLASSCD ");
        stb.append("     LEFT JOIN ACADEMICTEST_FAC_DAT ACT_FAC ");
        stb.append("           ON ACT_FAC.YEAR       = ACT_SCH.YEAR ");
        stb.append("          AND ACT_FAC.TESTID     = ACT_SCH.TESTID ");
        stb.append("          AND ACT_FAC.PERIODID   = ACT_SCH.PERIODID ");
        stb.append("          AND ACT_FAC.TESTID     = ACT_SCH.TESTID ");
        stb.append("          AND ACT_FAC.BUNRIDIV   = ACT_SCH.BUNRIDIV ");
        stb.append("          AND ACT_FAC.CLASSCD    = ACT_SCH.CLASSCD ");
        stb.append("          AND ACT_FAC.SUBCLASSCD = ACT_SCH.SUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST H321 ");
        stb.append("           ON H321.NAMECD1    = 'H321' ");
        stb.append("          AND H321.NAMECD2    = ACT_SCH.PERIODID ");
        stb.append(" WHERE ");
        stb.append("     ACT_SCH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND ACT_SCH.TESTID = '" + _param._testId + "' ");
        stb.append(" GROUP BY ");
        stb.append("     ACT_SCH.TESTID, ");
        stb.append("     ACT_SCH.PERIODID, ");
        stb.append("     H321.ABBV2, ");
        stb.append("     ACT_SCH.START_HOUR, ");
        stb.append("     ACT_SCH.START_MINUTE, ");
        stb.append("     ACT_SCH.END_HOUR, ");
        stb.append("     ACT_SCH.END_MINUTE, ");
        stb.append("     ACT_SCH.BUNRIDIV, ");
        stb.append("     ACT_SCH.CLASSCD, ");
        stb.append("     ACT_SCH.SUBCLASSCD, ");
        stb.append("     ACT_SUB.SUBCLASSNAME, ");
        stb.append("     ACT_SUB.SUBCLASSABBV, ");
        stb.append("     ACT_FAC.FACCD ");
        stb.append(" ), SUB_NAME AS ( ");
        stb.append(" SELECT ");
        stb.append("     SUB_T.NAMECD1, ");
        stb.append("     MAX(SUB_T.CLASSCD) AS CLASSCD, ");
        stb.append("     MAX(SUB_T.SUBCLASSCD) AS SUBCLASSCD, ");
        stb.append("     LISTAGG(SUB_T.SUBCLASSABBV, '・') WITHIN GROUP(order BY SUB_T.CLASSCD, SUB_T.SUBCLASSCD) AS SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("          'H322' AS NAMECD1, ");
        stb.append("          MAIN_T.CLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSABBV ");
        stb.append("      FROM ");
        stb.append("          MAIN_T ");
        stb.append("      WHERE ");
        stb.append("          MAIN_T.CLASSCD IN (SELECT NAME1 FROM NAME_MST H322 WHERE H322.NAMECD1 = 'H322') ");
        stb.append("      GROUP BY ");
        stb.append("          MAIN_T.CLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSABBV ");
        stb.append("     ) SUB_T ");
        stb.append(" GROUP BY ");
        stb.append("     SUB_T.NAMECD1 ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     SUB_T.NAMECD1, ");
        stb.append("     MAX(SUB_T.CLASSCD) AS CLASSCD, ");
        stb.append("     MAX(SUB_T.SUBCLASSCD) AS SUBCLASSCD, ");
        stb.append("     LISTAGG(SUB_T.SUBCLASSABBV, '・') WITHIN GROUP(order BY SUB_T.NAMECD1, SUB_T.SUBCLASSCD) AS SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("          'H323' AS NAMECD1, ");
        stb.append("          MAIN_T.CLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSABBV ");
        stb.append("      FROM ");
        stb.append("          MAIN_T ");
        stb.append("      WHERE ");
        stb.append("          MAIN_T.CLASSCD IN (SELECT NAME1 FROM NAME_MST H322 WHERE H322.NAMECD1 = 'H323') ");
        stb.append("      GROUP BY ");
        stb.append("          MAIN_T.CLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSCD, ");
        stb.append("          MAIN_T.SUBCLASSABBV ");
        stb.append("     ) SUB_T ");
        stb.append(" GROUP BY ");
        stb.append("     SUB_T.NAMECD1 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     MAIN_T.BUNRIDIV, ");
        stb.append("     MAX(SUB_NAME.CLASSCD) AS CLASSCD, ");
        stb.append("     MAX(SUB_NAME.SUBCLASSCD) AS SUBCLASSCD, ");
        stb.append("     MAX(SUB_NAME.SUBCLASSABBV) AS SUBCLASSABBV, ");
        stb.append("     MAIN_T.FACCD, ");
        stb.append("     VALUE(SUM(MAIN_T.CNT), 0) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append("     LEFT JOIN SUB_NAME ");
        stb.append("            ON SUB_NAME.NAMECD1 = 'H322' ");
        stb.append(" WHERE ");
        stb.append("     MAIN_T.CLASSCD IN (SELECT NAME1 FROM NAME_MST H322 WHERE H322.NAMECD1 = 'H322') ");
        stb.append(" GROUP BY ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     MAIN_T.BUNRIDIV, ");
        stb.append("     MAIN_T.FACCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     MAIN_T.BUNRIDIV, ");
        stb.append("     MAX(SUB_NAME.CLASSCD) AS CLASSCD, ");
        stb.append("     MAX(SUB_NAME.SUBCLASSCD) AS SUBCLASSCD, ");
        stb.append("     MAX(SUB_NAME.SUBCLASSABBV) AS SUBCLASSABBV, ");
        stb.append("     MAIN_T.FACCD, ");
        stb.append("     VALUE(SUM(MAIN_T.CNT), 0) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append("     LEFT JOIN SUB_NAME ");
        stb.append("            ON SUB_NAME.NAMECD1 = 'H323' ");
        stb.append(" WHERE ");
        stb.append("     MAIN_T.CLASSCD IN (SELECT NAME1 FROM NAME_MST H323 WHERE H323.NAMECD1 = 'H323') ");
        stb.append(" GROUP BY ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     MAIN_T.BUNRIDIV, ");
        stb.append("     MAIN_T.FACCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     '9' AS BUNRIDIV, ");
        stb.append("     MAIN_T.CLASSCD, ");
        stb.append("     MAX(MAIN_T.SUBCLASSCD) AS SUBCLASSCD, ");
        stb.append("     MAX(MAIN_T.SUBCLASSABBV) AS SUBCLASSABBV, ");
        stb.append("     MAIN_T.FACCD, ");
        stb.append("     VALUE(SUM(MAIN_T.CNT), 0) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" WHERE ");
        stb.append("     MAIN_T.CLASSCD IN (SELECT NAME1 FROM NAME_MST H324 WHERE H324.NAMECD1 = 'H324') ");
        stb.append(" GROUP BY ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     MAIN_T.CLASSCD, ");
        stb.append("     MAIN_T.FACCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.TESTID, ");
        stb.append("     MAIN_T.PERIODID, ");
        stb.append("     MAIN_T.PERIODNAME, ");
        stb.append("     MAIN_T.START_HOUR, ");
        stb.append("     MAIN_T.START_MINUTE, ");
        stb.append("     MAIN_T.END_HOUR, ");
        stb.append("     MAIN_T.END_MINUTE, ");
        stb.append("     MAIN_T.BUNRIDIV, ");
        stb.append("     MAIN_T.CLASSCD, ");
        stb.append("     MAIN_T.SUBCLASSCD, ");
        stb.append("     MAIN_T.SUBCLASSABBV, ");
        stb.append("     MAIN_T.FACCD, ");
        stb.append("     VALUE(MAIN_T.CNT, 0) AS CNT ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" WHERE ");
        stb.append("         MAIN_T.CLASSCD NOT IN (SELECT NAME1 FROM NAME_MST H322 WHERE H322.NAMECD1 = 'H322') ");
        stb.append("     AND MAIN_T.CLASSCD NOT IN (SELECT NAME1 FROM NAME_MST H323 WHERE H323.NAMECD1 = 'H323') ");
        stb.append("     AND MAIN_T.CLASSCD NOT IN (SELECT NAME1 FROM NAME_MST H324 WHERE H324.NAMECD1 = 'H324') ");
        stb.append(" ORDER BY ");
        stb.append("     PERIODID, ");
        stb.append("     BUNRIDIV, ");
        stb.append("     CLASSCD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     FACCD ");

        return stb.toString();
    }

    private class PeriodData {
        private final String _periodId;
        private final String _periodName;
        private final String _haifuTime;
        private final String _startTime;
        private final String _endTime;
        private final Map _subclassMap;
        private final Map _facilityTotalMap;
        public PeriodData(
                final String periodId,
                final String periodName,
                final int startHour,
                final int startMinute,
                final int endHour,
                final int endMinute
        ) {
            _periodId   = periodId;
            _periodName = periodName;
            _startTime  = String.valueOf(startHour) + ":" + (startMinute > 9 ? String.valueOf(startMinute) : "0" + String.valueOf(startMinute));
            _endTime    = String.valueOf(endHour) + ":" + (endMinute > 9 ? String.valueOf(endMinute) : "0" + String.valueOf(endMinute));
            final int totalTime = startHour * 60 + startMinute;
            final int haifuTime = totalTime - 5;
            final int totalHour = haifuTime / 60;
            final int totalMinute = (haifuTime - (totalHour * 60));
            _haifuTime  = String.valueOf(totalHour) + ":" + (totalMinute > 9 ? String.valueOf(totalMinute) : "0" + String.valueOf(totalMinute));
            _subclassMap = new TreeMap<String, SubclassData>();
            _facilityTotalMap = new TreeMap<String, String>();
        }
        public void setTotalCnt(final String facCd, final String cnt) {
            if ("".equals(cnt)) {
                return;
            }
            int setCnt = Integer.parseInt(cnt);
            if (_facilityTotalMap.containsKey(facCd)) {
                final String totalCnt = (String) _facilityTotalMap.get(facCd);
                setCnt += Integer.parseInt(totalCnt);
            }
            _facilityTotalMap.put(facCd, String.valueOf(setCnt));
        }
    }

    private class SubclassData {
        private final String _bunriDiv;
        private final String _classCd;
        private final String _subclassCd;
        private final String _subclassAbbv;
        private final Map _facilityCntMap;
        public SubclassData(
                final String bunriDiv,
                final String classCd,
                final String subclassCd,
                final String subclassAbbv
        ) {
            _bunriDiv       = bunriDiv;
            _classCd        = classCd;
            _subclassCd     = subclassCd;
            _subclassAbbv   = subclassAbbv;
            _facilityCntMap = new TreeMap<String, String>();
        }
    }

    private class FacilityData {
        private final String _testId;
        private final String _facCd;
        private final String _facilityName;
        private final String _facilityAbbv;
        public FacilityData(
                final String testId,
                final String facCd,
                final String facilityName,
                final String facilityAbbv
        ) {
            _testId       = testId;
            _facCd        = facCd;
            _facilityName = facilityName;
            _facilityAbbv = facilityAbbv;
        }
    }

    private class AcademicTestMst {
        private final String _testDiv;
        private final String _testId;
        private final String _examDate;
        private final String _examWeek;
        private final String _testName;
        private final String _testAbbv;
        public AcademicTestMst(
                final String testDiv,
                final String testId,
                final String examDate,
                final String examWeek,
                final String testName,
                final String testAbbv
        ) {
            _testDiv  = testDiv;
            _testId   = testId;
            _examDate = examDate;
            _examWeek = examWeek;
            _testName = testName;
            _testAbbv = testAbbv;
        }
        public String getMD() {
            final String[] dateArray = _examDate.split("-");
            return Integer.parseInt(dateArray[1]) + "/" + Integer.parseInt(dateArray[2]);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _testDiv;
        private final String _testId;
        private final AcademicTestMst _academicTestMst;
        private final Map _facilityData;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear     = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate     = request.getParameter("CTRL_DATE");

            final String[] divId = request.getParameter("TESTID").split("-");
            _testDiv  = divId[0];
            _testId   = divId[1];
            _academicTestMst = getAcademicTestMst(db2);
            _facilityData = getFacilityData(db2);
        }

        private AcademicTestMst getAcademicTestMst(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTID, ");
            stb.append("     EXAM_DATE, ");
            stb.append("     TESTNAME, ");
            stb.append("     TESTNAMEABBV ");
            stb.append(" FROM ");
            stb.append("     ACADEMICTEST_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND TESTID = '" + _testId + "' ");

            AcademicTestMst retObj = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String testDiv  = rs.getString("TESTDIV");
                    final String testId   = rs.getString("TESTID");
                    final String examDate = rs.getString("EXAM_DATE");
                    final String testName = rs.getString("TESTNAME");
                    final String testAbbv = rs.getString("TESTNAMEABBV");
                    final String examWeek = KNJ_EditDate.h_format_W(examDate);
                    retObj = new AcademicTestMst(testDiv, testId, examDate, examWeek, testName, testAbbv);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retObj;
        }

        private Map getFacilityData(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     ACT_STF.TESTID, ");
            stb.append("     ACT_STF.FACCD, ");
            stb.append("     FAC_M.FACILITYNAME, ");
            stb.append("     FAC_M.FACILITYABBV ");
            stb.append(" FROM ");
            stb.append("     ACADEMICTEST_STF_DAT ACT_STF ");
            stb.append("     LEFT JOIN FACILITY_MST FAC_M ");
            stb.append("          ON FAC_M.FACCD = ACT_STF.FACCD ");
            stb.append(" WHERE ");
            stb.append("     ACT_STF.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND ACT_STF.TESTID = '" + _testId + "' ");
            stb.append(" ORDER BY ");
            stb.append("     FACCD ");

            final Map<String, List> retMap = new TreeMap<String, List>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                List facList = null;
                final int maxCol = 11;
                int colCnt = 1;
                int pageCnt = 1;
                while (rs.next()) {
                    final String testId   = rs.getString("TESTID");
                    final String facCd = rs.getString("FACCD");
                    final String facilityName = rs.getString("FACILITYNAME");
                    final String facilityAbbv = rs.getString("FACILITYABBV");
                    final FacilityData facilityData = new FacilityData(testId, facCd, facilityName, facilityAbbv);
                    if (colCnt > maxCol) {
                        pageCnt++;
                        colCnt = 1;
                    }
                    if (!retMap.containsKey(String.valueOf(pageCnt))) {
                        facList = new ArrayList<PeriodData>();
                    } else {
                        facList = retMap.get(String.valueOf(pageCnt));
                    }
                    facList.add(facilityData);
                    retMap.put(String.valueOf(pageCnt), facList);
                    colCnt++;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
    }
}

// eof
