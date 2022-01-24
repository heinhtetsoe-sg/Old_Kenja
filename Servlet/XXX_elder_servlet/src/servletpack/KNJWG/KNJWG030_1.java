// kanji=漢字
/*
 * $Id: 374e4b24072082ebbcfc51f999b7f45ad18e85c3 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJWE.detail.KNJ_StudyrecSql;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;

/*
 *  学校教育システム 賢者 [事務管理]  単位習得証明書
 *
 *  2005/07/12 yamashiro 出力項目を追加（生年月日・課程・学科・入学・卒業・出欠）
 *  2005/08/04 yamashiro 最後尾が'入学'であれば'入学'と出力( => 近大付属仕様 )
 *                       各出力様式用の内部クラスを追加し、標準と近大付属の出力仕様を分ける
 *  2005/08/26 yamashiro 近大付属版の校長名出力仕様変更
 *  2005/09/07 yamashiro 2005/08/26の変更を無効とする
 *  2005/11/18 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度
 *                        学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 *  2006/01/18 Modify yamashiro
 *    ○修得単位数の合計欄の不具合を修正  --NO001
 *    ○校長名の職名が出力されない不具合を修正            --NO001
 */

public class KNJWG030_1 {

    private static final Log log = LogFactory.getLog(KNJWG030_1.class);

    Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                         //Databaseクラスを継承したクラス
    int ret;                            //ＳＶＦ応答値
    boolean nonedata;
    static int gline = 39;              //学習記録評価出力行数
    protected PreparedStatement ps1, ps2, ps6, ps7;
    private KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定 05/07/12 Build
    private KNJWG030_1.outDetailCommonClass outobj;  //各出力様式のクラス 05/08/04 Build
    public boolean _isJuniorHiSchool;
    public boolean _isHosei;
    public boolean _isChristianEra;
    private boolean _seirekiFlg = false;
    private final Map _noDispSubclass;
    private static Map _w029Map;

