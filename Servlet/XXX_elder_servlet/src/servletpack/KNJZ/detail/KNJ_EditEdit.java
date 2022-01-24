// kanji=漢字
/*
 * $Id$
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 文字列チェック＆編集
 */
// 2005/08/13 yamashiro 文字列の前ブランク挿入（桁あわせ）editFlontBrankメソッドを追加
// 2006/04/21 yamashiro 組コードを編集するメソッド Ret_Num_Str の別バージョンを作成 => 引数が異なる

public class KNJ_EditEdit
{
    private static final Log log = LogFactory.getLog(KNJ_EditEdit.class);
    String strone;

    public KNJ_EditEdit(String pstrx){
        this.strone = pstrx;
        if(this.strone == null) this.strone = "";
    }


/*---------------------------------------------------------------------------------*
 * 文字列長（ｂｙｔｅ）取得
 *---------------------------------------------------------------------------------
 *@deprecated
 **/
    public int ret_byte(int pintx){

        byte arr_byte[] = new byte[pintx];  //byte配列を定義
        try {
            arr_byte = strone.getBytes("MS932");       //文字列をbyte配列へ
        } catch (Exception e) {
            log.error("exception! stron = " + strone, e);
        }
        int w_byte = arr_byte.length;       //byte数を取得

        return w_byte;


    }//int ret_byteの括り




/*---------------------------------------------------------------------------------*
 * 文字列長（ｂｙｔｅ）取得
 *---------------------------------------------------------------------------------
 *@deprecated
 **/
    public static int ret_byte_2(String strone,int pintx){

        if( strone==null )  return 0;
        if( strone.length()==0 )    return 0;

        byte arr_byte[] = new byte[pintx];  //byte配列を定義
        try {
            arr_byte = strone.getBytes("MS932");       //文字列をbyte配列へ
        } catch (Exception e) {
            log.error("exception! stron = " + strone, e);
        }
        int w_byte = arr_byte.length;       //byte数を取得
        return w_byte;


    }//int ret_byte2の括り



/*---------------------------------------------------------------------------------*
 * 文字変換     "-" --> "の"
 *---------------------------------------------------------------------------------*/
    public String replace_char(){

        StringBuffer stb = new StringBuffer(strone);
        String moji = new String();

        for(int ia=0 ; ia<stb.length() ; ia++){
            moji = stb.substring(ia,ia+1);
            if(moji.equals("-")){
                stb.replace(ia,ia+1,"の");
                continue;
            }
            if(moji.equals("1")){
                stb.replace(ia,ia+1,"一");
                continue;
            }
            if(moji.equals("2")){
                stb.replace(ia,ia+1,"二");
                continue;
            }
            if(moji.equals("3")){
                stb.replace(ia,ia+1,"三");
                continue;
            }
            if(moji.equals("4")){
                stb.replace(ia,ia+1,"四");
                continue;
            }
            if(moji.equals("5")){
                stb.replace(ia,ia+1,"五");
                continue;
            }
            if(moji.equals("6")){
                stb.replace(ia,ia+1,"六");
                continue;
            }
            if(moji.equals("7")){
                stb.replace(ia,ia+1,"七");
                continue;
            }
            if(moji.equals("8")){
                stb.replace(ia,ia+1,"八");
                continue;
            }
            if(moji.equals("9")){
                stb.replace(ia,ia+1,"九");
                continue;
            }
            if(moji.equals("0")){
                stb.replace(ia,ia+1,"〇");
                continue;
            }
        }

        return stb.toString();


    }//replace_charの括り



    /*------------------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力
     *      改行マークとフィールド長を考慮しながら出力する
     *------------------------------------------------------------------------------------
     *@deprecated
     **/
    public Vector get_token(int f_len ,int f_cnt){

            //String h_befor : 編集元の文字列
            //int f_len : 出力先のフィールド長
            //int f_cnt : 出力先のフィールド数

        Vector f_array = new Vector(f_cnt);             //編集後文字列を格納する配列
        StringBuffer stry = new StringBuffer();         //SVF出力用
        int len = 0;                                    //文字列byteカウント用
        int ib = 0;                                     //SVFフィールドカウント用

        for( int ic=0 ; ic<strone.length() ; ic++ ){
            String strz = strone.substring(ic,ic+1);               //1文字
            byte[] SendB = {};
            try {
                SendB = strz.getBytes("MS932"); // 1文字byteカウント用 getBytes("MS932")にすべき?
            } catch (Exception e) {
                log.error("exception! strz = " + strz, e);
            }
            if( strz.equals("\r") || strz.equals("\n") ){           //改行マークがある場合、強制的に次行へ
                len = f_len+1;
                continue;
            }
            if( ((len+SendB.length)>f_len) ){
                f_array.addElement(stry.toString());                //１行文字列
                ib++;
                if(ib == f_cnt) break;
                len = 0;
            }
            if( len == 0 )  stry.delete(0,stry.length());
            stry.append(strz);
            len = len + SendB.length;
        }
        if( len>0 & ib<f_cnt )  f_array.addElement(stry.toString());

        return f_array;

    }//get_tokenの括り



