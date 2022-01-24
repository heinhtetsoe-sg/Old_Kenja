package servletpack.KNJB;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ０７０＞  学級別時間割一覧表
 *
 * 2005/05/02 nakamoto １６校時に対応
 * 2005/06/27 nakamoto 名称マスタの「B001」の予備2は、Null以外は時間割表で出力しない校時との意味で追加
 * 2006/01/20 nakamoto NO001 extends HttpServletをカット。Log4jに対応
 * 2006/01/20 nakamoto NO002 職員の変更（ダンスの先生）に対応。
 * 2006/04/21 nakamoto NO005 特別職員が設定されている場合、特別職員のみを表示するよう修正
 * 2006/07/07 nakamoto NO006 ＳＥＱが2桁以上の場合、帳票が出力されない不具合を修正。
 *                     ------パラメータを正常に取得できていなかった。
 *                     ------パラメータを分けて取得するようにした。
 * 2006/10/20 nakamoto NO007 「学期」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 *                     ------一週間の間に前期後期両方ある場合の対応である
 * 2006/10/24 nakamoto NO008 通常時間割：タイトル下の日付範囲は、学期範囲内を表示するよう修正した。
 * 2006/10/25 nakamoto NO009 通常時間割：「年度」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 * 2006/10/26 nakamoto NO010 SQL修正。SCH_DATの不要な部分をカットした。NO002,NO005など
 *                     NO011 SQL修正。ソート順に生徒名簿存在フラグを追加した。
 * 2006/11/06 nakamoto NO012 通常時間割：テスト時間割のコマに網掛けをする。また、注記を入れる。「■：テスト時間割」
 **/

public class KNJB070 {

    private static final Log log = LogFactory.getLog(KNJB070.class);//NO001

    Vrw32alp svf = new Vrw32alp();
    DB2UDB   db2;
    String dbname = new String();
    int ret;
    boolean nonedata  = false;

    final String GAKKYUU = "1";
    final String SHOKUIN = "2";
    final String SISETU = "3";
    String _kubun;
    String _kubunName;
    String _kubunName2;
    String _gradeIn;
    String _sectionCdName1;
    String _sectionCdName2;
    String _faccdName1;
    String _faccdName2;
    boolean _isJoudanSubclass;
    boolean _isJoudanChair;
    boolean _isGedanStaff;
    boolean _isGedanSisetu;
    boolean _isGedanClass;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意

        String param[] = new String[21];

    // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");          // データベース名
            if(dbname == null) dbname = "gakumudb";
            param[0] = request.getParameter("RADIO");            // 1:基本 2:通常
            if(param[0].equals("1")){
//                param[1] = strx.substring(0,4);                    //年度
//                param[2] = strx.substring(5,6);                    //ＳＥＱ
//                param[6] = strx.substring(7,8);                    //学期
                param[1] = request.getParameter("T_YEAR");        //年度 NO006
                param[2] = request.getParameter("T_BSCSEQ");    //ＳＥＱ NO006
                param[6] = request.getParameter("T_SEMESTER");    //学期 NO006
            } else{
                String strx = request.getParameter("DATE1");    //指定日付
                param[9] = strx.substring(0,4) + "-" + strx.substring(5,7) + "-" + strx.substring(8); // NO007
                //週始めの日付を取得
                int nen  = Integer.parseInt(strx.substring(0,4));
                int tuki = Integer.parseInt(strx.substring(5,7));
                int hi   = Integer.parseInt(strx.substring(8));
                param[1] = (tuki <= 3) ? Integer.toString(nen - 1) : Integer.toString(nen); // 年度の取得 NO009
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                while(cals.get(Calendar.DAY_OF_WEEK) != 2){
                    cals.add(Calendar.DATE,-1);
                }
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[3] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
//                //年度の取得
//                if(tuki >= 1 & tuki <= 3){
//                    param[1] = Integer.toString(nen - 1);    //年度
//                } else{
//                    param[1] = Integer.toString(nen);        //年度
//                }
                //週最終日の取得
                cals.add(Calendar.DATE,+6);
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[4] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
            }

