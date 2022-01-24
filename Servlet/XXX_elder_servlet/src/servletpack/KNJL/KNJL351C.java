package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *      ＜ＫＮＪＬ３５１Ｃ＞
 *      ＜１＞  合格通知書
 *      ＜２＞  入学許可通知書(中学/高校)
 *      ＜３＞  移行合格通知書
 *      ＜４＞  追加合格通知書
 *
 *    2008/11/07 takara 作成日
 *
 **/

public class KNJL351C {
    private static final Log log = LogFactory.getLog(KNJL351C.class);
    Param _param;

/****************************************************************************************************************/
/****************************************************************************************************************/
/**********************************                                   *******************************************/
/**********************************            処理のメイン           *******************************************/
/**********************************                                   *******************************************/
/****************************************************************************************************************/
/****************************************************************************************************************/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //■■■print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //■■■svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //■■■ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _param = new Param(request, db2);

        //■■■SVF出力
        boolean nonedata = true;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)
        nonedata = setSvfMain(db2, svf); //帳票出力のメソッド

        //■■■該当データ無し(nonedate=falseで該当データなし)
        if( nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
        }
        svf.VrEndPage();

        //■■■終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();       //DBを閉じる
        outstrm.close();   //ストリームを閉じる
    }


/****************************************************************************************************************/
/****************************************************************************************************************/
/**********************************                                   *******************************************/
/**********************************           フォームに出力          *******************************************/
/**********************************                                   *******************************************/
/****************************************************************************************************************/
/****************************************************************************************************************/
    private boolean setSvfMain(DB2UDB db2, Vrw32alp svf) {
        boolean nonedata = true;
        for(int i=0; i<_param._testdiv.length; i++) {
            String currentTestdiv = _param._testdiv[i];
            nonedata &= setSvfList1(db2, svf, currentTestdiv);
            nonedata &= setSvfList2(db2, svf, currentTestdiv);
        }
        return nonedata;
    }

    private boolean setSvfList1(DB2UDB db2, Vrw32alp svf, String currentTestdiv) {
        svf.VrSetForm(_param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "KNJL351C_1_2.frm" : "KNJL351C_1.frm", 4);
        String district;
        String applicantdiv;
        String testdiv;
        String nendo;
        String select_type;
        String total1;
        String boy1;
        String birl1;
        String total2;
        String boy2;
        String girl2;
        String total3;
        String boy3;
        String girl3;
        String total4;
        String boy4;
        String girl4;
        String date = gethiduke(db2, _param._login_date);
        HashMap totalPageMap = getTotalPage(db2, currentTestdiv, false);
        String totalPage = null;
        int page = 0;
        int count = 0;
        if (_param._print_type.equals("1")) {
            select_type = "合格者";
        } else {
            select_type = "入学者";
        }
        if (getSeirekiFlg(db2)) {
            nendo = _param._year + "年度";
        } else {
            nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        }

        PreparedStatement sql = null;
        boolean nonedata = true;
        try {
            int allTotal1=0, allBoy1=0, allGirl1=0, allTotal2=0, allBoy2=0, allGirl2=0;
            int allTotal3=0, allBoy3=0, allGirl3=0, allTotal4=0, allBoy4=0, allGirl4=0;
            sql = db2.prepareStatement(get_sql(currentTestdiv, false));
            ResultSet result = sql.executeQuery();
            String oldTestdiv = null;

            while (result.next()) {

                oldTestdiv = result.getString("TESTDIV_VALUE");
                if (count % 50 == 0) {
                    page++;
                }
                count++;
                district   = nvlT(result.getString("TIKUMEI")) + "("+nvlT(result.getString("DISTRICTCD"))+")";
                total1       = result.getString("SIGAN_KEI");
                boy1         = result.getString("SIGAN_OTOKO");
                birl1        = result.getString("SIGAN_ONNA");
                total2       = result.getString("PASS_KEI");
                boy2         = result.getString("PASS_OTOKO");
                girl2        = result.getString("PASS_ONNA");
                total3       = result.getString("PASS_KEI3");
                boy3         = result.getString("PASS_OTOKO3");
                girl3        = result.getString("PASS_ONNA3");
                total4       = result.getString("PASS_KEI4");
                boy4         = result.getString("PASS_OTOKO4");
                girl4        = result.getString("PASS_ONNA4");
                applicantdiv = result.getString("APPLICANTDIV");
                testdiv      = result.getString("TESTDIV");
                totalPage = (String) totalPageMap.get(Integer.valueOf(result.getString("TESTDIV_VALUE")));

                svf.VrsOut("PAGE"         , String.valueOf(page));
                svf.VrsOut("DATE"         , date);
                svf.VrsOut("TOTAL_PAGE"   , String.valueOf(totalPage));
                svf.VrsOut("NENDO"        , nendo);
                svf.VrsOut("APPLICANTDIV" , applicantdiv);
                svf.VrsOut("TESTDIV"      , testdiv);
                svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
                svf.VrsOut("SELECT_TYPE"  , select_type);
                svf.VrsOut("DISTRICT"   , district);
                if (!total1.equals("0")){ svf.VrsOut("TOTAL1"  , total1); allTotal1 += Integer.parseInt(total1);}
                if (!boy1.equals("0")){ svf.VrsOut("BOY1"    , boy1);     allBoy1   += Integer.parseInt(boy1  );}
                if (!birl1.equals("0")){ svf.VrsOut("GIRL1"   , birl1);   allGirl1  += Integer.parseInt(birl1 );}
                if (!total2.equals("0")){ svf.VrsOut("TOTAL2"  , total2); allTotal2 += Integer.parseInt(total2);}
                if (!boy2.equals("0")){ svf.VrsOut("BOY2"    , boy2);     allBoy2   += Integer.parseInt(boy2  );}
                if (!girl2.equals("0")){ svf.VrsOut("GIRL2"   , girl2);   allGirl2  += Integer.parseInt(girl2 );}
                if (!total3.equals("0")){ svf.VrsOut("TOTAL3"  , total3); allTotal3 += Integer.parseInt(total3);}
                if (!boy3.equals("0")){ svf.VrsOut("BOY3"    , boy3);     allBoy3   += Integer.parseInt(boy3  );}
                if (!girl3.equals("0")){ svf.VrsOut("GIRL3"   , girl3);   allGirl3  += Integer.parseInt(girl3 );}
                if (!total4.equals("0")){ svf.VrsOut("TOTAL4"  , total4); allTotal4 += Integer.parseInt(total4);}
                if (!boy4.equals("0")){ svf.VrsOut("BOY4"    , boy4);     allBoy4   += Integer.parseInt(boy4  );}
                if (!girl4.equals("0")){ svf.VrsOut("GIRL4"   , girl4);   allGirl4  += Integer.parseInt(girl4 );}

                svf.VrEndRecord();
                nonedata = false;
            }

            if (nonedata==false) {
                if (count % 50 == 0) {
                    page++;
                }
                count++;
                totalPage = (String) totalPageMap.get(Integer.valueOf(oldTestdiv));
                svf.VrsOut("PAGE"       , String.valueOf(page));
                svf.VrsOut("DATE"       , date);
                svf.VrsOut("TOTAL_PAGE" , String.valueOf(totalPage));
                svf.VrsOut("NENDO"      , nendo);
                svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
                svf.VrsOut("ITEM" , "総計");
                if (allTotal1 != 0) svf.VrsOut("TOTAL1"     , String.valueOf(allTotal1));
                if (allBoy1   != 0) svf.VrsOut("BOY1"       , String.valueOf(allBoy1));
                if (allGirl1  != 0) svf.VrsOut("GIRL1"      , String.valueOf(allGirl1));
                if (allTotal2 != 0) svf.VrsOut("TOTAL2"     , String.valueOf(allTotal2));
                if (allBoy2   != 0) svf.VrsOut("BOY2"       , String.valueOf(allBoy2));
                if (allGirl2  != 0) svf.VrsOut("GIRL2"      , String.valueOf(allGirl2));
                if (allTotal3 != 0) svf.VrsOut("TOTAL3"     , String.valueOf(allTotal3));
                if (allBoy3   != 0) svf.VrsOut("BOY3"       , String.valueOf(allBoy3));
                if (allGirl3  != 0) svf.VrsOut("GIRL3"      , String.valueOf(allGirl3));
                if (allTotal4 != 0) svf.VrsOut("TOTAL4"     , String.valueOf(allTotal4));
                if (allBoy4   != 0) svf.VrsOut("BOY4"       , String.valueOf(allBoy4));
                if (allGirl4  != 0) svf.VrsOut("GIRL4"      , String.valueOf(allGirl4));
                svf.VrEndRecord();
                log.debug("count="+count);
                for(int i=count; i<50; i++) {
                    svf.VrsOut("DISTRICT", "\n");
                    svf.VrEndRecord();
                }
            }
        } catch (Exception e) {
            log.debug("svf出力の区間でエラーらしい" , e);
        }
        return nonedata;
    }

    private boolean setSvfList2(DB2UDB db2, Vrw32alp svf, String currentTestdiv) {
        svf.VrSetForm(_param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "KNJL351C_2_2.frm" : "KNJL351C_2.frm", 4);
        String finschoolcd;
        String districtcd;
        String schoolname;
        String applicantdiv;
        String testdiv;
        String nendo;
        String select_type;
        String total1;
        String boy1;
        String birl1;
        String total2;
        String boy2;
        String girl2;
        String total3;
        String boy3;
        String girl3;
        String total4;
        String boy4;
        String girl4;
        String date = gethiduke(db2, _param._login_date);
        HashMap totalPageMap = getTotalPage(db2, currentTestdiv, true);
        String totalPage = null;
        int page = 0;
        int count = 0;
        if (_param._print_type.equals("1")) {
            select_type = "合格者";
        } else {
            select_type = "入学者";
        }
        if (getSeirekiFlg(db2)) {
            nendo = _param._year + "年度";
        } else {
            nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        }

        PreparedStatement sql = null;
        boolean nonedata = true;
        try {
            int allTotal1=0, allBoy1=0, allGirl1=0, allTotal2=0, allBoy2=0, allGirl2=0;
            int allTotal3=0, allBoy3=0, allGirl3=0, allTotal4=0, allBoy4=0, allGirl4=0;
            sql = db2.prepareStatement(get_sql(currentTestdiv, true));
            ResultSet result = sql.executeQuery();
            String oldTestdiv = null;

            while (result.next()) {

                oldTestdiv = result.getString("TESTDIV_VALUE");
                if (count % 50 == 0) {
                    page++;
                }
                count++;
                finschoolcd  = result.getString("FINSCHOOLCD");
                districtcd   = result.getString("DISTRICTCD");
                schoolname   = result.getString("FINSCHOOL_NAME");
                total1       = result.getString("SIGAN_KEI");
                boy1         = result.getString("SIGAN_OTOKO");
                birl1        = result.getString("SIGAN_ONNA");
                total2       = result.getString("PASS_KEI");
                boy2         = result.getString("PASS_OTOKO");
                girl2        = result.getString("PASS_ONNA");
                total3       = result.getString("PASS_KEI3");
                boy3         = result.getString("PASS_OTOKO3");
                girl3        = result.getString("PASS_ONNA3");
                total4       = result.getString("PASS_KEI4");
                boy4         = result.getString("PASS_OTOKO4");
                girl4        = result.getString("PASS_ONNA4");
                applicantdiv = result.getString("APPLICANTDIV");
                testdiv      = result.getString("TESTDIV");
                totalPage = (String) totalPageMap.get(Integer.valueOf(result.getString("TESTDIV_VALUE")));

                svf.VrsOut("PAGE"         , String.valueOf(page));
                svf.VrsOut("DATE"         , date);
                svf.VrsOut("TOTAL_PAGE"   , String.valueOf(totalPage));
                svf.VrsOut("NENDO"        , nendo);
                svf.VrsOut("APPLICANTDIV" , applicantdiv);
                svf.VrsOut("TESTDIV"      , testdiv);
                svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
                String fieldSchool = "FINSCHOOL1";
                if (null != schoolname && schoolname.length() > 13 ) {fieldSchool = "FINSCHOOL2"; }
                svf.VrsOut(fieldSchool , schoolname);
                svf.VrsOut("FINSCHOOLCD"  , finschoolcd);
                svf.VrsOut("SELECT_TYPE"  , select_type);
                svf.VrsOut("DISTRICTCD"   , districtcd);
//                svf.VrsOut("ITEM"         , districtcd);
                if (!total1.equals("0")){ svf.VrsOut("TOTAL1"  , total1); allTotal1 += Integer.parseInt(total1);}
                if (!boy1.equals("0")){ svf.VrsOut("BOY1"    , boy1);     allBoy1   += Integer.parseInt(boy1  );}
                if (!birl1.equals("0")){ svf.VrsOut("GIRL1"   , birl1);   allGirl1  += Integer.parseInt(birl1 );}
                if (!total2.equals("0")){ svf.VrsOut("TOTAL2"  , total2); allTotal2 += Integer.parseInt(total2);}
                if (!boy2.equals("0")){ svf.VrsOut("BOY2"    , boy2);     allBoy2   += Integer.parseInt(boy2  );}
                if (!girl2.equals("0")){ svf.VrsOut("GIRL2"   , girl2);   allGirl2  += Integer.parseInt(girl2 );}
                if (!total3.equals("0")){ svf.VrsOut("TOTAL3"  , total3); allTotal3 += Integer.parseInt(total3);}
                if (!boy3.equals("0")){ svf.VrsOut("BOY3"    , boy3);     allBoy3   += Integer.parseInt(boy3  );}
                if (!girl3.equals("0")){ svf.VrsOut("GIRL3"   , girl3);   allGirl3  += Integer.parseInt(girl3 );}
                if (!total4.equals("0")){ svf.VrsOut("TOTAL4"  , total4); allTotal4 += Integer.parseInt(total4);}
                if (!boy4.equals("0")){ svf.VrsOut("BOY4"    , boy4);     allBoy4   += Integer.parseInt(boy4  );}
                if (!girl4.equals("0")){ svf.VrsOut("GIRL4"   , girl4);   allGirl4  += Integer.parseInt(girl4 );}

                svf.VrEndRecord();
                nonedata = false;
            }

            if (nonedata==false) {
                if (count % 50 == 0) {
                    page++;
                }
                count++;
                totalPage = (String) totalPageMap.get(Integer.valueOf(oldTestdiv));
                svf.VrsOut("PAGE"       , String.valueOf(page));
                svf.VrsOut("DATE"       , date);
                svf.VrsOut("TOTAL_PAGE" , String.valueOf(totalPage));
                svf.VrsOut("NENDO"      , nendo);
                svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
                svf.VrsOut("FINSCHOOL1" , "総計");
                if (allTotal1 != 0) svf.VrsOut("TOTAL1"     , String.valueOf(allTotal1));
                if (allBoy1   != 0) svf.VrsOut("BOY1"       , String.valueOf(allBoy1));
                if (allGirl1  != 0) svf.VrsOut("GIRL1"      , String.valueOf(allGirl1));
                if (allTotal2 != 0) svf.VrsOut("TOTAL2"     , String.valueOf(allTotal2));
                if (allBoy2   != 0) svf.VrsOut("BOY2"       , String.valueOf(allBoy2));
                if (allGirl2  != 0) svf.VrsOut("GIRL2"      , String.valueOf(allGirl2));
                if (allTotal3 != 0) svf.VrsOut("TOTAL3"     , String.valueOf(allTotal3));
                if (allBoy3   != 0) svf.VrsOut("BOY3"       , String.valueOf(allBoy3));
                if (allGirl3  != 0) svf.VrsOut("GIRL3"      , String.valueOf(allGirl3));
                if (allTotal4 != 0) svf.VrsOut("TOTAL4"     , String.valueOf(allTotal4));
                if (allBoy4   != 0) svf.VrsOut("BOY4"       , String.valueOf(allBoy4));
                if (allGirl4  != 0) svf.VrsOut("GIRL4"      , String.valueOf(allGirl4));
                svf.VrEndRecord();
                for(int i=count; i<50; i++) {
                    svf.VrsOut("FINSCHOOLCD", "\n");
                    svf.VrEndRecord();
                }
            }
        } catch (Exception e) {
            log.debug("svf出力の区間でエラーらしい" , e);
        }
        return nonedata;
    }

    private HashMap getTotalPage(DB2UDB db2, String currentTestdiv, boolean groupByFinschoolCd) {
        PreparedStatement sql = null;
        HashMap totalPageMap = new HashMap();
        try {
            sql = db2.prepareStatement(getSqltotalPage(currentTestdiv, groupByFinschoolCd));
            ResultSet result = sql.executeQuery();
            while(result.next()) {
                Integer testdiv = Integer.valueOf(result.getString("TESTDIV"));
                int i = Integer.valueOf(result.getString("X")).intValue();
                int totalPage = i/50;
                if (i%50 > 0) {
                    totalPage++;
                }
                log.debug("testdiv = "+testdiv+" , totalPage = " + totalPage);
                totalPageMap.put(testdiv, String.valueOf(totalPage));
            }

        } catch (Exception e) {
            log.debug("トータルページの処理でエラー" + e);
        }
        return totalPageMap;
    }

    private String gethiduke(DB2UDB db2, String inputDate) {
        // 西暦か和暦はフラグで判断
        boolean _seirekiFlg = getSeirekiFlg(db2);
        String date;
        if (null != inputDate) {
            if (_seirekiFlg) {
                //2008年3月3日の形
                date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
            } else {
                //平成14年10月27日の形
                date = KNJ_EditDate.h_format_JP(db2, inputDate);
            }
            return date;
        }
        return null;
    }

    /* 西暦表示にするのかのフラグ  */
    private boolean getSeirekiFlg(DB2UDB db2) {
        boolean seirekiFlg = false;
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if ( rs.next() ) {
                if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("getSeirekiFlg Exception", e);
        }
        return seirekiFlg;
    }

    /****************************************************************************************************************/
    /****************************************************************************************************************/
    /**********************************                                   *******************************************/
    /**********************************         sqlを作る                 *******************************************/
    /**********************************                                   *******************************************/
    /****************************************************************************************************************/
    /****************************************************************************************************************/
    /**
     * @testDiv 出力指定の試験区分
     * @groupByFinSchoolCd 学校単位でGROUP BYするときのフラグ
     */
    private String get_sql(String currentTestdiv, boolean groupByFinschoolCd) {
        StringBuffer stb = new StringBuffer();

        //和歌山中学の「前／後期」
        if ("0".equals(currentTestdiv)) {
            stb.append(" SELECT ");
            stb.append("     '0' AS TESTDIV_VALUE, ");
            if (groupByFinschoolCd) { stb.append("     F1.FINSCHOOLCD, "); }
            stb.append("     F1.DISTRICTCD, ");
            if (groupByFinschoolCd) {
                if (_param.isWakayama()) {
                    stb.append("     MAX(F1.FINSCHOOL_NAME_ABBV) AS FINSCHOOL_NAME, ");
                } else {
                    stb.append("     MAX(F1.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
                }
            }
            stb.append("     SUM(CASE WHEN E1.SEX = '1' OR E1.SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_KEI, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_OTOKO, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_ONNA, ");
            String whereIfPass = _param._print_type.equals("2") ? "  AND E1.PROCEDUREDIV = '1' AND E1.ENTDIV = '1' " : "";
            String whereIfPass8  = _param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "  AND R1.JUDGEDIV in ('8') " : "";
            String whereIfPass9  = _param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "  AND R1.JUDGEDIV in ('9') " : "";
            String whereIfPass89 = _param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "  AND R1.JUDGEDIV in ('8','9') " : "";
            stb.append("     SUM(CASE WHEN (E1.SEX = '1' OR E1.SEX = '2') AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass89+" THEN 1 ELSE 0 END) AS PASS_KEI, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass89+" THEN 1 ELSE 0 END) AS PASS_OTOKO, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass89+" THEN 1 ELSE 0 END) AS PASS_ONNA, ");
            stb.append("     SUM(CASE WHEN (E1.SEX = '1' OR E1.SEX = '2') AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass8+" THEN 1 ELSE 0 END) AS PASS_KEI3, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass8+" THEN 1 ELSE 0 END) AS PASS_OTOKO3, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass8+" THEN 1 ELSE 0 END) AS PASS_ONNA3, ");
            stb.append("     SUM(CASE WHEN (E1.SEX = '1' OR E1.SEX = '2') AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass9+" THEN 1 ELSE 0 END) AS PASS_KEI4, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass9+" THEN 1 ELSE 0 END) AS PASS_OTOKO4, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass9+" THEN 1 ELSE 0 END) AS PASS_ONNA4, ");
            stb.append("     MAX(N2.NAME1) AS TIKUMEI, ");
            stb.append("     MAX(N3.NAME1) AS APPLICANTDIV, ");
            stb.append("     '前／後期' AS TESTDIV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("   LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = E1.FS_CD ");
            stb.append("   LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z003' AND N2.NAMECD2 = F1.DISTRICTCD ");
            stb.append("   LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L003' AND N3.NAMECD2 = E1.APPLICANTDIV ");
            stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = E1.ENTEXAMYEAR AND R1.APPLICANTDIV = E1.APPLICANTDIV AND R1.TESTDIV = E1.TESTDIV AND R1.EXAMNO = E1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("      E1.ENTEXAMYEAR = '"+ _param._year +"' ");
            stb.append("  AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
            stb.append("  AND E1.TESTDIV IN ('1','2') ");
            stb.append(" GROUP BY F1.DISTRICTCD ");
            if (groupByFinschoolCd) {stb.append(" , F1.FINSCHOOLCD "); }
            stb.append(" ORDER BY F1.DISTRICTCD ");
            if (groupByFinschoolCd) {stb.append(" , F1.FINSCHOOLCD "); }
        } else {
            stb.append(" SELECT ");
            stb.append("     E1.TESTDIV AS TESTDIV_VALUE, ");
            if (groupByFinschoolCd) { stb.append("     F1.FINSCHOOLCD, "); }
            stb.append("     F1.DISTRICTCD, ");
            if (groupByFinschoolCd) {
                if (_param.isWakayama()) {
                    stb.append("     MAX(F1.FINSCHOOL_NAME_ABBV) AS FINSCHOOL_NAME, ");
                } else {
                    stb.append("     MAX(F1.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
                }
            }
            stb.append("     SUM(CASE WHEN E1.SEX = '1' OR E1.SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_KEI, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_OTOKO, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_ONNA, ");
            String whereIfPass = _param._print_type.equals("2") ? "  AND E1.PROCEDUREDIV = '1' AND E1.ENTDIV = '1' " : "";
            String whereIfPass8  = _param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "  AND R1.JUDGEDIV in ('8') " : "";
            String whereIfPass9  = _param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "  AND R1.JUDGEDIV in ('9') " : "";
            String whereIfPass89 = _param.isWakayama() && "1".equals(_param._applicantdiv) && "1".equals(_param._print_type) ? "  AND R1.JUDGEDIV in ('8','9') " : "";
            stb.append("     SUM(CASE WHEN (E1.SEX = '1' OR E1.SEX = '2') AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass89+" THEN 1 ELSE 0 END) AS PASS_KEI, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass89+" THEN 1 ELSE 0 END) AS PASS_OTOKO, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass89+" THEN 1 ELSE 0 END) AS PASS_ONNA, ");
            stb.append("     SUM(CASE WHEN (E1.SEX = '1' OR E1.SEX = '2') AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass8+" THEN 1 ELSE 0 END) AS PASS_KEI3, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass8+" THEN 1 ELSE 0 END) AS PASS_OTOKO3, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass8+" THEN 1 ELSE 0 END) AS PASS_ONNA3, ");
            stb.append("     SUM(CASE WHEN (E1.SEX = '1' OR E1.SEX = '2') AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass9+" THEN 1 ELSE 0 END) AS PASS_KEI4, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '1' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass9+" THEN 1 ELSE 0 END) AS PASS_OTOKO4, ");
            stb.append("     SUM(CASE WHEN E1.SEX = '2' AND E1.JUDGEMENT in ('1','7') "+whereIfPass+whereIfPass9+" THEN 1 ELSE 0 END) AS PASS_ONNA4, ");
            stb.append("     MAX(N2.NAME1) AS TIKUMEI, ");
            stb.append("     MAX(N3.NAME1) AS APPLICANTDIV, ");
            stb.append("     MAX(N4.NAME1) AS TESTDIV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("   LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = E1.FS_CD ");
            stb.append("   LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z003' AND N2.NAMECD2 = F1.DISTRICTCD ");
            stb.append("   LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L003' AND N3.NAMECD2 = E1.APPLICANTDIV ");
            stb.append("   LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L004' AND N4.NAMECD2 = E1.TESTDIV ");
            stb.append("   LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = E1.ENTEXAMYEAR AND R1.APPLICANTDIV = E1.APPLICANTDIV AND R1.TESTDIV = E1.TESTDIV AND R1.EXAMNO = E1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("      E1.ENTEXAMYEAR = '"+ _param._year +"' ");
            stb.append("  AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
            stb.append("  AND E1.TESTDIV = '"+ currentTestdiv +"' ");
            stb.append(" GROUP BY E1.TESTDIV, F1.DISTRICTCD ");
            if (groupByFinschoolCd) {stb.append(" , F1.FINSCHOOLCD "); }
            stb.append(" ORDER BY E1.TESTDIV, F1.DISTRICTCD ");
            if (groupByFinschoolCd) {stb.append(" , F1.FINSCHOOLCD "); }
        }

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }

    private String getSqltotalPage(String currentTestdiv, boolean groupByFinschoolCd) {
        StringBuffer stb = new StringBuffer();

        //和歌山中学の「前／後期」
        if ("0".equals(currentTestdiv)) {
            stb.append(" WITH MK AS ( ");
            stb.append(" SELECT ");
            stb.append("     '0' AS TESTDIV, 'X' AS X ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("   LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = E1.FS_CD ");
            stb.append("   LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = E1.JUDGEMENT ");
            stb.append("   LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z003' AND N2.NAMECD2 = F1.DISTRICTCD ");
            stb.append("   LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L003' AND N3.NAMECD2 = E1.APPLICANTDIV ");
            stb.append(" WHERE ");
            stb.append("      E1.ENTEXAMYEAR = '"+ _param._year +"'  ");
            stb.append("  AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"'  ");
            stb.append("  AND E1.TESTDIV IN ('1','2') ");
            stb.append(" GROUP BY F1.DISTRICTCD ");
            if (groupByFinschoolCd) { stb.append(" , F1.FINSCHOOLCD "); }
            stb.append(" ORDER BY F1.DISTRICTCD ");
            if (groupByFinschoolCd) { stb.append(" , F1.FINSCHOOLCD "); }
            stb.append(" ) ");
            stb.append(" SELECT TESTDIV, COUNT(*) AS X FROM MK GROUP BY TESTDIV ");
        } else {
            stb.append(" WITH MK AS ( ");
            stb.append(" SELECT ");
            stb.append("     E1.TESTDIV, 'X' AS X ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
            stb.append("   LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = E1.FS_CD ");
            stb.append("   LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = E1.JUDGEMENT ");
            stb.append("   LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z003' AND N2.NAMECD2 = F1.DISTRICTCD ");
            stb.append("   LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L003' AND N3.NAMECD2 = E1.APPLICANTDIV ");
            stb.append("   LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L004' AND N4.NAMECD2 = E1.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("      E1.ENTEXAMYEAR = '"+ _param._year +"'  ");
            stb.append("  AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"'  ");
            stb.append("  AND E1.TESTDIV = '"+ currentTestdiv +"' ");
            stb.append(" GROUP BY E1.TESTDIV, F1.DISTRICTCD ");
            if (groupByFinschoolCd) { stb.append(" , F1.FINSCHOOLCD "); }
            stb.append(" ORDER BY E1.TESTDIV, F1.DISTRICTCD ");
            if (groupByFinschoolCd) { stb.append(" , F1.FINSCHOOLCD "); }
            stb.append(" ) ");
            stb.append(" SELECT TESTDIV, COUNT(*) AS X FROM MK GROUP BY TESTDIV ");
        }

        log.debug("SQL = " + stb.toString());
        return stb.toString();
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

    /**
     * パラメータを受け取るクラス
     **/
    private class Param {
        private final String _prgid;
        private final String _dbname;
        private final String _year;
        private final String _login_date;
        private final String _applicantdiv;
        private final String[] _testdiv;
        private final String _print_type;
        private final String _z010SchoolCode;

        Param(
                final HttpServletRequest request,
                final DB2UDB db2
        ) {
            _prgid        = request.getParameter("PRGID");
            _dbname       = request.getParameter("DBNAME");
            _year         = request.getParameter("YEAR");
            _login_date   = request.getParameter("LOGIN_DATE");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _z010SchoolCode = getSchoolCode(db2);
            String temp_testdiv      = request.getParameter("TESTDIV");
            if ("9".equals(temp_testdiv)) {
                if("1".equals(_applicantdiv)) _testdiv = new String[]{"1","2"};
                else if("2".equals(_applicantdiv)) {
                    if (isGojo()) {
                        _testdiv = new String[]{"3","4","5","7"};
                    } else {
                        _testdiv = new String[]{"3","4","5"};
                    }
                }
                else _testdiv = null;
            } else {
                _testdiv = new String[]{temp_testdiv};
            }
            _print_type   = request.getParameter("PRINT_TYPE");
        }

        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantdiv)) certifKindCd = "105";
            if ("2".equals(_applicantdiv)) certifKindCd = "106";
            if (certifKindCd == null) return "UNIDENTIFIED CERTIFKINDCD";

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch(Exception ex) {
                log.debug(ex);
            }

            return name;
        }
        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode) || isCollege();
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}//クラスの括り
