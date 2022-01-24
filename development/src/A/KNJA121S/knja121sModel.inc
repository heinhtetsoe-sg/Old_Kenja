<?php

require_once('for_php7.php');

class knja121sModel extends Model {
    var $cmd;
    var $syomeiBtn = false; //署名ボタンの表示/非表示
    var $schoolCd;
    var $existPem;

    /**
     * メンバ変数の初期化
     */
    function knja121sModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->syomeiBtn = false;

        $db = Query::dbCheckOut();

        $query = knja121sQuery::getNameMst("Z010");
        $nmMstRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolCd = $nmMstRow["NAME2"];

        Query::dbCheckIn($db);

        //クッキー無し(初回表示時)
        if ($this->cmd == "https") {
            $this->existPem = "";
            //証明書をクッキーへ格納
            setcookie("I9e58bUn", $this->getP12(), time() + 60 * 60, "/");  //2009/11/27 miyabe

            //ランダム文字列をセット
            $this->randm = "";
            //2010.01.21 K.M ランダム文字列を16進数化
            $arr_hex = array('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f');
            for ($i = 0; $i < 20; $i++) {
                $this->randm .= $arr_hex[mt_rand(0,15)];
            }
        }
    }

    //PEMファイル読込（証明書）
    function getP12()   //2009/11/11 K.M
    {

        //P12形式個人証明書ファイルを読込む
        $filename = DOCUMENTROOT."/image/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".p12";
        if (!file_exists($filename)) {
            $this->existPem = "NONE";
        } else {
            $fd = fopen($filename, "r");
            $cert_bin = fread($fd, filesize($filename));
            fclose($fd);
            $cert_asc = base64_encode($cert_bin);
        }
        return $cert_asc;
    }

    //2009/11/11 K.M
    function getUpdateModel()
    {
        $cert_pem  = "";                    // 検証処理PEM形式個人証明書読込みエリア
        $cert_asc  = VARS::COOKIE("I9e58bUn");  // クッキーの証明書
        $passwd    = VARS::POST("PASSWD");  // パスワード
        $signature = "";                    // 署名
        $this->syomeiBtn = false;

        $cert_bin = base64_decode($cert_asc);
        if (openssl_pkcs12_read( $cert_bin, $p12cert, $passwd)) {
            $privatekey = $p12cert["pkey"]; //プライベートキー取出し
            $pkeyid = openssl_get_privatekey($privatekey);
            if ($pkeyid) {
                // 署名
                openssl_sign($this->randm, $signature_bin, $pkeyid);
                // base64に変換
                $signature = base64_encode($signature_bin);
                // メモリからキーを開放
                openssl_free_key($pkeyid);
            } else {
                $this->setMessage("プライベートキー取出しに失敗しました。");
                return;
            }
        }
        else {
            $this->setMessage("認証に失敗しました。{".$passwd."}");
            return;
        }

        //公開鍵取出し
        $pubkeyid = openssl_pkey_get_public($p12cert["cert"]);    //2010.04.14
        if ($pubkeyid) {
            // base64のデータを元に戻す
            $signature_bin = base64_decode($signature);
            // 検証
            $exeResult = openssl_verify($this->randm, $signature_bin, $pubkeyid);
            // メモリからキーを開放
            openssl_free_key($pubkeyid);
        } else {
            $this->setMessage("公開鍵取出しに失敗しました。");
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja121sQuery::insUpDatQuery($db, $this, $signature, $exeResult);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        //検証結果
        if ($exeResult == 1) {
            //指導要録ボタン表示
            $this->syomeiBtn = true;
            //パスワードをクッキーへ格納
            $s_passwd = common::passwdEncode($passwd,$this->randm);
            setcookie("hoj3RG8t", $s_passwd, time() + 60 * 60, "/");
            $this->setMessage("認証に成功しました。");
        } else {
            $this->setMessage("検証に失敗しました。");
        }

        return;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
