<?php

require_once('for_php7.php');

class knjd617uModel extends Model {
    var $field = array();
    var $cmd;
    var $subclassGroup;

    function knjd617uModel() {}

    function init() {
        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),     //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),        //学年
                             "MAJOR"                =>  VARS::post("MAJOR"),        //課程学科
                             "TESTCD"               =>  VARS::post("TESTCD"),       //テスト
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),    //グループ
                             "SORT_DIV"             =>  VARS::post("SORT_DIV"),     //出力順
                             "DBNAME"               =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->subclassGroup = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

    }
}
?>