    /*------------------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力
     *  2004/02/13 作成   改行マークとフィールド長を考慮しながら出力する
     *------------------------------------------------------------------------------------
     * @deprecated
     **/
    public Vector get_token(int f_len,int f_cnt,int rmark){

//String h_befor : 編集元の文字列
//int f_len : 出力先のフィールド長
//int f_cnt : 出力先のフィールド数

        Vector f_array = new Vector(f_cnt);             //編集後文字列を格納する配列
        StringBuffer stry = new StringBuffer();         //SVF出力用
        int len = 0;                                    //文字列byteカウント用
        int ib = 0;                                     //SVFフィールドカウント用
        String strz;                                    //1文字用

        for( int ic=0 ; ic<strone.length() ; ic++ ){
            strz = strone.substring(ic,ic+1);           //1文字
            byte[] SendB = {};
            try {
                SendB = strz.getBytes();
            } catch (Exception e) {
                log.error("exception! strz = " + strz, e);
            }
            if( strz.equals("\r") || strz.equals("\n") ){
                if( rmark==0 ){             //改行マークがある場合、強制的に次行へ
                    len = f_len+1;
                    continue;
                }
                if( rmark==1 ){             //改行マークは除外
                    continue;
                }
            }
            if( ((len+SendB.length)>f_len) ){
                f_array.addElement(stry.toString());                //１行文字列
                ib++;
                if(ib == f_cnt) break;
                len = 0;
            }
            if( len == 0 )  stry.delete(0,stry.length());
            stry.append(strz);
            len = len + SendB.length;
        }
        if( len>0 & ib<f_cnt )  f_array.addElement(stry.toString());

        return f_array;

    }//get_tokenの括り



    /*------------------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力
     *  2004/06/28 作成   改行マークがなければ文字数で改行する
     *  2004/08/24 変更   メソッドをget_token_1へ変更-->調査書等の仕様再変更に伴いget_tokenを修正
     *      String strx : 編集元の文字列
     *      int f_len   : 行当りの文字数（Byte)
     *      int f_cnt   : 行数
     *------------------------------------------------------------------------------------
     * @deprecated
     **/
    public static String get_token_1(String strx,int f_len,int f_cnt)[]{

        if( strx==null )return null;
        if( strx.length()==0 )return null;

    //  改行マークの存在チェック
        int ia = strx.indexOf("\n");
        if( ia<0 )ia = strx.indexOf("\r");

        String stoken[] = new String[f_cnt];
    //  改行マークが存在する --> 改行マークで行替え
        if( ia>-1 ){
            StringTokenizer stkn = new StringTokenizer(strx, "\r\n");
            for( int ib=0 ; stkn.hasMoreTokens() && ib<f_cnt ; ib++ )stoken[ib] = stkn.nextToken();
//System.out.println("[v]String get_token()  1");
        }else{
            char schar[] = strx.toCharArray();          //文字列をchar配列へ入れる
            byte sbyte[] = new byte[3];                 //1文字byteカウント用
            int slen = 0;
            int s_sta = 0;
            int ib = 0;
            int stop = 0;
            for( int s_cur=0 ; s_cur<schar.length && ib<f_cnt ; s_cur++ ){
                if( stop++>1000 )break;
                try {
                    sbyte = String.valueOf(schar[s_cur]).getBytes("MS932");
                } catch (Exception e) {
                    log.error("exception!", e);
                }
                slen+=sbyte.length;
                if( slen>f_len ){
                    stoken[ib++] = strx.substring(s_sta,s_cur);
                    slen = sbyte.length;
                    s_sta = s_cur;
                }
            }
            if( slen>0 && ib<f_cnt )stoken[ib] = strx.substring(s_sta);
//System.out.println("[KNJ_EditEdit]String get_token()  2");
        }
        return stoken;

    }//String get_token()の括り




