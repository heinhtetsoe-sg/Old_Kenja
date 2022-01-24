<?php

require_once('for_php7.php');


class knjf100dModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjf100dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHKIND"          =>  VARS::post("SCHKIND"),          //校種
                             "COMMITTEE"        =>  VARS::post("COMMITTEE"),        //委員会
                             "DATE1"            =>  VARS::post("DATE1"),            //日付FROM
                             "DATE2"            =>  VARS::post("DATE2")             //日付TO
                            );

        $this->cmd         = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
