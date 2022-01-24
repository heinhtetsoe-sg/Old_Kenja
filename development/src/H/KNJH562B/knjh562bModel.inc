<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh562bModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $schoolKind;
    public $selectdata;

    /**
     * メンバ変数の初期化
     */
    public function knjh562bModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("PROFICIENCYDIV"   =>  VARS::post("PROFICIENCYDIV"),   //データ区分
                             "PROFICIENCYCD"    =>  VARS::post("PROFICIENCYCD"),    //テスト名称
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),        //帳票パターン
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->schoolKind = "H";
        $this->selectdata = VARS::post("selectdata");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
