<?php

require_once('for_php7.php');

class knje070hModel extends Model
{
    public $field       = array();
    public $select_data = array();
    public $cmd;
    public $control;   //コントロールマスタ

    public function knje070hModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {

        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力指定
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );


        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }
}
