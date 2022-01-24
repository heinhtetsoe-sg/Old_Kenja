// kanji=漢字
/*
 * $Id: f7abb79fd298071b124f3ae0ad3028f870eda7c0 $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 成績不振者一覧(スクーリング・レポート)
 * @author nakada
 * @version $Id: f7abb79fd298071b124f3ae0ad3028f870eda7c0 $
 */
public class KNJWD760 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWD760.class);
    private static final String FORM_FILE = "KNJWD760.frm";

    /* 
     * 文字数による出力項目切り分け基準 
     */
    /** 担任 */
    private static final int STAFF_LENG = 10;
    /** 名前 */
    private static final int NAME_LENG = 10;
    /** 科目名 */
    private static final int SUBCLASSNAME_LENG = 10;

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 30;
    private int _dataCnt;
    private int _detailCnt;
    private int _page;
    private boolean _LINEFLG1 = false;  // 担任欄罫線制御フラグ
    private boolean _LINEFLG2 = false;  // 生徒氏名欄罫線制御フラグ

    /*
     * スクーリング種別
     */
    /** 登校：01 */
    private static final String SCHOOLING_DIV_01 = "01";
    /** 代替：02 */
    private static final String SCHOOLING_DIV_02 = "02";

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        _param = createParam(request);
        _form = new Form(FORM_FILE, response, _svf);
        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            for (int i = 0; i < _param._grade.length; i++) {
                log.debug(">>所属=" + _param._grade[i]);
                log.debug(">>スクーリング登校実績率=" + _param._rate1);
                log.debug(">>スクーリング代替実績率=" + _param._rate2);
                log.debug(">>レポート提出実績率=" + _param._rate3);

                final List students = createSchregRegdDat(db2, _param._grade[i]);
                printMain(students, i);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List students, int i) throws SQLException {
        _form._svf.VrAttribute( "RECORD1", "Print=1");
        _form._svf.VrAttribute( "RECORD2", "Print=0");
        _form._svf.VrAttribute( "SCHOOLNAME", "FF=1");

        _page = 0;
        _detailCnt = 0;

        String[] classcdArray;         // 当年度の教科コードリスト
        int classcdArrayNum;           // _classcdArrayの格納件数
        String[][] subclassArray;      // 当年度の科目コードリスト（[教科][科目]）
        int subclassArrayNum;          // _subclassArrayの格納件数（最大件数）
        String[][][] curriculumArray;  // 当年度の教育課程コードリスト（[教科][科目][教育課程]）
        int curriculumArrayNum;        // _curriculumArrayの格納件数（最大件数）
        
        String oldClass = "";
        String oldStaffCd = "";
        String oldStaffName = null;
        String oldStudentCd = null;
        String staffNameFlg = "";
        String studentNameFlg = "";

        SchregRegdDat schregRegdDat = null;

        for (Iterator it = students.iterator(); it.hasNext();) {
            schregRegdDat = (SchregRegdDat) it.next();

            // 学生の教科、科目、教育課程のコード＋名称のリスト（全科目）を取得
            final List compCreditTitles = createCompCreditTitleList(schregRegdDat._applicantno, schregRegdDat._schregno);

            // 上記のリストを多次元配列（基本マップ）化
            final List list = createCompCreditArray(compCreditTitles);
            classcdArray = (String[]) list.get(0);
            classcdArrayNum = ((Integer) list.get(1)).intValue();

            subclassArray= (String[][]) list.get(2);
            subclassArrayNum = ((Integer) list.get(3)).intValue();

            curriculumArray = (String[][][]) list.get(4);
            curriculumArrayNum = ((Integer) list.get(5)).intValue();

            // 通信スクーリング実績取得
            final List recSchoolingDats = createRecSchoolingDats(db2, schregRegdDat._schregno);

            // 通信スクーリング割合取得
            final List recSchoolingRateDats = createRecSchoolingRateDatt(db2, schregRegdDat._schregno);
            
            // レポート実績取得
            final List recReportDats = createRecReportDats(db2, schregRegdDat._schregno);

            // スクーリング及びレポートの実施/基準/残情報取得
            final List results = setArrayCompCredit(
                    schregRegdDat,
                    classcdArray,
                    classcdArrayNum,
                    subclassArray,
                    subclassArrayNum,
                    curriculumArray,
                    curriculumArrayNum,
                    recSchoolingDats,
                    recReportDats,
                    recSchoolingRateDats);

            if (!results.isEmpty()) {
                if ((oldClass.equals("")) || (!schregRegdDat._hrClass.equals(oldClass))) {
                    if (!oldClass.equals("")) {
                        printDummyLine();
                        _detailCnt = 0;
                    }

                    printHeader(i);
                    oldClass = schregRegdDat._hrClass;
                }

                String str1 = _param.getStaffCd(i, schregRegdDat._hrClass);
                if (!str1.equals(oldStaffCd)) {
                    oldStaffCd = str1;
                    staffNameFlg = getLineFlgNum();
                }

                studentNameFlg = getLineFlgNum2();
                printApplicant(schregRegdDat, list, results, oldStaffName, staffNameFlg, oldStudentCd, studentNameFlg, i);

                _hasData = true;
            }
        }

        if (_detailCnt <= DETAILS_MAX && _hasData) {
            if (_detailCnt == DETAILS_MAX ) {
                _detailCnt = 0;
            }
            printDummyLine();
        }
    }

    private void printHeader(final int i) {
        // 年度
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/01/01") + "度");

        // 所属
        final String schoolName = (String) _param._belonging.get(_param._grade[i]);
        if (null != schoolName) {
            _form._svf.VrsOut("SCHOOLNAME", schoolName);
        }

        // 作成日
        _form._svf.VrsOut("DATE", _param._jDate);

        // ページ
        _form._svf.VrsOut("PAGE", Integer.toString(++_page));
    }

    private void printApplicant(
            SchregRegdDat schregRegdDat,
            List compCreditTitles,
            List results,
            String oldStaffName,
            String staffNameFlg,
            String oldStudentCd,
            String studentNameFlg,
            final int i
    ) throws SQLException {

        String[] classcdArray;         // 当年度の教科コードリスト
        int classcdArrayNum;           // _classcdArrayの格納件数
        String[][] subclassArray;      // 当年度の科目コードリスト（[教科][科目]）
        int subclassArrayNum;          // _subclassArrayの格納件数（最大件数）
        String[][][] curriculumArray;  // 当年度の教育課程コードリスト（[教科][科目][教育課程]）
        int curriculumArrayNum;        // _curriculumArrayの格納件数（最大件数）

        classcdArray = (String[]) compCreditTitles.get(0);
        classcdArrayNum = ((Integer) compCreditTitles.get(1)).intValue();

        subclassArray= (String[][]) compCreditTitles.get(2);
        subclassArrayNum = ((Integer) compCreditTitles.get(3)).intValue();

        curriculumArray = (String[][][]) compCreditTitles.get(4);
        curriculumArrayNum = ((Integer) compCreditTitles.get(5)).intValue();

        boolean[][][] compResultsSet1 = (boolean[][][]) results.get(1);
        boolean[][][] compResultsSet2 = (boolean[][][]) results.get(2);
        boolean[][][] compResultsSet3 = (boolean[][][]) results.get(3);
        List[][][] rtn2 = (List[][][]) results.get(4);

        for (int j = 0; j < classcdArrayNum; j++) {
            for (int k = 0; k < subclassArrayNum; k++) {
                for (int m = 0; m < curriculumArrayNum; m++) {
                    if (compResultsSet1[j][k][m] || compResultsSet2[j][k][m] || compResultsSet3[j][k][m]) {

                        oldStaffName = printDetail(
                                schregRegdDat,
                                oldStaffName,
                                staffNameFlg,
                                studentNameFlg,
                                i,
                                classcdArray,
                                subclassArray,
                                curriculumArray,
                                compResultsSet1,
                                compResultsSet2,
                                compResultsSet3,
                                rtn2,
                                j,
                                k,
                                m);
                    }
                }
            }
        }
    }

    private String printDetail(
            SchregRegdDat schregRegdDat,
            String oldStaffName,
            String staffNameFlg,
            String studentNameFlg,
            final int i,
            String[] classcdArray,
            String[][] subclassArray,
            String[][][] curriculumArray,
            boolean[][][] compResultsSet1,
            boolean[][][] compResultsSet2,
            boolean[][][] compResultsSet3,
            List[][][] rtn2,
            int j,
            int k,
            int m
    ) {
        _detailCnt++;
        if (_detailCnt > DETAILS_MAX) {
            printHeader(i);
            oldStaffName = _param.getStaffName(_param._year
                    + _param._semester
                    + _param._grade[i]
                    + schregRegdDat._hrClass);

            _detailCnt = 1;
        } else {
            if (oldStaffName == null) {
                oldStaffName =
                    _param.getStaffName(_param._year
                            + _param._semester
                            + _param._grade[i]
                            + schregRegdDat._hrClass);
            }
        }

        /* 担任罫線制御 */
        _form._svf.VrsOut("FLG1", staffNameFlg);

        /* 担任 */
        _form.printStaffName(oldStaffName);

        /* 学籍番号 */
        _form._svf.VrsOut("SCHREGNO", schregRegdDat._schregno);

        /* 生徒氏名罫線制御 */
        _form._svf.VrsOut("FLG2", studentNameFlg);

        /* 生徒氏名 */
        _form.printName(schregRegdDat._name);

        /* 教科名 */
        _form._svf.VrsOut("CLASS_NAME", _param.getClass(classcdArray[j]));

        /* 科目 */
        _form.printSubclassName(_param.getSubclass(
                classcdArray[j] + curriculumArray[j][k][m] + subclassArray[j][k]));

        String commutingDiv = _param.getCommutingDiv(schregRegdDat);
        // 不振データ取得
        Results result = (Results) rtn2[j][k][m].get(0);

        if (compResultsSet1[j][k][m]) {
            /* スクーリング・登校・実施 */
            _form._svf.VrsOut("TIME1", String.valueOf(result._schoolingResults));
            /* スクーリング・登校・基準 */
            _form._svf.VrsOut("STANDARD_TIME1", String.valueOf(result._schoolingStandard));
            /* スクーリング・登校・残 */
            _form._svf.VrsOut("TIME_LEFT1", String.valueOf(result._schoolingDeduction));
        }

        // 通学区分＝1:通学生以外の場合、代替の対象
        if(!commutingDiv.equals("1")){
            if (compResultsSet2[j][k][m]) {
                /* スクーリング・代替・実施 */
                _form._svf.VrsOut("TIME2", String.valueOf(result._schoolingResults2));
                /* スクーリング・代替・基準 */
                _form._svf.VrsOut("STANDARD_TIME2", String.valueOf(result._schoolingStandard2));
                /* スクーリング・代替・残 */
                _form._svf.VrsOut("TIME_LEFT2", String.valueOf(result._schoolingDeduction2));
            }
        }

        if (compResultsSet3[j][k][m]) {
            /* レポート・実施 */
            _form._svf.VrsOut("COUNT", String.valueOf(result._reportResults));
            /* レポート・基準 */
            _form._svf.VrsOut("REPORT_SEQ", String.valueOf(result._reportStandard));
            /* レポート・残 */
            _form._svf.VrsOut("COUNT_LEFT", String.valueOf(result._reportDeduction));
        }

        _form._svf.VrEndRecord();
        return oldStaffName;
    }

    private void printDummyLine() {
        _form._svf.VrEndRecord();

        _form._svf.VrAttribute( "RECORD1", "Print=0");
        _form._svf.VrAttribute( "RECORD2", "Print=1");


        for (int j = (DETAILS_MAX - _detailCnt + 1); j != 0; j--) {
            _form._svf.VrAttribute( "KARA", "Print=1");
            _form._svf.VrsOut("KARA", "空行");
            _form._svf.VrEndRecord();
        }

        _form._svf.VrAttribute( "RECORD1", "Print=1");
        _form._svf.VrAttribute( "RECORD2", "Print=0");

        _form._svf.VrEndRecord();
    }

    private List setArrayCompCredit(
            SchregRegdDat schregRegdDat,
            String[] classcdArray,
            int classcdArrayNum,
            String[][] subclassArray,
            int subclassArrayNum,
            String[][][] curriculumArray,
            int curriculumArrayNum,
            List recSchoolingDats,
            List recReportDats,
            List recSchoolingRateDats
    ) {
        final List rtn = new ArrayList();
        final List[][][] rtn2 = new ArrayList[classcdArrayNum][subclassArrayNum][curriculumArrayNum];
        int dataCnt = 0;

        boolean[][][] compResultsSet1 = new boolean[classcdArrayNum][subclassArrayNum][curriculumArrayNum];
        boolean[][][] compResultsSet2 = new boolean[classcdArrayNum][subclassArrayNum][curriculumArrayNum];
        boolean[][][] compResultsSet3 = new boolean[classcdArrayNum][subclassArrayNum][curriculumArrayNum];

        // 該当する教科、科目、教育課程に履修単位を格納
        for (int i = 0; i < classcdArrayNum; i++) {
            for (int j = 0; j < subclassArrayNum; j++) {
                for (int k = 0; k < curriculumArrayNum; k++) {

                    int rateSum = 0;        // 登校・基準
                    int rateSum2 = 0;       // 代替・基準
                    int rateSumzishi = 0;   // 登校・実施
                    int getValueSum2 = 0;   // 代替・実施
                    int reportSum = 0;      // レポート・実施
                    int recCount = 0;
                    double schoolingResults = 0;
                    double schoolingStandard = 0;
                    double schoolingDeduction = 0;
                    double schoolingResults2 = 0;
                    double schoolingStandard2 = 0;
                    double schoolingDeduction2 = 0;
                    int reportResults = 0;
                    int reportStandard = 0;
                    int reportDeduction = 0;

                    // 通信スクーリング実績データ取得
                    for (Iterator it = recSchoolingDats.iterator(); it.hasNext();) {
                        final RecSchoolingDat recSchoolingDat = (RecSchoolingDat) it.next();

                        if (recSchoolingDat._classcd.equals(classcdArray[i]) &&
                                recSchoolingDat._subclasscd.equals(subclassArray[i][j]) &&
                                recSchoolingDat._curriculumCd.equals(curriculumArray[i][j][k])
                        ) {

                        	if (_param.getSchoolingType(recSchoolingDat._schoolingType).equals(SCHOOLING_DIV_02)) {
                                getValueSum2 += recSchoolingDat._getValue;
                            }

                            if (_param.getSchoolingType(recSchoolingDat._schoolingType).equals(SCHOOLING_DIV_01)) {
                                ++recCount;
                            }
                        }
                    }

                    // 通信スクーリング割合データ取得
                    for (Iterator it = recSchoolingRateDats.iterator(); it.hasNext();) {
                        final RecSchoolingRateDat recSchoolingRateDat = (RecSchoolingRateDat) it.next();

                        if (recSchoolingRateDat._classcd.equals(classcdArray[i]) &&
                        		recSchoolingRateDat._subclasscd.equals(subclassArray[i][j]) &&
                        		recSchoolingRateDat._curriculumCd.equals(curriculumArray[i][j][k])
                        ) {

                            if (_param.getSchoolingType(recSchoolingRateDat._schoolingType).equals(SCHOOLING_DIV_01)) {
                            	// 割合を集計し登校・基準時間とする
                            	rateSum += recSchoolingRateDat._rate;
                                // 完了期間開始日と完了期間終了日に設定のある割合を集計し登校・実施時間とする
                                if (!recSchoolingRateDat._commitedS.equals("") && !recSchoolingRateDat._commitedE.equals("")) {
                                	rateSumzishi += recSchoolingRateDat._rate;
                                }
                            } else if (_param.getSchoolingType(recSchoolingRateDat._schoolingType).equals(SCHOOLING_DIV_02)) {
                                rateSum2 += recSchoolingRateDat._rate;
                            }
                        }
                    }

                    
                    // 年間スクーリング回数取得
                    int db1 = _param.getSubclassDetailsMstMap1Int(classcdArray[i] + subclassArray[i][j] + curriculumArray[i][j][k]);

                    // 登校 ------------------------------------------------------------------------
                    String commutingDiv = _param.getCommutingDiv(schregRegdDat);
                    double rt1 = 0;
                    double db3 = 0;
                    // 通学区分＝1:通学生以外の場合
                    if(!commutingDiv.equals("1")){
                        // 割合計数化
                        rt1 = ((double) rateSum) / 10;
                        // 基準値算出
                        db3 = ((double)db1) * rt1;
                    } else {
                        db3 = (double)db1;
                    }
                    
                    // 画面指定率計数化
                    double db5 = ((double) Integer.parseInt(_param._rate1)) / 100;
                    // ボーダーライン算出
                    double db6 = db3 * db5; // ボーダーライン                    
                    // 成績不良判定
                    double drateSumzishi = 0;
                    if(!commutingDiv.equals("1")){
                        // 通学区分＝1:通学生以外の場合
                        drateSumzishi = db1 * ((double)rateSumzishi / 10);
                    } else {
                        drateSumzishi = (double)recCount;
                    }
                    
                    if (((double)drateSumzishi) < db6) {
                        // 実施
                        schoolingResults = getHour(drateSumzishi).doubleValue();
                        // 基準
                        schoolingStandard = getHour(db3).doubleValue();
                        // 残
                        double db4 = db3 - drateSumzishi;
                        schoolingDeduction = getHour(db4).doubleValue();

                        compResultsSet1[i][j][k] = true;
                    }

                    // 代替 ------------------------------------------------------------------------
                    // 割合計数化
                    double rt21 = ((double) rateSum2) / 10;
                    // 基準値算出
                    double db23 = ((double)db1) * rt21;
                    // 画面指定率計数化
                    double db25 = ((double) Integer.parseInt(_param._rate2)) / 100;
                    // ボーダーライン算出
                    double db26 = db23 * db25; // ボーダーライン                    
                    // 成績不良判定
    				double dgetValueSum2 = (double)getValueSum2 / 50;
                    if (((double)dgetValueSum2) < db26) {
                        // 実施
                        schoolingResults2 = getHour(dgetValueSum2).doubleValue();
                        // 基準
                        schoolingStandard2 = getHour(db23).doubleValue();
                        // 残
                        double db24 = db23 - dgetValueSum2;
                        schoolingDeduction2 = getHour(db24).doubleValue();

                        compResultsSet2[i][j][k] = true;
                    }

                    for (Iterator it = recReportDats.iterator(); it.hasNext();) {
                        final RecReportDat reportDat = (RecReportDat) it.next();

                        if (reportDat._classcd.equals(classcdArray[i]) &&
                                reportDat._subclasscd.equals(subclassArray[i][j]) &&
                                reportDat._curriculumCd.equals(curriculumArray[i][j][k])
                        ) {
                            reportSum += reportDat.sum();
                        }
                    }

                    // 年間レポート回数取得
                    int db37 = _param.getSubclassDetailsMstMap2Int(classcdArray[i]
                                      + subclassArray[i][j]
                                      + curriculumArray[i][j][k]);
                    // 画面指定率計数化
                    double db35 = ((double) Integer.parseInt(_param._rate3)) / 100;
                    // ボーダーライン算出
                    double db36 = (((double) db37) * db35);
                    BigDecimal bd = new BigDecimal(db36);
                    int db38 = bd.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                    // 成績不良判定
                    if (((double)reportSum) < db38) {
                        // 実施
                        reportResults = reportSum;
                        // 基準
                        reportStandard = db37;
                        // 残
                        reportDeduction = reportStandard - reportSum;

                        compResultsSet3[i][j][k] = true;
                    }

                    if (compResultsSet1[i][j][k] || compResultsSet2[i][j][k] || compResultsSet3[i][j][k]) {
                        Results results = new Results(
                                schoolingResults,
                                schoolingStandard,
                                schoolingDeduction,
                                schoolingResults2,
                                schoolingStandard2,
                                schoolingDeduction2,
                                reportResults,
                                reportStandard,
                                reportDeduction
                                );

                        rtn2[i][j][k] = new ArrayList();
                        rtn2[i][j][k].add(results);
                        dataCnt++;
                    }
                }
            }
        }

        if (dataCnt > 0) {
            rtn.add(new Integer(dataCnt));
            rtn.add(compResultsSet1);
            rtn.add(compResultsSet2);
            rtn.add(compResultsSet3);
            rtn.add(rtn2);
        }

        return rtn;
    }

    private String getLineFlgNum() {
        if (_LINEFLG1) {
            _LINEFLG1 = false;
            return "1";
        } else {
            _LINEFLG1 = true;
            return "2";
        }
    }

    private String getLineFlgNum2() {
        if (_LINEFLG2) {
            _LINEFLG2 = false;
            return "1";
        } else {
            _LINEFLG2 = true;
            return "2";
        }
    }

    private static BigDecimal getHour(int minutes) {
        double hour = ((double) minutes) / 60;

        BigDecimal bd = new BigDecimal(String.valueOf(hour));

        return bd.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private static BigDecimal getHour(double minutes) {
        BigDecimal bd = new BigDecimal(String.valueOf(minutes));

        return bd.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private List createCompCreditTitleList (String applicantno, String schregno) throws SQLException {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlCompCreditTitleList(applicantno, schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscd = rs.getString("subclasscd");

            final CompCreditTitle compCreditTitle = new CompCreditTitle(
                    classcd,
                    _param.getClass(classcd),
                    curriculumCd,
                    subclasscd,
                    _param.getSubclass(classcd + curriculumCd + subclasscd)
            );

            rtn.add(compCreditTitle);
        }

        return rtn;
    }

    private String sqlCompCreditTitleList(String applicantno, String schregno) {
        return " select"
                + "    CLASSCD as classcd,"
                + "    CURRICULUM_CD as curriculumCd,"
                + "    SUBCLASSCD as subclasscd"
                + " from"
                + "    ANOTHER_SCHOOL_GETCREDITS_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "'"
                + "    and APPLICANTNO = '" + applicantno + "'"
        
                + " union"
        
                + " select"
                + "    CLASSCD as classcd,"
                + "    CURRICULUM_CD as curriculumCd,"
                + "    SUBCLASSCD as subclasscd"
                + " from"
                + "    COMP_REGIST_DAT"
                + " where"
                + "    YEAR = '" + _param._year + "'"
                + "    and SCHREGNO = '" + schregno + "'"
                + " order by classcd, subclasscd, curriculumCd desc"
                ;
    }

    private List createCompCreditArray(final List list) {
        final List rtn = new ArrayList();

        int classNum = 0;

        int subclassNum = 0;
        int subclassMaxNum = 0;

        int curriculumNum = 0;
        int curriculumMaxNum = 0;

        String oldClasscd = "";
        String oldSubclasscd = "";
        String oldCurriculumCd = "";

        CompCreditTitle cct = null;
        if (!list.isEmpty()) {
            // 最初のの教科、科目、教育課程リストを取得
            cct = (CompCreditTitle) list.get(0);
        }

        int i = 0;
        // 当年度の教科、科目、教育課程の各件数を取得
        classNum = 0;
        while (!list.isEmpty() && i < list.size()) {
            classNum++;

            oldClasscd = cct._classcd;
            subclassNum = 0;
            while (i < list.size() && cct._classcd.equals(oldClasscd)) {
                // 同一教科の中に異なる科目が幾つあるか？
                if (!cct._subclasscd.equals(oldSubclasscd)) {
                    subclassNum++;
                }

                oldSubclasscd = cct._subclasscd;
                curriculumNum = 0;
                while (i < list.size() && cct._classcd.equals(oldClasscd) && cct._subclasscd.equals(oldSubclasscd)) {
                    // 同一教科、科目の中に異なる教育課程が幾つあるか？
                    if (!cct._curriculumCd.equals(oldCurriculumCd)) {
                        curriculumNum++;
                    }

                    oldCurriculumCd = cct._curriculumCd;
                    while (i < list.size() && cct._classcd.equals(oldClasscd) && cct._subclasscd.equals(oldSubclasscd) && cct._curriculumCd.equals(oldCurriculumCd)) {
                        i++;
                        if (i < list.size()) {
                            // 次の教科、科目、教育課程リストを取得
                            cct = (CompCreditTitle) list.get(i);
                        }
                    }
                }

                // 教育課程の最大件数を取得
                if (curriculumNum > curriculumMaxNum) {
                    curriculumMaxNum = curriculumNum;
                }
            }            

            // 科目の最大件数を取得
            if (subclassNum > subclassMaxNum) {
                subclassMaxNum = subclassNum;
            }
        }

        // 上記で確定された、当年度の教科、科目、教育課程の各配列数で配列を定義し、
        // 実際の当年度の教科、科目、教育課程の配列を作成。
        // ※科目、教育課程は、最大件数で、一律作成。
        // ※この配列（いわゆる3次元マップ）を基に、
        // 　所属別年次別学生別に履修単位を集計する。
        String[] classcdArray = new String[classNum];
        String[][] subclassArray = new String[classNum][subclassMaxNum];
        String[][][] curriculumArray = new String[classNum][subclassMaxNum][curriculumMaxNum];

        if (!list.isEmpty()) {
            cct = (CompCreditTitle) list.get(0);
        }

        i = 0;
        int j = 0;  // 教科
        int k = 0;  // 科目
        int m = 0;  // 教育課程

        while (!list.isEmpty() && i < list.size()) {
            classcdArray[j] = cct._classcd;

            oldClasscd = cct._classcd;
            // 同一　教科の間繰り返し処理する。
            while (i < list.size() && cct._classcd.equals(oldClasscd)) {
                subclassArray[j][k] = cct._subclasscd;

                oldSubclasscd = cct._subclasscd;
                // 同一　教科コード＋科目コードの間繰り返し処理する。
                while (i < list.size() && cct._classcd.equals(oldClasscd) && cct._subclasscd.equals(oldSubclasscd)) {
                    curriculumArray[j][k][m] = cct._curriculumCd;

                    oldCurriculumCd = cct._curriculumCd;
                    // 同一　教科コード＋科目コード＋教育課程コードの間処理する。
                    while (i < list.size() && cct._classcd.equals(oldClasscd) && cct._subclasscd.equals(oldSubclasscd) && cct._curriculumCd.equals(oldCurriculumCd)) {
                        // 次の教科、科目、教育課程リストを取得
                        i++;
                        if (i < list.size()) {
                            cct = (CompCreditTitle) list.get(i);
                        }
                    }
                    m++;    // 教育課程の件数カウント
                }
                m = 0;
                k++;// 科目の件数カウント
            }            
            k = 0;
            j++;// 教科の件数カウント
        }

        rtn.add(classcdArray);
        rtn.add(new Integer(classNum));
        rtn.add(subclassArray);
        rtn.add(new Integer(subclassMaxNum));
        rtn.add(curriculumArray);
        rtn.add(new Integer(curriculumMaxNum));

        return rtn;
    }

    private class CompCreditTitle {
        private final String _classcd;
        private final String _className;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final String _subclassName;

        CompCreditTitle(
                final String classcd,
                final String className,
                final String curriculumCd,
                final String subclasscd,
                final String subclassName
        ) {
            _classcd = classcd;
            _className = className;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassName = subclassName;
        }
    }

    private class Results {
        private final double _schoolingResults;     // スクーリング・登校・実施(単位：時間)
        private final double _schoolingStandard;    // スクーリング・登校・基準(単位：時間)
        private final double _schoolingDeduction;   // スクーリング・登校・残(単位：時間)
        private final double _schoolingResults2;    // スクーリング・代替・実施(単位：時間)
        private final double _schoolingStandard2;   // スクーリング・代替・基準(単位：時間)
        private final double _schoolingDeduction2;  // スクーリング・代替・残(単位：時間)
        private final int _reportResults;        // レポート・実施
        private final int _reportStandard;       // レポート・基準
        private final int _reportDeduction;      // レポート・残

        Results() {
            _schoolingResults = 0;
            _schoolingStandard = 0;
            _schoolingDeduction = 0;
            _schoolingResults2 = 0;
            _schoolingStandard2 = 0;
            _schoolingDeduction2 = 0;
            _reportResults = 0;
            _reportStandard = 0;
            _reportDeduction = 0;
        }

        Results(
                final double schoolingResults,
                final double schoolingStandard,
                final double schoolingDeduction,
                final double schoolingResults2,
                final double schoolingStandard2,
                final double schoolingDeduction2,
                final int reportResults,
                final int reportStandard,
                final int reportDeduction
        ) {
            _schoolingResults = schoolingResults;
            _schoolingStandard = schoolingStandard;
            _schoolingDeduction = schoolingDeduction;
            _schoolingResults2 = schoolingResults2;
            _schoolingStandard2 = schoolingStandard2;
            _schoolingDeduction2 = schoolingDeduction2;
            _reportResults = reportResults;
            _reportStandard = reportStandard;
            _reportDeduction = reportDeduction;
        }
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;

        private final String _jDate;

        private final String[] _grade;

        /** スクーリング登校実績率. */
        private final String _rate1;

        /** スクーリング代替実績率. */
        private final String _rate2;

        /** レポート提出実績率. */
        private final String _rate3;

        /** 所属マスタ. */
        private Map _belonging;

        private Map _subclassMst;

        private Map _classMst;

        /** スクーリング種別マスタ. */
        private Map _schoolingTypeMst;
        private Map _subclassDetailsMstMap1;    // 科目詳細マスタ(スクーリング)//TAKAESU: 1 と 2?
        private Map _subclassDetailsMstMap2;    // 科目詳細マスタ(レポート)

        private Map _schregStaffCdMap = new HashMap();

        /** 通学区分マスタ. */
        private Map _commutingDiv;

        public Param(
                final String year,
                final String semester,
                final String loginDate,
                final String[] grade,
                final String rate1,
                final String rate2,
                final String rate3
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _rate1 = rate1;
            _rate2 = rate2;
            _rate3 = rate3;

            _jDate = getJDate(loginDate);
        }

        public void load(DB2UDB db2) throws SQLException {
            _belonging = createBelongingDat(db2);
            _subclassMst = createSubclassMst(db2);
            _classMst = createClassMst(db2);
            _schoolingTypeMst = createSchoolingTypeMst(db2);
            setSubclassDetailsMstMap(createSubclassDetailsMst(db2));//TAKAESU: 科目マスタと合体したい
            setSchregRegdHdatMap(createSchregRegdHdat(db2));
            _commutingDiv = createStudentDivMst(db2);
        }

        private void setSubclassDetailsMstMap(List subclassDetailsMsts) {
            final Map rtn = new HashMap();
            final Map rtn2 = new HashMap();

            for (Iterator it = subclassDetailsMsts.iterator(); it.hasNext();) {
                final SubclassDetailsMst subclassDetailsMst = (SubclassDetailsMst) it.next();

                final String code = subclassDetailsMst.getKey();

                final String val = Integer.toString(subclassDetailsMst._schoolingSeq);
                rtn.put(code, val);

                final String val2 = Integer.toString(subclassDetailsMst._reportSeq);
                rtn2.put(code, val2);
            }

            _subclassDetailsMstMap1 = rtn;
            _subclassDetailsMstMap2 = rtn2;
        }

        private void setSchregRegdHdatMap(List schregRegdHdats) {
            for (Iterator it = schregRegdHdats.iterator(); it.hasNext();) {
                final SchregRegdHdat schregRegdHdat = (SchregRegdHdat) it.next();

                final String code = schregRegdHdat.getKey();

                final Staff staff = new Staff(schregRegdHdat._staffcd, schregRegdHdat._staffName);
                _schregStaffCdMap.put(code, staff);
            }
        }

        public String getSubclass(String code) {
            return nvlT((String)_subclassMst.get(code));
        }

        public String getClass(String code) {
            return nvlT((String)_classMst.get(code));
        }

        public String getSchoolingType(String code) {
            return nvlT((String)_schoolingTypeMst.get(code));
        }

        public String getCommutingDiv(final SchregRegdDat schregRegdDat) {
            String code1 = schregRegdDat._courseDiv;
            String code2 = schregRegdDat._studentDiv;

            return nvlT((String)_commutingDiv.get(code1 + code2));
        }
        
        public int getSubclassDetailsMstMap1Int(String code) {//TAKAESU: 何してるメソッド?
            final String str1 = nvlT((String)_subclassDetailsMstMap1.get(code));

            int rtn = 0;
            if (str1.length() != 0) {
                rtn = Integer.parseInt(str1);
            }

            return rtn;
        }

        public int getSubclassDetailsMstMap2Int(String code) {//TAKAESU: 何してるメソッド?
            final String str1 = nvlT((String)_subclassDetailsMstMap2.get(code));

            int rtn = 0;
            if (str1.length() != 0) {
                rtn = Integer.parseInt(str1);
            }

            return rtn;
        }

        public String getStaffCd(final int i, final String hrClass) {
            final Staff staff = (Staff)_schregStaffCdMap.get(_year + _semester + _grade[i] + hrClass);
            return nvlT(staff._code);
        }

        public String getStaffName(String code) {
            final Staff staff = (Staff)_schregStaffCdMap.get(code);
            return nvlT(staff._name);
        }

        private String getJDate(final String date) {
            try {
                final Calendar cal = KNJServletUtils.parseDate(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int dom = cal.get(Calendar.DAY_OF_MONTH);

                return nao_package.KenjaProperties.gengou(year, month, dom);
            } catch (final Exception e) {
                return null;
            }
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String[] grade = request.getParameterValues("CATEGORY_SELECTED");
        final String rate1 = request.getParameter("SCHOOLING1");     // TODO: 暫定
        final String rate2 = request.getParameter("SCHOOLING2");     // TODO: 暫定
        final String rate3 = request.getParameter("REPORT");     // TODO: 暫定

        final Param param = new Param(
                year,
                semester,
                loginDate,
                grade,
                rate1,
                rate2,
                rate3
        );

        return param;
    }

    // ======================================================================

    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response,
                final Vrw32alp svf) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
        }

        public void printStaffName(String oldStaffName) {
            String name = oldStaffName;

            if (name != null) {
                final String label;
                if (name.length() <= STAFF_LENG) {
                    label = "STAFFNAME1_1";
                } else {
                    label = "STAFFNAME1_2";
                }

                _form._svf.VrsOut(label, name);
            }
        }

        public void printName(String pName) {
            String name = pName;

            if (name != null) {
                final String label;
                if (name.length() <= NAME_LENG) {
                    label = "NAME1_1";
                } else {
                    label = "NAME1_2";     // TODO: 現行1項目のみ
                }

                _form._svf.VrsOut(label, name);
            }
        }

        public void printSubclassName(String pName) {
            String name = pName;

            if (name != null) {
                final String label;
                if (name.length() <= SUBCLASSNAME_LENG) {
                    label = "SUBCLASSNAME1_1";
                } else {
                    label = "SUBCLASSNAME1_2";
                }
                _form._svf.VrsOut(label, name);
            }
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _schregno;
        private final String _hrClass;
        private final String _courseDiv;
        private final String _studentDiv;
        private final String _name;
        private final String _applicantno;

        SchregRegdDat(
                final String schregno,
                final String hrClass,
                final String courseDiv,
                final String studentDiv,
                final String name,
                final String applicantno
        ) {
            _schregno = schregno;
            _hrClass = hrClass;
            _courseDiv = courseDiv;
            _studentDiv = studentDiv;
            _name = name;
            _applicantno = applicantno;
        }
    }

    public List createSchregRegdDat(DB2UDB db2, final String grade) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdDat(grade));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String schregno = rs.getString("schregno");
            final String hrClass = rs.getString("hrClass");
            final String courseDiv = nvlT(rs.getString("courseDiv"));
            final String studentDiv = nvlT(rs.getString("studentDiv"));
            final String name = rs.getString("name");
            final String applicantno = rs.getString("applicantno");

            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    schregno,
                    hrClass,
                    courseDiv,
                    studentDiv,
                    name,
                    applicantno
            );
            rtn.add(schregRegdDat);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>SCHREG_REGD_DAT に該当するものがありません。");
        }

        return rtn;
    }

    private String sqlSchregRegdDat(String grade) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    T1.SCHREGNO as schregno,");
        stb.append("    T1.HR_CLASS as hrClass,");
        stb.append("    T1.COURSE_DIV as courseDiv,");
        stb.append("    T1.STUDENT_DIV as studentDiv,");
        stb.append("    T2.NAME as name,");
        stb.append("    T2.APPLICANTNO as applicantno");
        stb.append(" from");
        stb.append("    SCHREG_REGD_DAT T1");
        stb.append("    left join SCHREG_BASE_MST T2 on");
        stb.append("    T2.SCHREGNO = T1.SCHREGNO");
        stb.append(" where");
        stb.append("    T1.YEAR = '" + _param._year + "'");
        stb.append("    and T1.SEMESTER = '" + _param._semester + "'");
        stb.append("    and T1.GRADE = '" + grade + "'");
        stb.append(" order by T1.HR_CLASS, T1.SCHREGNO");

        return stb.toString();
    }

    // ======================================================================
    /**
     * 科目マスタ。
     */
    private Map createSubclassMst(final DB2UDB db2) throws SQLException {
        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            + "   CLASSCD as classcd,"
            + "   CURRICULUM_CD as curriculumCd,"
            + "   SUBCLASSCD as subclasscd,"
            + "   SUBCLASSNAME as subclassname"
            + " from"
            + "   SUBCLASS_MST";
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code1 = rs.getString("classcd");
            final String code2 = rs.getString("curriculumCd");
            final String code3 = rs.getString("subclasscd");
            final String name = rs.getString("subclassname");

            rtn.put(code1 + code2 + code3, name);
        }

        return rtn;
    }

    // ======================================================================
    /**
     * 教科マスタ。
     */
    private Map createClassMst(final DB2UDB db2) throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            + "   CLASSCD as classcd,"
            + "   CLASSNAME as classname"
            + " from"
            + "   CLASS_MST";
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code = rs.getString("classcd");
            final String name = rs.getString("classname");

            rtn.put(code, name);
        }

        return rtn;
    }

    // ======================================================================
    /**
     * 科目詳細マスタ。
     */
    private class SubclassDetailsMst {
        private final String _classcd;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final int _schoolingSeq;
        private final int _reportSeq;

        SubclassDetailsMst(
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final int schoolingSeq,
                final int reportSeq
        ) {
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _schoolingSeq = schoolingSeq;
            _reportSeq = reportSeq;
        }

        private String getKey() {
            return _classcd + _subclasscd + _curriculumCd;
        }
    }

    public List createSubclassDetailsMst(DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSubclassDetailsMst());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscd = rs.getString("subclasscd");
            final int schoolingSeq = Integer.parseInt(rs.getString("schoolingSeq"));
            final int reportSeq = Integer.parseInt(rs.getString("reportSeq"));

            final SubclassDetailsMst subclassDetailsMst = new SubclassDetailsMst(
                    classcd,
                    curriculumCd,
                    subclasscd,
                    schoolingSeq,
                    reportSeq
            );

            rtn.add(subclassDetailsMst);
        }

        return rtn;
    }

    private String sqlSubclassDetailsMst() {
        return "select"
                + "   CLASSCD as classcd,"
                + "   CURRICULUM_CD as curriculumCd,"
                + "   SUBCLASSCD as subclasscd,"
                + "   value(SCHOOLING_SEQ, 0) as schoolingSeq,"
                + "   value(REPORT_SEQ, 0) as reportSeq"
                + " from"
                + "   SUBCLASS_DETAILS_MST"
                + " where"
                + "   YEAR = '" + _param._year + "'"
                ;
    }

    // ======================================================================
    /**
     * スクーリング種別マスタ。
     */
    public Map createSchoolingTypeMst(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            + "   SCHOOLING_TYPE as schoolingType,"
            + "   SCHOOLING_DIV as schoolingDiv"
            + " from"
            + "   SCHOOLING_TYPE_MST";
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("schoolingType");
            final String name = rs.getString("schoolingDiv");

            rtn.put(code, name);
        }

        return rtn;
    }

    // ======================================================================
    /**
     * 学生区分マスタ。
     */
    private Map createStudentDivMst(final DB2UDB db2) throws SQLException {
        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            + "   COURSE_DIV as courseDiv,"
            + "   STUDENT_DIV as studentDiv,"
            + "   COMMUTING_DIV as commutingDiv"
            + " from"
            + "   STUDENTDIV_MST";
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code1 = rs.getString("courseDiv");
            final String code2 = rs.getString("studentDiv");
            final String name = rs.getString("commutingDiv");

            rtn.put(code1 + code2, name);
        }

        return rtn;
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    public Map createBelongingDat(DB2UDB db2) throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql;
        sql = "select"
            + "   BELONGING_DIV,"
            + "   SCHOOLNAME1"
            + " from"
            + "   BELONGING_MST";
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("belonging_div");
            final String name = rs.getString("schoolname1");

            rtn.put(code, name);
        }

        return rtn;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {
        if (val == null) {
            return "";
        } else {
            return val;
        }
    }

    // ======================================================================
    /**
     * 学籍在籍ヘッダデータ。
     */
    private class SchregRegdHdat {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _hrClass;
        private final String _staffcd;
        private final String _staffName;

        SchregRegdHdat(
                final String year,
                final String semester,
                final String grade,
                final String hrClass,
                final String staffcd,
                final String staffname
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _staffcd = staffcd;
            _staffName = staffname;
        }

        private String getKey() {
            return _year + _semester + _grade + _hrClass;
        }
    }

    public List createSchregRegdHdat(DB2UDB db2) throws SQLException {

        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdHdat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String year = rs.getString("year");
            final String semester = rs.getString("semester");
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hrClass");
            final String staffcd = rs.getString("staffcd");
            final String staffname = rs.getString("staffname");

            final SchregRegdHdat schregRegdHdat = new SchregRegdHdat(
                    year,
                    semester,
                    grade,
                    hrClass,
                    staffcd,
                    staffname
            );
            rtn.add(schregRegdHdat);
        }

        return rtn;
    }

    private String sqlSchregRegdHdat() {
        return " select"
                + "    T1.YEAR as year,"
                + "    T1.SEMESTER as semester,"
                + "    T1.GRADE as grade,"
                + "    T1.HR_CLASS as hrClass,"
                + "    T1.TR_CD1 as staffcd,"
                + "    T2.STAFFNAME as staffname"
                + " from"
                + "    SCHREG_REGD_HDAT T1"
                + "    left join STAFF_MST T2 on"
                + "    T2.STAFFCD = T1.TR_CD1"
                ;
    }

    // ======================================================================
    /**
     * 通信スクーリング実績。
     */
    private class RecSchoolingDat {
        private final String _classcd;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final String _schoolingType;
        private final int _getValue;

        RecSchoolingDat(
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final String schoolingType,
                final int getValue
        ) {
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _schoolingType = schoolingType;
            _getValue = getValue;
        }
    }

    public List createRecSchoolingDats(DB2UDB db2, String schregno) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlRecSchoolingDats(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscd = rs.getString("subclasscd");
            final String schoolingType = rs.getString("schoolingType");
            final int getValue = Integer.parseInt(rs.getString("getValue"));

            final RecSchoolingDat recSchoolingDat = new RecSchoolingDat(
                    classcd,
                    curriculumCd,
                    subclasscd,
                    schoolingType,
                    getValue
            );
            rtn.add(recSchoolingDat);
        }
        return rtn;
    }

    private String sqlRecSchoolingDats(String schregno) {
        return "select"
          + "    W1.CLASSCD as classcd,"
          + "    W1.CURRICULUM_CD as curriculumCd,"
          + "    W1.SUBCLASSCD as subclasscd,"
          + "    W1.SCHOOLING_TYPE as schoolingType,"
          + "    SUM(W1.GET_VALUE) as getValue"
          + " from"
          + "    REC_SCHOOLING_DAT W1"
          + " where"
          + "    W1.YEAR = '" + _param._year + "'"
          + "    and W1.SCHREGNO = '" + schregno + "'"
          + " group by W1.CLASSCD, W1.SUBCLASSCD, W1.CURRICULUM_CD, W1.SCHOOLING_TYPE"
          + " order by W1.CLASSCD, W1.SUBCLASSCD, W1.CURRICULUM_CD desc, W1.SCHOOLING_TYPE"
          ;
    }

    // ======================================================================
    /**
     * 通信スクーリング割合。
     */
    private class RecSchoolingRateDat {
        private final String _classcd;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final String _schoolingType;
        private final String _commitedS;
        private final String _commitedE;
        private final int _rate;

        RecSchoolingRateDat(
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final String schoolingType,
                final String commitedS,
                final String commitedE,
                final int rate
        ) {
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _schoolingType = schoolingType;
            _commitedS = commitedS;
            _commitedE = commitedE;
            _rate = rate;
        }
    }

    public List createRecSchoolingRateDatt(DB2UDB db2, String schregno) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlRecSchoolingRateDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd       = rs.getString("classcd");
            final String curriculumCd  = rs.getString("curriculumCd");
            final String subclasscd    = rs.getString("subclasscd");
            final String schoolingType = rs.getString("schoolingType");
            final String commitedS     = nvlT(rs.getString("commitedS"));
            final String commitedE     = nvlT(rs.getString("commitedE"));
            final int rate = Integer.parseInt(rs.getString("rate"));

            final RecSchoolingRateDat recSchoolingRateDat = new RecSchoolingRateDat(
                    classcd,
                    curriculumCd,
                    subclasscd,
                    schoolingType,
                    commitedS,
                    commitedE,
                    rate
            );
            rtn.add(recSchoolingRateDat);
        }
       return rtn;
    }

    private String sqlRecSchoolingRateDat(String schregno) {
      StringBuffer stb = new StringBuffer();
      stb.append(" select");
      stb.append("    W1.CLASSCD as classcd,");
      stb.append("    W1.CURRICULUM_CD as curriculumCd,");
      stb.append("    W1.SUBCLASSCD as subclasscd,");
      stb.append("    W1.SCHOOLING_TYPE as schoolingType,");
      stb.append("    W1.COMMITED_S as commitedS,");
      stb.append("    W1.COMMITED_E as commitedE,");
      stb.append("    SUM(value(W1.RATE,0)) as rate");
      stb.append(" from");
      stb.append("    REC_SCHOOLING_RATE_DAT W1");
      stb.append(" where");
      stb.append("    W1.YEAR = '" + _param._year + "'");
      stb.append("    and W1.SCHREGNO = '" + schregno + "'");
      stb.append(" group by W1.CLASSCD, W1.SUBCLASSCD, W1.CURRICULUM_CD, W1.SCHOOLING_TYPE, W1.COMMITED_S, W1.COMMITED_E");
      stb.append(" order by W1.CLASSCD, W1.SUBCLASSCD, W1.CURRICULUM_CD, W1.SCHOOLING_TYPE, W1.COMMITED_S, W1.COMMITED_E");

      return stb.toString();
    }

    // ======================================================================
    /**
     * レポート実績。
     */
    private class RecReportDat {
        private final String _classcd;
        private final String _curriculumCd;
        private final String _subclasscd;
        private final int _reportCount1;
        private final int _reportCount2;

        RecReportDat(
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final int reportCount1,
                final int reportCount2
        ) {
            _classcd      = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd   = subclasscd;
            _reportCount1 = reportCount1;
            _reportCount2 = reportCount2;
        }

        private int sum() {
            return _reportCount1 + _reportCount2;
        }
    }

    public List createRecReportDats(DB2UDB db2, String schregno) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlRecReportDats(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscd = rs.getString("subclasscd");
            final int reportCount1 = Integer.parseInt(rs.getString("reportCount1"));
            final int reportCount2 = Integer.parseInt(rs.getString("reportCount2"));

            final RecReportDat recReportDat = new RecReportDat(
                    classcd,
                    curriculumCd,
                    subclasscd,
                    reportCount1,
                    reportCount2
            );
            rtn.add(recReportDat);
        }

        return rtn;
    }

    private String sqlRecReportDats(String schregno) {
        StringBuffer stb = new StringBuffer();

        stb.append("     select");
        stb.append("         T1.CLASSCD as classcd,");
        stb.append("         T1.CURRICULUM_CD as curriculumCd,");
        stb.append("         T1.SUBCLASSCD as subclasscd,");
        stb.append("         SUM(T1.REPORT_COUNT1) AS reportCount1,");
        stb.append("         SUM(T1.REPORT_COUNT2) AS reportCount2");
        stb.append("     from (");
        stb.append("         select");
        stb.append("             YEAR,");
        stb.append("             SCHREGNO,");
        stb.append("             CLASSCD,");
        stb.append("             CURRICULUM_CD,");
        stb.append("             SUBCLASSCD,");
        stb.append("             SUM(CASE WHEN COMMITED_SCORE2 IS NULL AND COMMITED_SCORE1 >= 30 THEN 1 ELSE 0 END) AS REPORT_COUNT1,");
        stb.append("             SUM(CASE WHEN COMMITED_SCORE2 IS NOT NULL AND COMMITED_SCORE2 >= 30 THEN 1 ELSE 0 END) AS REPORT_COUNT2");
        stb.append("         from REC_REPORT_DAT");
        stb.append("         where");
        stb.append("             YEAR = '" + _param._year + "' and");
        stb.append("             SCHREGNO = '" + schregno + "' and");
        stb.append("             (COMMITED_SCORE1 is not null or");
        stb.append("              COMMITED_SCORE2 is not null)");
        stb.append("         group by YEAR, SCHREGNO, CLASSCD, SUBCLASSCD, CURRICULUM_CD");
        stb.append("         order by YEAR, SCHREGNO, CLASSCD, SUBCLASSCD, CURRICULUM_CD desc");
        stb.append("     ) T1");
        stb.append("     group by T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SUBCLASSCD, T1.CURRICULUM_CD");
        stb.append("     order by T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SUBCLASSCD, T1.CURRICULUM_CD desc");
        return stb.toString();
    }

    private class Staff {
        private final String _code;
        private final String _name;

        Staff(final String code, final String name) {
            _code = code;
            _name = name;
        }

        public String toString() {
            return _code + "/" + _name;
        }
    }
} // KNJWD760

// eof
