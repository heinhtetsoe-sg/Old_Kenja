<?php

require_once('for_php7.php');

class knjd192rModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd192rModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "SUB_TESTCD"           =>  VARS::post("SUB_TESTCD"),           //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "RETESTDATE"           =>  VARS::post("RETESTDATE"),           //再試日付
                             "KETTEN1"              =>  VARS::post("KETTEN1"),               //欠点
                             "KETTEN2"              =>  VARS::post("KETTEN2"),               //欠点
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        $this->Properties["checkKettenDiv"] = '';
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
