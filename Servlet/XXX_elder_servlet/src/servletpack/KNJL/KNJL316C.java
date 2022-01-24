package servletpack.KNJL;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１６Ｃ＞  成績一覧
 *
 *  2008/11/12 RTS 作成日
 **/

public class KNJL316C {

    private static final Log log = LogFactory.getLog(KNJL316C.class);
    private AverageScore[] _averageScore;
    private TestSubclassName[] _testSubclassName;
    private Param _param;
    //カレッジ中学A日程用(分布表)
    private final String SUBCLASS_2KA = "2"; //２科　国語、算数
    private final String SUBCLASS_3KA = "3"; //３科　国語、算数、理科
    private final String SUB_ORDER1 = "1"; //受験型　1:Ⅰ型(国算理)、2:Ⅱ型(国算)
    private final String SUB_ORDER2 = "2"; //受験型　1:Ⅰ型(国算理)、2:Ⅱ型(国算)
    private final String KOKUGO = "1"; //国語
    private final String SANSUU = "2"; //算数
    private final String RIKA = "3"; //理科
    
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        log.fatal("$Revision: 65271 $ $Date: 2019-01-24 16:17:13 +0900 (木, 24 1 2019) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, request);
        
        String[] testDivPrint = {""};
        if ("9".equals(_param._testDiv)) {
            if ("1".equals(_param._applicantDiv)) {
                if (_param.isCollege()) {
                    testDivPrint = new String[]{"1","2"};
                } else if (_param.isGojo()) {
                    testDivPrint = new String[]{"1","7","2"};
                } else {
                    testDivPrint = new String[]{"1","2"};
                }
            }
            if ("2".equals(_param._applicantDiv)) {
                if (_param.isGojo()) {
                    testDivPrint = new String[]{"3","4","5","8"}; 
                } else {
                    testDivPrint = new String[]{"3","4","5"}; 
                }
            }
        } else {
            testDivPrint = new String[]{_param._testDiv};
        }
        
        //  print設定
        response.setContentType("application/pdf");
        
        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ

        
        //SQL作成
        try {
            for(int i=0; i<testDivPrint.length; i++) {
                String testDiv = testDivPrint[i];

                String[] shDivPrint = null;
                if ("9".equals(_param._testDiv)) {
                    if (_param.isCollege()) {
                        //入試区分毎
                        shDivPrint = new String[]{"9"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                        shDivPrint = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                        shDivPrint = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                        shDivPrint = new String[]{"9"};
                    } else if (_param.isGojo() || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) {
                        shDivPrint = new String[]{"1","2"};
                    } else {
                        shDivPrint = new String[]{"1"};
                    }
                } else {
                    if (_param.isCollege()) {
                        shDivPrint = new String[]{"9"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                        shDivPrint = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                        shDivPrint = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                        shDivPrint = new String[]{"9"};
                    } else {
                        shDivPrint = new String[]{_param._shDiv};
                    }
                }
                
                for(int j=0; j<shDivPrint.length; j++) {
                    String shDiv = shDivPrint[j];
                    
                    String[] selSubDivPrint = null;
                    if (_param.isGojo() && "2".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                        selSubDivPrint = new String[]{"7","6","9"}; //7:数学 6:社会
                    } else {
                        selSubDivPrint = new String[]{"9"};
                    }

                    for(int k=0; k<selSubDivPrint.length; k++) {
                        String selSubDiv = selSubDivPrint[k];
                        
                        String sql;
                        sql = preStat1(testDiv, shDiv, selSubDiv);
                        log.debug("preStat1 sql="+sql);
                        ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement
                        
                        sql = preStatTotalPage(testDiv, shDiv, selSubDiv);
                        log.debug("preStateTotalPage sql="+sql);
                        psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement
                        
                        //SVF出力
                        if (setSvfMain(db2, svf, ps1, psTotalPage, testDiv, shDiv, selSubDiv)) nonedata = true;  //帳票出力のメソッド
                    }
                }
            }
        } catch (Exception ex) {
            log.error("DB2 prepareStatement set error!", ex);
        }

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(psTestdiv,ps1,psTotalPage);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り


    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv,
        String shDiv,
        int formNum,
        String selSubDiv,
        String sub2ka3kaDiv
    ) {
        if (formNum == 1) {
            setTotalScore(db2, testDiv, shDiv, selSubDiv);
            setTestSubclassName(db2, testDiv);
            final String form;
            if (_param.isCollege()) {
                if ("2".equals(_param._applicantDiv) || "1".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                    form = "KNJL316C_1C_2.frm";
                } else {
                    form = "KNJL316C_1C.frm";
                }
            } else if (_param.isGojo()) {
                form = "KNJL316C_1G.frm";
            } else {
                form = "KNJL316C_1.frm";
            }
            svf.VrSetForm(form, 1);
        } else if (formNum == 2) {
            final String form;
            if (_param.isCollege()) {
                if ("1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    form = "KNJL316C_2C_2.frm";
                } else if ("1".equals(_param._applicantDiv)) {
                    form = "KNJL316C_2C.frm";
                } else {
                    form = "KNJL316C_3C.frm";
                }
            } else if (_param.isGojo()) {
                if ("1".equals(_param._applicantDiv)) {
                    form = "KNJL316C_2G.frm";
                } else {
                    form = "1".equals(shDiv) ? "KNJL316C_3G.frm" : "KNJL316C_4G.frm";
                }
            } else {
                if ("1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                    form = "KNJL316C_3.frm";
                } else {
                    form = "KNJL316C_2.frm";
                }
            }
            svf.VrSetForm(form, 4);
        }
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        String selsubdibText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdibText = ","+_param.getNameMst(db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            shdivText = "("+_param.getNameMst(db2, "L006", shDiv)+selsubdibText+")";// 画面から専併区分
        }
        svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv)+shdivText);// 画面から入試区分
        
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        svf.VrsOut("DATE", _param.getLoginDateString());
        
        String sortType = "";
        if ("1".equals(_param._sortType)) sortType+="(成績順)";
        if ("2".equals(_param._sortType)) sortType+="(受験番号順)";
        String title = "得点成績一覧表"+sortType;
        svf.VrsOut("TITLE", title);

        //カレッジ中学A日程
        if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
            svf.VrsOut( "SUBTITLE", "（２科型）");
        } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
            svf.VrsOut( "SUBTITLE", "（３科型）");
        }

        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
    }

    private void setTotalScore(DB2UDB db2, String testDiv, String shDiv, String selSubDiv) {
        String sql = preTotalScore(testDiv, shDiv, selSubDiv);
        log.debug("setTotalScore sql="+sql);
        List averageScoreList = new ArrayList();
        try{
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String showOrder = rs.getString("SHOWORDER");
                String totalScore = rs.getString("TOTALSCORE");
                String count = rs.getString("COUNT");
                //log.debug(showOrder+","+score+","+total);
                if (null == showOrder)
                    continue;
                AverageScore averageScore = new AverageScore(
                        Integer.valueOf(showOrder).intValue(),
                        totalScore,
                        Integer.valueOf(count).intValue());
                averageScoreList.add( averageScore );
            }
            _averageScore = new AverageScore[averageScoreList.size()];
            for(int i=0; i<averageScoreList.size(); i++) {
                _averageScore[i] = (AverageScore) averageScoreList.get(i);
            }
        } catch (Exception ex) {
            log.debug("setTotalScore exception=", ex);
        }
    }

    private void setTestSubclassName(DB2UDB db2, String testDiv) {
        String sql = preSubclassName(testDiv);
        log.debug("setTestSubclassName sql="+sql);
        List testSubclassNameList = new ArrayList();
        try{
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String showOrder = rs.getString("SHOWORDER");
                String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                String testSubclassName = rs.getString("TESTSUBCLASSNAME");
                String isSelectSubclassStr = rs.getString("IS_SELECT_SUBCLASS");
                boolean isSelectSubclass = false;
                if (isSelectSubclassStr != null && "1".equals(isSelectSubclassStr))
                    isSelectSubclass = true;
                
                log.debug(showOrder+","+testSubclassName);
                testSubclassNameList.add(new TestSubclassName(
                        Integer.valueOf(showOrder).intValue(),
                        isSelectSubclass,
                        testSubclassName,
                        testSubclassCd));
            }
            _testSubclassName = new TestSubclassName[testSubclassNameList.size()];
            for(int i=0; i<testSubclassNameList.size(); i++) {
                _testSubclassName[i] = (TestSubclassName) testSubclassNameList.get(i);
            }
        } catch (Exception ex) {
            log.debug("setTestSubclassName exception=", ex);
        }
    }
    
    private int byteCountMS932(final String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }

    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "9" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */

    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1,
            PreparedStatement psTotalpage,
            String testDiv,
            String shDiv,
            String selSubDiv
        ) {
            boolean nonedata = false;

            try {

                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    if (setSvfout1(db2, svf, testDiv, shDiv, selSubDiv, SUBCLASS_2KA)) nonedata = true; //帳票出力のメソッド
                    if (setSvfout1(db2, svf, testDiv, shDiv, selSubDiv, SUBCLASS_3KA)) nonedata = true; //帳票出力のメソッド
                } else {
                    if (setSvfout1(db2, svf, testDiv, shDiv, selSubDiv, "")) nonedata = true; //帳票出力のメソッド
                }
                if (setSvfout2(db2, svf, ps1, psTotalpage, testDiv, shDiv, nonedata, selSubDiv)) nonedata = true; //帳票出力のメソッド
            } catch (Exception ex) {
                log.error("setSvfMain set error!", ex);
            }
            return nonedata;
        }

    /** 表紙をセットする */
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String testDiv,
            String shDiv,
            String selSubDiv
    )
    {
        String sortType = "";
        if ("1".equals(_param._sortType)) sortType+="(成績順)";
        if ("2".equals(_param._sortType)) sortType+="(受験番号順)";
        String title = "【得点成績一覧表"+sortType+"】";
        svf.VrSetForm("KNJLCVR001C.frm", 1);
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度

        String selsubdibText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdibText = ","+_param.getNameMst(db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            shdivText = "("+_param.getNameMst(db2, "L006", shDiv)+selsubdibText+")";// 画面から専併区分
        }
        svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv)+shdivText);// 画面から入試区分
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        svf.VrsOut("TITLE", title);
        svf.VrsOut("NOTE" ,"note");
        svf.VrEndPage();
    }
    
    /**総ページ数をセット**/
    private String getTotalPage(
        DB2UDB db2,
        PreparedStatement psTotalPage
    ) {
        try {
            ResultSet rs = psTotalPage.executeQuery();

            while( rs.next() ){
                if (rs.getString("TOTAL_PAGE") != null) 
                    return rs.getString("TOTAL_PAGE");
            }
            rs.close();
            db2.commit();
        } catch (Exception ex) {
            log.error("setTotalPage set error! =", ex);
        }
        return null;
    }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout2(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        PreparedStatement psTotalpage,
        String testDiv,
        String shDiv,
        boolean nonedataBefore,
        String selSubDiv
    ) {
        boolean nonedata = false;
        try {
            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            ResultSet rs = ps1.executeQuery();
            String currentSeqno = "-";
            List testRecordList = new ArrayList();
            TestRecord tr = null;
            while( rs.next() ){
                if (!currentSeqno.equals(rs.getString("SEQNO"))) {
                    currentSeqno = rs.getString("SEQNO");
                    //明細
                    tr = new TestRecord(
                           rs.getString("SEQNO"), 
                           rs.getString("EXAMNO"),
                           rs.getString("NAME"),
                           rs.getString("SHDIV"),
                           rs.getString("SEX_NAME"),
                           rs.getString("PREF_NAME"),
                           nvlT(rs.getString("FINSCHOOL_NAME")),
                           rs.getString("TOTAL4"),
                           rs.getString("AVERAGE4"),
                           rs.getString("TOTAL_RANK4"),
                           rs.getString("SUB_ORDER"),
                           rs.getString("TOTAL2"),
                           rs.getString("AVERAGE2"),
                           rs.getString("AVERAGE1"),
                           rs.getString("TOTAL_RANK1"),
                           rs.getString("JUDGEDIV"),
                           rs.getString("JUDGEDIV_NAME"),
                           nvlT(rs.getString("REMARK1")),
                           nvlT(rs.getString("REMARK2")),
                           rs.getString("PRE_RECEPTNO"),
                           rs.getString("PRISCHOOL_NAME"),
                           rs.getString("RECOM_EXAMNO"),
                           rs.getString("OTHER_TESTDIV_NAME"),
                           rs.getString("OTHER_JUDGEDIV_NAME"),
                           rs.getString("OTHER_PROCEDUREDIV"),
                           rs.getString("SHIFT_DESIRE_FLG"),
                           rs.getString("SLIDE_FLG"),
                           rs.getString("SPORTS_FLG"),
                           rs.getString("GENERAL_FLG_NAME"),
                           rs.getString("DORMITORY_FLG"),
                           rs.getString("SHSCHOOL_DIV"),
                           rs.getString("SHSCHOOL_NAME")
                    );
                    testRecordList.add(tr);
                    
                    //レコード数カウント
                    reccnt++;
                    if (rs.getString("SEX") != null) {
                        if (rs.getString("SEX").equals("1")) reccnt_man++;
                        if (rs.getString("SEX").equals("2")) reccnt_woman++;
                    }
                }
                if (null != rs.getString("SHOWORDER")) {
                    int score = (null !=rs.getString("SCORE")) ? Integer.parseInt(rs.getString("SCORE")) : 0;
                    tr.addTestSubclass(
                            Integer.parseInt(rs.getString("SHOWORDER")),
                            score,
                            rs.getString("IS_SELECT_SUBCLASS"),
                            rs.getString("SELECT_SUBCLASS_DIV"),
                            rs.getString("TESTSUBCLASSCD"),
                            rs.getString("ATTEND_FLG")
                            );
                }
            }
            
            int pagecnt = 1;            //現在ページ数
            int gyo = 0;                //現在ページ数の判断用（行）
            int lineNumber=0;
            int totalPage = -1;
            for(Iterator it=testRecordList.iterator(); it.hasNext();) {
                if (nonedata == false) {
                    if (nonedataBefore == false) {
                        setCover(db2, svf, testDiv, shDiv, selSubDiv);
                    }
                    setHeader(db2, svf, testDiv, shDiv, 2, selSubDiv, "");
                    totalPage = Integer.parseInt(getTotalPage(db2, psTotalpage)); //総ページ数メソッド
                    svf.VrsOut("TOTAL_PAGE", String.valueOf(totalPage));
                    log.debug(" total page="+totalPage);
                    nonedata = true;
                }
                lineNumber += 1;
                TestRecord tr1 = (TestRecord) it.next();
                svf.VrsOut("NUMBER", ""+lineNumber);       //連番
                svf.VrsOut("EXAMNO", tr1._examno);       //受験番号
                if (_param._output) {
                    svf.VrsOut(setformatArea("NAME", 10, tr1._name), tr1._name);//名前
                }
                if (_param.isCollege()) {
                    svf.VrsOut("APPLYDIV", "1".equals(tr1._shdiv) || "6".equals(tr1._shdiv) || "7".equals(tr1._shdiv) || "8".equals(tr1._shdiv) ? "○" : "");
                } else if (_param.isGojo()) {
                    svf.VrsOut("APPLYDIV", "1".equals(tr1._shdiv) ? "○" : "6".equals(tr1._shdiv) ? "Ⅰ" : "7".equals(tr1._shdiv) ? "Ⅱ" : "8".equals(tr1._shdiv) ? "Ⅲ" : "");
                }
                //志望コース・合格コース
                if (_param.isCollege() && "1".equals(_param._applicantDiv)) {
                    svf.VrsOut("HOPE_COURSE", "6".equals(tr1._shdiv) || "9".equals(tr1._shdiv) ? "G" : "7".equals(tr1._shdiv) || "A".equals(tr1._shdiv) ? "S" : "8".equals(tr1._shdiv) || "B".equals(tr1._shdiv) ? "S/G" : "");
                } else if (_param.isCollege() && "2".equals(_param._applicantDiv)) {
                    svf.VrsOut("HOPE_COURSE", "3".equals(tr1._shdiv) ? "EA" : "4".equals(tr1._shdiv) ? "ES" : "5".equals(tr1._shdiv) ? "EA/ES" : "");
                    svf.VrsOut("PASS_COURSE", "A".equals(tr1._judgediv) ? "EA" : "B".equals(tr1._judgediv) ? "ES" : "");
                }
                svf.VrsOut("SEX", tr1._sexname);     //性別
                svf.VrsOut("PREF", tr1._pref); // 学校の都道府県
                svf.VrsOut(setformatArea("FINSCHOOL", 13, tr1._fschool), tr1._fschool);
                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    svf.VrsOut("SH_DIV", "1".equals(tr1._subOrder) ? "Ⅰ型" : "2".equals(tr1._subOrder) ? "Ⅱ型" : ""); //受験型 1:Ⅰ型(国算理)、2:Ⅱ型(国算)
                    if (null == tr1._total2){ 
                        svf.VrsOut("TOTAL2", "*"); // ２科合計
                        svf.VrsOut("AVERAGE2", "*"); // ２科平均
                        svf.VrsOut("TOTAL3", "*"); // ３科合計
                        svf.VrsOut("AVERAGE3", "*"); // ３科平均
                        svf.VrsOut("PER_SCORE", "*"); // 得点率
                        svf.VrsOut("RANK", "*"); // 順位(得点率)
                    } else {
                        svf.VrsOut("TOTAL2", tr1._total2); // ２科合計
                        svf.VrsOut("AVERAGE2", tr1._average2); // ２科平均
                        svf.VrsOut("TOTAL3", tr1._total4); // ３科合計
                        svf.VrsOut("AVERAGE3", tr1._average4); // ３科平均
                        svf.VrsOut("PER_SCORE", tr1._average1); // 得点率
                        svf.VrsOut("RANK", tr1._total_rank1); // 順位(得点率)
                    }
                } else {
                    if (null == tr1._total4){ 
                        svf.VrsOut("TOTAL", "*"); // 総合
                        svf.VrsOut("AVERAGE", "*"); // 平均
                        svf.VrsOut("RANK", "*"); // 順位
                    } else {
                        svf.VrsOut("TOTAL", tr1._total4); // 総合
                        svf.VrsOut("AVERAGE", tr1._average4); // 平均
                        svf.VrsOut("RANK", tr1._total_rank4); // 順位
                    }
                }
                svf.VrsOut("JUDGE", tr1._judgeName); // 判定
                if (_param.isGojo() && "1".equals(_param._applicantDiv)) {
                    String otherTestDivName = null != tr1._otherTestDivName ? tr1._otherTestDivName + " " : "";
                    String recomExamno = null != tr1._recomExamno ? tr1._recomExamno : "";
                    String otherJudgeDivName = null != tr1._otherJudgeDivName ? " " + tr1._otherJudgeDivName : "";
                    if (_param.isCollege()) {
                        svf.VrsOut("APPLICATION", recomExamno + otherJudgeDivName);
                    } else {
                        svf.VrsOut("APPLICATION", otherTestDivName + recomExamno);
                    }
                    String kibou = (!"".equals(tr1._shiftDesireFlg) && null != tr1._shiftDesireFlg) ? "○": "";
                    if (_param.isCollege()) {
                        kibou = "1".equals(tr1._shiftDesireFlg) ? "五併" : "2".equals(tr1._shiftDesireFlg) ? "和併" : "3".equals(tr1._shiftDesireFlg) ? "五併/和併" : "";
                    }
                    svf.VrsOut("SHIFT", kibou);
                    String juku = null != tr1._prischoolName ? tr1._prischoolName : "";
                    svf.VrsOut("JUKU" + (juku.length() > 17 ? "3" : juku.length() > 10 ? "2" : ""), juku);
                    svf.VrsOut("PRENO", tr1._preReceptno);
                    final String remark = tr1._remark1 + tr1._remark2;
                    svf.VrsOut(setformatArea("REMARK", 10, nvlT(remark))  ,nvlT(remark)); // 備考
                } else if (_param.isGojo() && "2".equals(_param._applicantDiv)) {
                    final int priLen = byteCountMS932(tr1._prischoolName);
                    svf.VrsOut("JUKU" + (priLen > 34 ? "3" : priLen > 24 ? "2" : ""), tr1._prischoolName); // 塾

                    final int brosisLen = byteCountMS932(tr1._remark1);
                    svf.VrsOut("BROSIS" + (brosisLen > 30 ? "3" : brosisLen > 20 ? "2" : "1"), tr1._remark1); // 兄弟姉妹
                    
                    if ("1".equals(tr1._dormitoryFlg)) {
                        svf.VrsOut("DORMITORY", "○"); // 寮
                    }

                    final int shschoollen = byteCountMS932(tr1._shSchoolname);
                    final String shSchoolField = "HEIGAN" + (shschoollen > 30 ? "3" : shschoollen > 20 ? "2" : "1");
                    svf.VrsOut(shSchoolField, tr1._shSchoolname); // 備考

                    if ("1".equals(tr1._sportsFlg)) {
                        svf.VrsOut("SPORTS", "○"); // S組
                    }
                    if (null != tr1._generalFlgName) {
                        svf.VrsOut("GENERAL_FLG", tr1._generalFlgName); // スポーツ
                    }
                    if ("1".equals(shDiv) || _param.isCollege()) {
                        // 専願
                        svf.VrsOut(setformatArea("REMARK", 26, nvlT(tr1._remark2))  ,nvlT(tr1._remark2)); // 備考
                    } else if ("2".equals(shDiv)) {
                        // 併願                      
                        if ("1".equals(tr1._slideFlg)) {
                            svf.VrsOut("SLIDE", ""); // 特併
                        }
                        svf.VrsOut(setformatArea("REMARK", 25, nvlT(tr1._remark2))  ,nvlT(tr1._remark2)); // 備考
                    }
                        
                } else {
                    String remark = "";
                    if (null != tr1._otherTestDivName || (!"".equals(tr1._shiftDesireFlg) && null != tr1._shiftDesireFlg)) {
                        String otherTestDivName = null != tr1._otherTestDivName ? tr1._otherTestDivName + " " : "";
                        String kibou = _param.isGojo() ? "カレッジ併願" : "移行希望";
                        if (_param.isCollege()) {
                            kibou = "1".equals(tr1._shiftDesireFlg) ? "五併" : "2".equals(tr1._shiftDesireFlg) ? "和併" : "3".equals(tr1._shiftDesireFlg) ? "五併/和併" : "";
                        }
                        String shiftDesire = (!"".equals(tr1._shiftDesireFlg) && null != tr1._shiftDesireFlg) ? kibou + " ": "";
                        String recomExamno = null != tr1._recomExamno ? tr1._recomExamno + " " : "";
                        remark += otherTestDivName + recomExamno + shiftDesire;
                    }
                    remark += tr1._remark1 + tr1._remark2;
                    if ("1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                        svf.VrsOut("BEFORE_JUDGE", tr1._otherJudgeDivName); // 前期判定
                        svf.VrsOut("PROCEDURE", "1".equals(tr1._otherProcedureDiv) ? "○" : ""); // 手続済
                        svf.VrsOut(setformatArea("REMARK", 22, remark)  ,remark); // 備考
                    } else {
                        svf.VrsOut(setformatArea("REMARK", 27, remark)  ,remark); // 備考
                    }
                }
                for(int i=1; i<=6; i++) {
                    TestScore ts = tr1.getTestScore(i);
                    String score = null;
                    if (null == ts) {
                        TestSubclassName tsn = null;
                        for(int j=0; j<_testSubclassName.length; j++) {
                            if (_testSubclassName[j]._showOrder == i) {
                               tsn = _testSubclassName[j];
                               break;
                            }
                        }
                        
                        if (tsn != null && tsn._isSelectSubclass) {
                            // 選択していない科目
                            score = "-";
                        } else {
                            // 受験科目ではない
                            score = " ";
                        }
                    } else {
                        if (ts._attend){
                            score = ts.getTestScore();
                        } else {
                            // 欠席している科目
                            score = "*";
                        }
                    }

                    svf.VrsOut("SCORE"+i, score);
                }
                svf.VrEndRecord();
                
                gyo ++;
                //50行超えたとき、ページ数カウント
                if (gyo % 50 == 1) {
                    svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    svfOutTestName(svf);
                    //svf.VrsOut("TESTDIV", pagecnt+1+"");
                    //ヘッダ
                    pagecnt++;
                    if (pagecnt > totalPage) {
                        //最終ページに男女合計を出力
                        svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                    }
                }
                if (gyo % 50 == 0) {
                    svfOutCountNinzu(svf, testDiv);
                    svf.VrEndRecord();
                    svfOutAverage(svf, testDiv);
                    svf.VrEndRecord();
                }
            }
            if (gyo % 50 != 0) {
                for (int i=gyo%50; i<50; i++) {
                    svf.VrEndRecord();
                }
                svfOutCountNinzu(svf, testDiv);
                svf.VrEndRecord();
                svfOutAverage(svf, testDiv);
                svf.VrEndRecord();
            }

            rs.close();
            db2.commit();
        } catch (Exception ex) {
            log.error("setSvfout set error! =", ex);
        }
        return nonedata;
    }
    
    /**帳票出力（速報データをセット）**/
    private boolean setSvfout1(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv,
        String shDiv,
        String selSubDiv,
        String sub2ka3kaDiv
    ) {
        boolean nonedata = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int[][] manCount = new int[3][2];
        try {
            String sql = preStatTestApplicantCount(testDiv, shDiv, selSubDiv, sub2ka3kaDiv);
            log.debug("preStatTestApplicantCount sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                if (nonedata == false) {
                    nonedata = true;
                }
                int index1 = -1;
                int index2 = -1;
                final String div = rs.getString("NAME");
                
                if ("APPLICANT".equals(div)) {
                    index1 = 0;
                }else if("DESIRE".equals(div)) {
                    index1 = 1;
                }else if("RECEPT".equals(div)) {
                    index1 = 2;
                }
                
                final String sex = rs.getString("SEX");
                
                if ("1".equals(sex)) {
                    index2 = 0;
                } else if("2".equals(sex)) {
                    index2 = 1;
                }
                manCount[index1][index2] = Integer.valueOf(rs.getString("COUNT")).intValue();    
                //log.debug(index1+","+index2+","+manCount[index1][index2]);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            String sql = preStatTestScoreData(testDiv, shDiv, selSubDiv, sub2ka3kaDiv);
            log.debug("preStateTestScoreData sql="+sql);
            ps = db2.prepareStatement(sql);
            
            rs = ps.executeQuery();
            List testScoreNewsList = new ArrayList();
            while (rs.next()) {
                if (nonedata == false) {
                    nonedata = true;
                }
                String showOrderStr;
                if( null == rs.getString("SHOWORDER")) {
                    showOrderStr = "-1";
                } else if("9".equals(rs.getString("SHOWORDER"))) {
                    showOrderStr = "6"; //最大5科目表示の右側
                } else {
                    showOrderStr = rs.getString("SHOWORDER");
                }
                int showOrder = Integer.valueOf(showOrderStr).intValue();
                
                TestScoreNews tsn = new TestScoreNews(rs.getString("TESTSUBCLASSNAME"), showOrder);
                tsn.setAverageMan(null == rs.getString("AVERAGE1") ? "" : new BigDecimal(rs.getString("AVERAGE1")).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                tsn.setAverageWoman(null == rs.getString("AVERAGE2") ? "" : new BigDecimal(rs.getString("AVERAGE2")).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                tsn.setAverageAll(null == rs.getString("AVERAGE3") ? "" : new BigDecimal(rs.getString("AVERAGE3")).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                tsn.setMax(rs.getString("MAX"));
                tsn.setMin(rs.getString("MIN"));
                testScoreNewsList.add(tsn);
            }
            
            // 志願者、受験者、合格者を表示
            if (nonedata == true) {
                //カレッジ中学A日程
                if (!SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                    setCover(db2, svf, testDiv, shDiv, selSubDiv);
                }
                setHeader(db2, svf, testDiv, shDiv, 1, selSubDiv, sub2ka3kaDiv);

                for (Iterator it = testScoreNewsList.iterator(); it.hasNext();) {
                    TestScoreNews tsn = (TestScoreNews) it.next();
                    svf.VrsOutn("SUBCLASSNAME",tsn._showOrder, tsn._subclassName);
                    svf.VrsOutn("AVERAGE1",tsn._showOrder, tsn._averageMan);
                    svf.VrsOutn("AVERAGE2",tsn._showOrder, tsn._averageWoman);
                    svf.VrsOutn("AVERAGE3",tsn._showOrder, tsn._averageAll);
                    svf.VrsOutn("HIGH", tsn._showOrder,tsn._max);
                    svf.VrsOutn("LOW", tsn._showOrder,tsn._min);
                }
                
                int j = 1;
                for (int div = 0; div < 3; div++) {
                    for (int sex = 0; sex < 3; sex++) {
                        int count = -1;
                        if (sex == 2) {
                            // 合計値
                            count = (manCount[div][0] + manCount[div][1]);
                        } else {
                            // 男/女
                            count = manCount[div][sex];
                        }
                        svf.VrsOutn("COUNT", j, String.valueOf(count));
                        j++;
                    }
                }

                if (_param.isCollege()) {
                    setSvfMainBunpu(db2, svf, testDiv, shDiv, sub2ka3kaDiv); //得点分布表
                }
            }
            
            svf.VrEndPage();
        } catch (Exception ex) {
            log.error("setSvfout set error! =", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return nonedata;
    }

    /** その科目を選択した人数を表示する */
    private void svfOutCountNinzu(Vrw32alp svf, String testDiv) {
        for(int i=1; i<=8; i++) {
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == i) {
                    svf.VrsOut("SCORE"+i, ""+_averageScore[j].getCount());
                    break;
                }
            }
        }
        for (int j=0; j<_averageScore.length; j++) {
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                if (_averageScore[j].getShowOrder() == 21) { // 総合(2科合計)
                    svf.VrsOut("TOTAL2", ""+_averageScore[j].getCount());
                }
                if (_averageScore[j].getShowOrder() == 22) { // 総合平均(2科平均)
                    svf.VrsOut("AVERAGE2", ""+_averageScore[j].getCount());
                }
                if (_averageScore[j].getShowOrder() == 9) { // 総合(3科合計)
                    svf.VrsOut("TOTAL3", ""+_averageScore[j].getCount());
                }
                if (_averageScore[j].getShowOrder() == 99) { // 総合平均(3科平均)
                    svf.VrsOut("AVERAGE3", ""+_averageScore[j].getCount());
                }
            } else {
                if (_averageScore[j].getShowOrder() == 9) { // 総合
                    svf.VrsOut("TOTAL", ""+_averageScore[j].getCount());
                }
                if (_averageScore[j].getShowOrder() == 99) { // 総合平均
                    svf.VrsOut("AVERAGE", ""+_averageScore[j].getCount());
                }
            }
        }
    }
    
    /** 科目名を表示する */
    private void svfOutTestName(Vrw32alp svf) {
        for(int i=0; i<_testSubclassName.length; i++) {
            svf.VrsOut("SUBCLASS"+_testSubclassName[i]._showOrder,
                    _testSubclassName[i]._testSubclassName);
        }
    }

    /** 平均点を表示する */
    private void svfOutAverage(Vrw32alp svf, String testDiv) {
        for(int i=1; i<=8; i++) {
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == i) {
                    svf.VrsOut("SCORE"+i, _averageScore[j].getAverage());
                    break;
                }
            }
        }
        for (int j=0; j<_averageScore.length; j++) {
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                if (_averageScore[j].getShowOrder() == 21) { // 総合(2科合計)
                    svf.VrsOut("TOTAL2", _averageScore[j].getAverage());
                }
                if (_averageScore[j].getShowOrder() == 22) { // 総合平均(2科平均)
                    svf.VrsOut("AVERAGE2", _averageScore[j].getAverage());
                }
                if (_averageScore[j].getShowOrder() == 9) { // 総合(3科合計)
                    svf.VrsOut("TOTAL3", _averageScore[j].getAverage());
                }
                if (_averageScore[j].getShowOrder() == 99) { // 総合平均(3科平均)
                    svf.VrsOut("AVERAGE3", _averageScore[j].getAverage());
                }
            } else {
                if (_averageScore[j].getShowOrder() == 9) { // 総合
                    svf.VrsOut("TOTAL", _averageScore[j].getAverage());
                }
                if (_averageScore[j].getShowOrder() == 99) { // 総合平均
                    svf.VrsOut("AVERAGE", _averageScore[j].getAverage());
                }
            }
        }
    }

    /** 表示する科目名を取得 */
    private String preSubclassName(String testDiv)
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     T2.NAME1 AS TESTSUBCLASSNAME, ");
            stb.append("     T2.NAMESPARE1 AS IS_SELECT_SUBCLASS, ");
            stb.append("     SHOWORDER ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT ");
            stb.append("       left join NAME_MST T2 on ");
            stb.append("           T2.NAMECD1 = 'L009' AND ");
            stb.append("           T2.NAMECD2 = TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     APPLICANTDIV= '"+_param._applicantDiv+"' AND ");
            stb.append("     TESTDIV= '"+testDiv+"' ");
            stb.append(" ORDER BY ");
            stb.append("     SHOWORDER ");
        } catch (Exception e) {
            log.error("preTotalScore error!", e);
        }
        return stb.toString();
    }
    
    /** テスト科目の点数全体の和と人数を取得 */
    private String preTotalScore(String testDiv, String shDiv, String selSubDiv)
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append(" with t_subclass as(SELECT ");
            stb.append("     ENTEXAMYEAR, ");
            stb.append("     APPLICANTDIV, ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     SHOWORDER ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     TESTDIV= '"+testDiv+"' ");
            stb.append(" ORDER BY ");
            stb.append("     APPLICANTDIV, ");
            stb.append("     SHOWORDER ");
            stb.append(" ), t_score as(SELECT ");
            stb.append("     t1.receptno, ");
            stb.append("     t1.score, ");
            stb.append("     t2.* ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT t1 ");
            stb.append("        inner join t_subclass t2 on ");
            stb.append("           t1.ENTEXAMYEAR= t2.ENTEXAMYEAR AND ");
            stb.append("           t1.APPLICANTDIV = t2.APPLICANTDIV AND ");
            stb.append("           t1.TESTDIV = t2.TESTDIV AND ");
            stb.append("           t1.TESTSUBCLASSCD = t2.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     t1.ATTEND_FLG = '1' ");
            stb.append(" ORDER BY ");
            stb.append("     t1.APPLICANTDIV ");
            stb.append(" ), t_score2 as( ");
            stb.append(" select ");
            stb.append("     T10.SHOWORDER, ");
            stb.append("     T10.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("               W3.EXAMNO = W1.EXAMNO ");
            stb.append("       LEFT JOIN t_score T10 ON ");
            stb.append("               W1.ENTEXAMYEAR = T10.ENTEXAMYEAR AND ");
            stb.append("               W1.APPLICANTDIV = T10.APPLICANTDIV AND ");
            stb.append("               W1.TESTDIV = T10.TESTDIV AND ");
            stb.append("               W1.RECEPTNO = T10.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("         AND W3.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("         AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                stb.append(" union all ");
                stb.append(" select ");
                stb.append("     21 AS SHOWORDER, "); // 総合
                stb.append("     VALUE(W1.TOTAL2,-1) AS SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT W1 ");
                stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
                stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
                stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
                stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
                stb.append("               W3.EXAMNO = W1.EXAMNO ");
                stb.append(" WHERE ");
                stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
                stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
                stb.append("     W1.TESTDIV= '"+testDiv+"' ");
                stb.append("     AND W1.ATTEND_ALL_FLG = '1' ");
                stb.append("     AND W1.TOTAL2 IS NOT NULL ");
                if (!"9".equals(shDiv)) {
                    stb.append("         AND W3.SHDIV = '" + shDiv + "' ");
                }
                if (!"9".equals(selSubDiv)) {
                    stb.append("         AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
                }
                stb.append(" union all ");
                stb.append(" select ");
                stb.append("     22 AS SHOWORDER, "); // 総合平均
                stb.append("     VALUE(W1.AVARAGE2,-1) AS SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT W1 ");
                stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
                stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
                stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
                stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
                stb.append("               W3.EXAMNO = W1.EXAMNO ");
                stb.append(" WHERE ");
                stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
                stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
                stb.append("     W1.TESTDIV= '"+testDiv+"' ");
                stb.append("     AND W1.ATTEND_ALL_FLG = '1' ");
                stb.append("     AND W1.AVARAGE2 IS NOT NULL ");
                if (!"9".equals(shDiv)) {
                    stb.append("         AND W3.SHDIV = '" + shDiv + "' ");
                }
                if (!"9".equals(selSubDiv)) {
                    stb.append("         AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
                }
            }
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("     9 AS SHOWORDER, "); // 総合
            stb.append("     VALUE(W1.TOTAL4,-1) AS SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("               W3.EXAMNO = W1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' ");
            stb.append("     AND W1.ATTEND_ALL_FLG = '1' ");
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                stb.append("     AND W1.TOTAL4 IS NOT NULL ");
            }
            if (!"9".equals(shDiv)) {
                stb.append("         AND W3.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("         AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("     99 AS SHOWORDER, "); // 総合平均
            stb.append("     VALUE(W1.AVARAGE4,-1) AS SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("               W3.EXAMNO = W1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' ");
            stb.append("     AND W1.ATTEND_ALL_FLG = '1' ");
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                stb.append("     AND W1.AVARAGE4 IS NOT NULL ");
            }
            if (!"9".equals(shDiv)) {
                stb.append("         AND W3.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("         AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("     SHOWORDER, ");
            stb.append("     sum(SCORE) AS TOTALSCORE, ");
            stb.append("     count(*) AS COUNT ");
            stb.append(" from ");
            stb.append("     t_score2 ");
            stb.append(" group by SHOWORDER ");
        } catch (Exception e) {
            log.error("preTotalScore error!", e);
        }
        return stb.toString();
    }
    

    /**受験者一覧を取得**/ 
    private String preStat1(String testDiv, String shDiv, String selSubDiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" with t_subclass as( ");
            stb.append(" select ");
            stb.append("     ENTEXAMYEAR, ");
            stb.append("     APPLICANTDIV, ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     T2.NAME1 AS TESTSUBCLASSNAME, ");
            stb.append("     T2.NAMESPARE1 AS IS_SELECT_SUBCLASS, ");
            stb.append("     SHOWORDER ");
            stb.append(" from ENTEXAM_TESTSUBCLASSCD_DAT ");
            stb.append("     left join NAME_MST T2 on ");
            stb.append("         T2.NAMECD1 = 'L009' AND ");
            stb.append("         T2.NAMECD2 = TESTSUBCLASSCD ");
            stb.append(" where ");
            stb.append("     ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     APPLICANTDIV= '"+_param._applicantDiv+"' AND ");
            stb.append("     TESTDIV= '"+testDiv+"' ");
            stb.append(" order by ");
            stb.append("     APPLICANTDIV,SHOWORDER ");
            stb.append(" ) ");
            stb.append(" , t_score as( ");
            stb.append(" select t1.receptno, t1.attend_flg, t1.score, t2.* ");
            stb.append(" from ENTEXAM_SCORE_DAT t1  ");
            stb.append("     inner join t_subclass t2 on ");
            stb.append("         t1.ENTEXAMYEAR= t2.ENTEXAMYEAR AND ");
            stb.append("         t1.APPLICANTDIV = t2.APPLICANTDIV AND ");
            stb.append("         t1.TESTDIV = t2.TESTDIV AND ");
            stb.append("         t1.TESTSUBCLASSCD = t2.TESTSUBCLASSCD ");
            stb.append(" order by ");
            stb.append("     t1.APPLICANTDIV ");
            stb.append(" ) ");
            stb.append(" select ");
            if ("1".equals(_param._sortType)) {
                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    stb.append("     dense_rank() over(order by T1.AVERAGE1 desc, T1.EXAMNO) AS SEQNO, ");
                } else {
                    stb.append("     dense_rank() over(order by T1.TOTAL4 desc, T1.EXAMNO) AS SEQNO, ");
                }
            }
            if ("2".equals(_param._sortType)) stb.append("     dense_rank() over(order by T1.EXAMNO) AS SEQNO, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     T1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T7.NAME1 AS RITU_NAME, ");
            stb.append("     T6.FINSCHOOL_DISTCD AS FINSCHOOLCD, ");
            stb.append("     VALUE(T6.FINSCHOOL_NAME_ABBV, T6.FINSCHOOL_NAME)AS FINSCHOOL_NAME, ");
            stb.append("     T6.FINSCHOOL_ZIPCD, ");
            stb.append("     T11.PREF AS PREF_NAME, ");
            stb.append("     T10.TESTSUBCLASSNAME, ");
            stb.append("     T10.TESTSUBCLASSCD, ");
            stb.append("     T10.SHOWORDER, ");
            stb.append("     T10.ATTEND_FLG, ");
            stb.append("     T10.SCORE, ");
            stb.append("     T10.IS_SELECT_SUBCLASS, ");
            stb.append("     T1.SELECT_SUBCLASS_DIV, ");
            stb.append("     (case when T1.TOTAL4 = -1 then null else T1.TOTAL4 end) AS TOTAL4, ");
            stb.append("     T1.AVERAGE4, ");
            stb.append("     T1.TOTAL_RANK4, ");
            stb.append("     T1.SUB_ORDER, ");
            stb.append("     (case when T1.TOTAL2 = -1 then null else T1.TOTAL2 end) AS TOTAL2, ");
            stb.append("     T1.AVERAGE2, ");
            stb.append("     T1.AVERAGE1, ");
            stb.append("     T1.TOTAL_RANK1, ");
            stb.append("     T1.JUDGEDIV, ");
            stb.append("     T1.JUDGEDIV_NAME, ");
            stb.append("     T1.PRE_RECEPTNO, ");
            stb.append("     P1.PRISCHOOL_NAME, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T15.NAME1 AS OTHER_JUDGEDIV_NAME, ");//Ａ日程合格 //前期判定
            stb.append("     T12.PROCEDUREDIV AS OTHER_PROCEDUREDIV, ");//前期手続済
            stb.append("     T1.SHIFT_DESIRE_FLG, ");
            stb.append("     T1.SLIDE_FLG, ");
            stb.append("     T1.SPORTS_FLG, ");
            stb.append("     T1.GENERAL_FLG, ");
            stb.append("     NM1.NAME1 AS GENERAL_FLG_NAME, ");
            stb.append("     T1.DORMITORY_FLG, ");
            stb.append("     FINH.FINSCHOOL_DIV AS SHSCHOOL_DIV, ");
            stb.append("     FINH.FINSCHOOL_NAME AS SHSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     (SELECT ");
            stb.append("         W1.ENTEXAMYEAR, ");
            stb.append("         W1.TESTDIV, ");
            stb.append("         W1.EXAMNO, ");
            stb.append("         W3.RECOM_EXAMNO, ");
            stb.append("         W1.RECEPTNO, ");
            stb.append("         W3.NAME, ");
            stb.append("         W3.NAME_KANA, ");
            stb.append("         W3.SHDIV, ");
            stb.append("         W3.SEX, ");
            stb.append("         W3.FS_NAME, ");
            stb.append("         W3.FS_CD, ");
            stb.append("         W1.TOTAL4 AS TOTAL4, ");
            stb.append("         W1.AVARAGE4 AS AVERAGE4, ");
            stb.append("         W1.TOTAL_RANK4, ");
            stb.append("         W3.SUB_ORDER, ");
            stb.append("         W1.TOTAL2 AS TOTAL2, ");
            stb.append("         W1.AVARAGE2 AS AVERAGE2, ");
            stb.append("         W1.AVARAGE1 AS AVERAGE1, ");
            stb.append("         W1.TOTAL_RANK1, ");
            stb.append("         W3.SELECT_SUBCLASS_DIV, ");
            stb.append("         W1.JUDGEDIV, ");
            stb.append("         W4.NAME1 AS JUDGEDIV_NAME, ");
            stb.append("         W3.PRE_RECEPTNO, ");
            stb.append("         W3.PRISCHOOLCD, ");
            stb.append("         W3.REMARK1, ");
            stb.append("         W3.REMARK2, ");
            stb.append("         W3.SHIFT_DESIRE_FLG, ");
            stb.append("         W3.SLIDE_FLG, ");
            stb.append("         W3.SPORTS_FLG, ");
            stb.append("         W3.GENERAL_FLG, ");
            stb.append("         W3.DORMITORY_FLG, ");
            stb.append("         W3.SH_SCHOOLCD ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_RECEPT_DAT W1 ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("             W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("             W3.EXAMNO = W1.EXAMNO AND ");
            stb.append("             W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("             W3.TESTDIV = W1.TESTDIV ");
            stb.append("         LEFT JOIN NAME_MST W4 ON ");
            stb.append("             W4.NAMECD1 = 'L013' AND ");
            stb.append("             W4.NAMECD2 = W1.JUDGEDIV ");
            stb.append("     WHERE ");
            stb.append("         W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("         W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("         W1.TESTDIV= '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("         AND W3.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("         AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            stb.append("     ) T1 ");
            stb.append("     LEFT JOIN NAME_MST T5 ON ");
            stb.append("                 T5.NAMECD1='Z002' AND ");
            stb.append("                 T5.NAMECD2=T1.SEX ");
            stb.append("     LEFT JOIN FINSCHOOL_MST T6 ON ");
            stb.append("                 T6.FINSCHOOLCD = T1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST T7 ON ");
            stb.append("                 T7.NAMECD1 = 'L001' AND ");
            stb.append("                 T7.NAMECD2 = T6.FINSCHOOL_DISTCD ");
            stb.append("     LEFT JOIN t_score T10 ON ");
            stb.append("                 T1.RECEPTNO = T10.RECEPTNO ");
            stb.append("     LEFT JOIN ZIPCD_MST T11 ON ");
            stb.append("                 T6.FINSCHOOL_ZIPCD = T11.NEW_ZIPCD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T12 ON ");
            stb.append("                 T12.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                 T12.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                 T12.APPLICANTDIV = '1' AND ");
            stb.append("                 T12.TESTDIV <> T1.TESTDIV ");            
            if (_param.isWakayama() && "1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                stb.append("                 AND T12.TESTDIV = '1' ");//前期
            }
            stb.append("     LEFT JOIN NAME_MST T13 ON ");
            stb.append("                 T13.NAMECD1 = 'L004' AND ");
            stb.append("                 T13.NAMECD2 = T12.TESTDIV ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT T14 ON ");
            stb.append("                 T14.ENTEXAMYEAR = T12.ENTEXAMYEAR AND ");
            stb.append("                 T14.EXAMNO = T12.EXAMNO AND ");
            stb.append("                 T14.APPLICANTDIV = T12.APPLICANTDIV AND ");
            stb.append("                 T14.TESTDIV = T12.TESTDIV AND ");
            stb.append("                 T14.TESTDIV = '1' ");//Ａ日程 //前期
            stb.append("     LEFT JOIN NAME_MST T15 ON ");
            stb.append("                 T15.NAMECD1 = 'L013' AND ");
            stb.append("                 T15.NAMECD2 = T14.JUDGEDIV ");
            if (_param.isWakayama() && "1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
            } else {
                stb.append("                 AND T15.NAMESPARE1 = '1' ");//合格扱い
            }
            stb.append("     LEFT JOIN PRISCHOOL_MST P1 ON ");
            stb.append("         P1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("     LEFT JOIN FINHIGHSCHOOL_MST FINH ON ");
            stb.append("         FINH.FINSCHOOLCD = T1.SH_SCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST NM1 ON ");
            stb.append("                 NM1.NAMECD1 = 'L027' AND ");
            stb.append("                 NM1.NAMECD2 = T1.GENERAL_FLG ");
            stb.append(" ORDER BY ");
            if ("1".equals(_param._sortType)) {
                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    stb.append("     VALUE(T1.AVERAGE1,-1) desc, T1.EXAMNO, T10.SHOWORDER ");
                } else {
                    stb.append("     INT(VALUE(T1.TOTAL4,-1)) desc, T1.EXAMNO, T10.SHOWORDER ");
                }
            }
            if ("2".equals(_param._sortType)) stb.append("     T1.EXAMNO, T10.SHOWORDER ");
            
        } catch (Exception e) {
            log.error("preStat1 error!", e);
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String testDiv, String shDiv, String selSubDiv)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+_param._year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+_param._applicantDiv+"' ");
            stb.append("            AND W1.TESTDIV= '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("            AND W2.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("            AND W2.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            stb.append("            AND W2.ENTEXAMYEAR=W1.ENTEXAMYEAR  ");
            stb.append("            AND W2.EXAMNO=W1.EXAMNO  ");
            stb.append("            AND W2.APPLICANTDIV=W1.APPLICANTDIV  ");
            stb.append("            AND W2.TESTDIV=W1.TESTDIV  ");
            stb.append("     GROUP BY W1.TESTDIV ) T1  ");
        } catch (Exception e) {
            log.error("preStatTotalPage error!", e);
        }
        return stb.toString();

    }//preStat2()の括り

    /** 志願者数、受験者数、合格者数を取得する */
    private String preStatTestApplicantCount(String testDiv, String shDiv, String selSubDiv, String sub2ka3kaDiv){
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" select ");
            stb.append("      'APPLICANT' AS NAME, t1.SEX, count(*) AS COUNT ");
            stb.append(" from ");
            stb.append("      ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append(" where  ");
            stb.append("     t1.ENTEXAMYEAR='"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV='"+_param._applicantDiv+"' and ");
            stb.append("     t1.TESTDIV='"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("     and t1.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     and t1.SELECT_SUBCLASS_DIV = '" + selSubDiv + "' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     and t1.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     and t1.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
            }
            stb.append(" group by ");
            stb.append("      SEX ");
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("      'DESIRE' AS NAME, t1.SEX, count(*) AS COUNT ");
            stb.append(" from ");
            stb.append("      ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("     inner join ENTEXAM_RECEPT_DAT t2 on  ");
            stb.append("         t1.ENTEXAMYEAR = t2.ENTEXAMYEAR and ");
            stb.append("         t1.APPLICANTDIV = t2.APPLICANTDIV and ");
            stb.append("         t1.TESTDIV = t2.TESTDIV and ");
            stb.append("         t1.EXAMNO = t2.EXAMNO         ");
            stb.append(" where  ");
            stb.append("     t1.ENTEXAMYEAR='"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV='"+_param._applicantDiv+"' and ");
            stb.append("     t1.TESTDIV='"+testDiv+"' and ");
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     t2.TOTAL2 is not null ");
            } else {
                stb.append("     t2.TOTAL4 is not null ");
            }
            if (!"9".equals(shDiv)) {
                stb.append("     and t1.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     and t1.SELECT_SUBCLASS_DIV = '" + selSubDiv + "' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     and t1.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     and t1.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
            }
            stb.append(" group by ");
            stb.append("      sex ");
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("     'RECEPT' AS NAME, t1.SEX, count(*) AS COUNT ");
            stb.append(" from ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("     inner join ENTEXAM_RECEPT_DAT t2 on  ");
            stb.append("         t1.ENTEXAMYEAR = t2.ENTEXAMYEAR and ");
            stb.append("         t1.APPLICANTDIV = t2.APPLICANTDIV and ");
            stb.append("         t1.TESTDIV = t2.TESTDIV and ");
            stb.append("         t1.EXAMNO = t2.EXAMNO ");
            stb.append(" where  ");
            stb.append("     t1.ENTEXAMYEAR='"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV='"+_param._applicantDiv+"' and ");
            stb.append("     t1.TESTDIV='"+testDiv+"' and ");
            stb.append("     t2.JUDGEDIV in ('1','7','8','9','A','B') ");
            if (!"9".equals(shDiv)) {
                stb.append("     and t1.SHDIV = '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     and t1.SELECT_SUBCLASS_DIV = '" + selSubDiv + "' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     and t1.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     and t1.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
            }
            stb.append(" group by ");
            stb.append("      SEX ");
            stb.append(" order by ");
            stb.append("      NAME ");
        } catch (Exception e) {
            log.error("preStatApplicantCount error!", e);
        }
        return stb.toString();
    }
    
    /** 合格者の最高点、最低点、平均点を取得する */
    private String preStatTestScoreData(String testDiv, String shDiv, String selSubDiv, String sub2ka3kaDiv){
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" with t_subclass as( ");
            stb.append(" SELECT ");
            stb.append("     ENTEXAMYEAR, ");
            stb.append("     APPLICANTDIV, ");
            stb.append("     TESTDIV, ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     NAME_MST.NAME1 AS TESTSUBCLASSNAME, ");
            stb.append("     SHOWORDER ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT ");
            stb.append("         LEFT JOIN NAME_MST ON ");
            stb.append("             NAMECD1 = 'L009' AND ");
            stb.append("             NAMECD2 = TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     TESTDIV= '"+testDiv+"' ");
            stb.append(" ORDER BY ");
            stb.append("     APPLICANTDIV, ");
            stb.append("     SHOWORDER ");
            stb.append(" ), t_score as( ");
            stb.append(" SELECT ");
            stb.append("     t1.receptno, ");
            stb.append("     t1.score, ");
            stb.append("     t2.* ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT t1 ");
            stb.append("         inner join t_subclass t2 on ");
            stb.append("             t1.ENTEXAMYEAR= t2.ENTEXAMYEAR AND ");
            stb.append("             t1.APPLICANTDIV = t2.APPLICANTDIV AND ");
            stb.append("             t1.TESTDIV = t2.TESTDIV AND ");
            stb.append("             t1.TESTSUBCLASSCD = t2.TESTSUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("     t1.APPLICANTDIV ");
            stb.append(" ), t_score2 as( ");
            stb.append(" SELECT ");
            stb.append("     T10.SHOWORDER, ");
            stb.append("     T10.TESTSUBCLASSCD, ");
            stb.append("     T10.TESTSUBCLASSNAME, ");
            stb.append("     W3.SEX, ");
            stb.append("     T10.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("             W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("             W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("             W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("             W3.EXAMNO = W1.EXAMNO ");
            stb.append("         LEFT JOIN t_score T10 ON ");
            stb.append("             W1.ENTEXAMYEAR = T10.ENTEXAMYEAR AND ");
            stb.append("             W1.APPLICANTDIV = T10.APPLICANTDIV AND ");
            stb.append("             W1.TESTDIV = T10.TESTDIV AND ");
            stb.append("             W1.RECEPTNO = T10.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
            stb.append("     W1.JUDGEDIV in ('1','7','8','9','A','B') ");
            if (!"9".equals(shDiv)) {
                stb.append("     AND W3.SHDIV= '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     AND W3.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
                stb.append("     AND T10.TESTSUBCLASSCD in ('" + KOKUGO + "','" + SANSUU + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     AND W3.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
                stb.append("     AND T10.TESTSUBCLASSCD in ('" + KOKUGO + "','" + SANSUU + "','" + RIKA + "') ");
            }
            stb.append(" UNION ALL     ");
            stb.append(" SELECT ");
            stb.append("     9 AS SHOWORDER, ");
            stb.append("     '9' AS TESTSUBCLASSCD, ");
            stb.append("     '合計' AS TESTSUBCLASSNAME, ");
            stb.append("     W3.SEX, ");
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     W1.TOTAL2 AS SCORE ");
            } else {
                stb.append("     W1.TOTAL4 AS SCORE ");
            }
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("             W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("             W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("             W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("             W3.EXAMNO = W1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
            stb.append("     W1.JUDGEDIV in ('1','7','8','9','A','B') ");
            if (!"9".equals(shDiv)) {
                stb.append("     AND W3.SHDIV= '" + shDiv + "' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     AND W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     AND W3.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     AND W3.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
            }
            stb.append(" ), t_score3 as( ");
            stb.append(" select ");
            stb.append("     SHOWORDER, ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     TESTSUBCLASSNAME, ");
            stb.append("     '9' AS SEX, ");
            stb.append("     avg(SCORE*1.0) AS AVERAGE, ");
            stb.append("     max(SCORE) AS MAX, ");
            stb.append("     min(SCORE) AS MIN ");
            stb.append(" from ");
            stb.append("     t_score2 ");
            stb.append(" group by ");
            stb.append("     SHOWORDER, TESTSUBCLASSCD, TESTSUBCLASSNAME ");
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("     SHOWORDER, ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     TESTSUBCLASSNAME, ");
            stb.append("     SEX, ");
            stb.append("     avg(SCORE*1.0) AS AVERAGE, ");
            stb.append("     max(SCORE) AS MAX, ");
            stb.append("     min(SCORE) AS MIN ");
            stb.append(" from ");
            stb.append("     t_score2 ");
            stb.append(" group by ");
            stb.append("     SEX, SHOWORDER, TESTSUBCLASSCD , TESTSUBCLASSNAME ");
            stb.append(" order by  ");
            stb.append("     TESTSUBCLASSCD      ");
            stb.append(" )select ");
            stb.append("     t1.SHOWORDER, ");
            stb.append("     t1.TESTSUBCLASSCD, ");
            stb.append("     t1.TESTSUBCLASSNAME, ");
            stb.append("     t2.AVERAGE AS AVERAGE1, ");
            stb.append("     t3.AVERAGE AS AVERAGE2, ");
            stb.append("     t1.AVERAGE AS AVERAGE3, ");
            stb.append("     t1.MAX AS MAX, ");
            stb.append("     t1.MIN AS MIN ");
            stb.append(" from t_score3 t1 ");
            stb.append("     left join t_score3 t2 on ");
            stb.append("         t2.TESTSUBCLASSCD = t1.TESTSUBCLASSCD and ");
            stb.append("         t2.SEX = '1' ");
            stb.append("     left join t_score3 t3 on ");
            stb.append("         t3.TESTSUBCLASSCD = t1.TESTSUBCLASSCD and ");
            stb.append("         t3.SEX = '2' ");
            stb.append(" where t1.SEX = '9' ");
            
        } catch (Exception e) {
            log.error("preStatTestScoreData error!", e);
        }
        return stb.toString();        
    }

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            if(ps!=null) ps.close();
            if(ps1!=null) ps1.close();
            if(ps2!=null) ps2.close();
        } catch (Exception e) {
            log.error("preStatClose error! =", e);
        }
    }//preStatClose()の括り
    
    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name 帳票出力エリア
     * @param area_len      制限文字数
     * @param sval          値
     * @return
     */
    private String setformatArea(String area_name, int area_len, String sval) {

        String retAreaName = "";
        // 値がnullの場合はnullが返される
        if (sval == null) {
            return null;
        }
        // 設定値が制限文字超の場合、帳票設定エリアの変更を行う
        if(area_len >= sval.length()){
            retAreaName = area_name + "1";
        } else {
            retAreaName = area_name + "2";
        }
        return retAreaName;
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

    private void setSvfMainBunpu(
            DB2UDB db2,
            Vrw32alp svf,
            String testDiv,
            String shDiv,
            String sub2ka3kaDiv
        ) {
            PreparedStatement ps1 = null;
            PreparedStatement ps2 = null;
            List subclassList = new ArrayList();
            List distributionList = new ArrayList();
            subclassList.add(distributionList);
            try {
                String sql1 = preStat1Bunpu(testDiv, shDiv, sub2ka3kaDiv);
                log.debug("preStat1Bunpu sql="+sql1);
                ps1 = db2.prepareStatement(sql1);

                ResultSet rs = ps1.executeQuery();
                int oldTestSubclassCd = -1;
                int testSubclassCd = -1;
                // 各科目の分布データ
                while(rs.next()) {
                    testSubclassCd = Integer.valueOf(rs.getString("TESTSUBCLASSCD")).intValue();
                    if (testSubclassCd != oldTestSubclassCd) {
                        distributionList = new ArrayList();
                        subclassList.add(distributionList);
                    }
                    int perfect = Integer.valueOf(rs.getString("PERFECT")).intValue();
                    int distIndex = Integer.valueOf(rs.getString("DISTINDEX")).intValue();
                    int distCount = Integer.valueOf(rs.getString("COUNT")).intValue();
                    TestScoreBunpu ts = new TestScoreBunpu(testSubclassCd, perfect, distIndex, distCount);
                    distributionList.add(ts);
                    oldTestSubclassCd = testSubclassCd;
                }
                rs.close();
                db2.commit();
                
                // 合計点の分布データ
                String sql2 = preStat2Bunpu(testDiv, shDiv, sub2ka3kaDiv);
                log.debug("preStat2Bunpu sql="+sql2);
                ps2 = db2.prepareStatement(sql2);

                rs = ps2.executeQuery();
                distributionList = new ArrayList();
                subclassList.add(distributionList);
                
                while(rs.next()) {
                    testSubclassCd = 999; // 合計の仮の科目コードとする
                    int perfect = _param._totalPerfect; // 合計点
                    String distIndexStr = rs.getString("DISTINDEX");
                    String countStr = rs.getString("COUNT");
                    if (distIndexStr == null || countStr == null) { continue; }
                    int distIndex = Integer.valueOf(distIndexStr).intValue();
                    int distCount = Integer.valueOf(countStr).intValue();
                    TestScoreBunpu ts = new TestScoreBunpu(testSubclassCd, perfect, distIndex, distCount);
                    distributionList.add(ts);
                }
                
                if (null != subclassList) {
                    setSvfout2Bunpu(db2, svf, subclassList, testDiv, sub2ka3kaDiv); //帳票出力のメソッド
                }
            } catch (Exception ex) {
                log.error("setSvfMainBunpu set error!", ex);
            }
        }

    private void setSvfout2Bunpu(
        DB2UDB db2,
        Vrw32alp svf,
        List subclassList,
        String testDiv,
        String sub2ka3kaDiv
    ) {
        try {
            int subclassSize = subclassList.size();
            int[] subclassDiv = new int[subclassSize]; // 
            int subclassDiv150 = 1; // 150点満点の科目は 値が1～10
            int subclassDiv100 = 11; // 100点満点の科目は 値が11～
            int[] totalScore = new int[subclassSize];
            int[][] distribution = new int[subclassSize][];
            String[] subclassName = new String[subclassSize];
            log.debug("subclassList.size="+subclassSize);
            for(int si = 0; si < subclassSize; si++) {
                List distributionList = (List) subclassList.get(si);
                if (null == distributionList || distributionList.size()==0) continue;

                TestScoreBunpu firstElement = (TestScoreBunpu) distributionList.get(0);
                subclassName[si] = getTestSubclassName(String.valueOf(firstElement._testSubclassCd));
                int kizami = (firstElement._perfect==_param._totalPerfect) ? 10 : 5;
                distribution[si] = new int[firstElement._perfect/kizami + 1];
                if (firstElement._perfect==150) {
                    subclassDiv[si] = subclassDiv150;
                    subclassDiv150++;
                } else if (firstElement._perfect==100) {
                    subclassDiv[si] = subclassDiv100;
                    subclassDiv100++;
                } else{
                    subclassDiv[si] = 0;
                }

                for(Iterator distIterator = distributionList.iterator(); distIterator.hasNext();) {
                    TestScoreBunpu ts = (TestScoreBunpu) distIterator.next();
                    if(999 == ts._distIndex) {
                        totalScore[si] = ts._count; // 科目の総人数合計スコア
                    } else {
                        distribution[si][ts._distIndex] = ts._count;
                    }
                }
            }
            log.debug("dislen=" + distribution.length);
            for (int i = 0; i < distribution.length; i++) {
                if (distribution[i] == null) {
                    continue;
                }
                log.debug("dislen" + i + "=" + distribution[i].length);
            }
            int[] cumulative = new int[subclassSize];
            // 出力 (総合と150点満点科目)
            for (int i = 0; i < 51; i++) {
                //log.debug("lineIndex=" + i);
                for (int si = 0; si < subclassSize; si++) {
                    //log.debug("sN si="+si);
                    if (distribution[si] == null || distribution[si].length <= i) {
                        continue;
                    }
                    int subDiv = subclassDiv[si];
                    if (subDiv > 10) {
                        continue; // 100点満点の科目はここでは出力しない
                    }
                    cumulative[si] += distribution[si][i];
                    final String average = cumulative[si] == 0 ? "" : new BigDecimal(totalScore[si]).divide(new BigDecimal(cumulative[si]), 1, BigDecimal.ROUND_HALF_UP).toString();
                    
                    if(subDiv == 0) {
                        if (0 != distribution[si][i]) {
                            svf.VrsOutn("COUNT1", i+1, String.valueOf(distribution[si][i]));
                        }
                        if (0 != cumulative[si]) {
                            svf.VrsOutn("CUMULATIVE1", i+1, String.valueOf(cumulative[si]));
                        }
                        svf.VrsOut("TOTAL_COUNT1", String.valueOf(cumulative[si]));
                        svf.VrsOut("TOTAL_AVERAGE1", average);
                    } else if(subDiv <= 10) {
                        svf.VrsOut("SUBCLASS2_"+subDiv, subclassName[si]);
                        if (0 != distribution[si][i]) {
                            svf.VrsOutn("COUNT2_" + subDiv, i+1, String.valueOf(distribution[si][i]));
                        }
                        if (0 != cumulative[si]) {
                            svf.VrsOutn("CUMULATIVE2_" + subDiv, i+1, String.valueOf(cumulative[si]));
                        }
                        svf.VrsOut("TOTAL_COUNT2_" + subDiv, String.valueOf(cumulative[si]));
                        svf.VrsOut("TOTAL_AVERAGE2_" + subDiv, average);
                    }
                }
            }

            // 出力 (総合と100点満点科目)
            // 100点満点の科目はフォームの繰り返しレコードの関係で別に出力する。
            int fieldNo = 3;
            for (int si = 0; si < subclassSize; si++) {
                int subDiv = subclassDiv[si];
                if (subDiv <= 10) {
                    continue; // 100点満点以外の科目は処理をしない
                }
                if (distribution[si] == null) {
                    continue;
                }
                cumulative[si] = 0;
                for(int i = 0; i < 21; i++) {
                    if (distribution[si].length <= i) {
                        continue;
                    }
                    cumulative[si] += distribution[si][i];
                    final String average = cumulative[si] == 0 ? "" : new BigDecimal(totalScore[si]).divide(new BigDecimal(cumulative[si]), 1, BigDecimal.ROUND_HALF_UP).toString();
                    
                    svf.VrsOut("SUBCLASS" + fieldNo, subclassName[si]);
                    if (0 != distribution[si][i]) {
                        svf.VrsOutn("COUNT" + fieldNo, i + 1, String.valueOf(distribution[si][i]));
                    }
                    if (0 != cumulative[si]) {
                        svf.VrsOutn("CUMULATIVE" + fieldNo, i + 1, String.valueOf(cumulative[si]));
                    }
                    svf.VrsOut("TOTAL_COUNT" + fieldNo, String.valueOf(cumulative[si]));
                    svf.VrsOut("TOTAL_AVERAGE" + fieldNo, average);
                }
                if (_param.isCollege() && ("2".equals(_param._applicantDiv) || "1".equals(_param._applicantDiv) && "7".equals(testDiv))) {//TODO:KNJL316C_1C_2.frm用
                    fieldNo++;
                }
            }
            
        } catch (Exception ex) {
            log.error("setSvfout2Bunpu set error! =", ex);
        }
    }

    private String getTestSubclassName(String testSubclassCd) {
        for(int i=0; i<_testSubclassName.length; i++) {
            if (testSubclassCd.equals(_testSubclassName[i]._testSubclassCd))
                return _testSubclassName[i]._testSubclassName;
        }
        return "";
    }
    
    /**得点分布を取得**/ 
    private String preStat1Bunpu(String testDiv, String shDiv, String sub2ka3kaDiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" with T_ENTEXAM_SUBCLASS as( ");
            stb.append(" select ");
            stb.append("     t1.*, ");
            stb.append("     t2.NAME1 AS SUBCLASSNAME, ");
            stb.append("     5 AS KIZAMI ");
            stb.append(" from ");
            stb.append("     ENTEXAM_PERFECT_MST t1 ");
            stb.append("         left join NAME_MST t2 on ");
            stb.append("             t2.NAMECD1 = 'L009' and ");
            stb.append("             t2.NAMECD2 = t1.TESTSUBCLASSCD ");
            stb.append(" where  ");
            stb.append("     t1.ENTEXAMYEAR='"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV = '"+_param._applicantDiv+"' and ");
            stb.append("     t1.TESTDIV = '"+testDiv+"' ");
            stb.append(" order by ");
            stb.append("     PERFECT desc, TESTSUBCLASSCD ");
            stb.append(" ), T_ENTEXAM_SCORE as( ");
            stb.append(" select ");
            stb.append("     t1.ENTEXAMYEAR, ");
            stb.append("     t1.APPLICANTDIV, ");
            stb.append("     t1.TESTDIV, ");
            stb.append("     t1.RECEPTNO, ");
            stb.append("     t1.TESTSUBCLASSCD, ");
            stb.append("     t2.SUBCLASSNAME, ");
            stb.append("     t2.PERFECT, ");
            stb.append("     t1.SCORE, ");
            stb.append("     (case when MOD(t2.PERFECT - t1.SCORE, t2.KIZAMI) = 0 then 0 ");
            stb.append("      else 1 end) + (t2.PERFECT - t1.SCORE)/t2.KIZAMI AS DISTINDEX ");
            stb.append(" from ENTEXAM_SCORE_DAT t1 ");
            stb.append("     left join T_ENTEXAM_SUBCLASS t2 on ");
            stb.append("         t1.ENTEXAMYEAR = t2.ENTEXAMYEAR and ");
            stb.append("         t1.APPLICANTDIV = t2.APPLICANTDIV and ");
            stb.append("         t1.TESTDIV = t2.TESTDIV and ");
            stb.append("         t1.TESTSUBCLASSCD = t2.TESTSUBCLASSCD ");
            stb.append("     left join ENTEXAM_RECEPT_DAT l1 on ");
            stb.append("         l1.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("         and l1.APPLICANTDIV = t1.APPLICANTDIV ");
            stb.append("         and l1.TESTDIV = t1.TESTDIV ");
            stb.append("         and l1.RECEPTNO = t1.RECEPTNO ");
            stb.append("     left join ENTEXAM_APPLICANTBASE_DAT l2 on ");
            stb.append("         l2.ENTEXAMYEAR = t1.ENTEXAMYEAR and ");
            stb.append("         l2.APPLICANTDIV = t1.APPLICANTDIV and ");
            stb.append("         l2.TESTDIV = t1.TESTDIV and ");
            stb.append("         l2.EXAMNO = l1.EXAMNO ");
            stb.append(" where  ");
            stb.append("     t1.ATTEND_FLG = '1' and ");
            stb.append("     t1.ENTEXAMYEAR='"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV = '"+_param._applicantDiv+"' and ");
            stb.append("     t1.TESTDIV = '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("     and l2.SHDIV = '" + shDiv + "' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     and l2.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
                stb.append("     and t1.TESTSUBCLASSCD in ('" + KOKUGO + "','" + SANSUU + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     and l2.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
                stb.append("     and t1.TESTSUBCLASSCD in ('" + KOKUGO + "','" + SANSUU + "','" + RIKA + "') ");
            }
            stb.append(" ) ");
            stb.append(" select  ");
            stb.append("     t1.TESTSUBCLASSCD, ");
            stb.append("     t1.PERFECT, ");
            stb.append("     t1.SUBCLASSNAME,  ");
            stb.append("     t1.DISTINDEX, ");
            stb.append("     count(*) as COUNT ");
            stb.append(" from ");
            stb.append("     T_ENTEXAM_SCORE t1 ");
            stb.append(" group by ");
            stb.append("     t1.TESTSUBCLASSCD, ");
            stb.append("     t1.PERFECT, ");
            stb.append("     t1.SUBCLASSNAME,  ");
            stb.append("     t1.DISTINDEX ");
            stb.append(" union ");
            stb.append(" select ");
            stb.append("     t1.TESTSUBCLASSCD,  ");
            stb.append("     t1.PERFECT, ");
            stb.append("     t1.SUBCLASSNAME, ");
            stb.append("     999 AS DISTINDEX, ");
            stb.append("     SUM(SCORE) as COUNT   ");
            stb.append(" from ");
            stb.append("     T_ENTEXAM_SCORE t1 ");
            stb.append(" group by      ");
            stb.append("     t1.TESTSUBCLASSCD, ");
            stb.append("     t1.PERFECT, ");
            stb.append("     t1.SUBCLASSNAME ");
            stb.append(" order by ");
            stb.append("     TESTSUBCLASSCD ");
        } catch (Exception e) {
            log.error("preStat1Bunpu error!", e);
        }
        return stb.toString();

    }//preStat1Bunpu()の括り

    
    /** 得点分布 (総合)を取得 **/ 
    private String preStat2Bunpu(String testDiv, String shDiv, String sub2ka3kaDiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" with T_TOTALSCORE as( ");
            stb.append("     select ");
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("         w1.TOTAL2 AS TOTAL ");
            } else {
                stb.append("         w1.TOTAL4 AS TOTAL ");
            }
            stb.append("     from ");
            stb.append("         ENTEXAM_RECEPT_DAT w1 ");
            stb.append("         left join ENTEXAM_APPLICANTBASE_DAT l1 on ");
            stb.append("             l1.ENTEXAMYEAR = w1.ENTEXAMYEAR and ");
            stb.append("             l1.APPLICANTDIV = w1.APPLICANTDIV and ");
            stb.append("             l1.TESTDIV = w1.TESTDIV and ");
            stb.append("             l1.EXAMNO = w1.EXAMNO ");
            stb.append("     where ");
            stb.append("         w1.ATTEND_ALL_FLG = '1' and ");
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("         w1.TOTAL2 is not null and ");
            } else {
                stb.append("         w1.TOTAL4 is not null and ");
            }
            stb.append("         w1.ENTEXAMYEAR = '"+_param._year+"' and ");
            stb.append("         w1.APPLICANTDIV = '"+_param._applicantDiv+"' and ");
            stb.append("         w1.TESTDIV = '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("         and l1.SHDIV = '" + shDiv + "' ");
            }
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("     (case when MOD("+_param._totalPerfect+" - t1.TOTAL, 10) = 0 then 0 ");
            stb.append("      else 1 end) + ("+_param._totalPerfect+" - t1.TOTAL)/10 AS DISTINDEX, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" from ");
            stb.append("     T_TOTALSCORE t1 ");
            stb.append(" group by ");
            stb.append("     (case when MOD("+_param._totalPerfect+" - t1.TOTAL, 10) = 0 then 0 ");
            stb.append("      else 1 end) + ("+_param._totalPerfect+" - t1.TOTAL)/10 ");
            stb.append(" union ");
            stb.append(" select ");
            stb.append("     999 AS DISTINDEX, ");
            stb.append("     SUM(t2.TOTAL) AS COUNT ");
            stb.append(" from ");
            stb.append("     T_TOTALSCORE t2 ");
            stb.append(" order by DISTINDEX ");
        } catch (Exception e) {
            log.error("preStat2Bunpu error!", e);
        }
        return stb.toString();

    }//preStat2Bunpu()の括り

    class TestScoreBunpu {
        final int _testSubclassCd;
        final int _perfect;
        final int _distIndex; // 点数の領域のインデックス 500点満点だと10点きざみで 500点->0, 499点->1, 490点->1, 489点->2, 0点->50 
        final int _count;     // その領域の点数の人数
        public TestScoreBunpu(int testSubclassCd, int perfect, int distindex, int count){
            _testSubclassCd = testSubclassCd;
            _perfect = perfect;
            _distIndex = distindex;
            _count = count;
        }
    }
    
    class TestRecord {
        final String _number;
        final String _examno;
        final String _name;
        final String _shdiv;
        final String _sexname;
        final String _pref;
        final String _fschool;
        final String _total4;
        final String _average4;
        final String _total_rank4;
        final String _subOrder;
        final String _total2;
        final String _average2;
        final String _average1;
        final String _total_rank1;
        final String _judgediv;
        final String _judgeName;
        final String _remark1;
        final String _remark2;
        final String _preReceptno;
        final String _prischoolName;
        final String _recomExamno;
        final String _otherTestDivName;
        final String _otherJudgeDivName;
        final String _otherProcedureDiv;
        final String _shiftDesireFlg;
        final String _slideFlg;
        final String _sportsFlg;
        final String _generalFlgName;
        final String _dormitoryFlg;
        final String _shSchooldiv;
        final String _shSchoolname;
        List _testSubclass = new ArrayList();
        public TestRecord(String number,String examno,String name,String shdiv,String sexname,
                String pref,String fschool, String total4, String average4, String rank4,
                String subOrder, String total2, String average2, String average1, String rank1,
                String judgediv, String judgeName, String remark1, String remark2, String preReceptno, String prischoolName,
                String recomExamno, String otherTestDivName, String otherJudgeDivName, String otherProcedureDiv,
                String shiftDesireFlg,
                final String slideFlg, final String sportsFlg, final String generalFlgName, final String dormitoryFlg,
                final String shSchooldiv, final String shSchoolname
        ){
            _number = number;
            _examno = examno;
            _name = name;
            _shdiv = shdiv;
            _sexname = sexname;
            _pref = pref;
            _fschool = fschool;
            _total4 = total4;
            _average4 = average4;
            _total_rank4 = rank4;
            _subOrder = subOrder;
            _total2 = total2;
            _average2 = average2;
            _average1 = average1;
            _total_rank1 = rank1;
            _judgediv = judgediv;
            _judgeName = judgeName;
            _remark1 = remark1;
            _remark2 = remark2;
            _preReceptno = preReceptno;
            _prischoolName = prischoolName;
            _recomExamno = recomExamno;
            _otherTestDivName = otherTestDivName;
            _otherJudgeDivName = otherJudgeDivName;
            _otherProcedureDiv = otherProcedureDiv;
            _shiftDesireFlg = shiftDesireFlg;
            _slideFlg = slideFlg;
            _sportsFlg = sportsFlg;
            _generalFlgName = generalFlgName;
            _dormitoryFlg = dormitoryFlg;
            _shSchooldiv = shSchooldiv;
            _shSchoolname = shSchoolname;
        }
        
        public void addTestSubclass(
                int showOrder,
                int testScore,
                String isSelectSubclass,
                String testSubclassDiv,
                String testSubclassCd,
                String attendflg) {
            boolean isHyphen = false;
            if (null == isSelectSubclass || !"1".equals(isSelectSubclass)) {
                isHyphen = false;
            } else if (testSubclassDiv != null && !testSubclassDiv.equals(testSubclassCd)) {
                isHyphen = true;
            } else {
                isHyphen = false;
            }
            boolean attend = (null!=attendflg && "1".equals(attendflg));
            
            _testSubclass.add(new TestScore(showOrder, testScore, isHyphen, attend));
        }
        
        public TestScore getTestScore(int showOrder) {
            for(Iterator it=_testSubclass.iterator(); it.hasNext(); ) {
                TestScore ts = (TestScore) it.next();
                if (ts._showOrder == showOrder) {
                    return ts;
                }
            }
            return null;
        }
    }

    class TestSubclassName implements Comparable{
        final int _showOrder;
        final boolean _isSelectSubclass;
        final String _testSubclassName;
        final String _testSubclassCd;
        public TestSubclassName(int showOrder, boolean isSelectSubclass, String testSubclassName, String testSubclassCd) {
            _showOrder = showOrder;
            _isSelectSubclass = isSelectSubclass;
            _testSubclassName = testSubclassName;
            _testSubclassCd = testSubclassCd;
        }
        public int compareTo(Object o) {
            if (!(o instanceof TestSubclassName))
                return -1;
            TestSubclassName other = (TestSubclassName) o;
            return new Integer(_showOrder).compareTo(new Integer(other._showOrder));
        }
    }
    
    
    class TestScore implements Comparable{
        final int _showOrder;
        final private int _testScore;
        final private boolean _isHyphen;
        final boolean _attend;
        public TestScore(int showOrder, int testScore, boolean isHyphen, boolean attend) {
            _showOrder = showOrder;
            _testScore = testScore;
            _isHyphen = isHyphen;
            _attend = attend;
        }
        public int compareTo(Object o) {
            if (!(o instanceof TestScore))
                return -1;
            TestScore other = (TestScore) o;
            return new Integer(_showOrder).compareTo(new Integer(other._showOrder));
        }
        public String getTestScore() {
            if (_isHyphen) return "  -  ";
            return ""+_testScore;
        }
    }
    
    class AverageScore{
        final int _showOrder;
        final String _totalScore;
        final int _count;
        public AverageScore(int showOrder, String totalScore, int count) {
            _showOrder = showOrder;
            _totalScore = totalScore;
            _count = count;
        }

        public String getAverage() {
            if (!NumberUtils.isNumber(_totalScore) || _count == 0) {
                return null;
            }
            return new BigDecimal(_totalScore).divide(new BigDecimal(_count), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        public int getShowOrder() {
            return _showOrder;
        }

        public int getCount() {
            return _count;
        }
    }
    
    class TestScoreNews {
        private String _subclassName;
        private int _showOrder;
        private String _averageMan;
        private String _averageWoman;
        private String _averageAll;
        private String _max;
        private String _min;

        public TestScoreNews(String subclassName,int showOrder) {
            _subclassName = subclassName;
            _showOrder = showOrder;
        }
        public void setAverageMan(String averageMan){
            _averageMan = averageMan;
        }
        public void setAverageWoman(String averageWoman){
            _averageWoman = averageWoman;
        }
        public void setAverageAll(String averageAll){
            _averageAll = averageAll;
        }
        public void setMax(String max) {
            _max = max;
        }
        public void setMin(String min) {
            _min = min;
        }
        public String toString() {
            return _showOrder+","+_subclassName+","+_averageMan+","+_averageWoman+","+_averageAll+","+_max+","+_min;
        }
    }


    class Param {
        
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _loginDate;
        final String _sortType;
        final boolean _output;
        final String _shDiv;

        final boolean _seirekiFlg;
        final String _z010SchoolCode;

        final int _totalPerfect; //合計の分布の満点
        
        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _testDiv = request.getParameter("TESTDIV");                      //入試区分
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _sortType = request.getParameter("SORT");                        // 印刷順序
            _output = (!"1".equals(request.getParameter("OUTPUT"))) ? true : false ;
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _shDiv = request.getParameter("SHDIV");
            _z010SchoolCode = getSchoolCode(db2);
            _totalPerfect = isGojo() ? 400 : 500;
        }
        
        String getNendo() {
            return _seirekiFlg ? _year+"年度":
                KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }
        
        String getLoginDateString() {
            return getDateString(_loginDate);
        }

        String getDateString(String dateFormat) {
            if (null != dateFormat) {
                return _seirekiFlg ?
                        dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat):
                            KNJ_EditDate.h_format_JP(dateFormat ) ;        
            }
            return null;
        }
        
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
        
        private String getNameMst(DB2UDB db2, String namecd1,String namecd2) {
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug(ex);
            }
            
            return name;
        }
        
        /*
         * 年度と入試制度から学校名を返す
         */
        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantDiv)) certifKindCd = "105";
            if ("2".equals(_applicantDiv)) certifKindCd = "106";
            if (certifKindCd == null) return null;
            
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
            } catch (SQLException ex) {
                log.error(ex);
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
