/*
 * $Id: 8fa84ec19857eb8cac4dc73d59878d6b68706637 $
 *
 * 作成日: 2016/04/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomerBarcode {

    private static final Log log = LogFactory.getLog(CustomerBarcode.class);
    
    public static final String revision = "$Revision: 56595 $";

    static Map kansujis;
    static {
        kansujis = new HashMap();
        kansujis.put("〇", new Integer(0));
        kansujis.put("一", new Integer(1));
        kansujis.put("二", new Integer(2));
        kansujis.put("三", new Integer(3));
        kansujis.put("四", new Integer(4));
        kansujis.put("五", new Integer(5));
        kansujis.put("六", new Integer(6));
        kansujis.put("七", new Integer(7));
        kansujis.put("八", new Integer(8));
        kansujis.put("九", new Integer(9));
        kansujis.put("十", new Integer(10));
        kansujis.put("百", new Integer(100));
    }
    static List replaceTgt;
    static {
        replaceTgt = new ArrayList();
        replaceTgt.add("&");
        replaceTgt.add("＆");
        replaceTgt.add("/");
        replaceTgt.add("／");
        replaceTgt.add("・");
        replaceTgt.add("・");
        replaceTgt.add(".");
        replaceTgt.add("．");
    }
//    static Map customerBarcodeAlphabetMap;
//    static {
//        customerBarcodeAlphabetMap = new HashMap();
//        customerBarcodeAlphabetMap.put("A", Arrays.asList(new String[] {"CC1", "0"}));
//        customerBarcodeAlphabetMap.put("B", Arrays.asList(new String[] {"CC1", "1"}));
//        customerBarcodeAlphabetMap.put("C", Arrays.asList(new String[] {"CC1", "2"}));
//        customerBarcodeAlphabetMap.put("D", Arrays.asList(new String[] {"CC1", "3"}));
//        customerBarcodeAlphabetMap.put("E", Arrays.asList(new String[] {"CC1", "4"}));
//        customerBarcodeAlphabetMap.put("F", Arrays.asList(new String[] {"CC1", "5"}));
//        customerBarcodeAlphabetMap.put("G", Arrays.asList(new String[] {"CC1", "6"}));
//        customerBarcodeAlphabetMap.put("H", Arrays.asList(new String[] {"CC1", "7"}));
//        customerBarcodeAlphabetMap.put("I", Arrays.asList(new String[] {"CC1", "8"}));
//        customerBarcodeAlphabetMap.put("J", Arrays.asList(new String[] {"CC1", "9"}));
//        customerBarcodeAlphabetMap.put("K", Arrays.asList(new String[] {"CC2", "0"}));
//        customerBarcodeAlphabetMap.put("L", Arrays.asList(new String[] {"CC2", "1"}));
//        customerBarcodeAlphabetMap.put("M", Arrays.asList(new String[] {"CC2", "2"}));
//        customerBarcodeAlphabetMap.put("N", Arrays.asList(new String[] {"CC2", "3"}));
//        customerBarcodeAlphabetMap.put("O", Arrays.asList(new String[] {"CC2", "4"}));
//        customerBarcodeAlphabetMap.put("P", Arrays.asList(new String[] {"CC2", "5"}));
//        customerBarcodeAlphabetMap.put("Q", Arrays.asList(new String[] {"CC2", "6"}));
//        customerBarcodeAlphabetMap.put("R", Arrays.asList(new String[] {"CC2", "7"}));
//        customerBarcodeAlphabetMap.put("S", Arrays.asList(new String[] {"CC2", "8"}));
//        customerBarcodeAlphabetMap.put("T", Arrays.asList(new String[] {"CC2", "9"}));
//        customerBarcodeAlphabetMap.put("U", Arrays.asList(new String[] {"CC3", "0"}));
//        customerBarcodeAlphabetMap.put("V", Arrays.asList(new String[] {"CC3", "1"}));
//        customerBarcodeAlphabetMap.put("W", Arrays.asList(new String[] {"CC3", "2"}));
//        customerBarcodeAlphabetMap.put("X", Arrays.asList(new String[] {"CC3", "3"}));
//        customerBarcodeAlphabetMap.put("Y", Arrays.asList(new String[] {"CC3", "4"}));
//        customerBarcodeAlphabetMap.put("Z", Arrays.asList(new String[] {"CC3", "5"}));
//    }

//    static Map checkdigitMap;
//    static String CC1 = "CC1";
//    static String CC4 = "CC4";
//    static {
//        checkdigitMap = new HashMap();
//        checkdigitMap.put("0", new Integer(0));
//        checkdigitMap.put("1", new Integer(1));
//        checkdigitMap.put("2", new Integer(2));
//        checkdigitMap.put("3", new Integer(3));
//        checkdigitMap.put("4", new Integer(4));
//        checkdigitMap.put("5", new Integer(5));
//        checkdigitMap.put("6", new Integer(6));
//        checkdigitMap.put("7", new Integer(7));
//        checkdigitMap.put("8", new Integer(8));
//        checkdigitMap.put("9", new Integer(9));
//        checkdigitMap.put("-", new Integer(10));
//        checkdigitMap.put(CC1, new Integer(11));
//        checkdigitMap.put("CC2", new Integer(12));
//        checkdigitMap.put("CC3", new Integer(13));
//        checkdigitMap.put(CC4, new Integer(14));
//        checkdigitMap.put("CC5", new Integer(15));
//        checkdigitMap.put("CC6", new Integer(16));
//        checkdigitMap.put("CC7", new Integer(17));
//        checkdigitMap.put("CC8", new Integer(18));
//    }
    static String typeAlpha = "alpha";
    static String typeHyphen = "hyphen";
    static String typeNum = "num";
    static String type = "type";

    /**
     * 動作確認テスト
     */
    private static void test(final DB2UDB db2, final Map psMap) {
        String[][] ss = {
                {"3-20-5B604", "東3丁目-20-5　郵便・A&bコーポB604号"},
                {"11-6-1-601", "十一丁目六番地一号　郵便タワー601"},
                {"7-28", "七線　西28"},
                {"6-7-14-2", "6丁目7-14　ABCビル2F"},
                {"6-7-14-2-201", "6丁目7-14　ABCビル2F201号室"},
                {"9-7-6A1-1", "9丁目7-6　郵便シティA棟1F1号"},
                {"6-7LB106", "綾部6-7　LプラザB106"},
        };
        for (int i = 0; i < ss.length; i++) {
            final String e = concat(CustomerBarcode.extract3(ss[i][1]));
            log.debug(" extract3 = " + e + " ( source = " + ss[i][1] + ", expected = " + ss[i][0] + ", pass? " + ss[i][0].equals(e) + "\n\n");
        }
        String[][] sss = {
                {"31700556-7-14-2", "317-0055", "6丁目7-14　ABCビル2F"},
                {"064080429-1524-23-2-", "064-0804", "29丁目1524-23　第2郵便ハウス501"},
//                {"91000673-80-25J1-2CC1", "910-0067", "3丁目80-25　J1ビル2-B"},
                {"", "100-0013", "1丁目3番2号　郵便プラザ503室"},

                {"", "2630023", "千葉市稲毛区緑町3丁目30-8　郵便ビル403号"},
                {"", "0140113", "秋田県大仙市堀見内　南田茂木　添60-1"},
                {"", "1100016", "東京都台東区台東5-6-3　ABCビル10F"},
                {"", "0600906", "北海道札幌市東区北六条東4丁目　郵便センター6号館"},
                {"", "0650006", "北海道札幌市東区北六条東8丁目　郵便センター10号館"},
                {"", "4070033", "山梨県韮崎市龍岡町下條南割　韮崎400"},
                {"", "2730102", "千葉県鎌ケ谷市右京塚　東3丁目-20-5　郵便・A&bコーポB604号"},
                {"", "1980036", "東京都青梅市河辺町十一丁目六番地一号　郵便タワー601"},
                {"", "0270203", "岩手県宮古市大字津軽石第二十一地割大淵川480"},
                {"", "5900016", "大阪府堺市堺区中田出井町四丁六番十九号"},
                {"", "0800831", "北海道帯広市稲田町南七線　西28"},
                {"", "3170055", "茨城県日立市宮田町6丁目7-14　ABCビル2F"},
                {"", "6500046", "神戸市中央区港島中町9丁目7-6　郵便シティA棟1F1号"},
                {"", "6230011", "京都府綾部市青野町綾部6-7　LプラザB106"},
                {"", "0640804", "札幌市中央区南四条西29丁目1524-23　第2郵便ハウス501"},
                {"", "9100067", "福井県福井市新田塚3丁目80-25　J1ビル2-B"},
        };
        for (int i = 0; i < sss.length; i++) {
            log.debug(" source = " + ArrayUtils.toString(sss[i]) + "");
            final String e = CustomerBarcode.getCustomerBarcode(db2, psMap, sss[i][1], sss[i][2]);
            log.debug(" barcode = " + e + ", expected = " + sss[i][0] + ", pass? " + sss[i][0].equals(e) + "\n\n");
        }
    }

    public static String getCustomerBarcode(final DB2UDB db2, final Map psMap, final String zipcd, final String tgt) {

        // 郵便番号とバーコードデータを連結し、チェックデジットを計算する前のカスタマバーコードを生成します。
        List stringlist = new ArrayList();
        if (null != zipcd) {
            for (int i = 0; i < zipcd.length(); i++) {
                if (zipcd.charAt(i) == '-') {
                    continue;
                }
                stringlist.add(String.valueOf(zipcd.charAt(i))); // 連結の際、郵便番号の3〜4けた目の間のハイフンは取り除きます。
            }
        }
        if (null != tgt) {
            final CustomerBarcodeAddress cva = new CustomerBarcodeAddress();
            stringlist.addAll(extract3(cva.getAddressForCustomerBarcodeCalc(db2, psMap, zipcd, tgt)));
        }
        return concat(stringlist);
        
//        // バーコードに必要な文字情報の抜き出し法(4/9) // -> SVFが処理しているようだ
//        // http://www.post.japanpost.jp/zipcode/zipmanual/p20.html
//        // 生成したカスタマバーコード(チェックデジットは未計算)の合計けた数が20けたを超えた場合、以降の文字については切り捨てます。
//        // その際、20〜21けた目がアルファベット文字となった場合、20けた目の英字用制御(CC1、CC2、CC3)は残して以降は切り捨てます。
//        // 注.アルファベット文字の場合、英字用制御コードと数字の2けたでアルファベット1文字を表します。
//        log.debug(" keta = " + stringlist.size());
//        if (20 <= stringlist.size()) {
//            final char c20 = stringlist.get(20 - 1).toString().charAt(0);
//            if (c20 == '-' || CharUtils.isAsciiNumeric(c20)) {
//                // 1.20けた目が数字あるいはハイフンとなる場合 
//                stringlist = stringlist.subList(0, 20);
//            } else if (CharUtils.isAsciiAlphaUpper(c20)) {
//                // 2.20〜21けた目がアルファベット文字となる場合 
//                stringlist = stringlist.subList(0, 20);
//                stringlist.addAll((Collection) customerBarcodeAlphabetMap.get(String.valueOf(c20)));
//            }
//        }
//        checkdigit(stringlist);
//        return concat(stringlist);
    }
    
    private static String concat(final List stringList) {
        final StringBuffer stb = new StringBuffer();
        for (final Iterator it = stringList.iterator(); it.hasNext();) {
            stb.append(it.next());
        }
        return stb.toString();
    }
    
