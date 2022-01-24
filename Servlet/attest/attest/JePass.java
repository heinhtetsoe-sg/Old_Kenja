// kanji=漢字
/*
 * $Id: JePass.java 56575 2017-10-22 11:23:32Z maeshiro $
 *
 * 作成者: miyabe
 *
 * Copyright(C) 2014-2017 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package attest;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.applet.*;
import java.awt.*;
import netscape.javascript.JSObject;

//〇jar新規作成
// d:
// cd D:\0-ALP\CVS_WORK\attest\e-classes
// jar cf jApplet.jar attest
//
//〇jar内容表示
// jar vtf jApplet.jar
//
//〇署名します。
// 熊本：jarsigner -keystore jarapkey.jks -storepass jarapkey jApplet.jar jarapkey
// 益城：jarsigner -keystore mashikiapkey.jks -storepass mashikiapkey jApplet.jar mashikiapkey
// 京都：jarsigner -keystore kyotoapkey.jks -storepass kyotoapkey jApplet.jar kyotoapkey
// \\pc300pl\F\ファイル共有\賢者システム\指導要録・認証システム\宮部さんの資料\
// の各学校フォルダーにも署名のやり方があります。
//
//〇jApplet.jar設置
// 署名したファイルは、/usr/local/Applet/Alp/KNJA122S/
// に設置します。
// 各学校毎のKNJA122S_KUMA等もありますので、KNJA122S_KYOUTOには京都署名のjApplet.jar
// KNJA122S_MASIKIには、益城署名のjApplet.jar。各学校毎に署名適用して下さい。
// ※alpokiappの環境は熊本です。
//
//公開鍵と非公開鍵のペアを作成、公開鍵は常に自己署名証明書でラップされます。
//keytool -genkey -alias alpapkey -keystore alpapkey.jks
//公開鍵にCAで署名する為、CAに送る証明書署名要求を作成します。
//keytool -certreq -alias alpapkey -keystore alpapkey.jks -file alpapkey.csr
//予めCA の証明書をインポートします。
//keytool -import -alias rootca -keystore alpapkey.jks -file alp_r_ca.der
//デジタル署名用の証明書をインポートします。
//keytool -import -alias alpapkey -keystore alpapkey.jks -file alpapkey.der
//署名用の証明書のエイリアスを同一にすることに注意。
public class JePass extends Applet {

    /**
     * 認証USBキー処理
     * 2010/12/22　ログをSystem.out.printlnで行う
     */
    private static final long serialVersionUID = 1L;

    private static final int length = 344;

    private String staff;
    private String exediv;
    private String schregNo;

    public void init() {
        Logging("start Applet");
        // パラメータ
        final String password = getParameter("PASS"); // 必須
        final String planeText = getParameter("RANDM"); // ATTESTのみあり(VIEWSは、DBより取得)
        this.setBackground(Color.white);
        staff = getParameter("STAFF");
        exediv = getParameter("EXEDIV"); // VIEWS：所見、ATTEST：認証、ALL：一括
        schregNo = getParameter("SCHREGNO");
        String[] schregNos = null;
        String[] signatures = null;
        if ("ALL".equals(exediv)) {
            schregNos = schregNo.split(",");
        }

        String signSep = "";
        String[] arg = null;
        if ("ALL".equals(exediv)) {
            arg = new String[3];
            arg[0] = "";
            arg[1] = "";
            arg[2] = "";
        } else {
            arg = new String[2];
            arg[0] = "";
            arg[1] = "";
        }
        if (password != null && password.length() > 0 && planeText != null && planeText.length() > 0 && staff != null) {
            String signature;
            JSObject win = JSObject.getWindow(this);
            try {
                signature = sign(password, planeText);
                Logging("結果=[" + signature + "] length=" + signature.length());
                if (signature.length() >= length && !"ATTEST".equals(exediv)) {
                    signatures = signature.split(",");
                    for (int i = 0; i < signatures.length; i++) {
                        if (signatures[i].length() > 0) {
                            if ("ALL".equals(exediv)) {
                                arg[0] += signSep + signatures[i];
                                arg[1] += signSep + "OK";
                                arg[2] += signSep + schregNos[i];
                            } else {
                                arg[0] = signatures[i];
                                arg[1] = "OK";
                            }
                            Logging("結果=[" + signatures[i] + "] length=" + signatures[i].length());
                        } else {
                            if ("ALL".equals(exediv)) {
                                arg[0] += signSep + "";
                                arg[1] += signSep + "NG";
                                arg[2] += signSep + schregNos[i];
                            } else {
                                arg[0] = "";
                                arg[1] = "NG";
                            }
                        }
                        signSep = ",";
                    }
                } else if (signature.length() >= length && "ATTEST".equals(exediv)) {
                    arg[0] = signature;
                    arg[1] = "OK";
                    Logging("結果 Sign:" + arg[1]);
                } else {
                    Logging("ePass error :" + signature);
                }
            } catch (UnsupportedEncodingException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
                arg[0] = new String("");
                arg[1] = new String("UnsupportedEncodingException");
            } catch (IOException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
                arg[0] = new String("");
                arg[1] = new String("IOException");
            }
            win.call("recvValue", arg);
        } else {
            Logging("not request");
        }
        Logging("stop Applet $Revision: 56575 $");
    }

    // 署名関数
    public String sign(String password, String planeText)
            throws UnsupportedEncodingException, IOException {
        Logging("EXEDIV:" + exediv + " randm:" + planeText + " staff:"
                        + staff);
        StringBuffer signature = new StringBuffer();
        try {
            // コマンド実行
            String[] cmd = { "C:/ALP/UsbKey/ePass.exe",
                    password + "," + planeText };
            Process prs = Runtime.getRuntime().exec(cmd);
            InputStream in = prs.getInputStream(); // 結果を標準出力から読込む

            byte[] setBuff = new byte[1024];
            int totalCount = 0; //出力結果の全体長
            int count;
            //in.readは、出力結果がなくなるまで実行する。
            //setBuff(1024)まで入れて返す。残ってればもう一回って感じ
            while (-1 != (count = in.read(setBuff, 0, setBuff.length))) {
                // 結果を作業エリアへ格納する
                signature.append(new String(setBuff, 0, count));
                totalCount += count;
            }

            if (0 == totalCount) {
                Logging("パスワード不正");
                return signature.toString();
            }
            if (length > totalCount) {
                Logging("署名エラー :" + "読込みバイト数=" + String.valueOf(totalCount));
                return signature.toString();
            }

            prs.waitFor();

            // 結果を作業エリアへ格納する
            Logging(password + "," + planeText);

        } catch (Exception e) {
            Logging("署名コマンド実行エラー");
            e.printStackTrace();
        }
        return signature.toString();
    }

    public void Logging(String msg){
        Date date1 = new Date();  //Dateオブジェクトを生成
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd kk:mm:ss.SSS ");  //Dateオブジェクトを表示
        System.out.println(sdf1.format(date1)+msg);  //ログを出力
    }
}