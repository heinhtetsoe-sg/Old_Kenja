<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh564Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh564Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("PROFICIENCYDIV"       =>  VARS::post("PROFICIENCYDIV"),       //データ種別
                             "PROFICIENCYCD"        =>  VARS::post("PROFICIENCYCD"),        //テスト名称
                             "YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //平均点
                             "JUNI"                 =>  VARS::post("JUNI"),                 //基準点
                             "NOT_PRINT_JUNI"       =>  VARS::post("NOT_PRINT_JUNI"),       //順位を印刷しない
                             "NOT_PRINT_SEMESTERNAME" =>  VARS::post("NOT_PRINT_SEMESTERNAME"),  //学期名を印刷しない
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
