<?php

require_once('for_php7.php');

class knjd667vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $useSlumpD048;

    function knjd667vModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "SCHOOLKIND"           =>  VARS::post("SCHOOLKIND"),           //校種
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "TESTKIND_CD"          =>  VARS::post("TESTKIND_CD"),          //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GRADECOLOR"           =>  VARS::post("GRADECOLOR"),           //学年カラー指定
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        $this->testCd = array("010101", // 中間考査
                              "020101"  // 期末考査
                              );

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
