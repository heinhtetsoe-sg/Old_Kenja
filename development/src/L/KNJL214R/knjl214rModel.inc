<?php

require_once('for_php7.php');

class knjl214rModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;
    var $upd_data_base;
    var $upd_data_before;
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)

    function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            $this->field = array();
            unset($this->examyear);
        }
        $this->field = array("APPLICANTDIV" => VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"      => VARS::post("TESTDIV"),       //入試区分
                             "CENTER_TITLE" => VARS::post("CENTER_TITLE")   //重複チェック項目
                     );
        $this->cmd          = VARS::request("cmd");
        $this->examyear     = CTRL_YEAR + 1;
        $this->upd_data_base    = strlen(VARS::post("upd_data_base")) ? preg_split("/,/", VARS::post("upd_data_base")) : array();   //志願者データで更新
        $this->upd_data_before  = strlen(VARS::post("upd_data_before")) ? preg_split("/,/", VARS::post("upd_data_before")) : array(); //事前相談データで更新
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
    }

    function &getMainModel()
    {
    }

    function validateInput()
    {
        if(!strlen($this->field["APPLICANTDIV"])){
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if(!strlen($this->field["TESTDIV"])){
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        return true;
    }

    function &getExecModel()
    {
        if ($this->validateInput()) {
            $ret = knjl214rQuery::ExecuteQuery($this);
            $this->setMessage("MSG202");
        }
    }
}
?>
