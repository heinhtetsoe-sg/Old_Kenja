package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３００Ｃ＞  座席ラベル
 *
 *  2008/11/13 RTS 作成日
 **/

public class KNJL300C {

    private static final Log log = LogFactory.getLog(KNJL300C.class);
    private String[] _hallCdList;
    private String _receptNumberRange;
    private int _startRow;
    private int _startColumn;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[]  = getParam(request);
        
        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());  //PDFファイル名の設定
        ret = svf.VrSetForm("KNJL300C.frm", 1);
        
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ
        for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);

        try{
            //SQL作成
            String year=param[0];
            String testDiv = param[2]; 
            String examType = param[4];
            
            String sql = preStat1(year, testDiv, examType);
            log.debug("preStat1 sql="+sql);
            ps1 = db2.prepareStatement(sql);
            //SVF出力
            String nendoNumber= KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(param[0]+"-01-01"))[1];
            // 変換例: "2008"+"01-01" -> nendoNumber = "20" (String[]{"平成","20","1","1"}[1])
            if (setSvfout(db2, svf, nendoNumber, ps1)) nonedata = true;  //帳票出力のメソッド
        }catch (SQLException ex) {
            log.debug("DB2 SQL exception "+ex);
        }
    
        //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 1);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        preStatClose(psTestdiv,ps1,psTotalPage);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り
    
    private String[] getParam(HttpServletRequest request) {

        String[] param = new String[10];
        
        //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = null;                                               //入試制度(不使用)
            param[2] = request.getParameter("TESTDIV");                      //入試区分
            param[3] = request.getParameter("LOGIN_DATE");                  // ログイン日付
            param[4] = request.getParameter("EXAM_TYPE");                  // 受験型
            param[6] = request.getParameter("RECEPTNO_FROM");                  // 受付番号
            param[7] = request.getParameter("RECEPTNO_TO");                  // 受付番号
            param[8] = request.getParameter("LINE");                  // 開始位置
            param[9] = request.getParameter("ROW");                  // 開始位置
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        String[] halls = request.getParameterValues("CATEGORY_SELECTED");

        for(int i=0; i<halls.length; i++) {
            halls[i] = halls[i].substring(0, 4);
        }
        
        _hallCdList = halls; 
        //log.debug("hall list="+SQLUtils.whereIn(true, _hallCdList)+"  (length="+_hallCdList.length+")");

        if (_hallCdList.length == 1) {
            if (param[6]==null || param[6].equals("") || param[7]==null || param[7].equals("")) {
                _receptNumberRange = null;
            } else {
                _receptNumberRange = " '"+param[6]+"' and '"+param[7]+"' ";
            }
            log.debug(" _receptNumberRange = "+_receptNumberRange);

            if (param[8]==null || param[9]==null) {
                _startRow = 1;
                _startColumn = 1;
            } else {
                _startRow = Integer.valueOf(param[8]).intValue();
                _startColumn = Integer.valueOf(param[9]).intValue();
            }
            log.debug(" _startRow = "+_startRow+" , _startColumn = "+_startColumn);
        } else {
            _receptNumberRange = null;
            _startRow = 1;
            _startColumn = 1;
        }
        

        return param;
    }

    // 文字列 text を 文字列 splitで分割した配列を返す
    private static String[] split(String text, String split){
        if(text==null) return null;
        List splitted = null;
        int beforeindex=0;
        int index=beforeindex;
        while(text.indexOf(split,beforeindex)!=-1){
            index=text.indexOf(split,beforeindex);
            if(splitted==null) splitted=new ArrayList();
            String item = text.substring(beforeindex, index);
            if (!"".equals(item))
                splitted.add(item);
            beforeindex = index + 1;
        }

        String item = text.substring(beforeindex);
        if (!"".equals(item)) {
            if(splitted==null) splitted=new ArrayList();
            splitted.add(item);
        }

        String[] s = new String[splitted.size()];

        for(int i=0; i<splitted.size(); i++){
            s[i] = (String) splitted.get(i);
        }
        return s;
    }

    /**帳票出力 **/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String nendoNumber,
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int page=1;
        try {
            ResultSet rs = ps1.executeQuery();
            int currentRow = _startRow;
            int currentColumn = _startColumn;
            final int maxRow = 5;
            final int maxColumn = 4;
            int examineeCount = 0;
            while( rs.next() ){
                if (maxColumn < currentColumn ) {
                    svf.VrEndRecord();
                    currentColumn = 1;
                    currentRow += 1;
                    if (maxRow < currentRow) {
                        svf.VrEndPage();
                        page += 1;
                        currentRow = 1;
                    }
                }

                examineeCount += 1;
                String examNo = rs.getString("EXAMNO");
                String testDiv = rs.getString("TESTDIV");
                String nendo_applicantdiv = nendoNumber;
                if ("1".equals(testDiv) || "2".equals(testDiv)) {
                    nendo_applicantdiv += "中";
                } else if ("3".equals(testDiv) || "4".equals(testDiv) || "5".equals(testDiv)) {
                    nendo_applicantdiv += "高";
                }
                //log.debug(examineeCount+"["+page+","+currentRow+","+currentColumn+"] "+rs.getString("EXAMHALLCD")+" : "+nendo_applicantdiv+" ("+testDiv+"), "+ rs.getString("RECEPTNO") + " , " + examNo + " , "+ rs.getString("NAME"));

                ret = svf.VrsOutn("EXAMNO"+currentColumn, currentRow, "受験番号 "+examNo+" 番");
                svfVrsOutnFormat(svf, "NAME"+currentColumn+"_", currentRow, 10, rs.getString("NAME"), null);
                ret = svf.VrsOutn("TESTDIV"+currentColumn, currentRow, nendo_applicantdiv);

                currentColumn += 1;

                nonedata = true;
            }
            //最終レコードを出力
            if (nonedata) {
                ret = svf.VrEndPage();//レコードを出力
                setSvfInt(svf);         //ブランクセット
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!"+ex);
        }
        return nonedata;
    }


    /** ラベル取得 */
    private String preStat1(String year, String testDiv, String examType)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" select ");
            stb.append("     t2.EXAMHALLCD, ");
            stb.append("     t1.RECEPTNO, ");
            stb.append("     t1.EXAMNO, ");
            stb.append("     t1.TESTDIV, ");
            stb.append("     t3.NAME ");
            stb.append(" from ENTEXAM_RECEPT_DAT t1 ");
            stb.append("     inner join ENTEXAM_HALL_DAT t2 on ");
            stb.append("         t1.TESTDIV = t2.TESTDIV and ");
            stb.append("         t1.EXAM_TYPE = t2.EXAM_TYPE and ");
            stb.append("         t1.RECEPTNO between t2.S_RECEPTNO and t2.E_RECEPTNO ");
            stb.append("     inner join ENTEXAM_APPLICANTBASE_DAT t3 on ");
            stb.append("         t1.ENTEXAMYEAR = t3.ENTEXAMYEAR and ");
            stb.append("         t1.APPLICANTDIV = t3.APPLICANTDIV and ");
            stb.append("         t1.TESTDIV = t3.TESTDIV and ");
            stb.append("         t1.EXAMNO = t3.EXAMNO ");
            stb.append(" where ");
            if (_receptNumberRange != null)
                stb.append("     t1.RECEPTNO between "+_receptNumberRange+" and ");
            stb.append("     t1.ENTEXAMYEAR = '"+year+"' and ");
            stb.append("     t1.TESTDIV = '"+testDiv+"' and ");
            stb.append("     t2.EXAM_TYPE = '"+examType+"' and ");
            stb.append("     t2.EXAMHALLCD in "+SQLUtils.whereIn(true, _hallCdList)+" ");
            stb.append(" order by ");
            stb.append("     t2.EXAMHALLCD, t1.EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

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
        } catch( Exception e ){
            log.error("preStatClose error!"+e);
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

    private int svfVrsOutFormat(Vrw32alp svf,String area_name, int len, String sval1, String sval2) {
        if (sval1 == null || "null".equals(sval1)) return 0;
        if (sval2 == null || "null".equals(sval2)) {
            sval2 = "";
        } else if ("ADDRESS".equals(area_name)){
            sval2 = " " + sval2;
        }
        return svf.VrsOut(setformatArea(area_name, len, sval1+sval2), sval1+sval2 );
    }

    private int svfVrsOutnFormat(Vrw32alp svf,String area_name, int gyou, int len, String sval1, String sval2) {
        if (sval1 == null || "null".equals(sval1)) return 0;
        if (sval2 == null || "null".equals(sval2)) {
            sval2 = "";
        } else if ("ADDRESS".equals(area_name)){
            sval2 = " " + sval2;
        }
        return svf.VrsOutn(setformatArea(area_name, len, sval1+sval2), gyou, sval1+sval2 );
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
    
}//クラスの括り
