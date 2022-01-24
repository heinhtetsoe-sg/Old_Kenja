<?php

require_once('for_php7.php');

class knjj193Model extends Model {
    var $field = array();
    var $cmd;

    function knjj193Model() { }

    function init() {

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),
                             "SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),
                             "TYOUSHI_FLG"      =>  VARS::post("TYOUSHI_FLG"),
                             "IDOU_FLG"         =>  VARS::post("IDOU_FLG"),
                             "IDOU_DATE"        =>  VARS::post("IDOU_DATE"),
                             "S_ROW"            =>  VARS::post("S_ROW"),
                             "S_COL"            =>  VARS::post("S_COL"),
                             "DBNAME"           =>  VARS::post("DBNAME"));

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
