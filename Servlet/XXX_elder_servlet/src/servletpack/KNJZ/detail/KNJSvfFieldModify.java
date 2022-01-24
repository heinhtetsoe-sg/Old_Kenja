// kanji=漢字
/*
 * $Id: 80763c4476461c7ff36bb669833a80910ccfe2db $
 *
 * 作成日: 2005/12/14
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  フォームのフィールド属性変更（ポイント＆Ｙ軸）
 **/

/*  ＳＶＦにおける印刷設定       ：１ドット＝１／４００インチ
 *  WINDOWSにおける文字サイズ設定：１ポイント＝１／７２インチ
 *
 *
 *        ----------------------
 *       ｜科目名　　　　｜　　｜
 *       A---------------B------
 *       ｜     　   　　｜　　｜
 *       C---------------D------
 *       ｜　　　　　　　｜　　｜
 *        ----------------------
 *   width;   AとBのY軸の差
 *   height;  AとCのX軸の差
 *   ystart;  AのX軸-height
 *   minnum;  文字の大きさを変えない最大文字数(バイト)
 *   maxnum;  SVF-FORMのフィールドの設定文字数(バイト)
 *
 */

public class KNJSvfFieldModify{

    private static final Log log = LogFactory.getLog(KNJSvfFieldModify.class);

    public int jiku;
    public float size;
    public int width;   //フィールドの幅(ドット)
    public int height;  //フィールドの高さ(ドット)
    public int ystart;  //開始位置(ドット)
    public int minnum;  //最小設定文字数
    public int maxnum;  //最大設定文字数

    /**
     *  ポイント＆Ｙ軸の設定
     *  引数について  String str : 出力する文字列
     *                int hnum   : 出力位置(行)
     */
    public void setRetvalue( String str, int hnum )
    {
        try {
            int num = retStringByteValue( str );          //文字数(BYTE)
            if( num < minnum )num = minnum;
            else
            if( num > maxnum )num = maxnum;
            size = retFieldPoint( num );                  //文字サイズ
            jiku = retFieldY() + ystart + height * hnum;  //出力位置＋Ｙ軸の移動幅
//log.debug("width="+width+"  height="+height+"   num="+num+"  size="+size);
        } catch( Exception ex ){
            log.error("setRetvalue error!"+ex );
            log.debug("size="+this.size+"   jiku="+this.jiku);
        }
    }


    /**
     *  文字数を取得
     */
    private int retStringByteValue( String str )
    {
        if ( str == null )return 0;
        int ret = 0;
        try {
            byte arrbyte[] = str.getBytes( "MS932" );   //文字列をbyte配列へ
            ret = arrbyte.length;                       //byte数を取得
        } catch( Exception ex ){
            log.error("retStringByteValue error!"+ex );
        }
        return ret;
    }


    /**
     *  文字サイズを設定
     */
    private float retFieldPoint( int num )
    {
        float ret = 0;
        try {
            //ret = width / ( num / 2 ) * 72 / 400;
//log.debug("ret="+(float)Math.round( (float)width / (num/2) * 72 / 400 * 10 ) / 10  );
            ret = (float)Math.round( (float)width / (num/2) * 72 / 400 * 10 ) / 10;
        } catch( Exception ex ){
            log.error("retFieldPoint error!"+ex );
        }
        return ret;
    }


    /**
     *  Ｙ軸の移動幅算出
     */
    private int retFieldY()
    {
        int ret = 0;
        try {
            ret = (int)Math.round( ( (double)height - ( size / 72 * 400 ) ) / 2 );
        } catch( Exception ex ){
            log.error("retFieldPoint error!"+ex );
        }
        return ret;
    }


}