    /*------------------------------------------------------------------------------------*
     * 改行編集後ＳＶＦ出力
     *  2004/08/24 作成   改行マーク＆文字数で改行する-->
     *                      static String get_token_1(String,int,int)を改造
     *      String strx : 編集元の文字列
     *      int f_len   : 行当りの文字数（Byte)
     *      int f_cnt   : 行数
     *  2004/09/22 yamashiro・改行マークがない場合の不具合を修正
     *  2004/09/28 yamashiro・'?'等の文字が１バイトとして変換される不具合を修正
     *------------------------------------------------------------------------------------*/
    public static String[] get_token(String strx,int f_len,int f_cnt) {

        if( strx==null || strx.length()==0 )return null;

        String stoken[] = new String[f_cnt];        //分割後の文字列の配列
        byte sbyte[] = new byte[3];                 //1文字byteカウント用
        int slen = 0;                               //文字列のバイト数カウント
        int s_sta = 0;                              //文字列の開始位置
        int ib = 0;
        for( int s_cur=0 ; s_cur<strx.length() && ib<f_cnt ; s_cur++ ){
            //改行マークチェック    04/09/28Modify
            if( strx.charAt(s_cur)=='\r' )continue;
            if( strx.charAt(s_cur)=='\n' ){
                stoken[ib++] = strx.substring(s_sta,s_cur);
                slen = 0;
                s_sta = s_cur+1;
            } else{
            //文字数チェック
                try{
                    sbyte = (strx.substring(s_cur,s_cur+1)).getBytes("MS932");  // 04/09/28Modify
                } catch( Exception e ){
                    System.out.println("[KNJ_EditEdit]exam_out error!"+e );
                }
                slen+=sbyte.length;
//if(f_len==10)System.out.println("[KNJ_EditEdit]  strx="+strx.substring(s_cur,s_cur+1)+"  sbyte="+sbyte.length);
                if( slen>f_len ){
                    stoken[ib++] = strx.substring(s_sta,s_cur);     // 04/09/22Modify
                    slen = sbyte.length;
                    s_sta = s_cur;
                }
            }
        }
        if( slen>0 && ib<f_cnt )stoken[ib] = strx.substring(s_sta);

        return stoken;

    }//String get_token()の括り



    /**
     *  文字数を取得
     */
    public static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length; //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /**
     * 文字列の全角、半角を考慮した文字列幅を得る
     * @str 文字列
     * @return 文字列の全角、半角を考慮した文字列幅
     */
    public static int getTextKeta(final String text) {
    	// getMS932ByteLengthがMS932エンコーディング文字のみの判定に対し
    	// こちらはUNICODE文字すべて判定可（UNICODEの仕様上、桁が曖昧な文字はある）
        int keta = 0;
        if (null != text) {
        	final String[] hankakuHantei = {"Na", "N", "H"};
        	for (final char ch : text.toCharArray()) {
        		final String eaw = getUnicodeEastAsianWidth(ch);
        		final boolean isHankaku = ArrayUtils.contains(hankakuHantei, eaw);
        		final int mojiKeta;
				if (isHankaku) {
					mojiKeta = 1;
        		} else {
        			mojiKeta = 2;
        		}
				keta += mojiKeta;
        	}
        }
        return keta;
    }

