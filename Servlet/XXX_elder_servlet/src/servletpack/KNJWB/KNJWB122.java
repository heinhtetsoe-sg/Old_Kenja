// kanji=漢字
/*
 * $Id: 06cba832491b652c50f87444acc128b96b494c3a $
 *
 * 作成日: 2007/12/20 13:51:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
 *  教科書発注書発行
 * @author nakada
 * @version $Id: 06cba832491b652c50f87444acc128b96b494c3a $
 */
public class KNJWB122 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWB122.class);

    private static final String FORM_FILE = "KNJWB122.frm";

//    /*
//     * 文字数による出力項目切り分け基準
//     */
//    /** 科目名称 */
//    private static final int SUBCLASS_LENG = 20;
//    /** 発行社名称 */
//    private static final int ISSUECOMPANY_LENG = 20;
//    /** 教科書名称 */
//    private static final int TEXTBOOKNAME_LENG = 30;

    /*
     * 教科書区分
     */
    /** 教科書： */
    private static final String TEXTBOOKDIV = "1";
    /** 副教材： */
    private static final String EDUCATION_MATERIALS = "3";
    /** 金額表示有無： */
    private static final String MONEY_PRINT_ON = "1";
    private static final String MONEY_PRINT_OFF = "2";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 20;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;

    private int _page = 0;

    /** 印字明細印字位置 */
    private int _detailCnt = 0;
    /** 同一ブレークキー内処理データ件数目 */
    private int _inKeyDatCnt = 0;
    /** 処理データ件数目 */
    private int _totalCnt;

    private String _newOrderCd;
    private String _oldOrderCd;
    private String _newSchregno;
    private String _oldSchregno;

    private int _unitprice1 = 0;
    private int _num1 = 0;
    private int _unitprice2 = 0;
    private int _num2 = 0;

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

            log.debug(">>発注連番（ＳＥＱ）=" + _param._seq);
            log.debug(">>発注日 =" + _param._date);

            // ＳＥＱのみで抽出＆ソート
            List textOrders = createTextOrderHistDats(db2);

            // 該当学生のみ抽出
            textOrders = tergetTextOrderDats(textOrders);

            printMain(db2, textOrders);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private List tergetTextOrderDats(final List textOrders) {
        final List rtn = new ArrayList();

        if (textOrders != null) {
            _oldSchregno = "";

            Collections.sort(textOrders);
            for (Iterator it = textOrders.iterator(); it.hasNext();) {
                final TextOrderHistDat textOrderHistDat = (TextOrderHistDat) it.next();

                if (!textOrderHistDat._schregno.equals(_oldSchregno)) {
                    for (int i = 0; i < _param._schregno.length; i++) {
                        if (textOrderHistDat._schregno.equals(_param._schregno[i])) {
                            rtn.add(textOrderHistDat);
                            _oldSchregno = textOrderHistDat._schregno;

                            break;
                        }
                    }
                } else {
                    rtn.add(textOrderHistDat);
                }
            }
        }

        return rtn;
    }

    private void printMain(final DB2UDB db2, final List textOrders) 
        throws SQLException {

        int textOrdersSize = 0;

        if (!textOrders.isEmpty()) {
            // 印字対象件数取得
            textOrdersSize = textOrders.size();
        }

        // 明細データ取得用
        TextOrderHistDat textOrderHistDat = initSet(textOrders);
        // 明細データ退避用
        TextOrderHistDat saveTextOrder = textOrderHistDat;

        while (!textOrders.isEmpty() &&
                _totalCnt < textOrdersSize) {

            _form._svf.VrAttribute( "RECORD1", "Print=1");
            _form._svf.VrAttribute( "ORDER_CD", "FF=1");
            _form._svf.VrAttribute( "SCHREGNO1", "FF=1");
            _hasData = true;
            _oldOrderCd = _newOrderCd;

            while (_totalCnt < textOrdersSize && 
                    _newOrderCd.equals(_oldOrderCd)
            ) {
                // 同一ブレークキー内明細件数取得
                int tergetCnt = getTotalCnt(textOrders, _newOrderCd, _newSchregno);

                _oldSchregno = _newSchregno;

                SchregBaseMst schregBaseMst = createStudent(db2, textOrderHistDat._schregno);
                while (_totalCnt < textOrdersSize &&
                        _newOrderCd.equals(_oldOrderCd) &&
                        _newSchregno.equals(_oldSchregno)
                ) {

                    printDetail(textOrderHistDat, schregBaseMst, saveTextOrder, tergetCnt);
                    _totalCnt++;
                    if (_totalCnt < textOrdersSize) {
                        textOrderHistDat = (TextOrderHistDat) textOrders.get(_totalCnt);

                        _newOrderCd = textOrderHistDat._orderCd;
                        _newSchregno = textOrderHistDat._schregno;

                    }
                }

                _detailCnt = 0;
                _page = 0;
            }
        }
    }

    private void printDetail(TextOrderHistDat textOrderHistDat, SchregBaseMst schregBaseMst, TextOrderHistDat saveTextOrder, int tergetCnt) {
        if (_detailCnt == 0 ) {
            printPage(saveTextOrder, schregBaseMst, tergetCnt);
        }

        _detailCnt++;
        _inKeyDatCnt++;

        priceSum(textOrderHistDat);

        // 同一発注先，学生の最後の印刷ページのとき
        // 合計を印刷する。
        if (_inKeyDatCnt == tergetCnt ) {
            printTotal(_unitprice1, _num1, _unitprice2, _num2);

            _inKeyDatCnt = 0;   
            _unitprice1 = 0;
            _num1 = 0;
            _unitprice2 = 0;
            _num2 = 0;        
           
        }

        printTextOrder(_detailCnt, textOrderHistDat);

        saveTextOrder = textOrderHistDat;

        if (_detailCnt == DETAILS_MAX ) {
            _detailCnt = 0;
        }
    }
    
    private TextOrderHistDat initSet(final List textOrders) {
        TextOrderHistDat textOrderHistDat = null;

        if (!textOrders.isEmpty()) {
            textOrderHistDat = (TextOrderHistDat) textOrders.get(0);

            _newOrderCd = textOrderHistDat._orderCd;
            _oldOrderCd = textOrderHistDat._orderCd;
            _newSchregno = textOrderHistDat._schregno;
            _oldSchregno = textOrderHistDat._schregno;
        }

        return textOrderHistDat;
    }

    private void priceSum(TextOrderHistDat textOrderHistDat) {
        if (textOrderHistDat._textbookdiv.equals(TEXTBOOKDIV)) {
            _unitprice1 += Integer.parseInt(textOrderHistDat._textbookunitprice);
            _num1 += Integer.parseInt(textOrderHistDat._textbookamount);
        } else {
            _unitprice2 += Integer.parseInt(textOrderHistDat._textbookunitprice);
            _num2 += Integer.parseInt(textOrderHistDat._textbookamount);
        }
    }

    private void printPage(TextOrderHistDat saveTextOrder, SchregBaseMst schregBaseMst, int tergetCnt) {
        printHeader();
        printFooter(saveTextOrder, schregBaseMst);
    }

    private int getTotalCnt(List textOrders, String orderCd, String schregno) {
        int cnt = 0;

        for (Iterator it = textOrders.iterator(); it.hasNext();) {
            final TextOrderHistDat textOrder = (TextOrderHistDat) it.next();

            if (orderCd.equals(textOrder._orderCd) &&
                    schregno.equals(textOrder._schregno)) {
                cnt++;
            }
        }

        return cnt;
    }

    private void printHeader() {
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._date));  // TODO: 暫定
        /* ページ */
        _form._svf.VrsOut("PAGE", String.valueOf(++_page));
        /* 年度 */
        _form._svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._exeYear + "/" + "1/1") + "度");
        /* 学校名 */
        _form._svf.VrsOut("TITLE", _param._staffSchoolMst._schoolName1);
    }

    private void printFooter(TextOrderHistDat textOrder, SchregBaseMst schregBaseMst) {
        /* 所属学習センター */
        _form._svf.VrsOut("SCHOOLNAME", schregBaseMst._belongingDat._schoolName1);
        /* 受付ＮＯ */
        _form._svf.VrsOut("ACCEPT_NO", "");   // TODO: 空白
        /* 担任名 */
        _form._svf.VrsOut("STAFF_NAME", schregBaseMst._schregRegdHdat._staff._name);
        /* 学籍番号 */
        _form._svf.VrsOut("SCHREGNO2", schregBaseMst._schregNo);
        /* 生徒氏名 */
        _form._svf.VrsOut("NAME", schregBaseMst._name);
        /* 郵便番号 */
        _form._svf.VrsOut("ZIPCD", schregBaseMst._textSchAddrHistDat._zipcd);
        /* 電話番号 */
        _form._svf.VrsOut("TELNO", schregBaseMst._textSchAddrHistDat._telno);
        /* 住所 */
        _form.printAddr(schregBaseMst);
    }

    private void printTotal(int price1, int num1, int price2, int num2) {
        if (_param._moneyPrint.equals(MONEY_PRINT_ON)) {
            /* 教科書小計・単価 */
            _form._svf.VrsOutn("TOTAL_PRICE", 1, Integer.toString(price1));
            /* 教材小計・単価 */
            _form._svf.VrsOutn("TOTAL_PRICE", 2, Integer.toString(price2));
            /* 合計・単価 */
            _form._svf.VrsOutn("TOTAL_PRICE", 3, Integer.toString(price1 + price2));
        }

        /* 教科書小計・数量 */
        _form._svf.VrsOutn("TOTAL_AMOUNT", 1, Integer.toString(num1));
        /* 教材小計・数量 */
        _form._svf.VrsOutn("TOTAL_AMOUNT", 2, Integer.toString(num2));
        /* 合計・数量 */
        _form._svf.VrsOutn("TOTAL_AMOUNT", 3, Integer.toString(num1 + num2));
    }

    private void printTextOrder(int i, TextOrderHistDat textOrder) {
        _form._svf.VrAttribute( "ORDER_CD", "FF=1");
        _form._svf.VrAttribute( "SCHREGNO1", "FF=1");

        /* 改ページ制御用発注先コード */
        _form._svf.VrsOut("ORDER_CD", textOrder._orderCd);
        /* 改ページ制御用学籍番号 */
        _form._svf.VrsOut("SCHREGNO1", textOrder._schregno);

        /* 科目 */
        _form._svf.VrsOut("SUBCLASSNAME", textOrder._subclassMst._subclassname);
        /* 発行社 */
        _form._svf.VrsOut("ISSUECOMPANY_NAME", textOrder._textIssuecompanyMst._issuecompanyName);
        /* 教科書番号 */
        _form._svf.VrsOut("TEXTBOOKCD", textOrder._textbookcd);
        /* 教科書名 */
        _form._svf.VrsOut("TEXTBOOKNAME", textOrder._textbookname);

        if (_param._moneyPrint.equals(MONEY_PRINT_ON)) {
            /* 単価 */
            _form._svf.VrsOut("TEXTBOOKPRICE", textOrder._textbookunitprice);
        }

        /* 数量 */
        _form._svf.VrsOut("AMOUNT", textOrder._textbookamount);

        _form._svf.VrEndRecord();
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
        private final String _exeYear;
        private final String _semester;
        private final String _prgrId;
        private final String _dbName;
        private final String _loginDate;
        private final String _seq;
        private final String _date;
        private final String[] _schregno;
        private final String _moneyPrint;

        private Map _prefMap;           // 都道府県
        private School _staffSchoolMst;             // 学校名

        public Param(
                final String year,
                final String exeYear,
                final String semester,
                final String prgId,
                final String dbName,
                final String loginDate,
                final String seq,
                final String date,
                final String[] schregno,
                final String moneyPrint
        ) {
            _year = year;
            _exeYear = exeYear;
            _semester = semester;
            _prgrId = prgId;
            _dbName = dbName;
            _loginDate = loginDate;
            _seq = seq;
            _date = date;
            _schregno = schregno;
            _moneyPrint = moneyPrint;
        }

        public void load(DB2UDB db2) throws SQLException {
            _staffSchoolMst = createSchool(db2, _param._year);
            _prefMap = getPrefMst();

            return;
        }

        public String _prefMapString(String pref) {
            return (String) _prefMap.get(pref) != null ? (String) _prefMap.get(pref) : "";
        }

        private Map getPrefMst() throws SQLException {
            final String sql = sqlPrefMst();
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

        private String sqlPrefMst() {
            return " select"
            + "    PREF_CD as code,"
            + "    PREF_NAME as name"
            + " from"
            + "    PREF_MST"
            + " order by PREF_CD";
        }

        private School createSchool(DB2UDB db2, String year) throws SQLException {
            final String sql = sqlSchool(year);

            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolName1 = rs.getString("schoolName1");

                final School school = new School(schoolName1);
                return school;
            }

            return new School();
        }

        private String sqlSchool(String year) {
            return " select"
                    + "    SCHOOLNAME1 as schoolName1"
                    + " from"
                    + "    SCHOOL_MST"
                    + " where"
                    + "    YEAR = '" + year + "'";
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String exeYear = request.getParameter("EXE_YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String seq = request.getParameter("ORDER_SEQ");
        final String date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
        final String[] schregno = request.getParameterValues("CATEGORY_SELECTED");
        final String moneyPrint = request.getParameter("MONEY_PRINT");

        final Param param = new Param
        (
                year,
                exeYear,
                semester,
                programId,
                dbName,
                loginDate,
                seq,
                date,
                schregno,
                moneyPrint
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

            _svf.VrSetForm(FORM_FILE, 4);
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

        public void printAddr(SchregBaseMst schregBaseMst) {
           	String name = nvlT(_param._prefMapString(schregBaseMst._textSchAddrHistDat._prefCd)) 
                + nvlT(schregBaseMst._textSchAddrHistDat._addr1)
                + nvlT(schregBaseMst._textSchAddrHistDat._addr2)
                + nvlT(schregBaseMst._textSchAddrHistDat._addr3);
            _form._svf.VrsOut("ADDR", name);
        }
    }

    // ======================================================================
    /**
     * 発注履歴データ。
     */
    private class TextOrderHistDat implements Comparable {
        /*
         * 発注履歴データ
         */
        private final String _orderDate;    // 発注日

        /*
         * 学籍教科書購入データ
         */
        private final String _schregno;
        private final String _classcd;      // 教科コード
        private final String _curriculumCd; // 科目コード
        private final String _subclasscd;   // 教育課程コード
        private final String _textbookcd;   // 教科書コード

        /*
         * 教科書マスタ
         */
        private final String _textbookdiv;
        private final String _textbookname;
        private final String _textbookunitprice;
        private final String _issuecompanycd;
        private final String _orderCd;
        private final String _textbookamount;

        private SubclassMst _subclassMst;                   // 科目マスタ
        private TextIssuecompanyMst _textIssuecompanyMst;   // 発行社マスタ。

        TextOrderHistDat() {
            _orderDate = "";
            _schregno = "";
            _classcd = "";
            _curriculumCd = "";
            _subclasscd = "";
            _textbookcd = "";
            _textbookdiv = "";
            _textbookname = "";
            _textbookunitprice = "";
            _issuecompanycd = "";
            _orderCd = "";
            _textbookamount = "";
        }

        TextOrderHistDat(
                final String orderDate,
                final String schregno,
                final String classcd,
                final String curriculumCd,
                final String subclasscd,
                final String textbookcd,
                final String textbookdiv,
                final String textbookname,
                final String textbookunitprice,
                final String issuecompanycd,
                final String orderCd,
                final String textbookamount
        ) {
            _orderDate = orderDate;
            _schregno = schregno;
            _classcd = classcd;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _textbookcd = textbookcd;
            _textbookdiv = textbookdiv;
            _textbookname = textbookname;
            _textbookunitprice = textbookunitprice;
            _issuecompanycd = issuecompanycd;
            _orderCd = orderCd;
            _textbookamount = textbookamount;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _subclassMst = createSubclassMst(db2, _classcd, _curriculumCd, _subclasscd);
            _textIssuecompanyMst = createTextIssuecompanyMst(db2, _issuecompanycd);
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof TextOrderHistDat)) {
                return -1;
            }
            final TextOrderHistDat that = (TextOrderHistDat) o;

            // 発注先コード
            if (!this._orderCd.equals(that._orderCd)) {
                return this._orderCd.compareTo(that._orderCd);
            }
            // 学籍番号
            if (!this._schregno.equals(that._schregno)) {
                return this._schregno.compareTo(that._schregno);
            }
            // 教科コード
            if (!this._classcd.equals(that._classcd)) {
                return this._classcd.compareTo(that._classcd);
            }
            // 科目コード
            if (!this._subclasscd.equals(that._subclasscd)) {
                return this._subclasscd.compareTo(that._subclasscd);
            }
            // 課程コード
            if (!this._curriculumCd.equals(that._curriculumCd)) {
                return this._curriculumCd.compareTo(that._curriculumCd);
            }

            // 教科書コード
            return this._textbookcd.compareTo(that._textbookcd);
        }
    }

    private List createTextOrderHistDats(final DB2UDB db2)
        throws SQLException, Exception {

        final List rtn = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlEntexamApplicantbaseDats());
        rs = ps.executeQuery();

        String checkGroup = "";
        while (rs.next()) {
            final String orderDate = rs.getString("orderDate");
            final String schregno = rs.getString("schregno");
            final String classcd = rs.getString("classcd");
            final String curriculumCd = rs.getString("curriculumCd");
            final String subclasscd = rs.getString("subclasscd");
            final String textbookcd = rs.getString("textbookcd");
            final String textbookdiv = rs.getString("textbookdiv");
            final String textbookname = rs.getString("textbookname");
            final String textbookunitprice = rs.getString("textbookunitprice");
            final String issuecompanycd = rs.getString("issuecompanycd");
            final String orderCd = rs.getString("orderCd");
            final String textbookamount = rs.getString("textbookamount");
            final String textGroupCd = rs.getString("text_group_cd");

            final TextOrderHistDat textOrderHistDats = new TextOrderHistDat(
                    orderDate,
                    schregno,
                    classcd,
                    curriculumCd,
                    subclasscd,
                    textbookcd,
                    textbookdiv,
                    textbookname,
                    textbookunitprice,
                    issuecompanycd,
                    orderCd,
                    textbookamount
            );

            if (!textGroupCd.equals(checkGroup)) {
                textOrderHistDats.load(db2);
                rtn.add(textOrderHistDats);
            }
            checkGroup = textGroupCd;
        }

        if (rtn.isEmpty()) {
            log.debug(">>>TEXT_ORDER_HIST_DAT に該当するものがありません。");
            throw new Exception();
        } else {
            return rtn;
        }
    }

    private String sqlEntexamApplicantbaseDats() {
        return " select"
                + "    T1.ORDER_DATE as orderDate,"
                + "    T2.SCHREGNO as schregno,"
                + "    T2.CLASSCD as classcd,"
                + "    T2.CURRICULUM_CD as curriculumCd,"
                + "    T2.SUBCLASSCD as subclasscd,"
                + "    T2.TEXTBOOKCD as textbookcd,"
                + "    T3.TEXT_GROUP_CD as text_group_cd,"
                + "    T3.TEXTBOOKDIV as textbookdiv,"
                + "    T3.TEXTBOOKNAME as textbookname,"
                + "    T3.TEXTBOOKUNITPRICE as textbookunitprice,"
                + "    T3.ISSUECOMPANYCD as issuecompanycd,"
                + "    T3.ORDER_CD as orderCd,"
                + "    T3.TEXTBOOKAMOUNT as textbookamount"
                + " from"
                + "    TEXT_ORDER_HIST_DAT T1"
                + "    left join SCHREG_TEXTBOOK_DAT T2 on ("
                + "    T2.ORDER_SEQ = T1.ORDER_SEQ)"
                + "    left join TEXTBOOK_MST T3 on ("
                + "    T3.TEXTBOOKCD = T2.TEXTBOOKCD)"
                + " where"
                + "    T1.ORDER_SEQ = " + _param._seq
                + " order by T3.TEXT_GROUP_CD, T2.TEXTBOOKCD DESC, T2.CLASSCD, T2.SUBCLASSCD"
                ;
    }

    // ======================================================================
    /**
     * 科目マスタ。
     */
    private class SubclassMst {
        private final String _subclassname;     // 科目名称

        SubclassMst() {
            _subclassname = "";
        }

        SubclassMst(final String subclassname) {
            _subclassname = subclassname;
        }
    }

    private SubclassMst createSubclassMst(final DB2UDB db2, String pClasscd, String pCurriculumCd, String pSubclasscd)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSubclassMst(pClasscd, pCurriculumCd, pSubclasscd));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String subclassname = rs.getString("subclassname");

            final SubclassMst subclassMst = new SubclassMst(subclassname);

            return subclassMst;
        }

        return new SubclassMst();
    }

    private String sqlSubclassMst(String pClasscd, String pCurriculumCd, String pSubclasscd) {
        return " select"
                + "    SUBCLASSNAME as subclassname"
                + " from"
                + "    SUBCLASS_MST"
                + " where"
                + "    CLASSCD = '" + pClasscd + "' and"
                + "    CURRICULUM_CD = '" + pCurriculumCd + "' and"
                + "    SUBCLASSCD = '" + pSubclasscd + "'"
                ;
    }

    // ======================================================================
    /**
     * 発行社マスタ。
     */
    private class TextIssuecompanyMst {
        private final String _issuecompanyName;     // 発行社名称

        TextIssuecompanyMst() {
            _issuecompanyName = "";
        }

        TextIssuecompanyMst(final String issuecompanyName) {
            _issuecompanyName = issuecompanyName;
        }
    }

    private TextIssuecompanyMst createTextIssuecompanyMst(final DB2UDB db2, String pIssuecompanyCd)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlTextIssuecompanyMst(pIssuecompanyCd));
        rs = ps.executeQuery();

        while (rs.next()) {
            final String issuecompanyName = rs.getString("issuecompanyName");

            final TextIssuecompanyMst textIssuecompanyMst = new TextIssuecompanyMst(issuecompanyName);

            return textIssuecompanyMst;
        }

        return new TextIssuecompanyMst();
    }

    private String sqlTextIssuecompanyMst(String pIssuecompanyCd) {
        return " select"
                + "    ISSUECOMPANY_NAME as issuecompanyName"
                + " from"
                + "    TEXT_ISSUECOMPANY_MST"
                + " where"
                + "    ISSUECOMPANY_CD = '" + pIssuecompanyCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class SchregBaseMst {
        private final String _schregNo;             // 学籍番号
        private final String _name;                 // 氏名

        private SchregRegdHdat _schregRegdHdat;     // 学生在籍ヘッダデータ
        private SchregRegdDat _schregRegdDat;       // 学籍在籍データ
        private TextSchAddrHistDat _textSchAddrHistDat; // 学籍住所データ
        private Belonging _belongingDat;

        SchregBaseMst() {
            _schregNo = "";
            _name = "";
        }

        SchregBaseMst(final String schregNo,
                final String name
        ) {
            _schregNo = schregNo;
            _name = name;
        }

        public void load(DB2UDB db2) throws SQLException {
            _schregRegdDat = createSourseCodeDat(db2, _schregNo);
            _schregRegdHdat = createSchregRegdHdat(db2, _schregRegdDat._grade);
            _textSchAddrHistDat = createTextSchAddrHistDat(db2, _schregNo);
            _belongingDat = createBelongingDat(db2, _schregRegdDat._grade);
        }
    }

    private SchregBaseMst createStudent(final DB2UDB db2, String schregno)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");

                final SchregBaseMst schregBaseMst = new SchregBaseMst(
                        schregNo,
                        name
                );

                schregBaseMst.load(db2);
                return schregBaseMst;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        
        return new SchregBaseMst();
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _grade;
        
//        private SchregRegdHdat _schregRegdHdat;
//        private Belonging _belongingDat;

        SchregRegdDat() {
            _grade = "";
        }

        SchregRegdDat(
                final String grade
        ) {
            _grade = grade;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
//            _schregRegdHdat = createSchregRegdHdat(db2, _grade);
//            _belongingDat = createBelongingDat(db2, _grade);
        }
    }

    public SchregRegdDat createSourseCodeDat(DB2UDB db2, String SCHREGNO)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdDat(SCHREGNO));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String grade = rs.getString("grade");

            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    grade
            );
            return schregRegdDat;
        }

        return new SchregRegdDat();
    }

    private String sqlSchregRegdDat(String schregNo) {
        return " select"
                + "    GRADE as grade"
                + " from"
                + "    SCHREG_REGD_DAT"
                + " where"
                + "    SCHREGNO = '" + schregNo + "' and"
                + "    YEAR = '" + _param._year + "' and"
                + "    SEMESTER = '" + _param._semester + "'"
                ;
    }

    // ======================================================================
    /**
     * 学籍在籍ヘッダデータ。
     */
    private class SchregRegdHdat {
        private final String _trCd1;        // 担任コード1

        private Staff _staff;

        SchregRegdHdat() {
            _trCd1 = "";
        }

        SchregRegdHdat(final String trCd1) {
            _trCd1 = trCd1;
        }

        public void load(DB2UDB db2) throws SQLException {
            _staff = createStaff(db2, _trCd1);
        }
    }

    private SchregRegdHdat createSchregRegdHdat(final DB2UDB db2, String grade) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdHdat(grade));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String trCd1 = rs.getString("trCd1");

            final SchregRegdHdat schregRegdHdat = new SchregRegdHdat(trCd1);
            schregRegdHdat.load(db2);

            return schregRegdHdat;
        }

        return new SchregRegdHdat();
    }

    private String sqlSchregRegdHdat(String grade) {
        return " select"
                + "    TR_CD1 as trCd1"
                + " from"
                + "    SCHREG_REGD_HDAT"
                + " where" 
                + "    YEAR = '" + _param._year + "' and"
                + "    SEMESTER = '" + _param._semester + "' and"
                + "    GRADE = '" + grade + "'"
                ;
    }

    // ======================================================================
    /**
     * 生徒。教科書生徒別住所発行履歴データ
     */
    private class TextSchAddrHistDat {
        private final String _zipcd; // 郵便番号
        private final String _prefCd;
        private final String _addr1; // 住所１
        private final String _addr2; // 住所２
        private final String _addr3; // 住所３
        private final String _telno; // 電話番号

        TextSchAddrHistDat(
                final String zipcd,
                final String prefCd,
                final String addr1,
                final String addr2,
                final String addr3,
                final String telno
        ) {
            _zipcd = zipcd;
            _prefCd = prefCd;
            _addr1 = addr1;
            _addr2 = addr2;
            _addr3 = addr3;
            _telno = telno;
        }

        public TextSchAddrHistDat() {
            _zipcd = "";
            _prefCd = "";
            _addr1 = "";
            _addr2 = "";
            _addr3 = "";
            _telno = "";
        }
    }

    private TextSchAddrHistDat createTextSchAddrHistDat(DB2UDB db2, String schregno)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sqlTextSchAddrHistDat(schregno));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String zipcd = rs.getString("zipcd");
            final String prefCd = rs.getString("prefCd");
            final String addr1 = rs.getString("addr1");
            final String addr2 = rs.getString("addr2");
            final String addr3 = rs.getString("addr3");
            final String telno = rs.getString("telno");

            final TextSchAddrHistDat studentTextSchAddrHistDat = new TextSchAddrHistDat(
                    zipcd,
                    prefCd,
                    addr1,
                    addr2,
                    addr3,
                    telno
            );
            return studentTextSchAddrHistDat;
        }                    
        return new TextSchAddrHistDat();
    }

    private String sqlTextSchAddrHistDat(String schregno) {
        return " select"
                + "    ZIPCD as zipcd,"
                + "    PREF_CD as prefCd,"
                + "    ADDR1 as addr1,"
                + "    ADDR2 as addr2,"
                + "    ADDR3 as addr3,"
                + "    TELNO as telno"
                + " from"
                + "    TEXT_SCH_ADDR_HIST_DAT"
                + " where"
                + "    YEAR = '" + _param._exeYear + "' AND"
                + "    SCHREGNO = '" + schregno + "' AND"
                + "    ORDER_SEQ = " + _param._seq
                ;
    }

    // ======================================================================
    /**
     * 職員。
     */
    private class Staff {
        private final String _name; // 職員氏名

        Staff(final String name) {
            _name = name;
        }

        public Staff() {
            _name = "";
        }
    }

    private Staff createStaff(DB2UDB db2, String staffCd) throws SQLException {
        final String sql = sqlStaff(staffCd);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String staffname = rs.getString("staffName");

            final Staff staff = new Staff(staffname);
            return staff;
        }

        return new Staff();
    }

    private String sqlStaff(String staffCd) {
        return " select"
                + "    STAFFNAME as  staffName"
                + " from"
                + "    STAFF_MST"
                + " where"
                + "    STAFFCD = '" + staffCd + "'"
                ;
    }

    // ======================================================================
    /**
     * 学校マスタ。
     */
    private class School {
        private final String _schoolName1; // 学校名1

        School(final String schoolName1) {
            _schoolName1 = schoolName1;
        }

        public School() {
            _schoolName1 = "";
        }
    }

    private School createSchool(DB2UDB db2, String year) throws SQLException {
        final String sql = sqlSchool(year);

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            final String schoolName1 = rs.getString("schoolName1");

            final School school = new School(schoolName1);
            return school;
        }

        return new School();
    }

    private String sqlSchool(String year) {
        return " select"
                + "    SCHOOLNAME1 as schoolName1"
                + " from"
                + "    SCHOOL_MST"
                + " where"
                + "    YEAR = '" + year + "'";
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    private class Belonging {
        private final String _schoolName1;

        Belonging(
                final String schoolName1
        ) {
            _schoolName1 = schoolName1;
        }

        Belonging() {
            _schoolName1 = "";
        }
    }

    public Belonging createBelongingDat(DB2UDB db2, String belongingDiv)
        throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            ps = db2.prepareStatement(sqlBelongingDat(belongingDiv));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                
                final Belonging belonging = new Belonging(
                        name
                );
                return belonging;
            }
            return null;
    }

    private String sqlBelongingDat(String belongingDiv) {
        return " select"
                + "    SCHOOLNAME1 as name"
                + " from"
                + "    BELONGING_MST"
                + " where"
                + "    BELONGING_DIV = '" + belongingDiv + "'"
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
    }} // KNJWB122

// eof
