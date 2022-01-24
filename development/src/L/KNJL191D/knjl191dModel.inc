<?php

require_once('for_php7.php');

class knjl191dModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $desirediv;

    function knjl191dModel(){
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd          = VARS::request("cmd");

        $this->examyear     = VARS::post("YEAR");
        $this->applicantdiv = "1";  //固定値

        $this->testdiv       = VARS::post("TESTDIV");
        $this->desirediv     = VARS::post("DESIREDIV");

        $this->field = array("SEND_TO"      => VARS::post("SEND_TO"),   //送付先
                             "POROW"        =>  VARS::post("POROW"),    //開始位置 行
                             "POCOL"        =>  VARS::post("POCOL"),    //開始位置 列
                            );
    }
}
?>
