<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje371dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata;

    /**
     * メンバ変数の初期化
     */
    public function knje371dModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),         //データ区分
                             "DEPARTMENT_CD"     =>  VARS::post("DEPARTMENT_CD"),     //テスト
                             "SCHOOL_CD"         =>  VARS::post("SCHOOL_CD"),         //学校
                             "FACULTY_CD"        =>  VARS::post("FACULTY_CD"),        //学部
                             "RADIO"            =>  VARS::post("RADIO"),            //出力種別ラジオ
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->selectdata = VARS::post("selectdata");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
