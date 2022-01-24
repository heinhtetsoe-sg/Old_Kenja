package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４１＞  身分証明書（近大）
 *
 *  2005/03/25 nakamoto 作成日
 *  2005/03/29 nakamoto ふりがな(名)と漢字(名)の表示開始位置を合わせる
 *  2005/03/30 nakamoto 仮身分証明書の通学証明書（下）の有効期限の終了日を
 *                      発行日の翌年 + "3月31日"に変更 ---NO001
 *  2005/03/31 nakamoto NO001を変更 ---NO002
 *                      ４〜１２月：発行日の翌年 + "3月31日"
 *                      １〜３月　：発行日の年　 + "3月31日"
 *                      仮身分証明書を出力するとき、留年フラグが’１’の生徒は、
 *                      学籍基礎データから氏名、学籍住所データから住所を表示 ---NO003
 *  2005/05/17 nakamoto 仮身分証明書を出力するとき、
 *                      １年生でかつ学籍基礎データにない場合のみ、新入生移行データから出力
 *                      それ以外は、学籍基礎データ・学籍住所データから出力---NO004
 *
 ****************************************************************************************************
 *
 *  2005/06/10 nakamoto (KNJA141H,KNJA141J)⇒KNJA141に変更(１本に統一)
 *                      仮身分証明書を学籍基礎から出力できるよう修正・・・仮身分証明書(在籍)を追加
 *  2005/07/15 nakamoto クラス指定を追加---NO005
 *  2007/05/11 nakamoto NO006:学校長印の".bmp"を".jpg"に変更した。
 *
 **/

public class KNJA141 {

    private static final Log log = LogFactory.getLog(KNJA141.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    private String _useAddrField2;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[15];//---NO001

        log.debug(" $Revision: 56595 $ ");
    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("GAKKI");                       //学期
            param[2] = request.getParameter("GRADE_HR_CLASS");              //学年＋組
            param[3] = request.getParameter("OUTPUT");                      //フォーム種別(1:仮身分証明書,2:身分証明書)
            String sdate = request.getParameter("TERM_SDATE");              //有効期限(開始)
            String edate = request.getParameter("TERM_EDATE");              //有効期限(終了)
            param[5] = sdate.replace('/','-');
            param[6] = edate.replace('/','-');
            param[7] = request.getParameter("DOCUMENTROOT");                // '/usr/local/deve_oomiya/src'
            // 学籍番号の指定---NO005
            param[14] = request.getParameter("DISP");                       //2:クラス,1:個人
            String classcd[] = request.getParameterValues("category_selected");
            _useAddrField2 = request.getParameter("useAddrField2");
            param[4] = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) param[4] = param[4] + ",";
                if (param[14].equals("2")) param[4] = param[4] + "'" + classcd[ia] + "'";
                if (param[14].equals("1")) param[4] = param[4] + "'" + (classcd[ia]).substring(0,(classcd[ia]).indexOf("-")) + "'";
            }
            param[4] = param[4] + ")";
/******************---NO005
            String schno[] = request.getParameterValues("category_selected");//学籍番号
            int i = 0;
            param[4] = "(";
            while(i < schno.length){
                if(schno[i] == null ) break;
                if(i > 0) param[4] = param[4] + ",";
                param[4] = param[4] + "'" + schno[i] + "'";
                i++;
            }
            param[4] = param[4] + ")";
*******************/
            param[12] = request.getParameter("SCHOOL_JUDGE");               //学校区分(J:中,H:高)---2005.06.10Modify
            //---NO001
            //int nen = Integer.parseInt(sdate.substring(0,4)) + 1;
            //---NO002
            int nen = Integer.parseInt(sdate.substring(0,4));
            int mon = Integer.parseInt(sdate.substring(5,7));
            if (mon > 3) nen = nen + 1;
            param[13] = String.valueOf(nen) + "-03-31";
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps3 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(preStat1(param));        //生徒情報
//          ps2 = db2.prepareStatement(preStat2(param));        //組名称---NO005
            ps3 = db2.prepareStatement(preStat3(param));        //学校名
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
//      setHrName(db2,svf,param,ps2);                           //組名称取得メソッド---NO005
        setSchoolName(db2,svf,param,ps3);                       //学校名取得メソッド
        if (param[3].equals("2")) { //身分証明書
            if( setSvfout(db2,svf,param,ps1) ) nonedata = true; //帳票出力のメソッド
//          if (param[3].equals("1")) //仮身分証明書
        }
        if (param[3].equals("1") || param[3].equals("3")) {//仮身分証明書---仮身分証明書(在籍)---2005.06.10Modify
            if( setSvfout2(db2,svf,param,ps1) ) nonedata = true;//帳票出力のメソッド
        }

