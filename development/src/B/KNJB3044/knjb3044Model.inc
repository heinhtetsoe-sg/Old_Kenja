<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjb3044Model extends Model {
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
    function knjb3044Model()
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

        $this->field = array(
            "YEAR_SEME" => VARS::post("YEAR_SEME"),
            "PRESEQ"    => VARS::post("PRESEQ"),
            "BSCSEQ"    => VARS::post("BSCSEQ")
        );

        if ($this->field["YEAR_SEME"]){
            $list = explode('-', $this->field["YEAR_SEME"]);
            $this->year = $list[0];
            $this->semester = $list[1];
        } else {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイル
    }

    function &getMainModel()
    {
    }

    // 科目からの講座自動生成
    function &getExecModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        knjb3044Query::delInsQuery($db, $this);

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
        return;
    }

}
//var_dump($_REQUEST);
?>
