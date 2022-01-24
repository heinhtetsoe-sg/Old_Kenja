package servletpack.KNJL;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１５Ｃ＞  得点分布表
 *
 *  2008/11/18 RTS 作成日
 **/

public class KNJL315C {

    private static final Log log = LogFactory.getLog(KNJL315C.class);
    private TestSubclassName[] _testSubclassName;
    private Param _param;
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
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        log.fatal("$Revision: 65206 $ $Date: 2019-01-22 17:21:04 +0900 (火, 22 1 2019) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, request);

        String[] testDivPrint = {""};
        if ("9".equals(_param._testDiv)) {
            if ("1".equals(_param._applicantDiv)) testDivPrint = new String[]{"1","2"};
            if ("2".equals(_param._applicantDiv)) {
                if (_param.isGojo()) {
                    testDivPrint = new String[]{"3","4","5","7"};
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
        PreparedStatement ps2 = null;
        boolean nonedata = false;                               //該当データなしフラグ


        //SQL作成
        try {
            for(int i=0; i<testDivPrint.length; i++) {
                String testDiv = testDivPrint[i];

                String[] sub2ka3kaDivPrint = null;
                if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                    sub2ka3kaDivPrint = new String[]{SUBCLASS_2KA,SUBCLASS_3KA};
                } else {
                    sub2ka3kaDivPrint = new String[]{""};
                }

                for(int j=0; j<sub2ka3kaDivPrint.length; j++) {
                    String sub2ka3kaDiv = sub2ka3kaDivPrint[j];

                    String sql = preStat1(testDiv, sub2ka3kaDiv);
                    log.debug("preStat1 sql="+sql);
                    ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                    sql = preStat2(testDiv, sub2ka3kaDiv);
                    log.debug("preStat2 sql="+sql);
                    ps2 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                    //SVF出力
                    if (setSvfMain(db2, svf, ps1, ps2, testDiv, sub2ka3kaDiv)) nonedata = true;  //帳票出力のメソッド
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
        preStatClose(psTestdiv,ps1,ps2);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り

    private String getTestSubclassName(int testSubclassCd) {
        for(int i=0; i<_testSubclassName.length; i++) {
            if(_testSubclassName[i]._testSubclassCd == testSubclassCd)
                return _testSubclassName[i]._testSubclassName;
        }
        return null;
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
            log.error(ex);
        }

        return name;
    }

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv,
        String sub2ka3kaDiv
    ) {
        final String form;
        _param.formn = 4;
        if (_param.isCollege()) {
            form = "2".equals(_param._applicantDiv) || "1".equals(_param._applicantDiv) && "7".equals(testDiv) ? "KNJL315C_2C.frm" : "KNJL315C_1C.frm";
        } else if (_param.isGojo()) {
            form = "2".equals(_param._applicantDiv) ? "KNJL315C_2G.frm" : "KNJL315C_1G.frm";
        } else {
            form = "KNJL315C.frm";  //当初は中高別フォームだったが、フォーム統一により、高校用のKNJL315C_2.frmは未使用となった。
            _param.formn = 1;
        }
        svf.VrSetForm(form, _param.formn);
        svf.VrsOut("NENDO", _param.getNendo(db2));
        svf.VrsOut("APPLICANTDIV", getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        svf.VrsOut("TESTDIV",      getNameMst(db2, "L004", testDiv));// 画面から入試区分
        if ((!_param.isCollege() && _param.isGojo() && "2".equals(_param._applicantDiv)) ||
            (_param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(_param._testDiv))) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            svf.VrsOut("SHDIV",      "("+getNameMst(db2, "L006", _param._shDiv)+")");// 画面から専併区分
        }
        //カレッジ中学A日程
        if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
            svf.VrsOut( "SUBTITLE", "（２科型）");
        } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
            svf.VrsOut( "SUBTITLE", "（３科型）");
        }
        svf.VrsOut( "DATE", _param.getDateString(db2));

        setTestSubclassName(db2, testDiv, sub2ka3kaDiv);
        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
    }

    private void setTestSubclassName(DB2UDB db2, String testDiv, String sub2ka3kaDiv) {
        String sql = preStatSubclassName(testDiv, sub2ka3kaDiv);
        log.debug("setTestSubclassName sql="+sql);
        List testSubclassNameList = new ArrayList();
        try{
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                String showOrder = rs.getString("SHOWORDER");
                String testSubclassName = rs.getString("TESTSUBCLASSNAME");
                log.debug(showOrder+","+testSubclassName);
                testSubclassNameList.add(new TestSubclassName(
                        Integer.valueOf(testSubclassCd).intValue(),
                        Integer.valueOf(showOrder).intValue(),
                        testSubclassName));
            }
            _testSubclassName = new TestSubclassName[testSubclassNameList.size()];
            for(int i=0; i<testSubclassNameList.size(); i++) {
                _testSubclassName[i] = (TestSubclassName) testSubclassNameList.get(i);
            }
        } catch (Exception ex) {
            log.debug("setTestSubclassName exception=", ex);
        }
    }

    /** 表示する科目名を取得 */
    private String preStatSubclassName(String testDiv, String sub2ka3kaDiv)
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     T2.NAME1 AS TESTSUBCLASSNAME, ");
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
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     and TESTSUBCLASSCD in ('" + KOKUGO + "','" + SANSUU + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     and TESTSUBCLASSCD in ('" + KOKUGO + "','" + SANSUU + "','" + RIKA + "') ");
            }
            stb.append(" ORDER BY ");
            stb.append("     SHOWORDER ");
        }catch (Exception e) {
            log.error("preTotalScore error!", e);
        }
        return stb.toString();
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
            PreparedStatement ps2,
            String testDiv,
            String sub2ka3kaDiv
        ) {
            boolean nonedata = false;
            List subclassList = new ArrayList();
            List distributionList = new ArrayList();
            subclassList.add(distributionList);
            try {
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
                    TestScore ts = new TestScore(testSubclassCd, perfect, distIndex, distCount);
                    distributionList.add(ts);
                    oldTestSubclassCd = testSubclassCd;
                }
                rs.close();
                db2.commit();

                // 合計点の分布データ
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
                    TestScore ts = new TestScore(testSubclassCd, perfect, distIndex, distCount);
                    distributionList.add(ts);
                }


                if (setSvfout2(db2, svf, subclassList, testDiv, sub2ka3kaDiv)) nonedata = true; //帳票出力のメソッド
            } catch (Exception ex) {
                log.error("setSvfMain set error!", ex);
            }
            return nonedata;
        }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout2(
        DB2UDB db2,
        Vrw32alp svf,
        List subclassList,
        String testDiv,
        String sub2ka3kaDiv
    ) {
        boolean nonedata = false;
        if (null == subclassList) return nonedata;
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
                if (nonedata == false) {
                    setHeader(db2, svf, testDiv, sub2ka3kaDiv);
                    nonedata = true;
                }

                TestScore firstElement = (TestScore) distributionList.get(0);
                subclassName[si] = getTestSubclassName(firstElement._testSubclassCd);
                int kizami = (firstElement._perfect==_param._totalPerfect) ? 10 : 5;
                distribution[si] = new int[firstElement._perfect/kizami + 1];
                if (firstElement._perfect==150 || (_param.isWakayama()  && firstElement._perfect==100)) {
                    subclassDiv[si] = subclassDiv150;
                    subclassDiv150++;
                } else if (firstElement._perfect==100) {
                    subclassDiv[si] = subclassDiv100;
                    subclassDiv100++;
                } else{
                    subclassDiv[si] = 999;
                }

                for(Iterator distIterator = distributionList.iterator(); distIterator.hasNext();) {
                    TestScore ts = (TestScore) distIterator.next();
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
            // 出力 (総合と150点満点科目 or 和歌山中学100点満点科目)
            for (int i = 0; i < 51; i++) {
                //log.debug("lineIndex=" + i);
                for (int si = 0; si < subclassSize; si++) {
                    //log.debug("sN si="+si);
                    if (distribution[si] == null || distribution[si].length <= i) {
                        continue;
                    }
                    final int subDiv = subclassDiv[si];
                    if (subDiv > 10 && 999 != subDiv) {
                        continue; // 100点満点の科目はここでは出力しない
                    }
                    cumulative[si] += distribution[si][i];
                    final String average = cumulative[si] == 0 ? "" : new BigDecimal(totalScore[si]).divide(new BigDecimal(cumulative[si]), 1, BigDecimal.ROUND_HALF_UP).toString();

                    if(subDiv == 999) {
                        if (0 != distribution[si][i]) {
                            svf.VrsOutn("COUNT1", i+1, String.valueOf(distribution[si][i]));
                        }
                        if (0 != cumulative[si]) {
                            svf.VrsOutn("CUMULATIVE1", i+1, String.valueOf(cumulative[si]));
                        }
                        svf.VrsOut("TOTAL_COUNT1", String.valueOf(cumulative[si]));
                        svf.VrsOut("AVERAGE1", average);
                    } else if(subDiv <= 10) {
                        svf.VrsOut("SUBCLASS2_"+subDiv, subclassName[si]);
                        if (0 != distribution[si][i]) {
                            svf.VrsOutn("COUNT2_" + subDiv, i+1, String.valueOf(distribution[si][i]));
                        }
                        if (0 != cumulative[si]) {
                            svf.VrsOutn("CUMULATIVE2_" + subDiv, i+1, String.valueOf(cumulative[si]));
                        }
                        svf.VrsOut("TOTAL_COUNT2_" + subDiv, String.valueOf(cumulative[si]));
                        svf.VrsOut("AVERAGE2_" + subDiv, average);
                    }
                }
            }

            boolean output100 = false; // 100点満点科目が1科目でもあるか

            // 出力 (総合と100点満点科目)
            // 100点満点の科目はフォームの繰り返しレコードの関係で別に出力する。
            for (int si = 0; si < subclassSize; si++) {
                int subDiv = subclassDiv[si];
                if (subDiv <= 10 || subDiv == 999) {
                    continue; // 100点満点以外の科目は処理をしない
                }
                if (distribution[si] == null) {
                    continue;
                }
                output100 = true;
                cumulative[si] = 0;
                for(int i = 0; i < 21; i++) {
                    if (distribution[si].length <= i) {
                        continue;
                    }
                    cumulative[si] += distribution[si][i];
                    final String average = cumulative[si] == 0 ? "" : new BigDecimal(totalScore[si]).divide(new BigDecimal(cumulative[si]), 1, BigDecimal.ROUND_HALF_UP).toString();

                    svf.VrsOut("SUBCLASS3", subclassName[si]);
                    if (0 != distribution[si][i]) {
                        svf.VrsOutn("COUNT3", i + 1, String.valueOf(distribution[si][i]));
                    }
                    if (0 != cumulative[si]) {
                        svf.VrsOutn("CUMULATIVE3", i + 1, String.valueOf(cumulative[si]));
                    }
                    svf.VrsOut("TOTAL_COUNT3", String.valueOf(cumulative[si]));
                    svf.VrsOut("AVERAGE3", average);
                }
                svf.VrEndRecord();
            }

            if (output100 == false && _param.formn == 4) {
                for(int i=0; i<21; i++){
                    svf.VrsOut("SUBCLASS3", "");
                    svf.VrsOutn("COUNT3", i + 1, "");
                    svf.VrsOutn("CUMULATIVE3", i + 1, "");
                    svf.VrsOut("TOTAL_COUNT3", "");
                    svf.VrsOut("AVERAGE3", "\n"); // " "出力ではpdf出力エラー
                }
                svf.VrEndRecord();
            } else if (_param.formn == 1) {
                svf.VrEndPage();
            }

            log.debug("nondata=" + nonedata);
            //最終レコードを出力
            if (nonedata) {
                setSvfInt(svf);         //ブランクセット
            }

        } catch (Exception ex) {
            log.error("setSvfout set error! =", ex);
        }
        return nonedata;
    }

    /**得点分布を取得**/
    private String preStat1(String testDiv, String sub2ka3kaDiv)
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
            stb.append("     t1.TESTDIV = '"+_param._testDiv+"' ");
            if (!_param.isGojo() || !_param.isCollege() && !"1".equals(_param._applicantDiv)) {
                stb.append("     and l2.SHDIV = '" + _param._shDiv + "' ");
            }
            if (_param.isWakayama()) {  //和歌山校のみ
            	stb.append("     and t2.PERFECT IS NOT NULL ");  //満点マスタ登録も必須
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
            log.error("preStat1 error!", e);
        }
        return stb.toString();

    }//preStat1()の括り


    /** 得点分布 (総合)を取得 **/
    private String preStat2(String testDiv, String sub2ka3kaDiv)
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
            stb.append("         w1.TESTDIV = '"+_param._testDiv+"' ");
            if (!_param.isGojo() || !_param.isCollege() && !"1".equals(_param._applicantDiv)) {
                stb.append("         and l1.SHDIV = '" + _param._shDiv + "' ");
            }
            if (SUBCLASS_2KA.equals(sub2ka3kaDiv)) {
                stb.append("     and l1.SUB_ORDER in ('" + SUB_ORDER1 + "','" + SUB_ORDER2 + "') ");
            } else if (SUBCLASS_3KA.equals(sub2ka3kaDiv)) {
                stb.append("     and l1.SUB_ORDER in ('" + SUB_ORDER1 + "') ");
            }
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("     (case when MOD("+_param._totalPerfect+" - t1.TOTAL, 10) = 0 then 0 ");
            stb.append("      else 1 end) + ("+_param._totalPerfect+" - t1.TOTAL)/10 AS DISTINDEX, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" from ");
            stb.append("     T_TOTALSCORE t1 ");
            stb.append(" where ");
            stb.append("     t1.TOTAL <= "+_param._totalPerfect+" ");
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
            log.error("preStat2 error!", e);
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
            if(ps!=null) ps.close();
            if(ps1!=null) ps1.close();
            if(ps2!=null) ps2.close();
        } catch (Exception e) {
            log.error("preStatClose error! =", e);
        }
    }//preStatClose()の括り

    /**ブランクをセット**/
    private void setSvfInt(
        Vrw32alp svf
    ) {
        try {
            svf.VrsOut("NOTE"   ,"note");
        } catch (Exception ex) {
            log.error("setSvfInt set error!", ex);
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
        } catch (Exception e) {
            log.error("setHeader set error!", e);
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

    class TestSubclassName implements Comparable{
        final int _testSubclassCd;
        final int _showOrder;
        final String _testSubclassName;
        public TestSubclassName(int testSubclassCd, int showOrder, String testSubclassName) {
            _testSubclassCd = testSubclassCd;
            _showOrder = showOrder;
            _testSubclassName = testSubclassName;
        }
        public int compareTo(Object o) {
            if (!(o instanceof TestSubclassName))
                return -1;
            TestSubclassName other = (TestSubclassName) o;
            return new Integer(_showOrder).compareTo(new Integer(other._showOrder));
        }
    }

    class TestScore {
        final int _testSubclassCd;
        final int _perfect;
        final int _distIndex; // 点数の領域のインデックス 500点満点だと10点きざみで 500点->0, 499点->1, 490点->1, 489点->2, 0点->50
        final int _count;     // その領域の点数の人数
        public TestScore(int testSubclassCd, int perfect, int distindex, int count){
            _testSubclassCd = testSubclassCd;
            _perfect = perfect;
            _distIndex = distindex;
            _count = count;
        }
    }
    class Param {
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _shDiv;
        final String _loginDate;
        final boolean _seirekiFlg;
        final String _z010SchoolCode;
        final int _totalPerfect; //合計の分布の満点
        int formn;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _shDiv = request.getParameter("SHDIV");
            _loginDate = request.getParameter("LOGIN_DATE");

            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);
            //和歌山中学判定フラグ
            _totalPerfect = isGojo() ? 400 : isWakayama() ? 300 : 500;  //和歌山校
        }

        String getNendo(final DB2UDB db2) {
            return _seirekiFlg ? _year+"年度":
                KNJ_EditDate.h_format_JP_N(db2, _year+"-01-01")+"度";
        }

        String getDateString(final DB2UDB db2) {
            if (null == _loginDate) return null;
            return _seirekiFlg ?
                    _loginDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate):
                        KNJ_EditDate.h_format_JP(db2,  _loginDate ) ;
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
