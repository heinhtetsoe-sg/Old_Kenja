package nao_package;

import java.util.*;

/*
 * 「曜日」クラス
 */
public final class Youbi {
    // 指定された日付の曜日文字列を返す
    public static String get( Calendar cal ){
        return get( cal.get(Calendar.DAY_OF_WEEK) );
    }
    public static String get( Date d ){
        Calendar cal = Calendar.getInstance();

        cal.setTime( d );
        return get( cal.get(Calendar.DAY_OF_WEEK) );
    }
    public static String get( int i ){
        switch( i ){
        case 1: return "日";
        case 2: return "月";
        case 3: return "火";
        case 4: return "水";
        case 5: return "木";
        case 6: return "金";
        case 7: return "土";
        default: return "？";
        }
    }
}
