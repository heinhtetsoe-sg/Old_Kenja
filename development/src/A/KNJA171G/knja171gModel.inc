<?php

require_once('for_php7.php');

class knja171gModel extends Model {
    var $semester;
    var $hrClass;
    var $field = array();
    var $cmd;
    var $control;

    function knja171gModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("HR_CLASS_TYPE"    =>  VARS::post("HR_CLASS_TYPE"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "MEIBO_DIV"        =>  VARS::post("MEIBO_DIV"),        //名簿選択 (1:後援会名簿 2:むらさき会名簿)
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