log.debug("nonedata = "+nonedata);

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1,ps3);  //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

        if (param[3].equals("2")) svf.VrSetForm("KNJA141_1.frm", 4);//身分証明書
        else                      svf.VrSetForm("KNJA141_2.frm", 4);//仮身分証明書---2005.06.10Modify
//      if (param[3].equals("1")) svf.VrSetForm("KNJA141_2.frm", 4);//仮身分証明書

    //  写真データ
        try {
            returnval = getinfo.Control(db2);
            param[8] = returnval.val4;      //格納フォルダ
            param[9] = returnval.val5;      //拡張子
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;
    }


    /**組名称を取得---NO005未使用**/
    private void setHrName(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps2
    ) {
        try {
            ResultSet rs = ps2.executeQuery();

            String hr_nameabbv = "";
            String class_name = "";
            while( rs.next() ){
                //Ｊ組のみ表示変更
                hr_nameabbv = rs.getString("HR_NAMEABBV");
                if ((hr_nameabbv.substring(1,2)).equals("J") && param[12].equals("H")) {
                    class_name = hr_nameabbv.substring(0,2) + hr_nameabbv.substring(3);
                } else {
                    class_name = hr_nameabbv.substring(1);
                }

                param[10] = "第" + param[2].substring(1,2) + "学年" + class_name + "組";  //年組番
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setHrName set error!");
        }

    }


    /**組名称を取得---NO005追加**/
    private String getHrName( ResultSet rs, String param[] )
    {

        String ret_val = "";
//        String hr_nameabbv = "";
//        try {
//            if (rs.getString("HR_NAMEABBV") != null) {
//                //Ｊ組のみ表示変更
//                hr_nameabbv = rs.getString("HR_NAMEABBV");
//                if ((hr_nameabbv.substring(1,2)).equals("J") && param[12].equals("H")) {
//                    ret_val = hr_nameabbv.substring(0,2) + hr_nameabbv.substring(3);
//                } else {
//                    ret_val = hr_nameabbv.substring(1);
//                }
//
//                ret_val = "第" + String.valueOf(rs.getInt("GRADE")) + "学年" + ret_val + "組";  //年組
//            }
//        } catch( Exception ex ) {
//            log.error("getHrName error!");
//        }
        return ret_val;

    }


    /**学校名を取得**/
    private void setSchoolName(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps3
    ) {
        try {
            ResultSet rs = ps3.executeQuery();
            while( rs.next() ){
                param[11] = rs.getString("SCHOOLNAME1");    //学校名
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSchoolName set error!");
        }
    }


    /**身分証明書**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        try {
            ResultSet rs = ps1.executeQuery();

            //年齢--------------
            Calendar cals = Calendar.getInstance();
            Calendar calb = Calendar.getInstance();
            cals.setTime(sdf.parse( param[5] ));                //有効期限(開始)をCalendar calに変換
//            int cals_year = cals.get(Calendar.YEAR);            //calsの年を取得
//            int cals_day  = cals.get(Calendar.DAY_OF_YEAR);     //calsの年の何日目を取得
            String birth_age = "";                              //年齢

            //画像--------------
            String badge = "SCHOOLMARK"  + "." + param[9];              //校章
            String sname = "SCHOOLNAME" + param[12] + "." + param[9];   //学校名
            String photo = "";                                          //顔写真
            String stamp = "PRINCIPAL" + param[12] + ".jpg";            //学校長印---NO006 ".bmp"→".jpg"
            String photo_check = "";
            String stamp_check = param[7] + "/" + param[8] + "/" + stamp;
            String badge_check = param[7] + "/" + param[8] + "/" + badge;
            String sname_check = param[7] + "/" + param[8] + "/" + sname;
            File f2 = new File(stamp_check);    //学校長印データ存在チェック用
            File f3 = new File(badge_check);    //校章データ存在チェック用
            File f4 = new File(sname_check);    //学校名データ存在チェック用

            while( rs.next() ){
                //年齢--------------
                //生年月日・年齢
                if (rs.getString("BIRTHDAY") != null) {
                    calb.setTime(sdf.parse( rs.getString("BIRTHDAY") ));//生年月日をCalendar calに変換
//                    int calb_year = calb.get(Calendar.YEAR);            //calbの年を取得
//                    int calb_day  = calb.get(Calendar.DAY_OF_YEAR);     //calbの年の何日目を取得
//                    if (calb_day <= cals_day) birth_age = "　(" + String.valueOf(cals_year - calb_year) + "才)";
//                    else                      birth_age = "　(" + String.valueOf(cals_year - calb_year - 1) + "才)";
                    svf.VrsOut("BIRTHDAY" ,KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")) + birth_age );
                }
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_useAddrField2) &&
                        ((addr1 != null && addr1.length() > 25) || 
                         (addr2 != null && addr2.length() > 25)))
                {
                    svf.VrsOut("ADDRESS1_3" ,addr1 );  //住所１(60)
                    svf.VrsOut("ADDRESS2_3" ,addr2 );  //住所２(60)
                } else if ((addr1 != null && addr1.length() > 20) || 
                            (addr2 != null && addr2.length() > 20))
                {
                    //住所１または住所２が２０文字超えたら(50)にセット
                    svf.VrsOut("ADDRESS1_2" ,addr1 );  //住所１(50)
                    svf.VrsOut("ADDRESS2_2" ,addr2 );  //住所２(50)
                } else {
                    svf.VrsOut("ADDRESS1_1" ,addr1 );  //住所１(40)
                    svf.VrsOut("ADDRESS2_1" ,addr2 );  //住所２(40)
                }
                //画像--------------
                //顔写真
                photo = "P" + rs.getString("SCHREGNO") + "." + param[9];
                photo_check = param[7] + "/" + param[8] + "/" + photo;
                File f1 = new File(photo_check);//写真データ存在チェック用
                if (f1.exists()) 
                    svf.VrsOut("PHOTO"    , photo_check );//顔写真
                //学校長印
                if (f2.exists()) 
                    svf.VrsOut("STAMP"    , stamp_check );//学校長印
                //校章
                if (f3.exists()) 
                    svf.VrsOut("BITMAP1"  , badge_check );//校章
                //学校名
                if (f4.exists()) 
                    svf.VrsOut("BITMAP2"  , sname_check );//学校名
                //在籍--------------
                String coursename = (rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+"課程　" : "";
                String majorname  = (rs.getString("MAJORNAME") != null) ? rs.getString("MAJORNAME")+"　" : "";
                if (param[12].equals("H")) 
                    svf.VrsOut("CLASS" ,coursename + majorname + getHrName( rs, param ) );//年組---NO005
//                    svf.VrsOut("CLASS" ,coursename + majorname + param[10] );//年組
                if (param[12].equals("J")) 
                    svf.VrsOut("CLASS" ,getHrName( rs, param ) );//年組---NO005
//                    svf.VrsOut("CLASS" ,param[10] );//年組
                //その他--------------
                if (param[12].equals("H")) svf.VrsOut("SCHOOLDIV" ,"高等課程" );  //学校区分
                if (param[12].equals("J")) svf.VrsOut("SCHOOLDIV" ,"中等課程" );  //学校区分
                svf.VrsOut("SCHREGNO"     ,rs.getString("SCHREGNO") );            //学籍番号
                svf.VrsOut("SDATE"        ,KNJ_EditDate.h_format_JP(param[5]));   //有効期限(開始)
                svf.VrsOut("FDATE"        ,KNJ_EditDate.h_format_JP(param[6]));   //有効期限(終了)
                svf.VrsOut("SCHOOLNAME"   , param[11] + "長" );                   //学校長名
                svf.VrsOut("BARCODE"      ,rs.getString("SCHREGNO") );            //バーコード
                
                final String nameKana;
                final String name;
                if ("1".equals(rs.getString("USE_REAL_NAME"))) {
                    nameKana = rs.getString("REAL_NAME_KANA");
                    name = rs.getString("REAL_NAME");
                } else {
                    nameKana = rs.getString("NAME_KANA");
                    name = rs.getString("NAME");
                }
                
                svf.VrsOut("KANA"         ,nameKana );           //氏名(かな)
//  05/03/30 以前の仕様を有効とする             String name_kana = (rs.getString("NAME_KANA") != null && rs.getString("NAME") != null) ? setKanaName( rs.getString("NAME_KANA"), rs.getString("NAME") ) : rs.getString("NAME_KANA") ;
//  05/03/30            svf.VrsOut("KANA"         ,name_kana );                           //氏名(かな)
                final String nameField = getMS932ByteCount(name) > 20 ? "NAME_2" : "NAME";
                svf.VrsOut(nameField         ,name );                //氏名(漢字)

                svf.VrEndRecord();//１行出力
                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }
    
    private int getMS932ByteCount(final String s) {
        int n = 0;
        if (s != null) {
            try {
                n = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return n;
    }



    /**仮身分証明書**/
    private boolean setSvfout2(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        try {
            ResultSet rs = ps1.executeQuery();

            //年齢--------------
            Calendar cals = Calendar.getInstance();
            Calendar calb = Calendar.getInstance();
            cals.setTime(sdf.parse( param[5] ));                //有効期限(開始)をCalendar calに変換
//            int cals_year = cals.get(Calendar.YEAR);            //calsの年を取得
//            int cals_day  = cals.get(Calendar.DAY_OF_YEAR);     //calsの年の何日目を取得
            String birth_age = "";                              //年齢

            //画像--------------
            String badge = "SCHOOLMARK"  + "." + param[9];              //校章
            String sname = "SCHOOLNAME" + param[12] + "." + param[9];   //学校名
            String photo = "";                                          //顔写真
            String stamp = "PRINCIPAL" + param[12] + ".jpg";            //学校長印---NO006 ".bmp"→".jpg"
            String photo_check = "";
            String stamp_check = param[7] + "/" + param[8] + "/" + stamp;
            String badge_check = param[7] + "/" + param[8] + "/" + badge;
            String sname_check = param[7] + "/" + param[8] + "/" + sname;
            File f2 = new File(stamp_check);    //学校長印データ存在チェック用
            File f3 = new File(badge_check);    //校章データ存在チェック用
            File f4 = new File(sname_check);    //学校名データ存在チェック用

            while( rs.next() ){
                //年齢--------------
                //生年月日・年齢
                if (rs.getString("BIRTHDAY") != null) {
                    calb.setTime(sdf.parse( rs.getString("BIRTHDAY") ));//生年月日をCalendar calに変換
//                    int calb_year = calb.get(Calendar.YEAR);            //calbの年を取得
//                    int calb_day  = calb.get(Calendar.DAY_OF_YEAR);     //calbの年の何日目を取得
//                    if (calb_day <= cals_day) birth_age = "　(" + String.valueOf(cals_year - calb_year) + "才)";
//                    else                      birth_age = "　(" + String.valueOf(cals_year - calb_year - 1) + "才)";
                    svf.VrsOut("BIRTHDAY1" ,KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")) + birth_age );
                    svf.VrsOut("BIRTHDAY2" ,KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")) );
                }
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_useAddrField2) &&
                        ((addr1 != null && addr1.length() > 25) || 
                         (addr2 != null && addr2.length() > 25)))
                {
                    svf.VrsOut("ADDRESS1_3" ,addr1 );  //住所１(60)
                    svf.VrsOut("ADDRESS2_3" ,addr2 );  //住所２(60)
                    svf.VrsOut("ADDRESS3_3" ,addr1 );  //住所１(60)
                    svf.VrsOut("ADDRESS4_3" ,addr2 );  //住所２(60)
                } else if ((addr1 != null && addr1.length() > 20) || 
                            (addr2 != null && addr2.length() > 20))
                {
                    //住所１または住所２が２０文字超えたら(50)にセット
                    svf.VrsOut("ADDRESS1_2" ,addr1 );  //住所１(50)
                    svf.VrsOut("ADDRESS2_2" ,addr2 );  //住所２(50)
                    svf.VrsOut("ADDRESS3_2" ,addr1 );  //住所１(50)
                    svf.VrsOut("ADDRESS4_2" ,addr2 );  //住所２(50)
                } else {
                    svf.VrsOut("ADDRESS1_1" ,addr1 );  //住所１(40)
                    svf.VrsOut("ADDRESS2_1" ,addr2 );  //住所２(40)
                    svf.VrsOut("ADDRESS3_1" ,addr1 );  //住所１(40)
                    svf.VrsOut("ADDRESS4_1" ,addr2 );  //住所２(40)
                }
                //画像--------------
                //顔写真
                photo = "P" + rs.getString("SCHREGNO") + "." + param[9];
                photo_check = param[7] + "/" + param[8] + "/" + photo;
                File f1 = new File(photo_check);//写真データ存在チェック用
                if (f1.exists()) 
                    svf.VrsOut("PHOTO"    , photo_check );//顔写真
                //学校長印
                if (f2.exists()) {
                    svf.VrsOut("STAMP1"    , stamp_check );//学校長印
                    svf.VrsOut("STAMP2"    , stamp_check );//学校長印
                }
                //校章
                if (f3.exists()) 
                    svf.VrsOut("BITMAP1"  , badge_check );//校章
                //学校名
                if (f4.exists()) 
                    svf.VrsOut("BITMAP2"  , sname_check );//学校名
                //在籍--------------
                String coursename = (rs.getString("COURSENAME") != null) ? rs.getString("COURSENAME")+"課程　" : "";
                String majorname  = (rs.getString("MAJORNAME") != null) ? rs.getString("MAJORNAME")+"　" : "";
                String gradeName2 = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                String hrName = rs.getString("GRADE_NAME2") + " " + rs.getString("HR_CLASS") + "組";
                if (param[12].equals("H")) {
                    svf.VrsOut("CLASS1"   ,coursename + majorname + gradeName2 );
                    svf.VrsOut("CLASS2"   ,coursename + majorname + hrName );
//                  svf.VrsOut("CLASS1"   ,coursename + majorname + getHrName( rs, param ) );//年組---NO005
//                  svf.VrsOut("CLASS2"   ,coursename + majorname + getHrName( rs, param ) );//年組---NO005
//                  svf.VrsOut("CLASS1"   ,coursename + majorname + param[10] );//年組
//                  svf.VrsOut("CLASS2"   ,coursename + majorname + param[10] );//年組
                }
                if (param[12].equals("J")) {
                    svf.VrsOut("CLASS1"   ,gradeName2 );
                    svf.VrsOut("CLASS2"   ,hrName );
//                  svf.VrsOut("CLASS1"   ,getHrName( rs, param ) );//年組---NO005
//                  svf.VrsOut("CLASS2"   ,getHrName( rs, param ) );//年組---NO005
//                  svf.VrsOut("CLASS1"   ,param[10] );//年組
//                  svf.VrsOut("CLASS2"   ,param[10] );//年組
                }
                //その他--------------
                svf.VrsOut("SCHREGNO1"    ,rs.getString("SCHREGNO") );            //学籍番号
                svf.VrsOut("SCHREGNO2"    ,rs.getString("SCHREGNO") );            //学籍番号
                svf.VrsOut("SDATE1"       ,KNJ_EditDate.h_format_JP(param[5]));   //有効期限(開始)
                svf.VrsOut("FDATE1"       ,KNJ_EditDate.h_format_JP(param[6]));   //有効期限(終了)
                svf.VrsOut("SDATE2"       ,KNJ_EditDate.h_format_JP(param[5]));   //有効期限(開始)
//              svf.VrsOut("FDATE2"       ,KNJ_EditDate.h_format_JP(param[6]));   //有効期限(終了)
                svf.VrsOut("FDATE2"       ,KNJ_EditDate.h_format_JP(param[13]));  //有効期限(終了)---NO001
                svf.VrsOut("SCHOOLNAME1"  , param[11] + "長" );                   //学校長名
                svf.VrsOut("SCHOOLNAME2"  , param[11] + "長" );                   //学校長名
                svf.VrsOut("BARCODE"      ,rs.getString("SCHREGNO") );            //バーコード

                final String nameKana;
                final String name;
                if (param[3].equals("3") && "1".equals(rs.getString("USE_REAL_NAME"))) {
                    nameKana = rs.getString("REAL_NAME_KANA");
                    name = rs.getString("REAL_NAME");
                } else {
                    nameKana = rs.getString("NAME_KANA");
                    name = rs.getString("NAME");
                }
                
                svf.VrsOut("KANA"         ,nameKana );           //氏名(かな)
//  05/03/30 以前の仕様を有効とする                 String name_kana = (rs.getString("NAME_KANA") != null && rs.getString("NAME") != null) ? setKanaName( rs.getString("NAME_KANA"), rs.getString("NAME") ) : rs.getString("NAME_KANA") ;
//  05/03/30            svf.VrsOut("KANA"         ,name_kana );                           //氏名(かな)
                if (getMS932ByteCount(name) > 20) {
                    svf.VrsOut("NAME1_2"      ,name );                //氏名(漢字)
                    svf.VrsOut("NAME2_2"      ,name );                //氏名(漢字)
                } else {
                    svf.VrsOut("NAME1"        ,name );                //氏名(漢字)
                    svf.VrsOut("NAME2"        ,name );                //氏名(漢字)
                }

                svf.VrEndRecord();//１行出力
                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout2 set error!");
        }
        return nonedata;
    }


    /*
     * ふりがな(名)と漢字(名)の表示開始位置を合わせる
     *
     * ふりがな２文字を漢字１文字分と計算---2005.03.29
     **/
    private String setKanaName(
        String kana,
        String kanji
    ) {
        String kana_rep = "";   //リターン値(かな)
        try {
            int z;                  //空白文字の位置
            //漢字(姓)
            z = kanji.indexOf("　");
            if ( z < 0 ){
                kana_rep = kana;                    //空白がない
            } else {
                //漢字(姓)の文字数
                String strx = kanji.substring(0,z);
                int x = strx.length();
                //ふりがな(姓)
                z = kana.indexOf("　");
                if ( z < 0 ){
                    kana_rep = kana;                //空白がない
                } else {
                    //ふりがな(姓)の文字数
                    String stry = kana.substring(0,z);
                    int y = stry.length();
                    //ふりがな２文字を漢字１文字分と計算
                    String strbrank = "";
                    int imax = (x * 2) - y;
                    //ふりがなの文字数が多い場合
                    if (imax < 0) {
                        kana_rep = kana;
                    } else {
                        for (int i = 0; i < imax+2; i++) strbrank = strbrank + "　";
                        kana_rep = stry + strbrank + kana.substring(z+1);   //姓と名の間にブランクをセット
                    }
                }
            }
        } catch( Exception ex ) {
            log.error("setKanaName set error!");
        }
        return kana_rep;
    }



    /**生徒情報**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（学籍番号）
        try {
            /*---NO003---OLD↓---
            //住所
            stb.append("WITH SCH_ADDR AS ( ");
            stb.append("    SELECT SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    GROUP BY SCHREGNO ) ");
            //メイン
            stb.append("SELECT T1.SCHREGNO, ");
            stb.append("       VALUE(T1.ATTENDNO,'0') AS ATTENDNO, ");
            stb.append("       T5.COURSENAME, ");
            stb.append("       T6.MAJORNAME, ");
            stb.append("       T2.NAME, ");
            stb.append("       T2.NAME_KANA, ");
            stb.append("       T2.BIRTHDAY, ");
            if (param[3].equals("1") && (param[2].substring(0,2)).equals("01")) {//仮身分証明書:新入生
                stb.append("   T2.ADDR1, ");
                stb.append("   T2.ADDR2 ");
            } else {
                stb.append("   T3.ADDR1, ");
                stb.append("   T3.ADDR2 ");
            }
            if (param[3].equals("1")) //仮身分証明書
                stb.append("FROM   CLASS_FORMATION_DAT T1 ");
            if (param[3].equals("2")) //身分証明書
                stb.append("FROM   SCHREG_REGD_DAT T1 ");
            if (param[3].equals("1") && (param[2].substring(0,2)).equals("01")) {//仮身分証明書:新入生
                stb.append("       INNER JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR=T1.YEAR AND T2.SCHREGNO=T1.SCHREGNO ");
            } else {
                stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
                stb.append("       INNER JOIN SCHREG_ADDRESS_DAT T3 ON T3.SCHREGNO=T1.SCHREGNO ");
                stb.append("       INNER JOIN SCH_ADDR T4 ON T4.SCHREGNO=T3.SCHREGNO AND T4.ISSUEDATE=T3.ISSUEDATE ");
            }
            ---NO003---OLD↑---*/
            //生徒情報---NO005
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT * ");
                //1:新入生データ,23:在籍データ
            if (param[3].equals("1")) 
                stb.append("FROM   CLASS_FORMATION_DAT ");
            else 
                stb.append("FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
                //1:個人,2:クラス
            if (param[14].equals("2")) 
                stb.append("       GRADE||HR_CLASS IN "+param[4]+" ");
            if (param[14].equals("1")) 
                stb.append("       SCHREGNO IN "+param[4]+" ");
            stb.append("    ) ");

            //---NO003---NEW↓---
            //住所
//          stb.append("WITH SCH_ADDR1 AS ( ");
            stb.append(",SCH_ADDR1 AS ( ");
            stb.append("    SELECT SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");//---NO005
//          stb.append("    WHERE  SCHREGNO IN "+param[4]+" ");
            stb.append("    GROUP BY SCHREGNO ) ");
            stb.append(",SCH_ADDR2 AS ( ");
            stb.append("    SELECT SCHREGNO,ISSUEDATE,ADDR1,ADDR2 ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ) ");//---NO005
//          stb.append("    WHERE  SCHREGNO IN "+param[4]+" ) ");
            stb.append(",SCH_ADDR AS ( ");
            stb.append("    SELECT W2.SCHREGNO,W2.ADDR1,W2.ADDR2 ");
            stb.append("    FROM   SCH_ADDR1 W1,SCH_ADDR2 W2 ");
            stb.append("    WHERE  W1.SCHREGNO=W2.SCHREGNO AND ");
            stb.append("           W1.ISSUEDATE=W2.ISSUEDATE ) ");

            //メイン
            stb.append("SELECT T1.SCHREGNO, ");
            stb.append("       T1.GRADE, T1.HR_CLASS, T7.HR_NAMEABBV, ");//---NO005
            stb.append("       VALUE(T1.ATTENDNO,'0') AS ATTENDNO, ");
            stb.append("       T5.COURSENAME, ");
            stb.append("       T6.MAJORNAME, ");
            stb.append("       VALUE(T9.GRADE_NAME2, '') AS GRADE_NAME2, ");
            if (param[3].equals("1")) {//仮身分証明書
                //stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("       CASE WHEN T1.GRADE = '01' AND T2.SCHREGNO IS NULL ");//---NO004
                stb.append("            THEN T0.NAME ELSE T2.NAME END AS NAME, ");
                //stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("       CASE WHEN T1.GRADE = '01' AND T2.SCHREGNO IS NULL ");//---NO004
                stb.append("            THEN T0.NAME_KANA ELSE T2.NAME_KANA END AS NAME_KANA, ");
                //stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("       CASE WHEN T1.GRADE = '01' AND T2.SCHREGNO IS NULL ");//---NO004
                stb.append("            THEN T0.BIRTHDAY ELSE T2.BIRTHDAY END AS BIRTHDAY, ");
                //stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("       CASE WHEN T1.GRADE = '01' AND T2.SCHREGNO IS NULL ");//---NO004
                stb.append("            THEN T0.ADDR1 ELSE T3.ADDR1 END AS ADDR1, ");
                //stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("       CASE WHEN T1.GRADE = '01' AND T2.SCHREGNO IS NULL ");//---NO004
                stb.append("            THEN T0.ADDR2 ELSE T3.ADDR2 END AS ADDR2 ");
//              stb.append("FROM   CLASS_FORMATION_DAT T1 ");
            }
//            if (param[3].equals("2")) {//身分証明書
            if (param[3].equals("2") || param[3].equals("3")) {//身分証明書---仮身分証明書(在籍)---2005.06.10Modify
                stb.append("       T2.NAME, T2.REAL_NAME, T2.NAME_KANA, T2.REAL_NAME_KANA, T2.BIRTHDAY, T3.ADDR1, T3.ADDR2, ");
                stb.append("       (CASE WHEN T8.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME ");
//              stb.append("FROM   SCHREG_REGD_DAT T1 ");
            }
            stb.append("FROM   SCHNO T1 ");//---NO005
            stb.append("       LEFT JOIN FRESHMAN_DAT T0 ON T0.ENTERYEAR=T1.YEAR AND T0.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCH_ADDR T3 ON T3.SCHREGNO=T1.SCHREGNO ");
            //---NO003---NEW↑---
            stb.append("       LEFT JOIN COURSE_MST T5 ON T5.COURSECD=T1.COURSECD ");
            stb.append("       LEFT JOIN MAJOR_MST T6 ON T6.COURSECD=T1.COURSECD AND T6.MAJORCD=T1.MAJORCD ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR=T1.YEAR AND T7.SEMESTER=T1.SEMESTER AND ");
            stb.append("                                        T7.GRADE=T1.GRADE AND T7.HR_CLASS=T1.HR_CLASS ");//---NO005
            stb.append("       LEFT JOIN SCHREG_NAME_SETUP_DAT T8 ON T8.SCHREGNO=T1.SCHREGNO AND T8.DIV='05' ");
            stb.append("       LEFT JOIN SCHREG_REGD_GDAT T9 ON T9.YEAR=T1.YEAR AND T9.GRADE=T1.GRADE ");

//---NO005
//          stb.append("WHERE  T1.YEAR='"+param[0]+"' AND ");
//          stb.append("       T1.SEMESTER='"+param[1]+"' AND ");
//          stb.append("       T1.GRADE||T1.HR_CLASS = '"+param[2]+"' AND ");
//          stb.append("       T1.SCHREGNO IN "+param[4]+" ");
            stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り


    /**組名称**/
    private String preStat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT HR_NAME, HR_NAMEABBV ");
            stb.append("FROM   SCHREG_REGD_HDAT ");
            stb.append("WHERE  YEAR='"+param[0]+"' AND ");
            stb.append("       SEMESTER='"+param[1]+"' AND ");
            stb.append("       GRADE||HR_CLASS = '"+param[2]+"' ");
        } catch( Exception e ){
            log.error("preStat2 error!");
        }
        return stb.toString();

    }//preStat2()の括り


    /**学校名---NO005未使用**/
    private String preStat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT SCHOOLNAME1 ");
            stb.append("FROM   SCHOOL_MST ");
            stb.append("WHERE  YEAR='"+param[0]+"' ");
        } catch( Exception e ){
            log.error("preStat3 error!");
        }
        return stb.toString();

    }//preStat3()の括り


    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps3
    ) {
        try {
            ps1.close();
            ps3.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



}//クラスの括り
