/**
 * @(#)Svfxsample.java  200710/26
 *
 * SVF for Java Print
 *
 * Sample Program
 *
 */

import jp.co.fit.vfreport.*;

public class Svfxsample {

  public static void main(String[] args) {
    int ret;
    Vrw32 svf = new Vrw32();
    ret = svf.VrInit();
    ret = svf.VrSetPrinter("", "PDF");
    ret = svf.VrSetSpoolFileName2("Svfxsample.pdf");
    ret = svf.VrSetForm("svfxhachusho.xml", 4);
    ret = svf.VrsOut("発行年月日", "2007/10/26 00000");
    ret = svf.VrsOut("発注番号", "1000476");
    ret = svf.VrsOut("仕入先名", "ウイングアーク テクノロジーズ株式会社");
    ret = svf.VrsOut("仕入先郵便番号", "〒111-1111");
    ret = svf.VrsOut("仕入先住所", "東京都○○区□□町△△1-1-1");
    ret = svf.VrsOut("仕入先電話番号", "03-1234-5678");
    ret = svf.VrsOut("支払条件", "納入翌月末現金払");
    ret = svf.VrsOut("納品場所", "〒222-2222 東京都○△区□○町2-2-2");
    ret = svf.VrsOut("発注明細番号", "1000522");
    ret = svf.VrsOut("商品名", "SVFX-Designer");
    ret = svf.VrsOut("単価", "700000.00");
    ret = svf.VrsOut("数量", "2.00");
    ret = svf.VrsOut("金額", "1400000.00");
    ret = svf.VrsOut("仕入先製品番号", "SVF01");
    ret = svf.VrEndRecord();
    ret = svf.VrsOut("発行年月日", "2007/10/26 00000");
    ret = svf.VrsOut("発注番号", "1000476");
    ret = svf.VrsOut("仕入先名", "ウイングアーク テクノロジーズ株式会社");
    ret = svf.VrsOut("仕入先郵便番号", "〒111-1111");
    ret = svf.VrsOut("仕入先住所", "東京都○○区□□町△△1-1-1");
    ret = svf.VrsOut("仕入先電話番号", "03-1234-5678");
    ret = svf.VrsOut("支払条件", "納入翌月末現金払");
    ret = svf.VrsOut("納品場所", "〒222-2222 東京都○△区□○町2-2-2");
    ret = svf.VrsOut("発注明細番号", "1000523");
    ret = svf.VrsOut("商品名", "SVF for JavaPrint");
    ret = svf.VrsOut("単価", "600000.00");
    ret = svf.VrsOut("数量", "3.00");
    ret = svf.VrsOut("金額", "1800000.00");
    ret = svf.VrsOut("仕入先製品番号", "SVF02");
    ret = svf.VrEndRecord();
    ret = svf.VrsOut("発行年月日", "2007/10/26 00000");
    ret = svf.VrsOut("発注番号", "1000476");
    ret = svf.VrsOut("仕入先名", "ウイングアーク テクノロジーズ株式会社");
    ret = svf.VrsOut("仕入先郵便番号", "〒111-1111");
    ret = svf.VrsOut("仕入先住所", "東京都○○区□□町△△1-1-1");
    ret = svf.VrsOut("仕入先電話番号", "03-1234-5678");
    ret = svf.VrsOut("支払条件", "納入翌月末現金払");
    ret = svf.VrsOut("納品場所", "〒222-2222 東京都○△区□○町2-2-2");
    ret = svf.VrsOut("発注明細番号", "1000524");
    ret = svf.VrsOut("商品名", "SVF for PDF");
    ret = svf.VrsOut("単価", "800000.00");
    ret = svf.VrsOut("数量", "1.00");
    ret = svf.VrsOut("金額", "800000.00");
    ret = svf.VrsOut("仕入先製品番号", "SVF03");
    ret = svf.VrEndRecord();
    ret = svf.VrPrint();
    ret = svf.VrQuit();
  }
}
