// kanji=漢字
/*
 * $Id: aeac1d2828954a59ce8dc6e2dc72cd7674121746 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.File;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 *
 *          在学証明書(日本語・英語）
 *          卒業見込証明書
 *          卒業証明書(日本語・英語）
 *
 *  2005/04/06 yamashiro 在学証明書(高校用)
 *  2005/04/12 yamashiro 在学証明書(高校用)
 *  2005/04/14 yamashiro 中学用在学証明書を追加
 *  2005/05/12 yamashiro 在学証明書(中学用)
 *  2005/07/12 yamashiro 卒業見込証明書において、氏名は文字数により文字の大きさを変えて出力する
 *  2005/07/21 yamashiro 標準版と近大付属版の異なる出力様式に対応 => 在学証明書(高校用)/卒業見込証明書/卒業証明書
 *  2005/08/04 yamashiro 近大付属版卒業見込証明書の出力仕様変更
 *  2005/08/31 yamashiro 近大付属版卒業証明書・卒業見込証明書・在学証明書の校長名出力仕様変更
 *                       近大付属中学用卒業証明書・卒業見込証明書・在学証明書を追加
 *  2005/10/05 yamashiro 卒業証明書において、学校名を現在年度で取得
 *  2005/11/18〜11/22 yamashiro 「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *                       学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

 /*
  *  2005/07/21 Modify KNJGCertificateの継承を追加 => インスタンスKNJDefineCodeの読み込みおよびインスタンス変数definecodeの設定
  */

public class KNJG010_1 {

    private static final Log log = LogFactory.getLog(KNJG010_1.class);
    public Vrw32alp svf = new Vrw32alp();       //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    public DB2UDB db2;                          //Databaseクラスを継承したクラス
    public boolean nonedata;
    protected PreparedStatement ps6,ps7;
    private KNJEditString knjobj = new KNJEditString();     //各学校における定数等設定 05/07/12 Build
    final KNJDefineSchool _definecode;  // 各学校における定数等設定
    private boolean _seirekiFlg = false;
    final String PRINCIPAL = " PRINCIPAL, ";
    final String PRINCIPALUNNUN = " PRINCIPAL, KINDAI UNIVERSITY HIGH SCHOOL";
    private Map _schoolMstYearMap = new HashMap();
    private Map _certifSchoolDatYearCertifKindMap = new HashMap();