	// https://takenspc.hatenadiary.org/entries/2011/11/26#1322252878
	// （日本語の場合は）この値が
	// Na（狭）、N（中立）、H（半角）だと半角
	// W（広）、F（全角）、A（曖昧）だと全角として扱うことが推奨されているようです。
	public static String getUnicodeEastAsianWidth(final char character) {
//    	var x = character.charCodeAt(0);
//    	var y = (character.length == 2) ? character.charCodeAt(1) : 0;
//    	var codePoint = x;
    	int x = character;
    	int y = x >= 0xFFFF ? x >> 16 : 0;
    	int codePoint = x - y;
    	if ((0xD800 <= x && x <= 0xDBFF) && (0xDC00 <= y && y <= 0xDFFF)) {
    		x &= 0x3FF;
    		y &= 0x3FF;
    		codePoint = (x << 10) | y;
    		codePoint += 0x10000;
    	}

    	if (
    	    (0x3000 == codePoint) ||
    	    (0xFF01 <= codePoint && codePoint <= 0xFF60) ||
    	    (0xFFE0 <= codePoint && codePoint <= 0xFFE6)
    	   ) {
    		return "F";
    	}
    	if (
    	    (0x20A9 == codePoint) ||
    	    (0xFF61 <= codePoint && codePoint <= 0xFFBE) ||
    	    (0xFFC2 <= codePoint && codePoint <= 0xFFC7) ||
    	    (0xFFCA <= codePoint && codePoint <= 0xFFCF) ||
    	    (0xFFD2 <= codePoint && codePoint <= 0xFFD7) ||
    	    (0xFFDA <= codePoint && codePoint <= 0xFFDC) ||
    	    (0xFFE8 <= codePoint && codePoint <= 0xFFEE)
    	   ) {
    		return "H";
    	}
    	if (
    	    (0x1100 <= codePoint && codePoint <= 0x115F) ||
    	    (0x11A3 <= codePoint && codePoint <= 0x11A7) ||
    	    (0x11FA <= codePoint && codePoint <= 0x11FF) ||
    	    (0x2329 <= codePoint && codePoint <= 0x232A) ||
    	    (0x2E80 <= codePoint && codePoint <= 0x2E99) ||
    	    (0x2E9B <= codePoint && codePoint <= 0x2EF3) ||
    	    (0x2F00 <= codePoint && codePoint <= 0x2FD5) ||
    	    (0x2FF0 <= codePoint && codePoint <= 0x2FFB) ||
    	    (0x3001 <= codePoint && codePoint <= 0x303E) ||
    	    (0x3041 <= codePoint && codePoint <= 0x3096) ||
    	    (0x3099 <= codePoint && codePoint <= 0x30FF) ||
    	    (0x3105 <= codePoint && codePoint <= 0x312D) ||
    	    (0x3131 <= codePoint && codePoint <= 0x318E) ||
    	    (0x3190 <= codePoint && codePoint <= 0x31BA) ||
    	    (0x31C0 <= codePoint && codePoint <= 0x31E3) ||
    	    (0x31F0 <= codePoint && codePoint <= 0x321E) ||
    	    (0x3220 <= codePoint && codePoint <= 0x3247) ||
    	    (0x3250 <= codePoint && codePoint <= 0x32FE) ||
    	    (0x3300 <= codePoint && codePoint <= 0x4DBF) ||
    	    (0x4E00 <= codePoint && codePoint <= 0xA48C) ||
    	    (0xA490 <= codePoint && codePoint <= 0xA4C6) ||
    	    (0xA960 <= codePoint && codePoint <= 0xA97C) ||
    	    (0xAC00 <= codePoint && codePoint <= 0xD7A3) ||
    	    (0xD7B0 <= codePoint && codePoint <= 0xD7C6) ||
    	    (0xD7CB <= codePoint && codePoint <= 0xD7FB) ||
    	    (0xF900 <= codePoint && codePoint <= 0xFAFF) ||
    	    (0xFE10 <= codePoint && codePoint <= 0xFE19) ||
    	    (0xFE30 <= codePoint && codePoint <= 0xFE52) ||
    	    (0xFE54 <= codePoint && codePoint <= 0xFE66) ||
    	    (0xFE68 <= codePoint && codePoint <= 0xFE6B) ||
    	    (0x1B000 <= codePoint && codePoint <= 0x1B001) ||
    	    (0x1F200 <= codePoint && codePoint <= 0x1F202) ||
    	    (0x1F210 <= codePoint && codePoint <= 0x1F23A) ||
    	    (0x1F240 <= codePoint && codePoint <= 0x1F248) ||
    	    (0x1F250 <= codePoint && codePoint <= 0x1F251) ||
    	    (0x20000 <= codePoint && codePoint <= 0x2F73F) ||
    	    (0x2B740 <= codePoint && codePoint <= 0x2FFFD) ||
    	    (0x30000 <= codePoint && codePoint <= 0x3FFFD)
    	   ) {
    		return "W";
    	}
    	if (
    	    (0x0020 <= codePoint && codePoint <= 0x007E) ||
    	    (0x00A2 <= codePoint && codePoint <= 0x00A3) ||
    	    (0x00A5 <= codePoint && codePoint <= 0x00A6) ||
    	    (0x00AC == codePoint) ||
    	    (0x00AF == codePoint) ||
    	    (0x27E6 <= codePoint && codePoint <= 0x27ED) ||
    	    (0x2985 <= codePoint && codePoint <= 0x2986)
    	   ) {
    		return "Na";
    	}
    	if (
    	    (0x00A1 == codePoint) ||
    	    (0x00A4 == codePoint) ||
    	    (0x00A7 <= codePoint && codePoint <= 0x00A8) ||
    	    (0x00AA == codePoint) ||
    	    (0x00AD <= codePoint && codePoint <= 0x00AE) ||
    	    (0x00B0 <= codePoint && codePoint <= 0x00B4) ||
    	    (0x00B6 <= codePoint && codePoint <= 0x00BA) ||
    	    (0x00BC <= codePoint && codePoint <= 0x00BF) ||
    	    (0x00C6 == codePoint) ||
    	    (0x00D0 == codePoint) ||
    	    (0x00D7 <= codePoint && codePoint <= 0x00D8) ||
    	    (0x00DE <= codePoint && codePoint <= 0x00E1) ||
    	    (0x00E6 == codePoint) ||
    	    (0x00E8 <= codePoint && codePoint <= 0x00EA) ||
    	    (0x00EC <= codePoint && codePoint <= 0x00ED) ||
    	    (0x00F0 == codePoint) ||
    	    (0x00F2 <= codePoint && codePoint <= 0x00F3) ||
    	    (0x00F7 <= codePoint && codePoint <= 0x00FA) ||
    	    (0x00FC == codePoint) ||
    	    (0x00FE == codePoint) ||
    	    (0x0101 == codePoint) ||
    	    (0x0111 == codePoint) ||
    	    (0x0113 == codePoint) ||
    	    (0x011B == codePoint) ||
    	    (0x0126 <= codePoint && codePoint <= 0x0127) ||
    	    (0x012B == codePoint) ||
    	    (0x0131 <= codePoint && codePoint <= 0x0133) ||
    	    (0x0138 == codePoint) ||
    	    (0x013F <= codePoint && codePoint <= 0x0142) ||
    	    (0x0144 == codePoint) ||
    	    (0x0148 <= codePoint && codePoint <= 0x014B) ||
    	    (0x014D == codePoint) ||
    	    (0x0152 <= codePoint && codePoint <= 0x0153) ||
    	    (0x0166 <= codePoint && codePoint <= 0x0167) ||
    	    (0x016B == codePoint) ||
    	    (0x01CE == codePoint) ||
    	    (0x01D0 == codePoint) ||
    	    (0x01D2 == codePoint) ||
    	    (0x01D4 == codePoint) ||
    	    (0x01D6 == codePoint) ||
    	    (0x01D8 == codePoint) ||
    	    (0x01DA == codePoint) ||
    	    (0x01DC == codePoint) ||
    	    (0x0251 == codePoint) ||
    	    (0x0261 == codePoint) ||
    	    (0x02C4 == codePoint) ||
    	    (0x02C7 == codePoint) ||
    	    (0x02C9 <= codePoint && codePoint <= 0x02CB) ||
    	    (0x02CD == codePoint) ||
    	    (0x02D0 == codePoint) ||
    	    (0x02D8 <= codePoint && codePoint <= 0x02DB) ||
    	    (0x02DD == codePoint) ||
    	    (0x02DF == codePoint) ||
    	    (0x0300 <= codePoint && codePoint <= 0x036F) ||
    	    (0x0391 <= codePoint && codePoint <= 0x03A1) ||
    	    (0x03A3 <= codePoint && codePoint <= 0x03A9) ||
    	    (0x03B1 <= codePoint && codePoint <= 0x03C1) ||
    	    (0x03C3 <= codePoint && codePoint <= 0x03C9) ||
    	    (0x0401 == codePoint) ||
    	    (0x0410 <= codePoint && codePoint <= 0x044F) ||
    	    (0x0451 == codePoint) ||
    	    (0x2010 == codePoint) ||
    	    (0x2013 <= codePoint && codePoint <= 0x2016) ||
    	    (0x2018 <= codePoint && codePoint <= 0x2019) ||
    	    (0x201C <= codePoint && codePoint <= 0x201D) ||
    	    (0x2020 <= codePoint && codePoint <= 0x2022) ||
    	    (0x2024 <= codePoint && codePoint <= 0x2027) ||
    	    (0x2030 == codePoint) ||
    	    (0x2032 <= codePoint && codePoint <= 0x2033) ||
    	    (0x2035 == codePoint) ||
    	    (0x203B == codePoint) ||
    	    (0x203E == codePoint) ||
    	    (0x2074 == codePoint) ||
    	    (0x207F == codePoint) ||
    	    (0x2081 <= codePoint && codePoint <= 0x2084) ||
    	    (0x20AC == codePoint) ||
    	    (0x2103 == codePoint) ||
    	    (0x2105 == codePoint) ||
    	    (0x2109 == codePoint) ||
    	    (0x2113 == codePoint) ||
    	    (0x2116 == codePoint) ||
    	    (0x2121 <= codePoint && codePoint <= 0x2122) ||
    	    (0x2126 == codePoint) ||
    	    (0x212B == codePoint) ||
    	    (0x2153 <= codePoint && codePoint <= 0x2154) ||
    	    (0x215B <= codePoint && codePoint <= 0x215E) ||
    	    (0x2160 <= codePoint && codePoint <= 0x216B) ||
    	    (0x2170 <= codePoint && codePoint <= 0x2179) ||
    	    (0x2189 == codePoint) ||
    	    (0x2190 <= codePoint && codePoint <= 0x2199) ||
    	    (0x21B8 <= codePoint && codePoint <= 0x21B9) ||
    	    (0x21D2 == codePoint) ||
    	    (0x21D4 == codePoint) ||
    	    (0x21E7 == codePoint) ||
    	    (0x2200 == codePoint) ||
    	    (0x2202 <= codePoint && codePoint <= 0x2203) ||
    	    (0x2207 <= codePoint && codePoint <= 0x2208) ||
    	    (0x220B == codePoint) ||
    	    (0x220F == codePoint) ||
    	    (0x2211 == codePoint) ||
    	    (0x2215 == codePoint) ||
    	    (0x221A == codePoint) ||
    	    (0x221D <= codePoint && codePoint <= 0x2220) ||
    	    (0x2223 == codePoint) ||
    	    (0x2225 == codePoint) ||
    	    (0x2227 <= codePoint && codePoint <= 0x222C) ||
    	    (0x222E == codePoint) ||
    	    (0x2234 <= codePoint && codePoint <= 0x2237) ||
    	    (0x223C <= codePoint && codePoint <= 0x223D) ||
    	    (0x2248 == codePoint) ||
    	    (0x224C == codePoint) ||
    	    (0x2252 == codePoint) ||
    	    (0x2260 <= codePoint && codePoint <= 0x2261) ||
    	    (0x2264 <= codePoint && codePoint <= 0x2267) ||
    	    (0x226A <= codePoint && codePoint <= 0x226B) ||
    	    (0x226E <= codePoint && codePoint <= 0x226F) ||
    	    (0x2282 <= codePoint && codePoint <= 0x2283) ||
    	    (0x2286 <= codePoint && codePoint <= 0x2287) ||
    	    (0x2295 == codePoint) ||
    	    (0x2299 == codePoint) ||
    	    (0x22A5 == codePoint) ||
    	    (0x22BF == codePoint) ||
    	    (0x2312 == codePoint) ||
    	    (0x2460 <= codePoint && codePoint <= 0x24E9) ||
    	    (0x24EB <= codePoint && codePoint <= 0x254B) ||
    	    (0x2550 <= codePoint && codePoint <= 0x2573) ||
    	    (0x2580 <= codePoint && codePoint <= 0x258F) ||
    	    (0x2592 <= codePoint && codePoint <= 0x2595) ||
    	    (0x25A0 <= codePoint && codePoint <= 0x25A1) ||
    	    (0x25A3 <= codePoint && codePoint <= 0x25A9) ||
    	    (0x25B2 <= codePoint && codePoint <= 0x25B3) ||
    	    (0x25B6 <= codePoint && codePoint <= 0x25B7) ||
    	    (0x25BC <= codePoint && codePoint <= 0x25BD) ||
    	    (0x25C0 <= codePoint && codePoint <= 0x25C1) ||
    	    (0x25C6 <= codePoint && codePoint <= 0x25C8) ||
    	    (0x25CB == codePoint) ||
    	    (0x25CE <= codePoint && codePoint <= 0x25D1) ||
    	    (0x25E2 <= codePoint && codePoint <= 0x25E5) ||
    	    (0x25EF == codePoint) ||
    	    (0x2605 <= codePoint && codePoint <= 0x2606) ||
    	    (0x2609 == codePoint) ||
    	    (0x260E <= codePoint && codePoint <= 0x260F) ||
    	    (0x2614 <= codePoint && codePoint <= 0x2615) ||
    	    (0x261C == codePoint) ||
    	    (0x261E == codePoint) ||
    	    (0x2640 == codePoint) ||
    	    (0x2642 == codePoint) ||
    	    (0x2660 <= codePoint && codePoint <= 0x2661) ||
    	    (0x2663 <= codePoint && codePoint <= 0x2665) ||
    	    (0x2667 <= codePoint && codePoint <= 0x266A) ||
    	    (0x266C <= codePoint && codePoint <= 0x266D) ||
    	    (0x266F == codePoint) ||
    	    (0x269E <= codePoint && codePoint <= 0x269F) ||
    	    (0x26BE <= codePoint && codePoint <= 0x26BF) ||
    	    (0x26C4 <= codePoint && codePoint <= 0x26CD) ||
    	    (0x26CF <= codePoint && codePoint <= 0x26E1) ||
    	    (0x26E3 == codePoint) ||
    	    (0x26E8 <= codePoint && codePoint <= 0x26FF) ||
    	    (0x273D == codePoint) ||
    	    (0x2757 == codePoint) ||
    	    (0x2776 <= codePoint && codePoint <= 0x277F) ||
    	    (0x2B55 <= codePoint && codePoint <= 0x2B59) ||
    	    (0x3248 <= codePoint && codePoint <= 0x324F) ||
    	    (0xE000 <= codePoint && codePoint <= 0xF8FF) ||
    	    (0xFE00 <= codePoint && codePoint <= 0xFE0F) ||
    	    (0xFFFD == codePoint) ||
    	    (0x1F100 <= codePoint && codePoint <= 0x1F10A) ||
    	    (0x1F110 <= codePoint && codePoint <= 0x1F12D) ||
    	    (0x1F130 <= codePoint && codePoint <= 0x1F169) ||
    	    (0x1F170 <= codePoint && codePoint <= 0x1F19A) ||
    	    (0xE0100 <= codePoint && codePoint <= 0xE01EF) ||
    	    (0xF0000 <= codePoint && codePoint <= 0xFFFFD) ||
    	    (0x100000 <= codePoint && codePoint <= 0x10FFFD)
    	   ) {
    		return "A";
    	}

    	return "N";
    }

