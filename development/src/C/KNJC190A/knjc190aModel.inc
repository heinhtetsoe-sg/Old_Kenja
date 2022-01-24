<?php

require_once('for_php7.php');

class knjc190aModel extends Model {
    var $field = array();
    var $cmd;

    function knjc190aModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("DISP"         =>  VARS::post("DISP"),             //表示選択
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //出力選択
                             "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),      //出力選択
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
    }
}
?>
