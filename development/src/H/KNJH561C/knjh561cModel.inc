<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh561cModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh561cModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),          //データ種別
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),     //テスト名称
                             "PRINT_DIV"        =>  VARS::post("PRINT_DIV"),            //出力方法
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"),        //選択区分
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),  //順位
                             "JUNI"             =>  VARS::post("JUNI"),       //順位の基準点
                             "SORT"             =>  VARS::post("SORT"),       //出力順
                             "RYO_ONLY"         =>  VARS::post("RYO_ONLY"),             //寮生のみ
                             "DBNAME"           =>  VARS::post("DBNAME")      //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
