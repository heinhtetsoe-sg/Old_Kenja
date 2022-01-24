<?php

require_once('for_php7.php');

class knjd187tModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ

    public function knjd187tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "TESTCD"           =>  VARS::post("TESTCD"),           //考査種別
                             "GRADE_HRCLASS"    =>  VARS::post("GRADE_HRCLASS"),    //学年
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
}
