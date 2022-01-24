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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ０４０＞  担当教科別時間割一覧表
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
 * 2006/11/06 nakamoto NO010 通常時間割：テスト時間割のコマに網掛けをする。また、注記を入れる。「■：テスト時間割」
 **/

public class KNJB040 {

    private static final Log log = LogFactory.getLog(KNJB040.class);//NO001

    Vrw32alp svf = new Vrw32alp();     //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;                    // Databaseクラスを継承したクラス
    String dbname = new String();
    int ret;                          //リターン値
    boolean nonedata;                 //該当データなしフラグ

    String _useSchool_KindField;
    String _SCHOOLKIND;
    String use_prg_schoolkind;
    String selectSchoolKind;
    String selectSchoolKindSql;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 62358 $"); // CVSキーワードの取り扱いに注意

        String param[] = new String[25];  // 0-6:画面より受取  7:週最終日(2:週最初日） 10:作成日

    // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");          // データベース名
            if(dbname == null) dbname = "kenjadb";
            param[0] = request.getParameter("RADIO");                    // 1:基本 2:通常
            if(param[0].equals("1")){
//                param[1] = strx.substring(0,4);                            //年度
//                param[2] = strx.substring(5,6);                            //ＳＥＱ
//                param[15] = strx.substring(7,8);                            //学期
                param[1] = request.getParameter("T_YEAR");        //年度 NO006
                param[2] = request.getParameter("T_BSCSEQ");    //ＳＥＱ NO006
                param[15] = request.getParameter("T_SEMESTER");    //学期 NO006
            } else{
                String strx = request.getParameter("DATE");                //指定日付
                param[11] = strx.substring(0,4) + "-" + strx.substring(5,7) + "-" + strx.substring(8); // NO007
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
                param[2] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
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
                param[7] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
            }
            param[3] = request.getParameter("SECTION_CD_NAME1");        //所属from
            param[4] = request.getParameter("SECTION_CD_NAME2");           //所属to
            param[5] = request.getParameter("OUTPUT");                   //担当教科別時間割表出力
            param[6] = request.getParameter("OUTPUT2");                   //同時展開授業一覧表出力
            //出力項目 1:科目名 2:講座名
            param[23] = request.getParameter("SUBCLASS_CHAIR_DIV");
            param[24] = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                selectSchoolKindSql = stb.append("')").toString();
            }
        } catch( Exception ex ) {
            log.debug("parameter error!", ex);
        }

        for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);

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
                param[10] = strx.substring(0,10);
            }
            rs.close();
            ps.close();
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
                           + "WHERE YEAR = '" + param[1] + "' AND BSCSEQ = " + param[2] + " AND SEMESTER = '" + param[15] + "'";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if( rs.next() ){
                    param[14] = rs.getString(1);
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
        nonedata = false;

    // ＳＶＦフォーム出力
        if (param[0].equals("2")) get_semester(param);
        /*担当教科別時間割表*/
        if(param[5] != null){
            ret = svf.VrSetForm("KNJB040_1.frm", 4);
            set_head(param);
            set_chapter1(param);
        }
        /*同時展開授業一覧表*/
        if(param[6] != null){
            ret = svf.VrSetForm("KNJB040_2.frm", 4);
            set_head(param);
            set_chapter2(param);
        }

        /*該当データ無し*/
        if(nonedata == false){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            ret = svf.VrEndPage();
        }

        ret = svf.VrPrint();

    // 終了処理
        db2.close();        // DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 

    }    //doGetの括り


    /*------------------------------------*
     * 担当教科別時間割表ＳＶＦ出力       *
     *------------------------------------*/
    public void set_chapter1(String param[])
                     throws ServletException, IOException
    {
        try {

            db2.query(getCountPeriod());
            ResultSet rsCnt = db2.getResultSet();
            int cntPeriod = 36;
            while( rsCnt.next() ){
                cntPeriod = rsCnt.getInt("PERIODCD") + 1;
            }
            log.fatal("cntPeriod = "+cntPeriod);

            final String sql = Pre_Stat1(param);
            db2.query(sql);
            ResultSet rs2 = db2.getResultSet();
            log.fatal("sql2 ok!");

            String outputDyaPeriod[][] = new String[7][cntPeriod];    //出力列　曜日・校時
            while( rs2.next() ){
                int iday = rs2.getInt("DAYCD");
                int iper = rs2.getInt("PERIODCD");
                if (iday == 1) iday = 8;
                outputDyaPeriod[iday-2][iper] = String.valueOf(iday) + "-" + String.valueOf(iper);
            }

            final String sql2 = Pre_Stat1(param);
            db2.query(sql2);
            ResultSet rs = db2.getResultSet();
            log.fatal("sql ok!");

                /** SVFフォームへデータをセット **/
            String staff = "0";
            int totaljisu = 0;         //合計時数
            int gyo = 0;             //行No
            int datadiv[][][]       = new int[7][cntPeriod][40];       //テスト NO010
            String targetclass[][][] = new String[7][cntPeriod][40];    //対象クラス NO003
            String subclass[][][]   = new String[7][cntPeriod][40];    //科目名 NO003
            String chairName[][][]  = new String[7][cntPeriod][40];    //講座名
            String monthday[]       = {param[16],param[17],param[18],param[19],param[20],param[21],param[22]};//月日(通常)
            String monthday1[]      = {"(MON)","(TUE)","(WED)","(THU)","(FRI)","(SAT)","(SUN)"};//月日(基本)・・・枠線を表示するために必要
            String day[]            = {"月","火","水","木","金","土","日"};//曜日
            String period[]         = new String[cntPeriod];           //校時 NO003

            while( rs.next() ){
                if (!staff.equals(rs.getString("SECTIONCD") + rs.getString("STAFFCD"))){
                    if (!staff.equals("0")){
                        totaljisu = 0;
                        //４０名分出力
                        if (gyo == 40) {
                            int ret_brank = 0;//空列カウント
                            for (int ia = 0; ia < 7; ia++) {
                                for (int ib = 0; ib < cntPeriod; ib++) {//NO003
                                    int flg = 0;
                                    for (int ic = 0; ic < 40; ic++) {
                                        if (targetclass[ia][ib][ic] != null) flg = 1;//パターン１
                                        if (outputDyaPeriod[ia][ib] != null) flg = 2;//パターン2
                                        if ("2".equals(param[23])) {
                                            ret=svf.VrAttributen("CHAIRNAME"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリアNO010
                                            if (datadiv[ia][ib][ic] == 2) //NO010
                                                ret=svf.VrAttributen("CHAIRNAME"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テストNO010
                                            ret=svf.VrsOutn("CHAIRNAME" ,ic+1 , chairName[ia][ib][ic]);     //講座名
                                        } else {
                                            ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリアNO010
                                            if (datadiv[ia][ib][ic] == 2) //NO010
                                                ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テストNO010
                                            ret=svf.VrsOutn("CLASS"     ,ic+1 , targetclass[ia][ib][ic]);   //対象クラス
                                            ret=svf.VrsOutn("SUBCLASS"  ,ic+1 , subclass[ia][ib][ic]);      //科目名
                                        }
                                    }
                                    if (flg == 2) {
                                        if (param[0].equals("2")) ret=svf.VrsOut("DATE", monthday[ia]); //月日
                                        if (param[0].equals("1")) ret=svf.VrsOut("DATE", monthday1[ia]); //月日
                                        ret=svf.VrsOut("WEEK"           , day[ia]);                        //曜日
                                        ret=svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                                        if (param[0].equals("2")) 
                                            ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//NO010 テスト
                                        ret = svf.VrEndRecord();//１列出力
                                        ret_brank++;//空列カウント
                                    }
                                }
                            }
                            //空列出力
                            for ( ; ret_brank%42 > 0; ret_brank++) ret = svf.VrEndRecord();
                            //初期化
                            gyo = 0;
                            for (int ia = 0; ia < 7; ia++) {
                                for (int ib = 0; ib < cntPeriod; ib++) {//NO003
                                    for (int ic = 0; ic < 40; ic++) {
                                        targetclass[ia][ib][ic] = null;    //対象クラス
                                        subclass[ia][ib][ic] = null;    //科目名
                                        chairName[ia][ib][ic] = null;    //講座名
                                        datadiv[ia][ib][ic] = 0;        //テスト NO010
                                    }
                                }
                            }
                            for (int i = 0; i < 40; i++) {
                                ret=svf.VrsOutn("belong"    ,i+1    ,"");     //所属
                                ret=svf.VrsOutn("name"      ,i+1    ,"");    //氏名
                                ret = svf.VrsOutn("total"   ,i+1    ,"");     //合計時数
                            }
                        }
                    }
                    staff = rs.getString("SECTIONCD") + rs.getString("STAFFCD");
                    gyo++;
                }
                ret=svf.VrsOutn("belong"    ,gyo    ,rs.getString("SECTIONABBV"));     //所属
                ret=svf.VrsOutn("name"      ,gyo    ,rs.getString("STAFFNAME"));    //氏名
                totaljisu++;
                ret = svf.VrsOutn("total"   ,gyo    ,String.valueOf(totaljisu));     //合計時数

                int iday = rs.getInt("DAYCD");
                int iper = rs.getInt("PERIODCD");
                if (iday == 1) iday = 8;
                targetclass[iday-2][iper][gyo-1] = rs.getString("TARGETCLASS");
                subclass[iday-2][iper][gyo-1] = rs.getString("SUBCLASSABBV");
                chairName[iday-2][iper][gyo-1] = rs.getString("CHAIRABBV");
                datadiv[iday-2][iper][gyo-1] = rs.getInt("DATADIV"); //テスト NO010
                period[iper] = rs.getString("ABBV1");
            }
            log.fatal("while ok!");
            //最後のレコード出力
            if (!staff.equals("0")){
                int ret_brank = 0;//空列カウント
                for (int ia = 0; ia < 7; ia++) {
                    for (int ib = 0; ib < cntPeriod; ib++) {//NO003
                        int flg = 0;
                        for (int ic = 0; ic < 40; ic++) {
                            if (targetclass[ia][ib][ic] != null) flg = 1;//パターン１
                            if (outputDyaPeriod[ia][ib] != null) flg = 2;//パターン2
                            if ("2".equals(param[23])) {
                                ret=svf.VrAttributen("CHAIRNAME"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリアNO010
                                if (datadiv[ia][ib][ic] == 2) //NO010
                                    ret=svf.VrAttributen("CHAIRNAME"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テストNO010
                                ret=svf.VrsOutn("CHAIRNAME" ,ic+1 , chairName[ia][ib][ic]);       //講座名
                            } else {
                                ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリアNO010
                                if (datadiv[ia][ib][ic] == 2) //NO010
                                    ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テストNO010
                                ret=svf.VrsOutn("CLASS"     ,ic+1 , targetclass[ia][ib][ic]);    //対象クラス
                                ret=svf.VrsOutn("SUBCLASS"  ,ic+1 , subclass[ia][ib][ic]);        //科目名
                            }
                        }
                        if (flg == 2) {
                            if (param[0].equals("2")) ret=svf.VrsOut("DATE", monthday[ia]); //月日
                            if (param[0].equals("1")) ret=svf.VrsOut("DATE", monthday1[ia]); //月日
                            ret=svf.VrsOut("WEEK"           , day[ia]);                        //曜日
                            ret=svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                            if (param[0].equals("2")) 
                                ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//NO010 テスト
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
            log.fatal("read ok!");
        } catch( Exception ex ) {
            log.error("set_chapter1 read error!", ex);
        }

    }  //set_chapter1の括り


    /*------------------------------------*
     * 同時展開授業一覧表ＳＶＦ出力       *
     *------------------------------------*/
    public void set_chapter2(String param[])
                     throws ServletException, IOException
    {
        try {
            StringBuffer stb = new StringBuffer();
            if(param[0].equals("1")){
                stb.append(" SELECT DISTINCT ");
                stb.append(" T1.GROUPCD AS GROUPCD, ");
                stb.append(" T1.CHAIRCD AS CHAIRCD, ");
                stb.append(" MAX(T5.GROUPNAME) AS GROUPNAME, ");
                if ("1".equals(param[24])) {
                    stb.append(" T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD AS SUBCLASSCD, ");
                } else {
                    stb.append(" T4.SUBCLASSCD, ");
                }
                stb.append(" MAX(T4.SUBCLASSABBV) AS SUBCLASSABBV ");
                stb.append(" ,VALUE(T1.CHAIRABBV,'') AS CHAIRABBV ");
                stb.append(" FROM ");
                stb.append(" ( ");
                stb.append(" SELECT ");
                stb.append("  W1.STAFFCD, ");
                stb.append("  W1.SECTIONCD AS STAFFSEC_CD, ");
                stb.append("  W2.CHAIRCD, ");
                stb.append("  W3.GROUPCD, ");
                if ("1".equals(param[24])) {
                    stb.append("  W3.CLASSCD, ");
                    stb.append("  W3.SCHOOL_KIND, ");
                    stb.append("  W3.CURRICULUM_CD, ");
                }
                stb.append("  W3.SUBCLASSCD, ");
                stb.append("  W3.CHAIRABBV ");
                stb.append(" FROM ");
                stb.append("  V_STAFF_MST   W1, ");/*職員マスターデータ*/
                stb.append("  CHAIR_STF_DAT W2, ");
                stb.append("  CHAIR_DAT     W3  ");
                stb.append(" WHERE ");
                stb.append("  CHARGECLASSCD = '1' ");
                stb.append("  AND SECTIONCD BETWEEN '" +  param[3] + "' AND '" +  param[4] + "' ");
                stb.append("  AND W1.YEAR = '" +  param[1] + "' ");
                stb.append("  AND W1.YEAR = W2.YEAR ");
                stb.append("  AND W1.YEAR = W3.YEAR ");
                stb.append("  AND W1.STAFFCD = W2.STAFFCD ");
                stb.append("  AND W2.CHAIRCD = W3.CHAIRCD ");
                stb.append("  AND W2.SEMESTER = '" +  param[15] + "' ");
                stb.append("  AND W2.SEMESTER = W3.SEMESTER ");
                stb.append("  AND INT(W3.GROUPCD) > 0 ");
                stb.append(" ) T1 ");
                stb.append(" INNER JOIN ( ");
                stb.append(" SELECT ");
                stb.append("  DAYCD, ");
                stb.append("  PERIODCD, ");
                stb.append("  CHAIRCD ");
                stb.append(" From ");
                stb.append("  SCH_PTRN_DAT ");
                stb.append(" WHERE ");
                stb.append("  YEAR = '" +  param[1] + "' ");
                stb.append("  AND SEMESTER = '" +  param[15] + "' ");
                stb.append("  AND BSCSEQ  = " +  param[2] + " ");
                stb.append(" ) T2 ON (T1.CHAIRCD = T2.CHAIRCD) ");
                if ("1".equals(param[24])) {
                    stb.append(" LEFT JOIN SUBCLASS_MST   T4 ON T1.CLASSCD = T4.CLASSCD ");
                    stb.append("      AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
                    stb.append("      AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ");
                    stb.append("      AND T1.SUBCLASSCD = T4.SUBCLASSCD ");
                } else {
                    stb.append(" LEFT JOIN SUBCLASS_MST   T4 ON (T1.SUBCLASSCD = T4.SUBCLASSCD) ");
                }
                stb.append(" LEFT JOIN V_ELECTCLASS_MST T5 ON (T5.YEAR='"+param[1]+"' AND T1.GROUPCD = T5.GROUPCD) ");
                if ("1".equals(param[24])) {
                    if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                        stb.append("   WHERE T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                    }
                }
                stb.append(" GROUP BY ");
                stb.append(" T1.GROUPCD, ");
                stb.append(" T1.CHAIRCD, ");
                if ("1".equals(param[24])) {
                    stb.append(" T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD, ");
                } else {
                    stb.append(" T4.SUBCLASSCD, ");
                }
                stb.append(" VALUE(T1.CHAIRABBV,'') ");
                stb.append(" ORDER BY 1 ");

            }
            else {

                stb.append(" SELECT DISTINCT ");
                stb.append(" T1.GROUPCD AS GROUPCD, ");
                stb.append(" T1.CHAIRCD AS CHAIRCD, ");
                stb.append(" MAX(T5.GROUPNAME) AS GROUPNAME, ");
                if ("1".equals(param[24])) {
                    stb.append(" T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD AS SUBCLASSCD, ");
                } else {
                    stb.append(" T4.SUBCLASSCD, ");
                }
                stb.append(" MAX(T4.SUBCLASSABBV) AS SUBCLASSABBV ");
                stb.append(" ,VALUE(T1.CHAIRABBV,'') AS CHAIRABBV ");
                stb.append(" FROM ");
                stb.append(" ( ");
                stb.append(" SELECT ");
                stb.append("  W1.STAFFCD, ");
                stb.append("  W1.SECTIONCD AS STAFFSEC_CD, ");
                stb.append("  W2.CHAIRCD, ");
                stb.append("  W3.GROUPCD, ");
                if ("1".equals(param[24])) {
                    stb.append("  W3.CLASSCD, ");
                    stb.append("  W3.SCHOOL_KIND, ");
                    stb.append("  W3.CURRICULUM_CD, ");
                }
                stb.append("  W3.SUBCLASSCD, ");
                stb.append("  W3.CHAIRABBV ");
                stb.append(" FROM ");
                stb.append("  V_STAFF_MST   W1, "); /*職員マスターデータ*/
                stb.append("  CHAIR_STF_DAT W2, ");
                stb.append("  CHAIR_DAT     W3  ");
                stb.append(" WHERE ");
                stb.append("  CHARGECLASSCD = '1' ");
                stb.append("  AND SECTIONCD BETWEEN '" +  param[3] + "' AND '" +  param[4] + "' ");
                stb.append("  AND W1.YEAR = '" +  param[1] + "' ");
                stb.append("  AND W1.YEAR = W2.YEAR ");
                stb.append("  AND W1.YEAR = W3.YEAR ");
                stb.append("  AND W1.STAFFCD = W2.STAFFCD ");
                stb.append("  AND W2.SEMESTER = '" +  param[15] + "' ");
                stb.append("  AND W2.SEMESTER = W3.SEMESTER ");
                stb.append("  AND W2.CHAIRCD = W3.CHAIRCD ");
                stb.append("  AND INT(W3.GROUPCD) > 0 ");
                stb.append(" ) T1 ");
                stb.append(" INNER JOIN ( ");
                stb.append(" SELECT ");
                stb.append("  DAYOFWEEK(EXECUTEDATE) AS DAYCD, ");
                stb.append("  PERIODCD, ");
                stb.append("  CHAIRCD ");
                stb.append(" From ");
                stb.append("  SCH_CHR_DAT ");
                stb.append(" WHERE ");
                stb.append("  YEAR = '" +  param[1] + "' ");
                stb.append("  AND EXECUTEDATE BETWEEN '" +  param[2] + "' AND '" +  param[7] + "' ");
                stb.append(" ) T2 ON (T1.CHAIRCD = T2.CHAIRCD) ");
                if ("1".equals(param[24])) {
                    stb.append(" LEFT JOIN SUBCLASS_MST   T4 ON T1.CLASSCD = T4.CLASSCD ");
                    stb.append("      AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
                    stb.append("      AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ");
                    stb.append("      AND T1.SUBCLASSCD = T4.SUBCLASSCD ");
                } else {
                    stb.append(" LEFT JOIN SUBCLASS_MST   T4 ON (T1.SUBCLASSCD = T4.SUBCLASSCD) ");
                }
                stb.append(" LEFT JOIN V_ELECTCLASS_MST T5 ON (T5.YEAR='"+param[1]+"' AND T1.GROUPCD = T5.GROUPCD) ");
                if ("1".equals(param[24])) {
                    if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                        stb.append("   WHERE T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                    }
                }
                stb.append(" GROUP BY ");
                stb.append(" T1.GROUPCD, ");
                stb.append(" T1.CHAIRCD, ");
                if ("1".equals(param[24])) {
                    stb.append(" T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD, ");
                } else {
                    stb.append(" T4.SUBCLASSCD, ");
                }
                stb.append(" VALUE(T1.CHAIRABBV,'') ");
                stb.append(" ORDER BY 1 ");

            }

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            log.debug("set_chapter2 sql ok!");

                /** SVFフォームへデータをセット **/
            ret=svf.VrsOut("ymd"     , KNJ_EditDate.h_format_JP(param[10]));
            int seqno=0;
            while( rs.next() ){
                seqno++;
                ret=svf.VrlOut("number" , seqno);                            //No.
                ret=svf.VrsOut("course" , rs.getString("GROUPNAME"));        //選択科目名
                if ("2".equals(param[23])) {
                    ret=svf.VrsOut("SELECTNAME", "講座名");
                    ret=svf.VrsOut("subject", rs.getString("CHAIRABBV"));
                } else {
                    ret=svf.VrsOut("SELECTNAME", "科目名");
                    ret=svf.VrsOut("subject", rs.getString("SUBCLASSABBV"));
                }
                ret=svf.VrsOut("CLASSCD", rs.getString("CHAIRCD"));            //講座コード
                String strx = rs.getString("GROUPCD");
                try {

                    String sql2 = "SELECT DISTINCT"
                                  + " T2.HR_NAME, "    //対象クラス
                                  + " T2.GRADE, "
                                  + " T2.HR_CLASS "
                               + "FROM "
                               + " ( "
                               + " SELECT "
                               + "  T1.CHAIRCD,"
                               + "  T1.TRGTGRADE, "
                               + "  T1.TRGTCLASS "
                               + " From "
                               + "  CHAIR_CLS_DAT T1 ";
                    if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                        sql2 += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.TRGTGRADE ";
                        sql2 += "   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
                    }
                         sql2 += " WHERE "
                               + "  T1.YEAR = ? "
                               + "  AND T1.SEMESTER = ? "
                               + "  AND T1.GROUPCD = ? "
                               + " ) T1 "
                               + " INNER JOIN SCHREG_REGD_HDAT T2 ON (T2.YEAR = ? AND T2.SEMESTER = ? AND T1.TRGTGRADE = T2.GRADE AND T1.TRGTCLASS = T2.HR_CLASS) "
                                  + " ORDER BY T2.GRADE,T2.HR_CLASS ";

                    PreparedStatement ps2 = db2.prepareStatement(sql2);
                    ps2.setString(1, param[1]);
                    ps2.setString(2, param[15]);
                    ps2.setString(3, strx);
                    ps2.setString(4, param[1]);
                    ps2.setString(5, param[15]);
                    ResultSet rs2 = ps2.executeQuery();

                    String stry = "";
                    if(rs2.next()){
                        stry = rs2.getString(1);
                        while(rs2.next()){
                            stry = stry + "," + rs2.getString(1);
                        }
                    }
                    ret=svf.VrsOut("class", stry);    //対象クラス

                    rs2.close();
                    ps2.close();

                } catch( Exception e ){
                    log.error("targetclass error!", e);
                }
                if(rs.getString("GROUPNAME") == null && rs.getString("SUBCLASSABBV") == null && strx == null){
                } else{
                    ret = svf.VrEndRecord();
                    nonedata  = true; //該当データなしフラグ
                }
            }
            rs.close();
            ps.close();
            db2.commit();
            log.debug("set_chapter2 read ok!");
        } catch( Exception ex ) {
            log.error("set_chapter2 read error!", ex);
        }

    }  //set_chapter2の括り


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
                + "      ,case when '"+param[2]+"' < SDATE then SDATE else null end as SDATE "
                + "      ,case when '"+param[7]+"' > EDATE then EDATE else null end as EDATE "
                + "FROM SEMESTER_MST "
                + "WHERE SDATE <= ? AND EDATE >= ? AND YEAR=? AND SEMESTER<>?";

            PreparedStatement ps = db2.prepareStatement(sql);
            ps.setString(1, param[11]); // NO007 param[2]→param[11]
            ps.setString(2, param[11]); // NO007 param[2]→param[11]
            ps.setString(3, param[1]);
            ps.setString(4, strx);
            ResultSet rs = ps.executeQuery();

            param[12] = param[2]; //週開始日 NO008Add
            if( rs.next() ){
                param[15] = rs.getString("SEMESTER"); //学期
                if (rs.getString("SDATE") != null) param[2] = rs.getString("SDATE"); //学期開始日 NO008Add
                if (rs.getString("EDATE") != null) param[7] = rs.getString("EDATE"); //学期終了日 NO008Add
            }
            if(param[15] == null) param[15] = "0";
            ps.close();
            rs.close();
            db2.commit();
            log.debug("get_semester ok!");
        } catch( Exception e ){
            log.error("get_semester error!", e);
        }
    }  //get_semesterの括り


    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void set_head(String param[])
                     throws ServletException, IOException
    {
        try {
            ret=svf.VrsOut("nendo"     , nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度");
            ret=svf.VrsOut("TODAY"     , KNJ_EditDate.h_format_JP(param[10]));

            if(param[0].equals("1")){
                ret=svf.VrsOut("TITLE"     , "(" + param[14] + ")");
            }
            //週の日付
            if(param[0].equals("2")){
//                ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//NO010 テスト
                ret = svf.VrsOut("MARK1_1",  "  " );//NO010 テスト
                ret = svf.VrsOut("MARK1_2",  "　：テスト時間割" );//NO010 テスト

                ret=svf.VrsOut("term"       , "(" + KNJ_EditDate.h_format_JP(param[2]) + " \uFF5E " + KNJ_EditDate.h_format_JP(param[7]) + ")");

                int nen  = Integer.parseInt(param[12].substring(0,4)); // NO008 param[2]→param[12]
                int tuki = Integer.parseInt(param[12].substring(5,7)); // NO008 param[2]→param[12]
                int hi   = Integer.parseInt(param[12].substring(8)); // NO008 param[2]→param[12]
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                for(int ia=0 ; ia<7 ; ia++){
                    param[16+ia] = "(" + tuki + "/" + hi + ")";
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
            log.error("set_head read error!", ex);
        }

    }  //set_headの括り



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
            log.error("h_tuki error!", ex);
        }
        return strx;
    }  //h_tukiの括り


    /**担当教科別時間割表**/
    private String Pre_Stat1(String param[])
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
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W4.SUBCLASSCD,W4.GROUPCD,'0' AS DATADIV "); // NO010 データ区分 2:テスト
                if ("1".equals(param[24])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//NO002 ダンスの先生
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD,W1.DATADIV "); // NO010 データ区分 2:テスト
                if ("1".equals(param[24])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//NO002 ダンスの先生
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[15]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' AND  ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座クラス
            stb.append(",CHAIR_CLS AS ( ");
            stb.append("    SELECT W2.CHAIRCD,MIN(W1.TRGTGRADE||W1.TRGTCLASS) AS TRGTGRCL ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKind)) {
                    stb.append("        AND T2.SCHOOL_KIND IN " + selectSchoolKindSql + "  ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = W1.YEAR AND T2.GRADE = W1.TRGTGRADE ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[15]+"' ");
            stb.append("    GROUP BY W2.CHAIRCD ) ");
            //所属
            stb.append(",SECTION AS ( ");
            stb.append("    SELECT W2.SECTIONCD,W2.STAFFCD,W2.STAFFNAME,W1.SECTIONABBV ");
            stb.append("    FROM   V_SECTION_MST W1,V_STAFF_MST W2 ");
            stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND ");
            stb.append("           W1.SECTIONCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND ");
            stb.append("           W2.YEAR=W1.YEAR AND ");
            stb.append("           W2.SECTIONCD=W1.SECTIONCD ) ");

            //メイン
            stb.append("SELECT T3.SECTIONCD ,T3.SECTIONABBV ");
            stb.append("       ,T1.DAYCD,f_period(T1.PERIODCD) as PERIODCD,L7.ABBV1 ");
            stb.append("       ,T1.STAFFCD ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ");
            stb.append("       ,T1.GROUPCD ,L8.GROUPNAME ");
            if ("1".equals(param[24])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSABBV ");
            stb.append("       ,T1.CHAIRCD ,VALUE(T1.CHAIRABBV,'') AS CHAIRABBV ");
            stb.append("       ,L3.GRADE ,L3.HR_CLASS ");
            stb.append("       ,CASE WHEN T1.GROUPCD > '0000' THEN L3.HR_NAMEABBV||'*' ");
            stb.append("             ELSE L3.HR_NAMEABBV END AS TARGETCLASS ");
            stb.append("       ,CASE WHEN T1.DATADIV = '2' THEN 2 ELSE 0 END AS DATADIV "); // NO010 データ区分 2:テスト
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[24])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            stb.append("       ,CHAIR_CLS T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR='"+param[1]+"' AND  ");
            stb.append("                                        L3.SEMESTER='"+param[15]+"' AND  ");
            stb.append("                                        L3.GRADE||L3.HR_CLASS=T2.TRGTGRCL  ");
            stb.append("       ,SECTION T3 ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD AND ");
            stb.append("       T1.STAFFCD=T3.STAFFCD ");
            stb.append("ORDER BY T3.SECTIONCD,T1.STAFFCD,T1.DAYCD,4 ");
        } catch( Exception e ){
            log.error("Pre_Stat1 error!", e);
        }
        return stb.toString();

    }//Pre_Stat1()の括り


    /**校時のカウント**/
    private String getCountPeriod()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("    SELECT f_period(NAMECD2) as PERIODCD ");
            stb.append("    FROM   NAME_MST ");
            stb.append("    WHERE  NAMECD1='B001' ");
            stb.append("    ORDER BY 1 ");
        } catch( Exception e ){
            log.error("getCountPeriod error!", e);
        }
        return stb.toString();

    }//getCountPeriod()の括り



}  //クラスの括り
