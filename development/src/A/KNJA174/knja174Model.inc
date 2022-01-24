<?php

require_once('for_php7.php');

class knja174Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;

    function knja174Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"),
                             "GRADE"    =>  VARS::post("GRADE"),
                             "DBNAME"   =>  VARS::post("DBNAME")
                            );

        $this->selectdata  = VARS::post("selectdata");
        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

}
?>
