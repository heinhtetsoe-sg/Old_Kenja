<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjb3024Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjb3024Model()
    {
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"),
                             "SEQ"      =>  VARS::post("SEQ"),
                             "BSCSEQ"   =>  VARS::post("BSCSEQ")
                            );

        $this->getPropertiesAll(); //プロパティファイル
    }
    function &getMainModel()
    {
    }

    // 基本時間割自動生成
    function &getExecModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        knjb3024Query::delInsQuery($db, $this);

        $this->setMessage("MSG201");

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

}
//var_dump($_REQUEST);
?>
