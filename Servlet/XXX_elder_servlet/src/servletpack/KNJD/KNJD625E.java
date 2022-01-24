/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: a8c64643f35132d94e91e70bf708392ede7e2080 $
 *
 * 作成日: 2020/04/24
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD625E {

    private static final Log log = LogFactory.getLog(KNJD625E.class);

    private boolean _hasData;
    private final String MOCK_NOBIRITSU = "999999999";
    private final String NOBIRITSU_SUBCLS = "99-Z-9-999999";
    private final String KESSEKI = "97";
    private final String SOUKEI = "98";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map<String, StaffData> printStaffMap = getStaffMap(db2);
        for (Iterator itStaff = printStaffMap.keySet().iterator(); itStaff.hasNext();) {
            svf.VrSetForm("KNJD625E_1.frm", 4);

            final String staffCd = (String) itStaff.next();
            final StaffData staffData = (StaffData) printStaffMap.get(staffCd);

            final String nameField = KNJ_EditEdit.getMS932ByteLength(staffData._staffName) > 20 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, staffData._staffName);
            svf.VrsOut("STAFF_CD", staffData._staffCd);
            final String fName = "T" + staffData._staffCd;
            final String fpath = _param.getImageFilePath("image", fName, "jpg");
            if (!"".equals(StringUtils.defaultString(fpath))) {
                svf.VrsOut("PIC", fpath);
            }
            //担当教科
            int tcCnt = 1;
            for (Iterator itTitleClass = staffData._titleClassMap.keySet().iterator(); itTitleClass.hasNext();) {
                final String titleKey = (String) itTitleClass.next();
                final String className = staffData._titleClassMap.get(titleKey);
                final String cNameField = KNJ_EditEdit.getMS932ByteLength(className) > 20 ? "2" : "1";
                svf.VrsOutn("CLASS_NAME" + cNameField, tcCnt, className);
                tcCnt++;
            }
            svf.VrsOut("CLASS_TOTAL_SCORE", String.valueOf(staffData._totalScore));
            svf.VrsOut("CLASS_AVG_SCORE", staffData._avgScore.toString());

            //担当クラス
            int hrCnt = 1;
            int hrLineCnt = 1;
            for (Iterator itHr = staffData._hrClassMap.keySet().iterator(); itHr.hasNext();) {
                final String gradeHr = (String) itHr.next();
                final HrClass hrClass = staffData._hrClassMap.get(gradeHr);
                if (hrCnt > 6) {
                    hrLineCnt++;
                    hrCnt = 1;
                }
                svf.VrsOutn("HR_NAME" + hrLineCnt + "_1", hrCnt, hrClass._hrAbbv);
                svf.VrsOutn("CLASS_NUM" + hrLineCnt + "_1", hrCnt, String.valueOf(hrClass._score));
                svf.VrsOutn("CLASS_NUM" + hrLineCnt + "_2", hrCnt, String.valueOf(hrClass._nobiRitsu));
                svf.VrsOutn("CLASS_NUM" + hrLineCnt + "_3", hrCnt, String.valueOf(hrClass._totalScore));
                hrCnt++;
            }

            //年組
            int pageCnt = 1;
            int maxPageCnt = 3;
            for (Iterator itHr = staffData._hrClassMap.keySet().iterator(); itHr.hasNext();) {
                final String gradeHr = (String) itHr.next();
                final HrClass hrClass = staffData._hrClassMap.get(gradeHr);
                //模試グループ
                for (Iterator itMockGrp = hrClass._grpMockMap.keySet().iterator(); itMockGrp.hasNext();) {
                    if (pageCnt > maxPageCnt) {
                        svf.VrSetForm("KNJD625E_2.frm", 4);
                        pageCnt = 1;
                        maxPageCnt = 4;
                    }
                    final String mockGrp = (String) itMockGrp.next();
                    final GrpMock grpMock = hrClass._grpMockMap.get(mockGrp);
                    //模試
                    int recCnt = 1;
                    for (Iterator itMock = grpMock._mockDataMap.keySet().iterator(); itMock.hasNext();) {
                        final String mockCd = (String) itMock.next();
                        final MockData mockData = grpMock._mockDataMap.get(mockCd);
                        //模試科目

                        for (Iterator itMocSubCls = mockData._mockSubclassDataMap.keySet().iterator(); itMocSubCls.hasNext();) {
                            final String mockSubCls = (String) itMocSubCls.next();
                            final MockSubclassData mockSubclassData = mockData._mockSubclassDataMap.get(mockSubCls);
                            if (recCnt > 5 && !mockSubCls.equals(NOBIRITSU_SUBCLS)) {
                                continue;
                            }
                            //分布
                            int rangeCnt = 1;
                            if (mockSubCls.equals(NOBIRITSU_SUBCLS)) {
                                final String setNobiritsuName = "第1回から第" + (recCnt - 1) + "回の伸び率";
                                final String mockNameField = KNJ_EditEdit.getMS932ByteLength(setNobiritsuName) > 14 ? "2" : "1";
                                svf.VrsOut("MOCK_NAME3_" + mockNameField, setNobiritsuName);
                                svf.VrsOut("TEST_SUBCLASS_NAME3_1", hrClass._hrAbbv);
                                //範囲
                                for (Iterator itRange = mockSubclassData._rangeDataMap.keySet().iterator(); itRange.hasNext();) {
                                    final String rangeKey = (String) itRange.next();
                                    final RangeData rangeData = mockSubclassData._rangeDataMap.get(rangeKey);
                                    if (SOUKEI.equals(rangeKey)) {
                                        svf.VrsOutn("TEST_NUM_NAME3", rangeCnt, String.valueOf(rangeData._totalScore));
                                    } else {
                                        svf.VrsOutn("TEST_NUM_NAME3", rangeCnt, String.valueOf(rangeData.getScore()));
                                    }
                                    svf.VrsOutn("TEST_NUM3_2", rangeCnt, String.valueOf(rangeData.getCnt()));
                                    rangeCnt++;
                                }
                            } else {
                                final String mockNameField = KNJ_EditEdit.getMS932ByteLength(mockData._mockName) > 14 ? "2" : "1";
                                svf.VrsOut("MOCK_NAME1_" + mockNameField, mockData._mockName);
                                svf.VrsOut("HR_NAME_ABBV1_1", hrClass._hrAbbv);
                                svf.VrsOut("TEST_SUBCLASS_NAME1_1", mockSubclassData._subclassAbbv);
                                //範囲
                                for (Iterator itRange = mockSubclassData._rangeDataMap.keySet().iterator(); itRange.hasNext();) {
                                    final String rangeKey = (String) itRange.next();
                                    final RangeData rangeData = mockSubclassData._rangeDataMap.get(rangeKey);
                                    if (SOUKEI.equals(rangeKey)) {
                                        svf.VrsOutn("TEST_NUM_NAME1", rangeCnt, String.valueOf(rangeData._totalScore));
                                        svf.VrsOut("TEST_AVE1", rangeData._avg.toString());
                                    } else {
                                        svf.VrsOutn("TEST_NUM_NAME1", rangeCnt, String.valueOf(rangeData.getScore()));
                                    }
                                    svf.VrsOutn("TEST_NUM1_2", rangeCnt, String.valueOf(rangeData.getCnt()));
                                    rangeCnt++;
                                }
                            }

                            recCnt++;
                            svf.VrEndRecord();
                        }
                    }
                    for (int i = recCnt; i <= 6; i++) {
                        svf.VrsOut("BLANK", "1");
                        svf.VrEndRecord();
                    }
                    pageCnt++;
                }
            }
            _hasData = true;
            if (pageCnt == 1) {
                svf.VrsOut("BLANK", "1");
                svf.VrEndRecord();
            }
        }
    }

    private Map getStaffMap(final DB2UDB db2) throws SQLException {
        final Map<String, StaffData> stfMap = new TreeMap<String, StaffData>();

        //職員データ
        PreparedStatement psStf = null;
        ResultSet rsStf = null;
        try {
            final String stfSql = getStfSql();
            log.debug(" sql =" + stfSql);
            psStf = db2.prepareStatement(stfSql);
            rsStf = psStf.executeQuery();

            while (rsStf.next()) {
                final String staffCd = rsStf.getString("STAFFCD");
                final String staffName = rsStf.getString("STAFFNAME");

                final StaffData staffData = new StaffData(staffCd, staffName);
                stfMap.put(staffCd, staffData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, psStf, rsStf);
            db2.commit();
        }

        //担当教科
        PreparedStatement psTitleClass = null;
        ResultSet rsTitleClass = null;
        try {
            final String titleClassSql = getTitleClassSql();
            log.debug(" sql =" + titleClassSql);
            psTitleClass = db2.prepareStatement(titleClassSql);
            rsTitleClass = psTitleClass.executeQuery();

            while (rsTitleClass.next()) {
                final String staffCd    = rsTitleClass.getString("STAFFCD");
                final String abbv2      = rsTitleClass.getString("ABBV2");
                final String schoolKind = rsTitleClass.getString("SCHOOL_KIND");
                final String classCd    = rsTitleClass.getString("CLASSCD");
                final String className  = rsTitleClass.getString("CLASSNAME");

                if (stfMap.containsKey(staffCd)) {
                    StaffData staffData = (StaffData) stfMap.get(staffCd);
                    staffData._titleClassMap.put(abbv2 + '-' + schoolKind + '-' + classCd, className);
                }
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, psTitleClass, rsTitleClass);
            db2.commit();
        }

        //担当クラス
        PreparedStatement psSclKindHr = null;
        ResultSet rsSclKindHr = null;
        try {
            final String sclKindHrSql = getSclKindHr();
            log.debug(" sql =" + sclKindHrSql);
            psSclKindHr = db2.prepareStatement(sclKindHrSql);
            rsSclKindHr = psSclKindHr.executeQuery();
            while (rsSclKindHr.next()) {
                final String staffCd    = rsSclKindHr.getString("STAFFCD");
                final String grade      = rsSclKindHr.getString("TRGTGRADE");
                final String hrClass    = rsSclKindHr.getString("TRGTCLASS");
                final String hrAbbv     = rsSclKindHr.getString("HR_NAMEABBV");
                final String schoolKind = rsSclKindHr.getString("SCHOOL_KIND");

                final HrClass hrClassObj = new HrClass(grade, hrClass, hrAbbv, schoolKind);
                if (stfMap.containsKey(staffCd)) {
                    StaffData staffData = (StaffData) stfMap.get(staffCd);
                    final String key = grade + '-' + hrClass;
                    staffData._hrClassMap.put(key, hrClassObj);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, psSclKindHr, rsSclKindHr);
            db2.commit();
        }

        for (Iterator itStaff = stfMap.keySet().iterator(); itStaff.hasNext();) {
            final String staffCd = (String) itStaff.next();
            final StaffData staffData = stfMap.get(staffCd);
            staffData.setMockData(db2);
        }

        return stfMap;
    }

    private String getStfSql() {
        final StringBuffer stb = new StringBuffer();

        final String staff = SQLUtils.whereIn(true, _param._categorySelected);

        stb.append(" SELECT ");
        stb.append("     STF.STAFFCD, ");
        stb.append("     STF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     STAFF_MST STF ");
        stb.append(" WHERE ");
        stb.append("     STAFFCD IN " + staff + " ");
        stb.append(" ORDER BY ");
        stb.append("     STF.STAFFCD ");
        return stb.toString();
    }

    private class StaffData {
        final String _staffCd;
        final String _staffName;
        final Map<String, String> _titleClassMap;
        int _totalScore;
        final Map<String, HrClass> _totalCntMap;
        BigDecimal _avgScore;
        final Map<String, HrClass> _hrClassMap;
        public StaffData(
                final String staffCd,
                final String staffName
        ) {
            _staffCd = staffCd;
            _staffName = staffName;
            _titleClassMap = new TreeMap<String, String>();
            _totalCntMap = new HashMap<String, HrClass>();
            _avgScore = new BigDecimal(0.0);
            _hrClassMap = new TreeMap<String, HrClass>();
        }

        public Map getNobiritsuMap() {

            final Map retMap = new TreeMap<String, RangeData>();
            retMap.put("01", new RangeData("20以上", 4));
            retMap.put("02", new RangeData("15-20", 3));
            retMap.put("03", new RangeData("10-15", 2));
            retMap.put("04", new RangeData("5-10", 1));
            retMap.put("05", new RangeData("0-5", 0));
            retMap.put("06", new RangeData("-5-0", 0));
            retMap.put("07", new RangeData("-10--5", 0));
            retMap.put("08", new RangeData("-15--10", -1));
            retMap.put("09", new RangeData("-20--15", -2));
            retMap.put("10", new RangeData("-25--20", -3));
            retMap.put(SOUKEI, new RangeData("総計"));
            return retMap;
        }

        public Map getRangeMap() {

            final Map retMap = new TreeMap<String, RangeData>();
            retMap.put("01", new RangeData("75-80", 5));
            retMap.put("02", new RangeData("70-75", 4));
            retMap.put("03", new RangeData("65-70", 3));
            retMap.put("04", new RangeData("60-65", 2));
            retMap.put("05", new RangeData("55-60", 1));
            retMap.put("06", new RangeData("50-55", 0));
            retMap.put("07", new RangeData("45-50", 0));
            retMap.put("08", new RangeData("40-45", 0));
            retMap.put("09", new RangeData("35-40", 0));
            retMap.put("10", new RangeData("30-35", -1));
            retMap.put("11", new RangeData("25-30", -2));
            retMap.put("12", new RangeData("20-25", -3));
            retMap.put(KESSEKI, new RangeData("欠席等"));
            retMap.put(SOUKEI, new RangeData("総計"));
            return retMap;
        }

        public void setMockData(final DB2UDB db2) throws SQLException {

            //対象クラス
            String gradeHrInState = "";
            String gradeHrSep = "";
            for (Iterator itClass = _hrClassMap.keySet().iterator(); itClass.hasNext();) {
                final String gradeHr = (String) itClass.next();

                gradeHrInState += gradeHrSep + "'" + gradeHr + "'";
                gradeHrSep = ",";
            }
            gradeHrInState = "".equals(gradeHrInState) ? "('')" : "(" + gradeHrInState + ")";

            //対象教科
            String sclKindClsInState = "";
            String sclKindClsSep = "";
            for (Iterator itClass = _titleClassMap.keySet().iterator(); itClass.hasNext();) {
                final String schKindCls = (String) itClass.next();
                final String[] kindClsArray = schKindCls.split("-");

                sclKindClsInState += sclKindClsSep + "'" + kindClsArray[1] + "-" + kindClsArray[2] + "'";
                sclKindClsSep = ",";
            }
            sclKindClsInState = "".equals(sclKindClsInState) ? "('')" : "(" + sclKindClsInState + ")";

            final String mockRangeSql = getMockRangeSql(gradeHrInState, sclKindClsInState);
            log.debug(mockRangeSql);
            PreparedStatement psMockRange = null;
            ResultSet rsMockRange = null;
            try {
                psMockRange = db2.prepareStatement(mockRangeSql);
                rsMockRange = psMockRange.executeQuery();
                HrClass hrClassObj = null;
                while (rsMockRange.next()) {
                    final String grade = rsMockRange.getString("GRADE");
                    final String hrClass = rsMockRange.getString("HR_CLASS");
                    final String schregNo = rsMockRange.getString("SCHREGNO");
                    final String groupCd = rsMockRange.getString("GROUPCD");
                    final String mockCd = rsMockRange.getString("MOCKCD");
                    final String mockName = rsMockRange.getString("MOCKNAME1");
                    final String classCd = rsMockRange.getString("CLASSCD");
                    final String schoolKind = rsMockRange.getString("SCHOOL_KIND");
                    final String curriculumCd = rsMockRange.getString("CURRICULUM_CD");
                    final String subclassCd = rsMockRange.getString("SUBCLASSCD");
                    final String subclassAbbv = rsMockRange.getString("SUBCLASSABBV");
                    final String deviation = rsMockRange.getString("DEVIATION");

                    final String setSubclassCd = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclassCd;
                    String setGroupSubCls = groupCd + "-" + setSubclassCd;

                    hrClassObj = _hrClassMap.get(grade + "-" + hrClass);

                    GrpMock grpMock = null;
                    MockData mockData = null;
                    MockSubclassData mockSubclassData = null;
                    if (!hrClassObj._grpMockMap.containsKey(setGroupSubCls)) {
                        grpMock = new GrpMock(setGroupSubCls);

                        mockData = new MockData(MOCK_NOBIRITSU, "伸び率");
                        mockSubclassData = new MockSubclassData(NOBIRITSU_SUBCLS, subclassAbbv);
                        mockSubclassData._rangeDataMap = getNobiritsuMap();
                        mockData._mockSubclassDataMap.put(NOBIRITSU_SUBCLS, mockSubclassData);
                        grpMock._mockDataMap.put(MOCK_NOBIRITSU, mockData);


                        mockData = new MockData(mockCd, mockName);
                        mockSubclassData = new MockSubclassData(setSubclassCd, subclassAbbv);
                        mockSubclassData._rangeDataMap = getRangeMap();
                        mockData._mockSubclassDataMap.put(setSubclassCd, mockSubclassData);
                        grpMock._mockDataMap.put(mockCd, mockData);
                    } else {
                        grpMock = hrClassObj._grpMockMap.get(setGroupSubCls);
                        if (!grpMock._mockDataMap.containsKey(mockCd)) {
                            mockData = new MockData(mockCd, mockName);
                        } else {
                            mockData = grpMock._mockDataMap.get(mockCd);
                        }
                        if (!mockData._mockSubclassDataMap.containsKey(setSubclassCd)) {
                            mockSubclassData = new MockSubclassData(setSubclassCd, subclassAbbv);
                        } else {
                            mockSubclassData = mockData._mockSubclassDataMap.get(setSubclassCd);
                        }
                        if (mockSubclassData._rangeDataMap.size() == 0) {
                            mockSubclassData._rangeDataMap = getRangeMap();
                        }
                    }
                    //伸び率計算
                    if (!grpMock._nobiritsuMap.containsKey(schregNo)) {
                        grpMock._nobiritsuMap.put(schregNo, new Nobiritsu(StringUtils.defaultString(deviation, "0.0")));
                    } else {
                        final Nobiritsu nobiritsu = grpMock._nobiritsuMap.get(schregNo);
                        nobiritsu.setLast(StringUtils.defaultString(deviation, "0.0"));
                    }
                    final String rangeKey = _param.getRangeField(deviation);
                    final RangeData rangeData = mockSubclassData._rangeDataMap.get(rangeKey);
                    rangeData._cnt++;

                    final RangeData totalRangeData = mockSubclassData._rangeDataMap.get(SOUKEI);
                    totalRangeData._cnt++;
                    totalRangeData._totalScore += rangeData._baseScore;
                    totalRangeData._totalDeviation = totalRangeData._totalDeviation.add(new BigDecimal(StringUtils.defaultString(deviation, "0.0")));
                    if (!KESSEKI.equals(rangeKey)) {
                        totalRangeData.setAvg();
                    }

                    mockData._mockSubclassDataMap.put(setSubclassCd, mockSubclassData);
                    grpMock._mockDataMap.put(mockCd, mockData);
                    grpMock._totalScore = totalRangeData._totalScore;
                    hrClassObj._grpMockMap.put(setGroupSubCls, grpMock);
                }
            } finally {
                DbUtils.closeQuietly(null, psMockRange, rsMockRange);
            }

            //伸び率セット
            final BigDecimal minVal = new BigDecimal(20.0);
            for (Iterator itHr = _hrClassMap.keySet().iterator(); itHr.hasNext();) {
                final String gradeHr = (String) itHr.next();
                final HrClass hrClass = _hrClassMap.get(gradeHr);
                //模試グループ
                for (Iterator itMockGrp = hrClass._grpMockMap.keySet().iterator(); itMockGrp.hasNext();) {
                    final String mockGrp = (String) itMockGrp.next();
                    final GrpMock grpMock = hrClass._grpMockMap.get(mockGrp);

                    final MockData mockData = grpMock._mockDataMap.get(MOCK_NOBIRITSU);

                    final MockSubclassData mockSubclassData = mockData._mockSubclassDataMap.get(NOBIRITSU_SUBCLS);

                    final RangeData totalRangeData = mockSubclassData._rangeDataMap.get(SOUKEI);

                    for (Iterator itNobiritsu = grpMock._nobiritsuMap.keySet().iterator(); itNobiritsu.hasNext();) {
                        final String schregNo = (String) itNobiritsu.next();
                        final Nobiritsu nobiritsu = grpMock._nobiritsuMap.get(schregNo);
                        if (nobiritsu._first.compareTo(minVal) > -1 && nobiritsu._last.compareTo(minVal) > -1) {
                            final String rangeKey = _param.getNobiritsuRangeField(nobiritsu._nobiritsu.toString());
                            final RangeData rangeData = mockSubclassData._rangeDataMap.get(rangeKey);
                            rangeData._cnt++;
                            totalRangeData._cnt++;
                            totalRangeData._totalScore += rangeData._baseScore;
                        }
                    }
                    hrClass._nobiRitsu += totalRangeData._totalScore;
                    hrClass._score += grpMock._totalScore;
                    final int addScore = totalRangeData._totalScore + grpMock._totalScore;
                    hrClass._totalScore += addScore;
                    _totalScore += addScore;
                    if (addScore > 0) {
                        setAvg(hrClass);
                    }
                }
            }
        }
        private void setAvg(final HrClass hrClass) {
            _totalCntMap.put(hrClass._grade + "-" + hrClass._hrClass, hrClass);
            _avgScore = BigDecimal.valueOf(_totalScore).divide(BigDecimal.valueOf(_totalCntMap.size()), 1, BigDecimal.ROUND_HALF_UP);
        }

        private String getMockRangeSql(final String gradeHrInState, final String sclKindClsInState) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear +"' ");
            stb.append("     AND GRADE || '-' || HR_CLASS IN " + gradeHrInState +" ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     SCHREGNO ");
            stb.append(" ), STF_SUBCLS AS ( ");
            stb.append(" SELECT ");
            stb.append("     A023.ABBV2, ");
            stb.append("     SUBCLASS_M.CLASSCD, ");
            stb.append("     SUBCLASS_M.SCHOOL_KIND, ");
            stb.append("     SUBCLASS_M.CURRICULUM_CD, ");
            stb.append("     SUBCLASS_M.SUBCLASSCD, ");
            stb.append("     MAX(SUBCLASS_M.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT C_STF ");
            stb.append("     INNER JOIN CHAIR_DAT C_DAT ON C_STF.YEAR = C_DAT.YEAR ");
            stb.append("           AND C_STF.SEMESTER = C_DAT.SEMESTER ");
            stb.append("           AND C_STF.CHAIRCD = C_DAT.CHAIRCD ");
            stb.append("     INNER JOIN SUBCLASS_MST SUBCLASS_M ON C_DAT.CLASSCD = SUBCLASS_M.CLASSCD ");
            stb.append("           AND C_DAT.SCHOOL_KIND = SUBCLASS_M.SCHOOL_KIND ");
            stb.append("           AND C_DAT.CURRICULUM_CD = SUBCLASS_M.CURRICULUM_CD ");
            stb.append("           AND C_DAT.SUBCLASSCD = SUBCLASS_M.SUBCLASSCD ");
            stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
            stb.append("          AND SUBCLASS_M.SCHOOL_KIND = A023.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     C_STF.YEAR = '" + _param._ctrlYear +"' ");
            stb.append("     AND C_STF.STAFFCD = '" + _staffCd +"' ");
            stb.append(" GROUP BY ");
            stb.append("     A023.ABBV2, ");
            stb.append("     SUBCLASS_M.CLASSCD, ");
            stb.append("     SUBCLASS_M.SCHOOL_KIND, ");
            stb.append("     SUBCLASS_M.CURRICULUM_CD, ");
            stb.append("     SUBCLASS_M.SUBCLASSCD ");
            stb.append(" ), M_GRP_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     MG_SELECT.YEAR, ");
            stb.append("     MG_SELECT.GROUPCD, ");
            stb.append("     M_MST.MOCKCD, ");
            stb.append("     M_MST.MOCKNAME1, ");
            stb.append("     MG_SELECT.MOCK_SUBCLASS_CD, ");
            stb.append("     MG_SELECT.CLASSCD, ");
            stb.append("     MG_SELECT.SCHOOL_KIND, ");
            stb.append("     MG_SELECT.CURRICULUM_CD, ");
            stb.append("     MG_SELECT.SUBCLASSCD, ");
            stb.append("     MG_SELECT.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     ( ");
            stb.append("     SELECT ");
            stb.append("         M_GRP.YEAR, ");
            stb.append("         M_GRP.GROUPCD, ");
            stb.append("         M_RANGE.MOCK_SUBCLASS_CD, ");
            stb.append("         M_SUB.CLASSCD, ");
            stb.append("         M_SUB.SCHOOL_KIND, ");
            stb.append("         M_SUB.CURRICULUM_CD, ");
            stb.append("         M_SUB.SUBCLASSCD, ");
            stb.append("         SUB_M.SUBCLASSABBV ");
            stb.append("     FROM ");
            stb.append("         SCH_T ");
            stb.append("         LEFT JOIN MOCK_RANK_RANGE_DAT M_RANGE ON SCH_T.YEAR = M_RANGE.YEAR ");
            stb.append("              AND SCH_T.SCHREGNO = M_RANGE.SCHREGNO ");
            stb.append("              AND RANK_RANGE = '2' ");
            stb.append("              AND RANK_DIV = '02' ");
            stb.append("              AND MOCKDIV = '1' ");
            stb.append("         INNER JOIN MOCK_DISP_GROUP_DAT M_GRP ON M_RANGE.YEAR = M_GRP.YEAR ");
            stb.append("               AND M_RANGE.MOCKCD = M_GRP.MOCKCD ");
            stb.append("         INNER JOIN MOCK_SUBCLASS_MST M_SUB ON M_RANGE.MOCK_SUBCLASS_CD = M_SUB.MOCK_SUBCLASS_CD ");
            stb.append("               AND M_SUB.SCHOOL_KIND || '-' || M_SUB.CLASSCD IN " + sclKindClsInState + " ");
            stb.append("         INNER JOIN SUBCLASS_MST SUB_M ON M_SUB.CLASSCD = SUB_M.CLASSCD ");
            stb.append("               AND M_SUB.SCHOOL_KIND = SUB_M.SCHOOL_KIND ");
            stb.append("               AND M_SUB.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("               AND M_SUB.SUBCLASSCD = SUB_M.SUBCLASSCD ");
            stb.append("     GROUP BY ");
            stb.append("         M_GRP.YEAR, ");
            stb.append("         M_GRP.GROUPCD, ");
            stb.append("         M_RANGE.MOCK_SUBCLASS_CD, ");
            stb.append("         M_SUB.CLASSCD, ");
            stb.append("         M_SUB.SCHOOL_KIND, ");
            stb.append("         M_SUB.CURRICULUM_CD, ");
            stb.append("         M_SUB.SUBCLASSCD, ");
            stb.append("         SUB_M.SUBCLASSABBV ");
            stb.append("     ) MG_SELECT ");
            stb.append("     LEFT JOIN MOCK_DISP_GROUP_DAT M_GRP ON MG_SELECT.YEAR = M_GRP.YEAR ");
            stb.append("          AND MG_SELECT.GROUPCD = M_GRP.GROUPCD ");
            stb.append("     INNER JOIN MOCK_MST M_MST ON M_GRP.MOCKCD = M_MST.MOCKCD ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'X' ");
            stb.append("         FROM ");
            stb.append("             STF_SUBCLS ");
            stb.append("         WHERE ");
            stb.append("         STF_SUBCLS.CLASSCD = MG_SELECT.CLASSCD ");
            stb.append("         AND STF_SUBCLS.SCHOOL_KIND = MG_SELECT.SCHOOL_KIND ");
            stb.append("         AND STF_SUBCLS.CURRICULUM_CD = MG_SELECT.CURRICULUM_CD ");
            stb.append("         AND STF_SUBCLS.SUBCLASSCD = MG_SELECT.SUBCLASSCD ");
            stb.append("     ) ");
            stb.append(" ORDER BY ");
            stb.append("     MG_SELECT.YEAR, ");
            stb.append("     MG_SELECT.GROUPCD, ");
            stb.append("     M_MST.MOCKCD, ");
            stb.append("     M_MST.MOCKNAME1, ");
            stb.append("     MG_SELECT.CLASSCD, ");
            stb.append("     MG_SELECT.SCHOOL_KIND, ");
            stb.append("     MG_SELECT.CURRICULUM_CD, ");
            stb.append("     MG_SELECT.SUBCLASSCD, ");
            stb.append("     MG_SELECT.SUBCLASSABBV ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SCH_T.GRADE, ");
            stb.append("     SCH_T.HR_CLASS, ");
            stb.append("     SCH_T.SCHREGNO, ");
            stb.append("     M_GRP_SUB.GROUPCD, ");
            stb.append("     M_GRP_SUB.MOCKCD, ");
            stb.append("     M_GRP_SUB.MOCKNAME1, ");
            stb.append("     M_GRP_SUB.CLASSCD, ");
            stb.append("     M_GRP_SUB.SCHOOL_KIND, ");
            stb.append("     M_GRP_SUB.CURRICULUM_CD, ");
            stb.append("     M_GRP_SUB.SUBCLASSCD, ");
            stb.append("     M_GRP_SUB.SUBCLASSABBV, ");
            stb.append("     M_RANGE.DEVIATION ");
            stb.append(" FROM ");
            stb.append("     M_GRP_SUB ");
            stb.append("     INNER JOIN SCH_T ON M_GRP_SUB.YEAR = SCH_T.YEAR ");
            stb.append("     LEFT JOIN MOCK_RANK_RANGE_DAT M_RANGE ON SCH_T.YEAR = M_RANGE.YEAR ");
            stb.append("          AND M_GRP_SUB.MOCKCD = M_RANGE.MOCKCD ");
            stb.append("          AND SCH_T.SCHREGNO = M_RANGE.SCHREGNO ");
            stb.append("          AND M_RANGE.RANK_RANGE = '2' ");
            stb.append("          AND M_RANGE.RANK_DIV = '02' ");
            stb.append("          AND M_RANGE.MOCKDIV = '1' ");
            stb.append("          AND M_GRP_SUB.MOCK_SUBCLASS_CD = M_RANGE.MOCK_SUBCLASS_CD ");
            stb.append(" ORDER BY ");
            stb.append("     SCH_T.GRADE, ");
            stb.append("     SCH_T.HR_CLASS, ");
            stb.append("     SCH_T.SCHREGNO, ");
            stb.append("     M_GRP_SUB.GROUPCD, ");
            stb.append("     M_GRP_SUB.MOCKCD, ");
            stb.append("     M_GRP_SUB.CLASSCD, ");
            stb.append("     M_GRP_SUB.SCHOOL_KIND, ");
            stb.append("     M_GRP_SUB.CURRICULUM_CD, ");
            stb.append("     M_GRP_SUB.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private String getTitleClassSql() {
        final StringBuffer stb = new StringBuffer();

        final String staff = SQLUtils.whereIn(true, _param._categorySelected);
        stb.append(" SELECT ");
        stb.append("     C_STF.STAFFCD, ");
        stb.append("     A023.ABBV2, ");
        stb.append("     CLASS_M.SCHOOL_KIND, ");
        stb.append("     CLASS_M.CLASSCD, ");
        stb.append("     MAX(CLASS_M.CLASSNAME) AS CLASSNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT C_STF ");
        stb.append("     INNER JOIN CHAIR_DAT C_DAT ON C_STF.YEAR = C_DAT.YEAR ");
        stb.append("           AND C_STF.SEMESTER = C_DAT.SEMESTER ");
        stb.append("           AND C_STF.CHAIRCD = C_DAT.CHAIRCD ");
        stb.append("     INNER JOIN CLASS_MST CLASS_M ON C_DAT.CLASSCD = CLASS_M.CLASSCD ");
        stb.append("           AND C_DAT.SCHOOL_KIND = CLASS_M.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("          AND CLASS_M.SCHOOL_KIND = A023.NAME1 ");
        stb.append(" WHERE ");
        stb.append("     C_STF.YEAR = '" + _param._ctrlYear +"' ");
        stb.append("     AND C_STF.STAFFCD IN " + staff + " ");
        stb.append(" GROUP BY ");
        stb.append("     C_STF.STAFFCD, ");
        stb.append("     A023.ABBV2, ");
        stb.append("     CLASS_M.SCHOOL_KIND, ");
        stb.append("     CLASS_M.CLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     C_STF.STAFFCD, ");
        stb.append("     A023.ABBV2, ");
        stb.append("     CLASS_M.CLASSCD ");
        return stb.toString();
    }

    private String getSclKindHr() {
        final StringBuffer stb = new StringBuffer();
        final String staff = SQLUtils.whereIn(true, _param._categorySelected);

        stb.append(" SELECT ");
        stb.append("     C_STF.STAFFCD, ");
        stb.append("     C_CLS.TRGTGRADE, ");
        stb.append("     C_CLS.TRGTCLASS, ");
        stb.append("     MAX(REGD_H.HR_NAMEABBV) AS HR_NAMEABBV, ");
        stb.append("     GDAT.SCHOOL_KIND ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT C_STF ");
        stb.append("     INNER JOIN CHAIR_CLS_DAT C_CLS ON C_STF.YEAR = C_CLS.YEAR ");
        stb.append("           AND C_STF.CHAIRCD = C_CLS.CHAIRCD ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGD_H ON C_CLS.YEAR = REGD_H.YEAR ");
        stb.append("           AND C_CLS.TRGTGRADE = REGD_H.GRADE ");
        stb.append("           AND C_CLS.TRGTCLASS = REGD_H.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON C_CLS.YEAR = GDAT.YEAR ");
        stb.append("           AND C_CLS.TRGTGRADE = GDAT.GRADE ");
        stb.append(" WHERE ");
        stb.append("     C_STF.YEAR = '" + _param._ctrlYear +"' ");
        stb.append("     AND C_STF.STAFFCD IN " + staff + " ");
        stb.append(" GROUP BY ");
        stb.append("     C_STF.STAFFCD, ");
        stb.append("     C_CLS.TRGTGRADE, ");
        stb.append("     C_CLS.TRGTCLASS, ");
        stb.append("     GDAT.SCHOOL_KIND ");
        stb.append(" ORDER BY ");
        stb.append("     C_STF.STAFFCD, ");
        stb.append("     C_CLS.TRGTGRADE, ");
        stb.append("     C_CLS.TRGTCLASS ");

        return stb.toString();
    }

    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrAbbv;
        final String _schoolKind;
        int _score;
        int _nobiRitsu;
        int _totalScore;
        final Map<String, GrpMock> _grpMockMap;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrAbbv,
                final String schoolKind
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrAbbv = hrAbbv;
            _schoolKind = schoolKind;
            _score = 0;
            _nobiRitsu = 0;
            _totalScore = 0;
            _grpMockMap = new TreeMap<String, GrpMock>();
        }
    }

    private class GrpMock {
        final String _groupCd;
        Map<String, MockData> _mockDataMap;
        Map<String, Nobiritsu> _nobiritsuMap;
        int _totalScore = 0;
        public GrpMock(
                final String groupCd
        ) {
            _groupCd = groupCd;
            _mockDataMap = new TreeMap<String, MockData>();
            _nobiritsuMap = new HashMap<String, Nobiritsu>();
        }
    }

    private class MockData {
        final String _mockCd;
        final String _mockName;
        Map<String, MockSubclassData> _mockSubclassDataMap;
        public MockData(
                final String mockCd,
                final String mockName
        ) {
            _mockCd = mockCd;
            _mockName = mockName;
            _mockSubclassDataMap = new TreeMap<String, MockSubclassData>();
        }
    }

    private class MockSubclassData {
        final String _subclassCd;
        final String _subclassAbbv;
        Map<String, RangeData> _rangeDataMap;
        public MockSubclassData(
                final String subclassCd,
                final String subclassAbbv
        ) {
            _subclassCd = subclassCd;
            _subclassAbbv = subclassAbbv;
            _rangeDataMap = new TreeMap<String, RangeData>();
        }
    }

    private class RangeData {
        final int _baseScore;
        final String _rangeName;
        int _cnt = 0;
        int _totalScore = 0;
        BigDecimal _totalDeviation;
        int _avgCnt = 0;
        BigDecimal _avg;
        public RangeData(final String rangeName) {
            _rangeName = rangeName;
            _baseScore = 0;
            _totalDeviation = new BigDecimal("0.0");
            _avg = new BigDecimal("0.0");
        }
        public RangeData(final String rangeName, final int score) {
            _rangeName = rangeName;
            _baseScore = score;
            _totalDeviation = new BigDecimal("0.0");
            _avg = new BigDecimal("0.0");
        }
        public String getScore() {
            if (_cnt > 0) {
                return String.valueOf(_baseScore * _cnt);
            } else {
                return "";
            }
        }
        public String getCnt() {
            if (_cnt > 0) {
                return String.valueOf(_cnt);
            } else {
                return "";
            }
        }
        public void setAvg() {
            _avgCnt++;
            _avg = _totalDeviation.divide(BigDecimal.valueOf(_avgCnt), 1, BigDecimal.ROUND_HALF_UP);
        }
    }

    private class Nobiritsu {
        final BigDecimal _first;
        BigDecimal _last;
        BigDecimal _nobiritsu;
        public Nobiritsu(final String first) {
            _first = new BigDecimal(first);
        }
        private void setLast(final String last) {
            _last = new BigDecimal(last);
            _nobiritsu = _last.subtract(_first);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74097 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String[] _categorySelected;
        private final String _documentRoot;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _categorySelected = request.getParameterValues("category_selected");
            _documentRoot = request.getParameter("DOCUMENTROOT");
        }

        String getImageFilePath(final String imageDir, final String filename, final String ext) {
            if (null == _documentRoot || null == imageDir || null == ext || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentRoot).append("/").append(imageDir).append("/").append(filename).append(".").append(ext);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

        String getRangeField(final String deviation) {
            if (null == deviation) {
                return KESSEKI;
            }
            final BigDecimal bigDec = new BigDecimal(deviation);
            if (new BigDecimal(75.0).floatValue() <= bigDec.floatValue()) {
                return "01";
            } else if (new BigDecimal(70.0).floatValue() <= bigDec.floatValue()) {
                return "02";
            } else if (new BigDecimal(65.0).floatValue() <= bigDec.floatValue()) {
                return "03";
            } else if (new BigDecimal(60.0).floatValue() <= bigDec.floatValue()) {
                return "04";
            } else if (new BigDecimal(55.0).floatValue() <= bigDec.floatValue()) {
                return "05";
            } else if (new BigDecimal(50.0).floatValue() <= bigDec.floatValue()) {
                return "06";
            } else if (new BigDecimal(45.0).floatValue() <= bigDec.floatValue()) {
                return "07";
            } else if (new BigDecimal(40.0).floatValue() <= bigDec.floatValue()) {
                return "08";
            } else if (new BigDecimal(35.0).floatValue() <= bigDec.floatValue()) {
                return "09";
            } else if (new BigDecimal(30.0).floatValue() <= bigDec.floatValue()) {
                return "10";
            } else if (new BigDecimal(25.0).floatValue() <= bigDec.floatValue()) {
                return "11";
            } else if (new BigDecimal(20.0).floatValue() <= bigDec.floatValue()) {
                return "12";
            }
            return "";
        }

        String getNobiritsuRangeField(final String deviation) {
            if (null == deviation) {
                return KESSEKI;
            }
            final BigDecimal bigDec = new BigDecimal(deviation);
            if (new BigDecimal(20.0).floatValue() <= bigDec.floatValue()) {
                return "01";
            } else if (new BigDecimal(15.0).floatValue() <= bigDec.floatValue()) {
                return "02";
            } else if (new BigDecimal(10.0).floatValue() <= bigDec.floatValue()) {
                return "03";
            } else if (new BigDecimal(5.0).floatValue() <= bigDec.floatValue()) {
                return "04";
            } else if (new BigDecimal(0.0).floatValue() <= bigDec.floatValue()) {
                return "05";
            } else if (new BigDecimal(-5.0).floatValue() <= bigDec.floatValue()) {
                return "06";
            } else if (new BigDecimal(-10.0).floatValue() <= bigDec.floatValue()) {
                return "07";
            } else if (new BigDecimal(-15.0).floatValue() <= bigDec.floatValue()) {
                return "08";
            } else if (new BigDecimal(-20.0).floatValue() <= bigDec.floatValue()) {
                return "09";
            } else if (new BigDecimal(-25.0).floatValue() <= bigDec.floatValue()) {
                return "10";
            }
            return "";
        }
    }
}

// eof
