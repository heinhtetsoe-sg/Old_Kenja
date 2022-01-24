<?php

require_once('for_php7.php');

class knjmp910Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjmp910Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
    
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "OUTGO_L_CD"       =>  VARS::post("OUTGO_L_CD"),       //支出科目
                             "OUTGO_L_M_CD"     =>  VARS::post("OUTGO_L_M_CD"),     //支出項目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "REQUEST_DATE"     =>  VARS::post("REQUEST_DATE")      //伺い日
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
