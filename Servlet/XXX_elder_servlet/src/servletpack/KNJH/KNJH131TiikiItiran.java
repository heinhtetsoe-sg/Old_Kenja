// kanji=漢字
/*
 * $Id: 973e1acf9bb73b92ccecbb889c2a3a359c7fad64 $
 *
 * 作成日: 2007/06/07
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 973e1acf9bb73b92ccecbb889c2a3a359c7fad64 $
 */
public class KNJH131TiikiItiran {

    private static final Log log = LogFactory.getLog(KNJH131TiikiItiran.class);

    private static final String FORM_FILE = "KNJH131.frm";

    private static final String SEX_MAN   = "1";
    private static final String SEX_GIRL  = "2";

    private static final String OTHERS_AREA_CD   = "9999";
    private static final String OTHERS_AREA_NAME = "その他";

    private static final String TOTAL_GRADE = "9";
    private static final int MAXLINE = 47;

    Param _param;
    private String _useSchool_KindField;
    private String _SCHOOLKIND;
    private String _maxGrade;
    private String _minGrade;

    /**
     * KNJH131.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        log.fatal("$Revision: 75815 $");
        KNJServletUtils.debugParam(request, log);

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                setGradeMaxMin(db2, _param);
            }

            boolean hasData = false; //該当データなしフラグ
            hasData = printMain(db2, svf);

            //  該当データ無し
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
        }

    }


    /** 中高一貫はTrue */
    private void setGradeMaxMin(
        final DB2UDB db2,
        final Param param
    ) {
        String sql = "SELECT MIN(GRADE) AS MIN, MAX(GRADE) AS MAX FROM SCHREG_REGD_GDAT WHERE YEAR = '"+param._year+"' AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                _maxGrade = rs.getString("MAX");
                _minGrade = rs.getString("MIN");
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 75815 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                final String name = (String) enumeration.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
        log.debug(" 対象者=" + param._title + " 日付=" + param._date);
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gengou;
        private final String _outputA;
        private final String[] _areaCd;
        private final String _title;
        private final String _date;
        private final boolean _hischoolFlg;
        private boolean _isHighScool;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");

            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_year));

            _gengou = gengou + "年度";

            _semester = request.getParameter("SEMESTER");

            _outputA = request.getParameter("OUTPUTA");

            _areaCd = request.getParameterValues("CATEGORY_SELECTED");

            _title = _outputA.equals("1") ? "保護者住所" : _outputA.equals("2") ? "負担者住所" : "生徒住所";

            // DBより取得
            _hischoolFlg = getJh(db2, _year, _semester);
            _isHighScool = false;

            final KNJ_Control date = new KNJ_Control();
            final KNJ_Control.ReturnVal returnval = date.Control(db2);
            _date = KNJ_EditDate.h_format_JP(db2, returnval.val3);

        }

        /** 中高一貫はTrue */
        private boolean getJh(
                final DB2UDB db2,
                final String year,
                final String semester
        ) throws Exception {
            boolean rtnflg = false;
            final String jhsql = "SELECT COUNT(*) AS CNT FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE > '03' ";
            try {
                db2.query(jhsql);
                final ResultSet rs = db2.getResultSet();
                while (rs.next()) {
                    if (0 < rs.getInt("CNT")) {
                        rtnflg = true;
                    }
                }
            } finally {
                db2.commit();
            }
            return rtnflg;
        }
    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {

        PreparedStatement studentsData = null;

        boolean rtnflg = false;

        try {
            if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                _param._isHighScool = "H".equals(_SCHOOLKIND);

                studentsData = db2.prepareStatement(getStudentsAreaInfoSql(_minGrade, _maxGrade));

                //SVF出力
                if (setSvfMain(svf, studentsData, db2)) {
                    rtnflg = true; //帳票出力のメソッド
                }
            } else {
                //SQL作成
                studentsData = db2.prepareStatement(getStudentsAreaInfoSql("01", "03"));
                
                //SVF出力
                if (setSvfMain(svf, studentsData, db2)) {
                    rtnflg = true; //帳票出力のメソッド
                }
                _param._isHighScool = _param._hischoolFlg;
                if (_param._isHighScool) {
                    studentsData = db2.prepareStatement(getStudentsAreaInfoSql("04", "06"));
                    if (setSvfMain(svf, studentsData, db2)) {
                        rtnflg = true; //帳票出力のメソッド
                    }
                }
            }
        } finally {
            DbUtils.closeQuietly(studentsData);
            db2.commit();
        }
        return rtnflg;
    }

    /**
     *  svf print 集計＆印刷処理
     */
    private boolean setSvfMain(
            final Vrw32alp svf,
            final PreparedStatement studentsData,
            final DB2UDB db2
    ) throws Exception {
        boolean nonedata = false;
        final Map selectArea = getArea(db2);

        final PrintData printData = new PrintData(db2, studentsData, selectArea);

        nonedata = printOut(svf, printData);   //印刷処理

        return nonedata;
    }

    /**
     *  地域マップ作成
     */
    private Map getArea(final DB2UDB db2) throws Exception {
        final Map rtnMap = new HashMap();

        final StringBuffer inState = new StringBuffer();
        String sep = "";
        int cnt = 1;
        if (null != _param._areaCd) {
            for (int i = 0; i < _param._areaCd.length; i++) {

                if (cnt > 30) {
                    db2.query(getAreaSql(inState.toString()));
                    final ResultSet rs = db2.getResultSet();
                    while (rs.next()) {
                        rtnMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                    }
                    inState.delete(0, inState.length());
                    sep = "";
                    cnt = 1;
                }

                inState.append(sep + "'" + _param._areaCd[i] + "'");
                sep = ",";
                cnt++;
            }
        }

        if (cnt > 1) {
            db2.query(getAreaSql(inState.toString()));
            final ResultSet rs = db2.getResultSet();
            while (rs.next()) {
                rtnMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
            }
        }

        return rtnMap;
    }

    private String getAreaSql(final String instate) {
        final String sql = "SELECT "
                         + "    NAMECD2, "
                         + "    NAME1 "
                         + "FROM "
                         + "    NAME_MST "
                         + "WHERE "
                         + "    NAMECD1 = 'H130' "
                         + "    AND NAMECD2 IN (" + instate + ") "
                         + "ORDER BY "
                         + "    NAMECD2";
        return sql;
    }

    /** 印字データクラス */
    private class PrintData {
        private Map _kenData = new HashMap();
        private Map _totalDistribute = new HashMap();

        public PrintData(
                final DB2UDB db2,
                final PreparedStatement studentsData,
                final Map selectArea
        ) throws Exception {
            final ResultSet rs = studentsData.executeQuery();
            while (rs.next()) {
                final int getGrade = (rs.getInt("GRADE") > 3) ? rs.getInt("GRADE") - 3 : rs.getInt("GRADE");
                final String setGrade = String.valueOf(getGrade);
                if (null == rs.getString("KENCD")) { 
                    continue;
                }
                setKenData(
                        rs.getString("KENCD"),
                        rs.getString("KENNAME"),
                        setGrade,
                        rs.getString("SEX"),
                        rs.getString("ADDR1"),
                        selectArea
                );
                setDistribute(setGrade, rs.getString("SEX"));
                setDistribute(TOTAL_GRADE, rs.getString("SEX"));
            }
        }

        private void setKenData(
                final String kenCd,
                final String kenName,
                final String grade,
                final String sex,
                final String addr,
                final Map selectArea
        ) {
            if (!_kenData.keySet().contains(kenCd)) {
                _kenData.put(kenCd, new KenData(kenCd, kenName, grade, sex, addr, selectArea));
            } else {
                ((KenData) _kenData.get(kenCd)).setAreaData(grade, sex, addr, selectArea);
            }
        }

        private void setDistribute(
                final String grade,
                final String sex
        ) {
            if (!_totalDistribute.keySet().contains(grade)) {
                _totalDistribute.put(grade, new DistributeValue(sex));
            } else {
                ((DistributeValue) _totalDistribute.get(grade)).set(sex);
            }
        }

        public String toString() {
            final StringBuffer stb = new StringBuffer();
            for (final Iterator it = _totalDistribute.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                stb.append(key + " 学年" + " 男計=" + ((DistributeValue) _totalDistribute.get(key))._mcnt);
                stb.append(" 女計=" + ((DistributeValue) _totalDistribute.get(key))._lcnt);
                stb.append(" 合計=" + ((DistributeValue) _totalDistribute.get(key))._tcnt + "\n");
            }
            return stb.toString();
        }
    }

    /** 県クラス */
    private class KenData implements Comparable {
        private final String _cd;
        private final String _name;
        private boolean _hasArea;
        private Map _areaData = new HashMap();
        private Map _totalDistribute = new HashMap();

        KenData(
                final String kenCd,
                final String kenName,
                final String grade,
                final String sex,
                final String addr,
                final Map selectArea
        ) {
            _cd = kenCd;
            _name = kenName;
            _hasArea = false;
            setAreaData(grade, sex, addr, selectArea);
        }

        private void setAreaData(
                final String grade,
                final String sex,
                final String addr,
                final Map selectArea
        ) {
            boolean setFlg = false;
            for (final Iterator it = selectArea.keySet().iterator(); it.hasNext();) {
                final String keyArea = (String) it.next();
                final String areaName = (String) selectArea.get(keyArea);
                if (-1 < addr.indexOf(areaName)) {
                    if (!_areaData.keySet().contains(keyArea)) {
                        _areaData.put(keyArea, new AreaData(keyArea, areaName, grade, sex));
                    } else {
                        ((AreaData) _areaData.get(keyArea)).setDistribute(grade, sex);
                        ((AreaData) _areaData.get(keyArea)).setDistribute(TOTAL_GRADE, sex);
                    }
                    _hasArea = true;
                    setFlg = true;
                }
            }

            if (!setFlg) {
                if (!_areaData.keySet().contains(OTHERS_AREA_CD)) {
                    _areaData.put(OTHERS_AREA_CD, new AreaData(OTHERS_AREA_CD, OTHERS_AREA_NAME, grade, sex));
                } else {
                    ((AreaData) _areaData.get(OTHERS_AREA_CD)).setDistribute(grade, sex);
                    ((AreaData) _areaData.get(OTHERS_AREA_CD)).setDistribute(TOTAL_GRADE, sex);
                }
            }
            setDistribute(grade, sex);
            setDistribute(TOTAL_GRADE, sex);
        }

        private void setDistribute(
                final String grade,
                final String sex
        ) {
            if (!_totalDistribute.keySet().contains(grade)) {
                _totalDistribute.put(grade, new DistributeValue(sex));
            } else {
                ((DistributeValue) _totalDistribute.get(grade)).set(sex);
            }
        }

        public String toString() {
            final StringBuffer stb = new StringBuffer();
            stb.append("県コード = " + _cd + " 県名称=" + _name + " 地域データ=" + _hasArea + "\n");
            for (final Iterator it = _totalDistribute.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                stb.append(key + " 学年" + " 男計=" + ((DistributeValue) _totalDistribute.get(key))._mcnt);
                stb.append(" 女計=" + ((DistributeValue) _totalDistribute.get(key))._lcnt);
                stb.append(" 合計=" + ((DistributeValue) _totalDistribute.get(key))._tcnt + "\n");
            }
            return stb.toString();
        }

        public int compareTo(final Object o) {
            if (!(o instanceof KenData)) {
                return -1;
            }
            final KenData that = (KenData) o;
            if (!(this._hasArea == that._hasArea)) {
                final String thishas = (this._hasArea) ? "1" : "0";
                final String thathas = (that._hasArea) ? "1" : "0";
                return thathas.compareTo(thishas);
            }
            return this._cd.compareTo(that._cd);
        }
    }

    /** 地域クラス */
    private class AreaData implements Comparable {
        private final String _cd;
        private final String _name;
        private Map _areaDistribute = new HashMap();

        AreaData(
                final String areaCd,
                final String areaName,
                final String grade,
                final String sex
        ) {
            _cd = areaCd;
            _name = areaName;
            setDistribute(grade, sex);
            setDistribute(TOTAL_GRADE, sex);
        }

        private void setDistribute(
                final String grade,
                final String sex
        ) {
            if (!_areaDistribute.keySet().contains(grade)) {
                _areaDistribute.put(grade, new DistributeValue(sex));
            } else {
                ((DistributeValue) _areaDistribute.get(grade)).set(sex);
            }
        }

        public String toString() {
            final StringBuffer stb = new StringBuffer();
            stb.append("地域コード = " + _cd + " 地域名称=" + _name + "\n");
            for (final Iterator it = _areaDistribute.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                stb.append(key + " 学年" + " 男計=" + ((DistributeValue) _areaDistribute.get(key))._mcnt);
                stb.append(" 女計=" + ((DistributeValue) _areaDistribute.get(key))._lcnt);
                stb.append(" 合計=" + ((DistributeValue) _areaDistribute.get(key))._tcnt + "\n");
            }
            return stb.toString();
        }

        public int compareTo(final Object o) {
            if (!(o instanceof AreaData)) {
                return -1;
            }
            final AreaData that = (AreaData) o;

            return this._cd.compareTo(that._cd);
        }
    }

    /** データクラス */
    private class DistributeValue {

        private int _mcnt;
        private int _lcnt;
        private int _tcnt;

        DistributeValue(final String sex) {
            set(sex);
        }

        public void set(final String sex) {
            if (SEX_MAN.equals(sex)) {
                _mcnt++;
            } else {
                _lcnt++;
            }
            _tcnt++;
        }

        public String toString() {
            return _mcnt + "/" + _lcnt + "/" + _tcnt;
        }
    }

    private boolean printOut(final Vrw32alp svf, final PrintData printData) {

        boolean rtnflg = false;
        setForm(svf);
        printsvfHead(svf);
        int lineCnt = 1;

        final List kenSorted = new ArrayList(printData._kenData.values());
        Collections.sort(kenSorted);
        for (final Iterator itKen = kenSorted.iterator(); itKen.hasNext();) {

            if (lineCnt > 1) {
                lineCnt++;
            }

            final KenData kenData = (KenData) itKen.next();

            final List areaSorted = new ArrayList(kenData._areaData.values());
            Collections.sort(areaSorted);
            if (kenData._hasArea) {
                for (final Iterator itArea = areaSorted.iterator(); itArea.hasNext();) {
                    final AreaData areaData = (AreaData) itArea.next();

                    lineCnt = printCheck(svf, lineCnt);
                    dataPrint(svf, areaData._areaDistribute, lineCnt, areaData._name);
                    lineCnt++;
                }
            }

            lineCnt = printKenTotal(svf, kenData, lineCnt);
            rtnflg = true;
        }

        dataTotalPrint(svf, printData._totalDistribute);
        svf.VrEndPage();
        debugPrint(printData);

        return rtnflg;
    }

    private int printCheck(final Vrw32alp svf, final int lineCnt) {
        if (MAXLINE < lineCnt) {
            svf.VrEndPage();
            printsvfHead(svf);
            return 1;
        }
        return lineCnt;
    }

    /** SVF-FORM **/
    private void setForm(
            final Vrw32alp svf
    ) {
        svf.VrSetForm(FORM_FILE, 1);
    }

    private void dataPrint(final Vrw32alp svf, final Map printData, final int lineCnt, final String name) {

        if (null != name) {
            final String fieldNo = (20 < name.getBytes().length) ? "2" : "1";
            svf.VrsOutn("area" + fieldNo, lineCnt, name);
        }
        for (final Iterator it = printData.keySet().iterator(); it.hasNext();) {
            final String keyGrade = (String) it.next();
            final DistributeValue dist = (DistributeValue) printData.get(keyGrade);
            if (keyGrade.equals("9")) {
                svf.VrsOutn("total_man", lineCnt, String.valueOf(dist._mcnt));
                svf.VrsOutn("total_woman", lineCnt, String.valueOf(dist._lcnt));
                svf.VrsOutn("total_syokei", lineCnt, String.valueOf(dist._tcnt));
            } else {
                svf.VrsOutn("man" + keyGrade, lineCnt, String.valueOf(dist._mcnt));
                svf.VrsOutn("woman" + keyGrade, lineCnt, String.valueOf(dist._lcnt));
                svf.VrsOutn("syokei" + keyGrade, lineCnt, String.valueOf(dist._tcnt));
            }
        }
    }

    private int printKenTotal(final Vrw32alp svf, final KenData kenData, final int lineCnt) {
        int rtnCnt = lineCnt;
        rtnCnt = printCheck(svf, lineCnt);

        rtnCnt = printCheck(svf, rtnCnt);
        dataPrint(svf, kenData._totalDistribute, rtnCnt, kenData._name);
        rtnCnt++;
        return rtnCnt;
    }

    private void dataTotalPrint(final Vrw32alp svf, final Map totalDistribute) {

        for (final Iterator it = totalDistribute.keySet().iterator(); it.hasNext();) {
            final String keyGrade = (String) it.next();
            final DistributeValue dist = (DistributeValue) totalDistribute.get(keyGrade);
            if (keyGrade.equals("9")) {
                svf.VrsOut("allman", String.valueOf(dist._mcnt));
                svf.VrsOut("allwoman", String.valueOf(dist._lcnt));
                svf.VrsOut("totalall", String.valueOf(dist._tcnt));
            } else {
                svf.VrsOut("man" + keyGrade + "kei", String.valueOf(dist._mcnt));
                svf.VrsOut("woman" + keyGrade + "kei", String.valueOf(dist._lcnt));
                svf.VrsOut("total" + keyGrade + "kei", String.valueOf(dist._tcnt));
            }
        }
    }

    private void debugPrint(final PrintData printData) {
        for (final Iterator itprint = printData._kenData.keySet().iterator(); itprint.hasNext();) {
            final String keyKen = (String) itprint.next();
            final KenData kenData = (KenData) printData._kenData.get(keyKen);

            log.debug(kenData);

            for (final Iterator itken = kenData._areaData.keySet().iterator(); itken.hasNext();) {
                final String keyArea = (String) itken.next();
                final AreaData areaData = (AreaData) kenData._areaData.get(keyArea);

                log.debug(areaData);

                for (final Iterator itarea = areaData._areaDistribute.keySet().iterator(); itarea.hasNext();) {
                    final String keyDist = (String) itarea.next();
                    final DistributeValue dist = (DistributeValue) areaData._areaDistribute.get(keyDist);

                    log.debug(dist);
                }
            }
        }
    }

    /**
     *  SVF-FORM ページ見出し印刷
     */
    private void printsvfHead(
            final Vrw32alp svf
    ) {

        svf.VrsOut("nendo",    _param._gengou);
        svf.VrsOut("DATE",     _param._date);
        svf.VrsOut("AREA_DIV", "地域");
        svf.VrsOut("DEF_DIV",  _param._title);
        if (_param._isHighScool) {
            svf.VrsOut("GRADE1", "４");
            svf.VrsOut("GRADE2", "５");
            svf.VrsOut("GRADE3", "６");
        } else {
            svf.VrsOut("GRADE1", "１");
            svf.VrsOut("GRADE2", "２");
            svf.VrsOut("GRADE3", "３");
        }
    }

    /**PrepareStatement作成**/
    // CSOFF: ExecutableStatementCount
    // CSOFF: MethodLength
    private String getStudentsAreaInfoSql(
            final String sGrade,
            final String eGrade
    ) {

    //  生徒及び公欠・欠席者データ
        final StringBuffer stb = new StringBuffer();
        String fieldZip = "";
        String fieldAdr = "";
        String tableName = "GUARDIAN_DAT";
        if (_param._outputA.equals("1")) {
            fieldZip = "GUARD_ZIPCD";
            fieldAdr = "GUARD_ADDR1";
        } else if (_param._outputA.equals("2")) {
            fieldZip = "GUARANTOR_ZIPCD";
            fieldAdr = "GUARANTOR_ADDR1";
        } else {
            fieldZip = "ZIPCD";
            fieldAdr = "ADDR1";
            tableName = makeStudentSql();
        }
        /* 2005/02/18Modify yamasihro 異動者を除外 */
        stb.append("with ziptable as ( ");
        stb.append("SELECT ");
        stb.append("    " + fieldZip + ", ");
        stb.append("    case when substr(" + fieldZip + ",1,2) IN('00','04','05','06','07','08','09') then '00北海道' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('03') then '01青森県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('02') then '02岩手県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('98') then '03宮城県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('01') then '04秋田県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('99') then '05山形県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('96','97') then '06福島県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('30','31') then '07茨城県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('32') then '08栃木県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('37') then '09群馬県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('33','34','35','36') then '10埼玉県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('26','27','28','29') then '11千葉県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('10','11','12','13','14','15','16','17','18','19','20') then '12東京都' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('21','22','23','24','25') then '13神奈川県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('94','95') then '14新潟県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('93') then '15富山県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('92') then '16石川県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('91') then '17福井県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('40') then '18山梨県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('38','39') then '19長野県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('50') then '20岐阜県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('41','42','43') then '21静岡県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('44','45','46','47','48','49') then '22愛知県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('51') then '23三重県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('52') then '24滋賀県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('60','61','62') then '25京都府' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('53','54','55','56','57','58','59') then '26大阪府' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('65','66','67') then '27兵庫県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('63') then '28奈良県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('64') then '29和歌山県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('68') then '30鳥取県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('69') then '31島根県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('70','71') then '32岡山県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('72','73') then '33広島県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('74','75') then '34山口県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('77') then '35徳島県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('76') then '36香川県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('79') then '37愛媛県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('78') then '38高知県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('80','81','82','83') then '39福岡県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('84') then '40佐賀県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('85') then '41長崎県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('86') then '42熊本県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('87') then '43大分県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('88') then '44宮崎県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('89') then '45鹿児島県' ");
        stb.append("         when substr(" + fieldZip + ",1,2) IN('90') then '46沖縄県' ELSE NULL END KENCD ");
        stb.append("FROM ");
        stb.append("    " + tableName + " t1 ");
        stb.append("GROUP BY ");
        stb.append("    " + fieldZip + " ");
        stb.append(") ");

        stb.append("SELECT ");
        stb.append("    SUBSTR(T4.KENCD,1,2) AS KENCD, ");
        stb.append("    SUBSTR(T4.KENCD,3) AS KENNAME, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T3.SEX, ");
        stb.append("    T2." + fieldAdr + " AS ADDR1 ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_DAT t1, ");
        stb.append("    " + tableName + " t2, ");
        stb.append("    SCHREG_BASE_MST t3, ");
        stb.append("    ziptable t4, ");
        stb.append("    SCHREG_REGD_HDAT T5 ");
        if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
            stb.append(" , SCHREG_REGD_GDAT GDAT ");
        }
        stb.append("WHERE ");
        stb.append("    t1.YEAR = '" + _param._year + "' AND ");
        stb.append("    t1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    t1.GRADE BETWEEN '" + sGrade + "' AND '" + eGrade + "' AND ");
        stb.append("    t1.SCHREGNO = t2.SCHREGNO AND ");
        stb.append("    t1.SCHREGNO = t3.SCHREGNO AND ");
        stb.append("    t2." + fieldZip + " IS NOT NULL AND ");
        stb.append("    t2." + fieldZip + " = t4." + fieldZip + " AND ");
        stb.append("    T5.YEAR = '" + _param._year + "' AND ");
        stb.append("    T5.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    T5.GRADE = T1.GRADE AND ");
        stb.append("    T5.HR_CLASS = T1.HR_CLASS ");
        if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
            stb.append(" AND GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE AND GDAT.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("ORDER BY ");
        stb.append("    CASE SUBSTR(T4.KENCD,1,2) WHEN '26' THEN '1' WHEN '28' THEN '2' ELSE '3'END, ");
        stb.append("    T4.KENCD, ");
        stb.append("    T2." + fieldAdr);
//      log.debug(stb);

        return stb.toString();

    }
    // CSON: MethodLength
    // CSON: ExecutableStatementCount

    /** 生徒用SQL */
    private String makeStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("(SELECT ");
        stb.append("    T1.* ");
        stb.append("FROM ");
        stb.append("    SCHREG_ADDRESS_DAT T1, ");
        stb.append("    (SELECT ");
        stb.append("        SCHREGNO, ");
        stb.append("        MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("    FROM ");
        stb.append("        SCHREG_ADDRESS_DAT ");
        stb.append("    WHERE ");
        stb.append("        SCHREGNO IN (SELECT ");
        stb.append("                        SCHREGNO ");
        stb.append("                    FROM ");
        stb.append("                        SCHREG_REGD_DAT ");
        stb.append("                    WHERE ");
        stb.append("                        YEAR = '" + _param._year + "' ");
        stb.append("                        AND SEMESTER = '" + _param._semester + "' ");
        stb.append("                    ) ");
        stb.append("    GROUP BY ");
        stb.append("        SCHREGNO ");
        stb.append("    ) T2 ");
        stb.append("WHERE ");
        stb.append("    T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    AND T1.ISSUEDATE = T2.ISSUEDATE) ");

        return stb.toString();
    }

}
