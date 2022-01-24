<?php

require_once('for_php7.php');

class knjp969Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjp969Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "OUTPUT"           =>  VARS::post("OUTPUT"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }
}
?>
