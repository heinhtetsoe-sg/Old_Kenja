<?php

require_once('for_php7.php');

class knjd105fModel extends Model {
    var $field = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    function knjd105fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd  = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        //初期値の設定
        $this->subclassGroup = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
    }
}
?>
