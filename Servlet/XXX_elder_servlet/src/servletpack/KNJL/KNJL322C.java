package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２２Ｃ＞  合格者成績一覧
 *
 *  2008/11/05 RTS 作成日
 **/

public class KNJL322C {

    private static final Log log = LogFactory.getLog(KNJL322C.class);
    private AverageScore[] _averageScore;
    private TestSubclassName[] _testSubclassName;
    private Param _param;
    
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
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ

        
        //SQL作成
        try {
            for (int m = 0; m < _param._printPassArray.length; m++) {
                String printPassSelect = _param._printPassArray[m];
                if (printPassSelect == null) continue;
                log.debug("printPassSelect="+printPassSelect);

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
                            sql = preStat1(testDiv, shDiv, selSubDiv, printPassSelect);
                            log.debug("preStat1 sql="+sql);
                            ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement
                            
                            sql = preStatTotalPage(testDiv, shDiv, selSubDiv, printPassSelect);
                            log.debug("preStateTotalPage sql="+sql);
                            psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement
                            
                            //SVF出力
                            if (setSvfMain(db2, svf, ps1, psTotalPage, testDiv, shDiv, selSubDiv, printPassSelect)) nonedata = true;  //帳票出力のメソッド
                        }
                    }
                }
            }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }

    //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
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
        String selSubDiv,
        String printPassSelect
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if (_param.isCollege()) {
            ret = svf.VrSetForm("2".equals(_param._applicantDiv) ? "KNJL322C_2C.frm" : "1".equals(_param._applicantDiv) && "1".equals(testDiv) ? "KNJL322C_1C_2.frm" : "KNJL322C_1C.frm", 4);
        } else if (_param.isGojo()) {
            ret = svf.VrSetForm("2".equals(_param._applicantDiv) ? "KNJL322C_2G.frm" : "KNJL322C_1G.frm", 4);
        } else {
            ret = svf.VrSetForm("KNJL322C.frm", 4);
        }

        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        String selsubdibText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdibText = ","+_param.getNameMst(db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            shdivText = "("+_param.getNameMst(db2, "L006", shDiv)+selsubdibText+")";// 画面から専併区分
        }
        ret = svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv)+shdivText);// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        ret = svf.VrsOut("DATE", _param.getLoginDateString());

        String passType = "";
        if ("1".equals(printPassSelect)){ passType="(S合格/G合格)"; }
        if ("2".equals(printPassSelect)){ passType="(S合格)"; }
        if ("3".equals(printPassSelect)){ passType="(G合格)"; }
        String sortType = "";
        if ("1".equals(_param._sortType)){ sortType="(成績順)"; }
        if ("2".equals(_param._sortType)){ sortType="(受験番号順)"; }
        String title = "合格者成績一覧"+sortType+passType;
        svf.VrsOut("TITLE", title);
        
        setTotalScore(db2, testDiv, shDiv, selSubDiv, printPassSelect);
        setTestSubclassName(db2, testDiv, shDiv, selSubDiv, printPassSelect);
        //  ＳＶＦ属性変更--->改ページ
        ret = svf.VrAttribute("TESTDIV","FF=1");
    }

    private void setTotalScore(DB2UDB db2, String testDiv, String shDiv, String selSubDiv, String printPassSelect) {
        String sql = preTotalScore(testDiv, shDiv, selSubDiv, printPassSelect);
        log.debug("setTotalScore sql="+sql);
        List averageScoreList = new ArrayList();
        try{
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String showOrder = rs.getString("SHOWORDER");
                String score = rs.getString("TOTALSCORE");
                String total = rs.getString("COUNT");
                //log.debug(showOrder+","+score+","+total);
                if (null == showOrder || null == score || null == total)
                    continue;
                AverageScore averageScore = new AverageScore(
                        Integer.valueOf(showOrder).intValue(),
                        Double.valueOf(score).doubleValue(),
                        Integer.valueOf(total).intValue());
                averageScoreList.add( averageScore );
            }
            _averageScore = new AverageScore[averageScoreList.size()];
            for(int i=0; i<averageScoreList.size(); i++) {
                _averageScore[i] = (AverageScore) averageScoreList.get(i);
            }
        }catch (Exception ex){
            log.debug("setTotalScore exception="+ex);            
        }
    }

    private void setTestSubclassName(DB2UDB db2, String testDiv, String shDiv, String selSubDiv, String printPassSelect) {
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
                if (_param.isGojo() && "7".equals(testDiv) && !"9".equals(selSubDiv) && isSelectSubclass) {
                    if (!testSubclassCd.equals(selSubDiv)) {
                        continue;
                    }
                }
                
                log.debug(showOrder+","+testSubclassName);
                testSubclassNameList.add(new TestSubclassName(
                        Integer.valueOf(showOrder).intValue(),
                        isSelectSubclass,
                        testSubclassName));
            }
            _testSubclassName = new TestSubclassName[testSubclassNameList.size()];
            for(int i=0; i<testSubclassNameList.size(); i++) {
                _testSubclassName[i] = (TestSubclassName) testSubclassNameList.get(i);
            }
        }catch (Exception ex){
            log.debug("setTotalScore exception="+ex);            
        }
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
            String selSubDiv,
            String printPassSelect
        ) {
            boolean nonedata = false;

            try {
                    if (setSvfout(db2, svf, ps1, psTotalpage, testDiv, shDiv, selSubDiv, printPassSelect)) nonedata = true; //帳票出力のメソッド
                    db2.commit();
            } catch( Exception ex ) {
                log.error("setSvfMain set error!"+ex);
            }
            return nonedata;
        }

    /** 表紙をセットする */
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String testDiv,
            String shDiv,
            String selSubDiv,
            String printPassSelect)
    {
        String passType = "";
        if ("1".equals(printPassSelect)){ passType="(S合格/G合格)"; }
        if ("2".equals(printPassSelect)){ passType="(S合格)"; }
        if ("3".equals(printPassSelect)){ passType="(G合格)"; }
        String sortType = "";
        if ("1".equals(_param._sortType)){ sortType="(成績順)"; }
        if ("2".equals(_param._sortType)){ sortType="(受験番号順)"; }
        String title = "【合格者成績一覧"+sortType+passType+"】";

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJLCVR001C.frm", 4);
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        String selsubdibText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdibText = ","+_param.getNameMst(db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            shdivText = "("+_param.getNameMst(db2, "L006", shDiv)+selsubdibText+")";// 画面から専併区分
        }
        ret = svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv)+shdivText);// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        ret = svf.VrsOut("TITLE", title);
        ret = svf.VrsOut("NOTE" ,"");
        ret = svf.VrEndRecord();//レコードを出力
    }
    
    /**総ページ数をセット**/
    private void setTotalPage(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement psTotalPage
    ) {
        try {
            ResultSet rs = psTotalPage.executeQuery();

            while( rs.next() ){
                if (rs.getString("TOTAL_PAGE") != null) 
                    svf.VrsOut("TOTAL_PAGE" ,rs.getString("TOTAL_PAGE"));
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setTotalPage set error! ="+ex);
        }

    }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        PreparedStatement psTotalpage,
        String testDiv,
        String shDiv,
        String selSubDiv,
        String printPassSelect
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
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
                           rs.getString("SEX_NAME"),
                           rs.getString("PREF_NAME"),
                           nvlT(rs.getString("FINSCHOOL_NAME")),
                           rs.getString("TOTAL4"),
                           rs.getString("AVERAGE4"),
                           rs.getString("TOTAL_RANK4"),
                           rs.getString("TOTAL2"),
                           rs.getString("AVERAGE2"),
                           rs.getString("AVERAGE1"),
                           rs.getString("TOTAL_RANK1"),
                           rs.getString("JUDGEDIV"),
                           rs.getString("JUDGEDIV_NAME"),
                           nvlT(rs.getString("REMARK1"))+nvlT(rs.getString("REMARK2")),
                           rs.getString("RECOM_EXAMNO"),
                           rs.getString("OTHER_TESTDIV_NAME"),
                           rs.getString("SHIFT_DESIRE_FLG"),
                           rs.getString("SHDIV"),
                           rs.getString("SUB_ORDER"),
                           rs.getString("SLIDE_FLG"),
                           rs.getString("SPORTS_FLG"),
                           rs.getString("GENERAL_FLG_NAME"),
                           rs.getString("DORMITORY_FLG"),
                           rs.getString("SHSCHOOL_NAME"),
                           rs.getString("PRE_RECEPTNO"),
                           rs.getString("PRISCHOOL_NAME")
                    );
                    testRecordList.add(tr);

                    //レコード数カウント
                    reccnt++;
                    if (rs.getString("SEX") != null) {
                        if (rs.getString("SEX").equals("1")) reccnt_man++;
                        if (rs.getString("SEX").equals("2")) reccnt_woman++;
                    }
                }
                //log.debug("\""+rs.getString("SHOWORDER")+"\" , \""+rs.getString("SCORE")+"\"");
                if (null != rs.getString("SHOWORDER") && null !=rs.getString("SCORE"))
                    tr.addTestSubclass(
                            Integer.valueOf(rs.getString("SHOWORDER")).intValue(),
                            Integer.valueOf(rs.getString("SCORE")).intValue(),
                            rs.getString("IS_SELECT_SUBCLASS"),
                            rs.getString("SELECT_SUBCLASS_DIV"),
                            rs.getString("TESTSUBCLASSCD")
                            );
            }
            
            int pagecnt = 1;            //現在ページ数
            int gyo = 0;                //現在ページ数の判断用（行）
            for(Iterator it=testRecordList.iterator(); it.hasNext();) {
                if (nonedata == false) {
                    setCover(db2, svf, testDiv, shDiv, selSubDiv, printPassSelect);
                    setHeader(db2, svf, testDiv, shDiv, selSubDiv, printPassSelect);
                    setTotalPage(db2, svf, psTotalpage); //総ページ数メソッド
                    nonedata = true;
                }
                TestRecord tr1 = (TestRecord) it.next();
                ret = svf.VrsOut("NUMBER", tr1._number);       //連番
                ret = svf.VrsOut("EXAMNO", tr1._examno);       //受験番号
                ret = svf.VrsOut(setformatArea("NAME", 10, tr1._name), tr1._name);//名前
                //志望コース
                if (_param.isCollege() && "1".equals(_param._applicantDiv)) {
                    svf.VrsOut("HOPE_COURSE", "6".equals(tr1._shdiv) || "9".equals(tr1._shdiv) ? "G" : "7".equals(tr1._shdiv) || "A".equals(tr1._shdiv) ? "S" : "8".equals(tr1._shdiv) || "B".equals(tr1._shdiv) ? "S/G" : "");
                }
                //志望コース・合格コース
                if (_param.isCollege() && "2".equals(_param._applicantDiv)) {
                    svf.VrsOut("HOPE_COURSE", "3".equals(tr1._shdiv) ? "EA" : "4".equals(tr1._shdiv) ? "ES" : "5".equals(tr1._shdiv) ? "EA/ES" : "");
                    svf.VrsOut("PASS_COURSE", "A".equals(tr1._judgediv) ? "EA" : "B".equals(tr1._judgediv) ? "ES" : "");
                }
                ret = svf.VrsOut("SEX", tr1._sexname);     //性別
                ret = svf.VrsOut("PREF", tr1._pref); // 学校の都道府県
                ret = svf.VrsOut(setformatArea("FINSCHOOL", 13, tr1._fschool), tr1._fschool);
                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    ret = svf.VrsOut("SH_DIV", "1".equals(tr1._subOrder) ? "Ⅰ型" : "2".equals(tr1._subOrder) ? "Ⅱ型" : ""); //受験型 1:Ⅰ型(国算理)、2:Ⅱ型(国算)
                    ret = svf.VrsOut("TOTAL2", tr1._total2); // ２科合計
                    ret = svf.VrsOut("AVERAGE2", tr1._average2); // ２科平均
                    ret = svf.VrsOut("TOTAL3", tr1._total4); // ３科合計
                    ret = svf.VrsOut("AVERAGE3", tr1._average4); // ３科平均
                    ret = svf.VrsOut("PER_SCORE", tr1._average1); // 得点率
                    ret = svf.VrsOut("RANK", tr1._totalRank1); // 順位(得点率)
                } else {
                    ret = svf.VrsOut("TOTAL", tr1._total4); // 合計
                    ret = svf.VrsOut("AVERAGE", tr1._average4); // 平均
                    ret = svf.VrsOut("RANK", tr1._totalRank4); // 順位
                }
                ret = svf.VrsOut("JUDGE", tr1._judgeName); // 判定
                String remark = "";
                if (_param.isGojo() && "1".equals(_param._applicantDiv)) {
                    String otherTestDivName = null != tr1._otherTestDivName ? tr1._otherTestDivName + " " : "";
                    String recomExamno = null != tr1._recomExamno ? tr1._recomExamno : "";
                    svf.VrsOut("APPLICATION", otherTestDivName + recomExamno);
                    String kibou = (!"".equals(tr1._shiftDesireFlg) && null != tr1._shiftDesireFlg) ? "○": "";
                    if (_param.isCollege()) {
                        kibou = "1".equals(tr1._shiftDesireFlg) ? "五併" : "2".equals(tr1._shiftDesireFlg) ? "和併" : "3".equals(tr1._shiftDesireFlg) ? "五併/和併" : "";
                    }
                    svf.VrsOut("SHIFT", kibou);
                    svf.VrsOut("PRENO", tr1._preReceptno);
                } else if (null != tr1._otherTestDivName || (!"".equals(tr1._shiftDesireFlg) && null != tr1._shiftDesireFlg)) {
                    String otherTestDivName = null != tr1._otherTestDivName ? tr1._otherTestDivName + " " : "";
                    String kibou = _param.isGojo() ? "カレッジ併願" : "移行希望";
                    if (_param.isCollege()) {
                        kibou = "1".equals(tr1._shiftDesireFlg) ? "五併" : "2".equals(tr1._shiftDesireFlg) ? "和併" : "3".equals(tr1._shiftDesireFlg) ? "五併/和併" : "";
                    }
                    String shiftDesire = (!"".equals(tr1._shiftDesireFlg) && null != tr1._shiftDesireFlg) ? kibou + " ": "";
                    String recomExamno = null != tr1._recomExamno ? tr1._recomExamno + " " : "";
                    remark += otherTestDivName + recomExamno + shiftDesire;
                }
                if (_param.isGojo()) {
                    String juku = null != tr1._prischoolName ? tr1._prischoolName : "";
                    if ("1".equals(_param._applicantDiv)) {
                        svf.VrsOut("JUKU" + (juku.length() > 17 ? "3" : juku.length() > 10 ? "2" : ""), juku);
                        if (_param.isCollege()) {
                            svf.VrsOut("APPLYDIV", "1".equals(tr1._shdiv) || "6".equals(tr1._shdiv) || "7".equals(tr1._shdiv) || "8".equals(tr1._shdiv) ? "○" : "");
                        } else if (_param.isGojo()) {
                            svf.VrsOut("APPLYDIV", "1".equals(tr1._shdiv) ? "○" : "6".equals(tr1._shdiv) ? "Ⅰ" : "7".equals(tr1._shdiv) ? "Ⅱ" : "8".equals(tr1._shdiv) ? "Ⅲ" : "");
                        }
                        if ("1".equals(tr1._dormitoryFlg)) {
                            String dormitory = "1".equals(tr1._dormitoryFlg) ? "入寮希望" + " " : "";
                            remark += dormitory;
                        }
                    } else {
                        svf.VrsOut("JUKU" + (juku.length() > 16 ? "2" : "1"), juku);
                        if (_param.isCollege()) {
                            svf.VrsOut("SLIDE_FLG", "1".equals(tr1._slideFlg) ? "○" : "");
                        } else {
                            svf.VrsOut("SLIDE_FLG", "");
                        }
                        String heigan = null != tr1._shschoolName ? tr1._shschoolName : "";
                        svf.VrsOut("HEIGAN" + (heigan.length() > 16 ? "2" : "1"), heigan);
                        svf.VrsOut("SPORTS", "1".equals(tr1._sportsFlg) ? "○" : "");
                        svf.VrsOut("GENERAL_FLG", tr1._generalFlgName);
                        svf.VrsOut("DORMITORY", "1".equals(tr1._dormitoryFlg) ? "○" : "");
                    }
                }
                if (null != tr1._remark) { 
                    remark += tr1._remark;
                }
                int mojisuu = _param.isGojo() && "1".equals(_param._applicantDiv) ? 20 : _param.isGojo() && "2".equals(_param._applicantDiv) ? 22 : 27;
                ret = svf.VrsOut(setformatArea("REMARK", mojisuu, remark)  ,remark); // 備考
                
                if (_param.isGojo()) {
                    for(int j=0; j<_testSubclassName.length; j++) {
                        final String score;
                        if ("7".equals(testDiv) && "9".equals(selSubDiv) && _testSubclassName.length > 3 && j >= 2) {
                            final TestScore ts1 = tr1.getTestScore(_testSubclassName[2]._showOrder);
                            String s = null;
                            if (null != ts1) {
                                s = ts1.getTestScore();
                            }
                            final TestScore ts2 = tr1.getTestScore(_testSubclassName[3]._showOrder);
                            if (null != ts2) {
                                s = ts2.getTestScore();
                            }
                            score = s;
                        } else {
                            final TestScore ts = tr1.getTestScore(_testSubclassName[j]._showOrder);
                            if (null == ts) {
                                if (_testSubclassName[j]._isSelectSubclass) {
                                    score = "-";
                                } else {
                                    score = " ";
                                }
                            } else {
                                score = String.valueOf(ts.getTestScore());
                            }
                        }
                        final String field;
                        if (_testSubclassName.length > 3 && j >= 2) {
                            if (j == 2) { // 3rd
                                field = "SCORE3";
                            } else if (j == 3) { // 4th
                                field = "SCORE3";
                            } else {
                                field = null;
                            }
                        } else {
                            field = "SCORE"+(j+1);
                        }
                        svf.VrsOut(field, score);
                    }
                } else {
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
                                score = "-";
                            } else {
                                score = " ";
                            }
                        } else {
                            score = ""+ts.getTestScore();
                        }
                        
                        ret = svf.VrsOut("SCORE"+i, score);
                    }
                }
                svf.VrEndRecord();
                
                gyo ++;
                //50行超えたとき、ページ数カウント
                if (gyo % 50 == 1) {
                    ret = svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    svfOutTestName(svf, testDiv, selSubDiv);
                    log.debug(gyo+", set page ="+String.valueOf(pagecnt));
                    //svf.VrsOut("TESTDIV", pagecnt+1+"");
                    //ヘッダ
                    pagecnt++;
                }
                if (gyo % 50 == 0) {
                    svfOutCountNinzu(svf, testDiv, selSubDiv);
                    svf.VrEndRecord();
                    svfOutAverage(svf, testDiv, selSubDiv);
                    svf.VrEndRecord();
                }
            }
            if (gyo % 50 != 0) {
                for (int i=gyo%50; i<50; i++) {
                    svf.VrEndRecord();
                }
                svfOutCountNinzu(svf, testDiv, selSubDiv);
                svf.VrEndRecord();
                svfOutAverage(svf, testDiv, selSubDiv);
            }

            //最終レコードを出力
            if (nonedata) {
                //最終ページに男女合計を出力
                ret = svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                ret = svf.VrEndRecord();//レコードを出力
                setSvfInt(svf);         //ブランクセット
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error! =",ex);
        }
        return nonedata;
    }

    /** その科目を選択した人数を表示する */
    private void svfOutCountNinzu(final Vrw32alp svf, final String testDiv, final String selSubDiv) {
        if (_param.isGojo()) {
            for(int i=0; i<_testSubclassName.length; i++) {
                if ("7".equals(testDiv) && "9".equals(selSubDiv) && _testSubclassName.length > 3 && i >= 2) {
                    int count = 0;
                    for (int j=0; j<_averageScore.length; j++) {
                        if (_averageScore[j].getShowOrder() == _testSubclassName[2]._showOrder ||
                                _averageScore[j].getShowOrder() == _testSubclassName[3]._showOrder) {
                            count += _averageScore[j].getCount();
                        }
                    }
                    svf.VrsOut("SCORE3", "" + count);

                } else {
                    for (int j=0; j<_averageScore.length; j++) {
                        if (_averageScore[j].getShowOrder() == _testSubclassName[i]._showOrder) {
                            svf.VrsOut("SCORE"+(i+1), ""+_averageScore[j].getCount());
                            break;
                        }
                    }
                }
            }
        } else {
            for(int i=1; i<=8; i++) {
                for (int j=0; j<_averageScore.length; j++) {
                    if (_averageScore[j].getShowOrder() == i) {
                        svf.VrsOut("SCORE"+i, ""+_averageScore[j].getCount());
                        break;
                    }
                }
            }
        }
        if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 21) {
                    svf.VrsOut("TOTAL2", ""+_averageScore[j].getCount());
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 22) {
                    svf.VrsOut("AVERAGE2", ""+_averageScore[j].getCount());
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 9) {
                    svf.VrsOut("TOTAL3", ""+_averageScore[j].getCount());
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 99) {
                    svf.VrsOut("AVERAGE3", ""+_averageScore[j].getCount());
                    break;
                }
            }
        } else {
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 9) {
                    svf.VrsOut("TOTAL", ""+_averageScore[j].getCount());
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 99) {
                    svf.VrsOut("AVERAGE", ""+_averageScore[j].getCount());
                    break;
                }
            }
        }
    }
    
    /** 科目名を表示する */
    private void svfOutTestName(Vrw32alp svf, final String testDiv, final String selSubDiv) {
        for(int i=0; i<_testSubclassName.length; i++) {
            if (_param.isGojo()) {
                final String field;
                if ("7".equals(testDiv) && "9".equals(selSubDiv) && _testSubclassName.length > 3 && i >= 2) {
                    if (i == 2) { // 3rd
                        field = "SUBCLASS3_2";
                    } else if (i == 3) { // 4th
                        field = "SUBCLASS3_3";
                    } else {
                        field = null;
                    }
                } else {
                    field = "SUBCLASS"+(i+1);
                }
                svf.VrsOut(field, _testSubclassName[i]._testSubclassName);
            } else {
                svf.VrsOut("SUBCLASS"+_testSubclassName[i]._showOrder, _testSubclassName[i]._testSubclassName);
            }
        }
    }

    /** 平均点を表示する */
    private void svfOutAverage(Vrw32alp svf, final String testDiv, final String selSubDiv) {
        DecimalFormat df = new DecimalFormat("####.0");
        if (_param.isGojo()) {
            for(int i=0; i<_testSubclassName.length; i++) {
                if ("7".equals(testDiv) && "9".equals(selSubDiv) && _testSubclassName.length > 3 && i >= 2) {
                    double totalScore = 0.0;
                    int count = 0;
                    for (int j=0; j<_averageScore.length; j++) {
                        if (_averageScore[j].getShowOrder() == _testSubclassName[2]._showOrder ||
                                _averageScore[j].getShowOrder() == _testSubclassName[3]._showOrder) {
                            totalScore += _averageScore[j]._totalScore;
                            count += _averageScore[j].getCount();
                        }
                    }
                    if (0 != count) {
                        final AverageScore as = new AverageScore(-1, totalScore, count);
                        svf.VrsOut("SCORE3", "" + df.format(as.getAverage()));
                    }

                } else {
                    for (int j=0; j<_averageScore.length; j++) {
                        if (_averageScore[j].getShowOrder() == _testSubclassName[i]._showOrder) {
                            svf.VrsOut("SCORE"+(i+1), df.format(_averageScore[j].getAverage()));
                            break;
                        }
                    }
                }
            }
        } else {
            for(int i=1; i<=8; i++) {
                for (int j=0; j<_averageScore.length; j++) {
                    if (_averageScore[j].getShowOrder() == i) {
                        svf.VrsOut("SCORE"+i, df.format(_averageScore[j].getAverage()));
                        break;
                    }
                }
            }
        }
        if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 21) {
                    svf.VrsOut("TOTAL2", df.format(_averageScore[j].getAverage()));
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 22) {
                    svf.VrsOut("AVERAGE2", df.format(_averageScore[j].getAverage()));
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 9) {
                    svf.VrsOut("TOTAL3", df.format(_averageScore[j].getAverage()));
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 99) {
                    svf.VrsOut("AVERAGE3", df.format(_averageScore[j].getAverage()));
                    break;
                }
            }
        } else {
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 9) {
                    svf.VrsOut("TOTAL", df.format(_averageScore[j].getAverage()));
                    break;
                }
            }
            for (int j=0; j<_averageScore.length; j++) {
                if (_averageScore[j].getShowOrder() == 99) {
                    svf.VrsOut("AVERAGE", df.format(_averageScore[j].getAverage()));
                    break;
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
            stb.append("     T2.NAME1 AS TESTSUBCLASSNAME, ");
            stb.append("     T2.NAMESPARE1 AS IS_SELECT_SUBCLASS, ");
            stb.append("     SHOWORDER, ");
            stb.append("     TESTSUBCLASSCD ");
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
        }catch( Exception e ){
            log.error("preTotalScore error!"+e);
        }
        return stb.toString();
    }
    
    /** テスト科目の点数全体の和と人数を取得 */
    private String preTotalScore(String testDiv, String shDiv, String selSubDiv, String printPassSelect)
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
            stb.append("     LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'L013' AND W4.NAMECD2 = W1.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
            if (!"9".equals(shDiv)) {
                stb.append("     W3.SHDIV= '"+shDiv+"' AND ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
            }
            if ("1".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8','9') AND ");
            } else if ("2".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8') AND ");
            } else if ("3".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('9') AND ");
            }
            stb.append("         W4.NAMESPARE1 = '1' ");//合格扱い
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                stb.append(" union all ");
                stb.append(" select ");
                stb.append("     21 AS SHOWORDER, ");
                stb.append("     W1.TOTAL2 AS SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT W1 ");
                stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
                stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
                stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
                stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
                stb.append("               W3.EXAMNO = W1.EXAMNO ");
                stb.append("     LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'L013' AND W4.NAMECD2 = W1.JUDGEDIV ");
                stb.append(" WHERE ");
                stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
                stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
                stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
                if (!"9".equals(shDiv)) {
                    stb.append("     W3.SHDIV= '"+shDiv+"' AND ");
                }
                if (!"9".equals(selSubDiv)) {
                    stb.append("     W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
                }
                if ("1".equals(printPassSelect)) {
                    stb.append("         W1.JUDGEDIV in ('8','9') AND ");
                } else if ("2".equals(printPassSelect)) {
                    stb.append("         W1.JUDGEDIV in ('8') AND ");
                } else if ("3".equals(printPassSelect)) {
                    stb.append("         W1.JUDGEDIV in ('9') AND ");
                }
                stb.append("         W4.NAMESPARE1 = '1' ");//合格扱い
                stb.append(" UNION ALL   ");
                stb.append(" SELECT ");
                stb.append("     22 AS SHOWORDER, ");
                stb.append("     W1.AVARAGE2 AS SCORE ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_RECEPT_DAT W1 ");
                stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
                stb.append("        W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
                stb.append("        W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
                stb.append("        W3.TESTDIV = W1.TESTDIV AND ");
                stb.append("        W3.EXAMNO = W1.EXAMNO ");
                stb.append("     LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'L013' AND W4.NAMECD2 = W1.JUDGEDIV ");
                stb.append(" WHERE ");
                stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
                stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
                stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
                if (!"9".equals(shDiv)) {
                    stb.append("     W3.SHDIV= '"+shDiv+"' AND ");
                }
                if (!"9".equals(selSubDiv)) {
                    stb.append("     W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
                }
                if ("1".equals(printPassSelect)) {
                    stb.append("         W1.JUDGEDIV in ('8','9') AND ");
                } else if ("2".equals(printPassSelect)) {
                    stb.append("         W1.JUDGEDIV in ('8') AND ");
                } else if ("3".equals(printPassSelect)) {
                    stb.append("         W1.JUDGEDIV in ('9') AND ");
                }
                stb.append("         W4.NAMESPARE1 = '1' ");//合格扱い
            }
            stb.append(" union all ");
            stb.append(" select ");
            stb.append("     9 AS SHOWORDER, ");
            stb.append("     W1.TOTAL4 AS SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("               W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("               W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("               W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("               W3.EXAMNO = W1.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'L013' AND W4.NAMECD2 = W1.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
            if (!"9".equals(shDiv)) {
                stb.append("     W3.SHDIV= '"+shDiv+"' AND ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
            }
            if ("1".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8','9') AND ");
            } else if ("2".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8') AND ");
            } else if ("3".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('9') AND ");
            }
            stb.append("         W4.NAMESPARE1 = '1' ");//合格扱い
            stb.append(" UNION ALL   ");
            stb.append(" SELECT ");
            stb.append("     99 AS SHOWORDER, ");
            stb.append("     W1.AVARAGE4 AS SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT W1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
            stb.append("        W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("        W3.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("        W3.TESTDIV = W1.TESTDIV AND ");
            stb.append("        W3.EXAMNO = W1.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'L013' AND W4.NAMECD2 = W1.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     W1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     W1.TESTDIV= '"+testDiv+"' AND ");
            if (!"9".equals(shDiv)) {
                stb.append("     W3.SHDIV= '"+shDiv+"' AND ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("     W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
            }
            if ("1".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8','9') AND ");
            } else if ("2".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8') AND ");
            } else if ("3".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('9') AND ");
            }
            stb.append("         W4.NAMESPARE1 = '1' ");//合格扱い
            stb.append("  ");
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("     SHOWORDER, ");
            stb.append("     sum(SCORE) AS TOTALSCORE, ");
            stb.append("     count(SCORE) AS COUNT ");
            stb.append(" from ");
            stb.append("     t_score2 ");
            stb.append(" group by SHOWORDER ");
        }catch( Exception e ){
            log.error("preTotalScore error!"+e);
        }
        return stb.toString();
    }
    

    /**合格者一覧を取得**/ 
    private String preStat1(String testDiv, String shDiv, String selSubDiv, String printPassSelect)
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
            stb.append(" select t1.receptno, t1.score, t2.* ");
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
            stb.append("     T1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     T1.SUB_ORDER, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T7.NAME1 AS RITU_NAME, ");
            stb.append("     T6.FINSCHOOL_DISTCD AS FINSCHOOLCD, ");
            stb.append("     T6.FINSCHOOL_NAME, ");
            stb.append("     T6.FINSCHOOL_ZIPCD, ");
            stb.append("     T11.PREF AS PREF_NAME, ");
            stb.append("     T10.TESTSUBCLASSNAME, ");
            stb.append("     T10.TESTSUBCLASSCD, ");
            stb.append("     T10.SHOWORDER, ");
            stb.append("     T10.SCORE, ");
            stb.append("     T10.IS_SELECT_SUBCLASS, ");
            stb.append("     T1.SELECT_SUBCLASS_DIV, ");
            stb.append("     (case when T1.TOTAL4 = -1 then null else T1.TOTAL4 end) AS TOTAL4, ");
            stb.append("     T1.AVERAGE4, ");
            stb.append("     T1.TOTAL_RANK4, ");
            stb.append("     (case when T1.TOTAL2 = -1 then null else T1.TOTAL2 end) AS TOTAL2, ");
            stb.append("     T1.AVERAGE2, ");
            stb.append("     T1.AVERAGE1, ");
            stb.append("     T1.TOTAL_RANK1, ");
            stb.append("     T1.JUDGEDIV, ");
            stb.append("     T1.JUDGEDIV_NAME, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T1.SHIFT_DESIRE_FLG, ");
            stb.append("     T1.SLIDE_FLG, ");
            stb.append("     T1.SPORTS_FLG, ");
            stb.append("     T1.GENERAL_FLG, ");
            stb.append("     NM1.NAME1 AS GENERAL_FLG_NAME, ");
            stb.append("     T1.DORMITORY_FLG, ");
            stb.append("     FINH.FINSCHOOL_NAME AS SHSCHOOL_NAME, ");
            stb.append("     T1.PRE_RECEPTNO, ");
            stb.append("     P1.PRISCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     (SELECT ");
            stb.append("         W1.ENTEXAMYEAR, ");
            stb.append("         W1.TESTDIV, ");
            stb.append("         W1.EXAMNO, ");
            stb.append("         W1.RECEPTNO, ");
            stb.append("         W3.RECOM_EXAMNO, ");
            stb.append("         W3.NAME, ");
            stb.append("         W3.NAME_KANA, ");
            stb.append("         W3.SEX, ");
            stb.append("         W3.SHDIV, ");
            stb.append("         W3.SUB_ORDER, ");
            stb.append("         W3.FS_NAME, ");
            stb.append("         W3.FS_CD, ");
            stb.append("         VALUE(W1.TOTAL4,-1) AS TOTAL4, ");
            stb.append("         W1.AVARAGE4 AS AVERAGE4, ");
            stb.append("         W1.TOTAL_RANK4, ");
            stb.append("         VALUE(W1.TOTAL2,-1) AS TOTAL2, ");
            stb.append("         W1.AVARAGE2 AS AVERAGE2, ");
            stb.append("         W1.AVARAGE1 AS AVERAGE1, ");
            stb.append("         W1.TOTAL_RANK1, ");
            stb.append("         W3.SELECT_SUBCLASS_DIV, ");
            stb.append("         W1.JUDGEDIV, ");
            stb.append("         W4.NAME1 AS JUDGEDIV_NAME, ");
            stb.append("         W3.REMARK1, ");
            stb.append("         W3.REMARK2, ");
            stb.append("         W3.SHIFT_DESIRE_FLG, ");
            stb.append("         W3.SLIDE_FLG, ");
            stb.append("         W3.SPORTS_FLG, ");
            stb.append("         W3.GENERAL_FLG, ");
            stb.append("         W3.DORMITORY_FLG, ");
            stb.append("         W3.SH_SCHOOLCD, ");
            stb.append("         W3.PRE_RECEPTNO, ");
            stb.append("         W3.PRISCHOOLCD ");
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
            stb.append("         W1.TESTDIV= '"+testDiv+"' AND ");
            if (!"9".equals(shDiv)) {
                stb.append("         W3.SHDIV= '"+shDiv+"' AND ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("         W3.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
            }
            if ("1".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8','9') AND ");
            } else if ("2".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('8') AND ");
            } else if ("3".equals(printPassSelect)) {
                stb.append("         W1.JUDGEDIV in ('9') AND ");
            }
            stb.append("         W4.NAMESPARE1 = '1' ");//合格扱い
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
            stb.append("     LEFT JOIN NAME_MST T13 ON ");
            stb.append("                 T13.NAMECD1 = 'L004' AND ");
            stb.append("                 T13.NAMECD2 = T12.TESTDIV ");
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
                    stb.append("     T1.AVERAGE1 desc, T1.EXAMNO, T10.SHOWORDER ");
                } else {
                    stb.append("     T1.TOTAL4 desc, T1.EXAMNO, T10.SHOWORDER ");
                }
            }
            if ("2".equals(_param._sortType)) stb.append("     T1.EXAMNO, T10.SHOWORDER ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String testDiv, String shDiv, String selSubDiv, String printPassSelect)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_RECEPT_DAT W1 ");
            stb.append("            LEFT JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON ");
            stb.append("                W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("                W2.EXAMNO = W1.EXAMNO AND ");
            stb.append("                W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("                W2.TESTDIV = W1.TESTDIV ");
            stb.append("            LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'L013' AND W4.NAMECD2 = W1.JUDGEDIV ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+_param._year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+_param._applicantDiv+"' ");
            stb.append("            AND W1.TESTDIV= '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("            AND W2.SHDIV= '"+shDiv+"' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("            AND W2.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
            }
            if ("1".equals(printPassSelect)) {
                stb.append("            AND W1.JUDGEDIV in ('8','9') ");
            } else if ("2".equals(printPassSelect)) {
                stb.append("            AND W1.JUDGEDIV in ('8') ");
            } else if ("3".equals(printPassSelect)) {
                stb.append("            AND W1.JUDGEDIV in ('9') ");
            }
            stb.append("            AND W4.NAMESPARE1 = '1' ");//合格扱い
            stb.append("     GROUP BY W1.TESTDIV ) T1  ");
        } catch( Exception e ){
            log.error("preStatTotalPage error!");
        }
        return stb.toString();

    }//preStat2()の括り



    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            //ps.close();
            ps1.close();
            ps2.close();
        } catch( Exception e ){
            log.error("preStatClose error! ="+e);
        }
    }//preStatClose()の括り



    /**ブランクをセット**/
    private void setSvfInt(
        Vrw32alp svf
    ) {
        try {
            svf.VrsOut("NOTE"   ,"note");
        } catch( Exception ex ) {
            log.error("setSvfInt set error!");
        }

    }
    
    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String formatSakuseiDate(String cnvDate) {

        String retDate = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); 
            //文字列よりDate型へ変換
            Date date1 = format.parse(cnvDate); 
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        return retDate;
    }

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
    
    class TestRecord {
        final String _number;
        final String _examno;
        final String _name;
        final String _sexname;
        final String _pref;
        final String _fschool;
        final String _total4;
        final String _average4;
        final String _totalRank4;
        final String _total2;
        final String _average2;
        final String _average1;
        final String _totalRank1;
        final String _judgediv;
        final String _judgeName;
        final String _remark;
        final String _recomExamno;
        final String _otherTestDivName;
        final String _shiftDesireFlg;
        final String _shdiv;
        final String _subOrder;
        final String _slideFlg;
        final String _sportsFlg;
        final String _generalFlgName;
        final String _dormitoryFlg;
        final String _shschoolName;
        final String _preReceptno;
        final String _prischoolName;
        List _testSubclass = new ArrayList();
        public TestRecord(String number,String examno,String name,String sexname,
                String pref,String fschool, String total4, String average4, String rank4,
                String total2, String average2, String average1, String rank1,
                String judgediv, String judgeName, String remark,
                String recomExamno, String otherTestDivName,
                String shiftDesireFlg,
                String shdiv,
                String subOrder,
                String slideFlg,
                String sportsFlg,
                String generalFlgName,
                String dormitoryFlg,
                String shschoolName,
                String preReceptno,
                String prischoolName
                ){
            _number = number;
            _examno = examno;
            _name = name;
            _sexname = sexname;
            _pref = pref;
            _fschool = fschool;
            _total4 = total4;
            _average4 = average4;
            _totalRank4 = rank4;
            _total2 = total2;
            _average2 = average2;
            _average1 = average1;
            _totalRank1 = rank1;
            _judgediv = judgediv;
            _judgeName = judgeName;
            _remark = remark;
            _recomExamno = recomExamno;
            _otherTestDivName = otherTestDivName;
            _shiftDesireFlg = shiftDesireFlg;
            _shdiv = shdiv;
            _subOrder = subOrder;
            _slideFlg = slideFlg;
            _sportsFlg = sportsFlg;
            _generalFlgName = generalFlgName;
            _dormitoryFlg = dormitoryFlg;
            _shschoolName = shschoolName;
            _preReceptno = preReceptno;
            _prischoolName = prischoolName;
        }
        
        public void addTestSubclass(int showOrder, int testScore,String isSelectSubclass,
                String testSubclassDiv, String testSubclassCd) {
            boolean isHyphen = false;
            if (null == isSelectSubclass || !"1".equals(isSelectSubclass)) {
                isHyphen = false; // この科目が選択科目(isSelectSubclass=='1')ではないとき、'-'ではない
            } else if (testSubclassDiv != null && !testSubclassDiv.equals(testSubclassCd)) {
                isHyphen = true;  // この科目が選択科目かつこの学生が選択していないとき、'-'を表示する
            } else {
                isHyphen = false; // 上記以外(選択科目でこの学生が選択している)のとき、'-'ではない
            }
            
            _testSubclass.add(new TestScore(showOrder, testScore, isHyphen));
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
        public TestSubclassName(int showOrder, boolean isSelectSubclass, String testSubclassName) {
            _showOrder = showOrder;
            _isSelectSubclass = isSelectSubclass;
            _testSubclassName = testSubclassName;
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
        public TestScore(int showOrder, int testScore, boolean isHyphen) {
            _showOrder = showOrder;
            _testScore = testScore;
            _isHyphen = isHyphen;
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
        final double _totalScore;
        final int _count;
        public AverageScore(int showOrder, double totalScore, int count) {
            _showOrder = showOrder;
            _totalScore = totalScore;
            _count = count;
        }

        public double getAverage() {
            return _totalScore / (double) _count;
        }
        
        public int getShowOrder() {
            return _showOrder;
        }

        public int getCount() {
            return _count;
        }
    }

class Param {
        
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _loginDate;
        final String _sortType;
        final String _shDiv;
        private final String[] _printPassArray;

        final boolean _seirekiFlg;
        final String _z010SchoolCode;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _testDiv = request.getParameter("TESTDIV");                      //入試区分
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _sortType = request.getParameter("SORT");                        // 印刷順序
            _shDiv = request.getParameter("SHDIV");

            //和歌山中学の場合、指示画面からパラメータがくる
            String printPass1 = request.getParameter("PRINT_PASS1"); //S合格/G合格
            String printPass2 = request.getParameter("PRINT_PASS2"); //S合格
            String printPass3 = request.getParameter("PRINT_PASS3"); //G合格
            if ("1".equals(printPass1) || "1".equals(printPass2) || "1".equals(printPass3)) {
                _printPassArray = new String[] {
                        "1".equals(printPass1) ? "1" : null,
                        "1".equals(printPass2) ? "2" : null,
                        "1".equals(printPass3) ? "3" : null };
            } else {
                _printPassArray = new String[]{"0"};
            }

            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);
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
            } catch(SQLException ex) {
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
            } catch(SQLException ex) {
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
