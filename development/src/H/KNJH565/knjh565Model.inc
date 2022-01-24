<?php

require_once('for_php7.php');

/**
 * 定数定義
 */

class knjh565Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh565Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "PROFICIENCYDIV"       =>  VARS::post("PROFICIENCYDIV"),       //データ区分
                             "PROFICIENCYCD"        =>  VARS::post("PROFICIENCYCD"),        //テスト名称
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "JUNI"                 =>  VARS::post("JUNI"),                 //順位の基準点
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
