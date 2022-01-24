<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd617gModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ
    var $schoolName;
    var $schoolCode;

    /**
     * メンバ変数の初期化
     */
    function knjd617gModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "PRINT_DIV"            =>  VARS::post("PRINT_DIV"),            //出力方法
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "SORT_DIV"             =>  VARS::post("SORT_DIV"),             //出力順
                             "AVG_DIV"              =>  VARS::post("AVG_DIV"),              //順位の基準点
                             "ATTEND_SUBCLASS"      =>  VARS::post("ATTEND_SUBCLASS"),      //親科目のみを対象とした平均点や順位を印字
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;

    }

    function clean() {}
}
?>