    public KNJG010_1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final KNJDefineSchool definecode
    ){
        this.db2 = db2;
        this.svf = svf;
        nonedata = false;
        _definecode = definecode;
        log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
        setSeirekiFlg();
        setSchoolMst();
    }

    /**
     *  PrepareStatement作成
     *  2005/07/21 Modify 'オブジェクトがNULLの場合作成する'条件を追加
     */
    public void pre_stat(final String ptype) {
        try {
            //個人データ
            if (ps6 == null) {
                String ps6sql = new KNJ_PersonalinfoSql().sql_info_reg("1111001111");
                ps6 = db2.prepareStatement(ps6sql);
            }
            //学校データ
            if (ps7 == null) {
                String ps7sql;
                if (_definecode.schoolmark.equals("KIN") || _definecode.schoolmark.equals("KINJUNIOR")) {
                    ps7sql = new servletpack.KNJZ.detail.KNJ_SchoolinfoSql("12100").pre_sql();
                } else {
                    ps7sql = new servletpack.KNJG.detail.KNJ_SchoolinfoSql("12100").pre_sql();
                }
                ps7 = db2.prepareStatement(ps7sql);
            }
        } catch(Exception e) {
            log.error("pre_stat error! ", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("USE KNJG010_1");
        }
    }


    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        DbUtils.closeQuietly(ps6);
        DbUtils.closeQuietly(ps7);
    }


    /**
     *  過卒生対応年度取得
     */
    public static String b_year(final String pdate) {
        String b_year = null;

        try {
            if( pdate != null) {
                b_year = pdate.substring(0, 4);
                String b_month = pdate.substring(5, 7);
                if (b_month.equals("01") || b_month.equals("02") || b_month.equals("03")) {
                    b_year = String.valueOf(Integer.parseInt(b_year) - 1);
                }
            }
        } catch (Exception e) {
            log.error("b_year error! ", e);
        }
        if (b_year == null) {
            b_year = "";
        }
        return b_year;
    }
    
    private void setSchoolMst() {
        final String sql = "SELECT * FROM SCHOOL_MST ";
        _schoolMstYearMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, sql), "YEAR");
    }
    
    private void setSeirekiFlg() {
        final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
        _seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
    }
    
    public void printSvfMain(final int paper, final String schregno, final String year, final String year2, final String semester, final String date, final String certifNumber, final Map paramap) {
        
        final Param param = new Param(db2, paper, schregno, year, year2, semester, date, certifNumber, paramap);
        
        if (paper == 1 || paper == 22 || paper == 23) {
            //卒業証明書
            certif1_out(param);
        } else if (paper == 2 || paper == 19) {
            //卒業証明書（英語）
            certif2_out(param);
        } else if (paper == 3) {
            //卒業見込証明書
            certif3_out(param);
        } else if (paper == 4 || paper == 12 || paper == 24) {
            //在学証明書(日本語)
            certif4_out(param);
        } else if (paper == 5) {
            //在学証明書(英語)
            certif5_out(param);
        } else if (paper == 31) {
            //在寮証明書
            certif31_out(param);
        } 
    }
    
    private List ps6List(final Param param) {
        return KnjDbUtils.query(db2, ps6, new Object[] {param._schregno, param._year, param._semester, param._schregno, param._year});
    }
    
    private List ps7List(final Param param) {
        return KnjDbUtils.query(db2, ps7, new Object[] {param._year2, param._year2, "00000000", param._year});
    }

    /**
     * KNJG050_BASEからコール
     * @param schregno
     * @param year
     * @param year2
     * @param semester
     * @param date
     * @param certifNumber
     */
    public void certif1_out(final String schregno, final String year, final String year2, final String semester, final String date, final String certifNumber) {
        final Param param = new Param(db2, 1, schregno, year, year2, semester, date, certifNumber, new HashMap());
        certif1_out(param);
    }

    /** 
     *  卒業証明書 
     *  2005/01/17 近大付属高校用のフォームレイアウトとする
     *  2005/01/19 学科課程の表示に’課程’を入れる
     *  2005/01/27 FormにおいてFieldの変更に伴い修士
     *  2005/07/21 Modify 標準版と近大付属版の異なる出力様式に対応
     **/
    public void certif1_out(Param param) {
        setCertifForm(svf, 1);

        final List ps7List = ps7List(param);
        if (!ps7List.isEmpty()) {
            outGraduateSchoolinfo(svf, KnjDbUtils.firstRow(ps7List), param._date);
        }

        final List ps6List = ps6List(param);
        if (!ps6List.isEmpty()) {
            outGraduateStudentinfo(svf, KnjDbUtils.firstRow(ps6List), param._certifNumber);
            svf.VrEndPage();
            nonedata = true;
        }
    }

    /** 
     *  卒業証明書（英語） 
     */
    public void certif2_out(final Param param) {
        try {
            if (param._paper == 19) {
                setCertifForm(svf, 6);
            } else {
                setCertifForm(svf, 2);
            }

            final List ps7List = ps7List(param);
            String schoolnameEng = null;
            if (!ps7List.isEmpty()) {
                final Map rs = KnjDbUtils.firstRow(ps7List);
                svf.VrsOut("DATE", (param._date == null) ? "" : KNJ_EditDate.h_format_UK(param._date, "MMMM"));    //証明日付 05/11/18 Modify
                schoolnameEng = KnjDbUtils.getString(rs, "SCHOOLNAME_ENG");
                svf.VrsOut("SCHOOLNAME1"  , schoolnameEng);                  //学校名称
                svf.VrsOut("SCHOOLNAME2"  , PRINCIPAL + KnjDbUtils.getString(rs, "SCHOOLNAME_ENG"));                  //学校名称
                svf.VrsOut("STAFFNAME"    , StringUtils.upperCase(KnjDbUtils.getString(rs, "PRINCIPAL_NAME_ENG")));              //代表名
                
                svf.VrsOut("SCHOOLADDRESS1", KnjDbUtils.getString(rs, "SCHOOLADDR1_ENG"));                               //学校住所
                svf.VrsOut("SCHOOLADDRESS2", KnjDbUtils.getString(rs, "SCHOOLADDR2_ENG"));                               //学校住所
                
                if (null != param._principalSignatureImage) {
                    svf.VrsOut("SIGNATURE", param._principalSignatureImage);
                }
            }

            final Map schoolMst = (Map) _schoolMstYearMap.get(param._year2);
            if (null != schoolMst) {
                svf.VrsOut("PHONE", (String) schoolMst.get("SCHOOLTELNO"));
                svf.VrsOut("FAX", (String) schoolMst.get("SCHOOLFAXNO"));
            }

            svf.VrsOut("JOBNAME1", " ");
            svf.VrsOut("JOBNAME2", PRINCIPALUNNUN);

            final List ps6List = ps6List(param);
            String graduDate = null;
            boolean hasps6 = false;
            if (!ps6List.isEmpty()) {
                final Map rs = KnjDbUtils.firstRow(ps6List);
                graduDate = KnjDbUtils.getString(rs, "GRADU_DATE");
                svf.VrsOut("COURSE"   , KnjDbUtils.getString(rs, "MAJORENG"));
                svf.VrsOut("NUMBER"   , param._certifNumber);                                              //証明書番号
                svf.VrsOut("NAME"     , KnjDbUtils.getString(rs, "NAME_ENG"));                            //氏名
                svf.VrsOut("SEX"      , KnjDbUtils.getString(rs, "SEX_ENG"));                             //性別
                svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_UK(KnjDbUtils.getString(rs, "BIRTHDAY"), "MMMM"));      //生年月日
                svf.VrsOut("YEAR_S"   , h_format_US(KnjDbUtils.getString(rs, "ENT_DATE"), "MMMM,yyyy"));    //入学日
                svf.VrsOut("YEAR_F"   , h_format_US(graduDate, "MMMM,yyyy"));  //卒業日
                hasps6 = true;
            }

            if (param._paper == 19) {
                svf.VrsOut("TEXT1", "　We hereby certify that the student named above was admitted");
                svf.VrsOut("TEXT2", "to " + StringUtils.defaultString(schoolnameEng) + " and is expected to complete");
                svf.VrsOut("TEXT3", "the said course on coming " + h_format_US(graduDate, "MMMM") + ".");
            } else {
                svf.VrsOut("TEXT1", "We hereby certify that the student named above was admitted");
                svf.VrsOut("TEXT2", "to " + StringUtils.defaultString(schoolnameEng) + " and completed the said course.");
            }
            if (hasps6) {
                nonedata = true;
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.error("certif2_out error! " , ex);
        }

    }//certif2_outの括り

    /** 
     *  卒業見込証明書 
     *  2005/07/12 Modify 氏名を文字数により文字の大きさを変えて出力する処理を追加
     *                    nullデータの処理を追加
     *  2005/07/21 Modify 標準版と近大付属版の異なる出力様式に対応
     *  2005/08/04 Modify 課程・学科名を出力 => 卒業証明書と同様とする
     *  2005/08/31 Modify 近大付属版の校長名等の出力様式を変更 卒業証明書と同様とする
     **/
    public void certif3_out(final Param param) {
        setCertifForm(svf, 3);

        final List ps7List = ps7List(param);
        if (!ps7List.isEmpty()) {
            outGraduateSchoolinfo(svf, KnjDbUtils.firstRow(ps7List), param._date );   //05/08/31Build
        }
        final List ps6List = ps6List(param);
        if (!ps6List.isEmpty()) {
            final Vrw32alp svf1 = svf;
            outGraduateStudentinfo(svf1, KnjDbUtils.firstRow(ps6List), param._certifNumber);  // 卒業証明書の生徒項目  //05/08/04
            svf.VrEndPage();
            nonedata = true;
        }
    }

    
    /** 
     *  在学証明書(日本語) 
     *  2005/04/06 Modify レイアウト変更に因る
     *  2005/04/12 Modify 氏名出力仕様変更に因る
     *  2005/07/21 Modify 標準版と近大付属版の異なる出力様式に対応
     */
    public void certif4_out(final Param param) {
        setCertifForm(svf, 4);

        String schooldivname = null;
        final List ps7List = ps7List(param);
        if (!ps7List.isEmpty()) {
            final Map rs = KnjDbUtils.firstRow(ps7List);
            outRegistSchoolinfo(svf, rs, param._date);
            if (KnjDbUtils.getString(rs, "T4SCHOOLDIV").equals("0")) {
                schooldivname = "学年";  //05/11/18Modify
            } else {
                schooldivname = "年次";
            }
        }
        if (schooldivname == null) {
            schooldivname = "";    //05/04/14
        }

        final List ps6List = ps6List(param);
        if (!ps6List.isEmpty()) {
            final Map rs = KnjDbUtils.firstRow(ps6List);
            outRegistStudentinfo(svf, rs, param._certifNumber, schooldivname);
            svf.VrEndPage();
            nonedata = true;
        }
    }

    /** 
     *  在学証明書(英語) 
     */
    public void certif5_out(final Param param) {
        setCertifForm(svf, 5);

        final List ps7List = ps7List(param);
        String schoolnameEng = null;
        if (!ps7List.isEmpty()) {
            final Map rs = KnjDbUtils.firstRow(ps7List);
            
//            svf.VrsOut("SYOSYO_NAME", null); // 証書名
//            svf.VrsOut("SYOSYO_NAME2", null); // 年度、学校略称

            svf.VrsOut("DATE", ( param._date != null ) ? KNJ_EditDate.h_format_UK(param._date, "MMMM") : ""); //証明日付 05/11/18 Modify
            schoolnameEng = KnjDbUtils.getString(rs, "SCHOOLNAME_ENG");
            svf.VrsOut("SCHOOLNAME1"  , KnjDbUtils.getString(rs, "SCHOOLNAME_ENG"));                  //学校名称
            svf.VrsOut("SCHOOLNAME2"  , PRINCIPAL + KnjDbUtils.getString(rs, "SCHOOLNAME_ENG"));                  //学校名称

            svf.VrsOut("STAFFNAME", StringUtils.upperCase(KnjDbUtils.getString(rs, "PRINCIPAL_NAME_ENG")));  //代表名
//            String address = KnjDbUtils.getString(rs, "SCHOOLADDR1_ENG");
//            if (KnjDbUtils.getString(rs, "SCHOOLADDR2_ENG") != null) address += KnjDbUtils.getString(rs, "SCHOOLADDR2_ENG");
//            svf.VrsOut("SCHOOL_ADDRESS1", address);                               //学校住所
            
            svf.VrsOut("SCHOOLADDRESS1", KnjDbUtils.getString(rs, "SCHOOLADDR1_ENG"));                               //学校住所
            svf.VrsOut("SCHOOLADDRESS2", KnjDbUtils.getString(rs, "SCHOOLADDR2_ENG"));                               //学校住所

            svf.VrsOut("PHONE", KnjDbUtils.getString(rs, "SCHOOLTELNO"));         //学校電話番号
            
            if (null != param._principalSignatureImage) {
                svf.VrsOut("SIGNATURE", param._principalSignatureImage);
            }
        }

        final List ps6List = ps6List(param);
        if (!ps6List.isEmpty()) {
            final Map rs = KnjDbUtils.firstRow(ps6List);
            
            svf.VrsOut("COURSE"   , KnjDbUtils.getString(rs, "MAJORENG"));
            
            svf.VrsOut("YEAR_S"   , h_format_US(KnjDbUtils.getString(rs, "ENT_DATE"), "MMMM,yyyy"));    //入学日
            svf.VrsOut("YEAR_F"   , h_format_US(KnjDbUtils.getString(rs, "GRADU_DATE"), "MMMM,yyyy"));  //卒業日

            final String courseeng = StringUtils.lowerCase(StringUtils.defaultString(KnjDbUtils.getString(rs, "COURSEENG")));
            final String majoreng = StringUtils.defaultString(KnjDbUtils.getString(rs, "MAJORENG"));
            Integer a = KnjDbUtils.getInt(rs, "ANNUAL", null);
            String grade = " ";
            if (null != a) {
                grade = String.valueOf(a.intValue() + 9);
            }

            svf.VrsOut("TEXT1", " This is certify that the above-mentioned student is enrolled");
            svf.VrsOut("TEXT2", "as a " + grade + "th year student in the " + courseeng + " " + majoreng + " at");
            svf.VrsOut("TEXT3", WordUtils.capitalize(StringUtils.defaultString(schoolnameEng).toLowerCase()) + ".");
            
            svf.VrsOut("CERTIF_NO"   , param._certifNumber);                                              //証明書番号
            svf.VrsOut("NAME"     , KnjDbUtils.getString(rs, "NAME_ENG"));                            //氏名
            svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_UK(KnjDbUtils.getString(rs, "BIRTHDAY"), "MMMM"));  //生年月日
            svf.VrsOut("SEX"      , KnjDbUtils.getString(rs, "SEX_ENG"));                             //性別
            svf.VrEndPage();
            nonedata = true;
        }
    }  //certif5_outの括り

    /** 
     *  在学証明書(中学用) 
     *  2005/04/14 yamashiro
     *  2005/05/12 yamashiro 学年の前スペースを削除
     *
     */
    public void certif12_out(final Param param) {

        try {
            final List ps7List = ps7List(param);

            String schooldivname = null;
            if (!ps7List.isEmpty()) {
                final Map rs = KnjDbUtils.firstRow(ps7List);
                printsvfDate(svf, param._date, "DATE" );  //05/11/18 処理日の出力
                svf.VrsOut("SCHOOLNAME"   , KnjDbUtils.getString(rs, "SCHOOLNAME1"));         //学校名称
                String pname = null;
                if (KnjDbUtils.getString(rs, "PRINCIPAL_JOBNAME") != null) {
                    pname = KnjDbUtils.getString(rs, "PRINCIPAL_JOBNAME");
                }
                if (KnjDbUtils.getString(rs, "PRINCIPAL_NAME") != null) {
                    pname = pname + "  " + KnjDbUtils.getString(rs, "PRINCIPAL_NAME");
                }
                svf.VrsOut("STAFFNAME"  , pname);                                 //職名・校長名
                if (KnjDbUtils.getString(rs, "T4SCHOOLDIV").equals("0")) {
                    schooldivname = "学年";   //05/11/18 Modify
                } else {
                    schooldivname = "年次";
                }
            }
            if (schooldivname == null) {
                schooldivname = "";    //05/04/14
            }

            final List ps6List = ps6List(param);

            if (!ps6List.isEmpty()) {
                final Map rs = KnjDbUtils.firstRow(ps6List);
                svf.VrsOut( "NUMBER",  param._certifNumber);                                                     //証明書番号
                final Integer a = KnjDbUtils.getInt(rs, "ANNUAL", null);
                if (null != a) {
                    svf.VrsOut( "GRADE",   "第" + a.toString() + schooldivname);               //05/04/14Modify 05/05/12Modify
                }
                svf.VrsOut( "COURSE",  StringUtils.defaultString(KnjDbUtils.getString(rs, "COURSENAME")));//05/04/14Modify
                svf.VrsOut( "SUBJECT", StringUtils.defaultString(KnjDbUtils.getString(rs, "MAJORNAME")));//05/04/14Modify

                final String name = "1".equals(KnjDbUtils.getString(rs, "USE_REAL_NAME")) ? KnjDbUtils.getString(rs, "REAL_NAME") : KnjDbUtils.getString(rs, "NAME");
                if (name != null  &&  12 < name.length()) {     //05/04/12Modify
                    svf.VrsOut("NAME2", name);                          //氏名
                } else {
                    svf.VrsOut("NAME1", name);                          //氏名
                }
                svf.VrsOut("BIRTHDAY" , KNJ_EditDate.h_format_JP_Bth(KnjDbUtils.getString(rs, "BIRTHDAY")));   //生年月日
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.debug("certif12 error! " , ex );
        }
    }

    /** 
     *  在寮証明書
     */
    public void certif31_out(final Param param) {
        svf.VrSetForm("KNJG031.frm", 1);
        final List ps7List = ps7List(param);
        if(!ps7List.isEmpty()) {
            outRegistSchoolinfo(svf, KnjDbUtils.firstRow(ps7List), param._date);
        }

        final List ps6List = ps6List(param);
        if (!ps6List.isEmpty()) {
            final Map rs = KnjDbUtils.firstRow(ps6List);
            if (!"1".equals(KnjDbUtils.getString(rs, "IN_DORMITORY"))) { // 非入寮者
                nonedata = false;
                return;
            }
            
            svf.VrsOut("HRNAME", KnjDbUtils.getString(rs, "HR_NAME"));

            final String name = "1".equals(KnjDbUtils.getString(rs, "USE_REAL_NAME")) ? KnjDbUtils.getString(rs, "REAL_NAME") : KnjDbUtils.getString(rs, "NAME");
            svf.VrsOut(KNJ_EditKinsoku.getMS932ByteCount(name) > 24 ? "NAME2" : "NAME1", name); // 氏名
            if (null != KnjDbUtils.getString(rs, "BIRTHDAY")) {
                svf.VrsOut("BIRTHDAY",   getBirthdayFormattedString(KnjDbUtils.getString(rs, "BIRTHDAY"), KnjDbUtils.getString(rs, "BIRTHDAY_FLG")));  //生年月日
            }
            
            svf.VrsOut("MAIN1", "上記の者は本校" + StringUtils.defaultString(KnjDbUtils.getString(rs, "DORMITORY_NAME")));
            svf.VrsOut("MAIN2", "所に在寮中のものであること");
            svf.VrsOut("MAIN3", "を証明する");

            if (null != KnjDbUtils.getString(rs, "ENT_DATE")) {
                final String entDate;
                if (_seirekiFlg) {
                    entDate = KnjDbUtils.getString(rs, "ENT_DATE").substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(KnjDbUtils.getString(rs, "ENT_DATE"));
                } else {
                    entDate = KNJ_EditDate.h_format_JP(KnjDbUtils.getString(rs, "ENT_DATE"));
                }
                svf.VrsOut("ENT_DATE", entDate);
            }

            svf.VrEndPage();
            nonedata = true;
        }
    }  //certif31_outの括り


    /*----------------------------------------------------------------------------------------------*
     * 日付の編集(米国式)
     * ※使い方
     *   String dat = h_format_US("2002-10-27")     :Apr 2002
     *   String dat = h_format_US("2002/10/27")     :Apr 2002
     *----------------------------------------------------------------------------------------------*/
    public static String h_format_US(final String strx, final String format) {

        String hdate = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse(strx);
            } catch (Exception e) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse(strx);
                } catch (Exception e2) {
                    hdate = "";
                    return hdate;
                }
            }
            Locale local = new Locale("en","US");
            hdate = new SimpleDateFormat(format, local).format(dat);
        } catch (Exception e3) {
            hdate = "";
        }

        return hdate;

    }//String h_format_US_Mの括り

    /** 
     *  処理日の出力
     *  2005/11/18 Build
     */
    private void printsvfDate(final Vrw32alp svf, final String date, final String fname) {
        try {
            if (date != null) {
                svf.VrsOut(fname, KNJ_EditDate.h_format_JP(date)); //証明日付
            } else {
                svf.VrAttribute(fname, "Hensyu=1" );    //05/11/22
                svf.VrsOut(fname, "年　 月　 日" ); //05/11/22
            }
        } catch (Exception ex ) {
            log.error("error! " , ex);
        }
    }
    
    private String getBirthdayFormattedString(final String rsBirthday, final String birthdayFlg) {
        final String birthday;
        if (_seirekiFlg || (!_seirekiFlg && "1".equals(birthdayFlg))) {
            birthday = rsBirthday.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(rsBirthday);
        } else {
            birthday = KNJ_EditDate.h_format_JP(rsBirthday);
        }
        return birthday;
    }

    /*
     *  [近大付属用様式] 卒業証明書の学校項目を出力
     *  2005/08/31 Modify 校長名を'X X(姓)   X X(名)'と編集して出力
     */
    private void outGraduateSchoolinfo(
            final Vrw32alp svf,
            final Map rs,
            final String date
    ) {
        printsvfDate(svf, date, "CERTIFDATE");  //05/11/18 処理日の出力
        final String principalJobname = KnjDbUtils.getString(rs, "PRINCIPAL_JOBNAME");
        final String principalName = KnjDbUtils.getString(rs, "PRINCIPAL_NAME");
        if (principalJobname != null ) {
            svf.VrsOut( "STAFFJOBNAME", principalJobname);
        }
        if (principalName != null ) {
            svf.VrsOut( "STAFFNAME", editName(principalName));    //校長名
        }
    }

    /*
     *  [近大付属用様式] 卒業証明書の生徒項目を出力
     */
    private void outGraduateStudentinfo(
            final Vrw32alp svf,
            final Map rs,
            final String number
    ) {
        if (number != null) {
            svf.VrsOut("CERTIFNO", number);  // 証明書番号
        }
        outStudentname(svf, rs);  // 生徒名
        if (KnjDbUtils.getString(rs, "BIRTHDAY") != null ) svf.VrsOut("BIRTHDAY", getBirthdayFormattedString(KnjDbUtils.getString(rs, "BIRTHDAY"), KnjDbUtils.getString(rs, "BIRTHDAY_FLG")));  // 生年月日
        if (KnjDbUtils.getString(rs, "GRADU_DATE") != null ) svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_M(KnjDbUtils.getString(rs, "GRADU_DATE")));  // 卒業年月日
        if (KnjDbUtils.getString(rs, "COURSENAME") != null ) svf.VrsOut("COURSE", KnjDbUtils.getString(rs, "COURSENAME"));
        if (KnjDbUtils.getString(rs, "MAJORNAME") != null ) svf.VrsOut("MAJOR", KnjDbUtils.getString(rs, "MAJORNAME"));
    }

    /*
     *  [近大付属用様式] 在学証明書の学校項目を出力
     *  2005/08/31 Modify 校長名を'X X(姓)   X X(名)'と編集して出力
     */
    private void outRegistSchoolinfo(
            final Vrw32alp svf,
            final Map rs,
            final String date
    ) {
        printsvfDate(svf, date, "DATE");  // 処理日の出力
        try {
            svf.VrsOut("SCHOOLNAME", KnjDbUtils.getString(rs, "SCHOOLNAME1"));  // 学校名称
            if (KnjDbUtils.getString(rs, "PRINCIPAL_JOBNAME") != null) {
                svf.VrsOut( "STAFFJOBNAME", KnjDbUtils.getString(rs, "PRINCIPAL_JOBNAME"));
            }
            if (KnjDbUtils.getString(rs, "PRINCIPAL_NAME") != null) {
                svf.VrsOut("STAFFNAME", editName( KnjDbUtils.getString(rs, "PRINCIPAL_NAME")));
            }  // 校長名
        } catch (Exception e) {
             log.error("Exception", e);
        }
    }

    /*
     *  [近大付属用様式] 在学証明書の生徒項目を出力
     */
    private void outRegistStudentinfo(
            final Vrw32alp svf,
            final Map rs,
            final String number,
            final String schooldivname
    ) {
        svf.VrsOut("NUMBER", number);  // 証明書番号
        final Integer a = KnjDbUtils.getInt(rs, "ANNUAL", null);
        if (null != a) {
            svf.VrsOut("GRADE", "  第" + a.toString() + schooldivname);
        }
        svf.VrsOut("COURSE", StringUtils.defaultString(KnjDbUtils.getString(rs, "COURSENAME")));
        svf.VrsOut("SUBJECT", StringUtils.defaultString(KnjDbUtils.getString(rs, "MAJORNAME")));
        outStudentname(svf, rs);   //生徒名
        svf.VrsOut("BIRTHDAY" , getBirthdayFormattedString(KnjDbUtils.getString(rs, "BIRTHDAY"), KnjDbUtils.getString(rs, "BIRTHDAY_FLG")));  // 生年月日
    }

    /*
     *  [近大付属用様式] 生徒名を出力
     */
    private void outStudentname(final Vrw32alp svf, final Map rs) {
        final String name = "1".equals(KnjDbUtils.getString(rs, "USE_REAL_NAME")) ? KnjDbUtils.getString(rs, "REAL_NAME") : KnjDbUtils.getString(rs, "NAME");
        if (name != null) {
            if (24 < KNJ_EditKinsoku.getMS932ByteCount(name)) {
                svf.VrsOut("NAME2", name);
            } else {
                svf.VrsOut("NAME1", name);
            }
        }
    }

    /*
     *  [近大付属用様式] 校長名を編集
     */
    private String editName(final String name) {
        final StringBuffer stb = new StringBuffer();
        if (null != name) {
            final char chr[] = name.toCharArray();
            boolean boo = false;
            for (int i = 0 ; i < chr.length ; i++) {
                if (chr[i] == (' ')  ||  chr[i] == ('　')) {
                    if (!boo && 0 < i ) {
                        stb.append(" ");
                        boo = true;
                    }
                    continue;
                }
                if (0 < i) {
                    stb.append("  ");
                }
                stb.append(chr[i]);
            }
        }
        return stb.toString();
    }

    /*
     *  [近大付属用様式]フォーム設定 高校用・中学用
     *  引数　1:卒業証明書 3:卒業見込証明書 4:在学証明書
     */
    private void setCertifForm(final Vrw32alp svf, final int i) {
        final String form;
        if (_definecode.schoolmark.equals("KINJUNIOR")) {
            form = "KNJG010_" + i + "JKIN.frm";
        } else {
            form = "KNJG010_" + i + "KIN.frm";
        }
        //log.debug("schoolmark=" + _definecode.schoolmark);
        log.info(" form = " + form);
        svf.VrSetForm(form, 1);
    }
    
    private static class Param {
        final int _paper;
        final String _schregno;
        final String _year;
        final String _year2;
        final String _semester;
        final String _date;
        final String _certifNumber;
        final Map _paramap;

        final String DOCUMENTROOT;
        final Map _controlMstMap;
        final String _principalSignatureImage;
        
        Param(  final DB2UDB db2,
                final int paper,
                final String schregno,
                final String year,
                final String year2,
                final String semester,
                final String date,
                final String certifNumber,
                final Map paramap
                ) {
            _paper = paper;
            _schregno = schregno;
            _year = year;
            _year2 = year2;
            _semester = semester;
            _date = date;
            _certifNumber = certifNumber;
            _paramap = paramap;
            DOCUMENTROOT = (String) _paramap.get("DOCUMENTROOT");
            _controlMstMap = getControlMst(db2);
            _principalSignatureImage = getImagePath("PRINCIPAL_SIGNATURE_H", "jpg");
        }
        
        /**
         * @param _nameMstD001Map 設定する _nameMstD001Map。
         */
        private Map getControlMst(final DB2UDB db2) {
            final String sql = "SELECT CTRL_YEAR, CTRL_SEMESTER, CTRL_DATE, IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
        }

        private String getImagePath(final String filename, final String ext) {
            final String imageDir = KnjDbUtils.getString(_controlMstMap, "IMAGEPATH");
            final String imageExt = null != ext ? ext : KnjDbUtils.getString(_controlMstMap, "EXTENSION");
            if (null == DOCUMENTROOT) {
                log.warn(" documentroot null.");
                return null;
            } // DOCUMENTROOT
            if (null == imageDir) {
                log.warn(" imageDir null.");
                return null;
            }
            if (null == imageExt) {
                log.warn(" imageExt null.");
                return null;
            }
            if (null == filename) {
                log.warn(" filename null.");
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(DOCUMENTROOT);
            stb.append("/");
            stb.append(imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(imageExt);
            final File file = new File(stb.toString());
            log.warn("image file:" + file.getAbsolutePath() + " exists? " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return stb.toString();
        }
    }
}
