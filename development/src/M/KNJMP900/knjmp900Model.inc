<?php

require_once('for_php7.php');

class knjmp900Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjmp900Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
    
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "INCOME_L_CD"      =>  VARS::post("INCOME_L_CD"),      //収入科目
                             "INCOME_L_M_CD"    =>  VARS::post("INCOME_L_M_CD"),    //収入項目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "REQUEST_DATE"     =>  VARS::post("REQUEST_DATE")      //伺い日
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
