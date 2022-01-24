// kanji=漢字
/*
 * $Id: fbc54b7fa74a5404e618b5a996bb9766cda03f12 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.io.UnsupportedEncodingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;


/*
 *
 *  学校教育システム 賢者 [事務管理]
 *
 *      卒業証明書（和）     Form-ID:KNJWG010_1   証明書種別:001
 *      卒業証明書（英）     Form-ID:KNJWG010_2   証明書種別:002
 *      卒業見込証明書（和） Form-ID:KNJWG010_3   証明書種別:003
 *      卒業見込証明書（英） Form-ID:KNJWG010_6   証明書種別:012
 *      在学証明書（和）     Form-ID:KNJWG010_4   証明書種別:004
 *      在学証明書（英）     Form-ID:KNJWG010_5   証明書種別:005
 *      在籍証明書（和）     Form-ID:KNJWG010_7   証明書種別:013
 *      在籍証明書（英）     Form-ID:KNJWG010_8   証明書種別:014
 *      修了証明書（和）     Form-ID:KNJWG010_9   証明書種別:015
 *      修了証明書（英）     Form-ID:KNJWG010_10  証明書種別:016
 *
 *      引数について  param[0] :学籍番号
 *                    param[2] :対象年度
 *                    param[3] :対象学期
 *                    param[8] :証明書日付
 *                    param[9] :証明書番号
 *                    param[11]:現年度
 *
 *    2005/01/26 yamashiro  東京都用に修正
 *    2005/10/22 m-yama /KNJZ/KNJ_Schoolinfoを/KNJWG/KNJ_SchoolinfoSqlへ変更 NO001
 *    2005/10/22 m-yama 除籍通知書追加 NO002
 *    2005/10/22 m-yama 在学、終了の第*学年を単位制：*年次 その他：第*学年 NO003
 *
 */

public class KNJWG010_1T {

    private static final Log log = LogFactory.getLog(KNJWG010_1T.class);

    public Vrw32alp svf = new Vrw32alp();       //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    public DB2UDB db2;                          //Databaseクラスを継承したクラス
    public boolean nonedata;
    protected PreparedStatement ps6,ps7;
    ResultSet rs;
    final KNJDefineSchool _definecode;  // 各学校における定数等設定
    private String _private_schooldiv;  // 生徒別学校制
    private boolean _seirekiFlg = false;
    private boolean _ikkanFlg = false;
    private String _nameZ010 = "";

    public KNJWG010_1T(){
        _definecode = new KNJDefineSchool();  // 各学校における定数等設定
        setNameZ010();

    }

    public KNJWG010_1T(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        this.db2 = db2;
        this.svf = svf;
        nonedata = false;
        _definecode = definecode;
        setNameZ010();
    }

    private void setNameZ010() {
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                _nameZ010 = rs.getString("NAME1");
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
     *  各種証明書印刷処理
     *      証書種別番号からフォームの枝番を取出したものをココでの通番号とする
     */
    public void printSvfMain(String param[]) {
        Base schoolobj = null;
        if (_definecode.schoolmark.equals("TOK")) {
            schoolobj = new Tokyo();
        } else {
            schoolobj = new Hiro();            
        }
        schoolobj.print(param);
    }


    /**
     *  PrepareStatement作成
     */
    public void pre_stat(String ptype)
    {
        try {
            // 個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            ps6 = db2.prepareStatement(obj_Personalinfo.sql_info_reg("11110011"));
            // 学校データ
            if (ps7 == null) {
                final String sql;
                if (_definecode.schoolmark.equals("KIN") || _definecode.schoolmark.equals("KINJUNIOR")) {
                    sql = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("12000").pre_sql();
                } else if (_definecode.schoolmark.equals("HIRO")) {
                    sql = new servletpack.KNJWG.detail.KNJ_SchoolinfoSql("12100").pre_sql();
                } else {
                    sql = new servletpack.KNJWG.detail.KNJ_SchoolinfoSql("12000").pre_sql();
                }
                ps7 = db2.prepareStatement(sql);
            }
        } catch( Exception e ){
            log.error("priparedStatement error! ", e );
        }
        if (log.isDebugEnabled()) log.debug("USE KNJWG010_1T");
    }


    /**
     *  PrepareStatement close
     */
    public void pre_stat_f()
    {
        try {
            if( ps6 != null) ps6.close();
            if( ps7 != null) ps7.close();
        } catch( Exception e ){
            log.error("priparedStatement close() error! ", e );
        }
    }


    /**
     *  過卒生対応年度取得
     */
    public static String b_year(String pdate)
    {
        String b_year = new String();

        if(pdate != null){
            b_year = pdate.substring(0,4);
            String b_month = pdate.substring(5,7);
            if(b_month.equals("01")  ||  b_month.equals("02")  ||  b_month.equals("03"))
                b_year = String.valueOf(Integer.parseInt(b_year)-1);
        }

        return b_year;

    }//b_yearの括り


    /*
     *  フォームを設定
     *      引数について  int pdiv:証明書種別番号
     */
    private boolean printSvfGetForm(String param[]) {

        final String formName = _nameZ010 != null && _nameZ010.equals("WITHUS") ? "W" : "";

        int ret = 0;
        if (0 == ret) ret = 0;
        int pdiv = 0;
        pdiv = Integer.parseInt(param[1]);
        if (pdiv == 1) ret = svf.VrSetForm("KNJ" + formName + "G010_1.frm", 1);  // 卒業証明書（和）
        else if (pdiv == 2) ret = svf.VrSetForm("KNJWG010_2.frm", 1);  // 卒業証明書（英）
        else if (pdiv == 3) ret = svf.VrSetForm("KNJ" + formName + "G010_3.frm", 1);  // 卒業見込証明書（和）
        else if (pdiv == 19) ret = svf.VrSetForm("KNJWG010_6.frm", 1);  // 卒業見込証明書（英）
        else if (pdiv == 4) ret = svf.VrSetForm("KNJ" + formName + "G010_4.frm", 1);  // 在学証明書（和）
        else if (pdiv == 5) ret = svf.VrSetForm("KNJWG010_5.frm", 1);  // 在学証明書（英）
        else if (pdiv == 13) ret = svf.VrSetForm("KNJ" + formName + "G010_7.frm", 1);  // 在籍証明書（和）
        else if (pdiv == 14) ret = svf.VrSetForm("KNJWG010_8.frm", 1);  // 在籍証明書（英）
        else if (pdiv == 15) ret = svf.VrSetForm("KNJWG010_9.frm", 1);  // 修了証明書（和）
        else if (pdiv == 16) ret = svf.VrSetForm("KNJWG010_10.frm", 1);  // 修了証明書（英）
        else if (pdiv == 20) ret = svf.VrSetForm("KNJWG010_11.frm", 1);  // 除籍証明書（和）
        else if (pdiv == 21) ret = svf.VrSetForm("KNJWG010_5.frm", 1);  // 在学証明書（中英）
        else if (pdiv == 22) ret = svf.VrSetForm("KNJWG010_1.frm", 1);  // 卒業証明書（中英）
        else return false;
        
        return true;
    }

    /*
     *  証明日付の出力
     */
    private void printsvfDate(
            final String date,
            final String fname
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if (date != null && 0 < date.length()) {
            if (_seirekiFlg) {
                svf.VrsOut(fname, date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date));  // 証明日付
            } else {
                svf.VrsOut(fname, KNJ_EditDate.h_format_JP(date));  // 証明日付
            }
        } else {
            ret = svf.VrAttribute(fname, "Hensyu=1");
            ret = svf.VrsOut(fname, "年　 月　 日");
        }
    }