            // 学年の指定
            String nen[] = request.getParameterValues("NEN");
            param[5] = "(";
            for(int i=0 ; i< nen.length ; i++){
                if(nen[i] == null ) break;
                if(i > 0) param[5] = param[5] + ",";
                param[5] = param[5] + "'" + nen[i] + "'";
            }
            param[5] = param[5] + ")";
            //出力項目(上段) 1:科目名 2:講座名
            param[18] = request.getParameter("SUBCLASS_CHAIR_DIV");
            //出力項目(下段) 1:職員名 2:施設名
            param[19] = request.getParameter("STAFF_SISETU_DIV");
            param[20] = request.getParameter("useCurriculumcd");

            //帳票区分
            _kubun = request.getParameter("KUBUN");
            _kubunName = GAKKYUU.equals(_kubun) ? "学級" : SHOKUIN.equals(_kubun) ? "職員" : "施設";
            _kubunName2 = GAKKYUU.equals(_kubun) ? "学　級" : SHOKUIN.equals(_kubun) ? "職　員" : "施　設";
            //学年
            _gradeIn = param[5];
            //所属cd
            _sectionCdName1 = request.getParameter("SECTION_CD_NAME1");
            _sectionCdName2 = request.getParameter("SECTION_CD_NAME2");
            //施設cd
            _faccdName1 = request.getParameter("FACCD_NAME1");
            _faccdName2 = request.getParameter("FACCD_NAME2");
            //上段・下段
            final String joudan = GAKKYUU.equals(_kubun) ? request.getParameter("SUBCLASS_CHAIR_DIV") : SHOKUIN.equals(_kubun) ? request.getParameter("K2SUBCLASS_CHAIR_DIV") : request.getParameter("K3SUBCLASS_CHAIR_DIV");
            final String gedan = GAKKYUU.equals(_kubun) ? request.getParameter("STAFF_SISETU_DIV") : SHOKUIN.equals(_kubun) ? request.getParameter("SISETU_CLASS_DIV") : request.getParameter("STAFF_CLASS_DIV");
            _isJoudanSubclass = "1".equals(joudan);
            _isJoudanChair = "2".equals(joudan);
            _isGedanStaff  = GAKKYUU.equals(_kubun) && "1".equals(gedan) || SISETU.equals(_kubun)  && "1".equals(gedan);
            _isGedanSisetu = GAKKYUU.equals(_kubun) && "2".equals(gedan) || SHOKUIN.equals(_kubun) && "1".equals(gedan);
            _isGedanClass  = SHOKUIN.equals(_kubun) && "2".equals(gedan) || SISETU.equals(_kubun)  && "2".equals(gedan);
        } catch( Exception ex ) {
            log.debug("parameter error!", ex);
        }

        for(int ia=0 ; ia<param.length ; ia++)    log.debug("param[" + ia + "]=" + param[ia]);

    // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.debug("DB2 open error!", ex);
        }

    // ＤＢ検索（コントロールマスター）
        /* 作成日の取得 */
        try {
            String sql = "SELECT CHAR(CTRL_DATE) FROM CONTROL_MST WHERE CTRL_NO='01'";
            db2.query(sql);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if( rs.next() ){
                String strx = (String)rs.getString(1);
                param[8] = strx.substring(0,10);
            }
            db2.commit();
        } catch( Exception e ){
            log.debug("sakuseibi error!", e);
        }

    // ＤＢ検索（基本時間割Ｈ）
        /* 期間の取得 */
        if(param[0].equals("1")){
            try {
                String sql = "SELECT TITLE "
                           + "FROM SCH_PTRN_HDAT "
                           + "WHERE YEAR = '" + param[1] + "' AND BSCSEQ = " + param[2] + " AND SEMESTER = '" + param[6] + "'";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if( rs.next() ){
                    param[10] = rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.debug("SCH_PTRN_HDAT error!", e);
            }
        }


    // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        ret = svf.VrInit();                           //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

    /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理       
      -----------------------------------------------------------------------------*/
    // ＳＶＦフォーム出力
        ret = svf.VrSetForm("KNJB070.frm", 4);
        if(param[0].equals("2")) get_semester(param);
        set_head(param);
        set_chapter1(param);

        /*該当データ無し*/
        if(nonedata == false){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            ret = svf.VrEndPage();
        }

    // 終了処理
        db2.close();        // DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 

    }    //doGetの括り




    /*------------------------------------*
     * 学級別時間割表ＳＶＦ出力           *
     *------------------------------------*/
    public void set_chapter1(String param[])
                     throws ServletException, IOException
    {
        try {
            db2.query(Pre_Stat3(param));
            ResultSet rs = db2.getResultSet();
//            log.debug("sql = "+Pre_Stat3(param));
            log.debug("set_chapter1 sql ok!");

                /** SVFフォームへデータをセット **/
            String g_hr_class = new String();
            g_hr_class = "0";        // 学年＋組
            String cmpData = ""; //比較用
            int gyo = 0;             //行No
            int datadiv[][][]       = new int[7][36][30];       //テスト NO012
            int groupcd[][][]       = new int[7][36][30];       //群コード NO003
            String subclass[][][]   = new String[7][36][30];    //科目名 NO003
            String staff[][][]      = new String[7][36][30];    //講座職員名
            String monthday[]       = {param[11],param[12],param[13],param[14],param[15],param[16],param[17]};//月日
            String monthday1[]      = {"(MON)","(TUE)","(WED)","(THU)","(FRI)","(SAT)","(SUN)"};//月日(基本)・・・枠線を表示するために必要
            String day[]            = {"月","火","水","木","金","土","日"};//曜日
            String period[]         = new String[36];           //校時 NO003

            while( rs.next() ){
                if (GAKKYUU.equals(_kubun)) {
                    cmpData = rs.getString("GRADE") + rs.getString("HR_CLASS");
                } else if (SHOKUIN.equals(_kubun)) {
                    cmpData = rs.getString("STAFFCD");
                } else if (SISETU.equals(_kubun)) {
                    cmpData = rs.getString("FACCD");
                }
                //学年＋組のブレイク時
                if (!g_hr_class.equals(cmpData)) {
                    if (!g_hr_class.equals("0")) {
                        //３０名分出力
                        if (gyo == 30) {
                            int ret_brank = 0;//空列カウント
                            for (int ia = 0; ia < 7; ia++) {
                                for (int ib = 0; ib < 36; ib++) {//NO003
                                    int flg = 0;
                                    for (int ic = 0; ic < 30; ic++) {
                                        if (subclass[ia][ib][ic] != null) flg = 1;//パターン１
                                        ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリア
                                        if (groupcd[ia][ib][ic] > 0) 
                                            ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,1),Bold=1");//ペイント：群名称
                                        if (datadiv[ia][ib][ic] == 2) //NO012
                                            ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テストNO012
                                        ret=svf.VrsOutn("SUBCLASS"  ,ic+1 , subclass[ia][ib][ic]);        //科目名
                                        ret=svf.VrsOutn("CHR_STAFF" ,ic+1 , staff[ia][ib][ic]);        //講座職員名
                                    }
                                    if (flg == 1) {
                                        if (param[0].equals("2")) ret=svf.VrsOut("DATE", monthday[ia]); //月日
                                        if (param[0].equals("1")) ret=svf.VrsOut("DATE", monthday1[ia]); //月日
                                        ret=svf.VrsOut("WEEK"           , day[ia]);                        //曜日
                                        ret=svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                                        ret = svf.VrAttribute("MARK2_1",  "Paint=(2,70,1),Bold=1");//NO012 群名称
                                        if (param[0].equals("2")) 
                                            ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//NO012 テスト
                                        ret = svf.VrEndRecord();//１列出力
                                        ret_brank++;//空列カウント
                                    }
                                }
                            }
                            //空列出力
                            for ( ; ret_brank%42 > 0; ret_brank++) ret = svf.VrEndRecord();
                            //初期化
                            gyo = 0;
                            for (int ia = 0; ia < 7; ia++) 
                                for (int ib = 0; ib < 36; ib++) //NO003
                                    for (int ic = 0; ic < 30; ic++) {
                                        groupcd[ia][ib][ic] = 0;        //群コード
                                        subclass[ia][ib][ic] = null;    //科目名
                                        staff[ia][ib][ic] = null;    //講座職員名
                                        datadiv[ia][ib][ic] = 0;        //テスト NO012
                                    }
                            for (int i = 0; i < 30; i++) {
                                ret=svf.VrsOutn("gakkyuu"    ,i+1    ,"");     //学級
                                ret=svf.VrsOutn("HR_STAFF"    ,i+1    ,"");     //学級担任名
                            }
                        }
                    }
                    g_hr_class = cmpData;
                    gyo++;
                }
                //明細項目セット
                if (GAKKYUU.equals(_kubun)) {
                    ret = svf.VrsOutn("gakkyuu"     ,gyo    ,rs.getString("TARGETCLASS"));      //学級
                    ret = svf.VrsOutn("HR_STAFF"    ,gyo    ,rs.getString("HR_STAFFNAME"));     //学級担任名
                } else if (SHOKUIN.equals(_kubun)) {
                    ret = svf.VrsOutn("gakkyuu"     ,gyo    ,rs.getString("SECTIONABBV"));
                    ret = svf.VrsOutn("HR_STAFF"    ,gyo    ,rs.getString("STAFFNAME"));
                } else if (SISETU.equals(_kubun)) {
                    ret = svf.VrsOutn("HR_STAFF"    ,gyo    ,rs.getString("FACILITYNAME"));
                }

                int iday = rs.getInt("DAYCD");
                int iper = rs.getInt("PERIODCD");
                if (iday == 1) iday = 8;

                period[iper] = rs.getString("ABBV1");
                int group_cd = rs.getInt("GROUPCD");
                groupcd[iday-2][iper][gyo-1] = group_cd;
                subclass[iday-2][iper][gyo-1] = (group_cd > 0) ? rs.getString("GROUPNAME") : rs.getString("SUBCLASSABBV");
                staff[iday-2][iper][gyo-1] = rs.getString("GEDAN_NAME");
                datadiv[iday-2][iper][gyo-1] = rs.getInt("DATADIV"); //テスト NO012
            }
            //最後のレコード出力
            if (!g_hr_class.equals("0")) {
                int ret_brank = 0;//空列カウント
                for (int ia = 0; ia < 7; ia++) {
                    for (int ib = 0; ib < 36; ib++) {//NO003
                        int flg = 0;
                        for (int ic = 0; ic < 30; ic++) {
                            if (subclass[ia][ib][ic] != null) flg = 1;//パターン１
                            ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリア
                            if (groupcd[ia][ib][ic] > 0) 
                                ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,1),Bold=1");//ペイント
                            if (datadiv[ia][ib][ic] == 2) //NO012
                                ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テストNO012
                            ret=svf.VrsOutn("SUBCLASS"  ,ic+1 , subclass[ia][ib][ic]);        //科目名
                            ret=svf.VrsOutn("CHR_STAFF" ,ic+1 , staff[ia][ib][ic]);        //講座職員名
                        }
                        if (flg == 1) {
                            if (param[0].equals("2")) ret=svf.VrsOut("DATE", monthday[ia]); //月日
                            if (param[0].equals("1")) ret=svf.VrsOut("DATE", monthday1[ia]); //月日
                            ret=svf.VrsOut("WEEK"           , day[ia]);                        //曜日
                            ret=svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                            ret = svf.VrAttribute("MARK2_1",  "Paint=(2,70,1),Bold=1");//NO012 群名称
                            if (param[0].equals("2")) 
                                ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//NO012 テスト
                            ret = svf.VrEndRecord();//１列出力
                            ret_brank++;//空列カウント
                        }
                    }
                }
                //空列出力
                for ( ; ret_brank%42 > 0; ret_brank++) ret = svf.VrEndRecord();
                nonedata  = true; //該当データなしフラグ
            }
            db2.commit();
            log.debug("set_chapter1 read ok!");
        } catch( Exception ex ) {
            log.debug("set_chapter1 read error!", ex);
        }

    }  //set_chapter1の括り


    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void set_head(String param[])
                     throws ServletException, IOException
    {
        try {
            ret = svf.VrsOut("nendo"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度　" + _kubunName + "別時間割表");
            ret = svf.VrsOut("CLASS_HEADER", _kubunName2);
            ret = svf.VrsOut("sakusei_hi"    , KNJ_EditDate.h_format_JP(param[8]));
//            ret = svf.VrAttribute("MARK2_1",  "Paint=(2,70,1),Bold=1");//NO012 群名称
            ret = svf.VrsOut("MARK2_1",  "  " );//NO012 群名称
            //基本時間割
            if(param[0].equals("1")){
                ret = svf.VrsOut("TITLE"    , "(" + param[10] + ")");
            //通常時間割
            } else{
//                ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//NO012 テスト
                ret = svf.VrsOut("MARK1_1",  "  " );//NO012 テスト
                ret = svf.VrsOut("MARK1_2",  "　：テスト時間割" );//NO012 テスト

                ret=svf.VrsOut("term"       , "(" + KNJ_EditDate.h_format_JP(param[3]) + " \uFF5E " + KNJ_EditDate.h_format_JP(param[4]) + ")");
                //週の日付
                int nen  = Integer.parseInt(param[7].substring(0,4)); // NO008 param[3]→param[7]
                int tuki = Integer.parseInt(param[7].substring(5,7)); // NO008 param[3]→param[7]
                int hi   = Integer.parseInt(param[7].substring(8)); // NO008 param[3]→param[7]
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                for(int ia=0 ; ia<7 ; ia++){
                    param[11+ia] = "(" + tuki + "/" + hi + ")";
                    if(ia != 6){
                        cals.add(Calendar.DATE,+1);
                        nen  = cals.get(Calendar.YEAR);
                        tuki = cals.get(Calendar.MONTH);
                        tuki++;
                        hi   = cals.get(Calendar.DATE);
                    }
                }
            }
            log.debug("set_head read ok!");
        } catch( Exception ex ) {
            log.debug("set_head read error!", ex);
        }

    }  //set_headの括り


    /*--------------------*
     * 学期取得           *
     *--------------------*/
    public void get_semester(String param[])
                     throws ServletException, IOException
    {
        try {
            //指定日付を日付型へ変換
            String strx = new String();
            strx = "9";

            //学期期間の取得 NO008Modify
            String sql = new String();
            sql = "SELECT SEMESTER "
                + "      ,case when '"+param[3]+"' < SDATE then SDATE else null end as SDATE "
                + "      ,case when '"+param[4]+"' > EDATE then EDATE else null end as EDATE "
                + "FROM SEMESTER_MST "
                + "WHERE SDATE <= ? AND EDATE >= ? AND YEAR=? AND SEMESTER<>?";

            PreparedStatement ps = db2.prepareStatement(sql);
            ps.setString(1, param[9]); // NO007 param[3]→param[9]
            ps.setString(2, param[9]); // NO007 param[3]→param[9]
            ps.setString(3, param[1]);
            ps.setString(4, strx);
            ResultSet rs = ps.executeQuery();

            param[7] = param[3]; //週開始日 NO008Add
            if( rs.next() ){
                param[6] = rs.getString("SEMESTER"); //学期
                if (rs.getString("SDATE") != null) param[3] = rs.getString("SDATE"); //学期開始日 NO008Add
                if (rs.getString("EDATE") != null) param[4] = rs.getString("EDATE"); //学期終了日 NO008Add
            }
            if(param[6] == null) param[6] = "0";
            ps.close();
            rs.close();
            db2.commit();
            log.debug("get_semester ok!");
        } catch( Exception e ){
            log.debug("get_semester error!", e);
        }
    }  //get_semesterの括り



    /*----------------*
     * 月の前ゼロ挿入 *
     *----------------*/
    public String h_tuki(int intx)
                     throws ServletException, IOException
    {
        String strx = null;
        try {
            strx = "00" + String.valueOf(intx);
            strx = strx.substring(strx.length()-2);
        } catch( Exception ex ) {
            log.debug("h_tuki error!", ex);
        }
        return strx;
    }  //h_tukiの括り


    /**学級別時間割表**/
    private String Pre_Stat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");//2005.06.27
            //特別職員
            stb.append(",SCH_STF AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT ");
                stb.append("    YEAR, ");
                stb.append("    SEMESTER, ");
                stb.append("    BSCSEQ, ");
                stb.append("    DAYCD, ");
                stb.append("    PERIODCD, ");
                stb.append("    CHAIRCD, ");
                stb.append("    max(STAFFCD) AS STAFFCD ");
                stb.append("FROM ");
                stb.append("    SCH_PTRN_STF_DAT ");
                stb.append("WHERE ");
                stb.append("    YEAR = '"+param[1]+"' ");
                stb.append("GROUP BY ");
                stb.append("    YEAR, ");
                stb.append("    SEMESTER, ");
                stb.append("    BSCSEQ, ");
                stb.append("    DAYCD, ");
                stb.append("    PERIODCD, ");
                stb.append("    CHAIRCD ");
            } else {                                //通常
                stb.append("SELECT ");
                stb.append("    EXECUTEDATE, ");
                stb.append("    PERIODCD, ");
                stb.append("    CHAIRCD, ");
                stb.append("    max(STAFFCD) AS STAFFCD ");
                stb.append("FROM ");
                stb.append("    SCH_STF_DAT ");
                stb.append("GROUP BY ");
                stb.append("    EXECUTEDATE, ");
                stb.append("    PERIODCD, ");
                stb.append("    CHAIRCD ");
            }
            stb.append("    ) ");
            //講座職員
            stb.append(",CHAIR_STF AS ( ");
            stb.append("    SELECT ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        CHAIRCD, ");
            stb.append("        max(STAFFCD) AS STAFFCD ");
            stb.append("    FROM ");
            stb.append("        CHAIR_STF_DAT ");
            stb.append("    WHERE ");
            stb.append("        YEAR = '"+param[1]+"' ");
            stb.append("    GROUP BY ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        CHAIRCD ");
            stb.append("    ) ");
            //講座施設
            stb.append(",CHAIR_FAC AS ( ");
            stb.append("    SELECT ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        CHAIRCD, ");
            stb.append("        max(FACCD) AS FACCD ");
            stb.append("    FROM ");
            stb.append("        CHAIR_FAC_DAT ");
            stb.append("    WHERE ");
            stb.append("        YEAR = '"+param[1]+"' ");
            stb.append("    GROUP BY ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        CHAIRCD ");
            stb.append("    ) ");
            //講座クラスMIN
            stb.append(",CHAIR_CLS_MIN AS ( ");
            stb.append("    SELECT W1.YEAR,W1.SEMESTER,W2.CHAIRCD,MIN(W1.TRGTGRADE||W1.TRGTCLASS) AS TRGTGRCL ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[6]+"' ");
            stb.append("    GROUP BY W1.YEAR,W1.SEMESTER,W2.CHAIRCD ) ");
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");
                stb.append("       ,W4.SUBCLASSCD,W4.GROUPCD,'0' AS DATADIV "); // NO012 データ区分 2:テスト
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
                stb.append("       ,W4.CHAIRABBV,W2.FACCD,W6.TRGTGRCL ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                stb.append("       LEFT JOIN SCH_STF W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD,W1.DATADIV "); // NO012 データ区分 2:テスト
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
                stb.append("       ,W4.CHAIRABBV,W2.FACCD,W6.TRGTGRCL ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                stb.append("       LEFT JOIN SCH_STF W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                W5.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                      W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W2.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_STF W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                      W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_CLS_MIN W6 ON (W6.YEAR = W1.YEAR AND ");
            stb.append("                                      W6.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W6.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[6]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
            } else {                      //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND  ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座クラス
            stb.append(",CHAIR_CLS AS ( ");
            stb.append("    SELECT W2.CHAIRCD,W1.TRGTGRADE,W1.TRGTCLASS,W1.YEAR,W1.SEMESTER ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[6]+"' ");
            if (GAKKYUU.equals(_kubun)) {
                stb.append("           AND W1.TRGTGRADE IN "+_gradeIn+" ");
            }
            stb.append("           ) ");
            //講座生徒 NO011Add
            stb.append(",EXISTS_SCHNO AS ( ");
            stb.append("    select distinct chaircd, grade, hr_class ");
            stb.append("    from chair_std_dat w1 ");
            stb.append("         inner join schreg_regd_dat w2 on w2.year=w1.year ");
            stb.append("                                     and w2.semester=w1.semester ");
            stb.append("                                     and w2.schregno=w1.schregno ");
            if (GAKKYUU.equals(_kubun)) {
                stb.append("                                     and w2.grade IN "+_gradeIn+" ");
            }
            stb.append("    where w1.year='"+param[1]+"' and w1.semester='"+param[6]+"' ) ");
            if (SHOKUIN.equals(_kubun)) {
                stb.append(",SECTION AS ( ");
                stb.append("    SELECT W3.CHAIRCD,W2.SECTIONCD,W2.STAFFCD,W2.STAFFNAME,W1.SECTIONABBV ");
                stb.append("    FROM   V_SECTION_MST W1,V_STAFF_MST W2,CHAIR_STF_DAT W3 ");
                stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND ");
                stb.append("           W1.SECTIONCD BETWEEN '"+_sectionCdName1+"' AND '"+_sectionCdName2+"' AND ");
                stb.append("           W2.YEAR=W1.YEAR AND ");
                stb.append("           W2.SECTIONCD=W1.SECTIONCD AND ");
                stb.append("           W3.YEAR=W2.YEAR AND ");
                stb.append("           W3.SEMESTER='"+param[6]+"' AND ");
                stb.append("           W3.STAFFCD=W2.STAFFCD ) ");
            } else if (SISETU.equals(_kubun)) {
                stb.append(",FACILITY AS ( ");
                stb.append("    SELECT W2.CHAIRCD, W1.FACCD, W1.FACILITYNAME ");
                stb.append("    FROM   V_FACILITY_MST W1, CHAIR_FAC_DAT W2 ");
                stb.append("    WHERE  W1.YEAR='"+ param[1]+"' ");
                stb.append("      AND  W1.FACCD BETWEEN '"+_faccdName1+"' AND '"+_faccdName2+"' ");
                stb.append("      AND  W2.YEAR=W1.YEAR ");
                stb.append("      AND  W2.SEMESTER='"+param[6]+"' ");
                stb.append("      AND  W2.FACCD=W1.FACCD ");
                stb.append(" ) ");
            }

            //メイン
            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            stb.append("       ,CASE WHEN E1.CHAIRCD IS NULL THEN 0 ELSE 1 END AS SCHNO "); // NO011Add
            stb.append("       ,CASE WHEN T1.DATADIV = '2' THEN 2 ELSE 0 END AS DATADIV "); // NO012 データ区分 2:テスト
            stb.append("       ,T1.CHAIRCD ");
            if ("1".equals(param[20])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.GROUPCD ");
            if (GAKKYUU.equals(_kubun)) {
                stb.append("       ,L3.GRADE ");
                stb.append("       ,L3.HR_CLASS ");
                stb.append("       ,L3.HR_NAME AS TARGETCLASS ");
                stb.append("       ,(SELECT W1.STAFFNAME FROM STAFF_MST W1 WHERE W1.STAFFCD = L3.TR_CD1) AS HR_STAFFNAME ");
            } else if (SHOKUIN.equals(_kubun)) {
                stb.append("       ,S1.SECTIONCD ");
                stb.append("       ,S1.SECTIONABBV ");
                stb.append("       ,S1.STAFFCD ");
                stb.append("       ,S1.STAFFNAME ");
            } else if (SISETU.equals(_kubun)) {
                stb.append("       ,F1.FACCD ");
                stb.append("       ,F1.FACILITYNAME ");
            }
            if (_isJoudanSubclass) {
                stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSABBV ");
            } else {
                stb.append("       ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSABBV ");
            }
            stb.append("       ,L7.ABBV1 ");
            stb.append("       ,L8.GROUPNAME ");
            if (_isGedanStaff) {
                stb.append("       ,W2.STAFFNAME AS GEDAN_NAME ");
            } else if (_isGedanSisetu) {
                stb.append("       ,VALUE(L5.FACILITYABBV,'') AS GEDAN_NAME ");
            } else if (_isGedanClass) {
                stb.append("       ,CASE WHEN T1.GROUPCD > '0000' THEN L9.HR_NAMEABBV||'*' ");
                stb.append("             ELSE L9.HR_NAMEABBV END AS GEDAN_NAME ");
            }
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[20])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            stb.append("       LEFT JOIN STAFF_MST W2 ON W2.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L9 ON L9.YEAR='"+param[1]+"' AND  ");
            stb.append("                                        L9.SEMESTER='"+param[6]+"' AND  ");
            stb.append("                                        L9.GRADE||L9.HR_CLASS=T1.TRGTGRCL  ");
            stb.append("       ,CHAIR_CLS T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=T2.YEAR AND  ");
            stb.append("                                        L3.SEMESTER=T2.SEMESTER AND  ");
            stb.append("                                        L3.GRADE=T2.TRGTGRADE AND  ");
            stb.append("                                        L3.HR_CLASS=T2.TRGTCLASS ");
            stb.append("       INNER JOIN EXISTS_SCHNO E1 ON E1.CHAIRCD=T2.CHAIRCD AND  "); // NO011Add
            stb.append("                                    E1.GRADE=T2.TRGTGRADE AND  "); // NO011Add
            stb.append("                                    E1.HR_CLASS=T2.TRGTCLASS "); // NO011Add
            if (GAKKYUU.equals(_kubun)) {
                stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
            } else if (SHOKUIN.equals(_kubun)) {
                stb.append("       ,SECTION S1 ");
                stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
                stb.append("  AND  T1.CHAIRCD=S1.CHAIRCD  ");
            } else if (SISETU.equals(_kubun)) {
                stb.append("       ,FACILITY F1 ");
                stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
                stb.append("  AND  T1.CHAIRCD=F1.CHAIRCD  ");
            }
            if (GAKKYUU.equals(_kubun)) {
                stb.append("ORDER BY L3.GRADE,L3.HR_CLASS, ");
            } else if (SHOKUIN.equals(_kubun)) {
                stb.append("ORDER BY S1.SECTIONCD,S1.STAFFCD, ");
            } else if (SISETU.equals(_kubun)) {
                stb.append("ORDER BY F1.FACCD, ");
            }
            stb.append("    T1.DAYCD,PERIODCD,SCHNO,T1.GROUPCD, ");
            if ("1".equals(param[20])) {
                stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
            } else {
                stb.append("       T1.SUBCLASSCD ");
            }
        } catch( Exception e ){
            log.debug("Pre_Stat3 error!", e);
        }
        return stb.toString();

    }//Pre_Stat3()の括り



}  //クラスの括り
