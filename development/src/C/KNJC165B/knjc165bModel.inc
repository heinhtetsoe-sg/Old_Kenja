<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc165bModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    public function knjc165bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMES_ID"     =>  VARS::post("SEMES_ID"),     //学期
                             "SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),  //校種
                             "COURSECD"     =>  VARS::post("COURSECD"),     //課程
                             "DBNAME"       =>  VARS::post("DBNAME"),       //DB変更に備えて
                             "HR_CLASS_TYPE" => VARS::post("HR_CLASS_TYPE") //集計単位
                     );
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
