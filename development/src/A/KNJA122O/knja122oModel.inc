<?php
class knja122oModel extends Model {
    var $cmd;
    var $syomeiBtn = false; //署名ボタンの表示/非表示
    var $passwd;
    var $schoolCd;
    var $existPem;
    var $setUrl;
    var $gosign;    //2010.02.10
    var $signature; //2010.02.10
    var $randm;     //2010.02.10

    /**
     * メンバ変数の初期化
     */
    function knja122oModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->syomeiBtn = false;
        $this->passwd = VARS::POST("PASSWD") ? VARS::POST("PASSWD") : $this->passwd;
          //2010.02.10
        $this->gosign = VARS::POST("GOSIGN");
        $this->signature = VARS::POST("SIGNATURE");

        //起動時
        $this->randm = VARS::POST("RANDM");
        if (strlen($this->randm) < 20) {
            //ランダム文字列をセット
            unset($this->randm);
            unset($this->passwd);
            $arr_hex = array('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f');
            for ($i = 0; $i < 20; $i++) {
                $this->randm .= $arr_hex[mt_rand(0,15)];
            }
        }

        $db = Query::dbCheckOut();

        if (VARS::get("setUrl")) {
            $this->setUrl = VARS::get("setUrl");
        }
        $query = knja122oQuery::getNameMst("Z010");
        $nmMstRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolCd = $nmMstRow["NAME2"];

        Query::dbCheckIn($db);
    }

    //PEMファイル読込（証明書）2010.04.15
    function getPem()
    {

        $this->existPem = "";
        //P12形式個人証明書ファイルを読込む
        $filename = DOCUMENTROOT."/image/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".p12";
        if (!file_exists($filename)) {
            $this->existPem = "NONE";
        } else {
            $fd = fopen($filename, "r");
            $cert_bin = fread($fd, filesize($filename));
            fclose($fd);
            if (openssl_pkcs12_read( $cert_bin, $p12cert, $this->passwd))
                return $p12cert["cert"];
            else
                $this->gosign="NE";
        }
        return "";
    }

    function getUpdateModel()
    {
        $cert_pem  = "";                    // 検証処理PEM形式個人証明書読込みエリア
        $cert_pem  = $this->getPem();       // サーバの証明書
        $this->syomeiBtn = false;
        $ret = 0;

        //P12形式個人証明書ファイルパスワードチェック
        if (strcmp($this->gosign,"NE")==0) {
            unset($this->passwd);
            $this->gosign="";
            $this->setMessage("認証に失敗しました。");
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //USBでの署名データ取出し
        if (strcmp($this->gosign,"OK")!=0) {
            unset($this->passwd);
            $this->gosign="";
            $this->setMessage("認証処理に失敗しました。");
            return;
        }

        /** 検証処理 **/
        //ランダム文字をsha1(バイナリ)
        $randmSha1 = sha1($this->randm);

        //公開鍵取出し
        $pubkeyid = openssl_pkey_get_public($cert_pem);
        if ($pubkeyid) {
            // 署名が正しいかどうかを確認
            $signature_bin=base64_decode($this->signature);
            if (openssl_public_decrypt($signature_bin, $digest, $pubkeyid)==TRUE) {
                //実行結果ファイル(sha1)を読み込んでランダム(sha1)と比較
                if (trim($digest) == trim($randmSha1))
                    $ret = 1;
//echo "{".$digest."==".$randmSha1."}".$ret;
            }
            // メモリからキーを開放
            openssl_free_key($pubkeyid);
        }
        else {
            $ret = -1;
            $this->setMessage("Fail openssl_get_publickey");
        }
        if ($ret == 1) {
            //指導要録ボタン表示
            $this->syomeiBtn = true;
            //パスワードをクッキーへ格納
            $s_passwd = common::passwdEncode($this->passwd,$this->randm);
            //setcookie("passwd", $this->passwd, time() + 60 * 30, "/");
            setcookie("hoj3RG8t", $s_passwd, time() + 60 * 30, "/");
            $this->setMessage("認証に成功しました。");
        } elseif ($ret == 0) {
            $this->setMessage("認証に失敗しました。");
            unset($this->passwd);
            return;
        } else {
            $this->setMessage("署名を確認する際にエラーが発生しました".$ret);
            unset($this->passwd);
            return;
        }

        //実行結果
        $query = knja122oQuery::insUpDatQuery($db, $this, $this->signature, $ret);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

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
