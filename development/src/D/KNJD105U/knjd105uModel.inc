<?php

require_once('for_php7.php');

class knjd105uModel extends Model {
    var $field = array();
    var $cmd;
    var $subclassGroup;

    function knjd105uModel() {}

    function init() {

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "MAJOR"                =>  VARS::post("MAJOR"),                //課程学科
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //年組
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //初期値の設定
        $this->subclassGroup = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
    }
}
?>
