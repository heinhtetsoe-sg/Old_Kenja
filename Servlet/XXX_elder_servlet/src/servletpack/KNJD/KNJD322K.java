package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ３２２＞  追試試験結果報告書（進級・卒業用）
 *
 *  2005/01/18 nakamoto 新規作成
 *                      フォームは１ページ当り３０行(生徒)１７列(科目)とする
 *  2005/01/21 nakamoto 選科数の列は、データがなければ表示しない。NO001
 *  2006/10/16 nakamoto NO002:項目名「判定」を「科目数」に変更した。
 **/

public class KNJD322K {

    /* ロギング */
    private static final Log log = LogFactory.getLog(KNJD322K.class);
    
    private String _useCurriculumcd;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[10];

    //  パラメータの取得
        String classcd[] = request.getParameterValues("GRADE");             //学年
        try {
            param[0] = request.getParameter("YEAR");                                //年度
            param[1] = request.getParameter("GAKKI");                               //学期
            //param[2] = request.getParameter("SUBCLASS");                              //科目数
            //param[3] = request.getParameter("OUTPUT");                            //帳票選択（1:以上,2:未満）
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        } catch( Exception ex ) {
            log.warn("parameter error!");
        }

    //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);          //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!", ex);
            return;
        }
        log.fatal(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");

    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
        if (log.isDebugEnabled())
            for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));       //追試験対象者
            ps2 = db2.prepareStatement(Pre_Stat2(param));       //追試験対象科目
            ps3 = db2.prepareStatement(Pre_Stat3(param));       //追試験データ（素点、勤怠、判定類型、類型評定下限値）
            ps4 = db2.prepareStatement(Pre_Stat4(param));       //科目数
        } catch( Exception ex ) {
            log.warn("db2.prepareStatement error!");
        }
        //SVF出力
        for (int ia=0; ia<classcd.length; ia++) {
            SetTitle(svf,param,classcd[ia]);                    //見出し出力タイトルのメソッド
            if( printMain(db2,svf,param,classcd[ia],ps1,ps2,ps3,ps4) )nonedata = true;
        }

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        Pre_Stat_f(ps1,ps2,ps3,ps4);//preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 見出し項目（年度・作成日・プログラムＩＤ） **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJD_KYOTU2.frm", 4);
        svf.VrsOut("NENDO2",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");   //年度
        svf.VrsOut("PRGID","KNJD322");//プログラムＩＤ

    //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("GRADE","FF=1");

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
    //  コメント
        svf.VrsOut("NOTE1","(公)公欠　　(欠)欠席　　(未)追試結果が未入力の場合");
    //  学期の取得
        try {
            returnval = getinfo.Semester(db2,param[0],param[1]);
            param[6] = returnval.val2;                      //学期開始日
            param[7] = returnval.val3;                      //学期終了日
        } catch( Exception e ){
            System.out.println("Semester name get error!");
            System.out.println( e );
        }
        getinfo = null;
        returnval = null;

    }//Set_Head()の括り



    /** 見出し項目（タイトル） **/
    private void SetTitle(Vrw32alp svf,String param[],String classcd) {

        try {
            svf.VrsOut("TITLE2" ,   "追試験結果報告書");//タイトル
            svf.VrsOut("SEMESTER2", String.valueOf(Integer.parseInt(classcd)) + "学年");//学年
        } catch( Exception e ){
            log.warn("SetTitle error!", e);
        }

    }//SetTitle()の括り



    /** 該当学年の追試験対象者を印刷する **/
    private boolean printMain(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        String classcd,
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4
    ) {
        boolean nonedata = false;

        try {
            int pp = 0;
            ps1.setString(++pp,classcd);    //学年
            ResultSet rs = ps1.executeQuery();
            svf.VrsOut("GRADE", classcd);                           //学年（改ページ用）

            Map hm1 = new HashMap();                                        //学籍番号と行番号の保管
            Map hm2 = new HashMap();                                        //学籍番号と番号０の保管（留年対象者）
            int schno = 0;
            while( rs.next() ){
                hm1.put(rs.getString("SCHREGNO"),new Integer(++schno));     //行番号に学籍番号を付ける
                setSchregData(svf,rs,schno);                                //生徒名等出力のメソッド
                if( schno==1 )param[4] = rs.getString("ATTENDNO2");         //開始生徒
                param[5] = rs.getString("ATTENDNO2");                       //終了生徒
                if (schno == 30) {
                    printSubclassName(db2,svf,param,hm1,hm2,ps2,ps3,ps4,classcd);       //科目出力のメソッド
                    Svf_Int(svf);                                           //SVFフィールド初期化
                    hm1.clear();                                            //行番号情報を削除
                    hm2.clear();                                            //番号０情報を削除（留年対象者）
                    schno = 0;
                    param[4] = null;                                        //開始生徒
                    param[5] = null;                                        //終了生徒
                }
                nonedata = true;
            }
            rs.close();
            db2.commit();
            if (schno > 0) {
                printSubclassName(db2,svf,param,hm1,hm2,ps2,ps3,ps4,classcd);       //科目出力のメソッド
                Svf_Int(svf);                                               //SVFフィールド初期化
            }
        } catch( Exception ex ) {
            log.warn("printMain error!", ex);
        }
        return nonedata;

    }//boolean printMain()の括り


    /** 生徒名等出力 **/
    private void setSchregData(Vrw32alp svf,ResultSet rs,int ia){

        try {
            svf.VrsOutn("HR_CLASS",ia ,rs.getString("HR_NAMEABBV"));            //組略称
            svf.VrsOutn("ATTENDNO",ia ,rs.getString("ATTENDNO"));               //出席番号
            svf.VrsOutn("NAME"    ,ia ,rs.getString("NAME"));                   //生徒名
        } catch( Exception ex ){
            log.warn("setSchregData error!", ex);
        }

    }//setSchregData()の括り


    /** 科目出力 **/
    private void printSubclassName(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        Map hm1,
        Map hm2,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4,
        String classcd
    ) {
        try {
            int pp = 0;
            ps2.setString(++pp,classcd);            //学年
            ResultSet rs = ps2.executeQuery();      //科目表のレコードセット
            if (log.isDebugEnabled()) {
                log.debug("param[4]="+param[4]);
                log.debug("param[5]="+param[5]);
            }

            int lcount = 0;
            int numbercount = 0;    //科目数出力フラグ
            while( rs.next() ){
                if ((numbercount == 0) && (rs.getInt("ELECTDIV") == 1)) {
                    printSubclassJudge(db2,svf,param,hm1,hm2,ps4,classcd);      //判定欄出力のメソッド
                    svf.VrEndRecord();
                    lcount++;
                    numbercount++;
                }
                setSubclassName(svf,rs);                                                        //科目名等出力のメソッド
                printScore(db2,svf,param,hm1,hm2,ps3,classcd,rs.getString("SUBCLASSCD"));       //得点出力のメソッド
                svf.VrEndRecord();
                lcount++;
            }
            rs.close();
            db2.commit();
            //空列の出力-->学年で改ページ
            if (lcount > 0) {
                //選科数の列は、データがなければ表示しない。NO001
                if (numbercount == 0) {
                    printSubclassJudge(db2,svf,param,hm1,hm2,ps4,classcd);      //判定欄出力のメソッド
                } else {
                    printSubclassNumber(db2,svf,param,hm1,ps4,classcd);         //選択科目数出力のメソッド
                }
                svf.VrEndRecord();
                lcount++;
                for( ; lcount%17>0 ; lcount++ )svf.VrEndRecord();
            }
        } catch( Exception ex ) {
            log.warn("printSubclassName error!", ex);
        }

    }//printSubclassName()の括り



    /** 科目名出力 **/
    private void setSubclassName(Vrw32alp svf,ResultSet rs){

        try {
            int electdiv = rs.getInt("ELECTDIV");                   //選択区分
            //科目マスタの選択区分＝１の時、科目名を網掛けにする。
            if (electdiv > 0) svf.VrAttribute("SUBCLASS1"   ,"Paint=(2,60,1),Bold=1");      //網掛け
                              svf.VrAttribute("SUBCLASS1"       ,"Hensyu=3");//中央表示
                              svf.VrsOut("SUBCLASS1"            ,rs.getString("SUBCLASSABBV")); //科目名
            if (electdiv > 0) svf.VrAttribute("SUBCLASS1"   ,"Paint=(0,0,0),Bold=0");       //網掛けクリア
        } catch( Exception ex ){
            log.warn("setSubclassName error!", ex);
        }

    }//setSubclassName()の括り



    /** 得点出力 **/
    private void printScore(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        Map hm1,
        Map hm2,
        PreparedStatement ps3,
        String classcd,
        String subclasscd
    ) {
        try {
            int pp = 0;
            ps3.setString(++pp,subclasscd);         //科目コード
            ps3.setString(++pp,classcd);            //学年
            ps3.setString(++pp,param[4]);           //開始生徒
            ps3.setString(++pp,param[5]);           //終了生徒
            ResultSet rs = ps3.executeQuery();      //科目表のレコードセット

            int schno = 0;
            while( rs.next() ){
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
                if (int1 == null) continue;
                //網掛け（評価１または勤怠）
                if (rs.getString("JUDGE") != null || rs.getString("DI_MARK") != null) {
                    svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3,Paint=(2,60,1),Bold=1");
                    hm2.put(rs.getString("SCHREGNO"),new Integer(schno));       //番号0に学籍番号を付ける（留年対象者）
                } else {
                    svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3,Paint=(0,0,0),Bold=0");
                }
                //得点
                if (rs.getString("SCORE") != null) {
                    String tokuten = rs.getString("SCORE") + "(" + rs.getString("JUDGE_PATTERN") + ")";
                    svf.VrsOutn("POINT1", int1.intValue(), tokuten);    //素点
                } else if (rs.getString("DI_MARK") != null) {
                    String di_mark = (rs.getString("DI_MARK").equals("KK")) ? "(公)" : "(欠)";
                    svf.VrsOutn("POINT1", int1.intValue(), di_mark);    //勤怠
                } else {
                    svf.VrsOutn("POINT1", int1.intValue(), "(未)");  //素点と勤怠が未入力
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printScore error!", ex);
        }

    }//printScore()の括り



    /** 判定欄出力 **/
    private void printSubclassJudge(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        Map hm1,
        Map hm2,
        PreparedStatement ps4,
        String classcd
    ) {
        try {
            int pp = 0;
            ps4.setString(++pp,classcd);            //学年
            ps4.setString(++pp,param[4]);           //開始生徒
            ps4.setString(++pp,param[5]);           //終了生徒
            ps4.setString(++pp,"0");                //選択区分
            ResultSet rs = ps4.executeQuery();      //科目数のレコードセット

            svf.VrAttribute("SUBCLASS1"     ,"Hensyu=3");//中央表示
            svf.VrsOut("SUBCLASS1"          ,"科目数");    //科目数---NO002
//          svf.VrsOut("SUBCLASS1"          ,"判定"); //科目数

            String hantei = "";     //判定記号'*'
            while( rs.next() ){
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
                if (int1 == null) continue;
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int2 = (Integer)hm2.get(rs.getString("SCHREGNO"));
                if (int2 == null)   hantei = "   ";
                else                hantei = "  *";
                //svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3");//中央表示
                svf.VrsOutn("POINT1", int1.intValue(), hantei + rs.getString("SUB_CNT"));//科目数
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSubclassJudge error!", ex);
        }

    }//printSubclassJudge()の括り



    /** 科目数出力 **/
    private void printSubclassNumber(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        Map hm1,
        PreparedStatement ps4,
        String classcd
    ) {
        try {
            int pp = 0;
            ps4.setString(++pp,classcd);            //学年
            ps4.setString(++pp,param[4]);           //開始生徒
            ps4.setString(++pp,param[5]);           //終了生徒
            ps4.setString(++pp,"1");                //選択区分
            ResultSet rs = ps4.executeQuery();      //科目数のレコードセット

            svf.VrAttribute("SUBCLASS1"     ,"Hensyu=3");//中央表示
            svf.VrsOut("SUBCLASS1"          ,"選科数");    //科目数
            while( rs.next() ){
                //学籍番号（生徒）に対応した行にデータをセットする。
                Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
                if (int1 == null) continue;
                svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3");//中央表示
                svf.VrsOutn("POINT1", int1.intValue(), rs.getString("SUB_CNT"));//科目数
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printSubclassNumber error!", ex);
        }

    }//printSubclassNumber()の括り



    /* 追試験対象者 */
    private String Pre_Stat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  学年をパラメータとする
        try {
            stb.append("SELECT DISTINCT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T2.NAME, ");
            stb.append("    T3.GRADE, ");
            stb.append("    T3.HR_CLASS, ");
            stb.append("    T3.ATTENDNO, ");
            stb.append("    T3.GRADE||T3.HR_CLASS||T3.ATTENDNO ATTENDNO2, ");
            stb.append("    T4.HR_NAMEABBV ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_HDAT T4, ");
            stb.append("    SCHREG_REGD_DAT T3, ");
            stb.append("    SUPP_EXA_DAT T1  ");
            stb.append("    LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR     = '"+param[0]+"' AND ");
            stb.append("    T3.SEMESTER = '"+param[1]+"' AND ");
            stb.append("    T3.GRADE    = ? AND ");
            stb.append("    T1.YEAR     = T3.YEAR AND ");
            stb.append("    T1.SCHREGNO = T3.SCHREGNO AND ");
            stb.append("    T1.YEAR     = T4.YEAR AND ");
            stb.append("    T3.SEMESTER = T4.SEMESTER AND ");
            stb.append("    T3.GRADE    = T4.GRADE AND ");
            stb.append("    T3.HR_CLASS = T4.HR_CLASS ");
            stb.append("ORDER BY ");
            stb.append("    T3.GRADE, ");
            stb.append("    T3.HR_CLASS, ");
            stb.append("    T3.ATTENDNO ");
        } catch( Exception e ){
            log.warn("Pre_Stat1 error!", e);
        }
        return stb.toString();

    }//Pre_Stat1()の括り


    /* 追試験対象科目 */
    String Pre_Stat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        //学年をパラメータとする
        try {
            stb.append("SELECT DISTINCT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T2.SUBCLASSABBV, ");
            stb.append("    VALUE(T2.ELECTDIV,'0') ELECTDIV ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT T3, SUPP_EXA_DAT T1  ");
            stb.append("    LEFT OUTER JOIN V_SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD AND T1.YEAR = T2.YEAR ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+param[0]+"' AND ");
            stb.append("    T1.YEAR = T3.YEAR AND ");
            stb.append("    T3.SEMESTER = '"+param[1]+"' AND ");
            stb.append("    T3.GRADE = ? AND ");
            stb.append("    T1.SCHREGNO = T3.SCHREGNO ");
            stb.append("ORDER BY ");
            stb.append("    VALUE(T2.ELECTDIV,'0'), ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD ");
        } catch( Exception e ){
            log.warn("Pre_Stat2 error!", e);
        }
        return stb.toString();

    }//Pre_Stat2()の括り


    /* 追試験データ（素点、勤怠、判定類型、類型評定下限値）*/
    String Pre_Stat3(String param[])
    {
        //科目、学年、出力生徒範囲をパラメータとする
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.SCORE, ");
            stb.append("    T1.DI_MARK, ");
            stb.append("    T1.JUDGE_PATTERN, ");
            stb.append("    T2.TYPE_ASSES_LOW, ");
            stb.append("    CASE WHEN T1.SCORE < T2.TYPE_ASSES_LOW THEN '*' ELSE NULL END JUDGE ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT T3, SUPP_EXA_DAT T1  ");
            stb.append("    LEFT OUTER JOIN TYPE_ASSES_MST T2 ON T1.YEAR = T2.YEAR AND  ");
            stb.append("                                         T1.JUDGE_PATTERN = T2.TYPE_ASSES_CD AND  ");
            stb.append("                                         T2.TYPE_ASSES_LEVEL = '2' ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+param[0]+"' AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD = ? AND ");
            stb.append("    T1.YEAR = T3.YEAR AND ");
            stb.append("    T3.SEMESTER = '"+param[1]+"' AND ");
            stb.append("    T3.GRADE = ? AND ");
            stb.append("    T3.GRADE||T3.HR_CLASS||T3.ATTENDNO >= ? AND ");
            stb.append("    T3.GRADE||T3.HR_CLASS||T3.ATTENDNO <= ? AND ");
            stb.append("    T1.SCHREGNO = T3.SCHREGNO ");
        } catch( Exception e ){
            log.warn("Pre_Stat3 error!", e);
        }
        return stb.toString();

    }//Pre_Stat3()の括り



    /* 生徒毎の科目数 */
    String Pre_Stat4(String param[])
    {
        //学年、出力生徒範囲・選択区分をパラメータとする
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    COUNT(*) AS SUB_CNT ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT T3, SUPP_EXA_DAT T1, SUBCLASS_MST T2 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+param[0]+"' AND ");
            stb.append("    T1.YEAR = T3.YEAR AND ");
            stb.append("    T3.SEMESTER = '"+param[1]+"' AND ");
            stb.append("    T3.GRADE = ? AND ");
            stb.append("    T3.GRADE||T3.HR_CLASS||T3.ATTENDNO >= ? AND ");
            stb.append("    T3.GRADE||T3.HR_CLASS||T3.ATTENDNO <= ? AND ");
            stb.append("    T1.SCHREGNO = T3.SCHREGNO AND ");
            stb.append("    VALUE(T2.ELECTDIV,'0') = ? AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    T2.CLASSCD = T1.CLASSCD AND ");
                stb.append("    T2.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                stb.append("    T2.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append("    T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("GROUP BY  ");
            stb.append("    T1.SCHREGNO ");
        } catch( Exception e ){
            log.warn("Pre_Stat4 error!", e);
        }
        return stb.toString();

    }//Pre_Stat4()の括り



    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3,PreparedStatement ps4)
    {
        try {
            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();
        } catch( Exception e ){
            log.warn("Preparedstatement-close error!", e);
        }
    }//Pre_Stat_f()の括り



    /**SVF-FORM-FIELD-INZ**/
    private void Svf_Int(Vrw32alp svf){

        for (int j=1; j<31; j++){
            svf.VrsOutn("HR_CLASS"      ,j  , "" );
            svf.VrsOutn("ATTENDNO"      ,j  , "" );
            svf.VrsOutn("NAME"          ,j  , "" );
        }

    }//Svf_Int()の括り



}//クラスの括り
