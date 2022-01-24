/*
 * $Id: dafdd2b52f00a4dcf1133873b7acb8b189bea95c $
 *
 * 作成日: 2016/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.svf.Vrw32alp;

public class SvfField implements Comparable {

    private static final Log log = LogFactory.getLog(SvfField.class);
    
    private static String revision = "$Revision: 56595 $";

    public static String AttributeX = "X";
    public static String AttributeY = "Y";
    public static String AttributeSize = "Size";
    public static String AttributeKeta = "Keta";
    public static String AttributeZenFont = "ZenFont";
    public static String AttributeHanFont = "HanFont";
    public static String AttributeHanZen = "HanZen";
    public static String AttributeTateBai = "TateBai";
    public static String AttributeYokoBai = "YokoBai";
    public static String AttributeMeido = "Meido";
    public static String AttributeBold = "Bold";
    public static String AttributeItalic = "Italic";
    public static String AttributeOutLine = "OutLine";
    public static String AttributeShadow = "Shadow";
    public static String AttributeHensyu = "Hensyu";
    public static String AttributeDataStyle = "DataStyle";
    public static String AttributeDirection = "Direction";
    public static String AttributeRotation = "Rotation";
    public static String AttributeCalc = "Calc";
    public static String AttributeEdit = "Edit";
    public static String AttributeLinkField = "LinkField";
    public static String AttributeTsuzuri = "Tsuzuri";
    public static String AttributeLock = "Lock";
    public static String AttributeTotalPageCount = "TotalPageCount";
    public static String AttributeSerialPageCount = "SerialPageCount";
    
    static final String[] AttributeNames = {AttributeX, AttributeY, AttributeZenFont, AttributeHanFont, AttributeSize, AttributeKeta, AttributeHanZen, AttributeTateBai, AttributeYokoBai, AttributeMeido, AttributeBold, AttributeItalic, AttributeOutLine, AttributeShadow, AttributeHensyu, AttributeDataStyle, AttributeDirection, AttributeRotation, AttributeCalc, AttributeEdit, AttributeLinkField, AttributeTsuzuri, AttributeLock, AttributeTotalPageCount, AttributeSerialPageCount, "$SPOOLFILENAME$"};
    
    public final int _idx;
    boolean _isTmp;
    public String _name;

    private static String getString(final Map m, final Object colname) {
        if (m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(colname)) {
            throw new RuntimeException("not found column \"" + colname + "\" in " + m);
        }
        return (String) m.get(colname);
    }

    private static int toInt(final String s, final int def) {
        return NumberUtils.isNumber(s) ? ((int) Double.parseDouble(s)) : def;
    }
    
    private static double toDouble(final String s, final double def) {
        return NumberUtils.isNumber(s) ? Double.parseDouble(s) : def;
    }


    // 0:文字型 1:数値型 2:バーコード 3:ビットマップ 負数:エラーステータス
    int _fieldType;
    private String fieldTypeString() {
        switch (_fieldType) {
        case 0: return "MOJI";
        case 1: return "SUUCHI";
        case 2: return "BARCODE";
        case 3: return "BITMAP";
        }
        if (_fieldType < 0) {
            return "ERROR!";
        }
        return null;
    }

    /**
     * 桁数
     * リンクフィールドの場合
     *   リンク元フィールドならリンク先フィールドをあわせた桁数、
     *   リンク先フィールドなら0
     */
    public int _fieldLength;
    
    /**
     * レコードの属性
     *  0: レコードには属さないフィールド
     *  1: 明細レコードに属するフィールド
     *  2: 合計レコードに属するフィールド
     *  3: ヘッダーレコードに属するフィールド
     *  4: 総計レコードに属するフィールド
     */
    public int _fieldRecordType;

    private String fieldRecordTypeStr() {
        switch (_fieldRecordType) {
        case 0: return "NONE";
        case 1: return "MEISAI";
        case 2: return "GOUKEI";
        case 3: return "HEADER";
        case 4: return "SOUKEI";
        }
        return null;
    }

    /**
     * 繰り返し数
     * 繰り返しフィールドでない場合は、0
     */
    public int _fieldRepeatCount;

    /**
     * 開始X座標           | X               |  0 以上（ドット）
     * 開始Y座標           | Y               |  0 以上（ドットで）
     * 全角フォント        | ZenFont         |  0:明朝体 1:ゴシック
     * 半角フォント        | HanFont         |  0:全角フォント 1:クーリエ
     * 文字サイズ          | Size            |  1.0〜96.0
     * 桁数                | Keta            |  1〜256
     * 半角／全角          | HanZen          |  0:半角 1:全角
     * 縦倍率              | TateBai         |  0.5,1.0,2.0,3.0,4.0,6.0,8.0
     * 横倍率              | YokoBai         |  0.5,1.0,2.0,3.0,4.0,6.0,8.0
     * 明度                | Meido           |  0:黒 100:白
     * ボールド            | Bold            |  0:しない 1:する
     * イタリック          | Italic          |  0:しない 1:する
     * アウトライン        | OutLine         |  0:しない 1:する
     * シャドウ            | Shadow          |  0:しない 1:する
     * 編集スタイル        | Hensyu          |  0:無編集  1:右詰め 2:左詰め 3:中央割付 4:均等割付 5:小数点位置固定右詰め
     * データ型            | DataStyle       |  0:文字1:数値　※
     * 印字方向            | Direction       |  0:横 1:縦
     * 回転                | Rotation        |  0,90,180,270
     * 計算式              | Calc            |  計算式　※
     * 編集式              | Edit            |  編集式　※
     * リンクフィールド名  | LinkField       |  リンクするフィールド名
     * 綴りページ指定      | Tsuzuri         |  0:印字しない 1:印字する
     * 入力ロック          | Lock            |  0:しない 1:する
     * 総ページ数          | TotalPageCount  |  総ページ数
     * 帳票連番の取得      | SerialPageCount |  帳票連番
     * ファイル名の取得    | $SPOOLFILENAME$ |  ファイル名
     * 
     * ※の項目については、VrSetForm 関数のモードが4 または5 のレポートライターモードでのVrAttributeメソッド使用はできません。
     */
    final Map _attributeMap = new HashMap();
    SvfField(final int idx) {
        _idx = idx;
    }
    
    public int x() {
        return toInt(getString(_attributeMap, AttributeX), -1);
    }
    
    public int y() {
        return toInt(getString(_attributeMap, AttributeY), -1);
    }
    
    /**
     * フォントサイズ
     */
    public double size() {
        return toDouble(getString(_attributeMap, AttributeSize), -1);
    }

    /**
     * ※
     * @return
     */
    public Map getAttributeMap() {
        return _attributeMap;
    }

    public static Map getSvfFormFieldInfoMap(final Vrw32alp svf) {
        final Map fieldInfoMap = new TreeMap();
        final int fieldCount = svf.VrGetFieldCount();
        //for (int i = 0; i <= fieldCount; i++) {
        for (int i = 0; i < fieldCount; i++) { // マニュアルでは「以下」となっているがエラーが発生する
            StringBuffer tmp = new StringBuffer();
            svf.VrGetFieldName(i, tmp);
            final SvfField field = new SvfField(i);
            field._name = tmp.toString();
            fieldInfoMap.put(new Integer(i), field);
            
            field._fieldType = svf.VrGetFieldType(i);
            field._fieldLength = svf.VrGetFieldLength(i);
            field._fieldRecordType = svf.VrGetFieldRecordType(i);
            field._fieldRepeatCount = svf.VrGetFieldRepeatCount(i);
            if (field._fieldRepeatCount > 0) { // マニュアルでは「個数」となっているが-1された値が返る
                field._fieldRepeatCount += 1;
            }

            for (int j = 0; j < SvfField.AttributeNames.length; j++) {
                StringBuffer tmp2 = new StringBuffer();
                svf.VrGetAttribute(field._name, SvfField.AttributeNames[j], tmp2);
                field._attributeMap.put(SvfField.AttributeNames[j], tmp2.toString());
            }
            //svfform._pageRecordCount = _svf.VrGetPageRecordCount();
        }
        return fieldInfoMap;
    }

    public static Map getSvfFormFieldInfoMapGroupByName(final Vrw32alp svf) {
        final Map fieldInfoMap = getSvfFormFieldInfoMap(svf);
        final Map groupByName = new TreeMap();
        for (final Iterator it = fieldInfoMap.values().iterator(); it.hasNext();) {
            final SvfField field = (SvfField) it.next();
            groupByName.put(field._name, field);
        }
        return groupByName;
    }
    
    /**
     * 新しい属性値をセットしたフィールドを得る
     * @param newAttributeMap 新しい属性値
     * @return 新しい属性値をセットしたフィールド
     */
    public SvfField set(final Map newAttributeMap) {
        final SvfField newField = new SvfField(_idx);
        newField._name = _name;
        newField._fieldLength = _fieldLength;
        newField._fieldRecordType = _fieldRecordType;
        newField._fieldType = _fieldType;
        newField._attributeMap.putAll(_attributeMap);
        for (final Iterator it = newAttributeMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String key = (String) e.getKey();
            final String value = (String) e.getValue();
            
            if (AttributeX.equals(key) || AttributeY.equals(key) || AttributeSize.equals(key)) {
            } else if (AttributeKeta.equals(key)) {
                newField._fieldLength = Integer.parseInt(value);
            }
            newField._attributeMap.put(key, value);
        }
        newField._isTmp = true;
        return newField;
    }
    
    public Map attributeMapToStringMap() {
        final Map rtn = new HashMap();
        for (final Iterator it = _attributeMap.keySet().iterator(); it.hasNext();) {
            final String attrName = (String) it.next();
            String value = (String) _attributeMap.get(attrName);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            if (AttributeZenFont.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "明朝体";
                    continue;
                } else if ("1".equals(value)) {
                    value = "ゴシック";
                }
            } else if (AttributeHanFont.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "全角フォント";
                    continue;
                } else if ("1".equals(value)) {
                    value = "クーリエ";
                }
            } else if (AttributeHanZen.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "半角";
                } else if ("1".equals(value)) {
                    value = "全角";
                }
            } else if (AttributeTateBai.equals(attrName) || AttributeYokoBai.equals(attrName)) {
                if ("1.0".equals(value)) {
                    continue;
                }
            } else if (AttributeBold.equals(attrName) || AttributeItalic.equals(attrName) || AttributeOutLine.equals(attrName) || AttributeShadow.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "しない";
                    continue;
                } else if ("1".equals(value)) {
                    value = "する";
                }
            } else if (AttributeHensyu.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "無編集";
                    continue;
                } else if ("1".equals(value)) {
                    value = "右詰め";
                } else if ("2".equals(value)) {
                    value = "左詰め";
                } else if ("3".equals(value)) {
                    value = "中央割付";
                } else if ("4".equals(value)) {
                    value = "均等割付";
                } else if ("5".equals(value)) {
                    value = "小数点位置固定右詰め";
                }
            } else if (AttributeDataStyle.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "文字";
                    continue;
                } else if ("1".equals(value)) {
                    value = "数値";
                }
            } else if (AttributeDirection.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "横";
                    continue;
                } else if ("1".equals(value)) {
                    value = "縦";
                }
            } else if (AttributeRotation.equals(attrName)) {
                if ("0".equals(value)) {
                    continue;
                }
            } else if (AttributeTsuzuri.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "印字しない";
                } else if ("1".equals(value)) {
                    value = "印字する";
                    continue;
                }
            } else if (AttributeLock.equals(attrName)) {
                if ("0".equals(value)) {
                    value = "しない";
                    continue;
                } else if ("1".equals(value)) {
                    value = "する";
                }
            } else if (AttributeMeido.equals(attrName)) {
                if ("0".equals(value)) {
                    continue;
                }
            } else if (AttributeTotalPageCount.equals(attrName)) {
                if ("0".equals(value)) {
                    continue;
                }
            } else if (AttributeSerialPageCount.equals(attrName)) {
                continue;
            }
            rtn.put(attrName, value);
        }
        return rtn;
    }
    
    /**
     * 対象文字列を数値と文字列に分けたオブジェクトのリストを得る
     * (".._2"が".._10"の前に位置するようにソートするため)
     * @param str 対象文字列
     * @return 対象文字列を数値と文字列に分けたオブジェクトのリスト
     */
    private static Object[] toStringNum(final String name) {
        final List list = new ArrayList();
        char beforeCh = (char) -1;
        StringBuffer current = null;
        for (int i = 0; i < name.length(); i++) {
            final char ch = name.charAt(i);
            if (beforeCh != -1) {
                final boolean isDiff = Character.isDigit(beforeCh) && !Character.isDigit(ch) || !Character.isDigit(beforeCh) && Character.isDigit(ch);
                if (isDiff) {
                    current = null;
                }
            }
            if (null == current) { 
                current = new StringBuffer();
                list.add(current);
            }
            current.append(ch);
            beforeCh = ch;
        }
        final Object[] array = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (NumberUtils.isDigits(list.get(i).toString())) {
                array[i] = Integer.valueOf(list.get(i).toString());
            } else {
                array[i] = list.get(i).toString();
            }
        }
        return array;
    }
    
    private int compare(final Object[] snum1, final Object[] snum2) {
        int cmp;
        //log.debug("compare:" + ArrayUtils.toString(snum1) + " <> " + ArrayUtils.toString(snum2));
        for (int i = 0; i < Math.min(snum1.length, snum2.length); i++) {
            if (snum1[i].getClass() != snum2[i].getClass()) {
                log.debug("couldn't compare:" + ArrayUtils.toString(snum1) + " <> " + ArrayUtils.toString(snum2));
                return 0;
            }
            if (snum1[i] instanceof String) {
                cmp = ((String) snum1[i]).toUpperCase().compareTo(((String) snum2[i]).toUpperCase());
            } else {
                cmp = ((Comparable) snum1[i]).compareTo(snum2[i]);
            }
            if (0 != cmp) {
                return cmp;
            }
        }
        if (snum1.length < snum2.length) {
            return -1;
        } else if (snum1.length > snum2.length) {
            return 1;
        }
        return 0;
    }
    
    public int compareTo(final Object o) {
        if (null == o || !(o instanceof SvfField)) {
            return -1;
        }
        final SvfField of = (SvfField) o;
        if (null == of._name) {
            return -1;
        }
        if (null == _name) {
            return 1;
        }
        int cmp;
        cmp = compare(toStringNum(_name), toStringNum(of._name));
        if (0 != cmp) {
            return cmp;
        }
        cmp = new Integer(_idx).compareTo(new Integer(of._idx));
        return cmp;
    }

    public String toString() {
        final StringBuffer stb = new StringBuffer();
        stb.append("SvfField(idx = " + _idx);
        stb.append(", name = " + _name);
        if (!"MOJI".equals(fieldTypeString())) {
            stb.append(", fieldType = " + fieldTypeString());
        }
        stb.append(", length = " + _fieldLength);
        if (!"NONE".equals(fieldRecordTypeStr())) {
            stb.append(", recordType = " + fieldRecordTypeStr());
        }
        stb.append(", attr = " + attributeMapToStringMap());
        stb.append(")");
        return stb.toString();
    }
}

// eof