	/**
     * dataをcount文字数で分割し配列を返す
     * 主に縦書き用
     */
    public static String[] splitByLength(final String data, int count) {
        if (null == data || data.length() == 0) {
            return new String[] {};
        }
        final int cnt = data.length() / count;
        final int cntAmari = data.length() % count;
        final int forCnt = cntAmari > 0 ? cnt + 1 : cnt;
        final String[] retStr = new String[forCnt];
        String dataHoge = data;
        for (int i = 0; i < forCnt; i++) {
            if (dataHoge.length() < count) {
                retStr[i] = dataHoge.substring(0, dataHoge.length());
                dataHoge = dataHoge.substring(dataHoge.length());
            } else {
                retStr[i] = dataHoge.substring(0, count);
                dataHoge = dataHoge.substring(count);
            }
        }
        return retStr;
    }

/*---------------------------------------------------------------------------------*
 * 数値チェック＆文字列へ変換  2004/08/18作成
 *      組をフォームへ出力する際、数値ならゼロサプレイスして返す
 *---------------------------------------------------------------------------------*/
    public static String Ret_Num_Str(String strx){

        if( strx==null )    return null;
        if( strx.length()==0 )  return null;

        char schar[] = strx.toCharArray();          //文字列をchar配列へ入れる
        boolean boodigit = true;
        for( int ia=0; ia<schar.length ; ia++ ){
            if( !Character.isDigit(schar[ia]) ){
                boodigit = false;
                break;
            }
        }

//System.out.println("[KNJ_EditEdit]static String Ret_Num_Str() boodigit="+boodigit);
//System.out.println("[KNJ_EditEdit]static String Ret_Num_Str() ret="+String.valueOf(Integer.parseInt(strx)));
        if( boodigit ){
            try {
                return String.valueOf(Integer.parseInt(strx));
            } catch( Exception e ){
                log.error("error!" + e );
            }
        }
        return strx;


    }//static String Ret_Num_Str()の括り


