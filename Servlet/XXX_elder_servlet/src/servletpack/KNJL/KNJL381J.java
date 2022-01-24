// kanji=漢字
/*
 * $Id: b3a904374636b52ff68e3ec85c453476c634bb37 $
 *
 * 作成日: 2007/12/27 16:49:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
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
 *  プレスクール出欠票
 * @author nakada
 * @version $Id: b3a904374636b52ff68e3ec85c453476c634bb37 $
 */
public class KNJL381J {
    /* pkg */static final Log log = LogFactory.getLog(KNJL381J.class);

    private static final String FORM_FILE = "KNJL381J.frm";

    /*
     * タイトル
     */
    /** 仮クラス */
    private static final String TITLE_1_CD = "1";
    private static final String TITLE_1 = "仮クラス";
    /** クラス */
    private static final String TITLE_2_CD = "2";
    private static final String TITLE_2 = "クラス";

    // 名称マスタキー（NAMECD1）
    private static final String NAME_MST_SEX = "Z002";

    /*
     * 性別
     */
    /** 性別：男 */
    private static final String SEX_MAN = "1";

    /*
     * 出欠
     */
    /** 出席 */
    private static final String ATTEND_YES = "1";

    /*
     * 駅利用
     */
    /** 利用 */
    private static final String STATION_USE = "1";

    /*
     * 情報有無
     */
    /** 利用 */
    private static final String INF_ON = "1";

    /*
     * 提出書類印字有無
     */
    /** 提出書類印字 */
    private static final String PRINT1_ON = "1";

    /*
     * ＴＥＬ印字有無
     */
    /** ＴＥＬ印字 */
    private static final String PRINT2_ON = "1";

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

        if (!preSchoolInfoDats.isEmpty()) {
            final PreSchoolInfoDat preSchoolInfoDat = (PreSchoolInfoDat) preSchoolInfoDats.get(0);

            if (_param._title.equals(TITLE_1_CD)) {
                oldHrClass = preSchoolInfoDat._preHrClass;
            } else {
                oldHrClass = preSchoolInfoDat._hrClass;
            }
        }

        int i = 0; // １ページあたり件数
        int no = 0; // №

        int manNum = 0;
        int womanNum = 0;

        boolean getCnt = false;
        PreSchoolInfoDat preSchoolInfoDat = null;

        for (Iterator it = preSchoolInfoDats.iterator(); it.hasNext();) {
            preSchoolInfoDat = (PreSchoolInfoDat) it.next();

            if ((_param._title.equals(TITLE_1_CD) && (!preSchoolInfoDat._preHrClass.equals(oldHrClass))) ||
                    (_param._title.equals(TITLE_2_CD) && (!preSchoolInfoDat._hrClass.equals(oldHrClass)))) {

                _form._svf.VrEndPage();
                _hasData = true;

                if (_param._title.equals(TITLE_1_CD)) {
                    oldHrClass = preSchoolInfoDat._preHrClass;
                } else {
                    oldHrClass = preSchoolInfoDat._hrClass;
                }

                getCnt = false;
                no = 0;
                i = 0;
            }

            if (!getCnt) {
                // 同一年組内、件数算出
                _dataCnt = getDataCnt(preSchoolInfoDats, oldHrClass);
                // 総ページ数算出
                _totalPage = _dataCnt / DETAILS_MAX;

                if ((_dataCnt % DETAILS_MAX) != 0) {
                    _totalPage++;
                }

                _page = 0;
                printHeader(preSchoolInfoDat);
                getCnt = true;
            }

            i++;
            no++;

            printApplicant(preSchoolInfoDat, no, i);

            if (preSchoolInfoDat._entexamApplicantbaseDat._sex.equals(SEX_MAN)) {
                manNum++;
            } else {
                womanNum++;
            }

            if (_dataCnt == no) {
                printFooter(manNum, womanNum);
                manNum = 0;
                womanNum = 0;
                no = 0;
            }

            if (i >= DETAILS_MAX) {
                _form._svf.VrEndPage();
                printHeader(preSchoolInfoDat);
                i = 0;
            }
        }

