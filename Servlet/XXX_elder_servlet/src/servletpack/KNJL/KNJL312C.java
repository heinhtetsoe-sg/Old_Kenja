
package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試処理]  得点データチェックリスト
 *
 *                  ＜ＫＮＪＬ３１２Ｃ＞  得点データチェックリスト
 *
 *  2008/11/18 takara 作成日
 *
 */

public class KNJL312C {
    private static final Log log = LogFactory.getLog(KNJL312C.class);
    private DecimalFormat dft    = new DecimalFormat("#");

    private Map map_param        = new HashMap();
    private Map map_sql          = new HashMap();
    private Map map_output       = new HashMap();
    private boolean nonedata     = true;
    private int gyousu           = 40; //1ページ40行
    private String manCount = null, womanCount = null, allCount = null;
    private String sum = null, avg = null;
    private String _z010SchoolCode;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        getParam(request);              // HTTPからのパラメータの取得、

        // print svf設定
        setSvfInit(response, svf);
        svf.VrSetForm("KNJL312C_2.frm", 4);

        // DB接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }

        _z010SchoolCode = getSchoolCode(db2);

        // 印刷処理（実際にPDFを作る処理）
        nonedata = printSvf(db2, svf);

        log.info("nonedata = "+nonedata);
        // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }

    /*■■■■■   パラメータの取得   ■■■■■*/
    private void getParam(HttpServletRequest request) {
        try {
            map_param.put("YEAR"              ,request.getParameter("YEAR"));
            map_param.put("APPLICANTDIV"      ,request.getParameter("APPLICANTDIV"));
            map_param.put("TESTDIV"           ,request.getParameter("TESTDIV"));
            map_param.put("EXAM_TYPE"         ,request.getParameter("EXAM_TYPE"));
            map_param.put("TESTSUBCLASSCD"    ,request.getParameter("TESTSUBCLASSCD"));
            map_param.put("CATEGORY_SELECTED" ,request.getParameterValues("CATEGORY_SELECTED"));
            map_param.put("DATE"              ,request.getParameter("LOGIN_DATE"));
        } catch( Exception ex ) {
            log.error("get parameter error!" + ex);
        }

        /*********************デバッグ用************************************/
        String[] aaa = (String[]) map_param.get("CATEGORY_SELECTED");
        for (int i = 0; i < aaa.length; i++) {
            log.debug("CATEGORY_SELECTED[" + i + "] = " + aaa[i]);
        }
        Iterator it = map_param.keySet().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            log.debug("PARAM  >>>>>  " + o + " = " + map_param.get(o));
        }
        /*********************デバッグ用************************************/
    }


    /*■■■■■   実際にPDFを作る処理   ■■■■■*/
    private boolean printSvf(DB2UDB db2, Vrw32alp svf) {
        String[] category_selected = (String[]) map_param.get("CATEGORY_SELECTED");
        List scoreLists = new ArrayList();
        List examHallNameList = new ArrayList();

        try {
            for (int i = 0; i < category_selected.length; i++) {
                log.debug("CATEGORY_SELECTED[" + i + "] = " + category_selected[i]);
                PreparedStatement ps = db2.prepareStatement(setStatement1(category_selected[i]));
                ResultSet rs1   = ps.executeQuery();
                if(rs1.next()) {
                    examHallNameList.add(rs1.getString("EXAMHALL_NAME"));
                }
                rs1.close();
                ps.close();
                db2.commit();
                List scoreList = getScoreList(db2, category_selected[i]);
                if (scoreList.size()!=0)
                    scoreLists.add(scoreList);
            }

            /*************** ヘッダ **************************/
            printHead(db2, svf, category_selected);


            /*************** リスト **************************/
            nonedata = printMain(db2, svf, category_selected, scoreLists, examHallNameList);

        } catch(Exception ex) {
            log.error("printSvf error!",ex);
        }
        return nonedata;
    }



    private List getScoreList(DB2UDB db2, String category_selected) {
        List scoreList = new ArrayList();
        try {
            PreparedStatement ps = db2.prepareStatement(setStatement7(category_selected)); //リストを作るのに必要な項目が帰ってくる
            ResultSet rs = ps.executeQuery(); //リストに必要な項目くが帰ってくる。
            while(rs.next()) {
                Map score = new HashMap();
                score.put("RECEPTNO", rs.getString("RECEPTNO"));
                score.put("EXAMNO",   rs.getString("EXAMNO"));
                score.put("POINT",    rs.getString("SCORE"));
                scoreList.add(score);
            }
        } catch (SQLException e) {
            log.error("getScoreList error!", e);
        }
        return scoreList;
    }


    private boolean printMain(DB2UDB db2, Vrw32alp svf, String[] category_selected, List scoreListsSrc, List examHallNameList) {
        int page_count = 1;
        boolean nonedata = false;
        Integer totalPage = (Integer) map_output.get("TOTAL_PAGE");

        final List scoreLists = new ArrayList();
        for (int i = 0; i < scoreListsSrc.size(); i++) {
        	List l = (List) scoreListsSrc.get(i);
        	if (null == l) {
        		continue;
        	}
        	final int lines = l.size() / gyousu + (l.size() % gyousu == 0 ? 0 : 1);
    		for (int j = 0; j < lines; j++) {
    			scoreLists.add(l.subList(j * gyousu, Math.min((j + 1) * gyousu, l.size())));
    		}
        }

        while(scoreLists.size()!=0) {
            svf.VrsOut("PAGE" ,String.valueOf(page_count));
            svf.VrsOut("TOTAL_PAGE" , totalPage.toString());
            log.debug("PAGE="+String.valueOf(page_count));
            String[] examHallName = new String[3];
            List[] lists = new List[3];

            lists[0] = (List) scoreLists.remove(0);
            examHallName[0] = examHallNameList.size() == 0 ? "" : (String) examHallNameList.remove(0);

            if (lists[0] != null && lists[0].size() > gyousu) {
                lists[1] = lists[0].subList(gyousu, lists[0].size());
            } else {
                if (scoreLists.size()!=0) {
                    lists[1] = (List) scoreLists.remove(0);
                    examHallName[1] = examHallNameList.size() == 0 ? "" : (String) examHallNameList.remove(0);
                }
            }

            if (lists[1] != null && lists[1].size() > gyousu) {
                lists[2] = lists[1].subList(gyousu, lists[1].size());
            } else {
                if (scoreLists.size()!=0) {
                    lists[2] =  (List) scoreLists.remove(0);
                    examHallName[2] = examHallNameList.size() == 0 ? "" : (String) examHallNameList.remove(0);
                }
            }

            for(int line=0; line<3; line++) {
                svf.VrsOut("EXAM_PLACE"+String.valueOf(line+1), examHallName[line]);
            }

            for(int count=0; count<gyousu; count++) {
                for(int line=0; line<3; line++) {
                    if (null == lists[line] || lists[line].size() <= count) continue;
                    Map score = (Map) lists[line].get(count);
                    if (null == score) continue;

                    svf.VrsOut("RECEPTNO"+String.valueOf(line+1), (String) score.get("RECEPTNO"));
                    svf.VrsOut("EXAMNO"  +String.valueOf(line+1), (String) score.get("EXAMNO"));
                    svf.VrsOut("POINT"   +String.valueOf(line+1), (String) score.get("POINT"));
                    nonedata = true;
                }
                svf.VrEndRecord();
                if (page_count == totalPage.intValue()) {
                    printFoot(db2, svf, category_selected);
                }
            }
            page_count+=1;
        }
        return nonedata;
    }

    private void svfVrsOutWithFormat(Vrw32alp svf, String fieldname, int area_len, String data) {
        svf.VrsOut(setformatArea(fieldname, area_len, data), data);
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

    private void printHead(DB2UDB db2, Vrw32alp svf, String[] category_selected) {
        try {
            map_sql.put("HALL_NAME_TYPE"    , db2.prepareStatement(setStatement1(category_selected[0]))); //EXAMHALL_NAME EXAMNAMEが帰ってくる
            map_sql.put("SCHOOLNAME"        , db2.prepareStatement(setStatement2())); //SCHOOL_NAMEがってくる
            map_sql.put("TESTSUBCLASSCD"    , db2.prepareStatement(setStatement3())); //TESTSUBCLASCD_NAMEが帰ってくる
            map_sql.put("APP_TEST_NAME"     , db2.prepareStatement(setStatement4())); //TESTDIV_NAME APPLICANTDIV_NAMEが帰ってくる
            map_sql.put("TOTAL_PAGE"        , db2.prepareStatement(setStatement9(category_selected))); //TOTAL_SCHが帰ってくる。

            /******************************** 受験型・会場名 ***************************************/
            ResultSet rs1   = ((PreparedStatement) map_sql.get("HALL_NAME_TYPE" )).executeQuery(); //EXAMHALL_NAME EXAMNAMEが帰ってくる
            if(rs1.next()) {
                map_output.put("EXAM_PLACE"   ,rs1.getString("EXAMHALL_NAME"));
                map_output.put("EXAM_TYPE"    ,rs1.getString("EXAMNAME"));
                log.debug("EXAM_PLACE   = " + map_output.get("EXAM_PLACE"));
                log.debug("EXAM_TYPE    = " + map_output.get("EXAM_TYPE"));
            }
            rs1.close();
            /******************************** 学校名 ***************************************/
            ResultSet rs2   = ((PreparedStatement) map_sql.get("SCHOOLNAME"     )).executeQuery(); //SCHOOL_NAMEがってくる
            if(rs2.next()) {
                map_output.put("SCHOOLNAME"   ,rs2.getString("SCHOOL_NAME"));
                log.debug("SCHOOLNAME   = " + map_output.get("SCHOOLNAME"));
            }
            rs2.close();
            /******************************** 科目名 ***************************************/
            ResultSet rs3   = ((PreparedStatement) map_sql.get("TESTSUBCLASSCD" )).executeQuery(); //TESTSUBCLASCD_NAMEが帰ってくる
            if(rs3.next()) {
                map_output.put("SUBCLASS"     ,rs3.getString("TESTSUBCLASCD_NAME"));
                log.debug("SUBCLASS     = " + map_output.get("SUBCLASS"));
            }
            rs3.close();
            /******************************** 入試制度・入試区分 ***************************************/
            ResultSet rs4   = ((PreparedStatement) map_sql.get("APP_TEST_NAME"  )).executeQuery(); //TESTDIV_NAME APPLICANTDIV_NAMEが帰ってくる
            if(rs4.next()) {
                map_output.put("TESTDIV"      ,rs4.getString("TESTDIV_NAME"));
                map_output.put("APPLICANTDIV" ,rs4.getString("APPLICANTDIV_NAME"));
                log.debug("TESTDIV      = " + map_output.get("TESTDIV"));
                log.debug("APPLICANTDIV = " + map_output.get("APPLICANTDIV"));
            }
            rs4.close();

            // 西暦か和暦はフラグで判断
            boolean _seirekiFlg = getSeirekiFlg(db2);
            String date;
            if (null != map_param.get("YEAR")) {
                if (_seirekiFlg) {
                    //2008年度の形
                    date = map_param.get("YEAR").toString().substring(0, 4) + "年度";
                } else {
                    //平成14年度の形
                    date = KNJ_EditDate.gengou(db2, Integer.parseInt(map_param.get("YEAR").toString())) + "年度";
                }
                map_output.put("NENDO", date);
                log.debug("NENDO = " + date);
            }
            if (null != map_param.get("DATE")) {
                if (_seirekiFlg) {
                    //2008年3月3日の形
                    date = map_param.get("DATE").toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(map_param.get("DATE").toString());
                } else {
                    //平成14年10月27日の形
                    date = KNJ_EditDate.h_format_JP(db2, map_param.get("DATE").toString());
                }
                map_output.put("DATE", date);
            }

            // 各リスト項目の人数
            ResultSet rs9 = ((PreparedStatement) map_sql.get("TOTAL_PAGE"    )).executeQuery(); //TOTAL_SCHが帰ってくる
            if (rs9.next()) {
                String totalPage = rs9.getString("TOTAL_PAGE");
                if (totalPage==null) {
                    totalPage = "0";
                }
                map_output.put("TOTAL_PAGE", Integer.valueOf(totalPage));
                log.debug("TOTAL_PAGE   = " + map_output.get("TOTAL_PAGE"));
            } else {
                map_output.put("TOTAL_PAGE", "0");
            }
            rs9.close();
            db2.commit();

            /*********** svfに出力 ************/
            if (map_output.get("EXAM_TYPE"   ) != null) svf.VrsOut("EXAM_TYPE"    ,map_output.get("EXAM_TYPE"   ).toString());
            if (map_output.get("SCHOOLNAME"  ) != null) svf.VrsOut("SCHOOLNAME"   ,map_output.get("SCHOOLNAME"  ).toString());
            if (map_output.get("SUBCLASS"    ) != null) svf.VrsOut("SUBCLASS"     ,map_output.get("SUBCLASS"    ).toString());
            if (map_output.get("TESTDIV"     ) != null) svf.VrsOut("TESTDIV"      ,map_output.get("TESTDIV"     ).toString());
            if (map_output.get("APPLICANTDIV") != null) svf.VrsOut("APPLICANTDIV" ,map_output.get("APPLICANTDIV").toString());
            if (map_output.get("TOTAL_PAGE"  ) != null) svf.VrsOut("TOTAL_PAGE"   ,map_output.get("TOTAL_PAGE"  ).toString());
            if (map_output.get("NENDO"       ) != null) svf.VrsOut("NENDO"        ,map_output.get("NENDO"       ).toString());
            if (map_output.get("DATE"        ) != null) svf.VrsOut("DATE"         ,map_output.get("DATE"        ).toString());
        } catch (SQLException e) {
            log.error("ヘッダー出力の区間がエラーらしい", e);
        }
    }

    private void printFoot(DB2UDB db2, Vrw32alp svf, String[] category_selected) {
        try {
            if (sum==null && avg == null && manCount == null && womanCount == null && allCount == null) {

                PreparedStatement ps = null;

                ps = db2.prepareStatement(setStatement5(category_selected)); //COUNTが帰ってくる。
                /******************************** 男の合計 ***************************************/
                ps.setString( 1, "1" );
                ResultSet rs5_1 = ps.executeQuery(); //COUNTが帰ってくる（男の合計）
                if (rs5_1.next()) {
                    manCount = rs5_1.getString("COUNT");
                }
                if (manCount == null) manCount = "0";
                rs5_1.close();
                /******************************** 女の合計 ***************************************/
                ps.setString( 1, "2" );
                ResultSet rs5_2 = ps.executeQuery(); //COUNTが帰ってくる（女の合計）
                if (rs5_2.next()) {
                    womanCount = rs5_2.getString("COUNT");
                    log.debug("womanCount="+womanCount);
                }
                if (womanCount == null) womanCount = "0";
                rs5_2.close();

                // リストtoリストで選択した全ての試験会場の合計人数
                ps = db2.prepareStatement(setStatement6(category_selected));
                ResultSet rs6   = ps.executeQuery(); //TOTAL_SCHが帰ってくる
                if (rs6.next()) {
                    allCount = rs6.getString("TOTAL_SCH");
                }
                if (allCount == null) allCount = "0";
                rs6.close();

                // 帳票に出力する試験会場の生徒の試験科目の合計点と平均点を表示する
                ps = db2.prepareStatement(setStatement8(category_selected));
                ResultSet rs8   = ps.executeQuery();
                if (rs8.next()) {
                    sum = rs8.getString("SUM");
                    avg = rs8.getString("AVG");
                }
                if (sum == null) sum = "0";
                if (avg == null) avg = "0";
                rs8.close();
                ps.close();
                db2.commit();
            }
        } catch (SQLException ex) {
            log.debug("printFoot error!", ex);
        }
        String str = "男"+ manCount +"名, 女"+ womanCount +"名, 計" + allCount + "名, 合計" + sum + "点, 平均"+ avg +"点";
        svf.VrsOut("NOTE",str);
    }



    /*■■■■■   print svfの設定   ■■■■■*/
    private void setSvfInit(HttpServletResponse response, Vrw32alp svf) {
        response.setContentType("application/pdf");
        svf.VrInit();                                             //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定
        } catch( java.io.IOException ex ) {
            log.info("db new error:" + ex);
        }
    }


    /*■■■■■   DB接続   ■■■■■*/
    private DB2UDB setDb(HttpServletRequest request) {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
        } catch(Exception ex) {
            log.info("db new error:" + ex);
            if(db2 != null)db2.close();
        }
        return db2;
    }


    /*■■■■■   DBのOPEN   ■■■■■*/
    private boolean openDb(DB2UDB db2) {
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("db open error!"+ex);
            return true;
        }
        return false;
    }


    /*■■■■■   DBのCLOSE   ■■■■■*/
    private void closeDb(DB2UDB db2) {
        try {
            db2.commit();
            db2.close();
        } catch( Exception ex ){
            log.error("db close error!"+ex );
        }
    }


    /*■■■■■   SVFのCLOSE   ■■■■■*/
    private void closeSvf(Vrw32alp svf) {
        if(!nonedata){
            int ret = svf.VrSetForm("MES001.frm", 0);
            if (false && 0 != ret) {ret = 0;}
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }
        svf.VrQuit();
    }

    /*■■■■■   西暦表示にするのかのフラグ   ■■■■■*/
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


    /* ■■■■■■■■■■■■■■■■■ 以下クエリです。 ■■■■■■■■■■■■■■■■■■■ */
    private String setStatement1(String category_selected) {                //会場名と受験型を取得するSQLの発行
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.EXAMHALL_NAME, ");
        stb.append("     T2.NAME1 AS EXAMNAME ");
        stb.append(" FROM ");
        stb.append("           ENTEXAM_HALL_DAT T1 ");
        stb.append(" LEFT JOIN NAME_MST T2 ON  T2.NAMECD1 = 'L005' ");
        stb.append("                       AND T2.NAMECD2 = T1.EXAM_TYPE ");
        stb.append(" WHERE ");
        stb.append("     EXAMHALLCD = '"+ category_selected             +"' AND ");
        stb.append("     TESTDIV    = '"+ map_param.get("TESTDIV") +"' AND ");
        stb.append("     EXAM_TYPE  = '"+ map_param.get("EXAM_TYPE") +"' ");

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }


    private String setStatement2() {                //学校名を取得するSQLの発行
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     SCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     CERTIF_SCHOOL_DAT ");
        stb.append(" WHERE ");
        if (map_param.get("APPLICANTDIV").equals("1")) {
            stb.append("     CERTIF_KINDCD = '105' AND ");
        } else {
            stb.append("     CERTIF_KINDCD = '106' AND ");
        }
        stb.append("     YEAR = '"+ map_param.get("YEAR") +"' ");

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }



    private String setStatement3() {            //受験科目を取得するSQLの発行
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     NAME1 AS TESTSUBCLASCD_NAME ");
        stb.append(" FROM ");
        stb.append("     NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     NAMECD1 = 'L009' AND ");
        stb.append("     NAMECD2 = '"+ map_param.get("TESTSUBCLASSCD") +"' ");//NAMECD2 は TESTSUBCLASSCD

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }

    private String setStatement4() {            //入試制度と入試区分を取得
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.NAME1 AS TESTDIV_NAME, ");
        stb.append("     T2.NAME1 AS APPLICANTDIV_NAME ");
        stb.append(" FROM ");
        stb.append("     NAME_MST T1,NAME_MST T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.NAMECD1 = 'L004' AND ");
        stb.append("     T1.NAMECD2 = '"+ map_param.get("TESTDIV") +"' AND ");
        stb.append("     T2.NAMECD1 = 'L003' AND ");
        stb.append("     T2.NAMECD2 = '"+ map_param.get("APPLICANTDIV") +"' ");

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }



    private String setStatement5(String[] category_selected) { // 性別ごとの人数
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3  ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR   ");
        stb.append("                                         AND T3.EXAMNO = T1.EXAMNO, ");
        stb.append("     ENTEXAM_HALL_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.TESTDIV      = '"+ map_param.get("TESTDIV")      +"' AND ");
        stb.append("     T2.EXAM_TYPE    = '"+ map_param.get("EXAM_TYPE")    +"' AND ");
        stb.append("     T2.EXAMHALLCD   in "+ SQLUtils.whereIn(true, category_selected) +" AND ");
        stb.append("     INT(T1.RECEPTNO) BETWEEN INT(T2.S_RECEPTNO) AND INT(T2.E_RECEPTNO) AND ");
        stb.append("     T1.ENTEXAMYEAR  = '"+ map_param.get("YEAR")         +"' AND ");
        stb.append("     T1.APPLICANTDIV = '"+ map_param.get("APPLICANTDIV") +"' AND ");
        stb.append("     T1.TESTDIV      = '"+ map_param.get("TESTDIV")      +"' AND ");
        stb.append("     T1.EXAM_TYPE    = '"+ map_param.get("EXAM_TYPE")    +"' AND ");
        stb.append("     T3.SEX          = ? ");
        if (isCollege() && "1".equals(map_param.get("APPLICANTDIV")) && "1".equals(map_param.get("TESTDIV")) && "3".equals(map_param.get("TESTSUBCLASSCD"))) {
            stb.append("     AND T3.SUB_ORDER = '1' "); //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        }
        stb.append(" GROUP BY ");
        stb.append("     T3.SEX ");

        log.debug("SEIBETU_COUNT = " + stb.toString());
        return stb.toString();
    }


    private String setStatement6(String[] category_selected) { // 全ての試験会場の総人数
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS TOTAL_SCH ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO, ");
        stb.append("     ENTEXAM_HALL_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.TESTDIV      = '"+ map_param.get("TESTDIV")      +"' AND ");
        stb.append("     T2.EXAM_TYPE    = '"+ map_param.get("EXAM_TYPE")    +"' AND ");
        stb.append("     T2.EXAMHALLCD   in "+ SQLUtils.whereIn(true, category_selected) +"  AND ");
        stb.append("     INT(T1.RECEPTNO)BETWEEN INT(T2.S_RECEPTNO) AND ");
        stb.append("     INT(T2.E_RECEPTNO) AND ");
        stb.append("     T1.ENTEXAMYEAR  = '"+ map_param.get("YEAR")         +"' AND ");
        stb.append("     T1.APPLICANTDIV = '"+ map_param.get("APPLICANTDIV") +"' AND ");
        stb.append("     T1.TESTDIV      = '"+ map_param.get("TESTDIV")      +"' AND ");
        stb.append("     T1.EXAM_TYPE    = '"+ map_param.get("EXAM_TYPE")    +"' ");
        if (isCollege() && "1".equals(map_param.get("APPLICANTDIV")) && "1".equals(map_param.get("TESTDIV")) && "3".equals(map_param.get("TESTSUBCLASSCD"))) {
            stb.append("     AND T3.SUB_ORDER = '1' "); //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        }

        return stb.toString();
    }

    private String setStatement7(String category_selected) {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     INT(T1.RECEPTNO) AS RECEPTNO, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T2.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ");
        stb.append("     ENTEXAM_SCORE_DAT T2 ");
        stb.append("     ON  T1.ENTEXAMYEAR    = T2.ENTEXAMYEAR  AND ");
        stb.append("         T1.APPLICANTDIV   = T2.APPLICANTDIV AND ");
        stb.append("         T1.TESTDIV        = T2.TESTDIV      AND ");
        stb.append("         T1.EXAM_TYPE      = T2.EXAM_TYPE    AND ");
        stb.append("         T1.RECEPTNO       = T2.RECEPTNO     AND ");
        stb.append("         T2.TESTSUBCLASSCD = '"+ map_param.get("TESTSUBCLASSCD") +"' ");
        stb.append("     LEFT JOIN ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T3 ");
        stb.append("     ON  T1.ENTEXAMYEAR    = T3.ENTEXAMYEAR  AND ");
        stb.append("         T1.EXAMNO         = T3.EXAMNO ");
        stb.append("     LEFT JOIN ");
        stb.append("     NAME_MST T5 ");
        stb.append("     ON  T5.NAMECD2        = T3.SEX AND ");
        stb.append("         T5.NAMECD1        = 'Z002', ");
        stb.append("     ENTEXAM_HALL_DAT T5 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR        = '"+ map_param.get("YEAR")         +"'    AND ");
        stb.append("     T1.APPLICANTDIV       = '"+ map_param.get("APPLICANTDIV") +"'    AND ");
        stb.append("     T1.TESTDIV            = '"+ map_param.get("TESTDIV")      +"'    AND ");
        stb.append("     T1.EXAM_TYPE          = '"+ map_param.get("EXAM_TYPE")    +"'    AND ");
        stb.append("     INT(T1.RECEPTNO) BETWEEN INT(T5.S_RECEPTNO) AND INT(T5.E_RECEPTNO) AND ");
        stb.append("     T5.TESTDIV            = '"+ map_param.get("TESTDIV")      +"'    AND ");
        stb.append("     T5.EXAM_TYPE          = '"+ map_param.get("EXAM_TYPE")    +"'    AND ");
        stb.append("     T5.EXAMHALLCD         = '"+ category_selected             +"' ");
        if (isCollege() && "1".equals(map_param.get("APPLICANTDIV")) && "1".equals(map_param.get("TESTDIV")) && "3".equals(map_param.get("TESTSUBCLASSCD"))) {
            stb.append("     AND T3.SUB_ORDER = '1' "); //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        }
        stb.append(" ORDER BY 1 ");
        log.debug("setStatement7 sql="+stb.toString());
        return stb.toString();
    }

    /* 帳票に出力する試験会場の生徒の試験科目の合計点と平均点を表示する */
    private String setStatement8(String[] category_selected) {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAM_TYPE, ");
        stb.append("     T1.TESTSUBCLASSCD, ");
        stb.append("     SUM(T1.SCORE) AS SUM, ");
        stb.append("     DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*10, 0)/10, 5, 1) AS AVG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("             AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("             AND T2.TESTDIV      = T1.TESTDIV ");
        stb.append("             AND T2.EXAM_TYPE    = T1.EXAM_TYPE ");
        stb.append("             AND T2.RECEPTNO     = T1.RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_HALL_DAT T3 ");
        stb.append("              ON T3.TESTDIV      = T1.TESTDIV ");
        stb.append("             AND T3.EXAM_TYPE    = T1.EXAM_TYPE ");
        stb.append("             AND T3.EXAMHALLCD in "+ SQLUtils.whereIn(true, category_selected) +" ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND B1.EXAMNO = T2.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR        ='"+ map_param.get("YEAR")     +"' ");
        stb.append("     AND T1.EXAM_TYPE      ='"+ map_param.get("EXAM_TYPE")+"' ");
        stb.append("     AND T1.APPLICANTDIV   ='"+ map_param.get("APPLICANTDIV")+"' ");
        stb.append("     AND T1.TESTDIV        ='"+ map_param.get("TESTDIV")+"' ");
        stb.append("     AND T1.TESTSUBCLASSCD ='"+ map_param.get("TESTSUBCLASSCD")+"' ");
        stb.append("     AND T1.RECEPTNO BETWEEN T3.S_RECEPTNO AND T3.E_RECEPTNO ");
        if (isCollege() && "1".equals(map_param.get("APPLICANTDIV")) && "1".equals(map_param.get("TESTDIV")) && "3".equals(map_param.get("TESTSUBCLASSCD"))) {
            stb.append("     AND B1.SUB_ORDER = '1' "); //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAM_TYPE, ");
        stb.append("     T1.TESTSUBCLASSCD ");

        return stb.toString();
    }

    private String setStatement9(String[] category_selected) { // 試験会場の総ページ
        StringBuffer stb = new StringBuffer();
        stb.append("WITH MK AS ( ");
        stb.append(" SELECT ");
        stb.append("     (CASE WHEN MOD(COUNT(*), "+gyousu+") = 0 THEN ");
        stb.append("        COUNT(*) / "+gyousu+"  ");
        stb.append("           WHEN COUNT(*) = 0 THEN 1 ");
        stb.append("      ELSE COUNT(*) / "+gyousu+" + 1 END) AS TOTAL_SCH ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO, ");
        stb.append("     ENTEXAM_HALL_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T2.TESTDIV      = '"+ map_param.get("TESTDIV")      +"' AND ");
        stb.append("     T2.EXAM_TYPE    = '"+ map_param.get("EXAM_TYPE")    +"' AND ");
        stb.append("     T2.EXAMHALLCD   in "+ SQLUtils.whereIn(true, category_selected) +" AND ");
        stb.append("     INT(T1.RECEPTNO)BETWEEN INT(T2.S_RECEPTNO) AND ");
        stb.append("     INT(T2.E_RECEPTNO) AND ");
        stb.append("     T1.ENTEXAMYEAR  = '"+ map_param.get("YEAR")         +"' AND ");
        stb.append("     T1.APPLICANTDIV = '"+ map_param.get("APPLICANTDIV") +"' AND ");
        stb.append("     T1.TESTDIV      = '"+ map_param.get("TESTDIV")      +"' AND ");
        stb.append("     T1.EXAM_TYPE    = '"+ map_param.get("EXAM_TYPE")    +"' ");
        if (isCollege() && "1".equals(map_param.get("APPLICANTDIV")) && "1".equals(map_param.get("TESTDIV")) && "3".equals(map_param.get("TESTSUBCLASSCD"))) {
            stb.append("     AND T3.SUB_ORDER = '1' "); //1:Ⅰ型(国算理)、2:Ⅱ型(国算)
        }
        stb.append(" GROUP BY ");
        stb.append("     T2.EXAMHALLCD ");
        stb.append(" ) SELECT ");
        stb.append("     (CASE WHEN MOD(SUM(TOTAL_SCH), 3) = 0 THEN ");
        stb.append("        SUM(TOTAL_SCH) / 3  ");
        stb.append("      ELSE  SUM(TOTAL_SCH) / 3 + 1 END) AS TOTAL_PAGE ");
        stb.append(" FROM MK ");
        log.debug("setStatement9 sql="+stb.toString());
        return stb.toString();
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

    boolean isWakayama() {
        return "30300049001".equals(_z010SchoolCode);
    }

    boolean isGojo() {
        return "30290053001".equals(_z010SchoolCode);
    }

    boolean isCollege() {
        return "30290086001".equals(_z010SchoolCode);
    }
}// class end
