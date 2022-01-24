/*
 * KenjaProperties.java
 *
 * Created on 2003/10/30, 10:49
 */

package nao_package;

import java.util.*;

import nao_package.db.DB2UDB;

/**
 * 賢者向けプロパティー
 * @author  takaesu
 * @version 1.0
 */
public class KenjaProperties extends Properties {
    // 和暦テーブル（元号、年、月、日）年月日はその元号の始まり日
    // 大正の始まり日、1912/7/30よりも古いのは全て“明治”とする。なので1800/1/1も明治。
    // 古い順から並べる
    static Object[][] warekiTable = {
        { "明治", new GregorianCalendar(1868, 9-1, 8) },
        { "大正", new GregorianCalendar(1912, 7-1, 30) },
        { "昭和", new GregorianCalendar(1926, 12-1, 25) },
        { "平成", new GregorianCalendar(1989, 1-1, 8) },
        { "令和", new GregorianCalendar(2019, 5-1, 1) },
    };

    /** Creates a new instance of KenjaProperties */
    public KenjaProperties() {
        /***
        super();
        this.load(new java.io.FileInputStream("kenja.properties"));  // プロパティファイルの読み込み
         */
    }

    /**
     * 西暦⇒和暦変換
     * @param seireki ４桁の西暦
     * @return 和暦を返す
     * @deprecated use {@link #KNJ_EditDate.gengou(DB2UDB db2, int seireki)}
     */
    public static String gengou(int seireki){
        int wrk;

        // 昭和の範囲
        if( seireki>=1926 && seireki<=1988 ){
            wrk = seireki - 1925;   // 大正１５年＝昭和元年＝１９２６年
            return (wrk==1)? "昭和元": "昭和"+wrk;
        }
        // 平成の範囲
        else if( seireki>=1989 && seireki<=2018 ){
            wrk = seireki - 1988;   // 昭和６４年＝平成元年＝１９８９年
            return (wrk==1)? "平成元": "平成"+wrk;
        }
        // 令和の範囲
        else if( seireki>=2019 && seireki<=9999 ){
            wrk = seireki - 2018;   // 平成３１年＝令和元年＝２０１９年
            return (wrk==1)? "令和元": "令和"+wrk;
        }
        // 変換エラー
        else {
            return "変換err!";
        }
    }

    /**
     * 西暦⇒和暦変換<br>
     * “明治”より古い日付は IllegalArgumentException をスローします。<br>
     * 和暦の定義は以下の通り。<br>
     *         明治: 1868年 9月 8日〜<br>
     *         大正: 1912年 7月30日〜<br>
     *         昭和: 1926年12月25日〜<br>
     *         平成: 1989年 1月 8日〜<br>
     *         令和: 2019年 5月 1日〜<br>
     * @param yyyy    ４桁の西暦
     * @param mm    ２桁の年
     * @param dd    ２桁の日
     * @return 和暦を返す。例）1968/11/10 ⇒ "昭和43年11月10日"
     * @deprecated use {@link #KNJ_EditDate.gengou(DB2UDB db2, int yyyy, int mm, int dd)}
     */
    public static String gengou(int yyyy, int mm, int dd){
        int i = 0;
        int nen;    // 和暦の年。
        String rtn;

        // 日付の妥当性チェック
        Calendar cal = new GregorianCalendar();
        cal.setLenient( false );
        cal.set( yyyy, mm-1, dd );
        try{
//            Date ud = cal.getTime();
            cal.getTime();
        } catch( IllegalArgumentException e ){
            throw e;
        }

        // “明治”より古い日付はエラー
        if( cal.before(warekiTable[0][1]) ){
            throw new IllegalArgumentException();
        }

        //
        String wrk;
        for(i=0; i<warekiTable.length; i++){
            if( cal.before(warekiTable[i][1]) ){
                // 和暦の年を算出
                nen = yyyy - (((Calendar)(warekiTable[i-1][1])).get(Calendar.YEAR)) + 1;

                // １年は「元年」
                if( nen==1 ){
                    wrk = (String)warekiTable[i-1][0] + "元";
                } else {
                    wrk = (String)warekiTable[i-1][0] + nen;
                }
                // 日付文字列の作成
                rtn = wrk
                    + "年"
                    + mm + "月"
                    + dd + "日";

                //
                return rtn;
            }
        }

        // 和暦変換
        nen = yyyy - (((Calendar)(warekiTable[i-1][1])).get(Calendar.YEAR)) + 1;
        // １年は「元年」
        if( nen==1 ){
            wrk = (String)warekiTable[i-1][0] + "元";
        } else {
            wrk = (String)warekiTable[i-1][0] + nen;
        }
        // 日付文字列の作成
        rtn = wrk
            + "年"
            + mm + "月"
            + dd + "日";

        //
        return rtn;
    }
}
