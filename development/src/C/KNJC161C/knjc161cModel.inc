<?php

require_once('for_php7.php');

class knjc161cModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード
    public $selectdata;

    public function knjc161cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"),
                             "GRADE"    =>  VARS::post("GRADE"),
                             "DATE"     =>  VARS::post("DATE"),
                             "DBNAME"   =>  VARS::post("DBNAME")
                            );

        $this->selectdata  = VARS::post("selectdata");
        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