    /*
     *  証明日付の出力
     */
    private void printsvfDate_US(
            final String date,
            final String fname
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if (date != null && 0 < date.length()) {
            ret = svf.VrsOut(fname, KNJ_EditDate.h_format_US(date));  // 証明日付
        } else {
            ret = svf.VrsOut(fname, "  ,   ,    ");
        }
    }

    /*
     * @param param
     * @throws SQLException
     */
    private void printSvfSyoushoNum(
            String number,
            String syousyoname
    ) throws SQLException {
        int ret = 0;
        if (0 == ret) ret = 0;
        if (number == null) return;
        if (syousyoname != null) {
            ret = svf.VrsOut("NENDO_NAME",  number + syousyoname);  // 証明書番号
        }else {
            ret = svf.VrsOut("NENDO_NAME",  number);  // 証明書番号
        }
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる学校別出力処理クラス
     */
    abstract class Base {
        School_Common schoolobj;
        Private_Common privateobj;

        abstract void print (String param[]); 
        abstract boolean printSchool(String param[]);
        abstract boolean printPrivate(String param[]);
    }
        
    //--- 内部クラス -------------------------------------------------------
    /**
     * 東京都版の出力処理クラス
     */
    class Tokyo extends Base {

        /* 
         * 出力処理
         */
        void print (String param[]) {
            if (!printSvfGetForm(param)) return;;  // フォームを設定
            setSeirekiFlg();
            setIkkanFlg();
            create(param);
            if (schoolobj == null) return;  // 非処理
            if (!printSchool(param)) return;  // 学校情報出力
            if (printPrivate(param)) {  // 個人情報出力
                int ret = svf.VrEndPage();
                if (0 == ret) ret = 0;
                nonedata = true;
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

        private void setIkkanFlg() {
            try {
                String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAMESPARE2 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    _ikkanFlg = true; //中高一貫
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        /* 
         * 帳票別クラス生成
         */
        void create (String param[]) {
            int pdiv = 0;
            pdiv = Integer.parseInt(param[1]);
            switch (pdiv) {
                case 1: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Graduate_JP_T(); break;  // 卒業証明書（和）
                case 2: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 卒業証明書（英）
                case 3: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Graduate_JP_T(); break;  // 卒業見込証明書（和）
                case 4: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Regist_JP_T(); break;  // 在学証明書（和）
                case 5: schoolobj = new School_Regist_EN_T(); privateobj = new Private_Regist_EN_T(); break;  // 在学証明書（英）
                case 13: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Registered_JP_T(); break;  // 在籍証明書（和）
                case 14: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 在籍証明書（英）
                case 15: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Complete_JP_T(); break;  // 修了証明書（和）
//                case 16: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 修了証明書（英）
                case 19: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 卒業見込証明書（英）
                case 20: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Expel_JP_T(); break;  // 除籍証明書（和）
                case 21: schoolobj = new School_Regist_EN_T(); privateobj = new Private_Regist_EN_T(); break;  // 在学証明書（英）
                case 22: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Graduate_JP_T(); break;  // 卒業証明書（和）
                default: schoolobj = null; privateobj = null; break;
            }
        }

        /*
         *  学校情報出力
         */
        boolean printSchool(String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            int p = 0;
            try {
                ps7.setString(++p, param[2]);  // 対象年度
                ps7.setString(++p, param[1]);  // 証明書種別
                ps7.setString(++p, param[11]);  // 現年度
                ps7.setString(++p, "00000000");  // SQL選択フラグ
                rs = ps7.executeQuery();
                if (rs.next()) {
                    ret = svf.VrsOut("SYOSYO_NAME",  rs.getString("SYOSYO_NAME") );  //証書名
                    ret = svf.VrsOut("SYOSYO_NAME2", rs.getString("SYOSYO_NAME2") ); //証書名２
                    //証書番号の印刷 0:あり,1:なし
                    if (rs.getString("CERTIF_NO") != null && rs.getString("CERTIF_NO").equals("0")) {
                        ret = svf.VrsOut("CERTIF_NO",  param[9] );
                    }
                    printSyoushoNum(param[9], rs.getString("SYOSYO_NAME"));  // 証書番号を出力
                    schoolobj.printInfo(param);
                }
            } catch(Exception e) {
                log.error("rs.next()! ", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (Exception e) { log.error("rs.close() error! ", e); }
            }
            return true;
        }

        /*
         *  個人情報出力
         */
        boolean printPrivate(String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            int p = 0;
            boolean nonedata = false;

            //  学校名の取得 NO003
            _private_schooldiv = null;
            try {
                String sql = "SELECT SCHOOLDIV, SCHOOLTELNO, SCHOOLFAXNO FROM SCHOOL_MST WHERE YEAR = '"+param[2]+"'";
                db2.query(sql);
                ResultSet rs3 = db2.getResultSet();
                while( rs3.next() ){
                    _private_schooldiv = rs3.getString("SCHOOLDIV");
                    svf.VrsOut("PHONE", rs3.getString("SCHOOLTELNO"));
                    svf.VrsOut("FAX", rs3.getString("SCHOOLFAXNO"));
                }
                rs3.close();
                db2.commit();
            } catch( Exception e ){
                log.warn("ctrl_date get error!",e);
            }
            try {
                p = 0;
                ps6.setString( ++p, param[0] );     //学籍番号
                ps6.setString( ++p, param[2] );     //対象年度
                ps6.setString( ++p, param[3] );     //対象学期
                ps6.setString( ++p, param[0] );     //学籍番号
                ps6.setString( ++p, param[2] ); //対象年度
                rs = ps6.executeQuery();
                if( rs.next() ){
                    privateobj.printInfo(param);
                    nonedata = true;
                }
            } catch( Exception e ){
                log.error("rs.next()! ", e );
            } finally {
                try {
                    if( rs != null ) rs.close();
                } catch( Exception e ){
                    log.error("rs.close() error! ", e );
                }
            }
            return nonedata;
        }

        /*
         * @param param
         * @throws SQLException
         */
        private void printSyoushoNum(
                String number,
                String syousyoname
        ) throws SQLException {
            int ret = 0;
            if (0 == ret) ret = 0;
            if (number == null) return;
            if (syousyoname != null) {
                ret = svf.VrsOut("NENDO_NAME",  number + syousyoname);  // 証明書番号
            }else {
                ret = svf.VrsOut("NENDO_NAME",  number);  // 証明書番号
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 広島版の出力処理クラス
     */
    class Hiro extends Base {

        /* 
         * 出力処理
         */
        void print (String param[]) {
            if (!printSvfGetForm(param)) return;;  // フォームを設定
            create(param);
            if (schoolobj == null) return;  // 非処理
            if (!printSchool(param)) return;  // 学校情報出力
            if (printPrivate(param)) {  // 個人情報出力
                int ret = svf.VrEndPage();
                if (0 == ret) ret = 0;
                nonedata = true;
            }
        }

        /* 
         * 帳票別クラス生成
         */
        void create (String param[]) {
            int pdiv = 0;
            pdiv = Integer.parseInt(param[1]);
            switch (pdiv) {
                case 1: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Graduate_JP_H(); break;  // 卒業証明書（和）
//                case 2: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 卒業証明書（英）
                case 3: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_GraduateExpect_JP_H(); break;  // 卒業見込証明書（和）
                case 4: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Regist_JP_H(); break;  // 在学証明書（和）
                case 5: schoolobj = new School_Regist_EN_H(); privateobj = new Private_Regist_EN_H(); break;  // 在学証明書（英）
                case 13: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Registered_JP_T(); break;  // 在籍証明書（和）
//                case 14: schoolobj = new School_Regist_EN_T(); privateobj = new Private_Regist_EN_H(); break;  // 在籍証明書（英）
                case 15: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Complete_JP_T(); break;  // 修了証明書（和）
//                case 16: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 修了証明書（英）
//                case 19: schoolobj = new School_Graduate_EN_T(); privateobj = new Private_Graduate_EN_T(); break;  // 卒業見込証明書（英）
                case 20: schoolobj = new School_Graduate_JP_T(); privateobj = new Private_Expel_JP_T(); break;  // 除籍証明書（和）
                default: schoolobj = null; break;
            }
        }

        /*
         *  学校情報出力
         */
        boolean printSchool(String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            int p = 0;
            try {
                ps7.setString(++p, param[2]);  // 対象年度
                ps7.setString(++p, param[1]);  // 証明書種別
                ps7.setString(++p, param[11]);  // 現年度
                ps7.setString(++p, "00000000");  // SQL選択フラグ
                ps7.setString(++p, param[2]);  // 対象年度
                rs = ps7.executeQuery();
                if (rs.next()) {
                    printSyoushoNum(param[9], null);  // 証書番号を出力
                    schoolobj.printInfo(param);
                }
            } catch(Exception e) {
                log.error("rs.next()! ", e);
            } finally {
                try {
                    if (rs != null) rs.close();
                } catch (Exception e) { log.error("rs.close() error! ", e); }
            }
            return true;
        }

        /*
         *  個人情報出力
         */
        boolean printPrivate(String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            int p = 0;
            boolean nonedata = false;

            //  学校名の取得 NO003
            _private_schooldiv = null;
            try {
                String sql = "SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR = '"+param[2]+"'";
                db2.query(sql);
                ResultSet rs3 = db2.getResultSet();
                while( rs3.next() ){
                    _private_schooldiv = rs3.getString("SCHOOLDIV");
                }
                rs3.close();
                db2.commit();
            } catch( Exception e ){
                log.warn("ctrl_date get error!",e);
            }
            try {
                p = 0;
                ps6.setString( ++p, param[0] );     //学籍番号
                ps6.setString( ++p, param[2] );     //対象年度
                ps6.setString( ++p, param[3] );     //対象学期
                ps6.setString( ++p, param[0] );     //学籍番号
                ps6.setString( ++p, param[2] ); //対象年度
                rs = ps6.executeQuery();
                if( rs.next() ){
                    privateobj.printInfo(param);
                    nonedata = true;
                }
            } catch( Exception e ){
                log.error("rs.next()! ", e );
            } finally {
                try {
                    if( rs != null ) rs.close();
                } catch( Exception e ){
                    log.error("rs.close() error! ", e );
                }
            }
            return nonedata;
        }

        /*
         * @param param
         * @throws SQLException
         */
        private void printSyoushoNum(
                String number,
                String syousyoname
        ) throws SQLException {
            int ret = 0;
            if (0 == ret) ret = 0;
            if (number == null) return;
            ret = svf.VrsOut("CERTIFNO", number);  // 証明書番号
        }
        
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる学校情報の出力処理クラス
     */
    abstract class School_Common {

        /* 
         * 学校情報の出力
         */
        abstract void printInfo (String param[]); 
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる個人情報の出力処理クラス
     */
    abstract class Private_Common {

        /* 
         * 個人情報の出力
         */
        abstract void printInfo (String param[]);
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(和)が基準となる学校情報の出力処理クラス
     */
    class School_Graduate_JP_T extends School_Common {

        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            printsvfDate(param[8],"DATE");  // 証明日付
            try {
                ret = svf.VrsOut( "SCHOOLNAME",  rs.getString("SCHOOLNAME1") );             //学校名
                if (rs.getString("PRINCIPAL_JOBNAME") != null) { ret = svf.VrsOut("JOBNAME", rs.getString("PRINCIPAL_JOBNAME")); }  // 校長名
                if (rs.getString("PRINCIPAL_NAME") != null) { ret = svf.VrsOut("STAFFNAME", rs.getString("PRINCIPAL_NAME")); }  // 校長名
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(和)が基準となる学校情報の出力処理クラス
     */
    class School_Graduate_JP_H extends School_Common {
        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            printsvfDate(param[8],"DATE");  // 証明日付
            try {
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名称
//                String pname = null;
//                if (rs.getString("PRINCIPAL_JOBNAME") != null) { pname = rs.getString("PRINCIPAL_JOBNAME"); }
//                if (rs.getString("PRINCIPAL_NAME") != null) { pname = pname + "  " + rs.getString("PRINCIPAL_NAME"); }
//                ret = svf.VrsOut("STAFFNAME", pname);  // 職名・校長名
                if (rs.getString("PRINCIPAL_JOBNAME") != null) { ret = svf.VrsOut( "JOBNAME",   rs.getString("PRINCIPAL_JOBNAME") ); }   //校長名
                if (rs.getString("PRINCIPAL_NAME") != null ) { ret = svf.VrsOut( "STAFFNAME", rs.getString("PRINCIPAL_NAME")); }   //校長名
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Graduate_JP_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int pdiv = 0;
            pdiv = Integer.parseInt(param[1]);
            int ret = 0;
            if (0 == ret) ret = 0;
            byte check_len[] = new byte[24];
            try {
                if (rs.getString("NAME") != null){
                    check_len  = (rs.getString("NAME")).getBytes("MS932");
                }
                if (check_len.length > 24){
                    ret = svf.VrsOut( "NAME2",       rs.getString("NAME") );                                         //氏名
                }else {
                    ret = svf.VrsOut( "NAME1",       rs.getString("NAME") );                                         //氏名
                }
                if (_seirekiFlg && null != rs.getString("BIRTHDAY")) {
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")) + "生");  // 証明日付
                } else {
                    svf.VrsOut("BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //生年月日
                }

                if (_seirekiFlg && null != rs.getString("ENT_DATE")) {
                    svf.VrsOut("ENT_DATE", rs.getString("ENT_DATE").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("ENT_DATE")));
                } else {
                    svf.VrsOut("ENT_DATE",   KNJ_EditDate.h_format_JP( rs.getString("ENT_DATE") ) );
                }
                ret = svf.VrsOut( "COURSE_NAME",     rs.getString("COURSENAME") + rs.getString("MAJORNAME")); //課程+学科
                ret = svf.VrsOut( "MAJORNAME", rs.getString("MAJORNAME")); //学科
                if (_nameZ010.equals("WITHUS") && pdiv == 1) {
                    ret = svf.VrsOut( "GRADUATION", KNJ_EditDate.h_format_JP( rs.getString("GRADU_DATE") ) );  //卒業年月日
                } else if (_nameZ010.equals("WITHUS") && pdiv == 3) {
                    String scheduleDate = "";
                    PreparedStatement psGradu = null;
                    ResultSet rsGradu = null;
                    try {
                        log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        final String sql = "SELECT * FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + param[0] + "'";
                        log.debug(sql);
                        log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        psGradu = db2.prepareStatement(sql);
                        rsGradu = psGradu.executeQuery();
                        while (rsGradu.next()) {
                            scheduleDate = rsGradu.getString("GRD_SCHEDULE_DATE");
                        }
                        log.debug(sql);
                    } finally {
                        DbUtils.closeQuietly(null, psGradu, rsGradu);
                    }
                    ret = svf.VrsOut( "GRADUATION", KNJ_EditDate.h_format_JP(scheduleDate));  //卒業予定年月日
                } else {
                    ret = svf.VrsOut( "GRADUATION", KNJ_EditDate.h_format_JP_M( rs.getString("GRADU_DATE") ) );  //卒業年月日
                }
            } catch (UnsupportedEncodingException e) {
                 log.error("UnsupportedEncodingException", e);
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Graduate_JP_H extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
//            ret = svf.VrsOut("NUMBER", number);  // 証明書番号
            try {
                ret = svf.VrsOut("NAME"         , rs.getString("NAME"));  // 氏名
                ret = svf.VrsOut("BIRTHDAY"     , KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));  // 生年月日
                ret = svf.VrsOut("GRADUATION"   , KNJ_EditDate.h_format_JP_M(rs.getString("GRADU_DATE")));  // 卒業年月日
                ret = svf.VrsOut("COURSE"       , rs.getString("COURSENAME"));  // 課程
                ret = svf.VrsOut("SUBJECT"      , rs.getString("MAJORNAME"));  // 学科
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(英)が基準となる学校情報の出力処理クラス
     */
    class School_Graduate_EN_T extends School_Common {

        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            printsvfDate_US(param[8],"DATE");  // 証明日付
            try {
                final String schoolName1 = _definecode.schoolmark.equals("TOK") ? rs.getString("REMARK2") + "　" + rs.getString("REMARK3") : rs.getString("SCHOOLNAME_ENG");
                svf.VrsOut("SCHOOLNAME1", schoolName1);                    //学校名称（英）
                svf.VrsOut("SCHOOLNAME2", schoolName1);                    //学校名称（英）
                svf.VrsOut("SCHOOLNAME3_1", rs.getString("REMARK2"));                         //学校名称（英）
                svf.VrsOut("SCHOOLNAME3_2", rs.getString("REMARK3"));                         //学校名称（英）
                svf.VrsOut("SCHOOLADDRESS1", rs.getString("REMARK4"));                        //学校名称（英）
                svf.VrsOut("SCHOOLADDRESS2", rs.getString("REMARK5"));                        //学校名称（英）
                final String staffName = _definecode.schoolmark.equals("TOK") ? rs.getString("REMARK1") : rs.getString("PRINCIPAL_NAME_ENG");
                svf.VrsOut("STAFFNAME",   staffName);                //代表名（英）
                svf.VrsOut("SCHOOLNAME_JP",  rs.getString("SCHOOLNAME1") );                  //学校名（和）
                svf.VrsOut("SCHOOLNAME2_1",  rs.getString("REMARK2") );                  //学校名（和）
                svf.VrsOut("SCHOOLNAME2_2",  rs.getString("REMARK3") );                  //学校名（和）
                if (_definecode.schoolmark.equals("TOK")) {
                    svf.VrsOut( "STAFFNAME_JP",   ( rs.getString("PRINCIPAL_NAME") != null )? rs.getString("PRINCIPAL_NAME") : "" );            //校長名
                    svf.VrsOut( "JOBNAME_JP",   ( rs.getString("PRINCIPAL_JOBNAME") != null )? rs.getString("PRINCIPAL_JOBNAME") : "" ); //職名
                } else if (rs.getString("PRINCIPAL_JOBNAME") != null) {
                    svf.VrsOut( "STAFFNAME_JP",   rs.getString("PRINCIPAL_JOBNAME") + ( ( rs.getString("PRINCIPAL_NAME") != null )? rs.getString("PRINCIPAL_NAME") : "" ) );   //校長名（和）
                } else {
                    svf.VrsOut( "STAFFNAME_JP",   ( rs.getString("PRINCIPAL_NAME") != null )? rs.getString("PRINCIPAL_NAME") : "" );   //校長名（和）
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(英)が基準となる学校情報の出力処理クラス
     */
    class School_Graduate_EN_H extends School_Common {

        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            printsvfDate_US(param[8],"DATE");  // 証明日付
            try {
//                ret = svf.VrsOut("DATE", ( date != null )? KNJ_EditDate.h_format_US(date): "" );    //証明日付 05/11/18 Modify
                ret = svf.VrsOut("SCHOOLNAME1"  , rs.getString("SCHOOLNAME_ENG"));  // 学校名称
//                ret = svf.VrsOut("SCHOOLNAME2"  , rs.getString("SCHOOLNAME_ENG"));  // 学校名称
                ret = svf.VrsOut("SCHOOLNAME2"  , rs.getString("SCHOOLNAME1"));  // 学校名称
//                ret = svf.VrsOut("STAFFNAME"    , rs.getString("PRINCIPAL_NAME_ENG"));  // 代表名
                ret = svf.VrsOut("STAFFNAME"    , rs.getString("PRINCIPAL_NAME"));  // 代表名
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(英)が基準となる個人情報の出力処理クラス
     */
    class Private_Graduate_EN_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            try {
                ret = svf.VrsOut("NAME",     rs.getString("NAME_ENG"));                               //氏名（英）
                ret = svf.VrsOut("SEX",      rs.getString("SEX_ENG"));                                //性別（英）
                ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_US(rs.getString("BIRTHDAY")));     //生年月日（英）
                ret = svf.VrsOut("ENTRANCE_DAY",   KNJ_EditDate.h_format_US(rs.getString("ENT_DATE")));   //入学日（英）
                ret = svf.VrsOut("YEAR_S",   KNJ_EditDate.h_format_US_M(rs.getString("ENT_DATE")));   //入学日（英）
                ret = svf.VrsOut("YEAR_F",   KNJ_EditDate.h_format_US_M(rs.getString("GRADU_DATE"))); //卒業日（英）
                ret = svf.VrsOut("YEAR_G",   KNJ_EditDate.h_format_US(rs.getString("GRADU_DATE")));   //卒業日（英）
                ret = svf.VrsOut("GRADE",  getGradeEng(rs.getString("GRADE")) );                  //学年
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }

    }

    /** 学年を取得 */
    private String getGradeEng(final String grade) {
        String rtn = "";
        if (grade.equals("01")) {
            rtn = "1st";
        } else if (grade.equals("02")) {
            rtn = "2nd";
        } else if (grade.equals("03")) {
            rtn = "3rd";
        } else if (grade.equals("04")) {
            rtn = "4th";
        } else if (grade.equals("05")) {
            rtn = "5th";
        } else {
            rtn = "6th";
        }
        return rtn;
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業証明書(英)が基準となる個人情報の出力処理クラス
     */
    class Private_Graduate_EN_H extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            try {
//                ret = svf.VrsOut("NUMBER"   , number);                                              //証明書番号
                ret = svf.VrsOut("NAME"     , rs.getString("NAME_ENG"));                            //氏名
                ret = svf.VrsOut("SEX"      , rs.getString("SEX_ENG"));                             //性別
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_US(rs.getString("BIRTHDAY")));      //生年月日
                ret = svf.VrsOut("YEAR_S"   , KNJ_EditDate.h_format_US_M(rs.getString("ENT_DATE")));    //入学日
                ret = svf.VrsOut("YEAR_F"   , KNJ_EditDate.h_format_US_M(rs.getString("GRADU_DATE")));  //卒業日
                ret = svf.VrsOut("YEAR_G"   , KNJ_EditDate.h_format_US(rs.getString("GRADU_DATE")));    //卒業日
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 卒業見込み証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_GraduateExpect_JP_H extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            try {
//                outobj.outStudentname(rs);  // 生徒名
//                if (number != null ) ret = svf.VrsOut("NUMBER",  number);  // 証明書番号
                if (rs.getString("NAME") != null) { ret = svf.VrsOut("NAME", rs.getString("NAME")); }
                if (rs.getString("ADDR") != null) { ret = svf.VrsOut("ADDRESS1", rs.getString("ADDR")); }  // 住所
                if (rs.getString("BIRTHDAY") != null) { ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY"))); }  // 生年月日
                if (rs.getString("GRADU_DATE") != null) { ret = svf.VrsOut("GRADUATION", KNJ_EditDate.h_format_JP_M(rs.getString("GRADU_DATE"))); }  // 卒業見込日
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(和)が基準となる学校情報の出力処理クラス
     */
    class School_Regist_JP_H extends School_Common {

        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            printsvfDate(param[8],"DATE");  // 証明日付
            try {
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名称
                String pname = new String();
                if (rs.getString("PRINCIPAL_JOBNAME") != null) { pname = rs.getString("PRINCIPAL_JOBNAME"); }
                if (rs.getString("PRINCIPAL_NAME") != null) { pname = pname + "  " + rs.getString("PRINCIPAL_NAME"); }
                ret = svf.VrsOut("STAFFNAME", pname);  // 職名・校長名
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Regist_JP_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            byte check_len[] = new byte[24];
            try {
                if (rs.getString("NAME") != null){
                    check_len  = (rs.getString("NAME")).getBytes("MS932");
                }
                if (check_len.length > 24){
                    ret = svf.VrsOut( "NAME2",       rs.getString("NAME") );                                         //氏名
                }else {
                    ret = svf.VrsOut( "NAME1",       rs.getString("NAME") );                                         //氏名
                }
                if (_seirekiFlg && null != rs.getString("BIRTHDAY")) {
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")) + "生");  // 証明日付
                } else {
                    svf.VrsOut("BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //生年月日
                }

                if (_seirekiFlg && null != rs.getString("ENT_DATE")) {
                    svf.VrsOut("ENT_DATE", rs.getString("ENT_DATE").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("ENT_DATE")));
                } else {
                    svf.VrsOut("ENT_DATE",   KNJ_EditDate.h_format_JP( rs.getString("ENT_DATE") ) );
                }

                if (_private_schooldiv.equals("1")) {
                    ret = svf.VrsOut( "GRADE",      String.valueOf( rs.getInt("ANNUAL") ) + "年次" );    //学年
                } else {
                    if (_ikkanFlg && rs.getInt("GRADE") > 3) {
                        svf.VrsOut( "GRADE",      "第" + String.valueOf( rs.getInt("GRADE") - 3 ) + "学年" );     //学年
                    } else {
                        svf.VrsOut( "GRADE",      "第" + String.valueOf( rs.getInt("GRADE") ) + "学年" );     //学年
                    }
                }
                ret = svf.VrsOut( "COURSE",     ( rs.getString("COURSENAME") != null )? rs.getString("COURSENAME") : "" ); //課程
                ret = svf.VrsOut( "SUBJECT",    ( rs.getString("MAJORNAME") != null )? rs.getString("MAJORNAME") : "" );   //学科
            } catch (UnsupportedEncodingException e) {
                log.error("UnsupportedEncodingException", e);
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Regist_JP_H extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
//            ret = svf.VrsOut("NUMBER", number);  // 証明書番号
            try {
                String schooldivname;
                if (_private_schooldiv.equals("0")) {
                    schooldivname = "学年";
                } else {
                    schooldivname = "年次";
                }
                StringBuffer strb = new StringBuffer("  第" + String.valueOf(rs.getInt("ANNUAL")) + schooldivname);
                if (rs.getString("COURSENAME") != null) { strb.insert(0,rs.getString("COURSENAME")); }
                if (rs.getString("MAJORNAME") != null) { strb.insert(0,rs.getString("MAJORNAME")); }
                ret = svf.VrsOut("GRADE"    , strb.toString());  // 課程学科学年
                ret = svf.VrsOut("NAME"     , rs.getString("NAME"));  // 氏名
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));  // 生年月日
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }
    
    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(英)が基準となる学校情報の出力処理クラス
     */
    class School_Regist_EN_T extends School_Common {

        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            printsvfDate_US(param[8],"DATE");  // 証明日付
            if ("005".equals(param[1])) {
                ret = svf.VrsOut("HJDIV", "High School");
            } else {
                ret = svf.VrsOut("HJDIV", "Junior High School");
            }
            try {
                ret = svf.VrsOut("SCHOOLNAME",  rs.getString("SCHOOLNAME_ENG"));                    //学校名称（英）

                String str = rs.getString("SCHOOLADDR1_ENG");
                if (null != str) { ret = svf.VrsOut("SCHOOL_ADDRESS1", str); }
                str = rs.getString("SCHOOLADDR2_ENG");
                if (null != str) { ret = svf.VrsOut("SCHOOL_ADDRESS2", str); }

                ret = svf.VrsOut("PHONE",       ( rs.getString("SCHOOLTELNO") != null )? rs.getString("SCHOOLTELNO") : "" );    //学校電話番号

                str = rs.getString("REMARK1");
                if (null != str) { ret = svf.VrsOut("STAFFNAME", str); }

                ret = svf.VrsOut( "SCHOOLNAME_JP",  rs.getString("SCHOOLNAME1") );                  //学校名（和）
                svf.VrsOut( "JOBNAME_JP", rs.getString("PRINCIPAL_JOBNAME") != null ? rs.getString("PRINCIPAL_JOBNAME") : "" ); //職名
                svf.VrsOut( "STAFFNAME_JP", rs.getString("PRINCIPAL_NAME") != null ? rs.getString("PRINCIPAL_NAME") : "" );   //校長名（和）
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(英)が基準となる個人情報の出力処理クラス
     */
    class Private_Regist_EN_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            try {
                ret = svf.VrsOut("NAME",     rs.getString("NAME_ENG"));                               //氏名（英）
                ret = svf.VrsOut("SEX",      rs.getString("SEX_ENG"));                                //性別（英）
                ret = svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_US(rs.getString("BIRTHDAY")));     //生年月日（英）
                ret = svf.VrsOut("GRADE",    String.valueOf( rs.getInt("ANNUAL") ) );                 //学年
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(英)が基準となる学校情報の出力処理クラス
     */
    class School_Regist_EN_H extends School_Common {

        /* 
         * 学校情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            printsvfDate_US(param[8],"DATE");  // 証明日付
            try {
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOLNAME1"));  // 学校名称
                ret = svf.VrsOut("STAFFNAME", rs.getString("PRINCIPAL_NAME"));  // 代表名
                if (rs.getString("SCHOOLADDR1_ENG")!=null) { ret = svf.VrsOut("SCHOOL_ADDRESS1", rs.getString("SCHOOLADDR1_ENG")); }  // 学校住所
                if (rs.getString("SCHOOLADDR2_ENG")!=null) { ret = svf.VrsOut("SCHOOL_ADDRESS2", rs.getString("SCHOOLADDR2_ENG")); }  // 学校住所
                if (rs.getString("SCHOOLTELNO")!=null) { ret = svf.VrsOut("PHONE", rs.getString("SCHOOLTELNO")); }  // 学校電話番号
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 在学証明書(英)が基準となる個人情報の出力処理クラス
     */
    class Private_Regist_EN_H extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            try {
//                ret = svf.VrsOut("NUMBER"   , number);                                              //証明書番号
                ret = svf.VrsOut("GRADE"    , String.valueOf(rs.getInt("ANNUAL")));                 //年次
                ret = svf.VrsOut("NAME"     , rs.getString("NAME_ENG"));                            //氏名
                ret = svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_US(rs.getString("BIRTHDAY")));  //生年月日
                ret = svf.VrsOut("SEX"      , rs.getString("SEX_ENG"));                             //性別
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 在籍証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Registered_JP_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            byte check_len[] = new byte[24];
            try {
                if (rs.getString("NAME") != null){
                    check_len  = (rs.getString("NAME")).getBytes("MS932");
                }
                if (check_len.length > 24){
                    ret = svf.VrsOut( "NAME2",       rs.getString("NAME") );                                         //氏名
                }else {
                    ret = svf.VrsOut( "NAME1",       rs.getString("NAME") );                                         //氏名
                }
                if (_seirekiFlg && null != rs.getString("BIRTHDAY")) {
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")) + "生");  // 証明日付
                } else {
                    svf.VrsOut("BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //生年月日
                }

                if (_seirekiFlg && null != rs.getString("ENT_DATE")) {
                    svf.VrsOut("ENT_DATE", rs.getString("ENT_DATE").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("ENT_DATE")));
                } else {
                    svf.VrsOut("ENT_DATE",   KNJ_EditDate.h_format_JP( rs.getString("ENT_DATE") ) );
                }
                ret = svf.VrsOut( "GRADE",      String.valueOf( rs.getInt("ANNUAL") ) );                     //学年
                ret = svf.VrsOut( "COURSE",     ( rs.getString("COURSENAME") != null )? rs.getString("COURSENAME") : "" ); //課程
                ret = svf.VrsOut( "SUBJECT",    ( rs.getString("MAJORNAME") != null )? rs.getString("MAJORNAME") : "" );   //学科
                ret = svf.VrsOut( "SDATE",      KNJ_EditDate.h_format_JP(rs.getString("ENT_DATE")));         //入学日
                ret = svf.VrsOut( "FDATE",      KNJ_EditDate.h_format_JP(rs.getString("GRADU_DATE")));       //卒業日
            } catch (UnsupportedEncodingException e) {
                 log.error("UnsupportedEncodingException", e);
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 修了証明書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Complete_JP_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            byte check_len[] = new byte[24];
            try {
                if (rs.getString("NAME") != null){
                    check_len  = (rs.getString("NAME")).getBytes("MS932");
                }
                if (check_len.length > 24) {
                    ret = svf.VrsOut("NAME2", rs.getString("NAME"));  // 氏名
                } else {
                    ret = svf.VrsOut("NAME1", rs.getString("NAME"));  // 氏名
                }
                if (_seirekiFlg && null != rs.getString("BIRTHDAY")) {
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")) + "生");  // 証明日付
                } else {
                    svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")));  // 生年月日
                }
                ret = svf.VrsOut("COURSE", (rs.getString("COURSENAME") != null)? rs.getString("COURSENAME") + "課程" : "");  // 課程
                ret = svf.VrsOut("MAJOR", (rs.getString("MAJORNAME") != null)? rs.getString("MAJORNAME") : "");  // 学科
                if (_private_schooldiv.equals("1")){
                    ret = svf.VrsOut("GRADE", String.valueOf(rs.getInt("ANNUAL")) + "年次");  // 学年
                } else {
                    ret = svf.VrsOut("GRADE", "第" + String.valueOf(rs.getInt("ANNUAL")) + "学年");  // 学年
                }
            } catch (UnsupportedEncodingException e) {
                 log.error("UnsupportedEncodingException", e);
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 除籍通知書(和)が基準となる個人情報の出力処理クラス
     */
    class Private_Expel_JP_T extends Private_Common {

        /* 
         * 個人情報の出力
         */
        void printInfo (String param[]) {
            int ret = 0;
            if (0 == ret) ret = 0;
            byte check_len3[] = new byte[24];
            try {
                if (rs.getString("NAME") != null){
                    check_len3  = (rs.getString("NAME")).getBytes("MS932");
                }
                if (check_len3.length > 24){
                    ret = svf.VrsOut( "NAME2",       rs.getString("NAME") );                                         //氏名
                }else {
                    ret = svf.VrsOut( "NAME1",       rs.getString("NAME") );                                         //氏名
                }
                ret = svf.VrsOut( "SCHREGNO",   param[0] );  //学籍番号
                if (_seirekiFlg && null != rs.getString("BIRTHDAY")) {
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")) + "生");  // 証明日付
                } else {
                    svf.VrsOut("BIRTHDAY",   KNJ_EditDate.h_format_JP_Bth( rs.getString("BIRTHDAY") ) );  //生年月日
                }
                ret = svf.VrsOut( "GRADE",      String.valueOf( rs.getInt("ANNUAL") ) );                     //学年
                ret = svf.VrsOut( "COURSE",     ( rs.getString("COURSENAME") != null )? rs.getString("COURSENAME") : "" ); //課程
                ret = svf.VrsOut( "SUBJECT",    ( rs.getString("MAJORNAME") != null )? rs.getString("MAJORNAME") : "" );   //学科

                byte check_len[] = new byte[40];
                byte check_len2[] = new byte[40];
                if (rs.getString("ADDR1") != null){
                    check_len  = (rs.getString("ADDR1")).getBytes("MS932");
                }
                if (rs.getString("ADDR2") != null){
                    check_len2 = (rs.getString("ADDR2")).getBytes("MS932");
                }
                if (check_len.length > 40 || check_len2.length > 40){
                    ret = svf.VrsOut( "ADDRESS1_2", ( rs.getString("ADDR1") != null )? rs.getString("ADDR1") : "" );   //住所1
                    ret = svf.VrsOut( "ADDRESS2_2", ( rs.getString("ADDR2") != null )? rs.getString("ADDR2") : "" );   //住所2
                }else {
                    ret = svf.VrsOut( "ADDRESS1_1", ( rs.getString("ADDR1") != null )? rs.getString("ADDR1") : "" );   //住所1
                    ret = svf.VrsOut( "ADDRESS2_1", ( rs.getString("ADDR2") != null )? rs.getString("ADDR2") : "" );   //住所2
                }
            } catch (UnsupportedEncodingException e) {
                log.error("UnsupportedEncodingException", e);
            } catch (SQLException e) {
                log.error("SQLException", e);
            }
        }
    }
}    
