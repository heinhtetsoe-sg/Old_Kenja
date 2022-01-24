// kanji=漢字
/*
 * $Id: bddebeca5ac053941204badb5057b3166a43aabd $
 *
 * 作成日: 2007/02/05 13:24:42 - JST
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import nao_package.svf.Vrw32alp;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * Vrw32alpをラップしたクラス。
 * doNumbering*(...)メソッドは
 *   メンバ変数_FieldNum[FIELDの付番の最大値]、_KurikaeshiNum[繰り返し回数]を設定して使用する。
 *   以下はFORMの例
 *      FIELD1    R2   R3   .  .   R19   (RnはFIELD1のn番目の繰り返し)
 *      FIELD2    R2   R3   .  .   R19   (RnはFIELD2のn番目の繰り返し)
 *        . 
 *        .
 *      FIELD50   R2   R3   .  .   R19   (RnはFIELD50のn番目の繰り返し)
 * 
 */
public class Vrw32alpWrap extends Vrw32alp {

    private int _FieldNum;
    private int _KurikaeshiNum;


    /**
     * メンバ変数_KurikaeshiNumを設定する。
     * @param SVF-FORM FIELDの繰り返し回数。例えばR1,R2...R20の場合は20。
     */
    public void setKurikaeshiNum(int n) {
        _KurikaeshiNum = n;
    }

    /**
     * メンバ変数_FieldNumを設定する。
     * @param SVF-FORM FIELDの付番の最大値。例えばFIELD1,FIELD2...FIELD50の場合は50。
     */
    public void setFieldNum(int n) {
        _FieldNum = n;
    }

    /**
     * @param form
     * @param modo
     * @return
     */
    public int VrsSetForm(
            final String form,
            final int modo
    ) {
        if (null == form || 0 == modo) return 0;
        return super.VrSetForm(form, modo);
    }
    
    /**
     * {@inheritDoc}
     */
    public int VrsOut(
            final String field, 
            final String data
    ) {
        if (null == field || null == data) return 0;
        return super.VrsOut(field, data);
    }

    /**
     * {@inheritDoc}
     */
    public int VrsOutn(
            final String field, 
            final int r,
            final String data
    ) {
        if (null == field || null == data) return 0;
        return super.VrsOutn(field, r, data);
    }

    /**
     * VrsOutを行う。ゼロは回避。
     * @param field：出力先フィールド名
     * @param data：出力対象データ
     */
    public void doSvfOutNonZero(
            final String field,
            final String data
    ) {
        if (null == field || null == data) return;
        if (data.equals("0")) return;
        VrsOut(field, data);
    }

    /**
     * VrsOutを行う。ゼロは回避。
     * @param field：出力先フィールド名
     * @param data：出力対象データ
     */
    public void doSvfOutNonZero(
            final String field,
            final int data
    ) {
        if (null == field) return;
        if (0 == data) return;
        VrsOut(field, String.valueOf(data));
    }

    /**
     * VrsOutnを行う。ゼロは回避。
     * @param field：出力先フィールド名
     * @param r：繰り返し番号
     * @param data：出力対象データ
     */
    public void doSvfOutnNonZero(
            final String field,
            final int r,
            final String data
    ) {
        if (null == field || null == data) return;
        if (data.equals("0")) return;
        VrsOutn(field, r, data);
    }

    /**
     * VrsOutnを行う。ゼロは回避。
     * @param field：出力先フィールド名
     * @param r：繰り返し番号
     * @param data：出力対象データ
     */
    public void doSvfOutnNonZero(
            final String field,
            final int r,
            final int data
    ) {
        if (null == field) return;
        if (0 == data) return;
        VrsOutn(field, r, String.valueOf(data));
    }

    /**
     * VrAttributenを行う。
     * フィールド番号と繰り返し番号は再付番する。
     * 出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * @param field：フィールド名
     * @param n：フィールド番号
     * @param r：繰り返し番号
     * @param data：出力対象データ
     */
    public int doNumberingVrAttributen(
            final String field,
            final int n,
            final int r,
            final String data
    ) {
        if (null == field || 0 == r) return 0;
        return super.VrAttributen(field + getReNumbering(n,0), getReNumbering(r,1), data);
    }

