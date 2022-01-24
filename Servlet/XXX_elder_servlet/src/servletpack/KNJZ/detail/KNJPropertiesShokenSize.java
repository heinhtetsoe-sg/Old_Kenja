/*
 * $Id: 4e30c0777bda732f32435456b04601177cffad76 $
 *
 * 作成日: 2017/01/31
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJPropertiesShokenSize {
    
    private static final Log log = LogFactory.getLog(KNJPropertiesShokenSize.class);

    /**
     * 文字数
     */
    public int _mojisu;

    /**
     * 行数
     */
    public int _gyo;

    /**
     * 指定桁
     */
    private int _setKeta;
    /**
     * 指定桁を使用するか
     */
    private boolean _isSetKeta;
    
    public KNJPropertiesShokenSize(final int mojisu, final int gyo) {
        _mojisu = mojisu;
        _gyo = gyo;
    }

    /**
     * 指定桁をセット
     * @param setKeta 指定桁
     */
    public void setKeta(final int setKeta) {
    	_setKeta = setKeta;
    	_isSetKeta = true;
    }

    /**
     * 桁を追加
     * @param keta 桁
     */
    public KNJPropertiesShokenSize createAddKeta(final int keta) {
    	final KNJPropertiesShokenSize size = new KNJPropertiesShokenSize(_mojisu, _gyo);
    	size.setKeta(getKeta() + keta);
    	return size;
    }

    /**
     * 指定桁を使用するか
     * @return 指定桁を使用するか
     */
    public boolean isUseKeta() {
    	return _isSetKeta;
    }

    /**
     * 桁数（指定桁 or 文字数×2）
     * @return 桁数を得る
     */
    public int getKeta() {
    	if (_isSetKeta) {
    		return _setKeta;
    	}
        return _mojisu * 2;
    }
    
    public boolean isValid() {
    	return (_isSetKeta && -1 != _setKeta || !_isSetKeta && -1 != _mojisu) && (-1 != _gyo);
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータからKNJPropertiesShokenSizeインスタンスを作成
     * @param paramString パラメータの値
     * @param mojisuDefault 文字数デフォルト
     * @param gyoDefault 行数デフォルト
     * @return KNJPropertiesShokenSizeインスタンスを作成
     */
    public static KNJPropertiesShokenSize getShokenSize(final String paramString, final int mojisuDefault, final int gyoDefault) {
        final int mojisu = KNJPropertiesShokenSize.getParamSizeNum(paramString, 0);
        final int gyo = KNJPropertiesShokenSize.getParamSizeNum(paramString, 1);
        KNJPropertiesShokenSize size = new KNJPropertiesShokenSize(mojisu, gyo);
        if (!size.isValid()) {
            size = new KNJPropertiesShokenSize(mojisuDefault, gyoDefault);
        }
        return size;
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    public static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        if (StringUtils.isBlank(param)) {
            return num;
        }
        final String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), "*");
        for (int i = 0; i < nums.length; i++) {
        	nums[i] = StringUtils.trim(nums[i]);
        }
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }
    
    public String toString() {
    	return "KNJPropertiesShokenSize(_mojisu = " + _mojisu + ", _gyo = " + _gyo + ", (isSetKeta = " + _isSetKeta + (_isSetKeta ? ", setKeta = " + _setKeta : "") + "))";
    }
}

// eof
