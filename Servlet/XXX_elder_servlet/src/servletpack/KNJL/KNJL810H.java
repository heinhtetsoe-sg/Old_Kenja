/*
 * $Id: e9fd7e97c75096e2c0f384142b7f57ef158af90f $
 *
 * 作成日: 2020/10/02
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL810H {

    private static final Log log = LogFactory.getLog(KNJL810H.class);

    private boolean _hasData;

    private Param _param;

    private static final String ABSENT = "5"; // 5:欠席

    private static final int CONFIDENTIAL = 0; // 内申教科科目
    private static final int TESTSUBCLASS = 1;  // テスト教科科目

    private static final int SHIGANSYA_ICHIRAN_LINE_MAX = 25; // 志願者一覧表の一頁の最大表示行数

    private static final int SYUKESEKI_KINYUHYOU_LINE_MAX = 45; // 出欠席記入表の一列の最大表示行数
    private static final int SYUKESEKI_KINYUHYOU_COL_MAX = 2; // 出欠席記入表の最大表示列数

    private static final int SEISEKI_KINYUHYOU_LINE_MAX = 45 ;// 成績記入表の一列の最大表示行数
    private static final int SEISEKI_KINYUHYOU_COL_MAX = 2; // 成績記入表の最大表示列数

    private static final int TACK_SEAL_MAX_LINE = 10;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String outputDate = sdf.format(new Date());

        if ("1".equals(_param._output)) {
            printShigansyaIchiranhyou(db2, svf, outputDate);
        } else if ("2".equals(_param._output)) {
            printSyukesekiKinyuhyou(db2, svf, outputDate);
        } else if ("3".equals(_param._output)) {
            printSeisekiKinyuhyou(db2, svf, outputDate);
        } else if ("4".equals(_param._output)) {
            printTackSeal(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 志願者一覧表を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd 形式の現在日時
     */
    private void printShigansyaIchiranhyou(final DB2UDB db2, final Vrw32alp svf, final String outputDate) {
        svf.VrSetForm("KNJL810H_1.frm", 1);

        List<TestSubclassData> testSubclassList = getTestSubclassList(db2, CONFIDENTIAL);
        Map<String, String> qualificationsMap = getQualificationsMap(db2);
        List<List<ShigansyaData>> shigansyaList = getShigansyaList(db2);

        int pageCnt = 1;

        for (List<ShigansyaData> refList : shigansyaList) {
            setShigansyaTitle(svf, pageCnt, outputDate);
            setShigansyaHeader(svf, testSubclassList);
            setShigansyaData(svf, refList, testSubclassList, qualificationsMap);

            svf.VrEndPage();
            pageCnt++;
        }
    }

    /**
     * 出欠席記入表を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd 形式の現在日時
     */
    private void printSyukesekiKinyuhyou(final DB2UDB db2, final Vrw32alp svf, final String outputDate) {
        svf.VrSetForm("KNJL810H_2.frm", 1);

        int lineCnt = 1;
        int colCnt = 1;
        int pageCnt = 1;
        List<Map<String, List<SyukesekiKinyuData>>> syukesekiKinyuList = getSyukesekiKinyuList(db2);

        for (Map<String, List<SyukesekiKinyuData>> examhallMap : syukesekiKinyuList) {
            for (List<SyukesekiKinyuData> refList : examhallMap.values()) {
                svf.VrsOut("PAGE", pageCnt + "頁");
                svf.VrsOut("DATE", outputDate);
                svf.VrsOut("TITLE", _param._entexamyear + "年度　出欠席記入表");
                svf.VrsOut("EXAM_DIV", _param._testDivName);
                svf.VrsOut("EXAM_HALL", refList.get(0)._examhallName);
                svf.VrsOut("SCHOOL_NAME", _param._schoolName);

                for (SyukesekiKinyuData syukesekiKinyuData : refList) {
                    if (lineCnt > SYUKESEKI_KINYUHYOU_LINE_MAX) {
                        lineCnt = 1;
                        colCnt++;
                    }

                    svf.VrsOutn("EXAM_NO" + String.valueOf(colCnt), lineCnt, syukesekiKinyuData._receptNo);

                    if (!"1".equals(_param._check)) {
                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(syukesekiKinyuData._name);
                        final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                        svf.VrsOutn("NAME" + String.valueOf(colCnt) + "_" + nameFieldStr, lineCnt, syukesekiKinyuData._name);

                        svf.VrsOutn("SEX" + String.valueOf(colCnt), lineCnt, syukesekiKinyuData._sex);
                    }

                    if (ABSENT.equals(syukesekiKinyuData._judgeDiv)) {
                        svf.VrsOutn("ATTEND" + String.valueOf(colCnt), lineCnt, "欠席");
                    }

                    lineCnt++;
                    _hasData = true;
                }

                svf.VrEndPage();
                lineCnt = 1;
                colCnt = 1;
                pageCnt++;
            }
        }
    }

    /**
     * 成績記入表を出力する。
     *
     * @param db2
     * @param svf
     * @param outputDate yyyy/MM/dd 形式の現在日時
     */
    private void printSeisekiKinyuhyou(final DB2UDB db2, final Vrw32alp svf, final String outputDate) {
        svf.VrSetForm("KNJL810H_3.frm", 1);

        int lineCnt = 1;
        int colCnt = 1;
        int pageCnt = 1;
        List<TestSubclassData> testSubclassList = getTestSubclassList(db2, TESTSUBCLASS);
        List<Map<String, List<SeisekiKinyuData>>> seisekiKinyuList = getSeisekiKinyuList(db2);

        for (TestSubclassData testSubclassData : testSubclassList) {
            for (Map<String, List<SeisekiKinyuData>> examhallMap : seisekiKinyuList) {
               for (List<SeisekiKinyuData> refList : examhallMap.values()) {
                    svf.VrsOut("PAGE", pageCnt + "頁");
                    svf.VrsOut("DATE", outputDate);
                    svf.VrsOut("TITLE", _param._entexamyear + "年度　成績記入表");
                    svf.VrsOut("EXAM_DIV", _param._testDivName);
                    svf.VrsOut("EXAM_SUBCLASS", testSubclassData._testClassName);
                    svf.VrsOut("EXAM_HALL", refList.get(0)._examhallName);
                    svf.VrsOut("SCHOOL_NAME", _param._schoolName);

                    for (SeisekiKinyuData seisekiKinyuData : refList) {
                        if (lineCnt > SEISEKI_KINYUHYOU_LINE_MAX) {
                           lineCnt = 1;
                            colCnt++;
                        }

                        svf.VrsOutn("EXAM_NO" + String.valueOf(colCnt), lineCnt, seisekiKinyuData._receptNo);

                        if (!"1".equals(_param._check)) {
                            final int nameByte = KNJ_EditEdit.getMS932ByteLength(seisekiKinyuData._name);
                            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                            svf.VrsOutn("NAME" + String.valueOf(colCnt) + "_" + nameFieldStr, lineCnt, seisekiKinyuData._name);

                            svf.VrsOutn("SEX" + String.valueOf(colCnt), lineCnt, seisekiKinyuData._sex);
                        }

                        if (ABSENT.equals(seisekiKinyuData._judgeDiv)) {
                            svf.VrsOutn("SCORE" + String.valueOf(colCnt), lineCnt, "欠席");
                        }

                        lineCnt++;
                        _hasData = true;
                    }

                    svf.VrEndPage();
                    lineCnt = 1;
                    colCnt = 1;
                    pageCnt++;
                }
            }
        }
    }

    /**
     * 机上タックシールを出力する。
     *
     * @param db2
     * @param svf
     */
    private void printTackSeal(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL810H_4.frm", 1);

        int lineCnt = 0;

        List<TackSealData> tackSealList = getTackSealList(db2);

        for (TackSealData tackSealData : tackSealList) {
            // 改ページの制御
            if (lineCnt >= TACK_SEAL_MAX_LINE) {
                svf.VrEndPage();
                lineCnt = 0;
            }

            int lineFiledCnt = (lineCnt / 2) + 1;
            String lineFiledStr = String.valueOf((lineCnt % 2) + 1);
            svf.VrsOutn("EXAM_DIV" + lineFiledStr, lineFiledCnt, _param._testDivName);
            svf.VrsOutn("EXAM_NO" + lineFiledStr, lineFiledCnt, tackSealData._receptNo);
            svf.VrsOutn("EXAM_HALL" + lineFiledStr, lineFiledCnt, tackSealData._examhallName);

            lineCnt++;
            _hasData = true;
        }
    }

    private void setShigansyaTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("EXAM_DIV", _param._testDivName);
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", outputDate);
        svf.VrsOut("TITLE", _param._entexamyear + "年度　志願者一覧表（受験番号順）");
    }

    private void setShigansyaHeader(final Vrw32alp svf, final List<TestSubclassData> testSubclassList) {
        int colMax = 0;
        if ("1".equals(_param._applicantDiv)) {
            colMax = 8;

            svf.VrsOut("DIV_HEADER9", "評定");
            svf.VrsOut("DIV_HEADER10", "行動");
            svf.VrsOut("DIV_HEADER11", "欠席");

        } else if ("2".equals(_param._applicantDiv)) {
            colMax = 9;

            svf.VrsOut("DIV_HEADER10", "５教");
            svf.VrsOut("DIV_HEADER11", "３教");
            svf.VrsOut("DIV_HEADER12", "資格");
            svf.VrsOut("DIV_HEADER13", "評定");
            svf.VrsOut("DIV_HEADER14", "行動");
            svf.VrsOut("DIV_HEADER15", "欠席");
        }

        for (int i = 0; (i < colMax) && (i < testSubclassList.size()); i++ ) {
            TestSubclassData testSubclassData = testSubclassList.get(i);
            svf.VrsOut("DIV_HEADER" + testSubclassData._colNumber, testSubclassData._testClassName);
        }
    }

    private void setShigansyaData(final Vrw32alp svf, final List<ShigansyaData> refList, final List<TestSubclassData> testSubclassList, final Map<String, String> qualificationsMap) {
        int lineCnt = 1;

        for (ShigansyaData shigansyaData : refList) {
            svf.VrsOutn("EXAM_NO1", lineCnt, shigansyaData._receptNo);

            final int nameKanaByte = KNJ_EditEdit.getMS932ByteLength(shigansyaData._nameKana);
            final String nameKanaFieldStr = nameKanaByte > 30 ? "3" : nameKanaByte > 20 ? "2" : "1";
            svf.VrsOutn("KANA" + nameKanaFieldStr, lineCnt, shigansyaData._nameKana);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(shigansyaData._name);
            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, shigansyaData._name);

            svf.VrsOutn("SEX", lineCnt, shigansyaData._sex);
            svf.VrsOutn("BIRTHDAY", lineCnt, shigansyaData._birthDay);
            svf.VrsOutn("AREA_NAME", lineCnt, shigansyaData._finschoolDivName);

            final int finSchoolNameByte = KNJ_EditEdit.getMS932ByteLength(shigansyaData._finschoolName);
            final String finSchoolNameFieldStr = finSchoolNameByte > 20 ? "2" : "1";
            svf.VrsOutn("FINSCHOOL_NAME" + finSchoolNameFieldStr, lineCnt, shigansyaData._finschoolName);

            if ("1".equals(_param._applicantDiv)) {
                // 学校種別が　1:中学 の場合
                /**
                 *  CLASSCD が連番にならない場合（飛び番）を考慮して、
                 *  switch-case で CLASSCD に該当する CONFIDENTIALRPTO～ の値を出力する。
                 */
                for (TestSubclassData testSubclassData : testSubclassList) {
                    String confidentialRpto = "";
                    switch (Integer.parseInt(testSubclassData._testClassCd)) {
                    case 1:
                        confidentialRpto = shigansyaData._confidentialRpto1;
                        break;
                    case 2:
                        confidentialRpto = shigansyaData._confidentialRpto2;
                        break;
                    case 3:
                        confidentialRpto = shigansyaData._confidentialRpto3;
                        break;
                    case 4:
                        confidentialRpto = shigansyaData._confidentialRpto4;
                        break;
                    case 5:
                        confidentialRpto = shigansyaData._confidentialRpto5;
                        break;
                    case 6:
                        confidentialRpto = shigansyaData._confidentialRpto6;
                        break;
                    case 7:
                        confidentialRpto = shigansyaData._confidentialRpto7;
                        break;
                    case 8:
                        confidentialRpto = shigansyaData._confidentialRpto8;
                        break;
                    }
                    svf.VrsOutn("DIV" + testSubclassData._colNumber, lineCnt, confidentialRpto);
                }

            } else if ("2".equals(_param._applicantDiv)) {
                // 学校種別が　2:高校 の場合
                /**
                 *  CLASSCD が連番にならない場合（飛び番）を考慮して、
                 *  switch-case で CLASSCD に該当する CONFIDENTIALRPTO～ の値を出力する。
                 */
                for (TestSubclassData testSubclassData : testSubclassList) {
                    String confidentialRpto = "";
                    switch (Integer.parseInt(testSubclassData._testClassCd)) {
                    case 1:
                        confidentialRpto = shigansyaData._confidentialRpto1;
                        break;
                    case 2:
                        confidentialRpto = shigansyaData._confidentialRpto2;
                        break;
                    case 3:
                        confidentialRpto = shigansyaData._confidentialRpto3;
                        break;
                    case 4:
                        confidentialRpto = shigansyaData._confidentialRpto4;
                        break;
                    case 5:
                        confidentialRpto = shigansyaData._confidentialRpto5;
                        break;
                    case 6:
                        confidentialRpto = shigansyaData._confidentialRpto6;
                        break;
                    case 7:
                        confidentialRpto = shigansyaData._confidentialRpto7;
                        break;
                    case 8:
                        confidentialRpto = shigansyaData._confidentialRpto8;
                        break;
                    case 9:
                        confidentialRpto = shigansyaData._confidentialRpto9;
                        break;
                    }
                    svf.VrsOutn("DIV" + testSubclassData._colNumber, lineCnt, confidentialRpto);
                }
                svf.VrsOutn("DIV10", lineCnt, shigansyaData._total5);
                svf.VrsOutn("DIV11", lineCnt, shigansyaData._total3);

                String qualificationsStr = "";
                if (shigansyaData._qualifiedAbbv1 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("1")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv1) + " ";
                }
                if (shigansyaData._qualifiedAbbv2 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("2")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv2) + " ";
                }
                if (shigansyaData._qualifiedAbbv3 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("3")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv3) + " ";
                }
                if (shigansyaData._qualifiedAbbv4 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("4")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv4) + " ";
                }
                if (shigansyaData._qualifiedAbbv5 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("5")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv5) + " ";
                }
                if (shigansyaData._qualifiedAbbv6 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("6")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv6) + " ";
                }
                if (shigansyaData._qualifiedAbbv7 != null) {
                    qualificationsStr += StringUtils.defaultString(qualificationsMap.get("7")) + StringUtils.defaultString(shigansyaData._qualifiedAbbv7);
                }
                svf.VrsOutn("REQUIRE2_1", lineCnt, qualificationsStr);
            }

            svf.VrsOutn("DIV13", lineCnt, shigansyaData._totalAll);
            svf.VrsOutn("DIV14", lineCnt, shigansyaData._specialactrec);
            svf.VrsOutn("DIV15", lineCnt, shigansyaData._absenceDays3);

            svf.VrsOutn("EXAM_NO2", lineCnt, shigansyaData._recomExamno);

            lineCnt++;
            _hasData = true;
        }
    }

    /**
     * 科目一覧を返す。
     *
     * @param db2
     * @param div 0:ENTEXAM_SETTING_MSTから科目一覧（内申教科）を取得、
     * 1:ENTEXAM_TESTSUBCLASSCD_DATから科目一覧（テスト教科）を取得
     * @return
     */
    private List<TestSubclassData> getTestSubclassList(final DB2UDB db2, int div) {
        List<TestSubclassData> testSubclassList = new ArrayList<TestSubclassData>();
        int colCnt = 1;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String testSubclassSql = div == 0 ? getTestSubclassSql() : getTestSubclassSql2();
            log.debug(" sql =" + testSubclassSql);
            ps = db2.prepareStatement(testSubclassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testSubClassCd = rs.getString("TESTSUBCLASSCD");
                final String testSubClassName = rs.getString("TESTSUBCLASS_NAME");

                final TestSubclassData testSubclassData = new TestSubclassData(testSubClassCd, testSubClassName, colCnt);
                testSubclassList.add(testSubclassData);
                colCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return testSubclassList;
    }

    private Map<String, String> getQualificationsMap(final DB2UDB db2) {
        Map<String, String> qualificationsMap = new LinkedHashMap<String, String>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String qualificationsSql = getQualificationsSql();
            log.debug(" sql =" + qualificationsSql);
            ps = db2.prepareStatement(qualificationsSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String qualificationsCd = rs.getString("QUALIFICATIONSCD");
                final String qualificationsName = rs.getString("QUALIFICATIONSNAME");

                qualificationsMap.put(qualificationsCd, qualificationsName);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return qualificationsMap;
    }

    private List<List<ShigansyaData>> getShigansyaList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        int lineCnt = 1;

        List<List<ShigansyaData>> shigansyaList = new ArrayList<List<ShigansyaData>>();
        List<ShigansyaData> retList = new ArrayList<ShigansyaData>();

        try {
            final String shigansyaIchiranhyouSql = getShigansyaIchiranhyouSql();
            log.debug(" sql =" + shigansyaIchiranhyouSql);
            ps = db2.prepareStatement(shigansyaIchiranhyouSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (lineCnt > SHIGANSYA_ICHIRAN_LINE_MAX) {
                    shigansyaList.add(retList);
                    lineCnt = 1;
                    retList = new ArrayList<ShigansyaData>();
                }

                final String receptNo = rs.getString("RECEPTNO");
                final String nameKana = rs.getString("NAME_KANA");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String birthDay = rs.getString("BIRTHDAY");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String finschoolDivName = rs.getString("FINSCHOOL_DIV_NAME");
                final String confidentialRpto1 = rs.getString("CONFIDENTIAL_RPT01");
                final String confidentialRpto2 = rs.getString("CONFIDENTIAL_RPT02");
                final String confidentialRpto3 = rs.getString("CONFIDENTIAL_RPT03");
                final String confidentialRpto4 = rs.getString("CONFIDENTIAL_RPT04");
                final String confidentialRpto5 = rs.getString("CONFIDENTIAL_RPT05");
                final String confidentialRpto6 = rs.getString("CONFIDENTIAL_RPT06");
                final String confidentialRpto7 = rs.getString("CONFIDENTIAL_RPT07");
                final String confidentialRpto8 = rs.getString("CONFIDENTIAL_RPT08");
                final String confidentialRpto9 = rs.getString("CONFIDENTIAL_RPT09");
                final String total5 = rs.getString("TOTAL5");
                final String total3 = rs.getString("TOTAL3");
                final String qualifiedAbbv1 = rs.getString("QUALIFIED_ABBV1");
                final String qualifiedAbbv2 = rs.getString("QUALIFIED_ABBV2");
                final String qualifiedAbbv3 = rs.getString("QUALIFIED_ABBV3");
                final String qualifiedAbbv4 = rs.getString("QUALIFIED_ABBV4");
                final String qualifiedAbbv5 = rs.getString("QUALIFIED_ABBV5");
                final String qualifiedAbbv6 = rs.getString("QUALIFIED_ABBV6");
                final String qualifiedAbbv7 = rs.getString("QUALIFIED_ABBV7");
                final String totalAll = rs.getString("TOTAL_ALL");
                final String specialactrec = rs.getString("SPECIALACTREC");
                final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                final String recomExamno = rs.getString("RECOM_EXAMNO");

                final ShigansyaData shigansyaData = new ShigansyaData(
                        receptNo,
                        nameKana,
                        name,
                        sex,
                        birthDay,
                        finschoolName,
                        finschoolDivName,
                        confidentialRpto1,
                        confidentialRpto2,
                        confidentialRpto3,
                        confidentialRpto4,
                        confidentialRpto5,
                        confidentialRpto6,
                        confidentialRpto7,
                        confidentialRpto8,
                        confidentialRpto9,
                        total5,
                        total3,
                        qualifiedAbbv1,
                        qualifiedAbbv2,
                        qualifiedAbbv3,
                        qualifiedAbbv4,
                        qualifiedAbbv5,
                        qualifiedAbbv6,
                        qualifiedAbbv7,
                        totalAll,
                        specialactrec,
                        absenceDays3,
                        recomExamno
                        );

                retList.add(shigansyaData);
                lineCnt++;
            }

            if (!retList.isEmpty()) {
                shigansyaList.add(retList);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return shigansyaList;
    }

    private List<Map<String, List<SyukesekiKinyuData>>> getSyukesekiKinyuList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final int maxLine = SYUKESEKI_KINYUHYOU_LINE_MAX * SYUKESEKI_KINYUHYOU_COL_MAX;
        int lineCnt = 1;

        List<Map<String, List<SyukesekiKinyuData>>> syukesekiKinyuList = new ArrayList<Map<String, List<SyukesekiKinyuData>>>();
        Map<String, List<SyukesekiKinyuData>> examhallMap = new LinkedHashMap<String, List<SyukesekiKinyuData>>();
        List<SyukesekiKinyuData> retList = new ArrayList<SyukesekiKinyuData>();

        try {
            final String syukesekiKinyuSql = getSyukesekiKinyuSql();
            log.debug(" sql =" + syukesekiKinyuSql);
            ps = db2.prepareStatement(syukesekiKinyuSql);
            rs = ps.executeQuery();

            syukesekiKinyuList.add(examhallMap);
            while (rs.next()) {
                final String examhallCd = rs.getString("EXAMHALLCD");
                final String examhallName = rs.getString("EXAMHALL_NAME");
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String judgeDiv = rs.getString("JUDGEDIV");

                final SyukesekiKinyuData syukesekiKinyuData = new SyukesekiKinyuData(examhallName, receptNo, name, sex, judgeDiv);

                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    examhallMap = new LinkedHashMap<String, List<SyukesekiKinyuData>>();
                    syukesekiKinyuList.add(examhallMap);
                }

                if (examhallMap.containsKey(examhallCd)) {
                    retList = examhallMap.get(examhallCd);
                } else {
                    retList = new ArrayList<SyukesekiKinyuData>();
                    examhallMap.put(examhallCd, retList);
                }
                retList.add(syukesekiKinyuData);

                lineCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return syukesekiKinyuList;
    }

    private List<Map<String, List<SeisekiKinyuData>>> getSeisekiKinyuList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final int maxLine = SEISEKI_KINYUHYOU_LINE_MAX * SEISEKI_KINYUHYOU_COL_MAX;
        int lineCnt = 1;

        List<Map<String, List<SeisekiKinyuData>>> seisekiKinyuList = new ArrayList<Map<String, List<SeisekiKinyuData>>>();
        Map<String, List<SeisekiKinyuData>> examhallMap = new LinkedHashMap<String, List<SeisekiKinyuData>>();
        List<SeisekiKinyuData> retList = new ArrayList<SeisekiKinyuData>();

        try {
            final String seisekiKinyuSql = getSeisekiKinyuSql();
            log.debug(" sql =" + seisekiKinyuSql);
            ps = db2.prepareStatement(seisekiKinyuSql);
            rs = ps.executeQuery();

            seisekiKinyuList.add(examhallMap);
            while (rs.next()) {
                final String examhallCd = rs.getString("EXAMHALLCD");
                final String examhallName = rs.getString("EXAMHALL_NAME");
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String judgeDiv = rs.getString("JUDGEDIV");

                final SeisekiKinyuData seisekiKinyuData = new SeisekiKinyuData(examhallName, receptNo, name, sex, judgeDiv);

                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    examhallMap = new LinkedHashMap<String, List<SeisekiKinyuData>>();
                    seisekiKinyuList.add(examhallMap);
                }
                if (examhallMap.containsKey(examhallCd)) {
                    retList = examhallMap.get(examhallCd);
                } else {
                    retList = new ArrayList<SeisekiKinyuData>();
                    examhallMap.put(examhallCd, retList);
                }
                retList.add(seisekiKinyuData);

                lineCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return seisekiKinyuList;
    }

    private List<TackSealData> getTackSealList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<TackSealData> tackSealList = new ArrayList<TackSealData>();

        try {
            final String tackSealSql = getTackSealSql();
            log.debug(" sql =" + tackSealSql);
            ps = db2.prepareStatement(tackSealSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examhallName = rs.getString("EXAMHALL_NAME");
                final String receptNo = rs.getString("RECEPTNO");

                final TackSealData tackSealData = new TackSealData(examhallName, receptNo);
                tackSealList.add(tackSealData);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return tackSealList;
    }

    /**
     * ENTEXAM_SETTING_MST の SETTING_CD = 'L008' から取得
     * @return
     */
    private String getTestSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("      SEQ AS TESTSUBCLASSCD, ");
        stb.append("      NAME1 AS TESTSUBCLASS_NAME ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_SETTING_MST ");
        stb.append("  WHERE ");
        stb.append("      SETTING_CD = 'L008' ");
        stb.append("      AND ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("      AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("  ORDER BY ");
        stb.append("      VALUE(TESTSUBCLASSCD, 0) ");
        return stb.toString();
    }

    /**
     * ENTEXAM_TESTSUBCLASSCD_DAT から取得
     * @return
     */
    private String getTestSubclassSql2() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("      TESTSUBCLASSCD, ");
        stb.append("      TESTSUBCLASS_NAME ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_TESTSUBCLASSCD_DAT ");
        stb.append("  WHERE ");
        stb.append("      ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("      AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND TESTDIV = '" + _param._testDiv + "' ");
        stb.append("      AND EXAM_TYPE = '1' ");
        stb.append("      AND TESTSUBCLASS_NAME IS NOT NULL ");
        stb.append("  ORDER BY ");
        stb.append("      VALUE(TESTSUBCLASSCD, 0) ");
        return stb.toString();
    }

    private String getQualificationsSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("      SEQ AS QUALIFICATIONSCD, ");
        stb.append("      NAME1 AS QUALIFICATIONSNAME ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_SETTING_MST");
        stb.append("  WHERE ");
        stb.append("      ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("      AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND SETTING_CD = 'L026' ");
        stb.append("  ORDER BY ");
        stb.append("      VALUE(QUALIFICATIONSCD, 0) ");
        return stb.toString();
    }

    private String getShigansyaIchiranhyouSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("      RECEPT.RECEPTNO, ");
        stb.append("      BASE.NAME_KANA, ");
        stb.append("      BASE.NAME, ");
        stb.append("      Z002.NAME2 AS SEX, ");
        stb.append("      VARCHAR_FORMAT(BASE.BIRTHDAY, 'yyyy/MM/dd') AS BIRTHDAY, ");
        stb.append("      FS.FINSCHOOL_NAME, ");
        stb.append("      L015.ABBV1 AS FINSCHOOL_DIV_NAME, ");
        stb.append("      CONF.CONFIDENTIAL_RPT01, ");
        stb.append("      CONF.CONFIDENTIAL_RPT02, ");
        stb.append("      CONF.CONFIDENTIAL_RPT03, ");
        stb.append("      CONF.CONFIDENTIAL_RPT04, ");
        stb.append("      CONF.CONFIDENTIAL_RPT05, ");
        stb.append("      CONF.CONFIDENTIAL_RPT06, ");
        stb.append("      CONF.CONFIDENTIAL_RPT07, ");
        stb.append("      CONF.CONFIDENTIAL_RPT08, ");
        stb.append("      CONF.CONFIDENTIAL_RPT09, ");
        stb.append("      CONF.CONFIDENTIAL_RPT10, ");
        stb.append("      CONF.CONFIDENTIAL_RPT11, ");
        stb.append("      CONF.CONFIDENTIAL_RPT12, ");
        stb.append("      CONF.TOTAL5, ");
        stb.append("      CONF.TOTAL3, ");
        stb.append("      CONF.TOTAL_ALL, ");
        stb.append("      QUALIFIED_1.QUALIFIED_ABBV AS QUALIFIED_ABBV1, ");
        stb.append("      QUALIFIED_2.QUALIFIED_ABBV AS QUALIFIED_ABBV2, ");
        stb.append("      QUALIFIED_3.QUALIFIED_ABBV AS QUALIFIED_ABBV3, ");
        stb.append("      QUALIFIED_4.QUALIFIED_ABBV AS QUALIFIED_ABBV4, ");
        stb.append("      QUALIFIED_5.QUALIFIED_ABBV AS QUALIFIED_ABBV5, ");
        stb.append("      QUALIFIED_6.QUALIFIED_ABBV AS QUALIFIED_ABBV6, ");
        stb.append("      QUALIFIED_7.QUALIFIED_ABBV AS QUALIFIED_ABBV7, ");
        stb.append("      CONF.SPECIALACTREC, ");
        stb.append("      CONF.ABSENCE_DAYS3, ");
        stb.append("      BASE.RECOM_EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("       AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("       AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("       AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON FS.FINSCHOOLCD = BASE.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L015 ON L015.SETTING_CD = 'L015' ");
        stb.append("          AND L015.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND L015.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND L015.SEQ = FS.FINSCHOOL_DIV ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D005 ON BASE_D005.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND BASE_D005.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND BASE_D005.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND BASE_D005.SEQ = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND CONF.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND CONF.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D031 ON BASE_D031.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND BASE_D031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND BASE_D031.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND BASE_D031.SEQ = '031' ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_1 ON QUALIFIED_1.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_1.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_1.QUALIFIED_CD = '1' ");
        stb.append("          AND QUALIFIED_1.QUALIFIED_JUDGE_CD = BASE_D031.REMARK1 ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_2 ON QUALIFIED_2.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_2.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_2.QUALIFIED_CD = '2' ");
        stb.append("          AND QUALIFIED_2.QUALIFIED_JUDGE_CD = BASE_D031.REMARK2 ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_3 ON QUALIFIED_3.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_3.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_3.QUALIFIED_CD = '3' ");
        stb.append("          AND QUALIFIED_3.QUALIFIED_JUDGE_CD = BASE_D031.REMARK3 ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_4 ON QUALIFIED_4.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_4.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_4.QUALIFIED_CD = '4' ");
        stb.append("          AND QUALIFIED_4.QUALIFIED_JUDGE_CD = BASE_D031.REMARK4 ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_5 ON QUALIFIED_5.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_5.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_5.QUALIFIED_CD = '5' ");
        stb.append("          AND QUALIFIED_5.QUALIFIED_JUDGE_CD = BASE_D031.REMARK5 ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_6 ON QUALIFIED_6.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_6.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_6.QUALIFIED_CD = '6' ");
        stb.append("          AND QUALIFIED_6.QUALIFIED_JUDGE_CD = BASE_D031.REMARK6 ");
        stb.append("     LEFT JOIN ENTEXAM_QUALIFIED_MST QUALIFIED_7 ON QUALIFIED_7.ENTEXAMYEAR = BASE_D031.ENTEXAMYEAR ");
        stb.append("          AND QUALIFIED_7.APPLICANTDIV = BASE_D031.APPLICANTDIV ");
        stb.append("          AND QUALIFIED_7.QUALIFIED_CD = '7' ");
        stb.append("          AND QUALIFIED_7.QUALIFIED_JUDGE_CD = BASE_D031.REMARK7 ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        // 「出身校立区分」 が 1:共通 以外の場合に抽出条件に加える
        if (!"1".equals(_param._passSchoolCd)) {
            stb.append("     AND BASE.FS_NATPUBPRIDIV = '" + _param._passSchoolCd + "'");
        }
        // 「入試種別」 が ALL:全て 以外の場合に抽出条件に加える
        if (!"ALL".equals(_param._kindDiv)) {
            stb.append("     AND BASE_D005.REMARK1 = '" + _param._kindDiv + "' ");
        }
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getSyukesekiKinyuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     RECEPT.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_HALL_LIST_YDAT HALLLIST ON HALLLIST.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND HALLLIST.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND HALLLIST.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND HALLLIST.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = HALLLIST.ENTEXAMYEAR ");
        stb.append("          AND HALL.APPLICANTDIV = HALLLIST.APPLICANTDIV ");
        stb.append("          AND HALL.TESTDIV = HALLLIST.TESTDIV ");
        stb.append("          AND HALL.EXAM_TYPE = HALLLIST.EXAM_TYPE ");
        stb.append("          AND HALL.EXAMHALLCD = HALLLIST.EXAMHALLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND Z002.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("     AND HALLLIST.EXAM_TYPE = '" + _param._syukessekiHanteiHou + "' ");
        if (!"ALL".equals(_param._hallcd1)) {
            stb.append("     AND HALLLIST.EXAMHALLCD = '" + _param._hallcd1 + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     EXAMHALLCD, ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getSeisekiKinyuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     RECEPT.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_HALL_LIST_YDAT HALLLIST ON HALLLIST.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND HALLLIST.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND HALLLIST.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND HALLLIST.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = HALLLIST.ENTEXAMYEAR ");
        stb.append("          AND HALL.APPLICANTDIV = HALLLIST.APPLICANTDIV ");
        stb.append("          AND HALL.TESTDIV = HALLLIST.TESTDIV ");
        stb.append("          AND HALL.EXAM_TYPE = HALLLIST.EXAM_TYPE ");
        stb.append("          AND HALL.EXAMHALLCD = HALLLIST.EXAMHALLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND Z002.YEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND Z002.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        // 会場を 1:筆記、2:面接、3:作文 で切り分けるために、ENTEXAM_HALL_LIST_YDAT(HALLLIST) の EXAM_TYPE を流用して使っている。
        // 本来の用途とは違う使い方のため、他のテーブルの EXAM_TYPE と結合させない。
        stb.append("     AND HALLLIST.EXAM_TYPE = '" + _param._score + "' ");
        if (!"ALL".equals(_param._hallcd2)) {
            stb.append("     AND HALLLIST.EXAMHALLCD = '" + _param._hallcd2 + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     EXAMHALLCD, ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    private String getTackSealSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     RECEPT.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_HALL_LIST_YDAT HALLLIST ON HALLLIST.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND HALLLIST.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND HALLLIST.TESTDIV = RECEPT.TESTDIV ");
        stb.append("           AND HALLLIST.RECEPTNO = RECEPT.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON HALL.ENTEXAMYEAR = HALLLIST.ENTEXAMYEAR ");
        stb.append("          AND HALL.APPLICANTDIV = HALLLIST.APPLICANTDIV ");
        stb.append("          AND HALL.TESTDIV = HALLLIST.TESTDIV ");
        stb.append("          AND HALL.EXAM_TYPE = HALLLIST.EXAM_TYPE ");
        stb.append("          AND HALL.EXAMHALLCD = HALLLIST.EXAMHALLCD ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        // 会場を 1:筆記、2:面接、3:作文 で切り分けるために、ENTEXAM_HALL_LIST_YDAT(HALLLIST) の EXAM_TYPE を流用して使っている。
        // 本来の用途とは違う使い方のため、他のテーブルの EXAM_TYPE と結合させない。
        stb.append("     AND HALLLIST.EXAM_TYPE = '1' ");
        if (!"ALL".equals(_param._hallcd3)) {
            stb.append("     AND HALLLIST.EXAMHALLCD = '" + _param._hallcd3 + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     EXAMHALLCD, ");
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77308 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class TestSubclassData {
        final String _testClassCd;
        final String _testClassName;
        final int _colNumber;

        public TestSubclassData(
                final String testClassCd,
                final String testClassName,
                final int colNumber
        ) {
            _testClassCd = testClassCd;
            _testClassName = testClassName;
            _colNumber = colNumber;
        }
    }

    private class ShigansyaData {
        final String _receptNo;
        final String _nameKana;
        final String _name;
        final String _sex;
        final String _birthDay;
        final String _finschoolName;
        final String _finschoolDivName;
        final String _confidentialRpto1;
        final String _confidentialRpto2;
        final String _confidentialRpto3;
        final String _confidentialRpto4;
        final String _confidentialRpto5;
        final String _confidentialRpto6;
        final String _confidentialRpto7;
        final String _confidentialRpto8;
        final String _confidentialRpto9;
        final String _total5;
        final String _total3;
        final String _qualifiedAbbv1;
        final String _qualifiedAbbv2;
        final String _qualifiedAbbv3;
        final String _qualifiedAbbv4;
        final String _qualifiedAbbv5;
        final String _qualifiedAbbv6;
        final String _qualifiedAbbv7;
        final String _totalAll;
        final String _specialactrec;
        final String _absenceDays3;
        final String _recomExamno;

        public ShigansyaData(
                final String receptNo,
                final String nameKana,
                final String name,
                final String sex,
                final String birthDay,
                final String finschoolName,
                final String finschoolDivName,
                final String confidentialRpto1,
                final String confidentialRpto2,
                final String confidentialRpto3,
                final String confidentialRpto4,
                final String confidentialRpto5,
                final String confidentialRpto6,
                final String confidentialRpto7,
                final String confidentialRpto8,
                final String confidentialRpto9,
                final String total5,
                final String total3,
                final String qualifiedAbbv1,
                final String qualifiedAbbv2,
                final String qualifiedAbbv3,
                final String qualifiedAbbv4,
                final String qualifiedAbbv5,
                final String qualifiedAbbv6,
                final String qualifiedAbbv7,
                final String totalAll,
                final String specialactrec,
                final String absenceDays3,
                final String recomExamno
        ) {
            _receptNo = receptNo;
            _nameKana = nameKana;
            _name = name;
            _sex = sex;
            _birthDay = birthDay;
            _finschoolName = finschoolName;
            _finschoolDivName = finschoolDivName;
            _confidentialRpto1 = confidentialRpto1;
            _confidentialRpto2 = confidentialRpto2;
            _confidentialRpto3 = confidentialRpto3;
            _confidentialRpto4 = confidentialRpto4;
            _confidentialRpto5 = confidentialRpto5;
            _confidentialRpto6 = confidentialRpto6;
            _confidentialRpto7 = confidentialRpto7;
            _confidentialRpto8 = confidentialRpto8;
            _confidentialRpto9 = confidentialRpto9;
            _total5 = total5;
            _total3 = total3;
            _qualifiedAbbv1 = qualifiedAbbv1;
            _qualifiedAbbv2 = qualifiedAbbv2;
            _qualifiedAbbv3 = qualifiedAbbv3;
            _qualifiedAbbv4 = qualifiedAbbv4;
            _qualifiedAbbv5 = qualifiedAbbv5;
            _qualifiedAbbv6 = qualifiedAbbv6;
            _qualifiedAbbv7 = qualifiedAbbv7;
            _totalAll = totalAll;
            _specialactrec = specialactrec;
            _absenceDays3 = absenceDays3;
            _recomExamno = recomExamno;
        }
    }

    private class SyukesekiKinyuData {
        final String _examhallName;
        final String _receptNo;
        final String _name;
        final String _sex;
        final String _judgeDiv;

        public SyukesekiKinyuData(
                final String examhallName,
                final String receptNo,
                final String name,
                final String sex,
                final String judgeDiv
        ) {
            _examhallName = examhallName;
            _receptNo = receptNo;
            _name = name;
            _sex = sex;
            _judgeDiv = judgeDiv;
        }
    }

    private class SeisekiKinyuData {
        final String _examhallName;
        final String _receptNo;
        final String _name;
        final String _sex;
        final String _judgeDiv;

        public SeisekiKinyuData(
                final String examhallName,
                final String receptNo,
                final String name,
                final String sex,
                final String judgeDiv
        ) {
            _examhallName = examhallName;
            _receptNo = receptNo;
            _name = name;
            _sex = sex;
            _judgeDiv = judgeDiv;
        }
    }

    private class TackSealData {
        final String _examhallName;
        final String _receptNo;

        public TackSealData(
                final String examhallName,
                final String receptNo
        ) {
            _examhallName = examhallName;
            _receptNo = receptNo;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _kindDiv;
        private final String _passSchoolCd;
        private final String _check;
        private final String _output;
        private final String _syukessekiHanteiHou;
        private final String _hallcd1;
        private final String _score;
        private final String _hallcd2;
        private final String _hallcd3;
        private final String _schoolName;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _kindDiv = request.getParameter("KINDDIV");
            _passSchoolCd = request.getParameter("PASS_SCHOOL_CD");
            _check = request.getParameter("CHECK");
            _output = request.getParameter("OUTPUT");
            _syukessekiHanteiHou = request.getParameter("SYUKESSEKI_HANTEI_HOU");
            _hallcd1 = request.getParameter("HALLCD1");
            _score = request.getParameter("SCORE");
            _hallcd2 = request.getParameter("HALLCD2");
            _hallcd3 = request.getParameter("HALLCD3");
            _schoolName = getSchoolName(db2);
            _testDivName = getTestDivName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
            String sqlwk = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "'";
            String sql = "1".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '105' ") : "2".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '106' ") : "";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_ABBV");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof

