/**
 *
 *  学校教育システム 賢者 [入試管理] 推薦一般両方受験者チェックリスト
 *
 *                  ＜ＫＮＪＬ３０８Ｈ＞  推薦一般両方受験者チェックリスト
 *
 *  2008/10/29 RTS 作成日
 *
 **/

package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL308H {

    private static final Log log = LogFactory.getLog(KNJL308H.class);
    private boolean nonedata;
    private final int MAX_ITEM_PER_PAGE = 24;
    private final int MAX_NAME_LENGTH1 = 10;
    private final int MAX_KANA_LENGTH1 = 12;
    private final int MAX_FSCD_LENGTH1 = 13;
    
    private boolean _useName, _useKana, _useSex, _useBirthday, _useFscd;
    private String _year, _nendo, _date;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

    // パラメータの取得
        getParam(request);

    // print svf設定
        setSvfInit(response, svf);

    // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            System.out.println("db open error");
            return;
        }

    // 印刷処理
        printSvf(db2, svf);

    // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り


    /**
     *  svf print 印刷処理 
     */
    void printSvf(DB2UDB db2, Vrw32alp svf) {

        int ret = svf.VrSetForm("KNJL308H.frm", 4);
        if (false && 0 != ret) { ret = 0; }
        PreparedStatement ps = null;

        try {
            String sql = statementCheckList();
            log.debug("printSvf sql="+sql);
            ps = db2.prepareStatement(sql);
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
        printSvfMain(db2, svf, ps);

        try {
            if( ps != null ) ps.close();
        } catch( Exception ex ){
            log.error("printSvf error!",ex);
        }
    }

    private int svfVrsOutWithLengthCheck(Vrw32alp svf,String fieldhead, String data, int length1) {
        return svf.VrsOut(fieldhead+ (data.length() <= length1 ? "1" : "2"), data);
    }
    
    /**帳票出力（各通知書をセット）**/
    private void printSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1
    ) {
        int itemCount = 0;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        svf.VrsOut("NENDO", _nendo);
        svf.VrsOut("DATE", _date);
        String item_name = (_useName ? "*":"") + "氏名";
        String item_kana = (_useKana ? "*":"") + "氏名かな";
        String item_sex =  (_useSex  ? "*":"") + "性別";
        String item_birthday = (_useBirthday ? "*":"") + "生年月日";
        String item_finschool = (_useFscd    ? "*":"") + "出身学校";
        svf.VrsOut("ITEM_NAME1",     item_name);
        svf.VrsOut("ITEM_NAME2",     item_name);
        svf.VrsOut("ITEM_KANA1",     item_kana);
        svf.VrsOut("ITEM_KANA2",     item_kana);
        svf.VrsOut("ITEM_SEX1",      item_sex);
        svf.VrsOut("ITEM_SEX2",      item_sex);
        svf.VrsOut("ITEM_BIRTHDAY1", item_birthday);
        svf.VrsOut("ITEM_BIRTHDAY2", item_birthday);
        svf.VrsOut("ITEM_FINSCHOOL1",item_finschool);
        svf.VrsOut("ITEM_FINSCHOOL2",item_finschool);
        List suisenList = new ArrayList();
        HashMap jukenshaMap = new HashMap(); // key:推薦受験者, value:一般受験者のリスト
        try {
            ResultSet rs  = ps1.executeQuery();
            while( rs.next() ){
                itemCount += 1;
                String strBirthday, sDate_yyyy, sDate_mm, sDate_dd;
                String name=rs.getString("NAME");
                String name_kana=rs.getString("NAME_KANA");
                String sex=rs.getString("SEX");
                strBirthday = rs.getString("BIRTHDAY");
                sDate_yyyy = rs.getString("BIRTH_Y"); // 日付(年)
                sDate_mm   = rs.getString("BIRTH_M"); // 日付(月)
                sDate_dd   = rs.getString("BIRTH_D"); // 日付(日)
                String birthday=getFormattedBirthday(strBirthday, sDate_yyyy, sDate_mm, sDate_dd);
                String fs_cd = rs.getString("RITU_NAME") + "立" + rs.getString("FS_NAME");
                String examno = rs.getString("SUISEN_JUKEN_NO");

                String name2=rs.getString("NAME2");
                String name_kana2=rs.getString("NAME_KANA2");
                String sex2=rs.getString("SEX2");
                strBirthday = rs.getString("BIRTHDAY2");
                sDate_yyyy = rs.getString("BIRTH_Y2"); // 日付(年)
                sDate_mm   = rs.getString("BIRTH_M2"); // 日付(月)
                sDate_dd   = rs.getString("BIRTH_D2"); // 日付(日)
                String birthday2=getFormattedBirthday(strBirthday, sDate_yyyy, sDate_mm, sDate_dd);
                String fs_cd2 = rs.getString("RITU_NAME") + "立" + rs.getString("FS_NAME2");
                String examno2 = rs.getString("IPPAN_JUKEN_NO");

                // 推薦受験者
                Examinee suisen = new Examinee(name, name_kana, sex, birthday, fs_cd, examno);
                
                // 一般受験者
                Examinee ippan = new Examinee(name2, name_kana2, sex2, birthday2, fs_cd2, examno2);

                if( !suisenList.contains(suisen)) {
                    // 推薦受験者がリストにない場合追加する
                    suisenList.add(suisen);
                }
                
                List jukenshaList = null; // 推薦受験者と検索条件結果が一致する受験者のリスト
                if( jukenshaMap.containsKey(suisen)) {
                    jukenshaList = (List) jukenshaMap.get(suisen);
                } else {
                    jukenshaList = new ArrayList();
                    jukenshaMap.put(suisen, jukenshaList);
                }                
                jukenshaList.add(ippan);
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error(ex);
        }

        try{
            final int pageMax = itemCount / MAX_ITEM_PER_PAGE + 1;
            itemCount = 0;
            for(Iterator it=suisenList.iterator(); it.hasNext();){
                nonedata = true;
                // 推薦受験者
                Examinee suisen = (Examinee) it.next();  
                ret = svfVrsOutWithLengthCheck(svf, "NAME1_", suisen._name, MAX_NAME_LENGTH1);
                ret = svfVrsOutWithLengthCheck(svf, "KANA1_", suisen._kana, MAX_KANA_LENGTH1);
                ret = svf.VrsOut("SEX1",    suisen._sex);          // 性別
                ret = svf.VrsOut("BIRTHDAY1", suisen._birthday);   // 誕生日
                ret = svfVrsOutWithLengthCheck(svf, "FINSCHOOL1_", suisen._fscd, MAX_FSCD_LENGTH1);
                ret = svf.VrsOut("EXAMNO1",    suisen._examno);    // 推薦受験番号
                List examineeList = (List) jukenshaMap.get(suisen);
                for(Iterator it2=examineeList.iterator(); it2.hasNext();) {
                    for (int i=1; i<=6; i++) {
                        ret = svf.VrsOut("MASK"+i, suisen._examno);
                    }

                    itemCount += 1;
                    Examinee ippan = (Examinee) it2.next();
                    // 一般受験者
                    ret = svfVrsOutWithLengthCheck(svf, "NAME2_", ippan._name, MAX_NAME_LENGTH1);
                    ret = svfVrsOutWithLengthCheck(svf, "KANA2_", ippan._kana, MAX_KANA_LENGTH1);
                    ret = svf.VrsOut("SEX2",    ippan._sex);         // 性別
                    ret = svf.VrsOut("BIRTHDAY2", ippan._birthday);  // 誕生日
                    ret = svfVrsOutWithLengthCheck(svf, "FINSCHOOL2_", ippan._fscd, MAX_FSCD_LENGTH1);
                    ret = svf.VrsOut("EXAMNO2",   ippan._examno);    // 一般受験番号
                    ret = svf.VrEndRecord();
                    if (itemCount % MAX_ITEM_PER_PAGE ==1) { 
                        ret = svf.VrsOut("PAGE", ""+(itemCount/MAX_ITEM_PER_PAGE+1));
                        ret = svf.VrsOut("TOTAL_PAGE", ""+pageMax);
                    }
                }
            }
        } catch( Exception ex ) {
            log.error(ex);
        }
    }
    
    
    
    private class Examinee {
        final String _name;
        final String _kana;
        final String _sex;
        final String _birthday;
        final String _fscd;
        final String _examno;
        public Examinee(String name,String kana,String sex,String birthday,String fscd,String examno){
            _name = name;
            _kana = kana;
            _sex = sex;
            _birthday = birthday;
            _fscd = fscd;
            _examno = examno;
        }
        public boolean equals(Object o) {
            if (!(o instanceof Examinee))
                return false;
            Examinee other = (Examinee) o;
            return _examno.equals(other._examno);
        }
        public int hashCode() {
            return Integer.valueOf(_examno).intValue();
        }
    }


    /**
     *  推薦一般両方受験者を取得
     **/
    private String statementCheckList()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(" with T_SUISEN as ( ");
        sb.append(" select ");
        sb.append("     t1.EXAMNO    as SUISEN_JUKEN_NO, ");
        sb.append("     t1.NAME      as NAME, ");
        sb.append("     t1.NAME_KANA as NAME_KANA, ");
        sb.append("     t3.NAME2     as SEX, ");
        sb.append("     VALUE(t1.BIRTHDAY,'9999-12-31') as BIRTHDAY, ");
        sb.append("     VALUE(t1.BIRTH_Y,'00') as BIRTH_Y, ");
        sb.append("     VALUE(t1.BIRTH_M,'00') as BIRTH_M, ");
        sb.append("     VALUE(t1.BIRTH_D,'00') as BIRTH_D, ");
        sb.append("     t1.FS_CD     as FS_CD, ");
        sb.append("     t5.NAME1     as RITU_NAME, ");
        sb.append("     t1.FS_NAME   as FS_NAME ");
        sb.append(" from ");
        sb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
        sb.append(" inner join ENTEXAM_RECEPT_DAT t2 on ");
        sb.append("         t2.ENTEXAMYEAR = t1.ENTEXAMYEAR and ");
        sb.append("         t2.APPLICANTDIV = t1.APPLICANTDIV and ");
        sb.append("         t2.EXAMNO = t1.EXAMNO ");
        sb.append(" left join NAME_MST t3 on ");
        sb.append("         t3.NAMECD1 = 'Z002' and ");
        sb.append("         t1.SEX = t3.NAMECD2 ");
        sb.append(" left join FINSCHOOL_MST t4 ON  ");
        sb.append("         t4.FINSCHOOLCD = t1.FS_CD  ");
        sb.append(" left join NAME_MST t5 ON  ");
        sb.append("         t5.NAMECD1 = 'L001' and ");
        sb.append("         t5.NAMECD2 = t4.FINSCHOOL_DISTCD ");
        sb.append(" where ");
        sb.append("     t1.ENTEXAMYEAR='"+_year+"' and ");
        sb.append("     t1.APPLICANTDIV='3' ");
        sb.append(" ), T_IPPAN as ( ");
        sb.append(" select ");
        sb.append("     t1.EXAMNO    as IPPAN_JUKEN_NO, ");
        sb.append("     t1.NAME      as NAME2, ");
        sb.append("     t1.NAME_KANA as NAME_KANA2, ");
        sb.append("     t3.NAME2     as SEX2, ");
        sb.append("     VALUE(t1.BIRTHDAY,'9999-12-31') as BIRTHDAY2, ");
        sb.append("     VALUE(t1.BIRTH_Y,'00')  as BIRTH_Y2, ");
        sb.append("     VALUE(t1.BIRTH_M,'00')  as BIRTH_M2, ");
        sb.append("     VALUE(t1.BIRTH_D,'00')  as BIRTH_D2, ");
        sb.append("     t1.FS_CD     as FS_CD2, ");
        sb.append("     t5.NAME1     as RITU_NAME, ");
        sb.append("     t1.FS_NAME   as FS_NAME2 ");
        sb.append(" from ");
        sb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
        sb.append(" left join NAME_MST t3 on ");
        sb.append("         t3.NAMECD1 = 'Z002' and ");
        sb.append("         t1.SEX = t3.NAMECD2 ");
        sb.append(" left join FINSCHOOL_MST t4 ON  ");
        sb.append("         t4.FINSCHOOLCD = t1.FS_CD  ");
        sb.append(" left join NAME_MST t5 ON  ");
        sb.append("         t5.NAMECD1 = 'L001' and ");
        sb.append("         t5.NAMECD2 = t4.FINSCHOOL_DISTCD ");
        sb.append(" where ");
        sb.append("     t1.ENTEXAMYEAR='"+_year+"' and ");
        sb.append("     t1.APPLICANTDIV='2' ");
        sb.append(" ) ");
        sb.append(" select ");
        sb.append("     t1.*, ");
        sb.append("     t2.*  ");
        sb.append(" from ");
        sb.append("     T_SUISEN t1, ");
        sb.append("     T_IPPAN t2 ");
        boolean addedWhere = false;
        if (_useName) {
            sb.append( (addedWhere ? "    and ":" where  ")+" (t2.NAME2=t1.NAME) ");
            addedWhere = true;
        }
        if (_useKana) {
            sb.append( (addedWhere ? "    and ":" where  ")+" (t2.NAME_KANA2=t1.NAME_KANA)");
            addedWhere = true;
        }
        if (_useSex) {
            sb.append( (addedWhere ? "    and ":" where  ")+" ((t2.SEX2 = t1.SEX) or (t1.SEX is null and t2.SEX2 is null)) ");
            addedWhere = true;
        }
        if (_useBirthday) {
            sb.append( (addedWhere ? "    and ":" where  ")+" ((t2.BIRTHDAY2 = t1.BIRTHDAY or (t1.BIRTHDAY is null and t2.BIRTHDAY2 is null))) ");
            addedWhere = true;
        }
        if (_useFscd) {
            sb.append( (addedWhere ? "    and ":" where  ")+" ((t2.FS_CD2 = t1.FS_CD or (t1.FS_CD is null and t2.FS_CD2 is null))) ");
            addedWhere = true;
        }
        sb.append(" order by ");
        sb.append("     t1.NAME_KANA, t1.SUISEN_JUKEN_NO ");
        return sb.toString();
    }

    /**
     * 左側の'0'をトリムする
     */
    private String trimZero(String str) {
        for(int i=0; i<str.length(); i++) {
            if (str.charAt(i)!='0')
                return str.substring(i);
        }
        return null;
    }

    /**
     * 年月日の値をフォーマットする
     * @param sDate_yyyy 年
     * @param sDate_mm 月
     * @param sDate_dd 日
     * @return "年号Y年M月D日"
     */
    private String getFormattedBirthday(String strBirthday, String sDate_yyyy, String sDate_mm, String sDate_dd) {
        if (null != strBirthday && "9999-12-31".equals(strBirthday)) {
            return null;
        }
        if (null != strBirthday) {
            String year   = trimZero(strBirthday.substring(0,4)); // 日付(年)
            String month  = trimZero(strBirthday.substring(5,7)); // 日付(月)
            String day    = trimZero(strBirthday.substring(8,10));// 日付(日)
            return year + "年" + month + "月" + day + "日";
        } else {
            String year   = trimZero(sDate_yyyy.substring(0,4)); // 日付(年)
            String month  = trimZero(sDate_mm.substring(5,7)); // 日付(月)
            String day    = trimZero(sDate_dd.substring(8,10));// 日付(日)
            return year + "年" + month + "月" + day + "日";
        }
    }
    
    /** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request){
        String[] param = new String[8];
        try {
            String[] options = request.getParameterValues("category_selected");
            
            for(int i=0; i<options.length; i++) {
                log.debug("options["+i+"]="+options[i]);
                if("name".equals(options[i]))      _useName = true; // 名前
                if("name_kana".equals(options[i])) _useKana = true; // 名前かな
                if("sex".equals(options[i]))       _useSex = true; // 性別
                if("birthday".equals(options[i]))  _useBirthday = true; // 誕生日
                if("fs_cd".equals(options[i]))     _useFscd = true; // 出身学校
            }
            if (_useName ==false && _useKana == false) {
                _useName = true;
            }
            _year = request.getParameter("YEAR");
            _date = getFormattedBirthday(request.getParameter("DATE"),"","","");
            _nendo = request.getParameter("YEAR")+"年度";
            
            param[0] = new Boolean(_useName).toString();
            param[1] = new Boolean(_useKana).toString();
            param[2] = new Boolean(_useSex).toString();
            param[3] = new Boolean(_useBirthday).toString();
            param[4] = new Boolean(_useFscd).toString();
            param[5] = _year;
            param[6] = _date;
            param[7] = _nendo;
        } catch( Exception ex ) {
            System.out.println("get parameter error!" + ex);
        }
        for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("[KNJL308H]param[" + i + "]=" + param[i]);
    }


    /** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
        response.setContentType("application/pdf");
        int ret = svf.VrInit();                                         //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定
        } catch( java.io.IOException ex ){
            System.out.println("db new error:" + ex);
        }
   }


    /** svf close */
    private void closeSvf(Vrw32alp svf){
        if( !nonedata ){
            int ret = svf.VrSetForm("MES001.frm", 0);
            if (false && 0 != ret) { ret = 0; }
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }
        svf.VrQuit();
    }


    /** DB set */
    private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
        } catch( Exception ex ){
            System.out.println("db new error:" + ex);
            if( db2 != null)db2.close();
        }
        return db2;
    }


    /** DB open */
    private boolean openDb(DB2UDB db2){
        try {
            db2.open();
        } catch( Exception ex ){
            System.out.println("db open error!"+ex );
            return true;
        }//try-cathの括り
        return false;
    }//private boolean Open_db()


    /** DB close */
    private void closeDb(DB2UDB db2){
        try {
            db2.commit();
            db2.close();
        } catch( Exception ex ){
            System.out.println("db close error!"+ex );
        }//try-cathの括り
    }//private Close_Db()
}//クラスの括り
