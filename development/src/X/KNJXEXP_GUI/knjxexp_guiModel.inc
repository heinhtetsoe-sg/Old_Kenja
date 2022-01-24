<?php

require_once('for_php7.php');

class knjxexp_guiModel extends Model {
    var $cmd;
    var $programid;         //プログラムＩＤ
    var $target;            //フレーム
    var $usr_auth;
    var $sendAuth;
    var $grade;
    var $check;
    var $search = array();
    var $mode;  //在籍生
    var $exp_year;
    
    function knjxexp_guiModel()
    {
        $this->mode   = "ungrd";  //在籍生
    }
    function init()
    {
        if(VARS::request("PROGRAMID")){
            $this->programid = VARS::request("PROGRAMID");
            unset($this->mode);
            unset($this->grade);
            unset($this->search);
            unset($this->exp_year);
        }
        //年度選択コンボ
        if (VARS::post("EXP_YEAR")) {
            $this->exp_year    = VARS::post("EXP_YEAR");
        }
        if(VARS::get("TARGET")){
            $this->target[$this->programid] = VARS::get("TARGET");
        }
        if(VARS::get("PATH")){
            $this->path[$this->programid] = VARS::get("PATH");
        }
        if(strlen($this->sendAuth) == 0){
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        if (strlen($this->sendAuth) > 0) {
            $this->usr_auth = $this->sendAuth;
        }
        $this->cmd          = VARS::request("cmd");
        if (VARS::post("GRADE")) {
            $this->grade    = VARS::post("GRADE");
        }
        if ($this->cmd == "list"){
            unset($this->search);
        }
        //検索モード
        if(VARS::request("mode")){
            $this->mode = VARS::request("mode");
        }
        $this->getPropertiesAll(); //プロパティファイル
    }
}
?>
