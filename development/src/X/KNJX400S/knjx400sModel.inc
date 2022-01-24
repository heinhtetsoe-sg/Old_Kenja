<?php

require_once('for_php7.php');

class knjx400sModel extends Model {
    var $cmd;
    var $schoolCd;
    var $existPem;
    var $setUrl;
    var $gosign;
    var $signature;
    var $randm;
    var $seqno;
    var $seqno_save;
    var $crtext;

    /**
     * メンバ変数の初期化
     */
    function knjx400sModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->gosign = VARS::POST("GOSIGN");
        $this->seqno = VARS::POST("SEQNO");
        $this->seqno_save = VARS::POST("SEQNO_SAVE");
        $this->signature = VARS::POST("SIGNATURE");

        //起動時
        $this->randm = VARS::POST("RANDOM");
        if (strlen($this->randm) < 20) {
            //ランダム文字列をセット
            unset($this->randm);
            $arr_hex = array('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f');
            for ($i = 0; $i < 20; $i++) {
                $this->randm .= $arr_hex[mt_rand(0,15)];
            }
        }

        $db = Query::dbCheckOut();

        if (VARS::get("setUrl")) {
            $this->setUrl = VARS::get("setUrl");
        }
        $query = knjx400sQuery::getNameMst("Z010");
        $nmMstRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolCd = $nmMstRow["NAME2"];

        //起動時・電子証明書を読込む
        $this->crtext = VARS::POST("CRTEXT");
        if (strlen($this->crtext) < 20) {
            $cert_pem  = "";                    // 検証処理PEM形式個人証明書読込みエリア
            $cert_pem  = $this->getPem();       // サーバの証明書
            $this->crtext = base64_encode($cert_pem);
        }

        Query::dbCheckIn($db);
    }

    //crtファイル読込（証明書）2011.03.22
    function getPem()
    {
        //crtファイルのパス指定
        $filename = DOCUMENTROOT."/etc_system/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".crt";
        $this->existPem = "";
        //cet形式個人証明書ファイルを読込む
        if (!file_exists($filename)) {
            $this->existPem = "NONE";
        } else {
            $fd = fopen($filename, "r");
            $cert_asc = fread($fd, filesize($filename));
            fclose($fd);
            if (strlen($cert_asc)==0){
                $this->gosign="NE";
            }
        }
        return $cert_asc;
    }

    function getUpdateModel()
    {
        //P12形式個人証明書ファイルパスワードチェック
        if (strcmp($this->gosign, "CANCEL") == 0) {
            $this->gosign="";
            $this->setMessage("認証はCANCELしました。");
            return;
        }
        if (strcmp($this->gosign, "OK") != 0) {
            $this->gosign="";
            $this->setMessage("認証に失敗しました。");
            return;
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $ret = 1;

        //認証確認ボタン表示
        $this->seqno_save = $this->seqno;
        $this->setMessage("認証に成功しました。");

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
