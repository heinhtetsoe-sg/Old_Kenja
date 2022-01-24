<?php

require_once('for_php7.php');

class knjd618uModel extends Model {
    var $field = array();
    var $cmd;

    function knjd618uModel() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"                =>  VARS::post("GRADE"),        //学年
                             "COURSE"               =>  VARS::post("COURSE"),       //コース
                             "TESTCD"               =>  VARS::post("TESTCD"),       //テスト
                             "DBNAME"               =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
