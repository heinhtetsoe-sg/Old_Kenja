<?php

require_once('for_php7.php');

class knjp970Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjp970Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "OUTPUT"           =>  VARS::post("OUTPUT"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "SORT"             =>  VARS::post("SORT"),
                             "SCHNAME"          =>  VARS::post("SCHNAME"),
                             "POROW"            =>  VARS::post("POROW"),
                             "POCOL"            =>  VARS::post("POCOL"),
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }
}
?>
