<?php

require_once('for_php7.php');

class knje451Model extends Model {
    var $field = array();
    var $cmd;

    function knje451Model() {}

    function init() {

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "STAFFCD"          =>  VARS::post("STAFFCD"),          //職員コード
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
