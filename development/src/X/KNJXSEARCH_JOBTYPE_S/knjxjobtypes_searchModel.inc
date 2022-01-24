<?php

require_once('for_php7.php');

class knjxjobtypes_searchModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjxjobtypes_searchModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("JOBTYPE_LCD"        => VARS::post("JOBTYPE_LCD"),     //大分類
                             "JOBTYPE_MCD"        => VARS::post("JOBTYPE_MCD"),     //中分類
                             "JOBTYPE_SCD"        => VARS::post("JOBTYPE_SCD")      //小分類CD
                             );
    }
}
?>
