// kanji=漢字
/*
 * $Id: 62b03ea07d086f5d8b1624f095b33dd9f13364b4 $
 *
 * 作成日: 2007/12/28 09:04:00 - JST
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
 *  入学試験成績統計
 * @author nakada
 * @version $Id: 62b03ea07d086f5d8b1624f095b33dd9f13364b4 $
 */
public class KNJL325J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL325J.class);


    /*
     * 最大得点
     */
    /** 最大得点 */
    private static final String JUDGEDIV_PASS = "1";

    /*
     * 合否区分
     */
    /** 合格 */
    private static final int SCORE_MAX = 99999;

    /*
     * 入試区分
     */
    /** Ａ-１ */
    private static final String TESTDIV_A1 = "1";
    /** Ａ-２ */
    private static final String TESTDIV_A2 = "2";
    /** Ｂ */
    private static final String TESTDIV_B = "3";
    /** Ｃ */
    private static final String TESTDIV_C = "4";
    /** Ｄ */
    private static final String TESTDIV_D = "6";
    /** 帰国生 */
    private static final String TESTDIV_R = "5";

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
    /** 国算計： */
    private static final String TESTSUBCLASSCD_TSC_2 = "TSC_2";
    /** ４科計： */
    private static final String TESTSUBCLASSCD_TSC_4 = "TSC_4";

    /*
     * 受験型コード
     */
    /** ２科： */
    private static final String EXAM_TYPE2 = "1";
    /** ４科： */
    private static final String EXAM_TYPE4 = "2";

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

        _form = new Form(response);

        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            final List entexamReceptDats = createEntexamReceptDats(db2);

            printMain(createSumList(entexamReceptDats));

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List sumLists ) 
        throws SQLException, Exception {
    
        final String formName = (_param._infuruFlg) ? "KNJL325J_2.frm" : "KNJL325J.frm";
        _form._svf.VrSetForm(formName, 1);

        for (Iterator it = sumLists.iterator(); it.hasNext();) {
            final SumList sumList = (SumList) it.next();
                
            printApplicant(sumList);
        }

        printHeader();

        _form._svf.VrEndPage();
        _hasData = true;
    }

    private void printHeader() {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* 国語満点 */
        _form._svf.VrsOut("SCORE1", _param._perfectScoreInt(TESTSUBCLASSCD_NATIONAL_LANG));
        /* 算数満点 */
        _form._svf.VrsOut("SCORE2", _param._perfectScoreInt(TESTSUBCLASSCD_ARITHMETIC));
        /* 理科満点 */
        _form._svf.VrsOut("SCORE3", _param._perfectScoreInt(TESTSUBCLASSCD_SCIENCE));
        /* 社会満点 */
        _form._svf.VrsOut("SCORE4", _param._perfectScoreInt(TESTSUBCLASSCD_SOCIETY));
    }

    private void printApplicant(SumList sumList) {

        
        log.debug(">>_testSubclassCd=" + sumList._testSubclassCd);
        log.debug(">>_testdiv=" + sumList._testdiv);
        log.debug(">>_axisY=" + sumList._axisY);
        log.debug(">>_score=" + sumList._score);
        
        
        int i = getTestDivIndex(sumList._testdiv) + 1;

        final int setScore = (int) sumList._score;

        if (sumList._testSubclassCd.equals(TESTSUBCLASSCD_NATIONAL_LANG)) {
            if (sumList._axisY == 1) {
                _form._svf.VrsOutn("AVERAGE1", i, String.valueOf(sumList._score));
            } else if (sumList._axisY == 2) {
                _form._svf.VrsOutn("PASS_AVERAGE1", i, String.valueOf(sumList._score));
            } else if (sumList._axisY == 3) {
                _form._svf.VrsOutn("PASSLOW1", i, String.valueOf(setScore));
            } else if (sumList._axisY == 4) {
                _form._svf.VrsOutn("PASSHIGH1", i, String.valueOf(setScore));
            }
        } else if (sumList._testSubclassCd.equals(TESTSUBCLASSCD_ARITHMETIC)) {
            if (sumList._axisY == 1) {
                _form._svf.VrsOutn("AVERAGE2", i, String.valueOf(sumList._score));
            } else if (sumList._axisY == 2) {
                _form._svf.VrsOutn("PASS_AVERAGE2", i, String.valueOf(sumList._score));
            } else if (sumList._axisY == 3) {
                _form._svf.VrsOutn("PASSLOW2", i, String.valueOf(setScore));
            } else if (sumList._axisY == 4) {
                _form._svf.VrsOutn("PASSHIGH2", i, String.valueOf(setScore));
            }
        } else if (sumList._testSubclassCd.equals(TESTSUBCLASSCD_SCIENCE)) {
            if (i != 2) {
                if (sumList._axisY == 1) {
                    _form._svf.VrsOutn("AVERAGE3", i, String.valueOf(sumList._score));
                } else if (sumList._axisY == 2) {
                    _form._svf.VrsOutn("PASS_AVERAGE3", i, String.valueOf(sumList._score));
                } else if (sumList._axisY == 3) {
                    _form._svf.VrsOutn("PASSLOW3", i, String.valueOf(setScore));
                } else if (sumList._axisY == 4) {
                    _form._svf.VrsOutn("PASSHIGH3", i, String.valueOf(setScore));
                }
            }
        } else if (sumList._testSubclassCd.equals(TESTSUBCLASSCD_SOCIETY)) {
            if (i != 2) {
                if (sumList._axisY == 1) {
                    _form._svf.VrsOutn("AVERAGE4", i, String.valueOf(sumList._score));
                } else if (sumList._axisY == 2) {
                    _form._svf.VrsOutn("PASS_AVERAGE4", i, String.valueOf(sumList._score));
                } else if (sumList._axisY == 3) {
                    _form._svf.VrsOutn("PASSLOW4", i, String.valueOf(setScore));
                } else if (sumList._axisY == 4) {
                    _form._svf.VrsOutn("PASSHIGH4", i, String.valueOf(setScore));
                }
            }
        } else if (sumList._testSubclassCd.equals(TESTSUBCLASSCD_TSC_2)) {
            if (sumList._axisY == 1) {
                _form._svf.VrsOutn("AVERAGE5", i, String.valueOf(sumList._score));
            } else if (sumList._axisY == 2) {
                _form._svf.VrsOutn("PASS_AVERAGE5", i, String.valueOf(sumList._score));
            } else if (sumList._axisY == 3) {
                _form._svf.VrsOutn("PASSLOW5", i, String.valueOf(setScore));
            } else if (sumList._axisY == 4) {
                _form._svf.VrsOutn("PASSHIGH5", i, String.valueOf(setScore));
            }
        } else if (sumList._testSubclassCd.equals(TESTSUBCLASSCD_TSC_4)) {
            if (i != 2) {
                if (sumList._axisY == 1) {
                    _form._svf.VrsOutn("AVERAGE6", i, String.valueOf(sumList._score));
                } else if (sumList._axisY == 2) {
                    _form._svf.VrsOutn("PASS_AVERAGE6", i, String.valueOf(sumList._score));
                } else if (sumList._axisY == 3) {
                    _form._svf.VrsOutn("PASSLOW6", i, String.valueOf(setScore));
                } else if (sumList._axisY == 4) {
                    _form._svf.VrsOutn("PASSHIGH6", i, String.valueOf(setScore));
                }
            }
        }
    }

    private double getRoundingOffNum(double score) {
        BigDecimal bd = new BigDecimal(String.valueOf(score));
        double scale = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return scale;
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

        private Map _perfectScoreMap;

        private boolean _infuruFlg;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
        }

        public String _perfectScoreInt(String testsubclasscd) {
            return (String) _perfectScoreMap.get(testsubclasscd);
        }

        public void load(DB2UDB db2) throws SQLException {
            _perfectScoreMap = getPerfectScore();
            _infuruFlg = getInfuruFlg(db2);

            return;
        }

        private Map getPerfectScore() throws SQLException {
            final String sql = sqlPerfectScore();
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String score = rs.getString("score");
                    rtn.put(code, score);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlPerfectScore() {
            return " select"
                    + "    TESTSUBCLASSCD as code,"
                    + "    MAX(PERFECT) as score"
                    + " from"
                    + "    ENTEXAM_PERFECT_MST"
                    + "    group by TESTSUBCLASSCD"
                    ;
        }

        private boolean getInfuruFlg(final DB2UDB db2) throws SQLException {
            String str = "";
            final String sql = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1='L017' AND NAMECD2='01'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    str = rs.getString("NAMESPARE1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return "1".equals(str);
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate
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

        public Form(final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
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
     * 志願者受付データ。
     */
    class EntexamReceptDat {
        private final String _applicantDiv;         // 入試制度
        private final String _testDiv;              // 入試区分
        private final String _examType;             // 受験型
        private final String _receptno;             // 受付№
        private final String _judgediv;             // 合否区分

        private final String _testSubclassCd;   // 試験科目コード
        private final int _score;
        private final int _total2;
        private final int _total4;

        EntexamReceptDat(
                final String applicantDiv,
                final String testDiv,
                final String examType,
                final String receptno,
                final String judgediv,
                final String testSubclassCd,
                final int score,
                final int total2,
                final int total4
        ) {
            _applicantDiv = applicantDiv;
            _testDiv = testDiv;
            _examType = examType;
            _receptno = receptno;
            _judgediv = judgediv;
            _testSubclassCd = testSubclassCd;
            _score = score;
            _total2 = total2;
            _total4 = total4;
        }

        public void load(DB2UDB db2, String applicantDiv, String examno) throws SQLException, Exception {
        }
    }

    private List createEntexamReceptDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlEntexamReceptDats());
            rs = ps.executeQuery();

//            BigDecimal bd = null;
//            double scale1 = 0.0;

            while (rs.next()) {
                final String applicantDiv = rs.getString("applicantDiv");
                final String testDiv = rs.getString("testDiv");
                final String examType = rs.getString("examType");
                final String receptno = rs.getString("receptno");
                final String judgediv = rs.getString("judgediv");
                final String testSubclassCd = rs.getString("testSubclassCd");
                final int score = Integer.parseInt(rs.getString("score"));
                final int total2 = Integer.parseInt(rs.getString("total2"));
                final int total4 = Integer.parseInt(rs.getString("total4"));

                final EntexamReceptDat entexamDesireDat = new EntexamReceptDat(
                        applicantDiv,
                        testDiv,
                        examType,
                        receptno,
                        judgediv,
                        testSubclassCd,
                        score,
                        total2,
                        total4
                );

//                entexamDesireDat.load(db2, entexamDesireDat._applicantDiv, entexamDesireDat._examNo);
                rtn.add(entexamDesireDat);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>EntexamReceptDat に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlEntexamReceptDats() {
        return " "
        + " with T_TOTAL2 as ("
        + " select"
        + "    T1.APPLICANTDIV,"
        + "    T1.TESTDIV,"
        + "    T1.EXAM_TYPE,"
        + "    T1.RECEPTNO,"
        + "    sum(case when T2.TESTSUBCLASSCD in('" + TESTSUBCLASSCD_NATIONAL_LANG + "','" + TESTSUBCLASSCD_ARITHMETIC + "') then T2.SCORE end) as total2"
        + " from"
        + "    ENTEXAM_RECEPT_DAT T1"
        + "    inner join ENTEXAM_SCORE_DAT T2 on ("
        + "    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR"
        + "    and T2.APPLICANTDIV    = T1.APPLICANTDIV"
        + "    and T2.TESTDIV      = T1.TESTDIV"
        + "    and T2.EXAM_TYPE   = T1.EXAM_TYPE"
        + "    and T2.RECEPTNO      = T1.RECEPTNO"
        + "    and T2.TESTSUBCLASSCD is not NULL)"
        + " where"
        + "    T1.ENTEXAMYEAR = '" + _param._year + "'"
        + " group by T1.APPLICANTDIV, T1.TESTDIV, T1.EXAM_TYPE, T1.RECEPTNO"
        + "    )"
        + " select"
        + "    T1.APPLICANTDIV as applicantDiv,"
        + "    T1.TESTDIV as testDiv,"
        + "    T1.EXAM_TYPE as examType,"
        + "    T1.RECEPTNO as receptno,"
        + "    value(T3.TOTAL2, 0) as total2,"
        + "    value(T1.TOTAL4, 0) as total4,"
        + "    value(T1.JUDGEDIV, '') as judgediv,"
        + "    value(T2.TESTSUBCLASSCD, '') as testSubclassCd,"
        + "    value(T2.SCORE, 0) as score"
        + " from"
        + "    ENTEXAM_RECEPT_DAT T1"
        + "    inner join ENTEXAM_SCORE_DAT T2 on ("
        + "    T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR"
        + "    and T2.APPLICANTDIV    = T1.APPLICANTDIV"
        + "    and T2.TESTDIV      = T1.TESTDIV"
        + "    and T2.EXAM_TYPE   = T1.EXAM_TYPE"
        + "    and T2.RECEPTNO      = T1.RECEPTNO"
        + "    and T2.TESTSUBCLASSCD is not NULL)"
        + "    left join T_TOTAL2 T3 on ("
        + "    T3.APPLICANTDIV    = T1.APPLICANTDIV"
        + "    and T3.TESTDIV      = T1.TESTDIV"
        + "    and T3.EXAM_TYPE   = T1.EXAM_TYPE"
        + "    and T3.RECEPTNO      = T1.RECEPTNO)"
        + " where"
        + "    T1.ENTEXAMYEAR = '" + _param._year + "'"
        + " order by TESTSUBCLASSCD, TESTDIV"
        ;
    }

    // ======================================================================
    /**
     * 集計ＭＡＰ。
     */
    class SumList {
        private final String _testSubclassCd;
        private final int _axisY;              // 集計得点種別
        private final String _testdiv;
        private final double _score;

        SumList(
                final String testSubclassCd,
                final int axisY,
                final String testdiv,
                final double score
        ) {
            _testSubclassCd = testSubclassCd;
            _axisY = axisY;
            _testdiv = testdiv;
            _score = score;
        }
    }

    private List createSumList(final List entexamReceptDats) {

        List rtn = new ArrayList();

        /** 平均点 */
        double[] avrScore = {0, 0, 0, 0, 0}; 
        /** 合格者平均点 */
        double[] passAvrScore = {0, 0, 0, 0, 0}; 
        /** 合格者最低点 */
        double[] passAvrScoreMin = {SCORE_MAX, SCORE_MAX, SCORE_MAX, SCORE_MAX, SCORE_MAX}; 
        /** 合格者最高点 */
        double[] passAvrScoreMax = {0, 0, 0, 0, 0}; 
        int[] avrScoreCnt = {0, 0, 0, 0, 0};
        int[] passAvrScoreCnt = {0, 0, 0, 0, 0};

        /** ２科 */
        double[] avrTsc2Score = {0, 0, 0, 0, 0}; 
        /** 合格者平均点 */
        double[] passAvrTsc2Score = {0, 0, 0, 0, 0}; 
        /** ２科合格者最低点 */
        double[] passAvrTsc2ScoreMin = {SCORE_MAX, SCORE_MAX, SCORE_MAX, SCORE_MAX, SCORE_MAX}; 
        /** ２科合格者最高点 */
        double[] passAvrTsc2ScoreMax = {0, 0, 0, 0, 0}; 
        int[] avrTsc2ScoreCnt = {0, 0, 0, 0, 0};
        int[] passAvrTsc2ScoreCnt = {0, 0, 0, 0, 0};

        /** ４科 */
        double[] avrTsc4Score = {0, 0, 0, 0, 0}; 
        /** ４科合格者平均点 */
        double[] passAvrTsc4Score = {0, 0, 0, 0, 0}; 
        /** ４科合格者最低点 */
        double[] passAvrTsc4ScoreMin = {SCORE_MAX, SCORE_MAX, SCORE_MAX, SCORE_MAX, SCORE_MAX}; 
        /** ４科合格者最高点 */
        double[] passAvrTsc4ScoreMax = {0, 0, 0, 0, 0}; 
        int[] avrTsc4ScoreCnt = {0, 0, 0, 0, 0};
        int[] passAvrTsc4ScoreCnt = {0, 0, 0, 0, 0};

        String oldTestSubclassCd = null;

        for (Iterator it = entexamReceptDats.iterator(); it.hasNext();) {
            final EntexamReceptDat entexamReceptDat = (EntexamReceptDat) it.next();

            if (entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_NATIONAL_LANG) ||
                    entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_ARITHMETIC) ||
                    entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_SCIENCE) ||
                    entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_SOCIETY)) {
                if ((entexamReceptDat._testDiv.equals(TESTDIV_A1) ||
                        entexamReceptDat._testDiv.equals(TESTDIV_A2) ||
                        entexamReceptDat._testDiv.equals(TESTDIV_B)  ||
                        entexamReceptDat._testDiv.equals(TESTDIV_C)  ||
                        entexamReceptDat._testDiv.equals(TESTDIV_D))) {

                    // 入試区分格納位置の取得
                    final int i = getTestDivIndex(entexamReceptDat._testDiv);

                    if (oldTestSubclassCd == null) {
                        oldTestSubclassCd = entexamReceptDat._testSubclassCd;
//                        oldTestDiv = entexamReceptDat._testDiv;
        
                        passAvrScoreMin[i] = SCORE_MAX;
                        passAvrScoreMax[i] = 0;
                    }

                    if (!entexamReceptDat._testSubclassCd.equals(oldTestSubclassCd)) {
                        rtn = addSucore(
                                rtn, 
                                avrScore,
                                passAvrScore,
                                passAvrScoreMin,
                                passAvrScoreMax,
                                avrScoreCnt,
                                passAvrScoreCnt,
                                oldTestSubclassCd
                        );

                        for (int j = 0; j < 5; j++) {
                            avrScore[j] = 0;
                            passAvrScore[j] = 0;
                            passAvrScoreMin[j] = SCORE_MAX;
                            passAvrScoreMax[j] = 0;
                            avrScoreCnt[j] = 0;
                            passAvrScoreCnt[j] = 0;
                        }

                        oldTestSubclassCd = entexamReceptDat._testSubclassCd;
//                        oldTestDiv = entexamReceptDat._testDiv;
                    }

                    // 平均点
                    avrScore[i] += (double)entexamReceptDat._score;
                    avrScoreCnt[i]++;
        
                    if (entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_NATIONAL_LANG) ||
                            entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_ARITHMETIC)) {
                        if (entexamReceptDat._examType.equals(EXAM_TYPE2)) {
                        
                            avrTsc2Score[i] += (double)entexamReceptDat._total2;
                            avrTsc2ScoreCnt[i]++;
                        }
                    }
        
                    if (entexamReceptDat._examType.equals(EXAM_TYPE4)) {
                        
                        avrTsc4Score[i] += (double)entexamReceptDat._total4;
                        avrTsc4ScoreCnt[i]++;
                    }
        
                    if (entexamReceptDat._judgediv.equals(JUDGEDIV_PASS)) {
                        passAvrScoreCnt[i]++;

                        // 合格者平均点
                        passAvrScore[i] += (double)entexamReceptDat._score;

                        // 合格者最低点
                        if ((double)entexamReceptDat._score < passAvrScoreMin[i]) {
                            passAvrScoreMin[i] = (double)entexamReceptDat._score;
                        }

                        // 合格者最高点
                        if ((double)entexamReceptDat._score > passAvrScoreMax[i]) {
                            passAvrScoreMax[i] = (double)entexamReceptDat._score;
                        }

                        if (entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_NATIONAL_LANG) ||
                                entexamReceptDat._testSubclassCd.equals(TESTSUBCLASSCD_ARITHMETIC)) {
                            if (entexamReceptDat._examType.equals(EXAM_TYPE2)) {
        
                                // ２科合格者平均点
                                passAvrTsc2Score[i] += (double)entexamReceptDat._total2;
                                
                                // ２科合格者最低点
                                if ((double)entexamReceptDat._total2 < passAvrTsc2ScoreMin[i]) {
                                    passAvrTsc2ScoreMin[i] = (double)entexamReceptDat._total2;
                                }
                                
                                // ２科合格者最高点
                                if ((double)entexamReceptDat._total2 > passAvrTsc2ScoreMax[i]) {
                                    passAvrTsc2ScoreMax[i] = (double)entexamReceptDat._total2;
                                }
                                
                                passAvrTsc2ScoreCnt[i]++;
                            }
                        }
        
                        if (entexamReceptDat._examType.equals(EXAM_TYPE4)) {
                            
                            // ４科合格者平均点
                            passAvrTsc4Score[i] += (double)entexamReceptDat._total4;
                            
                            // ４科合格者最低点
                            if ((double)entexamReceptDat._total4 < passAvrTsc4ScoreMin[i]) {
                                passAvrTsc4ScoreMin[i] = (double)entexamReceptDat._total4;
                            }
                            
                            // ４科合格者最高点
                            if ((double)entexamReceptDat._total4 > passAvrTsc4ScoreMax[i]) {
                                passAvrTsc4ScoreMax[i] = (double)entexamReceptDat._total4;
                            }
                            
                            passAvrTsc4ScoreCnt[i]++;
                        }
                    }
                }
            }
        }

        if (rtn != null) {
            rtn = addSucore(
                    rtn, 
                    avrScore,
                    passAvrScore,
                    passAvrScoreMin,
                    passAvrScoreMax,
                    avrScoreCnt,
                    passAvrScoreCnt,
                    oldTestSubclassCd
            );

            double comp = 0;
            String str1 = null;

            // ２科目別集計結果設定
            for (int j = 0; j < 5; j++) {

                str1 = getIndexToTestDiv(j);

                if (avrTsc2ScoreCnt[j] != 0) {
                    comp = getRoundingOffNum(avrTsc2Score[j] / (double)avrTsc2ScoreCnt[j]);
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_2, 1, str1, comp));
                }

                if (passAvrTsc2ScoreCnt[j] != 0) {
                    comp = getRoundingOffNum(passAvrTsc2Score[j] / (double)passAvrTsc2ScoreCnt[j]);
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_2, 2, str1, comp));
                    
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_2, 3, str1, passAvrTsc2ScoreMin[j]));
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_2, 4, str1, passAvrTsc2ScoreMax[j]));
                }
            }

            // ４科目別集計結果設定
            for (int j = 0; j < 5; j++) {

                str1 = getIndexToTestDiv(j);

                if (avrTsc4ScoreCnt[j] != 0) {
                    comp = getRoundingOffNum(avrTsc4Score[j] / (double)avrTsc4ScoreCnt[j]);
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_4, 1, str1, comp));
                }

                if (passAvrTsc4ScoreCnt[j] != 0) {
                    comp = getRoundingOffNum(passAvrTsc4Score[j] / (double)passAvrTsc4ScoreCnt[j]);
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_4, 2, str1, comp));
                    
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_4, 3, str1, passAvrTsc4ScoreMin[j]));
                    rtn.add(new SumList(TESTSUBCLASSCD_TSC_4, 4, str1, passAvrTsc4ScoreMax[j]));
                }
            }
        }

        return rtn;
    }

    private List addSucore(
            final List pRtn,
            double[] avrScore,
            double[] passAvrScore,
            double[] passAvrScoreMin,
            double[] passAvrScoreMax,
            int[] avrScoreCnt,
            int[] passAvrScoreCnt,
            String oldTestSubclassCd
    ) {
        final List rtn = pRtn;

        double comp;
        String str1 = null;

        // 科目別集計結果設定
        for (int j = 0; j < 5; j++) {
            str1 = getIndexToTestDiv(j);

            if (avrScoreCnt[j] != 0) {
                comp = getRoundingOffNum(avrScore[j] / (double)avrScoreCnt[j]);
                rtn.add(new SumList(oldTestSubclassCd, 1, str1, comp));
            }

            if (passAvrScoreCnt[j] != 0) {
                comp = getRoundingOffNum(passAvrScore[j] / (double)passAvrScoreCnt[j]);
                rtn.add(new SumList(oldTestSubclassCd, 2, str1, comp));
                
                rtn.add(new SumList(oldTestSubclassCd, 3, str1, passAvrScoreMin[j]));
                rtn.add(new SumList(oldTestSubclassCd, 4, str1, passAvrScoreMax[j]));
            }
        }

        return rtn;
    }

    private int getTestDivIndex(final String testDiv) {
        int i = -1;

        if (testDiv.equals(TESTDIV_A1)) {
            i = 0;
        } else if (testDiv.equals(TESTDIV_A2)) {
            i = 1;
        } else if (testDiv.equals(TESTDIV_B)) {
            i = 2;
        } else if (testDiv.equals(TESTDIV_C)) {
            i = 3;                
        } else if (testDiv.equals(TESTDIV_D)) {
            i = 4;
        }
        return i;
    }

    private String getIndexToTestDiv(final int index) {
        String rtn = null;

        if (String.valueOf(index).equals("0")) {
            rtn = TESTDIV_A1;
        } else if (String.valueOf(index).equals("1")) {
            rtn = TESTDIV_A2;
        } else if (String.valueOf(index).equals("2")) {
            rtn =  TESTDIV_B;
        } else if (String.valueOf(index).equals("3")) {
            rtn =  TESTDIV_C;
        } else if (String.valueOf(index).equals("4")) {
            rtn =  TESTDIV_D;
        }

        return rtn;
    }
} //  KNJL325J
//  eof