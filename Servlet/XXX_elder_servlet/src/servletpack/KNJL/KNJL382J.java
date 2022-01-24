// kanji=漢字
/*
 * $Id: c619827950e29157edb2fa12778db3c534d6928e $
 *
 * 作成日: 2008/02/20 13:16:00 - JST
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
 *  クラス振り分け資料
 * @author nakada
 * @version $Id: c619827950e29157edb2fa12778db3c534d6928e $
 */
public class KNJL382J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL382J.class);

    private static final String FORM_FILE = "KNJL382J.frm";

    /*
     * 文字数による出力項目切り分け基準
     */
    /** 出身校 */
    private static final int FINSCHOOL_NAME_LENG = 12;
    /** 塾名 */
    private static final int PRISCHOOL_NAME_LENG = 10;

    /*
     * タイトル
     */
    /** 仮クラス */
    private static final String TITLE_1_CD = "1";
    /** クラス */
    private static final String TITLE_2_CD = "2";
    /** 合計平均点 */
    private static final String TOTAL_AVERAGE = "平均点";

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";

    /*
     * 性別
     */
    /** 性別：男 */
    private static final String SEX_MAN = "1";

    /*
     * 駅利用
     */
    /** 利用 */
    private static final String STATION_USE = "1";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 40;

    /*
     * 記号
     */
    /** ■ */
    private static final String SIGN1 = "■";

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    Param _param;

    private int _dataCnt;
    private int _page;
    private int _totalPage;
    private int _kokugoTotalScore;
    private int _sansuTotalScore;

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

            final List preSchoolInfoDats = createPreSchoolInfoDats(db2);

            printMain(preSchoolInfoDats);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final List preSchoolInfoDats) 
        throws SQLException, Exception {

        String oldHrClass = "";
        String footHrClass = "";

        if (!preSchoolInfoDats.isEmpty()) {
            final PreSchoolInfoDat preSchoolInfoDat = (PreSchoolInfoDat) preSchoolInfoDats.get(0);

            if (_param._title.equals(TITLE_1_CD)) {
                oldHrClass = preSchoolInfoDat._preHrClass;
            } else {
                oldHrClass = preSchoolInfoDat._hrClass;
            }
        }

        int i = 1;			// １ページあたり件数
        int manNum = 0;		// 男子合計
        int womanNum = 0;	// 女子合計

        PreSchoolInfoDat preSchoolInfoDat = null;

        for (Iterator it = preSchoolInfoDats.iterator(); it.hasNext();) {
            preSchoolInfoDat = (PreSchoolInfoDat) it.next();
            
            if ((_param._title.equals(TITLE_1_CD) && (!preSchoolInfoDat._preHrClass.equals(oldHrClass))) ||
                    (_param._title.equals(TITLE_2_CD) && (!preSchoolInfoDat._hrClass.equals(oldHrClass)))) {

            	footHrClass = oldHrClass;
            	// ページMAX行の場合
                if(i > DETAILS_MAX){
                	++_totalPage;
                    /* 総ページ */
                    _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
                    // ページ出力
                    _form._svf.VrEndPage();
                	// フッタ編集
                	printFooter(manNum, womanNum);
                	// ヘッダ編集
                    printHeader(preSchoolInfoDat);
                    // 合計行編集
                    printTotal(manNum, womanNum);
                    // ページ出力
                    _form._svf.VrEndPage();
                    _kokugoTotalScore = 0;
                    _sansuTotalScore  = 0;
                    manNum = 0;
                    womanNum = 0;
                	_page = 0;
                } else {
                	// フッター編集
                	printFooter(manNum, womanNum);
                    // 合計行編集
                    printTotal(manNum, womanNum);
                    // ページ出力
                    _form._svf.VrEndPage();
                    _kokugoTotalScore = 0;
                    _sansuTotalScore  = 0;
                    manNum = 0;
                    womanNum = 0;
                }
            	i = 1;
            	_page = 0;
            } else {
            	// ページMAX行の場合
                if(i > DETAILS_MAX){
                    // ページ出力
                    _form._svf.VrEndPage();
                	i = 1;
                }
            }

            if (_param._title.equals(TITLE_1_CD)) {
                oldHrClass = preSchoolInfoDat._preHrClass;
            } else {
                oldHrClass = preSchoolInfoDat._hrClass;
            }

            if(i == 1){
                // 同一年組内、件数算出
                _dataCnt = getDataCnt(preSchoolInfoDats, oldHrClass);
                // 総ページ数算出
                _totalPage = _dataCnt / DETAILS_MAX;

                if ((_dataCnt % DETAILS_MAX) != 0) {
                    _totalPage++;
                }
                
                printHeader(preSchoolInfoDat);
            }
            // 生徒情報出力
            printApplicant(preSchoolInfoDat, i);
            // 男女別合計算出
            if (preSchoolInfoDat._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                manNum++;
            } else {
                womanNum++;
            }

            _hasData = true;
            // ページ内行数のカウントアップ
            ++i;
        }

        //最終レコードを出力
		if (_hasData) {
        	// ページMAX行の場合
            if(i > DETAILS_MAX){
                if (!oldHrClass.equals(footHrClass)){
                	++_totalPage;
                    /* 総ページ */
                    _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));
                }
                // ページ出力
                _form._svf.VrEndPage();
            	// フッタ編集
            	printFooter(manNum, womanNum);
            	// ヘッダ編集
                printHeader(preSchoolInfoDat);
                // 合計行編集
                printTotal(manNum, womanNum);
                // ページ出力
                _form._svf.VrEndPage();
            } else {
            	// フッター編集
            	printFooter(manNum, womanNum);
                // 合計行編集
                printTotal(manNum, womanNum);
                // ページ出力
                _form._svf.VrEndPage();
            }
		}

    }

    private void printHeader(PreSchoolInfoDat preSchoolInfoDat) {
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "/" + "1/1") + "度");
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));
        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 総ページ */
        _form._svf.VrsOut("TOTAL_PAGE", String.valueOf(_totalPage));

        if (_param._title.equals(TITLE_1_CD)) {
            /* 仮クラス */
            _form._svf.VrsOut("HR_NAME", _param._schregRegdHdatMapString(
                    _param._year + _param._semester + _param._grade + preSchoolInfoDat._preHrClass));
        } else {
            /* クラス */
            _form._svf.VrsOut("HR_NAME", _param._schregRegdHdatMapString(
                    _param._year + _param._semester + _param._grade + preSchoolInfoDat._hrClass));
        }
    }

    private void printTotal(int manNum, int womanNum) {

    	int totalNum = 0;
        
    	/* 平均点タイトル */
        _form._svf.VrsOut("TOTAL_NAME", TOTAL_AVERAGE);
        totalNum = manNum + womanNum;
        /* 国語平均点 */
        double Class1Average = getRoundingOffNum((double)_kokugoTotalScore / (double)totalNum);
        _form._svf.VrsOut("AVERAGE_CLASS1", String.valueOf(Class1Average));
        /* 算数平均点 */
        double Class2Average = getRoundingOffNum((double)_sansuTotalScore / (double)totalNum);
        _form._svf.VrsOut("AVERAGE_CLASS2", String.valueOf(Class2Average));
    }

    private void printFooter(final int manNum, final int womanNum) {
        /* 性別人数 */
        _form._svf.VrsOut("NOTE", "男 " + Integer.toString(manNum)
                + "名、女 " + Integer.toString(womanNum)
                + "名、合計 " + (Integer.toString(manNum + womanNum)) + "名"
        );
    }

    private int getDataCnt(List classFormationDats, final String hrClass) {
        int cnt = 0;

        for (Iterator it = classFormationDats.iterator(); it.hasNext();) {
            final PreSchoolInfoDat preSchoolInfoDat = (PreSchoolInfoDat) it.next();

            if ((_param._title.equals(TITLE_1_CD) && (preSchoolInfoDat._preHrClass.equals(hrClass))) ||
                    (_param._title.equals(TITLE_2_CD) && (preSchoolInfoDat._hrClass.equals(hrClass)))) {
                cnt++;
            }
        }

        return cnt;
    }

    private void printApplicant(PreSchoolInfoDat preSchoolInfoDat, final int i) {
        if (_param._title.equals(TITLE_1_CD)) {
            if (nvlT(preSchoolInfoDat._preAttendno).length() != 0) {
                int int1 = Integer.parseInt(preSchoolInfoDat._preAttendno);
                /* NO */
                _form._svf.VrsOutn("ATTENDNO", i, Integer.toString(int1));
            }
        } else {
            if (nvlT(preSchoolInfoDat._attendno).length() != 0) {
                /* NO */
                int int1 = Integer.parseInt(preSchoolInfoDat._attendno);
                _form._svf.VrsOutn("ATTENDNO", i, Integer.toString(int1));
            }
        }

        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, preSchoolInfoDat._examno);
        /* カナ名前 */
        _form._svf.VrsOutn("NAME_KANA", i, preSchoolInfoDat._entexamApplicantbaseDat._nameKana);
        /* 名前 */
        _form._svf.VrsOutn("NAME", i, preSchoolInfoDat._entexamApplicantbaseDat._name);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(preSchoolInfoDat._entexamApplicantbaseDat._sex));
        /* 平塚バス */
        if ((nvlT(preSchoolInfoDat._stationcd1).length() != 0) ||
                (nvlT(preSchoolInfoDat._stationcd2).length() != 0) ||
                (nvlT(preSchoolInfoDat._stationcd3).length() != 0) ||
                (nvlT(preSchoolInfoDat._stationcd4).length() != 0)) {

            _form._svf.VrsOutn("BUS", i, SIGN1);
        }

        int scoreSum = 0;
        if (nvlT(preSchoolInfoDat._score1).length() != 0) {
            /* 国語 */
            int int1 = Integer.parseInt(preSchoolInfoDat._score1);
            _form._svf.VrsOutn("SCORE1", i, Integer.toString(int1));
            scoreSum += int1;
            _kokugoTotalScore += int1;
        }

        if (nvlT(preSchoolInfoDat._score2).length() != 0) {
            /* 算数 */
            int int1 = Integer.parseInt(preSchoolInfoDat._score2);
            _form._svf.VrsOutn("SCORE2", i, Integer.toString(int1));
            scoreSum += int1;
            _sansuTotalScore += int1;
        }
        
        if ((nvlT(preSchoolInfoDat._totalScore).length() != 0)){
            int int1 = Integer.parseInt(preSchoolInfoDat._totalScore);
            /* 合計 */
            _form._svf.VrsOutn("TOTAL_SCORE", i, Integer.toString(int1));
        }

        /* 出身校 */
        _form.printFinschoolName(preSchoolInfoDat, i);
        /* 塾名／塾兄弟情報 */
        _form.printRemark(preSchoolInfoDat, i);
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
        private final String _title;
        private final String _grade;

        private Map _sexMap;
        private Map _schregRegdHdatMap;
        private Map _priSchoolName;
        private Map _finschoolName;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String title,
                final String grade
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _title = title;
            _grade = grade;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregRegdHdatMap = createPreSchoolHdat(db2);
            _sexMap = getNameMst(NAME_MST_SEX);
            _priSchoolName = createPriSchoolMst0();
            _finschoolName = createFinschoolMst();

            return;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String _schregRegdHdatMapString(String code) {
            return nvlT((String)_schregRegdHdatMap.get(code));
        }

        public String _priSchoolNameString(String priSchoolCd) {
            return (String) _priSchoolName.get(priSchoolCd);
        }

        public String _priFinschoolNameString(String priSchoolCd) {
            return (String) _finschoolName.get(priSchoolCd);
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
         * 出身学校マスタ。
         */
        private Map createFinschoolMst() throws SQLException {
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlFinschoolMst());
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
        
        

        private String sqlFinschoolMst() {
            return " select"
                    + "    FINSCHOOLCD as code,"
                    + "    FINSCHOOL_NAME as name"
                    + " from"
                    + "    FINSCHOOL_MST"
                    ;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String title = request.getParameter("TITLE");
        final String grade = request.getParameter("GRADE");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                title,
                grade
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

        public void printFinschoolName(PreSchoolInfoDat preSchoolInfoDat, int i) {

            String name = nvlT(_param._priFinschoolNameString(nvlT(preSchoolInfoDat._entexamApplicantbaseDat._fsCd)));

            if (name.length() != 0) {
                final String label;

                if (name.length() <= FINSCHOOL_NAME_LENG) {
                    label = "FINSCHOOL_NAME1";
                } else {
                    label = "FINSCHOOL_NAME2_1";
                }

                _form._svf.VrsOutn(label, i, name);
            }
        }

        public void printRemark(PreSchoolInfoDat preSchoolInfoDat, int i) {
            String str1 = nvlT(_param._priSchoolNameString(nvlT(preSchoolInfoDat._entexamApplicantbaseDat._prischoolcd)));
            String str2 = nvlT(preSchoolInfoDat._entexamApplicantbaseDat._remark1);

            if ((str1.length() != 0) &&
                    (str2.length() != 0)) {

                str1 += "／";
            }

            str1 += str2;

            final String label;

            if (str1.length() <= PRISCHOOL_NAME_LENG) {
                label = "PRISCHOOL_NAME1";
            } else {
                label = "PRISCHOOL_NAME2_1";
            }

            _form._svf.VrsOutn(label, i, str1);
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
     * プレスクールヘッダデータ
     */
    public Map createPreSchoolHdat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlPreSchoolHdat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String year = rs.getString("year");
            final String semester = rs.getString("semester");
            final String grade = rs.getString("grade");
            final String hrClass = rs.getString("hrClass");
            final String hrName = rs.getString("hrName");

            rtn.put(year + semester + grade + hrClass, hrName);
        }

        return rtn;
    }

    private String sqlPreSchoolHdat() {
        return " select"
                + "    YEAR as year,"
                + "    SEMESTER as semester,"
                + "    GRADE as grade,"
                + "    HR_CLASS as hrClass,"
                + "    HR_NAMEABBV as hrName"
                + " from"
                + "    PRE_SCHOOL_HDAT"
                + " where"
                + "    YEAR = '" + _param._year + "'"
                + "    and SEMESTER = '" + _param._semester + "'"
                + "    and GRADE = '" + _param._grade + "'"
                ;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {
        if (val == null || val.length() == 0) {
            return "";
        } else {
            return val;
        }
    }

    // ======================================================================
    /**
     * プレスクール情報データ。
     */
    private class PreSchoolInfoDat {
        private final String _examno;
        private final String _hrClass;
        private final String _attendno;
        private final String _preHrClass;
        private final String _preAttendno;
        private final String _attendflg1;
        private final String _score1;
        private final String _score2;
        private final String _totalScore;
        private final String _stationcd1;
        private final String _stationcd2;
        private final String _stationcd3;
        private final String _stationcd4;

        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ

        PreSchoolInfoDat(
                final String examno,
                final String hrClass,
                final String attendno,
                final String preHrClass,
                final String preAttendno,
                final String attendflg1,
                final String score1,
                final String score2,
                final String totalScore,
                final String stationcd1,
                final String stationcd2,
                final String stationcd3,
                final String stationcd4
        ) {
            _examno = examno;
            _hrClass = hrClass;
            _attendno = attendno;
            _preHrClass = preHrClass;
            _preAttendno = preAttendno;
            _attendflg1 = attendflg1;
            _score1 = score1;
            _score2 = score2;
            _totalScore = totalScore;
            _stationcd1 = stationcd1;
            _stationcd2 = stationcd2;
            _stationcd3 = stationcd3;
            _stationcd4 = stationcd4;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, _examno);
        }
    }

    private List createPreSchoolInfoDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlPreSchoolInfoDats());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String examno = rs.getString("examno");
            final String hrClass = rs.getString("hrClass");
            final String attendno = rs.getString("attendno");
            final String preHrClass = rs.getString("preHrClass");
            final String preAttendno = rs.getString("preAttendno");
            final String attendflg1 = rs.getString("attendflg1");
            final String score1 = rs.getString("score1");
            final String score2 = rs.getString("score2");
            final String totalScore = rs.getString("totalScore");
            final String stationcd1 = rs.getString("stationcd1");
            final String stationcd2 = rs.getString("stationcd2");
            final String stationcd3 = rs.getString("stationcd3");
            final String stationcd4 = rs.getString("stationcd4");

            final PreSchoolInfoDat classFormationDat = new PreSchoolInfoDat(
                    examno,
                    hrClass,
                    attendno,
                    preHrClass,
                    preAttendno,
                    attendflg1,
                    score1,
                    score2,
                    totalScore,
                    stationcd1,
                    stationcd2,
                    stationcd3,
                    stationcd4
            );

            classFormationDat.load(db2);
            rtn.add(classFormationDat);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>PRE_SCHOOL_INFO_DAT に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlPreSchoolInfoDats() {
        StringBuffer stb = new StringBuffer();
        

        stb.append(" select");
        stb.append("    EXAMNO as examno,");
        stb.append("    HR_CLASS as hrClass,");
        stb.append("    ATTENDNO as attendno,");
        stb.append("    PRE_HR_CLASS as preHrClass,");
        stb.append("    PRE_ATTENDNO as preAttendno,");
        stb.append("    ATTENDFLG1 as attendflg1,");
        stb.append("    SCORE1 as score1,");
        stb.append("    SCORE2 as score2,");
        stb.append("    TOTAL_SCORE as totalScore,");
        stb.append("    STATIONCD1 as stationcd1,");
        stb.append("    STATIONCD2 as stationcd2,");
        stb.append("    STATIONCD3 as stationcd3,");
        stb.append("    STATIONCD4 as stationcd4");
        stb.append(" from");
        stb.append("    PRE_SCHOOL_INFO_DAT");
        stb.append(" where");
        stb.append("    YEAR = '" + _param._year + "'");
        stb.append("    and SEMESTER = '" + _param._semester + "'");
        stb.append("    and GRADE = '" + _param._grade + "'");

        if (_param._title.equals(TITLE_1_CD)) {
            stb.append("    order by PRE_HR_CLASS, PRE_ATTENDNO");
        } else {
            stb.append("    and HR_CLASS is not null");
            stb.append("    order by HR_CLASS, ATTENDNO");
        }

        return stb.toString();
     }

    // ======================================================================
    /**
     * 志願者基礎データ。
     */
    private class EntexamApplicantbaseDat {
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _fsCd;
        private final String _prischoolcd;
        private final String _remark1;

        EntexamApplicantbaseDat() {
            _name = "";
            _nameKana = "";
            _sex = "";
            _fsCd = "";
            _prischoolcd = "";
            _remark1 = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String nameKana,
                final String sex,
                final String fsCd,
                final String prischoolcd,
                final String remark1
        ) {
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _fsCd = fsCd;
            _prischoolcd = prischoolcd;
            _remark1 = remark1;
        }
    }

    private EntexamApplicantbaseDat createEntexamApplicantbaseDat(final DB2UDB db2, String examno)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDat(examno));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String name = rs.getString("name");
            final String nameKana = rs.getString("nameKana");
            final String sex = rs.getString("sex");
            final String fsCd = rs.getString("fsCd");
            final String prischoolcd = rs.getString("prischoolcd");
            final String remark1 = rs.getString("remark1");

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    name,
                    nameKana,
                    sex,
                    fsCd,
                    prischoolcd,
                    remark1
            );

            return entexamApplicantbaseDat;
        }

        return new EntexamApplicantbaseDat();
    }

    private String sqlEntexamApplicantbaseDat(String examno) {
        return " select"
                + "    NAME as name,"
                + "    NAME_KANA as nameKana,"
                + "    SEX as sex,"
                + "    FS_CD as fsCd,"
                + "    PRISCHOOLCD as prischoolcd,"
                + "    REMARK1 as remark1"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + examno + "'"
                ;
    }

    private double getRoundingOffNum(double score) {
        BigDecimal bd = new BigDecimal(String.valueOf(score));
        double scale = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return scale;
    }

} // KNJL382J

// eof