//    private static List checkdigit(final List code) {
//        final List list = new ArrayList();
//        final List check = new ArrayList();
//        for (int i = 0; i < code.size(); i++) {
//            Collection col = (Collection) customerBarcodeAlphabetMap.get(code.get(i));
//            if (null == col) {
//                list.add(code.get(i));
//            } else {
//                list.addAll(col);
//            }
//        }
//        for (int j = 0; j < 20 - code.size(); j++) {
//            list.add(String.valueOf(CC4));
//        }
//        for (int i = 0; i < list.size(); i++) {
//            check.add(checkdigitMap.get(list.get(i)));
//        }
////        log.debug(" check = "+ check + " / list = " + list);
//        int sum = 0;
//        for (int i = 0; i < check.size(); i++) {
//            sum += ((Integer) check.get(i)).intValue();
//        }
//        final int checkdigit = 19 * ((sum / 19) + 1) - sum;
//        log.debug(" check = "+ check + ", sum = " + sum + ", checkdigit = " + checkdigit + " / list = " + list);
//        list.add(String.valueOf(checkdigit));
//        return list;
//    }
    
    private static List extract3(String tgt) {
        // 郵便局 バーコードに必要な文字情報の抜き出し法(3/9)
        // http://www.post.japanpost.jp/zipcode/zipmanual/p19.html
        tgt = process1(tgt);
        tgt = process2(tgt);
        tgt = supple1(tgt);
        LinkedList list;
        list = process3(tgt);
        //log.debug(" list3 = " + list);
        supple2supple3(list, 0);
        list = process4(list, "v4");
        //log.debug(" list4 = " + list);
        list = process5(list, "v4", "v5");
        list = process6(list, "v5", "v6");
        list = supple4(list, "v6", "s4");
        list = supple5(list, "s4", "s5");
        return filter(list, "s5");
    }

    /**
     * キーの値を抽出
     * @param list データのMapのリスト
     * @param valkey キー
     * @return
     */
    private static List filter(final List list, final String valkey) {
        final List string = new ArrayList();
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            if (m.containsKey(valkey)) {
                final String s = (String) m.get(valkey);
                for (int i = 0; i < s.length(); i++) {
                    string.add(String.valueOf(s.charAt(i)));
                }
            }
        }
        return string;
    }

    /**
     * 1.まず、データ内にあるアルファベットの小文字は大文字に置き換えます。
     * @param tgt
     * @return
     */
    private static String process1(String tgt) {
        return tgt.toUpperCase();
    }

    /**
     * 2.同様に、データ内にある"&"等の下記の文字は取り除き、後ろのデータを詰めます。 <br />
     * 「&」(アンパサンド)、「/」(スラッシュ)、「・」(中グロ)、「.」(ピリオド)
     * @param tgt
     * @return
     */
    private static String process2(String tgt) {
        for (final Iterator it = replaceTgt.iterator(); it.hasNext();) {
            final String r = (String) it.next();
            if (-1 != tgt.indexOf(r)) {
                tgt = StringUtils.replace(tgt, r, "");
            }
        }
        return tgt;
    }
    
    private static Map createData(final String kind, final String s) {
        final Map m = new HashMap();
        m.put(type, kind);
        m.put("d", s);
        return m;
    }

    /**
     * 3.1および2で整理したデータから、算用数字、ハイフンおよび連続していないアルファベット1文字を必要な文字情報として抜き出します。
     * @param tgt
     * @return
     */
    private static LinkedList process3(String tgt) {
        LinkedList list = new LinkedList();
        String currentAlphabet = null;
        String currentNum = null;
        String currentHyphen = null;
        String currentEtc = null;
        boolean isNum = false, isAlphabet = false, isHyphen = false, isEtc = false;
        for (int i = 0; i < tgt.length(); i++) {
            char ch = tgt.charAt(i);
            if ('０' <= ch && ch <= '９') {
                ch = (char) (ch - '０' + '0');
            } else if ('Ａ' <= ch && ch <= 'Ｚ') {
                ch = (char) (ch - 'Ａ' + 'A');
            }
            final String chs = String.valueOf(ch);
            isNum = CharUtils.isAsciiNumeric(ch);
            isAlphabet = 'A' <= ch && ch <= 'Z'; // 前処理で小文字を大文字に変換しているため大文字のみ対応
            isHyphen = '-' == ch;
            isEtc = !isNum && !isAlphabet && !isHyphen;

            if (isNum) {
                currentNum = StringUtils.defaultString(currentNum) + chs;
            } else {
                if (null != currentNum) {
                    list.add(createData(typeNum, currentNum));
                }
                currentNum = null;
            }

            if (isAlphabet) {
                currentAlphabet = StringUtils.defaultString(currentAlphabet) + chs;
            } else {
                if (null != currentAlphabet) {
                    list.add(createData(typeAlpha, currentAlphabet));
                }
                currentAlphabet = null;
            }

            if (isHyphen) {
                currentHyphen = StringUtils.defaultString(currentHyphen) + chs;
            } else {
                if (null != currentHyphen) {
                    list.add(createData(typeHyphen, currentHyphen));
                }
                currentHyphen = null;
            }

            if (isEtc) {
                currentEtc = StringUtils.defaultString(currentEtc) + chs;
            } else {
                if (null != currentEtc) {
                    list.add(createData("etc", currentEtc));
                }
                currentEtc = null;
            }
        }
        if (isNum) {
            list.add(createData(typeNum, currentNum));
        } else if (isAlphabet) {
            list.add(createData(typeAlpha, currentAlphabet));
        } else if (isHyphen) {
            list.add(createData(typeHyphen, currentHyphen));
        } else if (isEtc) {
            list.add(createData("etc", currentEtc));
        }
        return list;
    }
    
    /**
     * 補足1: 漢数字が下記の特定文字の前にある場合は抜き出し対象とし、算用数字に変換して抜き出します。<br />
     *  "丁目"　 "丁 "　"番地" 　"番"　 "号" 　"地割" 　"線"　 "の" 　"ノ" 
     * @param tgt 対象文字列
     * @return 処理結果文字列
     */
    private static String supple1(final String tgt0) {
        String tgt = tgt0;
        boolean converted = false;
        final String[] okikaeCheck = new String[] {"丁目", "丁", "番地", "番", "号", "地割", "線", "の", "ノ"};
        for (int ci = 0; ci < okikaeCheck.length; ci++) {
            int idx = tgt.indexOf(okikaeCheck[ci]);
            if (-1 != idx) {
                //log.debug(" found check = " + okikaeCheck[ci] + ", idx = " + idx);
                int seqEnd = -1, seqStart = -1;
                for (int i = idx - 1; i >= 0; i--) {
                    if (kansujis.keySet().contains(tgt.substring(i, i + 1))) {
                        if (seqEnd == -1) {
                            seqEnd = i + 1;
                        }
                        seqStart = i;
                    } else {
                        break;
                    }
                }
                if (-1 != seqEnd && -1 != seqStart) {
                    final String sub = tgt.substring(seqStart, seqEnd);
                    //log.debug(" sub = " + sub + " / check = " + okikaeCheck[ci]);
                    String num = "0";
                    for (int i = 0; i < sub.length(); i++) {
                        //log.debug(" henkan = " + sub.substring(i, i + 1) + " -> " + kansujis.get(sub.substring(i, i + 1)));
                        final int n = ((Integer) kansujis.get(sub.substring(i, i + 1))).intValue();
                        if (n == 100) {
                            if (Integer.parseInt(num) == 0) {
                                num = "100";
                            } else if (Integer.parseInt(num) < 10) {
                                num = String.valueOf(100 * Integer.parseInt(num));
                            }
                        } else if (n == 10) {
                            if (Integer.parseInt(num) == 0) {
                                num = "10";
                            } else if (Integer.parseInt(num) < 10) {
                                num = String.valueOf(10 * Integer.parseInt(num));
                            }
                        } else if (n == 0) {
                            num += String.valueOf(n);
                        } else {
                            num = String.valueOf(Integer.parseInt(num) + n);
                        }
                    }
                    tgt = tgt.substring(0, seqStart) + String.valueOf(num) + tgt.substring(seqEnd);
                    converted = true;
                }
            }
        }
        if (converted) {
            log.debug(" convert tgt = " + tgt + ", src = " + tgt0);
        }
        return tgt;
    }
    
    /**
     * 補足2: 連続していないアルファベット1文字は抜き出し対象となりますが、算用数字に続くアルファベット1文字"F"に限っては抜き出し対象としません。<br />
     * 補足3: 補足2に記述したように、算用数字に続くアルファベット1文字"F"は抜き出し対象となりませんが、更に、"F"以降のデータに抜き出し対象となる文字がある場合、"F"はハイフン1文字に置き換えます。<br />
     * @param list
     * @param idxStart
     * @return
     */
    private static int supple2supple3(final LinkedList list, final int idxStart) {
        int rtn = -1;
        for (int i = idxStart; i < list.size(); i++) {
            final Map map = (Map) list.get(i);
            final String kind = (String) map.get(type);
            final String d = (String) map.get("d");
            if (typeNum.equals(kind)) {
                map.put("v", d);
                rtn = i;
            } else if (typeHyphen.equals(kind)) {
                map.put("v", d.substring(0, 1));
                rtn = i;
            } else if (typeAlpha.equals(kind)) {
                if (d.length() == 1) { // 連続していないアルファベット1文字は抜き出し対象となりますが、算用数字に続くアルファベット1文字"F"に限っては抜き出し対象としません。
                    if ("F".equals(d)) {
                        int idx = supple2supple3(list, i + 1);
                        if (-1 != idx) { // "F"以降のデータに抜き出し対象となる文字がある場合、"F"はハイフン1文字に置き換えます。
                            map.put("v", "-");
                            rtn = i;
                            return i;
                        }
                    } else {
                        rtn = i;
                        map.put("v", d);
                    }
                }
            }
        }
        return rtn;
    }

    /**
     * 4.次に抜き出された文字の前にある下記の文字等は、ハイフン1文字に置き換えます。 <br />
     * 「漢字」、「かな文字」、「カタカナ文字」、「漢数字」、「ブランク」、「2文字以上連続したアルファベット文字」
     * @param list
     * @return
     */
    private static LinkedList process4(final LinkedList list, final String valkey) {
        for (int i = 0; i < list.size(); i++) {
            final Map map = (Map) list.get(i);
            if (map.containsKey("v")) {
                map.put(valkey, map.get("v"));
            } else {
                map.put(type, typeHyphen);
                map.put(valkey, "-");
            }
        }
        // 記述されていないが、図を確認する限りここで後続のハイフンを除去
        for (final ListIterator lit = list.listIterator(list.size()); lit.hasPrevious();) {
            final Map m = (Map) lit.previous();
            if (typeHyphen.equals(m.get(type))) {
                m.remove(valkey);
            } else {
                break;
            }
        }
        return list;
    }
    
    /**
     * 5.4の置き換えで、ハイフンが連続する場合は1つにまとめます。
     * @param tgt
     * @return
     */
    private static LinkedList process5(LinkedList list, final String valkeyBef, final String valkey) {
        changeKey(list, valkeyBef, valkey);
        for (int i = 1; i < list.size(); i++) {
            final Map m = (Map) list.get(i);
            if (typeHyphen.equals(m.get(type))) {
                final Map bm = (Map) list.get(i - 1);
                if (typeHyphen.equals(bm.get(type))) {
                    if (bm.containsKey(valkey)) {
                        bm.remove(valkey);
                    }
                }
            }
        }
        return list;
    }
    
    /**
     * 6.最後に、先頭がハイフンの場合は取り除きます。
     * @param list
     * @return
     */
    private static LinkedList process6(LinkedList list, final String valkeyBef, final String valkey) {
        changeKey(list, valkeyBef, valkey);
        int idx = 0;
        while (list.size() > idx) {
            final Map m = (Map) list.get(idx);
            if (typeHyphen.equals(m.get(type))) {
                if (m.containsKey(valkey)) {
                    m.remove(valkey);
                }
                idx += 1;
            } else {
                break;
            }
        }
        return list;
    }
    
    /**
     * 補足4: 抜き出し後のバーコードデータについて、アルファベット文字の前後にあるハイフンは取り除きます。
     * @param list
     * @return
     */
    private static LinkedList supple4(LinkedList list, final String valkeyBef, final String valkey) {
        changeKey(list, valkeyBef, valkey);
        for (int i = 0; i < list.size(); i++) {
            final Map m = (Map) list.get(i);
            if (typeAlpha.equals(m.get(type))) {
                if (i > 0) {
                    final Map bm = (Map) list.get(i - 1);
                    if (typeHyphen.equals(bm.get(type))) {
                        bm.remove(valkey);
                    }
                }
                if (i < list.size() - 1) {
                    final Map am = (Map) list.get(i + 1);
                    if (typeHyphen.equals(am.get(type))) {
                        am.remove(valkey);
                    }
                }
            }
        }
        return list;
    }
    
    /**
     * 補足5: 4の処理でアルファベット文字の前後にあるハイフンを取り除いた結果、2文字以上の連続したアルファベット文字が残った場合、取り除かないでそのままにします。
     * @param list
     * @return
     */
    private static LinkedList supple5(LinkedList list, final String valkeyBef, final String valkey) {
        changeKey(list, valkeyBef, valkey);
        return list;
    }
    
    /**
     * キーの値をコピーする
     * @param list
     * @param valkeyBef 前キー
     * @param valkey キー
     * @return
     */
    private static LinkedList changeKey(LinkedList list, final String valkeyBef, final String valkey) {
        for (int i = 0; i < list.size(); i++) {
            final Map m = (Map) list.get(i);
            if (m.containsKey(valkeyBef)) {
                m.put(valkey, m.get(valkeyBef));
            }
        }
        return list;
    }

    private static class CustomerBarcodeAddress {
        
        private Map _cache = new HashMap();
    
        private Map zipcdMstPrefCityTownMap(final DB2UDB db2, final Map psMap, final String zipcd) {
            final Map rtn = new HashMap();
            if (null == zipcd) {
                return rtn;
            }
            if (null != _cache.get(zipcd)) {
                return (Map) _cache.get(zipcd);
            }
// off           _cache.put(zipcd, rtn);
            ResultSet rs = null;
            try {
                if (null == psMap.get("ZIPCD_MST")) {
                    
                    String sql = "";
                    sql += " SELECT DISTINCT PREF, CITY, TOWN ";
                    sql += " FROM ZIPCD_MST ";
                    sql += " WHERE NEW_ZIPCD = ? ";
                    sql += "       AND PREF IS NOT NULL AND CITY IS NOT NULL AND TOWN IS NOT NULL ";
                    
                    psMap.put("ZIPCD_MST", db2.prepareStatement(sql));
                }
                final PreparedStatement ps = (PreparedStatement) psMap.get("ZIPCD_MST");
                ps.setString(1, zipcd);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String pref = rs.getString("PREF");
                    if (null == rtn.get(pref)) {
                        rtn.put(pref, new HashMap());
                    }
                    final Map cityMap = (Map) rtn.get(pref);
                    final String city = rs.getString("CITY");
                    if (null == cityMap.get(city)) {
                        cityMap.put(city, new HashSet());
                    }
                    final Collection towns = (Collection) cityMap.get(city);
                    final String town = rs.getString("TOWN");
                    towns.add(town);
                }
                
            } catch (SQLException e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtn;
        }
        
        private String getAddressForCustomerBarcodeCalc(final DB2UDB db2, final Map psMap, final String zipcd, final String addr) {
            if (StringUtils.isBlank(zipcd) || StringUtils.isBlank(addr)) {
                return addr;
            }
            String modaddr = addr;
            final Map prefCityTownMap = zipcdMstPrefCityTownMap(db2, psMap, zipcd);
            if (prefCityTownMap.keySet().size() != 1) { 
                // １つの郵便眼号に都道府県が2つ以上ある場合、もしくは指定の郵便番号がない場合処理しない
                return modaddr;
            }
            final Map.Entry prefCityEntry = (Map.Entry) prefCityTownMap.entrySet().iterator().next();
            final String pref = (String) prefCityEntry.getKey();
            final Map cityMap = (Map) prefCityEntry.getValue();
            if (modaddr.startsWith(pref)) {
                modaddr = modaddr.substring(pref.length());
            }
            while (modaddr.startsWith(" ") || modaddr.startsWith("　")) {
                modaddr = modaddr.substring(1);
            }
            final TreeMap matchScore = new TreeMap();
            for (final Iterator it = cityMap.keySet().iterator(); it.hasNext();) {
                final String city = (String) it.next();
                if (!modaddr.startsWith(city)) {
                    continue;
                }
                String modaddrtown = modaddr.substring(city.length());
                matchScore.put(new Integer(city.length()), modaddrtown);
                final Collection towns = (Collection) cityMap.get(city);
                if (null == towns) {
                    continue;
                }
                for (final Iterator tit = towns.iterator(); tit.hasNext();) {
                    final String town = (String) tit.next();
                    if (modaddrtown.startsWith(town)) {
                        matchScore.put(new Integer(city.length() + town.length()), modaddrtown.substring(town.length()));
                    }
                }
            }
            
            if (matchScore.isEmpty()) {
                return modaddr;
            }
            log.debug(" match = " + matchScore.get(matchScore.lastKey()) + " / " + matchScore + ", cityMap = " + cityMap);
            return (String) matchScore.get(matchScore.lastKey());
        }
    }
    
}

// eof

