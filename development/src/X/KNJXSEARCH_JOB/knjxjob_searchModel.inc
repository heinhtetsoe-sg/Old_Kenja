<?php

require_once('for_php7.php');

class knjxjob_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $target_number;

    function knjxjob_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("SORT_TYPE"     => VARS::post("SORT_TYPE"),
                             "JOBTYPE_SNAME" => VARS::post("JOBTYPE_SNAME")
                             );

        if (!$this->cmd) {
            unset($this->target_number);
        }

        if (VARS::get("target_number")) {
            $this->target_number = VARS::get("target_number");
        }
    }
}
?>
