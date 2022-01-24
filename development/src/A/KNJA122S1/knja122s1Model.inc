<?php

require_once('for_php7.php');

class knja122s1Model extends Model {
    var $cmd;
    var $syomeiBtn = false; //署名ボタンの表示/非表示
    var $passwd;
    var $schoolCd;
    var $existPem;
    var $setUrl;

    /**
     * メンバ変数の初期化
     */
    function knja122s1Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->syomeiBtn = false;
        $this->passwd = VARS::POST("PASSWD") ? VARS::POST("PASSWD") : $this->passwd;

        //起動時
        if ($this->cmd == "https") {
            //ランダム文字列をセット
            unset($this->randm);
            unset($this->passwd);
            for ($i = 0; $i < 20; $i++) {
                $this->randm .= mt_rand(0,9);
            }
        }

        $db = Query::dbCheckOut();

        if (VARS::get("setUrl")) {
            $this->setUrl = VARS::get("setUrl");
        }
        $query = knja122s1Query::getNameMst("Z010");
        $nmMstRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolCd = $nmMstRow["NAME2"];

        Query::dbCheckIn($db);
    }

    //PEMファイル読込（証明書）
    function getPem()
    {

        $this->existPem = "";
        //PEM形式個人証明書ファイルを読込む
        $filename = DOCUMENTROOT."/image/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".pem";

        if (!file_exists($filename)) {
            $this->existPem = "NONE";
        } else {
            $fd = fopen($filename, "r");
            $cert_asc = fread($fd, filesize($filename));
            fclose($fd);
        }
        return $cert_asc;
    }

    function getUpdateModel()
    {
        $cert_pem  = "";                    // 検証処理PEM形式個人証明書読込みエリア
        $cert_pem  = $this->getPem();       // サーバの証明書
        $signature = "";                    // 署名
        $this->syomeiBtn = false;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //USBでの署名データ取出し
        $query = knja122s1Query::getSignature();
        $signature = $db->getOne($query);

        /** 検証処理 **/
        //ランダム文字をsha1(バイナリ)
        $randmSha1 = sha1($this->randm);

        //DBの署名データをbase64_decode(sha1にする)してファイルに書き込む
        $signature_bin = base64_decode($signature);
        $filename = tempnam('/tmp', STAFFCD);
        $fd = fopen($filename, "w");
        fwrite($fd, $signature_bin);
        fclose($fd);

        //PEMファイルのパス指定
        $pem = DOCUMENTROOT."/image/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".pem";;
        //verifyの結果を書き込むファイル(randamのsha1した値が入る)
        $digestFile = DOCUMENTROOT."/image/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".txt";
        //コマンド作成
        $command  = "openssl rsautl -verify -pubin -inkey {$pem} -certin -in {$filename} -out {$digestFile}";
        //コマンド実行
        passthru($command, $ret);
        //実行結果ファイル読込
        $filename = $digestFile;
        $fd = fopen($filename, "r");
        if (filesize($filename) > 0) {
            $digest = fread($fd, filesize($filename));
        }
        fclose($fd);

        //実行結果ファイル(sha1)を読み込んでランダム(sha1)と比較
        if ($digest != $randmSha1) {
            $this->setMessage("認証に失敗しました。");
            return;
        }

        //実行結果０は、１とする。(とりあえず)
        $ret = $ret == 0 ? 1 : $ret;
        $query = knja122s1Query::insUpDatQuery($db, $this, $signature, $ret);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        //検証結果
        if ($ret == 1) {
            //指導要録ボタン表示
            $this->syomeiBtn = true;
            //パスワードをクッキーへ格納
            setcookie("passwd", $this->passwd, time() + 60 * 30, "/");
            $this->setMessage("認証に成功しました。");
        } else {
            $this->setMessage("認証に失敗しました。".$ret);
            unset($this->passwd);
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
