<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh568Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh568Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),          //データ種別
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),     //テスト名称
                             "SELECT_DIV"       =>  VARS::post("SELECT_DIV"),        //選択区分
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),  //順位
                             "PRINT_COUNT"      =>  VARS::post("PRINT_COUNT"),  //上位出力人数
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
        $this->useKnjd106cJuni1 = (strlen($this->Properties["useKnjd106cJuni1"])) ? $this->Properties["useKnjd106cJuni1"] : 1;
        $this->useKnjd106cJuni2 = (strlen($this->Properties["useKnjd106cJuni2"])) ? $this->Properties["useKnjd106cJuni2"] : 2;
        $this->useKnjd106cJuni3 = (strlen($this->Properties["useKnjd106cJuni3"])) ? $this->Properties["useKnjd106cJuni3"] : 3;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
