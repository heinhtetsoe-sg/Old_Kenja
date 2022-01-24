<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd616iModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd616iModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "SUBCLASS"             =>  VARS::post("SUBCLASS"),             //科目
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "PRINT_DIV"            =>  VARS::post("PRINT_DIV"),            //出力方法
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "SORT_DIV"             =>  VARS::post("SORT_DIV"),             //出力順
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
