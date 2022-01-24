// kanji=漢字
/*
 * $Id: aaec89532f9f38c218aaa6693026c5eeb85a6ed6 $
 *
 * 作成日: 2007/12/12 09:28:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  判定資料（偏差値）
 * @author nakada
 * @version $Id: aaec89532f9f38c218aaa6693026c5eeb85a6ed6 $
 */
public class KNJL370J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL370J.class);

    private static final String FORM_FILE = "KNJL370J.frm";

    /*
     * 文字数による出力項目切り分け基準
     */
    /** 名前 */
    private static final int NAME_LENG = 12;
    /** 塾名・塾兄弟情報 */
    private static final int PRISCHOOL_NAME_LENG = 13;
    /** 志願者備考 */
    private static final int SIGAN_REMARK_LENG = 20;

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";
    private static final String NAME_MST_TESTDIV = "L004";

    /*
     * 入試区分
     */
    /** 入試区分：全て */
    private static final String TESTDIV_ALL = "0";
    /** 入試区分：帰国生 */
    private static final String TESTDIV_RETURN = "5";

    /*
     * 入試制度
     */
    /** 入試制度：帰国生 */
//    private static final String APPLICANTDIV_RETURN = "2";

    /*
     * 受験型
     */
    /** 受験型：4科型 */
    private static final String EXAM_TYPE_2 = "2";

    /*
     * 試験科目コード
     */
    /** 英語： */
    private static final String TESTSUBCLASSCD_ENGLISH = "1";
    /** 国語： */
    private static final String TESTSUBCLASSCD_NATIONAL_LANG = "2";
    /** 算数： */
    private static final String TESTSUBCLASSCD_ARITHMETIC = "3";
    /** 理科： */
    private static final String TESTSUBCLASSCD_SCIENCE = "5";
    /** 社会： */
    private static final String TESTSUBCLASSCD_SOCIETY = "4";

    /*
     * 科目名
     */
    /** 理科 */
    private static final String TESTSUBCLASSCD_SCIENCE_NAME = "理科";
    /** 社会： */
    private static final String TESTSUBCLASSCD_SOCIETY_NAME = "社会";
    /** 英語： */
    private static final String TESTSUBCLASSCD_ENGLISH_NAME = "英語";

    /*
     * 判定偏差値区分
     */
    /** ３科平均偏差値： */
    private static final String JUDGE_DEVIATION_DIV_3 = "1";
    /** ４科平均偏差値： */
    private static final String JUDGE_DEVIATION_DIV_4 = "2";

    /*
     * サブタイトル
     */
    /** 判定偏差値 */
    private static final String SUB_TITLE_1 = "判定偏差値";
    /** 2科平均偏差値 */
    private static final String SUB_TITLE_2 = "2科平均偏差値";

    /*
     * ソート条件
     */
    /** 判定偏差値順： */
    private static final String SORT_JUDGE_DEVIATION = "1";

    /*
     * 性別
     */
    /** 性別：男 */
    private static final String SEX_MAN = "1";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 40;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    private int _page;
    private int _totalPage;

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);

        _form = new Form(FORM_FILE, response);

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            for(int i=0; i<_param._testDiv.length; i++) {
                String testDiv = _param._testDiv[i];
                
                log.debug(">>入試区分=" + testDiv);

                final List entexamApplicantbaseDats = createEntexamDesireDats(db2, testDiv);

                printMain(entexamApplicantbaseDats);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List applicants) 
        throws SQLException, Exception {

        int i = 0; // １ページあたり件数
        int no = 0; // №

        String newTestDiv = null;
        String oldTestDiv = null;
        int manNum = 0;
        int womanNum = 0;
        int manNumPassedBefore = 0;
        int womanNumPassedBefore = 0;
        int manAbsent = 0;
        int womanAbsent = 0;
        _page = 0;

        if (!applicants.isEmpty()) {
            final EntexamDesireDat entexamReceptDatKey = (EntexamDesireDat) applicants.get(0);
            newTestDiv = entexamReceptDatKey._testDiv;
            oldTestDiv = entexamReceptDatKey._testDiv;
        }

        _totalPage = 0;
        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamDesireDat applicant = (EntexamDesireDat) it.next();
            
            i++;
            no++;

            newTestDiv = applicant._testDiv;

            if ((_totalPage == 0) || (!newTestDiv.equals(oldTestDiv))) {
                _totalPage = getTotalPage(applicants, oldTestDiv);
            }

            if (newTestDiv.equals(oldTestDiv)) {
                printApplicant(i, no, applicant);

                if (applicant._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                    manNum++;
                    if (applicant.passedBefore()) {
                        manNumPassedBefore++;
                    }else if (applicant.isAbsent()) {
                        manAbsent++;
                    }
                } else {
                    womanNum++;
                    if (applicant.passedBefore()) {
                        womanNumPassedBefore++;
                    }else if (applicant.isAbsent()) {
                        womanAbsent++;
                    }
                }
            } else {
                final int realApplcntMan = manNum-manNumPassedBefore;
                final int realApplcntWoman = womanNum-womanNumPassedBefore;
                final int examineeMan = realApplcntMan - manAbsent;
                final int examineeWoman = realApplcntWoman - womanAbsent;

                prtHeader(oldTestDiv, examineeMan, examineeWoman, realApplcntMan, realApplcntWoman);

                i = 1;
                no = 1;
                printApplicant(i, no, applicant);

                oldTestDiv = newTestDiv;

                _page = 0;
                _totalPage = 0;

                manNum = 0;
                womanNum = 0;
                manNumPassedBefore = 0;
                womanNumPassedBefore = 0;
                manAbsent = 0;
                womanAbsent = 0;
                if (applicant._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                    manNum = 1;
                    if (applicant.passedBefore()) {
                        manNumPassedBefore = 1;
                    }else if (applicant.isAbsent()) {
                        manAbsent = 1;
                    }
                } else {
                    womanNum = 1;
                    if (applicant.passedBefore()) {
                        womanNumPassedBefore = 1;
                    }else if (applicant.isAbsent()) {
                        womanAbsent = 1;
                    }
                }
            }

            if (i >= DETAILS_MAX) {
                prtHeader2(oldTestDiv);

                i = 0;
            }
        }

        if (i > 0) {
            final int realApplcntMan = manNum-manNumPassedBefore;
            final int realApplcntWoman = womanNum-womanNumPassedBefore;
            final int examineeMan = realApplcntMan - manAbsent;
            final int examineeWoman = realApplcntWoman - womanAbsent;

            prtHeader(oldTestDiv, examineeMan, examineeWoman, realApplcntMan, realApplcntWoman);
        }
    }

    private void prtHeader2(String oldTestDiv) {
        printHeader(oldTestDiv);

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void prtHeader(String oldTestDiv, int manNum, int womanNum, int manNumRealApplicant, int womanNumRealApplicant) {
        printHeader(oldTestDiv);
        printFooter(manNum, womanNum, manNumRealApplicant, womanNumRealApplicant);
        _form._svf.VrEndPage();
        _hasData = true;
    }

    private int getTotalPage(List applicants, String testDiv) {
        int cnt = 0;
        int totalPage = 0;

        for (Iterator it = applicants.iterator(); it.hasNext();) {
            final EntexamDesireDat applicant = (EntexamDesireDat) it.next();

            if (testDiv.equals(applicant._testDiv)) {
                cnt++;
            }
        }

        totalPage = cnt / DETAILS_MAX;
        if (cnt % DETAILS_MAX != 0) {
            totalPage++;
        }

        return totalPage;
    }
    
    private void printHeader(String testDiv) {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");

        /* サブタイトル */
        if (_param._print.equals(SORT_JUDGE_DEVIATION)) {
            _form._svf.VrsOut("VALUE", SUB_TITLE_1);
        } else {
            _form._svf.VrsOut("VALUE", SUB_TITLE_2);
        }

        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 総ページ */
        _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
        /* 入試区分 */
        _form._svf.VrsOut("TESTDIV", _param._testDivString(testDiv));
        
        if (testDiv.equals(TESTDIV_RETURN)) {
            /* 英語項目名 */
            _form._svf.VrsOut("D_ITEM1", TESTSUBCLASSCD_ENGLISH_NAME);
            /* 英語項目名 */
            _form._svf.VrsOut("D_ITEM2", TESTSUBCLASSCD_ENGLISH_NAME);
        } else {
            /* 理科項目名 */
            _form._svf.VrsOut("D_ITEM1", TESTSUBCLASSCD_SCIENCE_NAME);
            /* 社会項目名 */
            _form._svf.VrsOut("E_ITEM1", TESTSUBCLASSCD_SOCIETY_NAME);
            /* 理科項目名 */
            _form._svf.VrsOut("D_ITEM2", TESTSUBCLASSCD_SCIENCE_NAME);
            /* 社会項目名 */
            _form._svf.VrsOut("E_ITEM2", TESTSUBCLASSCD_SOCIETY_NAME);
        }
    }

    private void printFooter(int manNum, int womanNum, int manNumRealApplicant, int womanNumRealApplicant) {
        /* 性別人数 */
        _form._svf.VrsOut("NOTE",
                "実志願者数：男 " + Integer.toString(manNumRealApplicant)
                + "名、女 " + Integer.toString(womanNumRealApplicant)
                + "名、合計 " + (Integer.toString(manNumRealApplicant + womanNumRealApplicant)) + "名 "
                + "受験者数：男 " + Integer.toString(manNum)
                + "名、女 " + Integer.toString(womanNum)
                + "名、合計 " + (Integer.toString(manNum + womanNum)) + "名"
        );
    }

    private double parseDouble(final String s) {
        return NumberUtils.isNumber(s) ? Double.parseDouble(s) : 0.0;
    }
    
    private int parseInt(final String s) {
        return NumberUtils.isNumber(s) ? (int) Double.parseDouble(s) : 0;
    }
    
    private String intSum(final String ns1, final String ns2) {
        if (!NumberUtils.isNumber(ns1) && !NumberUtils.isNumber(ns2)) {
            return null;
        }
        return Integer.toString(parseInt(ns1) + parseInt(ns2));
    }
    
    private String doubleSum(final String ns1, final String ns2) {
        if (!NumberUtils.isNumber(ns1) && !NumberUtils.isNumber(ns2)) {
            return null;
        }
        return Double.toString(parseDouble(ns1) + parseDouble(ns2));
    }
    
    private void printApplicant(int i, int no, EntexamDesireDat applicant) {
        /* № */
        _form._svf.VrsOutn("NO", i, String.valueOf(no));
        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO1", i, applicant._examNo);
        /* 志願者氏名 */
        _form.printName(i, applicant);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(applicant._entexamApplicantbaseDat._sex));
        /* 座席 */
        _form._svf.VrsOutn("EXAMNO2", i, applicant._receptNo);

        if (applicant._testSubClassCd1 != null) {
            /* 国語点数 */
            _form._svf.VrsOutn("SCORE1", i, applicant._score1);
        }

        if (applicant._testSubClassCd2 != null) {
            /* 算数点数 */
            _form._svf.VrsOutn("SCORE2", i, applicant._score2);
        }

        if ((applicant._testSubClassCd1 != null) || (applicant._testSubClassCd2 != null)) {
            /* 国語＋算数点数 */
            _form._svf.VrsOutn("SCORE3", i, applicant._score1_plus_2);
        }

        if (applicant._testDiv.equals(TESTDIV_RETURN)) {
            if (applicant._testSubClassCd5 != null) {
                /* 英語点数 */
                _form._svf.VrsOutn("SCORE4", i, applicant._score5);
            }

            /* 点数総合計 */
            _form._svf.VrsOutn("SCORE6", i, intSum(applicant._score1_plus_2, applicant._score5));
        } else {
            if (applicant._testSubClassCd4 != null) {
                /* 理科点数 */
                _form._svf.VrsOutn("SCORE4", i, applicant._score4);
            }

            if (applicant._testSubClassCd3 != null) {
                /* 社会点数 */
                _form._svf.VrsOutn("SCORE5", i, applicant._score3);
            }

            /* 点数総合計 */
            _form._svf.VrsOutn("SCORE6", i, intSum(applicant._score1_plus_2, applicant._score3_plus_4));
        }

        if (applicant._testSubClassCd1 != null) {
            /* 国語偏差値 */
            _form._svf.VrsOutn("DEVIATION_SCORE1", i, String.valueOf(getRoundingOffNum(applicant._std_score1)));
        }

        if (applicant._testSubClassCd2 != null) {
            /* 算数偏差値 */
            _form._svf.VrsOutn("DEVIATION_SCORE2", i, String.valueOf(getRoundingOffNum(applicant._std_score2)));
        }

        if ((applicant._testSubClassCd1 != null) || (applicant._testSubClassCd2 != null)) {
            /* 国語＋算数平均偏差値 */
            _form.printDeviaionScore(i, applicant);
        }

        if (applicant._testDiv.equals(TESTDIV_RETURN)) {
            if (applicant._testSubClassCd5 != null) {
                /* 英語偏差値 */
                _form._svf.VrsOutn("DEVIATION_SCORE4", i, String.valueOf(getRoundingOffNum(applicant._std_score5)));
            }
        } else {
            /* アンダーバー */
            if (applicant._examType.equals(EXAM_TYPE_2)) {
                _form.printBLine(i, applicant);
            }

            if (applicant._testSubClassCd4 != null) {
                /* 理科偏差値 */
                _form._svf.VrsOutn("DEVIATION_SCORE4", i, String.valueOf(getRoundingOffNum(applicant._std_score4)));
            }

            if (applicant._testSubClassCd3 != null) {
                /* 社会偏差値 */
                _form._svf.VrsOutn("DEVIATION_SCORE5", i, String.valueOf(getRoundingOffNum(applicant._std_score3)));
            }
        }

        if ((applicant._testSubClassCd1 != null) || (applicant._testSubClassCd2 != null) ||
            (applicant._testSubClassCd3 != null) || (applicant._testSubClassCd4 != null) ||
            (applicant._testSubClassCd5 != null)) {

            /* 判定偏差値 */
            _form._svf.VrsOutn("DEVIATION_SCORE6", i, String.valueOf(getRoundingOffNum(applicant._judgeDeviation)));
        }

        /* 塾名・塾兄弟情報 */
        String priSchoolName = _form.getPriSchoolName(i, applicant);
        if(!applicant._entexamApplicantbaseDat._remark1.equals("")){
            _form._svf.VrsOutn(_form.getAreaPriSchoolName(priSchoolName+"／"+applicant._entexamApplicantbaseDat._remark1,
            		PRISCHOOL_NAME_LENG), i, priSchoolName+"／"+applicant._entexamApplicantbaseDat._remark1);
        } else {
            _form._svf.VrsOutn(_form.getAreaPriSchoolName(priSchoolName, PRISCHOOL_NAME_LENG), i, priSchoolName);
        }
        
        /* 志願者備考 */
        _form._svf.VrsOutn(_form.getAreaSiganBiko(String.valueOf(applicant._entexamApplicantbaseDat._remark2), SIGAN_REMARK_LENG),
        		i, String.valueOf(applicant._entexamApplicantbaseDat._remark2));

        /* 試験備考 */
        _form._svf.VrsOutn(_form.getAreaTestRemark(_form.getTestRemarkVal(applicant)),i, _form.getTestRemarkVal(applicant));
    }

    private String getRoundingOffNum(final String std_score) {
        if (null == std_score) {
            return null;
        }
        final BigDecimal bd = new BigDecimal(String.valueOf(std_score));
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String getJDate(String date) {
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
        private final String _prgrId;
        private final String _dbName;
        private final String _loginDate;
        private final String[] _testDiv;
        private final String _print;

        private Map _sexMap;
        private Map _testDivMap;
        private Map _priSchoolName;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String testDiv,
                final String print
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            String temp_testDiv = testDiv;
            if ("0".equals(temp_testDiv)) {
                _testDiv = new String[]{"1","2","3","4","5"};
            } else {
                _testDiv = new String[]{temp_testDiv};
            }
            _print = print;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String _testDivString(String testDiv) {
            return (String) _testDivMap.get(testDiv);
        }

        public String _priSchoolNameString(String priSchoolCd) {
            return (String) _priSchoolName.get(priSchoolCd);
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);
            _testDivMap = getNameMst(NAME_MST_TESTDIV);
            _priSchoolName = createPriSchoolMst0();

            return;
        }

        private Map getNameMst(String nameCd1) throws SQLException {
            final String sql = sqlNameMst(nameCd1);
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("name");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlNameMst(String nameCd1) {
            return " select"
                    + "    NAMECD2 as code,"
                    + "    NAME1 as name"
                    + " from"
                    + "    V_NAME_MST"
                    + " where"
                    + "    year = '" + _year + "' AND"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }
        
        /**
         * testdivより前に実施された試験区分のリストをSQLに整形して返す
         *     testdiv=5 -> testdivBefore= "('')"
         *     testdiv=1 -> testdivBefore= "('5')"
         *     testdiv=2 -> testdivBefore= "('5','1')"
         *     testdiv=3 -> testdivBefore= "('5','1','2')"
         *     testdiv=4 -> testdivBefore= "('5','1','2','3')"
         */
        private String getTestdivBeforeIn(String testDiv) {
            StringBuffer testDivBefore = new StringBuffer();
            testDivBefore.append("(");
            if ("5".equals(testDiv)) {
                testDivBefore.append("''");
            } else {
                testDivBefore.append("'5'");
                for(int i=1; i<=4; i++) {
                    if (testDiv.equals(String.valueOf(i))) {
                        break;
                    }
                    testDivBefore.append(",'"+String.valueOf(i)+"'");
                }
            }
            testDivBefore.append(")");
            log.debug("testDivBefore ="+testDivBefore);
            return testDivBefore.toString();
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String testDiv = request.getParameter("TESTDIV");
        final String print = request.getParameter("SORT");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                testDiv,
                print
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================
    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 1);
        }

        public void printName(int i, EntexamDesireDat applicant) {
            String name = applicant._entexamApplicantbaseDat._name;

            if (name != null) {
                final String label;
                if (name.length() <= NAME_LENG) {
                    label = "NAME1";
                } else {
                    label = "NAME2";
                }
                _form._svf.VrsOutn(label, i, name);
            }
        }

        public String getPriSchoolName(int i, EntexamDesireDat applicant) {
            String name = _param._priSchoolNameString(applicant._entexamApplicantbaseDat._priSchoolCd);

            if (name != null) {
                return name;
            } else {
            	return "";
            }
        }

        public String getAreaPriSchoolName(String val, int areaLen) {
            if (val.length() <= areaLen) {
                return "PRISCHOOL_NAME1";
            } else {
                return "PRISCHOOL_NAME2_1";
            }
        }

        public String getAreaSiganBiko(String val, int areaLen) {
            if (val.length() <= areaLen) {
                return "REMARK1_1";
            } else {
                return "REMARK1_2";
            }
        }

        public String getAreaTestRemark(String testRemark) {
            String label = "";

            if (testRemark != null) {
                if (testRemark.length() <= 20) {
                    label = "REMARK2_1";
                } else {
                    label = "REMARK2_2";
                }
            }
            return label;
        }
        
        public String getTestRemarkVal(EntexamDesireDat applicant) {
            String[] scoreRemake = new String[5];
            scoreRemake[0] = nvlT(applicant._score1_Remark);	// 国語備考
            scoreRemake[1] = nvlT(applicant._score2_Remark);	// 算数備考
            scoreRemake[2] = nvlT(applicant._score3_Remark);	// 社会備考
            scoreRemake[3] = nvlT(applicant._score4_Remark);    // 理科備考
            scoreRemake[4] = nvlT(applicant._score5_Remark);    // 英語備考

            final StringBuffer sb = new StringBuffer();
        	boolean hitflg = false;

            for (int idx = 0; idx < scoreRemake.length; idx++) {

            	if(!scoreRemake[idx].equals("")){
            		if(hitflg){
                		sb.append("／");
                	}
            		switch (idx) {
            		  case 0:
                    	sb.append("2:");
            			break;	
            		  case 1:
                    	sb.append("3:");
            			break;	
            		  case 2:
                    	sb.append("4:");
            			break;	
                      case 3:
                        sb.append("5:");
                        break;  
                      case 4:
                        sb.append("1:");
                        break;  
            		  default:
            			break;
            		}
            		sb.append(scoreRemake[idx]);
            		hitflg = true;
            	}
            }

            return sb.toString();
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
    	
        public String getAreaTestBiko(String val, int areaLen) {
            if (val.length() <= areaLen) {
                return "TESTREMARK1_1";
            } else {
                return "TESTREMARK2_1";
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

        void printDeviaionScore(int i, EntexamDesireDat applicant) {
            double db = (parseDouble(applicant._std_score1) + parseDouble(applicant._std_score2)) / 2;
            BigDecimal bd = new BigDecimal(String.valueOf(db));
            double scale1 = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
           _svf.VrsOutn("DEVIATION_SCORE3", i, String.valueOf(scale1));
        }

        void printBLine(int i, EntexamDesireDat applicant) {
            _svf.VrsOutn("FLG", i, "");
            if (applicant._judgeDeviationDiv.equals(JUDGE_DEVIATION_DIV_3)) {
                if (parseDouble(applicant._std_score4) >= parseDouble(applicant._std_score3)) {
                    _svf.VrsOutn("FLG", i, "1");
                } else {
                    _svf.VrsOutn("FLG", i, "3");
                }
            } else if (applicant._judgeDeviationDiv.equals(JUDGE_DEVIATION_DIV_4)) {
                _svf.VrsOutn("FLG", i, "2");
            }
        }
    }

    // ======================================================================
    class EntexamDesireDat {
        private final String _applicantDiv;		// 入試制度
        private final String _testDiv;				// 入試区分
        private final String _examType;			// 受験型
        private final String _examNo;				// 受験番号
        private final String _receptNo;			// 受付№
        private final String _judgeDeviation;       // 判定偏差値
        private final String _examineeDiv;        // 受験者区分(1:有り／2:欠席)
        private final String _judgeDeviationDiv;	// 判定偏差値区分
        private final String _testSubClassCd1;		// 試験科目コード：国語
        private final String _score1;              // 得点
        private final String _std_score1;           // 偏差値
        private final String _testSubClassCd2;      // 試験科目コード：算数
        private final String _score2;              // 得点
        private final String _std_score2;           // 偏差値
        private final String _testSubClassCd3;      // 試験科目コード:社会
        private final String _score3;              // 得点
        private final String _std_score3;           // 偏差値
        private final String _testSubClassCd4;      // 試験科目コード：理科
        private final String _score4;              // 得点
        private final String _std_score4;           // 偏差値

        private final String _testSubClassCd5;    // 試験科目コード：英語
        private final String _score5;                // 得点
        private final String _std_score5;        // 偏差値

        private final String _score1_plus_2;           // 国語得点＋算数得点
        private final String _score3_plus_4;           // 社会得点＋理科得点
        private final String _std_score1_plus_2;    // 国語偏差値＋算数偏差値
        private final String _score1_Remark;		// 国語試験備考
        private final String _score2_Remark;		// 算数試験備考
        private final String _score3_Remark;		// 社会試験備考
        private final String _score4_Remark;      // 理科試験備考
        private final String _score5_Remark;      // 英語試験備考
        private final String _judgedivBefore;           // 前回の試験までの合否区分

        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ

        EntexamDesireDat(
                final String applicantDiv,
                final String testDiv,
                final String examType,
                final String examNo,
                final String receptNo,
                final String examineeDiv,
                final String judgeDeviation,
                final String judgeDeviationDiv,
                final String testSubClassCd1,
                final String score1,
                final String std_score1,
                final String testSubClassCd2,
                final String score2,
                final String std_score2,
                final String testSubClassCd3,
                final String score3,
                final String std_score3,
                final String testSubClassCd4,
                final String score4,
                final String std_score4,
                final String testSubClassCd5,
                final String score5,
                final String std_score5,
                final String score1_plus_2,
                final String score3_plus_4,
                final String std_score1_plus_2,
                final String score1_Remark,
                final String score2_Remark,
                final String score3_Remark,
                final String score4_Remark,
                final String score5_Remark,
                final String judgedivBefore
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examType = examType;
            _examNo = examNo;
            _receptNo = receptNo;
            _examineeDiv = examineeDiv;
            _judgeDeviation = judgeDeviation;
            _judgeDeviationDiv = judgeDeviationDiv;
            _testSubClassCd1 = testSubClassCd1;
            _score1 = score1;
            _std_score1 = std_score1;
            _testSubClassCd2 = testSubClassCd2;
            _score2 = score2;
            _std_score2 = std_score2;
            _testSubClassCd3 = testSubClassCd3;
            _score3 = score3;
            _std_score3 = std_score3;
            _testSubClassCd4 = testSubClassCd4;
            _score4 = score4;
            _std_score4 = std_score4;
            _testSubClassCd5 = testSubClassCd5;
            _score5 = score5;
            _std_score5 = std_score5;
            _score1_plus_2 = score1_plus_2;
            _score3_plus_4 = score3_plus_4;
            _std_score1_plus_2 = std_score1_plus_2;
            _score1_Remark = score1_Remark;
            _score2_Remark = score2_Remark;
            _score3_Remark = score3_Remark;
            _score4_Remark = score4_Remark;
            _score5_Remark = score5_Remark;
            _judgedivBefore = judgedivBefore;
        }

        public void load(DB2UDB db2, String applicantDiv, String examno) throws SQLException, Exception {
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, applicantDiv, examno);
        }
        
        public boolean passedBefore() {
            return _judgedivBefore != null && "1".equals(_judgedivBefore);
        }
        
        public boolean isAbsent() {
            return _examineeDiv != null && "2".equals(_examineeDiv);
        }
    }

    private List createEntexamDesireDats(final DB2UDB db2, String testDivOrder)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamDesireDats(testDivOrder));
            rs = ps.executeQuery();

            while (rs.next()) {
                final String applicantDiv = rs.getString("applicantDiv");
                final String testDiv = rs.getString("testDiv");
                final String examType = rs.getString("examType");
                final String examNo = rs.getString("examNo");
                final String receptNo = rs.getString("receptNo");
                final String examineeDiv = rs.getString("examineeDiv");

                final String judgeDeviation = doubleValue(rs.getString("judgeDeviation"));

                final String judgeDeviationDiv = rs.getString("judgeDeviationDiv");
                final String testSubClassCd1 = rs.getString("testSubClassCd1");
                final String score1 = rs.getString("score1");

                final String std_score1 = doubleValue(rs.getString("std_score1"));

                final String testSubClassCd2 = rs.getString("testSubClassCd2");
                final String score2 = rs.getString("score2");

                final String std_score2 = doubleValue(rs.getString("std_score2"));

                final String testSubClassCd3 = rs.getString("testSubClassCd3");
                final String score3 = rs.getString("score3");

                final String std_score3 = doubleValue(rs.getString("std_score3"));

                final String testSubClassCd4 = rs.getString("testSubClassCd4");
                final String score4 = rs.getString("score4");

                final String std_score4 = doubleValue(rs.getString("std_score4"));


                final String testSubClassCd5 = rs.getString("testSubClassCd5");
                final String score5 = rs.getString("score5");

                final String std_score5 = doubleValue(rs.getString("std_score5"));

                final String score1_plus_2 = intSum(score1, score2); // rs.getString("score1_plus_2");
                final String score3_plus_4 = intSum(score3, score4); // rs.getString("score3_plus_4");

                final String std_score1_plus_2 = doubleSum(std_score1, std_score2); // doubleValue(rs.getString("std_score1_plus_2"));

                final String score1_Remark = rs.getString("score1_Remark");
                final String score2_Remark = rs.getString("score2_Remark");
                final String score3_Remark = rs.getString("score3_Remark");
                final String score4_Remark = rs.getString("score4_Remark");
                final String score5_Remark = rs.getString("score5_Remark");
                
                final String judgedivBefore = rs.getString("judgediv_before");

                final EntexamDesireDat entexamDesireDat = new EntexamDesireDat(
                        applicantDiv,
                        testDiv,
                        examType,
                        examNo,
                        receptNo,
                        examineeDiv,
                        judgeDeviation,
                        judgeDeviationDiv,
                        testSubClassCd1,
                        score1,
                        std_score1,
                        testSubClassCd2,
                        score2,
                        std_score2,
                        testSubClassCd3,
                        score3,
                        std_score3,
                        testSubClassCd4,
                        score4,
                        std_score4,
                        testSubClassCd5,
                        score5,
                        std_score5,
                        score1_plus_2,
                        score3_plus_4,
                        std_score1_plus_2,
                        score1_Remark,
                        score2_Remark,
                        score3_Remark,
                        score4_Remark,
                        score5_Remark,
                        judgedivBefore
                );

                entexamDesireDat.load(db2, entexamDesireDat._applicantDiv, entexamDesireDat._examNo);
                rtn.add(entexamDesireDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>EntexamDesireDat に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String doubleValue(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return s;
        }
        return new BigDecimal(s).setScale(2, BigDecimal.ROUND_UNNECESSARY).toString();
    }
    
    private String sqlEntexamDesireDats(String testDiv) {
        StringBuffer stb = new StringBuffer();

        stb.append(" with T_BEFORE_PASSED as( ");
        stb.append("     select ");
        stb.append("         T1.ENTEXAMYEAR, ");
        stb.append("         T1.APPLICANTDIV, ");
        stb.append("         T1.EXAMNO, ");
        stb.append("         min(case when T1.TESTDIV in "+_param.getTestdivBeforeIn(testDiv)+" then T2.JUDGEDIV ");
        stb.append("             ELSE NULL end) as JUDGEDIV_BEFORE ");
        stb.append("     from ENTEXAM_DESIRE_DAT T1 ");
        stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT T2 on ");
        stb.append("             T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("             and T1.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("             and T1.TESTDIV = T2.TESTDIV ");
        stb.append("             and T1.EXAM_TYPE = T2.EXAM_TYPE ");
        stb.append("             and T1.EXAMNO = T2.EXAMNO ");
        stb.append("     group by  ");
        stb.append("         T1.ENTEXAMYEAR, T1.APPLICANTDIV, T1.EXAMNO ");
        stb.append(" ) select");
        stb.append("    T1.ENTEXAMYEAR as entExamYear,");
        stb.append("    T1.APPLICANTDIV as applicantDiv,");
        stb.append("    T1.TESTDIV as testDiv,");
        stb.append("    T1.EXAM_TYPE as examType,");
        stb.append("    T1.EXAMNO as examNo,");
        stb.append("    T2.RECEPTNO as receptNo,");
        stb.append("    T1.EXAMINEE_DIV as examineeDiv,");
        stb.append("    T2.JUDGE_DEVIATION as judgeDeviation,");
        stb.append("    value(T2.JUDGE_DEVIATION_DIV, '') as judgeDeviationDiv,");
        stb.append("    T3.TESTSUBCLASSCD as testSubClassCd1,");
        stb.append("    T3.SCORE as score1,");
        stb.append("    T3.STD_SCORE as std_score1,");
        stb.append("    T4.TESTSUBCLASSCD as testSubClassCd2,");
        stb.append("    T4.SCORE as score2,");
        stb.append("    T4.STD_SCORE as std_score2,");
        stb.append("    T5.TESTSUBCLASSCD as testSubClassCd3,");
        stb.append("    T5.SCORE as score3,");
        stb.append("    T5.STD_SCORE as std_score3,");
        stb.append("    T6.TESTSUBCLASSCD as testSubClassCd4,");
        stb.append("    T6.SCORE as score4,");
        stb.append("    T6.STD_SCORE as std_score4,");

        stb.append("    T11.TESTSUBCLASSCD as testSubClassCd5,");
        stb.append("    T11.SCORE as score5,");
        stb.append("    T11.STD_SCORE as std_score5,");

        stb.append("    (value(T3.SCORE,0) + value(T4.SCORE,0)) as score1_plus_2,");
        stb.append("    (value(T5.SCORE,0) + value(T6.SCORE,0)) as score3_plus_4,");
        stb.append("    (value(T3.STD_SCORE,0) + value(T4.STD_SCORE,0)) as std_score1_plus_2,");
        stb.append("    value(T7.REMARK, '') as score1_Remark,");
        stb.append("    value(T8.REMARK, '') as score2_Remark,");
        stb.append("    value(T9.REMARK, '') as score3_Remark,");
        stb.append("    value(T10.REMARK, '') as score4_Remark,");
        stb.append("    value(T12.REMARK, '') as score5_Remark, ");
        stb.append("    value(T13.JUDGEDIV_BEFORE, '') as judgediv_before ");
        stb.append(" from");
        stb.append("    ENTEXAM_DESIRE_DAT T1 left join ENTEXAM_RECEPT_DAT T2 on (");
        stb.append("    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR");
        stb.append("    and T2.APPLICANTDIV    = T1.APPLICANTDIV");
        stb.append("    and T2.TESTDIV      = T1.TESTDIV");
        stb.append("    and T2.EXAM_TYPE   = T1.EXAM_TYPE");
        stb.append("    and T2.EXAMNO      = T1.EXAMNO)");
        stb.append("    left join ENTEXAM_SCORE_DAT T3 on (");
        stb.append("    T3.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T3.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T3.TESTDIV = T2.TESTDIV");
        stb.append("    and T3.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T3.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T3.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_NATIONAL_LANG + "')");
        stb.append("    left join ENTEXAM_SCORE_DAT T4 on (");
        stb.append("    T4.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T4.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T4.TESTDIV = T2.TESTDIV");
        stb.append("    and T4.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T4.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T4.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_ARITHMETIC + "')");
        stb.append("    left join ENTEXAM_SCORE_DAT T5 on (");
        stb.append("    T5.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T5.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T5.TESTDIV = T2.TESTDIV");
        stb.append("    and T5.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T5.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T5.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SOCIETY + "')");
        stb.append("    left join ENTEXAM_SCORE_DAT T6 on (");
        stb.append("    T6.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T6.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T6.TESTDIV = T2.TESTDIV");
        stb.append("    and T6.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T6.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T6.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SCIENCE + "')");

        stb.append("    left join ENTEXAM_SCORE_DAT T11 on (");
        stb.append("    T11.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T11.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T11.TESTDIV = T2.TESTDIV");
        stb.append("    and T11.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T11.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T11.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_ENGLISH + "')");

        stb.append("    left join ENTEXAM_SCORE_REMARK_DAT T7 on (");
        stb.append("    T7.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T7.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T7.TESTDIV = T2.TESTDIV");
        stb.append("    and T7.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T7.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T7.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_NATIONAL_LANG + "')");
        stb.append("    left join ENTEXAM_SCORE_REMARK_DAT T8 on (");
        stb.append("    T8.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T8.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T8.TESTDIV = T2.TESTDIV");
        stb.append("    and T8.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T8.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T8.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_ARITHMETIC + "')");
        stb.append("    left join ENTEXAM_SCORE_REMARK_DAT T9 on (");
        stb.append("    T9.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T9.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T9.TESTDIV = T2.TESTDIV");
        stb.append("    and T9.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T9.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T9.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SOCIETY + "')");
        stb.append("    left join ENTEXAM_SCORE_REMARK_DAT T10 on (");
        stb.append("    T10.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T10.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T10.TESTDIV = T2.TESTDIV");
        stb.append("    and T10.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T10.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T10.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SCIENCE + "')");

        stb.append("    left join ENTEXAM_SCORE_REMARK_DAT T12 on (");
        stb.append("    T12.ENTEXAMYEAR = T2.ENTEXAMYEAR");
        stb.append("    and T12.APPLICANTDIV = T2.APPLICANTDIV");
        stb.append("    and T12.TESTDIV = T2.TESTDIV");
        stb.append("    and T12.EXAM_TYPE = T2.EXAM_TYPE");
        stb.append("    and T12.RECEPTNO = T2.RECEPTNO");
        stb.append("    and T12.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_ENGLISH + "')");

        stb.append("    left join T_BEFORE_PASSED T13 on");
        stb.append("    T13.EXAMNO = T1.EXAMNO");
        stb.append("    and T13.ENTEXAMYEAR = T1.ENTEXAMYEAR");
        stb.append("    and T13.APPLICANTDIV = T1.APPLICANTDIV");
        stb.append(" where");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and T1.TESTDIV = '" + testDiv + "'");

        stb.append(" order by T1.TESTDIV");

        if (_param._print.equals(SORT_JUDGE_DEVIATION)) {
            stb.append("    ,value(judgeDeviation, -100.0) DESC");
        } else {
            stb.append("    ,std_score1_plus_2 DESC");
        }
        stb.append("    ,T1.EXAMNO");

        return stb.toString();
    }

    // ======================================================================
    /**
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _name;
        private final String _sex;
        private final String _priSchoolCd;
        private final String _remark1;
        private final String _remark2;

        EntexamApplicantbaseDat() {
            _name = "";
            _sex = "";
            _priSchoolCd = "";
            _remark1 = "";
            _remark2 = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String sex,
                final String priSchoolCd,
                final String remark1,
                final String remark2
        ) {
            _name = name;
            _sex = sex;
            _priSchoolCd = priSchoolCd;
            _remark1 = remark1;
            _remark2 = remark2;
        }
    }

    private EntexamApplicantbaseDat createEntexamApplicantbaseDat(final DB2UDB db2, String pApplicantDiv, String pExamNo)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDat(pApplicantDiv, pExamNo));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String name = rs.getString("name");
            final String sex = rs.getString("sex");
            final String priSchoolCd = rs.getString("priSchoolCd");
            final String remark1 = rs.getString("remark1");
            final String remark2 = rs.getString("remark2");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    name,
                    sex,
                    priSchoolCd,
                    remark1,
                    remark2
            );

            return entexamApplicantbaseDat;
        }

        return new EntexamApplicantbaseDat();
    }

    private String sqlEntexamApplicantbaseDat(String pApplicantDiv, String pExamNo) {
        return " select"
                + "    NAME as name,"
                + "    SEX as sex,"
                + "    PRISCHOOLCD as priSchoolCd,"
                + "    VALUE(REMARK1, '') as remark1,"
                + "    VALUE(REMARK2, '') as remark2"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    APPLICANTDIV = '" + pApplicantDiv + "' and"
                + "    EXAMNO = '" + pExamNo + "'"
                ;
    }

    // ======================================================================
    /**
     * 塾マスタ。
     */
    private Map createPriSchoolMst0() throws SQLException {
        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlPriSchoolMst0());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("prischoolcd");
                final String name = rs.getString("priSchoolName");
                rtn.put(code, name);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String sqlPriSchoolMst0() {
        return " select"
                + "    PRISCHOOLCD as prischoolcd,"
                + "    PRISCHOOL_NAME as priSchoolName"
                + " from"
                + "    PRISCHOOL_MST"
                ;
    }
} // KNJL370J

// eof