    /**
     * VrsOutを行う。
     * フィールド番号は再付番する。
     * 出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * @param field：フィールド名
     * @param n：フィールド番号
     * @param data：出力対象データ
     * @return
     */
    public int doNumberingSvfOut(
            final String field,
            final int n,
            final String data
    ) {
        if (null == field || null == data) return 0;
        return super.VrsOut(field + String.valueOf(getReNumbering(n,0)), data);
    }

    /**
     * VrsOutnを行う。
     * 繰り返し番号は再付番する。
     * @param field：フィールド名
     * @param r：繰り返し番号
     * @param data：出力対象データ
     * @return
     */
    public int doNumberingSvfOutn(
            final String field, 
            final int r,
            final String data
    ) {
        if (null == field || null == data) return 0;
        return super.VrsOutn(field, getReNumbering(r,1), data);
    }

    /**
     * VrsOutnを行う。
     * 繰り返し番号rがゼロではない場合、
     * 　フィールド番号と繰り返し番号は再付番する。
     * 　出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * 例外として繰り返し番号rがゼロの場合、
     * 　nは再付番し、繰り返し番号として使用する。
     * @param field：出力先フィールド名
     * @param n：フィールドの番号
     * @param r：繰り返しの番号
     * @param data：出力対象データ
     * @return
     */
    public int doNumberingSvfOutn(
            final String field, 
            final int n,
            final int r,
            final String data
    ) {
        if (null == field || null == data) return 0;
        if (0 == r) {
            return super.VrsOutn(field, getReNumbering(n,0), data);
        }
        return super.VrsOutn(field + String.valueOf(getReNumbering(n,0)), getReNumbering(r,1), data);
    }

    /**
     * VrsOutを行う。ゼロは回避。
     * フィールド番号は再付番する。
     * 出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * @param field：フィールド名
     * @param n：フィールド番号
     * @param data：出力対象データ
     * @return
     */
    public void doNumberingSvfOutNonZero(
            final String field,
            final int n,
            final String data
    ) {
        if (null == field || null == data) return;
        if (data.equals("0")) return;
        doNumberingSvfOut(field, n, data);
    }

    /**
     * VrsOutを行う。ゼロは回避。
     * フィールド番号は再付番する。
     * 出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * @param field：フィールド名
     * @param n：フィールド番号
     * @param data：出力対象データ
     */
    public void doNumberingSvfOutNonZero(
            final String field,
            final int n,
            final int data
    ) {
        if (null == field) return;
        if (0 == data) return;
        doNumberingSvfOut(field, n, String.valueOf(data));
    }

    /**
     * VrsOutnを行う。ゼロは回避。
     * フィールド番号と繰り返し番号は再付番する。
     * 出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * @param field：フィールド名
     * @param n：フィールド番号
     * @param r：繰り返し番号
     * @param data：出力対象データ
     */
    public void doNumberingSvfOutnNonZero(
            final String field,
            final int n,
            final int r,
            final String data
    ) {
        if (null == field || null == data) return;
        if (data.equals("0")) return;
        doNumberingSvfOutn(field, n, r, data);
    }

    /**
     * VrsOutnを行う。ゼロは回避。
     * フィールド番号と繰り返し番号は再付番する。
     * 出力先のフィールド名が、フィールド名＋フィールド番号 (FIELD1,FIELD2 ... )の場合使用する。
     * @param field：フィールド名
     * @param n：フィールド番号
     * @param r：繰り返し番号
     * @param data：出力対象データ
     */
    public void doNumberingSvfOutnNonZero(
            final String field,
            final int n,
            final int r,
            final int data
    ) {
        if (null == field) return;
        if (0 == data) return;
        doNumberingSvfOutn(field, n, r, String.valueOf(data));
    }
    
    /**
     * 再付番を行う。
     * 例えば 50行のフィールドあるいは繰り返しの場合、1,2...50,51,52 ==> 1,2...50,1,2 とする。
     * @param n 再付番前の番号。
     * @param div 0はField名の付番、1はField繰り返しの番号。
     * @return  n を再付番して戻す。
     */
    public int getReNumbering(
            final int n,
            final int div
    ) {
        if (n == 0) return n;
        final int l = (div == 0) ? _FieldNum: _KurikaeshiNum;
        return (n % l == 0)? l: n % l;
    }

}
