// kanji=漢字
/*
 * $Id: 2236c589ff9776607da94c04658b45eb96d66687 $
 *
 * 作成日: 2007/12/13 10:32:00 - JST
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  判定資料（リンク判定）
 * @author nakada
 * @version $Id: 2236c589ff9776607da94c04658b45eb96d66687 $
 */
public class KNJL371J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL371J.class);

    private static final String FORM_FILE = "KNJL371J.frm";
    private static final String FORM_FILE2 = "KNJL371J_2.frm";

    /*
     * 文字数による出力項目切り分け基準
     */
    /** 名前 */
    private static final int NAME_LENG = 12;
    /** 塾名 */
    private static final int PRISCHOOL_NAME_LENG = 13;
    /** 志願者備考 */
    private static final int SIGAN_REMARK_LENG = 20;

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";
    private static final String NAME_MST_TESTDIV = "L004";

    /*
     * 試験科目コード
     */
    /** 国語： */
    private static final String TESTSUBCLASSCD_NATIONAL_LANG = "2";
    /** 算数： */
    private static final String TESTSUBCLASSCD_ARITHMETIC = "3";
    /** 理科： */
    private static final String TESTSUBCLASSCD_SCIENCE = "5";
    /** 社会： */
    private static final String TESTSUBCLASSCD_SOCIETY = "4";

    /*
     * 入試区分
     */
    /** Ａ-１： */
    private static final String TESTDIV_A1 = "1";
    /** Ａ-２： */
    private static final String TESTDIV_A2 = "2";
    /** Ｂ： */
    private static final String TESTDIV_B = "3";
    /** Ｃ： */
    private static final String TESTDIV_C = "4";
    /** Ｄ： */
    private static final String TESTDIV_D = "6";
    
    /*
     * リンク判定区分
     */
    /** あり */
    private static final String LINK_JUDGE_DIV_ON = "1";

    /*
     * リンク判定偏差値区分
     */
    /** 1:3科平均偏差値 */
    private static final String LINK_JUDGE_DEVIATION_DIV_3 = "1";

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

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            _form = new Form(FORM_FILE, response);

            log.debug(">>入試区分=" + _param._testDiv);

            final List entexamApplicantbaseDats = createEntexamDesireDats(db2);

            printMain(entexamApplicantbaseDats);

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
                } else {
                    womanNum++;
                }
            } else {

                prtHeader(oldTestDiv, manNum, womanNum);

                i = 1;
                no = 1;
                printApplicant(i, no, applicant);

                oldTestDiv = newTestDiv;

                _page = 0;
                _totalPage = 0;

                manNum = 0;
                womanNum = 0;
                if (applicant._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                    manNum = 1;
                } else {
                    womanNum = 1;
                }
            }

            if (i >= DETAILS_MAX) {
                prtHeader2(oldTestDiv);

                i = 0;
            }
        }

        if (i > 0) {
            prtHeader(oldTestDiv, manNum, womanNum);
        }
    }

    private void prtHeader2(String oldTestDiv) {
        printHeader(oldTestDiv);

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void prtHeader(String oldTestDiv, int manNum, int womanNum) {
        printHeader(oldTestDiv);
        printFooter(manNum, womanNum);
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
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 総ページ */
        _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
        /* 入試区分 */
        _form._svf.VrsOut("TESTDIV", _param._testDivString(testDiv));
    }

    private void printFooter(int manNum, int womanNum) {
        /* 性別人数 */
        _form._svf.VrsOut("NOTE", "男 " + Integer.toString(manNum)
                + "名、女 " + Integer.toString(womanNum)
                + "名、合計 " + (Integer.toString(manNum + womanNum)) + "名"
        );
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

        _form.printStdScore(applicant, i);

        /* 判定偏差値 */
        _form._svf.VrsOutn("LINK_JUDGE", i, String.valueOf(getRoundingOffNum(applicant._judgeDeviation)));

        /* 塾名・塾兄弟情報 */
        String priSchoolName = _form.getPriSchoolName(i, applicant);
        if(!applicant._entexamApplicantbaseDat._remark1.equals("")){
            _form._svf.VrsOutn(_form.getAreaPriSchoolName(priSchoolName+"／"+applicant._entexamApplicantbaseDat._remark1,
            		PRISCHOOL_NAME_LENG), i, priSchoolName+"／"+applicant._entexamApplicantbaseDat._remark1);
        } else {
            _form._svf.VrsOutn(_form.getAreaPriSchoolName(priSchoolName, PRISCHOOL_NAME_LENG), i, priSchoolName);
        }

        /* 備考１ */
        _form._svf.VrsOutn(_form.getAreaSiganBiko(String.valueOf(applicant._entexamApplicantbaseDat._remark2), SIGAN_REMARK_LENG),
        		i, String.valueOf(applicant._entexamApplicantbaseDat._remark2));
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

    private double getRoundingOffNum(double std_score) {
        BigDecimal bd = new BigDecimal(String.valueOf(std_score));
        double scale = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return scale;
    }

    // ======================================================================
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _prgrId;
        private final String _dbName;
        private final String _loginDate;
        private final String _testDiv;

        private Map _sexMap;
        private Map _testDivMap;
        private Map _priSchoolName;
        private Map _linkJudgeDivMap;
        private boolean _isInfluence;
        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String testDiv
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _testDiv = testDiv;
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

        public String _linkJudgeDivString(String linkJudgeDiv) {
            return (String) _linkJudgeDivMap.get(linkJudgeDiv);
        }

        public void load(DB2UDB db2) throws SQLException {
            _sexMap = getNameMst(NAME_MST_SEX);
            _testDivMap = getNameMst(NAME_MST_TESTDIV);
            _priSchoolName = createPriSchoolMst0();
            _linkJudgeDivMap = createEntexamTestsubclasscdDatMap();
            _isInfluence = isInfluence(db2);
        }

        private boolean isInfluence(DB2UDB db2) throws SQLException {
            boolean isInfluence = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            ps = db2.prepareStatement("SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '01' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                isInfluence = "1".equals(rs.getString("NAMESPARE1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return isInfluence;
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
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String testDiv = request.getParameter("TESTDIV");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                testDiv
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

            if (_param._isInfluence) {
                _svf.VrSetForm(FORM_FILE2, 1);
            } else {
                _svf.VrSetForm(FORM_FILE, 1);
            }
        }
//
//        public void printStdScore(EntexamDesireDat applicant,final int i) {
//            /** 国語： */
//            double[] nationalScore =  {0, 0, 0, 0}; 
//            /** 算数： */
//            double[] arithmeticScore =  {0, 0, 0, 0}; 
//            /** 理科： */
//            double[] scienceScore =  {0, 0, 0}; 
//            /** 社会： */
//            double[] societyScore =  {0, 0, 0}; 
//
//            double maxNationalScore = 0;
//
//            double maxArithmeticScore = 0;
//
//            double maxScienceScore = 0;
//
//            double maxSocietyScore = 0;
//
//            for (Iterator it = applicant._SCORE.iterator(); it.hasNext();) {
//                final EntexamScoreDat Score = (EntexamScoreDat) it.next();
//
//                if (Score._testDiv.compareTo(_param._testDiv) <= 0) {
//                    if (Score._testSubclassCd.equals(TESTSUBCLASSCD_NATIONAL_LANG)) {
//                        
//                        if (Score._stdScore > maxNationalScore) {
//                            maxNationalScore = Score._stdScore;
//                        }
//
//                        prtNationalScore(i, nationalScore, Score);
//                    } else if (Score._testSubclassCd.equals(TESTSUBCLASSCD_ARITHMETIC)) {
//                        
//                        if (Score._stdScore > maxArithmeticScore) {
//                            maxArithmeticScore = Score._stdScore;
//                        }
//
//                        prtArithmeticScore(i, arithmeticScore, Score);
//                    } else if (Score._testSubclassCd.equals(TESTSUBCLASSCD_SCIENCE)) {
//                        
//                        if (Score._stdScore > maxScienceScore) {
//                            maxScienceScore = Score._stdScore;
//                        }
//
//                        prtScienceScore(i, scienceScore, Score);
//                    } else if (Score._testSubclassCd.equals(TESTSUBCLASSCD_SOCIETY)) {
//                        
//                        if (Score._stdScore > maxSocietyScore) {
//                            maxSocietyScore = Score._stdScore;
//                        }
//
//                        prtSocietyScore(i, societyScore, Score);
//                    }
//                }            
//            }
//
//            prtUnderLine(
//                    i,
//                    nationalScore,
//                    arithmeticScore,
//                    scienceScore,
//                    societyScore,
//                    maxNationalScore,
//                    maxArithmeticScore,
//                    maxScienceScore,
//                    maxSocietyScore
//            );
//        }

        public void printStdScore(EntexamDesireDat applicant,final int i) {
            /** 国語： */
            double[] nationalScore =  {0, 0, 0, 0, 0}; 
            /** 算数： */
            double[] arithmeticScore =  {0, 0, 0, 0, 0}; 
            /** 理科： */
            double[] scienceScore =  {0, 0, 0, 0}; 
            /** 社会： */
            double[] societyScore =  {0, 0, 0, 0}; 

            /** 試験備考： */
            HashMap scoreRemakeMap = new HashMap();

            double maxNationalScore = 0;

            double maxArithmeticScore = 0;

            double maxScienceScore = 0;

            double maxSocietyScore = 0;

            for (Iterator it = applicant._SCORE.iterator(); it.hasNext();) {
                final EntexamScoreDat Score = (EntexamScoreDat) it.next();

                String str = _param._linkJudgeDivString(_param._year
                        + applicant._applicantDiv
                        + applicant._testDiv
                        + applicant._examType
                        + Score._testSubclassCd);
//                str = str != null ? str : LINK_JUDGE_DIV_ON;
                
                
//                if (str != null && (
//                        (str.equals(LINK_JUDGE_DIV_ON) &&
//                        Score._testDiv.compareTo(_param._testDiv) <= 0) ||
//                   (!str.equals(LINK_JUDGE_DIV_ON) &&
//                        Score._testDiv.compareTo(_param._testDiv) == 0))) {
//                if ((str != null &&
//                        (str.equals(LINK_JUDGE_DIV_ON) &&
//                        Score._testDiv.compareTo(_param._testDiv) <= 0)) ||
//                   (str == null &&
//                        (Score._testDiv.compareTo(_param._testDiv) == 0))) {
                    if (Score._testSubclassCd.equals(TESTSUBCLASSCD_NATIONAL_LANG)) {
//                        if ((str.equals(LINK_JUDGE_DIV_ON) &&
//                                Score._testDiv.compareTo(_param._testDiv) <= 0) ||
//                                (!str.equals(LINK_JUDGE_DIV_ON) &&
//                                        Score._testDiv.compareTo(_param._testDiv) == 0)) {

                        if ((str != null &&
                                (str.equals(LINK_JUDGE_DIV_ON) &&
                                        Score._testDiv.compareTo(_param._testDiv) <= 0)) ||
                                        (str == null &&
                                                (Score._testDiv.compareTo(_param._testDiv) == 0))) {
                            if (Score._stdScore > maxNationalScore) {
                                maxNationalScore = Score._stdScore;
                                prtNationalScore(i, nationalScore, Score, 1);
                            }
                        }
//                        }

                        prtNationalScore(i, nationalScore, Score, 0);
                        // 試験備考格納
                        scoreRemakeMap.put(Score._testSubclassCd,Score._scoreRemark);
                    } else if (Score._testSubclassCd.equals(TESTSUBCLASSCD_ARITHMETIC)) {
                        
                        if ((str != null &&
                                (str.equals(LINK_JUDGE_DIV_ON) &&
                                        Score._testDiv.compareTo(_param._testDiv) <= 0)) ||
                                        (str == null &&
                                                (Score._testDiv.compareTo(_param._testDiv) == 0))) {
                            if (Score._stdScore > maxArithmeticScore) {
                                maxArithmeticScore = Score._stdScore;
                                prtArithmeticScore(i, arithmeticScore, Score, 1);
                            }
                        }

                        prtArithmeticScore(i, arithmeticScore, Score, 0);
                        // 試験備考格納
                        scoreRemakeMap.put(Score._testSubclassCd,Score._scoreRemark);
                    } else if (Score._testSubclassCd.equals(TESTSUBCLASSCD_SCIENCE)) {
                        
                        if ((str != null &&
                                (str.equals(LINK_JUDGE_DIV_ON) &&
                                        Score._testDiv.compareTo(_param._testDiv) <= 0)) ||
                                        (str == null &&
                                                (Score._testDiv.compareTo(_param._testDiv) == 0))) {
                            if (Score._stdScore > maxScienceScore) {
                                maxScienceScore = Score._stdScore;
                                prtScienceScore(i, scienceScore, Score, 1);
                            }
                        }

                        prtScienceScore(i, scienceScore, Score, 0);
                        // 試験備考格納
                        scoreRemakeMap.put(Score._testSubclassCd,Score._scoreRemark);
                    } else if (Score._testSubclassCd.equals(TESTSUBCLASSCD_SOCIETY)) {
                        
                        if ((str != null &&
                                (str.equals(LINK_JUDGE_DIV_ON) &&
                                        Score._testDiv.compareTo(_param._testDiv) <= 0)) ||
                                        (str == null &&
                                                (Score._testDiv.compareTo(_param._testDiv) == 0))) {
                            if (Score._stdScore > maxSocietyScore) {
                                maxSocietyScore = Score._stdScore;
                                prtSocietyScore(i, societyScore, Score, 1);
                            }
                        }

                        prtSocietyScore(i, societyScore, Score, 0);
                        // 試験備考格納
                        scoreRemakeMap.put(Score._testSubclassCd,Score._scoreRemark);
                    }

                    
//                }            
            }

            // 試験備考設定
            _form._svf.VrsOutn(_form.getAreaTestRemark(_form.getTestRemarkVal(scoreRemakeMap)),
            		i, _form.getTestRemarkVal(scoreRemakeMap));

            prtUnderLine(
                    i,
                    nationalScore,
                    arithmeticScore,
                    scienceScore,
                    societyScore,
                    maxNationalScore,
                    maxArithmeticScore,
                    maxScienceScore,
                    maxSocietyScore,
                    applicant._linkJudgeDeviationDiv
            );
        }

        private void prtSocietyScore(final int i, double[] societyScore, final EntexamScoreDat Score, final int flg) {
            if (Score._testDiv.equals(TESTDIV_A1)) {
                if (flg == 1) societyScore[0] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE12", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_B)) {
                if (flg == 1) societyScore[1] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE13", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_C)) {
                if (flg == 1) societyScore[2] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE14", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_D)) {
                if (flg == 1) societyScore[3] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE18", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            }
        }

        private void prtScienceScore(final int i, double[] scienceScore, final EntexamScoreDat Score, final int flg) {
            if (Score._testDiv.equals(TESTDIV_A1)) {
                if (flg == 1) scienceScore[0] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE9", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_B)) {
                if (flg == 1) scienceScore[1] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE10", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_C)) {
                if (flg == 1) scienceScore[2] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE11", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_D)) {
                if (flg == 1) scienceScore[3] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE17", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            }
        }

        private void prtArithmeticScore(final int i, double[] arithmeticScore, final EntexamScoreDat Score, final int flg) {
            if (Score._testDiv.equals(TESTDIV_A1)) {
                if (flg == 1) arithmeticScore[0] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE5", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_A2)) {
                if (flg == 1) arithmeticScore[1] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE6", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_B)) {
                if (flg == 1) arithmeticScore[2] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE7", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_C)) {
                if (flg == 1) arithmeticScore[3] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE8", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_D)) {
                if (flg == 1) arithmeticScore[4] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE16", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            }
        }

        private void prtNationalScore(final int i, double[] nationalScore, final EntexamScoreDat Score, final int flg) {
            if (Score._testDiv.equals(TESTDIV_A1)) {
                if (flg == 1) nationalScore[0] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE1", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_A2)) {
                if (flg == 1) nationalScore[1] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE2", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_B)) {
                if (flg == 1) nationalScore[2] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE3", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_C)) {
                if (flg == 1) nationalScore[3] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE4", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            } else if (Score._testDiv.equals(TESTDIV_D)) {
                if (flg == 1) nationalScore[4] = Score._stdScore;
                else          _form._svf.VrsOutn("DEVIATION_SCORE15", i, String.valueOf(getRoundingOffNum(Score._stdScore)));
            }
        }

        private String getTestRemarkVal(HashMap scoreMap) {

            String[] scoreRemake = new String[4];
            scoreRemake[0] = nvlT((String)scoreMap.get(TESTSUBCLASSCD_NATIONAL_LANG));	// 国語備考
            scoreRemake[1] = nvlT((String)scoreMap.get(TESTSUBCLASSCD_ARITHMETIC));		// 算数備考
            scoreRemake[2] = nvlT((String)scoreMap.get(TESTSUBCLASSCD_SOCIETY));		// 社会備考
            scoreRemake[3] = nvlT((String)scoreMap.get(TESTSUBCLASSCD_SCIENCE));		// 理科備考
            
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
    	
        private void prtUnderLine(
                final int i,
                double[] nationalScore,
                double[] arithmeticScore,
                double[] scienceScore,
                double[] societyScore,
                double maxNationalScore,
                double maxArithmeticScore,
                double maxScienceScore,
                double maxSocietyScore,
                String judgeDeviationDiv
        ) {
            /** 国語アンダーライン */
            for (int j = 0; j < nationalScore.length; j++) {
                if ((nationalScore[j] != 0) && (nationalScore[j] == maxNationalScore)) {
                    if (j == 0) {
                        _form._svf.VrsOutn("FLG1", i, "1");
                    } if (j == 1) {
                        _form._svf.VrsOutn("FLG1", i, "2");
                    } if (j == 2) {
                        _form._svf.VrsOutn("FLG1", i, "3");
                    } if (j == 3) {
                        _form._svf.VrsOutn("FLG1", i, "4");
                    } if (j == 4) {
                        _form._svf.VrsOutn("FLG1", i, "5");
                    }                    
                }
            }

            /** 算数アンダーライン */
            for (int j = 0; j < arithmeticScore.length; j++) {
                if ((arithmeticScore[j] != 0) && (arithmeticScore[j] == maxArithmeticScore)) {
                    if (j == 0) {
                        _form._svf.VrsOutn("FLG2", i, "1");
                    } if (j == 1) {
                        _form._svf.VrsOutn("FLG2", i, "2");
                    } if (j == 2) {
                        _form._svf.VrsOutn("FLG2", i, "3");
                    } if (j == 3) {
                        _form._svf.VrsOutn("FLG2", i, "4");
                    } if (j == 4) {
                        _form._svf.VrsOutn("FLG2", i, "5");
                    }                    
                }
            }

            if (judgeDeviationDiv.equals(LINK_JUDGE_DEVIATION_DIV_3)) {
                /** 理科アンダーライン */
                if (maxScienceScore >= maxSocietyScore) {
                    prtUnderlineScience(i, scienceScore, maxScienceScore);
                }

                /** 社会アンダーライン */
                if (maxSocietyScore >= maxScienceScore) {
                    prtUnderlineSociety(i, societyScore, maxSocietyScore);
                }
            } else {
                prtUnderlineScience(i, scienceScore, maxScienceScore);
                prtUnderlineSociety(i, societyScore, maxSocietyScore);
            }
        }

        private void prtUnderlineSociety(final int i, double[] societyScore, double maxSocietyScore) {
            for (int j = 0; j < societyScore.length; j++) {
                if ((societyScore[j] != 0) && (societyScore[j] == maxSocietyScore)) {
                    if (j == 0) {
                        _form._svf.VrsOutn("FLG4_1", i, "1");
                    } if (j == 1) {
                        _form._svf.VrsOutn("FLG4_2", i, "1");
                    } if (j == 2) {
                        _form._svf.VrsOutn("FLG4_3", i, "1");
                    } if (j == 3) {
                        _form._svf.VrsOutn("FLG4_4", i, "1");
                    }                    
                }
            }
        }

        private void prtUnderlineScience(final int i, double[] scienceScore, double maxScienceScore) {
            for (int j = 0; j < scienceScore.length; j++) {
                if ((scienceScore[j] != 0) && (scienceScore[j] == maxScienceScore)) {
                    if (j == 0) {
                        _form._svf.VrsOutn("FLG3_1", i, "1");
                    } if (j == 1) {
                        _form._svf.VrsOutn("FLG3_2", i, "1");
                    } if (j == 2) {
                        _form._svf.VrsOutn("FLG3_3", i, "1");
                    } if (j == 3) {
                        _form._svf.VrsOutn("FLG3_4", i, "1");
                    }                    
                }
            }
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

        public String getAreaSiganBiko(String val, int areaLen) {
            if (val.length() <= areaLen) {
                return "REMARK1_1";
            } else {
                return "REMARK1_2";
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
     * 志願者データ。
     */
    class EntexamDesireDat {
        private final String _applicantDiv;         // 入試制度
        private final String _testDiv;              // 入試区分
        private final String _examType;             // 受験型
        private final String _examNo;               // 受験番号
        private final String _receptNo;             // 受付№
        private final double _judgeDeviation;      // 判定偏差値
        private final String _linkJudgeDeviationDiv;        // リンク判定偏差値区分

        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ
        private List _SCORE;

        EntexamDesireDat(
                final String applicantDiv,
                final String testDiv,
                final String examType,
                final String examNo,
                final String receptNo,
                final double judgeDeviation,
                final String linkJudgeDeviationDiv
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examType = examType;
            _examNo = examNo;
            _receptNo = receptNo;
            _judgeDeviation = judgeDeviation;
            _linkJudgeDeviationDiv = linkJudgeDeviationDiv;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, _applicantDiv, _examNo);
            _SCORE = createEntexamScoreDats(db2, _examNo);
        }
    }

    private List createEntexamDesireDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamDesireDats());
            rs = ps.executeQuery();

            BigDecimal bd = null;
            double scale1 = 0.0;

            while (rs.next()) {
                final String applicantDiv = rs.getString("applicantDiv");
                final String testDiv = rs.getString("testDiv");
                final String examType = rs.getString("examType");
                final String examNo = rs.getString("examNo");
                final String receptNo = rs.getString("receptNo");
                final String linkJudgeDeviationDiv = rs.getString("linkJudgeDeviationDiv");

                bd = new BigDecimal(rs.getString("judgeDeviation"));            
                scale1 = bd.setScale(2, BigDecimal.ROUND_UNNECESSARY).doubleValue();
                final double judgeDeviation = scale1;


                final EntexamDesireDat entexamDesireDat = new EntexamDesireDat(
                        applicantDiv,
                        testDiv,
                        examType,
                        examNo,
                        receptNo,
                        judgeDeviation,
                        linkJudgeDeviationDiv
                );

                entexamDesireDat.load(db2);
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

    private String sqlEntexamDesireDats() {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    T1.ENTEXAMYEAR as entExamYear,");
        stb.append("    T1.APPLICANTDIV as applicantDiv,");
        stb.append("    T1.TESTDIV as testDiv,");
        stb.append("    T1.EXAM_TYPE as examType,");
        stb.append("    T1.EXAMNO as examNo,");
        stb.append("    T2.RECEPTNO as receptNo,");
        stb.append("    value(T2.LINK_JUDGE_DEVIATION, 0.0) as judgeDeviation,");
        stb.append("    value(T2.LINK_JUDGE_DEVIATION_DIV, '') as linkJudgeDeviationDiv");
        stb.append(" from");
        stb.append("    ENTEXAM_DESIRE_DAT T1 left join ENTEXAM_RECEPT_DAT T2 on (");
        stb.append("    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR");
        stb.append("    and T2.APPLICANTDIV    = T1.APPLICANTDIV");
        stb.append("    and T2.TESTDIV      = T1.TESTDIV");
        stb.append("    and T2.EXAM_TYPE   = T1.EXAM_TYPE");
        stb.append("    and T2.EXAMNO      = T1.EXAMNO)");
        stb.append(" where");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append(" order by judgeDeviation desc");

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
        throws SQLException {

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
     * 志願者得点備考データ。
     */
    class EntexamScoreDat {
        private final String _testDiv;          // 入試区分
        private final String _testSubclassCd;   // 試験科目コード
        private final double _stdScore;        // 偏差値
        private final String _scoreRemark;      // 試験備考

        EntexamScoreDat(
                final String testDiv,
                final String testSubclassCd,
                final double stdScore,
                final String scoreRemark
        ) {
            _testDiv = testDiv;
            _testSubclassCd = testSubclassCd;
            _stdScore = stdScore;
            _scoreRemark = scoreRemark;
        }
    }

    private List createEntexamScoreDats(final DB2UDB db2, String pExamNo)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamScoreDats(pExamNo));
            rs = ps.executeQuery();

            BigDecimal bd = null;
            double scale1 = 0.0;

            while (rs.next()) {
                final String testDiv = rs.getString("testDiv");
                final String testSubclassCd = rs.getString("testSubclassCd");
                final String scoreRemark = rs.getString("scoreRemark");

                bd = new BigDecimal(rs.getString("stdScore"));            
                scale1 = bd.setScale(2, BigDecimal.ROUND_UNNECESSARY).doubleValue();
                final double stdScore = scale1;

                final EntexamScoreDat entexamScoreDat = new EntexamScoreDat(
                        testDiv,
                        testSubclassCd,
                        stdScore,
                        scoreRemark
                );
                rtn.add(entexamScoreDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String sqlEntexamScoreDats(String pExamNo) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    value(T3.TESTDIV, '') as testDiv,");
        stb.append("    value(T3.TESTSUBCLASSCD, '') as testSubclassCd,");
        stb.append("    value(T3.STD_SCORE, 0.0) as stdScore,");
        stb.append("    value(T4.REMARK, '') as scoreRemark");
        stb.append(" from");
        stb.append("    ENTEXAM_DESIRE_DAT T1");
        stb.append("    left join ENTEXAM_RECEPT_DAT T2 on (");
        stb.append("    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR");
        stb.append("    and T2.APPLICANTDIV    = T1.APPLICANTDIV");
        stb.append("    and T2.TESTDIV      = T1.TESTDIV");
        stb.append("    and T2.EXAM_TYPE   = T1.EXAM_TYPE");
        stb.append("    and T2.EXAMNO      = T1.EXAMNO)");
        stb.append("    left join ENTEXAM_SCORE_DAT T3 on (");
        stb.append("    T3.ENTEXAMYEAR  = T2.ENTEXAMYEAR");
        stb.append("    and T3.APPLICANTDIV    = T2.APPLICANTDIV");
        stb.append("    and T3.TESTDIV      = T2.TESTDIV");
        stb.append("    and T3.EXAM_TYPE   = T2.EXAM_TYPE");
        stb.append("    and T3.RECEPTNO      = T2.RECEPTNO");
        stb.append("    and T3.TESTSUBCLASSCD is not NULL)");
        stb.append("    left join ENTEXAM_SCORE_REMARK_DAT T4 on (");
        stb.append("    T4.ENTEXAMYEAR  = T3.ENTEXAMYEAR");
        stb.append("    and T4.APPLICANTDIV    = T3.APPLICANTDIV");
        stb.append("    and T4.TESTDIV      = T3.TESTDIV");
        stb.append("    and T4.EXAM_TYPE   = T3.EXAM_TYPE");
        stb.append("    and T4.RECEPTNO      = T3.RECEPTNO");
        stb.append("    and T4.TESTSUBCLASSCD = T3.TESTSUBCLASSCD");
        stb.append("    and T4.TESTSUBCLASSCD is not NULL)");
        stb.append(" where");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._year + "'");
        stb.append("    and T1.EXAMNO = '" + pExamNo + "'");
        stb.append("    and T1.TESTDIV is not NULL");
        stb.append("    and T1.TESTDIV <= '" + _param._testDiv + "'");
        stb.append(" order by T3.TESTSUBCLASSCD, T1.TESTDIV");

        return stb.toString();
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
                + "    PRISCHOOLCD,"
                + "    PRISCHOOL_NAME as priSchoolName"
                + " from"
                + "    PRISCHOOL_MST"
                ;
    }

    // ======================================================================
    /**
     * 入試試験科目データ。
     */
    private Map createEntexamTestsubclasscdDatMap() throws SQLException {
        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlEntexamTestsubclasscdDat());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String code = rs.getString("entexamyear")
                    + rs.getString("applicantdiv")
                    + rs.getString("testdiv")
                    + rs.getString("examType")
                    + rs.getString("testsubclasscd");
                final String name = rs.getString("linkJudgeDiv");
                rtn.put(code, name);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return rtn;
    }

    private String sqlEntexamTestsubclasscdDat() {
        return " select"
                + "    ENTEXAMYEAR as entexamyear,"
                + "    APPLICANTDIV as applicantdiv,"
                + "    TESTDIV as testdiv,"
                + "    EXAM_TYPE as examType,"
                + "    TESTSUBCLASSCD as testsubclasscd,"
                + "    LINK_JUDGE_DIV as linkJudgeDiv"
                + " from"
                + "    ENTEXAM_TESTSUBCLASSCD_DAT"
                ;
    }
} // KNJL371J

// eof
