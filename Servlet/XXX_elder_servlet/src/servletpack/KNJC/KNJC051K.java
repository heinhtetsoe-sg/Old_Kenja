package servletpack.KNJC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [出欠管理]
 *
 *                  ＜ＫＮＪＣ０５１Ｋ＞  出席簿（科目別）
 *
 *  2004/08/31 yamashiro・プログラムＩＤをKNJC051Kへ変更 KNJC051を継承
 *  2004/09/14 yamashiro・SQL文の不具合を修正->ペナルティ欠課選択の場合SQLの不具合が起きる
 *  2004/10/17 yamashiro・欠課時数累計を出力する
 *  2004/10/30 yamashiro・欠課時数における'*'出力はしない
 *  2004/11/12 yamashiro・欠課カウントに１日欠席を含める
 *  2004/11/15 yamashiro・累計処理は時間割データとリンク(学籍番号・日付・校時)したデータを集計する
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJC051K extends KNJC051 {

    private static final Log log = LogFactory.getLog(KNJC051K.class);


    /** 出欠集計 04/10/17Add KNJC051をoverwrite 04/10/26Modify **/
    void Set_Detail_4_1(Vrw32alp svf,ResultSet rs,Map hm1,String s1){

        try {
            Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
            if( int1!=null ){
                cnt_sum[0][int1.intValue()-1] += rs.getInt("ABSENT");
                cnt_sum[1][int1.intValue()-1] += rs.getInt("LATE");
                cnt_sum[2][int1.intValue()-1] += rs.getInt("EARLY");
                cnt_sum[3][int1.intValue()-1] += rs.getInt("ABSENT2");

                //if( rs.getInt("ABSENT2") > c_chr[Integer.parseInt(s1)-1] / 3 )
                if (rs.getInt("ABSENT2") >= c_chr[Integer.parseInt(s1)-1] / 3) {     // 04/10/26Modify
                    srs_sum[0][int1.intValue()-1] = true;    //欠課時数* 04/10/26Add
                }
                //log.debug("ichi="+int1.intValue()+"  absent2="+rs.getInt("ABSENT2")+"  c_chr["+s1+"]"+c_chr[Integer.parseInt(s1)-1]);

                if (s1.equals(param[1])) {
                    if( cnt_sum[3][int1.intValue()-1] >= getKekkaover( rs.getInt("GRADE") ) )
                        srs_sum[1][int1.intValue()-1] = true;    //欠課時数累計* 04/10/17Add 04/10/26Modify
                }
            }
        } catch( SQLException ex ){
            log.warn("ResultSet read error!",ex);
        }

    }//Set_Detail_4_1()の括り


    /** 出欠集計出力 04/10/26Add **/
    void Set_Detail_4_2(Vrw32alp svf ,Map hm1 ,String s1) {

        try {
            for (int i = 0 ; i < cnt_sum[0].length && i < hm1.size() ; i++ ) {
                svf.VrsOutn("TOTAL_ABSENCE"+s1 ,i+1 ,getSumstr(cnt_sum[0][i]) );    //欠席
                svf.VrsOutn("TOTAL_LATE"   +s1 ,i+1 ,getSumstr(cnt_sum[1][i]) );    //遅刻
                svf.VrsOutn("TOTAL_LEAVE"  +s1 ,i+1 ,getSumstr(cnt_sum[2][i]) );    //早退

                //if( srs_sum[0][i] == true )   04/10/30Modify ココでの'*'出力は削除
                //  svf.VrsOutn("TOTAL_TIME"+s1 ,i+1 ,"*" + getSumstr(cnt_sum[3][i]) ); //欠課時数
                //else
                svf.VrsOutn("TOTAL_TIME"+s1 ,i+1 ,getSumstr(cnt_sum[3][i]) );           //欠課時数

                if (s1.equals(param[1])) {   // 04/10/17Add
                    svf.VrAttributen("TOTAL_TIME4" ,i+1 ,"Hensyu=1");  // 04/10/19Add
                    if( srs_sum[1][i] == true )
                        svf.VrsOutn("TOTAL_TIME4" ,i+1 ,"*" + getSumstr(cnt_sum[3][i]) );   //欠課時数累計
                    else
                        svf.VrsOutn("TOTAL_TIME4" ,i+1 ,getSumstr(cnt_sum[3][i]) );     //欠課時数累計
                }
            }
        } catch (Exception ex) {
            log.warn("error!", ex);
        }

    }//Set_Detail_4_2()の括り


    /** 04/10/17Add **/
    private int getKekkaover(int grade){
        int value = 0;
        if( grade == 3 )value = subclasscredit * 8 * Integer.parseInt(param[1]) / 2 + 1;
        else            value = subclasscredit * 10 * Integer.parseInt(param[1]) / 3 + 1;
        if( subclasscredit == 0 )value = 0;
        if( value == 0 )value = 1000;
        return value;
    }


    /**PrepareStatement作成**/
    String Pre_Stat_4(boolean output) {

    //  出欠集計
        StringBuffer stb = new StringBuffer();
        try {
          /* **************************************
            stb.append("WITH ATTEND_A AS(");
            stb.append("SELECT W1.SCHREGNO,W2.GRADE,W2.HR_CLASS,W2.ATTENDNO,ATTENDDATE,PERIODCD,DI_CD ");
            stb.append("FROM   ATTEND_DAT W1,SCHREG_REGD_DAT W2,");
            stb.append(      "(SELECT DISTINCT SCHREGNO,CHAIRCD FROM CHAIR_STD_DAT ");
            stb.append(       "WHERE  YEAR=? AND SEMESTER=? AND CHAIRCD=? AND((APPDATE<=? AND APPENDDATE>=?)OR");
            stb.append(              "(APPDATE<=? AND APPENDDATE>=?)OR(APPDATE>=? AND APPENDDATE<=?)))AS W3 ");
            stb.append("WHERE  W1.ATTENDDATE>=? AND W1.ATTENDDATE<=? AND ");
            stb.append(       "W2.YEAR=? AND W2.SEMESTER=? AND W2.SCHREGNO=W1.SCHREGNO AND ");
            stb.append(       "W2.GRADE||W2.HR_CLASS||W2.ATTENDNO>=? AND W2.GRADE||W2.HR_CLASS||W2.ATTENDNO<=? AND ");
            //stb.append(       "W3.YEAR=? AND W3.SEMESTER=? AND W3.CHAIRCD=? AND ");
            stb.append(       "W3.SCHREGNO=W2.SCHREGNO AND W3.CHAIRCD=W1.CHAIRCD) ");
         ******************************************* */

           /*
            * 出欠データを時間割データ、講座生徒データとリンクした表
            * 2004/11/15Modify 出欠集計処理と同様にする => 時間割データとリンク
            **/
            stb.append("WITH ATTEND_A AS(");
            stb.append(  "SELECT S2.GRADE,S2.HR_CLASS,S2.ATTENDNO,S2.SCHREGNO,S2.ATTENDDATE,S2.PERIODCD,S2.DI_CD ");
            stb.append(  "FROM  (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD ");
            stb.append(         "FROM   SCH_CHR_DAT T1,");
            stb.append(                "CHAIR_STD_DAT T2,");
            stb.append(                "CHAIR_DAT T3 ");
            stb.append(         "WHERE  T1.YEAR = '" + param[0] + "' AND ");
            stb.append(                "T1.SEMESTER = ? AND ");
            stb.append(                "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(                "? <= T1.EXECUTEDATE AND T1.EXECUTEDATE <= ? AND ");
            stb.append(                "T1.PERIODCD != '0' AND ");
            stb.append(                "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(                "T1.YEAR = T2.YEAR AND ");
            stb.append(                "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(                "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(                "T1.CHAIRCD = T3.CHAIRCD AND ");
            stb.append(                "T1.YEAR = T3.YEAR AND ");
            stb.append(                "T1.SEMESTER = T3.SEMESTER ");
            stb.append(         "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD)S1,");
            stb.append(        "(SELECT W1.SCHREGNO,W2.GRADE,W2.HR_CLASS,W2.ATTENDNO,ATTENDDATE,PERIODCD,DI_CD ");
            stb.append(         "FROM   ATTEND_DAT W1,");
            stb.append(                "SCHREG_REGD_DAT W2 ");
            stb.append(         "WHERE  ? <= W1.ATTENDDATE AND W1.ATTENDDATE <= ? AND ");
            stb.append(                "W2.YEAR = '" + param[0] + "' AND ");
            stb.append(                "W2.SEMESTER = ? AND ");
            stb.append(                "W2.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                "? <= W2.GRADE||W2.HR_CLASS||W2.ATTENDNO AND W2.GRADE||W2.HR_CLASS||W2.ATTENDNO <= ? )S2 ");
            stb.append(   "WHERE S1.SCHREGNO = S2.SCHREGNO AND ");
            stb.append(         "S1.EXECUTEDATE = S2.ATTENDDATE AND ");
            stb.append(         "S1.PERIODCD = S2.PERIODCD)");

            //メイン表
            stb.append("SELECT T1.SCHREGNO,GRADE,");
            if( output ){       //遅刻と早退を欠課に換算する処理を行う場合->遅刻、早退の減算は遅刻を優先！
                stb.append(   "ABSENT,LATE,EARLY,");
                stb.append(   "VALUE(ABSENT,0)+VALUE(ABSENT2,0)AS ABSENT2 ");
            } else {
                //stb.append(     "ABSENT,LATE,EARLY,0 AS ABSENT2 ");       // 04/09/14Modify
                stb.append(   "ABSENT,LATE,EARLY,ABSENT AS ABSENT2 ");      // 04/10/26Modify
            }
            stb.append("FROM  (SELECT SCHREGNO,GRADE, "); // 04/10/17Modify Add 'GRADE'
                //１日忌引、１日出停以外の忌引、出停は欠席としてカウントする 04/07/13仕様を確認済み
                //忌引、出停は欠席としてカウントしない 04/07/13仕様変更
            //stb.append(         "SUM(CASE DI_CD WHEN '3' THEN 1 ELSE NULL END)AS MOURNING, ");
            //stb.append(         "SUM(CASE DI_CD WHEN '2' THEN 1 ELSE NULL END)AS SUSPEND, ");
            //stb.append(         "SUM(CASE WHEN DI_CD IN('4','5','6','14') THEN 1 ELSE NULL END)AS ABSENT, ");
            stb.append(       "SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13') THEN 1 ELSE NULL END)AS ABSENT, ");   // 04/11/12Modify
            stb.append(       "SUM(CASE DI_CD WHEN '15' THEN 1 ELSE NULL END)AS LATE, ");
            stb.append(       "SUM(CASE DI_CD WHEN '16' THEN 1 ELSE NULL END)AS EARLY ");
            stb.append("FROM  ATTEND_A W1 ");
            //stb.append("WHERE DI_CD IN('4','5','6','14','15','16') ");
            stb.append("WHERE DI_CD IN('4','5','6','14','15','16','11','12','13') ");   // 04/11/12Modify
            stb.append("GROUP BY SCHREGNO,GRADE "); // 04/10/17Modify Add 'GRADE'
            stb.append(      ")T1 ");

            if (output) {       //遅刻と早退を欠課に換算する処理を行う場合
                stb.append(      "LEFT JOIN(SELECT SCHREGNO,COUNT(*)/3 AS ABSENT2 ");
                stb.append(                "FROM   ATTEND_A W1 ");
                stb.append(                "WHERE  DI_CD IN('15','16')  ");
                stb.append(                "GROUP BY SCHREGNO ");
                stb.append(      ")T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            }
        } catch( Exception e ){
            System.out.println("[KNJC051K]Pre_Stat_4 error!");
            System.out.println( e );
        }
        return stb.toString();

    }//Pre_Stat_4()の括り


}//クラスの括り