    /**
     *  数値チェック＆文字列へ変換
     *  引数のMapのキーに組コードがあればMapの値を返し、無い場合は従来通り数値ならゼロサプレイスして返す
     *  2006/04/21 yamashiro
     */
    public static String Ret_Num_Str( String str, Map hmap )
    {
        String sret = null;
        try {
            if( str != null  &&  hmap != null ){
                if( hmap.containsKey( str ) ) sret = (String)hmap.get( str );
            }
            if( sret == null ) sret = Ret_Num_Str( str );
        } catch( Exception e ){
            log.error("error! " + e );
        } finally{
            if( sret == null ) sret = "";
        }
        return sret;
    }


    /**
     *  文字列の前ブランク挿入（桁あわせ）
     *  205/08/13 Build
     */
    public static String editFlontBrank( String str, int len )
    {
        StringBuffer stb = new StringBuffer();
        try {
            for( int i = 0 ; i < len ; i++ )stb.append(" ");
            stb.append( str );
            if( len < stb.length() )stb.delete( 0, stb.length() - len );
        } catch( Exception e ){
            log.error("error! " + e );
        }
        return stb.toString();
    }


    /**
     *  svf print 数字を漢数字へ変換(文字単位)
     */
    public static String convertKansuuji(final String suuji)
    {
        final String arraykansuuji[] = {"〇","一","二","三","四","五","六","七","八","九","十"};
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < suuji.length(); i++) {
            if (Character.isDigit(suuji.charAt(i))) {
                stb.append(arraykansuuji[Integer.parseInt(suuji.substring(i, i + 1))]);
            } else {
                stb.append(suuji.substring(i, i + 1));
            }
        }
        stb.append("");
        return stb.toString();
    }

    /**
     *  svf print 数字を全角数字へ変換(文字単位)
     */
    public static String convertZenkakuSuuji(final String suuji)
    {
        final String arraykansuuji[] = {"０","１","２","３","４","５","６","７","８","９"};
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < suuji.length(); i++) {
            if (Character.isDigit(suuji.charAt(i))) {
                stb.append(arraykansuuji[Integer.parseInt(suuji.substring(i, i + 1))]);
            } else {
                stb.append(suuji.substring(i, i + 1));
            }
        }
        stb.append("");
        return stb.toString();
    }


    /**
     *  svf print 日付を漢数字変換
     */
    public static String getKansuujiWareki(String hdate)
    {
        StringBuffer stb = new StringBuffer();
        try{
            boolean dflg = false;       //数値？
            int ia = 0;
            for (int i=0 ; i<hdate.length() ; i++) {
                if (( Character.isDigit(hdate.charAt(i)) && !dflg ) || ( !Character.isDigit(hdate.charAt(i)) && dflg )) {
                    if (i == 0) continue;
                    if (!dflg) {
                        stb.append(hdate.substring(ia,i));
                    } else {
                        stb.append(convertKansuuji(Integer.parseInt(hdate.substring(ia,i))));
                    }
                    ia = i;
                    dflg = Character.isDigit(hdate.charAt(i));
                }
            }
            if (ia > 0) stb.append(hdate.substring(ia));
        } catch( Exception ex ){
            log.error("getKansuujiWareki error!",ex);
        }
        return stb.toString();
    }


    /**
     *  svf print 数字を漢数字へ変換.百の位まで(数値単位)
     */
    public static String convertKansuuji(int suuji)
    {
        String arraykansuuji[] = {"〇","一","二","三","四","五","六","七","八","九","十"};
        StringBuffer stb = new StringBuffer();
        int kurai = (String.valueOf(suuji)).length();
        if (kurai > 0) {
            if (Integer.parseInt((String.valueOf(suuji)).substring(kurai-1)) > 0)
                stb.append(arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-1))]);
        }
        if (kurai >= 2) {
            stb.insert(0,"十");
            if( Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1)) > 1 )
                stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1))]);
        }
        if (kurai >= 3) {
            stb.insert(0,"百");
            if( Integer.parseInt((String.valueOf(suuji)).substring(kurai-3,kurai-2)) > 1 )
                stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai-3,kurai-2))]);
        }
        stb.append("");
        return stb.toString();
    }

    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name 帳票出力エリア名
     * @param sval          値
     * @param area_len      制限文字数
     * @param hokan_Name1   制限文字以下の場合のエリア名
     * @param hokan_Name2   制限文字超の場合のエリア名
     * @return
     */
    public static String setformatArea(
            final String area_name,
            final String sval,
            final int area_len,
            final String hokan_Name1,
            final String hokan_Name2
    ) {

        String retAreaName = "";
        // 値がnullの場合はnullが返される
        if (sval == null) {
            return area_name + hokan_Name1;
        }
        // 設定値が制限文字超の場合、帳票設定エリアの変更を行う
        if(area_len >= sval.length()){
            retAreaName = area_name + hokan_Name1;
        } else {
            retAreaName = area_name + hokan_Name2;
        }
        return retAreaName;
    }

}//クラスの括り