    public KNJWG030_1(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws SQLException {
        this.db2 = db2;
        this.svf = svf;
        nonedata = false;
        setJuniorHiSchool();
        setChristianEra();
        _noDispSubclass = getNoDispSubclass(db2);
        _w029Map = loadW029Map(db2);
    }

    private Map loadW029Map(final DB2UDB db2) throws SQLException {
        final Map retMap = new HashMap();
        ResultSet rs = null;
        String sql = "SELECT * FROM V_NAME_MST WHERE NAMECD1 = 'W029' ORDER BY YEAR, NAMECD1 ";
        try {
            db2.query(sql);
            rs = db2.getResultSet();
            Map setNamecd2Map = new HashMap();
            String befYear = "";
            while (rs.next()) {
                if (!befYear.equals(rs.getString("YEAR"))) {
                    setNamecd2Map = new HashMap();
                }
                setNamecd2Map.put(rs.getString("NAME1"), rs.getString("NAMECD2"));
                retMap.put(rs.getString("YEAR"), setNamecd2Map);
                befYear = rs.getString("YEAR");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return retMap;
    }

    private boolean isW029(final String year, final String subclassCd) {
        if (!_w029Map.containsKey(year)) {
            return false;
        }
        final Map setNamecd2Map = (Map) _w029Map.get(year);
        return setNamecd2Map.containsKey(subclassCd);
    }

    private Map getNoDispSubclass(final DB2UDB db2) throws SQLException {
        final Map rtnMap = new HashMap();
        final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'W027' ";

        db2.query(sql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            while (rs.next()) {
                rtnMap.put(rs.getString("NAME1"), rs.getString("NAME1"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnMap;
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat() {
        try {
            setSeirekiFlg();
        //  学習記録データ
            KNJ_StudyrecSql obj_StudyrecSql = new KNJ_StudyrecSql("hyde", "hyde", 1, false, _isHosei);
            ps1 = db2.prepareStatement(obj_StudyrecSql.getSchregStudyRec("011"));
            //  学習記録データ(見込)
            ps2 = db2.prepareStatement(obj_StudyrecSql.getSchregStudyRec("018"));
        //  個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            //ps6 = db2.prepareStatement( obj_Personalinfo.sql_info_reg("0000001000") );
            ps6 = db2.prepareStatement(obj_Personalinfo.studentInfoSql(false));
        //  学校データ
            servletpack.KNJWG.detail.KNJ_SchoolinfoSql obj_SchoolinfoSql = null;
            obj_SchoolinfoSql = new servletpack.KNJWG.detail.KNJ_SchoolinfoSql("10000");
            ps7 = db2.prepareStatement(obj_SchoolinfoSql.pre_sql());
        } catch( Exception e ){
            log.error("pre_stat error! " + e );
        }
    }

    private void setSeirekiFlg() {
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
    }

    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        try {
            if( ps1 != null ) ps1.close();
            if( ps6 != null ) ps6.close();
            if( ps7 != null ) ps7.close();
        } catch( Exception e ){
            log.error("pre_stat_f error! " + e );
        }
    }

    /**
     *  学校情報
     *  2005/08/26 Modify 校長名を'X X(姓)   X X(名)'と編集して出力　職名と校長名を別けて出力
     */
    public void head_out(
            final String year,
            final String date,
            final Map paramap,
            final String certifkind,
            final String number
    ) {
        outobj = new KNJWG030_1.outDetailCommonClass();
        outobj.svfPrintSchoolInfo(year, date, paramap, certifkind, number);
    }

    /**
     *  個人情報
     */
    public void studentInfoPrint(
            final String schregno,
            final String year,
            final String semes,
            final String certifkind
    ) {
        ResultSet rs = null;
        try {
            int pp = 1;
            ps6.setString(pp++, schregno );
            ps6.setString(pp++, year );
            ps6.setString(pp++, semes );
            rs = ps6.executeQuery();

            if (rs.next()) {
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("BIRTHDAY", changePrintDate(rs.getString("BIRTHDAY")));
                svf.VrsOut("COURSE_MAJOR", rs.getString("COURSENAME") + rs.getString("MAJORNAME"));
                svf.VrsOut("COURSRCODE", rs.getString("COURSECODENAME"));

                if (!rs.getString("STUDENT_DIV").equals("05")) {
                    svf.VrsOut("ENT_DATE", changePrintDate(rs.getString("ENT_DATE")));
                    svf.VrsOut("ENT_DIV", rs.getString("ENT_NAME"));
                    final String grdDate = certifkind.equals("011") ? rs.getString("GRD_DATE") : rs.getString("GRD_SCHEDULE_DATE");
                    final String grdDiv = certifkind.equals("011") ? rs.getString("GRD_NAME") : "卒業見込";
                    svf.VrsOut("GRD_DATE", changePrintDate(grdDate));
                    svf.VrsOut("GRD_DIV", grdDiv);
                }
            }
        } catch( Exception e ){
            log.error("error! " + e );
        } finally{
            try {
                if( rs != null )rs.close();
                db2.commit();
            } catch( Exception e ){
                log.error("error! " + e );
            }
        }
    }

    /**
     *  学習の記録
     */
    public void study_out(
            final String schregno,
            final String year,
            final String certifkind,
            final Map paramap
    ) {
        final PrintData printData = getDataPrint(schregno, year, certifkind, paramap);
        final PrintTotalData printTotalData = printData._printTotalData;
        int cnt = 1;
        final int lineCnt = printData._lineList.size();
        for (final Iterator iter = printData._lineList.iterator(); iter.hasNext();) {
            final LineData lineData = (LineData) iter.next();
            svf.VrsOut("CLASS", lineData._className);
            svf.VrsOut("SUBCLASS", lineData._subclassName);
            svf.VrsOut("GET_DIV", lineData._getDiv);
            svf.VrsOut("NENDO", changePrintYear(lineData._year));
            svf.VrsOut("CREDIT", lineData._credit);
            svf.VrsOut("VALUE", lineData._value);

            if (lineCnt > cnt) {
                cnt++;
                svf.VrEndRecord();
            }
        }
        svf.VrsOut("TOTAL_CREDIT", printTotalData._totalCredit);
        svf.VrEndRecord();
        nonedata = true;
    }

    /**
     *  学習の記録
     */
    public PrintData getDataPrint(
            final String schregno,
            final String year,
            final String certifkind,
            final Map paramap
    ) {
        final List lineList = new ArrayList();
        PrintData printData = null;
        ResultSet rs = null;
        try {
            int pp = 1;
            if (certifkind.equals("011")) {
                ps1.setString(pp++, schregno);
                ps1.setString(pp++, schregno);
                rs = ps1.executeQuery();
            } else {
                ps2.setString(pp++, schregno);
                ps2.setString(pp++, schregno);
                ps2.setString(pp++, year);
                ps2.setString(pp++, schregno);
                rs = ps2.executeQuery();
            }

            int ad_credit = 0;              //加算単位数

            while (rs.next()) {
                svf.VrsOut("CLASS", rs.getString("CLASSNAME"));
                String subclassCd = rs.getString("SUBCLASSCD");
                String subclassName = rs.getString("SUBCLASSNAME");
                String valuation = rs.getString("VALUATION");
                if ((null != _noDispSubclass && _noDispSubclass.containsKey(rs.getString("CLASSCD"))) ||
                    (isW029(year, subclassCd))
                ) {
                    valuation = "\uFF0D";
                }
                String credit = rs.getString("GET_CREDIT");
                if (((String) paramap.get("MIRISYU")).equals("2")) {
                    if (null == credit || "0".equals(credit)) continue;
                }
                if (certifkind.equals("018") && rs.getString("ORDERCD").equals("2")) {
                    credit =  "(" + rs.getString("GET_CREDIT") + ")";
                }
                final LineData lineData = new LineData(rs.getString("CLASSNAME"),
                                                       getSchoolCd(rs.getString("SCHOOLCD")),
                                                       valuation,
                                                       rs.getString("YEAR"),
                                                       subclassName,
                                                       credit);
                lineList.add(lineData);

                ad_credit += rs.getInt("GET_CREDIT");
            }
            PrintTotalData printTotalData = new PrintTotalData(String.valueOf(ad_credit));
            printData = new PrintData(lineList, printTotalData);
        } catch( Exception e ){
            log.error("study_out error!" + e );
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return printData;
    }

    private class PrintData {
        final List _lineList;
        final PrintTotalData _printTotalData;

        public PrintData(final List lineList, final PrintTotalData printTotalData) {
            _lineList = lineList;
            _printTotalData = printTotalData;
        }
    }

    private class LineData {
        final String _className;
        final String _getDiv;
        final String _value;
        final String _year;
        final String _subclassName;
        final String _credit;
        public LineData(
                final String className,
                final String getDiv,
                final String value,
                final String year,
                final String subclassName,
                final String credit
        ) {
            _className = className;
            _getDiv = getDiv;
            _value = value;
            _year = year;
            _subclassName = subclassName;
            _credit = credit;
        }
    }

    private class PrintTotalData {
        final String _totalCredit;

        public PrintTotalData(
                final String totalCredit
        ) {
            _totalCredit = totalCredit;
        }
    }

    /**
     * @param string
     * @return
     */
    private String getSchoolCd(final String schoolCd) {
        final Map schoolDisp = new HashMap();
        schoolDisp.put("0", "");
        schoolDisp.put("1", "前");
        schoolDisp.put("2", "大");
        schoolDisp.put("3", "高");
        return (String) schoolDisp.get(schoolCd);
    }

    public String changePrintDate(final String date) {
        if (null != date) {
            if (_seirekiFlg) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        } else {
            return "";
        }
    }

    public String changePrintYear(final String year) {
        if (_seirekiFlg) {
            return year + "年度";
        } else {
            return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
        }
    }

    /**
     * @param isJuniorHiSchool 設定する isJuniorHiSchool。
     */
    void setJuniorHiSchool() {
        try {
            db2.query("SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2 = '00'");
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                String str = rs.getString("NAMESPARE2");
                if (str != null) {
                    _isJuniorHiSchool = true;
                }

                str = rs.getString("NAME1");
                if ("HOUSEI".equals(str)) {
                    _isHosei = true;
                }
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    /**
     * 西暦フラグをセット。
     */
    void setChristianEra() {
        try {
            db2.query("SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2 = '00' AND NAME1='2'");
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                String str = rs.getString("NAME1");
                if (str != null) {
                    _isChristianEra = true;
                }
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    /**
     * 西暦に変換。
     * 
     * @param  strx     : '2008/03/07' or '2008-03-07'
     * @param  pattern  : 'yyyy年M月d日生'
     * @return hdate    : '2008年3月7日生'
     */
    String getChristianEra(String strx, String pattern) {
        String hdate = new String();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse( strx );
            } catch ( Exception e ) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse( strx );
                } catch ( Exception e2 ) {
                    hdate = "";
                    return hdate;
                }
            }
            SimpleDateFormat sdfseireki = new SimpleDateFormat(pattern);
            hdate = sdfseireki.format(dat);
        } catch ( Exception e3 ) {
            hdate = "";
        }
        return hdate;
    }

    /**
     *  ＳＶＦ−ＦＯＲＭフィールド初期化
     **/
    public void svf_int() {
        ret = svf.VrsOut("name"         ,"");       //生徒氏名
        ret = svf.VrsOut("BRITHDAY"     ,"");       //生年月日
        ret = svf.VrsOut("lesson"       ,"");       //総合学習修得単位数
        ret = svf.VrsOut("abroad"       ,"");       //留学修得単位数
        ret = svf.VrsOut("subtotal"     ,"");       //小計修得単位数
        ret = svf.VrsOut("total_credit" ,"");       //総合修得単位数
    }



    //--- 内部クラス -------------------------------------------------------
    /*
     *  標準出力仕様のクラス
     */
    private class outDetailCommonClass {

        /*
         * 学校情報
         */
        void svfPrintSchoolInfo(
                final String year,
                final String date,
                final Map paramap,
                final String certifkind,
                final String number
        ) {
            ResultSet rs = null;
            try {
                int pp = 1;
                ps7.setString(pp++, year );
                ps7.setString(pp++, certifkind);
                ps7.setString(pp++, year );
                rs = ps7.executeQuery();

                if (rs.next()) {
                    svf.VrsOut("DATE", changePrintDate(date));
                    setScoolInfo("SYOSYO_NAME", rs.getString("SYOSYO_NAME"));
                    setScoolInfo("CERTIF_NO", number);
                    setScoolInfo("SYOSYO_NAME2", rs.getString("SYOSYO_NAME2"));
                    setScoolInfo("SCHOOLNAME", rs.getString("SCHOOLNAME1"));
                    setScoolInfo("JOBNAME", rs.getString("PRINCIPAL_JOBNAME"));
                    setScoolInfo("STAFFNAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch( Exception e ){
                log.error("head_out error! " + e );
            } finally{
                try {
                    if( rs != null )rs.close();
                    db2.commit();
                } catch( Exception e ){
                    log.error("error! " + e );
                }
            }
        }

        private void setScoolInfo(final String fieldName, final String value) {
            if (value != null) {
                svf.VrsOut(fieldName,  value);
            }
        }

    }

}