        if (i > 0) {
            _form._svf.VrEndPage();
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

    private void printApplicant(PreSchoolInfoDat preSchoolInfoDat, final int no, final int i) {
        if (_param._title.equals(TITLE_1_CD)) {
            if (nvlT(preSchoolInfoDat._preAttendno).length() != 0) {
                int int1 = Integer.parseInt(preSchoolInfoDat._preAttendno);
                /* NO */
                _form._svf.VrsOutn("NO", i, Integer.toString(int1));
            }
        } else {
            if (nvlT(preSchoolInfoDat._attendno).length() != 0) {
                /* NO */
                int int1 = Integer.parseInt(preSchoolInfoDat._attendno);
                _form._svf.VrsOutn("NO", i, Integer.toString(int1));
            }
        }
        
        /* 備考 */
        _form._svf.VrsOutn("REMARK1_1", i, nvlT(preSchoolInfoDat._remark));

        /* 受験番号 */
        _form._svf.VrsOutn("EXAMNO", i, preSchoolInfoDat._examno);
        /* カナ名前 */
        _form._svf.VrsOutn("NAME_KANA", i, preSchoolInfoDat._entexamApplicantbaseDat._nameKana);
        /* 名前 */
        _form._svf.VrsOutn("NAME", i, preSchoolInfoDat._entexamApplicantbaseDat._name);
        /* 性別 */
        _form._svf.VrsOutn("SEX", i, _param._sexString(preSchoolInfoDat._entexamApplicantbaseDat._sex));
        /* 前回クラス */
        _form._svf.VrsOutn("OLDCLASS", i, nvlT(_param._schregRegdHdatMapString(
                nvlT(_param._year + _param._semester + _param._grade + preSchoolInfoDat._preHrClass))));
        /* 第1回出欠 */
        if (nvlT(preSchoolInfoDat._attendflg1).equals(ATTEND_YES)) {
            _form._svf.VrsOutn("ATTEND1", i, SIGN1);
        }
        /* 第2回出欠 */
        if (nvlT(preSchoolInfoDat._attendflg2).equals(ATTEND_YES)) {
            _form._svf.VrsOutn("ATTEND2", i, SIGN1);
        }
        /* 平バス１ */
        if (nvlT(preSchoolInfoDat._stationcd1).equals(STATION_USE)) {
            _form._svf.VrsOutn("BUS1", i, SIGN1);
        }
        /* 平バス２ */
        if (nvlT(preSchoolInfoDat._stationcd2).equals(STATION_USE)) {
            _form._svf.VrsOutn("BUS2", i, SIGN1);
        }
        /* 平バス３ */
        if (nvlT(preSchoolInfoDat._stationcd3).equals(STATION_USE)) {
            _form._svf.VrsOutn("BUS3", i, SIGN1);
        }
        /* 平バス４ */
        if (nvlT(preSchoolInfoDat._stationcd4).equals(STATION_USE)) {
            _form._svf.VrsOutn("BUS4", i, SIGN1);
        }

        if (nvlT(_param._flg1).equals(PRINT1_ON)) {
            if (nvlT(preSchoolInfoDat._preInfo1).equals(INF_ON)) {
                /* 預金口座依頼書 */
                _form._svf.VrsOutn("REQUEST", i, SIGN1);
            }

            if (nvlT(preSchoolInfoDat._preInfo2).equals(INF_ON)) {
                /* 契約書 */
                _form._svf.VrsOutn("OATH", i, SIGN1);
            }

            if (nvlT(preSchoolInfoDat._preInfo3).equals(INF_ON)) {
                /* 生徒情報カード */
                _form._svf.VrsOutn("CARD", i, SIGN1);
            }
        }

        if (nvlT(_param._flg2).equals(PRINT2_ON)) {
            /* 電話番号 */
            _form._svf.VrsOutn("TELNO", i, preSchoolInfoDat._entexamApplicantaddrDat._telno);
        }
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
        private final String _flg1;
        private final String _flg2;

        private Map _sexMap;
        private Map _schregPreSchoolHdatMap;

        public Param(
                final String year,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String title,
                final String grade,
                final String flg1,
                final String flg2
        ) {
            _year = year;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _title = title;
            _grade = grade;
            _flg1 = flg1;
            _flg2 = flg2;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregPreSchoolHdatMap = createPreSchoolHdat(db2);
            _sexMap = getNameMst(NAME_MST_SEX);

            return;
        }

        public String _sexString(String sex) {
            return (String) _sexMap.get(sex);
        }

        public String _schregRegdHdatMapString(String code) {
            return nvlT((String)_schregPreSchoolHdatMap.get(code));
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
        final String title = request.getParameter("TITLE");
        final String grade = request.getParameter("GRADE");
        final String flg1 = request.getParameter("FLG1");
        final String flg2 = request.getParameter("FLG2");

        final Param param = new Param
        (
                year,
                semester,
                programId,
                dbName,
                loginDate,
                title,
                grade,
                flg1,
                flg2
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
        private final String _attendflg2;
        private final String _stationcd1;
        private final String _stationcd2;
        private final String _stationcd3;
        private final String _stationcd4;
        private final String _remark;
        private final String _preInfo1;
        private final String _preInfo2;
        private final String _preInfo3;

        private EntexamApplicantbaseDat _entexamApplicantbaseDat;   // 志願者基礎データ
        private EntexamApplicantaddrDat _entexamApplicantaddrDat;   // 志願者住所データ

        PreSchoolInfoDat(
                final String examno,
                final String hrClass,
                final String attendno,
                final String preHrClass,
                final String preAttendno,
                final String attendflg1,
                final String attendflg2,
                final String stationcd1,
                final String stationcd2,
                final String stationcd3,
                final String stationcd4,
                final String remark,
                final String preInfo1,
                final String preInfo2,
                final String preInfo3
        ) {
            _examno = examno;
            _hrClass = hrClass;
            _attendno = attendno;
            _preHrClass = preHrClass;
            _preAttendno = preAttendno;
            _attendflg1 = attendflg1;
            _attendflg2 = attendflg2;
            _stationcd1 = stationcd1;
            _stationcd2 = stationcd2;
            _stationcd3 = stationcd3;
            _stationcd4 = stationcd4;
            _remark = remark;
            _preInfo1 = preInfo1;
            _preInfo2 = preInfo2;
            _preInfo3 = preInfo3;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _entexamApplicantbaseDat = createEntexamApplicantbaseDat(db2, _examno);
            _entexamApplicantaddrDat = createEntexamApplicantaddrDat(db2, _examno);
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
            final String attendflg2 = rs.getString("attendflg2");
            final String stationcd1 = rs.getString("stationcd1");
            final String stationcd2 = rs.getString("stationcd2");
            final String stationcd3 = rs.getString("stationcd3");
            final String stationcd4 = rs.getString("stationcd4");
            final String remark = rs.getString("remark");
            final String preInfo1 = rs.getString("preInfo1");
            final String preInfo2 = rs.getString("preInfo2");
            final String preInfo3 = rs.getString("preInfo3");

            final PreSchoolInfoDat classFormationDat = new PreSchoolInfoDat(
                    examno,
                    hrClass,
                    attendno,
                    preHrClass,
                    preAttendno,
                    attendflg1,
                    attendflg2,
                    stationcd1,
                    stationcd2,
                    stationcd3,
                    stationcd4,
                    remark,
                    preInfo1,
                    preInfo2,
                    preInfo3
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
        stb.append("    ATTENDFLG2 as attendflg2,");
        stb.append("    STATIONCD1 as stationcd1,");
        stb.append("    STATIONCD2 as stationcd2,");
        stb.append("    STATIONCD3 as stationcd3,");
        stb.append("    STATIONCD4 as stationcd4,");
        stb.append("    REMARK as remark,");
        stb.append("    PRE_INFO1 as preInfo1,");
        stb.append("    PRE_INFO2 as preInfo2,");
        stb.append("    PRE_INFO3 as preInfo3");
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

        EntexamApplicantbaseDat() {
            _name = "";
            _nameKana = "";
            _sex = "";
        }

        EntexamApplicantbaseDat(
                final String name,
                final String nameKana,
                final String sex
        ) {
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
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

            final EntexamApplicantbaseDat entexamApplicantbaseDat = new EntexamApplicantbaseDat(
                    name,
                    nameKana,
                    sex
            );

            return entexamApplicantbaseDat;
        }

        return new EntexamApplicantbaseDat();
    }

    private String sqlEntexamApplicantbaseDat(String examno) {
        return " select"
                + "    NAME as name,"
                + "    NAME_KANA as nameKana,"
                + "    SEX as sex"
                + " from"
                + "    ENTEXAM_APPLICANTBASE_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + examno + "'"
                ;
    }

    // ======================================================================
    /**
     * 志願者住所データ
     */
    private class EntexamApplicantaddrDat {
        private final String _telno;

        EntexamApplicantaddrDat() {
            _telno = "";
        }

        EntexamApplicantaddrDat(
                final String telno
        ) {
            _telno = telno;
        }
    }

    private EntexamApplicantaddrDat createEntexamApplicantaddrDat(final DB2UDB db2, String examno)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantaddrDat(examno));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String telno = rs.getString("telno");

            final EntexamApplicantaddrDat entexamApplicantaddrDat = new EntexamApplicantaddrDat(
                    telno
            );

            return entexamApplicantaddrDat;
        }

        return new EntexamApplicantaddrDat();
    }

    private String sqlEntexamApplicantaddrDat(String examno) {
        return " select"
                + "    GTELNO as telno"
                + " from"
                + "    ENTEXAM_APPLICANTADDR_DAT"
                + " where"
                + "    ENTEXAMYEAR = '" + _param._year + "' and"
                + "    EXAMNO = '" + examno + "'"
                ;
    }
} // KNJL381J

// eof
