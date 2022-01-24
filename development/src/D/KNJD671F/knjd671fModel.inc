<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd671fModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd671fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS_TYPE"        =>  VARS::post("HR_CLASS_TYPE"),        //クラス選択
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "RANK"                 =>  VARS::post("RANK"),                 //順位
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
