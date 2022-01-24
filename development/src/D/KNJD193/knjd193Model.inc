<?php

require_once('for_php7.php');

class knjd193Model extends Model {
    var $semester;
    var $grade;
    var $selectdata;
    var $cmd;
    var $control;        //コントロールマスタ
    function knjd193Model() {}

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->semester    = VARS::post("SEMESTER");
        $this->grade       = VARS::post("GRADE_HRCLASS");

        $this->kamoku_su   = VARS::post("KAMOKU_SU");
        $this->testkindcd  = VARS::post("TESTKINDCD");

        $this->cmd         = VARS::request("cmd");

        $this->field = array("JORETU_DIV" => VARS::post("JORETU_DIV"));
    }
}
?>
